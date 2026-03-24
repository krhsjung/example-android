package kr.hs.jung.example.ui.feature.auth.signup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
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
import kr.hs.jung.example.ui.component.common.ExampleCheckbox
import kr.hs.jung.example.ui.component.common.ExampleDividerWithText
import kr.hs.jung.example.ui.component.common.ExampleErrorAlert
import kr.hs.jung.example.ui.component.common.ExampleLoadingOverlayBox
import kr.hs.jung.example.ui.component.layout.TitleSection
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.ExampleTheme

/**
 * 회원가입 화면 Composable
 *
 * Figma SignupForm과 동일 구조:
 * - TitleSection → CredentialsSection(Name→Email→PW→ConfirmPW) → Checkbox → Button
 * - DividerWithText → SocialLoginButtons
 * - Footer: "로그인" 링크
 * - 필드별 인라인 에러 + blur 검증 + leading icon
 *
 * @param onNavigateBack 뒤로가기 콜백
 * @param onSignUpSuccess 회원가입 성공 시 콜백
 * @param onOAuthRequest OAuth 인증 요청 시 콜백
 * @param viewModel 회원가입 화면의 ViewModel
 */
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onOAuthRequest: (LoginProvider) -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Event 기반 일회성 이벤트 처리
    viewModel.event.collectAsEvent { event ->
        when (event) {
            is SignUpEvent.Success -> onSignUpSuccess()
            is SignUpEvent.OAuthRequest -> onOAuthRequest(event.provider)
        }
    }

    // 람다 안정화 - ViewModel 메서드 참조로 리컴포지션 최적화
    val onEmailChange = remember<(String) -> Unit>(viewModel) { { viewModel.updateEmail(it) } }
    val onPasswordChange = remember<(String) -> Unit>(viewModel) { { viewModel.updatePassword(it) } }
    val onConfirmPasswordChange = remember<(String) -> Unit>(viewModel) { { viewModel.updateConfirmPassword(it) } }
    val onNameChange = remember<(String) -> Unit>(viewModel) { { viewModel.updateName(it) } }
    val onAgreeToTermsChange = remember<(Boolean) -> Unit>(viewModel) { { viewModel.updateAgreeToTerms(it) } }
    val onSignUpClick = remember(viewModel) { { viewModel.signUp() } }
    val onSocialLogin = remember<(LoginProvider) -> Unit>(viewModel) { { viewModel.signInWith(it) } }
    val onClearError = remember(viewModel) { { viewModel.clearError() } }
    val onValidateEmail = remember(viewModel) { { viewModel.validateEmail() } }
    val onValidatePassword = remember(viewModel) { { viewModel.validatePassword() } }
    val onValidateConfirmPassword = remember(viewModel) { { viewModel.validateConfirmPassword() } }
    val onValidateName = remember(viewModel) { { viewModel.validateName() } }

    SignUpContent(
        uiState = uiState,
        onNameChange = onNameChange,
        onEmailChange = onEmailChange,
        onPasswordChange = onPasswordChange,
        onConfirmPasswordChange = onConfirmPasswordChange,
        onAgreeToTermsChange = onAgreeToTermsChange,
        onValidateName = onValidateName,
        onValidateEmail = onValidateEmail,
        onValidatePassword = onValidatePassword,
        onValidateConfirmPassword = onValidateConfirmPassword,
        onSignUpClick = onSignUpClick,
        onSocialLogin = onSocialLogin,
        onNavigateBack = onNavigateBack
    )

    // 서버/네트워크 에러만 다이얼로그로 표시 (유효성 에러는 인라인)
    ExampleErrorAlert(
        isPresented = uiState.error != null,
        message = uiState.error?.let { ErrorMessageResolver.getMessage(context, it) } ?: "",
        onDismiss = onClearError
    )
}

/**
 * 회원가입 화면 콘텐츠 (프리뷰 지원)
 *
 * ViewModel 의존성 없이 UiState만으로 렌더링
 */
@Composable
private fun SignUpContent(
    uiState: SignUpUiState,
    onNameChange: (String) -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onAgreeToTermsChange: (Boolean) -> Unit = {},
    onValidateName: () -> Unit = {},
    onValidateEmail: () -> Unit = {},
    onValidatePassword: () -> Unit = {},
    onValidateConfirmPassword: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onSocialLogin: (LoginProvider) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    ExampleLoadingOverlayBox(isLoading = uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Screen.HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingXXLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TitleSection(
                title = stringResource(R.string.application_name),
                subtitle = stringResource(R.string.signup_subtitle),
                horizontalAlignment = Alignment.CenterHorizontally
            )

            SignUpCredentialsSection(
                name = uiState.name,
                email = uiState.email,
                password = uiState.password,
                confirmPassword = uiState.confirmPassword,
                isAgreeToTerms = uiState.isAgreeToTerms,
                isFormValid = uiState.isFormValid,
                nameError = uiState.nameError,
                emailError = uiState.emailError,
                passwordError = uiState.passwordError,
                confirmPasswordError = uiState.confirmPasswordError,
                onNameChange = onNameChange,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onAgreeToTermsChange = onAgreeToTermsChange,
                onValidateName = onValidateName,
                onValidateEmail = onValidateEmail,
                onValidatePassword = onValidatePassword,
                onValidateConfirmPassword = onValidateConfirmPassword,
                onSignUpClick = onSignUpClick
            )

            ExampleDividerWithText(
                text = stringResource(R.string.signup_continue_with)
            )

            SocialLoginButtons(
                onSocialLogin = onSocialLogin
            )

            LogInFooterSection(onNavigateBack = onNavigateBack)
        }
        }
    }
}

/**
 * 회원가입 인증 입력 섹션
 *
 * Figma 순서: 이름 → 이메일 → 비밀번호 → 비밀번호 확인
 * 각 필드에 Figma에 맞는 leading icon 적용
 */
@Composable
private fun SignUpCredentialsSection(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    isAgreeToTerms: Boolean,
    isFormValid: Boolean,
    nameError: AppError.Validation?,
    emailError: AppError.Validation?,
    passwordError: AppError.Validation?,
    confirmPasswordError: AppError.Validation?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAgreeToTermsChange: (Boolean) -> Unit,
    onValidateName: () -> Unit,
    onValidateEmail: () -> Unit,
    onValidatePassword: () -> Unit,
    onValidateConfirmPassword: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingDefault),
        horizontalAlignment = Alignment.Start
    ) {
        // 이름 (첫 번째) - person icon
        AuthInputField(
            label = stringResource(R.string.placeholder_name),
            value = name,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.placeholder_name),
            error = nameError,
            leadingIcon = Icons.Outlined.Person,
            onBlurValidate = onValidateName
        )

        // 이메일 (두 번째) - email icon
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

        // 비밀번호 (세 번째) - lock icon + eye toggle
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

        // 비밀번호 확인 (네 번째) - lock icon + eye toggle
        AuthInputField(
            label = stringResource(R.string.placeholder_confirm_password),
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = stringResource(R.string.placeholder_confirm_password),
            error = confirmPasswordError,
            isSecure = true,
            leadingIcon = Icons.Outlined.Lock,
            onBlurValidate = onValidateConfirmPassword
        )

        ExampleCheckbox(
            isChecked = isAgreeToTerms,
            onCheckedChange = onAgreeToTermsChange
        ) {
            val colors = ExampleTheme.extendedColors
            val annotatedText = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.linkText, fontWeight = FontWeight.Medium)) {
                    append(stringResource(R.string.signup_terms_of_service))
                }
                append(stringResource(R.string.signup_and))
                withStyle(SpanStyle(color = colors.linkText, fontWeight = FontWeight.Medium)) {
                    append(stringResource(R.string.signup_privacy_policy))
                }
                append(stringResource(R.string.signup_agree_suffix))
            }
            Text(
                text = annotatedText,
                style = TextStyle(fontSize = Dimensions.Checkbox.FontSize, color = colors.textSecondary)
            )
        }

        ExampleButton(
            title = stringResource(R.string.signup),
            onClick = onSignUpClick,
            enabled = isFormValid,
            modifier = Modifier.alpha(if (isFormValid) 1.0f else 0.5f)
        )
    }
}

/**
 * 로그인 링크 푸터
 *
 * "로그인" 클릭 텍스트
 */
@Composable
private fun LogInFooterSection(onNavigateBack: () -> Unit) {
    Text(
        text = stringResource(R.string.login),
        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ExampleTheme.extendedColors.linkText),
        modifier = Modifier.clickable(onClick = onNavigateBack)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenPreview() {
    ExampleAndroidTheme {
        SignUpContent(
            uiState = SignUpUiState(
                name = "",
                email = "",
                password = "",
                confirmPassword = ""
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenFilledPreview() {
    ExampleAndroidTheme {
        SignUpContent(
            uiState = SignUpUiState(
                name = "홍길동",
                email = "test@example.com",
                password = "password123",
                confirmPassword = "password123",
                isAgreeToTerms = true
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenWithErrorPreview() {
    ExampleAndroidTheme {
        SignUpContent(
            uiState = SignUpUiState(
                name = "",
                email = "invalid",
                password = "short",
                confirmPassword = "mismatch",
                nameError = AppError.Validation.EmptyUsername,
                emailError = AppError.Validation.InvalidEmail,
                passwordError = AppError.Validation.WeakPassword,
                confirmPasswordError = AppError.Validation.PasswordMismatch
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenDarkPreview() {
    ExampleAndroidTheme(darkTheme = true) {
        SignUpContent(
            uiState = SignUpUiState(
                name = "",
                email = "",
                password = "",
                confirmPassword = ""
            )
        )
    }
}
