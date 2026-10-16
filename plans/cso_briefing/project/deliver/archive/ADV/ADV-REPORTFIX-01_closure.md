# ADV-REPORTFIX-01: SV report preflight 前移 Check D，並使 REPORT_FIX 在 report-only 條件下不耗 BLOCKER 預算且可有界自動閉環

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T01`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261010-reportfix-plan.md`（invocation 之 Source 字串因反斜線遺失而變形，經核對 `.tmp\ticket-author\` 下僅此一份 `reportfix-plan`，指認唯一；`.tmp` 為短期傳遞，本票自足，不依賴該檔留存）；Parent 狀態＝`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`／`Fixture: false`；Parent 讀取時刻＝2026-10-10 18:0x（HEAD `10b16ed8`）。上游診斷＝`.tmp/ticket-author/20261010-reportfix-diag.md`（`Gate: PASS`／`Verdict: PARTIALLY_CONFIRMED`）。裁決＝Akira／Desktop 2026-10-10：report-only 閉環七條件、state／budget 行為、preflight 邊界、契約衝突裁決（必須一併修正 `sv/SKILL.md`、`rules/cross-review.md`、`rules/ssot-governance.md`、`uc/SKILL.md`）、同一 REPORT_FIX incident 最多自動閉環 2 次、`qa/SKILL.md` 納入 ALLOWED、授權隔離重現；以及 ticket 階段追加裁決：`CHECKPOINT_REPORT_FIX_LIMIT` 改為 **exit 2**（見 HC-6／HC-7）。議題線＝Advisor（Akira 2026-10-10 指定：票號前綴 `ADV`、來源議題 `Advisor`、registry topic `Advisor`；plan 草案原擬 `INFRA`，以本指定為準）。

**External-review decision**: WAIVED — this bounded, reversible REPORT_FIX closure and SV-report preflight change is supported by local first-party source evidence and a completed red-to-green isolated fixture; no unresolved product or authority ambiguity remains.（Trigger: 6a、6b②）

> **證據分級**：①**已實證**（2026-10-10，隔離環境：scratch fake repo＋fake `codex` stub，腳本自真 repo 唯讀複製）：現況 `REPORT_FIX→REPORT_FIX→BLOCKER`＝**3→3→2**（Codex 來源與 structural 來源皆然）、`BLOCKER×3`＝1→1→2、`REPORT_FIX×3`＝3→3→3（無上限）、缺 `## Stage B` 的報告通過 pre-check（exit 0 且 Codex 被呼叫）而 `--post` 才報 `[D]`。②**目標狀態可行性已探針驗證**（同為 scratch：僅對**複製品**套用本票 AI-1／AI-2／AI-3 的目標代碼，repo 未被修改）：`REPORT_FIX→REPORT_FIX→BLOCKER`＝3→3→1；structural 來源 `REPORT_FIX×3`＝3→3→**2**（第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`，`REPORT_FIX_COUNT` 1→2→2，Codex 呼叫 0 次）；`BLOCKER×3`＝1→1→2；混合 `BLOCKER,REPORT_FIX,BLOCKER,BLOCKER`＝1→3→1→2；舊格式 state 相容；closure 守門對「tracked 變更」「`.tmp/` 外 untracked」exit 2、對「僅 `.tmp/` 變更」放行。③**待本票實作驗證**：上列於 repo 內實際變更後以票內 fixture 轉綠；AI-0 須在票的基線重跑 ① 並保存輸出。
> **已知限制（如實揭露）**：closure 守門只能偵測 `.tmp/` 以外之工作樹變更，**無法偵測 report 內捏造新事實**（仍為 agent 義務，七條件之⑤）；無關之既有 dirty／untracked 檔會造成保守誤停（停下升報，不會誤放行）；v1 僅對 `REPORT_FIX_SOURCE=structural_preflight` 自動閉環，Codex 來源一律 `REPORT_FIX_ESCALATE`；LIMIT 後的「機械式拒跑」需 state 區分第 2 次後與第 3 次後，超出固定 schema，列為 deferred。closure 守門亦不偵測 gitignored 路徑（如 `tmp/verify_cmd.conf`）與 `.tmp/` 內他檔（含 `.tmp/cross_review_state/<ticket>`，故 `REPORT_FIX_COUNT` 之完整性亦屬 agent 紀律）；`.agents/skills/**` 為未追蹤鏡像、與 `.claude/skills/**` 既有漂移，本票不同步。`.claude/CLAUDE.md` Rule 4c 載「/qa /sv 發現問題→回報，不自行修復」——本票將「修 `.tmp/sv_report.md`（SV 自身產物）」視為非實作；該檔列 FORBIDDEN，若 Akira 認為 4c 措辭須同步，另票處理。

**簽發日期**: 2026-10-10
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無（判準來自 Akira／Desktop 2026-10-10 裁決與 Parent plan；基線以 `BASE_COMMIT`／`TARGET_BLOB` 固定，AC／HC／AI-0 錨點皆為 live 直查，不引用任何其他票之產物）
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=ADV-REPORTFIX-01
CREATED=2026-10-10
PRESET=docs
ALLOWED=.claude/skills/sv/scripts/cross_review.sh,.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/qa/SKILL.md,.claude/skills/sv/SKILL.md,.claude/skills/sv/CROSS_REVIEW.md,.claude/rules/cross-review.md,.claude/rules/ssot-governance.md,.claude/skills/uc/SKILL.md,.claude/skills/qa/tests/test_report_fix_closure.sh
FORBIDDEN=.claude/hooks/**,.claude/settings.json,.claude/settings.local.json,.claude/agents/**,.claude/skills/ticket-author/**,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,.claude/CLAUDE.md,.claude/skills/qa/scripts/lib_verify.sh,.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/qa/scripts/preflight_activation.sh,archive/meo-v1/**,app/**,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-10 18:08
BASE_COMMIT=10b16ed8
TARGET_BLOB=.claude/skills/sv/scripts/cross_review.sh:c5c81377;.claude/skills/qa/scripts/structural_verify.sh:db439499;.claude/skills/qa/SKILL.md:7c68043d;.claude/skills/sv/SKILL.md:aaa77453;.claude/skills/sv/CROSS_REVIEW.md:c5a411a9;.claude/rules/cross-review.md:20915aff;.claude/rules/ssot-governance.md:2e4bdef8;.claude/skills/uc/SKILL.md:dc0f93af
INTRODUCED_CAPABILITY=feature:report-fix-closure
DEPENDENCY_DELTA=NONE

> PRESET=docs（混合性質：腳本＋治理文本＋shell fixture；零 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。MAX_LINES＝`N/A`：本票含 fail-closed／守門類不變量，失敗形狀枚舉為審查發現式，不可先驗估算（Template v3.5 R3）。`TARGET_BLOB` 不列新增檔（`test_report_fix_closure.sh` 現不存在）。FORBIDDEN 含 `/` 的 token 在 `verify_checklist.sh` 為字面比對（已知缺陷，見 memory `verify-checklist-glob-token-bug`），**不構成機械閘**；實際執法＝AC「`git diff --name-only` ⊆ ALLOWED」＋下列 Hard Constraints。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）＋隔離 baseline 紅燈
- **檔案**: 上列 ALLOWED 既有 8 檔
- **動作**: 逐字確認（行號僅供定位；2026-10-10 18:08 live 取樣，HEAD `10b16ed8`，工作樹 clean）：
  - `cross_review.sh`（blob `c5c81377`；全檔 CRLF：`git ls-files --eol` 顯示 `w/crlf`，位元組計數 `\r\n`＝`\n`＝486；**`grep -c $'\r'` 在本工具環境不可靠〔LF 檔亦回傳全行數、CRLF 檔曾回傳 0〕，不得採用**）：L14 `#   2 — ENV 錯誤 或 FAIL BUDGET EXHAUSTED (依 MAX_BLOCKER_BUDGET)`；L15 `#   3 — REPORT_FIX（sv_report.md 問題，修報告後重跑）`；L154–159 `CURRENT_ROUND=1`／`if [[ -f "$STATE_FILE" ]]; then`／`  _PREV="$(grep "^ROUND=" "$STATE_FILE" 2>/dev/null | head -1 | cut -d'=' -f2- || echo 0)"`／`  CURRENT_ROUND=$(( _PREV + 1 ))`／`fi`／`echo "📊 Round: R${CURRENT_ROUND} | Ticket: ${TICKET_ID}"`；L185 `    printf 'TICKET=%s\nROUND=%s\nLAST=REPORT_FIX\n' "$TICKET_ID" "$CURRENT_ROUND" > "$STATE_FILE"`（其後 L186 `    fail_report_fix`）；L344 `CONF_ALLOWED=$(grep "^ALLOWED=" "tmp/verify_cmd.conf" | head -1 | cut -d'=' -f2-)`；L448 `printf 'TICKET=%s\nROUND=%s\nLAST=%s\n' "$TICKET_ID" "$CURRENT_ROUND" "${VERDICT:-UNKNOWN}" > "$STATE_FILE"`；L453 `  if [[ $CURRENT_ROUND -ge $MAX_BLOCKER_BUDGET ]]; then`；L462 `# BLOCKER/REPORT_FIX must KEEP the state file so ROUND keeps accumulating across reruns.`；L479 `  REPORT_FIX) echo; fail_report_fix ;;`、L480 `  BLOCKER)    echo; fail_blocker ;;`。
  - `structural_verify.sh`（blob `db439499`，LF）：L7 `#   --post: 啟用 post mode，額外執行 Check D/E/F（/qa Step 5 使用）`；L8 `#           pre-check 模式（cross_review.sh 呼叫，不帶 --post）只跑 A/B/C`；L14 `#   D. sv_report 五段結構完整性 → REPORT_FIX (缺必要段落)           [post only]`；L208–211 `if [[ "$POST_MODE" != "true" ]]; then`／`  archive_report "$SV_REPORT" || fail_env "Cannot archive sv_report to tmp/"`／`  exit 0`／`fi`；L213–228 Check D 區塊（L217 `for section in "## Files Changed" "## Implementation Scope" "## Stage A" "## Stage B" "## Stage C"; do`）。
  - `sv/SKILL.md`（blob `aaa77453`）：L99 `9. **SV Report 輸出**：…`、L102 `` - `## Files Changed (payload)` — … ``、L104 `` - `## Implementation Scope` — … ``、L111 `` - **MUST** 禁止只列 ALLOWED 而省略 payload 其他檔案，否則 `structural_verify.sh` Check B FAIL ``、L156 `` - `exit 3` (REPORT_FIX) → 📋 修 .tmp/sv_report.md 後重新執行 cross_review（不需重跑 /qa） ``、L157 `` - `exit 2` (ENV / FAIL BUDGET) → ⛔ 人工介入，檢查 `.tmp/cross_review_state/<ticket>` ``、L158–160 Fail Budget 三行、L171 `` - **機械輪次**：`.tmp/cross_review_state/<TICKET_ID>` 之 `CURRENT_ROUND`（腳本自增，producer 明確） ``。**`## Stage A`／`## Stage B`／`## Stage C` 字面於 `sv/**`、`qa/SKILL.md`、`rules/**` 命中數＝0**。
  - `CROSS_REVIEW.md`（blob `c5a411a9`）：L63 `` | `REPORT_FIX` | 3 | `.tmp/sv_report.md` 或 payload disclosure 不可信 | 修報告後重跑 cross-review，不需重跑 /qa | ``、L64 `| ENV / FAIL BUDGET | 2 | codex、Git、輸入、或 fail budget 錯誤 | 人工介入 |`、L76 `` 內容包含 `TICKET`、`ROUND`、`LAST`。`MAX_BLOCKER_BUDGET` 從 `.claude/verify-policy.env` 讀取，預設為 `3`。 ``、L80 `` - `REPORT_FIX` 不扣 blocker budget，但會阻擋流程直到報告修正 ``。
  - `rules/cross-review.md`（blob `20915aff`）：L13 `- 修正涉及：PROGRESS.md / sv_report*.md / ssot_context.md / CLAUDE.md / SSOT*.md / *_Contract*.md`、L15 `- 第 2 次 FAIL（同票任何維度）`、L17 `**次數限制**：第 1 次 FAIL 純 .kt fix **MAY** inline，完成後 **MUST** 立即回報等 ack。第 2 次 → **MUST** 停。`、L27 `**退出機制**：無 Akira ack **MUST** 不得連續執行 2 次以上 cross_review.sh。`
  - `rules/ssot-governance.md`（blob `2e4bdef8`）：L126 前言含 `**MUST NOT** 各自複製 GO predicate 副本`；L154 `` | SV 或 Codex 非 PASS（BLOCKER／REPORT_FIX） | `MUST STOP`，依 SV／Cross-Review 既有契約處理 | 由 UC 自動重跑或改判（回指 `uc/SKILL.md` 規則 9） | ``。
  - `uc/SKILL.md`（blob `dc0f93af`）：L58 `` - SV 的 PASS／ADVISORY／REPORT_FIX／BLOCKER／ENV ERROR 依 SV Skill 契約處理，**MUST NOT** 由 UC 自動重跑或改判。 ``
  - `qa/SKILL.md`（blob `7c68043d`）：L245 `` `--post` 模式執行 A/B/C/D/E/F（D: sv_report 五段結構 / E: cross_review PASS / F: PROGRESS.md 格式）。 ``；L246 ``不帶 `--post`（cross_review.sh pre-check）只跑 A/B/C，不讀 `.tmp/cross_review_state`。``
  - 既有 exit 語意（HC-6 基線）：`cross_review.sh` 表頭 L11–15 與 `sv/SKILL.md` L154–157 一致——exit 0＝PASS／ADVISORY、**1＝BLOCKER（`fail_blocker`）**、**2＝ENV／FAIL BUDGET＝人工介入**、**3＝REPORT_FIX**。`CHECKPOINT_REPORT_FIX_LIMIT` 現不存在於任何檔案（本票新增之 target state，非 current-state 衝突）。
  - **隔離 baseline 紅燈**（scratch fake git repo＋fake `codex` stub；`command -v codex` 必須解析到 stub 否則中止；禁寫真 repo／真 `.tmp`／真 worktree；fake repo 須含 `.gitignore`〔`tmp/`、`.tmp/`、`bin/` 與輸出檔〕並 commit 腳本副本，且 `tmp/verify_cmd.conf` 須含 `ALLOWED=`——缺 `ALLOWED=` 時 L344 於 `set -euo pipefail` 下靜默 exit 1）：重跑並保存輸出 ①Codex 來源 `REPORT_FIX→REPORT_FIX→BLOCKER`＝3／3／2（state `ROUND` 1→2→3）；②structural 來源（ghost 報告×2 後 BLOCKER）＝3／3／2、Codex 呼叫 0／0／1；③`BLOCKER×3`＝1／1／2；④`REPORT_FIX×3`＝3／3／3；⑤缺 `## Stage B` 的報告通過 pre-check（exit 0 且 Codex 被呼叫），`--post` 才報 `[D]`。**任一紅燈無法重現 → 停止回報**（推論被推翻）。
- **任一錨點不符 → 依 TPL v3.11 基線漂移三分流處置（本票僅觸及 `.claude/` 可逆治理面，`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 記錄後以現值續作；行號漂移、措辭變動或 `CHECKPOINT_REPORT_FIX_LIMIT` 尚不存在皆屬資訊性，只重讀補證；`ALREADY_EXECUTED_PENDING_CLOSURE` 則停止）。**
- [x] 完成（依據：八檔 blob 與 TARGET_BLOB 一致；隔離 baseline 紅燈全數重現（fixture 對未改腳本 24 FAIL：F2 3/3/2 且 ROUND=3、F4／F4b 3/3/3、F1 exit 0 且 Codex 1 次、F3 1/1/2、F8 1/3/2/2；ad hoc 結構來源 ghost×2→Codex BLOCKER＝3/3/2、Codex 呼叫 0/0/1））

### AI-1: cross_review.sh — REPORT_FIX 不耗 ROUND；新增 REPORT_FIX_COUNT、來源標記與 LIMIT（exit 2）
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`
- **現值**: AI-0 所列 L14、L154–159、L185–186、L448、L462
- **目標值**（逐字；target 行為已於 scratch 複製品探針驗證，見「證據分級」②）:
  - L14 改為 `#   2 — ENV 錯誤 或 FAIL BUDGET EXHAUSTED (依 MAX_BLOCKER_BUDGET)；或 REPORT_FIX 閉環上限（CHECKPOINT_REPORT_FIX_LIMIT）／closure 守門拒絕（REPORT_FIX_ESCALATE）— 人工介入`（L11–13、L15 不變）。
  - L154–159 改為（`_PREV_RF` 為既有計數；舊格式 state 無此欄＝0，向後相容）：
    ```bash
    CURRENT_ROUND=1
    _PREV_RF=0
    if [[ -f "$STATE_FILE" ]]; then
      _PREV="$(grep "^ROUND=" "$STATE_FILE" 2>/dev/null | head -1 | cut -d'=' -f2- || echo 0)"
      CURRENT_ROUND=$(( _PREV + 1 ))
      _PREV_RF="$(grep "^REPORT_FIX_COUNT=" "$STATE_FILE" 2>/dev/null | head -1 | cut -d'=' -f2- || echo 0)"
      [[ "$_PREV_RF" =~ ^[0-9]+$ ]] || _PREV_RF=0
    fi
    echo "📊 Round: R${CURRENT_ROUND} | Ticket: ${TICKET_ID}"
    ```
  - 於其後新增函式（`$1`＝REPORT_FIX 來源 `structural_preflight`｜`codex`；`ROUND` 寫回上一輪值＝不遞增；既有計數 `>=2` 即第 3 次 → LIMIT，**exit 2**；`<2` 才授予一次閉環並 `COUNT+1`，函式返回後由既有 `fail_report_fix` 路徑〔P1 為其後緊接的呼叫、P2 為 `case "$VERDICT"` 之 REPORT_FIX 分支〕以 exit 3 結束）:
    ```bash
    record_report_fix() {
      local _rf="$_PREV_RF" _limit=0
      if [[ "$_rf" -ge 2 ]]; then _limit=1; else _rf=$(( _rf + 1 )); fi
      printf 'TICKET=%s\nROUND=%s\nREPORT_FIX_COUNT=%s\nLAST=REPORT_FIX\n' "$TICKET_ID" "$(( CURRENT_ROUND - 1 ))" "$_rf" > "$STATE_FILE"
      echo "REPORT_FIX_SOURCE=$1"
      if [[ $_limit -eq 1 ]]; then
        echo "CHECKPOINT_REPORT_FIX_LIMIT"
        echo "⛔ 同票同一 state 週期（PASS／ADVISORY 清 state 前）已自動閉環 2 次 — 人工裁決 checkpoint（非 BLOCKER）；不得再自動重跑。"
        exit 2
      fi
    }
    ```
  - L185 改為 `    record_report_fix structural_preflight`（保留其後 L186 `    fail_report_fix`）。
  - L448 改為：
    ```bash
    if [[ "$VERDICT" == "REPORT_FIX" ]]; then
      record_report_fix codex
    else
      printf 'TICKET=%s\nROUND=%s\nREPORT_FIX_COUNT=%s\nLAST=%s\n' "$TICKET_ID" "$CURRENT_ROUND" "$_PREV_RF" "${VERDICT:-UNKNOWN}" > "$STATE_FILE"
    fi
    ```
  - L462 註解改為 `# BLOCKER/REPORT_FIX must KEEP the state file so ROUND (blocker rounds) and REPORT_FIX_COUNT keep accumulating across reruns.`
  - **不改**：預算判斷區塊（L451–459，含 `fail_budget`／R(N-1) checkpoint 文字）、`case "$VERDICT"` 全部分支（L476–486）、`fail_blocker`／`fail_budget`／`fail_report_fix` 定義；不新增 exit code（僅 0／1／2／3 四值）。保持工作樹 CRLF；`bash -n` 通過。
- [x] 完成（依據：cross_review.sh 套用 AI-1（record_report_fix／_PREV_RF／L14／L448／L462），fixture F1–F4b／F7／F8 全綠；BLOCKER 路徑 git diff -U0 無變動行）

### AI-2: cross_review.sh — closure 模式無狀態守門（REPORT_FIX_ESCALATE，exit 2）
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`
- **現值**: AI-1 新增函式之後、`## Parse ## Excluded Payload` 區段之前無 closure 檢查
- **目標值**（逐字；不讀寫 state、不屬 preflight）:
  ```bash
  if [[ "${REPORT_FIX_CLOSURE:-0}" == "1" ]]; then
    if [[ -n "$(git status --porcelain -- . ':!.tmp' 2>/dev/null)" ]]; then
      echo "REPORT_FIX_ESCALATE"
      echo "   closure 模式：.tmp/ 以外偵測到工作樹變更，report-only 條件不成立 → 停止自動閉環，等待人工裁決。"
      exit 2
    fi
  fi
  ```
  CRLF 保持。
- [x] 完成（依據：closure 守門已加入 cross_review.sh，fixture F5 全綠（tracked／untracked exit 2＋ESCALATE＋state 不變＋Codex 0；對照組 exit 0））

### AI-3: structural_verify.sh — Check D 併入 preflight（pre 模式）
- **檔案**: `.claude/skills/qa/scripts/structural_verify.sh`
- **現值**: AI-0 所列 L7、L8、L14、L208–211、L213–228
- **目標值**: ①將 Check D 區塊（L213–228，內容不變，僅註解標題改為 `# ─── Check D: sv_report 五段結構（preflight 與 --post 共用）───`）移到 `if [[ "$POST_MODE" != "true" ]]` 之前；②該 `if` 區塊（置於 Check D 之後、Check E 之前）改為：
  ```bash
  if [[ "$POST_MODE" != "true" ]]; then
    if [[ "$_CURRENT_VERDICT" == "REPORT_FIX" || "$_CURRENT_VERDICT" == "BLOCKER" ]]; then
      echo "   修正方式：補齊 .tmp/sv_report.md 缺少之必要標題。"
      archive_report "$SV_REPORT" || fail_env "Cannot archive sv_report to tmp/"
      exit 1
    fi
    archive_report "$SV_REPORT" || fail_env "Cannot archive sv_report to tmp/"
    exit 0
  fi
  ```
  ③表頭 L7 改為 `額外執行 Check E/F/G…（/qa Step 5 使用）`、L8 改為 `pre-check 模式（SV_REPORT_PREFLIGHT；cross_review.sh 呼叫，不帶 --post）只跑 A/B/C/D`、L14 標註改為 `[pre + post]`。post 模式流程不變（D 只執行一次）。不新增 flag；不動 Check E／F／G／GH-*。
- [x] 完成（依據：structural_verify.sh Check D 前移至 preflight＋表頭 L7／L8／L14，fixture F6 全綠（pre exit 0 無 E/F/G、無 fail_budget_state；--post exit 1 被 [E] 擋、[D] 僅 1 次））

### AI-4: qa/SKILL.md — pre-check 描述同步
- **檔案**: `.claude/skills/qa/SKILL.md`
- **現值**: L245–246（見 AI-0）
- **目標值**: L245 不變；L246 改為 ``不帶 `--post`（`cross_review.sh` pre-check，即 SV_REPORT_PREFLIGHT）執行 A/B/C/D，不讀 `.tmp/cross_review_state`。``。僅此一行，不動 Step 5 表格與其餘章節。
- [x] 完成（依據：qa/SKILL.md L246 已改為 A/B/C/D）

### AI-5: sv/SKILL.md — 標題字面契約＋report-only 閉環步驟＋exit 語意
- **檔案**: `.claude/skills/sv/SKILL.md`
- **現值**: AI-0 所列 L111、L156–160、L171
- **目標值**:
  - L111 之後新增一行：`` - **必備標題（字面，與 `structural_verify.sh` Check D 相同，於 Codex 前由 SV_REPORT_PREFLIGHT 檢查）**：`## Files Changed`、`## Implementation Scope`、`## Stage A`、`## Stage B`、`## Stage C`；缺任一 → REPORT_FIX（`cross_review.sh` exit 3，不呼叫 Codex） ``。
  - L156 改為 `` - `exit 3` (REPORT_FIX，未達閉環上限) → 📋 符合 report-only 閉環條件者依下方「REPORT_FIX report-only 閉環」處理；不符者修 `.tmp/sv_report.md` 後停止回報（不需重跑 /qa） ``。
  - L157 改為 `` - `exit 2` (ENV / FAIL BUDGET / `CHECKPOINT_REPORT_FIX_LIMIT` / `REPORT_FIX_ESCALATE`) → ⛔ 人工介入，檢查 `.tmp/cross_review_state/<ticket>`；`CHECKPOINT_REPORT_FIX_LIMIT` 為人工裁決 checkpoint（非 BLOCKER、非程式碼修正重試），不得自動重跑 ``（`exit 1` 行不變，仍為 BLOCKER）。
  - L158–160 Fail Budget 之後新增子項：`REPORT_FIX` 不遞增 `ROUND`（不扣 blocker budget）、遞增 `REPORT_FIX_COUNT`（上限 2）；`REPORT_FIX → REPORT_FIX → BLOCKER` 的第三次 exit 1 為一般 BLOCKER R1，**不是 REPORT_FIX 升級**。
  - 新增「REPORT_FIX report-only 閉環」小節（七條件 predicate 之**唯一來源**為 `ssot-governance.md` Transition Table，此處**不複製**）：①讀 exit 3 輸出之 `REPORT_FIX_SOURCE=`——`structural_preflight` 才可進入閉環；`codex` 一律輸出 `REPORT_FIX_ESCALATE` 並停止；②見 `CHECKPOINT_REPORT_FIX_LIMIT` 或 `REPORT_FIX_ESCALATE`（exit 2）即停止等待人工裁決；③僅修改 `.tmp/sv_report.md`（僅限既有內容重排、必要標題補齊、既有事實之欄位格式校正；不得新增測試結果、commit、命令輸出、證據引用、完成宣稱等新事實）；④單跑 `bash .claude/skills/qa/scripts/structural_verify.sh .tmp/sv_report.md "<DIFF_REF>..<DIFF_END>"` 至 exit 0；⑤以 `REPORT_FIX_CLOSURE=1 bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md "<DIFF_REF>..<DIFF_END>"` 重跑；⑥同票同一 state 週期（PASS／ADVISORY 清 state 前）最多自動閉環 2 次，第 3 次由腳本輸出 `CHECKPOINT_REPORT_FIX_LIMIT`。
  - L171 改為 `` - **機械輪次**：`.tmp/cross_review_state/<TICKET_ID>` 之 `CURRENT_ROUND`（BLOCKER 輪次，REPORT_FIX 不遞增；腳本自增，producer 明確） ``。
- [x] 完成（依據：sv/SKILL.md 新增必備標題行、exit 3／2 語意、Fail Budget 子項、report-only 閉環小節、L171 機械輪次）

### AI-6: sv/CROSS_REVIEW.md — 表列／state 欄位／預算敘述（含 exit 2）
- **檔案**: `.claude/skills/sv/CROSS_REVIEW.md`
- **現值**: AI-0 所列 L63、L64、L76、L80
- **目標值**: L63 改為 `` | `REPORT_FIX` | 3 | `.tmp/sv_report.md` 或 payload disclosure 不可信 | 符合 report-only 閉環條件者依 `sv/SKILL.md` 閉環（修報告→preflight→重跑）；否則修報告後停止回報，不需重跑 /qa | ``；L64 改為 `` | ENV / FAIL BUDGET / REPORT_FIX 閉環上限 | 2 | codex、Git、輸入、fail budget 錯誤；或 `CHECKPOINT_REPORT_FIX_LIMIT`／`REPORT_FIX_ESCALATE` | 人工介入（LIMIT 為人工裁決 checkpoint，非 BLOCKER） | ``；L76 改為 `` 內容包含 `TICKET`、`ROUND`（BLOCKER 輪次）、`REPORT_FIX_COUNT`、`LAST`。`MAX_BLOCKER_BUDGET` 從 `.claude/verify-policy.env` 讀取，預設為 `3`。 ``；L80 改為 `` - `REPORT_FIX` 不遞增 `ROUND`（不扣 blocker budget），但遞增 `REPORT_FIX_COUNT`（上限 2）；同票同一 state 週期（PASS／ADVISORY 清 state 前）第 3 次 REPORT_FIX 輸出 `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2），停止自動閉環 ``。
- [x] 完成（依據：CROSS_REVIEW.md L63／L64／L76／L80 已改，舊句 0 殘留）

### AI-7: ssot-governance.md — Transition Table REPORT_FIX 列（predicate 單一來源）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: L154 單列（見 AI-0）
- **目標值**: 拆為兩列——①`` | SV 或 Codex 判 BLOCKER | `MUST STOP`，依 SV／Cross-Review 既有契約處理 | 由 UC 自動重跑或改判（回指 `uc/SKILL.md` 規則 9） | ``；②`` | SV／Codex 判 REPORT_FIX | **先判斷是否符合 report-only 閉環**（七條件須全成立：①verdict＝`REPORT_FIX`；②僅修改 `.tmp/sv_report*.md`；③不改 Ticket、SSOT、AC、contract、產品程式碼、`PROGRESS.md`、`CLAUDE.md` 或任何其他 repo 檔案；④修補僅限既有內容重排、必要標題補齊、既有事實之欄位格式校正；⑤不新增測試結果、commit、命令輸出、證據引用、完成宣稱或任何新事實；⑥修補後通過 SV report preflight（`structural_verify.sh` 不帶 `--post`）才重跑 Cross-Review；⑦本次 `cross_review.sh` 以 exit 3 結束，且本次 result 不含 `CHECKPOINT_REPORT_FIX_LIMIT` 或 `REPORT_FIX_ESCALATE`）。成立 → `MUST AUTO-CHAIN`（步驟見 `sv/SKILL.md` 階段 D「REPORT_FIX report-only 閉環」）；任一不成立 → `MUST STOP` 並輸出 `REPORT_FIX_ESCALATE`；第 3 次 → `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2，人工裁決 checkpoint） | 於條件外修改 `.tmp/sv_report*.md` 以外任何檔案、新增事實、超過上限重跑、將 LIMIT 當 BLOCKER 或程式碼修正重試 | ``。**MUST NOT** 新增 lifecycle／verdict／receipt 枚舉（上列標記為輸出字面，非 verdict）。
- [x] 完成（依據：ssot-governance.md Transition Table 原第 154 行已拆為 BLOCKER／REPORT_FIX 兩列，REPORT_FIX 列含七條件與新 ⑦「本次 cross_review.sh 以 exit 3 結束，且本次 result 不含 CHECKPOINT_REPORT_FIX_LIMIT 或 REPORT_FIX_ESCALATE」）

### AI-8: rules/cross-review.md — 狹窄例外
- **檔案**: `.claude/rules/cross-review.md`
- **現值**: AI-0 所列 L13、L15、L17、L27
- **目標值**: L13 行尾加 `（**例外**：符合 `ssot-governance.md` Transition Table「SV／Codex 判 REPORT_FIX」七條件、僅修改 `.tmp/sv_report*.md` 之 report-only 閉環不觸發本款）`；L15 改為 `- 第 2 次 FAIL（同票任何維度；report-only REPORT_FIX 閉環之重跑不計入，其上限由腳本於第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2）管制）`；L17 行尾加 ` report-only REPORT_FIX 閉環不適用本段。`；L27 改為 `**退出機制**：無 Akira ack **MUST** 不得連續執行 2 次以上 cross_review.sh（report-only REPORT_FIX 閉環於腳本本次回 exit 3 之重跑除外；第 3 次 → `CHECKPOINT_REPORT_FIX_LIMIT` 人工裁決）。`。其餘 FAIL 規範不變。
- [x] 完成（依據：rules/cross-review.md L13／L15／L17／L27 已改（腳本本次回 exit 3 之重跑除外；第 3 次 LIMIT 人工裁決））

### AI-9: uc/SKILL.md — 規則 9 末項
- **檔案**: `.claude/skills/uc/SKILL.md`
- **現值**: L58
- **目標值**: `` - SV 的 PASS／ADVISORY／REPORT_FIX／BLOCKER／ENV ERROR 依 SV Skill 契約處理；`/uc` 自身 **MUST NOT** 重跑、改判或修改 `.tmp/sv_report*.md`；SV 契約內之 report-only REPORT_FIX 閉環由 SV 流程自行消費 exit 3，不屬 UC 之重跑或改判。 ``
- [x] 完成（依據：uc/SKILL.md 規則 9 末項已改）

### AI-10: 新增最小 shell fixture
- **檔案**: `.claude/skills/qa/tests/test_report_fix_closure.sh`（新增，LF）
- **現值**: 不存在
- **目標值**: scratch fake git repo（`mktemp -d` 或 session 暫存路徑；結束 `rm -rf`）：含 `.gitignore`（`tmp/`、`.tmp/`、`bin/` 及輸出檔）並 commit `.claude` 腳本副本（`cross_review.sh`／`structural_verify.sh`／`lib_verify.sh`／`verify-policy.env`，**自真 repo 唯讀複製**）；`tmp/verify_cmd.conf` 含 `TICKET_ID`／`DIFF_REF`／`ALLOWED`；fake `codex` stub 以環境變數決定 verdict 並計呼叫次數；`[[ "$(command -v codex)" == "$S/bin/codex" ]] || abort`；不寫真 repo／真 `.tmp`。覆蓋（目標態預期值；「現況」欄＝AI-0 基線紅燈，用於證明測試能失敗）：

  | Case | 輸入 | 目標態預期 | 現況 |
  |---|---|---|---|
  | F1 | structural 來源：報告缺 `## Stage B` | exit 3；stdout 含 `[D]`、`REPORT_FIX_SOURCE=structural_preflight`；Codex 呼叫 0；`REPORT_FIX_COUNT=1` | exit 0 且 Codex 被呼叫 |
  | F2 | Codex 來源 `REPORT_FIX→REPORT_FIX→BLOCKER` | **3→3→1**；第 3 次後 state＝`ROUND=1`、`REPORT_FIX_COUNT=2`、`LAST=BLOCKER`；`.tmp/cross_review_pass/<ID>` 不存在 | 3→3→2 |
  | F3 | `BLOCKER×3` | 1→1→2（不變） | 1→1→2 |
  | F4 | structural 來源 `REPORT_FIX×3` | **3→3→2**；`COUNT` 1→2→2；僅第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`；Codex 呼叫全程 0 | 3→3→3 |
  | F4b | Codex 來源 `REPORT_FIX×3` | 3→3→2；第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`；Codex 呼叫 3 | 3→3→3 |
  | F5 | `REPORT_FIX_CLOSURE=1`：tracked 檔被改／`.tmp/` 外 untracked | exit 2、輸出 `REPORT_FIX_ESCALATE`、state 不變、Codex 呼叫增量 0；對照組（僅 `.tmp/` 內變更）exit 0 | 無守門（不會 ESCALATE） |
  | F6 | 通過 A／B／C／D 但無憑條的報告 | pre 模式 exit 0、輸出不含 `[E]`／`[F]`／`[G]`、`tmp/.fail_budget_state` 不存在；`--post` exit 1 並被 Check E 擋、`[D]` 區塊僅出現 1 次 | pre 同；`--post` 同（D 僅 post） |
  | F7 | 舊格式 state（無 `REPORT_FIX_COUNT`）＋REPORT_FIX | state＝`ROUND=<原值>`、`REPORT_FIX_COUNT=1` | 無此欄 |
  | F8 | 混合 `BLOCKER,REPORT_FIX,BLOCKER,BLOCKER` | 1→3→1→2 | 1→3→2→2（第 2 次 REPORT_FIX 使 ROUND 前進） |
- [x] 完成（依據：fixture 已新增並對目標態 ALL PASS（37 PASS／0 FAIL），baseline 24 FAIL）

### AI-11: 驗證與收尾證據
- **動作**: 執行 fixture（全綠）；`bash -n` 兩支腳本；行尾檢查（以 `git ls-files --eol` 與位元組計數為準：`cross_review.sh`、`qa/SKILL.md`、`uc/SKILL.md` 為 `w/crlf` 且 `\r\n`＝`\n`〔修改前既有〕、其餘 ALLOWED 檔 `w/lf`）；`git diff -U0` 確認 BLOCKER 路徑（L451–459 預算判斷、`case "$VERDICT"` 各分支）逐字未變；`git diff --name-only` ⊆ ALLOWED；FORBIDDEN 零變更；顯式路徑 staging（禁 `git add -A`）。
- [x] 完成（依據：fixture 37 PASS；bash -n 通過；行尾檢查（cross_review.sh／qa／uc 純 CRLF，其餘純 LF）；BLOCKER 路徑 git diff -U0 無變動行；diff name-only ⊆ ALLOWED；FORBIDDEN 零變更；/qa Step 4.5 以顯式路徑 staging（無 git add -A）commit 07d3c242）

## Hard Constraints
1. **範圍**：僅 ALLOWED；`.claude/hooks/**`、`settings*.json`、`.claude/agents/**`、ticket-author 本體、`PROGRESS.md`、`PROGRESS_ARCHIVE.md`、`CLAUDE.md`、registry 一律不碰；`lib_verify.sh` 的 `tmp/.fail_budget_state` 計數、Check E／G／F／F.2／GH-* 邏輯不重構。
2. **預算**：REPORT_FIX **MUST NOT** 遞增 `ROUND`、**MUST NOT** 產生 PASS receipt；`BLOCKER×3` 行為（1／1／2）**MUST** 不變。
3. **preflight 邊界**：`SV_REPORT_PREFLIGHT`＝Check A／B／C／**D**；只前移 Check D；E、G、receipt／verdict／diff scope／reachability、F、F.2、GH-B 及任何涉及非 report 檔案的檢查 **MUST NOT** 前移。`sv/SKILL.md` 必備標題 **MUST** 與腳本 Check D 字面完全相同。
4. **閉環**：僅限七條件全成立；v1 僅 `REPORT_FIX_SOURCE=structural_preflight` 可自動修，`codex` 來源 **MUST** `REPORT_FIX_ESCALATE`；七條件 predicate 之單一來源＝`ssot-governance.md`，其餘檔 **MUST NOT** 複製；來源閘（`structural_preflight`／`codex`）位於 `sv/SKILL.md` 閉環步驟 ①，不屬七條件。同票同一 state 週期（PASS／ADVISORY 清 state 前）最多自動閉環 2 次。
5. **守門限制如實揭露**：closure 守門只偵測 `.tmp/` 以外工作樹變更，**不得**宣稱可偵測 report 內新事實；無關 dirty 檔造成保守誤停為預期行為。
6. **exit 語意（不變 + 單一新增用途）**：exit 1 維持 BLOCKER／`fail_blocker`；exit 2 維持「人工介入」（ENV／FAIL BUDGET），並**新增**用於 `CHECKPOINT_REPORT_FIX_LIMIT` 與 `REPORT_FIX_ESCALATE`；exit 3＝未達上限的 REPORT_FIX（exit 3 不等於自動閉環授權；自動閉環仍受既有 source、scope、evidence 限制）；exit 0 不變。**不新增 exit code、不修改 BLOCKER 路徑**（預算判斷與 `case` 分支逐字不變）。
7. **LIMIT 與第三次 exit 1 的語義**：`CHECKPOINT_REPORT_FIX_LIMIT` 是**人工裁決 checkpoint**，不是 BLOCKER、不是 advisor escalation、不是 code-fix retry；`REPORT_FIX → REPORT_FIX → BLOCKER` 的第三次 exit 1 是**一般 BLOCKER R1**，不是 REPORT_FIX 升級。**非目標**：本票屬 Advisor 議題線，但**不建立、也不依賴** advisor-first、decision-advisor 機制，亦不引用 MEO；亦不涉 R0（`cross-reviewer`）、Stop／SubagentStop／PermissionRequest 與任何 hook。
8. **行尾**：`cross_review.sh`、`qa/SKILL.md`、`uc/SKILL.md` 維持修改前既有 CRLF，其餘 LF；不得產生 mixed EOL 或非必要的行尾轉換。
9. **測試**：僅在 scratch 內執行；`codex` 必須解析到 stub 才執行；**MUST NOT** 於真 repo／真 `.tmp`／真 worktree 寫入 state。
10. 不引入第二套 receipt／finding database／workflow 平台；不順手改善相鄰程式碼；不處理 `cross_review.sh:344` 缺 `ALLOWED=` 時靜默 exit 1 之既有行為（僅 fixture 須含 `ALLOWED=`）。
11. staging 僅顯式路徑；禁 `git add -A`／`.`／`commit -a`。

## Error Handling
- AI-0 任一錨點不符 → 依三分流分類處置；`ALREADY_EXECUTED_PENDING_CLOSURE` → 停止回報；行號漂移、措辭變動或 `CHECKPOINT_REPORT_FIX_LIMIT` 尚不存在 → 只重讀補證，不停止
- 隔離環境中 `codex` 未解析到 stub → 中止，不執行
- baseline 紅燈無法重現（如 3／3／2 實測為其他值）→ **停止回報**（推論被推翻，不得續行）
- `bash -n` 失敗、mixed EOL、fixture 非全綠 → 停止回報，不 commit
- **REPORT_FIX（exit 3，`REPORT_FIX_SOURCE=structural_preflight`，七條件成立，未達上限）** → 依 `sv/SKILL.md` 閉環步驟：修 `.tmp/sv_report.md`→單跑 preflight 至 exit 0→`REPORT_FIX_CLOSURE=1` 重跑
- **REPORT_FIX（`REPORT_FIX_SOURCE=codex`，或任一條件不成立）／`REPORT_FIX_ESCALATE`（exit 2）** → 不得自動重跑，回報原因等待人工裁決
- **`CHECKPOINT_REPORT_FIX_LIMIT`（exit 2，第 3 次）** → 人工裁決 checkpoint：不得再自動重跑；非 BLOCKER、非 code-fix retry、非 advisor escalation；等待 Akira 裁決
- **BLOCKER（exit 1）** → 不屬自動閉環；依既有契約修程式碼或 Checkpoint；`ROUND>=3` → exit 2 FAIL BUDGET（既有行為不變）
- scope gate 因本票清單以外之 dirty 檔而 FAIL → 停止回報，由 Akira 處置（禁 stash／restore／順手 commit）
- **[標配]** `git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）。適用 commit 標的：ALLOWED 全部 9 檔

## Acceptance Criteria
- [x] **[存在性]** `sv/SKILL.md` 含字面 `## Stage A`、`## Stage B`、`## Stage C`，且 `structural_verify.sh` Check D 五標題清單逐項皆出現於 `sv/SKILL.md`；`sv/SKILL.md`、`CROSS_REVIEW.md`、`rules/cross-review.md`、`rules/ssot-governance.md`、`uc/SKILL.md` 含 report-only 例外字樣且彼此不抵觸；`qa/SKILL.md` L246 描述 pre-check＝A／B／C／D；`CROSS_REVIEW.md` 不再含舊句「REPORT_FIX 不扣 blocker budget，但會阻擋流程直到報告修正」（依據：fixture 外：sv/SKILL.md 五標題逐項命中（Stage A/B/C 各 1）；sv/SKILL.md、CROSS_REVIEW.md L63、rules/cross-review.md L13、ssot-governance.md L155、uc/SKILL.md 皆含 report-only 例外字樣且互不抵觸；qa/SKILL.md L246 為 A/B/C/D；CROSS_REVIEW.md 舊句 0 殘留）
- [x] **[存在性]** exit 語意：`cross_review.sh` 之 exit code 集合仍僅 {0,1,2,3}；BLOCKER 路徑（預算判斷區塊與 `case "$VERDICT"` 之 PASS／ADVISORY／REPORT_FIX／BLOCKER／`*` 分支行）於 `git diff -U0` 中無任何變動行；`CHECKPOINT_REPORT_FIX_LIMIT` 與 `REPORT_FIX_ESCALATE` 之 exit 皆為 2；`sv/SKILL.md`、`CROSS_REVIEW.md` 對 exit 1／2／3 之描述與腳本一致（依據：exit 字面集合 {0,1,2,3}；BLOCKER 路徑（預算判斷與 case 分支）於 git diff -U0 無變動行；LIMIT／ESCALATE 皆 exit 2；sv/SKILL.md、CROSS_REVIEW.md 與腳本一致）
- [x] **[可達性]** REPORT_FIX 路徑 trace：F1（structural 來源）與 F2／F4b（Codex 來源）fixture 實跑輸出顯示 `REPORT_FIX_SOURCE=…`、state 檔內容（`ROUND`／`REPORT_FIX_COUNT`／`LAST`）、exit code；producer 證據欄：`cross_review.sh` 兩處 `record_report_fix` 呼叫＋`structural_verify.sh` Check D 於 pre 路徑之位置 `cross_review.sh` L211（structural_preflight）／L475（codex）／定義 L166；`structural_verify.sh` Check D L208 起（位於 `if POST_MODE != true` 之前）（依據：fixture F1／F2／F4b 實跑輸出，37 PASS）
- [x] **[可達性]** agent-rule consumer（predicate ⑦）以 F4 序列 trace：given F4 實跑三次之 exit code 與 stdout token 序列（run1 exit 3、run2 exit 3、run3 exit 2＋`CHECKPOINT_REPORT_FIX_LIMIT`），由**全新 subagent**（僅提供改後 `ssot-governance.md` Transition Table「SV／Codex 判 REPORT_FIX」列、`sv/SKILL.md`「REPORT_FIX report-only 閉環」小節與各次輸出；不提供本票其餘內容，亦不提供 `REPORT_FIX_COUNT` 值）逐次判讀：expect run1→`MUST AUTO-CHAIN`（第 1 次閉環）、run2→`MUST AUTO-CHAIN`（第 2 次閉環）、run3→`MUST STOP`（不自動閉環）；判讀依據僅限 exit code 與 stdout token，不得依賴 state 計數；新組 RUN 2 判讀須無 ⑦／B⑥ 衝突；對照組（同序列改餵舊式「`REPORT_FIX_COUNT`＜2」⑦ 文字與完整舊票面含 B⑥，並提供 COUNT 值）RUN 2 須明示 `COUNT`＜2 不成立及其與 B⑥ 之解讀衝突，routing 結果如實記錄、不要求必為 `MUST STOP`；本 AC 不宣稱已證明新舊行為差異。觀察者＝該 subagent 之逐次輸出，原文貼入收尾實錄；consumer 證據欄：⑦ 於 `ssot-governance.md` 之位置 `ssot-governance.md` L155（⑦）（依據：兩組 subagent 重放原文見下，新組 AUTO-CHAIN／AUTO-CHAIN／STOP 且 RUN 2 無衝突；舊組 RUN 2 明示衝突）
  - 重放結果（新 predicate，全新 subagent，僅給規則 A／B 與 F4 三次輸出，不給 COUNT）：RUN 1→MUST AUTO-CHAIN；RUN 2→MUST AUTO-CHAIN（依據 exit 3 且無 LIMIT／ESCALATE token）；RUN 3→MUST STOP（依據 CHECKPOINT_REPORT_FIX_LIMIT＋exit 2）。符合 AC；RUN 2 判讀僅引 exit 3 與無 LIMIT／ESCALATE token，無 ⑦／B⑥ 衝突。
  - 對照組結果（舊式 ⑦「REPORT_FIX_COUNT＜2」，必須給 COUNT=1/2/2）：RUN 1→MUST AUTO-CHAIN；RUN 2→**MUST AUTO-CHAIN**（agent 明示「嚴格依條文 COUNT=2 時 ⑦ 不成立」，但以 B⑥『最多自動閉環 2 次』與 exit 3 推翻）；RUN 3→MUST STOP。即 RUN 2 明示 COUNT＜2 不成立並與 B⑥ 衝突，routing 以 B⑥ 解讀為 AUTO-CHAIN，如實記錄；符合 Akira 修正後之 AC（不要求必為 STOP，不宣稱已證明新舊行為差異）。
- [x] **[行為性]** given structural 來源報告缺 `## Stage B`（其餘正確）、stub codex 呼叫計數歸零：expect `cross_review.sh` exit 3、stdout 含 `[D]`、`REPORT_FIX_SOURCE=structural_preflight`、stub 呼叫數＝0（基線：exit 0 通過 pre-check）（依據：fixture F1 全綠（exit 3、[D]、REPORT_FIX_SOURCE=structural_preflight、Codex 0、COUNT=1）；baseline 為 exit 0 且 Codex 被呼叫）
- [x] **[行為性]** given Codex 來源 REPORT_FIX→REPORT_FIX→BLOCKER：expect exit **3／3／1**（基線紅燈 3／3／2，AI-0 實測）；第 3 次後 state＝`ROUND=1`、`REPORT_FIX_COUNT=2`、`LAST=BLOCKER`；任一 REPORT_FIX 後 `.tmp/cross_review_pass/<ID>` 不存在（依據：fixture F2 全綠（3/3/1、ROUND=1、COUNT=2、LAST=BLOCKER、無 PASS receipt）；baseline 3/3/2）
- [x] **[行為性]** given `BLOCKER×3`：expect exit 1／1／2（維持不變）（依據：fixture F3 全綠（1/1/2））
- [x] **[行為性]** given structural 來源連續 3 次 REPORT_FIX：expect exit **3／3／2**，`REPORT_FIX_COUNT` 1／2／2，僅第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`，Codex 呼叫全程＝0；given Codex 來源連續 3 次 REPORT_FIX：expect exit 3／3／2、第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`、Codex 呼叫 3 次（基線：3／3／3，無上限）（依據：fixture F4／F4b 全綠（3/3/2、COUNT 1/2/2、LIMIT 僅第 3 次、structural Codex 0／Codex 來源 Codex 3）；baseline 3/3/3）
- [x] **[行為性]** given `REPORT_FIX_CLOSURE=1` 且 `.tmp/` 以外有 tracked 變更（或 untracked 檔）：expect 輸出 `REPORT_FIX_ESCALATE`、exit 2、state 檔不變、stub 呼叫增量＝0；對照組（僅 `.tmp/` 內變更）exit 0 正常進入（依據：fixture F5 全綠（tracked／untracked exit 2＋ESCALATE＋state 不變＋Codex 0；.tmp 內變更對照組 exit 0））
- [x] **[行為性]** given 報告通過 A／B／C／D 但無憑條：expect pre 模式 exit 0、輸出不含 `[E]`／`[F]`／`[G]` 區塊、`tmp/.fail_budget_state` 不存在；同報告 `--post` exit 1 並被 Check E 擋、`[D]` 區塊僅出現 1 次（依據：fixture F6 全綠（pre exit 0、無 E/F/G、無 fail_budget_state；--post exit 1 被 [E] 擋、[D] 僅 1 次））
- [x] **[行為性]** given 舊格式 state（無 `REPORT_FIX_COUNT`）＋REPORT_FIX：expect `REPORT_FIX_COUNT=1`、`ROUND` 維持原值；given `BLOCKER,REPORT_FIX,BLOCKER,BLOCKER`：expect exit 1／3／1／2（依據：fixture F7／F8 全綠（COUNT=1、ROUND 維持原值；1/3/1/2）；baseline 1/3/2/2）
- [x] **[存在性]** `cross_review.sh` 全 CRLF（`git ls-files --eol` 顯示 `w/crlf`，且位元組計數 `\r\n`＝`\n`）且 `bash -n` 通過；`qa/SKILL.md`、`uc/SKILL.md` 維持修改前既有 CRLF；其餘 ALLOWED 檔維持 LF（含新增之 fixture）。本次修改不得引入混合行尾或非必要的行尾轉換（依據：cross_review.sh w/crlf 且 516=516、bash -n 通過；qa/SKILL.md（279=279）、uc/SKILL.md（95=95）修改前既有 CRLF，CR 不敏感 diff 僅預定之各 1 行修改；其餘 ALLOWED 檔 w/lf 且純 LF（含 fixture）；無混合行尾）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案；FORBIDDEN 零變更；已知限制（Codex 來源 v1 不自動閉環、守門限制、`CLAUDE.md` Rule 4c 措辭、LIMIT 後無機械式拒跑）已於收尾實錄揭露（依據：commit 07d3c242 僅 9 個 ALLOWED 檔（9F +240L −27L）；FORBIDDEN 零變更；已知限制已揭露於下方「收尾實錄」與 sv_report）

## 收尾實錄（Agent closeout，2026-10-11）

- **結果**：AI-0 至 AI-11 與 AC 全數完成（`assembleDebug` N/A，docs preset）。實作 commit `07d3c242`（9F +240L −27L，`DIFF_REF=b6dc0441`／`DIFF_END=07d3c242`）。`/qa` Step 0 PASS（commit 前 diff 為空）、Step 3.5 ⚠️ 3 項皆在 `app/src/main/` 屬既有；`/sv` Codex Cross-Review **PASS**（`REACHABILITY: REPORT_FIX_CLOSURE:.claude/skills/sv/SKILL.md:162,REPORT_FIX_COUNT:.claude/skills/sv/scripts/cross_review.sh:169`；receipt 已寫入）；`structural_verify.sh --post` PASS（A/B/C/D/E/F/G，verdict ADVISORY＝PROGRESS.md 79 行 > 60 警示）。
- **Baseline／目標態**：fixture 對修改前腳本 24 FAIL（F2 3/3/2 ROUND=3、F4／F4b 3/3/3、F1 exit 0 且 Codex 被呼叫、F3 1/1/2、F8 1/3/2/2；結構來源 ghost×2→Codex BLOCKER＝3/3/2、Codex 0/0/1），對 commit 後腳本 37 PASS／0 FAIL。
- **agent-rule consumer 重放原文（n-5 承接）**：新組（新 ⑦）RUN 1→MUST AUTO-CHAIN；RUN 2→MUST AUTO-CHAIN（依據 exit 3 且無 LIMIT／ESCALATE token）；RUN 3→MUST STOP（依據 `CHECKPOINT_REPORT_FIX_LIMIT`＋exit 2）。舊組（舊 ⑦＋B⑥，給 COUNT）RUN 1→MUST AUTO-CHAIN；RUN 2→MUST AUTO-CHAIN（明示 COUNT＜2 不成立，與 B⑥ 衝突，以 B⑥ 解讀）；RUN 3→MUST STOP。如實記錄，**不宣稱已證明新舊行為差異**；重放者為 subagent，非真實 `/sv` session。
- **Akira 執行期裁決**：m-3 採 (b)（HC-4 只改述「七條件單一來源」，不新增 `REPORT_FIX_SOURCE` 依賴）；n-2 維持不修；EOL AC 改為 `qa/SKILL.md`、`uc/SKILL.md` 維持修改前既有 CRLF（HC-8、AI-11 同義同步）；M-2 對照組預期改為如實記錄。
- **偏差與揭露**：①`ssot-governance.md` 首次編輯被 auto mode classifier 拒絕，未繞過，經 Akira 指示後重試成功；②`REPORT_FIX_SOURCE` 為本票 AI-1 自定 stdout token（非既有 producer），HC-6 走授權 fallback 分支；③AI-0 另以 ad hoc 腳本重現「結構來源 ghost×2→BLOCKER」baseline（非 F 系列案例）。
- **已知限制**：closure 守門只偵測 `.tmp/` 以外之 tracked／非 ignored untracked 變更，無法偵測 report 內新事實（七條件⑤為 agent 義務），亦不偵測 gitignored 路徑與 `.tmp/` 內他檔（含 `cross_review_state`，`COUNT` 完整性屬 agent 紀律）；無關 dirty 檔造成保守誤停；v1 僅 `structural_preflight` 來源自動閉環，Codex 來源一律 `REPORT_FIX_ESCALATE`；LIMIT 後無機械式拒跑（deferred）；`CLAUDE.md` Rule 4c 措辭未同步（FORBIDDEN，若需要另票）；`.agents/skills/**` 未追蹤鏡像既有漂移、不同步。
- **DEFERRED-TO-DOCSYNC（不開票）**：`.claude/PROGRESS.md` 同步與 `PROGRESS_ARCHIVE.md` Done 標記（兩檔命中票面 FORBIDDEN），依 §FORBIDDEN 延後項免開票紀律，於下一輪 `/compact` 正常收尾處理。
- **Traycer**：`Traycer Workflow: N/A` → `reviews.traycer=N_A`。終審 `CLOSED` 限審查方 SMTR／CSO。
