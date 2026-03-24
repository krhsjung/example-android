package kr.hs.jung.example.ui.component.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kr.hs.jung.example.R
import kr.hs.jung.example.domain.model.LoginProvider
import kr.hs.jung.example.ui.component.common.SocialLoginButton
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme

/**
 * 소셜 로그인 버튼 그룹 (텍스트+아이콘 Column 레이아웃)
 *
 * 로그인 화면에서 사용
 */
@Composable
fun SocialLoginButtons(
    onSocialLogin: (LoginProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    // 람다 안정화 - 리컴포지션 시 람다 재생성 방지
    val onGoogleClick = remember(onSocialLogin) { { onSocialLogin(LoginProvider.GOOGLE) } }
    val onAppleClick = remember(onSocialLogin) { { onSocialLogin(LoginProvider.APPLE) } }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SocialLoginButton(
            title = stringResource(R.string.oauth_google),
            icon = R.drawable.ic_google,
            onClick = onGoogleClick
        )

        SocialLoginButton(
            title = stringResource(R.string.oauth_apple),
            icon = R.drawable.ic_apple,
            onClick = onAppleClick
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SocialLoginButtonsPreview() {
    ExampleAndroidTheme {
        SocialLoginButtons(
            onSocialLogin = { provider ->
                println("Social Login: $provider")
            }
        )
    }
}
