package com.taruc.visory.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.taruc.visory.BlindHomeActivity
import com.taruc.visory.R
import com.taruc.visory.VolunteerHomeActivity
import com.taruc.visory.quickblox.activities.PermissionsActivity
import com.taruc.visory.quickblox.utils.PERMISSIONS
import com.taruc.visory.utils.LoggedUser
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        startPermissionsActivity(false)

        setTitle(R.string.label_welcome)
        val user = LoggedUser(this)
        val name = user.getUserName()
        text_welcome_header.text = getString(R.string.welcome_header_name, name)
        text_welcome_body1.text = getString(R.string.welcome_body_text1)

        if(user.getUserType() == 1){
            text_welcome_body2.text = getString(R.string.welcome_body2_volunteer)
        }else{
            text_welcome_body2.text = getString(R.string.welcome_body2_blind)
        }


        val loggedUserType = LoggedUser(this)

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
    }

    private fun startPermissionsActivity(checkOnlyAudio: Boolean) {
        PermissionsActivity.startForResult(this, checkOnlyAudio, PERMISSIONS)
    }
}
