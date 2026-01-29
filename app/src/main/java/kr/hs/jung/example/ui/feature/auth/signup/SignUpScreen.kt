package kr.hs.jung.example.ui.feature.auth.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.hs.jung.example.R
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.ui.common.ErrorMessageResolver
import kr.hs.jung.example.ui.common.collectAsEvent
import kr.hs.jung.example.ui.component.auth.SocialLoginButtons
import kr.hs.jung.example.ui.component.layout.AuthScreenLayout
import kr.hs.jung.example.ui.component.button.ExampleButton
import kr.hs.jung.example.ui.component.checkbox.ExampleCheckbox
import kr.hs.jung.example.ui.component.divider.ExampleDividerWithText
import kr.hs.jung.example.ui.component.dialog.ExampleErrorAlert
import kr.hs.jung.example.ui.component.input.ExampleInputBox
import kr.hs.jung.example.ui.component.dialog.ExampleLoadingOverlayBox
import kr.hs.jung.example.ui.component.layout.TitleSection
import kr.hs.jung.example.ui.theme.Dimensions

/**
 * 회원가입 화면 Composable
 *
 * 이메일/비밀번호로 회원가입 및 소셜 로그인을 제공하는 화면
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
    onOAuthRequest: (SnsProvider) -> Unit = {},
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
    val onSnsLogin = remember<(SnsProvider) -> Unit>(viewModel) { { viewModel.signInWith(it) } }
    val onClearError = remember(viewModel) { { viewModel.clearError() } }

    ExampleLoadingOverlayBox(isLoading = uiState.isLoading) {
        AuthScreenLayout(
            header = {
                TitleSection(
                    title = stringResource(R.string.signup_title),
                    subtitle = stringResource(R.string.signup_subtitle),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
            }
        ) {
            SignUpForm(
                email = uiState.email,
                password = uiState.password,
                confirmPassword = uiState.confirmPassword,
                name = uiState.name,
                isAgreeToTerms = uiState.isAgreeToTerms,
                isFormValid = uiState.isFormValid,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onNameChange = onNameChange,
                onAgreeToTermsChange = onAgreeToTermsChange,
                onSignUpClick = onSignUpClick
            )

            ExampleDividerWithText(
                text = stringResource(R.string.signup_continue_with)
            )

            SocialLoginButtons(
                onSnsLogin = onSnsLogin
            )
        }
    }

    ExampleErrorAlert(
        isPresented = uiState.error != null,
        message = uiState.error?.let { ErrorMessageResolver.getMessage(context, it) } ?: "",
        onDismiss = onClearError
    )
}

/**
 * 회원가입 폼
 *
 * 이메일/비밀번호/이름 입력 필드와 약관 동의, 회원가입 버튼
 *
 * @param email 이메일 입력값
 * @param password 비밀번호 입력값
 * @param confirmPassword 비밀번호 확인 입력값
 * @param name 이름 입력값
 * @param isAgreeToTerms 약관 동의 여부
 * @param isFormValid 폼 유효성 여부
 * @param onEmailChange 이메일 변경 콜백
 * @param onPasswordChange 비밀번호 변경 콜백
 * @param onConfirmPasswordChange 비밀번호 확인 변경 콜백
 * @param onNameChange 이름 변경 콜백
 * @param onAgreeToTermsChange 약관 동의 변경 콜백
 * @param onSignUpClick 회원가입 버튼 클릭 콜백
 */
@Composable
private fun SignUpForm(
    email: String,
    password: String,
    confirmPassword: String,
    name: String,
    isAgreeToTerms: Boolean,
    isFormValid: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onAgreeToTermsChange: (Boolean) -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingDefault),
        horizontalAlignment = Alignment.Start
    ) {
        ExampleInputBox(
            value = email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.placeholder_email),
            keyboardType = KeyboardType.Email
        )
        ExampleInputBox(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.placeholder_password),
            isSecure = true
        )
        ExampleInputBox(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = stringResource(R.string.placeholder_confirm_password),
            isSecure = true
        )
        ExampleInputBox(
            value = name,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.placeholder_name)
        )
        ExampleCheckbox(
            text = stringResource(R.string.signup_agree_to_terms),
            isChecked = isAgreeToTerms,
            onCheckedChange = onAgreeToTermsChange
        )
    }
    ExampleButton(
        title = stringResource(R.string.signup),
        onClick = onSignUpClick,
        enabled = isFormValid,
        modifier = Modifier.alpha(if (isFormValid) 1.0f else 0.5f)
    )
}
