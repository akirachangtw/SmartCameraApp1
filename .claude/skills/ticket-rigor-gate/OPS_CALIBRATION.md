# Ticket Rigor Gate — Ops Calibration

This file holds the calibration accounting for Advisor Repair Mode: the ledger, the sampling regime, and the Phase promotion bar. It is one of three carriers of the `ticket-rigor-gate` skill, split out of `SKILL.md` by `RGR-RESTRUCT-01`. Core review law lives in `SKILL.md`; repair routing and the Post-Signature Erratum Lane live in `REPAIR_ROUTING.md`. This file is the single legislative place for the calibration bar; other files point here rather than restating it.

## Repair Ledger

One line per case, appended to a single ledger location: `ticket-id | class | proposed | accepted / rejected | date`. Do not generate a report file per repair — the ledger is the record. Rejections MUST be logged; a silently dropped rejection corrupts the calibration counter that the Phase Promotion Criteria below depend on.

## Phase Promotion Criteria (C-3 校準閘)

Each Advisor Repair whitelist class (see `REPAIR_ROUTING.md`'s Repair Whitelist) starts in **Phase 0 (advisory only)**. A class is not eligible for **Phase 1 (A-1 auto-apply to disk)** until it clears the calibration bar: **N = 10 at 90% or higher** — ten repair proposals of that class, each accepted by the human without modification, at a 90% or higher acceptance rate.

Before a class clears this bar, its repairs remain proposal-only: emitted as a diff for human review, and MUST NOT be written to disk. Any rejection resets that class's counter to zero, regardless of how close it was to the bar.

> 若日後決意退役，MUST 同批立 supersede 紀錄，禁靜默。

This mirrors the shadow-mode rule for gate verdicts defined in `SKILL.md`'s Iteration And Escalation section; the two counters are independent — passing one does not advance the other.

## Calibration Coupling

Each ratification or rejection of an erratum (see `REPAIR_ROUTING.md`'s Post-Signature Erratum Lane) counts toward the **A-1 class counter** under the Phase Promotion Criteria above, on the same terms — a rejection resets A-1 to zero. This is the point of the lane beyond convenience: it converts routine execution into calibration evidence, so the N = 10 bar is reached from real work instead of from a separate exercise.

## Verification Sampling

Codex output (see `REPAIR_ROUTING.md`'s Cross-Review Execution Leg) is an executor report and carries no evidential weight by itself. Verification is **sampled, not exhaustive** — at least 3 diffs read per 10 erratum entries, chosen by the human or by the host session, and read against the cited source rather than against Codex's summary of it. A sampled diff that fails counts as a rejection under Calibration Coupling above.
