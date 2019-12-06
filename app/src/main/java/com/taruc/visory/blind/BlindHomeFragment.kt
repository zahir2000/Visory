package com.taruc.visory.blind

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth

import com.taruc.visory.R
import com.taruc.visory.ui.LandingActivity
import kotlinx.android.synthetic.main.fragment_blind_home.*

class BlindHomeFragment : Fragment(), View.OnClickListener {

    lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_blind_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Home"

        button_blind_detect_object.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.button_blind_detect_object -> {
                auth.signOut()
                activity?.onBackPressed()
                activity?.let{
                    val intent = Intent(it, LandingActivity::class.java)
                    it.startActivity(intent)
                }
            }
        }
    }
}
