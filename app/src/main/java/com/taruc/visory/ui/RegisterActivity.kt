package com.taruc.visory.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.taruc.visory.R
import kotlinx.android.synthetic.main.activity_register.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.utils.*
import java.lang.Exception


class RegisterActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 2420
    private val RC_SIGN_IN_GOOGLE: Int = 2024
    private var userType: Int = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var googleSignInClient: GoogleSignInClient

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        button_register_submit.setOnClickListener{
            register()
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

        button_google.setOnClickListener{
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)
        }
    }

    private fun register() {
        var fName = edit_text_fname.text.toString()
        var lName = edit_text_lname.text.toString()
        val email = edit_text_email.text.toString()
        val password = edit_text_password.text.toString()

        if(TextUtils.isEmpty(fName)){
            Toast.makeText(applicationContext, "Please enter your first name", Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(lName)){
            Toast.makeText(applicationContext, "Please enter your last name", Toast.LENGTH_SHORT).show()
            return
        }
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

        fName = fName.capitalize()
        lName = lName.capitalize()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    val database = FirebaseDatabase.getInstance()
                    val myRef = database.getReference("users")
                    val key = FirebaseAuth.getInstance().currentUser!!.uid
                    val newUser = User(
                        fName,
                        lName,
                        email,
                        userType,
                        getCurrentDate(),
                        "English" // TODO : User preferred language
                    )
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(applicationContext,  "Email sent.", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Toast.makeText(applicationContext,  "Email could not be sent.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    myRef.child(key).setValue(newUser).addOnCompleteListener{
                        Toast.makeText(applicationContext, "Registration successful", Toast.LENGTH_SHORT).show()
                        val loggedUserTypePref = LoggedUser(this)
                        val userName = "$fName $lName"
                        loggedUserTypePref.setUserData(
                            userName,
                            email,
                            getCurrentDate(),
                            userType
                        )

                        val intent = Intent(this, VerifyEmailActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }else{
                    Toast.makeText(applicationContext,  "Email already exists.", Toast.LENGTH_SHORT).show()
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
        val loggedUserTypePref = LoggedUser(this)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                if(auth.currentUser != null){
                    if(response?.isNewUser!!){
                        val user = FirebaseAuth.getInstance().currentUser
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("users")

                        val name:String = user!!.displayName!!
                        val key = FirebaseAuth.getInstance().currentUser!!.uid

                        val newUser = User(
                            getFirstName(name),
                            getLastName(name),
                            user.email!!,
                            userType,
                            getCurrentDate(),
                            "English" // TODO : User preferred language
                        )
                        myRef.child(key).setValue(newUser).addOnCompleteListener{
                            loggedUserTypePref.setUserData(
                                name,
                                user.email!!,
                                getCurrentDate(),
                                userType
                            )
                        }
                    }
                    else{
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
                    }
                }

                Handler().postDelayed({
                    try {
                        val intent = Intent(this, WelcomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {}
                }, 3000)
            } else {
                Toast.makeText(applicationContext, "" + response!!.error!!.message, Toast.LENGTH_SHORT).show()
                //TODO : check if facebook email is not used before.
            }
        }
        else if (requestCode == RC_SIGN_IN_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {

            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        //TODO: Check if user has registered before
        val loggedUserTypePref = LoggedUser(this)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if(task.result?.additionalUserInfo?.isNewUser!!){
                        val user = auth.currentUser
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("users")

                        val name = user!!.displayName!!
                        val key = FirebaseAuth.getInstance().currentUser!!.uid

                        val newUser = User(
                            getFirstName(name),
                            getLastName(name),
                            user.email!!,
                            userType,
                            getCurrentDate(),
                            "English" // TODO : User preferred language
                        )
                        myRef.child(key).setValue(newUser)

                        loggedUserTypePref.setUserData(
                            name,
                            user.email!!,
                            getCurrentDate(),
                            userType
                        )
                    }
                    else{
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
                    }

                    Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()

                    Handler().postDelayed({
                        try {
                            val intent = Intent(this, WelcomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {}
                    }, 3000)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
