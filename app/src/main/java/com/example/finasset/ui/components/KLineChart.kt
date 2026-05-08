package com.example.finasset.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finasset.ui.theme.*

data class ChartData(
    val dates: List<String>,
    val closes: List<Double>,
    val opens: List<Double> = emptyList(),
    val highs: List<Double> = emptyList(),
    val lows: List<Double> = emptyList(),
    val isCandlestick: Boolean = false
)

@Composable
fun KLineChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    lineColor: Color = Ink,
    redUpGreenDown: Boolean = true,
    title: String = ""
) {
    val density = LocalDensity.current
    Column(modifier = modifier) {
        if (title.isNotEmpty()) {
            Text(title, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            if (data.closes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("\u6682\u65E0\u6570\u636E", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            } else {
                val textSizePx = with(density) { 10.sp.toPx() }
                Canvas(modifier = Modifier.fillMaxSize().padding(start = 48.dp, end = 8.dp, top = 8.dp, bottom = 24.dp)) {
                    drawChart(data, lineColor, redUpGreenDown, textSizePx)
                }
            }
        }
    }
}

private fun DrawScope.drawChart(data: ChartData, lineColor: Color, redUpGreenDown: Boolean, textSizePx: Float) {
    val maxVal = if (data.isCandlestick) data.highs.maxOrNull() ?: 1.0 else data.closes.maxOrNull() ?: 1.0
    val minVal = if (data.isCandlestick) data.lows.minOrNull() ?: 0.0 else data.closes.minOrNull() ?: 0.0
    var range = (maxVal - minVal).coerceAtLeast(0.0001)
    if (range < maxVal * 0.001) range = maxVal * 0.02
    val n = data.closes.size
    if (n < 2) return
    val w = size.width; val h = size.height
    val barWidth = (w / n) * 0.6f

    val labelPaint = Paint().apply {
        color = 0xFF888888.toInt(); textSize = textSizePx; isAntiAlias = true
    }

    // Y轴标签（画在左侧 48dp padding 区域内）
    for (i in 0..3) {
        val y = h * i / 3
        drawLine(Color.LightGray.copy(alpha = 0.25f), Offset(0f, y), Offset(w, y), strokeWidth = 0.5f)
        val price = maxVal - range * i / 3
        labelPaint.textAlign = Paint.Align.RIGHT
        val label = if (range < 1) String.format("%.4f", price) else String.format("%.2f", price)
        drawContext.canvas.nativeCanvas.drawText(label, -6f, y + textSizePx / 3, labelPaint)
    }

    if (data.isCandlestick) {
        for (i in 0 until n) {
            val x = (i + 0.5f) * w / n
            val open = data.opens[i]; val close = data.closes[i]; val high = data.highs[i]; val low = data.lows[i]
            val isUp = close >= open
            val yHigh = ((maxVal - high) / range * h).toFloat()
            val yLow = ((maxVal - low) / range * h).toFloat()
            val yOpen = ((maxVal - open) / range * h).toFloat()
            val yClose = ((maxVal - close) / range * h).toFloat()
            val candleColor = if (isUp) { if (redUpGreenDown) RedUp else GreenUp } else { if (redUpGreenDown) GreenDown else RedDown }
            drawLine(candleColor, Offset(x, yHigh), Offset(x, yLow), strokeWidth = 1f)
            val bodyTop = minOf(yOpen, yClose); val bodyBottom = maxOf(yOpen, yClose)
            drawRect(candleColor, Offset(x - barWidth / 2, bodyTop), Size(barWidth, (bodyBottom - bodyTop).coerceAtLeast(1f)))
        }
    } else {
        // Line chart
        if (n == 1) {
            val y = ((maxVal - data.closes[0]) / range * h).toFloat()
            drawCircle(lineColor, radius = 4f, center = Offset(w / 2, y))
        } else {
            val path = Path(); val fillPath = Path()
            val sx = (0.5f) * w / n; val sy = ((maxVal - data.closes[0]) / range * h).toFloat()
            path.moveTo(sx, sy); fillPath.moveTo(sx, h); fillPath.lineTo(sx, sy)
            for (i in 1 until n) {
                val x = (i + 0.5f) * w / n
                val y = ((maxVal - data.closes[i]) / range * h).toFloat()
                path.lineTo(x, y); fillPath.lineTo(x, y)
            }
            fillPath.lineTo((n - 0.5f) * w / n, h); fillPath.close()
            drawPath(fillPath, lineColor.copy(alpha = 0.06f))
            drawPath(path, lineColor, style = Stroke(width = 2f, cap = StrokeCap.Round))
        }
    }

    // X轴日期标签
    labelPaint.textAlign = Paint.Align.CENTER; labelPaint.textSize = textSizePx * 0.85f
    val step = (n / 5).coerceAtLeast(1)
    for (i in 0 until n step step) {
        val x = (i + 0.5f) * w / n
        val ds = data.dates.getOrElse(i) { "" }
        val label = if (ds.length >= 10) ds.substring(5, 10) else ds
        drawContext.canvas.nativeCanvas.drawText(label, x, h + textSizePx + 2f, labelPaint)
    }
}
