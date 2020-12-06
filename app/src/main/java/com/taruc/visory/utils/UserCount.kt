package com.taruc.visory.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class UserCount(context: Context){
    private val SHARED_PREF = "sharedUserCountPrefs"
    private val VOL_COUNT = "volCount"
    private val BVI_COUNT = "bviCount"
    private val UPD_DATE = "lastUpdateDate"
    val preference: SharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    fun getVolCount(): Int{
        return preference.getInt(VOL_COUNT, 0)
    }

    fun setVolCount(volCount: Int){
        val editor = preference.edit()
        editor.putInt(VOL_COUNT, volCount)
        editor.apply()
    }

    fun getBviCount(): Int{
        return preference.getInt(BVI_COUNT, 0)
    }

    fun setBviCount(bviCount: Int){
        val editor = preference.edit()
        editor.putInt(BVI_COUNT, bviCount)
        editor.apply()
    }

    fun setUserCount(volCount: Int, bviCount: Int){
        val editor = preference.edit()
        editor.putInt(VOL_COUNT, volCount)
        editor.putInt(BVI_COUNT, bviCount)
        editor.apply()
    }

    fun getLastUpdateDate(): String {
        return preference.getString(UPD_DATE, "null")!!
    }

    fun setLastUpdateDate(){
        val editor = preference.edit()
        val date = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US);
        val resultDate = Date(date)
        editor.putString(UPD_DATE, dateFormat.format(resultDate))
        editor.apply()
    }
}

fun loadUsers(context: Context){
    val userCount = UserCount(context)
    val date = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US);
    val resultDate = Date(date)

    if(userCount.getLastUpdateDate() != dateFormat.format(resultDate)){
        val rootRef = FirebaseDatabase.getInstance().reference
        val userRef = rootRef.child("users")
        userCount.setUserCount(0, 0)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds in dataSnapshot.children){
                    val role = ds.child("role").value
                    //Log.d("Role", role.toString())
                    if (role != null){
                        val roleVal = role.toString().toInt()
                        if(roleVal == 1){
                            userCount.setVolCount(userCount.getVolCount() + 1)
                        }else if(roleVal == 2){
                            userCount.setBviCount(userCount.getBviCount() + 1)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        userRef.addListenerForSingleValueEvent(valueEventListener)
    }
}