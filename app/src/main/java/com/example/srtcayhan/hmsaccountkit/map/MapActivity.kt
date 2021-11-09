package com.example.srtcayhan.hmsaccountkit.map

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.LatLng

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    // HUAWEI map
    private var hMap: HuaweiMap? = null

    private var mMapView: MapView? = null

    companion object {
        private const val TAG = "MapViewDemoActivity"
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate:")
        super.onCreate(savedInstanceState)
        MapsInitializer.setApiKey("CwEAAAAAJo5BV02u3Cu2mxr18WIZKXpQEUF7jSI1oLZOHqvSlmKg1Mg0k8we0IiGORkM0hrIhBauG2vMz6Hw25PNoETSsNzqIYA=")
        setContentView(R.layout.activity_map)
        mMapView = findViewById(R.id.mapView)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle =
                savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView?.apply {
            onCreate(mapViewBundle)
            getMapAsync(this@MapActivity)
        }
        hasPermissions()
    }

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE])
    override fun onMapReady(map: HuaweiMap) {
        hMap = map
        // Enable the my-location layer.
        hMap!!.isMyLocationEnabled = true
        // Enable the my-location icon.
        hMap!!.uiSettings.isMyLocationButtonEnabled = true
    }

    private fun hasPermissions(): Boolean {
        // If the API level is 23 or higher (Android 6.0 or later), you need to dynamically apply for permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "sdk >= 23 M")
            // Check whether your app has the specified permission and whether the app operation corresponding to the permission is allowed.
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request permissions for your app.
                val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                // Request permissions.
                ActivityCompat.requestPermissions(this, strings, 1)
            }
        }
        return true
    }


    override fun onStart() {
        super.onStart()
        mMapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
    }

    override fun onPause() {
        mMapView?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

}