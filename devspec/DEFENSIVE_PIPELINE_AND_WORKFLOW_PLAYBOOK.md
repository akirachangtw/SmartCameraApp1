# Defensive Pipeline & Workflow Playbook

> **定位**: 本文件為 v2.12.0 防禦型相機管線實戰的通用知識萃取。
> 融合來源：迭代記憶 (v2.12.0-25~27) + MERGE_SOP + Ultimate Defensive Hybrid Workflow Guide
>
> **用途**: 新對話、新功能開始前的「戰前簡報」，不重複已有規範，只記錄從失敗中燒出來的實戰守則。

---

## 1. 幾何管線鐵律（v2.12.0-26 燒出來）

### 守則 A：無效幾何 → `null`，禁止偽裝者 Identity Matrix

```
無效輸入（imgW/H ≤ 0、contentBounds ≤ 0）
        ↓ 正確
  return null
        ↓ 上游收到 null
  latestSensorToViewMatrix = null
        ↓
  FIT_CENTER fallback 自動接管

                    ❌ 錯誤
            return Matrix()   ← 偽裝成合法但幾何全錯
```

**實作位置**: `PreviewViewGeometryMapper.createSensorToViewMatrix()` 開頭 guard clause
**致命症狀**: 對焦框在畫面角落亂跑（「點 A 畫 B」）

### 守則 B：FIT_CENTER 等比例縮放公式（Ticket 2 最終版）

```kotlin
val scale = minOf(contentBounds.width() / imgW, contentBounds.height() / imgH)
val offsetX = (contentBounds.width()  - imgW * scale) / 2f + contentBounds.left
val offsetY = (contentBounds.height() - imgH * scale) / 2f + contentBounds.top

// 後鏡頭
matrix.postScale(scale, scale)
matrix.postTranslate(offsetX, offsetY)

// 前鏡頭（水平鏡像）
matrix.postScale(-scale, scale)
matrix.postTranslate(contentBounds.right - offsetX + contentBounds.left, offsetY)
```

> ❌ 禁止：`scaleX = cW/imgW, scaleY = cH/imgH`（非等比例 → AR 不同時座標漂移）

### 守則 C：buildNewPipelineGeometryContext() 必須 Early-Return Null

```kotlin
fun buildNewPipelineGeometryContext(state: CameraState): GeometryContext? {
    val w = state.effectiveImageWidth
    val h = state.effectiveImageHeight
    if (w <= 0 || h <= 0) {
        Log.e(TAG, "[CoordSystem] Invalid img dims ${w}x${h} — returning null")
        return null  // ← 絕對不可繼續往下建構
    }
    // ...
}
```

**呼叫端必須**: `val ctx = buildNewPipelineGeometryContext(state) ?: return`

---

## 2. 分析管線活性守則（SP-A，v2.12.0-26）

### 守則：心跳 Log 是強制的，不是可選的

```kotlin
// observePreviewForAnalysis() → onEach 開頭
if (mlkitProcessedFrames % 30L == 0L) {
    Log.i("SmartCameraAnalyzer", "[SP-A Heartbeat] bitmap analysis alive frame=$mlkitProcessedFrames")
}
mlkitProcessedFrames++
```

**診斷規則**：
- `[SP-A Heartbeat]` 每 2~3 秒出現一次 → 管線存活 ✅
- 心跳消失 → 管線靜默死亡，優先排查：相機權限（重裝後需重新授權）

### 管線崩潰的常見殺手

| 原因 | 症狀 | 防禦 |
|------|------|------|
| `Matrix()` NaN/Infinity | `ImageAnalysis` Coroutine 靜默結束 | SP-A Guard + `return null` |
| `ImageProxy` 未 close | 後續幀卡住 | `use {}` 包覆 |
| Flow 未 catch | 一次 Exception 殺死整個 Flow | `.catch { e -> Log.e(...) }` |

---

## 3. AF 追蹤防禦守則（SP-C，v2.12.0-27）

### 核心問題：自我取消迴圈 (Self-Cancellation Loop)

```
Frame N  → runNewFocusPipeline() → focusControllerJob.cancel() → 發 HAL 請求
Frame N+1→ runNewFocusPipeline() → focusControllerJob.cancel() → 打斷 Frame N 的 HAL
Frame N+2→ ...
```

**結果**：`StandaloneCoroutine was cancelled` 洗版，Sony HAL 從未完成對焦。

### SP-C In-Flight 鎖定模式

```kotlin
fun runNewFocusPipeline(userIntent: FocusUserIntent, ...) {
    // Step G: In-Flight 鎖定防禦
    val isJobActive = focusControllerJob?.isActive == true
    val isUserTap = userIntent.isTapToFocusActive

    if (isJobActive && !isUserTap) {
        Log.v(TAG, "[SP-C] Skipping AF request: previous job is still in-flight")
        return  // ← 讓上一筆 HAL 跑完，不 cancel
    }

    // 只有 in-flight=false 或 user 明確 tap 才允許 cancel
    focusControllerJob?.cancel()
    focusControllerJob = viewModelScope.launch { ... }
}
```

### CancellationException 黃金原則

```kotlin
} catch (e: CancellationException) {
    // 靜默結束，不視為 failure，不呼叫 onFailure()，不觸發 backoff
    throw e  // 必須 re-throw，讓 Coroutine 機制正常運作
} catch (e: Exception) {
    Log.e(TAG, "AF focus error", e)
    onFailure()
}
```

---

## 4. 實機診斷速查（Sony XQ-BT52 適用）

### 開機後 30 秒內必須看到的 Log

```
# 心跳（管線存活確認）
adb -s HQ616R2713 logcat SmartCameraAnalyzer:I *:S | grep "Heartbeat"
→ 預期: [SP-A Heartbeat] bitmap analysis alive frame=30

# 幾何確認（點擊畫面後）
adb -s HQ616R2713 logcat CameraViewModel:I *:S | grep "CoordSystem"
→ 預期: [CoordSystem] imageProxy=480x640 preview=1080x2415 contentBounds=[...]

# SP-C 確認（被動追蹤時）
adb -s HQ616R2713 logcat CameraViewModel:V *:S | grep "SP-C"
→ 預期: [SP-C] Skipping AF request: previous job is still in-flight
```

### Sony 特有 HAL 特性

| 特性 | Pixel 9a | Sony XQ-BT52 |
|------|----------|--------------|
| `imageProxy` 方向 | rot=90°，需 swap W/H | rot=0°，已是直立 |
| 最小 YUV 解析度 | 支援 640×360 | 無 640×360，最小 1280×720 |
| AF 馬達速度 | 快 | 慢（需 SP-C 保護） |

---

## 5. Git 分批提交紀律（本次實戰確立）

```
Phase 1: 程式碼修復 commit（只含 .kt 檔）
  git add <修改的Kotlin檔>
  git commit -m "[Fix] v2.12.0-XX: <簡述>"

Phase 2: 文件與證據 commit（.md + .log）
  git add devspec/*.md docs/test_results/.../*.log
  git commit -m "[Docs] v2.12.0-XX: <簡述>"
```

**原則**：絕不混入未授權的檔案。每次 commit 前執行 `git status --short` 確認 Scope。

---

## 6. 開發 → 驗收 → 合併 標準流程

```
實作完成
  → /qa  (assembleDebug — 確認編譯)
  → /sv  (殘骸掃描 + Mock 偵測 + UI 互斥)
  → 分批 git commit (Code / Docs 分離)
  → 實機驗收
      └─ 收集 Heartbeat / CoordSystem log
      └─ 觸發 tap-to-focus，觀察 SP-C 鎖定
  → 若 PASS: 更新 BRANCH_RELEASES.md + HANDOVER_SUMMARY.md
  → 若 FAIL: logcat → Traycer trigger-workflow 重啟 PDCA 閉環
      ⚠️ 禁止直接搶修，必須重走 PDCA
```

### 合併前強制執行（出自 MERGE_SOP_QUICK_REFERENCE.md）

```bash
git fetch origin --tags
python tools/check_merge_readiness.py  # 必須輸出 ✅ READY TO MERGE
git merge origin/<branch> --no-edit    # 使用遠端分支，不用本地
```

---

## 7. 架構邊界守則（出自 Ultimate Defensive Hybrid Workflow Guide）

| 層 | 允許修改 | 禁止修改 |
|----|---------|---------|
| 幾何數學層 (`PreviewViewGeometryMapper`) | `createSensorToViewMatrix()` 數學 | UI 層、ML Kit 內部邏輯 |
| 狀態層 (`CameraViewModel`) | SP-A/C 防禦、Flow 收集 | FocusIndicatorOverlay、CameraScreen |
| AF 硬體層 (`CameraXFocusController`) | 只在 SP-C 中控制 cancel 時機 | HAL callback 的回傳內容 |

**Scope Drift 零容忍**：修改 `CameraViewModel.kt` 時，`git diff` 不得出現 `CameraScreen.kt` 的變動。

---

## 8. 關鍵 SSOT 文件索引

| 需要查什麼 | 去哪裡找 |
|-----------|---------|
| 防禦型守則（最新） | `devspec/feature_record_Backlog.md` → v2.12.0-26 section |
| 對焦顏色 Cyan vs Yellow | `plans/proposals/Camera_focus_specification_SSOT.md` |
| AE/AF Lock 7 坑點 | `devspec/feature_record_Backlog.md` → AE/AF Lock section |
| 幾何管線架構 | `plans/proposals/camera_focus_domain_ssot.md` |
| Branch 穩定版與 tag | `devspec/BRANCH_RELEASES.md` |
| 合併 SOP | `devspec/MERGE_SOP.md` + `MERGE_SOP_QUICK_REFERENCE.md` |
| Sony 實機驗收 logcat | `docs/test_results/v2.12.0/logcat_v2.12.0-26_sony_20260319.log` |

---

**文件版本**: v2.12.0-27 (2026-03-20)
**維護者**: Claude Sonnet 4.6 + Akira Chang
