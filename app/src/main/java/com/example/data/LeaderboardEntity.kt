package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard")
data class LeaderboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val score: Double,
    val avatarId: Int,
    val miningType: String,
    val isPlayer: Boolean = false,
    val lastUpdate: Long = System.currentTimeMillis()
)
