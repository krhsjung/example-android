package kr.hs.jung.example.domain.event

/**
 * 세션 이벤트 타입
 *
 * 인증 관련 이벤트를 정의합니다.
 */
sealed class SessionEvent {
    /** 세션 만료 (401 Unauthorized) */
    data object SessionExpired : SessionEvent()

    /** 접근 거부 (403 Forbidden) */
    data object AccessDenied : SessionEvent()

    /** Rate Limit 초과 (429) */
    data class RateLimited(val retryAfterSeconds: Long?) : SessionEvent()
}
