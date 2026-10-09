package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.quant.QuantSignalReport
import com.example.domain.quant.QuantStrategyEngine
import com.example.ui.QuantUiState
import com.example.ui.theme.AlphaEmerald
import com.example.ui.theme.AlphaEmeraldDim
import com.example.ui.theme.BearishCrimson
import com.example.ui.theme.BearishCrimsonDim
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDim
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardElevated
import com.example.ui.theme.SovereignGold
import com.example.ui.theme.SovereignGoldDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RadarScannerScreen(
    uiState: QuantUiState,
    onRefreshLive: () -> Unit,
    onSelectTimeframe: (String) -> Unit,
    onInspectAssetInLab: (String) -> Unit,
    onDeploySignal: (QuantSignalReport) -> Unit,
    modifier: Modifier = Modifier
) {
    val topAlphaReport = uiState.liveReports.maxByOrNull { it.convictionScore }
    val syncTimeText = if (uiState.lastSyncedEpochMs > 0L) {
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(uiState.lastSyncedEpochMs))
    } else "Syncing..."

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("radar_scanner_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Executive Hero Header Card for Boss Krunal
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SovereignGold.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_quant_hero_banner_1791541861125),
                    contentDescription = "Krunal Capital Quant Desk Hero Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(225.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(225.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianBg.copy(alpha = 0.68f),
                                    ObsidianBg.copy(alpha = 0.88f),
                                    ObsidianBg.copy(alpha = 0.97f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SovereignGoldDim.copy(alpha = 0.9f))
                                .border(1.dp, SovereignGold.copy(alpha = 0.6f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SovereignGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "BOSS KRUNAL • EXECUTIVE PROP DESK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SovereignGold
                            )
                        }

                        // Live Feed Status Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(AlphaEmeraldDim)
                                .border(1.dp, AlphaEmerald.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AlphaEmerald)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${uiState.feedSourceLabel} • ${uiState.feedLatencyMs}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = AlphaEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Krunal Capital Quant Terminal",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Engineered for Boss Krunal by Aryan Verma (Employee #01 • Chief Quant Architect)",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricCyan
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Net Desk Equity & Live P&L Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MidnightSurface.copy(alpha = 0.92f))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NET COMPANY DESK EQUITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = String.format("$%,.2f", uiState.netDeskEquityUsd),
                                style = MaterialTheme.typography.headlineMedium,
                                color = SovereignGold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val totalPnl = uiState.realizedPnlUsd + uiState.unrealizedPnlUsd
                            val pnlSign = if (totalPnl >= 0) "+" else ""
                            Text(
                                text = "TOTAL DESK P&L (OPEN + CLOSED)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "$pnlSign${String.format("$%,.2f", totalPnl)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (totalPnl >= 0) AlphaEmerald else BearishCrimson
                            )
                        }
                    }
                }
            }
        }

        // 2. Timeframe Selector & Live Feed Sync Control Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("15m", "1h", "4h", "1d").forEach { tf ->
                        val selected = uiState.selectedTimeframe == tf
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectTimeframe(tf) },
                            label = {
                                Text(
                                    text = tf.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SovereignGold,
                                selectedLabelColor = Color(0xFF1A1300),
                                containerColor = MidnightSurface,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("timeframe_chip_$tf")
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "AUTO-LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = AlphaEmerald
                        )
                        Text(
                            text = syncTimeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onRefreshLive,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("refresh_live_feed_button")
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Live Market Data",
                                tint = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // 3. Top Institutional Confluence Spotlight Card ("Boss Krunal's #1 Live Trade")
        if (topAlphaReport != null) {
            item {
                TopAlphaSpotlightCard(
                    report = topAlphaReport,
                    onDeploySignal = { onDeploySignal(topAlphaReport) },
                    onOpenStrategyLab = { onInspectAssetInLab(topAlphaReport.symbol) }
                )
            }
        }

        // 4. Section Header for All Live Scanned Markets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE MULTI-ASSET CONFLUENCE RADAR",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time OHLCV + VWAP + EMA Ribbon + ATR(14) Stop scanner",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text = "${uiState.liveReports.size} PAIRS LIVE",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan
                )
            }
        }

        // 5. Live Market Cards
        items(uiState.liveReports, key = { it.symbol }) { report ->
            MarketSignalCard(
                report = report,
                onDeploySignal = { onDeploySignal(report) },
                onInspectInLab = { onInspectAssetInLab(report.symbol) }
            )
        }
    }
}

@Composable
private fun TopAlphaSpotlightCard(
    report: QuantSignalReport,
    onDeploySignal: () -> Unit,
    onOpenStrategyLab: () -> Unit
) {
    val isLong = report.direction == "LONG"
    val accentColor = if (isLong) AlphaEmerald else BearishCrimson
    val bgTint = if (isLong) AlphaEmeraldDim else BearishCrimsonDim

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(SlateCardElevated, MidnightSurface)
                )
            )
            .border(1.5.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .padding(16.dp)
            .testTag("top_alpha_spotlight_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = SovereignGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HIGHEST CONVICTION SETUP FOR BOSS KRUNAL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SovereignGold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(bgTint)
                    .border(1.dp, accentColor, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${report.direction} • ${report.convictionScore}% CONFLUENCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "${report.symbol} (${report.timeframe.uppercase()})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${report.assetName} • ${report.exchangeSource}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = QuantStrategyEngine.formatCompactPrice(report.currentPrice),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${QuantStrategyEngine.formatSignedPct(report.change24hPercent)} (24h)",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (report.change24hPercent >= 0) AlphaEmerald else BearishCrimson
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Exact Execution Matrix (Entry / Stop Loss / TP1 / TP2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianBg)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ExecutionLevelCol("ENTRY ZONE", QuantStrategyEngine.formatCompactPrice(report.entryPrice), ElectricCyan)
            ExecutionLevelCol("ATR STOP", QuantStrategyEngine.formatCompactPrice(report.stopLoss), BearishCrimson)
            ExecutionLevelCol("TARGET 1 (1.6R)", QuantStrategyEngine.formatCompactPrice(report.takeProfit1), AlphaEmerald)
            ExecutionLevelCol("TARGET 2 (3.2R)", QuantStrategyEngine.formatCompactPrice(report.takeProfit2), SovereignGold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = report.executiveSummary,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onDeploySignal,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color(0xFF041A0E)
                ),
                modifier = Modifier
                    .weight(1.25f)
                    .height(48.dp)
                    .testTag("deploy_spotlight_signal_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Deploy ${report.direction} (${String.format("$%,.0f", report.recommendedPositionUsd)})",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = onOpenStrategyLab,
                modifier = Modifier
                    .weight(0.85f)
                    .height(48.dp)
                    .testTag("inspect_spotlight_chart_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoGraph,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Live Chart",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
private fun MarketSignalCard(
    report: QuantSignalReport,
    onDeploySignal: () -> Unit,
    onInspectInLab: () -> Unit
) {
    val isLong = report.direction == "LONG"
    val sideColor = if (isLong) AlphaEmerald else BearishCrimson
    val sideBg = if (isLong) AlphaEmeraldDim else BearishCrimsonDim

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable { onInspectInLab() }
            .padding(14.dp)
            .testTag("market_card_${report.symbol}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(sideBg)
                        .border(1.dp, sideColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLong) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = sideColor
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = report.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(sideBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = report.action.shortTag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = sideColor
                            )
                        }
                    }
                    Text(
                        text = "${report.assetName} • RSI ${String.format("%.1f", report.rsiValue)} • Vol ${String.format("%.2fx", report.volumeSurgeRatio)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = QuantStrategyEngine.formatCompactPrice(report.currentPrice),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = QuantStrategyEngine.formatSignedPct(report.change24hPercent),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (report.change24hPercent >= 0) AlphaEmerald else BearishCrimson
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Confluence Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Apex Confluence Score: ${report.convictionScore}% (${report.direction})",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "WinRate ${String.format("%.0f%%", report.backtest.winRatePercent)} • PF ${String.format("%.2f", report.backtest.profitFactor)}",
                style = MaterialTheme.typography.labelSmall,
                color = SovereignGold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (report.convictionScore / 100f).coerceIn(0.1f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = sideColor,
            trackColor = ObsidianBg
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Levels + Quick Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExecutionLevelCol("ENTRY", QuantStrategyEngine.formatCompactPrice(report.entryPrice), ElectricCyan)
                ExecutionLevelCol("STOP", QuantStrategyEngine.formatCompactPrice(report.stopLoss), BearishCrimson)
                ExecutionLevelCol("TP1", QuantStrategyEngine.formatCompactPrice(report.takeProfit1), AlphaEmerald)
                ExecutionLevelCol("TP2", QuantStrategyEngine.formatCompactPrice(report.takeProfit2), SovereignGold)
            }

            Button(
                onClick = onDeploySignal,
                colors = ButtonDefaults.buttonColors(
                    containerColor = sideBg,
                    contentColor = sideColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("deploy_card_button_${report.symbol}")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Take Trade",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExecutionLevelCol(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
