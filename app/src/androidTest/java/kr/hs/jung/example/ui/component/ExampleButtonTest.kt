package kr.hs.jung.example.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import kr.hs.jung.example.ui.component.common.ExampleButton
import kr.hs.jung.example.ui.component.common.ExampleOutlinedButton
import kr.hs.jung.example.ui.theme.ButtonStyle
import kr.hs.jung.example.util.BaseComposeTest
import org.junit.Test

/**
 * ExampleButton 컴포넌트 테스트
 *
 * 버튼 컴포넌트의 렌더링, 클릭 이벤트, 활성화/비활성화 상태를 테스트합니다.
 */
class ExampleButtonTest : BaseComposeTest() {

    @Test
    fun button_displays_title_correctly() {
        // Given
        val title = "Test Button"

        // When
        setContentWithTheme {
            ExampleButton(
                title = title,
                onClick = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText(title)
            .assertIsDisplayed()
    }

    @Test
    fun button_triggers_onClick_when_clicked() {
        // Given
        var clicked = false
        val title = "Click Me"

        setContentWithTheme {
            ExampleButton(
                title = title,
                onClick = { clicked = true }
            )
        }

        // When
        composeTestRule
            .onNodeWithText(title)
            .performClick()

        // Then
        assertThat(clicked).isTrue()
    }

    @Test
    fun button_is_disabled_when_enabled_is_false() {
        // Given
        val title = "Disabled Button"

        // When
        setContentWithTheme {
            ExampleButton(
                title = title,
                onClick = {},
                enabled = false
            )
        }

        // Then
        composeTestRule
            .onNodeWithText(title)
            .assertIsNotEnabled()
    }

    @Test
    fun button_is_enabled_by_default() {
        // Given
        val title = "Enabled Button"

        // When
        setContentWithTheme {
            ExampleButton(
                title = title,
                onClick = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText(title)
            .assertIsEnabled()
    }

    @Test
    fun button_does_not_trigger_onClick_when_disabled() {
        // Given
        var clicked = false
        val title = "Disabled Button"

        setContentWithTheme {
            ExampleButton(
                title = title,
                onClick = { clicked = true },
                enabled = false
            )
        }

        // When
        composeTestRule
            .onNodeWithText(title)
            .performClick()

        // Then
        assertThat(clicked).isFalse()
    }

    @Test
    fun button_applies_custom_style() {
        // Given
        val title = "Custom Style"
        val customStyle = ButtonStyle(
            backgroundColor = Color.Blue,
            textColor = Color.White
        )

        // When
        setContentWithTheme {
            ExampleButton(
                title = title,
                onClick = {},
                style = customStyle
            )
        }

        // Then
        composeTestRule
            .onNodeWithText(title)
            .assertIsDisplayed()
    }

    @Test
    fun outlined_button_displays_text_correctly() {
        // Given
        val text = "Outlined Button"

        // When
        setContentWithTheme {
            ExampleOutlinedButton(
                text = text,
                onClick = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText(text)
            .assertIsDisplayed()
    }

    @Test
    fun outlined_button_triggers_onClick_when_clicked() {
        // Given
        var clicked = false
        val text = "Click Outlined"

        setContentWithTheme {
            ExampleOutlinedButton(
                text = text,
                onClick = { clicked = true }
            )
        }

        // When
        composeTestRule
            .onNodeWithText(text)
            .performClick()

        // Then
        assertThat(clicked).isTrue()
    }
}
