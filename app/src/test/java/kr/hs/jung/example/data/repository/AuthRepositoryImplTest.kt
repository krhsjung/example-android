package kr.hs.jung.example.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kr.hs.jung.example.data.local.cache.UserCache
import kr.hs.jung.example.domain.model.AppException
import kr.hs.jung.example.data.remote.PersistentCookieJar
import kr.hs.jung.example.data.remote.SafeApiCall
import kr.hs.jung.example.data.remote.api.AuthApi
import kr.hs.jung.example.data.remote.dto.LoginRequestDto
import kr.hs.jung.example.data.remote.dto.SignUpRequestDto
import kr.hs.jung.example.data.remote.dto.UserDto
import kr.hs.jung.example.domain.event.SessionManager
import kr.hs.jung.example.domain.model.User
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * AuthRepositoryImpl 단위 테스트
 *
 * 테스트 대상:
 * - API 호출 및 응답 처리
 * - DTO → Domain 변환
 * - 에러 케이스 처리
 * - 세션 클리어
 * - 캐싱 동작
 */
class AuthRepositoryImplTest {

    private lateinit var authApi: AuthApi
    private lateinit var cookieJar: PersistentCookieJar
    private lateinit var sessionManager: SessionManager
    private lateinit var safeApiCall: SafeApiCall
    private lateinit var userCache: UserCache
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        // Mock android.util.Log to avoid RuntimeException in unit tests
        mockkStatic(android.util.Log::class)
        io.mockk.every { android.util.Log.e(any(), any()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any(), any()) } returns 0
        io.mockk.every { android.util.Log.d(any(), any()) } returns 0

        authApi = mockk()
        cookieJar = mockk(relaxed = true)
        sessionManager = SessionManager()
        userCache = mockk(relaxed = true)
        safeApiCall = SafeApiCall(Json { ignoreUnknownKeys = true }, sessionManager)
        repository = AuthRepositoryImpl(authApi, cookieJar, safeApiCall, userCache)
    }

    @After
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
    }

    // ========================================
    // 로그인 테스트
    // ========================================

    @Test
    fun `login returns mapped domain model on success`() = runTest {
        // Given
        val userDto = UserDto(idx = 1, name = "Test User", email = "test@test.com")
        coEvery { authApi.login(any()) } returns Response.success(userDto)

        // When
        val result = repository.login(LoginRequestDto("test@test.com", "password"))

        // Then
        assertThat(result.isSuccess).isTrue()
        val user = result.getOrNull()
        assertThat(user?.idx).isEqualTo(1)
        assertThat(user?.name).isEqualTo("Test User")
        assertThat(user?.email).isEqualTo("test@test.com")
    }

    @Test
    fun `login caches user on success`() = runTest {
        // Given
        val userDto = UserDto(idx = 1, name = "Test User", email = "test@test.com")
        coEvery { authApi.login(any()) } returns Response.success(userDto)

        // When
        repository.login(LoginRequestDto("test@test.com", "password"))

        // Then
        coVerify { userCache.set(any()) }
    }

    @Test
    fun `login calls api with correct request`() = runTest {
        // Given
        val userDto = UserDto(idx = 1, name = "Test", email = "test@test.com")
        coEvery { authApi.login(any()) } returns Response.success(userDto)

        val request = LoginRequestDto("test@test.com", "password123")

        // When
        repository.login(request)

        // Then
        coVerify { authApi.login(request) }
    }

    @Test
    fun `login returns failure on 401 response`() = runTest {
        // Given
        coEvery { authApi.login(any()) } returns Response.error(
            401,
            "Unauthorized".toResponseBody(null)
        )

        // When
        val result = repository.login(LoginRequestDto("test@test.com", "wrong"))

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AppException::class.java)
    }

    @Test
    fun `login returns failure on 500 response`() = runTest {
        // Given
        coEvery { authApi.login(any()) } returns Response.error(
            500,
            "Internal Server Error".toResponseBody(null)
        )

        // When
        val result = repository.login(LoginRequestDto("test@test.com", "password"))

        // Then
        assertThat(result.isFailure).isTrue()
    }

    // ========================================
    // 회원가입 테스트
    // ========================================

    @Test
    fun `signUp returns mapped domain model on success`() = runTest {
        // Given
        val userDto = UserDto(idx = 2, name = "New User", email = "new@test.com")
        coEvery { authApi.signUp(any()) } returns Response.success(userDto)

        // When
        val result = repository.signUp(SignUpRequestDto("new@test.com", "password", "New User"))

        // Then
        assertThat(result.isSuccess).isTrue()
        val user = result.getOrNull()
        assertThat(user?.name).isEqualTo("New User")
    }

    @Test
    fun `signUp caches user on success`() = runTest {
        // Given
        val userDto = UserDto(idx = 2, name = "New User", email = "new@test.com")
        coEvery { authApi.signUp(any()) } returns Response.success(userDto)

        // When
        repository.signUp(SignUpRequestDto("new@test.com", "password", "New User"))

        // Then
        coVerify { userCache.set(any()) }
    }

    @Test
    fun `signUp returns failure on 409 conflict`() = runTest {
        // Given
        coEvery { authApi.signUp(any()) } returns Response.error(
            409,
            "Email already exists".toResponseBody(null)
        )

        // When
        val result = repository.signUp(SignUpRequestDto("existing@test.com", "password", "User"))

        // Then
        assertThat(result.isFailure).isTrue()
    }

    // ========================================
    // 로그아웃 테스트
    // ========================================

    @Test
    fun `logout returns success on 200 response`() = runTest {
        // Given
        coEvery { authApi.logout() } returns Response.success(Unit)

        // When
        val result = repository.logout()

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `logout clears cache on success`() = runTest {
        // Given
        coEvery { authApi.logout() } returns Response.success(Unit)

        // When
        repository.logout()

        // Then
        coVerify { userCache.clear() }
    }

    @Test
    fun `logout calls api`() = runTest {
        // Given
        coEvery { authApi.logout() } returns Response.success(Unit)

        // When
        repository.logout()

        // Then
        coVerify { authApi.logout() }
    }

    // ========================================
    // 사용자 정보 조회 테스트
    // ========================================

    @Test
    fun `me returns user on success`() = runTest {
        // Given
        val userDto = UserDto(idx = 1, name = "Current User", email = "current@test.com")
        coEvery { authApi.me() } returns Response.success(userDto)

        // When
        val result = repository.me()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.name).isEqualTo("Current User")
    }

    @Test
    fun `me caches user on success`() = runTest {
        // Given
        val userDto = UserDto(idx = 1, name = "Current User", email = "current@test.com")
        coEvery { authApi.me() } returns Response.success(userDto)

        // When
        repository.me()

        // Then
        coVerify { userCache.set(any()) }
    }

    @Test
    fun `me returns failure on 401 unauthorized`() = runTest {
        // Given
        coEvery { authApi.me() } returns Response.error(
            401,
            "Unauthorized".toResponseBody(null)
        )

        // When
        val result = repository.me()

        // Then
        assertThat(result.isFailure).isTrue()
    }

    // ========================================
    // 세션 클리어 테스트
    // ========================================

    @Test
    fun `clearSession calls cookieJar clear and cache clear`() = runTest {
        // When
        repository.clearSession()

        // Then
        verify { cookieJar.clear() }
        coVerify { userCache.clear() }
    }

    // ========================================
    // 캐시 조회 테스트
    // ========================================

    @Test
    fun `getCachedUser returns cached user`() = runTest {
        // Given
        val cachedUser = User(idx = 1, name = "Cached", email = "cached@test.com")
        coEvery { userCache.get() } returns cachedUser

        // When
        val result = repository.getCachedUser()

        // Then
        assertThat(result).isEqualTo(cachedUser)
    }

    @Test
    fun `getCachedUser returns null when cache is empty`() = runTest {
        // Given
        coEvery { userCache.get() } returns null

        // When
        val result = repository.getCachedUser()

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getUser returns cached user when available and forceRefresh is false`() = runTest {
        // Given
        val cachedUser = User(idx = 1, name = "Cached", email = "cached@test.com")
        coEvery { userCache.get() } returns cachedUser

        // When
        val result = repository.getUser(forceRefresh = false)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(cachedUser)
        coVerify(exactly = 0) { authApi.me() }
    }

    @Test
    fun `getUser fetches from server when cache is empty`() = runTest {
        // Given
        coEvery { userCache.get() } returns null
        val userDto = UserDto(idx = 1, name = "Server User", email = "server@test.com")
        coEvery { authApi.me() } returns Response.success(userDto)

        // When
        val result = repository.getUser(forceRefresh = false)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.name).isEqualTo("Server User")
        coVerify { authApi.me() }
    }

    @Test
    fun `getUser fetches from server when forceRefresh is true`() = runTest {
        // Given
        val cachedUser = User(idx = 1, name = "Cached", email = "cached@test.com")
        coEvery { userCache.get() } returns cachedUser
        val userDto = UserDto(idx = 1, name = "Fresh User", email = "fresh@test.com")
        coEvery { authApi.me() } returns Response.success(userDto)

        // When
        val result = repository.getUser(forceRefresh = true)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.name).isEqualTo("Fresh User")
        coVerify { authApi.me() }
    }
}
