package kr.hs.jung.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Test 기본 예제
 *
 * Hilt를 사용하는 통합 테스트의 기본 구조를 보여줍니다.
 *
 * 설정:
 * - HiltAndroidRule: Hilt 의존성 주입 설정
 * - HiltAndroidTest: Hilt 테스트 어노테이션
 * - HiltTestRunner: 커스텀 테스트 러너 (build.gradle.kts에서 설정)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertThat(appContext.packageName).isEqualTo("kr.hs.jung.example")
    }
}
