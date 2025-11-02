package com.example.android.myproject.Add_New_Chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddNewChatActivity : AppCompatActivity() {

    private lateinit var selectUserChatRecyclerView: RecyclerView
    private lateinit var addChatAdapter: AddChatAdapter
    private lateinit var contactList: ArrayList<User>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var searchBar : EditText
    private lateinit var viewModel: AddNewChatActivityViewModel
    private lateinit var noDataFound : LinearLayout
    private lateinit var loading : LinearLayout
    private lateinit var close : ImageView
    private lateinit var back : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chat_page)
        noDataFound = findViewById<LinearLayout>(R.id.noDataFound_addChat)
        loading = findViewById<LinearLayout>(R.id.progressBar_addChat)
        loading.visibility = View.VISIBLE
        noDataFound.visibility = View.GONE

        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()

        viewModel = ViewModelProvider(this)[AddNewChatActivityViewModel::class.java]

        selectUserChatRecyclerView = findViewById<RecyclerView>(R.id.selectUserChatRecyclerView)

        contactList = ArrayList()
        addChatAdapter = AddChatAdapter(this, contactList, viewModel)

        selectUserChatRecyclerView.layoutManager = LinearLayoutManager(this)
        selectUserChatRecyclerView.adapter = addChatAdapter

        viewModel.contactList.observe(this) {
            contactList.clear()
            contactList.addAll(it)
            addChatAdapter.notifyDataSetChanged()
        }

        back = findViewById<ImageView>(R.id.back_addChat)
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }


        searchBar = findViewById(R.id.searchBar_AddChat)

        searchBar.setOnClickListener {
            searchBar.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)
        }

        searchBar.addTextChangedListener {
            fetchContacts(searchBar.text.toString())
            if(searchBar.text.toString() == ""){
                close.visibility = View.GONE
            } else {
                close.visibility = View.VISIBLE
            }
        }

        fetchContacts("")

        close = findViewById<ImageView>(R.id.close_addChat)
        close.visibility = View.GONE
        close.setOnClickListener {
            searchBar.text.clear()
            fetchContacts("")
            close.visibility = View.GONE
        }

    }

    fun fetchContacts(text: String) {
        loading.visibility = View.VISIBLE
        noDataFound.visibility = View.GONE
        mDBRef.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUserId = mAuth.currentUser?.uid
                val tempList = ArrayList<User>()

                for (postSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java)
                    user?.uid = postSnapshot.key

                    if (user != null && user.uid != currentUserId &&
                        user.name?.contains(text, ignoreCase = true) == true) {
                        tempList.add(user)
                    }
                }

                viewModel.setContactList(tempList)
                loading.visibility= View.GONE
                if(viewModel.contactList.value.size == 0){
                    noDataFound.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddNewChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}