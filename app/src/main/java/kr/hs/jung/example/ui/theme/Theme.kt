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
    val buttonBackground: Color,
    val buttonText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textItem: Color,
    val inputBoxBackground: Color,
    val placeholderColor: Color,
    val borderPrimary: Color,
    val dividerLine: Color,
    val socialButtonBackground: Color,
    val checkboxChecked: Color,
    val checkboxUnchecked: Color,
    val error: Color,
    val linkText: Color,
    val themeToggleIcon: Color,
    val themeToggleBackground: Color
)

private val LightExtendedColors = ExtendedColors(
    brand = Brand,
    buttonBackground = ButtonBackground,
    buttonText = ButtonText,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textItem = TextItem,
    inputBoxBackground = InputBoxBackground,
    placeholderColor = PlaceholderColor,
    borderPrimary = BorderPrimary,
    dividerLine = DividerLine,
    socialButtonBackground = SocialButtonBackground,
    checkboxChecked = CheckboxChecked,
    checkboxUnchecked = CheckboxUnchecked,
    error = ErrorColor,
    linkText = LinkTextColor,
    themeToggleIcon = ThemeToggleIcon,
    themeToggleBackground = ThemeToggleBackground
)

private val DarkExtendedColors = ExtendedColors(
    brand = Brand,
    buttonBackground = Color(0xFFE0E0E0),
    buttonText = Color(0xFF1A1A1A),
    textPrimary = Color(0xFFE0E0E0),
    textSecondary = Color(0xFFB0B0B0),
    textItem = Color(0xFFE0E0E0),
    inputBoxBackground = Color(0xFF2C2C2C),
    placeholderColor = Color(0xFF808080),
    borderPrimary = Color(0xFF404040),
    dividerLine = Color(0xFF404040),
    socialButtonBackground = Color(0xFF1E1E1E),
    checkboxChecked = Brand,
    checkboxUnchecked = Color(0xFF808080),
    error = Color(0xFFFF6B6B),
    linkText = Brand,
    themeToggleIcon = Color(0xFFFAFAFA),
    themeToggleBackground = Color(0xFF181C26)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/**
 * Material You 기반 Light Color Scheme
 */
private val LightColorScheme = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
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
    surfaceVariant = Color(0xFFF3F3F5),
    onSurfaceVariant = Color(0xFF4A5565),
    outline = Color(0xFF717182),
    outlineVariant = Color(0xFFE0E0E0),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFF9ECAFF)
)

/**
 * Material You 기반 Dark Color Scheme
 */
private val DarkColorScheme = darkColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF003258),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Pink80,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Brand
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
@Composable
fun ExampleAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
