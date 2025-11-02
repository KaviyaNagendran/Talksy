package com.example.android.myproject.Forward_Message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Entities.User
import com.example.android.myproject.R
import com.google.android.material.imageview.ShapeableImageView

class UserListAdapter(
    var context: Context,
    var userList: ArrayList<User>,
    var viewModel: UserAndGroupListActivityViewModel
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserListAdapter.ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_contact_forward_each, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UserListAdapter.ViewHolder,
        position: Int
    ) {
        var contact = userList[position]
        holder.name.text = contact.name
        holder.about.text = contact.about

        if(contact.profileUrl != "") {
            Glide.with(context).load(contact.profileUrl).into(holder.profile)
        }

        if(viewModel.selectUserForwardList.value.contains(contact)){
            holder.select.visibility = View.VISIBLE
        } else {
            holder.select.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if(viewModel.selectUserForwardList.value.contains(contact)){
                viewModel.removeSelectUserForward(contact)
            } else {
                viewModel.addSelectUserForward(contact)
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var name = itemView.findViewById<TextView>(R.id.contactName_forwarded)
        var about = itemView.findViewById<TextView>(R.id.contactAbout_forwarded)
        var profile = itemView.findViewById<ShapeableImageView>(R.id.profileContact_forwarded)
        var select = itemView.findViewById<ImageView>(R.id.contactSelect_forwarded)
    }

}