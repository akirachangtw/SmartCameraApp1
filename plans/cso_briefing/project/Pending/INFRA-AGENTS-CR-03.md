# INFRA-AGENTS-CR-03: Hooks 全自動閘門（R0 送審順序機械強制）【DRAFT — Gate 未過前禁止執行】

**簽發日期**: 2026-07-15（草案）
**簽發者**: Claude（Fable 5 session 起草；本票設計為模型無關，任何執行者依票面即可施工，無需起草 session 上下文）
**來源議題**: Advisor
**狀態**: ⛔ **DRAFT** — 執行前 MUST 完成「§0 Gate 證據回填」並經 CSO 簽核轉為 ACTIVE
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: execute_review
**前置依賴**: INFRA-AGENTS-CR-01 v1.1（已部署）、INFRA-AGENTS-CR-02（fail-advisor 記錄運行中）

> 檔案治理/來源說明：CR-01 以 rules 條款要求「codex exec 前 MUST 先過 R0」，遵循靠主 session 自律。本票將該 MUST 機械化：PreToolUse hook 攔截 codex 送審指令，無 FORWARD 記錄即硬阻。設計哲學：**hook 是守門員不是執行者**——R0 委派仍在 session 內進行，hook 只驗證順序證據（fail-advisor 記錄），純本地、零模型呼叫、確定性判斷。

---

## 0. Gate 證據回填（執行者第一步：全數貼上機械證據，任一缺漏即停）

| Gate | 閾值 | 證據（執行前回填） |
|------|------|------------------|
| 樣本量 | ≥ 12 張過 R0 之票 且 ≥ 14 天 | `ls .tmp/fail-advisor/ \| wc -l` = [　] ；最舊記錄日期 = [　] |
| 漏攔 | Codex BLOCKER 中字面可預攔品類（ssot_context §1/§2/§3）= 0 | 逐筆比對記錄欄位「後續實際走向」= [　] |
| 誤攔率 | `_OVERRIDE` 檔數 ≤ 2 | `ls .tmp/fail-advisor/*_OVERRIDE* \| wc -l` = [　] |
| Budget 隔離 | `fail_budget_state` 增量與 Codex FAIL 逐票相符 | 比對表 = [　] |

**翻盤條件（起草時已定，非執行者可改）**：樣本期內漏攔 ≥ 1 → 修 R0 字面閘後樣本歸零重計；`_OVERRIDE` ≥ 3 → R0 降級 advisory，本票無限期擱置。

## Verify Config
TICKET_ID=INFRA-AGENTS-CR-03
CREATED=2026-07-15
PRESET=refactor
ALLOWED=.claude/hooks/r0-gate.sh,.claude/settings.json,.claude/rules/cross-review.md
FORBIDDEN=.claude/CLAUDE.md,.claude/PROGRESS.md,tools/cross_review.sh,.claude/agents/cross-reviewer.md
MAX_LINES=200
DIFF_REF=<Gate 通過、CSO 簽核轉 ACTIVE 時回填>

> ⚠️ 本票將 `settings.json` 由慣例 FORBIDDEN 提升為 ALLOWED——此即本票須為治理票（execute_review + CSO 簽核）之原因。

## Action Items

### AI-1: 新建 `.claude/hooks/r0-gate.sh`（守門員腳本）
- **現值**: 不存在
- **目標值**: PreToolUse(Bash) hook，邏輯順序固定如下（執行者不得重排）：
  1. **Kill switch 最優先**：`[ -f .claude/hooks/r0-gate.off ] && exit 0`（回滾 = touch 一個檔案，零 JSON 編輯）
  2. 解析 stdin 之 tool_input.command；不含 `codex exec` 且不含 `cross_review.sh` → exit 0（放行無關指令）
  3. `.tmp/cross_review_payload.md` 不存在 → exit 2，stderr:「R0-GATE: payload 不存在」
  4. 計算 payload `sha256sum`，於 `.tmp/fail-advisor/` 現存記錄中搜尋 `PAYLOAD_SHA256: <hash>` 且裁決 = FORWARD → exit 0
  5. 存在 `.tmp/fail-advisor/OVERRIDE_[TICKET]` 標記檔 → exit 0 並 stderr 記載 override 放行
  6. 以上皆非 → exit 2，stderr:「R0-GATE: 本 payload 無 FORWARD 記錄，請先委派 @cross-reviewer」
  7. **Fail-closed**：腳本內部錯誤（set -euo pipefail 觸發）一律 exit 2——逃生門是 kill switch，不是靜默放行
- [ ] 完成

### AI-2: `settings.json` hooks 段掛載
- **現值**: PreToolUse 已有既存 matcher（telegram_ask 等）
- **目標值**: PreToolUse 陣列**追加** Bash matcher → `r0-gate.sh`；**MUST NOT** 取代或重排既存 hooks
- **⚠️ 歷史教訓（K-JSON）**：本專案曾因 JSON duplicate key 靜默失效整份 config——編輯後 MUST `jq . settings.json` 驗證可解析 + `jq '.hooks.PreToolUse | length'` 確認既存 matcher 數量 = 原數量 +1
- [ ] 完成

### AI-3: `cross-review.md` 配套修訂
- **現值**: CR-02 記錄格式六欄位，無 hash 欄
- **目標值**: (a) 記錄格式增第七欄 `PAYLOAD_SHA256`（hook 匹配依據）；(b) R0 路由節加註：順序自本票部署日起由 r0-gate.sh 機械強制，rules 條款保留為語義權威；(c) 護欄二 override 之機械形式 = CSO 顯式授權後由主 session 建立 `OVERRIDE_[TICKET]` 標記檔，事後 MUST 併入 `_OVERRIDE` 記錄
- [ ] 完成

## Hard Constraints
1. r0-gate.sh 零網路、零模型呼叫、單次執行 < 1 秒（純檔案系統判斷）
2. Kill switch 檢查 MUST 位於腳本第一個判斷（任何情況下 touch `.claude/hooks/r0-gate.off` 即完全退回 CR-01 rules 路由模式）
3. 既存 hooks（telegram_ask / stop.sh 家族）零觸碰、零重排

## Error Handling
- `jq` 驗證失敗或 PreToolUse matcher 數量異常 → 立即 `git checkout -- .claude/settings.json` 還原並停止回報
- fail-advisor 目錄為空（housekeeping 異常清空）→ 停止回報，不得以「無記錄可查」為由放寬 gate 邏輯

## Acceptance Criteria
- [ ] **[行為性]** 無 FORWARD 記錄時執行 `codex exec` 模擬指令 → hook exit 2 阻擋，stderr 含指引訊息
- [ ] **[行為性]** 寫入含匹配 `PAYLOAD_SHA256` + FORWARD 之記錄後同一指令 → 放行
- [ ] **[行為性]** touch `r0-gate.off` → 無記錄亦放行（回滾路徑實測）
- [ ] **[存在性]** `jq . .claude/settings.json` 可解析且 PreToolUse matcher 數 = 原數 +1，既存 matcher 逐一在列
- [ ] **[存在性]** cross-review.md grep 命中 `PAYLOAD_SHA256` 與 override 標記檔條款
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案


---

## ⚡ Amendment A1（2026-07-18 CSO 裁決 · Akira 核准 · §0 Gate 樣本起算點重置）

INFRA-AGENTS-CR-04 落地即變更 R0 職責 1 規格本身（增條件豁免分支＋第三分類）。§0 Gate 之樣本量與 14 天觀察期 **MUST 自 CR-04 部署日重新起算**——Gate 量測對象是修補後的規則，非舊規則（法理同票面既有翻盤條件「修字面閘後樣本歸零重計」）。CR-04 部署前累積之 fail-advisor 記錄不計入樣本，但保留為歷史證據。本票最早轉 ACTIVE ＝ CR-04 部署日＋14 天且餘項閾值齊達。
