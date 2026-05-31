---
name: index-verify
description: 日常維運：驗證 CSO_INDEX.md 與 UITA_INDEX.md 所有引用路徑存在性 + 過期版本引用掃描。
disable-model-invocation: false
---

# Description
驗證 CSO_INDEX.md / UITA_INDEX.md 引用路徑有效性，並掃描已知過期版本引用（SSOT / Layout Spec 版號）。

# Prompt
執行 INDEX 維運驗證：

```bash
bash .claude/skills/index-verify/scripts/index_path_verify.sh
```

## 退出碼對應行動

| 碼 | 意義 | 行動 |
|----|------|------|
| 0 | 全部通過（含 ⚠️ 警告） | ✅ 無需處理 |
| 1 | ❌ 路徑不存在 | 修正 INDEX 文件中的路徑，或補建遺漏的檔案/目錄 |
| 2 | 環境問題 | 確認在專案根目錄執行 |

## ⚠️ 警告項目處理規則

| 警告類型 | 處理方式 |
|---------|---------|
| 過期 SSOT/Layout 引用（CLAUDE.md / PROGRESS.md） | 需 Ticket + Cross-Review Checkpoint 才能修正 |
| 過期引用（INDEX 文件本身） | 可直接修正 INDEX 文件 |
| 大小寫重複檔案 | 確認正確版本後刪除重複 |

## 回報格式

```
=== Index Verify Report ===
📁 CSO_INDEX.md: ✅ [N] 通過 / ❌ [N] 失敗
📁 UITA_INDEX.md: ✅ [N] 通過 / ❌ [N] 失敗
⚠️  警告: [N] 項（過期引用 / 大小寫重複）
→ 結論: PASS / FAIL
```
