package com.example.pointoassignment.ble

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class BleViewModel: ViewModel() {
    val clintInfo = MutableStateFlow(Pair("", ""))
}