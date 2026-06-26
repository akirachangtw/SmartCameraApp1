# Phase 1 Multi-Agent Skills Governance Decision

Date: 2026-06-26
Status: Approved for governance-only implementation
Source Artifacts:

- `tmp/multiagent-skills-plan.md`
- `tmp/Headroom/Phase1-governance.md`
- `tmp/Headroom/agents-skills-manifest-full.md`
- `tmp/Headroom/cross-review-md-patch-draft.md`
- `tmp/Headroom/scan-common-skills.sh`

## 1. Decision Summary

This decision resolves the remaining ambiguity for Phase 1 of the multi-agent
skills consolidation work.

Approved direction:

- Keep `AGENTS.md` as the repo-level startup contract and deployment prerequisite.
- Introduce `.agents/skills` only as the shared common source layer.
- Preserve `.claude/skills` and `.codex/skills` as active runtime adapter entrypoints.
- Limit Phase 1 to governance-only implementation.
- Do not move runtime scripts.
- Do not create empty common skill directories.
- Do not update `.claude/PROGRESS.md` as part of this governance change unless explicitly requested later.

## 2. Resolved Ambiguities

| Question | Decision |
|---|---|
| Are `tmp/Headroom/*.md` files formal repo documents? | No. They are draft sources / design inputs. |
| Which draft should become the formal common skill manifest? | `tmp/Headroom/agents-skills-manifest-full.md` should be normalized into `.agents/skills/MANIFEST.md`. |
| Where should the common-layer scan script live? | `.agents/skills/scripts/scan-common-skills.sh`. |
| Should `.claude/rules/cross-review.md` be updated now? | Yes. Add `.agents/skills/**` to the review globs. |
| Should empty `.agents/skills/<skill-name>/` directories be created now? | No. Empty skill directories may imply usable skills and create registry noise. |
| Should `.claude/PROGRESS.md` be updated now? | No. This Phase 1 governance decision does not require progress dashboard mutation. |
| Should `Phase1-governance.md` itself become the formal document? | No. It should be treated as source input; this file is the normalized formal decision record. |

## 3. Approved Phase 1 Scope

Phase 1 may implement only the following repo changes:

1. Create `.agents/skills/MANIFEST.md` from the approved manifest draft.
2. Create `.agents/skills/scripts/scan-common-skills.sh` from the approved scan script draft.
3. Update `.claude/rules/cross-review.md` so `globs` includes `.agents/skills/**`.
4. Keep `.claude/skills/**` and `.codex/skills/**` behavior unchanged.

Anything outside this list is out of scope for Phase 1.

## 4. Explicit Non-Goals

Phase 1 must not:

- move `qa`, `sv`, `uc`, `normal`, or `telegram-notify` out of `.claude/skills`;
- move `trigger-workflow`, `architecture-review`, `ticket-breakdown`, `execute`,
  `strict-verification`, or `conversation-lifecycle` out of `.codex/skills`;
- create placeholder common skills without a real `SKILL.md` contract;
- change Claude statusline wiring;
- change Codex `agents/openai.yaml` behavior;
- modify `.claude/PROGRESS.md`;
- treat `.agents/skills` as the sole runtime entrypoint.

## 5. Guardrail Requirements

The Phase 1 implementation must preserve the following hierarchy:

```text
AGENTS.md          # repo-level startup contract / prompt authority
.agents/skills     # shared common source layer
.claude/skills     # Claude runtime adapter entrypoints
.codex/skills      # Codex runtime adapter entrypoints
```

The cross-review rule must include:

```yaml
globs:
  - "app/src/**/*.kt"
  - ".claude/skills/**"
  - ".agents/skills/**"
  - "tmp/**"
```

The scan script path must be:

```bash
bash .agents/skills/scripts/scan-common-skills.sh
```

Do not use the earlier draft path `bash scripts/scan-common-skills.sh`, because
this repo currently has no top-level `scripts/` governance convention.

## 6. Acceptance Criteria

Phase 1 is accepted only when all of the following are true:

- `.agents/skills/MANIFEST.md` exists and records skill classification, ownership,
  migration state, common source path, adapter path, review guard expectations,
  and telemetry identity fields.
- `.agents/skills/scripts/scan-common-skills.sh` exists.
- `.claude/rules/cross-review.md` includes `.agents/skills/**`.
- No empty `.agents/skills/<skill-name>/` placeholder directories are created.
- Claude runtime paths remain valid.
- Codex runtime paths remain valid.
- `.claude/PROGRESS.md` is unchanged by this Phase 1 change.

## 7. Follow-Up Sequence

After Phase 1 is complete and verified, later phases may proceed in this order:

1. Extract `compact` shared behavior.
2. Extract `status` shared behavior while preserving Claude statusline wiring.
3. Migrate maintenance skills after path parameterization:
   `index-verify`, `mdlint`, `cleanchat`, `handoff`, `dtaudit`.
4. Add pilot telemetry fields:
   `prompt_contract_path`, `skill_source_root`, `runtime_adapter_root`,
   `skill_name`, `skill_classification`, `adapter_runtime`.

Runtime-specific skills remain locked until a separate decision authorizes
reclassification.

