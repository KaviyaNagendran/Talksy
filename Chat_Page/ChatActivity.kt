package com.example.android.myproject.Chat_Page

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
import com.example.android.myproject.Chat_Main.MainActivity
import com.example.android.myproject.DeleteAction.CameraActionsFragment
import com.example.android.myproject.DeleteAction.DeleteActionFragment
import com.example.android.myproject.DeleteAction.DeletePopUpFragment
import com.example.android.myproject.Listeners.MediaPickerFragment
import com.example.android.myproject.Listeners.MediaPickerListener
import com.example.android.myproject.Entities.Message
import com.example.android.myproject.Forward_Message.UserAndGroupListActivity
import com.example.android.myproject.Listeners.CallBackListenerMessageSearch
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
import kotlin.toString

class ChatActivity : AppCompatActivity() , MultipleMessageSelectListener , OnUserLongClickListener, ReplyHighlightListener{
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference
    var senderRoom : String? = null
    var receiverRoom : String? = null
    private var senderUid: String? = null
    private var receiverUid: String? = null
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var sendColumns: View
    private lateinit var topBar : FrameLayout
    private lateinit var nameBar : RelativeLayout
    private lateinit var searchEditText : EditText
    private lateinit var searchBarLayout : LinearLayout
    private lateinit var close : ImageView
    private lateinit var ImageLayout : RelativeLayout
    private lateinit var receiverProfile : ShapeableImageView
    private lateinit var receiverName : TextView
    private lateinit var activeStatus : TextView
    private lateinit var backNavigation : ImageView
    private lateinit var deleteButton : ImageView
    private lateinit var forwardButton : ImageView
    private lateinit var menuButton : ImageView
    private lateinit var selectCount : TextView
    private lateinit var reply_Layout : FrameLayout
    private lateinit var reply_layout_close : ImageView
    private lateinit var spaceAdjustment : View
    private lateinit var reply_image : ImageView
    private lateinit var reply_video : ImageView
    private lateinit var containedMessage : TextView
    private lateinit var messagerName : TextView
    private lateinit var mediaPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher : ActivityResultLauncher<Array<String>>
    private lateinit var cameraLauncher : ActivityResultLauncher<Uri>
    private lateinit var videoLauncher : ActivityResultLauncher<Intent>
    private lateinit var regularMessageLayout : FrameLayout
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""
    private var recordingStartTime : Long = System.currentTimeMillis()
    private var secondsElapsed = 0
    private var timer: CountDownTimer? = null
    private lateinit var voiceRecordMessageLayout: FrameLayout
    private lateinit var timerTextView: TextView
    private lateinit var voiceRecordButton: ImageButton
    private lateinit var editText_layout : FrameLayout
    private lateinit var editText_messagerName : TextView
    private lateinit var editText_containedMessage : TextView
    private lateinit var textMessageLayout : FrameLayout
    private lateinit var searchBarBack : ImageView
    private val PICK_AUDIO_REQUEST = 101
    private val PICK_DOCUMENT_REQUEST = 104
    private var initialX = 0f
    private val SWIPE_THRESHOLD = 150

    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    companion object{
        var editingkey : String = ""
        var oldMessage : String? = ""
        lateinit var selectList : List<String>
        var isReply : Boolean = false
        var replyMessage : String = ""
        var replyMessageKey : String = ""
        var replySender : String = ""
        var SelectMultipleMessages = ArrayList<String>()
        var senderRoomGlobal = ""
        var IsSearchBar = false

        var photoUri : Uri? = null
        var videoUri : Uri? = null
        var recordingCancelled = false


    }

    fun editMessage(message: String?) {
        messageBox.setText(message)
        oldMessage = message
        messageBox.requestFocus()
    }

    private var localActiveReceiver : String? = null
    private lateinit var viewModel : ChatActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val name = intent.getStringExtra("name")
        receiverUid = intent.getStringExtra("uid")
        val profile = intent.getStringExtra("profileUrl")

        rootLayout = findViewById(R.id.rootLayout)
        sendColumns = findViewById(R.id.sendcolumns)
        topBar = findViewById(R.id.topBar)
        searchBarLayout = findViewById(R.id.searchbar_chat)
        close = findViewById<ImageView>(R.id.close)
        searchBarBack = findViewById<ImageView>(R.id.searchBar_back_Chat)

        ImageLayout = findViewById(R.id.image_layout)
        receiverProfile = findViewById(R.id.receiverProfile)
        receiverName = findViewById(R.id.receiverName)
        activeStatus = findViewById(R.id.activeStatus)
        backNavigation = findViewById(R.id.BackNavigation)
        deleteButton = findViewById(R.id.DeleteButton_chat)
        forwardButton = findViewById(R.id.ForwardButton_chat)
        menuButton = findViewById(R.id.chatMenuButton)
        selectCount = findViewById(R.id.selectCount_chat)

        reply_Layout = findViewById(R.id.reply_layout)
        reply_layout_close = findViewById(R.id.reply_layout_close)
        spaceAdjustment = findViewById(R.id.spaceAdjustmentView)
        reply_image = findViewById(R.id.reply_image)
        reply_video = findViewById(R.id.reply_video)
        containedMessage = findViewById(R.id.contained_message)
        messagerName = findViewById(R.id.messager_name)

        textMessageLayout = findViewById<FrameLayout>(R.id.textMessageLayout)

        regularMessageLayout = findViewById(R.id.regular_message)
        voiceRecordMessageLayout = findViewById(R.id.voiceRecord_message)
        voiceRecordButton = findViewById(R.id.voiceRecord_button)

        textMessageLayout.visibility = View.VISIBLE
        voiceRecordMessageLayout.visibility = View.GONE

        editText_layout = findViewById(R.id.editText_layout)
        editText_messagerName = findViewById(R.id.edit_messager_name)
        editText_containedMessage = findViewById(R.id.edit_contained_message)

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

        messageRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.message)
        sendButton = findViewById(R.id.send_button)

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, supportFragmentManager)

        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = messageAdapter

        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUid + senderUid
        senderRoomGlobal = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        fetchMessages()

        voiceRecordMessageLayout = findViewById(R.id.voiceRecord_message)
        timerTextView = findViewById(R.id.timerTextView)
        voiceRecordButton = findViewById(R.id.voiceRecord_button)

        voiceRecordButton.visibility = View.VISIBLE
        sendButton.visibility = View.GONE
        voiceRecordMessageLayout.visibility = View.GONE
        regularMessageLayout.visibility = View.VISIBLE

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

        selectList = emptyList()

        viewModel = ViewModelProvider(this).get(ChatActivityViewModel::class.java)

        viewModel.imageLayout.observe(this) { value ->
            ImageLayout.visibility = value
        }

        viewModel.receiverProfile.observe(this) { value ->
            Glide.with(this).load(value).into(receiverProfile)
        }

        viewModel.receiverName.observe(this) { value ->
            receiverName.visibility = value
        }

        viewModel.activeStatus.observe(this) { value ->
            activeStatus.visibility = value
        }

        viewModel.backNavigation.observe(this) { value ->
            backNavigation.visibility = value
        }

        viewModel.deleteButton.observe(this) { value ->
            deleteButton.visibility = value
        }

        viewModel.selectList.observe(this) { value ->
            selectList = value
        }

        viewModel.selectCount.observe(this) { value ->
            selectCount.visibility = value
        }

        viewModel.isLongPressed.observe(this) { value ->
            if (value) {
                viewModel.setDeleteButton(View.VISIBLE)
                viewModel.setEditButton(View.VISIBLE)
                viewModel.setCopyButton(View.VISIBLE)
                viewModel.setSelectCount(View.VISIBLE)

                viewModel.setImageLayout(View.GONE)
                viewModel.setReceiverName(View.GONE)
                viewModel.setActiveStatus(View.GONE)
            }
            else {
                viewModel.setDeleteButton(View.GONE)
                viewModel.setEditButton(View.GONE)
                viewModel.setCopyButton(View.GONE)
                viewModel.setSelectCount(View.GONE)

                viewModel.setImageLayout(View.VISIBLE)
                viewModel.setReceiverName(View.VISIBLE)
                viewModel.setActiveStatus(View.VISIBLE)
            }
            messageAdapter.notifyDataSetChanged()
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            sendColumns.translationY = -imeHeight.toFloat()

            messageRecyclerView.setPadding(
                messageRecyclerView.paddingLeft,
                messageRecyclerView.paddingTop,
                messageRecyclerView.paddingRight,
                imeHeight
            )

            messageRecyclerView.post {
                messageRecyclerView.scrollToPosition(messageList.size - 1)
            }

            insets
        }

        sendColumns.setOnClickListener {
            MainActivity().updateSentToReceived()
            updateReceivedToSeen()
        }

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

        if (name != null) {
            supportActionBar?.title = name
            receiverName.text = name
        } else {
            Toast.makeText(this, "Name is missing in Intent", Toast.LENGTH_SHORT).show()
            finish()
        }

        if(profile != ""){
            Glide.with(this).load(profile).into(receiverProfile)
        }
        else {
            receiverProfile.setImageResource(R.drawable.default_profile_image)
        }

        sendButton.setOnClickListener {

            val messageText = messageBox.text.toString().trim()

            if(messageText.length>1000){
                Toast.makeText(this, "Message length cannot exceed 1000 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (messageText.isEmpty()) {
                return@setOnClickListener
            }

            if (editingkey.isNotEmpty() && oldMessage != messageText) {
                val messageKeyToUpdate = editingkey

                val updatedMessageData = mapOf<String, Any?>(
                    "message" to messageText,
                    "edited" to true
                )

                mDbRef.child("chats").child(senderRoom!!).child("messages")
                    .child(messageKeyToUpdate)
                    .updateChildren(updatedMessageData)
                    .addOnSuccessListener {
                        mDbRef.child("chats").child(receiverRoom!!).child("messages")
                            .child(messageKeyToUpdate)
                            .updateChildren(updatedMessageData)
                            .addOnSuccessListener {
                                Log.d(
                                    "ChatActivity",
                                    "Message successfully updated and marked as edited."
                                )
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Failed to update receiver's message: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to update sender's message: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                editText_layout.visibility = View.GONE
                reply_layout_close.visibility = View.GONE

                editingkey = ""
                messageBox.setText("")

                setArchieve()


            } else if (oldMessage == messageText) {
                editingkey = ""
                messageBox.setText("")
                Log.d(
                    "ChatActivity",
                    "Message not updated Because Old Message and New Message are same"
                )
            } else {
                val messageRef = mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                val messageKey = messageRef.key!!

                val messageObject = Message(
                    message = messageText,
                    sender = senderUid,
                    receiver = receiverUid,
                    messageType = "text",
                    status = "sent",
                    messageKey = messageKey,
                    edited = false,
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
                    replySender = ""
                    reply_Layout.visibility = View.GONE
                    reply_layout_close.visibility = View.GONE
                    spaceAdjustment.visibility = View.GONE

                    reply_image.visibility = View.GONE
                    reply_video.visibility = View.GONE
                    containedMessage.visibility = View.GONE
                    messagerName.visibility = View.GONE
                }

                messageRef.setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").child(messageKey)
                        .setValue(messageObject)
                }

                setArchieve()

                messageBox.setText("")
                messageBox.requestFocus()
            }
            messageAdapter.notifyDataSetChanged()

            voiceRecordButton.visibility = View.VISIBLE
            sendButton.visibility = View.GONE
            voiceRecordMessageLayout.visibility = View.GONE
            regularMessageLayout.visibility = View.VISIBLE

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
                    var recordingEndTime = System.currentTimeMillis()
                    var duration = recordingEndTime - recordingStartTime
                    stopRecording()

                    if (!recordingCancelled){
                        if (duration > 1000) {
                            val file = File(outputFile)
                            val uri = file.toUri()

                            val mDbRef = FirebaseDatabase.getInstance().getReference()
                            val senderRoom = senderUid + receiverUid
                            val receiverRoom = receiverUid + senderUid

                            val messageRef =
                                mDbRef.child("chats").child(senderRoom).child("messages").push()
                            val messageKey = messageRef.key!!

                            val placeholder = Message(
                                message = "",
                                sender = senderUid,
                                receiver = receiverUid,
                                status = "sending",
                                messageType = "voiceRecord",
                                messageKey = messageKey,
                                forwarded = false,
                                isReplyed = isReply,
                                replyToMessage = replyMessageKey,
                                replyMessage = replyMessage,
                                replySender = replySender,
                                fileName = ChatActivity().getFileNameFromUri(this@ChatActivity, uri),
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

                            messageRef.setValue(placeholder)
                            FirebaseDatabase.getInstance()
                                .getReference("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(messageKey)
                                .setValue(placeholder)

                            uploadMediaAndSendMessage(
                                fileUri = uri,
                                mediaType = "voiceRecord",
                                senderUid = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                receiverUid = receiverUid.toString(),
                                messageKey = messageKey
                            )

                            voiceRecordButton.visibility = View.VISIBLE
                            sendButton.visibility = View.GONE
                            voiceRecordMessageLayout.visibility = View.GONE
                            regularMessageLayout.visibility = View.VISIBLE
                            textMessageLayout.visibility = View.VISIBLE
                        }
                        else {
                        Toast.makeText(this, "Recording too short", Toast.LENGTH_SHORT).show()
                        }
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

        var receiverStatus = findViewById<TextView>(R.id.activeStatus)

        val receiverUserRef = FirebaseDatabase.getInstance().getReference("user")
            .child(receiverUid.toString())
            .child("activeChatUid")

        receiverUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(nameSnapshot: DataSnapshot) {
                val receiverStatusLocal = nameSnapshot.getValue(String::class.java)
                if (receiverStatus != null && receiverStatusLocal == "offline" ) {
                    receiverStatus.text = receiverStatusLocal
                } else if ((receiverStatus != null && receiverStatusLocal == "online")){
                    receiverStatus.text = receiverStatusLocal
                    MainActivity().updateSentToReceived()
                } else if (receiverStatus != null) {
                    receiverStatus.text = "active"
                    updateSenttoSeen()
                    updateReceivedToSeen()
                } else {
                    receiverStatus?.text = buildString {
                        append("offline")
                    }
                }
                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessageAdapter", "Failed to get receiver name: ${error.message}")
            }
        })

        FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom.toString()).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(nameSnapshot: DataSnapshot) {
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MessageAdapter-receiverRoom", "Failed to get receiver name: ${error.message}")
                }
        })

        FirebaseDatabase.getInstance().getReference("chats").child(senderRoom.toString()).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(nameSnapshot: DataSnapshot) {
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MessageAdapter-senderRoom", "Failed to get receiver name: ${error.message}")
                }
            })

        // Current Message Status of Receiver

        updateSenttoSeen()

        updateReceivedToSeen()

        viewModel.searchBarLayout.observe(this) { value ->
            searchBarLayout.visibility = value
        }

        viewModel.topBar.observe(this) { value ->
            topBar.visibility = value
        }

        val btnAttach = findViewById<ImageView>(R.id.btnAttach)

        btnAttach.setOnClickListener {
            showMediaPicker()
        }

        mediaPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            localActiveReceiver = receiverUid.toString()
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val clipData = data?.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        handleMultipleUri(uri)
                    }
                } else {
                    data?.data?.let { uri ->
                        handleMultipleUri(uri)
                    }
                }
            }
        }

        audioPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                handleAudioUri(uri)
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
                    handleMultipleUri(photoUri!!)
                }
            }
        }

        videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val videoUri = result.data!!.data
                val videoFile = getRealFileFromUri(videoUri!!)

                if (videoFile != null) {
                    handleMultipleUri(videoUri)
                } else {
                    Toast.makeText(this, "Invalid video file", Toast.LENGTH_SHORT).show()
                }
            }
        }

        menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        nameBar = findViewById<RelativeLayout>(R.id.nameBar_chat)

        nameBar.setOnClickListener {
            var reference = FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().currentUser!!.uid).child("activeChatUid")

            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ReceiverProfileActivity.Companion.currentReceiver=snapshot.value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChatActivity", "Error: ${error.message}")
                }

            })

            val intent = Intent(this@ChatActivity, ReceiverProfileActivity::class.java)
            startActivity(intent)
        }

        receiverProfile.setOnClickListener {
            var reference = FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().currentUser!!.uid).child("activeChatUid")

            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ReceiverProfileActivity.Companion.currentReceiver=snapshot.value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChatActivity", "Error: ${error.message}")
                }

            })

            val intent = Intent(this@ChatActivity, ReceiverProfileActivity::class.java)
            startActivity(intent)
        }

        close.setOnClickListener {
            searchEditText.setText("")
            close.visibility = View.GONE
        }

        searchEditText = findViewById<EditText>(R.id.searchEditText_chat)

        searchEditText.addTextChangedListener {
            if(searchEditText.text.isEmpty()){
                close.visibility = View.GONE
            }
            else{
                close.visibility = View.VISIBLE
            }
            fetchSearchedMessages(searchEditText.text.toString())
        }

        searchBarBack.setOnClickListener {
            viewModel.setSearchBarLayout(View.GONE)
            viewModel.setTopBar(View.VISIBLE)
            sendColumns.visibility = View.VISIBLE
            fetchMessages()
        }

        backNavigation.setOnClickListener {
            Log.d("MessageAdapter",MessageAdapter.IsMultipleSelect.toString())
            if(MessageAdapter.IsMultipleSelect){
                resetTopBarUI()
                messageAdapter.notifyDataSetChanged()
            }
            else {
                finish()
            }
        }

        deleteButton.setOnClickListener {
            val dialog = DeletePopUpFragment()
            val bundle = Bundle().apply {
                putString("person","chat")
            }
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "DeleteAction")
            messageAdapter.notifyDataSetChanged()
        }

        forwardButton.setOnClickListener {
            val intent = Intent(this@ChatActivity, UserAndGroupListActivity::class.java)
            intent.putExtra("receiverUid", receiverUid)
            intent.putExtra("person", "chats")
            startActivity(intent)
        }

        val replyToMessageKey = intent.getStringExtra("REPLY_TO_MESSAGE_KEY")
        if (!replyToMessageKey.isNullOrEmpty()) {
            fetchMessagesFromFirebase(replyToMessageKey)
        }

    }

    fun getRealFileFromUri(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("video_", ".mp4", cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
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

    fun createMediaFile(extension: String): File {
        val fileName = "media_${System.currentTimeMillis()}.$extension"
        return File(cacheDir, fileName)
    }
    private fun handleAudioUri(audioUri: Uri) {
        val messageRef = mDbRef.child("chats").child(senderRoom!!).child("messages").push()
        val messageKey = messageRef.key!!

        val placeholder = Message(
            message = "",
            sender = senderUid,
            receiver = receiverUid,
            status = "sending",
            messageType = "voiceRecord",
            messageKey = messageKey,
            forwarded = false,
            isReplyed = isReply,
            replyToMessage = replyMessageKey,
            replyMessage = replyMessage,
            replySender = replySender,
            fileName = ChatActivity().getFileNameFromUri(this@ChatActivity,audioUri),
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

        messageRef.setValue(placeholder).addOnSuccessListener {
            mDbRef.child("chats").child(receiverRoom!!).child("messages").child(messageKey)
                .setValue(placeholder)

            uploadMediaAndSendMessage(
                fileUri = audioUri,
                mediaType = "voiceRecord",
                senderUid = senderUid!!,
                receiverUid = receiverUid!!,
                messageKey = messageKey
            )
        }
    }

    fun replyUiChanges(message : String, sender : String, messageType : String, messageKey : String){

        isReply = true
        replyMessage = message
        replyMessageKey = messageKey
        replySender = sender

        Toast.makeText(this, "Reply Messages", Toast.LENGTH_SHORT).show()

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
    fun handleMultipleUri(uri: Uri) {
        val mimeType = contentResolver.getType(uri)
        val mediaType = when {
            mimeType?.startsWith("image") == true -> "image"
            mimeType?.startsWith("video") == true -> "video"
            else -> "text"
        }

        if (mediaType == "text") return

        val messageRef = mDbRef.child("chats").child(senderRoom!!).child("messages").push()
        val messageKey = messageRef.key!!

        val placeholder = Message(
            message = "",
            sender = senderUid,
            receiver = receiverUid,
            status = "sending",
            messageType = mediaType,
            messageKey = messageKey,
            forwarded = false,
            isReplyed = isReply,
            replyToMessage = replyMessageKey,
            replyMessage = replyMessage,
            replySender = replySender,
            fileName = ChatActivity().getFileNameFromUri(this@ChatActivity,uri),
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

        messageRef.setValue(placeholder).addOnSuccessListener {
            mDbRef.child("chats").child(receiverRoom!!).child("messages").child(messageKey)
                .setValue(placeholder)

            uploadMediaAndSendMessage(uri, mediaType, senderUid!!, receiverUid!!, messageKey)
        }
    }

    fun fetchSearchedMessages(text: String) {
        messageList.clear()
        mDbRef.child("chats").child(receiverRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postsnapshot in snapshot.children) {
                        val message = postsnapshot.getValue(Message::class.java)
                        if (message != null) {
                            val type = message.messageType?.lowercase() ?: "text"
                            val matches = when (type) {
                                "text", "image", "video" ->
                                    message.message?.contains(text, ignoreCase = true) == true
                                else ->
                                    message.fileName?.contains(text, ignoreCase = true) == true
                            }

                            if (matches) {
                                messageList.add(message)
                            }
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun fetchMessages(){
        messageList.clear()
        mDbRef.child("chats").child(receiverRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postsnapshot in snapshot.children) {
                        val message = postsnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageRecyclerView.scrollToPosition(messageList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }

    fun setArchieve(){
        val chatSendRef = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom!!)

        chatSendRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChild("archive")) {
                    mDbRef.child("chats").child(senderRoom!!).child("archive").setValue(false)
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
                    mDbRef.child("chats").child(receiverRoom!!).child("archive").setValue(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CheckArchive", "Error: ${error.message}")
            }
        })
    }

    override fun onBackPressed() {
        if(IsSearchBar){
            IsSearchBar=false
            viewModel.setSearchBarLayout(View.GONE)
            viewModel.setTopBar(View.VISIBLE)
            sendColumns.visibility = View.VISIBLE
            searchEditText.text.clear()
            fetchMessages()
        }
        else if(MessageAdapter.IsMultipleSelect){
            resetTopBarUI()
            messageAdapter.notifyDataSetChanged()
        }
        else {
            super.onBackPressed()
            finish()
        }
    }

    fun updateSenttoSeen(){

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val senderRef = FirebaseDatabase.getInstance().getReference("user").child(currentUserId.toString())

        senderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var current_receiver = snapshot.child("activeChatUid").value.toString()

                val receiverRef = FirebaseDatabase.getInstance().getReference("user").child(current_receiver)

                receiverRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var current_receiver_sender = snapshot.child("activeChatUid").value.toString()

                        if(current_receiver_sender == currentUserId){

                            var senderRoom : String = currentUserId + current_receiver
                            var receiverRoom : String = current_receiver +currentUserId

                            val chatRef_sender = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom).child("messages")
                            val chatRef_receiver = FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom).child("messages")

                            chatRef_sender.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (messageSnap in snapshot.children) {
                                        val message = messageSnap.getValue(Message::class.java)

                                        if (message?.sender == currentUserId && message.status == "sent") {
                                            messageSnap.ref.child("status").setValue("seen")
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Failed to mark as seen: ${error.message}")
                                }
                            })

                            chatRef_receiver.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (messageSnap in snapshot.children) {
                                        val message = messageSnap.getValue(Message::class.java)

                                        if (message?.sender == currentUserId && message.status == "sent") {
                                            messageSnap.ref.child("status").setValue("seen")
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Failed to mark as seen: ${error.message}")
                                }
                            })
                        }
                        else if(current_receiver_sender.equals("online")){

                            var senderRoom : String = currentUserId + current_receiver
                            var receiverRoom : String = current_receiver +currentUserId

                            val chatRef_sender = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom).child("messages")
                            val chatRef_receiver = FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom).child("messages")

                            chatRef_sender.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (messageSnap in snapshot.children) {
                                        val message = messageSnap.getValue(Message::class.java)
                                        if (message?.sender == currentUserId && message?.status == "sent") {
                                            Log.d("Equals-2","changed")
                                            messageSnap.ref.child("status").setValue("received")
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Failed to mark as seen: ${error.message}")
                                }
                            })

                            chatRef_receiver.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (messageSnap in snapshot.children) {
                                        val message = messageSnap.getValue(Message::class.java)
                                        Log.d("Inside-receiver", message?.status.toString())
                                        Log.d("Message-Sender",message?.sender.toString())
                                        Log.d("CurrentUser",currentUserId.toString())
                                        if (message?.sender == currentUserId && message?.status == "sent") {
                                            Log.d("Equals-2","changed")
                                            messageSnap.ref.child("status").setValue("received")
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Failed to mark as seen: ${error.message}")
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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
                    searchEditText.requestFocus()
                    close.visibility = View.GONE
                    viewModel.setTopBar(View.GONE)
                    sendColumns.visibility = View.GONE
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
                    IsSearchBar = true
                    true
                }
                R.id.clearChat -> {
                    val chatId = FirebaseAuth.getInstance().currentUser?.uid + receiverUid
                    val chatRef = FirebaseDatabase.getInstance().getReference("chats")
                        .child(chatId)

                    chatRef.removeValue().addOnSuccessListener {
                        Toast.makeText(this, "Message deleted successfully", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to delete messages: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    fun uploadMediaAndSendMessage(
        fileUri: Uri,
        mediaType: String,
        senderUid: String,
        receiverUid: String,
        messageKey: String,
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


        val inputStream = contentResolver.openInputStream(fileUri) ?: return
        val fileBytes = inputStream.readBytes()
        inputStream.close()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "chat_media_${System.currentTimeMillis()}.$extension",
                fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
            .addFormDataPart("fileName", "chat_media_${System.currentTimeMillis()}.$extension")
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
                        uploadMediaAndSendMessage(fileUri, mediaType, senderUid, receiverUid, messageKey, attempt + 1)
                    }, RETRY_DELAY_MS)
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "Upload failed after retries", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val jsonObject = JSONObject(body ?: "")
                val uploadedUrl = jsonObject.optString("url", null)
                if (uploadedUrl != null) {
                    runOnUiThread {
                        val senderRoom = senderUid + receiverUid
                        val receiverRoom = receiverUid + senderUid

                        var pagecount=""
                        var fileSize=""
                        var fileType=""

                        if(mediaType == "document"){
                            pagecount = ChatActivity().getPageCountFromUri(this@ChatActivity,fileUri).toString()
                            fileSize = ChatActivity().getFileSize(this@ChatActivity,fileUri)
                            fileType = ChatActivity().getFileType(this@ChatActivity,fileUri,getFileNameFromUri(this@ChatActivity,fileUri))
                        }

                        val message = Message(
                            message = uploadedUrl,
                            sender = senderUid,
                            receiver = receiverUid,
                            status = "sent",
                            messageType = mediaType,
                            messageKey = messageKey,
                            forwarded = false,
                            isReplyed = isReply,
                            replyToMessage = replyMessageKey,
                            replyMessage = replyMessage,
                            replySender = replySender,
                            fileName = ChatActivity().getFileNameFromUri(this@ChatActivity,fileUri),
                            fileSize = fileSize,
                            fileType = fileType,
                            pageCount = pagecount
                        )

                        if(isReply) {
                            isReply = false
                            replyMessageKey = ""
                            replyMessage = ""
                            replySender = ""
                        }

                        FirebaseDatabase.getInstance()
                            .getReference("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(messageKey)
                            .setValue(message)
                        FirebaseDatabase.getInstance()
                            .getReference("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(messageKey)
                            .setValue(message)

                        setArchieve()
                    }
                } else {
                    onFailure(call, IOException("No URL in ImageKit response"))
                }
            }
        })
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
                            this@ChatActivity,
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
    fun updateReceivedToSeen() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val chatRef_sender = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom!!).child("messages")
        val chatRef_receiver = FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom!!).child("messages")

        chatRef_sender.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (messageSnap in snapshot.children) {
                    val message = messageSnap.getValue(Message::class.java)

                    if (message?.sender != currentUserId && message?.status == "received") {
                        messageSnap.ref.child("status").setValue("seen")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to mark as seen: ${error.message}")
            }
        })

        chatRef_receiver.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (messageSnap in snapshot.children) {
                    val message = messageSnap.getValue(Message::class.java)

                    if (message?.sender != currentUserId && message?.status == "received") {
                        messageSnap.ref.child("status").setValue("seen")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to mark as seen: ${error.message}")
            }
        })
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

    private fun updateTimerText(seconds: Int) {
        val mins = seconds / 60
        val secs = seconds % 60
        timerTextView.text = String.format("%02d:%02d", mins, secs)
    }

    override fun onPause() {
        super.onPause()
        Log.d("state","Paused")
        FirebaseDatabase.getInstance().getReference("user")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("activeChatUid")
            .setValue("online")

    }

    override fun onResume() {
        super.onResume()
        Log.d("state","Resumed")
        FirebaseDatabase.getInstance().getReference("user")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("activeChatUid")
            .setValue(receiverUid)
        MessageAdapter.IsMultipleSelect=false
        SelectMultipleMessages.clear()
        messageAdapter.notifyDataSetChanged()
        resetTopBarUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("state","destroyed")
        FirebaseDatabase.getInstance().getReference("user")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("activeChatUid")
            .setValue("online")
        mediaRecorder?.release()
        timer?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    handleAudioUpload(uri)
                }
            } else if (data.data != null) {
                val uri = data.data!!
                handleAudioUpload(uri)
            }
        }

        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val documentUri = clipData.getItemAt(i).uri
                    handleDocumentUri(documentUri)
                }
            }
            else if (data.data != null) {
                val documentUri = data.data!!
                handleDocumentUri(documentUri)
            }
        }
    }

    private fun handleAudioUpload(audioUri: Uri) {
        val messageRef = mDbRef.child("chats").child(senderRoom!!).child("messages").push()
        val messageKey = messageRef.key!!

        val placeholder = Message(
            message = "",
            sender = senderUid,
            receiver = receiverUid,
            status = "sending",
            messageType = "audio",
            messageKey = messageKey,
            forwarded = false,
            isReplyed = isReply,
            replyToMessage = replyMessageKey,
            replyMessage = replyMessage,
            replySender = replySender,
            fileName = getFileNameFromUri(this, audioUri),
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

        messageRef.setValue(placeholder).addOnSuccessListener {
            mDbRef.child("chats").child(receiverRoom!!).child("messages").child(messageKey)
                .setValue(placeholder)

            uploadMediaAndSendMessage(
                fileUri = audioUri,
                mediaType = "audio",
                senderUid = senderUid!!,
                receiverUid = receiverUid!!,
                messageKey = messageKey
            )
        }
    }

    private fun handleDocumentUri(uri: Uri) {
        val messageRef = mDbRef.child("chats").child(senderRoom!!).child("messages").push()
        val messageKey = messageRef.key!!

        val fileName = ChatActivity().getFileNameFromUri(this, uri)

        val placeholder = Message(
            message = "",
            sender = senderUid,
            receiver = receiverUid,
            status = "sending",
            messageType = "document",
            messageKey = messageKey,
            forwarded = false,
            isReplyed = isReply,
            replyToMessage = replyMessageKey,
            replyMessage = replyMessage,
            replySender = replySender,
            fileName = fileName,
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

        messageRef.setValue(placeholder).addOnSuccessListener {
            mDbRef.child("chats").child(receiverRoom!!).child("messages").child(messageKey)
                .setValue(placeholder)

            uploadMediaAndSendMessage(
                fileUri = uri,
                mediaType = "document",
                senderUid = senderUid!!,
                receiverUid = receiverUid!!,
                messageKey = messageKey
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

    override fun onMultiSelectStarted() {
        Log.d("ProcessUi","true")
        processUi()
    }

    override fun onMultiSelectEnded() {
        resetTopBarUI()
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
        messageAdapter.notifyDataSetChanged()
    }

    override fun onUserClicked(position: Int): Boolean {
        val uid  = messageList[position].messageKey.toString()
        if(SelectMultipleMessages.contains(uid)){
            Log.d("OnUserClicked-if",SelectMultipleMessages.toString())
            SelectMultipleMessages.remove(uid)
            if(SelectMultipleMessages.size == 0){
                resetTopBarUI()
            }
            else {
                selectCount.text = SelectMultipleMessages.size.toString()
                messageAdapter.notifyDataSetChanged()
            }
            return true
        }
        else{
            Log.d("OnUserClicked-else",SelectMultipleMessages.toString())
            if(SelectMultipleMessages.size <=5 ){
                SelectMultipleMessages.add(uid)
                messageAdapter.notifyDataSetChanged()
                selectCount.text = SelectMultipleMessages.size.toString()
            }

            return false
        }
    }

    fun resetTopBarUI(){
        selectCount.text = "0"
        SelectMultipleMessages.clear()
        messageAdapter.notifyDataSetChanged()
        MessageAdapter.IsMultipleSelect = false
        ImageLayout.visibility = View.VISIBLE
        nameBar.visibility = View.VISIBLE
        selectCount.visibility = View.GONE
        deleteButton.visibility = View.GONE
        forwardButton.visibility = View.GONE
    }

    fun processUi(){
        Log.d("ProcessUi-inside","true")
        ImageLayout.visibility = View.INVISIBLE
        nameBar.visibility = View.INVISIBLE
        selectCount.visibility = View.VISIBLE
        backNavigation.visibility = View.VISIBLE
        deleteButton.visibility = View.VISIBLE
        forwardButton.visibility = View.VISIBLE
        messageAdapter.notifyDataSetChanged()
        selectCount.text = "1"
    }

    override fun onReplyClicked(replyToMessageKey: String) {

        val position = messageList.indexOfFirst { it.messageKey == replyToMessageKey }
        Log.d("Highlights",messageList.toString())
        Log.d("Highlights",replyToMessageKey)
        Log.d("Highlights",position.toString())
        if (position != -1) {
            highlightRepliedMessage(position)
        } else {
            Toast.makeText(this, "Message not found", Toast.LENGTH_SHORT).show()
        }
    }
    fun highlightRepliedMessage(position: Int) {
        messageRecyclerView.scrollToPosition(position)

        messageRecyclerView.post {
            val viewHolder = messageRecyclerView.findViewHolderForAdapterPosition(position)
            if (viewHolder != null) {
                val itemView = viewHolder.itemView
                val highlightColor = ContextCompat.getColor(this@ChatActivity, R.color.highlight_light)

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

    private fun fetchMessagesFromFirebase(replyToMessageKey: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("chats")
            .child(FirebaseAuth.getInstance().currentUser?.uid + receiverUid)
            .child("messages")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()

                for (snap in snapshot.children) {
                    val message = snap.getValue(Message::class.java)
                    message?.messageKey = snap.key
                    if (message != null) {
                        messageList.add(message)
                    }
                }

                messageAdapter.notifyDataSetChanged()

                val position = messageList.indexOfFirst { it.messageKey == replyToMessageKey }
                Log.d("Highlights", "Target Key: $replyToMessageKey at Position: $position")

                if (position != -1) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        highlighting(position)
                    }, 100)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Error fetching messages: ${error.message}")
            }
        })
    }

    private fun highlighting(position: Int) {
        messageRecyclerView.scrollToPosition(position)

        messageRecyclerView.doOnPreDraw {
            messageRecyclerView.post {
                val viewHolder = messageRecyclerView.findViewHolderForAdapterPosition(position)

                if (viewHolder != null) {
                    val highlightColor = ContextCompat.getColor(this, R.color.highlight_light)

                    when (viewHolder) {
                        is MessageAdapter.SentViewHolder -> {
                            val layout = viewHolder.itemView.findViewById<View>(R.id.sent_message_layout)
                            applyHighlight(layout, highlightColor)
                        }
                        is MessageAdapter.ReceiveViewHolder -> {
                            val layout = viewHolder.itemView.findViewById<View>(R.id.received_message_layout)
                            applyHighlight(layout, highlightColor)
                        }
                        is MessageAdapter.SendMessageReplyViewHolder -> {
                            val layout = viewHolder.itemView.findViewById<View>(R.id.sent_message_reply_layout)
                            applyHighlight(layout, highlightColor)
                        }
                        is MessageAdapter.ReceiveMessageReplyViewHolder -> {
                            val layout = viewHolder.itemView.findViewById<View>(R.id.received_message_reply_layout)
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


}