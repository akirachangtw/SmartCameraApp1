檔案治理／來源說明：本文件依據目前對話中已確認的 MEO 問題分析、既有 model-effort-orchestrator 設計草案、跨 Session 工作流需求，以及公開可得的 Claude Code hooks / append-only state / observability 設計脈絡整理而成，作為 MEO v2-lite 的規格初稿。本文為維持可讀性與後續加工穩定性，正文不顯示中括號型引用標記；若內容屬推論、建議值或需依實際 repo / runtime 調整之處，將直接在段落中標明。

# MEO v2-lite
## Cross-Session Orchestration Skill
Status: Draft v0.2(2026-08-01 CSO M1–M5 修訂落檔;v0.1 由外部設計輪產出)
Date: 2026-08-01 13:47 CST

## 1. Purpose

MEO v2-lite 的目標，是把原本會造成自指死結與跨 Session 汙染的執法式設計，改寫成一個：
- 可跨 Session 延續
- 可支援平行作業
- 不依賴 agent 自寫 mutable gate state
- 可觀測、可回放、可派生狀態

的 coordination layer。

它不再承擔 runtime hard-deny enforcement，而是專注在：
1. task diagnosis
2. execution routing
3. verification planning
4. cross-session handoff
5. event capture
6. derived state generation

## 2. Non-Goals

以下不再屬於 MEO v2-lite 的責任：
- 以單一 STATE.md 內計數器直接驅動 deny
- 以 pending / escalation mutable flag 當作寫入放行條件
- 以 agent 自寫狀態檔作為唯一治理真相
- 直接阻斷高風險動作的最終 enforcement

最終 enforcement 應由外部 policy gate、review gate、sync commit ritual、或獨立 orchestrator 處理。

## 3. Core Design Shift

### v1 問題

v1 問題本質：
- self-referential state
- mutable singleton state
- session-coupled logic
- counter-driven deny
- human/machine frequency mismatch

### v2-lite 原則

v2-lite 改為：
- append-only event log
- session-scoped write path
- derived state, not hand-maintained truth
- advisory escalation, not self-enforced deny
- external enforcement boundaries

## 4. Architecture Overview

系統拆為四層：

1. Skill Layer  
   負責 diagnosis、route recommendation、verification plan。

2. Event Layer  
   每個 session 只 append 自己的 event，不共寫同一 mutable state。

3. State Derivation Layer  
   根據 event log fold 出 ticket / task / session 的當前視圖。

4. Governance Layer  
   review gate、policy gate、sync commit、human adjudication 等外部治理機制。

## 5. File Layout

```text
meo/
  events/
    <ticket-id>/
      <session-id>.jsonl
  snapshots/
    <ticket-id>.state.json
    <ticket-id>.summary.md
  locks/
    <ticket-id>.lock.json
  schemas/
    meo-event.schema.json
    meo-state.schema.json
  reducers/
    fold-events.ts
  prompts/
    meo-v2-lite-prompt.md
  docs/
    meo-v2-lite-spec.md
```

## 6. Event Model

### 6.1 Event Properties

每個 event 必須具備：
- append-only
- immutable
- timestamped
- session-scoped
- ticket-scoped
- actor-attributed
- replayable

### 6.2 Event Types

**SSOT 裁定(M3)**:本節事件型別清單為事件字彙唯一 SSOT;reducer 與 JSON schema 為衍生表述,新增/消費任何事件型別 MUST 先改本節。

建議初始事件型別：
- session_started
- task_attached
- diagnosis_recorded
- action_plan_recorded
- execution_started
- file_touched
- verification_planned
- verification_result
- escalation_suggested
- escalation_resolved(M1:消解事件,依 causation_id 對消)
- handoff_written
- session_paused
- session_resumed
- session_closed
- sync_commit_recorded
- review_gate_result
- timeout_observed(M3 補齊:原 reducer 已消費而事件表漏列之型別)

### 6.3 Event Schema

```json
{
  "event_id": "uuid",
  "ticket_id": "TICKET-123",
  "session_id": "SESSION-20260801-001",
  "timestamp": "2026-08-01T13:47:00+08:00",
  "actor": {
    "type": "agent",
    "id": "claude-code"
  },
  "event_type": "diagnosis_recorded",
  "payload": {
    "task_type": "multi_step",
    "context_completeness": "sufficient",
    "failure_mode_guess": "execution_gap",
    "proposed_action": "run_and_verify",
    "risk_level": "medium",
    "verification_depth": "standard"
  },
  "causation_id": "optional-parent-event-id",
  "correlation_id": "optional-flow-id",
  "schema_version": "0.1"
}
```

## 7. Derived State

### 7.1 Principle

狀態不是直接寫出來，而是由 reducer 從 event log 折疊出來。
因此 state file 是 cache / snapshot / view，不是唯一真相來源。

### 7.2 Derived Fields

每張 ticket 可派生：
- latest_diagnosis
- latest_action_plan
- open_verification_items
- unresolved_escalations
- active_sessions(M2:TTL 判活制——依各 session 最後事件時齡判定,禁採 session_closed 成員制;崩潰/wedge session 不得殭屍化為永久 active)
- last_sync_commit
- last_review_result
- timeout_risk_flag
- session_conflict_flag

### 7.3 Snapshot Rules

- snapshot 可覆寫
- 原始 event 不可覆寫
- snapshot rebuild 必須 deterministic
- rebuild 失敗時，不可修改 event 檔

## 8. Session Isolation

### 8.1 Session-Scoped Writes

每個 session 只能寫：
- 自己的 `<session-id>.jsonl`
- 由外部流程授權的 handoff / artifact 檔

### 8.2 Shared Mutable Files

共享 mutable file 應極小化，只保留：
- optional lock file
- optional generated snapshot

### 8.3 Lock Model

若同一 ticket 不允許同時進行 destructive write，建議使用 ticket-level lock：

```json
{
  "ticket_id": "TICKET-123",
  "locked": true,
  "owner_session_id": "SESSION-20260801-001",
  "owner_actor": "claude-code",
  "locked_at": "2026-08-01T13:47:00+08:00",
  "intent": "write-phase"
}
```

注意：lock 只應保護 destructive shared write，不應阻斷觀察、診斷、handoff、或 event append。

## 9. Diagnosis Contract

MEO v2-lite 延續原 model-effort-orchestrator 的核心分類：

```yaml
task_type: simple | multi_step | ambiguous | high_risk
context_completeness: sufficient | partial | insufficient
failure_mode_guess: context_gap | execution_gap | capability_gap | unknown
proposed_action: ask | run | run_and_verify | escalate
risk_level: low | medium | high
verification_depth: none | basic | standard | strict
```

但這些診斷只作為：
- routing hint
- verification depth suggestion
- handoff context
- future review evidence

而不是直接當 deny flag。

## 10. Execution Modes

### 10.1 Observe Mode

只記錄 diagnosis、plan、verification 與 outcome，不對 runtime 動作施加阻斷。

### 10.2 Advisory Mode

可附加：
- escalation suggested
- high-risk warning
- review required
- sync commit required

但不直接阻斷寫入。

### 10.3 Governed Mode

真正的阻斷只由外部治理層處理：
- policy gate
- review gate
- sync commit discipline
- human adjudication

## 11. Cross-Session Handoff

### 11.1 Handoff Trigger

以下情況建議產生 handoff event / file：
- 任務跨天
- session 即將結束
- 遇到 blocked state
- 需要交接給另一 agent / human
- 需要等待 review / external input

### 11.2 Handoff Content

handoff 應至少包含：
- current task
- latest diagnosis
- files touched
- verification status
- open risks
- next recommended action
- unresolved escalation suggestions

## 12. Human Frequency Mismatch Handling

v2-lite 承認人類裁決頻率低於機器事件頻率，因此：
- escalation 使用 append-only suggestion event
- 不因未即時裁決而阻斷全部工作
- 可做 deferred batch adjudication
- 可在 snapshot 中標示 pending human review

## 13. Verification Policy

高風險或 multi-step 任務仍要求 verification plan：
- requirements_restate
- touched_files_list
- blast_radius_scan
- test_plan
- read_back_or_diff_check
- result_vs_acceptance_criteria
- escalation_trigger_review

差別在於：verification 是可觀測與可審計要求，而不是自指 deny 的唯一通行證。

## 14. Reducer Rules

Reducer 必須：
- deterministic
- idempotent
- append-safe
- tolerant to duplicate replay
- able to ignore unknown future event types
- (M4)時戳一律 UTC 正規化後排序,以 `(timestamp, event_id)` 雙鍵決序——防混時區字串排序失序與同時戳跨 session 不決定性
- (M5)尾行殘缺容忍:JSONL 末行 malformed(torn write)時跳過並記 warning,MUST NOT 修改 event 檔

建議 reducer 採：
- last-write-wins on timestamp for singleton view fields
- set union for touched_files
- queue/list for unresolved escalations;(M1)`escalation_resolved` 依 `causation_id` 對消移除,防 push-only 單調增長重演 pending 穩態
- boolean summary for high-risk pending state

## 15. Minimal MVP

若只做最小可用版，先做這四件事：
1. session-scoped JSONL event files
2. one reducer to build ticket snapshot
3. advisory diagnosis block
4. handoff generator

先不要做：
- 自動 policy deny
- 跨 ticket 全域計數器
- 複雜多層 escalator
- 自動消費式裁決釋放機制

## 16. Success Criteria

MEO v2-lite 成功的標準不是「完全阻止錯誤」，而是：
- 跨 Session 工作可延續
- 平行作業時不互相污染
- 人機頻率失配不再造成 pending 穩態死結
- state 可由 event replay 重建
- 治理成本低於治理收益

## 17. Migration Plan

### Phase 1
- 退役 v1 hard-deny 計數器
- 保留 diagnosis 邏輯
- 導入 session-scoped event log

### Phase 2
- 建立 reducer 與 snapshot view
- 將 STATE.md 降級為 derived summary

### Phase 3
- 將 review / sync commit / policy gate 外部化
- 僅保留 advisory escalation

## 18. Final Position

MEO 不應再扮演「自我執法者」，而應扮演「跨 Session 協調者」。
真正的工程重點不在於讓 agent 更會 deny 自己，而在於讓系統能：
- 保存脈絡
- 支援接力
- 降低污染
- 支援 replay
- 讓治理回到可驗證的外部邊界


---

## 19. 平台實態驗證義務(M5)

採用前 MUST 實測本平台 `write_file_safe mode=append` 對 `.jsonl` 之語義:①是否位元組級追加(非讀改寫——memory.jsonl 全檔覆寫模型曾致併寫資料遺失,為既證);②`.jsonl` 目的地既有 APPROVAL_REQUIRED 護欄之交互;③中斷寫入之殘行樣態。實測證據落 `meo/docs/` 前,event log 不得承載治理決策。

## 20. Open Items(S1–S3,採納後 MVP 期內修)

- **S1 lock 檔**:本設計唯一殘存之共享 mutable 單體——須加 stale-lock TTL+「destructive write」機械化枚舉(禁語義裁量)+明文「單人多 session、無原子 CAS」邊界假設(TOCTOU/TTL split-brain 佐證見同目錄 claim-URL-date.md)。
- **S2 snapshot 信任邊界條文**:任何裁決/驗證 MUST 引 `event_id`,snapshot 僅導航——同構 vault=SSOT/圖譜=指標既有紀律,防 STATE.md 式真相漂移重演。
- **S3 成功判準數值化**:§16 全質化判準須補數值閘(連續 N session 零跨污染事件、reducer 重建 property test 全過、escalation 佇列時齡 P50 閾值),並裁 v2-lite × Executor–Advisor Orchestrator MVP 之 scope 切分。

## Changelog

- **v0.2(2026-08-01,CSO)**:M1 `escalation_resolved` 消解事件(§6.2/§14)/M2 `active_sessions` TTL 判活(§7.2)/M3 事件字彙 SSOT 裁定+`timeout_observed` 補齊(§6.2)/M4 UTC+雙鍵決序(§14)/M5 torn-write 容忍(§14)+平台 append 實測義務(§19)。外部證據表=同目錄 `claim-URL-date.md`(官方 hooks 文檔證實 SessionStart 於 --resume 重入——v1 phantom-reset 屬可預期生命週期後果而非平台 bug)。
- **v0.1(2026-08-01)**:外部設計輪初稿。
