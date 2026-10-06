package io.github.marchansergi.cleancard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.marchansergi.cleancard.NfcStatus
import io.github.marchansergi.cleancard.R
import io.github.marchansergi.cleancard.data.CardDesign
import io.github.marchansergi.cleancard.data.CardRepository
import io.github.marchansergi.cleancard.data.newCardDesign
import io.github.marchansergi.cleancard.openNfcSettings
import io.github.marchansergi.cleancard.openPaymentSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: CardRepository,
    nfcStatus: NfcStatus,
    onPay: (CardDesign) -> Unit,
    onEdit: (CardDesign) -> Unit,
    onCreate: () -> Unit,
) {
    val context = LocalContext.current
    val cards by repository.cards.collectAsStateWithLifecycle()
    val defaultId by repository.defaultId.collectAsStateWithLifecycle()
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CardDesign?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.home_how_it_works))
                    }
                },
            )
        },
        floatingActionButton = {
            if (cards.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onCreate,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.home_new_card)) },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (nfcStatus != NfcStatus.READY) {
                item { NfcWarning(nfcStatus, onEnable = { context.openNfcSettings() }) }
            }
            if (cards.isEmpty()) {
                item { EmptyState(onCreate) }
            } else {
                item {
                    Text(
                        stringResource(R.string.home_tap_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(cards, key = { it.id }) { card ->
                    CardItem(
                        card = card,
                        isDefault = card.id == defaultId || (defaultId == null && card == cards.first()),
                        onPay = { onPay(card) },
                        onMakeDefault = { repository.setDefault(card.id) },
                        onEdit = { onEdit(card) },
                        onDelete = { pendingDelete = card },
                    )
                }
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(stringResource(R.string.info_title)) },
            text = {
                Text(
                    stringResource(R.string.info_body),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text(stringResource(R.string.close)) }
            },
            dismissButton = {
                TextButton(onClick = { context.openPaymentSettings() }) {
                    Text(stringResource(R.string.info_payment_settings))
                }
            },
        )
    }

    pendingDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.home_delete_title)) },
            text = { Text(stringResource(R.string.home_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    repository.delete(card.id)
                    pendingDelete = null
                }) { Text(stringResource(R.string.home_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun CardItem(
    card: CardDesign,
    isDefault: Boolean,
    onPay: () -> Unit,
    onMakeDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
        CardFace(
            design = card,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onPay),
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = card.title.ifBlank { stringResource(R.string.home_untitled) },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onMakeDefault) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = stringResource(if (isDefault) R.string.home_default else R.string.home_make_default),
                    tint = if (isDefault) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    },
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.home_edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.home_delete))
            }
        }
    }
}

@Composable
private fun EmptyState(onCreate: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.widthIn(max = 520.dp).padding(top = 24.dp),
    ) {
        val sample = remember { newCardDesign().copy(holderName = "Tu nombre", lastDigits = "0000") }
        CardFace(sample, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.home_empty_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.home_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onCreate) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text(stringResource(R.string.home_create_first), modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun NfcWarning(status: NfcStatus, onEnable: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier.widthIn(max = 520.dp).fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                stringResource(if (status == NfcStatus.MISSING) R.string.nfc_missing else R.string.nfc_disabled),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            )
            if (status == NfcStatus.DISABLED) {
                TextButton(onClick = onEnable) { Text(stringResource(R.string.nfc_enable)) }
            }
        }
    }
}
