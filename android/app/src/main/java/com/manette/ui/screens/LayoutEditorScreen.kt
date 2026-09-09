package com.manette.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.manette.config.ButtonPosition
import com.manette.config.LayoutDefaults
import com.manette.ui.components.getSkinTheme
import com.manette.ui.theme.*
import com.manette.viewmodel.GameViewModel

@Composable
fun LayoutEditorScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Verrouillage en mode paysage pour l'éditeur
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val originalOrientation = activity?.requestedOrientation
            ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    val backgroundUri by viewModel.backgroundUri.collectAsState()
    val backgroundDim by viewModel.backgroundDim.collectAsState()
    val backgroundScale by viewModel.backgroundScale.collectAsState()
    val backgroundOffsetX by viewModel.backgroundOffsetX.collectAsState()
    val backgroundOffsetY by viewModel.backgroundOffsetY.collectAsState()
    val skin by viewModel.skin.collectAsState()
    val savedPositions by viewModel.customLayout.collectAsState()

    val theme = getSkinTheme(skin)

    // Mode d'édition : "controls" (touches) ou "background" (arrière-plan)
    var editorMode by remember { mutableStateOf("controls") }

    // État local de l'arrière-plan (modifiable librement)
    var bgScale by remember(backgroundScale) { mutableStateOf(backgroundScale) }
    var bgOffsetX by remember(backgroundOffsetX) { mutableStateOf(backgroundOffsetX) }
    var bgOffsetY by remember(backgroundOffsetY) { mutableStateOf(backgroundOffsetY) }
    var bgDim by remember(backgroundDim) { mutableStateOf(backgroundDim) }

    // Sélecteur d'image direct dans l'éditeur
    val bgPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.setBackgroundUri(it.toString())
        }
    }

    // Copie de travail locale
    var positions by remember(savedPositions) {
        mutableStateOf(LayoutDefaults.getEffectivePositions(savedPositions).toMutableMap())
    }

    var selectedKey by remember { mutableStateOf<String?>("left_stick") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GamepadBackground)
    ) {
        // ── 1. Fond d'écran global avec dim & cadrage en temps réel ──────────
        if (backgroundUri.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(backgroundUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = bgScale
                        scaleY = bgScale
                        translationX = bgOffsetX * size.width
                        translationY = bgOffsetY * size.height
                    }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = bgDim))
            )
        }

        // Grille d'aide discrète
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (editorMode == "background") 0.10f else 0.35f))
        )

        // ── Zone tactile plein écran pour manipuler l'arrière-plan ────────────
        if (editorMode == "background") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            bgScale = (bgScale * zoom).coerceIn(1.0f, 5.0f)
                            bgOffsetX = (bgOffsetX + pan.x / (1600f * bgScale)).coerceIn(-1.0f, 1.0f)
                            bgOffsetY = (bgOffsetY + pan.y / (720f * bgScale)).coerceIn(-1.0f, 1.0f)
                        }
                    }
            )
        }

        // ── 2. Zone Canvas Éditeur ────────────────────────────────────────────
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenW = maxWidth.value
            val screenH = maxHeight.value
            val isControlsMode = editorMode == "controls"

            // 13 Contrôles éditables
            positions.forEach { (key, pos) ->
                val isSelected = selectedKey == key && isControlsMode

                when (key) {
                    "left_stick", "right_stick" -> {
                        val size = (140f * pos.size).dp
                        EditorDraggableControl(
                            x = pos.x,
                            y = pos.y,
                            itemWidth = size,
                            itemHeight = size,
                            screenW = screenW,
                            screenH = screenH,
                            enabled = isControlsMode,
                            onSelect = { selectedKey = key },
                            onDrag = { dx, dy ->
                                val cur = positions[key] ?: return@EditorDraggableControl
                                val newX = (cur.x + dx / screenW).coerceIn(0.01f, 0.99f)
                                val newY = (cur.y + dy / screenH).coerceIn(0.02f, 0.98f)
                                positions = positions.toMutableMap().apply {
                                    this[key] = cur.copy(x = newX, y = newY)
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = if (isControlsMode) 1.0f else 0.50f }
                                    .clip(CircleShape)
                                    .background(Color(0xFF161B26).copy(alpha = 0.85f))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.5.dp,
                                        color = if (isSelected) GamepadPrimary else Color(0xFF353B4E),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size((50f * pos.size).dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) GamepadPrimary else TGCGold.copy(alpha = 0.8f))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (key == "left_stick") "L-STICK" else "R-STICK",
                                        fontSize = (10 * pos.size).toInt().coerceAtLeast(8).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    "dpad" -> {
                        val size = (130f * pos.size).dp
                        val btnSize = (42f * pos.size).dp
                        EditorDraggableControl(
                            x = pos.x,
                            y = pos.y,
                            itemWidth = size,
                            itemHeight = size,
                            screenW = screenW,
                            screenH = screenH,
                            enabled = isControlsMode,
                            onSelect = { selectedKey = key },
                            onDrag = { dx, dy ->
                                val cur = positions[key] ?: return@EditorDraggableControl
                                val newX = (cur.x + dx / screenW).coerceIn(0.01f, 0.99f)
                                val newY = (cur.y + dy / screenH).coerceIn(0.02f, 0.98f)
                                positions = positions.toMutableMap().apply {
                                    this[key] = cur.copy(x = newX, y = newY)
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = if (isControlsMode) 1.0f else 0.50f }
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) GamepadPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.align(Alignment.TopCenter).size(btnSize),
                                    shape = RoundedCornerShape(8.dp),
                                    color = theme.dpadColor
                                ) { Box(contentAlignment = Alignment.Center) { Text("▲", color = Color.White) } }
                                Surface(
                                    modifier = Modifier.align(Alignment.BottomCenter).size(btnSize),
                                    shape = RoundedCornerShape(8.dp),
                                    color = theme.dpadColor
                                ) { Box(contentAlignment = Alignment.Center) { Text("▼", color = Color.White) } }
                                Surface(
                                    modifier = Modifier.align(Alignment.CenterStart).size(btnSize),
                                    shape = RoundedCornerShape(8.dp),
                                    color = theme.dpadColor
                                ) { Box(contentAlignment = Alignment.Center) { Text("◀", color = Color.White) } }
                                Surface(
                                    modifier = Modifier.align(Alignment.CenterEnd).size(btnSize),
                                    shape = RoundedCornerShape(8.dp),
                                    color = theme.dpadColor
                                ) { Box(contentAlignment = Alignment.Center) { Text("▶", color = Color.White) } }
                            }
                        }
                    }

                    "abxy" -> {
                        val clusterSize = (130f * pos.size).dp
                        val btnSize = (44f * pos.size).dp

                        EditorDraggableControl(
                            x = pos.x,
                            y = pos.y,
                            itemWidth = clusterSize,
                            itemHeight = clusterSize,
                            screenW = screenW,
                            screenH = screenH,
                            enabled = isControlsMode,
                            onSelect = { selectedKey = key },
                            onDrag = { dx, dy ->
                                val cur = positions[key] ?: return@EditorDraggableControl
                                val newX = (cur.x + dx / screenW).coerceIn(0.01f, 0.99f)
                                val newY = (cur.y + dy / screenH).coerceIn(0.02f, 0.98f)
                                positions = positions.toMutableMap().apply {
                                    this[key] = cur.copy(x = newX, y = newY)
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = if (isControlsMode) 1.0f else 0.50f }
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) GamepadPrimary.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) GamepadPrimary else TGCGold.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Touche Y (Haut)
                                Surface(
                                    modifier = Modifier.align(Alignment.TopCenter).size(btnSize),
                                    shape = CircleShape,
                                    color = theme.yColor,
                                    shadowElevation = 3.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = theme.yLabel,
                                            fontSize = (16 * pos.size).toInt().coerceAtLeast(10).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                // Touche A (Bas)
                                Surface(
                                    modifier = Modifier.align(Alignment.BottomCenter).size(btnSize),
                                    shape = CircleShape,
                                    color = theme.aColor,
                                    shadowElevation = 3.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = theme.aLabel,
                                            fontSize = (16 * pos.size).toInt().coerceAtLeast(10).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                // Touche X (Gauche)
                                Surface(
                                    modifier = Modifier.align(Alignment.CenterStart).size(btnSize),
                                    shape = CircleShape,
                                    color = theme.xColor,
                                    shadowElevation = 3.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = theme.xLabel,
                                            fontSize = (16 * pos.size).toInt().coerceAtLeast(10).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                // Touche B (Droite)
                                Surface(
                                    modifier = Modifier.align(Alignment.CenterEnd).size(btnSize),
                                    shape = CircleShape,
                                    color = theme.bColor,
                                    shadowElevation = 3.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = theme.bLabel,
                                            fontSize = (16 * pos.size).toInt().coerceAtLeast(10).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "btn_lt", "btn_lb", "btn_rt", "btn_rb" -> {
                        val size = (54f * pos.size).dp
                        val lbl = when (key) {
                            "btn_lt" -> "LT"
                            "btn_lb" -> "LB"
                            "btn_rt" -> "RT"
                            else -> "RB"
                        }

                        EditorDraggableControl(
                            x = pos.x,
                            y = pos.y,
                            itemWidth = size,
                            itemHeight = size,
                            screenW = screenW,
                            screenH = screenH,
                            enabled = isControlsMode,
                            onSelect = { selectedKey = key },
                            onDrag = { dx, dy ->
                                val cur = positions[key] ?: return@EditorDraggableControl
                                val newX = (cur.x + dx / screenW).coerceIn(0.01f, 0.99f)
                                val newY = (cur.y + dy / screenH).coerceIn(0.02f, 0.98f)
                                positions = positions.toMutableMap().apply {
                                    this[key] = cur.copy(x = newX, y = newY)
                                }
                            }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = if (isControlsMode) 1.0f else 0.50f },
                                shape = RoundedCornerShape(12.dp),
                                color = theme.bumperColor,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, GamepadPrimary) else null,
                                shadowElevation = 3.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = lbl,
                                        fontSize = (15 * pos.size).toInt().sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    "btn_back", "btn_start" -> {
                        val size = (46f * pos.size).dp
                        val lbl = if (key == "btn_back") "BACK" else "START"

                        EditorDraggableControl(
                            x = pos.x,
                            y = pos.y,
                            itemWidth = size,
                            itemHeight = size,
                            screenW = screenW,
                            screenH = screenH,
                            enabled = isControlsMode,
                            onSelect = { selectedKey = key },
                            onDrag = { dx, dy ->
                                val cur = positions[key] ?: return@EditorDraggableControl
                                val newX = (cur.x + dx / screenW).coerceIn(0.01f, 0.99f)
                                val newY = (cur.y + dy / screenH).coerceIn(0.02f, 0.98f)
                                positions = positions.toMutableMap().apply {
                                    this[key] = cur.copy(x = newX, y = newY)
                                }
                            }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = if (isControlsMode) 1.0f else 0.50f },
                                shape = RoundedCornerShape(10.dp),
                                color = theme.centerColor,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, GamepadPrimary) else null,
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = lbl,
                                        fontSize = (9 * pos.size).toInt().sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── 3. Barre d'outils supérieure flottante ───────────────────────────
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.Black.copy(alpha = 0.88f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232838))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = GamepadPrimary)
                }

                // Sélecteur d'onglet : Touches vs Arrière-Plan
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B2030))
                        .padding(2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (editorMode == "controls") GamepadPrimary else Color.Transparent,
                        modifier = Modifier.clickable { editorMode = "controls" }
                    ) {
                        Text(
                            "🎮 Touches",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (editorMode == "controls") Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (editorMode == "background") TGCGold else Color.Transparent,
                        modifier = Modifier.clickable { editorMode = "background" }
                    ) {
                        Text(
                            "🖼 Arrière-Plan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (editorMode == "background") Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Bouton Réinitialiser
                    OutlinedButton(
                        onClick = {
                            if (editorMode == "controls") {
                                positions = LayoutDefaults.defaultPositions.toMutableMap()
                                Toast.makeText(context, "Touches réinitialisées par défaut", Toast.LENGTH_SHORT).show()
                            } else {
                                bgScale = 1.0f
                                bgOffsetX = 0.0f
                                bgOffsetY = 0.0f
                                bgDim = 0.35f
                                Toast.makeText(context, "Arrière-plan réinitialisé", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TGCGold, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Défaut", color = TGCGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Bouton Sauvegarder
                    Button(
                        onClick = {
                            viewModel.updateAllButtonPositions(positions)
                            viewModel.applyBackgroundAdjustment(bgDim, bgScale, bgOffsetX, bgOffsetY)
                            Toast.makeText(context, "Disposition et arrière-plan enregistrés !", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GamepadPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Sauvegarder", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // ── 4. Barre inférieure : Propriétés Touches (Mode Touches) ───────────
        if (editorMode == "controls") {
            selectedKey?.let { key ->
                val pos = positions[key]
                if (pos != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                            .fillMaxWidth(0.72f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.90f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamepadPrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LayoutDefaults.controlLabels[key] ?: key,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamepadPrimary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Taille : ${(pos.size * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Slider(
                                    value = pos.size,
                                    onValueChange = { newScale ->
                                        positions = positions.toMutableMap().apply {
                                            this[key] = pos.copy(size = newScale)
                                        }
                                    },
                                    valueRange = 0.70f..1.40f,
                                    modifier = Modifier.width(110.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = GamepadPrimary,
                                        activeTrackColor = GamepadPrimary
                                    )
                                )
                            }

                            TextButton(
                                onClick = {
                                    val def = LayoutDefaults.defaultPositions[key]
                                    if (def != null) {
                                        positions = positions.toMutableMap().apply {
                                            this[key] = def
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Centrer", color = TGCGold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ── 5. Barre inférieure : Réglages Arrière-Plan (Mode Arrière-plan) ───
        if (editorMode == "background") {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .fillMaxWidth(0.92f),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TGCGold.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Zoom: ${String.format("%.1f", bgScale)}x", fontSize = 10.sp, color = Color.White)
                        Slider(
                            value = bgScale,
                            onValueChange = { bgScale = it },
                            valueRange = 1.0f..5.0f,
                            modifier = Modifier.width(90.dp),
                            colors = SliderDefaults.colors(thumbColor = TGCGold, activeTrackColor = TGCGold)
                        )
                    }

                    // Pan X
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Pan X: ${(bgOffsetX * 100).toInt()}%", fontSize = 10.sp, color = Color.White)
                        Slider(
                            value = bgOffsetX,
                            onValueChange = { bgOffsetX = it },
                            valueRange = -0.8f..0.8f,
                            modifier = Modifier.width(80.dp),
                            colors = SliderDefaults.colors(thumbColor = GamepadPrimary, activeTrackColor = GamepadPrimary)
                        )
                    }

                    // Pan Y
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Pan Y: ${(bgOffsetY * 100).toInt()}%", fontSize = 10.sp, color = Color.White)
                        Slider(
                            value = bgOffsetY,
                            onValueChange = { bgOffsetY = it },
                            valueRange = -0.8f..0.8f,
                            modifier = Modifier.width(80.dp),
                            colors = SliderDefaults.colors(thumbColor = GamepadPrimary, activeTrackColor = GamepadPrimary)
                        )
                    }

                    // Assombrissement (Dim)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Dim: ${(bgDim * 100).toInt()}%", fontSize = 10.sp, color = Color.White)
                        Slider(
                            value = bgDim,
                            onValueChange = { bgDim = it },
                            valueRange = 0.0f..0.85f,
                            modifier = Modifier.width(75.dp),
                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
                        )
                    }

                    // Bouton changer fond
                    Button(
                        onClick = { bgPicker.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = GamepadCard),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("🖼 Changer", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun EditorDraggableControl(
    x: Float,
    y: Float,
    itemWidth: androidx.compose.ui.unit.Dp,
    itemHeight: androidx.compose.ui.unit.Dp,
    screenW: Float,
    screenH: Float,
    enabled: Boolean = true,
    onSelect: () -> Unit,
    onDrag: (dxDp: Float, dyDp: Float) -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnSelect by rememberUpdatedState(onSelect)

    val posX = (screenW * x - itemWidth.value / 2f).dp
    val posY = (screenH * y - itemHeight.value / 2f).dp

    Box(
        modifier = Modifier
            .offset(x = posX, y = posY)
            .size(width = itemWidth, height = itemHeight)
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { currentOnSelect() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dxDp = with(density) { dragAmount.x.toDp().value }
                                val dyDp = with(density) { dragAmount.y.toDp().value }
                                currentOnDrag(dxDp, dyDp)
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}
