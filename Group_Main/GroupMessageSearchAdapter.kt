package com.example.android.myproject.Group_Main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.myproject.GroupChat_Page.GroupChatActivity
import com.example.android.myproject.Entities.*
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.example.android.myproject.Listeners.CallBackListenerMessageSearch
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupMessageSearchAdapter(
    val context: Context,
    val messageList: ArrayList<GroupMessage>,
    viewModel: GroupMainActivityViewModel
) : RecyclerView.Adapter<GroupMessageSearchAdapter.MessageHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.search_main_layout, parent, false)
        return MessageHolder(view)
    }

    private var callBackListenerMessageSearch = context as CallBackListenerMessageSearch

    override fun onBindViewHolder(
        holder: MessageHolder,
        position: Int
    ) {
        holder.searchMessage.visibility= View.GONE
        holder.searchMessage_group.visibility= View.VISIBLE
        holder.message_sender_name.visibility= View.VISIBLE

        val currentMessage = messageList[position]
        holder.searchMessageSentdate.text = currentMessage.date
        holder.searchMessageSenttime.text = currentMessage.time
        holder.searchMessage_group.text = currentMessage.message

        if(currentMessage.messageType=="image" ){
            holder.searchMessage_group.text = "🏙️ Image"
        }
        else if(currentMessage.messageType=="video") {
            holder.searchMessage_group.text = "🎥 Video"
        }
        else if(currentMessage?.messageType=="document"){
            holder.searchMessage_group.text = "📄 Document"
        }
        else if(currentMessage?.messageType=="audio"){
            holder.searchMessage_group.text = "🎧 Audio"
        }
        else if(currentMessage?.messageType=="voiceRecord"){
            holder.searchMessage_group.text = "🎙️Voice Record"
        }
        else{
            holder.searchMessage_group.text = currentMessage.message
        }

        val groupref = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(currentMessage.groupUid.toString())
            .child("groupDetails").child(currentMessage.groupUid.toString()).child("groupname")

        Log.d("GroupRef", "Group UID: ${GroupChatActivity.Companion.groupUid}")

        groupref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupname = snapshot.getValue(String::class.java)
                holder.searchName.text = groupname ?: "Unknown"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("GroupRef", "Cancelled: ${error.message}")
            }
        })

        val senderRef = FirebaseDatabase.getInstance().getReference("user")
            .child(currentMessage.sender ?: return)

        senderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                holder.message_sender_name.text = user?.name ?: "Unknown"
            }
            override fun onCancelled(error: DatabaseError) {
                holder.message_sender_name.text = "Unknown"
            }
        })

        holder.itemView.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("groups").child(currentMessage.groupUid.toString())
                .child("groupDetails").child(currentMessage.groupUid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var currentGroup = snapshot.getValue(Group::class.java)
                        val intent = Intent(context, GroupChatActivity::class.java)
                        intent.putExtra("REPLY_TO_MESSAGE_KEY", currentMessage.messageKey)
                        intent.putExtra("groupuid", currentMessage.groupUid)
                        intent.putExtra("groupname", currentGroup?.groupname)
                        intent.putExtra("groupProfileUrl",currentGroup?.groupProfileImageUrl)
                        intent.putStringArrayListExtra("groupMembers",
                            currentGroup?.groupMembers as java.util.ArrayList<String?>?
                        )
                        context.startActivity(intent)
                        callBackListenerMessageSearch.ResetFilters()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Cancelled","Cancelled")
                    }

                })
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