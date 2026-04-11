# Cueshot 工作交接 — 20260411_2234

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `data/source/camera/CameraCapabilityDetector.kt` | `detectAndCacheCapabilities()`, `querySupportedResolutions()` | FIX-04：補 `@androidx.annotation.OptIn(ExperimentalCameraInfo + ExperimentalCamera2Interop)`；新增兩項 import |
| `data/source/camera/LiveCameraSource.kt` | `bindToLifecycle()` | FIX-04：補 `@androidx.annotation.OptIn(ExperimentalCameraInfo + ExperimentalCamera2Interop)`；新增 `ExperimentalCameraInfo` import |
| `presentation/camera/CameraViewModel.kt` | `companion object` + bitmap analysis pipeline (~L1633) | D3-T4：提取 `internal fun computeFocalLengthPixels(factor, previewWidth)` 至 companion object；inline `run{}` 改呼叫此函數 |
| `test/.../domain/camera/ImuCompensatorTest.kt` | 新建 | D3-T4：6 tests — `focalLength=0f` 哨兵靜默、公式驗算、consume-once 重置、無 sample baseline、大焦距有限性 |
| `test/.../presentation/camera/CameraViewModelTest.kt` | 新建 | D3-T4：10 tests — `computeFocalLengthPixels` 邊界 (width=0, fail-safe, hardware-first, NaN guard)；`CameraState.deviceRoll` 極端值 (±180°, 預設, copy 隔離) |
| `devspec/feature_record_Backlog.md` | ADR 區塊末尾 | D3-T5：追加 D3 ADR（UnsafeOptIn 治理、fail-safe 契約測試模板、Rule 10 落地、ImuCompensator bootstrap 陷阱）；追加 D3 Backlog 殘骸表格 |
| `.claude/PROGRESS.md` | D3 Tickets 表 + Sprint Health | D3-T4/T5 ✅ DONE；D3 整體 ✅ CLOSED；Sprint Health 單元測試更新為 120t/48skipped/72pass；封板宣言寫入 |

## 當前阻塞點

無

## 下一步待辦

1. **[git push]**：執行 `git push origin feature/cueshot-mvp`（本 Session 所有 commits 尚未 push：`3a47552` FIX-04、`83dec87` refactor、`31daf53` D3-T4 tests、`8f443b6` D3-T5 docs）

2. **[D4-T0 前置焦土]**：清除 `CameraScreen.kt:106` 的 `GuidelineOverlay` dead import。步驟：`grep -n "GuidelineOverlay" app/src/main/java/.../CameraScreen.kt` 確認 import 行號 → 刪除 → 執行 `/qa` → `/sv`

3. **[D4-T1] Edge Warmth 第二層（浮動光點）**：依 SSOT v3.3 §EdgeWarmthLayer 實作浮動光點 Overlay，接線至 `CameraState.deviceRoll`。開工前讀取 SSOT 對應段落確認命名與動畫規格。

4. **[D4-T2] 呼吸韻律初版**：`#50C864` 綠色脈動動畫，接線至 `CameraState.compositionResult.isAllAligned`。

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| FIX-04 使用 `@androidx.annotation.OptIn` 而非 `@kotlin.OptIn` | Lint checker 使用 `androidx.annotation.experimental`，只認識 Java-compatible annotation；Kotlin 原生 `@OptIn` 對 Lint 無效 | `CameraCapabilityDetector.kt`, `LiveCameraSource.kt` |
| D3-T4 提取 `computeFocalLengthPixels()` 為 companion internal fun | 原 inline `run{}` 無法在 JVM 單元測試中直接呼叫；提取後零 DI 可測，邏輯完全不變 | `CameraViewModel.kt` |
| ImuCompensatorTest 使用 `timestamp=1_000_000L` 作為 bootstrap sample | `accumulate()` 在 `lastTimestampNs==0L` 時只設 timestamp 不積分；若 sample1 timestamp 也是 `0L`，後續所有 sample 均走 bootstrap，累積永遠為 0 | `ImuCompensatorTest.kt` |

## 需帶入新對話的 Context

- `CLAUDE.md`（規則 #10 Bash safety，KAIROS #11/#12 重編號）
- `PROGRESS.md`（D3 ✅ CLOSED；D4 下一步）
- `SmartCameraApp_Strategy_v3_3_SSOT_20260404.md`（D4 EdgeWarmth / 呼吸韻律規格來源）
- `presentation/camera/CameraScreen.kt`（D4-T0 焦土 + D4 overlay 插入點）
- `presentation/camera/CameraState.kt`（`deviceRoll`, `compositionResult` 欄位確認）

## 不需帶入的 Context

- D3-T3-FIX-01/02/03 的討論（已完成，commits `d348e8f` + `d96366f`）
- D3-T3-FIX-04 的 `@OptIn` 格式選擇討論（已閉環，結論寫入 ADR）
- D3-T4 測試設計與 ImuCompensator bootstrap 陷阱偵錯過程（已閉環，結論寫入 ADR）
- D3-T5 歸檔討論（已 commit `8f443b6`）
- `600f` / `800f` 魔術數字討論（D3 前已清零）
- Lint `UnsafeOptInUsageError` 除錯過程（`@kotlin.OptIn` vs `@androidx.annotation.OptIn` 差異）
