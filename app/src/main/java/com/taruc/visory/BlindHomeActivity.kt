package com.taruc.visory

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.taruc.visory.jalal.FirebaseService
import com.taruc.visory.quickblox.activities.PermissionsActivity
import com.taruc.visory.quickblox.utils.CALL_TOPIC
import com.taruc.visory.quickblox.utils.CHECK_PERMISSIONS
import com.taruc.visory.quickblox.utils.Helper
import com.taruc.visory.quickblox.utils.PERMISSIONS
import com.taruc.visory.utils.LoggedUser

class BlindHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blind_home)
        val navView: BottomNavigationView = findViewById(R.id.nav_view_blind)

        val navController = findNavController(R.id.nav_host_fragment_blind)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_blind_home, R.id.nav_stories_blind, R.id.nav_donation_blind
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        updateToken()

        if (Helper[CHECK_PERMISSIONS, true]) {
            PermissionsActivity.startForResult(this, false, PERMISSIONS)
        }
    }

    private fun updateToken(){
        val loggedUser = LoggedUser(this)
        FirebaseService.sharedPref = this.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if(token != null){
                FirebaseService.token = token
            }
        }

        FirebaseDatabase.getInstance().getReference("Tokens").child(loggedUser.getUserID()).setValue(Token(FirebaseService.token!!))
    }

    inner class Token(val token:String){
        constructor():this(""){}
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStackImmediate()
        return super.onSupportNavigateUp()
    }
}
