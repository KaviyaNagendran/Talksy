package com.example.android.myproject.GroupChat_Page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.myproject.Entities.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupProfileViewModel : ViewModel() {

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> get() = _userList

    fun fetchGroupMembers(groupId: String, seeAll: Boolean) {
        val groupMembersRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupDetails")
            .child(groupId)
            .child("groupMembers")

        groupMembersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val memberIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                val limitedIds = if (seeAll) memberIds else memberIds.take(3)

                val fetchedUsers = mutableListOf<User>()
                var loadedCount = 0

                for (userId in limitedIds) {
                    FirebaseDatabase.getInstance().getReference("user").child(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(User::class.java)
                                if (user != null) {
                                    fetchedUsers.add(user)
                                }
                                loadedCount++
                                if (loadedCount == limitedIds.size) {
                                    _userList.value = fetchedUsers
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                if (limitedIds.isEmpty()) {
                    _userList.value = emptyList()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}