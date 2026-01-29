package kr.hs.jung.example.ui.feature.auth.signup

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.domain.usecase.auth.SignUpUseCase
import kr.hs.jung.example.util.BaseViewModelTest
import kr.hs.jung.example.util.TestFixtures
import kr.hs.jung.example.util.assertProperty
import kr.hs.jung.example.util.assertPropertyIsNotNull
import kr.hs.jung.example.util.assertPropertyIsNull
import kr.hs.jung.example.util.collectEvent
import org.junit.Test

/**
 * SignUpViewModel 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest : BaseViewModelTest() {

    private lateinit var signUpUseCase: SignUpUseCase
    private lateinit var viewModel: SignUpViewModel

    override fun setup() {
        super.setup()
        signUpUseCase = mockk()
        viewModel = SignUpViewModel(signUpUseCase, testDispatcher)
    }

    // ========================================
    // 상태 업데이트 테스트
    // ========================================

    @Test
    fun `updateEmail updates email in state`() {
        viewModel.updateEmail("new@email.com")
        viewModel.uiState.assertProperty({ it.email }, "new@email.com")
    }

    @Test
    fun `updatePassword updates password in state`() {
        viewModel.updatePassword("newPassword123!")
        viewModel.uiState.assertProperty({ it.password }, "newPassword123!")
    }

    @Test
    fun `updateConfirmPassword updates confirmPassword in state`() {
        viewModel.updateConfirmPassword("newPassword123!")
        viewModel.uiState.assertProperty({ it.confirmPassword }, "newPassword123!")
    }

    @Test
    fun `updateName updates name in state`() {
        viewModel.updateName("New Name")
        viewModel.uiState.assertProperty({ it.name }, "New Name")
    }

    @Test
    fun `updateAgreeToTerms updates isAgreeToTerms in state`() {
        viewModel.updateAgreeToTerms(true)
        viewModel.uiState.assertProperty({ it.isAgreeToTerms }, true)

        viewModel.updateAgreeToTerms(false)
        viewModel.uiState.assertProperty({ it.isAgreeToTerms }, false)
    }

    // ========================================
    // 폼 유효성 테스트
    // ========================================

    @Test
    fun `isFormValid returns false when email is blank`() {
        viewModel.updateEmail("")
        viewModel.updatePassword("ValidPass123!")
        viewModel.updateConfirmPassword("ValidPass123!")
        viewModel.updateName("Test")
        viewModel.updateAgreeToTerms(true)

        viewModel.uiState.assertProperty({ it.isFormValid }, false)
    }

    @Test
    fun `isFormValid returns false when password is blank`() {
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("")
        viewModel.updateConfirmPassword("ValidPass123!")
        viewModel.updateName("Test")
        viewModel.updateAgreeToTerms(true)

        viewModel.uiState.assertProperty({ it.isFormValid }, false)
    }

    @Test
    fun `isFormValid returns false when terms not agreed`() {
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")
        viewModel.updateConfirmPassword("ValidPass123!")
        viewModel.updateName("Test")
        viewModel.updateAgreeToTerms(false)

        viewModel.uiState.assertProperty({ it.isFormValid }, false)
    }

    @Test
    fun `isFormValid returns true when all fields valid`() {
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")
        viewModel.updateConfirmPassword("ValidPass123!")
        viewModel.updateName("Test")
        viewModel.updateAgreeToTerms(true)

        viewModel.uiState.assertProperty({ it.isFormValid }, true)
    }

    // ========================================
    // 회원가입 검증 테스트
    // ========================================

    @Test
    fun `signUp with empty email shows validation error`() = runTest {
        viewModel.updateEmail("")
        viewModel.updatePassword("ValidPass123!")
        viewModel.updateConfirmPassword("ValidPass123!")
        viewModel.updateName("Test")
        viewModel.updateAgreeToTerms(true)

        viewModel.signUp()

        viewModel.uiState.assertPropertyIsNotNull { it.error }
        coVerify(exactly = 0) { signUpUseCase(any(), any(), any()) }
    }

    @Test
    fun `signUp with mismatched passwords shows validation error`() = runTest {
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")
        viewModel.updateConfirmPassword("DifferentPass456!")
        viewModel.updateName("Test")
        viewModel.updateAgreeToTerms(true)

        viewModel.signUp()

        viewModel.uiState.assertPropertyIsNotNull { it.error }
        coVerify(exactly = 0) { signUpUseCase(any(), any(), any()) }
    }

    // ========================================
    // 회원가입 성공 테스트
    // ========================================

    @Test
    fun `signUp success emits Success event`() = runTest {
        val testUser = TestFixtures.createUser()
        coEvery { signUpUseCase(any(), any(), any()) } returns Result.success(testUser)

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")
        viewModel.updateConfirmPassword("ValidPass123!")
        viewModel.updateName("Test User")
        viewModel.updateAgreeToTerms(true)

        viewModel.event.collectEvent(
            action = { viewModel.signUp() },
            assertion = { event -> assertThat(event).isEqualTo(SignUpEvent.Success) }
        )
    }

    // ========================================
    // 회원가입 실패 테스트
    // ========================================

    @Test
    fun `signUp failure sets error in state`() = runTest {
        coEvery { signUpUseCase(any(), any(), any()) } returns Result.failure(Exception("SignUp failed"))

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")
        viewModel.updateConfirmPassword("ValidPass123!")
        viewModel.updateName("Test User")
        viewModel.updateAgreeToTerms(true)

        viewModel.signUp()

        viewModel.uiState.assertPropertyIsNotNull { it.error }
    }

    // ========================================
    // 로딩 상태 테스트
    // ========================================

    @Test
    fun `signUp sets loading false after completion`() = runTest {
        val testUser = TestFixtures.createUser()
        coEvery { signUpUseCase(any(), any(), any()) } returns Result.success(testUser)

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")
        viewModel.updateConfirmPassword("ValidPass123!")
        viewModel.updateName("Test User")
        viewModel.updateAgreeToTerms(true)

        viewModel.signUp()

        viewModel.uiState.assertProperty({ it.isLoading }, false)
    }

    // ========================================
    // OAuth 테스트
    // ========================================

    @Test
    fun `signInWith emits OAuthRequest event`() = runTest {
        viewModel.event.collectEvent(
            action = { viewModel.signInWith(SnsProvider.GOOGLE) },
            assertion = { event ->
                assertThat(event).isEqualTo(SignUpEvent.OAuthRequest(SnsProvider.GOOGLE))
            }
        )
    }

    @Test
    fun `signInWith with different providers emits correct events`() = runTest {
        viewModel.event.test {
            viewModel.signInWith(SnsProvider.GOOGLE)
            assertThat(awaitItem()).isEqualTo(SignUpEvent.OAuthRequest(SnsProvider.GOOGLE))

            viewModel.signInWith(SnsProvider.APPLE)
            assertThat(awaitItem()).isEqualTo(SignUpEvent.OAuthRequest(SnsProvider.APPLE))
        }
    }

    // ========================================
    // 에러 클리어 테스트
    // ========================================

    @Test
    fun `clearError clears error from state`() = runTest {
        // Given: 에러가 있는 상태
        viewModel.updateEmail("")
        viewModel.signUp()
        viewModel.uiState.assertPropertyIsNotNull { it.error }

        // When
        viewModel.clearError()

        // Then
        viewModel.uiState.assertPropertyIsNull { it.error }
    }
}
