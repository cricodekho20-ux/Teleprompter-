package com.example.ui.studio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioRed
import kotlin.math.roundToInt

@Composable
fun EyeLineGuide(
    positionPercent: Float,
    onPositionChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalHeight = constraints.maxHeight.toFloat()
        var currentY by remember(positionPercent) { mutableFloatStateOf(totalHeight * positionPercent) }

        Box(
            modifier = Modifier
                .offset { IntOffset(0, currentY.roundToInt()) }
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        currentY = (currentY + dragAmount).coerceIn(40f, totalHeight - 100f)
                        onPositionChanged(currentY / totalHeight)
                    }
                }
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Horizontal Glowing Laser Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                StudioRed.copy(alpha = 0.5f),
                                StudioRed,
                                StudioRed.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Center Eye Line Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioRed)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "EYE LINE GUIDE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
