"""Table-driven tests for preflight_core.decide().

Source: INFRA-PREFLIGHT-CORE-01 (Rev C).

Standard library unittest + subTest only (no pytest, no root tests/ dir —
HC-5). Every row of preflight_rules.json becomes one subTest; adding a rule
row there adds a subTest here automatically with zero code change.
"""

from __future__ import annotations

import json
import os
import sys
import tempfile
import unittest
from pathlib import Path

_SCRIPTS_DIR = Path(__file__).resolve().parent.parent / "scripts"
if str(_SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(_SCRIPTS_DIR))

import preflight_core as core  # noqa: E402

_RULES_PATH = _SCRIPTS_DIR / "preflight_rules.json"


def _load_rows() -> list[dict]:
    with open(_RULES_PATH, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data["rows"]


def _build_context(d: dict) -> core.ContextFacts:
    return core.ContextFacts(
        ticket_id=d["ticket_id"],
        repo_root=d["repo_root"],
        worktree_path=d["worktree_path"],
        head_sha=d["head_sha"],
        config_revision=d["config_revision"],
        collected_at=d["collected_at"],
        activation_intent=core.ActivationIntent(d["activation_intent"]),
    )


def _build_environment(d: dict) -> core.EnvironmentFacts:
    return core.EnvironmentFacts(
        v0_env=core.V0Env(d["v0_env"]),
        baseline_check=core.BaselineCheck(d["baseline_check"]),
        v1_scope=core.V1Scope(d["v1_scope"]),
        v1_provenance=core.V1Provenance(d["v1_provenance"]),
        blob_check=core.BlobCheck(d["blob_check"]),
        v2_semantics=core.V2Semantics(d["v2_semantics"]),
        v2_provenance=core.V2Provenance(d["v2_provenance"]),
    )


_EXISTING_FINAL_STATUSES = frozenset(
    {
        core.FINAL_STATUS_BLOCKED_ENV_MISMATCH,
        core.FINAL_STATUS_BLOCKED_UNATTRIBUTED_STATE,
        core.FINAL_STATUS_BLOCKED_SCOPE_OR_TARGET_MISMATCH,
        core.FINAL_STATUS_ALREADY_EXECUTED_PENDING_CLOSURE,
        core.FINAL_STATUS_CANDIDATE_SUPERSEDED,
        core.FINAL_STATUS_BASELINE_MATCHED,
    }
)


class DecisionTableTest(unittest.TestCase):
    """Every preflight_rules.json row -> one subTest. AC: 決策語義測試 100% 映射。"""

    def test_all_rows(self) -> None:
        rows = _load_rows()
        self.assertGreaterEqual(len(rows), 12, "decision table should cover >=12 rows")
        for row in rows:
            with self.subTest(row=row["id"]):
                ctx = _build_context(row["context"])
                env = _build_environment(row["environment"])
                decision = core.decide(core.ActivationFacts(ctx, env))

                expected = row["expected"]
                self.assertEqual(decision.final_status, expected["final_status"])
                self.assertEqual(decision.reason_code, expected["reason_code"])
                self.assertEqual(decision.process_exit_code, expected["process_exit_code"])
                self.assertEqual(decision.write_authority, expected["write_authority"])
                self.assertEqual(decision.may_activate_ticket, expected["may_activate_ticket"])
                self.assertEqual(decision.v2_probe_may_write, expected["v2_probe_may_write"])
                self.assertEqual(
                    decision.may_run_tests_with_authority,
                    expected["may_run_tests_with_authority"],
                )
                self.assertEqual(decision.evidence_required, expected["evidence_required"])
                self.assertEqual(decision.ticket_id, row["context"]["ticket_id"])
                self.assertEqual(decision.context_id, ctx.digest())
                self.assertIn(decision.final_status, _EXISTING_FINAL_STATUSES)

                # v2_probe_may_write is a fixed contract value (V2 zero-write).
                self.assertFalse(decision.v2_probe_may_write)


class UnlistedCombinationTest(unittest.TestCase):
    """R-2: any combination not matched by F-0..F-7 MUST fail-closed."""

    def _ctx(self) -> core.ContextFacts:
        return core.ContextFacts(
            "T-UNLISTED", "/repo", "/repo", "cafebabe", "rev-0",
            "2026-09-06T00:00:00Z", core.ActivationIntent.IMPLICIT_RESUME,
        )

    def test_random_unlisted_combination(self) -> None:
        env = core.EnvironmentFacts(
            v0_env=core.V0Env.PASS,
            baseline_check=core.BaselineCheck.VALID,
            v1_scope=core.V1Scope.CHANGED_TARGETS,
            v1_provenance=core.V1Provenance.NOT_APPLICABLE,  # no rule expects this pairing
            blob_check=core.BlobCheck.NOT_EVALUATED,
            v2_semantics=core.V2Semantics.NOT_EVALUATED,
            v2_provenance=core.V2Provenance.NOT_EVALUATED,
        )
        decision = core.decide(core.ActivationFacts(self._ctx(), env))
        self.assertEqual(decision.final_status, core.FINAL_STATUS_BLOCKED_ENV_MISMATCH)
        self.assertEqual(decision.reason_code, core.REASON_UNLISTED_COMBINATION)
        self.assertFalse(decision.may_activate_ticket)
        self.assertFalse(decision.v2_probe_may_write)
        self.assertEqual(decision.write_authority, "NO")

    def test_r1_blob_failures_all_four_variants(self) -> None:
        for blob_value in (
            core.BlobCheck.TARGET_MISSING,
            core.BlobCheck.COMMAND_FAILED,
            core.BlobCheck.INVALID_TICKET_VALUE,
            core.BlobCheck.MISMATCH,
        ):
            with self.subTest(blob_check=blob_value.value):
                env = core.EnvironmentFacts(
                    v0_env=core.V0Env.PASS,
                    baseline_check=core.BaselineCheck.VALID,
                    v1_scope=core.V1Scope.NOT_EVALUATED,
                    v1_provenance=core.V1Provenance.NOT_APPLICABLE,
                    blob_check=blob_value,
                    v2_semantics=core.V2Semantics.MISSING,
                    v2_provenance=core.V2Provenance.UNKNOWN,
                )
                decision = core.decide(core.ActivationFacts(self._ctx(), env))
                self.assertEqual(
                    decision.final_status, core.FINAL_STATUS_BLOCKED_SCOPE_OR_TARGET_MISMATCH
                )
                self.assertEqual(decision.write_authority, "NO")
                self.assertFalse(decision.may_activate_ticket)


class ProducerClosureTest(unittest.TestCase):
    """R-6: v2_provenance=KNOWN / v2_semantics=FAILED have no producer and
    MUST fail-closed via R-2, not be given a bespoke producer to pass."""

    def _ctx(self) -> core.ContextFacts:
        return core.ContextFacts(
            "T-R6", "/repo", "/repo", "cafebabe", "rev-0",
            "2026-09-06T00:00:00Z", core.ActivationIntent.EXPLICIT_SWITCH,
        )

    def test_v2_provenance_known_is_unlisted(self) -> None:
        env = core.EnvironmentFacts(
            v0_env=core.V0Env.PASS,
            baseline_check=core.BaselineCheck.VALID,
            v1_scope=core.V1Scope.UNCHANGED_TARGETS,
            v1_provenance=core.V1Provenance.NOT_APPLICABLE,
            blob_check=core.BlobCheck.MATCH,
            v2_semantics=core.V2Semantics.SATISFIED,
            v2_provenance=core.V2Provenance.KNOWN,
        )
        decision = core.decide(core.ActivationFacts(self._ctx(), env))
        self.assertEqual(decision.final_status, core.FINAL_STATUS_BLOCKED_ENV_MISMATCH)
        self.assertEqual(decision.reason_code, core.REASON_UNLISTED_COMBINATION)
        self.assertFalse(decision.may_activate_ticket)

    def test_v2_semantics_failed_is_unlisted(self) -> None:
        env = core.EnvironmentFacts(
            v0_env=core.V0Env.PASS,
            baseline_check=core.BaselineCheck.VALID,
            v1_scope=core.V1Scope.UNCHANGED_TARGETS,
            v1_provenance=core.V1Provenance.NOT_APPLICABLE,
            blob_check=core.BlobCheck.MATCH,
            v2_semantics=core.V2Semantics.FAILED,
            v2_provenance=core.V2Provenance.UNKNOWN,
        )
        decision = core.decide(core.ActivationFacts(self._ctx(), env))
        self.assertEqual(decision.final_status, core.FINAL_STATUS_BLOCKED_ENV_MISMATCH)
        self.assertEqual(decision.reason_code, core.REASON_UNLISTED_COMBINATION)
        self.assertFalse(decision.may_activate_ticket)


class ExitCodeCompatibilityTest(unittest.TestCase):
    """R-5: process_exit_code is a pure function of v0_env, never inferred
    from final_status. Exercises the four v0_env values against the table."""

    def _ctx(self) -> core.ContextFacts:
        return core.ContextFacts(
            "T-DEMO", "/repo", "/repo", "deadbeef", "rev-0",
            "2026-09-06T00:00:00Z", core.ActivationIntent.IMPLICIT_RESUME,
        )

    def test_every_row_exit_code_matches_v0_env_rule(self) -> None:
        for row in _load_rows():
            with self.subTest(row=row["id"]):
                v0_env = row["environment"]["v0_env"]
                expected_exit = 1 if v0_env != "PASS" else 0
                self.assertEqual(row["expected"]["process_exit_code"], expected_exit)

    def test_exit0_v0pass_with_blocked_unattributed_state(self) -> None:
        # F-5: v0_env=PASS but a blocked final_status (unattributed commits
        # on the ticket's own scope) MUST still yield exit 0, not 1 —
        # process_exit_code is never inferred from final_status.
        env = core.EnvironmentFacts(
            v0_env=core.V0Env.PASS,
            baseline_check=core.BaselineCheck.VALID,
            v1_scope=core.V1Scope.CHANGED_TARGETS,
            v1_provenance=core.V1Provenance.UNATTRIBUTED,
            blob_check=core.BlobCheck.MATCH,
            v2_semantics=core.V2Semantics.MISSING,
            v2_provenance=core.V2Provenance.UNKNOWN,
        )
        decision = core.decide(core.ActivationFacts(self._ctx(), env))
        self.assertEqual(decision.final_status, core.FINAL_STATUS_BLOCKED_UNATTRIBUTED_STATE)
        self.assertEqual(decision.process_exit_code, 0)
        self.assertFalse(decision.may_activate_ticket)

    def test_exit0_v0pass_with_blocked_scope_or_target_mismatch(self) -> None:
        # F-2: v0_env=PASS but a blob-check hard failure MUST still yield
        # exit 0, not 1.
        env = core.EnvironmentFacts(
            v0_env=core.V0Env.PASS,
            baseline_check=core.BaselineCheck.VALID,
            v1_scope=core.V1Scope.NOT_EVALUATED,
            v1_provenance=core.V1Provenance.NOT_APPLICABLE,
            blob_check=core.BlobCheck.MISMATCH,
            v2_semantics=core.V2Semantics.MISSING,
            v2_provenance=core.V2Provenance.UNKNOWN,
        )
        decision = core.decide(core.ActivationFacts(self._ctx(), env))
        self.assertEqual(decision.final_status, core.FINAL_STATUS_BLOCKED_SCOPE_OR_TARGET_MISMATCH)
        self.assertEqual(decision.process_exit_code, 0)
        self.assertFalse(decision.may_activate_ticket)

    def test_exit0_v0pass_with_candidate_superseded(self) -> None:
        # F-4: v0_env=PASS with other-ticket attribution (CANDIDATE_SUPERSEDED)
        # MUST still yield exit 0, not 1.
        env = core.EnvironmentFacts(
            v0_env=core.V0Env.PASS,
            baseline_check=core.BaselineCheck.VALID,
            v1_scope=core.V1Scope.CHANGED_TARGETS,
            v1_provenance=core.V1Provenance.OTHER_TICKET,
            blob_check=core.BlobCheck.MATCH,
            v2_semantics=core.V2Semantics.MISSING,
            v2_provenance=core.V2Provenance.UNKNOWN,
        )
        decision = core.decide(core.ActivationFacts(self._ctx(), env))
        self.assertEqual(decision.final_status, core.FINAL_STATUS_CANDIDATE_SUPERSEDED)
        self.assertEqual(decision.process_exit_code, 0)
        self.assertTrue(decision.may_activate_ticket)

    def test_exit1_v0_each_failure_value(self) -> None:
        # F-0: every non-PASS v0_env value MUST yield exit 1, each on its own
        # subTest (DETACHED_HEAD / NO_ACTIVE_TICKET / OUT_OF_SCOPE).
        for v0_value in (
            core.V0Env.DETACHED_HEAD,
            core.V0Env.NO_ACTIVE_TICKET,
            core.V0Env.OUT_OF_SCOPE,
        ):
            with self.subTest(v0_env=v0_value.value):
                env = core.EnvironmentFacts(
                    v0_env=v0_value,
                    baseline_check=core.BaselineCheck.NOT_EVALUATED,
                    v1_scope=core.V1Scope.NOT_EVALUATED,
                    v1_provenance=core.V1Provenance.NOT_APPLICABLE,
                    blob_check=core.BlobCheck.NOT_EVALUATED,
                    v2_semantics=core.V2Semantics.NOT_EVALUATED,
                    v2_provenance=core.V2Provenance.NOT_EVALUATED,
                )
                decision = core.decide(core.ActivationFacts(self._ctx(), env))
                self.assertEqual(decision.final_status, core.FINAL_STATUS_BLOCKED_ENV_MISMATCH)
                self.assertEqual(decision.process_exit_code, 1)
                self.assertFalse(decision.may_activate_ticket)


class PurityTest(unittest.TestCase):
    """Core purity: same input -> same output under a cleared environment,
    a relocated working directory, and no .git present. HC-1."""

    def test_no_purity_violation_tokens_in_source(self) -> None:
        import inspect

        source = inspect.getsource(core)
        for banned in ("subprocess", "open(", "os.environ"):
            self.assertNotIn(banned, source, f"purity violation literal: {banned}")

    def test_decide_is_stable_under_cleared_environment_and_cwd(self) -> None:
        rows = _load_rows()
        row = rows[0]
        ctx = _build_context(row["context"])
        env = _build_environment(row["environment"])
        baseline_decision = core.decide(core.ActivationFacts(ctx, env))

        saved_environ = dict(os.environ)
        original_cwd = os.getcwd()
        tmp_dir = tempfile.mkdtemp()
        try:
            os.environ.clear()
            os.chdir(tmp_dir)
            relocated_decision = core.decide(core.ActivationFacts(ctx, env))
        finally:
            os.chdir(original_cwd)
            os.environ.clear()
            os.environ.update(saved_environ)

        self.assertEqual(relocated_decision, baseline_decision)

    def test_context_digest_varies_with_activation_intent(self) -> None:
        base_kwargs = dict(
            ticket_id="T-CTX",
            repo_root="/repo",
            worktree_path="/repo",
            head_sha="deadbeef",
            config_revision="rev-0",
            collected_at="2026-09-06T00:00:00Z",
        )
        ctx_explicit = core.ContextFacts(
            **base_kwargs, activation_intent=core.ActivationIntent.EXPLICIT_SWITCH
        )
        ctx_implicit = core.ContextFacts(
            **base_kwargs, activation_intent=core.ActivationIntent.IMPLICIT_RESUME
        )
        self.assertNotEqual(ctx_explicit.digest(), ctx_implicit.digest())


if __name__ == "__main__":
    unittest.main()
