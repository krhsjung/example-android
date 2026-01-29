package kr.hs.jung.example.domain.service

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 비밀번호 해싱 서비스
 *
 * Domain 레이어에서 비밀번호 해싱 로직을 담당
 * 클라이언트 사이드 해싱은 보안상 권장되지 않으나,
 * 서버와의 호환성을 위해 유지
 */
@Singleton
class PasswordHasher @Inject constructor() {

    /**
     * 비밀번호를 SHA-512로 해싱
     *
     * @param password 원본 비밀번호
     * @return 해싱된 비밀번호 (hex string)
     */
    fun hash(password: String): String {
        val bytes = password.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance(ALGORITHM)
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val ALGORITHM = "SHA-512"
    }
}
