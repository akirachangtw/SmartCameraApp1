# Pixel 9a 實機測試指南 - v2.9.9 Day 1-2

## 📱 測試環境

- **測試設備**: Google Pixel 9a
- **Android 版本**: Android 15 (16KB Page Size)
- **測試版本**: v2.9.9-Day1-2 (versionCode: 60)
- **測試日期**: 2026-02-02

## 🎯 測試目標

驗證 **Traycer v2 專業對焦系統** 的以下功能：
1. AE/AF Lock（長按鎖定曝光與對焦）
2. 虛線3等分弧線視覺設計
3. 三模式並存（追蹤/靜態/鎖定）
4. 效能表現（FPS ≥ 25）

## 🔧 測試前準備

### 1. 拉取代碼並編譯

```bash
# 拉取最新代碼
cd /path/to/SmartCameraApp1
git pull github main

# 編譯 Device Debug APK
./gradlew assembleDeviceDebug

# 確認 APK 生成
ls -lh app/build/outputs/apk/device/debug/app-device-debug.apk
```

### 2. 安裝到 Pixel 9a

```bash
# 查看設備 ID
adb devices

# 安裝 APK (替換 <DEVICE_ID> 為實際設備 ID)
adb -s <DEVICE_ID> install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# 啟動應用
adb -s <DEVICE_ID> shell am start -n com.akirachang.ai.photographer/.MainActivity
```

### 3. 啟用 Logcat 監控

```bash
# 清除舊日誌
adb -s <DEVICE_ID> logcat -c

# 開始監控 (保存到檔案)
adb -s <DEVICE_ID> logcat -s "CameraViewModel:* LiveCameraSource:* CameraScreen:*" > docs/test_results/v2.9.9-Day1-2/logcat_test.log
```

## ✅ 測試場景

### 測試場景 1: 人臉追蹤模式 ⭐ HIGH

**目標**: 驗證點擊人臉後，白色虛線圓圈連續跟隨移動

**操作步驟**:
1. 開啟應用，確保相機預覽正常
2. 確保畫面中有人臉（可使用前鏡頭自拍）
3. **點擊畫面中的人臉**

**預期結果**:
- ✅ 白色虛線3等分弧線立即出現在人臉位置
- ✅ 弧線由 3 段 120° 組成，虛線效果清晰可見
- ✅ 圓圈連續跟隨人臉移動（左右移動、上下移動）
- ✅ 使用 Spring 彈性動畫，過渡平滑無跳動
- ✅ 中心點（3dp 實心圓）對準人臉中心

**驗證方法**:
```bash
# 查看 Logcat 確認追蹤啟動
grep "Tracking started" docs/test_results/v2.9.9-Day1-2/logcat_test.log

# 預期輸出範例:
# CameraViewModel: Tracking started: ID=12345, type=FACE
```

**截圖要求**:
- 📸 追蹤圓圈跟隨人臉（至少 3 張不同位置）
- 📸 虛線3等分弧線特寫

**通過標準**:
- [ ] 白色虛線圓圈正確顯示
- [ ] 圓圈連續跟隨人臉移動
- [ ] Spring 動畫平滑無跳動
- [ ] 虛線效果清晰（dash 8dp, gap 6dp）

---

### 測試場景 2: 靜態對焦模式 ⭐ HIGH

**目標**: 驗證點擊空白處，螢光藍圓圈固定位置，對焦完成變白色

**操作步驟**:
1. 確保畫面中有清晰的靜物（書本、杯子等）
2. **點擊靜物表面（非人臉/物件）**
3. 觀察對焦過程和顏色變化

**預期結果**:
- ✅ 螢光藍 (#00E5FF) 虛線圓圈立即出現在點擊位置
- ✅ 圓圈固定不移動
- ✅ 對焦成功後變為白色 (#FFFFFF)
- ✅ 1 秒後自動消失

**驗證方法**:
```bash
# 查看 Logcat 確認靜態對焦
grep "Static focus mode" docs/test_results/v2.9.9-Day1-2/logcat_test.log
grep "Focus successful" docs/test_results/v2.9.9-Day1-2/logcat_test.log

# 預期輸出範例:
# CameraViewModel: Static focus mode at (540.0, 960.0)
# CameraViewModel: Focus successful at (540.0, 960.0)
```

**截圖要求**:
- 📸 螢光藍圓圈（對焦中）
- 📸 白色圓圈（對焦成功）

**通過標準**:
- [ ] 螢光藍圓圈正確顯示
- [ ] 對焦成功變為白色
- [ ] 1 秒後自動消失
- [ ] 圓圈固定不移動

---

### 測試場景 3: AE/AF 鎖定 ⭐⭐ CRITICAL

**目標**: 驗證長按觸發 AE/AF 鎖定，顯示黃色方框和圓圈

**操作步驟**:
1. 對準有明顯亮暗對比的場景（窗戶、燈光）
2. **長按預覽畫面**（約 1 秒）
3. 觀察黃色指示器出現
4. 移動相機角度，觀察曝光是否鎖定

**預期結果**:
- ✅ 黃色虛線方框（80dp）出現在長按位置
- ✅ 黃色虛線3等分弧線同時顯示
- ✅ 移動相機時，曝光和對焦保持鎖定（畫面亮度不變）
- ✅ 指示器持續顯示，不自動消失

**驗證方法**:
```bash
# 查看 Logcat 確認 AE/AF 鎖定
grep "Locking AE/AF" docs/test_results/v2.9.9-Day1-2/logcat_test.log
grep "AE/AF locked successfully" docs/test_results/v2.9.9-Day1-2/logcat_test.log

# 預期輸出範例:
# CameraViewModel: Locking AE/AF at (540.0, 960.0)
# LiveCameraSource: AE/AF locked successfully at (540.0, 960.0)
```

**截圖要求**:
- 📸 黃色方框 + 圓圈（鎖定狀態）
- 📸 移動相機角度，曝光保持不變

**通過標準**:
- [ ] 黃色虛線方框正確顯示（80dp）
- [ ] 黃色虛線3等分弧線正確顯示
- [ ] 曝光與對焦鎖定（移動相機，亮度不變）
- [ ] 指示器持續顯示

---

### 測試場景 4: 解除鎖定 ⭐ HIGH

**目標**: 驗證鎖定狀態下點擊螢幕，黃色指示器消失，恢復自動模式

**操作步驟**:
1. 先執行測試場景 3，進入 AE/AF 鎖定狀態
2. **點擊螢幕任意位置**
3. 觀察黃色指示器消失

**預期結果**:
- ✅ 黃色方框立即消失
- ✅ 黃色圓圈立即消失
- ✅ 恢復自動曝光與對焦模式
- ✅ 移動相機時，曝光自動調整

**驗證方法**:
```bash
# 查看 Logcat 確認解鎖
grep "AE/AF unlocked by tap" docs/test_results/v2.9.9-Day1-2/logcat_test.log
grep "AE/AF unlocked" docs/test_results/v2.9.9-Day1-2/logcat_test.log

# 預期輸出範例:
# CameraViewModel: AE/AF unlocked by tap
# CameraViewModel: AE/AF unlocked, returned to auto mode
```

**截圖要求**:
- 📸 解鎖後畫面（無黃色指示器）

**通過標準**:
- [ ] 黃色指示器完全消失
- [ ] 恢復自動曝光與對焦
- [ ] 無殘留視覺元素

---

### 測試場景 5: 追蹤目標離開畫面 ⭐ MEDIUM

**目標**: 驗證追蹤目標離開畫面後，圓圈自動消失

**操作步驟**:
1. 點擊人臉進入追蹤模式
2. 讓人臉慢慢移出畫面（左、右、上、下任一方向）
3. 觀察圓圈消失

**預期結果**:
- ✅ 人臉離開畫面後，追蹤圓圈在 **10 幀內**消失
- ✅ 約 0.3-0.5 秒延遲（取決於 FPS）

**驗證方法**:
```bash
# 查看 Logcat 確認追蹤清除
grep "Tracking target lost" docs/test_results/v2.9.9-Day1-2/logcat_test.log

# 預期輸出範例:
# CameraViewModel: Tracking target lost - clearing after 10 frames
```

**截圖要求**:
- 📸 人臉在畫面邊緣（追蹤中）
- 📸 人臉離開後圓圈消失

**通過標準**:
- [ ] 追蹤圓圈在 10 幀內消失
- [ ] 無殘留視覺元素

---

### 測試場景 6: 切換相機清除狀態 ⭐ HIGH

**目標**: 驗證切換前後鏡頭時，追蹤與鎖定狀態自動清除

**操作步驟**:
1. **子測試 A**: 追蹤模式下切換相機
   - 點擊人臉進入追蹤模式
   - 點擊「切換相機」按鈕
   - 觀察追蹤圓圈是否消失

2. **子測試 B**: 鎖定模式下切換相機
   - 長按進入 AE/AF 鎖定模式
   - 點擊「切換相機」按鈕
   - 觀察黃色指示器是否消失

**預期結果**:
- ✅ 追蹤圓圈立即消失
- ✅ 黃色方框和圓圈立即消失
- ✅ 切換後相機處於自動模式

**驗證方法**:
```bash
# 查看 Logcat 確認狀態清除
grep "Clear tracking state before switching" docs/test_results/v2.9.9-Day1-2/logcat_test.log

# 預期輸出範例:
# CameraViewModel: Clear tracking state before switching camera
```

**截圖要求**:
- 📸 切換前（追蹤/鎖定狀態）
- 📸 切換後（無指示器）

**通過標準**:
- [ ] 追蹤狀態自動清除
- [ ] 鎖定狀態自動解除
- [ ] 無殘留視覺元素

---

### 測試場景 7: 效能測試 ⭐⭐ CRITICAL

**目標**: 驗證追蹤模式下 FPS ≥ 25，無明顯延遲或卡頓

**操作步驟**:
1. 開啟 FPS 顯示（如果有）
2. 點擊人臉進入追蹤模式
3. 持續追蹤 30 秒
4. 記錄 FPS 數據

**預期結果**:
- ✅ Pixel 9a FPS ≥ 25（追蹤模式）
- ✅ 無明顯延遲或卡頓
- ✅ 追蹤圓圈移動平滑

**驗證方法**:
```bash
# 查看 Logcat FPS 數據
grep "FPS:" docs/test_results/v2.9.9-Day1-2/logcat_test.log

# 使用 ADB 監控效能
adb -s <DEVICE_ID> shell dumpsys gfxinfo com.akirachang.ai.photographer
```

**截圖要求**:
- 📸 FPS 顯示（如有）
- 📸 追蹤過程中畫面

**通過標準**:
- [ ] FPS ≥ 25（追蹤模式）
- [ ] 無明顯卡頓
- [ ] 記憶體使用正常

---

## 📊 測試報告模板

完成測試後，請填寫以下模板並保存至 `docs/test_results/v2.9.9-Day1-2/PIXEL9A_TEST_REPORT.md`：

```markdown
# Pixel 9a 實機測試報告 - v2.9.9 Day 1-2

## 測試環境
- 設備: Google Pixel 9a
- Android 版本: [填寫]
- 測試日期: [填寫]
- 測試人員: [填寫]

## 測試結果總覽
- 總測試場景: 7
- 通過場景: [填寫]
- 失敗場景: [填寫]
- 通過率: [填寫]%

## 詳細測試結果

### 場景 1: 人臉追蹤模式
- 狀態: ✅ 通過 / ❌ 失敗
- 備註: [填寫]

### 場景 2: 靜態對焦模式
- 狀態: ✅ 通過 / ❌ 失敗
- 備註: [填寫]

### 場景 3: AE/AF 鎖定
- 狀態: ✅ 通過 / ❌ 失敗
- 備註: [填寫]

### 場景 4: 解除鎖定
- 狀態: ✅ 通過 / ❌ 失敗
- 備註: [填寫]

### 場景 5: 追蹤目標離開畫面
- 狀態: ✅ 通過 / ❌ 失敗
- 備註: [填寫]

### 場景 6: 切換相機清除狀態
- 狀態: ✅ 通過 / ❌ 失敗
- 備註: [填寫]

### 場景 7: 效能測試
- 狀態: ✅ 通過 / ❌ 失敗
- FPS: [填寫] (目標 ≥ 25)
- 備註: [填寫]

## 問題與建議
[填寫發現的問題和改進建議]

## 附件
- Logcat: [logcat_test.log]
- 截圖: [screenshot_*.png]
- 測試照片: [test_photo_*.jpg]
```

## 🚀 測試完成後

1. **保存測試產出**:
   ```bash
   # 測試報告
   docs/test_results/v2.9.9-Day1-2/PIXEL9A_TEST_REPORT.md

   # Logcat 日誌
   docs/test_results/v2.9.9-Day1-2/logcat_test.log

   # 截圖
   docs/test_results/v2.9.9-Day1-2/screenshots/*.png

   # 測試照片 (如有需要)
   docs/test_results/v2.9.9-Day1-2/photos/*.jpg
   ```

2. **提交測試結果**:
   ```bash
   git add docs/test_results/v2.9.9-Day1-2/
   git commit -m "[Test] v2.9.9 Day 1-2: Pixel 9a 實機測試結果

   測試場景: 7/7 通過
   - 人臉追蹤模式: ✅
   - 靜態對焦模式: ✅
   - AE/AF 鎖定: ✅
   - 解除鎖定: ✅
   - 追蹤目標離開畫面: ✅
   - 切換相機清除狀態: ✅
   - 效能測試: ✅ (FPS: XX)

   Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

   git push github main
   ```

---

**準備完成，開始測試！** 🧪
