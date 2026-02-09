# Stage Verification Checklist

> **使用時機**: Level 3 實作完成後，提交 PR 之前
> **執行者**: 開發者（手動）或使用 `python tools/stage_verification_gui.pyw`

---

## 驗測階段總覽

- [ ] **Phase 1**: Pre-Build 檢查
- [ ] **Phase 2**: Build 驗證
- [ ] **Phase 3**: 16KB Compatibility
- [ ] **Phase 4**: Emulator Testing
- [ ] **Phase 5**: Device Testing（條件觸發）

---

## Phase 1: Pre-Build 檢查

### 1.1 Git 狀態檢查

```bash
git status
git diff --stat
```

**檢查項目**:
- [ ] Git status 清潔（無未追蹤的關鍵變更）
- [ ] 變更範圍明確（檔案數量: ______）
- [ ] 符合命名規範（變數至少 3 字元）
- [ ] 無硬編碼字串/顏色
- [ ] 註解完整

**Pass 標準**: 所有變更都在預期範圍內

---

## Phase 2: Build 驗證

### 2.1 Clean Build

```bash
./gradlew clean
./gradlew assembleEmulatorDebug
```

**檢查項目**:
- [ ] Build 成功
- [ ] Build 時間: ______ 秒
- [ ] APK 大小: ______ MB
- [ ] 無 Build ERROR

### 2.2 Lint 檢查

```bash
./gradlew lintEmulatorDebug
```

**檢查項目**:
- [ ] Lint 通過
- [ ] 無新增 Critical/Error
- [ ] Warning 數量: ______ 個

**Pass 標準**: 無 ERROR，Warning 未增加

---

## Phase 3: 16KB Compatibility

### 3.1 執行 Google 官方檢測工具

```bash
bash docs/test_results/v2.9.6-Phase2.6/Google-check_elf_alignment.sh \
  app/build/outputs/apk/emulator/debug/app-emulator-debug.apk
```

**檢查項目**:
- [ ] `=== ELF alignment ===` 區塊顯示
- [ ] `ELF Verification Successful` 訊息出現
- [ ] 所有 64-bit 架構 ALIGNED（16KB 或 64KB）

**Pass 標準**:
```
=== ELF alignment ===
ELF Verification Successful
=====================
```

**Fail 處理**:
- 檢查 NDK 版本（建議 r27+ 或 r28+）
- 添加 CMake 參數：`-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON`
- 參考腳本輸出的修復指南

---

## Phase 4: Emulator Testing

### 4.1 啟動模擬器

**方法 1: 使用 GUI**
```bash
python tools/stage_verification_gui.pyw
# Tab 4 → 選擇模擬器 → 點擊 "🚀 啟動模擬器"
```

**方法 2: 命令列（Windows PowerShell）**
```powershell
# 列出可用模擬器
emulator -list-avds

# 啟動模擬器
Start-Process emulator -ArgumentList "-avd", "Medium_Phone_API_36.1", "-gpu", "host" -NoNewWindow
```

**驗證模擬器已啟動**:
```bash
adb devices
# 預期輸出: emulator-5554    device
```

### 4.2 安裝與 Logcat 監控

```bash
# 安裝 APK
adb -s emulator-5554 install -r app/build/outputs/apk/emulator/debug/app-emulator-debug.apk

# 啟動 Logcat
adb -s emulator-5554 logcat -c
adb -s emulator-5554 logcat -v time > docs/test_results/<version>/emulator_test_logcat.log &
```

### 4.3 功能驗證

- [ ] App 啟動成功（無 crash）
- [ ] 主要功能可用（列出測試場景）:
  - [ ] 場景 1: __________
  - [ ] 場景 2: __________
  - [ ] 場景 3: __________
- [ ] UI 顯示正常
- [ ] 無 Logcat ERROR

### 4.4 穩定性測試

- [ ] 模式切換: ______ 次，Crash: ______ 次
- [ ] 旋轉螢幕: ______ 次，異常: ______ 次
- [ ] 長時間運行（5-10 分鐘）
  - Memory: 開始 ______ MB → 結束 ______ MB
  - Memory 增長 < 20%

**Pass 標準**: Crash 率 = 0%，Memory 無明顯 Leak

---

## Phase 5: Device Testing（條件觸發）

> ⚠️ **觸發條件**: Phase 1-4 全部通過

### 5.1 Build Device APK

```bash
./gradlew assembleDeviceDebug
```

### 5.2 16KB Check（Device APK）

```bash
bash docs/test_results/v2.9.6-Phase2.6/Google-check_elf_alignment.sh \
  app/build/outputs/apk/device/debug/app-device-debug.apk
```

### 5.3 Test Handoff（推送到 Work NB）

**使用 Test Handoff GUI**:
```bash
python tools/test_handoff_gui.pyw
```

1. 切換到 "1. 📤 Home NB (Push)" 分頁
2. 填寫任務資訊（Topic, Device, Objective）
3. 點擊 "🚀 執行推送 (Commit & Push)"

### 5.4 Work NB 實機測試

**Work NB 執行**:
```bash
# 拉取更新
git pull origin <branch-name>

# 安裝到實機
adb -s <device-id> install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# Logcat 收集
adb -s <device-id> logcat -c
adb -s <device-id> logcat -v time > docs/test_results/<version>/device_test_logcat.log
```

**測試項目**:
- [ ] 裝置型號: __________
- [ ] Android 版本: __________
- [ ] App 啟動成功
- [ ] 測試場景執行結果:
  - [ ] 場景 1: __________ ✅/❌
  - [ ] 場景 2: __________ ✅/❌

**Pass 標準**: 所有測試場景通過，無實機特有 Bug

---

## 驗測結果總結

### 通過標準（All Must Pass）

- [ ] Phase 1: Pre-Build ✅
- [ ] Phase 2: Build 驗證 ✅
- [ ] Phase 3: 16KB Compatibility ✅
- [ ] Phase 4: Emulator Testing ✅
- [ ] Phase 5: Device Testing ✅ / ⏭️ SKIPPED

### 驗測報告

- **Version**: __________
- **Date**: __________
- **Status**: ✅ PASS / ❌ FAIL / ⚠️ PARTIAL

**如果 PASS**:
- 生成驗測報告: `docs/test_results/<version>/VERIFICATION_REPORT.md`
- 更新 `devspec/feature_record_Backlog.md`（如有新功能）
- 準備 PR: 使用 `devspec/pull_request_template.md`

**如果 FAIL**:
- 記錄失敗原因: `docs/test_results/<version>/VERIFICATION_FAILED.md`
- 修復問題後重新驗測

---

## 快速指令參考

### 模擬器相關

```bash
# 列出可用模擬器
emulator -list-avds

# 啟動模擬器（PowerShell）
Start-Process emulator -ArgumentList "-avd", "<avd-name>", "-gpu", "host" -NoNewWindow

# 檢查裝置
adb devices

# 關閉模擬器
adb -s emulator-5554 emu kill
```

### Build 相關

```bash
# Clean build
./gradlew clean

# Emulator build
./gradlew assembleEmulatorDebug

# Device build
./gradlew assembleDeviceDebug

# Lint check
./gradlew lintEmulatorDebug
```

### 16KB 檢測

```bash
# Google 官方工具
bash docs/test_results/v2.9.6-Phase2.6/Google-check_elf_alignment.sh <apk-path>
```

### ADB 相關

```bash
# 安裝 APK
adb -s <device-id> install -r <apk-path>

# Logcat
adb -s <device-id> logcat -v time > <output-file>

# 截圖（正確方法）
adb -s <device-id> shell "screencap -p > /sdcard/Download/temp.png"
adb -s <device-id> pull //sdcard//Download//temp.png <local-path>
adb -s <device-id> shell "rm /sdcard/Download/temp.png"
```
