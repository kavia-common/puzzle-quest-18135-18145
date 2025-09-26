package org.example.app.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import org.example.app.data.db.AchievementDao
import org.example.app.data.db.AchievementEntity
import org.example.app.data.db.GameDatabase
import org.example.app.data.db.LevelProgressDao
import org.example.app.data.db.LevelProgressEntity
import org.example.app.data.db.RewardDao
import org.example.app.data.db.RewardEntity

/**
 * Repository layer to abstract Room DAOs and provide suspend/Flow APIs to UI and domain.
 * These repositories are offline-first; there is no remote source in this project scope.
 */

// PUBLIC_INTERFACE
class LevelProgressRepository private constructor(
    private val dao: LevelProgressDao
) {
    /** Observe all level progress for menu and progression UI. */
    fun observeAll(): Flow<List<LevelProgressEntity>> = dao.observeAll()

    /** Update or create level progress; merges best score/stars and completion flag. */
    suspend fun upsertProgress(level: Int, stars: Int, score: Long, completed: Boolean, timeMillis: Long) {
        dao.upsertProgress(level, stars, score, completed, timeMillis)
    }

    /** Fetch a single level's progress. */
    suspend fun getByLevel(level: Int): LevelProgressEntity? = dao.getByLevel(level)

    companion object {
        // PUBLIC_INTERFACE
        fun get(context: Context): LevelProgressRepository {
            /** Obtain a repository instance backed by the Room database singleton. */
            val db = GameDatabase.getInstance(context)
            return LevelProgressRepository(db.levelProgressDao())
        }
    }
}

// PUBLIC_INTERFACE
class AchievementRepository private constructor(
    private val dao: AchievementDao
) {
    /** Observe unlocked achievements. */
    fun observeAll(): Flow<List<AchievementEntity>> = dao.observeAll()

    /** Unlock or update an achievement progress. */
    suspend fun unlockOrUpdate(code: String, now: Long, progress: Int = 1, maxProgress: Int = 1) {
        val current = dao.getByCode(code)
        val entity = if (current == null) {
            AchievementEntity(code = code, unlockedAt = now, progress = progress, maxProgress = maxProgress)
        } else {
            current.copy(
                unlockedAt = if (progress >= maxProgress && current.unlockedAt == 0L) now else current.unlockedAt,
                progress = maxOf(current.progress, progress),
                maxProgress = maxOf(current.maxProgress, maxProgress)
            )
        }
        dao.upsert(entity)
    }

    companion object {
        // PUBLIC_INTERFACE
        fun get(context: Context): AchievementRepository {
            /** Obtain a repository instance backed by the Room database singleton. */
            val db = GameDatabase.getInstance(context)
            return AchievementRepository(db.achievementDao())
        }
    }
}

// PUBLIC_INTERFACE
class RewardRepository private constructor(
    private val dao: RewardDao
) {
    /** Observe granted rewards history. */
    fun observeAll(): Flow<List<RewardEntity>> = dao.observeAll()

    /** Grant a reward and persist it. */
    suspend fun grant(type: String, amount: Int, now: Long) {
        dao.insert(RewardEntity(type = type, amount = amount, grantedAt = now))
    }

    companion object {
        // PUBLIC_INTERFACE
        fun get(context: Context): RewardRepository {
            /** Obtain a repository instance backed by the Room database singleton. */
            val db = GameDatabase.getInstance(context)
            return RewardRepository(db.rewardDao())
        }
    }
}
