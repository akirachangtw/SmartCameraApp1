# Cueshot 工作交接 — 20260424_1338

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/src/main/java/.../data/sensor/DeviceSensorManagerImpl.kt` | `areSensorsAvailable()` | 僅要求 accelerometer，magnetometer 設為 optional |
| `app/src/main/java/.../data/sensor/DeviceSensorManagerImpl.kt` | `startOrientationTracking()` | magnetometer registerListener 加 null guard + Log.w |
| `app/src/main/java/.../data/sensor/DeviceSensorManagerImpl.kt` | `updateOrientationAngles()` | `getRotationMatrix` 失敗時改用 atan2(-accX, accZ) fallback 計算 roll/pitch |
| `app/src/main/java/.../presentation/camera/FocusIndicatorOverlay.kt` | Canvas when block | 刪除 State 2 `drawTrackingFrame()`、State 3 `drawLockedFrame()`、Secondary boxes forEach；全狀態統一走 `drawSearchingBase()` 圓環 |
| `app/src/main/java/.../presentation/camera/FocusIndicatorOverlay.kt` | `FocusTokens` | 移除 `GoldenFocus`、`TrackingSize`、`LockedSize`、`CLAMP_DURATION`、`OUTER_STROKE_*`、`INNER_STROKE_*` 等廢棄常數 |
| `app/src/main/java/.../presentation/camera/FocusIndicatorOverlay.kt` | import block | 移除 7 個廢棄 import（Path、Size、StrokeJoin、Animatable、animateDpAsState、keyframes 等） |
| `app/build.gradle.kts` | `defaultConfig` | 新增 `providers.exec("git rev-parse --short HEAD")` → `BuildConfig.GIT_COMMIT_HASH`（CC-compatible） |
| `app/src/main/java/.../presentation/settings/SettingsScreen.kt` | Detection Results 區段 | 新增 `DiagnosticInfoRow("Commit Hash", BuildConfig.GIT_COMMIT_HASH)` |

### Commits（本 Session）
| Hash | 類型 | 說明 |
|------|------|------|
| `8f520cd` | `[D8-STABILIZE-FIX]` | I1 Sensor Fallback + I2 FocusIndicator 焦土（主要代碼） |
| `7ebe293` | `[Docs]` | PROGRESS.md 歸檔 |
| `2fd71e3` | `[Docs]` | Session handoffs 歸檔（20260423_2057 + 20260423_2249） |
| `ac73276` | `[Build]` | BuildConfig.GIT_COMMIT_HASH 自動注入 |

---

## 當前阻塞點

無。

---

## 下一步待辦

1. **I3/I4 偵查**：讀取 `LevelIndicator.kt` 完整實作，確認 `tiltOffset` 計算邏輯與 `isLevel` 色彩切換條件（是否符合 SSOT v3.3 §2.4）
2. **I5 偵查**：`HDRWarningOverlay.kt` — 確認是否有 hardcode 中文字串，應改用 `stringResource(R.string.hdr_warning)`
3. **I6 調查**：`LiveCameraSource.kt` `focusOnPoint()` L690 — MeteringPoint 座標邊界防禦（兩台機均出現 `IllegalArgumentException: None of the specified AF/AE/AWB MeteringPoints is supported`）
4. **D8-VERIFY P3 實機驗測**：Pixel 9a + Sony XQ-BT52 安裝 APK（commit `ac73276`），驗測 Portrait Halo / PTF 引力場 / EdgeWarmth roll ≥3°
5. **D8-VERIFY P6/P7 CSO 決策**：圖片瀏覽入口（功能缺失 vs 推遲至 D13）+ 頂部工具列圖示功能確認

---

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| I2 選擇選項 B（焦土）：State 2/3 全刪，全狀態走圓環 | 最符合 SSOT v3.3 §2.4 Ambient UI 規範；HaloGuidanceLayer 已承擔追蹤視覺 | `FocusIndicatorOverlay.kt` |
| `BuildConfig.GIT_COMMIT_HASH` 使用 `providers.exec()` 而非 `Runtime.getRuntime().exec()` | Configuration Cache compatible（Gradle 8.11+），消除 CC warning | `app/build.gradle.kts` |
| Commit Hash 放入 Detection Results 而非獨立欄位 | 診斷用途，與 App Version / Cache Version 同一區塊語意一致 | `SettingsScreen.kt` |

---

## 🎯 Context 指標針（Context Pointers）

（新 Session Agent 請主動執行以下指令，**嚴禁依賴歷史記憶**）

1. **確認本次 Session 變更**：
   ```bash
   git diff HEAD~4 --stat
   ```
   預期：`DeviceSensorManagerImpl.kt` + `FocusIndicatorOverlay.kt` + `build.gradle.kts` + `SettingsScreen.kt`

2. **核心檔案最新狀態**：
   - Read `.claude/PROGRESS.md`（確認 D8-STABILIZE-FIX ✅ DONE 狀態）
   - Read `CLAUDE.local.md`（ADB device ID）

3. **I3/I4 偵查起點**：
   ```bash
   cat app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/overlay/LevelIndicator.kt
   ```

4. **I5 偵查起點**：
   ```bash
   grep -n "HDR\|hdr\|Text(" app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/overlay/HDRWarningOverlay.kt
   ```

5. **I6 調查起點**（MeteringPoint 邊界）：
   ```bash
   sed -n '680,710p' app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt
   ```

6. **Settings Diagnostics 確認**（BuildConfig 注入驗證）：
   ```bash
   grep -n "GIT_COMMIT_HASH" app/build/generated/source/buildConfig/device/debug/com/akirachang/ai/photographer/BuildConfig.java
   ```

---

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（最高憲法）
- `.claude/PROGRESS.md` — 當前狀態（D8-STABILIZE-FIX ✅ DONE，D8-VERIFY P3/P6/P7 待驗測）
- `CLAUDE.local.md` — ADB device ID（Pixel 9a: `59131JEBF17244`，Sony: `HQ616R2713`）
- 本交接文件

---

## 不需帶入的 Context

- I1 magnetometer 問題完整偵察過程（三層修復已實作閉環）
- I2 FocusIndicatorOverlay State Machine 分析與三選項討論（選項 B 已實作完成）
- D8-STABILIZE-FIX 全部 logcat 原始輸出
- BuildConfig providers.exec vs Runtime.exec 技術討論（結論已落地 `ac73276`）
- Session Token 使用率討論
