# RGR-APPDEPLOY-01: 將 ticket-rigor-gate Skill（含 runner）由 PLT 移植至 APP，產物一律落 `.tmp/rigor-gate/` 輪替（KEEP_N=10）

**簽發日期**: 2026-08-27
**簽發者**: Claude (SKAD)
**來源議題**: RGR
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `Skill-Advisor/deliver/PLT_TO_APP_MIGRATION_INVENTORY_AND_DIRECTION_20260826.md` §3 #5；`WKTR-APPDEPLOY-01`（APP 端 skill 移轉先例＋`.claude/` hash 驗收方法）
- `coordination_dependency`: 待開之 APP-3 治理規則同步票（票號未定；其 `TICKET_RIGOR` 段以本票為前提，開立時 MUST 回填本欄）

## 檔案治理／來源說明（含本票之 L2 設計裁決）

源端 `mcp-ticket-platform/.claude/skills/ticket-rigor-gate/` 全樹 **11 檔**（`visited_files=11`，`aud_20260827_d25368c135`；盤點檔 §3 #5 記「5 檔」與實測不符，以實測為準）。**7 檔純複製、4 檔修正**。

**L2 裁決（Akira 2026-08-27）——中間產物不永久保存**：RGR quality report 與執行遙測**同屬執行期產物**，統一落 `.tmp/rigor-gate/` 並由 housekeeping 輪替（`KEEP_N` 5→10）；**耐久結論之唯一載體＝票面收尾實錄**，回溯品質依票面註記完整度，不依中間產物。禁任何票面 AC 引用 `.tmp/` 下 RGR 產物為長期證據。已知權衡（L2 明示接受）：CB-1／CB-2／CB-4 之凍結記憶域縮短為 report 存續期，非永久 latch；翻盤條件＝若同一票遭重複自動修補造成實害，重啟耐久 latch 討論。runner L224 守門（report 不得落 `TELEMETRY_ROOT` 之內）**不修改**，以平行子目錄滿足其字面。

**落點設計**：`TELEMETRY_ROOT` → `<repo>/.tmp/rigor-gate/ticket-repair/`（housekeeping 清掃域）；report → `<repo>/.tmp/rigor-gate/RGR_<ticket-id>_<YYYYMMDD>.md`（`ticket-repair/` 之外，L224 通過）。`.tmp/` 已在 APP `.gitignore` 排除——**本票不動 `.gitignore`**，與 APP-3 零檔案交集。既有佔用者 `cross_review.sh` 之清理為檔名限定 `cross_review_*.md`、明文禁遞迴禁 `rm -rf`（L131/L139/L208，`aud_20260827_3f5dc4e4e2`），與 `rigor-gate/` 子目錄共存安全。

**開票期 live 取證（2026-08-27 14:26，SKAD 第一手）**：
- PLT 綁定全樹掃描共 **5 處**（盤點檔載 3 處，本次補 2 處）：`SKILL.md` L369／L568／L578（`aud_20260827_785b155077`）＋ `repair_runner.sh:32`＋`README.md:58`（`aud_20260827_3f394ca040`）。其餘 7 檔（EXAMPLE／OPS_CALIBRATION／REPAIR_ROUTING／profiles／prompts／schemas×2）零命中。
- `REPO_ROOT` 推導（`repair_runner.sh:31` 四層上溯）為 repo-agnostic，APP 端路徑結構相同，**不列改寫項**（`aud_20260827_0ae13c1d7d`）。
- `KEEP_N` 相關字面三處：`housekeeping.sh` L2（`N=5`）／L6（`保留最近 5 個 run`）／L13（`KEEP_N=5`）（`aud_20260827_c794e95520`）。
- 外部命令 fail-closed 前置：`jq`(L63)／`sha256sum`(L64)／`diff`(L65)／`codex.cmd`(L580，禁裸 `codex`)。
- 律 A 取號：APP 冊＋`Pending/`＋`deliver/` 全樹 `RGR-*` 票檔零命中；PLT 端 RGR 家族均為語義子家族制（INSTALL／RUNNER／ARTIFACTS／…，`visited_files=536`，`aud_20260827_8559df1bb0`），`RGR-APPDEPLOY-01` 零碰撞，命名沿 `WKTR-APPDEPLOY-01` 先例。
- **RGR family 於 APP 端未登記**：`ticket_pattern.md` Family Provenance 現僅 `MEO-`／`ADV-`／`WKTR-` 三列（`aud_20260827_dd55e14b47`）。依 TPL v3.6 縮寫規則＋`CLAUDE.md` Rule 4a，首用當刻 MUST 補登（AI-6；僅補表列，L6/L10 明文新增 family 不改 regex）。
- 源端 11 檔位元組數（AI-0(B) 比對基準）：`SKILL.md` 30,903／`EXAMPLE.md` 16,841／`REPAIR_ROUTING.md` 10,259／`OPS_CALIBRATION.md` 2,682／`runner/repair_runner.sh` 30,814／`runner/housekeeping.sh` 2,733／`runner/README.md` 3,750／`runner/profiles/model-profiles.v1.json` 258／`runner/prompts/ticket-repair-prompt.v1.txt` 1,901／`runner/schemas/ticket-repair-payload.v1.json` 2,666／`runner/schemas/ticket-repair-response.v1.json` 2,121。

## Verify Config
TICKET_ID=RGR-APPDEPLOY-01
CREATED=2026-08-27
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/,.claude/rules/ticket_pattern.md
FORBIDDEN=.claude/rules/ssot-governance.md,.claude/rules/mcp-service-safety.md,.claude/skills/sv/,.claude/skills/qa/,.gitignore,plans/cso_briefing/,app/
MAX_LINES=80
DIFF_REF=9dce488a
ANCHOR_PROBED=2026-08-27 14:26
BASE_COMMIT=9dce488a
TARGET_BLOB=N/A

**MAX_LINES**：估算遙測非判準。修正面＝5 處路徑字面＋KEEP_N 三處＋provenance 一列 ≈ 12 行，1.5× 進位；純複製 7 檔不計（hash 判準承載）。
**TARGET_BLOB=N/A 理由**：同 `WKTR-APPDEPLOY-01` 先例——標的於 APP 端不存在（pre-image 空集），源端 hash 為 AC 量測對象本身，由 AI-0(B) live 計算，V1 降級為 AI-0 錨點制。

## Action Items

### AI-0(A): 錨點復驗（派工後、修改前）— 標的側
- `.claude/skills/ticket-rigor-gate/` 於 APP 端**不存在**。
- `ticket_pattern.md` 錨點：`| \`WKTR-\` | \`WKTR-01\`` 行恰 1 次命中（provenance 表末列，AI-6 插入點）；全檔 `RGR-` 命中 0。
- 基線漂移三分流（TPL v3.8）：不符時 ①`git diff --name-only 9dce488a..HEAD -- .claude/skills/ticket-rigor-gate .claude/rules/ticket_pattern.md` 空 → `ANCHOR_STALE_SCOPE_INTACT`；②非空依 commit 前綴：本票 ID → `ALREADY_EXECUTED_PENDING_CLOSURE`／他票 ID → `CANDIDATE_SUPERSEDED`／無前綴 → `BLOCKED_UNATTUTED_STATE`（唯讀取證後升裁）。三態均停止＋分類回報。
- [x] 完成

### AI-0(B): 源端基線建立（唯讀）
- 對源端 `/d/DevSources/workflow/mcp-ticket-platform/.claude/skills/ticket-rigor-gate/` 全 11 檔逐檔 `wc -c`，MUST 與票面基準表相符；**7 個純複製檔**另逐檔 `git hash-object` 記錄 SHA 於完成回報。任一位元組數不符 → 停止回報（源端漂移，禁自行採新值）。
- [x] 完成

### AI-1: 純複製 7 檔（位元組級）
- **檔案**: `EXAMPLE.md`、`OPS_CALIBRATION.md`、`REPAIR_ROUTING.md`、`runner/profiles/model-profiles.v1.json`、`runner/prompts/ticket-repair-prompt.v1.txt`、`runner/schemas/ticket-repair-payload.v1.json`、`runner/schemas/ticket-repair-response.v1.json`
- **目標值**: 源端逐位元組複本（禁改編、禁行尾轉換）
- [x] 完成

### AI-2: `SKILL.md` 三處 PLT 綁定改寫（其餘內容位元組級沿源端）
- **L369 現值**: `docs/rigor-gate/RGR_<ticket-id>_<YYYYMMDD>.md` → **目標值**: `.tmp/rigor-gate/RGR_<ticket-id>_<YYYYMMDD>.md`
- **L568 現值**: `in the \`mcp-ticket-platform\` repository` → **目標值**: `in the \`SmartCameraApp1\` repository`
- **L578 現值**: `\`docs/deliver/TKR-02_Fix_Family_Ticket_Review_20260802.md\` — … inside this repository` → **目標值**: 改註為 PLT 端歷史參照 `\`mcp-ticket-platform/docs/deliver/TKR-02_...\`（PLT 端歷史檔，非本 repo 路徑）`，其餘語句不動
- 改寫後全檔 grep `docs/rigor-gate`＝0、`mcp-ticket-platform`＝恰 1（僅 L578 歷史參照）
- [x] 完成

### AI-3: `runner/repair_runner.sh` 遙測落點改寫
- **L32 現值**: `TELEMETRY_ROOT="$REPO_ROOT/docs/rigor-gate/ticket-repair"`
- **目標值**: `TELEMETRY_ROOT="$REPO_ROOT/.tmp/rigor-gate/ticket-repair"`
- 其餘內容（含 L224 守門、CB-1~4、`REPO_ROOT` 推導）**一字不動**
- [x] 完成

### AI-4: `runner/README.md` 落點記載同步
- **L58 現值**: `docs/rigor-gate/ticket-repair/<ticket_id>/<source_run_id>/` → **目標值**: `.tmp/rigor-gate/ticket-repair/<ticket_id>/<source_run_id>/`
- [x] 完成

### AI-5: `runner/housekeeping.sh` 保留額度 5→10（波及面三處全改）
- **L2 現值**: `（P1 額度制，N=5）` → `（P1 額度制，N=10）`
- **L6 現值**: `保留最近 5 個 run` → `保留最近 10 個 run`
- **L13 現值**: `KEEP_N=5` → `KEEP_N=10`
- 改後全檔 `=5`／`N=5` 命中 0（修訂波及面全檔掃描律）
- [x] 完成

### AI-6: `ticket_pattern.md` Family Provenance 補登 RGR（純增一列）
- **插入點**: `WKTR-` 列之後
- **目標值**: `| \`RGR-\` | \`RGR-APPDEPLOY-01\` | \`RGR-APPDEPLOY-01\` | \`plans/cso_briefing/project/Pending/RGR-APPDEPLOY-01_RigorGate_Skill_Port.md\` | 2026-08-27 | TPL v3.6 前綴表預授權（RGR）；APP 端首用當刻補登（Rule 4a）；PLT 端 family 既存（RGR-INSTALL-01 起） |`
- 禁動既有各列與 Regex 區塊任何字元
- [x] 完成

## Hard Constraints
1. 純複製 7 檔位元組級同一；修正 4 檔僅限 AI-2~AI-5 列明行，其餘內容位元組級沿源端。
2. 源端唯讀；暫存限 `tmp/RGR-APPDEPLOY-01/`。
3. **禁引入五段生命週期、禁動 `ssot-governance.md`／`mcp-service-safety.md`／`.gitignore`**（APP-3 範圍；本票只落工具，不加關卡）。
4. 禁修改 L224 守門與 CB-1~CB-4 邏輯（L2 裁決以落點達成，非改碼達成）。
5. RAPID 於 APP 不適用，走全程序；commit 前綴 MUST `[RGR-APPDEPLOY-01]`。

## Error Handling
- AI-0 任一不符 → 三分流分類停止。
- `jq`／`sha256sum`／`diff`／`codex.cmd` 任一 `command -v` 缺 → 停止回報（Gate Self-Bootstrap：環境未備，禁部署後留 fail_env 死件）。
- 純複製檔 hash 不等且重試一次仍不等 → 停止回報（疑行尾／編碼轉換）。
- **[標配]** `git check-ignore -v` 對 `.claude/skills/ticket-rigor-gate/SKILL.md`、`.claude/rules/ticket_pattern.md` 任一命中 → 停止回報（KAIROS #20）。

## Acceptance Criteria
- [x] **[存在性]** 11 檔於 APP 端存在；7 純複製檔逐檔 `git hash-object` 兩端相等（SHA 逐筆列於完成回報）
- [x] **[存在性]** 5 處改寫目標值逐字命中；APP 端全樹 grep `docs/rigor-gate` 命中 0
- [x] **[存在性]** `housekeeping.sh` 之 `KEEP_N=10` 恰 1、`5` 相關舊字面三處全清；`ticket_pattern.md` RGR 列恰 1 且既有列零異動（`git diff` 該檔零 `-` 行）
- [x] **[行為性]** given APP repo 根執行 `command -v jq sha256sum diff codex.cmd`，expect 四項全數命中（缺項即本票不可收束）
- [x] **[行為性]** given `.tmp/rigor-gate/ticket-repair/PROBE-T/` 下以不同 mtime 建立 11 個假 run 目錄後執行 `bash .claude/skills/ticket-rigor-gate/runner/housekeeping.sh .tmp/rigor-gate/ticket-repair`，expect 恰保留最新 10 個、刪除最舊 1 個；驗畢刪除 `PROBE-T/` 全部並確認零殘留
- [x] **[可達性]** N/A 顯式劃記（`.claude/` 治理載體，無 `app/src/main/` producer，型別不適用）
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

## 完成後動作（收尾義務）
1. 票檔勾選後搬 `deliver/`；冊面轉態四要件原子完成；冊面走**僅含 registry 單檔**之獨立 `[GOVERNANCE-SYNC]` commit（`WKTR-APPDEPLOY-01` F2 教訓）。
2. 收尾實錄 MUST 引 commit SHA 為證據，**禁引 `sv_report.md` 或 `.tmp/` 下任何產物**（F3／KAIROS #28）。
3. MUST NOT 順手執行 APP-3 範圍之任何項目。

---
**End of RGR-APPDEPLOY-01**
