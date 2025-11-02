package com.example.android.myproject.GroupChat_Page

import android.Manifest
import android.animation.ObjectAnimator
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.InputFilter
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Chat_Page.ChatActivity
import com.example.android.myproject.Chat_Page.MessageAdapter
import com.example.android.myproject.DeleteAction.CameraActionsFragment
import com.example.android.myproject.DeleteAction.DeletePopUpFragment
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.Entities.GroupMessage
import com.example.android.myproject.Forward_Message.UserAndGroupListActivity
import com.example.android.myproject.Listeners.MediaPickerFragment
import com.example.android.myproject.Listeners.MediaPickerListener
import com.example.android.myproject.Listeners.MultipleMessageSelectListener
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.Listeners.ReplyHighlightListener
import com.example.android.myproject.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.json.JSONObject
import java.io.File
import java.io.IOException

class GroupChatActivity : AppCompatActivity(), MultipleMessageSelectListener , OnUserLongClickListener, ReplyHighlightListener {
    private lateinit var groupChatRecyclerView: RecyclerView
    private lateinit var messageBox : EditText
    private lateinit var sendButton: ImageButton
    private lateinit var group_Name: TextView
    private lateinit var group_ProfileImage : ShapeableImageView
    private lateinit var groupChatAdapter: GroupChatAdapter
    private lateinit var messageList: ArrayList<GroupMessage>
    private lateinit var mDbRef : DatabaseReference
    private lateinit var groupId : String
    private lateinit var groupName : String
    private lateinit var groupProfileUrl : String
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var sendColumns: View
    private lateinit var noLongerColumns : RelativeLayout
    private lateinit var topBar : FrameLayout
    private lateinit var nameBar : RelativeLayout
    private lateinit var searchBarLayout : LinearLayout
    private lateinit var searchBar : EditText
    private lateinit var close : ImageView
    private lateinit var reply_Layout : FrameLayout
    private lateinit var reply_layout_close : ImageView
    private lateinit var spaceAdjustment : View
    private lateinit var reply_image : ImageView
    private lateinit var reply_video : ImageView
    private lateinit var containedMessage : TextView
    private lateinit var messagerName : TextView
    private lateinit var backNavigation : ImageView
    private var senderUid: String? = null
    private var localActiveReceiver : String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""
    private var recordingStartTime : Long = System.currentTimeMillis()
    private lateinit var cameraLauncher : ActivityResultLauncher<Uri>
    private lateinit var videoLauncher : ActivityResultLauncher<Intent>
    private var secondsElapsed = 0
    private var timer: CountDownTimer? = null
    private lateinit var voiceRecordMessageLayout: FrameLayout
    private lateinit var timerTextView: TextView
    private lateinit var regularMessageLayout : FrameLayout
    private lateinit var voiceRecordButton: ImageButton
    private lateinit var editText_layout : FrameLayout
    private lateinit var editText_messagerName : TextView
    private lateinit var editText_containedMessage : TextView
    private val PICK_AUDIO_REQUEST = 101
    private val PICK_DOCUMENT_REQUEST = 104
    private lateinit var deleteButton : ImageView
    private lateinit var forwardButton : ImageView
    private lateinit var menuButton : ImageView
    private lateinit var selectCount : TextView
    private lateinit var textMessageLayout : FrameLayout
    private lateinit var searchBarBack : ImageView
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>
    private var initialX = 0f
    private val SWIPE_THRESHOLD = 150

    companion object{
        var editingkey : String = ""
        var groupUid : String = ""
        var oldMessage : String? = ""
        lateinit var groupMembers : ArrayList<String>
        var isReply : Boolean = false
        var replyMessage : String = ""
        var replyMessageKey : String = ""
        var replySender : String = ""

        var SelectMultipleMessages = ArrayList<String>()
        var groupIdGlobal = ""
        var IsSearchBar = false
        var photoUri : Uri? = null
        var videoUri : Uri? = null
        var IsNoLongerMember = false
        var recordingCancelled = false
    }

    fun editMessage(message: String?) {
        messageBox.setText(message)
        oldMessage = message
        messageBox.requestFocus()
    }

    private lateinit var viewModel : GroupChatActivityViewModel

    private lateinit var mediaPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher : ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        groupChatRecyclerView = findViewById(R.id.groupChatRecyclerView)
        messageBox = findViewById(R.id.message_group)
        sendButton = findViewById(R.id.send_button_group)
        searchBarLayout = findViewById(R.id.searchbar_chat_group)
        searchBar = findViewById(R.id.searchEditText_chat_group)
        close = findViewById(R.id.close_group)
        backNavigation = findViewById(R.id.BackNavigation_group)

        reply_Layout = findViewById(R.id.reply_layout_group)
        reply_layout_close = findViewById(R.id.reply_layout_close_group)
        spaceAdjustment = findViewById(R.id.spaceAdjustmentView_group)
        reply_image = findViewById(R.id.reply_image_group)
        reply_video = findViewById(R.id.reply_video_group)
        containedMessage = findViewById(R.id.contained_message_group)
        messagerName = findViewById(R.id.messager_name_group)
        searchBarBack = findViewById(R.id.searchBar_back_groupChat)

        textMessageLayout = findViewById<FrameLayout>(R.id.textMessageLayout_group)

        regularMessageLayout = findViewById(R.id.regular_message_group)
        voiceRecordMessageLayout = findViewById(R.id.voiceRecord_message_group)
        voiceRecordButton = findViewById(R.id.voiceRecord_button_group)
        timerTextView = findViewById(R.id.timerTextView_group)

        editText_layout = findViewById(R.id.editText_layout_group)
        editText_messagerName = findViewById(R.id.edit_messager_name_group)
        editText_containedMessage = findViewById(R.id.edit_contained_message_group)

        deleteButton = findViewById(R.id.DeleteButton_group)
        forwardButton = findViewById(R.id.ForwardButton_group)
        menuButton = findViewById(R.id.chatMenuButton_group)
        selectCount = findViewById(R.id.selectCount_group)

        textMessageLayout.visibility = View.VISIBLE
        voiceRecordMessageLayout.visibility = View.GONE

        voiceRecordButton.visibility = View.VISIBLE
        sendButton.visibility = View.GONE
        voiceRecordMessageLayout.visibility = View.GONE
        regularMessageLayout.visibility = View.VISIBLE

        reply_Layout.visibility = View.GONE
        reply_layout_close.visibility = View.GONE
        spaceAdjustment.visibility = View.GONE

        reply_image.visibility = View.GONE
        reply_video.visibility = View.GONE
        containedMessage.visibility = View.GONE
        messagerName.visibility = View.GONE

        reply_layout_close.setOnClickListener {
            isReply = false
            replyMessage = ""
            replyMessageKey = ""
            replySender = ""

            reply_layout_close.visibility = View.GONE
            reply_Layout.visibility = View.GONE
            reply_layout_close.visibility = View.GONE
            spaceAdjustment.visibility = View.GONE

            reply_image.visibility = View.GONE
            reply_video.visibility = View.GONE
            containedMessage.visibility = View.GONE
            messagerName.visibility = View.GONE

            editingkey = ""
            oldMessage = ""
            editText_layout.visibility = View.GONE
            messageBox.setText("")
            messageBox.requestFocus()
        }

        voiceRecordButton.visibility = View.VISIBLE
        sendButton.visibility = View.GONE
        voiceRecordMessageLayout.visibility = View.GONE
        regularMessageLayout.visibility = View.VISIBLE

        viewModel = ViewModelProvider(this).get(GroupChatActivityViewModel::class.java)

        viewModel.searchBarLayout.observe(this) { value ->
            searchBarLayout.visibility = value
        }

        viewModel.topBar.observe(this) { value ->
            topBar.visibility = value
        }

        messageList = ArrayList()
        groupChatAdapter = GroupChatAdapter(this, messageList, supportFragmentManager)

        groupChatRecyclerView.layoutManager = LinearLayoutManager(this)
        groupChatRecyclerView.adapter = groupChatAdapter

        groupId = intent.getStringExtra("groupuid").toString()
        groupUid = groupId
        groupIdGlobal = groupId
        groupName = intent.getStringExtra("groupname").toString()
        groupProfileUrl = intent.getStringExtra("groupProfileUrl").toString()
        groupMembers = intent.getStringArrayListExtra("groupMembers") as ArrayList<String>

        if (!hasPermissions()) {
            requestPermissions()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                123
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                123
            )
        }

        setUserOnlineStatus(groupId)

        group_Name = findViewById<TextView>(R.id.groupName_group)
        group_Name.text = groupName

        group_ProfileImage = findViewById<ShapeableImageView>(R.id.receiverProfile_group)
        if(groupProfileUrl != "") {
            Glide.with(this).load(groupProfileUrl).into(group_ProfileImage)
        }
        else{
            group_ProfileImage.setImageResource(R.drawable.default_profile_image)
        }

        mDbRef = FirebaseDatabase.getInstance().getReference()
        senderUid = FirebaseAuth.getInstance().currentUser?.uid

        rootLayout = findViewById(R.id.rootLayout_group)
        sendColumns = findViewById(R.id.sendcolumns_group)

        topBar = findViewById<FrameLayout>(R.id.topBar_group)
        noLongerColumns = findViewById<RelativeLayout>(R.id.no_Longer_column)

        nameBar = findViewById<RelativeLayout>(R.id.nameBar_group)

        nameBar.setOnClickListener {
            val intent = Intent(this@GroupChatActivity, GroupProfileActivity::class.java)
            intent.putExtra("groupID",groupUid)
            intent.putExtra("NoLongerUser",IsNoLongerMember)
            startActivity(intent)
            finish()
        }

        group_ProfileImage.setOnClickListener {
            val intent = Intent(this@GroupChatActivity, GroupProfileActivity::class.java)
            intent.putExtra("groupID",groupUid)
            intent.putExtra("NoLongerUser",IsNoLongerMember)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            sendColumns.translationY = -imeHeight.toFloat()

            groupChatRecyclerView.setPadding(
                groupChatRecyclerView.paddingLeft,
                groupChatRecyclerView.paddingTop,
                groupChatRecyclerView.paddingRight,
                imeHeight
            )

            groupChatRecyclerView.post {
                groupChatRecyclerView.scrollToPosition(messageList.size - 1)
            }

            insets
        }

        fetchMessages()

        messageBox.filters = arrayOf(InputFilter.LengthFilter(1000))

        messageBox.setOnClickListener {

            voiceRecordButton.visibility = View.GONE
            sendButton.visibility = View.VISIBLE
            voiceRecordMessageLayout.visibility = View.GONE
            regularMessageLayout.visibility = View.VISIBLE

        }

        messageBox.addTextChangedListener {
            if(messageBox.text.toString().length==0){
                sendButton.visibility = View.GONE
                voiceRecordButton.visibility = View.VISIBLE
            }
            else{
                sendButton.visibility = View.VISIBLE
                voiceRecordButton.visibility = View.GONE
            }
            val messageText = messageBox.text.toString().trim()
            if(messageText.length==1000){
                Toast.makeText(this, "Message length cannot exceed 1000 characters", Toast.LENGTH_SHORT).show()
            }
        }

        sendButton.setOnClickListener {
            val messageText = messageBox.text.toString().trim()
            if (messageText.isEmpty()) return@setOnClickListener

            if(messageText.length>1000){
                Toast.makeText(this, "Message length cannot exceed 250 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editingkey.isNotEmpty() && oldMessage != messageText) {
                val updatedData = mapOf<String, Any?>(
                    "message" to messageText,
                    "edited" to true
                )

                for (memberId in groupMembers) {
                    val messageRef = mDbRef.child("groups")
                        .child(groupId)
                        .child("groupMessages")
                        .child(memberId)
                        .child("messages")
                        .child(editingkey)

                    messageRef.updateChildren(updatedData)
                }

                editText_layout.visibility = View.GONE
                reply_layout_close.visibility = View.GONE

                editingkey = ""
                messageBox.setText("")
                setArchieve()
                Log.d("GroupChatActivity", "Message updated successfully.")
            }
            else if (oldMessage == messageText) {
                editingkey = ""
                messageBox.setText("")
                Log.d(
                    "ChatActivity",
                    "Message not updated Because Old Message and New Message are same"
                )
            }
            else {
                val baseRef = mDbRef.child("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(groupMembers.first())
                    .child("messages")
                    .push()

                val messageKey = baseRef.key ?: return@setOnClickListener

                getViewersList { viewersMap ->
                    val messageObject = GroupMessage(
                        message = messageText,
                        sender = senderUid,
                        messageType = "text",
                        status = "sent",
                        messageKey = messageKey,
                        edited = false,
                        groupUid = groupId,
                        viewersList = viewersMap,
                        forwarded = false,
                        isReplyed = isReply,
                        replyToMessage = replyMessageKey,
                        replyMessage = replyMessage,
                        replySender = replySender,
                        fileName = "",
                        fileSize = "",
                        fileType = "",
                        pageCount = ""
                    )

                    if(isReply){
                        isReply = false
                        replyMessageKey = ""
                        replyMessage = ""
                        reply_Layout.visibility = View.GONE
                        reply_layout_close.visibility = View.GONE
                        spaceAdjustment.visibility = View.GONE

                        reply_image.visibility = View.GONE
                        reply_video.visibility = View.GONE
                        containedMessage.visibility = View.GONE
                        messagerName.visibility = View.GONE
                    }


                    for (memberId in groupMembers) {
                        val messageRef = mDbRef.child("groups")
                            .child(groupId)
                            .child("groupMessages")
                            .child(memberId)
                            .child("messages")
                            .child(messageKey)

                        messageRef.setValue(messageObject)
                    }

                    groupChatAdapter.notifyDataSetChanged()
                }
            }
            updateSentToSeenCurrentUserWork()

            voiceRecordButton.visibility = View.VISIBLE
            sendButton.visibility = View.GONE
            voiceRecordMessageLayout.visibility = View.GONE
            regularMessageLayout.visibility = View.VISIBLE
            messageBox.setText("")
            messageBox.requestFocus()
            setArchieve()
        }

        voiceRecordButton.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    if (hasPermissions()) {
                        recordingStartTime = System.currentTimeMillis()
                        startRecording()
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val currentX = event.rawX
                    val deltaX = currentX - initialX

                    if (deltaX < -SWIPE_THRESHOLD) {
                        cancelRecording()
                        voiceRecordButton.visibility = View.VISIBLE
                        sendButton.visibility = View.GONE
                        voiceRecordMessageLayout.visibility = View.GONE
                        regularMessageLayout.visibility = View.VISIBLE
                        textMessageLayout.visibility = View.VISIBLE

                        return@setOnTouchListener true
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val recordingEndTime = System.currentTimeMillis()
                    val duration = recordingEndTime - recordingStartTime
                    stopRecording()

                    if (!recordingCancelled){
                        if (duration > 1000) {
                            val file = File(outputFile)
                            val uri = file.toUri()

                            val messageKey = FirebaseDatabase.getInstance()
                                .getReference("groups")
                                .child(groupId)
                                .child("groupMessages")
                                .push().key!!

                            getViewersList { viewersMap ->
                                val placeholder = GroupMessage(
                                    message = "",
                                    sender = senderUid,
                                    status = "uploading",
                                    messageType = "voiceRecord",
                                    messageKey = messageKey,
                                    groupUid = groupId,
                                    viewersList = viewersMap,
                                    forwarded = false,
                                    isReplyed = isReply,
                                    replyToMessage = replyMessageKey,
                                    replyMessage = replyMessage,
                                    replySender = replySender,
                                    fileName = GroupChatActivity().getFileNameFromUri(
                                        this@GroupChatActivity,
                                        uri
                                    ),
                                    fileSize = "",
                                    fileType = "",
                                    pageCount = ""
                                )

                                if (isReply) {
                                    reply_Layout.visibility = View.GONE
                                    reply_layout_close.visibility = View.GONE
                                    spaceAdjustment.visibility = View.GONE

                                    reply_image.visibility = View.GONE
                                    reply_video.visibility = View.GONE
                                    containedMessage.visibility = View.GONE
                                    messagerName.visibility = View.GONE
                                }

                                for (i in groupMembers) {
                                    FirebaseDatabase.getInstance()
                                        .getReference("groups")
                                        .child(groupId)
                                        .child("groupMessages").child(i).child("messages")
                                        .child(messageKey)
                                        .setValue(placeholder)
                                }

                                uploadMediaAndSendMessage(
                                    fileUri = uri,
                                    mediaType = "voiceRecord",
                                    senderUid = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                    viewersMap = viewersMap,
                                    attempt = 3,
                                    messageKey = messageKey
                                )

                                voiceRecordButton.visibility = View.VISIBLE
                                sendButton.visibility = View.GONE
                                voiceRecordMessageLayout.visibility = View.GONE
                                regularMessageLayout.visibility = View.VISIBLE
                                textMessageLayout.visibility = View.VISIBLE
                            }
                        } else {
                            Toast.makeText(this, "Recording too short", Toast.LENGTH_SHORT).show()
                        }
                        recordingCancelled = false
                    }
                    else{
                        Toast.makeText(this, "Recoding Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    recordingCancelled = false
                    true
                }

                else -> false
            }
        }

        val btnAttach = findViewById<ImageView>(R.id.btnAttach_group)

        btnAttach.setOnClickListener {
            showMediaPicker()
        }

        mediaPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    val clipData = data?.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            handleUri(uri,senderUid)
                        }
                    } else {
                        data?.data?.let { handleUri(it, senderUid) }
                    }
                }
            }

        audioPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                handleAudioUriForGroup(uri)
            } else {
                Toast.makeText(this, "No audio selected", Toast.LENGTH_SHORT).show()
            }
        }

        requestCameraPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                if(photoUri != null) {
                    handleUri(photoUri!!, senderUid)
                }
            }
        }

        videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val videoUri = result.data!!.data
                val videoFile = getRealFileFromUri(videoUri!!)

                if (videoFile != null) {
                    handleUri(videoUri,senderUid)
                } else {
                    Toast.makeText(this, "Invalid video file", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val menuButton: ImageView = findViewById(R.id.chatMenuButton_group)

        menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        close.setOnClickListener {
            searchBar.setText("")
            close.visibility = View.GONE
        }

        searchBar.addTextChangedListener {
            if(searchBar.text.isEmpty()){
                close.visibility = View.GONE
            }
            else{
            close.visibility = View.VISIBLE
            }
            fetchSearchedMessages(searchBar.text.toString())
        }

        searchBarBack.setOnClickListener {
            viewModel.setSearchBarLayout(View.GONE)
            viewModel.setTopBar(View.VISIBLE)
            sendColumns.visibility = View.VISIBLE
            fetchMessages()
        }

        updateSentToSeen()

        viewModel.messageSeenStatusUpdated.observe(this) { updated ->
            if (updated) {
                groupChatAdapter.notifyDataSetChanged()
                viewModel.setSeenStatusUpdated(false)
            }
        }

        backNavigation.setOnClickListener {
            if(GroupChatAdapter.IsMultipleSelect){
                resetTopBarUI()
                groupChatAdapter.notifyDataSetChanged()
            }
            else {
                finish()
            }
        }

        deleteButton.setOnClickListener {
            val dialog = DeletePopUpFragment()
            val bundle = Bundle().apply {
                putString("person","group")
            }
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "DeleteAction")
            groupChatAdapter.notifyDataSetChanged()
        }

        forwardButton.setOnClickListener {
            val intent = Intent(this@GroupChatActivity, UserAndGroupListActivity::class.java)
            intent.putExtra("groupId", groupId)
            intent.putExtra("person","groups")
            startActivity(intent)
        }

        val replyToMessageKey = intent.getStringExtra("REPLY_TO_MESSAGE_KEY")
        if (!replyToMessageKey.isNullOrEmpty()) {
            fetchGroupMessagesFromFirebase(replyToMessageKey)
        }

        checkMemberOrNot(){ flag->
            if(!flag){
                noLongerColumns.visibility = View.VISIBLE
                sendColumns.visibility = View.GONE
                IsNoLongerMember = true
            }
            else{
                noLongerColumns.visibility = View.GONE
                sendColumns.visibility = View.VISIBLE
                IsNoLongerMember = false
            }
        }

    }

    fun checkMemberOrNot(callback: (Boolean) -> Unit) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val memberRef = FirebaseDatabase.getInstance().getReference("groups")
            .child(groupId)
            .child("groupDetails")
            .child(groupId)

        memberRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var group = snapshot.getValue(Group::class.java)
                if(group?.groupMembers?.contains(FirebaseAuth.getInstance().currentUser?.uid.toString()) == true){
                    callback(true)
                }
                else{
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    fun EditTextUiChanges(message : String){

        editText_layout.visibility = View.VISIBLE
        editText_containedMessage.text = message
        reply_layout_close.visibility = View.VISIBLE

        Toast.makeText(this, "Edit Messages", Toast.LENGTH_SHORT).show()

        if(isReply){
            reply_Layout.visibility = View.GONE
            reply_layout_close.visibility = View.GONE
            spaceAdjustment.visibility = View.GONE
            containedMessage.visibility = View.GONE
            messagerName.visibility = View.GONE
            reply_image.visibility = View.GONE
            reply_video.visibility = View.GONE
            isReply = false
            replyMessageKey = ""
            replyMessage = ""
            replySender = ""
        }

        messageBox.requestFocus()
    }

    fun replyUiChanges(message : String, sender : String, messageType : String, messageKey : String,groupId : String){

        isReply = true
        replyMessage = message
        replyMessageKey = messageKey
        replySender = sender

        if(editingkey.isNotEmpty()){
            editingkey = ""
            oldMessage = ""
            messageBox.setText("")

            editText_layout.visibility = View.GONE
            reply_layout_close.visibility = View.GONE
            spaceAdjustment.visibility = View.GONE
            containedMessage.visibility = View.GONE
            messagerName.visibility = View.GONE
        }

        Toast.makeText(this, "Reply Messages", Toast.LENGTH_SHORT).show()

        reply_Layout.visibility = View.VISIBLE
        reply_layout_close.visibility = View.VISIBLE
        spaceAdjustment.visibility = View.VISIBLE
        containedMessage.visibility = View.VISIBLE
        messagerName.visibility = View.VISIBLE

        messagerName.text = sender

        if(messageType == "image") {
            reply_image.visibility = View.VISIBLE
            reply_video.visibility = View.GONE
            containedMessage.text = "🏙️ Image"
        }
        else if(messageType == "video"){
            reply_video.visibility = View.VISIBLE
            reply_image.visibility = View.GONE
            containedMessage.text = "🎥 Video"
        }
        else{
            reply_video.visibility = View.GONE
            reply_image.visibility = View.GONE
            containedMessage.text = message
        }

        messageBox.requestFocus()
    }

    fun updateSentToSeen(){
            for(i in groupMembers) {
                val messagesRef = FirebaseDatabase.getInstance()
                    .getReference("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(i)
                    .child("messages")

                messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val reversedMessages = snapshot.children.reversed()

                        for (messageSnapshot in reversedMessages) {
                            val viewersListSnapshot = messageSnapshot.child("viewersList")
                            val hasSeen =
                                viewersListSnapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                    .getValue(Boolean::class.java) == true

                            if (hasSeen) {
                                break
                            } else {
                                viewersListSnapshot.ref.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                    .setValue(true)
                            }
                            checkAllVisited(messageSnapshot.key.toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "FirebaseError",
                            "updateSentToSeen cancelled: ${error.message}"
                        )
                    }
                })
            }
            groupChatAdapter.notifyDataSetChanged()
    }

    fun updateSentToSeenCurrentUserWork() {
        for (i in groupMembers) {
            getActiveStatus(i) { currentStatus ->
                if (currentStatus == groupId) {
                    for(j in groupMembers) {
                        val messagesRef = FirebaseDatabase.getInstance()
                            .getReference("groups")
                            .child(groupId)
                            .child("groupMessages")
                            .child(j)
                            .child("messages")

                        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val reversedMessages = snapshot.children.reversed()

                                for (messageSnapshot in reversedMessages) {
                                    val viewersListSnapshot = messageSnapshot.child("viewersList")
                                    val hasSeen =
                                        viewersListSnapshot.child(i).getValue(Boolean::class.java) == true

                                    if (hasSeen) {
                                        break
                                    } else {
                                        viewersListSnapshot.ref.child(i).setValue(true)
                                    }
                                    checkAllVisited(messageSnapshot.key.toString())
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(
                                    "FirebaseError",
                                    "updateSentToSeen cancelled: ${error.message}"
                                )
                            }
                        })
                    }
                }
            }
        }
        groupChatAdapter.notifyDataSetChanged()
    }

    fun checkAllVisited(messageKey: String) {
        for(i in groupMembers) {
            val viewersListRef = FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupId)
                .child("groupMessages")
                .child(i)
                .child("messages")
                .child(messageKey)
                .child("viewersList")

            viewersListRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allViewed = groupMembers.all { uid ->
                        snapshot.child(uid).getValue(Boolean::class.java) == true
                    }

                    if (allViewed) {
                        for (member in groupMembers) {
                            FirebaseDatabase.getInstance()
                                .getReference("groups")
                                .child(groupId)
                                .child("groupMessages")
                                .child(member)
                                .child("messages")
                                .child(messageKey)
                                .child("status")
                                .setValue("seen")
                        }
                    }
                    groupChatAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "checkAllVisited cancelled: ${error.message}")
                }
            })
        }
    }

    fun fetchSearchedMessages(text: String) {
        messageList.clear()
        val seenKeys = mutableSetOf<String>()
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return

        mDbRef.child("groups").child(groupId).child("groupMessages").child(currentUser)
            .child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (msgSnap in snapshot.children) {
                        val message = msgSnap.getValue(GroupMessage::class.java)
                        val key = message?.messageKey
                        if (message != null &&
                            key != null &&
                            key !in seenKeys &&
                            message.message?.contains(text, ignoreCase = true) == true
                        ) {
                            seenKeys.add(key)
                            messageList.add(message)
                        }
                    }

                    groupChatAdapter.notifyDataSetChanged()

                    var searchEditText = findViewById<EditText>(R.id.searchEditText_chat_group)
                    searchEditText.post {
                        searchEditText.requestFocus()
                        searchEditText.setSelection(searchEditText.text.length)
                    }

                    if (messageList.isNotEmpty()) {
                        groupChatRecyclerView.scrollToPosition(0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@GroupChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

    }

    fun fetchMessages(){
        messageList.clear()
        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        mDbRef.child("groups").child(groupId).child("groupMessages").child(currentUser).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postsnapshot in snapshot.children) {
                        val message = postsnapshot.getValue(GroupMessage::class.java)
                        messageList.add(message!!)
                    }
                    groupChatAdapter.notifyDataSetChanged()
                    groupChatAdapter.notifyItemInserted(messageList.size - 1)
                    groupChatRecyclerView.scrollToPosition(messageList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@GroupChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.receiver_settings_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.searchMessage_menu -> {
                    viewModel.setSearchBarLayout(View.VISIBLE)
                    searchBar.requestFocus()
                    viewModel.setTopBar(View.GONE)
                    close.visibility = View.GONE
                    sendColumns.visibility = View.GONE
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)
                    IsSearchBar = true
                    true
                }
                R.id.clearChat -> {
                    FirebaseDatabase.getInstance()
                        .getReference("groups")
                        .child(groupId)
                        .child("groupMessages")
                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                        .child("messages")
                        .removeValue()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun handleAudioUriForGroup(audioUri: Uri) {
        val messageKey = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupMessages")
            .push().key!!

        getViewersList { viewersMap ->

            val placeholder = GroupMessage(
                message = "",
                sender = senderUid,
                status = "sending",
                messageType = "voiceRecord",
                messageKey = messageKey,
                groupUid = groupId,
                viewersList = viewersMap,
                forwarded = false,
                isReplyed = isReply,
                replyToMessage = replyMessageKey,
                replyMessage = replyMessage,
                replySender = replySender,
                fileName = GroupChatActivity().getFileNameFromUri(this@GroupChatActivity,audioUri),
                fileSize = "",
                fileType = "",
                pageCount = ""
            )

            if (isReply) {
                reply_Layout.visibility = View.GONE
                reply_layout_close.visibility = View.GONE
                spaceAdjustment.visibility = View.GONE
                reply_image.visibility = View.GONE
                reply_video.visibility = View.GONE
                containedMessage.visibility = View.GONE
                messagerName.visibility = View.GONE
            }

            for (memberId in groupMembers) {
                FirebaseDatabase.getInstance()
                    .getReference("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(memberId)
                    .child("messages")
                    .child(messageKey)
                    .setValue(placeholder)
            }

            uploadMediaAndSendMessage(
                fileUri = audioUri,
                mediaType = "voiceRecord",
                senderUid = senderUid!!,
                messageKey = messageKey,
                viewersMap = viewersMap,
                attempt = 3
            )
        }
    }

    private fun showMediaPicker() {
        val picker = MediaPickerFragment(object : MediaPickerListener {
            override fun onMediaSelected() {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }

                mediaPickerLauncher.launch(Intent.createChooser(intent, "Select media"))

            }

            override fun onCameraProcess() {
                val dialog = CameraActionsFragment(
                    onTakePhoto = {
                        val imageFile = createMediaFile("jpg")
                        photoUri = FileProvider.getUriForFile(
                            this@GroupChatActivity,
                            "${applicationContext.packageName}.fileprovider",
                            imageFile
                        )
                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                    },
                    onTakeVideo = {
                        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                        videoLauncher.launch(videoIntent)
                    }
                )
                dialog.show(supportFragmentManager, "DeleteAction")
            }

            override fun onDocumentSelect() {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "text/plain",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                    ))
                }
                startActivityForResult(Intent.createChooser(intent, "Select Document(s)"), PICK_DOCUMENT_REQUEST)
            }

            override fun onAudioSelect() {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "audio/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                startActivityForResult(Intent.createChooser(intent, "Select Audio"), PICK_AUDIO_REQUEST)
            }

        })
        picker.show(supportFragmentManager, "MediaPicker")
    }

    fun handleUri(uri: Uri, senderUid: String?) {
        val mimeType = contentResolver.getType(uri)
        val messageType = when {
            mimeType?.startsWith("image") == true -> "image"
            mimeType?.startsWith("video") == true -> "video"
            else -> "text"
        }

        val messageKey = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupMessages")
            .push().key!!

        getViewersList { viewersMap ->
            val placeholder = GroupMessage(
                message = "",
                sender = senderUid,
                status = "uploading",
                messageType = messageType,
                messageKey = messageKey,
                groupUid = groupId,
                viewersList = viewersMap,
                forwarded = false,
                isReplyed = isReply,
                replyToMessage = replyMessageKey,
                replyMessage = replyMessage,
                replySender = replySender,
                fileName = getFileNameFromUri(this@GroupChatActivity,uri),
                fileSize = "",
                fileType = "",
                pageCount = ""
            )

            if(isReply){
                reply_Layout.visibility = View.GONE
                reply_layout_close.visibility = View.GONE
                spaceAdjustment.visibility = View.GONE

                reply_image.visibility = View.GONE
                reply_video.visibility = View.GONE
                containedMessage.visibility = View.GONE
                messagerName.visibility = View.GONE
            }

            for (i in groupMembers) {
                FirebaseDatabase.getInstance()
                    .getReference("groups")
                    .child(groupId)
                    .child("groupMessages").child(i).child("messages").child(messageKey)
                    .setValue(placeholder)
            }

            uploadMediaAndSendMessage(uri, messageType, senderUid!!, messageKey, viewersMap)
        }
    }

    fun getViewersList(callback: (HashMap<String, Boolean>) -> Unit) {
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

    fun uploadMediaAndSendMessage(
        fileUri: Uri,
        mediaType: String,
        senderUid: String,
        messageKey: String,
        viewersMap: HashMap<String, Boolean>,
        attempt: Int = 1
    ) {
        val MAX_RETRIES = 3
        val RETRY_DELAY_MS = 2000L
        val contentResolver = contentResolver

        val (mimeType, extension) = when (mediaType) {
            "image" -> "image/jpeg" to "jpg"
            "video" -> "video/mp4" to "mp4"
            "voiceRecord" -> "audio/mpeg" to "mp3"
            "document" -> {
                val fileName = getFileNameFromUri(fileUri)
                val ext = fileName?.substringAfterLast('.', "dat") ?: "dat"
                val type = contentResolver.getType(fileUri) ?: "application/octet-stream"
                type to ext
            }
            else -> {
                val type = contentResolver.getType(fileUri) ?: "application/octet-stream"
                val ext = when {
                    type.startsWith("image") -> "jpg"
                    type.startsWith("video") -> "mp4"
                    type.startsWith("audio") -> "mp3"
                    else -> "dat"
                }
                type to ext
            }
        }

        val fileName = "chat_media_${System.currentTimeMillis()}.$extension"
        val inputStream = contentResolver.openInputStream(fileUri) ?: return
        val fileBytes = inputStream.readBytes()
        inputStream.close()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", fileName,
                fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
            .addFormDataPart("fileName", fileName)
            .build()

        val privateKey = "private_HuUA3m3WeeBImajVyqgJaDzCSyY="
        val credentials = Credentials.basic(privateKey, "")

        val request = Request.Builder()
            .url("https://upload.imagekit.io/api/v1/files/upload")
            .addHeader("Authorization", credentials)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (attempt < MAX_RETRIES) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        uploadMediaAndSendMessage(fileUri, mediaType, senderUid, messageKey, viewersMap, attempt + 1)
                    }, RETRY_DELAY_MS)
                } else {
                    runOnUiThread {
                        Toast.makeText(this@GroupChatActivity, "Upload failed after retries", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val jsonObject = JSONObject(body ?: "")
                val uploadedUrl = jsonObject.optString("url", null)

                if (uploadedUrl != null) {
                    runOnUiThread {

                        var pagecount=""
                        var fileSize=""
                        var fileType=""

                        if(mediaType == "document"){
                            pagecount = GroupChatActivity().getPageCountFromUri(this@GroupChatActivity,fileUri).toString()
                            fileSize = GroupChatActivity().getFileSize(this@GroupChatActivity,fileUri)
                            fileType = GroupChatActivity().getFileType(this@GroupChatActivity,fileUri,getFileNameFromUri(this@GroupChatActivity,fileUri))
                        }

                        val updatedMessage = GroupMessage(
                            message = uploadedUrl,
                            sender = senderUid,
                            status = "sent",
                            messageType = mediaType,
                            messageKey = messageKey,
                            groupUid = groupId,
                            viewersList = viewersMap,
                            forwarded = false,
                            isReplyed = isReply,
                            replyToMessage = replyMessageKey,
                            replyMessage = replyMessage,
                            replySender = replySender,
                            fileName = GroupChatActivity().getFileNameFromUri(this@GroupChatActivity,fileUri),
                            fileSize = fileSize,
                            fileType = fileType,
                            pageCount = pagecount
                        )

                        if(isReply){
                            isReply = false
                            replyMessageKey = ""
                            replyMessage = ""
                        }

                        for (i in groupMembers) {
                            FirebaseDatabase.getInstance()
                                .getReference("groups")
                                .child(groupId)
                                .child("groupMessages")
                                .child(i)
                                .child("messages")
                                .child(messageKey)
                                .setValue(updatedMessage)
                        }

                        setArchieve()
                        groupChatAdapter.notifyDataSetChanged()
                    }
                } else {
                    onFailure(call, IOException("No URL in ImageKit response"))
                }
            }
        })
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
    fun setArchieve(){

        var currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val chatSendRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("groupMessages").child(currentUserId).ref

        chatSendRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChild("archive")) {
                    for (i in groupMembers) {
                        mDbRef.child("groups").child(groupId).child("groupMessages")
                            .child(i).child("archive").setValue(false)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CheckArchive", "Error: ${error.message}")
            }
        })

    }

    private fun setUserOnlineStatus(status: String) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            FirebaseDatabase.getInstance().getReference("user").child(userId).child("activeChatUid").setValue(status)
        }
    }

    private fun getActiveStatus(uid: String, callback: (String?) -> Unit) {
        FirebaseDatabase.getInstance()
            .getReference("user")
            .child(uid)
            .child("activeChatUid")
            .get()
            .addOnSuccessListener { snapshot ->
                val status = snapshot.getValue(String::class.java)
                callback(status)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_AUDIO
                ),
                101
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                101
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording() {
        try {
            voiceRecordMessageLayout.visibility = View.VISIBLE
            textMessageLayout.visibility = View.GONE
            secondsElapsed = 0
            updateTimerText(secondsElapsed)

            val musicDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            if (musicDir != null && !musicDir.exists()) {
                musicDir.mkdirs()
            }
            outputFile = "${musicDir?.absolutePath}/audio_${System.currentTimeMillis()}.mp3"


            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)
                prepare()
                start()
            }

            timer = object : CountDownTimer(600000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    secondsElapsed++
                    updateTimerText(secondsElapsed)
                }

                override fun onFinish() {}
            }.start()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        Log.d("Recording", "Recording stopped")
        try {
            voiceRecordMessageLayout.visibility = View.GONE
            textMessageLayout.visibility = View.VISIBLE
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            timer?.cancel()
            MediaScannerConnection.scanFile(this, arrayOf(outputFile), null, null)
//            Toast.makeText(this, "Recording saved: $outputFile", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Recording error", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelRecording(){
        try {
            voiceRecordMessageLayout.visibility = View.GONE
            textMessageLayout.visibility = View.VISIBLE
            recordingCancelled = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Recording error", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateTimerText(seconds: Int) {
        val mins = seconds / 60
        val secs = seconds % 60
        timerTextView.text = String.format("%02d:%02d", mins, secs)
    }

    override fun onStop() {
        super.onStop()
        setUserOnlineStatus("online")
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        timer?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val audioUri = clipData.getItemAt(i).uri
                    handleGroupAudioUpload(audioUri)
                }
            } else if (data.data != null) {
                val audioUri = data.data!!
                handleGroupAudioUpload(audioUri)
            }
        }
        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    handleGroupDocumentUpload(uri)
                }
            } else if (data.data != null) {
                val uri = data.data!!
                handleGroupDocumentUpload(uri)
            }
        }
    }

    private fun handleGroupAudioUpload(audioUri: Uri) {
        val messageKey = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupMessages")
            .push().key!!

        getViewersList { viewersMap ->

            val placeholder = GroupMessage(
                message = "",
                sender = senderUid,
                status = "sending",
                messageType = "audio",
                messageKey = messageKey,
                groupUid = groupId,
                viewersList = viewersMap,
                forwarded = false,
                isReplyed = isReply,
                replyToMessage = replyMessageKey,
                replyMessage = replyMessage,
                replySender = replySender,
                fileName = getFileNameFromUri(this@GroupChatActivity, audioUri),
                fileSize = "",
                fileType = "",
                pageCount = ""
            )

            if (isReply) {
                reply_Layout.visibility = View.GONE
                reply_layout_close.visibility = View.GONE
                spaceAdjustment.visibility = View.GONE
                reply_image.visibility = View.GONE
                reply_video.visibility = View.GONE
                containedMessage.visibility = View.GONE
                messagerName.visibility = View.GONE
            }

            for (memberId in groupMembers) {
                FirebaseDatabase.getInstance()
                    .getReference("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(memberId)
                    .child("messages")
                    .child(messageKey)
                    .setValue(placeholder)
            }

            uploadMediaAndSendMessage(
                fileUri = audioUri,
                mediaType = "audio",
                senderUid = senderUid!!,
                messageKey = messageKey,
                viewersMap = viewersMap,
                attempt = 3
            )
        }
    }

    private fun handleGroupDocumentUpload(documentUri: Uri) {
        val messageKey = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupMessages")
            .push().key!!

        getViewersList { viewersMap ->

            val placeholder = GroupMessage(
                message = "",
                sender = senderUid,
                status = "sending",
                messageType = "document",
                messageKey = messageKey,
                groupUid = groupId,
                viewersList = viewersMap,
                forwarded = false,
                isReplyed = isReply,
                replyToMessage = replyMessageKey,
                replyMessage = replyMessage,
                replySender = replySender,
                fileName = getFileNameFromUri(this@GroupChatActivity, documentUri),
                fileSize = "",
                fileType = "",
                pageCount = ""
            )

            if (isReply) {
                reply_Layout.visibility = View.GONE
                reply_layout_close.visibility = View.GONE
                spaceAdjustment.visibility = View.GONE
                reply_image.visibility = View.GONE
                reply_video.visibility = View.GONE
                containedMessage.visibility = View.GONE
                messagerName.visibility = View.GONE
            }

            for (memberId in groupMembers) {
                FirebaseDatabase.getInstance()
                    .getReference("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(memberId)
                    .child("messages")
                    .child(messageKey)
                    .setValue(placeholder)
            }

            uploadMediaAndSendMessage(
                fileUri = documentUri,
                mediaType = "document",
                senderUid = senderUid!!,
                messageKey = messageKey,
                viewersMap = viewersMap,
                attempt = 3
            )
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = "unknown_file"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }

    fun getFileSize(context: Context, uri: Uri): String {
        return try {
            var sizeInBytes: Long? = null

            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1 && it.moveToFirst()) {
                        sizeInBytes = it.getLong(sizeIndex)
                    }
                }
            } else if (uri.scheme == ContentResolver.SCHEME_FILE) {
                sizeInBytes = File(uri.path ?: "").length()
            }

            if (sizeInBytes != null && sizeInBytes > 0) {
                val sizeInKB = sizeInBytes / 1024
                val sizeInMB = sizeInKB / 1024.0
                if (sizeInMB >= 1) "%.2f MB".format(sizeInMB) else "$sizeInKB KB"
            } else {
                "Unknown size"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error"
        }
    }

    fun getPageCountFromUri(context: Context, uri: Uri): Int {
        val mimeType = context.contentResolver.getType(uri)

        return when {
            mimeType?.startsWith("application/pdf") == true -> {
                try {
                    val inputStream = context.contentResolver.openFileDescriptor(uri, "r") ?: return -1
                    val renderer = PdfRenderer(inputStream)
                    val pageCount = renderer.pageCount
                    renderer.close()
                    inputStream.close()
                    pageCount
                } catch (e: Exception) {
                    e.printStackTrace()
                    -1
                }
            }

            mimeType?.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") == true -> {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val doc = XWPFDocument(inputStream)
                    val paragraphCount = doc.paragraphs.size
                    doc.close()
                    inputStream?.close()
                    (paragraphCount / 40).coerceAtLeast(1)
                } catch (e: Exception) {
                    e.printStackTrace()
                    -1
                }
            }

            mimeType?.startsWith("text/plain") == true -> {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val lines = inputStream?.bufferedReader()?.readLines()
                    inputStream?.close()
                    (lines?.size?.div(50) ?: 1).coerceAtLeast(1)
                } catch (e: Exception) {
                    e.printStackTrace()
                    -1
                }
            }

            else -> {
                -1
            }
        }
    }

    fun getFileType(context: Context, uri: Uri,fileName : String): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)

        return when {
            mimeType?.contains("pdf") == true -> "PDF"
            mimeType?.contains("msword") == true -> "DOC"
            mimeType?.contains("vnd.openxmlformats-officedocument.wordprocessingml.document") == true -> "DOCX"
            mimeType?.contains("text/plain") == true -> "TXT"
            mimeType?.contains("vnd.ms-excel") == true -> "XLS"
            mimeType?.contains("vnd.openxmlformats-officedocument.spreadsheetml.sheet") == true -> "XLSX"
            mimeType?.contains("audio") == true -> "Audio"
            mimeType?.contains("video") == true -> "Video"
            mimeType?.contains("image") == true -> "Image"
            else -> {
                val extension = getFileExtension(context, uri,fileName)
                extension?.uppercase() ?: "Unknown"
            }
        }
    }

    fun getFileExtension(context: Context, uri: Uri,fileName : String): String? {
        return try {
            fileName?.substringAfterLast('.', "")?.lowercase()
        } catch (e: Exception) {
            null
        }
    }

    override fun onUserLongClicked(position: Int) {
        val uid  = messageList[position].messageKey.toString()
        if(SelectMultipleMessages.contains(uid)){
            SelectMultipleMessages.remove(uid)
            selectCount.text = SelectMultipleMessages.size.toString()
        }
        else{
            SelectMultipleMessages.add(uid)
            selectCount.text = SelectMultipleMessages.size.toString()
        }
        groupChatAdapter.notifyDataSetChanged()
    }

    override fun onUserClicked(position: Int): Boolean {
        val uid  = messageList[position].messageKey.toString()
        if(SelectMultipleMessages.contains(uid)){
            Log.d("OnUserClicked-if", SelectMultipleMessages.toString())
            SelectMultipleMessages.remove(uid)
            if(SelectMultipleMessages.size == 0){
                resetTopBarUI()
            }
            else {
                selectCount.text = SelectMultipleMessages.size.toString()
                groupChatAdapter.notifyDataSetChanged()
            }
            return true
        }
        else{
            Log.d("OnUserClicked-else", SelectMultipleMessages.toString())
            if(SelectMultipleMessages.size <=5 ){
                SelectMultipleMessages.add(uid)
                groupChatAdapter.notifyDataSetChanged()
                selectCount.text = SelectMultipleMessages.size.toString()
            }

            return false
        }
    }

    fun resetTopBarUI(){
        selectCount.text = "0"
        SelectMultipleMessages.clear()
        groupChatAdapter.notifyDataSetChanged()
        GroupChatAdapter.IsMultipleSelect = false
        group_ProfileImage.visibility = View.VISIBLE
        nameBar.visibility = View.VISIBLE
        selectCount.visibility = View.GONE
        deleteButton.visibility = View.GONE
        forwardButton.visibility = View.GONE
    }

    fun processUi(){
        Log.d("ProcessUi-inside","true")
        group_ProfileImage.visibility = View.INVISIBLE
        nameBar.visibility = View.INVISIBLE
        selectCount.visibility = View.VISIBLE
        backNavigation.visibility = View.VISIBLE
        deleteButton.visibility = View.VISIBLE
        forwardButton.visibility = View.VISIBLE
        groupChatAdapter.notifyDataSetChanged()
        selectCount.text = "1"
    }

    override fun onMultiSelectStarted() {
        Log.d("ProcessUi","true")
        processUi()
    }

    override fun onMultiSelectEnded() {
        resetTopBarUI()
    }

    override fun onResume(){
        super.onResume()
        GroupChatAdapter.IsMultipleSelect=false
        SelectMultipleMessages.clear()
        groupChatAdapter.notifyDataSetChanged()
        resetTopBarUI()
    }

    override fun onBackPressed() {
        if(IsSearchBar){
            IsSearchBar=false
            viewModel.setSearchBarLayout(View.GONE)
            viewModel.setTopBar(View.VISIBLE)
            searchBar.text.clear()
            fetchMessages()
            sendColumns.visibility = View.VISIBLE
        }
        else if(GroupChatAdapter.IsMultipleSelect){
            resetTopBarUI()
            groupChatAdapter.notifyDataSetChanged()
        }
        else {
            super.onBackPressed()
            finish()
        }
    }

    override fun onReplyClicked(replyToMessageKey: String) {
        val position = messageList.indexOfFirst { it.messageKey == replyToMessageKey }
        Log.d("Highlights",position.toString())
        if (position != -1) {
            highlightRepliedMessage(position)
        } else {
            Toast.makeText(this, "Message not found", Toast.LENGTH_SHORT).show()
        }
    }
    fun highlightRepliedMessage(position: Int) {
        groupChatRecyclerView.scrollToPosition(position)

        groupChatRecyclerView.post {
            val viewHolder = groupChatRecyclerView.findViewHolderForAdapterPosition(position)
            if (viewHolder != null) {
                val itemView = viewHolder.itemView
                val highlightColor = ContextCompat.getColor(this@GroupChatActivity, R.color.highlight_light)

                itemView.setBackgroundColor(highlightColor)

                itemView.postDelayed({
                    val fadeOut = ObjectAnimator.ofArgb(
                        itemView,
                        "backgroundColor",
                        highlightColor,
                        Color.TRANSPARENT
                    )
                    fadeOut.duration = 1000
                    fadeOut.start()
                }, 1000)
            }
        }
    }

    private fun fetchGroupMessagesFromFirebase(replyToMessageKey: String) {
        val groupRef = FirebaseDatabase.getInstance().getReference("groups")
            .child(groupId)
            .child("groupMessages")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .child("messages")

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()

                for (snap in snapshot.children) {
                    val message = snap.getValue(GroupMessage::class.java)
                    message?.messageKey = snap.key
                    if (message != null) {
                        messageList.add(message)
                    }
                }

                groupChatAdapter.notifyDataSetChanged()

                val position = messageList.indexOfFirst { it.messageKey == replyToMessageKey }
                Log.d("GroupHighlights", "ReplyTo Key: $replyToMessageKey at Position: $position")

                if (position != -1) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        highlighting(position)
                    }, 100)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GroupChat", "Error fetching group messages: ${error.message}")
            }
        })
    }

    private fun highlighting(position: Int) {
        groupChatRecyclerView.scrollToPosition(position)

        groupChatRecyclerView.doOnPreDraw {
            groupChatRecyclerView.post {
                val viewHolder = groupChatRecyclerView.findViewHolderForAdapterPosition(position)

                if (viewHolder != null) {
                    val highlightColor = ContextCompat.getColor(this, R.color.highlight_light)

                    when (viewHolder) {
                        is GroupChatAdapter.GroupSentViewHolder -> {
                            val layout = viewHolder.itemView.findViewById<View>(R.id.sent_message_group_layout)
                            applyHighlight(layout, highlightColor)
                        }
                        is GroupChatAdapter.GroupReceiveViewHolder -> {
                            val layout = viewHolder.itemView.findViewById<View>(R.id.received_message_layout)
                            applyHighlight(layout, highlightColor)
                        }
                        is GroupChatAdapter.GroupSentReplyViewHolder -> {
                            val layout = viewHolder.itemView.findViewById<View>(R.id.receive_message_group_layout)
                            applyHighlight(layout, highlightColor)
                        }
                        is GroupChatAdapter.GroupReceiveReplyViewHolder -> {
                            val layout = viewHolder.itemView.findViewById<View>(R.id.receive_message_group_reply_layout)
                            applyHighlight(layout, highlightColor)
                        }
                        else -> Log.d("Highlights", "Unknown ViewHolder at position $position")
                    }
                } else {
                    Log.d("Highlights", "ViewHolder was null for position $position")
                }
            }
        }
    }

    private fun applyHighlight(view: View?, highlightColor: Int) {
        view?.let {
            it.setBackgroundColor(highlightColor)

            val fadeOut = ObjectAnimator.ofArgb(
                it,
                "backgroundColor",
                highlightColor,
                Color.TRANSPARENT
            )
            fadeOut.startDelay = 500
            fadeOut.duration = 1000
            fadeOut.start()
        }
    }

    private fun openCamera() {
        val imageFile = createMediaFile("jpg")
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            imageFile
        )
        photoUri = uri
        cameraLauncher.launch(uri)
    }

    fun getRealFileFromUri(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("video_", ".mp4", cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }

    fun createMediaFile(extension: String): File {
        val fileName = "media_${System.currentTimeMillis()}.$extension"
        return File(cacheDir, fileName)
    }
}