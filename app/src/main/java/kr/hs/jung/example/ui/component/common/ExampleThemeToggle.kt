package kr.hs.jung.example.ui.component.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.hs.jung.example.ui.theme.ExampleTheme

/**
 * 라이트/다크 모드 전환 토글 버튼
 *
 * iOS ExampleThemeToggle.swift와 동일한 구조:
 * - 라이트 모드: moon(다크모드) 아이콘 표시 → 다크 모드로 전환
 * - 다크 모드: sun(라이트모드) 아이콘 표시 → 라이트 모드로 전환
 * - 40x40 크기, cornerRadius 8, border 1.5pt
 * - 테마에 따라 아이콘/배경/border 색상 자동 대응
 *
 * @param isDarkTheme 현재 다크 테마 여부
 * @param onToggle 테마 전환 콜백
 * @param modifier Modifier
 */
@Composable
fun ExampleThemeToggle(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ExampleTheme.extendedColors

    OutlinedIconButton(
        onClick = onToggle,
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, colors.borderPrimary),
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = colors.themeToggleBackground
        )
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = "Toggle theme",
            tint = colors.themeToggleIcon
        )
    }
}
