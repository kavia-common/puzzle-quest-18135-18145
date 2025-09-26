# Persistence Layer Overview

This app uses:
- Room for offline-first storage of level progress, achievements, and rewards
- DataStore for app settings (language is handled in `i18n/LocaleManager`; sound/music/haptics in `settings/AppSettings.kt`)

Key files:
- data/db/entities.kt — Room entities
- data/db/dao.kt — DAOs with suspend functions and Flows
- data/db/GameDatabase.kt — Singleton Room database
- data/repository/Repositories.kt — Repositories exposing suspend/Flow APIs
- data/ServiceLocator.kt — Simple accessors to obtain repositories
- settings/AppSettings.kt — DataStore settings APIs (suspend/Flow)

Usage example (inside an Activity or ViewModel):
```kotlin
val levelRepo = ServiceLocator.levelProgressRepository(context)
val achievementRepo = ServiceLocator.achievementRepository(context)
val rewardRepo = ServiceLocator.rewardRepository(context)

// Write:
lifecycleScope.launch {
    levelRepo.upsertProgress(level = 1, stars = 3, score = 12000, completed = true, timeMillis = System.currentTimeMillis())
    rewardRepo.grant(type = "coins", amount = 50, now = System.currentTimeMillis())
    achievementRepo.unlockOrUpdate(code = "first_clear", now = System.currentTimeMillis())
}

// Read as Flow (Compose):
val progress by levelRepo.observeAll().collectAsState(initial = emptyList())
```

Settings (DataStore):
```kotlin
AppSettings.observeSoundEnabled(context).collect { enabled -> /* use */ }
AppSettings.setHapticEnabled(context, true)
```

All APIs work fully offline.
