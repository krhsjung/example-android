package kr.hs.jung.example.util.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kr.hs.jung.example.util.logger.AppLogger

/**
 * Flow 공통 확장 함수
 *
 * 앱 전체에서 사용하는 Flow 유틸리티 함수들입니다.
 */

/**
 * UI 상태를 나타내는 sealed class
 *
 * Flow의 데이터를 UI 상태로 변환할 때 사용합니다.
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data
    fun exceptionOrNull(): Throwable? = (this as? Error)?.exception
}

/**
 * Flow를 UiState Flow로 변환
 *
 * Loading -> Success/Error 상태 전환을 자동으로 처리합니다.
 */
fun <T> Flow<T>.asUiState(): Flow<UiState<T>> = this
    .map<T, UiState<T>> { UiState.Success(it) }
    .onStart { emit(UiState.Loading) }
    .catch { emit(UiState.Error(it)) }

/**
 * Flow를 StateFlow로 변환 (WhileSubscribed 전략)
 *
 * 구독자가 없으면 5초 후 업스트림을 중지합니다.
 * Configuration change 시에도 데이터를 유지합니다.
 *
 * @param scope CoroutineScope (보통 viewModelScope)
 * @param initialValue 초기값
 * @param stopTimeoutMillis 구독 중단 후 대기 시간 (기본값: 5000ms)
 */
fun <T> Flow<T>.stateInWhileSubscribed(
    scope: CoroutineScope,
    initialValue: T,
    stopTimeoutMillis: Long = 5000L
): StateFlow<T> = stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
    initialValue = initialValue
)

/**
 * Flow에 에러 핸들링 추가
 *
 * 에러 발생 시 로깅하고 기본값을 반환합니다.
 *
 * @param tag 로그 태그
 * @param defaultValue 에러 시 반환할 기본값
 */
fun <T> Flow<T>.catchWithDefault(
    tag: String,
    defaultValue: T
): Flow<T> = catch { e ->
    AppLogger.e(tag, "Flow error: ${e.message}", e)
    emit(defaultValue)
}

/**
 * Flow에 에러 로깅만 추가
 *
 * 에러 발생 시 로깅 후 다시 throw합니다.
 *
 * @param tag 로그 태그
 */
fun <T> Flow<T>.logErrors(tag: String): Flow<T> = catch { e ->
    AppLogger.e(tag, "Flow error: ${e.message}", e)
    throw e
}
