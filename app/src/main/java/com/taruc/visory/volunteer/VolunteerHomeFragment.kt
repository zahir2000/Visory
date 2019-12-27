package com.taruc.visory.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.taruc.visory.R
import com.taruc.visory.utils.LoggedUser
import kotlinx.android.synthetic.main.profile_card.*


class VolunteerHomeFragment : Fragment() {

    lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_volunteer_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar?.title = "Home"

        updateUI()
    }

    private fun updateUI() {
        val loggedUserTypePref = LoggedUser(this.activity!!.baseContext)
        profile_joindate.text = "Member since " + loggedUserTypePref.getUserJoinDate()
        profile_name.text = loggedUserTypePref.getUserName()
        //profile_language.text = loggedUserTypePref.getLanguage()
    }
}
