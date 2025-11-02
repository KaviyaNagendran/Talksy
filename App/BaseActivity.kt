package com.example.android.myproject.App

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.LinearLayout
import com.example.android.myproject.Chat_Main.MainActivity
import com.example.android.myproject.Current_User_Profile.SenderProfileActivity
import com.example.android.myproject.Group_Main.GroupMainActivity
import com.example.android.myproject.R

open class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        findViewById<LinearLayout>(R.id.chats)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.group)?.setOnClickListener {
            startActivity(Intent(this, GroupMainActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.profile)?.setOnClickListener {
            startActivity(Intent(this, SenderProfileActivity::class.java))
        }
    }
}
