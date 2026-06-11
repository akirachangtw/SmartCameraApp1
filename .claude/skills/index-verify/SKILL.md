---
name: index-verify
description: 日常維運：列出 CSO/UITA 角色目錄與 plans/architecture 檔案清單，校對 INDEX/SOP 引用，自動刪除缺檔引用並新增未索引檔案。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Description
列出 CSO / UITA 角色目錄與 `plans/architecture/` 所有檔案，校對 INDEX / Session Startup SOP 引用路徑有效性，自動刪除缺檔引用，自動新增未索引檔案引用，並掃描已知過期版本引用（SSOT / Layout Spec 版號）。

# Prompt
執行 INDEX 維運驗證：

```bash
bash .claude/skills/index-verify/scripts/index_path_verify.sh
```

## 驗證範圍

| 角色 | 角色目錄 | 校對文件 |
|------|----------|----------|
| CSO | `plans/cso_briefing/` | `plans/cso_briefing/CSO_INDEX.md` + `plans/cso_briefing/CSO_Session_Startup_SOP.md` |
| UITA | `plans/uita_project/` | `plans/uita_project/UITA_INDEX.md` + `plans/uita_project/UITA_Session_Startup_SOP.md` |
| Architecture | `plans/architecture/` | 由所有 CSO/UITA INDEX + SOP 共同引用 |

## 執行順序

1. **MUST** 先列出 `plans/cso_briefing/`、`plans/uita_project/`、`plans/architecture/` 所有檔案清單。
2. **MUST** 校對 INDEX / Session Startup SOP 中所有反引號包裹的 `plans/` 或 `.claude/` 路徑。
3. **MUST** 若引用檔案或目錄不存在，直接從對應 INDEX/SOP 刪除相關引用：
   - 表格列或清單列：刪除整列。
   - 一般段落：只刪除該反引號 path token。
4. **MUST** 對角色目錄與 `plans/architecture/` 做覆蓋率檢查；存在但未被引用的檔案自動新增至 INDEX：
   - CSO 角色目錄新增檔案 → 寫入 `plans/cso_briefing/CSO_INDEX.md` 的 `Auto Index: CSO Role Files` 區塊。
   - UITA 角色目錄新增檔案 → 寫入 `plans/uita_project/UITA_INDEX.md` 的 `Auto Index: UITA Role Files` 區塊。
   - `plans/architecture/` 新增檔案 → 同步寫入 CSO / UITA 兩份 INDEX 的 `Auto Index: Architecture Files` 區塊。

## 退出碼對應行動

| 碼 | 意義 | 行動 |
|----|------|------|
| 0 | 全部通過（含 ⚠️ 警告 / 🧹 自動刪除 / 🧾 自動新增） | ✅ 檢查 diff，確認自動維護符合預期 |
| 1 | ❌ 仍有路徑不存在或目錄缺失 | **MUST** 修正 INDEX/SOP 文件中的路徑，或補建遺漏的檔案/目錄 |
| 2 | 環境問題 | **MUST** 確認在專案根目錄執行 |

## ⚠️ 警告項目處理規則

| 警告類型 | 處理方式 |
|---------|---------|
| 過期 SSOT/Layout 引用（CLAUDE.md / PROGRESS.md） | **MUST** 需 Ticket + Cross-Review Checkpoint 才能修正 |
| 過期引用（INDEX 文件本身） | **SHOULD** 可直接修正 INDEX 文件 |
| 大小寫重複檔案 | **MUST** 確認正確版本後刪除重複 |

## 回報格式

```
=== Index Verify Report ===
📁 Scope File Inventory: CSO / UITA / Architecture listed
📁 CSO INDEX+SOP: ✅ [N] 通過 / ❌ [N] 失敗 / 🧹 [N] 自動刪除 / 🧾 [N] 自動新增
📁 UITA INDEX+SOP: ✅ [N] 通過 / ❌ [N] 失敗 / 🧹 [N] 自動刪除 / 🧾 [N] 自動新增
⚠️  警告: [N] 項（過期引用 / 大小寫重複）
→ 結論: PASS / FAIL
```
