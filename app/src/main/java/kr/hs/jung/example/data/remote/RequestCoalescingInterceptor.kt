package kr.hs.jung.example.data.remote

import kr.hs.jung.example.util.logger.AppLogger
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GET 요청 중복 방지 인터셉터 (Request Coalescing)
 *
 * 동일한 URL에 대한 GET 요청이 동시에 발생하면 하나의 네트워크 호출만 수행하고
 * 모든 호출자에게 동일한 응답을 반환합니다.
 *
 * - GET 요청만 대상 (POST, PUT 등은 그대로 통과)
 * - URL 문자열을 키로 사용하여 중복 판별
 * - ConcurrentHashMap + CountDownLatch로 thread-safe 동기화
 */
@Singleton
class RequestCoalescingInterceptor @Inject constructor() : Interceptor {

    private class InFlightRequest {
        val latch = CountDownLatch(1)
        var cachedResponse: CachedResponse? = null
        var exception: IOException? = null
    }

    private class CachedResponse(
        val code: Int,
        val message: String,
        val headers: okhttp3.Headers,
        val bodyBytes: ByteArray?,
        val contentType: MediaType?,
        val protocol: Protocol
    )

    private val inFlightRequests = ConcurrentHashMap<String, InFlightRequest>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // GET 요청만 coalescing 적용
        if (request.method != "GET") {
            return chain.proceed(request)
        }

        val key = request.url.toString()
        val newInFlight = InFlightRequest()
        val existing = inFlightRequests.putIfAbsent(key, newInFlight)

        if (existing != null) {
            // 동일 URL에 대한 in-flight 요청이 있으면 대기
            AppLogger.d(TAG, "Request coalesced: $key")
            existing.latch.await()
            existing.exception?.let { throw it }
            return existing.cachedResponse!!.toResponse(request)
        }

        // 첫 번째 요청: 네트워크 호출 수행
        try {
            val response = chain.proceed(request)
            val body = response.body
            val bodyBytes = body?.bytes()
            val contentType = body?.contentType()

            newInFlight.cachedResponse = CachedResponse(
                code = response.code,
                message = response.message,
                headers = response.headers,
                bodyBytes = bodyBytes,
                contentType = contentType,
                protocol = response.protocol
            )

            return newInFlight.cachedResponse!!.toResponse(request)
        } catch (e: IOException) {
            newInFlight.exception = e
            throw e
        } finally {
            newInFlight.latch.countDown()
            inFlightRequests.remove(key)
        }
    }

    private fun CachedResponse.toResponse(request: okhttp3.Request): Response {
        return Response.Builder()
            .request(request)
            .protocol(protocol)
            .code(code)
            .message(message)
            .headers(headers)
            .body(bodyBytes?.toResponseBody(contentType))
            .build()
    }

    companion object {
        private const val TAG = "RequestCoalescing"
    }
}
