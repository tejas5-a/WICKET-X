const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

test("Unauthenticated user: cannot read matches", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("matches").get());
});

test("Authenticated user: can create a valid match", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("matches").doc("match_1").set({
      id: "match_1",
      creatorId: ALICE_UID,
      teamAName: "Lions CC",
      teamBName: "Tigers CC",
      matchFormat: "T20",
      totalOvers: 20,
      status: "live",
      createdAt: new Date(),
    })
  );
});

test("Cross-user: Bob cannot modify Alice's match", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("matches").doc("match_alice").set({
      id: "match_alice",
      creatorId: ALICE_UID,
      teamAName: "Lions CC",
      teamBName: "Tigers CC",
      matchFormat: "T20",
      totalOvers: 20,
      status: "live",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("matches").doc("match_alice").update({
      status: "completed",
    })
  );
});

test("Creator: Alice can update her own match score", async () => {
  const createdDate = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("matches").doc("match_alice_2").set({
      id: "match_alice_2",
      creatorId: ALICE_UID,
      teamAName: "Lions CC",
      teamBName: "Tigers CC",
      matchFormat: "T20",
      totalOvers: 20,
      status: "live",
      teamAScore: 50,
      createdAt: createdDate,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("matches").doc("match_alice_2").update({
      teamAScore: 54,
    })
  );
});
