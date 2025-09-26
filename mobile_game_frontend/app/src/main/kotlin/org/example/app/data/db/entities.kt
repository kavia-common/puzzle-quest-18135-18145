package org.example.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entities used by the local Room database for offline-first persistence.
 * They are minimal, versionable records for progress, achievements, and rewards.
 */

@Entity(
    tableName = "level_progress",
    indices = [Index(value = ["level"], unique = true)]
)
data class LevelProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val level: Int,
    val stars: Int,
    val score: Long,
    val completed: Boolean,
    val lastPlayedAt: Long // epoch millis
)

@Entity(
    tableName = "achievements",
    indices = [Index(value = ["code"], unique = true)]
)
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,                  // e.g., "first_clear", "combo_master"
    val unlockedAt: Long,              // epoch millis
    val progress: Int = 1,             // for incremental achievements
    val maxProgress: Int = 1
)

@Entity(
    tableName = "rewards"
)
data class RewardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,                  // e.g., "coins", "booster_bomb"
    val amount: Int,
    val grantedAt: Long                // epoch millis
)
