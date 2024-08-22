package com.example.pointoassignment

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.pointoassignment.utils.Utils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import timber.log.Timber
import java.util.Locale

class LocationService : Service() {

    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (360 * 1000 /* 6 minute */).toLong()
    private val FASTEST_INTERVAL = (180 * 1000 /* 3 minute */).toLong()
    private val MIN_DISPLACEMENT = 10.0f /* 10 meters */
    private var addressLocality: String = ""

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NOTIFICATION_ID,
            createNotification(0.0, 0.0, currentLocation = "Fetching Location....")
        )
        mLocationRequest =
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMaxUpdateDelayMillis(FASTEST_INTERVAL)
                .setMinUpdateDistanceMeters(MIN_DISPLACEMENT)
                .build()
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    location?.let { setLocation(it) }
                }
            }
        }
        startLocationUpdates()
    }

    private fun setLocation(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            if (Utils.isNetworkAvailable(this)) {
                val addressList: List<Address>? =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val currentLat = location.latitude
                val currentLong = location.longitude
                Timber.e("Coordinates >>>>>>>>>>>>> Lat: $currentLat, Long: $currentLong")
                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    val sb = StringBuilder()

                    if (address.subLocality != null) {
                        sb.append(address.subLocality).append(", ")
                    }
                    if (address.locality != null) {
                        sb.append(address.locality)
                    }

                    addressLocality = sb.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            val latitude = location.latitude
            val longitude = location.longitude
            updateNotification(
                latitude = latitude,
                longitude = longitude,
                currentLocation = addressLocality
            )
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest!!,
            mLocationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback!!)
    }

    private fun createNotification(
        latitude: Double,
        longitude: Double,
        currentLocation: String
    ): Notification {
        val notificationChannelId = "LOCATION_CHANNEL"
        val channelName = "Location Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        return notificationBuilder.setContentTitle(currentLocation)
            .setContentText("Latitude: $latitude, Longitude: $longitude")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(latitude: Double, longitude: Double, currentLocation: String) {
        val notification = createNotification(latitude, longitude, currentLocation)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    companion object {
        const val NOTIFICATION_ID = 1234
    }
}