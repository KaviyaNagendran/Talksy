package com.example.android.myproject.GroupChat_Page

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.android.myproject.Chat_Page.ChatActivity
import com.example.android.myproject.Chat_Page.ReceiverProfileActivity
import com.example.android.myproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupMembersActionFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_group_member_action, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var userName = arguments?.getString("userName")
        var userId = arguments?.getString("userId")
        var profileUrl = arguments?.getString("profileUrl")
        var groupId = arguments?.getString("groupId")

        isAdmin(view)

        view.findViewById<TextView>(R.id.message_to_member).text = userName
        view.findViewById<TextView>(R.id.remove_member).text = userName
        view.findViewById<TextView>(R.id.view_member).text = userName

        val adminTextView = view.findViewById<TextView>(R.id.make_admin_member)

        val adminListRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(groupId!!)
            .child("groupDetails")
            .child(groupId)
            .child("adminList")

        adminListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adminList = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                if (adminList.contains(userId)) {
                    adminTextView.text = "Dismiss as Admin"
                } else {
                    adminTextView.text = "Make Group Admin"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                adminTextView.text = "Make Group Admin"
            }
        })

        view.findViewById<RelativeLayout>(R.id.MessageToMember).setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", userName)
            intent.putExtra("uid", userId)
            context?.startActivity(intent)
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.ViewMember).setOnClickListener {
            val intent =
                Intent(this@GroupMembersActionFragment.context, ReceiverProfileActivity::class.java)
            ReceiverProfileActivity.Companion.currentReceiver = userId.toString()
            startActivity(intent)

            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.MakeGroupAdmin).setOnClickListener {

            if(view.findViewById<TextView>(R.id.make_admin_member).text == "Dismiss as Admin"){

                GroupProfileActivity.removeAdmin(
                    userId.toString(), groupId.toString(),
                    onSuccess = {
                        view.findViewById<TextView>(R.id.make_admin_member).text = "Make Group Admin"
                        (activity as? GroupProfileActivity)?.fetchUsers()
                        Toast.makeText(context, "$userName is now an admin", Toast.LENGTH_SHORT)
                            .show()
                        dismiss()
                    },
                    onFailure = {
                        Toast.makeText(context, "Failed: $it", Toast.LENGTH_SHORT).show()
                    }
                )

            }
            else {

                GroupProfileActivity.addAdmin(
                    userId.toString(), groupId.toString(),
                    onSuccess = {
                        view.findViewById<TextView>(R.id.make_admin_member).text = "Dismiss as Admin"
                        (activity as? GroupProfileActivity)?.fetchUsers()
                        Toast.makeText(context, "$userName is now an admin", Toast.LENGTH_SHORT)
                            .show()
                        dismiss()
                    },
                    onFailure = {
                        Toast.makeText(context, "Failed: $it", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                )
            }
        }

        view.findViewById<RelativeLayout>(R.id.RemoveMember).setOnClickListener {
            GroupProfileActivity.removeFromGroup(
                userId = userId.toString(),
                groupId = groupId.toString(),
                onSuccess = {
                    (activity as? GroupProfileActivity)?.fetchUsers()
                    Toast.makeText(context, "$userName removed from group", Toast.LENGTH_SHORT).show()
                    dismiss()
                },
                onFailure = {
                    Toast.makeText(context, "Failed to remove: $it", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            )
        }
    }

    fun isAdmin(view : View){
        var currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val adminListRef = FirebaseDatabase.getInstance()
            .getReference("groups")
            .child(GroupProfileActivity.groupId)
            .child("groupDetails")
            .child(GroupProfileActivity.groupId)
            .child("adminList")

        adminListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adminList = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                if (adminList.contains(currentUser)) {
                    view.findViewById<RelativeLayout>(R.id.MakeGroupAdmin).visibility = View.VISIBLE
                    view.findViewById<RelativeLayout>(R.id.RemoveMember).visibility = View.VISIBLE
                } else {
                    view.findViewById<RelativeLayout>(R.id.MakeGroupAdmin).visibility = View.GONE
                    view.findViewById<RelativeLayout>(R.id.RemoveMember).visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("isAdmin", "Error checking admin list: ${error.message}")
            }
        })
    }

}