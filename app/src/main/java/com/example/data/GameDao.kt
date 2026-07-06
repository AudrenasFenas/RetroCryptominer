package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_state WHERE userId = :userId LIMIT 1")
    fun getGameStateFlow(userId: String): Flow<GameStateEntity?>

    @Query("SELECT * FROM game_state WHERE userId = :userId LIMIT 1")
    suspend fun getGameState(userId: String): GameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGameState(state: GameStateEntity)

    @Query("SELECT * FROM leaderboard ORDER BY score DESC")
    fun getLeaderboardFlow(): Flow<List<LeaderboardEntity>>

    @Query("SELECT * FROM leaderboard WHERE isPlayer = 1 LIMIT 1")
    suspend fun getPlayerLeaderboardEntry(): LeaderboardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLeaderboard(entry: LeaderboardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntity>)

    @Query("DELETE FROM leaderboard WHERE isPlayer = 0")
    suspend fun clearBots()
}
