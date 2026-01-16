package kr.hs.jung.example.ui.component.common

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hs.jung.example.ui.theme.Brand
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isChecked) checkedColor else uncheckedColor
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                color = textColor
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
