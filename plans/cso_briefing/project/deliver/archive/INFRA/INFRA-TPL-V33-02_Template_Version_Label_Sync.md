# INFRA-TPL-V33-02: TICKET_TPL 版本標記三處同步至 v3.3（標頭／版本欄／檔尾）

**簽發日期**: 2026-08-02
**簽發者**: Claude (SMTR/CSO)
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: 無（與 TKR-05-A 並行，無交互依賴）

## 檔案治理／來源說明

**缺陷來源**：`INFRA-TPL-V33-01`（已 CLOSED）執行時僅於 Revision 區增列 v3.3 條目，未同批同步標頭、版本欄與檔尾，形成版本標記帳實分離——與 Rev C.1（L31 已修、L52/56 漏改）同型之「修訂波及面全檔掃描律」違反。
**取證來源**：`D:/DevSources/Smarter-Project/file/TKR-05_tool_optimization_spec.md` Addendum-4 §14 F-1、§15 Q5 裁決 5-a、§19 教訓 L-3。
**雙載體版控**：SSOT = app repo `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`（受版控）；`Smarter-Project/file/` 份為衍生副本（**非 git repo，無 commit 義務**，同步為收束附帶義務）。2026-08-02 17:00 實測兩份 size 均為 11523 bytes 且缺陷字面一致。

## Verify Config
TICKET_ID=INFRA-TPL-V33-02
CREATED=2026-08-02
PRESET=docs
ALLOWED=plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md
FORBIDDEN=plans/cso_briefing/project/TICKET_REGISTRY.json, .gitignore, app/
MAX_LINES=30
DIFF_REF=3909824
ANCHOR_PROBED=2026-08-02 17:00

**MAX_LINES 推導註記**（額度制截斷序律，強制欄）：AI-1 SSOT 三行替換 3｜AI-2 衍生副本三行替換 3 → 分項合計 6 → 緩衝倍率 1.5 → 9 → **進位至 `docs` preset 下限 30**（採 preset 下限而非計算值，避免對純標籤替換設定不合理之緊繃額度）。
**截斷序**：本票無可截斷分項；任一 AI 之替換無法逐字命中即**停止回報**，禁改以模糊匹配或全檔重寫。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
- **動作**: 以 `grep_files` regex `v3\.2` 對該檔取證，確認命中**恰 4 處**且行號為 L1 / L3 / L6 / L221；同時對 `D:/DevSources/Smarter-Project/file/Unified_Ticket_Template_v3.md` 取證，確認命中亦為同 4 處同行號。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成 —— SSOT 側 live grep 取證屬實（4 處 L1/L3/L6/L221）；衍生副本側**未取證**（標的於 2026-08-13 前已刻意刪除，Akira 確認 TPL 收斂單一載體），見 Addendum-B

### AI-1: SSOT 三處版本標記替換
- **檔案**: `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
- **現值 / 目標值**（逐字，各為唯一匹配）：
  1. L1 `# 統一 Ticket 模板 v3.2（CSO + Pipeline v1.6 合併版）` → `# 統一 Ticket 模板 v3.3（CSO + Pipeline v1.6 合併版）`
  2. L3 `**版本**: v3.2` → `**版本**: v3.3`
  3. L221 `**End of 統一 Ticket 模板 v3.2**` → `**End of 統一 Ticket 模板 v3.3**`
- **絕對約束**: **L6 之 `**Revision**: v3.2（2026-07-31，INFRA-TPL-V32-01）…` 為歷史修訂紀錄，MUST 原樣保留，禁改為 v3.3。** 誤改即銷毀版本沿革。
- [x] 完成 —— 替代手段達成，見文末 Addendum-B：#2（L3）逐字達成（`f1dbdc1`）；#1/#3（L1／檔尾）改以**移除版本號**達成同一立法目的（`38b1bd3`），非逐字替換

### AI-2: 衍生副本同步（版控外，收束附帶義務）
- **檔案**: `D:/DevSources/Smarter-Project/file/Unified_Ticket_Template_v3.md`
- **目標值**: 套用與 AI-1 完全相同之三處替換，同受 L6 保留約束
- **註**: 該路徑不在 app repo 版控範圍內，**不計入 `git diff --stat` AC**；其同步狀態由本票 AC 之獨立 grep 條目驗證
- [ ] 完成 —— **MOOT（標的退場，保留未勾）**：`Smarter-Project/file/Unified_Ticket_Template_v3.md` 已不存在（2026-08-13 實測目錄零命中），Akira 確認為刻意退場、TPL 收斂為 app-repo 單一載體。本 AI 已無可執行標的，依「禁推定勾選」保留未勾並於 Addendum-B 揭露

## Hard Constraints
1. 禁止變更 TPL 之任何實質條文內容（本票僅處理版本標籤字面，不涉模板語義）
2. 禁止新增 Revision 條目（v3.3 條目已存在，本票非新版次）
3. 禁止以全檔覆寫方式套用（全記錄替換型寫入禁令；MUST 走字元級 patch）
4. 禁止順手修正該檔其他觀察到的瑕疵（scope 一致性；另立票）

## Error Handling
- AI-0 命中數或行號與票面不符 → 停止回報
- 任一「現值」字串非唯一匹配 → 停止回報
- 替換後 L6 之 Revision 歷史條目字面發生任何變化 → **立即停止並回報**
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）
  ⚠️ **本票高風險項**：`.gitignore` 曾排除整個 `plans/cso_briefing/` 目錄；現行 TPL SSOT 為 tracked，但 MUST 於異動前實跑 `git check-ignore -v` 驗證，禁以本註記代替實測

## Acceptance Criteria
- [ ] **[存在性]** SSOT 檔全檔 regex `v3\.2` 命中數 **恰為 1**，且該筆位於 Revision 區（L6，歷史條目）
      — **保留未勾**：實測 **3**（L6 歷史條目 ＋ L9 v3.5 Revision 之引述 ＋ L52 推導註記之引述）。超出 2 筆均為**散文引述**而非過期標籤，立法目的（消除誤導性版本標籤）未受損，惟字面判準不成立
- [ ] **[存在性]** SSOT 檔 L1、L3、檔尾三處各含 `v3.3` 字面（grep `v3\.3` 命中數 ≥ 4：三處新值 + L7 既有 Revision 條目）
      — **保留未勾**：實測 `v3.3` 命中 **1**；L1 與檔尾已**無版本號**（`38b1bd3` 刻意移除，收斂單一來源），L3 為 `v3.5`（`94a2108`／`f51344d` 兩次後續授權修訂）。判準因情勢變更成為**不可能滿足**
- [ ] **[存在性]** 衍生副本 `Smarter-Project/file/Unified_Ticket_Template_v3.md` 滿足與上兩條完全相同之命中條件
      — **保留未勾（標的退場）**：檔案已不存在，判準無驗收對象
- [x] **[存在性]** 兩檔 L6 之 Revision v3.2 歷史條目字面完整保留（逐字比對）
      — SSOT 側 L6 逐字未動（`git diff` 全期零觸），絕對約束遵守；衍生副本側因標的退場不適用
- [x] ~~`assembleDebug` PASS~~ **(N/A: docs preset，純文檔票無編譯產物。依「豁免停點不豁免可見性」顯式劃記，禁靜默刪除)**
- [ ] `git diff --stat`（app repo，基準 `3909824`）僅涉及 ALLOWED 檔案，即單一檔 `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`
      — **保留未勾**：自 `3909824` 起之累積 diff 另含 `.claude/rules/` 三檔與 `TICKET_REGISTRY.json`（分屬 `INFRA-RULES-CARRIER-01` 與 `[GOVERNANCE-SYNC]`）。**逐 commit 檢視**則本票各 commit 均僅動 ALLOWED 單檔；判準之「累積基準」設定使其對長跨度票不可滿足

---

## Addendum-B：收尾偏差揭露（2026-08-13）

**性質**：本節為 erratum + 情勢變更揭露，**非** AC 事後放寬。原判準全數以原文保留，逐項附實測值並列（豁免停點不豁免可見性）。

**立法目的達成度**：票面問題陳述為「標頭 v3.2 字樣對每個後續開票 session 均構成誤導源」。現況實測 SSOT 檔標頭與檔尾**已無任何版本號**，版本號單一來源收斂至 L3 `**版本**:` 欄——誤導源已根除，且較票面原設計（三處同步為 v3.3）**更強**：同步制每次改版需維護三處、必然再度漂移；移除制結構上不可能漂移。

**四項偏差歸因**：
1. **AI-1 #1/#3 與 AC-2**：以「移除」替代「同步為 v3.3」。屬執行期判斷（`38b1bd3`，Akira 核可提交），非票面違反；然票面未預留替代手段條款，字面判準因此不成立。
2. **AC-1**：判準以「全檔 `v3\.2` 命中恰 1」編碼「無過期標籤」語義，未計後續 Revision 散文引述歷史版號之合法情形——與 `INFRA-TPL-V32-01` Addendum-A 之「AC 算術未投影後續增刪」**同型第三例**。
3. **AI-2／AC-3**：標的（衍生副本）於本票簽發後退場，屬前提消滅型不可執行。
4. **AC-6**：判準採「累積基準 `3909824`」而非「逐 commit scope」，跨多票時段後必然混入他票 diff。

**⚠ 未解治理債（closure 不涵蓋、不予追認）**：`f1dbdc1`／`16ec953`／`38b1bd3`／`94a2108`／`f51344d` 五筆 commit 均掛 `[INFRA-TPL-V33-02]` 前綴，其中 `94a2108`（v3.4 依賴三分）與 `f51344d`（v3.5 MAX_LINES 退役）之工作內容**完全在本票 scope 之外**，另 `f1dbdc1`／`16ec953`（來源議題欄）亦非本票 AI 所載。本票 closure **MUST NOT** 解為對該五筆 commit 票號歸屬之追認；票號正名待 CSO 裁決（已於對話層三度提出未獲裁決）。

**End of INFRA-TPL-V33-02**
