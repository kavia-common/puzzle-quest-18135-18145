package org.example.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for the game.
 * Includes LevelProgressEntity, AchievementEntity, and RewardEntity.
 */
@Database(
    entities = [
        LevelProgressEntity::class,
        AchievementEntity::class,
        RewardEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun achievementDao(): AchievementDao
    abstract fun rewardDao(): RewardDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        // PUBLIC_INTERFACE
        fun getInstance(context: Context): GameDatabase {
            /** Returns the singleton Room database instance. */
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game.db"
                )
                    .fallbackToDestructiveMigration() // safe during development; replace with proper migrations later
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
