package kr.hs.jung.example.data.repository

import kr.hs.jung.example.data.local.cache.UserCache
import kr.hs.jung.example.data.local.datastore.TokenManager
import kr.hs.jung.example.data.remote.SafeApiCall
import kr.hs.jung.example.data.remote.api.AuthApi
import kr.hs.jung.example.data.remote.dto.ExchangeRequestDto
import kr.hs.jung.example.data.remote.dto.LoginRequestDto
import kr.hs.jung.example.data.remote.dto.SignUpRequestDto
import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.repository.AuthRepository
import kr.hs.jung.example.util.logger.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthRepository 구현체
 *
 * 인증 관련 데이터 작업을 처리합니다.
 * JWT 토큰은 TokenManager를 통해 암호화 저장하고,
 * UserCache를 통해 사용자 정보를 캐싱하여 불필요한 네트워크 요청을 줄입니다.
 *
 * 캐시 전략:
 * - login/signUp/exchangeOAuthCode 성공 시 → 토큰 저장 + 캐시 갱신
 * - logout/clearSession 시 → 토큰 삭제 + 캐시 제거
 * - me() 호출 시 → 캐시 사용 가능 (getUser 권장)
 * - getUser(forceRefresh) → 캐시 우선, 강제 새로고침 지원
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val safeApiCall: SafeApiCall,
    private val userCache: UserCache
) : AuthRepository {

    override suspend fun login(request: LoginRequestDto): Result<User> {
        return safeApiCall { authApi.login(request) }
            .map { authResponse ->
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                authResponse.user.toDomain()
            }
            .onSuccess { user ->
                AppLogger.d(TAG, "Login success, caching user")
                userCache.set(user)
            }
    }

    override suspend fun signUp(request: SignUpRequestDto): Result<User> {
        return safeApiCall { authApi.signUp(request) }
            .map { authResponse ->
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                authResponse.user.toDomain()
            }
            .onSuccess { user ->
                AppLogger.d(TAG, "SignUp success, caching user")
                userCache.set(user)
            }
    }

    override suspend fun logout(): Result<Unit> {
        return safeApiCall.ignoreBody { authApi.logout() }
            .onSuccess {
                AppLogger.d(TAG, "Logout success, clearing session")
                tokenManager.clearTokens()
                userCache.clear()
            }
    }

    override suspend fun me(): Result<User> {
        return safeApiCall { authApi.me() }
            .map { it.toDomain() }
            .onSuccess { user ->
                AppLogger.d(TAG, "Me success, caching user")
                userCache.set(user)
            }
    }

    override suspend fun exchangeOAuthCode(request: ExchangeRequestDto): Result<User> {
        return safeApiCall { authApi.exchange(request) }
            .map { authResponse ->
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                authResponse.user.toDomain()
            }
            .onSuccess { user ->
                AppLogger.d(TAG, "OAuth exchange success, caching user")
                userCache.set(user)
            }
    }

    override suspend fun clearSession() {
        AppLogger.d(TAG, "Clearing session and cache")
        tokenManager.clearTokens()
        userCache.clear()
    }

    override suspend fun getCachedUser(): User? {
        return userCache.get()
    }

    override suspend fun getUser(forceRefresh: Boolean): Result<User> {
        // 강제 새로고침이 아니면 캐시 먼저 확인
        if (!forceRefresh) {
            userCache.get()?.let { cachedUser ->
                AppLogger.d(TAG, "Returning cached user")
                return Result.success(cachedUser)
            }
        }

        // 캐시 미스 또는 강제 새로고침: 서버에서 조회
        AppLogger.d(TAG, "Fetching user from server (forceRefresh=$forceRefresh)")
        return me()
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
