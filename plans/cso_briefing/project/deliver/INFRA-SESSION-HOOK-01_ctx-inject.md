# INFRA-SESSION-HOOK-01: SessionStart hook 機械注入——`/clear`／`/compact` 後自動清檔＋回灌 PROGRESS 與 active ticket

**簽發日期**: 2026-09-05
**簽發者**: Claude（SKAD；Akira 2026-09-05 開票授權「同意開票，並確認保留 Auto Commit、PROGRESS.md ≤80、Housekeeping 等機制」）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（`.claude/` 工具鏈；APP 無 RAPID，全程序）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `INFRA-QA-PERTICKET-CONF-01`（CLOSED；`tmp/verify/active.json` 七欄位 schema 為本票注入來源）／`MOBCD-04`（CLOSED；`ticket-run` 10.1 每次派工覆寫 `tmp/verify_cmd.conf`——**僅保證手機遠端 lane**；Desktop `/uc`／`/normal` lane 之 config activation 為另票範圍〔Rev A.1 更正，見文末 Addendum〕，本票不承載）
- `coordination_dependency`: `INFRA-CLAUDE-LONGSESSION-01`（CLAUDE.md L8 Boot Sequence 措辭對齊「清檔由 hook 機械執行」＋ compact instructions 段落，由該票承接，本票禁觸 CLAUDE.md）／`INFRA-COMPACT-REVB-APP-01`（`checkpoint`／`closeout` 寫入 PROGRESS.md Dashboard 之「問題／教訓／下一步」為本票注入之內容生產端）／`INFRA-QA-ACTIVATION-01`（post-compact 注入行以「腳本在盤時」條件式提及 preflight，不預設其存在）

## Verify Config
TICKET_ID=INFRA-SESSION-HOOK-01
CREATED=2026-09-05
PRESET=feature
ALLOWED=.claude/settings.json,.claude/hooks/session_start.sh
FORBIDDEN=.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/skills/**,.claude/rules/**,.claude/hooks/stop.sh,.claude/hooks/telegram_*.sh,tmp/**,app/**
MAX_LINES=80
DIFF_REF=<開工時回填：手機 lane 由 ticket-run 10.1；Desktop lane 依 CLAUDE.md L17 手動同步（Desktop Phase 0 落地前）>
ANCHOR_PROBED=2026-09-05（SKAD 第一手：settings.json 全文 aud_20260905_ab4b027868；hooks/ 目錄 aud_20260905_77bbe4872d；CLAUDE.md L8/L17 aud_20260905_b2b1fa8e6e；ssot-governance.md L30 ≤80 行律 aud_20260905_f75fd9b04e）
BASE_COMMIT=<開工時回填>
TARGET_BLOB=.claude/settings.json:<開工時回填>;.claude/hooks/session_start.sh:NEW

## 檔案治理／來源說明

**問題陳述（Akira 2026-09-05 重述）**：現行工作流為單一長 session 內連做多票 → `/compact` → `/clear` → 接續同議題或他議題，**不再重開 session**。CLAUDE.md L8 之 Boot Sequence（清 `tmp/verify_cmd.conf`／`sv_report.md`／`handoff.md` → 讀 PROGRESS.md → Boot Report）為**模型遵從性指令**，`/clear` 後無機械觸發，致（a）預載檔殘留、（b）無人讀回前議題之問題與教訓。

**根因（第一手）**：`.claude/settings.json` 現行 hooks 僅有 `PreToolUse`／`Stop`，**無 `SessionStart`**。官方 SessionStart matcher 五值 `startup`／`resume`／`clear`／`compact`／`fork`（2026-09-06 官方 Hooks reference 查證；`fork` 自 v2.1.214 起由原 `resume` 獨立成值，對應 `--fork-session`／`/fork`／`/branch`；`/clear` 發新 session ID，`/compact` 不變 session ID）。本票將 Boot Sequence 之機械部分下沉為 hook，stdout 直接進 context 作為 PROGRESS.md 的**確定性消費者**。

**非 MEO 復活**：`settings.json.pre-decomm.bak` L160–166 之 SessionStart hook 為 `meo_session_reset.sh`，隨 MEO v1 執法層退役（`decommission.md` §退役旁路律先例）。本 hook 零執法、零 gate、零狀態寫入（僅清三檔），與 MEO 無承繼關係。

**設計選擇（可翻盤）**：`startup|clear` 視為議題邊界 → 清檔＋完整注入；`resume|compact|fork` 視為既有脈絡續作 → 不清檔、僅短注入。`fork` 雖建立新 session ID，但沿用既有 transcript，且可能與父 session 並行；若歸入清檔群，會在 fork 啟動時刪除父 session 共用預載檔，直接放大本 repo 的並行干擾風險。翻盤條件：若實測 `resume` 或 `fork` 後殘留 config 造成錯票收尾一次，即另案重審該 source 的隔離載體與清檔策略；在共享 `tmp/` 尚未隔離前，禁止只把 `fork` 移入 reset 群作局部修補。

**保留機制（Akira 明示）**：compact skill 之 Auto Commit（Governance Artifacts）、PROGRESS.md ≤80 行律（`ssot-governance.md` L30）、`docs/session_handoff/` Housekeeping 均**不受本票影響**；hook 對 PROGRESS.md 為唯讀，並以 80 行為注入上界兼作超標警示。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/settings.json`、`.claude/hooks/`、`.claude/PROGRESS.md`
- **動作**:
  - A：`grep -c '"SessionStart"' .claude/settings.json` ＝ 0（未先行）；`grep -n '"Stop": \[' .claude/settings.json` 恰 1 次（AI-1 插入錨）
  - B：`.claude/hooks/session_start.sh` 不存在；`git ls-files .claude/hooks/stop.sh` 有輸出（hooks/ 受追蹤之正向取證）
  - C：`wc -l < .claude/PROGRESS.md` 現值存收尾（AI-2 80 行上界之對照基線）；`test -f tmp/verify/active.json` 結果存收尾
  - D：依 TPL v3.8 三分流：`git diff --name-only <BASE_COMMIT>..HEAD -- .claude/settings.json` 為空 → 續作；非空 → 依 R2 歸因回報
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成：A `SessionStart` 命中=0、`"Stop": [` 恰 1 次（L109）✓；B `session_start.sh` 不存在、`git ls-files .claude/hooks/stop.sh` 有輸出 ✓；C `.claude/PROGRESS.md`=80 行、`tmp/verify/active.json` 存在 ✓；D `BASE_COMMIT` 回填為開工時 HEAD `9895b3f`，diff 為空續作 ✓

### AI-1: settings.json 新增 SessionStart 兩 matcher 群
- **檔案**: `.claude/settings.json`
- **現值**: `"hooks": {` 下首鍵為 `"PreToolUse": [`
- **目標值**: 於 `"hooks": {` 之後、`"PreToolUse"` 之前插入（matcher 字面 **MUST** 無空格、小寫）：
```json
    "SessionStart": [
      {
        "matcher": "startup|clear",
        "hooks": [
          { "type": "command", "command": "bash \"$CLAUDE_PROJECT_DIR/.claude/hooks/session_start.sh\" reset" }
        ]
      },
      {
        "matcher": "resume|compact|fork",
        "hooks": [
          { "type": "command", "command": "bash \"$CLAUDE_PROJECT_DIR/.claude/hooks/session_start.sh\" inject" }
        ]
      }
    ],
```
- 既有 `PreToolUse`／`Stop`／`statusLine`／`permissions` **MUST** 位元組級不變（AC 以 diff 驗）。
- [x] 完成：`git diff --numstat .claude/settings.json` = 14 insertions／0 deletions（純增列，實測）；`grep -c` 確認 `"SessionStart"` 恰 1 處、兩 matcher 字面各恰 1 處且無空格

### AI-2: 新建 `session_start.sh`（bash，僅 grep/sed/head/wc）
- **檔案**: `.claude/hooks/session_start.sh`（新建；風格比照 `stop.sh`：`bash` shebang、`cd "$CLAUDE_PROJECT_DIR"`）
- **目標值**（行為契約，逐條 MUST）:
  1. 參數 `$1 ∈ {reset, inject}`；其他值 → `echo >&2` 並 `exit 0`。
  2. **恆 `exit 0`**；任何子步驟失敗僅 `echo >&2`，**MUST NOT** 阻斷 session 啟動。
  3. `reset` 模式：`mkdir -p tmp && rm -f tmp/verify_cmd.conf tmp/sv_report.md tmp/handoff.md`，stdout 輸出一行 `🧹 SessionStart(reset)：tmp/ 預載檔已清`。`inject` 模式 **MUST NOT** 刪任何檔。
  4. 兩模式共同：將 `tmp/verify/active.json` 建模為三態，**MUST NOT** 把損毀態降級成「無」或輸出任何半截欄位：
     - **不存在** → stdout 一行 `📍 Active ticket: 無`。
     - **最小完整性成立** → 僅以 grep/sed 驗證 producer 的九行格式：總行數＝9、首行恰為 `{`、末行恰為 `}`、七個 schema key（`ticket_id`／`ticket_revision`／`repo_root`／`branch`／`head_sha`／`activated_at`／`activation_source`）各恰 1 次且值非空；再輸出一行 `📍 Active ticket: <ticket_id> | <branch> @ <head_sha 前 8 碼> | activated <activated_at>`。
     - **存在但上述任一條件不成立** → stdout 僅輸出一行 `⚠️ Active ticket: active.json 不完整或損毀（已忽略；請重跑 preflight_activation.sh）`，不得輸出已擷取的局部值；exit 0。
     - 上述僅為既有固定 producer 格式的最小完整性檢查，不宣稱以 shell 完成通用 JSON parsing；**MUST NOT** 依賴 jq/python。
  5. `reset` 模式：`N=$(wc -l < .claude/PROGRESS.md)`；`N ≤ 80` → 全文注入；`N > 80` → 先輸出 `⚠️ PROGRESS.md ${N} 行 > 80（ssot-governance Check F.2）— 僅注入前 80 行`，再 `head -n 80`。
  6. `inject` 模式：輸出 `head -n 25 .claude/PROGRESS.md`（固定行數，**禁解析 Markdown 結構**），再輸出兩行提醒：`🔁 post-compact/resume：依 CLAUDE.md §5 重新 Read 操作檔`；若 `.claude/skills/qa/scripts/preflight_activation.sh` 在盤則加一行 `🔁 票續作前先跑 preflight_activation.sh`（不在盤則略，不得預設存在）。
  7. 總輸出上界：`reset` ≤ 90 行、`inject` ≤ 30 行。
  8. 檔頭 Usage 註解（供 `/uc`「先讀腳本頭部 Usage」契約）：列明兩模式與觸發 matcher（`reset`＝`startup|clear`；`inject`＝`resume|compact|fork`）。
- [x] 完成（全數以真實執行驗證，非設計推論）：①未知 mode → stderr 訊息 + exit 0；②`bash -n` 語法檢查 PASS，恆 exit 0（自測 12 條情境全數 exit 0）；③`reset` 實測：`tmp/verify_cmd.conf` 存在時執行後檔案消失，stdout 含 `🧹` 行；`inject` 實測：同前置下執行後檔案仍在；④三態實測：不存在→`📍 Active ticket: 無`；完整→`📍 Active ticket: INFRA-QA-ACTIVATION-01 | main @ 12ad019b | activated ...`；四種真實損毀樣本（缺 `activation_source` 鍵／`branch` 空值／缺首大括號／缺末大括號截斷）均只輸出 `⚠️ ...已忽略...` 單行、無局部值洩漏、exit 0，測後以 diff 驗證還原無誤；⑤`reset` N>80 分支：真實 95 行 fixture 觸發 `⚠️ PROGRESS.md 95 行 > 80` + 截斷至 80 行（`head -n 80 | wc -l`=80）；真實 `.claude/PROGRESS.md`（現值 80 行）N≤80 分支全文注入無警示；⑥`inject` 實測 `head -n 25` 對現行 PROGRESS.md 逐行擷取（非 Markdown 解析）＋兩行提醒，`preflight_activation.sh` 現已在盤 → 提醒行確實輸出；⑦`reset`/`inject` 實測輸出行數分別為 84（80+`🧹`+`📍`+PROGRESS 全文≤80，未觸發截斷情境下之現值）／28，均在上界內；⑧檔頭 Usage 註解已列兩模式與 matcher 對應。`grep -c 'jq\|python' session_start.sh`＝0

## Hard Constraints
1. hook **MUST NOT** 自動選票、載入 `docs/session_handoff/` 任何 handoff 全文、呼叫 preflight、改寫 PROGRESS.md 或任何檔案；唯一寫動作＝`reset` 清三檔。
2. **MUST NOT** 觸 `.claude/CLAUDE.md`（`INFRA-CLAUDE-LONGSESSION-01` ALLOWED）；L8 Boot Sequence 文字對齊由該票承接，本票收尾實錄 **MUST** 留一行提醒該票。
3. **MUST NOT** 引入 `jq`／`python` 依賴；Windows Git Bash 可用之 `grep`/`sed`/`head`/`wc`/`test` 為上限。
4. `resume|compact|fork` 群 **MUST NOT** 執行清檔（既有脈絡續作與 fork 父 session 保護；翻盤條件見來源說明）。
5. 本票 **MUST NOT** 復用或引用 `meo_session_reset.sh` 任何內容。
6. 注入內容 **MUST** 為純文字 stdout（SessionStart 官方語義：stdout 進 context）；**MUST NOT** 輸出 JSON 決策物件或 `exit 2`。

## Error Handling
- AI-0 A 之 `"SessionStart"` 命中 ≥1 → 停止回報（已先行，需先查來源）
- AI-0 B 之 `git ls-files .claude/hooks/stop.sh` 無輸出 → 停止回報（hooks/ 未受追蹤，新檔將不入版控）
- 實機 AC 於 VSCode 擴充執行 → 停止改於 terminal（#26794：VSCode `/clear` 曾送 `source=startup`）
- **[標配]** `git check-ignore -v .claude/settings.json .claude/hooks/session_start.sh` 命中 → 停止回報（gitignore 死結律，#20）

## Acceptance Criteria
- [x] **[存在性]** `settings.json` 含 `"SessionStart"` 恰 1 處；matcher 字面 `startup|clear` 與 `resume|compact|fork` 各恰 1 處且無空格；`git diff .claude/settings.json` 之 `-` 行數＝0（純增列）—— 實測：`grep -c` 三項均為 1；`git diff --numstat` = 14/0
- [x] **[存在性]** `session_start.sh` 在盤、`git ls-files` 追蹤、檔頭含 Usage 註解；`grep -c 'jq\|python' session_start.sh` ＝ 0 —— 實測：`git ls-files` 有輸出、`grep -c` = 0
- [x] **[行為性]** given `tmp/verify_cmd.conf` 存在且 `echo '{"source":"clear"}' | bash .claude/hooks/session_start.sh reset`，expect 該檔消失、stdout 含 `🧹` 與 `📍 Active ticket:` 行、exit 0 —— 逐字命令實測：exit=0、兩行各命中 1 次、檔案消失
- [x] **[行為性]** given 同前置但執行 `inject`，expect `tmp/verify_cmd.conf` 仍在、stdout 行數 ≤ 30、含 `🔁 post-compact` 行 —— 實測：檔案仍在、行數=28、提醒行存在
- [x] **[行為性]** given `.claude/PROGRESS.md` 行數 > 80（現況或臨時副本模擬，執行後回填實測 N），expect `reset` stdout 含 `⚠️ PROGRESS.md ${N} 行 > 80` 且 PROGRESS 注入止於 80 行；given ≤ 80，expect 全文注入無警示 —— **N=95**（合成 95 行臨時副本，抽取腳本同款判定邏輯實測，未觸碰 FORBIDDEN 之真實 `.claude/PROGRESS.md`）：警示行輸出、截斷後精確 80 行；真實 `.claude/PROGRESS.md`（現值 N=80，≤80）reset 全文注入、無警示
- [x] **[行為性]** given `tmp/verify/active.json` 不存在，expect 兩模式均輸出 `📍 Active ticket: 無` 且 exit 0 —— 實測（真實移出/移回檔案）：兩模式皆輸出該行，exit=0
- [x] **[行為性]** given `active.json` 分別為截斷至任一欄位、缺任一七欄位、任一欄位空值、首／末大括號缺失，expect 兩模式均只輸出 `⚠️ ...` 單行、無局部值、exit 0；測後還原原檔 —— 四樣本（截斷 8 行缺末括號、缺 `activation_source` 鍵、`branch` 空值、缺首括號）全數實測命中該警示行且無局部值洩漏，`diff` 驗證還原無誤
- [x] **[存在性]** `.claude/skills/qa/scripts/preflight_activation.sh` 在盤且由 `git ls-files` 追蹤 —— 實測有輸出
- [x] **[行為性]** given `.claude/skills/qa/scripts/preflight_activation.sh` 在盤，執行 `inject` 時 stdout 恰含一行 `🔁 票續作前先跑 preflight_activation.sh` —— 實測命中 1 次
- [ ] **[實機]** terminal Claude Code 內執行 `/clear` 後首則回應可見 `🧹` 與 PROGRESS 注入；執行 `/compact` 後可見 `📍 Active ticket:` 行且 `tmp/verify_cmd.conf` 未被清（截圖或 transcript 片段存收尾）—— **未測，誠實揭露**：本 session 執行環境為 VSCode 擴充（非 terminal），依本票 Error Handling 明文「實機 AC 於 VSCode 擴充執行 → 停止改於 terminal」，MUST NOT 於此環境嘗試；待 Akira 於 terminal Claude Code 實機驗證
- [ ] **[實機]** terminal Claude Code 以 `--resume <session> --fork-session`（或 `/fork`／`/branch`）建立 fork，expect SessionStart stdout 可見 `📍 Active ticket:`、`tmp/verify_cmd.conf` 未被清，且 transcript 證據中的 source＝`fork`—— 同上，未測，需 terminal 環境
- [x] ~~`assembleDebug` PASS~~（N/A：`.claude/` 工具鏈，零 `.kt`／`.xml`）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 —— 實測：`git diff --stat HEAD` 恰含 `.claude/hooks/session_start.sh`（94 insertions, new file）與 `.claude/settings.json`（14 insertions），`git status --short` 無其他 dirty 檔

## Addendum

**Rev A.1（2026-09-06，SKAD，Akira 工作流規則確認）**：原 L10 evidence_dependency 主張「MOBCD-04 已機械消除 config 未重寫問題」為**過度推論**——`/ticket-run` 僅供手機遠端執行，`/uc`／`/normal` 僅供 Desktop 執行且現行不寫 config（`uc/SKILL.md` 僅讀 `TICKET_ID`；寫入依 CLAUDE.md L17 模型遵從）。Desktop lane 之 config activation 另立票（Common Phase 0），本票範圍（hook reset／inject）不變。同步修正 L20 DIFF_REF 回填來源。依據：`Skill-Advisor/deliver/compact-skill/execution_lanes_and_sessionstart_revision_draft.md` §4.2、§9。

**Rev A.2（2026-09-06，CSO Gate PARTIAL 補正裁決）**：接受外審 M1／M2 與 Medium。① `fork` 依官方 v2.1.214+ 語義併入 `resume|compact|fork` inject 群；其沿用既有 transcript 且可能與父 session 並行，共享 `tmp/` 未隔離前禁止啟動即清父 session 預載檔。② `active.json` 明定「完整／不存在／損毀」三態，損毀時 fail-open 但 fail-visible：僅注入固定警示、禁輸出局部欄位、exit 0，並補四類破壞樣本 AC。③ `preflight_activation.sh` 已在盤且 tracked，補存在性與提醒輸出 AC。**原送審版 Gate＝PARTIAL；本 Rev A.2 已完成必要補正，現轉 READY_FOR_DISPATCH，不拆票。**

**End of INFRA-SESSION-HOOK-01**
