package org.example.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress WHERE level = :level LIMIT 1")
    suspend fun getByLevel(level: Int): LevelProgressEntity?

    @Query("SELECT * FROM level_progress ORDER BY level ASC")
    fun observeAll(): Flow<List<LevelProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LevelProgressEntity): Long

    @Update
    suspend fun update(entity: LevelProgressEntity)

    @Query("DELETE FROM level_progress")
    suspend fun clearAll()

    @Transaction
    suspend fun upsertProgress(level: Int, stars: Int, score: Long, completed: Boolean, time: Long) {
        val current = getByLevel(level)
        if (current == null) {
            upsert(
                LevelProgressEntity(
                    level = level,
                    stars = stars,
                    score = score,
                    completed = completed,
                    lastPlayedAt = time
                )
            )
        } else {
            val bestStars = maxOf(current.stars, stars)
            val bestScore = maxOf(current.score, score)
            val isCompleted = current.completed || completed
            update(
                current.copy(
                    stars = bestStars,
                    score = bestScore,
                    completed = isCompleted,
                    lastPlayedAt = time
                )
            )
        }
    }
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AchievementEntity): Long

    @Query("DELETE FROM achievements")
    suspend fun clearAll()
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM rewards ORDER BY grantedAt DESC")
    fun observeAll(): Flow<List<RewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RewardEntity): Long

    @Query("DELETE FROM rewards")
    suspend fun clearAll()
}
