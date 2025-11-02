package com.example.android.myproject.DeleteAction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.android.myproject.GroupChat_Page.GroupProfileActivity.Companion.groupId
import com.example.android.myproject.GroupChat_Page.GroupProfileActivity.Companion.removeFromGroup
import com.example.android.myproject.Group_Main.GroupMainActivity
import com.example.android.myproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth

class ExitGroupFragment : BottomSheetDialogFragment() {

    private var safeContext: Context? = null

    companion object {
        fun newInstance(): ExitGroupFragment {
            return ExitGroupFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exit_group_confirmation, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<RelativeLayout>(R.id.Exit_group).setOnClickListener {
            removeFromGroup(
                FirebaseAuth.getInstance().currentUser?.uid.toString(),
                groupId = groupId.toString(),
                onSuccess = {
                    Toast.makeText(
                        safeContext,
                        "You have left the group",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(safeContext, GroupMainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    dismiss()
                },
                onFailure = {
                    Toast.makeText(
                        safeContext,
                        "Failed to remove: $it",
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            )
        }

        view.findViewById<RelativeLayout>(R.id.Cancel_Exit_Group).setOnClickListener {
            dismiss()
        }
    }

}