# ACAGOV-RUNTIME-TABLE-01: 於 ssot-governance.md 建立唯一 GO lane 轉移表並將 APP Git persistence 移至 /compact closeout

**簽發日期**: 2026-09-13
**簽發者**: Claude (SMTR)
**來源議題**: ACAGOV
**Mode**: normal → uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: INFRA-LIFECYCLE-SSOT-01（五段語義來源）、INFRA-COMPACT-CLOSURE-GATE-APP-01（Closure Gate 現況）、INFRA-CLOSURE-REGISTRY-GATE-01（冊面四要件）、INFRA-QA-PERTICKET-CONF-01（per-ticket conf／active.json 讀取優先序）
- `coordination_dependency`: 無

## 檔案治理/來源說明

本票由 2026-09-12～13 ACAGOV 議題三輪審查產出（外部規格書 `Smarter-Project/deliver/Autonomous_closed_Antiover/*.md` 之診斷採納、處方作廢）。Akira 2026-09-13 裁決採方案 C′ 與 C-1：唯一 GO lane 置於既有 `ssot-governance.md`，不新建 `ticket-runtime-contract.md`、不建獨立 UC Permit／放行表／Snapshot producer；closeout 四要件同回合原子性保留，僅 APP Git persistence 延至 `/compact closeout`。

**Rigor Gate 回應（2026-09-15，Akira 裁決：刻意合併，不拆票）**：`.tmp/rigor-gate/RGR_ACAGOV-RUNTIME-TABLE-01_20260915.md` F1（Overloaded Ticket）——GO-lane 集中化（AI-1／AI-4-7）與 Session Persistence 時序變更（AI-2／AI-3）為兩個獨立可分別驗收之失效機制，報告建議拆為二票。**裁決：維持單票**，理由：兩半皆源自同一輪 2026-09-13 ACAGOV 三輪審查裁決（方案 C′ 與 C-1 同批拍板），拆票僅增加治理開銷（雙 registry 條目、雙 rigor gate、雙 Cross-Review），對已於同一決策回合定案之內容收益有限。兩半之獨立驗收路徑已由該報告 §6 分組表記錄在案（分組 A＝AI-1/4-7＋AC 136-144,147-148；分組 B＝AI-2/3＋AC 139-140,149），**MUST** 據此分組表獨立追蹤兩半之驗收狀態，**MUST NOT** 因單票交付而將分組 A／B 之未達項互相掩蓋或混報。

## Verify Config
TICKET_ID=ACAGOV-RUNTIME-TABLE-01
CREATED=2026-09-13
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md,.claude/CLAUDE.md,.claude/skills/sv/SKILL.md,.claude/skills/uc/SKILL.md,.claude/skills/qa/SKILL.md
FORBIDDEN=.claude/skills/compact/SKILL.md,.claude/hooks/,.claude/scripts/,app/,build.gradle.kts,settings.gradle.kts
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-09-14 23:26
BASE_COMMIT=7ad7e8f
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**MAX_LINES=N/A 依據**：本票 AI 含 GO predicate、授權邊界與 fail-closed 判定類安全不變量，依 TPL v3.5 R3 MUST 標 `N/A`；失敗形狀枚舉為審查發現式，不可先驗估算。

**TARGET_BLOB=N/A 依據**：Desktop 開票端無合法 blob-hash producer（MCP 唯讀 git 原語不回傳 blob hash，`PowerShell` 依存取邊界恆禁），依 TPL Desktop fallback 正式化條款標 `N/A`。替代控制＝`BASE_COMMIT` ＋ AI-0 逐檔現值錨點。`N/A` **MUST NOT** 被解讀為豁免 AI-0、scope 或 evidence 驗證。

**`BASE_COMMIT`／`ANCHOR_PROBED` 之性質（可逆優先裁決，Akira 2026-09-14）**：二欄為**資訊性診斷值**，供定位與比對；**MUST NOT** 作為本票續行、QA、SV、Review 或收尾之門檻。目標檔於 `BASE_COMMIT..HEAD` 間之檔案級變動、他票 commit 前綴歸因、行號漂移與 `ANCHOR_PROBED` 時效，**均不得單獨阻止本票續作**——執行者一律以工作樹現值為準，需求仍存在即直接實作，已由他票等價完成即跳過該項，僅真實互斥語義或觸及 `app/` 之無歸因狀態才停止。

## Action Items

### AI-0: Runtime 載體與工具契約復驗（派工後、修改前）
- **檔案**: `.claude/rules/ssot-governance.md`、`.claude/skills/qa/SKILL.md`、`.claude/skills/uc/SKILL.md`、`.claude/skills/sv/SKILL.md`、`.claude/CLAUDE.md`、當前票檔 Header
- **動作**（逐項以 live `read_file_safe`／`grep_files`／registry 直讀取證，禁記憶、禁前次 session 輸出、禁執行者回報）：
  1. 依 `/qa` 現有設定讀取優先序（`tmp/verify/<TICKET_ID>.<REVISION>.conf` 優先，legacy `tmp/verify_cmd.conf` 為 fallback）實測取得本票 `TICKET_ID`／`ALLOWED`／`FORBIDDEN`／`DIFF_REF` 與 binding facts。**禁止假設 `tmp/verify_cmd.conf` 存在**（2026-09-13 實測：該檔於工作樹不存在，`NOT_FOUND`）。
  2. 逐字讀取當前 Ticket Header 之 `Traycer Workflow`，驗證其值屬封閉列舉（`N/A`／`architectural_review`／`execute_review`／`strict_verification`）。**Traycer 值之唯一 producer 為 Ticket Header 字面**，非 conf、非 registry、非記憶。
  3. 以既有工具 schema 驗證 `registry_set_checkpoint` 之參數分工：`checkpoint_data` 為 object（結構化欄）、`detail_content` 為 string（敘事長文）；並確認本專案 `registry_append_note` 不可作為敘事通道（回 `REGISTRY_CROSS_PROJECT_REF`）。
  4. 任取一張既已收束票，以 `registry_list` 讀其 `lifecycle` 與 `file` 欄並核對票檔實際位置，確認 `lifecycle ∈ {REVIEW, CLOSED}` ＋ 票檔位於 `deliver/` 時，既有 Closure Gate 之處理為「正常返回、不重複收束」。
  5. 逐字復驗 `/compact` 兩段 commit 路徑仍分離：①「當前票 artifacts」段之 `[<TICKET_ID>]` 單票前綴與禁止多票合併條文；②`## Auto Commit: Governance Artifacts` 段之表驅動 prefix、pathspec 逐檔處理與同 prefix 合併語義。二段邊界不存在或互相矛盾 → 停止回報；**MUST NOT** 自行修改 `/compact`、hook 或 prefix regex。
  6. 錨點字串復驗（**以現值為準，字面不符即依現值續作，不停工**）：`ssot-governance.md` 之 `## Ticket Closure 紀律` 與其「原子性」條（現值含「冊面異動走獨立 `[GOVERNANCE-SYNC]` commit」字面）各恰 1 次；`CLAUDE.md` Rule 4b 之「不基於 MEMORY.md / PROGRESS.md / TODO / FIXME 自主決定下一步」恰 1 次；`sv/SKILL.md` 之「準備提交 Traycer 終審」字面命中數；`uc/SKILL.md` 之「**收尾 SOP 觸發**」恰 1 次；`qa/SKILL.md` 之「## Step 4.5 完成後：Auto-Chain 銜接點」恰 1 次。
- **停止範圍（可逆優先）**：僅 AI-0.5（兩段 commit 路徑矛盾）、票檔／冊面現值互相矛盾、或需求互斥時停止回報。錨點字面過期、行號位移、他票曾改同檔**一律不停工**，改以現值續作並於收尾實錄載明差異。
- [x] 完成

### AI-1: ssot-governance.md 新增「Ticket Runtime Transition Table」節（唯一 GO lane）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 無此節（GO lane 目前分散於 `/uc` 規則 8、`/qa` Step 4.5 銜接點、`/sv` 報告末行敘事）
- **目標值**: 於 `## Ticket Closure 紀律` 之前新增 `## Ticket Runtime Transition Table`，內容 MUST 含下列五塊，且每條 MUST 回指本檔既有節次為法源：
  1. **控制詞封閉四值**：`MUST AUTO-CHAIN`／`MUST STOP`／`MUST REQUEST APPROVAL`／`MUST RETURN`。MUST NOT 以「→」、「準備」、「建議下一步」承擔執行語義。
  2. **不變量**（四條）：①對話模式不是 lifecycle——`/uc`／`/normal`／`/compact` 切換 MUST NOT 改變已啟動 Ticket 之 required next transition；②Agent MUST NOT 由 MEMORY／PROGRESS／TODO／FIXME／Pending 清單／歷史對話自行選取、開啟、排序或擴張另一張票；③當前票之下一步若由 Header／Verify Config／Action Items／AC／gate receipt 唯一決定，Agent MUST AUTO-CHAIN，此非新工作；④Agent 最高可寫 lifecycle 為 `REVIEW`，`CLOSED` 限審查方 SMTR/CSO（回指 §五段觸發與寫入者⑤）。
  3. **轉移表**（逐列含「已知事實／MUST 執行／MUST NOT」三欄），MUST 含 **RAPID MODE 分支**：RAPID MODE ON（QA/SV 已豁免）→ Ticket DONE 輸出當下即為 closeout 觸發點；RAPID MODE OFF → 觸發點為 SV PASS（Cross-Review 通過）。其餘列涵蓋：Action Items 未完成／Action Items 完成→QA／QA PASS→SV／QA FAIL／SV 或 Codex 非 PASS／SV＋Codex PASS 且 `Traycer Workflow: N/A`／SV＋Codex PASS 且 `≠ N/A`／已收束重入／Header 缺失或非法。
  4. **Traycer 語義**：`N/A` → 不詢問、不操作，寫 `reviews.traycer=N_A`；`architectural_review`／`execute_review`／`strict_verification` → MUST NOT 假設可由 agent／CLI／MCP 觸發封閉 VS Code plugin，寫 `reviews.traycer=PENDING` 並完成 Agent closeout 至 `REVIEW`（回指本檔既有 Traycer 條）；缺失或未知值 → `MUST STOP`，不寫猜測值、不轉態。**`PENDING` MUST NOT 被當作 blocker**。
  5. **最小冪等回傳**：`registry lifecycle ∈ {REVIEW, CLOSED}` **且**票檔實際位於 `deliver/` → `MUST RETURN`（已收束）；此時 MUST NOT 再呼叫 `registry_set_status`／`registry_upsert_ticket` 轉態、MUST NOT 再搬移或 unlink 票檔、MUST NOT 再寫等價 checkpoint、MUST NOT 建立空 commit。二者不一致 → 交既有 Closure Gate 判定，Agent MUST NOT 猜測修復。
- **本節 MUST NOT 定義新 lifecycle 值、新 approval／verdict／receipt 枚舉或新 registry schema。**
- [x] 完成

### AI-2: ssot-governance.md 新增「Session Persistence 邊界」節（四面向＋多票混合 commit）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 無此節
- **目標值**: 於 AI-1 新節之後新增 `## Session Persistence 邊界`，含：
  1. **可見性**：尚未持久化之收尾異動，以 `git status --short` 之 APP 工作樹 tracked dirty 集合，與 `registry_list` 讀得之 `lifecycle`／`file` 現值**共同觀測**；此二者即唯一觀測面，MUST NOT 另設狀態檔。
  2. **Artifact set 範圍**：含本 session 已收束票之 ①`Pending/` → `deliver/` 票檔移動、②`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 同步、③`plans/cso_briefing/project/TICKET_REGISTRY.json`。**registry 之雙面事實 MUST 明載**：其**合法寫入通道**為平台 API（`registry_set_status`／`registry_set_checkpoint` 等，**MUST NOT** 以檔案寫入直改），但其**檔案落點在 APP repo 內**且已列於 §Governance Artifact 清單（`[GOVERNANCE-SYNC]`），故仍由既有治理 artifact persistence 納入。**MUST NOT** 因「寫入通道在平台側」而將其排除於 APP artifact set 之外。
  3. **時效**：未持久化之 APP closeout artifact **MUST NOT 跨 session**；`/handoff`、主題切換或明確結束 session 前 MUST 執行 `/compact closeout`。
  4. **驗證**：`/compact closeout` 於 commit 前執行既有 Closure Gate 與 scoped `git_diff`（零意外變更）；任一失敗 → 禁止提交，MUST NOT 將未驗證之治理異動帶過 session 邊界。
  5. **Session Persistence 路徑指認（選路，非創設權限）**：本節所列 closeout artifact 之 APP Git persistence **MUST** 走 `/compact` 之 `## Auto Commit: Governance Artifacts` 路徑；其 commit prefix、pathspec、逐檔處理、同 prefix 合併與 zero-diff SKIPPED 語義，一律依 §Governance Artifact 清單與 `compact/SKILL.md` 既有規則執行。**MUST NOT** 使用「當前票 artifacts」段之 `[<TICKET_ID>]` 單票路徑；該段之多票合併禁令**不受本票修改**，亦 **MUST NOT** 擴義 `<TICKET_ID>` 佔位符值域。本條為既有機制之指認，**不新增任何提交授權**。
  6. **Governance Artifact 清單補列**：於本檔 §Governance Artifact 清單表新增一列 `plans/cso_briefing/project/deliver/*.md` → `[GOVERNANCE-SYNC]`（說明欄：已交付票檔，`REVIEW`／`CLOSED` 之物理歸位產物）。依該表節首「新增 Governance Artifact 只需在此表加一行，**MUST NOT** 另開票或單獨改 SKILL.md 判斷邏輯」之既有授權執行。**不補此列則票檔搬移將落在 `compact` Scope 盤點之表列外而永不提交，屬靜默失效。**
- [x] 完成

### AI-3: ssot-governance.md「原子性」條 C-1 窄改（commit 時點移轉，驗證義務保留）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: `## Ticket Closure 紀律` 之「原子性」條含字面「冊面異動走獨立 `[GOVERNANCE-SYNC]` commit，寫入後 **MUST** 以 scoped `git_diff` 驗證零意外變更」
- **目標值**: 僅改 commit 時點，**四要件同回合原子性字面一律不動**：改為「冊面異動之 APP Git persistence 依 §Session Persistence 邊界於 `/compact closeout` 邊界執行；scoped `git_diff` 零意外變更之驗證義務隨之移轉至該邊界，**MUST NOT 取消**」。
- **修訂波及面全檔掃描律**: MUST 以 grep 全檔清點「`[GOVERNANCE-SYNC]`」與「scoped `git_diff`」之**所有**實例（主段落＋註記＋條列），逐一確認語義一致；僅改本條而遺留他處舊述即視為未完成修訂。
- **MUST NOT** 擴充為三層敘事（C-2 已由 Akira 2026-09-13 明示不採）。
- [x] 完成

### AI-4: CLAUDE.md Rule 4b carve-out
- **檔案**: `.claude/CLAUDE.md`
- **現值**: `b) 不基於 MEMORY.md / PROGRESS.md / TODO / FIXME 自主決定下一步。`
- **目標值**: 同條之後追加一句：本條拘束「自行開啟新工作」，**不適用於**當前已啟動 Ticket 中由 Header／Verify Config／Action Items／AC／gate receipt 唯一決定之同票後續 transition；該類 transition 依 `.claude/rules/ssot-governance.md` §Ticket Runtime Transition Table 執行，**不因對話模式切換而失效**。
- [x] 完成

### AI-5: sv/SKILL.md 新增 Step 11 並將「準備」改為控制詞
- **檔案**: `.claude/skills/sv/SKILL.md`
- **現值**: 最末流程步驟為 `## Step 10.5: Session Note 清理`；回報格式含字面「→ 本地 SV + Cross-Review 通過，準備提交 Traycer 終審。」
- **目標值**:
  1. 新增 `## Step 11: Closeout Auto-Chain`——SV＋Codex PASS（或 RAPID MODE 豁免）後 **MUST AUTO-CHAIN**，依 `ssot-governance.md` §Ticket Runtime Transition Table 與 §Ticket Closure 紀律執行 Agent closeout 至 `REVIEW`。**本節 MUST 為引用，MUST NOT 複製四要件、GO predicate 或 Traycer 判定表**。
  2. 入口冪等：先 `registry_list` 讀現值，命中 §Table 之冪等條件即 `MUST RETURN`。
  3. 敘事寫入走 `registry_set_checkpoint`（`detail_content` 承載收尾實錄敘事、`checkpoint_data` 承載結構化欄），**MUST NOT** 用 `registry_append_note`；且 MUST 置於 `registry_set_status` **之後**（`set_status` 每次呼叫清空 checkpoint）。寫後 MUST read-back 驗證。
  4. 回報格式該行改為控制詞：`→ MUST AUTO-CHAIN: Step 11 Closeout`。
- [x] 完成

### AI-6: uc/SKILL.md 收尾三子項搬離＋session-local Snapshot
- **檔案**: `.claude/skills/uc/SKILL.md`
- **現值**: 規則 8 含「**收尾 SOP 觸發**」及其三子項（AC/AI checkbox 勾選／registry lifecycle sync／`registry_append_note`）
- **目標值**:
  1. 刪除三子項，改為單行引用：收尾由 `/sv` Step 11 承載，判準依 `ssot-governance.md` §Ticket Runtime Transition Table；**模式切換不中斷 Ticket lifecycle**。
  2. Phase 0 activation 成功後輸出 **session-local Snapshot** 區塊（固定欄位：`Ticket`／`Allowed`／`Forbidden`／`Traycer workflow`／`Agent terminal lifecycle: REVIEW`／`Auto-chain`／`Stop only`）。欄位 producer 限定：verification facts 依 `/qa` 既有讀取優先序取得；`Traycer workflow` 唯一取自 Ticket Header 字面。
  3. Snapshot **MUST 為 session-local 輸出**，MUST NOT 寫成持久檔、MUST NOT 新增 script／hook producer、MUST NOT 成為第二份流程規則。Header／`ALLOWED`／`FORBIDDEN`／必要 gate 任一不完整 → 禁止進入 auto-chain，`MUST STOP` 並列出缺少之具體欄位。
  4. **legacy config 讀取修正**：將本檔「`tmp/verify_cmd.conf` 不存在或 `TICKET_ID` 為空即 STOP」改為——依 `/qa` 現行 verification-config 讀取優先序解析當前 Ticket binding；僅當**所有**既有候選載體均不存在、`TICKET_ID` 空值、各載體 Ticket ID 不一致，或票檔 Header 無法解析時，才 `MUST STOP` 並沿用 `⛔ UC Auto-Chain STOPPED` 格式。此為修正執行路徑上之過時檔名，**非**建立新 Snapshot producer。
- **`registry_append_note` 於本檔收尾語境之引用 MUST 一併移除**（該原語於本隔離專案回 `REGISTRY_CROSS_PROJECT_REF`）。
- **不重做既有能力**：`INFRA-CTAWR-PHASE0-02` 已落地之 activation autostart（Phase 0 成功後 MUST 立即自動接續、不得停下等待使用者）為既有能力，**MUST NOT** 重寫、重述或另立等價條文。
- [x] 完成

### AI-7: qa/SKILL.md 銜接點改為引用
- **檔案**: `.claude/skills/qa/SKILL.md`
- **現值**: `## Step 4.5 完成後：Auto-Chain 銜接點` 節自持 QA→SV 路由敘事
- **目標值**: **僅改一行**——銜接敘事改為引用 `ssot-governance.md` §Ticket Runtime Transition Table 決定 next transition。QA 本體、Step 順序、停止條件、Scope Gate 與 auto-commit 邏輯**一律不動**；**MUST NOT** 於本票重構 QA 實作邏輯，**MUST NOT** 於本檔自持 GO predicate 副本。
- [x] 完成

## Hard Constraints
1. MUST NOT 新增 Skill、hook、daemon、watcher、registry schema、第二套 lifecycle／approval／verdict／receipt，或任何 runtime contract 文件（含 `ticket-runtime-contract.md`）。
2. Ticket Runtime Transition Table **只能**置於 `ssot-governance.md`；`/uc`、`/qa`、`/sv` **只能引用**，MUST NOT 各自複製 GO predicate。
3. `/uc` Snapshot 為 session-local 輸出，MUST NOT 寫成新持久檔、新 script producer 或另一份流程規則。
4. Snapshot 之 `Traycer Workflow` 唯一來源為 Ticket Header；verification facts MUST 依 `/qa` 既有讀取優先序取得，MUST NOT 硬編碼 legacy `tmp/verify_cmd.conf`。
5. Agent MUST NOT 操作封閉 Traycer plugin；非 `N/A` 票只寫既有 `reviews.traycer=PENDING`，Agent lifecycle 上限為 `REVIEW`。
6. MUST NOT 修改 Android production code、build 設定、hooks、activation／preflight scripts、registry schema、歷史 ticket artifacts 或 mcp-ticket-platform。
7. 採 session-level commit 後，closeout 四要件**仍 MUST 同回合完成**；只延後 APP Git persistence 至 `/compact closeout`，MUST NOT 延後 lifecycle 轉態、票檔搬移、registry 更新或 evidence 回填。
8. `/compact` 本體、commit-msg hook、驗證腳本（`verify_checklist.sh`／`lib_verify.sh`／`structural_verify.sh`）、activation／preflight scripts 與 registry schema **一律不動**。closeout artifact 依 §Session Persistence 之路徑指認走既有 `Auto Commit: Governance Artifacts`，**MUST NOT** 擴義 `<TICKET_ID>` 佔位符值域、**MUST NOT** 改寫任何 commit message 模板或 prefix regex。
9. **scope-diff 觀測與 per-ticket 程式碼 auto-commit 不在本票拆除範圍**：`git diff --name-only` 對 `ALLOWED` 之範圍觀測為 Hard Stop 1（scope）之機械 producer，`/qa` 之 per-ticket code auto-commit 為可逆性之存檔點，二者 **MUST NOT** 因「Git 去權化」被移除或降級。本票拆除者限**歷史閘**（`BASE_COMMIT` 漂移、`ANCHOR_PROBED` 時效、commit prefix 歸因、`CANDIDATE_SUPERSEDED`、`TARGET_BLOB` 缺失）。

## Error Handling
- AI-0 任一錨點命中數與票面不符 → 依 TPL v3.8 三分流分類停止回報（`ANCHOR_STALE_SCOPE_INTACT`／`ALREADY_EXECUTED_PENDING_CLOSURE`／`CANDIDATE_SUPERSEDED`／`BLOCKED_UNATTRIBUTED_STATE`）
- `registry_append_note` 於本專案回 `REGISTRY_CROSS_PROJECT_REF` 以外之非預期結果 → 停止回報
- AI-3 全檔掃描發現 `[GOVERNANCE-SYNC]`／scoped `git_diff` 之他處實例與新語義衝突 → 停止回報，禁自行擴大修訂
- 新節與既有條文出現 lifecycle 語義競爭 → 停止回報（`⚠️ SSOT 衝突`），禁自行裁剪既有條文
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** `ssot-governance.md` 含 `## Ticket Runtime Transition Table` 節，且該節明載其不創建第二套 lifecycle／verdict／approval／receipt／registry schema
- [x] **[存在性]** 該表含 RAPID MODE ON／OFF 二分支列（grep `RAPID MODE` 於該節命中 ≥1）
- [x] **[存在性]** 該節含四條不變量與最小冪等回傳條件，且每條回指本檔既有節次為法源
- [x] **[存在性]** `ssot-governance.md` 含 `## Session Persistence 邊界` 節，明載 registry 之雙面事實（寫入通道＝平台 API；檔案落點＝APP repo 且屬 Governance Artifact）
- [x] **[存在性]** §Governance Artifact 清單表已含 `plans/cso_briefing/project/deliver/*.md` → `[GOVERNANCE-SYNC]` 一列
- [x] **[存在性]** 票面無任何「多票 Ticket ID 列舉」「`<TICKET_ID>` 值域擴義」字面；負向 grep 命中為 0
- [x] **[存在性]** `CLAUDE.md` Rule 4b 明確區分「自行開啟新工作」與「當前票已唯一決定之 transition」
- [x] **[存在性]** `uc/SKILL.md` 不再持有收尾三子項；grep `registry_append_note` 於該檔收尾語境命中為 0
- [x] **[存在性]** `qa`／`sv`／`uc` 三檔均無自持之 GO predicate 副本——負向判準：於三檔 grep `Traycer Workflow` 之 N/A→`N_A`／非 N/A→`PENDING` 對照敘事、以及 `lifecycle` 終態宣告，命中數為 0（`sv` Step 11 之單行引用不計）；正向判準：三檔各至少 1 處字面引用 `Ticket Runtime Transition Table`
- [ ] **[行為性]** 測試票 A（真實低風險 docs／governance 票，`Traycer Workflow: N/A`，路徑 `/normal → /uc → /normal`）：given Action Items 完成且證據齊備，expect 自動 QA → SV → Agent closeout → `REVIEW`，全程無須人類輸入 `/qa`、`/sv`、歸檔或同步命令，且 `reviews.traycer=N_A`
- [ ] **[行為性]** 測試票 B（同上模式切換，`Traycer Workflow ≠ N/A`）：given SV＋Codex PASS，expect Agent 未嘗試操作 Traycer、寫入 `reviews.traycer=PENDING`、收束至 `REVIEW`，且 `PENDING` 未被當作 blocker 停車
- [ ] **[行為性]** 模式切換不變性：given 測試票 A 或 B 於執行中切換 `/normal ↔ /uc`，expect next transition、required gate 與收束權限均不改變
- [ ] **[行為性]** 重跑冪等：given 測試票 A 或 B 已達 `REVIEW`／`CLOSED` 且票檔位於 `deliver/`，when 重進收束，expect 正常返回且無 status 重寫、無票檔二次搬移、無等價 checkpoint、無空 commit
- [ ] **[行為性]** Compact 邊界：given 同一 session 完成測試票 A 與 B 後執行 `/compact closeout`，expect 票檔（`deliver/*.md`）、PROGRESS 類與 `TICKET_REGISTRY.json` 均經 `Auto Commit: Governance Artifacts` 表驅動路徑逐檔提交，Closure Gate 與 scoped `git_diff` PASS，且未使用 `[<TICKET_ID>]` 單票路徑
- [ ] **[行為性]** Git 歷史非 gate：given 本票任一 ALLOWED 檔於 `BASE_COMMIT..HEAD` 間已被他票 commit 改動，expect 執行者讀現值後直接續作，未因檔案級漂移、他票前綴歸因或 `ANCHOR_PROBED` 過期而停工
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

## 收尾義務
- 本票自身之收尾 MUST 依**修訂後**之 §Ticket Runtime Transition Table 與 §Session Persistence 邊界執行（自我適用），作為第三個行為性驗證樣本。
- 冊面四要件（lifecycle／reviews 四欄／`file` 欄／收尾實錄）MUST 同回合完成；APP Git persistence 依新制於 `/compact closeout` 邊界執行。
