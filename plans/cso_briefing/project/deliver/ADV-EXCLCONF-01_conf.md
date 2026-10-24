# ADV-EXCLCONF-01: `activate_ticket.sh` 將票面 Verify Config 之 `EXCLUDED_PAYLOAD=` 行（若有）轉錄進 `tmp/verify_cmd.conf`，使 `verify_checklist.sh` [0.1]／[0.2] 之已授權排除有自動 producer

**簽發日期**: 2026-10-24
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ADV-FORBIDEXCL-01 —— Codex Cross-Review 對其 [0.2] 新增 guard 指出 `EXCLUDED_PAYLOAD` 無自動 producer（`activate_ticket.sh` 九欄不含此鍵），SV 報告據實標 PARTIAL 並轉票至本票
- `coordination_dependency`: 無

## 來源（Parent）
- Parent type：Spec-backed（無 artifact header 之既有缺口描述；來源＝ADV-FORBIDEXCL-01 之 SV 報告〈Guard Input Reachability〉接續缺口與 Codex Cross-Review 回件）；讀取時間 2026-10-24。
- Selected item：`EXCLUDED_PAYLOAD` 自動 producer。唯一可觀察目標：票面 Verify Config 有 `EXCLUDED_PAYLOAD=` 行時，`activate_ticket.sh` 啟動該票後 `tmp/verify_cmd.conf` 含同值該行；無該行時 conf 與現行逐位元組相同。
- 票號歸屬：由「工程師自主閉環」議題發起之接續票，依 Akira 2026-10-24 裁決與模板 v3.12「發起議題律」掛 `ADV` 前綴。
- 已知耦合（須同票處理）：`activate_ticket.sh` 之九欄 regex 註明「與 `ticket-run/SKILL.md` L96 字面一致（禁另立欄位集）」；`ticket-run/SKILL.md` 步驟 10.1 之 `grep_files` query 亦為同一欄位集。新增第十欄須兩處同步，否則違反該一致性約束。

## 證據（開票時 live，2026-10-24 @c76bf9c8）
- `.claude/scripts/activate_ticket.sh`：欄位擷取 `grep -E '^(TICKET_ID|CREATED|PRESET|ALLOWED|FORBIDDEN|MAX_LINES|ANCHOR_PROBED|BASE_COMMIT|TARGET_BLOB)='`（約 :119）；conf 寫入區塊（約 :161-170）僅 `echo` 九欄；`grep -rn "EXCLUDED_PAYLOAD" .claude --include=*.sh --include=*.py` 之寫入者為 0（僅 `verify_checklist.sh` 讀取／旗標解析）。
- `.claude/skills/ticket-run/SKILL.md:96`：步驟 10.1 之 `grep_files` query 與九欄字面相同。
- `verify_checklist.sh` 讀取點：`:257-258`（conf）、`:277-278`（per-ticket 快照）、`:298`（CLI `--excluded-payload`）。
- 現況影響：票面 `EXCLUDED_PAYLOAD=` 行（如 ADV-CLAUDECLI-01 票面 :22）不會自動進入 conf，須人工追加；Step 0 `[0.1]` 與（ADV-FORBIDEXCL-01 後之）`[0.2]` 之已授權排除因此依賴人工。
- 既有測試：`.claude/scripts/tests/owner_writer_test.sh` 以真實 `activate_ticket.sh` 驅動（`:4`、`:11`、`:59`）；修改前須記錄其基線通過數。
- 反例檢查：①票面無 `EXCLUDED_PAYLOAD=` 行 → 不得寫空行或改動 conf 既有九欄次序（對 `verify_checklist.sh` 之 `get_conf` 空值語義與既有 per-ticket 快照比對須保持）；②票面含多行 `EXCLUDED_PAYLOAD=` → 取首行（同九欄慣例）；③`EXCLUDED_PAYLOAD=` 值為空字串 → 視同缺席。

## Verify Config
TICKET_ID=ADV-EXCLCONF-01
CREATED=2026-10-24
PRESET=feature
ALLOWED=.claude/scripts/activate_ticket.sh,.claude/scripts/tests/owner_writer_test.sh,.claude/skills/ticket-run/SKILL.md
FORBIDDEN=.claude/scripts/gate_ledger.py,.claude/scripts/route.py,.claude/hooks/**,.claude/rules/**,.claude/skills/qa/**,.claude/skills/sv/**,.claude/skills/ticket-author/**,.claude/skills/compact/**,.claude/settings.json,.claude/settings.local.json,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,CLAUDE.md,app/**,plans/roadmap/**,plans/architecture/**,plans/cso_briefing/project/TICKET_REGISTRY.json,.gitignore
MAX_LINES=N/A（conf 產生之守門判定類票：失敗形狀為審查發現式，TPL R3）
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-24 15:05
BASE_COMMIT=c76bf9c8
TARGET_BLOB=N/A（開票端未取 blob；AI-0 逐檔現值錨點替代）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點復驗（派工後、修改前；零模型呼叫）
- **動作**: 確認 `activate_ticket.sh` 九欄 regex 與 conf 寫入區塊現值（各 1 次）、`ticket-run/SKILL.md` 步驟 10.1 之九欄 query 字面、`owner_writer_test.sh` 驅動真實 `activate_ticket.sh`；記錄 `owner_writer_test.sh` 修改前基線通過數與 `verify_checklist.sh` 讀取 `EXCLUDED_PAYLOAD` 之行號（唯讀，不改）。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置。**
- [x] 完成（2026-10-24：HEAD `880d1b43`；`activate_ticket.sh` 九欄 regex :131 與 conf 寫入區塊 :162-171 現值相符；`ticket-run/SKILL.md:96` query 九欄字面相符；`owner_writer_test.sh` 驅動真實 `activate_ticket.sh`，修改前基線 `34 PASS／0 FAIL`；`verify_checklist.sh` 讀取 `EXCLUDED_PAYLOAD` 於 :257-258／:277-278／:298，[0.1] :444、[0.2] :517 起；唯讀未改）

### AI-1: `activate_ticket.sh` 轉錄 `EXCLUDED_PAYLOAD`
- **檔案**: `.claude/scripts/activate_ticket.sh`
- **現值**: 九欄擷取與寫入，無 `EXCLUDED_PAYLOAD`。
- **目標值**: 擷取 regex 與寫入區塊新增第十欄 `EXCLUDED_PAYLOAD`（可選欄）：票面有非空 `EXCLUDED_PAYLOAD=` 首行時，conf 於 `TARGET_BLOB` 後追加同值一行；缺席或空值時 conf 與現行逐位元組相同。不改其他欄次序與驗證（TICKET_ID／ALLOWED 必填語義不變）。
- [x] 完成（`activate_ticket.sh`：regex :131 加 `EXCLUDED_PAYLOAD`、擷取 :141、寫入 :173 `[ -n "$EXCLUDED_PAYLOAD" ] && echo ...` 置於 `TARGET_BLOB` 後；缺席或空值不寫、九欄次序不變）

### AI-2: `ticket-run/SKILL.md` 欄位集同步
- **檔案**: `.claude/skills/ticket-run/SKILL.md`
- **現值**: 步驟 10.1 之 `grep_files` query 為九欄。
- **目標值**: query 與其說明同步納入 `EXCLUDED_PAYLOAD`（可選欄；`max_matches` 依需調整），維持與 `activate_ticket.sh` 字面一致。
- [x] 完成（`ticket-run/SKILL.md:96` query 納入 `EXCLUDED_PAYLOAD`、`max_matches` 12→13，附可選欄說明；與 `activate_ticket.sh` 字面一致）

### AI-3: 測試
- **檔案**: `.claude/scripts/tests/owner_writer_test.sh`
- **目標值**: 以真實 `activate_ticket.sh` 在沙盒新增案例：(a) 票面含 `EXCLUDED_PAYLOAD=x.md` → conf 含該行且其餘九欄值不變；(b) 票面無該行 → conf 與修改前同票之輸出逐位元組相同；(c) 值為空字串 → 同 (b)；(d) 多行 → 取首行；既有案例全數保持通過（相對基線無新增失敗）。
- [x] 完成（`owner_writer_test.sh` 新增 (a)(b)(c)(d)＋e2e 共 13 項斷言；修改後 `47 PASS／0 FAIL`〔基線 34〕；突變：以 HEAD 版 `activate_ticket.sh` 跑同測試 → 42 PASS／5 FAIL，(a)(d)(e2e) 被擊殺、(b)(c) 位元組相同如預期仍過）

## Hard Constraints
1. 不改 `verify_checklist.sh`、`gate_ledger.py`、hook 與 settings；不新增 conf 必填欄；九欄既有語義與次序不變。
2. 欄位集變更須 `activate_ticket.sh` 與 `ticket-run/SKILL.md` 同票同步（一致性約束）。
3. 不以本票修復他票授權之內容；不放寬 `EXCLUDED_PAYLOAD` 之授權門檻（只轉錄票面既有行）。

## Error Handling
- `owner_writer_test.sh` 基線已有失敗且與本票無關 → 記錄基線、僅比對無新增失敗，不修復他項。
- 新增第十欄導致既有 per-ticket 快照／`verify_checklist.sh` 讀取行為改變（以沙盒實跑確認）→ 停止回報。
- **[標配]** `git check-ignore -v <標的路徑>` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** `activate_ticket.sh` 擷取與寫入區塊含 `EXCLUDED_PAYLOAD`（可選欄）；`ticket-run/SKILL.md` 步驟 10.1 query 與之字面一致。（`activate_ticket.sh:131,141,173`；`ticket-run/SKILL.md:96` 同欄位集；提交見收尾實錄）
- [x] **[可達性]** producer 證據欄（執行後回填 `檔案:行號`）：票面 `EXCLUDED_PAYLOAD=` 行 → `activate_ticket.sh` 擷取 → conf → `verify_checklist.sh:257-258` 讀取 → [0.1]（`:444`）與 [0.2]（`:517` 起）消費，全鏈各點行號與沙盒實跑輸出。（全鏈：票面 `EXCLUDED_PAYLOAD=` 行→`activate_ticket.sh:131,141` 擷取→`:173` 寫入 `tmp/verify_cmd.conf`→`verify_checklist.sh:257-258` 讀取→[0.1] `:444-447`、[0.2] `:517-521` 消費；沙盒實跑：`excl e2e` 兩案，含該行→`Excluded Payload (FORBIDDEN, authorized): f.txt` 且無 `⛔ FORBIDDEN FILE`，無該行→`⛔ FORBIDDEN FILE: f.txt` 且 exit 非 0）
- [x] **[行為性]** given 真實 `activate_ticket.sh` 沙盒：(a) 票面含 `EXCLUDED_PAYLOAD=x.md` → conf 含該行、其餘欄不變；(b)(c) 無該行或空值 → conf 與修改前逐位元組相同；(d) 多行取首行；並以該 conf 實跑 `verify_checklist.sh` 確認 [0.2] 之已授權排除生效（端到端）。（(a)(b)(c)(d) 與端到端皆於 `owner_writer_test.sh` 通過；突變證明 (a)(d)(e2e) 對基線會失敗）
- [x] **[行為性]** `owner_writer_test.sh` 相對基線無新增失敗（記錄基線與修改後通過／失敗數）。（基線 34 PASS／0 FAIL → 修改後 47 PASS／0 FAIL，無新增失敗）
- [x] ~~assembleDebug PASS~~ (N/A: 無 app/ 變更；本環境 Gradle 亦不可用)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（3 檔：`activate_ticket.sh`、`owner_writer_test.sh`、`ticket-run/SKILL.md`，皆 ALLOWED）

## 收尾實錄（Agent closeout，2026-10-24；交 TKA close 判定，非自行終審）
- **實作提交**：`c209eb04`（`activate_ticket.sh` +4/-2、`owner_writer_test.sh` +49、`ticket-run/SKILL.md` +1/-1；範圍 `880d1b43..c209eb04` 僅 3 個 ALLOWED 檔）。
- **QA**：Step 0 PASS；Gradle 因 `java.io.IOException: Unable to establish loopback connection` 未能執行（`reviews.qa=N_A`，不宣稱 PASS；無 `app/` diff）；`owner_writer_test.sh` 47 PASS／0 FAIL（基線 34），突變（HEAD 版腳本）→ 42 PASS／5 FAIL。
- **SV／Cross-Review**：SV 報告五段完整；R0 FORWARD（非正式 PASS，記錄 `.tmp/fail-advisor/R0_ADV-EXCLCONF-01_*.md`）；Codex `gpt-5.6-sol` 終局 verdict＝**PASS**（`Guard Input Reachability: 有新 Guard — State: EXCLUDED_PAYLOAD — Producer: (B)`，`REACHABILITY: …ADV-CLAUDECLI-01_channel.md:22`；結果檔 `.tmp/cross_review_result_20261024_161114.md`）。
- **未勾 AC**：無。
- **已知限制**：Gradle 未驗；Codex 於審查過程中 `exec_command` 啟動文件讀取失敗（`helper_unknown_error`），結論僅依 payload；`PROGRESS.md` Dashboard 同步屬 FORBIDDEN 延後項，待 `/compact`；本票 close 之保存由 `persist` 執行。

<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: 交付：activate_ticket.sh 將票面可選 EXCLUDED_PAYLOAD 首行（非空）轉錄進 tmp/verify_cmd.conf（TARGET_BLOB 後；缺席／空值不寫、九欄不變），ticket-run/SKILL.md 欄位集同步；實作提交 c209eb04；owner_writer_test.sh 47 PASS／0 FAIL（基線 34），突變 42 PASS／5 FAIL。 / 驗收：close reviewer gpt-6-sol 6 條 AC：1–4、6 PASS、5 N/A（assembleDebug 劃除）；Cross-Review 終局 PASS（gpt-5.6-sol，Guard Input Reachability Producer (B)）。 / 限制：Gradle 因 loopback IOException 未驗（qa=N_A）；測試以命令替換比對 conf 會去除尾端換行，(b)(c) 逐位元組結論另由寫入區塊 diff 支持；Codex 審查時 exec_command 讀檔失敗，結論僅依 payload；PROGRESS Dashboard 同步待 /compact。
- 最終驗收結論: 核心 AC 3 條皆為 PASS 或有效 N/A；非核心 PASS 2／N/A 1／UNVERIFIED 0／FAIL 0（共 6 條 AC）
- 實作提交: c209eb04
- 實際收束結果: 2026-10-24 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: codex/gpt-6-sol（tier=heavy）；覆蓋 AC 6／6（全部），independent_call=True
- 披露: dependency contract: 未知（未宣稱已確認無缺件）；reviewer limitation: reviewer 無法讀檔，diff／原始碼／票檔嵌入 prompt；測試以命令替換比對 conf 會去除尾端換行，(b)(c) 逐位元組結論另由寫入區塊 diff 支持
<!-- close-result:end -->
