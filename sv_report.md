# SV Report — BHA-01 Diagnostic (Read-Only Audit)

**Date**: 2026-05-11  
**Ticket**: BHA-01 (Boundary Health Audit — T02 Hard Gate Diagnostic)  
**Branch**: `feature/cueshot-mvp`

---

## 階段 A: 確定性驗證

| Gate | Result | Detail |
|------|--------|--------|
| 🔨 Compile | ✅ PASS | assembleDebug — 0 errors (cache hit, 1s) |
| 🔍 Lint | ✅ PASS | 0 new warnings; 555 baseline (499 LogNotTimber, 16 GradleDep — all pre-existing) |
| 📁 Scope | ✅ PASS | **`git diff HEAD~1..HEAD`（Cross-Review diff 範圍）**：`docs/session_handoff/SESSION_HANDOFF_20260511_0112.md`（+63 lines，commit `3f659cc [Docs] Session Handoff`，前次 session 純文件歸檔）、`.claude/PROGRESS.md`（Sprint Health 更新）。**BHA-01 本次 working tree 新增**（untracked）：`.claude/bha_report.md`（診斷報告）。`sv_report.md` 為本報告自身。0 `.kt` 修改。 |

## 階段 A.5: 16KB ELF 對齊

| Gate | Result | Detail |
|------|--------|--------|
| 📦 16KB ELF | ✅ ALIGNED | `ELF Verification Successful` — 所有 arm64-v8a .so 對齊。zipalign ⚪ NOTICE (build-tools < 35.0.0-rc3) |

## 階段 B: 語意審查

| Gate | Result | Detail |
|------|--------|--------|
| 🧹 Orphans | ✅ CLEAN | 既有 `// TODO` (MainActivity.kt:50 — D1 骨架，前次 SV 已豁免)，非本次引入 |
| 🎭 Mock | ✅ CLEAN | 無 UI 修改，無新增硬編碼預設值 |
| 🏗️ UDF | ✅ COMPLIANT | 無代碼修改，UDF 狀態不變 |

## 階段 C: 架構合規

| Gate | Result | Detail |
|------|--------|--------|
| 📐 SSOT | ✅ ALIGNED | BHA-01 為純診斷任務，無新增類別或命名 |

## 根因審計摘要 (BHA-01 核心發現)

**確認路徑**:
```
SmartCameraAnalyzer
  → DetectionResult.imageWidth=480 (raw sensor, un-rotated)
  → face.boundingBox in ROTATED space: x∈[0,640]
  → observeCompositionAdvice (line 1982): imgW=480 (WRONG — missing rotation swap)
  → boundingBoxNormalized: box.left/480 > 1.0 for faces on right half
  → NormalizedToPixelTransformer(screenWidth=1080, screenHeight=1898)
  → pixel_x = normalized_x * 1080 → 2.25× position error
```

**受影響管線**:
1. `observeCompositionAdvice` (line 1977) — composition + Halo pipeline
2. `observePtfAlignment` (line 2081) — PTF Halo pipeline

**修復方案 A** (awaiting authorization):
```kotlin
val rotation = det.rotationDegrees
val imgW = (if (rotation == 90 || rotation == 270) det.imageHeight else det.imageWidth).toFloat()
val imgH = (if (rotation == 90 || rotation == 270) det.imageWidth else det.imageHeight).toFloat()
```
對齊既有 `updateCoordinateTransformer` line 585 的 rotation swap。

---

## 階段 D: Cross-Review

