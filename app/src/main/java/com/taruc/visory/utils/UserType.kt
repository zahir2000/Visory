package com.taruc.visory.utils

import android.content.Context
import android.content.SharedPreferences

class UserType(context: Context){
    private val SHARED_PREF = "sharedPrefs"
    private val USER_TYPE = "userType"
    val preference: SharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    fun getUserType(): Int{
        return preference.getInt(USER_TYPE, 0)
    }

    fun setUserType(userType: Int){
        val editor = preference.edit()
        editor.putInt(USER_TYPE, userType)
        editor.apply()
    }
}