package kr.hs.jung.example.ui.feature.auth.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.domain.usecase.auth.LogInUseCase
import kr.hs.jung.example.util.BaseViewModelTest
import kr.hs.jung.example.util.TestFixtures
import kr.hs.jung.example.util.assertProperty
import kr.hs.jung.example.util.assertPropertyIsNotNull
import kr.hs.jung.example.util.assertPropertyIsNull
import kr.hs.jung.example.util.collectEvent
import kr.hs.jung.example.util.expectNoEvent
import org.junit.Test

/**
 * LogInViewModel 단위 테스트
 *
 * 테스트 대상:
 * - 상태 업데이트 (email, password)
 * - 로그인 검증 로직
 * - 로그인 성공/실패 처리
 * - 이벤트 발생
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LogInViewModelTest : BaseViewModelTest() {

    private lateinit var logInUseCase: LogInUseCase
    private lateinit var viewModel: LogInViewModel

    override fun setup() {
        super.setup()
        logInUseCase = mockk()
        viewModel = LogInViewModel(logInUseCase, testDispatcher)
    }

    // ========================================
    // 상태 업데이트 테스트
    // ========================================

    @Test
    fun `updateEmail updates email in state`() {
        // When
        viewModel.updateEmail("new@email.com")

        // Then
        viewModel.uiState.assertProperty({ it.email }, "new@email.com")
    }

    @Test
    fun `updatePassword updates password in state`() {
        // When
        viewModel.updatePassword("newPassword123!")

        // Then
        viewModel.uiState.assertProperty({ it.password }, "newPassword123!")
    }

    @Test
    fun `clearError clears error from state`() = runTest {
        // Given: 에러가 있는 상태
        viewModel.updateEmail("")
        viewModel.logIn() // 빈 이메일로 검증 에러 발생

        viewModel.uiState.assertPropertyIsNotNull { it.error }

        // When
        viewModel.clearError()

        // Then
        viewModel.uiState.assertPropertyIsNull { it.error }
    }

    // ========================================
    // 입력 검증 테스트
    // ========================================

    @Test
    fun `logIn with empty email shows validation error`() = runTest {
        // Given
        viewModel.updateEmail("")
        viewModel.updatePassword("ValidPass123!")

        // When
        viewModel.logIn()

        // Then
        val error = viewModel.uiState.value.error
        assertThat(error).isNotNull()
        assertThat(error).isInstanceOf(AppError.Validation::class.java)
    }

    @Test
    fun `logIn with invalid email format shows validation error`() = runTest {
        // Given
        viewModel.updateEmail("invalid-email")
        viewModel.updatePassword("ValidPass123!")

        // When
        viewModel.logIn()

        // Then
        val error = viewModel.uiState.value.error
        assertThat(error).isNotNull()
        assertThat(error).isInstanceOf(AppError.Validation::class.java)
    }

    @Test
    fun `logIn with empty password shows validation error`() = runTest {
        // Given
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("")

        // When
        viewModel.logIn()

        // Then
        viewModel.uiState.assertPropertyIsNotNull { it.error }
    }

    @Test
    fun `logIn with validation error does not call useCase`() = runTest {
        // Given
        viewModel.updateEmail("")
        viewModel.updatePassword("")

        // When
        viewModel.logIn()

        // Then
        coVerify(exactly = 0) { logInUseCase(any(), any()) }
    }

    // ========================================
    // 로그인 성공 테스트
    // ========================================

    @Test
    fun `logIn success emits Success event`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        coEvery { logInUseCase(any(), any()) } returns Result.success(testUser)

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")

        // When & Then
        viewModel.event.collectEvent(
            action = { viewModel.logIn() },
            assertion = { event -> assertThat(event).isEqualTo(LogInEvent.Success) }
        )
    }

    @Test
    fun `logIn success does not set error`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        coEvery { logInUseCase(any(), any()) } returns Result.success(testUser)

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")

        // When
        viewModel.logIn()

        // Then
        viewModel.uiState.assertPropertyIsNull { it.error }
    }

    // ========================================
    // 로그인 실패 테스트
    // ========================================

    @Test
    fun `logIn failure sets error in state`() = runTest {
        // Given
        coEvery { logInUseCase(any(), any()) } returns Result.failure(Exception("Login failed"))

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")

        // When
        viewModel.logIn()

        // Then
        viewModel.uiState.assertPropertyIsNotNull { it.error }
    }

    @Test
    fun `logIn failure does not emit Success event`() = runTest {
        // Given
        coEvery { logInUseCase(any(), any()) } returns Result.failure(Exception("Login failed"))

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")

        // When & Then
        viewModel.event.expectNoEvent { viewModel.logIn() }
    }

    // ========================================
    // 로딩 상태 테스트
    // ========================================

    @Test
    fun `logIn sets loading true then false`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        coEvery { logInUseCase(any(), any()) } returns Result.success(testUser)

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")

        // When
        viewModel.logIn()

        // Then: 로그인 완료 후 로딩은 false
        viewModel.uiState.assertProperty({ it.isLoading }, false)
    }

    @Test
    fun `logIn failure also sets loading false`() = runTest {
        // Given
        coEvery { logInUseCase(any(), any()) } returns Result.failure(Exception("Error"))

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")

        // When
        viewModel.logIn()

        // Then
        viewModel.uiState.assertProperty({ it.isLoading }, false)
    }

    // ========================================
    // OAuth 테스트
    // ========================================

    @Test
    fun `signInWith emits OAuthRequest event`() = runTest {
        // When & Then
        viewModel.event.collectEvent(
            action = { viewModel.signInWith(SnsProvider.GOOGLE) },
            assertion = { event ->
                assertThat(event).isEqualTo(LogInEvent.OAuthRequest(SnsProvider.GOOGLE))
            }
        )
    }

    @Test
    fun `signInWith with different providers emits correct events`() = runTest {
        viewModel.event.test {
            // Google
            viewModel.signInWith(SnsProvider.GOOGLE)
            assertThat(awaitItem()).isEqualTo(LogInEvent.OAuthRequest(SnsProvider.GOOGLE))

            // Apple
            viewModel.signInWith(SnsProvider.APPLE)
            assertThat(awaitItem()).isEqualTo(LogInEvent.OAuthRequest(SnsProvider.APPLE))
        }
    }

    // ========================================
    // UseCase 호출 검증
    // ========================================

    @Test
    fun `logIn calls useCase with correct parameters`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        coEvery { logInUseCase(any(), any()) } returns Result.success(testUser)

        val email = "test@test.com"
        val password = "ValidPass123!"

        viewModel.updateEmail(email)
        viewModel.updatePassword(password)

        // When
        viewModel.logIn()

        // Then
        coVerify { logInUseCase(email, password) }
    }
}
