package kr.hs.jung.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * Compose 최적화를 위한 스타일 객체들
 *
 * @Stable/@Immutable 마킹으로 불필요한 리컴포지션을 방지합니다.
 * 복잡한 파라미터를 단일 객체로 통합하여 안정성을 보장합니다.
 */

/**
 * InputBox 스타일 설정
 *
 * @property backgroundColor 배경색
 * @property textColor 텍스트 색상
 * @property placeholderColor 플레이스홀더 색상
 * @property cornerRadius 모서리 둥글기
 * @property horizontalPadding 수평 패딩
 * @property verticalPadding 수직 패딩
 * @property fontSize 폰트 크기
 */
@Immutable
data class InputBoxStyle(
    val backgroundColor: Color = InputBoxBackground,
    val textColor: Color = TextPrimary,
    val placeholderColor: Color = PlaceholderColor,
    val cornerRadius: Dp = Dimensions.InputBox.CornerRadius,
    val horizontalPadding: Dp = Dimensions.InputBox.HorizontalPadding,
    val verticalPadding: Dp = Dimensions.InputBox.VerticalPadding,
    val fontSize: TextUnit = Dimensions.InputBox.FontSize
) {
    companion object {
        /**
         * 기본 스타일 (싱글톤으로 리컴포지션 최적화)
         */
        @Stable
        val Default = InputBoxStyle()
    }
}

/**
 * Button 스타일 설정
 *
 * @property backgroundColor 배경색
 * @property textColor 텍스트 색상
 * @property borderColor 테두리 색상 (null이면 테두리 없음)
 * @property borderWidth 테두리 두께
 * @property cornerRadius 모서리 둥글기
 * @property horizontalPadding 수평 패딩
 * @property verticalPadding 수직 패딩
 * @property fontSize 폰트 크기
 * @property iconSize 아이콘 크기
 * @property iconSpacing 아이콘-텍스트 간격
 */
@Immutable
data class ButtonStyle(
    val backgroundColor: Color = PrimaryButton,
    val textColor: Color = TextBlack,
    val borderColor: Color? = null,
    val borderWidth: Dp = Dimensions.Button.BorderWidth,
    val cornerRadius: Dp = Dimensions.Button.CornerRadius,
    val horizontalPadding: Dp = Dimensions.Button.HorizontalPadding,
    val verticalPadding: Dp = Dimensions.Button.VerticalPadding,
    val fontSize: TextUnit = Dimensions.Button.FontSize,
    val iconSize: Dp = Dimensions.Button.IconSize,
    val iconSpacing: Dp = Dimensions.Button.IconSpacing,
    val minHeight: Dp? = null,
    val maxHeight: Dp? = null
) {
    companion object {
        /**
         * 기본 버튼 스타일
         */
        @Stable
        val Default = ButtonStyle()

        /**
         * SNS 로그인 버튼 스타일
         */
        @Stable
        val Sns = ButtonStyle(
            backgroundColor = SnsButtonBackground,
            textColor = TextBlack,
            borderColor = BorderPrimary,
            horizontalPadding = Dimensions.SnsButton.HorizontalPadding,
            minHeight = Dimensions.SnsButton.Height,
            maxHeight = Dimensions.SnsButton.Height
        )
    }
}

/**
 * Checkbox 스타일 설정
 */
@Immutable
data class CheckboxStyle(
    val size: Dp = Dimensions.Checkbox.Size,
    val cornerRadius: Dp = Dimensions.Checkbox.CornerRadius,
    val borderWidth: Dp = Dimensions.Checkbox.BorderWidth,
    val spacing: Dp = Dimensions.Checkbox.Spacing,
    val fontSize: TextUnit = Dimensions.Checkbox.FontSize,
    val checkedColor: Color = CheckboxChecked,
    val uncheckedColor: Color = CheckboxUnchecked,
    val borderColor: Color = BorderPrimary,
    val textColor: Color = TextPrimary
) {
    companion object {
        @Stable
        val Default = CheckboxStyle()
    }
}
