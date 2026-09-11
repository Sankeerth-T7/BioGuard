package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.api.GeminiService
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDialog(
    geminiService: GeminiService,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var promptInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hello! I am BioGuard AI 🌿. Ask me anything about biodiversity, native flora & fauna, ecosystems, habitat restoration, or conservation practices.",
                isUser = false
            )
        )
    }

    val suggestedQuestions = listOf(
        "What is a keystone species?",
        "How to identify invasive plants?",
        "Tell me about local biodiversity",
        "What causes habitat loss?",
        "Explain ecological restoration",
        "Explain social forestry"
    )

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BioGuard AI Response", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied response to clipboard ✓", Toast.LENGTH_SHORT).show()
    }

    fun sendQuestion(questionText: String) {
        if (questionText.isBlank() || isLoading) return
        val userMsg = questionText.trim()
        messages.add(ChatMessage(text = userMsg, isUser = true))
        promptInput = ""
        isLoading = true

        coroutineScope.launch {
            val answer = geminiService.askBioGuardAi(userMsg)
            messages.add(ChatMessage(text = answer, isUser = false))
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = bioColors.surfaceCard,
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bioColors.containerGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = bioColors.onContainerGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BioGuard AI Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = bioColors.textPrimary
                            )
                            Text(
                                text = "Environmental & Conservation Tutor",
                                fontSize = 11.sp,
                                color = bioColors.textSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = bioColors.textPrimary)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = bioColors.surfaceBorder)

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start,
                                modifier = Modifier.widthIn(max = 285.dp)
                            ) {
                                Surface(
                                    color = if (msg.isUser) ForestGreenPrimary else bioColors.containerNeutral,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                        bottomEnd = if (msg.isUser) 4.dp else 16.dp
                                    )
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = if (msg.isUser) Color.White else bioColors.textPrimary,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                                if (!msg.isUser) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(top = 2.dp, start = 4.dp)
                                            .clickable { copyToClipboard(msg.text) }
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy Response",
                                            tint = bioColors.textMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Copy",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = bioColors.textMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Surface(
                                color = bioColors.containerNeutral,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = ForestGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "BioGuard AI is formulating response...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = bioColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Suggested Prompt Chips
                Text(
                    text = "Suggested Questions:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = bioColors.textSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    items(suggestedQuestions) { question ->
                        Surface(
                            color = bioColors.containerGreen,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.clickable { sendQuestion(question) }
                        ) {
                            Text(
                                text = question,
                                fontSize = 11.sp,
                                color = bioColors.onContainerGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Input Box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = { Text("Ask about flora, fauna, ecosystems...", fontSize = 12.sp, color = bioColors.textMuted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreenPrimary,
                            unfocusedBorderColor = bioColors.surfaceBorder,
                            focusedTextColor = bioColors.textPrimary,
                            unfocusedTextColor = bioColors.textPrimary
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { sendQuestion(promptInput) },
                        enabled = promptInput.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (promptInput.isNotBlank()) ForestGreenPrimary else bioColors.containerNeutral)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (promptInput.isNotBlank()) Color.White else bioColors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
