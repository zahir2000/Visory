package com.taruc.visory.volunteer

import android.app.ActivityManager
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.core.request.GenericQueryRule
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.squareup.picasso.Picasso
import com.taruc.visory.R
import com.taruc.visory.blind.*
import com.taruc.visory.fragments.SettingsFragment
import com.taruc.visory.quickblox.DEFAULT_USER_PASSWORD
import com.taruc.visory.quickblox.activities.CallActivity
import com.taruc.visory.quickblox.db.QbUsersDbManager
import com.taruc.visory.quickblox.services.CallService
import com.taruc.visory.quickblox.services.LoginService
import com.taruc.visory.quickblox.util.loadUsersByPagedRequestBuilder
import com.taruc.visory.quickblox.util.signInUser
import com.taruc.visory.quickblox.util.signUp
import com.taruc.visory.quickblox.utils.EXTRA_IS_INCOMING_CALL
import com.taruc.visory.quickblox.utils.Helper
import com.taruc.visory.report.loggedUser
import com.taruc.visory.utils.LoadingDialog
import com.taruc.visory.utils.LoggedUser
import com.taruc.visory.utils.UserCount
import com.taruc.visory.utils.loadUsers
import kotlinx.android.synthetic.main.fragment_volunteer_home.*
import kotlinx.android.synthetic.main.profile_card.*
import kotlinx.android.synthetic.main.user_stats.*
import kotlin.collections.ArrayList

class VolunteerHomeFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var user: QBUser
    private var uid: String = ""
    private var fullName: String = ""
    private lateinit var con: ViewGroup
    private var progressDialog: Dialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("FragmentLifeCycle", "onAttach")

        showProgress()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FragmentLifeCycle", "onCreate")
        setHasOptionsMenu(true)

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    it.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            createNewUser()
                            Log.d("InternetConnection", "Internet is available")
                        }

                        override fun onLost(network: Network?) {
                            Log.d("InternetConnection", "Internet is not available")
                        }
                    })
                }
            }
        }

        val loggedUser = LoggedUser(requireContext())
        Log.d("ContactNo", loggedUser.getUserContact())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("FragmentLifeCycle", "onCreateView")

        con = container!!

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_volunteer_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FragmentLifeCycle", "onViewCreated")

        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Home"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val loggedUserPrefs = LoggedUser(this.requireActivity().baseContext)
        uid = loggedUserPrefs.getUserID()
        fullName = loggedUserPrefs.getUserName()

        button_tutorial.setOnClickListener(this)
        button_help_someone.setOnClickListener(this)

        createNewUser()
        startLoginService()
        updateUI()
    }

    override fun onStart() {
        super.onStart()
        Log.d("FragmentLifeCycle", "onStart")

        if (!checkIsLoggedInChat()) {
            startLoginService()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("FragmentLifeCycle", "onResume")

        val isIncomingCall = Helper[EXTRA_IS_INCOMING_CALL, false]
        if (isCallServiceRunning(CallService::class.java)) {
            CallActivity.start(this.requireActivity().applicationContext, isIncomingCall)
        }
        clearAppNotifications()

        Handler().postDelayed({
            hideProgress()
        }, 5000)
    }

    private fun showProgress(){
        hideProgress()
        progressDialog = LoadingDialog.showLoadingDialog(this.requireContext())
    }

    private fun hideProgress(){
        progressDialog?.let { if(it.isShowing)it.cancel() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_settings -> {
                val settingsFragment = SettingsFragment()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(con.id, settingsFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun checkIsLoggedInChat(): Boolean {
        if (!QBChatService.getInstance().isLoggedIn) {
            startLoginService()
            return false
        }
        return true
    }

    private fun startLoginService() {
        if (Helper.hasQbUser()) {
            Log.d("VolunteerQBUser", Helper.getQbUser().fullName)
            LoginService.start(this.requireActivity().applicationContext, Helper.getQbUser())
        } else {
            Log.d("VolunteerQBUser", "Not Found! Creating new user now!")
            createNewUser()
        }
    }

    private fun createNewUser(){
        val user = createQBUser()
        signUpNewUser(user)
    }

    private fun createQBUser(): QBUser {
        val qbUser = QBUser()
        val tags = StringifyArrayList<String>()
        val loggedUser = LoggedUser(requireContext())

        qbUser.login = loggedUser.getUserID()
        qbUser.fullName = loggedUser.getUserName()
        qbUser.password = DEFAULT_USER_PASSWORD

        tags.add("volunteer")
        tags.add(loggedUser.getUserLanguage())
        qbUser.tags = tags

        return qbUser
    }

    private fun signUpNewUser(newUser: QBUser) {
        signUp(newUser, object : QBEntityCallback<QBUser> {
            override fun onSuccess(result: QBUser, params: Bundle) {
                Helper.saveQbUser(newUser)
                loginToChat(result)
            }

            override fun onError(e: QBResponseException) {
                if (e.httpStatusCode == ERROR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                    signInCreatedUser(newUser)
                } else {
                    Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun loginToChat(qbUser: QBUser) {
        qbUser.password = DEFAULT_USER_PASSWORD
        user = qbUser
        startLoginService(qbUser)
    }

    private fun startLoginService(qbUser: QBUser) {
        val intent = Intent(activity, LoginService::class.java)
        //val tempIntent = activity!!.startService(intent)
        val pendingIntent = requireActivity().createPendingResult(EXTRA_LOGIN_RESULT_CODE, intent, 0)
        LoginService.start(requireActivity().baseContext, qbUser, pendingIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == EXTRA_LOGIN_RESULT_CODE) {

            var isLoginSuccess = false
            data?.let {
                isLoginSuccess = it.getBooleanExtra(EXTRA_LOGIN_RESULT, false)
            }

            var errorMessage = "Unknown Error"
            data?.let {
                errorMessage = it.getStringExtra(EXTRA_LOGIN_ERROR_MESSAGE)!!
            }

            if (isLoginSuccess) {
                Helper.saveQbUser(user)
                signInCreatedUser(user)
            } else {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInCreatedUser(user: QBUser) {
        signInUser(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(result: QBUser, params: Bundle) {
                Helper.saveQbUser(user)
                updateUserOnServer(user)
            }

            override fun onError(responseException: QBResponseException) {
                Toast.makeText(context, "Sign in error.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUserOnServer(user: QBUser) {
        user.password = null
        QBUsers.updateUser(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(updUser: QBUser?, params: Bundle?) {
                Log.d("QBUser", "Finished creating user in server")
                hideProgress()
            }

            override fun onError(responseException: QBResponseException?) {
                Log.d("QBUser", "Error creating user in server")
                hideProgress()
            }
        })
    }

    private fun updateUI() {
        updateUserInfo()
        updateUserCount()
    }

    private fun updateUserInfo(){
        val loggedUserTypePref = LoggedUser(this.requireActivity().baseContext)
        profile_joindate.text =
            getString(R.string.member_since, loggedUserTypePref.getUserJoinDate())
        profile_name.text = loggedUserTypePref.getUserName()
        profile_language.text = loggedUserTypePref.getUserLanguage()

        if (loggedUserTypePref.getAvatarUrl().isNotEmpty()
            && loggedUserTypePref.getAvatarUrl().compareTo("null") != 0
        ) {
            val imageView = activity?.findViewById<ImageView>(R.id.image_profile_profile)
            Picasso.get().load(loggedUserTypePref.getAvatarUrl()).into(imageView)
        }
    }

    private fun updateUserCount() {
        val userCount = UserCount(this.requireContext())

        Log.d("UserCount", userCount.getVolCount().toString())

        try {
            if (userCount.getBviCount() != 0 && userCount.getVolCount() != 0) {
                user_stats_view.visibility = View.VISIBLE
                textViewBlindCount.text = userCount.getBviCount().toString()
                textViewVolunteerCount.text = userCount.getVolCount().toString()
            } else {
                user_stats_view.visibility = View.VISIBLE
                textViewBlindCount.text = "0"
                textViewVolunteerCount.text = "0"
            }
        } catch (e: Exception) { }

        loadUsers(requireContext())
    }

    private fun isCallServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = manager.getRunningServices(Integer.MAX_VALUE)
        for (service in services) {
            if (CallService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun clearAppNotifications() {
        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_tutorial -> {
                activity?.let {
                    val intent = Intent(it, TutorialActivity::class.java)
                    it.startActivity(intent)
                }
            }
            R.id.button_help_someone->{
                activity?.let {
                    val intent = Intent(it, ShowLocationActivity::class.java)
                    it.startActivity(intent)
                }
            }
        }
    }
}
