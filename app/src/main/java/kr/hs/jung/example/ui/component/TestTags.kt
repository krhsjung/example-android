package kr.hs.jung.example.ui.component

/**
 * UI 테스트를 위한 테스트 태그 정의
 *
 * Compose 테스트에서 사용할 testTag 값들을 중앙에서 관리합니다.
 * Modifier.testTag(TestTags.XXX)로 사용합니다.
 *
 * 사용법:
 * ```
 * // 프로덕션 코드
 * ExampleInputBox(
 *     modifier = Modifier.testTag(TestTags.Login.EMAIL_INPUT),
 *     ...
 * )
 *
 * // 테스트 코드
 * composeTestRule.onNodeWithTag(TestTags.Login.EMAIL_INPUT)
 *     .performTextInput("test@example.com")
 * ```
 */
object TestTags {

    /**
     * 공통 컴포넌트 테스트 태그
     */
    object Common {
        const val LOADING_INDICATOR = "common_loading_indicator"
        const val ERROR_DIALOG = "common_error_dialog"
        const val ERROR_DIALOG_DISMISS = "common_error_dialog_dismiss"
        const val BACK_BUTTON = "common_back_button"
    }

    /**
     * 로그인 화면 테스트 태그
     */
    object Login {
        const val SCREEN = "login_screen"
        const val EMAIL_INPUT = "login_email_input"
        const val PASSWORD_INPUT = "login_password_input"
        const val LOGIN_BUTTON = "login_button"
        const val SIGNUP_BUTTON = "login_signup_button"
        const val GOOGLE_LOGIN_BUTTON = "login_google_button"
        const val APPLE_LOGIN_BUTTON = "login_apple_button"
    }

    /**
     * 회원가입 화면 테스트 태그
     */
    object SignUp {
        const val SCREEN = "signup_screen"
        const val EMAIL_INPUT = "signup_email_input"
        const val PASSWORD_INPUT = "signup_password_input"
        const val CONFIRM_PASSWORD_INPUT = "signup_confirm_password_input"
        const val TERMS_CHECKBOX = "signup_terms_checkbox"
        const val SIGNUP_BUTTON = "signup_button"
        const val GOOGLE_LOGIN_BUTTON = "signup_google_button"
        const val APPLE_LOGIN_BUTTON = "signup_apple_button"
    }

    /**
     * 메인 화면 테스트 태그
     */
    object Main {
        const val SCREEN = "main_screen"
        const val BOTTOM_NAV = "main_bottom_nav"
        const val HOME_TAB = "main_home_tab"
        const val PROFILE_TAB = "main_profile_tab"
        const val SETTINGS_TAB = "main_settings_tab"
        const val LOGOUT_BUTTON = "main_logout_button"
    }

    /**
     * 입력 필드 테스트 태그
     */
    object Input {
        const val TEXT_FIELD = "input_text_field"
        const val CLEAR_BUTTON = "input_clear_button"
        const val PASSWORD_TOGGLE = "input_password_toggle"
    }

    /**
     * 버튼 테스트 태그
     */
    object Button {
        const val PRIMARY = "button_primary"
        const val SECONDARY = "button_secondary"
        const val SNS = "button_sns"
    }
}
