# v2.9.6 Phase 2 對話交接指南

**交接時間**: 2026-01-25 19:45
**前一對話 Token 使用**: 103,794 / 200,000 (51.9%)
**Phase 1 狀態**: ✅ 完全完成並提交

---

## Phase 1 完成成果

### Git Commits
```bash
dab596f [Fix] v2.9.6 Phase 1 - Fix EXIF Orientation using ImageProxy rotation
d006ef6 [Feature] v2.9.6 Phase 1 - Add EXIF Orientation support
```

### 測試結果
- ✅ 後鏡頭：Portrait/Landscape Right/Landscape Left 全部正確
- ✅ 前鏡頭：Portrait/Landscape Right/Landscape Left 全部正確
- ✅ 模擬器：StaticImageSource EXIF 寫入正常
- ✅ 效能：拍照延遲 < 100ms，EXIF 寫入 ~40ms

### 關鍵修復
**問題**: Portrait 拍照後照片在相簿向左旋轉 90°

**解決**: 使用 `ImageProxy.imageInfo.rotationDegrees` 而非 OrientationEventListener
- ImageProxy rotation 已包含感光元件方向補償
- Portrait: 90° → ROTATION_90 → EXIF=6 (Rotate 90° CW) → 正確顯示

---

## Phase 2 任務範圍

### 主要目標
修復預覽雙重旋轉問題（橫向模式時預覽可能過度旋轉）

### 實作計畫
參考：[docs/plan/v2.9.6-Phase1-EXIF-Preview-Rotation-Fix.md](../plan/v2.9.6-Phase1-EXIF-Preview-Rotation-Fix.md)

**步驟 2.1: 新增診斷 Log**
- 目標：不改變行為，僅收集數據
- 位置：LiveCameraSource.kt line 288-294
- 內容：記錄 `currentRotation`, `rotationDegrees`, frame 尺寸

```kotlin
val rotationDegrees = imageProxy.imageInfo.rotationDegrees

// v2.9.6 Phase 2: Diagnostic log for preview rotation issue
if (frameCount % 30 == 0L) {
    Log.d(TAG, "[Rotation Debug] currentRotation=$currentRotation, " +
        "rotationDegrees=$rotationDegrees, " +
        "isFront=$isFrontCamera, " +
        "frame=${rawBuffer.bitmap.width}x${rawBuffer.bitmap.height}")
}
```

**步驟 2.2: 收集診斷數據**
- 在 Pixel 9a 上測試：Portrait → Landscape Right → Landscape Left
- 記錄每個方向的 `rotationDegrees` 值
- 觀察預覽是否過度旋轉

**步驟 2.3: 修正 transformBitmap 邏輯**
- 根據診斷數據決定修復策略
- 可能方案：
  1. 使用固定 rotation = 90° (強制 Portrait UI 基準)
  2. 使用 currentRotation 偏移補償公式
  3. 分離 Preview 與 Analysis 的 bitmap 轉換

---

## 關鍵檔案路徑

### 實作計畫
- `docs/plan/v2.9.6-Phase1-EXIF-Preview-Rotation-Fix.md` - 完整計畫（Phase 1 + Phase 2）

### Phase 1 測試報告
- `docs/test_results/v2.9.6-Phase1/EXIF_Test_Report_Final.md` - 最終測試報告

### 核心程式碼
- `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
  - Line 288-300: transformBitmap() 呼叫點（需診斷）
  - Line 212: currentRotation 定義
  - Line 549-570: OrientationEventListener 邏輯

### EXIF 實作
- `app/src/main/java/com/akirachang/ai/photographer/data/repository/CameraRepositoryImpl.kt`
  - Line 170-177: getCurrentRotation() 呼叫
  - Line 237-254: writeExifOrientation() 實作

---

## 重要提醒

### Phase 1 與 Phase 2 必須分離
- ✅ Phase 1 已提交兩個獨立 commits
- ⚠️ Phase 2 必須是獨立 commit/PR
- 原因：允許獨立回滾

### 預覽修復風險
**中等風險**: 可能破壞 AI Overlay 對齊
- AI 功能依賴 ImageAnalysis 的 bitmap
- 修改 `transformBitmap()` 可能影響座標映射
- **必須測試**: 啟用 AI 輔助線，檢查對齊

### 測試要求
- Portrait/Landscape 預覽方向正確
- AI Overlay 與預覽對齊
- 拍照與預覽方向一致
- 旋轉時 Live FPS ≥ 25

---

## 新對話啟動建議

### 第一條訊息範例

```
繼續 v2.9.6 Phase 2 - 預覽旋轉修復

Phase 1 已完成：
- ✅ EXIF Orientation 實作並測試通過
- ✅ 所有方向照片正確顯示（前後鏡頭）
- ✅ Commits: dab596f, d006ef6

Phase 2 目標：
修復預覽雙重旋轉問題（橫向模式）

參考文檔：
- @docs/plan/v2.9.6-Phase1-EXIF-Preview-Rotation-Fix.md (完整計畫)
- @docs/test_results/v2.9.6-Phase1/EXIF_Test_Report_Final.md (Phase 1 測試報告)
- @docs/handoff/Phase2_Handoff_Instructions.md (本交接文檔)

請先執行步驟 2.1：新增診斷 log 到 LiveCameraSource.kt
```

### 建議工作流程
1. **診斷優先**: 先添加 log，收集數據，再決定修復方案
2. **小步提交**: 診斷 log 獨立提交，修復邏輯獨立提交
3. **完整測試**: 每次修改都要測試 Portrait/Landscape + AI Overlay

---

## 已知限制與待辦

### 不在 Phase 2 範圍
- ❌ Upside Down (180°) 模式支援（罕見場景）
- ❌ 跨平台 EXIF 驗證（Windows/macOS）
- ❌ 第三方應用測試（LINE/Instagram）

### 可選改進（Phase 3 或後續）
- 單元測試：`writeExifOrientation()` 測試
- Integration Test：完整拍照→EXIF 流程測試
- 效能 Profiling：不同裝置的 EXIF 寫入時間

---

## 緊急回滾計畫

### 如果 Phase 2 失敗
```bash
# 回滾 Phase 2 commit（保留 Phase 1）
git revert <phase2-commit-sha>

# 或完全回到 Phase 1
git reset --hard dab596f
```

### 如果 Phase 1 EXIF 有問題
```bash
# 回滾兩個 commits（回到 Phase 1 之前）
git reset --hard d006ef6~1
```

---

## 技術備註

### ImageProxy Rotation 對應表（Pixel 9a 後鏡頭）
| 手機方向 | ImageProxy Rotation | Surface.ROTATION_* | EXIF Orientation |
|---------|-------------------|-------------------|-----------------|
| Portrait | 90° | ROTATION_90 | 6 (90° CW) |
| Landscape R | 0° | ROTATION_0 | 1 (Normal) |
| Landscape L | 180° | ROTATION_180 | 3 (180°) |

### 前鏡頭差異
- Portrait: EXIF=8 (90° CCW) 而非 6
- 鏡像處理在 `transformBitmap()` 中（不影響 EXIF）

---

**交接文檔版本**: 1.0
**準備狀態**: ✅ 可開啟新對話
**優先度**: 高（預覽體驗影響使用者）
