package com.taruc.visory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        (activity as AppCompatActivity).supportActionBar?.title = "Register"

        button_register_submit.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.button_register_submit -> {
                // TODO : necessary checking required
                navController.navigate(R.id.action_registerFragment_to_verifyFragment)
            }
        }
    }
}
