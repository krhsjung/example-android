package kr.hs.jung.example.domain.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kr.hs.jung.example.util.logger.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 세션 이벤트 버스
 *
 * 인증 관련 이벤트를 앱 전체에 브로드캐스트합니다.
 * SafeApiCall에서 401/403/429 응답 감지 시 이벤트를 발생시키고,
 * UI 레이어(Activity/ViewModel)에서 구독하여 적절한 처리를 수행합니다.
 *
 * 주요 기능:
 * - 401 응답 시 자동 로그아웃 트리거
 * - 403 응답 시 권한 오류 알림
 * - 429 응답 시 Rate Limit 정보 전달
 */
@Singleton
class SessionEventBus @Inject constructor() {

    private val _sessionEvents = MutableSharedFlow<SessionEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )

    /**
     * 세션 이벤트 Flow
     *
     * Activity/ViewModel에서 구독하여 세션 이벤트 처리
     */
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    /**
     * 세션 만료 이벤트 발생
     *
     * 401 Unauthorized 응답 수신 시 호출
     */
    fun notifySessionExpired() {
        AppLogger.w(TAG, "Session expired - notifying observers")
        _sessionEvents.tryEmit(SessionEvent.SessionExpired)
    }

    /**
     * 접근 거부 이벤트 발생
     *
     * 403 Forbidden 응답 수신 시 호출
     */
    fun notifyAccessDenied() {
        AppLogger.w(TAG, "Access denied - notifying observers")
        _sessionEvents.tryEmit(SessionEvent.AccessDenied)
    }

    /**
     * Rate Limit 초과 이벤트 발생
     *
     * 429 Too Many Requests 응답 수신 시 호출
     *
     * @param retryAfterSeconds Retry-After 헤더 값 (초 단위)
     */
    fun notifyRateLimited(retryAfterSeconds: Long?) {
        AppLogger.w(TAG, "Rate limited - retry after: $retryAfterSeconds seconds")
        _sessionEvents.tryEmit(SessionEvent.RateLimited(retryAfterSeconds))
    }

    companion object {
        private const val TAG = "SessionEventBus"
    }
}

/**
 * 기존 SessionManager와의 호환성을 위한 타입 별칭
 */
typealias SessionManager = SessionEventBus
