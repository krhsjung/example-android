package kr.hs.jung.example.ui.common.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.state.AuthStateHolder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 전역 인증 상태 관리
 *
 * 앱 전체에서 현재 로그인된 사용자 정보를 관리합니다.
 * UI 계층에서 사용자 상태를 구독하고 반응할 수 있습니다.
 *
 * Domain 계층의 AuthStateHolder 인터페이스를 구현하여
 * UseCase에서 인터페이스를 통해 접근할 수 있습니다.
 *
 * @property currentUserFlow 현재 사용자 상태를 Flow로 제공
 * @property currentUser 현재 사용자 즉시 접근
 */
@Singleton
class AuthState @Inject constructor() : AuthStateHolder {

    private val _currentUser = MutableStateFlow<User?>(null)

    /** 현재 로그인된 사용자 (Flow) */
    override val currentUserFlow: StateFlow<User?> = _currentUser.asStateFlow()

    /** 현재 로그인된 사용자 (즉시 접근) */
    override val currentUser: User?
        get() = _currentUser.value

    /** 사용자 정보 설정 */
    override fun setUser(user: User?) {
        _currentUser.value = user
    }

    /** 사용자 정보 초기화 (로그아웃 시) */
    override fun clear() {
        _currentUser.value = null
    }
}

/**
 * 기존 AuthManager와의 호환성을 위한 타입 별칭
 */
typealias AuthManager = AuthState
