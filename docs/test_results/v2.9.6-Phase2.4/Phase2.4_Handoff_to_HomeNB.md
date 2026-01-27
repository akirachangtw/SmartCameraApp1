# v2.9.6 Phase 2.4 - Work NB → Home NB 交接文件

**交接日期**: 2026-01-27
**交接來源**: Work NB (Claude Code)
**交接對象**: Home NB

---

## 一、Phase 2.4 實體裝置測試結果

### 測試裝置與結果

| Device | Android | Page Size | pageSizeCompat | PageSizeDialog | APP 功能 | Result |
|--------|---------|-----------|----------------|----------------|---------|--------|
| Pixel 9a | 16 (API 36) | 4096 (4KB) | ✅ enabled | ❌ 仍出現 | ✅ 正常 | **FAIL** |
| Sony XQ-BT52 | 12 (API 31) | 4096 (4KB) | ✅ enabled | ✅ 無此檢查 | ✅ 正常 | **PASS** |

### 核心發現

**`android:pageSizeCompat="enabled"` 在實體 Pixel 9a 上未能消除 PageSizeMismatchDialog。**

### 根因分析：模擬器 PASS / 實機 FAIL 的原因

| | 模擬器 Pixel9a-16KB | 實體 Pixel 9a |
|---|---|---|
| Architecture | **x86_64** | **arm64-v8a** |
| PAGE_SIZE | **16384** (真正 16KB) | **4096** (預設 4KB) |
| 16KB 環境 | 出廠即 16KB image | 需**解鎖 Bootloader**（清除全部資料）才能切換 |
| 檢查的 `.so` | `lib/x86_64/` | `lib/arm64-v8a/` |
| pageSizeCompat | ✅ 有效 | ❌ 無效 |

**結論**：
1. 模擬器是真正 16KB 環境，pageSizeCompat 正確運作
2. 實體 Pixel 9a 仍在 4KB 模式，Android 16 顯示的是「前瞻性相容性警告」
3. pageSizeCompat 是給真正 16KB 設備用的，對 4KB 設備前瞻性警告無效
4. 兩者使用完全不同的 CPU 架構（x86_64 vs arm64-v8a），二進位檔案不同

---

## 二、PageSizeMismatchDialog 問題函式庫分析

### 8 個問題函式庫（來自實機截圖 `Pixel9a/16kb-warning.JPG`）

| # | 函式庫 | 對齊狀態 | 來源 | 必要性 | 備註 |
|---|--------|---------|------|--------|------|
| 1 | `libc++_shared.so` | 不明錯誤 | C++ Runtime | **必要** | OpenCV 依賴 |
| 2 | `libsurface_util_jni.so` | 不明錯誤 | CameraX 1.4.1 | **必要** | 相機 Surface |
| 3 | `libimage_processing_util_jni.so` | 不明錯誤 | CameraX 1.4.1 | **必要** | 影像處理 |
| 4 | `libdatastore_shared_counter.so` | 不明錯誤 | DataStore 1.1.1 | **非必要** | APP 僅用 Preferences API |
| 5 | `libopencv_java4.so` | 不明 | OpenCV 4.13.0 | **必要** | 構圖線偵測核心 |
| 6 | `libandroidx.graphics.path.so` | 不明錯誤 | AndroidX Graphics | **非必要** | APP 未直接使用 |
| 7 | `face_detector_v2_jni.so` | **LOAD 區段沒有對齊** | ML Kit 16.1.5 | **必要** | 人臉偵測核心 |
| 8 | `libmlkitcommonpipeline.so` | **LOAD 區段沒有對齊** | ML Kit 17.0.1 | **必要** | ML Kit 推論管線 |

**小結**：6 個必要 / 2 個非必要。移除非必要函式庫不能解決問題，因為必要函式庫仍有對齊問題。

---

## 三、Google Play 發布影響 — 重要！

### 政策時程

| 時間點 | 政策 | 狀態 |
|--------|------|------|
| **2025/11/01** | 所有新 APP/更新（target Android 15+）必須支援 16KB | **已生效** |
| **2026/05/31** | 延期申請截止日（一次性寬限） | ⏳ 倒數中 |

**影響**：若 APP target API 35+，原生函式庫必須 16KB 對齊，否則 Google Play **會拒絕提交**。

### 上游修復狀態（關鍵！）

| 函式庫 | 16KB 修復版本 | 目前使用版本 | 狀態 | 參考 |
|--------|-------------|------------|------|------|
| **OpenCV** | 4.12.0+ (2025/07) | **4.13.0** | ✅ arm64-v8a 已修復 | [Issue #27024](https://github.com/opencv/opencv/issues/27024) |
| **CameraX** | **1.4.2+** | **1.4.1** | ⚠️ **需升級** | [ML Kit Issue #987](https://github.com/googlesamples/mlkit/issues/987) |
| **ML Kit** | 由 CameraX 1.4.2+ 間接修復 | 16.1.5 / 17.0.1 | ⚠️ 依賴 CameraX 更新 | 同上 |

### 最關鍵修復：升級 CameraX 1.4.1 → 1.4.2+

根據 [ML Kit GitHub Issue #987](https://github.com/googlesamples/mlkit/issues/987)（已關閉，COMPLETED）：

> ML Kit 本身已 16KB 對齊，問題出在 **CameraX 的 transitive dependency**。
> **解法：升級 CameraX 至 1.4.2+**，會覆蓋舊版未對齊的原生函式庫。

修改內容：
```toml
# gradle/libs.versions.toml
camerax = "1.4.2"   # 目前: 1.4.1
```

預期修復的函式庫：
- `libsurface_util_jni.so` → 16KB 對齊
- `libimage_processing_util_jni.so` → 16KB 對齊
- `face_detector_v2_jni.so` → 被 CameraX 1.4.2 覆蓋為對齊版本
- `libmlkitcommonpipeline.so` → 被 CameraX 1.4.2 覆蓋為對齊版本

---

## 四、效能數據（附帶收集）

### Pixel 9a (Android 16)

| 指標 | 數據 |
|------|------|
| FPS | 18.8 → **28.7** (穩定) |
| 丟幀率 | **0.0%** |
| 記憶體 | Native=83MB, Total=370-446MB |
| 熱節流 | NONE |
| 解析度 | 1280x720 |

### Sony XQ-BT52 (Android 12)

| 指標 | 數據 |
|------|------|
| FPS | **29.2 → 30.0** |
| 丟幀率 | **0.0%** |
| 記憶體 | Native=58-69MB, Total=269-292MB |
| 熱節流 | NONE |

---

## 五、測試證據檔案清單

```
docs/test_results/v2.9.6-Phase2.4/
├── Physical_Devices_Test_Report.md          # 完整實機測試報告 (v1.2)
├── Emulator_Stability_Test_Report.md        # 模擬器測試報告
├── Work_NB_Test_Guide.md                    # 測試指南
├── pixel9a_physical_phase2.4_startup.txt    # Pixel 9a logcat
├── sony_physical_phase2.4_startup.txt       # Sony logcat
├── Phase2.4_Handoff_to_HomeNB.md            # 本交接文件
└── Pixel9a/
    ├── 16kb-warning.JPG                     # PageSizeMismatchDialog 截圖
    └── Enable-16KB-Support.png              # 16KB 需解鎖 Bootloader 截圖
```

---

## 六、Home NB 建議行動

### 高優先：Phase 2.5 — CameraX 升級與 16KB 對齊修復

1. **升級 CameraX 1.4.1 → 1.4.2+**
   - 修改 `gradle/libs.versions.toml`: `camerax = "1.4.2"`
   - 重新 build APK
   - 用模擬器 Pixel9a-16KB 驗證 PageSizeMismatchDialog 是否消除

2. **驗證 `.so` 16KB 對齊**
   - 用 Android Studio Lint 或 `readelf` 檢查所有 arm64-v8a `.so`
   - 確認 ELF LOAD segment alignment ≥ 16KB (0x4000)

3. **重新在實體 Pixel 9a 測試**（如有機會）
   - 確認 PageSizeMismatchDialog 是否消除
   - 如果消除 → Google Play 提交合規

### 低優先：保留項目

- `pageSizeCompat="enabled"` → 建議保留（零成本，真正 16KB 設備下有效）
- EXIF Orientation 修復 → 後續 Phase 處理
- 解鎖 Bootloader 測試 16KB → 不建議（風險過高）

---

## 七、參考資源

- [Google Play 16KB Page Size Blog](https://android-developers.googleblog.com/2025/05/prepare-play-apps-for-devices-with-16kb-page-size.html)
- [ML Kit 16KB Issue #987](https://github.com/googlesamples/mlkit/issues/987) — 已關閉，解法：CameraX 1.4.2+
- [OpenCV 16KB Issue #27024](https://github.com/opencv/opencv/issues/27024) — 已關閉，4.12.0+ 修復
- [Google Play 16KB Extension Policy](https://support.google.com/googleplay/android-developer/thread/369751886)
- [Android 16KB Page Size Guide](https://developer.android.com/guide/practices/page-sizes)
- [Bootloader Unlock Guide](https://source.android.com/docs/core/architecture/bootloader/locking_unlocking)

---

**交接文件版本**: 1.0
**建立日期**: 2026-01-27
