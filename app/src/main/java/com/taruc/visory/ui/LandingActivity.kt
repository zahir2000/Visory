package com.taruc.visory.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.google.firebase.auth.FirebaseAuth
import com.taruc.visory.BlindHomeActivity
import com.taruc.visory.R
import com.taruc.visory.VolunteerHomeActivity
import com.taruc.visory.utils.LoggedUser
import com.taruc.visory.utils.UserType
import kotlinx.android.synthetic.main.activity_landing.*


class LandingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val userTypePref = UserType(this)

        auth = FirebaseAuth.getInstance()

        buttonVolunteer.setOnClickListener {
            val intent = Intent(this, LandingActionsActivity::class.java)
            userTypePref.setUserType(1)
            startActivity(intent)
        }

        buttonBlind.setOnClickListener {
            val intent = Intent(this, LandingActionsActivity::class.java)
            userTypePref.setUserType(2)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            updateUI()
        } catch (e: Exception) {

        }
    }

    private fun updateUI() {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        val loggedUserTypePref = LoggedUser(this)

        if (loggedUserTypePref.getUserType() == 0) {
            if (auth.currentUser != null)
                auth.signOut()
        }

        if (auth.currentUser != null) {
            if (auth.currentUser!!.isEmailVerified || isLoggedIn) {
                if (loggedUserTypePref.getUserType() == 1) {
                    val intent = Intent(this, VolunteerHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                } else {
                    val intent = Intent(this, BlindHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }

            } else {
                val intent = Intent(this, VerifyEmailActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.landing_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about_visory -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
