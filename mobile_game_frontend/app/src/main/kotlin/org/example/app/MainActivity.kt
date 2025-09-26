package org.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.example.app.ui.theme.OceanTheme

/**
 * MainActivity migrated to ComponentActivity with Jetpack Compose.
 * Hosts the NavGraph and a top-level Scaffold with overlay menus for pause, settings, and language.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OceanTheme {
                AppRoot()
            }
        }
    }
}

// PUBLIC_INTERFACE
@Composable
fun AppRoot() {
    /** Root composable with navigation and overlay controls using stable Material3 containers. */
    RootContent()
}

@Composable
private fun RootContent() {
    val navController = rememberNavController()

    var showPause by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showLanguage by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = stringResource(id = R.string.app_name),
                onLanguage = { showLanguage = true },
                onPause = { showPause = true },
                onSettings = { showSettings = true }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Directly host Nav without Column to avoid inline layout
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                GameNavHost(
                    navController = navController,
                    onRequestPause = { showPause = true },
                    onRequestSettings = { showSettings = true },
                    onRequestLanguage = { showLanguage = true }
                )
            }
        }

        if (showPause) PauseOverlay(onDismiss = { showPause = false })
        if (showSettings) SettingsOverlay(onDismiss = { showSettings = false })
        if (showLanguage) LanguageOverlay(onDismiss = { showLanguage = false })
    }
}

@Composable
private fun SimpleTopBar(
    title: String,
    onLanguage: () -> Unit,
    onPause: () -> Unit,
    onSettings: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        // Title
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = Color.Transparent
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        // Actions bar as separate Surface to avoid Row
        Surface(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 0.dp),
            color = Color.Transparent
        ) {
            IconButton(onClick = onLanguage) {
                Icon(
                    Icons.Outlined.Translate,
                    contentDescription = "Language",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onPause) {
                Icon(
                    Icons.Outlined.Pause,
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// PUBLIC_INTERFACE
@Composable
fun GameNavHost(
    navController: NavHostController,
    onRequestPause: () -> Unit,
    onRequestSettings: () -> Unit,
    onRequestLanguage: () -> Unit
) {
    /** Navigation host for MainMenu, Game, Achievements, and Settings routes. */
    NavHost(navController = navController, startDestination = Routes.MainMenu.route) {
        composable(Routes.MainMenu.route) {
            MainMenuScreen(
                onStartGame = { navController.navigate(Routes.Game.route) },
                onAchievements = { navController.navigate(Routes.Achievements.route) },
                onSettings = { navController.navigate(Routes.Settings.route) }
            )
        }
        composable(Routes.Game.route) {
            GameScreen(
                onPause = onRequestPause,
                onSettings = onRequestSettings,
                onLanguage = onRequestLanguage
            )
        }
        composable(Routes.Achievements.route) {
            AchievementsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private sealed class Routes(val route: String) {
    data object MainMenu : Routes("main_menu")
    data object Game : Routes("game")
    data object Achievements : Routes("achievements")
    data object Settings : Routes("settings")
}

@Composable
private fun SimpleOverlayContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    // Fullscreen scrim clickable layer
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(onClick = onDismiss),
        color = Color.Transparent
    ) {
        // Card container; stack content via sequential elements (no Column)
        Surface(
            modifier = Modifier.padding(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        ) {
            Surface(modifier = Modifier.padding(20.dp), color = Color.Transparent) {
                content()
            }
        }
    }
}

@Composable
private fun PauseOverlay(onDismiss: () -> Unit) {
    SimpleOverlayContainer(onDismiss = onDismiss) {
        Icon(
            Icons.Outlined.Pause,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Paused",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text("Resume") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDismiss) { Text("Restart Level") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDismiss) { Text("Quit to Menu") }
    }
}

@Composable
private fun SettingsOverlay(onDismiss: () -> Unit) {
    SimpleOverlayContainer(onDismiss = onDismiss) {
        Icon(
            Icons.Outlined.Settings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Settings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(16.dp))
        Text("Sound")
        Divider()
        Text("Music")
        Divider()
        Text("Haptics")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text("Close") }
    }
}

@Composable
private fun LanguageOverlay(onDismiss: () -> Unit) {
    SimpleOverlayContainer(onDismiss = onDismiss) {
        Icon(
            Icons.Outlined.Translate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Language",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text("English") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDismiss) { Text("Español") }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text("Done") }
    }
}

// --- Placeholder Screens ---

@Composable
private fun MainMenuScreen(
    onStartGame: () -> Unit,
    onAchievements: () -> Unit,
    onSettings: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        // Vertical stack simulated with simple surfaces and spacers
        Surface(color = Color.Transparent) {
            Text(
                text = "Puzzle Quest",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
        Spacer(Modifier.height(12.dp))
        Surface(color = Color.Transparent) { Button(onClick = onStartGame) { Text("Start Game") } }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) { OutlinedButton(onClick = onAchievements) { Text("Achievements") } }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) { OutlinedButton(onClick = onSettings) { Text("Settings") } }
    }
}

@Composable
private fun GameScreen(
    onPause: () -> Unit,
    onSettings: () -> Unit,
    onLanguage: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Surface(color = Color.Transparent) {
            Text(
                text = "Game (Match-3 grid placeholder)",
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(Modifier.height(12.dp))
        Surface(color = Color.Transparent) { Button(onClick = onPause) { Text("Pause") } }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) { OutlinedButton(onClick = onSettings) { Text("Settings") } }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) { OutlinedButton(onClick = onLanguage) { Text("Language") } }
    }
}

@Composable
private fun AchievementsScreen(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Surface(color = Color.Transparent) {
            Text("Achievements", style = MaterialTheme.typography.headlineLarge)
        }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) { OutlinedButton(onClick = onBack) { Text("Back") } }
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Surface(color = Color.Transparent) { Text("Settings", style = MaterialTheme.typography.headlineLarge) }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) { OutlinedButton(onClick = onBack) { Text("Back") } }
    }
}
