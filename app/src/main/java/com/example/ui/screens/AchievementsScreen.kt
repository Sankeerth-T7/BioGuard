package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BioGuardRepository
import com.example.data.model.Achievement
import com.example.ui.theme.*

@Composable
fun AchievementsScreen(
    repository: BioGuardRepository,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val userProfile by repository.userProfile.collectAsState()
    val achievements by repository.achievements.collectAsState()
    var selectedBadge by remember { mutableStateOf<Achievement?>(null) }

    val levelNames = listOf(
        "Eco Scout",
        "Habitat Ranger",
        "Forest Guardian",
        "Wildlife Sentinel",
        "Ecosystem Champion"
    )
    val currentLevelName = levelNames.getOrElse(userProfile.level - 1) { "Ecosystem Champion" }

    val nextLevelPoints = when (userProfile.level) {
        1 -> 100
        2 -> 200
        3 -> 350
        4 -> 500
        else -> 500
    }
    val progress = (userProfile.points.toFloat() / nextLevelPoints).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        item {
            Column {
                Text(
                    text = "Conservation Badges & Level",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = bioColors.textPrimary
                )
                Text(
                    text = "Earn points by identifying species, filing threat reports, and completing biodiversity quizzes.",
                    fontSize = 12.sp,
                    color = bioColors.textSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Level & Points Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (bioColors.isDark) Color(0xFF132A1C) else ForestGreenPrimary
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(
                            if (bioColors.isDark) Color(0xFF1E462E) else ForestGreenDark,
                            if (bioColors.isDark) Color(0xFF2E6342) else ForestGreenPrimary
                        )
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "LEVEL ${userProfile.level}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MintLight
                            )
                            Text(
                                text = currentLevelName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            color = MintLight.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "⭐ ${userProfile.points} Pts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MintLight,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Next Tier: ${if (userProfile.level < 5) "Level ${userProfile.level + 1}" else "Max Tier Reached"}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${userProfile.points} / $nextLevelPoints pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MintLight,
                        trackColor = if (bioColors.isDark) Color(0xFF0D1F14) else ForestGreenDark
                    )
                }
            }
        }

        // Achievements Section
        item {
            Text(
                text = "Milestone Badges (${achievements.count { it.isUnlocked }} / ${achievements.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = bioColors.textPrimary
            )
        }

        items(achievements) { ach ->
            val progressHint = when (ach.title) {
                "First Discovery" -> if (ach.isUnlocked) "1/1 species recorded" else "0/1 species recorded"
                "Eco Scholar" -> if (ach.isUnlocked) "1/1 quiz completed" else "0/1 quiz completed"
                "Threat Spotter" -> if (ach.isUnlocked) "1/1 hazard reported" else "0/1 hazard reported"
                "Botanical Master" -> if (ach.isUnlocked) "5/5 flora identified" else "${(ach.progress * 5).toInt()}/5 flora identified"
                "Wildlife Guardian" -> if (ach.isUnlocked) "5/5 fauna logged" else "${(ach.progress * 5).toInt()}/5 fauna logged"
                else -> "${(ach.progress * 100).toInt()}% progress"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedBadge = ach },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (ach.isUnlocked) bioColors.containerGreen else bioColors.containerNeutral),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (ach.isUnlocked) ach.badgeEmoji else "🔒",
                            fontSize = 26.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ach.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) bioColors.textPrimary else bioColors.textMuted
                            )

                            Surface(
                                color = if (ach.isUnlocked) bioColors.containerGreen else bioColors.containerNeutral,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (ach.isUnlocked) "Unlocked ✓" else "+${ach.pointsReward} XP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ach.isUnlocked) bioColors.onContainerGreen else bioColors.textMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = ach.description,
                            fontSize = 12.sp,
                            color = bioColors.textSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = progressHint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = bioColors.textMuted
                            )
                            Text(
                                text = "${(ach.progress * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) ForestGreenPrimary else bioColors.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { ach.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = ForestGreenPrimary,
                            trackColor = bioColors.containerNeutral
                        )
                    }
                }
            }
        }
    }

    if (selectedBadge != null) {
        val ach = selectedBadge!!
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            containerColor = bioColors.surfaceCard,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(if (ach.isUnlocked) bioColors.containerGreen else bioColors.containerNeutral),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (ach.isUnlocked) ach.badgeEmoji else "🔒", fontSize = 30.sp)
                }
            },
            title = {
                Text(
                    text = ach.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = bioColors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = ach.description,
                        fontSize = 13.sp,
                        color = bioColors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        color = if (ach.isUnlocked) bioColors.containerGreen else bioColors.containerNeutral,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (ach.isUnlocked) "✓ Unlocked & Verified • Earned +${ach.pointsReward} XP" else "Locked Milestone Badge",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ach.isUnlocked) bioColors.onContainerGreen else bioColors.textMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    if (!ach.isUnlocked) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bioColors.containerNeutral)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Criteria to Unlock:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )
                            Text(
                                text = "Complete ${ach.description.lowercase()} in your field sessions.",
                                fontSize = 11.sp,
                                color = bioColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reward: +${ach.pointsReward} Conservation XP towards Level ${userProfile.level + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreenPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) {
                    Text("Got it", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
