# Cueshot Handoff — 2026-05-11 21:49

## Dashboard（新 session 先讀這段）
- **完成**: CSO 裁決簡報完整歸檔（4 場景驗測 + 3 P0 Bug + 雙圓環追加場景）；雙圓環根因確認為 `FocusIndicatorOverlay.drawSearchingBase`（設計 3 層同心圓）+ I7 觸發鏈，並修正至 CSO 簡報
- **阻塞**: 4 個 P0/P1 Bug 等待修復（見下方）；T02 Hard Gate 視覺驗收仍 🔴 FAIL
- **下一步**: 優先修復 `BUG-I7-FOCUS-RING-PERSIST` — `EyeAfController.kt:process()` 加入 Guard 7（`LensFocusState.Focused + 座標漂移 < 閾值 → return null`），需同步修改 `CameraViewModel.kt` 傳入 `lensFocusState` 參數
- **帶入**: CLAUDE.md · PROGRESS.md · `EyeAfController.kt` · `FocusIndicatorOverlay.kt` · `plans/cso_briefing/CSO_HardGate_BHA01_20260511.md`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `plans/cso_briefing/CSO_HardGate_BHA01_20260511.md` | 五、追加場景 | 新增雙圓環場景（初版 I7+HaloGuidanceLayer 雙原因）|
| `plans/cso_briefing/CSO_HardGate_BHA01_20260511.md` | 五、根因分析 | 修正為 FocusIndicatorOverlay.drawSearchingBase 單一 Overlay；Bug 登記更新為 BUG-I7-FOCUS-RING-PERSIST，含 Guard 7 修復方向 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| 雙圓環 = 單一 FocusIndicatorOverlay | 圖片確認外圈細實線 + 中圈粗虛線 = drawSearchingBase 3 層同心圓設計規格，非兩 Overlay 疊加 | CSO 簡報根因描述 |
| 修復目標收斂至 EyeAfController Guard 7 | HaloGuidanceLayer 本身規格正確（compositionResult==null 已隱藏）；問題根源是 I7 重觸發打斷 dwell fade | `EyeAfController.kt` |
| BUG-MLKIT-DUAL-EMIT 在有人臉時也可觀察到 | 背景 logcat 顯示同毫秒雙重 observeCompositionAdvice emit，且 face[0] normBbox 座標凍結（stale 舊管線） | `CameraViewModel.kt` 鏡頭切換邏輯 |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD~2 --stat`

本次 Session commits（HEAD~2 起）：
```
a49c935 [Docs] CSO 簡報更新 — 雙圓環根因修正
960e09e [Docs] CSO 裁決簡報 — BHA-01 Hard Gate 四場景驗測 + 3 Bug 登記
```

FocusIndicatorOverlay 規格確認：
- `FocusIndicatorOverlay.kt:152` `drawSearchingBase` — 3 層同心圓
  - 外圈：radius × 0.92，1.5dp 實線
  - 中圈：radius × 0.78，14dp 虛線（8dp/6dp 間距）
  - 內圈：radius × 0.64，1.5dp 實線
- `FocusTokens.SearchingSize = 180dp`，所有 alpha = animatedAlpha × 0.2
- Guard：`overlayModel == null || visualTargetState == None → return`

EyeAfController Guard 7 修復方向（`EyeAfController.kt:74`）：
```kotlin
fun process(
    compositionResult: CompositionResult?,
    faces: List<FaceLandmarkData>,
    facing: CameraLens,
    lensFocusState: LensFocusState,   // 新增參數
    previewWidth: Int,
    previewHeight: Int,
    nowMs: Long = System.currentTimeMillis(),
): EyeAfTrigger? {
    // ... existing guards ...

    // Guard 7: lens already focused + target stable → skip re-trigger
    val eyePixelCandidate = /* resolve eye midpoint */
    if (lensFocusState == LensFocusState.Focused
        && lastTriggerPoint != null
        && distance(eyePixelCandidate, lastTriggerPoint) < DRIFT_THRESHOLD_PX) {
        Log.v(TAG, "[I7] Focused+stable — AF skip")
        return null
    }
```
`lastTriggerPoint: PointF?` 需新增為 instance 變數，`reset()` 時清除。

BUG-MLKIT-DUAL-EMIT logcat 證據（21:19:22）：
```
observeCompositionAdvice × 2 per ms（一有 face，一無 face）
face[0] normBbox x=0.308 y=0.441 w=0.294 h=0.219（全程固定 = stale 舊管線）
```

</details>
