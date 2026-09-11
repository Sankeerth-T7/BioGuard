package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BioGuardRepository
import com.example.data.model.Species
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.ui.util.SpeciesVisualFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    repository: BioGuardRepository,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedSpeciesForDetail by remember { mutableStateOf<Species?>(null) }

    val categories = listOf("All", "Plants", "Birds", "Mammals", "Insects", "Reptiles", "Amphibians", "Ecosystems")
    val allSpecies = repository.getSpeciesCatalog()

    val filteredSpecies = remember(searchQuery, selectedCategory) {
        allSpecies.filter { sp ->
            val matchesCategory = selectedCategory == "All" || sp.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    sp.commonName.contains(searchQuery, ignoreCase = true) ||
                    sp.scientificName.contains(searchQuery, ignoreCase = true) ||
                    sp.habitat.contains(searchQuery, ignoreCase = true) ||
                    sp.ecologicalRole.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Description
        item {
            Column {
                Text(
                    text = "Biodiversity Explorer",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = bioColors.textPrimary
                )
                Text(
                    text = "Discover flora, fauna, keystone species, and fragile ecosystems of the Indian subcontinent.",
                    fontSize = 12.sp,
                    color = bioColors.textSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search species, family, habitat...", fontSize = 13.sp, color = bioColors.textMuted) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (bioColors.isDark) MintLight else ForestGreenPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = bioColors.textMuted)
                        }
                    }
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
                        label = {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
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
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Results count
        item {
            Text(
                text = "Showing ${filteredSpecies.size} species / ecosystems",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = bioColors.textMuted
            )
        }

        // Empty state
        if (filteredSpecies.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.SearchOff,
                    title = "No Species Found",
                    message = if (searchQuery.isNotBlank()) "No species matching '$searchQuery'. Try searching by common name or family." else "Try selecting another category or resetting filters.",
                    actionButtonText = "Reset Filters",
                    onActionClick = {
                        searchQuery = ""
                        selectedCategory = "All"
                    }
                )
            }
        } else {
            items(filteredSpecies) { species ->
                SpeciesCard(
                    species = species,
                    onClick = { selectedSpeciesForDetail = species }
                )
            }
        }
    }

    // Detail Modal Bottom Sheet
    if (selectedSpeciesForDetail != null) {
        val sp = selectedSpeciesForDetail!!
        ModalBottomSheet(
            onDismissRequest = { selectedSpeciesForDetail = null },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = bioColors.surfaceCard
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    val detailBitmap = remember(sp.commonName) {
                        SpeciesVisualFactory.getSpeciesBitmap(sp.commonName, 800, 420)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            bitmap = detailBitmap.asImageBitmap(),
                            contentDescription = sp.commonName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "${sp.emoji} ${sp.category}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sp.commonName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = bioColors.textPrimary
                            )
                            Text(
                                text = sp.scientificName,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                color = bioColors.textSecondary
                            )
                        }

                        val threatColors = getThreatBadgeColors(sp.conservationStatus, bioColors.isDark)
                        Surface(
                            color = threatColors.first,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = sp.conservationStatus.take(24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = threatColors.second,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider(color = bioColors.surfaceBorder)
                }

                // Description
                item {
                    Text(
                        text = "OVERVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(text = sp.description, fontSize = 13.sp, color = bioColors.textPrimary, lineHeight = 18.sp)
                }

                // Habitat & Distribution
                item {
                    Text(
                        text = "HABITAT & REGION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(text = "Habitat: ${sp.habitat}", fontSize = 13.sp, color = bioColors.textPrimary)
                    Text(text = "Distribution: ${sp.geographicDistribution}", fontSize = 12.sp, color = bioColors.textMuted, modifier = Modifier.padding(top = 2.dp))
                }

                // Ecological Role
                item {
                    Text(
                        text = "ECOLOGICAL ROLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(text = sp.ecologicalRole, fontSize = 13.sp, color = bioColors.textPrimary, lineHeight = 17.sp)
                }

                // Economic Value
                item {
                    Text(
                        text = "ECONOMIC & PRACTICAL VALUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(text = sp.economicValue, fontSize = 13.sp, color = bioColors.textPrimary, lineHeight = 17.sp)
                }

                // Threats
                item {
                    Text(
                        text = "THREATS TO POPULATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) DarkAmberText else EarthyAmber,
                        letterSpacing = 1.sp
                    )
                    Text(text = sp.threats, fontSize = 13.sp, color = bioColors.textPrimary, lineHeight = 17.sp)
                }

                // Conservation Status
                item {
                    Text(
                        text = "CONSERVATION STATUS & ADVICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        color = bioColors.containerGreen,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = sp.conservationStatus,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.onContainerGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = sp.conservationAdvice, fontSize = 13.sp, color = bioColors.textSecondary, lineHeight = 17.sp)
                }

                // Close button
                item {
                    Button(
                        onClick = { selectedSpeciesForDetail = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Details", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SpeciesCard(
    species: Species,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val speciesBitmap = remember(species.commonName) {
        SpeciesVisualFactory.getSpeciesBitmap(species.commonName, 600, 260)
    }
    val threatColors = getThreatBadgeColors(species.conservationStatus, bioColors.isDark)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 2.dp)
    ) {
        Column {
            // High Quality Image Placeholder & Badges Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                Image(
                    bitmap = speciesBitmap.asImageBitmap(),
                    contentDescription = species.commonName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )

                // Category badge overlay
                Surface(
                    color = bioColors.containerGreen.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "${species.emoji} ${species.category}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = bioColors.onContainerGreen,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                // Threat level indicator (color-coded)
                Surface(
                    color = threatColors.first.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = species.conservationStatus.take(20),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = threatColors.second,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = species.commonName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = bioColors.textPrimary
                )
                Text(
                    text = species.scientificName,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = bioColors.textSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = species.description,
                    fontSize = 12.sp,
                    color = bioColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = bioColors.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = species.habitat.take(28),
                            fontSize = 11.sp,
                            color = bioColors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Tap for details →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else EmeraldAccent
                    )
                }
            }
        }
    }
}

fun getThreatBadgeColors(status: String, isDark: Boolean): Pair<Color, Color> {
    return when {
        status.contains("Critically", ignoreCase = true) || status.contains("Endangered", ignoreCase = true) ->
            if (isDark) Color(0xFF5C1D1D) to Color(0xFFFF8A80) else Color(0xFFFFEBEE) to Color(0xFFC62828)
        status.contains("Vulnerable", ignoreCase = true) || status.contains("Threatened", ignoreCase = true) ->
            if (isDark) Color(0xFF5C381D) to Color(0xFFFFCC80) else Color(0xFFFFF3E0) to Color(0xFFE65100)
        status.contains("Rare", ignoreCase = true) || status.contains("Protected", ignoreCase = true) ->
            if (isDark) Color(0xFF4A441E) to Color(0xFFFFEE58) else Color(0xFFFFFDE7) to Color(0xFFF57F17)
        else ->
            if (isDark) Color(0xFF1B4332) to Color(0xFF80E27E) else Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    }
}
