# ENVSYNC_AUDIT_20260826 — `.claude/` 兩端全清單 hash 盤點（AUDIT-01 重跑，取代 20260821 版）

**票**: INFRA-ENVSYNC-AUDIT-01（CLOSED）重跑，Akira 2026-08-26 對話直接授權（原因：INFRA-ENVSYNC-BATCH-01 AI-0 錨點復驗發現基線漂移，20260821 報告失效）
**盤點時間**: 2026-08-26
**PLT**: `D:/DevSources/workflow/mcp-ticket-platform/.claude/`
**APP**: `D:/DevSources/AndroidProj/SmartCameraApp1/.claude/`（本 repo）
**排除範圍**: `__pycache__/`、`worktrees/`、`*.bak`、`PROGRESS*.md`（各端活體，恆不同步）

本次為唯讀重盤點，**未執行任何同步動作**，不觸碰 `.claude/` 下任何檔案。取代 `ENVSYNC_AUDIT_20260821.md` 作為 `INFRA-ENVSYNC-BATCH-01` 派工用選檔依據；舊報告保留不刪（歷史記錄）。

## 基線漂移比對（vs 20260821 報告）

- PLT 側檔案總數：58 → **60**（新增 2 檔：`scripts/ssot_skill_drift_scan.sh`、`skills/ticket-rigor-gate/runner/housekeeping.sh`，均為 PLT_ONLY，不影響本票 17 檔 ALLOWED 集合）
- APP 側檔案總數：48（無變化）
- 聯集：**69**（原 67）

**已確認漂移之 2 檔（BATCH-01 AI-0 復驗觸發點）**：
- `rules/ssot-governance.md`：PLT hash `8960acb1`→`f5be6c2f`（PLT 端 2026-08-22 `d0014ea` 後續 commit 已改動，晚於原審查基線 `b64c381 2026-08-19`）
- `skills/qa/scripts/verify_checklist.sh`：APP hash `defce99e`→`bb665859`（APP 端 `fbcb612 2026-08-24` `[INFRA-MAXLINES-EVIDENCE-01]` 追加 evidence record 邏輯；FORBIDDEN 檔，不影響本票 ALLOWED）

**額外偵測之連動漂移**（原表未見，本次新增）：
- `CLAUDE.md`（禁搬）：PLT `30bb9bc7`→`c8bcd82e`
- `rules/concurrency.md`：PLT `13ba97e2`→`21e0f974`（PLT `02f1ef7 2026-08-25`）
- `rules/ticket_pattern.md`：PLT `4640b730`→`2ad4a819`（PLT `02f1ef7 2026-08-25`）
- `skills/compact/SKILL.md`：PLT `7b2fc376`→`5baad9d6`（PLT `d0014ea 2026-08-22`）
- `skills/qa/SKILL.md`：APP `3caa6a54`→`b736ef39`（APP `fbcb612 2026-08-24`，與 verify_checklist.sh 同源 commit）
- `skills/sv/scripts/cross_review.sh`：APP `3e8f09a3`→`c45376c7`（APP `390f50d 2026-08-26` `INFRA-CROSSREVIEW-GOVPATH-EXCL-01` 修復；FORBIDDEN 檔）
- `skills/uc/SKILL.md`：PLT `8690193b`→`decdca1d`

## 全量四欄表（69 列＝聯集數）

| 相對路徑 | PLT hash | APP hash | 判定 |
|---|---|---|---|
| `CLAUDE.md` | `c8bcd82e` | `df14b55b` | DIVERGED（禁搬） |
| `agents/cross-reviewer.md` | `8efcac68` | `da546d1a` | DIVERGED（候選同步） |
| `hooks/mcp_service_pid_guard.py` | `090d50ed` | `—` | PLT_ONLY（禁搬） |
| `hooks/snapshot_cueshot_memory.py` | `—` | `96ae34ad` | APP_ONLY（禁搬） |
| `hooks/stop.sh` | `af45101b` | `cb3d0fef` | DIVERGED（候選同步） |
| `hooks/telegram_ask.sh` | `b38e4a70` | `b38e4a70` | IDENTICAL |
| `hooks/telegram_stop.sh` | `775109c9` | `775109c9` | IDENTICAL |
| `hooks/tests/telegram_stop_test.sh` | `1bfc38d7` | `1bfc38d7` | IDENTICAL |
| `rules/android-conventions.md` | `—` | `da7fffca` | APP_ONLY（禁搬） |
| `rules/camera-constraints.md` | `—` | `9aa74c37` | APP_ONLY（禁搬） |
| `rules/compose-ui.md` | `—` | `fc3f8115` | APP_ONLY（禁搬） |
| `rules/concurrency.md` | `21e0f974` | `c5dab635` | DIVERGED（候選同步） |
| `rules/critical-files.md` | `—` | `02e56668` | APP_ONLY（禁搬） |
| `rules/cross-review.md` | `b0a21ac0` | `cb0f870c` | DIVERGED（候選同步） |
| `rules/decommission.md` | `7ecf63cf` | `fd7cfd2a` | DIVERGED（候選同步） |
| `rules/kairos-lessons.md` | `0dc9c0fe` | `d55f394b` | DIVERGED（候選同步） |
| `rules/mcp-service-safety.md` | `1af7a1cb` | `—` | PLT_ONLY（禁搬） |
| `rules/phase0-lessons.md` | `9fef66a7` | `5a9fe01b` | DIVERGED（候選同步） |
| `rules/ptf-rules.md` | `—` | `72090c12` | APP_ONLY（禁搬） |
| `rules/ssot-governance.md` | `f5be6c2f` | `0262ac77` | DIVERGED（候選同步）⚠️基線已漂移 |
| `rules/state-machine.md` | `—` | `c306c973` | APP_ONLY（禁搬） |
| `rules/ticket_pattern.md` | `2ad4a819` | `067600b3` | DIVERGED（候選同步） |
| `scripts/m3_family_audit.sh` | `e7aba503` | `e7aba503` | IDENTICAL |
| `scripts/ssot_skill_drift_scan.sh` | `c50614fa` | `—` | PLT_ONLY（新增，禁搬） |
| `settings.json` | `eb52255b` | `ba327195` | DIVERGED（禁搬） |
| `settings.local.json` | `921b740a` | `d48b50b1` | DIVERGED（候選同步） |
| `skills/cleanchat/SKILL.md` | `2d30036f` | `2d30036f` | IDENTICAL |
| `skills/cleanchat/scripts/md-formatting.sh` | `04399578` | `04399578` | IDENTICAL |
| `skills/compact/SKILL.md` | `5baad9d6` | `0ed0676a` | DIVERGED（候選同步） |
| `skills/dream/SKILL.md` | `6fda83ae` | `6fda83ae` | IDENTICAL |
| `skills/dtaudit/SKILL.md` | `—` | `d1de4aab` | APP_ONLY（禁搬） |
| `skills/dtaudit/references/cueshot-example.md` | `—` | `b419e297` | APP_ONLY（禁搬） |
| `skills/index-verify/SKILL.md` | `7559c891` | `7559c891` | IDENTICAL |
| `skills/index-verify/scripts/index_path_verify.sh` | `d05bf27a` | `d05bf27a` | IDENTICAL |
| `skills/mdlint/SKILL.md` | `28521913` | `68145ca0` | DIVERGED（候選同步） |
| `skills/normal/SKILL.md` | `1c3bca26` | `1c3bca26` | IDENTICAL |
| `skills/pythonw-design/SKILL.md` | `fd93fcb9` | `—` | PLT_ONLY（禁搬） |
| `skills/pythonw-design/references/desktop-ui-principles.md` | `21ab7f36` | `—` | PLT_ONLY（禁搬） |
| `skills/pythonw-design/references/review-checklist.md` | `1a2bc152` | `—` | PLT_ONLY（禁搬） |
| `skills/pythonw-design/templates/review-template.md` | `a4f1ae27` | `—` | PLT_ONLY（禁搬） |
| `skills/qa/SKILL.md` | `20b09149` | `b736ef39` | DIVERGED（候選同步） |
| `skills/qa/scripts/lib_verify.sh` | `f2cc21f5` | `f2cc21f5` | IDENTICAL |
| `skills/qa/scripts/static_blind_scan.sh` | `7b2be731` | `6bc04739` | DIVERGED（候選同步） |
| `skills/qa/scripts/structural_verify.sh` | `d017c679` | `d017c679` | IDENTICAL |
| `skills/qa/scripts/verify_checklist.sh` | `a03b2957` | `bb665859` | DIVERGED（反向票，禁搬）⚠️基線已漂移 |
| `skills/status/SKILL.md` | `9420248d` | `9420248d` | IDENTICAL |
| `skills/status/scripts/statusline.py` | `b3e26bf9` | `b3e26bf9` | IDENTICAL |
| `skills/sv/CROSS_REVIEW.md` | `b19e1baf` | `11d30051` | DIVERGED（候選同步） |
| `skills/sv/SKILL.md` | `6ae86be2` | `c729153f` | DIVERGED（候選同步） |
| `skills/sv/scripts/cross_review.sh` | `82ac43cf` | `c45376c7` | DIVERGED（凍結待 CROSSREVIEW-GOVPATH-EXCL-01，禁搬） |
| `skills/sv/ssot_context.md` | `e996b0c1` | `6092b217` | DIVERGED（候選同步） |
| `skills/telegram-notify/SKILL.md` | `82627956` | `82627956` | IDENTICAL |
| `skills/telegram-notify/scripts/notify.sh` | `8c9086cd` | `8c9086cd` | IDENTICAL |
| `skills/ticket-rigor-gate/EXAMPLE.md` | `e3db3bcc` | `—` | PLT_ONLY（禁搬） |
| `skills/ticket-rigor-gate/OPS_CALIBRATION.md` | `3db630a9` | `—` | PLT_ONLY（禁搬） |
| `skills/ticket-rigor-gate/REPAIR_ROUTING.md` | `1e3c250b` | `—` | PLT_ONLY（禁搬） |
| `skills/ticket-rigor-gate/SKILL.md` | `e73151d0` | `—` | PLT_ONLY（禁搬） |
| `skills/ticket-rigor-gate/runner/README.md` | `29cf2830` | `—` | PLT_ONLY（禁搬） |
| `skills/ticket-rigor-gate/runner/housekeeping.sh` | `62fca7e3` | `—` | PLT_ONLY（新增，禁搬） |
| `skills/ticket-rigor-gate/runner/profiles/model-profiles.v1.json` | `d5ca7b1b` | `—` | PLT_ONLY（禁搬） |
| `skills/ticket-rigor-gate/runner/prompts/ticket-repair-prompt.v1.txt` | `2506f884` | `—` | PLT_ONLY（禁搬） |
| `skills/ticket-rigor-gate/runner/repair_runner.sh` | `58f0e9a7` | `—` | PLT_ONLY（禁搬） |
| `skills/ticket-rigor-gate/runner/schemas/ticket-repair-payload.v1.json` | `1abf5e35` | `—` | PLT_ONLY（禁搬） |
| `skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json` | `ddaa0a48` | `—` | PLT_ONLY（禁搬） |
| `skills/uc/SKILL.md` | `decdca1d` | `049654d2` | DIVERGED（候選同步） |
| `skills/wktree-close/SKILL.md` | `b628643d` | `—` | PLT_ONLY（禁搬） |
| `skills/wktree-close/scripts/wktree-close.sh` | `2d1974db` | `—` | PLT_ONLY（禁搬） |
| `skills/wktree-compact/SKILL.md` | `7a3eb580` | `—` | PLT_ONLY（禁搬） |
| `verify-policy.env` | `d87079ed` | `d87079ed` | IDENTICAL |

## 分類統計

| 分類 | 數量 |
|---|---|
| IDENTICAL | 17 |
| DIVERGED | 22 |
| PLT_ONLY | 21 |
| APP_ONLY | 9 |
| **合計** | **69** |

分類分布與 20260821 報告相同形狀（IDENTICAL/DIVERGED/PLT_ONLY/APP_ONLY 四類邊界未變，PLT_ONLY 因新增 2 檔由 19→21，含 SYNC01 兩檔已於 20260821 完成故本次不再單列 SYNC01_承接中 類，兩檔併入 IDENTICAL）。

## INFRA-ENVSYNC-BATCH-01 選檔集重新核對（17 檔 ALLOWED）

本票 ALLOWED 17 檔對照本次 DIVERGED 22 項（扣除 2 項凍結/反向票 `verify_checklist.sh`／`cross_review.sh`，及 `CLAUDE.md`／`settings.json`／`settings.local.json` 三項禁搬或另案）與原 20260821 報告完全一致，**選檔集合未變**，僅 hash 值更新：

`agents/cross-reviewer.md`、`hooks/stop.sh`、`rules/concurrency.md`、`rules/cross-review.md`、`rules/decommission.md`、`rules/kairos-lessons.md`、`rules/phase0-lessons.md`、`rules/ssot-governance.md`、`rules/ticket_pattern.md`、`skills/compact/SKILL.md`、`skills/mdlint/SKILL.md`、`skills/qa/SKILL.md`、`skills/qa/scripts/static_blind_scan.sh`、`skills/sv/CROSS_REVIEW.md`、`skills/sv/SKILL.md`、`skills/sv/ssot_context.md`、`skills/uc/SKILL.md`

**結論**：BATCH-01 可依本報告新錨點重新派工，17 檔選檔依據不變。

## 抽樣複驗（本報告產出當下自我複驗）

| 檔案 | 首跑值 | 複驗值 | 一致 |
|---|---|---|---|
| `hooks/telegram_ask.sh`（IDENTICAL） | PLT `b38e4a70` / APP `b38e4a70` | PLT `b38e4a70` / APP `b38e4a70` | ✅ |
| `rules/ssot-governance.md`（DIVERGED，本次漂移觸發點） | PLT `f5be6c2f` / APP `0262ac77` | PLT `f5be6c2f` / APP `0262ac77` | ✅ |
| `skills/qa/scripts/verify_checklist.sh`（DIVERGED，反向票） | PLT `a03b2957` / APP `bb665859` | PLT `a03b2957` / APP `bb665859` | ✅ |

## G-3：APP 根目錄 PROGRESS.md 指涉查核

`test -f PROGRESS.md`（repo 根）→ **不存在**（與 20260821 報告一致）。
