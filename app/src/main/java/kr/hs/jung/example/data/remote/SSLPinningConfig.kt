package kr.hs.jung.example.data.remote

import okhttp3.CertificatePinner

/**
 * SSL 인증서 피닝 설정
 *
 * 서버의 공개 키(SPKI) SHA-256 해시를 검증하여 MITM(중간자 공격)을 방지합니다.
 * 인증서 전체가 아닌 공개 키 해시를 고정하여 인증서 갱신에도 안정적으로 동작합니다.
 *
 * 핀이 비어있으면 피닝을 적용하지 않습니다 (개발 환경).
 *
 * openssl 명령어로 해시 생성:
 * ```bash
 * openssl s_client -connect HOST:443 2>/dev/null | \
 *   openssl x509 -pubkey -noout | \
 *   openssl pkey -pubin -outform DER | \
 *   openssl dgst -sha256 -binary | base64
 * ```
 *
 * @see <a href="https://square.github.io/okhttp/features/https/#certificate-pinning">OkHttp Certificate Pinning</a>
 */
object SSLPinningConfig {

    /** 피닝을 적용할 도메인 */
    private const val PINNED_DOMAIN = "hsjung.asuscomm.com"

    /**
     * 고정할 공개 키의 SHA-256 해시 목록 (Base64 인코딩)
     *
     * 최소 2개의 핀을 설정해야 합니다 (현재 인증서 + 백업).
     * 비어있으면 피닝이 비활성화됩니다.
     *
     * TODO: 서버 배포 환경 확정 후 실제 해시 추가
     */
    private val pinnedKeyHashes: List<String> = listOf(
        // "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", // 현재 인증서
        // "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=", // 백업 인증서
    )

    /**
     * OkHttp용 CertificatePinner 생성
     *
     * 핀이 비어있으면 [CertificatePinner.DEFAULT]를 반환하여
     * 기본 SSL 검증만 수행합니다.
     */
    fun createCertificatePinner(): CertificatePinner {
        if (pinnedKeyHashes.isEmpty()) {
            return CertificatePinner.DEFAULT
        }

        return CertificatePinner.Builder().apply {
            pinnedKeyHashes.forEach { hash ->
                add(PINNED_DOMAIN, hash)
            }
        }.build()
    }
}
