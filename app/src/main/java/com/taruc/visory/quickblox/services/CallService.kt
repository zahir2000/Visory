package com.taruc.visory.quickblox.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.quickblox.chat.QBChatService
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.callbacks.*
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import com.taruc.visory.R
import com.taruc.visory.quickblox.activities.CallActivity
import com.taruc.visory.quickblox.db.QbUsersDbManager
import com.taruc.visory.quickblox.util.NetworkConnectionChecker
import com.taruc.visory.quickblox.utils.*
import com.taruc.visory.utils.shortToast
import org.jivesoftware.smack.AbstractConnectionListener
import org.jivesoftware.smack.ConnectionListener
import org.webrtc.CameraVideoCapturer
import com.taruc.visory.quickblox.fragments.*
import java.util.*
import kotlin.collections.HashMap

class CallService : Service(){
    private val callServiceBinder: CallServiceBinder = CallServiceBinder()
    private lateinit var ringtonePlayer: RingtonePlayer
    private lateinit var networkConnectionListener: NetworkConnectionListener
    private lateinit var networkConnectionChecker: NetworkConnectionChecker
    private lateinit var sessionEventsListener: SessionEventsListener
    private lateinit var connectionListener: ConnectionListenerImpl
    private lateinit var sessionStateListener: SessionStateListener
    private lateinit var signalingListener: QBRTCSignalingListener
    private lateinit var videoTrackListener: VideoTrackListener
    private lateinit var appRTCAudioManager: AppRTCAudioManager
    private lateinit var rtcClient: QBRTCClient
    private var videoTrackMap: MutableMap<Int, QBRTCVideoTrack> = java.util.HashMap()
    private var callTimerListener: CallTimerListener? = null
    private var currentSession: QBRTCSession? = null
    private val callTimerTask: CallTimerTask = CallTimerTask()
    private var callTime: Long? = null
    private val callTimer = Timer()
    private var expirationReconnectionTime: Long = 0
    private var isCallState: Boolean = false

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CallService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        currentSession = WebRtcSessionManager.getCurrentSession()
        clearButtonsState()
        initNetworkChecker()
        initRTCClient()
        initListeners()
        initAudioManager()
        ringtonePlayer = RingtonePlayer(this, R.raw.beep)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isIncomingCall = Helper[EXTRA_IS_INCOMING_CALL, false]
        if(isIncomingCall){
            val notification = initNotification()
            startForeground(SERVICE_ID, notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkConnectionChecker.unregisterListener(networkConnectionListener)
        removeConnectionListener(connectionListener)
        removeVideoTrackRenders()

        releaseCurrentSession()
        releaseAudioManager()

        stopCallTimer()
        clearButtonsState()
        clearCallState()
        stopForeground(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun clearButtonsState() {
        Helper.delete(MIC_ENABLED)
        Helper.delete(SPEAKER_ENABLED)
        Helper.delete(CAMERA_ENABLED)
        Helper.delete(IS_CURRENT_CAMERA_FRONT)
    }

    private fun clearCallState() {
        Helper.delete(EXTRA_IS_INCOMING_CALL)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return callServiceBinder
    }

    private fun initNetworkChecker() {
        networkConnectionChecker = NetworkConnectionChecker(application)
        networkConnectionListener = NetworkConnectionListener()
        networkConnectionChecker.registerListener(networkConnectionListener)
    }

    private inner class NetworkConnectionListener : NetworkConnectionChecker.OnConnectivityChangedListener {
        override fun connectivityChanged(availableNow: Boolean) {
            //Toast.makeText(applicationContext, "Internet connection " + if (availableNow) "available" else " unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initRTCClient() {
        rtcClient = QBRTCClient.getInstance(this)
        rtcClient.setCameraErrorHandler(CameraEventsListener())

        QBRTCConfig.setMaxOpponentsCount(2)
        QBRTCConfig.setDebugEnabled(true)

        configRTCTimers(this)

        rtcClient.prepareToProcessCalls()
    }

    private fun initListeners() {
        sessionStateListener = SessionStateListener()
        addSessionStateListener(sessionStateListener)

        signalingListener = QBRTCSignalingListener()
        addSignalingListener(signalingListener)

        videoTrackListener = VideoTrackListener()
        addVideoTrackListener(videoTrackListener)

        connectionListener = ConnectionListenerImpl()
        addConnectionListener(connectionListener)

        sessionEventsListener = SessionEventsListener()
        addSessionEventsListener(sessionEventsListener)
    }

    private fun initAudioManager() {
        appRTCAudioManager = AppRTCAudioManager.create(this)

        appRTCAudioManager.setOnWiredHeadsetStateListener { plugged, hasMicrophone ->
            shortToast("Headset " + if (plugged) "plugged" else "unplugged")
        }

        appRTCAudioManager.setBluetoothAudioDeviceStateListener { connected ->
            shortToast("Bluetooth " + if (connected) "connected" else "disconnected")
        }

        appRTCAudioManager.start { selectedAudioDevice, availableAudioDevices ->
            Log.i("CallService", "Audio device switched to  $selectedAudioDevice")
        }

        if (currentSessionExist() && currentSession!!.conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO) {
            appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE)
        }
    }

    fun currentSessionExist(): Boolean {
        return currentSession != null
    }

    private fun releaseCurrentSession() {
        removeSessionStateListener(sessionStateListener)
        removeSignalingListener(signalingListener)
        removeSessionEventsListener(sessionEventsListener)
        removeVideoTrackListener(videoTrackListener)
        currentSession = null
    }

    fun playRingtone() {
        ringtonePlayer.play(true)
    }

    fun stopRingtone() {
        ringtonePlayer.stop()
    }

    private fun startCallTimer() {
        if (callTime == null) {
            callTime = 1000
        }
        if (!callTimerTask.isRunning) {
            callTimer.scheduleAtFixedRate(callTimerTask, 0, 1000)
        }
    }

    private fun stopCallTimer() {
        callTimerListener = null

        callTimer.cancel()
        callTimer.purge()
    }

    private fun getCallTime(): String {
        var time = ""
        callTime?.let {
            val format = String.format("%%0%dd", 2)
            val elapsedTime = it / 1000
            val seconds = String.format(format, elapsedTime % 60)
            val minutes = String.format(format, elapsedTime % 3600 / 60)
            val hours = String.format(format, elapsedTime / 3600)
            time = "$minutes:$seconds"
            if (!TextUtils.isEmpty(hours) && hours != "00") {
                time = "$hours:$minutes:$seconds"
            }
        }
        return time
    }

    fun getVideoTrackMap(): MutableMap<Int, QBRTCVideoTrack> {
        return videoTrackMap
    }

    private fun addVideoTrack(userId: Int, videoTrack: QBRTCVideoTrack) {
        videoTrackMap[userId] = videoTrack
    }

    fun getVideoTrack(userId: Int): QBRTCVideoTrack? {
        return videoTrackMap[userId]
    }

    private fun removeVideoTrack(userId: Int) {
        videoTrackMap.remove(userId)
    }

    private fun removeVideoTrackRenders() {
        if (videoTrackMap.isNotEmpty()) {
            val entryIterator = videoTrackMap.entries.iterator()
            while (entryIterator.hasNext()) {
                val entry = entryIterator.next()
                val userId = entry.key
                val videoTrack = entry.value
                val qbUser = QBChatService.getInstance().user
                val remoteVideoTrack = userId != qbUser.id
                if (remoteVideoTrack) {
                    videoTrack.renderer?.let {
                        videoTrack.removeRenderer(it)
                    }
                }
            }
        }
    }

    fun hangUpCurrentSession(userInfo: Map<String, String>): Boolean {
        stopRingtone()
        var result = false
        currentSession?.let {
            it.hangUp(userInfo)
            result = true
        }
        return result
    }

    private fun releaseAudioManager() {
        appRTCAudioManager.stop()
    }

    fun acceptCall(userInfo: Map<String, String>) {
        currentSession?.acceptCall(userInfo)
    }

    fun startCall(userInfo: Map<String, String>) {
        currentSession?.startCall(userInfo)
    }

    fun rejectCurrentSession(userInfo: Map<String, String>) {
        currentSession?.rejectCall(userInfo)
    }

    fun setAudioEnabled(enabled: Boolean) {
        currentSession?.mediaStreamManager?.localAudioTrack?.setEnabled(enabled)
    }

    fun getCallerId(): Int? {
        return currentSession?.callerID
    }

    fun getOpponents(): List<Int>? {
        return currentSession?.opponents
    }

    fun isVideoCall(): Boolean {
        return QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO == currentSession?.conferenceType
    }

    fun setVideoEnabled(videoEnabled: Boolean) {
        currentSession?.mediaStreamManager?.localVideoTrack?.setEnabled(videoEnabled)
    }

    fun getCurrentSessionState(): BaseSession.QBRTCSessionState? {
        return currentSession?.state
    }

    fun isMediaStreamManagerExist(): Boolean {
        return currentSession?.mediaStreamManager != null
    }

    fun getPeerChannel(userId: Int): QBRTCTypes.QBRTCConnectionState? {
        return currentSession?.getPeerChannel(userId)?.state
    }

    fun isCurrentSession(session: QBRTCSession?): Boolean {
        var isCurrentSession = false
        session?.let {
            isCurrentSession = currentSession?.sessionID == it.sessionID
        }
        return isCurrentSession
    }

    fun switchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler) {
        val videoCapturer = currentSession!!.mediaStreamManager!!.videoCapturer as QBRTCCameraVideoCapturer
        videoCapturer.switchCamera(cameraSwitchHandler)
    }

    fun switchAudio() {
        if (appRTCAudioManager.selectedAudioDevice != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
            appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
        } else {
            when {
                appRTCAudioManager.audioDevices.contains(AppRTCAudioManager.AudioDevice.BLUETOOTH) -> {
                    appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.BLUETOOTH)
                }
                appRTCAudioManager.audioDevices.contains(AppRTCAudioManager.AudioDevice.WIRED_HEADSET) -> {
                    appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)
                }
                else -> {
                    appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE)
                }
            }
        }
    }

    fun isCallMode(): Boolean {
        return isCallState
    }

    fun setCallTimerCallback(callback: CallTimerListener) {
        callTimerListener = callback
    }

    fun removeCallTimerCallback() {
        callTimerListener = null
    }

    //Listeners
    fun addConnectionListener(connectionListener: ConnectionListener?) {
        QBChatService.getInstance().addConnectionListener(connectionListener)
    }

    fun removeConnectionListener(connectionListener: ConnectionListener?) {
        QBChatService.getInstance().removeConnectionListener(connectionListener)
    }

    fun addSessionStateListener(callback: QBRTCSessionStateCallback<*>?) {
        currentSession?.addSessionCallbacksListener(callback)
    }

    fun removeSessionStateListener(callback: QBRTCSessionStateCallback<*>?) {
        currentSession?.removeSessionCallbacksListener(callback)
    }

    fun addVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?) {
        currentSession?.addVideoTrackCallbacksListener(callback)
    }

    fun removeVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?) {
        currentSession?.removeVideoTrackCallbacksListener(callback)
    }

    fun addSignalingListener(callback: QBRTCSignalingCallback?) {
        currentSession?.addSignalingCallback(callback)
    }

    fun removeSignalingListener(callback: QBRTCSignalingCallback?) {
        currentSession?.removeSignalingCallback(callback)
    }

    fun addSessionEventsListener(callback: QBRTCSessionEventsCallback?) {
        rtcClient.addSessionCallbacksListener(callback)
    }

    fun removeSessionEventsListener(callback: QBRTCSessionEventsCallback?) {
        rtcClient.removeSessionsCallbacksListener(callback)
    }

    //Notification
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        channel.lightColor = getColor(R.color.colorPrimary)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    private fun initNotification(): Notification {
        val notifyIntent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val isIncomingCall = Helper[EXTRA_IS_INCOMING_CALL, false]

        var notificationTitle = getString(R.string.notification_title)
        var notificationText = getString(R.string.notification_text_calling)

        val callTime = getCallTime()
        if (!TextUtils.isEmpty(callTime)) {
            notificationText = getString(R.string.notification_text, callTime)
        }

        if (!isIncomingCall) {
            notificationTitle = getString(R.string.notification_text_bvi)
        }

        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(notificationTitle)
        bigTextStyle.bigText(notificationText)

        val channelId: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
        } else {
            getString(R.string.app_name)
        }

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setStyle(bigTextStyle)
        builder.setContentTitle(notificationTitle)
        builder.setContentText(notificationText)
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.drawable.ic_notification_icon)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.color = getColor(R.color.colorPrimary)
        } else {
            builder.color = ContextCompat.getColor(applicationContext, R.color.colorPrimary);
        }

        val bitmapIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_notification_icon)
        builder.setLargeIcon(bitmapIcon)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.priority = NotificationManager.IMPORTANCE_DEFAULT
        } else {
            builder.priority = Notification.PRIORITY_DEFAULT
        }
        builder.apply {
            setContentIntent(notifyPendingIntent)
        }
        return builder.build()
    }

    private inner class CameraEventsListener : CameraVideoCapturer.CameraEventsHandler {
        override fun onCameraError(s: String) {
            //shortToast("Camera error: $s")
        }

        override fun onCameraDisconnected() {
            //shortToast("Camera Disconnected: ")
        }

        override fun onCameraFreezed(s: String) {
            //shortToast("Camera freezed: $s")
            hangUpCurrentSession(HashMap())
        }

        override fun onCameraOpening(s: String) {
            //shortToast("Camera opening: $s")
        }

        override fun onFirstFrameAvailable() {
            //shortToast("onFirstFrameAvailable: ")

        }

        override fun onCameraClosed() {
            //shortToast("Camera closed: ")
        }
    }

    private inner class SessionStateListener : QBRTCSessionStateCallback<QBRTCSession> {
        override fun onDisconnectedFromUser(session: QBRTCSession?, userId: Int?) {

        }

        override fun onConnectedToUser(session: QBRTCSession?, userId: Int?) {
            stopRingtone()
            isCallState = true
            startCallTimer()
        }

        override fun onConnectionClosedForUser(session: QBRTCSession?, userID: Int?) {
            //shortToast("The user: " + userID + "has left the call")
            Log.i("CallService", "The user: " + userID + "has left the call")
            userID?.let {
                removeVideoTrack(it)
            }
        }

        override fun onStateChanged(session: QBRTCSession?, sessionState: BaseSession.QBRTCSessionState?) {

        }
    }

    private inner class VideoTrackListener : QBRTCClientVideoTracksCallbacks<QBRTCSession> {
        override fun onLocalVideoTrackReceive(session: QBRTCSession?, videoTrack: QBRTCVideoTrack?) {
            videoTrack?.let {
                val userId = QBChatService.getInstance().user.id
                removeVideoTrack(userId)
                addVideoTrack(userId, it)
            }
        }

        override fun onRemoteVideoTrackReceive(session: QBRTCSession?, videoTrack: QBRTCVideoTrack?, userId: Int?) {
            if (videoTrack != null && userId != null) {
                addVideoTrack(userId, videoTrack)
            }
        }
    }

    private inner class QBRTCSignalingListener : QBRTCSignalingCallback {
        override fun onSuccessSendingPacket(p0: QBSignalingSpec.QBSignalCMD?, p1: Int?) {

        }

        override fun onErrorSendingPacket(p0: QBSignalingSpec.QBSignalCMD?, p1: Int?, p2: QBRTCSignalException?) {
            shortToast(getString(R.string.check_connection))
        }
    }

    private inner class ConnectionListenerImpl : AbstractConnectionListener() {
        override fun connectionClosedOnError(e: Exception?) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val reconnectHangUpTimeMillis = getPreferenceInt(sharedPref, applicationContext,
                R.string.pref_disconnect_time_interval_key,
                R.string.pref_disconnect_time_interval_default_value) * 1000
            expirationReconnectionTime = System.currentTimeMillis() + reconnectHangUpTimeMillis
        }

        override fun reconnectionSuccessful() {
        }

        override fun reconnectingIn(seconds: Int) {
            if (!isCallState && expirationReconnectionTime < System.currentTimeMillis()) {
                hangUpCurrentSession(HashMap())
            }
        }
    }

    private inner class SessionEventsListener : QBRTCClientSessionCallbacks {
        override fun onUserNotAnswer(session: QBRTCSession?, userId: Int?) {
            stopRingtone()
            Helper.save(STOP_CALLING, false)
        }

        override fun onSessionStartClose(session: QBRTCSession?) {
            if (session == WebRtcSessionManager.getCurrentSession()) {
                stop(applicationContext)
            }
        }

        override fun onReceiveHangUpFromUser(session: QBRTCSession?, userID: Int?, p2: MutableMap<String, String>?) {
            stopRingtone()
            Helper.save(STOP_CALLING, false)
            if (session == WebRtcSessionManager.getCurrentSession()) {
                if (userID == session?.callerID) {
                    currentSession?.let {
                        it.hangUp(HashMap<String, String>())
                        stop(this@CallService)
                    }
                }
            } else {
                stop(this@CallService)
            }

            val participant = QbUsersDbManager.getUserById(userID)
            val participantName = if (participant != null) participant.fullName else userID.toString()

            //shortToast("User " + participantName + " " + getString(R.string.hang_up_call) + " conversation")
            Log.i("CallService", "User " + participantName + " " + getString(R.string.hang_up_call) + " conversation")
        }

        override fun onCallAcceptByUser(session: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
            stopRingtone()
            Helper.save(STOP_CALLING, true)
            Helper.save(CONNECTED_TO_USER, true)
            if (session != WebRtcSessionManager.getCurrentSession()) {
                return
            }
        }

        override fun onReceiveNewSession(session: QBRTCSession?) {
            WebRtcSessionManager.getCurrentSession()?.let {
                session?.rejectCall(null)
            }
        }

        override fun onUserNoActions(p0: QBRTCSession?, p1: Int?) {

        }

        override fun onSessionClosed(session: QBRTCSession?) {
            stopRingtone()
            if (session == currentSession) {
                stop(this@CallService)
            }
        }

        override fun onCallRejectByUser(session: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
            stopRingtone()
            Helper.save(STOP_CALLING, false)
            if (session != WebRtcSessionManager.getCurrentSession()) {
                return
            }
        }
    }

    private inner class CallTimerTask : TimerTask() {
        var isRunning: Boolean = false

        override fun run() {
            isRunning = true

            callTime = callTime?.plus(1000L)

            val isIncomingCall = Helper[EXTRA_IS_INCOMING_CALL, false]
            if (isIncomingCall) {
                val notification = initNotification()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(SERVICE_ID, notification)
            }

            callTimerListener?.let {
                val callTime = getCallTime()
                if (!TextUtils.isEmpty(callTime)) {
                    it.onCallTimeUpdate(callTime)
                }
            }
        }
    }

    inner class CallServiceBinder : Binder() {
        fun getService(): CallService = this@CallService
    }

    fun getPreferenceInt(sharedPref: SharedPreferences, context: Context, strResKey: Int, strResDefValue: Int): Int {
        return sharedPref.getInt(context.getString(strResKey), Integer.valueOf(context.getString(strResDefValue)))
    }

    interface CallTimerListener {
        fun onCallTimeUpdate(time: String)
    }
}