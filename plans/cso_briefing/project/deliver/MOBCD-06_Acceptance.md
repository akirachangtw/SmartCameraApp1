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

**[待補]**：本輪（桌機 Claude Code session）完成 AI-0~AI-3 碼閉環與靜態驗證；AI-4 要求「手機端於 APP session 觸發 skill」之四項行為性驗證（Profile Gate 讀到 APP profile、第一層分組含 `KAIROS3`/`KAIROS4`/`TRACTION` 等 prefix、第二層正常、步驟 5 摘要五欄位齊全）**MUST** 由實際手機端 Remote Control 觸發完成，非桌機 session 可替代實測。此為誠實揭露之未完成項，追蹤去向：待下次手機端觸發 `/ticket-run` 時一併驗證並回填本節。

## AC 對應（初步）

- [x] **[存在性]** Profile 檔存在，恰五欄位，`TICKET_FILE_BASE` 含 `plans/cso_briefing/project`，`VERIFY_CONF` 等於 `${REPO_ROOT}/tmp/verify_cmd.conf`
- [x] **[存在性]** 兩檔位元組數相等（17839=17839）且 `diff` 差異數為 0
- [x] **[存在性]** 可執行參數中 `SmartCameraApp1`／`plans/cso_briefing`／`D:/DevSources` 均 0 命中（Gate 路徑範例 1 命中除外）
- [ ] **[行為性]** Profile Gate 於 APP session 讀取驗證 — 待手機端實觸發（AI-4 待補）
- [ ] **[行為性]** 第一層分組含 `KAIROS3`／`KAIROS4`／`TRACTION` 可選 — 待手機端實觸發（AI-4 待補）
- [ ] **[行為性]** 選中票步驟 5 摘要五欄位齊全 — 待手機端實觸發（AI-4 待補）
- [ ] **[行為性]** conf 寫入 APP `tmp/verify_cmd.conf` 而非 PLT/ObsidianVault — 待手機端實觸發（AI-4 待補）
- [ ] **[實機]** 手機端完整流程證據 — 待補（本節即為承載位置，AI-4 完成後回填）
- [x] ~~`assembleDebug` PASS~~（N/A：docs preset，不觸及 app/）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`.claude/skills/ticket-run/` 為 untracked，比照 PLT `MOBCD-05` AC-151 先例，本條 vacuous，改以 `git status` 核對範圍相符——僅本票三檔異動）
