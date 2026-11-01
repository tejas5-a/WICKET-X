# WicketX — Live Scoring Visibility & Role Permissions

Architectural blueprint to decouple WicketX into distinct **Scorer** and **Viewer** experiences, secure scoring permissions via creator authorization and PIN-code sharing, and synchronize live cricket scores in real-time.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed based on your requirements:
> - **Scorer Permission Model**: The match creator is the authoritative scorer by default. Scoring authorization can be delegated to co-scorers or club officials using a secure **4-digit Scorer PIN** generated per match.
> - **Real-time Live Engine**: Supabase Realtime / REST API integration using OkHttp WebSocket and Retrofit client with configurable credentials (`SUPABASE_URL` and `SUPABASE_ANON_KEY`), backed by local Room database caching for zero-latency offline resilience and duplicate prevention.
> - **Primary Viewer Experience**: Opening a live match defaults directly to the **Full Scorecard & Live Tracker** view with batting tables, bowling analysis, run rates, ball-by-ball commentary reel, and live connection status.

---

## 1. Overview & Core Concept

Currently in WicketX, when a scorer inputs deliveries on their device, viewers or customers opening the match do not receive automated real-time updates and there is no strict boundary separating scoring actions from spectator viewing.

This implementation resolves the problem by:
1. **Role Enforcement**: Partitions match interaction into two dedicated UI modes:
   - **Viewer Mode (Spectators & Players)**: Clean, read-only live scoreboard and comprehensive scorecard with real-time ball-by-ball commentary. All keypad scoring controls (0, 1, 2, 3, 4, 6, extras, wicket) and undo functions are hidden.
   - **Scorer Mode (Authorized Official)**: Unlocks the full scoring engine keypad, bowler change, strike switch, wicket dialog, and delivery undo. Accessible only by the match creator or upon entering the verified match PIN.
2. **Real-time Synchronization Engine**:
   - Publishes every delivery and match score change to Supabase with unique delivery UUIDs.
   - Viewers subscribe to live match events via Supabase Realtime channel (`matches:{matchId}` and `deliveries:{matchId}`).
   - Immediate score hydration on match open, automatic exponential-backoff reconnection after internet disruptions, and deduplication against local Room storage.
3. **Live Connection Observability**: Displays a subtle, real-time connection badge on the scoreboard (`Live Synced`, `Updating...`, `Reconnecting`, `Offline Cache`).

---

## 2. User Experience & Visual Design

### A. Viewer Live Match Experience (Default for All Spectators)
- **Header & Match Overview**: Team names, team logos/crests, current batting team, total score and wickets (`142/3 in 16.4 ov`), Current Run Rate (CRR), Required Run Rate (RRR), target progress bar.
- **Batters & Bowler on Crease**:
  - Active striker with cricket bat icon, runs, balls, 4s, 6s, and strike rate.
  - Active non-striker with live stats.
  - Active bowler with overs, maidens, runs conceded, wickets, and economy rate.
- **Recent Deliveries Reel**: Horizontal scrollable reel of recent balls with color-coded pills:
  - Dot balls (`•`, Slate)
  - 1, 2, 3 runs (`1`, `2`, `3`, Light Surface)
  - Boundaries (`4`, Blue | `6`, Purple)
  - Wickets (`W`, Vibrant Red)
  - Extras (`Wd`, `Nb`, Orange)
- **Ball-by-Ball Live Commentary**: Chronological live feed explaining each delivery (e.g., *"FOUR! Tucked through mid-wicket with superb timing"*).
- **Full Scorecard Tables**:
  - Complete Batting table (R, B, 4s, 6s, SR, Dismissal method).
  - Complete Bowling figures (O, M, R, W, Econ, Wides, NBs).
  - Extras breakdown and Fall of Wickets timeline.
- **Scorer Entry Button**: A discreet `"Score This Match"` button in the top action bar that prompts for the 4-digit Scorer PIN for authorized scorers.

### B. Scorer Experience (Authorized Match Scorer)
- Unlocks the interactive keypad: `0`, `1`, `2`, `3`, `4`, `6` runs.
- Extra buttons: `WD` (Wide), `NB` (No-Ball + Free Hit), `BYE`, `LB` (Leg Bye).
- `OUT / WICKET` action launching the dismissal selector modal.
- `UNDO` button with confirmation to roll back previous delivery.
- Switch strike and bowler rotation controls.
- `"Share Scorer PIN"` modal allowing the creator to display or share the 4-digit PIN with a co-scorer.

---

## 3. Key Technical Decisions & Trade-Offs

| Decision | Chosen Solution | Rationale | Alternatives Considered |
| :--- | :--- | :--- | :--- |
| **Scorer Authorization** | Creator UID match + 4-digit Match PIN code | Allows club captains and organizers to share scoring duties with umpires without sharing personal credentials. | Rigid creator-only locking (prevents co-scoring if battery dies). |
| **Realtime Sync Protocol** | Supabase Realtime Channels (WebSocket) + REST Fallback | Sub-second latency for live ball broadcasts with reliable HTTP fallback on weak cellular networks. | Long polling only (high battery drain and 3-5s lag). |
| **Local State Management** | Room Database as Single Source of Truth + Replay Engine | Deliveries are persisted locally first. When cloud updates arrive, Room updates, triggering Compose `collectAsState` reactively. | Memory-only state (loses data on process kill). |
| **Delivery Deduplication** | Unique Delivery UUIDs (`del_uuid`) + Timestamp Ordering | Prevents duplicate ball counting during network retries or offline syncing. | Incremental integer IDs (collision-prone across devices). |

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                              WICKETX                                   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
            ┌───────────────────────┴───────────────────────┐
            ▼                                               ▼
┌───────────────────────────────┐               ┌───────────────────────┐
│          SCORER APP           │               │      VIEWER APP       │
│  (Creator or Validated PIN)   │               │   (Spectator/Guest)   │
├───────────────────────────────┤               ├───────────────────────┤
│ • Scoring Keypad (0..6)       │               │ • Live Scoreboard     │
│ • Extras & Wicket Dialog      │               │ • Full Scorecard      │
│ • Strike & Bowler Switch      │               │ • Ball Commentary     │
│ • Undo Delivery Control       │               │ • Connection Badge    │
│ • Share Scorer PIN Dialog     │               │ • "Enter PIN" Prompt  │
└───────────────┬───────────────┘               └───────────▲───────────┘
                │                                           │
                │ 1. Record Ball                            │ 4. Push Live
                ▼                                           │    Delivery
┌───────────────────────────────┐                           │
│     CricketScoringEngine      │                           │
│   (Pure Cricket Calculator)   │                           │
└───────────────┬───────────────┘                           │
                │                                           │
                │ 2. Save to Room                           │
                ▼                                           │
┌───────────────────────────────┐                           │
│   Room Database (Offline)     │                           │
│ (Matches, Deliveries, Teams)  │                           │
└───────────────┬───────────────┘                           │
                │                                           │
                │ 3. Broadcast Event                        │
                ▼                                           │
┌───────────────────────────────────────────────────────────┴───────────┐
│                   Supabase Realtime & REST API                        │
│                 Channel: matches:{matchId}                            │
│                 Row Level Security (RLS) Enforced                     │
└───────────────────────────────────────────────────────────────────────┘
```

### Data Models & Permission Entities

1. **`MatchEntity` updates**:
   - `scorerPin: String` (4-digit numeric code generated at match creation, e.g. `"4829"`).
   - `creatorId: String` (Firebase / Supabase user ID of creator).
   - `isPublished: Boolean` (viewable by all spectators).

2. **Supabase Realtime Service (`SupabaseSyncService.kt`)**:
   - WebSocket client connecting to Supabase Realtime endpoint: `wss://<project>.supabase.co/realtime/v1/websocket`.
   - Broadcasts delivery payload:
     ```json
     {
       "event": "NEW_DELIVERY",
       "matchId": "match_123",
       "delivery": { "runsBat": 4, "extraType": "NONE", "isWicket": false, ... }
     }
     ```
   - Auto-reconnect handler with exponential backoff on disconnect.

3. **Role Determination Flow**:
   - If `currentUserId == match.creatorId` $\rightarrow$ Scorer Mode.
   - If user entered correct `scorerPin` for `matchId` $\rightarrow$ Scorer Mode.
   - Otherwise $\rightarrow$ Viewer Mode (Scorecard & Live Commentary only).

---

## 5. Verification & Testing Plan

1. **Role Separation Verification**:
   - Verify Scorer mode displays the scoring keypad, bowler change, and undo buttons.
   - Verify Viewer mode displays the full scorecard, recent ball reel, and commentary, with zero editing controls.
2. **Two-Device Real-Time Sync Simulation**:
   - Execute local JVM Robolectric test simulating Device A (Scorer) inserting delivery events into the match stream and Device B (Viewer) observing Flow emissions in real time.
   - Test scoring sequences: Dot ball, Single (strike rotation check), Boundary Four, Six, Wide ball, Wicket.
3. **Reconnection & Deduplication Test**:
   - Simulate a network disconnect, emit buffered deliveries, reconnect, and confirm that deliveries are deduplicated and the scorecard remains 100% mathematically aligned.
4. **Applet Compilation**: Run `compile_applet` to verify zero build or syntax errors.
