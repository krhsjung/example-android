package kr.hs.jung.example.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.hs.jung.example.R
import kr.hs.jung.example.ui.modifier.constrainedHeight
import kr.hs.jung.example.ui.theme.ButtonStyle
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme

/**
 * 커스텀 버튼 컴포넌트
 *
 * @param title 버튼 텍스트
 * @param onClick 클릭 콜백
 * @param modifier Modifier
 * @param icon 아이콘 리소스 ID (선택)
 * @param enabled 활성화 여부
 * @param style 버튼 스타일 (@Immutable로 리컴포지션 최적화)
 */
@Composable
fun ExampleButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    style: ButtonStyle = ButtonStyle.Default
) {
    // Shape 메모라이제이션으로 리컴포지션 최적화
    val shape = remember(style.cornerRadius) {
        RoundedCornerShape(style.cornerRadius)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            // Modifier.Node 기반 constrainedHeight 사용
            .constrainedHeight(
                minHeight = style.minHeight,
                maxHeight = style.maxHeight
            ),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.backgroundColor,
            contentColor = style.textColor
        ),
        border = style.borderColor?.let { BorderStroke(style.borderWidth, it) },
        contentPadding = PaddingValues(
            horizontal = style.horizontalPadding,
            vertical = style.verticalPadding
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(style.iconSize)
                )
                Spacer(modifier = Modifier.width(style.iconSpacing))
            }

            Text(
                text = title,
                fontSize = style.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = style.textColor
            )
        }
    }
}

@Composable
fun ExampleOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimensions.Button.DefaultHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text)
    }
}

/**
 * SNS 로그인 버튼
 *
 * Google, Apple 등 소셜 로그인 버튼에 사용하는 통일된 스타일입니다.
 * ButtonStyle.Sns를 사용하여 리컴포지션을 최적화합니다.
 *
 * @param title 버튼 텍스트
 * @param icon 아이콘 리소스 ID
 * @param onClick 클릭 콜백
 */
@Composable
fun SnsLoginButton(
    title: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExampleButton(
        title = title,
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        style = ButtonStyle.Sns
    )
}

@Preview(showBackground = true)
@Composable
fun ExampleButtonPreview() {
    ExampleAndroidTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 기본 로그인 버튼
            ExampleButton(title = "Login", onClick = {})

            // 기본 회원가입 버튼
            ExampleButton(title = "Sign Up", onClick = {})

            // Google SNS 버튼
            SnsLoginButton(
                title = "Continue with Google",
                icon = R.drawable.ic_google,
                onClick = {}
            )

            // Apple SNS 버튼
            SnsLoginButton(
                title = "Continue with Apple",
                icon = R.drawable.ic_apple,
                onClick = {}
            )

            // 커스텀 스타일 버튼
            ExampleButton(
                title = "Custom Style",
                onClick = {},
                style = ButtonStyle(
                    backgroundColor = Color.Blue,
                    textColor = Color.White
                )
            )
        }
    }
}
