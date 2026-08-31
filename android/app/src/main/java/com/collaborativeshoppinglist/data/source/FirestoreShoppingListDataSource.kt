package com.collaborativeshoppinglist.data.source

import com.collaborativeshoppinglist.data.model.Membership
import com.collaborativeshoppinglist.data.model.MembershipRole
import com.collaborativeshoppinglist.data.model.ShoppingList
import com.collaborativeshoppinglist.data.model.ShoppingListItem
import com.collaborativeshoppinglist.data.model.ShoppingListStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreShoppingListDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun observeLists(userId: String): Flow<List<ShoppingList>> = callbackFlow {
        val registration = firestore.collection("lists")
            .whereArrayContains("memberIds", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull(::listFrom))
            }
        awaitClose { registration.remove() }
    }

    fun observeList(listId: String): Flow<ShoppingList?> = callbackFlow {
        val registration = firestore.collection("lists").document(listId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.takeIf { it.exists() }?.let(::listFrom))
            }
        awaitClose { registration.remove() }
    }

    fun observeItems(listId: String): Flow<List<ShoppingListItem>> = callbackFlow {
        val registration = firestore.collection("lists").document(listId)
            .collection("items")
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull(::itemFrom))
            }
        awaitClose { registration.remove() }
    }

    fun observeMembers(listId: String): Flow<List<Membership>> = callbackFlow {
        val registration = firestore.collection("lists").document(listId)
            .collection("members")
            .orderBy("joinedAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { document ->
                    val role = runCatching {
                        MembershipRole.valueOf(document.getString("role") ?: "MEMBER")
                    }.getOrNull() ?: return@mapNotNull null
                    Membership(
                        userId = document.id,
                        displayName = document.getString("displayName").orEmpty(),
                        role = role,
                        joinedAt = document.getTimestamp("joinedAt"),
                    )
                })
            }
        awaitClose { registration.remove() }
    }

    private fun listFrom(document: DocumentSnapshot): ShoppingList? {
        val name = document.getString("name") ?: return null
        val status = runCatching {
            ShoppingListStatus.valueOf(document.getString("status") ?: "ACTIVE")
        }.getOrDefault(ShoppingListStatus.ACTIVE)
        @Suppress("UNCHECKED_CAST")
        val memberIds = document.get("memberIds") as? List<String> ?: emptyList()
        return ShoppingList(
            id = document.id,
            name = name,
            ownerId = document.getString("ownerId").orEmpty(),
            memberIds = memberIds,
            status = status,
            createdAt = document.getTimestamp("createdAt"),
            updatedAt = document.getTimestamp("updatedAt"),
            closedAt = document.getTimestamp("closedAt"),
        )
    }

    private fun itemFrom(document: DocumentSnapshot): ShoppingListItem? {
        val name = document.getString("name") ?: return null
        return ShoppingListItem(
            id = document.id,
            name = name,
            normalizedName = document.getString("normalizedName") ?: document.id,
            quantity = document.getLong("quantity")?.toInt() ?: 1,
            inCart = document.getBoolean("inCart") ?: false,
            lastMarkedByUserId = document.getString("lastMarkedByUserId"),
            updatedAt = document.get("updatedAt") as? Timestamp,
            updatedByUserId = document.getString("updatedByUserId").orEmpty(),
        )
    }
}
