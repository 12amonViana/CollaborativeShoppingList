package com.collaborativeshoppinglist.core.validation

import java.text.Normalizer
import java.util.Locale

object ItemValidator {
    const val MIN_QUANTITY = 1
    const val MAX_NAME_LENGTH = 120

    fun displayName(rawName: String): String =
        rawName.trim().replace(Regex("\\s+"), " ")

    fun normalizedName(rawName: String): String {
        val displayName = displayName(rawName)
        require(displayName.isNotEmpty()) { "O nome do item é obrigatório." }
        require(displayName.length <= MAX_NAME_LENGTH) {
            "O nome do item deve ter no máximo $MAX_NAME_LENGTH caracteres."
        }
        return Normalizer.normalize(displayName, Normalizer.Form.NFKC)
            .lowercase(Locale.ROOT)
    }

    fun validatedQuantity(quantity: Int): Int {
        require(quantity >= MIN_QUANTITY) { "A quantidade mínima é 1." }
        return quantity
    }
}
