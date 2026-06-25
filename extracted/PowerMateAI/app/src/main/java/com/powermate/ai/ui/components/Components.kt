package com.powermate.ai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powermate.ai.ui.theme.CardDark
import com.powermate.ai.ui.theme.CardElevated
import com.powermate.ai.ui.theme.Cyan
import com.powermate.ai.ui.theme.PrimaryBlue
import com.powermate.ai.ui.theme.SoftPrimary
import com.powermate.ai.ui.theme.SuccessGreen
import com.powermate.ai.ui.theme.TextMain
import com.powermate.ai.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
fun MetricCard(title: String, value: String, caption: String, modifier: Modifier = Modifier, accent: Color = Cyan, fontScale: Float = 1.0f) {
    SectionCard(modifier = modifier) {
        Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(value, color = TextMain, fontSize = (22 * fontScale).sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(caption, color = accent, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color = SuccessGreen,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 54.dp)
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        modifier = modifier.height(52.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = TextMain),
        shape = RoundedCornerShape(18.dp)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun BatteryRing(level: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            val size = Size(this.size.width - 28.dp.toPx(), this.size.height - 28.dp.toPx())
            val topLeft = Offset(14.dp.toPx(), 14.dp.toPx())
            drawArc(
                color = CardElevated,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = stroke
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(PrimaryBlue, Cyan, SuccessGreen, PrimaryBlue)),
                startAngle = -90f,
                sweepAngle = 360f * (level.coerceIn(0, 100) / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$level%", color = TextMain, fontSize = 54.sp, fontWeight = FontWeight.Bold)
            Text("Battery", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun MiniGraph(values: List<Float>, modifier: Modifier = Modifier) {
    if (values.size < 2) return
    Canvas(modifier = modifier.fillMaxWidth().height(80.dp)) {
        val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val step = size.width / (values.size - 1).coerceAtLeast(1)
        for (i in 0 until values.lastIndex) {
            val x1 = i * step
            val y1 = size.height - (values[i] / max) * size.height
            val x2 = (i + 1) * step
            val y2 = size.height - (values[i + 1] / max) * size.height
            drawLine(
                brush = Brush.linearGradient(listOf(PrimaryBlue, Cyan)),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SettingToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ScoreGauge(label: String, score: Int, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BatteryRing(level = score, modifier = Modifier.size(130.dp))
        }
        Text(label, modifier = Modifier.align(Alignment.CenterHorizontally), color = SoftPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BatteryRingColored(level: Int, accent: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            val size = Size(this.size.width - 28.dp.toPx(), this.size.height - 28.dp.toPx())
            val topLeft = Offset(14.dp.toPx(), 14.dp.toPx())
            drawArc(
                color = CardElevated,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = stroke
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(accent.copy(alpha = 0.7f), accent, SuccessGreen, accent.copy(alpha = 0.7f))),
                startAngle = -90f,
                sweepAngle = 360f * (level.coerceIn(0, 100) / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$level%", color = accent, fontSize = 54.sp, fontWeight = FontWeight.Bold)
            Text("Battery", color = TextSecondary, fontSize = 14.sp)
        }
    }
}
