package com.taruc.visory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_landing_actions.*

class LandingActionsFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private var clickedButton: Int = 0
    private var userType: String = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clickedButton = arguments!!.getInt("clicked_button")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        if(clickedButton == 1){
            userType = "Volunteer"
            (activity as AppCompatActivity).supportActionBar?.title = "Volunteer"
            imageLandingActions.setImageResource(R.drawable.ic_volunteer_intro)
            headerLandingActions.setText(R.string.welcome_volunteer_header)
            textLandingActions.setText(R.string.welcome_volunteer_text)
        }else{
            userType = "Visually Impaired"
            (activity as AppCompatActivity).supportActionBar?.title = "Visually Impaired"
            imageLandingActions.setImageResource(R.drawable.ic_blind_intro)
            headerLandingActions.setText(R.string.welcome_blind_header)
            textLandingActions.setText(R.string.welcome_blind_text)
        }

        button_register_submit.setOnClickListener(this)
        button_login.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val bundle = bundleOf("userType" to userType)
        when(view.id){
            R.id.button_register_submit -> {
                navController.navigate(R.id.action_landingActionsFragment_to_registerFragment, bundle)
            }
            R.id.button_login -> {
                navController.navigate(R.id.action_landingActionsFragment_to_loginFragment, bundle)
            }
        }
    }
}
