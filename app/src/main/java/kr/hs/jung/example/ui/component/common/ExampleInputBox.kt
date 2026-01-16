package kr.hs.jung.example.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.InputBoxBackground
import kr.hs.jung.example.ui.theme.PlaceholderColor
import kr.hs.jung.example.ui.theme.TextPrimary

@Composable
fun ExampleInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isSecure: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    val cornerRadius = 16.dp

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(InputBoxBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = TextPrimary
        ),
        visualTransformation = if (isSecure) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isSecure) KeyboardType.Password else keyboardType
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = PlaceholderColor
                            )
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ExampleInputBoxPreview() {
    ExampleAndroidTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 빈 이메일 입력
            ExampleInputBox(
                value = "",
                onValueChange = {},
                placeholder = "Email"
            )

            // 입력된 이메일
            ExampleInputBox(
                value = "test@example.com",
                onValueChange = {},
                placeholder = "Email"
            )

            // 빈 비밀번호 입력
            ExampleInputBox(
                value = "",
                onValueChange = {},
                placeholder = "Password",
                isSecure = true
            )

            // 입력된 비밀번호
            ExampleInputBox(
                value = "password123",
                onValueChange = {},
                placeholder = "Password",
                isSecure = true
            )
        }
    }
}
