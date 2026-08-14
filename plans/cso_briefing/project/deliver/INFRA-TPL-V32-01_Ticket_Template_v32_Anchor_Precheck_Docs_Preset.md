# INFRA-TPL-V32-01: TICKET_TPL v3.2 — 錨點預檢律常設化 + MAX_LINES 推導註記強制欄 + PRESET 增 docs 型

**簽發日期**: 2026-07-31
**簽發者**: Claude (SMTR / CSO 主 session)
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴**: 無（版控前置 `06a377f` 已完成）

## 檔案治理／來源說明

本票為 `TKR02_Family_Ticketing_Audit_20260730.md` 桶 C 第 ⑨ 項，Akira 2026-07-31 核可批次優先序 ⑦→⑨→⑧→TKR-05→⑤⑥ 之第二張票。目的：將 TKR-02 家族踩出的三條治理律由「單票先例層」提升為「模板常設層」，消除學習曲線斷代風險。

**承接家族缺陷映射**（審查檔 §一）：
- **缺陷① 錨點/基線時效失準（3 起，最高頻：FIX-D 舊 DIFF_REF／FIX-G intake 分離／CLOSEOUT-01 全錨失效廢票）** → AI-2
- **缺陷④ 前提誤判開票（FIX-H）** → AI-2 涵蓋（開票前逐字取證即錨點預檢之上游動作）
- **缺陷② MAX_LINES 估值失準（2 起：FIX-B BLOAT +1495 攔截／FIX-F 127/120 超額放行）** → AI-3
- **⑦ deferred 承接：PRESET 枚舉缺 docs 型** → AI-4（Akira 2026-07-31 裁決 (a) 承接 ⑨ 並正式入票面）
- **缺陷③ Scope 劃界失準（FIX-F）** → **不承接**，依 `Traycer_T2_Global_Approval_And_Registry_Closeout` KD4 留 KAIROS 池待 ⑩ 收割輪（Akira 2026-07-31 裁決 2-a），禁繞道直增

**版控前置（已完成，非本票範圍）**：`06a377f`（2026-07-31 14:31:26，`[INFRA-TICKET-TPL-01]`）以例外強制追蹤將本模板納入版控，`new file mode 100644`、`@@ -0,0 +1,200 @@`、`.gitignore` 未變動。CSO 已獨立以 `git_show` + `git_status` 逐項複驗。**此前置解除了本票 `DIFF_REF` 與 `git diff --stat` AC 之前置資源缺口**（前置資源恆存律）。

**SSOT 形態（Akira 2026-07-31 裁決 1-c′）**：模板存兩份就近副本，明定 **app-repo 份（本票 ALLOWED）為唯一 SSOT**（受版控、可 diff、可 revert）；`D:/DevSources/Smarter-Project/file/Unified_Ticket_Template_v3.md` 降為**衍生副本**。該 root 非 git repo（`git_status` 回 `POLICY_DENIED: repo_path must be an independent Git repository`），故其路徑**無法以 ALLOWED 表達**（ALLOWED 係 verify_checklist.sh 之 git scope 建構）。副本同步改列為下方「收尾義務」專節並附獨立 AC。**此為對「新增條款 scope 一致性檢查」之顯式聲明例外，非疏漏**——依「豁免停點不豁免可見性」，於此明文揭露而不靜默夾帶。

**錨點預檢已於派工前執行**（派工前錨點預檢律，`Dispatch_Anchor_Precheck_Doctrine` KD2）：下列全部現值字串取自 2026-07-31 對 app-repo 份之 `grep_files` 直查，經逐一確認命中數；行號為當時實測值，僅供定位，比對以字串為準。

## Verify Config
TICKET_ID=INFRA-TPL-V32-01
CREATED=2026-07-31
ANCHOR_PROBED=2026-07-31 14:41
PRESET=docs
ALLOWED=plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md
FORBIDDEN=.gitignore,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=60
DIFF_REF=06a377f

**MAX_LINES 推導註記**（額度制截斷序律）：AI-1 淨增 1／改 4；AI-2 淨增 9（ANCHOR_PROBED 1 + AI-0 文塊 8）；AI-3 淨增 2；AI-4 淨增 5／改 1；AI-5 淨增 2／改 5。合計淨增 ~19、改 ~10 = 觸及 29 行。取 2× 緩衝並無條件進位至十位 → **60**。**截斷序**：若實作超額，MUST 停止回報；禁為壓行數而省略 AI-5 之範例同步（該項正是修訂波及面全檔掃描律之落地，省略即重蹈 Rev C.1 覆轍）。

**本票自身即 `docs` preset 之首個 dogfood 案例**（PRESET=docs 於 AI-4 落地後回溯自洽）。

## Action Items

> **AI 序律**：AI-1 → AI-5 依序執行。AI-2/3/4 之錨點互不重疊（經預檢確認），但仍 MUST 序列執行以確保行號回報之確定性。

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
- **動作**: 逐字確認 AI-1～AI-5 之各「現值」字串於檔內命中數與本票所載**完全相符**（標示「唯一」者恰 1 次）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成

### AI-1: 版本標示全檔一致化（v3.1 → v3.2）
> 修訂波及面全檔掃描律：本檔版本字串共 **4 處**，經 grep 全檔清點，**MUST 逐一改正**。現況 L200 已與 L3 不一致（v3.0 vs v3.1），為 v3.1 修訂時之漏改，本票一併清償。

- **檔案**: 同上
- **現值 (a)**（唯一，L1）: `# 統一 Ticket 模板 v3.1（CSO + Pipeline v1.6 合併版）`
- **目標值 (a)**: `# 統一 Ticket 模板 v3.2（CSO + Pipeline v1.6 合併版）`
- **現值 (b)**（唯一，L3）: `**版本**: v3.1`
- **目標值 (b)**: `**版本**: v3.2`
- **現值 (c)**（唯一，L5，僅於行尾追加新行，原行不動）:
```
**Revision**: v3.1（2026-07-06，INFRA-REACH-02 AI-1）— Acceptance Criteria 增列 AC 類型學（`[存在性]`/`[可達性]`/`[行為性]`/`[實機]`），對治 K4-50「AC 語義折損律」
```
- **目標值 (c)**: 上行原樣保留，其下新增一行：
```
**Revision**: v3.2（2026-07-31，INFRA-TPL-V32-01）— ①錨點預檢律常設化（`ANCHOR_PROBED` 欄 + AI-0 標準文塊）；②MAX_LINES 推導註記強制欄；③PRESET 增 `docs` 型並明定固定 AC 適用性；④範例 Ticket 同步 AC 類型學。依據 `Dispatch_Anchor_Precheck_Doctrine`（KD2）與 TKR-02 家族審查
```
- **現值 (d)**（唯一，L200）: `**End of 統一 Ticket 模板 v3.0**`
- **目標值 (d)**: `**End of 統一 Ticket 模板 v3.2**`
- [x] 完成

### AI-2: 錨點預檢律常設化
- **檔案**: 同上
- **現值 (a)**（唯一，L31，模板 Verify Config 內）: `DIFF_REF=[起始 commit hash]`
- **目標值 (a)**:
```
DIFF_REF=[起始 commit hash]
ANCHOR_PROBED=[YYYY-MM-DD hh:mm]
```
- **現值 (b)**（唯一，L35）: `### AI-1: [動作描述]`
- **目標值 (b)**: 於該行**之前**插入下列文塊（原行保留）：
```
> **AI-0 為強制首項**（派工前錨點預檢律，`Dispatch_Anchor_Precheck_Doctrine` KD2）。開票方 MUST 於草擬同刻以 live `read_file_safe`／`grep_files` 直查取樣全部「現值」錨點並填寫 `ANCHOR_PROBED` 時戳；**禁以記憶、前次 session 輸出或執行者回報充當錨點**。派工時若目標檔已有新 commit 越過 `ANCHOR_PROBED` 時刻，MUST 重跑預檢方可派工。

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `path/to/File.kt`
- **動作**: 逐字確認各 AI 之「現值」字串於檔內命中數與票面所載相符（標示「唯一」者恰 1 次）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [ ] 完成

```
- [x] 完成

### AI-3: MAX_LINES 推導註記強制欄
- **檔案**: 同上
- **現值**（唯一，L30，模板 Verify Config 內）: `MAX_LINES=[數字]`
- **目標值**: 該行原樣保留，於 Verify Config 區塊**結束後**（即 `ANCHOR_PROBED` 行之下、`## Action Items` 之上）新增：
```

**MAX_LINES 推導註記**（額度制截斷序律，**強制欄；無此註記之票視同格式不合格**）：[逐 AI 行數分項] → [緩衝倍率] → [進位結果]。**截斷序**：[實作超額時之停止規則]。
```
- [x] 完成

### AI-4: PRESET 增 `docs` 型（⑦ deferred 正式承接）
- **檔案**: 同上
- **現值 (a)**（唯一，L27）: `PRESET=[feature|bugfix|refactor|hotfix]`
- **目標值 (a)**: `PRESET=[feature|bugfix|refactor|hotfix|docs]`
- **現值 (b)**（唯一，L105，Preset 快查表末列）: `| hotfix | 1-2 AI-N | 省略 | 50 | 緊急：crash 修復 |`
- **目標值 (b)**: 該行原樣保留，其下新增：
```
| docs | 完整 AI-N（行級替換，現值/目標值逐字） | **嚴格列舉** | 30-80 | 契約增補、治理檔同步、歸檔建檔、模板修訂 |
```
- **現值 (c)**（唯一，L70，欄位說明表）:
```
| **Acceptance Criteria** | 人類 + 機械雙重驗收 | Akira + verify_checklist.sh | 最後兩項固定（assembleDebug + diff stat） |
```
- **目標值 (c)**: 該行原樣保留，於 Preset 快查表（AI-4b 新增列）**之下**新增：
```

**固定 AC 之 preset 適用性**：`assembleDebug PASS` 對 `docs` 型 **N/A**（純文檔票無編譯產物，強制保留即製造不可能滿足之 AC——FIX-F traycer=FAIL 永久留帳之制度性來源）；`git diff --stat 僅涉及 ALLOWED 檔案` 對**所有** preset **保留強制**。`docs` 型票 MUST 於 AC 區以 `~~assembleDebug PASS~~ (N/A: docs preset)` 顯式劃記，禁靜默刪除（豁免停點不豁免可見性）。
```
- [x] 完成

### AI-5: 範例 Ticket 同步（修訂波及面全檔掃描律）
> 範例 Ticket 為執行端實際 pattern-match 對象。v3.1 增列 AC 類型學時**未同步範例**（L158-162 五條 AC 全無類型標記），屬既存漏改，本票一併清償。

- **檔案**: 同上
- **現值 (a)**（唯一，L127）: `DIFF_REF=fccd5cf`
- **目標值 (a)**:
```
DIFF_REF=fccd5cf
ANCHOR_PROBED=2026-05-29 10:00
```
- **現值 (b)**（唯一，範例 AC 區前三條）:
```
- [ ] Logcat 不再出現 `IllegalArgumentException: None of the specified AF/AE/AWB MeteringPoints`
- [ ] gazeScore=0.0 的 trigger 被攔截（Logcat 確認）
- [ ] bbox.left < 0 的 face 被過濾（Logcat 確認）
```
- **目標值 (b)**:
```
- [ ] **[實機]** Logcat 不再出現 `IllegalArgumentException: None of the specified AF/AE/AWB MeteringPoints`
- [ ] **[行為性]** gazeScore=0.0 的 trigger 被攔截（Logcat 確認）
- [ ] **[行為性]** bbox.left < 0 的 face 被過濾（Logcat 確認）
```
- [x] 完成

## Hard Constraints
1. **禁新增任何檔案**（Akira 2026-07-31 裁決 2-a）：`.gitignore:76` `plans/cso_briefing/project/*` 仍覆蓋本目錄，`06a377f` 係例外強制追蹤單檔；同目錄**新增檔案將靜默維持 untracked**（`git status` 不報、`git add` 不收，與「hook 置錯 repo 靜默 exit 0」同型失效）。本票僅得就地行級替換。
2. **禁改 `.gitignore`**（屬 app 建置檔，與模板檔異質，違手術刀不混提；根治另循 2-c 獨立 INFRA 票）。
3. 禁修訂 AC 類型學四型之定義本體（`[存在性]`/`[可達性]`/`[行為性]`/`[實機]`）——本票僅增 preset 適用性宣告，不動類型學。
4. 禁新增 scope 劃界相關條款（Akira 裁決 2-a：留 KAIROS 池，禁繞道直增；違反即重蹈 T2 KD4 所禁之通道繞行）。
5. 禁改 `MAX_LINES` 既有四型 preset 之數值（feature 80-150／bugfix 100／refactor 300／hotfix 50）——無裁決依據。
6. AI-2b 之 AI-0 文塊 MUST 置於**模板** Action Items 區（L33-35 段），**非**範例 Ticket 之 Action Items（L129 段）——兩處同名，錯置即條款落在範例內失去常設效力。

## Error Handling
- AI-0 任一現值命中數與票面所載不符 → 停止回報
- `## Action Items` 於檔內命中數 ≠ 2（模板 + 範例）→ 停止回報（表示結構已變，AI-2b 錨點語境失效）
- 修訂過程需**新增檔案**方能完成 → 停止回報（HC-1）
- 實作行數超出 MAX_LINES=60 → 停止回報，禁省略 AI-5
- `Smarter-Project` 副本路徑不可寫或內容已與 app-repo 份分歧 → 停止回報，禁逕以覆蓋消解分歧

## Acceptance Criteria
- [x] ~~**[存在性]** `grep -c 'v3\.1' <檔>` = 1（僅 L5 舊 Revision 行保留歷史記載）~~ → **勘誤 Addendum-1：`grep -c 'v3\.1' <檔>` = 2**（L5 舊 Revision 行 + L84「AC 類型學（v3.1，INFRA-REACH-02 AI-1）」標題；二者均為合法沿革歸屬記載，**MUST NOT 改為 v3.2**——類型學確係 v3.1 引入，改之即偽造沿革）；`grep -c 'v3\.2' <檔>` = 4（標題／版本／新 Revision／End 行，本項原判準無誤）
- [x] **[存在性]** `grep -c 'v3\.0' <檔>` = 0（L200 漏改已清償）
- [x] ~~**[存在性]** `ANCHOR_PROBED` 命中 = 2（模板 Verify Config + 範例 Verify Config）~~ → **勘誤 Addendum-2：`ANCHOR_PROBED` 命中 = 4 行 / 5 次**——L33 模板 Verify Config（1）、L143 範例 Verify Config（1）、L6 新 Revision 行（1；AI-1(c) 逐字指定文本自帶詞面）、L39 AI-0 doctrine 段（2；AI-2(b) 逐字指定文本自帶詞面）。行數計法採 **4**、次數計法採 **5**，擇一報回並註明計法即可
- [x] **[存在性]** `### AI-0: 錨點復驗` 命中 = 1，且位於**模板**區（其後首個 `### AI-1:` 為 `[動作描述]` 佔位版，非 `EyeAfController`）
- [x] **[存在性]** `MAX_LINES 推導註記` 命中 ≥ 1 且含「強制欄」與「截斷序」二詞
- [x] **[存在性]** `PRESET=[feature|bugfix|refactor|hotfix|docs]` 命中 = 1；Preset 快查表列數 = 5
- [x] **[存在性]** `固定 AC 之 preset 適用性` 命中 = 1 且含 `N/A: docs preset` 字樣
- [x] ~~**[行為性]** 範例 Ticket 五條 AC 全數帶類型標記：`grep -c '\[實機\]\|\[行為性\]\|\[存在性\]\|\[可達性\]'` 於 L155-175 區間 ≥ 3（三條 Logcat AC）~~ → **勘誤 Addendum-3：改採行號無關判準**——AI-2(b) 之 AI-0 文塊插入使範例 AC 區下移至 L174-176，原 L155-175 區間必漏第三條。新判準：`grep -n '\[實機\]\|\[行為性\]' <檔>` MUST 命中「Logcat 不再出現」（`[實機]`）、「gazeScore=0.0」（`[行為性]`）、「bbox.left < 0」（`[行為性]`）三行且行號連續。**類型標記併正為 `[存在性]`**——本 AC 實為 grep 存在檢查而非 production 路徑驅動，原標 `[行為性]` 屬類型誤標（不觸 HC-3：僅改本票 AC 之自身標記，未動模板類型學定義本體）
- [x] **[存在性]** 檔案總行數介於 200～260（MAX_LINES=60 上界自洽）
- [x] ~~`assembleDebug` PASS~~ **(N/A: docs preset — 本票即 AI-4c 條款之首個適用案例)**
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

## Addendum-A：AC 勘誤（2026-07-31，Akira L2 裁決核可）

> **本節為勘誤（erratum），非 AC 放寬。** 依「票依簽發時法結案、禁事後修訂 AC」之禁令射程——該禁令對治的是**事後放寬驗收要求**；本案 AI-1～AI-5 之操作性規格已由執行端逐字落地且經 AI-0 預檢全數相符，僅 AC 之**算術編碼**與票面自身 AI 文本不自洽。修正 AC 數值使其符合票面 AI 之字面，屬勘誤而非要求變更。原判準以刪除線保留、勘誤值並列，禁靜默替換（豁免停點不豁免可見性）。

**缺陷歸因（CSO 自陳，非執行偏差）**：

| # | 缺陷 | 根因 | 執行端處置 |
|---|------|------|-----------|
| 1 | AC「`v3.1` = 1」實測 = 2 | CSO 開票時之派工前預檢 grep **已顯示** L72（今 L84）含 `v3.1`，仍誤寫 = 1。AI-1 宣稱「版本字串共 4 處」亦漏列該處 | 停止回報 ✅ 正確 |
| 2 | AC「`ANCHOR_PROBED` = 2」實測 = 4 行 / 5 次 | AC 撰寫時以「套用前檔案」為計數基數，未計入 AI-1(c) / AI-2(b) **目標值文本自身**所帶之詞面（自指盲區） | 停止回報 ✅ 正確 |
| 3 | AC-8「L155-175 區間」必漏第三條 | 同上自指盲區之衍生：AC 以套用前行號編碼，未投影 AI-2(b) 插入之 +9 行位移。**本項由 CSO 於勘誤時補行取證發現，執行端尚未回報** | — |

**否決之替代方案 (b)**（額外授權 AI-1 擴及 L84、或調整 AI-1(c)/AI-2(b) 措辭以消除詞面重複）：
- 改 L84 為 v3.2 即**偽造沿革**（AC 類型學確係 v3.1 之 INFRA-REACH-02 引入）
- 為迴避計數而修改 doctrine 段措辭，係讓正確內容遷就錯誤 AC，屬內容降級
- 二者皆令檔案遷就 AC，方向顛倒

**KAIROS 候選（已入 `2026-07-31_Ticket_Template_v32_Batch_Rulings` 教訓收割表 #3）**：**AC 投影計數律**——凡 grep 型 AC，其期望值 MUST 以「套用後投影檔」為基數計算（現況命中數 ± 各 AI 之增刪，**含票面目標值文本自身所帶之詞面**），並禁以套用前行號編碼區間判準。

**執行端後續**：本節生效後，AC 勘誤三項改依勘誤值驗收；其餘 AC（v3.2=4、v3.0=0、AI-0=1、MAX_LINES 推導註記、PRESET 枚舉/快查表 5 列、固定 AC 適用性、總行數 200-260、`assembleDebug` N/A 劃記、`git diff --stat`）判準不變，續行 QA → Codex → commit → 下方收尾義務。

## 收尾義務（Closeout Obligation，非 Action Item）

> **範圍外聲明**：下列動作之目標位於 `Smarter-Project` root，該 root 非 git repo，**不在本票 ALLOWED 亦不可能在**（ALLOWED 為 git scope 建構）。依 Akira 2026-07-31 裁決 1-c′ 列為收尾義務並附獨立 AC；此為對「新增條款 scope 一致性檢查」之顯式聲明例外，已於本節明文揭露。

- [x] 以 `write_file_safe mode=patch` 將 AI-1～AI-5 之同一組 search/replace 套用至衍生副本 `D:/DevSources/Smarter-Project/file/Unified_Ticket_Template_v3.md`。**禁以 copy 整檔覆蓋**（無 diff 取證，且會抹除該檔可能存在之未知分歧）。（以逐段 Edit 精確 search/replace 套用，非全檔覆寫；套用前已確認該副本現值與 app-repo 份套用前狀態逐字相符）
- [x] **[存在性] 同體性驗收**：對兩檔以同一 `grep_files` regex `v3\.2|ANCHOR_PROBED|docs \||MAX_LINES 推導註記|### AI-0` 取樣，**兩檔命中數逐項相等**。不等 → 停止回報，禁單方修正。（實測：AI-0=1/1、ANCHOR_PROBED=5/5、MAX_LINES 推導註記=2/2、docs \|=1/1、v3.2=4/4，逐項相等）
- [x] commit 前綴 MUST 為 `[INFRA-TPL-V32-01]`（本票動 app-repo 之治理文檔，非 Governance Artifact 檔類，禁用 `GOVERNANCE-SYNC` / `COMPACT-SYNC`）。（app-repo 份 commit `d6a75f8`、`6a8f508` 皆為 `[INFRA-TPL-V32-01]` 前綴；Smarter-Project 副本非 git repo，無 commit 動作）

## 待後續承接（deferred，本票明列不做）
1. **`.gitignore` 同目錄盲區根治**：`!plans/cso_briefing/project/file/` 白名單（Akira 裁決 2-c，另開獨立 INFRA 票）。在此之前，本目錄新增檔案一律靜默 untracked。
2. **`.gitignore` 行號錯記勘誤**：SMTR 側常駐記載為「`:71` 排除整個 `plans/cso_briefing/`」，實測為 **L74-77 四行反選集**且 `TICKET_REGISTRY.json` 有白名單。`06a377f` commit message 已正確記為 `:76`，惟 SMTR 常駐文本待同步——**本項無承接載體，MUST 於 ⑩ KAIROS 收割輪或 SMTR_INDEX 修訂時清償**（基礎設施異動即檔律）。
3. **Scope 劃界失準（缺陷③）條款**：留 KAIROS 池待 ⑩ 收割輪（裁決 2-a）。
