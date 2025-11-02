package com.example.android.myproject.Archive_Chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.myproject.Entities.User

class ArchiveChatViewModel : ViewModel() {

    private val _archivedUsers = MutableLiveData<List<User>>()
    val archivedUsers: LiveData<List<User>> get() = _archivedUsers

    fun setArchiveUsers(userList : List<User>){
        _archivedUsers.value = userList
    }

    private val _selectList = MutableLiveData<List<String>>()
    val selectList: LiveData<List<String>> get() = _selectList

    fun setSelectList(list : List<String>){
        _selectList.value = list
    }

    private val _isLongPressed = MutableLiveData<Boolean>(false)
    val isLongPressed: LiveData<Boolean> get() = _isLongPressed

    fun setIsLongPressed(value : Boolean){
        _isLongPressed.value = value
    }

    private val _isAppTextVisible = MutableLiveData<Int>()
    val isAppTextVisible: LiveData<Int> get() = _isAppTextVisible

    fun setIsAppTextVisible(value: Int){
        _isAppTextVisible.value = value
    }

    private val _isSelectCountVisible = MutableLiveData<Int>()
    val isSelectCountVisible: LiveData<Int> get() = _isSelectCountVisible

    fun setIsSelectCountVisible(value : Int){
        _isSelectCountVisible.value = value
    }

    private val _isBackNavigationVisible = MutableLiveData<Int>()
    val isBackNavigationVisible: LiveData<Int> get() = _isBackNavigationVisible

    fun setIsBackNavigationVisible(value : Int){
        _isBackNavigationVisible.value = value
    }

    private val _isArchiveVisible = MutableLiveData<Int>()
    val isArchiveVisible: LiveData<Int> get() = _isArchiveVisible

    fun setIsArchiveVisible(value : Int){
        _isArchiveVisible.value = value
    }

    private val _isDeleteVisible = MutableLiveData<Int>()
    val isDeleteVisible: LiveData<Int> get() = _isDeleteVisible

    fun setIsDeleteVisible(value : Int){
        _isDeleteVisible.value = value
    }

    private val _isTopBarVisible = MutableLiveData<Int>()
    val isTopBarVisible: LiveData<Int> get() = _isTopBarVisible

    fun setIsTopBarVisible(value : Int){
        _isTopBarVisible.value = value
    }

    private val _selectCount = MutableLiveData<Int>()
    val selectCount: LiveData<Int> get() = _selectCount

    fun setSelectCount(value : Int){
        _selectCount.value = value
    }

}
