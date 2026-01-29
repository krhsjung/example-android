package kr.hs.jung.example.ui.component.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.hs.jung.example.ui.modifier.roundedBackground
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.InputBoxStyle

/**
 * 커스텀 입력 필드 컴포넌트
 *
 * @param value 현재 입력값
 * @param onValueChange 값 변경 콜백
 * @param placeholder 플레이스홀더 텍스트
 * @param modifier Modifier
 * @param isSecure 비밀번호 입력 여부
 * @param keyboardType 키보드 타입
 * @param enabled 활성화 여부
 * @param style 스타일 설정 (@Immutable로 리컴포지션 최적화)
 */
@Composable
fun ExampleInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isSecure: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    style: InputBoxStyle = InputBoxStyle.Default
) {
    // TextStyle 메모라이제이션으로 리컴포지션 최적화
    val textStyle = remember(style.fontSize, style.textColor) {
        TextStyle(fontSize = style.fontSize, color = style.textColor)
    }
    val placeholderStyle = remember(style.fontSize, style.placeholderColor) {
        TextStyle(fontSize = style.fontSize, color = style.placeholderColor)
    }

    // VisualTransformation 메모라이제이션
    val visualTransformation = remember(isSecure) {
        if (isSecure) PasswordVisualTransformation() else VisualTransformation.None
    }

    // KeyboardOptions 메모라이제이션
    val keyboardOptions = remember(isSecure, keyboardType) {
        KeyboardOptions(keyboardType = if (isSecure) KeyboardType.Password else keyboardType)
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            // Modifier.Node 기반 roundedBackground 사용
            .roundedBackground(
                color = style.backgroundColor,
                cornerRadius = style.cornerRadius
            )
            .padding(
                horizontal = style.horizontalPadding,
                vertical = style.verticalPadding
            ),
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.InputBox.ContentHeight),
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
                            style = placeholderStyle
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
