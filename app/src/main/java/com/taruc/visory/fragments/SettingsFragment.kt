package com.taruc.visory.fragments


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.taruc.visory.R
import com.taruc.visory.ui.LandingActivity


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
        fragmentManager
            ?.beginTransaction()
            ?.replace(R.id.settings_fragment, MySettingsFragment())
            ?.commit()
    }

    class MySettingsFragment : PreferenceFragmentCompat() {
        private lateinit var auth: FirebaseAuth

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
                val builder = AlertDialog.Builder(requireContext())

                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()

                val mGoogleSignInClient = GoogleSignIn.getClient(this.requireActivity(), gso)

                builder
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes"){ _, _ ->
                        mGoogleSignInClient?.signOut()
                        auth.signOut()
                        activity?.onBackPressed()
                        activity?.let{
                            val intent = Intent(it, LandingActivity::class.java)
                            it.startActivity(intent)
                            it.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            return@let true
                        }
                    }
                    .setNegativeButton("No"){ _, _ ->
                        Toast.makeText(context, "Good to see you back!", Toast.LENGTH_SHORT).show()
                    }

                val alertDialog = builder.create()
                alertDialog.show()
            }

            return false
        }
    }
}
