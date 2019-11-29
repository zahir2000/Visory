package com.taruc.visory.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.taruc.visory.R

class LoginActivity : AppCompatActivity() {

    private var userType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        userType = intent.getIntExtra("userType", 0)

        setTitle(R.string.label_login)

        when(userType){
            1 -> {
                //
            }
            2 -> {
                //
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        //return to previous activity
        onBackPressed()
        return true
    }
}
