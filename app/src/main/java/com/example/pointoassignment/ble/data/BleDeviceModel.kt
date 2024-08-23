package com.example.pointoassignment.ble.data

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_NONE

data class BleDeviceModel(
    val name: String,
    val address: String,
    val bleDevice: BluetoothDevice,
    val bondSate: Int = BOND_NONE,
    val signalStrength: Int = 0,
    val deviceTypes: String = ""
)
