# model-effort-orchestrator prompt-template
derived-from: decision-tree.yaml v0.2
Do not edit rule logic here — change decision-tree.yaml first, then re-derive.

## Agent-Facing Rules(R1–R7,依 decision-tree.yaml 順序,先命中先出)

- **R1 context_first**:`context_completeness != sufficient` → 輸出 `ask`,禁止提高 effort 或送 escalation。
- **R2 fast_path**:`task_type=simple` 且 `risk_level=low` 且 `context_completeness=sufficient` → `run`(default model/effort),回應內嵌 `[MEO:fast-path|simple|low]`。
- **R3 mechanical_escalation**:`MEO_RETRY_COUNT`/`MEO_VERIFIER_REJECT_COUNT`/`MEO_SAME_ERROR_CLASS_STREAK` 任一 `>= 2` 且 `MEO_QUOTA_STATE != exhausted` → 輸出 `request_escalation`(附 diagnostics + execution_trace),裁決權在 orchestrator,不得自行升 model。
- **R4 quota_exhausted_defer**:同上機械信號達閾值但 `MEO_QUOTA_STATE=exhausted` → `defer_and_checkpoint`,禁止 request_escalation 或 upgrade_model。
- **R5 execution_gap**:`failure_mode_guess=execution_gap`(有漏讀/漏測/漏驗證證據) → `run_and_verify`,effort 提升為 `high`,model_tier 不變,需附 plan + verification_checklist。
- **R6 complexity**:`task_type in [multi_step, high_risk]` → `run_and_verify`,effort=`high`,`risk_level=high` 時 verification_depth=`strict` 且必須通過 `sv_codex_stage`。
- **R7 default_safe_fallback**:以上皆不命中(含 `unknown`)→ `run_and_verify`,`risk_level=high` 時 effort=`high`/verification_depth=`strict`,否則 effort=`default`/verification_depth=`basic`。

## 機械信號來源(唯一 action-gating 依據)

僅 `STATE.md` §MEO 區段之 KV 值可觸發 R3/R4;語義診斷(`failure_mode_guess` 等)為 hypothesis,單獨不得觸發升級動作。
