# INFRA-QA-ACTIVATION-01: Ticket Activation Protocol preflight（P2，V0/V1/V2 四層驗證接線）

**簽發日期**: 2026-08-26 ｜ **簽發者**: Claude（SMTR；Akira 2026-08-23 開票授權，2026-08-26「繼續上述」續行）
**來源議題**: INFRA ｜ **Mode**: uc ｜ **Effort**: Medium
**Traycer Workflow**: N/A（`.claude/` 工具鏈；APP 無 RAPID，全程序）
**前置依賴（依賴三分）**：
- `execution_dependency`: **`INFRA-QA-PERTICKET-CONF-01`（P3）**——本票 Step 2 依賴其 `active.json` 寫入器與 per-ticket config 落點；未 CLOSED 前禁派工
- `evidence_dependency`: QA 草案 Rev B「Ticket Activation Protocol」節（Step 0–4、Rebase Protocol、PROGRESS.md 整合區塊）；`INFRA-TPL-V38-01` checkpoint（v3.8 BASE_COMMIT／TARGET_BLOB 與三分流條文——本票為其**執行端對應物**）
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=INFRA-QA-ACTIVATION-01
CREATED=2026-08-26
PRESET=refactor
ALLOWED=.claude/skills/qa/scripts/preflight_activation.sh,.claude/skills/qa/SKILL.md,.claude/PROGRESS.md
FORBIDDEN=.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/sv/**,.claude/CLAUDE.md,.claude/settings.json,app/**
MAX_LINES=N/A
BASE_COMMIT=<開工時回填>
TARGET_BLOB=<開工時回填；新建檔標 NEW>
DIFF_REF=字面佔位待開工回填
ANCHOR_PROBED=2026-08-26（新建腳本無 pre-image；`.claude/PROGRESS.md` 與 qa/SKILL.md 存在性經 SMTR 實測；派工時 MUST 重跑）

## 檔案治理／來源說明

TPL v3.8 已將三分流與 baseline 欄位寫入**票面規範層**，本票補上**執行端**：一支可重複呼叫的 preflight，於新 session／`/resume`／切票／`/compact` 後輸出機械化狀態，取代「憑對話記憶推定 active ticket」。

**設計約束（草案 Rev B 明載）**：V2 語義探針 **MUST NOT** 修改任何檔案；`overall=SATISFIED` 且 `provenance=UNKNOWN` 時狀態只能為 `CANDIDATE_SUPERSEDED`，**MUST NOT** 自動寫為 `DONE` 或 `ZERO-DIFF-DECLARED`。

**與既有票之語義對齊**：本票 V1 之三態輸出 MUST 使用 TPL v3.8 既定枚舉字面（`ANCHOR_STALE_SCOPE_INTACT`／`ALREADY_EXECUTED_PENDING_CLOSURE`／`CANDIDATE_SUPERSEDED`／`BLOCKED_UNATTRIBUTED_STATE`），禁另立同義詞（禁雙軌）。

## Action Items

### AI-0: 前置取證
- A：`INFRA-QA-PERTICKET-CONF-01` 冊面 lifecycle＝CLOSED 且 `tmp/verify/active.json` 寫入器在盤（execution_dependency）；不符 → 停止回報
- B：TPL v3.8 四枚舉字面於 `Unified_Ticket_Template_v3.md` 各命中 ≥1（語義對齊來源）
- C：`.claude/PROGRESS.md` 現行結構存收尾（AI-3 插入點基線）
- [ ] 完成

### AI-1: preflight 腳本 — V0 環境驗證
- 新建 `.claude/skills/qa/scripts/preflight_activation.sh`
- 輸出：repo root（`git rev-parse --show-toplevel`）、branch、HEAD SHA、`git status --short`、target path 是否落於 ticket scope、worktree/branch 是否允許、active ticket identity
- 任一不符 → `STATUS=BLOCKED_ENV_MISMATCH`，且明示 `ACTION=No writes, no test execution with ticket authority, no AC claim`
- V0 PASS 後方得呼叫 P3 之 `active.json` 寫入器（**MUST 複用，禁重複實作**）
- [ ] 完成

### AI-2: V1 baseline 判定 ＋ V2 語義探針
- **V1**：比對現行 HEAD 與票面 `BASE_COMMIT`、現行 target blob 與 `TARGET_BLOB`；四態輸出 `BASELINE_MATCHED`／`NEEDS_REBASE`／`BLOCKED_UNATTRIBUTED_STATE`／`BLOCKED_SCOPE_OR_TARGET_MISMATCH`。**R1 範圍不相交規則 MUST 實作**：`git diff --name-only BASE_COMMIT..HEAD -- <targets>` 為空 → 視同 `BASELINE_MATCHED` 直進 V2（不進 rebase 流程）
- **R2 歸因**：非空時清點區間內觸及標的之 commit 前綴，依 v3.8 三態路由
- **V2**：唯讀語義探針（symbol 存在性、fixture 輸入輸出、self_test 分支覆蓋），輸出 JSON 含 `ticket_id`／`semantic_contract`（逐 id PASS/FAIL）／`overall`／`provenance`
- **HC 對應**：V2 零檔案寫入；`SATISFIED`＋`UNKNOWN` → `CANDIDATE_SUPERSEDED`，禁 DONE／ZERO-DIFF
- [ ] 完成

### AI-3: PROGRESS.md Active Ticket Activation 區塊
- 於 `.claude/PROGRESS.md` 新增固定區塊，十一欄：Ticket ID／Ticket Revision／Activation Source／Repo Root／Branch／HEAD／Baseline State／Semantic State／Config State／Write Authority／Last Verified
- **Write Authority 判定（逐字）**：僅當 `V0=PASS` 且（`V1=MATCHED` 或已 owner REAUTHORIZED）且（`V2=MISSING` 或 `PARTIAL` 且已有實作授權）時方得為 `YES`
- **MUST NOT** 改動 PROGRESS.md 既有 Dashboard 內容（純新增區塊）
- [ ] 完成

### AI-4: SKILL.md 契約段
- `/qa` SKILL.md 補 preflight 呼叫時機（新 session／resume／切票／post-compact）、四層語義、Write Authority 規則、以及「`/resume` ≠ 可立即續 edit」之明文
- [ ] 完成

## Hard Constraints
1. V2 **MUST NOT** 寫入任何檔案（唯讀探針）。
2. **MUST NOT** 自動將 `SATISFIED` 判為 `DONE`／`ZERO-DIFF-DECLARED`；**MUST NOT** 自行修改票面 `BASE_COMMIT`／`TARGET_BLOB`；**MUST NOT** 將現行行號視為舊錨點之等價位置；**MUST NOT** 將來源不明之現況修改歸屬本票成果（草案 Rebase Protocol 四禁）。
3. **MUST NOT** 改動 `verify_checklist.sh`（FORBIDDEN；本票為獨立 preflight，非既有 Step 0 之改寫）。
4. `active.json` 寫入 **MUST** 複用 P3 之寫入器，禁重複實作。
5. 狀態枚舉 **MUST** 沿用 TPL v3.8 字面，禁另立同義詞。
6. preflight 失敗 **MUST NOT** 阻斷唯讀取證動作（草案：BLOCKED 態仍允許 read-only provenance）。

## Error Handling
- AI-0 A 不符 → 停止回報（execution_dependency 未滿足）
- AI-2 遇 v3.8 四枚舉外之情形 → 停止回報升裁（禁自造第五態）
- **[標配]** `git check-ignore -v` 對 ALLOWED 三檔 → 命中即停止回報（#20）

## Acceptance Criteria
- [ ] **[存在性]** `preflight_activation.sh` 在盤；四枚舉字面各命中 ≥1；`DONE`／`ZERO-DIFF-DECLARED` 之自動寫入路徑命中 0（HC-2 負向取證）
- [ ] **[存在性]** `.claude/PROGRESS.md` 含 `## Active Ticket Activation` 與十一欄位；既有 Dashboard 區塊行數不減（HC-3 類正向取證）
- [ ] **[行為性]** given 於非 ticket scope 之路徑執行 preflight，expect `STATUS=BLOCKED_ENV_MISMATCH` 且 `active.json` 未被更新
- [ ] **[行為性]** given `BASE_COMMIT..HEAD` 有漂移但未觸及 targets，expect V1 判為 `BASELINE_MATCHED` 直進 V2（R1 範圍不相交；此為本票核心價值，MUST 實測不得以設計驗證代替）
- [ ] **[行為性]** given 漂移觸及 targets 且區間內 commit 攜本票 ID 前綴，expect 輸出 `ALREADY_EXECUTED_PENDING_CLOSURE`；given 攜他票前綴，expect `CANDIDATE_SUPERSEDED` 附該 ID；given 無前綴，expect `BLOCKED_UNATTRIBUTED_STATE`（三態各一實測，輸出存收尾）
- [ ] **[行為性]** given V2 全部探針 PASS 而 provenance 未知，expect `overall=SATISFIED`＋狀態 `CANDIDATE_SUPERSEDED`，且執行前後 `git status` 一致（V2 零寫入之直接驗證）
- [ ] **[可達性]** Write Authority 判定之三項 input producer 各可指認（V0/V1/V2 於腳本內之行號，執行後回填）
- [ ] ~~`assembleDebug` PASS~~（N/A 顯式劃記：`.claude/` 工具鏈，零 `.kt`／`.xml`）
- [ ] `git diff --stat` 僅涉 ALLOWED 三檔

**End of INFRA-QA-ACTIVATION-01**
