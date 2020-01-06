package com.taruc.visory.blind

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.taruc.visory.R
import com.taruc.visory.report.ReportActivity
import kotlinx.android.synthetic.main.activity_call_end.*

class CallEndActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_end)

        button_bvi_done.setOnClickListener(this)
        button_bvi_report.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_bvi_done -> {
                finish()
            }

            R.id.button_bvi_report -> {
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
