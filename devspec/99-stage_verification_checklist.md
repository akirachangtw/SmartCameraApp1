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
- [ ] **Phase 6**: Merge Readiness（準備合併前）

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

## Phase 4: Emulator Testing（可跳過條件）

> 💡 **可跳過 Phase 4 的情境**：實體裝置已連線（Sony XQ-BT52 / Pixel 9a）時，可直接進行 Phase 5 Device Testing，以節省時間。
> 僅在「無實體裝置可用」或「需要模擬器特定場景（虛擬攝影機、旋轉測試）」時才執行 Phase 4。

### 4.1 啟動模擬器

> ⚠️ **經驗記錄（2026-03-01）**：以下為實際驗證的正確指令，避免重踩錯誤操作。

**方法 1: Bash 背景啟動（推薦）**
```bash
# ✅ 正確：& 背景執行，不佔用 terminal
emulator -avd Pixel_5 -gpu host &

# 可用 AVD 清單
emulator -list-avds
# 目前可用: MPhone_A11, Medium_Phone_API_36.1, Pixel9a-16KB, Pixel9a-virtual, Pixel_5
```

```
# ❌ 不要用以下方式：
# cmd /c "start /min emulator..."   ← 在 bash 環境行為不一致
```

**等待開機完成（輪詢，勿用 sleep 死等）**:
```bash
for i in 1 2 3 4 5 6 7 8 9 10; do
  BOOT=$(adb -s emulator-5554 shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
  echo "  boot_completed=$BOOT attempt $i/10"
  [ "$BOOT" = "1" ] && echo "✅ Boot completed!" && break
  sleep 5
done
```

**驗證模擬器已啟動**:
```bash
adb devices
# 預期輸出: emulator-5554    device
```

**方法 2: 使用 GUI**
```bash
python tools/stage_verification_gui.pyw
# Tab 4 → 選擇模擬器 → 點擊 "🚀 啟動模擬器"
```

### 4.2 安裝、授權與啟動

```bash
# ⚠️ Emulator flavor 的 package 名稱與 Device 不同！
# Device:   com.akirachang.ai.photographer
# Emulator: com.akirachang.ai.photographer.emulator

# 安裝 APK
adb -s emulator-5554 install -r app/build/outputs/apk/emulator/debug/app-emulator-debug.apk

# 一次性授予必要權限（避免每次點選彈窗）
adb -s emulator-5554 shell pm grant com.akirachang.ai.photographer.emulator android.permission.CAMERA
adb -s emulator-5554 shell pm grant com.akirachang.ai.photographer.emulator android.permission.READ_MEDIA_IMAGES 2>/dev/null
adb -s emulator-5554 shell pm grant com.akirachang.ai.photographer.emulator android.permission.READ_EXTERNAL_STORAGE 2>/dev/null

# ✅ 啟動 App（用 monkey，不要用 am start -n）
adb -s emulator-5554 shell "monkey -p com.akirachang.ai.photographer.emulator -c android.intent.category.LAUNCHER 1"
# ❌ 不要用 am start -n → Error type 3: Activity class does not exist

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

> ⚠️ **觸發條件**: Phase 1-3 全部通過（Phase 4 Emulator 可跳過，見上方說明）
> 🚀 **快速路徑**：有實體裝置連線時，完成 Phase 1-3 後直接執行 Phase 5

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

## Phase 6: Merge Readiness（準備合併前）

> ⚠️ **執行時機**: 準備執行 `git merge` 合併分支前
> 📖 **目的**: 確保已完成 MERGE_SOP.md Phase 2 文檔同步，避免合併後文檔與代碼脫節

### 6.1 執行 Smart Check

**方法 1: 直接執行腳本**
```bash
python tools/check_merge_readiness.py
```

**方法 2: 使用 GUI（未來開發）**
```bash
python tools/stage_verification_gui.pyw
# Tab 6 → 點擊 "🔍 檢查合併準備狀態"
```

**預期輸出**（全部通過時）:
```
============================================================
                🔍 Git Merge Readiness Check
============================================================

📋 Step 1: 版本偵測
────────────────────────────────────────────────────────────
ℹ️  偵測到版本: v2.11.1

📋 Step 2: 檢查功能文檔 (docs/features/v2.11.1/)
────────────────────────────────────────────────────────────
✅ 功能文檔存在: 8 個檔案
   - L1 規劃: 2 個
   - L2 可行性: 3 個
   - Traycer 實作: 3 個

📋 Step 3: 檢查測試報告 (docs/test_results/v2.11.1/)
────────────────────────────────────────────────────────────
✅ 測試報告存在: 5 個檔案
   - 交接摘要: HANDOVER_SUMMARY.md
   - 測試報告: PIXEL9A_TEST_REPORT.md

📋 Step 4: 檢查分支發布記錄
────────────────────────────────────────────────────────────
✅ BRANCH_RELEASES.md 包含 v2.11.1 記錄
   - 發現穩定版本標記 ✅

📋 Step 5: 檢查功能決議文檔
────────────────────────────────────────────────────────────
✅ feature_record_Backlog.md 包含 v2.11.1 相關記錄

============================================================
                           📊 檢查結果
============================================================

✅ READY TO MERGE

所有檢查通過，您可以安全執行 git merge
```

### 6.2 檢查項目詳解

腳本會自動檢查以下項目：

**基礎檢查**:
- [ ] 版本號偵測成功（從 `app/build.gradle.kts`）
- [ ] 當前版本: __________

**文檔同步檢查**:
- [ ] `docs/features/v<version>/` 目錄存在
  - [ ] L1 規劃文檔: ______ 個
  - [ ] L2 可行性文檔: ______ 個
  - [ ] Traycer 實作計劃: ______ 個
- [ ] `docs/test_results/v<version>/` 目錄存在
  - [ ] 測試報告: ______ 個
  - [ ] 交接摘要: ______ 個

**規範文檔更新檢查**:
- [ ] `devspec/BRANCH_RELEASES.md` 包含當前版本
- [ ] `devspec/feature_record_Backlog.md` 包含相關記錄

**可選檢查**:
- [ ] `devspec/DOCS_SYNC_LOG_*.md` 存在（MERGE_SOP Phase 2 執行證據）

### 6.3 處理檢查失敗

**如果檢查失敗**（輸出 `⚠️  NOT READY`）:

1. **查看問題列表**:
   ```
   ⚠️  NOT READY - 發現 4 個問題

   問題列表:
     1. 目錄 docs/features/v2.11.1/ 不存在
     2. 目錄 docs/test_results/v2.11.1/ 不存在
     3. feature_record_Backlog.md 未提及 v2.11.1
     4. 建議檢查是否需要更新決議文檔
   ```

2. **執行修正動作**:
   - 參考 `devspec/MERGE_SOP.md` Phase 2 執行文檔同步
   - 確保 `docs/features/v<version>/` 包含功能文檔
   - 確保 `docs/test_results/v<version>/` 包含測試報告
   - 更新 `devspec/BRANCH_RELEASES.md`
   - 更新 `devspec/feature_record_Backlog.md`

3. **重新執行檢查**:
   ```bash
   python tools/check_merge_readiness.py
   ```

**緊急情況**（使用 `--bypass`）:
```bash
# 繞過檢查（僅顯示警告，不阻擋合併）
python tools/check_merge_readiness.py --bypass
```

> ⚠️ **警告**: `--bypass` 模式應僅用於緊急修復或特殊情況，風險自負

### 6.4 後續動作

**如果檢查通過 (✅ READY TO MERGE)**:
- [ ] 執行 `devspec/MERGE_SOP.md` 的完整流程
- [ ] 使用遠端分支或 tag 進行合併（避免使用過時的本地分支）
- [ ] 解決衝突時參考決議文檔（`devspec/feature_record_Backlog.md`）
- [ ] 合併後更新 `devspec/BRANCH_RELEASES.md` 記錄合併資訊

**參考文檔**:
- `devspec/MERGE_SOP.md` - 完整合併流程
- `devspec/DOCS_SYNC_CHECKLIST.md` - 文檔同步檢查清單
- `.claude/CLAUDE.md` - Git Merge 規範

**Pass 標準**: Smart Check 輸出 `✅ READY TO MERGE`

---

## 驗測結果總結

### 通過標準（All Must Pass）

- [ ] Phase 1: Pre-Build ✅
- [ ] Phase 2: Build 驗證 ✅
- [ ] Phase 3: 16KB Compatibility ✅
- [ ] Phase 4: Emulator Testing ✅
- [ ] Phase 5: Device Testing ✅ / ⏭️ SKIPPED
- [ ] Phase 6: Merge Readiness ✅ / ⏭️ SKIPPED (若不需合併)

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

# ✅ 啟動模擬器（Bash 背景）
emulator -avd Pixel_5 -gpu host &

# 等待開機完成
for i in 1 2 3 4 5 6 7 8 9 10; do
  BOOT=$(adb -s emulator-5554 shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
  [ "$BOOT" = "1" ] && echo "✅ Boot completed!" && break
  sleep 5
done

# 檢查裝置
adb devices

# ✅ 啟動 App（Emulator package）
adb -s emulator-5554 shell "monkey -p com.akirachang.ai.photographer.emulator -c android.intent.category.LAUNCHER 1"

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
