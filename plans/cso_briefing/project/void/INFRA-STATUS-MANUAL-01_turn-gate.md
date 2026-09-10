# INFRA-STATUS-MANUAL-01: /status 改為僅使用者手動觸發，移除 turn 健康度判定與 /compact 導向鏈

**簽發日期**: 2026-09-10
**簽發者**: Claude (SKAD)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（單一 skill .md 文本修訂，§0 `git restore` 五分鐘域）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `INFRA-CLAUDE-LONGSESSION-01`（Akira 2026-09-10 裁決：`/status` 保留且限定使用者手動觸發；該票 Rigor Gate F1「不另開追蹤票」豁免**僅就 `status/SKILL.md` 部分**被 supersede，見該票票面 L14 補記與冊面 `checkpoint.status_skill_decision_pointer`）
- `coordination_dependency`: `INFRA-CLAUDE-LONGSESSION-01`（APP 冊 seq 33，TICKET_RIGOR）——**非 ALLOWED 交集，但語義耦合**：本票移除 `SKILL.md:77` 對 `CLAUDE.md §Session Health` 之交叉引用，該節本身之處置歸該票；本票措辭 **MUST** 對該節最終形態（刪除或改寫）**中立**，僅移除本側依賴，**MUST NOT** 於本票任何字面預設其結果

**Rev B（2026-09-10，SKAD 裁決；`SPEC_DEFECT`，派工中補正）**：執行端於 AI-0 停止回報 HC-4（L39 逐字保留）與 AC「`健康度`／`對話輪數`／`Session Health` 全檔命中＝0」互斥——L39 尾段含四處禁字面。SMTR 一手核實（`aud_20260910_c8b6758ec0`）：L39 尾段為「**MUST NOT** 作為 Session 健康度判準、**MUST NOT** 作為 `/compact`／`/clear` 之充分條件。健康度判定依既有「對話輪數」規則（見下方 Session 健康度段），本票不變更之。」——其中「見下方 Session 健康度段」係對 L77 之**交叉引用**（L77 為 AI-3 刪除標的），「本票不變更之」係前票 `INFRA-STATUS-SYNC-01` 遺留於活契約之票級相對語。**根因**：開票 AI-0 之 grep 字面（`Session Health|turn|🟢|🟡|🔴|compact`）未含 AC 所列禁字面 `健康度`／`對話輪數`，致 L39 尾段與 **L10**（正文重複 description 之「Session 健康度」）均漏列。裁決：執行端三選項皆不採——A 案排除 L39 會留下指向已刪 L77 之懸空引用；B 案另開票屬過度治理；D 案不成立（確為衝突）。**Rev B**：①HC-4 對 L39 之保留**限縮至子句**（見 HC-4）；②L39 尾段刪除併入 AI-3；③L10 併入 AI-1；④AI-0 錨點與 AC 對應更新。AC「全檔＝0」**維持**——判準正確，錯的是保留範圍。本 Rev 未新增 AI、未改 ALLOWED。O1：`Rev=A→B ｜ AI 標題數=5 ｜ AI 深度=1 ｜ 4a 未成立（計數 2）`。

## 檔案治理／來源說明

**SSOT**：`.claude/skills/status/SKILL.md`（`/status` 行為契約唯一入口）。

**缺口字面（2026-09-10 live grep，`aud_20260910_718b77284c`）**：
- **L4 `disable-model-invocation: false`** —— **自動觸發器之機械載體**。值為 `false` 即允許模型自行調用本 skill，與「僅使用者明確輸入 `/status` 時執行」直接衝突。此為本票唯一之自動觸發器實作點（非 hook、非內文建議鏈）。
- **L3 `description`** —— 自稱「Token 使用估算與 **Session 健康度**」，健康度判定移除後即失準。
- **L71 `| 對話輪數 | [N] 輪 | 🟡 估算 |`** —— 輪數列，健康度判準之輸入。
- **L72 `| 狀態 | 🟢 正常 / 🟡 預警 / 🔴 需優化 | — |`** —— 健康度分級列。
- **L77** —— `**SHOULD** 參照 CLAUDE.md §Session Health 規則。健康度以**對話輪數**判定（≤10 🟢 / 11-18 🟡 / ≥19 🔴）` —— 失效交叉引用＋固定輪數門檻。
- **L79 `**建議行動**：[**依對話輪數**給出一句建議，例如：繼續工作 / 完成當前 Ticket 後執行 /compact / 立即 /compact]`** —— turn／單票完成 → `/compact` 導向鏈。
- **〔Rev B〕L10 `快速顯示當前對話的 Token 使用估算與 Session 健康度。`** —— 正文重複 description，AI-1 一併改寫。
- **〔Rev B〕L39 尾段** `**MUST NOT** 作為 Session 健康度判準、` 與 `健康度判定依既有「對話輪數」規則（見下方 Session 健康度段），本票不變更之。` —— 前者為對已不存在概念之否定，後者為對 L77 之失效交叉引用＋前票遺留之票級相對語。AI-3 一併刪除。

**MUST 保留字面（四處，AI-5 以 diff 驗證未觸）**：
- **L17**：`/context` 為當前 session context 之最終裁決視圖
- **L21**：本 skill 無程式化通道讀取 `/context`，discrepancy 偵測為人工中介
- **L39**〔Rev B 限縮至子句〕：`固定 1,000,000 常數分母所得之佔比為**顯示性 telemetry**，**MUST NOT** 作為 \`/compact\`／\`/clear\` 之充分條件。`——**僅此二子句逐字保留**（後者與 Akira「不以 telemetry 產生強制處置」裁決同向）；L39 前段（1M 常數分母計算規則）亦不動；尾段見缺口字面
- **L41**：快取齡僅顯示原始值、禁分類與建議（`STALE_AFTER_SECONDS` 為 OPEN_PARAMETER）

**⚠️ 標記語義二分（本票最易誤刪處）**：L64–L70 七列之 `🟢 真實 / 🟡 估算 / 🟡 N/A` 為**資料來源真實性標記**（該值取自 hook 快取或為 fallback 估算），**非**健康度分級，**MUST 全數保留**。僅 L72「狀態」列之 `🟢 正常 / 🟡 預警 / 🔴 需優化` 屬健康度分級，為本票刪除標的。二者共用 emoji 但語義互異。

## Verify Config
TICKET_ID=INFRA-STATUS-MANUAL-01
CREATED=2026-09-10
PRESET=docs
ALLOWED=.claude/skills/status/SKILL.md
FORBIDDEN=.claude/skills/sv/SKILL.md,.claude/skills/status/scripts/**,.claude/skills/compact/SKILL.md,.claude/CLAUDE.md,.claude/rules/**,.claude/settings.json,.claude/hooks/**,.claude/PROGRESS.md,plans/**,app/**
MAX_LINES=15
DIFF_REF=<開工時 `git rev-parse --short HEAD` 回填>
ANCHOR_PROBED=2026-09-10（派工時 MUST 重跑 AI-0 並更新本欄時戳）
BASE_COMMIT=<同上>
TARGET_BLOB=N/A（docs preset 單檔小改，比例原則；V1 降級為 AI-0 錨點制）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**MAX_LINES 推導註記**：AI-1 兩行、AI-2 兩行刪除、AI-3 約兩行、AI-4 一行 ≈ 7 → ×1.4 → 10 → 進位 **15**。依 v3.5 本欄為估算校準遙測，非執行期停止判準。

**ALLOWED 收斂說明**：Akira 裁決文字為「僅處理 `.claude/skills/status/**`」；經 AI-0 取證，全部缺口字面均落於該目錄下之 `SKILL.md` 單檔，`scripts/statusline.py` 為本票 **保留** 之 telemetry producer（`INFRA-STATUS-SYNC-01` 已實測記載其 `write_cache()` 行為），故 ALLOWED 取更窄之單檔並將 `scripts/**` 列入 FORBIDDEN，**嚴於**授權範圍而非逾越。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/status/SKILL.md`
- **動作**: 逐字確認下列現值命中數與票面相符（各標示「唯一」者恰 1 次）：
  - L4 `disable-model-invocation: false`（唯一）
  - L3 `description:` 行含「Session 健康度」（唯一）
  - L71 `| 對話輪數 |`（唯一）；L72 `| 狀態 | 🟢 正常 / 🟡 預警 / 🔴 需優化 |`（唯一）
  - L77 含 `CLAUDE.md §Session Health` 與 `≤10 🟢 / 11-18 🟡 / ≥19 🔴`（各唯一）
  - L79 含 `依對話輪數` 與 `立即 /compact`（各唯一）
  - **保留側正向確認**：L17／L21／L41 三處字面在盤；L39 之保留子句「顯示性 telemetry」與「MUST NOT 作為 `/compact`／`/clear` 之充分條件」在盤；L64–L70 七列在盤
  - **〔Rev B 增列〕缺口側**：L10 含「Session 健康度」（唯一）；L39 含「見下方 Session 健康度段」與「本票不變更之」（各唯一）
- **任一不符 → 依 TPL v3.10 基線漂移三分流分類回報，禁單一 STOP、禁推定等價位置**
- **[標配]** `git check-ignore -v .claude/skills/status/SKILL.md` 命中 → 停止回報
- [ ] 完成

### AI-1: frontmatter 關閉模型自主調用＋description 去健康度
- **檔案**: `.claude/skills/status/SKILL.md`，L3–L4
- **現值**: `disable-model-invocation: false`；`description: 快速顯示當前對話的 Token 使用估算與 Session 健康度。優先讀取 statusline hook 快取取得真實 API 數據，無快取則 fallback 至估算模式。`
- **目標值**:
  - `disable-model-invocation: true`
  - `description:` 改為僅述診斷用途，**MUST NOT** 含「健康度」字面；**MUST** 保留「statusline hook 快取／fallback 估算」之既有語義（該機制不在本票變更範圍）
  - **〔Rev B〕L10** 正文首句同步改寫，與新 `description` 語義一致，**MUST NOT** 含「健康度」字面
- [ ] 完成

### AI-2: 刪除輪數列與健康度分級列
- **檔案**: `.claude/skills/status/SKILL.md`，L71–L72
- **動作**: 刪除 `| 對話輪數 | [N] 輪 | 🟡 估算 |` 與 `| 狀態 | 🟢 正常 / 🟡 預警 / 🔴 需優化 | — |` 兩列整列
- **紀律**: L64–L70 七列**逐字不動**（其 🟢/🟡 為資料來源真實性標記，非健康度分級——見「標記語義二分」）
- [ ] 完成

### AI-3: 移除失效交叉引用與固定輪數門檻
- **檔案**: `.claude/skills/status/SKILL.md`，L77
- **現值**: `**Session 健康度**：**SHOULD** 參照 CLAUDE.md §Session Health 規則。健康度以**對話輪數**判定（≤10 🟢 / 11-18 🟡 / ≥19 🔴），**MUST** 不以 Context 佔比判定（佔比為顯示性 telemetry、非健康度判準；其數值本身經 1M 常數分母已可靠——見 Step 1 定位條款）。`
- **目標值**: 整段改寫為——本 skill **MUST NOT** 輸出任何 session 健康度判定或分級；Context 佔比、快取齡、Δtoken 等一律為**顯示性 telemetry**，**MUST NOT** 據以產生強制處置或狀態評級。**MUST NOT** 保留對 `CLAUDE.md §Session Health` 之任何引用
- **紀律**: 措辭 **MUST** 對 `CLAUDE.md §Session Health` 之最終形態中立，**MUST NOT** 出現「該節已刪除」「該節已改寫」等預設其結果之字面
- **〔Rev B 增列〕L39 尾段刪除**：刪除 `**MUST NOT** 作為 Session 健康度判準、`（含頓號）與 `健康度判定依既有「對話輪數」規則（見下方 Session 健康度段），本票不變更之。`（整句）。刪後 L39 尾段應為「…所得之佔比為**顯示性 telemetry**，**MUST NOT** 作為 `/compact`／`/clear` 之充分條件。」——L39 其餘字面**逐字不動**。理由：前者否定一個本票後已不存在之概念（憲章層同型裁決：不需一條規則宣告「沒有規則」）；後者為對 L77 之交叉引用（L77 本 AI 刪除）＋前票票級相對語
- [ ] 完成

### AI-4: 建議行動改為如實呈現
- **檔案**: `.claude/skills/status/SKILL.md`，L79
- **現值**: `**建議行動**：[**依對話輪數**給出一句建議，例如：繼續工作 / 完成當前 Ticket 後執行 /compact / 立即 /compact]`
- **目標值**: 改為 session continuation／handoff／progress 之如實資訊呈現；**MUST NOT** 因輪數或單票完成而建議 `/compact` 或另開 session；**MUST NOT** 引入任何數值閾值型建議觸發
- [ ] 完成

### AI-5: 修訂波及面全檔掃描
- 全檔 grep `Session Health`／`健康度`／`輪數`／`turn`／`🔴`／`compact`，確認無殘留判定語義；`/compact` 若仍出現，**MUST** 僅為使用者主動行為之中性描述，非建議鏈
- 正向確認 L17／L21／L39／L41 四處保留字面與 L64–L70 七列之 diff 為空
- [ ] 完成

## Hard Constraints
1. **MUST NOT** 觸碰 `.claude/skills/sv/SKILL.md` —— 其 `:215` 同型 turn 閾值殘留之豁免經 Akira 2026-09-10 明示**維持有效**，保留 carry-forward，待 `/sv` 自身下次修訂或另票完成上下文取證後處理；**MUST NOT** 因本票而讀作已解除
2. **MUST NOT** 觸碰 `.claude/skills/status/scripts/**` —— telemetry producer 保留不動
3. L64–L70 七列之 `🟢/🟡` 為資料來源真實性標記，**MUST NOT** 刪除或改寫；僅 L72 屬健康度分級
4. 〔Rev B 限縮〕L17／L21／L41 三處保留條款**逐字不動**；L39 **僅**「顯示性 telemetry」與「MUST NOT 作為 `/compact`／`/clear` 之充分條件」二子句及其前段計算規則逐字不動，尾段依 AI-3 刪除
5. **MUST NOT** 引入任何新的數值閾值（輪數、token 數、百分比）作為建議或處置觸發
6. 本票措辭 **MUST** 對 `CLAUDE.md §Session Health` 之最終形態中立
7. 完成後 STOP，等待 SMTR 複審，禁自行擴 scope

## Error Handling
- AI-0 任一錨點不符 → 依 TPL v3.10 三分流分類回報
- `disable-model-invocation` 欄不存在，或其值非 `false` → 停止回報（前提不成立，禁自行新增或推定）
- 改寫後 frontmatter YAML 解析失敗 → 停止回報，禁自行調整其他欄位
- **[標配]** `git check-ignore -v <標的>` 命中 → 停止回報

## Acceptance Criteria
- [ ] **[存在性]** frontmatter `disable-model-invocation: true` 命中＝1；`disable-model-invocation: false` 命中＝0
- [ ] **[存在性]** 全檔 `Session Health`／`健康度` 字面命中＝0（含 `description:` 行）
- [ ] **[存在性]** 全檔 `≤10`／`11-18`／`≥19`／`🔴` 字面命中＝0
- [ ] **[存在性]** 全檔 `對話輪數` 命中＝0；`立即 /compact`／`完成當前 Ticket 後執行 /compact` 命中＝0
- [ ] **[存在性]（保留側正向取證，Rev B 修正）** L17／L21／L41 三處條款字面在盤；L39 含「顯示性 telemetry」與「MUST NOT 作為 `/compact`／`/clear` 之充分條件」二子句且其前段計算規則逐字在盤；L64–L70 七列（模型／Context 已用／5h Rate Limit／Session 花費／快取齡／Δtoken／工作目錄一致性）逐字在盤——`git diff` 佐證 L17／L21／L41／L64–L70 十處零異動，L39 之 diff 僅為尾段刪除
- [ ] **[存在性]〔Rev B〕** 全檔 `本票不變更之` 命中＝0；`見下方` 命中＝0
- [ ] **[行為性]** given 使用者於新 session 明確輸入 `/status`，expect 仍輸出既有 diagnostics 表且輸出中不含健康度分級、輪數判定或 `/compact` 建議字面（實跑一次並附原始輸出）
- [ ] **[存在性]（已知取證限制，誠實揭露）** 「模型不自行調用本 skill」之負向驗證**無程式化通道**，以 frontmatter `disable-model-invocation: true` 之字面值為唯一判準；**MUST NOT** 以「本 session 未見自動觸發」充當正向證明
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset，純 skill 文本，無編譯產物)
- [ ] `git diff --stat` 僅涉及 `.claude/skills/status/SKILL.md` 單檔

---

**End of INFRA-STATUS-MANUAL-01**
