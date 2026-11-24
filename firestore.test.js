const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

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

test("Unauthenticated user: denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("studioProjects").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("studioProjects").doc("proj_1").set({
      userId: ALICE_UID,
      toolId: "text_to_video",
      title: "Test",
      prompt: "Cyberpunk rain",
      aiOutput: "Scene 1",
      presetTag: "Ruby Noir",
      status: "COMPLETED",
      createdAt: past,
      updatedAt: past,
    })
  );
});

test("UserProfile: owner create, partial update, list block, and cross-user denial", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Creator",
      email: "alice@example.com",
      tier: "PRO",
      dailyAiQuotaUsed: 2,
      dailyAiQuotaLimit: 100,
      autoDeleteCache: true,
      preferredColorGrade: "Ruby Noir",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      dailyAiQuotaUsed: 3,
      autoDeleteCache: false,
      preferredColorGrade: "Cyber Obsidian",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject listing /users
  await assertFails(aliceDb.collection("users").get());
  // Reject cross-user read & write
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      tier: "FREE",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      dailyAiQuotaUsed: 4,
      updatedAt: new Date(Date.now() + 120000),
    })
  );
});

test("StudioProjects: Kotlin create, update, list, delete and negative guards", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("studioProjects");

  await assertSucceeds(
    col.doc("proj_01").set({
      userId: ALICE_UID,
      toolId: "text_to_video",
      title: "Neon Tokyo Drift",
      prompt: "Rainy neon alley in Tokyo with glowing ruby reflections",
      aiOutput: "Scene 1: Dolly zoom into wet asphalt...",
      presetTag: "Ruby Noir",
      status: "COMPLETED",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(col.get());

  await assertSucceeds(
    col.doc("proj_01").update({
      title: "Neon Tokyo Drift v2",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject @DocumentId "id" inside body
  await assertFails(
    col.doc("proj_02").set({
      id: "proj_02",
      userId: ALICE_UID,
      toolId: "upscale_4k",
      title: "Bad Doc",
      prompt: "Upscale portrait",
      aiOutput: "Done",
      presetTag: "4K HDR",
      status: "COMPLETED",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Reject cross-user list/get/delete
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("studioProjects").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("studioProjects").doc("proj_01").delete()
  );
});

test("DownloadTasks: create, update status, list, and reject invalid platform/format", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 5000);
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("downloadTasks");

  await assertSucceeds(
    col.doc("dl_01").set({
      userId: ALICE_UID,
      url: "https://youtube.com/watch?v=dQw4w9WgXcQ",
      platform: "YOUTUBE",
      title: "Studio Cinematic Reel",
      format: "MP4_4K",
      muxingMode: "AUDIO_VIDEO_SYNC",
      status: "READY",
      aiSummary: "4K stream profile ready with synchronized AAC audio.",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    col.doc("dl_01").update({
      status: "COMPLETED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(col.get());

  // Invalid format enum must fail
  await assertFails(
    col.doc("dl_02").set({
      userId: ALICE_UID,
      url: "https://tiktok.com/@redtor/video/123",
      platform: "TIKTOK",
      title: "Invalid",
      format: "AVI_RAW",
      muxingMode: "NO_WATERMARK_CLEAN",
      status: "READY",
      aiSummary: "",
      createdAt: past,
      updatedAt: past,
    })
  );
});

test("ChatMessages & CreatorAssets: immutable create/list/delete and query filters", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  const chatCol = aliceDb.collection("users").doc(ALICE_UID).collection("chatMessages");
  await assertSucceeds(
    chatCol.doc("msg_01").set({
      userId: ALICE_UID,
      platform: "TIKTOK",
      role: "user",
      content: "Buatkan hook video viral 3 detik tentang AI editing.",
      createdAt: past,
    })
  );

  // Filtered platform query matching Kotlin repository
  await assertSucceeds(chatCol.where("platform", "==", "TIKTOK").get());
  // Immutable update must fail
  await assertFails(chatCol.doc("msg_01").update({ content: "Tampered" }));

  const assetCol = aliceDb.collection("users").doc(ALICE_UID).collection("creatorAssets");
  await assertSucceeds(
    assetCol.doc("asset_01").set({
      userId: ALICE_UID,
      assetType: "SCRIPT",
      platform: "INSTAGRAM",
      topic: "Liquid Glass UI Tutorial",
      content: "HOOK: Stop using flat cards...",
      createdAt: past,
    })
  );

  await assertSucceeds(assetCol.get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("creatorAssets").get()
  );
});
