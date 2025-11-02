package com.example.android.myproject.Entities

data class User(
    var uid: String? = null,
    var name: String? = null,
    var email: String? = null,
    var activeChatUid : String? = "offline",
    var about : String? = "",
    var profileUrl : String? = ""
)