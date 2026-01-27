# 🚀 Phase 2.6 Quick Start Guide

**新接手的 Claude，請先讀這份文件！**

---

## 📋 3 分鐘快速理解

### 問題核心
Android 16 在 Pixel 9a 上顯示警告：
> "This app uses native libraries that are not aligned to 16KB page boundaries"

### 真正原因（Phase 2.5 重大發現）
❌ **不是** ELF 二進位結構問題
✅ **是** APK ZIP 封裝結構問題

**兩層對齊**：
1. **Layer 1 (ZIP Offset)** ← Android 16 檢查這個 → ❌ 0/8 對齊
2. **Layer 2 (ELF LOAD)** ← Phase 2.5 修復這個 → ✅ 6/8 對齊

### 解決方案
使用 `zipalign -p -f -z 16` 修復 ZIP 偏移量對齊

---

## 🎯 你的任務 (Phase 2.6)

### Step 1: Build APK
```bash
./gradlew clean assembleDeviceDebug
```

### Step 2: Apply zipalign
```bash
zipalign -p -f -z 16 \
  app/build/outputs/apk/device/debug/app-device-debug.apk \
  app/build/outputs/apk/device/debug/app-device-debug-aligned.apk
```

### Step 3: Verify ZIP Alignment
```bash
zipinfo -v app-device-debug-aligned.apk | \
  grep "lib/arm64-v8a.*\.so" -A 3 | \
  grep "offset"

# Expected: All offsets divisible by 16384
```

### Step 4: Test on Pixel 9a
```bash
# Install
adb -s 59131JEBF17244 install app-device-debug-aligned.apk

# Launch and check
adb -s 59131JEBF17244 shell am start -n com.akirachang.ai.photographer/.MainActivity

# Verify (should be EMPTY)
adb -s 59131JEBF17244 logcat -d | grep -i "pagesize"
```

### Step 5: Update Version & Commit
```kotlin
// app/build.gradle.kts
versionCode = 58
versionName = "2.9.6-phase2.6"
```

---

## 📚 必讀文件（依序閱讀）

1. **這份文件** - 快速開始 ✅ 你在這裡
2. **完整交接文件**: `chat_logs/handofflog_20260127_223000.md` - 技術細節
3. **Gemini 計畫**: `docs/plan/v2.9.6_Phase2.6_ZIP_Alignment_Plan.md` - 可行性分析

---

## ⚠️ 關鍵注意事項

### 不要犯這些錯誤

1. ❌ **不要**只檢查 ELF 對齊 (llvm-readelf) → Phase 2.5 已做過，不夠
2. ❌ **不要**相信 AGP 8.10.1 自動對齊 → 已證實無效
3. ❌ **不要**只在模擬器測試 → 必須實機 Pixel 9a 驗證
4. ❌ **不要**忘記視覺確認無警告對話框 → logcat 不夠，要看螢幕

### 必須做這些

1. ✅ **必須**使用 `zipinfo -v` 驗證 ZIP 偏移量
2. ✅ **必須**在實機 Pixel 9a (PAGE_SIZE=4096) 測試
3. ✅ **必須**視覺確認螢幕無警告對話框
4. ✅ **必須**確認所有 8 個函式庫 ZIP offset % 16384 == 0

---

## 🎓 背景知識速成

### 為什麼 Phase 2.5 失敗？

**Phase 2.5 做了什麼**：
- 升級 CameraX 1.4.1 → 1.4.2
- 修復 6/8 函式庫的 **ELF LOAD segment** 對齊

**為什麼還有警告**：
- Pixel 9a 警告截圖顯示**全部 8 個函式庫**
- 不只是 2 個 ML Kit 未修復的
- 證明 Android 16 檢查的是 **ZIP 結構**，非 ELF

**驗證證據**：
```bash
# Phase 2.5 用這個檢查（ELF 層）
llvm-readelf -l library.so | grep LOAD
# Result: 6/8 libraries show 0x4000 (16KB) ✅

# 但應該用這個檢查（ZIP 層）
zipinfo -v app.apk | grep "lib/arm64-v8a" -A 3
# Result: 0/8 libraries aligned ❌ ← 這才是問題！
```

### ZIP 偏移量實例

**未對齊（Phase 2.5 狀態）**：
```
libsurface_util_jni.so: offset=63621824
63621824 % 16384 = 2752 ❌ 不是 0
```

**對齊後（Phase 2.6 目標）**：
```
libsurface_util_jni.so: offset=63619072
63619072 % 16384 = 0 ✅ 完美對齊
```

---

## 🔧 工具速查

### zipalign 位置
```bash
# 通常在 Android SDK build-tools 目錄
$ANDROID_HOME/build-tools/35.0.0/zipalign

# 或使用系統 PATH
zipalign -h  # 檢查是否可用
```

### zipinfo 驗證腳本
```python
import re

with open('zip_offsets.txt') as f:
    for line in f:
        if 'offset' in line:
            offset = int(re.search(r'(\d+)', line).group(1))
            aligned = "✅ YES" if offset % 16384 == 0 else "❌ NO"
            print(f'{offset:>12,}  16KB: {aligned}  (mod {offset % 16384})')
```

---

## 📊 成功標準

**Phase 2.6 完成的定義**：

- ✅ **Build**: APK 成功建置
- ✅ **ZIP 對齊**: 8/8 函式庫 offset % 16384 == 0
- ✅ **模擬器測試**: Pixel9a-16KB 無警告
- ✅ **實機測試**: Pixel 9a 無警告（**最關鍵**）
- ✅ **視覺確認**: 螢幕無 PageSizeMismatchDialog
- ✅ **功能測試**: 相機、AI、OpenCV 全部正常
- ✅ **版本更新**: versionCode=58, versionName="2.9.6-phase2.6"
- ✅ **Git 提交**: 清晰的 commit 訊息

---

## 🚨 如果失敗怎麼辦？

### Scenario 1: zipalign 失敗
```bash
# Error: zipalign command not found
# Solution: 使用完整路徑
$ANDROID_HOME/build-tools/35.0.0/zipalign -p -f -z 16 input.apk output.apk
```

### Scenario 2: ZIP 對齊後仍有函式庫未對齊
```bash
# 檢查是否有新的函式庫加入
zipinfo -v aligned.apk | grep "\.so" | wc -l
# 應該是 8 個（每個 ABI 架構各 8 個）

# 重新對齊
zipalign -p -f -z 16 aligned.apk re-aligned.apk
```

### Scenario 3: 實機仍有警告
1. 確認安裝的是 `-aligned.apk`，不是原始 APK
2. 重新驗證 ZIP 偏移量
3. 檢查 logcat 詳細錯誤訊息
4. 截圖警告對話框內容

---

## 💬 語言規範提醒

- **程式碼/文件內容**: 英文
- **與使用者對話**: 繁體中文
- **Git commit**: 英文
- **技術分析**: 英文（文件內）、繁體中文（對話）

---

## 📞 需要幫助？

如遇到問題，檢查這些資源：

1. **完整技術細節**: `chat_logs/handofflog_20260127_223000.md`
2. **ZIP 對齊分析**: `docs/test_results/v2.9.6-Phase2.5/Critical_Finding_ZIP_Alignment.md`
3. **Gemini 計畫**: `docs/plan/v2.9.6_Phase2.6_ZIP_Alignment_Plan.md`

---

**準備好了嗎？開始執行 Step 1: Build APK！** 🚀

**記住**：這次是 **ZIP 對齊**，不是 ELF 對齊！
