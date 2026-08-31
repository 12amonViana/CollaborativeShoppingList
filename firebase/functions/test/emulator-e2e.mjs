import assert from "node:assert/strict";
import { initializeApp, deleteApp } from "firebase/app";
import { connectAuthEmulator, createUserWithEmailAndPassword, getAuth } from "firebase/auth";
import {
  arrayUnion,
  collection,
  connectFirestoreEmulator,
  doc,
  getDoc,
  getDocs,
  getFirestore,
  onSnapshot,
  runTransaction,
  serverTimestamp,
  setDoc,
  Timestamp,
  updateDoc,
  writeBatch,
} from "firebase/firestore";
import { assertFails, initializeTestEnvironment } from "@firebase/rules-unit-testing";

const projectId = "demo-collaborative-shopping-list";
const apps = [];

function normalizedName(value) {
  return value.trim().replace(/\s+/g, " ").normalize("NFKC").toLowerCase();
}

async function createAccount(label, email, displayName) {
  const app = initializeApp({ projectId, apiKey: "demo-key" }, label);
  apps.push(app);
  const auth = getAuth(app);
  connectAuthEmulator(auth, "http://127.0.0.1:9099", { disableWarnings: true });
  const firestore = getFirestore(app);
  connectFirestoreEmulator(firestore, "127.0.0.1", 8080);
  const credential = await createUserWithEmailAndPassword(auth, email, "senha123");
  await setDoc(doc(firestore, "users", credential.user.uid), {
    email, displayName, createdAt: serverTimestamp(),
  });
  return { firestore, user: credential.user, displayName };
}

async function createList(account, listId, name) {
  const batch = writeBatch(account.firestore);
  batch.set(doc(account.firestore, "lists", listId), {
    name, ownerId: account.user.uid, memberIds: [account.user.uid], status: "ACTIVE",
    createdAt: serverTimestamp(), updatedAt: serverTimestamp(), closedAt: null,
  });
  batch.set(doc(account.firestore, "lists", listId, "members", account.user.uid), {
    userId: account.user.uid, displayName: account.displayName, role: "OWNER",
    joinedAt: serverTimestamp(),
  });
  await batch.commit();
}

async function addItem(account, listId, rawName) {
  const itemId = normalizedName(rawName);
  const listRef = doc(account.firestore, "lists", listId);
  const itemRef = doc(account.firestore, "lists", listId, "items", itemId);
  await runTransaction(account.firestore, async (transaction) => {
    const current = await transaction.get(itemRef);
    if (current.exists()) {
      transaction.update(itemRef, {
        quantity: current.data().quantity + 1,
        updatedAt: serverTimestamp(), updatedByUserId: account.user.uid,
      });
    } else {
      transaction.set(itemRef, {
        name: rawName.trim().replace(/\s+/g, " "), normalizedName: itemId,
        quantity: 1, inCart: false, lastMarkedByUserId: null,
        updatedAt: serverTimestamp(), updatedByUserId: account.user.uid,
      });
    }
    transaction.update(listRef, { updatedAt: serverTimestamp() });
  });
  return itemRef;
}

async function createInvitation(owner, listId, code, expiresAt) {
  const listRef = doc(owner.firestore, "lists", listId);
  const invitationRef = doc(owner.firestore, "invitations", code);
  await runTransaction(owner.firestore, async (transaction) => {
    const list = await transaction.get(listRef);
    transaction.set(invitationRef, {
      listId,
      listName: list.data().name,
      inviterId: owner.user.uid,
      inviterDisplayName: owner.displayName,
      status: "PENDING",
      createdAt: serverTimestamp(),
      expiresAt,
      acceptedAt: null,
      acceptedByUserId: null,
    });
  });
  return invitationRef;
}

async function acceptInvitation(account, code) {
  const invitationRef = doc(account.firestore, "invitations", code);
  let acceptedListId = "";
  await runTransaction(account.firestore, async (transaction) => {
    const invitation = await transaction.get(invitationRef);
    const listId = invitation.data().listId;
    acceptedListId = listId;
    transaction.set(doc(account.firestore, "lists", listId, "members", account.user.uid), {
      userId: account.user.uid,
      displayName: account.displayName,
      role: "MEMBER",
      joinedAt: serverTimestamp(),
      acceptedInvitationId: code,
    });
    transaction.update(doc(account.firestore, "lists", listId), {
      memberIds: arrayUnion(account.user.uid), updatedAt: serverTimestamp(),
    });
    transaction.update(invitationRef, {
      status: "ACCEPTED", acceptedAt: serverTimestamp(), acceptedByUserId: account.user.uid,
    });
  });
  return acceptedListId;
}

async function main() {
  const testEnvironment = await initializeTestEnvironment({
    projectId,
    firestore: { host: "127.0.0.1", port: 8080 },
  });
  try {
    const owner = await createAccount("owner", "ana@example.test", "Ana");
    const member = await createAccount("member", "bruno@example.test", "Bruno");
    const outsider = await createAccount("outsider", "carla@example.test", "Carla");
    const listId = "compra-semana";
    await createList(owner, listId, "Compra da semana");

    const milkRef = await addItem(owner, listId, "Leite");
    await addItem(owner, listId, " leite ");
    assert.equal((await getDoc(milkRef)).data().quantity, 2);
    await assertFails(updateDoc(milkRef, {
      quantity: 0, updatedAt: serverTimestamp(), updatedByUserId: owner.user.uid,
    }));
    await assertFails(getDoc(doc(outsider.firestore, "lists", listId)));

    const code = "00112233445566778899AABBCCDDEEFF";
    await createInvitation(
      owner,
      listId,
      code,
      Timestamp.fromMillis(Date.now() + 3 * 60 * 60 * 1000),
    );
    await assertFails(getDocs(collection(outsider.firestore, "invitations")));
    assert.equal(await acceptInvitation(member, code), listId);
    const memberDoc = await getDoc(doc(member.firestore, "lists", listId, "members", member.user.uid));
    assert.equal(memberDoc.data().displayName, "Bruno");
    await assertFails(acceptInvitation(outsider, code));

    const realTimeUpdate = new Promise((resolve, reject) => {
      const timeout = setTimeout(() => reject(new Error("Atualização não chegou em 3 segundos")), 3000);
      const unsubscribe = onSnapshot(doc(member.firestore, milkRef.path), (snapshot) => {
        if (snapshot.data()?.inCart === true) {
          clearTimeout(timeout); unsubscribe(); resolve();
        }
      }, reject);
    });
    await runTransaction(owner.firestore, async (transaction) => {
      transaction.update(milkRef, {
        inCart: true, lastMarkedByUserId: owner.user.uid,
        updatedAt: serverTimestamp(), updatedByUserId: owner.user.uid,
      });
      transaction.update(doc(owner.firestore, "lists", listId), { updatedAt: serverTimestamp() });
    });
    await realTimeUpdate;

    const expiredCode = "FFEEDDCCBBAA99887766554433221100";
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), "invitations", expiredCode), {
        listId, listName: "Compra da semana", inviterId: owner.user.uid,
        inviterDisplayName: "Ana", status: "PENDING",
        createdAt: Timestamp.fromMillis(Date.now() - 4 * 60 * 60 * 1000),
        expiresAt: Timestamp.fromMillis(Date.now() - 60 * 60 * 1000),
        acceptedAt: null, acceptedByUserId: null,
      });
    });
    await assertFails(acceptInvitation(outsider, expiredCode));

    await updateDoc(doc(owner.firestore, "lists", listId), {
      status: "CLOSED", closedAt: serverTimestamp(), updatedAt: serverTimestamp(),
    });
    assert.equal((await getDoc(doc(member.firestore, "lists", listId))).data().status, "CLOSED");
    await assertFails(updateDoc(doc(member.firestore, milkRef.path), {
      quantity: 5, updatedAt: serverTimestamp(), updatedByUserId: member.user.uid,
    }));

    console.log("E2E_OK: Spark, regras, convite por código, tempo real, expiração e encerramento.");
  } finally {
    await testEnvironment.cleanup();
    await Promise.all(apps.map(deleteApp));
  }
}

await main();
