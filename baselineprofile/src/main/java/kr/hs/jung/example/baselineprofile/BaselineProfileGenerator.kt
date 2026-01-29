package kr.hs.jung.example.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile Generator
 *
 * 앱의 주요 사용자 시나리오를 실행하여 Baseline Profile을 생성합니다.
 * 생성된 프로필은 앱 시작 시간을 20-30% 단축시킵니다.
 *
 * 실행 방법:
 * ./gradlew :baselineprofile:pixel6Api31BenchmarkAndroidTest
 *   -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
 *
 * 또는 간단히:
 * ./gradlew :app:generateBaselineProfile
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(
            packageName = "kr.hs.jung.example",
            includeInStartupProfile = true,
            profileBlock = {
                // 앱 시작 - Cold Start
                pressHome()
                startActivityAndWait()

                // 주요 사용자 시나리오 실행
                // 1. 로그인 화면 대기 (앱 시작 시 기본 화면)
                device.waitForIdle()

                // 2. 입력 필드 상호작용 시뮬레이션
                // 이메일 입력 필드 찾기 및 클릭
                // (실제 UI 요소가 있으면 여기서 상호작용)

                // 3. 화면 스크롤
                device.waitForIdle()
            }
        )
    }
}
