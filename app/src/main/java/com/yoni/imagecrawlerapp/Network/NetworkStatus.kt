package com.yoni.imagecrawlerapp.Network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkStatus(private val mContext: Context) {

    private fun preAndroidMInternetCheck(connectivityManager: ConnectivityManager): Boolean {
        val activeNetwork = connectivityManager.activeNetworkInfo
        if (activeNetwork != null) {
            return (activeNetwork.type == ConnectivityManager.TYPE_WIFI ||
                    activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
        }
        return false
    }

    private fun postAndroidMInternetCheck(
        connectivityManager: ConnectivityManager
    ): Boolean {
        val network = connectivityManager.activeNetwork
        val connection =
            connectivityManager.getNetworkCapabilities(network)

        return connection != null && (
                connection.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        connection.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    fun isConnectionOn(): Boolean {
        val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            println("post")
            postAndroidMInternetCheck(connectivityManager)
        } else {
            println("pre")
            preAndroidMInternetCheck(connectivityManager)
        }
    }
}