package com.taruc.visory.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.taruc.visory.R
import com.taruc.visory.utils.*
import kotlinx.android.synthetic.main.activity_change_password.*

class ChangePasswordActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var loggedUser: LoggedUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        supportActionBar?.title = "Edit Password"

        auth = FirebaseAuth.getInstance()
        loggedUser = LoggedUser(this)

        if(loggedUser.getProvider() == "fb"){
            facebook_auth_warning_text.visibility = View.VISIBLE
            text_input_new_password.visibility = View.GONE
            text_input_existing_password.visibility = View.GONE
        }else if(loggedUser.getProvider() == "google"){
            google_auth_warning_text.visibility = View.VISIBLE
            text_input_new_password.visibility = View.GONE
            text_input_existing_password.visibility = View.GONE
        }

        button_change_password.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_change_password -> {
                if(loggedUser.getProvider() == "fb" || loggedUser.getProvider() == "google"){
                    finish()
                }else{
                    if (isInternetAvailable(this)){
                        updatePassword(v)
                    } else {
                        makeErrorSnackbar(v, "An active internet connection is required.")
                    }
                }
            }
        }
    }

    private fun updatePassword(view: View) {
        val currentPassword = edit_text_existing_password.text.toString()
        val newPassword = edit_text_new_password.text.toString()

        if(TextUtils.isEmpty(currentPassword)){
            makeWarningSnackbar(view, "Please enter your current password")
            return
        }

        if(TextUtils.isEmpty(newPassword)){
            makeWarningSnackbar(view, "Please enter your new password")
            return
        }

        val credential = EmailAuthProvider.getCredential(loggedUser.getUserEmail(), currentPassword)
        val user = auth.currentUser

        user?.reauthenticate(credential)?.addOnCompleteListener{task ->
            if(task.isSuccessful){

                user.updatePassword(newPassword).addOnCompleteListener{secondTask ->
                    if(secondTask.isSuccessful){
                        makeSuccessSnackbar(view, "Password has been successfully changed.")
                        Handler().postDelayed({
                            finish()
                        },2000)
                    }else{
                        val e = secondTask.exception
                        if(e is FirebaseAuthWeakPasswordException){
                            makeErrorSnackbar(view, "Password is too short. Please enter more than 6 characters.")
                        }else{
                            makeErrorSnackbar(view, "Password could not be changed. $e")
                        }
                    }
                }

            }else{
                makeErrorSnackbar(view, "Incorrect current password. Please try again.")
            }
        }
    }
}
