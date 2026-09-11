package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BioGuardRepository
import com.example.data.api.GeminiService
import com.example.ui.components.AiAssistantDialog
import com.example.ui.components.BioGuardBottomNav
import com.example.ui.components.BioGuardTopBar
import com.example.ui.components.BioScreen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.bioColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = BioGuardRepository(applicationContext)
        val geminiService = GeminiService()

        setContent {
            val isSystemDark = isSystemInDarkTheme()
            val darkOverride by repository.darkThemeOverride.collectAsStateWithLifecycle()
            val isDark = darkOverride ?: isSystemDark

            MyApplicationTheme(darkTheme = isDark) {
                BioGuardApp(
                    repository = repository,
                    geminiService = geminiService,
                    isDarkTheme = isDark,
                    onToggleDarkTheme = { repository.toggleDarkTheme(isSystemDark) }
                )
            }
        }
    }
}

@Composable
fun BioGuardApp(
    repository: BioGuardRepository,
    geminiService: GeminiService,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val bioColors = MaterialTheme.bioColors
    val isLoggedIn by repository.isLoggedIn.collectAsStateWithLifecycle()
    val observations by repository.getObservations().collectAsStateWithLifecycle(initialValue = emptyList())
    val threatReports by repository.getThreatReports().collectAsStateWithLifecycle(initialValue = emptyList())
    val userProfile by repository.userProfile.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(BioScreen.HOME) }
    var showAiAssistant by remember { mutableStateOf(false) }

    // If user is not authenticated or not in guest session, show full Login Screen
    if (!isLoggedIn) {
        LoginScreen(
            repository = repository,
            onLoginSuccess = {
                currentScreen = BioScreen.HOME
            },
            onContinueAsGuest = {
                repository.continueAsGuest()
                currentScreen = BioScreen.HOME
            }
        )
        return
    }

    val isSubScreen = currentScreen in listOf(
        BioScreen.THREAT_REPORT,
        BioScreen.OBSERVATIONS,
        BioScreen.QUIZ,
        BioScreen.TIPS,
        BioScreen.ACHIEVEMENTS,
        BioScreen.ABOUT,
        BioScreen.LOGIN
    )

    BackHandler(enabled = isSubScreen || currentScreen != BioScreen.HOME) {
        currentScreen = BioScreen.HOME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BioGuardTopBar(
                currentScreen = currentScreen,
                userPoints = userProfile.points,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                onOpenAiAssistant = { showAiAssistant = true },
                onOpenProfile = { currentScreen = BioScreen.PROFILE },
                onNavigateBack = if (isSubScreen) {
                    { currentScreen = BioScreen.HOME }
                } else null
            )
        },
        bottomBar = {
            BioGuardBottomNav(
                currentScreen = currentScreen,
                onSelectScreen = { screen ->
                    currentScreen = screen
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bioColors.bg)
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                BioScreen.HOME -> {
                    HomeScreen(
                        repository = repository,
                        observations = observations,
                        threatCount = threatReports.size,
                        onNavigate = { currentScreen = it },
                        onOpenAiAssistant = { showAiAssistant = true }
                    )
                }
                BioScreen.IDENTIFY -> {
                    IdentifyScreen(
                        repository = repository,
                        geminiService = geminiService,
                        onObservationSaved = {
                            // Can stay on screen to see saved state
                        }
                    )
                }
                BioScreen.EXPLORE -> {
                    ExploreScreen(
                        repository = repository
                    )
                }
                BioScreen.MAP -> {
                    MapScreen(
                        repository = repository
                    )
                }
                BioScreen.MORE, BioScreen.PROFILE -> {
                    ProfileScreen(
                        repository = repository,
                        onNavigate = { currentScreen = it },
                        onOpenAiAssistant = { showAiAssistant = true },
                        onSignOut = {
                            currentScreen = BioScreen.HOME
                        }
                    )
                }
                BioScreen.THREAT_REPORT -> {
                    ThreatReportScreen(
                        repository = repository,
                        threatReports = threatReports
                    )
                }
                BioScreen.OBSERVATIONS -> {
                    ObservationsScreen(
                        repository = repository,
                        observations = observations,
                        onNavigateToIdentify = { currentScreen = BioScreen.IDENTIFY }
                    )
                }
                BioScreen.QUIZ -> {
                    QuizScreen(
                        repository = repository,
                        onNavigate = { currentScreen = it }
                    )
                }
                BioScreen.TIPS -> {
                    TipsScreen(
                        repository = repository,
                        geminiService = geminiService
                    )
                }
                BioScreen.ACHIEVEMENTS -> {
                    AchievementsScreen(
                        repository = repository
                    )
                }
                BioScreen.ABOUT -> {
                    ProfileScreen(
                        repository = repository,
                        onNavigate = { currentScreen = it },
                        onOpenAiAssistant = { showAiAssistant = true },
                        onSignOut = {
                            currentScreen = BioScreen.HOME
                        }
                    )
                }
                BioScreen.LOGIN -> {
                    LoginScreen(
                        repository = repository,
                        onLoginSuccess = { currentScreen = BioScreen.PROFILE },
                        onContinueAsGuest = {
                            repository.continueAsGuest()
                            currentScreen = BioScreen.PROFILE
                        }
                    )
                }
            }
        }
    }

    if (showAiAssistant) {
        AiAssistantDialog(
            geminiService = geminiService,
            onDismiss = { showAiAssistant = false }
        )
    }
}
