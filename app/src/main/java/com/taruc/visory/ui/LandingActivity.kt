package com.taruc.visory.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.taruc.visory.*
import kotlinx.android.synthetic.main.activity_landing.*

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val userTypePreferences = UserType(this)

        buttonVolunteer.setOnClickListener{
            val intent = Intent(this,LandingActionsActivity::class.java)
            userTypePreferences.setUserType(1)
            //intent.putExtra("userType",1)
            startActivity(intent)
        }

        buttonBlind.setOnClickListener{
            val intent = Intent(this,LandingActionsActivity::class.java)
            //intent.putExtra("userType",2)
            userTypePreferences.setUserType(2)
            startActivity(intent)
        }
    }
}
