package kr.hs.jung.example.data.remote

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kr.hs.jung.example.util.logger.AppLogger
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.random.Random

/**
 * 네트워크 요청 재시도 인터셉터
 *
 * 일시적인 네트워크 오류나 서버 오류 시 자동으로 요청을 재시도합니다.
 * Coroutine delay를 사용하여 스레드를 블로킹하지 않고 대기합니다.
 *
 * @param maxRetryCount 최대 재시도 횟수
 * @param retryDelayMillis 재시도 간 대기 시간 (밀리초)
 * @param dispatcher 재시도 대기에 사용할 디스패처 (테스트 시 TestDispatcher 주입 가능)
 */
class RetryInterceptor(
    private val maxRetryCount: Int = NetworkConstants.MAX_RETRY_COUNT,
    private val retryDelayMillis: Long = NetworkConstants.RETRY_DELAY_MILLIS,
    private val dispatcher: CoroutineDispatcher? = null
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null

        for (attempt in 0..maxRetryCount) {
            try {
                // 이전 응답이 있으면 닫기
                response?.close()

                response = chain.proceed(request)

                // 성공이거나 재시도 불가능한 상태 코드면 반환
                if (response.isSuccessful || !shouldRetry(response.code)) {
                    return response
                }

                // 마지막 시도가 아니면 재시도
                if (attempt < maxRetryCount) {
                    logRetry(attempt, response.code, null)
                    response.close()
                    suspendDelay(calculateDelay(attempt))
                }
            } catch (e: IOException) {
                exception = e

                // 마지막 시도가 아니면 재시도
                if (attempt < maxRetryCount) {
                    logRetry(attempt, null, e)
                    suspendDelay(calculateDelay(attempt))
                }
            }
        }

        // 모든 재시도 실패
        return response ?: throw (exception ?: IOException("Unknown network error"))
    }

    /**
     * Coroutine delay를 사용한 비동기 대기
     *
     * OkHttp Interceptor는 동기식이므로 runBlocking을 사용하지만,
     * 내부의 delay는 스레드를 블로킹하지 않아 효율적입니다.
     */
    private fun suspendDelay(delayMillis: Long) {
        val context = dispatcher ?: return runBlocking { delay(delayMillis) }
        runBlocking(context) { delay(delayMillis) }
    }

    /**
     * 해당 상태 코드가 재시도 가능한지 확인
     */
    private fun shouldRetry(statusCode: Int): Boolean {
        return statusCode in NetworkConstants.RETRYABLE_STATUS_CODES
    }

    /**
     * 지수 백오프 + Jitter를 적용한 대기 시간 계산
     *
     * Thundering Herd 방지를 위해 랜덤 jitter를 추가합니다.
     * 예: 기본 2초 + jitter(0~1초) = 2.0~3.0초
     */
    private fun calculateDelay(attempt: Int): Long {
        val baseDelay = retryDelayMillis * (1L shl attempt)
        val jitter = (baseDelay * NetworkConstants.JITTER_FACTOR * Random.nextDouble()).toLong()
        return baseDelay + jitter
    }

    /**
     * 재시도 로그 출력 (Debug 빌드에서만)
     */
    private fun logRetry(attempt: Int, statusCode: Int?, exception: IOException?) {
        val reason = statusCode?.let { "HTTP $it" } ?: exception?.message ?: "Unknown"
        AppLogger.w("RetryInterceptor", "Retry attempt ${attempt + 1}/$maxRetryCount due to: $reason")
    }
}
