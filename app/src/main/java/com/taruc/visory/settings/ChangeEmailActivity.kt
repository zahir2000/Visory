package com.taruc.visory.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.R
import com.taruc.visory.utils.LoggedUser
import com.taruc.visory.utils.makeErrorSnackbar
import com.taruc.visory.utils.makeSuccessSnackbar
import com.taruc.visory.utils.makeWarningSnackbar
import kotlinx.android.synthetic.main.activity_change_email.*

class ChangeEmailActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var loggedUser: LoggedUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        supportActionBar?.title = "Edit Email"

        auth = FirebaseAuth.getInstance()
        loggedUser = LoggedUser(this)

        if(loggedUser.getProvider() == "fb"){
            facebook_auth_warning_text.visibility = View.VISIBLE
            edit_text_new_email.visibility = View.GONE
            edit_text_current_password.visibility = View.GONE
        }else if(loggedUser.getProvider() == "google"){
            google_auth_warning_text.visibility = View.VISIBLE
            edit_text_new_email.visibility = View.GONE
            edit_text_current_password.visibility = View.GONE
        }

        button_change_email.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_change_email -> {
                if(loggedUser.getProvider() == "fb" || loggedUser.getProvider() == "google"){
                    finish()
                }else{
                    updateEmail(v)
                }
            }
        }
    }

    private fun updateEmail(view: View) {
        val newEmail = edit_text_new_email.text.toString()
        val currentPassword = edit_text_current_password.text.toString()

        if(TextUtils.isEmpty(newEmail)){
            makeWarningSnackbar(view, "Please enter your new email")
            return
        }else if(!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()){
            makeWarningSnackbar(view, "Please enter a valid email")
            return
        }

        if(TextUtils.isEmpty(currentPassword)){
            makeWarningSnackbar(view, "Please enter your current password")
            return
        }

        val credential = EmailAuthProvider.getCredential(loggedUser.getUserEmail(), currentPassword)
        val user = auth.currentUser
        user?.reauthenticate(credential)?.addOnCompleteListener{task ->
            if(task.isSuccessful){
                user.updateEmail(newEmail).addOnCompleteListener{newTask ->
                    if(newTask.isSuccessful){
                        makeSuccessSnackbar(view, "Email has been successfully changed.")
                        changeDBEmail(newEmail)
                        Handler().postDelayed({
                            finish()
                        },2000)
                    }
                    else{
                        val e = newTask.exception
                        if(e is FirebaseAuthUserCollisionException){
                            makeErrorSnackbar(view, "This email already exists.")
                        }else{
                            makeErrorSnackbar(view, "Email could not be changed. $e")
                        }
                    }
                }
            }
            else{
                makeErrorSnackbar(view, "Incorrect current password. Please try again.")
            }
        }
    }

    private fun changeDBEmail(newEmail: String) {
        val rootRef = FirebaseDatabase.getInstance().getReference("users")
        val uidRef = rootRef.child(String.format("%s", loggedUser.getUserID()))

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                rootRef.child(loggedUser.getUserID())
                    .child("email").setValue(newEmail).addOnCompleteListener{task ->
                        if(task.isSuccessful) {
                            loggedUser.setUserEmail(newEmail)
                        }
                    }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        uidRef.addListenerForSingleValueEvent(valueEventListener)
    }
}
