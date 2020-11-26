package com.vrockk.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log

class ConnectionStateMonitor(
    val context: Context
) : ConnectivityManager.NetworkCallback() {

    val networkRequest: NetworkRequest

    init {
        networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()
    }

    fun enable() {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    fun disable() {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Log.d(TAG, "")
        checkCapabilities(context)

    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Log.d(TAG, "")
        checkCapabilities(context)
    }

    override fun onUnavailable() {
        super.onUnavailable()

        if (connectivityChangedListener != null)
            connectivityChangedListener!!.onNetworkConnectionChanged(false)
    }


    interface ConnectivityChangedListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        val TAG = "ConnectionStateMonitor"
        var connectivityChangedListener: ConnectivityChangedListener? = null

        fun checkCapabilities(context: Context): Boolean {
            var result: Boolean

            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            val activeNetwork = connectivityManager.activeNetwork
            result = if (activeNetwork != null) {
                val caps =
                    connectivityManager.getNetworkCapabilities(activeNetwork)
                if (caps != null) {
                    when {
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                } else
                    false
            } else
                false


            if (result) {
                Log.d(TAG, "")
            } else {
                Log.d(TAG, "")
            }
            if (connectivityChangedListener != null) {
                connectivityChangedListener!!.onNetworkConnectionChanged(result)
            }
            return result
        }
    }


}