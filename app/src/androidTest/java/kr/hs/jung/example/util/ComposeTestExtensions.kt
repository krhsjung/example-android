package kr.hs.jung.example.util

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput

/**
 * Compose UI 테스트를 위한 확장 함수 모음
 *
 * ComposeTestRule에 대한 다양한 편의 함수를 제공합니다:
 * - 노드 검색 헬퍼
 * - 액션 헬퍼
 * - 검증 헬퍼
 * - 대기 헬퍼
 */

// ============================================
// 노드 검색 헬퍼
// ============================================

/**
 * 텍스트로 노드를 찾습니다.
 */
fun ComposeTestRule.findByText(text: String): SemanticsNodeInteraction =
    onNodeWithText(text)

/**
 * 부분 텍스트로 노드를 찾습니다.
 */
fun ComposeTestRule.findByTextContains(text: String): SemanticsNodeInteraction =
    onNodeWithText(text, substring = true)

/**
 * 테스트 태그로 노드를 찾습니다.
 */
fun ComposeTestRule.findByTag(tag: String): SemanticsNodeInteraction =
    onNodeWithTag(tag)

/**
 * contentDescription으로 노드를 찾습니다.
 */
fun ComposeTestRule.findByContentDescription(description: String): SemanticsNodeInteraction =
    onNodeWithContentDescription(description)

/**
 * 텍스트를 포함한 모든 노드를 찾습니다.
 */
fun ComposeTestRule.findAllByText(text: String): SemanticsNodeInteractionCollection =
    onAllNodesWithText(text)

/**
 * 테스트 태그를 포함한 모든 노드를 찾습니다.
 */
fun ComposeTestRule.findAllByTag(tag: String): SemanticsNodeInteractionCollection =
    onAllNodesWithTag(tag)

// ============================================
// 액션 헬퍼
// ============================================

/**
 * 텍스트로 찾은 노드를 클릭합니다.
 */
fun ComposeTestRule.clickByText(text: String) {
    findByText(text).performClick()
}

/**
 * 테스트 태그로 찾은 노드를 클릭합니다.
 */
fun ComposeTestRule.clickByTag(tag: String) {
    findByTag(tag).performClick()
}

/**
 * 텍스트 필드에 텍스트를 입력합니다.
 */
fun ComposeTestRule.typeTextByTag(tag: String, text: String) {
    findByTag(tag).performTextInput(text)
}

/**
 * 텍스트 필드의 내용을 지우고 새 텍스트를 입력합니다.
 */
fun ComposeTestRule.replaceTextByTag(tag: String, text: String) {
    findByTag(tag).apply {
        performTextClearance()
        performTextInput(text)
    }
}

/**
 * 텍스트 필드의 내용을 지웁니다.
 */
fun ComposeTestRule.clearTextByTag(tag: String) {
    findByTag(tag).performTextClearance()
}

// ============================================
// 검증 헬퍼
// ============================================

/**
 * 텍스트가 화면에 표시되는지 검증합니다.
 */
fun ComposeTestRule.assertTextDisplayed(text: String) {
    findByText(text).assertIsDisplayed()
}

/**
 * 텍스트가 화면에 표시되지 않는지 검증합니다.
 */
fun ComposeTestRule.assertTextNotDisplayed(text: String) {
    findByText(text).assertDoesNotExist()
}

/**
 * 테스트 태그를 가진 노드가 표시되는지 검증합니다.
 */
fun ComposeTestRule.assertTagDisplayed(tag: String) {
    findByTag(tag).assertIsDisplayed()
}

/**
 * 테스트 태그를 가진 노드가 존재하지 않는지 검증합니다.
 */
fun ComposeTestRule.assertTagNotExists(tag: String) {
    findByTag(tag).assertDoesNotExist()
}

/**
 * 버튼이 활성화되어 있는지 검증합니다.
 */
fun ComposeTestRule.assertButtonEnabled(text: String) {
    findByText(text).assertIsEnabled()
}

/**
 * 버튼이 비활성화되어 있는지 검증합니다.
 */
fun ComposeTestRule.assertButtonDisabled(text: String) {
    findByText(text).assertIsNotEnabled()
}

/**
 * 테스트 태그를 가진 노드의 텍스트가 예상값과 일치하는지 검증합니다.
 */
fun ComposeTestRule.assertTextByTag(tag: String, expectedText: String) {
    findByTag(tag).assertTextEquals(expectedText)
}

// ============================================
// 대기 헬퍼
// ============================================

/**
 * 특정 텍스트가 나타날 때까지 대기합니다.
 *
 * @param text 대기할 텍스트
 * @param timeoutMillis 타임아웃 (기본 5초)
 */
fun ComposeTestRule.waitUntilTextDisplayed(
    text: String,
    timeoutMillis: Long = 5000
) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
}

/**
 * 특정 테스트 태그가 나타날 때까지 대기합니다.
 *
 * @param tag 대기할 테스트 태그
 * @param timeoutMillis 타임아웃 (기본 5초)
 */
fun ComposeTestRule.waitUntilTagDisplayed(
    tag: String,
    timeoutMillis: Long = 5000
) {
    waitUntil(timeoutMillis) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
}

/**
 * 특정 텍스트가 사라질 때까지 대기합니다.
 *
 * @param text 사라질 텍스트
 * @param timeoutMillis 타임아웃 (기본 5초)
 */
fun ComposeTestRule.waitUntilTextNotDisplayed(
    text: String,
    timeoutMillis: Long = 5000
) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
    }
}

/**
 * 특정 테스트 태그가 사라질 때까지 대기합니다.
 *
 * @param tag 사라질 테스트 태그
 * @param timeoutMillis 타임아웃 (기본 5초)
 */
fun ComposeTestRule.waitUntilTagNotDisplayed(
    tag: String,
    timeoutMillis: Long = 5000
) {
    waitUntil(timeoutMillis) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()
    }
}

/**
 * 조건이 만족될 때까지 대기합니다.
 *
 * @param condition 대기 조건
 * @param timeoutMillis 타임아웃 (기본 5초)
 */
fun ComposeTestRule.waitUntilCondition(
    timeoutMillis: Long = 5000,
    condition: () -> Boolean
) {
    waitUntil(timeoutMillis, condition)
}

// ============================================
// SemanticsNodeInteraction 확장
// ============================================

/**
 * 노드가 존재하지 않는지 검증합니다.
 */
fun SemanticsNodeInteraction.assertDoesNotExist(): SemanticsNodeInteraction {
    try {
        fetchSemanticsNode()
        throw AssertionError("Expected node to not exist, but it was found")
    } catch (e: AssertionError) {
        if (e.message?.contains("Expected node to not exist") == true) throw e
        // 노드가 없으면 정상
    }
    return this
}

/**
 * 노드가 특정 텍스트를 포함하는지 검증합니다.
 */
fun SemanticsNodeInteraction.assertContainsText(text: String): SemanticsNodeInteraction =
    assert(hasText(text, substring = true))

/**
 * 노드가 특정 테스트 태그를 가지고 있는지 검증합니다.
 */
fun SemanticsNodeInteraction.assertHasTag(tag: String): SemanticsNodeInteraction =
    assert(hasTestTag(tag))
