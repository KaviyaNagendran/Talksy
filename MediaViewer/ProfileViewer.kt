package com.example.android.myproject.MediaViewer

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.android.myproject.R

class ProfileViewer : AppCompatActivity() {
    private lateinit var imageViewer : ImageView
    private lateinit var backNavigation : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_viewer)

        imageViewer = findViewById(R.id.imageViewer_profileView)
        backNavigation = findViewById(R.id.BackNavigation_ImageVideo_profileView)
        backNavigation.visibility = View.VISIBLE
        backNavigation.setOnClickListener {
            finish()
        }

        var sourceUrl = intent.getStringExtra("profileUrl")

        imageViewer.visibility = View.VISIBLE
        Glide.with(this).load(sourceUrl).into(imageViewer)
    }
}