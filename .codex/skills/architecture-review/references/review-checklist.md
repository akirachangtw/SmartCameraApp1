# Android Camera Architecture Review Checklist

Use this checklist while reviewing L1 and L2 artifacts.

## 1. Hardware And Capability Matrix

- Has each relevant lens or device been identified explicitly?
- Does the plan separate fixed-focus, continuous AF, tap-to-focus, and optional Camera2-only behavior?
- Is fixed-focus bypass defined as a controlled architectural rule rather than an ad hoc UI workaround?
- Are VCM motor delays, AF cooldowns, or request throttles defined where focus hardware is involved?
- Does the design avoid assuming a capability because one test device supports it?
- Does the plan define FULL, LIMITED, and LEGACY behavior if the feature depends on camera capability?

Red flags:

- "Front camera should probably focus the same way"
- "We can infer support from success callbacks"
- "UI will special-case unsupported hardware"

## 2. State Machine Defense

- Are core states expressed as sealed classes or sealed interfaces with mutually exclusive meaning?
- Can stale target coordinates, lock flags, or previous focus results leak into the next request?
- Does each transition define who is allowed to emit it?
- Are derived booleans read-only views of richer state rather than competing sources of truth?
- Is scene switching protected by explicit hysteresis or equivalent gating when detector modes can change?

Red flags:

- One boolean means multiple product semantics
- Old target data survives after camera switch or reset
- Success and lock semantics are collapsed into the same flag

## 3. UDF Boundaries

- Does Compose render immutable state and emit intents only?
- Does ViewModel or domain orchestration own camera decision-making?
- Are CameraX or Camera2 callbacks translated into domain events before UI sees them?
- Can UI remain responsive without blocking on AF or AE callbacks?

Red flags:

- Compose directly interprets hardware callbacks
- UI merges product semantics from multiple low-level flags
- View logic depends on callback timing to stay consistent

## 4. ML Kit And Sensor Mutual Exclusion

- If Face Detection and Object Detection both appear, can only one be active in the relevant pipeline at a time?
- Is detector switching gated by a concrete hysteresis or cooldown rule?
- Is IMU sampling decoupled from render cadence when 60fps UI and higher-rate sensor input coexist?

Red flags:

- detector switching without a debounce or hysteresis policy
- two detection pipelines emitting competing UI states
- render code consuming raw sensor frequency directly

## 5. Async And Lifecycle Defense

- Is there a request identity, session ID, or equivalent invalidation token?
- Are late callbacks dropped safely after lens switch, pause/resume, or reset?
- Does cleanup happen only when the underlying session can actually accept it?
- Is the plan explicit about cancellation not being sufficient on its own?

Red flags:

- "cancel() should be enough"
- state reset without invalidating in-flight callbacks
- cleanup timing depends on optimistic lifecycle ordering

## 6. Implementation Drift Defense

- Is scope clearly split into in-scope and out-of-scope work?
- Are acceptance criteria tied to the defensive patterns above?
- Would the next `$ticket-breakdown` step be able to produce small, isolated tickets?

Red flags:

- architecture approval depends on a later unspecified refactor
- L2 quietly rewrites unrelated modules
- defensive invariants are implied but not written down
