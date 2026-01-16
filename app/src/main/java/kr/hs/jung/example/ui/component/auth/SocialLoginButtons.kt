package kr.hs.jung.example.ui.component.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.hs.jung.example.R
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.ui.component.common.ExampleButton
import kr.hs.jung.example.ui.theme.BorderPrimary
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.SnsButtonBackground
import kr.hs.jung.example.ui.theme.TextBlack

@Composable
fun SocialLoginButtons(
    onSnsLogin: (SnsProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExampleButton(
            title = stringResource(R.string.oauth_google),
            icon = R.drawable.ic_google,
            backgroundColor = SnsButtonBackground,
            textColor = TextBlack,
            borderColor = BorderPrimary,
            horizontalPadding = 20.dp,
            minHeight = 36.dp,
            maxHeight = 36.dp,
            onClick = { onSnsLogin(SnsProvider.GOOGLE) }
        )

        ExampleButton(
            title = stringResource(R.string.oauth_apple),
            icon = R.drawable.ic_apple,
            backgroundColor = SnsButtonBackground,
            textColor = TextBlack,
            borderColor = BorderPrimary,
            horizontalPadding = 20.dp,
            minHeight = 36.dp,
            maxHeight = 36.dp,
            onClick = { onSnsLogin(SnsProvider.APPLE) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SocialLoginButtonsPreview() {
    ExampleAndroidTheme {
        SocialLoginButtons(
            onSnsLogin = { provider ->
                println("SNS Login: $provider")
            }
        )
    }
}
