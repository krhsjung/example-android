package kr.hs.jung.example.data.remote

/**
 * 네트워크 관련 상수
 *
 * 타임아웃, 재시도, 연결 풀 등의 설정 값을 중앙화합니다.
 */
object NetworkConstants {

    // 타임아웃 설정 (초)
    const val CONNECT_TIMEOUT_SECONDS = 10L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    // 재시도 설정
    const val MAX_RETRY_COUNT = 3
    const val RETRY_DELAY_MILLIS = 1000L
    const val JITTER_FACTOR = 0.5 // 기본 딜레이의 0~50% 추가

    // 연결 풀 설정
    const val CONNECTION_POOL_SIZE = 5
    const val CONNECTION_KEEP_ALIVE_MINUTES = 5L

    // 재시도 가능한 HTTP 상태 코드
    val RETRYABLE_STATUS_CODES = setOf(408, 429, 500, 502, 503, 504)
}
