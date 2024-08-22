package com.example.pointoassignment.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.pointoassignment.ble.adpter.BleDeviceAdapter
import com.example.pointoassignment.ble.data.BleDeviceModel
import com.example.pointoassignment.databinding.ActivityBleDeviceInfoBinding
import timber.log.Timber

class BleDeviceActivity : AppCompatActivity(), ScanResultConsumer, BleDeviceAdapter.BleDeviceAdapterListener {

    private val binding by lazy { ActivityBleDeviceInfoBinding.inflate(layoutInflater) }
    private val bleScanner by lazy { BleScanner(this) }
    private val bleDeviceAdapter by lazy { BleDeviceAdapter(arrayListOf(), listener = this
    ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.rvDevice.adapter = bleDeviceAdapter

        binding.btnScan.setOnClickListener {
            if (bleScanner.isScanning()) {
                return@setOnClickListener
            }
            bleDeviceAdapter.deviceList.clear()
            // Scan Time 10 seconds
            bleScanner.scan(this@BleDeviceActivity, scanTime = 10000)
        }
    }

    override fun onDeviceFound(device: BluetoothDevice, scanRecord: ByteArray, rssi: Int) {
        prepareData(device)
    }

    override fun onScanningStarted() {
        Timber.e("Scanning Start")
    }

    override fun onScanningStopped() {
        Timber.e("Scanning Stop")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun prepareData(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this@BleDeviceActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val name = if (device.name == null) {
            "Unknown"
        } else {
            device.name
        }
        val bleDeviceModel =
            BleDeviceModel(name = name, address = device.address, bleDevice = device)
        bleDeviceAdapter.deviceList.add(bleDeviceModel)
        bleDeviceAdapter.notifyDataSetChanged()
    }

    private fun connect(device: BluetoothDevice) {
        val gatt = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            null
        }
        device.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (ActivityCompat.checkSelfPermission(
                        this@BleDeviceActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                gatt?.close()
            }
            Timber.e("Connection State: $newState")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (ActivityCompat.checkSelfPermission(
                        this@BleDeviceActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                for (service in gatt!!.services) {
                    val characteristic = service.characteristics
                    characteristic[0].properties
                }
            }
        }

    }

    override fun onItemClick(device: BluetoothDevice) {
        connect(device)
    }
}