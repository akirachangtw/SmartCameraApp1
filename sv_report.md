# SV Report — Phase0 Session A (P0-NAMING + P0-VF)

**Date**: 2026-05-13
**Ticket**: P0-NAMING + P0-VF
**Branch**: `feature/cueshot-mvp`
**Reviewer**: Local SV (Claude Sonnet 4.6)

---

## 階段 A: 確定性驗證

### Compile
- Result: ✅ PASS — BUILD SUCCESSFUL, 0 errors (1s cached)

### Lint
- Result: ✅ PASS — 0 errors, 557 warnings (baseline, 0 new)

### File Scope
- Modified: `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`
- Authorized: ✅ — BHA-01-FIX scope = CameraViewModel.kt only
- Uncommitted docs: `.claude/PROGRESS.md` — within /qa update scope ✅

---

## 階段 A.5: 16KB ELF 對齊

- Result: ✅ ELF Verification Successful
- zipalign: ⚪ SKIP — build-tools < 35.0.0-rc3 (NOTICE only, not blocking)

---

## 階段 B: 語意審查

### Orphan Check
- TODO / FIXME / HACK: ✅ CLEAN (0 in modified scope)
- `return null` instances: ✅ pre-existing, outside BHA-01-FIX scope

### Diff Summary
Two symmetric patches applied:

**`observeCompositionAdvice` (line ~1982)**:
```kotlin
- val imgW = det.imageWidth.toFloat()
- val imgH = det.imageHeight.toFloat()
+ val rotation = det.rotationDegrees
+ val imgW = (if (rotation == 90 || rotation == 270) det.imageHeight else det.imageWidth).toFloat()
+ val imgH = (if (rotation == 90 || rotation == 270) det.imageWidth else det.imageHeight).toFloat()
+ Log.d(TAG, "[BHA-01] observeCompositionAdvice rotation=$rotation imgW=$imgW imgH=$imgH")
```

**`observePtfAlignment` (line ~2081)**:
- Identical rotation swap pattern applied ✅

### Mock Check
- ✅ CLEAN — No hardcoded Size/Color/placeholder values introduced

### State Exclusive / UDF
- ✅ Changes are pure computation inside `onEach {}` lambda
- No new state mutation paths, no UI layer calls

---

## 階段 C: 架構合規

### UDF Compliance
- ✅ COMPLIANT — imgW/imgH are local vars, no state mutation

### SSOT Drift
- ✅ ALIGNED — Pattern mirrors `updateCoordinateTransformer` line 585 exactly
- Variable names `rotation`, `imgW`, `imgH` consistent with existing codebase conventions

---

## 階段 D: Cross-Review 路由

- codex CLI: AVAILABLE
- ssot_context.md: EXISTS

### Cross-Review 判定

```
PASS
- [arch] 修改僅限 presentation 層 CameraViewModel.kt 內的旋轉尺寸正規化邏輯，
         未引入 domain 依賴、SSOT 型別漂移或硬體約束違反。
- [sv]   SV Report 宣稱的兩處對稱 patch 與 Git Diff @@ -1979、@@ -2078 內容相符，
         且 diff 未顯示超出聲明範圍的修改。
```

→ 本地 SV + Cross-Review 通過，準備提交 Traycer 終審。

---

## Device Hard Gate Verification Report

**Date**: 2026-05-11
**Device**: Sony XQ-BT52 (HQ616R2713)
**Build**: assembleDeviceDebug (commit ea8d5e7)

### 場景驗證結果

| 場景 | Logcat Gate | 視覺 | 備註 |
|------|-------------|------|------|
| 場景一 直立 Portrait | ✅ PASS | ✅ PASS | rotation=0, normBbox [0,1], Halo 跟隨正確 |
| 場景二 橫向 Landscape | ✅ PASS | 🔴 FAIL | normBbox [0,1] 正確，但 Halo 忽大忽小 |
| 場景三 PTF 切換 | ✅ PASS | 🔴 FAIL | normBbox [0,1] 正確，圓環過大且不跟隨 |
| 場景四 前鏡頭 | ✅ PASS | 🔴 FAIL | normBbox [0,1] 正確，Halo 重複出現/消失 |

### Logcat 數據（rotation=0 全場景）

```
Sony XQ-BT52: rotationDegrees 直立/橫向皆 = 0（sensor portrait-native）
raw=480x640  effective=480x640（無 swap，rotation=0 正確）
normBbox: x/y/w/h 全部落在 [0.0, 1.0] ✅
```

### 發現的新 Bug（非 BHA-01-FIX 引入）

| ID | 問題 | 根因 | 優先級 |
|----|------|------|--------|
| BUG-MLKIT-DUAL-EMIT | ML Kit 雙管線交替發射 → Halo 忽大忽小 / 閃爍 | 切換鏡頭後舊 DetectionResult Flow 未取消，與新鏡頭交替輸出 | 🔴 P0 |
| BUG-SONY-ROTATION-CONST | Sony `rotationDegrees` 恆為 0 | 裝置 sensor orientation 固定，rotation swap 在此裝置不觸發 | 🔴 P0 |
| BUG-PTF-HALO-SIZE | PTF 圓環過大且不跟隨 | 渲染層 bbox → 圓半徑轉換錯誤 | 🔴 P0 |

### CSO 最終判定

**BHA-01 歸一化數學：✅ PASS**
**T02 Hard Gate 視覺：🔴 FAIL** — 3 個前置 Bug 需先修復再重跑 Hard Gate。
**下一步**：BUG-MLKIT-DUAL-EMIT → BUG-SONY-ROTATION-CONST → BUG-PTF-HALO-SIZE → Hard Gate 重驗
