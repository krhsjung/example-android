package kr.hs.jung.example.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.hs.jung.example.BuildConfig
import kr.hs.jung.example.data.remote.NetworkException
import kr.hs.jung.example.domain.model.LogInFormData
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.domain.usecase.auth.LogInUseCase
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val logInUseCase: LogInUseCase
) : ViewModel() {

    private val _email = MutableStateFlow(if (BuildConfig.DEBUG) "test@test.com" else "")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow(if (BuildConfig.DEBUG) "Test2022@!" else "")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError.asStateFlow()

    private val _logInSuccess = MutableStateFlow(false)
    val logInSuccess: StateFlow<Boolean> = _logInSuccess.asStateFlow()

    private val _oAuthLaunchRequest = MutableStateFlow<SnsProvider?>(null)
    val oAuthLaunchRequest: StateFlow<SnsProvider?> = _oAuthLaunchRequest.asStateFlow()

    private val formData: LogInFormData
        get() = LogInFormData(
            email = _email.value,
            password = _password.value
        )

    fun updateEmail(value: String) {
        _email.value = value
    }

    fun updatePassword(value: String) {
        _password.value = value
    }

    fun logIn() {
        val validationResult = formData.validateAll()

        if (!validationResult.isValid) {
            _errorMessage.value = validationResult.errorMessage
            _showError.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = logInUseCase(_email.value, _password.value)
                result.onSuccess {
                    _logInSuccess.value = true
                }.onFailure { error ->
                    _errorMessage.value = when (error) {
                        is NetworkException -> error.message ?: "로그인에 실패했습니다"
                        else -> error.message ?: "로그인에 실패했습니다"
                    }
                    _showError.value = true
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWith(provider: SnsProvider) {
        _oAuthLaunchRequest.value = provider
    }

    fun clearOAuthLaunchRequest() {
        _oAuthLaunchRequest.value = null
    }

    fun clearError() {
        _errorMessage.value = ""
        _showError.value = false
    }

    fun resetLogInSuccess() {
        _logInSuccess.value = false
    }
}
