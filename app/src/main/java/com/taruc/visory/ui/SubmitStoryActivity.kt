package com.taruc.visory.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.taruc.visory.R
import com.taruc.visory.utils.LoggedUser
import com.taruc.visory.utils.Story
import com.taruc.visory.utils.getCurrentDate
import com.taruc.visory.utils.shortToast
import kotlinx.android.synthetic.main.activity_submit_story.*
import java.util.*

class SubmitStoryActivity : AppCompatActivity() {

    lateinit var editTextTitle: EditText
    lateinit var editTextStory: EditText
    lateinit var btnSubmit: Button
    lateinit var textDate: TextView
    lateinit var user : LoggedUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_story)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextStory = findViewById(R.id.editTextStory)
        btnSubmit = findViewById(R.id.btnSubmit)
        textDate = findViewById(R.id.textDate)
        textDate.text = getCurrentDate()

        btnSelectPhoto.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }
        btnSubmit.setOnClickListener {
            uploadImageToFirebaseStorage()
        }

        val contentView: View = findViewById(R.id.submit_story)
        contentView.setOnClickListener {
            it.hideKeyboard()
        }

    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            imgStoryCover.setImageBitmap(bitmap)
            btnSelectPhoto.alpha = 0f
        }
    }

    private fun saveStory(storyCoverUrl:String){
        val title = editTextTitle.text.toString().trim()
        val story = editTextStory.text.toString()
        val date = getCurrentDate()
        val status = "pending"
        user = LoggedUser(this)
        val userName = user.getUserName()
        val email = user.getUserEmail()

        val uid = FirebaseDatabase.getInstance().getReference("stories").push().key
        val ref = FirebaseDatabase.getInstance().getReference("/stories/$uid")

        val storyObj = Story(uid, storyCoverUrl, title, story, date, status, userName, email)
        if(title.isEmpty() || story.isEmpty()){
            editTextTitle.error = "Please enter a title for the story!"
           return
        }

        ref.setValue(storyObj).addOnCompleteListener{task ->
            if (task.isSuccessful){
                shortToast("Story has been shared!")
            }
        }
    }

    private fun uploadImageToFirebaseStorage(){

        if(selectedPhotoUri==null)return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveStory(it.toString())
                }
            }
    }

    private fun View.hideKeyboard() {
        val inputMethodManager = context!!.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
    }
}
