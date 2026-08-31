package com.collaborativeshoppinglist.feature.invitations

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
    var email by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("Voltar") }
        Text("Convidar participante", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = { Text("E-mail da pessoa") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Button(
            enabled = !state.isWorking,
            onClick = { viewModel.create(listId, email) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) { Text("Enviar convite") }
        Text(
            "O convite expira em 3 horas e deixa de valer se a lista for encerrada.",
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
