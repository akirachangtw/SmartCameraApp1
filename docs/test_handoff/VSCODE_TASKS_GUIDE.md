# VSCode Tasks 測試交接指南

**文檔版本**: 1.0
**建立日期**: 2026-01-16
**適用專案**: SmartCameraApp1 v2.9.6+

---

## 📋 概述

本指南說明如何使用 VSCode Tasks 進行 Home NB ↔ Work NB 之間的測試交接。

### 核心概念

VSCode Tasks 將測試交接流程標準化為 4 個一鍵操作：

```
📤 Home NB (Push)  →  📥 Work NB (Pull)  →  🧪 Execute Tests  →  📤 Work NB (Push)  →  📥 Home NB (Pull)
```

### 優勢

- ✅ **一鍵操作**: 自動執行 git add/commit/push/pull
- ✅ **標準化**: 統一的 commit 訊息格式
- ✅ **清晰指引**: 每步顯示下一步操作提示
- ✅ **可重複**: 適用於所有測試交接場景

---

## 🎯 可用的 Tasks

### 1. 📤 Test Handoff: 1. Home NB (Push)

**用途**: Home NB 完成測試文檔後，推送到 GitHub

**執行時機**: 在 Home NB 上，完成測試文檔撰寫後

**流程**:
1. 檢查 git 狀態
2. 添加 `docs/test_handoff/` 檔案
3. Commit with structured message
4. Push 到 GitHub

**需要輸入**:
- `testHandoffTopic`: 測試任務名稱（例如：v2.9.6_phase1）
- `testHandoffDevice`: 測試裝置（例如：Pixel 9a）
- `testHandoffObjective`: 測試目標簡述

**輸出範例**:
```
📤 Test Handoff: Home NB (Push)

Step 1: Checking git status...
M docs/test_handoff/v2.9.6_phase1_work_nb_execution.md

Step 2: Adding test handoff files...
Added: docs/test_handoff/

Step 3: Committing...
[main abc1234] [Docs] Test handoff: v2.9.6_phase1

Step 4: Pushing to GitHub...
To https://github.com/akirachangtw/SmartCameraApp1.git
   e45f374..abc1234  main -> main

✅ Test handoff pushed successfully!

📋 Next step: Switch to Work NB and run "📥 Test Handoff: 2. Work NB (Pull)"
```

---

### 2. 📥 Test Handoff: 2. Work NB (Pull)

**用途**: Work NB 拉取最新測試文檔並準備執行

**執行時機**: 在 Work NB 上，準備開始測試時

**流程**:
1. Pull 最新代碼
2. 列出可用的測試交接文檔
3. 顯示 Claude Code 執行提示

**無需輸入**

**輸出範例**:
```
📥 Test Handoff: Work NB (Pull)

Step 1: Pulling latest from GitHub...
Updating e45f374..abc1234
Fast-forward
 docs/test_handoff/v2.9.6_phase1_work_nb_execution.md | 50 ++++++
 1 file changed, 50 insertions(+)

Step 2: Listing available test handoff docs...
Name
----
quick_commands_reference.md
test_report_template.md
v2.9.6_phase1_work_nb_execution.md

✅ Test handoff pulled successfully!

📋 Next steps:
  1. Open Claude Code Chat
  2. Run: 引用 @docs/test_handoff/v2.9.6_phase1_work_nb_execution.md
  3. Ask Claude to execute tests and generate report
  4. After testing, run "📤 Test Handoff: 3. Work NB (Push)"
```

---

### 3. 📤 Test Handoff: 3. Work NB (Push)

**用途**: Work NB 完成測試後，提交測試報告

**執行時機**: 在 Work NB 上，完成測試並生成報告後

**流程**:
1. 檢查 git 狀態
2. 添加 `docs/test_results/` 檔案
3. 提示輸入測試結果（PASS/FAIL）
4. Commit with test result
5. Push 到 GitHub

**需要輸入**:
- `testHandoffTopic`: 測試任務名稱（自動使用之前輸入的值）
- `testHandoffDevice`: 測試裝置（自動使用之前輸入的值）
- Runtime: 測試結果（PASS 或 FAIL）

**輸出範例**:
```
📤 Test Handoff: Work NB (Push)

Step 1: Checking git status...
?? docs/test_results/v2.9.6_phase1_pixel9a_20260116_173045.md

Step 2: Adding test results...
Added: docs/test_results/

Step 3: Committing test results...
Enter test result (PASS/FAIL): PASS
[main def5678] [Test] v2.9.6_phase1: Test results - PASS

Step 4: Pushing to GitHub...
To https://github.com/akirachangtw/SmartCameraApp1.git
   abc1234..def5678  main -> main

✅ Test results pushed successfully!

📋 Next step: Switch to Home NB and run "📥 Test Handoff: 4. Home NB (Pull)"
```

---

### 4. 📥 Test Handoff: 4. Home NB (Pull)

**用途**: Home NB 拉取測試結果並檢視報告

**執行時機**: 在 Home NB 上，Work NB 完成測試後

**流程**:
1. Pull 最新代碼
2. 列出最近 5 個測試報告
3. 自動打開最新的測試報告

**無需輸入**

**輸出範例**:
```
📥 Test Handoff: Home NB (Pull)

Step 1: Pulling latest from GitHub...
Updating abc1234..def5678
Fast-forward
 docs/test_results/v2.9.6_phase1_pixel9a_20260116_173045.md | 200 ++++++
 1 file changed, 200 insertions(+)

Step 2: Listing test results...
Recent test reports:
  - v2.9.6_phase1_pixel9a_20260116_173045.md (2026-01-16 17:30:45)
  - v2.9.0_beta5_emulator_test_20260115_142010.md (2026-01-15 14:20:10)

Opening latest report: v2.9.6_phase1_pixel9a_20260116_173045.md

✅ Test handoff cycle completed!
```

---

### 5. 🔄 Test Handoff: Full Cycle (Home)

**用途**: 展示完整測試交接循環（僅供參考）

**執行時機**: 不建議實際使用（需要手動切換到 Work NB）

**流程**:
- 依序執行 "📤 Home NB (Push)" 和 "📥 Home NB (Pull)"

**注意**: 這個 Task 無法真正完成完整循環，因為中間需要在 Work NB 執行測試。

---

## 🚀 使用流程

### 完整測試交接流程

#### Step 1: Home NB - 準備測試文檔

1. **撰寫測試文檔**:
   - 編輯 `docs/test_handoff/v2.9.6_phase1_work_nb_execution.md`
   - 更新測試步驟、預期結果等

2. **執行 Push Task**:
   - 按 `Ctrl+Shift+P`
   - 輸入 "Run Task"
   - 選擇 "📤 Test Handoff: 1. Home NB (Push)"
   - 輸入參數:
     - Topic: `v2.9.6_phase1`
     - Device: `Pixel 9a`
     - Objective: `Verify OrientationEventListener fix`

3. **確認推送成功**: 看到 "✅ Test handoff pushed successfully!"

---

#### Step 2: Work NB - 拉取並執行測試

1. **執行 Pull Task**:
   - 切換到 Work NB
   - 按 `Ctrl+Shift+P` → "Run Task"
   - 選擇 "📥 Test Handoff: 2. Work NB (Pull)"

2. **在 Claude Code 中執行測試**:
   - 打開 Claude Code Chat
   - 輸入:
     ```
     引用 @docs/test_handoff/v2.9.6_phase1_work_nb_execution.md

     請協助我執行 v2.9.6 Phase 1 測試並生成報告。
     ```
   - Claude Code 會讀取文檔並協助執行測試

3. **生成測試報告**:
   - Claude Code 會協助填寫報告模板
   - 測試報告保存至 `docs/test_results/`

4. **執行 Push Task**:
   - 按 `Ctrl+Shift+P` → "Run Task"
   - 選擇 "📤 Test Handoff: 3. Work NB (Push)"
   - 輸入測試結果: `PASS` 或 `FAIL`

---

#### Step 3: Home NB - 檢視測試結果

1. **執行 Pull Task**:
   - 切換回 Home NB
   - 按 `Ctrl+Shift+P` → "Run Task"
   - 選擇 "📥 Test Handoff: 4. Home NB (Pull)"

2. **檢視測試報告**:
   - Task 會自動打開最新的測試報告
   - 檢視測試結果（PASS/FAIL）
   - 閱讀關鍵發現與建議

3. **決定下一步行動**:
   - 如果 PASS: 進入下一階段測試或 merge
   - 如果 FAIL: 分析失敗原因，修復並重新測試

---

## ⚙️ 配置與自訂

### 修改預設值

編輯 `.vscode/tasks.json` 中的 inputs 部分：

```json
{
  "id": "testHandoffTopic",
  "type": "promptString",
  "description": "輸入測試任務名稱（例如：v2.9.6_phase1）",
  "default": "your_default_topic"  // 修改這裡
}
```

### 修改 Git Remote

如果您的 remote 名稱不是 `github`，需要修改 Tasks：

**Home NB (Push)** - Line 391:
```powershell
git push your_remote main  // 將 github 改為您的 remote 名稱
```

**Work NB (Pull)** - Line 407:
```powershell
git pull your_remote main  // 將 origin 改為您的 remote 名稱
```

**Work NB (Push)** - Line 423:
```powershell
git push your_remote main  // 將 origin 改為您的 remote 名稱
```

**Home NB (Pull)** - Line 439:
```powershell
git pull your_remote main  // 將 origin 改為您的 remote 名稱
```

---

## 🔧 故障排除

### Issue 1: Task 執行失敗 - "git: command not found"

**原因**: Git 未安裝或未加入 PATH

**解決方案**:
```bash
# 檢查 git 是否安裝
git --version

# 如未安裝，請下載安裝 Git
# https://git-scm.com/downloads

# 確保 Git 在 PATH 中
# 重啟 VSCode
```

---

### Issue 2: Task 執行失敗 - "permission denied"

**原因**: 無權限執行 PowerShell 腳本

**解決方案**:
```powershell
# 以系統管理員身分執行 PowerShell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned

# 確認執行政策
Get-ExecutionPolicy -List
```

---

### Issue 3: Commit 失敗 - "nothing to commit"

**原因**: 沒有檔案變更

**解決方案**:
- 確認測試文檔已修改並保存
- 檢查 `git status` 是否有變更
- 如果是新檔案，確保檔案已建立

---

### Issue 4: Push 失敗 - "authentication failed"

**原因**: Git 認證失敗

**解決方案**:
```bash
# 使用 GitHub CLI 登入
gh auth login

# 或設置 Personal Access Token
git config --global credential.helper store
```

---

### Issue 5: Work NB 無法使用 Tasks

**原因**: `.vscode/tasks.json` 被 .gitignore 忽略

**解決方案**:
```bash
# 方案 A: 手動複製 tasks.json
copy .vscode\tasks.json d:\MyWorking\...\SmartCameraApp1\.vscode\

# 方案 B: 將 tasks.json 加入版本控制（不建議）
# 在 .gitignore 中加入例外:
!.vscode/tasks.json
```

---

### Issue 6: 測試報告未自動打開

**原因**: VSCode `code` 指令未找到

**解決方案**:
```bash
# 確認 code 指令可用
code --version

# 如不可用，在 VSCode 中:
# Ctrl+Shift+P → "Shell Command: Install 'code' command in PATH"
```

---

## 📚 參考資料

### 相關文檔

- [v2.9.6_phase1_work_nb_execution.md](v2.9.6_phase1_work_nb_execution.md) - 測試執行指南
- [test_report_template.md](test_report_template.md) - 測試報告模板
- [quick_commands_reference.md](quick_commands_reference.md) - 快速指令參考

### VSCode Tasks 官方文檔

- [VSCode Tasks Documentation](https://code.visualstudio.com/docs/editor/tasks)
- [Tasks JSON Schema](https://code.visualstudio.com/docs/editor/tasks-appendix)

---

## 💡 最佳實踐

### 1. 保持參數一致性

在整個測試循環中使用相同的 Topic/Device 參數，確保 commit 訊息一致。

### 2. 及時 Commit

完成測試文檔撰寫後立即執行 Push Task，避免累積過多未提交的變更。

### 3. 詳細的測試目標描述

在 `testHandoffObjective` 中提供清晰的測試目標，方便後續追蹤。

### 4. 測試報告命名規範

遵循命名格式：`v{version}_phase{n}_{device}_{timestamp}.md`

### 5. 定期清理舊報告

定期檢視並歸檔舊的測試報告，保持 `docs/test_results/` 整潔。

---

## 🎯 快速檢查清單

### Home NB (Push) 前

- [ ] 測試文檔已撰寫完成
- [ ] 測試步驟清晰明確
- [ ] 預期結果已標註
- [ ] Git 狀態正常（無衝突）

### Work NB (Pull) 後

- [ ] 成功拉取最新代碼
- [ ] 測試文檔可正常讀取
- [ ] Claude Code 可引用文檔
- [ ] 測試裝置已連接

### Work NB (Push) 前

- [ ] 測試已完整執行
- [ ] 測試報告已填寫完整
- [ ] PASS/FAIL 判定清晰
- [ ] 關鍵發現已記錄

### Home NB (Pull) 後

- [ ] 成功拉取測試結果
- [ ] 測試報告可正常開啟
- [ ] 測試結果符合預期
- [ ] 下一步行動已規劃

---

**文檔版本**: 1.0
**最後更新**: 2026-01-16
**維護者**: Claude Code Assistant
**適用範圍**: SmartCameraApp1 所有測試交接場景
