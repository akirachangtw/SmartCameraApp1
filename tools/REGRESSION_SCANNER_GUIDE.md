# Regression Scanner 使用指南

## 概述

Regression Scanner 是 SmartCameraApp1 的自動化回歸檢測工具，用於掃描代碼庫中已知的反模式（anti-patterns）和回歸問題，防止重新引入已修復的 Bug。

**版本**: 1.0.0
**創建日期**: 2026-02-10
**作者**: Claude Sonnet 4.5

---

## 功能特性

### 🔍 自動檢測已知問題

Regression Scanner 能夠檢測以下已知的反模式：

| ID | 名稱 | 嚴重性 | 修復版本 | 參考 |
|---|------|--------|---------|------|
| **AP001** | Snackbar "照片已儲存" | Critical | v2.9.8 | `feature_record_Backlog.md` |
| **AP002** | Photo Info Button | Critical | v2.10.2 | `feature_record_Backlog.md` |
| **AP003** | AspectRatioMode `.first()` | Critical | v2.10.2 | `feature_record_Backlog.md` |
| **AP004** | PhotoInfoBottomSheet Auto-Display | Warning | v2.10.2 | `feature_record_Backlog.md` |
| **AP005** | Gradual Permission Request | Warning | v2.10.2 | `feature_record_Backlog.md` |
| **AP006** | Optical Flow Fade Guard | Critical | v2.9.9 Phase 9B-fix | `MEMORY.md` |

### 📊 多種輸出格式

- **Text**: 終端友好的文字報告
- **Markdown**: 適合文檔的 Markdown 格式
- **JSON**: 機器可讀格式，適合 CI/CD 整合

### 🎯 高精度檢測

- 自動排除註解行（避免誤報）
- 檢測代碼部分，忽略註解中的關鍵字
- 支援 Kotlin/Java 語法

---

## 使用方法

### 方法 1：命令行執行

#### 基本掃描（Text 輸出）
```bash
python tools/regression_scanner.py
```

#### 詳細模式（顯示掃描進度）
```bash
python tools/regression_scanner.py --verbose
```

#### Markdown 報告輸出
```bash
python tools/regression_scanner.py --format markdown --output tmp/regression_report.md
```

#### JSON 報告輸出（適合 CI/CD）
```bash
python tools/regression_scanner.py --format json --output tmp/regression_report.json
```

#### CI 模式（如有 Critical Issues 則 exit 1）
```bash
python tools/regression_scanner.py --ci
```

### 方法 2：VSCode Tasks

按 `Ctrl+Shift+P`，輸入 "Run Task"，選擇以下任務之一：

1. **🛡️ Regression Scanner (Text Report)** - 終端輸出文字報告
2. **🛡️ Regression Scanner (Markdown Report)** - 生成 Markdown 報告至 `tmp/regression_report.md`
3. **🛡️ Regression Scanner (JSON Report)** - 生成 JSON 報告至 `tmp/regression_report.json`

---

## 輸出範例

### Text Report 範例

```
================================================================================
REGRESSION SCANNER REPORT
Scan Date: 2026-02-10 10:17:43
Root Path: D:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1
================================================================================

🔴 Critical Issues: 1
⚠️  Warnings: 0
ℹ️  Info: 0

--------------------------------------------------------------------------------
🔴 CRITICAL ISSUES
--------------------------------------------------------------------------------

[AP001] app\src\main\java\com\akirachang\ai\photographer\presentation\camera\CameraScreen.kt:151
    Snackbar Photo Saved Message: Snackbar with "照片已儲存" or "Photo saved" message
    Fixed in: v2.9.8
    Reference: devspec/feature_record_Backlog.md - Photo Capture Feedback
    Code: snackbarHostState.showSnackbar("照片已儲存")

================================================================================
Total Issues: 1
================================================================================
```

### Markdown Report 範例

```markdown
# Regression Scanner Report

**Scan Date**: 2026-02-10 10:17:43
**Root Path**: `D:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1`

## Summary

- 🔴 **Critical Issues**: 1
- ⚠️ **Warnings**: 0
- ℹ️ **Info**: 0

## 🔴 Critical Issues

### [AP001] app\src\main\java\com\akirachang\ai\photographer\presentation\camera\CameraScreen.kt:151

**Message**: Snackbar Photo Saved Message: Snackbar with "照片已儲存" or "Photo saved" message

```kotlin
snackbarHostState.showSnackbar("照片已儲存")
```

---
**Total Issues**: 1
```

---

## 整合到開發工作流

### Pre-Commit Hook 整合

Regression Scanner 已經整合到 Git pre-commit hook 中：

- **自動觸發**: 修改高風險檔案時自動掃描（CameraScreen.kt, CameraViewModel.kt, MainActivity.kt, LiveCameraSource.kt）
- **阻止提交**: 如檢測到 Critical Issues，將阻止 commit
- **警告提示**: 顯示檢測到的問題和修復建議

### CI/CD 整合

在 GitHub Actions 或其他 CI/CD 系統中使用：

```yaml
# .github/workflows/regression-check.yml
name: Regression Check

on: [push, pull_request]

jobs:
  regression-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Python
        uses: actions/setup-python@v2
        with:
          python-version: '3.x'
      - name: Run Regression Scanner
        run: python tools/regression_scanner.py --ci
```

---

## 添加新的 Anti-Pattern

如果發現新的回歸問題，可以在 `tools/regression_scanner.py` 中添加新的 `AntiPattern`：

```python
AntiPattern(
    id="AP007",  # 新的 ID
    name="Your Anti-Pattern Name",
    description="Brief description of the issue",
    file_pattern=r"YourFile\.kt$",  # Regex for file name
    code_pattern=r'your.*regex.*pattern',  # Regex for code content
    severity="critical",  # critical, warning, or info
    reference="Path to documentation",
    fixed_in="vX.X.X"  # Version where it was fixed
)
```

**重要**：添加新的 anti-pattern 後，記得：
1. 更新 `devspec/feature_record_Backlog.md` 的 Critical Warnings
2. 測試 Scanner 是否正常工作
3. 提交變更並通知團隊

---

## 命令行參數詳細說明

```
python tools/regression_scanner.py [options]

Options:
  --path PATH         指定掃描路徑（預設: 當前目錄）
  --format FORMAT     輸出格式: text, json, markdown（預設: text）
  --output FILE       輸出檔案（預設: 標準輸出）
  --verbose           顯示詳細資訊（掃描進度）
  --ci                CI 模式（如有 Critical Issues 則 exit 1）
  -h, --help          顯示幫助訊息
```

### 範例

```bash
# 掃描特定目錄
python tools/regression_scanner.py --path app/src/main

# 詳細模式 + Markdown 輸出
python tools/regression_scanner.py --verbose --format markdown --output report.md

# CI 模式（GitHub Actions）
python tools/regression_scanner.py --ci
```

---

## 常見問題 (FAQ)

### Q1: Scanner 為什麼報告註解行有問題？

**A**: 這是 v1.0.0 之前的已知問題。v1.0.0 已修復，現在會自動排除註解行。如果仍有誤報，請提交 Issue。

### Q2: 如何更新 Anti-Pattern 規則？

**A**: 編輯 `tools/regression_scanner.py` 中的 `ANTI_PATTERNS` 列表，添加或修改 `AntiPattern` 物件。

### Q3: Scanner 可以整合到 VSCode 嗎？

**A**: 是的！已經整合到 `.vscode/tasks.json`。使用 `Ctrl+Shift+P` → "Run Task" → 選擇 Regression Scanner 任務。

### Q4: Scanner 會掃描哪些檔案？

**A**: Scanner 只掃描 `app/src/` 目錄下的 `.kt` 檔案（Kotlin 原始碼）。

### Q5: 如何在 CI/CD 中使用 Scanner？

**A**: 使用 `--ci` 參數。如有 Critical Issues，Scanner 會以 exit code 1 退出，CI/CD 會標記為失敗。

---

## 技術細節

### 掃描邏輯

1. **檔案過濾**: 只掃描符合 `file_pattern` 的檔案
2. **註解排除**: 自動排除 `//` 開頭的行和註解部分
3. **Regex 匹配**: 使用 `code_pattern` 進行正則表達式匹配
4. **結果聚合**: 按嚴重性（Critical → Warning → Info）排序

### 效能考量

- **快速掃描**: 只掃描高風險檔案（約 10 個檔案）
- **增量掃描**: 只檢查符合 anti-pattern file pattern 的檔案
- **記憶體效率**: 逐行處理，不載入完整檔案內容

---

## 相關文檔

- **Feature Record (SSOT)**: `devspec/feature_record_Backlog.md`
- **Pre-Commit Checklist**: `devspec/pre_commit_checklist.md`
- **Auto Memory**: `C:\Users\Administrator\.claude\projects\.../memory/MEMORY.md`
- **Git Hooks**: `.git/hooks/pre-commit`

---

## 版本歷史

### v1.0.0 (2026-02-10)
- ✅ 初始版本
- ✅ 支援 6 種 Anti-Patterns 檢測
- ✅ Text/JSON/Markdown 輸出格式
- ✅ CI 模式支援
- ✅ 註解行自動排除
- ✅ VSCode Tasks 整合

---

## 貢獻與反饋

如發現新的回歸問題或改進建議，請：
1. 在 `devspec/feature_record_Backlog.md` 中添加 Critical Warning
2. 在 `tools/regression_scanner.py` 中添加對應的 Anti-Pattern
3. 提交 Pull Request 並通知團隊

**聯絡資訊**: 參考專案 README.md

---

**最後更新**: 2026-02-10
**維護者**: SmartCameraApp1 開發團隊
