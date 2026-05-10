# SV Report — TICKET-02 Camera Layout Shell
**Date**: 2026-05-10
**Branch**: feature/cueshot-mvp
**Reviewer**: Local SV (Claude Sonnet 4.6)

---

## 階段 A: 確定性驗證

### Compile
- Result: ✅ PASS — BUILD SUCCESSFUL, 0 errors
- Command: `./gradlew assembleDebug`

### Lint
- Result: ✅ PASS — 0 new warnings
- Command: `./gradlew lint`

### File Scope
- Result: ✅ PASS
- Modified files (feature commit HEAD~2..HEAD~1):
  - `CameraScreen.kt` (TICKET-02 target)
  - `GridOverlay.kt` (TICKET-02 target)
- Docs commit (HEAD~1..HEAD): `.claude/PROGRESS.md` + `sv_report.md` (authorized: /qa + /sv outputs)

---

## 階段 A.5: 16KB ELF 對齊

- Result: ✅ PASS — ELF Verification Successful
- zipalign: ⚪ SKIP (build-tools < 35.0.0-rc3, NOTICE only)

---

## 階段 B: 語意審查

### Orphan Check
- Result: ✅ CLEAN — No TODO/FIXME/HACK/return null in modified files

### Mock / Placeholder Check
- Result: ✅ CLEAN
- Scrim bound to `AmbientConstants.SCRIM_HEIGHT_PX` (not hardcoded 120px)
- Layout weights: viewfinder=0.787f, control band=0.213f (spec-compliant)
- GridOverlay defaults: α=0.08f / strokeWidth=0.5f ✅

### State Exclusive / UI Mutual Exclusion
- Result: ✅ PASS
- Column cleanly separates viewfinder (78.7%) from control band (21.3%)
- Scrim Z-order: above FocusIndicatorOverlay, below top controls Row ✅
- Separator Canvas 1dp / α0.05 at boundary, no overlap risk ✅

---

## 階段 C: 架構合規

### UDF Compliance
- Result: ✅ COMPLIANT
- All state flows ViewModel → UI via collectAsState()
- Control band dispatches via viewModel.onEvent(...) only ✅

### SSOT Drift Check
- Result: ✅ ALIGNED
- EdgeWarmthLayer, HaloGuidanceLayer, GridOverlay — match v3.3 SSOT ✅
- AmbientConstants.SCRIM_HEIGHT_PX referenced (not hardcoded) ✅

### Risk #1 — Coordinate Mapping Validation
- Result: ✅ SAFE
- PreviewViewportContainer constrained to viewfinder Box (weight=0.787f)
- onSizeChanged reports viewfinder dimensions → calculateContentBounds uses correct size
- Viewfinder starts at y=0 (top of Column) — coordinate space identical to prior layout
- CoordinateTransformer.toMatrix() unaffected; ML Kit bbox mapping correct ✅

---

## 階段 D: Cross-Review 路由

- codex CLI: AVAILABLE
- ssot_context.md: EXISTS (updated: HaloLayer → HaloGuidanceLayer)
- Diff scope used: HEAD~4..HEAD~3 (feature commit)

### Cross-Review 判定與豁免紀錄

| Codex 發現 | Severity | 處置 |
| --- | --- | --- |
| Scrim Z-order 覆蓋 FocusIndicatorOverlay | medium/sv | ✅ 已修正 (commit 98226aa) — Scrim 移至 FocusIndicator 之前 |
| 佈局比例 0.787f/0.213f 無 SSOT 依據 | medium/sv | ✅ 已修正 (commit 5ea257e) — 抽取至 AmbientConstants |
| CameraScreen.kt 未用 Strangler Pattern 新建元件 | medium/arch | ⚪ 授權例外 — TICKET-02 明確列 CameraScreen.kt 為 Target Scope，layout 重構不適用元件替換模式 |

→ 本地 SV + Cross-Review 三輪驗收，2 項已修正，1 項授權例外。準備提交 Traycer 終審。
