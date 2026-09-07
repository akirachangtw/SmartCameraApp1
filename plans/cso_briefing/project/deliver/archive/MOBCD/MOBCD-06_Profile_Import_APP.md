# MOBCD-06: APP 側導入 Project Profile，並以逐字複製 PLT 定稿版達成兩側零差異

**簽發日期**: 2026-09-03（2026-09-04 改寫：由「NOT_FOUND 三分支同步」擴為「Profile 導入」）
**簽發者**: Claude (SKAD)
**來源議題**: MOBCD
**Mode**: normal → uc  ||  **Effort**: Low
**前置依賴**: `MOBCD-07`（PLT，定稿版 SKILL.md 為本票唯一來源，MUST 先 CLOSED）
**依賴三分明細**：
- `execution_dependency`: `MOBCD-07`（PLT）
- `evidence_dependency`: `Skill-Advisor/deliver/ticket_run/ticket_run_repo_local_project_profile_spec.md`（母規格）；`…prefix_filter_spec_addendum.md` v0.2 §11
- `coordination_dependency`: 無

**改寫說明（2026-09-04）**：本票原範圍為「將 NOT_FOUND 三分支歸因同步至 APP」，其後因 Akira 追加裁決而三度擴充（三分支 → +三狀態清單 → +五項交棒選單）。此反覆擴充本身即證明「兩份 SKILL.md 各自寫死參數、靠票務同步」之設計不可持續。Akira 2026-09-04 L2 裁定改採 repo-local Project Profile，本票遂改寫為 **APP 側導入票**：不再逐項同步個別功能，而是**逐字複製** PLT 定稿版並建立 APP profile。原三項同步內容已全數含於 PLT 定稿版中，隨複製一次到位。

**驗收方式之根本轉變**：舊模式為「逐項比對 N 處差異」（MOBCD-03 五處、MOBCD-05 五處，且 MOBCD-05 曾因清單標籤錯位而險些多插一處）；新模式為 **`diff` 為空**——兩檔逐字相同，比對成本歸零，漂移在結構上不再可能。

**票號 provenance**：`MOBCD-` 前綴已登記於 PLT `.claude/rules/ticket_pattern.md` L65。本票編號早於 `MOBCD-07` 但依賴之，說明見 MOBCD-07 票面「編號順序說明」；依 v3.6 不追溯改號律，MUST NOT 為求順序而改號。

**執行進度裁決（2026-09-04，Akira）**：桌機 Claude Code session 完成 AI-0~AI-3 碼閉環（零差異驗收 PASS），AI-4「手機端於 APP session 觸發 skill」因環境限制（桌機無法模擬手機 Remote Control 觸發）本輪無法執行。Akira 裁決①：接受本輪部分完成，AI-4 留待手機端補測；票面維持 `Pending/`，registry lifecycle 維持 `IN_PROGRESS`，不觸發 `/qa` Auto-Chain（AI-4 未竟，Ticket 未達 DONE 標準）。

## Verify Config
TICKET_ID=MOBCD-06
CREATED=2026-09-03
PRESET=docs
ALLOWED=.claude/skills/ticket-run/SKILL.md,.claude/skills/ticket-run/ticket-run.project.md,plans/cso_briefing/project/deliver/MOBCD-06_Acceptance.md
FORBIDDEN=app/,src/,gradle/,.claude/skills/uc/,D:/DevSources/workflow/mcp-ticket-platform/
MAX_LINES=80
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-09-04（來源為 MOBCD-07 定稿版，開工時以 AI-0 復驗其存在與 Profile Gate）
BASE_COMMIT=N/A
TARGET_BLOB=N/A

> **零差異驗收（本票之核心）**：完成後 APP 與 PLT 之 `SKILL.md` MUST **逐字相同**，差異數為 **0**。任何無法收進 Profile 而必須兩側各改之字面量，即為 `MOBCD-07` 未竟之處，MUST 停止並回報，**MUST NOT 於 APP 側自行修補**——那會立即重建本票所要消除的漂移。

## Action Items

### AI-0: 來源就緒與錨點復驗
- **動作**: ①確認 `MOBCD-07` 已 CLOSED；②`read_file_safe` PLT 定稿版 SKILL.md，確認含 Profile Load Gate、兩層選單、`limit=50`／顯示 12 分離、全冊級 revision 措辭四項；③確認 APP `.claude/skills/ticket-run/ticket-run.project.md` **不存在**；④`registry_list(project="SmartCameraApp1", lifecycle="PENDING")` 確認本票在列。
- **任一不符 → 停止回報。**
- [x] 完成

### AI-1: 逐字複製 PLT 定稿版
- **檔案**: `.claude/skills/ticket-run/SKILL.md`
- **現值**: MOBCD-04 終態（8,623 bytes，含專案相依 hardcode）
- **目標值**: 以 PLT `.claude/skills/ticket-run/SKILL.md` 為唯一來源**逐字覆寫**，**零處修改**。
- **Hard 要求**: MUST NOT 改動任何字元，包含專案名、路徑、範例。若複製後發現任何 PLT 專屬字面量殘留於可執行參數中，**停止並回報 MOBCD-07 未竟**，MUST NOT 於本票逕行修補。
- [x] 完成

### AI-2: 建立 APP Project Profile
- **檔案**: `.claude/skills/ticket-run/ticket-run.project.md`
- **現值**: 不存在
- **目標值**: 新建，恰五欄位：
  ```
  # Ticket Run Project Profile

  PROJECT=SmartCameraApp1
  REPO_ROOT=D:/DevSources/AndroidProj/SmartCameraApp1
  TICKET_FILE_BASE=D:/DevSources/AndroidProj/SmartCameraApp1/plans/cso_briefing/project
  TICKET_SEARCH_ROOT=D:/DevSources/AndroidProj/SmartCameraApp1/plans/cso_briefing/project
  VERIFY_CONF=D:/DevSources/AndroidProj/SmartCameraApp1/tmp/verify_cmd.conf
  ```
- **⚠️ 基準差異之關鍵**（本票最易出錯處）: APP 冊面 `file` 欄為相對 **`plans/cso_briefing/project/`** 之路徑（如 `Pending/…`），非相對 repo root；PLT 則相對 repo root（如 `docs/pending/…`）。故 APP 之 `TICKET_FILE_BASE` **不等於** `REPO_ROOT`，而是 `REPO_ROOT + "/plans/cso_briefing/project"`。此差異正是 Profile 存在之理由——它被收進設定檔，SKILL.md 遂無需知悉。
- **Hard 要求**: 路徑一律正斜線；`VERIFY_CONF` MUST 為 `${REPO_ROOT}/tmp/verify_cmd.conf`（母規格 §5.3 第 5 條之硬性等式）。
- [x] 完成

### AI-3: 零差異驗證
- **檔案**: `plans/cso_briefing/project/deliver/MOBCD-06_Acceptance.md`
- **動作**: 以 `grep_files` 對 APP SKILL.md 掃描下列字面，**全數 MUST 為 0 命中**：`SmartCameraApp1`（可執行參數中）、`plans/cso_briefing`（可執行參數中）、`D:/DevSources`（除 Gate 路徑範例外）。
- **主驗收**: 比對兩檔位元組數與內容。**位元組數不等即為未逐字複製**，停止回報。逐行比對確認差異數為 0。
- **記錄**: 兩檔各自之 bytes、`grep` 掃描結果、比對結論。
- [x] 完成

### AI-4: 實測並落檔
- **檔案**: `plans/cso_briefing/project/deliver/MOBCD-06_Acceptance.md`
- **動作**: 手機端於 APP session 觸發 skill，確認：①Profile Gate 讀到 **APP** profile 且 `REPO_ROOT` 驗證通過；②第一層分組出現 APP 之 prefix（實測應含 `INFRA`／`ADV`／`MEO`／`GAP`／`STATE`／`KAIROS3`／`KAIROS4`／`TRACTION`／`MOBCD` 等，其中 `KAIROS3`／`KAIROS4`／`TRACTION` 為文字 prefix 方案不可達者，**MUST 確認其正常出現**）；③選定分組後第二層正常；④步驟 5 摘要五欄位齊全（驗證 `TICKET_FILE_BASE` 之基準差異已被 Profile 正確吸收）。
- **標的選本票以外之票時 MUST 於同 session 還原。**
- **[實機] MUST 記錄 conf 寫入路徑為 APP 之 `tmp/verify_cmd.conf`**，而非 PLT 或 ObsidianVault（後者為 MOBCD-04 之已知誤寫落點）。
- [x] 完成

## Hard Constraints
1. MUST NOT 改動 PLT repo 任何檔案；PLT 定稿版為唯讀來源。
2. MUST NOT 於複製時修改任何字元——發現需修改處即表示 `MOBCD-07` 未竟，停止回報。
3. MUST NOT 增列母規格 §4.4 所禁之 Profile 欄位。
4. MUST NOT 於任何等待輸入點接受非數字值。
5. MUST NOT 建立 lock、`selection_state` 或任何外部狀態載體。
6. AI-4 之受控構造（若有）MUST 於同一 session 還原。

## Error Handling
- `MOBCD-07` 未 CLOSED → 停止回報，本票無定稿來源
- 兩檔位元組數不等 → 未逐字複製，停止回報
- APP SKILL.md 掃出 PLT 專屬可執行參數 → `MOBCD-07` 之 AI-3 未竟，停止回報，**MUST NOT 於 APP 側修補**
- Profile Gate 回 `PROFILE_INVALID：REPO_ROOT 不符` → 檢查 `git rev-parse --show-toplevel` 於 APP 之回傳格式（斜線方向），停止回報
- 步驟 5 回 `NOT_FOUND` → `TICKET_FILE_BASE` 之基準層級有誤（未含 `plans/cso_briefing/project`），停止回報
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** APP `.claude/skills/ticket-run/ticket-run.project.md` 存在，恰五欄位，`TICKET_FILE_BASE` 含 `plans/cso_briefing/project`，`VERIFY_CONF` 等於 `${REPO_ROOT}/tmp/verify_cmd.conf`
- [x] **[存在性]** APP 與 PLT 之 `SKILL.md` **位元組數相等**且逐行比對差異數為 **0**
- [x] **[存在性]** APP SKILL.md 之可執行參數中，`SmartCameraApp1`／`plans/cso_briefing`／`D:/DevSources` 均 **0 命中**（Gate 之路徑範例除外）
- [x] **[行為性]** given 於 APP session 觸發 skill，expect Profile Gate 讀到 APP profile 且 `REPO_ROOT` 驗證通過，expect `registry_list` 之 `project` 為 `SmartCameraApp1`
- [x] **[行為性]** given APP 冊面現況（實測 22 張 PENDING、≥9 個不重複 prefix），expect 第一層列出分組，且 `KAIROS3`／`KAIROS4`／`TRACTION` **正常出現且可選**（文字 prefix 方案對此三者不可達，此為兩層選單之關鍵優勢驗證）
- [x] **[行為性]** given 選中任一 APP 票，expect 步驟 5 摘要五欄位齊全且**不**回 `NOT_FOUND`（`TICKET_FILE_BASE` 基準差異已被 Profile 吸收之正向證明）
- [x] **[行為性]** given 步驟 10.1 執行，expect conf 寫入 `D:/DevSources/AndroidProj/SmartCameraApp1/tmp/verify_cmd.conf`，expect **不**寫入 PLT 或 `ObsidianVault`
- [x] **[實機]** 手機端完成一次完整流程，證據落 `plans/cso_briefing/project/deliver/MOBCD-06_Acceptance.md`（Akira 2026-09-04 對話直接確認觸發來源為手機端操作，解除 SV/Cross-Review 原標記之 session-origin 待證狀態）
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset，不觸及 app/)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（實測 `git diff --name-only d525f64..848faa0` 精確等於 ALLOWED 三檔，`.claude/skills/ticket-run/` 已 tracked 並落於本票 commit 範圍內，非 untracked／非 vacuous）

---

**收束後續**：本票 CLOSED 後，`ticket-run` 於兩側達成**零差異**，MOBCD 議題線收束。爾後任何改良一律**先改 PLT（SSOT）再複製至 APP**，驗收恆為 `diff` 為空；「逐項同步」票種（MOBCD-03／05／原 06）永久消失。若未來出現無法收進 Profile 之專案相依需求，MUST 先評估其是否應成為第六個 Profile 欄位，而非在 SKILL.md 中重建 hardcode。
