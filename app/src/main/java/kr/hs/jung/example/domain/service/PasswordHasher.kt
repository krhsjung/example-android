package kr.hs.jung.example.domain.service

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 비밀번호 해싱 서비스
 *
 * 클라이언트에서 SHA-512로 해싱하여 전송하고,
 * 서버에서 수신한 해시값을 Argon2로 다시 해싱하여 저장합니다.
 * 이를 통해 평문 비밀번호가 네트워크에 노출되지 않으며 (TLS 실패/로그 유출 대비),
 * 서버 DB 유출 시에도 Argon2로 보호됩니다.
 *
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html">OWASP Password Storage</a>
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
