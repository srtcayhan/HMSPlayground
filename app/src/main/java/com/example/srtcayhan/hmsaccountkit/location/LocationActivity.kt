package com.example.srtcayhan.hmsaccountkit.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.example.srtcayhan.hmsaccountkit.R
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityLocationBinding
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*


class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding

    private val TAG = "LocationActivity"

    private var logTextView: TextView? = null

    private var mLocationCallback: LocationCallback? = null

    private var mLocationRequest: LocationRequest? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var settingsClient: SettingsClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        binding = ActivityLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        logTextView = findViewById<View>(R.id.LogText) as TextView

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // create settingsClient
        settingsClient = LocationServices.getSettingsClient(this)
        mLocationRequest = LocationRequest().apply {
            interval = 1000
            needAddress = true
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        }

        binding.locationRequestLocationUpdatesWithCallback.setOnClickListener {
            requestLocationUpdatesWithCallback()
        }

        binding.locationRemoveLocationUpdatesWithCallback.setOnClickListener {
            removeLocationUpdatesWithCallback()
        }


        if (null == mLocationCallback) {
            mLocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult != null) {
                        val locations: List<Location> =
                            locationResult.getLocations()
                        if (locations.isNotEmpty()) {
                            for (location in locations) {
                                showLog(
                                    "onLocationResult location[Longitude,Latitude,Accuracy]:${location.longitude} , ${location.latitude} , ${location.accuracy}"
                                )
                            }
                        }
                    }
                }
            }

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    Log.i(TAG, "sdk < 28 Q")
                    if (checkSelfPermission(
                            this,
                            ACCESS_FINE_LOCATION
                        ) != PERMISSION_GRANTED
                        && checkSelfPermission(
                            this,
                            ACCESS_COARSE_LOCATION
                        ) != PERMISSION_GRANTED
                    ) {
                        val strings = arrayOf(
                            ACCESS_FINE_LOCATION,
                            ACCESS_COARSE_LOCATION
                        )
                        ActivityCompat.requestPermissions(this, strings, 1)
                    }
                } else {
                    if (checkSelfPermission(
                            this,
                            ACCESS_FINE_LOCATION
                        ) != PERMISSION_GRANTED && checkSelfPermission(
                            this,
                            ACCESS_COARSE_LOCATION
                        ) != PERMISSION_GRANTED && checkSelfPermission(
                            this,
                            "android.permission.ACCESS_BACKGROUND_LOCATION"
                        ) != PERMISSION_GRANTED
                    ) {
                        val strings = arrayOf(
                            ACCESS_FINE_LOCATION,
                            ACCESS_COARSE_LOCATION,
                            "android.permission.ACCESS_BACKGROUND_LOCATION"
                        )
                        ActivityCompat.requestPermissions(this, strings, 2)
                    }
                }

            fun onRequestPermissionsResult(
                requestCode: Int,
                permissions: Array<String?>,
                grantResults: IntArray
            ) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                if (requestCode == 1) {
                    if (grantResults.size > 1 && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
                        Log.i(
                            TAG,
                            "onRequestPermissionsResult: apply LOCATION PERMISSION successful"
                        )
                    } else {
                        Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION  failed")
                    }
                }
                if (requestCode == 2) {
                    if (grantResults.size > 2 && grantResults[2] == PERMISSION_GRANTED && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED
                    ) {
                        Log.i(
                            TAG,
                            "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful"
                        )
                    } else {
                        Log.i(
                            TAG,
                            "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION  failed"
                        )
                    }
                }
            }
        }


    }

    private fun requestLocationUpdatesWithCallback() {
        try {
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(mLocationRequest)
            val locationSettingsRequest = builder.build()
            // check devices settings before request location updates.
            //Before requesting location update, invoke checkLocationSettings to check device settings.
            val locationSettingsResponseTask: Task<LocationSettingsResponse> =
                settingsClient.checkLocationSettings(locationSettingsRequest)

            locationSettingsResponseTask.addOnSuccessListener { locationSettingsResponse: LocationSettingsResponse? ->
                showLog( "check location settings success  {$locationSettingsResponse}")
                // request location updates
                fusedLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.getMainLooper()
                )
                    .addOnSuccessListener {
                        showLog("requestLocationUpdatesWithCallback onSuccess")
                    }
                    .addOnFailureListener { e ->
                        showLog(
                            "requestLocationUpdatesWithCallback onFailure:${e.message}"
                        )
                    }
            }
                .addOnFailureListener { e: Exception ->
                    Log.e(TAG, "checkLocationSetting onFailure:${e.message}")
                    when ((e as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(
                                this@LocationActivity, 0
                            )
                        } catch (sie: SendIntentException) {
                            Log.e(TAG, "PendingIntent unable to execute request.")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "requestLocationUpdatesWithCallback exception:${e.message}")
        }
    }

    private fun removeLocationUpdatesWithCallback() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                .addOnSuccessListener {
                    showLog(

                        "removeLocationUpdatesWithCallback onSuccess"
                    )
                }
                .addOnFailureListener { e ->
                    showLog(
                        "removeLocationUpdatesWithCallback onFailure:${e.message}"
                    )
                }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "removeLocationUpdatesWithCallback exception:${e.message}"
            )
        }

    }
    @SuppressLint("SetTextI18n")
    private fun showLog(log: String) {
        logTextView!!.text = "log:\n$log"
    }

}