# INFRA-CLAUDE-LONGSESSION-01: CLAUDE.md 長 session 工作流憲章化（廢 turn 閾值強制 compact＋規則載入實測）

**簽發日期**: 2026-09-03
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（憲章文本修訂＋兩檔 frontmatter 三行，§0 `git restore` 五分鐘域）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: Akira 2026-09-03 工作流方向 L2 裁決（APP 全面導入長對話＋原生 context compaction；compact 為 context 管理與階段性交接，非每票結案儀式；長對話規則置於 CLAUDE.md）；`Skill-Advisor/deliver/CLAUDE-Long-term-Rules.md`（外部審查稿，**僅採其分層框架與骨架措辭，其「官方引用」經 SMTR 查核與主張不對應，MUST NOT 作為票面依據**）
- `coordination_dependency`: `INFRA-COMPACT-REVB-APP-01`（APP 冊 seq 32，PENDING）——**非 ALLOWED 交集，但語義耦合**：本票 AI-1 之「`/compact` 用於刻意工作邊界」措辭預設三模式存在；REVB 未落地前 MUST 以模式無關之措辭書寫（見 AI-1 紀律），CLAUDE.md L131 Skills 行之三模式描述**明示不在本票**（待 REVB 落地另補）

**Rigor Gate 回應（2026-09-10，Akira 裁決：豁免）**：`.tmp/rigor-gate/RGR_INFRA-CLAUDE-LONGSESSION-01_20260910.md` F1（Incomplete Root-Cause Sweep）——`.claude/skills/sv/SKILL.md:215`／`.claude/skills/status/SKILL.md:77` 仍含與本票廢除之 turn 閾值語意相同之字面（後者並對 `CLAUDE.md §Session Health` 有交叉引用，本票落地後將失準）。**裁決：豁免，不另開追蹤票**——兩檔屬 `.claude/skills/**`（本票 FORBIDDEN），本票範圍僅收斂 `CLAUDE.md` 憲章層本身；兩檔之殘留舊語意留待該二 skill 自身下次修訂時一併處理，非本票收尾前提，**MUST NOT** 讀作缺口已消除。
**〔Rev B 補記，2026-09-10；範圍限縮修正〕**：上述 F1 豁免之「不另開追蹤票」，**僅就 `status/SKILL.md` 部分**由 Akira 2026-09-10 裁決 supersede；**`sv/SKILL.md:215` 之豁免維持有效，MUST NOT 因本次 status 裁決而讀作已解除**——該檔屬獨立 skill 與驗證鏈責任面，依 Akira 明示裁決保留 carry-forward，待 `/sv` 自身下次修訂或另票完成上下文取證後處理，亦 MUST NOT 併入 status 修補票之 ALLOWED。status 部分之裁決內容：`/status` skill 改為**僅限使用者手動觸發**（移除：自動觸發器、`CLAUDE.md §Session Health` 舊交叉引用、固定輪數門檻、🟢🟡🔴 分級、單票完成→`/compact` 自動建議鏈；保留：指令入口、既有 diagnostics、continuation／handoff／progress 資訊、可取得 telemetry 之如實呈現；行為限制：不自行觸發、不以 telemetry 產生強制處置、不建議因單票完成而 `/compact` 或另開 session），由**另票**承接（`.claude/skills/**` 仍為本票 FORBIDDEN；該票 ALLOWED 限 `.claude/skills/status/**`）。

**Rev D（2026-09-10，Akira 存廢三案裁決＝降級收束；4a 外審回流。本 Rev 為存廢裁決落檔，依 O6 遲滯律不計入修訂計數，落檔後計數重置為 0，本子系統新修補票開立之凍結隨之解除）**：外審回流（MyCraft 外審包「裁決（2026-09-10）」節）：本票不續修 Rev C、不退役重開，**僅執行 `## Session Health` 整節刪除，不插入替代文字**。①**AI-2 撤出本票**：長 session 鐵律條僅為宣示性常駐註記，未提供新的平台行為、可機械驗收之保護或不可重建事實，且與「降低常駐 token」之根因修復方向相反——已寫入工作樹之第 7 條（4 行）、標題「6 條→7 條」、L3「12→13」**全數回退**（見 AI-2）。②**AI-3 撤出本票**：path-scope 議題依既有升裁另案處理；未取得版本綁定之本地 `/context` 實測前，禁改寫 `globs:`／`paths:`（AI-0 動作 A 之 O2 流程留存供另案沿用，本票不執行）。③`/status` 手動化維持 `INFRA-STATUS-MANUAL-01`；`sv/SKILL.md:215` 維持 carry-forward；均不屬本票。**Closure 僅驗證四項**（見 AC 之 Rev D 節）。**回件驗收（SMTR）**：Q1 為負向發現（未發現官方一手來源支持整檔物理行數作常駐成本判準），缺席主張屬邏輯論證，依回件驗收規範得為裁決依據；其正向主張「官方文件以實際 token／有效 context window 衡量」**未附 URL 與發布日期**，不得獨立引用（裁決不依賴之亦成立）。Q2 未直接答覆，裁決以「宣示性註記≠行為保護」邏輯論證替代；Q3 明示延至版本綁定實測，與 AI-3 撤出一致。**取證缺口三處**：Q1 正向主張來源、Q2 全部、Q3 全部。Rigor Gate F2（AI-0 第三分支未明文）隨 AI-3 撤出而 moot。

**Rev C（2026-09-10，Akira 直接裁決；`SPEC_DEFECT`——修法方向層級之翻修，非補正）**：Akira 指出 `## Session Health` 問題之**根因為 `CLAUDE.md` 常駐內容過量**，故「刪掉一個短的舊規則、再補進一段更長的新規則」屬**本末倒置之錯誤修法**。裁決：**刪除該區塊內容**，不改寫。連帶效果三項：①AI-1 目標值由「改寫為四條原則」翻轉為「刪除整節」（見 AI-1）；②Rev B 之 L3「≤150→≤160」補正**自動失效並回退**——刪除該節後檔案由 152 回落至 ≤150，`≤ 150` 自身即真實，無須調高門檻來容納衝突（見 AI-2）；③規範性內容之承載者改為核心鐵律第 7 條（AI-2，一條兩句），憲章層不再另設 Session Health 節。**執行端注意**：AI-1 之四條原則段**已寫入工作樹**（未提交），本 Rev 要求將其連同 `## Session Health` 標題整塊刪除，非在其上再修改。

**⚠️ 4a 停損律成立（機械判定，禁語義裁量）**：本票修訂版次計數 Rev A→B→C＝**3**，於本 Rev 首次達門檻。依觸發條件 4a，強制輸出「設計可行性外審查詢包」（已產出，見冊面 `checkpoint.extreview_4a`），並**凍結本子系統新修補票之開立**，直到外審回流＋存廢三案（退役／降級／續修）裁決落檔。依 O6 遲滯律，存廢裁決落檔後計數重置為 0；存廢裁決本身不計入計數。**凍結範圍界定**：標的為「`CLAUDE.md` 憲章層長 session 工作流」子系統之新修補票；`INFRA-STATUS-MANUAL-01`（seq 44）於本 Rev 前已入冊、ALLOWED 為 `.claude/skills/status/SKILL.md`、屬獨立 skill 責任面（與 `sv/SKILL.md` 獨立性論據同源），**不在凍結範圍**——如認定有異，以 Akira 裁決為準。

**Rev B（2026-09-10，SKAD 裁決，Akira D-10／D-11 甲案；`SPEC_DEFECT`，派工中補正）**：執行端於 AI-2 後停止回報兩項 SSOT 衝突。**①行數預算**：`CLAUDE.md` L3 自我宣告「≤ 150 行」，AI-2 第 7 條（4 行）使檔案 148→152（SMTR 一手：`git_diff --stat` +7/-3 淨 +4，末 hunk `@@ -126,7 +130,7 @@` 位移 +4，`aud_20260910_346d3e80b2`／`b208e1eab5`；`## Path-Scoped Rules` L146 起 6 行至檔尾，`aud_20260910_af3903d317`）。裁決甲：授權 152 行，並於同一補正將 L3 改為「≤ 160 行 / 13 核心規則。超標即重構。」——**此變更修復 SSOT 自我宣告與實體檔案之機械可驗證一致性，非為放行單次 +2 行**；除 L3 外 MUST NOT 藉本 Rev 擴張至其他段落。B 案（壓縮他段）不採：本票 AI 授權不含其他段落，唯一授權內候選（新 Session Health 段）已為 1 行；且 `CLAUDE.md` 為每 session 全量載入，行數係代理指標，該段以 1 行承載約 230 字，壓行數不減載入成本。**②AI-0 動作 A 取證通道**：`/memory` 為互動式指令且票面要求新 session，執行端無法於本 session 產生取證，依 Error Handling 停止 AI-3 並升裁，處置正確。裁決甲：觀測命令改為 **`/context` 優先**（不可用時 `/memory`），流程改寫見 AI-0 動作 A。SMTR web_search 補證（2026-09-10）：`paths` 於 v2.1.84 始為 skills 引入、鍵名語義隨版本變動；另有 2026-04 回報稱 rules 之 `globs:` 無 scoping 作用（anthropics/claude-code #45587），與票面所引 2026-01 #17204 方向相反——故 **12 檔既有 `globs:` 是否生效本身即為待測前提**，動作 A 增列逐一比對。**O2 回收前不預開分支 Rev。** 本 Rev 未新增 AI、未改 ALLOWED／FORBIDDEN／MAX_LINES（L3 為既有異動行之字面修改，不增行）。O1：`Rev=A→B ｜ AI 標題數=4 ｜ AI 深度=1 ｜ 4a 狀態=未成立（計數 2）`。

## 檔案治理／來源說明

**SSOT**：`.claude/CLAUDE.md`（APP 行為憲章唯一入口）。

**缺口字面（2026-09-03 live grep，`aud_20260903_059e0fadc8`）**：
- **L127 `## Session Health`**：「≤10 turns 🟢 / 11-18 🟡 / ≥19 🔴 立即 /compact。單則 >200 行代碼 +2 turns。」——turn 計數型強制 compact 閾值，把 `/compact` 定義為到點即做之儀式，係被 2026-09-03 裁決否定之舊假設的**憲章層具現**。REVB-APP-01 將 skill 改為三模式、CLOSURE-GATE-APP-01 管冊面一致性，但只要本行存在，執行端每 19 輪即被憲章逼迫 compact 一次，兩票成果被上位條文架空。
- **L69**「/compact 後強制重新 Read（無論檔案大小）」——與新工作流**相容**（compact 後 re-read 正確），保留不動。
- **L4／L140-142**：已宣告 `.claude/rules/*.md` 為 path-scoped 自動載入。

**規則載入實況盤點（2026-09-03，`aud_20260903_9c0200614f`／`c07b97d802`）**：`.claude/rules/` 共 **14** 檔（外部審查稿誤計 13，漏 `mcp-service-safety.md`），合計 59,177B。12 檔具 `globs:` frontmatter；**4 檔無 path-scope 故恆常載入**：`ticket_pattern.md`（6,289B，無 frontmatter）、`mcp-service-safety.md`（2,333B，無 frontmatter，**安全規則應恆載，不動**）、`decommission.md`（5,150B，有 description 無 globs）、`state-machine.md`（84B stub，另票）。

**⚠️ 核心技術歧義（決定 AI-3 分支，MUST 實測不得推定）**：Claude Code 官方文件之 path-scope 鍵為 `paths:`，APP 使用的是 `globs:`。2026 年初公開 issue 顯示三種互斥實況並存：①`paths:` 在多種配置下不工作而未文件化之 `globs:` 反而可靠（#17204）；②帶 `paths:` 之規則於 session 啟動時被全域載入、無視路徑限定（#16299）；③帶 `paths:` 之規則完全被忽略永不載入（#22170，v2.1.27）。**三者皆為靜默失敗、無錯誤訊息。** 故 APP 之 12 個 `globs:` 檔實際載入行為不可由靜態閱讀得知，取決於當前 Claude Code 版本。依「工具行為假設 MUST 經實證方可寫入耐久載體」律，本票 AI-0 以 `/context`（〔Rev B〕優先；不可用時 `/memory`）實測為唯一合法取證通道。

## Verify Config
TICKET_ID=INFRA-CLAUDE-LONGSESSION-01
CREATED=2026-09-03
PRESET=docs
ALLOWED=.claude/CLAUDE.md,.claude/rules/ticket_pattern.md,.claude/rules/decommission.md
FORBIDDEN=.claude/rules/mcp-service-safety.md,.claude/rules/ssot-governance.md,.claude/rules/state-machine.md,.claude/skills/**,.claude/hooks/**,.claude/settings.json,.claude/PROGRESS.md,plans/**,app/**
MAX_LINES=25
DIFF_REF=<開工時 `git rev-parse --short HEAD` 回填>
BASE_COMMIT=<同上>
TARGET_BLOB=N/A（docs preset，V1 降級為 AI-0 錨點制）
ANCHOR_PROBED=2026-09-03（派工時 MUST 重跑 AI-0 並更新本欄時戳；與 BASE_COMMIT 不同步即為票面內部矛盾）

**MAX_LINES 推導註記**：AI-1 替換 L127 段 ~5 行；AI-2 鐵律新增 ~4 行；AI-3 兩檔 frontmatter 各 ~3 行 ≈ 15 → ×1.4 → 21 → 進位 **25**。**ALLOWED 三檔中 `ticket_pattern.md`／`decommission.md` 僅得動 frontmatter 區（`---` 之間），內容區任何異動即 diff 超出授權**。

## Action Items

### AI-0: 前置取證（派工後、修改前）
- **動作 A（規則載入實測，本票分支點；〔Rev B 改寫，D-11〕由 Akira 於新終端手動執行，執行端 MUST 以貼回之原始輸出判定，禁推定）**：於 APP repo 根目錄開**新** Claude Code session，**未觸碰任何匹配 Kotlin 檔前**依序執行 `claude --version` 與 `/context`（不可用時 `/memory`），將「Memory files」下列出之全部檔案與 token 數之**完整原始輸出**逐字記錄存收尾。判定：
  - **逐一比對 12 個具 `globs:` 之 rules 檔**：任一於啟動即載入 → **`globs:` 於當前版本為 no-op 或被全域載入** → AI-3 **停止並升裁**（分支 β；票面「12 檔已具 globs 故無全量載入問題」之前提反轉，處置另裁，禁預開 Rev）
  - 12 檔**皆未**載入，且列出者僅含 `CLAUDE.md` ＋ 無 path-scope 之四檔（`ticket_pattern`／`mcp-service-safety`／`decommission`／`state-machine`）→ 續下一步
  - **僅在上一步成立時**，Read 一個明確匹配之 `app/src/main/**/*.kt` 檔，再執行 `/context`：`android-conventions.md` 等**此時才出現** → **`globs:` 機制正常** → AI-3 走分支 α；匹配檔讀取後**仍無法證實載入** → AI-3 停止並升裁（分支 β）
  - 全程 **MUST NOT** 自行改用 `paths:`、刪除 `globs:` 或批次修改既有 12 檔
  - `claude --version` 輸出 MUST 隨附——載入行為與版本綁定，缺版本號之取證不可重放
- **動作 B（錨點復驗）**：`CLAUDE.md` L127 「≤10 turns 🟢」字面唯一命中；L69 「/compact 後強制重新 Read」在盤；L21 `## 核心鐵律（6 條）` 在盤；`ticket_pattern.md` 首行**非** `---`（證無 frontmatter）；`decommission.md` 首行為 `---` 且 `globs:` 命中 0
- **動作 C（三分流）**：任一不符依 TPL v3.8 基線漂移三分流分類回報，禁單一 STOP
- **[標配]** `git check-ignore -v` 對 ALLOWED 三檔 → 命中即停止回報
- [x] 完成（動作B：L127「≤10 turns 🟢」/L69/L21「6 條」/`ticket_pattern.md`首行非`---`/`decommission.md`首行`---`且globs=0，全數複核相符；動作C：無不符無需三分流；動作D：check-ignore 未命中。**動作A 未取證**：`/context`／`/memory` 為互動式指令，本 session 無法開新 session 執行，依 Error Handling 停止並升裁——惟其唯一消費者 AI-3 已依 Rev D 撤出本票，缺口不影響本票 closure，另案沿用）

### AI-1: 〔Rev C 翻轉〕刪除 `## Session Health` 整個區塊
- **檔案**: `.claude/CLAUDE.md`
- **原始現值**（本票開工前）: `## Session Health` 標題行 ＋ 「≤10 turns 🟢 / 11-18 🟡 / ≥19 🔴 立即 /compact。單則 >200 行代碼 +2 turns。」
- **工作樹現值**（Rev B 期間執行端已寫入，未提交）: `## Session Health` 標題行 ＋ 一行約 230 字之四條長 session 原則段
- **目標值**〔Rev C〕: **該區塊不存在**——刪除 `## Session Health` 標題行、其下內容行與該節專屬之空行，使 `## SOP` 節之後直接銜接 `## Skills` 節（節間空行維持原格式，不增不減）
- **裁決理由（Akira 2026-09-10，逐字承載）**: 根因為 `CLAUDE.md` 常駐內容過量；「刪掉一個短的舊規則、再補進一段更長的新規則」是典型本末倒置之錯誤修法。**要廢的是強制觸發機制本身，不是補一段更長的說明。**
- **規範性內容之去向**: 原四條原則之規範性內容由**核心鐵律第 7 條**（AI-2）以一條兩句承載；憲章層**不再另設** Session Health 節。原第 3 條「context 壓力由原生 auto-compact 承擔」之語義由「憲章對 compact 觸發保持沉默」直接實現——不需要一條規則來宣告「沒有規則」。
- **紀律**: 本 AI **MUST NOT** 於他處新增替代段落、**MUST NOT** 將被刪內容搬遷至其他節；除該區塊外**零異動**
- **紀律**：本 AI **MUST NOT** 引入任何新的數值閾值（turn 數、token 數、百分比）作為 compact 觸發——依「無計量通道即禁輸出計量律」，Claude Code 端雖有 `total_input_tokens` 通道，但該通道屬 `/status` skill 之顯示性 telemetry，非憲章層觸發器
- [x] 完成（`## Session Health` 標題行＋內容行＋專屬空行整節刪除，`## SOP` 直接銜接 `## Skills`，節間空行維持原格式；commit `0129930`，diff 見收尾）

### AI-2: 〔Rev D 撤出〕核心鐵律長 session 條——回退已寫入工作樹之變更
- **〔Rev D〕動作**: 本 AI 之原目標**撤銷**。執行端 MUST 回退工作樹中屬本 AI 之全部變更：①刪除已新增之 `### 7. 長 session 不終止律` 標題與正文（4 行）；②`## 核心鐵律（7 條）` 回退為 `## 核心鐵律（6 條）`；③L3 「13 核心規則」回退為 「12 核心規則」。**建議路徑**：`git checkout -- .claude/CLAUDE.md` 一次回退全部未提交變更至基線，再僅執行 AI-1 之整節刪除——此路徑天然滿足「diff 僅涉 CLAUDE.md」「bytes 小於基線」。
- **撤出理由（Akira 外審回流裁決，逐字承載）**: 其僅為宣示性常駐註記，未提供新的平台行為、可機械驗收的保護或不可重建事實，且與降低常駐 token 的根因修復相反。
- 以下為 Rev A–C 原文，保留供追溯，**不再執行**：

#### （原）AI-2: 核心鐵律增列長 session 條
- **檔案**: `.claude/CLAUDE.md`，`## 核心鐵律（6 條）` 節
- **動作**: 增列第 7 條（標題數字同步改「7 條」）：「**長 session 不因單票完成而終止**——票的結案在冊面，不在 session 邊界；交接檔是 context 管理載體，不是結案載體。」
- **紀律**: 一條、兩句，禁展開為段落；既有六條逐字不動
- **〔Rev C 回退，取代 Rev B 增列；Rev D 再修正〕L3 逐字不動**：Rev B 之「≤150→≤160」作廢（Rev C）；Rev D 撤出第 7 條後，L3 之「12→13」亦回退——**L3 全行維持基線字面「≤ 150 行 / 12 核心規則。超標即重構。」零異動**。刪除 Session Health 整節後檔案由基線 148 行降至 ≤148，`≤ 150` 自然成立，無須任何 L3 變更
- [x] 完成（Rev D 撤出動作已執行：`git checkout -- .claude/CLAUDE.md` 一次性回退全部未提交變更至基線，再僅重跑 AI-1；三項回退目標確認——第 7 條 4 行不存在、標題維持「6 條」、L3 維持「12 核心規則」，逐行 grep 複核見收尾）

### AI-3: 〔Rev D 撤出，另案〕兩檔補 path-scope（依 AI-0 分支）
- **〔Rev D〕動作**: 本 AI **撤出本票**，依既有升裁另案處理。**MUST NOT** 於本票觸碰 `.claude/rules/**`。未取得版本綁定之本地 `/context` 實測前，禁改寫任何檔之 `globs:`／`paths:`。AI-0 動作 A 所載 O2 流程與 12 檔逐一比對之設計留存於本票面，供另案開票時沿用。
- 以下為原文，保留供追溯，**不再執行**：
- **分支 α（`globs:` 機制正常）**：
  - `ticket_pattern.md`：檔首加 frontmatter `globs:` 綁 `plans/cso_briefing/project/**`、`.claude/skills/**`、`tmp/**`（開票、改票、closeout、skill 施作時載入；比照 `cross-review.md` 既有 globs 形狀）
  - `decommission.md`：於既有 frontmatter 補 `globs:` 綁 `app/src/main/**`、`.claude/skills/**`（刪除／下線／替換動作必經之路徑）
  - **兩檔內容區零異動**（AC 以 frontmatter 外 diff 為空驗證）
- **分支 β（機制失效或全域載入）**：**停止回報升裁**，附 `/context`（或 `/memory`）原始輸出與版本號。禁自行將 `globs:` 改為 `paths:`（12 檔批次改鍵屬另票，且 `paths:` 自身亦有失效回報，改鍵不保證修復）
- [x] 完成（Rev D 撤出：`.claude/rules/**` 於本票全程未觸碰，`git diff --stat` 佐證見收尾；path-scope 議題另案處理）

### AI-4: 修訂波及面全檔掃描〔Rev D 收斂為 closure 四項驗證〕
- **〔Rev D〕Closure 僅驗證下列四項**（Akira 裁決逐字）：
  1. `.claude/CLAUDE.md` 之 `Session Health`、`≤10`、`11-18`、`≥19`、`turns 🟢`、`🔴 立即 /compact`、`+2 turns` 命中為 0
  2. `git diff --check` 通過，且 diff 僅涉及 `.claude/CLAUDE.md`
  3. 修改後 `.claude/CLAUDE.md` 的 bytes 小於基線（`git show HEAD:.claude/CLAUDE.md | wc -c` vs 工作樹 `wc -c`）
  4. 核心鐵律標題為「6 條」、L3 為「12 核心規則」——即 AI-2 回退完成之正向證明
- 原掃描項目（下列）仍可執行，但**非 closure 判準**：
- `CLAUDE.md` 全檔 grep `compact`／`turn`／`Session Health`／`交接`，確認無殘留 turn 閾值語義、無與 AI-1 四條矛盾之敘述。L69、L61（`/compact` 為驗收工具非實作授權）、L131 Skills 行**保留**，僅回報其現值於收尾
- [x] 完成（Closure 四項機械驗證全 PASS，見 `sv_report.md`／收尾；L69/L61/L131 現值未變，`grep compact|turn|Session Health|交接` 僅命中預期保留行，無矛盾殘留）

## Hard Constraints
1. **MUST NOT** 動 L131 Skills 行之 `/compact` 描述（待 REVB-APP-01 落地後另補三模式）
2. **MUST NOT** 動 `mcp-service-safety.md`（安全規則恆載為正確狀態）、`state-machine.md`（stub 補實另票）、`ssot-governance.md`
3. AI-1 措辭 **MUST** 模式無關；出現 `brief`／`checkpoint`／`closeout` 字面即違反
4. **MUST NOT** 引入任何數值型 compact 觸發閾值
5. AI-3 分支 β **MUST** 升裁，禁改鍵名、禁刪 globs、禁人工加「Rule loading」路由段（後者為外部審查稿建議，僅在機制失效時才有意義，且屬另票範圍）
6. `ticket_pattern.md`／`decommission.md` 僅得動 frontmatter 區
7. 完成後 STOP，等待 SMTR 複審

## Error Handling
- AI-0 動作 A 判定為分支 β → AI-1／AI-2 照常執行，AI-3 停止並升裁（兩者獨立，禁因 β 而整票停止）
- `/context`／`/memory` 指令不可用、執行端無互動式通道（本 session 內無法開新 session）、或輸出格式無法辨識檔案清單 → 停止回報並附原始輸出，改由 Akira 於新終端手動執行後貼回；禁以「看起來只載了幾個」推定
- AI-0 動作 B 任一錨點不符 → 三分流分類回報
- **[標配]** `git check-ignore -v <標的>` 命中 → 停止回報

## Acceptance Criteria

**〔Rev D〕Closure 判準（唯此四項，取代下列 Rev A–C 各 AC）**：
- [x] **[存在性]** `.claude/CLAUDE.md` 全檔 `Session Health`／`≤10`／`11-18`／`≥19`／`turns 🟢`／`🔴 立即 /compact`／`+2 turns` 七組字面命中＝0（grep 複核）
- [x] **[存在性]** `git diff --check` exit 0；`git diff --stat` 僅涉 `.claude/CLAUDE.md` 單檔（`a58b03e..0129930`）
- [x] **[存在性]** 修改後 `.claude/CLAUDE.md` bytes **小於**基線：6975（基線）→ 6860（修改後）
- [x] **[存在性]** `## 核心鐵律（6 條）` 命中＝1、`7 條` 命中＝0；L3 字面為「≤ 150 行 / 12 核心規則。超標即重構。」逐字相符

以下 Rev A–C 之 AC 保留供追溯，**不再作為 closure 判準**。⚠️ **其未勾狀態 MUST NOT 讀作未閉環缺口**——本票依 Rev D 降級收束，AI-2／AI-3 已撤出，故下列各項或已由 Rev D 判準取代、或其標的已不存在。**其中二項與收票現況方向相反，係已作廢之舊判準，非待辦**：①「L3 字面為『≤ 150 行 / 13 核心規則』」——Rev D 撤出第 7 條後 L3 回退為 **12 核心規則**（現況正確）；②「核心鐵律標題為『7 條』」——現況為 **6 條**（現況正確）。二者之正確判準見上方 Rev D closure 第 4 項（已勾）。
- [ ] **[存在性]** `CLAUDE.md` 內 `turns 🟢`／`🔴 立即 /compact`／`+2 turns` 字面命中＝0
- [ ] **[存在性]〔Rev C 取代〕** `.claude/CLAUDE.md` 全檔 `## Session Health` 字面命中＝**0**（整節已刪除）；且被刪內容未於他處復現——`長 session 為預設工作型態`／`原生 auto-compact`／`刻意的工作邊界` 三片語全檔命中＝0
- [ ] **[存在性]〔Rev C 取代 Rev B〕** `CLAUDE.md` L3 字面為「≤ 150 行 / 13 核心規則。超標即重構。」（`≤ 150` 逐字未動，僅 `12→13`）；且 `wc -l` 實測 ≤ 150（`git diff --stat` 佐證淨行數變化）
- [ ] **[存在性]** 核心鐵律標題為「7 條」，第 7 條含「不因單票完成而終止」字面；原六條逐字未變（diff 驗證）
- [ ] **[行為性]** AI-0 之 `/context`（或 `/memory`）完整原始輸出（觸碰 Kotlin 前；及僅於 12 檔皆未載入時之觸碰後第二次）、12 檔 `globs:` 逐一比對結果與 `claude --version` 已逐字存收尾，且分支判定依該輸出作成——禁以未附輸出之敘述充當
- [ ] **[行為性]（分支 α）** `ticket_pattern.md` 與 `decommission.md` 之 diff 僅位於 frontmatter 區（`---` 之間），內容區 diff 為空；新 session `/context` 不再列出此二檔，Read `plans/cso_briefing/project/Pending/` 任一票後 `/context` 列出 `ticket_pattern.md`
- [ ] **[行為性]（分支 β）** 升裁記錄在盤，AI-3 未施作，本項以「升裁完成」勾選
- [ ] **[行為性]** AI-4 全檔掃描零殘留，L69／L61／L131 現值已回報
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset，憲章文本與 frontmatter，無編譯產物)
- [ ] `git diff --stat` 僅涉及 ALLOWED 三檔（分支 β 時僅 `CLAUDE.md` 一檔）

---

## 收尾實錄

**四要件④耐久載體（2026-09-10）**：AI-0~AI-4 碼閉環（動作A 取證缺口如實揭露）+ Rev D Closure 四項 AC 全數附證據勾選（本檔本體，見上方）。

**Commits**（`DIFF_REF=a58b03e..DIFF_END=0129930`）：
- `0129930` — 廢 turn 閾值強制 compact（`## Session Health` 整節刪除，`.claude/CLAUDE.md` 單檔，6975→6860 bytes）

**審查鏈**：
- `/qa`：✅ PASS（`assembleDebug` 50s、`testDeviceDebugUnitTest` 209/209、lint 584 baseline/0 new）；Step 4 PROGRESS.md 更新依票面 FORBIDDEN 命中自動 SKIP（一度誤觸即時 `git checkout --` 還原，未夾帶提交）
- `/sv` Stage A-C：✅ PASS（ELF ALIGNED；檔案邊界 `a58b03e..0129930` 僅命中 `.claude/CLAUDE.md`）
- Codex Cross-Review：R1 ✅ **PASS**（REACHABILITY: NONE）
- `structural_verify.sh --post`：✅ PASS(A/B/C/D/E/F/G) `[ADVISORY]`（唯一告誡：`.claude/PROGRESS.md` 80 行逼近上限，非本票 scope）
- Traycer：**N_A**（票面 Header L7 開票時即宣告）

**Rev 沿革**（誠實揭露，不隱去）：Rev A（原始「改寫四條原則」）→ Rev B（執行端 STOP 回報行數預算 SSOT 衝突）→ Rev C（Akira 裁決：根因為 CLAUDE.md 常駐內容過量，改為整節刪除）→ Rev D（外審回流，AI-2/AI-3 撤出，closure 收斂為四項機械判準，本輪執行依循）。

**非阻擋項誠實揭露**：
- AI-0 動作A（`/context`／`/memory` 規則載入實測）因本 session 無互動式新 session 通道未能取證；其唯一消費者 AI-3 已依 Rev D 撤出本票，另案沿用，不影響本票 closure
- `sv/SKILL.md:215`／`status/SKILL.md`（已由 `INFRA-STATUS-MANUAL-01` 承接）之殘留 turn 閾值語意屬 `.claude/skills/**`，本票 FORBIDDEN，非本票收尾前提（票面 L13-14 Rigor Gate 豁免已載明）

**下游影響**：`INFRA-COMPACT-REVB-APP-01` L131 Skills 行之 `/compact` 描述本票明示不動（待該票落地後另補三模式），無交互衝突。

---

**End of INFRA-CLAUDE-LONGSESSION-01**
