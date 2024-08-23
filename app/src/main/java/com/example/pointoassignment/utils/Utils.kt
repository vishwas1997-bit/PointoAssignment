package com.example.pointoassignment.utils

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pointoassignment.MyApplication

object Utils {

    private var toastMessage: Toast? = null

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val nw = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(nw) ?: return false

        return networkCapabilities.run {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
        }
    }

    fun showToast(toastMsg: String) {
        if (toastMessage != null) {
            (toastMessage as Toast).cancel()
        }
        toastMessage = Toast.makeText(MyApplication.instance, toastMsg, Toast.LENGTH_SHORT)
        (toastMessage as Toast).show()
    }


    fun isGPSEnabled(): Boolean {
        val locationManager = MyApplication.instance.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}