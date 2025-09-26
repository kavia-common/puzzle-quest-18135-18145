package org.example.app.game

/**
 * Simple provider for a singleton GameViewModel instance to avoid using the
 * androidx.lifecycle.viewmodel.compose.viewModel() inline helper which triggers
 * a Kotlin IR inlining error in this CI environment.
 *
 * This keeps things simple until full DI is introduced.
 */
object GameViewModelProvider {
    // PUBLIC_INTERFACE
    fun get(): GameViewModel {
        /** Returns a singleton GameViewModel for the running process. */
        return holder
    }

    // Keep as lazy to initialize on first use
    private val holder: GameViewModel by lazy { GameViewModel() }
}
