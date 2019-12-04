package com.taruc.visory.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.R
import com.taruc.visory.utils.LoggedUser
import com.taruc.visory.utils.UserType
import kotlinx.android.synthetic.main.activity_forgot_pass.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.email_text
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private var userType: Int = 0
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        //implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        //userType = intent.getIntExtra("userType", 0)
        val userTypePref = UserType(this)
        userType = userTypePref.getUserType()

        setTitle(R.string.label_login)

        forgot_password_button.setOnClickListener {
            val intent = Intent(this, ForgotPassActivity::class.java)
            startActivity(intent)
        }

        login_button_submit.setOnClickListener{
            login()
        }
    }

    private fun login() {
        val email = email_text.text.toString()
        val password = password_text.text.toString()
        val loggedUserTypePref = LoggedUser(this)

        if(TextUtils.isEmpty(email)){
            Toast.makeText(applicationContext, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(applicationContext, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(applicationContext, "Please enter your password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser

                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val rootRef = FirebaseDatabase.getInstance().getReference("users")
                    //val uidRef = rootRef.child(String.format("%s/role", uid))
                    val uidRef = rootRef.child(String.format("%s", uid))
                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val userName = dataSnapshot.child("fname").getValue().toString() + " " + dataSnapshot.child("lname").getValue().toString()
                            val userEmail = dataSnapshot.child("email").getValue().toString()
                            val userJoinDate = dataSnapshot.child("datejoined").getValue().toString()
                            val role = Integer.parseInt(dataSnapshot.child("role").getValue()!!.toString())

                            //store user details inside sharedPreferences so we don't need to load user data each time the app is opened
                            //if data is modified, it can directly be done using another activity.
                            loggedUserTypePref.setUserData(
                                userName,
                                userEmail,
                                userJoinDate,
                                role
                            )

                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    }
                    uidRef.addListenerForSingleValueEvent(valueEventListener)

                    Handler().postDelayed({
                        try {
                            if(auth.currentUser != null){
                                if(auth.currentUser!!.isEmailVerified){
                                    val intent = Intent(this, WelcomeActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                else{
                                    val intent = Intent(this, VerifyEmailActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } catch (e: Exception) {}
                    }, 3000)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
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
}
