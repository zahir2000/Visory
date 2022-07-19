package com.taruc.visory.ui

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
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.firebase.ui.auth.FirebaseUiException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FacebookAuthProvider
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

private const val RC_SIGN_IN_GOOGLE = 2004

class LoginActivity : AppCompatActivity() {

    private var userType: Int = 0
    private val TAG: String = "LoginActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var textInputPassword: TextInputEditText
    private lateinit var textInputEmail: TextInputEditText
    private var callbackManager: CallbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // init managers
        initUi()
        initGoogleLogin()
        initFacebookLogin()

        // login using Facebook
        button_login_facebook.setOnClickListener {
            hideKeyboard(this, it)

            if (isInternetAvailable(applicationContext)) {
                val facebookPermission = arrayListOf("email", "public_profile")
                LoginManager.getInstance().logInWithReadPermissions(this, facebookPermission)
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        // login using Google
        button_google.setOnClickListener {
            hideKeyboard(this, it)

            if (isInternetAvailable(applicationContext)) {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        // login using email
        login_button_submit.setOnClickListener {
            hideKeyboard(this, it)

            if (isInternetAvailable(applicationContext)) {
                verifyEmailLoginDetails(it)
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        // forgot password
        forgot_password_button.setOnClickListener {
            hideKeyboard(this, it)

            if (isInternetAvailable(applicationContext)) {
                val intent = Intent(this, ForgotPassActivity::class.java)
                startActivity(intent)
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }
    }

    private fun initUi() {
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
    }

    private fun initFacebookLogin() {
        // Initialize Facebook Login
        LoginManager.getInstance().registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }

            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:${result}")
                loginWithFacebook(result.accessToken)
            }
        })
    }

    private fun initGoogleLogin() {
        //Initialize Google login
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                //Successful Google Sign In, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                loginWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.e(TAG, "onActivityResult:${e}")
            }
        }
    }

    private fun verifyEmailLoginDetails(view: View) {
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
        loginWithEmail(view, email, password)
    }

    private fun retrieveUserDetails(uid: String, provider: String) {
        val loggedUserTypePref = LoggedUser(this)
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
                val avatarUrl: String = dataSnapshot.child("avatarurl").value.toString()
                if (avatarUrl.isNotEmpty()) {
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
                    provider
                )
            }
            override fun onCancelled(databaseError: DatabaseError) { }
        }
        uidRef.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun storeUserDetails(uid: String, provider: String) {
        val loggedUserTypePref = LoggedUser(this)
        val rootRef = FirebaseDatabase.getInstance().getReference("users")
        val user = auth.currentUser

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
                provider
            )
        }.addOnFailureListener {
            Log.e(TAG, "Facebook Firebase Database Error: ${it.message.toString()}")
        }
    }

    private fun loginWithEmail(view: View, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val viewDialog = ViewDialog(this)
                    viewDialog.showDialog()

                    // Sign in success, update UI with the signed-in user's information
                    makeSuccessSnackbar(view, "Login Successful")

                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val provider = getString(R.string.provider_email)
                    retrieveUserDetails(uid, provider)

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

    private fun loginWithFacebook(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        val provider = getString(R.string.provider_fb)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                val viewDialog = ViewDialog(this)
                viewDialog.showDialog()

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    if (user != null) {
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val isNewUser = task.result.additionalUserInfo!!.isNewUser
                        Log.d(TAG, "Is user new? $isNewUser")

                        if (isNewUser) {
                            storeUserDetails(uid, provider)
                        } else {
                            retrieveUserDetails(uid, provider)
                        }
                    }

                    Handler().postDelayed({
                        try {
                            viewDialog.hideDialog()
                            // check if the user was successfully retrieved before proceeding with next screen
                            if (LoggedUser(this).getUserName().isNotEmpty()) {
                                onLoginSuccessful()
                            }
                        } catch (e: Exception) { }
                    }, 3000)
                } else {
                    viewDialog.hideDialog()
                    val e = task.exception
                    if (e is FirebaseUiException) {
                        longToast("There was an issue with login. Please try again.")
                        Log.d("FirebaseUiException", e.message.toString())
                    } else {
                        Toast.makeText(applicationContext,"" + task.exception!!.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun loginWithGoogle(acc: GoogleSignInAccount) {
        val viewDialog = ViewDialog(this)
        viewDialog.showDialog()

        val credential = GoogleAuthProvider.getCredential(acc.idToken, null)
        val provider = getString(R.string.provider_google)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                when {
                    task.isSuccessful -> {
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val isNewUser = task.result?.additionalUserInfo?.isNewUser!!

                        if (isNewUser) {
                            storeUserDetails(uid, provider)
                        } else {
                            retrieveUserDetails(uid, provider)
                        }

                        longToast("Login is successful.")

                        Handler().postDelayed({
                            try {
                                viewDialog.hideDialog()
                                onLoginSuccessful()
                            } catch (e: Exception) { }
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() //return to previous activity
        return true
    }
}
