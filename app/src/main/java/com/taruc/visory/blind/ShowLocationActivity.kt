package com.taruc.visory.blind

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.taruc.visory.R
import com.taruc.visory.utils.LocationClass
import com.taruc.visory.utils.LoggedUser
import com.taruc.visory.utils.shortToast


class ShowLocationActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var mChildEventList:ChildEventListener
    private lateinit var marker:Marker
    private lateinit var rootRef:DatabaseReference
    private lateinit var uidRef:DatabaseReference
    private lateinit var location:LocationClass
    private lateinit var latlng:LatLng
    private lateinit var builder:LatLngBounds.Builder

    private lateinit var lastLocation:LocationClass
    private lateinit var locationManager:LocationManager
    private lateinit var myLoc: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    if(isLocationEnabled()){
                        getLocation()
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

    private fun showAlert(){
        val dialog = AlertDialog.Builder(this)

        dialog.setTitle("Location required!")
            .setMessage("Please allow our app to access your location to help you.")
            .setPositiveButton("Settings", dialogClicker)
        dialog.show()
    }

    private val dialogClicker = DialogInterface.OnClickListener{ _, which ->
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

                myLoc = LatLng(location.latitude, location.longitude)
                val cu: CameraUpdate = CameraUpdateFactory.newLatLngZoom(myLoc, 10F)
                mMap.moveCamera(cu)
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }

            override fun onProviderEnabled(provider: String?) {  }

            override fun onProviderDisabled(provider: String?) { }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        rootRef = FirebaseDatabase.getInstance().getReference("users")

        googleMap.setOnMarkerClickListener(this)
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val eventListener = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot : DataSnapshot in dataSnapshot.children){
                    val userRole = snapshot.child("role").value.toString().toInt()
                    if(userRole == 2){
                        val latitude = snapshot.child("latitude").value.toString()
                        val longitude = snapshot.child("longitude").value.toString()
                        val name = snapshot.child("fname").value.toString()

                        if(latitude.compareTo("null")!=0){
                            val loc = LocationClass(snapshot.child("latitude").value.toString().toDouble(),snapshot.child("longitude").value.toString().toDouble())
                            location = (loc)
                            latlng = LatLng(location.latitude, location.longitude)
                            mMap.addMarker(MarkerOptions().position(latlng).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))


                        }
                    }
                }
            }

        }
        rootRef.addListenerForSingleValueEvent(eventListener)
    }

    override fun onLocationChanged(location: Location?) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String?) {}

    override fun onProviderDisabled(provider: String?) {}

    override fun onMarkerClick(p0: Marker?)=false
}
