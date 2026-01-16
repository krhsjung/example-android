package kr.hs.jung.example.ui.auth.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kr.hs.jung.example.R
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.ui.component.auth.SocialLoginButtons
import kr.hs.jung.example.ui.component.common.ExampleButton
import kr.hs.jung.example.ui.component.common.ExampleCheckbox
import kr.hs.jung.example.ui.component.common.ExampleDividerWithText
import kr.hs.jung.example.ui.component.common.ExampleErrorAlert
import kr.hs.jung.example.ui.component.common.ExampleInputBox
import kr.hs.jung.example.ui.component.common.ExampleLoadingOverlayBox
import kr.hs.jung.example.ui.theme.Brand
import kr.hs.jung.example.ui.theme.TextPrimary

@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onOAuthRequest: (SnsProvider) -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val name by viewModel.name.collectAsState()
    val isAgreeToTerms by viewModel.isAgreeToTerms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showError by viewModel.showError.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val signUpSuccess by viewModel.signUpSuccess.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState(initial = false)
    val oAuthLaunchRequest by viewModel.oAuthLaunchRequest.collectAsState()

    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            viewModel.resetSignUpSuccess()
            onSignUpSuccess()
        }
    }

    LaunchedEffect(oAuthLaunchRequest) {
        oAuthLaunchRequest?.let { provider ->
            viewModel.clearOAuthLaunchRequest()
            onOAuthRequest(provider)
        }
    }

    ExampleLoadingOverlayBox(isLoading = isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignUpScreenHeader()
            SignUpScreenContainer(
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                name = name,
                isAgreeToTerms = isAgreeToTerms,
                isFormValid = isFormValid,
                onEmailChange = { viewModel.updateEmail(it) },
                onPasswordChange = { viewModel.updatePassword(it) },
                onConfirmPasswordChange = { viewModel.updateConfirmPassword(it) },
                onNameChange = { viewModel.updateName(it) },
                onAgreeToTermsChange = { viewModel.updateAgreeToTerms(it) },
                onSignUpClick = { viewModel.signUp() },
                onSnsLogin = { viewModel.signInWith(it) }
            )
        }
    }

    ExampleErrorAlert(
        isPresented = showError,
        message = errorMessage,
        onDismiss = { viewModel.clearError() }
    )
}

@Composable
private fun SignUpScreenHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.signup_title),
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        Text(
            text = stringResource(R.string.signup_subtitle),
            style = TextStyle(
                fontSize = 15.sp,
                color = TextPrimary
            )
        )
    }
}

@Composable
private fun ColumnScope.SignUpScreenContainer(
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
    onSignUpClick: () -> Unit,
    onSnsLogin: (SnsProvider) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SignUpForm(
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            name = name,
            isAgreeToTerms = isAgreeToTerms,
            isFormValid = isFormValid,
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
        ExampleInputBox(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = stringResource(R.string.confirm_password),
            isSecure = true
        )
        ExampleInputBox(
            value = name,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.name)
        )
        ExampleCheckbox(
            text = stringResource(R.string.terms_agreement),
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

@Composable
private fun SignUpScreenFooter(
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onNavigateBack) {
            Text(
                text = stringResource(R.string.login),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Brand
                )
            )
        }
    }
}
