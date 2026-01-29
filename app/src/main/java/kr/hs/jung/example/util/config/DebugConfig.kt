package kr.hs.jung.example.util.config

import kr.hs.jung.example.BuildConfig

/**
 * Debug 빌드에서 사용할 테스트 데이터 설정
 *
 * Debug 빌드에서만 기본값이 채워지고, Release 빌드에서는 빈 값 반환
 */
object DebugConfig {
    val testEmail: String
        get() = if (BuildConfig.DEBUG) "test@test.com" else ""

    val testPassword: String
        get() = if (BuildConfig.DEBUG) "Test2022@!" else ""

    val testName: String
        get() = if (BuildConfig.DEBUG) "tester" else ""

    val testAgreeToTerms: Boolean
        get() = BuildConfig.DEBUG
}
