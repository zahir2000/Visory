package com.taruc.visory.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.r0adkll.slidr.Slidr
import com.taruc.visory.R
import com.taruc.visory.utils.UserType
import kotlinx.android.synthetic.main.activity_landing_actions.*

class LandingActionsActivity : AppCompatActivity(), View.OnClickListener {

    private var userType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_actions)
        Slidr.attach(this)

        //implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        userType = intent.getIntExtra("USER_TYPE", 0)

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
        when (view.id) {
            R.id.button_login -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("USER_TYPE", userType)
                startActivity(intent)
            }

            R.id.button_register_submit -> {
                val intent = Intent(this, RegisterActivity::class.java)
                intent.putExtra("USER_TYPE", userType)
                startActivity(intent)
            }
        }
    }
}
