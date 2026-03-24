package kr.hs.jung.example.ui.feature.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.hs.jung.example.R
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.domain.model.LoginProvider
import kr.hs.jung.example.ui.common.ErrorMessageResolver
import kr.hs.jung.example.ui.common.collectAsEvent
import kr.hs.jung.example.ui.component.auth.AuthInputField
import kr.hs.jung.example.ui.component.auth.SocialLoginButtons
import kr.hs.jung.example.ui.component.common.ExampleButton
import kr.hs.jung.example.ui.component.common.ExampleDividerWithText
import kr.hs.jung.example.ui.component.common.ExampleErrorAlert
import kr.hs.jung.example.ui.component.common.ExampleLoadingOverlayBox
import kr.hs.jung.example.ui.component.common.ExampleThemeToggle
import kr.hs.jung.example.ui.component.layout.TitleSection
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.ExampleTheme

/**
 * 로그인 화면 Composable
 *
 * iOS LogInView.swift와 동일 구조:
 * - TitleSection → CredentialsSection → DividerWithText → SocialLoginButtons
 * - Footer: "계정이 없으신가요?" + "회원가입" 링크
 * - 필드별 인라인 에러 + blur 검증
 *
 * @param onNavigateToSignUp 회원가입 화면으로 이동 콜백
 * @param onLogInSuccess 로그인 성공 시 콜백
 * @param onOAuthRequest OAuth 인증 요청 시 콜백
 * @param viewModel 로그인 화면의 ViewModel
 */
@Composable
fun LogInScreen(
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onLogInSuccess: () -> Unit = {},
    onOAuthRequest: (LoginProvider) -> Unit = {},
    viewModel: LogInViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Event 기반 일회성 이벤트 처리
    viewModel.event.collectAsEvent { event ->
        when (event) {
            is LogInEvent.Success -> onLogInSuccess()
            is LogInEvent.OAuthRequest -> onOAuthRequest(event.provider)
        }
    }

    // 람다 안정화 - ViewModel 메서드 참조로 리컴포지션 최적화
    val onEmailChange = remember<(String) -> Unit>(viewModel) { { viewModel.updateEmail(it) } }
    val onPasswordChange =
        remember<(String) -> Unit>(viewModel) { { viewModel.updatePassword(it) } }
    val onLogInClick = remember(viewModel) { { viewModel.logIn() } }
    val onSocialLogin = remember<(LoginProvider) -> Unit>(viewModel) { { viewModel.signInWith(it) } }
    val onClearError = remember(viewModel) { { viewModel.clearError() } }
    val onValidateEmail = remember(viewModel) { { viewModel.validateEmail() } }
    val onValidatePassword = remember(viewModel) { { viewModel.validatePassword() } }

    LogInContent(
        uiState = uiState,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onEmailChange = onEmailChange,
        onPasswordChange = onPasswordChange,
        onValidateEmail = onValidateEmail,
        onValidatePassword = onValidatePassword,
        onLogInClick = onLogInClick,
        onSocialLogin = onSocialLogin,
        onNavigateToSignUp = onNavigateToSignUp
    )

    // 서버/네트워크 에러만 다이얼로그로 표시 (유효성 에러는 인라인)
    ExampleErrorAlert(
        isPresented = uiState.error != null,
        message = uiState.error?.let { ErrorMessageResolver.getMessage(context, it) } ?: "",
        onDismiss = onClearError
    )
}

@Composable
private fun LogInContent(
    uiState: LogInUiState,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onValidateEmail: () -> Unit = {},
    onValidatePassword: () -> Unit = {},
    onLogInClick: () -> Unit = {},
    onSocialLogin: (LoginProvider) -> Unit = {},
    onNavigateToSignUp: () -> Unit = {}
) {
    ExampleLoadingOverlayBox(isLoading = uiState.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Screen.HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(
                20.dp,
                Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TitleSection(
                title = stringResource(R.string.application_name),
                subtitle = stringResource(R.string.login_subtitle),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            LogInCredentialsSection(
                email = uiState.email,
                password = uiState.password,
                emailError = uiState.emailError,
                passwordError = uiState.passwordError,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onValidateEmail = onValidateEmail,
                onValidatePassword = onValidatePassword,
                onLogInClick = onLogInClick
            )

            ExampleDividerWithText(
                text = stringResource(R.string.login_continue_with)
            )

            SocialLoginButtons(
                onSocialLogin = onSocialLogin
            )

            SignUpFooterSection(onNavigateToSignUp = onNavigateToSignUp)
        }

        // Theme toggle button (top-right) - iOS ExampleThemeToggle과 동일
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.Screen.HorizontalPadding),
            contentAlignment = Alignment.TopEnd
        ) {
            ExampleThemeToggle(
                isDarkTheme = isDarkTheme,
                onToggle = onToggleTheme
            )
        }
    }
}

/**
 * 로그인 인증 입력 섹션
 *
 * AuthInputField(이메일/비밀번호) + "비밀번호 찾기" 링크 + 로그인 버튼
 */
@Composable
private fun LogInCredentialsSection(
    email: String,
    password: String,
    emailError: AppError.Validation?,
    passwordError: AppError.Validation?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onValidateEmail: () -> Unit,
    onValidatePassword: () -> Unit,
    onLogInClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        AuthInputField(
            label = stringResource(R.string.email),
            value = email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.placeholder_email),
            error = emailError,
            keyboardType = KeyboardType.Email,
            leadingIcon = Icons.Outlined.Email,
            onBlurValidate = onValidateEmail
        )

        AuthInputField(
            label = stringResource(R.string.password),
            value = password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.placeholder_password),
            error = passwordError,
            isSecure = true,
            leadingIcon = Icons.Outlined.Lock,
            onBlurValidate = onValidatePassword
        )

        Text(
            text = stringResource(R.string.login_forget_password),
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ExampleTheme.extendedColors.linkText),
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        ExampleButton(
            title = stringResource(R.string.login),
            onClick = onLogInClick
        )
    }
}


/**
 * 회원가입 링크 푸터
 *
 * "계정이 없으신가요?" + "회원가입" 클릭 텍스트
 */
@Composable
private fun SignUpFooterSection(onNavigateToSignUp: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.login_no_account),
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = ExampleTheme.extendedColors.textSecondary)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.signup),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ExampleTheme.extendedColors.linkText
            ),
            modifier = Modifier.clickable(onClick = onNavigateToSignUp)
        )
    }
}

// MARK: - Previews

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LogInScreenPreview() {
    var isDarkTheme by remember { mutableStateOf(false) }
    ExampleAndroidTheme(darkTheme = isDarkTheme) {
        LogInContent(
            uiState = LogInUiState(email = "", password = ""),
            isDarkTheme = isDarkTheme,
            onToggleTheme = { isDarkTheme = !isDarkTheme }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LogInScreenWithErrorPreview() {
    ExampleAndroidTheme {
        LogInContent(
            uiState = LogInUiState(
                email = "invalid",
                password = "short",
                emailError = AppError.Validation.InvalidEmail,
                passwordError = AppError.Validation.WeakPassword
            )
        )
    }
}
