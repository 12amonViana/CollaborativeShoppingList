package com.collaborativeshoppinglist.feature.invitations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CreateInvitationScreen(
    listId: String,
    onBack: () -> Unit,
    viewModel: InvitationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("Voltar") }
        Text("Compartilhar lista", style = MaterialTheme.typography.headlineSmall)
        Text("Gere um código e envie-o à pessoa por um meio de sua confiança.")
        state.invitationCode?.let { code ->
            OutlinedTextField(
                value = code,
                onValueChange = {},
                readOnly = true,
                label = { Text("Código do convite") },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            )
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            enabled = !state.isWorking,
            onClick = { viewModel.create(listId) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) { Text(if (state.invitationCode == null) "Gerar código" else "Gerar novo código") }
        Text(
            "O código expira em 3 horas, só pode ser usado uma vez e deixa de valer se a lista for encerrada.",
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
