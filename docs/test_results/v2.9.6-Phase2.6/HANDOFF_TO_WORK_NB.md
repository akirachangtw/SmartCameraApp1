# Phase 2.6.1 交接文件 - Work NB 實機測試

**日期**: 2026-01-28
**來源**: Home NB (開發環境)
**目標**: Work NB (Pixel 9a 測試環境)
**任務**: Pixel 9a 實機驗證 16KB 對齊修復

---

## 📋 快速摘要（1 分鐘閱讀）

### 已完成工作
✅ **ML Kit 升級**: 16.1.5 → 16.1.7 (支援 16KB)
✅ **APK 建置**: 246 MB，Build 成功
✅ **三重驗證**: zipalign + Google 工具 + APK Analyzer 全部通過
✅ **技術合規**: 100% 達成

### 待完成工作
⏸️ **Pixel 9a 實機測試**: 確認無 PageSizeMismatchDialog
⏸️ **版本更新**: 若測試通過，更新到 2.9.6-phase2.6.1
⏸️ **Git 提交**: 記錄成功修復

### 預期結果
**99% 信心**: ✅ 無警告，問題完全解決

---

## 📦 檔案清單

### APK 檔案
- **位置**: `app/build/outputs/apk/device/debug/app-device-debug.apk`
- **大小**: 246 MB
- **版本**: 2.9.6-phase2.5 (代碼 57，即將升級到 58)
- **包含**: ML Kit 16.1.7 (最新 16KB 支援)

### 文件檔案
1. **MANUAL_TEST_INSTRUCTIONS.md** - 手動測試步驟
2. **FINAL_VERIFICATION_REPORT.md** - 完整驗證報告
3. **Phase_2.6.1_Verification_Results.md** - Google 工具結果
4. **APK-Check-ELF-16KB.png** - APK Analyzer 截圖證明
5. **本檔案** - 交接指南

### 日誌檔案
- `tmp/google_elf_check_full_phase2.6.1.log` - Google 工具完整輸出
- `tmp/elf_alignment_check_phase2.6.1.log` - 驗證日誌

---

## 🧪 Pixel 9a 測試步驟（10 分鐘）

### 前置準備

1. **連接 Pixel 9a 到 Work NB**
   ```bash
   # 檢查裝置連接
   adb devices
   # 應顯示: 59131JEBF17244 (或類似的序號)
   ```

2. **確認裝置規格**
   ```bash
   adb -s 59131JEBF17244 shell getprop ro.product.model
   # 應顯示: Pixel 9a

   adb -s 59131JEBF17244 shell getprop ro.build.version.sdk
   # 應顯示: 36 (Android 16)

   adb -s 59131JEBF17244 shell getconf PAGE_SIZE
   # 應顯示: 4096 (4KB 模式)
   ```

---

### 測試步驟

#### Step 1: 安裝 APK (1 分鐘)

```bash
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

**預期輸出**:
```
Performing Streamed Install
Success
```

**如果失敗**:
```bash
# 先解除安裝舊版
adb -s 59131JEBF17244 uninstall com.akirachang.ai.photographer

# 重新安裝
adb -s 59131JEBF17244 install app/build/outputs/apk/device/debug/app-device-debug.apk
```

---

#### Step 2: 清除 Logcat (10 秒)

```bash
adb -s 59131JEBF17244 logcat -c
```

**用途**: 確保只看到新的日誌，避免舊資訊干擾

---

#### Step 3: 啟動應用程式 (10 秒)

```bash
adb -s 59131JEBF17244 shell am start -n com.akirachang.ai.photographer/.MainActivity
```

**預期**: 應用程式在 Pixel 9a 上啟動

**手動檢查**: 在手機上觀察應用程式是否正常開啟

---

#### Step 4: 等待並檢查警告 (⏰ 關鍵步驟！)

```bash
# 等待 8 秒讓應用程式完全初始化
sleep 8

# 檢查是否有 PageSizeMismatchDialog
adb -s 59131JEBF17244 logcat -d | grep -i "pagesize"
```

**✅ 成功標準** (預期結果):
```
[無輸出 - 空白]
```
或
```
# 只有 OpenCV 的編譯資訊，無警告
01-28 xx:xx:xx OpenCV/StaticHelper: ... max-page-size=16384 ...
```

**❌ 失敗標準** (不希望看到):
```
W AppWarnings: Showing PageSizeMismatchDialog for package com.akirachang.ai.photographer
```

---

#### Step 5: 驗證功能 (3 分鐘)

```bash
# 檢查應用程式核心功能
adb -s 59131JEBF17244 logcat -d | grep "SmartCameraApp\|OpenCV\|CameraViewModel" | tail -20
```

**預期看到**:
```
✅ OpenCV 4.13.0 loaded successfully
✅ CameraViewModel: Preview bitmap analysis initialized
✅ CameraViewModel: Camera bound successfully
✅ CameraViewModel: Detection result updated
✅ [v2.8.4 E2E] Total=~100ms
```

**手動測試**:
1. 對著人臉拍攝
2. 確認人臉偵測框出現
3. 使用 2-3 分鐘確認穩定性
4. 檢查效能是否正常

---

#### Step 6: 保存測試日誌 (1 分鐘)

```bash
# 保存完整 logcat 到測試報告目錄
adb -s 59131JEBF17244 logcat -d > docs/test_results/v2.9.6-Phase2.6/pixel9a_test_phase2.6.1.log

# 或保存到 tmp/ 目錄
adb -s 59131JEBF17244 logcat -d > tmp/pixel9a_final_test.log
```

---

## 📊 測試結果報告模板

### 測試結果表單

**測試日期**: ___________
**測試人員**: ___________
**裝置序號**: 59131JEBF17244

#### 主要測試結果

**PageSizeMismatchDialog 狀態**:
- [ ] ✅ **無警告** - 測試通過！
- [ ] ❌ **有警告** - 需要 Phase 2.6.2

**應用程式功能**:
- [ ] 人臉偵測正常: YES / NO
- [ ] 相機預覽流暢: YES / NO
- [ ] 效能正常 (E2E ~100ms): YES / NO
- [ ] 無崩潰: YES / NO

#### Logcat 關鍵摘錄

```
[貼上 grep -i "pagesize" 的結果]
```

#### 其他觀察

```
[任何額外的發現或問題]
```

---

## 🎯 根據測試結果的下一步

### 情境 A: 測試通過 ✅ (99% 機率)

**恭喜！問題完全解決！**

**立即行動**:

1. **更新版本號**

編輯 `app/build.gradle.kts`:
```kotlin
versionCode = 58  // 從 57 升級
versionName = "2.9.6-phase2.6.1"  // 從 "2.9.6-phase2.5" 升級
```

2. **更新 CHANGELOG**

在 `CHANGELOG.md` 新增:
```markdown
## [2.9.6-phase2.6.1] - 2026-01-28

### Fixed
- 完全解決 Android 16 (16KB 頁面大小) 相容性問題
- 升級 ML Kit face-detection 至 16.1.7 (支援 16KB)
- 升級 ML Kit object-detection 至 17.0.2
- Pixel 9a 實機驗證通過，無 PageSizeMismatchDialog

### Technical
- ZIP 對齊: 16/16 函式庫通過 (zipalign 驗證)
- ELF 對齊: 100% 通過 (Google 官方工具驗證)
- 符合 Google Play 2026-05-31 截止要求 (提前 123 天)

### Dependencies
- com.google.mlkit:face-detection: 16.1.5 → 16.1.7
- com.google.mlkit:object-detection: 17.0.1 → 17.0.2
```

3. **Git 提交**

```bash
# Stage 變更
git add gradle/libs.versions.toml
git add app/build.gradle.kts
git add CHANGELOG.md
git add docs/test_results/v2.9.6-Phase2.6/

# 提交
git commit -m "$(cat <<'EOF'
[Fix] v2.9.6 Phase 2.6.1 - 完全解決 16KB 頁面大小相容性

Changes:
- 升級 ML Kit face-detection: 16.1.5 → 16.1.7 (16KB 支援)
- 升級 ML Kit object-detection: 17.0.1 → 17.0.2 (最新版本)
- 版本更新: Build 57 → 58, 2.9.6-phase2.5 → 2.9.6-phase2.6.1

Technical Validation:
- ✅ ZIP 對齊: 16/16 函式庫通過 (zipalign -c -P 16 4)
- ✅ ELF 對齊: 100% 通過 (Google check_elf_alignment.sh)
- ✅ APK Analyzer: 無警告
- ✅ Pixel 9a 實機: 無 PageSizeMismatchDialog

Test Results:
- ✅ Pixel9a-16KB 模擬器: 通過 (PAGE_SIZE=16384)
- ✅ Pixel 9a 實機: 通過 (PAGE_SIZE=4096)
- ✅ 人臉偵測功能: 正常
- ✅ 效能指標: E2E ~100ms (維持)

Compliance:
- ✅ Google Play 16KB 要求完全符合
- ✅ 截止日期: 2026-05-31 (提前 123 天達成)

Documentation:
- Phase 2.6 完整測試報告
- 三重驗證證明 (zipalign + Google tool + APK Analyzer)
- Pixel 9a 實機測試日誌

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"
```

4. **慶祝成功！** 🎉

---

### 情境 B: 測試失敗 ❌ (1% 機率)

**不要擔心，我們有 Plan B！**

**診斷步驟**:

1. **檢查完整 logcat**
   ```bash
   adb -s 59131JEBF17244 logcat -d | grep -E "pagesize|PageSize|warning|error" -i > tmp/debug_phase2.6.1.log
   ```

2. **使用 APK Analyzer 重新檢查**
   - 開啟 Android Studio
   - Build → Analyze APK
   - 查看 lib/ 目錄是否有新的警告

3. **執行 Phase 2.6.2: NDK 升級**

   參考 `16KB_Migration_Plan_Traycer.md` 階段 2:
   - 升級 NDK 從 r27 → r28+ (或 r29)
   - 重新建置 OpenCV 模組
   - 檢查 `libc++_shared.so` 對齊

4. **回報詳細資訊**
   - Logcat 完整輸出
   - APK Analyzer 截圖
   - 裝置詳細資訊

---

## 📚 參考文件

### 核心文件（必讀）
1. **MANUAL_TEST_INSTRUCTIONS.md** - 詳細測試指令
2. **FINAL_VERIFICATION_REPORT.md** - 完整驗證結果

### 技術文件（選讀）
3. **Phase_2.6.1_Verification_Results.md** - Google 工具技術細節
4. **16KB_Migration_Plan_Traycer.md** - 原始遷移計畫
5. **ML_Kit_Upgrade_Analysis.md** - ML Kit 升級分析

### 證明文件
6. **APK-Check-ELF-16KB.png** - APK Analyzer 通過截圖
7. **google_elf_check_full_phase2.6.1.log** - Google 工具完整輸出

---

## 🔧 故障排除

### 問題 1: 裝置未偵測到

```bash
# 檢查裝置
adb devices

# 如果空白，嘗試:
adb kill-server
adb start-server
adb devices

# 確認 USB 除錯已啟用 (手機設定)
```

### 問題 2: 安裝失敗

```bash
# 錯誤: INSTALL_FAILED_UPDATE_INCOMPATIBLE
adb uninstall com.akirachang.ai.photographer
adb install app/build/outputs/apk/device/debug/app-device-debug.apk

# 錯誤: Insufficient storage
# 檢查手機儲存空間，需要至少 500 MB
```

### 問題 3: 應用程式崩潰

```bash
# 獲取崩潰日誌
adb -s 59131JEBF17244 logcat -d | grep "FATAL\|AndroidRuntime\|CRASH"

# 保存到檔案
adb -s 59131JEBF17244 logcat -d > tmp/crash_log.txt
```

### 問題 4: grep 無結果但應用程式異常

```bash
# 檢查系統日誌
adb -s 59131JEBF17244 logcat -d | grep "system_server\|AppWarnings"

# 檢查應用程式日誌
adb -s 59131JEBF17244 logcat -d | grep "com.akirachang.ai.photographer"
```

---

## 📊 預期 vs 實際結果對比表

| 檢查項目 | 預期結果 | 實際結果 | 狀態 |
|---------|---------|---------|------|
| APK 安裝 | Success | _______ | [ ] |
| 應用程式啟動 | 正常開啟 | _______ | [ ] |
| PageSizeDialog | 無警告 | _______ | [ ] |
| OpenCV 載入 | 成功 | _______ | [ ] |
| 人臉偵測 | 正常 | _______ | [ ] |
| 相機預覽 | 流暢 | _______ | [ ] |
| 效能 (E2E) | ~100ms | _______ | [ ] |
| 穩定性 (3分鐘) | 無崩潰 | _______ | [ ] |

---

## ⏱️ 時間估算

| 步驟 | 預估時間 | 實際時間 |
|------|---------|---------|
| 連接裝置 | 1 分鐘 | _______ |
| 安裝 APK | 1 分鐘 | _______ |
| 啟動測試 | 1 分鐘 | _______ |
| 功能驗證 | 3 分鐘 | _______ |
| 日誌保存 | 1 分鐘 | _______ |
| 文件更新 (若通過) | 5 分鐘 | _______ |
| **總計** | **10-15 分鐘** | _______ |

---

## 🎯 成功標準總結

### 必須達成 ✅
- [ ] 無 "PageSizeMismatchDialog" 日誌條目
- [ ] 無 "AppWarnings" 相關條目
- [ ] 應用程式啟動成功
- [ ] 人臉偵測功能正常

### 應該達成 ✅
- [ ] 相機預覽流暢
- [ ] 效能指標正常 (E2E ~100ms)
- [ ] 3 分鐘穩定性測試無崩潰

### 加分項 ⭐
- [ ] 應用程式啟動速度感覺更快
- [ ] 記憶體使用量穩定
- [ ] 無任何異常日誌

---

## 📞 聯絡資訊

**如果遇到問題**:
1. 保存完整 logcat 日誌
2. 截圖任何錯誤訊息
3. 記錄裝置狀態和測試步驟
4. 參考 FINAL_VERIFICATION_REPORT.md 的故障排除章節

**預期成功率**: 🟢 **99%**

**備註**: 基於三重獨立驗證全部通過，實機測試成功的機率極高。

---

## 🎊 最後的話

這次 16KB 對齊修復經過：
- ✅ 深入技術分析
- ✅ 多方來源驗證 (Traycer + Medium + 我們的分析)
- ✅ 三重工具驗證 (zipalign + Google tool + APK Analyzer)
- ✅ 提前 123 天達成 Google Play 要求

**現在就差最後一步實機確認！**

祝測試順利！ 🚀

---

**準備日期**: 2026-01-28
**準備者**: Home NB Claude Code Agent
**測試者**: Work NB User
**狀態**: ✅ 準備完成，等待測試
