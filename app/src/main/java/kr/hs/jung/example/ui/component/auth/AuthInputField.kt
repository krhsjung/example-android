package kr.hs.jung.example.ui.component.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.ui.common.ErrorMessageResolver
import kr.hs.jung.example.ui.component.common.ExampleInputBox
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.ExampleTheme

// 색상을 제외한 불변 스타일 상수 - 테마 색상은 @Composable 내에서 적용
private val LabelStyle = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium
)

private val ErrorStyle = TextStyle(
    fontSize = 14.sp
)

/**
 * 인증 폼용 입력 필드 컴포넌트
 *
 * iOS AuthInputField.swift와 동일 구조:
 * 라벨 + ExampleInputBox + 인라인 에러 텍스트를 하나의 단위로 묶음
 *
 * - 포커스 해제(blur) 시 검증 콜백 호출
 * - 에러 시 빨간 border + 인라인 에러 텍스트
 * - 타이핑 시 에러 자동 클리어 (ViewModel 측에서 처리)
 * - leadingIcon / isSecure는 ExampleInputBox 내부에서 처리
 *
 * @param label 입력 필드 위에 표시되는 라벨 텍스트
 * @param value 현재 입력값
 * @param onValueChange 값 변경 콜백 (ViewModel에서 에러 클리어도 함께 처리)
 * @param placeholder 플레이스홀더 텍스트
 * @param error 현재 필드의 유효성 검증 에러 (null이면 에러 없음)
 * @param modifier Modifier
 * @param isSecure 비밀번호 입력 여부
 * @param keyboardType 키보드 타입
 * @param leadingIcon 입력 필드 앞에 표시할 아이콘 (person/email/lock 등)
 * @param onBlurValidate 포커스 해제 시 호출되는 검증 콜백
 */
@Composable
fun AuthInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: AppError.Validation? = null,
    modifier: Modifier = Modifier,
    isSecure: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    onBlurValidate: () -> Unit = {}
) {
    val context = LocalContext.current
    val errorMessage = remember(error) {
        error?.let { ErrorMessageResolver.getMessage(context, it) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = LabelStyle.copy(color = ExampleTheme.extendedColors.textPrimary)
        )

        ExampleInputBox(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isSecure = isSecure,
            keyboardType = keyboardType,
            hasError = error != null,
            leadingIcon = leadingIcon,
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused && value.isNotEmpty()) {
                    onBlurValidate()
                }
            }
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = ErrorStyle.copy(color = ExampleTheme.extendedColors.error)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthInputFieldPreview() {
    ExampleAndroidTheme {
        AuthInputField(
            label = "Email",
            value = "",
            onValueChange = {},
            placeholder = "Enter your email",
            keyboardType = KeyboardType.Email,
            leadingIcon = Icons.Outlined.Email
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthInputFieldWithErrorPreview() {
    ExampleAndroidTheme {
        AuthInputField(
            label = "Email",
            value = "invalid",
            onValueChange = {},
            placeholder = "Enter your email",
            error = AppError.Validation.InvalidEmail,
            keyboardType = KeyboardType.Email,
            leadingIcon = Icons.Outlined.Email
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthInputFieldSecurePreview() {
    ExampleAndroidTheme {
        AuthInputField(
            label = "Password",
            value = "secret",
            onValueChange = {},
            placeholder = "Enter your password",
            isSecure = true,
            leadingIcon = Icons.Outlined.Lock
        )
    }
}
