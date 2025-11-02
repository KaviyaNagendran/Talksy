package com.example.android.myproject.Forward_Message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.R
import com.google.android.material.imageview.ShapeableImageView

class GroupListAdapter(
    var context: Context,
    var groupList: ArrayList<Group>,
    var viewModel: UserAndGroupListActivityViewModel
) : RecyclerView.Adapter<GroupListAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupListAdapter.ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_contact_forward_each, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: GroupListAdapter.ViewHolder,
        position: Int
    ) {
        var contact = groupList[position]
        holder.name.text = contact.groupname
        holder.about.text = contact.groupabout

        if(contact.groupProfileImageUrl != "") {
            Glide.with(context).load(contact.groupProfileImageUrl).into(holder.profile)
        }

        if(viewModel.selectGroupForwardList.value.contains(contact)){
            holder.select.visibility = View.VISIBLE
        } else {
            holder.select.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if(viewModel.selectGroupForwardList.value.contains(contact)){
                viewModel.removeSelectGroupForward(contact)
            } else {
                viewModel.addSelectGroupForward(contact)
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var name = itemView.findViewById<TextView>(R.id.contactName_forwarded)
        var about = itemView.findViewById<TextView>(R.id.contactAbout_forwarded)
        var profile = itemView.findViewById<ShapeableImageView>(R.id.profileContact_forwarded)
        var select = itemView.findViewById<ImageView>(R.id.contactSelect_forwarded)
    }
}