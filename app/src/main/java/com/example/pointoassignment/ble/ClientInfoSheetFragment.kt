package com.example.pointoassignment.ble

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.pointoassignment.R
import com.example.pointoassignment.databinding.ClientInfoLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClientInfoSheetFragment(
    private val height: Int,
    private val clientInfoSb: String
) : BottomSheetDialogFragment() {

    private val binding by lazy {
        ClientInfoLayoutBinding.inflate(LayoutInflater.from(context))
    }

    private lateinit var dialog: Dialog

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
        dialog.setContentView(binding.root)
        binding.tvClientInfo.text = clientInfoSb
    }
}