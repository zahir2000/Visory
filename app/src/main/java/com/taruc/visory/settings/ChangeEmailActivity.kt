package com.taruc.visory.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.taruc.visory.R
import kotlinx.android.synthetic.main.activity_change_email.*

class ChangeEmailActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        supportActionBar?.title = "Edit Email"

        button_change_email.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_change_email -> {
                finish()
            }
        }
    }
}
