package kr.hs.jung.example.domain.model

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
 */
enum class LoginProvider(val value: String) {
    EMAIL("email"),
    GOOGLE("google"),
    APPLE("apple")
}

/**
 * SNS OAuth 제공자 타입
 *
 * OAuth 인증에 사용 가능한 SNS 제공자입니다.
 */
enum class SnsProvider(val value: String) {
    GOOGLE("google"),
    APPLE("apple")
}
