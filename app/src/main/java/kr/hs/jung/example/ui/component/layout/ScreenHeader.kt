package kr.hs.jung.example.ui.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kr.hs.jung.example.ui.theme.Brand
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.TextPrimary

// 컴포저블 외부에 정의된 불변 스타일 상수 - 리컴포지션 시 재생성 방지
private val BrandHeaderStyle = TextStyle(
    fontSize = 28.sp,
    lineHeight = 28.sp,
    fontWeight = FontWeight.Bold,
    color = Brand
)

private val TitleStyle = TextStyle(
    fontSize = 28.sp,
    lineHeight = 32.sp,
    fontWeight = FontWeight.SemiBold,
    color = TextPrimary
)

private val SubtitleStyle = TextStyle(
    fontSize = 15.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Normal,
    color = TextPrimary
)

/**
 * 화면 헤더 컴포넌트
 *
 * 앱 이름을 표시하는 브랜드 헤더입니다.
 *
 * @param text 표시할 텍스트 (앱 이름)
 */
@Composable
fun BrandHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier.padding(vertical = Dimensions.Screen.VerticalPadding),
        text = text,
        style = BrandHeaderStyle
    )
}

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
            style = TitleStyle
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = SubtitleStyle
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BrandHeaderPreview() {
    ExampleAndroidTheme {
        BrandHeader(text = "Example App")
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
