package com.example.android.myproject.DeleteAction

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.android.myproject.Listeners.MultipleMessageSelectListener
import com.example.android.myproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.example.android.myproject.Entities.*
import com.google.firebase.database.*

class BasicOptionsFrgment : BottomSheetDialogFragment() {

    private lateinit var receiver : String
    private lateinit var type : String
    private var listener: MultipleMessageSelectListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MultipleMessageSelectListener) {
            listener = context
        } else {
            Log.e("FragmentAttach", "$context must implement MultipleMessageSelectListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.options_fragment_mainactivity, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        type = arguments?.getString("Is").toString()
        receiver = arguments?.getString("receiver").toString()

        var currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if(type == "chat") {

            view.findViewById<RelativeLayout>(R.id.DeleteChat).setOnClickListener {
                val chatId = currentUserId + receiver
                val chatRef = FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatId)

                chatRef.removeValue().addOnSuccessListener {
                    listener?.onMultiSelectStarted()
                    context?.let {
                        Toast.makeText(it, "Message Deleted successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                    dismiss()
                }.addOnFailureListener {
                    context?.let {
                        Toast.makeText(it, "Failed to Delete Chat", Toast.LENGTH_SHORT).show()
                    }
                }
                dismiss()
            }

            view.findViewById<RelativeLayout>(R.id.ArchiveChat).setOnClickListener {
                val chatId = currentUserId + receiver
                FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatId).child("archive").setValue(true)
                    .addOnSuccessListener {
                        listener?.onMultiSelectStarted()
                        context?.let {
                            Toast.makeText(it, "Chat Archived successfully", Toast.LENGTH_SHORT)
                                .show()
                        }
                        dismiss()
                    }.addOnFailureListener {
                        context?.let {
                            Toast.makeText(it, "Failed to Archive Chat", Toast.LENGTH_SHORT).show()
                        }
                    }

                dismiss()
            }

        }
        else {

            isGroupMember(receiver) { isMember ->
                if(isMember) {
                    view.findViewById<RelativeLayout>(R.id.DeleteChat).visibility = View.GONE
                }
            }

            view.findViewById<RelativeLayout>(R.id.DeleteChat).setOnClickListener {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

                val chatRef = FirebaseDatabase.getInstance().getReference("groups").child(receiver)
                    .child("groupMessages").child(currentUserId)

                chatRef.removeValue().addOnSuccessListener {
                    listener?.onMultiSelectStarted()
                    context?.let {
                        Toast.makeText(it, "Group Deleted successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                }.addOnFailureListener {
                    context?.let {
                        Toast.makeText(it, "Failed to Delete Group", Toast.LENGTH_SHORT).show()
                    }
                }
                dismiss()
            }

            view.findViewById<RelativeLayout>(R.id.ArchiveChat).setOnClickListener {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

                FirebaseDatabase.getInstance().getReference("groups")
                    .child(receiver).child("groupMessages").child(currentUserId).child("archive").setValue(true)
                    .addOnSuccessListener {
                        listener?.onMultiSelectStarted()
                        context?.let {
                            Toast.makeText(it, "Group Archived successfully", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }.addOnFailureListener {
                        context?.let {
                            Toast.makeText(it, "Failed to Archive Group", Toast.LENGTH_SHORT).show()
                        }
                    }
                dismiss()
            }
        }
    }

    fun isGroupMember(groupId: String, callback: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(false)
        val groupRef = FirebaseDatabase.getInstance().getReference("groups")
            .child(groupId)
            .child("groupDetails")
            .child(groupId)

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Group::class.java)
                val isMember = group?.groupMembers?.contains(currentUserId) == true
                callback(isMember)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }


}