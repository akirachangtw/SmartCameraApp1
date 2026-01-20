# 🔴 VSCode Tasks 保護指南

## ⚠️ 重要提醒

此目錄的 `tasks.json` 包含 **關鍵的開發工作流程配置**。誤刪除或修改會導致工作流程中斷！

---

## 🔒 受保護的 Tasks（請勿刪除）

### 1. Test Handoff Manager (1 個 Task)

**Task 標籤**:
- 🚀 Test Handoff Manager (Python GUI)

**用途**: Home NB ↔ Work NB 測試交接自動化工具

**刪除影響**:
- ❌ 無法啟動測試交接 GUI
- ❌ Home/Work NB 同步流程中斷
- ❌ 測試報告無法自動生成

**依賴檔案**:
- `tools/test_handoff_gui.pyw`
- `docs/test_handoff/` 目錄

---

### 2. Hybrid Workflow Tasks (5 個 Tasks)

**Task 標籤**:
1. 📋 Hybrid: L1 Plan (inline input)
2. 📋 Hybrid: L1 Plan (input_file)
3. 📋 Hybrid: L2 Feasibility (inline input)
4. 📋 Hybrid: L2 Feasibility (input_file)
5. 📋 Hybrid: PR Notes (inline input)

**用途**: L1/L2/PR 規劃文檔自動生成工具

**刪除影響**:
- ❌ 無法快速生成 L1 規劃文檔
- ❌ 無法執行 L2 可行性評估
- ❌ 無法生成 PR Notes
- ❌ Hybrid Workflow 完全中斷

**依賴檔案**:
- `tools/hybrid_flow.py`
- `tools/HYBRID_FLOW_CLAUDE_USAGE.md`

---

### 3. Hybrid Inputs (4 個 Input 參數)

**Input IDs**:
1. `hybridTopic` - 任務主題（檔名）
2. `hybridInput` - 問題描述
3. `hybridInputFile` - 外部檔案路徑
4. `hybridIssuePath` - L1 規劃檔路徑

**刪除影響**:
- ❌ Hybrid Tasks 無法接收使用者輸入
- ❌ 所有 Hybrid Tasks 將無法執行

---

## 🛡️ 保護措施

### 方案 A: 編輯前備份

修改 `tasks.json` 前，建議先備份：

```bash
# 創建備份
copy .vscode\tasks.json .vscode\tasks.json.backup

# 如需還原
copy .vscode\tasks.json.backup .vscode\tasks.json
```

### 方案 B: Git 版本控制

`tasks.json` 已強制加入 Git（使用 `git add -f`）：

```bash
# 查看歷史版本
git log .vscode/tasks.json

# 還原到特定版本
git checkout <commit-hash> -- .vscode/tasks.json
```

**重要 Commits**:
- `6031898` - 還原完整 Hybrid Tasks 配置（5 個 Tasks + 4 個 Inputs）
- `bb86b49` - SSH 連線與 remote 統一配置

### 方案 C: 從參考檔案還原

完整的 Hybrid Tasks 配置保存在：
```
tools/hybrid_flow-tasks.json
```

如果 `.vscode/tasks.json` 中的 Hybrid Tasks 遺失，可從此檔案還原。

---

## ✅ 安全編輯指南

### 可以安全修改的內容

✅ **Build/Test Tasks** (🚀 Build Emulator Debug, 📱 Install to Emulator 等)
- 修改 gradlew.bat 參數
- 調整 presentation 設定
- 修改 problemMatcher

✅ **Task 順序**
- 可以調整 tasks 陣列中的順序
- 但不要刪除任何 Task

✅ **Input 預設值**
- 可以修改 `default` 欄位的預設值
- 例如：`hybridTopic` 的預設值可改為其他主題

### 絕對不能刪除的內容

❌ **Test Handoff Manager Task**
- Label: "🚀 Test Handoff Manager (Python GUI)"

❌ **所有 Hybrid Tasks** (5 個)
- Label 包含 "Hybrid:" 的所有 Tasks

❌ **所有 Hybrid Inputs** (4 個)
- `hybridTopic`
- `hybridInput`
- `hybridInputFile`
- `hybridIssuePath`

---

## 🔧 驗證工具

### 檢查 Tasks 完整性

執行以下指令驗證關鍵 Tasks 是否存在：

```powershell
# 檢查 Hybrid Tasks 數量（應為 5）
powershell -Command "(Get-Content .vscode\tasks.json -Raw | ConvertFrom-Json).tasks | Where-Object { $_.label -like '*Hybrid*' } | Measure-Object | Select-Object -ExpandProperty Count"

# 檢查 Test Handoff Task（應為 1）
powershell -Command "(Get-Content .vscode\tasks.json -Raw | ConvertFrom-Json).tasks | Where-Object { $_.label -like '*Test Handoff Manager*' } | Measure-Object | Select-Object -ExpandProperty Count"

# 檢查 Inputs 數量（應為 4）
powershell -Command "(Get-Content .vscode\tasks.json -Raw | ConvertFrom-Json).inputs | Where-Object { $_.id -like 'hybrid*' } | Measure-Object | Select-Object -ExpandProperty Count"
```

### JSON 格式驗證

```powershell
powershell -NoProfile -Command "try { Get-Content .vscode\tasks.json -Raw | ConvertFrom-Json | Out-Null; Write-Host '✅ JSON format is valid' -ForegroundColor Green } catch { Write-Host '❌ JSON format is invalid' -ForegroundColor Red; Write-Host $_.Exception.Message -ForegroundColor Yellow }"
```

---

## 📊 當前 Tasks 統計

**總 Tasks 數**: 13
- 7 個 Build/Test Tasks (可修改)
- 5 個 Hybrid Tasks (🔒 受保護)
- 1 個 Test Handoff Task (🔒 受保護)

**總 Inputs 數**: 4
- 4 個 Hybrid Inputs (🔒 受保護)

---

## 🆘 如果不慎刪除

### Step 1: 檢查 Git 狀態

```bash
git status .vscode/tasks.json
```

### Step 2: 還原檔案

```bash
# 方案 A: 從最新 commit 還原
git checkout HEAD -- .vscode/tasks.json

# 方案 B: 從特定 commit 還原（完整 Hybrid Tasks）
git checkout 6031898 -- .vscode/tasks.json

# 方案 C: 從參考檔案手動還原
# 參考 tools/hybrid_flow-tasks.json
```

### Step 3: 驗證還原結果

執行上述「驗證工具」章節的指令，確認：
- Hybrid Tasks: 5 個 ✅
- Test Handoff Task: 1 個 ✅
- Hybrid Inputs: 4 個 ✅
- JSON 格式: 有效 ✅

---

## 📞 聯絡資訊

如果需要協助還原或修復 `tasks.json`，請：

1. 查看 Git Commit: `6031898` ([Config] Restore complete Hybrid Tasks configuration)
2. 參考文檔: `tools/HYBRID_FLOW_CLAUDE_USAGE.md`
3. 參考範本: `tools/hybrid_flow-tasks.json`
4. 查看規範: `.claude/CLAUDE.md`

---

**最後更新**: 2026-01-20
**維護者**: Claude Code Assistant
**保護等級**: 🔴 CRITICAL
