# INFRA-CTAWR-CKPT-TRIGGER-01: checkpoint 觸發時機條文化＋PROGRESS.md 過期可見化

**簽發日期**: 2026-09-11
**簽發者**: Claude（SKAD；Akira 2026-09-11「同意開票」）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（純 `.claude/` 工具鏈，零 `.kt`／`.xml`）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `INFRA-COMPACT-REVB-APP-01`（CLOSED；交付 `compact/SKILL.md` Rev B 三模式，其 §4.2／§4.3 定義 checkpoint 做什麼，**未定義何時被呼叫**——本票補該缺口）／`INFRA-SESSION-HOOK-01`（CLOSED；交付 `session_start.sh`，其 `reset` 分支無條件注入 PROGRESS.md，不論其是否過期）
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=INFRA-CTAWR-CKPT-TRIGGER-01
CREATED=2026-09-11
PRESET=docs
ALLOWED=.claude/skills/compact/SKILL.md,.claude/hooks/session_start.sh
FORBIDDEN=.claude/PROGRESS.md,.claude/settings.json,.claude/CLAUDE.md,.claude/rules/**,.claude/skills/uc/**,.claude/skills/normal/**,.claude/skills/qa/**,.claude/scripts/**,app/**
MAX_LINES=N/A
DIFF_REF=<開工時回填>
ANCHOR_PROBED=2026-09-11（SKAD 第一手：`session_start.sh` 全文 aud_20260911_c0dcfa3d4d；`compact/SKILL.md` 節構 aud_20260911_7a5d5d4a9b／aud_20260910_cd0a8a015c；`.claude/PROGRESS.md` 節構 aud_20260910_45b9b4bec6；草案 §10.1 全文 aud_20260911_da0676c2cc）
BASE_COMMIT=1566d6d
TARGET_BLOB=.claude/skills/compact/SKILL.md:<開工時回填>;.claude/hooks/session_start.sh:<開工時回填>
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**MAX_LINES=N/A 附註**（TPL v3.5 R3）：AI-2 修改 fail-open 守門腳本之輸出路徑（恆 `exit 0` 不變量），屬安全不變量票。

**條件 6 判定（TPL v3.10 生效後首張本線票）**：6a `INTRODUCED_CAPABILITY=NONE` → 不觸發；6b① ALLOWED 首層 path segment 去重後 ＝ `.claude` 單一（distinct=1 < 2）→ 不觸發；6b② `### AI-` 標題數 ＝ 2（AI-0 不計）< 8 → 不觸發；6b③ `DEPENDENCY_DELTA=NONE` → 不觸發；6c 二標的檔頭均無 `Revision` 欄位修訂歷史（`grep` aud_20260911_7a5d5d4a9b），依 6c ①「無 `Revision` 歷史者不納本款」→ 不觸發。**條件 6 未成立，得開執行票。**

## 檔案治理／來源說明

**問題（草案 §10.1 之殘餘缺口）**：草案 §10.1 列七項 checkpoint 產生時機（切換不相關 ticket 前、`/clear` 前、worktree 交接前、長暫停前、blocked/rebase、closeout 前、milestone 完成後）。`INFRA-COMPACT-REVB-APP-01` 交付之 `compact/SKILL.md` 定義三模式**行為**，但全檔無觸發時機條文；`§10.1 同一議題、同一 session`（L281）僅描述流程順序，非觸發判準。故「`/clear` 前先 checkpoint」現行完全依賴 Akira 記得手動呼叫。

**失效形狀（不對稱，本票核心理由）**：`session_start.sh` L63–76 之 `reset` 分支對 `.claude/PROGRESS.md` 為**無條件注入**——記得 checkpoint 則注入新鮮內容；忘記則注入過期內容**且無任何警示**。後者比無 hook 更糟：過期 Dashboard 取得了「當前狀態」的外觀，正是草案 §3.2 欲消除之失效形狀於生產端的殘留。

**設計選擇（最小化）**：以 mtime／commit time 比較作為過期訊號，**不解析 PROGRESS.md 內容**、不新增狀態檔、不新增閘。producer 為 `stat` 與 `git log -1 --format=%ct` 二者之數值比較，`grep` 可驗。訊號為**提示**非阻斷：`reset` 分支恆 `exit 0` 不變量（HC-1）不得因本票改變。

**範圍外**：草案 §10.2（消費時機）已由 `INFRA-CTAWR-PHASE0-01` 與 `INFRA-SESSION-HOOK-01` 覆蓋；§10.3（`/compact` 使用規則）依 2026-09-11 取證——Akira 現行工作流一律使用 APP `compact` skill，內建壓縮無常態觸發路徑，該節不落票。§11 Auto Commit profile 三檔維持不落票（現行 `/qa` code commit ＋ `/compact` governance commit 兩層已覆蓋）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/compact/SKILL.md`、`.claude/hooks/session_start.sh`
- **動作**:
  - A：`grep -c '觸發時機' .claude/skills/compact/SKILL.md` ＝ 0（未先行）；`grep -n '^## 10\. 操作流程' .claude/skills/compact/SKILL.md` 恰 1 次（AI-1 插入錨）
  - B：`grep -c 'PROGRESS_STALE\|舊於最後提交' .claude/hooks/session_start.sh` ＝ 0（未先行）；`grep -n 'progress_n=' .claude/hooks/session_start.sh` 恰 1 次（AI-2 插入錨，現行 L65）
  - C：`grep -n '^exit 0$' .claude/hooks/session_start.sh` 恰 1 次（恆 exit 0 不變量之位置基線）
  - D：TPL v3.8 三分流——`git diff --name-only 1566d6d..HEAD -- .claude/skills/compact/SKILL.md .claude/hooks/session_start.sh` 為空 → 續作；非空 → 依三分流歸因回報
- **任一項不符 → 停止回報。**
- [x] 完成 — A：`觸發時機`命中=0、`## 10\. 操作流程`唯一命中(L279)；B：`PROGRESS_STALE\|舊於最後提交`命中=0、`progress_n=`唯一命中(L66)；C：`^exit 0$`唯一命中(L94)；D：`git diff 1566d6d..HEAD`兩檔皆空，無drift；check-ignore未命中；pre-image `857862509fd95b4cab6df29328efe6cfb569b861`(compact/SKILL.md)/`dfdabbf88b8f4abb3b4612876bad0b087879bb2b`(session_start.sh)，BASE_COMMIT=1566d6d

### AI-1: `compact/SKILL.md` 新增「checkpoint 觸發時機」節
- **檔案**: `.claude/skills/compact/SKILL.md`
- **現值**: `## 10. 操作流程`（唯一）
- **目標值**: 於該行**之前**插入新節 `## 9.5 checkpoint 觸發時機`，內容為三條 **SHOULD**（草案 §10.1 七項收斂為現行工作流實際會遇到者）：
  1. 執行原生 `/clear` 前 SHOULD 先跑 `checkpoint` 模式——`/clear` 後 SessionStart `reset` 會注入 `.claude/PROGRESS.md`，未先 checkpoint 則注入內容為前次狀態。
  2. 於同一 session 內切換到不相關議題前 SHOULD 先跑 `checkpoint` 模式。
  3. 票 closeout 前 SHOULD 跑 `closeout` 模式（已由 §4.3 規範，本條僅作時機索引，不重複其行為規格）。
- 本節 **MUST** 標為 SHOULD 級提示，**MUST NOT** 寫成 MUST 或機械閘——無 producer 可機械判定「使用者是否即將 `/clear`」，寫成 MUST 即製造不可驗收之條文。
- 既有 §1–§10 各節、`## 冊面收束一致性閘`、`## Auto Commit: Governance Artifacts` 二節 **MUST** 位元組級不變。
- [x] 完成 — `## 9.5 checkpoint 觸發時機`已插入`## 10. 操作流程`之前，三條 SHOULD 齊備，節內`MUST`命中=0；`## 冊面收束一致性閘`／`## Auto Commit: Governance Artifacts` 二節逐字未動（僅因插入點在前而行號位移）

### AI-2: `session_start.sh` `reset` 分支新增過期提示
- **檔案**: `.claude/hooks/session_start.sh`
- **現值**（L65 起，`reset` 分支內）:
```bash
      progress_n="$(wc -l < .claude/PROGRESS.md 2>/dev/null | tr -d '[:space:]')"
```
- **目標值**: 於該行**之前**插入過期判定，並於 PROGRESS 注入**之前**輸出提示：
  1. `p_mtime="$(git log -1 --format=%ct -- .claude/PROGRESS.md 2>/dev/null)"`；`last_ct="$(git log -1 --format=%ct 2>/dev/null)"`。
  2. 二值均為純數字且 `p_mtime` < `last_ct` → `echo "⚠️ PROGRESS.md 舊於最後提交（可能未 checkpoint）——內容為前次狀態"`。
  3. 任一值取得失敗、非數字或相等 → **靜默跳過**，MUST NOT 輸出提示、MUST NOT 寫 stderr（避免 git 不可用時產生雜訊）。
- **實作約束**：僅得使用 `git log`（唯讀）；MUST NOT 使用 `stat`（Windows Git Bash 與 GNU `stat` 旗標不相容）。判定 MUST 僅存在於 `reset` 分支，`inject` 分支不變。
- 檔頭 Usage 註解之 `reset` 行 **MUST** 同步補述新增之過期提示（用法契約一致性）。
- 恆 `exit 0` 不變量、`report_active_ticket()` 函式、`inject` 分支 **MUST** 位元組級不變。
- [x] 完成 — 過期判定插入於 `progress_n=` 之前，用 `git log -1 --format=%ct` 二值比較，僅用 `git log`（禁 `stat`，`grep -c 'stat '`=0），任一值非數字或相等時靜默跳過；Usage 註解 reset 行已補述；`exit 0`／`report_active_ticket()`／`inject` 分支逐字未動；行為性三案（stale/equal/non-git）皆已 scratch 沙盒實測 PASS（見 AC 收尾記錄）

## Hard Constraints
1. **MUST NOT** 改變 `session_start.sh` 恆 `exit 0` 語義；過期提示為 stdout 純文字提示，**MUST NOT** 阻斷或改變任何既有輸出行之內容與順序（新增行置於 PROGRESS 注入前，不取代既有 `⚠️ PROGRESS.md N 行 > 80` 警示）。
2. **MUST NOT** 寫入、修改或建立任何檔案——本票二標的均為唯讀判定＋輸出；`.claude/PROGRESS.md` ∈ FORBIDDEN。
3. **MUST NOT** 新增狀態載體（lock 檔、快取、時戳檔）或解析 PROGRESS.md 內容。
4. **MUST NOT** 觸碰 `compact/SKILL.md` 之 `## 冊面收束一致性閘` 與 `## Auto Commit: Governance Artifacts` 二節（`INFRA-COMPACT-REVB-APP-01` 與 `INFRA-COMPACT-CLOSURE-GATE-APP-01` 之交付物，逐字保留）。
5. AI-1 之新增條文 **MUST NOT** 引入 lifecycle 狀態詞或第二套枚舉（`ssot-governance.md` L109「不新增第二套流程」）。

## Error Handling
- AI-0 任一 grep 命中數不符 → 停止回報
- `git log -1 --format=%ct -- .claude/PROGRESS.md` 於本 repo 回傳空值（PROGRESS.md 從未被提交）→ 停止回報（前提不成立，判定邏輯需改設計）
- **[標配]** `git check-ignore -v .claude/skills/compact/SKILL.md .claude/hooks/session_start.sh` 命中 → 停止回報（gitignore 死結律 #20）

## Acceptance Criteria
- [x] **[存在性]** `compact/SKILL.md` 含 `## 9.5 checkpoint 觸發時機` 恰 1 節，內含三條 SHOULD；`grep -c 'MUST'` 於該節範圍內 ＝ 0（僅計新增節） —— 實測：節內 `MUST` 命中=0，三條 SHOULD 齊備
- [x] **[存在性]** `compact/SKILL.md` 之 `## 冊面收束一致性閘` 與 `## Auto Commit: Governance Artifacts` 二節內容位元組級不變（`git diff` 該區段無 `+`／`-` 行） —— 實測：`git diff` 僅新增節前之插入，二節本身零 `+`／`-`
- [x] **[存在性]** `session_start.sh` 之 `grep -c 'stat '` ＝ 0；`grep -n '^exit 0$'` 仍恰 1 次 —— 實測皆符合
- [x] **[行為性]** given `.claude/PROGRESS.md` 之最後提交早於 repo 最後提交，when `bash .claude/hooks/session_start.sh reset`，expect stdout 含 `⚠️ PROGRESS.md 舊於最後提交` 且該行出現於 PROGRESS 內容之前、exit 0 —— 實測（真實 repo 現況，非合成）：`p_mtime=1789058950 < last_ct=1789103028`，警示行確實出現於 PROGRESS 內容前，exit 0
- [x] **[行為性]** given `.claude/PROGRESS.md` 即為最後一筆提交所含檔案，when 同前，expect stdout **不含**該提示行、其餘輸出與改動前逐行一致 —— 實測（scratch 沙盒 git repo，`p_mtime==last_ct=1789112221`）：無警示行，其餘輸出（`🧹`/`📍`/PROGRESS 內容）逐行一致，exit 0
- [x] **[行為性]** given 於非 git 目錄執行，when 同前，expect 無提示行、無 stderr 新增雜訊、exit 0 —— 實測（scratch 非 git 目錄）：無警示行、stderr 空、exit 0
- [x] **[行為性]** given `bash .claude/hooks/session_start.sh inject`，expect 輸出與改動前逐行一致（`inject` 分支未受汙染） —— 實測：inject 輸出無警示行、與改動前結構一致
- [x] ~~`assembleDebug` PASS~~（N/A：docs preset，純 `.claude/` 工具鏈）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 —— 實測：`git status --short` 僅 `compact/SKILL.md`／`session_start.sh` 兩檔

**End of INFRA-CTAWR-CKPT-TRIGGER-01**
