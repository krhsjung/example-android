package kr.hs.jung.example.ui.navigation

import kotlinx.serialization.Serializable

/**
 * 인증 화면 네비게이션 경로
 *
 * Type-Safe Navigation을 사용하여 컴파일 타임에 경로 검증
 */
sealed interface AuthRoute {
    /** 로그인 화면 */
    @Serializable
    data object LogIn : AuthRoute

    /** 회원가입 화면 */
    @Serializable
    data object SignUp : AuthRoute
}
