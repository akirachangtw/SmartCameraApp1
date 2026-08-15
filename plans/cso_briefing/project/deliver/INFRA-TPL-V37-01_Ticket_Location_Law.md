# INFRA-TPL-V37-01: TPL v3.6→v3.7 立「票檔落點律」（票檔物理落點 = 實作專案票務目錄；設計/協調專案禁設 Pending/）

**簽發日期**: 2026-08-14
**簽發者**: Claude (SKAD)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無（事件支撐＝ADV-WTREE-01 誤置事件，耐久載體為 vault `2026-08-14_Native_Worktree_Migration_And_WKTR_Line_Establishment.md` 教訓 2）
- `coordination_dependency`: 無

**檔案治理/來源說明**：Akira 2026-08-14 裁決確立通道紀律：設計專案（Skill-Advisor 等）不放實作票，票一律開在對應實作專案並對應 TKR 平台冊面；Skill-Advisor/Pending/ 已裁定刪除。本票將該紀律立法入 TPL（SSOT＝app-repo 份；Smarter-Project 份為衍生副本須同批同步）。與 v3.6「發現來源 ≠ 歸屬」律分工：彼律管**議題線歸屬**（二維判定），本律管**票檔物理落點**（實作專案判定），二者聯用。

## Verify Config
TICKET_ID=INFRA-TPL-V37-01
CREATED=2026-08-14
PRESET=docs
ALLOWED=plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,D:/DevSources/Smarter-Project/file/Unified_Ticket_Template_v3.md(衍生副本同步)
FORBIDDEN=ticket_pattern.md（本律非 ID 文法異動，禁碰 regex SSOT）
MAX_LINES=N/A（docs 型條文增補，v3.5 後非 gate，估算 ~15 行）
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-08-13 22:38（本 session read_file_safe 全文 13,069 chars 兩段讀畢，audit aud_20260813_1400915364／aud_20260813_0b5719c9e8；派工時若有新 commit 越過此刻 MUST 重跑預檢）

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
- **動作**: 逐字確認 AI-1／AI-2 之「現值」錨點於檔內命中恰 1 次。任一不符 → 停止回報，禁自行推定等價位置。
- [x] 完成 —— 兩錨點各恰 1 次

### AI-1: 版本標頭增 v3.7 Revision 行
- **檔案**: `Unified_Ticket_Template_v3.md`
- **現值**（錨點，行首唯一）: `**Revision**: v3.6（2026-08-13，Akira 直接裁決）— 立「票號議題前綴律」`（該行全文保留不動，於其**後**插入新行）
- **目標值**（插入行）: `**Revision**: v3.7（2026-08-14，INFRA-TPL-V37-01）— 立「票檔落點律」：票檔 MUST 落於實作專案之票務目錄並登記於 TKR 平台對應 project 冊面；設計/協調型專案 MUST NOT 設 Pending/ 或存放票檔。與 v3.6「發現來源 ≠ 歸屬」分工：彼律管議題線歸屬，本律管物理落點。事件支撐：ADV-WTREE-01（2026-08-14 作廢重開 WKTR-01 於 mcp-ticket-platform）`
- [x] 完成 —— 逐字落地；附帶更正 L13「雙載體版控」→「單一載體」聲明（Akira 2026-08-15 裁決，Smarter-Project 副本已刪除不重建）

### AI-2: Action Items 前言區增「票檔落點律」blockquote
- **檔案**: `Unified_Ticket_Template_v3.md`
- **現值**（錨點，唯一）: `### AI-0: 錨點復驗（派工後、修改前）`（於該標題**前**插入新 blockquote，與既有三律並列）
- **目標值**（插入區塊）:
```
> **票檔落點律**（v3.7，Akira 2026-08-14 裁決；與「發現來源 ≠ 歸屬」二階判定配對執行）。票檔 MUST 落於**實作專案**之票務目錄（如 SmartCameraApp1 `plans/cso_briefing/project/Pending/`、mcp-ticket-platform `docs/pending/`），並登記於 TKR 平台**對應 project** 之冊面。設計／協調型專案（Skill-Advisor、Smarter-Project 等）僅承載審查、裁決、交付物與交接載體，**MUST NOT** 設 Pending/ 目錄或存放票檔。二律分工：議題線歸屬依「發現來源 ≠ 歸屬」二維判定（關聯性／相依性）；物理落點依本律取實作專案——議題線與落點專案**可以不同**（如 WKTR 線落 mcp-ticket-platform）。事件支撐：ADV-WTREE-01 開於 Skill-Advisor/Pending/（非 git、非實作場，DIFF_REF 恆 N/A、AC 無法承載實作驗收），2026-08-14 作廢重開為 WKTR-01。
```
- [x] 完成 —— 逐字落地於 AI-0 標題前

### AI-3: Smarter-Project 衍生副本同步
- **檔案**: `D:/DevSources/Smarter-Project/file/Unified_Ticket_Template_v3.md`
- **動作**: SSOT 修訂落地後，整檔覆蓋副本（禁逐字重謄——以 SSOT 全文直接 overwrite），並確認副本檔頭「衍生副本」標記仍在。
- [x] **廢除（Akira 2026-08-15 裁決）** —— 標的檔案不存在（已於 v3.6 前依既有裁決刪除，TPL 收斂為單一載體，記憶 `ticket-tpl-single-carrier` 明禁重建）。票面起草時未知悉此後續裁決，屬 SSOT 前提消滅型不可執行，非執行缺陷。本項廢除，禁重建副本。SSOT L13 已同步更正為「單一載體」聲明，「雙載體版控」措辭作廢。

## Hard Constraints
1. 禁動既有三律（錨點預檢律／序號機械取號律／票號議題前綴律）任何字面。
2. 禁碰 ticket_pattern.md（本律不涉 ID 結構文法）。
3. 副本同步走整檔 overwrite，禁 patch 式重謄。

## Error Handling
- AI-0 錨點命中數 ≠ 1 → 停止回報
- SSOT 檔已有 v3.7 Revision 行（他 session 先行）→ 停止回報，禁重複立法
- **[標配]** `git check-ignore -v` 對 SSOT 路徑命中 → 停止回報（gitignore 死結律）

## Acceptance Criteria
- [x] **[存在性]** SSOT 檔 grep `票檔落點律` 命中恰 2 處（Revision 行＋blockquote）
      — 實測：`grep -c` = 2
- [x] ~~**[存在性]** 副本檔 grep `票檔落點律` 命中恰 2 處且與 SSOT byte 級一致（副本標記行除外）~~ **廢除（Akira 2026-08-15 裁決）**：標的不存在，TPL 已收斂單一載體，禁重建副本；AC 判準本身前提消滅，非執行缺陷
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（Smarter-Project 份非 git，以檔案清單比對替代）
      — 實測：實作 commit `4b1e99b` 僅 `Unified_Ticket_Template_v3.md`（3 insertions）；本次附帶更正（L13 單一載體聲明）落於同檔 ALLOWED 範圍內
