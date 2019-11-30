package com.taruc.visory.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.taruc.visory.R
import kotlinx.android.synthetic.main.activity_verify_email.*

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)

        setTitle(R.string.label_verify_email)

        auth = FirebaseAuth.getInstance()
        val user: FirebaseUser = auth.currentUser!!

        resend_email_button_submit.setOnClickListener{
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext,  "Email sent.", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(applicationContext,  "Failed to send email because email is already sent.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        confirm_email_button_submit.setOnClickListener{
            //Toast.makeText(applicationContext, user.uid, Toast.LENGTH_SHORT).show()
            user.reload()
            if(auth.currentUser != null){
                if(user.isEmailVerified){
                    Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                    val intent = Intent(applicationContext, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(applicationContext, "Please verify your email", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(applicationContext, "Please login.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
