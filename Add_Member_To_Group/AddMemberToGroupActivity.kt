package com.example.android.myproject.Add_Member_To_Group

import android.annotation.SuppressLint
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.myproject.Add_Member_To_Group.AddMemberToGroupAdapter
import com.example.android.myproject.Add_Member_To_Group.AddMemberToGroupViewModel
import com.example.android.myproject.GroupChat_Page.GroupProfileActivity
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.example.android.myproject.GroupChat_Page.GroupChatActivity.Companion.IsNoLongerMember
import com.example.android.myproject.GroupChat_Page.GroupChatActivity.Companion.groupUid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddMemberToGroupActivity : AppCompatActivity() {

    private lateinit var selectGroupUserRecyclerView: RecyclerView
    private lateinit var groupAdapter: AddMemberToGroupAdapter
    private lateinit var contactList: ArrayList<User>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference

    private lateinit var viewModel: AddMemberToGroupViewModel
    private lateinit var groupId : String
    private lateinit var close : ImageView
    private lateinit var searchBarLayout : LinearLayout
    private lateinit var searchBar : EditText
    private lateinit var loading : LinearLayout
    private lateinit var nodatafound : LinearLayout

    private lateinit var back : ImageView

    companion object {
        lateinit var existingMembers: ArrayList<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_members_view_contact)

        loading = findViewById<LinearLayout>(R.id.progressBar_newGroup)
        loading.visibility = View.VISIBLE
        nodatafound = findViewById<LinearLayout>(R.id.noDataFound_newGroup)
        nodatafound.visibility = View.GONE
        close = findViewById<ImageView>(R.id.close_addMembers)
        close.visibility = View.GONE
        back = findViewById<ImageView>(R.id.back_addMembers)
        back.visibility = View.VISIBLE

        back.setOnClickListener {
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()

        viewModel = ViewModelProvider(this)[AddMemberToGroupViewModel::class.java]

        selectGroupUserRecyclerView = findViewById<RecyclerView>(R.id.selectGroupUserRecyclerView_addGroup)

        contactList = ArrayList()
        groupAdapter = AddMemberToGroupAdapter(this, contactList, viewModel)

        selectGroupUserRecyclerView.layoutManager = LinearLayoutManager(this)
        selectGroupUserRecyclerView.adapter = groupAdapter

        groupId = intent.getStringExtra("groupId").toString()

        searchBarLayout = findViewById<LinearLayout>(R.id.selectGroupSearchbar_addGroup)
        searchBar = findViewById<EditText>(R.id.selectGroupSearchEditText_addGroup)

        fetchUser("")

        searchBarLayout.setOnClickListener {
            searchBar.requestFocus()
        }

        searchBar.addTextChangedListener {
            fetchUser(searchBar.text.toString())
            if(searchBar.text.toString().isNotEmpty()){
                close.visibility = View.VISIBLE
            }
            else{
                close.visibility = View.GONE
            }
        }

        close.setOnClickListener {
            searchBar.text.clear()
            searchBar.clearFocus()
            close.visibility = View.GONE
        }

        existingMembers = ArrayList()

        FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupDetails")
            .child(groupId)
            .child("groupMembers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val member = childSnapshot.getValue(String::class.java)
                    if (member != null) {
                        existingMembers.add(member)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("AddMemberToGroupActivity", "Failed to fetch group members: ${error.message}")
            }
        })

        val createGroupButton = findViewById<Button>(R.id.createGroupButton_addGroup)

        createGroupButton.setOnClickListener {
            if (viewModel.isGroupEmpty()) {
                Toast.makeText(this, "Please select at least one member", Toast.LENGTH_SHORT).show()
            } else {
                val members = ArrayList(viewModel.getGroupMembers())
                val groupRef = FirebaseDatabase.getInstance()
                    .getReference("groups")
                    .child(groupId)
                    .child("groupDetails")
                    .child(groupId)
                    .child("groupMembers")
                groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existingMembers = ArrayList<String>()
                        for (childSnapshot in snapshot.children) {
                            val member = childSnapshot.getValue(String::class.java)
                            if (member != null) {
                                existingMembers.add(member)
                            }
                        }

                        for (member in members) {
                            if (!existingMembers.contains(member)) {
                                existingMembers.add(member)
                            }
                        }

                        groupRef.setValue(existingMembers).addOnSuccessListener {
                            for (member in members) {
                                viewModel.removeGroupMember(member)
                                GroupProfileActivity.Companion.removePastMember(member)
                            }
                            val intent = Intent(this@AddMemberToGroupActivity, GroupProfileActivity::class.java)
                            intent.putExtra("groupID",groupUid)
                            intent.putExtra("NoLongerUser",IsNoLongerMember)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(
                                this@AddMemberToGroupActivity,
                                "Failed to save group members",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@AddMemberToGroupActivity,
                            "Error: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }
    }

    fun fetchUser(text : String){
        loading.visibility = View.VISIBLE
        nodatafound.visibility = View.GONE

        mDBRef.child("user").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUserId = mAuth.currentUser?.uid
                contactList.clear()

                for (postSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java)
                    user?.uid = postSnapshot.key

                    if (user != null && user.uid != currentUserId && user.name?.contains(text,ignoreCase = true)==true) {
                        contactList.add(user)
                    }
                }
                loading.visibility = View.GONE
                if(contactList.isEmpty()){
                    nodatafound.visibility = View.VISIBLE
                }
                groupAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AddMemberToGroupActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}