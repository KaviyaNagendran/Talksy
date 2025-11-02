package com.example.android.myproject.Chat_Page

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
import android.widget.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.DeleteAction.DeleteChatMessagesFragment
import com.example.android.myproject.MediaViewer.ImageVideoViewer
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.Listeners.ReplyHighlightListener
import com.example.android.myproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MessageAdapter(
    val context: Context,
    val messageList: ArrayList<Message>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var replyHighlightListener: ReplyHighlightListener = context as ReplyHighlightListener
    val item_receive = 1
    val item_send = 2
    val item_send_reply = 3
    val item_receive_reply = 4

    private var listener : OnUserLongClickListener = context as OnUserLongClickListener

    companion object{
        lateinit var currentHolder : RecyclerView.ViewHolder
        lateinit var currentMessageObject : Message
        var IsMultipleSelect = false
        var olddate = ""
        var highlightedPosition: Int = -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            item_receive -> {
                val view = LayoutInflater.from(context).inflate(R.layout.activity_receive_message, parent, false)
                ReceiveViewHolder(view)
            }
            item_send -> {
                val view = LayoutInflater.from(context).inflate(R.layout.activity_sent_message, parent, false)
                SentViewHolder(view)
            }
            item_send_reply -> {
                val view = LayoutInflater.from(context).inflate(R.layout.activity_send_message_reply, parent, false)
                SendMessageReplyViewHolder(view)
            }
            item_receive_reply -> {
                val view = LayoutInflater.from(context).inflate(R.layout.activity_receive_message_reply, parent, false)
                ReceiveMessageReplyViewHolder(view)
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

        if (holder is SentViewHolder) {
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

            if(currentMessage.forwarded) {
                holder.sentForwarded.visibility = View.VISIBLE
            }
            else{
                holder.sentForwarded.visibility = View.GONE
            }

            when (currentMessage.messageType) {
                "image" -> {
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
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
                            playButton = holder.play_audio,
                            pauseButton = holder.pause_audio,
                            seekBar = holder.voiceRecordSeekBar_audio,
                            playingTimeText = holder.playingTime_audio,
                            totalTimeText = holder.totalTime_audio,
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

            holder.itemView.setOnLongClickListener { view ->

                getReceiver() { receiver ->
                    val dialog = DeleteChatMessagesFragment()

                    val bundle = Bundle().apply {
                        putString("messageBy","sender")
                        putString("messageKey", messageList[position].messageKey)
                        putString("receiver", receiver)
                        putString("message",messageList[position].message)
                        putString("messageType",messageList[position].messageType)
                        putLong("messageTime",(messageList[position].timeStamp))
                    }

                    dialog.arguments = bundle

                    dialog.show(fragmentManager, "DeleteAction")

                    notifyItemChanged(position)
                }

                true
            }

            if (currentMessage.edited) {
                holder.sentEdited.visibility = View.VISIBLE
            } else {
                holder.sentEdited.visibility = View.GONE
            }

            ChatActivity().updateSenttoSeen()

            if(ChatActivity.SelectMultipleMessages.contains(messageList[position].messageKey)){
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
                if (IsMultipleSelect) {
                    if (listener.onUserClicked(position)) {
                        holder.sentLayout.background = null
                    }
                    else {
                        val typedValue = TypedValue()
                        val theme = holder.itemView.context.theme
                        theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                        holder.sentLayout.setBackgroundColor(typedValue.data)
                    }
                }
                else if(!IsMultipleSelect && messageList[position].messageType == "image" || messageList[position].messageType == "video") {

                    val intent = Intent(context, ImageVideoViewer::class.java)
                    intent.putExtra("messageKey", messageList[position].messageKey)
                    intent.putExtra("messageUrl", messageList[position].message)
                    intent.putExtra("messageType", messageList[position].messageType)
                    intent.putExtra("messageDate", messageList[position].date)
                    intent.putExtra("messageTime", messageList[position].time)
                    intent.putExtra("senderUid", messageList[position].sender)
                    context.startActivity(intent)
                }
                true
            }

        }

        else if (holder is ReceiveViewHolder) {
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

            if (holder is ReceiveViewHolder) {
                holder.receiveTime.text = currentMessage.time

                if(currentMessage.forwarded) {
                    holder.receivedForwarded.visibility = View.VISIBLE
                }else{
                    holder.receivedForwarded.visibility = View.GONE
                }

                when (currentMessage.messageType) {
                    "image" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receiveAudioMessageLayout.visibility = View.GONE
                        holder.receiveDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedImageMessagelayout.visibility = View.GONE
                            holder.receivedProgressBar.visibility = View.VISIBLE
                        }
                        else{
                            holder.receivedImageMessagelayout.visibility = View.VISIBLE
                            holder.receivedProgressBar.visibility = View.GONE
                        }

                        Glide.with(context).load(currentMessage.message).into(holder.receivedImageMessage)
                    }
                    "video" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receiveAudioMessageLayout.visibility = View.GONE
                        holder.receiveDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedVideoMessagelayout.visibility = View.GONE
                            holder.receivedProgressBar.visibility = View.VISIBLE
                        }
                        else{
                            holder.receivedVideoMessagelayout.visibility = View.VISIBLE
                            holder.receivedProgressBar.visibility = View.GONE
                        }

                        if(currentMessage.message!="") {
                            holder.receivedVideoMessage.setVideoURI(currentMessage.message?.toUri())
                        }
                    }
                    "voiceRecord" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receiveAudioMessageLayout.visibility = View.GONE
                        holder.receiveDocumentMessageLayout.visibility = View.GONE

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
                        holder.receiveDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receiveAudioMessageLayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receiveAudioMessageLayout.visibility = View.VISIBLE

                            handlevoiceRecordPlayback(
                                context = holder.itemView.context,
                                voiceRecordUrl = currentMessage.message.toString(),
                                playButton = holder.play_audio,
                                pauseButton = holder.pause_audio,
                                seekBar = holder.voiceRecordSeekBar_audio,
                                playingTimeText = holder.playingTime_audio,
                                totalTimeText = holder.totalTime_audio,
                                currentMessage = currentMessage
                            )
                        }
                    }

                    "document" -> {
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receiveAudioMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receiveDocumentMessageLayout.visibility = View.GONE
                        }
                        else {
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receiveDocumentMessageLayout.visibility = View.VISIBLE

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
                        holder.receiveAudioMessageLayout.visibility = View.GONE
                        holder.receiveDocumentMessageLayout.visibility = View.GONE

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
                val edited = currentMessage.edited
                if (edited) {
                    holder.receivedEdited.visibility = View.VISIBLE
                }
                else{
                    holder.receivedEdited.visibility = View.GONE
                }

                holder.itemView.setOnLongClickListener { view ->

                    getReceiver() { receiver ->
                        val dialog = DeleteChatMessagesFragment()

                        val bundle = Bundle().apply {
                            putString("messageBy","receiver")
                            putString("messageKey", messageList[position].messageKey)
                            putString("receiver", receiver)
                            putString("message",messageList[position].message)
                            putString("messageType",messageList[position].messageType)
                            putLong("messageTime",(messageList[position].timeStamp))
                        }

                        dialog.arguments = bundle

                        dialog.show(fragmentManager, "DeleteAction")

                        notifyItemChanged(position)
                    }

                    true
                }

            }

            if(ChatActivity.SelectMultipleMessages.contains(messageList[position].messageKey)){
                val typedValue = TypedValue()
                val theme = holder.itemView.context.theme
                theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                holder.receivedLayout.setBackgroundColor(typedValue.data)
            }
            else{
                holder.receivedLayout.background = null
            }

            holder.itemView.setOnClickListener {
                if (IsMultipleSelect) {
                    if (listener.onUserClicked(position)) {
                        holder.receivedLayout.background = null
                    } else {
                        val typedValue = TypedValue()
                        val theme = holder.itemView.context.theme
                        theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                        holder.receivedLayout.setBackgroundColor(typedValue.data)
                    }
                }
                else if(!IsMultipleSelect && messageList[position].messageType == "image" || messageList[position].messageType == "video") {

                    val intent = Intent(context, ImageVideoViewer::class.java)
                    intent.putExtra("messageKey", messageList[position].messageKey)
                    intent.putExtra("messageUrl", messageList[position].message)
                    intent.putExtra("messageType", messageList[position].messageType)
                    intent.putExtra("messageDate", messageList[position].date)
                    intent.putExtra("messageTime", messageList[position].time)
                    intent.putExtra("senderUid", messageList[position].sender)
                    context.startActivity(intent)
                }
            }
        }

        else if(holder is SendMessageReplyViewHolder){
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
                    Log.d("HighLight","1")
                    replyHighlightListener?.onReplyClicked(currentMessage.replyToMessage!!)
                }
            }

            when (currentMessage.messageType) {
                "image" -> {
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendAudioMessageLayout.visibility = View.GONE
                    holder.sendDocumentMessageLayout.visibility = View.GONE

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
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendAudioMessageLayout.visibility = View.GONE
                    holder.sendDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sentVideoMessagelayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sentVideoMessagelayout.visibility = View.VISIBLE
                    }

                    if(currentMessage.message!=""){
                        holder.sentVideoMessage.setVideoURI(currentMessage.message?.toUri())
                    }
                }

                "voiceRecord" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendAudioMessageLayout.visibility = View.GONE
                    holder.sendDocumentMessageLayout.visibility = View.GONE

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
                    holder.sendDocumentMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sendAudioMessageLayout.visibility = View.GONE
                    }
                    else{
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sendAudioMessageLayout.visibility = View.VISIBLE

                        handlevoiceRecordPlayback(
                            context = holder.itemView.context,
                            voiceRecordUrl = currentMessage.message.toString(),
                            playButton = holder.play_audio,
                            pauseButton = holder.pause_audio,
                            seekBar = holder.voiceRecordSeekBar_audio,
                            playingTimeText = holder.playingTime_audio,
                            totalTimeText = holder.totalTime_audio,
                            currentMessage = currentMessage
                        )
                    }
                }

                "document" -> {
                    holder.sentImageMessagelayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sendAudioMessageLayout.visibility = View.GONE

                    if(currentMessage.message == ""){
                        holder.sentProgressBar.visibility = View.VISIBLE
                        holder.sendDocumentMessageLayout.visibility = View.GONE
                    }
                    else {
                        holder.sentProgressBar.visibility = View.GONE
                        holder.sendDocumentMessageLayout.visibility = View.VISIBLE

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
                    holder.sendvoiceRecordMessageLayout.visibility = View.GONE
                    holder.sentVideoMessagelayout.visibility = View.GONE
                    holder.sentProgressBar.visibility = View.GONE

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

                getReceiver() { receiver ->
                    val dialog = DeleteChatMessagesFragment()

                    val bundle = Bundle().apply {
                        putString("messageBy","sender")
                        putString("messageKey", messageList[position].messageKey)
                        putString("receiver", receiver)
                        putString("message",messageList[position].message)
                        putString("messageType",messageList[position].messageType)
                        putLong("messageTime",(messageList[position].timeStamp))
                    }

                    dialog.arguments = bundle

                    dialog.show(fragmentManager, "DeleteAction")

                    notifyItemChanged(position)
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

            ChatActivity().updateSenttoSeen()

            if(ChatActivity.SelectMultipleMessages.contains(messageList[position].messageKey)){
                val typedValue = TypedValue()
                val theme = holder.itemView.context.theme
                theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                holder.sentLayout.setBackgroundColor(typedValue.data)
            }
            else{
                holder.sentLayout.background = null
            }

            holder.itemView.setOnClickListener {
                if (IsMultipleSelect) {
                    if (listener.onUserClicked(position)) {
                        holder.sentLayout.background = null
                    } else {
                        val typedValue = TypedValue()
                        val theme = holder.itemView.context.theme
                        theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                        holder.sentLayout.setBackgroundColor(typedValue.data)
                    }
                }
                else if(!IsMultipleSelect && messageList[position].messageType == "image" || messageList[position].messageType == "video") {

                    val intent = Intent(context, ImageVideoViewer::class.java)
                    intent.putExtra("messageKey", messageList[position].messageKey)
                    intent.putExtra("messageUrl", messageList[position].message)
                    intent.putExtra("messageType", messageList[position].messageType)
                    intent.putExtra("messageDate", messageList[position].date)
                    intent.putExtra("messageTime", messageList[position].time)
                    intent.putExtra("senderUid", messageList[position].sender)
                    context.startActivity(intent)
                }
            }
        }

        else if(holder is ReceiveMessageReplyViewHolder) {
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

            holder.receiveTime.text = currentMessage.time

            if (currentMessage.isReplyed && currentMessage.replyToMessage != null) {
                holder.replyLayout.setOnClickListener {
                    Log.d("HighLight","1")
                    replyHighlightListener?.onReplyClicked(currentMessage.replyToMessage!!)
                }
            }

                when (currentMessage.messageType) {
                    "image" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receiveAudioMessageLayout.visibility = View.GONE
                        holder.receiveDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedImageMessagelayout.visibility = View.GONE
                            holder.receivedProgressBar.visibility = View.VISIBLE
                        }
                        else{
                            holder.receivedImageMessagelayout.visibility = View.VISIBLE
                            holder.receivedProgressBar.visibility = View.GONE
                        }

                        Glide.with(context).load(currentMessage.message).into(holder.receivedImageMessage)
                    }
                    "video" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receiveAudioMessageLayout.visibility = View.GONE
                        holder.receiveDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedVideoMessagelayout.visibility = View.GONE
                            holder.receivedProgressBar.visibility = View.VISIBLE
                        }
                        else{
                            holder.receivedVideoMessagelayout.visibility = View.VISIBLE
                            holder.receivedProgressBar.visibility = View.GONE
                        }

                        if(currentMessage.message!="") {
                            holder.receivedVideoMessage.setVideoURI(currentMessage.message?.toUri())
                        }
                    }
                    "voiceRecord" -> {
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receiveAudioMessageLayout.visibility = View.GONE
                        holder.receiveDocumentMessageLayout.visibility = View.GONE

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
                        holder.receiveDocumentMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receiveAudioMessageLayout.visibility = View.GONE
                        }
                        else{
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receiveAudioMessageLayout.visibility = View.VISIBLE

                            handlevoiceRecordPlayback(
                                context = holder.itemView.context,
                                voiceRecordUrl = currentMessage.message.toString(),
                                playButton = holder.play_audio,
                                pauseButton = holder.pause_audio,
                                seekBar = holder.voiceRecordSeekBar_audio,
                                playingTimeText = holder.playingTime_audio,
                                totalTimeText = holder.totalTime_audio,
                                currentMessage = currentMessage
                            )
                        }
                    }

                    "document" -> {
                        holder.receivedImageMessagelayout.visibility = View.GONE
                        holder.receivedVideoMessagelayout.visibility = View.GONE
                        holder.receiveMessage.visibility = View.GONE
                        holder.receivevoiceRecordMessageLayout.visibility = View.GONE
                        holder.receiveAudioMessageLayout.visibility = View.GONE

                        if(currentMessage.message == ""){
                            holder.receivedProgressBar.visibility = View.VISIBLE
                            holder.receiveDocumentMessageLayout.visibility = View.GONE
                        }
                        else {
                            holder.receivedProgressBar.visibility = View.GONE
                            holder.receiveDocumentMessageLayout.visibility = View.VISIBLE

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
                val edited = currentMessage.edited
                if (edited) {
                    holder.receivedEdited.visibility = View.VISIBLE
                }
                else{
                    holder.receivedEdited.visibility = View.GONE
                }

                holder.itemView.setOnLongClickListener { view ->

                    getReceiver() { receiver ->
                        val dialog = DeleteChatMessagesFragment()

                        val bundle = Bundle().apply {
                            putString("messageBy","receiver")
                            putString("messageKey", messageList[position].messageKey)
                            putString("receiver", receiver)
                            putString("message",messageList[position].message)
                            putString("messageType",messageList[position].messageType)
                            putLong("messageTime",(messageList[position].timeStamp))
                        }

                        dialog.arguments = bundle

                        dialog.show(fragmentManager, "DeleteAction")

                        notifyItemChanged(position)
                    }

                    true
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

            if(ChatActivity.SelectMultipleMessages.contains(messageList[position].messageKey)){
                val typedValue = TypedValue()
                val theme = holder.itemView.context.theme
                theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                holder.receivedLayout.setBackgroundColor(typedValue.data)
            }
            else{
                holder.receivedLayout.background = null
            }

            holder.itemView.setOnClickListener {
                if (IsMultipleSelect) {
                    if (listener.onUserClicked(position)) {
                        holder.receivedLayout.background = null
                    } else {
                        val typedValue = TypedValue()
                        val theme = holder.itemView.context.theme
                        theme.resolveAttribute(R.attr.contactHighLight, typedValue, true)
                        holder.receivedLayout.setBackgroundColor(typedValue.data)
                    }
                }
                else if(!IsMultipleSelect && messageList[position].messageType == "image" || messageList[position].messageType == "video") {

                    val intent = Intent(context, ImageVideoViewer::class.java)
                    intent.putExtra("messageKey", messageList[position].messageKey)
                    intent.putExtra("messageUrl", messageList[position].message)
                    intent.putExtra("messageType", messageList[position].messageType)
                    intent.putExtra("messageDate", messageList[position].date)
                    intent.putExtra("messageTime", messageList[position].time)
                    intent.putExtra("senderUid", messageList[position].sender)
                    context.startActivity(intent)
                }
            }
        }

    }

    fun highlightMessageAt(position: Int) {
        val previous = highlightedPosition
        highlightedPosition = position
        if (previous != -1) notifyItemChanged(previous)
        notifyItemChanged(position)

        Handler(Looper.getMainLooper()).postDelayed({
            val old = highlightedPosition
            highlightedPosition = -1
            notifyItemChanged(old)
        }, 2000)
    }

    private fun handlevoiceRecordPlayback(
        context: Context,
        voiceRecordUrl: String,
        playButton: ImageView,
        pauseButton: ImageView,
        seekBar: SeekBar,
        playingTimeText: TextView,
        totalTimeText: TextView,
        currentMessage: Message
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

        pauseButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                pauseButton.visibility = View.GONE
                playButton.visibility = View.VISIBLE
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
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sentMessage = itemView.findViewById<TextView>(R.id.sent_message)
        val sentTime = itemView.findViewById<TextView>(R.id.sent_time)
        val sent_status_image = itemView.findViewById<ImageView>(R.id.sent_status_image)
        val sentImageMessage = itemView.findViewById<ImageView>(R.id.sentImageMessage)
        val sentVideoMessage = itemView.findViewById<VideoView>(R.id.sentVideoMessage)
        val sentEdited = itemView.findViewById<TextView>(R.id.sent_Edited)
        val sentVideoMessagelayout = itemView.findViewById<LinearLayout>(R.id.sentVideoMessagelayout)
        val sentImageMessagelayout = itemView.findViewById<LinearLayout>(R.id.sentImageMessagelayout)
        val sentProgressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_sent)
        val sentLayout = itemView.findViewById<RelativeLayout>(R.id.sent_message_layout)
        val sentForwarded = itemView.findViewById<LinearLayout>(R.id.sent_forwarded)
        val sendvoiceRecordMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendVoiceRecordMessageLayout)
        val pause = itemView.findViewById<ImageView>(R.id.pause_send)
        val play = itemView.findViewById<ImageView>(R.id.play_send)
        val voiceRecordSeekBar = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_send)
        val playingTime = itemView.findViewById<TextView>(R.id.playing_time_send)
        val totalTime = itemView.findViewById<TextView>(R.id.total_time_send)

        val sentAudioMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendAudioMessageLayout)
        val pause_audio = itemView.findViewById<ImageView>(R.id.pause_audio_send)
        val play_audio = itemView.findViewById<ImageView>(R.id.play_audio_send)
        val voiceRecordSeekBar_audio = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_audio_send)
        val playingTime_audio = itemView.findViewById<TextView>(R.id.playing_time_audio_send)
        val totalTime_audio = itemView.findViewById<TextView>(R.id.total_time_audio_send)
        val sentDocumentMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendDocumentMessageLayout)
        val documentImage = itemView.findViewById<ImageView>(R.id.documentImage_send)
        val documentName = itemView.findViewById<TextView>(R.id.documentName_send)
        val document_page_count = itemView.findViewById<TextView>(R.id.document_page_count_send)
        val document_page = itemView.findViewById<TextView>(R.id.document_page_send)
        val document_size = itemView.findViewById<TextView>(R.id.document_size_send)
        val document_type = itemView.findViewById<TextView>(R.id.document_type_send)
        val date = itemView.findViewById<LinearLayout>(R.id.date_layout_sent)
        val dateValue = itemView.findViewById<TextView>(R.id.dateOfMessages_sent)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receiveMessage = itemView.findViewById<TextView>(R.id.receive_message)
        val receiveTime = itemView.findViewById<TextView>(R.id.receive_time)
        val receivedImageMessage = itemView.findViewById<ImageView>(R.id.receivedImageMessage)
        val receivedVideoMessage = itemView.findViewById<VideoView>(R.id.receivedVideoMessage)
        val receivedEdited = itemView.findViewById<TextView>(R.id.receive_Edited)
        val receivedVideoMessagelayout = itemView.findViewById<LinearLayout>(R.id.receivedVideoMessagelayout)
        val receivedImageMessagelayout = itemView.findViewById<LinearLayout>(R.id.receivedImageMessagelayout)
        val receivedProgressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_receive)
        val receivedLayout = itemView.findViewById<RelativeLayout>(R.id.received_message_layout)
        val receivedForwarded = itemView.findViewById<LinearLayout>(R.id.received_forwarded)
        val receivevoiceRecordMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveVoiceRecordMessageLayout)
        val pause= itemView.findViewById<ImageView>(R.id.pause_receive)
        val play= itemView.findViewById<ImageView>(R.id.play_receive)
        val voiceRecordSeekBar = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_receive)
        val playingTime = itemView.findViewById<TextView>(R.id.playing_time_receive)
        val totalTime = itemView.findViewById<TextView>(R.id.total_time_receive)
        val receiveAudioMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveAudioMessageLayout)
        val pause_audio= itemView.findViewById<ImageView>(R.id.pause_audio_receive)
        val play_audio= itemView.findViewById<ImageView>(R.id.play_audio_receive)
        val voiceRecordSeekBar_audio = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_audio_receive)
        val playingTime_audio = itemView.findViewById<TextView>(R.id.playing_time_audio_receive)
        val totalTime_audio = itemView.findViewById<TextView>(R.id.total_time_audio_receive)

        val receiveDocumentMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveDocumentMessageLayout)
        val documentImage = itemView.findViewById<ImageView>(R.id.documentImage_receive)
        val documentName = itemView.findViewById<TextView>(R.id.documentName_receive)
        val document_page_count = itemView.findViewById<TextView>(R.id.document_page_count_receive)
        val document_page = itemView.findViewById<TextView>(R.id.document_page_receive)
        val document_size = itemView.findViewById<TextView>(R.id.document_size_receive)
        val document_type = itemView.findViewById<TextView>(R.id.document_type_receive)
        val date = itemView.findViewById<LinearLayout>(R.id.date_layout_receive)
        val dateValue = itemView.findViewById<TextView>(R.id.dateOfMessages_receive)

    }

    class SendMessageReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sentMessage = itemView.findViewById<TextView>(R.id.sent_message_reply)
        val sentTime = itemView.findViewById<TextView>(R.id.sent_time_reply)
        val sent_status_image = itemView.findViewById<ImageView>(R.id.sent_status_image_reply)
        val sentImageMessage = itemView.findViewById<ImageView>(R.id.sentImageMessage_reply)
        val sentVideoMessage = itemView.findViewById<VideoView>(R.id.sentVideoMessage_reply)
        val sentEdited = itemView.findViewById<TextView>(R.id.sent_Edited_reply)
        val sentVideoMessagelayout = itemView.findViewById<LinearLayout>(R.id.sentVideoMessagelayout_reply)
        val sentImageMessagelayout = itemView.findViewById<LinearLayout>(R.id.sentImageMessagelayout_reply)
        val sentProgressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_sent_reply)
        val sentLayout = itemView.findViewById<RelativeLayout>(R.id.sent_message_reply_layout)
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
        val sendAudioMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendAudioMessageLayout_reply)
        val pause_audio = itemView.findViewById<ImageView>(R.id.pause_audio_send_reply)
        val play_audio = itemView.findViewById<ImageView>(R.id.play_audio_send_reply)
        val voiceRecordSeekBar_audio = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_audio_send_reply)
        val playingTime_audio = itemView.findViewById<TextView>(R.id.playing_time_audio_send_reply)
        val totalTime_audio = itemView.findViewById<TextView>(R.id.total_time_audio_send_reply)

        val sendDocumentMessageLayout = itemView.findViewById<LinearLayout>(R.id.sendDocumentMessageLayout_reply)
        val documentImage = itemView.findViewById<ImageView>(R.id.documentImage_send_reply)
        val documentName = itemView.findViewById<TextView>(R.id.documentName_send_reply)
        val document_page_count = itemView.findViewById<TextView>(R.id.document_page_count_send_reply)
        val document_page = itemView.findViewById<TextView>(R.id.document_page_send_reply)
        val document_size = itemView.findViewById<TextView>(R.id.document_size_send_reply)
        val document_type = itemView.findViewById<TextView>(R.id.document_type_send_reply)
        val replyLayout = itemView.findViewById<FrameLayout>(R.id.reply_layout_sent)
        val date = itemView.findViewById<LinearLayout>(R.id.date_layout_sent_reply)
        val dateValue = itemView.findViewById<TextView>(R.id.dateOfMessages_sent_reply)
    }

    class ReceiveMessageReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receiveMessage = itemView.findViewById<TextView>(R.id.receive_message_reply)
        val receiveTime = itemView.findViewById<TextView>(R.id.receive_time_reply)
        val receivedImageMessage = itemView.findViewById<ImageView>(R.id.receivedImageMessage_reply)
        val receivedVideoMessage = itemView.findViewById<VideoView>(R.id.receivedVideoMessage_reply)
        val receivedEdited = itemView.findViewById<TextView>(R.id.receive_Edited_reply)
        val receivedVideoMessagelayout = itemView.findViewById<LinearLayout>(R.id.receivedVideoMessagelayout_reply)
        val receivedImageMessagelayout = itemView.findViewById<LinearLayout>(R.id.receivedImageMessagelayout_reply)
        val receivedProgressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_receive_reply)
        val receivedLayout = itemView.findViewById<RelativeLayout>(R.id.received_message_reply_layout)
        val messagerName = itemView.findViewById<TextView>(R.id.messager_name_receive)
        val containedMessage = itemView.findViewById<TextView>(R.id.contained_message_receive)
        val imageReply = itemView.findViewById<ImageView>(R.id.reply_image_receive)
        val videoReply = itemView.findViewById<ImageView>(R.id.reply_video_receive)
        val receivevoiceRecordMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveVoiceRecordMessageLayout_reply)
        val pause= itemView.findViewById<ImageView>(R.id.pause_receive_reply)
        val play= itemView.findViewById<ImageView>(R.id.play_receive_reply)
        val voiceRecordSeekBar = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_receive_reply)
        val playingTime = itemView.findViewById<TextView>(R.id.playing_time_receive_reply)
        val totalTime = itemView.findViewById<TextView>(R.id.total_time_receive_reply)

        val receiveAudioMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveAudioMessageLayout_reply)
        val pause_audio= itemView.findViewById<ImageView>(R.id.pause_audio_receive_reply)
        val play_audio= itemView.findViewById<ImageView>(R.id.play_audio_receive_reply)
        val voiceRecordSeekBar_audio = itemView.findViewById<SeekBar>(R.id.voiceRecordSeekBar_audio_receive_reply)
        val playingTime_audio = itemView.findViewById<TextView>(R.id.playing_time_audio_receive_reply)
        val totalTime_audio = itemView.findViewById<TextView>(R.id.total_time_audio_receive_reply)

        val receiveDocumentMessageLayout = itemView.findViewById<LinearLayout>(R.id.receiveDocumentMessageLayout_reply)
        val documentImage = itemView.findViewById<ImageView>(R.id.documentImage_receive_reply)
        val documentName = itemView.findViewById<TextView>(R.id.documentName_receive_reply)
        val document_page_count = itemView.findViewById<TextView>(R.id.document_page_count_receive_reply)
        val document_page = itemView.findViewById<TextView>(R.id.document_page_receive_reply)
        val document_size = itemView.findViewById<TextView>(R.id.document_size_receive_reply)
        val document_type = itemView.findViewById<TextView>(R.id.document_type_receive_reply)
        val replyLayout = itemView.findViewById<FrameLayout>(R.id.reply_layout_receive)
        val date = itemView.findViewById<LinearLayout>(R.id.date_layout_receive_reply)
        val dateValue = itemView.findViewById<TextView>(R.id.dateOfMessages_receive_reply)

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