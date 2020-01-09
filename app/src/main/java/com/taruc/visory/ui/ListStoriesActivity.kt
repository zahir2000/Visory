package com.taruc.visory.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.taruc.visory.R
import com.taruc.visory.utils.Story
import kotlinx.android.synthetic.main.activity_list_stories.*
import org.jivesoftware.smack.chat.Chat


class ListStoriesActivity : AppCompatActivity() {

    lateinit var recycle_view : RecyclerView
    lateinit var db : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_stories)
        setSupportActionBar(toolbar)

        db = FirebaseDatabase.getInstance().getReference("Stories")
        recycle_view = findViewById(R.id.list_view)
        recycle_view.setHasFixedSize(true)
        recycle_view.layoutManager = LinearLayoutManager(this)

        logRecyclerView()

    }

    private fun logRecyclerView() {
       var FirebaseRecyclerAdapter = object:FirebaseRecyclerAdapter<Story, StoryViewHolder>(
           Story::class.java,
           R.layout.content_list_stories,
           StoryViewHolder::class.java,
           db
       ){
           override fun populateViewHolder(p0: StoryViewHolder?, p1: Story?, p2: Int) {
               p0.itemView.
           }
       }

        recycle_view.adapter = FirebaseRecyclerAdapter
    }

    class StoryViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }

}

