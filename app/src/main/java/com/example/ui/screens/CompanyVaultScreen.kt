package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CompanyRecordEntity
import com.example.ui.QuantUiState
import com.example.ui.theme.AlphaEmerald
import com.example.ui.theme.AlphaEmeraldDim
import com.example.ui.theme.BearishCrimson
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
fun CompanyVaultScreen(
    uiState: QuantUiState,
    onSetCategoryFilter: (String) -> Unit,
    onSetSearchQuery: (String) -> Unit,
    onSaveRecord: (
        id: Long,
        title: String,
        category: String,
        amountUsd: Double,
        metricLabel: String,
        content: String,
        author: String,
        isPinned: Boolean
    ) -> Unit,
    onTogglePin: (CompanyRecordEntity) -> Unit,
    onDeleteRecord: (Long) -> Unit,
    onBuildAuditReport: () -> String,
    onNavigateBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBackHome()
    }

    val context = LocalContext.current
    var editingRecord by remember { mutableStateOf<CompanyRecordEntity?>(null) }
    var showRecordModal by remember { mutableStateOf(false) }
    var auditReportText by remember { mutableStateOf<String?>(null) }

    val filteredRecords = remember(
        uiState.companyRecords,
        uiState.vaultCategoryFilter,
        uiState.vaultSearchQuery
    ) {
        uiState.companyRecords.filter { rec ->
            val catMatch = uiState.vaultCategoryFilter == "ALL" || rec.category == uiState.vaultCategoryFilter
            val q = uiState.vaultSearchQuery.trim()
            val queryMatch = q.isEmpty() ||
                rec.title.contains(q, ignoreCase = true) ||
                rec.content.contains(q, ignoreCase = true) ||
                rec.metricLabel.contains(q, ignoreCase = true)
            catMatch && queryMatch
        }
    }

    if (showRecordModal) {
        CompanyRecordModal(
            existing = editingRecord,
            onDismiss = {
                showRecordModal = false
                editingRecord = null
            },
            onConfirm = { id, title, cat, amount, metric, content, author, pinned ->
                onSaveRecord(id, title, cat, amount, metric, content, author, pinned)
                showRecordModal = false
                editingRecord = null
            }
        )
    }

    if (auditReportText != null) {
        AlertDialog(
            onDismissRequest = { auditReportText = null },
            title = {
                Text(
                    text = "Krunal Capital — Executive Audit",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianBg)
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = auditReportText ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText("Krunal Capital Audit", auditReportText ?: "")
                        )
                        auditReportText = null
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy & Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { auditReportText = null }) {
                    Text("Close")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("company_vault_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Company Vault Banner & Tribute Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SovereignGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_company_vault_banner_1791541873857),
                    contentDescription = "Krunal Capital Company Vault Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(205.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(205.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianBg.copy(alpha = 0.60f),
                                    ObsidianBg.copy(alpha = 0.88f),
                                    ObsidianBg.copy(alpha = 0.96f)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = SovereignGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KRUNAL CAPITAL • COMPANY DATA VAULT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SovereignGold
                            )
                        }

                        Text(
                            text = "${uiState.companyRecords.size} RECORDS SAVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = AlphaEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Boss Krunal's Executive Treasury & Prop Desk Database",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Dedicated by Aryan Verma (Best Employee & Chief Quant, Krunal Capital)",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                editingRecord = null
                                showRecordModal = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SovereignGold,
                                contentColor = Color(0xFF1A1300)
                            ),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(48.dp)
                                .testTag("add_company_record_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.add_company_record),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val report = onBuildAuditReport()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(
                                    ClipData.newPlainText("Krunal Capital Audit", report)
                                )
                                auditReportText = report
                            },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(48.dp)
                                .testTag("export_audit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.export_company_report),
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // 2. Search & Category Filter Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.vaultSearchQuery,
                    onValueChange = onSetSearchQuery,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    placeholder = {
                        Text("Search treasury allocations, prop rules, audits, directives...")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_search_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All Records",
                        "TREASURY" to "Treasury AUM",
                        "PROP_RULES" to "Prop Desk Rules",
                        "AUDIT" to "P&L Audits",
                        "BROKER_DESK" to "Exchange Desk",
                        "DIRECTIVE" to "Boss Directives"
                    ).forEach { (catKey, label) ->
                        FilterChip(
                            selected = uiState.vaultCategoryFilter == catKey,
                            onClick = { onSetCategoryFilter(catKey) },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SovereignGold,
                                selectedLabelColor = Color.Black,
                                containerColor = SlateCard,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("vault_cat_chip_$catKey")
                        )
                    }
                }
            }
        }

        // 3. Company Records List
        if (filteredRecords.isEmpty()) {
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
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = SovereignGold,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(id = R.string.empty_vault_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(filteredRecords, key = { it.id }) { record ->
                CompanyVaultCard(
                    record = record,
                    onTogglePin = { onTogglePin(record) },
                    onEdit = {
                        editingRecord = record
                        showRecordModal = true
                    },
                    onDelete = { onDeleteRecord(record.id) }
                )
            }
        }
    }
}

@Composable
private fun CompanyVaultCard(
    record: CompanyRecordEntity,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (catColor, catBg) = when (record.category) {
        "TREASURY" -> SovereignGold to SovereignGoldDim
        "PROP_RULES" -> ElectricCyan to ElectricCyanDim
        "AUDIT" -> AlphaEmerald to AlphaEmeraldDim
        "DIRECTIVE" -> SovereignGold to SovereignGoldDim
        else -> ElectricCyan to ElectricCyanDim
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCard)
            .border(
                width = 1.dp,
                color = if (record.isPinned) SovereignGold.copy(alpha = 0.65f) else BorderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onEdit() }
            .padding(14.dp)
            .testTag("company_record_card_${record.id}")
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
                        .background(catBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = record.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = catColor
                    )
                }
                if (record.metricLabel.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.metricLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = AlphaEmerald
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin Record",
                        tint = if (record.isPinned) SovereignGold else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Record",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Record",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Text(
            text = record.title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = record.content,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Logged by: ${record.author}",
                style = MaterialTheme.typography.labelSmall,
                color = SovereignGold
            )
            Text(
                text = dateFormat.format(Date(record.updatedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun CompanyRecordModal(
    existing: CompanyRecordEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        id: Long,
        title: String,
        category: String,
        amountUsd: Double,
        metricLabel: String,
        content: String,
        author: String,
        isPinned: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "TREASURY") }
    var amountText by remember { mutableStateOf(existing?.amountUsd?.toInt()?.toString() ?: "50000") }
    var metricLabel by remember { mutableStateOf(existing?.metricLabel ?: "$50,000 Allocation") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var author by remember { mutableStateOf(existing?.author ?: "Boss Krunal • Managing Director") }
    var isPinned by remember { mutableStateOf(existing?.isPinned ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) "Save New Company Record" else "Edit Company Record",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("TREASURY", "PROP_RULES", "AUDIT", "BROKER_DESK", "DIRECTIVE").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Record Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_record_title_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = metricLabel,
                        onValueChange = { metricLabel = it },
                        label = { Text("Key Metric Tag") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("USD Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Company Data / Directive Details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_record_content_input")
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author / Desk Officer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        existing?.id ?: 0L,
                        title.ifBlank { "Krunal Capital Desk Entry" },
                        category,
                        amountText.toDoubleOrNull() ?: 0.0,
                        metricLabel.ifBlank { "Verified" },
                        content.ifBlank { "Saved to Krunal Capital persistent SQLite database." },
                        author,
                        isPinned
                    )
                },
                modifier = Modifier.testTag("confirm_save_record_button")
            ) {
                Text("Save to Vault")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
