package com.taruc.visory.donation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.taruc.visory.MainActivity
import com.taruc.visory.R
import kotlinx.android.synthetic.main.fragment_donation_main_page.*


class DonationMainPage : Fragment(), View.OnClickListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.btnDonate -> {
                activity?.let{
                    val intent = Intent (it, DonationPayment::class.java)
                    it.startActivity(intent)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.taruc.visory.R.layout.fragment_donation_main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Donation"
        btnDonate.setOnClickListener(this)
    }
}
