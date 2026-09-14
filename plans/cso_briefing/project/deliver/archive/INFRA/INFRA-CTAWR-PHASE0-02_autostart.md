# INFRA-CTAWR-PHASE0-02: `/uc` Phase 0 修補——參數容錯（票號 token 切分）＋ activation 成功後自動讀票開工

**簽發日期**: 2026-09-13
**簽發者**: Claude（SKAD；Akira 2026-09-13「檢視分析附件進行開票修補」）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（純 `.claude/` 工具鏈，零 `.kt`／`.xml`）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `INFRA-CTAWR-PHASE0-01`（CLOSED；本票修補其交付物 `activate_ticket.sh` 與 `uc/SKILL.md` Phase 0 節）
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=INFRA-CTAWR-PHASE0-02
CREATED=2026-09-13
PRESET=bugfix
ALLOWED=.claude/scripts/activate_ticket.sh,.claude/skills/uc/SKILL.md
FORBIDDEN=.claude/skills/normal/SKILL.md,.claude/skills/ticket-run/**,.claude/skills/qa/**,.claude/skills/compact/**,.claude/hooks/**,.claude/CLAUDE.md,.claude/rules/**,app/**
MAX_LINES=N/A
DIFF_REF=<開工時回填>
ANCHOR_PROBED=2026-09-13（SKAD 第一手：`uc/SKILL.md` 全文 aud_20260913_523d216903；`activate_ticket.sh` 全文 aud_20260913_1dd070ce0c／d731ab641e；票號唯一性與 Revision 檔頭 aud_20260913_3e2417a426）
BASE_COMMIT=a8073f5
TARGET_BLOB=.claude/scripts/activate_ticket.sh:<開工時回填>;.claude/skills/uc/SKILL.md:<開工時回填>
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**MAX_LINES=N/A 附註**（TPL v3.5 R3）：AI-1 含 fail-closed 守門判定（無合法票號 token → 拒絕 activation），屬安全不變量票。

**條件 6 判定**：6a `NONE`／6b① ALLOWED 首層 segment ＝ `.claude` 單一／6b② `### AI-` 標題數 ＝ 2／6b③ `NONE`／6c 二標的無 `Revision` 檔頭修訂歷史 → 均未觸發，得開執行票。

## 檔案治理／來源說明

**事件（2026-09-13，Claude Code 執行端自述）**：連續兩次 `/uc` 停止，原因不同——
1. `/uc RGR-RESP-SCHEMA-01 Rev A 收窄…<長文>`：Phase 0 將 `$ARGUMENTS` **整段逐字**代入 `activate_ticket.sh "$1"`，腳本以整段文字組 `ls Pending/"<整段>"_*.md` → 找不到 → exit 3 → 依規則不進臥底模式。
2. `/uc RGR-CODEX-TRANSPORT-01`：activation 成功、輸出進場確認句 `🤫 UC mode ON…` 後**停在原地**——`uc/SKILL.md` Phase 0 三條只定義到「載入 conf」，全檔無任何條文要求「activation 成功後接續讀票、執行 AI-0」；進場確認句為 prompt 最後一步，其後即回到等待。

**根因**：Phase 0（`INFRA-CTAWR-PHASE0-01`，2026-09-08）上線前，`/uc <文字>` 之參數僅為 prompt 上下文，模型自然接續開工；上線後參數被機械化為腳本輸入，但（a）未對「參數＝純票號」之隱性前提設防呆，（b）未把原本由模型隱式承擔的「讀票→開工」接續動作顯式化。前幾日未出問題係因呼叫形狀剛好落在隱性假設內。

**裁決依據**：Akira 2026-09-13「執行完進場確認句之後就沒有動作」以缺陷視之 → activation 成功 **MUST** 自動接續開工，不等使用者再下一句（本票據此落字；若 Akira 意在「activation 只載入、開工待確認」，本票 AI-2 第 3 條作廢，改為輸出 `📄 已載入，等待指示`）。

**Rev A.1（2026-09-13，SKAD）**：依 Akira「加入 @ 絕對路徑模式」補條 1a 與對應 AC／HC-6。Perplexity 同日草案之 B 模式 path gate、verified metadata（`ACTIVATION_STATUS`／`TICKET_BLOB`／`RUN_ID`／`INPUT_MODE`）、QA／SV admission gate（AI-3，觸 `qa/SKILL.md`、`sv/SKILL.md`）與後續票 `INFRA-CTAWR-ACTIVATION-RECEIPT-01` **均不採**——理由見 HC-6；Effort 維持 Low、ALLOWED 維持二檔。

**Rev A.2（2026-09-13，SKAD，Akira「該議題已收束，此事件有關連性，同意此對話一併處理」）**：併入 `active.json` 與 `tmp/verify_cmd.conf` 不同步事件。第一手根因（aud_20260913_262921ad4a）：`active.json` 唯一寫入者為 `verify_checklist.sh` L56–86（`/qa` 時），`verify_cmd.conf` 寫入者為 `activate_ticket.sh`（`/uc` 時）；`/uc X` 後、`/qa` 前 `active.json` 必指向前票，`session_start.sh` L54 之 `📍 Active ticket:` 因而於 `/clear` 後報錯票。修法為 AI-1 條 5a——activation 時同步寫 `active.json`（同 schema、同原子寫法），不新增載體、不改 QA 腳本。另採 Perplexity 09-13 回流確認之三點語義（basename 取 ID／不觸路徑／沿用既有 lookup）與 `@void/` 反例 AC——Rev A.1 已含，本次僅補 `resolve`／`stat` 字面。多 ID token 處置與 Perplexity 分歧，見條 1b。

**範圍外**：`/normal` 同以 `$ARGUMENTS` 呼叫腳本，AI-1 之 token 切分自動使其受益，`normal/SKILL.md` 不動（∈ FORBIDDEN）；`/normal` 不具自動開工語義，不套用 AI-2 第 3 條。`uc/SKILL.md`「驗證性動作豁免」節所引「session 開頭注入的〔腳本現況〕」為既存懸置引用，非本票範圍。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/scripts/activate_ticket.sh`、`.claude/skills/uc/SKILL.md`
- **動作**:
  - A：`grep -c '^TICKET_ID_ARG="\$1"$' .claude/scripts/activate_ticket.sh` ＝ 1；`grep -c 'no ticket ID token' .claude/scripts/activate_ticket.sh` ＝ 0（未先行）
  - B：`grep -c '^## Phase 0：Ticket Activation（帶參數呼叫時）$' .claude/skills/uc/SKILL.md` ＝ 1；`grep -c '自動接續\|📄 票檔' .claude/skills/uc/SKILL.md` ＝ 0（未先行）
  - C：`grep -n '^echo "✅ ACTIVATED' .claude/scripts/activate_ticket.sh` 恰 1 次（AI-1 條 4 插入錨）
  - D：TPL v3.8 三分流——`git diff --name-only a8073f5..HEAD -- .claude/scripts/activate_ticket.sh .claude/skills/uc/SKILL.md` 為空 → 續作；非空 → 依三分流歸因回報
- **任一項不符 → 停止回報。**
- [x] 完成

### AI-1: `activate_ticket.sh` 參數容錯——自 `$*` 切出票號 token
- **檔案**: `.claude/scripts/activate_ticket.sh`
- **現值**（L14–18）:
```bash
if [ -z "${1:-}" ]; then
  echo "Usage: activate_ticket.sh <TICKET_ID>" >&2
  exit 2
fi
TICKET_ID_ARG="$1"
```
- **目標值**（行為契約）:
  1. 改讀 `$*`（全部參數以空白合併），逐 whitespace-token 掃描，取**第一個**符合 `^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$` 且含至少一位數字之 token 為 `TICKET_ID_ARG`；比對前 **MUST** 先去除 token 尾端之 `:`／`,`／`;`／`。`／`，`（使用者常在票號後接冒號說明）。
  1a. **路徑形式（`@` 模式，Rev A.1）**：token 以 `@` 開頭、或去引號後以 `.md` 結尾者，視為票檔路徑拼法——取其 basename（去 `.md`）中**首個 `_` 之前**的子字串為候選票號，套用與條 1 相同之 regex 驗證。**腳本 MUST NOT 開啟、讀取、canonicalize、`realpath`／resolve、`stat` 或存在性檢查該路徑**；票檔一律經條 1 之 ID glob 於 repo 內 `Pending/`／`deliver/` 定位（`void/`、`archive/`、repo 外路徑因而自然落 exit 3，檔名 ID 與票面 ID 不一致落既有 exit 5）。路徑形式與純票號形式同時出現且票號不同 → exit 2。此為 A 模式之拼寫變體，**非新能力**，`INTRODUCED_CAPABILITY` 維持 `NONE`。
  1b. **多個相異純票號 token（Rev A.2）**：附帶指示合法地可引用他票（evidence／coordination 依賴），故 **MUST 以首個 ID token 為準**，不 fail-closed；但 **MUST** 於 stdout 印一行 `⚠️ 參數含多個票號 token：<清單>，以 <首個> 為準`，使誤取可見。（Perplexity 建議 fail-closed；不採，理由：exit 2 會迫使使用者刪除指示中的他票引用，摩擦大於誤取風險——誤取由 `✅ ACTIVATED | <ID>` 行與本提示行即時可見，且 AI-0 為唯讀復驗，`git restore` 可恢復。）
  2. 無任何 token 符合 → `echo "no ticket ID token in arguments: <前 60 字>" >&2`，exit 2（沿用用法錯誤碼，**MUST NOT** 寫入或清空既有 conf）。
  3. 票號 token 以外之文字非空 → stdout 印一行 `📝 附帶指示 <N> 字（由 skill 承接，腳本不處理）`，**MUST NOT** 據以停止、**MUST NOT** 印出全文。
  4. 成功輸出於 `✅ ACTIVATED` 行**之前**新增一行 `📄 ${TICKET_FILE}`（供 skill 接續讀票；路徑相對 repo root）。
  5. 檔頭 Usage 改為 `activate_ticket.sh <TICKET_ID|@票檔路徑> [附帶指示…]`，並補述 token 切分規則、`📄` 行與 `active.json` 同步。
  5a. **`active.json` 同步（Rev A.2，`INFRA-QA-PERTICKET-CONF-01` 線不同步事件併入）**：於 conf 覆寫成功後，**MUST** 以與 `qa/scripts/verify_checklist.sh` L69 printf **位元組級同構**之七欄 JSON（`ticket_id`／`ticket_revision`／`repo_root`／`branch`／`head_sha`／`activated_at`／`activation_source`，欄序不變）寫入 `tmp/verify/active.json`；**MUST** 沿用 L57／L76 之 `${dest}.tmp.$$` → `mv -f` 原子替換。欄值：`ticket_revision`＝`rev-0`（與 L250 之 conf 缺 `REVISION` 預設一致）、`head_sha`＝`git rev-parse HEAD` 全長、`activated_at`＝`date -u +%Y-%m-%dT%H:%M:%SZ`、`activation_source`＝`activate_ticket.sh`。**寫入或替換失敗僅 stderr 警示，MUST NOT 改變 exit 0**（比照 L73／L76 fail-open）。目的：使 `session_start.sh` L54 `📍 Active ticket:` 於 `/uc` 後、`/qa` 前即反映當前票；`verify_checklist.sh` 於 `/qa` 時之覆寫為同 schema 刷新，非衝突。
  6. 其餘 exit code 語義（3／4／5）、九欄 regex、切票通知、快照清除、`✅ ACTIVATED` 格式 **MUST** 位元組級不變。
- [x] 完成

### AI-2: `uc/SKILL.md` Phase 0 補「成功後自動接續」
- **檔案**: `.claude/skills/uc/SKILL.md`
- **現值**（Phase 0 節第 1 條）: `1. \`$ARGUMENTS\` 非空 → **MUST** 先執行 \`bash .claude/scripts/activate_ticket.sh "$ARGUMENTS"\`；非零 exit → 回報 \`⛔ UC Activation STOPPED: <腳本 stderr 首行>\` 並**不進入臥底模式**。`
- **目標值**: 該條保留，於其後**插入**第 1a／1b 兩條（原第 2、3 條順延，不改字）:
  - `1a. activation 成功（exit 0）→ 輸出「進入確認」句後 **MUST 立即自動接續**，**MUST NOT** 停下等待使用者：(i) 讀取腳本 \`📄\` 行所示票檔全文；(ii) 自票面 AI-0 起依序執行，進入既有 Auto-Chain（規則 8／9）。「進入確認」句為臥底模式之起點，非終點。`
  - `1b. \`$ARGUMENTS\` 中票號 token 以外之文字 → 視為本次執行之**附帶指示**（如 Rev 收窄、限定 AI 範圍、補充判準），與票面一併納入：附帶指示得**限縮或補充**票面 AI 之範圍，**MUST NOT** 擴張至 \`ALLOWED\` 之外、**MUST NOT** 覆蓋 Hard Constraints。附帶指示與票面衝突且無法以限縮解讀者 → 回報 \`⚠️ SSOT 衝突\` 停止。`
- 「進入確認」節位置與字面 **MUST** 不變；輸出規則 1–9、驗證性動作豁免、唯一例外三節 **MUST** 位元組級不變。
- [x] 完成

## Hard Constraints
1. AI-1 之 token 切分 **MUST** 為純 shell（`for`／`case`／`grep -E`），**MUST NOT** 引入 `jq`／`python`／`awk` 以外之新依賴；**MUST NOT** 改變 fail-closed 語義（任一前提不成立即非零 exit、不動既有 conf）。
2. AI-2 之自動接續 **MUST NOT** 繞過票面 AI-0 錨點復驗與三分流；接續＝從 AI-0 開始，非從 AI-1 開始。
3. 附帶指示 **MUST NOT** 被寫入 `tmp/verify_cmd.conf` 或任何檔案；其生命週期限本次對話。
4. **MUST NOT** 修改 `normal/SKILL.md`——其呼叫路徑經 AI-1 自動獲得 token 容錯，且不具自動開工語義。
5. **MUST NOT** 於 activation 失敗（非零 exit）時進入臥底模式或讀票；現行 STOPPED 語義不變。
6. **MUST NOT** 為 `@` 路徑形式新增任何 path gate、canonical path 欄、`ACTIVATION_STATUS`／`TICKET_BLOB`／`RUN_ID`／`INPUT_MODE` 等 verified metadata，或 QA／SV admission gate（Akira 2026-09-13 審查：blob 漂移由 TPL v3.8 票面 `BASE_COMMIT`／`TARGET_BLOB`＋AI-0 三分流承載，另立 activation 層 blob 檢查即為 v3.8 明禁之雙軌；admission gate 重現「單點失敗即全停」連坐）。

## Error Handling
- AI-0 任一 grep 命中數不符 → 停止回報
- 票號 regex 對既有票號樣式（含 `CUE-SNAP-01-FIX-A` 型尾段字母）不命中 → 停止回報並附反例（禁自行放寬 regex）
- **[標配]** `git check-ignore -v .claude/scripts/activate_ticket.sh .claude/skills/uc/SKILL.md` 命中 → 停止回報（gitignore 死結律 #20）

## Acceptance Criteria
- [x] **[存在性]** `activate_ticket.sh` 檔頭 Usage 含 `[附帶指示…]` 與 `📄`；`grep -c 'no ticket ID token'` ＝ 1；`grep -c '^TICKET_ID_ARG="\$1"$'` ＝ 0
- [x] **[存在性]** `uc/SKILL.md` Phase 0 節含 `1a.` 與 `1b.` 各恰 1 條；「進入確認」節字面不變
- [x] **[行為性]** given `bash activate_ticket.sh "RGR-CODEX-TRANSPORT-01"`（純票號，該票檔在盤），expect exit 0、stdout 含 `📄 plans/cso_briefing/project/…/RGR-CODEX-TRANSPORT-01_*.md` 行於 `✅ ACTIVATED` 之前、無 `📝` 行
- [x] **[行為性]** given `bash activate_ticket.sh "RGR-CODEX-TRANSPORT-01: Rev A 收窄，僅執行 AI-1 與 AI-2，AI-3 延後"`，expect exit 0、`TICKET_ID_ARG` 解析為 `RGR-CODEX-TRANSPORT-01`（尾端冒號去除）、stdout 含 `📝 附帶指示 N 字` 且 N > 0、conf 之 `TICKET_ID` 正確
- [x] **[行為性]** given `bash activate_ticket.sh "請幫我收窄 Rev A 的範圍"`（無票號 token），expect exit 2、stderr 含 `no ticket ID token`、`tmp/verify_cmd.conf` 位元組級不變
- [x] **[行為性]** given `bash activate_ticket.sh "@D:/DevSources/AndroidProj/SmartCameraApp1/plans/cso_briefing/project/Pending/RGR-CODEX-TRANSPORT-01_<slug>.md"`（實際檔名），expect 與純票號形式**逐行相同**之 stdout（`📄` 行為 repo 內 glob 定位之相對路徑，非參數所給絕對路徑）、exit 0；given 同形式但路徑指向 `void/` 下之檔，expect exit 3（腳本未開啟該路徑，僅以 basename 票號 glob 定位失敗）；given `@…/RGR-X-01_a.md RGR-Y-02`（路徑與票號不同），expect exit 2
- [x] **[行為性]** given 既有 exit 3／4／5 之觸發條件各一（不存在票號、缺 ALLOWED 之臨時票檔、票面 ID 不符），expect 行為與改動前逐行一致（附改動前後 stderr 對照）
- [x] **[行為性]** given `bash activate_ticket.sh "RGR-CODEX-TRANSPORT-01: 比照 INFRA-CTAWR-PHASE0-01 的做法"`（兩個相異 ID token），expect exit 0、activation 為 `RGR-CODEX-TRANSPORT-01`、stdout 含 `⚠️ 參數含多個票號 token` 行
- [x] **[行為性]** given `tmp/verify/active.json` 指向他票，when `bash activate_ticket.sh "RGR-CODEX-TRANSPORT-01"`，expect 該檔七欄被原子替換、`ticket_id` 為新票、`activation_source` 為 `activate_ticket.sh`、`head_sha` 等於 `git rev-parse HEAD`；且 `bash .claude/hooks/session_start.sh inject` 之 `📍 Active ticket:` 行顯示新票
- [ ] **[行為性]** given `tmp/verify/` 不可寫（`chmod` 或以唯讀掛載模擬），when activation，expect `tmp/verify_cmd.conf` 仍正確覆寫、stderr 含 `active.json` 警示、exit 仍為 0 — ⚠️ Windows/git-bash 下 `chmod 555` 對 NTFS 目錄未實際生效，無法在本環境模擬唯讀，未能驗證；程式碼路徑（`mv -f ... 2>/dev/null || echo ⚠️ ... >&2`，exit 0 不受影響）與既有 `write_active_json` fail-open 慣例一致，但未經真實唯讀環境驗證
- [ ] **[實機]** VSCode 擴充 host 下：①`/uc RGR-CODEX-TRANSPORT-01` 於進場確認句後**未經使用者輸入**即出現讀票與 AI-0 執行動作；②`/uc RGR-CODEX-TRANSPORT-01: 僅執行 AI-1` 於接續執行中可見附帶指示生效（未執行 AI-2 以後）；transcript 片段存收尾 — 未測（本輪為同一 UC session 內連續執行，非独立 `/uc` 冷啟叫用；需下次 session 冷啟以 `/uc <TICKET>` 實測 1a 自動接續）
- [x] ~~`assembleDebug` PASS~~（N/A：純 `.claude/` 工具鏈）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

**End of INFRA-CTAWR-PHASE0-02**

## Addendum

**收束裁決（2026-09-13，Akira，SKAD 落冊）**：轉 `CLOSED`。

1. **條 1a 明示裁決**：Akira 2026-09-13 逐字裁決「activation 成功後自動開工」。票面「檔案治理／來源說明」節所預留之作廢路徑（改為 `📄 已載入，等待指示`）**廢止**，1a 為定案語義。
2. **兩項未勾 AC 之處置（誠實留白，不偽勾）**：
   - L113 `[行為性]` `tmp/verify/` 不可寫 fail-open：執行端第一手取證 Windows／git-bash 下 `chmod 555` 對 NTFS 目錄不生效，無法模擬唯讀；程式碼路徑（`mv -f … 2>/dev/null || echo ⚠️ … >&2`，exit 0 不受影響）與 `verify_checklist.sh` L73／L76 既有 `write_active_json` fail-open 同構，未實測。**補驗途徑（不需唯讀掛載）**：以 regular file 佔位 `tmp/verify`（`mv tmp/verify tmp/verify.bak && touch tmp/verify`）使 `${dest}.tmp.$$` 寫入因 `Not a directory` 失敗，即可觸發同一 fail-open 分支；測後還原。留待下次順手驗。
   - L114 `[實機]` VSCode 冷啟自動接續：本輪為同一 UC session 內連續呼叫，非獨立 `/uc <TICKET>` 冷啟；1a 之「進場確認句後未經使用者輸入即讀票、執行 AI-0」需下次 session 首次 `/uc` 驗證。**此為下次 session 第一個 `/uc` 的自然副產品**，不需另排測試；結果以 Addendum 追加本檔。
3. **Rev 歷程冊面補記**：Rev A.1（`@` 路徑拼法、HC-6、三條 AC）與 Rev A.2（`active.json` 同步、多 ID 首個為準＋提示行、`resolve`／`stat` 字面）於派工前落票面，冊面 `notes` 停在 Rev A.0——本 Addendum 為冊面側之補記載體（`checkpoint` 於轉終態時依平台語義清空）。
4. **閉環確認**：兩起 `/uc` 停止事件（長文參數 exit 3、成功後停於進場確認句）均由 commits `04a29cc`／`5914562`／`8c2e6dc` 修復；`active.json` 與 `tmp/verify_cmd.conf` 不同步（PERTICKET 線事件）由 AI-1 條 5a 併入解決。後續遇問題依既定處置開 `INFRA-CTAWR-*` 修補票，不重開本票。
