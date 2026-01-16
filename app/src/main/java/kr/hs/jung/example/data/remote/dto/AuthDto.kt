package kr.hs.jung.example.data.remote.dto

import com.google.gson.annotations.SerializedName
import kr.hs.jung.example.domain.model.LoginProvider
import kr.hs.jung.example.domain.model.User

// Request DTOs
data class LoginRequestDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("provider")
    val provider: String = "email"
)

data class SignUpRequestDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("provider")
    val provider: String = "email"
)

data class ExchangeRequestDto(
    @SerializedName("code")
    val code: String
)

// Response DTOs
data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("picture")
    val picture: String? = null,
    @SerializedName("provider")
    val provider: String? = null,
    @SerializedName("max_sessions")
    val maxSessions: Int? = null
) {
    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        picture = picture,
        provider = provider?.let {
            LoginProvider.entries.find { p -> p.value == it }
        },
        maxSessions = maxSessions
    )
}
