# Ticket Rigor Gate — Ops Calibration

This file holds the calibration accounting for Advisor Repair Mode: the ledger, the sampling regime, and the Phase promotion bar. It is one of three carriers of the `ticket-rigor-gate` skill, split out of `SKILL.md` by `RGR-RESTRUCT-01`. Core review law lives in `SKILL.md`; repair routing and the Post-Signature Erratum Lane live in `REPAIR_ROUTING.md`. This file is the single legislative place for the calibration bar; other files point here rather than restating it.

## Repair Ledger

One line per case, appended to a single ledger location: `ticket-id | class | proposed | accepted / rejected | date`. Do not generate a report file per repair — the ledger is the record. Rejections MUST be logged; a silently dropped rejection corrupts the calibration counter that the Phase Promotion Criteria below depend on.

**Live-lane mapping (no separate ledger platform).** For `single_attachment_live_lane` repairs, this ledger's fields are carried by the existing repair-runner quality report and its batch closeout — no independent ledger file or format is created. Field correspondence: `ticket-id` ↔ quality report `Ticket ID`; `class` ↔ `findings[].class` in the originating payload; `proposed` ↔ quality report `Disposition`; `accepted / rejected` ↔ quality report `Apply Status` (`applied` is the accepted-equivalent; `rejected` / `escalated` are the rejected-equivalent); `date` ↔ the receipt's UTC timestamp. A batch closeout is this ledger's summary view for that session; `report_records()` already parses quality-report entries mechanically, so the calibration counter remains derivable from a real, existing record rather than a second thing to maintain.

## Phase Promotion Criteria (C-3 校準閘)

Each Advisor Repair whitelist class (see `REPAIR_ROUTING.md`'s Repair Whitelist) starts in **Phase 0 (advisory only)**. A class is not eligible for **Phase 1 (A-1 auto-apply to disk)** until it clears the calibration bar: **N = 10 at 90% or higher** — ten repair proposals of that class, each accepted by the human without modification, at a 90% or higher acceptance rate.

Before a class clears this bar, its repairs remain proposal-only: emitted as a diff for human review, and MUST NOT be written to disk. Any rejection resets that class's counter to zero, regardless of how close it was to the bar.

A finding that satisfies all six release conditions of `REPAIR_ROUTING.md`'s `Dispatch Authority` section is exempt from this Phase 0 proposal-only restriction for that finding; a finding that does not satisfy them falls back to the Phase 0/1 law above unchanged. The threshold values (N = 10, ≥ 90%), the rejection-reset rule, and the ledger obligation are unchanged; a preauthorized apply's result still counts toward that class's calibration counter.

**Phase 0 bootstrap exception (`single_attachment_live_lane`, A-1/A-2 only).** The N = 10 bar above presumes a population of prior proposals to draw from. For A-1/A-2 under the live lane, that population is necessarily zero before the lane's own runner has ever consumed a non-fixture payload — no prior calibration sample can exist for a mechanism that has not yet run once outside a fixture. Requiring N = 10 real samples before the lane may apply its first real finding is therefore not a stricter bar but a deadlock: the evidence the bar demands can only be produced by the thing the bar is gating. `REPAIR_ROUTING.md`'s Single-Attachment Live Lane Carve-out resolves this the same way the six-condition Dispatch Authority exemption above already resolves the general case — by satisfying condition ① (authority anchor) through the committed carve-out clause itself rather than through a calibration count. This is a one-time bootstrap for A-1 and A-2 specifically, adjudicated in vault decision `wiki/decisions/SKAD/2026-09-21_RGR_Single_Attachment_Live_Lane_Authorization.md` KD5, not a general suspension of the N = 10 bar. **Reversal condition**: if the Repair Whitelist's Autonomy column for A-1 or A-2 changes from `Full`, this bootstrap no longer applies to that class and the ordinary Phase 0/1 bar governs it going forward.

> 若日後決意退役，MUST 同批立 supersede 紀錄，禁靜默。

This mirrors the shadow-mode rule for gate verdicts defined in `SKILL.md`'s Iteration And Escalation section; the two counters are independent — passing one does not advance the other.

## Calibration Coupling

Each ratification or rejection of an erratum (see `REPAIR_ROUTING.md`'s Post-Signature Erratum Lane) counts toward the **A-1 class counter** under the Phase Promotion Criteria above, on the same terms — a rejection resets A-1 to zero. This is the point of the lane beyond convenience: it converts routine execution into calibration evidence, so the N = 10 bar is reached from real work instead of from a separate exercise.

## Verification Sampling

Codex output (see `REPAIR_ROUTING.md`'s Cross-Review Execution Leg) is an executor report and carries no evidential weight by itself. Verification is **sampled, not exhaustive** — at least 3 diffs read per 10 erratum entries, chosen by the human or by the host session, and read against the cited source rather than against Codex's summary of it. A sampled diff that fails counts as a rejection under Calibration Coupling above.
