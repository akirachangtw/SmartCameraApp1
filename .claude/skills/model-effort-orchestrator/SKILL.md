# model-effort-orchestrator SKILL.md
Date: 2026-07-18
Status: Draft v0.2(依 CSO 審查修訂)
Supersedes: v0.1(2026-07-16)
Deploy Path: `D:\DevSources\AndroidProj\SmartCameraApp1\skills\model-effort-orchestrator\`
Governance Ref: vault `wiki\decisions\CSO\2026-07-20_MEO_Skill_Governance_Ratification.md`(D1–D5 已批)

> **文件治理**:本文件為 skill 行為規格。routing / escalation 邏輯之唯一 SSOT 為同目錄 `decision-tree.yaml`;本文件 §6、§12、§13 為其**衍生表述**,任何邏輯變更必須先改 `decision-tree.yaml`,再同步衍生物。禁止平行維護。

---

## 1. Purpose

在 agent 執行任務前後,明確區分三類問題來源:

1. context 不足(context_gap)
2. execution effort 不足(execution_gap)
3. model capability 不足(capability_gap)

核心原則:**先修 context,再調 effort,最後升 model**。並加入第四維:**quota 狀態**——升級路徑必須 quota-aware,否則本 skill 會成為配額耗盡加速器。

## 2. Design Intent(v0.2 修訂重點)

| # | v0.1 缺口 | v0.2 對策 |
|---|-----------|----------|
| 1 | 裁判悖論:agent 自評 capability_gap 不可信 | 語義診斷降為 hypothesis;**機械信號**才觸發 action(§5.2) |
| 2 | 無 quota 意識 | 新增 `quota_state` 輸入與 `defer_and_checkpoint` 分支(§6) |
| 3 | 軟約束可被 agent 繞過 | PreToolUse hook 機械檢查 + escalation 裁決權收歸 orchestrator(§11) |
| 4 | 三份邏輯表述漂移 | `decision-tree.yaml` 為唯一 SSOT |
| 5 | simple task 診斷 overhead 自相矛盾 | fast-path 分層診斷(§5.3) |
| 6 | 無可證偽驗證設計 | H1/H2 假設與閾值(§15,D2/D3 已批) |

## 3. Skill Metadata

```yaml
name: model-effort-orchestrator
description: >
  Diagnose whether failure comes from missing context, insufficient execution
  effort, or insufficient model capability, then choose the cheapest effective
  escalation path under quota constraints. Semantic diagnosis produces
  hypotheses only; mechanical signals gate all escalation actions.
version: 0.2
owner: Akira Chang
ssot: decision-tree.yaml
scope: [coding-agent, review-agent, planning-agent, router-agent]
inputs:
  - task_description
  - available_files
  - available_tools
  - constraints
  - success_criteria
  - quota_state          # v0.2 新增
  - mechanical_signals   # v0.2 新增,來源 STATE.md
outputs:
  - diagnosis            # hypothesis 層
  - action_plan
  - effort_recommendation
  - verification_plan
  - escalation_request   # v0.2:agent 僅能「請求」,不能自行切 model
policies:
  context_first: true
  prefer_default_effort: true
  fast_path_simple_tasks: true
  escalation_authority: external (Akira/CSO checkpoint via meo/adjudications/ record + audit sidecar, produced outside agent session)
  quota_aware_escalation: true
  optimize_total_cost_not_unit_price: true
```

## 4. Operating Model

### 4.1 Mental Model

- model = 能力上限控制面
- effort = 執行深度控制面
- context = 前置燃料
- **quota = 生存約束面**

### 4.2 Priority Order

1. 先補 context
2. 再調 effort
3. 最後升 model
4. **升 model 前必過 quota gate**——`quota_state=exhausted` 時改走 `defer_and_checkpoint`

## 5. Diagnostic Contract

### 5.1 診斷物件(hypothesis 層)

```yaml
task_type: simple | multi_step | ambiguous | high_risk
context_completeness: sufficient | partial | insufficient
failure_mode_guess: context_gap | execution_gap | capability_gap | unknown
proposed_action: ask | run | run_and_verify | request_escalation | defer_and_checkpoint
risk_level: low | medium | high
verification_depth: none | basic | standard | strict
quota_state: abundant | constrained | exhausted
```

> ⚠️ **語義診斷僅為 hypothesis**。`failure_mode_guess=capability_gap` 本身**不觸發**任何升級動作;升級唯一入口為 §5.2 機械信號 + orchestrator 裁決。此設計消解「診斷者=被診斷者」悖論。

### 5.2 機械信號(action 層,SSOT 定義於 decision-tree.yaml)

信號持久化於 `STATE.md` §MEO 區段,扁平 KV 格式,grep/jq 雙相容:

```text
MEO_RETRY_COUNT=0
MEO_SAME_ERROR_CLASS_STREAK=0
MEO_VERIFIER_REJECT_COUNT=0
MEO_LAST_ERROR_CLASS=none
MEO_QUOTA_STATE=abundant
MEO_ESCALATION_PENDING=false
```

| 信號 | 來源 | 閾值(D1 已批) |
|------|------|---------|
| `MEO_RETRY_COUNT` | execution trace(PostToolUse hook 累加) | ≥2 → 強制 escalation review |
| `MEO_VERIFIER_REJECT_COUNT` | `/sv` Codex stage 駁回 | reserved:僅 watermark 記錄,非觸發器(SSOT-hook 對齊,Rev F AI-5.6) |
| `MEO_SAME_ERROR_CLASS_STREAK` | test runner / error class 比對 | ≥2 → capability_gap 確認 |

**閾值遵守「監控閾值不得語義化」原則**:數值直取,禁以「議題複雜度」「內容密度」等語義理由提前或延後觸發。

### 5.3 分層診斷(fast-path)

- **fast-path**:`task_type=simple && risk_level=low && context_completeness=sufficient` → 免產出完整診斷物件,僅在回應內嵌單行標記:
  `[MEO:fast-path|simple|low]`
- **full diagnosis**:`multi_step | ambiguous | high_risk` 任一成立 → 完整診斷物件,寫入 STATE.md `last_diagnosis`。

## 6. Decision Tree

> **本節為衍生表述,僅供人類閱讀。機器裁決一律以 `decision-tree.yaml` 為準。**

裁決順序(先命中先出,short-circuit):

1. `context_completeness != sufficient` → **ask**(不動 effort、不動 model)
2. fast-path 條件成立 → **run**(default model / default effort)
3. 機械信號任一達閾值 且 `quota_state != exhausted` → **request_escalation**(送 orchestrator 裁決)
4. 機械信號任一達閾值 且 `quota_state == exhausted` → **defer_and_checkpoint**(存 trace,排入下一 quota window)
5. `failure_mode_guess == execution_gap`(有漏讀/漏測/漏驗證證據) → **run_and_verify** + raise effort
6. `task_type in [multi_step, high_risk]` → **run_and_verify** + require_plan
7. fallback(含 `unknown`)→ **run_and_verify**(default-safe;high_risk 時 verification_depth=strict)

## 7. Failure Taxonomy

### 7.1 Context Gap
特徵:目標模糊、成功標準未定義、缺關鍵檔案/規格/工具、輸入互相矛盾。
對策:先 ask,不提高 effort;補 task contract / input manifest / acceptance criteria。

### 7.2 Execution Gap
特徵:漏讀檔案、無 blast radius 檢查、沒跑測試、無 read-back 即回報完成。
對策:提升 effort;強制 plan + verification checklist;高風險寫檔加 read-back 與 artifact checkpoint。

### 7.3 Capability Gap(v0.2 修訂)
**確認條件從語義自評改為機械信號**:`MEO_SAME_ERROR_CLASS_STREAK >= 2` 或(`MEO_RETRY_COUNT >= 2` 且 orchestrator review 判定 non-execution 因素)。
對策:agent 輸出 `escalation_request`(含 diagnostics + execution trace);**orchestrator 裁決**是否升 tier;`quota_state=exhausted` → `defer_and_checkpoint`,禁止硬升。

## 8. Execution Policy

### 8.1 Task Classification

- simple: 單檔、單步、可快速驗證
- multi_step: 跨檔、多階段、需規劃或多工具串接
- ambiguous: 需求未清、輸入不足、驗收條件模糊
- high_risk: 改核心架構、批次改檔、影響部署或資料安全

### 8.2 Effort Adapter(僅先落地兩欄,可測試優先)

| effort 級 | Claude Code 映射 | Codex 映射 | 其他平台 |
|-----------|-----------------|-----------|---------|
| none/low | 不觸發 plan mode;讀檔限指定路徑 | 單輪、無 self-review | 留空待補 |
| default | 標準流程 | 標準流程 | 留空待補 |
| high | 強制 plan 先行;blast radius 讀檔;測試必跑 | self-review 一輪 | 留空待補 |
| max/strict | plan + verification checklist + read-back 全開;`/sv` gate 必過 | cross-validation 模式 | 留空待補 |

> 映射具體化後才可寫 acceptance test(T4)。「其他平台」欄位留空為刻意設計,禁止無 adapter 驗證即填入。

### 8.3 Model Tier

tier 對應由 model registry 管理,skill 內不硬編碼。**escalation_history 每筆必記 `model_version`**,registry 更新後舊 capability_gap 判定自動過期(§14)。

## 9. Verification Policy

- checklist 見 decision-tree.yaml `verification_checklist`
- review gate 之 explicit pass/fail → 走既有 `stop_orchestrator.sh` + `gate.conf`
- 檔案修改 diff / read-back → 使用既有 `diff_hash.sh`(sha256 normalization)
- Verifier 獨立性:**強制掛 `/sv` Codex cross-validation stage**,禁止 Worker 同模型同 session 自驗充當 Verifier

## 10. Stop Conditions

見 decision-tree.yaml `stop_conditions`(含 v0.2 新增之 quota 阻斷條款)。任一命中 → 停止並輸出:目前狀態 / 阻塞原因 / 建議路徑。

## 11. Integration Pattern

### 11.1 Enforcement:PreToolUse Hook

`hooks/meo_pretooluse_gate.sh`(完整腳本見部署票 payload `MEO_Deployment_Tickets_v1.0.md`),契約:stdout 僅 `{decision, reason}`、exit 0、exec form 註冊、jq 缺失 fail-open。

### 11.2 Escalation 裁決權

- Agent → 輸出 `escalation_request`(寫 STATE.md `MEO_ESCALATION_PENDING=true` + trace 附件)
- **stop_orchestrator.sh 讀取 gate.conf `[meo]` 條目**(D5 已批:不新增第四個 Stop hook,遵守 G2)
- 裁決三態:`approve_upgrade | deny_retry_with_high_effort | defer_and_checkpoint`

### 11.3 STATE.md 欄位

```text
## MEO (model-effort-orchestrator)
MEO_LAST_DIAGNOSIS_TS=
MEO_LAST_TASK_TYPE=
MEO_LAST_FAILURE_MODE=
MEO_RETRY_COUNT=0
MEO_SAME_ERROR_CLASS_STREAK=0
MEO_VERIFIER_REJECT_COUNT=0
MEO_LAST_ERROR_CLASS=none
MEO_QUOTA_STATE=abundant
MEO_ESCALATION_PENDING=false
MEO_FASTPATH=false
# Rev F 新增(episode identity + watermark + record-before-clear 消費點):
MEO_ESCALATION_ID=0
MEO_WM_RETRY=0
MEO_WM_STREAK=0
MEO_WM_ERROR_CLASS=none
MEO_WM_REJECT=0
MEO_LAST_ESCALATION_DECISION=
```

### 11.4 MCP / Long Task Guardrails

**本節不重述規則。** 逾時分流、read-back 驗證、artifact checkpoint 一律以下列 SSOT 為準:
→ `search_decisions` 命中 `MCP_Search_Pipeline_Timeout_Hardening`(2026-07-17)。

## 12–13. Pseudocode / Prompt Template

> 衍生表述,由 decision-tree.yaml 生成。prompt-template.md 獨立成檔(T1 AI-4),標頭必註 `derived-from: decision-tree.yaml v0.2`。

## 14. Escalation History(D4 已批)

```yaml
- ts: <datetime>
  task_ref: <ticket-id>
  trigger_signal: retry_count | verifier_reject | same_error_class
  decision: approve_upgrade | deny_retry_with_high_effort | defer_and_checkpoint
  model_version_at_decision: <registry snapshot>
  invalidation: "model registry tier 更新或 model_version 變更即失效"
  ttl_days: 30
```

## 15. Validation Design(D2/D3 已批)

| 假設 | 陳述 | 閾值 |
|------|------|------|
| H1 分類一致性 | vault 歷史失敗案例 10–15 例 retrospective classification,skill 判定與事後人工判定一致率 | ≥ 80% |
| H2 成本上限 | 導入後每週升 tier 次數 | ≤ 導入前基線 × 1.0 |

**H1 未通過前不得開始 adapter 落地(T4 硬性 gate)。**

---
**End of SKILL.md v0.2**
