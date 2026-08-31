package com.collaborativeshoppinglist.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvitationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
) {
    suspend fun create(listId: String): String {
        val userId = requireUserId()
        val profile = authRepository.currentProfile()
        val inviterDisplayName = profile?.displayName
            ?: authRepository.currentDisplayName
            ?: "Proprietário"
        val code = generateCode()
        val listRef = firestore.collection("lists").document(listId)
        val invitationRef = firestore.collection("invitations").document(code)
        val expiresAt = Timestamp(Timestamp.now().seconds + INVITATION_LIFETIME_SECONDS, 0)

        firestore.runTransaction { transaction ->
            val list = transaction.get(listRef)
            check(list.exists()) { "Lista não encontrada." }
            check(list.getString("ownerId") == userId) { "NOT_AUTHORIZED" }
            check(list.getString("status") == "ACTIVE") { "LIST_CLOSED" }
            transaction.set(
                invitationRef,
                mapOf(
                    "listId" to listId,
                    "listName" to list.getString("name").orEmpty(),
                    "inviterId" to userId,
                    "inviterDisplayName" to inviterDisplayName,
                    "status" to "PENDING",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "expiresAt" to expiresAt,
                    "acceptedAt" to null,
                    "acceptedByUserId" to null,
                ),
            )
        }.await()
        return code
    }

    suspend fun accept(rawCode: String): String {
        val userId = requireUserId()
        val code = normalizeCode(rawCode)
        require(code.length == CODE_LENGTH) { "Código de convite inválido." }
        val displayName = authRepository.currentProfile()?.displayName
            ?: authRepository.currentDisplayName
            ?: "Participante"
        val invitationRef = firestore.collection("invitations").document(code)
        var acceptedListId = ""

        firestore.runTransaction { transaction ->
            val invitation = transaction.get(invitationRef)
            check(invitation.exists()) { "INVITATION_UNAVAILABLE" }
            check(invitation.getString("status") == "PENDING") { "INVITATION_UNAVAILABLE" }
            val expiresAt = invitation.getTimestamp("expiresAt")
                ?: error("INVITATION_UNAVAILABLE")
            check(expiresAt.toDate().after(Timestamp.now().toDate())) { "INVITATION_EXPIRED" }
            val listId = invitation.getString("listId") ?: error("INVITATION_UNAVAILABLE")
            val listRef = firestore.collection("lists").document(listId)
            val memberRef = listRef.collection("members").document(userId)
            transaction.set(
                memberRef,
                mapOf(
                    "userId" to userId,
                    "displayName" to displayName,
                    "role" to "MEMBER",
                    "joinedAt" to FieldValue.serverTimestamp(),
                    "acceptedInvitationId" to code,
                ),
            )
            transaction.update(
                listRef,
                mapOf(
                    "memberIds" to FieldValue.arrayUnion(userId),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            transaction.update(
                invitationRef,
                mapOf(
                    "status" to "ACCEPTED",
                    "acceptedAt" to FieldValue.serverTimestamp(),
                    "acceptedByUserId" to userId,
                ),
            )
            acceptedListId = listId
        }.await()
        return acceptedListId
    }

    private fun requireUserId(): String =
        requireNotNull(authRepository.currentUserId) { "Entre para continuar." }

    private fun generateCode(): String {
        val bytes = ByteArray(CODE_BYTES).also(random::nextBytes)
        return bytes.joinToString("") { "%02X".format(Locale.ROOT, it.toInt() and 0xff) }
    }

    private fun normalizeCode(value: String): String =
        value.filter(Char::isLetterOrDigit).uppercase(Locale.ROOT)

    private companion object {
        val random = SecureRandom()
        const val CODE_BYTES = 16
        const val CODE_LENGTH = CODE_BYTES * 2
        const val INVITATION_LIFETIME_SECONDS = 3 * 60 * 60L
    }
}
