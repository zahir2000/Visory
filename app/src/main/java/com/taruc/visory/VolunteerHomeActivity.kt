package com.taruc.visory

import android.os.Bundle
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

class VolunteerHomeActivity : AppCompatActivity() {

    lateinit var dialog: ViewDialog
    lateinit var userCount: UserCount

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

        //loadUserCount()

        loadUsers(this)

        try {
            dialog = ViewDialog(this)
            dialog.showDialogFor5Seconds()
        } catch (e: Exception) {
        }

        if (Helper[CHECK_PERMISSIONS, true]) {
            PermissionsActivity.startForResult(this, false, PERMISSIONS)
        }
    }

    private fun loadUserCount() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val userRef = rootRef.child("users")
        userCount = UserCount(this)
        userCount.setUserCount(0, 0)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val role = ds.child("role").value
                    val roleVal = role.toString().toInt()
                    if (roleVal == 1) {
                        userCount.setVolCount(userCount.getVolCount() + 1)
                    } else if (roleVal == 2) {
                        userCount.setBviCount(userCount.getBviCount() + 1)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        userRef.addListenerForSingleValueEvent(valueEventListener)
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
        dialog.hideDialog()
    }
}
