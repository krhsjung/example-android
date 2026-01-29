package kr.hs.jung.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kr.hs.jung.example.util.logger.AppLogger

/**
 * 앱 전용 확장 컬러
 *
 * Material3 ColorScheme에 포함되지 않는 앱 전용 색상들을 정의합니다.
 * Dynamic Color 지원 여부에 따라 적절한 색상을 제공합니다.
 */
data class ExtendedColors(
    val brand: Color,
    val primaryButton: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val inputBoxBackground: Color,
    val placeholderColor: Color,
    val borderPrimary: Color,
    val dividerLine: Color,
    val snsButtonBackground: Color,
    val checkboxChecked: Color,
    val checkboxUnchecked: Color
)

private val LightExtendedColors = ExtendedColors(
    brand = Brand,
    primaryButton = PrimaryButton,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    inputBoxBackground = InputBoxBackground,
    placeholderColor = PlaceholderColor,
    borderPrimary = BorderPrimary,
    dividerLine = DividerLine,
    snsButtonBackground = SnsButtonBackground,
    checkboxChecked = CheckboxChecked,
    checkboxUnchecked = CheckboxUnchecked
)

private val DarkExtendedColors = ExtendedColors(
    brand = Brand,
    primaryButton = PrimaryButton,
    textPrimary = Color(0xFFE0E0E0),
    textSecondary = Color(0xFFB0B0B0),
    inputBoxBackground = Color(0xFF2C2C2C),
    placeholderColor = Color(0xFF808080),
    borderPrimary = Color(0xFF404040),
    dividerLine = Color(0xFF404040),
    snsButtonBackground = Color(0xFF1E1E1E),
    checkboxChecked = Brand,
    checkboxUnchecked = Color(0xFF808080)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/**
 * Material You 기반 Light Color Scheme
 */
private val LightColorScheme = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410000),
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Pink40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Background,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5DDDA),
    onSurfaceVariant = Color(0xFF534341),
    outline = Color(0xFF857371),
    outlineVariant = Color(0xFFD8C2BF),
    inverseSurface = Color(0xFF362F2E),
    inverseOnSurface = Color(0xFFFBEEEC),
    inversePrimary = Color(0xFFFFB4A8)
)

/**
 * Material You 기반 Dark Color Scheme
 */
private val DarkColorScheme = darkColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410000),
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Pink40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Background,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5DDDA),
    onSurfaceVariant = Color(0xFF534341),
    outline = Color(0xFF857371),
    outlineVariant = Color(0xFFD8C2BF),
    inverseSurface = Color(0xFF362F2E),
    inverseOnSurface = Color(0xFFFBEEEC),
    inversePrimary = Color(0xFFFFB4A8)
)

/**
 * 앱 테마
 *
 * Material You (Dynamic Color) 지원:
 * - Android 12+ (API 31+): 시스템 배경화면에서 추출한 동적 색상 사용
 * - 이전 버전: 앱에서 정의한 정적 색상 사용
 *
 * @param darkTheme 다크 테마 사용 여부 (기본값: 시스템 설정 따름)
 * @param dynamicColor 동적 색상 사용 여부 (기본값: true, Android 12+ 에서만 동작)
 * @param content 테마가 적용될 컴포저블 콘텐츠
 */
// TODO: 임시 강제 라이트 모드 - 나중에 isSystemInDarkTheme()으로 복구
private const val FORCE_LIGHT_MODE = true

@Composable
fun ExampleAndroidTheme(
    darkTheme: Boolean = if (FORCE_LIGHT_MODE) false else isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    AppLogger.d("Theme", "darkTheme: $darkTheme, dynamicColor: $dynamicColor")

    val colorScheme = when {
        // Android 12+ 에서 Dynamic Color 지원
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * 확장 컬러 접근을 위한 객체
 *
 * 사용법: ExampleTheme.extendedColors.brand
 */
object ExampleTheme {
    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
