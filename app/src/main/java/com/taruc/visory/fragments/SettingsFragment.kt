package com.taruc.visory.fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.auth.FirebaseAuth

import com.taruc.visory.R
import com.taruc.visory.ui.LandingActivity
import com.taruc.visory.utils.shortToast
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.logout_button.*

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "Settings"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager
            ?.beginTransaction()
            ?.replace(R.id.settings_fragment, MySettingsFragment())
            ?.commit()
    }

    class MySettingsFragment : PreferenceFragmentCompat() {
        private lateinit var auth: FirebaseAuth

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            auth = FirebaseAuth.getInstance()
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            val key = preference?.key

            if(key.equals("logout_button")){
                auth.signOut()
                activity?.onBackPressed()
                activity?.let{
                    val intent = Intent(it, LandingActivity::class.java)
                    it.startActivity(intent)
                return true
                }
            }

            return false
        }
    }
}
