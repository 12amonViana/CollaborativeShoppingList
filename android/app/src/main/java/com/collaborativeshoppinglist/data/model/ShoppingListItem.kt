package com.collaborativeshoppinglist.data.model

import com.google.firebase.Timestamp

data class ShoppingListItem(
    val id: String = "",
    val name: String = "",
    val normalizedName: String = "",
    val quantity: Int = 1,
    val inCart: Boolean = false,
    val lastMarkedByUserId: String? = null,
    val updatedAt: Timestamp? = null,
    val updatedByUserId: String = "",
)
