# Cueshot 工作交接 — 20260412_2106

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `presentation/camera/CameraScreen.kt` | 頂層常數 + 快門 Box | SV-D4-T4-FIX-01：新增 `TAG_SHUTTER_BREATHING_RING` / `TAG_SHUTTER_PROGRESS_CIRCLE` 常數；提取快門區塊為 `ShutterButtonSection(isAligned, isCapturing, onCapture)`；BreathingRing Canvas 掛 testTag；ProgressCircle 掛 testTag；原位呼叫改為 `ShutterButtonSection(...)` |
| `presentation/camera/CameraScreen.kt` | `ShutterButtonSection` (新建 internal fun) | Stateless composable，無 ViewModel 依賴，供 androidTest 直接組合 |
| `presentation/camera/overlay/EdgeWarmthLayer.kt` | 頂層常數 + amber Canvas 前 | SV-D4-T4-FIX-02：新增 `TAG_EDGE_WARMTH_DOTS_ENABLED` 常數；在 `renderDots && direction != null` 條件內插入零尺寸 `Box` 語意探針；加入 `Box`、`size`、`dp` imports |
| `androidTest/.../CameraScreenBreathingRingTest.kt` | 全檔（新建） | SV-D4-T4-FIX-01/02：4 個 instrumented tests，直接組合 `ShutterButtonSection`；用 `mutableStateOf` 驅動狀態切換（正確 API，無 setContent 重複呼叫問題） |
| `devspec/feature_record_Backlog.md` | ADR 末尾 | SV-D4-T4-FIX-03：追加「D4-T4 測試越權與修改漂移防禦」ADR（測試邊界紀律 + 預存在技術債處理流程） |
| `.claude/PROGRESS.md` | D4 Tickets 表 + Blocking Issues | SV-D4-T4-FIX-03 閉環；Unit Tests 計數 120→125；HDRWarningOverlayTest 3 個狀態切換測試登記為 🟡 LOW 技術債 |

## 當前阻塞點

無

## 下一步待辦

1. **[SV-D4-T4-FIX-04] 執行 `CameraScreenBreathingRingTest` 實機驗證**：連接 Sony XQ-BT52（`HQ616R2713`），執行 `./gradlew connectedDeviceDebugAndroidTest`，確認 4 個新 tests 全數通過。
2. **[SV-D4-T4-FIX-05] `EdgeWarmthLayerTest` 補強 dots probe 測試**：在 `EdgeWarmthLayerTest.kt` 新增使用 `TAG_EDGE_WARMTH_DOTS_ENABLED` 的測試案例：
   - `fullTier_withDirection_dotsProbeExists`：`deviceRoll=15f`（>ROLL_THRESHOLD_DEG=3f），`capabilityTier=FULL` → `TAG_EDGE_WARMTH_DOTS_ENABLED` 探針存在
   - `legacyTier_dotsProbeAbsent`：`capabilityTier=LEGACY` → `TAG_EDGE_WARMTH_DOTS_ENABLED` 探針不存在
3. **[D4 最終封板] 執行 `/qa` → `/sv` → 更新 PROGRESS.md**：FIX-04/05 完成後，確認 D4 全系列 Path A 終局驗收通過。
4. **[D5-T1] 開工前讀取 SSOT**：讀取 `SmartCameraApp_Strategy_v3_3_SSOT_20260404.md` 確認 D5 交付物；建立 `domain/composition/PriorityEngine.kt`。

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `ShutterButtonSection` 提取為 stateless composable | `CameraScreen` 依賴 Hilt+CameraX 無法直接在測試中組合；stateless 提取讓 androidTest 可直接 `setContent { ShutterButtonSection(...) }` | `CameraScreen.kt`、`CameraScreenBreathingRingTest.kt` |
| `TAG_EDGE_WARMTH_DOTS_ENABLED` 用零尺寸 Box 而非 Canvas semantics | Canvas draw call 無法被 Compose semantics 樹觀測；零尺寸 Box 是最小侵入式的語意探針，不影響視覺輸出 | `EdgeWarmthLayer.kt` |
| 狀態切換測試用 `mutableStateOf` 而非重複 `setContent` | `HDRWarningOverlayTest` 的舊式 `setContent` 重複呼叫會拋出 `IllegalStateException`；`mutableStateOf` 才是 Compose Test 正確 API | `CameraScreenBreathingRingTest.kt` |
| `HDRWarningOverlayTest` 預存在 compile error 視為技術債而非本次越權 | D4-T4 授權範圍不含該檔案；修復行為已事後寫入 ADR（SV-D4-T4-FIX-03）作為教訓歸檔 | `devspec/feature_record_Backlog.md` |

## 需帶入新對話的 Context

- `CLAUDE.md`（規則 #10 Bash safety、KAIROS #11/#12）
- `PROGRESS.md`（D4 狀態、D5 下一步）
- `presentation/camera/CameraScreen.kt`（`ShutterButtonSection` 實作現況）
- `presentation/camera/overlay/EdgeWarmthLayer.kt`（`TAG_EDGE_WARMTH_DOTS_ENABLED` 探針位置）
- `androidTest/.../EdgeWarmthLayerTest.kt`（需新增 FIX-05 的 dots probe 測試）
- `androidTest/.../CameraScreenBreathingRingTest.kt`（FIX-04 實機驗證目標）

## 不需帶入的 Context

- D4-T4 測試矩陣實作過程（已 commit `4df4061`）
- SV-D4-T4-FIX-03 越權 ADR 歸檔討論（已 commit `f6d13b8`）
- `HDRWarningOverlayTest` compile error 修復細節（已閉環，ADR 歸檔）
- `CameraScreenStateTest` JVM 測試設計過程（已 commit，5/5 pass）
- `EdgeWarmthLayerTest` 初版（6/6 Sony 實機通過，但未含 dots probe 測試）
- Session token 壓縮討論
