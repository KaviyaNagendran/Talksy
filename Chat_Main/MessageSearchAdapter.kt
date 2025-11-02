package com.example.android.myproject.Chat_Main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.android.myproject.Chat_Page.ChatActivity
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.example.android.myproject.Listeners.CallBackListenerMessageSearch
import com.example.android.myproject.Listeners.MultipleMessageSelectListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageSearchAdapter(val context: Context, val messageList: ArrayList<Message>,var viewModel : ViewModel) : RecyclerView.Adapter<MessageSearchAdapter.MessageHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.search_main_layout, parent, false)
        return MessageHolder(view)
    }

    private var callBackListenerMessageSearch = context as CallBackListenerMessageSearch

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {

        val currentMessage = messageList[position]

        holder.searchMessage.visibility= View.VISIBLE
        holder.message_sender_name.visibility = View.GONE
        holder.searchMessage_group.visibility = View.GONE
        holder.searchMessageSentdate.text = currentMessage.date
        holder.searchMessageSenttime.text = currentMessage.time

        if(currentMessage.messageType=="image" ){
            holder.searchMessage.text = "🏙️ Image"
        }
        else if(currentMessage.messageType=="video") {
            holder.searchMessage.text = "🎥 Video"
        }
        else if(currentMessage?.messageType=="document"){
            holder.searchMessage.text = "📄 Document"
        }
        else if(currentMessage?.messageType=="audio"){
            holder.searchMessage.text = "🎧 Audio"
        }
        else if(currentMessage?.messageType=="voiceRecord"){
            holder.searchMessage.text = "🎙️Voice Record"
        }
        else{
            holder.searchMessage.text = currentMessage.message
        }


        val senderRef = FirebaseDatabase.getInstance().getReference("user")
            .child(currentMessage.sender ?: return)

        senderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                holder.searchName.text = user?.name ?: "Unknown"
            }

            override fun onCancelled(error: DatabaseError) {
                holder.searchName.text = "Unknown"
            }
        })

        holder.itemView.setOnClickListener {
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            val uid = if (currentMessage.sender == currentUserUid) {
                currentMessage.receiver.toString()
            } else {
                currentMessage.sender.toString()
            }

            getUserName(uid) { name ->
                getProfileUrl(uid) { profileUrl ->
                    Log.d("Search",name.toString())
                    Log.d("Search",profileUrl.toString())
                    val context = holder.itemView.context
                    val intent = Intent(context, ChatActivity::class.java).apply {
                        putExtra("REPLY_TO_MESSAGE_KEY", currentMessage.messageKey)
                        putExtra("name", name)
                        putExtra("uid", uid)
                        putExtra("profileUrl", profileUrl)
                    }
                    context.startActivity(intent)
                    callBackListenerMessageSearch.ResetFilters()
                }
            }
        }

    }

    fun getProfileUrl(uid: String, callback: (String?) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("user").child(uid)
        userRef.child("profileUrl").get()
            .addOnSuccessListener { snapshot ->
                val profile = snapshot.getValue(String::class.java)
                Log.d("Search", "Fetched name for $uid: $profile")
                callback(profile)
            }
            .addOnFailureListener {
                Log.e("Search", "Failed to fetch name for $uid", it)
                callback(null)
            }
    }


    fun getUserName(uid: String, callback: (String?) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("user").child(uid)
        userRef.child("name").get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java)
                Log.d("Search", "Fetched name for $uid: $name")
                callback(name)
            }
            .addOnFailureListener {
                Log.e("Search", "Failed to fetch name for $uid", it)
                callback(null)
            }
    }



    class MessageHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var searchName = itemView.findViewById<TextView>(R.id.searchName)
        var searchMessage = itemView.findViewById<TextView>(R.id.searchMessage)
        var searchMessageSentdate = itemView.findViewById<TextView>(R.id.searchMessageSentDate)
        var searchMessageSenttime = itemView.findViewById<TextView>(R.id.searchMessageSentTime)
        var message_sender_name = itemView.findViewById<TextView>(R.id.message_sender_name)
        var searchMessage_group = itemView.findViewById<TextView>(R.id.searchMessage_group)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}