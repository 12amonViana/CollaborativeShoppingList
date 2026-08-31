package com.collaborativeshoppinglist.data.repository

import com.collaborativeshoppinglist.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentDisplayName: String?
        get() = auth.currentUser?.displayName

    suspend fun register(displayName: String, email: String, password: String) {
        require(displayName.isNotBlank()) { "O nome é obrigatório." }
        require(email.isNotBlank()) { "O e-mail é obrigatório." }
        require(password.length >= 6) { "A senha deve ter pelo menos 6 caracteres." }

        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = requireNotNull(result.user)
        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(displayName.trim()).build(),
        ).await()
        firestore.collection("users").document(firebaseUser.uid).set(
            mapOf(
                "email" to (firebaseUser.email ?: email.trim()).lowercase(),
                "displayName" to displayName.trim(),
                "createdAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun signIn(email: String, password: String) {
        require(email.isNotBlank()) { "O e-mail é obrigatório." }
        require(password.isNotBlank()) { "A senha é obrigatória." }
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    fun signOut() = auth.signOut()

    suspend fun currentProfile(): User? {
        val user = auth.currentUser ?: return null
        val snapshot = firestore.collection("users").document(user.uid).get().await()
        return snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
    }
}
