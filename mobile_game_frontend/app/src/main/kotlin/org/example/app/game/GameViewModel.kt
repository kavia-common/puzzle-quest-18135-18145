package org.example.app.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.app.game.core.BoardFactory
import org.example.app.game.core.GameState
import org.example.app.game.core.LevelConfig
import org.example.app.game.core.TurnEngine

/**
 * GameViewModel orchestrates level configuration, current game state, and exposes simple actions:
 * - newGame(levelConfig)
 * - trySwap(r1, c1, r2, c2)
 *
 * It is UI-agnostic and can back any Compose screen.
 */
class GameViewModel : ViewModel() {

    private var _levelConfig: LevelConfig = LevelConfig()
    private val _state: MutableStateFlow<GameState> = MutableStateFlow(
        GameState(
            grid = BoardFactory.createInitialGrid(LevelConfig()),
            score = 0,
            movesLeft = LevelConfig().moveLimit,
            chainIndex = 0,
            isWin = false,
            isLose = false
        )
    )

    // PUBLIC_INTERFACE
    fun uiState(): StateFlow<GameState> {
        /** Returns a StateFlow emitting the latest GameState for UI consumption. */
        return _state
    }

    // PUBLIC_INTERFACE
    fun newGame(config: LevelConfig = LevelConfig()) {
        /** Starts a new game with the given level config. */
        _levelConfig = config
        val grid = BoardFactory.createInitialGrid(config)
        _state.value = GameState(
            grid = grid,
            score = 0,
            movesLeft = config.moveLimit,
            chainIndex = 0,
            isWin = false,
            isLose = false
        )
    }

    // PUBLIC_INTERFACE
    fun trySwap(r1: Int, c1: Int, r2: Int, c2: Int) {
        /**
         * Attempts to perform a swap action. If invalid, state is unchanged.
         * If valid, resolves cascades and updates score/moves/win flags.
         */
        viewModelScope.launch {
            val current = _state.value
            val next = TurnEngine.performSwap(current, _levelConfig, r1, c1, r2, c2)
            _state.value = next
        }
    }
}
