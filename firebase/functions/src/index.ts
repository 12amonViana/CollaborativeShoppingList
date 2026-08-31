import { initializeApp } from "firebase-admin/app";
import { HttpsError, onCall } from "firebase-functions/v2/https";

initializeApp();

export const healthCheck = onCall((request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Autenticação obrigatória.");
  }

  return {
    status: "ok",
    userId: request.auth.uid,
  };
});

export { createInvitation } from "./createInvitation.js";
export { acceptInvitation } from "./acceptInvitation.js";
export { closeShoppingList } from "./closeShoppingList.js";
