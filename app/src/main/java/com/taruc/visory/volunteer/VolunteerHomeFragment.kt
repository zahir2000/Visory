package com.taruc.visory.volunteer

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.core.request.GenericQueryRule
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.taruc.visory.R
import com.taruc.visory.blind.ERROR_LOGIN_ALREADY_TAKEN_HTTP_STATUS
import com.taruc.visory.blind.EXTRA_LOGIN_ERROR_MESSAGE
import com.taruc.visory.blind.EXTRA_LOGIN_RESULT
import com.taruc.visory.blind.EXTRA_LOGIN_RESULT_CODE
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
import com.taruc.visory.utils.LoggedUser
import com.taruc.visory.utils.UserCount
import com.taruc.visory.utils.shortToast
import kotlinx.android.synthetic.main.fragment_volunteer_home.*
import kotlinx.android.synthetic.main.profile_card.*
import kotlinx.android.synthetic.main.user_stats.*
import java.lang.Exception


class VolunteerHomeFragment : Fragment(), View.OnClickListener {

    lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var user: QBUser
    private var uid: String = ""
    private var fullName: String = ""
    private lateinit var con: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        con = container!!

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_volunteer_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Home"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val loggedUserPrefs = LoggedUser(this.activity!!.baseContext)
        uid = loggedUserPrefs.getUserID()
        fullName = loggedUserPrefs.getUserName()

        button_tutorial.setOnClickListener(this)

        val user = createQBUser()
        signUpNewUser(user)

        startLoginService()

        updateUI()
    }

    override fun onResume() {
        super.onResume()

        val isIncomingCall = Helper[EXTRA_IS_INCOMING_CALL, false]
        if (isCallServiceRunning(CallService::class.java)) {
            CallActivity.start(this.activity!!.applicationContext, isIncomingCall)
        }
        clearAppNotifications()

        loadUsers()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home_settings -> {
                val settingsFragment = SettingsFragment()
                fragmentManager!!.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
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
            //shortToast("Retrying to login")
            return false
        }
        return true
    }

    private fun startLoginService() {
        if (Helper.hasQbUser()) {
            LoginService.start(this.activity!!.applicationContext, Helper.getQbUser())
        }
    }

    private fun createQBUser(): QBUser {
        val qbUser = QBUser()
        val tags = StringifyArrayList<String>()
        val loggedUser = LoggedUser(requireContext())

        qbUser.login = uid
        qbUser.fullName = fullName
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
                    Toast.makeText(context, e.errors.toString(), Toast.LENGTH_LONG).show()
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
                //Toast.makeText(context, "Finished creating user in server", Toast.LENGTH_LONG).show()
            }

            override fun onError(responseException: QBResponseException?) {
                Toast.makeText(context, "Error creating user in server", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUI() {
        val loggedUserTypePref = LoggedUser(this.activity!!.baseContext)
        profile_joindate.text = getString(R.string.member_since, loggedUserTypePref.getUserJoinDate())
        profile_name.text = loggedUserTypePref.getUserName()
        profile_language.text = loggedUserTypePref.getUserLanguage()

        val userCount = UserCount(this.context!!)

        Handler().postDelayed({
            if(userCount.getBviCount() != 0 && userCount.getVolCount() != 0){
                try{
                    user_stats_view.visibility = View.VISIBLE
                    textViewBlindCount.text = userCount.getBviCount().toString()
                    textViewVolunteerCount.text = userCount.getVolCount().toString()
                }catch (e: Exception){}
            }else{
                try{
                    user_stats_view.visibility = View.GONE
                }catch (e: Exception){}
            }
        }, 2000)
        //profile_language.text = loggedUserTypePref.getLanguage()
    }

    private fun isCallServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = activity!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = manager.getRunningServices(Integer.MAX_VALUE)
        for (service in services) {
            if (CallService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun clearAppNotifications() {
        val notificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_tutorial -> {
                activity?.let{
                    val intent = Intent (it, TutorialActivity::class.java)
                    it.startActivity(intent)
                }
            }
        }
    }
}
