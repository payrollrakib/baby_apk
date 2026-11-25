const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("todos").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("photos").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("notes").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("todos")
      .doc("t1")
      .set({
        userId: ALICE_UID,
        title: "বিসিজি টিকা",
        notes: "",
        ageStage: "M0_3",
        category: "VACCINE",
        completed: false,
        createdAt: past,
        updatedAt: past,
      })
  );
});

test("Owner can create, update, and delete UserProfile; cross-user and invalid payloads fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      babyName: "আয়ান",
      babyBirthDateMillis: 1735689600000,
      babyGender: "BOY",
      babyAgeMonths: 3,
      starterSeeded: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Update baby profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      babyName: "সোনামণি আয়ান",
      babyAgeMonths: 4,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Mark starter seeded
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      starterSeeded: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field / invalid age / cross-user access
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      babyAgeMonths: 15,
      updatedAt: new Date(Date.now() - 200),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 200),
      updatedAt: new Date(Date.now() - 200),
    })
  );
  await assertFails(aliceDb.collection("users").get());
});

test("Owner can CRUD todos; rejects @DocumentId id key, future timestamps, and Bob access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  const todosRef = aliceDb.collection("users").doc(ALICE_UID).collection("todos");

  await assertSucceeds(
    todosRef.doc("todo_1").set({
      userId: ALICE_UID,
      title: "৬ সপ্তাহে পেন্টাভ্যালেন্ট টিকা",
      notes: "টিকা কার্ড সাথে নিতে হবে",
      ageStage: "M0_3",
      category: "VACCINE",
      completed: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Toggle completed
  await assertSucceeds(
    todosRef.doc("todo_1").update({
      completed: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Full edit
  await assertSucceeds(
    todosRef.doc("todo_1").update({
      title: "৬ সপ্তাহের টিকা সম্পন্ন",
      notes: "হালকা জ্বর হলে ডাক্তারের পরামর্শ",
      ageStage: "M0_3",
      category: "VACCINE",
      completed: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // List own todos
  await assertSucceeds(todosRef.get());

  // Negative checks
  await assertFails(
    todosRef.doc("todo_bad_id").set({
      id: "todo_bad_id",
      userId: ALICE_UID,
      title: "অবৈধ ফিল্ড",
      notes: "",
      ageStage: "ALL",
      category: "CARE",
      completed: false,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertFails(
    todosRef.doc("todo_future").set({
      userId: ALICE_UID,
      title: "ভবিষ্যৎ সময়",
      notes: "",
      ageStage: "ALL",
      category: "CARE",
      completed: false,
      createdAt: new Date(Date.now() + 60000),
      updatedAt: created,
    })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("todos").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("todos").doc("todo_1").delete()
  );

  await assertSucceeds(todosRef.doc("todo_1").delete());
});

test("Owner can CRUD photos and notes including optional localImageFileName", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  const photosRef = aliceDb.collection("users").doc(ALICE_UID).collection("photos");
  await assertSucceeds(
    photosRef.doc("photo_1").set({
      userId: ALICE_UID,
      title: "প্রথম হাসি",
      caption: "আজ সকালে প্রথমবার শব্দ করে হাসল",
      ageMonth: 2,
      milestoneTag: "প্রথম হাসি",
      localFileName: "baby_photo_1.jpg",
      dateLabel: "২ মাস বয়স",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    photosRef.doc("photo_1").update({
      title: "মিষ্টি প্রথম হাসি",
      caption: "২ মাস পূর্ণ হওয়ার সুন্দর মুহূর্ত",
      ageMonth: 2,
      milestoneTag: "হাসি",
      dateLabel: "২ মাস",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(photosRef.get());

  const notesRef = aliceDb.collection("users").doc(ALICE_UID).collection("notes");
  // Without optional localImageFileName
  await assertSucceeds(
    notesRef.doc("note_1").set({
      userId: ALICE_UID,
      title: "ডাক্তারের পরামর্শ",
      content: "ভিটামিন ডি ড্রপ প্রতিদিন ১ ফোঁটা",
      category: "DOCTOR",
      ageMonth: 1,
      pinned: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // With optional localImageFileName
  await assertSucceeds(
    notesRef.doc("note_2").set({
      userId: ALICE_UID,
      title: "প্রেসক্রিপশন ছবি",
      content: "শিশু বিশেষজ্ঞের রুটিন চেকআপ",
      category: "DOCTOR",
      ageMonth: 3,
      pinned: false,
      localImageFileName: "note_img_2.jpg",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Pin toggle
  await assertSucceeds(
    notesRef.doc("note_2").update({
      pinned: true,
      updatedAt: new Date(Date.now() - 800),
    })
  );

  // Ghost field and cross-user denial
  await assertFails(
    notesRef.doc("note_1").update({
      isAdmin: true,
      pinned: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").get()
  );

  await assertSucceeds(photosRef.doc("photo_1").delete());
  await assertSucceeds(notesRef.doc("note_1").delete());
});
