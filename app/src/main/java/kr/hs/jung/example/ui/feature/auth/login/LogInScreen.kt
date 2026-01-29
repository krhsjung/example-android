package kr.hs.jung.example.ui.feature.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kr.hs.jung.example.ui.component.layout.BrandHeader
import kr.hs.jung.example.ui.component.button.ExampleButton
import kr.hs.jung.example.ui.component.divider.ExampleDividerWithText
import kr.hs.jung.example.ui.component.dialog.ExampleErrorAlert
import kr.hs.jung.example.ui.component.input.ExampleInputBox
import kr.hs.jung.example.ui.component.dialog.ExampleLoadingOverlayBox
import kr.hs.jung.example.ui.component.layout.TitleSection
import kr.hs.jung.example.ui.theme.Dimensions

/**
 * 로그인 화면 Composable
 *
 * 이메일/비밀번호 로그인 및 소셜 로그인을 제공하는 화면
 *
 * @param onNavigateToSignUp 회원가입 화면으로 이동 콜백
 * @param onLogInSuccess 로그인 성공 시 콜백
 * @param onOAuthRequest OAuth 인증 요청 시 콜백
 * @param viewModel 로그인 화면의 ViewModel
 */
@Composable
fun LogInScreen(
    onNavigateToSignUp: () -> Unit = {},
    onLogInSuccess: () -> Unit = {},
    onOAuthRequest: (SnsProvider) -> Unit = {},
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
    val onPasswordChange = remember<(String) -> Unit>(viewModel) { { viewModel.updatePassword(it) } }
    val onLogInClick = remember(viewModel) { { viewModel.logIn() } }
    val onSnsLogin = remember<(SnsProvider) -> Unit>(viewModel) { { viewModel.signInWith(it) } }
    val onClearError = remember(viewModel) { { viewModel.clearError() } }

    ExampleLoadingOverlayBox(isLoading = uiState.isLoading) {
        AuthScreenLayout(
            header = {
                BrandHeader(text = stringResource(R.string.application_name))
            }
        ) {
            TitleSection(
                title = stringResource(R.string.login_title),
                subtitle = stringResource(R.string.login_subtitle)
            )

            LogInForm(
                email = uiState.email,
                password = uiState.password,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onLogInClick = onLogInClick,
                onSignUpClick = onNavigateToSignUp
            )

            ExampleDividerWithText(
                text = stringResource(R.string.login_continue_with)
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
 * 로그인 폼
 *
 * 이메일/비밀번호 입력 필드와 로그인/회원가입 버튼
 *
 * @param email 이메일 입력값
 * @param password 비밀번호 입력값
 * @param onEmailChange 이메일 변경 콜백
 * @param onPasswordChange 비밀번호 변경 콜백
 * @param onLogInClick 로그인 버튼 클릭 콜백
 * @param onSignUpClick 회원가입 버튼 클릭 콜백
 */
@Composable
private fun LogInForm(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogInClick: () -> Unit,
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
            placeholder = stringResource(R.string.email),
            keyboardType = KeyboardType.Email
        )
        ExampleInputBox(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.password),
            isSecure = true
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        ExampleButton(
            title = stringResource(R.string.login),
            onClick = onLogInClick
        )
        ExampleButton(
            title = stringResource(R.string.signup),
            onClick = onSignUpClick
        )
    }
}
