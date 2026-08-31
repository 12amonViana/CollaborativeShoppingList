package com.collaborativeshoppinglist.feature.lists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ListDetailScreen(
    onBack: () -> Unit,
    onInvite: (String) -> Unit,
    viewModel: ListDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var itemName by remember { mutableStateOf("") }
    val memberNames = state.members.associate { member ->
        member.userId to member.displayName.ifBlank { member.userId }
    }

    if (state.isLoading && state.list == null) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Voltar") }
            Text(
                state.list?.name ?: "Lista",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
        }
        if (state.isClosed) {
            Text("Lista encerrada — somente leitura.", color = MaterialTheme.colorScheme.error)
        }
        if (!state.isClosed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Adicionar item") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    enabled = !state.isWorking,
                    onClick = { viewModel.addItem(itemName); itemName = "" },
                    modifier = Modifier.padding(start = 8.dp),
                ) { Text("Adicionar") }
            }
        }
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = viewModel::retry) { Text("Tentar novamente") }
        }
        ParticipantsSection(state.members)
        if (state.isOwner && !state.isClosed) {
            Row {
                TextButton(onClick = { onInvite(state.list?.id.orEmpty()) }) { Text("Convidar") }
                TextButton(onClick = viewModel::closeList) { Text("Encerrar lista") }
            }
        }
        if (state.items.isEmpty()) {
            Text("Nenhum item nesta lista.")
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(state.items, key = { it.id }) { item ->
                    ListItemRow(
                        item = item,
                        markedByDisplayName = item.lastMarkedByUserId?.let(memberNames::get),
                        enabled = !state.isClosed && !state.isWorking,
                        onQuantityChange = { viewModel.updateQuantity(item.id, it) },
                        onCartStatusChange = { viewModel.setCartStatus(item.id, it) },
                        onRemove = { viewModel.removeItem(item.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
