package com.taruc.visory.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.taruc.visory.R

class FeedbackDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_details)

        supportActionBar?.title = "Feedback Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}