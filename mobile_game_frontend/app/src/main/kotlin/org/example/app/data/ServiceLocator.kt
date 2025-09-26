package org.example.app.data

import android.content.Context
import org.example.app.data.repository.AchievementRepository
import org.example.app.data.repository.LevelProgressRepository
import org.example.app.data.repository.RewardRepository

/**
 * Simple service locator to access repositories without a DI framework.
 * This keeps initialization centralized and avoids leaking database instances.
 */
object ServiceLocator {
    // PUBLIC_INTERFACE
    fun levelProgressRepository(context: Context): LevelProgressRepository =
        LevelProgressRepository.get(context)

    // PUBLIC_INTERFACE
    fun achievementRepository(context: Context): AchievementRepository =
        AchievementRepository.get(context)

    // PUBLIC_INTERFACE
    fun rewardRepository(context: Context): RewardRepository =
        RewardRepository.get(context)
}
