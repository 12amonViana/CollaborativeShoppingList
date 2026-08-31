package com.collaborativeshoppinglist.feature.invitations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardCapitalization
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
    var code by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Voltar") }
            Text("Entrar em uma lista", style = MaterialTheme.typography.headlineSmall)
        }
        Text("Informe o código compartilhado pelo proprietário da lista.")
        OutlinedTextField(
            value = code,
            onValueChange = { code = it; viewModel.clearError() },
            label = { Text("Código do convite") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            enabled = !state.isWorking && code.isNotBlank(),
            onClick = { viewModel.accept(code, onAccepted) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) { Text("Aceitar convite") }
    }
}
