package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class BioScreen(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    IDENTIFY("Identify", Icons.Default.CenterFocusStrong),
    EXPLORE("Explore", Icons.Default.TravelExplore),
    MAP("Map", Icons.Default.Map),
    PROFILE("Profile", Icons.Default.AccountCircle),
    // Secondary screens
    MORE("Menu", Icons.Default.GridView),
    THREAT_REPORT("Report Threat", Icons.Default.ReportProblem),
    OBSERVATIONS("Observations", Icons.Default.CollectionsBookmark),
    QUIZ("Quiz", Icons.Default.Quiz),
    TIPS("Conservation Tips", Icons.Default.TipsAndUpdates),
    ACHIEVEMENTS("Achievements", Icons.Default.EmojiEvents),
    ABOUT("About BioGuard", Icons.Default.Info),
    LOGIN("Login & Sync", Icons.Default.Login)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioGuardTopBar(
    currentScreen: BioScreen,
    userPoints: Int,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenProfile: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors

    TopAppBar(
        title = {
            if (onNavigateBack != null) {
                Text(
                    text = currentScreen.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = bioColors.textPrimary
                )
            } else {
                BioGuardLogo(size = 32, showText = true)
            }
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go Back",
                        tint = if (bioColors.isDark) MintLight else ForestGreenPrimary
                    )
                }
            }
        },
        actions = {
            // Dark / Light Theme Toggle Action
            IconButton(
                onClick = onToggleDarkTheme,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                    tint = if (isDarkTheme) Color(0xFFFDE047) else ForestGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Points Badge
            Surface(
                color = bioColors.containerGreen,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .clickable { onOpenProfile() }
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(text = "⭐", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$userPoints",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = bioColors.onContainerGreen
                    )
                }
            }

            // BioGuard AI Button
            FilledTonalIconButton(
                onClick = onOpenAiAssistant,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (bioColors.isDark) DarkGreenContainer else SoftGreenContainer,
                    contentColor = if (bioColors.isDark) MintLight else ForestGreenPrimary
                ),
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "BioGuard AI Assistant",
                    tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = bioColors.surfaceCard
        ),
        modifier = modifier
    )
}

@Composable
fun BioGuardBottomNav(
    currentScreen: BioScreen,
    onSelectScreen: (BioScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val mainTabs = listOf(
        BioScreen.HOME,
        BioScreen.IDENTIFY,
        BioScreen.EXPLORE,
        BioScreen.MAP,
        BioScreen.PROFILE
    )

    NavigationBar(
        containerColor = bioColors.surfaceCard,
        tonalElevation = if (bioColors.isDark) 0.dp else 4.dp,
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        mainTabs.forEach { screen ->
            val isSelected = currentScreen == screen || (screen == BioScreen.PROFILE && currentScreen in listOf(
                BioScreen.MORE,
                BioScreen.ACHIEVEMENTS,
                BioScreen.ABOUT
            ))

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                    selectedTextColor = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                    indicatorColor = bioColors.containerGreen,
                    unselectedIconColor = bioColors.textMuted,
                    unselectedTextColor = bioColors.textMuted
                )
            )
        }
    }
}
