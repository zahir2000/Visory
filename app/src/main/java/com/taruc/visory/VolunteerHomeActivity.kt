package com.taruc.visory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.taruc.visory.quickblox.activities.PermissionsActivity
import com.taruc.visory.quickblox.utils.CHECK_PERMISSIONS
import com.taruc.visory.quickblox.utils.Helper
import com.taruc.visory.quickblox.utils.PERMISSIONS
import com.taruc.visory.quickblox.utils.ViewDialog

class VolunteerHomeActivity : AppCompatActivity() {

    lateinit var dialog: ViewDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_home)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_volunteer_home, R.id.navigation_stories
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        dialog = ViewDialog(this)
        dialog.showDialog()

        val handler = Handler()
        handler.postDelayed({
            dialog.hideDialog()
        }, 5000)

        if(Helper[CHECK_PERMISSIONS, true]){
            PermissionsActivity.startForResult(this, false, PERMISSIONS)
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
