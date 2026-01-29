package kr.hs.jung.example.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * UI 컴포넌트에서 사용하는 공통 치수 상수
 *
 * Magic Number를 상수화하여 일관성 있는 디자인을 유지합니다.
 */
object Dimensions {

    // Corner Radius
    val CornerRadiusSmall: Dp = 8.dp
    val CornerRadiusMedium: Dp = 12.dp
    val CornerRadiusDefault: Dp = 16.dp
    val CornerRadiusLarge: Dp = 24.dp

    // Spacing
    val SpacingXSmall: Dp = 4.dp
    val SpacingSmall: Dp = 8.dp
    val SpacingMedium: Dp = 12.dp
    val SpacingDefault: Dp = 16.dp
    val SpacingLarge: Dp = 20.dp
    val SpacingXLarge: Dp = 24.dp
    val SpacingXXLarge: Dp = 32.dp

    // Padding
    val PaddingSmall: Dp = 8.dp
    val PaddingMedium: Dp = 12.dp
    val PaddingDefault: Dp = 16.dp
    val PaddingLarge: Dp = 20.dp

    // Button
    object Button {
        val CornerRadius: Dp = 16.dp
        val BorderWidth: Dp = 1.5.dp
        val HorizontalPadding: Dp = 18.dp
        val VerticalPadding: Dp = 8.dp
        val IconSize: Dp = 20.dp
        val IconSpacing: Dp = 10.dp
        val MinHeight: Dp = 36.dp
        val DefaultHeight: Dp = 50.dp
        val FontSize: TextUnit = 15.sp
    }

    // InputBox
    object InputBox {
        val CornerRadius: Dp = 16.dp
        val HorizontalPadding: Dp = 12.dp
        val VerticalPadding: Dp = 8.dp
        val ContentHeight: Dp = 20.dp
        val FontSize: TextUnit = 15.sp
    }

    // Checkbox
    object Checkbox {
        val Size: Dp = 24.dp
        val CornerRadius: Dp = 4.dp
        val BorderWidth: Dp = 1.5.dp
        val Spacing: Dp = 8.dp
        val FontSize: TextUnit = 14.sp
    }

    // Divider
    object Divider {
        val Height: Dp = 1.dp
        val LineHeight: Dp = 1.5.dp
        val HorizontalPadding: Dp = 8.dp
        val FontSize: TextUnit = 12.sp
    }

    // Screen
    object Screen {
        val HorizontalPadding: Dp = 20.dp
        val VerticalPadding: Dp = 18.dp
        val FooterHeight: Dp = 40.dp
    }

    // SNS Button
    object SnsButton {
        val Height: Dp = 36.dp
        val HorizontalPadding: Dp = 20.dp
    }
}
