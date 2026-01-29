package kr.hs.jung.example.ui.component.checkbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import kr.hs.jung.example.ui.theme.Brand
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.TextPrimary

@Composable
fun ExampleCheckbox(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedColor: Color = Brand,
    uncheckedColor: Color = Color.Gray,
    textColor: Color = TextPrimary
) {
    // TextStyle 메모라이제이션으로 리컴포지션 최적화
    val textStyle = remember(textColor) {
        TextStyle(fontSize = Dimensions.Checkbox.FontSize, color = textColor)
    }

    // 아이콘 및 tint 메모라이제이션
    val icon = remember(isChecked) {
        if (isChecked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank
    }
    val tint = remember(isChecked, checkedColor, uncheckedColor) {
        if (isChecked) checkedColor else uncheckedColor
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Checkbox.Spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.Checkbox.Size),
            tint = tint
        )
        Text(
            text = text,
            style = textStyle
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
