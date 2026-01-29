package kr.hs.jung.example.domain.state

import kotlinx.coroutines.flow.StateFlow
import kr.hs.jung.example.domain.model.User

/**
 * 인증 상태 관리 인터페이스
 *
 * Domain 계층에서 사용자 상태에 접근하기 위한 인터페이스입니다.
 * 실제 구현은 UI 계층(AuthState)에서 제공됩니다.
 *
 * 이 인터페이스를 통해 Domain 계층은 UI 계층에 의존하지 않고
 * 사용자 상태를 관리할 수 있습니다 (의존성 역전 원칙).
 */
interface AuthStateHolder {
    /** 현재 로그인된 사용자 (Flow) */
    val currentUserFlow: StateFlow<User?>

    /** 현재 로그인된 사용자 (즉시 접근) */
    val currentUser: User?

    /** 사용자 정보 설정 */
    fun setUser(user: User?)

    /** 사용자 정보 초기화 (로그아웃 시) */
    fun clear()
}
