# MEO-RESIDUAL-01: 移除 MEO v1 退役後殘留之現役干涉，使 Cross-Review BLOCKER 路徑回到設計 exit code

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T01`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261009-meo-residual-cleanup-plan.md`（`.tmp` 為短期傳遞，本票自足，不依賴該檔留存）；Parent 狀態＝`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`／`Fixture: false`；Parent 讀取時刻＝2026-10-09 14:5x（HEAD `35c76072`）。上游診斷＝`.tmp/ticket-author/20261009-meo-residual-cleanup-diag.md`（`Gate: PASS`／`Verdict: PARTIALLY_CONFIRMED`）；退役歷史＝`MEO-DECOMM-01`（冊面 CLOSED，票檔 `plans/cso_briefing/project/deliver/archive/MEO/MEO-DECOMM-01_MEO_V1_Enforcement_Decommission.md`）。裁決＝Akira 2026-10-09（R1／R2／R4 批准；R3 使用者層記憶拆為獨立 cleanup；D7 改採整段移除；順序：本票 → Advisor-first／Stop Hook 票 → REPORT_FIX 自動閉環）。MEO v1 已退役、v2 未建置且不再需要：本票只移除現役干涉，不復活、不補完任何 MEO 設計。

**Rev B（2026-10-09，Akira／Desktop 裁決；初版視為 Rev A，不新配號）**：僅調整冊面 VOID 通道之執行順序與失敗處置——AI-2 改為「copy→SHA 比對→`registry_upsert_ticket` 將 `file` 改指 `void/`→read-back→`registry_void_ticket`（先 `MEO-T4` 後 `MEO-T3`）→read-back→checkpoint（保留原 fallback）→AI-3 才刪 `Pending/`」；HC-A／HC-B／Error Handling／AI-0／檔頭順序說明同步。R-n 範圍、ALLOWED、FORBIDDEN、AC 期望值皆未變。裁決依據：F1 採認（VOID 跨專案拒收風險消解，CONFIRMED：見 AI-2 Claim）、F2 批准改序、F3 外審 WAIVED、F4 已結（`f2ee4597`，工作樹 clean）。Rev B 起草時復驗：`35c76072..HEAD` 僅觸及 `.claude/skills/ticket-author/SKILL.md`（非 ALLOWED），`cross_review.sh`／`PROGRESS.md`／兩 Pending 票檔 hash 與 `TARGET_BLOB`／AI-0 記錄一致（錨點未漂移）。
**External-review decision**: WAIVED（Trigger 6b1；理由：本地 live code＋兩次 provider 實測已足，票為有界可逆）

**Rev C（2026-10-09，Akira／Desktop 裁決，RGR PASS 後之局部收斂；lifecycle 維持 `TICKET_RIGOR`，不重跑 RGR）**：`MEO-T3`／`MEO-T4` 已由 Desktop 先行完成等價作業（冊面 `lifecycle=VOID`、`file` 指 `void/<basename>`、`void_reason` 已落，`Pending/` 已無兩票檔；見冊面 `checkpoint`），故 **AI-1～AI-3 與 HC-A／HC-B 及相關 Error Handling 列標為 `N/A — terminal state already completed`，不再執行**，本票自 AI-4 續跑；AI-0 中與兩票 `Pending/` 錨點相關之項同標 N/A，其餘錨點（`cross_review.sh`、`PROGRESS.md`、保存性、基線實測）仍為 AI-4～AI-6 所依賴，保留。RGR 結論（`.tmp/rigor-gate/RGR_MEO-RESIDUAL-01_20261009.md`）：M-1 不適用（兩票已是 VOID，不再有批次 void 半成功問題）；m-1、m-2、m-4 採納（見 HC-D／EH／AC／AI-4）；m-3 不採納（terminal cleanup，不增 `audit_id` 契約）。ALLOWED／FORBIDDEN 與 R-n 範圍未變。

**簽發日期**: 2026-10-09
**簽發者**: Claude
**來源議題**: MEO
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: MEO-DECOMM-01 —— 退役範圍、封存位置與已知非本票殘留之來源
- `coordination_dependency`: 無（下游 Advisor-first／Stop Hook 票以本票為 execution_dependency，不在本票處理）

## Verify Config
TICKET_ID=MEO-RESIDUAL-01
CREATED=2026-10-09
PRESET=docs
ALLOWED=.claude/skills/sv/scripts/cross_review.sh,.claude/PROGRESS.md,plans/cso_briefing/project/Pending/MEO-T3_H1_H2_Validation.md,plans/cso_briefing/project/Pending/MEO-T4_Effort_Adapter.md,plans/cso_briefing/project/void/MEO-T3_H1_H2_Validation.md,plans/cso_briefing/project/void/MEO-T4_Effort_Adapter.md
FORBIDDEN=archive/meo-v1/**,.claude/settings.json,.claude/settings.json.pre-decomm.bak,.claude/PROGRESS_ARCHIVE.md,.claude/rules/**,.claude/skills/ticket-author/SKILL.md,.claude/skills/ticket-run/**,.agents/**,plans/cso_briefing/project/file/**,plans/cso_briefing/project/deliver/**,plans/cso_briefing/reports/**,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=10
DIFF_REF=f2ee4597
ANCHOR_PROBED=2026-10-09 16:26
BASE_COMMIT=35c76072
TARGET_BLOB=.claude/skills/sv/scripts/cross_review.sh:80a9f39c;.claude/PROGRESS.md:d09ef24f;plans/cso_briefing/project/Pending/MEO-T3_H1_H2_Validation.md:43d5fb59;plans/cso_briefing/project/Pending/MEO-T4_Effort_Adapter.md:6764da37
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

> PRESET=docs：本票零 `.kt`／`.xml`（`.claude/` 腳本與治理文本＋冊面操作），`assembleDebug` N/A。MAX_LINES＝估算遙測（刪 2 行＋刪 3 行），非 gate。
> FORBIDDEN 中含 `**` 之 glob 在 `verify_checklist.sh` 對含 `/` 的 token 為字面比對（已知缺陷，見 memory `verify-checklist-glob-token-bug`），**不構成機械閘**；FORBIDDEN 之實際執法＝AC「`git diff --name-only` ⊆ ALLOWED」＋下列 Hard Constraints。`.claude/skills/ticket-author/SKILL.md`（精確路徑）非本票範圍（其先前 dirty 變更已於 `f2ee4597` 提交，Rev B 起草時工作樹 clean；仍列 FORBIDDEN）。

## Action Items

> Rev C：AI-1～AI-3（兩票搬遷與冊面 VOID）已由 Desktop 先行完成，標 `N/A`；本票實際執行順序為 AI-0（保留項）→ AI-4 → AI-5 → AI-6。

### AI-0: 錨點復驗（派工後、修改前）＋基線取證
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`、`.claude/PROGRESS.md`、`plans/cso_briefing/project/Pending/MEO-T3_H1_H2_Validation.md`、`…/MEO-T4_Effort_Adapter.md`
- **動作**: 逐字確認下列現值（2026-10-09 14:59 live 取樣；行號僅供定位，以字面為準）：
  - `cross_review.sh`：註解行 `# MEO-T2 AI-5: Codex 駁回累加(mechanical signal only, decision-tree.yaml 消費)` 恰 1 次；其下一行以 `[[ "$VERDICT" == "BLOCKER" ]] && { _meo_sf="${MEO_STATE_FILE:-STATE.md}"` 起首恰 1 次；`.claude/skills`、`.claude/hooks`、`.claude/scripts` 內 `STATE\.md|MEO_STATE_FILE|MEO_VERIFIER_REJECT_COUNT` 命中僅此一行；工作樹行尾為 CRLF（`grep -c $'\r'` 等於 `wc -l`，基線 488／488）。
  - `PROGRESS.md`：`^17\. \*\*🔴 MEO-T3 核心實作與 scope cleanup`、`^19\. \*\*🟡 α 第 3 次額度凍結中`、`24. **🔴 \`MEO-T3\` lifecycle 現為 PENDING` 三個字面各恰 1 次，各為獨立單行；全檔 80 行；工作樹行尾為 LF（`git ls-files --eol` 顯示 `w/lf`）。
  - ~~冊面 `MEO-T3`／`MEO-T4` 為 `PENDING` 之錨點~~ **N/A — terminal state already completed**（兩票已為 `VOID`，`file` 指 `void/<basename>`）。
  - ~~`Pending/` 兩票檔 sha256 與 `void/` 無 `MEO*` 檔之錨點~~ **N/A — terminal state already completed**（`Pending/` 已無兩票檔，`void/` 兩檔 2590／3008 B）。
  - 保存性基線（供 AI-6 比對）：`git rev-parse HEAD:archive/meo-v1`＝`8fa65cd46371b6bb01753902e96e4bd2aed4df2d`；`git hash-object .claude/settings.json.pre-decomm.bak`＝`e34091001ddc14836dc949f5d997424ff2d6726b`；存證 `D:\DevSources\Skill-Advisor\deliver\MEO-DECOMM-01_evidence\` 18 檔，逐檔 `sha256sum | sha256sum` 彙總前 16 碼＝`dc8068c423e4c35a`。
  - 基線實測（scratch，非 repo）：以 PATH 假 `codex` 驅動**真實** `cross_review.sh`（`codex` 必須解析到 stub，否則中止；scratch 路徑須為 POSIX 形式、不得含 `C:/`），記錄「無 `STATE.md` 時 BLOCKER R1／R2／R3 皆 exit 2、REPORT_FIX＝3、PASS＝0」；於含 repo 副本 `PROGRESS.md` 之 scratch git repo 跑 `session_start.sh reset`，記錄「額度凍結」與「`MEO-T3` lifecycle 現為 PENDING」各命中 1。
  - 工作樹 `git status --short` 無輸出（clean；先前 `.claude/skills/ticket-author/SKILL.md` dirty 變更已於 `f2ee4597` 提交）；若出現任何 dirty 檔，記錄之，**不得**併入本票、不得碰觸。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置（本票僅觸及 `.claude/` 與 `plans/` 可逆治理面：記錄分類後以工作樹現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 則停止）。**
- [x] 完成（2026-10-09：保留項錨點復驗相符——`cross_review.sh` 註解行／計數行各 1、`PROGRESS.md` 三字面各 1 且 80 行、保存性基線 `8fa65cd4…`／`e3409100…`／存證 18 檔 `dc8068c423e4c35a`；PATH-stub 基線＝BLOCKER 2/2/2、REPORT_FIX 3、PASS 0、STATE.md 有則被改寫、`額度凍結`／`MEO-T3 lifecycle 現為 PENDING` 各 1；兩票相關錨點 N/A）

### AI-1: ~~備份 Pending 票檔至 void/~~ N/A — terminal state already completed
- Desktop 已 move 兩票檔至 `void/`（2590／3008 B）；`Pending/` 已無兩票檔。
- [ ] ~~完成~~ (N/A)

### AI-2: ~~冊面 VOID~~ N/A — terminal state already completed
- `MEO-T3`／`MEO-T4` 已為 `VOID`，`file` 指 `void/<basename>`，`void_reason` 已落（冊面 `checkpoint` 記載）。
- [ ] ~~完成~~ (N/A)

### AI-3: ~~移除 Pending 來源~~ N/A — terminal state already completed
- `Pending/` 兩票檔已不存在。
- [ ] ~~完成~~ (N/A)

### AI-4: cross_review.sh 移除 MEO 計數區塊（R1／D7）
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`
- **現值**: 註解行 `# MEO-T2 AI-5: Codex 駁回累加(mechanical signal only, decision-tree.yaml 消費)` 及其下一行 `[[ "$VERDICT" == "BLOCKER" ]] && { _meo_sf="${MEO_STATE_FILE:-STATE.md}"; _meo_c="$(grep -m1 '^MEO_VERIFIER_REJECT_COUNT=' "$_meo_sf" 2>/dev/null | cut -d= -f2)"; grep -q '^MEO_VERIFIER_REJECT_COUNT=' "$_meo_sf" 2>/dev/null && sed -i "s|^MEO_VERIFIER_REJECT_COUNT=.*|MEO_VERIFIER_REJECT_COUNT=$(( ${_meo_c:-0} + 1 ))|" "$_meo_sf"; }`（兩行，位於 `# Fail budget enforcement` 之前，其前為空行）
- **目標值**: 兩行整段刪除（淨刪 2 行、0 新增）；不留 fallback、不新增 counter、不改其餘任何行；保持工作樹 CRLF（不得產生 mixed EOL 或整檔換行膨脹）；`bash -n` 通過
- [x] 完成（2026-10-09：刪除 L450–451，`bash -n` OK，CRLF 486/486，diff 恰 2 deletions／0 insertions）

### AI-5: PROGRESS.md 刪除三個過期 MEO 待辦（R2）
- **檔案**: `.claude/PROGRESS.md`
- **現值**: Next Actions #17（`MEO-T3 核心實作與 scope cleanup 已機械完成…`）、#19（`α 第 3 次額度凍結中…`）、#24（`` `MEO-T3` lifecycle 現為 PENDING… ``）各為獨立單行
- **目標值**: 此三行整行刪除（80 → 77 行）；不重編其餘編號、不動 #26 Schedule 與其他項目、不寫 `PROGRESS_ARCHIVE.md`；保持工作樹 LF
- [x] 完成（2026-10-09：刪除 #17／#19／#24，80→77 行，LF，diff 3 deletions／0 insertions）

### AI-6: 驗證與收尾證據
- **檔案**: 無新增編輯（scratch 測試腳本置於 session scratchpad，不入 repo）
- **動作**: 逐項執行並保留輸出作為收尾實錄證據：①PATH-stub 矩陣（`codex` 解析防呆）；②`STATE.md` 存在情境之 hash 不變；③scratch 內 `session_start.sh reset` 輸出字面檢查；④exact-token／`STATE\.md` grep；⑤保存性 hash 比對；⑥`git diff --name-only` 與 `--stat`；⑦行尾檢查；⑧顯式路徑 staging（見 HC-D）。
- [x] 完成（2026-10-09：①～⑧ 皆已執行，結果見各 AC；staging 僅 4 個 ALLOWED 檔，`.claude/rules/ssot-governance.md` 範圍外 dirty 檔未 stage）

## Hard Constraints
1. ~~**HC-A（冊面寫入通道）**~~ N/A — terminal state already completed（AI-2 已由 Desktop 完成）。
2. ~~**HC-B（搬遷順序）**~~ N/A — terminal state already completed（AI-1～AI-3 已由 Desktop 完成）。
3. **HC-C（行尾）**：各編輯檔維持其工作樹既有行尾：`cross_review.sh` 為 CRLF、`PROGRESS.md` 為 LF。修改後不得產生 mixed EOL，`git diff` 不得顯示整檔換行膨脹（`cross_review.sh` 之 diff 須恰為 2 行刪除、0 新增）。
4. **HC-D（staging）**：commit staging 僅允許明列之 ALLOWED 檔，以**顯式路徑** `git add <path>…`。禁止 `git add -A`、`git add .`、`git commit -a` 或任何會納入本次 cleanup 清單以外檔案之操作；亦不得為通過 scope gate 而 stash／restore／commit 清單以外之檔案。
5. 禁止修改 `TICKET_REGISTRY.json` 本體（冊面僅經 mcp-ticket-platform 原語）；禁用 `registry_append_note`（APP 端恆回 `REGISTRY_CROSS_PROJECT_REF`）。
6. 禁止復活或補完任何 MEO 設計；禁止新增 MEO 相關 fallback、counter、state；日後新機制另開新票，禁 revive `MEO-T3`／`MEO-T4`。
7. 禁止觸碰：`archive/meo-v1/**`、存證目錄、`.claude/settings.json`、`.claude/settings.json.pre-decomm.bak`、`deliver/archive/**`、`reports/**`、`file/MEO/**`、`.claude/rules/**`、模板、`ticket-run`、`.agents/**`、`Pending/MEO-FIX-*` 等未追蹤孤兒檔、`PROGRESS.md` #26 Schedule 與其餘項目、`PROGRESS_ARCHIVE.md`、使用者層 memory、Advisor-first 相關欄位／hook／agent。保存性項目僅做 hash 比對。
8. 行為性測試之 `codex` 必須解析到 stub；若 `command -v codex` 不等於 stub 路徑則中止（避免誤打真實 Codex）。測試於 scratch git repo 進行，不得在 repo 工作樹內以 stub 覆寫 PATH 後直接跑。

## Error Handling
- AI-0 任一錨點不符 → 依三分流分類處置；`ALREADY_EXECUTED_PENDING_CLOSURE` → 停止回報（`MEO-T3`／`MEO-T4` 已為 VOID 屬預期，不計）
- ~~`void/` 目的檔同名、SHA 不一致、`registry_void_ticket`／`registry_upsert_ticket` 拒收、PENDING 查詢仍列兩票之各停止列~~ N/A — terminal state already completed（AI-1～AI-3 已完成）
- `cross_review.sh` 刪除後 `bash -n` 失敗、出現 mixed EOL，或 diff 非「2 刪 0 增」→ 停止回報，以 `git diff` 判定還原方案前不得再編輯
- `PROGRESS.md` 刪除後行數非 77 或出現其他項目變動 → 停止回報
- scope gate 因本次 cleanup 清單以外之 dirty 檔而 FAIL → 停止回報，由 Akira 處置該檔（禁 stash／restore／順手 commit）
- 行為性測試中 `codex` 未解析到 stub → 中止，不執行
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）。適用於 commit 標的：`cross_review.sh`、`PROGRESS.md`、`void/` 兩個目的檔

## Acceptance Criteria
- [x] **[存在性]** `.claude/skills`、`.claude/hooks`、`.claude/scripts`（若存在）內 `grep -rnE 'MEO_STATE_FILE|MEO_VERIFIER_REJECT_COUNT'` 與 `grep -rnE 'STATE\.md'` 命中皆為 0（僅鎖 MEO runtime 讀寫之 exact token；不以不分大小寫之 `meo` 計，因 `Timeout` 含 `meo`）（`MEO_STATE_FILE|MEO_VERIFIER_REJECT_COUNT` 0 命中、`STATE\.md` 0 命中；2026-10-09）
- [x] **[可達性]** 行為性測試入口為真實 `cross_review.sh`（production producer）；observer 為其 exit code、`.tmp/cross_review_state/<ticket>` 與 `STATE.md` 內容（路徑被走過之 trace 由下列三條 `[行為性]` AC 的實測 exit code 承擔）（以真實 `cross_review.sh`／`session_start.sh` 於 scratch repo 驅動，exit code 為 observer）
- [x] **[行為性]** given PATH 假 `codex`（解析到 stub 為前提）於 scratch git repo 重跑真實 `cross_review.sh`、無 `STATE.md`：expect 第 1、2 次同類 BLOCKER＝exit 1；第 3 次同類 BLOCKER＝exit 2（既有 fail-budget，`MAX_BLOCKER_BUDGET=3`）；REPORT_FIX＝exit 3；PASS＝exit 0。基線（AI-0）為 BLOCKER 三次皆 exit 2；移除前預演（scratch 副本）已得 1／1／2／3／0（實測：無 STATE.md 時 BLOCKER R1/R2/R3＝1/1/2、REPORT_FIX＝3、PASS＝0；`command -v codex` 解析到 stub；基線 2/2/2）
- [x] **[行為性]** given 同測試但 scratch 內 `STATE.md` 存在且含 `MEO_VERIFIER_REJECT_COUNT=2`：expect 跑完 BLOCKER 後 `STATE.md` 執行前後 SHA-256 相同（實測：`ed8d1427a7d62cb7` 前後相同；基線為被改寫）
- [x] **[行為性]** scratch 內以 repo 副本（修改後）`PROGRESS.md` 執行 `session_start.sh reset`：輸出不含字面「額度凍結」與「`MEO-T3` lifecycle 現為 PENDING」（容許反引號），且 `PROGRESS.md` ≤ 77 行（實測：`額度凍結` 0、`MEO-T3 lifecycle 現為 PENDING` 0、77 行；基線各 1／80 行）
- [x] **[存在性]** `registry_list(project=SmartCameraApp1, include_archive=true, ticket_id=…)` read-back：`MEO-T3`、`MEO-T4` `lifecycle=VOID`、`file=void/<basename>`、`void_reason` 非空且載明本票與退役事由；`ticket-run` 同等查詢 `registry_list(project=SmartCameraApp1, lifecycle="PENDING", fields=["id","title","file","lifecycle"], limit=50)` 不再列出兩票；`void/` 兩目的檔存在、`Pending/` 無兩票檔（terminal state，AI-1～AI-3 N/A）（read-back：兩票 VOID、`file=void/<basename>`、`void_reason` 非空；PENDING 查詢 7 筆不含兩票；`void/` 兩檔存在、`Pending/` 無兩票檔；audit_id `aud_20261009_b619933cca`、`aud_20261009_b9fe56cef6`）
- [x] **[存在性]** `.claude/PROGRESS.md` 為 77 行（≤ 77）；#17、#19、#24 三個字面皆 0 命中；#18、#20–#23、#25 起其餘項目與 #26 Schedule 位元組未變（77 行；三字面 0 命中；numstat 0/3 僅刪三行，其餘位元組未變）
- [x] **[存在性]** `cross_review.sh` 行尾全為 CRLF（`grep -c $'\r'` 等於 `wc -l`）、`bash -n` 通過、`git diff --stat` 恰為 2 deletions／0 insertions（CRLF 486/486、`bash -n` OK、numstat 0/2）
- [x] **[存在性]** 保存性：`git rev-parse HEAD:archive/meo-v1`＝`8fa65cd46371b6bb01753902e96e4bd2aed4df2d`（16 檔不變）；`.claude/settings.json.pre-decomm.bak` blob＝`e34091001ddc14836dc949f5d997424ff2d6726b`；存證目錄 18 檔且彙總 hash 前 16 碼＝`dc8068c423e4c35a`（`8fa65cd46371b6bb01753902e96e4bd2aed4df2d`、`e34091001ddc14836dc949f5d997424ff2d6726b`、存證 18 檔 `dc8068c423e4c35a`，皆與基線一致）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（含 `void/` 兩個新檔）；清單以外檔案不在本票 diff 與 staging（`git diff --cached --name-only` 恰為 4 個 ALLOWED 檔；`ssot-governance.md` 未 stage）

## 收尾實錄（2026-10-09）

- **結果**：碼閉環 commit `945478ea`（`[MEO-RESIDUAL-01]`，4 檔 +108／−5：`cross_review.sh` −2、`PROGRESS.md` −3、`void/` 兩票檔各 +54）。AC 9/9 已勾（`assembleDebug` 劃線 N/A，docs preset）。
- **驗證鏈**：`/qa` Step 0 PASS（commit 後復驗）→ `/sv` PASS → Codex Cross-Review **PASS**（`gpt-5.6-sol`／high，R1，`REACHABILITY: NONE`，結果檔 `.tmp/cross_review_result_20261009_184537.md`）→ `structural_verify.sh --post` PASS（A–G，ADVISORY 僅為 PROGRESS.md 77 行 > 60 之既有提示）。
- **行為性實測**（scratch git repo＋PATH 假 `codex`，`command -v codex` 解析到 stub 才執行）：無 `STATE.md` 時 BLOCKER R1/R2/R3＝1/1/2、REPORT_FIX＝3、PASS＝0（基線 2/2/2/3/0）；有 `STATE.md` 時 sha256 前後相同（基線被改寫）；`session_start.sh reset` 輸出「額度凍結」「`MEO-T3` lifecycle 現為 PENDING」命中 0（基線各 1），`PROGRESS.md` 77 行（基線 80）。保存性三項 hash 與 AI-0 基線一致。
- **如實揭露（偏離與延後）**：
  1. AI-1～AI-3（`MEO-T3`／`MEO-T4` 搬 `void/` 與冊面 VOID）由 Desktop 先行以 move（非 copy＋SHA 比對）完成，Rev C 標 `N/A — terminal state already completed`；冊面現值以 read-back 驗證。M-1 因此不適用。
  2. `preflight_activation.sh` 於編輯**之後**補跑（V1＝`BLOCKED_SCOPE_OR_TARGET_MISMATCH`，因 `cross_review.sh` blob 已被本票自身編輯改變；Template v3.11 資訊性分類，非 gate）。
  3. R0 預審（`cross-reviewer` subagent）未執行：`cross_review.sh` 無可插入暫停點；R0 無放行權，不影響 Codex 正式裁決。
  4. `/qa` Step 4（PROGRESS.md Dashboard 列）與 `/sv` Step 10（`PROGRESS_ARCHIVE.md` Done 標記）記 `SKIPPED — DEFERRED-TO-DOCSYNC`：AC 限 `PROGRESS.md` ≤77 行、HC-7 禁動其餘項目、`PROGRESS_ARCHIVE.md` 為 FORBIDDEN；同步債併入下一輪 `/compact`，不另開 DOCSYNC 票。
  5. RGR m-3（移除時序 AC 之 observer 點名 `audit_id`）未採納（terminal cleanup，不擴張 audit 契約）。
  6. 範圍外 dirty 檔 `.claude/rules/ssot-governance.md`（非本票產物）全程未 stage、未入 commit。
  7. 兩次 bash `cat`／heredoc stdin 卡住為 session 內工具操作失誤，已終止並改寫，未影響票面或 repo。
- **未開票之後續**：「VOID terminal fast-path」（Akira 2026-10-09 裁決：不寫入本票、不寫入 `.claude/rules/**`，待下游 Advisor-first／Stop Hook 票於其 hook 設定旁實作並以 `stop_hook_active` 防迴圈）。
- **下游**：本票為 Advisor-first／Stop Hook 票之 `execution_dependency`；其 provisional plan（`20261009-advstop-plan.md`）須重跑 Plan Gate。
