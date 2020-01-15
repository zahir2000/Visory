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
import com.taruc.visory.utils.LoggedUser
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
    var ownAmount: Int = 0
    override fun onStart() {
        super.onStart()

        readTotalOfDonation()
    }

    private fun readTotalOfDonation() {
        val rootRef = FirebaseDatabase.getInstance().getReference("DonateDatabase")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                loadingReadProgressBar.visibility = View.GONE
                for (snapshot: DataSnapshot in dataSnapshot.children) {
                    val dbAmount = snapshot.child("amount").value.toString().toInt()
                    amount += dbAmount
                    lblTotalDonate.text = "People have donated RM $amount"

                    if(amount == 0){
                        amount = 0
                        lblTotalDonate.text = "People have donated RM $amount"
                    }

                    val dbEmail = snapshot.child("email").value.toString()
                    val user = LoggedUser(requireContext())
                    if (dbEmail == user.getUserEmail()) {
                        val dbAmount2 = snapshot.child("amount").value.toString().toInt()
                        ownAmount += dbAmount2
                        lblTotalDonate2.text = "You have donated RM $ownAmount"
                    }

                    if(ownAmount == 0){
                        ownAmount = 0
                        lblTotalDonate2.text = "You have donated RM $ownAmount"
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        rootRef.addListenerForSingleValueEvent(valueEventListener)
        amount = 0
        ownAmount = 0
    }
}