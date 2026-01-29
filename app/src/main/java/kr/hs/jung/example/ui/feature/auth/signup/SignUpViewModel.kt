package kr.hs.jung.example.ui.feature.auth.signup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kr.hs.jung.example.di.IoDispatcher
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.domain.model.SignUpFormData
import kr.hs.jung.example.domain.model.SnsProvider
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
    val error: AppError? = null
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
    data class OAuthRequest(val provider: SnsProvider) : SignUpEvent()
}

/**
 * 회원가입 화면 ViewModel
 *
 * 회원가입 화면의 상태 관리와 비즈니스 로직을 담당합니다.
 * - 이메일/비밀번호/이름 입력 상태 관리
 * - 폼 유효성 검증 (비밀번호 확인 포함)
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

    fun updateEmail(value: String) = updateState { copy(email = value) }

    fun updatePassword(value: String) = updateState { copy(password = value) }

    fun updateConfirmPassword(value: String) = updateState { copy(confirmPassword = value) }

    fun updateName(value: String) = updateState { copy(name = value) }

    fun updateAgreeToTerms(value: Boolean) = updateState { copy(isAgreeToTerms = value) }

    fun signUp() {
        val validationResult = formData.validateAll()

        if (validationResult is ValidationResult.Failure) {
            updateState { copy(error = validationResult.error) }
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

    fun signInWith(provider: SnsProvider) = sendEvent(SignUpEvent.OAuthRequest(provider))

    fun clearError() = updateState { copy(error = null) }
}
