package com.taruc.visory.blind

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
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
import com.taruc.visory.quickblox.DEFAULT_USER_PASSWORD
import com.taruc.visory.quickblox.activities.CallActivity
import com.taruc.visory.quickblox.db.QbUsersDbManager
import com.taruc.visory.quickblox.services.LoginService
import com.taruc.visory.quickblox.util.loadUsersByPagedRequestBuilder
import com.taruc.visory.quickblox.util.signInUser
import com.taruc.visory.quickblox.util.signUp
import com.taruc.visory.quickblox.utils.Helper
import com.taruc.visory.quickblox.utils.WebRtcSessionManager
import com.taruc.visory.quickblox.utils.sendPushMessage
import com.taruc.visory.utils.LoggedUser
import kotlinx.android.synthetic.main.fragment_blind_home.*


const val ERROR_LOGIN_ALREADY_TAKEN_HTTP_STATUS = 422
const val EXTRA_LOGIN_RESULT_CODE = 1002
const val EXTRA_LOGIN_RESULT = "login_result"
const val EXTRA_LOGIN_ERROR_MESSAGE = "login_error_message"

class BlindHomeFragment : Fragment(), View.OnClickListener {
    lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var user: QBUser
    private lateinit var currentUser: QBUser
    private var uid: String = ""
    private var fullName: String = ""
    private lateinit var volunteerUsers: ArrayList<QBUser>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_blind_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Home"

        val loggedUserPrefs = LoggedUser(this.activity!!.baseContext)
        uid = loggedUserPrefs.getUserID()
        fullName = loggedUserPrefs.getUserName()

        volunteerUsers = ArrayList()

        loadUsers()
        loadVolunteers()

        val user = createQBUser()
        signUpNewUser(user)

        currentUser = Helper.getQbUser()

        //Toast.makeText(context, volunteerUsers[0].fullName.toString(), Toast.LENGTH_SHORT).show()

        button_blind_detect_object.setOnClickListener(this)
        button_blind_help.setOnClickListener(this)
        button_blind_make_call.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.button_blind_detect_object -> {
                /*
                auth.signOut()
                activity?.onBackPressed()
                activity?.let{
                    val intent = Intent(it, LandingActivity::class.java)
                    it.startActivity(intent)
                }*/
            }

            R.id.button_blind_help -> {

            }

            R.id.button_blind_make_call -> {
                startCall(true)
            }
        }
    }

    private fun startCall(isVideoCall: Boolean) {
        val usersCount = volunteerUsers.size

        //TODO: loop for each volunteer until a call is found

        val opponentsList = ArrayList<Int>()
        opponentsList.add(volunteerUsers[0].id)

        val conferenceType = if (isVideoCall) {
            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
        } else {
            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO
        }

        val qbRtcClient = QBRTCClient.getInstance(this.activity!!.applicationContext)
        val newQbRtcSession = qbRtcClient.createNewSessionWithOpponents(opponentsList, conferenceType)

        WebRtcSessionManager.setCurrentSession(newQbRtcSession)
        sendPushMessage(opponentsList, currentUser.fullName)

        CallActivity.start(this.activity!!.applicationContext, false)
    }

    private fun loadVolunteers(){
        try {
            val usersFromDb = QbUsersDbManager.allUsers

            for(i in 0 until usersFromDb.size - 1){
                if(usersFromDb[i].tags.contains("volunteer")){
                    volunteerUsers.add(usersFromDb[i])
                }
            }
        }catch (e: Exception){
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

    private fun createQBUser(): QBUser {
        val qbUser = QBUser()
        val tags = StringifyArrayList<String>()

        qbUser.login = uid
        qbUser.fullName = fullName
        qbUser.password = DEFAULT_USER_PASSWORD

        tags.add("blind")
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
        val pendingIntent = activity!!.createPendingResult(EXTRA_LOGIN_RESULT_CODE, intent, 0)
        LoginService.start(activity!!.baseContext, qbUser, pendingIntent)
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
                Toast.makeText(context, "Finished creating user in server", Toast.LENGTH_LONG).show()
            }

            override fun onError(responseException: QBResponseException?) {
                Toast.makeText(context, "Error creating user in server", Toast.LENGTH_LONG).show()
            }
        })
    }
}
