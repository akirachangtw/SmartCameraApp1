# INFRA-REGISTRY-LIFECYCLE-GATE-APP-01: 收尾閘 Stop hook（REVIEW 轉態逐票即時攔截；開工閘明示排除）

**簽發日期**: 2026-09-04
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: 待 Akira 指定（APP 非 RAPID，本票 `reviews.traycer` 依 APP 常規為 `PENDING`）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: PLT `INFRA-REGISTRY-LIFECYCLE-GATE-01`（CLOSED 2026-09-02，`docs/deliver/`）之 Rev D 機制規格與 Rev D.1 契約紀律；其 PLT 端 AI-3／AI-4 實測 PASS 為本票開立前提（D-G 序，Akira 2026-09-01 裁決）；Akira 2026-09-03 長 session 工作流 L2 裁決
- `coordination_dependency`:
  - `INFRA-COMPACT-CLOSURE-GATE-APP-01`（APP 冊 seq 31，PENDING）——**非 ALLOWED 交集，職責互補**：該閘於 `/compact` 當下批次檢核，本閘於每次 stop 逐票即時攔截。**兩票順序不拘**，但本票 AI-1 之判定式 MUST 與該票之觸發訊號**保持語義一致**（票檔進 `deliver/` ＋ AC 全勾），避免兩閘對同一情境給出相反判定
  - `INFRA-QA-PERTICKET-CONF-01`（APP 冊 seq 23，PENDING）——**語義耦合，決定 AI-1 讀取序**：該票將當前票指標遷移至 `tmp/verify/active.json`（六欄位原子寫入），舊 `tmp/verify_cmd.conf` 降為 fallback 且明令不刪。本票 AI-1 之 `TICKET_ID` 讀取 MUST 為「`active.json` 優先 → `verify_cmd.conf` fallback」雙路徑，**兩票任一先落地皆不需改本票**

## 檔案治理／來源說明

**本票為 PLT 票之部分移植，非全量同型。** PLT 票交付兩個閘：開工閘（`PreToolUse`，`lifecycle: PENDING → IN_PROGRESS`）與收尾閘（`Stop`，`→ REVIEW`）。**開工閘於 APP 明示排除**：APP `.claude/rules/ssot-governance.md`「Ticket Rigor Gate（選用制，INFRA-GOVSYNC-01）」章節前段載明 APP 冊治理模型 lifecycle 維持 `PENDING / REVIEW / CLOSED / VOID`，**不引入** PLT 五段制之 `TICKET_RIGOR / IN_PROGRESS / EXECUTED`，係 Akira 2026-08-27 治理減量裁決之刻意裁剪。開工閘（`PENDING→IN_PROGRESS`）之轉態目標並非本治理模型下的正式操作，故仍應排除；移植之即為偷渡已被 L2 裁剪之狀態。**校準（2026-09-05 Rigor Gate 覆核）**：現場 `TICKET_REGISTRY.json` 存在少量歷史／例外性 `IN_PROGRESS`（2 筆：`ADV-CR-R0-PRECLEAR-01`、`INFRA-QA-ACTIVATION-01`）與 `BLOCKED`（1 筆：`ADV-AGENTS-CR-03`）值，非治理模型正式狀態，亦非本票引入——此為冊面既存落差，本票不承接修正責任，但 AI-1 之判定式 MUST 明文將此類非封閉集內之值一併視為未達收尾態處理（見 AI-0 動作 E、AI-1 判定式）。

**收尾閘於 APP 之必要性（2026-09-03 工作流裁決後成立，非移植慣性）**：APP 全面導入長對話工作流後，一條 session 連續處理多張票。`INFRA-COMPACT-CLOSURE-GATE-APP-01` 僅在 `/compact` 當下檢核一次——舊工作流下 `/compact` ≈ 每票一次故覆蓋率近 100%，新工作流下一次 `/compact` 可跨越數票，中間漏轉態要到下次 compact 才被發現，且屆時是一批而非一件。`Stop` 於每次 agent turn 結束觸發，是唯一能逐票即時攔截的載體。**兩閘非重複而是不同時間粒度的互補。**

**現況取證（2026-09-04 live，`aud_20260904_2be4ae73b4`／`e4fc16b056`）**：
- `.claude/settings.json`：`PreToolUse`（L87）兩組——L89 `AskUserQuestion`、L99 `mcp__cueshot-memory__(...)`（L103 `snapshot_cueshot_memory.py`）；**`Stop`（L109）兩支**——L114 `stop.sh`、L118 `telegram_stop.sh`。本票新增為 `Stop` 第三支。
- `.claude/hooks/`：現有 `snapshot_cueshot_memory.py`、`stop.sh`、`telegram_ask.sh`、`telegram_stop.sh`
- `.claude/hooks/tests/`：僅 `telegram_stop_test.sh`（1,430B），**零 Python pytest 先例**
- 冊面路徑：`plans/cso_briefing/project/TICKET_REGISTRY.json`（**≠** PLT 之 `docs/TICKET_REGISTRY.json`）；交付區為 `plans/cso_briefing/project/deliver/`（**注意 `Pending/` 首字大寫**）

**⚠️ 測試落點：PLT Rev C 教訓不可照搬。** PLT 端曾因票面要求測試落 `.claude/hooks/tests/` 而與 `pytest tests/` 之 AC 互斥，最終裁決改落根 `tests/`（依 `pyproject.toml` 之 `testpaths`）。**APP 為 Android／Kotlin 專案，無 Python 測試框架設定**，該裁決之前提於 APP 不存在。本票採 bash 自測腳本落 `.claude/hooks/tests/`（比照既有 `telegram_stop_test.sh` 唯一先例），**MUST NOT** 引入 pytest 或建立根 `tests/` 目錄。

**⚠️ Gate Self-Bootstrap 風險（APP 特有，PLT 無此項）**：`SmartCameraApp1` 為 isolated project，平台 `_assert_owner_not_isolated` 阻擋 `registry_append_note`／`rotate_note`／`archive_tickets`／`rename_ticket`／`void_ticket`／`delete_ticket`。若閘門阻擋後給出的轉態指令在 APP 端實際被平台拒絕，即構成「閘門要求的前置狀態無法在不通過該閘門的情況下達成」。故 AI-0 動作 C 為本票之硬前置。

## Verify Config
TICKET_ID=INFRA-REGISTRY-LIFECYCLE-GATE-APP-01
CREATED=2026-09-04
PRESET=feature
ALLOWED=.claude/settings.json,.claude/hooks/registry_review_stop_guard.py,.claude/hooks/tests/registry_review_stop_guard_test.sh
FORBIDDEN=.claude/hooks/stop.sh,.claude/hooks/telegram_stop.sh,.claude/hooks/snapshot_cueshot_memory.py,.claude/hooks/telegram_ask.sh,.claude/skills/**,.claude/rules/**,.claude/CLAUDE.md,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,app/**,tests/**
MAX_LINES=180
DIFF_REF=<開工時 `git rev-parse --short HEAD` 回填>
BASE_COMMIT=<同上>
TARGET_BLOB=<開工時 `git hash-object .claude/settings.json` 回填，格式 path:sha8；另二檔為新建，標 NEW>
ANCHOR_PROBED=2026-09-04（SMTR live grep：settings.json Stop 兩支與 PreToolUse 兩組行號在盤；hooks/tests 僅 .sh。**派工時 MUST 重跑 AI-0 並更新本欄時戳**）

**MAX_LINES 推導註記**：hook 腳本 ~95 行（判定式＋fail-open 六類＋標記檔自我淘汰）；bash 自測 ~35 行；settings.json ~8 行 ≈ 128 → ×1.4 → 180。

## Action Items

### AI-0: 前置取證（派工後、修改前）

- **動作 A（錨點復驗）**：`settings.json` L109 `"Stop"` 陣列現含**恰二支**（`stop.sh`、`telegram_stop.sh`）；`PreToolUse` 現含**恰二組**（`AskUserQuestion`、`mcp__cueshot-memory__(...)`）；`registry_review_stop_guard` 字面全 repo 命中 **0**（負向錨點，證未先行）。不符 → 依 TPL v3.8 基線漂移三分流分類回報
- **動作 B（四態制法源復驗）**：`.claude/rules/ssot-governance.md`「Ticket Rigor Gate（選用制，INFRA-GOVSYNC-01）」章節前段之四態制裁剪句逐字在盤。**不存在或已被改寫 → 停止回報升裁**（開工閘排除之法源消滅，本票範圍需重裁）
- **動作 C（Gate Self-Bootstrap 硬前置，本票最高風險項）**：於 APP 專案實測 `registry_set_status(project="SmartCameraApp1", ticket_id=<任一 PENDING 票>, lifecycle="PENDING", reviews=<原值帶回>)`——即**以原值寫回之無害轉態**，驗證 isolated project 是否允許 `set_status`。逐字記錄回傳存收尾。判定：
  - **可用** → AI-1 之 `reason` 得含 `registry_set_status` 呼叫指引，閘門為 fail-closed 阻擋
  - **被拒**（`_assert_owner_not_isolated` 或其他錯誤碼）→ **閘門 MUST 降級為 fail-open 純提示**（`decision` 不設 `block`，僅輸出警示），且 AI-1 之 `reason` MUST 改為「請以 <實際可用之通道> 完成轉態」。**MUST NOT** 在轉態通道不可用的情況下仍阻擋 stop——該情形即 Gate Self-Bootstrap Invariant 違反，閘門會把執行者鎖在門外而鑰匙在門內
  - **測試本身副作用**：以原值寫回不改變任何欄位語義，但會使 `revision` +1；此為預期，MUST 於收尾載明測試對象票號與 revision 前後值
- **動作 D（讀取序前置）**：確認 `tmp/verify_cmd.conf` 在盤且可讀出 `TICKET_ID=`；記錄 `tmp/verify/active.json` 是否已存在（`INFRA-QA-PERTICKET-CONF-01` 是否已落地），據以決定 AI-1 雙路徑之實測覆蓋範圍。**校準（2026-09-05 Rigor Gate 覆核）**：現場核實 `INFRA-QA-PERTICKET-CONF-01` 已於 `deliver/` 落地且 `tmp/verify/active.json` 已存在——本動作僅需重新確認派工當下仍成立，**MUST NOT** 因此將 AC-4／AI-3⑧ 之 `active.json` 分支標為 SKIP
- **動作 E（lifecycle 現場枚舉，2026-09-05 Rigor Gate 新增）**：執行 `grep -o '"lifecycle": "[A-Z_]*"' plans/cso_briefing/project/TICKET_REGISTRY.json | sort | uniq -c`，逐字記錄現場全部相異 `lifecycle` 值於收尾實錄。若列舉結果含 `{PENDING, REVIEW, CLOSED, VOID}` 以外之值（現場已知至少 `IN_PROGRESS`、`BLOCKED`），AI-1 判定式 MUST 明文將其一併視為未達收尾態（詳見 AI-1 判定式），**MUST NOT** 沿用「四態封閉集」之默示假設進入實作
- **動作 F（`session_id` 生產路徑實測，2026-09-05 Rigor Gate 新增，本票次高風險項）**：於**實作前**觸發一次真實 `Stop` hook（如本 session 自然結束一輪對話），截取當次 stdin payload 完整鍵名清單（值可遮蔽），確認 `session_id` 欄位是否存在。**理由**：本 repo 內 `.claude/hooks/snapshot_cueshot_memory.py`（`PreToolUse` payload）已有第一手先例，記載同類 stdin payload 之 session identity 欄位在此環境曾經缺席，若無 fallback 會造成「表象與正常放行完全相同」之靜默失效，該票因此建立 PPID fallback。判定：
  - **存在** → AI-1「至多阻止一次」機制照原設計實作，AC-8 收尾時之傾印記錄與本動作結果一併存證（可互相印證，不重複收集）
  - **缺席** → **MUST** 停止進入 AI-1 原設計，回頭裁決是否比照 `snapshot_cueshot_memory.py` 之 PPID fallback 精神設計退場方案（如改用 `transcript_path` 或其他 payload 內穩定欄位作為 session 代理鍵），**MUST NOT** 逕自接受「永久 fail-open」作為預設結果
- **[標配]** `git check-ignore -v` 對 ALLOWED 三檔 → 命中即停止回報
- [x] 完成

### AI-1: `registry_review_stop_guard.py`（新建）

- **檔案**: `.claude/hooks/registry_review_stop_guard.py`（新建）
- **當前票 `TICKET_ID` 讀取序（雙路徑，MUST 依此序）**：①`tmp/verify/active.json` 之 `ticket_id` 欄（存在且可解析時優先）→ ②`tmp/verify_cmd.conf` 之 `TICKET_ID=` 行（fallback）→ ③兩者皆無或皆不可解析 → **fail-open 放行**
- **判定式**：讀 APP 冊 `plans/cso_briefing/project/TICKET_REGISTRY.json`（**唯讀**）取該票 `lifecycle`；當下列**收尾跡象任一成立**而 `lifecycle ∉ {REVIEW, CLOSED, VOID}` → 依 AI-0 動作 C 之判定執行阻擋或純提示：
  1. 該 `TICKET_ID` 之票檔已位於 `plans/cso_briefing/project/deliver/`
  2. 票檔 AC 區塊 checkbox 全數已勾（`- [ ]` 命中＝0）
  - **unknown enum 分支（2026-09-05 Rigor Gate 新增）**：`lifecycle` 取值不限於 `{PENDING, REVIEW, CLOSED, VOID}`——依 AI-0 動作 E 現場列舉，冊面現存 `IN_PROGRESS`、`BLOCKED` 等非治理模型正式狀態之歷史值。**MUST** 將任何不屬於 `{REVIEW, CLOSED, VOID}` 之值（無論是否為治理模型正式列舉）一律視為「未達收尾態」，即沿用既有 `∉ {REVIEW, CLOSED, VOID}` 之補集判斷本身已滿足此需求，**不需**另立特判分支；此處明文僅為釐清判定式對非封閉集值之行為屬有意設計，非默示假設
- **票檔定位規則（機械規格，沿用 PLT Rev D 並依 APP 路徑適配）**：
  1. 搜尋範圍恰兩處、**不遞迴**：`plans/cso_briefing/project/Pending/<TICKET_ID>_*.md` 與 `plans/cso_briefing/project/deliver/<TICKET_ID>_*.md`（注意 `Pending/` 首字大寫）
  2. `<TICKET_ID>` 之後 **MUST** 緊接底線 `_`（區辨 `INFRA-CR-MODEL-APP-01` 與其衍生票 `...-FIX-A`）
  3. 同一目錄內多檔命中（>1）→ fail-open 放行並 log，禁猜測擇一
  4. 兩處各命中一檔 → 以 `deliver/` 為準，跡象①成立
  5. 零命中 → 跡象①②均不成立 → fail-open 放行
  6. 檔名比對採**精確字串前綴**，非大小寫不敏感之 glob（防 Windows 檔案系統不敏感造成跨平台分叉）
- **⚠️ AC 區塊界定（PLT 收尾實錄之教訓，MUST 落實）**：跡象②之 `- [ ]` 掃描 **MUST 限於 AC 區塊**（`## Acceptance Criteria` 標題至下一個 `##` 或檔末），**MUST NOT** 掃描全檔。理由：票面常保留「決策軌跡、MUST NOT 施作」之封存段落，其中可能殘留未勾 checkbox（PLT `INFRA-REGISTRY-LIFECYCLE-GATE-01` 票檔 L116 即為實例，已於其收尾實錄記為 KAIROS 候選「軌跡段去標記律」）。全檔掃描會使該類票之跡象②**永不成立**，閘門對其靜默失效
- **「至多阻止一次」狀態機制**：
  1. 鍵 **MUST 為 `session_id` ＋ `TICKET_ID` 二者**；`session_id` 取自 `Stop` hook stdin payload 之 `session_id` 欄
  2. payload 缺 `session_id` → fail-open 放行，**MUST NOT** 退化為僅 `TICKET_ID` 之鍵（否則上一 session 已消耗之額度跨到新 session 仍視為已用，安全網退化為永久失效，且該失效之表象與正常放行完全相同）
  3. 落點 `tmp/.stop_guard/<session_id>_<TICKET_ID>.blocked`（空檔即可）；目錄不存在時建立；建立或寫入失敗 → fail-open 放行
  4. **清除採自我淘汰**：每次寫入標記前先刪除 `tmp/.stop_guard/` 內檔名不以本次 `session_id` 起始之所有 `.blocked` 檔（機制自足，不依賴外部清理，避免新增 ALLOWED 外之條款）
- **契約紀律**：阻擋以官方 `Stop` hook schema 之 `decision="block"` ＋ `reason` 表達；**MUST NOT** 使用 `decision="approve"`（該值僅存在於 `PreToolUse` 契約，於 `Stop` 屬契約無效值）
- **fail-open 六類**（全部放行，2026-09-05 Rigor Gate 增列第 6 類）：兩來源皆無 `TICKET_ID`／冊面查無該票／冊面 JSON 解析失敗／票檔零命中或多檔命中／缺 `session_id`／**票檔存在但找不到 `## Acceptance Criteria` 標題或 AC 區塊掃描時擲出例外**。冊面讀取**不取鎖、不重試**
- **唯讀元件**：本 hook **MUST NOT** 對冊面作任何寫入（直寫 JSON 或經服務層皆然）
- **`tmp/` 落點確認**：`.gitignore` 對 `tmp/` 之忽略須成立；不成立 → 停止回報（禁令 runtime 態入版控）
- [x] 完成

### AI-2: `settings.json` 增列 `Stop` 第三支

- 於 `Stop`（L109）陣列**末位**增列一組，指向 `registry_review_stop_guard.py`
- 既有 L114 `stop.sh`、L118 `telegram_stop.sh` 兩支之**內容與相對順序逐字不動**；`PreToolUse` 兩組原樣不變
- 直譯器路徑比照同檔既有慣例（`python "$CLAUDE_PROJECT_DIR/..."`）；**MUST NOT** 引入 venv 釘選或鏈式 fallback（PLT `INFRA-HOOK-ABSPATH-01-FIX-A` 之三層 fallback 係為 PLT worktree 場景，APP 已於 D-F 裁定不移植）
- [x] 完成

### AI-3: bash 自測腳本

- **檔案**: `.claude/hooks/tests/registry_review_stop_guard_test.sh`（新建，比照 `telegram_stop_test.sh` 形狀）
- **MUST NOT** 引入 pytest、**MUST NOT** 建立根 `tests/` 目錄（APP 無 Python 測試框架，見上方測試落點說明）
- 覆蓋：①fail-open 六類全放行；②觸發情境（跡象成立且 lifecycle ∈ `{PENDING}`）依 AI-0 動作 C 判定回傳 `block` 或純提示；③非觸發（lifecycle ∈ `{REVIEW, CLOSED, VOID}`）放行；④同 session 同 `TICKET_ID` 第二次必放行；⑤**跨 session**：不同 `session_id` 各自享有一次額度，且舊 session 標記檔於新 session 首次寫入時被淘汰；⑥**AC 區塊界定回歸**：票檔在 AC 區塊外（如軌跡保留段）含未勾 checkbox 而 AC 區塊全勾時，跡象②**仍成立**（防全檔掃描回歸）；⑦票檔定位邊界：多檔命中放行、`<TICKET_ID>-FIX-A_*.md` 不被 `<TICKET_ID>` 誤配；⑧`active.json` 存在時優先於 `verify_cmd.conf`（**2026-09-05 Rigor Gate 校準**：現場核實 `INFRA-QA-PERTICKET-CONF-01` 已落地且 `tmp/verify/active.json` 已存在，本項 **MUST 實測**，**MUST NOT** 標 SKIP；若派工當下 `active.json` 意外不存在，MUST 停止回報而非靜默 SKIP）
- [x] 完成

## Hard Constraints
1. **MUST NOT** 實作開工閘或任何 `IN_PROGRESS` 轉態邏輯；`IN_PROGRESS`／`EXECUTED`／`TICKET_RIGOR` 字面於本票全部產出中命中 **0**
2. **MUST NOT** 修改既有兩支 `Stop` hook 之內容或相對順序
3. hook 為**唯讀元件**，禁任何冊面寫入
4. AI-0 動作 C 判定為「被拒」時，閘門 **MUST** 降級 fail-open 純提示；**MUST NOT** 在轉態通道不可用時阻擋 stop
5. 跡象②之 checkbox 掃描 **MUST** 限於 AC 區塊，禁全檔掃描
6. **MUST NOT** 引入 pytest 或建立根 `tests/` 目錄
7. **MUST NOT** 位元組級複製 PLT hook——冊面路徑、目錄大小寫、直譯器慣例、測試框架四處皆異
8. 完成後 STOP，等待 SMTR 複審

## Error Handling
- AI-0 動作 A 任一錨點不符 → 三分流分類回報
- 動作 B 之四態制裁剪句字面消滅 → 停止回報升裁
- 動作 C 回傳非預期錯誤碼（既非成功亦非 isolated 阻擋）→ 停止回報，**MUST NOT** 以「應該可以」推定
- `.gitignore` 未涵蓋 `tmp/` → 停止回報
- 新增 Stop hook 導致既有兩支行為或輸出改變 → 停止回報並回退
- **[標配]** `git check-ignore -v <任一 ALLOWED 標的>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** `settings.json` `Stop` 陣列為**三支**，末位指向 `registry_review_stop_guard.py`；既有兩支 diff 為空（`sv_report.md` 已核驗，`git show --stat 95f3594`）
- [x] **[存在性]** `IN_PROGRESS`／`EXECUTED`／`TICKET_RIGOR` 字面於本票三個標的檔命中＝0（grep 已核驗）
- [x] **[行為性]** AI-0 動作 C 之實測回傳已逐字存收尾，且閘門模式（fail-closed 阻擋）依該回傳作成（`registry_set_status` 對 `INFRA-SV-GOV-01` 原值回寫成功，rev195→196）
- [x] **[行為性]** 讀取序實證：`active.json` 存在時取其 `ticket_id`；僅 `verify_cmd.conf` 存在時取其 `TICKET_ID`；皆無時放行（AI-3 案例⑧+自測 18/18 PASS 覆蓋三案）
- [x] **[行為性]（AC 區塊界定，本票關鍵回歸）** 以一份 AC 區塊外含未勾 checkbox 之票檔測試，跡象②仍判成立——證未採全檔掃描（AI-3 案例⑥已回歸驗證）
- [x] **[行為性]** 同 session 同票第二次 stop 放行；不同 `session_id` 仍能阻止一次；舊 session 標記檔被淘汰（AI-3 案例④⑤已驗）**——附帶揭露**：Codex R1 指出 A→B→A 三向交錯情境下 A 會被再次攔截（`registry_review_stop_guard.py:95-101` 自我淘汰機制之票面明文既有設計代價，非實作偏離），R2 判 ADVISORY 非 BLOCKER；現有自測未覆蓋此三向序列，留待後續票追蹤
- [x] **[行為性]** 冊面 `lifecycle` 於閘門動作前後**逐字未變**（正面證明唯讀性）——scratch 隔離自測 + 程式碼僅 `open(path,"r")` 之靜態證明
- [ ] **[實機]** 於 APP 實際 session 觸發一次收尾跡象成立而 lifecycle 未達 `REVIEW` 之情境，驗證閘門行為與 AI-0 動作 C 判定一致——**未覆蓋**，需待下次真實票收尾時自然發生並記錄，不宜人為刻意觸發污染真實收尾流程，保留未勾如實揭露
- [~] **[可達性]** `session_id` 之 producer 可指認：`Stop` hook stdin payload 欄位——**PARTIAL**：AI-0 動作F 已即時捕獲真實 payload 確認 `session_id` 存在（36 字元字串），但 producer 為 Claude Code harness（外部系統，非本 repo 程式碼），無 file:line 可指，Codex R1 明確指出此類 producer 不得標 PASS，如實降級揭露
- [x] ~~`assembleDebug` PASS~~（N/A 顯式劃記：`.claude/` 工具鏈，零 `.kt`／`.xml` 變更；仍附可見性驗證：`assembleDebug` PASS 45s/14s 兩次獨立執行）
- [x] `git diff --stat` 僅涉及 ALLOWED 三檔（PROGRESS.md 為 Pipeline Artifact 獨立 commit `3a8e6a2`；SKILL.md 已於 sv_report.md Excluded Payload 揭露並獨立 commit `1e609bf`）

**收尾誠實揭露**：AI-0 動作C 之 `TICKET_REGISTRY.json lifecycle` producer 同樣為外部 MCP server，無 file:line；此點與 `[可達性]` 一項同源限制，但該 AC 字面僅要求「AI-0 動作C 實測回傳存收尾」（已達成），非要求 producer file:line，故維持勾選。A→B→A 跨 session 缺口與兩項外部系統 producer 無 file:line 之限制，均為 Codex R2 ADVISORY 範圍內之已知限制，非 BLOCKER，是否另開追蹤票留待 Akira 裁決。

---

**End of INFRA-REGISTRY-LIFECYCLE-GATE-APP-01**
