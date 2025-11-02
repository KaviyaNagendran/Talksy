package com.example.android.myproject.DeleteAction

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.android.myproject.Entities.GroupMessage
import com.example.android.myproject.Forward_Message.UserAndGroupListActivity
import com.example.android.myproject.GroupChat_Page.GroupChatActivity
import com.example.android.myproject.GroupChat_Page.GroupChatAdapter
import com.example.android.myproject.Listeners.MultipleMessageSelectListener
import com.example.android.myproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteGroupChatMessagesFragment : BottomSheetDialogFragment() {

    private var listener: MultipleMessageSelectListener? = null

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
        val view = inflater.inflate(R.layout.fragment_pop_up_action_message, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val person = arguments?.getString("messageBy")
        val messageKey = arguments?.getString("messageKey")
        val messageType = arguments?.getString("messageType")
        val message = arguments?.getString("message")
        val timeStamp = arguments?.getLong("messageTime")
        val groupId = GroupChatActivity.Companion.groupUid

        if(person == "receiver" || messageType == "image" || messageType == "video" || messageType == "document" || messageType == "voiceRecord" || messageType == "audio"){
            view.findViewById<RelativeLayout>(R.id.EditMessage).visibility = View.GONE
        }
        else{
            view.findViewById<RelativeLayout>(R.id.EditMessage).visibility = View.VISIBLE
        }

        if(message == "This message was Deleted!!!"){
            view.findViewById<RelativeLayout>(R.id.EditMessage).visibility = View.GONE
            view.findViewById<RelativeLayout>(R.id.CopyMessage).visibility = View.GONE
        }
        else{
            view.findViewById<RelativeLayout>(R.id.EditMessage).visibility = View.VISIBLE
            view.findViewById<RelativeLayout>(R.id.CopyMessage).visibility = View.VISIBLE
        }

        if(messageType == "image" || messageType == "video" || messageType == "document" || messageType == "voiceRecord" || messageType == "audio"){
            view.findViewById<RelativeLayout>(R.id.CopyMessage).visibility = View.GONE
        }
        else{
            view.findViewById<RelativeLayout>(R.id.CopyMessage).visibility = View.VISIBLE
        }

        val currentTime = System.currentTimeMillis()
        val messageTime = timeStamp!!

        if (!isWithinFiveMinutes(currentTime, messageTime)) {
            Log.d("isWithinFiveMinutes", "group")
            view.findViewById<RelativeLayout>(R.id.EditMessage).visibility = View.GONE
        }


        view.findViewById<RelativeLayout>(R.id.EditMessage).setOnClickListener {
            startEditing(messageKey.toString(),message.toString())
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.DeleteMessage).setOnClickListener {
            val dialog = DeleteActionFragment()
            val bundle = Bundle().apply {
                putString("person",person)
                putString("IsGroups", true.toString())
                putString("messageKey", messageKey)
                putString("message",message)
                putLong("messageTime", timeStamp)
            }
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "DeleteAction")
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.CopyMessage).setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
            FirebaseDatabase.getInstance().getReference("groups").child(groupId.toString()).child("groupMessages")
                .child(currentUser).child("messages").child(messageKey.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var message = snapshot.getValue(GroupMessage::class.java)
                        val clip = ClipData.newPlainText("Copied Message", message?.message)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.ForwardMessage).setOnClickListener {
            val intent = Intent(context, UserAndGroupListActivity::class.java)
            intent.putExtra("message",message)
            intent.putExtra("messageType",messageType)
            intent.putExtra("messageKey",messageKey)
            intent.putExtra("groupId",groupId)
            intent.putExtra("person","groups")
            startActivity(intent)
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.ReplyMessage).setOnClickListener {
            if (person == "receiver") {
                Toast.makeText(context, person, Toast.LENGTH_SHORT).show()

                getName(messageKey.toString(),groupId) { name ->
                    var messages = ""
                    getMessageType(messageKey.toString(),groupId) { messageType ->
                        if (messageType == "image") {
                            messages = "🏙️ Image"
                        } else if (messageType == "video") {
                            messages = "🎥 Video"
                        } else if (messageType == "voiceRecord") {
                            messages = "🎙️ Voice Message"
                        }  else if (messageType == "audio") {
                            messages = "🎧 Audio"
                        } else if(messageType == "document"){
                            messages = "📄Document"
                        } else{
                            messages = message.toString()
                        }
                        Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
                        (context as? GroupChatActivity)?.replyUiChanges(
                            messages,
                            name,
                            messageType.toString(),
                            messageKey.toString(),
                            groupId,
                        )
                        dismiss()
                    }
                }
            } else {
                var messages = ""
                getMessageType(messageKey.toString(),groupId) { messageType ->
                    if (messageType == "image") {
                        messages = "🏙️ Image"
                    } else if (messageType == "video") {
                        messages = "🎥 Video"
                    } else if (messageType == "voiceRecord") {
                        messages = "🎙️ Voice Message"
                    }  else if (messageType == "audio") {
                        messages = "🎧 Audio"
                    } else if(messageType == "document"){
                        messages = "📄Document"
                    } else{
                        messages = message.toString()
                    }
                    (context as? GroupChatActivity)?.replyUiChanges(
                        messages,
                        "You",
                        messageType.toString(),
                        messageKey.toString(),
                        groupId
                    )
                    dismiss()
                }
            }
        }

        view.findViewById<RelativeLayout>(R.id.EnableMultipleSelect).setOnClickListener {
            GroupChatAdapter.IsMultipleSelect = true
            GroupChatActivity.SelectMultipleMessages.add(messageKey.toString())
            listener?.onMultiSelectStarted()
            Log.d("EnableMultipleUsers-inside","true")
            dismiss()
        }
    }

    fun isWithinFiveMinutes(timestamp1: Long, timestamp2: Long): Boolean {
        val diffInMillis = kotlin.math.abs(timestamp1 - timestamp2)
        return diffInMillis < 5 * 60 * 1000
    }

    fun getName(messageKey: String, groupId : String, callback: (String) -> Unit){
        FirebaseDatabase.getInstance().getReference("groups")
            .child(groupId)
            .child("groupMessages")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .child("messages")
            .child(messageKey)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var message = snapshot.getValue(GroupMessage::class.java)
                    var sender = message?.sender
                    FirebaseDatabase.getInstance().getReference("user")
                        .child(sender.toString())
                        .child("name")
                        .get()
                        .addOnSuccessListener {
                            val name = it.value?.toString() ?: "Unknown"
                            callback(name)
                        }
                        .addOnFailureListener {
                            callback("Unknown")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback("Unknown")
                }

            })
    }

    fun startEditing(messageKey: String,message: String) {
        GroupChatActivity.editingkey = messageKey
        (context as? GroupChatActivity)?.editMessage(message)
        (context as? GroupChatActivity)?.EditTextUiChanges(message)
    }

    fun getMessageType(messageKey: String, groupId : String, callback: (String) -> Unit){
        FirebaseDatabase.getInstance().getReference("groups")
            .child(groupId)
            .child("groupMessages")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .child("messages")
            .child(messageKey)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var message = snapshot.getValue(GroupMessage::class.java)
                    callback(message?.messageType.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                    callback("Unknown")
                }

            })
    }
}