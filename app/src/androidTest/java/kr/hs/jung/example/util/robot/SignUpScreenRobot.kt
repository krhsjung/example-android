package kr.hs.jung.example.util.robot

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import kr.hs.jung.example.ui.component.TestTags
import kr.hs.jung.example.util.assertTextDisplayed
import kr.hs.jung.example.util.waitUntilTagDisplayed
import kr.hs.jung.example.util.waitUntilTagNotDisplayed

/**
 * 회원가입 화면 테스트를 위한 로봇 패턴 클래스
 *
 * 사용법:
 * ```
 * @Test
 * fun `signup with valid credentials`() {
 *     SignUpScreenRobot(composeTestRule)
 *         .enterEmail("test@example.com")
 *         .enterPassword("password123")
 *         .enterConfirmPassword("password123")
 *         .checkTermsAgreement()
 *         .tapSignUpButton()
 *         .assertLoadingDisplayed()
 * }
 * ```
 */
class SignUpScreenRobot(
    private val composeTestRule: ComposeTestRule
) {
    // ============================================
    // 액션
    // ============================================

    /**
     * 이메일을 입력합니다.
     */
    fun enterEmail(email: String): SignUpScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.SignUp.EMAIL_INPUT).apply {
            performTextClearance()
            performTextInput(email)
        }
        return this
    }

    /**
     * 비밀번호를 입력합니다.
     */
    fun enterPassword(password: String): SignUpScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.SignUp.PASSWORD_INPUT).apply {
            performTextClearance()
            performTextInput(password)
        }
        return this
    }

    /**
     * 비밀번호 확인을 입력합니다.
     */
    fun enterConfirmPassword(password: String): SignUpScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.SignUp.CONFIRM_PASSWORD_INPUT).apply {
            performTextClearance()
            performTextInput(password)
        }
        return this
    }

    /**
     * 약관 동의 체크박스를 체크합니다.
     */
    fun checkTermsAgreement(): SignUpScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.SignUp.TERMS_CHECKBOX).performClick()
        return this
    }

    /**
     * 회원가입 버튼을 탭합니다.
     */
    fun tapSignUpButton(): SignUpScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.SignUp.SIGNUP_BUTTON).performClick()
        return this
    }

    /**
     * Google 로그인 버튼을 탭합니다.
     */
    fun tapGoogleLoginButton(): SignUpScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.SignUp.GOOGLE_LOGIN_BUTTON).performClick()
        return this
    }

    /**
     * Apple 로그인 버튼을 탭합니다.
     */
    fun tapAppleLoginButton(): SignUpScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.SignUp.APPLE_LOGIN_BUTTON).performClick()
        return this
    }

    /**
     * 전체 회원가입 플로우를 수행합니다.
     */
    fun signUp(
        email: String,
        password: String,
        confirmPassword: String = password,
        acceptTerms: Boolean = true
    ): SignUpScreenRobot {
        enterEmail(email)
            .enterPassword(password)
            .enterConfirmPassword(confirmPassword)

        if (acceptTerms) {
            checkTermsAgreement()
        }

        return tapSignUpButton()
    }

    // ============================================
    // 검증
    // ============================================

    /**
     * 회원가입 화면이 표시되는지 검증합니다.
     */
    fun assertScreenDisplayed(): SignUpScreenRobot {
        composeTestRule.waitUntilTagDisplayed(TestTags.SignUp.SCREEN)
        return this
    }

    /**
     * 로딩 인디케이터가 표시되는지 검증합니다.
     */
    fun assertLoadingDisplayed(): SignUpScreenRobot {
        composeTestRule.waitUntilTagDisplayed(TestTags.Common.LOADING_INDICATOR)
        return this
    }

    /**
     * 로딩 인디케이터가 사라지는지 검증합니다.
     */
    fun assertLoadingNotDisplayed(): SignUpScreenRobot {
        composeTestRule.waitUntilTagNotDisplayed(TestTags.Common.LOADING_INDICATOR)
        return this
    }

    /**
     * 에러 다이얼로그가 표시되는지 검증합니다.
     */
    fun assertErrorDialogDisplayed(): SignUpScreenRobot {
        composeTestRule.waitUntilTagDisplayed(TestTags.Common.ERROR_DIALOG)
        return this
    }

    /**
     * 특정 에러 메시지가 표시되는지 검증합니다.
     */
    fun assertErrorMessage(message: String): SignUpScreenRobot {
        composeTestRule.assertTextDisplayed(message)
        return this
    }

    /**
     * 에러 다이얼로그를 닫습니다.
     */
    fun dismissErrorDialog(): SignUpScreenRobot {
        composeTestRule.onNodeWithTag(TestTags.Common.ERROR_DIALOG_DISMISS).performClick()
        return this
    }

    /**
     * 특정 텍스트가 화면에 표시되는지 검증합니다.
     */
    fun assertTextDisplayed(text: String): SignUpScreenRobot {
        composeTestRule.assertTextDisplayed(text)
        return this
    }

    // ============================================
    // 대기
    // ============================================

    /**
     * 유휴 상태가 될 때까지 대기합니다.
     */
    fun waitForIdle(): SignUpScreenRobot {
        composeTestRule.waitForIdle()
        return this
    }
}

/**
 * SignUpScreenRobot을 생성하는 확장 함수
 */
fun ComposeTestRule.signUpScreen(): SignUpScreenRobot = SignUpScreenRobot(this)
