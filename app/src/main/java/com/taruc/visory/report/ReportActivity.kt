package com.taruc.visory.report

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.taruc.visory.R
import com.taruc.visory.quickblox.utils.ViewDialog

class ReportActivity : AppCompatActivity(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_report)

        supportActionBar?.title = "Report a Problem"

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, MySettingsFragment())
            .commit()
    }

    class MySettingsFragment : PreferenceFragmentCompat() {
        private lateinit var loadingDialog: ViewDialog

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.report_preferences, rootKey)
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
            if (key.equals("submit_report")) {
                loadingDialog = ViewDialog(requireContext())
                loadingDialog.showDialogFor5Seconds()

                Handler().postDelayed({
                    activity?.onBackPressed()
                    activity?.finish()
                }, 6000)
                return true
            }

            return false
        }

        override fun onDestroy() {
            super.onDestroy()

            if(this::loadingDialog.isInitialized)
                loadingDialog.hideDialog()
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when(preference?.key){
            "poor_audio_video" -> { }
            "no_audio_video" -> { }
            "unexpected_call_end" -> { }
            "inappropriate_behavior" -> { }
            "no_help" -> { }
            "custom_feedback" -> { }
        }

        return true
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        when(preference?.key){
            "custom_feedback" -> {
                //TODO: Get result from Preferences
            }
        }

        return true
    }
}
