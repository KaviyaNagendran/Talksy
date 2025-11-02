package com.example.android.myproject.Chat_Page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatActivityViewModel : ViewModel() {

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

    private var _selectCount = MutableLiveData<Int>()
    val selectCount: LiveData<Int> = _selectCount

    fun setSelectCount(value: Int) {
        _selectCount.value = value
    }

    private var _selectList = MutableLiveData<List<String>>()
    val selectList: LiveData<List<String>> = _selectList

    fun setSelectList(value: List<String>) {
        _selectList.value = value
    }

    fun addToSelectList(userId: String) {
        val currentList = _selectList.value?.toMutableList() ?: mutableListOf()
        currentList.add(userId)
        _selectList.value = currentList
    }

    fun removeFromSelectList(userId: String) {
        val currentList = _selectList.value?.toMutableList() ?: mutableListOf()
        currentList.remove(userId)
        _selectList.value = currentList
    }

    fun clearSelectList() {
        _selectList.value = emptyList()
    }

    private var _isLongPressed = MutableLiveData<Boolean>()
    val isLongPressed: LiveData<Boolean> = _isLongPressed

    fun setIsLongPressed(value: Boolean) {
        _isLongPressed.value = value
    }

    private var _imageLayout = MutableLiveData<Int>()
    val imageLayout: LiveData<Int> = _imageLayout

    fun setImageLayout(value: Int) {
        _imageLayout.value = value
    }

    private var _receiverProfile = MutableLiveData<Int>()
    val receiverProfile: LiveData<Int> = _receiverProfile

    fun setReceiverProfile(value: Int) {
        _receiverProfile.value = value
    }

    private var _receiverName = MutableLiveData<Int>()
    val receiverName: LiveData<Int> = _receiverName

    fun setReceiverName(value: Int) {
        _receiverName.value = value
    }

    private var _receiverNameString = MutableLiveData<String>()
    val receiverNameString: LiveData<String> = _receiverNameString

    fun setReceiverNameString(value: String) {
        _receiverNameString.value = value
    }

    private var _activeStatus = MutableLiveData<Int>()
    val activeStatus: LiveData<Int> = _activeStatus

    fun setActiveStatus(value: Int) {
        _activeStatus.value = value
    }

    private var _backNavigation = MutableLiveData<Int>()
    val backNavigation: LiveData<Int> = _backNavigation

    fun setBackNavigation(value: Int) {
        _backNavigation.value = value
    }

    private var _deleteButton = MutableLiveData<Int>()
    val deleteButton: LiveData<Int> = _deleteButton

    fun setDeleteButton(value: Int) {
        _deleteButton.value = value
    }

    private var _editButton = MutableLiveData<Int>()
    val editButton: LiveData<Int> = _editButton

    fun setEditButton(value: Int) {
        _editButton.value = value
    }

    private var _copyButton = MutableLiveData<Int>()
    val copyButton: LiveData<Int> = _copyButton

    fun setCopyButton(value: Int) {
        _copyButton.value = value
    }

    private var _containSender = MutableLiveData<Boolean>()
    val containSender: LiveData<Boolean> = _containSender

    fun setContainSender(value: Boolean) {
        _containSender.value = value
    }

    private var _containReceiver = MutableLiveData<Boolean>()
    val containReceiver: LiveData<Boolean> = _containReceiver

    fun setContainReceiver(value: Boolean) {
        _containReceiver.value = value
    }

}