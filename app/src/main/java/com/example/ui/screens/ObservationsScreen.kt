package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BioGuardRepository
import com.example.data.model.Observation
import com.example.ui.components.ConfidenceBadge
import com.example.ui.components.DemoModeBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationsScreen(
    repository: BioGuardRepository,
    observations: List<Observation>,
    onNavigateToIdentify: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedObservationForDetail by remember { mutableStateOf<Observation?>(null) }
    var observationToDelete by remember { mutableStateOf<Observation?>(null) }

    val categories = listOf("All", "Plants", "Birds", "Mammals", "Reptiles", "Amphibians", "Insects")

    val filteredObservations = remember(observations, searchQuery, selectedCategory) {
        observations.filter { obs ->
            val matchesCategory = selectedCategory == "All" || obs.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    obs.commonName.contains(searchQuery, ignoreCase = true) ||
                    obs.scientificName.contains(searchQuery, ignoreCase = true) ||
                    obs.location.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Observations",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = bioColors.textPrimary
                    )
                    Text(
                        text = "Your personal biodiversity field journal & logged sightings.",
                        fontSize = 12.sp,
                        color = bioColors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                FilledTonalButton(
                    onClick = onNavigateToIdentify,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = bioColors.containerGreen,
                        contentColor = bioColors.onContainerGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by common name, scientific name, or location...", fontSize = 13.sp, color = bioColors.textMuted) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = if (bioColors.isDark) MintLight else ForestGreenPrimary
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreenPrimary,
                    unfocusedBorderColor = bioColors.surfaceBorder,
                    focusedContainerColor = bioColors.surfaceCard,
                    unfocusedContainerColor = bioColors.surfaceCard,
                    focusedTextColor = bioColors.textPrimary,
                    unfocusedTextColor = bioColors.textPrimary
                ),
                singleLine = true
            )
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

        // Observations List
        if (filteredObservations.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.CollectionsBookmark,
                    title = if (observations.isEmpty()) "Your Field Diary is Empty" else "No Observations Found",
                    message = if (observations.isEmpty()) "Your field diary is empty. Tap the camera to identify your first species!" else "No species matching '$searchQuery'. Try searching by common name or family.",
                    actionButtonText = if (observations.isEmpty()) "Identify First Species" else "Clear Search",
                    onActionClick = {
                        if (observations.isEmpty()) onNavigateToIdentify()
                        else {
                            searchQuery = ""
                            selectedCategory = "All"
                        }
                    }
                )
            }
        } else {
            items(filteredObservations) { obs ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedObservationForDetail = obs },
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
                                        text = obs.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bioColors.onContainerGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                if (obs.isDemo) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    DemoModeBadge()
                                }
                            }

                            IconButton(
                                onClick = { observationToDelete = obs },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = bioColors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = obs.commonName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = bioColors.textPrimary
                        )
                        Text(
                            text = obs.scientificName,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = bioColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ConfidenceBadge(confidence = obs.confidence)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = obs.description,
                            fontSize = 12.sp,
                            color = bioColors.textSecondary,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (bioColors.isDark) MintLight else EmeraldAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = obs.location, fontSize = 11.sp, color = bioColors.textSecondary)
                            }

                            Text(
                                text = dateFormatter.format(Date(obs.createdAt)),
                                fontSize = 11.sp,
                                color = bioColors.textMuted
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    if (selectedObservationForDetail != null) {
        val obs = selectedObservationForDetail!!
        ModalBottomSheet(
            onDismissRequest = { selectedObservationForDetail = null },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = bioColors.surfaceCard
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = obs.commonName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = bioColors.textPrimary
                    )
                    Text(
                        text = obs.scientificName,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = bioColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ConfidenceBadge(confidence = obs.confidence)
                }

                item {
                    HorizontalDivider(color = bioColors.surfaceBorder)
                }

                item {
                    Text(
                        "ABOUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(obs.description, fontSize = 13.sp, color = bioColors.textPrimary)
                }

                if (obs.identifyingFeatures.isNotBlank()) {
                    item {
                        Text(
                            "IDENTIFYING FEATURES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bioColors.isDark) MintLight else EmeraldAccent,
                            letterSpacing = 1.sp
                        )
                        Text(obs.identifyingFeatures, fontSize = 12.sp, color = bioColors.textPrimary)
                    }
                }

                item {
                    Text(
                        "HABITAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(obs.habitat, fontSize = 12.sp, color = bioColors.textPrimary)
                }

                item {
                    Text(
                        "ECOLOGICAL ROLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(obs.ecologicalRole, fontSize = 12.sp, color = bioColors.textPrimary)
                }

                item {
                    Text(
                        "CONSERVATION STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(obs.conservationStatus, fontSize = 12.sp, color = bioColors.textPrimary)
                    Text(obs.conservationAdvice, fontSize = 12.sp, color = bioColors.textSecondary, modifier = Modifier.padding(top = 2.dp))
                }

                item {
                    Button(
                        onClick = { selectedObservationForDetail = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (observationToDelete != null) {
        val obs = observationToDelete!!
        AlertDialog(
            onDismissRequest = { observationToDelete = null },
            containerColor = bioColors.surfaceCard,
            title = {
                Text(
                    "Delete Observation?",
                    color = bioColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove '${obs.commonName}' from your observations?",
                    color = bioColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteObservation(obs)
                            observationToDelete = null
                            Toast.makeText(context, "Observation removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        "Delete",
                        color = if (bioColors.isDark) AlertRedDark else AlertRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { observationToDelete = null }) {
                    Text("Cancel", color = bioColors.textSecondary)
                }
            }
        )
    }
}
