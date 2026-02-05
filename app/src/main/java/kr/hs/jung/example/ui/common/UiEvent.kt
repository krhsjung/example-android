package kr.hs.jung.example.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Lifecycle-aware 이벤트 수집 확장 함수
 *
 * SharedFlow 기반 일회성 이벤트를 Lifecycle에 맞춰 안전하게 수집합니다.
 * STARTED 상태에서만 이벤트를 수집하여 백그라운드에서의 불필요한 처리를 방지합니다.
 *
 * @param lifecycleOwner Lifecycle 소유자 (기본값: LocalLifecycleOwner)
 * @param minActiveState 이벤트를 수집할 최소 Lifecycle 상태 (기본값: STARTED)
 * @param onEvent 이벤트 발생 시 호출되는 콜백
 */
@Composable
fun <T> Flow<T>.CollectAsEvent(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEvent: (T) -> Unit
) {
    LaunchedEffect(lifecycleOwner, this) {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            collect { event ->
                onEvent(event)
            }
        }
    }
}

/**
 * 단순 이벤트 수집 확장 함수 (Lifecycle 무관)
 *
 * Lifecycle을 고려하지 않고 단순히 이벤트를 수집합니다.
 * 간단한 케이스나 테스트에서 사용합니다.
 *
 * @param onEvent 이벤트 발생 시 호출되는 콜백
 */
@Composable
fun <T> Flow<T>.CollectAsEventSimple(onEvent: (T) -> Unit) {
    LaunchedEffect(Unit) {
        collect { event ->
            onEvent(event)
        }
    }
}

/**
 * Main 디스패처에서 이벤트를 처리하는 확장 함수
 *
 * UI 작업이 필요한 이벤트 처리 시 사용합니다.
 *
 * @param lifecycleOwner Lifecycle 소유자
 * @param onEvent 이벤트 발생 시 Main 스레드에서 호출되는 콜백
 */
@Composable
fun <T> Flow<T>.CollectAsEventOnMain(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (T) -> Unit
) {
    LaunchedEffect(lifecycleOwner, this) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { event ->
                withContext(Dispatchers.Main.immediate) {
                    onEvent(event)
                }
            }
        }
    }
}

/**
 * Lifecycle-aware 이벤트 수집 확장 함수 (소문자 버전)
 *
 * CollectAsEvent의 소문자 별칭입니다.
 */
@Composable
fun <T> Flow<T>.collectAsEvent(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEvent: (T) -> Unit
) {
    CollectAsEvent(lifecycleOwner, minActiveState, onEvent)
}
