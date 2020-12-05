package com.taruc.visory.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseUiException
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.R
import com.taruc.visory.quickblox.utils.ViewDialog
import com.taruc.visory.utils.*
import kotlinx.android.synthetic.main.activity_login.*

private const val RC_SIGN_IN_FB = 2000
private const val RC_SIGN_IN_GOOGLE = 2004

class LoginActivity : AppCompatActivity() {

    private var userType: Int = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var textInputPassword: TextInputEditText
    private lateinit var textInputEmail: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        //implement back button
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        //Retrieve user role
        userType = intent.getIntExtra("USER_TYPE", 0)

        //Locate login input controls
        textInputPassword = findViewById(R.id.text_edit_password)
        textInputEmail = findViewById(R.id.text_edit_email)

        //Initialize FB login
        val facebookConfig = arrayListOf(AuthUI.IdpConfig.FacebookBuilder().build())
        button_facebook.setOnClickListener {
            hideKeyboard(this, it)

            if (isInternetAvailable(applicationContext)) {
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(facebookConfig)
                        .setIsSmartLockEnabled(false)
                        .build(),
                    RC_SIGN_IN_FB
                )
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        //Initialize Google login
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        button_google.setOnClickListener {
            hideKeyboard(this, it)

            if (isInternetAvailable(applicationContext)) {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        forgot_password_button.setOnClickListener {
            hideKeyboard(this, it)

            if (isInternetAvailable(applicationContext)) {
                val intent = Intent(this, ForgotPassActivity::class.java)
                startActivity(intent)
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        login_button_submit.setOnClickListener {
            hideKeyboard(this, it)

            if (isInternetAvailable(applicationContext)) {
                login(it)
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val fromRegister = intent.getBooleanExtra("fromRegister", false)
        if (!fromRegister) {
            menuInflater.inflate(R.menu.login_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login_register -> {
                val intent = Intent(this, RegisterActivity::class.java)
                intent.putExtra("fromLogin", true)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun login(view: View) {
        val password = textInputPassword.text.toString().trim()
        val email = textInputEmail.text.toString().trim()

        if (email.isEmpty()) {
            makeErrorSnackbar(view, "Email can't be empty.")
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            makeErrorSnackbar(view, "Please enter a valid email.")
            return
        }

        if (password.isEmpty()) {
            makeErrorSnackbar(view, "Password can't be empty.")
            return
        } else if (password.length !in 6..16) {
            makeErrorSnackbar(view, "Password must be between 6 to 16 characters.")
            return
        }

        //Email login after successful validation
        loginEmail(view, email, password)
    }

    private fun loginEmail(view: View, email: String, password: String) {
        val loggedUserTypePref = LoggedUser(this)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val viewDialog = ViewDialog(this)
                    viewDialog.showDialog()

                    // Sign in success, update UI with the signed-in user's information
                    makeSuccessSnackbar(view, "Login Successful")

                    val user = auth.currentUser
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val rootRef = FirebaseDatabase.getInstance().getReference("users")
                    val uidRef = rootRef.child(String.format("%s", uid))
                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            val userName = dataSnapshot.child("fname").value.toString() + " " +
                                        dataSnapshot.child("lname").value.toString()
                            val userEmail = dataSnapshot.child("email").value.toString()
                            val userContact = dataSnapshot.child("contactNo").value.toString()
                            val userJoinDate = dataSnapshot.child("datejoined").value.toString()
                            val userLanguage = dataSnapshot.child("language").value.toString()
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
                                userLanguage,
                                getString(R.string.provider_email)
                            )
                        }

                        override fun onCancelled(databaseError: DatabaseError) { }
                    }
                    uidRef.addListenerForSingleValueEvent(valueEventListener)

                    Handler().postDelayed({
                        try {
                            viewDialog.hideDialog()
                            var intent: Intent? = null

                            if (auth.currentUser != null) {
                                intent = if (auth.currentUser!!.isEmailVerified) {
                                    Intent(this, WelcomeActivity::class.java)
                                } else {
                                    Intent(this, VerifyEmailActivity::class.java)
                                }
                            }

                            intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            finish()
                        } catch (e: Exception) {
                            viewDialog.hideDialog()
                        }
                    }, 2000)
                } else {
                    // If sign in fails, display a message to the user.
                    makeErrorSnackbar(view, "Email and/or password is incorrect. Please try again")
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
            if (LoginManager.getInstance() != null){
                LoginManager.getInstance().logOut()
            }

            Log.d("LoginActivity", resultCode.toString())

            val response = IdpResponse.fromResultIntent(data)
            val viewDialog = ViewDialog(this)
            viewDialog.showDialog()
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                if (auth.currentUser != null) {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val rootRef = FirebaseDatabase.getInstance().getReference("users")

                    if (response?.isNewUser!!) {
                        val user = FirebaseAuth.getInstance().currentUser
                        val name: String = user!!.displayName!!

                        val newUser = User(
                            getFirstName(name),
                            getLastName(name),
                            user.email!!,
                            "",
                            userType,
                            getCurrentDate(),
                            "English"
                        )

                        //Store new user details into database
                        rootRef.child(uid).setValue(newUser).addOnCompleteListener {
                            //Store new user details into preferences
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
                                val role = Integer.parseInt(dataSnapshot.child("role").value.toString())
                                val avatarUrl: String = dataSnapshot.child("avatarurl").value.toString()
                                if (avatarUrl.isNotEmpty()) {
                                    loggedUserTypePref.setAvatarUrl(avatarUrl)
                                }

                                //store user details inside sharedPreferences so we don't need to load user data each time the app is opened
                                //if data is modified, it can directly be done using another activity.
                                loggedUserTypePref.setUserData(
                                    FirebaseAuth.getInstance().currentUser!!.uid,
                                    userName,
                                    userEmail,
                                    userContact,
                                    userJoinDate,
                                    role,
                                    "English",
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
                        onLoginSuccessful()
                    } catch (e: Exception) { }
                }, 2000)
            }

            else if (resultCode == Activity.RESULT_CANCELED) {
                viewDialog.hideDialog()
                longToast("Login is cancelled.")
            }

            else {
                viewDialog.hideDialog()
                val e = response?.error
                if (e is FirebaseUiException) {
                    longToast("There was an issue with login. Please try again.")
                    Log.d("FirebaseUiException", e.message.toString())
                } else {
                    Toast.makeText(applicationContext,"" + response!!.error!!.message,Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == RC_SIGN_IN_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                //Successful Google Sign In, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
            }
        }
    }

    private fun firebaseAuthWithGoogle(acc: GoogleSignInAccount) {
        val viewDialog = ViewDialog(this)
        viewDialog.showDialog()

        val loggedUserTypePref = LoggedUser(this)
        val credential = GoogleAuthProvider.getCredential(acc.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                when {
                    task.isSuccessful -> {
                        val rootRef = FirebaseDatabase.getInstance().getReference("users")
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid

                        if (task.result?.additionalUserInfo?.isNewUser!!) {
                            val user = auth.currentUser
                            val name = user!!.displayName!!

                            val newUser = User(
                                getFirstName(name),
                                getLastName(name),
                                user.email!!,
                                "",
                                userType,
                                getCurrentDate(),
                                "English"
                            )

                            rootRef.child(uid).setValue(newUser).addOnCompleteListener {
                                loggedUserTypePref.setUserData(
                                    FirebaseAuth.getInstance().currentUser!!.uid,
                                    newUser,
                                    getString(R.string.provider_google)
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
                                    val role = Integer.parseInt(dataSnapshot.child("role").value.toString())
                                    val avatarUrl: String = dataSnapshot.child("avatarurl").value.toString()
                                    if (avatarUrl.isNotEmpty()) {
                                        loggedUserTypePref.setAvatarUrl(avatarUrl)
                                    }

                                    //store user details inside sharedPreferences so we don't need to load user data each time the app is opened
                                    //if data is modified, it can directly be done using another activity.
                                    loggedUserTypePref.setUserData(
                                        FirebaseAuth.getInstance().currentUser!!.uid,
                                        userName,
                                        userEmail,
                                        userContact,
                                        userJoinDate,
                                        role,
                                        "English",
                                        getString(R.string.provider_google)
                                    )
                                }

                                override fun onCancelled(databaseError: DatabaseError) { }
                            }
                            uidRef.addListenerForSingleValueEvent(valueEventListener)
                        }

                        longToast("Login is successful.")

                        Handler().postDelayed({
                            try {
                                viewDialog.hideDialog()
                                onLoginSuccessful()
                            } catch (e: Exception) {
                            }
                        }, 2000)
                    }
                    task.isCanceled -> {
                        // If sign in cancelled, display a message to the user.
                        viewDialog.hideDialog()
                        longToast("Login is cancelled.")
                    }
                    else -> {
                        // If sign in fails, display a message to the user.
                        viewDialog.hideDialog()
                        longToast("There was an issue with login. Please try again.")
                    }
                }
            }
    }

    private fun onLoginSuccessful() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}
