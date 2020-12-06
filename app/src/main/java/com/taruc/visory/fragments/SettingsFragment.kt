package com.taruc.visory.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.quickblox.users.QBUsers
import com.taruc.visory.R
import com.taruc.visory.quickblox.db.QbUsersDbManager
import com.taruc.visory.quickblox.services.LoginService
import com.taruc.visory.quickblox.utils.CALL_TOPIC
import com.taruc.visory.quickblox.utils.CALL_TOPIC_END
import com.taruc.visory.quickblox.utils.Helper
import com.taruc.visory.quickblox.utils.TOPIC
import com.taruc.visory.ui.LandingActivity
import com.taruc.visory.utils.LoggedUser

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "Settings"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.settings_fragment, MySettingsFragment())
            .commit()
    }

    class MySettingsFragment : PreferenceFragmentCompat() {
        private lateinit var auth: FirebaseAuth
        private val TAG = "SettingsFragment"

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            auth = FirebaseAuth.getInstance()
            val view = super.onCreateView(inflater, container, savedInstanceState)
            view?.setBackgroundColor(Color.rgb(239, 239, 244))
            return view
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            val key = preference?.key

            if(key.equals("logout_button")){
                logoutFromVisory()
            }

            return false
        }

        private fun logoutFromVisory() {
            val builder = AlertDialog.Builder(requireContext())

            //Unsubscribe from Notification Topics for Volunteer
            val loggedUser = LoggedUser(requireContext())
            when (loggedUser.getUserType()){
                1 -> {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC).addOnFailureListener {
                        Log.d(TAG, "TOPIC was unsuccessful")
                    }
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(CALL_TOPIC).addOnFailureListener {
                        Log.d(TAG, "CALL_TOPIC was unsuccessful")
                    }
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(CALL_TOPIC_END).addOnFailureListener {
                        Log.d(TAG, "CALL_TOPIC_END was unsuccessful")
                    }
                }
            }

            builder
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes, log out"){ _, _ ->
                    //Logout from Quickblox
                    logoutFromQuickblox()

                    //Determine if Google or Facebook credentials used for login
                    if (loggedUser.getProvider() == getString(R.string.provider_fb)) {
                        logoutFromFacebook()
                    } else if (loggedUser.getProvider() == getString(R.string.provider_google)) {
                        logoutFromGoogle()
                    }

                    //Logout from Firebase Authentication
                    auth.signOut()

                    val sharedPreference =  requireContext().getSharedPreferences("sharedPrefs",
                        Context.MODE_PRIVATE)
                    sharedPreference.edit().clear().apply()

                    activity?.finishAffinity()
                    activity?.let{
                        val intent = Intent(it, LandingActivity::class.java)
                        it.startActivity(intent)
                        it.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    }
                }
                .setNegativeButton("Cancel"){ _, _ ->
                    Toast.makeText(context, "Good to see you back!", Toast.LENGTH_SHORT).show()
                }

            val alertDialog = builder.create()
            alertDialog.show()
        }

        private fun logoutFromFacebook() {
            if (LoginManager.getInstance() != null){
                LoginManager.getInstance().logOut()
            }
        }

        private fun logoutFromGoogle() {
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            val mGoogleSignInClient = GoogleSignIn.getClient(this.requireActivity(), gso)
            mGoogleSignInClient?.signOut()
        }

        private fun logoutFromQuickblox() {
            LoginService.logout(this.requireContext())
            removeAllUserData()
        }

        private fun removeAllUserData() {
            Helper.clearAllData()
            QbUsersDbManager.clearDB()
            QBUsers.signOut().performAsync(null)
        }
    }
}
