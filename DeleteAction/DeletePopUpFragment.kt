package com.example.android.myproject.DeleteAction

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.android.myproject.Chat_Page.ChatActivity
import com.example.android.myproject.Entities.*
import com.example.android.myproject.GroupChat_Page.GroupChatActivity
import com.example.android.myproject.Listeners.MultipleMessageSelectListener
import com.example.android.myproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.toString

class DeletePopUpFragment : BottomSheetDialogFragment() {

    private var listener: MultipleMessageSelectListener? = null

    private lateinit var person : String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("FragmentAttach", "Attaching fragment")
        if (context is MultipleMessageSelectListener) {
            listener = context
            Log.d("FragmentAttach", "Listener assigned")
        } else {
            Log.e("FragmentAttach", "$context must implement MultipleMessageSelectListener")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delete_options_popup, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        person = arguments?.getString("person").toString()

        if(person == "chat") {
            if (checkAllSender()) {
                if (checkForDeleteForEveryone()) {
                    view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne_popup).visibility =
                        View.VISIBLE
                    view.findViewById<RelativeLayout>(R.id.DeleteForMe_popup).visibility =
                        View.VISIBLE
                } else {
                    view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne_popup).visibility =
                        View.GONE
                    view.findViewById<RelativeLayout>(R.id.DeleteForMe_popup).visibility =
                        View.VISIBLE
                }
            } else {
                view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne_popup).visibility =
                    View.GONE
                view.findViewById<RelativeLayout>(R.id.DeleteForMe_popup).visibility = View.VISIBLE
            }

            view.findViewById<RelativeLayout>(R.id.DeleteForMe_popup).setOnClickListener {
                for (messageKey in ChatActivity.SelectMultipleMessages) {
                    val senderUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

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

                            Toast.makeText(context, "Message Deleted!!", Toast.LENGTH_SHORT).show()

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
                }
                ChatActivity.SelectMultipleMessages.clear()
                listener?.onMultiSelectEnded()
                dismiss()
            }

            view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne_popup).setOnClickListener {
                for (messageKey in ChatActivity.SelectMultipleMessages) {
                    val senderUid =
                        FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

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

                            FirebaseDatabase.getInstance()
                                .getReference("chats").child(senderRoom)
                                .child("messages").child(messageKey).removeValue()

                            val receiverMsgRef = FirebaseDatabase.getInstance()
                                .getReference("chats").child(receiverRoom)
                                .child("messages").child(messageKey)

                            receiverMsgRef.updateChildren(updates)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
                }
                ChatActivity.SelectMultipleMessages.clear()
                listener?.onMultiSelectEnded()
                dismiss()
            }

            view.findViewById<RelativeLayout>(R.id.Cancel).setOnClickListener {
                ChatActivity.SelectMultipleMessages.clear()
                listener?.onMultiSelectEnded()
                dismiss()
            }

        }
        else if(person == "group"){
            if (checkAllSenderFromGroup()) {
                if (checkForDeleteForEveryoneFromGroup()) {
                    view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne_popup).visibility =
                        View.VISIBLE
                    view.findViewById<RelativeLayout>(R.id.DeleteForMe_popup).visibility =
                        View.VISIBLE
                } else {
                    view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne_popup).visibility =
                        View.GONE
                    view.findViewById<RelativeLayout>(R.id.DeleteForMe_popup).visibility =
                        View.VISIBLE
                }
            } else {
                view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne_popup).visibility =
                    View.GONE
                view.findViewById<RelativeLayout>(R.id.DeleteForMe_popup).visibility = View.VISIBLE
            }

            view.findViewById<RelativeLayout>(R.id.DeleteForMe_popup).setOnClickListener{
                for(messageKey in GroupChatActivity.SelectMultipleMessages){
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
                }
                Toast.makeText(context,"Message Deleted!!", Toast.LENGTH_SHORT).show()
                GroupChatActivity.SelectMultipleMessages.clear()
                listener?.onMultiSelectEnded()
                dismiss()
            }

            view.findViewById<RelativeLayout>(R.id.DeleteForEveryOne_popup).setOnClickListener {
                for(messageKey in GroupChatActivity.SelectMultipleMessages){
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
                }
                Toast.makeText(context,"Message Deleted!!", Toast.LENGTH_SHORT).show()
                GroupChatActivity.SelectMultipleMessages.clear()
                listener?.onMultiSelectEnded()
                dismiss()
            }

            view.findViewById<RelativeLayout>(R.id.Cancel).setOnClickListener {
                GroupChatActivity.SelectMultipleMessages.clear()
                listener?.onMultiSelectEnded()
                dismiss()
            }

        }
    }

    fun checkAllSenderFromGroup() : Boolean {
        var flag = false
        for(messageKey in GroupChatActivity.SelectMultipleMessages){
            FirebaseDatabase.getInstance().getReference("groups")
                .child(GroupChatActivity.groupIdGlobal)
                .child("groupMessages")
                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .child("messages")
                .child(messageKey)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var message = snapshot.getValue(GroupMessage::class.java)
                        if(message?.messageKey != FirebaseAuth.getInstance().currentUser?.uid.toString()){
                            flag = true
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
        return flag
    }
    fun checkForDeleteForEveryoneFromGroup() : Boolean{
        var flag = false
        val currentTime = System.currentTimeMillis()
        for(messageKey in GroupChatActivity.SelectMultipleMessages){
            FirebaseDatabase.getInstance().getReference("groups")
                .child(GroupChatActivity.groupIdGlobal)
                .child("groupMessages")
                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .child("messages")
                .child(messageKey)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var message = snapshot.getValue(GroupMessage::class.java)
                        if(!isWithinFiveMinutes(currentTime, message?.timeStamp!!)){
                            flag = true
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
        return flag
    }
    fun checkAllSender() : Boolean {

        var flag = false
        for(messageKey in ChatActivity.SelectMultipleMessages){
            val senderUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

            FirebaseDatabase.getInstance()
                .getReference("chats").child(ChatActivity.senderRoomGlobal)
                .child("messages").child(messageKey)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var message = snapshot.getValue(Message::class.java)
                        if(message?.messageKey != senderUid){
                            flag = true
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Cancelled","Cancelled")
                    }
                })
        }

        return flag
    }
    fun checkForDeleteForEveryone() : Boolean{
        var flag = false
        val currentTime = System.currentTimeMillis()
        for(messageKey in ChatActivity.SelectMultipleMessages){
            FirebaseDatabase.getInstance().getReference("chats").child(ChatActivity.senderRoomGlobal).child("messages")
                .child(messageKey).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var message = snapshot.getValue(Message::class.java)
                        if(!isWithinFiveMinutes(currentTime, message?.timeStamp!!)){
                            flag = true
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Cancelled","Cancelled")
                    }

                })
        }
        return flag
    }
    fun isWithinFiveMinutes(timestamp1: Long, timestamp2: Long): Boolean {
        val diffInMillis = kotlin.math.abs(timestamp1 - timestamp2)
        return diffInMillis < 5 * 60 * 1000
    }

}