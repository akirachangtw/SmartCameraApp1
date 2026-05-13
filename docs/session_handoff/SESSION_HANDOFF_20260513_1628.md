# Cueshot Handoff — 2026-05-13 16:28

## Dashboard（新 session 先讀這段）
- **完成**: Phase0 Session A 完整落地 — P0-NAMING（EdgeWarmth 全專案重命名為 PitchTraction，7 檔）+ P0-VF（取景框 664.dp + FILL_CENTER + 移除綠色角括號）；QA ✅ / SV ✅ / 2 atomic commits 封存
- **阻塞**: 4 個 P0/P1 Bug 仍阻塞 T02 Hard Gate 視覺驗收（見 PROGRESS.md Active Blockers）
- **下一步**: Session B — 優先 `BUG-MLKIT-DUAL-EMIT`：在 `CameraViewModel.kt` 切換鏡頭時清空 `detectionResult`（或改用 `flatMapLatest`），再處理 `BUG-PTF-HALO-SIZE` 與 `BUG-SONY-ROTATION-CONST`
- **帶入**: CLAUDE.md · PROGRESS.md · `CameraViewModel.kt` · `PitchTractionLayer.kt` · `CameraScreen.kt`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `overlay/PitchTractionLayer.kt` | 全檔 | 由 EdgeWarmthLayer.kt rename 重建；TAG/enum/label 全更名為 PitchTraction* |
| `overlay/EdgeWarmthLayer.kt` | 全檔 | 焦土刪除 |
| `PitchTractionLayerTest.kt` | 全檔 | 對應重建，9 條測試更名，import 更新 |
| `EdgeWarmthLayerTest.kt` | 全檔 | 焦土刪除 |
| `CameraScreen.kt` | import L118 / comment L207 / call L299 / viewfinder Box L228 / control band L491 | import+call PitchTractionLayer；Box weight(0.787f)→height(664.dp)；control band weight(1f) |
| `HaloGuidanceLayer.kt` | L45 / L57 / L252 | 3 處 EdgeWarmthLayer comment → PitchTractionLayer |
| `CameraMode.kt` | L34 PassToFriend KDoc | "Edge Warmth" → "Pitch Traction" |
| `CompositionGuideOverlay.kt` | `drawGuideFrame` / `drawCornerMarkers` | 移除 `drawCornerMarkers` call + 函數本體（綠色 L 形角括號完全清除） |
| `AspectRatioMode.kt` | `toScaleType` | FIT_CENTER → FILL_CENTER（全模式統一） |
| `.claude/PROGRESS.md` | Dashboard / Sprint Health / Next Actions | Phase0 Session A 完成狀態同步 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| EdgeWarmth 全面改為 PitchTraction | SSOT v4.0 命名規範；舊名「Edge Warmth」已作廢 | 主檔 + 測試 + 7 個參考點 |
| 取景框從 weight(0.787f) 改為 height(664.dp) | SSOT v4.0 佈局規格（78.7% = 664dp reference height） | CameraScreen.kt Column 佈局 |
| scaleType 統一改為 FILL_CENTER | SSOT v4.0 P0-VF 規格；664.dp 固定高度下無需黑邊 | AspectRatioMode + CameraPreview |
| 移除 CompositionGuideOverlay 角括號 | P0-VF 規格：移除所有遺留 Debug 角括號 | CompositionGuideOverlay.kt |
| dtaudit SKILL.md 格式修正 | 補 disable-model-invocation + # Prompt 標頭，符合 Agent Skill 規範 | .claude/skills/dtaudit/ |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更（本 session 2 commits）：`git diff HEAD~2 --stat`

核心檔案快速查：
```
git diff HEAD~2 -p app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt
git diff HEAD~2 -p app/src/main/java/com/akirachang/ai/photographer/presentation/camera/overlay/PitchTractionLayer.kt
git diff HEAD~2 -p app/src/main/java/com/akirachang/ai/photographer/presentation/camera/model/AspectRatioMode.kt
```

本次 session commits（HEAD~2 起）：
```
96aaeff [Docs] Phase0 Session A — QA/SV 歸檔 + PROGRESS 更新
534cd3d [Refactor] Phase0 Session A — P0-NAMING + P0-VF
```

Active Blockers（Session B 目標）：
- `BUG-MLKIT-DUAL-EMIT`（🔴 P0）：切換鏡頭後舊管線 DetectionResult 殘留 → Halo 閃爍。修復：`CameraViewModel.kt` 切換時 `_state.update { copy(detectionResult = null) }` 或 `flatMapLatest`
- `BUG-SONY-ROTATION-CONST`（🔴 P0）：Sony XQ-BT52 `rotationDegrees` 恆為 0
- `BUG-PTF-HALO-SIZE`（🔴 P0）：PTF Halo 渲染過大且不跟隨
- `BUG-I7-FOCUS-RING-PERSIST`（🔴 P1）：`EyeAfController.kt:process()` 需加入 Guard 7

</details>
