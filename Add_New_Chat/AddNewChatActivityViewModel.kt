package com.example.android.myproject.Add_New_Chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.myproject.Entities.User

class AddNewChatActivityViewModel : ViewModel() {

    private val _contactList = MutableLiveData<List<User>>()
    val contactList: LiveData<List<User>> get() = _contactList

    fun setContactList(members: List<User>) {
        _contactList.value = members
    }

    init {
        _contactList.value = emptyList()
    }

}