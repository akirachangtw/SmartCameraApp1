---
id: exp-runtime-state-vs-fixture-output-01
status: active
topic: fixture-hygiene
trigger: "Running a regression/test suite modifies Git-tracked files in the working tree — a report, a state file, a log — such that `git status` is dirty after a clean run, and someone has to `git checkout --` it back before every commit."
decision: "First identify whether the file is a genuine runtime latch the code under test depends on (read/write cycle the production logic actually needs — check for read-back assertions, cooldown/CB counters, or a `report_records()`-style consumer) or is purely disposable test output. If it is a latch the runner needs, do not delete the read/write capability — only relocate WHERE it is written: move the path to a durable, gitignored root that is outside any auto-swept/housekeeping-cleaned directory (verify with `git check-ignore -v` and by reading the housekeeping script's own deletion-scope guard, not by assuming a `.tmp/`-looking path is safe). Only after the write path is relocated should the now-stale tracked copies be `git rm`'d."
why: "Deleting the read/write mechanism outright breaks whatever CB/stop-loss or audit logic consumes that state, even though it silences the symptom (no more dirty working tree). The actual defect is a path-location choice made when the fixture was authored, not the existence of the state file. Conflating 'this file causes VCS noise' with 'this file should not exist' throws away test coverage of the real runtime read/write contract. Also: `housekeeping.sh`-style cleanup scripts often scope their `rm -rf` to one root by first-argument contract — verify a new latch root is a *sibling*, not nested inside, that scoped root before assuming it's safe from auto-cleanup."
reuse_when: "A fixture payload or test harness embeds a repo-relative path to a file the test itself writes and later reads back (report/log/state), and that path currently resolves inside a tracked directory."
avoid_when: "The dirty file is genuinely disposable test output with no downstream reader in the code under test (confirm by grep — no `report_records()`/read-back/assertion consumer references it) — in that case just gitignore or delete it outright, relocating serves no purpose. Also avoid applying this if the runner itself (not just fixtures) needs modification — this pattern is scoped to fixture-payload path changes, not runner/schema logic changes."
confidence: high
source_refs:
  - "RGR-FIXTURE-REPORT-PATH-01 (CLOSED) — 14 payload report_path fields relocated to .tmp/rigor-gate/fixture-latch/, commit de6072d"
  - "RGR-AUTO-CLEANUP-01 (CLOSED) — the timestamp-evidence corruption incident this pattern was extracted from"
last_confirmed: 2026-09-24
retire_when: "All fixture suites in this skill that produce report/state output write exclusively to non-tracked roots by construction (e.g. a shared helper that enforces the gitignored root, rather than each payload hardcoding a path), such that a new fixture author cannot reintroduce a tracked output path without an explicit override."
---

# Runtime state files that pollute the working tree: relocate, don't delete

See `trigger`/`decision`/`why` above for the full pattern. Concretely: `run_preauth_matrix.sh` and
`run_no_call_tests.sh` both `rm -f` their report path before running (confirming it is pure output,
not a seeded input) and then read it back via `grep`/`cat` for CB-1~CB-4 assertions — so the
read/write capability itself is load-bearing. The fix changed only the `report_path` value inside
each fixture payload JSON, from a path under `runner/fixtures/.../reports/` (tracked) to
`.tmp/rigor-gate/fixture-latch/<name>.report.md` (confirmed gitignored, and confirmed to be a
*sibling* of — not nested inside — `TELEMETRY_ROOT`'s `ticket-repair` subtree that
`housekeeping.sh`'s `rm -rf` is scoped to). The stale tracked copies were then `git rm`'d. Two full
regression runs (before and after the commit) both showed zero `git status --short` reports-related
diff.
