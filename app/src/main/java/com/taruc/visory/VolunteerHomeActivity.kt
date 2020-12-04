package com.taruc.visory

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.taruc.visory.jalal.*
import com.taruc.visory.quickblox.activities.PermissionsActivity
import com.taruc.visory.quickblox.utils.*
import com.taruc.visory.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VolunteerHomeActivity : AppCompatActivity() {

    private val TAG = "VolunteerHomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_home)

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC).addOnFailureListener {
            Log.d(TAG, "TOPIC was unsuccessful")
        }
        FirebaseMessaging.getInstance().subscribeToTopic(CALL_TOPIC).addOnFailureListener {
            Log.d(TAG, "CALL_TOPIC was unsuccessful")
        }
        FirebaseMessaging.getInstance().subscribeToTopic(CALL_TOPIC_END).addOnFailureListener {
            Log.d(TAG, "CALL_TOPIC_END was unsuccessful")
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_volunteer_home, R.id.navigation_stories, R.id.navigation_donation
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        updateToken()

        if (Helper[CHECK_PERMISSIONS, true]) {
            PermissionsActivity.startForResult(this, false, PERMISSIONS)
        }

        if (intent.extras?.getString("BVI_CALL") != null){
            val callerId = intent.extras?.getString("BVI_CALL")
            Log.d("VolunteerHomeActivity", callerId.toString())

            val loggedUser = LoggedUser(this)

            FirebaseDatabase.getInstance().reference.child("Tokens").child(callerId.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userToken = snapshot.child("token").value.toString()

                        Log.d("VolunteerHomeActivity", userToken)

                        PushNotification(
                            NotificationData("Title", "Message", loggedUser.getUserID()),
                            userToken
                        ).also {
                            sendNotification(it)
                            intent.extras?.remove("BVI_CALL")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) { }

                })


        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)

            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.d(TAG, response.errorBody().toString())
            }
        } catch (e: java.lang.Exception){
            Log.e(TAG, e.toString())
        }
    }

    private fun updateToken(){
        val loggedUser = LoggedUser(this)
        FirebaseService.sharedPref = this.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }

        FirebaseDatabase.getInstance().getReference("Tokens").child(loggedUser.getUserID()).setValue(Token(FirebaseService.token!!))
    }

    inner class Token(val token:String){
        constructor():this(""){}
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStackImmediate()
        return super.onSupportNavigateUp()
    }
}
