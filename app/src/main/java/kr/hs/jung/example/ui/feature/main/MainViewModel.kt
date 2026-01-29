package kr.hs.jung.example.ui.feature.main

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kr.hs.jung.example.di.IoDispatcher
import kr.hs.jung.example.ui.common.state.AuthManager
import kr.hs.jung.example.domain.usecase.auth.LogoutUseCase
import kr.hs.jung.example.ui.common.BaseViewModel
import javax.inject.Inject

/**
 * 메인 화면 UI 상태
 */
data class MainUiState(
    val isLoading: Boolean = false
)

/**
 * 메인 화면 일회성 이벤트
 */
sealed class MainEvent {
    data object LogoutSuccess : MainEvent()
}

/**
 * 메인 화면 ViewModel
 *
 * 메인 화면의 상태 관리와 비즈니스 로직을 담당합니다.
 * - 로그아웃 처리
 * - 사용자 상태 관리
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val authManager: AuthManager,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : BaseViewModel<MainUiState, MainEvent>(MainUiState(), ioDispatcher) {

    fun logout() {
        viewModelScope.launch {
            withLoading(setLoading = { copy(isLoading = it) }) {
                logoutUseCase()
                authManager.clear()
                sendEvent(MainEvent.LogoutSuccess)
            }
        }
    }
}
