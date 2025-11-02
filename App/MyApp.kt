package com.example.android.myproject.App

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.android.myproject.App.InternetLostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyApp : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        var currentActivity: FragmentActivity? = null
    }

    object NetworkUtil {
        var isOnline: Boolean = false
        var internetLostFragment: InternetLostFragment? = null

        fun registerNetworkCallback(context: Context) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkRequest = NetworkRequest.Builder().build()

            connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    isOnline = true
                    currentActivity?.runOnUiThread {
                        internetLostFragment?.dismissAllowingStateLoss()
                        internetLostFragment = null
                    }
                }

                override fun onLost(network: Network) {
                    isOnline = false
                    currentActivity?.runOnUiThread {
                        if (internetLostFragment == null) {
                            val fragment = InternetLostFragment()
                            internetLostFragment = fragment
                            currentActivity?.supportFragmentManager
                                ?.beginTransaction()
                                ?.add(fragment, "internet_lost")
                                ?.commitAllowingStateLoss()
                        }
                    }
                }
            })
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        NetworkUtil.registerNetworkCallback(this)
    }

    override fun onActivityStarted(activity: Activity) {
        activityReferences++
        if (activity is FragmentActivity) {
            currentActivity = activity
            if (!NetworkUtil.isOnline && NetworkUtil.internetLostFragment == null) {
                val fragment = InternetLostFragment()
                NetworkUtil.internetLostFragment = fragment
                activity.supportFragmentManager
                    .beginTransaction()
                    .add(fragment, "internet_lost")
                    .commitAllowingStateLoss()
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        activityReferences--
        if (activityReferences == 0) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseDatabase.getInstance().getReference("user")
                    .child(uid)
                    .child("activeChatUid")
                    .setValue("offline")
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is FragmentActivity) {
            currentActivity = activity
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity == currentActivity) {
            currentActivity = null
        }
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {}

    private var activityReferences = 0
}
