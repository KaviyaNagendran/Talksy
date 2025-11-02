package com.example.android.myproject.DeleteAction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.android.myproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CameraActionsFragment(
    private val onTakePhoto: () -> Unit,
    private val onTakeVideo: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.PhotoLayout).setOnClickListener {
            onTakePhoto()
            dismiss()
        }

        view.findViewById<LinearLayout>(R.id.VideoLayout).setOnClickListener {
            onTakeVideo()
            dismiss()
        }
    }
}
