package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.QuantTab
import com.example.ui.QuantUiState
import com.example.ui.QuantViewModel
import com.example.ui.screens.CompanyVaultScreen
import com.example.ui.screens.RadarScannerScreen
import com.example.ui.screens.StrategyChartScreen
import com.example.ui.screens.TradeLedgerScreen
import com.example.ui.theme.AlphaEmerald
import com.example.ui.theme.AlphaEmeraldDim
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SovereignGold
import com.example.ui.theme.SovereignGoldDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val quantViewModel: QuantViewModel = viewModel(
                    factory = QuantViewModel.Factory(context)
                )
                val uiState by quantViewModel.uiState.collectAsStateWithLifecycle()

                KrunalQuantTerminalApp(
                    uiState = uiState,
                    viewModel = quantViewModel
                )
            }
        }
    }
}

private data class NavDestination(
    val tab: QuantTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun KrunalQuantTerminalApp(
    uiState: QuantUiState,
    viewModel: QuantViewModel
) {
    val destinations = listOf(
        NavDestination(
            tab = QuantTab.LIVE_DESK,
            labelRes = R.string.tab_live_desk,
            selectedIcon = Icons.Filled.Radar,
            unselectedIcon = Icons.Outlined.Radar,
            testTag = "nav_tab_live_desk"
        ),
        NavDestination(
            tab = QuantTab.STRATEGY_LAB,
            labelRes = R.string.tab_strategy_lab,
            selectedIcon = Icons.Filled.AutoGraph,
            unselectedIcon = Icons.Outlined.AutoGraph,
            testTag = "nav_tab_strategy_lab"
        ),
        NavDestination(
            tab = QuantTab.TRADE_LEDGER,
            labelRes = R.string.tab_trade_ledger,
            selectedIcon = Icons.Filled.ReceiptLong,
            unselectedIcon = Icons.Outlined.ReceiptLong,
            testTag = "nav_tab_trade_ledger"
        ),
        NavDestination(
            tab = QuantTab.COMPANY_VAULT,
            labelRes = R.string.tab_company_vault,
            selectedIcon = Icons.Filled.AccountBalance,
            unselectedIcon = Icons.Outlined.AccountBalance,
            testTag = "nav_tab_company_vault"
        )
    )

    LaunchedEffect(uiState.bannerMessage) {
        if (uiState.bannerMessage != null) {
            delay(3800L)
            viewModel.clearBannerMessage()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBg,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MidnightSurface,
                        tonalElevation = 8.dp
                    ) {
                        destinations.forEach { dest ->
                            val selected = uiState.activeTab == dest.tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(id = dest.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = dest.labelRes),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1A1300),
                                    selectedTextColor = SovereignGold,
                                    indicatorColor = SovereignGold,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MidnightSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        destinations.forEach { dest ->
                            val selected = uiState.activeTab == dest.tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(id = dest.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = dest.labelRes),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1A1300),
                                    selectedTextColor = SovereignGold,
                                    indicatorColor = SovereignGold,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState.activeTab) {
                        QuantTab.LIVE_DESK -> {
                            RadarScannerScreen(
                                uiState = uiState,
                                onRefreshLive = { viewModel.refreshLiveMarketNow(showSpinner = true) },
                                onSelectTimeframe = { tf -> viewModel.selectTimeframe(tf) },
                                onInspectAssetInLab = { sym ->
                                    viewModel.selectAssetAndOpenStrategy(sym, switchToLab = true)
                                },
                                onDeploySignal = { report ->
                                    viewModel.deploySignalToCompanyLedger(report)
                                }
                            )
                        }

                        QuantTab.STRATEGY_LAB -> {
                            StrategyChartScreen(
                                uiState = uiState,
                                onSelectAsset = { sym ->
                                    viewModel.selectAssetAndOpenStrategy(sym, switchToLab = false)
                                },
                                onSelectTimeframe = { tf -> viewModel.selectTimeframe(tf) },
                                onDeploySignalWithLeverage = { report, sizeUsd, lev ->
                                    viewModel.deploySignalToCompanyLedger(report, sizeUsd, lev)
                                },
                                onSaveStrategyConfig = { updated ->
                                    viewModel.saveStrategyConfig(updated)
                                },
                                onNavigateBackHome = {
                                    viewModel.selectTab(QuantTab.LIVE_DESK)
                                }
                            )
                        }

                        QuantTab.TRADE_LEDGER -> {
                            TradeLedgerScreen(
                                uiState = uiState,
                                onSetFilter = { f -> viewModel.setTradeFilter(f) },
                                onLogManualPosition = { sym, name, side, entry, sl, tp1, tp2, sizeUsd, lev, score, notes ->
                                    viewModel.logManualPosition(
                                        symbol = sym,
                                        assetName = name,
                                        side = side,
                                        entryPrice = entry,
                                        stopLoss = sl,
                                        takeProfit1 = tp1,
                                        takeProfit2 = tp2,
                                        positionSizeUsd = sizeUsd,
                                        leverage = lev,
                                        confluenceScore = score,
                                        notes = notes
                                    )
                                },
                                onCloseAtLiveMarket = { trade ->
                                    viewModel.closeTradeAtLivePrice(trade)
                                },
                                onDeleteTrade = { id ->
                                    viewModel.deleteTrade(id)
                                },
                                onNavigateBackHome = {
                                    viewModel.selectTab(QuantTab.LIVE_DESK)
                                }
                            )
                        }

                        QuantTab.COMPANY_VAULT -> {
                            CompanyVaultScreen(
                                uiState = uiState,
                                onSetCategoryFilter = { cat -> viewModel.setVaultCategoryFilter(cat) },
                                onSetSearchQuery = { q -> viewModel.setVaultSearchQuery(q) },
                                onSaveRecord = { id, title, cat, amount, metric, content, author, pinned ->
                                    viewModel.saveCompanyVaultRecord(
                                        id = id,
                                        title = title,
                                        category = cat,
                                        amountUsd = amount,
                                        metricLabel = metric,
                                        content = content,
                                        author = author,
                                        isPinned = pinned
                                    )
                                },
                                onTogglePin = { rec -> viewModel.togglePinCompanyRecord(rec) },
                                onDeleteRecord = { id -> viewModel.deleteCompanyRecord(id) },
                                onBuildAuditReport = { viewModel.buildAuditReportText() },
                                onNavigateBackHome = {
                                    viewModel.selectTab(QuantTab.LIVE_DESK)
                                }
                            )
                        }
                    }

                    // Floating Confirmation Toast Banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        AnimatedVisibility(
                            visible = uiState.bannerMessage != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AlphaEmeraldDim)
                                    .border(1.dp, AlphaEmerald, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("desk_toast_banner"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AlphaEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uiState.bannerMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                                TextButton(onClick = { viewModel.clearBannerMessage() }) {
                                    Text(
                                        text = "OK",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SovereignGold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
