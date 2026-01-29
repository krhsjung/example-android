@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package kr.hs.jung.example.util

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Flow 테스트를 위한 컬렉터
 *
 * Flow의 모든 emission을 수집하고 검증할 수 있습니다.
 * 특히 StateFlow의 상태 변화 히스토리를 추적하는 데 유용합니다.
 *
 * 사용법:
 * ```
 * @Test
 * fun `test state changes`() = runTest {
 *     val collector = viewModel.uiState.testCollector(this)
 *
 *     viewModel.doSomething()
 *     advanceUntilIdle()
 *
 *     collector.assertValues(
 *         { it.isLoading == false },
 *         { it.isLoading == true },
 *         { it.isLoading == false && it.data != null }
 *     )
 *
 *     collector.cancel()
 * }
 * ```
 */
class FlowTestCollector<T>(
    private val flow: Flow<T>,
    scope: CoroutineScope
) {
    private val _values = mutableListOf<T>()
    val values: List<T> get() = _values.toList()

    private val job: Job = scope.launch(UnconfinedTestDispatcher()) {
        flow.collect { _values.add(it) }
    }

    /**
     * 수집을 중지합니다.
     */
    fun cancel() {
        job.cancel()
    }

    /**
     * 수집된 값의 개수를 반환합니다.
     */
    val size: Int get() = _values.size

    /**
     * 마지막으로 수집된 값을 반환합니다.
     */
    val lastValue: T get() = _values.last()

    /**
     * 첫 번째로 수집된 값을 반환합니다.
     */
    val firstValue: T get() = _values.first()

    /**
     * 수집된 값들이 지정된 조건들을 순서대로 만족하는지 검증합니다.
     */
    fun assertValues(vararg predicates: (T) -> Boolean) {
        assertThat(_values.size).isAtLeast(predicates.size)
        predicates.forEachIndexed { index, predicate ->
            if (!predicate(_values[index])) {
                throw AssertionError("Value at index $index (${_values[index]}) did not match predicate")
            }
        }
    }

    /**
     * 수집된 값들이 정확히 지정된 값들과 일치하는지 검증합니다.
     */
    fun assertExactValues(vararg expected: T) {
        assertThat(_values).containsExactlyElementsIn(expected.toList()).inOrder()
    }

    /**
     * 마지막 값이 조건을 만족하는지 검증합니다.
     */
    fun assertLastValue(predicate: (T) -> Boolean) {
        if (!predicate(lastValue)) {
            throw AssertionError("Last value ($lastValue) did not match predicate")
        }
    }

    /**
     * 수집된 값의 개수가 예상과 일치하는지 검증합니다.
     */
    fun assertValueCount(expected: Int) {
        assertThat(_values.size).isEqualTo(expected)
    }

    /**
     * 특정 조건을 만족하는 값이 있는지 검증합니다.
     */
    fun assertAnyValue(predicate: (T) -> Boolean) {
        if (!_values.any(predicate)) {
            throw AssertionError("No value matched the predicate. Values: $_values")
        }
    }

    /**
     * 모든 값이 조건을 만족하는지 검증합니다.
     */
    fun assertAllValues(predicate: (T) -> Boolean) {
        val failedValues = _values.filterNot(predicate)
        if (failedValues.isNotEmpty()) {
            throw AssertionError("Not all values matched the predicate. Failed values: $failedValues")
        }
    }

    /**
     * 수집된 값들을 필터링합니다.
     */
    fun filter(predicate: (T) -> Boolean): List<T> = _values.filter(predicate)

    /**
     * 수집된 값들에서 특정 프로퍼티만 추출합니다.
     */
    fun <R> map(transform: (T) -> R): List<R> = _values.map(transform)
}

/**
 * Flow에서 테스트 컬렉터를 생성합니다.
 */
fun <T> Flow<T>.testCollector(scope: CoroutineScope): FlowTestCollector<T> {
    return FlowTestCollector(this, scope)
}

/**
 * StateFlow에서 테스트 컬렉터를 생성합니다.
 * StateFlow는 항상 현재 값을 가지므로 첫 번째 값은 초기 상태입니다.
 */
fun <T> StateFlow<T>.testCollector(scope: CoroutineScope): FlowTestCollector<T> {
    return FlowTestCollector(this, scope)
}

/**
 * TestScope 확장 - Flow 값들을 수집합니다.
 */
suspend fun <T> Flow<T>.collectValues(scope: TestScope): List<T> {
    val values = mutableListOf<T>()
    val job = scope.launch(UnconfinedTestDispatcher()) {
        toList(values)
    }
    job.cancel()
    return values
}
