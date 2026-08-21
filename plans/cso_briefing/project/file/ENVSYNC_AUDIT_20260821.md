# ENVSYNC_AUDIT_20260821 — `.claude/` 兩端全清單 hash 盤點

**票**: INFRA-ENVSYNC-AUDIT-01
**盤點時間**: 2026-08-21
**PLT**: `D:/DevSources/workflow/mcp-ticket-platform/.claude/`
**APP**: `D:/DevSources/AndroidProj/SmartCameraApp1/.claude/`（本 repo）
**排除範圍**: `__pycache__/`、`worktrees/`、`*.bak`、`PROGRESS*.md`（各端活體，恆不同步）

本票唯讀盤點，**未執行任何同步動作**，不觸碰 `.claude/` 下任何檔案。

## 基線漂移（AI-0）

兩端 `.claude/` 頂層各 12 項，與矩陣 §1 記載一致，**無漂移**：
- PLT 12 項（含 `worktrees/`）
- APP 12 項（含 `settings.json.pre-decomm.bak`）

## 聯集統計

- PLT 側檔案總數：58
- APP 側檔案總數：48
- 聯集（去重後）：**67**（= 58 + 48 − 39 重疊，重疊含 IDENTICAL 15 + DIVERGED 22 + SYNC01_承接中 2）
- 判定分布：IDENTICAL 15 / DIVERGED 22 / PLT_ONLY 19 / APP_ONLY 9 / SYNC01_承接中 2

## G-3：APP 根目錄 PROGRESS.md 指涉查核

`test -f PROGRESS.md`（repo 根，非 `.claude/PROGRESS.md`）→ **不存在**。APP 側僅 `.claude/PROGRESS.md` 一份，CLAUDE.md Boot Sequence 指涉之 `PROGRESS.md` 無路徑歧義（本票 FORBIDDEN 涵蓋 `.claude/**`，不建議另行處理，僅記錄查核結果消解 G-3）。

## 全量四欄表（AI-1/AI-2，67 列＝聯集數）

| 相對路徑 | PLT hash | APP hash | 判定 | 分類 | 定年（PLT／APP）| diff --stat 摘要 |
|---|---|---|---|---|---|---|
| `CLAUDE.md` | `30bb9bc7` | `df14b55b` | DIVERGED | 禁搬（C-1/C-2/C-3 端專屬） | `4d2aacf 2026-08-10 16:06:23 +0800` ／ `39e497e 2026-07-25 15:10:57 +0800` | 1 file changed, 10 insertions(+), 11 deletions(-) |
| `agents/cross-reviewer.md` | `8efcac68` | `da546d1a` | DIVERGED | 候選同步 | `602de3b 2026-08-07 20:37:21 +0800` ／ `94c5791 2026-07-18 14:50:07 +0800` | 1 file changed, 7 insertions(+), 9 deletions(-) |
| `hooks/mcp_service_pid_guard.py` | `090d50ed` | `—` | PLT_ONLY | 禁搬（端專屬 hook） | — | — |
| `hooks/snapshot_cueshot_memory.py` | `—` | `96ae34ad` | APP_ONLY | 禁搬（端專屬 hook） | — | — |
| `hooks/stop.sh` | `af45101b` | `cb3d0fef` | DIVERGED | 候選同步 | `0c6cb88 2026-08-10 16:06:41 +0800` ／ `f904908 2026-05-29 23:18:20 +0800` | 1 file changed, 2 insertions(+), 2 deletions(-) |
| `hooks/telegram_ask.sh` | `b38e4a70` | `b38e4a70` | IDENTICAL | —（IDENTICAL） | — | — |
| `hooks/telegram_stop.sh` | `775109c9` | `775109c9` | IDENTICAL | —（IDENTICAL） | — | — |
| `hooks/tests/telegram_stop_test.sh` | `1bfc38d7` | `1bfc38d7` | IDENTICAL | —（IDENTICAL） | — | — |
| `rules/android-conventions.md` | `—` | `da7fffca` | APP_ONLY | 禁搬（APP 獨有 rules） | — | — |
| `rules/camera-constraints.md` | `—` | `9aa74c37` | APP_ONLY | 禁搬（APP 獨有 rules） | — | — |
| `rules/compose-ui.md` | `—` | `fc3f8115` | APP_ONLY | 禁搬（APP 獨有 rules） | — | — |
| `rules/concurrency.md` | `13ba97e2` | `c5dab635` | DIVERGED | 候選同步 | `55da959 2026-08-06 01:01:59 +0800` ／ `882ec5b 2026-08-01 21:41:41 +0800` | 1 file changed, 3 insertions(+), 1 deletion(-) |
| `rules/critical-files.md` | `—` | `02e56668` | APP_ONLY | 禁搬（APP 獨有 rules） | — | — |
| `rules/cross-review.md` | `b0a21ac0` | `cb0f870c` | DIVERGED | 候選同步 | `b6acdcf 2026-07-27 16:09:25 +0800` ／ `fbdb18c 2026-07-18 18:21:02 +0800` | 1 file changed, 3 insertions(+), 4 deletions(-) |
| `rules/decommission.md` | `7ecf63cf` | `fd7cfd2a` | DIVERGED | 候選同步 | `55da959 2026-08-06 01:01:59 +0800` ／ `882ec5b 2026-08-01 21:41:41 +0800` | 1 file changed, 6 insertions(+) |
| `rules/kairos-lessons.md` | `0dc9c0fe` | `d55f394b` | DIVERGED | 候選同步 | `b6acdcf 2026-07-27 16:09:25 +0800` ／ `882ec5b 2026-08-01 21:41:41 +0800` | 1 file changed, 24 insertions(+), 15 deletions(-) |
| `rules/mcp-service-safety.md` | `1af7a1cb` | `—` | PLT_ONLY | 禁搬（PLT 獨有 rules） | — | — |
| `rules/phase0-lessons.md` | `9fef66a7` | `5a9fe01b` | DIVERGED | 候選同步 | `b6acdcf 2026-07-27 16:09:25 +0800` ／ `1c47797 2026-06-02 18:24:27 +0800` | 1 file changed, 6 insertions(+), 7 deletions(-) |
| `rules/ptf-rules.md` | `—` | `72090c12` | APP_ONLY | 禁搬（APP 獨有 rules） | — | — |
| `rules/ssot-governance.md` | `8960acb1` | `0262ac77` | DIVERGED | 候選同步 | `b64c381 2026-08-19 20:51:37 +0800` ／ `085d8bd 2026-08-14 17:16:14 +0800` | 1 file changed, 14 insertions(+), 53 deletions(-) |
| `rules/state-machine.md` | `—` | `c306c973` | APP_ONLY | 禁搬（APP 獨有 rules） | — | — |
| `rules/ticket_pattern.md` | `4640b730` | `067600b3` | DIVERGED | 候選同步 | `33f5599 2026-08-17 13:06:46 +0800` ／ `f0000c3 2026-08-14 17:16:15 +0800` | 1 file changed, 2 insertions(+), 4 deletions(-) |
| `scripts/m3_family_audit.sh` | `e7aba503` | `e7aba503` | IDENTICAL | —（IDENTICAL） | — | — |
| `settings.json` | `eb52255b` | `ba327195` | DIVERGED | 禁搬（S-2/S-3 端專屬） | `0cb83c9 2026-08-15 01:09:41 +0800` ／ `0b321c2 2026-08-01 16:01:38 +0800` | 1 file changed, 16 insertions(+), 19 deletions(-) |
| `settings.local.json` | `921b740a` | `d48b50b1` | DIVERGED | 候選同步 | `N/A` ／ `d67da50 2026-06-04 17:15:42 +0800` | 1 file changed, 11 insertions(+), 4 deletions(-) |
| `skills/cleanchat/SKILL.md` | `2d30036f` | `2d30036f` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/cleanchat/scripts/md-formatting.sh` | `04399578` | `04399578` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/compact/SKILL.md` | `7b2fc376` | `0ed0676a` | DIVERGED | 候選同步 | `b64c381 2026-08-19 20:51:37 +0800` ／ `5ab5d3c 2026-07-30 22:28:13 +0800` | 1 file changed, 13 insertions(+), 36 deletions(-) |
| `skills/dream/SKILL.md` | `6fda83ae` | `6fda83ae` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/dtaudit/SKILL.md` | `—` | `d1de4aab` | APP_ONLY | 禁搬（APP 獨有 skill） | — | — |
| `skills/dtaudit/references/cueshot-example.md` | `—` | `b419e297` | APP_ONLY | 禁搬（APP 獨有 skill） | — | — |
| `skills/index-verify/SKILL.md` | `7559c891` | `7559c891` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/index-verify/scripts/index_path_verify.sh` | `d05bf27a` | `d05bf27a` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/mdlint/SKILL.md` | `28521913` | `68145ca0` | DIVERGED | 候選同步 | `b6acdcf 2026-07-27 16:09:25 +0800` ／ `1c47797 2026-06-02 18:24:27 +0800` | 1 file changed, 1 insertion(+), 1 deletion(-) |
| `skills/normal/SKILL.md` | `1c3bca26` | `1c3bca26` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/pythonw-design/SKILL.md` | `fd93fcb9` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/pythonw-design/references/desktop-ui-principles.md` | `21ab7f36` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/pythonw-design/references/review-checklist.md` | `1a2bc152` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/pythonw-design/templates/review-template.md` | `a4f1ae27` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/qa/SKILL.md` | `20b09149` | `3caa6a54` | DIVERGED | 候選同步 | `f09d956 2026-07-31 16:08:19 +0800` ／ `39e497e 2026-07-25 15:10:57 +0800` | 1 file changed, 14 insertions(+), 24 deletions(-) |
| `skills/qa/scripts/lib_verify.sh` | `f2cc21f5` | `f2cc21f5` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/qa/scripts/static_blind_scan.sh` | `7b2be731` | `6bc04739` | DIVERGED | 候選同步 | `990d924 2026-07-27 16:09:15 +0800` ／ `4f06a27 2026-07-06 22:21:02 +0800` | 1 file changed, 97 insertions(+), 44 deletions(-) |
| `skills/qa/scripts/structural_verify.sh` | `d017c679` | `d017c679` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/qa/scripts/verify_checklist.sh` | `a03b2957` | `defce99e` | DIVERGED | **候選同步 ⚠️見下方標註** | `f3e0b85 2026-07-31 15:21:05 +0800` ／ `0bb6b4c 2026-08-15 17:14:47 +0800` | 1 file changed, 47 insertions(+), 38 deletions(-) |
| `skills/status/SKILL.md` | `9420248d` | `9420248d` | SYNC01_承接中 | —（SYNC01 承接中） | — | — |
| `skills/status/scripts/statusline.py` | `b3e26bf9` | `b3e26bf9` | SYNC01_承接中 | —（SYNC01 承接中） | — | — |
| `skills/sv/CROSS_REVIEW.md` | `b19e1baf` | `11d30051` | DIVERGED | 候選同步 | `9e4e90f 2026-07-27 16:09:20 +0800` ／ `46b8e24 2026-07-06 22:52:14 +0800` | 1 file changed, 1 insertion(+), 1 deletion(-) |
| `skills/sv/SKILL.md` | `6ae86be2` | `c729153f` | DIVERGED | 候選同步 | `e0fd42e 2026-08-06 13:05:05 +0800` ／ `4f06a27 2026-07-06 22:21:02 +0800` | 1 file changed, 31 insertions(+), 45 deletions(-) |
| `skills/sv/scripts/cross_review.sh` | `82ac43cf` | `3e8f09a3` | DIVERGED | **候選同步 ⚠️見下方標註** | `81712d1 2026-07-27 20:06:19 +0800` ／ `8044a0d 2026-07-21 01:24:54 +0800` | 1 file changed, 19 insertions(+), 15 deletions(-) |
| `skills/sv/ssot_context.md` | `e996b0c1` | `6092b217` | DIVERGED | 候選同步 | `9e4e90f 2026-07-27 16:09:20 +0800` ／ `fb27f37 2026-07-15 15:18:23 +0800` | 1 file changed, 7 insertions(+), 5 deletions(-) |
| `skills/telegram-notify/SKILL.md` | `82627956` | `82627956` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/telegram-notify/scripts/notify.sh` | `8c9086cd` | `8c9086cd` | IDENTICAL | —（IDENTICAL） | — | — |
| `skills/ticket-rigor-gate/EXAMPLE.md` | `e3db3bcc` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/ticket-rigor-gate/OPS_CALIBRATION.md` | `3db630a9` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/ticket-rigor-gate/REPAIR_ROUTING.md` | `1e3c250b` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/ticket-rigor-gate/SKILL.md` | `e73151d0` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/ticket-rigor-gate/runner/README.md` | `9af35c05` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/ticket-rigor-gate/runner/profiles/model-profiles.v1.json` | `d5ca7b1b` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/ticket-rigor-gate/runner/prompts/ticket-repair-prompt.v1.txt` | `2506f884` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/ticket-rigor-gate/runner/repair_runner.sh` | `5340311c` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/ticket-rigor-gate/runner/schemas/ticket-repair-payload.v1.json` | `1abf5e35` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json` | `ddaa0a48` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/uc/SKILL.md` | `8690193b` | `049654d2` | DIVERGED | 候選同步 | `de9e0cb 2026-08-19 21:47:31 +0800` ／ `6b74eab 2026-07-14 14:08:14 +0800` | 1 file changed, 10 deletions(-) |
| `skills/wktree-close/SKILL.md` | `b628643d` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/wktree-close/scripts/wktree-close.sh` | `2d1974db` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `skills/wktree-compact/SKILL.md` | `7a3eb580` | `—` | PLT_ONLY | 禁搬（PLT 獨有 skills） | — | — |
| `verify-policy.env` | `d87079ed` | `d87079ed` | IDENTICAL | —（IDENTICAL） | — | — |

## AI-3 分類統計

| 分類 | 數量 | 說明 |
|---|---|---|
| IDENTICAL | 15 | 無需動作 |
| SYNC01_承接中 | 2 | `INFRA-STATUS-SYNC-01` 承接，禁重複列入候選同步 |
| 禁搬 | 30 | 19 PLT_ONLY（4 個 PLT 獨有 skills 目錄：pythonw-design/ticket-rigor-gate/wktree-close/wktree-compact + mcp_service_pid_guard.py + mcp-service-safety.md）＋ 9 APP_ONLY（snapshot_cueshot_memory.py + 6 APP 獨有 rules + dtaudit 2 檔）＋ 2 DIVERGED（`settings.json`／`CLAUDE.md`） |
| 候選同步 | 20 | 全數為 DIVERGED（APP 較舊或內容分叉）；機械規則下無 PLT_ONLY 落入本類（PLT_ONLY 全數命中禁搬字面清單） |
| 待裁 | 0 | 無（APP_ONLY 9 檔全數命中禁搬清單，無剩餘） |

## ⚠️ 候選同步表重要標註（供 Akira 裁決）

- **`skills/qa/scripts/verify_checklist.sh`**：APP 端定年 `0bb6b4c 2026-08-15` **晚於** PLT 端 `f3e0b85 2026-07-31`。此因 **本 session** 剛完成 `INFRA-MAXLINES-GATE-01`（MAX_LINES execution gate 退役，量尺校正 + 四分支不阻斷 + `maxlines_verdict()` self-test），尚未同步回 PLT。**若逕行 PLT→APP 同步本檔，將覆蓋並遺失本 session 剛完成之工作**。機械分類規則下仍落「候選同步」，但強烈建議裁決時排除或反向處理（APP→PLT）。
- **`skills/sv/scripts/cross_review.sh`**：本 session 稽核發現 APP 端存在治理路徑排除邏輯缺陷（`.claude/` 整段誤排除於 basename-only `ALLOWED` 情境），已開票 `INFRA-CROSSREVIEW-GOVPATH-EXCL-01`（`Pending/`，未派工）。PLT 端此檔內容未經核對是否亦含同一缺陷或已修復；同步前建議先核對 PLT 端邏輯，避免用未知狀態覆蓋待修復標的。
- **`settings.local.json`**：PLT 側 `git log` 定年為 `N/A`（該檔於 PLT repo 無 commit 歷史，可能為該端 gitignored 或從未納入版控），機械規則下仍歸候選同步；建議 Akira 額外確認此檔在 PLT 端的版控狀態，避免同步一份「PLT 端本身也不受版控保護」的設定檔。
- **`rules/ssot-governance.md`**：diff --stat 顯示 APP 端 `14 insertions(+), 53 deletions(-)` 為本表最大幅度分叉之一，建議優先審閱。

## 待 Akira 裁決清單（AI-4）

1. **候選同步全表**（20 項，見上表「候選同步」標記行）：`agents/cross-reviewer.md`、`hooks/stop.sh`、`rules/concurrency.md`、`rules/cross-review.md`、`rules/decommission.md`、`rules/kairos-lessons.md`、`rules/phase0-lessons.md`、`rules/ssot-governance.md`、`rules/ticket_pattern.md`、`settings.local.json`、`skills/compact/SKILL.md`、`skills/mdlint/SKILL.md`、`skills/qa/SKILL.md`、`skills/qa/scripts/static_blind_scan.sh`、`skills/qa/scripts/verify_checklist.sh`（⚠️見上）、`skills/sv/CROSS_REVIEW.md`、`skills/sv/SKILL.md`、`skills/sv/scripts/cross_review.sh`（⚠️見上）、`skills/sv/ssot_context.md`、`skills/uc/SKILL.md`
2. **CLAUDE.md／settings.json 段落級合併是否另立票**：本票禁整檔覆寫（C-2/C-3/S-2/S-3 端專屬內容在內），兩檔均分類為禁搬且需人工段落級比對，建議另立專票處理，不建議併入候選同步批次同步。
3. **RAPID_MODE 是否引入 APP（C-1，治理憲章級變更）**：PLT 端 `CLAUDE.md` 已有 RAPID_MODE 概念（Boot Sequence 讀取 `docs/RAPID_MODE.md`、§0 凌駕條款、SOP 步驟跳過邏輯，L8/L13/L19/L125 共 4 處引用）；APP 端 `CLAUDE.md`／`settings.json` 全域零命中 `RAPID_MODE`。此為治理憲章級差異，**不建議隨候選同步批次處理**，需 Akira 單獨裁決是否引入。
4. **待裁項逐一**：本次盤點無 `待裁` 分類項（0 項）。

## 抽樣複驗（AC 行為性，執行後回填）

任取 3 檔重跑 `git hash-object` 複驗（1 IDENTICAL + 2 DIVERGED，production 路徑重放）：

| 檔案 | 首跑值 | 複驗值 | 一致 |
|---|---|---|---|
| `hooks/telegram_ask.sh`（IDENTICAL） | PLT `b38e4a70` / APP `b38e4a70` | PLT `b38e4a70` / APP `b38e4a70` | ✅ |
| `rules/ssot-governance.md`（DIVERGED，最大分叉） | PLT `8960acb1` / APP `0262ac77` | PLT `8960acb1` / APP `0262ac77` | ✅ |
| `skills/qa/scripts/verify_checklist.sh`（DIVERGED，本 session 觸碰） | PLT `a03b2957` / APP `defce99e` | PLT `a03b2957` / APP `defce99e` | ✅ |
