# v2.9.7 Day 1-2: GPS EXIF 模擬器穩定性測試計劃

**版本**: v2.9.7-Day1-2
**測試日期**: 2026-01-28
**測試環境**: Android Emulator
**測試目的**: 在實機測試前驗證 GPS EXIF 功能的代碼穩定性和邏輯正確性

---

## 📋 測試前準備

### 1. Gradle Sync

```bash
# 在 Android Studio 中執行 Gradle Sync
# 或使用命令行
./gradlew --refresh-dependencies
```

**預期結果**:
- ✅ 所有依賴正確下載
- ✅ `play-services-location:21.1.0` 成功引入
- ✅ 沒有編譯錯誤

**檢查點**:
- [ ] Gradle sync 成功
- [ ] Build output 無錯誤

---

### 2. 編譯和安裝

```bash
# 編譯 Debug 版本
./gradlew assembleEmulatorDebug

# 或直接從 Android Studio Run
# Run > Run 'app' (Shift+F10)
```

**預期結果**:
- ✅ APK 編譯成功
- ✅ 安裝到模擬器成功
- ✅ 應用可正常啟動

**檢查點**:
- [ ] Build 成功 (BUILD SUCCESSFUL)
- [ ] APK 安裝成功
- [ ] 應用圖標出現在模擬器桌面

---

## 🧪 測試場景

### 場景 1: 應用啟動測試

**目的**: 確保 Hilt 依賴注入正確，`OverlayPreferences` 注入無誤

**步驟**:
1. 啟動應用
2. 觀察 Logcat

**預期結果**:
```
✅ 應用正常啟動
✅ 相機預覽顯示
✅ 沒有 DI 注入錯誤
✅ Logcat 沒有 CameraRepositoryImpl 初始化錯誤
```

**Logcat 關鍵字過濾**:
```bash
# Android Studio Logcat 過濾
tag:CameraRepositoryImpl | tag:SettingsViewModel | tag:OverlayPreferences
```

**檢查點**:
- [ ] 應用啟動成功
- [ ] 相機預覽正常
- [ ] Logcat 無 `CameraRepositoryImpl` 初始化錯誤
- [ ] Logcat 無 `OverlayPreferences` 錯誤

**已知預期日誌**:
```
I/CameraRepositoryImpl: Camera bound to lifecycle via LiveCameraSource (real device)
或
I/CameraRepositoryImpl: LifecycleCameraController bound to lifecycle (emulator with StaticImageSource)
```

---

### 場景 2: 設置頁面 UI 測試

**目的**: 確認 GPS 開關 UI 正常顯示，沒有 Compose 錯誤

**步驟**:
1. 點擊右上角設置圖標
2. 滾動到底部
3. 確認 GPS 開關存在

**預期結果**:
```
✅ 設置頁面正常打開
✅ GPS 位置標記開關顯示
✅ 圖標為 LocationOn (📍)
✅ 描述文字正確：「將拍攝位置（經緯度、海拔）寫入照片 EXIF（需要位置權限，預設關閉保護隱私）」
✅ 開關預設為 OFF（關閉）
```

**檢查點**:
- [ ] 設置頁面打開成功
- [ ] 看到 6 個 Overlay 開關 + 1 個 Auto HDR + 1 個 GPS 標記 = 共 8 個開關
- [ ] GPS 開關在最後一個位置
- [ ] GPS 開關預設為關閉狀態
- [ ] 圖標和文字正確

**截圖位置**: `tmp/screenshots_temp/settings_gps_switch.png`

---

### 場景 3: GPS 開關切換測試

**目的**: 確認 DataStore 讀寫正常，ViewModel 響應正確

**步驟**:
1. 在設置頁面點擊 GPS 開關 → 開啟
2. 觀察 Logcat
3. 返回相機頁面
4. 再次進入設置頁面，確認開關狀態保持

**預期結果**:
```
✅ 開關可正常切換
✅ 狀態立即更新
✅ DataStore 寫入成功
✅ 返回後狀態保持（持久化成功）
```

**Logcat 關鍵字**:
```
OverlayPreferences
SettingsViewModel
```

**檢查點**:
- [ ] 開關可切換到 ON
- [ ] 開關可切換回 OFF
- [ ] 離開設置後再回來，狀態正確保持
- [ ] Logcat 無 DataStore 錯誤

---

### 場景 4: 拍照測試 - GPS 關閉狀態

**目的**: 確認 GPS 關閉時，拍照功能正常，不會因為 GPS 邏輯導致崩潰

**步驟**:
1. 確保 GPS 開關為 **OFF**
2. 返回相機頁面
3. 點擊快門拍照
4. 觀察 Logcat

**預期結果**:
```
✅ 拍照正常完成
✅ 照片保存成功
✅ Logcat 顯示 "GPS tagging disabled in settings"
✅ EXIF 只包含 Orientation，無 GPS 標籤
✅ 沒有 Location 相關錯誤
```

**Logcat 預期日誌**:
```
D/CameraRepositoryImpl: GPS tagging disabled in settings
D/CameraRepositoryImpl: Writing EXIF metadata: rotation=0 → exif=1, hasLocation=false
D/CameraRepositoryImpl: No GPS location available, skipping GPS EXIF tags
I/CameraRepositoryImpl: EXIF metadata saved successfully
```

**檢查點**:
- [ ] 拍照成功
- [ ] Logcat 顯示 "GPS tagging disabled in settings"
- [ ] Logcat 顯示 "hasLocation=false"
- [ ] Logcat 顯示 "EXIF metadata saved successfully"
- [ ] 應用無崩潰

**照片驗證**:
```bash
# 使用 adb 拉取照片
adb shell "ls -lt /sdcard/Pictures/SmartCameraApp | head -5"
adb pull /sdcard/Pictures/SmartCameraApp/[最新照片名稱] tmp/test_outputs/

# 檢查 EXIF (Windows)
# 如果有 exiftool，使用:
exiftool tmp/test_outputs/[照片名稱].jpg | findstr "GPS"
# 預期：無 GPS 相關標籤
```

---

### 場景 5: 拍照測試 - GPS 開啟但無信號

**目的**: 確認 GPS 開啟但無法獲取位置時，拍照功能正常，5秒 timeout 機制有效

**步驟**:
1. 進入設置，開啟 GPS 開關 → **ON**
2. **不授予位置權限**（或模擬器未開啟位置服務）
3. 返回相機頁面
4. 點擊快門拍照
5. 觀察 Logcat 和拍照時間

**預期結果**:
```
✅ 拍照正常完成（可能稍慢，最多 5 秒）
✅ Logcat 顯示 "Location permission not granted" 或 "Location not available within timeout"
✅ EXIF 只包含 Orientation，無 GPS 標籤
✅ 沒有崩潰或 ANR
```

**Logcat 預期日誌** (權限未授予):
```
D/CameraRepositoryImpl: Requesting current location with 5000ms timeout...
W/CameraRepositoryImpl: Location permission not granted
D/CameraRepositoryImpl: Writing EXIF metadata: rotation=0 → exif=1, hasLocation=false
I/CameraRepositoryImpl: EXIF metadata saved successfully
```

**Logcat 預期日誌** (權限已授予但無信號):
```
D/CameraRepositoryImpl: Requesting current location with 5000ms timeout...
W/CameraRepositoryImpl: Location not available within timeout
D/CameraRepositoryImpl: Writing EXIF metadata: rotation=0 → exif=1, hasLocation=false
I/CameraRepositoryImpl: EXIF metadata saved successfully
```

**檢查點**:
- [ ] 拍照成功（即使等待 5 秒）
- [ ] Logcat 顯示權限或 timeout 訊息
- [ ] 應用無崩潰
- [ ] 拍照時間 < 6 秒

---

### 場景 6: 拍照測試 - GPS 開啟且有模擬位置

**目的**: 確認 GPS 位置獲取和 EXIF 寫入邏輯正確

**步驟**:
1. 在模擬器中設置模擬位置：
   ```
   Extended Controls (⋮) > Location
   Latitude: 25.0330  (台北)
   Longitude: 121.5654
   Altitude: 50
   點擊 "Send"
   ```
2. 確保應用已授予位置權限
3. 確保 GPS 開關為 **ON**
4. 拍照
5. 觀察 Logcat

**預期結果**:
```
✅ 拍照正常完成
✅ Logcat 顯示 "Location obtained: lat=25.033, lon=121.5654, alt=50.0"
✅ Logcat 顯示 "GPS EXIF tags written"
✅ EXIF 包含 GPS 標籤
```

**Logcat 預期日誌**:
```
D/CameraRepositoryImpl: Requesting current location with 5000ms timeout...
I/CameraRepositoryImpl: Location obtained: lat=25.033, lon=121.5654, alt=50.0
D/CameraRepositoryImpl: Writing EXIF metadata: rotation=0 → exif=1, hasLocation=true
D/CameraRepositoryImpl: GPS Altitude set: 50.0m (ref=0)
I/CameraRepositoryImpl: GPS EXIF tags written: lat=25.033, lon=121.5654, alt=50.0
I/CameraRepositoryImpl: EXIF metadata saved successfully
```

**檢查點**:
- [ ] 拍照成功
- [ ] Logcat 顯示 "Location obtained"
- [ ] Logcat 顯示正確的經緯度和海拔
- [ ] Logcat 顯示 "GPS EXIF tags written"
- [ ] Logcat 顯示 "hasLocation=true"

**照片驗證** (如有 exiftool):
```bash
exiftool tmp/test_outputs/[照片名稱].jpg | findstr "GPS"

# 預期輸出：
# GPS Latitude                    : 25 deg 1' 58.80" N
# GPS Longitude                   : 121 deg 33' 55.44" E
# GPS Altitude                    : 50 m Above Sea Level
# GPS Date/Time                   : [當前日期時間]
```

---

## 📊 測試總結

### 必須通過的檢查項

#### 編譯和啟動 (Critical)
- [ ] Gradle Sync 成功
- [ ] APK 編譯成功
- [ ] 應用啟動無崩潰
- [ ] Hilt DI 注入正常

#### UI 功能 (Critical)
- [ ] 設置頁面顯示 GPS 開關
- [ ] GPS 開關可正常切換
- [ ] 開關狀態持久化正確

#### 拍照功能 (Critical)
- [ ] GPS 關閉時拍照正常
- [ ] GPS 開啟但無權限時拍照正常
- [ ] GPS 開啟但無信號時拍照正常（< 6秒）
- [ ] GPS 開啟且有位置時拍照正常

#### 錯誤處理 (Critical)
- [ ] 所有錯誤情況都有 Logcat 記錄
- [ ] 所有錯誤都是 non-fatal（不影響拍照）
- [ ] 無 SecurityException 未捕獲
- [ ] 無 ANR (Application Not Responding)

---

## 🐛 已知限制 (模擬器)

1. **模擬位置可能不穩定**
   - 模擬器的 FusedLocationProviderClient 行為可能與實機不同
   - 可能需要多次 "Send" 模擬位置

2. **權限請求**
   - 模擬器上需要手動到設置授予位置權限
   - 或使用 ADB 命令：
     ```bash
     adb shell pm grant com.akirachang.ai.photographer android.permission.ACCESS_FINE_LOCATION
     ```

3. **GPS 時間戳記**
   - 模擬位置可能沒有正確的 GPS 時間戳記
   - ExifInterface.setGpsInfo() 會自動處理

---

## ✅ 測試通過標準

**所有以下條件必須滿足**:

1. ✅ 應用啟動無崩潰
2. ✅ GPS 開關 UI 正常顯示
3. ✅ GPS 關閉時拍照正常（< 2秒）
4. ✅ GPS 開啟但無信號時拍照正常（< 6秒）
5. ✅ 所有錯誤都有 Logcat 記錄
6. ✅ 沒有未捕獲異常
7. ✅ Logcat 日誌邏輯正確

**若全部通過** → ✅ 可進行實機測試
**若有失敗** → ⚠️ 需修正後再測試

---

## 📝 測試記錄

**測試人員**: _____________
**測試日期**: _____________
**模擬器版本**: _____________
**Android 版本**: _____________

**測試結果**:
- [ ] PASS - 全部通過
- [ ] FAIL - 有失敗項目

**失敗項目** (如有):
```
[記錄失敗場景和錯誤訊息]
```

**Logcat 摘要**:
```
[貼上關鍵 Logcat 日誌]
```

---

## 🔗 相關文檔

- [Roadmap 2.4](plans/02.4-smart_camera_app_roadmap.md#day-1-2-gps-exif-寫入--high)
- [CameraRepositoryImpl.kt](app/src/main/java/com/akirachang/ai/photographer/data/repository/CameraRepositoryImpl.kt)
- [OverlayPreferences.kt](app/src/main/java/com/akirachang/ai/photographer/data/preferences/OverlayPreferences.kt)
- [SettingsScreen.kt](app/src/main/java/com/akirachang/ai/photographer/presentation/settings/SettingsScreen.kt)
