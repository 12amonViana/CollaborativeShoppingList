package com.collaborativeshoppinglist.data.repository

import com.collaborativeshoppinglist.data.model.Invitation
import com.collaborativeshoppinglist.data.model.InvitationStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvitationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val authRepository: AuthRepository,
) {
    fun observePendingInvitations(): Flow<List<Invitation>> = callbackFlow {
        val userId = requireNotNull(authRepository.currentUserId) { "Entre para continuar." }
        val registration = firestore.collection("invitations")
            .whereEqualTo("inviteeUid", userId)
            .whereEqualTo("status", "PENDING")
            .orderBy("expiresAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else {
                    val invitations = snapshot?.documents.orEmpty().mapNotNull { document ->
                        val listId = document.getString("listId") ?: return@mapNotNull null
                        Invitation(
                            id = document.id,
                            listId = listId,
                            listName = document.getString("listName").orEmpty(),
                            inviteeUid = document.getString("inviteeUid").orEmpty(),
                            inviteeEmail = document.getString("inviteeEmail").orEmpty(),
                            inviterId = document.getString("inviterId").orEmpty(),
                            status = InvitationStatus.PENDING,
                            createdAt = document.getTimestamp("createdAt"),
                            expiresAt = document.getTimestamp("expiresAt"),
                            acceptedAt = document.getTimestamp("acceptedAt"),
                        )
                    }
                    val now = Timestamp.now().toDate()
                    trySend(invitations.filter { invitation ->
                        invitation.expiresAt?.toDate()?.after(now) == true
                    })
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun create(listId: String, inviteeEmail: String) {
        require(inviteeEmail.isNotBlank()) { "O e-mail é obrigatório." }
        functions.getHttpsCallable("createInvitation")
            .call(mapOf("listId" to listId, "inviteeEmail" to inviteeEmail.trim().lowercase()))
            .await()
    }

    suspend fun accept(invitationId: String): String {
        val result = functions.getHttpsCallable("acceptInvitation")
            .call(mapOf("invitationId" to invitationId))
            .await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as? Map<String, Any?>
        return data?.get("listId") as? String
            ?: error("A lista aceita não foi informada.")
    }
}
