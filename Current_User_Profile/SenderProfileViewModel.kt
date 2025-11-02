package com.example.android.myproject.Current_User_Profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SenderProfileViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> get() = _userEmail

    private val _userAbout = MutableLiveData<String>()
    val userAbout: LiveData<String> get() = _userAbout

    private val _profileUrl = MutableLiveData<String>()
    val profileUrl: LiveData<String> get() = _profileUrl

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val databaseReference: DatabaseReference? = currentUserId?.let {
        FirebaseDatabase.getInstance().getReference("user").child(it)
    }

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _userName.value = snapshot.child("name").value?.toString() ?: ""
                _userEmail.value = snapshot.child("email").value?.toString() ?: ""
                _userAbout.value = snapshot.child("about").value?.toString() ?: ""
                _profileUrl.value = snapshot.child("profileUrl").value?.toString() ?: ""
                SenderProfileActivity.profileUrl = snapshot.child("profileUrl").value?.toString() ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                _error.value = error.message
            }
        })
    }

    fun updateUserName(newName: String, onComplete: (Boolean) -> Unit) {
        if (newName.isBlank() || databaseReference == null) {
            onComplete(false)
            return
        }
        databaseReference.child("name").setValue(newName).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _userName.value = newName
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun updateUserAbout(newAbout: String, onComplete: (Boolean) -> Unit) {
        if (newAbout.isBlank() || databaseReference == null) {
            onComplete(false)
            return
        }
        databaseReference.child("about").setValue(newAbout).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _userAbout.value = newAbout
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun updateUserProfile(newProfile: String, onComplete: (Boolean) -> Unit) {
        if (newProfile.isBlank() || databaseReference == null) {
            onComplete(false)
            return
        }
        databaseReference.child("profileUrl").setValue(newProfile).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _profileUrl.value = newProfile
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

}