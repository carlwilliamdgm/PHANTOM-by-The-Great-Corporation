package com.manette.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manette.ui.theme.TGCWatermark

@Composable
fun TGCWatermarkBadge(
    modifier: Modifier = Modifier,
    text: String = "The Great Corporation",
    color: Color = TGCWatermark
) {
    Text(
        text = text,
        modifier = modifier.padding(8.dp),
        color = color,
        fontSize = 11.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
}
