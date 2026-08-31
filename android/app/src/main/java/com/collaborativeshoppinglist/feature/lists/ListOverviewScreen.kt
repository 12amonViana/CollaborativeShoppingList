package com.collaborativeshoppinglist.feature.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
fun ListOverviewScreen(
    onListSelected: (String) -> Unit,
    onInvitations: () -> Unit,
    viewModel: ListOverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var listName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Minhas listas", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = viewModel::signOut) { Text("Sair") }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = listName,
                onValueChange = { listName = it },
                label = { Text("Nome da nova lista") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(
                enabled = !state.isCreating,
                onClick = {
                    viewModel.createList(listName) { id ->
                        listName = ""
                        onListSelected(id)
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
            ) { Text("Criar") }
        }
        TextButton(onClick = onInvitations) { Text("Ver convites") }
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            state.lists.isEmpty() -> Text("Nenhuma lista criada ou compartilhada.")
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.lists, key = { it.id }) { list ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onListSelected(list.id) }
                            .padding(vertical = 16.dp),
                    ) {
                        Text(list.name, style = MaterialTheme.typography.titleMedium)
                        Text(if (list.status.name == "ACTIVE") "Ativa" else "Encerrada")
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
