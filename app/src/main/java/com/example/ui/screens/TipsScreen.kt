package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BioGuardRepository
import com.example.data.api.GeminiService
import com.example.data.model.ConservationTip
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(
    repository: BioGuardRepository,
    geminiService: GeminiService,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf("All") }
    var isGeneratingAiTip by remember { mutableStateOf(false) }

    val categories = listOf("All", "Plants", "Water", "Wildlife", "Campus", "Waste", "Home", "Forests", "Community")
    val allTips = repository.getTips()

    val filteredTips = remember(allTips, selectedCategory) {
        if (selectedCategory == "All") allTips
        else allTips.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val adoptedTips = remember { mutableStateListOf<String>() }

    fun generateAiTip() {
        val cat = if (selectedCategory == "All") "Biodiversity" else selectedCategory
        isGeneratingAiTip = true

        coroutineScope.launch {
            try {
                val newTip = geminiService.generateConservationTip(cat)
                repository.addCustomTip(newTip)
                Toast.makeText(context, "AI Tip generated & added! (+10 points)", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Could not generate AI tip right now", Toast.LENGTH_SHORT).show()
            } finally {
                isGeneratingAiTip = false
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Conservation Action Guide",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = bioColors.textPrimary
                    )
                    Text(
                        text = "Practical everyday interventions for home, campus, and community ecosystems.",
                        fontSize = 12.sp,
                        color = bioColors.textSecondary,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // AI Tip Generation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.containerGreen),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ForestGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MintLight, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Generate Custom AI Tip",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.onContainerGreen
                        )
                        Text(
                            text = "Get an actionable recommendation for ${if (selectedCategory == "All") "any eco-theme" else selectedCategory}.",
                            fontSize = 11.sp,
                            color = bioColors.onContainerGreen.copy(alpha = 0.85f)
                        )
                    }

                    Button(
                        onClick = { generateAiTip() },
                        enabled = !isGeneratingAiTip,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isGeneratingAiTip) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Generate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = bioColors.surfaceCard,
                            labelColor = bioColors.textPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ForestGreenPrimary else bioColors.surfaceBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Tips List
        if (filteredTips.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Lightbulb,
                    title = "No Tips in this Category",
                    message = "Tap 'Generate Custom AI Tip' to formulate new suggestions.",
                    actionButtonText = "Generate Tip",
                    onActionClick = { generateAiTip() }
                )
            }
        } else {
            items(filteredTips) { tip ->
                val isAdopted = adoptedTips.contains(tip.id)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = bioColors.containerGreen,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = tip.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bioColors.onContainerGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                if (tip.isAiGenerated) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = bioColors.containerAmber,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "✨ AI Generated",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = bioColors.onContainerAmber,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    if (!isAdopted) {
                                        adoptedTips.add(tip.id)
                                        repository.addPoints(5)
                                        Toast.makeText(context, "Action adopted! (+5 points)", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (isAdopted) bioColors.containerGreen else bioColors.containerNeutral
                                )
                            ) {
                                Icon(
                                    imageVector = if (isAdopted) Icons.Default.CheckCircle else Icons.Default.AddTask,
                                    contentDescription = "Adopt",
                                    tint = if (isAdopted) (if (bioColors.isDark) MintLight else ForestGreenPrimary) else bioColors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = tip.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = tip.description,
                            fontSize = 12.sp,
                            color = bioColors.textSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = bioColors.containerNeutral,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("🎯 ", fontSize = 12.sp)
                                Column {
                                    Text(
                                        text = "Action Step:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                                    )
                                    Text(
                                        text = tip.actionStep,
                                        fontSize = 12.sp,
                                        color = bioColors.textPrimary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
