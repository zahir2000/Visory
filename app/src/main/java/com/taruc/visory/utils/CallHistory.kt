package com.taruc.visory.utils

import android.content.Context

class CallHistory(context: Context){
    private val SHARED_PREF = "callHistoryPrefs"
    private val CALLER_ID = "callerID"
    private val CALLEE_ID = "calleeID"
    private val CALL_DATE = "callDate"
    private val CALL_TIME = "callTime"

    private val preference = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    fun getCallerId(): String{
        return preference.getString(CALLER_ID, "")!!
    }

    fun setCallerId(callerId: String){
        val editor = preference.edit()
        editor.putString(CALLER_ID, callerId)
        editor.apply()
    }

    fun getCalleeId(): String{
        return preference.getString(CALLEE_ID, "")!!
    }

    fun setCalleeId(calleeId: String){
        val editor = preference.edit()
        editor.putString(CALLEE_ID, calleeId)
        editor.apply()
    }

    fun getCallDate(): String{
        return preference.getString(CALL_DATE, "")!!
    }

    fun setCallDate(callDate: String){
        val editor = preference.edit()
        editor.putString(CALL_DATE, callDate)
        editor.apply()
    }

    fun getCallTime(): String{
        return preference.getString(CALL_TIME, "")!!
    }

    fun setCallTime(callTime: String){
        val editor = preference.edit()
        editor.putString(CALL_TIME, callTime)
        editor.apply()
    }

    fun clear(){
        val editor = preference.edit()
        editor.putString(CALLER_ID, "")
        editor.putString(CALLEE_ID, "")
        editor.putString(CALL_DATE, "")
        editor.putString(CALL_TIME, "")
        editor.apply()
    }
}