package kr.hs.jung.example.data.remote

import kr.hs.jung.example.data.remote.dto.ErrorResponseDto

sealed class NetworkException : Exception() {
    data object InvalidURL : NetworkException() {
        override val message: String = "잘못된 URL입니다"
    }

    data object NoData : NetworkException() {
        override val message: String = "데이터가 없습니다"
    }

    data class DecodingError(val error: Throwable) : NetworkException() {
        override val message: String = "데이터 디코딩 오류: ${error.message}"
    }

    data class EncodingError(val error: Throwable) : NetworkException() {
        override val message: String = "데이터 인코딩 오류: ${error.message}"
    }

    data class ServerError(
        val statusCode: Int,
        val errorResponse: ErrorResponseDto?
    ) : NetworkException() {
        override val message: String
            get() = errorResponse?.localizedMessage ?: "서버 에러: $statusCode"
    }

    data class Unknown(val error: Throwable) : NetworkException() {
        override val message: String = error.message ?: "알 수 없는 에러"
    }

    data class Custom(override val message: String) : NetworkException()
}
