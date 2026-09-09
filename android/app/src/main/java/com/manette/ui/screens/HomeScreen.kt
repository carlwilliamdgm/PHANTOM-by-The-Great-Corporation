package com.manette.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manette.ui.components.TGCWatermarkBadge
import com.manette.ui.theme.*
import com.manette.viewmodel.OperationMode

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun HomeScreen(
    selectedMode: OperationMode,
    isAutoDiscovered: Boolean = false,
    discoveredIp: String = "",
    backgroundUri: String = "",
    backgroundDim: Float = 0.35f,
    backgroundScale: Float = 1.0f,
    backgroundOffsetX: Float = 0.0f,
    backgroundOffsetY: Float = 0.0f,
    onModeSelect: (OperationMode) -> Unit,
    onStartGame: () -> Unit,
    onOpenConfig: () -> Unit
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF090B10), Color(0xFF101420))
                )
            )
    ) {
        if (backgroundUri.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(backgroundUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Fond d'écran global",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = backgroundScale
                        scaleY = backgroundScale
                        translationX = backgroundOffsetX * size.width
                        translationY = backgroundOffsetY * size.height
                    }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backgroundDim.coerceAtLeast(0.45f)))
            )
        }

        val isLandscape = maxWidth > 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = if (isLandscape) 24.dp else 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 14.dp else 18.dp)
        ) {
            // Top Bar with discreet brand signature in corner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TGCWatermarkBadge(text = "The Great Corporation")
            }

            // Header: "PHANTOM by The Great Corporation"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "PHANTOM",
                    fontSize = if (isLandscape) 42.sp else 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = GamepadPrimary
                )
                Text(
                    text = "by The Great Corporation",
                    fontSize = if (isLandscape) 15.sp else 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 1.2.sp,
                    color = TGCGold
                )

                // Zero-Friction Auto-Discovery Badge
                if (isAutoDiscovered && selectedMode == OperationMode.THE_GREAT) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF00E676).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "⚡ Serveur PC détecté automatiquement ($discoveredIp)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Mode Selector Cards (Row in Landscape, Column in Portrait)
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    ModeSelectionCard(
                        title = "THE GREAT",
                        badge = "SERVEUR PC",
                        description = "• Émulation Xbox 360 / PS4\n• Détection Wi-Fi automatique\n• Câble USB ADB zéro-latence",
                        isSelected = selectedMode == OperationMode.THE_GREAT,
                        accentColor = GamepadPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onModeSelect(OperationMode.THE_GREAT) }
                    )

                    ModeSelectionCard(
                        title = "PLUG & PLAY",
                        badge = "DIFFUSION PHANTOM",
                        description = "• Reconnu comme « Phantom »\n• Bluetooth HID Natif sans serveur\n• Compatible PC, Mac, TV & Consoles",
                        isSelected = selectedMode == OperationMode.PLUG_AND_PLAY,
                        accentColor = TGCGold,
                        modifier = Modifier.weight(1f),
                        onClick = { onModeSelect(OperationMode.PLUG_AND_PLAY) }
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ModeSelectionCard(
                        title = "THE GREAT (SERVEUR PC)",
                        badge = "SERVEUR PC",
                        description = "• Émulation Xbox 360 & DualShock 4\n• Détection Wi-Fi automatique sans saisie d'IP\n• Liaison USB ADB zéro-latence",
                        isSelected = selectedMode == OperationMode.THE_GREAT,
                        accentColor = GamepadPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onModeSelect(OperationMode.THE_GREAT) }
                    )

                    ModeSelectionCard(
                        title = "PLUG & PLAY (BLUETOOTH HID)",
                        badge = "DIFFUSION PHANTOM",
                        description = "• Nom réseau : « Phantom »\n• Manette standard universelle sans serveur\n• Compatible PC, Smart TV & Consoles",
                        isSelected = selectedMode == OperationMode.PLUG_AND_PLAY,
                        accentColor = TGCGold,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onModeSelect(OperationMode.PLUG_AND_PLAY) }
                    )
                }
            }

            // Action Buttons
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.88f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenConfig,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(GamepadPrimary, TGCGold)))
                    ) {
                        Text("⚙ STUDIO & CONFIG", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = onStartGame,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GamepadPrimary)
                    ) {
                        Text("▶ LANCER LA MANETTE", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF0A0C10))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartGame,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GamepadPrimary)
                    ) {
                        Text("▶ LANCER LA MANETTE", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0A0C10))
                    }

                    OutlinedButton(
                        onClick = onOpenConfig,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(GamepadPrimary, TGCGold)))
                    ) {
                        Text("⚙ STUDIO DE CONFIGURATION", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Discrete Slogan Legend at the bottom
            Text(
                text = "« The controller you don't hold, the power you command »",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
fun ModeSelectionCard(
    title: String,
    badge: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(175.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) accentColor else GamepadCard,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GamepadSurface else GamepadBackground.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else Color.White
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Text(
                text = if (isSelected) "✔ MODE ACTIF" else "Cliquer pour activer",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) accentColor else Color.Gray
            )
        }
    }
}
