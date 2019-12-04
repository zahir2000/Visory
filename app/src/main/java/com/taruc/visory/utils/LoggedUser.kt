package com.taruc.visory.utils

import android.content.Context

class LoggedUser(context: Context){
    val SHARED_PREF = "sharedPrefs"
    val USER_TYPE = "userType"
    val USER_NAME = "userName"
    val USER_EMAIL = "userEmail"
    val USER_JOINDATE = "userJoinDate"
    val preference = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    fun getUserType(): Int{
        return preference.getInt(USER_TYPE, 0)
    }

    fun setUserType(userType: Int){
        val editor = preference.edit()
        editor.putInt(USER_TYPE, userType)
        editor.apply()
    }

    fun getUserName(): String{
        return preference.getString(USER_NAME, "")!!
    }

    fun setUserName(userName: String){
        val editor = preference.edit()
        editor.putString(USER_NAME, userName)
        editor.apply()
    }

    fun getUserEmail(): String{
        return preference.getString(USER_EMAIL, "")!!
    }

    fun setUserEmail(userEmail: String){
        val editor = preference.edit()
        editor.putString(USER_EMAIL, userEmail)
        editor.apply()
    }

    fun getUserJoinDate(): String{
        return preference.getString(USER_JOINDATE, "")!!
    }

    fun setUserJoinDate(userJoinDate: String){
        val editor = preference.edit()
        editor.putString(USER_JOINDATE, userJoinDate)
        editor.apply()
    }

    fun setUserData(userName: String, userEmail: String, userJoinDate: String, userType: Int){
        val editor = preference.edit()
        editor.putString(USER_NAME, userName)
        editor.putString(USER_EMAIL, userEmail)
        editor.putString(USER_JOINDATE, userJoinDate)
        editor.putInt(USER_TYPE, userType)
        editor.apply()
    }
}