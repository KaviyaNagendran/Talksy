package com.example.android.myproject.Create_Group

import android.annotation.SuppressLint
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.text.TextWatcher
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
import com.example.android.myproject.Entities.User
import com.example.android.myproject.Create_Group.GroupAdapter
import com.example.android.myproject.Create_Group.GroupDetailsActivity
import com.example.android.myproject.Create_Group.GroupViewModel
import com.example.android.myproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.internal.connection.RealCall

class GroupActivity : AppCompatActivity() {

    private lateinit var selectGroupUserRecyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var contactList: ArrayList<User>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var searchBarLayout : LinearLayout
    private lateinit var searchBar : EditText
    private lateinit var viewModel: GroupViewModel
    private lateinit var loading : LinearLayout
    private lateinit var nodatafound : LinearLayout
    private lateinit var close : ImageView
    private lateinit var back : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_group)

        loading = findViewById(R.id.progressBar_addMember)
        nodatafound = findViewById(R.id.noDataFound_addMember)
        loading.visibility = LinearLayout.VISIBLE
        nodatafound.visibility = LinearLayout.GONE
        close = findViewById<ImageView>(R.id.close_createGroup)
        back = findViewById<ImageView>(R.id.back_selectMember)
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()

        viewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        viewModel.addGroupMember(mAuth.currentUser!!.uid)

        selectGroupUserRecyclerView = findViewById<RecyclerView>(R.id.selectGroupUserRecyclerView)

        contactList = ArrayList()
        groupAdapter = GroupAdapter(this, contactList, viewModel)

        selectGroupUserRecyclerView.layoutManager = LinearLayoutManager(this)
        selectGroupUserRecyclerView.adapter = groupAdapter

        searchBarLayout = findViewById<LinearLayout>(R.id.selectGroupSearchbar)
        searchBar = findViewById<EditText>(R.id.selectGroupSearchEditText)

        val createGroupButton = findViewById<Button>(R.id.createGroupButton)

        fetchUser("")

        createGroupButton.setOnClickListener {
            var list = viewModel.groupMembers.value
            if (viewModel.isGroupEmpty() || list.size <=1) {
                Toast.makeText(this, "Please select at least one member", Toast.LENGTH_SHORT).show()
            } else {
                val members = ArrayList(viewModel.getGroupMembers())
                val intent = Intent(this@GroupActivity, GroupDetailsActivity::class.java)
                intent.putStringArrayListExtra("groupMembers", members)
                startActivity(intent)
                finish()
            }
        }

        searchBarLayout.setOnClickListener {
            searchBar.requestFocus()
        }

        searchBar.addTextChangedListener {
            if(searchBar.text.toString()==""){
                close.visibility = View.GONE
                fetchUser("")
            }
            else{
                close.visibility = View.VISIBLE
                fetchUser(searchBar.text.toString())
            }
        }

        close.visibility = View.GONE

        close.setOnClickListener {
            searchBar.setText("")
            close.visibility = View.GONE

        }

    }

    fun fetchUser(text : String){
        loading.visibility = LinearLayout.VISIBLE
        nodatafound.visibility = LinearLayout.GONE
        mDBRef.child("user").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUserId = mAuth.currentUser?.uid
                contactList.clear()

                for (postSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java)
                    user?.uid = postSnapshot.key

                    if (user != null && user.uid != currentUserId && user.name?.contains(text, ignoreCase = true) == true) {
                        contactList.add(user)
                    }
                }
                loading.visibility = LinearLayout.GONE
                if (contactList.isEmpty()) {
                    nodatafound.visibility = LinearLayout.VISIBLE
                } else {
                    nodatafound.visibility = LinearLayout.GONE
                }
                groupAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GroupActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}