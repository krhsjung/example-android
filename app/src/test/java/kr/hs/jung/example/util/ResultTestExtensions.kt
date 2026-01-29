package kr.hs.jung.example.util

import com.google.common.truth.Truth.assertThat

/**
 * Result 테스트를 위한 확장 함수
 *
 * Kotlin Result 타입에 대한 테스트 검증을 쉽게 할 수 있는 유틸리티를 제공합니다.
 *
 * 사용법:
 * ```
 * @Test
 * fun `test success result`() = runTest {
 *     val result = repository.fetchData()
 *
 *     result.assertSuccess { data ->
 *         assertThat(data.name).isEqualTo("expected")
 *     }
 * }
 *
 * @Test
 * fun `test failure result`() = runTest {
 *     val result = repository.fetchData()
 *
 *     result.assertFailure<NetworkException>()
 * }
 * ```
 */

/**
 * Result가 성공인지 검증하고, 성공 값에 대한 추가 검증을 수행합니다.
 *
 * @param assertion 성공 값에 대한 검증 (선택적)
 */
inline fun <T> Result<T>.assertSuccess(assertion: (T) -> Unit = {}) {
    if (!isSuccess) {
        throw AssertionError("Expected success but was failure: ${exceptionOrNull()}")
    }
    assertion(getOrThrow())
}

/**
 * Result가 성공이고 값이 예상 값과 일치하는지 검증합니다.
 *
 * @param expected 예상 값
 */
fun <T> Result<T>.assertSuccessEquals(expected: T) {
    if (!isSuccess) {
        throw AssertionError("Expected success but was failure: ${exceptionOrNull()}")
    }
    assertThat(getOrThrow()).isEqualTo(expected)
}

/**
 * Result가 성공이고 값이 null이 아닌지 검증합니다.
 */
fun <T> Result<T>.assertSuccessNotNull() {
    if (!isSuccess) {
        throw AssertionError("Expected success but was failure: ${exceptionOrNull()}")
    }
    assertThat(getOrNull()).isNotNull()
}

/**
 * Result가 실패인지 검증합니다.
 *
 * @param assertion 예외에 대한 추가 검증 (선택적)
 */
inline fun <T> Result<T>.assertFailure(assertion: (Throwable) -> Unit = {}) {
    if (!isFailure) {
        throw AssertionError("Expected failure but was success: ${getOrNull()}")
    }
    assertion(exceptionOrNull()!!)
}

/**
 * Result가 특정 타입의 예외로 실패했는지 검증합니다.
 *
 * @param E 예상 예외 타입
 * @param assertion 예외에 대한 추가 검증 (선택적)
 */
inline fun <T, reified E : Throwable> Result<T>.assertFailureOfType(
    assertion: (E) -> Unit = {}
) {
    if (!isFailure) {
        throw AssertionError("Expected failure but was success: ${getOrNull()}")
    }

    val exception = exceptionOrNull()
    assertThat(exception).isInstanceOf(E::class.java)
    assertion(exception as E)
}

/**
 * Result가 특정 메시지를 포함한 예외로 실패했는지 검증합니다.
 *
 * @param expectedMessage 예상 메시지 (부분 일치)
 */
fun <T> Result<T>.assertFailureWithMessage(expectedMessage: String) {
    if (!isFailure) {
        throw AssertionError("Expected failure but was success: ${getOrNull()}")
    }

    val exception = exceptionOrNull()
    assertThat(exception?.message).contains(expectedMessage)
}

/**
 * Result의 성공 값을 가져옵니다. 실패 시 테스트가 실패합니다.
 *
 * @return 성공 값
 */
fun <T> Result<T>.getOrFail(): T {
    if (!isSuccess) {
        throw AssertionError("Expected success but was failure: ${exceptionOrNull()}")
    }
    return getOrThrow()
}

/**
 * Result의 실패 예외를 가져옵니다. 성공 시 테스트가 실패합니다.
 *
 * @return 실패 예외
 */
fun <T> Result<T>.getExceptionOrFail(): Throwable {
    if (!isFailure) {
        throw AssertionError("Expected failure but was success: ${getOrNull()}")
    }
    return exceptionOrNull()!!
}

/**
 * Result가 성공이고 특정 프로퍼티가 예상 값과 일치하는지 검증합니다.
 *
 * @param selector 프로퍼티 선택자
 * @param expected 예상 값
 */
fun <T, R> Result<T>.assertSuccessProperty(
    selector: (T) -> R,
    expected: R
) {
    if (!isSuccess) {
        throw AssertionError("Expected success but was failure: ${exceptionOrNull()}")
    }
    assertThat(selector(getOrThrow())).isEqualTo(expected)
}

/**
 * Result가 성공이고 특정 프로퍼티가 null인지 검증합니다.
 *
 * @param selector 프로퍼티 선택자
 */
fun <T, R> Result<T>.assertSuccessPropertyIsNull(selector: (T) -> R?) {
    if (!isSuccess) {
        throw AssertionError("Expected success but was failure: ${exceptionOrNull()}")
    }
    assertThat(selector(getOrThrow())).isNull()
}

/**
 * Result가 성공이고 특정 프로퍼티가 null이 아닌지 검증합니다.
 *
 * @param selector 프로퍼티 선택자
 */
fun <T, R> Result<T>.assertSuccessPropertyIsNotNull(selector: (T) -> R?) {
    if (!isSuccess) {
        throw AssertionError("Expected success but was failure: ${exceptionOrNull()}")
    }
    assertThat(selector(getOrThrow())).isNotNull()
}

/**
 * Result 리스트에서 모든 항목이 성공인지 검증합니다.
 */
fun <T> List<Result<T>>.assertAllSuccess() {
    forEachIndexed { index, result ->
        if (!result.isSuccess) {
            throw AssertionError("Result at index $index was failure: ${result.exceptionOrNull()}")
        }
    }
}

/**
 * Result 리스트에서 모든 항목이 실패인지 검증합니다.
 */
fun <T> List<Result<T>>.assertAllFailure() {
    forEachIndexed { index, result ->
        if (!result.isFailure) {
            throw AssertionError("Result at index $index was success: ${result.getOrNull()}")
        }
    }
}

/**
 * Result 리스트에서 성공 개수를 검증합니다.
 */
fun <T> List<Result<T>>.assertSuccessCount(expected: Int) {
    val successCount = count { it.isSuccess }
    if (successCount != expected) {
        throw AssertionError("Expected $expected successes but found $successCount")
    }
}

/**
 * Result 리스트에서 실패 개수를 검증합니다.
 */
fun <T> List<Result<T>>.assertFailureCount(expected: Int) {
    val failureCount = count { it.isFailure }
    if (failureCount != expected) {
        throw AssertionError("Expected $expected failures but found $failureCount")
    }
}
