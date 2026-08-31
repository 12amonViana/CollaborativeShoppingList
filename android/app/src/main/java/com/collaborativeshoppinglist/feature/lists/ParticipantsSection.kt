package com.collaborativeshoppinglist.feature.lists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.collaborativeshoppinglist.data.model.Membership

@Composable
fun ParticipantsSection(members: List<Membership>) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text("Participantes (" + members.size + ")")
        members.forEach { member ->
            val role = if (member.role.name == "OWNER") "Proprietário" else "Participante"
            val name = member.displayName.ifBlank { member.userId }
            Text("• " + name + " — " + role)
        }
    }
}
