package com.example.pointoassignment.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import timber.log.Timber


class BleScanner(private val context: Context) {
    private var leScanner: BluetoothLeScanner? = null
    private var bleAdapter: BluetoothAdapter? = null
    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }
    private var scanResultConsumer: ScanResultConsumer? = null
    private var scanning = false
    private val foundDeviceList by lazy { ArrayList<BluetoothDevice>() }

    init {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bluetoothManager.adapter
        if (bleAdapter == null) {
            Timber.e("No bluetooth hardware.")
        } else if (!bleAdapter!!.isEnabled) {
            Timber.e("Bluetooth is off.")
        }
    }

    fun isBluetoothEnable(): Boolean{
       return bleAdapter?.isEnabled!!
    }

    fun scan(scanResultConsumer: ScanResultConsumer?, scanTime: Long = 30000) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        foundDeviceList.clear()
        if (scanning) {
            Timber.e("Already scanning.")
            return
        }
        Timber.e("Scanning...")
        if (leScanner == null) {
            leScanner = bleAdapter!!.bluetoothLeScanner
        }
        if (scanTime > 0) {
            uiHandler.postDelayed({
                if (scanning) {
                    Timber.e("Scanning is stopping.")
                    if (leScanner != null) leScanner!!.stopScan(scanCallBack) else Timber.e(
                        "Scanner null"
                    )
                    setScanning(false)
                }
            }, scanTime)
        }
        this.scanResultConsumer = scanResultConsumer
        leScanner!!.startScan(scanCallBack)
        setScanning(true)
    }

    private val scanCallBack: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (!scanning) {
                return
            }
            if (foundDeviceList.contains(result.device)) {
                // This device has already been found
                return
            }

            // New device found, add it to the list in order to prevent duplications
            foundDeviceList.add(result.device)
            if (scanResultConsumer != null) {
                uiHandler.post {
                    scanResultConsumer!!.onDeviceFound(
                        result.device,
                        result.scanRecord!!.bytes, result.rssi
                    )
                }
            }
        }
    }

    fun isScanning(): Boolean {
        return scanning
    }

    private fun setScanning(scanning: Boolean) {
        this.scanning = scanning
        uiHandler.post {
            if (scanResultConsumer == null) return@post
            if (!scanning) {
                scanResultConsumer!!.onScanningStopped()
                // Nullify the consumer in order to prevent UI crashes
                scanResultConsumer = null
            } else {
                scanResultConsumer!!.onScanningStarted()
            }
        }
    }

}