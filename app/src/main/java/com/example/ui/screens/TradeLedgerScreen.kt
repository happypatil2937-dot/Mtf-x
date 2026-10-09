package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TradePositionEntity
import com.example.domain.quant.QuantStrategyEngine
import com.example.ui.QuantUiState
import com.example.ui.theme.AlphaEmerald
import com.example.ui.theme.AlphaEmeraldDim
import com.example.ui.theme.BearishCrimson
import com.example.ui.theme.BearishCrimsonDim
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
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
fun TradeLedgerScreen(
    uiState: QuantUiState,
    onSetFilter: (String) -> Unit,
    onLogManualPosition: (
        symbol: String,
        assetName: String,
        side: String,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit1: Double,
        takeProfit2: Double,
        positionSizeUsd: Double,
        leverage: Double,
        confluenceScore: Int,
        notes: String
    ) -> Unit,
    onCloseAtLiveMarket: (TradePositionEntity) -> Unit,
    onDeleteTrade: (Long) -> Unit,
    onNavigateBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBackHome()
    }

    var showAddModal by remember { mutableStateOf(false) }

    val filteredTrades = remember(uiState.trades, uiState.tradeFilter) {
        when (uiState.tradeFilter) {
            "OPEN" -> uiState.trades.filter { it.status == "OPEN" }
            "CLOSED" -> uiState.trades.filter { it.status == "CLOSED" }
            else -> uiState.trades
        }
    }

    if (showAddModal) {
        val defaultSymbol = uiState.selectedSymbol
        val defaultPrice = uiState.livePriceMap[defaultSymbol] ?: 89400.0
        NewPositionModal(
            initialSymbol = defaultSymbol,
            initialPrice = defaultPrice,
            onDismiss = { showAddModal = false },
            onSave = { sym, side, entry, sl, tp1, tp2, sizeUsd, lev, notes ->
                onLogManualPosition(
                    sym,
                    "$sym Institutional Position",
                    side,
                    entry,
                    sl,
                    tp1,
                    tp2,
                    sizeUsd,
                    lev,
                    85,
                    notes
                )
                showAddModal = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("trade_ledger_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Desk Trade Book Header & P&L Telemetry
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SlateCardElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KRUNAL CAPITAL • MASTER TRADE BOOK",
                            style = MaterialTheme.typography.labelMedium,
                            color = SovereignGold
                        )
                        Text(
                            text = "Persistent SQLite Position & P&L Ledger",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = { showAddModal = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SovereignGold,
                            contentColor = Color(0xFF1A1300)
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_manual_trade_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Trade",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianBg)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val uPnl = uiState.unrealizedPnlUsd
                    val rPnl = uiState.realizedPnlUsd
                    LedgerMetricItem(
                        label = "OPEN POSITIONS (${uiState.openTrades.size})",
                        value = "${if (uPnl >= 0) "+" else ""}${String.format("$%,.2f", uPnl)}",
                        color = if (uPnl >= 0) AlphaEmerald else BearishCrimson
                    )
                    LedgerMetricItem(
                        label = "REALIZED P&L (${uiState.closedTrades.size})",
                        value = "${if (rPnl >= 0) "+" else ""}${String.format("$%,.2f", rPnl)}",
                        color = if (rPnl >= 0) SovereignGold else BearishCrimson
                    )
                    val activeMargin = uiState.openTrades.sumOf { it.positionSizeUsd }
                    LedgerMetricItem(
                        label = "ACTIVE MARGIN",
                        value = String.format("$%,.0f", activeMargin),
                        color = ElectricCyan
                    )
                }
            }
        }

        // 2. Filter Bar (ALL / OPEN / CLOSED)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "ALL" to "All (${uiState.trades.size})",
                    "OPEN" to "Open (${uiState.openTrades.size})",
                    "CLOSED" to "Closed (${uiState.closedTrades.size})"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = uiState.tradeFilter == key,
                        onClick = { onSetFilter(key) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = Color(0xFF002026),
                            containerColor = SlateCard,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("ledger_filter_$key")
                    )
                }
            }
        }

        // 3. Empty State or Position Cards
        if (filteredTrades.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = SovereignGold,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(id = R.string.empty_trades_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(filteredTrades, key = { it.id }) { trade ->
                val liveMark = uiState.livePriceMap[trade.symbol] ?: trade.entryPrice
                TradePositionCard(
                    trade = trade,
                    liveMarkPrice = liveMark,
                    onCloseAtMarket = { onCloseAtLiveMarket(trade) },
                    onDelete = { onDeleteTrade(trade.id) }
                )
            }
        }
    }
}

@Composable
private fun TradePositionCard(
    trade: TradePositionEntity,
    liveMarkPrice: Double,
    onCloseAtMarket: () -> Unit,
    onDelete: () -> Unit
) {
    val isOpen = trade.status == "OPEN"
    val isLong = trade.side == "LONG"
    val sideColor = if (isLong) AlphaEmerald else BearishCrimson
    val sideBg = if (isLong) AlphaEmeraldDim else BearishCrimsonDim

    val markOrExit = if (isOpen) liveMarkPrice else (trade.exitPrice ?: trade.entryPrice)
    val rawMovePct = if (isLong) {
        ((markOrExit - trade.entryPrice) / trade.entryPrice) * 100.0
    } else {
        ((trade.entryPrice - markOrExit) / trade.entryPrice) * 100.0
    }
    val leveredMovePct = rawMovePct * trade.leverage
    val pnlUsd = if (isOpen) {
        trade.positionSizeUsd * (rawMovePct / 100.0) * trade.leverage
    } else {
        trade.realizedPnlUsd ?: (trade.positionSizeUsd * (rawMovePct / 100.0) * trade.leverage)
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM HH:mm", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCard)
            .border(
                width = 1.dp,
                color = if (isOpen) sideColor.copy(alpha = 0.55f) else BorderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("trade_position_card_${trade.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(sideBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${trade.side} ${trade.leverage}x",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = sideColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = trade.symbol,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${trade.strategyName} • Score ${trade.confluenceScore}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val pnlSign = if (pnlUsd >= 0) "+" else ""
                Text(
                    text = "$pnlSign${String.format("$%,.2f", pnlUsd)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (pnlUsd >= 0) AlphaEmerald else BearishCrimson
                )
                Text(
                    text = "${if (leveredMovePct >= 0) "+" else ""}${String.format("%.2f%%", leveredMovePct)} • ${trade.status}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOpen) ElectricCyan else SovereignGold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianBg)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LedgerMetricItem("SIZE", String.format("$%,.0f", trade.positionSizeUsd), TextPrimary)
            LedgerMetricItem("ENTRY", QuantStrategyEngine.formatCompactPrice(trade.entryPrice), ElectricCyan)
            LedgerMetricItem(
                if (isOpen) "LIVE MARK" else "EXIT PRICE",
                QuantStrategyEngine.formatCompactPrice(markOrExit),
                if (pnlUsd >= 0) AlphaEmerald else BearishCrimson
            )
            LedgerMetricItem("STOP", QuantStrategyEngine.formatCompactPrice(trade.stopLoss), BearishCrimson)
            LedgerMetricItem("TP2", QuantStrategyEngine.formatCompactPrice(trade.takeProfit2), SovereignGold)
        }

        if (trade.thesisNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = trade.thesisNotes,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Opened: ${dateFormatter.format(Date(trade.openedAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOpen) {
                    Button(
                        onClick = onCloseAtMarket,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SovereignGoldDim,
                            contentColor = SovereignGold
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("close_trade_button_${trade.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(id = R.string.close_at_market),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_trade_button_${trade.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Trade",
                        tint = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerMetricItem(label: String, value: String, color: Color) {
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

@Composable
private fun NewPositionModal(
    initialSymbol: String,
    initialPrice: Double,
    onDismiss: () -> Unit,
    onSave: (
        symbol: String,
        side: String,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit1: Double,
        takeProfit2: Double,
        positionSizeUsd: Double,
        leverage: Double,
        notes: String
    ) -> Unit
) {
    var symbol by remember { mutableStateOf(initialSymbol) }
    var side by remember { mutableStateOf("LONG") }
    var entryText by remember { mutableStateOf(String.format(Locale.US, "%.2f", initialPrice)) }
    var slText by remember { mutableStateOf(String.format(Locale.US, "%.2f", initialPrice * 0.98)) }
    var tp1Text by remember { mutableStateOf(String.format(Locale.US, "%.2f", initialPrice * 1.03)) }
    var tp2Text by remember { mutableStateOf(String.format(Locale.US, "%.2f", initialPrice * 1.06)) }
    var sizeUsdText by remember { mutableStateOf("20000") }
    var leverageText by remember { mutableStateOf("2.0") }
    var notes by remember { mutableStateOf("Boss Krunal Desk Execution • Institutional Confluence") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Position to Company Ledger",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LONG", "SHORT").forEach { option ->
                        FilterChip(
                            selected = side == option,
                            onClick = { side = option },
                            label = { Text(option, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (option == "LONG") AlphaEmerald else BearishCrimson,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it.uppercase() },
                    label = { Text("Asset Pair (e.g. BTC/USD)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_trade_symbol_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = entryText,
                        onValueChange = { entryText = it },
                        label = { Text("Entry Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sizeUsdText,
                        onValueChange = { sizeUsdText = it },
                        label = { Text("Size (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = slText,
                        onValueChange = { slText = it },
                        label = { Text("Stop Loss") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tp2Text,
                        onValueChange = { tp2Text = it },
                        label = { Text("Target TP2") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Trade Thesis / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entry = entryText.toDoubleOrNull() ?: initialPrice
                    val sl = slText.toDoubleOrNull() ?: (entry * 0.98)
                    val tp1 = tp1Text.toDoubleOrNull() ?: (entry * 1.03)
                    val tp2 = tp2Text.toDoubleOrNull() ?: (entry * 1.06)
                    val sizeUsd = sizeUsdText.toDoubleOrNull() ?: 15000.0
                    val lev = leverageText.toDoubleOrNull() ?: 2.0
                    onSave(
                        symbol.ifBlank { "BTC/USD" },
                        side,
                        entry,
                        sl,
                        tp1,
                        tp2,
                        sizeUsd,
                        lev,
                        notes
                    )
                },
                modifier = Modifier.testTag("confirm_save_trade_button")
            ) {
                Text("Save Position")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
