package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.BioGuardRepository
import com.example.data.model.ThreatReport
import com.example.ui.components.EducationalDisclaimer
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatReportScreen(
    repository: BioGuardRepository,
    threatReports: List<ThreatReport>,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Form, 1: History

    data class ThreatTypeOption(
        val title: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val subtitle: String
    )

    val threatTypeOptions = listOf(
        ThreatTypeOption("Deforestation / Tree Cutting", Icons.Default.Park, "Logging, forest canopy loss"),
        ThreatTypeOption("Poaching / Hunting", Icons.Default.Warning, "Traps, snares, wildlife trade"),
        ThreatTypeOption("Pollution", Icons.Default.DeleteOutline, "Plastic, industrial, runoff"),
        ThreatTypeOption("Wildfire", Icons.Default.LocalFireDepartment, "Forest blaze, uncontrolled burn"),
        ThreatTypeOption("Invasive Species", Icons.Default.BugReport, "Displacing native species"),
        ThreatTypeOption("Injured Wildlife", Icons.Default.Healing, "Animal in distress or trapped")
    )

    val severityOptions = listOf(
        Triple("Low", "Monitor", LeafGreen),
        Triple("Medium", "Requires Action", EarthyAmber),
        Triple("High", "Urgent Response", Color(0xFFEA580C)),
        Triple("Critical", "Immediate Danger", AlertRed)
    )

    // Form states
    var selectedType by remember { mutableStateOf(threatTypeOptions[0].title) }
    var locationInput by remember { mutableStateOf("") }
    var coordinatesInput by remember { mutableStateOf("12.9716° N, 77.5946° E") }
    var selectedSeverity by remember { mutableStateOf("Medium") }
    var descriptionInput by remember { mutableStateOf("") }
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submittedReportCode by remember { mutableStateOf<String?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri
            Toast.makeText(context, "Evidence photo attached", Toast.LENGTH_SHORT).show()
        }
    }

    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun submitReport() {
        if (descriptionInput.isBlank()) {
            errorMessage = "Please enter an observed description in Step 4."
            return
        }
        val effectiveLocation = if (locationInput.isNotBlank()) {
            "${locationInput.trim()} ($coordinatesInput)"
        } else {
            coordinatesInput
        }

        errorMessage = null
        isSubmitting = true

        coroutineScope.launch {
            val code = "BG-THR-${Random.nextInt(1000, 9999)}"
            val report = ThreatReport(
                reportCode = code,
                threatType = selectedType,
                title = selectedType,
                description = descriptionInput.trim(),
                location = effectiveLocation,
                dateStr = currentDateStr,
                severity = selectedSeverity,
                imageUri = attachedImageUri?.toString(),
                status = "Logged in BioGuard Eco-Registry"
            )
            repository.saveThreatReport(report)
            isSubmitting = false
            submittedReportCode = code
            showConfirmationDialog = true

            // Reset inputs
            descriptionInput = ""
            locationInput = ""
            attachedImageUri = null
            Toast.makeText(context, "Threat report recorded (+35 points)", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Header
        item {
            Column {
                Text(
                    text = "Report Biodiversity Threat",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = bioColors.textPrimary
                )
                Text(
                    text = "Document habitat destruction, pollution, invasive weeds, or poaching risks for community conservation awareness.",
                    fontSize = 12.sp,
                    color = bioColors.textSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Tab Selector (Submit Form vs History)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = bioColors.surfaceCard,
                contentColor = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, bioColors.surfaceBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "File Threat Report",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 0) (if (bioColors.isDark) MintLight else ForestGreenPrimary) else bioColors.textSecondary
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.AddAlert,
                            contentDescription = null,
                            tint = if (selectedTab == 0) (if (bioColors.isDark) MintLight else ForestGreenPrimary) else bioColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Logged Reports (${threatReports.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 1) (if (bioColors.isDark) MintLight else ForestGreenPrimary) else bioColors.textSecondary
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = if (selectedTab == 1) (if (bioColors.isDark) MintLight else ForestGreenPrimary) else bioColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // SUCCESS CONFIRMATION BANNER
            if (submittedReportCode != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bioColors.containerGreen),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = bioColors.onContainerGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Threat Report Submitted Successfully!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bioColors.onContainerGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Reference Code: $submittedReportCode\nAwarded: +35 Conservation Points.",
                                fontSize = 12.sp,
                                color = bioColors.onContainerGreen.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { submittedReportCode = null }) {
                                Text("Dismiss", fontWeight = FontWeight.Bold, color = bioColors.onContainerGreen)
                            }
                        }
                    }
                }
            }

            // STEP-BY-STEP FORM
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // STEP 1: THREAT TYPE
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = bioColors.containerGreen,
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("1", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = bioColors.onContainerGreen)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STEP 1: THREAT TYPE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                                )
                            }
                            Text(
                                text = "Select the category that best matches your observation:",
                                fontSize = 12.sp,
                                color = bioColors.textSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                threatTypeOptions.chunked(2).forEach { rowOptions ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowOptions.forEach { opt ->
                                            val isSelected = selectedType == opt.title
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedType = opt.title },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) bioColors.containerGreen else bioColors.surfaceCard
                                                ),
                                                border = CardDefaults.outlinedCardBorder().copy(
                                                    brush = Brush.linearGradient(
                                                        listOf(
                                                            if (isSelected) ForestGreenPrimary else bioColors.surfaceBorder,
                                                            if (isSelected) ForestGreenPrimary else bioColors.surfaceBorder
                                                        )
                                                    )
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Icon(
                                                            imageVector = opt.icon,
                                                            contentDescription = null,
                                                            tint = if (isSelected) ForestGreenPrimary else bioColors.textSecondary,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                        if (isSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = null,
                                                                tint = ForestGreenPrimary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = opt.title,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = bioColors.textPrimary,
                                                        lineHeight = 15.sp
                                                    )
                                                    Text(
                                                        text = opt.subtitle,
                                                        fontSize = 10.sp,
                                                        color = bioColors.textMuted,
                                                        lineHeight = 13.sp,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 2: LOCATION
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = bioColors.containerGreen,
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("2", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = bioColors.onContainerGreen)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STEP 2: LOCATION",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                                )
                            }

                            // Use My Current Location button
                            Button(
                                onClick = {
                                    coordinatesInput = "12.9716° N, 77.5946° E"
                                    if (locationInput.isBlank()) {
                                        locationInput = "Field Observation Site"
                                    }
                                    Toast.makeText(context, "Location coordinates locked", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = bioColors.containerGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = bioColors.onContainerGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Use My Current Location", color = bioColors.onContainerGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Lat & Long display
                            OutlinedTextField(
                                value = coordinatesInput,
                                onValueChange = { coordinatesInput = it },
                                label = { Text("Latitude & Longitude (Auto-filled)", color = bioColors.textSecondary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = ForestGreenPrimary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreenPrimary,
                                    unfocusedBorderColor = bioColors.surfaceBorder,
                                    focusedTextColor = bioColors.textPrimary,
                                    unfocusedTextColor = bioColors.textPrimary
                                ),
                                singleLine = true
                            )

                            // Manual landmark / address
                            OutlinedTextField(
                                value = locationInput,
                                onValueChange = { locationInput = it },
                                label = { Text("Landmark / Sanctuary / Address (Optional)", color = bioColors.textSecondary) },
                                placeholder = { Text("e.g. Western Ghats Corridor near River Krishna", color = bioColors.textMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreenPrimary,
                                    unfocusedBorderColor = bioColors.surfaceBorder,
                                    focusedTextColor = bioColors.textPrimary,
                                    unfocusedTextColor = bioColors.textPrimary
                                ),
                                singleLine = true
                            )
                        }
                    }

                    // STEP 3: SEVERITY LEVEL
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = bioColors.containerGreen,
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("3", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = bioColors.onContainerGreen)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STEP 3: SEVERITY LEVEL",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                severityOptions.forEach { (level, actionText, color) ->
                                    val isSelected = selectedSeverity == level
                                    Surface(
                                        color = if (isSelected) color.copy(alpha = 0.15f) else bioColors.containerNeutral,
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                            brush = Brush.linearGradient(listOf(color, color))
                                        ) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedSeverity = level }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = level,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = bioColors.textPrimary
                                                    )
                                                    Text(
                                                        text = actionText,
                                                        fontSize = 11.sp,
                                                        color = if (isSelected) color else bioColors.textMuted
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = color,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 4: EVIDENCE & NOTES
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = bioColors.containerGreen,
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("4", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = bioColors.onContainerGreen)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STEP 4: EVIDENCE & NOTES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                                )
                            }

                            // Evidence button
                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (attachedImageUri != null) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = if (attachedImageUri != null) ForestGreenPrimary else bioColors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (attachedImageUri != null) "Photo Evidence Attached ✓" else "Take Photo or Upload Evidence",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (attachedImageUri != null) ForestGreenPrimary else bioColors.textPrimary
                                )
                            }

                            if (attachedImageUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(attachedImageUri),
                                        contentDescription = "Attached Evidence",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { attachedImageUri = null },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Description field
                            OutlinedTextField(
                                value = descriptionInput,
                                onValueChange = { descriptionInput = it },
                                label = { Text("Detailed Observation Notes", color = bioColors.textSecondary) },
                                placeholder = { Text("Describe what you observed, estimated scale, number of individuals affected...", color = bioColors.textMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreenPrimary,
                                    unfocusedBorderColor = bioColors.surfaceBorder,
                                    focusedTextColor = bioColors.textPrimary,
                                    unfocusedTextColor = bioColors.textPrimary
                                )
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            color = bioColors.containerRed,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                fontSize = 12.sp,
                                color = bioColors.onContainerRed,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // SUBMIT BUTTON (Prominent, full width)
                    Button(
                        onClick = { submitReport() },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submitting Threat Report...", color = Color.White)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Threat Report", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }

            // Mandatory Educational Project Disclaimer
            item {
                EducationalDisclaimer(
                    text = "EDUCATIONAL PROJECT NOTICE: This threat documentation module is constructed for academic demonstration and citizen science learning. It does NOT replace official state forest departments, wildlife emergency hotlines, or law enforcement channels."
                )
            }
        } else {
            // HISTORY TAB
            // HISTORY TAB
            if (threatReports.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.VerifiedUser,
                        title = "No Threats Logged Yet",
                        message = "Help document threats to local biodiversity by filing your first report.",
                        actionButtonText = "File a Report",
                        onActionClick = { selectedTab = 0 }
                    )
                }
            } else {
                items(threatReports) { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
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
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = report.reportCode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bioColors.onContainerGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                SeverityBadge(severity = report.severity)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = report.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )

                            Text(
                                text = "Type: ${report.threatType} • ${report.dateStr}",
                                fontSize = 11.sp,
                                color = bioColors.textSecondary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = report.description,
                                fontSize = 12.sp,
                                color = bioColors.textSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

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
                                    Text(text = report.location, fontSize = 11.sp, color = bioColors.textSecondary)
                                }

                                Surface(
                                    color = bioColors.containerNeutral,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = report.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = bioColors.onContainerNeutral,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmationDialog && submittedReportCode != null) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            containerColor = bioColors.surfaceCard,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(bioColors.containerGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Threat Report Submitted",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = bioColors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = bioColors.containerNeutral,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Report Reference ID", fontSize = 11.sp, color = bioColors.textMuted)
                            Text(
                                text = submittedReportCode ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                            )
                        }
                    }

                    Text("Next Steps:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = bioColors.textPrimary)
                    Text(
                        text = "• Logged into BioGuard Citizen Eco-Registry with timestamp and geospatial coordinates.\n• Telemetry queued for regional conservation rangers and community patrols.\n• Continuous AI threat clustering flag active.",
                        fontSize = 11.sp,
                        color = bioColors.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        selectedTab = 1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("View in Logged Reports", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Done", color = bioColors.textSecondary)
                }
            }
        )
    }
}
