# CSO_INDEX.md — CSO 對話唯一常駐文件

**版本**: v1.1
**用途**: 取代 Project Knowledge 全量載入，作為按需讀取的導航 index
**常駐方式**: Project Knowledge（唯一一份）— 僅保留靜態導航內容
**Handoff**: 由 Akira Upload（保留對話前後順序識別）
**操作 SOP**: `plans/cso_briefing/CSO_Session_Startup_SOP.md`（Session 流程 / 目錄結構 / 載入策略）

---

## §1 文件地圖

**根目錄**: `D:\DevSources\AndroidProj\SmartCameraApp1`

### 核心文件（決策前必讀）

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| SSOT | 唯一真理文件 v4.0.5 | `plans/architecture/SSOT_v4_0_5.md` | 任何技術規格判斷 |
| KB | CSO Knowledge Base v2 | `plans/cso_briefing/project/file/Cueshot_CSO_Knowledge_Base_v2.md` | 產品 DNA / 拷問模板 / gstack |
| CONTRACT | 當前活躍 Contract | `plans/cso_briefing/project/file/P08_Codebase_Sanitization_Contract.md` | 票定義 / 禁區 / 驗收 |
| LAYOUT | Layout Spec v2.5.1 | `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md` | UI 佈局判斷 |
| EFFORT | Model Effort 解析 | `plans/cso_briefing/project/file/Sonnet4.6-Effort等級解析.md` | Mode / Effort 指定 |
| TICKET_TPL | Ticket 範本 v3 | `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md` | 開票格式參考 |

### 執行體系文件

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| CLAUDE | Claude Code 行為契約 | `.claude/CLAUDE.md` | 規則收割 / KAIROS |
| RULES | Claude Code 路徑規則 | `.claude/rules/*.md` | 特定領域審查 |
| PROGRESS | 進度 Dashboard | `.claude/PROGRESS.md` | Session 開場確認狀態 |

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
| SCORCHED | Scorched Earth 報告 | `plans/cso_briefing/reports/scorched_earth_report.md` | 架構健康度回顧 |
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

**End of CSO_INDEX.md v1.1**
> 目錄結構 / Session Checklist / 載入策略 → 見 `CSO_Session_Startup_SOP.md`
