package com.example.android.myproject.Add_Member_To_Group

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddMemberToGroupViewModel : ViewModel(){

    private val _select = MutableLiveData<View>()
    val select: LiveData<View> get() = _select

    fun setSelect(view: View) {
        _select.value = view
    }

    private val _groupMembers = MutableLiveData<List<String>>()
    val groupMembers: LiveData<List<String>> get() = _groupMembers

    fun setGroupMembers(members: List<String>) {
        _groupMembers.value = members
    }

    fun addGroupMember(member: String) {
        val currentMembers = _groupMembers.value.orEmpty().toMutableList()
        currentMembers.add(member)
        _groupMembers.value = currentMembers
    }

    fun removeGroupMember(member: String) {
        val currentMembers = _groupMembers.value.orEmpty().toMutableList()
        currentMembers.remove(member)
        _groupMembers.value = currentMembers
    }

    fun clearGroupMembers() {
        _groupMembers.value = emptyList()
    }

    init {
        _groupMembers.value = emptyList()
    }

    fun getGroupMembers(): List<String> {
        return _groupMembers.value.orEmpty()
    }

    fun isGroupEmpty(): Boolean {
        return _groupMembers.value.isNullOrEmpty()
    }

    fun getMemberCount(): Int {
        return _groupMembers.value?.size ?: 0
    }

    fun isMemberInGroup(member: String): Boolean {
        return _groupMembers.value?.contains(member) ?: false
    }

}