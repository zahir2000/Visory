package com.taruc.visory.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.taruc.visory.R


class VolunteerHomeFragment : Fragment() {

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_volunteer_home, container, false)
        //val bottomNav = v.findViewById<View>(R.id.bottom_nav) as BottomNavigationView
        //bottomNav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Home"
    }

    /*private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when(menuItem.itemId){
            R.id.bottom_nav_stories -> {
                navController.navigate(R.id.action_volunteerHomeFragment_to_storiesFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }*/

    //TODO: Remove back button. There shouldn't be any way to return to previous screens. Except by Log Out

}
