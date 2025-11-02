package com.example.android.myproject.Archive_Chat

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.example.android.myproject.Chat_Page.ChatActivity
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArchiveChatAdapter(
    var context: Context,
    var userList: ArrayList<User>,
    var viewModel : ArchiveChatViewModel
) : RecyclerView.Adapter<ArchiveChatAdapter.ViewHolder>() {

    private var listener : OnUserLongClickListener = context as OnUserLongClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_each_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.name.text = currentUser.name

        if(currentUser.profileUrl == ""){
            holder.contactProfile.setImageResource(R.drawable.default_profile_image)
        } else {
            Glide.with(context).load(currentUser.profileUrl).into(holder.contactProfile)
        }

        if (viewModel.selectList.value?.contains(currentUser.uid) == true) {
            holder.green_tick.visibility = View.VISIBLE
            val typedValue = TypedValue()
            val theme = holder.itemView.context.theme
            theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
            holder.contactLayout.setBackgroundColor(typedValue.data)
        }
        else {
            holder.green_tick.visibility = View.GONE
            holder.contactLayout.background = null
        }

        if (viewModel.isLongPressed.value != true) {
            holder.green_tick.visibility = View.GONE
            holder.contactLayout.background = null
        }

        var sender = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseDatabase.getInstance().getReference("chats").child(sender+currentUser.uid).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var message : Message? = null
                    for(user in snapshot.children){
                        message = user.getValue(Message::class.java)!!
                    }
                    if(message?.sender.toString() == currentUser.uid){

                        if(message?.messageType=="image") {
                            holder.receivedMessage.text = "🏙️ Image"
                        } else if(message?.messageType=="video"){
                            holder.receivedMessage.text = "🎥 Video"
                        } else if(message?.messageType=="document"){
                            holder.receivedMessage.text = "📄 Document"
                        } else if(message?.messageType=="audio"){
                            holder.receivedMessage.text = "🎧 Audio"
                        } else if(message?.messageType=="voiceRecord"){
                            holder.receivedMessage.text = "🎙️Voice Record"
                        }else {
                            holder.receivedMessage.text = message?.message.toString()
                        }

                        holder.sentMessage.visibility = View.GONE
                        holder.sentStatusImage.visibility = View.GONE
                        holder.receivedMessage.visibility = View.VISIBLE

                    }
                    else{

                        holder.sentMessage.visibility = View.VISIBLE
                        holder.sentStatusImage.visibility = View.VISIBLE
                        holder.receivedMessage.visibility = View.GONE

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
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Cancelled","Cancelled")
                }

            })

        FirebaseDatabase.getInstance().getReference().child("chats").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val containsChat = snapshot.children.any { it.key.toString().contains(currentUser.uid.toString()) }

                if (containsChat) {
                    Log.d("ChatCheck", "Chat found for ${currentUser.uid}")
                } else {
                    Log.d("ChatCheck", "No chat for ${currentUser.uid}")
                    return
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Cancelled","$error")
            }

        })


        Log.d("CurrentUser-", currentUser.name.toString())
        setCount(currentUser.uid.toString()) { count ->
            if (count > 0) {
                holder.count.text = count.toString()
                holder.notification.visibility = View.VISIBLE
            } else {
                holder.count.text = "0"
                holder.notification.visibility = View.GONE
            }
        }

        setTime(currentUser.uid.toString()) { time ->
            holder.time.text = time
        }

        holder.itemView.setOnClickListener {
            if(viewModel.isLongPressed.value == true){
                ArchiveChatActivity.count++
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
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("name", currentUser.name)
                intent.putExtra("uid", currentUser.uid)
                context.startActivity(intent)
            }
        }

        holder.itemView.setOnLongClickListener {

            if(viewModel.isLongPressed.value == false){
                viewModel.setIsLongPressed(true)
                viewModel.setSelectList(ArrayList())
            }

            val typedValue = TypedValue()
            val theme = holder.itemView.context.theme
            theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
            holder.contactLayout.setBackgroundColor(typedValue.data)

            holder.green_tick.visibility = View.VISIBLE
            listener.onUserLongClicked(position)

            true

        }

        val isSelected = viewModel.selectList.value?.contains(currentUser.uid) == true
        if (isSelected) {
            holder.green_tick.visibility = View.VISIBLE
            val typedValue = TypedValue()
            val theme = holder.itemView.context.theme
            theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
            holder.contactLayout.setBackgroundColor(typedValue.data)
        } else {
            holder.green_tick.visibility = View.GONE
            holder.contactLayout.background = null
        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name = itemView.findViewById<TextView>(R.id.contactTextView1)

        val time = itemView.findViewById<TextView>(R.id.sent_time)
        val count = itemView.findViewById<TextView>(R.id.messageCount)
        val notification = itemView.findViewById<LinearLayout>(R.id.notification)
        val green_tick = itemView.findViewById<LinearLayout>(R.id.green_tick)
        val contactLayout = itemView.findViewById<RelativeLayout>(R.id.contactLayout)
        val receivedMessage = itemView.findViewById<TextView>(R.id.contactLastReceiverMessage)
        val sentMessage = itemView.findViewById<TextView>(R.id.contactLastSenderMessage)
        val sentStatusImage = itemView.findViewById<ImageView>(R.id.sent_status_image)
        val contactProfile = itemView.findViewById<ShapeableImageView>(R.id.contact_profile)
    }

    fun setCount(receiveruid :String,callback: (Int) -> Unit) {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            callback(0)
            return
        }
        val senderRoom = currentUserId + receiveruid
        val receiverRoom = receiveruid + currentUserId

        val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom).child("messages")

        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var calculatedCount = 0
                val reversedMessages = snapshot.children.toList().reversed()
                for (messageSnap in reversedMessages) {
                    val message = messageSnap.getValue(Message::class.java)
                    if (message != null && message.sender == receiveruid) {
                        if (message.status == "received" || message.status == "sent") {
                            calculatedCount += 1
                        }
                        if (message.status == "seen") {
                            break
                        }
                    }
                }
                Log.d("Counts--", calculatedCount.toString())
                callback(calculatedCount)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to mark as seen: ${error.message}")
                callback(0)
            }

        })
    }

    fun setTime(receiveruid: String, callback: (String) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val room = receiveruid + currentUserId
        var latestTime: Date? = null
        var latestDate: String? = null
        var dayCount = Int.MAX_VALUE

        val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(room).child("messages")

        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val sdfDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                val todayStr = sdfDate.format(Date())

                val reversedMessages = snapshot.children.toList().reversed()
                for (messageSnap in reversedMessages) {
                    val message = messageSnap.getValue(Message::class.java)

                    if (message!=null) {
                        val msgTime = sdfTime.parse(message.time)
                        val diffDays = getDateDifference(message.date, todayStr)

                        if ((latestTime == null || msgTime?.after(latestTime) == true) && diffDays < dayCount) {
                            latestTime = msgTime
                            latestDate = message.date
                            dayCount = diffDays
                        }
                    }
                    if(message?.status=="seen"){
                        break
                    }
                }

                val result = when (dayCount) {
                    0 -> sdfTime.format(latestTime!!)
                    1 -> "Yesterday"
                    in 2..6 -> "$dayCount days ago"
                    else -> latestDate ?: ""
                }

                Log.d("Time", latestTime.toString())
                Log.d("Date", latestDate.toString())
                Log.d("Days", dayCount.toString())

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

    fun updateData(newList: List<User>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

}