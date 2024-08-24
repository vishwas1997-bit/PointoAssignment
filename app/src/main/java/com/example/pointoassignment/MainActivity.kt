package com.example.pointoassignment

import android.Manifest
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pointoassignment.ble.BleDeviceActivity
import com.example.pointoassignment.databinding.ActivityMainBinding
import com.example.pointoassignment.utils.Utils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {


    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val REQUEST_CHECK_SETTINGS = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Check and request location permissions
        if (!isPermissionGranted()) {
            permissionRequest.launch(permission.toTypedArray())
        }

        binding.btnStartLocationUpdates.setOnClickListener {
            if (isPermissionGranted()) {
                if (Utils.isGPSEnabled()) {
                    startLocationService()
                } else {
                    enableGps()
                }
            } else {
                permissionRequest.launch(permission.toTypedArray())
            }
        }

        binding.btnStopLocationUpdates.setOnClickListener {
            Utils.showToast("Location Update Stop")
            stopService(Intent(this, LocationService::class.java))
        }

        binding.btnBleDeviceInfo.setOnClickListener {
            startActivity(Intent(this, BleDeviceActivity::class.java))
        }
    }


    private fun startLocationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, LocationService::class.java))
        } else {
            startService(Intent(this, LocationService::class.java))
        }
        Utils.showToast("Location updates, started, Please check Notification on StatusBar")
    }

    private fun isPermissionGranted() = permission.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private val permission = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            for (item in permissions) {
                if (item.key == Manifest.permission.POST_NOTIFICATIONS && !item.value) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Notification permission is required to show the persistent notification.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Grant") {

                    }.show()
                } else if (item.key == Manifest.permission.ACCESS_FINE_LOCATION && !item.value) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Location permission is required to use this app.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Grant") {

                    }.show()
                }
            }
        }

    private fun enableGps() {
        val locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true) //this displays dialog box like Google Maps with two buttons - OK and NO,THANKS
        val task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        task.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(
                                this@MainActivity,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (e: SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                RESULT_OK -> {
                    if (isPermissionGranted()) {
                        if (Utils.isGPSEnabled()) {
                            startLocationService()
                        }
                    }
                }
                RESULT_CANCELED -> {
                    Utils.showToast("Gps not enable")
                }
                else -> {}
            }
        }
    }

}