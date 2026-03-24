package kr.hs.jung.example.ui.feature.auth.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kr.hs.jung.example.di.IoDispatcher
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.domain.model.LogInFormData
import kr.hs.jung.example.domain.model.LoginProvider
import kr.hs.jung.example.domain.model.ValidationResult
import kr.hs.jung.example.domain.model.appError
import kr.hs.jung.example.domain.usecase.auth.LogInUseCase
import kr.hs.jung.example.ui.common.BaseViewModel
import kr.hs.jung.example.util.config.DebugConfig
import javax.inject.Inject

/**
 * 로그인 화면 UI 상태
 */
data class LogInUiState(
    val email: String = DebugConfig.testEmail,
    val password: String = DebugConfig.testPassword,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val emailError: AppError.Validation? = null,
    val passwordError: AppError.Validation? = null
)

/**
 * 로그인 화면 일회성 이벤트
 */
sealed class LogInEvent {
    data object Success : LogInEvent()
    data class OAuthRequest(val provider: LoginProvider) : LogInEvent()
}

/**
 * 로그인 화면 ViewModel
 *
 * 로그인 화면의 상태 관리와 비즈니스 로직을 담당합니다.
 * - 이메일/비밀번호 입력 상태 관리
 * - 필드별 인라인 유효성 검증 (blur 시)
 * - 로그인 API 호출
 * - OAuth 인증 요청 이벤트 발행
 */
@HiltViewModel
class LogInViewModel @Inject constructor(
    private val logInUseCase: LogInUseCase,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : BaseViewModel<LogInUiState, LogInEvent>(LogInUiState(), ioDispatcher) {

    private val formData: LogInFormData
        get() = LogInFormData(
            email = currentState.email,
            password = currentState.password
        )

    fun updateEmail(value: String) = updateState { copy(email = value, emailError = null) }

    fun updatePassword(value: String) = updateState { copy(password = value, passwordError = null) }

    // blur 시 호출되는 필드별 검증 메서드
    fun validateEmail() {
        val result = formData.validateEmail()
        updateState { copy(emailError = (result as? ValidationResult.Failure)?.error) }
    }

    fun validatePassword() {
        val result = formData.validatePassword()
        updateState { copy(passwordError = (result as? ValidationResult.Failure)?.error) }
    }

    fun logIn() {
        // 필드별 개별 검증 → 인라인 에러 표시
        val emailResult = formData.validateEmail()
        val passwordResult = formData.validatePassword()

        val emailErr = (emailResult as? ValidationResult.Failure)?.error
        val passwordErr = (passwordResult as? ValidationResult.Failure)?.error

        if (emailErr != null || passwordErr != null) {
            updateState {
                copy(
                    emailError = emailErr,
                    passwordError = passwordErr
                )
            }
            return
        }

        viewModelScope.launch {
            withLoading(setLoading = { copy(isLoading = it) }) {
                logInUseCase(currentState.email, currentState.password)
                    .onSuccess { sendEvent(LogInEvent.Success) }
                    .onFailure { error ->
                        updateState { copy(error = error.appError()) }
                    }
            }
        }
    }

    fun signInWith(provider: LoginProvider) = sendEvent(LogInEvent.OAuthRequest(provider))

    fun clearError() = updateState { copy(error = null) }
}
