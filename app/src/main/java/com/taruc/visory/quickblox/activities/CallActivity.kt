package com.taruc.visory.quickblox.activities

import android.content.*
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallbackImpl
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import com.taruc.visory.R
import com.taruc.visory.blind.CallEndActivity
import com.taruc.visory.quickblox.db.QbUsersDbManager
import com.taruc.visory.quickblox.fragments.*
import com.taruc.visory.quickblox.services.CallService
import com.taruc.visory.quickblox.services.LoginService
import com.taruc.visory.quickblox.util.loadUsersByIds
import com.taruc.visory.quickblox.utils.EXTRA_IS_INCOMING_CALL
import com.taruc.visory.quickblox.utils.Helper
import com.taruc.visory.quickblox.utils.setSettingsStrategy
import org.jivesoftware.smack.AbstractConnectionListener
import com.taruc.visory.quickblox.utils.*
import com.taruc.visory.utils.*
import org.jivesoftware.smack.ConnectionListener
import org.webrtc.CameraVideoCapturer
import java.util.HashMap

private const val INCOME_CALL_FRAGMENT = "income_call_fragment"
private const val REQUEST_PERMISSION_SETTING = 545

class CallActivity : BaseActivity(), CallCallbackListener, QBRTCSessionStateCallback<QBRTCSession>,
    QBRTCClientSessionCallbacks, ConversationCallback {
    private val currentCallStateCallbackList = ArrayList<CurrentCallStateCallback>()
    private lateinit var showIncomingCallWindowTaskHandler: Handler
    private var connectionListener: ConnectionListenerImpl? = null
    private lateinit var callServiceConnection: ServiceConnection
    private lateinit var showIncomingCallWindowTask: Runnable
    private lateinit var sharedPref: SharedPreferences
    private var opponentsIdsList: List<Int>? = null
    private lateinit var callService: CallService
    private var callAccepted = false

    private var isInComingCall: Boolean = false
    private var isVideoCall: Boolean = false

    companion object {
        fun start(context: Context, isIncomingCall: Boolean) {
            val intent = Intent(context, CallActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            Helper.save(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            context.startActivity(intent)
            CallService.start(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
    }

    private fun initScreen() {
        callService.setCallTimerCallback(CallTimerCallback())
        isVideoCall = callService.isVideoCall()

        opponentsIdsList = callService.getOpponents()

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        initSettingsStrategy()
        addListeners()

        if (callService.isCallMode()) {
            checkPermission()
            addConversationFragment(isInComingCall)
        } else {
            if (intent != null && intent.extras != null) {
                isInComingCall = intent?.extras?.getBoolean(EXTRA_IS_INCOMING_CALL) ?: true
            } else {
                isInComingCall = Helper[EXTRA_IS_INCOMING_CALL, false]
            }

            if (!isInComingCall) {
                callService.playRingtone()
            }
            startSuitableFragment(isInComingCall)
        }
    }

    private fun addListeners() {
        addSessionEventsListener(this)
        addSessionStateListener(this)

        connectionListener = ConnectionListenerImpl()
        addConnectionListener(connectionListener)
    }

    private fun removeListeners() {
        removeSessionEventsListener(this)
        removeSessionStateListener(this)
        removeConnectionListener(connectionListener)

        callService.removeCallTimerCallback()
    }

    private fun bindCallService() {
        callServiceConnection = CallServiceConnection()
        Intent(this, CallService::class.java).also { intent ->
            bindService(intent, callServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == EXTRA_LOGIN_RESULT_CODE) {
            data?.let {
                val isLoginSuccess = it.getBooleanExtra(EXTRA_LOGIN_RESULT, false)
                if (isLoginSuccess) {
                    initScreen()
                } else {
                    finish()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startSuitableFragment(isInComingCall: Boolean) {
        val session = WebRtcSessionManager.getCurrentSession()
        if (session != null) {
            if (isInComingCall) {
                initIncomingCallTask()
                startLoadAbsentUsers()
                addIncomeCallFragment()
                checkPermission()
            } else {
                addConversationFragment(isInComingCall)
                intent.removeExtra(EXTRA_IS_INCOMING_CALL)
                Helper.save(EXTRA_IS_INCOMING_CALL, false)
            }
        } else {
            finish()
        }
    }

    private fun checkPermission() {
        val cam = Helper[PERMISSIONS[0], true]
        val mic = Helper[PERMISSIONS[1], true]

        if (isVideoCall && checkPermissions(PERMISSIONS)) {
            if (cam) {
                PermissionsActivity.startForResult(this, false, PERMISSIONS)
            } else {
                val rootView = window.decorView.findViewById<View>(android.R.id.content)
                showErrorSnackbar(rootView, getString(R.string.error_permission_video), R.string.dlg_allow,
                    View.OnClickListener { startPermissionSystemSettings() })
            }
        } else if (checkPermission(PERMISSIONS[1])) {
            if (mic) {
                PermissionsActivity.startForResult(this, true, PERMISSIONS)
            } else {
                val rootView = window.decorView.findViewById<View>(android.R.id.content)
                showErrorSnackbar(rootView, R.string.error_permission_audio, "Allow Permission", R.string.dlg_allow,
                    View.OnClickListener { startPermissionSystemSettings() })
            }
        }
    }

    private fun startPermissionSystemSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQUEST_PERMISSION_SETTING)
    }

    private fun startLoadAbsentUsers() {
        val usersFromDb = QbUsersDbManager.allUsers
        val allParticipantsOfCall = ArrayList<Int>()

        opponentsIdsList?.let {
            allParticipantsOfCall.addAll(it)
        }

        if (isInComingCall) {
            val callerId = callService.getCallerId()
            callerId?.let {
                allParticipantsOfCall.add(it)
            }
        }

        val idsNotLoadedUsers = ArrayList<Int>()

        for (userId in allParticipantsOfCall) {
            val user = QBUser(userId)
            user.fullName = userId.toString()
            if (!usersFromDb.contains(user)) {
                idsNotLoadedUsers.add(userId)
            }
        }

        if (idsNotLoadedUsers.isNotEmpty()) {
            loadUsersByIds(idsNotLoadedUsers, object : QBEntityCallbackImpl<ArrayList<QBUser>>() {
                override fun onSuccess(users: ArrayList<QBUser>, params: Bundle) {
                    QbUsersDbManager.saveAllUsers(users, false)
                    notifyCallStateListenersNeedUpdateOpponentsList(users)
                }
            })
        }
    }

    private fun initSettingsStrategy() {
        opponentsIdsList?.let {
            setSettingsStrategy(it, sharedPref, this)
        }
    }

    private fun initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = Handler(Looper.myLooper())
        showIncomingCallWindowTask = Runnable {
            if (callService.currentSessionExist()) {
                val currentSessionState = callService.getCurrentSessionState()
                if (BaseSession.QBRTCSessionState.QB_RTC_SESSION_NEW == currentSessionState) {
                    callService.rejectCurrentSession(HashMap())
                } else {
                    callService.stopRingtone()
                    hangUpCurrentSession()
                }
                longToast("Call was stopped by timer")
            }
        }
    }

    private fun hangUpCurrentSession() {
        callService.stopRingtone()
        if (!callService.hangUpCurrentSession(HashMap())) {
            finish()
        }
    }

    private fun startIncomeCallTimer(time: Long) {
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time)
    }

    private fun stopIncomeCallTimer() {
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask)
    }

    override fun onResume() {
        super.onResume()
        bindCallService()
    }

    override fun onPause() {
        super.onPause()
        unbindService(callServiceConnection)
        if (::callService.isInitialized) {
            removeListeners()
        }
    }

    override fun finish() {
        //Fix bug when user returns to call from service and the backstack doesn't have any screens
        //OpponentsActivity.start(this)
        CallService.stop(this)
        super.finish()
    }

    override fun onBackPressed() {
        // To prevent returning from Call Fragment
    }

    private fun addIncomeCallFragment() {
        if (callService.currentSessionExist()) {
            val fragment = IncomeCallFragment()
            if (supportFragmentManager.findFragmentByTag(INCOME_CALL_FRAGMENT) == null) {
                addFragment(supportFragmentManager, R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT)
            }
        }
    }

    private fun addConversationFragment(isIncomingCall: Boolean) {
        val baseConversationFragment: BaseConversationFragment = if (isVideoCall) {
            VideoConversationFragment()
        } else {
            AudioConversationFragment()
        }
        val conversationFragment = BaseConversationFragment.newInstance(baseConversationFragment, isIncomingCall)
        addFragment(supportFragmentManager, R.id.fragment_container, conversationFragment, conversationFragment.javaClass.simpleName)
    }

    private fun showNotificationPopUp(text: Int, show: Boolean) {
        runOnUiThread {
            val connectionView = View.inflate(this, R.layout.connection_popup, null) as LinearLayout
            if (show) {
                (connectionView.findViewById(R.id.notification) as TextView).setText(text)
                if (connectionView.parent == null) {
                    (this@CallActivity.findViewById<View>(R.id.fragment_container) as ViewGroup).addView(connectionView)
                }
            } else {
                (this@CallActivity.findViewById<View>(R.id.fragment_container) as ViewGroup).removeView(connectionView)
            }
        }
    }

    ////////////////////////////// ConnectionListener //////////////////////////////

    private inner class ConnectionListenerImpl : AbstractConnectionListener() {
        override fun connectionClosedOnError(e: Exception?) {
            showNotificationPopUp(R.string.lost_connection, true)
        }

        override fun reconnectionSuccessful() {
            showNotificationPopUp(R.string.lost_connection, false)
        }
    }

    ////////////////////////////// QBRTCSessionStateCallbackListener ///////////////////////////

    override fun onDisconnectedFromUser(session: QBRTCSession?, userId: Int?) {

    }

    override fun onConnectedToUser(session: QBRTCSession?, userId: Int?) {
        notifyCallStateListenersCallStarted()
        if (isInComingCall) {
            stopIncomeCallTimer()
        }
    }

    override fun onConnectionClosedForUser(session: QBRTCSession?, userId: Int?) {

    }

    override fun onStateChanged(session: QBRTCSession?, sessiontState: BaseSession.QBRTCSessionState?) {

    }

    ////////////////////////////// QBRTCClientSessionCallbacks //////////////////////////////

    override fun onUserNotAnswer(session: QBRTCSession?, userId: Int?) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone()
        }
    }

    override fun onSessionStartClose(session: QBRTCSession?) {
        if (callService.isCurrentSession(session)) {
            callService.removeSessionStateListener(this)
            notifyCallStateListenersCallStopped()
        }
    }

    override fun onReceiveHangUpFromUser(session: QBRTCSession?, userId: Int?, map: MutableMap<String, String>?) {
        if (callService.isCurrentSession(session)) {
            if (userId == session?.callerID) {
                hangUpCurrentSession()
            }
            val participant = QbUsersDbManager.getUserById(userId)
            val participantName = if (participant != null) participant.fullName else userId.toString()
            //shortToast("User " + participantName + " " + getString(R.string.hang_up_call) + " conversation")
            Log.i("CallActivity", "User " + participantName + " " + getString(R.string.hang_up_call) + " conversation")
        }
    }

    override fun onCallAcceptByUser(session: QBRTCSession?, userId: Int?, map: MutableMap<String, String>?) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone()
        }
    }

    override fun onReceiveNewSession(session: QBRTCSession?) {

    }

    override fun onUserNoActions(session: QBRTCSession?, userId: Int?) {
        startIncomeCallTimer(0)
    }

    override fun onSessionClosed(session: QBRTCSession?) {
        if (callService.isCurrentSession(session)) {
            callService.stopForeground(true)
            if(Helper[CONNECTED_TO_USER, false]){
                val intent = Intent(this, CallEndActivity::class.java)
                startActivity(intent)
            } else if(callAccepted){
                val intent = Intent(this, com.taruc.visory.volunteer.CallEndActivity::class.java)
                startActivity(intent)
            }
            finish()
        }
    }

    override fun onCallRejectByUser(session: QBRTCSession?, userId: Int?, map: MutableMap<String, String>?) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone()
        }
    }

    ////////////////////////////// IncomeCallFragmentCallbackListener ////////////////////////////

    override fun onAcceptCurrentSession() {
        if (callService.currentSessionExist()) {
            addConversationFragment(true)
            callAccepted = true
        }
    }

    override fun onRejectCurrentSession() {
        callService.rejectCurrentSession(HashMap())
    }

    ////////////////////////////// ConversationFragmentCallback ////////////////////////////

    override fun addConnectionListener(connectionCallback: ConnectionListener?) {
        callService.addConnectionListener(connectionCallback)
    }

    override fun removeConnectionListener(connectionCallback: ConnectionListener?) {
        callService.removeConnectionListener(connectionCallback)
    }

    override fun addSessionStateListener(clientConnectionCallbacks: QBRTCSessionStateCallback<*>?) {
        callService.addSessionStateListener(clientConnectionCallbacks)
    }

    override fun addSessionEventsListener(eventsCallback: QBRTCSessionEventsCallback?) {
        callService.addSessionEventsListener(eventsCallback)
    }

    override fun onSetAudioEnabled(isAudioEnabled: Boolean) {
        callService.setAudioEnabled(isAudioEnabled)
    }

    override fun onHangUpCurrentSession() {
        hangUpCurrentSession()
    }

    override fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler) {
        callService.switchCamera(cameraSwitchHandler)
    }

    override fun onSetVideoEnabled(isNeedEnableCam: Boolean) {
        callService.setVideoEnabled(isNeedEnableCam)
    }

    override fun onSwitchAudio() {
        callService.switchAudio()
    }

    override fun removeSessionStateListener(clientConnectionCallbacks: QBRTCSessionStateCallback<*>?) {
        callService.removeSessionStateListener(clientConnectionCallbacks)
    }

    override fun removeSessionEventsListener(eventsCallback: QBRTCSessionEventsCallback?) {
        callService.removeSessionEventsListener(eventsCallback)
    }

    override fun addCurrentCallStateListener(currentCallStateCallback: CurrentCallStateCallback?) {
        currentCallStateCallback?.let {
            currentCallStateCallbackList.add(it)
        }
    }

    override fun removeCurrentCallStateListener(currentCallStateCallback: CurrentCallStateCallback?) {
        currentCallStateCallbackList.remove(currentCallStateCallback)
    }

    override fun addOnChangeAudioDeviceListener(onChangeDynamicCallback: OnChangeAudioDevice?) {
    }

    override fun removeOnChangeAudioDeviceListener(onChangeDynamicCallback: OnChangeAudioDevice?) {
    }

    override fun acceptCall(userInfo: Map<String, String>) {
        callService.acceptCall(userInfo)
    }

    override fun startCall(userInfo: Map<String, String>) {
        callService.startCall(userInfo)
    }

    override fun currentSessionExist(): Boolean {
        return callService.currentSessionExist()
    }

    override fun getOpponents(): List<Int>? {
        return callService.getOpponents()
    }

    override fun getCallerId(): Int? {
        return callService.getCallerId()
    }

    override fun addVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?) {
        callService.addVideoTrackListener(callback)
    }

    override fun removeVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?) {
        callService.removeVideoTrackListener(callback)
    }

    override fun getCurrentSessionState(): BaseSession.QBRTCSessionState? {
        return callService.getCurrentSessionState()
    }

    override fun getPeerChannel(userId: Int): QBRTCTypes.QBRTCConnectionState? {
        return callService.getPeerChannel(userId)
    }

    override fun isMediaStreamManagerExist(): Boolean {
        return callService.isMediaStreamManagerExist()
    }

    override fun isCallState(): Boolean {
        return callService.isCallMode()
    }

    override fun getVideoTrackMap(): MutableMap<Int, QBRTCVideoTrack> {
        return callService.getVideoTrackMap()
    }

    override fun getVideoTrack(userId: Int): QBRTCVideoTrack? {
        return callService.getVideoTrack(userId)
    }

    private fun notifyCallStateListenersCallStarted() {
        for (callback in currentCallStateCallbackList) {
            callback.onCallStarted()
        }
    }

    private fun notifyCallStateListenersCallStopped() {
        for (callback in currentCallStateCallbackList) {
            callback.onCallStopped()
        }
    }

    private fun notifyCallStateListenersNeedUpdateOpponentsList(newUsers: ArrayList<QBUser>) {
        for (callback in currentCallStateCallbackList) {
            callback.onOpponentsListUpdated(newUsers)
        }
    }

    private fun notifyCallStateListenersCallTime(callTime: String) {
        for (callback in currentCallStateCallbackList) {
            callback.onCallTimeUpdate(callTime)
        }
    }

    private inner class CallServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CallService.CallServiceBinder
            callService = binder.getService()
            if (callService.currentSessionExist()) {
                //we have already currentSession == null, so it's no reason to do further initialization
                if (QBChatService.getInstance().isLoggedIn) {
                    initScreen()
                } else {
                    login()
                }
            } else {
                finish()
            }
        }

        private fun login() {
            val qbUser = Helper.getQbUser()
            val tempIntent = Intent(this@CallActivity, LoginService::class.java)
            val pendingIntent = createPendingResult(EXTRA_LOGIN_RESULT_CODE, tempIntent, 0)
            LoginService.start(this@CallActivity, qbUser, pendingIntent)
        }
    }

    private inner class CallTimerCallback : CallService.CallTimerListener {
        override fun onCallTimeUpdate(time: String) {
            runOnUiThread {
                notifyCallStateListenersCallTime(time)
            }
        }
    }

    interface OnChangeAudioDevice {
        fun audioDeviceChanged(newAudioDevice: AppRTCAudioManager.AudioDevice)
    }

    interface CurrentCallStateCallback {
        fun onCallStarted()

        fun onCallStopped()

        fun onOpponentsListUpdated(newUsers: ArrayList<QBUser>)

        fun onCallTimeUpdate(time: String)
    }
}