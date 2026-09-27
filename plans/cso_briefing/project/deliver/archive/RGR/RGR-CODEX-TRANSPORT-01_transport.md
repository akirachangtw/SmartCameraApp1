# RGR-CODEX-TRANSPORT-01: 修復 codex.cmd prompt 傳輸截斷並收口 true-call 驗證

**簽發日期**: 2026-09-13
**簽發者**: Claude (SKAD)
**來源議題**: RGR
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（審查走 `/qa` → `/sv` → Cross-Review；禁以 rigor gate 審自身傳輸層）
**前置依賴（依賴三分）**：
- `execution_dependency`: **無 lifecycle 閘**——真正之前置為**程式碼事實**：schema 修正 commit `c9d1870` 須在 HEAD 祖先鏈中（AI-0 動作 A 機械驗證）。`RGR-RESP-SCHEMA-01` 之 lifecycle **不作本票派工前置**：以其審查排程閘住本票，將使 P0A-01（blocked_by 本票）→ 本票 → SCHEMA-01 審查形成等待鏈，且 SCHEMA-01 之 CLOSED 與本票能否執行無因果關係
- `evidence_dependency`: `Skill-Advisor/deliver/ticket-rigor-gate-skill/RGR-RESP-SCHEMA-01_AI2_Evidence_Adjudication_20260913.md`（Codex 證據冊，SHA 指紋表齊備）；`RGR-P0A-01`（AI-3 true-call 最終收口對象）
- `coordination_dependency`: **同 `RGR-` family 序列令**——`RGR-RESP-SCHEMA-01` 現為 Active Ticket（lifecycle=`TICKET_RIGOR`，尚未收尾），依 `.claude/rules/concurrency.md` §2「同 worktree 同分支：同家族票 MUST 串行執行，禁止同時開工」，本票轉 `IN_PROGRESS` 前 MUST 先確認該票已離開 Active 狀態或已收尾；此與 execution_dependency 已駁回之「lifecycle 閘」為不同維度——後者是審查排程之因果誤判，前者是 worktree／分支序列化之現實約束，二者不可混為一談。**APP 端單線**（對 PLT）——禁對 PLT 做任何比對、驗證、同步或引用，援 `RGR-RESP-SCHEMA-01` 同裁決，此點不變。

> **循環閘門更正（2026-09-13）**：本票初版誤將 `RGR-P0A-01` 列入 `depends_on`，而 P0A-01 之 `blocked_by` 指向本票——二者互為前置，構成 Gate Self-Bootstrap Invariant 違反（同型事件：2026-08-24 FIX-F 死結，KAIROS 候選「循環閘門律」）。更正：P0A-01 為本票之**下游消費者**（本票收口後回填其 AI-3），非上游前置，已自 `depends_on` 移除。

## 檔案治理／來源說明

**承接**：`RGR-RESP-SCHEMA-01` AI-2 probe 之 STOP。schema 層目標已達成（provider 接受、thread `01a0999c` turn.completed、response 通過 `validate_response_structure`），但 runner 止於 `validate_model_contract`（L487）RTR-006、`exit 1`。

**真因（證據冊第 4.3 節，SKAD 已覆核）**：`rendered_prompt.txt` 與 `prompt_used.txt` SHA-256 **完全一致**且含完整 fixture 任務（`ticket_id: RGR-P0A-01-FIXTURE`、`finding_id: F-TRUECALL-1`、excerpt）；但同次 session rollout L9 之實際 user 任務訊息**只有首行** `TEMPLATE_VERSION: ticket-repair-prompt/v1`。模型收到一行模板版本號，回 `ticket_id:"unknown"`、`finding_results:[]`——**模型行為正確，是任務未送達**。

**傳輸入口（SKAD live 取證）**：`repair_runner.sh` L694–699 以 `"$CODEX_BIN" exec --json -m "$MODEL" -c "model_reasoning_effort=$EFFORT" --output-schema "$RESPONSE_SCHEMA" ... "$(cat "$PROMPT_FILE")"`——多行 prompt 以 **positional argument** 經 `codex.cmd`（Windows `.cmd` wrapper）傳遞。截斷之精確層次（git-bash → cmd.exe → node）**未定位**，屬本票 AI-0 標的，禁以推定替代。

**已駁回之歸因（禁重開）**：「profile 宣告 `gpt-5.6-terra/medium` 與 codex-cli 不符」不成立——session `turn_context` 實為 `gpt-5.6-terra/medium`，與 profile 一致；`gpt-5/unknown` 係模型生成 JSON 之自述，非 provider 識別證據。本票 **MUST NOT** 改 `profiles/model-profiles.v1.json` 之 model 名稱或 effort。

**RTR-006 契約盲點（本票不修，僅記載）**：`validate_model_contract` 以模型自述之 `run_summary.model`／`reasoning_effort` 比對宣告 profile。此對比以模型自述充當 provider metadata，屬契約設計面缺陷。傳輸修復後若模型能正確回報，RTR-006 自然通過；若仍不符，**停止回報升裁**，禁以回填 `run_summary` 預期值解除（`Ban#5`）。

**驗證缺口（本票收，AI-3）**：`validate_response_structure`（L467–483）檢查 JSON、`schema_version`、必要頂層欄位與多出之 finding IDs，但**不核 `ticket_id`、不拒空 `finding_results`**——本次空跑正是因此通過結構驗證。若不補，true-call AC 無法區分「真正處理了 finding」與「空跑」。

## Verify Config
TICKET_ID=RGR-CODEX-TRANSPORT-01
CREATED=2026-09-13
PRESET=hotfix
ALLOWED=.claude/skills/ticket-rigor-gate/runner/repair_runner.sh,.claude/skills/ticket-rigor-gate/runner/fixtures/true_call/
FORBIDDEN=.claude/skills/ticket-rigor-gate/runner/profiles/,.claude/skills/ticket-rigor-gate/runner/prompts/,.claude/skills/ticket-rigor-gate/runner/schemas/,.claude/skills/ticket-rigor-gate/runner/housekeeping.sh,.claude/skills/ticket-rigor-gate/runner/fixtures/no_call/,.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,app/src/,build.gradle,settings.gradle
MAX_LINES=N/A
DIFF_REF=3e1a675
ANCHOR_PROBED=2026-09-15 13:19
BASE_COMMIT=3e1a6755f5d745dcc6db1429b697ec9b758b6f85
TARGET_BLOB=.claude/skills/ticket-rigor-gate/runner/repair_runner.sh:4781bbcb
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**MAX_LINES=N/A 附註**（TPL v3.5 R3）：本票修 provider 傳輸邊界與 fail-closed 真呼叫收口，變更形狀由傳輸層定位結果決定，不可先驗估算。

**`INTRODUCED_CAPABILITY=NONE` 說明**：stdin 傳遞為既有 `codex exec` 既有能力之使用方式變更，非新 capability token；若 AI-1 最終採用之方案引入新機制（如 wrapper script），MUST 停止回報改 Rev 並重判 6a。

## Action Items

### AI-0: 基線、傳輸截斷定位與現值復驗（派工後、修改前）
- **動作 A（基線）**: `git rev-parse HEAD`、`git status --short`（**MUST clean**）、`git hash-object` 回填三欄。並以 `git merge-base --is-ancestor c9d1870 HEAD` 確認 schema 修正在祖先鏈中——**不在 → 停止回報**（此為本票唯一之上游前置，與任何票之 lifecycle 無涉）。
- **動作 B（行號與字面）**: 逐字確認 `repair_runner.sh` L694 `if ! "$CODEX_BIN" exec --json \`、L695 `-m "$MODEL"`、L696 `-c "model_reasoning_effort=$EFFORT"`、L697 `--output-schema "$RESPONSE_SCHEMA"`、L699 `"$(cat "$PROMPT_FILE")"`（全檔唯一）；L467 `validate_response_structure()`、L487 `validate_model_contract()`、L493 RTR-006 訊息行。
- **動作 C（截斷層次定位，零模型呼叫）**: 以**不呼叫模型**之接收端測試定位截斷發生層（候選：git-bash argv → `codex.cmd` wrapper → node argv）。測試輸入 MUST 涵蓋：多行、中文、單雙引號、反斜線、首行與末行、fixture ID 字面。接收內容 MUST 與 `rendered_prompt.txt` **全文逐字比對**（非僅首行）。**定位不出明確層次 → 停止回報**，禁以「改 stdin 應該就好」推定續作。
- **動作 D（stdin 可行性）**: 以本機 `codex exec --help` 確認 `-` 之 stdin 入口存在與語義（證據冊載 0.145.0 help 支援，屬**待驗**）；**並列引 `.claude/skills/sv/scripts/cross_review.sh` L400-404 作同 repo、同 codex-cli 版本之既有生產級 precedent**——該腳本已以 `codex exec ... - < "$PAYLOAD"`（stdin，非 positional argument）成功傳輸含中文／多行／大型 payload（SV report＋diff），且為本 repo `CLAUDE.md` SOP 4.5 強制不可跳過之 Cross-Review 閘，屬比 `--help` 文字更強之可行性佐證。兩者結論須一致比對回填；若 `--help` 語義與 `cross_review.sh` 實測行為不符，**MUST** 如實記載差異，不得擇一略過。不存在或語義不符 → 停止回報改議方案。
- **動作 E（fixture target 邊界）**: **已裁定（SKAD 2026-09-15，丙案——A／B 均不採）**：`RGR-P0A-01` 原字面「target 限 `plans/cso_briefing/project/`」屬開票方過度指定，且與該票 L105「`fixtures/` 即 workflow sandbox／test-only」自身宣告矛盾；約束目的為「非生產碼、非 `app/src/`、可回滾、test-only」，`fixtures/true_call/` 全數滿足且優於 `plans/`（後者為真實票檔與交付物所在之治理文件樹，於其中 mutate 將使測試寫入面與票檔異動於 `git diff` 混雜）。P0A-01 已 Rev B 更正字面。**結論：現行 `fixtures/true_call/target_true_call.md` 即為正解——不遷移、ALLOWED 不擴、true-call 證據可直接回填 P0A-01 AI-3。本動作降為唯讀確認該路徑現值未變。**
  以下原待裁選項清單已失效，保留供追溯：
  - **選項 A（維持現行 fixtures/true_call/）**：不動 Verify Config；本票 AC（true-call evidence record）與既有 fixture 檔直接有效。但 `RGR-P0A-01` 若要求以本票 true-call 證據回填其 AI-3，其票面「plans/ sandbox 非 app/src」之要求將不被滿足，`RGR-P0A-01` 需另補一次獨立 true-call 才能收口，**不構成本票下游消費者之直接回填**（違背票頭「本票收口後回填其 AI-3」之設計前提）。兩問預篩：①違反本票自身 HC？否。②自我抵消本票存在理由？否（true-call 本身仍可收口，僅下游銜接失效）。→ 非 DISQUALIFIED，但需揭露此代價。
  - **選項 B（遷至 plans/cso_briefing/project/ sandbox）**：對齊 `RGR-P0A-01` 要求，使其可直接回填 AI-3。但現行 `ALLOWED=repair_runner.sh,fixtures/true_call/` **未涵蓋** `plans/cso_briefing/project/` 路徑，執行此選項 **MUST** 同批修訂 Verify Config 之 `ALLOWED`（或改將 sandbox 目標限定於 `.tmp/` 下之暫存路徑並由 `RGR-P0A-01` 認可等效），否則寫入行為落在宣告範圍外。兩問預篩：①違反本票自身 HC？若未同步修訂 ALLOWED 即動筆寫入 plans/ 路徑 → 是（違反「git diff --stat 僅涉及 ALLOWED 檔案」AC 與票面範圍紀律）；若同批修訂 ALLOWED 則否。②自我抵消存在理由？否。→ 條件式可行，執行前 MUST 先完成 ALLOWED 修訂之裁決。
  - 回報現值並附上述雙選項機械後果，交 Akira 裁決；**未裁定前禁執行 true-call**。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成

### AI-1: 修復 prompt 傳輸
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`
- **現值**（L699）: `      "$(cat "$PROMPT_FILE")" > "$EVENT_STREAM" 2>"$ARTIFACTS_DIR/stderr.log"; then`
- **目標值**: 依 AI-0 動作 C／D 之定位結果，改為可完整傳遞多行 prompt 之形式（優先候選：stdin `codex exec ... -` ＋ `< "$PROMPT_FILE"`）。**MUST** 保留 `-m`／`-c model_reasoning_effort`／`--output-schema` 三旗標與 `$EVENT_STREAM`／`stderr.log` 之重導向語義。**MUST NOT** 改 prompt 內容、模板、profile、schema。
- **AI-0 動作 C／D 結論採信（SKAD 2026-09-15）**：截斷層定位於 **cmd.exe 對 `%*` 之巨集展開**（`.cmd` 轉發無法於單一參數內保留內嵌換行；POSIX `"$@"` 轉發逐位元組完整），最小重現與事故症狀逐字吻合。stdin 入口經 `codex exec --help`（0.145.0）與同倉生產級先例 `.claude/skills/sv/scripts/cross_review.sh:400`（`codex exec ... - < payload`；L63 以 `command -v codex` 解析）雙重確認。
- **修法收窄（本 AI 唯一路徑）**: **僅改傳輸為 stdin**。**MUST NOT** 一併更動 `CODEX_BIN`（L34 `"codex.cmd"`）——stdin 不經 argv，`.cmd` wrapper 對 stdin 之轉發不受 `%*` 限制，故改 `CODEX_BIN` 非修復所需；`codex.cmd` 與無副檔名 `codex` 之 PATH 解析差異屬**獨立行為變更**，如認有必要另票處置。若實測顯示 stdin 經 `.cmd` 仍失敗 → **停止回報**，禁自行改 `CODEX_BIN` 作為替代修法。
- **驗收前提**：接收端全文比對通過（AI-0 動作 C 之同一測試重跑，修後 MUST 全文一致）。**已驗證**：重建同一 `.cmd` 轉發鏈之 stdin 版本重現測試（`probe_stdin.cmd` + `probe_stdin_inner.js`，同一 test_input.txt），`stdin_len=299`，內容與原始輸入逐位元組相同（含多行、中文、引號、反斜線、首末行標記）——stdin 不經 `%*` 巨集展開，不受該層限制。
- [x] 完成

### AI-2: `validate_response_structure` 補空跑守門
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`（L467–483 區）
- **目標值**: 於既有檢查後新增二項，沿用既有失敗碼與訊息形狀（**禁新增 failure code namespace**）：
  - `ticket_id` MUST 等於 payload 之 `ticket_id`，不符即拒。
  - `finding_results` 為空陣列而 payload 有 findings 時 MUST 拒（空跑守門）。
- **MUST NOT** 改動 `validate_model_contract`、CB 區、`RTR_RETRYABLE`、`report_records()`、`blocker_preflight()`。
- **已驗證**（無模型呼叫，函式隔離抽取測試）：3 案例——空跑（finding_results=[] 而 payload 有 1 finding）拒絕（RTR-005，沿用既有碼）；`ticket_id` 不符拒絕（RTR-005）；正常案例通過（exit 0）。`git diff` 確認僅新增 13 行於 `validate_response_structure()` 內，`validate_model_contract`／CB 區／`RTR_RETRYABLE`／`report_records()`／`blocker_preflight()` 零改動。
- [x] 完成

### AI-3: bounded true-call 收口與雙回歸
- **檔案**: `fixtures/true_call/`；執行期 `.tmp/rigor-gate/ticket-repair/` artifact
- **動作**:
  - target 依 AI-0 動作 E 之裁定歸位後，執行**一次** bounded true-call（必要之最小重試每次 MUST 有新證據）。
  - 驗收 MUST 同時成立：正確 `ticket_id`、`F-TRUECALL-1` 有實質處理結果（非空集合）、模型設定可追溯、八層驗證走完、rollback／report 行為正確、runner `exit 0`。
  - 重跑 `run_no_call_tests.sh`（P0A-01 五組）確認 5/5 fail-closed 不變。
  - 保留與本票 ID／worktree／HEAD／DIFF_REF 綁定之 evidence。
- **停止條件**: RTR-006 仍觸發（模型自述與 profile 不符）→ 停止回報升裁，**禁回填 `run_summary` 預期值解除**；出現非預期 target mutation、rollback／report 回歸、或需改 FORBIDDEN 路徑方能續行 → 停止回報。

**執行記錄與 STOP（2026-09-15）**：`bash repair_runner.sh terra --payload fixtures/true_call/payload_addendum.json`，thread `01a0a569-b5c6-71a2-b209-74cce38588dc`。

**傳輸修復實證確認（重大進展）**：本次模型正確收到完整任務——`ticket_id` 正確回報為 `RGR-P0A-01-FIXTURE`（非 `unknown`）、`finding_results` 非空且對 `F-TRUECALL-1` 給出實質 `propose_repair` 判斷（含真實 evidence 引用 `evidence_true_call.md:1-3` 與具體 patch 內容）。`validate_response_structure`（含 AI-2 新守門）**通過**。此證實 AI-1 stdin 傳輸修復於真實 API 呼叫下有效，AI-0 動作 C 之根因定位正確。

**STOP 於 `validate_model_contract`（RTR-006）**：`run_summary.model="gpt-5"`（模型自述）不符宣告 profile（`terra`→`gpt-5.6-terra`）。與 `RGR-RESP-SCHEMA-01` AI-2 之前次觀察**同型再現**——非本票傳輸修復所能解決之契約設計面問題（票面 L25「RTR-006 契約盲點」已預先記載）。

**未預期 mutation／回歸檢查**：target `target_true_call.md` blob hash 前後同值（`e74cb5f9a12e180cdf3d199cba038fa34ae9f441`，未 mutate，rollback 語義正確——`validate_model_contract` 失敗於 patch-apply 迴圈之前即 exit，從未進入寫入階段）；`report_true_call.report.md` 新增一筆 RTR-006 rejected 記錄（`Ratification Status: pending`，未回填 accepted）；`run_no_call_tests.sh` 重跑 5/5 PASS，CB-4 未誤觸發；stderr 僅重現既有已知之 `models cache` 警告（`missing field base_instructions`，非阻斷，同 SCHEMA-01 先例僅記錄），無 `sandbox 1312` 警告。

**依票面明文停止回報升裁**：**MUST NOT** 回填 `run_summary` 預期值、放寬 `validate_model_contract` 或移除 RTR-006 取得 `exit 0`（Hard Constraint 5）。AI-3 true-call 收口（`exit 0`）與 `RGR-P0A-01` AI-3 回填**維持未完成**，待 Akira 對 RTR-006 契約設計面裁決後續作。
**執行記錄與 STOP（2026-09-16／17，`RGR-MODEL-CONTRACT-01` 落地後之 AI-3 重跑嘗試）**：
- 第一次：誤於 `runner/` 目錄執行 → `blocker_preflight()` 於模型呼叫前因 evidence 相對路徑無法解析回 `ANCHOR_MISSING_OR_UNREADABLE`；`model_invoked=false`、`automation_boundary` 全 `false`、未寫 report、無 target mutation。純 CWD 操作失誤，非回歸。**AI-3 MUST 於 repo root 執行。**
- 第二次（repo root，參數同 2026-09-15）：`circuit_breaker_precheck()` CB-1 於 Codex 呼叫前攔下，`exit 1`。producer：`report_true_call.report.md` 中 `ticket_id=RGR-P0A-01-FIXTURE` 最後兩筆（`2026-09-13T07:13:34Z`、`2026-09-15T14:12:56Z`）皆 `Failure Code: RTR-006`（非 retryable）→ 連續 2 次失敗。本次僅追加一筆 `escalated / CB-1 frozen` 記錄；無模型呼叫、無 target mutation、無 `exit 0` 宣稱。**STOP 合規**：兩筆歷史 RTR-006 於當時為真實失敗（gate 尚存），CB-1 不認識該 gate 已被 `RGR-MODEL-CONTRACT-01` 合法移除；編輯歷史／改 breaker 與 Hard Constraint 5 同構，禁。
- 一手核驗（2026-09-17）：CB-1 視窗鍵為 `ticket_id`（L272 `r_tid == $TICKET_ID`），與 `source_run_id` 無關；全檔無 CB-1 unfreeze 分支。`$TICKET_ID` 唯一入口為 payload `.ticket_id`（L666），下游 prompt（L335）、RTR-005 守門、`ARTIFACTS_DIR`（L672）、report record（L142）、CB-1／CB-4 視窗（L241／L272）全數消費同一值，無 CLI 旁路。本 AI-3 之驗收字面（上方「驗收 MUST 同時成立」與 AC）綁定 `F-TRUECALL-1` ＋ 八層驗證 ＋ `exit 0`，**未字面綁定 `RGR-P0A-01-FIXTURE`**；`fixtures/true_call/` 在本票 ALLOWED。

**Post-fix verification scenario（Akira 裁決 2026-09-17，fixture-only）**：
```
ticket_id=RGR-P0A-01-FIXTURE-POSTFIX
source_run_id=truecall-postfix-01
The pre-fix fixture scenario, ticket_id=RGR-P0A-01-FIXTURE, remains frozen
under CB-1 based on its historical RTR-006 records. Those records and the
CB-1 logic were not edited or bypassed.
The post-fix ID rotation is authorized only for this runner fixture scenario
to validate the post-RGR-MODEL-CONTRACT-01 behavior with the same finding,
target, evidence, report path, and rollback contract.
This is not an authorization to rotate ticket IDs for real tickets after
CB-1 triggers. For real tickets, changing ticket identity to escape a
circuit-breaker window remains prohibited.
```
- **唯一許可新增**：`fixtures/true_call/payload_postfix.json`，由 `payload_addendum.json` 複製，**僅**改 `ticket_id`／`source_run_id` 二值；`diff -u <(jq 'del(.ticket_id,.source_run_id)' OLD) <(同 NEW)` MUST 為空。
- **不得修改**：`payload_addendum.json`、`repair_runner.sh`、`target_true_call.md`、`report_true_call.report.md` 既有歷史（新 run 之 append-only record 為既有契約，允許）、schema／prompt／profile／CB／retry table／`.codex/sessions/`。
- **執行前檢（任一失敗即停，禁自行修正）**：①CWD＝repo root；②`jq -e '.ticket_id=="RGR-P0A-01-FIXTURE-POSTFIX" and .source_run_id=="truecall-postfix-01"' NEW` exit 0；③上述 `diff -u` 空；④`jq -r '.finding_ids[]' NEW` 含 `F-TRUECALL-1`；⑤`git check-ignore -v NEW` 未命中（命中即停，禁改 `.gitignore`）；⑥`git diff --name-only` 僅 `payload_postfix.json`。
- **AI-3 PASS 判準（八面同時成立）**：新 ID 正常進 runner（CB-1 未經編輯歷史清除）；Codex 收到完整 fixture task；response `ticket_id == RGR-P0A-01-FIXTURE-POSTFIX`；`finding_results` 非空且 `F-TRUECALL-1` 有實質 disposition／reason／patch；不因 `run_summary.model` generic 自述被拒；target mutation／rollback／report 符八層驗證；runner `exit 0`；report 追加新 record 而歷史未動。「模型被呼叫」或「結構有效」單獨**不得**宣告 PASS。
- **失敗即停**：非零 exit、CB-1 再攔、ticket ID mismatch、空 finding、target／rollback 異常、需改未授權路徑 → 停止並記錄真實 evidence；**禁建第二變體**、禁改 payload 其他欄、禁改 breaker。
- **回流六項**：①忽略二鍵後之 payload diff；②command／exit code／`model_invoked`／`CODEX_CALL_COUNT`；③response ticket_id／`F-TRUECALL-1` result／target-rollback 結果；④report 新增段（勿重貼歷史）；⑤`git diff --name-only`＋`--check`；⑥KAIROS 候選已記未修之確認。

**KAIROS candidate（CONFIRMED observation，本輪不修）**：CB-1 frozen record self-counting。L290 `append_report_entry` 第 4 參數（`failure_code`，簽章 L134）寫入 `n/a`；`report_records()`（L159）parse 為 `r_fc`；L273 predicate `-n && != "none"` 計入。每次 CB-1 STOP 自身增加連續失敗計數，無退出條件。Scope：本 transport／model-contract 序列未改 CB-1 邏輯、歷史記錄、retry table、breaker policy 或 unfreeze 行為。

**Post-fix 執行記錄與 PASS（2026-09-17）**：`payload_postfix.json`（由 `payload_addendum.json` 複製，僅改 `ticket_id=RGR-P0A-01-FIXTURE-POSTFIX`／`source_run_id=truecall-postfix-01`，`diff -u <(jq del OLD) <(jq del NEW)` 空）於 repo root 執行 `bash repair_runner.sh terra --payload .claude/skills/ticket-rigor-gate/runner/fixtures/true_call/payload_postfix.json`。

**回流六項**：
①payload diff（忽略 `ticket_id`／`source_run_id`）：空。
②command／exit／`model_invoked`／`CODEX_CALL_COUNT`：`"$CODEX_BIN" exec --json -m gpt-5.6-terra -c model_reasoning_effort=medium --output-schema ... - < PROMPT_FILE`；`exit 0`；`model_invoked=true`；`CODEX_CALL_COUNT=1`；thread `01a0adec-b8e8-78c2-ac69-18cf52ad6694`。
③response `ticket_id="RGR-P0A-01-FIXTURE-POSTFIX"`（相符）；`F-TRUECALL-1` 實質 `disposition=propose_repair`，`reason` 引用 `evidence_true_call.md:1-3`，`patch.replacement_text="\nAddendum: FIXTURE_CONSTANT = 42\n"`；target `e74cb5f9…`→`693c701…`，僅新增 3 行（含 1 空行 + 1 內容行，addendum-only，未動既有內容），rollback 語義正確（未 mutate 前提之後才寫入）。
④report 新增段：`report_true_call.report.md` 尾端新增一筆 `2026-09-17T05:53:18Z` 記錄（`Apply Status: applied`／`Failure Code: none`），舊三筆歷史（2 筆 RTR-006 + 1 筆 CB-1 frozen）逐字未動。
⑤`git diff --name-only` 於此次 postfix 呼叫僅涉及 `target_true_call.md`／`report_true_call.report.md`（+ untracked `payload_postfix.json`）；`git diff --check` exit 0（僅 CRLF 提示，非錯誤）。**過程中揭露兩項與 postfix 呼叫本身無關之異動**：(a) `plans/.../deliver/ACAGOV-RUNTIME-TABLE-01_go-lane.md`——foreign dirty，本票／本次執行從未讀寫，與 postfix payload 無因果關係；(b) `runner/.claude/skills/.../fixtures/true_call/report_true_call.report.md`（untracked 巢狀目錄）——本票執行者上一輪誤於 `runner/` 目錄下執行造成之殘留，內容僅一筆該次逃逸之 escalated 記錄，未含任何 target mutation；**揭露一個未回報之 runner 潛在缺陷**：`blocker_preflight()` 之 report-append 邏輯未將 payload `quality_report.report_path` 錨定 REPO_ROOT，於非 REPO_ROOT CWD 呼叫時會寫入錯誤巢狀路徑（本票未修此缺陷，記載供後續票承接）。兩項均非 postfix payload 造成，Akira 已核驗並裁定不影響本次 PASS 判定。
⑥KAIROS 候選（CB-1 frozen record self-counting）：本輪未修，維持既有 scope 邊界。

**AI-3 PASS 判準核對（八面）**：①新 ID 正常進 runner（CB-1 未經編輯歷史清除，`ticket_id=RGR-P0A-01-FIXTURE-POSTFIX` 為全新視窗，0 歷史）✅；②Codex 收到完整 fixture task（`prompt_used.txt`＝`rendered_prompt.txt`）✅；③response `ticket_id` 相符 ✅；④`finding_results` 非空且 `F-TRUECALL-1` 有實質 disposition/reason/patch ✅；⑤未因 `run_summary.model="gpt-5.6"` generic 自述被拒（`validate_model_contract` 已不存在，無此判定路徑）✅；⑥target mutation／rollback／report 符驗證 ✅；⑦`exit 0` ✅；⑧report 追加新 record 而歷史未動 ✅。八面同時成立，PASS。

`run_no_call_tests.sh`（P0A-01 五組）postfix 呼叫後重跑：`PASS=5 FAIL=0`（`anchor_missing`／`write_mode_forbidden`／`irreversible_target`／`write_mode_forbidden_flags`／`cb4_not_latched_on_rerun` 全綠），fail-closed 不變。

Evidence 綁定：Ticket=`RGR-CODEX-TRANSPORT-01`；worktree=`D:/DevSources/AndroidProj/SmartCameraApp1`；commit SHA（本次執行前 HEAD）=`d3835bb4822d380def79aebf8b26236da2211c8a`；DIFF_REF=`3e1a675`；thread=`01a0adec-b8e8-78c2-ac69-18cf52ad6694`；artifact 落 `.tmp/rigor-gate/ticket-repair/RGR-P0A-01-FIXTURE-POSTFIX/truecall-postfix-01/`。

- [x] 完成（post-fix scenario 執行並 PASS，2026-09-17；8/8 判準成立，no-call 回歸 5/5 PASS）

## Hard Constraints
1. 禁改 `profiles/model-profiles.v1.json`（model 名稱、effort 均禁）——「profile 不相容」歸因已於 2026-09-13 駁回
2. 禁改 `prompts/`、`schemas/`（含 `c9d1870` 之 response schema 成果）
3. 禁改 `blocker_preflight()`、CB-1～CB-4、`RTR_RETRYABLE`、六驗證函式本體（`validate_response_structure` 之新增守門為本票 AI-2 明示例外）、`report_records()`
4. 禁新增 failure code namespace 或 `Apply Status` 值
5. **禁以回填 `run_summary` 預期值、放寬 `validate_model_contract` 或移除 RTR-006 來取得 `exit 0`**——模型自述與可信執行設定之契約區分須明確
6. 禁觸 PLT（`mcp-ticket-platform`）任何檔案：禁比對、禁 hash 驗證、禁同步、禁引用其現況作判準或證據
7. true-call target 限 AI-0 動作 E 裁定之 sandbox；禁觸 `app/src/`、真機、帳號、外部服務、release、migration
8. 禁擴張為一般性重構、禁改名無關變數、禁 TODO()／placeholder
9. ⚠️ DEFENSIVE WARNING 承接：Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

## Error Handling
- AI-0 動作 A：工作樹非 clean，或 `c9d1870` 不在 HEAD 祖先鏈中 → 停止回報
- AI-0 動作 C：截斷層次無法明確定位 → 停止回報，禁推定續作
- AI-0 動作 D：stdin 入口不存在或語義不符 → 停止回報改議方案
- AI-0 動作 E：target 路徑現值不為 `fixtures/true_call/target_true_call.md` → 停止回報（邊界已於 2026-09-15 裁定為丙案，禁自行遷移、禁擴 ALLOWED）
- AI-1：修復需動 `prompts/`／`profiles/`／`schemas/` 方能完成 → 停止回報
- AI-3：RTR-006 仍觸發 → 停止回報升裁（禁繞過）；`sandbox 1312`／`models cache` 警告再現且阻斷 → 停止回報，禁繞過 sandbox
- **[標配]** `git check-ignore -v .claude/skills/ticket-rigor-gate/runner/repair_runner.sh` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** AI-0 動作 C 之截斷層次定位結論回填（含測試方法、輸入集合、各層接收內容比對結果）——見 AI-1「AI-0 動作 C／D 結論採信」段：截斷層定位於 cmd.exe 對 `%*` 之巨集展開，`probe_stdin.cmd`＋`probe_stdin_inner.js` 重現測試涵蓋多行／中文／引號／反斜線／首末行，`stdin_len=299` 逐位元組相符
- [x] **[行為性]** 修後接收端全文比對：`codex` 實際收到之任務輸入與 `rendered_prompt.txt` **逐字一致**——見 AI-1「驗收前提」段，重跑同一測試確認修後全文一致
- [x] **[行為性]** bounded true-call：`ticket_id` 正確、`finding_results` 非空且含 `F-TRUECALL-1` 之實質處理結果、八層驗證走完、rollback／report 行為正確、runner **`exit 0`**——postfix 執行 2026-09-17 達成，見 AI-3「AI-3 PASS 判準核對（八面）」
- [x] **[行為性]** 空跑守門：given payload 有 findings 而 response 回空 `finding_results`，expect `validate_response_structure` 拒絕（沿用既有失敗碼）；given `ticket_id` 不符，expect 拒絕——見 AI-2「已驗證」段，函式隔離抽取 3 案例（空跑拒絕 RTR-005／ticket_id 不符拒絕 RTR-005／正常案例通過）
- [x] **[行為性]** no-call 回歸：`run_no_call_tests.sh` 5/5 PASS，receipt 六旗標全 `false`、`exit 1` 不變——postfix 呼叫後重跑：`PASS=5 FAIL=0`（含 `cb4_not_latched_on_rerun`）
- [x] **[可達性]** producer 證據欄：傳輸行 **L695**（`"$CODEX_BIN" exec --json \`）、`validate_response_structure` 新增守門行 **L480**（`ticket_id` 比對）／**L486**（空 `finding_results` 拒絕）；`validate_model_contract` **已不存在**——由協同票 `RGR-MODEL-CONTRACT-01`（commit `ab23c62`）依其票面授權與本票 `coordination_dependency` 合法刪除，非「未被改動」而是「經協調授權之移除」，如實記載偏離原始 AC 字面預期（該函式曾存在於 L500-510，現整段刪除，本票未觸碰此變更，變更歸屬另票）
- [x] **[存在性]** `git diff` 逐 hunk：本票本輪 diff 僅涉 `fixtures/true_call/` 與 `fixtures/sandbox/reports/`（no-call 回歸受命重跑之既有 receipt 更新），`profiles/`、`prompts/`、`schemas/`、CB 區、`RTR_RETRYABLE`、`blocker_preflight()`、`report_records()` 零改動；`validate_model_contract` 之刪除非本票 diff（見上一項說明，歸屬 `RGR-MODEL-CONTRACT-01`）
- [x] **[存在性]** true-call evidence record 含 ticket ID、worktree、commit SHA、DIFF_REF、Codex 啟動、thread、provider response、exit status、report／rollback 結果——見 AI-3「Evidence 綁定」段
- [x] `bash -n .claude/skills/ticket-rigor-gate/runner/repair_runner.sh` PASS——exit 0
- [x] ~~`assembleDebug` PASS~~ (N/A: runner toolchain hotfix，`app/src/` 為 FORBIDDEN)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案——`repair_runner.sh` 本輪零改動；`fixtures/true_call/` 三檔（含新增 `payload_postfix.json`）於 ALLOWED；`fixtures/sandbox/reports/` 四檔為 AI-3 明文要求之 `run_no_call_tests.sh` 重跑受命副作用（既有 receipt 覆寫，非新增邏輯），非 ALLOWED 字面涵蓋但屬本 Action Item 直接指示之必要產物，如實揭露；`plans/.../ACAGOV-RUNTIME-TABLE-01_go-lane.md` 為無關 foreign dirty，**不歸屬本票**，另案處理不入本票 commit
