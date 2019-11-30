package com.taruc.visory.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.google.firebase.auth.FirebaseAuth
import com.taruc.visory.R
import com.taruc.visory.UserType
import kotlinx.android.synthetic.main.activity_landing.*


class LandingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val userTypePref = UserType(this)

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

        auth = FirebaseAuth.getInstance()
        updateUI()
    }

    private fun updateUI() {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired

        if(auth.currentUser != null){
            if(auth.currentUser!!.isEmailVerified || isLoggedIn){
                val intent = Intent(this, WelcomeActivity::class.java) // TODO: Change to Home activity
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(0, 0)
            }else{
                val intent = Intent(this, VerifyEmailActivity::class.java) // TODO: Change to Home activity
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
            finish()
        }
    }
}
