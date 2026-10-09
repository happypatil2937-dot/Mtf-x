package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CompanyRecordEntity
import com.example.data.local.KrunalQuantDatabase
import com.example.data.local.StrategyConfigEntity
import com.example.data.local.TradePositionEntity
import com.example.data.repository.KrunalQuantRepository
import com.example.domain.quant.QuantSignalReport
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class QuantTab {
    LIVE_DESK,
    STRATEGY_LAB,
    TRADE_LEDGER,
    COMPANY_VAULT
}

data class QuantUiState(
    val activeTab: QuantTab = QuantTab.LIVE_DESK,
    val selectedSymbol: String = "BTC/USD",
    val selectedTimeframe: String = "1h",
    val isRefreshing: Boolean = true,
    val liveReports: List<QuantSignalReport> = emptyList(),
    val trades: List<TradePositionEntity> = emptyList(),
    val companyRecords: List<CompanyRecordEntity> = emptyList(),
    val strategyConfig: StrategyConfigEntity = StrategyConfigEntity(),
    val lastSyncedEpochMs: Long = 0L,
    val feedLatencyMs: Long = 42L,
    val feedSourceLabel: String = "KRAKEN LIVE SPOT",
    val tradeFilter: String = "ALL", // "ALL", "OPEN", "CLOSED"
    val vaultCategoryFilter: String = "ALL",
    val vaultSearchQuery: String = "",
    val bannerMessage: String? = null
) {
    val selectedReport: QuantSignalReport?
        get() = liveReports.find { it.symbol == selectedSymbol } ?: liveReports.firstOrNull()

    val livePriceMap: Map<String, Double>
        get() = liveReports.associate { it.symbol to it.currentPrice }

    val openTrades: List<TradePositionEntity>
        get() = trades.filter { it.status == "OPEN" }

    val closedTrades: List<TradePositionEntity>
        get() = trades.filter { it.status == "CLOSED" }

    val realizedPnlUsd: Double
        get() = closedTrades.sumOf { it.realizedPnlUsd ?: 0.0 }

    val unrealizedPnlUsd: Double
        get() = openTrades.sumOf { trade ->
            val mark = livePriceMap[trade.symbol] ?: trade.entryPrice
            val rawPct = if (trade.side == "LONG") {
                (mark - trade.entryPrice) / trade.entryPrice
            } else {
                (trade.entryPrice - mark) / trade.entryPrice
            }
            trade.positionSizeUsd * rawPct * trade.leverage
        }

    val netDeskEquityUsd: Double
        get() = strategyConfig.deskCapitalUsd + realizedPnlUsd + unrealizedPnlUsd
}

private data class LocalRuntimeState(
    val activeTab: QuantTab = QuantTab.LIVE_DESK,
    val selectedSymbol: String = "BTC/USD",
    val selectedTimeframe: String = "1h",
    val isRefreshing: Boolean = true,
    val liveReports: List<QuantSignalReport> = emptyList(),
    val lastSyncedEpochMs: Long = 0L,
    val feedLatencyMs: Long = 42L,
    val feedSourceLabel: String = "KRAKEN LIVE SPOT",
    val tradeFilter: String = "ALL",
    val vaultCategoryFilter: String = "ALL",
    val vaultSearchQuery: String = "",
    val bannerMessage: String? = null
)

class QuantViewModel(
    private val repository: KrunalQuantRepository
) : ViewModel() {

    private val runtimeState = MutableStateFlow(LocalRuntimeState())
    private var pollingJob: Job? = null

    val uiState: StateFlow<QuantUiState> = combine(
        runtimeState,
        repository.allTradesFlow,
        repository.allCompanyRecordsFlow,
        repository.strategyConfigFlow
    ) { rt, trades, records, config ->
        val safeConfig = config ?: StrategyConfigEntity()
        QuantUiState(
            activeTab = rt.activeTab,
            selectedSymbol = rt.selectedSymbol,
            selectedTimeframe = rt.selectedTimeframe,
            isRefreshing = rt.isRefreshing,
            liveReports = rt.liveReports,
            trades = trades,
            companyRecords = records,
            strategyConfig = safeConfig,
            lastSyncedEpochMs = rt.lastSyncedEpochMs,
            feedLatencyMs = rt.feedLatencyMs,
            feedSourceLabel = rt.feedSourceLabel,
            tradeFilter = rt.tradeFilter,
            vaultCategoryFilter = rt.vaultCategoryFilter,
            vaultSearchQuery = rt.vaultSearchQuery,
            bannerMessage = rt.bannerMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = QuantUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            startLiveMarketLoop()
        }
    }

    private fun startLiveMarketLoop() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                refreshLiveMarketNow(showSpinner = runtimeState.value.liveReports.isEmpty())
                val pollSec = uiState.value.strategyConfig.autoPollIntervalSec.coerceIn(6, 60)
                delay(pollSec * 1000L)
            }
        }
    }

    fun refreshLiveMarketNow(showSpinner: Boolean = true) {
        viewModelScope.launch {
            if (showSpinner) {
                runtimeState.value = runtimeState.value.copy(isRefreshing = true)
            }
            val currentRt = runtimeState.value
            val config = uiState.value.strategyConfig
            val (reports, latencyMs) = repository.fetchLiveQuantReports(
                selectedSymbol = currentRt.selectedSymbol,
                timeframe = currentRt.selectedTimeframe,
                config = config
            )
            val sourceLabel = reports.firstOrNull()?.exchangeSource ?: "KRAKEN LIVE SPOT"
            runtimeState.value = runtimeState.value.copy(
                isRefreshing = false,
                liveReports = reports,
                lastSyncedEpochMs = System.currentTimeMillis(),
                feedLatencyMs = latencyMs,
                feedSourceLabel = sourceLabel
            )
        }
    }

    fun selectTab(tab: QuantTab) {
        runtimeState.value = runtimeState.value.copy(activeTab = tab)
    }

    fun selectAssetAndOpenStrategy(symbol: String, switchToLab: Boolean = false) {
        val current = runtimeState.value
        val nextTab = if (switchToLab) QuantTab.STRATEGY_LAB else current.activeTab
        runtimeState.value = current.copy(
            selectedSymbol = symbol,
            activeTab = nextTab
        )
        refreshLiveMarketNow(showSpinner = false)
    }

    fun selectTimeframe(timeframe: String) {
        runtimeState.value = runtimeState.value.copy(selectedTimeframe = timeframe)
        refreshLiveMarketNow(showSpinner = true)
    }

    fun setTradeFilter(filter: String) {
        runtimeState.value = runtimeState.value.copy(tradeFilter = filter)
    }

    fun setVaultCategoryFilter(category: String) {
        runtimeState.value = runtimeState.value.copy(vaultCategoryFilter = category)
    }

    fun setVaultSearchQuery(query: String) {
        runtimeState.value = runtimeState.value.copy(vaultSearchQuery = query)
    }

    fun clearBannerMessage() {
        runtimeState.value = runtimeState.value.copy(bannerMessage = null)
    }

    private fun postBanner(msg: String) {
        runtimeState.value = runtimeState.value.copy(bannerMessage = msg)
    }

    fun deploySignalToCompanyLedger(
        report: QuantSignalReport,
        customSizeUsd: Double? = null,
        leverage: Double = 2.0
    ) {
        viewModelScope.launch {
            repository.deploySignalToLedger(report, customSizeUsd, leverage)
            postBanner("Deployed ${report.direction} ${report.symbol} (${report.convictionScore}% Confluence) to Krunal Capital Ledger!")
        }
    }

    fun logManualPosition(
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
    ) {
        viewModelScope.launch {
            repository.logManualTrade(
                symbol = symbol,
                assetName = assetName,
                side = side,
                entryPrice = entryPrice,
                stopLoss = stopLoss,
                takeProfit1 = takeProfit1,
                takeProfit2 = takeProfit2,
                positionSizeUsd = positionSizeUsd,
                leverage = leverage,
                confluenceScore = confluenceScore,
                notes = notes
            )
            postBanner("Saved $side $symbol position to Krunal Capital Ledger.")
        }
    }

    fun closeTradeAtLivePrice(trade: TradePositionEntity) {
        viewModelScope.launch {
            val liveMark = uiState.value.livePriceMap[trade.symbol] ?: trade.entryPrice
            repository.closePositionAtPrice(trade, liveMark)
            postBanner("Closed ${trade.symbol} ${trade.side} at live mark ${ String.format("$%,.2f", liveMark) }.")
        }
    }

    fun deleteTrade(id: Long) {
        viewModelScope.launch {
            repository.deleteTrade(id)
            postBanner("Position removed from desk ledger.")
        }
    }

    fun saveCompanyVaultRecord(
        id: Long = 0L,
        title: String,
        category: String,
        amountUsd: Double,
        metricLabel: String,
        content: String,
        author: String,
        isPinned: Boolean
    ) {
        viewModelScope.launch {
            repository.saveCompanyRecord(
                id = id,
                title = title,
                category = category,
                amountUsd = amountUsd,
                metricLabel = metricLabel,
                content = content,
                author = author,
                isPinned = isPinned
            )
            postBanner("Saved '$title' to Krunal Capital Company Vault.")
        }
    }

    fun togglePinCompanyRecord(record: CompanyRecordEntity) {
        viewModelScope.launch {
            repository.togglePinCompanyRecord(record)
        }
    }

    fun deleteCompanyRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteCompanyRecord(id)
            postBanner("Company record removed from vault.")
        }
    }

    fun saveStrategyConfig(updated: StrategyConfigEntity) {
        viewModelScope.launch {
            repository.updateStrategyConfig(updated)
            refreshLiveMarketNow(showSpinner = true)
            postBanner("Updated Apex Confluence v4.2 parameters for Krunal Capital Desk.")
        }
    }

    fun buildAuditReportText(): String {
        val st = uiState.value
        return repository.buildExecutiveAuditReport(
            config = st.strategyConfig,
            trades = st.trades,
            records = st.companyRecords,
            livePrices = st.livePriceMap
        )
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = KrunalQuantDatabase.getInstance(context.applicationContext)
            val repo = KrunalQuantRepository(db.quantDao())
            return QuantViewModel(repo) as T
        }
    }
}
