package com.taruc.visory.blind

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.taruc.visory.admin.FeedbackDetailsActivity
import com.taruc.visory.admin.FeedbackFragment
import com.taruc.visory.fragments.SettingsFragment
import com.taruc.visory.mlkit.ObjectDetectorActivity
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
    lateinit var viewDialog: ViewDialog
    private lateinit var con: ViewGroup


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

        //Toast.makeText(context, volunteerUsers[0].fullName.toString(), Toast.LENGTH_SHORT).show()

        button_blind_detect_object.setOnClickListener(this)
        button_blind_help.setOnClickListener(this)
        button_blind_make_call.setOnClickListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

    override fun onStart() {
        super.onStart()

        if (checkIsLoggedInChat()) {
            volunteerUsers = ArrayList()
            loadUsers()
            loadVolunteers()
        } else {
            startLoginService()
        }
    }

    override fun onResume() {
        super.onResume()

        val isIncomingCall = Helper.get(EXTRA_IS_INCOMING_CALL, false)
        if (isCallServiceRunning(CallService::class.java)) {
            CallActivity.start(this.requireActivity().applicationContext, isIncomingCall)
        }
        clearAppNotifications()

        volunteerUsers = ArrayList()
        loadVolunteers()
        loadUsers()
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

                /*val feedbackFragment = FeedbackFragment()
                parentFragmentManager.beginTransaction()
                    .replace(con.id, feedbackFragment)
                    .addToBackStack(null)
                    .commit()*/

                activity?.let {
                    val intent = Intent(it, ObjectDetectorActivity::class.java)
                    //val intent = Intent(it, FeedbackDetailsActivity::class.java)
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
                    Helper.save(HANG_UP, false)
                    Helper.save(CONNECTED_TO_USER, false)
                    if (checkIsLoggedInChat()) {
                        i = -1
                        makeCall()
                    }
                } else {
                    makeErrorSnackbar(view, getString(R.string.active_internet_connection_call))
                }
            }
        }
    }

    private fun makeCall() {
        viewDialog = ViewDialog(requireContext())
        viewDialog.showDialogFor5Seconds()
        var callAccepted = Helper[STOP_CALLING, false]

        i += 1

        if (Helper[HANG_UP, false]) {
            return
        }

        startCall(true, i)

        if (Helper[HANG_UP, false]) {
            return
        }

        Handler().postDelayed({
            if (Helper[HANG_UP, false]) {
                return@postDelayed
            }
            callAccepted = Helper[STOP_CALLING, false]

            if (callAccepted) {
                return@postDelayed
            }

            viewDialog = ViewDialog(requireContext())
            viewDialog.showDialogFor5Seconds()
        }, 10000)

        if (callAccepted) {
            return
        }

        Handler().postDelayed({
            if (Helper[HANG_UP, false]) {
                return@postDelayed
            }

            callAccepted = Helper[STOP_CALLING, false]

            if (!callAccepted) {
                if (i < (volunteerUsers.size - 1) && !Helper[HANG_UP, false] && i < 5) {
                    makeCall()
                }
                else if ((i + 1) == volunteerUsers.size || (i + 1) == 5) {
                    //No response after calling everyone
                    shortToast("Please try calling again later")
                }
            } else {
                return@postDelayed
            }
        }, 13000)
    }

    private fun startCall(isVideoCall: Boolean, callerIndex: Int) {
        val usersCount = volunteerUsers.size

        if (usersCount == 0) {
            loadUsers()
            loadVolunteers()
        }

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
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun loadVolunteers() {
        try {
            //So we don't keep adding same users again and again
            volunteerUsers.clear()

            val usersFromDb = QbUsersDbManager.allUsers
            val dbSize = usersFromDb.size
            val loggedUser = LoggedUser(requireContext())
            val userLanguage = loggedUser.getUserLanguage()

            for (i in 0 until dbSize) {
                if (usersFromDb[i].tags.contains("volunteer") && usersFromDb[i].tags.contains(userLanguage)) {
                    volunteerUsers.add(usersFromDb[i])
                }
            }

            if(volunteerUsers.size < 5){
                for (i in 0 until dbSize) {
                    if (usersFromDb[i].tags.contains("volunteer")) {
                        if(usersFromDb[i].tags.contains(userLanguage)){
                            //volunteerUsers.add(usersFromDb[i])
                        }else{
                            volunteerUsers.add(usersFromDb[i])
                        }
                    }
                }
            }

            //for(i in 0 until volunteerUsers.size){
            //    shortToast(volunteerUsers[i].login)
            //}

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
            //shortToast("Retrying to login")
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
                } else {
                    Toast.makeText(context, "Cannot create QB User", Toast.LENGTH_LONG).show()
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
                Toast.makeText(context, "Sign in error", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUserOnServer(user: QBUser) {
        user.password = null
        QBUsers.updateUser(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(updUser: QBUser?, params: Bundle?) {
                //Toast.makeText(context, "Finished creating user in server", Toast.LENGTH_LONG).show()
            }

            override fun onError(responseException: QBResponseException?) {
                Toast.makeText(context, "Error creating user in server", Toast.LENGTH_LONG).show()
            }
        })
    }
}
