package com.taruc.visory.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.R
import com.taruc.visory.utils.*
import kotlinx.android.synthetic.main.activity_personal_details.*

class PersonalDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var loggedUser: LoggedUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_details)

        supportActionBar?.title = "Edit Profile"
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        loggedUser = LoggedUser(this)

        edit_text_first_name.setText(getFirstName(loggedUser.getUserName()))
        edit_text_last_name.setText(getLastName(loggedUser.getUserName()))

        button_update_profile.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_update_profile -> {
                val fName = edit_text_first_name.text.toString()
                val lName = edit_text_last_name.text.toString()

                if(loggedUser.getProvider() == "fb" || loggedUser.getProvider() == "google"){
                    updateProfile(v, fName, lName)
                }
            }
        }
    }

    private fun updateProfile(view: View, fName: String, lName: String) {
        if(TextUtils.isEmpty(fName)){
            makeWarningSnackbar(view, "Please enter your first name")
            return
        }

        if(TextUtils.isEmpty(lName)){
            makeWarningSnackbar(view, "Please enter your last name")
            return
        }

        val rootRef = FirebaseDatabase.getInstance().getReference("users")
        val uidRef = rootRef.child(String.format("%s", loggedUser.getUserID()))
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                rootRef.child(loggedUser.getUserID())
                    .child("lname").setValue(lName)
                rootRef.child(loggedUser.getUserID())
                    .child("fname").setValue(fName).addOnCompleteListener{task ->
                        if(task.isSuccessful) {
                            makeSuccessSnackbar(view, "Profile details updated successfully.")
                            loggedUser.setUserName("$fName $lName")

                            Handler().postDelayed({
                                finish()
                            }, 2000)
                        }else{
                            makeErrorSnackbar(view, "Profile details was not updated.")
                        }
                    }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        uidRef.addListenerForSingleValueEvent(valueEventListener)
    }
}
