package com.example.android.myproject.Chat_Main

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.Entities.User

class MainActivityViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _IsSearchBarActive = MutableLiveData<Boolean>()
    val IsSearchBarActive: LiveData<Boolean> get() = _IsSearchBarActive

    private val _IsLongPressed = MutableLiveData<Boolean>()
    val IsLongPressed: LiveData<Boolean> get() = _IsLongPressed

    fun setIsSearchBarActive(value: Boolean) {
        _IsSearchBarActive.value = value
    }

    fun setIsLongPressed(value: Boolean) {
        _IsLongPressed.value = value
    }

    private val _selectList = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectList: LiveData<MutableList<String>> get() = _selectList

    fun setSelectList(list : List<String>){
        _selectList.value = list.toMutableList()
    }
    fun addToSelectList(uid: String) {
        val updatedList = _selectList.value ?: mutableListOf()
        if (!updatedList.contains(uid)) {
            updatedList.add(uid)
            _selectList.value = updatedList
        }
    }

    fun removeFromSelectList(uid: String) {
        val updatedList = _selectList.value ?: mutableListOf()
        updatedList.remove(uid)
        _selectList.value = updatedList
    }

    fun clearSelectList() {
        _selectList.value = mutableListOf()
    }

    private val _isAppTextVisible = MutableLiveData<Boolean>(true)
    val isAppTextVisible: LiveData<Boolean> get() = _isAppTextVisible

    private val _isSelectCountVisible = MutableLiveData<Boolean>(false)
    val isSelectCountVisible: LiveData<Boolean> get() = _isSelectCountVisible

    private val _isBackNavigationVisible = MutableLiveData<Boolean>(false)
    val isBackNavigationVisible: LiveData<Boolean> get() = _isBackNavigationVisible

    private val _isDeleteVisible = MutableLiveData<Boolean>(false)
    val isDeleteVisible: LiveData<Boolean> get() = _isDeleteVisible

    private val _isTopBarVisible = MutableLiveData<Boolean>(true)
    val isTopBarVisible: LiveData<Boolean> get() = _isTopBarVisible

    private val _appTextVisibility = MutableLiveData<Int>(View.VISIBLE)
    val appTextVisibility: LiveData<Int> get() = _appTextVisibility

    fun setAppTextVisibility(visibility: Int) {
        _appTextVisibility.value = visibility
    }

    fun showSelectCount(show: Boolean) {
        _isSelectCountVisible.value = show
    }

    fun showBackNavigation(show: Boolean) {
        _isBackNavigationVisible.value = show
    }

    private val _isArchiveVisible = MutableLiveData<Boolean>(false)
    val isArchiveVisible: LiveData<Boolean> get() = _isArchiveVisible

    fun showArchive(show: Boolean) {
        _isArchiveVisible.value = show
    }

    fun showDelete(show: Boolean) {
        _isDeleteVisible.value = show
    }

    fun showTopBar(show: Boolean) {
        _isTopBarVisible.value = show
    }

    private val _messageList = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messageList: LiveData<MutableList<Message>> get() = _messageList

    private val _userList = MutableLiveData<List<User>>(emptyList())
    val userList: LiveData<List<User>> get() = _userList

    fun setUserList(users: List<User>) {
        _userList.value = users.toList()
    }

    fun clearUserList() {
        _userList.value = emptyList()
    }

    fun addUserList(user: User) {
        val current = _userList.value ?: emptyList()
        _userList.value = current + user
    }

    fun getUserList(): List<User> {
        return _userList.value ?: emptyList()
    }


    private val _userUnreadList = MutableLiveData<MutableList<User>>(mutableListOf())
    val userUnreadList: LiveData<MutableList<User>> get() = _userUnreadList

    fun setMessageList(messages: List<Message>) {
        _messageList.value = messages.toMutableList()
    }

    fun clearMessageList(){
        _messageList.value = mutableListOf()
    }

    fun setUserUnreadList(users: List<User>) {
        _userUnreadList.value = users.toMutableList()
    }

    fun clearUserUnreadList(){
        _userUnreadList.value = mutableListOf()
    }

    fun getUserUnreadList() : List<User> {
        return userUnreadList.value ?: emptyList()
    }
    private val _IsMessages = MutableLiveData<Boolean>(true)
    val IsMessages: LiveData<Boolean> get() = _IsMessages

    fun setIsMessages(value: Boolean) {
        _IsMessages.value = value
    }

    private val _IsContacts = MutableLiveData<Boolean>(true)
    val IsContacts: LiveData<Boolean> get() = _IsContacts

    fun setIsContacts(value: Boolean) {
        _IsContacts.value = value
    }

    private val _IsUnread = MutableLiveData<Boolean>(true)
    val IsUnread: LiveData<Boolean> get() = _IsUnread

    fun setIsUnread(value: Boolean) {
        _IsUnread.value = value
    }

    private val _IsContactBar = MutableLiveData<Boolean>(false)
    val IsContactBar: LiveData<Boolean> get() = _IsContactBar

    fun showIsContactBar(show: Boolean) {
        _IsContactBar.value = show
    }

    private val _IsMessageBar = MutableLiveData<Boolean>(false)
    val IsMessageBar: LiveData<Boolean> get() = _IsMessageBar

    fun showIsMessageBar(show: Boolean) {
        _IsMessageBar.value = show
    }

    private var _IsUnreadBar = MutableLiveData<Boolean>(false)
    val IsUnreadBar: LiveData<Boolean> get() = _IsUnreadBar

    fun showIsUnreadBar(show: Boolean) {
        _IsUnreadBar.value = show
    }

    private val _IsContactBar_clicked = MutableLiveData<Boolean>(false)
    val IsContactBar_clicked: LiveData<Boolean> get() = _IsContactBar_clicked

    fun showIsContactBar_clicked(show: Boolean) {
        _IsContactBar_clicked.value = show
    }

    private val _IsMessageBar_clicked = MutableLiveData<Boolean>(false)
    val IsMessageBar_clicked: LiveData<Boolean> get() = _IsMessageBar_clicked

    fun showIsMessageBar_clicked(show: Boolean) {
        _IsMessageBar_clicked.value = show
    }

    private var _IsUnreadBar_clicked = MutableLiveData<Boolean>(false)
    val IsUnreadBar_clicked: LiveData<Boolean> get() = _IsUnreadBar_clicked

    fun showIsUnreadBar_clicked(show: Boolean) {
        _IsUnreadBar_clicked.value = show
    }

    init {
        showIsMessageBar(true)
        showIsUnreadBar(true)
        showIsContactBar(true)
    }

    private val _showUserRecyclerView = MutableLiveData<Boolean>(false)
    val showUserRecyclerView: LiveData<Boolean> get() = _showUserRecyclerView

    private val _showMessageRecyclerView = MutableLiveData<Boolean>(false)
    val showMessageRecyclerView: LiveData<Boolean> get() = _showMessageRecyclerView

    fun showIsUserRecyclerView(show: Boolean) {
        _showUserRecyclerView.value = show
    }

    private val _showUnreadRecyclerView = MutableLiveData<Boolean>(false)
    val showUnreadRecyclerView: LiveData<Boolean> get() = _showUnreadRecyclerView

    fun setActiveRecyclerView(user: Boolean, message: Boolean, unread: Boolean) {
        _showUserRecyclerView.value = user
        _showMessageRecyclerView.value = message
        _showUnreadRecyclerView.value = unread
    }


}