package com.example.android.myproject.Listeners

interface OnUserLongClickListener {
    fun onUserLongClicked(position: Int)
    fun onUserClicked(position: Int) : Boolean
}