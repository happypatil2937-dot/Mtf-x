package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.StrategyConfigEntity
import com.example.domain.quant.FactorBias
import com.example.domain.quant.IndicatorFactor
import com.example.domain.quant.QuantSignalReport
import com.example.domain.quant.QuantStrategyEngine
import com.example.ui.QuantUiState
import com.example.ui.components.BacktestEquityCurveChart
import com.example.ui.components.InstitutionalCandlestickChart
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

@Composable
fun StrategyChartScreen(
    uiState: QuantUiState,
    onSelectAsset: (String) -> Unit,
    onSelectTimeframe: (String) -> Unit,
    onDeploySignalWithLeverage: (QuantSignalReport, Double, Double) -> Unit,
    onSaveStrategyConfig: (StrategyConfigEntity) -> Unit,
    onNavigateBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBackHome()
    }

    val report = uiState.selectedReport
    var selectedLeverage by remember { mutableDoubleStateOf(2.0) }
    var showTuneDialog by remember { mutableStateOf(false) }

    if (showTuneDialog) {
        TuneStrategyConfigDialog(
            currentConfig = uiState.strategyConfig,
            onDismiss = { showTuneDialog = false },
            onConfirm = { updated ->
                onSaveStrategyConfig(updated)
                showTuneDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("strategy_lab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Architect Attribution & Strategy Engine Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SlateCardElevated)
                    .border(1.dp, SovereignGold.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = SovereignGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = uiState.strategyConfig.strategyTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "Chief Quant Architect: ${uiState.strategyConfig.chiefQuantName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SovereignGold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showTuneDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("tune_strategy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Tune Strategy Parameters",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tune",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        // 2. Asset Pair & Timeframe Selector Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.liveReports.forEach { itemReport ->
                        val isSel = itemReport.symbol == uiState.selectedSymbol
                        FilterChip(
                            selected = isSel,
                            onClick = { onSelectAsset(itemReport.symbol) },
                            label = {
                                Text(
                                    text = "${itemReport.symbol} (${itemReport.convictionScore}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan,
                                selectedLabelColor = Color(0xFF002026),
                                containerColor = MidnightSurface,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("lab_asset_chip_${itemReport.symbol}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CANDLE TIMEFRAME:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("15m", "1h", "4h", "1d").forEach { tf ->
                            FilterChip(
                                selected = uiState.selectedTimeframe == tf,
                                onClick = { onSelectTimeframe(tf) },
                                label = {
                                    Text(
                                        text = tf.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SovereignGold,
                                    selectedLabelColor = Color(0xFF1E1400),
                                    containerColor = MidnightSurface,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        if (report != null) {
            // 3. Interactive Candlestick + EMA/VWAP/Bollinger/Execution Levels Chart
            item {
                InstitutionalCandlestickChart(
                    report = report,
                    showTradeLevels = true,
                    showBollinger = true
                )
            }

            // 4. Institutional Trade Execution Plan Card
            item {
                val isLong = report.direction == "LONG"
                val dirColor = if (isLong) AlphaEmerald else BearishCrimson
                val dirBg = if (isLong) AlphaEmeraldDim else BearishCrimsonDim

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateCard)
                        .border(1.5.dp, dirColor.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "INSTITUTIONAL TRADE TICKET • ${report.symbol}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SovereignGold
                            )
                            Text(
                                text = report.action.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = dirColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(dirBg)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${report.convictionScore}/100",
                                style = MaterialTheme.typography.titleLarge,
                                color = dirColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LevelStatItem("ENTRY", QuantStrategyEngine.formatCompactPrice(report.entryPrice), ElectricCyan)
                        LevelStatItem("STOP (ATR)", QuantStrategyEngine.formatCompactPrice(report.stopLoss), BearishCrimson)
                        LevelStatItem("TP1 (1.6R)", QuantStrategyEngine.formatCompactPrice(report.takeProfit1), AlphaEmerald)
                        LevelStatItem("TP2 (3.2R)", QuantStrategyEngine.formatCompactPrice(report.takeProfit2), SovereignGold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Leverage Selector & Recommended Capital
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DESK POSITION SIZE (${uiState.strategyConfig.riskPerTradePercent}% RISK)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${String.format("$%,.0f", report.recommendedPositionUsd)} Margin • Max Risk ${String.format("$%,.0f", report.riskDollarAmount)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1.0, 2.0, 3.0, 5.0).forEach { lev ->
                                val active = selectedLeverage == lev
                                FilterChip(
                                    selected = active,
                                    onClick = { selectedLeverage = lev },
                                    label = {
                                        Text(
                                            text = "${lev.toInt()}x",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SovereignGold,
                                        selectedLabelColor = Color.Black,
                                        containerColor = MidnightSurface,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            onDeploySignalWithLeverage(
                                report,
                                report.recommendedPositionUsd,
                                selectedLeverage
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = dirColor,
                            contentColor = Color(0xFF041A0E)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("lab_deploy_signal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deploy ${report.direction} ${report.symbol} (${selectedLeverage.toInt()}x) to Company Ledger",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // 5. Walk-Forward Strategy Backtest Card & Equity Curve
            item {
                val bt = report.backtest
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = AlphaEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WALK-FORWARD BACKTEST AUDIT (${report.symbol} ${report.timeframe.uppercase()})",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${bt.totalSignals} Signals",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LevelStatItem("WIN RATE", String.format("%.1f%%", bt.winRatePercent), AlphaEmerald)
                        LevelStatItem("PROFIT FACTOR", String.format("%.2f", bt.profitFactor), SovereignGold)
                        LevelStatItem("EXPECTANCY", String.format("+%.2fR", bt.expectancyR), ElectricCyan)
                        LevelStatItem("MAX DRAWDOWN", String.format("-%.1f%%", bt.maxDrawdownPercent), BearishCrimson)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    BacktestEquityCurveChart(equityCurve = bt.equityCurve)
                }
            }

            // 6. 7-Pillar Institutional Confluence Breakdown
            item {
                Text(
                    text = "7-PILLAR INSTITUTIONAL CONFLUENCE MATRIX",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            items(report.factors, key = { it.pillarTitle }) { factor ->
                ConfluencePillarRow(factor = factor)
            }
        }
    }
}

@Composable
private fun ConfluencePillarRow(factor: IndicatorFactor) {
    val (badgeColor, badgeBg, badgeLabel) = when (factor.bias) {
        FactorBias.BULLISH -> Triple(AlphaEmerald, AlphaEmeraldDim, "BULLISH")
        FactorBias.BEARISH -> Triple(BearishCrimson, BearishCrimsonDim, "BEARISH")
        FactorBias.NEUTRAL -> Triple(SovereignGold, SovereignGoldDim, "NEUTRAL")
    }

    Column(
        modifier = Modifier
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
                text = factor.pillarTitle,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${factor.weightPoints}/${factor.maxPoints} pts",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Live Reading: ${factor.metricValue}",
            style = MaterialTheme.typography.labelMedium,
            color = ElectricCyan
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = factor.detailNote,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (factor.weightPoints.toFloat() / factor.maxPoints.toFloat()).coerceIn(0.1f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50)),
            color = badgeColor,
            trackColor = ObsidianBg
        )
    }
}

@Composable
private fun LevelStatItem(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
private fun TuneStrategyConfigDialog(
    currentConfig: StrategyConfigEntity,
    onDismiss: () -> Unit,
    onConfirm: (StrategyConfigEntity) -> Unit
) {
    var deskCapitalText by remember { mutableStateOf(currentConfig.deskCapitalUsd.toInt().toString()) }
    var riskPctText by remember { mutableStateOf(currentConfig.riskPerTradePercent.toString()) }
    var emaFastText by remember { mutableStateOf(currentConfig.emaFastPeriod.toString()) }
    var emaMidText by remember { mutableStateOf(currentConfig.emaMidPeriod.toString()) }
    var atrMultText by remember { mutableStateOf(currentConfig.atrStopMultiplier.toString()) }
    var minScoreText by remember { mutableStateOf(currentConfig.minConfluenceScore.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tune Apex Confluence v4.2",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = deskCapitalText,
                    onValueChange = { deskCapitalText = it },
                    label = { Text("Krunal Desk Capital (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = riskPctText,
                        onValueChange = { riskPctText = it },
                        label = { Text("Risk % / Trade") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = atrMultText,
                        onValueChange = { atrMultText = it },
                        label = { Text("ATR Stop Mult") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emaFastText,
                        onValueChange = { emaFastText = it },
                        label = { Text("Fast EMA") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = emaMidText,
                        onValueChange = { emaMidText = it },
                        label = { Text("Mid EMA") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minScoreText,
                        onValueChange = { minScoreText = it },
                        label = { Text("Min Score") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = currentConfig.copy(
                        deskCapitalUsd = deskCapitalText.toDoubleOrNull()?.coerceAtLeast(1000.0) ?: currentConfig.deskCapitalUsd,
                        riskPerTradePercent = riskPctText.toDoubleOrNull()?.coerceIn(0.25, 10.0) ?: currentConfig.riskPerTradePercent,
                        emaFastPeriod = emaFastText.toIntOrNull()?.coerceIn(3, 50) ?: currentConfig.emaFastPeriod,
                        emaMidPeriod = emaMidText.toIntOrNull()?.coerceIn(8, 100) ?: currentConfig.emaMidPeriod,
                        atrStopMultiplier = atrMultText.toDoubleOrNull()?.coerceIn(0.8, 5.0) ?: currentConfig.atrStopMultiplier,
                        minConfluenceScore = minScoreText.toIntOrNull()?.coerceIn(50, 90) ?: currentConfig.minConfluenceScore
                    )
                    onConfirm(updated)
                },
                modifier = Modifier.testTag("save_strategy_config_button")
            ) {
                Text("Save to Desk DB")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
