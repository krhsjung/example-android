package kr.hs.jung.example.ui.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier.Node API를 사용한 고성능 커스텀 Modifier
 *
 * Modifier.composed와 달리 Modifier.Node는:
 * - 컴포지션마다 새 객체를 생성하지 않음
 * - 상태 변경 시 효율적인 업데이트
 * - 메모리 사용량 감소
 */

// ============================================
// Rounded Background Modifier
// ============================================

/**
 * 둥근 모서리 배경을 그리는 Modifier
 *
 * 사용법:
 * ```kotlin
 * Modifier.roundedBackground(
 *     color = Color.Gray,
 *     cornerRadius = 16.dp
 * )
 * ```
 */
fun Modifier.roundedBackground(
    color: Color,
    cornerRadius: Dp = 16.dp
): Modifier = this then RoundedBackgroundElement(color, cornerRadius)

private data class RoundedBackgroundElement(
    val color: Color,
    val cornerRadius: Dp
) : ModifierNodeElement<RoundedBackgroundNode>() {

    override fun create() = RoundedBackgroundNode(color, cornerRadius)

    override fun update(node: RoundedBackgroundNode) {
        node.color = color
        node.cornerRadius = cornerRadius
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "roundedBackground"
        properties["color"] = color
        properties["cornerRadius"] = cornerRadius
    }
}

private class RoundedBackgroundNode(
    var color: Color,
    var cornerRadius: Dp
) : Modifier.Node(), DrawModifierNode {

    override fun ContentDrawScope.draw() {
        val radiusPx = cornerRadius.toPx()
        drawRoundRect(
            color = color,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
        )
        drawContent()
    }
}

// ============================================
// Min/Max Height Modifier
// ============================================

/**
 * 최소/최대 높이를 설정하는 Modifier
 *
 * 사용법:
 * ```kotlin
 * Modifier.constrainedHeight(
 *     minHeight = 48.dp,
 *     maxHeight = 56.dp
 * )
 * ```
 */
fun Modifier.constrainedHeight(
    minHeight: Dp? = null,
    maxHeight: Dp? = null
): Modifier {
    if (minHeight == null && maxHeight == null) return this
    return this then ConstrainedHeightElement(minHeight, maxHeight)
}

private data class ConstrainedHeightElement(
    val minHeight: Dp?,
    val maxHeight: Dp?
) : ModifierNodeElement<ConstrainedHeightNode>() {

    override fun create() = ConstrainedHeightNode(minHeight, maxHeight)

    override fun update(node: ConstrainedHeightNode) {
        node.minHeight = minHeight
        node.maxHeight = maxHeight
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "constrainedHeight"
        properties["minHeight"] = minHeight
        properties["maxHeight"] = maxHeight
    }
}

private class ConstrainedHeightNode(
    var minHeight: Dp?,
    var maxHeight: Dp?
) : Modifier.Node(), LayoutModifierNode {

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val minHeightPx = minHeight?.roundToPx() ?: constraints.minHeight
        val maxHeightPx = maxHeight?.roundToPx() ?: constraints.maxHeight

        val newConstraints = constraints.copy(
            minHeight = minHeightPx.coerceIn(constraints.minHeight, constraints.maxHeight),
            maxHeight = maxHeightPx.coerceIn(constraints.minHeight, constraints.maxHeight)
        )

        val placeable = measurable.measure(newConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}

// ============================================
// Conditional Modifier
// ============================================

/**
 * 조건부 Modifier 적용
 *
 * 사용법:
 * ```kotlin
 * Modifier.conditional(isEnabled) {
 *     background(Color.Blue)
 * }
 * ```
 */
inline fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier = if (condition) modifier() else this

/**
 * null이 아닐 때만 Modifier 적용
 *
 * 사용법:
 * ```kotlin
 * Modifier.ifNotNull(borderColor) { color ->
 *     border(1.dp, color)
 * }
 * ```
 */
inline fun <T : Any> Modifier.ifNotNull(
    value: T?,
    modifier: Modifier.(T) -> Modifier
): Modifier = if (value != null) modifier(value) else this

// ============================================
// Shimmer Loading Effect Modifier
// ============================================

/**
 * 로딩 시 shimmer 효과를 적용하는 Modifier
 *
 * 사용법:
 * ```kotlin
 * Modifier.shimmerEffect(
 *     isLoading = true,
 *     baseColor = Color.LightGray,
 *     highlightColor = Color.White
 * )
 * ```
 */
fun Modifier.shimmerEffect(
    isLoading: Boolean,
    baseColor: Color = Color.LightGray,
    highlightColor: Color = Color.White
): Modifier {
    if (!isLoading) return this
    return this then ShimmerElement(baseColor, highlightColor)
}

private data class ShimmerElement(
    val baseColor: Color,
    val highlightColor: Color
) : ModifierNodeElement<ShimmerNode>() {

    override fun create() = ShimmerNode(baseColor, highlightColor)

    override fun update(node: ShimmerNode) {
        node.baseColor = baseColor
        node.highlightColor = highlightColor
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shimmerEffect"
        properties["baseColor"] = baseColor
        properties["highlightColor"] = highlightColor
    }
}

private class ShimmerNode(
    var baseColor: Color,
    var highlightColor: Color
) : Modifier.Node(), DrawModifierNode {

    override fun ContentDrawScope.draw() {
        // 기본 shimmer 배경
        drawRect(color = baseColor)

        // TODO: 애니메이션 shimmer 효과 추가 (infinite transition 필요 시)
        // 현재는 정적 배경만 적용

        drawContent()
    }
}

// ============================================
// Clickable with Ripple (Node-based)
// ============================================

/**
 * 클릭 영역을 확장하는 Modifier
 *
 * 작은 터치 타겟을 최소 48dp로 확장합니다.
 *
 * 사용법:
 * ```kotlin
 * Modifier.expandedTouchTarget(minSize = 48.dp)
 * ```
 */
fun Modifier.expandedTouchTarget(
    minSize: Dp = 48.dp
): Modifier = this then ExpandedTouchTargetElement(minSize)

private data class ExpandedTouchTargetElement(
    val minSize: Dp
) : ModifierNodeElement<ExpandedTouchTargetNode>() {

    override fun create() = ExpandedTouchTargetNode(minSize)

    override fun update(node: ExpandedTouchTargetNode) {
        node.minSize = minSize
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "expandedTouchTarget"
        properties["minSize"] = minSize
    }
}

private class ExpandedTouchTargetNode(
    var minSize: Dp
) : Modifier.Node(), LayoutModifierNode {

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)

        val minSizePx = minSize.roundToPx()
        val width = maxOf(placeable.width, minSizePx)
        val height = maxOf(placeable.height, minSizePx)

        return layout(width, height) {
            val offsetX = (width - placeable.width) / 2
            val offsetY = (height - placeable.height) / 2
            placeable.placeRelative(offsetX, offsetY)
        }
    }
}
