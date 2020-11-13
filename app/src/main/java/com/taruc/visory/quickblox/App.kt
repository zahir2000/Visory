package com.taruc.visory.quickblox

import android.app.Application
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.quickblox.auth.session.QBSession
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.QBSessionParameters
import com.quickblox.auth.session.QBSettings
import com.quickblox.messages.services.QBPushManager
import com.taruc.visory.R
import com.taruc.visory.quickblox.db.DbHelper
import com.taruc.visory.utils.shortToast

//User default credentials
const val DEFAULT_USER_PASSWORD = "11234566"

//Quickblox credentials
private const val APPLICATION_ID = "79529"
private const val AUTH_KEY = "eZkp7HVAPVBTOUp"
private const val AUTH_SECRET = "MTL3J4kTQK6LzsW"
private const val ACCOUNT_KEY = "i1y6gNw-XJHFWgWQ4V2f"

class App: Application(){
    private lateinit var dbHelper: DbHelper
    private val TAG = App::class.java.simpleName

    companion object {
        private lateinit var instance: App

        @Synchronized
        fun getInstance(): App = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        dbHelper = DbHelper(this)
        checkCredentials()
        initCredentials()
        initQBSessionManager()
        initPushManager()
    }

    private fun checkCredentials() {
        if (APPLICATION_ID.isEmpty() || AUTH_KEY.isEmpty() || AUTH_SECRET.isEmpty() || ACCOUNT_KEY.isEmpty()) {
            throw AssertionError(getString(R.string.error_qb_credentials_empty))
        }
    }

    private fun initCredentials() {
        QBSettings.getInstance().init(applicationContext, APPLICATION_ID, AUTH_KEY, AUTH_SECRET)
        QBSettings.getInstance().accountKey = ACCOUNT_KEY
    }

    private fun initQBSessionManager() {
        QBSessionManager.getInstance().addListener(object : QBSessionManager.QBSessionListener {
            override fun onSessionCreated(qbSession: QBSession) {
                Log.d(TAG, "Session Created")
            }

            override fun onSessionUpdated(qbSessionParameters: QBSessionParameters) {
                Log.d(TAG, "Session Updated")
            }

            override fun onSessionDeleted() {
                Log.d(TAG, "Session Deleted")
            }

            override fun onSessionRestored(qbSession: QBSession) {
                Log.d(TAG, "Session Restored")
            }

            override fun onSessionExpired() {
                Log.d(TAG, "Session Expired")
            }

            override fun onProviderSessionExpired(provider: String) {
                Log.d(TAG, "Session Expired for provider: $provider")
            }
        })
    }

    private fun initPushManager() {
        QBPushManager.getInstance().addListener(object : QBPushManager.QBSubscribeListener {
            override fun onSubscriptionCreated() {
                shortToast("Subscription Created")
                Log.d(TAG, "SubscriptionCreated")
            }

            override fun onSubscriptionError(e: Exception, resultCode: Int) {
                Log.d(TAG, "SubscriptionError" + e.localizedMessage)
                if (resultCode >= 0) {
                    val error = GoogleApiAvailability.getInstance().getErrorString(resultCode)
                    Log.d(TAG, "SubscriptionError playServicesAbility: $error")
                }
                shortToast(e.localizedMessage)
            }

            override fun onSubscriptionDeleted(success: Boolean) {

            }
        })
    }

    @Synchronized
    fun getDbHelper(): DbHelper {
        return dbHelper
    }
}