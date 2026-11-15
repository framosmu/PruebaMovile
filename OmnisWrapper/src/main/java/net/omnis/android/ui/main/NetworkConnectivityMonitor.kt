package net.omnis.android.ui.main

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkConnectivityMonitor(
	private val context: Context,
	private val onNetworkLost: () -> Unit,
	private val onNetworkAvailable: () -> Unit
) {

	private val connectivityManager =
		context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

	private val networkCallback = object : ConnectivityManager.NetworkCallback() {
		override fun onAvailable(network: Network) {
			super.onAvailable(network)
			onNetworkAvailable()
		}

		override fun onLost(network: Network) {
			super.onLost(network)
			onNetworkLost()
		}
	}

	fun isNetworkAvailable(): Boolean {
		val activeNetwork = connectivityManager.activeNetwork ?: return false
		val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
		return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
	}

	fun startMonitoring() {
		val request = NetworkRequest.Builder()
			.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
			.build()
		try {
			connectivityManager.registerNetworkCallback(request, networkCallback)
		} catch (e: Exception) {
			// Fail-safe catch
		}
	}

	fun stopMonitoring() {
		try {
			connectivityManager.unregisterNetworkCallback(networkCallback)
		} catch (e: Exception) {
			// Fail-safe catch
		}
	}
}
