package com.taruc.visory.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.taruc.visory.R
import kotlinx.android.synthetic.main.activity_personal_details.*

class PersonalDetailsActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_details)

        supportActionBar?.title = "Edit Profile"
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)

        button_update_profile.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_update_profile -> {
                finish()
            }
        }
    }
}
