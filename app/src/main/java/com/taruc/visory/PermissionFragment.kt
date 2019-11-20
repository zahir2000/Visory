package com.taruc.visory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_permission.*

class PermissionFragment : Fragment(), View.OnClickListener {

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Allow Access"

        button_give_access.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.button_give_access -> {
                navController.navigate(R.id.action_permissionFragment_to_welcomeFragment)
            }
        }

        // TODO: when user is new, display Subscribe screen. If we decide to have subscribe that is.
    }

    // TODO: Disable the back button
}
