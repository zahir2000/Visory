package com.taruc.visory.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.firebase.ui.auth.FirebaseUiException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

private const val RC_SIGN_IN_GOOGLE: Int = 2024

class RegisterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var userType: Int = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var selectedLanguage: String = "English"
    private var TAG = "RegisterActivity"
    private var callbackManager: CallbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // init managers
        initUi()
        initLanguageSpinner()
        initGoogleLogin()
        initFacebookLogin()

        // register using email
        button_register_submit.setOnClickListener{
            hideKeyboard(this, it)

            if(isInternetAvailable(applicationContext)){
                verifyRegisterDetails(it)
            } else{
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        // login using Facebook
        button_login_facebook.setOnClickListener{
            hideKeyboard(this, it)

            if (isInternetAvailable(applicationContext)) {
                val facebookPermission = arrayListOf("email", "public_profile")
                LoginManager.getInstance().logInWithReadPermissions(this, facebookPermission)
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

        // login using Google
        button_google.setOnClickListener{
            if (isInternetAvailable(applicationContext)) {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)
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
    }

    private fun initFacebookLogin() {
        // Initialize Facebook Login
        LoginManager.getInstance().registerCallback(callbackManager, object:
            FacebookCallback<LoginResult> {
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

    private fun initLanguageSpinner() {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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

    private fun verifyRegisterDetails(view: View) {
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

        fName = fName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        lName = lName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                val provider = getString(R.string.provider_email)
                val viewDialog = ViewDialog(this)
                viewDialog.showDialog()

                if(task.isSuccessful){
                    Log.d(TAG, "createUserWithEmailAndPassword:success")
                    val user = auth.currentUser

                    if (user != null) {
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

                        storeUserDetails(uid, provider, newUser)
                        sendEmailVerification(view)

                        Handler().postDelayed({
                            try {
                                viewDialog.hideDialog()
                                // check if the user was successfully retrieved before proceeding with next screen
                                if (LoggedUser(this).getUserName().isNotEmpty()) {
                                    onRegisterSuccessful()
                                }
                            } catch (e: Exception) { }
                        }, 3000)
                    }
                } else {
                    makeErrorSnackbar(view, "Email already exists.")
                }
            }
    }

    private fun sendEmailVerification(view: View) {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { emailSent ->
                if (emailSent.isSuccessful) {
                    makeSuccessSnackbar(view, "Email sent.")
                } else {
                    makeWarningSnackbar(view, "Email could not be sent.")
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        //return to previous activity
        onBackPressed()
        return true
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
                                onRegisterSuccessful()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                loginWithGoogle(account!!)
            } catch (e: ApiException) {}
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
                                onRegisterSuccessful()
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

    private fun onRegisterSuccessful() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
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

    private fun storeUserDetails(uid: String, provider: String, emailUser: User? = null) {
        val loggedUserTypePref = LoggedUser(this)
        val rootRef = FirebaseDatabase.getInstance().getReference("users")
        val user = auth.currentUser

        val newUser: User = if (provider == getString(R.string.provider_google) && emailUser != null) {
            emailUser
        } else {
            val name: String = user!!.displayName!!
            User(
                getFirstName(name),
                getLastName(name),
                user.email!!,
                "",
                userType,
                getCurrentDate(),
                "English"
            )
        }

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

    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectedLanguage = "English"
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedLanguage = parent?.getItemAtPosition(position)?.toString() ?: "English"
    }
}
