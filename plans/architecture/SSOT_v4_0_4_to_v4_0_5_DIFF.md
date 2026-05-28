# SSOT v4.0.4 → v4.0.5 修訂差異文件

**產出日期**: 2026-05-28  
**產出角色**: UITA (Ambient UI Tech Auditor)  
**修訂項數**: 2 項（R35~R36）  
**觸發來源**: Session 9 — GPT UITA 交叉審查命中 2 項規格矛盾  
**新增決策**: D-25~D-26（CSO 確認 locked）

---

## R35（🔴 Critical）：Overlay 總表 Layer 1 文案修正（D-25）

**搜尋錨點**: `## Viewfinder Overlay 圖層定義`

**原文**:
```
Layer 1: Pitch Traction 漸層（邊帶 15% viewport, 琥珀金/綠色 — D-2; 底層先繪製）
```

**替換為**:
```
Layer 1: Pitch Traction 指示柱（右側 16dp, 36dp×25% viewfinder, 琥珀金/綠色 — D-2; 底層先繪製）
```

**理由**: R29 替換文字沿用 pre-D-2 時代的「漸層（邊帶 15% viewport）」語法。D-2（Session 3 locked）明確定義 Pitch Traction 為「右側垂直指示柱」（§2.3），由中軸線 + 定位環 + 浮動圓點組成，幾何定義為 `COLUMN_WIDTH_DP=36dp` + `COLUMN_RIGHT_MARGIN_DP=16dp`，與 viewport 百分比無關。「漸層」措辭會導致 Claude Code 實作出邊緣漸層而非指示柱。

**根因**: DIFF 撰寫階段缺少與 D-2 決策的交叉校驗。

---

## R36（🟠 High）：§2.9 LEGACY 光線指示語義修復（D-26）

**搜尋錨點**: §2.9 三級裝置降級表

**原文**:
```
| LEGACY | **靜態 amber α0.60**（無動畫） | LEGACY 已關閉光線分析（§10.3） |
```

**替換為**:
```
| LEGACY | **不顯示光線狀態**（靜態白色 α0.85，與正常態一致） | LEGACY 已關閉光線分析（§10.3），無數據源則不渲染狀態色（D-26） |
```

**理由**: §10.3 明確將「光線分析」列入 LEGACY 關閉清單（Frame 2/4 histogram 不執行）。原文要求 LEGACY 顯示「靜態 amber α0.60」，但在光線分析管線停止的前提下，系統無法判斷當前光線狀態，amber 會變成永久假警報，違反 Ambient UI「不確定時不干預」核心原則。

**否決方案**:
- 方案 B（僅反映手動 Flash/HDR 開關）：需區分分析驅動 vs 手動驅動雙路徑，複雜度不值得
- 方案 C（保留簡化分析 0.5fps）：違反 §10.3「關閉光線分析」聲明，且壓縮 LEGACY CPU 預算

---

## 元資訊更新

### 文件頭
- `文件版本`: 4.0.4 → **4.0.5 (Session 9 Micro-Revision — D-25~D-26 + R35~R36)**
- `合併來源`: v4.0.3 → **v4.0.4 + Session 9**
- `搭配文件`: `Cueshot_Layout_Spec_v2_5.md` → **`Cueshot_Layout_Spec_v2_5_1.md`**

### 文件元資訊區塊
- `版本`: v4.0.4 → v4.0.5
- `作廢文件`: 新增 v4.0.4
- `搭配文件`: → `Cueshot_Layout_Spec_v2_5_1.md`
- `決策記錄`: 新增 `D-25~D-26 (Session 9)`
- `合併來源列表`: 新增 Session 9 Micro-Revision 段落
- `End of`: v4.0.4 → v4.0.5

---

**End of SSOT v4.0.4 → v4.0.5 DIFF**
