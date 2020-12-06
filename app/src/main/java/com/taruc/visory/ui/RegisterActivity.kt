package com.taruc.visory.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.R
import com.taruc.visory.quickblox.utils.ViewDialog
import com.taruc.visory.utils.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import java.util.regex.Pattern

private const val RC_SIGN_IN_FB: Int = 2420
private const val RC_SIGN_IN_GOOGLE: Int = 2024

class RegisterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var userType: Int = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var selectedLanguage: String = "English"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        //Retrieve user role
        userType = intent.getIntExtra("USER_TYPE", 0)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize language spinner
        initializeLanguageSpinner()

        button_register_submit.setOnClickListener{
            hideKeyboard(this, it)

            if(isInternetAvailable(applicationContext)){
                register(it)
            } else{
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        //Initialize FB registration
        val providers = arrayListOf(
            AuthUI.IdpConfig.FacebookBuilder().build()
        )
        button_facebook.setOnClickListener{
            hideKeyboard(this, it)

            if(isInternetAvailable(applicationContext)){
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                    RC_SIGN_IN_FB
                )
            }
            else{
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        //Initialize Google registration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        button_google.setOnClickListener{
            if(isInternetAvailable(applicationContext)){
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)
            }
            else{
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }
    }

    private fun initializeLanguageSpinner() {
        val spinner: Spinner = findViewById(R.id.language_spinner)
        spinner.prompt = getString(R.string.select_language_spinner)
        spinner.onItemSelectedListener = this

        ArrayAdapter.createFromResource(
            this,
            R.array.language_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val fromLogin = intent.getBooleanExtra("fromLogin", false)
        if(!fromLogin){
            menuInflater.inflate(R.menu.register_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.register_login -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("fromRegister", true)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun register(view: View) {
        var fName = edit_text_fname.text.toString().trim()
        var lName = edit_text_lname.text.toString().trim()
        val email = text_edit_email.text.toString().trim()
        val contact = text_edit_contact.text.toString().trim()
        val password = text_edit_password.text.toString().trim()

        if(TextUtils.isEmpty(fName)){
            makeWarningSnackbar(view, "Please enter your first name")
            return
        }
        if(TextUtils.isEmpty(lName)){
            makeWarningSnackbar(view, "Please enter your last name")
            return
        }
        if(TextUtils.isEmpty(email)){
            makeWarningSnackbar(view, "Please enter your email")
            return
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            makeWarningSnackbar(view, "Please enter a valid email")
            return
        }
        if(TextUtils.isEmpty(contact)){
            makeWarningSnackbar(view, "Please enter your contact number")
            return
        }

        when {
            password.isEmpty() -> {
                makeErrorSnackbar(view, "Password can't be empty.")
                return
            }
            password.length !in 6..16 -> {
                makeErrorSnackbar(view, "Password must be between 6 to 16 characters.")
                return
            }
        }

        fName = fName.capitalize(Locale.ROOT)
        lName = lName.capitalize(Locale.ROOT)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    val viewDialog = ViewDialog(this)
                    viewDialog.showDialog()

                    val rootRef = FirebaseDatabase.getInstance().getReference("users")
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val newUser = User(
                        fName,
                        lName,
                        email,
                        contact,
                        userType,
                        getCurrentDate(),
                        selectedLanguage
                    )
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { emailSent ->
                            if (emailSent.isSuccessful) {
                                makeSuccessSnackbar(view, "Email sent.")
                            } else {
                                makeWarningSnackbar(view, "Email could not be sent.")
                            }
                        }

                    rootRef.child(uid).setValue(newUser).addOnCompleteListener{
                        makeSuccessSnackbar(view, "Registration successful")

                        val loggedUserTypePref = LoggedUser(this)
                        loggedUserTypePref.setUserData(
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            newUser,
                            getString(R.string.provider_email)
                        )

                        viewDialog.hideDialog()
                        val intent = Intent(this, VerifyEmailActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        finish()
                    }
                } else {
                    makeErrorSnackbar(view, "Email already exists.")
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        //return to previous activity
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val loggedUserTypePref = LoggedUser(this)

        if (requestCode == RC_SIGN_IN_FB) {
            val response = IdpResponse.fromResultIntent(data)

            val viewDialog = ViewDialog(this)
            viewDialog.showDialog()

            when (resultCode) {
                Activity.RESULT_OK -> {
                    // Successfully signed in
                    if(auth.currentUser != null){
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val rootRef = FirebaseDatabase.getInstance().getReference("users")

                        if(response?.isNewUser!!){
                            val user = FirebaseAuth.getInstance().currentUser
                            val name = user!!.displayName!!

                            val newUser = User(
                                getFirstName(name),
                                getLastName(name),
                                user.email!!,
                                "",
                                userType,
                                getCurrentDate(),
                                selectedLanguage
                            )
                            rootRef.child(uid).setValue(newUser).addOnCompleteListener{
                                loggedUserTypePref.setUserData(
                                    FirebaseAuth.getInstance().currentUser!!.uid,
                                    newUser,
                                    getString(R.string.provider_fb)
                                )
                            }
                        } else {
                            val uidRef = rootRef.child(String.format("%s", uid))
                            val valueEventListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {

                                    val userName = dataSnapshot.child("fname").value.toString() + " " +
                                            dataSnapshot.child("lname").value.toString()
                                    val userEmail = dataSnapshot.child("email").value.toString()
                                    val userContact = dataSnapshot.child("contactNo").value.toString()
                                    val userJoinDate = dataSnapshot.child("datejoined").value.toString()
                                    val role = Integer.parseInt(dataSnapshot.child("role").value!!.toString())
                                    val avatarUrl: String? = dataSnapshot.child("avatarurl").value.toString()
                                    if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                                        loggedUserTypePref.setAvatarUrl(avatarUrl)
                                    }

                                    //store user details inside sharedPreferences so we don't need to load user data each time the app is opened
                                    //if data is modified, it can directly be done using another activity.
                                    loggedUserTypePref.setUserData(
                                        uid,
                                        userName,
                                        userEmail,
                                        userContact,
                                        userJoinDate,
                                        role,
                                        selectedLanguage,
                                        getString(R.string.provider_fb)
                                    )
                                }

                                override fun onCancelled(databaseError: DatabaseError) { }
                            }
                            uidRef.addListenerForSingleValueEvent(valueEventListener)
                        }
                    }

                    Handler().postDelayed({
                        try {
                            viewDialog.hideDialog()
                            onRegisterSuccessful()
                        } catch (e: Exception) { }
                    }, 2000)
                }
                Activity.RESULT_CANCELED -> {
                    viewDialog.hideDialog()
                    Toast.makeText(applicationContext, "Login is cancelled.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    viewDialog.hideDialog()
                    Toast.makeText(
                        applicationContext,
                        "" + response!!.error!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        else if (requestCode == RC_SIGN_IN_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {}
        }
    }

    private fun firebaseAuthWithGoogle(acc: GoogleSignInAccount) {
        val viewDialog = ViewDialog(this)
        viewDialog.showDialog()

        val loggedUserTypePref = LoggedUser(this)
        val credential = GoogleAuthProvider.getCredential(acc.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val rootRef = FirebaseDatabase.getInstance().getReference("users")

                    if(task.result?.additionalUserInfo?.isNewUser!!){
                        val user = auth.currentUser
                        val name = user!!.displayName!!

                        val newUser = User(
                            getFirstName(name),
                            getLastName(name),
                            user.email!!,
                            "",
                            userType,
                            getCurrentDate(),
                            selectedLanguage
                        )

                        rootRef.child(uid).setValue(newUser).addOnCompleteListener {
                            loggedUserTypePref.setUserData(
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                newUser,
                                getString(R.string.provider_google)
                            )
                        }
                    }
                    else{
                        val uidRef = rootRef.child(String.format("%s", uid))
                        val valueEventListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                val userName = dataSnapshot.child("fname").value.toString() + " " +
                                        dataSnapshot.child("lname").value.toString()
                                val userEmail = dataSnapshot.child("email").value.toString()
                                val userContact = dataSnapshot.child("contactNo").value.toString()
                                val userJoinDate = dataSnapshot.child("datejoined").value.toString()
                                val role = Integer.parseInt(dataSnapshot.child("role").value!!.toString())
                                val avatarUrl: String? = dataSnapshot.child("avatarurl").value.toString()
                                if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                                    loggedUserTypePref.setAvatarUrl(avatarUrl)
                                }

                                //store user details inside sharedPreferences so we don't need to load user data each time the app is opened
                                //if data is modified, it can directly be done using another activity.
                                loggedUserTypePref.setUserData(
                                    uid,
                                    userName,
                                    userEmail,
                                    userContact,
                                    userJoinDate,
                                    role,
                                    selectedLanguage,
                                    getString(R.string.provider_google)
                                )
                            }
                            override fun onCancelled(databaseError: DatabaseError) { }
                        }
                        uidRef.addListenerForSingleValueEvent(valueEventListener)
                    }

                    Handler().postDelayed({
                        try {
                            viewDialog.hideDialog()
                            onRegisterSuccessful()
                        } catch (e: Exception) { }
                    }, 2000)
                } else {
                    // If sign in fails, display a message to the user.
                    viewDialog.hideDialog()
                    shortToast("Login was unsuccessful. Please try again")
                    Log.d("RegisterActivity", task.exception.toString())
                }
            }
    }

    private fun onRegisterSuccessful() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectedLanguage = "English"
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedLanguage = parent?.getItemAtPosition(position)?.toString() ?: "English"
    }
}
