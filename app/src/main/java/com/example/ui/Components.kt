package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Constants
val SolanaPurple = Color(0xFF9945FF)
val SolanaGreen = Color(0xFF1ED760) // Elegant Dark main active green (#1ed760)
val UsdtGreen = Color(0xFF26A17B)
val ToncoinBlue = Color(0xFF0088CC)
val TerminalBlack = Color(0xFF050505) // Elegant Dark deep pitch black (#050505)
val TerminalCardBg = Color(0xFF121212) // Elegant Dark gray card background (#121212)
val TerminalBorder = Color(0xFF1ED760).copy(alpha = 0.3f) // Elegant Dark neon green borders
val RetroGold = Color(0xFFFFB300)
val RetroAlert = Color(0xFFEF4444)

@Composable
fun PixelArtCoin(
    coinType: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // 16x16 grid layouts for pixel art
    val solanaGrid = listOf(
        "....SSSSSSSS....",
        "..SSSSCCCCSSSS..",
        ".SSSCCCCWWCCSSS.",
        "SSSCWWWWWWCCCCSS",
        "SSSCWWWWCCCCSSSS",
        "SSSCCCCWWCCCCSSS",
        "SSSSSSSCWCCCCSSS",
        "SSCCCCWCCCCSSSSS",
        "SCCCCWCCCCSSSSSS",
        "SCCCCWCCCCSSSSSS",
        "SSCCCCWCCCCSSSSS",
        "SSSCCCCWCCCCSSSS",
        "SSSSCCCCWCDSSSSS",
        ".SSSCCCCWCDSSSS.",
        "..SSSSCCCCSSSS..",
        "....SSSSSSSS...."
    )

    val usdtGrid = listOf(
        "....GGGGGGGG....",
        "..GGGGGGGGGGGG..",
        ".GGGGWWWWWWGGGG.",
        "GGGWWWWWWWWWWGGG",
        "GGGGGGWWWWGGGGGG",
        "GGGGGGWWWWGGGGGG",
        "GGWWWWWWWWWWWWGG",
        "GGWWWWWWWWWWWWGG",
        "GGGGGGWWWWGGGGGG",
        "GGGGGGWWWWGGGGGG",
        "GGGGGGWWWWGGGGGG",
        "GGGGGGWWWWGGGGGG",
        "GGG...WWWW...GGG",
        ".GG...WWWW...GG.",
        "..GGGGGGGGGGGG..",
        "....GGGGGGGG...."
    )

    val toncoinGrid = listOf(
        "....BBBBBBBB....",
        "..BBBBBBBBBBBB..",
        ".BBBBBBWWBBBBBB.",
        "BBBBBBWWWWBBBBBB",
        "BBBBBWWWWWWBBBBB",
        "BBBBWWWWWWWWBBBB",
        "BBBWWWW..WWWWBBB",
        "BBWWWW....WWWWBB",
        "BWWWWWWWWWWWWWWBB",
        "BBWWWWWWWWWWWWBB",
        "BBBWWWWWWWWWWBBB",
        "BBBBWWWWWWWWBBBB",
        "BBBBBWWWWWWBBBBB",
        ".BBBBBBWWBBBBBB.",
        "..BBBBBBBBBBBB..",
        "....BBBBBBBB...."
    )

    val currentGrid = when (coinType.uppercase()) {
        "SOL" -> solanaGrid
        "USDT" -> usdtGrid
        else -> toncoinGrid
    }

    var isPressed by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ScaleCoin"
    )

    Box(
        modifier = modifier
            .scale(scaleAnim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // Coin Drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val numCells = 16
            val cellSize = size.width / numCells

            for (r in 0 until numCells) {
                val rowStr = currentGrid[r]
                for (c in 0 until numCells) {
                    val char = rowStr[c]
                    val color = when (char) {
                        'S' -> SolanaPurple
                        'C' -> SolanaGreen
                        'G' -> UsdtGreen
                        'B' -> ToncoinBlue
                        'W' -> Color.White
                        'D' -> Color.DarkGray
                        else -> Color.Transparent
                    }
                    if (color != Color.Transparent) {
                        drawRect(
                            color = color,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }

        // Reset the scale press animation
        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(80)
                isPressed = false
            }
        }
    }
}

@Composable
fun PixelAvatar(
    avatarId: Int,
    modifier: Modifier = Modifier
) {
    // Standard 8x8 pixel faces for retro avatars
    // 0: Satoshi (retro green shades)
    // 1: Hal (vintage yellow smile)
    // 2: Vitalik (long purple alien face)
    // 3: Doge (pixel meme dog)
    // 4: TonGiga (blue laser eyes)
    // 5: ASIC (orange cyborg)
    // 6: AltcoinSlayer (red fighter)
    // 7: MicroRig (yellow gamer)
    // 8: PixelSlasher (green cyberninja)
    // 9: NewbieMiner (grey question)
    // Others: Custom Player Avatar (green headset/visor)

    val faces = listOf(
        // 0: Satoshi
        listOf(
            "........",
            "..BBBB..",
            ".BGGGGGB.",
            ".BGBBGB.",
            ".BGGGGGB.",
            ".BGGGGGB.",
            "..BBBB..",
            "........"
        ),
        // 1: Hal
        listOf(
            "........",
            "..YYYY..",
            ".YWWWWY.",
            ".YWYWYV.",
            ".YWWWWY.",
            ".YVWWVY.",
            "..YYYY..",
            "........"
        ),
        // 2: Vitalik
        listOf(
            "........",
            "..PPPP..",
            ".PWWWWP.",
            ".PWPWPP.",
            ".PWWWWP.",
            "..PPPP..",
            "..PPPP..",
            "........"
        ),
        // 3: Doge
        listOf(
            "........",
            ".YY..YY.",
            ".YYYYYY.",
            "YYWYYWY.",
            "YYYYYYY.",
            "YWWWYWY.",
            ".YYYYYY.",
            "........"
        ),
        // 4: TonGiga
        listOf(
            "........",
            "..CCCC..",
            ".CWWWWP.",
            ".CWCWCP.",
            ".CWWWWP.",
            ".CWWWWP.",
            "..CCCC..",
            "........"
        ),
        // 5: ASIC
        listOf(
            "........",
            ".RMRRMR.",
            ".RRRRRR.",
            ".RWRWRR.",
            ".RRRRRR.",
            ".RMRRMR.",
            ".RMRRMR.",
            "........"
        ),
        // 6: AltcoinSlayer
        listOf(
            "........",
            "..RRRR..",
            ".RWWWWR.",
            ".RWRWRR.",
            ".RWWWWR.",
            ".RWRWRR.",
            "..RRRR..",
            "........"
        ),
        // 7: MicroRig
        listOf(
            "........",
            "..YYYY..",
            ".YWWWWY.",
            ".YVWYVY.",
            ".YWWWWY.",
            ".YYVYYY.",
            "..YYYY..",
            "........"
        ),
        // 8: PixelSlasher
        listOf(
            "........",
            "..GGGG..",
            ".GWWWWG.",
            ".GWGWWG.",
            ".GWWWWG.",
            ".GGWGGG.",
            "..GGGG..",
            "........"
        ),
        // 9: NewbieMiner
        listOf(
            "........",
            "..DDDD..",
            ".DWWWWD.",
            ".DWDWDD.",
            ".DWWWWD.",
            ".DDWDDD.",
            "..DDDD..",
            "........"
        )
    )

    // Fallback Player visor avatar
    val playerFace = listOf(
        "........",
        "..CCCC..",
        ".CWWWWG.",
        ".CGGGGG.", // visors
        ".CWWWWG.",
        ".CGWWGC.",
        "..CCCC..",
        "........"
    )

    val currentFace = if (avatarId in 0..9) faces[avatarId] else playerFace

    Canvas(modifier = modifier) {
        val size8 = 8
        val cellSize = size.width / size8

        for (r in 0 until size8) {
            val rowStr = currentFace[r]
            for (c in 0 until size8) {
                val char = rowStr[c]
                val color = when (char) {
                    'B' -> Color(0xFF0F172A)
                    'G' -> Color(0xFF10B981)
                    'Y' -> Color(0xFFFBBF24)
                    'P' -> Color(0xFF8B5CF6)
                    'C' -> Color(0xFF06B6D4)
                    'R' -> Color(0xFFEF4444)
                    'M' -> Color(0xFFEC4899)
                    'W' -> Color.White
                    'D' -> Color.Gray
                    'V' -> Color(0xFFB45309)
                    else -> Color.Transparent
                }
                if (color != Color.Transparent) {
                    drawRect(
                        color = color,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}

@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    borderColor: Color = TerminalBorder,
    borderWidth: Dp = 1.5.dp, // Elegant thin borders
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp), // Elegant Dark style rounded corners
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalCardBg),
        shape = shape,
        modifier = modifier
            .border(borderWidth, borderColor, shape)
            .padding(1.dp),
        content = content
    )
}

@Composable
fun BlinkLed(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Blink")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LedBlink"
    )

    val color = if (isActive) {
        SolanaGreen.copy(alpha = alphaAnim)
    } else {
        RetroAlert.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .size(10.dp)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(5.dp))
            .padding(1.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = color)
        }
    }
}

@Composable
fun RetroProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    barColor: Color = SolanaGreen
) {
    Box(
        modifier = modifier
            .height(14.dp)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(TerminalBlack)
            .padding(1.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillWidth = size.width * progress.coerceIn(0f, 1f)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, 0f),
                size = Size(fillWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}
