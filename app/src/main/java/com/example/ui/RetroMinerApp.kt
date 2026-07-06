package com.example.ui

import com.example.data.GameStateEntity
import com.example.data.LeaderboardEntity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

// Model for floating text popups
data class FlyingText(
    val id: Long,
    val text: String,
    val xOffset: Int,
    val yOffset: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroMinerApp(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val solPrice by viewModel.solPrice.collectAsState()
    val usdtPrice by viewModel.usdtPrice.collectAsState()
    val tonPrice by viewModel.tonPrice.collectAsState()
    val solTrend by viewModel.solTrend.collectAsState()
    val tonTrend by viewModel.tonTrend.collectAsState()
    val isMuted by viewModel.isSoundMuted.collectAsState()

    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val authSuccessMessage by viewModel.authSuccessMessage.collectAsState()

    var activeTab by remember { mutableStateOf("MINE") }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(gameState.playerName) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Local state for coin tapper floating flying indicators
    var flyingTexts by remember { mutableStateOf(listOf<FlyingText>()) }
    var runningId by remember { mutableLongStateOf(0L) }

    fun addFlyingText(text: String, color: Color) {
        val randomX = (-60..60).random()
        val randomY = (-30..30).random()
        val id = runningId++
        val ft = FlyingText(id, text, randomX, randomY, color)
        flyingTexts = flyingTexts + ft
        coroutineScope.launch {
            delay(700)
            flyingTexts = flyingTexts.filter { it.id != id }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = TerminalBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- TOP RETRO TITLE HEADER BAR (ELEGANT DARK STYLE) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF1ED760).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0A0A), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1ED760), RoundedCornerShape(4.dp))
                            .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SOL",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column {
                        Text(
                            text = "RETRO MINER [v4.2]",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier.clickable {
                                nameInput = gameState.playerName
                                showNameDialog = true
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PILOT: ${gameState.playerName.uppercase()}",
                                color = RetroGold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = RetroGold,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseAlpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF1ED760).copy(alpha = pulseAlpha), RoundedCornerShape(3.dp))
                            )
                            Text(
                                text = "AUTO-SAVING",
                                color = Color.White.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        val level = gameState.level
                        Text(
                            text = "LVL. $level MINER",
                            color = SolanaPurple,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Cheat trigger for easy demo/testing (secret pixel star icon)
                    IconButton(
                        onClick = { viewModel.cheatIncreaseBalances() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Cheat Coins",
                            tint = RetroGold.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Mute / Unmute Button
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .background(Color(0xFF121212), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Sound Config",
                            tint = if (isMuted) RetroAlert else SolanaGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // --- NET WORTH DISPLAY BOARD (ELEGANT DARK STYLE) ---
            val playerNetWorth = viewModel.calculateNetWorth(gameState)
            RetroCard(
                borderColor = SolanaPurple.copy(alpha = 0.5f),
                borderWidth = 1.5.dp,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0A0A))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CURRENT ASSETS",
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = String.format("$%,.2f", playerNetWorth),
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${String.format("%.2f", gameState.solBalance)} SOL",
                            color = SolanaGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.2f", gameState.usdtBalance)} USDT",
                            color = UsdtGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.2f", gameState.tonBalance)} TON",
                            color = ToncoinBlue,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "NETWORK SPEED: SOL+${String.format("%.2f", gameState.solAutoRate)}/s | USDT+${String.format("%.2f", gameState.usdtAutoRate)}/s | TON+${String.format("%.2f", gameState.tonAutoRate)}/s",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LEVEL ${gameState.level} PROGRESS",
                                color = RetroGold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.0f", gameState.experience)} / ${String.format(Locale.US, "%.0f", gameState.nextLevelXp)} XP",
                                color = Color.White.copy(alpha = 0.8f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(3.dp))
                                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                        ) {
                            val progressFactor = (gameState.experience / gameState.nextLevelXp).coerceIn(0.0, 1.0).toFloat()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFactor)
                                    .fillMaxHeight()
                                    .background(SolanaGreen, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }

            // --- COIN PRICE TICKERS BAR (ELEGANT DARK STYLE) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .background(TerminalCardBg, RoundedCornerShape(8.dp))
                    .padding(vertical = 6.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SOL: ",
                        color = SolanaPurple,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("$%.2f", solPrice),
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (solTrend > 0) " ▲" else if (solTrend < 0) " ▼" else " -",
                        color = if (solTrend > 0) SolanaGreen else if (solTrend < 0) RetroAlert else Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "USDT: ",
                        color = UsdtGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("$%.3f", usdtPrice),
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TON: ",
                        color = ToncoinBlue,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("$%.2f", tonPrice),
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (tonTrend > 0) " ▲" else if (tonTrend < 0) " ▼" else " -",
                        color = if (tonTrend > 0) SolanaGreen else if (tonTrend < 0) RetroAlert else Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- TAB SELECTOR (ELEGANT DARK STYLE) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("MINE", "UPGRADES", "CONVERT", "ARENA", "ACCOUNT")
                tabs.forEach { tab ->
                    val isActive = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = if (isActive) Color(0xFF1ED760).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                color = if (isActive) Color(0xFF1ED760).copy(alpha = 0.12f) else TerminalCardBg,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { activeTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isActive) Color(0xFF1ED760) else Color.White.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }

            // --- TAB CONTENT ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    "MINE" -> MineTab(
                        gameState = gameState,
                        solPrice = solPrice,
                        usdtPrice = usdtPrice,
                        tonPrice = tonPrice,
                        flyingTexts = flyingTexts,
                        onMineSol = {
                            viewModel.clickSolana()
                            addFlyingText("+${String.format("%.2f", gameState.solPower)} SOL", SolanaGreen)
                        },
                        onMineUsdt = {
                            viewModel.clickUsdt()
                            addFlyingText("+${String.format("%.2f", gameState.usdtPower)} USDT", UsdtGreen)
                        },
                        onMineTon = {
                            viewModel.clickTon()
                            addFlyingText("+${String.format("%.2f", gameState.tonPower)} TON", ToncoinBlue)
                        },
                        onClaimDailyReward = {
                            viewModel.claimDailyReward()
                        }
                    )
                    "UPGRADES" -> UpgradesTab(
                        gameState = gameState,
                        viewModel = viewModel
                    )
                    "CONVERT" -> ConvertTab(
                        gameState = gameState,
                        solPrice = solPrice,
                        tonPrice = tonPrice,
                        onConvert = { from, to, amount ->
                            viewModel.convertCoins(from, to, amount)
                        }
                    )
                    "ARENA" -> ArenaTab(
                        gameState = gameState,
                        leaderboard = leaderboard,
                        playerNetWorth = playerNetWorth,
                        onReset = { viewModel.resetProgress() }
                    )
                    "ACCOUNT" -> AccountTab(
                        currentUserId = currentUserId,
                        currentUserEmail = currentUserEmail,
                        authErrorMessage = authErrorMessage,
                        authSuccessMessage = authSuccessMessage,
                        onLogin = { email, pass -> viewModel.signInWithEmailAndPassword(email, pass) },
                        onRegister = { email, pass -> viewModel.registerWithEmailAndPassword(email, pass) },
                        onSignOut = { viewModel.signOut() },
                        onClearMessages = { viewModel.clearAuthMessages() }
                    )
                }
            }
        }
    }

    // --- PILOT NAME ENTRY DIALOG ---
    if (showNameDialog) {
        Dialog(onDismissRequest = { showNameDialog = false }) {
            RetroCard(
                borderColor = RetroGold,
                borderWidth = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(TerminalBlack)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "=== ENTER PILOT NAME ===",
                        color = RetroGold,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { if (it.length <= 16) nameInput = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        ),
                        placeholder = {
                            Text(
                                "NAME (MAX 16 CHARS)",
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedContainerColor = TerminalCardBg,
                            unfocusedContainerColor = TerminalCardBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (nameInput.isNotBlank()) {
                                viewModel.updatePlayerName(nameInput)
                                showNameDialog = false
                            }
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showNameDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalCardBg),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "CANCEL",
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                if (nameInput.isNotBlank()) {
                                    viewModel.updatePlayerName(nameInput)
                                    showNameDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SolanaPurple),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "CONFIRM",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// MINE TAB SCREEN
// ==========================================
@Composable
fun MineTab(
    gameState: GameStateEntity,
    solPrice: Double,
    usdtPrice: Double,
    tonPrice: Double,
    flyingTexts: List<FlyingText>,
    onMineSol: () -> Unit,
    onMineUsdt: () -> Unit,
    onMineTon: () -> Unit,
    onClaimDailyReward: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            DailyRewardCard(gameState = gameState, onClaim = onClaimDailyReward)
        }
        item {
            // Solana Mining Node Card
            RetroCard(borderColor = SolanaPurple, borderWidth = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PixelArtCoin(coinType = "SOL", modifier = Modifier.fillMaxSize()) {
                                onMineSol()
                            }

                            // Flying popup indicator for this coin
                            flyingTexts.forEach { ft ->
                                AnimatedFlyingText(ft = ft)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SOLANA STATION",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BlinkLed(isActive = gameState.nodeUpgrades > 0)
                            }
                            Text(
                                text = String.format("%.4f SOL", gameState.solBalance),
                                color = SolanaGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = String.format("Value: ≈ $%.2f", gameState.solBalance * solPrice),
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "TAP POWER: +${String.format("%.4f", gameState.solPower)} SOL | AUTO: ${String.format("%.4f", gameState.solAutoRate)}/s",
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp
                            )
                        }
                    }

                    Button(
                        onClick = onMineSol,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolanaPurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "MINE SOLANA (+${String.format("%.4f", gameState.solPower)})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            // USDT Mining Node Card
            RetroCard(borderColor = UsdtGreen, borderWidth = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PixelArtCoin(coinType = "USDT", modifier = Modifier.fillMaxSize()) {
                                onMineUsdt()
                            }

                            // Flying popup indicator for this coin
                            flyingTexts.forEach { ft ->
                                AnimatedFlyingText(ft = ft)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "USDT RIG",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BlinkLed(isActive = gameState.farmUpgrades > 0)
                            }
                            Text(
                                text = String.format("%.2f USDT", gameState.usdtBalance),
                                color = UsdtGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = String.format("Value: ≈ $%.2f", gameState.usdtBalance * usdtPrice),
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "TAP POWER: +${String.format("%.2f", gameState.usdtPower)} USDT | AUTO: ${String.format("%.2f", gameState.usdtAutoRate)}/s",
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp
                            )
                        }
                    }

                    Button(
                        onClick = onMineUsdt,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UsdtGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "MINE USDT (+${String.format("%.2f", gameState.usdtPower)})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            // Toncoin Mining Node Card
            RetroCard(borderColor = ToncoinBlue, borderWidth = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PixelArtCoin(coinType = "TON", modifier = Modifier.fillMaxSize()) {
                                onMineTon()
                            }

                            // Flying popup indicator for this coin
                            flyingTexts.forEach { ft ->
                                AnimatedFlyingText(ft = ft)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TONCOIN WORKER",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BlinkLed(isActive = gameState.poolUpgrades > 0)
                            }
                            Text(
                                text = String.format("%.2f TON", gameState.tonBalance),
                                color = ToncoinBlue,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = String.format("Value: ≈ $%.2f", gameState.tonBalance * tonPrice),
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "TAP POWER: +${String.format("%.2f", gameState.tonPower)} TON | AUTO: ${String.format("%.2f", gameState.tonAutoRate)}/s",
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp
                            )
                        }
                    }

                    Button(
                        onClick = onMineTon,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ToncoinBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "MINE TONCOIN (+${String.format("%.2f", gameState.tonPower)})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyRewardCard(
    gameState: GameStateEntity,
    onClaim: () -> Unit
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val oneDayMs = 24 * 60 * 60 * 1000L
    val timePassed = currentTime - gameState.lastDailyClaimTime
    val isAvailable = gameState.lastDailyClaimTime == 0L || timePassed >= oneDayMs

    RetroCard(
        borderColor = if (isAvailable) SolanaGreen else Color.DarkGray,
        borderWidth = 1.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isAvailable) Color(0xFF0F2015) else Color(0xFF141414))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isAvailable) SolanaGreen.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎁",
                    fontSize = 20.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DAILY REWARD RADAR",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                if (isAvailable) {
                    Text(
                        text = "Claim consecutive days to scale rewards! (Streak: ${gameState.dailyStreak}/7)",
                        color = SolanaGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp
                    )
                } else {
                    val remainingMs = oneDayMs - timePassed
                    val hours = (remainingMs / (1000 * 60 * 60)) % 24
                    val minutes = (remainingMs / (1000 * 60)) % 60
                    val seconds = (remainingMs / 1000) % 60
                    Text(
                        text = String.format(Locale.US, "Next Claim in %02d:%02d:%02d (Streak: %d/7)", hours, minutes, seconds, gameState.dailyStreak),
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp
                    )
                }
            }

            Button(
                onClick = onClaim,
                enabled = isAvailable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SolanaGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = if (isAvailable) "CLAIM" else "LOCKED",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun AnimatedFlyingText(ft: FlyingText) {
    val infiniteTransition = rememberInfiniteTransition(label = "FloatText")
    val animatedY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FloatAnim"
    )
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AlphaAnim"
    )

    Box(
        modifier = Modifier
            .offset(x = ft.xOffset.dp, y = (ft.yOffset + animatedY).dp)
    ) {
        Text(
            text = ft.text,
            color = ft.color.copy(alpha = animatedAlpha),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// UPGRADES TAB SCREEN
// ==========================================
@Composable
fun UpgradesTab(
    gameState: GameStateEntity,
    viewModel: GameViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "=== HARDWARE STORES (TAP MULTIPLIERS) ===",
                color = RetroGold,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            // SOL TAP: CPU Tapper
            val cost = viewModel.getUpgradeCost(0.05, gameState.cpuUpgrades)
            val canAfford = gameState.solBalance >= cost
            UpgradeRow(
                title = "CPU CORE TAPPER [SOL]",
                level = gameState.cpuUpgrades,
                desc = "Increases manual Solana taps by +0.01 SOL/click",
                costText = String.format("%.2f SOL", cost),
                canAfford = canAfford,
                accentColor = SolanaPurple,
                onBuy = { viewModel.buyCpuUpgrade() }
            )
        }

        item {
            // USDT TAP: GPU RIG
            val cost = viewModel.getUpgradeCost(10.0, gameState.gpuUpgrades)
            val canAfford = gameState.usdtBalance >= cost
            UpgradeRow(
                title = "GPU BLOCK RIG [USDT]",
                level = gameState.gpuUpgrades,
                desc = "Increases manual USDT taps by +0.05 USDT/click",
                costText = String.format("%.2f USDT", cost),
                canAfford = canAfford,
                accentColor = UsdtGreen,
                onBuy = { viewModel.buyGpuUpgrade() }
            )
        }

        item {
            // TON TAP: ASIC Miner
            val cost = viewModel.getUpgradeCost(1.5, gameState.asicUpgrades)
            val canAfford = gameState.tonBalance >= cost
            UpgradeRow(
                title = "ASIC WORKER RIG [TON]",
                level = gameState.asicUpgrades,
                desc = "Increases manual Toncoin taps by +0.12 TON/click",
                costText = String.format("%.2f TON", cost),
                canAfford = canAfford,
                accentColor = ToncoinBlue,
                onBuy = { viewModel.buyAsicUpgrade() }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "=== NETWORK CLOUD (PASSIVE AUTO-MINERS) ===",
                color = RetroGold,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            // SOL AUTO: Solana Validator Node
            val cost = viewModel.getUpgradeCost(0.5, gameState.nodeUpgrades)
            val canAfford = gameState.solBalance >= cost
            UpgradeRow(
                title = "VALIDATOR NODE [SOL AUTO]",
                level = gameState.nodeUpgrades,
                desc = "Generates +0.02 SOL automatically every second",
                costText = String.format("%.2f SOL", cost),
                canAfford = canAfford,
                accentColor = SolanaPurple,
                onBuy = { viewModel.buyNodeUpgrade() }
            )
        }

        item {
            // USDT AUTO: Server Farm
            val cost = viewModel.getUpgradeCost(50.0, gameState.farmUpgrades)
            val canAfford = gameState.usdtBalance >= cost
            UpgradeRow(
                title = "SERVER RACK FARM [USDT AUTO]",
                level = gameState.farmUpgrades,
                desc = "Generates +0.25 USDT automatically every second",
                costText = String.format("%.2f USDT", cost),
                canAfford = canAfford,
                accentColor = UsdtGreen,
                onBuy = { viewModel.buyFarmUpgrade() }
            )
        }

        item {
            // TON AUTO: Mining Pool
            val cost = viewModel.getUpgradeCost(4.0, gameState.poolUpgrades)
            val canAfford = gameState.tonBalance >= cost
            UpgradeRow(
                title = "MINING POOL HOST [TON AUTO]",
                level = gameState.poolUpgrades,
                desc = "Generates +0.35 TON automatically every second",
                costText = String.format("%.2f TON", cost),
                canAfford = canAfford,
                accentColor = ToncoinBlue,
                onBuy = { viewModel.buyPoolUpgrade() }
            )
        }
    }
}

@Composable
fun UpgradeRow(
    title: String,
    level: Int,
    desc: String,
    costText: String,
    canAfford: Boolean,
    accentColor: Color,
    onBuy: () -> Unit
) {
    RetroCard(borderColor = if (canAfford) accentColor else Color.DarkGray, borderWidth = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = "$title Lvl $level",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = desc,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }

            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = TerminalCardBg
                ),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = canAfford),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Text(
                    text = if (canAfford) "BUY\n$costText" else "LOCKED\n$costText",
                    color = if (canAfford) Color.White else Color.DarkGray,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp
                )
            }
        }
    }
}

// ==========================================
// CONVERT TAB SCREEN
// ==========================================
@Composable
fun ConvertTab(
    gameState: GameStateEntity,
    solPrice: Double,
    tonPrice: Double,
    onConvert: (String, String, Double) -> Unit
) {
    var fromType by remember { mutableStateOf("SOL") }
    var toType by remember { mutableStateOf("USDT") }
    var inputAmount by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    val balance = when (fromType) {
        "SOL" -> gameState.solBalance
        "TON" -> gameState.tonBalance
        else -> gameState.usdtBalance
    }

    val rate = when {
        fromType == "SOL" && toType == "USDT" -> solPrice
        fromType == "TON" && toType == "USDT" -> tonPrice
        fromType == "USDT" && toType == "SOL" -> 1.0 / solPrice
        fromType == "USDT" && toType == "TON" -> 1.0 / tonPrice
        else -> 1.0
    }

    val numericAmount = inputAmount.toDoubleOrNull() ?: 0.0
    val outputAmount = numericAmount * rate

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "=== DECENTRALIZED SWAP KIOSK ===",
                color = RetroGold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        item {
            RetroCard(borderColor = SolanaGreen, borderWidth = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SELECT ASSET EXCHANGE PATH:",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SOL to USDT option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, if (fromType == "SOL") SolanaPurple else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .background(if (fromType == "SOL") TerminalBlack else TerminalCardBg, RoundedCornerShape(8.dp))
                                .clickable {
                                    fromType = "SOL"
                                    toType = "USDT"
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SOL ➔ USDT", color = if (fromType == "SOL") Color.White else Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }

                        // TON to USDT option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, if (fromType == "TON") ToncoinBlue else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .background(if (fromType == "TON") TerminalBlack else TerminalCardBg, RoundedCornerShape(8.dp))
                                .clickable {
                                    fromType = "TON"
                                    toType = "USDT"
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("TON ➔ USDT", color = if (fromType == "TON") Color.White else Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }

                        // USDT to SOL option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, if (fromType == "USDT" && toType == "SOL") SolanaGreen else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .background(if (fromType == "USDT" && toType == "SOL") TerminalBlack else TerminalCardBg, RoundedCornerShape(8.dp))
                                .clickable {
                                    fromType = "USDT"
                                    toType = "SOL"
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("USDT ➔ SOL", color = if (fromType == "USDT" && toType == "SOL") Color.White else Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "AVAILABLE BALANCE: ${String.format("%.4f", balance)} $fromType",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )

                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        ),
                        placeholder = {
                            Text(
                                "0.0",
                                color = Color.DarkGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedContainerColor = TerminalBlack,
                            unfocusedContainerColor = TerminalBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        suffix = {
                            Text(fromType, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.1, 0.25, 0.5, 1.0).forEach { percent ->
                            val pctText = if (percent == 1.0) "MAX" else "${(percent * 100).toInt()}%"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .background(TerminalCardBg, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val amt = balance * percent
                                        inputAmount = String.format(Locale.US, "%.4f", amt)
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(pctText, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "RECEIVE AMOUNT:",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )

                    Text(
                        text = "${String.format("%.4f", outputAmount)} $toType",
                        color = SolanaGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Text(
                        text = "Exchange rate: 1 $fromType ≈ ${String.format("%.4f", rate)} $toType",
                        color = Color.DarkGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp
                    )

                    Button(
                        onClick = {
                            if (numericAmount > 0.0 && numericAmount <= balance) {
                                onConvert(fromType, toType, numericAmount)
                                inputAmount = ""
                                focusManager.clearFocus()
                            }
                        },
                        enabled = numericAmount > 0.0 && numericAmount <= balance,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolanaPurple,
                            disabledContainerColor = TerminalCardBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = numericAmount > 0.0 && numericAmount <= balance),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (numericAmount > balance) "INSUFFICIENT FUNDS" else "EXECUTE SMART SWAP",
                            color = if (numericAmount > 0.0 && numericAmount <= balance) Color.White else Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// ARENA (LEADERBOARD) TAB SCREEN
// ==========================================
@Composable
fun ArenaTab(
    gameState: GameStateEntity,
    leaderboard: List<LeaderboardEntity>,
    playerNetWorth: Double,
    onReset: () -> Unit
) {
    var showConfirmReset by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "=== COMPETITIVE MINING ARENA ===",
                color = RetroGold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            // Reset Board Option
            Text(
                text = "[RESET PROGRESS]",
                color = RetroAlert,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { showConfirmReset = true }
                    .padding(4.dp)
            )
        }

        // Leaderboard List
        RetroCard(borderColor = Color.White, borderWidth = 2.dp, modifier = Modifier.weight(1f)) {
            if (leaderboard.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SolanaPurple)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TerminalBlack)
                ) {
                    itemsIndexed(leaderboard) { index, entry ->
                        val isSelf = entry.isPlayer
                        val rank = index + 1
                        val rankBadge = when (rank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "#$rank"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelf) SolanaPurple.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelf) SolanaPurple else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 6.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Rank number
                            Text(
                                text = rankBadge,
                                color = if (isSelf) RetroGold else Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.width(32.dp)
                            )

                            // 8-bit Avatar
                            PixelAvatar(
                                avatarId = entry.avatarId,
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            )

                            // Player Name
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = entry.name.uppercase(),
                                    color = if (isSelf) Color.White else Color.LightGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Mining: ${entry.miningType}",
                                    color = Color.Gray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp
                                )
                            }

                            // Net Worth Score
                            Text(
                                text = String.format("$%,.0f", entry.score),
                                color = if (isSelf) SolanaGreen else Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }

                        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    }
                }
            }
        }

        if (showConfirmReset) {
            Dialog(onDismissRequest = { showConfirmReset = false }) {
                RetroCard(borderColor = RetroAlert, borderWidth = 3.dp) {
                    Column(
                        modifier = Modifier
                            .background(TerminalBlack)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚠ FACTORY RESET DATA? ⚠",
                            color = RetroAlert,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "ALL YOUR MINED SOL, USDT, TON, AND PURCHASED GPU/ASIC HARDWARE RIGS WILL BE FOREVER PURGED FROM THE BLOCKCHAIN. ARE YOU ABSOLUTELY SURE?",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showConfirmReset = false },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalCardBg),
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("GO BACK", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }

                            Button(
                                onClick = {
                                    onReset()
                                    showConfirmReset = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RetroAlert),
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("PURGE", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountTab(
    currentUserId: String,
    currentUserEmail: String?,
    authErrorMessage: String?,
    authSuccessMessage: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onClearMessages: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    // Clear messages when leaving or toggling mode
    LaunchedEffect(isRegistering) {
        onClearMessages()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            RetroCard(
                borderColor = if (currentUserId != "offline") SolanaGreen else RetroGold,
                borderWidth = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalCardBg)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (currentUserId != "offline") SolanaGreen.copy(alpha = 0.15f) else RetroGold.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.5.dp,
                                if (currentUserId != "offline") SolanaGreen else RetroGold,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentUserId != "offline") Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = "Status Icon",
                            tint = if (currentUserId != "offline") SolanaGreen else RetroGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = if (currentUserId != "offline") "=== CLOUD TERMINAL SECURE ===" else "=== LOCAL OFFLINE RADAR ===",
                        color = if (currentUserId != "offline") SolanaGreen else RetroGold,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    if (currentUserId != "offline") {
                        Text(
                            text = "Authenticated with Firebase. Your mining rigs and levels are securely synced and saved across devices.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("PILOT ACCOUNT:", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                Text(currentUserEmail ?: "Unknown", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("UID STRING:", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                Text(
                                    currentUserId.take(12) + "...",
                                    color = SolanaPurple,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SYNC STATUS:", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                Text("CONNECTED (OK)", color = SolanaGreen, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = RetroAlert),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                "TERMINATE SESSION (SIGN OUT)",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Text(
                            text = "You are currently playing as a local Anon Miner. Register or Sign In to sync your levels and cryptocoin balances safely to the cloud.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        if (currentUserId == "offline") {
            item {
                RetroCard(
                    borderColor = SolanaPurple,
                    borderWidth = 1.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalCardBg)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isRegistering) "=== REGISTER PILOT ACCOUNT ===" else "=== AUTHENTICATE PILOT ===",
                            color = SolanaPurple,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (authErrorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RetroAlert.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, RetroAlert, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = authErrorMessage,
                                    color = RetroAlert,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (authSuccessMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SolanaGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, SolanaGreen, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = authSuccessMessage,
                                    color = SolanaGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("PILOT EMAIL", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("enter email address...", color = Color.DarkGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SolanaPurple,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("SECURITY CODE (PASSWORD)", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("enter password (min 6 chars)...", color = Color.DarkGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                textStyle = LocalTextStyle.current.copy(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SolanaPurple,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (isRegistering) {
                                    onRegister(email, password)
                                } else {
                                    onLogin(email, password)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SolanaPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (isRegistering) "SIGN UP & SYNC PROGRESS" else "SIGN IN & SYNC",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isRegistering = !isRegistering }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isRegistering) "Already registered? SIGN IN instead" else "New pilot? CREATE AN ACCOUNT",
                                color = RetroGold,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        }
                    }
                }
            }
        }

        item {
            RetroCard(
                borderColor = Color.DarkGray,
                borderWidth = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalCardBg)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ℹ TERMINAL HARDWARE DIAGNOSTIC",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "This applet implements a live Firebase Auth integration. If you are running in a restricted sandbox or local developer container where a valid `google-services.json` is missing, Firebase requests will gracefully fail and fall back to local offline guest mode with persistent SQLite storage so your progress is never lost.",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}
