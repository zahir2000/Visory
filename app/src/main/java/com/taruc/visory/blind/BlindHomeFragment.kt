package com.taruc.visory.blind

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
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCTypes
import com.taruc.visory.R
import com.taruc.visory.fragments.SettingsFragment
import com.taruc.visory.mlkit.ObjectDetectorActivity
import com.taruc.visory.mlkit.TextDetectionActivity
import com.taruc.visory.quickblox.DEFAULT_USER_PASSWORD
import com.taruc.visory.quickblox.activities.CallActivity
import com.taruc.visory.quickblox.db.QbUsersDbManager
import com.taruc.visory.quickblox.services.CallService
import com.taruc.visory.quickblox.services.LoginService
import com.taruc.visory.quickblox.util.loadUsersByPagedRequestBuilder
import com.taruc.visory.quickblox.util.signInUser
import com.taruc.visory.quickblox.util.signUp
import com.taruc.visory.quickblox.utils.*
import com.taruc.visory.ui.GetHelpActivity
import com.taruc.visory.utils.*
import kotlinx.android.synthetic.main.fragment_blind_home.*
import java.util.*
import kotlin.collections.ArrayList


const val ERROR_LOGIN_ALREADY_TAKEN_HTTP_STATUS = 422
const val EXTRA_LOGIN_RESULT_CODE = 1002
const val EXTRA_LOGIN_RESULT = "login_result"
const val EXTRA_LOGIN_ERROR_MESSAGE = "login_error_message"

class BlindHomeFragment : Fragment(), View.OnClickListener {
    lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var user: QBUser
    private var uid: String = ""
    private var fullName: String = ""
    private lateinit var volunteerUsers: ArrayList<QBUser>
    private var i = -1
    private lateinit var con: ViewGroup
    private var loadingDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        con = container!!

        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_blind_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Home"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val loggedUserPrefs = LoggedUser(this.requireActivity().baseContext)
        uid = loggedUserPrefs.getUserID()
        fullName = loggedUserPrefs.getUserName()

        val user = createQBUser()
        signUpNewUser(user)
        startLoginService()

        button_blind_detect_object.setOnClickListener(this)
        button_blind_help.setOnClickListener(this)
        button_blind_make_call.setOnClickListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    it.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            val user = createQBUser()
                            signUpNewUser(user)
                            Log.d("InternetConnection", "Internet is available")
                        }

                        override fun onLost(network: Network?) {
                            Log.d("InternetConnection", "Internet is not available")
                        }
                    })
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_settings -> {
                val settingsFragment = SettingsFragment()
                requireFragmentManager().beginTransaction()
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("OnAttach", "BlindHomeFragment Attached")
        showProgress()
    }

    override fun onStart() {
        super.onStart()

        if (checkIsLoggedInChat()) {
            if (!::volunteerUsers.isInitialized){
                volunteerUsers = ArrayList()
            }
        } else {
            startLoginService()
        }

        Log.d("onStart", "BlindHomeFragment starting!")
    }

    override fun onResume() {
        super.onResume()

        val isIncomingCall = false
        if (isCallServiceRunning(CallService::class.java)) {
            CallActivity.start(this.requireActivity().applicationContext, isIncomingCall)
        }
        clearAppNotifications()

        if (!::volunteerUsers.isInitialized){
            volunteerUsers = ArrayList()
            loadUsers()
            loadVolunteers()
        }

        Handler().postDelayed({
            if (!Helper[IS_CURRENTLY_CALLING, false]){
                hideProgress()
            }
        }, 5000)

        Log.d("OnResume", "BlindHomeFragment resuming!")
    }

    private fun showProgress(){
        hideProgress()
        loadingDialog = LoadingDialog.showLoadingDialog(this.requireContext())
    }

    private fun hideProgress(){
        loadingDialog?.let { if(it.isShowing)it.cancel() }
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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button_blind_detect_object -> {
                activity?.let {
                    val intent = Intent(it, TextDetectionActivity::class.java)
                    it.startActivity(intent)
                }
            }

            R.id.button_blind_help -> {
                val intent = Intent(context, GetHelpActivity::class.java)
                startActivity(intent)
            }

            R.id.button_blind_make_call -> {
                //calls last person who used the app
                val callHistory = CallHistory(this.requireContext())
                callHistory.clear()

                if (isInternetAvailable(requireContext())) {
                    loadUsers()
                    loadVolunteers()

                    Helper.delete(HANG_UP)
                    Helper.delete(CONNECTED_TO_USER)
                    Helper.delete(STOP_CALLING)
                    Helper.delete(IS_CURRENTLY_CALLING)
                    if (checkIsLoggedInChat()) {
                        i = 0
                        makeCall()
                    }
                } else {
                    makeErrorSnackbar(view, getString(R.string.active_internet_connection_call))
                }
            }
        }
    }

    private fun makeCall() {
        val timer = Timer()
        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    Log.d("CallTimer", "Timer is running!")

                    val hangUp = Helper[HANG_UP, false]
                    val connectedToUser = Helper[CONNECTED_TO_USER, false]

                    if (connectedToUser || hangUp || i > 5) {
                        Log.d("CallTimer", "Timer has stopped!")
                        Helper.save(IS_CURRENTLY_CALLING, false)
                        requireActivity().runOnUiThread{
                            hideProgress()
                        }
                        timer.cancel()
                    }

                    if (!connectedToUser && !hangUp && i <= 5) {
                        Log.d("CallTimer", "Timer is making a call!")
                        Helper.save(IS_CURRENTLY_CALLING, true)
                        requireActivity().runOnUiThread {
                            showProgress()
                            startCall(true, i++)
                        }
                    }

                    if (i > 5){
                        requireActivity().runOnUiThread {
                            //TODO: Display screen to inform to call later.
                            shortToast("Please try calling again later.")
                        }
                    }
                }
            },
            0, 13000
        )
    }

    private fun startCall(isVideoCall: Boolean, callerIndex: Int) {
        val opponentsList = ArrayList<Int>()
        opponentsList.add(volunteerUsers[callerIndex].id)

        val conferenceType = if (isVideoCall) {
            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
        } else {
            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO
        }

        try {
            val qbRtcClient = QBRTCClient.getInstance(this.requireActivity().applicationContext)
            val newQbRtcSession =
                qbRtcClient.createNewSessionWithOpponents(opponentsList, conferenceType)

            WebRtcSessionManager.setCurrentSession(newQbRtcSession)
            sendPushMessage(opponentsList, fullName)

            CallActivity.start(this.requireActivity().applicationContext, false)
        } catch (e: java.lang.Exception) {
            Log.d("CallError", e.message.toString())
        }
    }

    private fun loadVolunteers() {
        //TODO: Make an array to store volunteers that did not respond, and remove them from the calling list

        try {
            //So we don't keep adding same users again and again
            volunteerUsers.clear()

            val usersFromDb = QbUsersDbManager.allUsers
            val dbSize = usersFromDb.size
            val loggedUser = LoggedUser(requireContext())
            val userLanguage = loggedUser.getUserLanguage()

            for (i in 0 until dbSize) {
                if (usersFromDb[i].tags.contains("volunteer") && usersFromDb[i].tags.contains(
                        userLanguage
                    )) {
                    volunteerUsers.add(usersFromDb[i])
                }
            }

            if(volunteerUsers.size < 5){
                for (i in 0 until dbSize) {
                    if (usersFromDb[i].tags.contains("volunteer") && !usersFromDb[i].tags.contains(
                            userLanguage
                        )) {
                        if(usersFromDb[i].tags.contains(userLanguage)){
                            volunteerUsers.add(usersFromDb[i])
                        }
                    }
                }
            }

            for(i in 0 until volunteerUsers.size){
                Log.d(
                    "VolunteerList",
                    volunteerUsers[i].login + " and email: " + volunteerUsers[i].fullName
                )
            }

        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUsers() {
        val rules = ArrayList<GenericQueryRule>()
        rules.add(GenericQueryRule("order", "desc date updated_at"))
        val requestBuilder = QBPagedRequestBuilder()
        requestBuilder.rules = rules
        requestBuilder.perPage = 100

        loadUsersByPagedRequestBuilder(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(result: ArrayList<QBUser>, params: Bundle) {
                QbUsersDbManager.saveAllUsers(result, true)
            }

            override fun onError(responseException: QBResponseException) {}
        }, requestBuilder)
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
            LoginService.start(this.requireActivity().applicationContext, Helper.getQbUser())
        }
    }

    private fun createQBUser(): QBUser {
        val qbUser = QBUser()
        val tags = StringifyArrayList<String>()
        val loggedUser = LoggedUser(requireContext())

        qbUser.login = uid
        qbUser.fullName = fullName
        qbUser.password = DEFAULT_USER_PASSWORD

        tags.add("blind")
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
                    Log.d("QBUser", "Successfully created QB User")
                } else {
                    Log.d("QBUser", "Cannot create QB User")
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
        val pendingIntent = requireActivity().createPendingResult(
            EXTRA_LOGIN_RESULT_CODE,
            intent,
            0
        )
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
                Log.d("QBUser", "Sign in success")
            }

            override fun onError(responseException: QBResponseException) {
                Log.d("QBUser", "Sign in error")
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
}
