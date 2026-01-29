package kr.hs.jung.example.util.deeplink

import android.net.Uri

/**
 * Deep Link 설정
 *
 * 앱에서 사용하는 모든 Deep Link 관련 상수를 중앙에서 관리합니다.
 * AndroidManifest.xml의 intent-filter와 일치해야 합니다.
 */
object DeepLinkConfig {
    /** Deep Link 스킴 */
    const val SCHEME = "example"

    /** Deep Link 경로 정의 */
    object Path {
        /** OAuth 콜백 */
        const val OAUTH_HOST = "oauth"
        const val OAUTH_CALLBACK = "/callback"
    }

    /** Deep Link URI 빌더 */
    object DeepLinkUri {
        /** OAuth 콜백 URI (example://oauth/callback) */
        val oauthCallback: String
            get() = "$SCHEME://${Path.OAUTH_HOST}${Path.OAUTH_CALLBACK}"
    }

    /**
     * URI가 지정된 Deep Link 경로와 일치하는지 확인
     *
     * @param uri 확인할 URI
     * @param host 예상 호스트
     * @param path 예상 경로 (선택)
     * @return 일치 여부
     */
    fun matches(uri: Uri, host: String, path: String? = null): Boolean {
        if (uri.scheme != SCHEME) return false
        if (uri.host != host) return false
        if (path != null && uri.path != path) return false
        return true
    }

    /**
     * OAuth 콜백 URI인지 확인
     */
    fun isOAuthCallback(uri: Uri): Boolean {
        return matches(uri, Path.OAUTH_HOST, Path.OAUTH_CALLBACK)
    }
}
