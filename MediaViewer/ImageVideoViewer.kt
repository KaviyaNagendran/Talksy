package com.example.android.myproject.MediaViewer

import android.app.DownloadManager
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.android.myproject.Chat_Page.ChatActivity.Companion.IsSearchBar
import com.example.android.myproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ImageVideoViewer : AppCompatActivity() {

    private lateinit var videoPlayer : VideoView
    private lateinit var imageViewer : ImageView
    private lateinit var senderName : TextView
    private lateinit var dateOfMessage : TextView
    private lateinit var timeOfMessage : TextView
    private lateinit var sourceUrl : String
    private lateinit var messageType : String

    private lateinit var backNavigation : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_video_viewer)

        videoPlayer = findViewById(R.id.videoPlayer)
        imageViewer = findViewById(R.id.imageViewer)
        senderName = findViewById(R.id.senderName)
        dateOfMessage = findViewById(R.id.dateOfMessage)
        timeOfMessage = findViewById(R.id.timeOfMessage)
        backNavigation = findViewById(R.id.BackNavigation_ImageVideo)

        backNavigation.visibility = View.VISIBLE

        backNavigation.setOnClickListener {
            finish()
        }

        sourceUrl = intent.getStringExtra("messageUrl").toString()
        messageType = intent.getStringExtra("messageType").toString()

        Log.d("VideoViewer", "Video URL: $sourceUrl")

        dateOfMessage.text = intent.getStringExtra("messageDate")
        timeOfMessage.text = intent.getStringExtra("messageTime")
        var messageKey = intent.getStringExtra("messageKey")

        if(messageType =="image"){
            imageViewer.visibility = View.VISIBLE
            videoPlayer.visibility = View.GONE
            Glide.with(this).load(sourceUrl).into(imageViewer)
        }
        else {
            videoPlayer.visibility = View.VISIBLE
            imageViewer.visibility = View.GONE

            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoPlayer)
            videoPlayer.setMediaController(mediaController)

            videoPlayer.setVideoURI(sourceUrl?.toUri())

            videoPlayer.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                videoPlayer.requestFocus()
                videoPlayer.start()
            }

            videoPlayer.setOnErrorListener { mp, what, extra ->
                Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show()
                true
            }
            videoPlayer.setOnInfoListener { mp, what, extra ->
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    Toast.makeText(this, "Buffering...", Toast.LENGTH_SHORT).show()
                }
                false
            }
            videoPlayer.setOnErrorListener { mp, what, extra ->
                Log.e("VideoViewError", "Error what=$what, extra=$extra")
                Toast.makeText(this, "Video error: $what", Toast.LENGTH_SHORT).show()
                true
            }


        }

        var senderUid = intent.getStringExtra("senderUid")

        if(senderUid== FirebaseAuth.getInstance().currentUser?.uid){
            senderName.text = "You"
        }
        else{
            FirebaseDatabase.getInstance().getReference().child("user").child(senderUid!!).get().addOnSuccessListener {
                senderName.text = it.child("name").value.toString()
            }
        }

        findViewById<ImageView>(R.id.chatMenuButton_imageViewer).setOnClickListener {
            showPopupMenu(it)
        }

    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.image_video_layout_options, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.savetogallery -> {
                    val mediaUrl = sourceUrl
                    val mediaType = messageType
                    val fileName = "MyMedia_${System.currentTimeMillis()}.${if (mediaType == "image") "jpg" else "mp4"}"

                    saveToGallery(mediaUrl, fileName, mediaType)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun saveToGallery(url: String, fileName: String, mediaType: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle("Downloading $fileName")
            request.setDescription("Saving to gallery...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            val mimeType = if (mediaType == "image") "image/*" else "video/*"
            request.setMimeType(mimeType)

            val folder = if (mediaType == "image") Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES
            request.setDestinationInExternalPublicDir(folder, fileName)

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

            Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


}