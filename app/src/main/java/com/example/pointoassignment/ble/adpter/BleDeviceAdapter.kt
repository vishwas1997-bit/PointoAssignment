package com.example.pointoassignment.ble.adpter

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pointoassignment.ble.data.BleDeviceModel
import com.example.pointoassignment.databinding.BleDeviceItemLayoutBinding

class BleDeviceAdapter(
    val deviceList: ArrayList<BleDeviceModel>,
    private val listener: BleDeviceAdapterListener
) :
    RecyclerView.Adapter<BleDeviceAdapter.BleDeviceViewHolder>() {

    interface BleDeviceAdapterListener {
        fun onItemClick(device: BluetoothDevice)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BleDeviceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BleDeviceViewHolder(BleDeviceItemLayoutBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: BleDeviceViewHolder, position: Int) {
        holder.bindData(deviceList[position])
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    inner class BleDeviceViewHolder(private val binding: BleDeviceItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(data: BleDeviceModel) {
            binding.tvDeviceName.text = data.name

            binding.tvDeviceAddress.text = data.address

            when (data.bondSate) {
                BOND_NONE -> {
                    binding.tvBondState.text = "NOT BONDED"
                }

                BOND_BONDED -> {
                    binding.tvBondState.text = "BONDED"
                }

                BOND_BONDING -> {
                    binding.tvBondState.text = "BONDING"
                }
            }

            val signalStrength = "Signal Strength: ${data.signalStrength} dBM"
            binding.tvSignalStrength.text = signalStrength
            binding.tvDeviceType.text = data.deviceTypes

            binding.btnConnect.setOnClickListener {
                listener.onItemClick(data.bleDevice)
            }
        }
    }
}