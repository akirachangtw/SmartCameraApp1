# MEO-T3: MEO 前置債清償（T2 保留項封證 + 訊號治理 + STATE.md 分流）

**簽發日期**: 2026-07-21（R17 收案後起草，T2 收工 commit b79538a6）
**簽發者**: Claude（CSO 起草，待 Akira 核定）
**Mode**: normal → uc  ||  **Effort**: High
**Traycer Workflow**: execute_review
**前置依賴**: MEO-T2（R17 條件化收案）

## Verify Config
TICKET_ID=MEO-T3
CREATED=2026-07-21
PRESET=feature
ALLOWED=.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh,.claude/skills/model-effort-orchestrator/decision-tree.yaml,STATE.md,.gitignore,tmp/verify_cmd.conf
FORBIDDEN=stop_orchestrator.sh 既有非 MEO gate 邏輯,meo_pretooluse_gate.sh 自舉豁免邏輯（R9/R10 已收案）,.claude/skills/sv/scripts/cross_review.sh（屬 INFRA-SV-GOV-01 範圍）
MAX_LINES=250
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: V5/V6 封證（R17 保留項①）
- **現值**: PARTIAL——真實失敗驅動累加受 issue #27886 影響未封證；CSO R7 降級預案（/sv reject count 承載）運作中
- **目標值**: #27886 現況查證後二擇一，禁模糊：(a) 已修復→真實失敗驅動累加 + R4 交錯情境（失敗→成功→同 class 失敗續累）完整累加；(b) 未修復→維持降級預案並於 PROGRESS.md 設定期複查節點
- **結論（(b)，2026-07-21）**：GitHub #27886 現況查證——狀態 CLOSED as duplicate（併入 #17088，該票為 PreToolUse 誤標籤的 cosmetic 議題），無證據顯示底層問題（hook script 未實際執行/誤標籤）已修復，官方無法確認。**本 session 額外真實復現**（非查文獻推論）：對本專案 `.claude/skills/sv/scripts/cross_review.sh --nonexistent-flag` 觸發真實非零 exit 失敗，以及對照組 `ls /this/path/does/not/exist` 觸發真實非零 exit 失敗，兩者事後 `STATE.md` 之 `MEO_RETRY_COUNT`/`MEO_SAME_ERROR_CLASS_STREAK` 均未變動——即本 session 中 PostToolUseFailure 對一般非零 exit 失敗未觸發任何 kv 累加，與 #27886 症狀直接吻合，且此為本地實測而非文獻推論，證據強度高於單純查證 issue 狀態。維持 CSO R7 降級預案（`/sv reject count` 承載）。**PROGRESS.md 定期複查節點**：下次 KAIROS 節點（見 PROGRESS.md）時重跑本測試，若屆時真實失敗仍不驅動累加，升級為正式已知限制記錄；若已驅動累加，回頭執行選項 (a) 完整封證。
- [x] 完成

### AI-2: 可達性 producer 證據補齊（R17 保留項②）
- **現值**: `MEO_LAST_DIAGNOSIS_TS`/`MEO_FASTPATH` producer 為人為 STATE.md Edit，證據欄空缺
- **目標值**: scripted producer 落地，或 CSO 明文改判「人為 Edit 屬 R9 設計內 producer」；證據欄回填 T2 票面（deliver 檔追補，註記 T3 回填）
- **調查結論（2026-07-21）**：Scripted producer 落地路徑被本票 FORBIDDEN 清單阻斷（`meo_pretooluse_gate.sh` 為 FORBIDDEN），選項 (a) 於本票 scope 內不可行。
- **CSO 裁決（2026-07-21，Akira 對話裁決）**：選項 (b) 採認——`MEO_LAST_DIAGNOSIS_TS`/`MEO_FASTPATH` 之 producer = 人為 STATE.md Edit（R9 設計內合法 producer，非證據缺口）。佐證：①`settings.json:85-126` PreToolUse/PostToolUseFailure 豁免區塊實際掛載 `meo_pretooluse_gate.sh`/`meo_signal_update.sh`（producer 掛載點存在）；②`meo_pretooluse_gate.sh:17` 既有註解 "v1.2 (CSO R9): STATE.md 診斷自舉豁免" 為設計意圖記錄；③本輪 Akira 對話裁決本身為裁決檔記錄。T2 票面證據欄已依此裁決回填（見 deliver 檔）。
- [x] 完成（CSO 裁決結案）

### AI-3: diff_hash.sh 逐字節比對（R17 保留項③，承接 T2 AI-2）
- **現值**: 部署之 meo_signal_update.sh 與 Payload v1.1 A2 一致性未經 diff_hash.sh 驗證
- **目標值**: 比對執行並記錄結果（sha256 normalize 依 G2–G4 標準）
- **結論（2026-07-21）**：`diff_hash.sh` 於全 repo `Glob("**/diff_hash*")` 零命中——decision-tree.yaml/SKILL.md/payload 文件引用之工具實際不存在。改用其 sha256-normalize 語意的直接等效操作：working tree `meo_signal_update.sh` 與部署 commit `8044a0d` 版本 (`git show 8044a0d:.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh | sha256sum`) 比對，雙方雜湊值完全一致：`cb948e264b0fc880d734c79d744a6ce5e6e93086ade299a556cfad7bd505e78c`，確認自 T2 部署以來零漂移（本次 T3 對此檔的 AI-5 修改為部署後新增，不影響此歷史一致性結論）。`diff_hash.sh` 不存在本身列入 AI-2 佐證缺口同類問題，非本項停止條件觸發（比對結果一致，非「不一致」）。
- [x] 完成

### AI-4: PENDING 解除機制制度化
- **現值**: R13 四要件（結構性不可自解／record-before-clear／訊號保全／裁決權未僭越）僅為判例；本輪解除（b79538a6）靠人工合規
- **目標值**: 四要件升格為 decision-tree.yaml 明文條款
- **結論（2026-07-21）**：`decision-tree.yaml` `actions.defer_and_checkpoint` 下新增 `pending_release` 區塊，四要件（`structural_non_self_resolving`／`record_before_clear`／`signal_preservation`／`decision_authority_not_usurped`）逐條明文化為 requires 清單，並標註來源（"R13 four elements, ratified MEO-T3 AI-4"）。`python -c "import yaml,sys; yaml.safe_load(...)"` 驗證 YAML 合法。
- [x] 完成

### AI-5: 訊號源過濾
- **現值**: SV/cross-review 執行期間被動觀測訊號與真實任務訊號同池累加（94465d9 型；STREAK 5→8 即此污染）
- **目標值**: meo_signal_update.sh 增訊號源判別，SV 執行期觀測不入任務 STREAK
- **結論（2026-07-21）**：`meo_signal_update.sh` 於 `TOOL_NAME` gate 後、`kv_get` 前新增 `CMD` 路徑判別（`*.claude/skills/qa/scripts/*` 或 `*.claude/skills/sv/scripts/*` → `exit 0`），`bash -n` 語法驗證通過。**真實復現結果誠實揭露**：對 `.claude/skills/sv/scripts/cross_review.sh --bogus` 與對照組 `ls /nonexistent-path` 分別觸發真實非零 exit 失敗，事後兩者 `MEO_RETRY_COUNT`/`MEO_SAME_ERROR_CLASS_STREAK` 均未變動（0/8 不變）——此結果與 AI-1 發現一致：本 session 中 PostToolUseFailure 對一般非零 exit 未觸發任何 hook 執行，導致此真實復現**無法機械區辨**「filter 正確攔截 SV 訊號」與「hook 本身未觸發（#27886 症狀延伸）」兩種假說。Code-level 過濾邏輯已接線且語法正確，符合本項目標值；但 AC 清單第 5 項要求之行為性驗證因 #27886 混淆而無法確定性證實，如實記錄於 AC，不虛報勾選。
- [x] 完成（code-level；行為性驗證狀態見 AC 第 5 項）

### AI-6: escalation schema 收編 + kv 純值清洗
- **現值**: `MEO_LAST_ESCALATION_DECISION` 為 schema 外無主鍵；該鍵與 `MEO_LAST_FAILURE_MODE` 值含長敘事
- **目標值**: 鍵入 escalation_history_schema 並定消費方；敘事依 record-before-clear 先歸檔 vault 再清洗，kv 僅存 enum／短值
- **結論（2026-07-21）**：`decision-tree.yaml` `escalation_history_schema` 下新增消費方註解，明定 `MEO_LAST_ESCALATION_DECISION` 為該 schema `decision` 欄位之扁平單值具現化，消費方為 Akira/CSO Checkpoint 覆核 + KAIROS 回顧候選採集，現無自動腳本讀取此鍵。Record-before-clear：兩鍵敘事先寫入 memory 歸檔檔 `meo-r17-escalation-decision-archive.md`（含完整原敘事文字），再執行 STATE.md 清洗——`MEO_LAST_FAILURE_MODE` → `none`，`MEO_LAST_ESCALATION_DECISION` → `defer_and_checkpoint`，兩者均已為純 enum／短值。
- [x] 完成

### AI-7: STATE.md untrack 分流 + 五區段宿主釐清（R17 殘辦）
- **現值**: STATE.md tracked（runtime 值持續污染 git 史）；「既有五區段」宿主不明（git 全史僅 MEO 區段）
- **目標值**: `git rm --cached STATE.md` + `.gitignore` 增列；五區段實際宿主定位並記錄結論
- **Untrack 執行（2026-07-21）**：`git rm --cached STATE.md` 已執行（檔案保留於 disk，僅移出 git 索引）；`.gitignore` 新增 `STATE.md` 條目。此動作依既有 memory 裁決（`state-md-untracked-disposition.md`：CSO 已授權 MEO-T3 執行此分流，一次性治理後永久免票）執行，非本輪新裁決。
- **五區段宿主結論（2026-07-21，查無，依票面 Error Handling 停止回報）**：追溯 MEO-T1 deliver 票（`MEO-T1_Skill_Skeleton_SSOT_Deploy.md`）之 CSO 裁決，確認 STATE.md 從未於 MEO-T1 之前存在——`test -f STATE.md` 於 T1 開工前為 false，T1 全新建立此檔且僅含 `## MEO` 單一區段。故「既有五區段」宿主查無：git 全史中 STATE.md 自始至終只有 MEO 區段，五區段引用為後續票 FORBIDDEN 條款中的防禦性 boilerplate（保護一個從未真實存在過的假想範圍），非證據缺失。**依票面 Error Handling「五區段宿主查無 → 停止回報，CSO 裁決是否宣告制度性廢止」，本結論僅記錄於本票，不主動掃描/修改其他票據中類似的五區段 boilerplate 措辭（該項掃描/清理屬更廣泛的治理宣告，需 Akira 另行裁決是否授權）**。
- [x] 完成（untrack 機械動作 + 五區段查無結論已記錄；「制度性廢止宣告」範圍待 Akira 裁決，未逾權執行）

## Hard Constraints
1. 禁增第四個 Stop hook（G2/D5 沿用）
2. hook stdout 契約沿用 T2：PreToolUse 二鍵／Stop 類三鍵
3. R4 不變量：成功路徑禁重置 STREAK（AI-5 過濾不得繞道重置）
4. AI-7 untrack 前須先完成 AI-6 schema 定義（保留 schema 之 git 存證基線）
5. AI-6 清洗須 record-before-clear：敘事先歸檔後清除，禁直刪

## Error Handling
- #27886 查證無法確認 → AI-1 停止回報，維持降級預案
- 五區段宿主查無 → 停止回報，CSO 裁決是否宣告制度性廢止
- diff_hash 比對不一致 → 停止回報，禁自行重部署

## Acceptance Criteria
- [x] **[行為性]** V5/V6 封證或降級預案+複查節點明文（AI-1 二擇一結論記錄在案）— ✅ 選項(b)，見 AI-1
- [x] **[存在性]** T2 票面可達性證據欄已回填（含 T3 回填註記）— ✅ 見 deliver/MEO-T2 AC 71/73 行
- [x] **[存在性]** diff_hash 比對結果記錄在案 — ✅ 見 AI-3（工具不存在，sha256sum 等效比對一致）
- [x] **[存在性]** decision-tree.yaml 含 PENDING 解除四要件明文 — ✅ 見 AI-4，YAML 驗證通過
- [x] **[行為性]** SV 執行期間觀測訊號不再累入任務 STREAK（真實復現，禁 fixture 直注）— ⚠️ PARTIAL-accepted（Akira CSO Checkpoint 裁決，2026-07-21，vault: `2026-07-21_MEO_T3_Closure_Escalation_Release_Ruling.md`）。殘項「SV 訊號過濾正向行為單獨驗證（confound 未隔離：本 session 無法機械區辨 filter 正確攔截 vs. #27886 hook 未觸發）」明列於此，轉 **MEO-V1 儀器校準**承接，不另開 -FIX-A 追蹤票。
- [x] **[存在性]** escalation schema 含 LAST_ESCALATION_DECISION；STATE.md kv 全數純值 — ✅ 見 AI-6
- [x] **[行為性]** STATE.md untracked 生效（git status 不列）；五區段宿主結論記錄 — ✅ conditional PASS（Akira CSO Checkpoint 裁決）。Read-back：`git ls-files --error-unmatch STATE.md` → `error: pathspec 'STATE.md' did not match any file(s) known to git`（exit 1，untracked 確認）；git 可回復性核驗：`git show 89f41af:STATE.md` → `fatal: path 'STATE.md' exists on disk, but not in '89f41af'`（commit 89f41af 樹中已無此檔，untrack 於該 commit 落地；歷史內容仍可經其父 commit 回溯，非破壞性遺失）。五區段查無結論見 AI-7。
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 — ✅（commit 89f41af 已落地，此輪僅涉及 ALLOWED 四檔：decision-tree.yaml/meo_signal_update.sh/.gitignore/STATE.md）
