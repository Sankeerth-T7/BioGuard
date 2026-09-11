package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creative BioScan AI analysis experience.
 * Features a circular leaf / biological radar scanner with rotating beams,
 * animated scanning laser, orbiting nature particles, and sequential analytical stages.
 */
@Composable
fun BioScanLoadingView(
    previewBitmap: Bitmap? = null,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors

    // Infinite transitions for rotating radar, scanline, and pulse
    val infiniteTransition = rememberInfiniteTransition(label = "BioScanAnimations")

    // Rotation angle for scanning ring
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    // Reverse slow rotation for orbiting biological particles
    val particleOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleOrbitAngle"
    )

    // Pulsing halo scale
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )

    // Scan line vertical position fraction (-0.1f to 1.1f)
    val scanLineFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineFraction"
    )

    // Changing analytical stages
    val stages = remember {
        listOf(
            "Examining visual features...",
            "Comparing species patterns...",
            "Analyzing habitat clues...",
            "Checking identifying characteristics...",
            "Preparing your biodiversity report..."
        )
    }

    var currentStageIndex by remember { mutableIntStateOf(0) }
    var subtitleMessage by remember { mutableStateOf("BioGuard AI is studying your discovery...") }

    LaunchedEffect(Unit) {
        var idx = 0
        while (true) {
            delay(1600)
            idx++
            if (idx < stages.size) {
                currentStageIndex = idx
                if (idx >= 3) {
                    subtitleMessage = "Almost ready..."
                }
            } else {
                // Loop on the final stage
                currentStageIndex = stages.size - 1
                subtitleMessage = "Finalizing taxonomy data..."
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top Badge: BIOSCAN ACTIVE
        Surface(
            color = if (bioColors.isDark) ForestGreenDark.copy(alpha = 0.6f) else ForestGreenPrimary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                if (bioColors.isDark) MintLight.copy(alpha = 0.4f) else ForestGreenPrimary.copy(alpha = 0.35f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BIOSCAN AI IN PROGRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Center Animated Scanning Chamber
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Canvas: Sonar concentric circles & sweeping radar beam
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.minDimension / 2f) - 14.dp.toPx()

                // Sonar rings
                drawCircle(
                    color = (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.12f * haloPulse),
                    radius = radius * 1.08f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.22f),
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.15f),
                    radius = radius * 0.7f,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Rotating radar beam
                rotate(degrees = radarAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.02f),
                                (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.45f)
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 75f,
                        useCenter = true
                    )
                }

                // Four cardinal biological alignment reticles (N, E, S, W)
                val tickLen = 10.dp.toPx()
                drawLine(
                    color = (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.7f),
                    start = Offset(center.x, center.y - radius - tickLen),
                    end = Offset(center.x, center.y - radius + 2.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.7f),
                    start = Offset(center.x, center.y + radius + tickLen),
                    end = Offset(center.x, center.y + radius - 2.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.7f),
                    start = Offset(center.x - radius - tickLen, center.y),
                    end = Offset(center.x - radius + 2.dp.toPx(), center.y),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.7f),
                    start = Offset(center.x + radius + tickLen, center.y),
                    end = Offset(center.x + radius - 2.dp.toPx(), center.y),
                    strokeWidth = 2.5.dp.toPx()
                )
            }

            // Central Specimen Viewfinder / Leaf Core
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        if (bioColors.isDark) Color(0xFF0F2618)
                        else SoftGreenContainer
                    )
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                if (bioColors.isDark) MintLight else ForestGreenPrimary,
                                EmeraldAccent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    // Specimen image with subtle scanning overlay
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Specimen under analysis",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Darkening shade to make scan beam pop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )
                } else {
                    // Stylized biodiversity icon with pulsing glow
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                (if (bioColors.isDark) MintLight else ForestGreenPrimary).copy(alpha = 0.18f * haloPulse)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "BioGuard Nature Leaf",
                            tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                // Animated horizontal laser scanning beam moving vertically
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (160 * scanLineFraction).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    (if (bioColors.isDark) MintLight else EmeraldAccent),
                                    Color.White,
                                    (if (bioColors.isDark) MintLight else EmeraldAccent),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Orbiting biological elements & sparkles (✦, ✧, 🌿, ◌)
            val orbitRadius = 105.dp
            val radAngle1 = Math.toRadians(particleOrbitAngle.toDouble())
            val radAngle2 = Math.toRadians((particleOrbitAngle + 90).toDouble())
            val radAngle3 = Math.toRadians((particleOrbitAngle + 180).toDouble())
            val radAngle4 = Math.toRadians((particleOrbitAngle + 270).toDouble())

            Text(
                text = "✦",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                modifier = Modifier.offset(
                    x = (orbitRadius.value * cos(radAngle1)).dp,
                    y = (orbitRadius.value * sin(radAngle1)).dp
                )
            )

            Text(
                text = "🌿",
                fontSize = 13.sp,
                modifier = Modifier.offset(
                    x = (orbitRadius.value * cos(radAngle2)).dp,
                    y = (orbitRadius.value * sin(radAngle2)).dp
                )
            )

            Text(
                text = "✧",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (bioColors.isDark) DarkAmberText else EarthyAmber,
                modifier = Modifier.offset(
                    x = (orbitRadius.value * cos(radAngle3)).dp,
                    y = (orbitRadius.value * sin(radAngle3)).dp
                )
            )

            Text(
                text = "◌",
                fontSize = 15.sp,
                color = if (bioColors.isDark) MintLight.copy(alpha = 0.6f) else ForestGreenPrimary.copy(alpha = 0.5f),
                modifier = Modifier.offset(
                    x = (orbitRadius.value * cos(radAngle4)).dp,
                    y = (orbitRadius.value * sin(radAngle4)).dp
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic Stage Announcement with smooth AnimatedContent transition
        AnimatedContent(
            targetState = stages[currentStageIndex],
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
            label = "StageAnimation"
        ) { stageText ->
            Text(
                text = stageText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = bioColors.textPrimary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle message
        Text(
            text = subtitleMessage,
            fontSize = 12.sp,
            color = bioColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Analytical Progress Dots (1 through 5)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stages.indices.forEach { index ->
                val isActive = index <= currentStageIndex
                Box(
                    modifier = Modifier
                        .size(if (index == currentStageIndex) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) (if (bioColors.isDark) MintLight else ForestGreenPrimary)
                            else bioColors.surfaceBorder
                        )
                )
            }
        }
    }
}
