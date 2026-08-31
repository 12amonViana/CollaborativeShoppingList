package com.collaborativeshoppinglist.feature.lists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
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
import com.collaborativeshoppinglist.data.model.ItemCategory

@Composable
fun ListDetailScreen(
    onBack: () -> Unit,
    onInvite: (String) -> Unit,
    viewModel: ListDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ItemCategory.OTHER) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var editedListName by remember(state.list?.name) { mutableStateOf(state.list?.name.orEmpty()) }
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
            Column {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Adicionar item") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Categoria do item", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(
                            onClick = { categoryMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("${selectedCategory.label}  ▾")
                        }
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false },
                            modifier = Modifier.widthIn(min = 220.dp),
                        ) {
                            ItemCategory.entries.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.label) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Button(
                        enabled = !state.isWorking,
                        onClick = {
                            viewModel.addItem(itemName, selectedCategory)
                            itemName = ""
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    ) { Text("Adicionar") }
                }
            }
        }
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = viewModel::retry) { Text("Tentar novamente") }
        }
        ParticipantsSection(state.members)
        if (state.isOwner) {
            Column {
                Row {
                    TextButton(onClick = { showRenameDialog = true }) { Text("Editar nome") }
                    if (state.isClosed) {
                        TextButton(onClick = viewModel::reactivateList) { Text("Reutilizar lista") }
                    } else {
                        TextButton(onClick = { onInvite(state.list?.id.orEmpty()) }) {
                            Text("Convidar")
                        }
                    }
                }
                Row {
                    if (!state.isClosed) {
                        TextButton(onClick = viewModel::closeList) { Text("Encerrar lista") }
                    }
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("Excluir lista", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        } else {
            TextButton(onClick = { showLeaveDialog = true }) {
                Text("Abandonar lista", color = MaterialTheme.colorScheme.error)
            }
        }
        if (state.isOwner) {
            TextButton(onClick = { showLeaveDialog = true }) {
                Text("Abandonar lista")
            }
        }
        if (state.items.isEmpty()) {
            Text("Nenhum item nesta lista.")
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                ItemCategory.entries.forEach { category ->
                    val categoryItems = state.items.filter { it.category == category }
                    if (categoryItems.isNotEmpty()) {
                        item(key = "category-${category.name}") {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                            )
                        }
                        items(categoryItems, key = { it.id }) { item ->
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
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Editar nome da lista") },
            text = {
                OutlinedTextField(
                    value = editedListName,
                    onValueChange = { editedListName = it },
                    label = { Text("Nome da lista") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameList(editedListName)
                    showRenameDialog = false
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancelar") }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir lista?") },
            text = { Text("Esta ação remove a lista e seus itens para todos os participantes.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteList(onBack)
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
        )
    }

    if (showLeaveDialog) {
        val ownerWillTransfer = state.isOwner && state.members.size > 1
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Abandonar lista?") },
            text = {
                Text(
                    when {
                        ownerWillTransfer ->
                            "Você perderá o acesso e o participante mais antigo se tornará proprietário."
                        state.isOwner ->
                            "Como você é o único participante, a lista e seus itens serão excluídos."
                        else -> "Você perderá o acesso a esta lista."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    viewModel.leaveList(onBack)
                }) { Text("Abandonar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancelar") }
            },
        )
    }
}
