import { Timestamp, getFirestore } from "firebase-admin/firestore";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { logger } from "firebase-functions";

export const closeShoppingList = onCall(async (request) => {
  const userId = request.auth?.uid;
  if (!userId) {
    throw new HttpsError("unauthenticated", "Autenticação obrigatória.");
  }
  const listId = String(request.data?.listId ?? "").trim();
  if (!listId) {
    throw new HttpsError("invalid-argument", "Lista obrigatória.");
  }

  const db = getFirestore();
  const listRef = db.collection("lists").doc(listId);
  const pendingQuery = db.collection("invitations")
    .where("listId", "==", listId)
    .where("status", "==", "PENDING");
  const now = Timestamp.now();

  try {
    await db.runTransaction(async (transaction) => {
      const list = await transaction.get(listRef);
      if (!list.exists) throw new HttpsError("not-found", "Lista não encontrada.");
      if (list.get("ownerId") !== userId) {
        throw new HttpsError("permission-denied", "Somente o proprietário pode encerrar.");
      }
      if (list.get("status") === "CLOSED") return;

      const pendingInvitations = await transaction.get(pendingQuery);
      transaction.update(listRef, {
        status: "CLOSED",
        closedAt: now,
        updatedAt: now,
      });
      pendingInvitations.docs.forEach((invitation) => {
        transaction.update(invitation.ref, {
          status: "INVALIDATED",
          updatedAt: now,
        });
      });
    });
  } catch (error) {
    logger.error("Shopping list close failed", { listId, userId, error });
    throw error;
  }

  logger.info("Shopping list closed", { listId, userId });
  return { listId, status: "CLOSED" };
});
