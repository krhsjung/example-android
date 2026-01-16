package kr.hs.jung.example.data.remote

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kr.hs.jung.example.domain.model.SnsProvider

object OAuthHelper {
    // 딥링크 스킴 (AndroidManifest.xml과 일치해야 함)
    private const val DEEP_LINK_SCHEME = "example"
    private const val DEEP_LINK_HOST = "oauth"
    // Retrofit은 BASE_URL이 '/'로 끝나야 함
    private const val BASE_URL = "https://hsjung.asuscomm.com/example/nestjs/development/api/"

    fun getOAuthUrl(provider: SnsProvider): String {
        val redirectUri = "$DEEP_LINK_SCHEME://$DEEP_LINK_HOST/callback"
        return "${BASE_URL}auth/${provider.value}?flow=android&prompt=select_account&redirect_uri=$redirectUri"
    }

    fun launchOAuth(context: Context, provider: SnsProvider) {
        val url = getOAuthUrl(provider)
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    fun parseOAuthCallback(uri: Uri): String? {
        // 딥링크에서 code 파라미터 추출
        // 예: example://oauth/callback?code=xxx
        if (uri.scheme == DEEP_LINK_SCHEME && uri.host == DEEP_LINK_HOST) {
            return uri.getQueryParameter("code")
        }
        return null
    }
}
