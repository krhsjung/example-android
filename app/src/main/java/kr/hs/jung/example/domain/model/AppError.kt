package kr.hs.jung.example.domain.model

/**
 * 앱 전체에서 사용하는 통합 에러 타입
 *
 * Domain 레이어에 위치하며 플랫폼 독립적입니다.
 * 에러 메시지 표시는 UI 레이어의 ErrorMessageResolver에서 처리합니다.
 *
 * 사용 흐름:
 * Throwable → AppError (Data 레이어) → ViewModel → UI (ErrorMessageResolver로 문자열 변환)
 */
sealed class AppError {

    /**
     * 네트워크 관련 에러
     */
    sealed class Network : AppError() {
        /** 응답 데이터가 없음 */
        data object NoData : Network()

        /** 잘못된 URL */
        data object InvalidUrl : Network()

        /** 서버 에러 */
        data class Server(
            val statusCode: Int,
            val errorCode: String? = null,
            val serverMessage: String? = null
        ) : Network()

        /** 디코딩 에러 */
        data class Decoding(val cause: Throwable) : Network()

        /** 인코딩 에러 */
        data class Encoding(val cause: Throwable) : Network()

        /** 연결 타임아웃 */
        data object Timeout : Network()

        /** 네트워크 연결 없음 */
        data object NoConnection : Network()

        /**
         * Rate Limit 초과 (429)
         * @param retryAfterSeconds Retry-After 헤더 값 (초 단위), null이면 기본값 사용
         */
        data class RateLimited(val retryAfterSeconds: Long? = null) : Network()

        /** 알 수 없는 네트워크 에러 */
        data class Unknown(val cause: Throwable) : Network()
    }

    /**
     * 인증 관련 에러
     */
    sealed class Auth : AppError() {
        /** 인증 필요 (401) */
        data object Unauthorized : Auth()

        /** 권한 없음 (403) */
        data object Forbidden : Auth()

        /** 세션 만료 */
        data object SessionExpired : Auth()

        /** 잘못된 자격 증명 */
        data object InvalidCredentials : Auth()
    }

    /**
     * 검증 에러 (폼 입력 등)
     */
    sealed class Validation : AppError() {
        data object EmptyEmail : Validation()
        data object InvalidEmail : Validation()
        data object EmptyPassword : Validation()
        data object WeakPassword : Validation()
        data object PasswordMismatch : Validation()
        data object EmptyUsername : Validation()
        data object NameTooShort : Validation()
        data object PasswordNoUppercase : Validation()
        data object PasswordNoLowercase : Validation()
        data object PasswordNoNumber : Validation()
        data object PasswordNoSpecialCharacter : Validation()
        data class Custom(val field: String, val reason: String) : Validation()
    }

    /**
     * 비즈니스 로직 에러
     */
    sealed class Business : AppError() {
        data object UserNotFound : Business()
        data object DuplicateUser : Business()
        data class Custom(val code: String, val message: String? = null) : Business()
    }

    /**
     * 일반적인 에러
     */
    data class Generic(
        val message: String? = null,
        val cause: Throwable? = null
    ) : AppError()

    companion object {
        /**
         * Throwable을 AppError로 변환
         */
        fun from(error: Throwable): AppError {
            return when {
                error is AppError -> error
                error.cause is AppError -> error.cause as AppError
                else -> Generic(message = error.message, cause = error)
            }
        }
    }
}

/**
 * AppError를 Throwable로 래핑
 *
 * Result<T>와 함께 사용할 때 유용합니다.
 */
class AppException(val appError: AppError) : Exception(appError.toString())

/**
 * Result<T>에서 AppError 추출
 */
fun <T> Result<T>.appError(): AppError? {
    return exceptionOrNull()?.appError()
}

/**
 * Throwable에서 AppError 추출
 */
fun Throwable.appError(): AppError {
    return when (this) {
        is AppException -> appError
        else -> AppError.from(this)
    }
}
