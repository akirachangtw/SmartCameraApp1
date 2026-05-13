# Cueshot Handoff — 2026-05-13 23:49

## Dashboard（新 session 先讀這段）

- **完成**: P0-TOPBAR + P0-BOTTOM 全閉環（QA ✅ / SV ✅ / Cross-Review ✅ / Sony XQ-BT52 實機驗收 ✅）；BUG-SHUTTER-RING-OFFSET（圓環橢圓偏移）修復並實機確認
- **阻塞**: BUG-MLKIT-DUAL-EMIT 🔴 / BUG-SONY-ROTATION-CONST 🔴 / BUG-PTF-HALO-SIZE 🔴 / BUG-I7-FOCUS-RING-PERSIST 🔴 P1
- **下一步**: Session C — P0-FPS（`CameraViewModel.kt` Sony rotation 調查）+ P0-FOCUS（`EyeAfController.kt:process()` Guard 7）+ Baseline（MLKIT-DUAL-EMIT + PTF-HALO-SIZE）
- **帶入**: CLAUDE.md · PROGRESS.md · CameraScreen.kt · CameraViewModel.kt · EyeAfController.kt · BottomControlBar.kt · PillToggle.kt

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `CameraScreen.kt` | Top Bar Row | 舊單列 SpaceBetween → 新雙列：左 Row(Flash 48dp, HDR 48dp) / 右 Row(Settings 48dp, Help 48dp)；圖示 24dp / α0.85 |
| `CameraScreen.kt` | Top Bar Row | 移除舊 PTF Exit ✕ 浮動按鈕（移入 BottomControlBar）；移除 `Icons.filled.Cameraswitch` / `Close` import |
| `CameraScreen.kt` | Bottom Box | 舊 `Box(weight=1f){Row{Switch, Shutter, CameraTabBar}}` → `BottomControlBar(...)` |
| `CameraScreen.kt` | `ShutterButtonSection` | `Modifier.size()` → `Modifier.requiredSize()` for `CircularProgressIndicator` + breathing-ring `Canvas`（修復 80dp zone 截斷導致橢圓偏移）|
| `components/BottomControlBar.kt` | NEW | 底部控制列 shell：Column(40dp Pill zone + 80dp Shutter zone + Spacer weight)；barBackground animateColorAsState #0D0D0D↔#1A1714；Gallery/PTF-Exit alpha cross-fade |
| `components/PillToggle.kt` | NEW | 128×28dp pill toggle；60×24dp sliding indicator offset animateDpAsState；PILL_TRACK_STROKE #555555 / PILL_INDICATOR_BG #4A4A4A；test tags TAG_PILL_PHOTO / TAG_PILL_PTF |
| `res/values/strings.xml` | — | 新增 6 個 accessibility strings：flash_cd / hdr_cd / settings_cd / help_cd / gallery_cd / flip_camera_cd |
| `.claude/skills/sv/ssot_context.md` | Core types table | v1.1 → v1.2：`SceneMode` → `CameraMode`（SceneMode 從未存在於 codebase，Cross-Review false positive 根因修正）|
| `sv_report.md` | Stage D | Cross-Review 結果補錄：false positive 分析 + ✅ PASS 裁決 |
| `.claude/PROGRESS.md` | Dashboard | Sprint Health + Current Day + Next Actions 全同步 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `AnimatedVisibility` → `animateFloatAsState + alpha()` | Column 內嵌 Box 導致 Kotlin overload 解析選中 `ColumnScope.AnimatedVisibility`，6 個編譯錯誤 | `BottomControlBar.kt` Gallery/PTF-Exit cross-fade |
| `Icons.Outlined.Help` → `Icons.AutoMirrored.Outlined.Help` | M3 deprecation warning（AutoMirrored variant 為正確替換） | `CameraScreen.kt` Help icon import |
| `requiredSize()` 取代 `size()` for ring overlays | BottomControlBar shutter zone = 80dp；ringBaseSize = 84/94dp，`size()` 被父容器截斷為橢圓；`requiredSize()` 忽略父約束 | `CameraScreen.kt:ShutterButtonSection` |
| ssot_context.md `SceneMode` → `CameraMode` | Cross-Review FAIL false positive：ssot_context v1.1 列 `SceneMode` 但該型別從未存在於 codebase；實際為 `domain/camera/CameraMode.kt` sealed interface | `.claude/skills/sv/ssot_context.md` v1.2 |
| Code commit 先於 cross-review | 第一次 cross-review FAIL：diff 指向舊 Docs commit（SESSION_HANDOFF）而非 P0 code；先原子化提交 `[Feature]` commit 再執行 cross-review，`HEAD~1..HEAD` 才正確指向 P0 diff | cross_review.sh workflow |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

本次 session 4 commits（HEAD~4）：

```
git diff HEAD~4 --stat
# 7 files changed, 458 insertions(+), 257 deletions(-)

git log --oneline HEAD~4..HEAD
# 7449572 [Docs] P0 Device Acceptance ✅
# a9e0dbb [Fix] BUG-SHUTTER-RING-OFFSET
# ca30246 [Docs] P0-TOPBAR+P0-BOTTOM SV CLOSED + ssot_context fix
# b2827fe [Feature] P0-TOPBAR + P0-BOTTOM
```

核心檔案 full diff：
```
git diff HEAD~4 -p app/src/main/java/.../camera/CameraScreen.kt
git diff HEAD~4 -p app/src/main/java/.../camera/components/BottomControlBar.kt
git diff HEAD~4 -p app/src/main/java/.../camera/components/PillToggle.kt
```

Sony XQ-BT52 실機驗收截圖：`tmp/shot_normal2.png` / `tmp/shot_ptf3.png`

UIAutomator 驗證摘要（Normal mode）：
- Flash [21,84][147,210] 48dp touch / 24dp icon
- HDR [147,84][273,210] 48dp touch / 24dp icon
- Settings [807,84][933,210] 48dp touch / 24dp icon
- Help [933,84][1059,210] 48dp touch / 24dp icon
- Pill [372,1797][708,1923] = 128dp ✅
- Gallery [85,1955][211,2081] 48dp / Shutter [446,1923][635,2112] 72dp / Flip [871,1955][997,2081] 48dp

</details>

---

## Session C 啟動清單

開新 session 後依序執行：

1. 讀取 `PROGRESS.md` 確認 Next Actions
2. 讀取 `plans/architecture/SSOT_v4_0.md` — SSOT 基準
3. 開工前確認 Active Blockers 優先級：BUG-MLKIT-DUAL-EMIT > BUG-SONY-ROTATION-CONST > BUG-PTF-HALO-SIZE > BUG-I7-FOCUS-RING-PERSIST

**P0-FOCUS 入口**：`EyeAfController.kt` — `process()` 函數加入 Guard 7
**P0-FPS 入口**：`CameraViewModel.kt` — `observeCompositionAdvice` rotation 處理 + Sony `imageInfo.rotationDegrees` = 0 bug
**Baseline 入口**：`CameraViewModel.kt` — `switchCamera()` / `flatMapLatest` flow 管理
