package com.taruc.visory

import android.content.Context

class UserType(context: Context){
    val SHARED_PREF = "sharedPrefs"
    val USER_TYPE = "userType"
    val preference = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    fun getUserType(): Int{
        return preference.getInt(USER_TYPE, 0)
    }

    fun setUserType(userType: Int){
        val editor = preference.edit()
        editor.putInt(USER_TYPE, userType)
        editor.apply()
    }
}