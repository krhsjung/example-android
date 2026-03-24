package kr.hs.jung.example.ui.feature.auth.signup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kr.hs.jung.example.di.IoDispatcher
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.domain.model.SignUpFormData
import kr.hs.jung.example.domain.model.LoginProvider
import kr.hs.jung.example.domain.model.ValidationResult
import kr.hs.jung.example.domain.model.appError
import kr.hs.jung.example.domain.usecase.auth.SignUpUseCase
import kr.hs.jung.example.ui.common.BaseViewModel
import kr.hs.jung.example.util.config.DebugConfig
import javax.inject.Inject

/**
 * 회원가입 화면 UI 상태
 */
data class SignUpUiState(
    val email: String = DebugConfig.testEmail,
    val password: String = DebugConfig.testPassword,
    val confirmPassword: String = DebugConfig.testPassword,
    val name: String = DebugConfig.testName,
    val isAgreeToTerms: Boolean = DebugConfig.testAgreeToTerms,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val emailError: AppError.Validation? = null,
    val passwordError: AppError.Validation? = null,
    val confirmPasswordError: AppError.Validation? = null,
    val nameError: AppError.Validation? = null
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                name.isNotBlank() &&
                isAgreeToTerms
}

/**
 * 회원가입 화면 일회성 이벤트
 */
sealed class SignUpEvent {
    data object Success : SignUpEvent()
    data class OAuthRequest(val provider: LoginProvider) : SignUpEvent()
}

/**
 * 회원가입 화면 ViewModel
 *
 * 회원가입 화면의 상태 관리와 비즈니스 로직을 담당합니다.
 * - 이메일/비밀번호/이름 입력 상태 관리
 * - 필드별 인라인 유효성 검증 (blur 시)
 * - 약관 동의 상태 관리
 * - 회원가입 API 호출
 * - OAuth 인증 요청 이벤트 발행
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : BaseViewModel<SignUpUiState, SignUpEvent>(SignUpUiState(), ioDispatcher) {

    private val formData: SignUpFormData
        get() = SignUpFormData(
            email = currentState.email,
            password = currentState.password,
            confirmPassword = currentState.confirmPassword,
            name = currentState.name,
            isAgreeToTerms = currentState.isAgreeToTerms
        )

    fun updateEmail(value: String) = updateState { copy(email = value, emailError = null) }

    fun updatePassword(value: String) = updateState { copy(password = value, passwordError = null) }

    fun updateConfirmPassword(value: String) = updateState { copy(confirmPassword = value, confirmPasswordError = null) }

    fun updateName(value: String) = updateState { copy(name = value, nameError = null) }

    fun updateAgreeToTerms(value: Boolean) = updateState { copy(isAgreeToTerms = value) }

    // blur 시 호출되는 필드별 검증 메서드
    fun validateEmail() {
        val result = formData.validateEmail()
        updateState { copy(emailError = (result as? ValidationResult.Failure)?.error) }
    }

    fun validatePassword() {
        val result = formData.validatePassword()
        updateState { copy(passwordError = (result as? ValidationResult.Failure)?.error) }
    }

    fun validateConfirmPassword() {
        val result = formData.validateConfirmPassword()
        updateState { copy(confirmPasswordError = (result as? ValidationResult.Failure)?.error) }
    }

    fun validateName() {
        val result = formData.validateName()
        updateState { copy(nameError = (result as? ValidationResult.Failure)?.error) }
    }

    fun signUp() {
        // 필드별 개별 검증 → 인라인 에러 표시
        val emailErr = (formData.validateEmail() as? ValidationResult.Failure)?.error
        val passwordErr = (formData.validatePassword() as? ValidationResult.Failure)?.error
        val confirmPasswordErr = (formData.validateConfirmPassword() as? ValidationResult.Failure)?.error
        val nameErr = (formData.validateName() as? ValidationResult.Failure)?.error

        if (emailErr != null || passwordErr != null || confirmPasswordErr != null || nameErr != null) {
            updateState {
                copy(
                    emailError = emailErr,
                    passwordError = passwordErr,
                    confirmPasswordError = confirmPasswordErr,
                    nameError = nameErr
                )
            }
            return
        }

        viewModelScope.launch {
            withLoading(setLoading = { copy(isLoading = it) }) {
                signUpUseCase(
                    email = currentState.email,
                    password = currentState.password,
                    name = currentState.name
                ).onSuccess {
                    sendEvent(SignUpEvent.Success)
                }.onFailure { error ->
                    updateState { copy(error = error.appError()) }
                }
            }
        }
    }

    fun signInWith(provider: LoginProvider) = sendEvent(SignUpEvent.OAuthRequest(provider))

    fun clearError() = updateState { copy(error = null) }
}
