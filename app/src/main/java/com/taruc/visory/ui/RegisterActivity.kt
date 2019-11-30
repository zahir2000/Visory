package com.taruc.visory.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.taruc.visory.R
import com.taruc.visory.User
import com.taruc.visory.UserType
import kotlinx.android.synthetic.main.activity_register.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse


class RegisterActivity : AppCompatActivity() {

    val RC_SIGN_IN: Int = 2420
    private var userType: Int = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var providers: List<AuthUI.IdpConfig>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        //userType = intent.getIntExtra("userType", 0)
        val userTypePref = UserType(this)
        userType = userTypePref.getUserType()

        setTitle(R.string.label_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val providers = arrayListOf(
            AuthUI.IdpConfig.FacebookBuilder().build())

        button_register_submit.setOnClickListener{
            val fName = edit_text_fname.text.toString()
            val lName = edit_text_lname.text.toString()
            val email = edit_text_email.text.toString()
            val password = edit_text_password.text.toString()

            if(TextUtils.isEmpty(fName)){
                Toast.makeText(applicationContext, "Please enter your first name", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(lName)){
                Toast.makeText(applicationContext, "Please enter your last name", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(email)){
                Toast.makeText(applicationContext, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(applicationContext, "Please enter your password", Toast.LENGTH_SHORT).show()
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->
                    if(task.isSuccessful){
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("users")
                        val key = FirebaseAuth.getInstance().currentUser!!.uid
                        val newUser = User(fName, lName, email, userType)
                        val user = auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(applicationContext,  "Email sent.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        myRef.child(key).setValue(newUser).addOnCompleteListener{
                            Toast.makeText(applicationContext, "Registration successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, VerifyEmailActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
        }

        button_facebook.setOnClickListener{
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)
                    .build(),
                RC_SIGN_IN)
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

    public override fun onStart() {
        super.onStart()
    }

    override fun onSupportNavigateUp(): Boolean {
        //return to previous activity
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("users")

                val name:String = user!!.displayName!!
                val key = FirebaseAuth.getInstance().currentUser!!.uid
                val firstSpace: Int = name.indexOf(" ")
                val firstName = name.substring(0, firstSpace)
                val lastName = name.substring(firstSpace).trim()

                val newUser = User(firstName, lastName, user.email!!, userType)
                myRef.child(key).setValue(newUser)
                //Toast.makeText(applicationContext, " $lastName", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext, "" + response!!.error!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
