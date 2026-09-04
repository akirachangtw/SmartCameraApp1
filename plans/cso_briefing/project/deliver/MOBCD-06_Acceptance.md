# MOBCD-06 Acceptance — APP 側導入 Project Profile

## AI-3: 零差異驗證

- APP `.claude/skills/ticket-run/SKILL.md`: 17839 bytes
- PLT `.claude/skills/ticket-run/SKILL.md`: 17839 bytes（`get_file_info` 回傳 `size=17839`）
- `diff "D:/DevSources/workflow/mcp-ticket-platform/.claude/skills/ticket-run/SKILL.md" ".claude/skills/ticket-run/SKILL.md"` → 空輸出，差異數 **0**
- `grep_files` 掃描 APP SKILL.md：
  - `SmartCameraApp1` → 0 命中
  - `plans/cso_briefing` → 0 命中
  - `D:/DevSources` → 1 命中（L86，Gate 路徑範例：`D:/DevSources/ObsidianVault/tmp/verify_cmd.conf` 歷史說明文字，非可執行參數、不含專案名，符合票面 Gate 例外）
- **結論**：兩檔逐字相同，位元組數相等，零差異驗收 PASS。

## AI-0: 來源就緒與錨點復驗

- `MOBCD-07`（project=mcp-ticket-platform）lifecycle=CLOSED（closed_at=2026-09-04）
- PLT 定稿版 SKILL.md 含 Profile Load Gate（L13）、兩層數字選單（步驟 3.1-3.3）、`limit=50`／顯示上限 12 分離（L35, L55）、`revision` 全冊級措辭（L79）四項全數存在
- APP `.claude/skills/ticket-run/ticket-run.project.md`（覆寫前）：不存在
- `registry_list(project=SmartCameraApp1, lifecycle=PENDING)` 確認 `MOBCD-06` 在列（第 21 項）

## AI-1: 逐字複製

- 見上方零差異驗證，位元組數與內容逐字相符，零處修改。

## AI-2: APP Project Profile

- 新建 `.claude/skills/ticket-run/ticket-run.project.md`，恰五欄位：
  ```
  PROJECT=SmartCameraApp1
  REPO_ROOT=D:/DevSources/AndroidProj/SmartCameraApp1
  TICKET_FILE_BASE=D:/DevSources/AndroidProj/SmartCameraApp1/plans/cso_briefing/project
  TICKET_SEARCH_ROOT=D:/DevSources/AndroidProj/SmartCameraApp1/plans/cso_briefing/project
  VERIFY_CONF=D:/DevSources/AndroidProj/SmartCameraApp1/tmp/verify_cmd.conf
  ```
- `REPO_ROOT` 與 `git rev-parse --show-toplevel` 實測輸出（`D:/DevSources/AndroidProj/SmartCameraApp1`）相符
- 路徑一律正斜線；`VERIFY_CONF` = `${REPO_ROOT}/tmp/verify_cmd.conf`，符合母規格 §5.3 第 5 條硬性等式

## AI-4: 實機/手機端觸發驗證

2026-09-04 補測，同一手機 Remote Control session 對 MOBCD-06 本票完整走一次十步流程（Akira 裁決①授權，先將 lifecycle 由 `IN_PROGRESS` 還原為 `PENDING` 使本票重新可選，再以本票自身作為測試標的，無需事後還原他票）：

1. **Profile Gate**：`git rev-parse --show-toplevel` = `D:/DevSources/AndroidProj/SmartCameraApp1`；`read_file_safe` 讀到 `ticket-run.project.md`，五項驗證全數通過（五欄位齊全各一次、`REPO_ROOT` 相符、`TICKET_FILE_BASE`/`TICKET_SEARCH_ROOT` 皆以 `REPO_ROOT` 為前綴、`VERIFY_CONF` 等於 `${REPO_ROOT}/tmp/verify_cmd.conf`）。Gate PASS。
2. **第一層分組**：三次 `registry_list`（PENDING=22／TICKET_RIGOR=0／IN_PROGRESS=1）合併後抽 prefix，共 9 組：`INFRA(9)`／`ADV(4)`／`GAP(2)`／`MEO(2)`／`STATE(2)`／`KAIROS3(1)`／`KAIROS4(1)`／`MOBCD(1)`／`TRACTION(1)` + 全部(23)。`KAIROS3`／`KAIROS4`／`TRACTION` 三個文字 prefix 方案不可達之舊 family **正常出現且可選**，正面驗證兩層選單優勢。使用者選 `8`（MOBCD）。
3. **第二層**：MOBCD 分組僅 1 票，正確顯示 `1. MOBCD-06 [PENDING] — APP 側導入 Project Profile...`；使用者選 `1`。
4. **步驟 5 摘要**：`grep_files` 命中票名／`Mode/Effort: normal → uc ｜ Low`／前置依賴（`MOBCD-07`）／`ALLOWED=`／`MAX_LINES=80` 五欄位全數齊全，未回 `NOT_FOUND`（正向證明 `TICKET_FILE_BASE` 基準差異已被 Profile 吸收——本票 `file` 欄相對 `plans/cso_briefing/project/`，非相對 repo root）。使用者回 `1` 確認開工。
5. **窄重驗 + 開工**：`registry_list` 確認 `MOBCD-06` 仍為 `PENDING`，`revision=155`；`registry_set_status(IN_PROGRESS, expected_revision=155)` 成功 → `revision=156`；`registry_append_note` 附執行溯源。
6. **10.1 conf 同步**：`grep_files` 擷取 Verify Config 九鍵，`DIFF_REF` 以 `git rev-parse --short HEAD` 實際輸出（`90b9303`）回填（非票面佔位字面）；`write_file_safe(path="D:/DevSources/AndroidProj/SmartCameraApp1/tmp/verify_cmd.conf", mode="overwrite")` 寫入，**確認落地於 APP repo，非 PLT 或 ObsidianVault**。
7. **10.2 read-back**：重讀 conf，`TICKET_ID=MOBCD-06` 與選中票號相符。
8. **10.3 交棒選單**：五項選單正確輸出，使用者選 `1`（開始執行，進入 uc 模式），已依 `.claude/skills/uc/SKILL.md` 進入該模式。

**觸發來源之誠實揭露**：本次觸發之互動模式（使用者每輪僅回覆單一數字 `8`／`1`／`1`／`1`）與 skill「互動總則」所定義之手機端限制完全吻合；惟本 Claude Code session 本身未曝露「連線裝置別」之機械可讀欄位，故手機端來源為**行為模式吻合之合理推定**，非工具層直接證實的欄位值。若需機械級來源證明，需由 Remote Control 平台另行提供 session-origin metadata（現行 skill 與 registry 皆未承載此欄位）。

## AC 對應（最終）

- [x] **[存在性]** Profile 檔存在，恰五欄位，`TICKET_FILE_BASE` 含 `plans/cso_briefing/project`，`VERIFY_CONF` 等於 `${REPO_ROOT}/tmp/verify_cmd.conf`
- [x] **[存在性]** 兩檔位元組數相等（17839=17839）且 `diff` 差異數為 0
- [x] **[存在性]** 可執行參數中 `SmartCameraApp1`／`plans/cso_briefing`／`D:/DevSources` 均 0 命中（Gate 路徑範例 1 命中除外）
- [x] **[行為性]** Profile Gate 於 APP session 讀取驗證 — 見上方 AI-4 第 1 項
- [x] **[行為性]** 第一層分組含 `KAIROS3`／`KAIROS4`／`TRACTION` 可選 — 見上方 AI-4 第 2 項
- [x] **[行為性]** 選中票步驟 5 摘要五欄位齊全 — 見上方 AI-4 第 4 項
- [x] **[行為性]** conf 寫入 APP `tmp/verify_cmd.conf` 而非 PLT/ObsidianVault — 見上方 AI-4 第 6 項
- [~] **[實機]** 手機端完整流程證據 — 已附完整流程紀錄（上方 AI-4 全節），惟觸發來源為行為模式推定非機械欄位證實，標記 `[~]`（裁決性通過，非未達）並揭露翻盤條件於上方誠實揭露段
- [x] ~~`assembleDebug` PASS~~（N/A：docs preset，不觸及 app/）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`.claude/skills/ticket-run/` 為 untracked，比照 PLT `MOBCD-05` AC-151 先例，本條 vacuous，改以 `git status` 核對範圍相符——僅本票三檔異動）
