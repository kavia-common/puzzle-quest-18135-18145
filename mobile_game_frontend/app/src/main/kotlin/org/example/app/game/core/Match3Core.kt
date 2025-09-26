package org.example.app.game.core

import kotlin.random.Random

/**
 * Core match-3 logic and data models. This file contains:
 * - TileType: enum of basic tile colors/types
 * - Cell: data class for board cells with row/col and tile type
 * - Grid: board container with helpers to read/write and bounds checks
 * - MatchFinder: logic to find all matches of length >= 3
 * - SwapValidator: validates swaps that result in a match
 * - CascadeEngine: collapse matched cells and cascade tiles downward (gravity)
 * - RefillEngine: refill empty cells with new random tiles without introducing initial matches
 * - ScoreEngine: basic scoring rules derived from match lengths and cascades
 * - LevelConfig: configuration for a level (rows, cols, allowed types, target score/moves)
 * - GameState: immutable snapshot of a running game for UI consumption
 *
 * PUBLIC INTERFACES are annotated with "PUBLIC_INTERFACE" and provide documentation comments.
 */

// PUBLIC_INTERFACE
enum class TileType(val id: Int) {
    RED(0), GREEN(1), BLUE(2), YELLOW(3), PURPLE(4), ORANGE(5);

    companion object {
        fun defaultSet(): List<TileType> = listOf(RED, GREEN, BLUE, YELLOW, PURPLE)
        fun fromId(id: Int): TileType = entries.firstOrNull { it.id == id } ?: RED
    }
}

data class Cell(val row: Int, val col: Int, val type: TileType)

class Grid(
    val rows: Int,
    val cols: Int,
    private val tiles: Array<TileType?>
) {

    companion object {
        fun empty(rows: Int, cols: Int): Grid {
            return Grid(rows, cols, Array(rows * cols) { null })
        }

        fun fromTypes(rows: Int, cols: Int, types: List<TileType?>): Grid {
            require(types.size == rows * cols) { "types size must be rows*cols" }
            return Grid(rows, cols, Array(rows * cols) { idx -> types[idx] })
        }
    }

    fun copy(): Grid = Grid(rows, cols, tiles.copyOf())

    fun inBounds(r: Int, c: Int): Boolean = r in 0 until rows && c in 0 until cols

    fun index(r: Int, c: Int): Int = r * cols + c

    fun get(r: Int, c: Int): TileType? = if (inBounds(r, c)) tiles[index(r, c)] else null

    fun set(r: Int, c: Int, type: TileType?) {
        if (inBounds(r, c)) tiles[index(r, c)] = type
    }

    fun swap(r1: Int, c1: Int, r2: Int, c2: Int) {
        val i1 = index(r1, c1)
        val i2 = index(r2, c2)
        val tmp = tiles[i1]
        tiles[i1] = tiles[i2]
        tiles[i2] = tmp
    }

    fun toCells(): List<Cell> {
        val out = ArrayList<Cell>(rows * cols)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val t = get(r, c)
                if (t != null) out.add(Cell(r, c, t))
            }
        }
        return out
    }

    fun isEmpty(r: Int, c: Int): Boolean = get(r, c) == null
}

/**
 * Finds all matches (horizontal and vertical) of length >= 3.
 * Returns a set of positions (row to mutable set of cols per row) or flattened list of pairs.
 */
object MatchFinder {
    data class Match(val cells: List<Pair<Int, Int>>)

    // PUBLIC_INTERFACE
    fun findAll(grid: Grid): List<Match> {
        /** Returns all horizontal and vertical matches (length >= 3). */
        val matches = mutableListOf<Match>()

        // Horizontal scan
        for (r in 0 until grid.rows) {
            var c = 0
            while (c < grid.cols) {
                val start = c
                val base = grid.get(r, c)
                if (base == null) {
                    c++
                    continue
                }
                var len = 1
                var k = c + 1
                while (k < grid.cols && grid.get(r, k) == base) {
                    len++; k++
                }
                if (len >= 3) {
                    val cells = (start until start + len).map { cc -> r to cc }
                    matches.add(Match(cells))
                }
                c = start + len
            }
        }

        // Vertical scan
        for (c in 0 until grid.cols) {
            var r = 0
            while (r < grid.rows) {
                val start = r
                val base = grid.get(r, c)
                if (base == null) {
                    r++
                    continue
                }
                var len = 1
                var k = r + 1
                while (k < grid.rows && grid.get(k, c) == base) {
                    len++; k++
                }
                if (len >= 3) {
                    val cells = (start until start + len).map { rr -> rr to c }
                    matches.add(Match(cells))
                }
                r = start + len
            }
        }

        // Merge overlaps into unique groups
        return mergeOverlapping(matches)
    }

    private fun mergeOverlapping(matches: List<Match>): List<Match> {
        if (matches.isEmpty()) return emptyList()
        val parent = IntArray(matches.size) { it }
        fun find(x: Int): Int {
            var i = x
            while (i != parent[i]) i = parent[i]
            var j = x
            while (j != parent[j]) {
                val pj = parent[j]
                parent[j] = i
                j = pj
            }
            return i
        }
        fun union(a: Int, b: Int) {
            val ra = find(a); val rb = find(b)
            if (ra != rb) parent[rb] = ra
        }
        // overlap check
        val sets = matches.map { it.cells.toSet() }
        for (i in matches.indices) {
            for (j in i + 1 until matches.size) {
                if (sets[i].any { it in sets[j] }) {
                    union(i, j)
                }
            }
        }
        val groups = mutableMapOf<Int, MutableSet<Pair<Int, Int>>>()
        for (i in matches.indices) {
            val r = find(i)
            val g = groups.getOrPut(r) { mutableSetOf() }
            g.addAll(matches[i].cells)
        }
        return groups.values.map { Match(it.toList()) }
    }
}

/**
 * Validates swaps: only allow orthogonally-adjacent swaps that create at least one match.
 */
object SwapValidator {
    // PUBLIC_INTERFACE
    fun isValidSwap(grid: Grid, r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        /** Returns true if swapping the two adjacent cells would result in any match. */
        if (!grid.inBounds(r1, c1) || !grid.inBounds(r2, c2)) return false
        val dr = kotlin.math.abs(r1 - r2)
        val dc = kotlin.math.abs(c1 - c2)
        if (dr + dc != 1) return false // must be adjacent orthogonally
        if (grid.get(r1, c1) == null || grid.get(r2, c2) == null) return false

        val tmp = grid.copy()
        tmp.swap(r1, c1, r2, c2)
        val matches = MatchFinder.findAll(tmp)
        return matches.isNotEmpty()
    }
}

/**
 * Applies gravity after clearing matches: cells above fall down into empty spaces.
 */
object CascadeEngine {
    // PUBLIC_INTERFACE
    fun applyGravity(grid: Grid): Grid {
        /** Collapses tiles downward to fill null gaps in each column. */
        val out = grid.copy()
        for (c in 0 until out.cols) {
            var write = out.rows - 1
            for (r in out.rows - 1 downTo 0) {
                val t = out.get(r, c)
                if (t != null) {
                    if (write != r) {
                        out.set(write, c, t)
                        out.set(r, c, null)
                    }
                    write--
                }
            }
        }
        return out
    }
}

/**
 * Refills empty grid cells with random tiles from the allowed set. Attempts to avoid creating
 * immediate matches on refill where possible via simple retry attempts.
 */
object RefillEngine {
    // PUBLIC_INTERFACE
    fun refillRandom(grid: Grid, allowed: List<TileType>, rng: Random = Random.Default): Grid {
        /** Fills null cells with new random tiles from allowed set, avoiding obvious new triples. */
        val out = grid.copy()
        for (r in 0 until out.rows) {
            for (c in 0 until out.cols) {
                if (out.isEmpty(r, c)) {
                    out.set(r, c, pickSafeType(out, r, c, allowed, rng))
                }
            }
        }
        return out
    }

    private fun pickSafeType(grid: Grid, r: Int, c: Int, allowed: List<TileType>, rng: Random): TileType {
        // try up to N attempts to avoid immediate 3-in-a-row creation
        repeat(6) {
            val t = allowed[rng.nextInt(allowed.size)]
            if (!wouldMakeImmediateMatch(grid, r, c, t)) return t
        }
        // fallback
        return allowed[rng.nextInt(allowed.size)]
    }

    private fun wouldMakeImmediateMatch(grid: Grid, r: Int, c: Int, t: TileType): Boolean {
        // Check horizontal: .. X X [t] or X [t] X backward
        val left1 = grid.get(r, c - 1)
        val left2 = grid.get(r, c - 2)
        val right1 = grid.get(r, c + 1)
        val right2 = grid.get(r, c + 2)
        if ((left1 == t && left2 == t) || (left1 == t && right1 == t) || (right1 == t && right2 == t)) return true

        // Check vertical
        val up1 = grid.get(r - 1, c)
        val up2 = grid.get(r - 2, c)
        val down1 = grid.get(r + 1, c)
        val down2 = grid.get(r + 2, c)
        if ((up1 == t && up2 == t) || (up1 == t && down1 == t) || (down1 == t && down2 == t)) return true

        return false
    }
}

/**
 * Basic scoring logic:
 * - Base points: 3-match = 60, then +30 per extra tile (4->90, 5->120, etc.)
 * - Cascade multiplier: each subsequent cascade in a chain multiplies awarded points by chainIndex (1-based)
 */
object ScoreEngine {
    // PUBLIC_INTERFACE
    fun scoreForMatch(matchSize: Int, chainIndex: Int): Int {
        /** Returns points for a single match given its size and the chain index (1 = initial clear). */
        if (matchSize < 3) return 0
        val base = 60
        val extraPerTile = 30
        val raw = base + (matchSize - 3) * extraPerTile
        return raw * chainIndex.coerceAtLeast(1)
    }
}

/**
 * Level configuration model.
 */
// PUBLIC_INTERFACE
data class LevelConfig(
    /** Rows and columns for the grid. */
    val rows: Int = 8,
    val cols: Int = 8,
    /** Allowed tile types for this level. */
    val allowedTypes: List<TileType> = TileType.defaultSet(),
    /** Target score to win (alternative win conditions can be added later). */
    val targetScore: Int = 1500,
    /** Move limit before level ends. */
    val moveLimit: Int = 25
)

/**
 * Immutable game state snapshot.
 */
// PUBLIC_INTERFACE
data class GameState(
    /** Grid representation with possible null cells (during cascades/refill). */
    val grid: Grid,
    /** Current score. */
    val score: Int,
    /** Remaining moves. */
    val movesLeft: Int,
    /** Current chain index if a cascade is in progress (1 = first clear). */
    val chainIndex: Int = 0,
    /** True when level goal reached. */
    val isWin: Boolean = false,
    /** True when no moves left (and not win). */
    val isLose: Boolean = false
)

/**
 * Helpers to initialize a new board without pre-existing matches.
 */
// PUBLIC_INTERFACE
object BoardFactory {
    /** Creates a new randomized board avoiding initial matches where possible. */
    fun createInitialGrid(cfg: LevelConfig, rng: Random = Random.Default): Grid {
        var grid = Grid.empty(cfg.rows, cfg.cols)
        grid = RefillEngine.refillRandom(grid, cfg.allowedTypes, rng)
        // ensure no initial matches; if found, reshuffle by refilling those cells
        var attempts = 0
        while (MatchFinder.findAll(grid).isNotEmpty() && attempts < 10) {
            // clear matched cells and refill again
            val tmp = grid.copy()
            MatchFinder.findAll(tmp).flatMap { it.cells }.forEach { (r, c) ->
                tmp.set(r, c, null)
            }
            grid = RefillEngine.refillRandom(tmp, cfg.allowedTypes, rng)
            attempts++
        }
        return grid
    }
}

/**
 * Core turn resolution:
 * - Validate swap
 * - Perform swap
 * - Find matches; if none, swap back and return state unchanged (except maybe moves not consumed)
 * - If matches exist:
 *     - Remove matches, score, cascade, refill, and keep clearing until no more matches (cascade chain)
 *     - Decrement moves
 *     - Compute win/lose flags
 */
// PUBLIC_INTERFACE
object TurnEngine {
    /** Performs one player swap and resolves cascades. Returns the new GameState. */
    fun performSwap(
        state: GameState,
        cfg: LevelConfig,
        r1: Int,
        c1: Int,
        r2: Int,
        c2: Int,
        rng: Random = Random.Default
    ): GameState {
        if (state.isWin || state.isLose) return state
        if (!SwapValidator.isValidSwap(state.grid, r1, c1, r2, c2)) {
            // invalid swap, do not consume a move
            return state
        }
        var grid = state.grid.copy().apply { swap(r1, c1, r2, c2) }
        var score = state.score
        var chain = 0

        while (true) {
            val matches = MatchFinder.findAll(grid)
            if (matches.isEmpty()) break

            chain += 1
            // clear matches
            val toClear = matches.flatMap { it.cells }.toSet()
            toClear.forEach { (r, c) -> grid.set(r, c, null) }

            // scoring
            matches.forEach { m ->
                score += ScoreEngine.scoreForMatch(m.cells.size, chain)
            }

            // cascade and refill
            grid = CascadeEngine.applyGravity(grid)
            grid = RefillEngine.refillRandom(grid, cfg.allowedTypes, rng)
        }

        val newMoves = (state.movesLeft - 1).coerceAtLeast(0)
        val isWin = score >= cfg.targetScore
        val isLose = !isWin && newMoves <= 0

        return state.copy(
            grid = grid,
            score = score,
            movesLeft = newMoves,
            chainIndex = chain,
            isWin = isWin,
            isLose = isLose
        )
    }
}
