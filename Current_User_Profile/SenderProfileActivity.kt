package com.example.android.myproject.Current_User_Profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.android.myproject.Listeners.MediaPickerFragment
import com.example.android.myproject.Listeners.MediaPickerListener
import com.example.android.myproject.MediaViewer.ProfileViewer
import com.example.android.myproject.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SenderProfileActivity : AppCompatActivity() {

    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userAbout: TextView
    private lateinit var usernameEdit: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var usernameSubmitBtn: ImageView
    private lateinit var aboutEdit: ImageView
    private lateinit var aboutEditText: EditText
    private lateinit var aboutSubmitBtn: ImageView
    private lateinit var profileImageEdit: TextView
    private lateinit var profileImage: ShapeableImageView
    private lateinit var profileSubmitBtn: ImageView
    private lateinit var progressBar : ProgressBar
    private lateinit var mediaPickerLauncher: ActivityResultLauncher<Intent>
    private val viewModel: SenderProfileViewModel by viewModels()
    private lateinit var back : ImageView

    companion object {
        private const val KEY_IS_EDITING_NAME = "is_editing_name"
        private const val KEY_IS_EDITING_ABOUT = "is_editing_about"
        private const val KEY_NAME_TEXT = "editing_name_text"
        private const val KEY_ABOUT_TEXT = "editing_about_text"
        private var oldUrl: String = ""
        private var newUrl : String = ""
        var profileUrl : String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile_sender)

        back = findViewById<ImageView>(R.id.back_senderProfile)
        back.visibility = View.VISIBLE

        back.setOnClickListener {
            finish()
        }

        userName = findViewById(R.id.username)
        userEmail = findViewById(R.id.email)
        userAbout = findViewById(R.id.about)

        usernameEdit = findViewById(R.id.profile_name_edit)
        usernameEditText = findViewById(R.id.username_edittext)
        usernameSubmitBtn = findViewById(R.id.submitUser)

        aboutEdit = findViewById(R.id.profile_about_edit)
        aboutEditText = findViewById(R.id.about_edittext)
        aboutSubmitBtn = findViewById(R.id.submitAbout)

        profileImageEdit = findViewById(R.id.profile_image_edit)
        profileImage = findViewById<ShapeableImageView>(R.id.profile_image)

        progressBar = findViewById<ProgressBar>(R.id.progressBar_profile)

        if (savedInstanceState != null) {
            val isEditingName = savedInstanceState.getBoolean(KEY_IS_EDITING_NAME, false)
            val isEditingAbout = savedInstanceState.getBoolean(KEY_IS_EDITING_ABOUT, false)

            if (isEditingName) {
                usernameEditText.visibility = View.VISIBLE
                usernameSubmitBtn.visibility = View.VISIBLE
                usernameEditText.setText(savedInstanceState.getString(KEY_NAME_TEXT, ""))
                userName.visibility = View.GONE
            }

            if (isEditingAbout) {
                aboutEditText.visibility = View.VISIBLE
                aboutSubmitBtn.visibility = View.VISIBLE
                aboutEditText.setText(savedInstanceState.getString(KEY_ABOUT_TEXT, ""))
                userAbout.visibility = View.GONE
            }
        }

        viewModel.userName.observe(this, Observer { name ->
            userName.text = name
        })

        viewModel.userEmail.observe(this, Observer { email ->
            userEmail.text = email
        })

        viewModel.userAbout.observe(this, Observer { about ->
            userAbout.text = about
        })

        viewModel.profileUrl.observe(this, Observer { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this)
                    .load(url)
                    .centerCrop()
                    .into(profileImage)
            }
        })

        viewModel.error.observe(this, Observer { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        })

        usernameEdit.setOnClickListener {
            usernameEditText.visibility = View.VISIBLE
            usernameSubmitBtn.visibility = View.VISIBLE
            usernameEditText.setText(userName.text)
            usernameEditText.requestFocus()
            usernameEdit.visibility = View.GONE
            userName.visibility = View.GONE
        }

        usernameSubmitBtn.setOnClickListener {
            val newName = usernameEditText.text.toString()
            if(newName.isNotEmpty()) {
                viewModel.updateUserName(newName) { success ->
                    if (success) {
                        usernameEdit.visibility = View.VISIBLE
                        userName.visibility = View.VISIBLE
                        usernameEditText.visibility = View.GONE
                        usernameSubmitBtn.visibility = View.GONE
                        Toast.makeText(this, "Username Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update username", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                Toast.makeText(this, "UserName should not be Empty!!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        aboutEdit.setOnClickListener {
            aboutEditText.visibility = View.VISIBLE
            aboutSubmitBtn.visibility = View.VISIBLE
            aboutEditText.setText(userAbout.text)
            aboutEditText.requestFocus()
            aboutEdit.visibility = View.GONE
            userAbout.visibility = View.GONE
        }

        aboutSubmitBtn.setOnClickListener {
            val newAbout = aboutEditText.text.toString()
            if(newAbout.isNotEmpty()) {
                viewModel.updateUserAbout(newAbout) { success ->
                    if (success) {
                        aboutEdit.visibility = View.VISIBLE
                        userAbout.visibility = View.VISIBLE
                        aboutEditText.visibility = View.GONE
                        aboutSubmitBtn.visibility = View.GONE
                        Toast.makeText(this, "About Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update about info", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            else{
                Toast.makeText(this, "About should not be Empty!!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        profileImageEdit.setOnClickListener {
            showMediaPicker()
        }

        profileImage.setOnClickListener {
            if(profileUrl == ""){
                Toast.makeText(this, "No profile image", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this@SenderProfileActivity, ProfileViewer::class.java)
                intent.putExtra("profileUrl", profileUrl)
                this.startActivity(intent)
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

        if(usernameEditText.visibility == View.VISIBLE){
            usernameEditText.visibility = View.VISIBLE
            usernameSubmitBtn.visibility = View.VISIBLE
            usernameEdit.visibility = View.GONE
            userName.visibility = View.GONE
        }

        if(aboutEditText.visibility == View.VISIBLE){
            aboutEditText.visibility = View.VISIBLE
            aboutSubmitBtn.visibility = View.VISIBLE
            aboutEdit.visibility = View.GONE
            userAbout.visibility = View.GONE
        }

    }

    private fun showMediaPicker() {
        val picker = MediaPickerFragment(object : MediaPickerListener {
            override fun onMediaSelected() {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    putExtra("ProfileActivity","Yes")
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
                    Toast.makeText(this@SenderProfileActivity, "Upload failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jsonObject = JSONObject(responseBody ?: "")
                val uploadedUrl = jsonObject.optString("url", null)

                if (uploadedUrl != null) {
                    newUrl = uploadedUrl
                    runOnUiThread {
                        viewModel.updateUserProfile(newUrl) { success ->
                            if (success) {
                                progressBar.visibility = View.GONE
                                Toast.makeText(this@SenderProfileActivity, "Profile Updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SenderProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                else {
                    Log.e("ImageKitError", "No URL in response: $jsonObject")
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_EDITING_NAME, usernameEditText.visibility == View.VISIBLE)
        outState.putBoolean(KEY_IS_EDITING_ABOUT, aboutEditText.visibility == View.VISIBLE)
        outState.putString(KEY_NAME_TEXT, usernameEditText.text.toString())
        outState.putString(KEY_ABOUT_TEXT, aboutEditText.text.toString())
    }

}