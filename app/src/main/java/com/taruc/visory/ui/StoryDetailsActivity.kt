package com.taruc.visory.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Picasso
import com.taruc.visory.R
import kotlinx.android.synthetic.main.activity_story_details.*

class StoryDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_details)

        val bundle: Bundle? = intent.extras
        val img = bundle!!.getString("img")
        val title = bundle!!.getString("title")
        val story = bundle!!.getString("story")
        val date = bundle!!.getString("date")

        txtDate.text = date
        txtTitle.text = title
        txtStory.text = story
        Picasso.get().load(img).into(imgCover)

    }
}
