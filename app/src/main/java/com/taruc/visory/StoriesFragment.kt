package com.taruc.visory

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.taruc.visory.ui.SubmitStoryActivity
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.taruc.visory.ui.StoryDetailsActivity
import com.taruc.visory.utils.Story

class StoriesFragment : Fragment(), View.OnClickListener {

    lateinit var sRecyclerView: RecyclerView
    lateinit var ref : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.story_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.share_story -> {
                val intent = Intent(context, SubmitStoryActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stories, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sRecyclerView = view.findViewById(R.id.sRecyclerView)
        ref = FirebaseDatabase.getInstance().reference.child("stories")
        sRecyclerView.layoutManager = LinearLayoutManager(context)


        val option = FirebaseRecyclerOptions.Builder<Story>()
            .setQuery(ref, Story::class.java)
            .build()

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Story, StoriesFragment.StoryViewHolder>(option) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): StoriesFragment.StoryViewHolder {
                val itemview =
                    LayoutInflater.from(context).inflate(R.layout.cardview, parent, false)
                return StoriesFragment.StoryViewHolder(itemview)
            }

            override fun onBindViewHolder(
                holder: StoriesFragment.StoryViewHolder,
                position: Int,
                story: Story
            ) {
                val refID = getRef(position).key.toString()
                ref.child(refID).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        holder.sTitle.text = story.sTitle
                        Picasso.get().load(story.imageUrl).into(holder.sCoverImage)

                        holder.itemView.setOnClickListener {
                            val intent = Intent(context, StoryDetailsActivity::class.java)
                            intent.putExtra("img", story.imageUrl)
                            intent.putExtra("title", story.sTitle)
                            intent.putExtra("story", story.sBody)
                            intent.putExtra("date", story.sDate)
                            startActivity(intent)
                        }
                    }

                })
            }
        }

        sRecyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onClick(v: View?) {

    }

    class StoryViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){

        var sTitle : TextView = itemView.findViewById(R.id.txtStoryTitle)
        var sCoverImage : ImageView = itemView.findViewById(R.id.imgCover)
    }
}
