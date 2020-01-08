package com.taruc.visory

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.taruc.visory.ui.SubmitStoryActivity
import kotlinx.android.synthetic.main.fragment_stories.*

class StoriesFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button2.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button2 -> {
                activity?.let{
                    val intent = Intent (it, SubmitStoryActivity::class.java)
                    it.startActivity(intent)
                }
            }
        }
    }
}
