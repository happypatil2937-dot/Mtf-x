package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_positions")
data class TradePositionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val symbol: String,               // e.g., "BTC/USD"
    val assetName: String,            // e.g., "Bitcoin Institutional Perp"
    val side: String,                 // "LONG" or "SHORT"
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val positionSizeUsd: Double,      // Capital deployed in USD
    val leverage: Double = 1.0,
    val strategyName: String = "Apex Confluence v4.2",
    val confluenceScore: Int = 85,
    val status: String = "OPEN",      // "OPEN" or "CLOSED"
    val exitPrice: Double? = null,
    val realizedPnlUsd: Double? = null,
    val thesisNotes: String = "",
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)

@Entity(tableName = "company_records")
data class CompanyRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val category: String,             // "TREASURY", "PROP_RULES", "AUDIT", "BROKER_DESK", "DIRECTIVE"
    val amountUsd: Double = 0.0,
    val metricLabel: String = "",     // e.g., "$250,000 AUM", "Max 1.5% Risk/Trade"
    val content: String,
    val author: String = "Aryan Verma (Chief Quant)",
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "strategy_config")
data class StrategyConfigEntity(
    @PrimaryKey val id: Int = 1,
    val strategyTitle: String = "Apex Institutional Confluence v4.2",
    val chiefQuantName: String = "Aryan Verma (Employee #01 — Chief Quant)",
    val companyName: String = "Krunal Capital Desk",
    val deskCapitalUsd: Double = 250000.0,
    val riskPerTradePercent: Double = 1.5,
    val emaFastPeriod: Int = 9,
    val emaMidPeriod: Int = 21,
    val emaSlowPeriod: Int = 50,
    val rsiPeriod: Int = 14,
    val rsiOversold: Double = 35.0,
    val rsiOverbought: Double = 70.0,
    val atrPeriod: Int = 14,
    val atrStopMultiplier: Double = 1.8,
    val minConfluenceScore: Int = 68,
    val defaultTimeframe: String = "1h",
    val autoPollIntervalSec: Int = 10
)
