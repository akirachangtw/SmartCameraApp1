---
name: dream
description: KAIROS 階段回顧整合：提煉錯誤模式、決策審計、SSOT 漂移掃描。Sprint 節點 (D2/D6/D10/D13/D15) 使用。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Prompt
你現在進入「KAIROS 整合模式 (Dream Mode)」。本階段開發已告一段落，請執行以下回顧：

## 0. M3 Family Provenance 稽核（前置門控，INFRA-M3-AUDIT-01）

三層回顧前 **MUST** 先執行 family provenance 偵測控制的 liveness 檢查與刷新：

```bash
bash .claude/scripts/m3_family_audit.sh --stale-check   # 最近戳 > 14 日或缺失 → exit 1 fail-loud
bash .claude/scripts/m3_family_audit.sh                 # 刷新對帳，寫入當日 dated artifact
```

- `--stale-check` exit 1（stale）→ **MUST** fail-loud 回報「🔴 M3 控制 stale」，仍續跑刷新指令補戳。
- 刷新指令 exit 3（≥1 escalate）→ **MUST** 將 escalate 清單（結構合法但 family 未登記之 ID）列入下方 §1 🟢 預防性發現，交 CSO 語意裁決；**MUST NOT** 自動補表 / auto-register（DRA 保護）。
- exit 0（0 escalate）→ 控制 fresh 且家族全登記，續跑三層回顧。

## 1. 錯誤模式提煉 (Error Pattern Mining)

回顧本階段所有編譯錯誤、邏輯 Bug、SV 駁回項目，歸類為：

| 類別 | 定義 | 行動 |
|------|------|------|
| 🔴 架構性錯誤 | 重複出現 2+ 次的同類問題 | 提煉為 CLAUDE.md 新規則 |
| 🟡 一次性失誤 | typo、遺漏 import、copy-paste 殘留 | 記錄但不立規則 |
| 🟢 預防性發現 | 潛在風險但尚未爆發 | 加入風險矩陣 |

對 🔴 類別，**MUST** 產出**可直接追加到 CLAUDE.md 的條目**，格式：
```
X. **[規則名稱]**：[具體禁止/要求的行為]。觸發來源：[Ticket ID]。
```

## 2. 決策審計 (Decision Audit)

**SHOULD** 列出本階段做出的所有技術決策：

| 決策 | 預期結果 | 實際結果 | 判定 |
|------|---------|---------|------|
| [描述] | [預期] | [實際] | ✅/⚠️/❌ |

- ✅ 決策正確（事後驗證符合預期）
- ⚠️ 需微調（方向對但參數/實作需修正）→ **SHOULD** 提出具體修正方案
- ❌ 決策錯誤（需要回退）→ **SHOULD** 提出回退步驟 + 替代方案

## 3. SSOT 漂移掃描 (Drift Detection)

對比實際代碼與 v3.3 SSOT + Tech Deep Dive v3：

- **命名漂移**：類別名/變數名與 SSOT 不一致（如 SSOT 寫 `EdgeWarmthLayer` 但實作叫 `WarmGradient`）
- **數值漂移**：閾值/參數與 SSOT 不同（如冷卻從 2000ms 改成 1500ms 但未更新 SSOT）
- **架構漂移**：模組邊界或資料流向與 SSOT 定義不符

對每項漂移 **SHOULD** 判定：
- 📝 **規格需更新**：實作是對的，SSOT 過時
- 🔧 **代碼需修正**：SSOT 是對的，代碼偏離

## 4. Tacit Knowledge 壓測審查 (Skill Validation Audit)

**SHOULD** 列出本 Sprint 新增或修改的所有 Skill（`.claude/skills/*/SKILL.md`）及其實際執行次數：

| Skill | 本 Sprint 執行次數 | 壓測狀態 |
|-------|-----------------|---------|
| [skill-name] | [N] 次 | ✅ 壓測（≥3）/ ⚠️ 未壓測（<3） |

- ✅ **壓測**：本 Sprint 內經 ≥3 輪實際執行驗證（不含開發當下的冒煙測試）
- ⚠️ **未壓測**：執行次數 < 3，**SHOULD** 列入下輪 TODO 補跑；若 Skill 屬關鍵路徑（/qa /sv /compact）則升格為 **MUST**

未壓測項目 **SHOULD** 以下格式追加至 PROGRESS.md Next Actions：
```
[ ] Skill 壓測補跑：[skill-name]（INFRA-XX，KAIROS [Node] 標記）
```

## 輸出

**SHOULD** 產出以下格式的回顧報告，並更新 PROGRESS.md 的 KAIROS Findings 區塊：

```markdown
# KAIROS Report — [Sprint Node] (YYYY-MM-DD)

## Error Patterns
### 🔴 Architecture Rules (→ CLAUDE.md)
- [rule 1]
- [rule 2]

### 🟡 One-off Mistakes
- [list]

### 🟢 Preventive Findings
- [list]

## Decision Audit
| Decision | Expected | Actual | Verdict |
|----------|----------|--------|---------|
| ... | ... | ... | ... |

## SSOT Drift
| Item | Type | SSOT Says | Code Does | Action |
|------|------|-----------|-----------|--------|
| ... | naming/value/arch | ... | ... | 📝/🔧 |

## Recommended CLAUDE.md Additions
[ready-to-paste rules]
```

最後更新 PROGRESS.md：
- 在 KAIROS Findings 區塊追加本次報告摘要
- 若有 ⚠️/❌ 決策，**MUST** 加入 Blocking Issues
