package com.taruc.visory.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.taruc.visory.R
import com.taruc.visory.UserType
import kotlinx.android.synthetic.main.activity_landing_actions.*

class LandingActionsActivity : AppCompatActivity(), View.OnClickListener {

    private var userType: Int = 0
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_actions)

        auth = FirebaseAuth.getInstance()

        //implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        //get which button was clicked = userType
        //userType = intent.getIntExtra("userType", 0)

        val userTypePref = UserType(this)
        userType = userTypePref.getUserType()

        //change content based on user
        // 1 = Volunteer
        // 2 = Blind & Visually Impaired
        when (userType) {
            1 -> {
                imageLandingActions.setImageResource(R.drawable.ic_volunteer_intro)
                headerLandingActions.setText(R.string.welcome_volunteer_header)
                textLandingActions.setText(R.string.welcome_volunteer_text)
                setTitle(R.string.label_volunteer)
            }
            2 -> {
                imageLandingActions.setImageResource(R.drawable.ic_blind_intro)
                headerLandingActions.setText(R.string.welcome_blind_header)
                textLandingActions.setText(R.string.welcome_blind_text)
                setTitle(R.string.label_bvi)
            }
            else -> {
                //end activity
                finish()
            }
        }

        button_login.setOnClickListener(this)
        button_register_submit.setOnClickListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        //return to previous activity
        onBackPressed()
        return true
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.button_login -> {
                val intent = Intent(this, LoginActivity::class.java)
                //intent.putExtra("userType", userType)
                startActivity(intent)
            }

            R.id.button_register_submit -> {
                val intent = Intent(this, RegisterActivity::class.java)
                //intent.putExtra("userType", userType)
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if(auth.currentUser != null){
            if(auth.currentUser!!.isEmailVerified){
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
