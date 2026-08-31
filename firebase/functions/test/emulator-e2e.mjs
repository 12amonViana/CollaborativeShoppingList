import assert from "node:assert/strict";
import { initializeApp, deleteApp } from "firebase/app";
import {
  connectAuthEmulator,
  createUserWithEmailAndPassword,
  getAuth,
} from "firebase/auth";
import {
  collection,
  connectFirestoreEmulator,
  doc,
  getDoc,
  getDocs,
  getFirestore,
  onSnapshot,
  query,
  runTransaction,
  serverTimestamp,
  setDoc,
  Timestamp,
  updateDoc,
  where,
  writeBatch,
} from "firebase/firestore";
import {
  connectFunctionsEmulator,
  getFunctions,
  httpsCallable,
} from "firebase/functions";
import {
  assertFails,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";

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
  const functions = getFunctions(app);
  connectFunctionsEmulator(functions, "127.0.0.1", 5001);
  const credential = await createUserWithEmailAndPassword(auth, email, "senha123");
  await setDoc(doc(firestore, "users", credential.user.uid), {
    email,
    displayName,
    createdAt: serverTimestamp(),
  });
  return { auth, firestore, functions, user: credential.user, displayName };
}

async function createList(account, listId, name) {
  const batch = writeBatch(account.firestore);
  batch.set(doc(account.firestore, "lists", listId), {
    name,
    ownerId: account.user.uid,
    memberIds: [account.user.uid],
    status: "ACTIVE",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
    closedAt: null,
  });
  batch.set(doc(account.firestore, "lists", listId, "members", account.user.uid), {
    userId: account.user.uid,
    displayName: account.displayName,
    role: "OWNER",
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
        updatedAt: serverTimestamp(),
        updatedByUserId: account.user.uid,
      });
    } else {
      transaction.set(itemRef, {
        name: rawName.trim().replace(/\s+/g, " "),
        normalizedName: itemId,
        quantity: 1,
        inCart: false,
        lastMarkedByUserId: null,
        updatedAt: serverTimestamp(),
        updatedByUserId: account.user.uid,
      });
    }
    transaction.update(listRef, { updatedAt: serverTimestamp() });
  });
  return itemRef;
}

async function expectCallableFailure(callable, data) {
  let failed = false;
  try {
    await callable(data);
  } catch {
    failed = true;
  }
  assert.equal(failed, true, "A chamada deveria ter sido recusada");
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
      quantity: 0,
      updatedAt: serverTimestamp(),
      updatedByUserId: owner.user.uid,
    }));
    await assertFails(getDoc(doc(outsider.firestore, "lists", listId)));
    await assertFails(updateDoc(doc(owner.firestore, "lists", listId), {
      status: "CLOSED",
      closedAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    }));

    const createInvitation = httpsCallable(owner.functions, "createInvitation");
    const acceptInvitation = httpsCallable(member.functions, "acceptInvitation");
    const invitationResult = await createInvitation({
      listId,
      inviteeEmail: "bruno@example.test",
    });
    const invitationId = invitationResult.data.invitationId;
    const invitationRef = doc(member.firestore, "invitations", invitationId);
    assert.equal((await getDoc(invitationRef)).data().status, "PENDING");
    await assertFails(getDoc(doc(outsider.firestore, "invitations", invitationId)));

    const accepted = await acceptInvitation({ invitationId });
    assert.equal(accepted.data.listId, listId);
    const memberDoc = await getDoc(
      doc(member.firestore, "lists", listId, "members", member.user.uid),
    );
    assert.equal(memberDoc.data().displayName, "Bruno");

    const realTimeUpdate = new Promise((resolve, reject) => {
      const timeout = setTimeout(() => reject(new Error("Atualização não chegou em 3 segundos")), 3000);
      const unsubscribe = onSnapshot(doc(member.firestore, milkRef.path), (snapshot) => {
        if (snapshot.data()?.inCart === true) {
          clearTimeout(timeout);
          unsubscribe();
          resolve();
        }
      }, reject);
    });
    await runTransaction(owner.firestore, async (transaction) => {
      transaction.update(milkRef, {
        inCart: true,
        lastMarkedByUserId: owner.user.uid,
        updatedAt: serverTimestamp(),
        updatedByUserId: owner.user.uid,
      });
      transaction.update(doc(owner.firestore, "lists", listId), {
        updatedAt: serverTimestamp(),
      });
    });
    await realTimeUpdate;

    const increment = (account) => runTransaction(account.firestore, async (transaction) => {
      const accountItemRef = doc(account.firestore, milkRef.path);
      const snapshot = await transaction.get(accountItemRef);
      transaction.update(accountItemRef, {
        quantity: snapshot.data().quantity + 1,
        updatedAt: serverTimestamp(),
        updatedByUserId: account.user.uid,
      });
      transaction.update(doc(account.firestore, "lists", listId), {
        updatedAt: serverTimestamp(),
      });
    });
    await Promise.all([increment(owner), increment(member)]);
    assert.equal((await getDoc(milkRef)).data().quantity, 4);

    const outsiderInvitation = await createInvitation({
      listId,
      inviteeEmail: "carla@example.test",
    });
    const outsiderInvitationId = outsiderInvitation.data.invitationId;
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
      await updateDoc(
        doc(context.firestore(), "invitations", outsiderInvitationId),
        { expiresAt: Timestamp.fromMillis(Date.now() - 1000) },
      );
    });
    const outsiderAccept = httpsCallable(outsider.functions, "acceptInvitation");
    await expectCallableFailure(outsiderAccept, { invitationId: outsiderInvitationId });
    const expiredQuery = query(
      collection(outsider.firestore, "invitations"),
      where("inviteeUid", "==", outsider.user.uid),
    );
    const expiredDocs = await getDocs(expiredQuery);
    assert.equal(expiredDocs.docs[0].data().status, "EXPIRED");

    const replacementInvitation = await createInvitation({
      listId,
      inviteeEmail: "carla@example.test",
    });
    const closeList = httpsCallable(owner.functions, "closeShoppingList");
    await closeList({ listId });
    const closedList = await getDoc(doc(member.firestore, "lists", listId));
    assert.equal(closedList.data().status, "CLOSED");
    const invalidated = await getDoc(
      doc(outsider.firestore, "invitations", replacementInvitation.data.invitationId),
    );
    assert.equal(invalidated.data().status, "INVALIDATED");
    await expectCallableFailure(outsiderAccept, {
      invitationId: replacementInvitation.data.invitationId,
    });
    await assertFails(updateDoc(doc(member.firestore, milkRef.path), {
      quantity: 5,
      updatedAt: serverTimestamp(),
      updatedByUserId: member.user.uid,
    }));

    console.log("E2E_OK: autenticação, listas, itens, tempo real, convites, expiração, concorrência, encerramento e autorização.");
  } finally {
    await testEnvironment.cleanup();
    await Promise.all(apps.map(deleteApp));
  }
}

await main();
