package com.taruc.visory.ui

//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.MarkerOptions
//import com.taruc.visory.R
//import com.google.firebase.database.*
//import com.google.firebase.database.snapshot.ChildKey
//import com.taruc.visory.utils.LoggedUser
//import com.google.android.gms.maps.model.LatLng
//
//class ShowBlindLocationActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private lateinit var mMap: GoogleMap
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_show_blind_location)
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//    }
//
//    /**
//     * Manipulates the map once available.
//     * This callback is triggered when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a marker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user will be prompted to install
//     * it inside the SupportMapFragment. This method will only be triggered once the user has
//     * installed Google Play services and returned to the app.
//     */
//    override fun onMapReady(googleMap: GoogleMap) {
//        val loggedUser = LoggedUser(applicationContext)
//        val rootRef = FirebaseDatabase.getInstance().getReference("users")
//        val uidRef = rootRef.child(String.format("%s", loggedUser.getUserID()))
//        var latLng : LatLng
//
//        mMap = googleMap
//
//        val locationListener = object : ValueEventListener{
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                latLng = dataSnapshot.child("latitude").getValue(Long.javaClass)
//            }
//        }
//
////        override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
////            var newLocation = LatLng
////
////            newLocation = (
////                dataSnapshot.child("latitude").getValue(Long.class),
////                    dataSnapshot.child("longitude").getValue(Long.class)
////                    )
////            mMap.addMarker(new MarkerOptions()
////                .position(newLocation)
////                .title(dataSnapshot.getKey()));
////        }
//    }
//}
