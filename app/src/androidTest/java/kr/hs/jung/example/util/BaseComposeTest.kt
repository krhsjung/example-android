package kr.hs.jung.example.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import org.junit.Rule

/**
 * Compose UI 테스트를 위한 기본 테스트 클래스
 *
 * ComposeTestRule 설정과 테마 적용을 자동으로 처리합니다.
 *
 * 사용법:
 * ```
 * class MyScreenTest : BaseComposeTest() {
 *     @Test
 *     fun testSomething() {
 *         setContentWithTheme {
 *             MyScreen()
 *         }
 *
 *         // 테스트 검증
 *         composeTestRule.assertTextDisplayed("Expected Text")
 *     }
 * }
 * ```
 */
abstract class BaseComposeTest {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    /**
     * ExampleAndroidTheme을 적용하여 컴포저블을 설정합니다.
     *
     * @param darkTheme 다크 테마 여부 (기본 false)
     * @param dynamicColor 다이나믹 컬러 사용 여부 (기본 false - 테스트 일관성을 위해)
     * @param content 테스트할 컴포저블
     */
    protected fun setContentWithTheme(
        darkTheme: Boolean = false,
        dynamicColor: Boolean = false,
        content: @Composable () -> Unit
    ) {
        composeTestRule.setContent {
            ExampleAndroidTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor
            ) {
                content()
            }
        }
    }

    /**
     * 테마 없이 컴포저블을 설정합니다.
     * 개별 컴포넌트 테스트 시 사용합니다.
     *
     * @param content 테스트할 컴포저블
     */
    protected fun setContent(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            content()
        }
    }

    /**
     * 메인 스레드가 유휴 상태가 될 때까지 대기합니다.
     */
    protected fun waitForIdle() {
        composeTestRule.waitForIdle()
    }

    /**
     * 지정된 시간만큼 대기합니다.
     *
     * @param millis 대기 시간 (밀리초)
     */
    protected fun waitFor(millis: Long) {
        Thread.sleep(millis)
        waitForIdle()
    }
}
