package com.taruc.visory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
import java.lang.Exception

class BlindHomeActivity : AppCompatActivity() {

    lateinit var viewDialog: ViewDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blind_home)
        val navView: BottomNavigationView = findViewById(R.id.nav_view_blind)

        val navController = findNavController(R.id.nav_host_fragment_blind)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_blind_home, R.id.nav_stories_blind
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        try{
            viewDialog = ViewDialog(this)
            viewDialog.showDialogFor5Seconds()
        }catch (e: Exception){}

        if(Helper[CHECK_PERMISSIONS, true]){
            PermissionsActivity.startForResult(this, false, PERMISSIONS)
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
