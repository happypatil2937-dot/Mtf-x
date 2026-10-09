package com.example

import com.example.data.local.StrategyConfigEntity
import com.example.domain.quant.OhlcvCandle
import com.example.domain.quant.QuantStrategyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun quantStrategyEngine_computesValidConfluenceAndRiskLevels() {
        val candles = (0 until 60).map { idx ->
            val base = 85000.0 + idx * 120.0
            OhlcvCandle(
                timestampSec = 1700000000L + idx * 3600L,
                open = base - 50.0,
                high = base + 140.0,
                low = base - 110.0,
                close = base + 80.0,
                volume = 1500.0 + idx * 25.0
            )
        }
        val config = StrategyConfigEntity()
        val report = QuantStrategyEngine.analyzeMarket(
            symbol = "BTC/USD",
            assetName = "Bitcoin Institutional Spot",
            timeframe = "1h",
            candles = candles,
            change24hPercent = 3.85,
            high24h = 92800.0,
            low24h = 84900.0,
            volume24hUsd = 1850000000.0,
            exchangeSource = "KRAKEN LIVE SPOT",
            config = config
        )

        assertEquals("BTC/USD", report.symbol)
        assertEquals("LONG", report.direction)
        assertTrue(report.convictionScore in 52..98)
        assertTrue(report.takeProfit2 > report.takeProfit1)
        assertTrue(report.takeProfit1 > report.entryPrice)
        assertTrue(report.stopLoss < report.entryPrice)
        assertEquals(7, report.factors.size)
    }
}
