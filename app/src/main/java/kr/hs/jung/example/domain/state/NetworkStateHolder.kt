package kr.hs.jung.example.domain.state

import kotlinx.coroutines.flow.StateFlow

/**
 * 네트워크 상태 관리 인터페이스
 *
 * Domain 계층에서 네트워크 상태에 접근하기 위한 인터페이스입니다.
 * 실제 구현은 Data 계층(NetworkMonitor)에서 제공됩니다.
 */
interface NetworkStateHolder {
    /** 네트워크 사용 가능 여부 (Flow) */
    val isNetworkAvailable: StateFlow<Boolean>
}
