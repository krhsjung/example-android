package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.state.AuthStateHolder

/**
 * 인증 관련 UseCase 기본 클래스
 *
 * 인증 성공 시 사용자 정보를 AuthStateHolder에 저장하는 공통 로직을 제공합니다.
 * LogIn, SignUp, ExchangeOAuthCode 등 인증 후 사용자 정보를 저장해야 하는
 * UseCase들이 이 클래스를 상속받습니다.
 *
 * Domain 계층의 인터페이스(AuthStateHolder)를 사용하여
 * UI 계층에 직접 의존하지 않습니다 (의존성 역전 원칙).
 *
 * @property authStateHolder 사용자 상태 관리자 인터페이스
 */
abstract class BaseAuthUseCase(
    protected val authStateHolder: AuthStateHolder
) {
    /**
     * 인증 결과를 처리하고 성공 시 사용자 정보를 저장
     *
     * @param result 인증 API 호출 결과
     * @return 사용자 정보가 저장된 Result
     */
    protected fun Result<User>.withAuthUpdate(): Result<User> =
        onSuccess { user -> authStateHolder.setUser(user) }
}
