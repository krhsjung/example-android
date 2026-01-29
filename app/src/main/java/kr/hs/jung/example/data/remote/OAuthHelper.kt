package kr.hs.jung.example.data.remote

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.util.config.AppConfig
import kr.hs.jung.example.util.deeplink.DeepLinkConfig

/**
 * OAuth 인증 헬퍼
 *
 * SNS 로그인을 위한 OAuth 플로우를 처리합니다.
 * Chrome Custom Tab을 통해 OAuth 인증 페이지를 열고,
 * 콜백 딥링크를 통해 인증 결과를 받습니다.
 */
object OAuthHelper {

    /**
     * OAuth 인증 URL 생성
     *
     * @param provider SNS 제공자 (Google, Apple 등)
     * @return OAuth 인증 페이지 URL
     */
    fun getOAuthUrl(provider: SnsProvider): String {
        val redirectUri = DeepLinkConfig.DeepLinkUri.oauthCallback
        return "${AppConfig.baseUrl}auth/${provider.value}?flow=android&prompt=select_account&redirect_uri=$redirectUri"
    }

    /**
     * OAuth 인증 시작
     *
     * Chrome Custom Tab을 통해 OAuth 인증 페이지를 엽니다.
     *
     * @param context Context
     * @param provider SNS 제공자
     */
    fun launchOAuth(context: Context, provider: SnsProvider) {
        val url = getOAuthUrl(provider)
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, url.toUri())
    }
}
