package com.collaborativeshoppinglist.data.model

import com.google.firebase.Timestamp

enum class ItemCategory(
    val label: String,
) {
    COLD_CUTS_AND_DAIRY("🧀  Frios e Laticínios"),
    BUTCHER("🥩  Açougue"),
    PRODUCE("🥬  Hortifrut"),
    CLEANING("🧹  Limpeza"),
    FROZEN("❄️  Congelados"),
    OTHER("📦  Outros");

    companion object {
        fun fromStored(value: String?): ItemCategory =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}

data class ShoppingListItem(
    val id: String = "",
    val name: String = "",
    val normalizedName: String = "",
    val category: ItemCategory = ItemCategory.OTHER,
    val quantity: Int = 1,
    val inCart: Boolean = false,
    val lastMarkedByUserId: String? = null,
    val updatedAt: Timestamp? = null,
    val updatedByUserId: String = "",
)
