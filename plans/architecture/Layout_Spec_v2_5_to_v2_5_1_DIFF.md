# Layout Spec v2.5 → v2.5.1 修訂差異文件

**產出日期**: 2026-05-28  
**產出角色**: UITA (Ambient UI Tech Auditor)  
**修訂項數**: 2 項（H-4~H-5）  
**觸發來源**: Session 9 — GPT UITA 交叉審查命中 → SSOT R35/R36 對齊  
**對齊上游**: SSOT v4.0.5（D-25 + D-26）

---

## H-4（🔴 Critical）：§3.3 Overlay 總表 Layer 1 文案對齊 SSOT R35

**搜尋錨點**: `### 3.3 Viewfinder Overlay 圖層`

**原文**:
```
Layer 1: Pitch Traction 漸層（邊帶 15% viewport, 琥珀金/綠色 — D-2; 底層先繪製）
```

**替換為**:
```
Layer 1: Pitch Traction 指示柱（右側 16dp, 36dp×25% viewfinder, 琥珀金/綠色 — D-2; 底層先繪製）
```

**理由**: 對齊 SSOT v4.0.5 R35 / D-25。

---

## H-5（🟠 High）：§3.2.1 LEGACY 光線指示對齊 SSOT R36

**搜尋錨點**: §3.2.1 三級裝置降級表

**原文**:
```
| LEGACY | **靜態 amber α0.60**（無動畫） | LEGACY 已關閉光線分析（SSOT §10.3） |
```

**替換為**:
```
| LEGACY | **不顯示光線狀態**（靜態白色 α0.85，與正常態一致） | LEGACY 已關閉光線分析（SSOT §10.3），無數據源則不渲染狀態色（D-26） |
```

**理由**: 對齊 SSOT v4.0.5 R36 / D-26。

---

## 元資訊更新

- `版本`: v2.5 → **v2.5.1 (SSOT v4.0.5 Alignment + D-25 Layer 1 文案修正 + D-26 LEGACY 光線指示修復)**
- `上游文件`: `SSOT_v4_0_4.md` → **`SSOT_v4_0_5.md`**
- `取代`: v2.4 → **v2.5 及所有前版**
- 全文 `SSOT v4.0.4` → `SSOT v4.0.5`、`SSOT_v4_0_4.md` → `SSOT_v4_0_5.md`

---

**End of Layout Spec v2.5 → v2.5.1 DIFF**
