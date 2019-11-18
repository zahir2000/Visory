package com.taruc.visory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_landing_actions.*
import java.lang.Exception

class LandingActionsFragment : Fragment() {

    private lateinit var navController: NavController
    private var clickedButton: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clickedButton = arguments!!.getInt("clicked_button")

        // TODO : back button
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
            imageLandingActions.setImageResource(R.drawable.ic_volunteer_intro)
            headerLandingActions.setText(R.string.welcome_volunteer_header)
            textLandingActions.setText(R.string.welcome_volunteer_text)
        }else{
            imageLandingActions.setImageResource(R.drawable.ic_blind_intro)
            headerLandingActions.setText(R.string.welcome_blind_header)
            textLandingActions.setText(R.string.welcome_blind_text)
        }
    }


}
