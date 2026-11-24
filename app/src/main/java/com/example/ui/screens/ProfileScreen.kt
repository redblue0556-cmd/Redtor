package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.core.firebase.signOutUser
import com.example.data.model.UserProfile
import com.example.ui.components.GlassSectionHeader
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderRuby
import com.example.ui.theme.GlassFillDark
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RoseGoldAccent
import com.example.ui.theme.RubyBright
import com.example.ui.theme.RubyCrimson
import com.example.ui.theme.RubyDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RedtorViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(
    firebaseUser: FirebaseUser,
    viewModel: RedtorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val statusMsg by viewModel.profileStatusMessage.collectAsStateWithLifecycle()

    val profile = (profileState as? UiState.Success)?.data ?: UserProfile(
        userId = firebaseUser.uid,
        displayName = firebaseUser.displayName ?: "Redtor Creator",
        email = firebaseUser.email,
        photoUrl = firebaseUser.photoUrl?.toString()
    )

    val luts = remember { listOf("Ruby Noir", "Cyber Obsidian", "Golden Cinema", "Teal & Orange") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            GlassSectionHeader(
                badge = "ACCOUNT & SYSTEM",
                title = "Profil & Pengaturan",
                subtitle = "Akun Google terhubung, status VIP Redtor Pro, dan preferensi cloud",
                modifier = Modifier.widthIn(max = 600.dp)
            )
        }

        // Google Profile & VIP Card
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                highlightRuby = true
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!profile.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.photoUrl,
                            contentDescription = "Google Profile Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .border(2.dp, GlassBorderRuby, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(RubyDeep)
                                .border(2.dp, GlassBorderRuby, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Default Avatar",
                                tint = RoseGoldAccent,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.displayName,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (!profile.email.isNullOrBlank()) {
                            Text(
                                text = profile.email.orEmpty(),
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(RubyCrimson.copy(alpha = 0.25f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = RoseGoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REDTOR ${profile.tier} • VIP ACTIVE",
                                color = RoseGoldAccent,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Daily AI Quota Card
        item {
            val used = profile.dailyAiQuotaUsed.coerceAtLeast(0L)
            val limit = profile.dailyAiQuotaLimit.coerceAtLeast(1L)
            val ratio = (used.toFloat() / limit.toFloat()).coerceIn(0.04f, 1f)

            LiquidGlassCard(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kuota AI Harian (Gemini 3.8)", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text("$used / $limit", color = RoseGoldAccent, style = MaterialTheme.typography.labelMedium)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(ObsidianElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(RubyCrimson, RoseGoldAccent)))
                        )
                    }
                    Text(
                        text = "Semua proyek studio, antrean media, dan riwayat chat disinkronkan aman ke Firebase Cloud Firestore.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // System Settings Card
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Pengaturan Studio", color = TextPrimary, style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Delete Cloud Cache", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Bersihkan file sementara otomatis setelah ekspor",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Switch(
                            checked = profile.autoDeleteCache,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings(checked, profile.preferredColorGrade)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = RubyCrimson
                            ),
                            modifier = Modifier.testTag("profile_autodelete_switch")
                        )
                    }

                    Text("Default Color Grading LUT", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        luts.forEach { lut ->
                            FilterChip(
                                selected = profile.preferredColorGrade == lut,
                                onClick = { viewModel.updateSettings(profile.autoDeleteCache, lut) },
                                label = { Text(lut, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RubyCrimson.copy(alpha = 0.35f),
                                    selectedLabelColor = RoseGoldAccent,
                                    containerColor = GlassFillDark,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    if (statusMsg != null) {
                        Text(statusMsg.orEmpty(), color = EmeraldSuccess, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            signOutUser(credentialManager, scope)
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = RubyBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", color = RubyBright, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
