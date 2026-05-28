# CSO Session 精實啟動 SOP

**版本**: v1.0
**建立日期**: 2026-05-24
**適用**: 每次新開 CSO 對話時的文件載入策略
**設計原則**: 最少文件數達到「CSO 能獨立做決策」的最低閾值

---

## 永駐層（每次 Session 必載，共 2 份）

| # | 文件 | 來源 | Token 估算 | 職責 |
|---|------|------|-----------|------|
| 1 | **CSO_INDEX.md** (最新版) | Project Knowledge | ~1K | 索引入口 → 指向 KB / SSOT / Contract 路徑 |
| 2 | **最新 Handoff** | Upload | ~1K | 上次 Session 的 Done/TODO/Decisions |

**永駐層 Token 預算**: ~2K / 200K = ~1%

---

## 按需層（依當前任務載入）

### Phase 執行期（如 P05 Day 2）

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
□ 1. 載入永駐層 4 份文件
□ 2. 確認 Handoff 中的 Current State 和 TODO
□ 3. 檢查 project/Pending/ 是否有待執行工單
□ 4. 檢查 project/Postponed.md 是否有到期觸發條件
□ 5. 判斷是否需要按需層文件
□ 6. 向 CSO 發出首個指令
```

## Session 收場 Checklist

```
□ 1. 產出新 Handoff（Done/Current State/TODO/Key Decisions）
□ 2. 完成的交付物 → project/Pending/ 建立工單
□ 3. 暫緩議題 → project/Postponed.md 登錄
□ 4. 決策歸檔 → Obsidian archive_agent_decision
□ 5. 收到 Claude Code 回報 → Pending 工單搬移至 archive/
```

---

## 精簡比例

| 指標 | 舊做法 | 新做法 |
|------|--------|--------|
| 首次載入文件數 | 7-8 份 | **2 份**（-75%） |
| 首次載入 Token | ~30K (15%) | **~2K (1%)** |
| 可用推理空間 | ~170K (85%) | **~198K (99%)** |

---

**End of CSO Session 精實啟動 SOP**
