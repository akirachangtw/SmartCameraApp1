---
name: index-verify
description: 日常維運：列出 CSO/UITA 角色目錄與 plans/architecture 檔案清單，校對 INDEX/SOP 引用，自動刪除缺檔引用、智慧路由新增檔案至對應 Section（核心文件/審查報告）、動態版本偵測、空白行清理。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Description
列出 CSO / UITA 角色目錄與 `plans/architecture/` 所有檔案，校對 INDEX / Session Startup SOP 引用路徑有效性，自動刪除缺檔引用，智慧路由未索引檔案至 INDEX 對應 Section，掃描動態版本過期引用，並壓縮多餘空白行。

# Prompt
執行 INDEX 維運驗證：

```bash
bash .claude/skills/index-verify/scripts/index_path_verify.sh
```

## 驗證範圍

「角色目錄」供 ⓪ 缺檔清理 + ① 引用驗證/跨角色邊界（全樹）；「檔案異動管理夾（②）」供目錄覆蓋率 + 智慧路由（僅此三夾會被掃描新增至 INDEX）。

| 角色 | 角色目錄（⓪/① 邊界） | 檔案異動管理夾（② 覆蓋率+路由） | 校對文件 |
|------|----------|----------|----------|
| CSO | `plans/cso_briefing/` | `plans/cso_briefing/project/file/` | `plans/cso_briefing/CSO_INDEX.md` + `plans/cso_briefing/CSO_Session_Startup_SOP.md` |
| UITA | `plans/uita_project/` | `plans/uita_project/file/` | `plans/uita_project/UITA_INDEX.md` + `plans/uita_project/UITA_Session_Startup_SOP.md` |
| Architecture | `plans/architecture/` | `plans/architecture/` | 由所有 CSO/UITA INDEX + SOP 共同引用 |

## 智慧路由規則

新增檔案依路徑 pattern 自動路由至 INDEX 對應 Section（取代底部泛用 Auto-Index 區塊）：

| 角色 | 路徑 Pattern | 路由目標 Section | 預設讀取時機 |
|------|-------------|-----------------|------------|
| CSO | `project/file/*` | 核心文件 | 按需讀取 |
| CSO | `reports/*` | 審查報告 | 追溯歷史裁決 |
| UITA | `file/*` | 核心文件 | 按需讀取 |
| UITA | `reports/*` | 審查報告 | 追溯歷史審查裁決 |
| 任一 | 不匹配上述 | fallback Auto-Index 區塊 | 按需讀取 |

**MUST** 路由至 Section 的新行使用 `| short_name | filename | path | timing |` 四欄格式。
**SHOULD** 路由後手動補正 ID 欄位（script 以 short_name 暫代）。

> ⚠️ 現行 ② 掃描範圍已收斂至 `project/file/`（CSO）/ `file/`（UITA）。`reports/*` 路由規則**永久定案為引用登記制**：路由 pattern 表項保留僅供人工引用登記格式參考，**MUST** 永久禁止 `scan_dir` 擴張涵蓋 `reports/`；`reports/*` 檔案登記 **MUST** 僅透過人工於 INDEX 手動新增引用行，**MUST NOT** 恢復資料夾掃描（Akira 2026-07-19 對話裁決最終定案）。（INFRA-INDEXVERIFY-SCOPE-01）

## 版本偵測模式

- 自動掃描 `plans/architecture/` 取最新 SSOT / Layout Spec 版本
- 所有 INDEX/SOP/CLAUDE.md/PROGRESS.md 中低於最新版的引用自動標記 ⚠️
- **MUST NOT** 硬編碼版本號至 script 中

## SOP 語意保護

- INDEX 缺檔引用 → 🧹 自動刪除（純導航，低影響）
- SOP 缺檔引用 → 🔴 自動刪除 + 語意斷裂告警（操作流程，高影響）

## 空白行清理

每次執行後自動壓縮：
- 連續 2+ 空白行 → 最多 1 行
- 檔尾多餘空白行 → 移除

## 執行順序

1. **MUST** 列出檔案異動管理夾清單：`plans/cso_briefing/project/file/`、`plans/uita_project/file/`、`plans/architecture/`
2. **MUST** ⓪ 缺檔引用清理（INDEX + SOP 全 ref，SOP 附語意告警）
3. **MUST** ① INDEX/SOP 引用驗證（存在性 + 角色邊界，全 ref）
4. **MUST** ② 目錄覆蓋率 + 智慧路由（僅掃三個管理夾：`.../file/`→核心文件、`plans/architecture/`→跨角色 fallback）
5. **MUST** ③ 空白行清理
6. **MUST** 動態過期版本引用掃描
7. **MUST** 大小寫重複偵測

## 退出碼對應行動

| 碼 | 意義 | 行動 |
|----|------|------|
| 0 | 全部通過（含 ⚠️ 警告 / 🧹 自動修正 / 📌 路由新增） | ✅ 檢查 diff，確認路由和清理符合預期 |
| 1 | ❌ 仍有路徑不存在或目錄缺失 | **MUST** 修正 INDEX/SOP 文件中的路徑，或補建遺漏的檔案/目錄 |
| 2 | 環境問題 | **MUST** 確認在專案根目錄執行 |

## ⚠️ 警告項目處理規則

| 警告類型 | 處理方式 |
|---------|---------|
| 過期 SSOT/Layout 引用（CLAUDE.md / PROGRESS.md） | **MUST** 需 Ticket + Cross-Review Checkpoint 才能修正 |
| 過期引用（INDEX 文件本身） | **SHOULD** 可直接修正 INDEX 文件 |
| 大小寫重複檔案 | **MUST** 確認正確版本後刪除重複 |
| 🔴 SOP 語意引用被刪除 | **MUST** 手動檢查 SOP 載入指令是否需補正 |

## 回報格式

```
=== Index Verify Report v2.0 ===
📁 Scope File Inventory: CSO / UITA / Architecture listed
📁 CSO INDEX+SOP: ✅ [N] 通過 / ❌ [N] 失敗 / 🧹 [N] 自動修正 / 📌 [N] 路由新增 / 🧾 [N] fallback 新增
📁 UITA INDEX+SOP: ✅ [N] 通過 / ❌ [N] 失敗 / 🧹 [N] 自動修正 / 📌 [N] 路由新增 / 🧾 [N] fallback 新增
📌 Version: SSOT=[current] / Layout=[current]
⚠️  警告: [N] 項（過期引用 / 大小寫重複 / SOP 語意斷裂）
→ 結論: PASS / FAIL
```
