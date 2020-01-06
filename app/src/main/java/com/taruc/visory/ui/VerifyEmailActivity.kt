package com.taruc.visory.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.taruc.visory.R
import com.taruc.visory.utils.makeErrorSnackbar
import com.taruc.visory.utils.makeSuccessSnackbar
import com.taruc.visory.utils.makeWarningSnackbar
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
                        //Toast.makeText(applicationContext,  "Email sent.", Toast.LENGTH_SHORT).show()
                        makeSuccessSnackbar(it, "Email is sent.")
                    }
                    else{
                        //Toast.makeText(applicationContext,  "Failed to send email because email is already sent.", Toast.LENGTH_SHORT).show()
                        makeErrorSnackbar(it, "Failed to send email because email is already sent. Check your spam folder.")
                    }
                }
        }

        confirm_email_button_submit.setOnClickListener{
            //Reload user state to check if email is verified
            user.reload()

            // wait 2 seconds before checking if the email is verified.
            Handler().postDelayed({
                try {
                    if(auth.currentUser != null){
                        if(user.isEmailVerified){
                            //Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                            makeSuccessSnackbar(it, "Email successfully verified.")
                            val intent = Intent(applicationContext, WelcomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            //Toast.makeText(applicationContext, "Please verify your email", Toast.LENGTH_SHORT).show()
                            makeWarningSnackbar(it, "Please verify your email.")
                        }
                    }else{
                        Toast.makeText(applicationContext, "Please login.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {}
            }, 2000)
        }
    }
}
