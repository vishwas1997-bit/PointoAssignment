package com.example.pointoassignment.ble

import android.bluetooth.BluetoothDevice

interface ScanResultConsumer {
    fun onDeviceFound(device: BluetoothDevice, scanRecord: ByteArray, rssi: Int)
    fun onScanningStarted()
    fun onScanningStopped()
}