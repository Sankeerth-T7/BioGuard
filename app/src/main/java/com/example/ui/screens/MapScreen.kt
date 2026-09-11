package com.example.ui.screens

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.BioGuardRepository
import com.example.data.model.BioLocation
import com.example.ui.theme.*
import com.example.ui.util.IndiaMapGeometry
import kotlinx.coroutines.delay
import kotlin.math.*

enum class TileProviderType(
    val title: String,
    val shortName: String,
    val icon: ImageVector,
    val attribution: String,
    val isSatellite: Boolean = false,
    val isVector: Boolean = false
) {
    SURVEY_OF_INDIA(
        title = "Official Survey of India (SOI) Political Map",
        shortName = "Official SOI Map",
        icon = Icons.Default.Verified,
        attribution = "Official Map of India • Survey of India (SOI) Sovereign Cartography",
        isVector = true
    ),
    ESRI_TOPO(
        title = "Topographic Relief + Sovereign Boundary",
        shortName = "Terrain Topo",
        icon = Icons.Default.Terrain,
        attribution = "Esri, USGS, Survey of India"
    ),
    ESRI_SATELLITE(
        title = "Satellite Earth + Sovereign Boundary",
        shortName = "Satellite",
        icon = Icons.Default.Satellite,
        attribution = "Maxar, Earthstar Geographics, Esri",
        isSatellite = true
    ),
    CARTO_VOYAGER(
        title = "Cartographic Eco-Atlas + Sovereign Boundary",
        shortName = "Eco-Atlas",
        icon = Icons.Default.Map,
        attribution = "© OpenStreetMap contributors, CARTO"
    )
}

data class MapTileCoord(
    val tx: Int,
    val ty: Int,
    val z: Int,
    val originalTx: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    repository: BioGuardRepository,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val allLocations = remember { repository.getLocations() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }
    var selectedRegion by remember { mutableStateOf("All") }
    var selectedLocation by remember { mutableStateOf<BioLocation?>(allLocations.firstOrNull()) }

    // India Constrained Viewport State - Perfectly framed in the card container
    var centerLat by remember { mutableDoubleStateOf(22.2) }
    var centerLon by remember { mutableDoubleStateOf(82.5) }
    var zoom by remember { mutableFloatStateOf(4.5f) }
    var isBoundaryLocked by remember { mutableStateOf(false) }

    // Default Provider is the Official Survey of India sovereign vector map
    var activeProvider by remember { mutableStateOf(TileProviderType.SURVEY_OF_INDIA) }
    var showProviderMenu by remember { mutableStateOf(false) }
    var showBoundaryLine by remember { mutableStateOf(true) }
    var showStateBoundaries by remember { mutableStateOf(true) }
    var showStateCapitals by remember { mutableStateOf(true) }
    var showCardinalExtremes by remember { mutableStateOf(true) }
    var showAstronomicalLines by remember { mutableStateOf(true) }
    var showEcoBiomes by remember { mutableStateOf(true) }
    var showRivers by remember { mutableStateOf(true) }
    var showBufferRings by remember { mutableStateOf(true) }

    // Auto-dismiss boundary locked notification after 2 seconds
    LaunchedEffect(isBoundaryLocked) {
        if (isBoundaryLocked) {
            delay(2000)
            isBoundaryLocked = false
        }
    }

    val filterTypes = listOf("All", "National Park", "Wildlife Sanctuary", "Biosphere Reserve", "Biodiversity Hotspot")
    val filterRegions = listOf("All", "North", "West", "Central", "South", "East & NE")

    val filteredLocations = remember(searchQuery, selectedType, selectedRegion, allLocations) {
        allLocations.filter { loc ->
            val matchesQuery = searchQuery.isBlank() ||
                loc.name.contains(searchQuery, ignoreCase = true) ||
                loc.state.contains(searchQuery, ignoreCase = true) ||
                loc.keySpecies.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesType = selectedType == "All" || loc.type.equals(selectedType, ignoreCase = true)
            val matchesRegion = when (selectedRegion) {
                "North" -> loc.effectiveLat >= 28.0 && loc.effectiveLon < 88.0
                "West" -> loc.effectiveLon < 77.0 && loc.effectiveLat in 18.0..28.0
                "Central" -> loc.effectiveLon in 77.0..84.0 && loc.effectiveLat in 18.0..28.0
                "South" -> loc.effectiveLat < 18.0
                "East & NE" -> loc.effectiveLon >= 84.0
                else -> true
            }
            matchesQuery && matchesType && matchesRegion
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Header with Survey of India sovereign endorsement
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Republic of India",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = bioColors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFFF9933).copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(Color(0xFFFF9933), Color(0xFF138808)))
                                )
                            ) {
                                Text(
                                    text = "🇮🇳 Official SOI Map",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bioColors.isDark) Color(0xFFFFB74D) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Official Survey of India sovereign territory: 28 States, 8 UTs & Ecological Reserves.",
                            fontSize = 12.sp,
                            color = bioColors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Surface(
                        color = bioColors.containerGreen,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = bioColors.onContainerGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${filteredLocations.size} Reserves",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.onContainerGreen
                            )
                        }
                    }
                }
            }
        }

        // Quick Layer & Feature Toggles Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Layer Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(TileProviderType.values()) { provider ->
                        val isSelected = activeProvider == provider
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeProvider = provider },
                            label = {
                                Text(
                                    provider.shortName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    provider.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = bioColors.surfaceCard,
                                labelColor = bioColors.textPrimary,
                                iconColor = bioColors.textSecondary
                            )
                        )
                    }
                }

                // Feature Visibility Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = showStateBoundaries,
                            onClick = { showStateBoundaries = !showStateBoundaries },
                            label = { Text("States & Divisions", fontSize = 10.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = showStateCapitals,
                            onClick = { showStateCapitals = !showStateCapitals },
                            label = { Text("Capitals & Cities", fontSize = 10.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.LocationCity, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = showCardinalExtremes,
                            onClick = { showCardinalExtremes = !showCardinalExtremes },
                            label = { Text("Cardinal Extrema", fontSize = 10.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = showAstronomicalLines,
                            onClick = { showAstronomicalLines = !showAstronomicalLines },
                            label = { Text("Tropic & 82.5°E IST", fontSize = 10.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = showRivers,
                            onClick = { showRivers = !showRivers },
                            label = { Text("Rivers", fontSize = 10.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        )
                    }
                }
            }
        }

        // Search and Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search reserve, state, or species (e.g., Corbett, Kaziranga)...",
                            fontSize = 12.sp,
                            color = bioColors.textMuted
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = bioColors.textMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = bioColors.textMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = bioColors.surfaceCard,
                        unfocusedContainerColor = bioColors.surfaceCard,
                        focusedBorderColor = ForestGreenPrimary,
                        unfocusedBorderColor = bioColors.surfaceBorder,
                        focusedTextColor = bioColors.textPrimary,
                        unfocusedTextColor = bioColors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterTypes) { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    type,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = bioColors.containerGreen,
                                selectedLabelColor = bioColors.onContainerGreen,
                                containerColor = bioColors.surfaceCard,
                                labelColor = bioColors.textSecondary
                            )
                        )
                    }
                }
            }
        }

        // Interactive High-Accuracy Official Map Container
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(490.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 4.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(
                            if (bioColors.isDark) MintLight.copy(alpha = 0.5f) else Color(0xFFFF9933).copy(alpha = 0.6f),
                            ForestGreenPrimary
                        )
                    )
                )
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val density = LocalDensity.current
                    val viewWidth = constraints.maxWidth.toFloat()
                    val viewHeight = constraints.maxHeight.toFloat()

                    // Web Mercator Tile Math (used when a raster tile provider is active)
                    val intZoom = zoom.toInt().coerceIn(3, 12)
                    val fractionalZoom = zoom - intZoom
                    val tileDisplaySizePx = 256f * 2.0.pow(fractionalZoom.toDouble()).toFloat()
                    val numTiles = 1 shl intZoom

                    val centerTileX = IndiaMapGeometry.lonToMercatorX(centerLon) * numTiles
                    val centerTileY = IndiaMapGeometry.latToMercatorY(centerLat) * numTiles

                    val startTileX = floor(centerTileX - (viewWidth / (2f * tileDisplaySizePx))).toInt() - 1
                    val endTileX = ceil(centerTileX + (viewWidth / (2f * tileDisplaySizePx))).toInt() + 1
                    val startTileY = floor(centerTileY - (viewHeight / (2f * tileDisplaySizePx))).toInt() - 1
                    val endTileY = ceil(centerTileY + (viewHeight / (2f * tileDisplaySizePx))).toInt() + 1

                    val visibleTiles = remember(intZoom, startTileX, endTileX, startTileY, endTileY) {
                        val list = mutableListOf<MapTileCoord>()
                        val maxTile = (1 shl intZoom) - 1
                        for (ty in startTileY..endTileY) {
                            if (ty in 0..maxTile) {
                                for (tx in startTileX..endTileX) {
                                    val wrappedTx = (tx % (1 shl intZoom) + (1 shl intZoom)) % (1 shl intZoom)
                                    list.add(MapTileCoord(tx = wrappedTx, ty = ty, z = intZoom, originalTx = tx))
                                }
                            }
                        }
                        list
                    }

                    // 1. Base Canvas Background (Water / Ocean or Satellite tint)
                    val oceanBaseColor = when {
                        activeProvider == TileProviderType.ESRI_SATELLITE -> Color(0xFF0C1910)
                        activeProvider.isVector -> if (bioColors.isDark) Color(0xFF091724) else Color(0xFFE2F0F9)
                        bioColors.isDark -> Color(0xFF132A1C)
                        else -> Color(0xFFE5F2E7)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(oceanBaseColor)
                    )

                    // 2. High-Accuracy Terrain Raster Tiles (Loaded ONLY if not using pure Vector SOI mode)
                    if (!activeProvider.isVector) {
                        val tileDp = with(density) { (tileDisplaySizePx + 0.6f).toDp() }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                        ) {
                            visibleTiles.forEach { tile ->
                                val leftPx = (viewWidth / 2f + (tile.originalTx - centerTileX) * tileDisplaySizePx).toFloat()
                                val topPx = (viewHeight / 2f + (tile.ty - centerTileY) * tileDisplaySizePx).toFloat()

                                val tileUrl = when (activeProvider) {
                                    TileProviderType.ESRI_TOPO ->
                                        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/${tile.z}/${tile.ty}/${tile.tx}.jpg"
                                    TileProviderType.ESRI_SATELLITE ->
                                        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/${tile.z}/${tile.ty}/${tile.tx}.jpg"
                                    TileProviderType.CARTO_VOYAGER ->
                                        "https://basemaps.cartocdn.com/rastertiles/voyager/${tile.z}/${tile.tx}/${tile.ty}@2x.png"
                                    else -> ""
                                }

                                if (tileUrl.isNotBlank()) {
                                    key("${tile.z}-${tile.tx}-${tile.ty}-${activeProvider.name}") {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(tileUrl)
                                                .crossfade(150)
                                                .memoryCachePolicy(CachePolicy.ENABLED)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier
                                                .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                                                .size(tileDp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Official Cartographic Vector Elements & Protected Area Pins
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    val newZoom = (zoom * gestureZoom).coerceIn(IndiaMapGeometry.MIN_ZOOM, IndiaMapGeometry.MAX_ZOOM)
                                    zoom = newZoom

                                    val scale = 256.0 * 2.0.pow(newZoom.toDouble())
                                    val deltaLon = -(pan.x.toDouble() / scale) * 360.0

                                    val currentY = IndiaMapGeometry.latToMercatorY(centerLat)
                                    val newY = currentY - (pan.y.toDouble() / scale)
                                    val newLat = IndiaMapGeometry.mercatorYToLat(newY)

                                    val clampedLon = (centerLon + deltaLon).coerceIn(IndiaMapGeometry.INDIA_MIN_LON, IndiaMapGeometry.INDIA_MAX_LON)
                                    val clampedLat = newLat.coerceIn(IndiaMapGeometry.INDIA_MIN_LAT, IndiaMapGeometry.INDIA_MAX_LAT)

                                    if (clampedLon != (centerLon + deltaLon) || clampedLat != newLat) {
                                        isBoundaryLocked = true
                                    }

                                    centerLon = clampedLon
                                    centerLat = clampedLat
                                }
                            }
                            .pointerInput(filteredLocations, zoom, centerLat, centerLon) {
                                detectTapGestures { tapOffset ->
                                    val thresholdPx = 36 * density.density
                                    val closest = filteredLocations.minByOrNull { loc ->
                                        val pt = IndiaMapGeometry.geoToMercatorPixel(
                                            loc.effectiveLat, loc.effectiveLon,
                                            centerLat, centerLon, zoom, viewWidth, viewHeight
                                        )
                                        val dx = pt.x - tapOffset.x
                                        val dy = pt.y - tapOffset.y
                                        dx * dx + dy * dy
                                    }
                                    if (closest != null) {
                                        val pt = IndiaMapGeometry.geoToMercatorPixel(
                                            closest.effectiveLat, closest.effectiveLon,
                                            centerLat, centerLon, zoom, viewWidth, viewHeight
                                        )
                                        val dist = hypot((pt.x - tapOffset.x).toDouble(), (pt.y - tapOffset.y).toDouble())
                                        if (dist <= thresholdPx) {
                                            selectedLocation = closest
                                        }
                                    }
                                }
                            }
                    ) {
                        // A. Vector Sovereign Landmass Rendering (for Official Survey of India Mode)
                        if (activeProvider.isVector) {
                            val sovereignLandColor = if (bioColors.isDark) Color(0xFF142B20) else Color(0xFFFCFDF9)
                            val mainlandPath = IndiaMapGeometry.getIndiaBoundaryMercatorPath(
                                centerLat, centerLon, zoom, viewWidth, viewHeight
                            )
                            // Draw sovereign mainland fill
                            drawPath(path = mainlandPath, color = sovereignLandColor)

                            // Draw Andaman & Nicobar islands fill
                            val andamanIslands = IndiaMapGeometry.getAndamanNicobarMercatorIslands(
                                centerLat, centerLon, zoom, viewWidth, viewHeight
                            )
                            andamanIslands.forEach {
                                drawPath(path = it, color = sovereignLandColor)
                            }

                            // Draw Lakshadweep Atolls fill
                            val lakshadweepIslands = IndiaMapGeometry.getLakshadweepMercatorIslands(
                                centerLat, centerLon, zoom, viewWidth, viewHeight
                            )
                            lakshadweepIslands.forEach { pt ->
                                drawCircle(color = sovereignLandColor, radius = 5.0f * (zoom / 4.6f).coerceIn(1f, 2.5f), center = pt)
                                drawCircle(color = Color(0xFFFF9933), radius = 6.0f * (zoom / 4.6f).coerceIn(1f, 2.5f), center = pt, style = Stroke(width = 1.2f))
                            }

                            // Water body labels in italic typography
                            val waterPaint = Paint().apply {
                                isAntiAlias = true
                                textSize = 20f * (zoom / 4.6f).coerceIn(0.85f, 1.6f)
                                color = if (bioColors.isDark) android.graphics.Color.argb(130, 140, 190, 230) else android.graphics.Color.argb(140, 40, 110, 170)
                                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                                textAlign = Paint.Align.CENTER
                            }

                            val arabianPt = IndiaMapGeometry.geoToMercatorPixel(15.0, 70.0, centerLat, centerLon, zoom, viewWidth, viewHeight)
                            drawContext.canvas.nativeCanvas.drawText("ARABIAN SEA", arabianPt.x, arabianPt.y, waterPaint)

                            val bayPt = IndiaMapGeometry.geoToMercatorPixel(15.0, 89.0, centerLat, centerLon, zoom, viewWidth, viewHeight)
                            drawContext.canvas.nativeCanvas.drawText("BAY OF BENGAL", bayPt.x, bayPt.y, waterPaint)

                            val oceanPt = IndiaMapGeometry.geoToMercatorPixel(5.5, 78.5, centerLat, centerLon, zoom, viewWidth, viewHeight)
                            drawContext.canvas.nativeCanvas.drawText("INDIAN OCEAN", oceanPt.x, oceanPt.y, waterPaint)
                        }

                        // B. Eco-Biomes Shading (Western Ghats & Himalayan Snow Zone)
                        if (showEcoBiomes) {
                            val snowPath = IndiaMapGeometry.getHimalayanSnowMercatorZone(centerLat, centerLon, zoom, viewWidth, viewHeight)
                            drawPath(path = snowPath, color = Color.White.copy(alpha = if (activeProvider.isVector) 0.35f else 0.22f))

                            val ghatsPath = IndiaMapGeometry.getWesternGhatsMercatorRibbon(centerLat, centerLon, zoom, viewWidth, viewHeight)
                            drawPath(path = ghatsPath, color = EmeraldAccent.copy(alpha = if (activeProvider.isVector) 0.35f else 0.28f))
                        }

                        // C. Major Ecological Lifeline Rivers
                        if (showRivers) {
                            val rivers = IndiaMapGeometry.getMajorRiversMercator(centerLat, centerLon, zoom, viewWidth, viewHeight)
                            val riverColor = if (activeProvider == TileProviderType.ESRI_SATELLITE) {
                                Color(0xFF67E8F9).copy(alpha = 0.85f)
                            } else {
                                Color(0xFF0284C7).copy(alpha = 0.75f)
                            }
                            rivers.forEach { (_, rPath) ->
                                drawPath(path = rPath, color = riverColor, style = Stroke(width = 2.4f))
                            }
                        }

                        // D. Internal State Demarcation Boundaries
                        if (showStateBoundaries) {
                            val stateLines = IndiaMapGeometry.getStateDemarcationsMercator(centerLat, centerLon, zoom, viewWidth, viewHeight)
                            val stateBoundaryColor = if (bioColors.isDark) Color(0xFF94A3B8).copy(alpha = 0.5f) else Color(0xFF64748B).copy(alpha = 0.55f)
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                            stateLines.forEach { sPath ->
                                drawPath(path = sPath, color = stateBoundaryColor, style = Stroke(width = 1.4f, pathEffect = dashEffect))
                            }
                        }

                        // E. Tropic of Cancer & Indian Standard Meridian (82.5° E IST)
                        if (showAstronomicalLines) {
                            val cancerDash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            val tropicPath = IndiaMapGeometry.getTropicOfCancerMercator(centerLat, centerLon, zoom, viewWidth, viewHeight)
                            drawPath(path = tropicPath, color = Color(0xFFD97706), style = Stroke(width = 1.8f, pathEffect = cancerDash))

                            val meridianPath = IndiaMapGeometry.getStandardMeridianMercator(centerLat, centerLon, zoom, viewWidth, viewHeight)
                            drawPath(path = meridianPath, color = Color(0xFF0284C7).copy(alpha = 0.7f), style = Stroke(width = 1.6f, pathEffect = cancerDash))

                            if (zoom >= 4.4f) {
                                val astroPaint = Paint().apply {
                                    isAntiAlias = true
                                    textSize = 18f
                                    color = android.graphics.Color.argb(210, 217, 119, 6)
                                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                }
                                val labelPt = IndiaMapGeometry.geoToMercatorPixel(23.43, 75.0, centerLat, centerLon, zoom, viewWidth, viewHeight)
                                drawContext.canvas.nativeCanvas.drawText("Tropic of Cancer (23.5° N)", labelPt.x, labelPt.y - 6f, astroPaint)

                                val istPaint = Paint().apply {
                                    isAntiAlias = true
                                    textSize = 18f
                                    color = android.graphics.Color.argb(200, 2, 132, 199)
                                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                }
                                val istPt = IndiaMapGeometry.geoToMercatorPixel(25.5, 82.5, centerLat, centerLon, zoom, viewWidth, viewHeight)
                                drawContext.canvas.nativeCanvas.drawText("82.5° E (IST)", istPt.x + 8f, istPt.y, istPaint)
                            }
                        }

                        // F. Sovereign Outer Boundary of the Republic of India (Survey of India Compliant)
                        if (showBoundaryLine) {
                            val boundaryPath = IndiaMapGeometry.getIndiaBoundaryMercatorPath(
                                centerLat, centerLon, zoom, viewWidth, viewHeight
                            )
                            // Outer patriotic Saffron casing for high-contrast visibility
                            val casingColor = if (bioColors.isDark) Color(0xFFFF9933) else Color(0xFFE65100)
                            // Inner Forest Green core
                            val coreColor = if (bioColors.isDark) MintLight else Color(0xFF138808)

                            drawPath(path = boundaryPath, color = casingColor, style = Stroke(width = 4.4f))
                            drawPath(path = boundaryPath, color = coreColor, style = Stroke(width = 2.2f))

                            // Andaman & Nicobar Outer Sovereign Strokes
                            val andamanIslands = IndiaMapGeometry.getAndamanNicobarMercatorIslands(
                                centerLat, centerLon, zoom, viewWidth, viewHeight
                            )
                            andamanIslands.forEach {
                                drawPath(path = it, color = casingColor, style = Stroke(width = 3.6f))
                                drawPath(path = it, color = coreColor, style = Stroke(width = 1.8f))
                            }
                        }

                        // G. State & Union Territory Centroid Labels
                        if (showStateBoundaries && zoom >= 4.4f) {
                            val statePaint = Paint().apply {
                                isAntiAlias = true
                                textSize = (18f * (zoom / 4.6f).coerceIn(0.9f, 1.4f))
                                color = if (bioColors.isDark) android.graphics.Color.argb(160, 220, 240, 225) else android.graphics.Color.argb(170, 40, 70, 50)
                                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                            }
                            IndiaMapGeometry.getStateLabels().forEach { state ->
                                val pt = IndiaMapGeometry.geoToMercatorPixel(state.lat, state.lon, centerLat, centerLon, zoom, viewWidth, viewHeight)
                                if (pt.x in 0f..viewWidth && pt.y in 0f..viewHeight) {
                                    val textWidth = statePaint.measureText(state.name)
                                    drawContext.canvas.nativeCanvas.drawText(state.name, pt.x - textWidth / 2, pt.y, statePaint)
                                }
                            }
                        }

                        // H. State Capitals & National Capital Territory (New Delhi)
                        if (showStateCapitals && zoom >= 4.8f) {
                            val capitalPaint = Paint().apply {
                                isAntiAlias = true
                                textSize = 17f
                                color = if (bioColors.isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                            }
                            val capitalBoldPaint = Paint().apply {
                                isAntiAlias = true
                                textSize = 20f
                                color = if (bioColors.isDark) android.graphics.Color.argb(255, 255, 183, 77) else android.graphics.Color.argb(255, 198, 40, 40)
                                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                            }

                            IndiaMapGeometry.getStateCapitals().forEach { cap ->
                                val pt = IndiaMapGeometry.geoToMercatorPixel(cap.lat, cap.lon, centerLat, centerLon, zoom, viewWidth, viewHeight)
                                if (pt.x in -20f..(viewWidth + 20f) && pt.y in -20f..(viewHeight + 20f)) {
                                    if (cap.isNationalCapital) {
                                        // National Capital: Star symbol & prominent badge
                                        drawCircle(color = Color(0xFFFF9933), radius = 9f, center = pt)
                                        drawCircle(color = Color.White, radius = 5f, center = pt)
                                        drawContext.canvas.nativeCanvas.drawText("★ ${cap.name} (National Capital)", pt.x + 12f, pt.y + 6f, capitalBoldPaint)
                                    } else {
                                        // State Capital
                                        drawCircle(color = if (bioColors.isDark) Color.White else Color(0xFF1E293B), radius = 5f, center = pt)
                                        drawCircle(color = ForestGreenPrimary, radius = 3f, center = pt)
                                        if (zoom >= 5.6f) {
                                            drawContext.canvas.nativeCanvas.drawText(cap.name, pt.x + 8f, pt.y + 4f, capitalPaint)
                                        }
                                    }
                                }
                            }
                        }

                        // I. Cardinal Geographic Extrema of the Republic of India
                        if (showCardinalExtremes) {
                            val extremaPaint = Paint().apply {
                                isAntiAlias = true
                                textSize = 18f
                                color = if (bioColors.isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                            }
                            val extremaBgPaint = Paint().apply {
                                isAntiAlias = true
                                color = if (bioColors.isDark) android.graphics.Color.argb(230, 20, 40, 30) else android.graphics.Color.argb(230, 255, 255, 255)
                            }

                            IndiaMapGeometry.getCardinalExtremes().forEach { ext ->
                                val pt = IndiaMapGeometry.geoToMercatorPixel(ext.lat, ext.lon, centerLat, centerLon, zoom, viewWidth, viewHeight)
                                val px = pt.x
                                val py = pt.y

                                if (px in -60f..(viewWidth + 60f) && py in -60f..(viewHeight + 60f)) {
                                    // Diamond Pin
                                    val diamondPath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(px, py - 9f)
                                        lineTo(px + 9f, py)
                                        lineTo(px, py + 9f)
                                        lineTo(px - 9f, py)
                                        close()
                                    }
                                    drawPath(diamondPath, Color(0xFFFF9933))
                                    drawPath(diamondPath, Color.White, style = Stroke(width = 1.5f))

                                    // Cardinal Label Pill
                                    val labelText = "${ext.direction}: ${ext.title}"
                                    val textWidth = extremaPaint.measureText(labelText)
                                    val pillRect = RectF(
                                        px - textWidth / 2 - 6f,
                                        py + 12f,
                                        px + textWidth / 2 + 6f,
                                        py + 32f
                                    )
                                    drawContext.canvas.nativeCanvas.drawRoundRect(pillRect, 6f, 6f, extremaBgPaint)
                                    drawContext.canvas.nativeCanvas.drawText(labelText, px - textWidth / 2, py + 26f, extremaPaint)
                                }
                            }
                        }

                        // J. Detailed Protected Area Markers (Sanctuaries & National Parks)
                        filteredLocations.forEach { loc ->
                            val pt = IndiaMapGeometry.geoToMercatorPixel(
                                loc.effectiveLat, loc.effectiveLon,
                                centerLat, centerLon, zoom, viewWidth, viewHeight
                            )
                            val px = pt.x
                            val py = pt.y

                            if (px in -60f..(viewWidth + 60f) && py in -60f..(viewHeight + 60f)) {
                                val isSelected = loc.id == selectedLocation?.id
                                val pinColor = when (loc.type) {
                                    "National Park" -> if (bioColors.isDark) MintLight else ForestGreenPrimary
                                    "Wildlife Sanctuary" -> EmeraldAccent
                                    "Biodiversity Hotspot" -> if (bioColors.isDark) AlertRedDark else AlertRed
                                    else -> EarthyAmber
                                }

                                if (showBufferRings || isSelected) {
                                    val bufferRadius = (22f * (zoom / 4.6f).coerceIn(1f, 3.5f))
                                    drawCircle(
                                        color = pinColor.copy(alpha = if (isSelected) 0.32f else 0.12f),
                                        radius = bufferRadius,
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = pinColor.copy(alpha = if (isSelected) 0.65f else 0.28f),
                                        radius = bufferRadius,
                                        center = Offset(px, py),
                                        style = Stroke(width = 1.3f)
                                    )
                                }

                                if (isSelected) {
                                    drawCircle(
                                        color = pinColor.copy(alpha = 0.35f),
                                        radius = 28f,
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.6f),
                                        radius = 36f,
                                        center = Offset(px, py),
                                        style = Stroke(width = 2.2f)
                                    )
                                }

                                drawCircle(
                                    color = if (bioColors.isDark) Color(0xFF0B1713) else Color.White,
                                    radius = if (isSelected) 14f else 9f,
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = pinColor,
                                    radius = if (isSelected) 9.5f else 6f,
                                    center = Offset(px, py)
                                )

                                if (isSelected || (zoom >= 6.2f && filteredLocations.size <= 18)) {
                                    val labelPaint = Paint().apply {
                                        isAntiAlias = true
                                        textSize = if (isSelected) 24f else 18f
                                        color = if (bioColors.isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                                        typeface = Typeface.create(
                                            Typeface.DEFAULT,
                                            if (isSelected) Typeface.BOLD else Typeface.NORMAL
                                        )
                                    }
                                    val shortName = loc.name
                                        .replace(" National Park", " NP")
                                        .replace(" Biosphere Reserve", " BR")
                                        .replace(" Wildlife Sanctuary", " WS")
                                        .replace(" & Sanctuary", "")
                                        .replace(" Tiger Reserve", " TR")
                                        .take(18)

                                    val textWidth = labelPaint.measureText(shortName)
                                    val bgRect = RectF(
                                        px - textWidth / 2 - 8f,
                                        py + 14f,
                                        px + textWidth / 2 + 8f,
                                        py + 36f
                                    )
                                    val pillPaint = Paint().apply {
                                        isAntiAlias = true
                                        color = if (isSelected) {
                                            if (bioColors.isDark) android.graphics.Color.argb(235, 15, 45, 30)
                                            else android.graphics.Color.argb(245, 255, 255, 255)
                                        } else {
                                            if (bioColors.isDark) android.graphics.Color.argb(190, 10, 30, 20)
                                            else android.graphics.Color.argb(200, 240, 250, 242)
                                        }
                                    }
                                    drawContext.canvas.nativeCanvas.drawRoundRect(bgRect, 8f, 8f, pillPaint)
                                    drawContext.canvas.nativeCanvas.drawText(shortName, px - textWidth / 2, py + 30f, labelPaint)
                                }
                            }
                        }
                    }

                    // Top-Left Floating Controls: Tile Provider Switcher & Boundary Pill
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box {
                            Surface(
                                color = bioColors.surfaceCard.copy(alpha = 0.94f),
                                shape = RoundedCornerShape(12.dp),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                                ),
                                modifier = Modifier.clickable { showProviderMenu = !showProviderMenu }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = activeProvider.icon,
                                        contentDescription = null,
                                        tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = activeProvider.shortName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bioColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Switch Provider",
                                        tint = bioColors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showProviderMenu,
                                onDismissRequest = { showProviderMenu = false },
                                modifier = Modifier.background(bioColors.surfaceCard)
                            ) {
                                TileProviderType.values().forEach { provider ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    provider.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (activeProvider == provider) FontWeight.Bold else FontWeight.Medium,
                                                    color = bioColors.textPrimary
                                                )
                                                Text(
                                                    provider.attribution,
                                                    fontSize = 10.sp,
                                                    color = bioColors.textSecondary
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                provider.icon,
                                                contentDescription = null,
                                                tint = if (activeProvider == provider) ForestGreenPrimary else bioColors.textSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            activeProvider = provider
                                            showProviderMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // India Viewport Boundary Locked Notification Pill
                        AnimatedVisibility(
                            visible = isBoundaryLocked,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Surface(
                                color = EarthyAmber.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "India Frontier Locked",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Top-Right Floating Zoom & Map Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Zoom In
                        FilledTonalIconButton(
                            onClick = {
                                zoom = (zoom + 0.45f).coerceAtMost(IndiaMapGeometry.MAX_ZOOM)
                            },
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = bioColors.surfaceCard.copy(alpha = 0.95f),
                                contentColor = bioColors.textPrimary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
                        }

                        // Zoom Out
                        FilledTonalIconButton(
                            onClick = {
                                zoom = (zoom - 0.45f).coerceAtLeast(IndiaMapGeometry.MIN_ZOOM)
                            },
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = bioColors.surfaceCard.copy(alpha = 0.95f),
                                contentColor = bioColors.textPrimary
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
                        }

                        // Recenter India (Balanced Sovereign Viewport: 22.2° N, 82.5° E, Zoom 4.5f)
                        FilledTonalIconButton(
                            onClick = {
                                centerLat = 22.2
                                centerLon = 82.5
                                zoom = 4.5f
                            },
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = bioColors.surfaceCard.copy(alpha = 0.95f),
                                contentColor = if (bioColors.isDark) MintLight else ForestGreenPrimary
                            )
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Recenter India", modifier = Modifier.size(18.dp))
                        }

                        // Toggle Buffer Rings / Eco-layers
                        FilledTonalIconButton(
                            onClick = {
                                showBufferRings = !showBufferRings
                            },
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (showBufferRings) ForestGreenPrimary else bioColors.surfaceCard.copy(alpha = 0.95f),
                                contentColor = if (showBufferRings) Color.White else bioColors.textPrimary
                            )
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = "Toggle Eco-Layers", modifier = Modifier.size(18.dp))
                        }
                    }

                    // Bottom-Left Coordinates & Geodetic Scale
                    Surface(
                        color = bioColors.surfaceCard.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        val approxKm = (550.0 / 2.0.pow((zoom - 4.6).toDouble())).roundToInt().coerceAtLeast(25)
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(2.5.dp)
                                    .background(if (bioColors.isDark) MintLight else ForestGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "≈ $approxKm km • ${"%.2f".format(centerLat)}° N, ${"%.2f".format(centerLon)}° E",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )
                        }
                    }

                    // Bottom-Right Attribution Banner
                    Surface(
                        color = bioColors.surfaceCard.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = activeProvider.attribution,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            color = bioColors.textMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Survey of India Sovereign Boundary Highlights & Camera Shortcuts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFFF9933).copy(alpha = 0.4f),
                            Color(0xFF138808).copy(alpha = 0.4f)
                        )
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (bioColors.isDark) Color(0xFFFFB74D) else Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Survey of India (SOI) Sovereign Cartography",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "Certified original territorial representation including the northern crown of Jammu & Kashmir and Ladakh (Indira Col, Siachen, Karakoram, Aksai Chin), northeastern Arunachal Pradesh (Kibithu), western Gujarat (Ghuar Mota), southern Kanyakumari & Indira Point.",
                        fontSize = 11.sp,
                        color = bioColors.textSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "JUMP TO CARDINAL POINTS & CAPITALS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            SuggestionChip(
                                onClick = {
                                    centerLat = 36.80
                                    centerLon = 76.50
                                    zoom = 6.2f
                                },
                                label = { Text("▲ North: Ladakh / Indira Col", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.North, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = {
                                    centerLat = 28.61
                                    centerLon = 77.21
                                    zoom = 6.8f
                                },
                                label = { Text("★ New Delhi (Capital)", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = {
                                    centerLat = 28.01
                                    centerLon = 96.50
                                    zoom = 6.4f
                                },
                                label = { Text("► East: Kibithu, Arunachal", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.East, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = {
                                    centerLat = 23.70
                                    centerLon = 69.20
                                    zoom = 6.4f
                                },
                                label = { Text("◄ West: Kutch / Ghuar Mota", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.West, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = {
                                    centerLat = 8.40
                                    centerLon = 77.55
                                    zoom = 6.8f
                                },
                                label = { Text("▼ South: Kanyakumari", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.South, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = {
                                    centerLat = 7.10
                                    centerLon = 93.85
                                    zoom = 6.8f
                                },
                                label = { Text("✦ South: Indira Point, A&N", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.PinDrop, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = {
                                    centerLat = 22.2
                                    centerLon = 82.5
                                    zoom = 4.5f
                                },
                                label = { Text("🇮🇳 Reset Entire India", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                    }
                }
            }
        }

        // Active Sanctuary Inspector Detail Card
        if (selectedLocation != null) {
            val loc = selectedLocation!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 3.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (bioColors.isDark) MintLight.copy(alpha = 0.6f) else ForestGreenPrimary.copy(alpha = 0.6f),
                                ForestGreenPrimary
                            )
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bioColors.containerGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Park,
                                        contentDescription = null,
                                        tint = bioColors.onContainerGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = loc.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = bioColors.textPrimary
                                    )
                                    Text(
                                        text = "${loc.state} • ${loc.coordinatesStr}",
                                        fontSize = 11.sp,
                                        color = bioColors.textSecondary
                                    )
                                }
                            }

                            Surface(
                                color = bioColors.containerGreen,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = loc.type,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bioColors.onContainerGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = loc.description,
                            fontSize = 13.sp,
                            color = bioColors.textPrimary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (loc.elevationProfile.isNotBlank()) {
                                Surface(
                                    color = bioColors.containerNeutral,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Terrain,
                                            contentDescription = null,
                                            tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = loc.elevationProfile,
                                            fontSize = 10.sp,
                                            color = bioColors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            if (loc.areaSqKm > 0) {
                                Surface(
                                    color = bioColors.containerNeutral,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CropFree,
                                            contentDescription = null,
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${loc.areaSqKm.roundToInt()} km²",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = bioColors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = bioColors.containerGreen.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.NaturePeople,
                                    contentDescription = null,
                                    tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 1.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = loc.importance,
                                    fontSize = 11.sp,
                                    color = bioColors.textPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "KEY FLAGSHIP SPECIES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(loc.keySpecies) { spName ->
                                Surface(
                                    color = bioColors.containerNeutral,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "🐾 $spName",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = bioColors.onContainerNeutral,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                centerLat = loc.effectiveLat
                                centerLon = loc.effectiveLon
                                zoom = 7.8f
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Focus High-Accuracy Terrain on ${loc.name.take(20)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Carousel of Protected Reserves
        item {
            Text(
                text = "Protected Sanctuaries & Reserves (${filteredLocations.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = bioColors.textPrimary
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredLocations) { loc ->
                    val isSelected = loc.id == selectedLocation?.id
                    Card(
                        modifier = Modifier
                            .width(230.dp)
                            .clickable {
                                selectedLocation = loc
                                centerLat = loc.effectiveLat
                                centerLon = loc.effectiveLon
                                zoom = 7.0f
                            },
                        shape = RoundedCornerShape(16.dp),
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
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = if (isSelected) ForestGreenPrimary else bioColors.containerNeutral,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = loc.type.take(16),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else bioColors.onContainerNeutral,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = if (isSelected) "● ACTIVE" else "FOCUS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) (if (bioColors.isDark) MintLight else ForestGreenPrimary) else bioColors.textMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = loc.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = loc.state,
                                fontSize = 11.sp,
                                color = bioColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = loc.importance,
                                fontSize = 10.sp,
                                color = bioColors.textMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
