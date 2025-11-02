package com.example.android.myproject.Archive_Group

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.myproject.Entities.Group
import com.example.android.myproject.Listeners.OnUserLongClickListener
import com.example.android.myproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArchiveGroupChatActivity : AppCompatActivity(), OnUserLongClickListener {
    private lateinit var groupRecyclerView: RecyclerView
    private lateinit var groupList: ArrayList<Group>
    private lateinit var adapter: ArchiveGroupChatAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var topBar : FrameLayout
    private lateinit var appText : TextView
    private lateinit var selectCount : TextView
    private lateinit var back_navigation : ImageView
    private lateinit var archive : ImageView
    private lateinit var delete : ImageView
    private lateinit var loading : LinearLayout
    private lateinit var noDataFound : LinearLayout
    private lateinit var menu : ImageView

    companion object{
        var selectList : ArrayList<String> = ArrayList()
        var IsLongPressed : Boolean = false
    }

    private val groupMessageListeners = mutableMapOf<String, ValueEventListener>()

    private lateinit var viewModel: ArchiveGroupChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive_groups)

        viewModel = ViewModelProvider(this)[ArchiveGroupChatViewModel::class.java]

        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()
        groupList = ArrayList()
        adapter = ArchiveGroupChatAdapter(this, groupList, viewModel)

        groupRecyclerView = findViewById(R.id.GroupRecyclerView_archive)
        groupRecyclerView.layoutManager = LinearLayoutManager(this)
        groupRecyclerView.adapter = adapter

        appText = findViewById(R.id.appText_archive_group)
        selectCount = findViewById(R.id.selectCount_archive_group)
        back_navigation = findViewById(R.id.back_navigation_archive_group)
        archive = findViewById(R.id.archive_out_group)
        delete = findViewById(R.id.delete_archive_group)
        topBar = findViewById(R.id.topBar_archive_group)
        menu = findViewById(R.id.menuButton_archive_group)

        loading = findViewById(R.id.progressBar_archive_group)
        noDataFound = findViewById(R.id.noDataFound_archive_group)
        loading.visibility=View.GONE
        noDataFound.visibility = View.GONE

        viewModel.isAppTextVisible.observe(this) { appText.visibility = it }
        viewModel.isSelectCountVisible.observe(this) { selectCount.visibility = it }
//        viewModel.isBackNavigationVisible.observe(this) { back_navigation.visibility = it }
        viewModel.isArchiveVisible.observe(this) { archive.visibility = it }
        viewModel.isDeleteVisible.observe(this) { delete.visibility = it }
        viewModel.groupCount.observe(this) { selectCount.text = it.toString() }
        viewModel.isTopBarVisible.observe(this) { topBar.visibility = it }
        viewModel.isLongPressed.observe(this) { IsLongPressed = it }

        viewModel.archivedGroupList.observe(this) {
            groupList.clear()
            groupList.addAll(it)
            adapter.notifyDataSetChanged()
            setupGroupMessageListeners()
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if(!currentUserId.isNullOrEmpty()) {
            fetchArchivedGroups(currentUserId)
        }

        back_navigation.setOnClickListener {
            if(archive.visibility == View.VISIBLE || delete.visibility == View.VISIBLE){
                viewModel.setSelectList(emptyList())
                viewModel.setIsLongPressed(false)
                resetUIWithViewModel()
                adapter.notifyDataSetChanged()
            }else{
                finish()
            }
        }

        delete.setOnClickListener {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

            val selectedGroups = viewModel.selectList.value ?: emptyList()
            for (groupId in selectedGroups) {
                val ref = FirebaseDatabase.getInstance().getReference("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(currentUid)

                ref.removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Group chat deleted", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.setSelectList(emptyList())
            viewModel.setIsLongPressed(false)
            resetUIWithViewModel()
            adapter.notifyDataSetChanged()
            fetchArchivedGroups(currentUid)
        }

        archive.setOnClickListener {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

            val selectedGroups = viewModel.selectList.value ?: emptyList()
            for (groupId in selectedGroups) {
                FirebaseDatabase.getInstance().getReference("groups")
                    .child(groupId)
                    .child("groupMessages")
                    .child(currentUid)
                    .child("archive")
                    .setValue(false)
            }

            viewModel.setSelectList(emptyList())
            viewModel.setIsLongPressed(false)
            resetUIWithViewModel()
            adapter.notifyDataSetChanged()
            fetchArchivedGroups(currentUid)
//            GroupMainActivity().setListenerForArchieveBar()
        }

        setupGroupMessageListeners()

        menu.setOnClickListener {
            showPopupMenu(it)
        }

    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.archive_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.unarchiveAll -> {
                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

                    val selectedGroups = viewModel.archivedGroupList.value ?: emptyList()
                    for (groupId in selectedGroups) {
                        FirebaseDatabase.getInstance().getReference("groups")
                            .child(groupId.groupuid)
                            .child("groupMessages")
                            .child(currentUid)
                            .child("archive")
                            .setValue(false)
                    }

                    viewModel.setArchivedGroupList(emptyList())
                    viewModel.setIsLongPressed(false)
                    resetUIWithViewModel()
                    adapter.notifyDataSetChanged()
                    fetchArchivedGroups(currentUid)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun setupGroupMessageListeners() {
        clearGroupMessageListeners()

        for (group in groupList) {
            val groupUid = group.groupuid ?: continue

            val messagesRef = FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupUid)
                .child("groupMessages")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val index = groupList.indexOfFirst { it.groupuid == groupUid }
                    if (index != -1) {
                        Log.d("GroupMainActivity", "Message updated for $groupUid at index $index")
                        adapter.notifyItemChanged(index)
                    } else {
                        Log.w("GroupMainActivity", "Group $groupUid not found in list")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GroupMainActivity", "Listener cancelled for $groupUid: ${error.message}")
                }
            }

            messagesRef.addValueEventListener(listener)
            groupMessageListeners[groupUid] = listener
        }
    }

    private fun clearGroupMessageListeners() {
        for ((groupUid, listener) in groupMessageListeners) {
            FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupUid)
                .child("groupMessages")
                .removeEventListener(listener)
        }
        groupMessageListeners.clear()
    }

    override fun onUserLongClicked(position: Int) {
        val uid = groupList[position].groupuid.toString()

        val currentList = viewModel.selectList.value?.toMutableList() ?: mutableListOf()
        if (!currentList.contains(uid)) {
            currentList.add(uid)
            viewModel.setGroupCount(currentList.size)
            viewModel.setSelectList(currentList)
        }

        val group = groupList[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val isUserInGroup = group.groupMembers.contains(currentUserId)

        if(isUserInGroup) {
            viewModel.setIsDeleteVisible(View.GONE)
        }
        else{
            viewModel.setIsDeleteVisible(View.VISIBLE)
        }

        viewModel.setIsLongPressed(true)
        viewModel.setIsAppTextVisible(View.INVISIBLE)
        viewModel.setIsSelectCountVisible(View.VISIBLE)
        viewModel.setIsArchiveVisible(View.VISIBLE)


    }


    override fun onUserClicked(position: Int): Boolean {
        val uid = groupList[position].groupuid.toString()
        val currentList = viewModel.selectList.value?.toMutableList() ?: mutableListOf()

        if (currentList.contains(uid)) {
            currentList.remove(uid)
            viewModel.setGroupCount(currentList.size)
            viewModel.setSelectList(currentList)

            if (currentList.isEmpty()) {
                viewModel.setIsLongPressed(false)
                resetUIWithViewModel()
            }

            adapter.notifyItemChanged(position)

            return true
        }
        return false
    }

    fun resetUIWithViewModel(){
        viewModel.setIsAppTextVisible(View.VISIBLE)
        viewModel.setIsSelectCountVisible(View.GONE)
//        viewModel.setIsBackNavigationVisible(View.GONE)
        viewModel.setIsArchiveVisible(View.GONE)
        viewModel.setIsDeleteVisible(View.GONE)
        viewModel.setIsTopBarVisible(View.VISIBLE)
        viewModel.setIsLongPressed(false)
    }

    fun fetchArchivedGroups(currentUserId: String) {
        loading.visibility = View.VISIBLE
        noDataFound.visibility = View.GONE
        groupRecyclerView.visibility = View.GONE

        clearGroupMessageListeners()
        val ref = FirebaseDatabase.getInstance().getReference("groups")
        val tempList = mutableListOf<Group>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalGroupsToCheck = 0
                var groupsChecked = 0

                for (groupSnap in snapshot.children) {
                    val groupDetailsSnap = groupSnap.child("groupDetails")
                    for (groupMemberSnap in groupDetailsSnap.children) {
                        val group = groupMemberSnap.getValue(Group::class.java)
                        if (group != null && group.groupMembers.contains(currentUserId)) {
                            totalGroupsToCheck++
                            val groupUid = groupSnap.key.toString()

                            FirebaseDatabase.getInstance()
                                .getReference("groups")
                                .child(groupUid)
                                .child("groupMessages")
                                .child(currentUserId)
                                .child("archive")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val isArchived = snapshot.getValue(Boolean::class.java) == true
                                        if (isArchived) {
                                            tempList.add(group)
                                        }
                                        groupsChecked++
                                        if (groupsChecked == totalGroupsToCheck) {
                                            finishGroupCheck(tempList)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        groupsChecked++
                                        if (groupsChecked == totalGroupsToCheck) {
                                            finishGroupCheck(tempList)
                                        }
                                    }
                                })
                        }
                    }
                }

                if (totalGroupsToCheck == 0) {
                    finishGroupCheck(tempList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                finishGroupCheck(emptyList())
            }
        })
    }

    private fun finishGroupCheck(groups: List<Group>) {
        viewModel.setArchivedGroupList(groups)

        if (groups.isEmpty()) {
            noDataFound.visibility = View.VISIBLE
            groupRecyclerView.visibility = View.GONE
        } else {
            noDataFound.visibility = View.GONE
            groupRecyclerView.visibility = View.VISIBLE
        }

        loading.visibility = View.GONE
    }


}