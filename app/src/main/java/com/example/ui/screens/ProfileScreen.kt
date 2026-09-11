package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.BioGuardRepository
import com.example.ui.components.BioScreen
import com.example.ui.components.EducationalDisclaimer
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    repository: BioGuardRepository,
    onNavigate: (BioScreen) -> Unit,
    onOpenAiAssistant: () -> Unit,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by repository.userProfile.collectAsState()
    val isDemoMode by repository.isDemoMode.collectAsState()
    val isDarkOverride by repository.darkThemeOverride.collectAsState()
    val observations by repository.getObservations().collectAsState(initial = emptyList())
    val threatReports by repository.getThreatReports().collectAsState(initial = emptyList())
    val achievements by repository.achievements.collectAsState()
    val isSyncing by repository.isSyncing.collectAsState()
    val lastSyncTime by repository.lastSyncTime.collectAsState()
    val cloudStatusMessage by repository.cloudStatusMessage.collectAsState()

    // Current effective dark theme state
    val isCurrentlyDark = isDarkOverride ?: bioColors.isDark

    var showAboutDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var selectedAchievement by remember { mutableStateOf<com.example.data.model.Achievement?>(null) }

    val rankTitle = when (userProfile.level) {
        1 -> "Eco Scout"
        2 -> "Habitat Ranger"
        3 -> "Forest Guardian"
        4 -> "Wildlife Sentinel"
        else -> "Ecosystem Champion"
    }

    val nextLevelPoints = when (userProfile.level) {
        1 -> 100
        2 -> 200
        3 -> 350
        4 -> 500
        else -> 500
    }
    val levelProgress = (userProfile.points.toFloat() / nextLevelPoints).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top row with Sign Out / Logout icon button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { showSignOutDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                                    )
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Log Out",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Log Out",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Eco-Themed Avatar with Ring & Leaf Badge
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(86.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(bioColors.containerGreen)
                                .border(
                                    3.dp,
                                    Brush.sweepGradient(
                                        listOf(
                                            ForestGreenPrimary,
                                            EmeraldAccent,
                                            MintLight,
                                            ForestGreenPrimary
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = bioColors.onContainerGreen,
                                modifier = Modifier.size(62.dp)
                            )
                        }

                        // Eco leaf badge
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(ForestGreenPrimary)
                                .border(2.dp, bioColors.surfaceCard, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = bioColors.textPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            color = bioColors.containerGreen,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = bioColors.onContainerGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = rankTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bioColors.onContainerGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = userProfile.email,
                            fontSize = 12.sp,
                            color = bioColors.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Level Progress Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(bioColors.containerNeutral)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Level ${userProfile.level} - ${userProfile.points}/$nextLevelPoints XP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )
                            Text(
                                text = "Next: Level ${userProfile.level + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { levelProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ForestGreenPrimary,
                            trackColor = bioColors.surfaceBorder
                        )
                    }
                }
            }
        }

        // ECO CLOUD ACCOUNT & MULTI-DEVICE SYNC CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(bioColors.containerGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (userProfile.isGuest) Icons.Default.CloudOff else Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = bioColors.onContainerGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Eco-Cloud Sync",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bioColors.textPrimary
                                )
                                Text(
                                    text = if (userProfile.isGuest) "Offline / Guest Scout" else "Active on BioGuard Network",
                                    fontSize = 11.sp,
                                    color = if (userProfile.isGuest) bioColors.textMuted else (if (bioColors.isDark) MintLight else ForestGreenPrimary)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (userProfile.isGuest) bioColors.containerAmber else bioColors.containerGreen
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (userProfile.isGuest) bioColors.onContainerAmber else ForestGreenPrimary)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (userProfile.isGuest) "Guest Mode" else (userProfile.authProvider ?: "Firebase Auth"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userProfile.isGuest) bioColors.onContainerAmber else bioColors.onContainerGreen
                                )
                            }
                        }
                    }

                    Text(
                        text = if (userProfile.isGuest) {
                            "You are currently in offline scout mode. Sign in or continue with Google so that when you log in through another phone, all your observations, threat reports, and eco-points are automatically restored!"
                        } else {
                            val uid = userProfile.uid
                            val uidInfo = if (!uid.isNullOrBlank()) " • Firebase UID: ${uid.take(12)}…" else ""
                            "Logged in as ${userProfile.email}$uidInfo. Field observations, threat reports, and points saved on this phone will automatically appear when you log in on another phone."
                        },
                        fontSize = 12.sp,
                        color = bioColors.textSecondary,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = bioColors.surfaceBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!userProfile.isGuest) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val pulled = repository.syncFromCloud()
                                        val pushed = repository.syncToCloud()
                                        Toast.makeText(
                                            context,
                                            if (pulled || pushed) "Cloud sync complete ✓" else "Already up to date ✓",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Syncing...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { showSignOutDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                        )
                                    )
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = onSignOut,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Sign In / Create Account to Sync",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // QUICK STATS ROW
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickStatCard(
                    title = "Identified",
                    value = "${observations.size}",
                    icon = Icons.Default.CameraAlt,
                    accentColor = ForestGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Threats Logged",
                    value = "${threatReports.size}",
                    icon = Icons.Default.WarningAmber,
                    accentColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Quizzes Won",
                    value = "${(userProfile.points / 40).coerceAtLeast(3)}",
                    icon = Icons.Default.Quiz,
                    accentColor = ForestGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Days Active",
                    value = "14d",
                    icon = Icons.Default.CalendarToday,
                    accentColor = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ACHIEVEMENTS SHOWCASE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Conservation Badges",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )
                        }

                        TextButton(onClick = { onNavigate(BioScreen.ACHIEVEMENTS) }) {
                            Text("View All (${achievements.count { it.isUnlocked }}/${achievements.size})", fontSize = 12.sp, color = if (bioColors.isDark) MintLight else ForestGreenPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        achievements.take(4).forEach { ach ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (ach.isUnlocked) bioColors.containerGreen else bioColors.containerNeutral)
                                    .border(
                                        1.dp,
                                        if (ach.isUnlocked) ForestGreenPrimary.copy(alpha = 0.4f) else bioColors.surfaceBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedAchievement = ach }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (ach.isUnlocked) ach.badgeEmoji else "🔒",
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ach.title,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ach.isUnlocked) bioColors.textPrimary else bioColors.textMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Appearance & Theme Settings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isCurrentlyDark) DarkGreenContainer else SoftGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCurrentlyDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = if (isCurrentlyDark) Color(0xFFFDE047) else ForestGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dark Theme",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.textPrimary
                        )
                        Text(
                            text = if (isCurrentlyDark) "Dark mode enabled for night use & battery savings" else "Light mode enabled with high contrast nature palette",
                            fontSize = 11.sp,
                            color = bioColors.textSecondary,
                            lineHeight = 15.sp
                        )
                    }

                    Switch(
                        checked = isCurrentlyDark,
                        onCheckedChange = { repository.toggleDarkTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ForestGreenPrimary,
                            checkedTrackColor = bioColors.containerGreen,
                            uncheckedThumbColor = bioColors.textMuted,
                            uncheckedTrackColor = bioColors.containerNeutral
                        )
                    )
                }
            }
        }

        // Demo Mode Controller Card (for Presentation / Hackathon)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.containerAmber),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(bioColors.onContainerAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            tint = bioColors.onContainerAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Presentation Demo Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.onContainerAmber
                        )
                        Text(
                            text = if (isDemoMode) "Showing rich sample observations, threat logs, and species." else "Showing only your local field submissions.",
                            fontSize = 11.sp,
                            color = bioColors.onContainerAmber.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }

                    Switch(
                        checked = isDemoMode,
                        onCheckedChange = {
                            repository.toggleDemoMode()
                            Toast.makeText(
                                context,
                                if (!isDemoMode) "Demo data loaded for demonstration" else "Switched to clean user mode",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ForestGreenPrimary,
                            checkedTrackColor = bioColors.containerGreen
                        )
                    )
                }
            }
        }

        // Menu Hub Navigation
        item {
            Text(
                text = "Application Navigation",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = bioColors.textPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 2.dp)
            ) {
                Column {
                    ProfileMenuRow(
                        title = "My Observations & Field Diary",
                        subtitle = "Review and manage all recorded species",
                        icon = Icons.Default.CollectionsBookmark,
                        onClick = { onNavigate(BioScreen.OBSERVATIONS) }
                    )
                    HorizontalDivider(color = bioColors.surfaceBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        title = "Report Threat to Biodiversity",
                        subtitle = "Document habitat loss, deforestation, and pollution",
                        icon = Icons.Default.ReportProblem,
                        onClick = { onNavigate(BioScreen.THREAT_REPORT) }
                    )
                    HorizontalDivider(color = bioColors.surfaceBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        title = "Biodiversity & Ecosystem Quiz",
                        subtitle = "Test environmental knowledge and earn points",
                        icon = Icons.Default.Quiz,
                        onClick = { onNavigate(BioScreen.QUIZ) }
                    )
                    HorizontalDivider(color = bioColors.surfaceBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        title = "Conservation Action Guide",
                        subtitle = "Campus and community environmental tips",
                        icon = Icons.Default.TipsAndUpdates,
                        onClick = { onNavigate(BioScreen.TIPS) }
                    )
                    HorizontalDivider(color = bioColors.surfaceBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        title = "Conservation Badges & Tiers",
                        subtitle = "View your environmental achievements",
                        icon = Icons.Default.EmojiEvents,
                        onClick = { onNavigate(BioScreen.ACHIEVEMENTS) }
                    )
                    HorizontalDivider(color = bioColors.surfaceBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        title = "BioGuard AI Assistant",
                        subtitle = "Chat about flora, fauna, and ecological restoration",
                        icon = Icons.Default.AutoAwesome,
                        onClick = onOpenAiAssistant
                    )
                    HorizontalDivider(color = bioColors.surfaceBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        title = "About Project & Objectives",
                        subtitle = "Academic hackathon / college project details",
                        icon = Icons.Default.Info,
                        onClick = { showAboutDialog = true }
                    )
                    HorizontalDivider(color = bioColors.surfaceBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        title = "Log Out",
                        subtitle = "Sign out of your account on this device",
                        icon = Icons.Default.Logout,
                        titleColor = MaterialTheme.colorScheme.error,
                        iconColor = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        onClick = { showSignOutDialog = true }
                    )
                }
            }
        }

        // Disclaimer
        item {
            EducationalDisclaimer(
                text = "BIOGUARD is developed as an educational conservation project integrating Android Jetpack Compose, Gemini AI, and local data persistence."
            )
        }
    }

    // About Project Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = bioColors.surfaceCard,
            icon = {
                Icon(
                    Icons.Default.Eco,
                    contentDescription = null,
                    tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "About BIOGUARD",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = bioColors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Biodiversity & Conservation Assistant",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent
                    )
                    Text(
                        text = "A complete academic application addressing:\n" +
                                "• Biodiversity & Ecosystem Components\n" +
                                "• Flora, Fauna, and Keystone Species\n" +
                                "• Economic Values of Ecosystem Services\n" +
                                "• Habitat Loss, Deforestation, and Threats\n" +
                                "• National Parks & Wildlife Sanctuaries\n" +
                                "• Ecological Restoration & Social Forestry",
                        fontSize = 12.sp,
                        color = bioColors.textSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tech Stack: Android Jetpack Compose, Gemini AI API, Room Database, Material Design 3.",
                        fontSize = 11.sp,
                        color = bioColors.textMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = if (bioColors.isDark) MintLight else ForestGreenPrimary)
                }
            }
        )
    }

    // Achievement Detail Popup Dialog
    if (selectedAchievement != null) {
        val ach = selectedAchievement!!
        AlertDialog(
            onDismissRequest = { selectedAchievement = null },
            containerColor = bioColors.surfaceCard,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (ach.isUnlocked) bioColors.containerGreen else bioColors.containerNeutral),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (ach.isUnlocked) ach.badgeEmoji else "🔒", fontSize = 28.sp)
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
                            text = if (ach.isUnlocked) "✓ Unlocked • +${ach.pointsReward} XP Earned" else "Locked • Progress: ${(ach.progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ach.isUnlocked) bioColors.onContainerGreen else bioColors.textMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    if (!ach.isUnlocked) {
                        LinearProgressIndicator(
                            progress = { ach.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ForestGreenPrimary,
                            trackColor = bioColors.surfaceBorder
                        )
                        Text(
                            text = "Reward upon completion: +${ach.pointsReward} Conservation XP",
                            fontSize = 11.sp,
                            color = bioColors.textMuted
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAchievement = null }) {
                    Text("Close", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = bioColors.surfaceCard,
            icon = {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Sign Out of BioGuard?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = bioColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Your observations and reports are safely backed up to BioGuard Eco-Cloud. When you log in on another phone with your email, your data will be restored automatically.",
                    fontSize = 13.sp,
                    color = bioColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        repository.logout()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = bioColors.textPrimary)
                }
            }
        )
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = bioColors.textPrimary
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = bioColors.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    titleColor: Color? = null,
    iconColor: Color? = null,
    containerColor: Color? = null,
    onClick: () -> Unit
) {
    val bioColors = MaterialTheme.bioColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(containerColor ?: bioColors.containerGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor ?: bioColors.onContainerGreen,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor ?: bioColors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = bioColors.textSecondary,
                maxLines = 1
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = bioColors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
