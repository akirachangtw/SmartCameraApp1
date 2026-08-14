# INFRA-TPL-V33-01: TICKET_TPL v3.3 — 序號機械取號律 + gitignore 死結預檢標配

**簽發日期**: 2026-08-01
**簽發者**: Claude (SMTR/CSO)
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: 無（裁決來源 `KAIROS_CaseC_Harvest_Round_Merged_Laws` 已落檔）

## 檔案治理/來源說明

本票為 KAIROS C 案收割輪（vault `2026-08-01_KAIROS_CaseC_Harvest_Round_Merged_Laws.md`）之執行者面部署票（票甲），承載律 A（序號機械取號律，合併 K4-45+K4-49）與候選 #20（gitignore 死結律）。SSOT = 上述 vault 決策檔；本票僅寫入摘錄條文，條文語意歧義一律回 SSOT 裁決。

**⚠️ 目標檔治理特性（揭露，非豁免）**：`D:/DevSources/Smarter-Project/` 經 `git_status` 實測回 `POLICY_DENIED: repo_path must be an independent Git repository`，即目標檔**不在任何 git repo 內**。故本票無 `DIFF_REF`，且固定 AC `git diff --stat` 不可執行。依「豁免停點不豁免可見性」原則於 AC 區顯式劃記，禁靜默刪除。替代驗收為檔案級 grep 正向取證（AC-4）。

## Verify Config
TICKET_ID=INFRA-TPL-V33-01
CREATED=2026-08-01
PRESET=docs
ALLOWED=D:/DevSources/Smarter-Project/file/Unified_Ticket_Template_v3.md
FORBIDDEN=D:/DevSources/AndroidProj/SmartCameraApp1/**（本票零觸碰 app repo）
MAX_LINES=30
DIFF_REF=N/A（目標檔不在 git repo 內，見上方檔案治理說明）
ANCHOR_PROBED=2026-08-01 18:05

**MAX_LINES 推導註記**（額度制截斷序律）：AI-1 版本行替換 1 行 → AI-2 取號律文塊 4 行 → AI-3 Error Handling 標配 2 行 → 小計 7 行 × 2.5 緩衝 = 17.5 → 進位 **30**。**截斷序**：實作超額時，依 AI-1 → AI-2 → AI-3 順序保留，超額者停止並回報，禁自行壓縮條文字數以求達標。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `D:/DevSources/Smarter-Project/file/Unified_Ticket_Template_v3.md`
- **動作**: 逐字確認 AI-1～AI-3 之「現值」字串於檔內命中數與票面所載相符（標示「唯一」者恰 1 次）。
- **特別注意**: 本檔含「模板區 + 範例 Ticket 區」雙份結構，單行錨點易非唯一；AI-3 採**多行複合錨點**確保唯一性，MUST 逐字（含空行）比對。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成

### AI-1: Revision 行追加 v3.3
- **檔案**: 同上
- **現值**（唯一，L6 起）: `**Revision**: v3.2（2026-07-31，INFRA-TPL-V32-01）—`
- **目標值**: 於該 Revision 行**所在行之後**新增一行（不刪改 v3.2 行）：
```
**Revision**: v3.3（2026-08-01，INFRA-TPL-V33-01）— ①序號機械取號律（開票前置檢核）；②gitignore 死結預檢納入 Error Handling 標配。依據 `KAIROS_CaseC_Harvest_Round_Merged_Laws`（KD-2/KD-3）
```
- [x] 完成

### AI-2: 序號機械取號律文塊（AI-0 強制首項註記之後）
- **檔案**: 同上
- **現值**（唯一）: `派工時若目標檔已有新 commit 越過 `ANCHOR_PROBED` 時刻，MUST 重跑預檢方可派工。`
- **目標值**: 於該行之後追加空行與下列文塊（原行保留不動）：
```
> **序號機械取號律**（律 A，`KAIROS_CaseC_Harvest_Round_Merged_Laws` KD-2/KD-3）。指派任何票號／候選序號前，開票方 MUST 以**全冊機械取號**決定新號：對 registry 全檔（per-project `TICKET_REGISTRY.json`）與 `deliver/`、`reports/` 執行 ID 樣式 grep 取 max-ID，`max+1` 為新號。**禁只掃 `Pending/`**（已通過票離開 Pending/，只掃必碰撞）；**禁以 `tail`／局部讀取驗證「目前最大號」**（局部讀取必然漏看他處已登記條目）。二次實證：PROTO-01 票號碰撞、K4-46 雙登。
```
- [x] 完成

### AI-3: Error Handling 標配項（模板區，非範例區）
- **檔案**: 同上
- **現值**（**多行複合錨點**，唯一；範例區之 Error Handling 內容不同故不衝突）:
```
## Error Handling
- [條件] → 停止回報
- [條件] → 停止回報

## Acceptance Criteria
```
- **目標值**（於複合錨點內、`## Acceptance Criteria` 之前追加一行標配項，其餘各行逐字不動）:
```
## Error Handling
- [條件] → 停止回報
- [條件] → 停止回報
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）

## Acceptance Criteria
```
- [x] 完成

## Hard Constraints
1. 純追加／行級插入；**禁刪除或改寫任何既有行**（v3.2 Revision 行 MUST 原樣保留）。
2. 禁觸碰範例 Ticket 區（L120 之後）之任何內容，含其 Error Handling 段。
3. 條文逐字採用票面「目標值」，禁改寫、禁精簡、禁補充未載明之語句。
4. 禁夾帶他檔異動（本票 ALLOWED 為單一檔案）。

## Error Handling
- 任一「現值」錨點命中數 ≠ 1（尤其 AI-3 複合錨點）→ 停止回報，禁改用單行錨點替代
- `write_file_safe mode=patch` 回非唯一匹配拒改 → 停止回報，禁改 overwrite／禁重謄全檔
- 檔案 `total_chars` 與 7085 顯著不符（他人已改動）→ 停止回報，回請 CSO 重跑錨點預檢
- **[標配]** 令含 commit／版控標的時 `git check-ignore -v` 命中 → 停止回報（本票 N/A：目標檔不在 repo 內）

## Acceptance Criteria
- [x] **[存在性]** `grep -c "v3.3（2026-08-01，INFRA-TPL-V33-01）"` = 1，且 `grep -c "v3.2（2026-07-31，INFRA-TPL-V32-01）"` 仍 = 1（既有行未遭覆寫）
- [x] **[存在性]** `grep -c "序號機械取號律"` = 1；文塊內「禁只掃」「禁以 `tail`」「max+1」三字面均命中
- [x] **[存在性]** `grep -c "git check-ignore -v <標的路徑>"` = 1，且該行位於**模板區** Error Handling 段（行號 < 範例區 Error Handling 行號）
- [ ] **[行為性]** 檔案總行數增量 ∈ [5, 30]，且以 diff 方式確認**零刪除、零既有行改動**（無 git 可用時，以修改前 `get_file_info` size 與修改後比對 + 上述三條 grep 反證既有內容完好）— **佐證不明保留**：diff 已核驗零刪除/零既有行改動為真，但精確增量實測為 **4 行**，未落入 [5,30] 下界，待 CSO/Akira 裁決是否豁免推導誤差後方可勾選（SSOT Governance「Ticket Closure 紀律」：佐證不明禁推定勾選）
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] ~~`git diff --stat` 僅涉及 ALLOWED 檔案~~ (N/A: 目標檔不在任何 git repo 內，經 `git_status` 實測 `POLICY_DENIED` 取證；替代驗收見 AC-4。**此為揭露式劃記，非靜默刪除**)

## 完成後動作
- 完成後回報，停止，**不自行轉冊面狀態、不搬移票檔**（Pending→deliver 由 CSO 收束時執行）。


---
**冊面收束尾註(CSO,2026-08-01)**:全 AI 收束。**AC-4 偏差裁決**:實測增量 4 行 < 票面下界 5——判定為票面 MAX_LINES 推導誤差(自估 7 行,高估方向,無 scope 風險);實質驗收(零刪除、三內容 grep 命中、v3.2 Revision 行原樣保留)經 CSO 獨立取證全數成立,**視同 PASS**,依簽發時法不追溯修改 AC 字面。另:Akira 裁決本檔版控採**雙載體制**——app-repo 份為唯一 SSOT(受版控),本檔(Smarter-Project 份)降為衍生副本;實施另案執行。偏差事件收割為候選 #25(AC 驗收字串/數值與 AI 目標值同源律),登記於 SSOT Addendum-B。
