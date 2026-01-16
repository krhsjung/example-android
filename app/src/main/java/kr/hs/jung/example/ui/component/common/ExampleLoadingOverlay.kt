package kr.hs.jung.example.ui.component.common

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

@Composable
fun ExampleLoadingOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.scale(1.5f),
            color = Color.White
        )
    }
}

// Modifier extension for easy usage
fun Modifier.exampleLoadingOverlay(isLoading: Boolean): Modifier {
    return this
}

@Composable
fun Modifier.withLoadingOverlay(
    isLoading: Boolean,
    content: @Composable () -> Unit
): @Composable () -> Unit {
    return {
        Box(modifier = this) {
            content()
            if (isLoading) {
                ExampleLoadingOverlay()
            }
        }
    }
}

// Wrapper composable for convenience
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
