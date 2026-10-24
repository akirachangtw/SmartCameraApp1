# ADV-FORBIDEXCL-01: `verify_checklist.sh` [0.2] 禁區檢查對 `EXCLUDED_PAYLOAD` 具名檔之處理比照 [0.1]（已授權排除→不判 FORBIDDEN FAIL）

**簽發日期**: 2026-10-24
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ADV-CLAUDECLI-01 —— Step 0 實測 [0.1] 採排除而通過、[0.2] 因他 session `72352908` 觸及 FORBIDDEN 之 `.claude/PROGRESS.md` 判 FAIL 之事件；INFRA-MAXLINES-GATE-01 —— 同腳本既有修改先例（僅作對照）
- `coordination_dependency`: 無

## 來源（Parent）
- Parent type：Plan-backed；路徑 `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261024-autonomy-closeloop-plan.md`（Revision R2；Fixture false／Plan Gate PASS／Ticket mode eligibility ALLOWED）；plan-local item `T04`；讀取時間 2026-10-24T05:30Z。
- Selected item：[0.2] 採 `EXCLUDED_PAYLOAD`（diag C6）。

## 證據（開票時 live，2026-10-24 @2d8e7e34）
- [0.1]（`verify_checklist.sh` 約 :431–458）：先比對 ALLOWED，再比對 `EXCLUDED_PAYLOAD`（`token_matches_file "$excl" "$f"` 命中 → `📋 Excluded Payload: $BASENAME (authorized)` 並 `continue`），其後才分類 STALE／FUTURE／SCOPE_VIOLATION。**[0.1] 無「本票自身提交不得排除」之防線**。
- [0.2]（約 :505 起）：`token_matches_file "$fb_trimmed" "$_changed"` 命中 FORBIDDEN 即取該檔最近提交，僅 `is_stale_commit` 降級為 `⚠️ [stale]`，否則 `⛔ FORBIDDEN FILE` 並 `FAIL=1`；**無 EXCLUDED 分支**。
- 無既有 `verify_checklist.sh` 專屬測試；同目錄 `.claude/skills/qa/tests/test_report_fix_closure.sh` 採 `mktemp -d` 沙盒＋複製腳本之慣例。
- plan D6（自我豁免防線）狀態 `RECOMMENDED_ONLY`：因 [0.1] 無對應防線，且本票前綴判定可被 `[COMPACT-SYNC]` 等治理前綴繞過，本票**完全比照 [0.1]**，不新增 D6 語義，將自我豁免風險列為已知限制。
- 反例檢查：被排除之 FORBIDDEN 檔若實為本票自身越權修改，比照後將不 FAIL（與 [0.1] 對 ALLOWED 外檔之既有行為一致）；`EXCLUDED_PAYLOAD` 依 `sv/SKILL.md` 階段 D 第 9 項須「已授權」且於 sv_report `## Files Changed` 列出、由 `structural_verify.sh` Check C 檢核，自我豁免之控制點在授權與報告層而非 [0.2]。此為已知限制，於票面與收尾實錄揭露。

- 票號歸屬：本票由「工程師自主閉環」議題（源自 ADV-CLAUDECLI-01 收束與 Advisor 成果表 P1 缺口）發起，依 Akira 2026-10-24 裁決，同一議題 session 發起之票一律掛該議題前綴 `ADV`，不依修改落點另分前綴；原開票 ID 已以 `registry_rename_ticket` 改名。

## Verify Config
TICKET_ID=ADV-FORBIDEXCL-01
CREATED=2026-10-24
PRESET=bugfix
ALLOWED=.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/qa/tests/test_verify_forbidden_excluded.sh
FORBIDDEN=.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/qa/scripts/lib_verify.sh,.claude/skills/qa/SKILL.md,.claude/skills/sv/**,.claude/skills/ticket-author/**,.claude/skills/compact/**,.claude/scripts/**,.claude/hooks/**,.claude/rules/**,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,CLAUDE.md,app/**
MAX_LINES=30
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-24 14:10
BASE_COMMIT=2d8e7e34
TARGET_BLOB=N/A（開票端未取 blob；AI-0 逐檔現值錨點替代）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

MAX_LINES 推導（估算參考）：[0.2] 新增排除分支 ~12 行 ×1.5 → 20，進位 30（test 不計）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **動作**: 確認 [0.1] 之 `# Check: EXCLUDED_PAYLOAD` 區塊與 `📋 Excluded Payload: $BASENAME (authorized)` 字面各 1 次；[0.2] 段 `⛔ FORBIDDEN FILE: $_FORBIDDEN_FILE 被修改` 恰 1 次且其上游無 EXCLUDED 比對；`token_matches_file` 為 [0.1]／[0.2] 共用比對函式。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置。**
- [x] 完成（2026-10-24：[0.1] 之 `# Check: EXCLUDED_PAYLOAD` 與 `📋 Excluded Payload: $BASENAME (authorized)` 各 1 次；[0.2] 之 `⛔ FORBIDDEN FILE: $_FORBIDDEN_FILE 被修改` 恰 1 次且上游原無 EXCLUDED 比對；`token_matches_file` 為 [0.1]／[0.2] 共用）

### AI-1: [0.2] 加入 EXCLUDED_PAYLOAD 分支
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **現值**: FORBIDDEN 命中 → stale 降級或 `⛔ FORBIDDEN FILE` + `FAIL=1`。
- **目標值**: FORBIDDEN 命中後、stale 判定前，以與 [0.1] 相同之 `token_matches_file` 迴圈比對 `EXCLUDED_PAYLOAD`；命中 → 輸出 `📋 Excluded Payload (FORBIDDEN, authorized): <檔>`，不設 `FAIL`、不設 `FORBIDDEN_FAIL`；未命中 → 原有 stale／FAIL 行為不變。`EXCLUDED_PAYLOAD` 為空時行為與現值逐位元組等價。
- [x] 完成（[0.2] 迴圈內新增 EXCLUDED_PAYLOAD 比對〔同一 `token_matches_file`〕；命中者輸出 `📋 Excluded Payload (FORBIDDEN, authorized): <basename>` 並 `continue`，不設 `_FORBIDDEN_FILE`；EXCLUDED_PAYLOAD 為空時不影響原行為；同一 FORBIDDEN token 命中多檔時，已排除者逐檔略過、首個未排除者仍判 FAIL）

### AI-2: 最小沙盒測試
- **檔案**: `.claude/skills/qa/tests/test_verify_forbidden_excluded.sh`（新建）
- **目標值**: 依 `test_report_fix_closure.sh` 沙盒慣例（`mktemp -d`、真實 `git init`、複製腳本與其依賴），以 conf 模式真實執行 `verify_checklist.sh`，覆蓋 AC 2 之狀態矩陣並斷言 exit code 與輸出字面。
- [x] 完成（新建 `.claude/skills/qa/tests/test_verify_forbidden_excluded.sh`：mktemp 真實 git 沙盒＋conf 模式真實執行腳本；修改前基線 PASS=8／FAIL=7〔(b)(e)(f) 重現缺陷〕，修改後 PASS=15／FAIL=0）

## Hard Constraints
1. 不改 [0.1]、[0.3] 及其後各段；不改 conf 格式與 `token_matches_file`。
2. 不新增「本票前綴」等新判準（D6 未核准）；自我豁免列已知限制。
3. 不改 FUTURE_CONTAMINATION 判定（`concurrency.md` 待開票清單第 5 項其餘部分不屬本票）。

## Error Handling
- 沙盒內無法以 conf 模式執行腳本（依賴缺漏且無法以複製解決）→ 停止回報，不改腳本依賴結構。
- [0.1] 排除比對實際依賴 basename 而非完整路徑，致 [0.2] 比照後語義不同 → 停止回報。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** [0.2] 段含 EXCLUDED_PAYLOAD 比對，且使用與 [0.1] 相同之 `token_matches_file`。（`verify_checklist.sh:517`–`:531`：[0.2] 迴圈內以與 [0.1] 相同之 `token_matches_file` 比對 `EXCLUDED_PAYLOAD`；提交 `c76bf9c8`）
- [x] **[行為性]** given 沙盒 repo（真實 git 與 conf 模式腳本執行）：(a) FORBIDDEN 檔被修改且未列 `EXCLUDED_PAYLOAD` → expect `⛔ FORBIDDEN FILE`、整體 exit 非 0；(b) 同檔列於 `EXCLUDED_PAYLOAD` → expect `📋 Excluded Payload (FORBIDDEN, authorized)`、[0.2] 不判 FAIL（無其他違規時整體 exit 0）；(c) 未設 `EXCLUDED_PAYLOAD` 且無 FORBIDDEN 命中 → expect 輸出與修改前一致（`✅ 無禁區檔案被修改`）；(d) [0.1] 同時列為排除者仍為 `📋 Excluded Payload: ... (authorized)`（[0.1] 行為不變）。（`test_verify_forbidden_excluded.sh`：(a) 未排除→`⛔ FORBIDDEN FILE`＋exit≠0；(b) 已排除→`📋 Excluded Payload (FORBIDDEN, authorized)`、不判 FAIL、[0.1] 字面不變、exit 0；(c) 無命中輸出不變；(d) EXCLUDED 為空→原行為；(e) 多檔命中首檔排除次檔仍 FAIL；(f) 路徑形式 token。修改前基線 PASS=8／FAIL=7，修改後 PASS=15／FAIL=0）
- [ ] **[行為性]** 既有 `.claude/skills/qa/tests/` 下測試（`test_report_fix_closure.sh`、`test_preflight_adapter.sh`、`test_preflight_core.py`）仍通過。（**未勾，如實揭露**：`test_report_fix_closure.sh` ALL PASS、`test_preflight_core.py` 14 OK；`test_preflight_adapter.sh` 修改前後皆 39 pass／80 fail〔既有 Windows 環境因素，如 R7-1 python3 PATH 過濾〕，故無法稱「全數通過」，僅能證「相對基線無新增失敗」）
- [x] ~~assembleDebug PASS~~ (N/A: 無 app/ 變更)（票面劃記 N/A，無 app/ 變更；本環境 Gradle 回 loopback IOException，**未取得 assembleDebug 成功證據**，不宣稱 Gradle 通過）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（實測 2 檔：`verify_checklist.sh`、`test_verify_forbidden_excluded.sh`，皆 ALLOWED；`git diff --check` 無輸出）

## 收尾實錄（Agent closeout，2026-10-24；交 TKA close 判定，非自行終審）
- **實作提交**：`c76bf9c8`（`verify_checklist.sh` +15、新測試檔 +88）。
- **QA**：Step 0 PASS（已提交範圍）；Gradle 兩度（含本票 /qa）因 `java.io.IOException: Unable to establish loopback connection` 未能執行，非票面失敗亦非 PASS，`reviews.qa=N_A`；未依 Gate 記 `step_limit`（無對應 incident）。
- **SV／Cross-Review**：R0 FORWARD（非正式 PASS）。Codex `gpt-5.6-sol` **三次 REPORT_FIX → `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2），無終局 verdict**，`reviews.codex=N_A`（非 PASS）。verdict 史：①第 1 次為我之流程失誤——`.tmp/sv_report.md` 寫入被拒仍為前票舊報告，`structural_preflight` 判 REPORT_FIX（已揭露於 SV 報告、不清 state）；②第 2 次要求 `EXCLUDED_PAYLOAD` producer 之 `檔案:行號`→現場取證後誠實改標 PARTIAL（無自動 producer，`activate_ticket.sh:161-170` 九欄不含此鍵）；③第 3 次 Codex 改認 CLI 旗標 `verify_checklist.sh:298` 為有效 production writer（Producer B、`REACHABILITY: NONE`＝無缺口），僅指摘我將分類寫為 PARTIAL——LIMIT 後不再重跑。本票結論取較保守之誠實揭露：input 由人工追加 conf 行或 CLI 旗標提供，無自動接線；此限制與既有 [0.1] 消費者相同、非本票新增。Gate INC-001：executor 1 輪 fail、`step_limit` 已記。
- **接續**：自動接線缺口已另開 `ADV-EXCLCONF-01`（`activate_ticket.sh` 轉錄票面 `EXCLUDED_PAYLOAD=`；同步 `ticket-run/SKILL.md` 欄位集）。
- **已知限制**：自我豁免（票面 D6，[0.1] 本身無防線，[0.2] 比照；控制點在授權與報告層）；`test_preflight_adapter.sh` 既有 80 項環境失敗未修；Gradle 未驗。
- **PROGRESS.md Dashboard 同步**：FORBIDDEN 延後項，待 `/compact`。本票 close 之保存由 `persist` 執行。


<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: 交付：verify_checklist.sh [0.2] 對 EXCLUDED_PAYLOAD 具名檔比照 [0.1]（已授權排除不判 FORBIDDEN；多檔命中逐檔判定）＋新沙盒測試 test_verify_forbidden_excluded.sh（修改前 8/7→修改後 15/0；提交 c76bf9c8）。 / Cross-Review：Codex REPORT_FIX×3 觸 CHECKPOINT_REPORT_FIX_LIMIT 無終局 verdict（reviews.codex=N_A，非 PASS；第 3 次 Codex 認 CLI 旗標為 Producer B、REACHABILITY NONE）；QA Gradle 環境不可用（qa=N_A）；TKA close reviewer gpt-6-sol 5 條 AC：核心 2 條 PASS，AC3 非核心 FAIL（既有 test_preflight_adapter.sh 基線 39/80 環境失敗，僅證無新增失敗）、AC4 N/A。 / 限制：EXCLUDED_PAYLOAD 無自動 producer（人工追加 conf 或 CLI），已另開 ADV-EXCLCONF-01；自我豁免已知限制（D6 未實作）；第 1 次 REPORT_FIX 為我誤用前票舊報告所致（已揭露）。
- 最終驗收結論: 核心 AC 2 條皆為 PASS 或有效 N/A；非核心 PASS 1／N/A 1／UNVERIFIED 0／FAIL 1（共 5 條 AC）
- 非核心 FAIL 1 條: AC3 `test_preflight_adapter.sh` 仍有 80 項失敗；前後
- 實作提交: c76bf9c8
- 實際收束結果: 2026-10-24 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: codex/gpt-6-sol（tier=heavy）；覆蓋 AC 5／5（全部），independent_call=True
- 披露: reviewer limitation: reviewer 無法讀檔，diff、測試原件（含行號與整檔 sha256）與驗證輸出逐字嵌入 prompt；回覆檔取自 codex exec 最終訊息段（原始輸出 reviewer_reply_raw.txt）；Cross-Review 
<!-- close-result:end -->
