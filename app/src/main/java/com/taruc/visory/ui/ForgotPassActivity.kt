package com.taruc.visory.ui

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.taruc.visory.R
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
            resetEmail(it)
        }
    }

    fun makeSnackbar(view: View, text: String){
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null)
        snackbar.setActionTextColor(Color.WHITE)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundResource(R.color.colorPrimary)
        snackbar.show()
    }

    fun resetEmail(view: View){
        val email = email_text.text.toString()

        if(TextUtils.isEmpty(email)){
            hideKeyboard(view)
            makeSnackbar(view, "Please enter your email")
            return
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            hideKeyboard(view)
            makeSnackbar(view, "Please enter a valid email")
            return
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener(this){task ->
            if(task.isSuccessful){
                hideKeyboard(view)
                makeSnackbar(view, "Reset email has been sent.")

                Handler().postDelayed({
                    try {
                        finish()
                    } catch (e: Exception) {}
                }, 3000)
            }
            else{
                hideKeyboard(view)
                makeSnackbar(view, "Email could not be sent. Possibly a wrong email.")
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
