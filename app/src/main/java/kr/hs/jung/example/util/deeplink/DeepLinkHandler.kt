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
     * @property error 에러 메시지
     */
    data class OAuthError(val error: String) : DeepLinkResult()

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
                DeepLinkResult.OAuthError("인증 코드가 없습니다")
            } else {
                DeepLinkResult.OAuthCallback(code)
            }
        } else {
            val error = uri.getQueryParameter("error") ?: "OAuth 인증에 실패했습니다"
            DeepLinkResult.OAuthError(error)
        }
    }
}
