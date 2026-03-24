package kr.hs.jung.example.ui.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.ExampleTheme

// 색상을 제외한 불변 스타일 상수 - 테마 색상은 @Composable 내에서 적용
private val TitleStyle = TextStyle(
    fontSize = 30.sp,
    lineHeight = 36.sp,
    fontWeight = FontWeight.Bold
)

private val SubtitleStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Normal
)

/**
 * 타이틀/서브타이틀 섹션 컴포넌트
 *
 * 화면의 타이틀과 서브타이틀을 표시합니다.
 *
 * @param title 메인 타이틀
 * @param subtitle 서브타이틀 (선택적)
 * @param horizontalAlignment 가로 정렬 (기본값: Start)
 */
@Composable
fun TitleSection(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall, Alignment.Top),
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            text = title,
            style = TitleStyle.copy(color = ExampleTheme.extendedColors.textPrimary)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = SubtitleStyle.copy(color = ExampleTheme.extendedColors.textSecondary)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TitleSectionPreview() {
    ExampleAndroidTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)) {
            TitleSection(
                title = "Welcome Back",
                subtitle = "Sign in to continue"
            )
            TitleSection(
                title = "Create Account",
                subtitle = "Sign up to get started",
                horizontalAlignment = Alignment.CenterHorizontally
            )
        }
    }
}
