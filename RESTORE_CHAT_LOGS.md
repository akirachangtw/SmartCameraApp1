# Chat Logs 恢復指南

> 若 `chat_logs/` 目錄遺失或損壞，按照此指南快速復原。

---

## 🚀 快速恢復（推薦）

### 1️⃣ 完整恢復所有交接日誌

**場景**：整個 `chat_logs/` 目錄遺失

```bash
cd /path/to/SmartCameraApp1

# 恢復完整目錄
tar -xzf chat_logs_complete_backup_20260408.tar.gz

# 驗證恢復完成
ls -la chat_logs/ | head -20
```

**預期結果**：
- `chat_logs/` 目錄被完全恢復
- 包含所有交接日誌、指南、報告
- archive/ 子目錄與所有壓縮檔案恢復完整

---

## 🔍 選擇性恢復

### 2️⃣ 只恢復特定日期的交接日誌

**場景**：某個特定日期的交接日誌遺失

```bash
cd /path/to/SmartCameraApp1

# 例：恢復 2026-03-13 的最終交接日誌
tar -xzf chat_logs_complete_backup_20260408.tar.gz \
  chat_logs/handofflog_20260313_200000.md

# 檔案被提取至 chat_logs/handofflog_20260313_200000.md
```

### 3️⃣ 恢復特定月份的所有日誌

```bash
# 例：恢復 2026-02 月份的所有交接日誌
tar -xzf chat_logs_complete_backup_20260408.tar.gz \
  $(tar -tzf chat_logs_complete_backup_20260408.tar.gz | grep "handofflog_202602")
```

### 4️⃣ 恢復 archive/ 中的舊壓縮檔

```bash
# 恢復整個 archive 子目錄
tar -xzf chat_logs_complete_backup_20260408.tar.gz chat_logs/archive/

# 或提取特定舊日誌（例：v2.30）
tar -xzf chat_logs_complete_backup_20260408.tar.gz \
  chat_logs/archive/handoff_v230_20251204.md.gz
```

---

## 📋 預覽備份內容

### 5️⃣ 列出備份中所有檔案（不解壓）

```bash
# 查看所有檔案清單
tar -tzf chat_logs_complete_backup_20260408.tar.gz

# 只看交接日誌
tar -tzf chat_logs_complete_backup_20260408.tar.gz | grep "handofflog"

# 只看指南與報告
tar -tzf chat_logs_complete_backup_20260408.tar.gz | grep -E "(GUIDE|REPORT|QUICK)"
```

### 6️⃣ 查看特定檔案內容（無需解壓）

```bash
# 直接檢視最新交接日誌（2026-03-13）
tar -xzOf chat_logs_complete_backup_20260408.tar.gz \
  chat_logs/handofflog_20260313_200000.md | head -50

# 直接檢視歸檔清單
tar -xzOf chat_logs_complete_backup_20260408.tar.gz \
  chat_logs/ARCHIVE_MANIFEST.md
```

---

## 🔐 備份驗證

### 7️⃣ 驗證備份完整性

```bash
# 檢查備份是否損壞
tar -tzf chat_logs_complete_backup_20260408.tar.gz > /dev/null && \
  echo "✅ 備份完整無損" || echo "❌ 備份已損壞"

# 統計備份中的檔案數
tar -tzf chat_logs_complete_backup_20260408.tar.gz | wc -l

# 預期：89 項（含目錄與檔案）
```

### 8️⃣ 測試恢復到臨時位置

```bash
# 建立測試目錄
mkdir -p ./test_restore

# 恢復到測試位置
tar -xzf chat_logs_complete_backup_20260408.tar.gz -C ./test_restore/

# 驗證結構
ls -la ./test_restore/chat_logs/ | wc -l

# 清理測試目錄（完成後）
rm -rf ./test_restore
```

---

## 🛠️ 常見情況速查

| 情況 | 指令 |
|------|------|
| 整個 chat_logs/ 遺失 | `tar -xzf chat_logs_complete_backup_20260408.tar.gz` |
| 單一日誌遺失 | `tar -xzf chat_logs_complete_backup_20260408.tar.gz chat_logs/handofflog_YYYYMMDD_HHMM.md` |
| 預覽所有檔案 | `tar -tzf chat_logs_complete_backup_20260408.tar.gz` |
| 查看檔案大小 | `tar -tzvf chat_logs_complete_backup_20260408.tar.gz` |
| 測試備份完整性 | `tar -tzf chat_logs_complete_backup_20260408.tar.gz > /dev/null && echo OK` |
| 解壓到指定目錄 | `tar -xzf chat_logs_complete_backup_20260408.tar.gz -C /target/path/` |

---

## ⚠️ 注意事項

✅ **安全事項**：
- 備份已驗證無損
- 壓縮率 72%（無資料遺失）
- 可隨時驗證完整性

❌ **勿執行**：
- 不要刪除 `chat_logs_complete_backup_20260408.tar.gz` 檔案
- 不要在已有 chat_logs/ 的情況下直接解壓（會覆蓋）
- 若需合併內容，先解壓到臨時位置再手動合併

---

## 🆘 遇到問題

### 問題：tar: 無法找到檔案
**原因**：備份檔案路徑不正確  
**解決**：確保在專案根目錄執行，且備份檔名正確
```bash
pwd  # 確認在 SmartCameraApp1 根目錄
ls -l chat_logs_complete_backup_20260408.tar.gz  # 確認備份檔存在
```

### 問題：解壓後檔案無法讀取
**原因**：權限問題  
**解決**：
```bash
# 調整權限
chmod -R u+rw chat_logs/

# 驗證
ls -la chat_logs/handofflog_20260313_200000.md
```

### 問題：解壓速度很慢
**原因**：磁碟 I/O 繁忙  
**解決**：耐心等待或在系統空閒時執行

---

**最後更新**: 2026-04-08  
**備份版本**: chat_logs_complete_backup_20260408.tar.gz
