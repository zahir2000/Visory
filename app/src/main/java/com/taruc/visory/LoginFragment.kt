package com.taruc.visory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private var userType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userType = arguments!!.getInt("user_type")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Login"
        forgot_password_button.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.forgot_password_button -> {
                navController.navigate(R.id.action_loginFragment_to_forgotPassFragment)
            }
            R.id.login_button_submit -> {
                when(userType){
                    1 -> {
                        //TODO : when user is volunteer
                    }
                    2 -> {
                        //TODO : when user is blind
                    }
                }
            }
            R.id.button_facebook -> {
                //TODO
            }
            R.id.button_google -> {
                //TODO
            }
        }
    }
}
