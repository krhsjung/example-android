package kr.hs.jung.example.ui.feature.auth.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kr.hs.jung.example.di.IoDispatcher
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.domain.model.LogInFormData
import kr.hs.jung.example.domain.model.SnsProvider
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
    val error: AppError? = null
)

/**
 * 로그인 화면 일회성 이벤트
 */
sealed class LogInEvent {
    data object Success : LogInEvent()
    data class OAuthRequest(val provider: SnsProvider) : LogInEvent()
}

/**
 * 로그인 화면 ViewModel
 *
 * 로그인 화면의 상태 관리와 비즈니스 로직을 담당합니다.
 * - 이메일/비밀번호 입력 상태 관리
 * - 폼 유효성 검증
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

    fun updateEmail(value: String) = updateState { copy(email = value) }

    fun updatePassword(value: String) = updateState { copy(password = value) }

    fun logIn() {
        val validationResult = formData.validateAll()

        if (validationResult is ValidationResult.Failure) {
            updateState { copy(error = validationResult.error) }
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

    fun signInWith(provider: SnsProvider) = sendEvent(LogInEvent.OAuthRequest(provider))

    fun clearError() = updateState { copy(error = null) }
}
