# Cueshot 工作交接 — 20260423_1714

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/src/main/java/.../data/source/camera/LiveCameraSource.kt` | `initOrientationListener()` L949 | 修復 BUG-1：`context.display` → `context.getSystemService(DisplayManager::class.java)?.getDisplay(Display.DEFAULT_DISPLAY)`（方案 B，Singleton-safe）|
| `app/src/main/java/.../data/source/camera/LiveCameraSource.kt` | imports | 新增 `import android.hardware.display.DisplayManager` + `import android.view.Display` |
| `.claude/PROGRESS.md` | Blocking Issues 表 | BUG-1 封板，D8-VERIFY-P3 標記 UNBLOCKED |

**⚠️ 本次變更尚未 commit** — 需在 STAB-03 開始前或單獨執行 git commit。

---

## STAB-02 實機驗測結果（Sony XQ-BT52）

| Layer | 項目 | 結果 |
|-------|------|------|
| L1 相機預覽 | Camera bind / FPS / ML Kit 管線 | ✅ PASS（Thermal=SEVERE，FPS 21~22） |
| L2 Edge Warmth | 琥珀金漸層 25~30° 傾斜 | ❌ FAIL — 無任何暖光 |
| L3 Halo 圓環 | 人臉偵測 + 琥珀金 Halo | ✅ PASS（但有非預期四角框） |
| L4 PTF 切換 | Tab 切換 + ✕ 退出 | ✅ PASS（但有非預期四角框） |
| L5 水平儀 | 水平線顏色 + 位置 | 🟡 PARTIAL（位置偏底部，傾斜色彩邏輯異常，HDR 中文違規） |

---

## 當前阻塞點

無阻塞（BUG-1 已修復），但有 STAB-03 待修復問題。

---

## STAB-03 待修復問題清單

| # | 問題 | 嚴重度 | 根因 |
|---|------|--------|------|
| **I1** | Edge Warmth 完全失效 | 🔴 High | `DeviceSensorManagerImpl.areSensorsAvailable()` 強制要求 accelerometer AND magnetometer；Sony magnetometer 不可用（CamX HAL 衝突），Flow 從未 emit，`deviceRoll` 永遠=0f |
| **I2** | 綠色四角對焦框殘留（L3/L4） | 🔴 High | 非 SSOT 圓形規格，殘留舊版 AF 指示器 UI 元件，需偵查來源 |
| **I3** | LevelIndicator 位置偏底部 | 🟡 Medium | `tiltOffset` 計算或 Z-order 問題，待偵查 |
| **I4** | LevelIndicator 傾斜時保持綠色（應琥珀金）| 🟡 Medium | `isLevel` 判斷邏輯或色彩切換條件異常 |
| **I5** | HDR 警告使用中文 hardcode | 🔴 High | i18n 違規（CLAUDE.md Rule 9），需找到 HDRWarningOverlay 或對應元件 |
| **I6** | `focusOnPoint L690` exception | 🟡 Medium | `future.get()` 在熱節流狀態下拋出，非功能阻塞 |
| **I7** | Sony 全程 `Thermal=SEVERE` | 🟡 Medium | 環境因素；D12 降級策略（LIMITED tier）可緩解，目前尚未實作 |

---

## 下一步待辦

### 優先：commit BUG-1 修復
```bash
git add app/src/main/java/.../data/source/camera/LiveCameraSource.kt
git commit -m "[Fix] D8-HOTFIX-01: initOrientationListener getDisplay() crash — use DisplayManager API"
git add .claude/PROGRESS.md
git commit -m "[Docs] D8-HOTFIX-01: PROGRESS.md — BUG-1 封板 UNBLOCKED"
```

### STAB-03 修復（依優先序）

1. **I1 修復**：`DeviceSensorManagerImpl.kt` `areSensorsAvailable()` — 改為只要求 accelerometer，magnetometer 降為 optional（方案 A）
   ```kotlin
   // 修改前：return hasAccelerometer && hasMagnetometer
   // 修改後：return hasAccelerometer  // magnetometer optional，用於 azimuth only
   ```

2. **I5 修復**：找到 HDR 中文 hardcode 來源（疑為 `HDRWarningOverlay.kt`），改為 `stringResource(R.string.hdr_warning)`

3. **I2 偵查**：grep 找出四角對焦框來源
   ```bash
   grep -rn "FocusIndicator\|focusRect\|cornerLength\|drawRect.*focus\|strokeWidth" app/src/main/java/
   ```

4. **I3/I4 偵查**：讀取 `LevelIndicator.kt` 完整實作，確認 `tiltOffset` 計算與 `isLevel` 色彩邏輯

---

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| BUG-1 採用方案 B（DisplayManager）| `LiveCameraSource` 為 `@Singleton`，不可注入 Activity Context，方案 B 無 Context 洩漏風險 | `LiveCameraSource.kt` |
| I1 修復採方案 A（只要求 accelerometer）| magnetometer 僅用於 azimuth，與 roll/EdgeWarmth 完全無關；最小改動，零副作用 | `DeviceSensorManagerImpl.kt` |
| STAB-02 驗測「當場記錄不當場修」| CSO 指示：取得完整實機數據後再做 STAB-03 排程決策 | 驗測流程 |

---

## 🎯 Context 指標針 (Context Pointers)

（新 Session Agent 請主動執行以下指令，**嚴禁依賴歷史記憶**）

1. **確認 BUG-1 是否已 commit**：
   ```bash
   git diff --stat HEAD
   git log --oneline -5
   ```

2. **核心檔案最新狀態**：
   - Read `.claude/PROGRESS.md`（確認 D8-VERIFY-P3 / STAB 狀態）
   - Read `CLAUDE.local.md`（ADB device ID）

3. **I1 修復目標檔案**：
   - Read `app/src/main/java/com/akirachang/ai/photographer/data/sensor/DeviceSensorManagerImpl.kt`（L86-113，`areSensorsAvailable()` + `startOrientationTracking()`）

4. **I2 偵查起點**：
   ```bash
   grep -rn "FocusIndicator\|focusRect\|cornerLength" app/src/main/java/
   ```

5. **I5 偵查起點**：
   ```bash
   grep -rn "HDR\|hdr" app/src/main/java/ app/src/main/res/
   ```

---

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（最高憲法）
- `.claude/PROGRESS.md` — 當前狀態（D8-VERIFY-P3 UNBLOCKED，STAB-03 待開始）
- `CLAUDE.local.md` — ADB device ID（Pixel 9a `59131JEBF17244`、Sony `HQ616R2713`）
- `app/src/main/java/.../data/sensor/DeviceSensorManagerImpl.kt` — I1 修復目標
- 本交接文件（STAB-03 問題清單）

## 不需帶入的 Context

- BUG-1 偵查過程（根因已確診，修復已實作，待 commit）
- STAB-02 各 Layer Logcat 原始數據（結論已彙整至上方表格）
- Sony 熱節流 Logcat 詳細數據（結論：SOC 88°C，SEVERE，屬環境因素）
- 六條 grep 偵察過程（結論已彙整至 I1 根因分析）
- OrientationEventListener vs OrientationSensorManager 兩套系統的詳細分析（結論已確認）
