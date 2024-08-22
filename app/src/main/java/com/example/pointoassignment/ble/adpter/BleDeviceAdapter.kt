package com.example.pointoassignment.ble.adpter

import android.bluetooth.BluetoothDevice
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
            val deviceName = "Device Name: ${data.name}"
            binding.tvDeviceName.text = deviceName
            val deviceAddress = "Address: ${data.address}"
            binding.tvDeviceAddress.text = deviceAddress

            binding.parent.setOnClickListener {
                listener.onItemClick(data.bleDevice)
            }
        }
    }
}