package com.example.android.myproject.Create_Group

import android.util.Log
import com.example.android.myproject.Entities.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GroupDB {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mDBRef: DatabaseReference = FirebaseDatabase.getInstance().getReference()

    fun createGroup(groupName : String,groupAbout : String,groupMembers : ArrayList<String>,groupCreatedBy : String,profileUrl : String){
        var key = mDBRef.child("groups").push().key.toString()
        Log.d("GroupDB Key",key)
        val newGroup = Group(groupName, key, groupAbout, groupMembers, groupCreatedBy, profileUrl)
        mDBRef.child("groups").child(key).child("groupDetails").child(key).setValue(newGroup)

        var list = ArrayList<String>()
        list.add(groupCreatedBy)

        mDBRef.child("groups").child(key).child("groupDetails").child(key).child("adminList").setValue(list)
    }

}