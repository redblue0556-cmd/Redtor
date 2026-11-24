package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GlassBorderRuby
import com.example.ui.theme.GlassBorderSpecular
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RoseGoldAccent
import com.example.ui.theme.RubyBright
import com.example.ui.theme.RubyCrimson
import com.example.ui.theme.RubyDeep
import com.example.ui.theme.RubyGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "splash_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splash_fx")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )
    val particleShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    LaunchedEffect(Unit) {
        val steps = listOf(0.18f, 0.42f, 0.68f, 0.89f, 1.0f)
        for (step in steps) {
            targetProgress = step
            delay(360L)
        }
        delay(320L)
        onFinished()
    }

    val percent = (progress * 100).toInt().coerceIn(0, 100)

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
                .alpha(0.72f)
        )

        // Floating light particles + ruby refraction aura
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RubyCrimson.copy(alpha = 0.34f),
                        RubyDeep.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.42f),
                    radius = w * 0.58f
                ),
                center = Offset(w * 0.5f, h * 0.42f),
                radius = w * 0.58f
            )

            val seeds = listOf(0.14f, 0.28f, 0.41f, 0.56f, 0.72f, 0.84f, 0.22f, 0.65f, 0.90f)
            seeds.forEachIndexed { idx, xRatio ->
                val yRatio = ((1f - particleShift + idx * 0.13f) % 1f)
                val radius = if (idx % 2 == 0) 4.5f else 2.5f
                drawCircle(
                    color = if (idx % 3 == 0) RoseGoldAccent.copy(alpha = 0.65f) else RubyGlow.copy(alpha = 0.55f),
                    radius = radius,
                    center = Offset(w * xRatio, h * yRatio)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top subtle status pill
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x401E0810))
                    .border(1.dp, GlassBorderSpecular, CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "GEMINI 3.8 PRO • LIQUID GLASS ENGINE",
                    color = RoseGoldAccent,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Centerpiece 3D Ruby Crystal Logo + Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(164.dp)
                        .scale(pulse)
                ) {
                    Box(
                        modifier = Modifier
                            .size(164.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        RubyBright.copy(alpha = 0.45f),
                                        RubyDeep.copy(alpha = 0.22f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(138.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .border(
                                width = 1.8.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(RoseGoldAccent, RubyBright, GlassBorderSpecular)
                                ),
                                shape = RoundedCornerShape(36.dp)
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.redtor_logo),
                            contentDescription = "Redtor 3D Ruby Crystal Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "REDTOR",
                    color = TextPrimary,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 42.sp,
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "REDTOR - AI CREATIVE STUDIO",
                    color = RoseGoldAccent,
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Powered by Gemini 3.8 AI",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Bottom Game-Grade Frosted Glass Loading Bar + Copyright
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Loading AI Engine...",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "[$percent%]",
                        color = RoseGoldAccent,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.testTag("splash_progress_text")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Frosted Glass Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(Color(0x5518080F))
                        .border(1.dp, GlassBorderSpecular, CircleShape)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = progress.coerceIn(0.05f, 1f))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(RubyDeep, RubyCrimson, RubyBright, RoseGoldAccent)
                                )
                            )
                            .border(0.5.dp, GlassBorderRuby, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "© REDTOR STUDIO • Google AI Integrated",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
