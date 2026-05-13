# SV Report — P0-TOPBAR + P0-BOTTOM

**Date**: 2026-05-14
**Branch**: feature/cueshot-mvp
**Scope**: Phase0 Session B — Top Bar Scrim Overlay + Bottom Control Bar Shell (PillToggle + ShutterRow)

---

## 階段 A：確定性驗證

| 項目 | 結果 | 備註 |
| ---- | ---- | ---- |
| Compile | ✅ PASS | assembleDeviceDebug 0 errors, 0 warnings |
| Lint | ✅ PASS | 0 new warnings; baseline unchanged |
| Scope | ✅ PASS | 見下方檔案清單 |

**授權修改範圍：**

- `CameraScreen.kt` — Top Bar Row 重建（Scrim overlay 圖示佈局）+ Bottom Box → BottomControlBar
- `components/PillToggle.kt` — NEW: 128×28dp pill toggle (Layout Spec v2.2 §5)
- `components/BottomControlBar.kt` — NEW: 底部控制列 shell (Layout Spec v2.2 §4)
- `res/values/strings.xml` — 新增 6 個 accessibility strings（合理連帶修改）
- `.claude/PROGRESS.md` — /qa 授權更新 + MD-linter 格式修正

---

## 階段 A.5：16KB ELF 對齊

| 項目 | 結果 |
| ---- | ---- |
| ELF Verification | ✅ ALIGNED |
| zipalign | ⚪ SKIP (build-tools < 35.0.0-rc3，NOTICE 級，不影響 Debug) |

---

## 階段 B：語意審查

| 項目 | 結果 | 備註 |
| ---- | ---- | ---- |
| Orphan (TODO/FIXME/HACK) | ✅ CLEAN | 0 hits across 3 modified/new files |
| Mock / Placeholder | ✅ CLEAN | 無 Color.Gray 佔位、無硬編碼預設框 |
| Gallery `onClick = { }` | ✅ AUTHORIZED | Ticket Scope Ban 明文授權骨架；非 TODO 殘骸 |
| 舊 CameraTabBar 殘留 | ✅ CLEAN | import 已移除，CameraScreen.kt 無任何 CameraTabBar 引用 |
| 舊 Icons.Default.Close / Cameraswitch | ✅ CLEAN | CameraScreen.kt import 已清除 |
| None/Empty 透明 | ✅ CLEAN | galleryAlpha/flipAlpha=0 → 按鈕不可見且 enabled=false |

---

## 階段 C：架構合規

| 項目 | 結果 | 備註 |
| ---- | ---- | ---- |
| UDF 合規 | ✅ COMPLIANT | PillToggle/BottomControlBar 均為無狀態 Composable；State 從 CameraViewModel → CameraScreen → BottomControlBar，無逆向 |
| Repository/UseCase 入侵 | ✅ CLEAN | 新增檔案無任何 Repository/UseCase 引用 |
| SSOT 命名漂移 | ✅ ALIGNED | `PillToggle`、`BottomControlBar`、`galleryAlpha`、`flipAlpha`、`barBackground`、`PILL_INDICATOR_BG` — 均無 EdgeWarmth / WarmGradient 等禁用命名 |
| Scrim 規格 | ✅ ALIGNED | Scrim 保留原有 #000 α0.22 → transparent，120px 高度（AmbientConstants.SCRIM_HEIGHT_PX） |
| Pill 色值 | ✅ ALIGNED | Track #555555 / Indicator #4A4A4A / Selected #CCCCCC / Unselected #666666 — 完全對齊 Layout Spec v2.2 §5.2 |
| Bottom Bar 色值 | ✅ ALIGNED | Photo #0D0D0D / PTF #1A1714，300ms FastOutSlowInEasing — 對齊 Layout Spec v2.2 §6 |
| Icon 規格 | ✅ ALIGNED | 頂部圖示 24dp / 48dp touch target / α0.85；底部 28dp — 符合 §3.2 |
| Rule 14 (remember 置頂) | ✅ COMPLIANT | PillToggle.kt 所有 animateXxx 在函數頂部；BottomControlBar.kt 同理 |

---

## 已知授權骨架（不阻斷 SV）

| 項目 | 授權依據 |
| ---- | -------- |
| Gallery `onClick = { }` | Ticket Scope Ban §3：「勿接 Gallery 實際功能」 |
| Help `onClick = { }` | Ticket Scope Ban §3：「勿接 Settings/Help 導航實際功能」 |

---

## 階段 D：Cross-Review 路由

- codex CLI: ✅ 可用
- ssot_context.md: ✅ 存在

### Cross-Review 結果

初審 FAIL（false positive 確認，已解決）

Codex 標記 `[severity: high] [layer: arch]`：`CameraMode` 使用於 `CameraScreen.kt`/`BottomControlBar.kt`/`PillToggle.kt`，但 `ssot_context.md` v1.1 的核心型別清單列出 `SceneMode` → 判定 SSOT drift。

根因分析：`SceneMode` 從未存在於 codebase（`grep -rn SceneMode` = 0 hits）。`ssot_context.md` 殘留舊草稿名稱。實際 SSOT 定義型別為 `domain/camera/CameraMode.kt`（sealed interface：Normal / PassToFriend），已被 `CameraViewModel`、`CameraScreen`、所有 Composable 一致使用，無命名漂移。

修正動作：`ssot_context.md` v1.1 → v1.2：`SceneMode` → `CameraMode`（domain/camera）。

最終裁決：✅ PASS（false positive resolved）
