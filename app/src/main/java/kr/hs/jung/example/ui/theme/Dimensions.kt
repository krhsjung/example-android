package kr.hs.jung.example.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * UI 컴포넌트에서 사용하는 공통 치수 상수
 *
 * Figma 디자인 토큰 기반으로 정의되어 일관성 있는 디자인을 유지합니다.
 */
object Dimensions {

    // Spacing
    val SpacingSmall: Dp = 8.dp
    val SpacingMedium: Dp = 12.dp
    val SpacingDefault: Dp = 16.dp
    val SpacingLarge: Dp = 20.dp
    val SpacingXXLarge: Dp = 32.dp

    // Button (Figma: radius=8, py=8, text=14)
    object Button {
        val CornerRadius: Dp = 8.dp
        val BorderWidth: Dp = 1.3.dp
        val HorizontalPadding: Dp = 18.dp
        val VerticalPadding: Dp = 8.dp
        val IconSize: Dp = 20.dp
        val IconSpacing: Dp = 10.dp
        val DefaultHeight: Dp = 50.dp
        val FontSize: TextUnit = 14.sp
    }

    // Input (Figma: radius=8, px=12, py=8, text=14)
    object Input {
        val CornerRadius: Dp = 8.dp
        val HorizontalPadding: Dp = 12.dp
        val VerticalPadding: Dp = 8.dp
        val ContentHeight: Dp = 20.dp
        val FontSize: TextUnit = 14.sp
    }

    // Checkbox (Figma: size=16, gap=8, radius=4, border=1)
    object Checkbox {
        val Size: Dp = 16.dp
        val Spacing: Dp = 8.dp
        val FontSize: TextUnit = 14.sp
    }

    // Divider (Figma: text=14)
    object Divider {
        val HorizontalPadding: Dp = 8.dp
        val FontSize: TextUnit = 14.sp
    }

    // Screen (Figma: padding=25)
    object Screen {
        val HorizontalPadding: Dp = 25.dp
        val VerticalPadding: Dp = 18.dp
        val FooterHeight: Dp = 40.dp
    }

    // Social Button (Figma: height=36, gap=12)
    object SocialButton {
        val Height: Dp = 36.dp
        val HorizontalPadding: Dp = 20.dp
        val Spacing: Dp = 12.dp
    }
}
