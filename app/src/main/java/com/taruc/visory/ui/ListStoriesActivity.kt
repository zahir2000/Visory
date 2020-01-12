package com.taruc.visory.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.taruc.visory.R
import com.taruc.visory.utils.Story

class ListStoriesActivity : AppCompatActivity() {

    lateinit var sRecyclerView: RecyclerView
    lateinit var ref : DatabaseReference
    lateinit var showProgress : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_stories)

        sRecyclerView = findViewById(R.id.sRecyclerView)

        ref = FirebaseDatabase.getInstance().getReference().child("stories")

        sRecyclerView.layoutManager = LinearLayoutManager(this)

        showProgress = findViewById(R.id.sProgressBar)

        val option = FirebaseRecyclerOptions.Builder<Story>()
            .setQuery(ref, Story::class.java)
            .build()


        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Story, StoryViewHolder>(option){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
                val itemview = LayoutInflater.from(this@ListStoriesActivity).inflate(R.layout.cardview, parent, false)
                return StoryViewHolder(itemview)
            }

            override fun onBindViewHolder(holder: StoryViewHolder, position: Int, story: Story) {
                val refID = getRef(position).key.toString()
                ref.child(refID).addValueEventListener(object:ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        showProgress.visibility = if(itemCount == 0) View.VISIBLE else View.GONE

                        holder.sTitle.text = story.sTitle
                        Picasso.get().load(story.imageUrl).into(holder.sCoverImage)

                        holder.itemView.setOnClickListener {
                            val intent = Intent(this@ListStoriesActivity, StoryDetailsActivity::class.java)
                            intent.putExtra("img", story.imageUrl)
                            intent.putExtra("title",story.sTitle)
                            intent.putExtra("story",story.sBody)
                            intent.putExtra("date", story.sDate)
                            startActivity(intent)
                        }
                    }

                })
            }

        }

        sRecyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    class StoryViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){

        var sTitle : TextView = itemView!!.findViewById(R.id.txtStoryTitle)
        var sCoverImage : ImageView = itemView!!.findViewById(R.id.imgStoryCover)
    }
}
