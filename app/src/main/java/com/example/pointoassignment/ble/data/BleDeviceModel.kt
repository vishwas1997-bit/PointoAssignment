package com.example.pointoassignment.ble.data

import android.bluetooth.BluetoothDevice

data class BleDeviceModel(
    val name: String,
    val address: String,
    val bleDevice: BluetoothDevice
)
