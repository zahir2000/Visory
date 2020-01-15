package com.taruc.visory.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class LoginActivity : AppCompatActivity() {

    private var userType: Int = 0
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 2000
    private val RC_SIGN_IN_GOOGLE = 2004
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

        //userType = intent.getIntExtra("userType", 0)
        val userTypePref = UserType(this)
        userType = userTypePref.getUserType()

        textInputPassword = findViewById(R.id.text_edit_password)
        textInputEmail = findViewById(R.id.text_edit_email)
        setTitle(R.string.label_login)

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
                    RC_SIGN_IN
                )
            } else {
                makeErrorSnackbar(it, getString(R.string.active_internet_connection))
            }
        }

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
            val intent = Intent(this, ForgotPassActivity::class.java)
            startActivity(intent)
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

        val loggedUserTypePref = LoggedUser(this)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val viewDialog = ViewDialog(this)
                    viewDialog.showDialog()

                    // Sign in success, update UI with the signed-in user's information
                    makeSuccessSnackbar(view, "Login Successful")
                    //Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser

                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val rootRef = FirebaseDatabase.getInstance().getReference("users")
                    //val uidRef = rootRef.child(String.format("%s/role", uid))
                    val uidRef = rootRef.child(String.format("%s", uid))
                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val userName =
                                dataSnapshot.child("fname").value.toString() + " " + dataSnapshot.child(
                                    "lname"
                                ).value.toString()
                            val userEmail = dataSnapshot.child("email").value.toString()
                            val userJoinDate = dataSnapshot.child("datejoined").value.toString()
                            val userLanguage = dataSnapshot.child("language").value.toString()
                            val role =
                                Integer.parseInt(dataSnapshot.child("role").value!!.toString())

                            val avatarUrl: String? =
                                dataSnapshot.child("avatarurl").value.toString()
                            if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                                loggedUserTypePref.setAvatarUrl(avatarUrl)
                            }

                            //store user details inside sharedPreferences so we don't need to load user data each time the app is opened
                            //if data is modified, it can directly be done using another activity.
                            loggedUserTypePref.setUserData(
                                uid,
                                userName,
                                userEmail,
                                userJoinDate,
                                role,
                                userLanguage,
                                getString(R.string.provider_email)
                            )

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    }
                    uidRef.addListenerForSingleValueEvent(valueEventListener)

                    Handler().postDelayed({
                        try {
                            if (auth.currentUser != null) {
                                if (auth.currentUser!!.isEmailVerified) {
                                    viewDialog.hideDialog()
                                    val intent = Intent(this, WelcomeActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                    )
                                    finish()
                                } else {
                                    viewDialog.hideDialog()
                                    val intent = Intent(this, VerifyEmailActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                    )
                                    finish()
                                }
                            }
                        } catch (e: Exception) {
                            viewDialog.hideDialog()
                        }
                    }, 3000)
                } else {
                    // If sign in fails, display a message to the user.
                    makeErrorSnackbar(view, "Email and/or password is incorrect. Please try again")
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
            val viewDialog = ViewDialog(this)
            viewDialog.showDialog()
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                if (auth.currentUser != null) {
                    if (response?.isNewUser!!) {
                        val user = FirebaseAuth.getInstance().currentUser
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("users")

                        val name: String = user!!.displayName!!
                        val key = FirebaseAuth.getInstance().currentUser!!.uid

                        val newUser = User(
                            getFirstName(name),
                            getLastName(name),
                            user.email!!,
                            userType,
                            getCurrentDate(),
                            "English"
                        )
                        myRef.child(key).setValue(newUser).addOnCompleteListener {
                            loggedUserTypePref.setUserData(
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                name,
                                user.email!!,
                                getCurrentDate(),
                                userType,
                                "English",
                                getString(R.string.provider_fb)
                            )
                        }
                    } else {
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val rootRef = FirebaseDatabase.getInstance().getReference("users")
                        //val uidRef = rootRef.child(String.format("%s/role", uid))
                        val uidRef = rootRef.child(String.format("%s", uid))
                        val valueEventListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val userName =
                                    dataSnapshot.child("fname").value.toString() + " " + dataSnapshot.child(
                                        "lname"
                                    ).value.toString()
                                val userEmail = dataSnapshot.child("email").value.toString()
                                val userJoinDate = dataSnapshot.child("datejoined").value.toString()
                                val role =
                                    Integer.parseInt(dataSnapshot.child("role").value.toString())

                                val avatarUrl: String? =
                                    dataSnapshot.child("avatarurl").value.toString()
                                if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                                    loggedUserTypePref.setAvatarUrl(avatarUrl)
                                }

                                //store user details inside sharedPreferences so we don't need to load user data each time the app is opened
                                //if data is modified, it can directly be done using another activity.
                                loggedUserTypePref.setUserData(
                                    FirebaseAuth.getInstance().currentUser!!.uid,
                                    userName,
                                    userEmail,
                                    userJoinDate,
                                    role,
                                    "English",
                                    getString(R.string.provider_fb)
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
                        viewDialog.hideDialog()
                        val intent = Intent(this, WelcomeActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        finish()
                    } catch (e: Exception) {
                    }
                }, 3000)
            } else {
                viewDialog.hideDialog()
                val e = response?.error
                if (e is FirebaseUiException) {
                    Toast.makeText(applicationContext, "Login is cancelled.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "" + response!!.error!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else if (requestCode == RC_SIGN_IN_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
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
                if (task.isSuccessful) {
                    if (task.result?.additionalUserInfo?.isNewUser!!) {
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
                            "English"
                        )
                        myRef.child(key).setValue(newUser)

                        loggedUserTypePref.setUserData(
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            name,
                            user.email!!,
                            getCurrentDate(),
                            userType,
                            "English",
                            getString(R.string.provider_google)
                        )
                    } else {
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val rootRef = FirebaseDatabase.getInstance().getReference("users")
                        //val uidRef = rootRef.child(String.format("%s/role", uid))
                        val uidRef = rootRef.child(String.format("%s", uid))
                        val valueEventListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val userName =
                                    dataSnapshot.child("fname").value.toString() + " " + dataSnapshot.child(
                                        "lname"
                                    ).value.toString()
                                val userEmail = dataSnapshot.child("email").value.toString()
                                val userJoinDate = dataSnapshot.child("datejoined").value.toString()
                                val role =
                                    Integer.parseInt(dataSnapshot.child("role").value.toString())

                                val avatarUrl: String? =
                                    dataSnapshot.child("avatarurl").value.toString()
                                if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                                    loggedUserTypePref.setAvatarUrl(avatarUrl)
                                }

                                //store user details inside sharedPreferences so we don't need to load user data each time the app is opened
                                //if data is modified, it can directly be done using another activity.
                                loggedUserTypePref.setUserData(
                                    FirebaseAuth.getInstance().currentUser!!.uid,
                                    userName,
                                    userEmail,
                                    userJoinDate,
                                    role,
                                    "English",
                                    getString(R.string.provider_google)
                                )

                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                        }
                        uidRef.addListenerForSingleValueEvent(valueEventListener)
                    }

                    Toast.makeText(applicationContext, "Login is successful", Toast.LENGTH_SHORT)
                        .show()

                    Handler().postDelayed({
                        try {
                            viewDialog.hideDialog()
                            val intent = Intent(this, WelcomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            finish()
                        } catch (e: Exception) {
                        }
                    }, 3000)
                } else {
                    // If sign in fails, display a message to the user.
                    viewDialog.hideDialog()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}
