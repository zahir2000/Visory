package com.taruc.visory.mlkit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.taruc.visory.R
import com.taruc.visory.quickblox.utils.MLKIT_IMAGE_LABELING
import com.taruc.visory.quickblox.utils.MLKIT_TEXT_DETECTION
import kotlinx.android.synthetic.main.activity_mlkit_home.*

class MLKitHomeActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mlkit_home)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        button_mlkit_general.setOnClickListener(this)
        button_mlkit_text.setOnClickListener(this)
        button_mlkit_label.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id){
            R.id.button_mlkit_general -> {
                val intent = Intent(this, ObjectDetectorActivity::class.java)
                startActivity(intent)
            }

            R.id.button_mlkit_text -> {
                val intent = Intent(this, MLKitDetectionActivity::class.java)
                intent.putExtra("type", MLKIT_TEXT_DETECTION)
                startActivity(intent)
            }

            R.id.button_mlkit_label -> {
                val intent = Intent(this, MLKitDetectionActivity::class.java)
                intent.putExtra("type", MLKIT_IMAGE_LABELING)
                startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}