package kr.hs.jung.example.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 사용자 정보 모델
 *
 * 인증된 사용자의 정보를 담는 도메인 모델입니다.
 *
 * @property idx 사용자 고유 식별자
 * @property name 사용자 이름
 * @property email 이메일 주소
 * @property picture 프로필 이미지 URL (선택)
 * @property provider 로그인 제공자 (EMAIL, GOOGLE, APPLE)
 * @property maxSessions 최대 허용 세션 수
 */
data class User(
    val idx: Int,
    val name: String,
    val email: String,
    val picture: String? = null,
    val provider: LoginProvider? = null,
    val maxSessions: Int? = null
)

/**
 * 로그인 제공자 타입
 *
 * 사용자가 가입/로그인한 방식을 나타냅니다.
 * kotlinx.serialization에서 value 값으로 직렬화됩니다.
 *
 * @property value 서버 API에서 사용하는 문자열 값
 * @property isOAuth OAuth 기반 소셜 로그인 제공자 여부
 */
@Serializable
enum class LoginProvider(val value: String, val isOAuth: Boolean = false) {
    @SerialName("email") EMAIL("email"),
    @SerialName("google") GOOGLE("google", isOAuth = true),
    @SerialName("apple") APPLE("apple", isOAuth = true);

    companion object {
        /** OAuth 소셜 로그인 제공자 목록 */
        val oAuthProviders: List<LoginProvider>
            get() = entries.filter { it.isOAuth }
    }
}
