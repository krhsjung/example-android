package kr.hs.jung.example.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.hs.jung.example.domain.usecase.auth.LogoutUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    private val _logoutSuccess = MutableStateFlow(false)
    val logoutSuccess: StateFlow<Boolean> = _logoutSuccess.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _isLoggingOut.value = true
            logoutUseCase()
            _logoutSuccess.value = true
            _isLoggingOut.value = false
        }
    }

    fun resetLogoutSuccess() {
        _logoutSuccess.value = false
    }
}
