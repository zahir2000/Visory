package com.taruc.visory.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.taruc.visory.R
import kotlinx.android.synthetic.main.button_over_image.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.hide()

        button_close_about.setOnClickListener{
            finish()
        }
    }
}
