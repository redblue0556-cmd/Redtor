package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.core.firebase.onGoogleSignInClicked
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.RubyGlowButton
import com.example.ui.theme.GlassBorderRuby
import com.example.ui.theme.RoseGoldAccent
import com.example.ui.theme.RubyBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LiquidGlassBackdrop(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.5.dp, GlassBorderRuby, RoundedCornerShape(28.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.redtor_logo),
                        contentDescription = "Redtor App Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "REDTOR STUDIO",
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Liquid Glass Creative Suite powered by Gemini AI. Masuk dengan akun Google untuk sinkronisasi proyek, riwayat chat, dan kuota studio.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    highlightRuby = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FeatureRow(
                            icon = Icons.Filled.AutoAwesome,
                            title = "AI Photo & Video Studio",
                            desc = "Text-to-Video, 4K Upscale, Beauty FX, Magic Eraser & Color LUTs"
                        )
                        FeatureRow(
                            icon = Icons.Filled.Download,
                            title = "Universal Media Hub",
                            desc = "Smart Clipboard Link Analyzer, MP4/MP3 Muxing Profile & Queue"
                        )
                        FeatureRow(
                            icon = Icons.Filled.Forum,
                            title = "Multi-Platform Social AI",
                            desc = "Chatbot khusus IG, TikTok, YouTube, WA, X + Script & Trend Radar"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        RubyGlowButton(
                            text = if (isLoading) "Menghubungkan Google..." else "Sign in with Google",
                            icon = Icons.Filled.Login,
                            loading = isLoading,
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isLoading = false
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "google_sign_in_button"
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage.orEmpty(),
                                color = RubyBright,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RoseGoldAccent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = desc,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
