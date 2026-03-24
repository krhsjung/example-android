package kr.hs.jung.example.ui.component.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.hs.jung.example.ui.modifier.conditional
import kr.hs.jung.example.ui.modifier.roundedBackground
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.ExampleTheme
import kr.hs.jung.example.ui.theme.InputBoxStyle

/**
 * 커스텀 입력 필드 컴포넌트
 *
 * iOS ExampleInputBox.swift와 동일 구조:
 * - leadingIcon을 ImageVector로 받아 내부에서 렌더링
 * - isSecure일 때 비밀번호 표시/숨기기 토글 아이콘 내부 처리
 *
 * @param value 현재 입력값
 * @param onValueChange 값 변경 콜백
 * @param placeholder 플레이스홀더 텍스트
 * @param modifier Modifier
 * @param isSecure 비밀번호 입력 여부 (true이면 eye toggle 자동 표시)
 * @param keyboardType 키보드 타입
 * @param enabled 활성화 여부
 * @param hasError 에러 상태 여부 (true일 때 빨간 border 표시)
 * @param leadingIcon 입력 필드 앞에 표시할 아이콘 (null이면 미표시)
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
    hasError: Boolean = false,
    leadingIcon: ImageVector? = null,
    style: InputBoxStyle = InputBoxStyle.themed
) {
    val colors = ExampleTheme.extendedColors
    val errorColor = colors.error
    val iconTint = colors.placeholderColor

    // 비밀번호 표시/숨기기 상태
    var isPasswordVisible by remember { mutableStateOf(false) }

    // TextStyle 메모라이제이션으로 리컴포지션 최적화
    val textStyle = remember(style.fontSize, style.textColor) {
        TextStyle(fontSize = style.fontSize, color = style.textColor)
    }
    val placeholderStyle = remember(style.fontSize, style.placeholderColor) {
        TextStyle(fontSize = style.fontSize, color = style.placeholderColor)
    }

    // VisualTransformation: isSecure + 표시 상태에 따라 결정
    val resolvedTransformation = remember(isSecure, isPasswordVisible) {
        if (isSecure && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None
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
            // 에러 상태일 때 빨간 border 표시
            .conditional(hasError) {
                border(
                    width = 1.dp,
                    color = errorColor,
                    shape = RoundedCornerShape(style.cornerRadius)
                )
            }
            .padding(
                horizontal = style.horizontalPadding,
                vertical = style.verticalPadding
            ),
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle,
        visualTransformation = resolvedTransformation,
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.Input.ContentHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading icon (iOS: Image(systemName: leadingIcon))
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.Button.IconSize),
                        tint = iconTint
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
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
                // 비밀번호 표시/숨기기 토글 (iOS: eye / eye.slash)
                if (isSecure) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier
                            .size(Dimensions.Button.IconSize)
                            .clickable { isPasswordVisible = !isPasswordVisible },
                        tint = iconTint
                    )
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
