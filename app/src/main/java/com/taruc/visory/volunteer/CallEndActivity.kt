package com.taruc.visory.volunteer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.taruc.visory.R
import kotlinx.android.synthetic.main.activity_volunteer_call_end.*

class CallEndActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_call_end)

        button_volunteer_done.setOnClickListener(this)
        button_volunteer_report.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_volunteer_done -> {
                finish()
            }

            R.id.button_volunteer_report -> {

            }
        }
    }
}
