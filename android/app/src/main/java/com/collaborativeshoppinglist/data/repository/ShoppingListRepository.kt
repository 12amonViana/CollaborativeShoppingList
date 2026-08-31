package com.collaborativeshoppinglist.data.repository

import com.collaborativeshoppinglist.core.validation.ItemValidator
import com.collaborativeshoppinglist.data.model.Membership
import com.collaborativeshoppinglist.data.model.ShoppingList
import com.collaborativeshoppinglist.data.model.ShoppingListItem
import com.collaborativeshoppinglist.data.source.FirestoreShoppingListDataSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val dataSource: FirestoreShoppingListDataSource,
) {
    fun observeLists(): Flow<List<ShoppingList>> =
        dataSource.observeLists(requireUserId())

    fun observeList(listId: String): Flow<ShoppingList?> =
        dataSource.observeList(listId)

    fun observeItems(listId: String): Flow<List<ShoppingListItem>> =
        dataSource.observeItems(listId)

    fun observeMembers(listId: String): Flow<List<Membership>> =
        dataSource.observeMembers(listId)

    suspend fun createList(rawName: String): String {
        val name = rawName.trim()
        require(name.isNotEmpty()) { "O nome da lista é obrigatório." }
        require(name.length <= 100) { "O nome da lista deve ter no máximo 100 caracteres." }
        val userId = requireUserId()
        val displayName = authRepository.currentDisplayName
            ?: authRepository.currentProfile()?.displayName
            ?: "Proprietário"
        val listRef = firestore.collection("lists").document()
        val memberRef = listRef.collection("members").document(userId)
        firestore.runBatch { batch ->
            batch.set(
                listRef,
                mapOf(
                    "name" to name,
                    "ownerId" to userId,
                    "memberIds" to listOf(userId),
                    "status" to "ACTIVE",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "closedAt" to null,
                ),
            )
            batch.set(
                memberRef,
                mapOf(
                    "userId" to userId,
                    "displayName" to displayName,
                    "role" to "OWNER",
                    "joinedAt" to FieldValue.serverTimestamp(),
                ),
            )
        }.await()
        return listRef.id
    }

    suspend fun addItem(listId: String, rawName: String) {
        val userId = requireUserId()
        val displayName = ItemValidator.displayName(rawName)
        val normalizedName = ItemValidator.normalizedName(rawName)
        val listRef = firestore.collection("lists").document(listId)
        val itemRef = listRef.collection("items").document(normalizedName)
        firestore.runTransaction { transaction ->
            requireActive(transaction.get(listRef).getString("status"))
            val current = transaction.get(itemRef)
            if (current.exists()) {
                val quantity = current.getLong("quantity")?.toInt() ?: 1
                transaction.update(
                    itemRef,
                    mapOf(
                        "quantity" to quantity + 1,
                        "updatedAt" to FieldValue.serverTimestamp(),
                        "updatedByUserId" to userId,
                    ),
                )
            } else {
                transaction.set(
                    itemRef,
                    mapOf(
                        "name" to displayName,
                        "normalizedName" to normalizedName,
                        "quantity" to 1,
                        "inCart" to false,
                        "lastMarkedByUserId" to null,
                        "updatedAt" to FieldValue.serverTimestamp(),
                        "updatedByUserId" to userId,
                    ),
                )
            }
            transaction.update(listRef, "updatedAt", FieldValue.serverTimestamp())
        }.await()
    }

    suspend fun updateQuantity(listId: String, itemId: String, quantity: Int) {
        ItemValidator.validatedQuantity(quantity)
        val userId = requireUserId()
        val listRef = firestore.collection("lists").document(listId)
        val itemRef = listRef.collection("items").document(itemId)
        firestore.runTransaction { transaction ->
            requireActive(transaction.get(listRef).getString("status"))
            require(transaction.get(itemRef).exists()) { "Item não encontrado." }
            transaction.update(
                itemRef,
                mapOf(
                    "quantity" to quantity,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "updatedByUserId" to userId,
                ),
            )
            transaction.update(listRef, "updatedAt", FieldValue.serverTimestamp())
        }.await()
    }

    suspend fun removeItem(listId: String, itemId: String) {
        val listRef = firestore.collection("lists").document(listId)
        val itemRef = listRef.collection("items").document(itemId)
        firestore.runTransaction { transaction ->
            requireActive(transaction.get(listRef).getString("status"))
            transaction.delete(itemRef)
            transaction.update(listRef, "updatedAt", FieldValue.serverTimestamp())
        }.await()
    }

    suspend fun setCartStatus(listId: String, itemId: String, inCart: Boolean) {
        val userId = requireUserId()
        val listRef = firestore.collection("lists").document(listId)
        val itemRef = listRef.collection("items").document(itemId)
        firestore.runTransaction { transaction ->
            requireActive(transaction.get(listRef).getString("status"))
            require(transaction.get(itemRef).exists()) { "Item não encontrado." }
            transaction.update(
                itemRef,
                mapOf(
                    "inCart" to inCart,
                    "lastMarkedByUserId" to if (inCart) userId else null,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "updatedByUserId" to userId,
                ),
            )
            transaction.update(listRef, "updatedAt", FieldValue.serverTimestamp())
        }.await()
    }

    suspend fun closeList(listId: String) {
        val userId = requireUserId()
        val listRef = firestore.collection("lists").document(listId)
        firestore.runTransaction { transaction ->
            val list = transaction.get(listRef)
            check(list.getString("status") == "ACTIVE") { "LIST_CLOSED" }
            check(list.getString("ownerId") == userId) { "NOT_AUTHORIZED" }
            transaction.update(
                listRef,
                mapOf(
                    "status" to "CLOSED",
                    "closedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
        }.await()
    }

    private fun requireUserId(): String =
        requireNotNull(authRepository.currentUserId) { "Entre para continuar." }

    private fun requireActive(status: String?) {
        check(status == "ACTIVE") { "LIST_CLOSED" }
    }
}
