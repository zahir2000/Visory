package com.taruc.visory.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.taruc.visory.BlindHomeActivity
import com.taruc.visory.R
import com.taruc.visory.VolunteerHomeActivity
import com.taruc.visory.utils.LoggedUserType
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setTitle(R.string.label_welcome)

        val loggedUserType = LoggedUserType(this)

        button_welcome_submit.setOnClickListener{
            if(loggedUserType.getUserType() == 1){
                val intent = Intent(applicationContext, VolunteerHomeActivity::class.java)
                startActivity(intent)
            }
            else{
                val intent = Intent(applicationContext, BlindHomeActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

        // TODO: based on userType from db user role, open respective home screen
    }
}
