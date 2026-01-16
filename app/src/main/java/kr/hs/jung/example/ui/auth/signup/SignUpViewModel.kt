package kr.hs.jung.example.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kr.hs.jung.example.data.remote.NetworkException
import kr.hs.jung.example.domain.model.SignUpFormData
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.domain.usecase.auth.SignUpUseCase
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _isAgreeToTerms = MutableStateFlow(false)
    val isAgreeToTerms: StateFlow<Boolean> = _isAgreeToTerms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError.asStateFlow()

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess.asStateFlow()

    private val _oAuthLaunchRequest = MutableStateFlow<SnsProvider?>(null)
    val oAuthLaunchRequest: StateFlow<SnsProvider?> = _oAuthLaunchRequest.asStateFlow()

    val isFormValid = combine(
        _email, _password, _confirmPassword, _name, _isAgreeToTerms
    ) { email, password, confirmPassword, name, isAgreeToTerms ->
        email.isNotBlank() &&
        password.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        name.isNotBlank() &&
        isAgreeToTerms
    }

    private val formData: SignUpFormData
        get() = SignUpFormData(
            email = _email.value,
            password = _password.value,
            confirmPassword = _confirmPassword.value,
            name = _name.value,
            isAgreeToTerms = _isAgreeToTerms.value
        )

    fun updateEmail(value: String) {
        _email.value = value
    }

    fun updatePassword(value: String) {
        _password.value = value
    }

    fun updateConfirmPassword(value: String) {
        _confirmPassword.value = value
    }

    fun updateName(value: String) {
        _name.value = value
    }

    fun updateAgreeToTerms(value: Boolean) {
        _isAgreeToTerms.value = value
    }

    fun signUp() {
        val validationResult = formData.validateAll()

        if (!validationResult.isValid) {
            _errorMessage.value = validationResult.errorMessage
            _showError.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = signUpUseCase(
                    email = _email.value,
                    password = _password.value,
                    name = _name.value
                )
                result.onSuccess {
                    _signUpSuccess.value = true
                }.onFailure { error ->
                    _errorMessage.value = when (error) {
                        is NetworkException -> error.message ?: "회원가입에 실패했습니다"
                        else -> error.message ?: "회원가입에 실패했습니다"
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

    fun resetSignUpSuccess() {
        _signUpSuccess.value = false
    }
}
