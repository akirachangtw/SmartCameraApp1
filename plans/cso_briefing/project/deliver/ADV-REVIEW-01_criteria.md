# ADV-REVIEW-01: 收斂 Cross-Review 審查判準——BLOCKER 判準集中為單一表、輕微措辭／無品質影響連結／含糊完備性不再阻斷、決策相關錯誤 PASS 仍 REPORT_FIX、Reachability 依實際輸入且不固定目錄、風險關鍵詞降為取證提示

**Revision**: Rev A.1（2026-10-14；依 Akira 審查裁決就地局部修訂票面；不重跑 diag／plan、不新增 `### AI-` 標題（仍 AI-0～AI-8）、不擴大到 T01b、未改 lifecycle）。變更：①`External-review decision` 改分項記錄（新能力／依賴＝NONE／NONE 不成立；6b②成立→AI-8 bounded probe，狀態 PLANNED；無承重外部事實題→不送 PPSS；不豁免 Cross-Review），AI-8 收尾須把 probe 欄改記實際結果；②R0 預授權採甲（僅限本票）：N2、HC12、Error Handling、AI-8 第 4 項、對應 AC 同批替換；③AI-3 B-7 刪除「改標 PARTIAL 即放行」，改為補有效舉證解除缺證據 finding、PARTIAL 僅更正報告；同步 AI-2 目標值第 4 項與 F9 必存在字串（HC3 經核對語意相容，未改）；④AI-1 finding 格式分 REPORT_FIX／BLOCKER 兩式；⑤R2 阻斷以歷史報告確有承重缺證據為前提（AI-8 第 10 項）；其餘範圍、預算、隔離重放與效果門檻不變。

**Revision (Closeout)**: 2026-10-14 Akira 收束裁決：接受 N=1 有限證據與效果驗收未達門檻，**限定交付，移交 `REVIEW`**；不追加實作、研究、重放或 Codex 呼叫；效果驗收 AC 維持未勾（**UNVERIFIED，非 PASS**）；Codex 真實狀態為 **ADVISORY（REPORT_FIX 後回驗）**，不記為 Codex PASS；`CLOSED` 留終審角色。

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T01a`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261013-reportfix-slim-plan.md`（Revision R3.1，`.tmp` 非長期載體，下列內容使本票獨立成立）；Parent artifact status＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`／`Pending decisions: none`；Parent 讀取時間＝2026-10-14（本 session 全文讀取，早於 12:11 錨點探測）。`.tmp/ticket-author/20261013-reportfix-t01b-deferred.md`（T01b 延後記錄，header 為 `Plan Gate: NOT PASSED`／`FORBIDDEN`）**不是** parent，本票不承接其機制或授權。

**Selected item 原文（T01a goal，摘要不改義）**：Cross-Review 的審查判準單一來源且逐項有品質目的與證據門檻——輕微措辭、無品質影響連結、含糊完備性不再阻斷；真實契約／API／接線／行為違反仍阻斷；必要報告主張仍須如實（降級不得掩蓋錯誤 PASS）。範圍＝parent §3 審查瘦身（BLOCKER 判準集中一處並逐項保留品質目的與證據、REPORT_FIX 定義收窄、檔案清單不由模型重審、Reachability 依實際風險、B-4／B-8／L102 同步）＋§4 風險關鍵詞處置＋§6 驗收（payload 靜態斷言＋隔離重放）。

**相關約束（取自 parent）**：①**T01a 不做**：改 REPORT_FIX 上限；改 state schema、計數或 LIMIT；引入 `FINDING`／`PRIOR`／disposition preflight；改 token、額外回驗或故障恢復；改 `gate_ledger.py`／`gate_stop_guard.py`；改 `structural_verify.sh`／`lib_verify.sh`／`verify_checklist.sh`（含 Check G）、`ssot-governance.md`、階段 A.5 的 ELF 檢查（保留 `ALIGNED` 判讀原文）；改 BLOCKER 輪預算；新增 BLOCKER 列或擴大全局審查；V2；重開 ADV-GATE-04；把任何上限調整稱為與 Gate 三輪對齊。②BLOCKER 表只修現有表（現有白名單 6 項＋L1.2、L2.3、3.1、3.2），不新增列。③Reachability：不豁免 runtime hook／helper、不固定 Android 目錄、不以 AC 名稱或標記代替實際可達性風險；只限實際使用且影響判定的輸入，一跳為止。④`Guard Input Reachability:` 散文行與 `REACHABILITY:` token 的格式契約逐字不變。⑤驗收：stub 驗收不得寫成真實 Codex 閉環 PASS；隔離重放為 N=1 有限證據，不宣稱一般可靠，不追溯修改歷史品質結果。

**已裁決補充（Akira 2026-10-14 開票指令；與 parent 衝突時以本節為準）**：
- **N1（範圍）**：`cross-reviewer.md` 與 `ssot_context.md` 納入範圍，**僅限 parent §2 指定段落**（`cross-reviewer.md` 職責 1 的 token 部分＋條件豁免第 2 要件；`ssot_context.md` L66）。最終 ALLOWED 由本票取證核定（見 Verify Config）；ALLOWED 不代表本輪取得實作授權。
- **N2（R0，Rev A.1 採甲，預授權僅限本票）**：本票後續 R0 若**僅因正在修訂的舊 token 規則**攔截——命中 hunk 只是規則／prompt／文件文字、無 feature flag 預設值或 DI 綁定變更、無其他獨立 finding 或未授權變更——即記 `TRUE-POSITIVE-RULE-GAP`（承接票號＝`ADV-REVIEW-01`），**記錄後續送 Codex，不再停等同一情形的 Akira 裁決**；**不使用**誤攔 override（`_OVERRIDE`）、不耗誤攔預算、不擴改 R0 機制、不適用其他票；實際未授權變更或其他獨立 finding 不豁免。
- **N3（自身 /sv）**：本票自身 `/sv` 的真實 Cross-Review 僅為有限整合證據，**不作瘦身效果證據**。
- **N4（重放）**：歷史重放每份報告一次，真實 Codex（含失敗）至多三次，不補呼叫。**R1 仍阻斷，或 R2 被降級／未檢查，不將效果驗收標 PASS**；如實記錄並回報，不自動追加研究或呼叫。（此條將 parent §6.2「無預設通過門檻」收斂為效果驗收門檻，見下差異 X1。）
- **N5（邊界）**：不變更 state、計數、LIMIT、token 格式、Check G、gate helper／hook、`ssot-governance.md`、ELF `ALIGNED` 判讀；新增 payload 靜態斷言；預期舊測試斷言無需變更，若發現需改則逐項揭露，不為全綠修改政策。

**開票端與 parent 的差異（精準列出，未改單票目標與必要範圍）**：
- **X1（效果驗收門檻）**：parent §5／§6.2 寫「重放無預設通過門檻」；依 N4，本票將「效果驗收」另列 AC：僅當 R1＝明列非阻斷 **且** R2＝仍阻斷時方可標 PASS，否則保持未勾並如實回報；「重放協定已執行並如實記錄」另列一條 AC，與效果脫鉤。R3 結果僅記錄。
- **X2（EOL）**：`cross_review.sh` 工作樹為**全檔 CRLF**（516／516 行含 `\r`，`git ls-files --eol`＝`i/lf w/crlf attr/text eol=lf`，`core.autocrlf=true`）；其餘六個 ALLOWED 檔為 LF。編輯 MUST 保持各檔現行行尾，`git diff` 不得出現整檔行尾改寫；比對與重放一律取 **git blob**（`git show <rev>:<path>`），不取 CRLF 工作樹檔。
- **X3（非 PROMPT_HEADER 不變的驗證法已定）**：以 heredoc 去除法比對 blob（見 AI-0／AC-2）；基線 sha256＝`d71d1957…b94a0`（去 heredoc 後 428 行）。
- **X4（歷史輸入已定位並預驗）**：三份歷史 SV report 取自 `.tmp/cross_review_out_ADV-GATE-04{,_r2,_r3}.txt` 的 `## SV Report` 段（行 336–384，各 46 行，LF 正規化後 sha256 前 16 碼＝R1 `ef7a6e24f99772fa`／R2 `15da7e4481563191`／R3 `d0c703655225e844`）；重新產生的 Git Diff 段（`git diff 8a7c18e9..d724475f -- ':!.tmp/sv_report.md' ':!docs/' ':!plans/'`，1055 行，sha256 前 16 碼 `acd26d842fdec177`）與三份回聲逐位元相同（CRLF 正規化後）。
- **X5（歷史審查器版本）**：`d724475f` 與 `d21e8f72` 的 `cross_review.sh` blob 同為 `0331a328`、`ssot_context.md` 同為 `6092b217`，故歷史審查器版本＝此二 blob；T01a 審查器版本於收尾時另記。
- **X6（untracked 鏡像）**：`.agents/skills/sv/{ssot_context.md,SKILL.md,CROSS_REVIEW.md,scripts/}` 為 `.gitignore` 排除的本機鏡像（0 個 tracked 檔），其中僅 `ssot_context.md` 與 `.claude` 版相同，其餘已過期；**不同步、不納入**。
- **X7（Registry 查核，開票前歷史證據）**：全冊機械取號（registry＋ARCHIVE＋deliver／Pending／void＋`git log --all --grep`）無 `ADV-REVIEW-*`；`Advisor` 議題下 11 筆（ADV-GATE-01…04／REPORT-FIX-01／RULE4C-01 為 `CLOSED`，ADV-CR-R0-PRECLEAR-01 系列與 ADV-AGENTS-CR-03／05 為 `VOID`）無等價「審查判準瘦身」票；actionable 7 張（KAIROS3／4 彙整、GAP-06／09、STATE-CONTRACT-02／06、TRACTION-PROTO-02）皆不觸本票標的；`Pending/` 內 `MEO-FIX-A_*`／`MEO-FIX-B_*` 為冊面查無之孤兒檔，不構成占用。

**證據分級**：①**CONFIRMED（2026-10-14 12:11 live 直讀）**：HEAD `d21e8f72`、工作樹 clean；七個 ALLOWED 檔之錨點命中數如 AI-0；`git check-ignore -v` 對七檔零命中；`SSOT_v4_0_7.md` 無 `CompositionScore`／`EyeAfPolicyOutput`／`SceneType`／`零容忍` 條文（該零容忍句僅存於 `ssot_context.md` L66，故收窄不構成 SSOT_CONFLICT）；`codex-cli 0.162.1` 可用且與歷史回件同版；`.git/hooks` 僅 `commit-msg`／`pre-commit`（無 `post-checkout`；`pre-commit` 的 Scope Drift 僅擋 `.kt/.java/.xml` 與文件同提交，本票無此類檔）；`verify_checklist.sh` 的含 `/` token 以 glob 比對（`INFRA-VERIFY-GLOB-01`）；`ticket_pattern.md` 已登記 `ADV-` family；現役測試於 2026-10-13 本 session 實跑全綠（`test_report_fix_closure.sh` ALL PASS、`gate_stop_guard_test.py` 42 OK、`test_gate_ledger.py` 39 OK；HEAD 當時與現在同為 `d21e8f72`，10-14 未重跑，AI-8 重跑）。②**HISTORICAL**：ADV-GATE-04 三輪 REPORT_FIX（R1「7 hunk」應為 5 hunks、R2 Registry producer 宣稱缺 `檔案:行號`、R3 `stop_hook_active`／`last_assistant_message` 被描述為 hook 判定輸入而實際未讀）取自 `.tmp/cross_review_out_ADV-GATE-04*.txt` 的 codex 輸出。③**HYPOTHESIS**：新 prompt 是否使 Codex 對 R1 明列非阻斷、對 R2 仍阻斷（由 AI-8 重放檢驗，本票不預判）；R0 subagent 於本 session 載入的是舊定義或新定義（AI-8 實跑時以實際 R0 回報為準）。④**UNKNOWN 已處置**：暫存 worktree 內 `structural_verify` 預檢是否因缺 gitignore 檔案被擋——AI-8 步驟以 probe＋Error Handling 處置（補環境重跑，不計 Codex 次數）。

**External-review decision**（分項記錄，不以整節 WAIVED 掩蓋已成立的 probe 條件）：
- **新能力／依賴觸發**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` v3.4 §6，L43–L50）：6a（`INTRODUCED_CAPABILITY≠NONE`）與 6b③（`DEPENDENCY_DELTA≠NONE`）——本票二欄皆為 `NONE`，**不成立**；6b①（`ALLOWED` 首層 path segment distinct ≥2）——本票七檔首層皆為 `.claude`，distinct＝1，**不成立**；6c（非票面受管制產物近 90 日 `Revision` 歷史反覆修訂 ≥3 次）——六個受審檔頭皆無 `Revision` 欄（grep 0 命中），無歷史者不納本款，**不成立**。
- **6b②（依本機現役條文確認成立）**：`### AI-[1-9][0-9]*:` 標題數＝AI-1…AI-8＝8（AI-0 不計）≥ 8。條文後果為取證或 bounded probe（非凍結）；本票改變審查判準與阻斷語意，其效果以 bounded probe 檢驗，**承接＝AI-8 隔離重放**（真實 Codex 含失敗至多 3 次）。**probe 狀態：EXECUTED（2026-10-14 13:58–14:07；真實 Codex 3 次、0 失敗；R1 報告↔hunk 敘述＝未被檢查、R2 報告↔Registry producer 舉證＝仍阻斷、R3 報告↔`stop_hook_active`／`last_assistant_message` 描述＝未被檢查；N=1，非 PASS 判定，效果驗收裁決中 Akira，逐項見收尾實錄）**。AI-8 收尾時 MUST 把本欄由 PLANNED 改記實際結果（`EXECUTED`＋R1／R2／R3 逐項；或 `NOT_EXECUTED`＋原因），**不得留 PLANNED**。
- **外部研究**：無承重外部事實題（依據皆為本機原始碼、歷史回件、live 命令輸出），**不送 PPSS**。
- **Cross-Review**：**不豁免**；本欄任何狀態都不代表品質驗收 PASS。

**簽發日期**: 2026-10-14
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ADV-GATE-04（`CLOSED`，歷史三份 SV report／回件／範圍 `8a7c18e9..d724475f`／ALLOWED 原值作重放輸入）、ADV-REPORTFIX-01（`CLOSED`，現役 `cross_review.sh`／SV 閉環文字為本票基線，本票不改其機制）、INFRA-CR-AGENT-APP-01（`CLOSED`，`cross-reviewer.md` token 清單來源，本票僅改其觸發語意）——僅作字面基線與歷史證據，本票錨點皆 live 直查
- `coordination_dependency`: 無（本票與任何編輯 `cross_review.sh`／`sv/SKILL.md`／`sv/CROSS_REVIEW.md`／`rules/cross-review.md`／`cross-reviewer.md`／`ssot_context.md` 之票須串行，`concurrency.md` §2；T01b 為延後候選，非依賴）

## Verify Config
TICKET_ID=ADV-REVIEW-01
CREATED=2026-10-14
PRESET=docs
ALLOWED=.claude/skills/sv/scripts/cross_review.sh,.claude/skills/sv/SKILL.md,.claude/skills/sv/CROSS_REVIEW.md,.claude/rules/cross-review.md,.claude/agents/cross-reviewer.md,.claude/skills/sv/ssot_context.md,.claude/skills/qa/tests/test_report_fix_closure.sh
FORBIDDEN=.claude/skills/qa/scripts/**,.claude/skills/qa/SKILL.md,.claude/skills/qa/tests/test_preflight_adapter.sh,.claude/skills/qa/tests/test_preflight_core.py,.claude/skills/uc/**,.claude/skills/compact/**,.claude/skills/dream/**,.claude/skills/mdlint/**,.claude/skills/normal/**,.claude/skills/status/**,.claude/skills/ticket-author/**,.claude/skills/ticket-rigor-gate/**,.claude/skills/ticket-run/**,.claude/skills/wktree-close/**,.claude/skills/wktree-compact/**,.claude/skills/android-ui-design/**,.claude/scripts/**,.claude/hooks/**,.claude/settings.json,.claude/settings.local.json,.claude/CLAUDE.md,CLAUDE.md,CLAUDE.local.md,.claude/verify-policy.env,.claude/rules/android-conventions.md,.claude/rules/camera-constraints.md,.claude/rules/compose-ui.md,.claude/rules/concurrency.md,.claude/rules/critical-files.md,.claude/rules/decommission.md,.claude/rules/engineering-discipline.md,.claude/rules/kairos-lessons.md,.claude/rules/mcp-service-safety.md,.claude/rules/phase0-lessons.md,.claude/rules/ptf-rules.md,.claude/rules/ssot-governance.md,.claude/rules/state-machine.md,.claude/rules/ticket_pattern.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,.agents/**,tools/**,app/**,plans/architecture/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json
MAX_LINES=N/A
DIFF_REF=d21e8f72
ANCHOR_PROBED=2026-10-14 12:11
BASE_COMMIT=d21e8f72
TARGET_BLOB=.claude/skills/sv/scripts/cross_review.sh:0331a328;.claude/skills/sv/SKILL.md:9306e9c6;.claude/skills/sv/CROSS_REVIEW.md:89380136;.claude/rules/cross-review.md:3a7d2304;.claude/agents/cross-reviewer.md:cedb888b;.claude/skills/sv/ssot_context.md:6092b217;.claude/skills/qa/tests/test_report_fix_closure.sh:fb6fa198
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

> PRESET=docs（審查 prompt 字面、文件契約與測試斷言；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A；`cross_review.sh` 僅改 `PROMPT_HEADER` heredoc 字面，先例 `ADV-REPORTFIX-01` 同為 docs preset）。`MAX_LINES=N/A`：本票性質為審查判準（失敗形狀為審查發現式），MAX_LINES 非 execution gate（TPL v3.5）。`TARGET_BLOB`：`git rev-parse HEAD:<path>` 前 8 碼（七檔皆為既有檔，index 與工作樹 hash 相同）。`DIFF_REF`：依 `ticket_pattern.md`「票據模板規範」為字面佔位，開工時依 `qa/SKILL.md` Step 0 三原子動作回填。`PROGRESS.md`／`PROGRESS_ARCHIVE.md` 命中 FORBIDDEN：依「FORBIDDEN 延後項免開票紀律」，Dashboard 同步、ARCHIVE Done 標記、票檔 Pending→deliver 搬移與勾選屬標準 SOP 收尾，於下一輪 `/compact` 或後續 session 正常處理（`DEFERRED-TO-DOCSYNC`，不開票）。`FORBIDDEN` 不含 `.claude/agents/**`／`.claude/skills/sv/**`（其內 ALLOWED 檔以全路徑列出，其餘檔案無他物）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: 下列各檔（`ANCHOR_PROBED=2026-10-14 12:11`；基線漂移依模板三分流處置）
- **動作**: 逐字確認現值於檔內命中數與票面相符（`grep -cF`）；任一項不符 → 依三分流分類記錄，禁推定等價位置。
  1. 全域：HEAD `d21e8f72`、`git status --porcelain` 空；`git check-ignore -v` 對七個 ALLOWED 路徑零命中；`git ls-files --eol`：`cross_review.sh`＝`i/lf w/crlf attr/text eol=lf`（`\r` 行數＝總行數＝516），其餘六檔 `w/lf`（`\r` 行數 0）。
  2. `.claude/skills/sv/scripts/cross_review.sh`：`cat > "$PAYLOAD" << 'PROMPT_HEADER'`＝1（L249）；行首 `PROMPT_HEADER`＝1（L338）；`**BLOCKER 白名單 — 只有以下情況可判 BLOCKER，其他一律降級**：`＝1（L264）；`## Layer 1: Architectural Review（架構重審）`＝1；`### 3.5 Guard Input Reachability（INFRA-REACH-01 固定項）`＝1（L315）；`- sv_report.md 格式/完備性問題`＝1；`app/src/main/`＝1（L320）；`### 3.6 注意事項`＝1；`強制輸出格式（每次必須輸出此行）`＝1；`機械 token 格式（INFRA-REACH-02 AI-4`＝1。**去 heredoc 基線**：`git show HEAD:<path> | awk '/^cat > "\$PAYLOAD" << .PROMPT_HEADER.$/{s=1;print;next} s&&/^PROMPT_HEADER$/{s=0} !s{print}' | sha256sum`＝`d71d1957b6efa3e285539aba8d8abf07927b930191f0df6032f9f0314f1b94a0`（428 行）。
  3. `.claude/skills/sv/SKILL.md`：`**MUST** 搜尋 `// TODO`、`// FIXME`、`// HACK`、`return null`——零容忍。`＝1（L57）；`在 `app/src/main/` 是否存在 production 寫入點`＝1（L70）；`機械分級（INFRA-REACH-02）`＝1（L72）；`app/src/test`＝1（L79）；`` `## Files Changed (payload)` — **MUST** 列出 `git diff --name-only DIFF_REF..HEAD` 的**全部**檔案（完整 payload）``＝1（L102）；`3.5. **16KB ELF 對齊檢查**：`＝1（A.5，**不改**）。
  4. `.claude/skills/sv/CROSS_REVIEW.md`：`` `app/src/main/` 有 non-idle / non-reset 賦值點``＝1；`` | `REPORT_FIX` | 3 | `.tmp/sv_report.md` 或 payload disclosure 不可信 |``＝1；`- **Layer 2 — SV Report Cross-Review（報告驗證）**`＝1；`**強制輸出格式**（每次 Cross-Review 必須輸出此行）：`＝1。
  5. `.claude/rules/cross-review.md`：`- 修正含 token：FeatureFlags / gate / Gate / cooldown / @Inject / @Module / @Provides`＝1（L14）；L13（`- 修正涉及：PROGRESS.md / sv_report*.md / ssot_context.md / …`）存在，**不改**（記 `sed -n 13p | sha256sum` 基線）。
  6. `.claude/agents/cross-reviewer.md`：職責 1 字串 ``diff 含 token `FeatureFlags` / `gate(` / `Gate(` / `isGated` / `cooldown` / `@Inject` / `@Module` / `@Provides` → 標記 `CHECKPOINT-REQUIRED`，不放行。``＝1；`2. 該檔 diff hunks **零**上述敏感 token`＝1。
  7. `.claude/skills/sv/ssot_context.md`：`**零容忍**: 若 diff 中出現 `CompositionScore`, `EyeAfPolicyOutput`, `SceneType` 等非 SSOT 名稱 → FAIL`＝1（L66）；`app/src/main`＝0。
  8. `.claude/skills/qa/tests/test_report_fix_closure.sh`：`run_cases() {`＝1（L85）；`  # F8：BLOCKER,REPORT_FIX,BLOCKER,BLOCKER`＝1；`check "F8 exits" 1/3/1/2 "$(join "${RCS[@]}")"`＝1；`run_cases` 結尾 `}`＝L166；`setup_scratch` 不複製 `ssot_context.md`。
  9. 歷史輸入（唯讀）：三個 `.tmp/cross_review_out_ADV-GATE-04{,_r2,_r3}.txt` 存在，各 `^## SV Report$`＝1（L336）、`^## Git Diff (scope: 8a7c18e9..d724475f)`＝1（L385）；SV Report 段（L338–L383，各 46 行）與 Git Diff 段（1055 行）的 LF 正規化 sha256 前 16 碼如 X4。
  10. 隔離基線（供 AI-8 前後比對）：主樹 `.tmp/cross_review_state/ADV-GATE-04`＝62 bytes、sha256 前 16 碼 `0ce0036fa63d4dd4`、內容 `ROUND=0／REPORT_FIX_COUNT=2／LAST=REPORT_FIX`；`.git/gate/ADV-GATE-04.jsonl`＝5938 bytes、sha256 前 16 碼 `c44fc652508d6463`、mtime `2026-10-13 20:54:45`；`.tmp/cross_review_pass/` 無 `ADV-GATE-04` receipt；`git worktree list` 為 2 筆（主樹＋`~/.codex/worktrees/4ba7`）；Registry `ADV-GATE-04`＝`CLOSED`、reviews＝`{qa:PASS,sv:PASS,codex:N_A,traycer:N_A}`。
  11. `registry_list(project=SmartCameraApp1)`：本票 `ADV-REVIEW-01` 已登記、`file`＝`Pending/ADV-REVIEW-01_criteria.md`、`topic`＝`Advisor`、`lifecycle`＝`PENDING`；無第二筆同 ID 或等價票；`ADV-GATE-04`／`ADV-REPORTFIX-01`／`INFRA-CR-AGENT-APP-01` 為 `CLOSED`。
- [x] 完成（2026-10-14；證據見「收尾實錄（Agent，QA／SV 前）」）

### AI-1: `cross_review.sh`——BLOCKER 判準單一表、REPORT_FIX／ADVISORY 收窄、Layer 歸類提示
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`（**僅** `PROMPT_HEADER` heredoc 內；保持全檔 CRLF，不得混行尾）
- **現值**: 自 `**BLOCKER 白名單 — 只有以下情況可判 BLOCKER，其他一律降級**：`（L264）起，至 `### 3.4 治理證據鏈` 段末行 `- PROGRESS.md / CLAUDE.md 連結指向已刪除檔案 → REPORT_FIX（非 BLOCKER）`（L313）止，整段（含白名單 6 項、REPORT_FIX／ADVISORY 情境、Layer 1／2／3 與 3.1–3.4）。L250–L262（角色、審查紀律、裁決格式）、L315 起的 3.5 標題與其後、`### 3.6` 皆**不在本項**。
- **目標值**: 以下文字（逐字，保留 Markdown 表格）取代上述整段；表格列順序與編號固定為 1–10。**寫入時去除本票面列表縮排**：內容自行首開始，與現有 `PROMPT_HEADER` 各行一致（否則 `^## BLOCKER 判準（唯一來源` 與列號錨點會失配）；行尾保持 CRLF。
  ```markdown
  ## BLOCKER 判準（唯一來源；下列各 Layer 僅作歸類提示，不另立阻斷規則）

  只有同時符合「阻斷情形」與「適用條件」並提供「必備證據」者可判 BLOCKER；缺必備證據或不符適用條件者一律降級為 ADVISORY。

  | # | 阻斷情形 | 品質目的 | 適用條件 | 必備證據 |
  |---|----------|----------|----------|----------|
  | 1 | 確認存在的 runtime crash 或功能 regression（非假設性） | 防出貨崩潰或功能退化 | diff 含 `.kt`/`.xml` | diff 行號＋觸發路徑 |
  | 2 | Scope Ban 清單中的檔案被修改且無授權 | 防越權改動受保護檔 | ssot_context §3 | 檔名＋票面 ALLOWED 無授權 |
  | 3 | 佔位實作取代應有行為（`TODO()`、`error("Not implemented")`、placeholder return） | 防假實作進主線 | 本 diff 新增行 | diff 行號 |
  | 4 | Clean Architecture 分層違反（domain 依賴 presentation／data／android.*） | 守住依賴方向 | ssot_context §1 | 依賴／import 行 |
  | 5 | 管線約束違反（CameraX 重綁、ML Kit 互斥、Edge Warmth 60fps） | 防相機管線退化 | diff 觸及相關路徑 | diff 行號＋約束出處 |
  | 6 | 狀態機時序（Cooldown／Lockout／Timeout 時間戳寫入時機錯誤） | 防冷卻起算語意錯誤 | diff 含此類邏輯 | diff 行號 |
  | 7 | 新舊雙路徑或孤兒碼**造成行為或 API 影響**（接線斷裂、consumer 取值不確定） | 防 split-brain | 被消費者引用 | 兩路徑與 consumer 的 `檔案:行號` |
  | 8 | SSOT 明文契約違反：僅限 ssot_context §2 明列之三個名稱（`CompositionScore`、`EyeAfPolicyOutput`、`SceneType`），或實際破壞 SSOT 已定義型別／API／常數之使用者 | 防契約偽造或漂移 | 明列名稱／既有定義 | 名稱或定義出處＋使用處（consumer 證據） |
  | 9 | UI 物理規則（insets、48dp、LocalDensity） | 防遮擋、誤觸、細線消失 | 票面／SSOT／rules 對**該元件有明確要求** | 要求出處＋diff 行；不以關鍵詞自動套用 |
  | 10 | 全鏈路退場殘留（UI→ViewModel→Settings→DataStore 未同步清除） | 防退場後殘留層 | 票面**明示**為退場類 | grep 輸出 |

  ## REPORT_FIX 判準（修 sv_report.md 即可，不要求修程式碼，但會阻擋 Cross/Traycer pipeline）

  僅限：SV Report 中「決策相關」的主張與原始證據矛盾。決策相關＝影響 AC 的 PASS 狀態、範圍／payload 揭露、producer／consumer 證據、測試／驗證結果主張。

  - 降級不得掩蓋錯誤 PASS：報告標 ✅／PASS 的決策相關主張若與證據矛盾，維持 REPORT_FIX；ADVISORY 只適用於不影響 PASS 判定的項目。
  - finding 格式（兩式）：**REPORT_FIX** 須引用①報告主張原文；②原始證據（diff 行、log、`檔案:行號` 或指令輸出皆可，不限於 diff）；③修正判準（什麼條件下視為已解）。**BLOCKER** 須引用①違反的既定要求或程式行為（如 BLOCKER 表列號與其出處、或實際錯誤的程式行為）；②原始證據（diff 行號、`檔案:行號`、grep 輸出等）；③修正判準。BLOCKER 不要求另找一段報告原文。
  - `## Files Changed`、`## Excluded Payload` 的檔案清單由 structural_verify.sh Check A/B/C 確定性核對，不得就檔案清單提出任何 finding。
  - 不要求報告陳述 hunk 數或行數；報告出現此類敘述且不正確時，除非影響 PASS 判定，一律 ADVISORY。

  ## ADVISORY（記錄即可，exit 0）

  每項仍須在回件中列為一行，不得靜默略過：
  - 已豁免的 static scan warning（1.dp BorderStroke、既有 hardcoded string）
  - 潛在但未確認的 regression（無 diff 行號）；建議性改進（不影響功能正確性）；缺測試的 business logic 變更
  - 輕微措辭偏差、無品質影響的治理文件連結問題（PROGRESS.md 死連結等）、含糊的「完備性」意見（若報告據以宣稱 PASS 的證據連結失效，屬決策相關主張，依 REPORT_FIX 判準）
  - 不符 BLOCKER 表適用條件者：非明文清單的 SSOT 命名偏好、無 SSOT 矛盾的 hardcoded 值、無行為影響的孤兒碼、無明確要求的 UI 規則、非退場票的退場殘留

  ## Layer 1: Architectural Review（架構重審；歸類提示）

  根據 SSOT Reference 驗證，並依 BLOCKER 判準表歸類：
  1. diff 是否符合 Clean Architecture 分層（domain 不依賴任何人）→ 表 #4
  2. 型別名稱與 SSOT 核心型別清單不一致 → 僅 ssot_context §2 明列三名稱，或實際破壞 SSOT 已定義型別／API／常數之使用者，屬表 #8；其餘為 ADVISORY
  3. Scope Ban 清單檔案被修改且無授權 → 表 #2
  4. 硬體約束違反（ML Kit 互斥、CameraX 零重綁）→ 表 #5

  ## Layer 2: SV Report Cross-Review（報告驗證；歸類提示）

  1. SV Report ✅ 項目與 diff／原始證據不符：決策相關者 → REPORT_FIX（見 REPORT_FIX 判準）；其餘 → ADVISORY
  2. （檔案清單與 payload 揭露一致性由 structural_verify.sh Check A/B/C 確定性核對，不由模型重審，不得就此提出 finding）
  3. Orphan Code（split-brain）→ 造成行為或 API 影響者屬表 #7；其餘 ADVISORY
  4. Anti-Mock（TODO()、placeholder return）→ 表 #3
  5. 缺測試的 business logic 變更 → ADVISORY

  ## Layer 3: 實作級盲區掃描（歸類提示）

  ### 3.1 物理與系統邊界（僅本次 diff 新增的 .kt 代碼）
  - `statusBarsPadding()` / `navigationBarsPadding()` 缺失、極細線條缺 LocalDensity、Touch Target < 48dp → 僅當票面／SSOT／rules 對該元件有明確要求且 diff 行證實違反，屬表 #9；否則 ADVISORY
  - 既有代碼（diff 未改動）類似問題 → ADVISORY

  ### 3.2 全鏈路退場（僅本次 diff 含 UI 元件刪除/替換且票面明示為退場類時檢查）
  - UI → ViewModel → Settings → DataStore 未同步清除 → 表 #10（須附 grep 輸出）

  ### 3.3 狀態機時序（僅本次 diff 含 Cooldown/Lockout/Timeout 時檢查）
  - Timestamp 寫入時機錯誤 → 表 #6（必須引用 diff 行號）

  ### 3.4 治理證據鏈
  - PROGRESS.md / CLAUDE.md 連結指向已刪除檔案 → ADVISORY（無品質影響）；若報告據以宣稱 PASS 的證據連結失效，依 REPORT_FIX 判準
  ```
- [x] 完成（2026-10-14；證據見「收尾實錄（Agent，QA／SV 前）」）

### AI-2: `cross_review.sh`——§3.5 Reachability 依實際風險
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`（`PROMPT_HEADER` 內，`### 3.5 Guard Input Reachability（INFRA-REACH-01 固定項）` 標題下）
- **現值**: L317–L322：以 `以下問題為跨 Ticket 強制回答項（即使本次 diff 無相關新增，也必須明確輸出一行回答）：` 起，含編號 1–4（第 2 項含 `` `app/src/main/` ``），至 `4. 若 producer 為 (C) → 即使 unit test 全綠，對應 AC **不得標 PASS**，標記 `missing-wiring`` 止。
- **目標值**: 以下文字取代 L317–L322（**寫入時去除本票面列表縮排**，內容自行首開始；行尾保持 CRLF）；標題行（L315）、L324–L330（`**強制輸出格式（每次必須輸出此行）**：`起至 `REACHABILITY: <guard1>:<producer位置>,<guard2>:MISSING` 行止）與 `### 3.6` **逐字不變**。
  ```markdown
  以下問題為跨 Ticket 強制回答項（即使本次 diff 無相關新增，也必須明確輸出一行回答）。檢查依**實際風險**進行：不因檔案類型（含 runtime hook／helper／腳本）豁免，也不因 AC 文字是否出現某名稱或標記而決定是否檢查或升級：

  1. 本 Ticket diff 是否新增任何 guard / gate / early-return / reducer branch / fallback，且其觸發條件依賴 diff 外部的 CameraState 欄位或 runtime state（含 runtime hook／helper 所讀取的檔案、registry、ledger、payload 欄位等外部輸入）？
  2. 若有，只檢查該判定**實際讀取、且影響放行或攔截結果**的輸入：每個輸入舉證一個 production writer（`檔案:行號`，可位於任何 repo 或 runtime 路徑；非預設初始化、非 reset-to-Idle 語意）與其判定處 consumer。不必列出完整 schema 或判定未使用的欄位，也不必向 writer 的上游無限追溯（一跳為止）。
  3. producer 判定三選一：(A) diff 內可見；(B) diff 外但 production 可達（附 `檔案:行號`）；(C) 不存在或不可證明 → 標記 `missing-wiring` 風險
  4. 若 producer 為 (C) → 即使 unit test 全綠，對應 AC **不得標 PASS**，標記 `missing-wiring`；舉證由 SV Report 以指令輸出提供，報告宣稱可達卻未舉證者屬決策相關主張缺證據（REPORT_FIX）；補有效舉證可解除該 finding，僅改標 PARTIAL 只更正報告、不自動滿足原 AC 或放行整票。
  ```
- [x] 完成（2026-10-14；證據見「收尾實錄（Agent，QA／SV 前）」）

### AI-3: `sv/SKILL.md`——B-4／B-7／B-8／L102
- **檔案**: `.claude/skills/sv/SKILL.md`（階段 A.5 的 ELF 檢查、其餘段落**不改**）
- **現值**:
  1. L57：`   - **MUST** 搜尋 `// TODO`、`// FIXME`、`// HACK`、`return null`——零容忍。`
  2. B-7（L68–L75）：自 `7. **Guard Input Reachability（INFRA-REACH-01 / INFRA-REACH-02 機械分級）**：` 起至 `   - 輔助工具：Step 3.5.5 Dead-State 掃描輸出；參考 K3-Rule 26（多消費路徑掃描）` 止。
  3. B-8（L77–L82）：自 `8. **Integration Test 存在閘（INFRA-REACH-02 G-5）**：` 起至 `   - 結果 **MUST** 寫入 `.tmp/sv_report.md`。` 止（該段中的 `app/src/test/`、`app/src/androidTest/`、AC 標記作唯一觸發）。
  4. L102：`   - `## Files Changed (payload)` — **MUST** 列出 `git diff --name-only DIFF_REF..HEAD` 的**全部**檔案（完整 payload）`
- **目標值**（各 markdown 區塊**寫入時去除本票面列表縮排**；LF 行尾）:
  1. L57 → `   - **MUST** 於**本 diff 新增行**搜尋 `// TODO`、`// FIXME`、`// HACK` 與佔位語意的 `return null`（如 `return null // implement later`）——零容忍；有明確契約語意的 `return null`（如 `rules/android-conventions.md` 明令的無效幾何尺寸）不在此列；既有行不屬本項，發現時可另報 ADVISORY。`
  2. B-7 → 以下文字整段取代：
     ```markdown
     7. **Guard Input Reachability（INFRA-REACH-01 / INFRA-REACH-02，依實際風險）**：
        對本 diff 新增的任何 Guard / gate / early-return（含 runtime hook／helper 的判定分支），只檢查該判定**實際讀取且影響結果**的 input（不列全 schema、不向 producer 上游無限追溯，一跳為止）：
        - 該 input 是否存在 production 寫入點（`檔案:行號`，可位於任何 repo 或 runtime 路徑；非預設初始化、非 reset-to-Idle 語意）？舉證以指令輸出（grep／read）為準。
        - 若任一被讀取的 input 無 non-idle production producer → 標記 **⚠️ UNREACHABLE GUARD**：對應 AC **MUST** 不得標 PASS，如實標為 **⚠️ PARTIAL**。是否檢查與是否升級依實際可達性風險，**不**以 AC 文字是否出現某欄位名稱或標記為準。
        - 報告宣稱「可達」而未附 producer 舉證 → 決策相關主張缺證據，Cross-Review 判 REPORT_FIX。補上有效舉證（`檔案:行號` 與指令輸出）可解除該缺證據 finding；改標 PARTIAL 僅更正報告主張，**不**自動滿足原 AC、**不**豁免必要品質、**不**放行整票，後續依原票與 Gate 處置（本項不自動強制另開 follow-up Ticket；`ssot-governance.md` Ticket Closure 紀律對「母票存在未閉環 AC」之修補票要求於收尾時獨立適用，不因本句免除）。豁免 **MUST** 僅得引用 Ticket 內**具名白名單**條款（比照 K4-39/K4-48 具名排除格式），**MUST** 禁止 context-gated 理由文（如「本情境下可接受」等臨場豁免）。
        - 結果（含「已確認可達」「UNREACHABLE-PARTIAL」判定）**MUST** 寫入 `.tmp/sv_report.md`
        - 輔助工具：Step 3.5.5 Dead-State 掃描輸出；參考 K3-Rule 26（多消費路徑掃描）
     ```
  3. B-8 → 以下文字整段取代：
     ```markdown
     8. **Integration Test 存在閘（INFRA-REACH-02 G-5）**：
        凡本 diff 新增依賴外部輸入的行為（Guard／gate／early-return、reducer 分支、依 runtime state 的 fallback），或本 Ticket Acceptance Criteria 含 `[可達性]` 或 `[行為性]` 類型標記 → **MUST** 確認對應 integration／整合層測試存在且綠（標記是票面額外要求，不是唯一觸發條件）：
        - 於票面 ALLOWED 內的測試檔或本 diff 實際新增／修改的測試檔中搜尋，以驅動 production 路徑（非直注 fixture state）方式覆蓋該行為的測試檔案／方法；測試位置不固定於特定目錄（如 `app/src/test/`、`app/src/androidTest/`、`.claude/**/tests/` 皆可）。
        - 找不到對應測試 → **MUST** 標記 **⚠️ MISSING-INTEGRATION-TEST**，對應 AC **MUST** 不得標 PASS。
        - 本項為存在性檢查起步（確認測試「存在且綠」），測試斷言的行為深度由個別 Ticket 自行以更嚴格 AC 承載（不在本項強制範圍）。
        - 結果 **MUST** 寫入 `.tmp/sv_report.md`。
     ```
  4. L102 → `   - `## Files Changed (payload)` — **MUST** 列出 `git diff --name-only DIFF_REF..DIFF_END` 的**全部**檔案（完整 payload；`tmp/verify_cmd.conf` 無 `DIFF_END` 時才以 `DIFF_REF..HEAD`）`
- [x] 完成（2026-10-14；證據見「收尾實錄（Agent，QA／SV 前）」）

### AI-4: `sv/CROSS_REVIEW.md`——概念層、verdict 表、Guard 節
- **檔案**: `.claude/skills/sv/CROSS_REVIEW.md`（`Codex 輸出格式異常 → BLOCKER`、Fail Budget、降級策略等其餘段落**不改**）
- **現值 → 目標值**:
  1. L14：`- **Layer 2 — SV Report Cross-Review（報告驗證）**：SV Report 宣稱 vs diff 事實比對、Orphan Code、Anti-Mock` → `- **Layer 2 — SV Report Cross-Review（報告驗證）**：SV Report 決策相關主張 vs 原始證據比對（檔案清單由 `structural_verify.sh` Check A/B/C 確定性核對）、Orphan Code、Anti-Mock`；並於 Layer 3 列（L15）之後新增一行：`BLOCKER 判準之唯一來源為 `cross_review.sh` PROMPT_HEADER 的「BLOCKER 判準」表；各 Layer 僅作歸類提示。`
  2. verdict 表：`| `ADVISORY` | 0 | 有非阻斷建議 | 記錄後可進入 Traycer 終審 |` → `| `ADVISORY` | 0 | 有非阻斷觀察（含輕微措辭、無品質影響連結、含糊完備性；每項在回件列一行） | 記錄後可進入 Traycer 終審 |`；`| `BLOCKER` | 1 | 真實功能、架構、scope 或 anti-mock 缺陷 | … |` → 第三欄改為 `BLOCKER 判準表所列之真實契約、API、接線或行為違反（單一來源：`cross_review.sh`）`；`| `REPORT_FIX` | 3 | `.tmp/sv_report.md` 或 payload disclosure 不可信 | … |` → 第三欄改為 `` `.tmp/sv_report.md` 的決策相關主張與原始證據矛盾（輕微措辭、無品質影響連結、含糊完備性屬 ADVISORY；檔案清單由 Check A/B/C 確定性核對） ``；各列第四欄（下一步）**逐字不變**。
  3. Guard Input Reachability 節：`` | (B) diff 外但 production 可達 | `app/src/main/` 有 non-idle / non-reset 賦值點 | `` → `` | (B) diff 外但 production 可達 | 任何 repo／runtime 路徑上有 non-idle / non-reset 賦值點（附 `檔案:行號`） | ``；於該判定表之後新增一行：`檢查範圍只限判定實際讀取且影響結果的輸入；每個輸入舉證一個 writer 與判定處 consumer，不列全 schema、不向上游無限追溯（一跳為止）。`；`**強制輸出格式**` 與 `REACHABILITY 機械 Token` 節**逐字不變**。
- [x] 完成（2026-10-14；證據見「收尾實錄（Agent，QA／SV 前）」）

### AI-5: 風險關鍵詞降為取證提示——`rules/cross-review.md` L14 與 `cross-reviewer.md`
- **檔案**: `.claude/rules/cross-review.md`（L14）、`.claude/agents/cross-reviewer.md`（職責 1 與條件豁免第 2 要件）
- **現值**:
  1. `cross-review.md` L14：`- 修正含 token：FeatureFlags / gate / Gate / cooldown / @Inject / @Module / @Provides`
  2. `cross-reviewer.md` 職責 1 首句：``1. **Checkpoint 觸發掃描**：payload 或 diff 觸碰 `PROGRESS.md` / `sv_report*.md` / `SSOT*.md` / `*_Contract*.md` / `CLAUDE.md`，或 diff 含 token `FeatureFlags` / `gate(` / `Gate(` / `isGated` / `cooldown` / `@Inject` / `@Module` / `@Provides` → 標記 `CHECKPOINT-REQUIRED`，不放行。``；條件豁免第 2 要件：`     2. 該檔 diff hunks **零**上述敏感 token`
- **目標值**:
  1. `cross-review.md` L14 → `- 修正涉及既定 feature flag 預設值或 DI 綁定（FeatureFlags／@Inject／@Module／@Provides 的綁定與 scope）之變更，且票面未明確授權：屬新 authority → MUST 立即 Checkpoint／升報（HUMAN_REQUIRED，類別依 Gate，多為 SCOPE）。關鍵詞（FeatureFlags / gate / Gate / cooldown / @Inject / @Module / @Provides）僅作取證提示：命中後須讀 hunk 判定，不獨立觸發 Checkpoint；票面已明確授權、屬已授權 scope 內之契約修復者走 Gate，不因此再找人工。`（L13 及其 sv_report 例外**逐字不變**。）
  2. `cross-reviewer.md` 職責 1 首句 → ``1. **Checkpoint 觸發掃描**：payload 或 diff 觸碰 `PROGRESS.md` / `sv_report*.md` / `SSOT*.md` / `*_Contract*.md` / `CLAUDE.md` → 標記 `CHECKPOINT-REQUIRED`，不放行。diff 含關鍵詞 `FeatureFlags` / `gate(` / `Gate(` / `isGated` / `cooldown` / `@Inject` / `@Module` / `@Provides` 時僅作**取證提示**：必須讀該 hunk 判定，只有該 hunk 變更既定 feature flag 預設值或 DI 綁定（`@Inject` / `@Module` / `@Provides` 的綁定或 scope），且票面（Verify Config／Action Items）未明確授權，才標 `CHECKPOINT-REQUIRED`（新 authority）；其餘命中列入分級表為取證註記並可 `FORWARD`。``；條件豁免第 2 要件 → `     2. 該檔 diff hunks 無 feature flag 預設值或 DI 綁定變更（關鍵詞命中僅為取證提示）`；職責 1 並補一句票面讀取來源：判定「票面是否明確授權」時，讀 `tmp/verify_cmd.conf` 的 `TICKET_ID`，對到 `plans/cso_briefing/project/Pending/`（或 `deliver/`）票檔之 Verify Config／Action Items（R0 僅有唯讀工具，無法讀到時回報「授權無法判定」並標 `CHECKPOINT-REQUIRED`，不得推定已授權）；HTML 註解（技術棧校準說明）、要件 1／3、職責 2–5 與輸出格式**逐字不變**。
- [x] 完成（2026-10-14；證據見「收尾實錄（Agent，QA／SV 前）」）

### AI-6: `ssot_context.md`——L66 零容忍收斂為明列三名稱
- **檔案**: `.claude/skills/sv/ssot_context.md`
- **現值**: L66：`**零容忍**: 若 diff 中出現 `CompositionScore`, `EyeAfPolicyOutput`, `SceneType` 等非 SSOT 名稱 → FAIL`
- **目標值**: ``**零容忍**: 若 diff 中出現 `CompositionScore`, `EyeAfPolicyOutput`, `SceneType`（僅此三名；其餘型別命名差異不屬零容忍，依 `cross_review.sh` BLOCKER 判準表歸類）→ FAIL``。L44「Model Drift」敘述及其餘段落**不改**（超出 parent §2 指定段落）。
- [x] 完成（2026-10-14；證據見「收尾實錄（Agent，QA／SV 前）」）

### AI-7: `test_report_fix_closure.sh`——新增 payload 靜態斷言組（F9）
- **檔案**: `.claude/skills/qa/tests/test_report_fix_closure.sh`（既有 F1–F8 斷言與 helper 語意不變）
- **現值**: `run_cases` 內 F8 區塊末行 `  check "F8 exits" 1/3/1/2 "$(join "${RCS[@]}")"`（其後即 `}`，L166）。
- **目標值**: 在該行後、`}` 前新增 F9：①於 scratch 複製 `ssot_context.md`（`mkdir -p "$S/.claude/skills/sv" && cp "$REAL_ROOT/.claude/skills/sv/ssot_context.md" "$S/.claude/skills/sv/"`）；②`write_report good; reset_state; run_cr PASS`；③對 `"$S/.tmp/cross_review_payload.md"` 斷言（固定字串用 `grep -F`；「prompt 段」＝payload 中 `## Authorized Exclusions` 之前的行，避免與注入的 ssot_context 混淆）：
  - 計數類：`^## BLOCKER 判準（唯一來源` ＝1；BLOCKER 表資料列（介於該標題與下一個 `^## ` 之間，pattern 見下）＝10，表頭含 `品質目的`、`適用條件`、`必備證據`；`^FINDING:`／`^PRIOR:` 行首＝0。**計數 pattern 須逐字使用 ERE**：`grep -cE '^\| [0-9]+ \|'`（awk 用 `/^\| [0-9]+ \|/`）——BRE 的 `+` 是字面字元、未跳脫的 `|` 在 ERE／awk 中是 alternation，照抄舊式寫法會誤計。
  - 必須存在（prompt 段）：`BLOCKER 不要求另找一段報告原文`、`降級不得掩蓋錯誤 PASS`、`不得就檔案清單提出任何 finding`、`一跳為止`、`ADVISORY（記錄即可，exit 0）`、`每項仍須在回件中列為一行`、`不以關鍵詞自動套用`。
  - 必須不存在（prompt 段）：`其他一律降級`、`事實錯誤 → BLOCKER`、`diff 修改超出 SV Report 聲明範圍`、`sv_report.md 格式/完備性問題`、`app/src/main/`、`治理類`。
  - 逐字保留的格式契約（固定字串各出現 ≥1）：`` `PASS` / `BLOCKER` / `REPORT_FIX` / `ADVISORY` ``、`Guard Input Reachability: [無新 Guard] / [有新 Guard — State: X — Producer: (A)/(B)/(C)] / [missing-wiring: State X 無 producer]`、`` - `REACHABILITY: NONE`（無新 Guard，或所有 producer 判定為 (A)/(B)） ``、`` - `REACHABILITY: <guard1>:<producer位置>,<guard2>:MISSING`（逐一列出每個新 Guard；producer 位置格式 `檔案:行號`；判定 (C) 一律填 `MISSING`） ``。
  - 注入的 ssot_context 段：含 `僅此三名`，且不含 `等非 SSOT 名稱`。
  - stub 回 PASS 時 `run_cr` exit＝0（對照 F6 前置）。
  F9 僅證明「送進 Codex 的 prompt 字面」，**不是**真實 Codex 行為證據；輸出標題註明 `[stub]`。
- [x] 完成（2026-10-14；證據見「收尾實錄（Agent，QA／SV 前）」）

### AI-8: 驗證、隔離重放與收尾證據
- **檔案**: 不改檔（執行驗證；重放產物僅在 `tmp/ADV-REVIEW-01/` 與暫存 worktree；收尾實錄寫入本票檔）
- **動作**:
  1. **靜態與回歸**：`bash -n` 對 `cross_review.sh` 與測試檔通過；去 heredoc 比對——`git show <實作 commit>:<cross_review.sh>` 以 AI-0 第 2 項之 awk 去除 heredoc 後 sha256 **等於**基線 `d71d1957…b94a0`，且 `PROMPT_HEADER` 起訖標記各 1；`cross-review.md` L13 的 `sed -n 13p | sha256sum` 與基線相同；階段 A.5（`3.5. **16KB ELF 對齊檢查**：` 段）前後文字相同；`git diff --stat <DIFF_REF>..<DIFF_END>`（本票自己的範圍；不用固定的 BASE_COMMIT 區間，以免別票提交或 PROGRESS 同步被算入） 僅涉 ALLOWED、FORBIDDEN 零變更；行尾——`cross_review.sh` 仍全檔 CRLF（`\r` 行數＝總行數）、其餘六檔 `\r` 行數 0；實跑 `test_report_fix_closure.sh`（F1–F8 斷言不變＋新 F9）、`python -I .claude/hooks/tests/gate_stop_guard_test.py`（42）、`python -I .claude/scripts/tests/test_gate_ledger.py`（39）全綠。
  2. **文件 consumer 一致性**：對 AI-3／4／5／6 各目標段以 grep 驗證——SKILL B-7／B-8／L102、CROSS_REVIEW.md、`cross-review.md` L14、`cross-reviewer.md`、`ssot_context.md` L66 無殘存互斥敘述（`app/src/main/`／`app/src/test` 作為固定位置、token 單獨觸發 Checkpoint、`等非 SSOT 名稱`、`事實錯誤 → BLOCKER`）。
  3. **隔離重放（ADV-GATE-04 歷史報告，真實 Codex 含失敗至多 3 次）**：
     1. 輸入：於 `tmp/ADV-REVIEW-01/replay-in/` 以 `tr -d '\r' | sed -n "$((S+2)),$((E-2))p"`（`S`＝`^## SV Report$` 行、`E`＝`^## Git Diff (scope` 行）自三個 out 檔切出 R1／R2／R3 報告，核對 sha256 前 16 碼如 X4；out 檔本身唯讀、記錄其 sha256。
     2. 環境：於主 repo 外的系統暫存目錄執行 `git worktree add --detach <暫存路徑> d724475f`（不改共用 Git config、不 fetch／pull、不動主工作樹）；只在暫存 worktree 內放入 T01a 修改後的 `cross_review.sh` 與（若有改）`ssot_context.md`——**以 `git show <實作 commit>:<path>` 寫出 blob，不得複製 CRLF 工作樹檔**，並記錄 blob sha；`structural_verify.sh` 等其餘腳本沿用 `d724475f` 檢出版本。
     3. 暫存 worktree 的 `tmp/verify_cmd.conf`：`TICKET_ID=REPLAY-ADVGATE04`、`DIFF_REF=8a7c18e9`、`DIFF_END=d724475f`、`ALLOWED=.claude/hooks/gate_stop_guard.py,.claude/hooks/tests/gate_stop_guard_test.py,.claude/settings.json,.claude/rules/ssot-governance.md`（照抄 ADV-GATE-04 原值；`cross_review.sh` L368–L401 依此決定 payload 排除）。
     4. 輸入等價預驗（`cross_review.sh` 無 dry-run，**不計入**真實 Codex 次數）：PATH 前置 stub `codex`（讀 stdin 存檔、回 `codex／PASS／REACHABILITY: NONE`）跑一次僅生成 payload；CRLF→LF 正規化後，其 `## Git Diff` 段須與歷史回聲相同（sha256 前 16 碼 `acd26d842fdec177`，1055 行）；不符 → 停止、不呼叫真實 Codex。完成後清空暫存 worktree 的 `.tmp/cross_review_state`、`.tmp/cross_review_pass`、結果檔。
     5. 若 `structural_verify` 預檢因暫存 worktree 缺 gitignore 檔案而擋下：僅補齊重放環境後重跑，**不計入** Codex 次數，不解讀為瘦身效果。
     6. 執行：三份報告各一次——每份前清空暫存 worktree 的 state／receipt（三份獨立）；將報告置為該處 `.tmp/sv_report.md`；自暫存 worktree 根目錄直接執行 `bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md 8a7c18e9..d724475f 2>&1 | tee <保存目錄>/run_k.txt`（不啟動 /qa、/sv、Claude agent session、Gate helper、Stop hook）；**每次呼叫後立即**把 `.tmp/cross_review_result_*.md` 複製到不匹配 `cross_review_*.md` glob 之保存路徑（`cross_review.sh` 每次起跑會刪該 glob）並保存 payload。失敗以「未驗證」記錄且**計入三次**，不補呼叫。
     7. 記錄（寫入收尾實錄）：①**被審範圍**＝`8a7c18e9..d724475f`（歷史實作）與**審查器版本**分開記載——歷史審查器＝`cross_review.sh` `0331a328`＋`ssot_context.md` `6092b217`，T01a 審查器＝實作 commit 之兩 blob sha；②每份報告的 verdict、exit code，以及原 finding 逐項結果：**非阻斷**（回件明確列為 ADVISORY／不阻斷）／**仍阻斷**（同一問題仍為 REPORT_FIX／BLOCKER）／**未被檢查**（回件未提及——**不得當作通過**）；對角線為主記錄（R1 報告↔F1 hunk 敘述、R2 報告↔F2 Registry producer 舉證、R3 報告↔F3 `stop_hook_active`／`last_assistant_message` 描述），非對角線觀察僅記錄不下結論；③Codex 版本、呼叫數（含失敗）、各回件與結果檔之保存路徑與 sha256。
     8. 主樹隔離證明：重放前後比對 AI-0 第 10 項各值（state 檔 sha256／size／mtime、`ADV-GATE-04.jsonl` sha256／mtime、`.tmp/cross_review_pass/` 無 `ADV-GATE-04` receipt、Registry `ADV-GATE-04` 條目不變、`git worktree list`）；任何變動 → 停止回報。
     9. 清理：先保存輸入識別、回件與結果摘要；核對暫存路徑確為本次建立；再 `git worktree remove <path>`（若因被替換的 tracked 檔拒絕，先 `git -C <path> checkout -- <被替換檔>` 再重試，不盲目 `--force`；仍拒絕 → 回報）；移除後 `git worktree list` 回到 2 筆。
     10. 效果判讀（N4／X1）：**R2 阻斷以歷史報告確有承重缺證據為前提**（R2 報告對 Registry lifecycle producer 宣稱可達而未附 `檔案:行號`；若重放時核對發現該報告其實已附有效舉證，則「R2 仍阻斷」不是正確預期，須如實記錄並回報，不為達成預期結果而忽略有效證據）。**R1＝明列非阻斷 ∧ R2＝仍阻斷** → 效果驗收可勾 PASS；R1 仍阻斷或未被檢查，或 R2 非阻斷／未被檢查 → 效果驗收**保持未勾**，如實記錄並回報 Akira，**不自動追加研究或呼叫**；R3 任一結果只記錄。結論限 N=1、同一模型、單次取樣，不宣稱一般可靠，不追溯修改歷史品質結果。
  4. **R0 與自身 /sv（Rev A.1 採甲，預授權僅限本票）**：本票 R0 若回 `CHECKPOINT-REQUIRED` 且**僅**因本票正在修訂的舊 token 規則——命中 hunk 只是規則／prompt／文件文字、無 feature flag 預設值或 DI 綁定變更、無其他獨立 finding 或未授權變更——即依 `rules/cross-review.md` R0 記錄格式寫 `.tmp/fail-advisor/R0_ADV-REVIEW-01_<時戳>.md`（七欄位，`CLASSIFICATION: TRUE-POSITIVE-RULE-GAP`，承接票號欄＝`ADV-REVIEW-01`，附命中 hunk 摘錄與「僅因舊 token 規則」判定依據），不使用 `_OVERRIDE`、不耗誤攔預算、不計入 `fail_budget_state`，**記錄後續送 Codex，不再停等同一情形的裁決**；不擴改 R0 機制、不適用其他票。R0 另有獨立 finding、實際未授權變更、或命中 hunk 含 flag 預設值／DI 綁定變更者不豁免，仍依 Checkpoint 流程升報等裁決。本票自身 `/sv` 的真實 Cross-Review 結果僅記為有限整合證據，收尾實錄明載「不作瘦身效果證據」。
  5. **收尾實錄**：寫入本票檔「收尾實錄」段（耐久載體）：AI-0 命中數、各 AC 證據、F9／回歸結果、重放全部記錄（第 3 項之 7–9）、**把頂部 `External-review decision` 的 6b② probe 欄由 `PLANNED` 改記實際結果（`EXECUTED`＋R1／R2／R3 逐項，或 `NOT_EXECUTED`＋原因；不留 `PLANNED`）**、R0 事件、已知限制（N=1；F9 僅證 prompt 字面；Codex 是否穩定遵循新 prompt 未證；`ssot_context.md` L44「Model Drift」敘述未改；`.agents/` 本機鏡像未同步；T01b 延後與觀察窗口依 parent §5）。
- [x] 完成（2026-10-14；證據見「收尾實錄（Agent，QA／SV 前）」）

## Hard Constraints
1. 只改七個 ALLOWED 檔（本票檔之收尾實錄除外，見 13）；`cross_review.sh` 僅改 `PROMPT_HEADER` heredoc 字面，去 heredoc 後 blob 的 sha256 必須等於基線；不變更 state、計數、LIMIT、token 格式、Check G、`structural_verify.sh`／`lib_verify.sh`／`verify_checklist.sh`、gate helper／hook、`ssot-governance.md`、`qa/SKILL.md`、`uc/SKILL.md`；不改階段 A.5 ELF 檢查與 `ALIGNED` 判讀；不改 REPORT_FIX 上限或 BLOCKER 輪預算。
2. 不引入 `FINDING`／`PRIOR`／disposition preflight／額外回驗／故障恢復；不實作、不引用 T01b 延後記錄為授權；不把任何上限稱為與 Gate 三輪對齊。
3. BLOCKER 表固定 10 列、不新增列、不擴大全局審查；降級項仍須在回件列為一行；**降級不得掩蓋錯誤 PASS**，且不得降低「不可達 Guard 對應 AC 不得標 PASS」之底線（AI-3 B-7 改變的只是檢查觸發條件與升級依據，由名稱字面改為實際可達性風險）。
4. `Guard Input Reachability:` 散文行與 `REACHABILITY:` token 的格式契約、`### 3.6`、角色與審查紀律、裁決第一行格式逐字不變。
5. 檔案清單與 payload 揭露一致性由既有 Check A/B/C 確定性核對，prompt 不得要求模型重核；不要求報告陳述 hunk／行數。
6. Reachability 不豁免 runtime hook／helper、不固定 Android 目錄（含測試位置）、不以 AC 名稱或標記代替實際風險；只限實際使用且影響判定的輸入，一跳為止。
7. 風險關鍵詞只作取證提示、不獨立觸發 Checkpoint；為通過審查而改變既定 feature flag 預設值或 DI 綁定且票面未明確授權＝新 authority，須升報，**本票不得為通過自身審查而做此類變更**；`cross-review.md` L13 逐字不變。
8. 行尾：`cross_review.sh` 維持全檔 CRLF（不得混行尾）、其餘 ALLOWED 檔維持 LF；`git diff` 不得出現整檔行尾改寫；比對與重放取 git blob。
9. 重放：真實 Codex 含失敗至多 3 次、不補呼叫；不讀寫主樹真實 `ADV-GATE-04` 的 budget state／receipt／ledger／Registry 條目；暫存 worktree 內才放入被審器 blob；不修改共用 Git config、不 fetch／pull、不啟動 /qa、/sv、agent session、Gate helper、Stop hook；`structural_verify` 因環境缺檔被擋的重跑不計次數且不解讀為瘦身效果。
10. 效果驗收門檻依 N4：R1 仍阻斷或 R2 被降級／未檢查不得標 PASS；「未被檢查」一律不當作通過；不自動追加研究或呼叫；不宣稱一般可靠；不追溯修改歷史品質結果。
11. 自身 `/sv` 僅有限整合證據；stub（F9）不得寫成真實 Codex 閉環 PASS。
12. R0 預授權採甲（僅限本票；授權來源＝Akira 2026-10-14 審查裁決〔Rev A.1〕，即對下列情形之 Checkpoint 事前裁決，故不違反 `rules/cross-review.md` R0 路由「R0 回報 CHECKPOINT-REQUIRED → MUST NOT 送 Codex」；該條對其他情形照常適用）：R0 僅因本票修訂的舊 token 規則攔截——命中只是規則／prompt／文件文字、無 flag 預設值或 DI 綁定變更、無其他獨立 finding 或未授權變更——即記 `TRUE-POSITIVE-RULE-GAP`（承接＝`ADV-REVIEW-01`）並**記錄後續送 Codex，不停等同一情形的裁決**；不用 `_OVERRIDE`、不耗誤攔預算、不擴改 R0 機制、不適用其他票；其他情形（獨立 finding、實際未授權變更、命中 hunk 含 flag 預設值／DI 綁定變更）不豁免，仍依 Checkpoint 流程升報。
13. 作業範圍：除七個 ALLOWED 檔與本票檔外，僅在 `tmp/ADV-REVIEW-01/`（gitignored）與系統暫存目錄的暫存 worktree 作業；不觸 `.agents/**`、第二 worktree `~/.codex/worktrees/4ba7`、Git 設定；不使用全域 glob 清理 `tmp/`。
14. 提交：commit 前綴 `[ADV-REVIEW-01]`；`.claude/rules/cross-review.md` 屬 `[GOVERNANCE-SYNC]` 表列，隨票或雙軌持久化（`ssot-governance.md` §Session Persistence），不得混入無法歸屬於本票的 dirty 檔。

## Error Handling
- AI-0 任一錨點不符 → 依模板三分流分類記錄；本票無 `app/` 標的，`BLOCKED_UNATTRIBUTED_STATE` 降為記錄後續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 則停止。
- `git check-ignore -v <ALLOWED 標的>` 命中 → 停止回報（gitignore 死結律，標配）。
- 去 heredoc 後 sha256 與基線不符，或 `cross_review.sh` 出現混合行尾／整檔行尾改寫 → 還原並停止回報。
- 編輯 `.claude/rules/*.md`、`.claude/agents/*.md` 或 `.claude/skills/**` 被權限拒絕 → 記為 `HUMAN_REQUIRED(PERMISSION)`，不改道重試同一被拒動作、不繞過 permissions。
- F1–F8 任一既有斷言因本票變更而需修改 → 停止，逐項揭露原因，不為全綠修改政策。
- R0 回 `CHECKPOINT-REQUIRED`：僅因本票修訂的舊 token 規則（命中只是規則／prompt／文件文字、無 flag 預設值或 DI 綁定變更、無其他獨立 finding 或未授權變更）→ 依 AI-8 第 4 項記 `TRUE-POSITIVE-RULE-GAP`（承接＝本票）後續送 Codex，不停等；另有獨立 finding、未授權變更、或 hunk 實為 flag 預設值／DI 綁定變更 → 不豁免，依 Checkpoint 流程升報。
- 重放：輸入等價預驗不符 → 停止、不呼叫真實 Codex；真實 Codex 呼叫失敗 → 記「未驗證」、計入三次、不補呼叫；需要第 4 次呼叫 → 停止回報；暫存 worktree 以外路徑被寫入、或主樹隔離基線（AI-0 第 10 項）任一值變動 → 停止回報；`git worktree add`／`remove` 被權限拒絕 → 記 `HUMAN_REQUIRED(PERMISSION)`，不改道、不用 `--force` 繞過。
- 重放結果 R1 仍阻斷／未被檢查，或 R2 非阻斷／未被檢查 → 效果驗收保持未勾並如實回報，不自行改 prompt 後再重放（額外呼叫須 Akira 授權）。
- 本票自身 `/sv` Cross-Review 若 FAIL，其**額外修正**涉及 `rules/cross-review.md` L13 所列檔（`ssot_context.md`、`CLAUDE.md`、`SSOT*.md`、`PROGRESS.md`、`*_Contract*.md`）→ 依 L13 立即輸出 Checkpoint 格式並等裁決（AI-6 的計畫內編輯不屬 FAIL 後修正，不觸發）；僅修改 `.tmp/sv_report*.md` 之 report-only 閉環維持 L13 既有例外。
- AI-0 第 9 項歷史輸入（`.tmp/cross_review_out_ADV-GATE-04*.txt`，非耐久載體）缺失或 sha 不符 → AI-8 隔離重放記 `NOT_EXECUTED`＋原因（probe 欄同步記錄），**不重建、不以模型生成內容替代**；其餘 AI 照常完成，效果驗收保持未勾並回報。
- 發現第二個獨立缺陷或需擴大 scope（如需改 `structural_verify.sh`、`ssot-governance.md`、gate helper／hook、`ssot_context.md` L44、`.agents/` 鏡像）→ 先本地取證與 Advisor，依 Gate 以 `SCOPE` 處置，不自行擴大。

## Acceptance Criteria
- [x] **[存在性]** AI-0 各錨點命中數與基線相符，並在收尾實錄逐項記錄；偏離項依三分流分類且不推定等價位置。（依據：AI-0 全部錨點命中數與基線相符，無漂移（HEAD d21e8f72、porcelain 空；七檔 check-ignore 零命中；eol cross_review.sh CR=LF=516、其餘六檔 CR 0；strip sha 基線一致；歷史輸入三檔存在；隔離基線取值；Registry 條目））
- [x] **[存在性]** `cross_review.sh`：去 heredoc 後 blob sha256＝`d71d1957b6efa3e285539aba8d8abf07927b930191f0df6032f9f0314f1b94a0`；`PROMPT_HEADER` 起訖標記各 1；`bash -n` 通過；全檔 CRLF（`\r` 行數＝總行數）；其餘六個 ALLOWED 檔 `\r` 行數 0。（依據：去 heredoc 後 sha256＝d71d1957…b94a0（CR 正規化；與 HEAD blob 同值）；PROMPT_HEADER 起訖各 1；bash -n OK；CR=LF=530 無裸 LF；其餘六檔 CR 0）
- [x] **[存在性]** `cross_review.sh` PROMPT_HEADER 內：`## BLOCKER 判準（唯一來源` 恰 1、BLOCKER 表資料列恰 10 且含 `品質目的`／`適用條件`／`必備證據` 欄；不含 `其他一律降級`、`事實錯誤 → BLOCKER`、`diff 修改超出 SV Report 聲明範圍`、`sv_report.md 格式/完備性問題`、`app/src/main/`；含 `降級不得掩蓋錯誤 PASS`、`不得就檔案清單提出任何 finding`、`一跳為止`、`BLOCKER 不要求另找一段報告原文`；`Guard Input Reachability:` 與 `REACHABILITY:` 格式行、裁決第一行格式行與基線逐字相同。（依據：F9 於實際 payload 驗證：標題 1、表資料列 10、三欄頭、必存在 7／必不存在 6 字串、格式契約 4 行逐字、無行首 FINDING/PRIOR 全 PASS）
- [x] **[行為性]** given scratch repo、stub codex（回 PASS）與複製入 scratch 的 `ssot_context.md`，經 `cross_review.sh` 實際組出 payload：expect F9 全部斷言成立（含 BLOCKER 表 10 列、必存在／必不存在字串、格式契約逐字、ssot_context 段含 `僅此三名` 且不含 `等非 SSOT 名稱`、無行首 `FINDING:`／`PRIOR:`）且 exit 0（基線：舊 prompt 下 F9 多項不成立；註明 `[stub]`，僅證 prompt 字面，非真實 Codex 行為）。（依據：test_report_fix_closure.sh 實跑 ALL PASS（63 PASS／0 FAIL），F9 26 項全綠；基線：舊 prompt 對 `降級不得掩蓋錯誤 PASS`／`一跳為止`／`品質目的` 命中 0、`其他一律降級`／`app/src/main/` 命中 1；[stub] 僅證 prompt 字面）
- [x] **[可達性]** 新 prompt／ssot_context 文字確實抵達審查者：producer 證據欄——`cross_review.sh` `cat > "$PAYLOAD" << 'PROMPT_HEADER'`（`檔案:行號` 執行後回填）→ `cat "$SSOT_CONTEXT"` 注入（`檔案:行號` 回填）→ `codex exec … - < "$PAYLOAD"`（`檔案:行號` 回填）；observer＝F9 所讀 `.tmp/cross_review_payload.md` 與 AI-8 重放之 payload 保存檔。（依據：producer 證據欄：cross_review.sh:249 `cat > "$PAYLOAD" << 'PROMPT_HEADER'` → :369 `cat "$SSOT_CONTEXT"` 注入 → :440 `codex exec … - < "$PAYLOAD"`；observer＝F9 讀取之 .tmp/cross_review_payload.md 實跑結果（含 ssot 段 `僅此三名`）及重放 payload 保存檔 tmp/ADV-REVIEW-01/save/payload_R{1,2,3}.md（已含新 BLOCKER 表））
- [x] **[存在性]** 文件一致性：SKILL B-4／B-7／B-8／L102、CROSS_REVIEW.md（概念層、verdict 表、Guard 節）、`cross-review.md` L14、`cross-reviewer.md` 職責 1 與條件豁免第 2 要件、`ssot_context.md` L66 皆依 AI-3～6 目標值落地，且 grep 無殘存互斥敘述（固定 `app/src/main/`／`app/src/test`、token 單獨觸發 Checkpoint、`等非 SSOT 名稱`、`事實錯誤 → BLOCKER`）；`cross-review.md` L13 逐字不變；SKILL 階段 A.5 ELF 段前後相同、`ALIGNED` 判讀文字原樣。（依據：grep 掃描 .claude/skills/sv、cross-review.md、cross-reviewer.md：`等非 SSOT 名稱`／`事實錯誤 → BLOCKER`／`其他一律降級`／`零**上述敏感 token`／`改標 PARTIAL 即放行` 零命中；`app/src/test` 僅存於 SKILL B-8 目標值之非固定例示清單（`app/src/test/`、`app/src/androidTest/`、`.claude/**/tests/` 皆可），非固定位置；L13 sha c5df70a389163eb2 與 HEAD 相同；階段 A.5 區塊 diff 為空、ALIGNED 5/5）
- [x] **[存在性]** 邊界：`git diff --stat <DIFF_REF>..<DIFF_END>`（本票自己的範圍；不用固定的 BASE_COMMIT 區間，以免別票提交或 PROGRESS 同步被算入） 僅涉 ALLOWED；`structural_verify.sh`、`lib_verify.sh`、`verify_checklist.sh`、`gate_*.py`、`.claude/hooks/**`、`settings.json`、`ssot-governance.md`、`qa/SKILL.md`、`uc/SKILL.md` 零變更。（依據：`git diff --stat d21e8f72..0cb6fd44` 7 檔 +106／−60，ALLOWED 外 0 檔、FORBIDDEN（qa/scripts、qa/SKILL.md、uc、.claude/scripts、hooks、settings.json、ssot-governance.md、PROGRESS*、app、tools、plans）觸及 0 檔；2026-10-14 /sv 階段核對）
- [x] **[行為性]** 回歸：given 實作後工作樹，`test_report_fix_closure.sh` F1–F8 全部既有斷言**不變**且全綠（預算／LIMIT／closure 政策維持原樣）、`gate_stop_guard_test.py`（42）與 `test_gate_ledger.py`（39）全綠；預期更新的舊測試：無；若有任何斷言需動，已逐項揭露原因。（依據：F1–F8 斷言全部未改且全綠；gate_stop_guard_test.py 42 OK；test_gate_ledger.py 39 OK；預期更新的舊測試：無，實際亦無）
- [x] **[行為性]** 隔離重放協定已執行並如實記錄（與效果脫鉤）：given 三份歷史報告、detached 暫存 worktree（`d724475f`＋T01a 審查器 blob）、範圍 `8a7c18e9..d724475f`、原 ALLOWED：expect 真實 Codex 呼叫（含失敗）≤3；輸入等價預驗通過；收尾實錄含被審範圍與審查器版本（分開記載）、各輪 verdict／exit、原三 finding 逐項（非阻斷／仍阻斷／未被檢查）、回件與結果檔保存路徑與 sha256；主樹隔離基線（AI-0 第 10 項）前後相同；暫存 worktree 已移除且 `git worktree list` 回到 2 筆。（依據：真實 Codex 3 次、0 失敗；輸入等價預驗（stub、不計次）Git Diff 段 1055 行 sha acd26d842fdec177 與歷史相同；被審範圍／審查器版本分開記載；回件與結果檔保存路徑與 sha256 見收尾實錄；主樹隔離基線（state 0ce0036fa63d4dd4、jsonl c44fc652508d6463 mtime 20:54:45、無 receipt）前後相同；暫存 worktree 已移除、git worktree list 回到 2 筆）
- [ ] **[行為性]** 效果驗收（有限證據，N=1，非一般可靠）：given 上列重放結果：expect **R1＝明列非阻斷 且 R2＝仍阻斷** 方可勾 PASS；R1 仍阻斷／未被檢查，或 R2 非阻斷／未被檢查 → 本項保持未勾，於收尾實錄與「未勾項全盤點」標「裁決中 Akira」並回報，不自動追加研究或呼叫；R3 僅記錄。
- [x] **[存在性]** R0 與 /sv 處置：若本票 R0 觸發，`.tmp/fail-advisor/R0_ADV-REVIEW-01_*.md` 存在七欄位且 `CLASSIFICATION` 與走向相符（僅因本票修訂的舊 token 規則攔截者＝`TRUE-POSITIVE-RULE-GAP`＋承接票號 `ADV-REVIEW-01`，非 `_OVERRIDE`，記錄後已續送 Codex；有獨立 finding／未授權變更／flag 預設值或 DI 綁定變更者不得適用此預授權，已依 Checkpoint 升報）；本票自身 `/sv` 結果於收尾實錄標註為有限整合證據、不作瘦身效果證據；頂部 `External-review decision` 6b② probe 欄已由 `PLANNED` 改記實際結果。（依據：R0 記錄 `.tmp/fail-advisor/R0_ADV-REVIEW-01_20261014_141804.md` 七欄位齊備；Akira 2026-10-14 裁決 B 後 `CLASSIFICATION: TRUE-POSITIVE-RULE-GAP`（承接 ADV-REVIEW-01，非 `_OVERRIDE`，五列：職責 1 token 三列＋職責 4(c) 名稱兩列）並已續送 Codex；自身 /sv 真實 Cross-Review：run1 REPORT_FIX（codex 來源）→ 修報告 Stage B 分類 → run2 ADVISORY exit 0，receipt `VERDICT=ADVISORY` `DIFF_SCOPE=d21e8f72..0cb6fd44`；結果僅為有限整合證據，不作瘦身效果證據；頂部 External-review decision 6b② probe 欄已由 PLANNED 改記 EXECUTED）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案；FORBIDDEN 零變更；`DEFERRED-TO-DOCSYNC` 項（PROGRESS 同步、ARCHIVE Done 標記、票檔搬移、勾選）於收尾實錄揭露。（依據：`git diff --stat d21e8f72..0cb6fd44` 7 檔均在 ALLOWED、FORBIDDEN 觸及 0 檔；`DEFERRED-TO-DOCSYNC` 項〔PROGRESS 同步、ARCHIVE Done 標記、票檔搬移、勾選〕已於收尾實錄「待追補」與 Verify Config 註記揭露）

## 收尾實錄（Agent）

- **進度**：AI-0 至 AI-7 完成；AI-8 第 1–3、5 項完成，第 4 項（R0 與自身 /sv）依票面為 /sv 階段事件，結果於 /sv 後追補本節。**尚未 commit**（由 /qa Step 4.5 Auto Commit 承載）；DIFF_REF 已依三原子動作回填為 `d21e8f72`（票面＋`tmp/verify_cmd.conf` 由 activate_ticket.sh 同值再生）。lifecycle：`TICKET_RIGOR`→`IN_PROGRESS`（2026-10-14）。
- **AI-0**：全部錨點與基線相符，無漂移分類事件；`ALREADY_EXECUTED`／`SUPERSEDED` 皆無。
- **實作面**（working tree vs `d21e8f72`）：7 檔 +106／−60。`cross_review.sh` 僅 PROMPT_HEADER（L264–L313 整段、§3.5 L317–L322）；`SKILL.md` B-4／B-7／B-8／L102；`CROSS_REVIEW.md` 概念層、verdict 表、Guard 節；`rules/cross-review.md` L14；`cross-reviewer.md` 職責 1＋條件豁免第 2 要件；`ssot_context.md` L66；`test_report_fix_closure.sh` 新增 F9（+29L）。實作以票面目標值程式化套用（自票檔區塊抽取，非轉錄）。
- **審查器版本（與被審範圍分開記載）**：歷史審查器＝`cross_review.sh` blob `0331a328`＋`ssot_context.md` blob `6092b217`；T01a 審查器＝`cross_review.sh` LF blob `a6b61a76`＋`ssot_context.md` blob `7e052b49`（重放時以 LF 正規化內容寫入暫存 worktree；commit 後須核對 blob 同值）。被審範圍＝`8a7c18e9..d724475f`（ADV-GATE-04 歷史實作）。
- **重放環境與預驗**：detached worktree `d724475f`，路徑位於系統暫存目錄；conf＝`REPLAY-ADVGATE04`／`8a7c18e9`／`d724475f`／ADV-GATE-04 原 ALLOWED；`structural_verify` 預檢未被環境擋下（無需補環境）；輸入等價預驗以 stub codex 產生 payload（不計 Codex 次數）：Git Diff 段 1055 行、sha256 前 16 碼 `acd26d842fdec177`，與歷史回聲一致；新 payload 83887 bytes（歷史 R1 79243）。三份輸入 sha：R1 `ef7a6e24f99772fa`／R2 `15da7e4481563191`／R3 `d0c703655225e844`；來源 out 檔 sha256 前 16 碼 `da1f6eef47151094`／`a1f7d638cc9df727`／`ed04e6dc9780802c`。
- **重放結果**（真實 Codex `codex-cli 0.162.1`，`gpt-5.6-sol`／high；共 3 次呼叫、0 失敗；每份前清空 state／receipt；每次後即存回件）：

| 重放 | 報告 | verdict／exit | 對角線原 finding | 判讀 |
|------|------|----------------|------------------|------|
| R1 | 歷史 R1 報告（含「7 hunk」措辭） | REPORT_FIX／3 | F1 hunk 數敘述 | **未被檢查**（回件 hunk 0 次提及；「ADVISORY: 無」，無法區分「已檢視而降級」與「未檢視」；不當作通過）。另提兩項獨立 REPORT_FIX：Registry／Stop payload producer 缺 `檔案:行號`、「非直注 fixture state」與測試矛盾 |
| R2 | 歷史 R2 報告（Registry 僅列檔案名） | REPORT_FIX／3 | F2 Registry producer 舉證 | **仍阻斷**（producer `檔案:行號` 缺，補證判準明確；符合「R2 阻斷以歷史報告確有承重缺證據為前提」——該報告 `registry_service` 0 命中） |
| R3 | 歷史 R3 報告（`stop_hook_active`／`last_assistant_message`） | REPORT_FIX／3 | F3 欄位描述 | **未被檢查**（回件 stop_hook_active／last_assistant_message 0 次提及；僅記錄，不下結論）。另提 producer 缺證與「非直注 fixture state」兩項 |

  非對角線觀察（僅記錄，不下結論）：三份新回件皆以 Reachability producer 缺證與「非直注 fixture state」為 REPORT_FIX 主因，與歷史各輪的焦點不同；新 prompt 的 `FINDING:` 行未出現（符合 T01a 不含該協定）；R2／R3 回件含 `REACHABILITY:` 逐項 token（R1 為 MISSING 條目）。
- **效果驗收（AC 保持未勾）**：門檻為「R1＝明列非阻斷 ∧ R2＝仍阻斷」。R2 成立；**R1＝未被檢查，不滿足**。依 N4／HC10：不標 PASS、不自動追加研究或呼叫，如實回報 Akira（裁決中：Akira）。額外呼叫須 Akira 授權。
- **主樹隔離證明**：重放前後 `.tmp/cross_review_state/ADV-GATE-04`＝62 bytes／`0ce0036fa63d4dd4`、`.git/gate/ADV-GATE-04.jsonl`＝5938 bytes／`c44fc652508d6463`／mtime `2026-10-13 20:54:45`、`.tmp/cross_review_pass/` 無 `ADV-GATE-04`、Registry `ADV-GATE-04` 未動（本票僅動 ADV-REVIEW-01 條目）、`git worktree list` 回到 2 筆，皆相同。
- **清理**：先保存成果（tmp/ADV-REVIEW-01/save，gitignored）；暫存 worktree 內被替換的 2 個 tracked 檔以 `git -C <path> checkout --` 還原；一次 compound 清理指令（含 `rm -rf`）被權限拒絕——**未改道強刪、未用 `--force`**；改以票面既定序列：還原 tracked 檔後 `git worktree remove` 回報「含 untracked」，再以單檔 `rm`＋`rmdir` 移除本次建立的 stub `bin/`（僅本次暫存區內、我方產物），最後 `git worktree remove` 成功。
- **保存清單（tmp/ADV-REVIEW-01/save，非耐久載體；sha256 前 16 碼）**：run_R1.txt `880cfae2d456a8a4`／run_R2.txt `919cfea7e1fe5b58`／run_R3.txt `765beeff6aa8a9c4`；payload_R1.md `8fa6a1670d10231b`／R2 `d34f5ce9a7a686d2`／R3 `bd9f4521207a4563`；result_R1.md.keep `2af2c5fda7f8a211`／R2 `7db594efa13e31f9`／R3 `98fc24706aae977a`；equiv_new_diff.txt `acd26d842fdec177`；cross_review.t01a.sh `64abcaeb7530cdf3`；ssot_context.t01a.md `612a46f164d8c3cd`。
- **回歸與靜態**：`test_report_fix_closure.sh` ALL PASS（63 PASS；F1–F8 斷言未改）；`gate_stop_guard_test.py` 42 OK；`test_gate_ledger.py` 39 OK；去 heredoc sha256＝基線；L13 與 ELF 區塊未變。
- **已知限制**：N=1、同一模型、單次取樣，不宣稱一般可靠；F9 僅證 prompt 字面；新 prompt 對 R1／R3 原 finding 呈「未被檢查」代表該類措辭是否被降為 ADVISORY 未被證明（回件未列一行 ADVISORY，亦偏離新 prompt「每項仍須列為一行」要求——觀察記錄，非本票 AC）；`ssot_context.md` L44「Model Drift」敘述未改；`.agents/` 本機鏡像未同步；SKILL B-8 目標值仍含 `app/src/test/` 作為非固定例示；T01b 延後（觀察窗口依 parent §5）。
- **QA／commit（2026-10-14）**：Step 0 PASS（commit 前 diff 為空）；Build／Lint N/A（docs preset）；Step 3.5 ⚠️ 項皆為 app/src/main 既有；Step 4 PROGRESS 同步 SKIPPED（DEFERRED-TO-DOCSYNC）；Auto Commit `0cb6fd44`（7F +106／−60，`DIFF_REF=d21e8f72`→`DIFF_END=0cb6fd44`）；commit blob 與重放審查器 blob 相同（`a6b61a76`／`7e052b49`）。
- **R0 事件（AI-8.4，2026-10-14 14:18；記錄 `.tmp/fail-advisor/R0_ADV-REVIEW-01_20261014_141804.md`，非耐久載體，要點如下）**：stub codex 僅生成 payload（56313 bytes，無 receipt／state 殘留）後委派 `cross-reviewer`。R0 載入**舊版**職責 1（票面 HYPOTHESIS 實測結果＝舊定義）。裁決 FAIL：三列職責 1 token CHECKPOINT-REQUIRED（`cross-reviewer.md:13`、`rules/cross-review.md:14`、`cross_review.sh:275` 表 #6——皆為規則／prompt 文字，無 flag 預設值或 DI 綁定變更，票面 AI-5／AI-1 已授權，事實上符合甲條件）；另兩列**職責 4(c) 零容忍型別名 BLOCKER**（`cross_review.sh:277` 表 #8、`ssot_context.md:66`——三名僅出現於票面逐字指定的禁令條文本身，無 .kt、無型別定義或使用處，R0 規格缺「條文自我提及」豁免分支）。後者為**獨立 finding，不屬「僅因舊 token 規則」**，依 HC12 不適用甲預授權；依 `rules/cross-review.md` R0 路由（FAIL／CHECKPOINT-REQUIRED → MUST NOT 送 Codex）**未送 Codex**，輸出 Checkpoint 等 Akira 裁決。無 `_OVERRIDE`。落盤時 CLASSIFICATION＝PENDING-AKIRA；隨後 Akira 裁決 B（見下一條）。
- **Akira 裁決 B 與 R0 分類（2026-10-14，R0 回件並停車之後）**：五列（職責 1 token 三列＋職責 4(c) 名稱兩列）統一記 `TRUE-POSITIVE-RULE-GAP`，承接 ADV-REVIEW-01；不用 `_OVERRIDE`、不耗誤攔預算、不擴改 R0；記錄後續送 Codex。R0 記錄檔已於同日以「裁決補記」追加（不改寫原 1–6 欄）。**真實歷史**：R0 回件後主 agent 直接停車並輸出人工 Checkpoint，**當時未完成必要的 Gate 路由**；現在的取證與裁決不是停車前已執行的 consult。未偽造 Advisor、未硬湊 round、未記假 pass；未建立再解除的 human pending；原始 ledger 未被改寫（ledger 現有事件僅為下述 SV REPORT_FIX incident 與 check receipts）。
- **三類結果分開記錄**：①**規則缺口**——R0 職責 4(c) 未區分「禁令文字自我提及」與「實作型別使用」；②**路由缺口**——本次 R0 FAIL 直接進人工 Checkpoint，主 agent 未完成必要的本地判斷／Advisor 承接路徑；依 Akira 2026-10-14 分層解題需求（Skill／executor → R0 審查協作 → 真實 Advisor → 必要時 PPSS → 消費答案、修復、驗證與續作；同一問題各層各最多三次實質修復與回驗，不要求跑滿；各層不創造新權限），**列為後續設計輸入，本票不修**。此缺口不縮成單純 ledger 格式問題，也不是「三輪後升級」才能解決的問題。③**效果證據**——歷史重放 R1／R3 未被檢查、R2 仍阻斷（見前）。前兩項為後續分層路由設計的已知輸入，不在本票追加實作，不默認沿用舊 T01b 完整狀態機方案；各層預算、Gate helper／hook、R0 路由、PPSS 機制本票皆未改。
- **自身 /sv Codex Cross-Review（真實，`codex-cli 0.162.1`，`gpt-5.6-sol`／high；範圍 `d21e8f72..0cb6fd44`；有限整合證據，不作瘦身效果證據）**：
  - run1（15:45）verdict **REPORT_FIX**（`REPORT_FIX_SOURCE=codex`，exit 3）：finding＝SV Report Stage B 稱「無新 Guard」，但 F9 新增 `hasF()` 條件分支與依 payload／ssot 檔內容決定結果的 `awk` 斷言（`test_report_fix_closure.sh:168-194`），應列「有新 Guard（測試斷言層）— Producer (B)」並附 writer／consumer 行號。處置：Gate 記 INC-001（`sv-report:reachability-classification`，本地取證 consult 一筆〔local，已據實為本地逐行核對〕，無 Advisor、無 PPSS）、`round start`（策略：僅更正 sv_report Stage B 分類，不改程式碼）；codex 來源不使用 `REPORT_FIX_CLOSURE`。
  - run2（15:47）verdict **ADVISORY**（exit 0）：唯一 ADVISORY＝「效果驗收未達（R1、R3 未被檢查），報告未錯誤宣稱 PASS，不構成 REPORT_FIX」；`REACHABILITY: hasF:…cross_review.sh:249,ssot_context-injection:…:369`；receipt `.tmp/cross_review_pass/ADV-REVIEW-01`＝`VERDICT=ADVISORY`、`DIFF_SCOPE=d21e8f72..0cb6fd44`、`COMMIT=0cb6fd44…`。`round end --result pass` → INC-001 closed（rounds=1/3、failed=0）。機械輪次 R1（`ROUND=0` 因 REPORT_FIX 不遞增），`REPORT_FIX_COUNT` 達 1 後隨 ADVISORY 清 state；邏輯輪次帳在 vault，未對齊。
  - 回件與結果檔保存：`tmp/ADV-REVIEW-01/self_sv/run1.txt`／`run2.txt`／`result1.md.keep`／`result2.md.keep`（gitignored，非耐久載體）。
  - `structural_verify.sh --post`（QA Step 5）：E／G／F／GH 全過，verdict **ADVISORY**（F.2 PROGRESS.md 80 行 > 60 的既有警示），exit 0。Step 10 Done 標記與 Step 10.5 對 `PROGRESS(_ARCHIVE).md` 的寫入命中票面 FORBIDDEN → `DEFERRED-TO-DOCSYNC`（不開票）。
- **收束裁決與限定交付聲明（Akira 2026-10-14）**：接受 N=1 有限證據與效果驗收未達門檻，限定交付，移交 `REVIEW`（Agent 最高可寫 lifecycle；`CLOSED` 留終審角色）。不追加實作、研究、重放或 Codex 呼叫。ADV-GATE-04 僅為處置先例，本裁決是對本票的獨立決定。
  - **已證明（有界）**：①prompt／文件字面：F9 `[stub]` 26 項＋F1–F8 不變，證明送進 Codex 的 prompt 字面（BLOCKER 表 10 列、REPORT_FIX／ADVISORY 收窄、Reachability 不固定目錄、格式契約逐字、ssot_context 收窄）；②`cross_review.sh` 去 heredoc 後 sha256＝基線，PROMPT_HEADER 以外零變更；③ALLOWED 7 檔 +106／−60，FORBIDDEN 零變更；④回歸 42／39 測試全綠；⑤隔離重放協定已執行（真實 Codex 3 次、0 失敗、輸入等價預驗通過、主樹隔離前後相同、暫存 worktree 已移除）；⑥自身 Cross-Review 回驗為 ADVISORY（有限整合證據，不作瘦身效果證據）。
  - **未驗／未達門檻（UNVERIFIED，非 PASS）**：效果驗收 AC 的門檻為「R1＝明列非阻斷 ∧ R2＝仍阻斷」。**probe 已執行，但效果未達既定門檻**：R1（原「7 hunk」措辭 finding）＝**未被檢查**、R2（Registry producer 舉證缺失）＝**仍阻斷**、R3（`stop_hook_active`／`last_assistant_message` 描述）＝**未被檢查**（僅記錄，不設門檻）。回件對 R1／R3 原 finding **未提及**——**不解讀為非阻斷，亦不作為瘦身有效之證據**；「新 prompt 可能因降級而略過 R1／R3 原 finding」僅為**未驗假說**（回件於 R1 寫「ADVISORY: 無」，無法區分「已檢視而降級」與「未檢視」）。N=1、同一模型、單次取樣，不宣稱一般可靠。
  - **未勾項全盤點**：
    - AC [效果驗收（有限證據，N=1，非一般可靠）]: 豁免：Akira 2026-10-14 接受限定交付；效果未達門檻，UNVERIFIED，非 PASS。
    - AC [`assembleDebug` PASS]: 豁免：docs preset N/A（票面顯式劃記，無編譯產物）。
    - Action Item: 無未勾項（AI-0～AI-8 皆已勾；AI-8 第 4 項 R0 事件與第 5 項收尾實錄均已完成）。
- **本票 N=1 真實自主閉環（事實記錄）**：自身 /sv 的 Codex run1 出 REPORT_FIX（codex 來源）→ Gate `INC-001` finding → 一筆本地取證 consult（真實，逐行核對 `test_report_fix_closure.sh:168-194` 與 writer `cross_review.sh:249/:369`）→ 一輪修報告（僅更正 `.tmp/sv_report.md` Stage B Guard 分類）→ 回驗 run2 ADVISORY → `round end --result pass`，INC-001 closed（rounds=1/3、failed=0）。**無 Advisor、無 PPSS、無 `REPORT_FIX_CLOSURE`**。僅為本票單次實例，**不宣稱全系統首例，不宣稱完整分層升級已實作**。
- **Codex 狀態（以原始回件與 receipt 為準）**：run1＝REPORT_FIX（已消除：Stage B 分類措辭，經修報告後 run2 未再提出）；run2＝**ADVISORY**（exit 0），receipt `.tmp/cross_review_pass/ADV-REVIEW-01`：`VERDICT=ADVISORY`、`DIFF_SCOPE=d21e8f72..0cb6fd44`、`COMMIT=0cb6fd44830228ff061e375bebde1d0d635bbee5`。**不改記為 Codex PASS**。run2 的唯一 ADVISORY 項（效果驗收 R1／R3 未被檢查）**未處置**——由本節豁免聲明如實承接，非已消除。Registry `reviews.codex` 欄位取值見 checkpoint 之 `codex_verdict_actual`。
- **T01b 保持延後，不自動開票**：觀察本票收束後的三張相關 Cross-Review 票（**有無 REPORT_FIX 皆納入**）；必要補正遭 LIMIT、同問題反覆未解、或 Advisor 有策略卻不能回驗，可提前重新評估（不要求等滿三張）。不為觀察新增計數器或流程。
- **DEFERRED-TO-DOCSYNC（不開票）**：`.claude/PROGRESS.md` Dashboard 同步與 `PROGRESS_ARCHIVE.md` Done 標記（命中票面 FORBIDDEN）留後續 `/compact` 標準 SOP；本票未修改 Gate、R0 路由、預算或其他審查機制。
- **後續設計輸入（本票不修）**：R0 職責 4(c) 規則缺口；R0 FAIL→人工 Checkpoint 的路由缺口（含主 agent 未承接本地判斷／Advisor 路徑）；`ssot_context.md` L44「Model Drift」敘述、`.agents/` 鏡像、SKILL B-8 例示清單中的 `app/src/test/` 維持原狀。
- **收尾提交**：單票前綴 `[ADV-REVIEW-01]`——票檔 `Pending → deliver`（凍結點：首次進版控）；實作 commit `0cb6fd44`；Registry `REVIEW`；`TICKET_REGISTRY.json` 為 gitignored，不入提交。
