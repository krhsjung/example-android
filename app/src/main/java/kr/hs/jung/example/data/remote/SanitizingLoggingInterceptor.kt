package kr.hs.jung.example.data.remote

import kr.hs.jung.example.util.logger.AppLogger
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer

/**
 * 민감 정보를 마스킹하는 네트워크 로깅 인터셉터
 *
 * HttpLoggingInterceptor와 달리, JSON Body의 password/token 등
 * 민감한 필드 값과 인증 관련 헤더를 마스킹 처리하여 로그에 출력합니다.
 */
class SanitizingLoggingInterceptor : Interceptor {

    companion object {
        private const val TAG = "Network"
        private const val MASK = "***"

        /** 마스킹 대상 헤더 (대소문자 무시) */
        private val SENSITIVE_HEADERS = setOf(
            "authorization",
            "cookie",
            "set-cookie",
            "proxy-authorization"
        )

        /** 마스킹 대상 JSON 필드명 패턴 */
        private val SENSITIVE_BODY_PATTERN = Regex(
            """("(?:password|token|refreshToken|accessToken|secret|credential)")\s*:\s*"[^"]*"""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Request 로깅
        AppLogger.d(TAG, "--> ${request.method} ${request.url}")
        logHeaders(request.headers)
        request.body?.let { logRequestBody(it) }

        val startTime = System.nanoTime()
        val response = chain.proceed(request)
        val duration = (System.nanoTime() - startTime) / 1_000_000

        // Response 로깅
        AppLogger.d(TAG, "<-- ${response.code} ${request.url} (${duration}ms)")
        logHeaders(response.headers)
        logResponseBody(response)

        return response
    }

    private fun logHeaders(headers: okhttp3.Headers) {
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            val value = if (name.lowercase() in SENSITIVE_HEADERS) MASK else headers.value(i)
            AppLogger.d(TAG, "$name: $value")
        }
    }

    private fun logRequestBody(body: RequestBody) {
        try {
            val buffer = Buffer()
            body.writeTo(buffer)
            val bodyString = buffer.readUtf8()
            AppLogger.d(TAG, sanitizeBody(bodyString))
        } catch (e: Exception) {
            AppLogger.d(TAG, "[Body logging failed: ${e.message}]")
        }
    }

    private fun logResponseBody(response: Response) {
        try {
            val body = response.peekBody(Long.MAX_VALUE)
            val bodyString = body.string()
            AppLogger.d(TAG, sanitizeBody(bodyString))
        } catch (e: Exception) {
            AppLogger.d(TAG, "[Body logging failed: ${e.message}]")
        }
    }

    /**
     * JSON 문자열에서 민감 필드의 값을 마스킹합니다.
     *
     * 예: {"password":"secret123"} → {"password":"***"}
     */
    private fun sanitizeBody(body: String): String {
        return SENSITIVE_BODY_PATTERN.replace(body) { match ->
            "${match.groupValues[1]}:\"$MASK\""
        }
    }
}
