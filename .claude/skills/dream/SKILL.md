# Description
KAIROS 階段回顧整合：提煉錯誤模式、決策審計、SSOT 漂移掃描。Sprint 節點 (D2/D6/D10/D13/D15) 使用。

# Prompt
你現在進入「KAIROS 整合模式 (Dream Mode)」。本階段開發已告一段落，請執行以下三層回顧：

## 1. 錯誤模式提煉 (Error Pattern Mining)

回顧本階段所有編譯錯誤、邏輯 Bug、SV 駁回項目，歸類為：

| 類別 | 定義 | 行動 |
|------|------|------|
| 🔴 架構性錯誤 | 重複出現 2+ 次的同類問題 | 提煉為 CLAUDE.md 新規則 |
| 🟡 一次性失誤 | typo、遺漏 import、copy-paste 殘留 | 記錄但不立規則 |
| 🟢 預防性發現 | 潛在風險但尚未爆發 | 加入風險矩陣 |

對 🔴 類別，產出**可直接追加到 CLAUDE.md 的條目**，格式：
```
X. **[規則名稱]**：[具體禁止/要求的行為]。觸發來源：[Ticket ID]。
```

## 2. 決策審計 (Decision Audit)

列出本階段做出的所有技術決策：

| 決策 | 預期結果 | 實際結果 | 判定 |
|------|---------|---------|------|
| [描述] | [預期] | [實際] | ✅/⚠️/❌ |

- ✅ 決策正確（事後驗證符合預期）
- ⚠️ 需微調（方向對但參數/實作需修正）→ 提出具體修正方案
- ❌ 決策錯誤（需要回退）→ 提出回退步驟 + 替代方案

## 3. SSOT 漂移掃描 (Drift Detection)

對比實際代碼與 v3.3 SSOT + Tech Deep Dive v3：

- **命名漂移**：類別名/變數名與 SSOT 不一致（如 SSOT 寫 `EdgeWarmthLayer` 但實作叫 `WarmGradient`）
- **數值漂移**：閾值/參數與 SSOT 不同（如冷卻從 2000ms 改成 1500ms 但未更新 SSOT）
- **架構漂移**：模組邊界或資料流向與 SSOT 定義不符

對每項漂移判定：
- 📝 **規格需更新**：實作是對的，SSOT 過時
- 🔧 **代碼需修正**：SSOT 是對的，代碼偏離

## 輸出

產出以下格式的回顧報告，並更新 PROGRESS.md 的 KAIROS Findings 區塊：

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
- 若有 ⚠️/❌ 決策，加入 Blocking Issues
