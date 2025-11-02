package com.example.android.myproject.Entities

data class Group(
    var groupname : String = "",
    var groupuid : String = "",
    var groupabout : String = "",
    var groupMembers : List<String> = emptyList(),
    var groupCreatedBy : String = "",
    var groupProfileImageUrl : String = "",
    var createdDate: String = getCurrentDate(),
    var createdTime: String = getCurrentTime(),
    var adminList: List<String> = emptyList(),
    var pastMembersList: List<String> = emptyList()
)