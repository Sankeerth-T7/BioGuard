package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BioGuardRepository
import com.example.data.model.Observation
import com.example.ui.components.BioScreen
import com.example.ui.components.DemoModeBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    repository: BioGuardRepository,
    observations: List<Observation>,
    threatCount: Int,
    onNavigate: (BioScreen) -> Unit,
    onOpenAiAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val userProfile = repository.userProfile.value
    val isDemoMode = repository.isDemoMode.value
    val dailyTip = repository.getTips().firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Section with Main Action: Identify a Species
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.heroGradientStart),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(bioColors.heroGradientStart, bioColors.heroGradientEnd)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "BIOGUARD",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = MintLight
                                )
                                Text(
                                    text = "Discover. Protect. Restore.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            if (isDemoMode) {
                                DemoModeBadge()
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Explore & Protect Biodiversity",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Scan wildlife, learn ecological roles, and safeguard fragile habitats with AI-powered conservation technology.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Prominent Primary Action: Identify a Species
                        Button(
                            onClick = { onNavigate(BioScreen.IDENTIFY) },
                            colors = ButtonDefaults.buttonColors(containerColor = MintLight),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = ForestGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Identify a Species",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = ForestGreenDark
                            )
                        }
                    }
                }
            }
        }

        // 2. Intuitive Quick Actions
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Quick Actions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = bioColors.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionChip(
                            iconText = "📸",
                            title = "Identify",
                            subtitle = "Species",
                            onClick = { onNavigate(BioScreen.IDENTIFY) }
                        )
                    }
                    item {
                        QuickActionChip(
                            iconText = "🌿",
                            title = "Explore",
                            subtitle = "Biodiversity",
                            onClick = { onNavigate(BioScreen.EXPLORE) }
                        )
                    }
                    item {
                        QuickActionChip(
                            iconText = "🗺️",
                            title = "Eco-Map",
                            subtitle = "Sanctuaries",
                            onClick = { onNavigate(BioScreen.MAP) }
                        )
                    }
                    item {
                        QuickActionChip(
                            iconText = "🚨",
                            title = "Report",
                            subtitle = "Threat",
                            onClick = { onNavigate(BioScreen.THREAT_REPORT) }
                        )
                    }
                    item {
                        QuickActionChip(
                            iconText = "🧠",
                            title = "Take",
                            subtitle = "Quiz",
                            onClick = { onNavigate(BioScreen.QUIZ) }
                        )
                    }
                }
            }
        }

        // 3. Personal Statistics Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Personal Statistics",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = bioColors.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Species Identified",
                        value = "${observations.size}",
                        subtitle = "Cataloged",
                        icon = Icons.Default.Eco,
                        iconTint = LeafGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Observations",
                        value = "${observations.size}",
                        subtitle = "Saved",
                        icon = Icons.Default.CollectionsBookmark,
                        iconTint = EmeraldAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Reports Filed",
                        value = "$threatCount",
                        subtitle = "Eco Threats",
                        icon = Icons.Default.ReportProblem,
                        iconTint = if (bioColors.isDark) DarkAmberText else EarthyAmber,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Quiz Points",
                        value = "${userProfile.points}",
                        subtitle = "Level ${userProfile.level}",
                        icon = Icons.Default.EmojiEvents,
                        iconTint = if (bioColors.isDark) MintLight else Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Main Feature Action Cards
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Core Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = bioColors.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                ActionCard(
                    title = "A. AI Species Identification",
                    description = "Take or upload a photo and let Gemini AI analyze taxonomy, habitat, ecological role, and threats.",
                    icon = Icons.Default.PhotoCamera,
                    badgeText = "Gemini Powered",
                    buttonText = "Identify Species",
                    onClick = { onNavigate(BioScreen.IDENTIFY) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionCard(
                    title = "B. Explore Biodiversity",
                    description = "Learn about plants, animals, endemic species, and critical ecosystems of the Indian subcontinent.",
                    icon = Icons.Default.TravelExplore,
                    badgeText = "Catalog & Hotspots",
                    buttonText = "Explore Catalog",
                    onClick = { onNavigate(BioScreen.EXPLORE) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionCard(
                    title = "C. Biodiversity Map",
                    description = "Interactive conservation map marking National Parks, Wildlife Sanctuaries, and Global Hotspots.",
                    icon = Icons.Default.Map,
                    badgeText = "Eco-Sanctuaries",
                    buttonText = "Open Map",
                    onClick = { onNavigate(BioScreen.MAP) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionCard(
                    title = "D. Report a Threat",
                    description = "Help document habitat destruction, deforestation, pollution, invasive weeds, or forest fires.",
                    icon = Icons.Default.ReportProblem,
                    badgeText = "Citizen Action",
                    buttonText = "File Threat Report",
                    onClick = { onNavigate(BioScreen.THREAT_REPORT) }
                )
            }
        }

        // 4. Daily Conservation Tip Card
        if (dailyTip != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Daily Conservation Practice",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = bioColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(BioScreen.TIPS) },
                        shape = RoundedCornerShape(18.dp),
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
                                Surface(
                                    color = bioColors.containerGreen,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = dailyTip.category.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bioColors.onContainerGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = if (bioColors.isDark) DarkAmberText else EarthyAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = dailyTip.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = dailyTip.description,
                                fontSize = 12.sp,
                                color = bioColors.textSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "View All Actions",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Observations Carousel
        if (observations.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recent Discoveries",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.textPrimary
                        )
                        TextButton(onClick = { onNavigate(BioScreen.OBSERVATIONS) }) {
                            Text(
                                "See All (${observations.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(observations.take(6)) { obs ->
                            Card(
                                modifier = Modifier
                                    .width(180.dp)
                                    .clickable { onNavigate(BioScreen.OBSERVATIONS) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Surface(
                                        color = bioColors.containerGreen,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = obs.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = bioColors.onContainerGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = obs.commonName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bioColors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = obs.scientificName,
                                        fontSize = 10.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = bioColors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = obs.location,
                                        fontSize = 10.sp,
                                        color = bioColors.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Educational Quick Link
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenAiAssistant() },
                shape = RoundedCornerShape(16.dp),
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(bioColors.onContainerAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = null,
                            tint = bioColors.onContainerAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Have a Biodiversity Question?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.onContainerAmber
                        )
                        Text(
                            text = "Ask BioGuard AI about ecological restoration, habitat loss, and protected species.",
                            fontSize = 11.sp,
                            color = bioColors.onContainerAmber.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = bioColors.onContainerAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    badgeText: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bioColors.containerGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = bioColors.onContainerGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = bioColors.textPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = bioColors.textSecondary,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = bioColors.containerNeutral,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.onContainerNeutral,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onClick() }
                    ) {
                        Text(
                            text = buttonText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(
    iconText: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = bioColors.surfaceCard,
        border = BorderStroke(1.dp, bioColors.surfaceBorder),
        modifier = modifier.width(112.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = iconText, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = bioColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = bioColors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

