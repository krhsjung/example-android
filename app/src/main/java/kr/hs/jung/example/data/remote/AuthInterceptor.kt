package kr.hs.jung.example.data.remote

import kotlinx.serialization.json.Json
import kr.hs.jung.example.data.local.datastore.TokenManager
import kr.hs.jung.example.data.remote.dto.AuthResponseDto
import kr.hs.jung.example.data.remote.dto.RefreshTokenRequestDto
import kr.hs.jung.example.domain.event.SessionManager
import kr.hs.jung.example.util.config.AppConfig
import kr.hs.jung.example.util.logger.AppLogger
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT 인증 인터셉터
 *
 * 모든 요청에 Authorization: Bearer {accessToken} 헤더를 추가하고,
 * 401 응답 시 refreshToken으로 토큰을 갱신한 뒤 원래 요청을 재시도합니다.
 *
 * 동시성 처리:
 * - synchronized + 토큰 비교로 동시 갱신 요청 방지
 * - 갱신 중 다른 스레드는 대기 후 새 토큰으로 재시도
 *
 * 무한 루프 방지:
 * - refresh 요청 자체에는 갱신 로직 스킵
 * - 갱신 실패 시 SessionManager를 통해 세션 만료 이벤트 발생
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager,
    private val json: Json
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // refresh 요청은 토큰 갱신 로직 스킵 (무한 루프 방지)
        if (originalRequest.isRefreshRequest()) {
            return chain.proceed(originalRequest)
        }

        // 액세스 토큰이 있으면 헤더에 추가
        val accessToken = tokenManager.getAccessToken()
        val request = if (accessToken != null) {
            originalRequest.withBearerToken(accessToken)
        } else {
            originalRequest
        }

        val response = chain.proceed(request)

        // 401이 아니면 그대로 반환
        if (response.code != HTTP_UNAUTHORIZED) {
            return response
        }

        // 401 수신: 토큰 갱신 시도
        AppLogger.d(TAG, "401 received, attempting token refresh")

        synchronized(this) {
            // 다른 스레드가 이미 갱신했는지 확인
            val currentToken = tokenManager.getAccessToken()
            if (currentToken != null && currentToken != accessToken) {
                // 이미 갱신됨 → 새 토큰으로 재시도
                AppLogger.d(TAG, "Token already refreshed by another thread, retrying")
                response.close()
                return chain.proceed(originalRequest.withBearerToken(currentToken))
            }

            // 리프레시 토큰으로 갱신 시도
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                AppLogger.w(TAG, "No refresh token available, session expired")
                sessionManager.notifySessionExpired()
                return response
            }

            val refreshResult = executeRefresh(chain, refreshToken)
            if (refreshResult != null) {
                // 갱신 성공: 새 토큰 저장 후 원래 요청 재시도
                tokenManager.saveTokens(refreshResult.accessToken, refreshResult.refreshToken)
                AppLogger.d(TAG, "Token refreshed successfully, retrying original request")
                response.close()
                return chain.proceed(originalRequest.withBearerToken(refreshResult.accessToken))
            } else {
                // 갱신 실패: 세션 만료 이벤트 발생
                AppLogger.w(TAG, "Token refresh failed, session expired")
                tokenManager.clearTokens()
                sessionManager.notifySessionExpired()
                return response
            }
        }
    }

    /**
     * refresh API 호출
     *
     * @return 성공 시 AuthResponseDto, 실패 시 null
     */
    private fun executeRefresh(chain: Interceptor.Chain, refreshToken: String): AuthResponseDto? {
        return try {
            val requestBody = json.encodeToString(
                RefreshTokenRequestDto.serializer(),
                RefreshTokenRequestDto(refreshToken)
            )

            val refreshRequest = Request.Builder()
                .url("${AppConfig.baseUrl}auth/refresh")
                .post(requestBody.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val refreshResponse = chain.proceed(refreshRequest)

            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body?.string()
                refreshResponse.close()
                body?.let {
                    json.decodeFromString(AuthResponseDto.serializer(), it)
                }
            } else {
                AppLogger.e(TAG, "Refresh request failed with code: ${refreshResponse.code}")
                refreshResponse.close()
                null
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Refresh request exception: ${e.message}", e)
            null
        }
    }

    private fun Request.isRefreshRequest(): Boolean {
        return url.encodedPath.endsWith("auth/refresh")
    }

    private fun Request.withBearerToken(token: String): Request {
        return newBuilder()
            .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$token")
            .build()
    }

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val HTTP_UNAUTHORIZED = 401
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
