package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.quant.QuantSignalReport
import com.example.domain.quant.QuantStrategyEngine
import com.example.ui.theme.AlphaEmerald
import com.example.ui.theme.BearishCrimson
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SovereignGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun InstitutionalCandlestickChart(
    report: QuantSignalReport,
    showTradeLevels: Boolean = true,
    showBollinger: Boolean = true,
    modifier: Modifier = Modifier
) {
    val candles = report.candles.takeLast(48)
    val offsetStart = (report.candles.size - candles.size).coerceAtLeast(0)
    val emaFast = report.emaFastSeries.drop(offsetStart)
    val emaMid = report.emaMidSeries.drop(offsetStart)
    val vwap = report.vwapSeries.drop(offsetStart)
    val bbUpper = report.bbUpperSeries.drop(offsetStart)
    val bbLower = report.bbLowerSeries.drop(offsetStart)
    val rsi = report.rsiSeries.drop(offsetStart)

    var selectedIndex by remember(report.symbol, report.timeframe, candles.size) {
        mutableStateOf<Int?>(null)
    }

    val activeIdx = selectedIndex?.coerceIn(0, (candles.size - 1).coerceAtLeast(0))
        ?: (candles.size - 1).coerceAtLeast(0)
    val activeCandle = candles.getOrNull(activeIdx)
    val activeRsi = rsi.getOrNull(activeIdx) ?: report.rsiValue
    val activeVwap = vwap.getOrNull(activeIdx) ?: report.vwapValue
    val timeFormatter = remember { SimpleDateFormat("dd MMM HH:mm", Locale.US) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianBg)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("institutional_candlestick_chart")
    ) {
        // Top OHLCV & Indicator Telemetry Strip
        if (activeCandle != null) {
            val isBull = activeCandle.close >= activeCandle.open
            val barColor = if (isBull) AlphaEmerald else BearishCrimson
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${report.symbol} • ${report.timeframe.uppercase()} • ${timeFormatter.format(Date(activeCandle.timestampSec * 1000L))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = if (selectedIndex != null) "INSPECTING CANDLE (TAP TO RESET)" else report.exchangeSource,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedIndex != null) SovereignGold else ElectricCyan
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryChip("O", QuantStrategyEngine.formatCompactPrice(activeCandle.open), TextPrimary)
                TelemetryChip("H", QuantStrategyEngine.formatCompactPrice(activeCandle.high), AlphaEmerald)
                TelemetryChip("L", QuantStrategyEngine.formatCompactPrice(activeCandle.low), BearishCrimson)
                TelemetryChip("C", QuantStrategyEngine.formatCompactPrice(activeCandle.close), barColor)
                TelemetryChip("RSI", String.format("%.1f", activeRsi), SovereignGold)
                TelemetryChip("VWAP", QuantStrategyEngine.formatCompactPrice(activeVwap), ElectricCyan)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChartLegendDot(color = ElectricCyan, label = "EMA Fast")
            ChartLegendDot(color = SovereignGold, label = "EMA Mid")
            ChartLegendDot(color = Color(0xFFB388FF), label = "VWAP")
            if (showTradeLevels) {
                ChartLegendDot(color = AlphaEmerald, label = "TP Target")
                ChartLegendDot(color = BearishCrimson, label = "ATR Stop")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Candlestick + Overlay Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MidnightSurface)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles.size) {
                        detectTapGestures { offset ->
                            if (candles.isNotEmpty() && size.width > 0) {
                                val idx = ((offset.x / size.width) * candles.size)
                                    .toInt()
                                    .coerceIn(0, candles.size - 1)
                                selectedIndex = if (selectedIndex == idx) null else idx
                            }
                        }
                    }
                    .pointerInput(candles.size) {
                        detectHorizontalDragGestures(
                            onDragEnd = { },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                if (candles.isNotEmpty() && size.width > 0) {
                                    val idx = ((change.position.x / size.width) * candles.size)
                                        .toInt()
                                        .coerceIn(0, candles.size - 1)
                                    selectedIndex = idx
                                }
                            }
                        )
                    }
            ) {
                if (candles.isEmpty()) return@Canvas
                val w = size.width
                val h = size.height
                val topPad = 14f
                val bottomPad = 36f // Leave bottom 36px for volume histogram
                val chartHeight = (h - topPad - bottomPad).coerceAtLeast(50f)

                var minPrice = candles.minOf { it.low }
                var maxPrice = candles.maxOf { it.high }
                if (showBollinger && bbUpper.isNotEmpty() && bbLower.isNotEmpty()) {
                    maxPrice = max(maxPrice, bbUpper.maxOrNull() ?: maxPrice)
                    minPrice = min(minPrice, bbLower.minOrNull() ?: minPrice)
                }
                if (showTradeLevels) {
                    maxPrice = max(maxPrice, max(report.takeProfit1, report.stopLoss))
                    minPrice = min(minPrice, min(report.takeProfit1, report.stopLoss))
                }
                val priceSpan = (maxPrice - minPrice).coerceAtLeast(maxPrice * 0.005)

                fun priceToY(p: Double): Float {
                    val norm = ((p - minPrice) / priceSpan).toFloat().coerceIn(0f, 1f)
                    return topPad + chartHeight * (1f - norm)
                }

                // Subtle horizontal grid lines
                val gridDash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                for (g in 0..4) {
                    val gy = topPad + (chartHeight / 4f) * g
                    drawLine(
                        color = BorderSubtle.copy(alpha = 0.45f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1f,
                        pathEffect = gridDash
                    )
                }

                val stepX = w / candles.size.toFloat()

                // Bollinger Channel Fill
                if (showBollinger && bbUpper.size == candles.size && bbLower.size == candles.size) {
                    val bbPath = Path()
                    for (i in candles.indices) {
                        val x = stepX * i + stepX * 0.5f
                        val y = priceToY(bbUpper[i])
                        if (i == 0) bbPath.moveTo(x, y) else bbPath.lineTo(x, y)
                    }
                    for (i in candles.indices.reversed()) {
                        val x = stepX * i + stepX * 0.5f
                        val y = priceToY(bbLower[i])
                        bbPath.lineTo(x, y)
                    }
                    bbPath.close()
                    drawPath(
                        path = bbPath,
                        color = ElectricCyan.copy(alpha = 0.06f)
                    )
                }

                // Volume Histogram at bottom
                val maxVol = candles.maxOf { it.volume }.coerceAtLeast(1.0)
                for (i in candles.indices) {
                    val c = candles[i]
                    val xCenter = stepX * i + stepX * 0.5f
                    val volNorm = (c.volume / maxVol).toFloat().coerceIn(0.08f, 1f)
                    val volBarH = (bottomPad - 6f) * volNorm
                    val isBull = c.close >= c.open
                    drawRect(
                        color = (if (isBull) AlphaEmerald else BearishCrimson).copy(alpha = 0.28f),
                        topLeft = Offset(xCenter - stepX * 0.32f, h - volBarH),
                        size = Size(stepX * 0.64f, volBarH)
                    )
                }

                // Candlesticks (Wick + Body)
                for (i in candles.indices) {
                    val c = candles[i]
                    val xCenter = stepX * i + stepX * 0.5f
                    val yHigh = priceToY(c.high)
                    val yLow = priceToY(c.low)
                    val yOpen = priceToY(c.open)
                    val yClose = priceToY(c.close)
                    val isBull = c.close >= c.open
                    val candleColor = if (isBull) AlphaEmerald else BearishCrimson

                    // Wick
                    drawLine(
                        color = candleColor.copy(alpha = 0.9f),
                        start = Offset(xCenter, yHigh),
                        end = Offset(xCenter, yLow),
                        strokeWidth = 1.4.dp.toPx()
                    )

                    // Body
                    val topBody = min(yOpen, yClose)
                    val bodyHeight = abs(yClose - yOpen).coerceAtLeast(2.2.dp.toPx())
                    val bodyWidth = (stepX * 0.66f).coerceAtLeast(2.5f)
                    drawRect(
                        color = candleColor,
                        topLeft = Offset(xCenter - bodyWidth / 2f, topBody),
                        size = Size(bodyWidth, bodyHeight)
                    )
                }

                // Helper to draw indicator lines
                fun drawIndicatorSeries(series: List<Double>, color: Color, strokeWidthDp: Float, dashed: Boolean = false) {
                    if (series.size < 2) return
                    val path = Path()
                    val count = min(candles.size, series.size)
                    for (i in 0 until count) {
                        val x = stepX * i + stepX * 0.5f
                        val y = priceToY(series[i])
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = strokeWidthDp.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f) else null
                        )
                    )
                }

                drawIndicatorSeries(vwap, Color(0xFFB388FF), 1.5f, dashed = true)
                drawIndicatorSeries(emaMid, SovereignGold, 1.6f)
                drawIndicatorSeries(emaFast, ElectricCyan, 1.8f)

                // Draw Institutional Execution Levels (Entry, Stop Loss, TP1)
                if (showTradeLevels) {
                    val levelDash = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                    val yEntry = priceToY(report.entryPrice)
                    val yStop = priceToY(report.stopLoss)
                    val yTp1 = priceToY(report.takeProfit1)

                    drawLine(
                        color = ElectricCyan.copy(alpha = 0.85f),
                        start = Offset(w * 0.55f, yEntry),
                        end = Offset(w, yEntry),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = levelDash
                    )
                    drawLine(
                        color = BearishCrimson.copy(alpha = 0.9f),
                        start = Offset(w * 0.55f, yStop),
                        end = Offset(w, yStop),
                        strokeWidth = 1.6.dp.toPx(),
                        pathEffect = levelDash
                    )
                    drawLine(
                        color = AlphaEmerald.copy(alpha = 0.95f),
                        start = Offset(w * 0.55f, yTp1),
                        end = Offset(w, yTp1),
                        strokeWidth = 1.8.dp.toPx(),
                        pathEffect = levelDash
                    )
                }

                // Crosshair for inspected candle
                val selIdx = selectedIndex
                if (selIdx != null && selIdx in candles.indices) {
                    val cx = stepX * selIdx + stepX * 0.5f
                    val cy = priceToY(candles[selIdx].close)
                    drawLine(
                        color = SovereignGold.copy(alpha = 0.75f),
                        start = Offset(cx, 0f),
                        end = Offset(cx, h),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                    drawCircle(
                        color = SovereignGold,
                        radius = 4.5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}

@Composable
fun BacktestEquityCurveChart(
    equityCurve: List<Double>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlateCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WALK-FORWARD EQUITY CURVE (BASE = 100.0)",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            val finalEq = equityCurve.lastOrNull() ?: 100.0
            val netGain = finalEq - 100.0
            Text(
                text = "${if (netGain >= 0) "+" else ""}${String.format("%.2f%%", netGain)} Net Alpha",
                style = MaterialTheme.typography.labelLarge,
                color = if (netGain >= 0) AlphaEmerald else BearishCrimson
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
        ) {
            if (equityCurve.size < 2) return@Canvas
            val w = size.width
            val h = size.height
            val minVal = (equityCurve.minOrNull() ?: 95.0) - 0.5
            val maxVal = (equityCurve.maxOrNull() ?: 110.0) + 0.5
            val span = (maxVal - minVal).coerceAtLeast(1.0)

            val linePath = Path()
            val fillPath = Path()

            equityCurve.forEachIndexed { idx, value ->
                val x = (idx.toFloat() / (equityCurve.size - 1).toFloat()) * w
                val normY = ((value - minVal) / span).toFloat().coerceIn(0f, 1f)
                val y = h - normY * (h - 12f) - 6f
                if (idx == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo(w, h)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(AlphaEmerald.copy(alpha = 0.30f), Color.Transparent)
                )
            )
            drawPath(
                path = linePath,
                color = AlphaEmerald,
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun TelemetryChip(label: String, value: String, valueColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
