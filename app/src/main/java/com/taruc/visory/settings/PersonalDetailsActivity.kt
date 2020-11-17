package com.taruc.visory.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.taruc.visory.R
import com.taruc.visory.utils.*
import kotlinx.android.synthetic.main.activity_personal_details.*

class PersonalDetailsActivity : AppCompatActivity(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var loggedUser: LoggedUser
    private var selectedPhotoUri: Uri? = null
    private var newAvatarUrl: String = ""
    private var selectedLanguage: String = ""
    private var changedLanguage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_details)

        supportActionBar?.title = "Edit Profile"
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        loggedUser = LoggedUser(this)
        selectedLanguage = loggedUser.getUserLanguage()

        edit_text_first_name.setText(getFirstName(loggedUser.getUserName()))
        edit_text_last_name.setText(getLastName(loggedUser.getUserName()))
        edit_text_pd_email.setText(loggedUser.getUserEmail())

        if (loggedUser.getUserContact() != ""){
            text_edit_pd_contact.setText(loggedUser.getUserContact())
        } else {
            text_edit_pd_contact.setText("")
        }

        if (loggedUser.getUserType() == 2) {
            profile_image_card_view.visibility = View.GONE
        } else {
            if (loggedUser.getAvatarUrl().isNotEmpty() && loggedUser.getAvatarUrl().compareTo("null") != 0) {
                val imageView = findViewById<ImageView>(R.id.image_update_profile_profile)
                Picasso.get().load(loggedUser.getAvatarUrl()).into(imageView)
            }
        }

        val spinner: Spinner = findViewById(R.id.edit_language_spinner)
        spinner.prompt = getString(R.string.select_language_spinner)
        spinner.onItemSelectedListener = this

        ArrayAdapter.createFromResource(
            this,
            R.array.language_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(adapter.getPosition(selectedLanguage))
        }

        button_update_profile.setOnClickListener(this)
        image_update_profile_profile.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_update_profile -> {
                val fName = edit_text_first_name.text.toString()
                val lName = edit_text_last_name.text.toString()
                val contact = text_edit_pd_contact.text.toString()

                if (loggedUser.getUserType() == 1) {
                    if (fName.compareTo(getFirstName(loggedUser.getUserName())) == 0
                        && lName.compareTo(getLastName(loggedUser.getUserName())) == 0
                        && selectedPhotoUri == null && !changedLanguage
                    ) {
                        finish()
                    } else {
                        if (isInternetAvailable(this)){
                            updateProfile(v, fName, lName, contact)
                        } else {
                            makeErrorSnackbar(v, "An active internet connection is required.")
                        }
                    }
                } else {
                    if (fName.compareTo(getFirstName(loggedUser.getUserName())) == 0
                        && lName.compareTo(getLastName(loggedUser.getUserName())) == 0
                        && !changedLanguage
                    ) {
                        finish()
                    } else {
                        if (isInternetAvailable(this)){
                            updateProfile(v, fName, lName, contact)
                        } else {
                            makeErrorSnackbar(v, "An active internet connection is required.")
                        }
                    }
                }
            }

            R.id.image_update_profile_profile -> {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data!!
            val bitmap: Bitmap

            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri!!)
                ImageDecoder.decodeBitmap(source)
            }

            image_update_profile_profile.setImageBitmap(bitmap)
        }
    }

    private fun updateProfile(view: View, fName: String, lName: String, contact: String) {
        if (TextUtils.isEmpty(fName)) {
            makeWarningSnackbar(view, "Please enter your first name")
            return
        }

        if (TextUtils.isEmpty(lName)) {
            makeWarningSnackbar(view, "Please enter your last name")
            return
        }

        val rootRef = FirebaseDatabase.getInstance().getReference("users")
        val uidRef = rootRef.child(String.format("%s", loggedUser.getUserID()))
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                rootRef.child(loggedUser.getUserID())
                    .child("lname").setValue(lName)

                rootRef.child(loggedUser.getUserID())
                    .child("contactNo").setValue(contact)
                loggedUser.setUserContact(contact)

                rootRef.child(loggedUser.getUserID())
                    .child("language").setValue(selectedLanguage)
                loggedUser.setUserLanguage(selectedLanguage)

                if (loggedUser.getUserType() == 1) {
                    uploadImageToFirebaseStorage()
                }

                rootRef.child(loggedUser.getUserID())
                    .child("fname").setValue(fName).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            makeSuccessSnackbar(view, "Profile details updated successfully.")
                            loggedUser.setUserName("$fName $lName")

                            Handler().postDelayed({
                                finish()
                            }, 2000)
                        } else {
                            makeErrorSnackbar(view, "Profile details was not updated.")
                        }
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        uidRef.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null)
            return

        val loggedUser = LoggedUser(this)

        val filename = loggedUser.getUserID()
        val ref = FirebaseStorage.getInstance().getReference("/avatars/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    newAvatarUrl = it.toString()
                    loggedUser.setAvatarUrl(newAvatarUrl)

                    val rootRef = FirebaseDatabase.getInstance().getReference("users")
                    val uidRef = rootRef.child(String.format("%s", loggedUser.getUserID()))
                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            rootRef.child(loggedUser.getUserID())
                                .child("avatarurl").setValue(newAvatarUrl)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    }
                    uidRef.addListenerForSingleValueEvent(valueEventListener)
                }
            }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectedLanguage = "English"
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedLanguage = parent?.getItemAtPosition(position)?.toString() ?: "English"
        changedLanguage = true
    }
}
