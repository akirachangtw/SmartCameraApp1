# SV Report — FIX-P0-SESSION-A
**Date**: 2026-05-13
**Branch**: feature/cueshot-mvp
**Scope**: Phase0 Session A Traycer 驗收修補（DetectionOverlay 全鏈清理 + 真 1px 分隔線 + CLAUDE.md 命名淨化）

---

## 階段 A — Orphan / Residue Scan

### FIX-P0VF-01: DetectionOverlay 全鏈焦土清理

```
$ grep -ri "DetectionOverlay|detection_overlay_enabled" app/src/
(no output — 0 matches)
```

| 層 | 檔案 | 狀態 |
|----|------|------|
| UI Composable | `DetectionOverlay.kt` | ✅ 刪除（-225L） |
| Settings 開關 | `SettingsScreen.kt` | ✅ Detection Boxes toggle 移除（-16L） |
| 持久化 key | `OverlayPreferences.kt` | ✅ `KEY_DETECTION_OVERLAY_ENABLED` + Flow + setter 移除（-28L） |
| ViewModel 暴露 | `CameraViewModel.kt` | ✅ `isDetectionOverlayEnabled` Flow 移除（-1L） |
| ViewModel 暴露 | `SettingsViewModel.kt` | ✅ Flow + suspend fun 移除（-12L） |

### FIX-P0VF-02: 分隔線真 1px

```
$ grep -n "height(1.dp)" app/src/.../CameraScreen.kt
(no output — 1.dp hardcode removed)
```

變更後（CameraScreen.kt）:
```kotlin
Canvas(modifier = Modifier.fillMaxWidth().height((1f / LocalDensity.current.density).dp)) {
    drawRect(color = Color.White.copy(alpha = 0.05f))
}
```

### FIX-P0N-01: CLAUDE.md 命名淨化

```
$ grep -n "Edge Warmth|edge_warmth" .claude/CLAUDE.md | grep -v "EdgeWarmthLayer"
(no output — 0 active references)
```

淨化位置（4 處）: L164 / L215-217 / L219 / L302 → "Pitch Traction"
新增 Rule 19 Decommission 全鏈路紀律 ✅

---

## 階段 B — 編譯驗證

```
$ ./gradlew assembleDeviceDebug
BUILD SUCCESSFUL in 1s (cached)
0 errors
```

---

## 階段 C — 單元測試

```
$ ./gradlew testDeviceDebugUnitTest
BUILD SUCCESSFUL in 1s (cached)
All tests passed
```

---

## 階段 D — Lint

```
$ ./gradlew lintDeviceDebug
BUILD SUCCESSFUL in 1s
0 new warnings (baseline unchanged)
```

---

## 階段 E — Scope 邊界確認

**git diff --stat（HEAD vs working tree）**:
```
.claude/CLAUDE.md                                  |  18 +-
.../data/preferences/OverlayPreferences.kt         |  28 ---
.../presentation/camera/CameraScreen.kt            |   5 +-
.../presentation/camera/CameraViewModel.kt         |   1 -
.../camera/components/overlay/DetectionOverlay.kt  | 225 ---------------------
.../presentation/settings/SettingsScreen.kt        |  16 --
.../presentation/settings/SettingsViewModel.kt     |  12 --
7 files changed, 14 insertions(+), 291 deletions(-)
```

越權檢查:
- PitchTractionLayer.kt — ✅ NOT TOUCHED
- AspectRatioMode.kt (FILL_CENTER) — ✅ NOT TOUCHED
- CompositionGuideOverlay.kt — ✅ NOT TOUCHED

---

## 驗收結論

| 驗收項目 | 結果 |
|---------|------|
| grep DetectionOverlay/detection_overlay_enabled app/src/ 為空 | ✅ PASS |
| SettingsScreen 無 Detection Boxes 選項 | ✅ PASS |
| CameraScreen.kt 分隔線非 1.dp 硬編碼 | ✅ PASS |
| CLAUDE.md 無活躍 "Edge Warmth"（歷史反例 L11 保留）| ✅ PASS |
| CLAUDE.md Rule 19 Decommission 全鏈路紀律寫入 | ✅ PASS |
| 編譯 0 errors | ✅ PASS |
| Unit Test PASS | ✅ PASS |
| Lint 0 new warnings | ✅ PASS |
| PitchTraction / FILL_CENTER 邏輯未觸碰 | ✅ PASS |

**SV 結論**: PASS — Ready for Traycer re-verification.
