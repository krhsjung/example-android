package kr.hs.jung.example.util.config

import kr.hs.jung.example.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor

/**
 * 앱 환경 설정
 *
 * BuildConfig 기반으로 빌드 타입별 설정값을 제공
 * - Debug: 개발 서버, BODY 레벨 로깅
 * - Release: 프로덕션 서버, 로깅 비활성화
 */
object AppConfig {
    /**
     * API 기본 URL
     * - Debug: development API
     * - Release: production API
     */
    val baseUrl: String
        get() = BuildConfig.BASE_URL

    /**
     * HTTP 로깅 레벨
     * - Debug: BODY (전체 요청/응답 로깅)
     * - Release: NONE (로깅 비활성화)
     *
     * 사용 가능한 레벨:
     * - NONE: 로깅 없음
     * - BASIC: 요청/응답 라인만
     * - HEADERS: 요청/응답 라인 + 헤더
     * - BODY: 요청/응답 라인 + 헤더 + 바디
     */
    val logLevel: HttpLoggingInterceptor.Level
        get() = HttpLoggingInterceptor.Level.valueOf(BuildConfig.LOG_LEVEL)

    /**
     * 로깅 활성화 여부
     */
    val isLoggingEnabled: Boolean
        get() = logLevel != HttpLoggingInterceptor.Level.NONE

    /**
     * 디버그 빌드 여부
     */
    val isDebugBuild: Boolean
        get() = BuildConfig.DEBUG
}
