# Tech Plan Spec Template

Use this structure for the final approved output.

## Tech Plan Spec

### 1. Decision Summary

- State whether the design is `Ready for $ticket-breakdown` or `Not ready for $ticket-breakdown`.
- Summarize the primary architectural decision in 2-4 bullets.

### 2. Validated Inputs

- L1 Context reviewed:
- L2 Guide reviewed:
- Code paths verified:
- Extra SSOT or reference docs verified:

### 3. Hardware And Capability Matrix

| Device or Lens | Capability | Constraints | Required Behavior | Notes |
|---|---|---|---|---|
| Example | Fixed-focus | No physical AF travel | Synthesize bounded lock path | Replace with real data |

### 3.1 Mutual Exclusion And Degradation

- ML Kit Face/Object detector rule:
- Scene switching hysteresis or cooldown:
- IMU/render-loop decoupling:
- FULL device behavior:
- LIMITED device behavior:
- LEGACY device behavior:

### 4. Defensive State Model

- Define the sealed states that matter.
- State the invariants that must remain true.
- State which old fields or booleans must not become competing sources of truth.

Suggested format:

```kotlin
sealed interface ExampleState {
    data object Idle : ExampleState
    data class Focusing(val requestId: Long) : ExampleState
    data class Locked(val requestId: Long) : ExampleState
    data class Failed(val requestId: Long) : ExampleState
}
```

### 5. UDF Ownership Boundaries

- UI responsibilities:
- ViewModel or domain responsibilities:
- Camera adapter responsibilities:
- Forbidden couplings:

### 6. Async Defense Patterns

- Request or session invalidation rule:
- Lens or device consistency rule:
- Cleanup timing rule:
- Callback drop policy:
- Hardware cooldown or throttle rule:

### 7. Scope Boundaries

Scope in:

- Item

Scope out:

- Item

### 8. Open Questions Or Blocking Revisions

- List only questions that materially affect approval.
- If none remain, write `None`.

### 9. Ticket Breakdown Readiness

- State whether the architecture is ready for `$ticket-breakdown`.
- List the defensive patterns that every implementation ticket must preserve.
