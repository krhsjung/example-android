package kr.hs.jung.example.ui.component.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hs.jung.example.R
import kr.hs.jung.example.ui.theme.BorderPrimary
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.PrimaryButton
import kr.hs.jung.example.ui.theme.SnsButtonBackground
import kr.hs.jung.example.ui.theme.TextBlack

@Composable
fun ExampleButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    iconSpacing: Dp = 10.dp,
    backgroundColor: Color = PrimaryButton,
    textColor: Color = TextBlack,
    borderColor: Color? = null,
    borderWidth: Dp = 1.5.dp,
    cornerRadius: Dp = 16.dp,
    horizontalPadding: Dp = 18.dp,
    verticalPadding: Dp = 8.dp,
    minHeight: Dp? = null,
    maxHeight: Dp? = null,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(cornerRadius)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(
                when {
                    minHeight != null && maxHeight != null -> Modifier.heightIn(min = minHeight, max = maxHeight)
                    minHeight != null -> Modifier.heightIn(min = minHeight)
                    maxHeight != null -> Modifier.heightIn(max = maxHeight)
                    else -> Modifier
                }
            ),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        border = borderColor?.let { BorderStroke(borderWidth, it) },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = horizontalPadding,
            vertical = verticalPadding
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(iconSpacing))
            }

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = textColor
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
            .height(50.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text)
    }
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

            // Google SNS 버튼 (아이콘은 리소스에 추가 필요)
            ExampleButton(
                title = "Continue with Google",
                icon = R.drawable.ic_google,
                backgroundColor = SnsButtonBackground,
                textColor = TextBlack,
                borderColor = BorderPrimary,
                horizontalPadding = 20.dp,
                minHeight = 36.dp,
                maxHeight = 36.dp,
                onClick = {}
            )

            // Apple SNS 버튼
            ExampleButton(
                title = "Continue with Apple",
                icon = R.drawable.ic_apple,
                backgroundColor = SnsButtonBackground,
                textColor = TextBlack,
                borderColor = BorderPrimary,
                horizontalPadding = 20.dp,
                minHeight = 36.dp,
                maxHeight = 36.dp,
                onClick = {}
            )

            // 커스텀 색상 버튼
            ExampleButton(
                title = "Custom Color",
                backgroundColor = Color.Blue,
                textColor = Color.White,
                onClick = {}
            )
        }
    }
}
