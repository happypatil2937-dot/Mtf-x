package com.example.data.repository

import com.example.data.local.CompanyRecordEntity
import com.example.data.local.KrunalQuantDao
import com.example.data.local.StrategyConfigEntity
import com.example.data.local.TradePositionEntity
import com.example.data.remote.MarketDataService
import com.example.domain.quant.QuantSignalReport
import com.example.domain.quant.QuantStrategyEngine
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KrunalQuantRepository(
    private val dao: KrunalQuantDao,
    private val marketService: MarketDataService = MarketDataService()
) {
    val allTradesFlow: Flow<List<TradePositionEntity>> = dao.observeAllTrades()
    val allCompanyRecordsFlow: Flow<List<CompanyRecordEntity>> = dao.observeAllCompanyRecords()
    val strategyConfigFlow: Flow<StrategyConfigEntity?> = dao.observeStrategyConfig()

    suspend fun ensureSeeded() {
        val currentConfig = dao.getStrategyConfigOnce()
        if (currentConfig == null) {
            dao.upsertStrategyConfig(StrategyConfigEntity())
        }

        if (dao.getCompanyRecordCount() == 0) {
            val now = System.currentTimeMillis()
            val initialRecords = listOf(
                CompanyRecordEntity(
                    title = "Krunal Capital Master Prop Treasury Allocation",
                    category = "TREASURY",
                    amountUsd = 250000.0,
                    metricLabel = "$250,000.00 Core AUM",
                    content = "Primary proprietary desk liquidity allocated by Boss Krunal for high-probability multi-factor institutional setups (BTC, ETH, SOL, AVAX). Max single-position margin capped at 25% of desk equity.",
                    author = "Aryan Verma (Employee #01 — Chief Quant)",
                    isPinned = true,
                    updatedAt = now - 3600_000L * 12
                ),
                CompanyRecordEntity(
                    title = "Apex Institutional Confluence v4.2 — Desk Mandate",
                    category = "PROP_RULES",
                    amountUsd = 3750.0,
                    metricLabel = "1.5% Max Risk / Trade",
                    content = "Golden Rule for Boss Krunal's Desk: Only deploy capital when Confluence Score >= 68% across EMA Ribbon (9/21/50), Session VWAP anchor, Wilder's RSI(14), and ATR(14) volatility stops. Mandatory 1.6R TP1 & 3.2R TP2.",
                    author = "Aryan Verma (Employee #01 — Chief Quant)",
                    isPinned = true,
                    updatedAt = now - 3600_000L * 8
                ),
                CompanyRecordEntity(
                    title = "Q3 Institutional Quant Audit & Alpha Attribution",
                    category = "AUDIT",
                    amountUsd = 42850.0,
                    metricLabel = "+17.14% Net Desk Alpha",
                    content = "Verified walk-forward execution audit for Krunal Capital Desk. Win rate across 1H and 4H VWAP + EMA Ribbon breakouts held at 74.2% with 2.68 Profit Factor and max drawdown contained under 4.1%.",
                    author = "Aryan Verma (Employee #01 — Chief Quant)",
                    isPinned = false,
                    updatedAt = now - 3600_000L * 5
                ),
                CompanyRecordEntity(
                    title = "Direct Exchange Liquidity & Zero-Subscription Feed Architecture",
                    category = "BROKER_DESK",
                    amountUsd = 150000.0,
                    metricLabel = "Kraken + Binance Direct",
                    content = "Configured zero-subscription institutional REST orderflow aggregation directly for Boss Krunal. Automatically collects live OHLCV candles, 24h volume surges, and real-time mark prices with automatic failover.",
                    author = "Aryan Verma (Employee #01 — Chief Quant)",
                    isPinned = false,
                    updatedAt = now - 3600_000L * 2
                ),
                CompanyRecordEntity(
                    title = "Executive Directive — Boss Krunal's Scaling Protocol",
                    category = "DIRECTIVE",
                    amountUsd = 50000.0,
                    metricLabel = "Compound @ +10% Milestones",
                    content = "Whenever cumulative realized desk P&L crosses +$25,000 milestones, sweep 40% into Krunal Capital Cold Reserve Vault and compound 60% into active quant position sizing.",
                    author = "Boss Krunal • Founder & Managing Director",
                    isPinned = true,
                    updatedAt = now - 1800_000L
                )
            )
            dao.insertCompanyRecords(initialRecords)
        }

        if (dao.getTradeCount() == 0) {
            val now = System.currentTimeMillis()
            val seedTrades = listOf(
                TradePositionEntity(
                    symbol = "BTC/USD",
                    assetName = "Bitcoin Institutional Spot",
                    side = "LONG",
                    entryPrice = 87650.0,
                    stopLoss = 85900.0,
                    takeProfit1 = 90450.0,
                    takeProfit2 = 93250.0,
                    positionSizeUsd = 28000.0,
                    leverage = 2.0,
                    strategyName = "Apex Confluence v4.2 (1h)",
                    confluenceScore = 88,
                    status = "OPEN",
                    thesisNotes = "Full EMA 9/21/50 bull stack + defending above rolling institutional VWAP with 1.42x volume surge.",
                    openedAt = now - 3600_000L * 6
                ),
                TradePositionEntity(
                    symbol = "SOL/USD",
                    assetName = "Solana High-Frequency L1",
                    side = "LONG",
                    entryPrice = 188.40,
                    stopLoss = 182.10,
                    takeProfit1 = 198.50,
                    takeProfit2 = 208.50,
                    positionSizeUsd = 16500.0,
                    leverage = 2.0,
                    strategyName = "Apex Confluence v4.2 (1h)",
                    confluenceScore = 84,
                    status = "OPEN",
                    thesisNotes = "Bollinger-Keltner volatility squeeze breakout above VWAP anchor; RSI(14) holding 61.5 bull regime.",
                    openedAt = now - 3600_000L * 4
                ),
                TradePositionEntity(
                    symbol = "ETH/USD",
                    assetName = "Ethereum Smart-Contract Layer",
                    side = "LONG",
                    entryPrice = 3040.0,
                    stopLoss = 2965.0,
                    takeProfit1 = 3160.0,
                    takeProfit2 = 3280.0,
                    positionSizeUsd = 22000.0,
                    leverage = 1.5,
                    strategyName = "Apex Confluence v4.2 (4h)",
                    confluenceScore = 82,
                    status = "CLOSED",
                    exitPrice = 3178.50,
                    realizedPnlUsd = 1503.45,
                    thesisNotes = "Closed at TP1+ after clean institutional VWAP reclaim and positive MACD histogram impulse.",
                    openedAt = now - 3600_000L * 28,
                    closedAt = now - 3600_000L * 9
                ),
                TradePositionEntity(
                    symbol = "AVAX/USD",
                    assetName = "Avalanche Institutional Subnet",
                    side = "SHORT",
                    entryPrice = 38.90,
                    stopLoss = 40.40,
                    takeProfit1 = 36.50,
                    takeProfit2 = 34.10,
                    positionSizeUsd = 12000.0,
                    leverage = 1.5,
                    strategyName = "Apex Confluence v4.2 (1h)",
                    confluenceScore = 79,
                    status = "CLOSED",
                    exitPrice = 36.45,
                    realizedPnlUsd = 1133.68,
                    thesisNotes = "Overbought RSI exhaustion rejection below EMA21 + distribution candle.",
                    openedAt = now - 3600_000L * 46,
                    closedAt = now - 3600_000L * 19
                )
            )
            dao.insertTrades(seedTrades)
        }
    }

    suspend fun fetchLiveQuantReports(
        selectedSymbol: String,
        timeframe: String,
        config: StrategyConfigEntity
    ): Pair<List<QuantSignalReport>, Long> {
        val snapshots = marketService.fetchDeskSnapshots(selectedSymbol, timeframe)
        val avgLatency = if (snapshots.isNotEmpty()) {
            snapshots.map { it.latencyMs }.average().toLong()
        } else 45L

        val reports = snapshots.map { snap ->
            QuantStrategyEngine.analyzeMarket(
                symbol = snap.symbol,
                assetName = snap.assetName,
                timeframe = timeframe,
                candles = snap.candles,
                change24hPercent = snap.change24hPercent,
                high24h = snap.high24h,
                low24h = snap.low24h,
                volume24hUsd = snap.volume24hUsd,
                exchangeSource = snap.exchangeSource,
                config = config
            )
        }
        return reports to avgLatency
    }

    suspend fun deploySignalToLedger(
        report: QuantSignalReport,
        customSizeUsd: Double? = null,
        customLeverage: Double = 1.5
    ): Long {
        val posSize = customSizeUsd ?: report.recommendedPositionUsd
        val topFactors = report.factors
            .sortedByDescending { it.weightPoints }
            .take(3)
            .joinToString(" | ") { "${it.pillarTitle.substringAfter(". ")}: ${it.metricValue}" }

        val trade = TradePositionEntity(
            symbol = report.symbol,
            assetName = report.assetName,
            side = report.direction,
            entryPrice = report.entryPrice,
            stopLoss = report.stopLoss,
            takeProfit1 = report.takeProfit1,
            takeProfit2 = report.takeProfit2,
            positionSizeUsd = posSize,
            leverage = customLeverage,
            strategyName = "Apex v4.2 (${report.timeframe})",
            confluenceScore = report.convictionScore,
            status = "OPEN",
            thesisNotes = "Auto-deployed from live ${report.exchangeSource} signal (${report.convictionScore}% Confluence). $topFactors",
            openedAt = System.currentTimeMillis()
        )
        return dao.insertTrade(trade)
    }

    suspend fun logManualTrade(
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
    ): Long {
        val trade = TradePositionEntity(
            symbol = symbol,
            assetName = assetName,
            side = side,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit1 = takeProfit1,
            takeProfit2 = takeProfit2,
            positionSizeUsd = positionSizeUsd,
            leverage = leverage,
            strategyName = "Krunal Desk Execution",
            confluenceScore = confluenceScore,
            status = "OPEN",
            thesisNotes = notes.ifBlank { "Manual institutional desk execution logged by Boss Krunal." },
            openedAt = System.currentTimeMillis()
        )
        return dao.insertTrade(trade)
    }

    suspend fun closePositionAtPrice(trade: TradePositionEntity, exitPrice: Double) {
        val rawPct = if (trade.side == "LONG") {
            (exitPrice - trade.entryPrice) / trade.entryPrice
        } else {
            (trade.entryPrice - exitPrice) / trade.entryPrice
        }
        val pnlUsd = trade.positionSizeUsd * rawPct * trade.leverage
        dao.updateTrade(
            trade.copy(
                status = "CLOSED",
                exitPrice = exitPrice,
                realizedPnlUsd = pnlUsd,
                closedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteTrade(id: Long) {
        dao.deleteTradeById(id)
    }

    suspend fun saveCompanyRecord(
        id: Long = 0L,
        title: String,
        category: String,
        amountUsd: Double,
        metricLabel: String,
        content: String,
        author: String,
        isPinned: Boolean
    ) {
        val entity = CompanyRecordEntity(
            id = id,
            title = title,
            category = category,
            amountUsd = amountUsd,
            metricLabel = metricLabel,
            content = content,
            author = author.ifBlank { "Boss Krunal • Managing Director" },
            isPinned = isPinned,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertCompanyRecord(entity)
    }

    suspend fun togglePinCompanyRecord(record: CompanyRecordEntity) {
        dao.updateCompanyRecord(
            record.copy(
                isPinned = !record.isPinned,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteCompanyRecord(id: Long) {
        dao.deleteCompanyRecordById(id)
    }

    suspend fun updateStrategyConfig(config: StrategyConfigEntity) {
        dao.upsertStrategyConfig(config)
    }

    fun buildExecutiveAuditReport(
        config: StrategyConfigEntity,
        trades: List<TradePositionEntity>,
        records: List<CompanyRecordEntity>,
        livePrices: Map<String, Double>
    ): String {
        val df = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.US)
        val openTrades = trades.filter { it.status == "OPEN" }
        val closedTrades = trades.filter { it.status == "CLOSED" }
        val realizedPnl = closedTrades.sumOf { it.realizedPnlUsd ?: 0.0 }
        val unrealizedPnl = openTrades.sumOf { t ->
            val mark = livePrices[t.symbol] ?: t.entryPrice
            val pct = if (t.side == "LONG") (mark - t.entryPrice) / t.entryPrice else (t.entryPrice - mark) / t.entryPrice
            t.positionSizeUsd * pct * t.leverage
        }

        return buildString {
            appendLine("====================================================")
            appendLine("   KRUNAL CAPITAL DESK — EXECUTIVE QUANT AUDIT      ")
            appendLine("====================================================")
            appendLine("Generated: ${df.format(Date())}")
            appendLine("Managing Director: Boss Krunal")
            appendLine("Chief Quant Architect: ${config.chiefQuantName}")
            appendLine("Active Strategy: ${config.strategyTitle} (${config.defaultTimeframe})")
            appendLine("----------------------------------------------------")
            appendLine("DESK CAPITAL & PERFORMANCE SUMMARY")
            appendLine("Base Desk Capital: ${String.format("$%,.2f", config.deskCapitalUsd)}")
            appendLine("Realized Closed P&L: ${String.format("$%,.2f", realizedPnl)}")
            appendLine("Live Unrealized P&L: ${String.format("$%,.2f", unrealizedPnl)}")
            appendLine("Net Desk Equity: ${String.format("$%,.2f", config.deskCapitalUsd + realizedPnl + unrealizedPnl)}")
            appendLine("Open Positions: ${openTrades.size} | Closed Audits: ${closedTrades.size}")
            appendLine("----------------------------------------------------")
            appendLine("ACTIVE OPEN POSITIONS (${openTrades.size})")
            openTrades.forEach { t ->
                val mark = livePrices[t.symbol] ?: t.entryPrice
                appendLine("• [${t.side}] ${t.symbol} | Size: ${String.format("$%,.0f", t.positionSizeUsd)} (${t.leverage}x) | Entry: ${t.entryPrice} | Mark: $mark | SL: ${t.stopLoss} | TP2: ${t.takeProfit2}")
            }
            appendLine("----------------------------------------------------")
            appendLine("COMPANY VAULT RECORDS (${records.size})")
            records.forEach { r ->
                appendLine("• [${r.category}] ${r.title} (${r.metricLabel}) — ${r.content}")
            }
            appendLine("====================================================")
        }
    }
}
