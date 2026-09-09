package com.manette.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.manette.ui.screens.ConfigStudioScreen
import com.manette.ui.screens.GameScreen
import com.manette.ui.screens.HomeScreen
import com.manette.ui.screens.LayoutEditorScreen
import com.manette.viewmodel.GameViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ConfigStudio : Screen("config_studio")
    object LayoutEditor : Screen("layout_editor")
    object Game : Screen("game")
}

@Composable
fun ManetteNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: GameViewModel = viewModel()
) {
    val operationMode by viewModel.operationMode.collectAsState()
    val isAutoDiscovered by viewModel.isAutoDiscovered.collectAsState()
    val serverIp by viewModel.serverIp.collectAsState()
    val backgroundUri by viewModel.backgroundUri.collectAsState()
    val backgroundDim by viewModel.backgroundDim.collectAsState()
    val backgroundScale by viewModel.backgroundScale.collectAsState()
    val backgroundOffsetX by viewModel.backgroundOffsetX.collectAsState()
    val backgroundOffsetY by viewModel.backgroundOffsetY.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // ÉCRAN 1 : ACCUEIL & BRANDING THE GREAT CORPORATION
        composable(Screen.Home.route) {
            HomeScreen(
                selectedMode = operationMode,
                isAutoDiscovered = isAutoDiscovered,
                discoveredIp = serverIp,
                backgroundUri = backgroundUri,
                backgroundDim = backgroundDim,
                backgroundScale = backgroundScale,
                backgroundOffsetX = backgroundOffsetX,
                backgroundOffsetY = backgroundOffsetY,
                onModeSelect = { mode ->
                    viewModel.setOperationMode(mode)
                },
                onStartGame = {
                    navController.navigate(Screen.Game.route)
                },
                onOpenConfig = {
                    navController.navigate(Screen.ConfigStudio.route)
                }
            )
        }

        // ÉCRAN 2 : STUDIO DE CONFIGURATION & CUSTOMISATION (FONDS, GIFS, SKINS)
        composable(Screen.ConfigStudio.route) {
            ConfigStudioScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onOpenLayoutEditor = {
                    navController.navigate(Screen.LayoutEditor.route)
                },
                onStartGame = {
                    navController.navigate(Screen.Game.route)
                }
            )
        }

        // ÉCRAN 4 : ÉDITEUR DE DISPOSITION DES TOUCHES
        composable(Screen.LayoutEditor.route) {
            LayoutEditorScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ÉCRAN 3 : MANETTE PLEIN ÉCRAN + ONGLET DISCRET QUICK SETTINGS
        composable(Screen.Game.route) {
            GameScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onOpenFullConfig = {
                    navController.navigate(Screen.ConfigStudio.route)
                }
            )
        }
    }
}
