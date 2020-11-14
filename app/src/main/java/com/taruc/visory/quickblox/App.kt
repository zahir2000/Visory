package com.taruc.visory.quickblox

import android.app.Application
import com.quickblox.auth.session.QBSettings
import com.taruc.visory.R
import com.taruc.visory.quickblox.db.DbHelper

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

    @Synchronized
    fun getDbHelper(): DbHelper {
        return dbHelper
    }
}