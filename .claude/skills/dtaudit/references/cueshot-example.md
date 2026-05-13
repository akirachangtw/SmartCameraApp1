# DTA Example: Cueshot SSOT v3.4.1 Audit

This is a real-world example of a Decision Traceability Audit performed on the Cueshot
camera app's SSOT (Single Source of Truth) specification document.

**Context**: 2 audit sessions produced 25 decisions total (9 original + 7 patch + 2 supplement
+ 7 audit-passed items). Target document was a 566-line Delta file overlaying SSOT v3.3.

---

## Example: Decision Registry (abbreviated)

| # | Decision | Source | Session |
|---|----------|--------|---------|
| — | Edge Warmth Deprecated → 3-channel Traction system | Session 1 Key Decisions | S1 |
| — | Logo removed from camera screen → post-capture card + Splash | Session 1 Key Decisions | S1 |
| — | AE/AF Lock included in MVP (0.8 day, D13-D14) | Session 1 Key Decisions | S1 |
| — | Haptic fully removed, replaced by visual feedback | Session 1 Key Decisions | S1 |
| — | Progress ring + cooldown MVP 300ms | Session 1 Key Decisions | S1 |
| — | Frosted glass capsule deprecated → linear gradient Scrim | Session 1 Key Decisions | S1 |
| 1 | Level Traction disappears on alignment | Session 1 Numbered | S1 |
| D10 | Shutter flash × breathing animation mutex | Session 2 Patch | S2 |
| D14 | PTF Level Traction fixed green color | Session 2 Patch | S2 |

---

## Example: Five-Layer Audit Findings

### Finding: Edge Warmth → Three-Channel Rename (Layer 4 FAIL)

| Layer | Result | Evidence |
|-------|--------|---------|
| L1 Explicit | ❌ | No section declares "Edge Warmth is deprecated" |
| L2 Values | N/A | — |
| L3 Implicit | ⚠️ | Document uses "Level Traction" and "Pitch Traction" throughout |
| L4 Naming | ❌ | §2.3 header still reads "Edge Warmth (邊緣暖光)" |
| L5 Absent | ❌ | Three-channel architecture declaration missing |

**Verdict**: FAIL — Naming change is the most visible decision yet has zero explicit coverage.

**Fix produced**:
- Renamed §2.3 header to include three-channel declaration
- Added Deprecated block explaining the rename
- Preserved `drawEdgeWarmth` function name for backward compatibility note

### Finding: Logo Removal (Layer 5 — Complete Absence)

| Layer | Result | Evidence |
|-------|--------|---------|
| L1-L5 | All ❌ | Zero references to Logo anywhere in 566-line document |

**Verdict**: FAIL (BLOCKER) — Entire decision has no footprint.

**Fix produced**: New Delta-11 section with decision statement + design rationale.

### Finding: AE/AF Lock MVP (Layer 3 — Implicit)

| Layer | Result | Evidence |
|-------|--------|---------|
| L1 Explicit | ❌ | No "v3.4 new" marker on the Locked row |
| L2 Values | ✅ | Locked state values are correct (α=0.85, 亮金) |
| L3 Implicit | ⚠️ | Locked row exists in Focus state table from v3.3 |
| L4 Naming | ✅ | — |
| L5 Absent | ✅ | Not absent, just unmarked |

**Verdict**: WARN — Feature exists but decision to "include in MVP" is untraceable.

**Fix produced**: Added `v3.4: formally included in MVP scope (0.8d, D13-D14)` annotation.

---

## Example: Gap Summary

| # | Gap | Severity | Section | Fix |
|---|-----|----------|---------|-----|
| G1 | §2.3 still named "Edge Warmth" | 🟡 MEDIUM | §2.3 L89 | Rename + Deprecated block |
| G2 | Logo removal missing entirely | 🔴 BLOCKER | N/A | New Delta-11 |
| G3 | AE/AF Lock MVP not labeled | 🟢 LOW | §3.3 L481 | Add version annotation |
| G4 | Progress ring not defined | 🟡 MEDIUM | §10.3.1 | New subsection in Delta-12 |
| G5 | Haptic removal not declared | 🟡 MEDIUM | N/A | New Delta-12 |
| G6 | Frosted glass Deprecated not stated | 🟢 LOW | §3.2 L503 | Add Deprecated note |

**Audit Verdict**: FAIL (1 BLOCKER + 3 MEDIUM)
→ After fixes applied: PASS (0 BLOCKER, 0 MEDIUM, 2 LOW resolved)

---

## Key Lessons from This Audit

1. **"Obvious" decisions are the most dangerous** — Edge Warmth rename was THE defining
   architectural decision of the session, yet the document header still used the old name.
   Layer 4 (naming) caught what Layers 1-3 would have missed.

2. **Negative decisions need explicit coverage** — "Remove Logo", "Remove Haptic" are
   decisions to NOT do something. Documents naturally cover what IS built, not what was
   deliberately removed. Layer 5 (absence) is the only way to catch these.

3. **Implicit ≠ Covered** — AE/AF Lock existed in the table since v3.3, but the decision
   to "formally include in MVP scope" was a v3.4 decision. Without Layer 3, this would
   have been reported as PASS (the feature exists!) when it should be WARN (the decision
   is untraceable).
