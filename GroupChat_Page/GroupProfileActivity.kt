package com.example.android.myproject.GroupChat_Page

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Add_Member_To_Group.AddMemberToGroupActivity
import com.example.android.myproject.DeleteAction.DeleteActionFragment
import com.example.android.myproject.DeleteAction.ExitGroupFragment
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.GroupChat_Page.GroupProfileViewModel
import com.example.android.myproject.Listeners.MediaPickerFragment
import com.example.android.myproject.Listeners.MediaPickerListener
import com.example.android.myproject.GroupChat_Page.MemberListAdapter
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.example.android.myproject.Listeners.CallBackListenerMessageSearch
import com.example.android.myproject.MediaViewer.ProfileViewer
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class GroupProfileActivity : AppCompatActivity() {

    private lateinit var userList : ArrayList<User>
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var memberListAdapter: MemberListAdapter
    private var IsSeeAll : Boolean = false
    private lateinit var toggle_button : TextView
    private lateinit var aboutEditText : EditText
    private lateinit var aboutTextEdit : ImageView
    private lateinit var about_view : TextView
    private lateinit var aboutSubmit : ImageView
    private lateinit var profileImage : ShapeableImageView
    private lateinit var profileImageEdit : TextView
    private lateinit var addMembers : FrameLayout
    private lateinit var exitGroup : FrameLayout
    private lateinit var progressBar : ProgressBar

    private lateinit var mediaPickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var viewModel: GroupProfileViewModel
    private lateinit var back : ImageView

    private var profileUrl : String = ""

    companion object{

        lateinit var groupId : String

        var noLongerUser : Boolean = false

        fun addAdmin(userId: String, groupId: String, onSuccess: (() -> Unit)? = null, onFailure: ((String) -> Unit)? = null) {
            FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupId)
                .child("groupDetails")
                .child(groupId)
                .child("adminList")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existingList = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                        val mutableList = existingList.toMutableList()
                        if (!mutableList.contains(userId)) {
                            mutableList.add(userId)
                            FirebaseDatabase.getInstance()
                                .getReference("groups")
                                .child(groupId)
                                .child("groupDetails")
                                .child(groupId)
                                .child("adminList")
                                .setValue(mutableList)
                                .addOnSuccessListener {
                                    onSuccess?.invoke()
                                }
                                .addOnFailureListener {
                                    onFailure?.invoke(it.message ?: "Unknown error")
                                }
                        } else {
                            onFailure?.invoke("User is already an admin")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onFailure?.invoke(error.message)
                    }
                })
        }

        fun removeAdmin(userId: String, groupId: String, onSuccess: (() -> Unit)? = null, onFailure: ((String) -> Unit)? = null) {
            FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupId)
                .child("groupDetails")
                .child(groupId)
                .child("adminList")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existingList = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                        val mutableList = existingList.toMutableList()
                        if (mutableList.contains(userId)) {
                            mutableList.remove(userId)
                            FirebaseDatabase.getInstance()
                                .getReference("groups")
                                .child(groupId)
                                .child("groupDetails")
                                .child(groupId)
                                .child("adminList")
                                .setValue(mutableList)
                                .addOnSuccessListener {
                                    onSuccess?.invoke()
                                }
                                .addOnFailureListener {
                                    onFailure?.invoke(it.message ?: "Unknown error")
                                }
                        } else {
                            onFailure?.invoke("User is already not an admin")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onFailure?.invoke(error.message)
                    }
                })
        }

        fun removeFromGroup(
            userId: String,
            groupId: String,
            onSuccess: (() -> Unit)? = null,
            onFailure: ((String) -> Unit)? = null
        ) {
            val groupRef = FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupId)
                .child("groupDetails")
                .child(groupId)

            groupRef.child("adminList")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(adminSnap: DataSnapshot) {
                        val adminList = adminSnap.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                        val isCurrentUserOnlyAdmin = adminList.size == 1 && adminList.contains(userId)

                        groupRef.child("groupMembers")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(memberSnap: DataSnapshot) {
                                    val memberList = memberSnap.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()

                                    if (!memberList.contains(userId)) {
                                        onFailure?.invoke("User not found in groupMembers")
                                        return
                                    }

                                    memberList.remove(userId)

                                    if (isCurrentUserOnlyAdmin) {
                                        adminList.remove(userId)

                                        val newAdmin = memberList.firstOrNull()

                                        if (newAdmin != null) {
                                            adminList.add(newAdmin)
                                        }
                                    } else {
                                        adminList.remove(userId)
                                    }

                                    groupRef.child("groupMembers").setValue(memberList)
                                        .addOnSuccessListener {
                                            groupRef.child("adminList").setValue(adminList)
                                                .addOnSuccessListener { onSuccess?.invoke() }
                                                .addOnFailureListener { onFailure?.invoke(it.message ?: "Failed updating adminList") }
                                        }
                                        .addOnFailureListener { onFailure?.invoke(it.message ?: "Failed updating groupMembers") }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    onFailure?.invoke(error.message)
                                }
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onFailure?.invoke(error.message)
                    }
                })
            addPastMember(userId)
        }

        fun addPastMember(userId: String) {
            val pastRef = FirebaseDatabase.getInstance().getReference("groups")
                .child(groupId)
                .child("groupDetails")
                .child(groupId)
                .child("pastMembersList")

            pastRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pastMembers = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                    if (!pastMembers.contains(userId)) {
                        pastMembers.add(userId)
                        pastRef.setValue(pastMembers)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("GroupProfileActivity", "Error adding past member: $error")
                }
            })
        }

        fun removePastMember(userId: String) {
            val pastRef = FirebaseDatabase.getInstance().getReference("groups")
                .child(groupId)
                .child("groupDetails")
                .child(groupId)
                .child("pastMembersList")

            pastRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pastMembers = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                    if (pastMembers.remove(userId)) {
                        pastRef.setValue(pastMembers)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("GroupProfileActivity", "Error removing past member: $error")
                }
            })
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_profile_layout)

        userRecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        userList = ArrayList()

        groupId = intent.getStringExtra("groupID").toString()
        noLongerUser = intent.getBooleanExtra("NoLongerUser", false)

        Log.d("NoLong",noLongerUser.toString())

        memberListAdapter = MemberListAdapter(this, userList, groupId, supportFragmentManager)

        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = memberListAdapter

        viewModel = ViewModelProvider(this)[GroupProfileViewModel::class.java]

        viewModel.userList.observe(this) { users ->
            userList.clear()
            userList.addAll(users)
            memberListAdapter.notifyDataSetChanged()
        }

        viewModel.fetchGroupMembers(groupId, IsSeeAll)

        profileImage = findViewById<ShapeableImageView>(R.id.profile_image_view)
        profileImageEdit = findViewById<TextView>(R.id.profile_image_edit_view)
        addMembers = findViewById<FrameLayout>(R.id.add_members)
        exitGroup = findViewById<FrameLayout>(R.id.exit_group)
        back = findViewById<ImageView>(R.id.back_groupProfile)
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }

        FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupDetails")
            .child(groupId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val group = snapshot.getValue(Group::class.java)
                    if (group != null) {
                        findViewById<TextView>(R.id.groupname_view).text = group.groupname
                        findViewById<TextView>(R.id.memberCount_view).text = group.groupMembers.size.toString()
                        findViewById<TextView>(R.id.about_view).text = group.groupabout
                        if (group.groupProfileImageUrl.isNotBlank()) {
                            profileUrl = group.groupProfileImageUrl
                            Glide.with(this@GroupProfileActivity).load(group.groupProfileImageUrl).into(profileImage)
                        } else {
                            profileImage.setImageResource(R.drawable.default_profile_image)
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GroupProfileActivity", "Failed to fetch group: ${error.message}")
                }
            })

        toggle_button = findViewById<TextView>(R.id.toggle_button)
        toggle_button.visibility = View.VISIBLE

        toggle_button.setOnClickListener {
            IsSeeAll = !IsSeeAll
            toggle_button.text = if (IsSeeAll) "See less" else "See more"
            fetchUsers()

            userRecyclerView.post {
                userRecyclerView.requestLayout()
            }
        }

        aboutEditText = findViewById<EditText>(R.id.about_edittext_view)
        aboutTextEdit = findViewById<ImageView>(R.id.profile_about_edit_view)
        about_view = findViewById<TextView>(R.id.about_view)
        aboutSubmit = findViewById<ImageView>(R.id.submitAbout_view)
        progressBar = findViewById<ProgressBar>(R.id.progressBar_profile_group)

        aboutTextEdit.setOnClickListener {
            if(noLongerUser){
                Toast.makeText(this,"You are no longer a member of this group", Toast.LENGTH_SHORT).show()
            }
            else {
                aboutTextEdit.visibility = View.GONE
                aboutSubmit.visibility = View.VISIBLE
                about_view.visibility = View.GONE
                aboutEditText.setText(about_view.text)
                aboutEditText.requestFocus()
                aboutEditText.visibility = View.VISIBLE
            }
        }

        aboutEditText.filters = arrayOf(InputFilter.LengthFilter(150))

        aboutEditText.addTextChangedListener{
            if(aboutEditText.text.length==150){
                Toast.makeText(this@GroupProfileActivity,"About length cannot exceed 150 characters", Toast.LENGTH_SHORT).show()
            }
        }

        aboutSubmit.setOnClickListener {
            aboutTextEdit.visibility = View.VISIBLE
            aboutSubmit.visibility = View.GONE
            aboutEditText.visibility = View.GONE
            val newAbout = aboutEditText.text.toString()
            about_view.text = newAbout
            about_view.visibility = View.VISIBLE

            FirebaseDatabase.getInstance().getReference("groups")
                .child(groupId)
                .child("groupDetails")
                .child(groupId)
                .child("groupabout")
                .setValue(newAbout)
        }

        profileImage.setOnClickListener {
            if(profileUrl==""){
                Toast.makeText(this@GroupProfileActivity, "No profile image", Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(this@GroupProfileActivity, ProfileViewer::class.java)
                intent.putExtra("profileUrl", profileUrl)
                startActivity(intent)
            }
        }

        profileImageEdit.setOnClickListener {
            if(noLongerUser){
                Toast.makeText(this,"You are no longer a member of this group", Toast.LENGTH_SHORT).show()
            }
            else {
                showMediaPicker()
            }
        }

        mediaPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    val clipData = data?.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            handleUri(uri)
                        }
                    } else {
                        data?.data?.let { handleUri(it) }
                    }
                }
            }

        addMembers.setOnClickListener {
            if(noLongerUser){
                Toast.makeText(this,"You are no longer a member of this group", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this@GroupProfileActivity, AddMemberToGroupActivity::class.java)
                intent.putExtra("groupId", groupId)
                startActivity(intent)
                finish()
            }
        }

        exitGroup.setOnClickListener {
            if(noLongerUser){
                Toast.makeText(this,"You are no longer a member of this group", Toast.LENGTH_SHORT).show()
            }
            else {
                val dialog = ExitGroupFragment.newInstance()
                dialog.show(supportFragmentManager, "DeleteAction")
            }
        }
    }

    fun fetchUsers() {
        val groupMembersRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId)
            .child("groupDetails")
            .child(groupId)
            .child("groupMembers")

        groupMembersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                val memberIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                if(memberIds.size <=3 ){
                    toggle_button.visibility = View.GONE
                }
                else{
                    toggle_button.visibility = View.VISIBLE
                }
                val limitedIds = if (IsSeeAll) memberIds else memberIds.take(3)

                var loadedCount = 0
                for (userId in limitedIds) {
                    FirebaseDatabase.getInstance().getReference("user").child(userId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(User::class.java)
                                if (user != null) {
                                    userList.add(user)
                                    Log.d("GroupProfileActivity", "User added: ${user.name}")
                                }
                                loadedCount++
                                if (loadedCount == limitedIds.size) {
                                    memberListAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("GroupProfileActivity", "Error loading user: $userId")
                            }
                        })
                }

                if (limitedIds.isEmpty()) {
                    memberListAdapter.notifyDataSetChanged()
                    toggle_button.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GroupProfileActivity", "Error loading group members: ${error.message}")
            }
        })

    }

    private fun showMediaPicker() {
        val picker = MediaPickerFragment(object : MediaPickerListener {
            override fun onMediaSelected() {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                }
                mediaPickerLauncher.launch(Intent.createChooser(intent, "Select profile image"))
            }

            override fun onCameraProcess() {
                TODO("Not yet implemented")
            }

            override fun onDocumentSelect() {
                TODO("Not yet implemented")
            }

            override fun onAudioSelect() {
                TODO("Not yet implemented")
            }
        })
        val bundle = Bundle().apply {
            putString("ProfileActivity", "Yes")
        }
        picker.arguments = bundle
        picker.show(supportFragmentManager, "MediaPicker")
    }

    fun handleUri(uri: Uri) {
        val mimeType = contentResolver.getType(uri)
        if (mimeType?.startsWith("image") == true) {
            progressBar.visibility = View.VISIBLE
            uploadProfileImage(uri, FirebaseAuth.getInstance().currentUser?.uid.toString())
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadProfileImage(fileUri: Uri, userUid: String) {
        val contentResolver = contentResolver

        val mimeType = contentResolver.getType(fileUri) ?: return
        if (!mimeType.startsWith("image")) {
            Toast.makeText(this, "Please select a valid image", Toast.LENGTH_SHORT).show()
            return
        }

        val fileBytes = contentResolver.openInputStream(fileUri)?.use { it.readBytes() } ?: return
        val fileBase64 = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
        val base64Prefix = "data:$mimeType;base64,"

        val requestBody = FormBody.Builder()
            .add("file", "$base64Prefix$fileBase64")
            .add("fileName", "profile_${System.currentTimeMillis()}.jpg")
            .build()

        val privateKey = "private_HuUA3m3WeeBImajVyqgJaDzCSyY="
        val credentials = Credentials.basic(privateKey, "")

        val request = Request.Builder()
            .url("https://upload.imagekit.io/api/v1/files/upload")
            .addHeader("Authorization", credentials)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@GroupProfileActivity, "Upload failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jsonObject = JSONObject(responseBody ?: "")
                val uploadedUrl = jsonObject.optString("url", null)

                if (uploadedUrl != null) {
                    runOnUiThread {
                        Glide.with(this@GroupProfileActivity).load(uploadedUrl).into(profileImage)
                        FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("groupDetails")
                            .child(groupId).child("groupProfileImageUrl").setValue(uploadedUrl)
                        progressBar.visibility = View.GONE
                    }

                }

            }
        })
    }

}