# Cueshot 工作交接 — 2026-04-15 15:16

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/src/main/java/.../presentation/camera/components/EyeAfController.kt` | `process()`, `reset()`, `resolveEyeMidpointNormalized()` | NEW — D6-B3 Gaze-driven Eye-AF 控制器；6 道 Guard；2s 冷卻節流；eye midpoint → bbox 三段 fallback |
| `app/src/main/java/.../presentation/camera/CameraViewModel.kt` | `observeCompositionAdvice()`, `resetFocusState()` | 接入 EyeAfController；gaze unaligned 時呼叫 `cameraRepository.focusOnPoint()`；reset 時呼叫 `eyeAfController.reset()` |
| `app/src/test/java/.../presentation/camera/components/EyeAfControllerTest.kt` | 全部 | NEW — 9 條 Robolectric 單元測試（前鏡頭抑制、Gaze 對齊抑制、正確座標、節流、empty faces、null result、bbox fallback、reset） |
| `app/build.gradle.kts` | `testOptions.unitTests` | 新增 `isReturnDefaultValues = true`（允許 Log.* 在 JVM 單元測試中不拋出） |
| `.claude/CLAUDE.md` | KAIROS #2 產出區塊 | 追加 Rules 13/14/15（Android SDK JVM 測試規則、Compose 狀態記憶置頂規則、KDoc 呼叫鏈準確性規則） |
| `.claude/PROGRESS.md` | KAIROS #2 section, D6-B3 row | D6-B3 → ✅ DONE；KAIROS #2 → ✅ EXECUTED with full report |

## 當前阻塞點

無

## 下一步待辦

1. **D5-TECH-DEBT-01（已延至 D7）**: `HDRWarningOverlayTest` — 重寫 3 條狀態轉換測試（改用 `mutableStateOf` 而非 static 傳入），檔案：`app/src/androidTest/java/.../presentation/camera/components/HDRWarningOverlayTest.kt`
2. **D5-TECH-DEBT-01（已延至 D7）**: `EdgeWarmthLayerTest` Sony race condition — 加入 `waitForIdle()` 修復非同步斷言，檔案：`app/src/androidTest/java/.../presentation/camera/components/EdgeWarmthLayerTest.kt`
3. **SSOT Drift (minor, D7 排程)**：將 `ruleScores["gaze"]` magic string 提取至 `GazeRule.NAME` companion const；檔案：`EyeAfController.kt` + `GazeRule.kt`
4. **D7 Sprint 開工**：讀取 PROGRESS.md 確認 D7 Ticket 清單，依 SSOT roadmap 開始 Object Detection / Food Mode 或下一個排定功能

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `EyeAfController` 以 private member 方式持有於 ViewModel，不用 Hilt DI | 無外部依賴，constructor 無參數；DI 增加複雜度無益 | `CameraViewModel.kt` |
| 呼叫 `cameraRepository.focusOnPoint()` 而非走 `CameraXFocusController` 完整路徑 | EyeAfController 已產出 view-space pixel 座標，簡單路徑即可；複雜路徑需 `EyeAfDecision`+`FocusTarget`+`GeometryContext` | `CameraViewModel.kt` |
| `lastTriggerMs` 初始值為 `Long.MIN_VALUE / 2`（非 `0L`） | 確保第一次呼叫永遠通過節流閘，避免 `nowMs=1000L` 時 `1000-0<2000` 誤攔截 | `EyeAfController.kt`, test suite |
| KDoc FIX-01：第二道防線描述改為 `LiveCameraSource.focusOnPoint()` + `FixedFocusDetector` | 原 KDoc 誤引 `CameraXFocusController`；實際呼叫鏈為 `cameraRepository → LiveCameraSource → isFixedFocus`（由 `FixedFocusDetector` 在 bind 時偵測） | `EyeAfController.kt` KDoc |

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（特別是 Rules 13/14/15 為本次新增）
- `.claude/PROGRESS.md` — D7 Ticket 清單與 Sprint 狀態
- `app/src/main/java/.../presentation/camera/components/EyeAfController.kt` — 若 D7 需接線 ViewModel 端觸發邏輯
- `app/src/main/java/.../presentation/camera/CameraViewModel.kt` — 若 D7 需在觀察鏈加新邏輯

## 不需帶入的 Context

- D6-B3 的所有 debug 過程（Log 未 mock、PointF stub 返回 0、節流哨兵值 bug）— 已透過 KAIROS #2 提煉為 Rule 13，根因已解決
- Korean 語言漂移警告 — 已記錄至 memory，不需在新對話中重提
- KAIROS #2 完整報告細節 — 已寫入 PROGRESS.md，新對話讀 PROGRESS.md 即可
- `LiveCameraSource.kt` 第二道防線調查內容 — KDoc 已更正，不需再查
- Traycer 終審申請細節 — D6-B3 本地驗收已過，Traycer 流程由人工判斷時機
