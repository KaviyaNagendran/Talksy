package com.example.android.myproject.Forward_Message

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.myproject.Chat_Page.ChatActivity
import com.example.android.myproject.Chat_Page.MessageAdapter
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.Entities.GroupMessage
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.Entities.User
import com.example.android.myproject.GroupChat_Page.GroupChatActivity
import com.example.android.myproject.GroupChat_Page.GroupChatActivity.Companion.groupMembers
import com.example.android.myproject.GroupChat_Page.GroupChatAdapter
import com.example.android.myproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserAndGroupListActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var groupRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var groupList: ArrayList<Group>
    private lateinit var userAdapter: UserListAdapter
    private lateinit var groupAdapter: GroupListAdapter
    private lateinit var viewModel : UserAndGroupListActivityViewModel
    private lateinit var searchBar : EditText
    private lateinit var sendButton : Button
    private lateinit var close : ImageView
    private lateinit var message : String
    private lateinit var messageType : String
    private lateinit var messageKeyUid : String
    private lateinit var receiverUid : String
    private lateinit var groupId : String
    private lateinit var person : String
    private lateinit var loading_Contact : LinearLayout
    private lateinit var loading_Group : LinearLayout
    private lateinit var noDataFound_Contact : LinearLayout
    private lateinit var noDataFound_Group : LinearLayout
    private lateinit var back : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_forward)

        viewModel = ViewModelProvider(this)[UserAndGroupListActivityViewModel::class.java]

        userList = ArrayList()
        userAdapter = UserListAdapter(this, userList, viewModel)

        userRecyclerView = findViewById<RecyclerView>(R.id.userListRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = userAdapter

        groupList = ArrayList()
        groupAdapter = GroupListAdapter(this, groupList, viewModel)

        groupRecyclerView = findViewById<RecyclerView>(R.id.groupListRecyclerView)
        groupRecyclerView.layoutManager = LinearLayoutManager(this)
        groupRecyclerView.adapter = groupAdapter

        message = intent.getStringExtra("message").toString()
        messageType = intent.getStringExtra("messageType").toString()
        messageKeyUid = intent.getStringExtra("messageKey").toString()
        receiverUid = intent.getStringExtra("receiverUid").toString()
        groupId = intent.getStringExtra("groupId").toString()
        person = intent.getStringExtra("person").toString()

        Log.d("Test", message)
        Log.d("Test",messageType)
        Log.d("Test",messageKeyUid)
        Log.d("Test",groupId)


        searchBar = findViewById<EditText>(R.id.searchBar_forwarded)
        sendButton = findViewById<Button>(R.id.sendButton_forwarded)
        close = findViewById<ImageView>(R.id.close_forwarded)
        loading_Contact = findViewById<LinearLayout>(R.id.progressBar_contacts)
        loading_Group = findViewById<LinearLayout>(R.id.progressBar_groups)
        noDataFound_Contact = findViewById<LinearLayout>(R.id.noDataFound_contacts)
        noDataFound_Group = findViewById<LinearLayout>(R.id.noDataFound_groups)
        back = findViewById<ImageView>(R.id.back_forwarded)

        back.setOnClickListener {
            finish()
        }

        loading_Group.visibility = View.GONE
        loading_Contact.visibility = View.GONE
        noDataFound_Contact.visibility = View.GONE
        noDataFound_Group.visibility = View.GONE

        searchBar.addTextChangedListener {
            fetchUsers(searchBar.text.toString())
            fetchGroups(searchBar.text.toString())
            if(searchBar.text.toString().isEmpty()) {
                close.visibility = View.GONE
            }
            else{
                close.visibility = View.VISIBLE
            }
        }

        fetchUsers("")

        fetchGroups("")

        sendButton.setOnClickListener {
            if(viewModel.selectUserForwardList.value.isNullOrEmpty() && viewModel.selectGroupForwardList.value.isNullOrEmpty()){
                Toast.makeText(this, "Select at least one contact", Toast.LENGTH_SHORT).show()
            }
            else if (person == "chats") {
                if (MessageAdapter.IsMultipleSelect) {
                    forwardMultipleMessageToUser()
                    forwardMultipleMessageToGroups()
                } else {
                    forwardToUser()
                    forwardToGroups()
                }
            }
            else if (person == "groups"){
                if(GroupChatAdapter.IsMultipleSelect){
                    forwardMultipleMessageToGroupsFromGroup()
                    forwardMultipleMessageToUserFromGroup()
                }
                else{
                    forwardToUserFromGroup()
                    forwardToGroupsFromGroup()
                }
            }
            finish()
        }

        close.setOnClickListener {
            fetchUsers("")
            fetchGroups("")
            searchBar.text.clear()
        }

    }

    fun forwardMultipleMessageToGroupsFromGroup(){
        for(forwardMessageKey in GroupChatActivity.SelectMultipleMessages){
            var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
            for(group in viewModel.selectGroupForwardList.value!!) {

                val baseRef = FirebaseDatabase.getInstance().getReference().child("groups")
                    .child(group.groupuid)
                    .child("groupMessages")
                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .child("messages")
                    .push()

                val messageKey = baseRef.key

                FirebaseDatabase.getInstance().getReference().child("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .child("messages")
                    .child(forwardMessageKey)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val messages = snapshot.getValue(GroupMessage::class.java)
                            var messageObject = GroupMessage()
                            if(messages!=null && messages.sender == FirebaseAuth.getInstance().currentUser?.uid.toString()) {
                                Log.d("Test-2",messages.sender.toString())
                                getViewersList(group.groupuid) { viewersMap ->
                                    messageObject = GroupMessage(
                                        message = messages.message,
                                        sender = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                        messageType = messages.messageType,
                                        status = "sent",
                                        messageKey = messageKey,
                                        edited = false,
                                        groupUid = group.groupuid,
                                        viewersList = viewersMap,
                                        forwarded = false,
                                        isReplyed = false,
                                        replyToMessage = ""
                                    )
                                    for (memberId in group.groupMembers) {
                                        val messageRef = FirebaseDatabase.getInstance().getReference("groups")
                                            .child(group.groupuid)
                                            .child("groupMessages")
                                            .child(memberId)
                                            .child("messages")
                                            .child(messageKey.toString())

                                        messageRef.setValue(messageObject)
                                    }
                                }
                            }
                            else if(messages!=null){
                                Log.d("Test-2",messages?.sender.toString())
                                getViewersList(group.groupuid) { viewersMap ->
                                    messageObject = GroupMessage(
                                        message = messages.message,
                                        sender = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                        messageType = messages.messageType,
                                        status = "sent",
                                        messageKey = messageKey,
                                        edited = false,
                                        groupUid = group.groupuid,
                                        viewersList = viewersMap,
                                        forwarded = true,
                                        isReplyed = false,
                                        replyToMessage = ""
                                    )
                                    for (memberId in group.groupMembers) {
                                        val messageRef = FirebaseDatabase.getInstance().getReference("groups")
                                            .child(group.groupuid)
                                            .child("groupMessages")
                                            .child(memberId)
                                            .child("messages")
                                            .child(messageKey.toString())

                                        messageRef.setValue(messageObject)
                                    }
                                }
                            }
                            else{
                                Log.d("Check for Null","$messages")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("Cancelled","Cancelled")
                        }

                    })
            }
        }
    }
    fun forwardMultipleMessageToUserFromGroup(){
        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        for(forwardMessageKey in GroupChatActivity.SelectMultipleMessages){
            for(receiver in viewModel.selectUserForwardList.value!!) {
                var senderRoom = currentUser + receiver.uid
                var receiverRoom = receiver.uid + currentUser

                val messageRef =
                    FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom)
                        .child("messages").push()
                val messageKey = messageRef.key!!

                FirebaseDatabase.getInstance().getReference().child("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .child("messages")
                    .child(forwardMessageKey)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val messages = snapshot.getValue(GroupMessage::class.java)
                            var messageObject = Message()
                            if(messages?.sender == currentUser){
                                Log.d("ForwardMessage-true","$currentUser - ${messages.sender}")
                                messageObject = Message(
                                    message = messages.message,
                                    sender = currentUser,
                                    receiver = receiver.uid,
                                    messageType = messages.messageType,
                                    status = "sent",
                                    messageKey = messageKey,
                                    edited = false,
                                    forwarded = false,
                                    isReplyed = false,
                                    replyToMessage = ""
                                )
                            }
                            else{
                                Log.d("ForwardMessage-false","$currentUser - ${messages?.sender}")
                                messageObject = Message(
                                    message = messages?.message,
                                    sender = currentUser,
                                    receiver = receiver.uid,
                                    messageType = messages?.messageType.toString(),
                                    status = "sent",
                                    messageKey = messageKey,
                                    edited = false,
                                    forwarded = true,
                                    isReplyed = false,
                                    replyToMessage = ""
                                )
                            }
                            messageRef.setValue(messageObject).addOnSuccessListener {
                                FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom)
                                    .child("messages").child(messageKey)
                                    .setValue(messageObject)
                            }

                            setArchieve(senderRoom,receiverRoom,messageKey)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("Cancelled","Cancelled")
                        }

                    })
            }
        }
    }

    fun forwardToUserFromGroup(){
        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        for(receiver in viewModel.selectUserForwardList.value!!) {
            var senderRoom = currentUser + receiver.uid
            var receiverRoom = receiver.uid + currentUser

            val messageRef =
                FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom)
                    .child("messages").push()
            val messageKey = messageRef.key!!

            FirebaseDatabase.getInstance().getReference().child("groups")
                .child(groupId)
                .child("groupMessages")
                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .child("messages")
                .child(messageKeyUid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messages = snapshot.getValue(GroupMessage::class.java)
                        var messageObject = Message()
                        if(messages?.sender == currentUser){
                            Log.d("ForwardMessage-true","$currentUser - ${messages.sender}")
                            messageObject = Message(
                                message = message,
                                sender = currentUser,
                                receiver = receiver.uid,
                                messageType = messageType,
                                status = "sent",
                                messageKey = messageKey,
                                edited = false,
                                forwarded = false,
                                isReplyed = false,
                                replyToMessage = ""
                            )
                        }
                        else{
                            Log.d("ForwardMessage-false","$currentUser - ${messages?.sender}")
                            messageObject = Message(
                                message = message,
                                sender = currentUser,
                                receiver = receiver.uid,
                                messageType = messageType,
                                status = "sent",
                                messageKey = messageKey,
                                edited = false,
                                forwarded = true,
                                isReplyed = false,
                                replyToMessage = ""
                            )
                        }
                        messageRef.setValue(messageObject).addOnSuccessListener {
                            FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom)
                                .child("messages").child(messageKey)
                                .setValue(messageObject)
                        }

                        setArchieve(senderRoom,receiverRoom,messageKey)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Cancelled","Cancelled")
                    }

                })
        }
    }

    fun forwardToGroupsFromGroup(){
        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        for(group in viewModel.selectGroupForwardList.value!!) {

            val baseRef = FirebaseDatabase.getInstance().getReference().child("groups")
                .child(group.groupuid)
                .child("groupMessages")
                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .child("messages")
                .push()

            val messageKey = baseRef.key

            FirebaseDatabase.getInstance().getReference().child("groups")
                .child(groupId)
                .child("groupMessages")
                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .child("messages")
                .child(messageKeyUid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messages = snapshot.getValue(GroupMessage::class.java)
                        var messageObject = GroupMessage()
                        if(messages!=null && messages.sender == FirebaseAuth.getInstance().currentUser?.uid.toString()) {
                            Log.d("Test-2",messages.sender.toString())
                            getViewersList(group.groupuid) { viewersMap ->
                                messageObject = GroupMessage(
                                    message = messages.message,
                                    sender = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                    messageType = messages.messageType,
                                    status = "sent",
                                    messageKey = messageKey,
                                    edited = false,
                                    groupUid = group.groupuid,
                                    viewersList = viewersMap,
                                    forwarded = false,
                                    isReplyed = false,
                                    replyToMessage = ""
                                )
                                for (memberId in group.groupMembers) {
                                    val messageRef = FirebaseDatabase.getInstance().getReference("groups")
                                        .child(group.groupuid)
                                        .child("groupMessages")
                                        .child(memberId)
                                        .child("messages")
                                        .child(messageKey.toString())

                                    messageRef.setValue(messageObject)
                                }
                            }
                        }
                        else if(messages!=null){
                            Log.d("Test-2",messages?.sender.toString())
                            getViewersList(group.groupuid) { viewersMap ->
                                messageObject = GroupMessage(
                                    message = messages.message,
                                    sender = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                    messageType = messages.messageType,
                                    status = "sent",
                                    messageKey = messageKey,
                                    edited = false,
                                    groupUid = group.groupuid,
                                    viewersList = viewersMap,
                                    forwarded = true,
                                    isReplyed = false,
                                    replyToMessage = ""
                                )
                                for (memberId in group.groupMembers) {
                                    val messageRef = FirebaseDatabase.getInstance().getReference("groups")
                                        .child(group.groupuid)
                                        .child("groupMessages")
                                        .child(memberId)
                                        .child("messages")
                                        .child(messageKey.toString())

                                    messageRef.setValue(messageObject)
                                }
                            }
                        }
                        else{
                            Log.d("Check for Null","$messages")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Cancelled","Cancelled")
                    }

                })
        }
    }

    fun forwardMultipleMessageToUser(){
        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        Log.d("MessageKeysize",ChatActivity.SelectMultipleMessages.toString())
        for(forwardMessageKey in ChatActivity.SelectMultipleMessages){
            for(receiver in viewModel.selectUserForwardList.value!!) {
                var senderRoom = currentUser + receiver.uid
                var receiverRoom = receiver.uid + currentUser

                val messageRef =
                    FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom)
                        .child("messages").push()
                val messageKey = messageRef.key!!

                FirebaseDatabase.getInstance().getReference().child("chats").child(currentUser+receiverUid)
                    .child("messages").child(forwardMessageKey)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val messages = snapshot.getValue(Message::class.java)
                            var messageObject = Message()
                            if(messages!= null && messages.sender == currentUser){
                                Log.d("ForwardMessage-true","$currentUser - ${messages.sender}")
                                messageObject = Message(
                                    message = messages.message,
                                    sender = currentUser,
                                    receiver = receiver.uid,
                                    messageType = messages.messageType,
                                    status = "sent",
                                    messageKey = messageKey,
                                    edited = false,
                                    forwarded = false,
                                    isReplyed = false,
                                    replyToMessage = ""
                                )
                            }
                            else{
                                Log.d("ForwardMessage-false","$currentUser - ${messages?.sender}")
                                messageObject = Message(
                                    message = messages?.message,
                                    sender = currentUser,
                                    receiver = receiver.uid,
                                    messageType = messages?.messageType.toString(),
                                    status = "sent",
                                    messageKey = messageKey,
                                    edited = false,
                                    forwarded = true,
                                    isReplyed = false,
                                    replyToMessage = ""
                                )
                            }
                            messageRef.setValue(messageObject).addOnSuccessListener {
                                FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom)
                                    .child("messages").child(messageKey)
                                    .setValue(messageObject)
                            }
                            setArchieve(senderRoom,receiverRoom,messageKey)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("Cancelled","Cancelled")
                        }

                    })
            }
        }
    }
    fun forwardMultipleMessageToGroups(){
        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        for(forwardMessageKey in ChatActivity.SelectMultipleMessages){

            for(group in viewModel.selectGroupForwardList.value!!) {

                val baseRef = FirebaseDatabase.getInstance().getReference().child("groups")
                    .child(group.groupuid)
                    .child("groupMessages")
                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .child("messages")
                    .push()

                val messageKey = baseRef.key

                FirebaseDatabase.getInstance().getReference().child("chats").child(currentUser+receiverUid)
                    .child("messages").child(forwardMessageKey)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val messages = snapshot.getValue(Message::class.java)
                            var messageObject = GroupMessage()
                            if(messages!=null && messages.sender == FirebaseAuth.getInstance().currentUser?.uid.toString()) {
                                Log.d("Test-2",messages.sender.toString())
                                getViewersList(group.groupuid) { viewersMap ->
                                    messageObject = GroupMessage(
                                        message = messages.message,
                                        sender = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                        messageType = messages.messageType,
                                        status = "sent",
                                        messageKey = messageKey,
                                        edited = false,
                                        groupUid = group.groupuid,
                                        viewersList = viewersMap,
                                        forwarded = false,
                                        isReplyed = false,
                                        replyToMessage = ""
                                    )
                                    for (memberId in group.groupMembers) {
                                        val messageRef = FirebaseDatabase.getInstance().getReference("groups")
                                            .child(group.groupuid)
                                            .child("groupMessages")
                                            .child(memberId)
                                            .child("messages")
                                            .child(messageKey.toString())

                                        messageRef.setValue(messageObject)
                                    }
                                }
                            }
                            else if(messages!=null){
                                Log.d("Test-2",messages?.sender.toString())
                                getViewersList(group.groupuid) { viewersMap ->
                                    messageObject = GroupMessage(
                                        message = messages.message,
                                        sender = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                        messageType = messages.messageType,
                                        status = "sent",
                                        messageKey = messageKey,
                                        edited = false,
                                        groupUid = group.groupuid,
                                        viewersList = viewersMap,
                                        forwarded = true,
                                        isReplyed = false,
                                        replyToMessage = ""
                                    )
                                    for (memberId in group.groupMembers) {
                                        val messageRef = FirebaseDatabase.getInstance().getReference("groups")
                                            .child(group.groupuid)
                                            .child("groupMessages")
                                            .child(memberId)
                                            .child("messages")
                                            .child(messageKey.toString())

                                        messageRef.setValue(messageObject)
                                    }
                                }
                            }
                            else{
                                Log.d("Check for Null","$messages")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("Cancelled","Cancelled")
                        }

                    })
            }
        }
    }
    fun forwardToUser(){
        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        for(receiver in viewModel.selectUserForwardList.value!!) {
            var senderRoom = currentUser + receiver.uid
            var receiverRoom = receiver.uid + currentUser

            val messageRef =
                FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom)
                    .child("messages").push()
            val messageKey = messageRef.key!!

            FirebaseDatabase.getInstance().getReference().child("chats").child(currentUser+receiverUid)
                .child("messages").child(messageKeyUid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messages = snapshot.getValue(Message::class.java)
                        var messageObject = Message()
                        if(messages?.sender == currentUser){
                            Log.d("ForwardMessage-true","$currentUser - ${messages.sender}")
                            messageObject = Message(
                                message = message,
                                sender = currentUser,
                                receiver = receiver.uid,
                                messageType = messageType,
                                status = "sent",
                                messageKey = messageKey,
                                edited = false,
                                forwarded = false,
                                isReplyed = false,
                                replyToMessage = ""
                            )
                        }
                        else{
                            Log.d("ForwardMessage-false","$currentUser - ${messages?.sender}")
                            messageObject = Message(
                                message = message,
                                sender = currentUser,
                                receiver = receiver.uid,
                                messageType = messageType,
                                status = "sent",
                                messageKey = messageKey,
                                edited = false,
                                forwarded = true,
                                isReplyed = false,
                                replyToMessage = ""
                            )
                        }
                        messageRef.setValue(messageObject).addOnSuccessListener {
                            FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom)
                                .child("messages").child(messageKey)
                                .setValue(messageObject)
                        }

                        setArchieve(senderRoom,receiverRoom,messageKey)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Cancelled","Cancelled")
                    }

                })
        }
    }
    fun forwardToGroups(){
        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        for(group in viewModel.selectGroupForwardList.value!!) {

            val baseRef = FirebaseDatabase.getInstance().getReference().child("groups")
                .child(group.groupuid)
                .child("groupMessages")
                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .child("messages")
                .push()

            val messageKey = baseRef.key

            FirebaseDatabase.getInstance().getReference().child("chats").child(currentUser+receiverUid)
                .child("messages").child(messageKeyUid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messages = snapshot.getValue(Message::class.java)
                        var messageObject = GroupMessage()
                        if(messages!=null && messages.sender == FirebaseAuth.getInstance().currentUser?.uid.toString()) {
                            Log.d("Test-2",messages.sender.toString())
                            getViewersList(group.groupuid) { viewersMap ->
                                messageObject = GroupMessage(
                                    message = messages.message,
                                    sender = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                    messageType = messages.messageType,
                                    status = "sent",
                                    messageKey = messageKey,
                                    edited = false,
                                    groupUid = group.groupuid,
                                    viewersList = viewersMap,
                                    forwarded = false,
                                    isReplyed = false,
                                    replyToMessage = ""
                                )
                                for (memberId in group.groupMembers) {
                                    val messageRef = FirebaseDatabase.getInstance().getReference("groups")
                                        .child(group.groupuid)
                                        .child("groupMessages")
                                        .child(memberId)
                                        .child("messages")
                                        .child(messageKey.toString())

                                    messageRef.setValue(messageObject)
                                }
                            }
                        }
                        else if(messages!=null){
                            Log.d("Test-2",messages?.sender.toString())
                            getViewersList(group.groupuid) { viewersMap ->
                                messageObject = GroupMessage(
                                    message = messages.message,
                                    sender = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                    messageType = messages.messageType,
                                    status = "sent",
                                    messageKey = messageKey,
                                    edited = false,
                                    groupUid = group.groupuid,
                                    viewersList = viewersMap,
                                    forwarded = true,
                                    isReplyed = false,
                                    replyToMessage = ""
                                )
                                for (memberId in group.groupMembers) {
                                    val messageRef = FirebaseDatabase.getInstance().getReference("groups")
                                        .child(group.groupuid)
                                        .child("groupMessages")
                                        .child(memberId)
                                        .child("messages")
                                        .child(messageKey.toString())

                                    messageRef.setValue(messageObject)
                                }
                            }
                        }
                        else{
                            Log.d("Check for Null","$messages")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Cancelled","Cancelled")
                    }

                })
            }

    }

    fun getViewersList(groupId : String,callback: (HashMap<String, Boolean>) -> Unit) {
        val viewersList = hashMapOf<String, Boolean>()
        val ref = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("groupDetails")
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (groupMemberSnap in snapshot.children) {
                    val group = groupMemberSnap.getValue(Group::class.java)
                    var localList = listOf<String>()
                    if (group != null) {
                        localList = group.groupMembers
                        for(i in localList){
                            if(i==currentUser){
                                viewersList.put(i,true)
                            }
                            else {
                                viewersList.put(i, false)
                            }
                        }
                    }
                }
                callback(viewersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        })
    }

    fun setArchieve(senderRoom : String , receiverRoom : String,messageKey : String){
        val chatSendRef = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom!!)

        chatSendRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChild("archive")) {
                    FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom!!).child("archive").setValue(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CheckArchive", "Error: ${error.message}")
            }
        })

        val chatReceiveRef = FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom!!)

        chatReceiveRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChild("archive")) {
                    FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom!!).child("archive").setValue(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CheckArchive", "Error: ${error.message}")
            }
        })

    }


    fun fetchUsers(text : String){
        loading_Contact.visibility = View.VISIBLE
        noDataFound_Contact.visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("user").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                viewModel.clearSelectUserList()

                for (postSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java)
                    user?.uid = postSnapshot.key

                    if (user != null && user.uid != currentUserId && user.name?.contains(text,ignoreCase = true)==true) {
                        viewModel.addSelectUser(user)
                    }
                }
                userList.clear()
                userList.addAll(viewModel.selectUserList.value)
                userAdapter.notifyDataSetChanged()
                if(userList.isEmpty()){
                    noDataFound_Contact.visibility = View.VISIBLE
                }
                else{
                    noDataFound_Contact.visibility = View.GONE
                }
                loading_Contact.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@UserAndGroupListActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun fetchGroups(text: String) {
        loading_Group.visibility = View.VISIBLE
        noDataFound_Group.visibility = View.GONE
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance().getReference("groups")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewModel.clearSelectGroupList()

                    for (groupSnap in snapshot.children) {
                        val groupDetailSnap = groupSnap.child("groupDetails").children.firstOrNull() ?: continue
                        val group = groupDetailSnap.getValue(Group::class.java) ?: continue
                        var isMember = group.groupMembers.contains(currentUser)
                        if (group.groupname.contains(text, ignoreCase = true) == true && isMember) {
                            group.groupuid = groupSnap.key.toString()
                            viewModel.addSelectGroup(group)
                        }
                    }
                    groupList.clear()
                    groupList.addAll(viewModel.selectGroupList.value)
                    groupAdapter.notifyDataSetChanged()
                    if(groupList.isEmpty()){
                        noDataFound_Group.visibility = View.VISIBLE
                    }
                    else{
                        noDataFound_Group.visibility = View.GONE
                    }
                    loading_Group.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@UserAndGroupListActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}