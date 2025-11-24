# 腳本開發規範

## 語言規範
- **腳本內容**：使用英文撰寫（變數名稱、註解、輸出訊息等）
- **Claude Code Chat對話**：使用繁體中文進行溝通

## 版本控制規則

### 版本號格式：`主版本.次版本-v修訂號`

- **主版本號（+1.0）**：修改腳本內容 ≥ 80% 時遞增
- **次版本號（+0.1）**：修改腳本內容 ≥ 30% 時遞增  
- **修訂號（-v1, -v2...）**：調整修改時遞增

### 版本號範例
```
1.0      → 初始版本
1.1      → 修改了30-79%的內容
2.0      → 重大改版，修改超過80%
1.0-v1   → 在1.0基礎上的第一次小調整
1.0-v2   → 在1.0基礎上的第二次小調整
```

## 日期更新
- 每次調整修改時，必須更新腳本中的日期為當天日期
- 日期格式：`YYYY-MM-DD`

## 參數規範

### 必要參數
- **`-v`**：顯示腳本簡易說明及當前版本號（**必須實作**）

### 推薦參數（非必要）
- **`-h` 或 `--help`**：顯示完整參數說明文件
- **`-set`**：互動式設定腳本變數（顯示提示引導用戶輸入）
- **`-info`**：顯示腳本中已設定的變數值

### 參數輸出範例

#### -v 輸出範例
```bash
$ ./script_name.sh -v
Script Name v1.0
Brief description of what this script does
Last Updated: 2025-11-07
```

#### -h 輸出範例
```bash
$ ./script_name.sh -h
Usage: script_name.sh [OPTIONS]

Required Options:
  -v           Show version and brief description

Optional Parameters:
  -h, --help   Show this help message
  -set         Interactive setup for script variables
  -info        Display current variable settings
  -i INPUT     Input file path
  -o OUTPUT    Output file path
```

#### -set 輸出範例
```bash
$ ./script_name.sh -set
=== Script Configuration ===
Enter username [current: admin]: john_doe
Enter max retry count [current: 3]: 5
Enter timeout seconds [current: 30]: 60
Configuration saved successfully!
```

#### -info 輸出範例
```bash
$ ./script_name.sh -info
=== Current Configuration ===
Username:        john_doe
Max Retry:       5
Timeout:         60 seconds
Log Directory:   /var/log/script
Last Modified:   2025-11-07
```

## 命名規範

### 變數命名規則
- 變數名稱**至少3個字元**
- 使用有意義的英文單字
- 建議使用 `snake_case` 命名法
- 避免使用單字母或無意義的縮寫

#### ✅ 良好範例
```bash
user_name="John"
input_file="data.txt"
max_retry_count=3
timeout_seconds=30
log_directory="/var/log"
```

#### ❌ 避免使用
```bash
un="John"           # 太短
x="data.txt"        # 無意義
a=3                 # 太短且無意義
rt=30               # 縮寫不清楚
```

## 腳本檔頭模板

每個腳本開頭必須包含以下資訊：

```bash
#!/bin/bash
#
# Script Name:    script_name.sh
# Description:    Brief description of what this script does
# Version:        1.0
# Date:           2025-11-07
# Author:         Your Name
# Usage:          ./script_name.sh [OPTIONS]
#
# Required Options:
#   -v          Show version and brief description
#
# Optional Parameters (examples):
#   -h, --help  Show help message
#   -set        Setup configuration
#   -info       Show current settings
#   -i INPUT    Input file path
#   -o OUTPUT   Output file path
#
```

## 顯示符號定義

腳本中應使用統一的符號來表示不同狀態的訊息：

```bash
# Display Symbols
OK_SYMBOL="[V]"
WARN_SYMBOL="[!]"
FAIL_SYMBOL="[X]"
INFO_SYMBOL="[i]"
```

### 使用範例
```bash
echo "${OK_SYMBOL} Operation completed successfully"
echo "${WARN_SYMBOL} Warning: Configuration file not found, using defaults"
echo "${FAIL_SYMBOL} Error: Connection failed"
echo "${INFO_SYMBOL} Processing file: data.txt"
```

### 輸出效果
```
[V] Operation completed successfully
[!] Warning: Configuration file not found, using defaults
[X] Error: Connection failed
[i] Processing file: data.txt
```

## 對話記錄管理

### Claude Code Chat 對話記錄
- 所有與 Claude Code 的對話記錄自動儲存到腳本所在目錄的\chat_logs\
- 對話記錄檔案命名格式：`chat_log_YYYYMMDD_HHMMSS.txt`
- 每次新的對話開始時創建新的記錄檔案
- 記錄包含：
  - 對話時間戳
  - 用戶問題
  - Claude 回應
  - 執行的命令和結果

### 對話記錄範例結構
```
=== Claude Code Chat Log ===
Date: 2025-11-07
Time: 14:30:25
Script: script_name.sh

[14:30:25] User: 請幫我新增一個參數來處理 CSV 檔案
[14:30:30] Claude: 好的，我會新增 -csv 參數...
[14:30:35] Executed: vim script_name.sh
[14:30:40] Result: Success

---
```

## 配置檔案管理

### 變數儲存方式
建議使用配置檔案儲存 `-set` 設定的變數：

```bash
# config.conf 範例
USERNAME="john_doe"
MAX_RETRY=5
TIMEOUT=60
LOG_DIR="/var/log/script"
LAST_UPDATE="2025-11-07"
```

### 讀取配置檔案範例
```bash
# Load configuration
CONFIG_FILE="./config.conf"
if [[ -f "$CONFIG_FILE" ]]; then
    source "$CONFIG_FILE"
fi
```

## 錯誤處理

### 必須處理的情況
- 參數錯誤
- 檔案不存在
- 權限不足
- 網路連線問題（如適用）

### 錯誤訊息範例
```bash
echo "Error: Invalid parameter. Use -h for help." >&2
exit 1
```

## Git 版本控制建議

### Commit 訊息格式
```
[版本號] 簡短說明

詳細變更內容：
- 新增功能 A
- 修正錯誤 B
- 優化效能 C
```

### Commit 範例
```
[1.1] Add -set and -info parameters

Changes:
- Implement interactive configuration setup
- Add configuration display function
- Update help documentation
```

---

## CHANGELOG 管理規範

### CHANGELOG.md 格式標準
遵循 [Keep a Changelog](https://keepachangelog.com/) 標準

### 變更分類
- **Added**: 新增功能
- **Changed**: 現有功能的變更
- **Deprecated**: 即將移除的功能
- **Removed**: 已移除的功能
- **Fixed**: Bug修復
- **Security**: 安全性相關修復

### 格式範例
```markdown
# Changelog

## [版本號] - YYYY-MM-DD

### Added
- 新增功能描述

### Fixed
- 修復項目描述

### Changed
- 變更項目描述
```

### 對話記錄命名標準化
1. **開發功能對話**: `chat_[功能簡稱]_YYYYMMDD_HHMMSS.md`
   - 例如: `chat_backup_20251117_143025.md`
   - 功能簡稱: 1-4字母，如 backup, deploy, test

2. **版本更新記錄**: `YY-MM-DD_CHANGELOG_v[版本號].md`
   - 例如: `25-11-17_CHANGELOG_v1.1.md`

3. **一般對話記錄**: `chat_log_YYYYMMDD_HHMMSS.txt`
   - 例如: `chat_log_20251117_143025.txt`

---

## 快速參考

### 建立新腳本檢查清單
- [ ] 加入檔頭模板
- [ ] 實作 `-v` 參數（**必要**）
- [ ] 實作 `-h/--help` 參數（推薦）
- [ ] 實作 `-set` 參數（依需求）
- [ ] 實作 `-info` 參數（依需求）
- [ ] 所有變數名稱 ≥ 3 字元
- [ ] 更新日期為今天
- [ ] 測試所有參數功能
- [ ] 建立或更新配置檔案（如適用）
- [ ] 提交到 Git

### 更新現有腳本檢查清單
- [ ] 評估修改比例（決定版本號變化）
- [ ] 更新版本號
- [ ] 更新日期
- [ ] 更新 `-v` 輸出內容
- [ ] 測試修改的功能
- [ ] 儲存對話記錄
- [ ] 提交變更到 Git
