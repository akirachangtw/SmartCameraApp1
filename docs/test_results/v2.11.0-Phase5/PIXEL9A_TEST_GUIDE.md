# Pixel 9a v2.11.0 實機測試指南

## 📱 測試環境

- **Device**: Pixel 9a (59131JEBF17244)
- **Android Version**: 15 (API 36)
- **Version**: v2.11.0 (Build 73)
- **APK**: `app-device-debug.apk` (246 MB)

---

## 🚀 前置準備

### 1. 安裝 APK

```bash
# 方法 1: 重新編譯（推薦）
./gradlew assembleDeviceDebug
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# 方法 2: 使用已編譯的 APK（如果從 Home NB 傳輸）
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

### 2. 啟動 Logcat 監控

```bash
# 創建測試輸出目錄
mkdir -p docs/test_results/v2.11.0-Phase5

# 清除舊 Logcat
adb -s 59131JEBF17244 logcat -c

# 啟動監控（背景執行）
adb -s 59131JEBF17244 logcat -v time > docs/test_results/v2.11.0-Phase5/logcat.log 2>&1 &
```

### 3. 啟動應用程式

```bash
adb -s 59131JEBF17244 shell am start -n com.akirachang.ai.photographer/com.akirachang.ai.photographer.MainActivity
```

**檢查啟動狀態**：
```bash
adb -s 59131JEBF17244 shell dumpsys window | grep mCurrentFocus
# 預期輸出: mCurrentFocus=Window{... com.akirachang.ai.photographer/...MainActivity}
```

---

## 🎯 測試場景清單

### 🔹 場景 1: Eye-AF (眼睛優先對焦) ⭐ **實機專屬**

**測試目的**: 驗證 ML Kit 眼睛 landmark 提取與自動對焦整合

**前置條件**:
- 切換到**前置相機**（自拍模式）

**測試步驟**:
1. 點擊相機切換按鈕（前後相機切換）
2. 將臉部**正面**對準相機
3. 保持臉部位於畫面中央
4. 觀察焦點指示器的位置

**預期結果**:
- ✅ 焦點自動定位於**眼睛位置**（而非臉部中心）
- ✅ 如果畫面中有兩隻眼睛，優先對焦於**最近**的眼睛
- ✅ 矩形框（Rectangle）顯示在眼睛上
- ✅ 對焦成功後，出現**角落括號**（Corner Brackets）
- ✅ **Overshoot 動畫**：矩形框縮小 10% → 彈回原大小
- ✅ 顏色：Cyan 青色 (#00E5FF)

**檢查 Logcat**:
```bash
# 過濾 Eye-AF 相關日誌
adb -s 59131JEBF17244 logcat -s "MLKitDetector:D" | grep "Eye"

# 預期日誌範例：
# MLKitDetector: Eye-AF: Left eye at (x, y), Right eye at (x, y)
# MLKitDetector: Eye-AF: Selected eye at (x, y)
```

**截圖記錄**:
```bash
adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/eye_af.png"
adb -s 59131JEBF17244 pull //sdcard//Download//eye_af.png docs/test_results/v2.11.0-Phase5/
adb -s 59131JEBF17244 shell "rm /sdcard/Download/eye_af.png"
```

**可能的問題**:
- ❌ 焦點在臉部中心而非眼睛 → Eye-AF 未觸發
- ❌ 無角落括號顯示 → 視覺設計問題
- ❌ 無 Overshoot 動畫 → 動畫邏輯問題

---

### 🔹 場景 2: Smart Subject Focus (智能主體對焦)

**測試目的**: 驗證基於 Saliency 的自動對焦邏輯

**前置條件**:
- 使用**後置相機**

**測試步驟**:
1. 畫面中放置**多個物體**（如桌上的杯子、筆、手機、人臉）
2. **不要點擊螢幕**
3. 等待 5 秒，觀察焦點自動定位

**預期結果**:
- ✅ 自動對焦於**最顯著的主體**
- ✅ 優先順序：**人臉 > 大型物體 > 居中物體**
- ✅ 矩形框自動出現在主體上
- ✅ 無需手動點擊

**檢查 Logcat**:
```bash
adb -s 59131JEBF17244 logcat -s "CameraViewModel:D" | grep "Smart Subject"

# 預期日誌範例：
# CameraViewModel: Smart Subject Focus: Saliency score = 0.85 at (x, y)
# CameraViewModel: Smart Subject Focus: Auto-focus triggered
```

**變化測試**:
1. **無人臉場景**：畫面只有物體 → 對焦於最大物體
2. **人臉場景**：畫面有人臉 + 物體 → 優先對焦人臉
3. **多物體場景**：多個相似大小的物體 → 對焦於居中物體

**截圖記錄**:
```bash
adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/smart_subject_focus.png"
adb -s 59131JEBF17244 pull //sdcard//Download//smart_subject_focus.png docs/test_results/v2.11.0-Phase5/
adb -s 59131JEBF17244 shell "rm /sdcard/Download/smart_subject_focus.png"
```

---

### 🔹 場景 3: 手動點擊覆蓋 AI

**測試目的**: 驗證手動點擊可以覆蓋 AI 自動對焦

**測試步驟**:
1. 等待 Smart Subject Focus 觸發（場景 2）
2. **手動點擊**螢幕其他位置

**預期結果**:
- ✅ 焦點立即移動到點擊位置
- ✅ AI 自動對焦**暫停 5 秒**
- ✅ 5 秒後，AI 自動對焦恢復

**檢查 Logcat**:
```bash
adb -s 59131JEBF17244 logcat -s "CameraViewModel:D" | grep "Manual tap"

# 預期日誌範例：
# CameraViewModel: Manual tap detected, pausing Smart Subject Focus for 5s
```

---

### 🔹 場景 4: Rectangle + Corner Brackets 視覺設計 ⭐ **實機色彩驗證**

**測試目的**: 驗證新的焦點指示器視覺效果

**測試步驟**:
1. 在**黑暗背景**下點擊螢幕對焦
2. 仔細觀察焦點指示器的視覺變化

**預期結果**:

| 狀態 | 視覺效果 | 動畫 | 持續時間 |
|------|---------|------|---------|
| **Searching** (對焦中) | 矩形框，無角落括號 | 無 | 直到對焦完成 |
| **Success** (對焦成功) | 矩形框 + 角落括號 | 縮小 10% → 彈回 | 瞬間 |
| **Dwell** (3秒後) | Alpha 0.4，stroke 1.5dp | 淡化 | 持續顯示 |

**顏色檢查**:
- ✅ **Cyan 青色** (#00E5FF) - neon glow 效果
- ✅ **雙層輝光**：
  - 外層：Alpha 0.3（半透明）
  - 內層：Alpha 1.0（實色）
- ✅ **角落括號**：4 個角落，L 形

**動畫檢查**:
- ✅ **Overshoot 動畫**（Success 狀態）：
  - 矩形框縮小至 90%
  - 彈回至 100%
  - 時長：約 300ms

**截圖記錄**（3 個狀態）:
```bash
# Searching 狀態（對焦中）
adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/searching.png"

# Success 狀態（對焦成功，立即截圖）
adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/success.png"

# Dwell 狀態（等待 3 秒後）
adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/dwell.png"

# 拉取所有截圖
adb -s 59131JEBF17244 pull //sdcard//Download//searching.png docs/test_results/v2.11.0-Phase5/
adb -s 59131JEBF17244 pull //sdcard//Download//success.png docs/test_results/v2.11.0-Phase5/
adb -s 59131JEBF17244 pull //sdcard//Download//dwell.png docs/test_results/v2.11.0-Phase5/
adb -s 59131JEBF17244 shell "rm /sdcard/Download/*.png"
```

---

### 🔹 場景 5: AE/AF Lock (Cyber Yellow) ⭐ **色彩驗證**

**測試目的**: 驗證 AE/AF Lock 與新視覺設計的整合

**測試步驟**:
1. **長按**螢幕任意位置（約 1-2 秒）
2. 觀察焦點指示器的顏色變化

**預期結果**:
- ✅ 顏色變為 **Cyber Yellow** (#FFD700)
- ✅ 矩形框 + 角落括號保持顯示
- ✅ Neon glow 效果（雙層輝光，黃色）
- ✅ Smart Subject Focus **暫停**

**解除 Lock**:
- 點擊螢幕其他位置
- 預期：顏色恢復為 Cyan，Smart Subject Focus 恢復

**截圖記錄**:
```bash
adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/ae_af_lock.png"
adb -s 59131JEBF17244 pull //sdcard//Download//ae_af_lock.png docs/test_results/v2.11.0-Phase5/
adb -s 59131JEBF17244 shell "rm /sdcard/Download/ae_af_lock.png"
```

---

### 🔹 場景 6: 穩定性測試

**測試目的**: 驗證應用長時間運行的穩定性

**測試項目**:

#### 6.1 螢幕旋轉測試
- 旋轉手機至橫向模式（Landscape）
- 旋轉回直向模式（Portrait）
- 重複 **3 次**

**預期結果**:
- ✅ UI 正常旋轉
- ✅ 焦點指示器正確縮放
- ✅ 無 crash 或凍結

#### 6.2 模式切換測試（如果有）
- 切換相機模式（如 Live / Static / Debug）
- 重複 **5 次**

**預期結果**:
- ✅ 切換流暢
- ✅ 無 crash

#### 6.3 長時間運行測試
- 保持應用運行 **5-10 分鐘**
- 觀察手機溫度和記憶體使用

**預期結果**:
- ✅ 無明顯卡頓
- ✅ 手機溫度正常（不過熱）
- ✅ Memory 增長 < 20%

**檢查記憶體**:
```bash
# 運行前
adb -s 59131JEBF17244 shell dumpsys meminfo com.akirachang.ai.photographer | grep "TOTAL"

# 運行 10 分鐘後
adb -s 59131JEBF17244 shell dumpsys meminfo com.akirachang.ai.photographer | grep "TOTAL"
```

---

### 🔹 場景 7: 光學流追蹤與 v2.11.0 整合測試（可選）

**測試目的**: 驗證 Optical Flow 與新視覺設計無衝突

**前置條件**:
- 需要啟用 Optical Flow Point Tracking

**測試步驟**:
1. 點擊並追蹤移動物體
2. 觀察 Optical Flow 追蹤點與 Rectangle 的互動

**預期結果**:
- ✅ Optical Flow 追蹤點正常顯示
- ✅ Rectangle 淡化至 Alpha 0.4（符合規範）
- ✅ 無視覺衝突

**參考**:
- 規範文檔：`devspec/feature_record_Backlog.md`
- ⚠️ **Known Pitfall**: 不要添加 `&& !trackedPointActive` 淡化守衛（參考 MEMORY.md）

---

## 📝 測試結果記錄

### 測試報告模板

創建 `docs/test_results/v2.11.0-Phase5/PIXEL9A_TEST_REPORT.md`：

```markdown
# Pixel 9a v2.11.0 測試報告

## 測試環境
- Device: Pixel 9a (59131JEBF17244)
- Android Version: 15
- Test Date: YYYY-MM-DD
- Tester: [Your Name]

## 測試結果

### 場景 1: Eye-AF
- [ ] PASS / [ ] FAIL
- 說明: [描述測試結果]
- 截圖: [附上截圖檔名]

### 場景 2: Smart Subject Focus
- [ ] PASS / [ ] FAIL
- 說明: [描述測試結果]

... (其他場景)

## 發現的問題

1. [Issue 描述]
   - 嚴重程度: High / Medium / Low
   - 重現步驟: ...
   - Logcat: ...

## 總結

- 通過場景: X / 7
- 失敗場景: X / 7
- 阻塞問題: X 個
- 建議: ...
```

---

## 📤 測試完成後

### 1. 停止 Logcat 監控

```bash
# 找到 Logcat 進程
ps aux | grep "adb.*logcat"

# 結束進程（替換 PID）
kill <PID>
```

### 2. 拉取 Logcat 日誌

```bash
# 日誌已自動保存至 docs/test_results/v2.11.0-Phase5/logcat.log
```

### 3. 提交測試結果

```bash
git add docs/test_results/v2.11.0-Phase5/
git commit -m "[Test] v2.11.0: Pixel 9a 實機測試結果

測試結果摘要:
- Eye-AF: PASS/FAIL
- Smart Subject Focus: PASS/FAIL
- Rectangle + Corner Brackets: PASS/FAIL
- AE/AF Lock: PASS/FAIL
- 穩定性: PASS/FAIL

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

git push origin feature/camera-core-v2.11.0
```

---

## ⚠️ 重要提醒

### ADB 截圖方法（Windows）

❌ **絕對不要使用**：
```bash
adb shell screencap -p > file.png  # 會造成 PNG 損壞
```

✅ **正確方法**：
```bash
adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/temp.png"
adb -s 59131JEBF17244 pull //sdcard//Download//temp.png docs/test_results/v2.11.0-Phase5/
adb -s 59131JEBF17244 shell "rm /sdcard/Download/temp.png"
```

### Logcat 過濾技巧

```bash
# 只看應用相關日誌
adb logcat -s "ai.photographer:*"

# 只看錯誤
adb logcat *:E

# 多個 TAG
adb logcat -s "CameraViewModel:D" "MLKitDetector:D"
```

---

**測試預計時間**: 30-60 分鐘
**下一步**: 完成測試後，提交結果並通知 Home NB
