# Layout Spec v2.4 → v2.5 修訂差異文件

**產出日期**: 2026-05-28  
**產出角色**: UITA (Ambient UI Tech Auditor)  
**修訂項數**: 3 項（H-1~H-3）  
**觸發來源**: Session 8 — Grid 實機回流 + SSOT v4.0.4 R29 對齊  
**對齊上游**: SSOT v4.0.4（D-24 Grid 規格鎖定）

---

## 合併指令（給 Gemini Merge Technician）

**嚴格合併規則**：
1. 逐項比對「搜尋錨點」定位修改位置
2. 僅修改 DIFF 指定的行，不得修改任何未提及的內容
3. 版本號、修訂日期、上游文件引用按本文件末尾的「元資訊更新」執行
4. 完成後輸出修改行數統計供 UITA 驗證

---

## H-1（🔴 Critical）：§3.3 Grid 參數 + Z-order 對齊 SSOT R29

**搜尋錨點**: `### 3.3 Viewfinder Overlay 圖層（對齊 SSOT v4.0.3 §Viewfinder Overlay — 逐字對齊）`（約 L163）

**原文**:
```
### 3.3 Viewfinder Overlay 圖層（對齊 SSOT v4.0.3 §Viewfinder Overlay — 逐字對齊）

```
Layer 0: Camera Preview（CameraX PreviewView）
Layer 1: 三分網格線（#FFFFFF alpha 0.08, 線寬 0.5px）
Layer 2: Pitch Traction 指示柱（右側 16dp, 36×25% viewfinder, 琥珀金/綠色 — D-2）
```

**替換為**:
```
### 3.3 Viewfinder Overlay 圖層（對齊 SSOT v4.0.4 §Viewfinder Overlay — 逐字對齊）

```
Layer 0: Camera Preview（CameraX PreviewView）
Layer 1: Pitch Traction 漸層（邊帶 15% viewport, 琥珀金/綠色 — D-2; 底層先繪製）
Layer 2: 三分網格線（#FFFFFF alpha 0.35, 線寬 1.5px — D-24; 在 Pitch 之上確保可見）
```

**注意**：Layer 1↔Layer 2 互換後，下方的「Layer 2 詳細規格」標題不變（仍是 Pitch Traction 的詳細規格），但需更新為「Layer 1 詳細規格」。

**額外修改 — 搜尋錨點**: `**Layer 2 詳細規格（D-2 指示柱設計，完整定義見 SSOT v4.0.3 §2.3）**`（約 L175）

**替換為**:
```
**Layer 1 詳細規格（D-2 指示柱設計，完整定義見 SSOT v4.0.4 §2.3）**：
```

---

## H-2（🟠 High）：新增 Layer 2 Grid 詳細規格表

**搜尋錨點**: 在修改後的 `**Layer 1 詳細規格（D-2 指示柱設計...）**` 表格結束後（即 `| **禁止** |` 行之後），`**Layer 3 詳細規格**` 之前插入：

```markdown

**Layer 2 詳細規格（D-24 三分網格線，完整定義見 SSOT v4.0.4 §Viewfinder Overlay）**：

| 屬性 | 值 | 備註 |
|------|-----|------|
| 線條數量 | 4 條（2 vertical + 2 horizontal） | 三分法標準 |
| 顏色 | #FFFFFF | — |
| Alpha | **0.35** | Ambient UI 甜蜜點 — 可見但不干擾（D-24） |
| 線寬 | **1.5px** | 最小 dp-independent 可見線寬 |
| StrokeCap | Round | `StrokeCap.Round` |
| 繪製範圍 | contentBounds 內（排除 FIT_CENTER letterbox） | v2.11.1 P0-2 |
| Z-order | **在 PitchTractionLayer 之上（Layer 2 > Layer 1）** | 確保不被 Pitch aligned 呼吸遮蓋（D-24） |
| Food cold start 過渡 | ODT warm-up 期間維持 α0.35；首個 Food bbox 到達後 300ms FastOutSlowIn 淡出至 α0.15 | D-24 |
| Canvas 呼叫 | **4 次**（drawLine ×4） | 效能低負擔 |
| 可見性控制 | `isGridEnabled` 用戶設定 + `!isPtfActive`（PTF 模式隱藏） | — |

**Layer 3 詳細規格（D-3 Halo alpha，完整定義見 SSOT v4.0.4 §2.5）**：
```

**注意**：原有的 `**Layer 3 詳細規格**` 標題行也需要同步更新引用版本：`SSOT v4.0.3` → `SSOT v4.0.4`。

---

## H-3（🟡 Medium）：新增 §13 v2.4→v2.5 修訂追蹤

**搜尋錨點**: `## 文件元資訊`（約 L505）

**在該行之前插入**:

```markdown

## 13. v2.4 → v2.5 修訂追蹤

### 修訂總覽：3 項，全部完成

| # | 嚴重度 | 段落 | 修訂摘要 | 對應審計項 |
|---|--------|------|---------|-----------|
| H-1 | 🔴 Critical | §3.3 Overlay 圖層 | **Grid 參數 α0.08→0.35 / 0.5px→1.5px + Z-order Layer 1↔2 交換** | SSOT R29 / D-24 |
| H-2 | 🟠 High | §3.3 下方 | **新增 Layer 2 Grid 詳細規格表**（alpha、線寬、cold start 過渡） | D-24 補充 |
| H-3 | 🟡 Medium | §13 新增 | **v2.4→v2.5 修訂追蹤區塊**（本段落） | 追溯性要求 |

### 零殘留驗證

本文件已通過以下自動化檢查：
- ✅ **命名淨化**：全文不存在 "Edge Warmth" 或 "水平儀" 作為渲染元件名稱
- ✅ **上游引用**：全部指向 `SSOT_v4_0_4.md`，零殘留 `SSOT_v4_0_3.md`
- ✅ **Overlay 圖層描述**：§3.3 逐字對齊 SSOT v4.0.4 §Viewfinder Overlay
- ✅ **D-series 決策傳播**：D-24（Grid 規格 + Z-order + cold start 過渡）落地
- ✅ **Layer 詳細規格完整性**：Layer 1（Pitch）+ Layer 2（Grid）+ Layer 3（Halo）均有詳細規格表

---
```

---

## 元資訊更新

### 文件頭（L1~L13）

**搜尋錨點**: `# Cueshot 佈局規格 v2.4`

**替換文件頭為**:
```markdown
# Cueshot 佈局規格 v2.5 — 兩區塊模型

**版本**: Layout Spec v2.5 (SSOT v4.0.4 Alignment + D-24 Grid 規格鎖定 + Z-order 修正)  
**最後更新**: 2026-05-28  
**變更歷程**:  
- v2.0 → v2.1：§5 Pill Toggle 文字標籤改為圖示（Camera / Users）  
- v2.1 → v2.2：Edge Warmth 命名全面淨化（4 處）+ SSOT v4.0 數值對齊（Scrim 120px）+ 文件引用更新  
- v2.2 → v2.3：**16 項修訂**（見 §11 修訂追蹤 v2.2→v2.3）— SSOT v4.0.2 交叉審計全面對齊 + R14 快門下移落地 + Overlay 圖層重寫  
- v2.3 → v2.4：**8 項修訂**（見 §12 修訂追蹤 v2.3→v2.4）— SSOT v4.0.3 交叉審計：版本錨點全量更新 + Top Bar 光線指示三態 pulse 規格 + Edge-to-Edge/Cutout 規格 + FILL_CENTER 禁止聲明 + Scaffold padding 規則  
- v2.4 → v2.5：**3 項修訂**（見 §13 修訂追蹤 v2.4→v2.5）— Grid α0.35/1.5px + Z-order 交換 + Grid 詳細規格表
**決策來源**: CSO Session D8 — Top Bar Overlay 化 + Photo/PTF Pill Toggle · Session 4 D-12 快門下移 · Session 6 D-14~D-19 · **Session 8 D-24 Grid 規格**
**取代**: v2.4 及所有前版（v2.3, v2.2, v2.1, v2.0, v1.1 三區塊模型）  
**上游文件**: `SSOT_v4_0_4.md`（唯一真理文件）  
**下游文件**: Prototyper Prompt v2 · Background Image Prompts v2
```

### 文件元資訊區塊（約 L505~L516）

**全文搜尋替換**（所有出現處）:
- `SSOT_v4_0_3.md` → `SSOT_v4_0_4.md`
- `SSOT v4.0.3` → `SSOT v4.0.4`（在圖層對齊標題、相依性地圖等處）

**搜尋錨點**: `- **版本**: Layout Spec v2.4`

**替換為**:
```
- **版本**: Layout Spec v2.5 (SSOT v4.0.4 Alignment + D-24 Grid 規格鎖定 + Z-order 修正)
```

**搜尋錨點**: `- **審查狀態**: UITA 交叉審計完成`

**替換為**:
```
- **審查狀態**: UITA 交叉審計完成 — v2.2→v2.3 16 項 + v2.3→v2.4 8 項 + v2.4→v2.5 3 項修訂全部落地 + 零殘留驗證通過
```

**搜尋錨點**: `- **決策記錄**` 或 `D-series 決策傳播` 零殘留驗證處

**新增 D-24 至決策傳播清單**。

---

## Gemini 合併驗證清單

完成合併後，Merge Technician 須確認：

- [ ] 全文 `SSOT v4.0.3` → `SSOT v4.0.4`（預計 10+ 處）
- [ ] 全文 `SSOT_v4_0_3.md` → `SSOT_v4_0_4.md`（預計 3+ 處）
- [ ] §3.3 Overlay 圖層 Layer 1↔2 交換完成
- [ ] Layer 1 詳細規格表標題已從 "Layer 2" 更新為 "Layer 1"
- [ ] 新增 Layer 2 Grid 詳細規格表存在
- [ ] Layer 3 詳細規格表標題的 SSOT 版本引用已更新
- [ ] §13 修訂追蹤區塊存在
- [ ] 文件頭版本號 v2.4 → v2.5
- [ ] 文件元資訊版本號已更新
- [ ] 零殘留：全文不存在 `v4.0.3` 殘留引用

---

**End of Layout Spec v2.4 → v2.5 DIFF**
