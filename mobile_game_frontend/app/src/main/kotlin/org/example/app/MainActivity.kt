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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.example.app.i18n.LocaleManager
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
                AppRoot(activity = this@MainActivity)
            }
        }
    }
}

/** PUBLIC_INTERFACE */
@Composable
fun AppRoot(activity: ComponentActivity) {
    /**
     * Root composable with navigation, language observation and overlay controls.
     * Observes DataStore for language changes and applies locale to the running activity.
     * To avoid inline/IR issues, we do not use rememberCoroutineScope in nested lambdas.
     */
    val langFlow = LocaleManager.observeLanguage(activity)
    val language by langFlow.collectAsState(initial = "en")

    // Apply locale whenever language changes (including on first composition)
    LaunchedEffect(language) {
        LocaleManager.applyLocale(activity, language)
    }

    // Local state to trigger persistence without capturing composable scope in inline functions
    var pendingPersistLang by rememberSaveable { mutableStateOf<String?>(null) }

    // Persist language when requested via pendingPersistLang
    LaunchedEffect(pendingPersistLang) {
        val code = pendingPersistLang
        if (code != null) {
            LocaleManager.setLanguage(activity, code)
            pendingPersistLang = null
            // observeLanguage flow will emit and re-apply locale
        }
    }

    RootContent(
        language = language,
        onSelectLanguage = { newLang ->
            pendingPersistLang = newLang
        }
    )
}

@Composable
private fun RootContent(
    language: String,
    onSelectLanguage: (String) -> Unit
) {
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
        if (showLanguage) {
            LanguageOverlay(
                language = language,
                onChangeLanguage = { code ->
                    onSelectLanguage(code)
                },
                onDismiss = { showLanguage = false }
            )
        }
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
        // Actions bar (icons)
        Surface(
            modifier = Modifier.padding(horizontal = 12.dp),
            color = Color.Transparent
        ) {
            IconButton(onClick = onLanguage) {
                Icon(
                    Icons.Outlined.Translate,
                    contentDescription = stringResource(id = R.string.menu_language),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onPause) {
                Icon(
                    Icons.Outlined.Pause,
                    contentDescription = stringResource(id = R.string.menu_pause),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = stringResource(id = R.string.menu_settings),
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
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(onClick = onDismiss),
        color = Color.Transparent
    ) {
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
            stringResource(id = R.string.title_paused),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text(stringResource(id = R.string.resume)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDismiss) { Text(stringResource(id = R.string.restart_level)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDismiss) { Text(stringResource(id = R.string.quit_to_menu)) }
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
            stringResource(id = R.string.title_settings),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(16.dp))
        Text(stringResource(id = R.string.settings_sound))
        Divider()
        Text(stringResource(id = R.string.settings_music))
        Divider()
        Text(stringResource(id = R.string.settings_haptics))
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text(stringResource(id = R.string.common_close)) }
    }
}

@Composable
private fun LanguageOverlay(
    language: String,
    onChangeLanguage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    SimpleOverlayContainer(onDismiss = onDismiss) {
        Icon(
            Icons.Outlined.Translate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(id = R.string.title_language),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onChangeLanguage("en") }) { Text(stringResource(id = R.string.lang_english)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { onChangeLanguage("es") }) { Text(stringResource(id = R.string.lang_spanish)) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text(stringResource(id = R.string.common_done)) }
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
        Surface(color = Color.Transparent) {
            Text(
                text = stringResource(id = R.string.title_main),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
        Spacer(Modifier.height(12.dp))
        Surface(color = Color.Transparent) {
            Button(onClick = onStartGame) {
                Text(stringResource(id = R.string.action_start_game))
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) {
            OutlinedButton(onClick = onAchievements) {
                Text(stringResource(id = R.string.action_achievements))
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) {
            OutlinedButton(onClick = onSettings) {
                Text(stringResource(id = R.string.menu_settings))
            }
        }
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
                text = stringResource(id = R.string.title_game_placeholder),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(Modifier.height(12.dp))
        Surface(color = Color.Transparent) {
            Button(onClick = onPause) {
                Text(stringResource(id = R.string.action_pause))
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) {
            OutlinedButton(onClick = onSettings) {
                Text(stringResource(id = R.string.menu_settings))
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) {
            OutlinedButton(onClick = onLanguage) {
                Text(stringResource(id = R.string.menu_language))
            }
        }
    }
}

@Composable
private fun AchievementsScreen(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Surface(color = Color.Transparent) {
            Text(stringResource(id = R.string.title_achievements), style = MaterialTheme.typography.headlineLarge)
        }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) {
            OutlinedButton(onClick = onBack) {
                Text(stringResource(id = R.string.common_back))
            }
        }
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Surface(color = Color.Transparent) {
            Text(stringResource(id = R.string.title_settings), style = MaterialTheme.typography.headlineLarge)
        }
        Spacer(Modifier.height(8.dp))
        Surface(color = Color.Transparent) {
            OutlinedButton(onClick = onBack) {
                Text(stringResource(id = R.string.common_back))
            }
        }
    }
}
