# CSO_INDEX.md — CSO 對話唯一常駐文件

**版本**: v1.0
**用途**: 取代 Project Knowledge 全量載入，作為按需讀取的導航 index
**常駐方式**: Project Knowledge（唯一一份）
**Handoff**: 由 Akira Upload（保留對話前後順序識別）

---

## §1 文件地圖

**根目錄**: `D:\DevSources\AndroidProj\SmartCameraApp1`

### 核心文件（決策前必讀）

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| SSOT | 唯一真理文件 v4.0.2 | `plans/architecture/SSOT_v4_0_2.md` | 任何技術規格判斷 |
| KB | CSO Knowledge Base v2 | `plans/cso_briefing/project/file/Cueshot_CSO_Knowledge_Base_v2.md` | 產品 DNA / 拷問模板 / gstack |
| CONTRACT | 當前活躍 Contract | `plans/cso_briefing/project/file/P05_Photo_Mode_Recovery_Contract.md` | 票定義 / 禁區 / 驗收 |
| LAYOUT | Layout Spec v2.3 | `plans/architecture/Cueshot_Layout_Spec_v2_3.md` | UI 佈局判斷 |
| EFFORT | Model Effort 解析 | `plans/cso_briefing/project/file/Sonnet4.6-Effort等級解析.md` | Mode / Effort 指定 |

### 執行體系文件

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| CLAUDE | Claude Code 行為契約 | `.claude/CLAUDE.md` | 規則收割 / KAIROS |
| RULES | Claude Code 路徑規則 | `.claude/rules/*.md` | 特定領域審查 |
| PROGRESS | 進度 Dashboard | `.claude/PROGRESS.md` | Session 開場確認狀態 |

### 角色 Prompt 文件

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| GPT-ORDER | GPT 執行令 Prompt | `plans/cso_briefing/role/Cueshot_GPT55_Execution_Order_v1.md` | 產出 GPT 修補單 |
| GPT-PHASE | GPT Phase B Prompt | `plans/cso_briefing/role/Cueshot_PhaseB_GPT55_Prompt_v1.md` | GPT 審查 |
| GEMINI | Gemini CoS PDCA Prompt | `plans/cso_briefing/role/Gemini PDCA Role Prompt.md` | Dispatch 產出 |

### 狀態追蹤

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| PENDING | 待執行工單 | `plans/cso_briefing/project/Pending/` | Session 開場 |
| POSTPONED | 暫緩議題 | `plans/cso_briefing/project/Postponed.md` | KAIROS / Session 開場 |
| SOP | Session SOP | `plans/cso_briefing/CSO_Session_Startup_SOP.md` | 首次設定參考 |

### 審查報告 / 歸檔

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| BHA | 架構健康度報告 | `.claude/bha_report.md` | 架構變更決策 |
| AUDIT | 完整審計 + 票清單 | `plans/cso_briefing/reports/full_audit_and_tickets.md` | Wave 回顧 |
| ARCHIVE | CSO 歷史交付物 | `plans/cso_briefing/archive/` | 追溯歷史決策 |

---

## §2 決策前必讀矩陣

| 決策類型 | 必讀 | 選讀 |
|---------|------|------|
| 技術規格裁決 | SSOT（相關章節） | CONTRACT |
| 票修補裁決 | CONTRACT + SSOT（相關附錄） | CLAUDE（確認禁區） |
| 架構變更 | SSOT + LAYOUT | RULES（相關領域） |
| 規則收割（KAIROS） | CLAUDE + RULES | KB + POSTPONED |
| 跨模型流程優化 | KB + EFFORT | GPT-ORDER / GEMINI |
| Postponed 回顧 | POSTPONED | 各項原始文件 |
| Mode / Effort 指定 | EFFORT | KB（gstack 角色矩陣） |

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
plans/cso_briefing/
├── CSO_INDEX.md                    ← 本文件（Project Knowledge 唯一常駐）
├── CSO_Session_Startup_SOP.md      ← Session SOP
├── project/
│   ├── file/                       ← Project File（KB / Contract / Effort）
│   │   ├── Cueshot_CSO_Knowledge_Base_v2.md
│   │   ├── P05_Photo_Mode_Recovery_Contract.md
│   │   └── Sonnet4.6-Effort等級解析.md
│   ├── Pending/                    ← 待執行提交
│   │   └── T7_Addendum_LevelFadeRange.md
│   └── Postponed.md                ← 暫緩執行
├── role/                           ← 角色 Prompt
│   ├── Cueshot_GPT55_Execution_Order_v1.md
│   ├── Cueshot_PhaseB_GPT55_Prompt_v1.md
│   └── Gemini PDCA Role Prompt.md
├── reports/                        ← 審查報告
│   ├── full_audit_and_tickets.md
│   └── scorched_earth_report.md
└── archive/                        ← 歷史封存
    └── (11 份歷史文件)
```

---

## §5 Session 開場 Checklist

```
□ 1. Filesystem:read_text_file → .claude/PROGRESS.md（確認狀態）
□ 2. Filesystem:list_directory → project/Pending/（檢查待執行工單）
□ 3. 讀取 Akira Upload 的 Handoff（確認 Done/TODO/Decisions）
□ 4. 判斷本次議題 → 查 §2 必讀矩陣 → 按需讀取
□ 5. 開始推演
```

## §6 Session 收場 Checklist

```
□ 1. 產出新 Handoff 供 Akira 下次 Upload
□ 2. 完成的交付物 → project/Pending/ 建立工單
□ 3. 暫緩議題 → project/Postponed.md 登錄
□ 4. 決策歸檔 → Obsidian archive_agent_decision
□ 5. Token 監控狀態報告
```

---

## §7 載入策略對比

| 指標 | 舊（4 份 Project Knowledge） | 新（1 份 Index + Filesystem） |
|------|------|------|
| Project Knowledge 文件數 | 4 | **1** |
| 常駐 Token | ~19K (10%) | **~2K (1%)** |
| 可用推理空間 | 181K (90%) | **198K (99%)** |
| 按需讀取成本 | 0（已全載） | ~1-8K/次（視章節大小） |
| MCP 斷線影響 | 無 | 降級為手動 Upload |

---

**End of CSO_INDEX.md v1.0**
