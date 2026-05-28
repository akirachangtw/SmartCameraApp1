# UITA_INDEX.md — UITA 對話唯一常駐文件

**版本**: v1.2
**用途**: 取代 Project Knowledge 全量載入，作為按需讀取的導航 index
**常駐方式**: Project Knowledge（唯一一份）
**Handoff**: 由 Akira Upload（保留對話前後順序識別）
**變更記錄**: v1.1→v1.2 — CSO S15 審查修正（刪除 §2.1 CSO 殘留、重寫必讀矩陣、修正 §7 標題、新增 reports/ 目錄）

---

## §1 文件地圖

**根目錄**: `D:\DevSources\AndroidProj\SmartCameraApp1`

### 核心文件（審查前必讀）

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| SSOT | 唯一真理文件 v4.0.5 | `plans/architecture/SSOT_v4_0_5.md` | 任何技術規格判斷 |
| LAYOUT | Layout Spec v2.5.1 | `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md` | UI 佈局 / 座標 / Insets 判斷 |

### 狀態追蹤

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| SOP | Session SOP v2.1 | `plans/uita_project/UITA_Session_Startup_SOP.md` | 首次設定參考 / 審查流程確認 |

### 審查報告 / 歸檔

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| REPORTS | 審查報告目錄 | `plans/uita_project/reports/` | 追溯歷史審查裁決 |

---

## §2 審查前必讀矩陣

| 審查類型 | 必讀 | 選讀 |
|---------|------|------|
| Traction 通道設計審查（Pitch / Level / Halo） | SSOT §2（對應子章節）+ SSOT §10（效能預算） | Layout Spec |
| 場景引擎 UI 審查 | SSOT §3 + SSOT §2（Ambient UI 哲學） | 當前 Phase Contract |
| Focus 系統設計審查 | SSOT 附錄 A + Layout Spec | SSOT §10 |
| 新 Composable / Overlay 審查 | Layout Spec + SSOT §10 | SSOT 對應功能章節 |
| 實機異常診斷（Step 5 回流） | SSOT 對應章節 + Layout Spec | 歷史審查報告（reports/） |

---

## §3 SSOT 章節速查

| 章節 | 內容 | 搜尋關鍵字 |
|------|------|-----------|
| §1 | 產品定位 | `## 第一部分：產品定位` |
| §2 | Traction 三通道 | `## 第二部分：Ambient UI` |
| §2.0 | 全局常數 | `object AmbientConstants` |
| §2.3 | Pitch Traction | `### 2.3 Pitch Traction` |
| §2.4 | Level Traction | `### 2.4 Level Traction` |
| §2.5 | Halo Traction | `### 2.5 Halo Traction` |
| §3 | 場景引擎 | `## 第三部分：場景判斷` |
| §4 | 規則引擎 | `## 第四部分：規則引擎` |
| §7 | 評分 + EXIF | `## 第七部分：評分` |
| §10 | 效能預算 | `## 第十部分：效能` |
| 附錄 A | Focus 動畫規格 | `## 附錄 A：Focus` |
| 附錄 B | 全局決策記錄 | `## 附錄 B：全局決策` |

---

## §4 目錄結構總覽

```
plans/uita_project/
├── UITA_INDEX.md                    ← 本文件（Project Knowledge 唯一常駐）
├── UITA_Session_Startup_SOP.md      ← Session SOP v2.1
├── file/                            ← Project File（按需讀取）
└── reports/                         ← 審查報告歸檔

plans/architecture/
├── Cueshot_Layout_Spec_v2_5_1.md
└── SSOT_v4_0_5.md

```

---

## §5 Session 開場 Checklist

```
□ 1. 讀取 Akira Upload 的 Handoff（確認 Done/TODO/Decisions）
□ 2. 確認本次審查對象（哪個設計方案 / Ticket / 實機異常）
□ 3. 查 §2 必讀矩陣 → 按需讀取對應 SSOT 章節
□ 4. 開始審查（依 SOP 八維度 + 六步 Pipeline）
```

## §6 Session 收場 Checklist

```
□ 1. 輸出裁決報告（PASS / CONDITIONAL / FAIL）
□ 2. 產出新 Handoff 供 Akira 下次 Upload
□ 3. 關鍵技術決策歸檔 → Obsidian archive_agent_decision
□ 4. Token 監控狀態報告
```

---

## §7 載入策略

| 指標 | 值 |
|------|-----|
| Project Knowledge 文件數 | **1 份**（本文件） |
| 常駐 Token | **~1.5K (< 1%)** |
| 可用推理空間 | **~198K (99%)** |
| 按需讀取成本 | ~1-8K/次（視 SSOT 章節大小） |
| MCP 斷線影響 | 降級為手動 Upload |

---

**End of UITA_INDEX.md v1.2**
