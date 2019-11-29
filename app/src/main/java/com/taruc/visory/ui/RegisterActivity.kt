package com.taruc.visory.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.taruc.visory.R

class RegisterActivity : AppCompatActivity() {

    private var userType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        userType = intent.getIntExtra("userType", 0)

        setTitle(R.string.label_register)

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
