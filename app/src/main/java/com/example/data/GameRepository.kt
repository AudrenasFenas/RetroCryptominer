package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    fun getGameStateFlow(userId: String): Flow<GameStateEntity?> = gameDao.getGameStateFlow(userId)
    val leaderboardFlow: Flow<List<LeaderboardEntity>> = gameDao.getLeaderboardFlow()

    suspend fun getGameState(userId: String): GameStateEntity? = gameDao.getGameState(userId)

    suspend fun saveGameState(state: GameStateEntity) {
        gameDao.insertOrUpdateGameState(state)
    }

    suspend fun getPlayerLeaderboardEntry(): LeaderboardEntity? {
        return gameDao.getPlayerLeaderboardEntry()
    }

    suspend fun saveLeaderboardEntry(entry: LeaderboardEntity) {
        gameDao.insertOrUpdateLeaderboard(entry)
    }

    suspend fun populateBotsIfEmpty(force: Boolean = false) {
        // Simple check or we can just populate if the current list from database is empty.
        // Since flows are asynchronous, we can query or let the ViewModel handle population
        // when it detects empty list. We will define the list of bots here:
        val bots = listOf(
            LeaderboardEntity(name = "Satoshi8bit", score = 125000.0, avatarId = 0, miningType = "USDT", isPlayer = false),
            LeaderboardEntity(name = "HalFinneyRetro", score = 85000.0, avatarId = 1, miningType = "SOL", isPlayer = false),
            LeaderboardEntity(name = "VitalikRetro", score = 42000.0, avatarId = 2, miningType = "SOL", isPlayer = false),
            LeaderboardEntity(name = "DogePixel", score = 18500.0, avatarId = 3, miningType = "USDT", isPlayer = false),
            LeaderboardEntity(name = "TonGiga", score = 9200.0, avatarId = 4, miningType = "TON", isPlayer = false),
            LeaderboardEntity(name = "ByteASIC", score = 4800.0, avatarId = 5, miningType = "USDT", isPlayer = false),
            LeaderboardEntity(name = "AltcoinSlayer", score = 2500.0, avatarId = 6, miningType = "TON", isPlayer = false),
            LeaderboardEntity(name = "MicroRig", score = 1100.0, avatarId = 7, miningType = "SOL", isPlayer = false),
            LeaderboardEntity(name = "PixelSlasher", score = 450.0, avatarId = 8, miningType = "SOL", isPlayer = false),
            LeaderboardEntity(name = "NewbieMiner", score = 50.0, avatarId = 9, miningType = "USDT", isPlayer = false)
        )
        gameDao.insertLeaderboardEntries(bots)
    }

    suspend fun resetLeaderboardBots() {
        gameDao.clearBots()
        populateBotsIfEmpty(true)
    }
}
