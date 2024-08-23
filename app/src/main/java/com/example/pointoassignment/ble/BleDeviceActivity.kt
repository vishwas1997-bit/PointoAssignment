package com.example.pointoassignment.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.pointoassignment.R
import com.example.pointoassignment.ble.adpter.BleDeviceAdapter
import com.example.pointoassignment.ble.data.BleDeviceModel
import com.example.pointoassignment.databinding.ActivityBleDeviceInfoBinding
import com.example.pointoassignment.utils.Utils
import timber.log.Timber
import java.util.UUID

class BleDeviceActivity : AppCompatActivity(), ScanResultConsumer,
    BleDeviceAdapter.BleDeviceAdapterListener {

    private val binding by lazy { ActivityBleDeviceInfoBinding.inflate(layoutInflater) }
    private val REQUEST_ENABLE_BT = 1
    private val bleScanner by lazy { BleScanner(this) }
    private val bleDeviceAdapter by lazy {
        BleDeviceAdapter(
            arrayListOf(), listener = this
        )
    }
    private lateinit var clientInfoSheetFragment: ClientInfoSheetFragment
    private val viewModel by lazy { ViewModelProvider(this)[BleViewModel::class.java] }
    private var gatt: BluetoothGatt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.rvDevice.adapter = bleDeviceAdapter

        binding.btnScan.setOnClickListener {
            bleDeviceAdapter.deviceList.clear()
            if (!bleScanner.isBluetoothEnable()) {
                showEnableBluetoothDialog(this)
                return@setOnClickListener
            }

            if (bleScanner.isScanning()) {
                return@setOnClickListener
            }
            //default scan time 30 seconds
            bleScanner.scan(this@BleDeviceActivity)
        }
    }

    override fun onDeviceFound(device: BluetoothDevice, scanRecord: ByteArray, rssi: Int) {
        prepareData(device, rssi)
    }

    override fun onScanningStarted() {
        binding.progressCircular.visibility = View.VISIBLE
        Utils.showToast("Scanning Start")
    }

    override fun onScanningStopped() {
        Utils.showToast("Scanning Stop")
        binding.progressCircular.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun prepareData(device: BluetoothDevice, rssi: Int) {
        if (ActivityCompat.checkSelfPermission(
                this@BleDeviceActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val name = if (device.name == null) {
            ""
        } else {
            device.name
        }
        val bleDeviceModel =
            BleDeviceModel(
                name = name,
                address = device.address,
                bleDevice = device,
                bondSate = device.bondState,
                signalStrength = rssi,
                deviceTypes = checkDeviceType(device.bluetoothClass)
            )
        bleDeviceAdapter.deviceList.add(bleDeviceModel)
        bleDeviceAdapter.notifyDataSetChanged()
    }

    private fun connect(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        gatt?.close()
        gatt = device.connectGatt(this, false, gattCallback)
        showClientInfo(device.name, device.address)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (ActivityCompat.checkSelfPermission(
                    this@BleDeviceActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
            }

            Timber.e("Connection State: $newState")
            Utils.showToast("State: $newState")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayAvailableServicesAndCharacteristics(gatt!!)
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                viewModel.clintInfo.value = Pair("GATT_FAILURE", "")
            }
        }
    }

    private fun displayAvailableServicesAndCharacteristics(gatt: BluetoothGatt) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val clientInfoSb = StringBuilder()
        val services = gatt.services
        for (service in services) {
            clientInfoSb.append("Service UUID: ${UUID.fromString(service.uuid.toString())}\n\t\t\t")


            val characteristics = service.characteristics
            for (characteristic in characteristics) {
                clientInfoSb.append("${displayCharacteristicProperties(characteristic)}\n")
            }

            clientInfoSb.append("\n\n")
        }
        Timber.e("Client info >>>>>>>>>> $clientInfoSb")
        viewModel.clintInfo.value = Pair("GATT_SUCCESS", clientInfoSb.toString())
    }

    private fun displayCharacteristicProperties(characteristic: BluetoothGattCharacteristic): String {
        val properties = characteristic.properties

        if (properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
            return "Characteristic ${characteristic.uuid}\n\t\t\t Properties: READ"
        }

        if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
            return "Characteristic ${characteristic.uuid}\n\t\t\t Properties: WRITE"
        }

        if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
            return "Characteristic ${characteristic.uuid}\n\t\t\t Properties: NOTIFY"
        }

        if (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
            return "Characteristic ${characteristic.uuid}\n\t\t\t  Properties: INDICATE"
        }

        if (properties and BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE != 0) {
            return "Characteristic ${characteristic.uuid}\n\t\t\t Properties: SIGNED WRITE"
        }

        return ""
    }

    override fun onItemClick(device: BluetoothDevice) {
        connect(device)
    }

    private fun showEnableBluetoothDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Enable Bluetooth")
            .setMessage("Bluetooth is required for this feature. Please enable Bluetooth.")
            .setPositiveButton("Enable") { dialog, _ ->
                // Start an intent to enable Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@setPositiveButton
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Handle the case where Bluetooth is not enabled
            }
            .setCancelable(false)
            .show()
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                bleScanner.scan(this)
            } else {
                Utils.showToast("Bluetooth is not enable")
            }
        }
    }

    private fun checkDeviceType(bluetoothClass: BluetoothClass): String {
        Timber.e("Device Class ${bluetoothClass.deviceClass}")
        when (bluetoothClass.deviceClass) {
            BluetoothClass.Device.PERIPHERAL_POINTING -> {
                return "Mouse"
            }

            BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> {
                return "LoudSpeaker"
            }

            BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET -> {
                return "HeadSet"
            }

            BluetoothClass.Device.PERIPHERAL_KEYBOARD -> {
                return "Keyboard"
            }

            else -> {
                return ""
            }
        }
    }

    private fun showClientInfo(deviceName: String, address: String) {
        clientInfoSheetFragment =
            ClientInfoSheetFragment(binding.parent.height, deviceName, address)
        clientInfoSheetFragment.setStyle(
            DialogFragment.STYLE_NO_TITLE, R.style.QuizAppBottomSheetDialogTheme
        )
        if (!clientInfoSheetFragment.isAdded && !clientInfoSheetFragment.isRemoving) {
            val fragmentManager = supportFragmentManager
            val existingFragment = fragmentManager.findFragmentByTag("POP_UP")
            if (existingFragment == null) {
                clientInfoSheetFragment.show(fragmentManager, "POP_UP")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        gatt?.close()
    }

}