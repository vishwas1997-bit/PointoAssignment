package com.example.pointoassignment.ble

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pointoassignment.R
import com.example.pointoassignment.databinding.ClientInfoLayoutBinding
import com.example.pointoassignment.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ClientInfoSheetFragment(
    private val height: Int,
    private val deviceName: String,
    private val address: String
) : BottomSheetDialogFragment() {

    private val binding by lazy {
        ClientInfoLayoutBinding.inflate(LayoutInflater.from(context))
    }

    private lateinit var dialog: Dialog
    private val viewModel by lazy { ViewModelProvider(requireActivity())[BleViewModel::class.java] }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { view ->
                val behaviour = BottomSheetBehavior.from(view)
                behaviour.maxHeight = height
                behaviour.peekHeight = height
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                behaviour.isDraggable = false
            }
            parentLayout?.setBackgroundResource(R.drawable.rounded_top_corners)
        }
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        binding.parent.layoutParams =
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, height)
        dialog.setContentView(binding.root)

        binding.tvDeviceName.text = deviceName
        binding.tvDeviceAddress.text = address

        lifecycleScope.launch {
            viewModel.clintInfo.collectLatest {
                when (it.first) {
                    "GATT_SUCCESS" -> {
                        binding.progressCircular.visibility = View.GONE
                        binding.tvClientInfo.text = it.second
                        viewModel.clintInfo.value = Pair("", "")
                    }

                    "GATT_FAILURE" -> {
                        binding.progressCircular.visibility = View.GONE
                        Utils.showToast("Unable to connect, Please try later")
                    }
                }
            }
        }

        binding.icCross.setOnClickListener {
            dialog.dismiss()
        }
    }
}