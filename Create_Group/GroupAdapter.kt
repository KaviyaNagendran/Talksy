package com.example.android.myproject.Create_Group

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Entities.User
import com.example.android.myproject.Create_Group.GroupViewModel
import com.example.android.myproject.R

class GroupAdapter(val context: Context, val contactList: ArrayList<User>, private val viewModel : GroupViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_select_group_each_contact, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentUser = contactList[position]
        holder as GroupViewHolder

        holder.contactName.text = currentUser.name
        holder.contactAbout.text = currentUser.about
        holder.contactSelected.isVisible = viewModel.isMemberInGroup(currentUser.uid!!)
        if(currentUser.profileUrl!=""){
            Glide.with(context).load(currentUser.profileUrl).into(holder.contactProfile)
        }
        else{
            holder.contactProfile.setImageResource(R.drawable.default_profile_image)

        }

        holder.itemView.setOnClickListener {
            val uid = currentUser.uid!!
            if (viewModel.isMemberInGroup(uid)) {
                viewModel.removeGroupMember(uid)
            } else {
                viewModel.addGroupMember(uid)
            }
            notifyItemChanged(position)
        }
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName = itemView.findViewById<TextView>(R.id.contactName)
        val contactAbout = itemView.findViewById<TextView>(R.id.contactAbout)
        val contactSelected = itemView.findViewById<ImageView>(R.id.select)
        val contactProfile = itemView.findViewById<ImageView>(R.id.contactProfile)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

}