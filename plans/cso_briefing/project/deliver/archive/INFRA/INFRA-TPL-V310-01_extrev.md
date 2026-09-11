# INFRA-TPL-V310-01: TPL v3.10 增列 INTRODUCED_CAPABILITY 與 DEPENDENCY_DELTA 兩欄位，供實作級外審機械判定取值

**簽發日期**: 2026-09-08
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無 —— 立法依據為 `Smarter-Project/file/Instructions_for_Claude.md` v2.8.7 觸發條件 §6（非票面載體，故不列票號）
- `coordination_dependency`: 無

## 檔案治理／來源說明

本票標的為 TPL SSOT 單一載體（app-repo 份，v3.7 已明定 Smarter-Project 衍生副本不重建）。立法來源為 Instructions v2.8.7 新增之「6. 實作級外審」條款：該條 a 款與 b③ 款分別取值於票面 `INTRODUCED_CAPABILITY` 與 `DEPENDENCY_DELTA`，二欄位於 TPL v3.9 尚不存在。依「前置資源恆存律」，強制條款交付前其前置資源之產生通道 MUST 已明文授權——本票即該授權；Instructions §6 已字面載明「本條自 TICKET_TPL v3.x 補入兩欄位後生效」，故本票為 §6 之 a／b③ 生效前提。

**本票不觸發 §6 自我阻斷之取證**：①a 款與 b③ 款尚未生效（生效條件即本票）；②b① `ALLOWED` 首層 path segment distinct 數 = 1（`plans`）；③b② AI 數 = 4（AI-0 不計），< 8；④c 款比例原則豁免成立——本票為純新增預設值欄位與文件性條目，不改判準、不改狀態機、不改執行邏輯、不改 AC 類型學。

## Verify Config
TICKET_ID=INFRA-TPL-V310-01
CREATED=2026-09-08
PRESET=docs
ALLOWED=plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md
FORBIDDEN=plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json
MAX_LINES=40
DIFF_REF=4371d86ed986c4ef19ca57cf851e137c02d9f8cf
ANCHOR_PROBED=2026-09-08 15:33
BASE_COMMIT=4371d86ed986c4ef19ca57cf851e137c02d9f8cf
TARGET_BLOB=N/A

**TARGET_BLOB 標 `N/A` 之依據**：v3.8 比例原則（`docs` preset 且單檔小改），V1 降級為 AI-0 錨點制。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
- **動作**: 逐字確認 AI-1～AI-4 之「現值」字串於檔內命中數與票面所載相符（標示「唯一」者恰 1 次）。
- **基線漂移三分流**：不符時依 v3.8 三分流處置，禁推定等價位置。
- [x] 完成（4/4 命中相符，實測見 AI-1~AI-4）

### AI-1: 檔頭版本號遞增
- **檔案**: `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
- **現值**（唯一）: `**版本**: v3.9`
- **目標值**: `**版本**: v3.10`
- [x] 完成（commit `8ba73b5`；`grep -c '\*\*版本\*\*: v3.10'`=1、`v3.9`=0）

### AI-2: Verify Config 樣板增列兩欄位
- **檔案**: `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
- **現值**（唯一）: `TARGET_BLOB=[逐標的 git hash-object，格式 path:sha8，多筆分號分隔]`
- **目標值**: 於該行後新增兩行——
```
INTRODUCED_CAPABILITY=[NONE | feature:<token> | skill:<token> | tech:<token>]
DEPENDENCY_DELTA=[NONE | BUILD:<coordinate>@<version> | TOOL:<name>@<version|N/A> | SERVICE:<name>]
```
- [x] 完成（commit `8ba73b5`；置於 `TARGET_BLOB` 行後，非另立平行區塊）

### AI-3: 新增欄位說明段
- **檔案**: `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
- **現值**（唯一，定位錨點）: `**BASE_COMMIT／TARGET_BLOB 欄位說明**（v3.8，`INFRA-TPL-V38-01`）`
- **目標值**: 於該段落結束後、`**MAX_LINES 量尺定義與性質**` 之前，新增一段：

> **INTRODUCED_CAPABILITY／DEPENDENCY_DELTA 欄位說明**（v3.10，`INFRA-TPL-V310-01`）：二欄為實作級外審觸發之機械輸入，**MUST** 由開票方顯式填寫，**MUST NOT** 由票面內文推斷。無新導入能力者填 `NONE`；無依賴異動者填 `NONE`。**fail-closed**：欄位缺失、空值或格式非法者一律視為外審觸發，**MUST NOT** 靜默視為 `NONE`。`INTRODUCED_CAPABILITY` 之 canonical token 為字面檢索鍵，**MUST** 為穩定識別字（套件座標、skill 名、feature slug），**MUST NOT** 使用自然語言描述。

- [x] 完成（commit `8ba73b5`；置於 `**BASE_COMMIT／TARGET_BLOB 欄位說明**` 段後、`**MAX_LINES 量尺定義與性質**` 前）

### AI-4: Revision 歷史增列 v3.10 條目
- **檔案**: `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
- **現值**（唯一，定位錨點——文末 Revision 歷史章末筆）: `**Revision**: v3.9（2026-09-02，Akira 免票直接授權）`
- **目標值**: 於 v3.9 條目段落結束後新增一筆：

> **Revision**: v3.10（2026-09-08，INFRA-TPL-V310-01）— Verify Config 增列 `INTRODUCED_CAPABILITY` 與 `DEPENDENCY_DELTA` 二欄（實作級外審觸發之機械輸入），並立 fail-closed 判定：欄位缺失／空值／格式非法一律視為觸發，禁靜默視為 `NONE`。立法來源：`Instructions_for_Claude.md` v2.8.7「6. 實作級外審」a 款與 b③ 款；本票為該二款之生效前提（前置資源恆存律）。**forward-only**：既有在途票免補填。

- [x] 完成（commit `8ba73b5`；Revision 歷史含字面 `v3.10（2026-09-08，INFRA-TPL-V310-01）` 且含 `fail-closed`）

## Hard Constraints
1. 禁修改檔頭 `**Revision**: v3.7（2026-08-14，INFRA-TPL-V37-01）` 行——該行為歷史錨點，v3.8／v3.9 均未更動之現況屬既有帳實分離，本票**不追溯修正**（另立議題）。
2. 禁改動 AC 類型學、MAX_LINES 條款、依賴三分、票號議題前綴律、短檔名律等任何既有判準文字。
3. 二新欄位 **MUST** 置於 Verify Config 區塊內、`TARGET_BLOB` 行之後，禁另立平行區塊（禁雙軌）。
4. 禁於本票內同時實作 Instructions §6 之任何判定邏輯——本票僅供給欄位載體。

## Error Handling
- AI-0 任一現值命中數與票面不符 → 依 v3.8 基線漂移三分流分類停止回報。
- `**版本**: v3.9` 命中數 ≠ 1 → 停止回報（版本標記已再度分裂，超出本票 scope）。
- Verify Config 區塊已存在同名欄位（任一） → 停止回報（前次施作殘留，禁重複插入）。
- **[標配]** `git check-ignore -v plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md` 命中 → 停止回報（gitignore 死結律）。

## Acceptance Criteria
- [x] **[存在性]** `grep -c 'INTRODUCED_CAPABILITY' <TPL>` ≥ 2（Verify Config 樣板 1 處 + 欄位說明段 1 處）——實測 = 3
- [x] **[存在性]** `grep -c 'DEPENDENCY_DELTA' <TPL>` ≥ 2（同上）——實測 = 3
- [x] **[存在性]** `**版本**: v3.10` 命中 1 次，且 `**版本**: v3.9` 命中 0 次——實測相符
- [x] **[存在性]** Revision 歷史章含 `v3.10（2026-09-08，INFRA-TPL-V310-01）` 且字面含 `fail-closed`——實測命中
- [x] **[存在性]** 檔頭 `**Revision**: v3.7（2026-08-14，INFRA-TPL-V37-01）` 逐字保留（HC-1 反向驗證）——實測 = 1
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)——仍執行作基線 PASS（`assembleDebug` 12s、`testDeviceDebugUnitTest` 209/209、lint 0 new warnings）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案——`5504c35..8ba73b5` 僅 ALLOWED 單檔 + `.claude/PROGRESS.md`（Pipeline Artifact）

**AC 類型選用說明**（防「以存在性偽裝關閉」之審查質疑）：本票標的為模板文件之欄位載體增列，不引入 Guard／gate／early-return／外生 runtime state，`app/src/main/` 內無對應 producer，故 `[可達性]`／`[行為性]` 不適用。二欄位之**行為性**驗收落於其消費端（Instructions §6 判定），該端為 Claude 執行層而非 production code path，依 AC 類型學適用範圍不納本票。

## 結案授權（2026-09-08，Akira 直接裁決）

**Akira 裁決**：「本票是純治理, 跳過SV進行結案歸檔: 逐一勾選執行/AC核准項目＋票檔搬 `deliver/`＋`file` 欄及冊面更新狀態: REVIEW」

**覆寫範圍**：本裁決覆寫 `CLAUDE.md` SOP「本 repo 全程序執行，無 RAPID／快速通道；任何票均不得跳過 3./qa、4./sv、4.5.cross_review（Akira 2026-08-22 裁決）」條款，僅適用本票；不視為對該 SOP 條款之永久修訂（同 `INFRA-CTAWR-PHASE0-01` 2026-09-08 先例）。

**背景**：`/qa` 已 PASS（`assembleDebug`/`testDeviceDebugUnitTest`/`lint` 全綠，commit `8ba73b5`）；`/sv` 階段 A-C 已完成並記錄於 `sv_report.md`（Compile/Lint/Scope/16KB ELF/Orphan/Mock/Reachability 皆 PASS 或 N/A）。階段 D Cross-Review 尚未執行（R0 預審 subagent 於審查中途被 Akira 明示中止，未產出裁決，Codex 完全未觸發）。

**`reviews.codex` 填值**：`N_A`（未執行，非執行後 PASS；理由＝本裁決）。

**End of INFRA-TPL-V310-01**
