package com.example.android.myproject.Add_New_Chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Chat_Page.ChatActivity
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.example.android.myproject.MediaViewer.ProfileViewer
import com.google.android.material.imageview.ShapeableImageView

class AddChatAdapter(
    val context: Context,
    val contactList: ArrayList<User>,
    viewModel: AddNewChatActivityViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_add_each_chat_page, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentUser = contactList[position]
        holder as ContactViewHolder
        holder.contactName.text = currentUser.name
        holder.contactAbout.text = currentUser.about
        if(currentUser.profileUrl == ""){
            holder.contactProfile.setImageResource(R.drawable.default_profile_image)
        } else {
            Glide.with(context).load(currentUser.profileUrl).into(holder.contactProfile)
        }

        holder.contactProfile.setOnClickListener {
            if(currentUser.profileUrl == ""){
                Toast.makeText(context, "No profile image", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(context, ProfileViewer::class.java)
                intent.putExtra("profileUrl", currentUser.profileUrl)
                context.startActivity(intent)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", currentUser.name)
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("profileUrl", currentUser.profileUrl)
            context.startActivity(intent)
            (context as AddNewChatActivity).finish()
        }

    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName = itemView.findViewById<TextView>(R.id.contactName_contact)
        val contactAbout = itemView.findViewById<TextView>(R.id.contactAbout_contact)
        val contactProfile = itemView.findViewById<ShapeableImageView>(R.id.contact_profile_contact)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

}