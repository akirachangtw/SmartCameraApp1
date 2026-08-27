---
description: PROGRESS.md / SSOT 文件治理規則 — 確保儀表板精簡、歸檔紀律
globs:
  - ".claude/PROGRESS.md"
  - ".claude/PROGRESS_ARCHIVE.md"
  - "plans/architecture/*SSOT*.md"
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# SSOT & Progress Governance

## Governance Artifact 清單（單一真相來源）

`/compact` Auto Commit 步驟 **MUST** 依此表逐檔獨立提交，Scope 汙染判斷式 **MUST** 以此表為準（表列外之 tracked dirty 檔案才判 BLOCKED）；新增 Governance Artifact 只需在此表加一行，**MUST NOT** 另開票或單獨改 SKILL.md 判斷邏輯。

| 檔案路徑 | Commit Prefix | 說明 |
| ------- | ------------- | ---- |
| `.claude/PROGRESS.md` | `[COMPACT-SYNC]` | Sprint Dashboard |
| `.claude/PROGRESS_ARCHIVE.md` | `[COMPACT-SYNC]` | Dashboard 歸檔 |
| `plans/cso_briefing/project/TICKET_REGISTRY.json` | `[GOVERNANCE-SYNC]` | 跨票狀態總帳（Manifest/Registry 區分見 `.gitignore` 註解，2026-07-28 CSO 裁決） |
| `.claude/rules/*.md` | `[GOVERNANCE-SYNC]` | 執行者面治理規則載體（含本檔；目錄級表列，新增規則檔免再補表。Akira 2026-08-14 對話裁決：`.claude/rules/` 皆以 `[GOVERNANCE-SYNC]` 提交） |

觸發來源：Akira 2026-07-30 對話裁決——`.gitignore` Registry 裁決記錄僅存於 PROGRESS.md 敘述文字/memory，未回寫進 `compact/SKILL.md` 執行邏輯，導致每次 `/compact` 仍以單檔硬編碼判斷 Scope 汙染；改為表驅動以杜絕「規則寫了但執行邏輯沒讀到」重演。

## PROGRESS.md Dashboard 紀律

- PROGRESS.md **MUST** 保持 ≤80 行（目標 ≤50 行）。超過 80 行 → structural_verify.sh Check F.2 FAIL。
- 已完成的 Session Notes（`>` 開頭行）**MUST** 在 3 個 Session 內移至 `PROGRESS_ARCHIVE.md`。
- Current Day 行 **SHOULD** 使用 compact 表格格式列出最近 4–5 筆 Ticket，不使用 run-on 純文字串。
- Sprint Health `Schedule` 欄 **MUST** 僅列最近 5 筆 Ticket 狀態，歷史狀態移至 ARCHIVE。
- `PROGRESS_ARCHIVE.md` **MAY** 無行數限制，但 **MUST** 保持追加順序（新內容加在尾部）。

## Pipeline Artifact 豁免邊界

- Pipeline Artifact 豁免（`verify_checklist.sh` `PIPELINE_ARTIFACT` + `PIPELINE_ARTIFACT_MAX_LINES`）僅限 `/qa` Step 4 產生的**增量** Dashboard／ARCHIVE 同步。
- **任何服務於票面實質目的、不在 `ALLOWED` 內的內容變更（無論大小）MUST 明列進 `ALLOWED`**，不得倚賴 Pipeline Artifact 豁免夾帶。
- 任何 `sv_report.md` **MUST NOT** 以「Pipeline Artifact」標籤自行擴張 `ALLOWED` 範圍（`MEO-T3-FIX-A` Hard Constraint 4 常設化）。
- 超過 `PIPELINE_ARTIFACT_MAX_LINES` 的合法大規模輪替 **MUST** 走明列 `ALLOWED` 路徑取得授權，不得靠豁免放行；且 `PROGRESS_ARCHIVE.md` 輪替（3-session 規則）到期時 **MUST** 延後至專用 docsync 票執行（前例 `MEO-T3-DOCSYNC-01`），**MUST NOT** 在無關票內就地輪替。

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
  **凍結點語義**（INFRA-TRACKSCOPE-01，2026-08-27）：`Pending/` 期票檔為 untracked，搬移至 `deliver/` 之首次 commit 即該票檔進入版控之凍結版本，此前修訂史不入 git log。
- **修補票紀律**：母票存在未閉環 AC → **MUST** 同步產出修補票（`-FIX-[X]` 後綴或後續票）置於 `Pending/`，載明追蹤條目與轉 PASS 條件。
- 母票 AC 佐證不明（無法判定該勾或保留）→ **MUST** 停止勾選該項並列入修補票追蹤，禁止推定勾選。
- **冊面轉態紀律**：票檔層三步（勾選／搬移／修補票）完成後，`TICKET_REGISTRY.json` 對應條目 **MUST** 同步完成**四要件**——①`lifecycle` 轉終態；②`reviews` 四欄（qa／sv／codex／traycer）各填實測值，無可考記錄者填 `N_A` 並於 checkpoint 揭露豁免依據，**MUST NOT** 以推定補 `PASS`（撤警正向取證律）；③`file` 欄指標由 `Pending/` 改指實際落點 `deliver/` 或 `archive/`；④`checkpoint.resolution` 寫入收尾實錄（含 commit hash、審查終值來源、豁免揭露）。
- **原子性**：四要件 **MUST** 於同一收尾回合內完成，**MUST NOT** 分批跨 session；任一要件缺漏即視為該票**未收尾**，不得回報結案。冊面異動走獨立 `[GOVERNANCE-SYNC]` commit，寫入後 **MUST** 以 scoped `git_diff` 驗證零意外變更（「新內容已存在」不足為據）。
- **矛盾停止**：票檔層與冊面層狀態不一致（如票檔已搬 `deliver/` 而冊面 `file` 仍指 `Pending/`）→ **MUST** 停止回報並列入待裁，禁單方推定何者為真。
- **Dashboard 慣例同步**：`.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` 之例行 Dashboard 列更新得與實作同 commit 或以 `[COMPACT-SYNC]` 獨立提交，**不計入**票面「git diff --stat 僅涉 ALLOWED」AC 之違規判定（INFRA-GOVSYNC-01 定調，消解 RGR-APPDEPLOY-01 G2 帳實矛盾）。
> **強制層級（誠實定性）**：本節「冊面轉態紀律」為**紀律層**條款，非機械閘。冊面轉態發生於 Desktop 治理 session，而 `verify_checklist.sh` / `structural_verify.sh` 執行於 Claude Code 端——**閘門位置與動作發生位置不重合**，腳本層對本失效型無攔截力（`INFRA-TPL-V32-01` 事故全程未經 Claude Code）。真正之機械強制須由 mcp-ticket-platform server 端提供（`lifecycle` 轉終態時強制 `checkpoint` 非空、`file` 指標存在性校驗），屬 TKR-05 平台原語域，依前置資源恆存律**不得由本票預支**；在該閘落地前，本節效力等同宣告式紀律，違反不會被工具攔截。
- 觸發來源：INFRA-CLOSURE-SOP-01（Akira 2026-07-17 對話裁決：全票適用之標準收尾流程；實例缺口 INFRA-AGENTS-CR-02 Done 但票內 checkbox 全未勾）；即時勾選條款觸發來源：`INFRA-QA-COMMIT-EMPTYDIFF-01` + `-FIX-A`（碼閉環與 Codex PASS 數日後才因 Akira 詢問完成度而補勾，Akira 2026-07-19 對話裁決授權補訂）。
- **FORBIDDEN 延後項免開票紀律**：票面 `FORBIDDEN` 命中 `.claude/PROGRESS.md` 導致的收尾延後項（Dashboard 同步／`PROGRESS_ARCHIVE.md` Done 標記／票檔 Pending→deliver 搬移／票內 AC checkbox 勾選）**MUST NOT** 另開獨立治理票（如 `*-DOCSYNC-01`）承接；此四項本屬標準 SOP 收尾流程，**MUST** 於下一輪 `/compact` 或後續 session 之正常收尾動作中處理即可。觸發來源：`INFRA-CONCURRENCY-01`（Akira 2026-07-19 對話裁決：「這本來就是標準的 SOP 執行票流程」，避免治理疊床架屋）。
- **結構歧義停損律**(2026-08-01 CSO 確認決議):同一子系統下列任一**字面**成立,MUST 停止開立/受理新修補票並升報 CSO:①同一票修訂版次 ≥ 3(票面 Rev 直取);②結構性死結/自指阻塞 ≥ 2 種(決策檔/Handoff 記載筆數直取);③任一 workaround 額度制用量達上限 50%;④修補深度 ≥ 2(FIX 票之 FIX 票)。凍結僅及修補線,不及執行中之非修補工作;解除條件=外審回流+CSO 存廢三案(退役/降級/續修)裁決落檔。禁語義裁量(「此票很重要」不豁免)。SSOT:vault `2026-08-01_Structural_Ambiguity_Stop_Loss_Law.md`;觸發來源:MEO v1 十輪修補無停損(`MEO_V1_Enforcement_Layer_Decommission`)。

## Ticket Rigor Gate（選用制，INFRA-GOVSYNC-01）

- APP 冊 lifecycle 維持 `PENDING / REVIEW / CLOSED / VOID`，**不引入** PLT 五段制之 `TICKET_RIGOR / IN_PROGRESS / EXECUTED` 狀態——此為對 PLT 全閘制之刻意裁剪（Akira 2026-08-27 治理減量裁決），非同步遺漏；日後兩端規則合併裁決 MUST 以本節為 APP 基線。
- 新票**得**於派工前執行 `.claude/skills/ticket-rigor-gate/` 取品質報告；Akira 明示指派 rigor 檢核之票 **MUST** 執行並於派工訊息附報告要點。
- 報告與遙測一律落 `.tmp/rigor-gate/`（`RGR-APPDEPLOY-01` L2 裁決：執行期產物，KEEP_N=10 輪替）；**MUST NOT** 於任何票面 AC 引用 `.tmp/` 下 rigor 產物為長期證據——耐久結論唯一載體為票面／冊面收尾實錄。
- 本節不新增任何 MUST 級派工前置；未跑 gate 之票不因此退件。
