# CSO Session 精實啟動 SOP

**版本**: v1.2
**建立日期**: 2026-05-24 | **最後更新**: 2026-06-01
**適用**: 每次新開 CSO 對話時的文件載入策略
**設計原則**: 最少文件數達到「CSO 能獨立做決策」的最低閾值
**靜態導航索引**: `plans/cso_briefing/CSO_INDEX.md`（Project Knowledge 唯一常駐）

---

## 現行版本指針（Session 結束時如有升版即更新）

| ID | 精確路徑 |
|----|---------|
| SSOT | `plans/architecture/SSOT_v4_0_5.md` |
| LAYOUT | `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md` |
| CONTRACT | `plans/cso_briefing/project/file/P05_Photo_Mode_Recovery_Contract.md` |

---

## 永駐層（每次 Session 必載，共 2 份）

| # | 文件 | 來源 | Token 估算 | 職責 |
|---|------|------|-----------|------|
| 1 | **CSO_INDEX.md** (最新版) | Project Knowledge | ~1K | 索引入口 → 指向 KB / SSOT / Contract 路徑 |
| 2 | **最新 Handoff** | Upload | ~1K | 上次 Session 的 Done/TODO/Decisions |

**永駐層 Token 預算**: ~2K / 200K = ~1%

---

## 按需層（依當前任務載入）

### Phase 執行期（當前：P05 / D12）

| 文件 | 載入條件 | 取得方式 |
|------|---------|---------|
| 對焦系統完整架構解析 | T3 CameraX AF 接線討論時 | `Filesystem:read_text_file` |
| Traycer Workflow v2.0.1 | Traycer execute_review / strict_verification 前 | `Filesystem:read_text_file` |
| SSOT（最新版） | 技術決策需精確引用時 | `Filesystem:read_text_file` |
| 當前 Phase Contract | 票據禁區 / 驗收標準查閱時 | `Filesystem:read_text_file` |

### KAIROS 節點

| 文件 | 載入條件 | 取得方式 |
|------|---------|---------|
| gstack v2.0 | 角色矩陣 + SOP 收錄至 CLAUDE.md / CSO KB 時 | `Filesystem:read_text_file` |
| project/Postponed.md | 暫緩議題回顧 | `Filesystem:read_text_file` |
| Obsidian 決策 index | `read_decision_index` 工具調用即可 | 工具（0 Token） |

### Wave 2 啟動時

| 文件 | 載入條件 | Token |
|------|---------|-------|
| CameraScreen 重構文件 | W2 三票（triple-strike）規劃 | TBD |
| BHA Report | 架構健康度基準回顧 | ~3K |

---

## 已移除層（不再載入）

| 文件 | 移除理由 |
|------|---------|
| ClaudeCode-Model-Effort.txt | 已內化為 Slice Protocol v2.2 §Mode/Effort 欄位 |
| 舊版 Handoff（被新版取代後） | 內容已被新 Handoff 覆蓋 |
| gstack v1.5 | 被 v2.0 作廢 |

---

## Session 開場 Checklist

```
□ 1. 確認永駐層已載入（CSO_INDEX.md × 1 + 最新 Handoff × 1，共 2 份）
□ 2. Filesystem:read_text_file → .claude/PROGRESS.md（確認當前狀態）
□ 3. Filesystem:list_directory → project/Pending/（檢查待執行工單）
□ 4. 確認 Handoff 中的 Current State / TODO / Key Decisions
□ 5. 檢查 project/Postponed.md 是否有到期觸發條件
□ 6. 判斷本次議題 → 查 CSO_INDEX §2 必讀矩陣 → 按需讀取
□ 7. 向 CSO 發出首個指令
```

## Session 收場 Checklist

```
□ 1. 產出新 Handoff（Done/Current State/TODO/Key Decisions）
□ 2. 完成的交付物 → project/Pending/ 建立工單
□ 3. 暫緩議題 → project/Postponed.md 登錄
□ 4. 決策歸檔 → Obsidian archive_agent_decision
□ 5. 收到 Claude Code 回報 → Pending 工單搬移至 archive/
□ 6. Token 監控狀態報告
```

---

## 目錄結構總覽

```
plans/cso_briefing/
├── CSO_INDEX.md                    ← Project Knowledge 靜態索引（唯一常駐）
├── CSO_Session_Startup_SOP.md      ← 本文件（Session 流程 / 動態維護）
├── project/
│   ├── file/                       ← Project File（KB / Contract / Effort / Template）
│   │   ├── Cueshot_CSO_Knowledge_Base_v2.md
│   │   ├── P05_Photo_Mode_Recovery_Contract.md
│   │   ├── Sonnet4.6-Effort等級解析.md
│   │   └── Unified_Ticket_Template_v3.md
│   ├── Pending/                    ← 待執行提交
│   │   ├── T7_Addendum_LevelFadeRange.md
│   │   └── Pipeline_v1_6_Implementation.md
│   └── Postponed.md                ← 暫緩執行
├── reports/                        ← 審查報告
│   ├── full_audit_and_tickets.md
│   └── scorched_earth_report.md
└── archive/                        ← 歷史封存（含 SSOT/Layout DIFF 版本）
    └── (13 份歷史文件)

plans/architecture/
├── SSOT_v4_0_5.md                  ← 唯一真理文件（現行）
└── Cueshot_Layout_Spec_v2_5_1.md   ← 佈局精確定義（現行）
```

---

## 載入策略對比

| 指標 | 舊（4 份 Project Knowledge） | 新（1 份 Index + Filesystem） |
|------|------------------------------|-------------------------------|
| Project Knowledge 文件數 | 4 | **1** |
| 常駐 Token | ~19K (10%) | **~2K (1%)** |
| 可用推理空間 | ~170K (85%) | **~198K (99%)** |
| 按需讀取成本 | 0（已全載） | ~1-8K/次（視章節大小） |
| MCP 斷線影響 | 無 | 降級為手動 Upload |

---

**End of CSO Session 精實啟動 SOP v1.1**
