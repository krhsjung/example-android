package kr.hs.jung.example.util.deeplink

import android.content.Intent
import android.net.Uri

/**
 * Deep Link 처리 결과
 */
sealed class DeepLinkResult {
    /**
     * OAuth 콜백
     * @property code 인증 코드
     */
    data class OAuthCallback(val code: String) : DeepLinkResult()

    /**
     * OAuth 실패
     * @property errorCode 에러 코드 (UI 계층에서 문자열 리소스로 변환)
     * @property serverMessage 서버에서 전달한 에러 메시지 (선택, 디버깅용)
     */
    data class OAuthError(
        val errorCode: OAuthErrorCode,
        val serverMessage: String? = null
    ) : DeepLinkResult()

    /**
     * 앱 내 화면 네비게이션
     * @property path 이동할 경로
     */
    data class Navigation(val path: String) : DeepLinkResult()

    /**
     * 알 수 없는 Deep Link
     * @property uri 원본 URI
     */
    data class Unknown(val uri: Uri) : DeepLinkResult()

    /**
     * Deep Link 아님
     */
    data object NotDeepLink : DeepLinkResult()
}

/**
 * OAuth 에러 코드
 *
 * Context 없이 에러 유형을 전달하고,
 * UI 계층에서 문자열 리소스로 변환합니다.
 */
enum class OAuthErrorCode {
    /** 인증 코드가 없음 */
    MISSING_CODE,
    /** OAuth 인증 실패 (서버 응답) */
    AUTH_FAILED
}

/**
 * Deep Link 처리기
 *
 * Intent에서 Deep Link를 파싱하고 적절한 결과를 반환합니다.
 * 새로운 Deep Link 경로를 추가할 때 이 클래스를 확장합니다.
 */
object DeepLinkHandler {

    /**
     * Intent에서 Deep Link 처리
     *
     * @param intent 처리할 Intent
     * @return Deep Link 처리 결과
     */
    fun handle(intent: Intent?): DeepLinkResult {
        val uri = intent?.data ?: return DeepLinkResult.NotDeepLink
        return handle(uri)
    }

    /**
     * URI에서 Deep Link 처리
     *
     * @param uri 처리할 URI
     * @return Deep Link 처리 결과
     */
    fun handle(uri: Uri): DeepLinkResult {
        return when {
            DeepLinkConfig.isOAuthCallback(uri) -> handleOAuthCallback(uri)
            DeepLinkConfig.isSignUp(uri) -> DeepLinkResult.Navigation(DeepLinkConfig.Path.SIGNUP)
            uri.scheme == DeepLinkConfig.SCHEME -> DeepLinkResult.Unknown(uri)
            else -> DeepLinkResult.NotDeepLink
        }
    }

    /**
     * OAuth 콜백 처리
     *
     * 예상 URI 형식: example://oauth/callback?success=true&code=xxx
     * 또는 실패: example://oauth/callback?success=false&error=message
     */
    private fun handleOAuthCallback(uri: Uri): DeepLinkResult {
        val success = uri.getQueryParameter("success")?.toBooleanStrictOrNull() ?: false

        return if (success) {
            val code = uri.getQueryParameter("code")
            if (code.isNullOrBlank()) {
                DeepLinkResult.OAuthError(OAuthErrorCode.MISSING_CODE)
            } else {
                DeepLinkResult.OAuthCallback(code)
            }
        } else {
            val serverMessage = uri.getQueryParameter("error")
            DeepLinkResult.OAuthError(OAuthErrorCode.AUTH_FAILED, serverMessage)
        }
    }
}
