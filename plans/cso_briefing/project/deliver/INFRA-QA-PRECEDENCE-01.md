# INFRA-QA-PRECEDENCE-01: Skill 標準流程 vs 票面 FORBIDDEN 優先序規則 + 開票 Pre-flight 觸碰面 Lint

**簽發日期**: 2026-07-15
**簽發者**: Claude
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: INFRA-AGENTS-CR-01 v1.1（Pre-flight 職責 5 為 AI-2 擴充載體）

> 檔案治理/來源說明：事件支撐 = INFRA-HEADROOM-DECOMMISSION-01 執行中 STOP（2026-07-15）：/qa Step 4 之通用 MUST（更新 PROGRESS.md）與票面 HC3 + FORBIDDEN token（禁動 PROGRESS.md）正面對撞，執行端依 Rule 11 正確停手。CSO 個案裁決：選項 1（票面 FORBIDDEN 優先，Step 4 SKIP，同步債移交 DOCSYNC 專票，比照 INFRA-HEADROOM-DOCSYNC-01 慣例），並明示該裁決不構成通案修訂——本票即為通案承接。根因與 GAP-11 L1 同構：約束簽發前無交叉比對。

## Verify Config
TICKET_ID=INFRA-QA-PRECEDENCE-01
CREATED=2026-07-15
PRESET=refactor
ALLOWED=.claude/rules/cross-review.md,.claude/skills/qa/SKILL.md
FORBIDDEN=.claude/CLAUDE.md,.claude/settings.json,.claude/PROGRESS.md,tools/cross_review.sh,tools/verify_checklist.sh
MAX_LINES=150
DIFF_REF=<執行前 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: qa Skill 增列「票面 FORBIDDEN 優先序條款」
- **檔案**: `.claude/skills/qa/SKILL.md`（Step 4 / 4.5 節）
- **現值**: Step 4/4.5 為無條件 MUST（更新並 stage PROGRESS.md / 歸檔輪替）
- **目標值**: 增列條款——Skill 標準步驟之目標檔案命中**現行票面 FORBIDDEN** 時：
  1. 該步驟自動改判 **SKIP**，QA 報告標注 `SKIPPED — DEFERRED-TO-DOCSYNC`，不計 FAIL、不觸發 STOP
  2. **承接條件（缺一仍 STOP）**：存在承接 DOCSYNC 票，**或** QA 報告「待開票」節登記同步債（含本票 ID 與完成狀態）——防同步債靜默累積
  3. 本條款僅適用「skill 通用步驟 vs 票面 FORBIDDEN」此一衝突型；其餘規則衝突維持 Rule 11 停下確認，**不得推廣為任意衝突之自動裁決**
  4. **措辭校準（Akira 2026-07-18 直接授權，不回 CSO）**：本次事件證明衝突型不只①，還有②「Skill Auto-Chain vs 票面 Hard Constraints 明文例外」；落地條款合併為單一措辭「票面明文例外（FORBIDDEN 或 Hard Constraints）優先於 Skill 通用流程」，一條覆蓋兩型（見 `qa/SKILL.md` Step 4/4.5 例外節）
- [x] 完成

### AI-2: cross-review.md 增「管線標準觸碰面登記表」+ Pre-flight Lint 維度
- **檔案**: `.claude/rules/cross-review.md`
- **現值**: Pre-flight（CR-01 v1.1 職責 5）僅比對「裁決落點 × ssot_context 條文」
- **目標值**:
  1. 新增登記表（新管線步驟觸碰 repo-tracked 檔案時 MUST 同步維護）：

     | 管線步驟 | 標準觸碰面 |
     |---------|-----------|
     | /qa Step 4 | `.claude/PROGRESS.md` |
     | /qa Step 4.5 | `PROGRESS_ARCHIVE.md`（歸檔輪替） |
     | /handoff | `Handoff/` |

  2. Pre-flight 增比對維度：票面 FORBIDDEN 清單 × 上表觸碰面——命中時票面 **MUST** 內建處置條款（`DEFERRED-TO-DOCSYNC` 或明示例外授權），留白即 Pre-flight FAIL（開票 lint，衝突消滅於起草時）
- [x] 完成

## Hard Constraints
1. 零觸碰 `.sh`（Step 0 機械閘、cross_review.sh 均不在 scope；本票純 rules/skill 文字層）
2. 優先序條款不得以任何形式弱化票面 FORBIDDEN 的機械效力（FORBIDDEN 永遠贏，SKIP 的是 skill 步驟，不是閘門）

## Error Handling
- qa skill 實際路徑非 `.claude/skills/qa/SKILL.md` → 定位實際檔案；若落於 ALLOWED 之外 → 停止回報（票面修訂屬 CSO 權限）
- Step 4/4.5 現行條文結構與預期不符（無法安全錨定插入）→ 停止回報附現況引文

## Acceptance Criteria
- [x] **[存在性]** qa SKILL.md grep 命中 `DEFERRED-TO-DOCSYNC` 條款與承接條件雙要件
- [x] **[存在性]** cross-review.md grep 命中觸碰面登記表（三列）與 Pre-flight FORBIDDEN 比對維度
- [x] **[行為性]** 以 INFRA-HEADROOM-DECOMMISSION-01 票面回放 Pre-flight → lint 命中（FORBIDDEN 含 PROGRESS.md × Step 4 觸碰面）並要求處置條款 — **PASS**：由承接票 `INFRA-QA-PRECEDENCE-01-DOCSYNC-01-FIX-A` 派工 `cross-reviewer` subagent 對該歷史票真實 FORBIDDEN 清單 + Hard Constraint 3 執行回放，判定 `REPLAY VERDICT: PASS — 條款有效`（命中且無 `DEFERRED-TO-DOCSYNC` → 正確判 Pre-flight FAIL；Hard Constraint 3 屬禁令重述非明示例外授權）
- [x] **[行為性]** 模擬票面（FORBIDDEN 含 PROGRESS.md + 已內建處置條款）走 /qa → Step 4 產出 `SKIPPED — DEFERRED-TO-DOCSYNC` 而非 STOP — **PASS**：由承接票 `INFRA-QA-PRECEDENCE-01-DOCSYNC-01-FIX-A` 派工**獨立**第二個 `cross-reviewer` subagent 實例（非本票或 DOCSYNC-01 自身 dogfood），對純假設模擬票面執行 `/qa` Step 4 例外條款判斷，判定 `AC4 REPLAY VERDICT: PASS — 條款按設計運作於獨立情境`（觸發/承接/範圍三重門全綠 → SKIP，非 STOP）
- [x] `assembleDebug` PASS
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

---

## 結案留痕（2026-07-19 · Akira 裁決 A：T2 免除）

**裁決**：Traycer T2 免除，依票面結案。**本節即裁決留痕**，杜絕「T2 未執行」以同步債形式殭屍復發。

**依據三重**：
1. **簽發時法律**：票面（2026-07-15 簽發）明文 `Traycer Workflow: N/A`；2026-07-19 起之「.md skill 行為變更 = 治理票送 execute_review」慣例不追溯適用舊票
2. **先例雙引**：UC02（治理票不耗終審 ROI，Traycer 跳過）＋ CR-02（免 Traycer · 純 .md 治理票，Codex R2 PASS 收尾）——本票同型（HC1 零觸碰 `.sh`，純 rules/skill 文字層）
3. **證據完備度**：AC 6/6 實證勾選，兩條行為性 AC 由承接票派工**獨立** cross-reviewer subagent 實例回放（`REPLAY VERDICT: PASS` ×2），非自身 dogfood 裸自證

**翻盤條件**：`DEFERRED-TO-DOCSYNC` 條款實跑出界（SKIP 誤用於非「票面明文例外 vs Skill 通用流程」衝突型）→ 開 FIX 票補審，屆時 Traycer 送審必要性隨 FIX 票重議。

**End of INFRA-QA-PRECEDENCE-01** · **CLOSED**（AC 6/6 · 獨立回放 PASS ×2 · T2 免除依 Akira 2026-07-19 裁決 A）
