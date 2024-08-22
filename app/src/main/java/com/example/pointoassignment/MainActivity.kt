package com.example.pointoassignment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pointoassignment.ble.BleDeviceActivity
import com.example.pointoassignment.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Check and request location permissions
        if (!isPermissionGranted()) {
            permissionRequest.launch(permission.toTypedArray())
        }

        binding.btnStartLocationUpdates.setOnClickListener {
            if (isPermissionGranted()) {
                startLocationService()
            } else {
                permissionRequest.launch(permission.toTypedArray())
            }
        }

        binding.btnStopLocationUpdates.setOnClickListener {
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
    }

    private fun isPermissionGranted() = permission.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private val permission = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        add(Manifest.permission.ACCESS_FINE_LOCATION)
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

}