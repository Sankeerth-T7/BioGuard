package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun BioGuardLogo(
    modifier: Modifier = Modifier,
    size: Int = 38,
    showText: Boolean = true
) {
    val bioColors = MaterialTheme.bioColors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(ForestGreenPrimary, EmeraldAccent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "BioGuard Leaf Emblem",
                tint = Color.White,
                modifier = Modifier.size((size * 0.65).dp)
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "BIOGUARD",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.2.sp,
                    color = if (bioColors.isDark) MintLight else ForestGreenPrimary
                )
                Text(
                    text = "Discover • Protect • Restore",
                    fontSize = 10.sp,
                    color = if (bioColors.isDark) bioColors.textSecondary else EmeraldAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = EmeraldAccent,
    containerColor: Color = MaterialTheme.bioColors.surfaceCard,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (bioColors.isDark) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = bioColors.textMuted,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = bioColors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = bioColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ConfidenceBadge(
    confidence: Int,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val (bgColor, textColor, text) = when {
        confidence >= 85 -> Triple(bioColors.containerGreen, bioColors.onContainerGreen, "AI Confidence $confidence% (High)")
        confidence >= 60 -> Triple(bioColors.containerAmber, bioColors.onContainerAmber, "AI Confidence $confidence% (Moderate)")
        else -> Triple(bioColors.containerRed, bioColors.onContainerRed, "AI Confidence $confidence% (Uncertain)")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (confidence >= 60) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun SeverityBadge(
    severity: String,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val (bgColor, textColor) = when (severity.lowercase()) {
        "critical" -> Pair(bioColors.containerRed, bioColors.onContainerRed)
        "high" -> Pair(bioColors.containerAmber, bioColors.onContainerAmber)
        "medium" -> Pair(bioColors.containerAmber, bioColors.onContainerAmber)
        else -> Pair(bioColors.containerGreen, bioColors.onContainerGreen)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = "Severity: $severity",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EducationalDisclaimer(
    text: String = "AI identification is an estimate and should not be treated as definitive scientific identification. Verify with local flora/fauna experts.",
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    Surface(
        color = bioColors.containerNeutral,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Educational Disclaimer",
                tint = bioColors.textMuted,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = bioColors.onContainerNeutral,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun DemoModeBadge(
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    Surface(
        color = bioColors.containerAmber,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = "Demo Mode",
                tint = bioColors.onContainerAmber,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Demo/Sample Data",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = bioColors.onContainerAmber
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(bioColors.containerGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = bioColors.onContainerGreen,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = bioColors.textPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = bioColors.textMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = actionButtonText, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}
