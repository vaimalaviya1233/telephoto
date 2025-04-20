package me.saket.telephoto.zoomable.internal

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.unit.LayoutDirection
import me.saket.telephoto.zoomable.zoomable
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Calculates the top-left offset of this [Rect] such that it always overlaps with [destination].
 *
 * This is used by [Modifier.zoomable] to prevent panning of its content outside of its layout bounds.
 */
internal fun Rect.calculateTopLeftToOverlapWith(
  viewportSize: Size,
  paddedViewportBounds: Rect,
  alignment: Alignment,
  layoutDirection: LayoutDirection,
): Offset {
  check(viewportSize.isSpecified) {
    "Whoops Modifier.zoomable() is not supposed to handle gestures yet. " +
      "Please file an issue on https://github.com/saket/telephoto/issues?"
  }

  // For content larger than viewport: constrain to ensure overlap
  if (width >= viewportSize.width || height >= viewportSize.height) {
    return topLeft.copy(
      x = if (width >= viewportSize.width) {
        topLeft.x.coerceIn(
          minimumValue = (viewportSize.width - width).coerceAtMost(0f),
          maximumValue = 0f
        )
      } else {
        // When width is smaller but height is larger, calculate horizontal alignment position
        val alignedOffset = alignment.align(
          size = Size(width, 0f).roundToIntSize(),
          space = Size(viewportSize.width, 0f).roundToIntSize(),
          layoutDirection = layoutDirection,
        )
        alignedOffset.x.toFloat() + paddedViewportBounds.left
      },
      y = if (height >= viewportSize.height) {
        topLeft.y.coerceIn(
          minimumValue = (viewportSize.height - height).coerceAtMost(0f),
          maximumValue = 0f
        )
      } else {
        // When height is smaller but width is larger, calculate vertical alignment position
        val alignedOffset = alignment.align(
          size = Size(0f, height).roundToIntSize(),
          space = Size(0f, viewportSize.height).roundToIntSize(),
          layoutDirection = layoutDirection,
        )
        alignedOffset.y.toFloat() + paddedViewportBounds.top
      }
    )
  }

  // For content smaller than viewport in both dimensions: use alignment
  val alignedOffset = alignment.align(
    size = size.roundToIntSize(),
    space = paddedViewportBounds.size.roundToIntSize(),
    layoutDirection = layoutDirection,
  )

  return Offset(
    x = alignedOffset.x.toFloat() + paddedViewportBounds.left,
    y = alignedOffset.y.toFloat() + paddedViewportBounds.top
  )
}
