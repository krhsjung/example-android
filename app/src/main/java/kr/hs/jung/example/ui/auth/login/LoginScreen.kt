package kr.hs.jung.example.ui.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kr.hs.jung.example.ui.component.common.ExampleDividerWithText
import kr.hs.jung.example.ui.component.common.ExampleErrorAlert
import kr.hs.jung.example.ui.component.common.ExampleInputBox
import kr.hs.jung.example.ui.component.common.ExampleLoadingOverlayBox
import kr.hs.jung.example.ui.theme.Brand
import kr.hs.jung.example.ui.theme.TextPrimary

@Composable
fun LogInScreen(
    onNavigateToSignUp: () -> Unit = {},
    onLogInSuccess: () -> Unit = {},
    onOAuthRequest: (SnsProvider) -> Unit = {},
    viewModel: LogInViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val showError by viewModel.showError.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val logInSuccess by viewModel.logInSuccess.collectAsState()
    val oAuthLaunchRequest by viewModel.oAuthLaunchRequest.collectAsState()

    LaunchedEffect(logInSuccess) {
        if (logInSuccess) {
            viewModel.resetLogInSuccess()
            onLogInSuccess()
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogInScreenHeader()

            LogInScreenContainer(
                viewModel = viewModel,
                onSignUpClick = onNavigateToSignUp
            )

            LogInScreenFooter()
        }
    }

    ExampleErrorAlert(
        isPresented = showError,
        message = errorMessage,
        onDismiss = { viewModel.clearError() }
    )
}

@Composable
private fun LogInScreenHeader() {
    Text(
        modifier = Modifier.padding(vertical = 18.dp),
        text = stringResource(R.string.application_name),
        style = TextStyle(
            fontSize = 28.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Brand
        )
    )
}

@Composable
private fun ColumnScope.LogInScreenContainer(
    viewModel: LogInViewModel,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogInDescription()

        LogInForm(
            viewModel = viewModel,
            onSignUpClick = onSignUpClick
        )

        ExampleDividerWithText(
            text = stringResource(R.string.login_continue_with)
        )

        SocialLoginButtons(
            onSnsLogin = { provider ->
                viewModel.signInWith(provider)
            }
        )
    }
}

@Composable
private fun LogInDescription() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = TextStyle(
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        )

        Text(
            text = stringResource(R.string.login_subtitle),
            style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary
            )
        )
    }
}

@Composable
private fun LogInForm(
    viewModel: LogInViewModel,
    onSignUpClick: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.Start
    ) {
        ExampleInputBox(
            value = email,
            onValueChange = { viewModel.updateEmail(it) },
            placeholder = stringResource(R.string.email),
            keyboardType = KeyboardType.Email
        )
        ExampleInputBox(
            value = password,
            onValueChange = { viewModel.updatePassword(it) },
            placeholder = stringResource(R.string.password),
            isSecure = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExampleButton(
            title = stringResource(R.string.login),
            onClick = { viewModel.logIn() }
        )
        ExampleButton(
            title = stringResource(R.string.signup),
            onClick = onSignUpClick
        )
    }
}

@Composable
private fun LogInScreenFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
    }
}
