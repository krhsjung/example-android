package kr.hs.jung.example.ui.component.divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kr.hs.jung.example.ui.theme.Dimensions
import kr.hs.jung.example.ui.theme.DividerLine
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.ui.theme.TextSecondary

/**
 * 텍스트가 포함된 구분선 컴포넌트 (재사용 가능)
 *
 * @param text 구분선 중앙에 표시할 텍스트
 * @param modifier Modifier
 * @param textColor 텍스트 색상
 * @param lineColor 구분선 색상
 * @param lineHeight 구분선 높이
 */
@Composable
fun ExampleDividerWithText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = TextSecondary,
    lineColor: Color = DividerLine,
    lineHeight: Dp = 1.5.dp
) {
    // TextStyle 메모라이제이션으로 리컴포지션 최적화
    val textStyle = remember(textColor) {
        TextStyle(fontSize = Dimensions.Divider.FontSize, color = textColor)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Divider.HorizontalPadding, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(lineHeight)
                .background(lineColor)
        )

        Text(
            text = text,
            style = textStyle
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(lineHeight)
                .background(lineColor)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExampleDividerWithTextPreview() {
    ExampleAndroidTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ExampleDividerWithText(text = "또는")
            ExampleDividerWithText(text = "signup_continue_with")
            ExampleDividerWithText(
                text = "Custom",
                textColor = Color.Red,
                lineColor = Color.Blue
            )
        }
    }
}
