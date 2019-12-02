package com.taruc.visory.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.BlindHomeActivity
import com.taruc.visory.R
import com.taruc.visory.VolunteerHomeActivity
import com.taruc.visory.utils.LoggedUserType
import com.taruc.visory.utils.User
import com.taruc.visory.utils.UserType
import kotlinx.android.synthetic.main.activity_landing.*


class LandingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val userTypePref = UserType(this)

        auth = FirebaseAuth.getInstance()

        /*if(auth.currentUser != null){
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val rootRef = FirebaseDatabase.getInstance().getReference("users")
            val uidRef = rootRef.child(String.format("%s/role", uid))
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    role = dataSnapshot.getValue()!!
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            uidRef.addListenerForSingleValueEvent(valueEventListener)
        }*/

        buttonVolunteer.setOnClickListener{
            val intent = Intent(this,LandingActionsActivity::class.java)
            userTypePref.setUserType(1)
            //intent.putExtra("userType",1)
            startActivity(intent)
        }

        buttonBlind.setOnClickListener{
            val intent = Intent(this,LandingActionsActivity::class.java)
            //intent.putExtra("userType",2)
            userTypePref.setUserType(2)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    private fun updateUI() {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        val loggedUserTypePref = LoggedUserType(this)

        if(loggedUserTypePref.getUserType() == 0){
            auth.signOut()
        }

        if(auth.currentUser != null){
            if(auth.currentUser!!.isEmailVerified || isLoggedIn){
                if(loggedUserTypePref.getUserType() == 1){
                    val intent = Intent(this, VolunteerHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                else {
                    val intent = Intent(this, BlindHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }

            }else{
                val intent = Intent(this, VerifyEmailActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
            finish()
        }
    }
}
