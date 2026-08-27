# WKTR-APPDEPLOY-01: 將 `wktree-close`／`wktree-compact` 兩 Skill 由 PLT 位元組級複製至 APP，並補齊其執行期資料目錄之版控排除

**簽發日期**: 2026-08-27
**簽發者**: Claude (SKAD)
**來源議題**: WKTR
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `Skill-Advisor/deliver/PLT_TO_APP_MIGRATION_INVENTORY_AND_DIRECTION_20260826.md` §3 第 6／7 項（兩 Skill 判定「可純複製」）＋ §1 D-3（PLT→APP 移轉方向裁決）；`INFRA-ENVSYNC-AUDIT-01`（`.claude/` 兩端 hash 同一性驗收方法先例）
- `coordination_dependency`: 待開之「APP-3 治理規則同步票」（票號未定；該票依裁決 A 亦動 `.gitignore`，**開立時 MUST 回填本欄並序列化 commit**）

## 檔案治理／來源說明

本票為 PLT→APP 單向成果移轉之第一張（APP-1）。源端為 `mcp-ticket-platform`（PLT）之 `.claude/skills/wktree-close/`、`.claude/skills/wktree-compact/`；標的為 `SmartCameraApp1`（APP）同名路徑。**性質：無功能面、無 production 標的、無 Kotlin/Java 異動**，AC 主體為兩端 `git hash-object` 逐檔相等。

**開票期 live 取證（2026-08-27 12:50，SKAD 第一手）**：

| 源端檔案 | size (B) | audit_id |
|---|---:|---|
| `wktree-close/SKILL.md` | 2,742 | `aud_20260827_0fe17b72b8` |
| `wktree-close/scripts/wktree-close.sh` | 10,443 | `aud_20260827_025adac3a2` |
| `wktree-compact/SKILL.md` | 2,123 | `aud_20260827_4a16a10778` |

PLT 綁定 grep（`mcp-ticket-platform|DevSources|workflow/|docs/pending|docs/deliver|TKR-|PLT`，含 `.sh`）**零命中**，`visited_files=3`（`aud_20260827_0c658cc4cf`）——複製型成立、無需在地改編。
APP 端 `.claude/skills/` 現有 12 個 skill，`wktree-*` 不存在（`aud_20260827_dc1e74afa4`）。
律 A 全冊機械取號：APP 冊 34 筆 id ＋ `Pending/` ＋ `deliver/` 全樹 `WKTR-*` 零命中（`visited_files=92`，`aud_20260827_3c0e342548`）；WKTR 家族現存號位於 PLT 端（`WKTR-01`＋FIX-A/B/C/D、`WKTR-GOV-01`，`visited_files=536`，`aud_20260827_00aa956f2d`），本票採語義子家族 `APPDEPLOY` 避免與數字序碰撞。
歸屬判定（TPL v3.6 二階）：標的 `.claude/skills/wktree-*` 為 WKTR 議題之專屬模組，**關聯性成立** → 歸 WKTR；PLT→APP 移轉 session 僅為發現地，依「排除項」不構成歸屬。（Akira 2026-08-27 裁決採甲案）

**前置資源缺口（本票納入修補之依據）**：`wktree-close.sh:9` 定義 `WKTR_DATA_DIR="$MAIN_ROOT/.claude/wktrdata"`，兩份 SKILL.md 逐字宣告 `.claude/wktrdata/` 與 `worktrees/` 為**非版控**。PLT `.gitignore` L34–35 有對應二行排除；APP `.gitignore` 對 `.claude/` **僅 L26 一行**（`settings.local.json`），二者均無排除（`aud_20260827_998b9773e8`）。純複製將產生「skill 書面契約與版控實況矛盾」之死引用，屬 Gate Self-Bootstrap Invariant 同型失效，故 `.gitignore` 二行納入本票 ALLOWED（Akira 2026-08-27 裁決採甲案）。

## Verify Config
TICKET_ID=WKTR-APPDEPLOY-01
CREATED=2026-08-27
PRESET=docs
ALLOWED=.claude/skills/wktree-close/SKILL.md,.claude/skills/wktree-close/scripts/wktree-close.sh,.claude/skills/wktree-compact/SKILL.md,.gitignore
FORBIDDEN=.claude/rules/,.claude/hooks/,.claude/settings.json,.claude/PROGRESS.md,plans/cso_briefing/,app/
MAX_LINES=450
DIFF_REF=9f66645b
ANCHOR_PROBED=2026-08-27 12:50
BASE_COMMIT=9f66645b
TARGET_BLOB=N/A

**MAX_LINES 性質**：估算校準遙測，非執行閘（TPL v3.5）。推導：三檔合計 15,308 B，以 ~40 B/行 估 ≈ 383 行，1.15× 緩衝進位 → 450。複製型票行數完全由源端決定，開票期估算不具停止判準地位。

**TARGET_BLOB=N/A 之理由（依 TPL v3.8 比例原則顯式揭露）**：三個標的於 APP 端尚不存在，pre-image 為空集，無 blob 可綁定；而源端 blob SHA **正是本票 AC 之量測對象本身**，若由票面預填即構成「AC 驗證字串與 AI 目標值非同源」（違 KAIROS #25 AC/AI 同源律）且無程式化取得通道（開票端無 `git hash-object` 呼叫能力）。故標 `N/A`，V1 baseline 降級為 **AI-0 錨點制**，兩端 hash 由執行端於 AI-0(B) live 計算並回填票面。

## Action Items

### AI-0(A): 錨點復驗（派工後、修改前）— 標的側
- **檔案**: `.gitignore`
- **動作**: 確認第 25 行字面 `# Claude Code local settings` 於全檔命中 **恰 1 次**；其下一行為 `.claude/settings.local.json`。
- **動作**: 確認全檔 `wktrdata` 與 `.claude/worktrees` 命中數 **均為 0**（若非 0 → 已有他人施作，走三分流歸因）。
- **動作**: 確認 `.claude/skills/wktree-close/`、`.claude/skills/wktree-compact/` **均不存在**。
- **基線漂移三分流（TPL v3.8）**：錨點不符時 MUST 依序執行 —— ①`git diff --name-only 9f66645b..HEAD -- .gitignore .claude/skills/wktree-close .claude/skills/wktree-compact` 為空 → 停止並回報 `ANCHOR_STALE_SCOPE_INTACT`；②非空則依 commit 前綴歸因：含本票 ID → `ALREADY_EXECUTED_PENDING_CLOSURE`；含他票 ID → `CANDIDATE_SUPERSEDED`（附票 ID，禁自判等價）；無前綴 → `BLOCKED_UNATTRIBUTED_STATE`（唯讀取證後升裁）。
- **三態均為停止＋分類回報，禁推定等價位置續作。**
- [ ] 完成

### AI-0(B): 源端 blob 基線建立（複製前，唯讀）
- **檔案（源端，唯讀，不在 ALLOWED 亦不得寫入）**:
  - `/d/DevSources/workflow/mcp-ticket-platform/.claude/skills/wktree-close/SKILL.md`
  - `/d/DevSources/workflow/mcp-ticket-platform/.claude/skills/wktree-close/scripts/wktree-close.sh`
  - `/d/DevSources/workflow/mcp-ticket-platform/.claude/skills/wktree-compact/SKILL.md`
- **動作**: 逐檔執行 `git hash-object <源端路徑>`，將三個 SHA 與 `wc -c` 位元組數**回填本票 Verify Config 下方新增之「AI-0(B) 實測表」**。
- **動作**: 位元組數 MUST 與開票期取證表相符（2,742／10,443／2,123）。**任一不符 → 停止回報**（源端於開票後異動，票面前提失效，禁自行採用新值）。
- [ ] 完成

### AI-1: 複製 `wktree-close/SKILL.md`
- **檔案**: `.claude/skills/wktree-close/SKILL.md`
- **現值**: 檔案不存在
- **目標值**: 源端 `mcp-ticket-platform/.claude/skills/wktree-close/SKILL.md` 之**位元組級複本**（`cp` 或等效；禁以編輯器重打、禁行尾正規化、禁改編任何一字元）
- [ ] 完成

### AI-2: 複製 `wktree-close/scripts/wktree-close.sh`
- **檔案**: `.claude/skills/wktree-close/scripts/wktree-close.sh`
- **現值**: 檔案不存在
- **目標值**: 源端同名檔之位元組級複本；複製後 MUST 確認可執行位元與源端一致（`git ls-files -s` 檢視 mode，源端為 100755 則標的亦須 100755）
- [ ] 完成

### AI-3: 複製 `wktree-compact/SKILL.md`
- **檔案**: `.claude/skills/wktree-compact/SKILL.md`
- **現值**: 檔案不存在
- **目標值**: 源端 `mcp-ticket-platform/.claude/skills/wktree-compact/SKILL.md` 之位元組級複本
- [ ] 完成

### AI-4: `.gitignore` 補執行期資料目錄排除
- **檔案**: `.gitignore`
- **現值**（L25–26，連續兩行）:
```
# Claude Code local settings
.claude/settings.local.json
```
- **目標值**（於 L26 之後**純增列** 3 行，既有兩行一字不動）:
```
# Claude Code local settings
.claude/settings.local.json

# WKTR-APPDEPLOY-01: worktree 治理之執行期資料（skill 書面契約明載非版控）
.claude/worktrees/
.claude/wktrdata/
```
- [ ] 完成

## Hard Constraints
1. **位元組級複製**：三個標的檔內容 MUST 與源端逐位元組相同。禁在地改編、禁補 APP 語境註記、禁行尾轉換（CRLF/LF 一律沿源端）——任何「順手改善」即令 AC hash 相等判準失效。
2. **源端唯讀**：本票 MUST NOT 對 `mcp-ticket-platform` 任何檔案寫入。若複製過程需暫存，落點限 APP 端 `tmp/WKTR-APPDEPLOY-01/`。
3. **`.gitignore` 純增列**：禁修改既有任何一行（含 L25–26 兩行與 L74 起之 `plans/cso_briefing/*` 白名單集）；AC 以 `git diff` 零 `-` 行驗證。
4. 禁順手建立 `.claude/wktrdata/` 或 `.claude/worktrees/` 之任何實體內容（探針檔除外，且驗畢即刪）。
5. 禁動 `.claude/skills/` 下其餘 12 個既有 skill 之任何檔案。
6. **RAPID_MODE 於 APP 不適用**（Akira 2026-08-21 裁決，`INFRA-SETTINGS-MERGE-01` HC-1 同源）：本票走 APP 全程序，禁以「PLT 端此類票走 RAPID」為由簡化審查。
7. commit 前綴 MUST 為 `[WKTR-APPDEPLOY-01]`。`.gitignore` 與三個 skill 檔得同 commit（同屬本票 ALLOWED），但 MUST NOT 與 `coordination_dependency` 之 APP-3 票任何異動混入同一 commit。

## Error Handling
- AI-0(A) 任一錨點不符 → 依三分流分類停止回報，禁續作。
- AI-0(B) 源端位元組數與票面不符 → 停止回報（源端漂移）。
- 源端路徑不可讀（跨 root 存取受限）→ 停止回報，禁以「憑記憶重建內容」替代複製。
- 複製後 hash 不相等且重試一次仍不等 → 停止回報（疑為行尾／編碼轉換），禁以「內容看起來一樣」勾選。
- `.gitignore` 增列後既有規則行為改變（見 AC-6 負向迴歸）→ 回退 AI-4，禁帶病提交。
- **[標配]** 本票含 commit 標的，`git check-ignore -v .claude/skills/wktree-close/SKILL.md .claude/skills/wktree-compact/SKILL.md .gitignore` 任一命中（標的被排除）→ 停止回報（gitignore 死結律，KAIROS #20）。**開票端預檢**：APP `.gitignore` 對 `.claude/` 僅排除 `settings.local.json`，三標的推定可見；執行端仍 MUST 實跑復驗，禁以本註記替代。

## Acceptance Criteria
- [ ] **[存在性]** 三個標的檔於 APP 端存在，且 `wc -c` 分別為 2,742／10,443／2,123
- [ ] **[存在性]** 逐檔 `git hash-object` 兩端相等（三筆，逐筆列出 SHA 於完成回報；與 AI-0(B) 實測表同源）
- [ ] **[存在性]** `.gitignore` 中 `.claude/worktrees/` 與 `.claude/wktrdata/` 各命中恰 1 次；`git diff 9f66645b -- .gitignore` 之 `-` 行數為 0
- [ ] **[行為性]** given `.claude/wktrdata/` 下建立探針檔 `probe_wktr.md`，expect `git status --short` **不列出**該檔（`git check-ignore` 命中）；驗畢 MUST 刪除探針檔與空目錄，並以 `git status --short` 二次確認零殘留
- [ ] **[行為性]** given 三個新複製之 skill 檔，expect `git status --short` 列為 `??`（未被 `.gitignore` 誤傷、可納入版控）——此為 AI-4 未過度排除之負向迴歸
- [ ] **[行為性]** given 既有 `.claude/settings.local.json`，expect 仍被 ignore（`git check-ignore` 命中）——證 AI-4 增列未破壞既有規則
- [ ] **[可達性]** N/A 顯式劃記：本票交付物為 `.claude/` 治理載體與 `.gitignore`，結構上不存在 `app/src/main/` production producer，屬型別不適用，非規避
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset — 無編譯產物，依 TPL「固定 AC 之 preset 適用性」顯式劃記)
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

## 完成後動作（收尾義務）
1. 票檔 AI／AC 逐項勾選後，由 `Pending/` 搬移至 `deliver/`。
2. 冊面轉態四要件原子完成：`lifecycle`→ 終態、`reviews` 四欄實測值（無可考記錄填 `N_A` 並揭露，禁推定補 PASS）、`file` 指標改指 `deliver/`、`checkpoint.resolution` 收尾實錄。
3. 冊面走獨立 `[GOVERNANCE-SYNC]` commit，並以 scoped `git_diff` 驗零意外。
4. **MUST NOT** 於本票內執行 APP-2／APP-3 之任何範圍（rigor-gate 移植、`mcp-service-safety.md`、`ssot-governance.md` 五段生命週期、registry 退役）。

---
**End of WKTR-APPDEPLOY-01**
