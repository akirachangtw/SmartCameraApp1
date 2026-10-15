# ADV-RESETISO-01: SessionStart reset 與 Boot 清理在 owner 他人／身分未知／現役票 conf 匹配時不刪 tmp/verify_cmd.conf、.tmp/sv_report.md、tmp/handoff.md，其餘情境維持既有 reset 行為

**簽發日期**: 2026-10-15
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（ADV-OWNER-01 已 `CLOSED`，其 `owner_session_id` 欄位與 SID 讀取已在盤）
- `evidence_dependency`: ADV-OWNER-01（`deliver/ADV-OWNER-01_stopguard.md` 終審收束附註，commit `2dba94df`：reset 可同時刪三檔、觸發來源未確認；官方契約確認 `CLAUDE_CODE_SESSION_ID` 於 hook 子行程與 payload `session_id` 一致且 `/clear` 更新）
- `coordination_dependency`: 無（plan-local T02〔R0 回件承接，尚無正式票號〕與本票無檔案重疊；本票與任何編輯 `session_start.sh`／`.claude/CLAUDE.md` 之票須串行，`concurrency.md` §2）

## 檔案治理／來源說明

- **Parent**：Plan-backed 單票。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261015-resetiso-plan.md`（Rev A；`.tmp` 非長期載體，本節使本票獨立成立）；Parent artifact status＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`；plan-local item＝`PLAN-ITEM-01`；Parent read timestamp＝2026-10-15 16:18（+08:00）。
- **Template 實讀**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，v3.11，產出日期 2026-05-29，最新 Revision 日期 2026-09-14（全檔 322 行實讀）。
- **票號**：`ADV-RESETISO-01`（使用者提示；本機取號：registry／archive／`Pending/`／`deliver/`／`void/` 對 `ADV-RESETISO` 零命中，`reports/` 不存在；`ADV` family 已登記，topic `Advisor`）。
- **Selected item 原文（PLAN-ITEM-01 goal）**：reset 與 Boot 清理在 owner 他人／身分未知／現役票 conf 時不刪三檔，其餘情境維持既有行為。hook 守門、Boot 文字與測試互鎖（只改其一則守門被繞過或測試斷言與行為不符），故單票，不按三檔或情境拆。
- **相關約束（取自 parent，Akira 2026-10-15 裁決 D1／D2）**：
  - **D1**：納入 `.claude/CLAUDE.md` Boot 第 7 行：移除模型端 `rm`，僅保留 `mkdir -p tmp`；清理由 SessionStart reset 的隔離判定承擔；不在 resume／compact／fork 新增替代清理，不新增 GC。
  - **D2**：保護條件（括號化，唯一判準）：`valid_active AND ( (owner 非空 AND (SID 空 OR SID != owner)) OR (owner 空 AND conf_ticket 非空 AND conf_ticket == active_ticket) )`；`valid_active`＝`active.json` 可解析且 `ticket_id` 非空。其餘（owner 相同；owner 空且 conf 不匹配；`active.json` 缺檔／不可解析）維持既有清理。不引入 lifecycle／Registry 查詢；不 claim、不改 owner、不清理後重建。
  - 非目標：不改 `gate_ledger.py`、兩支 Stop hook、`settings.json`、K、ledger 格式、Router、R0／Advisor／T02；不動 ADV-OWNER-01；不新增 coordinator／鎖／daemon／DB／第二套 state／人工確認；不做全 repo 暫存檔治理；不新增清理保證。

### 與 parent（plan Rev A）的差異（本票新增或收準，不以「衝突處以本段為準」概括）

| # | plan Rev A 原文 | 本票處置 | 依據 |
|---|---|---|---|
| 1 | §5「殘餘限制」：owner 已登記的票 CLOSED 後三檔會留到下次 activation 覆寫或手動清理 | **改為較弱且可證實的敘述**：保護分支**可能**留存殘檔；各檔何時被覆寫取決於既有 producer（`activate_ticket.sh`／`/qa`／`ticket-run` 寫 conf；`/sv` 寫 sv_report；`handoff.md` 無已知 producer），亦可能由符合條件的 reset 或手動清理移除。本票不新增清理保證，不改 D2 | Akira 2026-10-15 開票指示 #1 |
| 2 | §5：D1 之 `.claude/CLAUDE.md` 屬 Checkpoint 觸發檔，ticket 階段「明載授權依據」 | 授權依據已於本票明載（Akira 2026-10-15 D1，僅限 Boot 第 7 行單行）；Error Handling 另規定 R0 回報 CHECKPOINT-REQUIRED 時的處理（引用既有裁決，不偽造事件、不自動豁免 checkpoint、不重問相同政策；超出該單行或有新衝突才重新判斷） | Akira 開票指示 #2 |
| 3 | §6 第 6 項「`/clear` 後新 SID」 | 以 `CLAUDE_CODE_SESSION_ID` env 模擬，AC 標 **[stub]**；真實 `/clear` 鏈（SessionStart `clear` matcher 實際觸發與 env 更新）不在本票驗證範圍 | Akira 開票指示 #3 |
| 4 | §4 測試落點僅寫「更新 reset 合法刪除集合斷言」 | 明列：`session_start_owner_test.sh` 現有「副作用」斷言（`session_start.sh reset` 恰刪三檔，且 given owner=`sid-A`／SID=`sid-B`）**與新行為衝突**，須改寫為情境矩陣；`test_gate_ledger.py::CarrierSurvival`（FORBIDDEN，僅執行）為回歸驗證，非相容政策依據 | 本票 live 取證（見證據分級 ②） |

**證據分級**（evidence 2026-10-15 16:18；repo `D:\DevSources\AndroidProj\SmartCameraApp1`，main @ `6c541133`，工作樹 clean）：
- ① **CONFIRMED（官方契約，ADV-OWNER-01 當日抽驗，本票重用不重研）**：`CLAUDE_CODE_SESSION_ID` 於 hook command 子行程設為當前 session ID，與 hook 輸入 `session_id` 一致，`/clear` 更新。**非本機實測**。
- ② **CONFIRMED（本地原始碼 live 直讀）**：
  - `session_start.sh`：`reset` 分支（L90-93）`mkdir -p tmp` 後無條件 `rm -f tmp/verify_cmd.conf .tmp/sv_report.md tmp/handoff.md`，再 `echo "🧹 SessionStart(reset)：tmp/ 預載檔已清"`；`inject` 分支（L115 起）不刪檔；檔頭註解 L9 描述 reset 清三檔；`report_owner_status`（L19）已有 `owner_session_id` 的 sed 讀取與 `CLAUDE_CODE_SESSION_ID` 讀取可重用。
  - `.claude/CLAUDE.md` L7 為唯一含 `rm -f tmp/verify_cmd.conf` 之行（`grep -c`＝1；全檔 142 行，上限 150）：`Session 啟動 → \`mkdir -p tmp && rm -f tmp/verify_cmd.conf .tmp/sv_report.md tmp/handoff.md\` → 讀 PROGRESS.md → 輸出 Boot Report → **停止等 Ticket**。`
  - 已掃描 `.claude` 下 shell／python／md 對 `rm -f tmp/verify_cmd` 之命中僅 `session_start.sh` 與 `.claude/CLAUDE.md`；`tmp/handoff.md` 無已知 producer／consumer（僅此兩處 `rm` 與測試 fixture 提及）。
  - `verify_cmd.conf` 內含 `TICKET_ID=`（單槽位）；per-ticket 快照 `tmp/verify/<TICKET_ID>.rev-0.conf` 不被 reset 刪除。
  - `session_start_owner_test.sh`：`setup` 於暫存 repo 放 `verify_cmd.conf`（`TICKET_ID=$TID`）、`.tmp/sv_report.md`、`tmp/handoff.md`；L72-76 之「side effects」段以 `active sid-A`＋`run $mode sid-B` 斷言 `reset` 恰刪三檔——**與本票新行為衝突，須改寫**。
  - `test_gate_ledger.py::CarrierSurvival.test_reset_inject_and_cleanup_leave_ledger`（L606）：`active.json={"ticket_id": tid}`（無 owner）＋conf 內容 `x`（無 `TICKET_ID`），跑真實 reset 後斷言 conf 已刪——依 D2 本票行為（owner 空且 conf_ticket 空→維持既有清理）此測試仍成立，ticket 階段須實跑確認。
  - **consumer 影響（live 讀取，F2 揭露）**：D2 在 owner 非空時**不比對** `conf_ticket`，故保護分支可能保留與 active 票不一致的 conf；其 consumer `/uc` 無參數啟動「`tmp/verify_cmd.conf` 沿用現況」（`uc/SKILL.md:91`），且 Ticket DONE 前自 conf 讀 `TICKET_ID`（`uc/SKILL.md:48`）——舊 reset 清殘留以降低跨票綁定之保護，在保護分支下不再成立。
  - `.gitignore:81` 命中 `plans/cso_briefing/project/*`（Pending 票檔 untracked 為既定慣例）；三個 ALLOWED 檔 `git check-ignore` 零命中，皆已追蹤。
- ③ **SUPPORTED（推論／parent 回流，未重驗）**：`/clear` 後新 SID≠舊 owner（故 owner==SID 在 reset 時罕見）；事件期間 conf／sv_report 消失與模型端 Boot `rm` 或他 session reset 皆相容，觸發者無法區分（plan §2）。
- ④ **UNKNOWN（本票不依賴其結論）**：實際造成既有刪除事件的路徑與觸發 session；Boot 指令存在不等於每次 session 都已觀察到模型執行；`tmp/handoff.md` 是否有 repo 外 producer。

**External-review decision**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` §6「實作級外審」）：
- **6a（不成立）**：`INTRODUCED_CAPABILITY=NONE`——本票重用 ADV-OWNER-01 已導入的 `feature:ticket-owner`（其 token 於 `deliver/ADV-OWNER-01_stopguard.md` 字面存在），不導入新 feature／skill／tech；以此為顯式填寫，非由票面內文推斷。
- 6b①：`ALLOWED` 三檔首層 path segment 皆 `.claude`，distinct＝1 <2，不成立；6b②：`### AI-[1-9][0-9]*:` 標題數＝4 <8，不成立；6b③：`DEPENDENCY_DELTA=NONE`，不成立；6c：各標的檔檔頭無 `Revision` 欄修訂歷史，不納本款。開工後若 AI 標題增至 ≥8 或 ALLOWED 增跨首層路徑，MUST 重判。
- 外部研究：無承重外部事實題（官方契約已由 ADV-OWNER-01 抽驗，本票重用），**不送 PPSS**。**不豁免 Cross-Review**。

## Verify Config
TICKET_ID=ADV-RESETISO-01
CREATED=2026-10-15
PRESET=feature
ALLOWED=.claude/hooks/session_start.sh,.claude/hooks/tests/session_start_owner_test.sh,.claude/CLAUDE.md
FORBIDDEN=.claude/settings.json,.claude/settings.local.json,CLAUDE.md,CLAUDE.local.md,.claude/hooks/gate_stop_guard.py,.claude/hooks/registry_review_stop_guard.py,.claude/hooks/tests/gate_stop_guard_test.py,.claude/hooks/tests/registry_review_stop_guard_test.sh,.claude/scripts/gate_ledger.py,.claude/scripts/tests/test_gate_ledger.py,.claude/scripts/tests/owner_writer_test.sh,.claude/scripts/activate_ticket.sh,.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/qa/scripts/preflight_activation.sh,.claude/rules/ssot-governance.md,.claude/rules/cross-review.md,plans/cso_briefing/project/deliver/ADV-OWNER-01_stopguard.md,plans/cso_briefing/project/deliver/ADV-LAYER-01_budget.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-15 16:18
BASE_COMMIT=6c541133
TARGET_BLOB=.claude/hooks/session_start.sh:fa01aff5;.claude/hooks/tests/session_start_owner_test.sh:ea7cc254;.claude/CLAUDE.md:412e5327
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

> PRESET=feature（`.sh` 行為變更＋測試＋治理檔單行；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。`MAX_LINES=N/A`：含 fail-closed／資料保護類不變量，失敗形狀為審查發現式，不可先驗估算（TPL v3.5 R3）；MAX_LINES 非 execution gate。`TARGET_BLOB`：三檔皆為既有檔，`git hash-object` 前 8 碼 2026-10-15 16:18 live 取得。`BASE_COMMIT`／`ANCHOR_PROBED`／`TARGET_BLOB` 為資訊性診斷值（TPL v3.11），不作 gate，AI-0 仍逐檔讀現值。`FORBIDDEN` 使用字面路徑（避開 `verify_checklist.sh` 對含 `/` token 的 glob 字面比對缺陷）；`test_gate_ledger.py`、`owner_writer_test.sh` 與兩支 Stop hook 測試列 FORBIDDEN 僅表示**不得修改**，仍須**執行**作回歸。
>
> **`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 命中 `/qa` Step 4／4.5 標準觸碰面**：`DEFERRED-TO-DOCSYNC`——Dashboard 同步、`PROGRESS_ARCHIVE.md` 輪替、票檔 Pending→deliver 搬移與 AC 勾選走下一輪 `/compact` 標準收尾，不另開 DOCSYNC 票（`ssot-governance.md` §Ticket Closure 紀律「FORBIDDEN 延後項免開票」）。`TICKET_REGISTRY.json` 僅得經 mcp-ticket-platform `registry_*` 寫入，MUST NOT 直改。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: 見下列各 AI
- **動作**: 以 live `grep`／`Read` 逐字確認「現值」命中數；記錄 `git rev-parse --short HEAD`、`git status --short`；記錄 baseline 測試（`bash .claude/hooks/tests/session_start_owner_test.sh`、`python -I .claude/scripts/tests/test_gate_ledger.py`，各自案例數與 OK/FAIL）。**不得**跑會改寫真實 `tmp/verify/active.json` 的 `test_preflight_adapter.sh`（本票不需要）。
- 錨點：①`session_start.sh`：`    rm -f tmp/verify_cmd.conf .tmp/sv_report.md tmp/handoff.md 2>/dev/null` 恰 1 次；`    echo "🧹 SessionStart(reset)：tmp/ 預載檔已清"` 恰 1 次；`report_owner_status() {` 恰 1 次。②`.claude/CLAUDE.md`：`rm -f tmp/verify_cmd.conf` 全檔恰 1 次（L7）；全檔行數 ≤150。③`session_start_owner_test.sh`：`expected="$(printf '%s\n' '< ./.tmp/sv_report.md' '< ./tmp/handoff.md' '< ./tmp/verify_cmd.conf' | sort)"` 恰 1 次。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置（本票僅觸及 `.claude/` 可逆治理面：`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 記錄後以現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止），禁自行推定等價位置。**
- [x] 完成（2026-10-15；HEAD `6c541133`，工作樹開工前 clean。錨點①②③全命中〔`session_start.sh` 三處各恰 1、`.claude/CLAUDE.md` `rm -f tmp/verify_cmd.conf` 恰 1 且 142 行、測試檔 `expected=` 恰 1〕。Baseline：`session_start_owner_test.sh` ALL PASS、`test_gate_ledger.py` 53 OK）

### AI-1: `session_start.sh` — reset 前加入隔離判定，protect 時整組略過 `rm`
- **檔案**: `.claude/hooks/session_start.sh`
- **現值**: `reset` 分支 `mkdir -p tmp` 後無條件 `rm -f tmp/verify_cmd.conf .tmp/sv_report.md tmp/handoff.md`。
- **目標值**:
  1. 新增唯讀判定函式（可與 `report_owner_status` 共用同款 `owner_session_id`／`ticket_id` 的 sed 讀取與 `CLAUDE_CODE_SESSION_ID`，不新增平行機制；不得讀 Registry／lifecycle）：回傳「protect」當且僅當 `valid_active AND ( (owner 非空 AND (SID 空 OR SID != owner)) OR (owner 空 AND conf_ticket 非空 AND conf_ticket == active_ticket) )`。`valid_active`＝`active.json` 存在、可取得非空 `ticket_id`（取不到＝缺檔、不可解析或 ticket_id 空，一律非 valid）；`conf_ticket`＝`tmp/verify_cmd.conf` 首個 `TICKET_ID=` 之值（去 `\r`；缺檔或無該行＝空）。
  2. `reset` 分支：仍 `mkdir -p tmp`；protect 成立 → **整組略過** `rm`（三檔同進同退，不逐檔分支）並輸出一行非阻斷狀態；其餘 → 維持既有 `rm -f` 與既有 `🧹` 輸出。之後 `report_active_ticket`、PROGRESS 注入、exit 0 維持不變。
  3. 不 claim、不改寫 `active.json`、不清 owner、不清理後重建、不新增 TTL／鎖／lifecycle 判斷；`inject` 分支不變。
  4. 檔頭註解 L9（reset 描述）更新為含隔離判定的敘述（單行層級）。
- [x] 完成（`session_start.sh:91` 新增唯讀 `reset_protected`〔重用同款 sed 讀 owner／ticket_id 與 `CLAUDE_CODE_SESSION_ID`，不查 Registry〕；`:112` reset 分支 protect→整組略過 `rm` 並輸出一行狀態，否則維持既有 `rm -f`＋`🧹` 輸出；檔頭註解 L9 更新）

### AI-2: `.claude/CLAUDE.md` — Boot 第 7 行移除模型端 `rm`（D1，僅此單行）
- **檔案**: `.claude/CLAUDE.md`（僅 Boot Sequence 第 7 行）
- **現值**: ``Session 啟動 → `mkdir -p tmp && rm -f tmp/verify_cmd.conf .tmp/sv_report.md tmp/handoff.md` → 讀 PROGRESS.md → 輸出 Boot Report → **停止等 Ticket**。``
- **目標值**: 僅保留 `mkdir -p tmp`，移除 `rm -f ...`；可附一句指向「runtime 檔清理僅由 SessionStart `reset` 之隔離判定承擔，Boot 不另行 `rm`」。**僅此單行**，行數不增（≤150），不改其他任何行、不新增規則／章節。授權依據：Akira 2026-10-15 D1（見 parent plan §7）。
- [x] 完成（`.claude/CLAUDE.md` Boot 第 7 行僅保留 `mkdir -p tmp`，移除 `rm -f ...`，附一句「清理僅由 SessionStart `reset` 隔離判定承擔」；`git diff --numstat`＝1/1，行數 142 不變；授權依據 Akira 2026-10-15 D1）

### AI-3: `session_start_owner_test.sh` — 改寫 reset 副作用斷言為情境矩陣，新增保護案例
- **檔案**: `.claude/hooks/tests/session_start_owner_test.sh`
- **現值**: 「side effects」段以 owner=`sid-A`／SID=`sid-B` 斷言 `reset` 恰刪三檔、`inject` 零變動。
- **目標值**: 於暫存 repo＋真實 `session_start.sh` 子行程，以 env 設定／清空 `CLAUDE_CODE_SESSION_ID`，依下方「reset 情境矩陣」逐列斷言**狀態**（三檔內容與存在狀態、`active.json` 不變、exit 0），不鎖提示整句文字；**每列於 reset 前先斷言起始形狀**（`active.json` 為該列預期形狀〔缺檔／損毀／ticket_id 空／舊 7 欄／owner 空或有值〕、conf `TICKET_ID` 為預期值、三檔皆存在），以確認種子確實處於 below-writer 邊界之預期狀態；另新增一條**鏈式案例**：在暫存 repo 內以真實 `activate_ticket.sh`（env `sid-A`）寫入 owner 與 conf，再以真實 `session_start.sh reset`（env `sid-B`，以及 `sid-A`）驅動並斷言保護／清除（對應 [可達性] AC）；`inject` 零變動斷言保留；既有狀態語義案例（owner 提示四態、REVIEW/CLOSED/VOID 靜默等）維持通過。
- [x] 完成（`session_start_owner_test.sh`：inject 保留零副作用斷言；新增 reset 情境矩陣 13 列〔含 2b〕每列先斷言起始形狀、KEEP＝整棵樹位元組相同、CLEAR＝恰刪實際存在的三檔；`clear [stub]` 以 env 模擬；鏈式案例〔真實 `activate_ticket.sh` env sid-A → 真實 reset sid-B／sid-A〕。ALL PASS）

### AI-4: 驗證、殘留掃描與收尾實錄（不新增實作）
- **動作**: 重跑 `session_start_owner_test.sh` 與 `test_gate_ledger.py`（含 `CarrierSurvival`，FORBIDDEN 僅執行）；`bash .claude/hooks/tests/registry_review_stop_guard_test.sh`、`python -I .claude/hooks/tests/gate_stop_guard_test.py`、`bash .claude/scripts/tests/owner_writer_test.sh` 作回歸（僅執行）；`git diff --numstat` 確認 `.claude/CLAUDE.md` 僅 1 行增 1 行刪；`grep -rn "rm -f tmp/verify_cmd" .claude/CLAUDE.md` 零命中；`git diff --stat` 僅涉 ALLOWED；於票檔收尾實錄如實記錄：真實 `/clear` 鏈、真實 session 啟動時模型是否遵守新 Boot 文字均 UNVERIFIED；官方契約非本地實測；殘餘限制照「與 parent 差異 #1」措辭。ADV-OWNER-01 票檔不追加任何紀錄。
- [x] 完成（見「收尾實錄」。回歸：`test_gate_ledger.py` 53 OK〔含 `CarrierSurvival`〕、`registry_review_stop_guard_test.sh` ALL PASS、`owner_writer_test.sh` ALL PASS、`gate_stop_guard_test.py` 54 OK；`CLAUDE.md` numstat 1/1）

### reset 情境矩陣（`session_start_owner_test.sh` 逐列涵蓋；「保護」＝三檔內容與存在狀態不變，「清除」＝三檔皆被刪且 `active.json` 等其餘檔不變）

| # | `active.json` | owner | env SID | conf `TICKET_ID` | 預期 |
|---|---|---|---|---|---|
| 1 | 有效（ticket `T`） | `sid-A` | `sid-A` | 任意 | 清除（A；既有合法 reset） |
| 2 | 有效 | `sid-A` | `sid-B` | 任意 | 保護（B；不 claim、不清 owner） |
| 3 | 有效 | `sid-A` | 未設 | 任意 | 保護（C；身分未知非刪除授權） |
| 4 | 有效 | `sid-A` | `sid-B`（模擬 `/clear` 後新 SID） | 任意 | 保護、`active.json` owner 仍為 `sid-A`（[stub]） |
| 5 | 有效 | 空字串 | 任意 | `T`（＝active） | 保護（D） |
| 6 | 有效 | 缺 key（舊 7 欄） | 任意 | `T` | 保護（D） |
| 7 | 有效 | 空／缺 key | 任意 | 另一票 `U` | 清除 |
| 8 | 有效 | 空／缺 key | 任意 | 無 `TICKET_ID` 行 | 清除 |
| 9 | 有效 | 空／缺 key | 任意 | conf 缺檔（sv_report／handoff 在） | 清除（conf_ticket 空） |
| 10 | 缺檔 | — | 任意 | 任意 | 清除（D'） |
| 11 | 不可解析（損毀） | — | 任意 | 任意 | 清除（D'） |
| 12 | `ticket_id` 為空 | 任意 | 任意 | 任意 | 清除 |

## Hard Constraints
1. 保護條件以 parent D2 括號化式為**唯一判準**；MUST NOT 加入 lifecycle／Registry 查詢、時間窗、鎖、claim、owner 改寫、清理後重建或任何新增清理機制（不新增 GC）。
2. 三檔同進同退：protect 成立時 MUST 整組略過，MUST NOT 逐檔分支或部分刪除。
3. `.claude/CLAUDE.md` 僅得改 Boot Sequence 第 7 行，MUST NOT 增減其他行、不新增規則；若需變動超出該單行 → 停止回報。
4. MUST NOT 修改 `gate_ledger.py`、兩支 Stop hook 及其測試、`settings.json`、`activate_ticket.sh`、`verify_checklist.sh`、`preflight_activation.sh`、`owner_writer_test.sh`、`test_gate_ledger.py`、ADV-OWNER-01 票檔。
5. reset 行為 MUST 維持非阻斷：恆 `exit 0`，`mkdir -p tmp`、active ticket 輸出、owner 提示與 PROGRESS 注入維持既有行為；`inject` 零新增副作用。
6. 殘餘限制 MUST 如實保留，**不得宣稱完整跨 session runtime 隔離**：`active.json` 缺檔／損毀、`ticket_id` 為空，或 owner 空且 conf 不匹配時，其他仍有用途的 runtime 檔不在本項保護保證內；保護分支**可能**留存殘檔，各檔何時覆寫取決於既有 producer，亦可能由符合條件的 reset 或手動清理移除；本票不新增清理保證，不改 D2；resume／compact／fork 不再有自動清檔。**另揭露（F2）**：owner 非空時保護不比對 conf `TICKET_ID`，保護分支可能保留與 active 票不一致的 conf，無參數 `/uc` 或 Ticket DONE 讀 conf 時可能綁到非預期票（`uc/SKILL.md:48,91`）；此風險沿用 `uc` 契約「禁猜測 Ticket ID」與 `/qa` Step 0 scope 驗證承擔，本票不改 D2、不新增比對。**殘餘窗口承接者**：本票收尾實錄（AI-4）與 `/sv` 揭露。
7. 驗證範圍限定：真實 `/clear` 鏈與真實 session 啟動時模型是否遵守新 Boot 文字 MUST 標 UNVERIFIED；官方契約 MUST NOT 記為本地實測 PASS；`/clear` 後新 SID 之 AC 為 [stub]（env 模擬）。不可在本票驗證之理由：真實 SessionStart／`/clear` 需重啟或清空 session，且不得對真實 `tmp/verify_cmd.conf`、`.tmp/sv_report.md` 做刪除探測；模型端是否遵守新 Boot 文字須實際新 session 觀察。承接者：收尾實錄（AI-4）與 `/sv` 揭露。
8. 不碰真實 runtime：測試一律在暫存 repo 內，MUST NOT 對真實 `tmp/verify_cmd.conf`、`.tmp/sv_report.md`、`tmp/verify/active.json` 執行會刪除或改寫之探測。

## Error Handling
- 任一 AI-0 錨點不符 → 依 TPL v3.11 三分流處置；`ALREADY_EXECUTED_PENDING_CLOSURE` 或需動 FORBIDDEN 檔才能達成 → 停止回報（`SCOPE`）。
- 實作需要修改 `.claude/CLAUDE.md` 第 7 行以外的內容，或需要新增 lifecycle／Registry 查詢、鎖、GC 才能成立 → 停止回報。
- **R0 回報 CHECKPOINT-REQUIRED（因 `.claude/CLAUDE.md` 命中 Checkpoint 觸發檔）時**：依現役機制處理——將 R0 報告落盤（`.tmp/fail-advisor/R0_ADV-RESETISO-01_*.md`），**引用既有授權**（Akira 2026-10-15 D1，僅限第 7 行單行，見 parent plan §7 與本票來源說明），並依 `cross-review.md` 的 Checkpoint 流程呈報該既有裁決；MUST NOT 偽造事件、MUST NOT 自動豁免 checkpoint、MUST NOT 重問已裁決的相同政策。若 diff 超出該單行範圍或出現新衝突 → 重新判斷／停止回報。
- 既有測試（`CarrierSurvival` 或其他回歸）因本票行為失敗 → 先本地取證判定是 ALLOWED 內可修的實作／測試缺口，或需動 FORBIDDEN 檔；後者停止回報。
- 同一 incident 修復受 Gate 約束：依 `ssot-governance.md` §Autonomous Engineering Gate 處置（本票不改該文）。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律）。本票 ALLOWED 三檔 live 檢查零命中；Pending 票檔本身命中 `.gitignore:81` 屬既定慣例，非 commit 標的。

## Acceptance Criteria
- [x] **[存在性]** `.claude/CLAUDE.md` L7 不再含 `rm -f tmp/verify_cmd.conf`（`grep -c` 於該檔＝0）且仍含 `mkdir -p tmp`；`git diff --numstat -- .claude/CLAUDE.md` 為 1 增 1 刪；全檔行數 ≤150；`session_start.sh` 含單一隔離判定函式並由 `reset` 分支呼叫。
- [x] **[可達性]** 守門 input 經 production 路徑可達（trace，非僅列位置）：given 暫存 git repo 內以**真實 `activate_ticket.sh`**（env `CLAUDE_CODE_SESSION_ID=sid-A`，票檔含 `TICKET_ID`／`ALLOWED`）activation 一張票 → `active.json` 由該 producer 寫入 `owner_session_id=sid-A` 且 conf 由其寫入 `TICKET_ID`；when 以**真實 `session_start.sh reset`**（env `CLAUDE_CODE_SESSION_ID=sid-B`）執行 → expect observer：三檔內容與存在狀態不變、`active.json` owner 仍為 `sid-A`、exit 0（執行結果即為 trace）；對照 when env＝`sid-A` → expect 三檔被刪（分支可被正反兩向走到）。producer 證據欄（輔助，已回填）：`activate_ticket.sh:190-196`（owner 寫入，ADV-OWNER-01 交付；唯一 claim 入口）、`session_start.sh:91`（`reset_protected`）與 `:112`（reset 呼叫）；trace 由測試鏈式案例〔`session_start_owner_test.sh:153` 起〕執行結果承載；`CLAUDE_CODE_SESSION_ID`＝hook 子行程 env（官方契約，非本地實測）；`session_start.sh` 為 SessionStart hook（`startup|clear`→`reset`）之 production consumer。
- [x] **[行為性]** given reset 情境矩陣各列（每列先斷言起始形狀）之 #2／#3／#4（owner 已登記且 SID 不同／未設／模擬 `/clear` 新 SID）→ expect 三檔內容與存在狀態前後相同、`active.json`（含 `owner_session_id`）前後相同、exit 0；經真實 `session_start.sh` 子行程驅動，禁 fixture 直注判定結果。[stub]：#4 以 env 模擬，真實 `/clear` 鏈不在本票驗證範圍。
- [x] **[行為性]** given 矩陣 #5／#6（owner 空／缺且 conf `TICKET_ID`＝active 票）→ expect 三檔不變；#7／#8／#9（owner 空且 conf 不匹配／無 `TICKET_ID`／conf 缺檔）→ expect 三檔皆被刪且其餘檔不變。
- [x] **[行為性]** given 矩陣 #1（owner＝SID）→ expect 維持既有合法 reset：三檔被刪、其餘檔不變；#10／#11／#12（`active.json` 缺檔／損毀／`ticket_id` 空）→ expect 維持既有清理、exit 0。
- [x] **[行為性]** `inject` 模式與 owner 提示函式零新增副作用（既有斷言維持）；owner 提示四態、`REVIEW`／`CLOSED`／`VOID`／冊面查無靜默、舊 7 欄與 8 欄不報損毀等既有案例維持通過；斷言只鎖狀態與資料保護，不鎖提示整句文字。
- [x] **[行為性]** 回歸：`python -I .claude/scripts/tests/test_gate_ledger.py`（含 `CarrierSurvival`）、`bash .claude/hooks/tests/registry_review_stop_guard_test.sh`、`python -I .claude/hooks/tests/gate_stop_guard_test.py`、`bash .claude/scripts/tests/owner_writer_test.sh` 全綠（baseline 於 AI-0 記錄）。
- [x] **[存在性]** 收尾實錄載明：殘餘限制（Hard Constraints #6 措辭，含 F2 conf 不一致之 consumer 揭露）、真實 `/clear` 鏈與模型端遵守新 Boot 文字 UNVERIFIED、官方契約非本地實測；`deliver/ADV-OWNER-01_stopguard.md` 無任何變動。
- ~~`assembleDebug` PASS~~ (N/A: 無 Android 產物；本票僅 `.sh`／測試與治理檔單行)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（3 個既有檔；`git status` 無其他變動）

## 收尾實錄（Agent，2026-10-15；QA／SV 前）

- **範圍**：HEAD `6c541133`；修改 3 檔（`.claude/hooks/session_start.sh` +27／−3、`.claude/hooks/tests/session_start_owner_test.sh` +104／−14、`.claude/CLAUDE.md` 1 行）。未碰 `settings.json`、兩支 Stop hook 與其測試、`gate_ledger.py`、`activate_ticket.sh`、`verify_checklist.sh`、`preflight_activation.sh`、ADV-OWNER-01 票檔。
- **保護判準（D2 字面）**：`valid_active AND ( (owner 非空 AND (SID 空 OR SID != owner)) OR (owner 空 AND conf_ticket 非空 AND conf_ticket == active_ticket) )`；不查 Registry／lifecycle；不 claim、不改 owner、不清理後重建；protect 時整組略過 `rm`，其餘維持既有清理。
- **測試結果（[stub]／決定性子行程；實測）**：
  - `bash .claude/hooks/tests/session_start_owner_test.sh`：**ALL PASS**——reset／inject 既有狀態語義案例維持；新增矩陣 13 列（1 A 清除；2、2b、3、4〔/clear 新 SID，env 模擬〕、5、6 保護；7、8、9、10、11、12 清除）、`clear [stub]` owner 不變、鏈式案例三項（真實 `activate_ticket.sh` 寫 owner／conf → 真實 reset sid-B 整樹不變 → sid-A 清三檔、active.json 保留）。
  - 回歸（僅執行）：`python -I .claude/scripts/tests/test_gate_ledger.py` **53 OK**（含 `CarrierSurvival`）；`registry_review_stop_guard_test.sh` ALL PASS；`owner_writer_test.sh` ALL PASS；`gate_stop_guard_test.py` **54 OK**。
- **殘餘限制（如實，不宣稱完整跨 session runtime 隔離）**：`active.json` 缺檔／損毀、`ticket_id` 為空，或 owner 空且 conf 不匹配時，其他仍有用途的 runtime 檔不在本項保護保證內；保護分支**可能**留存殘檔，各檔何時覆寫取決於既有 producer，亦可能由符合條件的 reset 或手動清理移除；本票不新增清理保證、不改 D2；resume／compact／fork 不再有自動清檔；owner 非空時不比對 conf `TICKET_ID`，保護分支可能保留與 active 票不一致的 conf，無參數 `/uc` 或 Ticket DONE 讀 conf 時可能綁到非預期票（`uc/SKILL.md:48,91`），沿用 uc 契約「禁猜測」與 `/qa` Step 0 承擔。承接者：本實錄與 `/sv` 揭露。
- **UNVERIFIED／未做**：真實 `/clear`／SessionStart 鏈與模型端是否遵守新 Boot 文字（需實際新 session 觀察）；官方契約（`CLAUDE_CODE_SESSION_ID` 於 hook 子行程與 `/clear` 更新）非本地實測；變異驗證未做（未以移除守門重跑測試的方式證明測試牙齒，KEEP 列在舊版無條件 `rm` 下必然失敗屬推論，非實測）。
- **作業觀察**：開工前 `tmp/verify_cmd.conf` 已由 ticket-run 步驟 10.1 寫入；本 session 的 `activate_ticket.sh` 重跑使 `owner_session_id` 為本 session（`23ee6171-…`）。ADV-OWNER-01 票檔未追加任何紀錄。
- **PROGRESS／ARCHIVE／票檔搬移／registry 收尾**：`DEFERRED-TO-DOCSYNC`，於下一輪 `/compact` 標準收尾處理。

## SV／Cross-Review 實錄（2026-10-15，commit `0475fbad`，範圍 `6c541133..0475fbad`）

- **SV**：Stage A–C 無阻斷；`structural_verify.sh --post` PASS [ADVISORY]（A/B/C/D/E/F/G）。Compile／Lint／16KB ELF 為 N/A／SKIP（無 `app/` 變更）。報告：`.tmp/sv_report.md`（歸檔 `.tmp/sv_report_20261015_170901.md`、`_171126.md`）。SV 報告於首次撰寫即內含可核對的可達性舉證（官方契約出處、`file:line`、本地觀測），並如實標 ⚠️ PARTIAL（hook 子行程內 env 實值與真實 `/clear` 鏈未驗）。
- **R0（1 次，預過濾，非放行）**：判 **CHECKPOINT-REQUIRED**——diff 觸碰 Checkpoint 觸發檔 `.claude/CLAUDE.md`（PROGRESS 條件豁免不含此檔）；R0 核對 hunk `@@ -4,7 +4,7 @@` 僅 1 刪 1 增，與 D1「僅限第 7 行單行」相符，其餘字面閘（K3-6 scope、關鍵詞、4a／4b／4c）全通過。記錄 `.tmp/fail-advisor/R0_ADV-RESETISO-01_20261015_171028.md`（CLASSIFICATION: NORMAL；不計 fail budget；無 override）。
- **Checkpoint 處置（依本票 Error Handling）**：**引用既有授權 Akira 2026-10-15 D1**（明確授權 `.claude/CLAUDE.md` Boot 第 7 行單行修改），不偽造事件、不自動豁免、不重問已裁決政策；放行權限來自 Akira 既有裁決而非 R0 或主 session 自判，並於 R0 記錄與回報中明示，保留 Akira 於 Codex 結果後否決之空間（護欄二）。diff 未超出該單行範圍、無新衝突，故未重新判斷。
- **Codex Cross-Review（正式審查）**：第 1 次即 **ADVISORY**（exit 0，`.tmp/cross_review_result_20261015_171028.md`）；無 BLOCKER、無 REPORT_FIX、無 Gate incident、`REPORT_FIX_COUNT` 0。唯一觀察：新 reset 隔離邏輯缺真實 `/clear` 與 hook 子行程內 env 實值的端到端驗證（目前以 `sid-NEW` stub＋子行程測試覆蓋），現有證據未顯示功能錯誤，不阻斷。`REACHABILITY: NONE`。
- **reviews 回填**：`qa=PASS`、`sv=PASS`、`codex=PASS`〔依 Transition Table「ADVISORY 無 BLOCKER／FAIL finding 視同 PASS」映射；**實際 verdict＝ADVISORY**，記於 checkpoint `codex_verdict_actual`〕、`traycer=N_A`（`Traycer Workflow: N/A`）。
- **作業觀察（如實）**：①`/sv` 進行中 registry Stop hook（ADV-OWNER-01 新增的 owner 核對）對本 session 觸發一次「AC 全勾但 lifecycle 仍 IN_PROGRESS」攔截——本 session 為 `active.json` owner，核對如設計生效；因 Cross-Review 尚未完成，未提前轉 `REVIEW`（不以推定補 PASS），攔截一次後 marker 已寫入；②為先產生 payload 而不呼叫 Codex，使用 PATH 上的 stub `codex`（`tmp/ADV-RESETISO-01/stubbin/codex`，僅 exit 1），未寫入 `.tmp/cross_review_state` 的 round／verdict；③本次未再發生 `tmp/verify_cmd.conf`／`.tmp/sv_report.md` 被外力刪除（本 session 為 owner，reset 隔離判定在位；單次觀察，不構成因果證明）；④`PROGRESS.md`／`PROGRESS_ARCHIVE.md` 同步、Done 標記為 `DEFERRED-TO-DOCSYNC`。
- **未做（不變）**：真實 `/clear`／SessionStart 鏈、模型端遵守新 Boot 文字、變異驗證；官方契約非本地實測。
