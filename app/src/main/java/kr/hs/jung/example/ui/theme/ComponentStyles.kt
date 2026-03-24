package kr.hs.jung.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
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
    val cornerRadius: Dp = Dimensions.Input.CornerRadius,
    val horizontalPadding: Dp = Dimensions.Input.HorizontalPadding,
    val verticalPadding: Dp = Dimensions.Input.VerticalPadding,
    val fontSize: TextUnit = Dimensions.Input.FontSize
) {
    companion object {
        /**
         * 기본 스타일 (싱글톤 - 라이트 모드 색상 고정)
         */
        @Stable
        val Default = InputBoxStyle()

        /**
         * 테마 인식 기본 스타일 (다크/라이트 모드 자동 대응)
         */
        val themed: InputBoxStyle
            @Composable
            @ReadOnlyComposable
            get() {
                val colors = ExampleTheme.extendedColors
                return InputBoxStyle(
                    backgroundColor = colors.inputBoxBackground,
                    textColor = colors.textPrimary,
                    placeholderColor = colors.placeholderColor
                )
            }
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
    val backgroundColor: Color = ButtonBackground,
    val textColor: Color = ButtonText,
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
         * 기본 버튼 스타일 (싱글톤 - 라이트 모드 색상 고정)
         */
        @Stable
        val Default = ButtonStyle()

        /**
         * 소셜 로그인 버튼 스타일 (싱글톤 - 라이트 모드 색상 고정)
         */
        @Stable
        val Social = ButtonStyle(
            backgroundColor = SocialButtonBackground,
            textColor = TextItem,
            borderColor = BorderPrimary,
            horizontalPadding = Dimensions.SocialButton.HorizontalPadding,
            minHeight = Dimensions.SocialButton.Height,
            maxHeight = Dimensions.SocialButton.Height
        )

        /**
         * 테마 인식 기본 버튼 스타일 (다크/라이트 모드 자동 대응)
         */
        val themed: ButtonStyle
            @Composable
            @ReadOnlyComposable
            get() {
                val colors = ExampleTheme.extendedColors
                return ButtonStyle(
                    backgroundColor = colors.buttonBackground,
                    textColor = colors.buttonText
                )
            }

        /**
         * 테마 인식 소셜 로그인 버튼 스타일 (다크/라이트 모드 자동 대응)
         */
        val themedSocial: ButtonStyle
            @Composable
            @ReadOnlyComposable
            get() {
                val colors = ExampleTheme.extendedColors
                return ButtonStyle(
                    backgroundColor = colors.socialButtonBackground,
                    textColor = colors.textItem,
                    borderColor = colors.borderPrimary,
                    horizontalPadding = Dimensions.SocialButton.HorizontalPadding,
                    minHeight = Dimensions.SocialButton.Height,
                    maxHeight = Dimensions.SocialButton.Height
                )
            }
    }
}
