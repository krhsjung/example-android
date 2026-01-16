package kr.hs.jung.example.data.remote.dto

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class ErrorResponseDto(
    @SerializedName("error")
    val error: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val messageString: String
) {
    private val parsedMessage: ErrorMessageDto?
        get() = try {
            Gson().fromJson(messageString, ErrorMessageDto::class.java)
        } catch (e: Exception) {
            null
        }

    val id: String
        get() = parsedMessage?.id ?: "unknown_error"

    val message: String
        get() = parsedMessage?.message ?: messageString

    val params: Map<String, String>?
        get() = parsedMessage?.params

    val localizedMessage: String
        get() = message

    val debugDescription: String
        get() = """
            ErrorResponse:
            - Status Code: $statusCode
            - Error: $error
            - ID: $id
            - Message: $message
            - Params: ${params?.toString() ?: "null"}
            - Raw Message: $messageString
        """.trimIndent()
}

private data class ErrorMessageDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("params")
    val params: Map<String, String>?
)
