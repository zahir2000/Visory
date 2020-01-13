package com.taruc.visory.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.taruc.visory.R
import com.taruc.visory.utils.*
import kotlinx.android.synthetic.main.activity_forgot_pass.*

class ForgotPassActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        //implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        button_reset_pass.setOnClickListener{
            if(isInternetAvailable(applicationContext)){
                resetEmail(it)
            }
            else{
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }
    }

    fun resetEmail(view: View){
        val email = email_text.text.toString()

        if(TextUtils.isEmpty(email)){
            hideKeyboard(view)
            makeWarningSnackbar(view, "Please enter your email")
            return
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            hideKeyboard(view)
            makeWarningSnackbar(view, "Please enter a valid email")
            return
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener(this){task ->
            if(task.isSuccessful){
                hideKeyboard(view)
                makeSuccessSnackbar(view, "Reset email has been sent.")

                Handler().postDelayed({
                    try {
                        finish()
                    } catch (e: Exception) {}
                }, 3000)
            }
            else{
                hideKeyboard(view)
                makeErrorSnackbar(view, "Email could not be sent. Possibly a wrong email.")
                email_text.requestFocus()
                Handler().postDelayed({
                    showKeyboard()
                }, 3000)
            }
        }
    }

    private fun hideKeyboard(view: View){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeyboard(){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(email_text, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onSupportNavigateUp(): Boolean {
        //return to previous activity
        onBackPressed()
        return true
    }
}
