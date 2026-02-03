# Work NB → Home NB 工作交接文檔

**交接日期**: 2026-02-03
**交接人**: Work NB (Pixel 9a 實機測試)
**接手人**: Home NB (後續開發)
**分支**: `feature/optical-flow-phase5-6`

---

## 📋 交接摘要

### ✅ 已完成

1. **Phase 7 核心修正** (4 項)
   - A: CameraViewModel OPT 高幀率訂閱 (移除 `.sample(100)` 瓶頸)
   - B: OpticalFlowTracker 動態 aspect-ratio-preserving 下降解析度
   - C: OpticalFlowConfig LK 參數調優 (winSize, maxLevel)
   - D: CoordinateMapper CENTER_CROP-aware 座標映射

2. **場景 1 測試驗證** ✓
   - 234 幀追蹤，status=0 失敗次數 = 0
   - Round-trip 座標誤差 = 0
   - LK quality 平均 0.914（優秀）

3. **EMA 移除** ✓
   - 移除 SMOOTHING_ALPHA=0.35 平滑邏輯
   - 直接使用原始 LK 座標 → 零延遲追蹤
   - 使用者確認: 不再出現速度相關的偏移變大現象

4. **文檔完善** ✓
   - `docs/issues/preview_zoom_aspect_ratio.md` — 預覽 1.6x 放大問題分析
   - `docs/issues/front_camera_mlkit_mirror.md` — 前置鏡頭 ML Kit 鏡像座標問題

---

### 🔴 未解決

1. **LK 累積漂移** — **關鍵問題**
   - 使用者報告: 10:1 漂移比 vs Pixel 9a 內建相機
   - Pixel 9a 內建相機「像有膠水黏住」，偏移度極小
   - 模板匹配漂移修正（`correctDrift()`）已實作但**無效果**
   - **根因未確認**: 需分析 logcat 了解模板匹配是否執行、quality 值多少
   - **建議方案**: 多點 LK 追蹤（GoodFeaturesToTrack + 多點 LK + 中位數位置）

2. **PreviewView 預覽 1.6x 放大**
   - 詳見 `docs/issues/preview_zoom_aspect_ratio.md`
   - 推薦方案: A（Preview 獨立 ResolutionSelector）
   - 預期能將等效放大倍數從 1.58x 降至 1.18x

3. **前置鏡頭 ML Kit 座標鏡像**
   - 詳見 `docs/issues/front_camera_mlkit_mirror.md`
   - 場景 7 自拍時，點擊座標 A vs 對焦圓圈座標 B 出現 X 方向偏移
   - 需在 `findTappedTarget()` 和 `updateTrackedObjectPosition()` 加入前置鏡頭鏡像修正

---

### 📊 場景測試進度

| 場景 | 項目 | 結果 | 備註 |
|------|------|------|------|
| 1 | 基本 LK 追蹤 | ✅ PASS | 234 幀、quality 0.914 |
| 2 | Re-tap 快速切換 | ✅ 正常 | 使用者確認 |
| 3 | 無紋理區域 | ⚠️ 預期失敗 | LK 無法追蹤無特徵區域，正常 |
| 4 | ML Kit 優先 | ❓ 未測 | 需補充測試 |
| 5 | AE/AF Lock | ✅ 正常 | 使用者確認 |
| 6 | 效能監測 | ✅ 正常 | 不掉幀，memory 穩定 |
| 7 | 前置鏡頭自拍 | ⚠️ 有座標偏移 | 前置鏡頭 ML Kit 鏡像問題 |

---

## 🔧 代碼修改清單

### 修改的檔案

1. **`app/src/main/java/.../presentation/camera/CameraViewModel.kt`**
   - 移除 EMA 相關變數（SMOOTHING_ALPHA, smoothedPointX, smoothedPointY 等）
   - 移除 `isFirstTrackingFrame` 標誌
   - 光學流動回調直接使用原始 LK 座標

2. **`app/src/main/java/.../data/opencv/OpticalFlowTracker.kt`**
   - 新增: 模板匹配漂移修正（`saveTemplate()`, `correctDrift()`, `releaseTemplate()`）
   - 常數: `TEMPLATE_HALF_SIZE=32`, `SEARCH_RADIUS=48`, `CORRECTION_INTERVAL=10`, `MIN_MATCH_QUALITY=0.7f`
   - 狀態: `templatePatch: Mat?`, `frameCount: Int`

### 新增的文檔

- `docs/issues/preview_zoom_aspect_ratio.md`
- `docs/issues/front_camera_mlkit_mirror.md`
- `docs/test_results/v2.x.x-OpticalFlow/PIXEL9A_TEST_REPORT.md`
- `docs/test_results/v2.x.x-OpticalFlow/logcat_phase7_centercrop.log`

---

## 🎯 Home NB 後續任務優先級

### 🔴 HIGH — 立即著手

1. **調查模板匹配漂移修正為何無效**
   - 分析 logcat: 模板是否成功保存、correctDrift() 是否執行、quality 值
   - 若模板匹配確實失效 → 轉向多點 LK 追蹤方案

2. **實現多點 LK 追蹤**（若單點 LK 無法達到要求精度）
   - 在 OpticalFlowTracker 中新增:
     - `GoodFeaturesToTrack` 偵測 tap 點周圍的角點
     - 用 `calcOpticalFlowPyrLK` 同時追蹤多個點
     - 濾除異常點（outlier），取中位數位置作為最終座標
   - 預期能顯著降低單點漂移

### 🟡 MEDIUM — 開發優化

3. **修正預覽 1.6x 放大** (`preview_zoom_aspect_ratio.md`)
   - 建議方案 A: Preview 獨立 ResolutionSelector（預期 1.6x → 1.18x）
   - 修改 `LiveCameraSource.kt` 的 Preview UseCase 配置
   - 驗證 logcat 的解析度協商結果

4. **修正前置鏡頭 ML Kit 座標鏡像** (`front_camera_mlkit_mirror.md`)
   - 在 `CameraViewModel.findTappedTarget()` 和 `updateTrackedObjectPosition()` 加入前置鏡頭鏡像映射
   - 驗證場景 7 自拍座標精度

### 🟢 LOW — 後續工作

5. **補充場景 4, 6 測試** (若有時間)
   - 場景 4: ML Kit 優先邏輯驗證
   - 場景 6: 效能監測（已驗證無掉幀，但可進一步分析 FPS 穩定性）

---

## 📝 關鍵數據與日誌

### 座標映射參考
```
Pixel 9a 測試環境:
- 屏幕解析度: 1080 × 2272 (9:19.03)
- Image 實際輸出: 960 × 1280 (3:4)  [downsample=480×640, scale=0.5]
- Raw ImageProxy: 1280 × 960 (4:3)  [landscape]
- CENTER_CROP uniformScale: 1.7258
- centerCrop offset: cropOffX=167.10, cropOffY=0
- Round-trip 誤差: 0 (完美)
```

### 性能監測結果
```
LK Tracking Quality: avg 0.914 (excellent)
Frame Success Rate: 234/234 (100%)
Dropped Frames: 0
Memory Stable: ✓
FPS Stable: ✓ (28 FPS optical flow)
```

### Logcat 位置
- `docs/test_results/v2.x.x-OpticalFlow/logcat_phase7_centercrop.log` (1665 行)
- 包含完整的場景 1 追蹤日誌

---

## 🔗 相關參考

### 使用者反饋
- 漂移觀察: "移動動作稍微大點, 其定點偏移度就變大" → **已修正**（移除 EMA）
- 漂移對比: "10:1 偏移比 vs Pixel 9a" → **未解決**（需多點 LK 或其他方案）
- 預覽放大: "APP預覽畫面像放大1.4-1.5倍" → **已文檔化**（預期方案 A 可解決）
- 前置鏡頭: "點擊定點A, 實際對焦圓圈定點B" → **已文檔化**（需鏡像修正）

### 代碼位置參考
- `app/src/main/java/.../presentation/camera/CameraViewModel.kt` — 光學流動入口、ML Kit 追蹤整合
- `app/src/main/java/.../data/opencv/OpticalFlowTracker.kt` — 核心 LK 追蹤 + 漂移修正（實驗性）
- `app/src/main/java/.../util/CoordinateMapper.kt` — CENTER_CROP 座標映射（已驗證正確）
- `app/src/main/java/.../data/source/camera/LiveCameraSource.kt:1428` — ResolutionSelector 配置

---

## ✔️ 交接檢查清單

- [x] 所有代碼修改已提交（EMA 移除、模板匹配實作）
- [x] 場景 1 測試報告完成
- [x] 關鍵問題已文檔化
- [x] Logcat 日誌已保存
- [x] 後續任務優先級已列出
- [x] 代碼位置參考已備註

**交接狀態**: ✅ **完成**
