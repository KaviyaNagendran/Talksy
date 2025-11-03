package com.example.android.myproject.Chat_Main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.myproject.Add_New_Chat.AddNewChatActivity
import com.example.android.myproject.Archive_Chat.ArchiveChatActivity
import com.example.android.myproject.Group_Main.GroupMainActivity
import com.example.android.myproject.Login.LoginActivity
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.R
import com.example.android.myproject.Current_User_Profile.SenderProfileActivity
import com.example.android.myproject.Entities.User
import com.example.android.myproject.Listeners.CallBackListenerMessageSearch
import com.example.android.myproject.Listeners.MultipleMessageSelectListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.iterator

class MainActivity : AppCompatActivity(), OnUserLongClickListener, MultipleMessageSelectListener,
    CallBackListenerMessageSearch {
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var chatSearchRecyclerView: RecyclerView
    private lateinit var searchAdapter: MessageSearchAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var userUnreadRecyclerView : RecyclerView
    private lateinit var userUnreadList : ArrayList<User>
    private lateinit var userUnreadAdapter : UserAdapter
    private lateinit var userAllContactsList : ArrayList<User>
    private lateinit var userAllContactsListAdapter : UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var searchEditText : EditText
    private lateinit var profile: LinearLayout
    private lateinit var chats: LinearLayout
    private lateinit var addChats: LinearLayout
    private lateinit var group: LinearLayout
    private lateinit var contacts : TextView
    private lateinit var contacts_clicked : TextView
    private lateinit var unread : TextView
    private lateinit var unread_clicked : TextView
    private lateinit var messages : TextView
    private lateinit var messages_clicked : TextView
    private lateinit var topBar : FrameLayout
    private lateinit var scrollView : HorizontalScrollView
    private lateinit var searchBar : LinearLayout
    private var IsSearchBarActive : Boolean = false
    private lateinit var archieveBar : FrameLayout
    private val chatMessageListeners = mutableMapOf<String, ValueEventListener>()
    private var userListListener: ValueEventListener? = null
    private lateinit var appText : TextView
    private lateinit var selectCount : TextView
    private lateinit var back_navigation : ImageView
    private lateinit var archive : ImageView
    private lateinit var delete : ImageView
    private lateinit var noDataFound : LinearLayout
    private lateinit var color : ImageView
    private lateinit var loading : LinearLayout
    private lateinit var searchBarBack : ImageView

    companion object{
        var selectList : ArrayList<String> = ArrayList()
        var IsLongPressed : Boolean = false
        var IsSearchBarSelected : Boolean = false
    }

    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val swipeContainer = findViewById<View>(R.id.main)

        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        swipeContainer.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        Log.d("Dummy Message","Dummy Message");

        loading = findViewById<LinearLayout>(R.id.progressBar_main)
        loading.visibility = View.VISIBLE

        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        setUserOnlineStatus("online")

        // UserList

        userList = ArrayList()
        adapter = UserAdapter(this, userList,viewModel,supportFragmentManager)

        userRecyclerView = findViewById(R.id.UserRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        // Unread

        userUnreadList = ArrayList()
        userUnreadAdapter = UserAdapter(this, userUnreadList, viewModel,supportFragmentManager)

        userUnreadRecyclerView = findViewById(R.id.UserUnreadRecyclerView)
        userUnreadRecyclerView.layoutManager = LinearLayoutManager(this)
        userUnreadRecyclerView.adapter = userUnreadAdapter

        // SearchList

        messageList = ArrayList()
        searchAdapter = MessageSearchAdapter(this, messageList,viewModel)

        chatSearchRecyclerView = findViewById(R.id.chatSearchRecyclerView)
        chatSearchRecyclerView.layoutManager = LinearLayoutManager(this)
        chatSearchRecyclerView.adapter = searchAdapter

        noDataFound = findViewById<LinearLayout>(R.id.noDataFound_main)

        fetchUsers()

        Log.d("MainActivity",viewModel.IsContacts.value.toString())
        Log.d("MainActivity",viewModel.IsMessages.value.toString())
        Log.d("MainActivity",viewModel.IsUnread.value.toString())

        if(viewModel.IsContacts.value == true &&
            viewModel.IsMessages.value == true &&
            viewModel.IsUnread.value == true){
            viewModel.setActiveRecyclerView(message = false, user = false, unread = false)
            viewModel.setIsContacts(false)
            viewModel.setIsUnread(false)
            viewModel.setIsMessages(false)
            fetchUsers()
        }
        else if (viewModel.IsContacts.value != true &&
            viewModel.IsMessages.value != true &&
            viewModel.IsUnread.value != true
        ) {

            viewModel.setIsContacts(false)
            viewModel.setIsUnread(false)
            viewModel.setIsMessages(false)

            viewModel.showIsContactBar(true)
            viewModel.showIsContactBar_clicked(false)
            viewModel.showIsUnreadBar(true)
            viewModel.showIsUnreadBar_clicked(false)
            viewModel.showIsMessageBar(true)
            viewModel.showIsMessageBar_clicked(false)

            fetchUsers()
            viewModel.setActiveRecyclerView(message = false, user = true, unread = false)
            Log.d("MainActivity", "if_condition")
        }
        else {

            if (viewModel.IsContacts.value == true) {
                Log.d("MainActivity", "contacts")
                userRecyclerView.visibility = View.VISIBLE
                chatSearchRecyclerView.visibility = View.GONE
                userUnreadRecyclerView.visibility = View.GONE

            } else if (viewModel.IsMessages.value == true) {
                Log.d("MainActivity", "messages")

                chatSearchRecyclerView.visibility = View.VISIBLE
                userRecyclerView.visibility = View.GONE
                userUnreadRecyclerView.visibility = View.GONE

            } else if (viewModel.IsUnread.value == true) {

                Log.d("MainActivity", "chats")
                userUnreadRecyclerView.visibility = View.VISIBLE
                userRecyclerView.visibility = View.GONE
                chatSearchRecyclerView.visibility = View.GONE

            }
            Log.d("MainActivity", "else_condition")
        }

        Log.d("MainActivity",(userRecyclerView.visibility == View.VISIBLE).toString())
        Log.d("MainActivity",(userUnreadRecyclerView.visibility == View.VISIBLE).toString())
        Log.d("MainActivity",(chatSearchRecyclerView.visibility == View.VISIBLE).toString())

        updateSentToReceived()

        val menuButton: ImageView = findViewById(R.id.menuButton)

        menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        searchEditText = findViewById<EditText>(R.id.searchEditText)

        searchEditText.setOnTouchListener { v, event ->
            v.requestFocus()
            v.performClick()
            false
        }

        profile = findViewById<LinearLayout>(R.id.profile)
        addChats = findViewById<LinearLayout>(R.id.addChats)
        group = findViewById<LinearLayout>(R.id.group)
        chats = findViewById<LinearLayout>(R.id.chats)
        scrollView = findViewById<HorizontalScrollView>(R.id.scrollView)
        searchBar = findViewById<LinearLayout>(R.id.searchbar)

        contacts = findViewById<TextView>(R.id.contacts)
        unread = findViewById<TextView>(R.id.unread)
        messages = findViewById<TextView>(R.id.messages)

        contacts_clicked = findViewById<TextView>(R.id.contacts_clicked)
        unread_clicked = findViewById<TextView>(R.id.unread_clicked)
        messages_clicked = findViewById<TextView>(R.id.messages_clicked)

        appText = findViewById<TextView>(R.id.appText)
        selectCount = findViewById<TextView>(R.id.selectCount)
        back_navigation = findViewById<ImageView>(R.id.back_navigation)
        archive = findViewById<ImageView>(R.id.archive_in)
        delete = findViewById<ImageView>(R.id.delete)
        topBar = findViewById<FrameLayout>(R.id.topBar)
        searchBarBack = findViewById<ImageView>(R.id.searchBar_back)
        searchBarBack.visibility = View.GONE

        findViewById<ImageView>(R.id.search_bar_close).visibility = View.GONE

        viewModel.isAppTextVisible.observe(this) { appText.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.isSelectCountVisible.observe(this) { selectCount.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.isBackNavigationVisible.observe(this) { back_navigation.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.isArchiveVisible.observe(this) { archive.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.isDeleteVisible.observe(this) { delete.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.isTopBarVisible.observe(this) { topBar.visibility = if (it) View.VISIBLE else View.GONE }

        viewModel.IsContactBar.observe(this) {
            contacts.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.IsContactBar_clicked.observe(this) { contacts_clicked.visibility = if (it) View.VISIBLE else View.GONE }

        viewModel.IsMessageBar.observe(this) {
            messages.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.IsMessageBar_clicked.observe(this) { messages_clicked.visibility = if (it) View.VISIBLE else View.GONE }

        viewModel.IsUnreadBar.observe(this) {
            unread.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.IsUnreadBar_clicked.observe(this) { unread_clicked.visibility = if (it) View.VISIBLE else View.GONE }

        viewModel.showUserRecyclerView.observe(this) {
            userRecyclerView.visibility = if (it) View.VISIBLE else View.GONE
            userUnreadRecyclerView.visibility = View.GONE
            chatSearchRecyclerView.visibility = View.GONE
        }

        viewModel.showMessageRecyclerView.observe(this) {
            chatSearchRecyclerView.visibility = if (it) View.VISIBLE else View.GONE
            userRecyclerView.visibility = View.GONE
            userUnreadRecyclerView.visibility = View.GONE
        }

        viewModel.showUnreadRecyclerView.observe(this) {
            userUnreadRecyclerView.visibility = if (it) View.VISIBLE else View.GONE
            userRecyclerView.visibility = View.GONE
            chatSearchRecyclerView.visibility = View.GONE
        }

        contacts.setOnClickListener {

            viewModel.setIsContacts(true)
            viewModel.showIsContactBar(false)
            viewModel.showIsContactBar_clicked(true)

            viewModel.setIsUnread(false)
            viewModel.showIsUnreadBar(true)
            viewModel.showIsUnreadBar_clicked(false)

            viewModel.setIsMessages(false)
            viewModel.showIsMessageBar(true)
            viewModel.showIsMessageBar_clicked(false)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectList()
            adapter.notifyDataSetChanged()
            resetUI()

            viewModel.setActiveRecyclerView(user = true, message = false, unread = false)
            fetchMessages(searchEditText.text.toString())

        }

        contacts_clicked.setOnClickListener {

            viewModel.setIsContacts(false)
            viewModel.showIsContactBar(true)
            viewModel.showIsContactBar_clicked(false)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectList()
            adapter.notifyDataSetChanged()
            resetUI()

            viewModel.setActiveRecyclerView(user = false, message = false, unread = false)

            if(IsSearchBarSelected){
                fetchMessages(searchEditText.text.toString())
            }
            else {
                fetchUsers()
            }

        }

        unread.setOnClickListener {

            viewModel.setIsUnread(true)
            viewModel.showIsUnreadBar(false)
            viewModel.showIsUnreadBar_clicked(true)

            viewModel.setIsContacts(false)
            viewModel.showIsContactBar(true)
            viewModel.showIsContactBar_clicked(false)

            viewModel.setIsMessages(false)
            viewModel.showIsMessageBar(true)
            viewModel.showIsMessageBar_clicked(false)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectList()
            adapter.notifyDataSetChanged()
            resetUI()

            viewModel.setActiveRecyclerView(user = false, message = false, unread = true)
            fetchMessages(searchEditText.text.toString())

        }

        unread_clicked.setOnClickListener {

            viewModel.setIsUnread(false)
            viewModel.showIsUnreadBar(true)
            viewModel.showIsUnreadBar_clicked(false)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectList()
            adapter.notifyDataSetChanged()
            resetUI()

            viewModel.setActiveRecyclerView(user = false, message = false, unread = false)
            if(IsSearchBarSelected){
                fetchMessages(searchEditText.text.toString())
            }
            else {
                fetchUsers()
            }

        }

        messages.setOnClickListener {

            viewModel.setIsMessages(true)
            viewModel.showIsMessageBar(false)
            viewModel.showIsMessageBar_clicked(true)

            viewModel.setIsUnread(false)
            viewModel.showIsUnreadBar(true)
            viewModel.showIsUnreadBar_clicked(false)

            viewModel.setIsContacts(false)
            viewModel.showIsContactBar(true)
            viewModel.showIsContactBar_clicked(false)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectList()
            adapter.notifyDataSetChanged()
            resetUI()

            viewModel.setActiveRecyclerView(user = false, message = true, unread = false)
            fetchMessages(searchEditText.text.toString())

        }

        messages_clicked.setOnClickListener {

            viewModel.setIsMessages(false)
            viewModel.showIsMessageBar(true)
            viewModel.showIsMessageBar_clicked(false)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectList()
            adapter.notifyDataSetChanged()
            resetUI()

            viewModel.setActiveRecyclerView(user = false, message = false, unread = false)
            if(IsSearchBarSelected){
                fetchMessages(searchEditText.text.toString())
            }
            else {
                fetchUsers()
            }

        }

        viewModel.appTextVisibility.observe(this) { appText.visibility = it }

        viewModel.messageList.observe(this) {
            messageList.clear()
            messageList.addAll(it)
            searchAdapter.notifyDataSetChanged()
        }

        viewModel.userList.observe(this) { users ->
            userList.clear()
            userList.addAll(users)
            adapter.notifyDataSetChanged()
        }

        viewModel.selectList.observe(this) {
            selectCount.text = it.size.toString()

            if (viewModel.IsUnread.value == true) {
                userUnreadAdapter.notifyDataSetChanged()
            } else {
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.userUnreadList.observe(this) {
            userUnreadList.clear()
            userUnreadList.addAll(it)
            userUnreadAdapter.notifyDataSetChanged()
        }

        viewModel.IsLongPressed.observe(this) { isActive ->
            if (isActive) {
                viewModel.setAppTextVisibility(View.INVISIBLE)
                viewModel.showSelectCount(true)
                viewModel.showBackNavigation(true)
                viewModel.showArchive(true)
                viewModel.showDelete(true)
            } else {
                viewModel.setAppTextVisibility(View.VISIBLE)
                viewModel.showSelectCount(false)
                viewModel.showBackNavigation(false)
                viewModel.showArchive(false)
                viewModel.showDelete(false)
            }
        }

        searchEditText.addTextChangedListener {
            if(searchEditText.text.length == 0){
                findViewById<ImageView>(R.id.search_bar_close).visibility = View.GONE
            }
            else{
                findViewById<ImageView>(R.id.search_bar_close).visibility = View.VISIBLE
            }
            fetchMessages(searchEditText.text.toString())
        }

        findViewById<ImageView>(R.id.search_bar_close).setOnClickListener {
            searchEditText.text.clear()
            fetchMessages("")
        }

        searchEditText.setOnClickListener {
            viewModel.clearSelectList()
            adapter.notifyDataSetChanged()
            searchAdapter.notifyDataSetChanged()
            userUnreadAdapter.notifyDataSetChanged()
            if(viewModel.IsMessages.value!= true && viewModel.IsUnread.value != true && viewModel.IsContacts.value != true){
//                fetchMessages(searchEditText.text.toString())
                fetchUsers()
            }
            viewModel.setIsSearchBarActive(true)
            viewModel.showTopBar(false)
            searchBarBack.visibility = View.VISIBLE
            IsSearchBarSelected=true
        }

        searchBarBack.setOnClickListener {
            viewModel.setIsSearchBarActive(false)
            viewModel.showTopBar(true)
            searchBarBack.visibility = View.GONE
            searchEditText.text.clear()
            fetchUsers()
            IsSearchBarSelected=false
        }

        chats.isSelected = true

        val drawableId = resolveThemeDrawable(R.attr.chats_contrast)
        findViewById<ImageView>(R.id.chatlogo).setImageResource(drawableId)

        val accentColor = resolveThemeColor(R.attr.colorAccent)
        findViewById<TextView>(R.id.chatlogo_text).setTextColor(accentColor)

        group.isSelected = false

        profile.setOnClickListener {
            val intent = Intent(this@MainActivity, SenderProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            startActivity(intent)
        }

        addChats.setOnClickListener {
            val intent = Intent(this@MainActivity, AddNewChatActivity::class.java)
            startActivity(intent)
        }

        group.setOnClickListener {
            val intent = Intent(this@MainActivity, GroupMainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        delete.setOnClickListener {
            val currentUserId = mAuth.currentUser?.uid
            val selectedUsers = viewModel.selectList.value ?: emptyList()

            for (receiver in selectedUsers) {
                val chatId = currentUserId + receiver
                val chatRef = FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatId)

                chatRef.removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Message deleted successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to delete messages: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.clearSelectList()
            viewModel.setIsLongPressed(false)
            resetUI()
            adapter.notifyDataSetChanged()
            fetchUsers()
        }

        archive.setOnClickListener {
            val currentUserId = mAuth.currentUser?.uid
            val selectedUsers = viewModel.selectList.value ?: emptyList()

            for (receiver in selectedUsers) {
                val chatId = currentUserId + receiver
                FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatId).child("archive").setValue(true)
            }

            viewModel.clearSelectList()
            viewModel.setIsLongPressed(false)
            resetUI()
            adapter.notifyDataSetChanged()
            fetchUsers()
            setListenerForArchieveBar()
        }

        archieveBar = findViewById<FrameLayout>(R.id.archieveBar)

        archieveBar.visibility = View.GONE

        setListenerForArchieveBar()

        archieveBar.setOnClickListener {
            val intent = Intent(this@MainActivity, ArchiveChatActivity::class.java)
            startActivity(intent)
        }

        back_navigation.setOnClickListener {

            viewModel.setAppTextVisibility(View.VISIBLE)
            viewModel.showSelectCount(false)
            viewModel.showBackNavigation(false)
            viewModel.showArchive(false)
            viewModel.showDelete(false)

            viewModel.clearSelectList()
            viewModel.setIsLongPressed(false)
            resetUI()

            adapter.notifyDataSetChanged()
        }

    }

    fun setListenerForArchieveBar() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("chats")
        var archiveShown = false
        archieveBar = findViewById<FrameLayout>(R.id.archieveBar)

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (chatSnap in snapshot.children) {
                    val chatId = chatSnap.key ?: continue

                    if (chatId.startsWith(currentUserId)) {
                        val isArchived = chatSnap.child("archive").getValue(Boolean::class.java) == true
                        if (isArchived) {
                            archieveBar.visibility = View.VISIBLE
                            archiveShown = true
                            break
                        }
                    }
                }

                if (!archiveShown) {
                    archieveBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ArchiveBar", "Error reading chats: ${error.message}")
            }
        })
    }

    fun resolveThemeDrawable(@AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.resourceId
    }

    fun resolveThemeColor(@AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
    private val lastMessageTimestamps = mutableMapOf<String, Long>()

    private fun setupChatMessageListeners(chatUserList: List<User>) {
        clearChatMessageListeners()

        val currentUserId = mAuth.currentUser?.uid ?: return

        for (user in chatUserList) {
            val receiverId = user.uid ?: continue
            val chatId = currentUserId + receiverId

            val messagesRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(chatId)
                .child("messages")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var latestTime = 0L
                    for (messageSnap in snapshot.children) {
                        val timestamp = messageSnap.child("timestamp").getValue(Long::class.java) ?: 0L
                        if (timestamp > latestTime) {
                            latestTime = timestamp
                        }
                    }

                    lastMessageTimestamps[chatId] = latestTime

                    val sortedList = chatUserList.sortedByDescending {
                        val chatIdForUser = currentUserId + (it.uid ?: "")
                        lastMessageTimestamps[chatIdForUser] ?: 0L
                    }

                    adapter.updateList(sortedList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatMainActivity", "Listener cancelled for $receiverId: ${error.message}")
                }
            }

            messagesRef.addValueEventListener(listener)
            chatMessageListeners[chatId] = listener
        }
        adapter.notifyDataSetChanged()
    }
    private fun clearChatMessageListeners() {
        for ((chatId, listener) in chatMessageListeners) {
            FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(chatId)
                .child("messages")
                .removeEventListener(listener)
        }
        chatMessageListeners.clear()
    }

    fun resetUI(){
        viewModel.setAppTextVisibility(View.VISIBLE)
        viewModel.showSelectCount(false)
        viewModel.showBackNavigation(false)
        viewModel.showArchive(false)
        viewModel.showDelete(false)
        viewModel.setIsLongPressed(false)
    }

    override fun onUserLongClicked(position: Int) {
        viewModel.addToSelectList(userList[position].uid.toString())
        val selectedUsers = viewModel.selectList.value ?: emptyList()
        selectCount.text = selectedUsers.size.toString()

        appText = findViewById<TextView>(R.id.appText)
        selectCount = findViewById<TextView>(R.id.selectCount)
        back_navigation = findViewById<ImageView>(R.id.back_navigation)
        archive = findViewById<ImageView>(R.id.archive_in)
        delete = findViewById<ImageView>(R.id.delete)

        viewModel.setAppTextVisibility(View.INVISIBLE)
        viewModel.showSelectCount(true)
        viewModel.showBackNavigation(true)
        viewModel.showArchive(true)
        viewModel.showDelete(true)

    }

    override fun onUserClicked(position: Int): Boolean {
        val uid = userList[position].uid.toString()
        val selectedUsers = viewModel.selectList.value ?: emptyList()

        if (selectedUsers.contains(uid)) {
            viewModel.removeFromSelectList(uid)
            val select = viewModel.selectList.value ?: emptyList()
            selectCount.text = select.size.toString()

            if (select.isEmpty()) {
                viewModel.setIsLongPressed(false)
                resetUI()
            }

            return true
        } else {
            return false
        }
    }

    override fun onBackPressed() {
        if (viewModel.IsLongPressed.value == true) {
            viewModel.clearSelectList()
            viewModel.setIsLongPressed(false)
            resetUI()
            adapter.notifyDataSetChanged()
        }
        else if (viewModel.IsSearchBarActive.value == true) {

            viewModel.setIsSearchBarActive(false)
            viewModel.showTopBar(true)
            searchEditText.text.clear()

            viewModel.setIsMessages(false)
            viewModel.setIsUnread(false)
            viewModel.setIsContacts(false)

            viewModel.showIsMessageBar(true)
            viewModel.showIsMessageBar_clicked(false)

            viewModel.showIsUnreadBar(true)
            viewModel.showIsUnreadBar_clicked(false)

            viewModel.showIsContactBar(true)
            viewModel.showIsContactBar_clicked(false)

            userRecyclerView.visibility = View.VISIBLE
            chatSearchRecyclerView.visibility = View.GONE
            userUnreadRecyclerView.visibility = View.GONE

            searchBarBack.visibility = View.GONE
            IsSearchBarSelected = false
            loading.visibility = View.GONE

            adapter.notifyDataSetChanged()

        } else {
            super.onBackPressed()
        }
    }

    fun fetchMessages(text: String) {
        loading.visibility = View.VISIBLE
        userRecyclerView.visibility = View.GONE
        chatSearchRecyclerView.visibility = View.GONE
        userUnreadRecyclerView.visibility = View.GONE

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("chats")

        if (viewModel.IsMessages.value == true) {

            noDataFound.visibility = View.GONE

            Log.d("Messaging","Executing")

            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val matchedMessages = mutableListOf<Message>()
                    for (postSnapshot in snapshot.children) {
                        val roomName = postSnapshot.key ?: continue
                        if (roomName.startsWith(currentUserId)) {
                            val messagesSnapshot = postSnapshot.child("messages")
                            for (messageSnap in messagesSnapshot.children) {
                                val message = messageSnap.getValue(Message::class.java)
                                if (message?.message?.contains(text, ignoreCase = true) == true) {
                                    matchedMessages.add(message)
                                }
                            }
                        }
                    }
                    viewModel.setMessageList(matchedMessages.sortedByDescending { it.timeStamp })
                    loading.visibility = View.GONE
                    if(viewModel.messageList.value.size == 0 ){
                        noDataFound.visibility = View.VISIBLE
                        userRecyclerView.visibility = View.GONE
                        chatSearchRecyclerView.visibility = View.GONE
                        userUnreadRecyclerView.visibility = View.GONE
                    }
                    else {
                        noDataFound.visibility = View.GONE
                        userRecyclerView.visibility = View.GONE
                        chatSearchRecyclerView.visibility = View.VISIBLE
                        userUnreadRecyclerView.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FetchMessages", "Database error: ${error.message}")
                }
            })

        }
        else if (viewModel.IsUnread.value == true) {
            noDataFound.visibility = View.GONE

            Log.d("unreading","Executing")

            val userRefNode = FirebaseDatabase.getInstance().getReference("user")
            val chatRef = FirebaseDatabase.getInstance().getReference("chats")

            userRefNode.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    val allUsers = userSnapshot.children.mapNotNull {
                        it.getValue(User::class.java)?.apply { uid = it.key }
                    }

                    chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(chatSnapshot: DataSnapshot) {
                            val usersWithUnread = mutableListOf<Pair<User, Long>>()

                            for (user in allUsers) {
                                if (user.uid == currentUserId) continue

                                val chatKey1 = "$currentUserId${user.uid}"
                                val chatKey2 = "${user.uid}$currentUserId"
                                val chatNode = chatSnapshot.child(chatKey1).takeIf { it.exists() }
                                    ?: chatSnapshot.child(chatKey2).takeIf { it.exists() }

                                var hasUnread = false
                                var latestUnreadTimestamp = 0L

                                chatNode?.child("messages")?.children?.forEach { messageSnap ->
                                    val senderId = messageSnap.child("sender").getValue(String::class.java)
                                    val status = messageSnap.child("status").getValue(String::class.java)
                                    val timeStamp = messageSnap.child("timeStamp").getValue(Long::class.java) ?: 0L

                                    if (senderId != currentUserId && status != "seen") {
                                        hasUnread = true
                                        latestUnreadTimestamp = maxOf(latestUnreadTimestamp, timeStamp)
                                    }
                                }

                                if (hasUnread) {
                                    usersWithUnread.add(Pair(user, latestUnreadTimestamp))
                                }
                            }

                            val sortedUnread = usersWithUnread.sortedByDescending { it.second }.map { it.first }
                            viewModel.setUserUnreadList(sortedUnread)
                            loading.visibility = View.GONE

                            if(viewModel.userUnreadList.value.size == 0 ){
                                noDataFound.visibility = View.VISIBLE
                                userRecyclerView.visibility = View.GONE
                                chatSearchRecyclerView.visibility = View.GONE
                                userUnreadRecyclerView.visibility = View.GONE
                            }
                            else {
                                noDataFound.visibility = View.GONE
                                userRecyclerView.visibility = View.GONE
                                chatSearchRecyclerView.visibility = View.GONE
                                userUnreadRecyclerView.visibility = View.VISIBLE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("ChatSearch", "Chat fetch failed: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatSearch", "User fetch failed: ${error.message}")
                }
            })
        }
        else {
            noDataFound.visibility = View.GONE

            Log.d("Contacting","Executing")

            val userRefNode = FirebaseDatabase.getInstance().getReference("user")
            val chatRef = FirebaseDatabase.getInstance().getReference("chats")

            userRefNode.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    val allUsers = userSnapshot.children.mapNotNull {
                        it.getValue(User::class.java)?.apply { uid = it.key }
                    }

                    chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(chatSnapshot: DataSnapshot) {
                            val usersWithTime = mutableListOf<Pair<User, Long>>()
                            for (user in allUsers) {
                                if (user.uid == currentUserId) continue

                                val chatKey = currentUserId + user.uid
                                val chatNode = chatSnapshot.child(chatKey)
                                val matchesText = user.name?.contains(text, ignoreCase = true) == true

                                if (chatNode.exists() && matchesText) {
                                    val lastTimestamp =
                                        chatNode.child("messages").children.maxOfOrNull {
                                            it.child("timeStamp").getValue(Long::class.java) ?: 0L
                                        } ?: 0L
                                    usersWithTime.add(Pair(user, lastTimestamp))
                                }
                            }

                            val sortedUsers = usersWithTime.sortedByDescending { it.second }.map { it.first }
                            viewModel.setUserList(sortedUsers)
                            loading.visibility = View.GONE

                            if(viewModel.userList.value.size == 0 ){
                                noDataFound.visibility = View.VISIBLE
                                userRecyclerView.visibility = View.GONE
                                chatSearchRecyclerView.visibility = View.GONE
                                userUnreadRecyclerView.visibility = View.GONE
                            }
                            else {
                                noDataFound.visibility = View.GONE
                                userRecyclerView.visibility = View.VISIBLE
                                chatSearchRecyclerView.visibility = View.GONE
                                userUnreadRecyclerView.visibility = View.GONE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("ChatSearch", "Chat fetch failed: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatSearch", "User fetch failed: ${error.message}")
                }
            })

        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    setUserOnlineStatus("offline")
                    mAuth.signOut()
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    finish()
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    fun updateSentToReceived() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatsRef = FirebaseDatabase.getInstance().getReference("chats")

        chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (chatSnapshot in snapshot.children) {
                    val roomName = chatSnapshot.key ?: continue

                    if (roomName.endsWith(currentUserId)) {
                        val messagesSnapshot = chatSnapshot.child("messages")
                        for (messageSnap in messagesSnapshot.children) {
                            val message = messageSnap.getValue(Message::class.java)
                            if (message?.status == "sent" && message.sender != currentUserId) {
                                messageSnap.ref.child("status").setValue("received")
                            }
                        }
                    }
                    if (roomName.startsWith(currentUserId)) {
                        val messagesSnapshot = chatSnapshot.child("messages")
                        for (messageSnap in messagesSnapshot.children) {
                            val message = messageSnap.getValue(Message::class.java)
                            if (message?.status == "sent" && message.sender != currentUserId) {
                                messageSnap.ref.child("status").setValue("received")
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to update to received: ${error.message}")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    fun fetchUsers() {
        userList.clear()

        loading.visibility = View.VISIBLE
        noDataFound.visibility = View.GONE

        userRecyclerView.visibility = View.GONE
        chatSearchRecyclerView.visibility = View.GONE
        userUnreadRecyclerView.visibility = View.GONE

        val db = FirebaseDatabase.getInstance().getReference()
        val userRefNode = db.child("user")
        val chatRef = db.child("chats")
        val currentUserId = mAuth.currentUser?.uid ?: return

        userRefNode.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                userList.clear()
                val allUsers = userSnapshot.children.mapNotNull {
                    it.getValue(User::class.java)?.apply { uid = it.key }
                }

                val usersWithTime = mutableListOf<Pair<User, Long>>()
                var processedCount = 0
                val totalUsersToCheck = allUsers.count { it.uid != currentUserId }

                for (user in allUsers) {
                    if (user.uid == currentUserId) {
                        processedCount++
                        continue
                    }

                    val chatKey = currentUserId + user.uid

                    isChatArchived(chatKey) { isArchived ->
                        if (!isArchived) {
                            chatRef.child(chatKey).child("messages")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(messagesSnapshot: DataSnapshot) {
                                        val lastTimestamp = messagesSnapshot.children.maxOfOrNull {
                                            it.child("timeStamp").getValue(Long::class.java) ?: 0L
                                        } ?: 0L

                                        if (lastTimestamp > 0L) {
                                            usersWithTime.add(Pair(user, lastTimestamp))
                                        }

                                        processedCount++
                                        if (processedCount == totalUsersToCheck) {
                                            updateUserList(usersWithTime)
                                            loading.visibility = View.GONE
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        processedCount++
                                        if (processedCount == totalUsersToCheck) {
                                            updateUserList(usersWithTime)
                                            loading.visibility = View.GONE
                                        }
                                    }
                                })
                        } else {
                            processedCount++
                            if (processedCount == totalUsersToCheck) {
                                updateUserList(usersWithTime)
                                loading.visibility = View.GONE
                            }
                        }
                    }
                }

                if (totalUsersToCheck == 0) {
                    updateUserList(usersWithTime)
                    loading.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserFetch", "User fetch failed: ${error.message}")
            }
        })
        noDataFound.visibility = View.GONE
        userRecyclerView.visibility = View.VISIBLE
        chatSearchRecyclerView.visibility = View.GONE
        userUnreadRecyclerView.visibility = View.GONE
    }

    private fun updateUserList(usersWithTime: List<Pair<User, Long>>) {
        userList.clear()
        userList.addAll(usersWithTime.sortedByDescending { it.second }.map { it.first })
        if(userList.isEmpty){
            noDataFound.visibility = View.VISIBLE
        }
        setupChatMessageListeners(userList)
        adapter.notifyDataSetChanged()
    }

    fun isChatArchived(senderRoom: String, callback: (Boolean) -> Unit) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("chats")
            .child(senderRoom)
            .child("archive")

        ref.get().addOnSuccessListener { snapshot ->
            val isArchived = snapshot.getValue(Boolean::class.java) == true
            callback(isArchived)
        }.addOnFailureListener {
            callback(false)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUsers()
        setUserOnlineStatus("online")
        setListenerForArchieveBar()
    }

    override fun onPause() {
        super.onPause()
        setUserOnlineStatus("offline")
    }

    override fun onDestroy() {
        super.onDestroy()
        setUserOnlineStatus("offline")
    }

    private fun setUserOnlineStatus(status: String) {
        mAuth.currentUser?.uid?.let { userId ->
            mDBRef.child("user").child(userId).child("activeChatUid").setValue(status)
        }
    }

    override fun onMultiSelectStarted() {
        if(viewModel.IsMessages.value || viewModel.IsUnread.value || viewModel.IsContacts.value){
            fetchMessages(searchEditText.text.toString())
        }
        else{
            fetchUsers()
        }
        setListenerForArchieveBar()
    }

    override fun onMultiSelectEnded() {
        if(viewModel.IsMessages.value || viewModel.IsUnread.value || viewModel.IsContacts.value){
            fetchMessages(searchEditText.text.toString())
        }
        else{
            fetchUsers()
        }
        setListenerForArchieveBar()
    }

    override fun ResetFilters() {
        viewModel.setIsMessages(false)
        viewModel.showIsMessageBar(true)
        viewModel.showIsMessageBar_clicked(false)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null || e2 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        startActivity(Intent(this@MainActivity, GroupMainActivity::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                    return true
                }
            }
            return false
        }
    }
}