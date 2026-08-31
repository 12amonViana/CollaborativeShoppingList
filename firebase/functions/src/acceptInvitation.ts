import { FieldValue, Timestamp, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { HttpsError, onCall } from "firebase-functions/v2/https";

type AcceptanceOutcome = "ACCEPTED" | "EXPIRED" | "INVALIDATED";

export const acceptInvitation = onCall(async (request) => {
  const userId = request.auth?.uid;
  if (!userId) {
    throw new HttpsError("unauthenticated", "Autenticação obrigatória.");
  }

  const invitationId = String(request.data?.invitationId ?? "").trim();
  if (!invitationId) {
    throw new HttpsError("invalid-argument", "Convite obrigatório.");
  }

  const db = getFirestore();
  const invitationRef = db.collection("invitations").doc(invitationId);
  const now = Timestamp.now();
  let acceptedListId = "";

  let outcome: AcceptanceOutcome;
  try {
    outcome = await db.runTransaction<AcceptanceOutcome>(async (transaction) => {
    const invitation = await transaction.get(invitationRef);
    if (!invitation.exists || invitation.get("inviteeUid") !== userId) {
      throw new HttpsError("not-found", "Convite não encontrado.");
    }

    if (invitation.get("status") === "ACCEPTED") {
      acceptedListId = invitation.get("listId") as string;
      return "ACCEPTED";
    }
    if (invitation.get("status") !== "PENDING") {
      throw new HttpsError("failed-precondition", "INVITATION_UNAVAILABLE");
    }

    const expiresAt = invitation.get("expiresAt") as Timestamp;
    if (expiresAt.toMillis() <= now.toMillis()) {
      transaction.update(invitationRef, { status: "EXPIRED", updatedAt: now });
      return "EXPIRED";
    }

    const listId = invitation.get("listId") as string;
    const listRef = db.collection("lists").doc(listId);
    const memberRef = listRef.collection("members").doc(userId);
    const list = await transaction.get(listRef);
    if (!list.exists || list.get("status") !== "ACTIVE") {
      transaction.update(invitationRef, { status: "INVALIDATED", updatedAt: now });
      return "INVALIDATED";
    }

    transaction.set(memberRef, {
      userId,
      displayName: invitation.get("inviteeDisplayName") ?? "Participante",
      role: "MEMBER",
      joinedAt: now,
    });
    transaction.update(listRef, {
      memberIds: FieldValue.arrayUnion(userId),
      updatedAt: now,
    });
    transaction.update(invitationRef, {
      status: "ACCEPTED",
      acceptedAt: now,
      updatedAt: now,
    });
    acceptedListId = listId;
    return "ACCEPTED";
    });
  } catch (error) {
    logger.error("Invitation acceptance failed", { invitationId, userId, error });
    throw error;
  }

  if (outcome === "EXPIRED") {
    throw new HttpsError("deadline-exceeded", "INVITATION_EXPIRED");
  }
  if (outcome === "INVALIDATED") {
    throw new HttpsError("failed-precondition", "INVITATION_UNAVAILABLE");
  }

  logger.info("Invitation accepted", { invitationId, userId, listId: acceptedListId });
  return { listId: acceptedListId };
});
