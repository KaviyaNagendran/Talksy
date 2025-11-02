package com.example.android.myproject.Entities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Message(
    var message: String? = null,
    var sender: String? = null,
    var receiver: String? = null,
    var time: String = getCurrentTime(),
    var date: String = getCurrentDate(),
    var timeStamp : Long = getTimeStamp(),
    var status : String = "sent",
    var messageType : String = "none",
    var messageKey : String? = null,
    var edited : Boolean = false,
    var forwarded : Boolean = false,
    var isReplyed : Boolean = false,
    var replyToMessage : String = "",
    var replyMessage : String = "",
    var replySender : String = "",
    var fileName : String = "",
    var fileSize : String = "",
    var fileType : String = "",
    var pageCount : String = ""
)

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date())
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    return sdf.format(Date())
}

fun getTimeStamp(): Long{
    return System.currentTimeMillis()
}
