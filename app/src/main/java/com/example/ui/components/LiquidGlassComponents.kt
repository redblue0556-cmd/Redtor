package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.GlassBorderRuby
import com.example.ui.theme.GlassBorderSpecular
import com.example.ui.theme.GlassFillDark
import com.example.ui.theme.GlassFillLight
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RoseGoldAccent
import com.example.ui.theme.RubyBright
import com.example.ui.theme.RubyCrimson
import com.example.ui.theme.RubyDeep
import com.example.ui.theme.RubyGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class RedtorTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    STUDIO("Studio", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_studio"),
    DOWNLOADER("Media", Icons.Filled.Download, Icons.Outlined.Download, "tab_downloader"),
    HUB("Social AI", Icons.Filled.Forum, Icons.Outlined.Forum, "tab_hub"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

@Composable
fun LiquidGlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_mesh")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mesh_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.32f)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RubyCrimson.copy(alpha = 0.24f),
                                RubyDeep.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.2f + 0.35f * phase), h * 0.16f),
                            radius = w * 0.85f
                        ),
                        radius = w * 0.85f,
                        center = Offset(w * (0.2f + 0.35f * phase), h * 0.16f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RubyGlow.copy(alpha = 0.16f),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.85f - 0.3f * phase), h * 0.78f),
                            radius = w * 0.75f
                        ),
                        radius = w * 0.75f,
                        center = Offset(w * (0.85f - 0.3f * phase), h * 0.78f)
                    )
                }
        )
        content()
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    highlightRuby: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = Brush.linearGradient(
        colors = if (highlightRuby) {
            listOf(RoseGoldAccent.copy(alpha = 0.65f), GlassBorderRuby, GlassBorderSpecular.copy(alpha = 0.25f))
        } else {
            listOf(GlassBorderSpecular, GlassBorderRuby.copy(alpha = 0.35f), Color.White.copy(alpha = 0.10f))
        }
    )
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            if (highlightRuby) RubyDeep.copy(alpha = 0.42f) else GlassFillLight,
            GlassFillDark
        )
    )

    Surface(
        modifier = modifier
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .border(width = 1.dp, brush = borderBrush, shape = shape),
        shape = shape,
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(bgBrush)
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun RubyGlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    testTag: String = "ruby_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RubyCrimson,
            contentColor = TextPrimary,
            disabledContainerColor = RubyDeep.copy(alpha = 0.5f),
            disabledContentColor = TextSecondary
        ),
        border = BorderStroke(1.dp, RoseGoldAccent.copy(alpha = 0.45f)),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(52.dp)
            .testTag(testTag)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = TextPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FloatingLiquidBottomBar(
    selectedTab: RedtorTab,
    onSelectTab: (RedtorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(30.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(barShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xD91E0B13),
                            Color(0xEE0D0509)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassBorderSpecular,
                            GlassBorderRuby,
                            Color.White.copy(alpha = 0.14f)
                        )
                    ),
                    shape = barShape
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RedtorTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tab_scale"
                )
                val pillColor by animateColorAsState(
                    targetValue = if (isSelected) RubyCrimson.copy(alpha = 0.28f) else Color.Transparent,
                    label = "tab_bg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) RoseGoldAccent else TextSecondary,
                    label = "tab_fg"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                        .clip(RoundedCornerShape(22.dp))
                        .background(pillColor)
                        .then(
                            if (isSelected) Modifier.border(
                                1.dp,
                                RubyBright.copy(alpha = 0.55f),
                                RoundedCornerShape(22.dp)
                            ) else Modifier
                        )
                        .clickable { onSelectTab(tab) }
                        .minimumInteractiveComponentSize()
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .testTag(tab.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        color = contentColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun GlassSectionHeader(
    badge: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(RubyCrimson.copy(alpha = 0.22f))
                    .border(1.dp, RubyBright.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    color = RoseGoldAccent,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
