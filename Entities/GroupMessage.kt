package com.example.android.myproject.Entities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GroupMessage(
    var message: String? = null,
    var sender: String? = null,
    var time: String = getCurrentTimeGroup(),
    var date: String = getCurrentDateGroup(),
    var timeStamp : Long = getTimeStampGroup(),
    var status: String = "sent",
    var messageType: String = "none",
    var messageKey: String? = null,
    var edited: Boolean = false,
    var groupUid: String? = null,
    var viewersList: HashMap<String, Boolean>? = null,
    var forwarded: Boolean = false,
    var isReplyed : Boolean = false,
    var fileName : String = "",
    var replyToMessage : String = "",
    var replyMessage : String = "",
    var replySender : String = "",
    var fileSize : String = "",
    var fileType : String = "",
    var pageCount : String = ""
)

fun getCurrentTimeGroup(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date())
}

fun getCurrentDateGroup(): String {
    val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    return sdf.format(Date())
}

fun getTimeStampGroup(): Long{
    return System.currentTimeMillis()
}