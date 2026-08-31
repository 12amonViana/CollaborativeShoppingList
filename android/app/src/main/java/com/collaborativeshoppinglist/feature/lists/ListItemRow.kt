package com.collaborativeshoppinglist.feature.lists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.collaborativeshoppinglist.data.model.ShoppingListItem

@Composable
fun ListItemRow(
    item: ShoppingListItem,
    markedByDisplayName: String?,
    enabled: Boolean,
    onQuantityChange: (Int) -> Unit,
    onCartStatusChange: (Boolean) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.inCart,
            enabled = enabled,
            onCheckedChange = onCartStatusChange,
            modifier = Modifier.semantics {
                contentDescription = if (item.inCart) {
                    "Marcar " + item.name + " como pendente"
                } else {
                    "Marcar " + item.name + " como colocado no carrinho"
                }
            },
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                textDecoration = if (item.inCart) TextDecoration.LineThrough else null,
            )
            if (item.inCart && item.lastMarkedByUserId != null) {
                Text("Marcado por " + (markedByDisplayName ?: item.lastMarkedByUserId))
            }
        }
        TextButton(
            enabled = enabled && item.quantity > 1,
            onClick = { onQuantityChange(item.quantity - 1) },
            modifier = Modifier.semantics {
                contentDescription = "Diminuir quantidade de " + item.name
            },
        ) { Text("−") }
        Text(item.quantity.toString())
        TextButton(
            enabled = enabled,
            onClick = { onQuantityChange(item.quantity + 1) },
            modifier = Modifier.semantics {
                contentDescription = "Aumentar quantidade de " + item.name
            },
        ) {
            Text("+")
        }
        TextButton(
            enabled = enabled,
            onClick = onRemove,
            modifier = Modifier.semantics {
                contentDescription = "Remover " + item.name
            },
        ) { Text("Remover") }
    }
}
