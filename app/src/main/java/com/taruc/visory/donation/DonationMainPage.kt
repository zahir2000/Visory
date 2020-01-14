package com.taruc.visory.donation

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.R
import kotlinx.android.synthetic.main.fragment_donation_main_page.*


class DonationMainPage : Fragment(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnDonate -> {
                activity?.let {
                    val intent = Intent(it, paypalmain::class.java)
                    it.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_donation_main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Donation"
        btnDonate.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.donation_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.donateHistory -> {
                activity?.let {
                    val intent = Intent(it, donateHistory::class.java)
                    it.startActivity(intent)
                    it.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    var amount: Int = 0
    override fun onStart() {
        super.onStart()

        readTotalOfDonation()
    }

    override fun onPause() {
        super.onPause()

        readTotalOfDonation()
    }

    override fun onStop() {
        super.onStop()

        readTotalOfDonation()
    }

    private fun readTotalOfDonation() {

//        val mainHandler = Handler(Looper.getMainLooper())
//
//        mainHandler.post(object : Runnable {
//            override fun run() {

        val rootRef = FirebaseDatabase.getInstance().getReference("DonateDatabase")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot: DataSnapshot in dataSnapshot.children) {
                    val dbAmount = snapshot.child("amount").value.toString().toInt()
                    amount += dbAmount
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        rootRef.addListenerForSingleValueEvent(valueEventListener)
        lblTotalDonate.text = "People have donated RM $amount"
        amount = 0

//                mainHandler.postDelayed(this, 5000)
//            }
//        })
    }
}