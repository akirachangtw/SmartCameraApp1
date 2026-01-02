# Phase 1 模擬器測試指引

**測試時間:** 2026-01-02 15:34
**模擬器:** emulator-5554
**APK版本:** v2.6.2 emulatorDebug

---

## ✅ 已完成步驟

1. ✅ 模擬器已啟動
2. ✅ APK 編譯成功 (BUILD SUCCESSFUL in 1m 17s)
3. ✅ APK 安裝成功
4. ✅ APP 已啟動
5. ✅ 收集到 AUTO 模式日誌

---

## 📊 AUTO 模式測試結果 (已自動完成)

### ✅ 關鍵發現

**無 "CameraControl not available" 警告!**

AUTO 模式測試日誌顯示:

```
01-02 15:34:37.363  CameraRepositoryImpl: Delegating HDR enable to CameraInputSource
01-02 15:34:37.364  StaticImageSource: enableHDR() called - no-op for static images ✅
01-02 15:34:37.366  CameraViewModel: Auto HDR activated

01-02 15:34:42.253  CameraRepositoryImpl: Delegating HDR disable to CameraInputSource
01-02 15:34:42.253  StaticImageSource: disableHDR() called - no-op for static images ✅
01-02 15:34:42.253  CameraViewModel: Auto HDR deactivated

01-02 15:34:51.048  CameraRepositoryImpl: Delegating HDR enable to CameraInputSource
01-02 15:34:51.049  StaticImageSource: enableHDR() called - no-op for static images ✅
01-02 15:34:51.049  CameraViewModel: Auto HDR activated
```

### ✅ 驗證通過項目

- ✅ **Repository 委派正常**: "Delegating HDR enable/disable to CameraInputSource"
- ✅ **StaticImageSource no-op**: "enableHDR() called - no-op for static images"
- ✅ **無錯誤警告**: 完全沒有 "CameraControl not available"
- ✅ **AUTO 模式生效**: AI 偵測到高對比場景自動觸發 HDR

---

## 📋 待執行: 手動 HDR 按鈕測試

請在模擬器上執行以下操作:

### Test Case 6: 手動 HDR ON/OFF

1. **在模擬器上點擊 HDR 按鈕 → ON**
   - 等待 2 秒

2. **點擊 HDR 按鈕 → OFF**
   - 等待 2 秒

3. **點擊 HDR 按鈕 → AUTO**
   - 等待 2 秒

### 預期日誌

執行上述操作時,應該看到:

```
CameraRepositoryImpl: Delegating HDR enable to CameraInputSource
StaticImageSource: enableHDR() called - no-op for static images

CameraRepositoryImpl: Delegating HDR disable to CameraInputSource
StaticImageSource: disableHDR() called - no-op for static images
```

**關鍵:** ❌ **不應該** 看到 "CameraControl not available"

---

## 🚀 開始手動測試

### 方式 1: 我協助監控 (推薦)

告訴我:
```
"開始手動測試 TC6"
```

我會啟動 logcat 監控,您執行點擊操作後,我會分析結果。

### 方式 2: 您自行執行

在另一個 PowerShell 視窗執行:

```powershell
# 清除 logcat
adb logcat -c

# 啟動監控
adb logcat | Select-String "(HDR|CameraControl|CameraRepository|StaticImageSource)" |
    Tee-Object -FilePath "emulator_tc6_manual_$(Get-Date -Format 'HHmmss').txt"

# 執行點擊操作後按 Ctrl+C 停止
```

然後告訴我:
```
"TC6 完成,請分析日誌"
```

---

## 📊 目前進度總結

| Test Case | 狀態 | 結果 |
|-----------|------|------|
| TC1: AUTO 模式 (AI 自動) | ✅ 完成 | 通過 |
| TC6: 手動 ON/OFF | ⏳ 待執行 | - |

---

**下一步:** 執行 TC6 手動測試,然後進入 Phase 2 實機測試。
