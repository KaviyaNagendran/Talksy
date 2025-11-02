package com.example.android.myproject.Archive_Chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.myproject.Chat_Main.MainActivity
import com.example.android.myproject.Chat_Page.ChatActivity.Companion.IsSearchBar
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class ArchiveChatActivity : AppCompatActivity(), OnUserLongClickListener {
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: ArchiveChatAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var topBar : FrameLayout
    private lateinit var appText : TextView
    private lateinit var selectCount : TextView
    private lateinit var back_navigation : ImageView
    private lateinit var archive : ImageView
    private lateinit var delete : ImageView
    private lateinit var loading : LinearLayout
    private lateinit var noDataFound : LinearLayout
    private lateinit var menu : ImageView

    companion object{
        var selectList : ArrayList<String> = ArrayList()
        var IsLongPressed : Boolean = false
        var count : Int = 0
    }

    private lateinit var viewModel: ArchiveChatViewModel
    private val messageListeners = mutableMapOf<String, ValueEventListener>()
    private var userListListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive_chats)

        viewModel = ViewModelProvider(this)[ArchiveChatViewModel::class.java]

        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()
        userList = ArrayList()
        adapter = ArchiveChatAdapter(this, userList, viewModel)

        userRecyclerView = findViewById(R.id.UserRecyclerView_archive)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        appText = findViewById<TextView>(R.id.appText_archive)
        selectCount = findViewById<TextView>(R.id.selectCount_archive)
        back_navigation = findViewById<ImageView>(R.id.back_navigation_archive)
        archive = findViewById<ImageView>(R.id.archive_out)
        delete = findViewById<ImageView>(R.id.delete_archive)
        topBar = findViewById<FrameLayout>(R.id.topBar_archive)
        loading = findViewById<LinearLayout>(R.id.progressBar_archive)
        noDataFound = findViewById<LinearLayout>(R.id.noDataFound_archive)
        menu = findViewById<ImageView>(R.id.menuButton_archive)

        noDataFound.visibility = View.GONE
        loading.visibility = View.GONE

        viewModel.isArchiveVisible.observe(this) { archive.visibility = it }
        viewModel.isDeleteVisible.observe(this) { delete.visibility = it }
        viewModel.isTopBarVisible.observe(this) { topBar.visibility = it }
        viewModel.isAppTextVisible.observe(this) { appText.visibility = it }
        viewModel.isSelectCountVisible.observe(this) { selectCount.visibility = it }
        viewModel.selectCount.observe(this) { selectCount.text = it.toString() }
        viewModel.isLongPressed.observe(this) { IsLongPressed = it }

        viewModel.archivedUsers.observe(this) {
            userList.clear()
            userList.addAll(it)
            adapter.notifyDataSetChanged()
        }

        val currentUserId = mAuth.currentUser?.uid
        if (!currentUserId.isNullOrEmpty()) {
            setupUserListListener()
        }

        back_navigation.setOnClickListener {
            if(delete.visibility == View.VISIBLE || archive.visibility == View.VISIBLE){
                viewModel.setSelectList(emptyList())
                viewModel.setIsLongPressed(false)
                resetUIWithViewModel()
                adapter.notifyDataSetChanged()
                fetchUsers()
            }else{
                finish()
            }
        }

        delete.setOnClickListener {
            val currentUserId = mAuth.currentUser?.uid

            val selected = viewModel.selectList.value ?: emptyList()
            for (receiver in selected) {
                val chatId = currentUserId + receiver
                val chatRef = FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatId)

                chatRef.removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Message deleted successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to delete messages: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.setSelectList(emptyList())
            viewModel.setIsLongPressed(false)
            resetUIWithViewModel()
            adapter.notifyDataSetChanged()
            fetchUsers()
        }

        archive.setOnClickListener {
            val currentUserId = mAuth.currentUser?.uid

            val selected = viewModel.selectList.value ?: emptyList()
            for (receiver in selected) {
                val chatId = currentUserId + receiver
                FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatId).child("archive").setValue(false)
            }

            viewModel.setSelectList(emptyList())
            viewModel.setIsLongPressed(false)
            resetUIWithViewModel()
            adapter.notifyDataSetChanged()
            fetchUsers()
        }

        fetchUsers()

        menu.setOnClickListener {
            showPopupMenu(it)
        }

    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.archive_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.unarchiveAll -> {
                    val currentUserId = mAuth.currentUser?.uid
                    val selected = viewModel.archivedUsers.value ?: emptyList()
                    for (receiver in selected) {
                        val chatId = currentUserId + receiver.uid
                        FirebaseDatabase.getInstance().getReference("chats")
                            .child(chatId).child("archive").setValue(false)
                    }
                    viewModel.setArchiveUsers(emptyList())
                    viewModel.setIsLongPressed(false)
                    resetUIWithViewModel()
                    adapter.notifyDataSetChanged()
                    fetchUsers()
                   true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    fun fetchUsers() {
        fetchArchivedUsers(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    override fun onUserLongClicked(position: Int) {
        val uid = userList[position].uid.toString()

        val currentList = viewModel.selectList.value?.toMutableList() ?: mutableListOf()
        if (!currentList.contains(uid)) {
            currentList.add(uid)
            viewModel.setSelectCount(currentList.size)
            viewModel.setSelectList(currentList)
        }
        viewModel.setIsLongPressed(true)
        viewModel.setIsAppTextVisible(View.INVISIBLE)
        viewModel.setIsSelectCountVisible(View.VISIBLE)
        viewModel.setIsArchiveVisible(View.VISIBLE)
        viewModel.setIsDeleteVisible(View.VISIBLE)
    }

    override fun onUserClicked(position: Int): Boolean {
        val uid = userList[position].uid.toString()
        val currentList = viewModel.selectList.value?.toMutableList() ?: mutableListOf()

        if (currentList.contains(uid)) {
            currentList.remove(uid)
            viewModel.setSelectCount(currentList.size)
            viewModel.setSelectList(currentList)

            if (currentList.isEmpty()) {
                viewModel.setIsLongPressed(false)
                resetUIWithViewModel()
            }

            adapter.notifyItemChanged(position)
            return true
        }

        return false
    }

    fun resetUIWithViewModel() {
        viewModel.setIsAppTextVisible(View.VISIBLE)
        viewModel.setIsSelectCountVisible(View.GONE)
//        viewModel.setIsBackNavigationVisible(View.GONE)
        viewModel.setIsArchiveVisible(View.GONE)
        viewModel.setIsDeleteVisible(View.GONE)
        viewModel.setIsTopBarVisible(View.VISIBLE)
        viewModel.setIsLongPressed(false)
    }

    fun fetchArchivedUsers(currentUserId: String) {
        loading.visibility = View.VISIBLE
        noDataFound.visibility = View.GONE

        val db = FirebaseDatabase.getInstance().getReference()
        val userRef = db.child("user")
        val chatRef = db.child("chats")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allUsers = snapshot.children.mapNotNull {
                    it.getValue(User::class.java)?.apply { uid = it.key }
                }

                val otherUsers = allUsers.filter { it.uid != currentUserId }
                val usersWithChats = mutableListOf<User>()

                if (otherUsers.isEmpty()) {
                    updateUI(emptyList())
                    return
                }

                chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(chatSnap: DataSnapshot) {
                        val chatKeys = chatSnap.children.mapNotNull { it.key }
                        var processedCount = 0

                        for (user in otherUsers) {
                            val key1 = currentUserId + user.uid
                            val key2 = user.uid + currentUserId
                            val chatKey = when {
                                chatKeys.contains(key1) -> key1
                                chatKeys.contains(key2) -> key2
                                else -> null
                            }

                            if (chatKey != null) {
                                db.child("chats").child(chatKey).child("archive")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val isArchived = snapshot.getValue(Boolean::class.java) == true
                                            if (isArchived) usersWithChats.add(user)

                                            processedCount++
                                            if (processedCount == otherUsers.size) {
                                                updateUI(usersWithChats)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            processedCount++
                                            if (processedCount == otherUsers.size) {
                                                updateUI(usersWithChats)
                                            }
                                        }
                                    })
                            } else {
                                processedCount++
                                if (processedCount == otherUsers.size) {
                                    updateUI(usersWithChats)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("fetchArchivedUsers", "ChatRef cancelled: ${error.message}")
                        updateUI(emptyList())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchArchivedUsers", "UserRef cancelled: ${error.message}")
                updateUI(emptyList())
            }
        })
    }

    private fun updateUI(users: List<User>) {
        viewModel.setArchiveUsers(users)
        loading.visibility = View.GONE

        if (users.isEmpty()) {
            noDataFound.visibility = View.VISIBLE
            userRecyclerView.visibility = View.GONE
        } else {
            noDataFound.visibility = View.GONE
            userRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupUserListListener() {
        val currentUserId = mAuth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        userListListener?.let {
            mDBRef.child("user").removeEventListener(it)
        }

        userListListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (postSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java)
                    user?.uid = postSnapshot.key
                    if (user != null && user.uid != currentUserId) {
                        val otherUserUid = user.uid!!
                        val chatRoomId = if (currentUserId < otherUserUid)
                            currentUserId + otherUserUid
                        else
                            otherUserUid + currentUserId

                        mDBRef.child("chats").child(chatRoomId).child("messages")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(msgSnapshot: DataSnapshot) {
                                    if (msgSnapshot.exists()) {
                                        userList.add(user)
                                        adapter.notifyItemInserted(userList.size - 1)
                                        setupMessageListenersForEachUser()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("MainActivity", "Error checking messages for $chatRoomId: ${error.message}")
                                }
                            })
                    }
                }
                adapter.notifyDataSetChanged()
                setupMessageListenersForEachUser()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ArchiveChatActivity, "Error fetching users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        mDBRef.child("user").addValueEventListener(userListListener!!)
    }

    private fun setupMessageListenersForEachUser() {
        val currentUserId = mAuth.currentUser?.uid ?: return
        clearMessageListeners()

        for (user in userList) {
            val otherUserUid = user.uid ?: continue
            val chatRoomId = if (currentUserId < otherUserUid)
                currentUserId + otherUserUid
            else
                otherUserUid + currentUserId

            val chatRef = mDBRef.child("chats").child(chatRoomId)

            val messageListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userIndex = userList.indexOfFirst { it.uid == otherUserUid }
                    if (userIndex in userList.indices) {
                        adapter.notifyItemChanged(userIndex)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ArchiveChatActivity", "Message listener failed: ${error.message}")
                }
            }

            val archiveListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isArchived = snapshot.getValue(Boolean::class.java) ?: false
                    if (!isArchived) {
                        val index = userList.indexOfFirst { it.uid == otherUserUid }
                        if (index != -1) {
                            userList.removeAt(index)
                            adapter.notifyItemRemoved(index)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ArchiveChatActivity", "Archive listener cancelled: ${error.message}")
                }
            }

            chatRef.child("messages").addValueEventListener(messageListener)
            chatRef.child("archive").addValueEventListener(archiveListener)

            messageListeners["$chatRoomId-messages"] = messageListener
            messageListeners["$chatRoomId-archive"] = archiveListener
        }
    }

    private fun clearMessageListeners() {
        for ((key, listener) in messageListeners) {
            val (chatId, type) = key.split("-")
            mDBRef.child("chats").child(chatId).child(type).removeEventListener(listener)
        }
        messageListeners.clear()
    }



}