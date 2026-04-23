# Cueshot 工作交接 — 20260423_2057

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/src/main/java/.../data/source/camera/LiveCameraSource.kt` | `initOrientationListener()` L949 | BUG-1 修復：`context.display` → `DisplayManager.getDisplay(DEFAULT_DISPLAY)` + 新增兩個 import |
| `.claude/PROGRESS.md` | Blocking Issues + Current Day | BUG-1 封板，D8-VERIFY-P3 UNBLOCKED |
| `docs/session_handoff/SESSION_HANDOFF_20260421_1619.md` | 新建 | D8-VERIFY 交接存檔 |
| `docs/session_handoff/SESSION_HANDOFF_20260422_2309.md` | 新建 | BUG-1 確認交接存檔 |
| `docs/session_handoff/SESSION_HANDOFF_20260423_1714.md` | 新建 | STAB-01 HOTFIX + STAB-02 驗測報告 |

**Commits（本次對話產生 3 個）**：
- `f90834f` [Fix] D8-HOTFIX-01: initOrientationListener crash on Android 12+
- `b21bc60` [Docs] D8-HOTFIX-01: PROGRESS.md BUG-1 封板
- `0eff91b` [Docs] STAB-01/02: session handoffs 歸檔

---

## STAB-02 實機驗測結果（Sony XQ-BT52，已完成）

| Layer | 結果 | 備註 |
|-------|------|------|
| L1 相機預覽 | ✅ PASS | Thermal=SEVERE，FPS 21~22 |
| L2 Edge Warmth | ❌ FAIL | 無琥珀金漸層 |
| L3 Halo 圓環 | ✅ PASS | 有非預期四角框 |
| L4 PTF 切換 | ✅ PASS | 有非預期四角框 |
| L5 水平儀 | 🟡 PARTIAL | 位置偏底部、傾斜保持綠色、HDR 中文違規 |

---

## 當前阻塞點

無（BUG-1 已修復），但 STAB-03 有 7 個待修問題。

---

## STAB-03 問題清單（依優先序）

| # | 問題 | 嚴重度 | 根因（已確診） |
|---|------|--------|--------------|
| **I1** | Edge Warmth 完全失效 | 🔴 High | `DeviceSensorManagerImpl` sensor 管線在 Sony 上**完全靜默**（無任何 log 輸出），`observeOrientation()` 呼叫鏈在 CameraViewModel 層即中斷，未到達 `areSensorsAvailable()`。需加入口 Log 確認後再修復。 |
| **I2** | 綠色四角對焦框殘留（L3/L4）| 🔴 High | 非 SSOT 圓形規格，舊版 AF 指示器殘留，來源未偵查 |
| **I3** | LevelIndicator 位置偏底部 | 🟡 Medium | `tiltOffset` 計算或 Z-order 問題 |
| **I4** | LevelIndicator 傾斜保持綠色（應琥珀金）| 🟡 Medium | `isLevel` 邏輯異常 |
| **I5** | HDR 警告中文 hardcode | 🔴 High | i18n 違規（CLAUDE.md Rule 9），來源疑為 `HDRWarningOverlay.kt` |
| **I6** | `focusOnPoint L690` exception 持續噴出 | 🟡 Medium | `future.get()` 在熱節流下失敗，非功能阻塞 |
| **I7** | Sony 全程 Thermal=SEVERE | 🟡 Medium | 環境因素；D12 LIMITED tier 降頻可緩解 |

---

## I1 深度診斷結論（本次新增，關鍵）

**訊號：Sony XQ-BT52 完全無 log**

```
期望路徑：
CameraViewModel.observeOrientation()
  → orientationSensorManager.areSensorsAvailable()  ← 此行 log 從未出現
  → startOrientationTracking()
  → state.deviceRoll 更新
  → EdgeWarmthLayer 渲染

實際：observeOrientation() 以上全部靜默
```

**修復升級方向**：
1. 先在 `CameraViewModel.observeOrientation()` 入口加 `Log.i`，確認是否被呼叫
2. 確認後再修 `DeviceSensorManagerImpl.areSensorsAvailable()` — 改為只要求 accelerometer（magnetometer 降為 optional）
3. 若 step 1 log 也不出現，問題在 Hilt 注入或 `init` block 執行順序

---

## 下一步待辦（STAB-03）

1. **I1 診斷確認**：在 `CameraViewModel.kt` `observeOrientation()` L464 入口加 `Log.i(TAG, "observeOrientation() called")`，build + 裝置驗證
2. **I1 修復**：`DeviceSensorManagerImpl.kt` `areSensorsAvailable()` L86 — 改為 `return hasAccelerometer`（magnetometer optional）
3. **I2 偵查**：
   ```bash
   grep -rn "FocusIndicator\|focusRect\|cornerLength\|drawRect.*green\|strokeWidth" app/src/main/java/
   ```
4. **I5 修復**：
   ```bash
   grep -rn "HDR\|hdr" app/src/main/res/values/strings.xml app/src/main/java/
   ```
   找到中文 hardcode 字串並提取至 `strings.xml`

---

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| BUG-1 採用 DisplayManager 方案 B | `@Singleton` scope 不可注入 Activity Context | `LiveCameraSource.kt` |
| STAB-02「當場記錄不當場修」| CSO 指示取得完整實機數據後再 STAB-03 排程 | 驗測流程 |
| I1 修復需先加診斷 Log | Sony 訊號為「完全無 log」，比預期更上游，需先確認呼叫鏈層級 | `CameraViewModel.kt`、`DeviceSensorManagerImpl.kt` |

---

## 🎯 Context 指標針 (Context Pointers)

（新 Session Agent 請主動執行以下指令，**嚴禁依賴歷史記憶**）

1. **確認 git 狀態**（本次 3 commits 已全數提交，working tree clean）：
   ```bash
   git log --oneline -5
   git diff --stat HEAD
   ```

2. **核心檔案最新狀態**：
   - Read `.claude/PROGRESS.md`
   - Read `CLAUDE.local.md`（ADB device ID）

3. **I1 修復目標檔案**：
   - Read `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt` L464-478（`observeOrientation()` 完整函數）
   - Read `app/src/main/java/com/akirachang/ai/photographer/data/sensor/DeviceSensorManagerImpl.kt` L55-113（`areSensorsAvailable()` + `startOrientationTracking()`）

4. **I2 偵查指令**：
   ```bash
   grep -rn "FocusIndicator\|cornerLength\|focusRect" app/src/main/java/
   ```

5. **I5 偵查指令**：
   ```bash
   grep -rn "HDR\|hdr_warning\|曝光" app/src/main/res/ app/src/main/java/
   ```

---

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（最高憲法）
- `.claude/PROGRESS.md` — 當前狀態
- `CLAUDE.local.md` — ADB device ID（Pixel 9a `59131JEBF17244`、Sony `HQ616R2713`）
- 本交接文件（STAB-03 問題清單 + I1 深度診斷）

## 不需帶入的 Context

- BUG-1 修復過程（已 commit `f90834f`，完成）
- STAB-02 各 Layer Logcat 原始數據（結論已彙整）
- Sony 熱節流詳細數據（SOC 88°C，環境因素，已記錄）
- 六條 grep 偵察原始輸出（結論已彙整至 I1 根因）
- OrientationEventListener vs OrientationSensorManager 兩套系統分析（結論已確認，兩者獨立，EdgeWarmth 依賴後者）
