# MEO-FIX-B: PreToolUse gate 分支序缺陷修復 — pending gate 架空 STATE.md 診斷自舉豁免

**簽發日期**: 2026-07-28
**簽發者**: Claude
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: strict_verification
**前置依賴**: 無（MEO-T3 非前置：本票限閘門域，T3 限信號域，ALLOWED 不相交）

> 檔案治理/來源說明：本票源自 MEO-FIX-A Rev J 收案後之殘留缺陷（前案裁決檔
> `MEO_PreToolUse_Diagnosis_Livelock_Residue` 已定性為 TRUE-POSITIVE-RULE-GAP）。
> 行級證據取自 meo_pretooluse_gate.sh 現行檔（2026-07-28 grep 實測）：L147 pending
> gate 之註解字面即「MUST precede STATE bootstrap exemption」，而 L156-160 之
> bootstrap 豁免在 PENDING=true 時結構性不可達。正向反例證據：
> plans/cso_briefing/reports/MEO-ADJUDICATION-01_Release_Evidence_20260726.md
> （含 Episode ID=2/ID=3 Addenda）與 meo/adjudications/2.md、3.md。
> Episode ID=3 已裁定「執法 deny 回聲構成信號自激迴路」（KAIROS 候選 #6），本票
> AI-4 為其閘門側修復；信號側計數規則屬 MEO-T3 AI-5，本票禁觸。

## Verify Config
TICKET_ID=MEO-FIX-B
CREATED=2026-07-28
PRESET=bugfix
ALLOWED=.claude/skills/model-effort-orchestrator/hooks/meo_pretooluse_gate.sh,.claude/skills/model-effort-orchestrator/SKILL.md,STATE.md
FORBIDDEN=.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh,gate.conf,.claude/settings.json,.claude/hooks/stop.sh,.claude/rules/ticket_pattern.md,.claude/rules/cross-review.md
MAX_LINES=120
DIFF_REF=<執行前 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: 治理面命中改為雙類（MEO_GOV_CLASS）
- **檔案**: `.claude/skills/model-effort-orchestrator/hooks/meo_pretooluse_gate.sh`
- **現值**: L128 `MEO_GOV_HIT=false`；L134 / L140 / L141 / L142 四處一律設 `MEO_GOV_HIT=true`
- **目標值**: 新增變數 `MEO_GOV_CLASS`（值域 `none|state|adjudication`），與既有
  `MEO_GOV_HIT` 並存、不移除。分類規則：`"$PROJECT_ROOT/STATE.md"` → `state`；
  `gate.conf` / `model-effort-orchestrator/*` / `meo/adjudications/*` → `adjudication`；
  `TOOL_NAME=Bash` 之命令列字面匹配（L131-135）一律 → `adjudication`（保守：Bash 無 payload 可字面隔離）
- [ ] 完成

### AI-2: pending gate 僅對 adjudication 類無條件生效
- **檔案**: 同 AI-1
- **現值**: L147 `if [ "$MEO_GOV_HIT" = true ] && grep -q '^MEO_ESCALATION_PENDING=true' "$STATE_FILE" 2>/dev/null; then`
- **目標值**: 條件左項改為 `[ "$MEO_GOV_CLASS" = adjudication ]`；`state` 類不進入本分支，改由 AI-3 處理
- [ ] 完成

### AI-3: state 類於 pending 期間之 payload 字面閘（反偽造保留）
- **檔案**: 同 AI-1
- **現值**: L156-160 `case "$TARGET" in *STATE.md) emit allow "MEO bootstrap: diagnosis write to STATE.md exempt" ;; esac`（PENDING=true 時結構性不可達）
- **目標值**: 於 pending 分支之後、既有 case 之前插入 state 類專屬分支：
  - `TOOL_NAME` ∈ {Edit, MultiEdit} 且 payload（`.tool_input.new_string` 與
    `.tool_input.edits[].new_string`）對 `MEO_(ESCALATION_(PENDING|ID)|WM_[A-Z_]+|FASTPATH)=`
    **零命中** → 放行（落入既有 bootstrap 豁免）
  - 上述命中，或 `TOOL_NAME` 為 `Write`（全檔覆寫無法字面隔離）→
    `emit deny "MEO: escalation pending - STATE.md write touches controlled keys; external adjudication required"`
- [ ] 完成

### AI-4: release 成功後改為放行，消滅一次性 deny 信號源
- **檔案**: 同 AI-1
- **現值**: L149-151 release 成功後 `emit deny "MEO: escalation released (record-before-clear complete) - pending cleared, retry this call"`
- **目標值**: release 成功後不再無條件 deny；改依本次呼叫之 `MEO_GOV_CLASS` 重新裁決——
  `state` 類且通過 AI-3 字面閘 → `emit allow "MEO: escalation released - call permitted"`；
  `adjudication` 類 → 維持 deny（HC2，反偽造不弱化），訊息改為
  `"MEO: escalation released; adjudication-path writes remain denied"`
- **理由**: 現行「release 成功仍 deny」每次製造一筆非零退出，構成自激迴路燃料（見 meo/adjudications/3.md）
- [ ] 完成

### AI-5: SKILL.md 文件同步
- **檔案**: `.claude/skills/model-effort-orchestrator/SKILL.md`
- **現值**: 記載單一治理面旗標與「pending gate 先於 bootstrap 豁免」之分支序
- **目標值**: 同步三項變更（雙類模型 / state 類 payload 字面閘 / release 即放行），並字面
  載明自舉不變量：「診斷寫入不得被任何 pending 狀態封鎖」
- [ ] 完成

## Hard Constraints
1. 禁止修改 `meo_signal_update.sh` 之任何信號計算（信號域屬 MEO-T3 AI-5）
2. 反偽造防線不得弱化：`meo/adjudications/*` 之寫入於 PENDING=true 期間 MUST 維持 deny，無任何例外分支
3. 禁止新增任何可由 agent session 內清除 PENDING 之路徑，含間接路徑（透過 STATE.md payload 寫入 `MEO_ESCALATION_*` / `MEO_WM_*` 鍵）
4. fail-open 語義維持：`PROJECT_ROOT` 不可判定時仍 `emit allow`，現行行為不得改

## Error Handling
- 執行開始時 `MEO_ESCALATION_PENDING=true` → 停止回報（本票 ALLOWED 全屬治理面，pending 期間結構性不可執行，須先完成外部裁決 release）
- 執行中新 episode 開啟致 gate lock → 停止回報，**禁逐次 release-and-retry**（自激迴路已裁，見 meo/adjudications/3.md）
- `jq` 無法取得 `.tool_input.edits[].new_string`（MultiEdit schema 與假設不符）→ 停止回報
- gate 檔行號與票面所載（L128 / L140-142 / L147 / L149-152 / L156-160）不符 → 停止回報

## Acceptance Criteria
- [ ] **[存在性]** grep 命中 `MEO_GOV_CLASS`，且三值 `none|state|adjudication` 於檔內字面出現
- [ ] **[可達性]** PENDING=true 時 STATE.md 診斷寫入放行分支之 producer 證據欄：`meo_pretooluse_gate.sh:<行號>`（執行後回填），並證明該分支排序在 pending 分支之後仍可達
- [ ] **[行為性]** 四象限交叉矩陣全通過（MUST 經 production hook 路徑驅動，禁 fixture 直注 emit）：
      Q1 PENDING=false × STATE.md 診斷 Edit → allow（回歸保護）
      Q2 PENDING=true × STATE.md 診斷 Edit（payload 無控制鍵）→ **allow**（本票核心修復；修復前為 deny）
      Q3 PENDING=true × STATE.md Edit（payload 含 `MEO_ESCALATION_PENDING=`）→ deny
      Q4 PENDING=true × `meo/adjudications/*` Write → deny（HC2）
- [ ] **[行為性]** release 消費用例：裁決檔＋sidecar 均存在時，**單次呼叫內**完成 release 且不再回傳 `retry this call`；六鍵（PENDING / WM_RETRY / WM_STREAK / WM_ERROR_CLASS / WM_REJECT / LAST_ESCALATION_DECISION）與裁決檔一致
- [ ] **[行為性]** episode-2/3 情境回放：以 `meo/adjudications/2.md`、`3.md` 為素材重演同一動作序列，確認不再衍生額外 escalation
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

---
## 冊面收束註記(2026-08-01,MEO-DECOMM-01 Step E)
本票 lifecycle=CLOSED 之語意為 **STOPPED/superseded**(前提消滅型退役),非驗收完成。本票未派工即收束:原序列化待 FIX-A Rev K + Codex 復審,嗣退役令生效,修復對象(PreToolUse gate 分支序)消滅。裁決鏈=vault `2026-08-01_MEO_V1_Enforcement_Layer_Decommission.md`。
