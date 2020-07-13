package com.taruc.visory.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import com.taruc.visory.R
import com.taruc.visory.utils.LoggedUser

private const val SPLASH_DELAY = 1500

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        fill()
        Handler().postDelayed({
            val intent = Intent(this, LandingActivity::class.java)
            finish()
            startActivity(intent)
            overridePendingTransition(0, 0)
        }, SPLASH_DELAY.toLong())
    }

    private fun fill() {
        val appName = getString(R.string.app_name)
        findViewById<TextView>(R.id.text_splash_app_title).text = appName
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<TextView>(R.id.text_splash_app_version).text = getString(R.string.splash_app_version, versionName)
    }
}
