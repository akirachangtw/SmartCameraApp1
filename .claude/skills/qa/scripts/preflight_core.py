"""preflight_core.py — pure decision core for the Ticket Activation Protocol.

Source: INFRA-PREFLIGHT-CORE-01 (Rev C).

Contract (Hard Constraint 1): this module MUST NOT spawn child processes,
MUST NOT touch the filesystem, and MUST NOT read process-level environment
state. It is a pure function of its inputs: decide(facts) -> Decision, same
input -> same output, callable under a cleared environment, a random working
directory, and no version-control metadata present.

All Git / file / environment side effects belong to the Bash facts collector
and adapter (preflight_activation.sh), never to this module.
"""

from __future__ import annotations

import hashlib
import json
import sys
from dataclasses import asdict, dataclass
from enum import Enum


class ActivationIntent(str, Enum):
    EXPLICIT_SWITCH = "EXPLICIT_SWITCH"
    IMPLICIT_RESUME = "IMPLICIT_RESUME"


class V0Env(str, Enum):
    PASS = "PASS"
    DETACHED_HEAD = "DETACHED_HEAD"
    NO_ACTIVE_TICKET = "NO_ACTIVE_TICKET"
    OUT_OF_SCOPE = "OUT_OF_SCOPE"


class BaselineCheck(str, Enum):
    VALID = "VALID"
    MISSING = "MISSING"
    UNRESOLVABLE = "UNRESOLVABLE"
    NOT_EVALUATED = "NOT_EVALUATED"


class V1Scope(str, Enum):
    UNCHANGED_TARGETS = "UNCHANGED_TARGETS"
    CHANGED_TARGETS = "CHANGED_TARGETS"
    NOT_EVALUATED = "NOT_EVALUATED"


class V1Provenance(str, Enum):
    SELF_TICKET = "SELF_TICKET"
    OTHER_TICKET = "OTHER_TICKET"
    UNATTRIBUTED = "UNATTRIBUTED"
    NOT_APPLICABLE = "NOT_APPLICABLE"


class BlobCheck(str, Enum):
    MATCH = "MATCH"
    MISMATCH = "MISMATCH"
    SKIPPED_NEW = "SKIPPED_NEW"
    SKIPPED_NA = "SKIPPED_NA"
    TARGET_MISSING = "TARGET_MISSING"
    COMMAND_FAILED = "COMMAND_FAILED"
    INVALID_TICKET_VALUE = "INVALID_TICKET_VALUE"
    NOT_EVALUATED = "NOT_EVALUATED"


class V2Semantics(str, Enum):
    SATISFIED = "SATISFIED"
    PARTIAL = "PARTIAL"
    MISSING = "MISSING"
    NOT_EVALUATED = "NOT_EVALUATED"
    # FAILED has no producer in the frozen pre-image (R-6): kept constructible
    # so a schema-outside injection test can prove it is fail-closed via R-2
    # rather than raising at construction time, per AC "R-6 producer closure".
    FAILED = "FAILED"


class V2Provenance(str, Enum):
    UNKNOWN = "UNKNOWN"
    NOT_EVALUATED = "NOT_EVALUATED"
    # KNOWN has no producer in the frozen pre-image (R-6): kept constructible
    # for the same schema-outside fail-closed proof as V2Semantics.FAILED.
    KNOWN = "KNOWN"


# final_status: existing external enum literals (frozen pre-image 9895b3f),
# HC-3: MUST NOT add a new one.
FINAL_STATUS_BLOCKED_ENV_MISMATCH = "BLOCKED_ENV_MISMATCH"
FINAL_STATUS_BLOCKED_UNATTRIBUTED_STATE = "BLOCKED_UNATTRIBUTED_STATE"
FINAL_STATUS_BLOCKED_SCOPE_OR_TARGET_MISMATCH = "BLOCKED_SCOPE_OR_TARGET_MISMATCH"
FINAL_STATUS_ALREADY_EXECUTED_PENDING_CLOSURE = "ALREADY_EXECUTED_PENDING_CLOSURE"
FINAL_STATUS_CANDIDATE_SUPERSEDED = "CANDIDATE_SUPERSEDED"
FINAL_STATUS_BASELINE_MATCHED = "BASELINE_MATCHED"

_BLOCKED_STATUSES = frozenset(
    {
        FINAL_STATUS_BLOCKED_ENV_MISMATCH,
        FINAL_STATUS_BLOCKED_UNATTRIBUTED_STATE,
        FINAL_STATUS_BLOCKED_SCOPE_OR_TARGET_MISMATCH,
    }
)

REASON_UNLISTED_COMBINATION = "UNLISTED_COMBINATION"
REASON_CONSERVATIVE_MANUAL_NOT_MACHINE_READABLE = (
    "CONSERVATIVE_MANUAL_NOT_MACHINE_READABLE"
)

_UNCHANGED_BLOB_OK = frozenset({BlobCheck.MATCH, BlobCheck.SKIPPED_NEW, BlobCheck.SKIPPED_NA})
_BLOB_FAILURE = frozenset(
    {BlobCheck.MISMATCH, BlobCheck.TARGET_MISSING, BlobCheck.COMMAND_FAILED, BlobCheck.INVALID_TICKET_VALUE}
)
# F-0 sentinel combination (early-return standardized producer).
_F0_SENTINEL = {
    "baseline_check": BaselineCheck.NOT_EVALUATED,
    "v1_scope": V1Scope.NOT_EVALUATED,
    "v1_provenance": V1Provenance.NOT_APPLICABLE,
    "blob_check": BlobCheck.NOT_EVALUATED,
    "v2_semantics": V2Semantics.NOT_EVALUATED,
    "v2_provenance": V2Provenance.NOT_EVALUATED,
}


@dataclass(frozen=True)
class ContextFacts:
    ticket_id: str
    repo_root: str
    worktree_path: str
    head_sha: str
    config_revision: str
    collected_at: str
    activation_intent: ActivationIntent

    def digest(self) -> str:
        payload = {
            "ticket_id": self.ticket_id,
            "repo_root": self.repo_root,
            "worktree_path": self.worktree_path,
            "head_sha": self.head_sha,
            "config_revision": self.config_revision,
            "collected_at": self.collected_at,
            "activation_intent": self.activation_intent.value,
        }
        normalized = json.dumps(payload, sort_keys=True, separators=(",", ":"))
        return hashlib.sha256(normalized.encode("utf-8")).hexdigest()[:16]


@dataclass(frozen=True)
class EnvironmentFacts:
    v0_env: V0Env
    baseline_check: BaselineCheck
    v1_scope: V1Scope
    v1_provenance: V1Provenance
    blob_check: BlobCheck
    v2_semantics: V2Semantics
    v2_provenance: V2Provenance
    manual_authorization: str = "NOT_MACHINE_READABLE"


@dataclass(frozen=True)
class ActivationFacts:
    context: ContextFacts
    environment: EnvironmentFacts


@dataclass(frozen=True)
class Decision:
    context_id: str
    ticket_id: str
    final_status: str
    reason_code: str
    process_exit_code: int
    write_authority: str
    may_activate_ticket: bool
    v2_probe_may_write: bool
    may_run_tests_with_authority: bool
    evidence_required: str

    def as_dict(self) -> dict:
        return asdict(self)


def _is_f0_sentinel(env: EnvironmentFacts) -> bool:
    return (
        env.baseline_check == _F0_SENTINEL["baseline_check"]
        and env.v1_scope == _F0_SENTINEL["v1_scope"]
        and env.v1_provenance == _F0_SENTINEL["v1_provenance"]
        and env.blob_check == _F0_SENTINEL["blob_check"]
        and env.v2_semantics == _F0_SENTINEL["v2_semantics"]
        and env.v2_provenance == _F0_SENTINEL["v2_provenance"]
    )


def _is_f1_baseline_unavailable(env: EnvironmentFacts) -> bool:
    return (
        env.baseline_check in (BaselineCheck.MISSING, BaselineCheck.UNRESOLVABLE)
        and env.v1_scope == V1Scope.NOT_EVALUATED
        and env.v1_provenance == V1Provenance.NOT_APPLICABLE
        and env.blob_check == BlobCheck.NOT_EVALUATED
    )


def _is_f2_blob_failure(env: EnvironmentFacts) -> bool:
    # F-2's blocked set is {MISMATCH, TARGET_MISSING, COMMAND_FAILED,
    # INVALID_TICKET_VALUE} (ticket table, verbatim) — MISMATCH was already
    # correctly caught pre-Rev-C; the other three are R-1's root-fix targets.
    # Both MUST land here, or a real MISMATCH would wrongly fall through to
    # F-8/R-2 as an "unlisted combination" instead of its own named status.
    return (
        env.baseline_check == BaselineCheck.VALID
        and env.blob_check in _BLOB_FAILURE
        and env.v1_scope == V1Scope.NOT_EVALUATED
        and env.v1_provenance == V1Provenance.NOT_APPLICABLE
    )


def _resolve_final_status_and_reason(env: EnvironmentFacts) -> tuple[str, str]:
    # F-0: V0 early return with standardized NOT_EVALUATED sentinel.
    if env.v0_env != V0Env.PASS and _is_f0_sentinel(env):
        return FINAL_STATUS_BLOCKED_ENV_MISMATCH, "V0_ENV_MISMATCH"

    # F-1: baseline unresolvable/missing.
    if _is_f1_baseline_unavailable(env):
        return FINAL_STATUS_BLOCKED_UNATTRIBUTED_STATE, "BASELINE_UNAVAILABLE"

    # F-2: blob check hard failure (R-1, R3 root fix).
    if _is_f2_blob_failure(env):
        return FINAL_STATUS_BLOCKED_SCOPE_OR_TARGET_MISMATCH, "BLOB_CHECK_FAILURE"

    if env.baseline_check == BaselineCheck.VALID and env.v1_scope == V1Scope.CHANGED_TARGETS:
        # F-3: self-ticket attribution.
        if env.v1_provenance == V1Provenance.SELF_TICKET:
            return FINAL_STATUS_ALREADY_EXECUTED_PENDING_CLOSURE, "SELF_TICKET_ATTRIBUTION"
        # F-4: other-ticket attribution.
        if env.v1_provenance == V1Provenance.OTHER_TICKET:
            return FINAL_STATUS_CANDIDATE_SUPERSEDED, "OTHER_TICKET_ATTRIBUTION"
        # F-5: unattributed commits.
        if env.v1_provenance == V1Provenance.UNATTRIBUTED:
            return FINAL_STATUS_BLOCKED_UNATTRIBUTED_STATE, "UNATTRIBUTED_COMMITS"

    if (
        env.baseline_check == BaselineCheck.VALID
        and env.v1_scope == V1Scope.UNCHANGED_TARGETS
        and env.blob_check in _UNCHANGED_BLOB_OK
        and env.v2_provenance == V2Provenance.UNKNOWN
    ):
        # R-6: only v2_provenance=UNKNOWN has a producer when V2 actually ran;
        # KNOWN (schema-outside) is excluded here and falls through to F-8/R-2.
        # F-6: V2 satisfied but provenance unknown -> route_v2_state() equivalent.
        if env.v2_semantics == V2Semantics.SATISFIED:
            return FINAL_STATUS_CANDIDATE_SUPERSEDED, "V2_SATISFIED_UNKNOWN_PROVENANCE"
        # F-7: baseline matched, V2 incomplete. reason_code distinguishes the
        # two write_authority sub-cases (R-3) though final_status is shared.
        if env.v2_semantics == V2Semantics.MISSING:
            return FINAL_STATUS_BASELINE_MATCHED, "BASELINE_MATCHED_WRITE_AUTHORITY_ELIGIBLE"
        if env.v2_semantics == V2Semantics.PARTIAL:
            return FINAL_STATUS_BASELINE_MATCHED, REASON_CONSERVATIVE_MANUAL_NOT_MACHINE_READABLE
        # v2_semantics=FAILED (schema-outside) falls through to F-8/R-2.

    # F-8 / R-2: unlisted combination, fail-closed.
    return FINAL_STATUS_BLOCKED_ENV_MISMATCH, REASON_UNLISTED_COMBINATION


def _process_exit_code(env: EnvironmentFacts) -> int:
    # R-5: legacy exit code is a pure function of v0_env alone.
    return 1 if env.v0_env != V0Env.PASS else 0


def _write_authority(env: EnvironmentFacts) -> str:
    # R-3 (Model A): YES only on the fully machine-verifiable clean path.
    # Computed independently from final_status (never inferred from it),
    # matching the R-5 spirit of not reverse-deriving decisions cross-field.
    if (
        env.v0_env == V0Env.PASS
        and env.baseline_check == BaselineCheck.VALID
        and env.v1_scope == V1Scope.UNCHANGED_TARGETS
        and env.blob_check in _UNCHANGED_BLOB_OK
        and env.v2_semantics == V2Semantics.MISSING
        and env.v2_provenance == V2Provenance.UNKNOWN
    ):
        return "YES"
    return "NO"


def decide(facts: ActivationFacts) -> Decision:
    env = facts.environment
    ctx = facts.context

    final_status, reason_code = _resolve_final_status_and_reason(env)
    process_exit_code = _process_exit_code(env)
    write_authority = _write_authority(env)

    may_activate_ticket = final_status not in _BLOCKED_STATUSES
    may_run_tests_with_authority = env.v0_env == V0Env.PASS

    if final_status in _BLOCKED_STATUSES:
        evidence_required = "No writes, no test execution with ticket authority, no AC claim"
    elif final_status == FINAL_STATUS_CANDIDATE_SUPERSEDED:
        evidence_required = "Human review of superseding ticket before any further edit"
    elif final_status == FINAL_STATUS_ALREADY_EXECUTED_PENDING_CLOSURE:
        evidence_required = "Closure review by ticket owner, no re-execution"
    else:
        evidence_required = "None beyond standard AC evidence"

    return Decision(
        context_id=ctx.digest(),
        ticket_id=ctx.ticket_id,
        final_status=final_status,
        reason_code=reason_code,
        process_exit_code=process_exit_code,
        write_authority=write_authority,
        may_activate_ticket=may_activate_ticket,
        v2_probe_may_write=False,
        may_run_tests_with_authority=may_run_tests_with_authority,
        evidence_required=evidence_required,
    )


def _facts_from_json(payload: dict) -> ActivationFacts:
    ctx_d = payload["context"]
    env_d = payload["environment"]
    ctx = ContextFacts(
        ticket_id=ctx_d["ticket_id"],
        repo_root=ctx_d["repo_root"],
        worktree_path=ctx_d["worktree_path"],
        head_sha=ctx_d["head_sha"],
        config_revision=ctx_d["config_revision"],
        collected_at=ctx_d["collected_at"],
        activation_intent=ActivationIntent(ctx_d["activation_intent"]),
    )
    env = EnvironmentFacts(
        v0_env=V0Env(env_d["v0_env"]),
        baseline_check=BaselineCheck(env_d["baseline_check"]),
        v1_scope=V1Scope(env_d["v1_scope"]),
        v1_provenance=V1Provenance(env_d["v1_provenance"]),
        blob_check=BlobCheck(env_d["blob_check"]),
        v2_semantics=V2Semantics(env_d["v2_semantics"]),
        v2_provenance=V2Provenance(env_d["v2_provenance"]),
    )
    return ActivationFacts(ctx, env)


def _main() -> int:
    """Thin CLI shim for the Bash adapter (AI-4): reads one JSON facts object
    from stdin, writes one JSON Decision object to stdout. All I/O lives here,
    never inside decide() itself — this function is not covered by the core
    purity contract (HC-1 targets decide() and its helpers, not this shim).
    """
    try:
        payload = json.loads(sys.stdin.read())
        facts = _facts_from_json(payload)
    except (KeyError, ValueError) as exc:
        print(json.dumps({"error": f"invalid facts payload: {exc}"}))
        return 2

    decision = decide(facts)
    print(json.dumps(decision.as_dict()))
    return 0


if __name__ == "__main__":
    sys.exit(_main())
