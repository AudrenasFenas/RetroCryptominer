package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameStateEntity
import com.example.data.GameRepository
import com.example.data.LeaderboardEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.pow

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // Prices of Cryptocurrencies (Simulated)
    private val _solPrice = MutableStateFlow(142.50)
    val solPrice: StateFlow<Double> = _solPrice.asStateFlow()

    private val _usdtPrice = MutableStateFlow(1.00)
    val usdtPrice: StateFlow<Double> = _usdtPrice.asStateFlow()

    private val _tonPrice = MutableStateFlow(7.62)
    val tonPrice: StateFlow<Double> = _tonPrice.asStateFlow()

    // Price trends: 1 for UP, -1 for DOWN, 0 for stable
    private val _solTrend = MutableStateFlow(1)
    val solTrend: StateFlow<Int> = _solTrend.asStateFlow()

    private val _tonTrend = MutableStateFlow(-1)
    val tonTrend: StateFlow<Int> = _tonTrend.asStateFlow()

    // Game state
    private val _gameState = MutableStateFlow(GameStateEntity())
    val gameState: StateFlow<GameStateEntity> = _gameState.asStateFlow()

    // Leaderboard flow (reactive from database)
    val leaderboard: StateFlow<List<LeaderboardEntity>> = repository.leaderboardFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val random = Random()
    private var gameLoopJob: Job? = null
    private var saveTimerCount = 0

    // Sound control
    private val _isSoundMuted = MutableStateFlow(RetroSoundManager.getIsMuted())
    val isSoundMuted: StateFlow<Boolean> = _isSoundMuted.asStateFlow()

    // --- FIREBASE AUTHENTICATION ---
    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            android.util.Log.e("GameViewModel", "Firebase Auth not available: ${e.message}")
            null
        }
    }

    private val _currentUserId = MutableStateFlow("offline")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    private var stateCollectJob: Job? = null

    init {
        // Populate bots in DB
        viewModelScope.launch {
            repository.populateBotsIfEmpty()
        }

        // Initialize Firebase Auth listener safely
        setupAuthListener()

        // Observe userId and switch database flows accordingly
        viewModelScope.launch {
            _currentUserId.collect { userId ->
                observeGameStateForUser(userId)
            }
        }

        // Start core game loops (auto-mining, price fluctuations, bot scoring)
        startGameLoops()
    }

    private fun setupAuthListener() {
        firebaseAuth?.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                _currentUserId.value = user.uid
                _currentUserEmail.value = user.email
            } else {
                _currentUserId.value = "offline"
                _currentUserEmail.value = null
            }
        }
    }

    private fun observeGameStateForUser(userId: String) {
        stateCollectJob?.cancel()
        stateCollectJob = viewModelScope.launch {
            repository.getGameStateFlow(userId).collect { savedState ->
                if (savedState != null) {
                    _gameState.value = savedState
                } else {
                    // Create a new default state for this user
                    val defaultState = GameStateEntity(
                        userId = userId,
                        playerName = if (userId == "offline") "Anon Miner" else _currentUserEmail.value?.substringBefore("@") ?: "Online Miner"
                    )
                    repository.saveGameState(defaultState)
                }
            }
        }
    }

    private suspend fun migrateOfflineProgressToOnline(onlineUid: String, onlineEmail: String) {
        // Check if online state already exists and has non-zero balances/upgrades.
        // If it doesn't, copy current offline values over!
        val onlineState = repository.getGameState(onlineUid)
        if (onlineState == null || (onlineState.solBalance == 0.0 && onlineState.usdtBalance == 0.0 && onlineState.tonBalance == 0.0 && onlineState.totalClicks == 0)) {
            val offlineState = repository.getGameState("offline") ?: _gameState.value
            val migratedState = offlineState.copy(
                userId = onlineUid,
                playerName = onlineEmail.substringBefore("@")
            )
            repository.saveGameState(migratedState)
            android.util.Log.d("GameViewModel", "Successfully migrated offline progress to Firebase account $onlineEmail")
        }
    }

    fun registerWithEmailAndPassword(email: String, pass: String) {
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
        val auth = firebaseAuth
        if (auth == null) {
            _authErrorMessage.value = "Firebase error: Please ensure google-services.json is configured."
            return
        }

        if (email.isBlank() || pass.length < 6) {
            _authErrorMessage.value = "Enter a valid email and password (min 6 chars)."
            return
        }

        try {
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            _authSuccessMessage.value = "Account created successfully!"
                            viewModelScope.launch {
                                migrateOfflineProgressToOnline(user.uid, email)
                            }
                        }
                    } else {
                        _authErrorMessage.value = task.exception?.localizedMessage ?: "Registration failed."
                    }
                }
        } catch (e: Exception) {
            _authErrorMessage.value = e.localizedMessage ?: "Unknown registration error."
        }
    }

    fun signInWithEmailAndPassword(email: String, pass: String) {
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
        val auth = firebaseAuth
        if (auth == null) {
            _authErrorMessage.value = "Firebase error: Please ensure google-services.json is configured."
            return
        }

        if (email.isBlank() || pass.isBlank()) {
            _authErrorMessage.value = "Please fill in all fields."
            return
        }

        try {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            _authSuccessMessage.value = "Logged in successfully!"
                            viewModelScope.launch {
                                migrateOfflineProgressToOnline(user.uid, email)
                            }
                        }
                    } else {
                        _authErrorMessage.value = task.exception?.localizedMessage ?: "Sign in failed."
                    }
                }
        } catch (e: Exception) {
            _authErrorMessage.value = e.localizedMessage ?: "Unknown sign-in error."
        }
    }

    fun signOut() {
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
        try {
            firebaseAuth?.signOut()
            _authSuccessMessage.value = "Signed out."
        } catch (e: Exception) {
            _authErrorMessage.value = e.localizedMessage ?: "Sign out failed."
        }
    }

    fun clearAuthMessages() {
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
    }

    private fun startGameLoops() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)

                // 1. Accumulate auto-mining earnings
                val state = _gameState.value
                val newSol = state.solBalance + (state.solAutoRate)
                val newUsdt = state.usdtBalance + (state.usdtAutoRate)
                val newTon = state.tonBalance + (state.tonAutoRate)

                // Update local state state
                _gameState.value = state.copy(
                    solBalance = newSol,
                    usdtBalance = newUsdt,
                    tonBalance = newTon
                )

                // 2. Periodic Price Fluctuations & Bot Updates (every 4 seconds)
                saveTimerCount++
                if (saveTimerCount % 4 == 0) {
                    fluctuatePrices()
                    updateBotLeaderboardScores()
                }

                // 3. Update player's own entry in the database for the leaderboard
                updatePlayerLeaderboardScore()

                // 4. Auto-save to Room database every 5 seconds
                if (saveTimerCount % 5 == 0) {
                    repository.saveGameState(_gameState.value)
                }
            }
        }
    }

    private fun fluctuatePrices() {
        // SOL price fluctuates between $100 and $250
        val solChangePercent = (random.nextDouble() * 3.0 - 1.4) / 100.0 // -1.4% to +1.6%
        val oldSolPrice = _solPrice.value
        val newSolPrice = (oldSolPrice * (1 + solChangePercent)).coerceIn(90.0, 280.0)
        _solPrice.value = Math.round(newSolPrice * 100.0) / 100.0
        _solTrend.value = if (newSolPrice > oldSolPrice) 1 else if (newSolPrice < oldSolPrice) -1 else 0

        // TON price fluctuates between $4 and $15
        val tonChangePercent = (random.nextDouble() * 4.0 - 2.0) / 100.0 // -2.0% to +2.0%
        val oldTonPrice = _tonPrice.value
        val newTonPrice = (oldTonPrice * (1 + tonChangePercent)).coerceIn(3.0, 20.0)
        _tonPrice.value = Math.round(newTonPrice * 100.0) / 100.0
        _tonTrend.value = if (newTonPrice > oldTonPrice) 1 else if (newTonPrice < oldTonPrice) -1 else 0

        // USDT fluctuates minimally around $1
        _usdtPrice.value = 1.00 + (random.nextDouble() * 0.004 - 0.002) // $0.998 to $1.002
    }

    private suspend fun updateBotLeaderboardScores() {
        val currentList = leaderboard.value
        val state = _gameState.value
        val playerNetWorth = calculateNetWorth(state)

        currentList.forEach { bot ->
            if (!bot.isPlayer) {
                // Bots earn passively based on their rank
                // High rank bots earn more. Add small random passive increase
                val baseEarning = when (bot.name) {
                    "Satoshi8bit" -> 350.0
                    "HalFinneyRetro" -> 200.0
                    "VitalikRetro" -> 120.0
                    "DogePixel" -> 60.0
                    "TonGiga" -> 35.0
                    "ByteASIC" -> 20.0
                    "AltcoinSlayer" -> 10.0
                    "MicroRig" -> 5.0
                    else -> 2.0
                }
                val multiplier = 0.8 + (random.nextDouble() * 0.4) // 80% to 120%
                val increment = baseEarning * multiplier
                val newScore = bot.score + increment

                // If player was below this bot, but is now above it, play milestone sound!
                val oldScore = bot.score
                if (oldScore > playerNetWorth && newScore <= playerNetWorth) {
                    RetroSoundManager.playMilestoneSound()
                }

                repository.saveLeaderboardEntry(bot.copy(score = Math.round(newScore * 100.0) / 100.0))
            }
        }
    }

    private suspend fun updatePlayerLeaderboardScore() {
        val state = _gameState.value
        val netWorth = calculateNetWorth(state)

        // Find or create player leaderboard entry
        val currentList = leaderboard.value
        val playerEntry = currentList.find { it.isPlayer }

        if (playerEntry == null) {
            repository.saveLeaderboardEntry(
                LeaderboardEntity(
                    name = state.playerName,
                    score = Math.round(netWorth * 100.0) / 100.0,
                    avatarId = 11, // Special Player avatar
                    miningType = "MULTI",
                    isPlayer = true
                )
            )
        } else {
            // Update name and score if changed
            if (playerEntry.score != netWorth || playerEntry.name != state.playerName) {
                repository.saveLeaderboardEntry(
                    playerEntry.copy(
                        name = state.playerName,
                        score = Math.round(netWorth * 100.0) / 100.0
                    )
                )
            }
        }
    }

    fun calculateNetWorth(state: GameStateEntity): Double {
        return (state.solBalance * _solPrice.value) +
                (state.usdtBalance * _usdtPrice.value) +
                (state.tonBalance * _tonPrice.value)
    }

    // --- EXPERIENCE & LEVEL UP SYSTEM ---

    private fun addExperience(state: GameStateEntity, amount: Double): GameStateEntity {
        var currentXp = state.experience + amount
        var currentLevel = state.level
        var nextXp = state.nextLevelXp
        var leveledUp = false

        while (currentXp >= nextXp) {
            currentLevel += 1
            currentXp -= nextXp
            nextXp = 100.0 + (currentLevel * 50.0) // Scale XP required for next level
            leveledUp = true
        }

        if (leveledUp) {
            RetroSoundManager.playMilestoneSound()
            // Level up rewards: multiplier based on level achieved
            val solBonus = 0.05 * currentLevel
            val usdtBonus = 2.0 * currentLevel
            val tonBonus = 0.5 * currentLevel
            
            // Increment clicking power with level
            return state.copy(
                level = currentLevel,
                experience = currentXp,
                nextLevelXp = nextXp,
                solPower = state.solPower + 0.005,
                usdtPower = state.usdtPower + 0.02,
                tonPower = state.tonPower + 0.05,
                solBalance = state.solBalance + solBonus,
                usdtBalance = state.usdtBalance + usdtBonus,
                tonBalance = state.tonBalance + tonBonus
            )
        } else {
            return state.copy(
                experience = currentXp
            )
        }
    }

    // --- DAILY REWARD SYSTEM ---

    fun claimDailyReward() {
        val state = _gameState.value
        val currentTime = System.currentTimeMillis()
        val timePassed = currentTime - state.lastDailyClaimTime
        val oneDayMs = 24 * 60 * 60 * 1000L
        val fortyEightHoursMs = 48 * 60 * 60 * 1000L

        // Check if eligible
        if (state.lastDailyClaimTime > 0 && timePassed < oneDayMs) {
            return
        }

        RetroSoundManager.playMilestoneSound()

        // Determine streak (reset to 1 if more than 48 hours passed)
        val newStreak = if (state.lastDailyClaimTime == 0L || timePassed > fortyEightHoursMs) {
            1
        } else {
            val s = state.dailyStreak + 1
            if (s > 7) 7 else s
        }

        val multiplier = newStreak.toDouble()
        val solReward = 0.05 * multiplier
        val usdtReward = 2.5 * multiplier
        val tonReward = 0.25 * multiplier
        val xpReward = 30.0 * multiplier

        val rewardedState = state.copy(
            solBalance = state.solBalance + solReward,
            usdtBalance = state.usdtBalance + usdtReward,
            tonBalance = state.tonBalance + tonReward,
            lastDailyClaimTime = currentTime,
            dailyStreak = newStreak
        )

        _gameState.value = addExperience(rewardedState, xpReward)
        viewModelScope.launch {
            repository.saveGameState(_gameState.value)
            updatePlayerLeaderboardScore()
        }
    }

    // --- GAME ACTIONS ---

    fun clickSolana() {
        RetroSoundManager.playTapSound()
        val state = _gameState.value
        val baseState = state.copy(
            solBalance = state.solBalance + state.solPower,
            solClickCount = state.solClickCount + 1,
            totalClicks = state.totalClicks + 1
        )
        // Gain 2 EXP per tap
        _gameState.value = addExperience(baseState, 2.0)
    }

    fun clickUsdt() {
        RetroSoundManager.playTapSound()
        val state = _gameState.value
        val baseState = state.copy(
            usdtBalance = state.usdtBalance + state.usdtPower,
            usdtClickCount = state.usdtClickCount + 1,
            totalClicks = state.totalClicks + 1
        )
        // Gain 2 EXP per tap
        _gameState.value = addExperience(baseState, 2.0)
    }

    fun clickTon() {
        RetroSoundManager.playTapSound()
        val state = _gameState.value
        val baseState = state.copy(
            tonBalance = state.tonBalance + state.tonPower,
            tonClickCount = state.tonClickCount + 1,
            totalClicks = state.totalClicks + 1
        )
        // Gain 2 EXP per tap
        _gameState.value = addExperience(baseState, 2.0)
    }

    fun updatePlayerName(newName: String) {
        if (newName.isNotBlank()) {
            _gameState.value = _gameState.value.copy(playerName = newName.take(16))
            viewModelScope.launch {
                repository.saveGameState(_gameState.value)
                updatePlayerLeaderboardScore()
            }
        }
    }

    fun toggleMute() {
        val isMuted = RetroSoundManager.toggleMute()
        _isSoundMuted.value = isMuted
    }

    // --- UPGRADE SHOP COSTS & ACTIONS ---

    // Upgrade cost scaling formula: Base * 1.28^Level
    fun getUpgradeCost(baseCost: Double, level: Int): Double {
        val scale = 1.28.pow(level)
        return Math.round((baseCost * scale) * 100.0) / 100.0
    }

    fun buyCpuUpgrade() {
        val state = _gameState.value
        val cost = getUpgradeCost(0.05, state.cpuUpgrades) // 0.05 SOL base
        if (state.solBalance >= cost) {
            RetroSoundManager.playUpgradeSound()
            val baseState = state.copy(
                solBalance = state.solBalance - cost,
                cpuUpgrades = state.cpuUpgrades + 1,
                solPower = state.solPower + 0.01
            )
            // Buy cpu upgrade gives 15 EXP
            _gameState.value = addExperience(baseState, 15.0)
            viewModelScope.launch { repository.saveGameState(_gameState.value) }
        }
    }

    fun buyGpuUpgrade() {
        val state = _gameState.value
        val cost = getUpgradeCost(10.0, state.gpuUpgrades) // $10 base
        if (state.usdtBalance >= cost) {
            RetroSoundManager.playUpgradeSound()
            val baseState = state.copy(
                usdtBalance = state.usdtBalance - cost,
                gpuUpgrades = state.gpuUpgrades + 1,
                usdtPower = state.usdtPower + 0.05
            )
            // Buy gpu upgrade gives 25 EXP
            _gameState.value = addExperience(baseState, 25.0)
            viewModelScope.launch { repository.saveGameState(_gameState.value) }
        }
    }

    fun buyAsicUpgrade() {
        val state = _gameState.value
        val cost = getUpgradeCost(1.5, state.asicUpgrades) // 1.5 TON base
        if (state.tonBalance >= cost) {
            RetroSoundManager.playUpgradeSound()
            val baseState = state.copy(
                tonBalance = state.tonBalance - cost,
                asicUpgrades = state.asicUpgrades + 1,
                tonPower = state.tonPower + 0.12
            )
            // Buy asic upgrade gives 35 EXP
            _gameState.value = addExperience(baseState, 35.0)
            viewModelScope.launch { repository.saveGameState(_gameState.value) }
        }
    }

    fun buyNodeUpgrade() {
        val state = _gameState.value
        val cost = getUpgradeCost(0.5, state.nodeUpgrades) // 0.5 SOL base
        if (state.solBalance >= cost) {
            RetroSoundManager.playUpgradeSound()
            val baseState = state.copy(
                solBalance = state.solBalance - cost,
                nodeUpgrades = state.nodeUpgrades + 1,
                solAutoRate = state.solAutoRate + 0.02
            )
            // Buy node upgrade gives 40 EXP
            _gameState.value = addExperience(baseState, 40.0)
            viewModelScope.launch { repository.saveGameState(_gameState.value) }
        }
    }

    fun buyFarmUpgrade() {
        val state = _gameState.value
        val cost = getUpgradeCost(50.0, state.farmUpgrades) // $50 base
        if (state.usdtBalance >= cost) {
            RetroSoundManager.playUpgradeSound()
            val baseState = state.copy(
                usdtBalance = state.usdtBalance - cost,
                farmUpgrades = state.farmUpgrades + 1,
                usdtAutoRate = state.usdtAutoRate + 0.25
            )
            // Buy farm upgrade gives 60 EXP
            _gameState.value = addExperience(baseState, 60.0)
            viewModelScope.launch { repository.saveGameState(_gameState.value) }
        }
    }

    fun buyPoolUpgrade() {
        val state = _gameState.value
        val cost = getUpgradeCost(4.0, state.poolUpgrades) // 4.0 TON base
        if (state.tonBalance >= cost) {
            RetroSoundManager.playUpgradeSound()
            val baseState = state.copy(
                tonBalance = state.tonBalance - cost,
                poolUpgrades = state.poolUpgrades + 1,
                tonAutoRate = state.tonAutoRate + 0.35
            )
            // Buy pool upgrade gives 80 EXP
            _gameState.value = addExperience(baseState, 80.0)
            viewModelScope.launch { repository.saveGameState(_gameState.value) }
        }
    }

    fun convertCoins(fromType: String, toType: String, amount: Double) {
        val state = _gameState.value
        if (amount <= 0.0) return

        when {
            fromType == "SOL" && toType == "USDT" -> {
                if (state.solBalance >= amount) {
                    val usdtGained = amount * _solPrice.value
                    _gameState.value = state.copy(
                        solBalance = state.solBalance - amount,
                        usdtBalance = state.usdtBalance + usdtGained
                    )
                    RetroSoundManager.playUpgradeSound()
                }
            }
            fromType == "TON" && toType == "USDT" -> {
                if (state.tonBalance >= amount) {
                    val usdtGained = amount * _tonPrice.value
                    _gameState.value = state.copy(
                        tonBalance = state.tonBalance - amount,
                        usdtBalance = state.usdtBalance + usdtGained
                    )
                    RetroSoundManager.playUpgradeSound()
                }
            }
            fromType == "USDT" && toType == "SOL" -> {
                if (state.usdtBalance >= amount) {
                    val solGained = amount / _solPrice.value
                    _gameState.value = state.copy(
                        usdtBalance = state.usdtBalance - amount,
                        solBalance = state.solBalance + solGained
                    )
                    RetroSoundManager.playUpgradeSound()
                }
            }
            fromType == "USDT" && toType == "TON" -> {
                if (state.usdtBalance >= amount) {
                    val tonGained = amount / _tonPrice.value
                    _gameState.value = state.copy(
                        usdtBalance = state.usdtBalance - amount,
                        tonBalance = state.tonBalance + tonGained
                    )
                    RetroSoundManager.playUpgradeSound()
                }
            }
        }
        viewModelScope.launch { repository.saveGameState(_gameState.value) }
    }

    fun cheatIncreaseBalances() {
        // Fun easter egg/debug cheat to test features quickly
        val state = _gameState.value
        _gameState.value = state.copy(
            solBalance = state.solBalance + 10.0,
            usdtBalance = state.usdtBalance + 1000.0,
            tonBalance = state.tonBalance + 100.0
        )
        RetroSoundManager.playMilestoneSound()
        viewModelScope.launch { repository.saveGameState(_gameState.value) }
    }

    fun resetProgress() {
        viewModelScope.launch {
            val defaultState = GameStateEntity(playerName = _gameState.value.playerName)
            repository.saveGameState(defaultState)
            _gameState.value = defaultState
            repository.resetLeaderboardBots()
            updatePlayerLeaderboardScore()
            RetroSoundManager.playMilestoneSound()
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
