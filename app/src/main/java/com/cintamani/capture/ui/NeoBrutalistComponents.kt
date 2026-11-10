package com.cintamani.capture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BrutalistBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalCintamaniTheme.current.surface,
    borderColor: Color = LocalCintamaniTheme.current.border,
    shadowColor: Color = LocalCintamaniTheme.current.border,
    shadowOffset: Dp = 5.dp,
    borderWidth: Dp = 3.dp,
    cornerRadius: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(modifier = modifier) {
        // Lapisan Bayangan Padat (Hard-Drop Offset Shadow tanpa blur)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset { IntOffset(shadowOffset.roundToPx(), shadowOffset.roundToPx()) }
                .background(color = shadowColor, shape = shape)
        )

        // Lapisan Kartu Permukaan Utama
        Box(
            modifier = Modifier
                .background(color = backgroundColor, shape = shape)
                .border(width = borderWidth, color = borderColor, shape = shape),
            content = content
        )
    }
}

@Composable
fun BrutalistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalCintamaniTheme.current.accent1,
    textColor: Color = if (LocalCintamaniTheme.current.isDark) Color(0xFF09090B) else Color(0xFF111111),
    borderColor: Color = LocalCintamaniTheme.current.border,
    shadowOffset: Dp = 5.dp,
    borderWidth: Dp = 3.dp,
    cornerRadius: Dp = 2.dp,
    enabled: Boolean = true,
    paddingValues: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(cornerRadius)

    val activeShadowOffset = if (isPressed) 1.dp else shadowOffset
    val contentOffset = if (isPressed) 4.dp else 0.dp

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // Bayangan padat
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset { IntOffset(shadowOffset.roundToPx(), shadowOffset.roundToPx()) }
                .background(color = borderColor, shape = shape)
        )

        // Tombol yang bergerak searah bayangan saat ditekan (tactile push effect)
        Box(
            modifier = Modifier
                .offset { IntOffset(contentOffset.roundToPx(), contentOffset.roundToPx()) }
                .background(color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f), shape = shape)
                .border(width = borderWidth, color = borderColor, shape = shape)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun BrutalistBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalCintamaniTheme.current.surface,
    textColor: Color = LocalCintamaniTheme.current.text,
    borderColor: Color = LocalCintamaniTheme.current.border
) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .border(2.dp, borderColor, shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
