package com.taruc.visory.quickblox.utils

import com.taruc.visory.quickblox.App
import com.taruc.visory.quickblox.activities.CallActivity
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacksImpl

object WebRtcSessionManager : QBRTCClientSessionCallbacksImpl() {
    private var currentSession: QBRTCSession? = null

    fun getCurrentSession(): QBRTCSession? {
        return currentSession
    }

    fun setCurrentSession(qbCurrentSession: QBRTCSession?) {
        currentSession = qbCurrentSession
    }

    override fun onReceiveNewSession(session: QBRTCSession) {
        if (currentSession == null) {
            setCurrentSession(session)
            CallActivity.start(App.getInstance(), true)
        }
    }

    override fun onSessionClosed(session: QBRTCSession?) {
        if (session == getCurrentSession()) {
            setCurrentSession(null)
        }
    }
}