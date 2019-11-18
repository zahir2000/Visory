package com.taruc.visory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_landing.*

class LandingFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO : make a introduction text list and shuffle during each visit
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        buttonBlind.setOnClickListener(this)
        buttonVolunteer.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.buttonVolunteer -> {
                val bundle = bundleOf("clicked_button" to 1)
                navController.navigate(R.id.action_landingFragment_to_landingActionsFragment, bundle)
            }
            R.id.buttonBlind -> {
                val bundle = bundleOf("clicked_button" to 2)
                navController.navigate(R.id.action_landingFragment_to_landingActionsFragment, bundle)
            }
        }
    }
}
