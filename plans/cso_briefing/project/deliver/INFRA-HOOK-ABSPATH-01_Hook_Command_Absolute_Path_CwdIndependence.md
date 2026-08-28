# INFRA-HOOK-ABSPATH-01: PreToolUse／Stop Hook＋statusLine 指令絕對路徑化（消除 cwd 依賴之 fail-open 缺口，APP 端）

**簽發日期**: 2026-08-28
**簽發者**: Claude（SMTR，Akira 對話裁決 2026-08-28 授權開票）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（RAPID §0：單檔字面路徑替換，`git restore` 五分鐘域）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: mcp-ticket-platform `INFRA-HOOK-ABSPATH-01`（Rev A，同型缺陷之發現源與修法先例）；`CUE-SNAP-01-FIX-A`／`-FIX-B`（L101 標的之 hook 本體與 CWD 去耦裁決來源）
- `coordination_dependency`: mcp-ticket-platform `INFRA-HOOK-ABSPATH-01`（D-6 交叉派工：同型缺陷兩端各自立票；PLT 端 POV 之 `$CLAUDE_PROJECT_DIR` 取證結果 MUST 回填本票 AI-1 ⚠️ 條款後方派工）

## 檔案治理/來源說明

- **缺陷來源**：PLT 端 2026-08-18 實測事故（cwd 偏移時 `bash` 對相對路徑解析失敗，hook 腳本從未啟動，harness 回 non-blocking 放行）。2026-08-28 Akira 指示比對 APP 端：`grep_files` 取證（`aud_20260828_8cf4fc6d10`）確認 APP `.claude/settings.json` **五處 `command` 值全部相對路徑**，同型缺陷成立。
- **APP 端特有風險**：L101 為 `CUE-SNAP-01` 家族之 Cueshot-memory 快照 hook。FIX-A AI-2 與 FIX-B 已於**腳本內部**消滅 CWD 耦合，但**呼叫該腳本的 command 本身仍相對於 cwd**——cwd 偏移時腳本根本不啟動，`Cueshot_Memory_Concurrent_Write_Model` D-F.2 之首次 mutation 快照即靜默失守（fail-open）。此為 FIX-A/B 修補面之外的殘留縫隙，非 FIX-A/B 之缺陷。
- **與 PLT 票之差異**：APP 無 `mcp_service_pid_guard.py`／`.venv` 直譯器項；多出 L101 snapshot hook；行號全異（L91／L101／L112／L116／L124）。依 D-6 內容依本端實況調整。

## Verify Config
TICKET_ID=INFRA-HOOK-ABSPATH-01
CREATED=2026-08-28
PRESET=docs
ALLOWED=.claude/settings.json
FORBIDDEN=.claude/hooks/**,.claude/skills/status/**,.mcp-memory/**,app/**,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=N/A（單檔 5 行字面替換，docs preset 比例原則）
DIFF_REF=762fa37
ANCHOR_PROBED=2026-08-28（`grep_files` `aud_20260828_8cf4fc6d10`；派工時執行端逐一字串復驗）
BASE_COMMIT=762fa370484ccbdcec24ea14a558eeb800a0db8a
TARGET_BLOB=N/A（docs preset 單檔小改，V1 降級為 AI-0 錨點制）

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/settings.json`
- **動作**: 逐字確認 `"hooks": {` L84（唯一）、`"statusLine": {` L122（唯一）＋五個 `"command"` 值（L91／L101／L112／L116／L124）現為相對路徑：
  - L91 `"bash .claude/hooks/telegram_ask.sh"`
  - L101 `"python .claude/hooks/snapshot_cueshot_memory.py"`
  - L112 `"bash .claude/hooks/stop.sh"`
  - L116 `"bash .claude/hooks/telegram_stop.sh"`
  - L124 `"python .claude/skills/status/scripts/statusline.py"`
- 任一不符 → 依基線漂移三分流停止分類回報（`git diff --name-only 762fa37..HEAD -- .claude/settings.json`）
- [x] 完成

### AI-1: 五處指令改絕對路徑化
- **檔案**: `.claude/settings.json`
- **現值 → 目標值**（`$CLAUDE_PROJECT_DIR` 組出絕對路徑，值須包引號）：
  - L91：`"bash .claude/hooks/telegram_ask.sh"` → `"bash \"$CLAUDE_PROJECT_DIR/.claude/hooks/telegram_ask.sh\""`
  - L101：`"python .claude/hooks/snapshot_cueshot_memory.py"` → `"python \"$CLAUDE_PROJECT_DIR/.claude/hooks/snapshot_cueshot_memory.py\""`
  - L112：`"bash .claude/hooks/stop.sh"` → `"bash \"$CLAUDE_PROJECT_DIR/.claude/hooks/stop.sh\""`
  - L116：`"bash .claude/hooks/telegram_stop.sh"` → `"bash \"$CLAUDE_PROJECT_DIR/.claude/hooks/telegram_stop.sh\""`
  - L124：`"python .claude/skills/status/scripts/statusline.py"` → `"python \"$CLAUDE_PROJECT_DIR/.claude/skills/status/scripts/statusline.py\""` — **Rev B 復活**（2026-08-28 Akira 正向更正：所觀察之「異常」為新開 Claude Code session 尚未累計用量、statusline `ctx 0 | $0.00 | 0m` 欄位顯示為零，非路徑解析失敗；PLT 端同現象已於 `fc5108e` 以最小 payload 重現並診斷為 harness 零帳務時刻之預期渲染。defer 前提消滅，L124 恢復為待執行動作）
- 僅改路徑字面；`matcher`（含 L97 之 `mcp__cueshot-memory__(...)` 字面）／`async`／`padding`／JSON 結構零觸碰
- **Rev B（2026-08-28，Akira 更正取證後 SMTR 修訂）**：原 ⚠️ 前置條款已消解。PLT 端 `fc5108e` 已將同型 L127 改為絕對路徑並多輪驗證渲染正常，`$CLAUDE_PROJECT_DIR` **確實**為 statusLine harness 所繼承；APP 端先前之「不繼承」結論係將零帳務渲染誤判為路徑失敗，該結論撤銷、不得再被後續票引為依據。
- **Rev B 唯一剩餘動作**：L124 依上列目標值改為絕對路徑，四處 hooks（`e0ca8d3` 已落地）零觸碰。
- 判別式（若再現疑似異常，用此區分，禁再以「異常」二字結論）：status line **整條空白／消失** ⇒ 路徑解析失敗，`git restore` 僅 L124；**model/repo/branch 段落正常顯示而 ctx/$ 為 0** ⇒ 解析成功之零帳務渲染，非缺陷。
- [x] 完成（四處已落地 `e0ca8d3`；L124 補於 `4de201a`）

### AI-2: 行為性驗證
- 於非 repo-root cwd（如 `app/src/main/`）發起一次任意 Bash 工具呼叫，確認 PreToolUse／Stop hook 觸發且無 `No such file or directory`
- **L101 專項**：於非 repo-root cwd 下對 `cueshot-memory` 發一次寫類 mutation（依 FIX-A HC-5 禁直呼腳本代替），確認 `.mcp-memory/memory.jsonl.<YYYYmmdd_HHMMSS>.bak` 新產生（首次 latch）——此為 D-F.2 快照在 cwd 偏移情境下不再失守之正向證據
- `python -c "import json; json.load(open('.claude/settings.json'))"` 確認 JSON 合法
- status line 於 L124 改後至少一次對話輪次正常渲染（Rev B：defer 已撤銷，本子項恢復必驗，依 AI-1 判別式判讀）
- [x] 完成（前三子項已於 `e0ca8d3` 驗畢；status line 子項於 `4de201a` 後 Akira 現場確認正常渲染 model/repo/branch，依判別式為解析成功之零帳務渲染）

## Hard Constraints
1. 僅動 `.claude/settings.json` 五個 `command` 值；hook 與 statusline 腳本內容零觸碰（FIX-A/B 之守門邏輯為凍結基線）
2. 禁引入新 hook、新 matcher 或改變既有觸發時機；禁改 L97 matcher 字面
3. `$CLAUDE_PROJECT_DIR` 值須包引號（路徑含空白時安全）
4. AI-2 L101 專項之 mutation 前 MUST 依 D-F.2 先確認本 session 為 Cueshot 宣告寫入端（禁隱式接管）

## Error Handling
- AI-0 錨點不符 → 基線漂移三分流停止分類回報
- ~~PLT 端 `$CLAUDE_PROJECT_DIR` statusLine 取證未回填 → 停止，禁派工~~（Rev B 解除：PLT `fc5108e` 已回填為「繼承」，前置消滅）
- JSON 格式驗證失敗 → `git restore .claude/settings.json` 後停止回報
- AI-2 L101 專項未見新 `.bak` 且 hook 無 stderr 輸出 → 停止回報（不得推定「本 session 已 latch」）
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v .claude/settings.json` 命中 → 停止回報（gitignore 死結律 #20）

## Acceptance Criteria
- [x] **[存在性]**（Rev B 更正）五個 `command` 值均含 `$CLAUDE_PROJECT_DIR` 前綴，`grep -c CLAUDE_PROJECT_DIR .claude/settings.json` = 5 — 實測（`4de201a` 後）：`grep -c CLAUDE_PROJECT_DIR .claude/settings.json` = 5，與五處生效項數相等
- [x] **[行為性]** JSON 合法性驗證通過 — 實測：`python -c "import json; json.load(open('.claude/settings.json'))"` 無例外
- [x] **[行為性]** given 非 repo-root cwd 之 Bash 呼叫，expect hook 觸發且無 `No such file or directory`（改前可重現、改後不重現）— 實測：`cd app/src/main && ls` 後無錯誤輸出
- [x] **[行為性]** given 非 repo-root cwd 之首次 cueshot-memory 寫類 mutation，expect `.mcp-memory/` 新增一筆 hook 命名樣式之 `.bak`（producer：`.claude/hooks/snapshot_cueshot_memory.py` 輪替命名 regex）— 實測：`mcp__cueshot-memory__create_entities` 觸發，產生 `memory.jsonl.20260828_193042.bak` + 對應 latch，符合 regex 命名樣式；探測實體事後 `delete_entities` 撤回，latch 生效未再新增 `.bak`
- [x] **[行為性]** status line 於 L124 改絕對路徑後正常渲染（Rev B：N/A 判定撤銷，本項恢復必驗，依上列判別式判讀）— 實測：改後 status line 正常顯示 model/repo/branch，Akira 現場確認，屬解析成功之零帳務渲染（非路徑解析失敗）
- [x] `git diff --stat` 僅涉 `.claude/settings.json` 一檔（`e0ca8d3` 已達成 4+/4-；Rev B 補 L124 後累計 5+/5-）— 實測：`git diff --stat 762fa37..HEAD -- .claude/settings.json` = 1 file changed, 5 insertions(+), 5 deletions(-)
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)

**End of INFRA-HOOK-ABSPATH-01（APP）**
