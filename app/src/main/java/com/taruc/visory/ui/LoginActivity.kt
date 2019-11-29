package com.taruc.visory.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.taruc.visory.R
import com.taruc.visory.UserType
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private var userType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        //userType = intent.getIntExtra("userType", 0)
        val userTypePref = UserType(this)
        userType = userTypePref.getUserType()

        setTitle(R.string.label_login)

        try{
            val mypref = UserType(this)
            Toast.makeText(applicationContext, userType.toString(), Toast.LENGTH_LONG).show()
        }catch (ex: Exception){
            Toast.makeText(applicationContext, ex.toString(), Toast.LENGTH_LONG).show()
        }

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
