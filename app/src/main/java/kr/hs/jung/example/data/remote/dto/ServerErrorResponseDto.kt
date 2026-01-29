package kr.hs.jung.example.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 서버 에러 응답 DTO
 *
 * 서버에서 반환하는 에러 응답을 파싱합니다.
 * 플랫폼 독립적으로 유지하며, 문자열 리소스 매핑은 UI 레이어에서 처리합니다.
 */
@Serializable
data class ServerErrorResponseDto(
    @SerialName("error")
    val error: String,
    @SerialName("statusCode")
    val statusCode: Int,
    @SerialName("message")
    val messageString: String
) {
    private val parsedMessage: ErrorMessageDto?
        get() = try {
            Json.decodeFromString<ErrorMessageDto>(messageString)
        } catch (e: Exception) {
            null
        }

    /** 에러 코드 (다국어 키로 사용) */
    val code: String
        get() = parsedMessage?.id ?: "unknown_error"

    /** 에러 메시지 */
    val message: String
        get() = parsedMessage?.message ?: messageString

    /** 추가 파라미터 (예: 필드명) */
    val params: Map<String, String>?
        get() = parsedMessage?.params

    val debugDescription: String
        get() = """
            ErrorResponse:
            - Status Code: $statusCode
            - Error: $error
            - Code: $code
            - Message: $message
            - Params: ${params?.toString() ?: "null"}
            - Raw Message: $messageString
        """.trimIndent()
}

/**
 * 에러 메시지 DTO
 *
 * @property id 에러 ID (다국어 키로 사용)
 * @property message 에러 메시지
 * @property params 추가 파라미터 (예: 필드명)
 */
@Serializable
data class ErrorMessageDto(
    @SerialName("id")
    val id: String,
    @SerialName("message")
    val message: String,
    @SerialName("params")
    val params: Map<String, String>? = null
)
