package com.taruc.visory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : Fragment(), View.OnClickListener {

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Welcome"

        //button_welcome_submit.setOnClickListener(this)
        button_welcome_submit.setOnClickListener{
            activity?.let{
                val intent = Intent (it, VolunteerHomeActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.button_welcome_submit -> {
                // TODO: send to appropriate home screens
                navController.navigate(R.id.action_welcomeFragment_to_volunteerHomeFragment)
            }
        }
    }

    // TODO: Disable the back button
}
