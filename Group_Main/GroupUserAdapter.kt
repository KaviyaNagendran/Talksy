package com.example.android.myproject.Group_Main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Chat_Main.MainActivity
import com.example.android.myproject.DeleteAction.BasicOptionsFrgment
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.GroupChat_Page.GroupChatActivity
import com.example.android.myproject.Entities.GroupMessage
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.MediaViewer.ProfileViewer
import com.example.android.myproject.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class GroupUserAdapter(
    private val context: Context,
    private val groupList: java.util.ArrayList<Group>,
    var viewModel: GroupMainActivityViewModel,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<GroupUserAdapter.GroupViewHolder>() {

    private var listener : OnUserLongClickListener = context as OnUserLongClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_each_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val currentGroup = groupList[position]
        holder.name.text = currentGroup.groupname

        setLastMessageTime(currentGroup.groupuid.toString()) { time ->
            holder.time.text = time
        }

        if(viewModel.isLongPressed.value == false){
            holder.green_tick.visibility = View.GONE
            holder.contactLayout.background = null
        }

        if(currentGroup.groupProfileImageUrl != ""){
            Glide.with(context).load(currentGroup.groupProfileImageUrl).into(holder.contactProfile)
        }

        holder.contactProfile.setOnClickListener {
            if(currentGroup.groupProfileImageUrl == ""){
                Toast.makeText(context, "No profile image", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(context, ProfileViewer::class.java)
                intent.putExtra("profileUrl", currentGroup.groupProfileImageUrl)
                context.startActivity(intent)
            }
        }

        unReadCount(currentGroup.groupuid.toString()) { count ->
            if (count > 0) {
                holder.count.text = count.toString()
                holder.notification.visibility = View.VISIBLE
            } else {
                holder.count.text = "0"
                holder.notification.visibility = View.GONE
            }
        }

        var currentUser =  FirebaseAuth.getInstance().currentUser?.uid.toString()

        FirebaseDatabase.getInstance().getReference("groups").child(currentGroup.groupuid).child("groupMessages")
            .child(currentUser).child("messages").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var message : Message? = null
                    val messageList = snapshot.children.toList().reversed()
                    for (data in messageList) {
                        val msg = data.getValue(Message::class.java)
                        if (msg != null) {
                            message = msg
                            break
                        }
                    }

                    if(message?.sender.toString() == currentUser){

                        holder.receivedMessage.visibility = View.GONE
                        holder.sentMessage.visibility = View.VISIBLE
                        holder.sentStatusImage.visibility = View.VISIBLE
                        holder.messageSenderName.visibility = View.GONE

                        if(message?.messageType =="image") {
                            holder.sentMessage.text = "🏙️ Image"
                        } else if(message?.messageType =="video"){
                            holder.sentMessage.text = "🎥 Video"
                        } else if(message?.messageType=="document"){
                            holder.sentMessage.text = "📄 Document"
                        } else if(message?.messageType=="audio"){
                            holder.sentMessage.text = "🎧 Audio"
                        } else if(message?.messageType=="voiceRecord"){
                            holder.sentMessage.text = "🎙️Voice Record"
                        } else {
                            holder.sentMessage.text = message?.message.toString()
                        }

                        when (message?.status) {
                            "sent" -> holder.sentStatusImage.setImageResource(R.drawable.single_tick)
                            "received" -> holder.sentStatusImage.setImageResource(R.drawable.double_tick_grey)
                            "seen" -> holder.sentStatusImage.setImageResource(R.drawable.double_tick_blue)
                        }

                    }
                    else if(message != null){

                        holder.sentMessage.visibility = View.GONE
                        holder.sentStatusImage.visibility = View.GONE
                        holder.receivedMessage.visibility = View.VISIBLE

                        if(message?.messageType =="image") {
                            holder.receivedMessage.text = "🏙️ Image"
                        } else if(message?.messageType =="video"){
                            holder.receivedMessage.text = "🎥 Video"
                        } else if(message?.messageType=="document"){
                            holder.receivedMessage.text = "📄 Document"
                        } else if(message?.messageType=="audio"){
                            holder.receivedMessage.text = "🎧 Audio"
                        } else if(message?.messageType=="voiceRecord"){
                            holder.receivedMessage.text = "🎙️Voice Record"
                        } else {
                            holder.receivedMessage.text = message?.message.toString()
                        }

                        holder.messageSenderName.visibility = View.VISIBLE

                        FirebaseDatabase.getInstance().getReference("user").child(message?.sender.toString()).child("name")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.value.toString() == currentUser){
                                        holder.messageSenderName.visibility = View.GONE
                                    }
                                    else{
                                        holder.messageSenderName.text = snapshot.value.toString()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d("SenderName", "Failed to fetch sender name: ${error.message}")
                                }

                            })
                    }
                    else{
                        holder.sentMessage.visibility = View.GONE
                        holder.sentStatusImage.visibility = View.GONE
                        holder.receivedMessage.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Cancelled","Cancelled")
                }

            })

        holder.itemView.setOnClickListener {
            if(viewModel.isLongPressed.value == true){

                if(listener.onUserClicked(position)){
                    holder.green_tick.visibility = View.GONE
                    holder.contactLayout.background = null
                }
                else{
                    holder.green_tick.visibility = View.VISIBLE
                    listener.onUserLongClicked(position)
                    val typedValue = TypedValue()
                    val theme = holder.itemView.context.theme
                    theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                    holder.contactLayout.setBackgroundColor(typedValue.data)
                }

            } else {
                val intent = Intent(context, GroupChatActivity::class.java)
                intent.putExtra("groupuid", currentGroup.groupuid)
                intent.putExtra("groupname", currentGroup.groupname)
                intent.putExtra("groupProfileUrl",currentGroup.groupProfileImageUrl)
                intent.putStringArrayListExtra("groupMembers",
                    currentGroup.groupMembers as ArrayList<String?>?
                )
                context.startActivity(intent)
            }
        }

        holder.itemView.setOnLongClickListener {

            if(GroupMainActivity.IsSearchBarSelected){
                val dialog = BasicOptionsFrgment()
                val bundle = Bundle().apply {
                    putString("Is","group")
                    putString("receiver",groupList[position].groupuid)
                }
                dialog.arguments = bundle
                dialog.show(fragmentManager, "DeleteAction")
            }
            else {
                if (viewModel.isLongPressed.value !=true) {
                    viewModel.setIsLongPressed(true)
                    viewModel.setSelectedList(arrayListOf())
                }

                val typedValue = TypedValue()
                val theme = holder.itemView.context.theme
                theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                holder.contactLayout.setBackgroundColor(typedValue.data)

                holder.green_tick.visibility = View.VISIBLE
                listener.onUserLongClicked(position)
            }
            true
        }

        isArchived(currentGroup.groupuid.toString()) { isArchived ->
            if (isArchived) {
                holder.archiveNotification.visibility = View.VISIBLE
            } else {
                holder.archiveNotification.visibility = View.GONE
            }
        }

        val isSelected = viewModel.selectedList.value?.contains(currentGroup.groupuid) == true

        if (isSelected) {
            holder.green_tick.visibility = View.VISIBLE
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
            holder.contactLayout.setBackgroundColor(typedValue.data)
        } else {
            holder.green_tick.visibility = View.GONE
            holder.contactLayout.background = null
        }


    }

    override fun getItemCount(): Int = groupList.size

    fun unReadCount(groupUid: String, callback: (Int) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val messageRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupUid)
            .child("groupMessages")
            .child(currentUser)
            .child("messages")

        var count = 0

        messageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.toList().reversed()
                for(dataSnap in messages){
                    val message = dataSnap.getValue(GroupMessage::class.java)
                    Log.d("UnreadCount",message.toString())
                    if(message?.viewersList?.get(currentUser) == false && message.sender != currentUser){
                        count++
                    }
                    else if(message?.viewersList?.get(currentUser) == true || message?.viewersList?.get(currentUser) == null || message.sender==currentUser){
                        break
                    }
                }
                Log.d("UnreadCount", count.toString())
                callback(count)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.GroupName)
        val time: TextView = itemView.findViewById(R.id.sent_time_group)
        val count = itemView.findViewById<TextView>(R.id.messageCount_group)
        val notification = itemView.findViewById<LinearLayout>(R.id.notification_group)
        val green_tick = itemView.findViewById<LinearLayout>(R.id.green_tick_group)
        val contactLayout = itemView.findViewById<RelativeLayout>(R.id.groupLayout)
        val receivedMessage = itemView.findViewById<TextView>(R.id.contactLastReceiverMessage_group)
        val sentMessage = itemView.findViewById<TextView>(R.id.contactLastSenderMessage_group)
        val sentStatusImage = itemView.findViewById<ImageView>(R.id.sent_status_image_group)
        val contactProfile = itemView.findViewById<ShapeableImageView>(R.id.group_profile)
        val archiveNotification = itemView.findViewById<TextView>(R.id.archive_notification_group)
        val messageSenderName = itemView.findViewById<TextView>(R.id.message_sender_name_group)
        var isSelected = false
    }

    private fun setLastMessageTime(groupId: String, callback: (String) -> Unit) {
        val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val sdfDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val todayStr = sdfDate.format(Date())
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val messagesRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupMessages")
            .child(currentUser)
            .child("messages")

        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var latestMessageTime: String? = null
                var latestMessageDate: String? = null

                for (messageSnap in snapshot.children.reversed()) {
                    val message = messageSnap.getValue(Message::class.java)
                    latestMessageTime = message?.time
                    latestMessageDate = message?.date
                    break
                }

                val dayDiff = getDateDifference(latestMessageDate ?: "", todayStr)

                val result = when (dayDiff) {
                    0 -> latestMessageTime ?: ""
                    1 -> "Yesterday"
                    in 2..6 -> "$dayDiff days ago"
                    else -> latestMessageDate ?: ""
                }

                callback(result)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch time: ${error.message}")
                callback("")
            }
        })
    }

    fun isArchived(groupUid: String, onResult: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupUid)
            .child("groupMessages")
            .child(currentUserId)
            .child("archive")
            .get()
            .addOnSuccessListener {
                val flag = it.getValue(Boolean::class.java) == true
                Log.d("isArchived-1",flag.toString())
                onResult(flag)
            }
            .addOnFailureListener {
                Log.d("isArchived-2","false")
                onResult(false)
            }
    }

    private fun getDateDifference(dateStr1: String, dateStr2: String): Int {
        return try {
            val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val date1 = sdf.parse(dateStr1)
            val date2 = sdf.parse(dateStr2)
            val diffInMillis = date2!!.time - date1!!.time
            (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }
}