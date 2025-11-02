package com.example.android.myproject.GroupChat_Page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GroupChatActivityViewModel : ViewModel() {

    private var _searchBarLayout = MutableLiveData<Int>()
    val searchBarLayout: LiveData<Int> = _searchBarLayout

    fun setSearchBarLayout(value: Int) {
        _searchBarLayout.value = value
    }

    private var _topBar = MutableLiveData<Int>()
    val topBar: LiveData<Int> = _topBar

    fun setTopBar(value: Int) {
        _topBar.value = value
    }

    private val _messageSeenStatusUpdated = MutableLiveData<Boolean>()
    val messageSeenStatusUpdated: LiveData<Boolean> get() = _messageSeenStatusUpdated

    fun setSeenStatusUpdated(value: Boolean) {
        _messageSeenStatusUpdated.value = value
    }

}