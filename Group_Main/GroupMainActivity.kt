package com.example.android.myproject.Group_Main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
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
import com.example.android.myproject.Archive_Group.ArchiveGroupChatActivity
import com.example.android.myproject.Chat_Main.MainActivity
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.Create_Group.GroupActivity
import com.example.android.myproject.Entities.GroupMessage
import com.example.android.myproject.Login.LoginActivity
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.R
import com.example.android.myproject.Current_User_Profile.SenderProfileActivity
import com.example.android.myproject.Listeners.CallBackListenerMessageSearch
import com.example.android.myproject.Listeners.MultipleMessageSelectListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.iterator

class GroupMainActivity : AppCompatActivity(), OnUserLongClickListener , MultipleMessageSelectListener, CallBackListenerMessageSearch{
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupUserAdapter
    private var groupList = ArrayList<Group>()
    private lateinit var groupChatSearchRecyclerView: RecyclerView
    private lateinit var searchAdapter: GroupMessageSearchAdapter
    private lateinit var messageList: ArrayList<GroupMessage>
    private lateinit var groupUnreadRecyclerView: RecyclerView
    private lateinit var groupUnreadAdapter: GroupUserAdapter
    private var groupUnreadList = ArrayList<Group>()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var searchEditText : EditText
    private lateinit var profile: LinearLayout
    private lateinit var chats: LinearLayout
    private lateinit var addGroups: LinearLayout
    private lateinit var group: LinearLayout
    private lateinit var groups : TextView
    private var IsGroups: Boolean = false
    private lateinit var unread : TextView
    private var IsUnread: Boolean = false
    private lateinit var messages : TextView
    private var IsMessages: Boolean = false
    private lateinit var scrollView : HorizontalScrollView
    private lateinit var searchBar : LinearLayout
    private var IsSearchBarActive : Boolean = false
    private lateinit var groups_clicked : TextView
    private lateinit var unread_clicked : TextView
    private lateinit var messages_clicked : TextView
    private lateinit var appText : TextView
    private lateinit var selectCount : TextView
    private lateinit var back_navigation : ImageView
    private lateinit var archive : ImageView
    private lateinit var delete : ImageView
    private lateinit var topBar : FrameLayout
    private lateinit var archieveBar : FrameLayout
    private lateinit var noDataFound : LinearLayout
    private lateinit var loading : LinearLayout
    private lateinit var searchBarBack : ImageView

    companion object{
        var selectList : ArrayList<String> = ArrayList()
        var IsLongPressed : Boolean = false
        var isDeleteDisabled : Boolean = false
        var IsSearchBarSelected : Boolean = false

    }

    private lateinit var gestureDetector: GestureDetectorCompat

    private lateinit var viewModel : GroupMainActivityViewModel

    private val groupMessageListeners = mutableMapOf<String, ValueEventListener>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_main)

        val swipeContainer = findViewById<View>(R.id.group_main)

        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        swipeContainer.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        viewModel = ViewModelProvider(this)[GroupMainActivityViewModel::class.java]

        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()

        loading = findViewById<LinearLayout>(R.id.progressBar_group_main)
        loading.visibility = View.VISIBLE

        // Groups

        groupList = ArrayList()
        groupAdapter = GroupUserAdapter(this, groupList,viewModel,supportFragmentManager)

        recyclerView = findViewById(R.id.GroupUserRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = groupAdapter

        // SearchList

        messageList = ArrayList()
        searchAdapter = GroupMessageSearchAdapter(this, messageList,viewModel)

        groupChatSearchRecyclerView = findViewById(R.id.groupChatSearchRecyclerView)
        groupChatSearchRecyclerView.layoutManager = LinearLayoutManager(this)
        groupChatSearchRecyclerView.adapter = searchAdapter

        // Unread

        groupUnreadList = ArrayList()
        groupUnreadAdapter = GroupUserAdapter(this, groupUnreadList, viewModel,supportFragmentManager)

        groupUnreadRecyclerView = findViewById(R.id.GroupUnreadRecyclerView)
        groupUnreadRecyclerView.layoutManager = LinearLayoutManager(this)
        groupUnreadRecyclerView.adapter = groupUnreadAdapter

        val menuButton: ImageView = findViewById(R.id.menuButton_group)

        menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        searchEditText = findViewById<EditText>(R.id.searchEditText_group)

        searchEditText.setOnTouchListener { v, event ->
            v.requestFocus()
            v.performClick()
            false
        }

        noDataFound = findViewById<LinearLayout>(R.id.noDataFound_group)

        fetchGroupsFromFirebase()

        if (viewModel.isGroupsSelected.value != true &&
            viewModel.isMessagesSelected.value != true &&
            viewModel.isUnreadSelected.value != true
        ) {
            viewModel.setIsGroupsSelected(false)
            viewModel.setIsUnreadSelected(false)
            viewModel.setIsMessagesSelected(false)

            viewModel.setGroups(View.VISIBLE)
            viewModel.setGroups_clicked(View.GONE)
            viewModel.setUnread(View.VISIBLE)
            viewModel.setUnread_clicked(View.GONE)
            viewModel.setMessages(View.VISIBLE)
            viewModel.setMessages_clicked(View.GONE)

            viewModel.setTabSelection(isMessages = false, isGroups = true, isUnread = false)
            fetchGroupsFromFirebase()
            Log.d("GroupMainActivity", "if_condition")
        }

        else {

            when {
                viewModel.isGroupsSelected.value == true -> {
                    Log.d("GroupMainActivity", "contacts")
                    recyclerView.visibility = View.VISIBLE
                    groupChatSearchRecyclerView.visibility = View.GONE
                    groupUnreadRecyclerView.visibility = View.GONE
                }

                viewModel.isMessagesSelected.value == true -> {
                    Log.d("GroupMainActivity", "messages")
                    recyclerView.visibility = View.GONE
                    groupChatSearchRecyclerView.visibility = View.VISIBLE
                    groupUnreadRecyclerView.visibility = View.GONE
                }

                viewModel.isUnreadSelected.value == true -> {
                    Log.d("GroupMainActivity", "chats")
                    recyclerView.visibility = View.GONE
                    groupChatSearchRecyclerView.visibility = View.GONE
                    groupUnreadRecyclerView.visibility = View.VISIBLE
                }
            }
            Log.d("GroupMainActivity", "else_condition")
        }

        profile = findViewById<LinearLayout>(R.id.profile_group)
        addGroups = findViewById<LinearLayout>(R.id.addGroups_group)
        group = findViewById<LinearLayout>(R.id.group_group)
        chats = findViewById<LinearLayout>(R.id.chats_group)
        scrollView = findViewById<HorizontalScrollView>(R.id.scrollView_group)
        searchBar = findViewById<LinearLayout>(R.id.searchbar_group)
        groups = findViewById<TextView>(R.id.groups_group)
        unread = findViewById<TextView>(R.id.unread_group)
        messages = findViewById<TextView>(R.id.messages_group)
        groups_clicked = findViewById<TextView>(R.id.groups_clicked_group)
        unread_clicked = findViewById<TextView>(R.id.unread_clicked_group)
        messages_clicked = findViewById<TextView>(R.id.messages_clicked_group)
        appText = findViewById<TextView>(R.id.appText_group)
        selectCount = findViewById<TextView>(R.id.selectCount_group)
        back_navigation = findViewById<ImageView>(R.id.back_navigation_group)
        archive = findViewById<ImageView>(R.id.archive_in_group)
        delete = findViewById<ImageView>(R.id.delete_group)
        topBar = findViewById<FrameLayout>(R.id.topBar_group)
        searchBarBack = findViewById<ImageView>(R.id.searchBar_back_group)
        searchBarBack.visibility = View.GONE

        viewModel.isAppTextVisible.observe(this) { appText.visibility = it }
        viewModel.isSelectCountVisible.observe(this) { selectCount.visibility = it }
        viewModel.isBackNavigationVisible.observe(this) { back_navigation.visibility = it }
        viewModel.isArchiveVisible.observe(this) { archive.visibility = it }
        viewModel.isDeleteVisible.observe(this) { delete.visibility = it }
        viewModel.isTopBarVisible.observe(this) { topBar.visibility = it }

        viewModel.unread.observe(this) { unread.visibility = it }
        viewModel.groups.observe(this) { groups.visibility = it }
        viewModel.messages.observe(this) { messages.visibility = it }
        viewModel.unread_clicked.observe(this) { unread_clicked.visibility = it }
        viewModel.groups_clicked.observe(this) { groups_clicked.visibility = it }
        viewModel.messages_clicked.observe(this) { messages_clicked.visibility = it }

        viewModel.recyclerView.observe(this){
            recyclerView.visibility = if(it) View.VISIBLE else View.GONE
            groupChatSearchRecyclerView.visibility  = View.GONE
            groupUnreadRecyclerView.visibility  = View.GONE
        }

        viewModel.groupChatSearchRecyclerView.observe(this){
            recyclerView.visibility  = View.GONE
            groupChatSearchRecyclerView.visibility  = if(it) View.VISIBLE else View.GONE
            groupUnreadRecyclerView.visibility  = View.GONE
        }

        viewModel.groupUnreadRecyclerView.observe(this){
            recyclerView.visibility  = View.GONE
            groupChatSearchRecyclerView.visibility  = View.GONE
            groupUnreadRecyclerView.visibility  = if(it) View.VISIBLE else View.GONE
        }

        viewModel.searchMessageList.observe(this) {
            messageList.clear()
            messageList.addAll(it)
            searchAdapter.notifyDataSetChanged()
        }

        viewModel.groupList.observe(this) { groups ->
            groupList.clear()
            groupList.addAll(groups)
            groupAdapter.notifyDataSetChanged()
        }

        viewModel.unreadGroupList.observe(this) {
            groupUnreadList.clear()
            groupUnreadList.addAll(it)
            groupUnreadAdapter.notifyDataSetChanged()
        }

        viewModel.selectedList.observe(this) {
            selectCount.text = it.size.toString()

            if (viewModel.isUnreadSelected.value == true) {
                groupUnreadAdapter.notifyDataSetChanged()
            } else {
                groupAdapter.notifyDataSetChanged()
            }
        }

        viewModel.isLongPressed.observe(this) { isActive ->
            if (isActive) {
                viewModel.setAppTextVisible(View.INVISIBLE)
                viewModel.setSelectCountVisible(View.VISIBLE)
                viewModel.setBackNavigationVisible(View.VISIBLE)
                viewModel.setArchiveVisible(View.VISIBLE)
                viewModel.setDeleteVisible(View.VISIBLE)
            } else {
                viewModel.setAppTextVisible(View.VISIBLE)
                viewModel.setSelectCountVisible(View.GONE)
                viewModel.setBackNavigationVisible(View.GONE)
                viewModel.setArchiveVisible(View.GONE)
                viewModel.setDeleteVisible(View.GONE)
            }
        }

        searchEditText.addTextChangedListener {
            if(searchEditText.text.length == 0){
                findViewById<ImageView>(R.id.search_bar_close_group).visibility = View.GONE
                fetchGroupsFromFirebase()
            }
            else{
                findViewById<ImageView>(R.id.search_bar_close_group).visibility = View.VISIBLE
                fetchMessages(searchEditText.text.toString())
            }
        }

        findViewById<ImageView>(R.id.search_bar_close_group).setOnClickListener {
            searchEditText.text.clear()
            findViewById<ImageView>(R.id.search_bar_close_group).visibility = View.GONE
        }

        searchEditText.setOnClickListener {
            viewModel.clearSelectedList()
            groupAdapter.notifyDataSetChanged()
            searchAdapter.notifyDataSetChanged()
            groupUnreadAdapter.notifyDataSetChanged()
          if(viewModel.isMessagesSelected.value == false && viewModel.isGroupsSelected.value == false && viewModel.isUnreadSelected.value == false){
//                fetchMessages(searchEditText.text.toString())
              fetchGroupsFromFirebase()
            }
            viewModel.setSearchBarActive(true)
            viewModel.setTopBarVisible(View.GONE)
            searchBarBack.visibility = View.VISIBLE
            IsSearchBarSelected=true

        }

        searchBarBack.setOnClickListener {
            viewModel.setSearchBarActive(false)
            viewModel.setTopBarVisible(View.VISIBLE)
            searchBarBack.visibility = View.GONE
            searchEditText.text.clear()
            fetchGroupsFromFirebase()
            IsSearchBarSelected =false
        }

        group.isSelected = true

        val drawableId = resolveThemeDrawable(R.attr.groups_contrast)
        findViewById<ImageView>(R.id.grouplogo).setImageResource(drawableId)

        val accentColor = resolveThemeColor(R.attr.colorAccent)
        findViewById<TextView>(R.id.grouplogo_text).setTextColor(accentColor)

        chats.isSelected = false

        profile.setOnClickListener {
            val intent = Intent(this@GroupMainActivity, SenderProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            startActivity(intent)
        }

        addGroups.setOnClickListener {
            val intent = Intent(this@GroupMainActivity, GroupActivity::class.java)
            startActivity(intent)
        }

        chats.setOnClickListener {
            val intent = Intent(this@GroupMainActivity, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        groups.setOnClickListener {

            viewModel.setIsGroupsSelected(true)
            viewModel.setIsUnreadSelected(false)
            viewModel.setIsMessagesSelected(false)

            viewModel.setUnread(View.VISIBLE)
            viewModel.setUnread_clicked(View.GONE)

            viewModel.setGroups(View.GONE)
            viewModel.setGroups_clicked(View.VISIBLE)

            viewModel.setMessages(View.VISIBLE)
            viewModel.setMessages_clicked(View.GONE)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectedList()
            groupAdapter.notifyDataSetChanged()
            resetUI()

            viewModel.setTabSelection(false, true, false)

            fetchMessages(searchEditText.text.toString())

        }

        groups_clicked.setOnClickListener {

            viewModel.setIsGroupsSelected(false)
            viewModel.setGroups(View.VISIBLE)
            viewModel.setGroups_clicked(View.GONE)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectedList()
            groupAdapter.notifyDataSetChanged()
            resetUI()

            viewModel.setTabSelection(false, false, false)

            if(MainActivity.Companion.IsSearchBarSelected){
                fetchMessages(searchEditText.text.toString())
            }
            else {
                fetchGroupsFromFirebase()
            }

        }

        unread.setOnClickListener {

            viewModel.setIsUnreadSelected(true)
            viewModel.setIsGroupsSelected(false)
            viewModel.setIsMessagesSelected(false)

            viewModel.setUnread(View.GONE)
            viewModel.setUnread_clicked(View.VISIBLE)

            viewModel.setGroups(View.VISIBLE)
            viewModel.setGroups_clicked(View.GONE)

            viewModel.setMessages(View.VISIBLE)
            viewModel.setMessages_clicked(View.GONE)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectedList()
            groupAdapter.notifyDataSetChanged()
            resetUI()

            viewModel.setTabSelection(false, false, true)
            fetchMessages(searchEditText.text.toString())
        }

        unread_clicked.setOnClickListener {
            viewModel.setIsUnreadSelected(false)
            viewModel.setUnread(View.VISIBLE)
            viewModel.setUnread_clicked(View.GONE)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectedList()
            groupAdapter.notifyDataSetChanged()
            resetUI()

            viewModel.setTabSelection(false, false, false)

            if(MainActivity.Companion.IsSearchBarSelected){
                fetchMessages(searchEditText.text.toString())
            }
            else {
                fetchGroupsFromFirebase()
            }

        }

        messages.setOnClickListener {
            viewModel.setIsMessagesSelected(true)
            viewModel.setIsUnreadSelected(false)
            viewModel.setIsGroupsSelected(false)

            viewModel.setUnread(View.VISIBLE)
            viewModel.setUnread_clicked(View.GONE)
            viewModel.setGroups(View.VISIBLE)
            viewModel.setGroups_clicked(View.GONE)
            viewModel.setMessages(View.GONE)
            viewModel.setMessages_clicked(View.VISIBLE)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectedList()
            groupAdapter.notifyDataSetChanged()
            resetUI()

            viewModel.setTabSelection(true, false, false)

            fetchMessages(searchEditText.text.toString())

        }

        messages_clicked.setOnClickListener {

            viewModel.setIsMessagesSelected(false)
            viewModel.setMessages(View.VISIBLE)
            viewModel.setMessages_clicked(View.GONE)

            viewModel.setIsLongPressed(false)
            viewModel.clearSelectedList()
            groupAdapter.notifyDataSetChanged()
            resetUI()

            viewModel.setTabSelection(false, false, false)

            if(MainActivity.Companion.IsSearchBarSelected){
                fetchMessages(searchEditText.text.toString())
            }
            else {
                fetchGroupsFromFirebase()
            }

        }

        delete.setOnClickListener {
            val currentUserId = mAuth.currentUser?.uid.toString()

            var list = viewModel.selectedList.value
            for (receiver in list) {
                val chatRef = FirebaseDatabase.getInstance().getReference("groups").child(receiver)
                    .child("groupMessages").child(currentUserId)

                chatRef.removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Message deleted successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to delete messages: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.setSelectedList(emptyList())
            viewModel.setIsLongPressed(false)
            resetUI()
            groupAdapter.notifyDataSetChanged()
            fetchGroupsFromFirebase()
        }

        archive.setOnClickListener {
            val currentUserId = mAuth.currentUser?.uid.toString()

            var list = viewModel.selectedList.value
            for (receiver in list) {
                FirebaseDatabase.getInstance().getReference("groups")
                    .child(receiver).child("groupMessages").child(currentUserId).child("archive").setValue(true)
            }

            viewModel.setSelectedList(emptyList())
            viewModel.setIsLongPressed(false)
            resetUI()
            groupAdapter.notifyDataSetChanged()
            fetchGroupsFromFirebase()
            setListenerForArchieveBar()
        }

        archieveBar = findViewById<FrameLayout>(R.id.archieveBar_group)

        archieveBar.visibility = View.GONE

        setListenerForArchieveBar()

        archieveBar.setOnClickListener {
            val intent = Intent(this@GroupMainActivity, ArchiveGroupChatActivity::class.java)
            startActivity(intent)
        }

        setupGroupMessageListeners()

        back_navigation.setOnClickListener {

            viewModel.setAppTextVisible(View.VISIBLE)
            viewModel.setSelectCount(0)
            viewModel.setBackNavigationVisible(View.GONE)
            viewModel.setArchiveVisible(View.GONE)
            viewModel.setDeleteVisible(View.GONE)

            viewModel.clearSelectedList()
            viewModel.setIsLongPressed(false)
            resetUI()

            groupAdapter.notifyDataSetChanged()

        }

    }

    fun setListenerForArchieveBar() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("groups")
        archieveBar = findViewById<FrameLayout>(R.id.archieveBar_group)

        var shouldShowArchiveBar = false
        var totalGroupsToCheck = 0
        var groupsChecked = 0

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val group = snap.child("groupDetails")
                        .children.firstOrNull()
                        ?.getValue(Group::class.java) ?: continue

                    if (group.groupMembers.contains(currentUserId)) {
                        totalGroupsToCheck++
                        FirebaseDatabase.getInstance().getReference("groups")
                            .child(group.groupuid)
                            .child("groupMessages")
                            .child(currentUserId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val isArchived = snapshot.child("archive").getValue(Boolean::class.java) == true
                                    if (isArchived) {
                                        shouldShowArchiveBar = true
                                    }

                                    groupsChecked++
                                    if (groupsChecked == totalGroupsToCheck) {
                                        archieveBar.visibility =
                                            if (shouldShowArchiveBar) View.VISIBLE else View.GONE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("ArchiveBar", "Error: ${error.message}")
                                }
                            })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ArchiveBar", "Error: ${error.message}")
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
    private fun setupGroupMessageListeners() {
        clearGroupMessageListeners()

        for (group in groupList) {
            val groupUid = group.groupuid ?: continue

            val messagesRef = FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupUid)
                .child("groupMessages")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val index = groupList.indexOfFirst { it.groupuid == groupUid }
                    if (index != -1) {
                        groupAdapter.notifyItemChanged(index)
                    } else {
                        Log.w("GroupMainActivity", "Group $groupUid not found in list")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GroupMainActivity", "Listener cancelled for $groupUid: ${error.message}")
                }
            }

            messagesRef.addValueEventListener(listener)
            groupMessageListeners[groupUid] = listener
        }
    }
    private fun clearGroupMessageListeners() {
        for ((groupUid, listener) in groupMessageListeners) {
            FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupUid)
                .child("groupMessages")
                .removeEventListener(listener)
        }
        groupMessageListeners.clear()
    }
    fun resetUI(){
        viewModel.setAppTextVisible(View.VISIBLE)
        viewModel.setSelectCount(0)
        viewModel.setBackNavigationVisible(View.GONE)
        viewModel.setArchiveVisible(View.GONE)
        viewModel.setDeleteVisible(View.GONE)
        viewModel.setIsLongPressed(false)
    }
    fun fetchMessages(text: String) {
        loading.visibility = View.VISIBLE
        noDataFound.visibility = View.GONE

        recyclerView.visibility = View.GONE
        groupChatSearchRecyclerView.visibility = View.GONE
        groupUnreadRecyclerView.visibility = View.GONE

        val userRef = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("groups")

        when {
            viewModel.isMessagesSelected.value == true -> {
                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val sortedMessages = mutableListOf<Pair<GroupMessage, Long>>()

                        for (groupSnapshot in snapshot.children) {
                            val group = groupSnapshot.child("groupDetails").children.firstOrNull()?.getValue(Group::class.java) ?: continue
                            val isCurrentUserMember = group.groupMembers.contains(userRef) || group.pastMembersList.contains(userRef)

                            if (isCurrentUserMember) {
                                val messagesSnapshot = groupSnapshot.child("groupMessages").child(userRef).child("messages")

                                for (msgSnap in messagesSnapshot.children) {
                                    val message = msgSnap.getValue(GroupMessage::class.java)
                                    val ts = msgSnap.child("timeStamp").getValue(Long::class.java) ?: 0L
                                    if (message?.message?.contains(text, ignoreCase = true) == true) {
                                        sortedMessages.add(Pair(message, ts))
                                    }
                                }
                            }
                        }

                        viewModel.setSearchMessageList(sortedMessages.sortedByDescending { it.second }.map { it.first })
                        loading.visibility = View.GONE
                        if(viewModel.searchMessageList.value == null || viewModel.searchMessageList.value?.isEmpty() == true){
                            noDataFound.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            groupChatSearchRecyclerView.visibility = View.GONE
                            groupUnreadRecyclerView.visibility = View.GONE
                        }
                        else {
                            noDataFound.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            groupChatSearchRecyclerView.visibility = View.VISIBLE
                            groupUnreadRecyclerView.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FetchMessages", "Database error: ${error.message}")
                    }
                })
            }
            viewModel.isUnreadSelected.value == true -> {
                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val unreadGroups = mutableListOf<Pair<Group, Long>>()

                        for (snap in snapshot.children) {
                            val groupUid = snap.key ?: continue
                            val group = snap.child("groupDetails").children.firstOrNull()?.getValue(Group::class.java) ?: continue

                            val isMember = group.groupMembers.contains(userRef) || group.pastMembersList.contains(userRef)
                            if (!isMember) continue

                            val messagesSnap = snap.child("groupMessages").child(userRef).child("messages")
                            val reversedMessages = messagesSnap.children.toList().reversed()

                            for (msgSnapshot in reversedMessages) {
                                val message = msgSnapshot.getValue(GroupMessage::class.java)
                                if(message?.sender == FirebaseAuth.getInstance().currentUser?.uid){
                                    break
                                }
                                val seen = message?.viewersList?.get(userRef) == true
                                if (!seen) {
                                    val timestamp = msgSnapshot.child("timeStamp").getValue(Long::class.java) ?: 0L
                                    unreadGroups.add(Pair(group.copy(groupuid = groupUid), timestamp))
                                    break
                                }
                            }
                        }

                        val sortedUnread = unreadGroups.sortedByDescending { it.second }.map { it.first }
                        viewModel.setUnreadGroupList(sortedUnread)
                        loading.visibility = View.GONE
                        if(viewModel.unreadGroupList.value == null || viewModel.unreadGroupList.value?.isEmpty() == true){
                            noDataFound.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            groupChatSearchRecyclerView.visibility = View.GONE
                            groupUnreadRecyclerView.visibility = View.GONE
                        }
                        else {
                            noDataFound.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            groupChatSearchRecyclerView.visibility = View.GONE
                            groupUnreadRecyclerView.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FetchUnreadGroups", "Error: ${error.message}")
                    }
                })
            }
            else -> {
                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val groupItems = mutableListOf<Pair<Group, Long>>()
                        var pendingChecks = 0

                        for (groupSnap in snapshot.children) {
                            val groupUid = groupSnap.key ?: continue
                            val group = groupSnap.child("groupDetails").children.firstOrNull()?.getValue(Group::class.java) ?: continue
                            containMessages(groupUid) { hasMessages ->
                                val isMember = group.groupMembers.contains(userRef) || (group.pastMembersList.contains(userRef) && hasMessages)

                                if (isMember && group.groupname.contains(text, ignoreCase = true)) {
                                    val messagesSnap = groupSnap.child("groupMessages").child(userRef).child("messages")
                                    val maxTs = messagesSnap.children.mapNotNull { it.child("timeStamp").getValue(Long::class.java) }.maxOrNull() ?: 0L
                                    groupItems.add(Pair(group.copy(groupuid = groupUid), maxTs))
                                }

                                pendingChecks--
                                if (pendingChecks == 0) {
                                    val sorted = groupItems.sortedByDescending { it.second }.map { it.first }
                                    viewModel.setGroupList(sorted)
                                    loading.visibility = View.GONE
                                }
                            }

                            pendingChecks++
                        }

                        if (pendingChecks == 0) {
                            viewModel.setGroupList(emptyList())
                            loading.visibility = View.GONE
                        }
                        if(viewModel.groupList.value == null || viewModel.groupList.value?.isEmpty() == true){
                            noDataFound.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            groupChatSearchRecyclerView.visibility = View.GONE
                            groupUnreadRecyclerView.visibility = View.GONE
                        } else {
                            noDataFound.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            groupChatSearchRecyclerView.visibility = View.GONE
                            groupUnreadRecyclerView.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@GroupMainActivity, "Error loading groups", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    setUserOnlineStatus("offline")
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this@GroupMainActivity, LoginActivity::class.java)
                    finish()
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
    private fun setUserOnlineStatus(status: String) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            FirebaseDatabase.getInstance().getReference("user").child(userId).child("activeChatUid").setValue(status)
        }
    }
    private fun fetchGroupsFromFirebase() {
        noDataFound.visibility = View.GONE
        loading.visibility = View.VISIBLE

        recyclerView.visibility = View.GONE
        groupChatSearchRecyclerView.visibility = View.GONE
        groupUnreadRecyclerView.visibility = View.GONE

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = FirebaseDatabase.getInstance().getReference("groups")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupItems = mutableListOf<Pair<Group, Long>>()
                var totalChecks = 0
                var completedChecks = 0

                for (groupSnap in snapshot.children) {
                    val groupUid = groupSnap.key ?: continue
                    val groupDetailSnap = groupSnap.child("groupDetails").children.firstOrNull() ?: continue
                    val group = groupDetailSnap.getValue(Group::class.java) ?: continue

                    containMessages(group.groupuid!!) { hasMessages ->
                        if ((group.groupMembers.contains(currentUserId) || (group.pastMembersList.contains(currentUserId) && hasMessages ))) {
                            totalChecks++
                            val tempGroup = group.copy()
                            isArchived(groupUid) { archived ->
                                if (!archived) {
                                    val messagesSnap = groupSnap.child("groupMessages").child(currentUserId).child("messages")
                                    var maxTs = 0L
                                    for (msg in messagesSnap.children) {
                                        val ts = msg.child("timeStamp").getValue(Long::class.java) ?: 0L
                                        if (ts > maxTs) maxTs = ts
                                    }
                                    tempGroup.groupuid = groupUid
                                    groupItems.add(Pair(tempGroup, maxTs))
                                }

                                completedChecks++
                                if (completedChecks == totalChecks) {
                                    groupList.clear()
                                    groupList.addAll(groupItems.sortedByDescending { it.second }.map { it.first })
                                    loading.visibility = View.GONE
                                    if(groupList.isEmpty){
                                        noDataFound.visibility=View.VISIBLE
                                    }
                                    groupAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }

                if (totalChecks == 0) {
                    groupAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GroupMainActivity, "Error loading groups", Toast.LENGTH_SHORT).show()
            }
        })
        noDataFound.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        groupChatSearchRecyclerView.visibility = View.GONE
        groupUnreadRecyclerView.visibility = View.GONE
    }
    fun containMessages(groupUid: String, onResult: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val messagesRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupUid)
            .child("groupMessages")
            .child(currentUserId)
            .child("messages")

        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onResult(snapshot.exists() && snapshot.childrenCount > 0)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(false)
            }
        })
    }
    fun isArchived(groupUid: String, onResult: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupUid)
            .child("groupMessages")
            .child(currentUserId)
            .child("archive")
            .get()
            .addOnSuccessListener {
                val flag = it.getValue(Boolean::class.java) == true
                onResult(flag)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    override fun onUserLongClicked(position: Int) {
        viewModel.addSelectedList(groupList[position].groupuid.toString())
        val selectedUsers = viewModel.selectedList.value ?: emptyList()
        selectCount.text = selectedUsers.size.toString()

        viewModel.setAppTextVisible(View.INVISIBLE)
        viewModel.setSelectCountVisible(View.VISIBLE)
        viewModel.setBackNavigationVisible(View.VISIBLE)
        viewModel.setArchiveVisible(View.VISIBLE)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val isDeletable = isDeleteAllowed(selectedUsers, groupList, currentUserId)

        viewModel.setDeleteVisible(if (isDeletable) View.VISIBLE else View.GONE)
        isDeleteDisabled = !isDeletable
    }
    fun isDeleteAllowed(selectedGroupUids: List<String>, groupList: List<Group>, currentUserId: String?): Boolean {
        selectedGroupUids.forEach { selectedGroupUid ->
            val group = groupList.find { it.groupuid == selectedGroupUid }
            if (group != null && group.groupMembers.contains(currentUserId)) {
                return false
            }
        }
        return true
    }

    override fun onUserClicked(position: Int): Boolean {
        val uid = groupList[position].groupuid.toString()
        val selectedUsers = viewModel.selectedList.value ?: emptyList()

        if (selectedUsers.contains(uid)) {
            viewModel.removeSelectedList(uid)
            val select = viewModel.selectedList.value ?: emptyList()
            selectCount.text = select.size.toString()
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isDeletable = isDeleteAllowed(select, groupList, currentUserId)

            viewModel.setDeleteVisible(if (isDeletable) View.VISIBLE else View.GONE)
            isDeleteDisabled = !isDeletable

            if (viewModel.selectedList.value.isEmpty()) {
                viewModel.setIsLongPressed(false)
                resetUI()
            }

            return true
        } else {
            return false
        }
    }

    override fun onBackPressed() {
        if (viewModel.isLongPressed.value==true) {
            viewModel.clearSelectedList()
            viewModel.setIsLongPressed(false)
            resetUI()
            groupAdapter.notifyDataSetChanged()
        }
        else if (viewModel.isSearchBarActive.value == true) {

            viewModel.setSearchBarActive(false)
            viewModel.setTopBarVisible(View.VISIBLE)
            searchEditText.text.clear()

            viewModel.setIsMessagesSelected(false)
            viewModel.setIsUnreadSelected(false)
            viewModel.setIsGroupsSelected(false)

            viewModel.setMessages(View.VISIBLE)
            viewModel.setMessages_clicked(View.GONE)

            viewModel.setUnread(View.VISIBLE)
            viewModel.setUnread_clicked(View.GONE)

            viewModel.setGroups(View.VISIBLE)
            viewModel.setGroups_clicked(View.GONE)

            recyclerView.visibility = View.VISIBLE
            groupChatSearchRecyclerView.visibility = View.GONE
            groupUnreadRecyclerView.visibility = View.GONE

            searchBarBack.visibility = View.GONE
            IsSearchBarSelected = false
            loading.visibility = View.GONE

            groupAdapter.notifyDataSetChanged()

        } else {
            super.onBackPressed()
            finish()
        }
    }

    override fun onResume(){
        super.onResume()
        if(viewModel.isGroupsSelected.value || viewModel.isMessagesSelected.value || viewModel.isUnreadSelected.value){
            fetchMessages(searchEditText.text.toString())
        }
        else{
            fetchGroupsFromFirebase()
        }
        setListenerForArchieveBar()
    }

    override fun onMultiSelectStarted() {
        if(viewModel.isGroupsSelected.value || viewModel.isMessagesSelected.value || viewModel.isUnreadSelected.value){
            fetchMessages(searchEditText.text.toString())
        }
        else{
            fetchGroupsFromFirebase()
        }
        setListenerForArchieveBar()
    }

    override fun onMultiSelectEnded() {
        if(viewModel.isGroupsSelected.value || viewModel.isMessagesSelected.value || viewModel.isUnreadSelected.value){
            fetchMessages(searchEditText.text.toString())
        }
        else{
            fetchGroupsFromFirebase()
        }
    }

    override fun ResetFilters() {
        viewModel.setIsMessagesSelected(false)
        viewModel.setMessages(View.VISIBLE)
        viewModel.setMessages_clicked(View.GONE)
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
                    if (diffX > 0) {
                        // Right swipe
                        startActivity(Intent(this@GroupMainActivity, MainActivity::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    } else {
                        // Left swipe
                        startActivity(Intent(this@GroupMainActivity, SenderProfileActivity::class.java))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                    return true
                }
            }
            return false
        }
    }

}

