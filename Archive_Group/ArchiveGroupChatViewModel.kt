package com.example.android.myproject.Archive_Group

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.myproject.Entities.Group
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArchiveGroupChatViewModel : ViewModel() {

    private val _isAppTextVisible = MutableLiveData<Int>(View.VISIBLE)
    val isAppTextVisible: LiveData<Int> get() = _isAppTextVisible
    fun setIsAppTextVisible(value: Int) {
        _isAppTextVisible.value = value
    }

    private val _isSelectCountVisible = MutableLiveData<Int>(View.GONE)
    val isSelectCountVisible: LiveData<Int> get() = _isSelectCountVisible
    fun setIsSelectCountVisible(value: Int) {
        _isSelectCountVisible.value = value
    }

    private val _isBackNavigationVisible = MutableLiveData<Int>(View.GONE)
    val isBackNavigationVisible: LiveData<Int> get() = _isBackNavigationVisible
    fun setIsBackNavigationVisible(value: Int) {
        _isBackNavigationVisible.value = value
    }

    private val _isArchiveVisible = MutableLiveData<Int>(View.GONE)
    val isArchiveVisible: LiveData<Int> get() = _isArchiveVisible
    fun setIsArchiveVisible(value: Int) {
        _isArchiveVisible.value = value
    }

    private val _isDeleteVisible = MutableLiveData<Int>(View.GONE)
    val isDeleteVisible: LiveData<Int> get() = _isDeleteVisible
    fun setIsDeleteVisible(value: Int) {
        _isDeleteVisible.value = value
    }

    private val _isTopBarVisible = MutableLiveData<Int>(View.VISIBLE)
    val isTopBarVisible: LiveData<Int> get() = _isTopBarVisible
    fun setIsTopBarVisible(value: Int) {
        _isTopBarVisible.value = value
    }

    private val _isLongPressed = MutableLiveData<Boolean>()
    val isLongPressed: LiveData<Boolean> get() = _isLongPressed
    fun setIsLongPressed(value: Boolean) {
        _isLongPressed.value = value
    }

    private val _selectList = MutableLiveData<List<String>>(emptyList())
    val selectList: LiveData<List<String>> get() = _selectList
    fun setSelectList(value: List<String>) {
        _selectList.value = value
    }

    private val _archivedGroupList = MutableLiveData<List<Group>>()
    val archivedGroupList: LiveData<List<Group>> get() = _archivedGroupList
    fun setArchivedGroupList(value: List<Group>) {
        _archivedGroupList.value = value
    }

    private val _groupCount = MutableLiveData<Int>()
    val groupCount: LiveData<Int> get() = _groupCount
    fun setGroupCount(value: Int) {
        _groupCount.value = value
    }

}