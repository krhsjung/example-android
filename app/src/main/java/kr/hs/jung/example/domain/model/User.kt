package kr.hs.jung.example.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val picture: String? = null,
    val provider: LoginProvider? = null,
    val maxSessions: Int? = null
)

enum class LoginProvider(val value: String) {
    EMAIL("email"),
    GOOGLE("google"),
    APPLE("apple")
}

enum class SnsProvider(val value: String) {
    GOOGLE("google"),
    APPLE("apple")
}
