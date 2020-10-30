package com.taruc.visory.report

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.taruc.visory.R
import com.taruc.visory.quickblox.utils.ViewDialog
import com.taruc.visory.utils.*

lateinit var callHistory: CallHistory
lateinit var loggedUser: LoggedUser

class ReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_report)

        supportActionBar?.title = "Report a Problem"

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, MySettingsFragment())
            .commit()

        loggedUser = LoggedUser(baseContext)

        if(loggedUser.getUserType() == 1){
            getLatestCallHistory(loggedUser.getUserID())
        }

        Log.d("userId", loggedUser.getUserID())
    }

    private fun getLatestCallHistory(userId: String) {
        val rootRef = FirebaseDatabase.getInstance().getReference("callhistory").orderByChild("calleeId").equalTo(userId).limitToLast(1)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("snapshotResult", snapshot.value.toString())

                for (values in snapshot.children){
                    Log.d("callId", values.key.toString())
                    Log.d("databaseItem", values.child("callDateTime").value.toString())
                    Log.d("databaseItem", values.child("callTime").value.toString())
                    Log.d("databaseItem", values.child("calleeId").value.toString())
                    Log.d("databaseItem", values.child("callerId").value.toString())

                    callHistory = CallHistory(baseContext)
                    callHistory.setCallId(values.key.toString())
                    callHistory.setCallDate(values.child("callDateTime").value.toString())
                    callHistory.setCallTime(values.child("callTime").value.toString())
                    callHistory.setCalleeId(userId)
                    callHistory.setCallerId(values.child("callerId").value.toString())

                    Log.d("callHistoryVolunteer", callHistory.toString())
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        rootRef.addListenerForSingleValueEvent(valueEventListener)
    }

    class MySettingsFragment : PreferenceFragmentCompat() {
        private val keysList = arrayOf("poor_audio_video", "no_audio_video", "unexpected_call_end", "inappropriate_behavior", "no_help", "custom_feedback")
        private lateinit var loadingDialog: ViewDialog

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.report_preferences, rootKey)
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            resetFeedbackOptions()
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            (activity as AppCompatActivity).actionBar?.title = "Report a Problem"
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
        }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.report_menu, menu)
            super.onCreateOptionsMenu(menu, inflater)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.skip_report -> {
                    activity?.onBackPressed()
                    activity?.finish()
                }
            }

            return super.onOptionsItemSelected(item)
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            val key = preference?.key
            val selectedOpts = Array(6){""}
            var index = 0
            callHistory = CallHistory(requireContext())

            val userId = if (loggedUser.getUserType() == 1) callHistory.getCalleeId() else callHistory.getCallerId()
            val feedback = Feedback(getCurrentFormattedDateTime(), "", userId, callHistory.getCallId())

            if (key.equals("submit_report")) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                var feedbackCounter = 0
                for (keys in prefs.all.entries){
                    if(keysList.contains(keys.key)){
                        if(keys.value is Boolean && keys.value == true){
                            Log.d("feedbackList", "Key is " + keys.key + " and value is " + keys.value)

                            prefs.edit().putBoolean(keys.key, false).apply()
                            selectedOpts[index++] = keys.key
                            feedbackCounter++
                        } else if (keys.value is String && keys.value.toString().isNotEmpty()){
                            Log.d("feedbackList", "Key is " + keys.key + " and value is " + keys.value)

                            feedback.customFeedback = keys.value.toString()
                            prefs.edit().putString(keys.key, "").apply()
                            feedbackCounter++
                        }
                    }
                }

                /*
                val feedbackOptions = FeedbackOptions("No help was provided")
                val rootRef = FirebaseDatabase.getInstance().reference.child("FeedbackOptions").child("no_help")
                rootRef.setValue(feedbackOptions)
                */

                if(feedbackCounter != 0){
                    view?.let { makeSuccessSnackbar(it, "Feedback has been successfully submitted.") }
                    loadingDialog = ViewDialog(requireContext())
                    loadingDialog.showDialogFor5Seconds()

                    Log.d("feedbackClass", feedback.toString())

                    val uid = FirebaseDatabase.getInstance().getReference("Feedback").push().key
                    val rootRef = FirebaseDatabase.getInstance().getReference("Feedback/$uid")
                    rootRef.setValue(feedback)

                    if(selectedOpts.isNotEmpty()){
                        for(opts in selectedOpts){
                            val subRef = rootRef.child("SelectedFeedbackOpt").child(opts)
                            Log.d("opts", opts)

                            when(opts){
                                "no_help" -> {
                                    subRef.setValue("No help was provided")
                                }
                                "poor_audio_video" -> {
                                    subRef.setValue("Poor audio or video quality")
                                }
                                "no_audio_video" -> {
                                    subRef.setValue("No audio or video")
                                }
                                "unexpected_call_end" -> {
                                    subRef.setValue("Call ended unexpectedly")
                                }
                                "inappropriate_behavior" -> {
                                    subRef.setValue("Inappropriate user behavior")
                                }
                            }
                        }
                    }

                    Handler().postDelayed({
                        activity?.onBackPressed()
                        activity?.finish()
                    }, 6000)
                    return true
                } else {
                    view?.let { makeErrorSnackbar(it, "Please select at least on option, or enter a custom feedback.") }
                }
            }
            return false
        }

        override fun onDestroy() {
            super.onDestroy()

            if(this::loadingDialog.isInitialized)
                loadingDialog.hideDialog()
        }

        private fun resetFeedbackOptions(){
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            for(keys in prefs.all.entries){
                if(keysList.contains(keys.key)){
                    if(keys.value is Boolean && keys.value == true){
                        prefs.edit().putBoolean(keys.key, false).apply()
                    } else if (keys.value is String && keys.value.toString().isNotEmpty()){
                        prefs.edit().putString(keys.key, "").apply()
                    }
                }
            }
        }
    }
}
