package com.example.android.myproject.Forward_Message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.Entities.User

class UserAndGroupListActivityViewModel : ViewModel() {

    private val _selectUserList = MutableLiveData<List<User>>(emptyList())
    val selectUserList: LiveData<List<User>> get() = _selectUserList

    fun setSelectUserList(list: List<User>) {
        _selectUserList.value = list
    }

    fun clearSelectUserList() {
        _selectUserList.value = emptyList()
    }

    fun addSelectUser(user: User) {
        val updatedList = _selectUserList.value?.toMutableList() ?: mutableListOf()
        updatedList.add(user)
        _selectUserList.value = updatedList
    }

    fun removeSelectUser(user: User) {
        val updatedList = _selectUserList.value?.toMutableList() ?: mutableListOf()
        updatedList.remove(user)
        _selectUserList.value = updatedList
    }

    private val _selectGroupList = MutableLiveData<List<Group>>(emptyList())
    val selectGroupList: LiveData<List<Group>> get() = _selectGroupList

    fun setSelectGroupList(list: List<Group>) {
        _selectGroupList.value = list
    }

    fun clearSelectGroupList() {
        _selectGroupList.value = emptyList()
    }

    fun addSelectGroup(group: Group) {
        val updatedList = _selectGroupList.value?.toMutableList() ?: mutableListOf()
        updatedList.add(group)
        _selectGroupList.value = updatedList
    }

    fun removeSelectGroup(group: Group) {
        val updatedList = _selectGroupList.value?.toMutableList() ?: mutableListOf()
        updatedList.remove(group)
        _selectGroupList.value = updatedList
    }

    private val _selectUserForwardList = MutableLiveData<List<User>>(emptyList())
    val selectUserForwardList: LiveData<List<User>> get() = _selectUserForwardList

    fun setSelectUserForwardList(list: List<User>) {
        _selectUserForwardList.value = list
    }

    fun clearSelectUserForwardList() {
        _selectUserForwardList.value = emptyList()
    }

    fun addSelectUserForward(user: User) {
        val updatedList = _selectUserForwardList.value?.toMutableList() ?: mutableListOf()
        updatedList.add(user)
        _selectUserForwardList.value = updatedList
    }

    fun removeSelectUserForward(user: User) {
        val updatedList = _selectUserForwardList.value?.toMutableList() ?: mutableListOf()
        updatedList.remove(user)
        _selectUserForwardList.value = updatedList
    }

    private val _selectGroupForwardList = MutableLiveData<List<Group>>(emptyList())
    val selectGroupForwardList: LiveData<List<Group>> get() = _selectGroupForwardList

    fun setSelectGroupForwardList(list: List<Group>) {
        _selectGroupForwardList.value = list
    }

    fun clearSelectGroupForwardList() {
        _selectGroupForwardList.value = emptyList()
    }

    fun addSelectGroupForward(group: Group) {
        val updatedList = _selectGroupForwardList.value?.toMutableList() ?: mutableListOf()
        updatedList.add(group)
        _selectGroupForwardList.value = updatedList
    }

    fun removeSelectGroupForward(group: Group) {
        val updatedList = _selectGroupForwardList.value?.toMutableList() ?: mutableListOf()
        updatedList.remove(group)
        _selectGroupForwardList.value = updatedList
    }

}