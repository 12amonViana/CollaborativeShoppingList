package com.collaborativeshoppinglist.data.model

import com.google.firebase.Timestamp

enum class ShoppingListStatus { ACTIVE, CLOSED }

data class ShoppingList(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val status: ShoppingListStatus = ShoppingListStatus.ACTIVE,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val closedAt: Timestamp? = null,
)
