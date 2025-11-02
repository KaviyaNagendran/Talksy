package com.example.android.myproject.Listeners

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.android.myproject.Listeners.MediaPickerListener
import com.example.android.myproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MediaPickerFragment(private val listener: MediaPickerListener) : BottomSheetDialogFragment() {

    private var IsProfilePage : String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chatpage_additional_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        IsProfilePage = arguments?.getString("ProfileActivity").toString()

        if(IsProfilePage == "Yes"){
            view.findViewById<LinearLayout>(R.id.camera).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.document).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.audio).visibility = View.GONE
        }

        view.findViewById<LinearLayout>(R.id.selectMedia).setOnClickListener {
            listener.onMediaSelected()
            dismiss()
        }

        view.findViewById<LinearLayout>(R.id.camera).setOnClickListener {
            listener.onCameraProcess()
            dismiss()
        }

        view.findViewById<LinearLayout>(R.id.document).setOnClickListener {
            listener.onDocumentSelect()
            dismiss()
        }

        view.findViewById<LinearLayout>(R.id.audio).setOnClickListener {
            listener.onAudioSelect()
            dismiss()
        }
    }
}