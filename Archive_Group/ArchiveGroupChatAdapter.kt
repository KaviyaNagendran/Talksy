package com.example.android.myproject.Archive_Group

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Archive_Chat.ArchiveChatActivity
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.GroupChat_Page.GroupChatActivity
import com.example.android.myproject.Entities.GroupMessage
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.Group_Main.GroupMainActivity
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArchiveGroupChatAdapter(
    var context: Context,
    var groupList: ArrayList<Group>,
    var viewModel: ArchiveGroupChatViewModel
) : RecyclerView.Adapter<ArchiveGroupChatAdapter.ViewHolder>() {

    private var listener : OnUserLongClickListener = context as OnUserLongClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_each_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentGroup = groupList[position]
        holder.name.text = currentGroup.groupname

        val isSelected = viewModel.selectList.value?.contains(currentGroup.groupuid) == true
        if (isSelected) {
            holder.green_tick.visibility = View.VISIBLE
            val typedValue = TypedValue()
            val theme = holder.itemView.context.theme
            theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
            holder.groupLayout.setBackgroundColor(typedValue.data)
        } else {
            holder.green_tick.visibility = View.GONE
            holder.groupLayout.background = null
        }

        if(currentGroup.groupProfileImageUrl != "") {
            Glide.with(context).load(currentGroup.groupProfileImageUrl).into(holder.contactProfile)
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

                        when (message?.status) {
                            "sent" -> holder.sentStatusImage.setImageResource(R.drawable.single_tick)
                            "received" -> holder.sentStatusImage.setImageResource(R.drawable.double_tick_grey)
                            "seen" -> holder.sentStatusImage.setImageResource(R.drawable.double_tick_blue)
                        }

                    }
                    else{
                        holder.sentMessage.visibility = View.GONE
                        holder.sentStatusImage.visibility = View.GONE
                        holder.receivedMessage.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        setCount(currentGroup.groupuid.toString()) { count ->
            if (count > 0) {
                holder.count.text = count.toString()
                holder.notification.visibility = View.VISIBLE
            } else {
                holder.count.text = "0"
                holder.notification.visibility = View.GONE
            }
        }

        setTime(currentGroup.groupuid.toString()) { time ->
            holder.time.text = time
        }

        holder.itemView.setOnClickListener {

            if(viewModel.isLongPressed.value == true){

                if(listener.onUserClicked(position)){
                    holder.green_tick.visibility = View.GONE
                    holder.groupLayout.background = null
                }
                else{
                    holder.green_tick.visibility = View.VISIBLE
                    listener.onUserLongClicked(position)
                    val typedValue = TypedValue()
                    val theme = holder.itemView.context.theme
                    theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                    holder.groupLayout.setBackgroundColor(typedValue.data)
                }

            } else {
                val intent = Intent(context, GroupChatActivity::class.java)
                intent.putExtra("groupuid", currentGroup.groupuid)
                intent.putExtra("groupname", currentGroup.groupname)
                intent.putStringArrayListExtra("groupMembers",
                    currentGroup.groupMembers as java.util.ArrayList<String?>?
                )
                intent.putExtra("groupProfileUrl",currentGroup.groupProfileImageUrl)
                context.startActivity(intent)

            }
        }

        holder.itemView.setOnLongClickListener {

            if (viewModel.isLongPressed.value == false) {
                viewModel.setIsLongPressed(true)
                viewModel.setSelectList(emptyList())
            }

            val typedValue = TypedValue()
            val theme = holder.itemView.context.theme
            theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
            holder.groupLayout.setBackgroundColor(typedValue.data)

            holder.green_tick.visibility = View.VISIBLE
            listener.onUserLongClicked(position)

            true
        }
    }


    override fun getItemCount(): Int {
        return groupList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.GroupName)
        val time = itemView.findViewById<TextView>(R.id.sent_time_group)
        val count = itemView.findViewById<TextView>(R.id.messageCount_group)
        val notification = itemView.findViewById<LinearLayout>(R.id.notification_group)
        val green_tick = itemView.findViewById<LinearLayout>(R.id.green_tick_group)
        val groupLayout = itemView.findViewById<RelativeLayout>(R.id.groupLayout)
        val receivedMessage = itemView.findViewById<TextView>(R.id.contactLastReceiverMessage_group)
        val sentMessage = itemView.findViewById<TextView>(R.id.contactLastSenderMessage_group)
        val sentStatusImage = itemView.findViewById<ImageView>(R.id.sent_status_image_group)
        val contactProfile = itemView.findViewById<ShapeableImageView>(R.id.group_profile)
        val archiveNotification = itemView.findViewById<TextView>(R.id.archive_notification_group)
        val messageSenderName = itemView.findViewById<TextView>(R.id.message_sender_name_group)
        var isSelected = false
    }

    fun setCount(groupId: String, callback: (Int) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val messageRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
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

    fun setTime(groupId: String, callback: (String) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val groupMessagesRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupMessages")
            .child(currentUserId)
            .child("messages")

        val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val sdfDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val todayStr = sdfDate.format(Date())

        var latestTime: Date? = null
        var latestDate: String? = null
        var dayCount = Int.MAX_VALUE

        groupMessagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reversedMessages = snapshot.children.toList().reversed()
                for (senderSnapshot in reversedMessages) {
                    val value = senderSnapshot.value
                    if (value is Map<*, *> && value.containsKey("sender")) {
                        val message = senderSnapshot.getValue(GroupMessage::class.java)

                        if (message != null) {
                            val msgTime = try {
                                sdfTime.parse(message.time ?: "")
                            } catch (e: Exception) {
                                null
                            }

                            val diffDays = getDateDifference(message.date, todayStr)

                            if ((latestTime == null || (msgTime != null && msgTime.after(latestTime))) && diffDays < dayCount) {
                                latestTime = msgTime
                                latestDate = message.date
                                dayCount = diffDays
                            }

                            if (message.status == "seen") {
                                break
                            }
                        }
                    }
                }

                val result = when {
                    dayCount == 0 && latestTime != null -> sdfTime.format(latestTime)
                    dayCount == 1 -> "Yesterday"
                    dayCount in 2..6 -> "$dayCount days ago"
                    else -> latestDate ?: ""
                }

                callback(result)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to get time: ${error.message}")
                callback("")
            }
        })
    }

    fun getDateDifference(dateStr1: String, dateStr2: String): Int {
        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val date1 = sdf.parse(dateStr1)
        val date2 = sdf.parse(dateStr2)
        val diffInMillis = date2.time - date1.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
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

}