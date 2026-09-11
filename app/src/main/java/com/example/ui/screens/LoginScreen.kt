package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BioGuardRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    repository: BioGuardRepository,
    onLoginSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bioColors = MaterialTheme.bioColors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    fun performGoogleSignIn() {
        errorMessage = null
        isGoogleSigningIn = true
        coroutineScope.launch {
            val res = repository.signInWithGoogle(context)
            isGoogleSigningIn = false
            res.fold(
                onSuccess = { profile ->
                    Toast.makeText(
                        context,
                        "Welcome, ${profile.name}! Signed in with Google (Firebase) ✓",
                        Toast.LENGTH_LONG
                    ).show()
                    onLoginSuccess()
                },
                onFailure = { err ->
                    errorMessage = err.localizedMessage ?: "Google Sign-In failed"
                }
            )
        }
    }

    fun performSubmit() {
        errorMessage = null
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank()) {
            errorMessage = "Please enter your email address."
            return
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            errorMessage = "Please enter a valid email address."
            return
        }
        if (cleanPassword.length < 4) {
            errorMessage = "Password must be at least 4 characters."
            return
        }
        if (isSignUpMode) {
            if (name.trim().isBlank()) {
                errorMessage = "Please enter your full name."
                return
            }
            if (cleanPassword != confirmPassword.trim()) {
                errorMessage = "Passwords do not match."
                return
            }
        }

        isLoading = true
        coroutineScope.launch {
            if (isSignUpMode) {
                val res = repository.register(name.trim(), cleanEmail, cleanPassword)
                isLoading = false
                res.fold(
                    onSuccess = {
                        Toast.makeText(context, "Account created! Welcome, ${it.name}", Toast.LENGTH_LONG).show()
                        onLoginSuccess()
                    },
                    onFailure = {
                        errorMessage = it.localizedMessage ?: "Registration failed."
                    }
                )
            } else {
                val res = repository.login(cleanEmail, cleanPassword)
                isLoading = false
                res.fold(
                    onSuccess = { profile ->
                        Toast.makeText(
                            context,
                            "Welcome back, ${profile.name}! Synced with cloud ✓",
                            Toast.LENGTH_LONG
                        ).show()
                        onLoginSuccess()
                    },
                    onFailure = {
                        errorMessage = it.localizedMessage ?: "Login failed."
                    }
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bioColors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Branding Visual Header
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    ForestGreenPrimary,
                                    ForestGreenDark
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Brush.sweepGradient(listOf(MintLight, EmeraldAccent, MintLight)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = MintLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BIOGUARD ECO-REGISTRY",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = bioColors.textPrimary
                )
                Text(
                    text = "Global Conservation & Species Network",
                    fontSize = 13.sp,
                    color = bioColors.textMuted
                )
            }

            // Cross-Device Sync Explanation Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = bioColors.containerGreen,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ForestGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Cross-Device Data Sync",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = bioColors.onContainerGreen
                        )
                        Text(
                            text = "Log in on any phone to automatically restore your observations, threat reports, and eco-points.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = bioColors.onContainerGreen.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Mode Selector: Sign In vs Create Account
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = bioColors.containerNeutral,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (!isSignUpMode) bioColors.surfaceCard else Color.Transparent
                            )
                            .clickable {
                                isSignUpMode = false
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            fontSize = 14.sp,
                            fontWeight = if (!isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isSignUpMode) bioColors.textPrimary else bioColors.textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSignUpMode) bioColors.surfaceCard else Color.Transparent
                            )
                            .clickable {
                                isSignUpMode = true
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Account",
                            fontSize = 14.sp,
                            fontWeight = if (isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSignUpMode) bioColors.textPrimary else bioColors.textMuted
                        )
                    }
                }
            }

            // Error Banner if present
            AnimatedVisibility(visible = errorMessage != null) {
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Input Fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = bioColors.surfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Full Name (only in Sign Up)
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Sankeerth T") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null, tint = bioColors.textMuted)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreenPrimary,
                                unfocusedBorderColor = bioColors.surfaceBorder
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input")
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("ranger@bioguard.eco") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Mail, contentDescription = null, tint = bioColors.textMuted)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreenPrimary,
                            unfocusedBorderColor = bioColors.surfaceBorder
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = bioColors.textMuted)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = bioColors.textMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (isSignUpMode) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = {
                                focusManager.clearFocus()
                                performSubmit()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreenPrimary,
                            unfocusedBorderColor = bioColors.surfaceBorder
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Confirm Password (only in Sign Up)
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            placeholder = { Text("••••••••") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = bioColors.textMuted)
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    performSubmit()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreenPrimary,
                                unfocusedBorderColor = bioColors.surfaceBorder
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_password_input")
                        )
                    }

                    // Quick Demo Credentials Chips for Easy Testing
                    if (!isSignUpMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Quick Fill Test Accounts:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = bioColors.textMuted
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SuggestionChip(
                                    onClick = {
                                        email = "sankeertht6@gmail.com"
                                        password = "password123"
                                    },
                                    label = { Text("sankeertht6", fontSize = 11.sp) },
                                    icon = {
                                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    modifier = Modifier.testTag("quick_fill_sankeerth")
                                )
                                SuggestionChip(
                                    onClick = {
                                        email = "ranger@bioguard.eco"
                                        password = "password123"
                                    },
                                    label = { Text("ranger@bioguard", fontSize = 11.sp) },
                                    icon = {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    modifier = Modifier.testTag("quick_fill_ranger")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Google Sign-In with Credential Manager Button
                    OutlinedButton(
                        onClick = { performGoogleSignIn() },
                        enabled = !isLoading && !isGoogleSigningIn,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = bioColors.surfaceCard,
                            contentColor = bioColors.textPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isGoogleSigningIn) {
                            CircularProgressIndicator(
                                strokeWidth = 2.5.dp,
                                color = ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Connecting with Google...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = bioColors.textPrimary
                            )
                        } else {
                            GoogleLogoIcon(modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = bioColors.textPrimary
                            )
                        }
                    }

                    // Primary Submit Button
                    Button(
                        onClick = { performSubmit() },
                        enabled = !isLoading && !isGoogleSigningIn,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreenPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isSignUpMode) "Creating Cloud Account..." else "Connecting to Cloud...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = if (isSignUpMode) Icons.Default.PersonAdd else Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSignUpMode) "Create BioGuard Account" else "Sign In with Email",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Divider or Guest
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = bioColors.surfaceBorder)
                Text(
                    text = "OR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = bioColors.textMuted,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = bioColors.surfaceBorder)
            }

            // Continue as Guest / Offline Button
            OutlinedButton(
                onClick = onContinueAsGuest,
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(bioColors.surfaceBorder, bioColors.surfaceBorder))
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = bioColors.textPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("guest_button")
            ) {
                Icon(
                    Icons.Default.Explore,
                    contentDescription = null,
                    tint = if (bioColors.isDark) MintLight else ForestGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continue as Guest Scout (Offline)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Protected by BioGuard Cloud & HTTPS Encryption. Observations are archived under scientific open-access standards.",
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = bioColors.textMuted.copy(alpha = 0.7f),
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.18f

        // Blue right arc + horizontal arm
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
            end = androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.5f),
            strokeWidth = stroke
        )

        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )

        // Yellow bottom-left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )

        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
    }
}

