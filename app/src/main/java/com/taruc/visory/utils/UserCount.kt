package com.taruc.visory.utils

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserCount(context: Context){
    val SHARED_PREF = "sharedUserCountPrefs"
    val VOL_COUNT = "volCount"
    val BVI_COUNT = "bviCount"
    val preference = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

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
}

fun loadUsers(context: Context){
    val userCount = UserCount(context)
    val rootRef = FirebaseDatabase.getInstance().reference
    val userRef = rootRef.child("users")
    userCount.setUserCount(0, 0)

    val valueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            for(ds in dataSnapshot.children){
                val role = ds.child("role").value
                val roleVal = role.toString().toInt()
                if(roleVal == 1){
                    userCount.setVolCount(userCount.getVolCount() + 1)
                }else if(roleVal == 2){
                    userCount.setBviCount(userCount.getBviCount() + 1)
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    }
    userRef.addListenerForSingleValueEvent(valueEventListener)
}