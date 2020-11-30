package com.taruc.visory.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
import kotlinx.android.synthetic.main.activity_get_help.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TOPIC = "/topics/blindLocation"

class GetHelpActivity : AppCompatActivity() {

    private lateinit var lastLocation: LocationClass
    private lateinit var locationManager: LocationManager
    private lateinit var latLng: LatLng

    //var isPermission: Boolean = false
    private val phoneNum = "000"
    private val TAG = "HelpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_help)

        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    if (isLocationEnabled()) {
                        getLocation()

                        val loggedUser = LoggedUser(applicationContext)

                        val title = "${loggedUser.getUserName()} needs your help."
                        val message =
                            "A visually impaired has requested for assistance. Open this to see if they are nearby you."

                        PushNotification(
                            NotificationData(title, message),
                            TOPIC
                        ).also {
                            sendNotification(it)
                        }
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    showLocAlert()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    showLocAlert()
                }

            })
            .check()


        btnCall.setOnClickListener {
//            val callIntent = Intent(Intent.ACTION_DIAL)
//            callIntent.data = Uri.parse("tel:$phoneNum")
//            startActivity(callIntent)
            callEmergency()
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)

                if (response.isSuccessful) {
                    Log.d(TAG, "Response: ${Gson().toJson(response)}")
                } else {
                    Log.d(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }

    private fun callEmergency() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CALL_PHONE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    val callIntent = Intent(Intent.ACTION_CALL)
                    callIntent.data = Uri.parse("tel:$phoneNum")
                    startActivity(callIntent)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    showCallError()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    showCallError()
                }

            })
            .check()
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

//    private fun checkLocation():Boolean{
//        if(!isLocationEnabled()){
//            showLocAlert()
//        }
//        return isLocationEnabled()
//    }

    private fun showLocAlert() {
        val dialog = AlertDialog.Builder(this)

        dialog.setTitle("Location required!")
            .setMessage("Please allow our app to access your location to help you.")
            .setPositiveButton("Settings", dialogClicker)
        dialog.show()
    }

    private fun showCallError() {
        val dialog = AlertDialog.Builder(this)

        dialog.setTitle("Call Permission Required!")
            .setMessage("Please allow our app to place calls.")
            .setPositiveButton("Settings", callDialogClicker)
        dialog.show()
    }

    private val dialogClicker = DialogInterface.OnClickListener { _, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> openLocSettings()
        }

    }

    private val callDialogClicker = DialogInterface.OnClickListener { _, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> openCallPermission()
        }

    }

    private fun openLocSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun openCallPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:"+packageName)
        startActivity(intent)
    }

    private fun isLocationEnabled(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!loc) {
            return false
        }
        return true
    }

//    private fun requestSinglePermission():Boolean{
//        Dexter.withActivity(this)
//            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//            .withListener(object : PermissionListener {
//                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
//                    isPermission = true
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    permission: PermissionRequest?,
//                    token: PermissionToken?
//                ) {
//                }
//
//                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
//                    if (response!!.isPermanentlyDenied) {
//                        isPermission = false
//                    }
//                }
//            }).check()
//        return isPermission
//    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            10000,
            0f,
            object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    lastLocation = LocationClass(location!!.latitude, location.longitude)

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

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                override fun onProviderEnabled(provider: String?) {}

                override fun onProviderDisabled(provider: String?) {}
            })
    }

}
