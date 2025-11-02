package com.example.android.myproject.App

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.android.myproject.R
import com.google.android.material.snackbar.Snackbar

class InternetLostFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.internet_lost_fragment, container, false)
    }
    private var currentSnackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.exit_internet_lost).setOnClickListener {
            activity?.finishAffinity()
        }

        view.findViewById<TextView>(R.id.try_again_internet_lost).setOnClickListener {
            if (MyApp.NetworkUtil.isOnline) {
                dismiss()
            } else {
                if (currentSnackbar?.isShown != true) {
                    currentSnackbar = Snackbar.make(view, "Still no internet", Snackbar.LENGTH_SHORT)
                    currentSnackbar?.show()
                }
            }
        }
    }


}
