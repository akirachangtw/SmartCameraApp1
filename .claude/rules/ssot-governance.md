---
description: PROGRESS.md / SSOT 文件治理規則 — 確保儀表板精簡、歸檔紀律
globs:
  - ".claude/PROGRESS.md"
  - ".claude/PROGRESS_ARCHIVE.md"
  - "plans/architecture/*SSOT*.md"
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# SSOT & Progress Governance

## PROGRESS.md Dashboard 紀律

- PROGRESS.md **MUST** 保持 ≤80 行（目標 ≤50 行）。超過 80 行 → structural_verify.sh Check F.2 FAIL。
- 已完成的 Session Notes（`>` 開頭行）**MUST** 在 3 個 Session 內移至 `PROGRESS_ARCHIVE.md`。
- Current Day 行 **SHOULD** 使用 compact 表格格式列出最近 4–5 筆 Ticket，不使用 run-on 純文字串。
- Sprint Health `Schedule` 欄 **MUST** 僅列最近 5 筆 Ticket 狀態，歷史狀態移至 ARCHIVE。
- `PROGRESS_ARCHIVE.md` **MAY** 無行數限制，但 **MUST** 保持追加順序（新內容加在尾部）。

## PROGRESS_ARCHIVE.md Sprint 邊界治理

- Sprint 結束時，**SHOULD** 將整份 ARCHIVE 重命名為 `PROGRESS_ARCHIVE_Sprint[N].md`，新 Sprint 從空 ARCHIVE 開始。
- Session-level 散文 **MUST** 在歸檔前壓縮為 compact 表格格式（保留：Session ID / Ticket / 一句話任務 / Status / Commit）。
- Build/Test/Lint/ELF 詳細結果 **MUST NOT** 進入 ARCHIVE（活在 `sv_report.md` + git log）。

## SSOT 版本引用紀律

- `CLAUDE.md` / `PROGRESS.md` / `CSO_INDEX.md` / `UITA_INDEX.md` **MUST** 只引用現行 SSOT 版本路徑。
- 每次 SSOT 版本升級 **MUST** 在同一 commit 中同步更新所有引用文件。
- `/index-verify` **SHOULD** 在每次 SSOT 升級後立即執行，確認零過期引用。
- 引用已刪除路徑 → structural_verify.sh Check F 或 index-verify **MUST** 攔截。

## Ticket Closure 紀律

- **勾選紀律**：碼閉環 + `/sv` PASS（Codex Cross-Review 終局 PASS）後，主 session **MUST** 逐項勾選母票 Action Items 與 Acceptance Criteria；僅勾「有 SV / QA / commit 佐證」之項；`[行為性]` / `[實機]` 未實測項 **MUST** 保留未勾並於 PROGRESS.md 誠實揭露（K5-Rule 29）。**MUST** 於該項目實際完成的同一回合內立即勾選（Edit 落地 / 測試轉綠 / Codex PASS / commit 成功發生當下），**MUST NOT** 留待後續 /sv Step 10 之外的另一輪「確認完成度」請求才回頭補勾。
- **搬移紀律**：勾選完成後，票檔 **MUST** 由 `plans/cso_briefing/project/Pending/` 搬移至 `plans/cso_briefing/project/deliver/`。
- **修補票紀律**：母票存在未閉環 AC → **MUST** 同步產出修補票（`-FIX-[X]` 後綴或後續票）置於 `Pending/`，載明追蹤條目與轉 PASS 條件。
- 母票 AC 佐證不明（無法判定該勾或保留）→ **MUST** 停止勾選該項並列入修補票追蹤，禁止推定勾選。
- 觸發來源：INFRA-CLOSURE-SOP-01（Akira 2026-07-17 對話裁決：全票適用之標準收尾流程；實例缺口 INFRA-AGENTS-CR-02 Done 但票內 checkbox 全未勾）；即時勾選條款觸發來源：`INFRA-QA-COMMIT-EMPTYDIFF-01` + `-FIX-A`（碼閉環與 Codex PASS 數日後才因 Akira 詢問完成度而補勾，Akira 2026-07-19 對話裁決授權補訂）。
