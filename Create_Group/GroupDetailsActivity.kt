package com.example.android.myproject.Create_Group

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.android.myproject.Group_Main.GroupMainActivity
import com.example.android.myproject.Listeners.MediaPickerFragment
import com.example.android.myproject.Listeners.MediaPickerListener
import com.example.android.myproject.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var profileImage : ShapeableImageView
    private lateinit var groupName : TextInputEditText
    private lateinit var memberCount : TextView
    private lateinit var groupAbout : TextInputEditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var mediaPickerLauncher: ActivityResultLauncher<Intent>
    private var profileUrl : String = ""
    private lateinit var back : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group_details)

        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()

        profileImage = findViewById<ShapeableImageView>(R.id.profileImage)
        groupName = findViewById<TextInputEditText>(R.id.groupName)
        memberCount = findViewById<TextView>(R.id.memberCount)
        groupAbout = findViewById<TextInputEditText>(R.id.groupAbout)

        val members = intent.getStringArrayListExtra("groupMembers") ?: arrayListOf()
        memberCount.text = members.size.toString()

        val groupCreateSubmitButton = findViewById<Button>(R.id.groupCreateSubmitButton)

        profileImage = findViewById<ShapeableImageView>(R.id.profileImage)

        profileImage.setOnClickListener {
            showMediaPicker()
        }

        back = findViewById<ImageView>(R.id.back_newGroup)
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }

        groupCreateSubmitButton.setOnClickListener {
            if(groupName.text.toString().isEmpty() || groupAbout.text.toString().isEmpty()) {
                Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show()
            } else {
                val creatorId = mAuth.currentUser?.uid.toString()
                GroupDB().createGroup(
                    groupName.text.toString(),
                    groupAbout.text.toString(),
                    members,
                    creatorId,
                    profileUrl
                )
                Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@GroupDetailsActivity, GroupMainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
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

    }

    fun handleUri(uri: Uri) {
        val mimeType = contentResolver.getType(uri)
        if (mimeType?.startsWith("image") == true) {
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
                    Toast.makeText(this@GroupDetailsActivity, "Upload failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jsonObject = JSONObject(responseBody ?: "")
                val uploadedUrl = jsonObject.optString("url", null)

                if (uploadedUrl != null) {
                    profileUrl = uploadedUrl
                    runOnUiThread {
                        Glide.with(this@GroupDetailsActivity)
                            .load(uploadedUrl)
                            .centerCrop()
                            .into(profileImage)
                    }
                } else {
                    Log.e("ImageKitError", "No URL in response: $jsonObject")
                }

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

}