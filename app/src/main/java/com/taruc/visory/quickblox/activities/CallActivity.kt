package com.taruc.visory.quickblox.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.taruc.visory.R
import com.taruc.visory.quickblox.fragments.CallCallbackListener
import com.taruc.visory.quickblox.fragments.ConversationCallback
import com.taruc.visory.quickblox.services.CallService
import com.taruc.visory.quickblox.utils.EXTRA_IS_INCOMING_CALL
import com.taruc.visory.quickblox.utils.Helper

class CallActivity : BaseActivity(), CallCallbackListener, QBRTCSessionStateCallback<QBRTCSession>,
    QBRTCClientSessionCallbacks, ConversationCallback {

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
}