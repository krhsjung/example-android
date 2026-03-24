package kr.hs.jung.example.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.ExampleTheme

/**
 * Figma 스타일 커스텀 체크박스
 *
 * Figma: 16x16, radius 4, bg #f3f3f5 (unchecked), border rgba(0,0,0,0.1)
 * Checked: bg #030213, checkmark icon
 *
 * iOS ExampleCheckbox<Label: View>와 동일한 generic label 슬롯 패턴
 *
 * @param isChecked 체크 상태
 * @param onCheckedChange 체크 상태 변경 콜백
 * @param modifier Modifier
 * @param label 체크박스 옆에 표시할 라벨 콘텐츠
 */
@Composable
fun ExampleCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit
) {
    val colors = ExampleTheme.extendedColors
    val checkboxShape = RoundedCornerShape(4.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Checkbox.Spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimensions.Checkbox.Size)
                .clip(checkboxShape)
                .background(
                    if (isChecked) colors.checkboxChecked else colors.inputBoxBackground,
                    checkboxShape
                )
                .border(1.dp, colors.borderPrimary, checkboxShape),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = colors.buttonText
                )
            }
        }
        label()
    }
}

/**
 * 텍스트 라벨 편의 오버로드
 */
@Composable
fun ExampleCheckbox(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ExampleTheme.extendedColors

    ExampleCheckbox(
        isChecked = isChecked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = Dimensions.Checkbox.FontSize,
                color = colors.textSecondary
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExampleCheckboxPreview() {
    ExampleAndroidTheme {
        ExampleCheckbox(
            text = "I agree to the Terms of Service",
            isChecked = true,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExampleCheckboxUncheckedPreview() {
    ExampleAndroidTheme {
        ExampleCheckbox(
            text = "I agree to the Terms of Service",
            isChecked = false,
            onCheckedChange = {}
        )
    }
}
