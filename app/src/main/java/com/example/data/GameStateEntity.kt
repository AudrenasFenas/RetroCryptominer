package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val userId: String = "offline",
    val solBalance: Double = 0.0,
    val usdtBalance: Double = 0.0,
    val tonBalance: Double = 0.0,
    val solPower: Double = 0.01,
    val usdtPower: Double = 0.05,
    val tonPower: Double = 0.1,
    val solAutoRate: Double = 0.0,
    val usdtAutoRate: Double = 0.0,
    val tonAutoRate: Double = 0.0,
    val solClickCount: Int = 0,
    val usdtClickCount: Int = 0,
    val tonClickCount: Int = 0,
    val cpuUpgrades: Int = 0,
    val gpuUpgrades: Int = 0,
    val asicUpgrades: Int = 0,
    val nodeUpgrades: Int = 0,
    val farmUpgrades: Int = 0,
    val poolUpgrades: Int = 0,
    val playerName: String = "Anon Miner",
    val totalClicks: Int = 0,
    val level: Int = 1,
    val experience: Double = 0.0,
    val nextLevelXp: Double = 100.0,
    val lastDailyClaimTime: Long = 0L,
    val dailyStreak: Int = 0,
    val lastSaveTime: Long = System.currentTimeMillis()
)
