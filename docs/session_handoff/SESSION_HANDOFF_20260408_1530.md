# Cueshot 工作交接 — 2026-04-08

## 本次完成的變更
| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `.claude/CLAUDE.md` | 全文 | gstack v1.3 升級：最高憲法、PROGRESS.md SOP、KAIROS D2/D6/D10/D13/D15、Skills 表格 |
| `.claude/PROGRESS.md` | 全文 | D0-D15 Sprint SSOT 建立；追加 D0.1 編譯警告清創手術 4 Tickets |
| `.claude/skills/sv/SKILL.md` | 全文 | 升級三階段：Deterministic Gate + Semantic Review + Architecture Compliance |
| `.claude/skills/qa/SKILL.md` | 全文 | 升級四步驟：Build → Unit Tests → Warning Review → PROGRESS.md 更新 |
| `.claude/skills/compact/SKILL.md` | Prompt | 新增儲存路徑 `docs/session_handoff/SESSION_HANDOFF_YYYYMMDD_HHMM.md` |
| `.claude/skills/handoff/SKILL.md` | Description + paths | 儲存路徑改為 `docs/handoff/HANDOFF_YYYYMMDD_HHMM.md` |
| `.claude/skills/dream/SKILL.md` | 全文 | 新建：KAIROS 三層回顧（錯誤挖掘 + 決策審計 + SSOT 漂移掃描）|
| `.claude/skills/normal/SKILL.md` | 全文 | 新建：退出臥底模式 |
| `.claude/skills/uc/SKILL.md` | 全文 | 新建：臥底模式零廢話純 Diff 輸出 |
| `.claude/skills/status/SKILL.md` | 全文 | 新建：Session token 使用估算 + 健康度報告 |
| `devspec/99-lifecycle.md` | 全文 | **已刪除** — 由 `/status` Skill 取代 |
| `app/build.gradle.kts` | dependencies | 移除 OpenCV dep + ENABLE_OPENCV/OPTICAL_FLOW flags；ABI filter 只留 arm64-v8a/x86_64 |
| `app/.../MainActivity.kt` | onCreate | OpenCV init → ML Kit pre-warm skeleton；`isOpenCvLoaded` → `isMlKitReady` |
| `app/.../data/opencv/OpticalFlowTracker.kt` | 全檔 | **已刪除** |
| `app/.../data/opencv/OpticalFlowConfig.kt` | 全檔 | **已刪除** |
| `app/.../data/repository/CameraRepositoryImpl.kt` | startImageAnalysis / overrides | 移除 5 OpticalFlow methods + ImuCompensator 參數 |
| `app/.../data/source/camera/SmartCameraAnalyzer.kt` | 全文 | Dumb Pipe 重寫：純 ML Kit，移除 OpticalFlow/ImuCompensator/opencv |
| `app/.../data/source/camera/ImageProxyExtensions.kt` | toGrayMat/toBgrMat | 替換為 toBitmap() (純 Android YUV→NV21→JPEG→Bitmap) |
| `app/.../data/source/camera/LiveCameraSource.kt` | GuidelineAnalyzer calls | 移除 3x `.stop()` + import |
| `app/.../data/source/ml/ImageAnalyzer.kt` | 全文 | 純 ML Kit emulator path，移除 opencv/OpticalFlow |
| `app/.../data/source/analysis/GuidelineAnalyzer.kt` | 全文 | 替換為 stub（只關閉 imageProxy）|
| `app/.../domain/config/FeatureFlags.kt` | 全文 | 移除 6 個 OpenCV/OpticalFlow flags |
| `app/.../domain/repository/CameraRepository.kt` | interface | 移除 5 OpticalFlow interface methods |
| `app/.../domain/usecase/AnalyzeCompositionUseCase.kt` | invoke | 清空 v2.4.0 硬編碼中文邏輯，骨架返回 `CompositionAdvice.None` (D1 TODO) |
| `app/.../presentation/camera/CameraState.kt` | data class / events | 移除 trackedPoint 系列欄位 + SetPreviewSize event |
| `app/.../presentation/camera/CameraViewModel.kt` | 多個函數 | 移除 GuidelineAnalyzer、setupOpticalFlowTracking、observePreviewFor* 等 |
| `app/.../presentation/camera/CameraScreen.kt` | onSizeChanged | 移除 SetPreviewSize dispatch + GuidelineOverlay call |
| `app/.../presentation/camera/FocusIndicatorOverlay.kt` | FocusTokens | AuroraBlue 移除；新增 CueshotAmber(#D4A030) + CueshotGreen(#50C864) |
| `app/.../util/CoordinateMapper.kt` | 全文 | 移除 opencv.core.Point；回傳改為 Pair<Float, Float> |
| `app/.../camera/overlay/EdgeWarmthLayer.kt` | 全文 | 新建 placeholder Composable (D1 實作) |
| `app/.../camera/overlay/HaloIndicator.kt` | 全文 | 新建 placeholder Composable (D6 實作) |
| `app/.../camera/overlay/SceneTransitionEffect.kt` | 全文 | 新建 placeholder Composable (D3 實作) |

## 當前阻塞點
- 無

## 下一步待辦
1. **D0.1-T1**: 執行 `./gradlew assembleDeviceDebug 2>&1 | grep -i warning` 取得完整 Warning 清單
2. **D0.1-T2**: 分類 Warning（Deprecation / Unused / Nullability / Cast）並逐一修復
3. **D0.1-T3**: 重新編譯確認 Warning = 0 new，更新 PROGRESS.md D0.1 Tickets → ✅ DONE
4. **D1-T1**: `CameraViewModel` 確認 CameraX preview 正常啟動 + Tap-to-Focus 流程（`handleFocusRequest()`）
5. **D1-T2**: 實作 `EdgeWarmthLayer.kt` — IMU pitch → amber gradient（`CueshotAmber #D4A030`）
6. **D1-T3**: `CameraViewModel.observeOrientation()` 新增 pitch > 35° → FoodMode 切換邏輯

## 關鍵決策 (本次對話中做出的)
| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `/compact` 與 `/handoff` 不合併 | 兩者受眾與目標不同：compact=Context Firewall / handoff=Knowledge Transfer | `.claude/skills/` |
| `/compact` 儲存至 `docs/session_handoff/` | 與 99-lifecycle.md 歷史路徑一致，新 Session 容易找到 | `.claude/skills/compact/SKILL.md` |
| `/handoff` 儲存至 `docs/handoff/` | 與 compact 路徑分開，避免混淆 | `.claude/skills/handoff/SKILL.md` |
| `devspec/99-lifecycle.md` 刪除 | `/status` Skill 已取代其功能；減少重複文件 | `devspec/` |
| D0.1 追加「編譯警告清創手術」 | D0 手術後須驗證 0 new Warning 才算完成 | `PROGRESS.md` |

## 需帶入新對話的 Context
- `.claude/CLAUDE.md`
- `.claude/PROGRESS.md`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`
- `app/src/main/java/com/akirachang/ai/photographer/camera/overlay/EdgeWarmthLayer.kt`

## 不需帶入的 Context
- OpenCV 移除過程（已完成，無殘留）
- Skills 安裝細節（/sv /qa /uc /normal /dream /compact /status /handoff 均已就位）
- `/compact` vs `/handoff` 合併評估（已決策：不合併）
- `devspec/99-lifecycle.md` 內容（已刪除，/status 取代）
