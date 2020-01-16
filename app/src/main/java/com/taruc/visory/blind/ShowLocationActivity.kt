package com.taruc.visory.blind

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.taruc.visory.R
import com.taruc.visory.utils.LocationClass
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
                            shortToast(latitude)
                            mMap.addMarker(MarkerOptions().position(latlng).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

                            builder = LatLngBounds.Builder()
                            builder.include(latlng)
                            val bounds = builder.build()
                            val cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,0)
                            mMap.moveCamera(cu)
                        }
                    }
                }
            }

        }
        rootRef.addListenerForSingleValueEvent(eventListener)
    }

    override fun onLocationChanged(location: Location?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMarkerClick(p0: Marker?)=false
}
