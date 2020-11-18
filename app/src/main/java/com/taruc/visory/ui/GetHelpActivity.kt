package com.taruc.visory.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.taruc.visory.R
import com.taruc.visory.jalal.NotificationData
import com.taruc.visory.jalal.PushNotification
import com.taruc.visory.jalal.RetrofitInstance
import com.taruc.visory.utils.LocationClass
import com.taruc.visory.utils.LoggedUser
import com.taruc.visory.utils.shortToast
import kotlinx.android.synthetic.main.activity_get_help.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

const val TOPIC = "/topics/blindLocation"

class GetHelpActivity : AppCompatActivity() {

    private lateinit var lastLocation:LocationClass
    private lateinit var locationManager:LocationManager
    private lateinit var latLng: LatLng
    var isPermission:Boolean = false
    private val phoneNum = "999"
    private val TAG = "HelpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_help)

        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object:PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    if(isLocationEnabled()){
                        getLocation()

                        val title = "A visually impaired needs your help!"
                        val message = "A visually impaired has requested for assistance. Open this to see if they are nearby you."

                        PushNotification(
                            NotificationData(title, message),
                            TOPIC
                        ).also {
                            sendNotification(it)
                        }
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    showAlert()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    showAlert()
                }

            })
            .check()


        btnCall.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:$phoneNum")
            startActivity(callIntent)
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)

            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.d(TAG, response.errorBody().toString())
            }
        } catch (e: Exception){
            Log.e(TAG, e.toString())
        }
    }

    override fun onStop() {
        super.onStop()
            val loggedUser = LoggedUser(this)
            val rootRef = FirebaseDatabase.getInstance().getReference("users")
            val uidRef = rootRef.child(String.format("%s", loggedUser.getUserID()))
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    rootRef.child(loggedUser.getUserID())
                        .child("latitude").removeValue()

                    rootRef.child(loggedUser.getUserID())
                        .child("longitude").removeValue()
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            }
        uidRef.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun checkLocation():Boolean{
        if(!isLocationEnabled()){
            showAlert()
        }
        return isLocationEnabled()
    }

    private fun showAlert(){
        val dialog = AlertDialog.Builder(this)

        dialog.setTitle("Location required!")
            .setMessage("Please allow our app to access your location to help you.")
            .setPositiveButton("Settings", dialogClicker)
        dialog.show()
    }

    private val dialogClicker = DialogInterface.OnClickListener{_, which ->
        when(which){
            DialogInterface.BUTTON_POSITIVE -> openSettings()
        }

    }

    private fun openSettings(){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun isLocationEnabled():Boolean{
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc:Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if(!loc){
            return false
        }
        return true
    }

    private fun requestSinglePermission():Boolean{
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object: PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    isPermission = true
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if(response!!.isPermanentlyDenied){
                        isPermission = false
                    }
                }
            }).check()
        return isPermission
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0f, object : LocationListener{
            override fun onLocationChanged(location: Location?) {
                lastLocation = LocationClass(location!!.latitude,location.longitude)

                val loggedUser = LoggedUser(applicationContext)
                val rootRef = FirebaseDatabase.getInstance().getReference("users")
                val uidRef = rootRef.child(String.format("%s", loggedUser.getUserID()))
                val valueEventListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        rootRef.child(loggedUser.getUserID())
                            .child("latitude").setValue(lastLocation.latitude)

                        rootRef.child(loggedUser.getUserID())
                            .child("longitude").setValue(lastLocation.longitude)
                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                }
                uidRef.addListenerForSingleValueEvent(valueEventListener)

                latLng = LatLng(location.latitude, location.longitude)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }

            override fun onProviderEnabled(provider: String?) {  }

            override fun onProviderDisabled(provider: String?) { }
        })
    }

}
