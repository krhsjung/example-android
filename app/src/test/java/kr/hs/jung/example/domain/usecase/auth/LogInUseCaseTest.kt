package kr.hs.jung.example.domain.usecase.auth

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kr.hs.jung.example.data.remote.dto.LoginRequestDto
import kr.hs.jung.example.domain.manager.AuthManager
import kr.hs.jung.example.domain.repository.AuthRepository
import kr.hs.jung.example.domain.service.PasswordHasher
import kr.hs.jung.example.util.TestFixtures
import org.junit.Before
import org.junit.Test

/**
 * LogInUseCase 단위 테스트
 *
 * 테스트 대상:
 * - 입력 데이터 전처리 (trim, hash)
 * - Repository 호출
 * - 성공 시 AuthManager 저장
 * - 실패 시 AuthManager 미호출
 */
class LogInUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authManager: AuthManager
    private lateinit var passwordHasher: PasswordHasher
    private lateinit var useCase: LogInUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        authManager = mockk(relaxed = true)
        passwordHasher = mockk()
        useCase = LogInUseCase(authRepository, authManager, passwordHasher)
    }

    @Test
    fun `invoke trims email before sending`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        val requestSlot = slot<LoginRequestDto>()

        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.login(capture(requestSlot)) } returns Result.success(testUser)

        // When
        useCase("  test@test.com  ", "password")

        // Then
        assertThat(requestSlot.captured.email).isEqualTo("test@test.com")
    }

    @Test
    fun `invoke hashes password before sending`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        val requestSlot = slot<LoginRequestDto>()

        coEvery { passwordHasher.hash("rawPassword") } returns "hashedPassword123"
        coEvery { authRepository.login(capture(requestSlot)) } returns Result.success(testUser)

        // When
        useCase("test@test.com", "rawPassword")

        // Then
        coVerify { passwordHasher.hash("rawPassword") }
        assertThat(requestSlot.captured.password).isEqualTo("hashedPassword123")
    }

    @Test
    fun `invoke calls repository with correct request`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.login(any()) } returns Result.success(testUser)

        // When
        useCase("test@test.com", "password")

        // Then
        coVerify { authRepository.login(any()) }
    }

    @Test
    fun `invoke saves user to AuthManager on success`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.login(any()) } returns Result.success(testUser)

        // When
        val result = useCase("test@test.com", "password")

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { authManager.setUser(testUser) }
    }

    @Test
    fun `invoke does not save user to AuthManager on failure`() = runTest {
        // Given
        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.login(any()) } returns Result.failure(Exception("Login failed"))

        // When
        val result = useCase("test@test.com", "password")

        // Then
        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { authManager.setUser(any()) }
    }

    @Test
    fun `invoke returns user on success`() = runTest {
        // Given
        val testUser = TestFixtures.createUser(email = "test@test.com")
        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.login(any()) } returns Result.success(testUser)

        // When
        val result = useCase("test@test.com", "password")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.email).isEqualTo("test@test.com")
    }

    @Test
    fun `invoke returns failure on repository error`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.login(any()) } returns Result.failure(exception)

        // When
        val result = useCase("test@test.com", "password")

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Network error")
    }
}
