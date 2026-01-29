package kr.hs.jung.example.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel 기본 클래스
 *
 * UI 상태(State)와 일회성 이벤트(Event)를 관리하는 공통 패턴을 제공합니다.
 *
 * State: MutableStateFlow - UI 상태를 나타내며, 최신 값을 유지
 * Event: MutableSharedFlow - 일회성 이벤트를 나타내며, replay=0으로 이벤트 손실 없이 전달
 *
 * @param S UI 상태 타입
 * @param E 일회성 이벤트 타입
 * @param initialState 초기 UI 상태
 * @param ioDispatcher IO 작업용 Dispatcher (@IoDispatcher로 주입, 테스트 시 TestDispatcher 주입 가능)
 */
abstract class BaseViewModel<S, E>(
    initialState: S,
    protected val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<E>(extraBufferCapacity = 1)
    val event: SharedFlow<E> = _event.asSharedFlow()

    /**
     * 현재 UI 상태 값
     */
    protected val currentState: S
        get() = _uiState.value

    /**
     * UI 상태를 업데이트합니다.
     *
     * @param reducer 현재 상태를 받아 새로운 상태를 반환하는 함수
     */
    protected fun updateState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    /**
     * 일회성 이벤트를 전송합니다.
     *
     * tryEmit을 사용하여 suspend 없이 즉시 발행합니다.
     * extraBufferCapacity=1로 설정되어 있어 버퍼가 가득 차도 손실 없이 전달됩니다.
     *
     * @param event 전송할 이벤트
     */
    protected fun sendEvent(event: E) {
        _event.tryEmit(event)
    }

    /**
     * 로딩 상태를 관리하며 비동기 작업을 실행합니다.
     *
     * @param setLoading 로딩 상태를 설정하는 함수
     * @param action 실행할 비동기 작업
     */
    protected suspend fun <T> withLoading(
        setLoading: S.(Boolean) -> S,
        action: suspend () -> T
    ): T {
        updateState { setLoading(true) }
        return try {
            action()
        } finally {
            updateState { setLoading(false) }
        }
    }

    /**
     * IO Dispatcher에서 비동기 작업을 실행합니다.
     *
     * @param action 실행할 비동기 작업
     */
    protected suspend fun <T> withIoContext(action: suspend () -> T): T {
        return withContext(ioDispatcher) { action() }
    }

    /**
     * viewModelScope에서 코루틴을 실행합니다.
     * 테스트 시 Dispatcher 교체가 용이합니다.
     *
     * @param action 실행할 비동기 작업
     */
    protected fun launchInScope(action: suspend () -> Unit) {
        viewModelScope.launch { action() }
    }
}
