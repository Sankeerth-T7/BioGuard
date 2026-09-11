package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BioGuardRepository
import com.example.data.model.QuizQuestion
import com.example.ui.components.BioScreen
import com.example.ui.theme.*

@Composable
fun QuizScreen(
    repository: BioGuardRepository,
    onNavigate: (BioScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val context = LocalContext.current

    var selectedQuestionCount by remember { mutableIntStateOf(10) }
    var isQuizActive by remember { mutableStateOf(false) }
    var isQuizFinished by remember { mutableStateOf(false) }

    var questionsList by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }

    val incorrectCategories = remember { mutableStateListOf<String>() }

    fun startQuiz(count: Int) {
        selectedQuestionCount = count
        questionsList = repository.getQuizQuestions(count)
        currentQuestionIndex = 0
        selectedOptionIndex = null
        score = 0
        incorrectCategories.clear()
        isQuizActive = true
        isQuizFinished = false
    }

    fun handleOptionSelect(index: Int) {
        if (selectedOptionIndex != null) return // Already answered
        selectedOptionIndex = index
        val currentQ = questionsList[currentQuestionIndex]
        if (index == currentQ.correctIndex) {
            score++
        } else {
            if (!incorrectCategories.contains(currentQ.category)) {
                incorrectCategories.add(currentQ.category)
            }
        }
    }

    fun nextQuestion() {
        if (currentQuestionIndex + 1 < questionsList.size) {
            currentQuestionIndex++
            selectedOptionIndex = null
        } else {
            isQuizActive = false
            isQuizFinished = true
            val earnedPoints = score * 20
            repository.addPoints(earnedPoints)
            Toast.makeText(context, "Quiz completed! +$earnedPoints points awarded!", Toast.LENGTH_LONG).show()
        }
    }

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
                    text = "Biodiversity & Conservation Quiz",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = bioColors.textPrimary
                )
                Text(
                    text = "Test your knowledge on biodiversity hotspots, ecological restoration, social forestry, and wildlife sanctuaries.",
                    fontSize = 12.sp,
                    color = bioColors.textSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (!isQuizActive && !isQuizFinished) {
            // QUIZ SETUP CARD
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
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(bioColors.containerGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = bioColors.onContainerGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Ready to Challenge Yourself?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.textPrimary
                        )
                        Text(
                            text = "Answer multiple choice questions and earn +20 points for each correct answer!",
                            fontSize = 12.sp,
                            color = bioColors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Select Quiz Length:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { startQuiz(5) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("5 Questions (Quick)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = { startQuiz(10) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (bioColors.isDark) EmeraldAccent else ForestGreenPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("10 Questions (Full)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // TOPICS COVERED PREVIEW
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TOPICS TESTED IN BIOGUARD QUIZ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bioColors.isDark) MintLight else EmeraldAccent,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val topics = listOf(
                            "Biodiversity & Ecological Economics",
                            "Flora & Keystone Mutualists",
                            "Fauna & Ecosystem Engineers",
                            "Habitat Loss & Primary Threats",
                            "National Parks vs Wildlife Sanctuaries",
                            "Ecological Restoration & Social Forestry",
                            "Indian Western Ghats & Coastal Mangroves"
                        )
                        topics.forEach { topic ->
                            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                Text("🌿 ", fontSize = 12.sp)
                                Text(topic, fontSize = 12.sp, color = bioColors.textSecondary)
                            }
                        }
                    }
                }
            }
        } else if (isQuizActive && questionsList.isNotEmpty()) {
            // ACTIVE QUESTION CARD
            val currentQ = questionsList[currentQuestionIndex]
            val progress = (currentQuestionIndex + 1).toFloat() / questionsList.size

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
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Progress Bar & Header
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
                                    text = currentQ.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bioColors.onContainerGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "Question ${currentQuestionIndex + 1} of ${questionsList.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ForestGreenPrimary,
                            trackColor = bioColors.containerGreen
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Question Text
                        Text(
                            text = currentQ.question,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.textPrimary,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4 Multiple Choice Options
                        currentQ.options.forEachIndexed { index, option ->
                            val isSelected = selectedOptionIndex == index
                            val isAnswered = selectedOptionIndex != null
                            val isCorrectAnswer = index == currentQ.correctIndex

                            val (containerBg, borderColor, textColor) = when {
                                !isAnswered -> Triple(bioColors.containerNeutral, bioColors.surfaceBorder, bioColors.textPrimary)
                                isCorrectAnswer -> Triple(bioColors.containerGreen, ForestGreenPrimary, bioColors.onContainerGreen)
                                isSelected && !isCorrectAnswer -> Triple(bioColors.containerRed, AlertRed, bioColors.onContainerRed)
                                else -> Triple(bioColors.containerNeutral.copy(alpha = 0.5f), bioColors.surfaceBorder, bioColors.textMuted)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable(enabled = !isAnswered) { handleOptionSelect(index) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = containerBg),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isAnswered && isCorrectAnswer) ForestGreenPrimary
                                                else if (isAnswered && isSelected) (if (bioColors.isDark) AlertRedDark else AlertRed)
                                                else bioColors.surfaceBorder
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isAnswered && isCorrectAnswer) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Correct",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else if (isAnswered && isSelected) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Incorrect",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = ('A' + index).toString(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = bioColors.textPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = option,
                                        fontSize = 13.sp,
                                        color = textColor,
                                        fontWeight = if (isSelected || (isAnswered && isCorrectAnswer)) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isAnswered && isCorrectAnswer) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Correct Answer",
                                            tint = ForestGreenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (isAnswered && isSelected) {
                                        Icon(
                                            Icons.Default.Cancel,
                                            contentDescription = "Wrong Answer",
                                            tint = if (bioColors.isDark) AlertRedDark else AlertRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Explanation revealed upon answer
                        if (selectedOptionIndex != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = bioColors.containerGreen,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = bioColors.onContainerGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (selectedOptionIndex == currentQ.correctIndex) "Correct! Explanation:" else "Incorrect. Explanation:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = bioColors.onContainerGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentQ.explanation,
                                        fontSize = 12.sp,
                                        color = bioColors.onContainerGreen.copy(alpha = 0.95f),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { nextQuestion() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (currentQuestionIndex + 1 < questionsList.size) "Next Question →" else "Finish Quiz & View Results",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        } else if (isQuizFinished) {
            // QUIZ COMPLETE RESULTS CARD
            val percentage = (score * 100) / questionsList.size
            val earnedPoints = score * 20

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = if (percentage >= 70) "🎉" else "🌱", fontSize = 48.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Quiz Completed!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = bioColors.textPrimary
                        )

                        Text(
                            text = when {
                                percentage >= 80 -> "Outstanding! You are a true Biodiversity Scholar."
                                percentage >= 50 -> "Well done! Good understanding of conservation biology."
                                else -> "Keep learning! Explore the catalog to strengthen your ecological knowledge."
                            },
                            fontSize = 13.sp,
                            color = bioColors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Circular Score Display
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(bioColors.containerGreen)
                                .border(3.dp, ForestGreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$score / ${questionsList.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = bioColors.onContainerGreen
                                )
                                Text(
                                    text = "Correct",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = bioColors.onContainerGreen.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Points Earned Badge
                        Surface(
                            color = ForestGreenPrimary,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+$earnedPoints Conservation XP",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (incorrectCategories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Recommended Topics for Review:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                incorrectCategories.forEach { cat ->
                                    Surface(
                                        color = bioColors.containerAmber,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = bioColors.onContainerAmber,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { startQuiz(selectedQuestionCount) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Retake Quiz", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onNavigate(BioScreen.EXPLORE) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Explore Catalog", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
