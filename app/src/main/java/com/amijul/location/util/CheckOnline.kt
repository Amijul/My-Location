package com.amijul.location.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun Context.isOnline(): Boolean {
    val cm = getSystemService(ConnectivityManager::class.java)
    val nw = cm.activeNetwork ?: return false

    val caps = cm.getNetworkCapabilities(nw) ?: return false

    return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

}