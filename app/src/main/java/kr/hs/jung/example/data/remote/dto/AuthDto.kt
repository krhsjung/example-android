package kr.hs.jung.example.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kr.hs.jung.example.domain.model.LoginProvider
import kr.hs.jung.example.domain.model.User

/**
 * 인증 관련 Request/Response DTO
 *
 * 서버 API와의 통신에 사용되는 데이터 전송 객체들입니다.
 * kotlinx.serialization을 사용하여 JSON 직렬화/역직렬화를 수행합니다.
 */

// ===== Request DTOs =====

/**
 * 로그인 요청 DTO
 *
 * @property email 사용자 이메일
 * @property password 해싱된 비밀번호
 * @property provider 로그인 제공자 (기본값: "email")
 */
@Serializable
data class LoginRequestDto(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("provider")
    val provider: String = "email"
)

/**
 * 회원가입 요청 DTO
 *
 * @property email 사용자 이메일
 * @property password 해싱된 비밀번호
 * @property name 사용자 이름
 * @property provider 로그인 제공자 (기본값: "email")
 */
@Serializable
data class SignUpRequestDto(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("name")
    val name: String,
    @SerialName("provider")
    val provider: String = "email"
)

/**
 * OAuth 코드 교환 요청 DTO
 *
 * @property code OAuth 인증 후 받은 인가 코드
 */
@Serializable
data class ExchangeRequestDto(
    @SerialName("code")
    val code: String
)

/**
 * 토큰 갱신 요청 DTO
 *
 * @property refreshToken 갱신에 사용할 리프레시 토큰
 */
@Serializable
data class RefreshTokenRequestDto(
    @SerialName("refreshToken")
    val refreshToken: String
)

// ===== Response DTOs =====

/**
 * 인증 응답 DTO
 *
 * 로그인/회원가입/OAuth 교환 시 반환되는 JWT 토큰 + 사용자 정보입니다.
 *
 * @property accessToken API 인증에 사용되는 액세스 토큰
 * @property refreshToken 토큰 갱신에 사용되는 리프레시 토큰
 * @property user 사용자 정보
 */
@Serializable
data class AuthResponseDto(
    @SerialName("accessToken")
    val accessToken: String,
    @SerialName("refreshToken")
    val refreshToken: String,
    @SerialName("user")
    val user: UserDto
)

/**
 * 사용자 응답 DTO
 *
 * 서버에서 반환하는 사용자 정보입니다.
 * toDomain() 메서드로 Domain 모델인 User로 변환합니다.
 */
@Serializable
data class UserDto(
    @SerialName("idx")
    val idx: Int,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("picture")
    val picture: String? = null,
    @SerialName("provider")
    val provider: String? = null,
    @SerialName("maxSessions")
    val maxSessions: Int? = null
) {
    fun toDomain(): User = User(
        idx = idx,
        name = name,
        email = email,
        picture = picture,
        provider = provider?.let {
            LoginProvider.entries.find { p -> p.value == it }
        },
        maxSessions = maxSessions
    )
}
