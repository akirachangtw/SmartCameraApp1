# VSCode Tasks 參考配置

**目的**: 快速找回 Hybrid Tasks 和 Test Handoff Manager Task 的完整配置
**檔案**: `.vscode/tasks.json`
**最後更新**: 2026-01-20
**Git Commit**: `6031898` - [Config] Restore complete Hybrid Tasks configuration

---

## 📋 目錄

1. [Hybrid Tasks (5 個)](#hybrid-tasks-5-個)
2. [Test Handoff Manager Task (1 個)](#test-handoff-manager-task-1-個)
3. [Hybrid Inputs (4 個)](#hybrid-inputs-4-個)
4. [完整 tasks.json 範例](#完整-tasksjson-範例)
5. [還原方式](#還原方式)

---

## Hybrid Tasks (5 個)

### 1. 📋 Hybrid: L1 Plan (inline input)

**用途**: 快速規劃 - 直接輸入問題描述生成 L1 計畫文檔

**完整配置**:
```json
{
  "label": "📋 Hybrid: L1 Plan (inline input)",
  "type": "shell",
  "command": "python",
  "args": [
    "tools/hybrid_flow.py",
    "--level",
    "L1",
    "--topic",
    "${input:hybridTopic}",
    "--input",
    "${input:hybridInput}",
    "--out",
    "docs/plan",
    "--repo",
    "${workspaceFolder}"
  ],
  "options": {
    "cwd": "${workspaceFolder}"
  },
  "problemMatcher": []
}
```

**輸入參數**:
- `${input:hybridTopic}` - 任務主題（檔名）
- `${input:hybridInput}` - 問題描述

**輸出檔案**: `docs/plan/<topic>_L1.md`

---

### 2. 📋 Hybrid: L1 Plan (input_file)

**用途**: 從檔案規劃 - 從 log 或 notes 檔案生成 L1 計畫文檔

**完整配置**:
```json
{
  "label": "📋 Hybrid: L1 Plan (input_file)",
  "type": "shell",
  "command": "python",
  "args": [
    "tools/hybrid_flow.py",
    "--level",
    "L1",
    "--topic",
    "${input:hybridTopic}",
    "--input_file",
    "${input:hybridInputFile}",
    "--out",
    "docs/plan",
    "--repo",
    "${workspaceFolder}"
  ],
  "options": {
    "cwd": "${workspaceFolder}"
  },
  "problemMatcher": []
}
```

**輸入參數**:
- `${input:hybridTopic}` - 任務主題（檔名）
- `${input:hybridInputFile}` - 外部檔案路徑

**輸出檔案**: `docs/plan/<topic>_L1.md`

---

### 3. 📋 Hybrid: L2 Feasibility (inline input)

**用途**: 可行性評估 - 基於 L1 文檔 + 直接輸入評估技術可行性

**完整配置**:
```json
{
  "label": "📋 Hybrid: L2 Feasibility (inline input)",
  "type": "shell",
  "command": "python",
  "args": [
    "tools/hybrid_flow.py",
    "--level",
    "L2",
    "--topic",
    "${input:hybridTopic}",
    "--issue",
    "${input:hybridIssuePath}",
    "--input",
    "${input:hybridInput}",
    "--out",
    "docs/plan",
    "--repo",
    "${workspaceFolder}"
  ],
  "options": {
    "cwd": "${workspaceFolder}"
  },
  "problemMatcher": []
}
```

**輸入參數**:
- `${input:hybridTopic}` - 任務主題（檔名）
- `${input:hybridIssuePath}` - L1 計畫檔路徑
- `${input:hybridInput}` - 補充描述

**輸出檔案**: `docs/plan/<topic>_L2_FEASIBILITY.md`

---

### 4. 📋 Hybrid: L2 Feasibility (input_file)

**用途**: 可行性評估 - 基於 L1 文檔 + 外部檔案評估技術可行性

**完整配置**:
```json
{
  "label": "📋 Hybrid: L2 Feasibility (input_file)",
  "type": "shell",
  "command": "python",
  "args": [
    "tools/hybrid_flow.py",
    "--level",
    "L2",
    "--topic",
    "${input:hybridTopic}",
    "--issue",
    "${input:hybridIssuePath}",
    "--input_file",
    "${input:hybridInputFile}",
    "--out",
    "docs/plan",
    "--repo",
    "${workspaceFolder}"
  ],
  "options": {
    "cwd": "${workspaceFolder}"
  },
  "problemMatcher": []
}
```

**輸入參數**:
- `${input:hybridTopic}` - 任務主題（檔名）
- `${input:hybridIssuePath}` - L1 計畫檔路徑
- `${input:hybridInputFile}` - 外部檔案路徑

**輸出檔案**: `docs/plan/<topic>_L2_FEASIBILITY.md`

---

### 5. 📋 Hybrid: PR Notes (inline input)

**用途**: PR 文檔 - 記錄實作細節與測試結果

**完整配置**:
```json
{
  "label": "📋 Hybrid: PR Notes (inline input)",
  "type": "shell",
  "command": "python",
  "args": [
    "tools/hybrid_flow.py",
    "--level",
    "PR",
    "--topic",
    "${input:hybridTopic}",
    "--input",
    "${input:hybridInput}",
    "--out",
    "docs/plan",
    "--repo",
    "${workspaceFolder}"
  ],
  "options": {
    "cwd": "${workspaceFolder}"
  },
  "problemMatcher": []
}
```

**輸入參數**:
- `${input:hybridTopic}` - 任務主題（檔名）
- `${input:hybridInput}` - 實作描述

**輸出檔案**: `docs/plan/<topic>_PR_NOTES.md`

---

## Test Handoff Manager Task (1 個)

### 🚀 Test Handoff Manager (Python GUI)

**用途**: 啟動 Home NB ↔ Work NB 測試交接 GUI 工具

**完整配置**:
```json
{
  "label": "🚀 Test Handoff Manager (Python GUI)",
  "type": "shell",
  "command": "python",
  "args": [
    "tools/test_handoff_gui.pyw"
  ],
  "presentation": {
    "reveal": "never",
    "panel": "dedicated"
  },
  "problemMatcher": [],
  "group": {
    "kind": "test",
    "isDefault": true
  }
}
```

**依賴檔案**:
- `tools/test_handoff_gui.pyw` - GUI 主程式
- `docs/test_handoff/` - 測試交接文檔目錄

**功能**:
- Home NB (Push) - 推送測試文檔到 GitHub
- Work NB (Pull) - 拉取測試文檔
- Work NB (Push) - 推送測試結果
- Home NB (Pull) - 拉取測試結果

---

## Hybrid Inputs (4 個)

### 1. hybridTopic

**用途**: 任務主題（用於生成檔名）

**完整配置**:
```json
{
  "id": "hybridTopic",
  "type": "promptString",
  "description": "輸入本次任務 topic（用於檔名）",
  "default": "camera_control_not_available"
}
```

---

### 2. hybridInput

**用途**: 問題/需求描述

**完整配置**:
```json
{
  "id": "hybridInput",
  "type": "promptString",
  "description": "輸入需求/問題描述",
  "default": "實體手機切回 Live 模式後呼叫 setZoomRatio 出現 CameraControl not available"
}
```

---

### 3. hybridInputFile

**用途**: 外部檔案路徑（logs/notes）

**完整配置**:
```json
{
  "id": "hybridInputFile",
  "type": "promptString",
  "description": "輸入 input_file 路徑（例如 logs/logcat.txt 或 notes/issue.md）",
  "default": "logs/logcat.txt"
}
```

---

### 4. hybridIssuePath

**用途**: L1 計畫檔路徑（用於 L2 評估）

**完整配置**:
```json
{
  "id": "hybridIssuePath",
  "type": "promptString",
  "description": "輸入 Issue/計畫檔路徑（L2 建議填 L1 產出，如 docs/plan/<topic>_L1.md）",
  "default": "docs/plan/camera_control_not_available_L1.md"
}
```

---

## 完整 tasks.json 範例

將以下內容整合到 `.vscode/tasks.json` 中：

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "🚀 Test Handoff Manager (Python GUI)",
      "type": "shell",
      "command": "python",
      "args": [
        "tools/test_handoff_gui.pyw"
      ],
      "presentation": {
        "reveal": "never",
        "panel": "dedicated"
      },
      "problemMatcher": [],
      "group": {
        "kind": "test",
        "isDefault": true
      }
    },
    {
      "label": "📋 Hybrid: L1 Plan (inline input)",
      "type": "shell",
      "command": "python",
      "args": [
        "tools/hybrid_flow.py",
        "--level",
        "L1",
        "--topic",
        "${input:hybridTopic}",
        "--input",
        "${input:hybridInput}",
        "--out",
        "docs/plan",
        "--repo",
        "${workspaceFolder}"
      ],
      "options": {
        "cwd": "${workspaceFolder}"
      },
      "problemMatcher": []
    },
    {
      "label": "📋 Hybrid: L1 Plan (input_file)",
      "type": "shell",
      "command": "python",
      "args": [
        "tools/hybrid_flow.py",
        "--level",
        "L1",
        "--topic",
        "${input:hybridTopic}",
        "--input_file",
        "${input:hybridInputFile}",
        "--out",
        "docs/plan",
        "--repo",
        "${workspaceFolder}"
      ],
      "options": {
        "cwd": "${workspaceFolder}"
      },
      "problemMatcher": []
    },
    {
      "label": "📋 Hybrid: L2 Feasibility (inline input)",
      "type": "shell",
      "command": "python",
      "args": [
        "tools/hybrid_flow.py",
        "--level",
        "L2",
        "--topic",
        "${input:hybridTopic}",
        "--issue",
        "${input:hybridIssuePath}",
        "--input",
        "${input:hybridInput}",
        "--out",
        "docs/plan",
        "--repo",
        "${workspaceFolder}"
      ],
      "options": {
        "cwd": "${workspaceFolder}"
      },
      "problemMatcher": []
    },
    {
      "label": "📋 Hybrid: L2 Feasibility (input_file)",
      "type": "shell",
      "command": "python",
      "args": [
        "tools/hybrid_flow.py",
        "--level",
        "L2",
        "--topic",
        "${input:hybridTopic}",
        "--issue",
        "${input:hybridIssuePath}",
        "--input_file",
        "${input:hybridInputFile}",
        "--out",
        "docs/plan",
        "--repo",
        "${workspaceFolder}"
      ],
      "options": {
        "cwd": "${workspaceFolder}"
      },
      "problemMatcher": []
    },
    {
      "label": "📋 Hybrid: PR Notes (inline input)",
      "type": "shell",
      "command": "python",
      "args": [
        "tools/hybrid_flow.py",
        "--level",
        "PR",
        "--topic",
        "${input:hybridTopic}",
        "--input",
        "${input:hybridInput}",
        "--out",
        "docs/plan",
        "--repo",
        "${workspaceFolder}"
      ],
      "options": {
        "cwd": "${workspaceFolder}"
      },
      "problemMatcher": []
    }
  ],
  "inputs": [
    {
      "id": "hybridTopic",
      "type": "promptString",
      "description": "輸入本次任務 topic（用於檔名）",
      "default": "camera_control_not_available"
    },
    {
      "id": "hybridInput",
      "type": "promptString",
      "description": "輸入需求/問題描述",
      "default": "實體手機切回 Live 模式後呼叫 setZoomRatio 出現 CameraControl not available"
    },
    {
      "id": "hybridInputFile",
      "type": "promptString",
      "description": "輸入 input_file 路徑（例如 logs/logcat.txt 或 notes/issue.md）",
      "default": "logs/logcat.txt"
    },
    {
      "id": "hybridIssuePath",
      "type": "promptString",
      "description": "輸入 Issue/計畫檔路徑（L2 建議填 L1 產出，如 docs/plan/<topic>_L1.md）",
      "default": "docs/plan/camera_control_not_available_L1.md"
    }
  ]
}
```

---

## 還原方式

### 方案 1: 從 Git 還原

```bash
# 還原到最新的完整配置（推薦）
git checkout 6031898 -- .vscode/tasks.json

# 或從 HEAD 還原
git checkout HEAD -- .vscode/tasks.json
```

### 方案 2: 從參考檔案還原

```bash
# 參考完整配置
cat tools/hybrid_flow-tasks.json

# 手動複製 Tasks 和 Inputs 到 .vscode/tasks.json
```

### 方案 3: 從本文檔還原

將上述「完整 tasks.json 範例」中的內容複製貼上到 `.vscode/tasks.json`。

---

## 驗證配置完整性

### 快速驗證指令

```bash
# 檢查 Hybrid Tasks 數量（應為 5）
powershell -Command "(Get-Content .vscode\tasks.json -Raw | ConvertFrom-Json).tasks | Where-Object { $_.label -like '*Hybrid*' } | Measure-Object | Select-Object -ExpandProperty Count"

# 檢查 Test Handoff Task（應為 1）
powershell -Command "(Get-Content .vscode\tasks.json -Raw | ConvertFrom-Json).tasks | Where-Object { $_.label -like '*Test Handoff Manager*' } | Measure-Object | Select-Object -ExpandProperty Count"

# 檢查 Hybrid Inputs（應為 4）
powershell -Command "(Get-Content .vscode\tasks.json -Raw | ConvertFrom-Json).inputs | Where-Object { $_.id -like 'hybrid*' } | Measure-Object | Select-Object -ExpandProperty Count"

# 驗證 JSON 格式
powershell -NoProfile -Command "try { Get-Content .vscode\tasks.json -Raw | ConvertFrom-Json | Out-Null; Write-Host '✅ JSON format is valid' -ForegroundColor Green } catch { Write-Host '❌ JSON format is invalid' -ForegroundColor Red; Write-Host $_.Exception.Message }"
```

### 預期結果

- Hybrid Tasks: **5** ✅
- Test Handoff Task: **1** ✅
- Hybrid Inputs: **4** ✅
- JSON 格式: **有效** ✅

---

## 相關文件

- **保護指南**: `.vscode/TASKS_PROTECTION.md`
- **專案規範**: `.claude/CLAUDE.md`
- **Hybrid Flow 文檔**: `tools/HYBRID_FLOW_CLAUDE_USAGE.md`
- **參考配置**: `tools/hybrid_flow-tasks.json`
- **Test Handoff 文檔**: `docs/test_handoff/`

---

**維護者**: Claude Code Assistant
**用途**: 快速參考與還原 VSCode Tasks 配置
**保護等級**: 🔴 CRITICAL
