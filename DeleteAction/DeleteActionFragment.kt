package com.example.android.myproject.DeleteAction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.android.myproject.GroupChat_Page.GroupChatActivity
import com.example.android.myproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteActionFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delete_action, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var person = arguments?.getString("person")
        var ischats = arguments?.getString("IsChats")
        var isgroup = arguments?.getString("IsGroups")
        var messageKey = arguments?.getString("messageKey")
        var message = arguments?.getString("message")
        var timeStamp = arguments?.getLong("messageTime")

        val currentTime = System.currentTimeMillis()
        val messageTime = timeStamp!!

        Log.d("DeleteActionFragment", ischats.toString())

        if(person == "receiver"){
            view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne).visibility = View.GONE
        }
        else if(message == "This message was Deleted!!!"){
            view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne).visibility = View.GONE
        }
        else if (!isWithinFiveMinutes(currentTime, messageTime)) {
            Log.d("isWithinFiveMinutes", "Delete")
            view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne).visibility = View.GONE
        }
        else{
            view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne).visibility = View.VISIBLE
        }

        view.findViewById<RelativeLayout>(R.id.DeleteForMe).setOnClickListener {

            if(ischats == "true"){

                val senderUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

                val receiverRef = FirebaseDatabase.getInstance().getReference("user")
                    .child(senderUid)
                    .child("activeChatUid")

                receiverRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val receiverUid = snapshot.value.toString()
                        val senderRoom = senderUid + receiverUid

                        val messageKey = messageKey.toString()

                        FirebaseDatabase.getInstance()
                            .getReference("chats").child(senderRoom)
                            .child("messages").child(messageKey).removeValue()

                        Toast.makeText(context,"Message Deleted!!", Toast.LENGTH_SHORT).show()

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            }

            else if(isgroup == "true"){

                val groupId = GroupChatActivity.Companion.groupUid

                val messageKey = messageKey.toString()

                FirebaseDatabase.getInstance()
                    .getReference("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .child("messages")
                    .child(messageKey)
                    .removeValue()

                Toast.makeText(context,"Message Deleted!!", Toast.LENGTH_SHORT).show()
            }

            dismiss()

        }

        view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne).setOnClickListener {

            if(ischats == "true"){

                val senderUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

                val receiverRef = FirebaseDatabase.getInstance().getReference("user")
                    .child(senderUid)
                    .child("activeChatUid")

                receiverRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val receiverUid = snapshot.value.toString()
                        val senderRoom = senderUid + receiverUid
                        val receiverRoom = receiverUid + senderUid

                        val updates = mapOf<String, Any>(
                            "message" to "This message was Deleted!!!",
                            "messageType" to "none"
                        )

                        val messageKey = messageKey.toString()

                        FirebaseDatabase.getInstance()
                            .getReference("chats").child(senderRoom)
                            .child("messages").child(messageKey).removeValue()

                        val receiverMsgRef = FirebaseDatabase.getInstance()
                            .getReference("chats").child(receiverRoom)
                            .child("messages").child(messageKey)

                        receiverMsgRef.updateChildren(updates)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            }

            else if(isgroup == "true"){

                val updates = mapOf<String, Any>(
                    "message" to "This message was Deleted!!!",
                    "messageType" to "none"
                )

                val messageKey = messageKey.toString()
                val groupId = GroupChatActivity.Companion.groupUid ?: return@setOnClickListener
                val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

                for (memberId in GroupChatActivity.Companion.groupMembers) {
                    val messageRef = FirebaseDatabase.getInstance()
                        .getReference("groups")
                        .child(groupId)
                        .child("groupMessages")
                        .child(memberId)
                        .child("messages")
                        .child(messageKey)

                    if(memberId == currentUser){
                        messageRef.removeValue()
                    }
                    else {
                        messageRef.updateChildren(updates)
                    }
                }
                Toast.makeText(context,"Message Deleted!!", Toast.LENGTH_SHORT).show()
            }

            dismiss()

        }

    }

    fun isWithinFiveMinutes(timestamp1: Long, timestamp2: Long): Boolean {
        val diffInMillis = kotlin.math.abs(timestamp1 - timestamp2)
        return diffInMillis < 5 * 60 * 1000
    }


}