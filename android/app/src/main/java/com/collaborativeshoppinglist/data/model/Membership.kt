package com.collaborativeshoppinglist.data.model

import com.google.firebase.Timestamp

enum class MembershipRole { OWNER, MEMBER }

data class Membership(
    val userId: String = "",
    val displayName: String = "",
    val role: MembershipRole = MembershipRole.MEMBER,
    val joinedAt: Timestamp? = null,
)
