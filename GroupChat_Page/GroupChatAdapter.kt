package com.example.android.myproject.GroupChat_Page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Chat_Page.ChatActivity
import com.example.android.myproject.DeleteAction.DeleteGroupChatMessagesFragment
import com.example.android.myproject.Entities.GroupMessage
import com.example.android.myproject.MediaViewer.ImageVideoViewer
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.Listeners.ReplyHighlightListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.iterator

class GroupChatAdapter(val context: Context, val messageList: ArrayList<GroupMessage>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val item_receive = 1
    val item_send = 2
    val item_send_reply = 3
    val item_receive_reply = 4
    var replyHighlightListener: ReplyHighlightListener = context as ReplyHighlightListener
    private var listener : OnUserLongClickListener = context as OnUserLongClickListener

    companion object {
        lateinit var currentHolder: RecyclerView.ViewHolder
        lateinit var currentMessageObject : GroupMessage
        var IsMultipleSelect = false
        var olddate = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            item_receive -> {
                val view = LayoutInflater.from(context).inflate(R.layout.activity_receive_group_message, parent, false)
                GroupReceiveViewHolder(view)
            }
            item_send -> {
                val view = LayoutInflater.from(context).inflate(R.layout.activity_send_group_message, parent, false)
                GroupSentViewHolder(view)
            }
            item_send_reply -> {
                val view = LayoutInflater.from(context).inflate(R.layout.activity_send_message_reply, parent, false)
                GroupSentReplyViewHolder(view)
            }
            item_receive_reply -> {
                val view = LayoutInflater.from(context).inflate(R.layout.activity_receive_group_message_reply, parent, false)
                GroupReceiveReplyViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        currentHolder = holder
        currentMessageObject = messageList[position]

        val currentMessage = messageList[position]
        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

        val messageDateStr = currentMessage.date
        val messageDate: Date = sdf.parse(messageDateStr) ?: Date()
        val formattedMessageDateStr = sdf.format(messageDate)

        val todayStr = sdf.format(Date())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val yesterdayStr = sdf.format(calendar.time)

        val shouldShowDateHeader = position == 0 || run {
            val prevMessage = messageList[position - 1]
            val prevDate = sdf.parse(prevMessage.date) ?: Date()
            val prevFormatted = sdf.format(prevDate)
            prevFormatted != formattedMessageDateStr
        }

        if (holder is GroupSentViewHolder) {
            if (shouldShowDateHeader) {
                val displayDate = when (formattedMessageDateStr) {
                    todayStr -> "Today"
                    yesterdayStr -> "Yesterday"
                    else -> formattedMessageDateStr
                }

                holder.dateValue.text = displayDate
                holder.date.visibility = View.VISIBLE
            } else {
                holder.date.visibility = View.GONE
            }

            holder.sentTime.text = currentMessage.time

            when (currentMessage.messageType) {
                "image" -> {
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentAudioMessageLayout.visibility = View.GONE
                    holder.sentDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sentImageMessagelayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sentImageMessagelayout.visibility = View.VISIBLE
                    }

                    Glide.with(context).load(currentMessage.message).into(holder.sentImageMessage)
                }

                "video" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentAudioMessageLayout.visibility = View.GONE
                    holder.sentDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sentVideoMessagelayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sentVideoMessagelayout.visibility = View.VISIBLE
                    }

                    if(currentMessage.message!="") {
                        holder.sentVideoMessage.setVideoURI(currentMessage.message?.toUri())
                    }
                }

                "voiceRecord" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sentAudioMessageLayout.visibility = View.GONE
                    holder.sentDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sendvoiceRecordMessageLayout.visibility = View.VISIBLE

                        handlevoiceRecordPlayback(
                            context = holder.itemView.context,
                            voiceRecordUrl = currentMessage.message.toString(),
                            playButton = holder.play,
                            pauseButton = holder.pause,
                            seekBar = holder.voiceRecordSeekBar,
                            playingTimeText = holder.playingTime,
                            totalTimeText = holder.totalTime,
                            currentMessage = currentMessage
                        )

                    }

                }
                "audio" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sentAudioMessageLayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sentAudioMessageLayout.visibility = View.VISIBLE

                        handlevoiceRecordPlayback(
                            context = holder.itemView.context,
                            voiceRecordUrl = currentMessage.message.toString(),
                            playButton = holder.playAudio,
                            pauseButton = holder.pauseAudio,
                            seekBar = holder.voiceRecordSeekBarAudio,
                            playingTimeText = holder.playingTimeAudio,
                            totalTimeText = holder.totalTimeAudio,
                            currentMessage = currentMessage
                        )
                    }
                }

                "document" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentAudioMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sentDocumentMessageLayout.visibility = View.GONE
                    }
                    else {
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sentDocumentMessageLayout.visibility = View.VISIBLE

                        holder.documentName.text = currentMessage.fileName

                        holder.document_page_count.text = currentMessage.pageCount
                        if(currentMessage.pageCount == "1"){
                            holder.document_page.text = "page"
                        }
                        else{
                            holder.document_page.text = "pages"
                        }

                        holder.document_size.text = currentMessage.fileSize
                        holder.document_type.text = currentMessage.fileType
                        if (currentMessage.fileType == "PDF") {
                            holder.documentImage.setImageResource(R.drawable.pdf_icon)
                        } else if (currentMessage.fileType == "TXT") {
                            holder.documentImage.setImageResource(R.drawable.txt_icon)
                        } else if (currentMessage.fileType == "DOC" || currentMessage.fileType == "DOCX") {
                            holder.documentImage.setImageResource(R.drawable.doc_icon)
                        } else {
                            holder.documentImage.setImageResource(R.drawable.document)
                        }

                    }
                }

                else -> {
                    holder.sentMessage.visibility = View.VISIBLE
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentProgressBar.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentAudioMessageLayout.visibility = View.GONE
                    holder.sentDocumentMessageLayout.visibility = View.GONE

                    if (currentMessage.message == "This message was Deleted!!!") {
                        holder.sentMessage.textSize = 12f
                        holder.sentMessage.setTypeface(null, Typeface.ITALIC)
                        holder.sentMessage.text = currentMessage.message
                    } else {
                        holder.sentMessage.textSize = 18f
                        holder.sentMessage.setTypeface(null, Typeface.NORMAL)
                        holder.sentMessage.text = currentMessage.message
                    }
                    if(currentMessage.edited){
                        holder.sentEdited.visibility = View.VISIBLE
                    }
                    else{
                        holder.sentEdited.visibility = View.GONE
                    }
                }
            }

            when (currentMessage.status) {
                "sent" -> holder.sent_status_image.setImageResource(R.drawable.single_tick)
                "received" -> holder.sent_status_image.setImageResource(R.drawable.double_tick_grey)
                "seen" -> holder.sent_status_image.setImageResource(R.drawable.double_tick_blue)
            }

            if(currentMessage.forwarded){
                holder.sentForwarded.visibility = View.VISIBLE
            }
            else{
                holder.sentForwarded.visibility = View.GONE
            }

            holder.itemView.setOnLongClickListener { view ->
                if (GroupChatActivity.IsNoLongerMember) {
                    Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                }
                else {
                    val realPosition = holder.adapterPosition
                    if (realPosition != RecyclerView.NO_POSITION) {

                        val dialog = DeleteGroupChatMessagesFragment()

                        val bundle = Bundle().apply {
                            putString("messageBy", "sender")
                            putString("messageKey", messageList[position].messageKey)
                            putString("message", messageList[position].message)
                            putString("messageType", messageList[position].messageType)
                            putLong("messageTime", (messageList[position].timeStamp))
                        }

                        dialog.arguments = bundle

                        dialog.show(fragmentManager, "DeleteAction")

                        notifyItemChanged(position)
                    }
                }
                true
            }


            if (currentMessage.edited) {
                holder.sentEdited.visibility = View.VISIBLE
            } else {
                holder.sentEdited.visibility = View.GONE
            }

            if(GroupChatActivity.SelectMultipleMessages.contains(messageList[position].messageKey)){
                val typedValue = TypedValue()
                val theme = holder.itemView.context.theme
                theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                holder.sentLayout.setBackgroundColor(typedValue.data)
            }
            else{
                holder.sentLayout.background = null
            }


            holder.itemView.setOnClickListener {
                Log.d("IsMultipleSelect", IsMultipleSelect.toString())
                if (GroupChatActivity.IsNoLongerMember) {
                    Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                } else{
                    if (IsMultipleSelect) {
                        if (listener.onUserClicked(position)) {
                            holder.sentLayout.background = null
                        } else {
                            val typedValue = TypedValue()
                            val theme = holder.itemView.context.theme
                            theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                            holder.sentLayout.setBackgroundColor(typedValue.data)
                        }
                    } else if (!IsMultipleSelect && messageList[position].messageType == "image" || messageList[position].messageType == "video") {

                        val realPosition = holder.adapterPosition
                        if (realPosition != RecyclerView.NO_POSITION) {
                            val intent = Intent(context, ImageVideoViewer::class.java)
                            intent.putExtra("messageKey", messageList[realPosition].messageKey)
                            intent.putExtra("messageUrl", messageList[realPosition].message)
                            intent.putExtra("messageType", messageList[realPosition].messageType)
                            intent.putExtra("messageDate", messageList[realPosition].date)
                            intent.putExtra("messageTime", messageList[realPosition].time)
                            intent.putExtra("senderUid", messageList[realPosition].sender)
                            context.startActivity(intent)
                        }

                    }
                }
                true
            }

        }

        else if (holder is GroupReceiveViewHolder) {
            if (shouldShowDateHeader) {
                val displayDate = when (formattedMessageDateStr) {
                    todayStr -> "Today"
                    yesterdayStr -> "Yesterday"
                    else -> formattedMessageDateStr
                }

                holder.dateValue.text = displayDate
                holder.date.visibility = View.VISIBLE
            } else {
                holder.date.visibility = View.GONE
            }

            holder.receiveTime.text = currentMessage.time

            if(currentMessage.forwarded){
                holder.receivedForwarded.visibility = View.VISIBLE
            }
            else{
                holder.receivedForwarded.visibility = View.GONE
            }

            FirebaseDatabase.getInstance().getReference("user")
                .child(currentMessage.sender.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentUser = snapshot.getValue(User::class.java)
                        holder.senderName.text = currentUser?.name ?: "Unknown"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to load user: ${error.message}")
                    }
                })


            if (holder is GroupReceiveViewHolder) {
                holder.receiveTime.text = currentMessage.time

                when (currentMessage.messageType) {
                    "image" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receivedAudioMessageLayout.visibility = View.GONE
                        holder.receivedDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivedImageMessagelayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivedImageMessagelayout.visibility = View.VISIBLE
                        }

                        Glide.with(context).load(currentMessage.message).into(holder.receivedImageMessage)
                    }
                    "video" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receivedAudioMessageLayout.visibility = View.GONE
                        holder.receivedDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivedVideoMessagelayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivedVideoMessagelayout.visibility = View.VISIBLE
                        }

                        if(currentMessage.message!="") {
                            holder.receivedVideoMessage.setVideoURI(currentMessage.message?.toUri())
                        }
                    }

                    "voiceRecord" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receivedAudioMessageLayout.visibility = View.GONE
                        holder.receivedDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivevoiceRecordMessageLayout.visibility = View.VISIBLE
                            handlevoiceRecordPlayback(
                                context = holder.itemView.context,
                                voiceRecordUrl = currentMessage.message.toString(),
                                playButton = holder.play,
                                pauseButton = holder.pause,
                                seekBar = holder.voiceRecordSeekBar,
                                playingTimeText = holder.playingTime,
                                totalTimeText = holder.totalTime,
                                currentMessage = currentMessage
                            )
                        }
                    }

                    "audio" -> {
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receivedDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivedAudioMessageLayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivedAudioMessageLayout.visibility = View.VISIBLE

                            handlevoiceRecordPlayback(
                                context = holder.itemView.context,
                                voiceRecordUrl = currentMessage.message.toString(),
                                playButton = holder.playAudio,
                                pauseButton = holder.pauseAudio,
                                seekBar = holder.voiceRecordSeekBarAudio,
                                playingTimeText = holder.playingTimeAudio,
                                totalTimeText = holder.totalTimeAudio,
                                currentMessage = currentMessage
                            )
                        }
                    }

                    "document" -> {
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receivedAudioMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivedDocumentMessageLayout.visibility = View.GONE
                        }
                        else {
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivedDocumentMessageLayout.visibility = View.VISIBLE

                            holder.documentName.text = currentMessage.fileName

                            holder.document_page_count.text = currentMessage.pageCount
                            if(currentMessage.pageCount == "1"){
                                holder.document_page.text = "page"
                            }
                            else{
                                holder.document_page.text = "pages"
                            }

                            holder.document_size.text = currentMessage.fileSize
                            holder.document_type.text = currentMessage.fileType
                            if (currentMessage.fileType == "PDF") {
                                holder.documentImage.setImageResource(R.drawable.pdf_icon)
                            } else if (currentMessage.fileType == "TXT") {
                                holder.documentImage.setImageResource(R.drawable.txt_icon)
                            } else if (currentMessage.fileType == "DOC" || currentMessage.fileType == "DOCX") {
                                holder.documentImage.setImageResource(R.drawable.doc_icon)
                            } else {
                                holder.documentImage.setImageResource(R.drawable.document)
                            }

                        }
                    }

                    else -> {
                        holder.receiveMessage.visibility = View.VISIBLE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receivedProgressBar.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receivedAudioMessageLayout.visibility = View.GONE
                        holder.receivedDocumentMessageLayout.visibility = View.GONE

                        if (currentMessage.message == "This message was Deleted!!!") {
                            holder.receiveMessage.textSize = 12f
                            holder.receiveMessage.setTypeface(null, Typeface.ITALIC)
                            holder.receiveMessage.text = currentMessage.message
                        } else {
                            holder.receiveMessage.setTypeface(null, Typeface.NORMAL)
                            holder.receiveMessage.textSize = 18f
                            holder.receiveMessage.text = currentMessage.message
                        }
                        if(currentMessage.edited){
                            holder.receivedEdited.visibility = View.VISIBLE
                        }
                        else{
                            holder.receivedEdited.visibility = View.GONE
                        }
                    }
                }

                holder.itemView.setOnLongClickListener {
                    if (GroupChatActivity.IsNoLongerMember) {
                        Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                    } else {
                        val dialog = DeleteGroupChatMessagesFragment()

                        val bundle = Bundle().apply {
                            putString("messageBy", "receiver")
                            putString("messageKey", messageList[position].messageKey)
                            putString("message", messageList[position].message)
                            putString("messageType", messageList[position].messageType)
                            putLong("messageTime", (messageList[position].timeStamp))
                        }

                        dialog.arguments = bundle

                        dialog.show(fragmentManager, "DeleteAction")

                        notifyItemChanged(position)
                    }
                    true
                }

                val edited = currentMessage.edited
                if (edited) {
                    holder.receivedEdited.visibility = View.VISIBLE
                }
                else{
                    holder.receivedEdited.visibility = View.GONE
                }

            }

            if(GroupChatActivity.SelectMultipleMessages.contains(messageList[position].messageKey)){
                val typedValue = TypedValue()
                val theme = holder.itemView.context.theme
                theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                holder.receivedLayout.setBackgroundColor(typedValue.data)
            }
            else{
                holder.receivedLayout.background = null
            }

            holder.itemView.setOnClickListener {
                Log.d("IsMultipleSelect",IsMultipleSelect.toString())
                if (GroupChatActivity.IsNoLongerMember) {
                    Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                }
                else{
                    if (IsMultipleSelect) {
                        if (listener.onUserClicked(position)) {
                            holder.receivedLayout.background = null
                        }
                        else {
                            val typedValue = TypedValue()
                            val theme = holder.itemView.context.theme
                            theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                            holder.receivedLayout.setBackgroundColor(typedValue.data)
                        }
                    }
                    else if(!IsMultipleSelect && messageList[position].messageType == "image" || messageList[position].messageType == "video") {

                        val realPosition = holder.adapterPosition
                        if (realPosition != RecyclerView.NO_POSITION) {
                            val intent = Intent(context, ImageVideoViewer::class.java)
                            intent.putExtra("messageKey", messageList[realPosition].messageKey)
                            intent.putExtra("messageUrl", messageList[realPosition].message)
                            intent.putExtra("messageType", messageList[realPosition].messageType)
                            intent.putExtra("messageDate", messageList[realPosition].date)
                            intent.putExtra("messageTime", messageList[realPosition].time)
                            intent.putExtra("senderUid", messageList[realPosition].sender)
                            context.startActivity(intent)
                        }
                    }
                }
                true
            }
        }

        else if(holder is GroupSentReplyViewHolder){
            if (shouldShowDateHeader) {
                val displayDate = when (formattedMessageDateStr) {
                    todayStr -> "Today"
                    yesterdayStr -> "Yesterday"
                    else -> formattedMessageDateStr
                }

                holder.dateValue.text = displayDate
                holder.date.visibility = View.VISIBLE
            } else {
                holder.date.visibility = View.GONE
            }

            holder.sentTime.text = currentMessage.time

            if (currentMessage.isReplyed && currentMessage.replyToMessage != null) {
                holder.replyLayout.setOnClickListener {
                    if (GroupChatActivity.IsNoLongerMember) {
                        Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("HighLight", "1")
                        replyHighlightListener.onReplyClicked(currentMessage.replyToMessage!!)
                    }
                }
            }

            when (currentMessage.messageType) {
                "image" -> {
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentAudioMessageLayout.visibility = View.GONE
                    holder.sentDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sentImageMessagelayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sentImageMessagelayout.visibility = View.VISIBLE
                    }

                    Glide.with(context).load(currentMessage.message).into(holder.sentImageMessage)
                }

                "video" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentAudioMessageLayout.visibility = View.GONE
                    holder.sentDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sentVideoMessagelayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sentVideoMessagelayout.visibility = View.VISIBLE
                    }

                    if(currentMessage.message!="") {
                        holder.sentVideoMessage.setVideoURI(currentMessage.message?.toUri())
                    }
                }

                "voiceRecord" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sentAudioMessageLayout.visibility = View.GONE
                    holder.sentDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sendvoiceRecordMessageLayout.visibility = View.VISIBLE

                        handlevoiceRecordPlayback(
                            context = holder.itemView.context,
                            voiceRecordUrl = currentMessage.message.toString(),
                            playButton = holder.play,
                            pauseButton = holder.pause,
                            seekBar = holder.voiceRecordSeekBar,
                            playingTimeText = holder.playingTime,
                            totalTimeText = holder.totalTime,
                            currentMessage = currentMessage
                        )

                    }

                }

                "audio" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sentAudioMessageLayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sentAudioMessageLayout.visibility = View.VISIBLE

                        handlevoiceRecordPlayback(
                            context = holder.itemView.context,
                            voiceRecordUrl = currentMessage.message.toString(),
                            playButton = holder.playAudio,
                            pauseButton = holder.pauseAudio,
                            seekBar = holder.voiceRecordSeekBarAudio,
                            playingTimeText = holder.playingTimeAudio,
                            totalTimeText = holder.totalTimeAudio,
                            currentMessage = currentMessage
                        )
                    }
                }

                "document" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentAudioMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sentDocumentMessageLayout.visibility = View.GONE
                    }
                    else {
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sentDocumentMessageLayout.visibility = View.VISIBLE

                        holder.documentName.text = currentMessage.fileName

                        holder.document_page_count.text = currentMessage.pageCount
                        if(currentMessage.pageCount == "1"){
                            holder.document_page.text = "page"
                        }
                        else{
                            holder.document_page.text = "pages"
                        }

                        holder.document_size.text = currentMessage.fileSize
                        holder.document_type.text = currentMessage.fileType
                        if (currentMessage.fileType == "PDF") {
                            holder.documentImage.setImageResource(R.drawable.pdf_icon)
                        } else if (currentMessage.fileType == "TXT") {
                            holder.documentImage.setImageResource(R.drawable.txt_icon)
                        } else if (currentMessage.fileType == "DOC" || currentMessage.fileType == "DOCX") {
                            holder.documentImage.setImageResource(R.drawable.doc_icon)
                        } else {
                            holder.documentImage.setImageResource(R.drawable.document)
                        }

                    }
                }

                else -> {
                    holder.sentMessage.visibility = View.VISIBLE
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentProgressBar.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE

                    if (currentMessage.message == "This message was Deleted!!!") {
                        holder.sentMessage.textSize = 12f
                        holder.sentMessage.setTypeface(null, Typeface.ITALIC)
                        holder.sentMessage.text = currentMessage.message
                    } else {
                        holder.sentMessage.textSize = 18f
                        holder.sentMessage.setTypeface(null, Typeface.NORMAL)
                        holder.sentMessage.text = currentMessage.message
                    }
                    if(currentMessage.edited){
                        holder.sentEdited.visibility = View.VISIBLE
                    }
                    else{
                        holder.sentEdited.visibility = View.GONE
                    }
                }
            }

            when (currentMessage.status) {
                "sent" -> holder.sent_status_image.setImageResource(R.drawable.single_tick)
                "received" -> holder.sent_status_image.setImageResource(R.drawable.double_tick_grey)
                "seen" -> holder.sent_status_image.setImageResource(R.drawable.double_tick_blue)
            }

            holder.itemView.setOnLongClickListener { view ->
                if (GroupChatActivity.IsNoLongerMember) {
                    Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                } else {
                    val realPosition = holder.adapterPosition
                    if (realPosition != RecyclerView.NO_POSITION) {

                        val dialog = DeleteGroupChatMessagesFragment()

                        val bundle = Bundle().apply {
                            putString("messageBy", "sender")
                            putString("messageKey", messageList[position].messageKey)
                            putString("message", messageList[position].message)
                            putString("messageType", messageList[position].messageType)
                            putLong("messageTime", (messageList[position].timeStamp))
                        }

                        dialog.arguments = bundle

                        dialog.show(fragmentManager, "DeleteAction")

                        notifyItemChanged(position)
                    }
                }
                true
            }

            if (currentMessage.edited) {
                holder.sentEdited.visibility = View.VISIBLE
            } else {
                holder.sentEdited.visibility = View.GONE
            }

            getCurrentUserName(){ name ->
                if(currentMessage.replySender == name){
                    holder.messagerName.text = "You"
                }
                else{
                    holder.messagerName.text = currentMessage.replySender
                }
            }

            holder.containedMessage.text = currentMessage.replyMessage

            if(GroupChatActivity.SelectMultipleMessages.contains(messageList[position].messageKey)){
                val typedValue = TypedValue()
                val theme = holder.itemView.context.theme
                theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                holder.sentLayout.setBackgroundColor(typedValue.data)
            }
            else{
                holder.sentLayout.background = null
            }

            holder.itemView.setOnClickListener {
                Log.d("IsMultipleSelect",IsMultipleSelect.toString())
                if (GroupChatActivity.IsNoLongerMember) {
                    Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                }
                else {
                    if (IsMultipleSelect) {
                        if (listener.onUserClicked(position)) {
                            holder.sentLayout.background = null
                        } else {
                            val typedValue = TypedValue()
                            val theme = holder.itemView.context.theme
                            theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                            holder.sentLayout.setBackgroundColor(typedValue.data)
                        }
                    } else if (!IsMultipleSelect && messageList[position].messageType == "image" || messageList[position].messageType == "video") {

                        val realPosition = holder.adapterPosition
                        if (realPosition != RecyclerView.NO_POSITION) {
                            val intent = Intent(context, ImageVideoViewer::class.java)
                            intent.putExtra("messageKey", messageList[realPosition].messageKey)
                            intent.putExtra("messageUrl", messageList[realPosition].message)
                            intent.putExtra("messageType", messageList[realPosition].messageType)
                            intent.putExtra("messageDate", messageList[realPosition].date)
                            intent.putExtra("messageTime", messageList[realPosition].time)
                            intent.putExtra("senderUid", messageList[realPosition].sender)
                            context.startActivity(intent)
                        }

                    }
                }
                true
            }


        }

        else if(holder is GroupReceiveReplyViewHolder){
            if (shouldShowDateHeader) {
                val displayDate = when (formattedMessageDateStr) {
                    todayStr -> "Today"
                    yesterdayStr -> "Yesterday"
                    else -> formattedMessageDateStr
                }

                holder.dateValue.text = displayDate
                holder.date.visibility = View.VISIBLE
            } else {
                holder.date.visibility = View.GONE
            }

            holder.receiveTime.text = currentMessage.time

            if (currentMessage.isReplyed && currentMessage.replyToMessage != null) {
                holder.replyLayout.setOnClickListener {
                    if (GroupChatActivity.IsNoLongerMember) {
                        Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("HighLight", "1")
                        replyHighlightListener?.onReplyClicked(currentMessage.replyToMessage!!)
                    }
                }
            }

            FirebaseDatabase.getInstance().getReference("user")
                .child(currentMessage.sender.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentUser = snapshot.getValue(User::class.java)
                        holder.senderName.text = currentUser?.name ?: "Unknown"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to load user: ${error.message}")
                    }
                })

             holder.receiveTime.text = currentMessage.time

                when (currentMessage.messageType) {
                    "image" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receivedAudioMessageLayout.visibility = View.GONE
                        holder.receivedDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivedImageMessagelayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivedImageMessagelayout.visibility = View.VISIBLE
                        }

                        Glide.with(context).load(currentMessage.message).into(holder.receivedImageMessage)
                    }
                    "video" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receivedAudioMessageLayout.visibility = View.GONE
                        holder.receivedDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivedVideoMessagelayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivedVideoMessagelayout.visibility = View.VISIBLE
                        }

                        if(currentMessage.message!="") {
                            holder.receivedVideoMessage.setVideoURI(currentMessage.message?.toUri())
                        }
                    }
                    "voiceRecord" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receivedAudioMessageLayout.visibility = View.GONE
                        holder.receivedDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivevoiceRecordMessageLayout.visibility = View.VISIBLE
                            handlevoiceRecordPlayback(
                                context = holder.itemView.context,
                                voiceRecordUrl = currentMessage.message.toString(),
                                playButton = holder.play,
                                pauseButton = holder.pause,
                                seekBar = holder.voiceRecordSeekBar,
                                playingTimeText = holder.playingTime,
                                totalTimeText = holder.totalTime,
                                currentMessage = currentMessage
                            )
                        }
                    }
                    "audio" -> {
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receivedDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivedAudioMessageLayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivedAudioMessageLayout.visibility = View.VISIBLE

                            handlevoiceRecordPlayback(
                                context = holder.itemView.context,
                                voiceRecordUrl = currentMessage.message.toString(),
                                playButton = holder.playAudio,
                                pauseButton = holder.pauseAudio,
                                seekBar = holder.voiceRecordSeekBarAudio,
                                playingTimeText = holder.playingTimeAudio,
                                totalTimeText = holder.totalTimeAudio,
                                currentMessage = currentMessage
                            )
                        }
                    }

                    "document" -> {
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receivedAudioMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receivedDocumentMessageLayout.visibility = View.GONE
                        }
                        else {
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receivedDocumentMessageLayout.visibility = View.VISIBLE

                            holder.documentName.text = currentMessage.fileName

                            holder.document_page_count.text = currentMessage.pageCount
                            if(currentMessage.pageCount == "1"){
                                holder.document_page.text = "page"
                            }
                            else{
                                holder.document_page.text = "pages"
                            }

                            holder.document_size.text = currentMessage.fileSize
                            holder.document_type.text = currentMessage.fileType
                            if (currentMessage.fileType == "PDF") {
                                holder.documentImage.setImageResource(R.drawable.pdf_icon)
                            } else if (currentMessage.fileType == "TXT") {
                                holder.documentImage.setImageResource(R.drawable.txt_icon)
                            } else if (currentMessage.fileType == "DOC" || currentMessage.fileType == "DOCX") {
                                holder.documentImage.setImageResource(R.drawable.doc_icon)
                            } else {
                                holder.documentImage.setImageResource(R.drawable.document)
                            }

                        }
                    }
                    else -> {
                        holder.receiveMessage.visibility = View.VISIBLE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receivedProgressBar.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE

                        if (currentMessage.message == "This message was Deleted!!!") {
                            holder.receiveMessage.textSize = 12f
                            holder.receiveMessage.setTypeface(null, Typeface.ITALIC)
                            holder.receiveMessage.text = currentMessage.message
                        } else {
                            holder.receiveMessage.setTypeface(null, Typeface.NORMAL)
                            holder.receiveMessage.textSize = 18f
                            holder.receiveMessage.text = currentMessage.message
                        }
                        if(currentMessage.edited){
                            holder.receivedEdited.visibility = View.VISIBLE
                        }
                        else{
                            holder.receivedEdited.visibility = View.GONE
                        }
                    }
                }

                holder.itemView.setOnLongClickListener {
                    if (GroupChatActivity.IsNoLongerMember) {
                        Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                    } else {
                        val dialog = DeleteGroupChatMessagesFragment()

                        val bundle = Bundle().apply {
                            putString("messageBy", "receiver")
                            putString("messageKey", messageList[position].messageKey)
                            putString("message", messageList[position].message)
                            putString("messageType", messageList[position].messageType)
                            putLong("messageTime", (messageList[position].timeStamp))
                        }

                        dialog.arguments = bundle

                        dialog.show(fragmentManager, "DeleteAction")

                        notifyItemChanged(position)
                    }
                    true
                }

                val edited = currentMessage.edited
                if (edited) {
                    holder.receivedEdited.visibility = View.VISIBLE
                }
                else{
                    holder.receivedEdited.visibility = View.GONE
                }

            getCurrentUserName(){ name ->
                if(currentMessage.replySender == name){
                    holder.messagerName.text = "You"
                }
                else{
                    holder.messagerName.text = currentMessage.replySender
                }
            }
            holder.containedMessage.text = currentMessage.replyMessage

            if(GroupChatActivity.SelectMultipleMessages.contains(messageList[position].messageKey)){
                val typedValue = TypedValue()
                val theme = holder.itemView.context.theme
                theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                holder.receivedLayout.setBackgroundColor(typedValue.data)
            }
            else{
                holder.receivedLayout.background = null
            }

            holder.itemView.setOnClickListener {
                if (GroupChatActivity.IsNoLongerMember) {
                    Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
                }
                else {
                    Log.d("IsMultipleSelect", IsMultipleSelect.toString())
                    if (IsMultipleSelect) {
                        if (listener.onUserClicked(position)) {
                            holder.receivedLayout.background = null
                        } else {
                            val typedValue = TypedValue()
                            val theme = holder.itemView.context.theme
                            theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                            holder.receivedLayout.setBackgroundColor(typedValue.data)
                        }
                    } else if (!IsMultipleSelect && messageList[position].messageType == "image" || messageList[position].messageType == "video") {

                        val realPosition = holder.adapterPosition
                        if (realPosition != RecyclerView.NO_POSITION) {
                            val intent = Intent(context, ImageVideoViewer::class.java)
                            intent.putExtra("messageKey", messageList[realPosition].messageKey)
                            intent.putExtra("messageUrl", messageList[realPosition].message)
                            intent.putExtra("messageType", messageList[realPosition].messageType)
                            intent.putExtra("messageDate", messageList[realPosition].date)
                            intent.putExtra("messageTime", messageList[realPosition].time)
                            intent.putExtra("senderUid", messageList[realPosition].sender)
                            context.startActivity(intent)
                        }
                    }
                }
                true
            }

        }

        updateStatus()

    }
    fun updateStatus(){
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

            FirebaseDatabase.getInstance().getReference("groups").child(GroupChatActivity.groupUid)
                .child("groupMessages")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(postsnapshot: DataSnapshot) {
                        for (snapshot in postsnapshot.children) {
                            for(i in GroupChatActivity.groupMembers) {
                                val message = snapshot.child(i).getValue(GroupMessage::class.java)
                                val localList = message?.viewersList

                                if (localList != null) {
                                    var count = 0
                                    for (i in localList) {
                                        if (i.value) count++
                                    }

                                    if (count == localList.size) {
                                        updateStatusTo("seen")
                                    } else if (count >= (localList.size / 2)) {
                                        updateStatusTo("received")
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "updateMessageVisit cancelled: ${error.message}")
                    }
                })
    }

    fun updateStatusTo(status: String) {
        for(i in GroupChatActivity.groupMembers) {
            FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(GroupChatActivity.groupUid)
                .child("groupMessages")
                .child(currentMessageObject.messageKey!!)
                .child(i)
                .child("status")
                .setValue(status)
        }

        notifyDataSetChanged()
    }

    fun startEditing(currentMessage: GroupMessage) {
        GroupChatActivity.editingkey = currentMessage.messageKey!!
        (context as? GroupChatActivity)?.editMessage(currentMessage.message)
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.sender) && currentMessage.isReplyed){
            return item_send_reply
        }
        else if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.sender)){
            return item_send
        }
        else if(!FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.sender) && currentMessage.isReplyed){
            return item_receive_reply
        }
        else{
            return item_receive
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class GroupSentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val sentLayout = itemView.findViewById<RelativeLayout>(R.id.sent_message_group_layout)
        val sentMessage = itemView.findViewById<TextView>(R.id.sent_message_group)
        val sentTime = itemView.findViewById<TextView>(R.id.sent_time_group)
        val sent_status_image = itemView.findViewById<ImageView>(R.id.sent_status_image_group)
        val sentImageMessage = itemView.findViewById<ImageView>(R.id.sentImageMessage_group)
        val sentVideoMessage = itemView.findViewById<VideoView>(R.id.sentVideoMessage_group)
        val sentEdited = itemView.findViewById<TextView>(R.id.sent_Edited_group)
        val sentVideoMessagelayout = itemView.findViewById<LinearLayout>(R.id.sentVideoMessagelayout_group)
        val sentImageMessagelayout = itemView.findViewById<LinearLayout>(R.id.sentImageMessagelayout_group)
        val sentProgressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_sent_group)
        val sentForwarded = itemView.findViewById<LinearLayout>(R.id.sent_forwarded_group)

        val sendvoiceRecordMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendVoiceRecordMessageLayout_group)
        val pause = itemView.findViewById<ImageView>(R.id.pause_send_group)
        val play = itemView.findViewById<ImageView>(R.id.play_send_group)
        val voiceRecordSeekBar = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_send_group)
        val playingTime = itemView.findViewById<TextView>(R.id.playing_time_send_group)
        val totalTime = itemView.findViewById<TextView>(R.id.total_time_send_group)

        val sentAudioMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendAudioMessageLayout_group)
        val pauseAudio = itemView.findViewById<ImageView>(R.id.pause_audio_send_group)
        val playAudio = itemView.findViewById<ImageView>(R.id.play_audio_send_group)
        val voiceRecordSeekBarAudio = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_audio_send_group)
        val playingTimeAudio = itemView.findViewById<TextView>(R.id.playing_time_audio_send_group)
        val totalTimeAudio = itemView.findViewById<TextView>(R.id.total_time_audio_send_group)

        val sentDocumentMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendDocumentMessageLayout)
        val documentImage = itemView.findViewById<ImageView>(R.id.documentImage_send)
        val documentName = itemView.findViewById<TextView>(R.id.documentName_send)
        val document_page_count = itemView.findViewById<TextView>(R.id.document_page_count_send)
        val document_size = itemView.findViewById<TextView>(R.id.document_size_send)
        val document_page = itemView.findViewById<TextView>(R.id.document_page_send)
        val document_type = itemView.findViewById<TextView>(R.id.document_type_send)
        val date = itemView.findViewById<LinearLayout>(R.id.date_layout_sent_group)
        val dateValue = itemView.findViewById<TextView>(R.id.dateOfMessages_sent_group)

    }

    class GroupReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receivedLayout = itemView.findViewById<RelativeLayout>(R.id.receive_message_group_layout)
        val senderName = itemView.findViewById<TextView>(R.id.senderName_group)
        val receiveMessage = itemView.findViewById<TextView>(R.id.receive_message_group)
        val receiveTime = itemView.findViewById<TextView>(R.id.receive_time_group)
        val receivedImageMessage = itemView.findViewById<ImageView>(R.id.receivedImageMessage_group)
        val receivedVideoMessage = itemView.findViewById<VideoView>(R.id.receivedVideoMessage_group)
        val receivedEdited = itemView.findViewById<TextView>(R.id.receive_Edited_group)
        val receivedVideoMessagelayout = itemView.findViewById<LinearLayout>(R.id.receivedVideoMessagelayout_group)
        val receivedImageMessagelayout = itemView.findViewById<LinearLayout>(R.id.receivedImageMessagelayout_group)
        val receivedProgressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_receive_group)
        val receivedForwarded = itemView.findViewById<LinearLayout>(R.id.received_forwarded_group)
        val receivevoiceRecordMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveVoiceRecordMessageLayout_group)
        val pause= itemView.findViewById<ImageView>(R.id.pause_receive_group)
        val play= itemView.findViewById<ImageView>(R.id.play_receive_group)
        val voiceRecordSeekBar = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_receive_group)
        val playingTime = itemView.findViewById<TextView>(R.id.playing_time_receive_group)
        val totalTime = itemView.findViewById<TextView>(R.id.total_time_receive_group)

        val receivedAudioMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveAudioMessageLayout_group)
        val pauseAudio = itemView.findViewById<ImageView>(R.id.pause_audio_receive_group)
        val playAudio = itemView.findViewById<ImageView>(R.id.play_audio_receive_group)
        val voiceRecordSeekBarAudio = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_audio_receive_group)
        val playingTimeAudio = itemView.findViewById<TextView>(R.id.playing_time_audio_receive_group)
        val totalTimeAudio = itemView.findViewById<TextView>(R.id.total_time_audio_receive_group)

        val receivedDocumentMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveDocumentMessageLayout_group)
        val documentImage = itemView.findViewById<ImageView>(R.id.documentImage_receive_group)
        val documentName = itemView.findViewById<TextView>(R.id.documentName_receive_group)
        val document_page_count = itemView.findViewById<TextView>(R.id.document_page_count_receive_group)
        val document_size = itemView.findViewById<TextView>(R.id.document_size_receive_group)
        val document_page = itemView.findViewById<TextView>(R.id.document_page_receive_group)
        val document_type = itemView.findViewById<TextView>(R.id.document_type_receive_group)
        val date = itemView.findViewById<LinearLayout>(R.id.date_layout_receive_group)
        val dateValue = itemView.findViewById<TextView>(R.id.dateOfMessages_receive_group)

    }

    class GroupSentReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sentLayout = itemView.findViewById<RelativeLayout>(R.id.sent_message_reply_layout)
        val sentMessage = itemView.findViewById<TextView>(R.id.sent_message_reply)
        val sentTime = itemView.findViewById<TextView>(R.id.sent_time_reply)
        val sent_status_image = itemView.findViewById<ImageView>(R.id.sent_status_image_reply)
        val sentImageMessage = itemView.findViewById<ImageView>(R.id.sentImageMessage_reply)
        val sentVideoMessage = itemView.findViewById<VideoView>(R.id.sentVideoMessage_reply)
        val sentEdited = itemView.findViewById<TextView>(R.id.sent_Edited_reply)
        val sentVideoMessagelayout = itemView.findViewById<LinearLayout>(R.id.sentVideoMessagelayout_reply)
        val sentImageMessagelayout = itemView.findViewById<LinearLayout>(R.id.sentImageMessagelayout_reply)
        val sentProgressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_sent_reply)
        val messagerName = itemView.findViewById<TextView>(R.id.messager_name_sent)
        val containedMessage = itemView.findViewById<TextView>(R.id.contained_message_sent)
        val imageReply = itemView.findViewById<ImageView>(R.id.reply_image_sent)
        val videoReply = itemView.findViewById<ImageView>(R.id.reply_video_sent)

        val sendvoiceRecordMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendVoiceRecordMessageLayout_reply)
        val pause = itemView.findViewById<ImageView>(R.id.pause_send_reply)
        val play = itemView.findViewById<ImageView>(R.id.play_send_reply)
        val voiceRecordSeekBar = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_send_reply)
        val playingTime = itemView.findViewById<TextView>(R.id.playing_time_send_reply)
        val totalTime = itemView.findViewById<TextView>(R.id.total_time_send_reply)

        val sentAudioMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendAudioMessageLayout_reply)
        val pauseAudio = itemView.findViewById<ImageView>(R.id.pause_audio_send_reply)
        val playAudio = itemView.findViewById<ImageView>(R.id.play_audio_send_reply)
        val voiceRecordSeekBarAudio = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_audio_send_reply)
        val playingTimeAudio = itemView.findViewById<TextView>(R.id.playing_time_audio_send_reply)
        val totalTimeAudio = itemView.findViewById<TextView>(R.id.total_time_audio_send_reply)

        val sentDocumentMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendDocumentMessageLayout_reply)
        val documentImage = itemView.findViewById<ImageView>(R.id.documentImage_send_reply)
        val documentName = itemView.findViewById<TextView>(R.id.documentName_send_reply)
        val document_page_count = itemView.findViewById<TextView>(R.id.document_page_count_send_reply)
        val document_size = itemView.findViewById<TextView>(R.id.document_size_send_reply)
        val document_page = itemView.findViewById<TextView>(R.id.document_page_send_reply)
        val document_type = itemView.findViewById<TextView>(R.id.document_type_send_reply)

        val replyLayout = itemView.findViewById<FrameLayout>(R.id.reply_layout_sent)
        val date = itemView.findViewById<LinearLayout>(R.id.date_layout_sent_reply)
        val dateValue = itemView.findViewById<TextView>(R.id.dateOfMessages_sent_reply)

    }

    class GroupReceiveReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val senderName = itemView.findViewById<TextView>(R.id.senderName_group_receive)
        val receiveMessage = itemView.findViewById<TextView>(R.id.receive_message_group_reply)
        val receiveTime = itemView.findViewById<TextView>(R.id.receive_time_group_reply)
        val receivedImageMessage = itemView.findViewById<ImageView>(R.id.receivedImageMessage_group_reply)
        val receivedVideoMessage = itemView.findViewById<VideoView>(R.id.receivedVideoMessage_group_reply)
        val receivedEdited = itemView.findViewById<TextView>(R.id.receive_Edited_group_reply)
        val receivedVideoMessagelayout = itemView.findViewById<LinearLayout>(R.id.receivedVideoMessagelayout_group_reply)
        val receivedImageMessagelayout = itemView.findViewById<LinearLayout>(R.id.receivedImageMessagelayout_group_reply)
        val receivedProgressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_receive_group_reply)
        val receivedLayout = itemView.findViewById<RelativeLayout>(R.id.receive_message_group_reply_layout)
        val messagerName = itemView.findViewById<TextView>(R.id.messager_name_group_receive)
        val containedMessage = itemView.findViewById<TextView>(R.id.contained_message_group_receive)
        val imageReply = itemView.findViewById<ImageView>(R.id.reply_image_group_receive)
        val videoReply = itemView.findViewById<ImageView>(R.id.reply_video_group_receive)

        val receivevoiceRecordMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveVoiceRecordMessageLayout_groupReply)
        val pause= itemView.findViewById<ImageView>(R.id.pause_receive_groupReply)
        val play= itemView.findViewById<ImageView>(R.id.play_receive_groupReply)
        val voiceRecordSeekBar = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_receive_groupReply)
        val playingTime = itemView.findViewById<TextView>(R.id.playing_time_receive_groupReply)
        val totalTime = itemView.findViewById<TextView>(R.id.total_time_receive_groupReply)

        val receivedAudioMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveAudioMessageLayout_group_reply)
        val pauseAudio = itemView.findViewById<ImageView>(R.id.pause_audio_receive_group_reply)
        val playAudio = itemView.findViewById<ImageView>(R.id.play_audio_receive_group_reply)
        val voiceRecordSeekBarAudio = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_audio_receive_group_reply)
        val playingTimeAudio = itemView.findViewById<TextView>(R.id.playing_time_audio_receive_group_reply)
        val totalTimeAudio = itemView.findViewById<TextView>(R.id.total_time_audio_receive_group_reply)

        val receivedDocumentMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveDocumentMessageLayout_group_reply)
        val documentImage = itemView.findViewById<ImageView>(R.id.documentImage_receive_group_reply)
        val documentName = itemView.findViewById<TextView>(R.id.documentName_receive_group_reply)
        val document_page_count = itemView.findViewById<TextView>(R.id.document_page_count_receive_group_reply)
        val document_size = itemView.findViewById<TextView>(R.id.document_size_receive_group_reply)
        val document_page = itemView.findViewById<TextView>(R.id.document_page_receive_group_reply)
        val document_type = itemView.findViewById<TextView>(R.id.document_type_receive_group_reply)
        val replyLayout = itemView.findViewById<FrameLayout>(R.id.reply_layout_group_receive)
        val date = itemView.findViewById<LinearLayout>(R.id.date_layout_receive_group_reply)
        val dateValue = itemView.findViewById<TextView>(R.id.dateOfMessages_receive_group_reply)

    }

    private fun handlevoiceRecordPlayback(
        context: Context,
        voiceRecordUrl: String,
        playButton: ImageView,
        pauseButton: ImageView,
        seekBar: SeekBar,
        playingTimeText: TextView,
        totalTimeText: TextView,
        currentMessage: GroupMessage
    ) {
        val mediaPlayer = MediaPlayer()
        val handler = Handler(Looper.getMainLooper())
        if (currentMessage.message != "") {
            try {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(context, Uri.parse(currentMessage.message))
                mediaPlayer.prepare()

                val durationInMillis = mediaPlayer.duration
                val durationInSeconds = durationInMillis / 1000
                val mins = durationInSeconds / 60
                val secs = durationInSeconds % 60

                totalTimeText.text = String.format("%02d:%02d", mins, secs)

                mediaPlayer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        playButton.setOnClickListener {
            if (GroupChatActivity.IsNoLongerMember) {
                Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(voiceRecordUrl)
                    mediaPlayer.prepare()
                    mediaPlayer.start()

                    playButton.visibility = View.GONE
                    pauseButton.visibility = View.VISIBLE

                    val totalDuration = mediaPlayer.duration
                    seekBar.max = totalDuration
                    totalTimeText.text = formatTime(totalDuration)

                    handler.post(object : Runnable {
                        override fun run() {
                            if (mediaPlayer.isPlaying) {
                                val current = mediaPlayer.currentPosition
                                seekBar.progress = current
                                playingTimeText.text = formatTime(current)
                                handler.postDelayed(this, 500)
                            }
                        }
                    })

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        pauseButton.setOnClickListener {
            if (GroupChatActivity.IsNoLongerMember) {
                Toast.makeText(context, "You are no longer a member of this group", Toast.LENGTH_SHORT).show()
            } else {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    pauseButton.visibility = View.GONE
                    playButton.visibility = View.VISIBLE
                }
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        mediaPlayer.setOnCompletionListener {
            pauseButton.visibility = View.GONE
            playButton.visibility = View.VISIBLE
            seekBar.progress = 0
            playingTimeText.text = "00:00"
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getCurrentUserName(callback: (String?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("user").child(uid)

            userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java)
                    callback(name)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
        } else {
            callback(null)
        }
    }


}