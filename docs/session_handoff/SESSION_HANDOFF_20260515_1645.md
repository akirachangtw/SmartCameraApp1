# Cueshot Handoff — Session E 2026-05-15

## Dashboard（新 session 先讀這段）
- **完成**: Session E — P0-FOCUS（Guard 7 stableId preservation + FocusIndicator VisualLocked/Lost complete fade）+ FIX-P0-SESSION-C-FINAL（FPS 四層完整剝離：interface/ViewModel/callsite/data + FPSOverlay.kt 刪除 + Rule 24）。SV ✅ Cross-Review PASS ✅ 3 commits。APK 已安裝至 Pixel 9a。
- **阻塞**: BUG-MLKIT-DUAL-EMIT（P0）、BUG-PTF-HALO-SIZE（P0）、BUG-SONY-ROTATION-CONST（P0）仍未解決。
- **下一步**: ①Traycer 終審（commits c5062cc + 582bb62 + 095b2a5）②Phase 0.5 Visual Baseline Gate（8 場景截圖）③BUG-MLKIT-DUAL-EMIT：`CameraViewModel.kt` 切換鏡頭清空 `detectionResult` 或改用 `flatMapLatest`
- **帶入**: CLAUDE.md · PROGRESS.md · CameraViewModel.kt · EyeAfController.kt · FocusIndicatorOverlay.kt

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `EyeAfController.kt` | `process()` Guard 7 | `preserveStableId`: drift < 0.05f → reuse lastStableId; `lastTriggerPoint` + `lastStableId` instance vars; `reset()` clears both |
| `EyeAfControllerTest.kt` | Tests 4b/4c/4d | Guard 7 boundary: drift=0/below/above threshold → stableId preserved or new |
| `FocusIndicatorOverlay.kt` | `FocusTokens` + `LaunchedEffect` | `DWELL_DELAY` 1000→1500ms; `FAILED_DWELL_DELAY=800ms`; `VisualLocked` alpha 0.4→0f; `Lost` delay 800ms + alpha 0f |
| `CameraRepository.kt` | interface | `getFpsStream()` removed |
| `CameraInputSource.kt` | interface | `getFpsStream()` default removed |
| `CameraRepositoryImpl.kt` | override | `getFpsStream()` override removed |
| `LiveCameraSource.kt` | declarations + frame loop | `_currentFps` StateFlow removed; `getFpsStream()` override removed; `_currentFps.value = fps` update removed |
| `CameraViewModel.kt` | property | `currentFps: StateFlow<Float?>` removed |
| `CameraScreen.kt` | collect + callsite | `currentFps` collect removed; `FPSOverlay(...)` callsite removed; import removed |
| `FPSOverlay.kt` | entire file | **deleted** (焦土政策) |
| `.claude/rules/phase0-lessons.md` | Rule 24 | Debug 功能剝離紀律：4-layer strip contract |
| `.claude/CLAUDE.md` | Path-Scoped Rules | index 19-23 → 19-24 |

3 commits:
- `c5062cc` [P0-FOCUS] Guard 7 stableId preservation + FocusIndicator dwell fade complete
- `582bb62` [P0-FPS] FPS pipeline 四層完整剝離 — interface + ViewModel + callsite + data
- `095b2a5` [INFRA-05] Rule 24 + PROGRESS Session E 狀態同步 + .bak 清理

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| Guard 7 → preserveStableId（非 return null） | return null 破壞 cooldown 測試；正確語意是「相同位置重觸發時保留 stableId 防止 LaunchedEffect 重置」 | EyeAfController + EyeAfControllerTest |
| FPS 完整刪除（非 BuildConfig.DEBUG gate） | Traycer 退回：interface 污染 Release 合約；`visible=DEBUG` 不足；split-brain 風險 | 5 個檔案 + FPSOverlay.kt 刪除 |
| `DRIFT_THRESHOLD_NORMALIZED = 0.05f` | SSOT v4.0 §7 Eye-AF motor protection：≈54px on 1080p，足以區分有意移動與 jitter | EyeAfController companion object |
| cross_review.sh 使用 `HEAD`（非預設 HEAD~1..HEAD） | 變更未提交前 HEAD~1..HEAD 指向錯誤 commit（含前次 SV skill 修改） | cross_review.sh 使用規範 |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD~3 --stat`
P0-FOCUS：`git diff HEAD~3..HEAD~2 -p app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/EyeAfController.kt`
P0-FPS：`git diff HEAD~2..HEAD~1 -p app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`

下一步目標檔案：
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`（BUG-MLKIT-DUAL-EMIT）

</details>
