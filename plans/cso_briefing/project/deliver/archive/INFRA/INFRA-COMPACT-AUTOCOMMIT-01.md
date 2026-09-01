# INFRA-COMPACT-AUTOCOMMIT-01: /compact 產出交接檔後自動 commit PROGRESS.md

**簽發日期**: 2026-07-19
**簽發者**: Claude
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: 免 Traycer（純 .md 治理票，比照 INFRA-SV-ZERODIFF-01 / INFRA-CLOSURE-SOP-01 / INFRA-SKILLS-UC02 先例，Akira 2026-07-19 裁決；僅豁免 SOP 第5步人工終審，Codex Cross-Review 第4.5步不受影響）
**前置依賴**: 無（Akira 對話裁決「執行方向 A」+「同意產出修補票」，2026-07-19）

> 背景：現行 `/compact` 完成 Housekeeping 後即結束，PROGRESS.md 的 dashboard 同步異動需使用者另外下指令做原子化 git commit（本 session 實例：`cfa4fc5`）。本票將此手動步驟收斂為 `/compact` 自身流程的最後一步，比照既有 `INFRA-COMPACT-SYNC-01/02/03` commit message 慣例，但自動化編號與觸發。

## Verify Config
TICKET_ID=INFRA-COMPACT-AUTOCOMMIT-01
CREATED=2026-07-19
PRESET=refactor
ALLOWED=.claude/skills/compact/SKILL.md
FORBIDDEN=.claude/CLAUDE.md,.claude/settings.json,.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/sv/scripts/cross_review.sh,.claude/agents/cross-reviewer.md,.claude/rules/cross-review.md,.claude/rules/ticket_pattern.md
MAX_LINES=40
DIFF_REF=cfa4fc5

## Action Items

### AI-1: Housekeeping 後新增 Auto Commit 步驟
- **檔案**: `.claude/skills/compact/SKILL.md`（現行 `## 最後 Housekeeping` 之後，約 :88-89 一帶）
- **現值**: Housekeeping 完成後僅輸出確認訊息，PROGRESS.md 的異動維持 uncommitted，需使用者另下指令提交
- **目標值**: 在確認訊息之後新增 `## Auto Commit: PROGRESS.md` 步驟，依序：
  1. **Scope 汙染確認**：執行 `git status --short`，除 `.claude/PROGRESS.md` 外若有其他 **tracked** dirty 檔案 → **MUST** 停止，回報 `⛔ Auto Commit BLOCKED: Scope 汙染 [檔案清單]`，**不執行 commit**（防止 /compact 的自動提交意外夾帶使用者其他未完成工作，違反 Rule 3 同一 commit 不混source/doc）
  2. **零 diff 判定**：執行 `git status --short -- .claude/PROGRESS.md`，輸出為空 → **MUST** 判定 SKIPPED，回報 `⚪ Auto Commit SKIPPED（PROGRESS.md 無異動）`，**不執行 commit**，**不得**使用 `git commit --allow-empty`
  3. **動態編號**：
     ```bash
     _LAST_N=$(git log --oneline --all 2>/dev/null | grep -oE "INFRA-COMPACT-SYNC-[0-9]+" | grep -oE "[0-9]+" | sort -n | tail -1)
     _NEXT_N=$(( ${_LAST_N:-0} + 1 ))
     ```
  4. **Commit**：`git add .claude/PROGRESS.md` → `git commit -m "[INFRA-COMPACT-SYNC-${_NEXT_N}] PROGRESS.md Dashboard 同步：/compact 交接"`，**SHOULD** 加 `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>` trailer
     - Commit 失敗 → `⛔ Auto Commit FAILED`，**MUST** 大聲回報，不得靜默吞錯
  5. **成功回報**：於既有確認訊息末尾附加一行 `📌 Auto Commit: [hash] [INFRA-COMPACT-SYNC-${_NEXT_N}]`
- **設計原則**：僅委任「已由 /compact 自身契約合法產生的 PROGRESS.md 異動」之提交動作；不擴權至任何其他檔案或情境，比照 `INFRA-QA-COMMIT-EMPTYDIFF-01` 零 diff 宣告制立法精神（零 diff 不強塞空提交，改為顯式 SKIPPED）

## Hard Constraints
1. 本票 `.md` 觸碰僅限 `.claude/skills/compact/SKILL.md` 單檔
2. 自動 commit 範圍 **MUST** 僅限 `.claude/PROGRESS.md`，**MUST** 禁止 `git add .` 或萬用字元 add
3. 零 diff 情境 **MUST NOT** 使用 `git commit --allow-empty`
4. 偵測到非 `.claude/PROGRESS.md` 的 tracked scope 汙染時 **MUST** STOP，不自動 commit，不得靜默略過汙染檔案
5. Commit message family `INFRA-COMPACT-SYNC-[N]` 沿用既有慣例（`01/02/03` 已存在於 git log），本票僅使規則自動化，不變更既有 3 筆歷史訊息格式

## Error Handling
- `git log` 掃描 `INFRA-COMPACT-SYNC-[0-9]+` 若因故無匹配（理論上不會，因至少 3 筆歷史存在）→ `_LAST_N` 為空時 **MUST** fallback 為 `0`（即 `_NEXT_N=1`），**不得**因此中止流程
- 若 Scope 汙染清單含 `.claude/CLAUDE.md` 等 FORBIDDEN 檔案 → 依現有 Rule 4a 邏輯照常 STOP 並回報，不特殊處理

## Acceptance Criteria
- [x] **[行為性]** 正常情境：僅 `.claude/PROGRESS.md` dirty → Auto Commit 成功，commit message 含正確遞增後的 `INFRA-COMPACT-SYNC-[N]`，`git diff --stat` 僅涉及 `.claude/PROGRESS.md`（真實編號腳本 + 邊界案例合成驗證：03→04/08→09/09→10/empty→01，見 sv_report.md；**非**真實 `/compact` 端到端觸發，附重開條件：首次真實觸發若行為不符 → 重開本票或開 FIX-A）
- [x] **[行為性]** 零 diff 情境：`.claude/PROGRESS.md` 無異動 → SKIPPED，未產生新 commit，未使用 `--allow-empty`（`SKILL.md:93` 字面禁令 + 無繞過路徑之文字驗證，同上附重開條件）
- [x] **[行為性]** Scope 汙染情境：另有其他 tracked dirty 檔案（模擬情境）→ STOP，回報汙染清單，未執行 commit（`SKILL.md:92` STOP 分支字面驗證；Codex R1 REPORT_FIX 指出前版證據誤用本票實作中期 dirty 狀態類比，已於 sv_report.md 更正為純文字驗證，同上附重開條件）
- [x] `git diff --stat` 僅涉及 `.claude/skills/compact/SKILL.md`（Stage A 實測：`git diff --name-only 7324dab..HEAD` 僅 `.claude/skills/compact/SKILL.md` + `.claude/PROGRESS.md` pipeline artifact）
