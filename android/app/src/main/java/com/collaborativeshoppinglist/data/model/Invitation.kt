package com.collaborativeshoppinglist.data.model

import com.google.firebase.Timestamp

enum class InvitationStatus { PENDING, ACCEPTED, EXPIRED, INVALIDATED }

data class Invitation(
    val id: String = "",
    val listId: String = "",
    val listName: String = "",
    val inviteeUid: String = "",
    val inviteeEmail: String = "",
    val inviterId: String = "",
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val acceptedAt: Timestamp? = null,
)
