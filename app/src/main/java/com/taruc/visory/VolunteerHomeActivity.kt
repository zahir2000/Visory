package com.taruc.visory

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.quickblox.activities.PermissionsActivity
import com.taruc.visory.quickblox.utils.CHECK_PERMISSIONS
import com.taruc.visory.quickblox.utils.Helper
import com.taruc.visory.quickblox.utils.PERMISSIONS
import com.taruc.visory.quickblox.utils.ViewDialog
import com.taruc.visory.utils.UserCount
import com.taruc.visory.utils.loadUsers
import java.lang.reflect.Executable

class VolunteerHomeActivity : AppCompatActivity() {

    private lateinit var dialog: ViewDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_home)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_volunteer_home, R.id.navigation_stories, R.id.navigation_donation
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        loadUsers(this)

        if (Helper[CHECK_PERMISSIONS, true]) {
            PermissionsActivity.startForResult(this, false, PERMISSIONS)
        }
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

    override fun onDestroy() {
        super.onDestroy()
    }
}
