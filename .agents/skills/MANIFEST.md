# Multi-Agent Skills Manifest

This manifest defines the governance contract for `.agents/skills`.
It enables shared skill reuse across runtimes without replacing the repo-level
startup contract or collapsing runtime-specific execution surfaces.

This file is the canonical registry for:

- skill classification
- ownership
- migration state
- common source path
- runtime adapter path
- review guard expectations
- telemetry identity fields

## 1. Layer Model

The repository preserves the following hierarchy:

```text
AGENTS.md          # repo-level startup contract / prompt authority
.agents/skills     # common source layer
.claude/skills     # Claude runtime adapter entrypoints
.codex/skills      # Codex runtime adapter entrypoints
```

Rules:

1. `AGENTS.md` remains the startup authority and deployment prerequisite.
2. `.agents/skills` is a common source layer, not the sole runtime entrypoint.
3. `.claude/skills` and `.codex/skills` remain active runtime surfaces.
4. Runtime-specific hooks, slash command glue, workflow wrappers, and runtime-local state writes stay in runtime adapter layers unless formally reclassified.

## 2. Classification Policy

### 2.1 common-with-adapters

Shared logic lives under `.agents/skills/<skill-name>/`.
Claude and/or Codex wrappers remain under runtime-specific directories.

### 2.2 claude-only

Skill remains fully under `.claude/skills/` because it is coupled to Claude runtime
semantics, hooks, slash flows, or local state conventions.

### 2.3 codex-only

Skill remains fully under `.codex/skills/` because it is coupled to Codex planning,
review, or `agents/openai.yaml`-driven orchestration.

## 3. Registry

| Skill | Classification | Owner | Review Owner | State | Migration Phase | Runtime Risk | Common Source | Claude Adapter | Codex Adapter | Notes |
|---|---|---|---|---|---|---|---|---|---|---|
| compact | common-with-adapters | Shared tooling | Shared tooling | planned | 2 | low | `.agents/skills/compact` | `.claude/skills/compact` | `.codex/skills/compact` | First low-risk extraction target. |
| status | common-with-adapters | Shared tooling | Claude runtime | planned | 2 | medium | `.agents/skills/status` | `.claude/skills/status` | `.codex/skills/status` | Must preserve Claude statusline behavior. |
| index-verify | common-with-adapters | Shared tooling | Shared tooling | planned | 3 | low | `.agents/skills/index-verify` | optional wrapper | optional wrapper | Parameterize repo paths before extraction. |
| mdlint | common-with-adapters | Shared tooling | Shared tooling | planned | 3 | low | `.agents/skills/mdlint` | optional wrapper | optional wrapper | Keep linter entrypoints stable. |
| cleanchat | common-with-adapters | Shared tooling | Shared tooling | planned | 3 | low | `.agents/skills/cleanchat` | optional wrapper | optional wrapper | Remove hidden cwd assumptions. |
| handoff | common-with-adapters | Shared tooling | Shared tooling | planned | 3 | medium | `.agents/skills/handoff` | optional wrapper | optional wrapper | Output contract must remain stable. |
| dtaudit | common-with-adapters | Shared tooling | Shared tooling | planned | 3 | medium | `.agents/skills/dtaudit` | optional wrapper | optional wrapper | Review repo path and output assumptions. |
| qa | claude-only | Claude runtime | Claude runtime | locked-runtime | 5 | high | none | `.claude/skills/qa` | none | Runtime-specific Claude execution flow. |
| sv | claude-only | Claude runtime | Claude runtime | locked-runtime | 5 | high | none | `.claude/skills/sv` | none | Must preserve `/sv` and `cross_review.sh`. |
| uc | claude-only | Claude runtime | Claude runtime | locked-runtime | 5 | medium | none | `.claude/skills/uc` | none | Runtime-specific Claude flow. |
| normal | claude-only | Claude runtime | Claude runtime | locked-runtime | 5 | medium | none | `.claude/skills/normal` | none | Runtime-specific Claude flow. |
| telegram-notify | claude-only | Claude runtime | Claude runtime | locked-runtime | 5 | medium | none | `.claude/skills/telegram-notify` | none | Runtime-specific integration. |
| trigger-workflow | codex-only | Codex runtime | Codex runtime | locked-runtime | 5 | high | none | none | `.codex/skills/trigger-workflow` | Runtime-specific workflow orchestration. |
| architecture-review | codex-only | Codex runtime | Codex runtime | locked-runtime | 5 | high | none | none | `.codex/skills/architecture-review` | Runtime-specific review pipeline. |
| ticket-breakdown | codex-only | Codex runtime | Codex runtime | locked-runtime | 5 | high | none | none | `.codex/skills/ticket-breakdown` | Runtime-specific planning pipeline. |
| execute | codex-only | Codex runtime | Codex runtime | locked-runtime | 5 | high | none | none | `.codex/skills/execute` | Runtime-specific execution pipeline. |
| strict-verification | codex-only | Codex runtime | Codex runtime | locked-runtime | 5 | high | none | none | `.codex/skills/strict-verification` | Runtime-specific guardrail flow. |
| conversation-lifecycle | codex-only | Codex runtime | Codex runtime | locked-runtime | 5 | medium | none | none | `.codex/skills/conversation-lifecycle` | Runtime-specific lifecycle logic. |

## 4. Common Layer Rules

The following rules apply to `.agents/skills/**` unless a file is explicitly
marked as adapter helper material or governance material:

1. Must not hard-code `.claude/skills`.
2. Must not hard-code `.codex/skills`.
3. Must not hard-code `.claude/PROGRESS.md`.
4. Must not assume Claude-only slash flows.
5. Must not assume Codex-only `agents/openai.yaml` presence.
6. Must receive runtime root, repo root, output root, or state file path from wrapper-provided context.
7. Must remain testable without runtime-specific directory layout.

## 5. Adapter Rules

Adapters may reference runtime-specific paths, but only inside runtime scope.

Allowed responsibilities:

- translate runtime invocation format;
- wire hooks and slash commands;
- locate runtime-local scripts;
- pass env vars or arguments to common scripts;
- preserve runtime-native UX such as statusline integration.

Not allowed:

- duplicating common business logic that belongs in `.agents/skills`;
- silently drifting from the shared contract;
- mutating common-layer semantics without updating this manifest.

## 6. Review Guard Requirements

When `.agents/skills` is active, review coverage must include it.

Required guard scope:

```yaml
globs:
  - "app/src/**/*.kt"
  - ".claude/skills/**"
  - ".agents/skills/**"
  - "tmp/**"
```

Static scan for common-layer forbidden runtime paths must run before or alongside
cross-review:

```bash
bash .agents/skills/scripts/scan-common-skills.sh
```

## 7. Telemetry Identity Fields

Pilot telemetry must distinguish the prompt contract, common source, and runtime
adapter.

Required fields:

- `prompt_contract_path`
- `skill_source_root`
- `runtime_adapter_root`
- `skill_name`
- `skill_classification`
- `adapter_runtime`

## 8. Migration Workflow

### Phase 1: Documentation only

- create this manifest;
- create `.agents/skills/` directory structure;
- create `.agents/skills/scripts/scan-common-skills.sh`;
- update `.claude/rules/cross-review.md`;
- do not move runtime scripts;
- do not create empty skill directories.

### Phase 2: Low-risk shared skills

- extract `compact` and `status` core logic;
- keep Claude and Codex wrappers intact;
- verify Claude statusline still resolves via runtime adapter.

### Phase 3: Maintenance skills

- migrate `index-verify`, `mdlint`, `cleanchat`, `handoff`, `dtaudit`;
- remove hard-coded runtime roots;
- validate wrapper compatibility.

### Phase 4: Guardrail enforcement

- keep `.claude/rules/cross-review.md` aligned with `.agents/skills/**`;
- run `scan-common-skills.sh`;
- add common-layer static scan to quick gate and review flow.

### Phase 5: Runtime-specific hold

- keep Claude-only and Codex-only skills locked until path coupling is explicitly removed and revalidated.

## 9. Acceptance Gates

A common skill migration is accepted only if all conditions hold:

- `AGENTS.md` remains the startup authority.
- `.agents/skills` remains common source, not sole runtime entrypoint.
- Claude runtime still resolves required runtime scripts through `.claude/skills`.
- Codex runtime still resolves required runtime files through `.codex/skills`.
- no forbidden runtime root is hard-coded in common-layer implementation files.
- cross-review covers `.agents/skills/**`.
- telemetry distinguishes prompt contract, common source, and runtime adapter.

## 10. Change Control

Any of the following requires manifest update in the same change set:

- adding a new skill;
- changing classification;
- changing owner or review owner;
- changing state or migration phase;
- changing common source or adapter path;
- moving a locked runtime skill into the common layer;
- introducing a new runtime-specific dependency.

