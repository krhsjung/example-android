package kr.hs.jung.example.ui.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme

// 컴포저블 외부에 정의된 불변 색상 상수 - 리컴포지션 시 재생성 방지
private val OverlayBackgroundColor = Color.Black.copy(alpha = 0.3f)
private val IndicatorColor = Color.White

/**
 * 로딩 오버레이 컴포넌트
 *
 * 화면 전체를 덮는 반투명 로딩 인디케이터
 */
@Composable
private fun ExampleLoadingOverlay(
    modifier: Modifier = Modifier
) {
    // MutableInteractionSource 메모라이제이션
    val interactionSource = remember { MutableInteractionSource() }
    // 빈 onClick 람다 메모라이제이션
    val emptyOnClick = remember { {} }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OverlayBackgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = emptyOnClick
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.scale(1.5f),
            color = IndicatorColor
        )
    }
}

/**
 * 로딩 오버레이가 포함된 컨테이너
 *
 * @param isLoading 로딩 상태
 * @param modifier 적용할 Modifier
 * @param content 컨텐츠 컴포저블
 */
@Composable
fun ExampleLoadingOverlayBox(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()
        if (isLoading) {
            ExampleLoadingOverlay()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExampleLoadingOverlayPreview() {
    ExampleAndroidTheme {
        ExampleLoadingOverlayBox(
            isLoading = true,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Content")
            }
        }
    }
}
