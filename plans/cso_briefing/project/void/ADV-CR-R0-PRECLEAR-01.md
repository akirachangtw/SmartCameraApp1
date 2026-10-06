# ADV-CR-R0-PRECLEAR-01 — R0 Pre-Clearance Lint 合票評估票 **(原Ticket_ID: INFRA-CR-R0-PRECLEAR-01更名以符合議題)**

**Status**: APPROVED（2026-07-20 Akira 批准 E0 範圍）— E0 待排程執行
**類型**: 評估票（E0 合票/拆票評估 → 批准後轉實作階段）
**來源議題**: Advisor
**起票依據**:
- Handoff_CharterEffective_GovernanceClose_20260719（TODO#1 素材清單）
- `2026-07-19_COMPACT_R0_ClearanceA_DelegationDirection.md`（KD1 兩要件放行＋KD2 PRECLEAR lint 中期方向＋KAIROS ⑧）
- `2026-07-19_VerifyCmdConf_TOCTOU_Mitigation_APlus_Ruling.md`（KD1–KD7；KD7 票號歸併：TOCTOU 掛本票不另立票）
- `2026-07-19_DOCSYNC_TicketLine_Retirement_ClosureAsStandardSOP.md`（KD3 KAIROS ⑧ 載體改掛本票）
- `plans/cso_briefing/reports/ADV-CONCURRENCY-01_VerifyConfConf_TOCTOU_RiskAnalysis_20260719.md`（風險分析 SSOT）

> **TL;DR**: 將四項既裁決事項合票為單一 R0 前置放行基礎設施評估：①兩要件 lint＋PRECLEARED token、②佔用宣告讀取器、③verify_cmd.conf TOCTOU 四要件防線、④KAIROS ⑧ 驗證輸入前置化。E0 先出合票/拆票結論，範圍過載時拆票（需 CSO 覆核）。

---

## 1. Outcome（成果定義）

### 1.1 合票評估範圍（四併入項）

| # | 併入項 | 來源裁決 | 併入性質 |
|---|--------|---------|---------|
| P1 | **兩要件 lint＋PRECLEARED token**：兩要件＝「純 artifact」＋「hunk 無敏感 token」（ClearanceA KD1 定義）。機械可驗事實由 lint 自動驗證，成立即簽發 PRECLEARED token 放行送 Codex，免抵達 Akira。含**敏感 token 清單定版**（本票交付物）。 | ClearanceA KD1/KD2（裁決稅病灶：缺「自動放行」） | 本票主體 |
| P2 | **佔用宣告讀取器**：R0 Pre-flight 讀取 concurrency.md 佔用宣告，機械判定觸碰面衝突。 | TOCTOU Ruling KD7（性質相近＋Handoff TODO#6 合票前例） | 合票評估對象 |
| P3 | **TOCTOU 四要件**（細節以 TOCTOU Ruling 為準）：(a) A+ snapshot-then-verify——執行開頭一次性讀 conf 入局部變數／`$$` 私有暫存全程複用＋關鍵操作前比對 CLI expected TICKET_ID，不符即中止並發 `CONF_RACE_DETECTED`；(b) 寫入端對稱防線——Boot Sequence `rm -f` 前 grep conf 內 TICKET_ID，非己票→不清除＋宣告佔用衝突；(c) `CONF_RACE_DETECTED` token 建模＋B 方案觸發接線（單週 ≥2 → 啟動 session-scoped conf 投資評估；閾值數值直取禁語義化，變更屬 L2 修訂）；(d) STATE_FILE 獨立 AC（沉默污染情境 3：fail-budget 跨票記錯；背景另見 2026-07-04_CrossReview_State_Overload_Regression_Fix）。 | TOCTOU Ruling KD1–KD4 | 票號歸併（KD7 已裁，不另立票） |
| P4 | **KAIROS ⑧ 驗證輸入前置化**：diff 清單＋hunk 原文餵 R0，消解「R0 職責含 git 驗證但無 git 工具」之結構性代驗稅。原載體 DOCSYNC-01 已退役，改掛本票。 | ClearanceA KAIROS ⑧＋DOCSYNC Retirement KD3 | 載體承接 |

### 1.2 追加涵蓋（2026-07-20 CSO session 裁定）

- **concurrency.md §5 append 競態**（`wc -l` 直取現值之 check-then-use gap）：明列入 P3 驗收範圍，與 verify_cmd.conf TOCTOU 同家族不同觸點（行數 append vs conf snapshot）。§5 已簽發條文**不回改**（TOCTOU KD7），僅以 CONCURRENCY-01 待開票清單登記「由本票涵蓋」防重複開票。
- **TICKET_ID CLI 覆寫路徑補齊**：消除 `cross_review.sh:116` fallback="unknown" 之不對稱缺陷（A+ 比對之前提條件，隨 P3(a) 落地）。

### 1.3 E0 階段交付物

1. 合票/拆票評估結論（四併入項逐項：合入理由或拆出建議＋範圍過載判定）
2. 批准後之實作階段票面切分（P1–P4 各自 AC 落點與相依序）
3. PRECLEARED token 信任錨設計草案（見 Constraints C1）
4. 敏感 token 清單初版（供定版審批）

---

## 2. Verification（驗收矩陣）

> E0 評估票驗收＝AC-E 系列；實作階段 AC-I 於 E0 結論批准後定稿，此處先立骨架供審批範圍確認。

### AC-E（本票 E0 驗收）

| # | AC | 驗證方式 |
|---|----|---------|
| AC-E1 | 四併入項逐項給出合/拆結論＋理由，無項目遺漏或蒸發 | Akira 審閱；對照 §1.1 四列 |
| AC-E2 | 拆票判定（若有）附 CSO 覆核請求，符合 TOCTOU KD7 翻盤條件程序 | 結論文件含明確拆票段落 |
| AC-E3 | PRECLEARED token 防偽造機制有具體設計（非 agent 可自簽） | 設計草案通過 C1 檢核 |
| AC-E4 | §5 append 場景明列於 P3 範圍描述，含「不回改已簽發條文」聲明 | 文字比對 |
| AC-E5 | KAIROS ⑧ 有明確落地形態或排程（不得再度蒸發） | 結論含 P4 專節 |

### AC-I 骨架（實作階段，E0 批准後定稿）

| # | 對應 | AC 骨架 |
|---|------|---------|
| AC-I1 | P1 | 兩要件（純 artifact／hunk 無敏感 token）成立 → lint 簽發 PRECLEARED；任一不成立 → fail-closed 走人工 R0 |
| AC-I2 | P1 | 敏感 token 清單定版並由 lint 引用；清單外 token 不觸發攔截 |
| AC-I3 | P2 | 佔用宣告衝突 → 機械攔截並回報觸碰面；無衝突 → 放行且留痕 |
| AC-I4 | P3(a) | conf 一次性 snapshot 全程複用；關鍵操作前 TICKET_ID 比對不符 → 中止＋發 `CONF_RACE_DETECTED`；TICKET_ID CLI 覆寫路徑補齊（§1.2） |
| AC-I5 | P3(b) | Boot Sequence 清除前 grep TICKET_ID：非己票 → 不清除＋宣告佔用衝突 |
| AC-I6 | P3(c) | `CONF_RACE_DETECTED` 事件可觀測可統計；單週 ≥2 → 機械觸發 B 方案投資評估（數值直取禁語義化） |
| AC-I7 | P3(d) | STATE_FILE fail-budget 跨票污染具獨立防線與獨立驗收（不與 conf 端合併驗證） |
| AC-I8 | P3+§1.2 | §5 append 場景：現值直取路徑具 snapshot-then-verify 等效防線 |
| AC-I9 | P4 | R0 收到前置化輸入（diff 清單＋hunk 原文），無需自行 git 取證 |

---

## 3. Constraints（約束）

| # | 約束 | 依據 |
|---|------|------|
| C1 | **PRECLEARED token 為信任錨，不得 agent-forgeable**：簽發權限限 lint 執行環境，agent 不得手寫等效標記（approved.marker P0 教訓同源） | 既有信任錨原則（CSO 延伸判斷，E0 審批確認） |
| C2 | concurrency.md §5 已簽發條文不回改；PROGRESS.md 屬 FORBIDDEN，豁免僅限既有四項收尾動作 | TOCTOU KD7＋DOCSYNC Retirement |
| C3 | 兩騎手約束承接：VERIFIER 代驗須標註；代驗不作先例 | ClearanceA 裁決 A 附款 |
| C4 | 來源決策檔為 SSOT，本票僅引用不複寫；實作 AC 定稿前如遇歧義以 `search_decisions` 覆核（兩要件定義、TOCTOU 四要件已於 2026-07-20 覆核完畢） | 跨引用衛生 |
| C5 | 佔用宣告紀律：本票實作 session 首跑須依 concurrency.md 做佔用宣告 | Handoff Next Steps#2 |
| C6 | 圖譜寫入僅經確認決議觸發；本票執行過程禁止 agent 自動寫回 | 歸檔協議 v3.2 §E |

---

## 4. Iteration（迭代路徑）

1. **E0（本票）**：合票/拆票評估 → Akira 批准範圍
2. **E1**：實作 AC-I 定稿＋P1–P4 相依序排定（建議：P3(a)(b) 為 P1 前置——lint 自身讀 conf 亦受 TOCTOU 威脅；P4 可並行）
3. **I 階段**：依批准切分執行；每一併入項獨立可驗收，避免全有全無
4. **翻盤條件**：範圍過載 → 拆票（CSO 覆核，TOCTOU KD7）；lint 上線後真陽性率顯著 → 回檢「儀式型閘門」定性（ClearanceA KD2 翻盤條件）；B 方案閾值變更屬 L2 修訂

## 5. Error Handling（錯誤處理）

| 情境 | 處置 |
|------|------|
| lint 假陰性（要件成立但未簽發） | fail-closed：照舊走人工 R0，零風險僅摩擦；登記頻次供調校 |
| lint 假陽性（要件不成立卻簽發） | P0 級：立即停用 PRECLEARED 快路，回退全人工，開 KAIROS 收割 |
| `CONF_RACE_DETECTED` 觸發 | 記錄事件；單週 ≥2 依 P3(c) 機械啟動 B 方案評估 |
| MCP 寫入/讀取逾時 | 依 userPreferences R5 SOP：read-back 驗證優先，無法確認改存 artifact，禁重複套 patch |
| 佔用宣告讀取器誤攔 | 回報觸碰面明細供人工覆核；不得靜默放行 |

---

**覆核狀態**（2026-07-20）：
- [x] 兩要件定義覆核（ClearanceA KD1：純 artifact／hunk 無敏感 token）
- [x] TOCTOU 四要件細節覆核（TOCTOU Ruling KD1–KD4 直讀）
- [x] Akira 批准 E0 範圍（四併入項＋§1.2 追加＋C1 延伸判斷＋P3(a)(b)→P1 前置相依序，2026-07-20）
