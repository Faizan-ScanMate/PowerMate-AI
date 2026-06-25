package com.powermate.ai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powermate.ai.ui.theme.*
import kotlin.math.*

// ── Section Card ───────────────────────────────────────────────────────────

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    gradient: List<Color>? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val bg = if (gradient != null) {
        Modifier.background(Brush.linearGradient(gradient), RoundedCornerShape(28.dp))
    } else {
        Modifier.background(
            Brush.verticalGradient(listOf(CardDark, CardElevated.copy(alpha = 0.8f))),
            RoundedCornerShape(28.dp)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(bg)
            .border(0.5.dp, TextMuted.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

// ── Metric Card ────────────────────────────────────────────────────────────

@Composable
fun MetricCard(
    title: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    accent: Color = Cyan,
    fontScale: Float = 1.0f,
    showPulse: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by if (showPulse) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "alpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(
        modifier = modifier
            .heightIn(min = 92.dp)
            .background(
                Brush.verticalGradient(listOf(CardDark, accent.copy(alpha = 0.05f))),
                RoundedCornerShape(22.dp)
            )
            .border(
                0.5.dp,
                accent.copy(alpha = if (showPulse) alpha * 0.4f else 0.18f),
                RoundedCornerShape(22.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Text(title, color = TextSecondary, fontSize = (11 * fontScale).sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(value, color = TextMain, fontSize = (22 * fontScale).sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text(caption, color = accent.copy(alpha = if (showPulse) alpha else 1f), fontSize = (11 * fontScale).sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ── Status Chip ───────────────────────────────────────────────────────────

@Composable
fun StatusChip(
    text: String,
    color: Color = SuccessGreen,
    modifier: Modifier = Modifier,
    pulsing: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chip")
    val scale by if (pulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 54.dp)
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
            .border(0.5.dp, color.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (pulsing) {
                Box(
                    modifier = Modifier
                        .size((6 * scale).dp)
                        .background(color, CircleShape)
                )
            }
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Primary Action Button ──────────────────────────────────────────────────

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, gradient: List<Color> = GradientPrimary) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(Brush.linearGradient(gradient), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

// ── Animated Battery Ring ──────────────────────────────────────────────────

@Composable
fun BatteryRing(level: Int, modifier: Modifier = Modifier, isCharging: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val rotationAnim by if (isCharging) {
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
            label = "rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val sweepAnim by animateFloatAsState(
        targetValue = 360f * (level.coerceIn(0, 100) / 100f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    Box(modifier = modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 18.dp.toPx()
            val stroke = Stroke(width = strokeW, cap = StrokeCap.Round)
            val inset = 18.dp.toPx()
            val sz = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // Background track
            drawArc(color = CardHighlight, startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = sz, style = stroke)

            // Glow layer
            if (isCharging) {
                rotate(rotationAnim) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Color.Transparent, Cyan.copy(alpha = 0.3f), Color.Transparent)),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = sz, style = stroke
                    )
                }
            }

            // Main arc
            drawArc(
                brush = Brush.sweepGradient(listOf(PrimaryBlue, Cyan, SuccessGreen, PrimaryBlue)),
                startAngle = -90f, sweepAngle = sweepAnim,
                useCenter = false, topLeft = topLeft, size = sz, style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$level%", color = TextMain, fontSize = 54.sp, fontWeight = FontWeight.Bold)
            Text(if (isCharging) "Charging" else "Battery", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// ── Colored Battery Ring ───────────────────────────────────────────────────

@Composable
fun BatteryRingColored(level: Int, accent: Color, modifier: Modifier = Modifier, isCharging: Boolean = false) {
    val sweepAnim by animateFloatAsState(
        targetValue = 360f * (level.coerceIn(0, 100) / 100f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    Box(modifier = modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 18.dp.toPx()
            val stroke = Stroke(width = strokeW, cap = StrokeCap.Round)
            val inset = 18.dp.toPx()
            val sz = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            drawArc(color = CardHighlight, startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = sz, style = stroke)
            drawArc(
                brush = Brush.sweepGradient(listOf(accent.copy(alpha = 0.6f), accent, SuccessGreen, accent.copy(alpha = 0.6f))),
                startAngle = -90f, sweepAngle = sweepAnim, useCenter = false, topLeft = topLeft, size = sz, style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$level%", color = accent, fontSize = 54.sp, fontWeight = FontWeight.Bold)
            if (isCharging) {
                Text("⚡ Charging", color = accent.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Text("Battery", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

// ── Mini Sparkline Graph ───────────────────────────────────────────────────

@Composable
fun MiniGraph(values: List<Float>, modifier: Modifier = Modifier, accent: Color = Cyan) {
    val safeValues = if (values.isEmpty()) List(16) { (35..90).random().toFloat() } else values
    Canvas(modifier = modifier.fillMaxWidth().height(80.dp)) {
        val max = (safeValues.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val min = (safeValues.minOrNull() ?: 0f)
        val range = (max - min).coerceAtLeast(1f)
        val step = size.width / (safeValues.size - 1).coerceAtLeast(1)

        // Fill area under line
        val path = Path()
        path.moveTo(0f, size.height)
        safeValues.forEachIndexed { i, v ->
            val x = i * step
            val y = size.height - ((v - min) / range) * size.height * 0.85f
            if (i == 0) path.lineTo(x, y) else path.lineTo(x, y)
        }
        path.lineTo((safeValues.size - 1) * step, size.height)
        path.close()
        drawPath(path, brush = Brush.verticalGradient(listOf(accent.copy(alpha = 0.25f), Color.Transparent)))

        // Line
        for (i in 0 until safeValues.lastIndex) {
            val x1 = i * step
            val y1 = size.height - ((safeValues[i] - min) / range) * size.height * 0.85f
            val x2 = (i + 1) * step
            val y2 = size.height - ((safeValues[i + 1] - min) / range) * size.height * 0.85f
            drawLine(
                brush = Brush.linearGradient(listOf(PrimaryBlue, accent)),
                start = Offset(x1, y1), end = Offset(x2, y2),
                strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round
            )
        }

        // Dots at peaks
        val maxIdx = safeValues.indexOfFirst { it == safeValues.maxOrNull() }
        if (maxIdx >= 0) {
            val x = maxIdx * step
            val y = size.height - ((safeValues[maxIdx] - min) / range) * size.height * 0.85f
            drawCircle(color = accent, radius = 5.dp.toPx(), center = Offset(x, y))
        }
    }
}

// ── Setting Toggle ─────────────────────────────────────────────────────────

@Composable
fun SettingToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) Cyan.copy(alpha = 0.06f) else Color.Transparent)
            .padding(vertical = 11.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Cyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CardHighlight
            )
        )
    }
}

// ── Score Gauge ────────────────────────────────────────────────────────────

@Composable
fun ScoreGauge(label: String, score: Int, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BatteryRing(level = score, modifier = Modifier.size(130.dp))
        }
        Text(label, modifier = Modifier.align(Alignment.CenterHorizontally), color = SoftPrimary, fontWeight = FontWeight.SemiBold)
    }
}

// ── Circular Score Widget ──────────────────────────────────────────────────

@Composable
fun CircularScore(
    label: String,
    score: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val sweep by animateFloatAsState(
        targetValue = 360f * (score.coerceIn(0, 100) / 100f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 8.dp.toPx()
                val stroke = Stroke(strokeW, cap = StrokeCap.Round)
                val inset = 6.dp.toPx()
                val sz = Size(this.size.width - inset * 2, this.size.height - inset * 2)
                val topLeft = Offset(inset, inset)
                drawArc(color = CardHighlight, startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = sz, style = stroke)
                drawArc(brush = Brush.sweepGradient(listOf(accent.copy(alpha = 0.5f), accent)), startAngle = -90f, sweepAngle = sweep, useCenter = false, topLeft = topLeft, size = sz, style = stroke)
            }
            Text("$score", color = accent, fontSize = (size.value * 0.28f).sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ── Gradient Header Banner ─────────────────────────────────────────────────

@Composable
fun GradientBanner(
    title: String,
    subtitle: String,
    gradient: List<Color> = GradientPrimary,
    badge: String? = null,
    badgeColor: Color = SuccessGreen
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(Brush.linearGradient(gradient), RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                        .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(badge, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Health Bar ─────────────────────────────────────────────────────────────

@Composable
fun HealthBar(
    progress: Float,
    accent: Color = SuccessGreen,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "health"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(CardHighlight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.7f), accent)))
        )
    }
}

// ── Sensor Readout Card ────────────────────────────────────────────────────

@Composable
fun SensorReadoutCard(
    label: String,
    value: String,
    source: String,
    accent: Color,
    modifier: Modifier = Modifier,
    isLive: Boolean = false
) {
    Column(
        modifier = modifier
            .heightIn(min = 92.dp)
            .background(
                Brush.verticalGradient(listOf(CardDark, accent.copy(alpha = 0.06f))),
                RoundedCornerShape(22.dp)
            )
            .border(0.5.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (isLive) {
                val inf = rememberInfiniteTransition(label = "dot")
                val a by inf.animateFloat(0.3f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a")
                Box(modifier = Modifier.size(6.dp).background(accent.copy(alpha = a), CircleShape))
            }
            Text(label, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(value, color = accent, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(source, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
