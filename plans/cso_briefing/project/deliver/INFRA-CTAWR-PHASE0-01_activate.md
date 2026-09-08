# INFRA-CTAWR-PHASE0-01: Desktop 側 ticket activation——`/uc`／`/normal` 帶票號呼叫時恆重生成 verify config

**簽發日期**: 2026-09-07
**簽發者**: Claude（SKAD；Akira 2026-09-07 裁決「同意上述建議」＝恆覆寫＋`/normal` 收參數＋現在開票）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（純 `.claude/` 工具鏈，無 `.kt`／`.xml`）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `MOBCD-04`（CLOSED；`ticket-run` SKILL.md 10.1 之九欄擷取 regex 為本票 AI-1 字面複用來源，L96；同檔 L113 載明「`/uc` 讀到殘留前一張票 ID 且不報錯、不中斷」之高嚴重度失效形狀，為本票 Desktop 側同形缺口之取證）
- `coordination_dependency`: `INFRA-SESSION-HOOK-01`（REVIEW；其 `reset` 於 `/clear` 後清除 `tmp/verify_cmd.conf`——該行為使 Desktop 側「清後無重建通道」成為必然，本票為其補全路徑。二票**無執行順序依賴**，可並行）

## Verify Config
TICKET_ID=INFRA-CTAWR-PHASE0-01
CREATED=2026-09-07
PRESET=feature
ALLOWED=.claude/scripts/activate_ticket.sh,.claude/skills/uc/SKILL.md,.claude/skills/normal/SKILL.md
FORBIDDEN=.claude/skills/ticket-run/**,.claude/skills/qa/**,.claude/skills/sv/**,.claude/hooks/**,.claude/CLAUDE.md,.claude/rules/**,.claude/settings.json,app/**
MAX_LINES=N/A
DIFF_REF=<開工時回填>
ANCHOR_PROBED=2026-09-07（SKAD 第一手：`uc/SKILL.md` 全文 aud_20260907_d27efec277；`normal/SKILL.md` 全文 aud_20260905_8ef675df1a；`ticket-run/SKILL.md` L96/L113 aud_20260907_d2673f5a8e；`qa/scripts/verify_checklist.sh` L250–271 aud_20260907_fde8f372ac；`.claude/scripts/` 目錄 aud_20260907_6f86e950fb；`CTAWR` 前綴全冊唯一性 aud_20260907_c93404d7d4）
BASE_COMMIT=745ab25
TARGET_BLOB=.claude/skills/uc/SKILL.md:<開工時回填>;.claude/skills/normal/SKILL.md:<開工時回填>;.claude/scripts/activate_ticket.sh:NEW

**MAX_LINES=N/A 附註**（TPL v3.5 R3）：AI-1 含 fail-closed 守門判定（票檔定位歧義、缺欄、票號不符三類一律非零 exit 拒絕 activation），失敗形狀枚舉為審查發現式，不可先驗估算。

## 檔案治理／來源說明

**問題（草案 §3.3 之 Desktop 側殘餘）**：`tmp/verify_cmd.conf` 為 `/uc` 收尾 SOP 之 `TICKET_ID` 唯一來源（`uc/SKILL.md` 輸出規則 8：MUST 自該檔以 `grep/head/cut` 讀取、MUST NOT 猜測）。手機 lane 已由 `ticket-run` 10.1 於派工時恆覆寫該檔；**Desktop lane 無對應通道**——`/uc`、`/normal` 現行均不接受票號參數（`uc/SKILL.md` 全文無 `$ARGUMENTS`；`normal/SKILL.md` 僅 168 B 模式切換），寫入依 CLAUDE.md L17 之模型遵從。後果與 `MOBCD-04` L113 所載同形：Desktop 開 A 票後 `/uc` 讀到殘留票 ID，收尾 SOP（勾 AC＋lifecycle sync＋append_note）套用於錯票，**不報錯、不中斷**。

**快照遮蔽（第一手，本票新增取證）**：`qa/scripts/verify_checklist.sh` L250–254——先自 legacy conf 讀 `REVISION`（缺省 `rev-0`）組出 `tmp/verify/<ID>.<REVISION>.conf`，**該檔存在即全欄改讀快照**（L255–266），新寫入之 legacy conf 被完全遮蔽。故同票二次 activation（rebase 後 `DIFF_REF` 變動）若不處理快照，新值不生效。本票以 activation 時刪除該票快照解決，**零觸碰 QA 腳本**（`verify_checklist.sh` ∈ FORBIDDEN）。

**恆覆寫之依據（Akira 2026-09-07 裁決）**：票面 Verify Config 為 SSOT，conf 為其衍生物；使用者鍵入 `/uc <ID>` 即為裁決，不設「讀舊 conf 再比對再決定」分支。錯 conf 之失敗形狀由 `/qa` scope 檢查與 `git diff --stat` AC 攔截，五分鐘內 `git restore` 可恢復（§0）。

**範圍外**：草案 §8.4–§8.9（checkpoint 消費、baseline 比對、語義探針、attribution、evidence 落檔）、§9 全節、§11 profile 均不落本票。本票僅覆蓋 §8.1–§8.3（Ticket Source Resolution／Identity／Runtime Activation）之精簡形。`preflight_activation.sh` 之 V0/V1/V2 一律不引用（該線已出案）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/uc/SKILL.md`、`.claude/skills/normal/SKILL.md`、`.claude/scripts/`
- **動作**:
  - A：`grep -c 'ARGUMENTS' .claude/skills/uc/SKILL.md .claude/skills/normal/SKILL.md` 兩檔均為 0（未先行）
  - B：`grep -n '^## 進入確認' .claude/skills/uc/SKILL.md` 恰 1 次（AI-2 插入錨）；`grep -n '^# Prompt' .claude/skills/normal/SKILL.md` 恰 1 次（AI-3 插入錨）
  - C：`.claude/scripts/activate_ticket.sh` 不存在；`git ls-files .claude/scripts/m3_family_audit.sh` 有輸出（該目錄受追蹤之正向取證）
  - D：TPL v3.8 三分流——`git diff --name-only 745ab25..HEAD -- .claude/skills/uc/SKILL.md .claude/skills/normal/SKILL.md .claude/scripts/` 為空 → 續作；非空 → 依三分流歸因回報，禁推定等價位置
- **任一項不符 → 停止回報。**
- [x] 完成（A/B/C/D 四項於開工當下實測全符，見本 session transcript）

### AI-1: 新建 `activate_ticket.sh`
- **檔案**: `.claude/scripts/activate_ticket.sh`（新建）
- **目標值**（行為契約，逐條 MUST）:
  1. **檔頭 Usage 註解**：載明 `activate_ticket.sh <TICKET_ID>`、三類非零 exit 與其意義（供 `uc/SKILL.md`／`normal/SKILL.md` 非零 exit 時擷取 stderr 首行回報之訊息來源）。
  2. **fail-closed**：與 `session_start.sh` 之恆 `exit 0` 相反——本腳本為主動派工，任一前提不成立 MUST 以非零 exit 拒絕，且 MUST NOT 寫入或清空任何檔案。
  3. `REPO_ROOT=$(git rev-parse --show-toplevel)`；失敗 → exit 2。無參數 → 印 Usage、exit 2。
  4. **票檔定位**：於 `plans/cso_briefing/project/Pending/` 與 `deliver/` 依序找 `<TICKET_ID>_*.md`。命中恰 1 → 續；命中 0 或 ≥2 → 印候選清單、exit 3（禁自選）。
  5. **九欄擷取**：`grep -E '^(TICKET_ID|CREATED|PRESET|ALLOWED|FORBIDDEN|MAX_LINES|ANCHOR_PROBED|BASE_COMMIT|TARGET_BLOB)='`——欄位集與 regex MUST 與 `ticket-run/SKILL.md` L96 字面一致（禁另立欄位集）。`TICKET_ID` 或 `ALLOWED` 缺值 → exit 4，MUST NOT 以空值寫入 conf（比照 10.1 三分支歸因之「缺 Verify Config 段」處置）。其餘 7 欄（`CREATED`／`PRESET`／`FORBIDDEN`／`MAX_LINES`／`ANCHOR_PROBED`／`BASE_COMMIT`／`TARGET_BLOB`）MUST 依票面原樣透傳，缺值時 MUST NOT 觸發 exit（僅 `TICKET_ID`／`ALLOWED` 為 fail-closed 校驗對象；`MAX_LINES=N/A` 屬合法值非缺值）。
  6. **票號自洽**：票面 `TICKET_ID` ≠ 傳入參數 → exit 5。
  7. **切票通知**：舊 `tmp/verify_cmd.conf` 存在且其 `TICKET_ID` 與新值不同 → stdout 印 `🔀 切票：<舊> → <新>`；**僅通知，不停止**。
  8. **恆覆寫**：`mkdir -p tmp` 後以九欄＋`DIFF_REF=$(git rev-parse --short HEAD)` 覆寫 `tmp/verify_cmd.conf`（`mode` 等同 `>`，禁 append、禁「已存在即跳過」）。
  9. **快照清除**：`rm -f "tmp/verify/${TICKET_ID}."*.conf`，並印一行 `🧹 已清除 <ID> 之 per-ticket 快照 N 筆`（N=實際刪除數，0 亦印）。MUST NOT 刪他票快照、MUST NOT 刪 `tmp/verify/active.json`。
  10. **成功輸出**：`✅ ACTIVATED | <TICKET_ID> | <branch> @ <short HEAD> | ALLOWED=<欄位前 60 字元>`，exit 0。
  11. 依賴上限：`git`／`grep`／`sed`／`ls`／`wc`／`rm`；MUST NOT 引入 `jq`／`python`。
- [x] 完成（commit `e962b40`；行為測試見 AC 逐項）

### AI-2: `/uc` 接受票號參數
- **檔案**: `.claude/skills/uc/SKILL.md`
- **現值**: `## 進入確認`（唯一）
- **目標值**: 於該行**之前**插入新節 `## Phase 0：Ticket Activation（帶參數呼叫時）`，內容三條：
  1. `$ARGUMENTS` 非空 → **MUST** 先執行 `bash .claude/scripts/activate_ticket.sh "$ARGUMENTS"`；非零 exit → 回報 `⛔ UC Activation STOPPED: <腳本 stderr 首行>` 並**不進入臥底模式**。
  2. `$ARGUMENTS` 為空 → **MUST** 維持現行行為（直接進入臥底模式，`tmp/verify_cmd.conf` 沿用現況），MUST NOT 因缺參數報錯。
  3. 本節 **MUST** 早於輸出規則 8 之 `TICKET_ID` 讀取；activation 成功後該讀取之來源即為本節重生成之 conf。
- 既有輸出規則 1–9、驗證性動作豁免、唯一例外三節 **MUST** 位元組級不變。
- [x] 完成（commit `e962b40`；`git diff` 確認僅插入新節，原文位元組級不變）

### AI-3: `/normal` 接受票號參數
- **檔案**: `.claude/skills/normal/SKILL.md`
- **現值**: `# Prompt\n退出臥底模式。恢復正常回應格式：可以解釋、討論架構、提供建議。`
- **目標值**: 於 `# Prompt` 段後追加新節 `## Phase 0：Ticket Activation（帶參數呼叫時）`，內容三條：
  1. `$ARGUMENTS` 非空 → **MUST** 先執行 `bash .claude/scripts/activate_ticket.sh "$ARGUMENTS"`；非零 exit → 回報 `⛔ Normal Activation STOPPED: <腳本 stderr 首行>`，但**仍繼續**退出臥底模式（不宣告 activation 成功，但模式切換本身 MUST NOT 因此被阻斷，見 Hard Constraint 6）。
  2. `$ARGUMENTS` 為空 → **MUST** 維持現行行為（直接退出臥底模式），MUST NOT 因缺參數報錯。
  3. activation 成功（exit 0）時，`activate_ticket.sh` 自身之 `✅ ACTIVATED` stdout **MUST** 原樣透出，`/normal` **MUST NOT** 額外吞掉或改寫該行。
- frontmatter 之 `name`／`description`／`disable-model-invocation` **MUST** 不變。
- [x] 完成（commit `e962b40`；`git diff` 確認 frontmatter 未觸碰）

## Hard Constraints
1. 腳本寫入標的 **MUST** 限 `tmp/verify_cmd.conf` 與 `tmp/verify/<本票 ID>.*.conf` 之刪除；MUST NOT 寫 `.claude/`、`plans/`、`app/` 或任何版控檔案。
2. **MUST NOT** 呼叫 `preflight_activation.sh`、**MUST NOT** 引用 V0/V1/V2 判定結果（該線已出案）。
3. **MUST NOT** 修改 `ticket-run` 任何檔案——PLT／APP 兩側現為 byte-identical，改動即製造同步債。
4. **MUST NOT** 建立 lock 檔、session token 或任何新狀態載體；`tmp/verify_cmd.conf` 為既有檔之同步。
5. 腳本 **MUST NOT** 於失敗路徑刪除或清空既有 `tmp/verify_cmd.conf`（保留舊 activation 優於留下空檔）。
6. `/normal` 之模式切換語義 **MUST NOT** 因 activation 失敗而被阻斷（見 AI-3）。

## Error Handling
- AI-0 A 任一檔 `ARGUMENTS` 命中 ≥1 → 停止回報（已先行）
- AI-0 C 之 `git ls-files .claude/scripts/m3_family_audit.sh` 無輸出 → 停止回報（目錄未受追蹤，新腳本將不入版控）
- `ticket-run/SKILL.md` L96 九欄集與本票 AI-1 條 5 不一致 → 停止回報（禁自行對齊，須先確認何者為新）
- **[標配]** `git check-ignore -v .claude/scripts/activate_ticket.sh .claude/skills/uc/SKILL.md .claude/skills/normal/SKILL.md` 命中 → 停止回報（gitignore 死結律 #20）

## Acceptance Criteria
- [x] **[存在性]** `activate_ticket.sh` 在盤、`git ls-files` 追蹤、檔頭含 Usage 註解；`grep -c 'jq\|python' ` ＝ 0；其九欄 regex 與 `ticket-run/SKILL.md` L96 字面一致（附兩側 grep 輸出對照）——腳本 L42 `^(TICKET_ID|CREATED|PRESET|ALLOWED|FORBIDDEN|MAX_LINES|ANCHOR_PROBED|BASE_COMMIT|TARGET_BLOB)=` 與 `ticket-run/SKILL.md:96` 字面相符，`grep -c 'jq\|python'` = 0
- [x] **[可達性]** `/uc` 收尾 SOP 之 `TICKET_ID` producer 為本腳本寫入之 `tmp/verify_cmd.conf`（證據欄：`uc/SKILL.md:48` 讀取點 ＋ `activate_ticket.sh:68-83` 寫入 block）
- [x] **[行為性]** given 存在之票號 `INFRA-CTAWR-PHASE0-01` 且 `tmp/verify_cmd.conf` 原屬他票，when 執行腳本，expect stdout 含 `🔀 切票：`、exit 0、conf 之 `TICKET_ID` 與 `ALLOWED` 等於票面值、`DIFF_REF` 等於 `git rev-parse --short HEAD`（實測通過，見 session transcript）
- [x] **[行為性]** given `tmp/verify/<ID>.rev-0.conf` 存在，when 執行腳本，expect 該檔消失且 stdout 含 `🧹`；同時 `tmp/verify/<他票 ID>.*.conf` 與 `tmp/verify/active.json` 仍在（實測通過）
- [x] **[行為性]** given 不存在之票號，when 執行腳本，expect 非零 exit 且 `tmp/verify_cmd.conf` 內容位元組級不變（fail-closed 不破壞既有 activation）（實測通過，`diff` 確認位元組級不變）
- [x] **[行為性]** given 無參數之 `/uc` 與 `/normal`，expect 二者行為與改動前一致（進入／退出臥底模式，無 activation 訊息、無錯誤）（`git diff` 確認 Phase 0 節僅追加、原文位元組級不變，且邏輯上僅在 `$ARGUMENTS` 非空時觸發）
- [ ] **[行為性]** given 存在之票號，when 執行 `/normal <ID>`，expect `tmp/verify_cmd.conf` 被覆寫為該票之九欄值，且無論 activation 成功或失敗均正常退出臥底模式（驗證 Hard Constraint 6：模式切換不得被 activation 失敗阻斷）——**未實機執行 `/normal <ID>`**，僅邏輯核驗（文字明定 Hard Constraint 6），誠實保留未勾，列入 K3-Rule 25 未覆蓋路徑
- [ ] **[實機]** VSCode 擴充 host 下，本機與手機 Remote Control 各執行一次 `/uc <ID>` 與 `/normal <ID>`，expect 均見 `✅ ACTIVATED` 且後續 `/qa` 讀到正確 `TICKET_ID`（transcript 片段存收尾）——未實測，誠實保留未勾
- [x] ~~`assembleDebug` PASS~~（N/A：純 `.claude/` 工具鏈，`/sv` 階段 A 仍執行 `assembleDebug` 作基線 PASS）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`745ab25..e962b40` 僅 3 個 ALLOWED 檔 + `.claude/PROGRESS.md`，後者依 INFRA-GOVSYNC-01 不計入違規判定）

## 結案授權（2026-09-08，Akira 直接裁決）

**Akira 逐字裁決**：「授權直接跳過SV進行進行結案歸檔...並因為它不是相關 App 的主程式，再加上現在的 QA SV code review 程序有問題，所以逢跑必擋。而且 bug 一堆、隨便亂擋，所以不要浪費時間，直接跳過。」

**覆寫範圍**：本裁決明確覆寫 `CLAUDE.md` SOP「本 repo 全程序執行，無 RAPID／快速通道；任何票均不得跳過 3./qa、4./sv、4.5.cross_review（Akira 2026-08-22 裁決）」條款，僅適用本票；不視為對該 SOP 條款之永久修訂。

**背景**：本票已完成 `/qa`（Build/Test PASS）+ `/sv` 階段 A-C（compile/lint/scope/orphan/mock/reachability 皆 PASS，見已刪除之 `sv_report.md` 快照）；階段 D Cross-Review 於 R0 預審卡在 `CHECKPOINT-REQUIRED`（`.tmp/fail-advisor/R0_INFRA-CTAWR-PHASE0-01_20260908_154022.md`：commit `e962b40` 同時含 `.claude/PROGRESS.md` 與 `.sh` 交付物，觸發 PROGRESS.md 條件豁免要件 1 之規則缺口，`TRUE-POSITIVE-RULE-GAP`，非本票程式碼缺陷）。Codex 實際審查**未執行**（R0 攔於 codex exec 之前）。

**`reviews.codex` 填值**：`N_A`（未執行，非執行後 PASS；理由＝本裁決）。

**End of INFRA-CTAWR-PHASE0-01**
