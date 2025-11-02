package com.example.android.myproject.Group_Main

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.Entities.GroupMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GroupMainActivityViewModel : ViewModel() {

    private val mAuth = FirebaseAuth.getInstance()
    private val mDBRef = FirebaseDatabase.getInstance().getReference()

    private val _groupList = MutableLiveData<List<Group>>()
    val groupList: LiveData<List<Group>> = _groupList

    fun setGroupList(list: List<Group>) {
        _groupList.value = list
    }

    private val _unreadGroupList = MutableLiveData<List<Group>>()
    val unreadGroupList: LiveData<List<Group>> = _unreadGroupList

    fun setUnreadGroupList(list: List<Group>) {
        _unreadGroupList.value = list
    }

    private val _searchMessageList = MutableLiveData<List<GroupMessage>>()
    val searchMessageList: LiveData<List<GroupMessage>> = _searchMessageList

    fun setSearchMessageList(list: List<GroupMessage>) {
        _searchMessageList.value = list
    }

    private val _selectedList = MutableLiveData<List<String>>(emptyList())
    val selectedList: LiveData<List<String>> = _selectedList

    fun setSelectedList(list: List<String>) {
        _selectedList.value = list
    }

    fun clearSelectedList(){
        _selectedList.value = emptyList()
    }

    fun addSelectedList(uid: String) {
        val updatedList = _selectedList.value?.toMutableList() ?: mutableListOf()
        if (!updatedList.contains(uid)) {
            updatedList.add(uid)
            _selectedList.value = updatedList
        }
    }

    fun removeSelectedList(uid: String) {
        val updatedList = _selectedList.value?.toMutableList() ?: mutableListOf()
        updatedList.remove(uid)
        _selectedList.value = updatedList
    }

    private val _isLongPressed = MutableLiveData(false)
    val isLongPressed: LiveData<Boolean> = _isLongPressed

    fun setIsLongPressed(value: Boolean) {
        _isLongPressed.value = value
    }

    private val _isAppTextVisible = MutableLiveData<Int>()
    val isAppTextVisible: LiveData<Int> get() = _isAppTextVisible

    private val _isSelectCountVisible = MutableLiveData<Int>()
    val isSelectCountVisible: LiveData<Int> get() = _isSelectCountVisible

    private val _isBackNavigationVisible = MutableLiveData<Int>()
    val isBackNavigationVisible: LiveData<Int> get() = _isBackNavigationVisible

    private val _isArchiveVisible = MutableLiveData<Int>()
    val isArchiveVisible: LiveData<Int> get() = _isArchiveVisible

    private val _isDeleteVisible = MutableLiveData<Int>()
    val isDeleteVisible: LiveData<Int> get() = _isDeleteVisible

    private val _isTopBarVisible = MutableLiveData<Int>()
    val isTopBarVisible: LiveData<Int> get() = _isTopBarVisible

    private val _selectCount = MutableLiveData(0)
    val selectCount: LiveData<Int> get() = _selectCount

    fun setAppTextVisible(visible: Int) = _isAppTextVisible.postValue(visible)
    fun setSelectCountVisible(visible: Int) = _isSelectCountVisible.postValue(visible)
    fun setBackNavigationVisible(visible: Int) = _isBackNavigationVisible.postValue(visible)
    fun setArchiveVisible(visible: Int) = _isArchiveVisible.postValue(visible)
    fun setDeleteVisible(visible: Int) = _isDeleteVisible.postValue(visible)
    fun setTopBarVisible(visible: Int) = _isTopBarVisible.postValue(visible)
    fun setSelectCount(count: Int) = _selectCount.postValue(count)

    private val _isMessagesSelected = MutableLiveData(false)
    val isMessagesSelected: LiveData<Boolean> get() = _isMessagesSelected

    fun setIsMessagesSelected(value: Boolean) {
        _isMessagesSelected.value = value
    }

    private val _isGroupsSelected = MutableLiveData(false)
    val isGroupsSelected: LiveData<Boolean> get() = _isGroupsSelected

    fun setIsGroupsSelected(value: Boolean) {
        _isGroupsSelected.value = value
    }

    private val _isUnreadSelected = MutableLiveData(false)
    val isUnreadSelected: LiveData<Boolean> get() = _isUnreadSelected

    fun setIsUnreadSelected(value: Boolean) {
        _isUnreadSelected.value = value
    }

    private val _isSearchBarActive = MutableLiveData(false)
    val isSearchBarActive: LiveData<Boolean> get() = _isSearchBarActive

    fun setSearchBarActive(active: Boolean) {
        _isSearchBarActive.postValue(active)
    }

    private val _unread = MutableLiveData<Int>()
    val unread: LiveData<Int> = _unread

    private val _groups = MutableLiveData<Int>()
    val groups: LiveData<Int> = _groups

    private val _messages = MutableLiveData<Int>()
    val messages: LiveData<Int> = _messages

    fun setUnread(value: Int) {
        _unread.value = value
    }

    fun setGroups(value: Int) {
        _groups.value = value
    }

    fun setMessages(value: Int) {
        _messages.value = value
    }

    private val _unread_clicked = MutableLiveData<Int>()
    val unread_clicked: LiveData<Int> = _unread_clicked

    private val _groups_clicked = MutableLiveData<Int>()
    val groups_clicked: LiveData<Int> = _groups_clicked

    private val _messages_clicked = MutableLiveData<Int>()
    val messages_clicked: LiveData<Int> = _messages_clicked

    fun setUnread_clicked(value: Int) {
        _unread_clicked.value = value
    }

    fun setGroups_clicked(value: Int) {
        _groups_clicked.value = value
    }

    fun setMessages_clicked(value: Int) {
        _messages_clicked.value = value
    }

    init {
        setUnread(View.VISIBLE)
        setGroups(View.VISIBLE)
        setMessages(View.VISIBLE)
    }

    private val _recyclerView = MutableLiveData<Boolean>(false)
    val recyclerView: LiveData<Boolean> get() = _recyclerView

    fun setRecyclerView(value: Boolean) {
        _recyclerView.value = value
    }

    private val _groupChatSearchRecyclerView = MutableLiveData<Boolean>(false)
    val groupChatSearchRecyclerView: LiveData<Boolean> get() = _groupChatSearchRecyclerView

    fun setGroupChatSearchRecyclerView(value: Boolean) {
        _groupChatSearchRecyclerView.value = value
    }

    private val _groupUnreadRecyclerView = MutableLiveData<Boolean>(false)
    val groupUnreadRecyclerView: LiveData<Boolean> get() = _groupUnreadRecyclerView

    fun setGroupUnreadRecyclerView(value: Boolean) {
        _groupUnreadRecyclerView.value = value
    }

    fun setTabSelection(isMessages: Boolean, isGroups: Boolean, isUnread: Boolean) {
        _groupChatSearchRecyclerView.value = isMessages
        _recyclerView.value = isGroups
        _groupUnreadRecyclerView.value = isUnread
    }

}
