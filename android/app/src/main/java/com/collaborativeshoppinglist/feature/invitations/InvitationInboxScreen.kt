package com.collaborativeshoppinglist.feature.invitations

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InvitationInboxScreen(
    onBack: () -> Unit,
    onAccepted: (String) -> Unit,
    viewModel: InvitationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Voltar") }
            Text("Convites", style = MaterialTheme.typography.headlineSmall)
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        when {
            state.isLoading -> CircularProgressIndicator()
            state.invitations.isEmpty() -> Text("Nenhum convite pendente.")
            else -> LazyColumn {
                items(state.invitations, key = { it.id }) { invitation ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(invitation.listName.ifBlank { "Lista compartilhada" })
                            Text("Convite de " + invitation.inviterId)
                        }
                        Button(
                            enabled = !state.isWorking,
                            onClick = { viewModel.accept(invitation.id, onAccepted) },
                        ) { Text("Aceitar") }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
