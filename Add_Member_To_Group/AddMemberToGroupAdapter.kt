package com.example.android.myproject.Add_Member_To_Group

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Add_Member_To_Group.AddMemberToGroupViewModel
import com.example.android.myproject.R
import com.example.android.myproject.Entities.User
import com.google.android.material.imageview.ShapeableImageView

class AddMemberToGroupAdapter(val context: Context, val contactList: ArrayList<User>, private val viewModel: AddMemberToGroupViewModel) : RecyclerView.Adapter<AddMemberToGroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_add_member_to_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val currentUser = contactList[position]

        holder.contactName.text = currentUser.name

        if(currentUser.profileUrl == ""){
            holder.profileImage.setImageResource(R.drawable.default_profile_image)
        }else {
            Glide.with(context).load(currentUser.profileUrl).into(holder.profileImage)
        }

        if(isAlreadyMember(currentUser.uid!!)) {

            holder.contactName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.light_grey)
            )

            holder.contactAbout.visibility = View.GONE
            holder.alreadyAdded.visibility = View.VISIBLE

        }
        else{

            holder.contactAbout.visibility = View.VISIBLE
            holder.alreadyAdded.visibility = View.GONE

            holder.contactAbout.text = currentUser.about
            holder.contactSelected.isVisible = viewModel.isMemberInGroup(currentUser.uid!!)

        }

        holder.itemView.setOnClickListener {

            if(isAlreadyMember(currentUser.uid!!)){
                Toast.makeText(context, "Already a member", Toast.LENGTH_SHORT).show()
            }
            else {

                val uid = currentUser.uid!!
                if (viewModel.isMemberInGroup(uid)) {
                    viewModel.removeGroupMember(uid)
                } else {
                    viewModel.addGroupMember(uid)
                }
                notifyItemChanged(position)
            }
        }

    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName = itemView.findViewById<TextView>(R.id.contactName_addGroup)
        val contactAbout = itemView.findViewById<TextView>(R.id.contactAbout_addGroup)
        val contactSelected = itemView.findViewById<ImageView>(R.id.select_addGroup)
        val profileImage = itemView.findViewById<ShapeableImageView>(R.id.image_layout_addGroup)
        val alreadyAdded = itemView.findViewById<TextView>(R.id.alreadyAdded_addGroup)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    fun isAlreadyMember(uid: String): Boolean {
        return AddMemberToGroupActivity.existingMembers.contains(uid)
    }

}