package com.example.android.myproject.GroupChat_Page

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Entities.User
import com.example.android.myproject.GroupChat_Page.GroupProfileActivity.Companion.noLongerUser
import com.example.android.myproject.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MemberListAdapter(
    private val context: Context,
    private val userList: ArrayList<User>,
    private val groupId: String,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<MemberListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_view_members_each, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){

        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

        Log.d("MemberListAdapter", "current user: $currentUser")

        if(userList[position].uid == currentUser) {
            holder.name.text = "You"
        }
        else{
            holder.name.text = userList[position].name
        }

        isAdmin(userList[position].uid.toString(), holder)

        holder.about.text = userList[position].about

        if(userList[position].profileUrl != "") {
            Glide.with(context).load(userList[position].profileUrl).into(holder.profileImage)
        }
        else{
            Glide.with(context).load(R.drawable.default_profile_image).into(holder.profileImage)
        }

        holder.itemView.setOnClickListener {

            if(noLongerUser){
                Toast.makeText(context,"You are no longer a member of this group", Toast.LENGTH_SHORT).show()
            }
            else {

                if (userList[position].uid != currentUser) {

                    val dialog = GroupMembersActionFragment()

                    val bundle = Bundle().apply {
                        putString("userId", userList[position].uid)
                        putString("userName", userList[position].name)
                        putString("profileUrl", userList[position].profileUrl)
                        putString("groupId", groupId)
                    }

                    dialog.arguments = bundle

                    dialog.show(fragmentManager, "GroupMemberAction")
                }
            }

        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.contactName_Member)
        val about = itemView.findViewById<TextView>(R.id.contactAbout_Member)
        val profileImage = itemView.findViewById<ShapeableImageView>(R.id.contact_profile_Member)
        val isAdmin = itemView.findViewById<TextView>(R.id.admin_notification)
    }

    fun isAdmin(userId: String, holder: ViewHolder) {
        val adminListRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(GroupProfileActivity.groupId)
            .child("groupDetails")
            .child(GroupProfileActivity.groupId)
            .child("adminList")

        adminListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adminList = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                if (adminList.contains(userId)) {
                    holder.isAdmin.visibility = View.VISIBLE
                } else {
                    holder.isAdmin.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("isAdmin", "Error checking admin list: ${error.message}")
            }
        })
    }

}