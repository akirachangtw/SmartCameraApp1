# ADV-SAVE-01: TKA close 於 read-back 確認 CLOSED 後，以 `close_helper.py persist` 確定性地單票提交該票 `deliver/` 票檔；其餘結果零提交；保存失敗如實回報、scratch 過期後可恢復保存

**Revision**: Rev A.1（2026-10-24，Akira 裁決「接受本批與 ADV 命名」後原地收準三點：①正式 close 鏈到 persist 之整合可達性；②保存失敗不得冒稱成功；③scratch 過期後之保存恢復。原 Rev A 之 AI-1～AI-4 與 AC 同票內修訂，票號與 `Pending/` 位置不變）。

**簽發日期**: 2026-10-24
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: TKA-CLOSE-01 —— `close_helper.py` 子命令結構、`verify_readback`、`Refuse`／exit 3 慣例與暫存 git repo 測試 fixture；ADV-CLAUDECLI-01 —— 收束後未自動提交之事件與 `2d8e7e34` 單檔軌道 A 提交實證
- `coordination_dependency`: ADV-COMPACT-01（依賴本票先落地）、ADV-EVEMBED-01（同檔 `ticket-author/SKILL.md`，排在本票後）

## 來源（Parent）
- Parent type：Plan-backed；路徑 `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261024-autonomy-closeloop-plan.md`（Revision R3；Fixture false／Plan Gate PASS／Ticket mode eligibility ALLOWED）；plan-local item `T01`；讀取時間 2026-10-24。
- Selected item：TKA close 確定性保存票檔。
- 承重決策（使用者 2026-10-24 分工裁決）：票務最終收束及票務文件保存歸 TKA，以確定性程序完成；compact 僅負責 PROGRESS／ARCHIVE／Handoff／歸檔與自身產物，不作票務提交兜底；成果表歸 projman。plan D1：保存**僅於 read-back 確認 CLOSED** 時執行，NOT_CLOSED／PRECONDITION_FAILED／BLOCKED／任何 STOP 零提交。反例已檢：①冊面／票檔矛盾時提交會持久化矛盾（違 §Ticket Closure「矛盾停止」）；②NOT_CLOSED 後正式退回修補會把票檔搬回 ignored `Pending/`，若已提交將產生已追蹤檔刪除與錯誤凍結點。
- Rev A.1 收準（使用者 2026-10-24 裁決）：①close→persist 須於正式 close 鏈可達；②保存失敗不得冒稱成功；③scratch 過期後保存可恢復，不依賴被清除之 `registry.json`／`decision.json`。
- 已知後果（揭露）：REVIEW 狀態之 `deliver/` 票檔在 close 成功前保持未提交，不由 compact 兜底，於該票下次成功 close 時一併保存。
- Diag parent：`.tmp/ticket-author/20261024-autonomy-closeloop-diag.md`（C1–C4）。
- 票號歸屬：本票由「工程師自主閉環」議題（源自 ADV-CLAUDECLI-01 收束與 Advisor 成果表 P1 缺口）發起，依 Akira 2026-10-24 裁決，同一議題 session 發起之票一律掛該議題前綴 `ADV`，不依修改落點另分前綴；原開票 ID 已以 `registry_rename_ticket` 改名（模板 v3.12「發起議題律」）。

## 證據（開票時 live，2026-10-24 @306b5276）
- 恢復所需之耐久依據：`finalize` 只於 read-back 與 binding 檢查通過後才寫入票檔〈收束結果（close）〉段（`close_helper.py` `cmd_finalize`，`MARK_BEGIN`／`MARK_END` 整行標記，`find_section` 取最後一段）；該段含「轉 CLOSED」之實際收束結果行。scratch（`.tmp/ticket-author/close/<ID>/`）七天後由 `sweep` 清除，`verify_readback` 之 reviews 期望值來自 scratch，故過期後現行驗證必拒絕。
- `deliver/` 未被 ignore：`.gitignore:82` `!plans/cso_briefing/project/deliver/`；`Pending/` 被 `:81` 排除。
- 既有 `test_close.py` 於 HEAD 基線 115 項 OK（`python -I`，約 190 秒）。

## Verify Config
TICKET_ID=ADV-SAVE-01
CREATED=2026-10-24
PRESET=feature
ALLOWED=.claude/skills/ticket-author/scripts/close_helper.py,.claude/skills/ticket-author/tests/test_close.py,.claude/skills/ticket-author/SKILL.md
FORBIDDEN=.claude/scripts/gate_ledger.py,.claude/scripts/route.py,.claude/hooks/**,.claude/rules/**,.claude/skills/compact/**,.claude/skills/sv/**,.claude/skills/qa/**,.claude/skills/projman/**,.claude/skills/ticket-author/references/**,.claude/settings.json,.claude/settings.local.json,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,CLAUDE.md,app/**,plans/roadmap/**,plans/architecture/**,plans/cso_briefing/project/TICKET_REGISTRY.json,.gitignore
MAX_LINES=260
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-24 15:30
BASE_COMMIT=306b5276
TARGET_BLOB=N/A（開票端未以 git hash-object 逐檔取 blob；AI-0 逐檔讀現值錨點替代）
INTRODUCED_CAPABILITY=feature:tka-close-persist
DEPENDENCY_DELTA=NONE

MAX_LINES 推導（估算參考，非判準）：AI-1 persist 與 recover 驗證 ~110＋AI-2 docstring／dispatch ~12＋AI-3 SKILL ~20 → ~140，緩衝 ×1.8 → 260（test 行數不計）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `close_helper.py`、`SKILL.md`（ticket-author）、`test_close.py`
- **動作**: 逐字確認下列現值命中：
  1. `close_helper.py` docstring 含 `Git is used read-only. Nothing else is touched.`（唯一）。
  2. `close_helper.py` `build_parser()` 之子命令恰為 `preflight`／`reviewer`／`decide`／`readback`／`finalize`／`sweep`，`main()` handler dict 同集合。
  3. `close_helper.py` `verify_readback(repo, tid, after)` 回傳 `(ok, mism, exp, loc)`，且要求 `lifecycle == "CLOSED"` 與票檔位於 `deliver`／`deliver/archive`；`find_section`、`MARK_BEGIN`／`MARK_END`、`REVIEW_ENUM`、`REVIEW_FIELDS`、`locate_ticket` 存在。
  4. `SKILL.md` 之 ```` ```close-dispatch ```` 區塊含 5 行（preflight／reviewer／decide／readback／finalize），close 程序第 8、9 步與〈Re-entry〉段現文。
  5. `test_close.py` 之 `TestSkillCallParser`、`TestUpstreamWiring` 與既有全鏈測試之名稱與是否解析 close-dispatch 區塊或 SKILL 文字（新增 dispatch 行前須知其影響）。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置。**
- [x] 完成（2026-10-24；close_helper.py persist／SKILL.md 接線／test_close.py 130 OK〔基線 115＋新增 15〕，實作未提交，待 /qa Step 4.5）

### AI-1: 新增 `persist` 子命令（確定性保存、失敗如實、可恢復）
- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`
- **現值**: 無 `persist` 子命令；`finalize` 後無任何 git 寫入。
- **目標值**: 新增 `persist --ticket ID --registry <registry_after.json> [--recover]`：
  1. **驗證（二選一，不自動降級）**：
     - 預設（full）：以 `verify_readback` 判定；`ok` 為假 → `Refuse`（exit 3），零 git 寫入，輸出 `{"saved": false, "refused": "readback_mismatch", "mismatches": [...]}`。scratch 之 `registry.json`／`decision.json` 缺失且未帶 `--recover` → `Refuse`，輸出 `{"saved": false, "refused": "scratch_missing_use_recover"}`（**不**自動改走 recover）。
     - `--recover`（scratch 過期後之恢復）：不讀 scratch，改驗①`registry_after` 之 `id`＝票號、`lifecycle`＝`CLOSED`；②`locate_ticket` 解得票檔且位於 `deliver`／`deliver/archive`；③`reviews` 四欄皆存在且值屬 `REVIEW_ENUM`；④票檔含整行標記之〈收束結果（close）〉段（`find_section` 非 None）且段內含「轉 CLOSED」之實際收束結果行。任一不符 → `Refuse`（exit 3），零 git 寫入，輸出 `refused` 與具體缺項。輸出 `verification: "recover"`（full 為 `"full"`）。
  2. 只對解出之該票票檔單一路徑操作：`git check-ignore -q -- <檔>` exit 0 → 不強制 add，輸出 `{"saved": false, "reason": "ignored"}`；exit 非 0／1 → `{"saved": false, "reason": "git_query_failed"}`，零寫入。
  3. `git ls-files --error-unmatch -- <檔>` 判追蹤；未追蹤 → `git add -- <檔>`（失敗 → `{"saved": false, "reason": "add_failed"}`）；已追蹤且 `git status --porcelain -- <檔>` 為空 → `{"saved": true, "reason": "no_diff"}`（已保存，不提交、不 `--allow-empty`）。
  4. 提交：`git commit -o -m "[<TICKET_ID>] docs(ticket): persist closeout record (TKA close persist)" -- <檔>`；成功輸出 `{"saved": true, "commit": <hash>, "path": <檔>, "verification": ...}`；失敗（含 commit-msg hook）輸出 `{"saved": false, "reason": "commit_failed", "stderr_head": ...}`，不重試、不改用其他提交方式。
  5. **退出碼**：`saved: true` → exit 0；任何 `saved: false`（含 Refuse、ignored、git_query_failed、add_failed、commit_failed）→ exit 3，使 Agent 無法把失敗誤讀為成功。
  6. 不呼叫 `gate_ledger.py check commit`；不觸及任何其他路徑（pathspec 限定，他檔 staged／unstaged 不受影響）。
- [x] 完成（2026-10-24；close_helper.py persist／SKILL.md 接線／test_close.py 130 OK〔基線 115＋新增 15〕，實作未提交，待 /qa Step 4.5）

### AI-2: docstring 與 CLI 註冊
- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`
- **現值**: docstring `Writes` 段稱 `Git is used read-only. Nothing else is touched.`；子命令清單無 persist；Exit codes 行為 `0 result produced | 2 usage error | 3 refused (precondition not met)`。
- **目標值**: docstring 改述「git 僅於 `persist` 對該票單一 `deliver/` 票檔做 pathspec add／commit，其餘子命令 git 唯讀」，子命令清單補 `persist`（含 `--recover`），Exit codes 註明 persist 之 `saved:false` 一律 exit 3；`build_parser()`／`main()` handler 註冊 `persist`。
- [x] 完成（2026-10-24；close_helper.py persist／SKILL.md 接線／test_close.py 130 OK〔基線 115＋新增 15〕，實作未提交，待 /qa Step 4.5）

### AI-3: SKILL close 程序接線（含正式鏈可達性、失敗回報、恢復路徑）
- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值**: close-dispatch 5 行；第 8 步止於 `finalize`；第 9 步回報「transition／read-back／final result」三行；〈Re-entry〉段稱「`CLOSED` 而無最終結果段且無對應 decision 者不動」。
- **目標值**:
  1. close-dispatch 新增兩行：`persist --ticket <ID> --registry <dir>/registry_after.json` 與 `persist --ticket <ID> --registry <dir>/registry_after.json --recover`（後者註明僅供 scratch 已過期之重入）。
  2. 第 8 步之後新增保存步驟：read-back 為 CLOSED 者（CLOSE→finalize 成功、ALREADY_CLOSED、WRITE_FINAL_ONLY）於 finalize（如有）後執行 `persist` 一次；NOT_CLOSED／PRECONDITION_FAILED／BLOCKED／任何 STOP 不執行。
  3. 重入規則：`ALREADY_CLOSED`／`WRITE_FINAL_ONLY` 而 `<dir>` 之 decision／registry 已被清除、且票檔含〈收束結果（close）〉段者，read-back 後以 `persist --recover` 保存；無該段者不執行 persist、回報 `not_run(no_final_result_section)`，維持既有「不動、不猜回證據」。
  4. 第 9 步回報增一行 `persist: saved <hash> | already_saved | NOT SAVED(<reason>) | not_run(<why>)`；**MUST** 以 helper 輸出之 `saved` 欄為準——`saved: false` 時 **MUST NOT** 寫「已保存／committed」，close 整體依既有「Partial success is reported as partial」回報為部分成功（lifecycle 已 CLOSED、票檔未提交），並載明下一步＝同票重入 `/ticket-author close <ID>`（scratch 在期內走 full、過期走 `--recover`）。不改 close 其他步驟語義。
- [x] 完成（2026-10-24；close_helper.py persist／SKILL.md 接線／test_close.py 130 OK〔基線 115＋新增 15〕，實作未提交，待 /qa Step 4.5）

### AI-4: 測試
- **檔案**: `.claude/skills/ticket-author/tests/test_close.py`
- **目標值**: 以既有暫存 git repo fixture（真實 `git init`，非 mock）新增 persist 測試，覆蓋下方 AC 之狀態矩陣；同步更新受 dispatch 行數影響之既有斷言（僅限 AI-0 第 5 點確認之處）。
- [x] 完成（2026-10-24；close_helper.py persist／SKILL.md 接線／test_close.py 130 OK〔基線 115＋新增 15〕，實作未提交，待 /qa Step 4.5）

## Hard Constraints
1. 保存只在 read-back 確認 CLOSED 時發生；任何非 CLOSED 結果 **MUST** 零 git 寫入（含 `git add`）。
2. 只提交該票單一票檔；**MUST NOT** 提交 PROGRESS、成果文件、Registry 檔或他票檔；**MUST NOT** `git add -A`／`.`。
3. **MUST NOT** 呼叫或修改 `gate_ledger.py`；**MUST NOT** 使用 `--allow-empty`、`--no-verify` 或重試迴圈。
4. 不改 close 之 decide／readback／finalize 判定語義；不新增 lifecycle、verdict、reason code 或 registry 欄位。
5. 不改 compact、sv、ssot-governance（屬 ADV-COMPACT-01、另票）。
6. `--recover` **MUST** 為顯式旗標，**MUST NOT** 由 helper 在 full 驗證失敗或 scratch 缺失時自動改走；recover 之驗證不得弱於本票 AI-1 第 1 點所列四項。

## Error Handling
- AI-0 第 5 點顯示既有測試以硬編碼行數或全文比對鎖定 SKILL 文字，且更新會改變其驗證意圖 → 停止回報。
- `verify_readback` 現行語義不足以區分 CLOSED 讀回一致（例如需新增欄位才能判定）→ 停止回報，不自行擴充判定。
- 已知限制（不停止，收尾實錄揭露）：recover 之依據為票檔內之收束結果段，該段若被手寫偽造則 recover 無法分辨（威脅模型限於 close 流程自身產物；registry 讀回為 CLOSED、票檔已在 `deliver/` 仍為必要條件）；recover 不重驗當時之 reviewer 結論。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（開票時已驗：`deliver/` 由 `.gitignore:82` `!plans/cso_briefing/project/deliver/` 放行；`Pending/` 被 `:81` 排除，故 persist 之 ignored 分支屬防禦性回報）。

## Acceptance Criteria
- [x] **[存在性]** `close_helper.py` 有 `persist` 子命令（含 `--recover`）並註冊於 `build_parser()`／`main()`；docstring 已改述 git 寫入邊界與 exit code；`SKILL.md` close-dispatch 含兩條 persist 行，close 程序含保存步驟、重入規則與第 9 步回報行。 （提交 `ef289991`：`close_helper.py` `cmd_persist`／`build_parser`／`main` 註冊；docstring 與 Exit codes 已改；`SKILL.md` close-dispatch 7 行含 persist 兩行、8b 步驟與第 9 步回報行）
- [x] **[可達性]** 正式 close 鏈可達 persist：①`SKILL.md` close-dispatch 之每一行（含 persist 兩行）皆可被既有 dispatch 解析 fixture 解析並實際執行；②producer 證據欄（執行後回填 `檔案:行號`）：persist 之 input（`registry_after`）producer 為 close 第 7 步 `registry_list`→`<dir>/registry_after.json`；票檔位置 producer 為 Agent closeout 搬檔（`sv/SKILL.md` Step 11）；persist 之呼叫 producer 為 SKILL close 第 8 步後之保存步驟，而 close 本身由 `sv/SKILL.md` Step 11 第 4 項（`Skill(ticket-author)` args `close <ID>`）呼叫，三處之文字須一致。 （producer 證據：`registry_after`＝`SKILL.md:297` 第 7 步 `registry_list`→`<dir>/registry_after.json`；persist 呼叫點＝`SKILL.md:299` 第 8b 步；close 呼叫鏈＝`sv/SKILL.md:214` Step 11 第 4 項；scratch `decision.json` 寫入＝`close_helper.py:978`、`registry.json`＝`SKILL.md:283` Agent 寫入；收束結果段寫入＝`close_helper.py:1181`；`TestStatic.test_dispatch_subcommands_exist_in_help` 與 `test_full_flow_from_skill_block` 解析並實跑 dispatch 每一行。**限制**：呼叫 persist 仍屬 Agent 依 SKILL 之模型契約，無機械後盾）
- [x] **[行為性]** given 暫存 git repo 與 helper 真實子程序呼叫（full 驗證）：(a) registry_after 為 CLOSED 且一致、票檔為 untracked 新檔 → expect 恰一筆新提交、前綴 `[TST-01]`、提交只含該票檔、他票 dirty 檔仍 dirty、exit 0、`saved:true`；(b) 票檔為 tracked 修改 → 提交一筆只含該檔；(c) tracked 無差分 → `saved:true`＋`no_diff`、HEAD 不變；(d) registry_after 為 REVIEW／lifecycle 不符 → exit 3、`saved:false`、HEAD 與 index 不變；(e) 票檔不在 `deliver/` → exit 3、零寫入；(f) 票檔被 ignore → exit 3、`saved:false`、reason `ignored`、HEAD 不變；(g) 第二次 persist（已提交後重入）→ `saved:true`＋`no_diff`、無空提交。 （`TestPersist`：untracked 新檔／tracked 修改／no_diff／非 CLOSED 拒絕零寫入／非 deliver 拒絕零寫入／ignored；真實暫存 git repo＋真實 helper 子程序）
- [x] **[行為性]** 保存失敗不得冒稱成功：given commit-msg hook 令 `git commit` 失敗 → expect exit 3、`saved:false`、reason `commit_failed`、無提交、`git add` 之新檔狀態與錯誤回報一致、不重試；given `git add` 失敗（例如不可寫之 index lock）→ exit 3、reason `add_failed`；任一 `saved:false` 情形 helper 輸出不含 `"saved": true`，且 SKILL 第 9 步之回報規則文字明載 `saved:false` 時不得寫已保存（以 `grep` 驗證該規則存在）。 （`test_commit_hook_failure_*`〔真實 commit-msg hook exit 1→exit 3、`saved:false`、`commit_failed`、無提交、未追蹤狀態還原〕、`test_add_failure_*`〔真實 index.lock→`add_failed`〕、`test_skill_documents_honest_failure_and_recovery_rules`〔SKILL 規則文字〕）
- [x] **[行為性]** scratch 過期後恢復：given 票檔已含 `finalize` 寫入之收束結果段、registry_after 為 CLOSED、scratch 目錄已清除 → expect 未帶 `--recover` 時 exit 3＋`scratch_missing_use_recover`、零寫入；帶 `--recover` 時恰一筆提交、`verification:"recover"`、`saved:true`；另測拒絕：(i) 票檔無收束結果段、(ii) registry lifecycle 非 CLOSED、(iii) 票檔不在 `deliver/`、(iv) reviews 欄缺值或值非法 → 各 exit 3、零 git 寫入。 （`test_scratch_missing_requires_explicit_recover_*`、`test_recover_commits_after_scratch_sweep`、`test_recover_refuses_*` 四類拒絕〔無收束段／lifecycle 非 CLOSED／票檔不在 deliver／reviews 缺或非法〕）
- [x] **[行為性]** 全鏈整合：沿用既有全鏈 fixture，依 SKILL close-dispatch 之文件順序（preflight→decide→readback→finalize→persist）在暫存 repo 實跑，expect 最後 `saved:true` 且提交只含該票票檔；NOT_CLOSED 之 decide 結果下執行同鏈 expect 不呼叫 persist 之判定在 SKILL 文字中有明文且 helper 於非 CLOSED registry 下拒絕。 （`test_full_flow_from_skill_block` 依 SKILL 文件順序 preflight→reviewer→decide→readback→finalize→persist 實跑：恰一筆 `[TST-01]` 提交、僅含票檔、工作樹乾淨；非 CLOSED 路徑零提交見第 3 條 (d)(e)）
- [x] **[行為性]** 既有 `test_close.py` 全數通過（不弱化既有斷言；僅 AI-0 第 5 點確認之 dispatch 相關斷言可同步）。 （`test_close.py` 全數 130 OK，基線 115；僅同步 `len(cmds)` 5→7 並追加子命令序列斷言，無弱化）
- [x] `python -I` 執行 `test_close.py` PASS（取代 assembleDebug；本票無 Android 產物）。~~assembleDebug PASS~~ (N/A: 無 app/ 變更) （`python -I test_close.py` → `Ran 130 tests ... OK`。**`assembleDebug` 未取得成功證據**：本環境 Gradle 兩次回 `Unable to establish loopback connection`，本票無 `app/` diff，票面已劃記 N/A，不宣稱 Gradle 通過）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 （Step 0 `verify_checklist.sh` @`306b5276..ef289991`：3 檔皆 ALLOWED、無禁區；evidence record `tmp/ADV-SAVE-01/evidence/step0_ef289991….txt`）

## 收尾實錄（Agent closeout，2026-10-24；交 TKA close 判定，非自行終審）
- **交付**：實作提交 `ef289991`（`close_helper.py` persist 子命令含 `--recover`；`SKILL.md` 接線；`test_close.py` 130 OK）。
- **QA**：Step 0 PASS（已提交範圍）；Step 1–3（Gradle）**環境不可用未執行**（`java.io.IOException: Unable to establish loopback connection`，daemon 與 `--no-daemon` 各一次；同機 Python loopback 正常），非票面失敗亦非 PASS，`reviews.qa` 填 `N_A`；未依 Gate 記 `step_limit`（無對應 incident，已於 SV 報告揭露）。
- **SV／Cross-Review**：R0（subagent）FORWARD（非正式 PASS）。Codex `gpt-5.6-sol` **三次 REPORT_FIX → `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2），無終局 verdict**，`reviews.codex=N_A`（不等於 PASS）。verdict 史：①缺 `registry.json`／`decision.json`／`deliver` 位置／收束結果段之 producer 行號→已補，第 2 次重審 `REACHABILITY: NONE`；②〈未勾項全盤點〉與 `assembleDebug` 環境失敗矛盾→已改為誠實豁免／裁決中；③將 git ignore／index／worktree／commit-hook 狀態視為外部 input 要求 production writer（本票判斷：此為 git 自身之執行環境，非需 producer 之狀態；其失敗分支皆以真實 git 測試，如實回報 `saved:false`）——LIMIT 後不再重跑。Gate INC-001：executor 2 輪 fail、`step_limit` 已記。
- **已知限制**：`--recover` 依票檔內收束結果段，手寫偽造不可辨；`registry_after` 為非物件 JSON 時 `persist` 以非 0 結束（無 `saved:true`）；呼叫 persist 為 Agent 依 SKILL 之模型契約；Gradle 未驗。
- **PROGRESS.md Dashboard 同步**：FORBIDDEN 延後項，於下一輪 `/compact` 處理，不另開票。
- **指示式觀察**：本票之 TKA close 為 persist 首次真實使用（非盲測）。

<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: 交付：close_helper.py persist（read-back CLOSED 後單票提交 deliver/ 票檔；saved:false 一律 exit 3；--recover 供 scratch 過期後恢復）＋SKILL.md 接線＋test_close.py 130 OK（提交 ef289991）。 / Cross-Review：Codex REPORT_FIX×3 觸 CHECKPOINT_REPORT_FIX_LIMIT 無終局 verdict（reviews.codex=N_A，非 PASS）；QA Gradle 因環境 loopback 錯誤未執行（reviews.qa=N_A）；TKA close reviewer gpt-6-sol 九條 AC 全 PASS（核心 6 條）。 / 限制：persist 之呼叫為 Agent 依 SKILL 之模型契約；--recover 不可辨偽造收束段；本次 close 為 persist 首次真實使用（指示式觀察）。
- 最終驗收結論: 核心 AC 6 條皆為 PASS 或有效 N/A；非核心 PASS 3／N/A 0／UNVERIFIED 0／FAIL 0（共 9 條 AC）
- 實作提交: ef289991be41cb9d82c363b6580a560ff23b9236
- 實際收束結果: 2026-10-24 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: codex/gpt-6-sol（tier=heavy）；覆蓋 AC 9／9（全部），independent_call=True
- 披露: reviewer limitation: reviewer 無法讀檔，證據逐字嵌入 prompt（diff、測試原文、驗證輸出）；回覆檔取自 codex exec 輸出之最終訊息段（原始輸出另存 reviewer_reply_raw.txt）；判 PASS 不等於 Cross-Re
<!-- close-result:end -->
