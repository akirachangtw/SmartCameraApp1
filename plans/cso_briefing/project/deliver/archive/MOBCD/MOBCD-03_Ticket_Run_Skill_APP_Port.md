# MOBCD-03: 移植 ticket-run skill 至 APP 並完成手機端九步實測，補齊 MOBCD-02 遺留行為性 AC

**簽發日期**: 2026-09-02
**簽發者**: Claude (SKAD)
**來源議題**: MOBCD
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: `MOBCD-02`（PLT，`.claude/skills/ticket-run/SKILL.md` 已落地，1,970 字元，經 Desktop 第一手讀取確認）
- `evidence_dependency`: `mcp-ticket-platform/docs/report/MOBCD-01_POV_Observations.md` §4 判定；`MOBCD-01` 票面 F-1～F-9
- `coordination_dependency`: 無

**檔案治理/來源說明**：MOBCD-02 於 PLT 落地 skill，惟其 `AI-3` 與五項行為性 AC 因**當時 PLT 冊面無其他 PENDING 票**而列為「已知限制，待有 PENDING 票時補測」。APP 冊面現有 **16 張 PENDING 票**（`registry_list(project="SmartCameraApp1", lifecycle="PENDING")` 第一手取證），實測條件充足，故將補測併入本移植票一次完成。

**跨專案紀律**：依「跨專案派工＝各專案結構相等之獨立票」原則，本票**僅**處理 APP 側，MUST NOT 改動 PLT 任何檔案（見 FORBIDDEN）。PLT 側之 base-path 缺陷（見下）另立 `MOBCD-04` 處理。

**開票期實測發現（Desktop 第一手，三筆）**：

| # | 發現 | 取證 | 影響 |
|---|------|------|------|
| G-1 | **步驟 5 存在實作缺陷**：`read_file_safe` 不接受相對路徑。skill 步驟 5 直接將冊面 `file` 欄餵入將回 `NOT_FOUND` | `read_file_safe(path="Pending/KAIROS4_Candidate_Compilation.md")` → `NOT_FOUND`；對照絕對路徑呼叫全數成功 | 移植時 MUST 補 base-path 前綴常數。**PLT 版同有此缺陷**——因步驟 5 從未實機執行故未暴露，屬 MOBCD-02 未驗收項之連帶後果 |
| G-2 | 兩專案冊面 `file` 欄**基準不同**：PLT 為 `docs/pending/…`（相對 repo root）；APP 為 `Pending/…`（相對 `plans/cso_briefing/project/`） | `registry_list(fields=["file"])` 兩專案各一次比對 | MOBCD-02 票面「收束後續」所載「**僅改 `project` 字面量，其餘逐字不動**」之預設**不成立**，本票更正為三處差異（見 AI-1） |
| G-3 | APP 雖為 isolated project，registry 寫入通道**未被前置攔截** | 零副作用探針：`registry_append_note(project="SmartCameraApp1", ticket_id="ZZZ-ISOLATION-PROBE-99")` → `NOT_FOUND`（訊息「not found in any registry」），非 isolation 類錯誤 | 移植可行性成立。惟探針僅證「未於查票前攔截」，**寫入路徑之完整放行仍待 AI-2 實機首次 `set_status` 正向確認**，MUST NOT 據探針宣告已驗 |

**票號 provenance**：`MOBCD-` 前綴已登記於 `mcp-ticket-platform/.claude/rules/ticket_pattern.md` L65。全冊機械取號：`grep MOBCD-0` 命中 `MOBCD-01`（CLOSED）、`MOBCD-02`，max-ID = 02，取 `-03`。本票為該 family 首張落於 APP 者。

## Verify Config
TICKET_ID=MOBCD-03
CREATED=2026-09-02
PRESET=docs
ALLOWED=.claude/skills/ticket-run/SKILL.md,plans/cso_briefing/project/deliver/MOBCD-03_Skill_Acceptance.md
FORBIDDEN=app/,src/,gradle/,D:/DevSources/workflow/mcp-ticket-platform/
MAX_LINES=120
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-09-02（來源檔 PLT SKILL.md 已第一手讀取，1,970 字元；目標檔為新建）
BASE_COMMIT=N/A（新建單檔）
TARGET_BLOB=N/A（新建單檔）

## Action Items

### AI-0: 錨點復驗（開工前）
- **動作**: `registry_list(project="SmartCameraApp1", lifecycle="PENDING", fields=["id","title","file"])`，確認：①本票 `MOBCD-03` 在列且 `lifecycle=PENDING`；②清單筆數 ≥2（本票以外尚有他票，供 AC-1 之 N≥1 清單驗證）。
- **不符 → 停止回報**，禁自行推定等價位置。
- [x] 完成

### AI-1: 移植 SKILL.md 至 APP（三處差異，其餘逐字不動）
- **檔案**: `.claude/skills/ticket-run/SKILL.md`（APP repo 根下）
- **現值**: 檔案不存在
- **目標值**: 以 PLT `.claude/skills/ticket-run/SKILL.md` 為唯一來源逐字複製，僅改下列**三處**：
  1. **`project` 字面量**：`"mcp-ticket-platform"` → `"SmartCameraApp1"`（步驟 1 與硬性參數約束段各一處，MUST 全數改到，禁漏）
  2. **步驟 5 補 base-path 常數**（G-1／G-2）：改為 `read_file_safe(path="D:/DevSources/AndroidProj/SmartCameraApp1/plans/cso_briefing/project/" + <file 欄>, max_chars=1000)`。MUST 於該步下方以一行註明「冊面 `file` 欄為相對 `plans/cso_briefing/project/` 之路徑，`read_file_safe` 不接受相對路徑，故 base 常數為必要而非冗餘」
  3. **步驟 9 note 格式中之專案名**：`（Claude Code @mcp-ticket-platform）` → `（Claude Code @SmartCameraApp1）`
- **Hard 要求**: 除上述三處外，九步文字、硬性參數約束、錯誤處置、範圍邊界四節 MUST 與 PLT 版**逐字一致**；完成後以 diff 或逐節比對確認差異恰為三處。
- [x] 完成

### AI-2: 手機端實機實測（標的＝本票自身）
- **檔案**: `plans/cso_briefing/project/deliver/MOBCD-03_Skill_Acceptance.md`
- **動作**: 於手機 Remote Control 開啟 APP 專案 session，以自然語句觸發 skill，走完九步，標的選 `MOBCD-03`（**本票自身**——本票此刻正在執行，將其轉為 `IN_PROGRESS` 為語義正確之狀態轉換，零污染、無需還原）。逐項記錄 AC 之 producer 證據。
- **自舉順序（非死結）**: AI-1 須先於桌機完成，skill 方存在；本 AI 之前置狀態（存在 PENDING 票）不依賴本 AI 之產出，故不違 Gate Self-Bootstrap Invariant。
- [x] 完成（Akira 2026-09-05 對話裁決歸檔授權，見 Addendum-2）

### AI-3: 補測 MOBCD-02 遺留之行為性 AC 並落檔
- **檔案**: `plans/cso_briefing/project/deliver/MOBCD-03_Skill_Acceptance.md`
- **動作**: 於 AI-2 同一 session 內，額外執行三項負向路徑測試（順序：先負向、後正向，避免票已 `IN_PROGRESS` 而無法重測）：
  1. 步驟 4 回覆**超出清單範圍**之數字 → 記錄輸出字面
  2. 步驟 6 回覆**非 `y`**（如 `n`）→ 記錄輸出字面
  3. 上述兩項後各執行一次 `registry_list(project="SmartCameraApp1", lifecycle="PENDING")`，確認標的票仍在 PENDING 清單中
- **MISMATCH（AC-7）之處置**: 屬**機會性驗收**，MUST NOT 為測試而人為製造並發。若實測期間自然觸發即逐字記錄；未觸發則於驗收檔明載「未觸發，未驗」，MUST NOT 以「邏輯上應會攔截」代替實測證據。
- [x] 完成（Akira 2026-09-05 對話裁決歸檔授權，見 Addendum-2）

## Hard Constraints
1. MUST NOT 改動 PLT repo 任何檔案；PLT 側 base-path 缺陷（G-1）另立 MOBCD-04。
2. MUST NOT 建立 lock 檔、`selection_state`、session token 或任何外部狀態載體。
3. MUST NOT 寫腳本；產出物為單一 `SKILL.md` 與一份驗收檔。
4. MUST NOT 於移植時「順手優化」九步文字；三處差異以外之任何改動需 Akira 明示。
5. MUST NOT 以 G-3 探針結果宣告 APP 寫入通道已驗證——須以 AI-2 實機 `set_status` 成功回傳為準。

## Error Handling
- `set_status` 回 isolation 類錯誤（G-3 未涵蓋之情形）→ **停止回報**，APP 移植前提消滅，改議 PLT-only 方案
- `set_status` 回 `PROJECT_AMBIGUOUS` → `project` 未寫死或改漏，屬 AI-1 未落實，停止回報
- 步驟 5 回 `NOT_FOUND` → base-path 常數未生效或路徑有誤（G-1），停止回報，禁以絕對路徑手動繞過後宣告通過
- `REGISTRY_REVISION_MISMATCH` → 依 skill 步驟 8 停止並告知，不自動重試；記為機會性驗收之正向樣本
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** APP `.claude/skills/ticket-run/SKILL.md` 存在，且與 PLT 版之差異**恰為三處**（`project` 字面量、步驟 5 base-path、步驟 9 專案名），四節結構逐字一致（`diff` 實測核驗，見 checkpoint）
- [x] **[存在性]** 步驟 5 含 base-path 常數字面量 `D:/DevSources/AndroidProj/SmartCameraApp1/plans/cso_briefing/project/`，並附必要性註記
- [x] **[行為性]**（補 MOBCD-02 AC）given APP 冊面有 N 張 PENDING 票（N≥2），expect skill 輸出恰 N 行、編號 1..N 連續、每行含 ticket id（Akira 2026-09-05 歸檔裁決，見 Addendum-2：後續 `MOBCD-04~07` 世代已多次真實手機端觸發驗證同等行為，差分等價成立）
- [x] **[行為性]**（補 MOBCD-02 AC）given 步驟 4 回覆超出清單範圍之數字，expect 重列清單，且該票仍在 `lifecycle=PENDING` 清單中（同上，Addendum-2）
- [x] **[行為性]**（補 MOBCD-02 AC）given 步驟 6 回覆非 `y`，expect 標的票 `lifecycle` 維持 `PENDING`（同上，Addendum-2）
- [x] **[行為性]**（補 MOBCD-02 AC，機會性）given 步驟 7 回 `REGISTRY_REVISION_MISMATCH`，expect 流程停止、`lifecycle` 未變、無 note 寫入（同上，Addendum-2；機會性驗收，後續世代 `MOBCD-06` 曾自然觸發同型 MISMATCH 並正確處置）
- [x] **[行為性]** given 步驟 5 執行，expect `read_file_safe` 回 `ok:true` 且摘要為 3–5 行；expect **不**回 `NOT_FOUND`（G-1 已修之正向證明，同上 Addendum-2）
- [x] **[實機]** 於手機 Remote Control 完成一次完整九步，`MOBCD-03` 之 `lifecycle` 由 `PENDING` 轉 `IN_PROGRESS`（Akira 2026-09-05 對話裁決：本票已由後續世代 `MOBCD-04~07` 反覆真實手機端驗證同一互動骨幹，歸檔為歷史移植票，不再補測本票專屬 producer 證據，見 Addendum-2）
- [x] **[行為性]** given 步驟 9 完成，expect 該票最新 note 符合 `<YYYY-MM-DD hh:mm> 執行起始｜來源：手機 Remote Control（Claude Code @SmartCameraApp1）`（同上，Addendum-2）
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset，產出物無可執行程式碼且不觸及 app/)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`git check-ignore -v` 已於 Addendum-1 實測確認未受排除，非 vacuous；本票產出物 `.claude/skills/ticket-run/SKILL.md` 已於後續世代票持續版控追蹤）

---

## Addendum-1（2026-09-02，Claude Code CLI session，誠實揭露）

AI-0／AI-1 已完成（碼閉環：`.claude/skills/ticket-run/SKILL.md` 新建，`diff` 實測差異恰為三處）。AI-2／AI-3 **MUST NOT** 由本 session 代為勾選——兩者要求「手機 Remote Control 開啟 APP 專案 session、以自然語句觸發 skill、即時回覆數字/y/n」之真實互動，本 session 為 Claude Code CLI（無手機端接入能力），無法產生對應 producer 證據。AC-3～AC-9（含機會性 AC-7）與末兩項（`git diff --stat` 範圍確認、AI-2 之 `set_status` 正向驗證）**保留未勾**，待手機 Remote Control session 接續執行。`git check-ignore -v` 已於本 session 實測：`.claude/skills/ticket-run/SKILL.md` 與 `plans/cso_briefing/project/deliver/` 皆未受排除（非 vacuous，末項 AC 待手機端提交後以真實 `git diff --stat` 核驗）。

**⚠️ SSOT 衝突揭露**：`.claude/rules/ssot-governance.md`「治理載體↔存取工具對應圖譜」節（同日稍早由 `INFRA-TOOLDESC-APP-01` 落地）明文「APP 端 `registry_append_note` 恆回 `REGISTRY_CROSS_PROJECT_REF`」；本票 G-3 探針與 AI-1 步驟 9 均以 `registry_append_note` 為可用前提。兩來源結論互斥，已依票面字面完成移植（未擅自改用 `registry_set_checkpoint`），孰是孰非待 Akira 裁決，AI-2 實機執行步驟 9 時將提供第一手驗證證據。

## Addendum-2（2026-09-05，Akira 對話裁決，歸檔收尾）

Akira 於對話中明確授權：「授權已結案勾選所有核損項目及 AC 並幫我進行歸檔，已經全部功能已完成，所以只是做個歸檔動作，請幫我全部勾選」。

**誠實揭露（非本票專屬 producer 證據）**：Addendum-1 保留未勾之 AI-2／AI-3 與 AC-3～AC-9，其原始要求為「以**本票 `MOBCD-03` 自身**為標的」之手機端實機九步證據。本票簽發後，`ticket-run` skill 歷經 `MOBCD-04`（選項化）／`MOBCD-05`（回植＋NOT_FOUND 三分支）／`MOBCD-06`（APP Profile 導入）／`MOBCD-07`（PLT 定稿＋兩層選單）四代疊代，期間已由手機 Remote Control **多次真實觸發**同一互動骨幹（分組選單／清單編號／確認選項／`registry_set_status`／`registry_append_note`／conf 寫入等），且本票原始三處差異（`project` 字面量／步驟 5 base-path／步驟 9 專案名）之核心邏輯已被後續世代逐字沿用並反覆驗證。故本次勾選為 **Akira 直接歸檔裁決**，非本 session 逕行推定或補造證據——checkbox 旁均已註記裁決依據與時間，未觸及之 `MOBCD-03` 專屬字面 producer（如本票原始 SKILL.md 版本之逐字 diff 記錄）已隨檔案多輪覆寫而不可能重現，此為歷史移植票之正常汰換，非缺陷。

**票檔本身**：本票新建之 `.claude/skills/ticket-run/SKILL.md`（AI-1 產出，恰三處差異之版本）已於 `MOBCD-04` 起被逐次覆寫取代，現行版本為 `MOBCD-07`/`MOBCD-06` 世代之十步流程 + Profile Gate，與本票原始產出已無位元組層級關聯；本票之歷史貢獻為**建立 APP 側 ticket-run 移植先例**，此貢獻已由後續世代完整繼承。

**收束後續**：
1. `MOBCD-05`（PLT，編號更正：原記為 MOBCD-04，因 2026-09-02 選項化票先行取號 04）：將 G-1 之 base-path 修補回植 PLT 版 SKILL.md（常數為 `D:/DevSources/workflow/mcp-ticket-platform/`），並連同 MOBCD-04 之三項修正（數字選項化、定位擷取、步驟 10）一併同步，以 PLT 側任一 PENDING 票補測步驟 5。
2. MOBCD-02 之五項遺留行為性 AC，於本票 AC-3～AC-6 通過後，以**差分等價**論證結案：兩版 SKILL.md 差異僅在 `project` 與 base-path 兩個資料源參數，四項負向路徑邏輯逐字相同，故 APP 側實測結果可推及 PLT 版。**惟 AC-7（MISMATCH）若未觸發，兩側均維持未驗，不得以差分等價補足。**
