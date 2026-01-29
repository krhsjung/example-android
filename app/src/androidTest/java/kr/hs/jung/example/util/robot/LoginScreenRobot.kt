package kr.hs.jung.example.util.robot

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import kr.hs.jung.example.ui.component.TestTags
import kr.hs.jung.example.util.assertTextDisplayed
import kr.hs.jung.example.util.waitUntilTagDisplayed
import kr.hs.jung.example.util.waitUntilTagNotDisplayed

/**
 * 로그인 화면 테스트를 위한 로봇 패턴 클래스
 *
 * 로봇 패턴은 테스트 코드의 가독성과 유지보수성을 높이기 위한 패턴입니다.
 * "What"(무엇을 테스트하는지)과 "How"(어떻게 수행하는지)를 분리합니다.
 *
 * 사용법:
 * ```
 * @Test
 * fun `login with valid credentials`() {
 *     LoginScreenRobot(composeTestRule)
 *         .enterEmail("test@example.com")
 *         .enterPassword("password123")
 *         .tapLoginButton()
 *         .assertLoadingDisplayed()
 * }
 * ```
 */
class LoginScreenRobot(
    private val composeTestRule: ComposeTestRule
) {
    // ============================================
    // 액션
    // ============================================

    /**
     * 이메일을 입력합니다.
     */
    fun enterEmail(email: String): LoginScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.Login.EMAIL_INPUT).apply {
            performTextClearance()
            performTextInput(email)
        }
        return this
    }

    /**
     * 비밀번호를 입력합니다.
     */
    fun enterPassword(password: String): LoginScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.Login.PASSWORD_INPUT).apply {
            performTextClearance()
            performTextInput(password)
        }
        return this
    }

    /**
     * 로그인 버튼을 탭합니다.
     */
    fun tapLoginButton(): LoginScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.Login.LOGIN_BUTTON).performClick()
        return this
    }

    /**
     * 회원가입 버튼을 탭합니다.
     */
    fun tapSignUpButton(): LoginScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.Login.SIGNUP_BUTTON).performClick()
        return this
    }

    /**
     * Google 로그인 버튼을 탭합니다.
     */
    fun tapGoogleLoginButton(): LoginScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.Login.GOOGLE_LOGIN_BUTTON).performClick()
        return this
    }

    /**
     * Apple 로그인 버튼을 탭합니다.
     */
    fun tapAppleLoginButton(): LoginScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.Login.APPLE_LOGIN_BUTTON).performClick()
        return this
    }

    /**
     * 이메일과 비밀번호를 입력하고 로그인 버튼을 탭합니다.
     */
    fun login(email: String, password: String): LoginScreenRobot {
        return enterEmail(email)
            .enterPassword(password)
            .tapLoginButton()
    }

    // ============================================
    // 검증
    // ============================================

    /**
     * 로그인 화면이 표시되는지 검증합니다.
     */
    fun assertScreenDisplayed(): LoginScreenRobot {
        composeTestRule.waitUntilTagDisplayed(TestTags.Login.SCREEN)
        return this
    }

    /**
     * 로딩 인디케이터가 표시되는지 검증합니다.
     */
    fun assertLoadingDisplayed(): LoginScreenRobot {
        composeTestRule.waitUntilTagDisplayed(TestTags.Common.LOADING_INDICATOR)
        return this
    }

    /**
     * 로딩 인디케이터가 사라지는지 검증합니다.
     */
    fun assertLoadingNotDisplayed(): LoginScreenRobot {
        composeTestRule.waitUntilTagNotDisplayed(TestTags.Common.LOADING_INDICATOR)
        return this
    }

    /**
     * 에러 다이얼로그가 표시되는지 검증합니다.
     */
    fun assertErrorDialogDisplayed(): LoginScreenRobot {
        composeTestRule.waitUntilTagDisplayed(TestTags.Common.ERROR_DIALOG)
        return this
    }

    /**
     * 특정 에러 메시지가 표시되는지 검증합니다.
     */
    fun assertErrorMessage(message: String): LoginScreenRobot {
        composeTestRule.assertTextDisplayed(message)
        return this
    }

    /**
     * 에러 다이얼로그를 닫습니다.
     */
    fun dismissErrorDialog(): LoginScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.Common.ERROR_DIALOG_DISMISS).performClick()
        return this
    }

    /**
     * 특정 텍스트가 화면에 표시되는지 검증합니다.
     */
    fun assertTextDisplayed(text: String): LoginScreenRobot {
        composeTestRule.assertTextDisplayed(text)
        return this
    }

    // ============================================
    // 대기
    // ============================================

    /**
     * 유휴 상태가 될 때까지 대기합니다.
     */
    fun waitForIdle(): LoginScreenRobot {
        composeTestRule.waitForIdle()
        return this
    }
}

/**
 * LoginScreenRobot을 생성하는 확장 함수
 */
fun ComposeTestRule.loginScreen(): LoginScreenRobot = LoginScreenRobot(this)
