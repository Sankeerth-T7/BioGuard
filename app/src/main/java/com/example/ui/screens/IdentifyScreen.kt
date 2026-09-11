package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BioGuardRepository
import com.example.data.api.GeminiService
import com.example.data.model.Observation
import com.example.data.model.SpeciesIdentificationResult
import com.example.ui.components.ConfidenceBadge
import com.example.ui.components.EducationalDisclaimer
import com.example.ui.components.BioScanLoadingView
import com.example.ui.components.CameraCapture
import com.example.ui.theme.*
import com.example.ui.util.SpeciesVisualFactory
import kotlinx.coroutines.launch

@Composable
fun IdentifyScreen(
    repository: BioGuardRepository,
    geminiService: GeminiService,
    onObservationSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var identificationResult by remember { mutableStateOf<SpeciesIdentificationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaved by remember { mutableStateOf(false) }
    var showLiveCamera by remember { mutableStateOf(false) }

    fun startIdentification(targetBitmap: Bitmap? = selectedBitmap, targetUriStr: String? = null) {
        val bitmap = targetBitmap ?: selectedBitmap
        if (bitmap == null) {
            errorMessage = "Please upload or capture a photo first."
            return
        }
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }
        selectedBitmap = safeBitmap

        isAnalyzing = true
        errorMessage = null
        identificationResult = null
        isSaved = false

        coroutineScope.launch {
            try {
                val imageUriStr = targetUriStr ?: selectedImageUri?.toString() ?: "uploaded_image"
                val result = geminiService.identifySpeciesFromImage(safeBitmap, imageUriStr)
                identificationResult = result
            } catch (e: Exception) {
                errorMessage = "Unable to complete identification: ${e.localizedMessage ?: "Unknown error"}. Please try again."
            } finally {
                isAnalyzing = false
            }
        }
    }

    // Gallery Photo Picker (Zero-permission modern Android PhotoPicker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            identificationResult = null
            errorMessage = null
            isSaved = false
            try {
                val loadedBitmap = loadBitmapFromUri(context, uri)
                selectedBitmap = loadedBitmap
                // Immediately initiate species identification upon uploading image
                startIdentification(targetBitmap = loadedBitmap, targetUriStr = uri.toString())
            } catch (e: Exception) {
                errorMessage = "Failed to load selected image: ${e.localizedMessage ?: "unsupported format"}. Please try another image."
            }
        }
    }

    fun loadSamplePreset(presetName: String) {
        val sample = repository.getSpeciesCatalog().find { it.commonName.contains(presetName, ignoreCase = true) }
            ?: repository.getSpeciesCatalog().first()

        val sampleBitmap = SpeciesVisualFactory.getSpeciesBitmap(sample.commonName, width = 800, height = 560)
        selectedBitmap = sampleBitmap
        selectedImageUri = null

        identificationResult = SpeciesIdentificationResult(
            commonName = sample.commonName,
            scientificName = sample.scientificName,
            category = sample.category,
            confidence = 96,
            description = sample.description,
            identifyingFeatures = sample.identifyingFeatures,
            habitat = sample.habitat,
            geographicDistribution = sample.geographicDistribution,
            ecologicalRole = sample.ecologicalRole,
            threats = sample.threats,
            conservationStatus = sample.conservationStatus,
            conservationAdvice = sample.conservationAdvice,
            isLowConfidence = false,
            isStatusVerified = true,
            imageUri = "preset"
        )
        errorMessage = null
        isSaved = false
    }

    if (showLiveCamera) {
        BackHandler { showLiveCamera = false }
        CameraCapture(
            onPhotoCaptured = { bitmap, uri ->
                showLiveCamera = false
                selectedBitmap = bitmap
                selectedImageUri = uri
                identificationResult = null
                errorMessage = null
                isSaved = false
                // Immediately initiate species identification with the captured real-time photo
                startIdentification(targetBitmap = bitmap, targetUriStr = uri.toString())
            },
            onClose = { showLiveCamera = false },
            onPickFromGallery = {
                showLiveCamera = false
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title & Header
        item {
            Column {
                Text(
                    text = "Identify a Species",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = bioColors.textPrimary
                )
                Text(
                    text = "Take a photo or upload an image to discover more about it.",
                    fontSize = 13.sp,
                    color = bioColors.textSecondary,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Upload / Preview Box
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
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedBitmap != null) {
                        // Image Preview Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                        ) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Species photo preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Change/Remove buttons
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { showLiveCamera = true },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.Black.copy(alpha = 0.65f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = "Retake Live Photo", modifier = Modifier.size(18.dp))
                                }

                                FilledTonalIconButton(
                                    onClick = {
                                        selectedBitmap = null
                                        selectedImageUri = null
                                        identificationResult = null
                                        errorMessage = null
                                    },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.Black.copy(alpha = 0.65f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove Photo", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    } else {
                        // Empty Upload Dropzone - Tap to launch live camera!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(bioColors.containerGreen.copy(alpha = 0.45f))
                                .border(
                                    width = 1.5.dp,
                                    color = if (bioColors.isDark) EmeraldAccent.copy(alpha = 0.4f) else ForestGreenPrimary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { showLiveCamera = true }
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(bioColors.containerGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Take a Real-Time Photo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )
                            Text(
                                text = "Tap here to open CameraX Live Scanner",
                                fontSize = 13.sp,
                                color = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "High-resolution real-time species capture",
                                fontSize = 11.sp,
                                color = bioColors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons: Live Camera, Gallery Upload
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showLiveCamera = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Gallery", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Action Button: Analyze Species (Large and Obvious)
                    Button(
                        onClick = { startIdentification() },
                        enabled = selectedBitmap != null && !isAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyze Species", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Take a live photo, select from gallery, or test with sample species below",
                        fontSize = 11.sp,
                        color = bioColors.textMuted
                    )
                }
            }
        }

        // BioScan Loading View when AI analysis is active
        if (isAnalyzing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (bioColors.isDark) MintLight else ForestGreenPrimary,
                                EmeraldAccent
                            )
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 4.dp)
                ) {
                    BioScanLoadingView(previewBitmap = selectedBitmap)
                }
            }
        }

        // Dedicated Visual Sample Species Carousel
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Try Sample Species",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.textPrimary
                        )
                    }
                    Text(
                        text = "Tap to load photo & AI analysis",
                        fontSize = 11.sp,
                        color = bioColors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    val sampleList = listOf(
                        "Tiger", "Peafowl", "Banyan", "Purple Frog", "Turtle", "Lion", "Rhino", "Leopard"
                    )
                    items(sampleList) { sampleKeyword ->
                        val sample = repository.getSpeciesCatalog().find { it.commonName.contains(sampleKeyword, ignoreCase = true) }
                        val name = sample?.commonName ?: sampleKeyword
                        val isSelected = identificationResult?.commonName?.contains(sampleKeyword, ignoreCase = true) == true
                        val thumbBitmap = remember(name) {
                            SpeciesVisualFactory.getSpeciesBitmap(name, width = 280, height = 180)
                        }

                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { loadSamplePreset(sampleKeyword) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) bioColors.containerGreen else bioColors.surfaceCard
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    if (isSelected) listOf(ForestGreenPrimary, EmeraldAccent)
                                    else listOf(bioColors.surfaceBorder, bioColors.surfaceBorder)
                                )
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                        ) {
                            Column {
                                Image(
                                    bitmap = thumbBitmap.asImageBitmap(),
                                    contentDescription = name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bioColors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = sample?.category ?: "Wildlife",
                                        fontSize = 10.sp,
                                        color = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error message if any
        if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bioColors.containerRed),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = bioColors.onContainerRed, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Analysis Encountered an Issue",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = bioColors.onContainerRed
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "BioGuard couldn't identify this image clearly. Try a clearer photo with good lighting and the subject centered.\n\nDetails: $errorMessage",
                            fontSize = 12.sp,
                            color = bioColors.onContainerRed.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { startIdentification() },
                            colors = ButtonDefaults.buttonColors(containerColor = bioColors.onContainerRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Identification Result Display
        if (identificationResult != null) {
            val res = identificationResult!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 3.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (bioColors.isDark) MintLight.copy(alpha = 0.5f) else MintLight,
                                ForestGreenPrimary
                            )
                        )
                    )
                ) {
                    Column {
                        // Hero Species Image Banner if selectedBitmap is available
                        if (selectedBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                            ) {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = res.commonName,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "📸 Field Specimen Verified",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MintLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(18.dp)) {
                        // Header: AI SPECIES IDENTIFICATION
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "AI SPECIES IDENTIFICATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                            )
                            Surface(
                                color = bioColors.containerGreen,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = res.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bioColors.onContainerGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Common & Scientific Name
                        Text(
                            text = res.commonName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = bioColors.textPrimary
                        )
                        Text(
                            text = res.scientificName,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = bioColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Confidence Indicator
                        ConfidenceBadge(confidence = res.confidence)

                        if (res.isLowConfidence) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = bioColors.containerAmber,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "⚠️ Identification uncertain – consider consulting a local field expert.",
                                    fontSize = 11.sp,
                                    color = bioColors.onContainerAmber,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = bioColors.surfaceBorder
                        )

                        // 1. ABOUT
                        ResultSectionTitle("ABOUT", Icons.Default.Info)
                        Text(
                            text = res.description,
                            fontSize = 13.sp,
                            color = bioColors.textPrimary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. IDENTIFYING FEATURES
                        ResultSectionTitle("IDENTIFYING FEATURES", Icons.Default.CheckCircle)
                        res.identifyingFeatures.forEach { feature ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    "• ",
                                    fontWeight = FontWeight.Bold,
                                    color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                                )
                                Text(
                                    text = feature,
                                    fontSize = 12.sp,
                                    color = bioColors.textSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. HABITAT & DISTRIBUTION
                        ResultSectionTitle("HABITAT & DISTRIBUTION", Icons.Default.Park)
                        Text(text = res.habitat, fontSize = 12.sp, color = bioColors.textPrimary)
                        if (res.geographicDistribution.isNotBlank()) {
                            Text(
                                text = "Distribution: ${res.geographicDistribution}",
                                fontSize = 11.sp,
                                color = bioColors.textMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. ECOLOGICAL ROLE
                        ResultSectionTitle("ECOLOGICAL ROLE", Icons.Default.Eco)
                        Text(text = res.ecologicalRole, fontSize = 12.sp, color = bioColors.textPrimary)

                        Spacer(modifier = Modifier.height(12.dp))

                        // 5. THREATS
                        ResultSectionTitle("THREATS", Icons.Default.Warning)
                        Text(text = res.threats, fontSize = 12.sp, color = bioColors.textPrimary)

                        Spacer(modifier = Modifier.height(12.dp))

                        // 6. CONSERVATION STATUS & ADVICE
                        ResultSectionTitle("CONSERVATION STATUS & ADVICE", Icons.Default.Shield)
                        Surface(
                            color = if (res.isStatusVerified) bioColors.containerGreen else bioColors.containerNeutral,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Status: ${res.conservationStatus}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (res.isStatusVerified) bioColors.onContainerGreen else bioColors.onContainerNeutral,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = res.conservationAdvice,
                            fontSize = 12.sp,
                            color = bioColors.textSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Save Observation, Identify Another, Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isSaved) {
                                        coroutineScope.launch {
                                            repository.saveObservation(
                                                Observation(
                                                    commonName = res.commonName,
                                                    scientificName = res.scientificName,
                                                    category = res.category,
                                                    confidence = res.confidence,
                                                    description = res.description,
                                                    identifyingFeatures = res.identifyingFeatures.joinToString(" • "),
                                                    habitat = res.habitat,
                                                    threats = res.threats,
                                                    conservationStatus = res.conservationStatus,
                                                    conservationAdvice = res.conservationAdvice,
                                                    ecologicalRole = res.ecologicalRole,
                                                    imageUri = res.imageUri,
                                                    location = "Field Observation"
                                                )
                                            )
                                            isSaved = true
                                            Toast.makeText(context, "Observation saved! (+50 points)", Toast.LENGTH_SHORT).show()
                                            onObservationSaved()
                                        }
                                    }
                                },
                                enabled = !isSaved,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (isSaved) "Saved" else "Save Observation",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    selectedBitmap = null
                                    selectedImageUri = null
                                    identificationResult = null
                                    isSaved = false
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Identify Another", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Identified ${res.commonName} (${res.scientificName}) using BioGuard AI! Category: ${res.category}."
                                        )
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Identification")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(bioColors.containerGreen)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = bioColors.onContainerGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Subtle visible disclaimer
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = bioColors.textMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI identification is an estimate and may be incorrect.",
                                fontSize = 11.sp,
                                color = bioColors.textMuted
                            )
                        }
                    }
                }
            }
        }
        }

        // Mandatory Disclaimer
        item {
            EducationalDisclaimer(
                text = "IMPORTANT: AI identification is a probabilistic estimate and must not be treated as definitive taxonomic identification. For conservation studies, verify findings with regional biodiversity boards."
            )
        }
    }
}

@Composable
private fun ResultSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val bioColors = MaterialTheme.bioColors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (bioColors.isDark) MintLight else EmeraldAccent,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (bioColors.isDark) MintLight else EmeraldAccent,
            letterSpacing = 1.sp
        )
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val contentResolver = context.contentResolver

    // 1. Determine EXIF orientation (handles rotated photos from Google/camera)
    var orientation = ExifInterface.ORIENTATION_NORMAL
    try {
        contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }
    } catch (_: Exception) {}

    // 2. Decode bitmap safely ensuring software memory allocation
    val rawBitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            // CRITICAL: Force software rendering so pixel operations & compression work
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
            val maxDimension = maxOf(info.size.width, info.size.height)
            if (maxDimension > 1400) {
                decoder.setTargetSampleSize((maxDimension / 1400).coerceAtLeast(1))
            }
        }
    } else {
        // Fallback for older Android API levels
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }
        val maxDimension = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
        var sampleSize = 1
        if (maxDimension > 1400) {
            sampleSize = maxDimension / 1400
        }
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw IllegalStateException("Unable to read image data from selected file.")
    }

    // 3. Ensure strictly software-backed ARGB_8888 bitmap
    val softwareBitmap = if (rawBitmap.config == Bitmap.Config.HARDWARE) {
        rawBitmap.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        rawBitmap
    }

    // 4. Apply EXIF orientation rotation if necessary
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        else -> return softwareBitmap
    }
    return Bitmap.createBitmap(softwareBitmap, 0, 0, softwareBitmap.width, softwareBitmap.height, matrix, true)
}
