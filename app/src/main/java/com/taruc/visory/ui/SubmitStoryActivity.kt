package com.taruc.visory.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.taruc.visory.R
import com.taruc.visory.utils.Story
import com.taruc.visory.utils.getCurrentDate
import com.taruc.visory.utils.shortToast
import kotlinx.android.synthetic.main.activity_submit_story.*

class SubmitStoryActivity : AppCompatActivity() {

    lateinit var editTextTitle: EditText
    lateinit var editTextStory: EditText
    lateinit var btnSubmit: Button
    lateinit var textDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_story)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextStory = findViewById(R.id.editTextStory)
        btnSubmit = findViewById(R.id.btnSubmit)
        textDate = findViewById(R.id.textDate)
        textDate.text = getCurrentDate()

        btnSubmit.setOnClickListener {
            saveStory()
        }

    }

    private fun saveStory(){
        val title = editTextTitle.text.toString().trim()
        val story = editTextStory.text.toString()

        if(title.isEmpty()){
            editTextTitle.error = "Please enter a title for the story!"
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("stories")
        val id= ref.push().key
        val date = getCurrentDate()
        val storyObj = Story(id, title, story, date)

        ref.child(id!!).setValue(storyObj).addOnCompleteListener{task ->
            if (task.isSuccessful){
                shortToast("Story has been created!")
            }
        }
    }
}
