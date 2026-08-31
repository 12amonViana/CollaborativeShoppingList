import { getAuth } from "firebase-admin/auth";
import { FieldValue, Timestamp, getFirestore } from "firebase-admin/firestore";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { logger } from "firebase-functions";

const INVITATION_LIFETIME_MS = 3 * 60 * 60 * 1000;

export const createInvitation = onCall(async (request) => {
  const inviterId = request.auth?.uid;
  if (!inviterId) {
    throw new HttpsError("unauthenticated", "Autenticação obrigatória.");
  }

  const listId = String(request.data?.listId ?? "").trim();
  const inviteeEmail = String(request.data?.inviteeEmail ?? "").trim().toLowerCase();
  if (!listId || !inviteeEmail) {
    throw new HttpsError("invalid-argument", "Lista e e-mail são obrigatórios.");
  }

  let invitee;
  try {
    invitee = await getAuth().getUserByEmail(inviteeEmail);
  } catch {
    throw new HttpsError("not-found", "Usuário não encontrado.");
  }
  if (invitee.uid === inviterId) {
    throw new HttpsError("invalid-argument", "Você já participa desta lista.");
  }

  const db = getFirestore();
  const inviteeProfile = await db.collection("users").doc(invitee.uid).get();
  const inviteeDisplayName = inviteeProfile.get("displayName") ??
    invitee.displayName ?? invitee.email ?? "Participante";
  const listRef = db.collection("lists").doc(listId);
  const memberRef = listRef.collection("members").doc(invitee.uid);
  const invitationRef = db.collection("invitations").doc(listId + "_" + invitee.uid);
  const now = Timestamp.now();
  const expiresAt = Timestamp.fromMillis(now.toMillis() + INVITATION_LIFETIME_MS);

  try {
    await db.runTransaction(async (transaction) => {
      const [list, member, existing] = await Promise.all([
        transaction.get(listRef),
        transaction.get(memberRef),
        transaction.get(invitationRef),
      ]);
      if (!list.exists) throw new HttpsError("not-found", "Lista não encontrada.");
      if (list.get("ownerId") !== inviterId) {
        throw new HttpsError("permission-denied", "Somente o proprietário pode convidar.");
      }
      if (list.get("status") !== "ACTIVE") {
        throw new HttpsError("failed-precondition", "LIST_CLOSED");
      }
      if (member.exists) {
        throw new HttpsError("already-exists", "Este usuário já participa da lista.");
      }
      if (
        existing.exists &&
        existing.get("status") === "PENDING" &&
        (existing.get("expiresAt") as Timestamp).toMillis() > now.toMillis()
      ) {
        throw new HttpsError("already-exists", "Já existe um convite pendente.");
      }

      transaction.set(invitationRef, {
        listId,
        listName: list.get("name"),
        inviteeUid: invitee.uid,
        inviteeEmail,
        inviteeDisplayName,
        inviterId,
        status: "PENDING",
        createdAt: now,
        expiresAt,
        acceptedAt: null,
        updatedAt: FieldValue.serverTimestamp(),
      });
    });
  } catch (error) {
    logger.error("Invitation creation failed", { listId, inviterId, error });
    throw error;
  }

  logger.info("Invitation created", { listId, inviterId, inviteeUid: invitee.uid });
  return { invitationId: invitationRef.id, expiresAt: expiresAt.toMillis() };
});
