package kr.hs.jung.example.data.remote

import kotlinx.serialization.json.Json
import kr.hs.jung.example.domain.event.SessionManager
import kr.hs.jung.example.data.remote.dto.ServerErrorResponseDto
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.domain.model.AppException
import kr.hs.jung.example.util.logger.AppLogger
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API 호출을 안전하게 래핑하는 유틸리티 클래스
 *
 * 네트워크 에러, 서버 에러를 AppError로 변환하여 Result로 반환합니다.
 * Domain 레이어에서 플랫폼 독립적인 AppError를 사용할 수 있도록 합니다.
 *
 * 강화된 에러 처리:
 * - 401 Unauthorized: 세션 만료 이벤트 발생 → 자동 로그아웃 트리거
 * - 403 Forbidden: 접근 거부 이벤트 발생
 * - 429 Too Many Requests: Rate Limit 초과, Retry-After 헤더 파싱
 */
@Singleton
class SafeApiCall @Inject constructor(
    private val json: Json,
    private val sessionManager: SessionManager
) {

    /**
     * API 호출을 실행하고 결과를 Result<T>로 반환
     *
     * @param apiCall 실행할 suspend API 호출
     * @return Result<T> 성공 시 응답 body, 실패 시 AppException(AppError)
     */
    suspend operator fun <T> invoke(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(AppException(AppError.Network.NoData))
                }
            } else {
                Result.failure(AppException(parseError(response)))
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network Error: ${e.message}", e)
            Result.failure(AppException(mapException(e)))
        }
    }

    /**
     * API 호출을 실행하고 Result<Unit>으로 반환 (응답 body 무시)
     *
     * @param apiCall 실행할 suspend API 호출
     * @return Result<Unit> 성공 시 Unit, 실패 시 AppException(AppError)
     */
    suspend fun <T> ignoreBody(apiCall: suspend () -> Response<T>): Result<Unit> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(AppException(parseError(response)))
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network Error: ${e.message}", e)
            Result.failure(AppException(mapException(e)))
        }
    }

    /**
     * HTTP 에러 응답을 AppError로 변환
     *
     * 특수 상태 코드(401, 403, 429)는 SessionManager를 통해 이벤트를 발생시킵니다.
     */
    private fun <T> parseError(response: Response<T>): AppError {
        val statusCode = response.code()

        // HTTP 상태 코드 기반 에러 처리
        when (statusCode) {
            HTTP_UNAUTHORIZED -> {
                AppLogger.w(TAG, "401 Unauthorized - triggering session expired event")
                sessionManager.notifySessionExpired()
                return AppError.Auth.SessionExpired
            }
            HTTP_FORBIDDEN -> {
                AppLogger.w(TAG, "403 Forbidden - triggering access denied event")
                sessionManager.notifyAccessDenied()
                return AppError.Auth.Forbidden
            }
            HTTP_TOO_MANY_REQUESTS -> {
                val retryAfterSeconds = parseRetryAfterHeader(response)
                AppLogger.w(TAG, "429 Too Many Requests - retry after: $retryAfterSeconds seconds")
                sessionManager.notifyRateLimited(retryAfterSeconds)
                return AppError.Network.RateLimited(retryAfterSeconds)
            }
        }

        // 에러 바디 파싱
        val errorBody = response.errorBody()?.string()
        val errorResponse = try {
            errorBody?.let { json.decodeFromString<ServerErrorResponseDto>(it) }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error parsing error response: ${e.message}", e)
            null
        }

        AppLogger.e(TAG, "Server Error: ${errorResponse?.debugDescription}")

        return AppError.Network.Server(
            statusCode = statusCode,
            errorCode = errorResponse?.code,
            serverMessage = errorResponse?.message
        )
    }

    /**
     * Retry-After 헤더 파싱
     *
     * RFC 7231에 따라 Retry-After 헤더는 두 가지 형식을 지원:
     * - 초 단위 정수 (예: "120")
     * - HTTP-date 형식 (예: "Wed, 21 Oct 2015 07:28:00 GMT") - 현재 미지원
     *
     * @return 재시도까지 대기해야 할 시간(초), 파싱 실패 시 null
     */
    private fun <T> parseRetryAfterHeader(response: Response<T>): Long? {
        val retryAfter = response.headers()[HEADER_RETRY_AFTER] ?: return null

        return try {
            retryAfter.toLongOrNull()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to parse Retry-After header: $retryAfter")
            null
        }
    }

    /**
     * Exception을 AppError로 변환
     */
    private fun mapException(e: Exception): AppError {
        return when (e) {
            is AppException -> e.appError
            is SocketTimeoutException -> AppError.Network.Timeout
            is UnknownHostException -> AppError.Network.NoConnection
            is java.io.IOException -> AppError.Network.NoConnection
            else -> AppError.Network.Unknown(e)
        }
    }

    companion object {
        private const val TAG = "SafeApiCall"

        // HTTP 상태 코드
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_TOO_MANY_REQUESTS = 429

        // HTTP 헤더
        private const val HEADER_RETRY_AFTER = "Retry-After"
    }
}
