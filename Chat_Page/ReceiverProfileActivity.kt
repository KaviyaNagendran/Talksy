package com.example.android.myproject.Chat_Page

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.android.myproject.Current_User_Profile.SenderProfileActivity
import com.example.android.myproject.MediaViewer.ProfileViewer
import com.example.android.myproject.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ReceiverProfileActivity : AppCompatActivity() {

    companion object {
        var currentReceiver: String = ""
        var profileUrl : String = ""
    }

    private lateinit var back : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile_receiver)

        var userName = findViewById<TextView>(R.id.username)
        var userEmail = findViewById<TextView>(R.id.email)
        var userAbout = findViewById<TextView>(R.id.about)
        var userProfile = findViewById<ShapeableImageView>(R.id.profile_image_layout)

        back = findViewById<ImageView>(R.id.back_receiverProfile)
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }

        var reference = FirebaseDatabase.getInstance().getReference("user").child(currentReceiver)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userName.text = snapshot.child("name").value.toString()
                userEmail.text = snapshot.child("email").value.toString()
                userAbout.text = snapshot.child("about").value.toString()
                Glide.with(this@ReceiverProfileActivity)
                    .load(snapshot.child("profileUrl").value.toString())
                    .placeholder(R.drawable.default_profile_image)
                    .into(userProfile)
                profileUrl = snapshot.child("profileUrl").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ReceiverProfileActivity", "Error: ${error.message}")
            }

        })

        userProfile.setOnClickListener {
            if(profileUrl == ""){
                Toast.makeText(this, "No profile image", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this@ReceiverProfileActivity, ProfileViewer::class.java)
                intent.putExtra("profileUrl", profileUrl)
                this.startActivity(intent)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        currentReceiver = ""
    }
}