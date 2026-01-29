package kr.hs.jung.example.domain.usecase.auth

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kr.hs.jung.example.data.remote.dto.SignUpRequestDto
import kr.hs.jung.example.domain.manager.AuthManager
import kr.hs.jung.example.domain.repository.AuthRepository
import kr.hs.jung.example.domain.service.PasswordHasher
import kr.hs.jung.example.util.TestFixtures
import org.junit.Before
import org.junit.Test

/**
 * SignUpUseCase 단위 테스트
 */
class SignUpUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authManager: AuthManager
    private lateinit var passwordHasher: PasswordHasher
    private lateinit var useCase: SignUpUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        authManager = mockk(relaxed = true)
        passwordHasher = mockk()
        useCase = SignUpUseCase(authRepository, authManager, passwordHasher)
    }

    @Test
    fun `invoke trims email and name before sending`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        val requestSlot = slot<SignUpRequestDto>()

        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.signUp(capture(requestSlot)) } returns Result.success(testUser)

        // When
        useCase("  test@test.com  ", "password", "  Test User  ")

        // Then
        assertThat(requestSlot.captured.email).isEqualTo("test@test.com")
        assertThat(requestSlot.captured.name).isEqualTo("Test User")
    }

    @Test
    fun `invoke hashes password before sending`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        val requestSlot = slot<SignUpRequestDto>()

        coEvery { passwordHasher.hash("rawPassword") } returns "hashedPassword123"
        coEvery { authRepository.signUp(capture(requestSlot)) } returns Result.success(testUser)

        // When
        useCase("test@test.com", "rawPassword", "Test User")

        // Then
        coVerify { passwordHasher.hash("rawPassword") }
        assertThat(requestSlot.captured.password).isEqualTo("hashedPassword123")
    }

    @Test
    fun `invoke saves user to AuthManager on success`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.signUp(any()) } returns Result.success(testUser)

        // When
        val result = useCase("test@test.com", "password", "Test User")

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { authManager.setUser(testUser) }
    }

    @Test
    fun `invoke does not save user to AuthManager on failure`() = runTest {
        // Given
        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.signUp(any()) } returns Result.failure(Exception("SignUp failed"))

        // When
        val result = useCase("test@test.com", "password", "Test User")

        // Then
        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { authManager.setUser(any()) }
    }

    @Test
    fun `invoke returns user on success`() = runTest {
        // Given
        val testUser = TestFixtures.createUser(name = "Test User")
        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.signUp(any()) } returns Result.success(testUser)

        // When
        val result = useCase("test@test.com", "password", "Test User")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.name).isEqualTo("Test User")
    }

    @Test
    fun `invoke returns failure on repository error`() = runTest {
        // Given
        val exception = Exception("Email already exists")
        coEvery { passwordHasher.hash(any()) } returns "hashedPassword"
        coEvery { authRepository.signUp(any()) } returns Result.failure(exception)

        // When
        val result = useCase("test@test.com", "password", "Test User")

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Email already exists")
    }
}
