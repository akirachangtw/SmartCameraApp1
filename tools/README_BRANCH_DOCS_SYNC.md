# Branch Documentation Sync Checker

## 目的

在合併分支到 `main` 之前，自動檢查並比對 `devspec/` 和 `tools/` 目錄中的文檔檔案，防止重要文檔遺失。

## 背景

在 v2.10.0 和 v2.11.0 開發過程中，發現多個分支（如 `feature/aspect-ratio-usecasegroup-v2.10.0`、`feature/optical-flow-phase5-6`）中新增的文檔檔案沒有被合併到主分支，導致文檔遺失。

**遺失檔案範例**：
- `devspec/stage_verification_checklist.md`
- `tools/stage_verification_gui.pyw`
- `tools/regression_scanner.py`
- 等共 12 個檔案

## 使用方法

### Windows (PowerShell)

```powershell
# 比對當前分支與 main
.\tools\check_branch_docs_sync.ps1 feature/camera-core-v2.11.0

# 比對兩個指定分支
.\tools\check_branch_docs_sync.ps1 feature/camera-core-v2.11.0 main
```

### Linux/macOS (Bash)

```bash
# 賦予執行權限（首次使用）
chmod +x tools/check_branch_docs_sync.sh

# 比對當前分支與 main
./tools/check_branch_docs_sync.sh feature/camera-core-v2.11.0

# 比對兩個指定分支
./tools/check_branch_docs_sync.sh feature/camera-core-v2.11.0 main
```

## 檢查範圍

### 檔案類型
- Markdown 文檔 (`.md`)
- Python 腳本 (`.py`, `.pyw`)
- Shell 腳本 (`.ps1`, `.sh`)

### 目錄
- `devspec/` - 開發規範文檔
- `tools/` - 自動化工具腳本

## 輸出範例

### 有差異時

```
🔍 Checking devspec/ and tools/ documentation sync...
   Source: feature/camera-core-v2.11.0
   Target: main

📂 Listing files in devspec/...
📂 Listing files in tools/...

================================================================================
📋 devspec/ Comparison
================================================================================

⚠️  Files in feature/camera-core-v2.11.0 but NOT in main:
   + devspec/stage_verification_checklist.md
   + devspec/pre_commit_checklist.md

================================================================================
🛠️  tools/ Comparison
================================================================================

⚠️  Files in feature/camera-core-v2.11.0 but NOT in main:
   + tools/stage_verification_gui.pyw
   + tools/regression_scanner.py

================================================================================
📊 Summary
================================================================================

Missing in main : 4 files
Extra in main   : 0 files

💡 Recommendation:
   Before merging to main, restore missing files:

   # Restore devspec/ files:
   git show feature/camera-core-v2.11.0:devspec/stage_verification_checklist.md > devspec/stage_verification_checklist.md
   git show feature/camera-core-v2.11.0:devspec/pre_commit_checklist.md > devspec/pre_commit_checklist.md

   # Restore tools/ files:
   git show feature/camera-core-v2.11.0:tools/stage_verification_gui.pyw > tools/stage_verification_gui.pyw
   git show feature/camera-core-v2.11.0:tools/regression_scanner.py > tools/regression_scanner.py

⚠️  Documentation sync check completed with differences
```

### 完全同步時

```
🔍 Checking devspec/ and tools/ documentation sync...
   Source: feature/camera-core-v2.11.0
   Target: main

📂 Listing files in devspec/...
📂 Listing files in tools/...

================================================================================
📋 devspec/ Comparison
================================================================================

✅ devspec/ files are in sync

================================================================================
🛠️  tools/ Comparison
================================================================================

✅ tools/ files are in sync

================================================================================
📊 Summary
================================================================================

Missing in main : 0 files
Extra in main   : 0 files

✅ All documentation is in sync!
```

## 整合到工作流程

### 方法 1: PR 創建前檢查

在 `devspec/pr_creation_guide.md` 中添加檢查步驟：

```markdown
## PR 創建前檢查清單

1. [ ] 代碼審查通過
2. [ ] 測試通過
3. [ ] **文檔同步檢查**：
   ```bash
   ./tools/check_branch_docs_sync.sh $(git rev-parse --abbrev-ref HEAD) main
   ```
4. [ ] 提交 PR
```

### 方法 2: VSCode Task

在 `.vscode/tasks.json` 中添加：

```json
{
    "label": "Check Branch Docs Sync",
    "type": "shell",
    "command": "./tools/check_branch_docs_sync.sh",
    "args": [
        "${input:sourceBranch}",
        "${input:targetBranch}"
    ],
    "presentation": {
        "echo": true,
        "reveal": "always",
        "panel": "new"
    },
    "problemMatcher": []
}
```

### 方法 3: Git Hook (pre-merge)

創建 `.git/hooks/pre-merge-commit`：

```bash
#!/bin/bash
# Pre-merge hook to check documentation sync

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [ "$CURRENT_BRANCH" = "main" ] || [ "$CURRENT_BRANCH" = "master" ]; then
    echo "Checking documentation sync before merge..."
    ./tools/check_branch_docs_sync.sh MERGE_HEAD main

    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ Documentation sync check failed!"
        echo "   Please restore missing files before merging."
        exit 1
    fi
fi
```

## 退出碼

- `0` - 文檔完全同步
- `1` - 發現差異，需要處理

## 相關文檔

- `devspec/pr_creation_guide.md` - PR 創建指南
- `devspec/pre_commit_checklist.md` - 提交前檢查清單
- `devspec/stage_verification_checklist.md` - 階段驗證清單

## 維護記錄

- **2026-02-12**: 初次創建，用於防止 v2.10.0 和 v2.11.0 分支文檔遺失問題

## 問題排查

### Q: 腳本無法執行（Permission denied）
**A**: 賦予執行權限
```bash
chmod +x tools/check_branch_docs_sync.sh
```

### Q: 顯示 "branch not found"
**A**: 確認分支名稱正確
```bash
git branch -a  # 列出所有分支
```

### Q: 如何批次還原檔案？
**A**: 複製腳本輸出的建議指令並執行
```bash
# 從腳本輸出複製 git show 指令並執行
git show feature/...:devspec/file.md > devspec/file.md
```
