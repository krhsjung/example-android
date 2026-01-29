package kr.hs.jung.example.util

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * StateFlow/SharedFlow 테스트를 위한 확장 함수
 *
 * 이 파일은 Flow 기반 테스트를 위한 다양한 확장 함수를 제공합니다:
 * - StateFlow 값 검증
 * - SharedFlow 이벤트 수집 및 검증
 * - Flow 상태 변화 추적
 */

/**
 * StateFlow의 현재 값이 조건을 만족하는지 검증합니다.
 */
fun <T> StateFlow<T>.assertValue(predicate: (T) -> Boolean) {
    if (!predicate(value)) {
        throw AssertionError("StateFlow value ($value) did not match predicate")
    }
}

/**
 * StateFlow의 현재 값이 예상 값과 일치하는지 검증합니다.
 */
fun <T> StateFlow<T>.assertValueEquals(expected: T) {
    assertThat(value).isEqualTo(expected)
}

/**
 * StateFlow의 특정 프로퍼티가 예상 값과 일치하는지 검증합니다.
 */
fun <T, R> StateFlow<T>.assertProperty(
    selector: (T) -> R,
    expected: R
) {
    assertThat(selector(value)).isEqualTo(expected)
}

/**
 * StateFlow의 특정 프로퍼티가 null인지 검증합니다.
 */
fun <T, R> StateFlow<T>.assertPropertyIsNull(selector: (T) -> R?) {
    assertThat(selector(value)).isNull()
}

/**
 * StateFlow의 특정 프로퍼티가 null이 아닌지 검증합니다.
 */
fun <T, R> StateFlow<T>.assertPropertyIsNotNull(selector: (T) -> R?) {
    assertThat(selector(value)).isNotNull()
}

/**
 * SharedFlow에서 이벤트를 수집하고 검증합니다.
 *
 * @param action 이벤트를 발생시키는 액션
 * @param assertion 수집된 이벤트에 대한 검증
 */
suspend fun <E> SharedFlow<E>.collectEvent(
    action: suspend () -> Unit,
    assertion: (E) -> Unit
) {
    test {
        action()
        assertion(awaitItem())
    }
}

/**
 * SharedFlow에서 이벤트가 발생하지 않았는지 검증합니다.
 *
 * @param action 이벤트를 발생시키지 않아야 하는 액션
 */
suspend fun <E> SharedFlow<E>.expectNoEvent(action: suspend () -> Unit) {
    test {
        action()
        expectNoEvents()
    }
}

/**
 * StateFlow의 여러 프로퍼티를 한번에 검증합니다.
 */
fun <T> StateFlow<T>.assertState(vararg assertions: (T) -> Boolean) {
    assertions.forEachIndexed { index, assertion ->
        if (!assertion(value)) {
            throw AssertionError("Assertion $index failed for state: $value")
        }
    }
}

/**
 * StateFlow의 현재 값에서 특정 프로퍼티가 리스트에 포함된 값인지 검증합니다.
 */
fun <T, R> StateFlow<T>.assertPropertyIn(
    selector: (T) -> R,
    expected: Collection<R>
) {
    assertThat(selector(value)).isIn(expected)
}

/**
 * StateFlow의 특정 Boolean 프로퍼티가 true인지 검증합니다.
 */
fun <T> StateFlow<T>.assertPropertyTrue(selector: (T) -> Boolean) {
    if (!selector(value)) {
        throw AssertionError("Expected true but was false for state: $value")
    }
}

/**
 * StateFlow의 특정 Boolean 프로퍼티가 false인지 검증합니다.
 */
fun <T> StateFlow<T>.assertPropertyFalse(selector: (T) -> Boolean) {
    if (selector(value)) {
        throw AssertionError("Expected false but was true for state: $value")
    }
}

/**
 * StateFlow의 특정 Collection 프로퍼티가 비어있는지 검증합니다.
 */
fun <T, R : Collection<*>> StateFlow<T>.assertPropertyEmpty(selector: (T) -> R) {
    assertThat(selector(value)).isEmpty()
}

/**
 * StateFlow의 특정 Collection 프로퍼티가 비어있지 않은지 검증합니다.
 */
fun <T, R : Collection<*>> StateFlow<T>.assertPropertyNotEmpty(selector: (T) -> R) {
    assertThat(selector(value)).isNotEmpty()
}

/**
 * StateFlow의 특정 Collection 프로퍼티의 크기를 검증합니다.
 */
fun <T, R : Collection<*>> StateFlow<T>.assertPropertySize(
    selector: (T) -> R,
    expectedSize: Int
) {
    assertThat(selector(value)).hasSize(expectedSize)
}

/**
 * SharedFlow에서 여러 이벤트를 순서대로 수집하고 검증합니다.
 *
 * @param count 수집할 이벤트 개수
 * @param action 이벤트를 발생시키는 액션
 * @param assertions 각 이벤트에 대한 검증 (순서대로)
 */
suspend fun <E> SharedFlow<E>.collectEvents(
    count: Int,
    action: suspend () -> Unit,
    vararg assertions: (E) -> Unit
) {
    require(assertions.size == count) { "Assertion count must match event count" }
    test {
        action()
        repeat(count) { index ->
            assertions[index](awaitItem())
        }
    }
}

/**
 * Flow에서 조건을 만족하는 첫 번째 값을 기다립니다.
 *
 * @param timeout 타임아웃 (기본 5초)
 * @param predicate 조건
 * @return 조건을 만족하는 첫 번째 값
 */
suspend fun <T> Flow<T>.awaitFirst(
    timeout: Duration = 5.seconds,
    predicate: (T) -> Boolean
): T = withTimeout(timeout) {
    first(predicate)
}

/**
 * StateFlow에서 FlowTestCollector를 생성하는 편의 함수입니다.
 * FlowTestCollector를 사용하면 모든 상태 변화 히스토리를 추적할 수 있습니다.
 *
 * @param scope 코루틴 스코프 (보통 TestScope)
 * @return FlowTestCollector 인스턴스
 */
fun <T> StateFlow<T>.collectHistory(scope: CoroutineScope): FlowTestCollector<T> {
    return FlowTestCollector(this, scope)
}

/**
 * Flow에서 FlowTestCollector를 생성하는 편의 함수입니다.
 *
 * @param scope 코루틴 스코프 (보통 TestScope)
 * @return FlowTestCollector 인스턴스
 */
fun <T> Flow<T>.collectHistory(scope: CoroutineScope): FlowTestCollector<T> {
    return FlowTestCollector(this, scope)
}
