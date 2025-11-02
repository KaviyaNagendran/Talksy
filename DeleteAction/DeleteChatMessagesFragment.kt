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
import com.example.android.myproject.Chat_Page.ChatActivity
import com.example.android.myproject.Chat_Page.MessageAdapter
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.Forward_Message.UserAndGroupListActivity
import com.example.android.myproject.Listeners.MultipleMessageSelectListener
import com.example.android.myproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteChatMessagesFragment : BottomSheetDialogFragment() {

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
        val receiver = arguments?.getString("receiver")
        val message = arguments?.getString("message")
        val messageType = arguments?.getString("messageType")
        val timeStamp = arguments?.getLong("messageTime")

        if(person == "receiver" || messageType == "image" || messageType == "video" || messageType == "document" || messageType == "voiceRecord" || messageType == "audio"){
            view.findViewById<RelativeLayout>(R.id.EditMessage).visibility = View.GONE
        }
        else{
            view.findViewById<RelativeLayout>(R.id.EditMessage).visibility = View.VISIBLE
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
            Log.d("isWithinFiveMinutes", "chat")
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
                putString("IsChats", true.toString())
                putString("messageKey", messageKey)
                putLong("messageTime", timeStamp!!)
            }
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "DeleteAction")
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.CopyMessage).setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
            var room = currentUser + receiver

            FirebaseDatabase.getInstance().getReference("chats").child(room).child("messages")
                .child(messageKey.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var message = snapshot.getValue(Message::class.java)
                        val clip = ClipData.newPlainText("Copied Message", message?.message)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Cancelled","$error")
                    }

                })
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.ForwardMessage).setOnClickListener {
            val intent = Intent(context, UserAndGroupListActivity::class.java)
            intent.putExtra("message",message)
            intent.putExtra("messageType",messageType)
            intent.putExtra("messageKey",messageKey)
            intent.putExtra("receiverUid",receiver)
            intent.putExtra("person","chats")
            startActivity(intent)
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.ReplyMessage).setOnClickListener {
            if (person == "receiver") {
                Toast.makeText(context, person, Toast.LENGTH_SHORT).show()

                getName(receiver.toString()) { name ->
                    var messages = ""
                    getReceiver { activeReceiver ->
                        getMessageType(messageKey.toString(), activeReceiver) { messageType ->
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
                            (context as? ChatActivity)?.replyUiChanges(
                                messages,
                                name,
                                messageType.toString(),
                                messageKey.toString()
                            )
                            dismiss()
                        }
                    }

                }
            } else {
                var messages = ""
                getReceiver { activeReceiver ->
                    getMessageType(messageKey.toString(), activeReceiver) { messageType ->
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
                        (context as? ChatActivity)?.replyUiChanges(
                            messages,
                            "You",
                            messageType.toString(),
                            messageKey.toString()
                        )
                        dismiss()
                    }
                }
            }
        }

        view.findViewById<RelativeLayout>(R.id.EnableMultipleSelect).setOnClickListener {
            MessageAdapter.IsMultipleSelect = true
            ChatActivity.SelectMultipleMessages.add(messageKey.toString())
            listener?.onMultiSelectStarted()
            Log.d("EnableMultipleUsers-inside","true")
            dismiss()
        }

    }

    fun isWithinFiveMinutes(timestamp1: Long, timestamp2: Long): Boolean {
        val diffInMillis = kotlin.math.abs(timestamp1 - timestamp2)
        return diffInMillis < 5 * 60 * 1000
    }
    fun getName(senderUid: String, callback: (String) -> Unit) {
        FirebaseDatabase.getInstance()
            .getReference("user")
            .child(senderUid)
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

    fun getMessageType(messageId: String, receiverUid: String, callback: (String) -> Unit) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val chatId = currentUserUid + receiverUid

        val ref = FirebaseDatabase.getInstance()
            .getReference("chats")
            .child(chatId)
            .child("messages")
            .child(messageId)

        Log.d("MessageIssue", "chatId = $chatId")
        Log.d("MessageIssue", "messageId = $messageId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val message = snapshot.child("messageType").getValue(String::class.java)

                Log.d("MessageIssue", "Fetched message: $message")

                if (!message.isNullOrEmpty()) {
                    callback(message)
                } else {
                    callback("MessageType deleted")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("getMessage", "Cancelled: ${error.message}")
                callback("MessageType unavailable")
            }
        })
    }
    fun startEditing(messageKey: String,message: String) {
        ChatActivity.editingkey = messageKey
        (context as? ChatActivity)?.editMessage(message)
        (context as? ChatActivity)?.EditTextUiChanges(message)
    }

    fun getReceiver(callback: (String) -> Unit) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val receiverRef = FirebaseDatabase.getInstance()
            .getReference("user")
            .child(currentUserUid)
            .child("activeChatUid")

        receiverRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val receiverUid = snapshot.getValue(String::class.java)
                if (receiverUid != null) {
                    callback(receiverUid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Cancelled","cancelled")
            }
        })
    }

}