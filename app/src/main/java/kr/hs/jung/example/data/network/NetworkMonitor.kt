package kr.hs.jung.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.hs.jung.example.domain.state.NetworkStateHolder
import kr.hs.jung.example.util.logger.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 네트워크 상태 모니터링
 *
 * ConnectivityManager.NetworkCallback을 사용하여
 * 실시간으로 네트워크 연결 상태를 감지합니다.
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkStateHolder {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isNetworkAvailable = MutableStateFlow(checkCurrentNetwork())
    override val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            AppLogger.d(TAG, "Network available")
            _isNetworkAvailable.value = true
        }

        override fun onLost(network: Network) {
            AppLogger.d(TAG, "Network lost")
            _isNetworkAvailable.value = false
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternet = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
            AppLogger.d(TAG, "Network capabilities changed: hasInternet=$hasInternet")
            _isNetworkAvailable.value = hasInternet
        }
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun checkCurrentNetwork(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        private const val TAG = "NetworkMonitor"
    }
}
