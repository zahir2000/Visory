package com.taruc.visory.utils

import android.content.Context
import java.lang.Exception

class LoggedUser(context: Context){
    private val SHARED_PREF = "sharedPrefs"
    private val USER_ID = "userID"
    private val USER_TYPE = "userType"
    private val USER_NAME = "userName"
    private val USER_EMAIL = "userEmail"
    private val USER_CONTACT = "userContactNo"
    private val USER_JOINDATE = "userJoinDate"
    private val USER_LANGUAGE = "userLanguage"
    private val PROVIDER = "provider"
    private val AVATAR_URL = "avatarUrl"
    private val preference = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    fun getUserID(): String{
        return preference.getString(USER_ID, "null")!!
    }

    fun setUserType(userID: String){
        val editor = preference.edit()
        editor.putString(USER_ID, userID)
        editor.apply()
    }

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

    fun getUserContact(): String{
        return preference.getString(USER_CONTACT, "")!!
    }

    fun setUserContact(userContact: String){
        val editor = preference.edit()
        editor.putString(USER_CONTACT, userContact)
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

    fun getUserLanguage(): String{
        return preference.getString(USER_LANGUAGE, "")!!
    }

    fun setUserLanguage(userLanguage: String){
        val editor = preference.edit()
        editor.putString(USER_LANGUAGE, userLanguage)
        editor.apply()
    }

    fun getProvider(): String{
        return preference.getString(PROVIDER, "")!!
    }

    fun setProvider(provider: String){
        val editor = preference.edit()
        editor.putString(PROVIDER, provider)
        editor.apply()
    }

    fun getAvatarUrl(): String{
        return preference.getString(AVATAR_URL, "")!!
    }

    fun setAvatarUrl(avatarUrl: String){
        val editor = preference.edit()
        editor.putString(AVATAR_URL, avatarUrl)
        editor.apply()
    }

    fun getUserData(): String{
        return try{
            "${getUserID()}\n${getUserName()}\n${getUserEmail()}\n${getUserJoinDate()}\n${getUserType()}"
        } catch (e: Exception){
            "Not all data is entered"
        }
    }

    fun setUserData(userID: String, userName: String, userEmail: String, userContact:String, userJoinDate: String, userType: Int, userLanguage:String, provider: String){
        val editor = preference.edit()
        editor.putString(USER_ID, userID)
        editor.putString(USER_NAME, userName)
        editor.putString(USER_EMAIL, userEmail)
        editor.putString(USER_CONTACT, userContact)
        editor.putString(USER_JOINDATE, userJoinDate)
        editor.putInt(USER_TYPE, userType)
        editor.putString(USER_LANGUAGE, userLanguage)
        editor.putString(PROVIDER, provider)
        editor.apply()
    }

    fun setUserData(userId: String, user: User, provider: String){
        val editor = preference.edit()
        editor.putString(USER_ID, userId)
        editor.putString(USER_NAME, "${user.fname} ${user.lname}")
        editor.putString(USER_EMAIL, user.email)
        editor.putString(USER_CONTACT, user.contactNo)
        editor.putString(USER_JOINDATE, user.datejoined)
        editor.putInt(USER_TYPE, user.role)
        editor.putString(USER_LANGUAGE, user.language)
        editor.putString(PROVIDER, provider)
        editor.apply()
    }
}