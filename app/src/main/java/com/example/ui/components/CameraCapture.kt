package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintLight
import com.example.ui.theme.bioColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

/**
 * CameraCapture composable provides a real-time CameraX viewfinder and photo capture HUD
 * for biodiversity species identification.
 */
@Composable
fun CameraCapture(
    onPhotoCaptured: (Bitmap, Uri) -> Unit,
    onClose: () -> Unit,
    onPickFromGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val bioColors = MaterialTheme.bioColors

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        // Permission Request UI
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F1713))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2720)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(ForestGreenPrimary, MintLight))
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ForestGreenPrimary.copy(alpha = 0.2f))
                            .border(1.5.dp, MintLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Camera Permission",
                            tint = MintLight,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Camera Access Needed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "To take real-time photos of wild animals, birds, insects, and flora for instant AI species identification, please grant camera access.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("grant_camera_permission_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grant Camera Permission", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPickFromGallery,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MintLight)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Use Gallery", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        return
    }

    // CameraX Active Viewfinder State
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // Tap to focus indicator
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var showFocusRing by remember { mutableStateOf(false) }

    // Shutter flash effect
    var triggerShutterFlash by remember { mutableStateOf(false) }

    // Scanner reticle pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_pulse")
    val reticleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reticle_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Live Camera Preview Surface with Tap-to-Focus
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        focusPoint = offset
                        showFocusRing = true
                        val pView = previewView
                        val cam = camera
                        if (pView != null && cam != null) {
                            try {
                                val factory = pView.meteringPointFactory
                                val point = factory.createPoint(offset.x, offset.y)
                                val action = FocusMeteringAction.Builder(
                                    point,
                                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                                )
                                    .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                                    .build()
                                cam.cameraControl.startFocusAndMetering(action)
                            } catch (_: Exception) {}
                        }
                    }
                }
        )

        // Bind CameraX on lensFacing or lifecycle change
        LaunchedEffect(lensFacing) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(previewView?.display?.rotation ?: android.view.Surface.ROTATION_0)
                        .build()

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    cameraProvider.unbindAll()

                    val boundCamera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        capture
                    )

                    previewView?.let { pv ->
                        preview.setSurfaceProvider(pv.surfaceProvider)
                    }

                    camera = boundCamera
                    imageCapture = capture

                    // Restore torch state if supported
                    if (boundCamera.cameraInfo.hasFlashUnit()) {
                        boundCamera.cameraControl.enableTorch(isTorchOn)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // Tap-to-focus ring
        LaunchedEffect(showFocusRing) {
            if (showFocusRing) {
                delay(1500)
                showFocusRing = false
            }
        }

        if (showFocusRing && focusPoint != null) {
            val pt = focusPoint!!
            Box(
                modifier = Modifier
                    .offset(x = (pt.x - 30).dp, y = (pt.y - 30).dp)
                    .size(60.dp)
                    .border(2.dp, MintLight, RoundedCornerShape(8.dp))
            )
        }

        // 2. Viewfinder Reticle & HUD Overlay
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val w = size.width
            val h = size.height

            // Semi-dark top & bottom margins
            val targetBoxWidth = w * 0.78f
            val targetBoxHeight = targetBoxWidth * 1.15f
            val boxLeft = (w - targetBoxWidth) / 2f
            val boxTop = (h - targetBoxHeight) / 2.2f
            val cornerLen = 32.dp.toPx()
            val strokeWidth = 3.5.dp.toPx()

            val reticleColor = Color(0xFF68D391).copy(alpha = reticleAlpha)

            // Top-Left Corner
            drawLine(reticleColor, Offset(boxLeft, boxTop), Offset(boxLeft + cornerLen, boxTop), strokeWidth)
            drawLine(reticleColor, Offset(boxLeft, boxTop), Offset(boxLeft, boxTop + cornerLen), strokeWidth)

            // Top-Right Corner
            drawLine(reticleColor, Offset(boxLeft + targetBoxWidth, boxTop), Offset(boxLeft + targetBoxWidth - cornerLen, boxTop), strokeWidth)
            drawLine(reticleColor, Offset(boxLeft + targetBoxWidth, boxTop), Offset(boxLeft + targetBoxWidth, boxTop + cornerLen), strokeWidth)

            // Bottom-Left Corner
            drawLine(reticleColor, Offset(boxLeft, boxTop + targetBoxHeight), Offset(boxLeft + cornerLen, boxTop + targetBoxHeight), strokeWidth)
            drawLine(reticleColor, Offset(boxLeft, boxTop + targetBoxHeight), Offset(boxLeft, boxTop + targetBoxHeight - cornerLen), strokeWidth)

            // Bottom-Right Corner
            drawLine(reticleColor, Offset(boxLeft + targetBoxWidth, boxTop + targetBoxHeight), Offset(boxLeft + targetBoxWidth - cornerLen, boxTop + targetBoxHeight), strokeWidth)
            drawLine(reticleColor, Offset(boxLeft + targetBoxWidth, boxTop + targetBoxHeight), Offset(boxLeft + targetBoxWidth, boxTop + targetBoxHeight - cornerLen), strokeWidth)

            // Center targeting reticle plus
            val centerX = w / 2f
            val centerY = boxTop + targetBoxHeight / 2f
            val crossSize = 10.dp.toPx()
            drawLine(Color.White.copy(alpha = 0.4f), Offset(centerX - crossSize, centerY), Offset(centerX + crossSize, centerY), 1.5.dp.toPx())
            drawLine(Color.White.copy(alpha = 0.4f), Offset(centerX, centerY - crossSize), Offset(centerX, centerY + crossSize), 1.5.dp.toPx())
        }

        // 3. Top Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close Button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .testTag("camera_close_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
            }

            // Central Status Pill
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(ForestGreenPrimary, MintLight))
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MintLight)
                    )
                    Text(
                        text = "BioScan Live Viewfinder",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Torch / Flash Toggle
            IconButton(
                onClick = {
                    val cam = camera
                    if (cam?.cameraInfo?.hasFlashUnit() == true) {
                        val nextState = !isTorchOn
                        cam.cameraControl.enableTorch(nextState)
                        isTorchOn = nextState
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isTorchOn) ForestGreenPrimary else Color.Black.copy(alpha = 0.6f))
                    .testTag("camera_torch_button")
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash Toggle",
                    tint = if (isTorchOn) Color(0xFFFFD54F) else Color.White
                )
            }
        }

        // 4. Center Guidance Helper
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 170.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Align species within frame & tap screen to focus",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // 5. Bottom Controls Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f), Color.Black)
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Switch to Gallery
                IconButton(
                    onClick = onPickFromGallery,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .testTag("camera_switch_gallery_button")
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = "Open Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Primary Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clickable(enabled = !isCapturing && imageCapture != null) {
                            val imgCap = imageCapture ?: return@clickable
                            isCapturing = true
                            triggerShutterFlash = true

                            val photoFile = try {
                                File.createTempFile("bioguard_species_", ".jpg", context.cacheDir)
                            } catch (e: Exception) {
                                isCapturing = false
                                return@clickable
                            }

                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            imgCap.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        coroutineScope.launch {
                                            try {
                                                val capturedBitmap = decodeRotatedBitmapFromFile(photoFile)
                                                onPhotoCaptured(capturedBitmap, Uri.fromFile(photoFile))
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            } finally {
                                                isCapturing = false
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        exception.printStackTrace()
                                        isCapturing = false
                                    }
                                }
                            )
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = ForestGreenPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                // Switch Camera Lens (Back / Front)
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .testTag("camera_switch_lens_button")
                ) {
                    Icon(
                        Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch Camera Lens",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Shutter flash effect
        if (triggerShutterFlash) {
            LaunchedEffect(Unit) {
                delay(80)
                triggerShutterFlash = false
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.75f))
            )
        }
    }
}

/**
 * Safely decodes a photo file captured by CameraX, applying EXIF rotation
 * and ensuring software-backed ARGB_8888 memory configuration for AI model inference.
 */
private fun decodeRotatedBitmapFromFile(file: File): Bitmap {
    var orientation = ExifInterface.ORIENTATION_NORMAL
    try {
        val exif = ExifInterface(file.absolutePath)
        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (_: Exception) {}

    // First decode bounds
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

    val maxDimension = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
    var sampleSize = 1
    if (maxDimension > 1400) {
        sampleSize = maxDimension / 1400
    }

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }

    val rawBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        ?: throw IllegalStateException("Unable to decode captured photo.")

    val softwareBitmap = if (rawBitmap.config == Bitmap.Config.HARDWARE) {
        rawBitmap.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        rawBitmap
    }

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
