# Cueshot SSOT Context for Cross-Review

**用途**: Codex CLI cross_review.sh 專用 — 提供架構審查所需的最小 SSOT 上下文
**維護頻率**: 每 Sprint 更新一次，或 SSOT 版本變更時同步
**當前 SSOT 版本**: v4.0.2 (Session 4 Full Revision + Session 5 D-13 Patch)
**搭配佈局文件**: Layout Spec v2.3

---

## 1. 架構層映射 (Clean Architecture Layers)

```
domain/   → 純 Kotlin，零 Android 依賴
  ├── model/          CompositionResult, SceneMode, DeviceTier, EyeAfDecision
  ├── rule/           CompositionRule 介面及所有實作
  ├── engine/         PriorityEngine (SSOT §4.2)
  └── usecase/        EvaluateCompositionUseCase

data/     → Android 框架依賴，實作 domain 介面
  ├── camera/         CameraDataSource (Real/Fake), CameraRepository
  ├── sensor/         IMU, ML Kit Face/Object Detection wrappers
  └── di/             Hilt Modules

presentation/ → Jetpack Compose + ViewModel
  ├── camera/                    CameraViewModel, CameraScreen, FocusIndicatorOverlay (MVP 關閉)
  ├── camera/focus/              TapFocusController (P05-T3: 狀態機 + CameraX 接線)
  ├── camera/overlay/            PitchTractionLayer
  ├── camera/components/         HaloGuidanceLayer, TapFocusRing (P05-T2)
  ├── camera/components/overlay/ GridOverlay
  ├── controls/                  ShutterButton, PillToggle, PTF controls
  └── postCapture/               ScoreCard, DOF pipeline UI
  注意：實際 package 路徑以 camera/ 為根，無獨立 presentation/overlay/ 層
```

**依賴方向鐵律**: presentation → domain ← data（domain 不依賴任何人）

---

## 2. 核心型別清單 (SSOT-Defined Types)

以下型別名稱由 SSOT v4.0.2 定義，任何 diff 中出現非此名稱的同功能型別即為 **Model Drift**：

| SSOT 型別 | 所在層 | 用途 |
|-----------|--------|------|
| `CompositionResult` | domain/model | 構圖評估結果（取代舊 CompositionScore） |
| `PriorityEngine` | domain/engine | 優先級公式 SSOT §4.2，獨立 class |
| `CameraMode` | domain/camera | Sealed interface: Normal / PassToFriend (Food / Portrait reserved D9+) |
| `DeviceTier` | domain/model | Enum: FULL / LIMITED / LEGACY |
| `CompositionRule` | domain/rule | 介面，所有規則實作之 |
| `PitchTractionLayer` | presentation/overlay | 俯仰角引導 overlay（P0-NAMING 遷移自 EdgeWarmthLayer） |
| `HaloGuidanceLayer` | presentation/overlay | 主體光環 overlay |
| `EyeAfDecision` | domain/model | Eye-AF 決策封裝 |
| `TapFocusState` | presentation/camera/components | Sealed class: Idle/Focusing/Focused/Failed/Locked (P05-T2) |
| `TapFocusRing` | presentation/camera/components | 單圈 60dp 渲染 Composable (P05-T2, SSOT 附錄 A) |
| `TapFocusController` | presentation/camera/focus | Tap 狀態機 + CameraX AF 接線 (P05-T3) |

**架構狀態標註**:
- `FocusIndicatorOverlay` — Eye-AF 視覺系統，MVP 階段由 flag 關閉（光學 AF 保留）。檔案保留不刪，非 dead code。
- `TapFocusRing` + `TapFocusController` — Strangler Fig 新建，取代 Eye-AF 視覺層。

**零容忍**: 若 diff 中出現 `CompositionScore`, `EyeAfPolicyOutput`, `SceneType` 等非 SSOT 名稱 → FAIL

---

## 3. 禁止修改檔案 (Scope Ban — 全局)

以下檔案在無明確 Intent 授權的情況下，任何修改皆為 Scope Creep：

| 檔案 | 禁止範圍 | 理由 |
|------|---------|------|
| `CameraViewModel.kt` | **全面禁止新增函數** | 2457 行 8.2× 超標，CSO P05 裁決 |
| `EyeAfController.kt` | **全面禁止修改** | 光學 AF 保留運作，P05 §1 裁決 |
| `FocusIndicatorOverlay.kt` | **禁止刪除** | T2 參考 + 未來評估保留 |
| `AndroidManifest.xml` | 除權限相關 Ticket | — |
| `build.gradle.kts` | 除依賴版本升級 Ticket | — |
| `Hilt Module` 綁定 | 除 DI wiring Ticket | — |
| 任何 `*Test.kt` 既有邏輯 | 只允許新增，不允許修改既有斷言 | — |

---

## 4. 硬體與系統約束 (Hardware Constraints)

| 約束 | 規則 | 違反後果 |
|------|------|---------|
| ML Kit 互斥 | Face Detection 與 Object Detection 不可同時開啟 | 500ms hysteresis 切換 |
| CameraX 零重綁 | PTF 切換不 rebind camera, 不 close ML Kit | 黑屏 crash |
| Pitch Traction 60fps | 任何裝置等級都必須保證 PT 60fps（IMU 驅動） | 用戶感知卡頓 |
| IMU 頻率脫鉤 | IMU 200Hz 採樣 → 降頻至 UI 60fps 消費 | 主線程阻塞 |
| DOF 後置管線 | ML Kit Selfie Seg + StackBlur，拍後處理 | CameraX rebinding 黑屏 |
| 前鏡頭 Fixed-Focus | 只追求視覺成功，不等物理對焦 | AF 超時死鎖 |
| VCM 馬達冷卻 | Eye-AF 呼叫間隔 ≥ 2-3 秒 | 馬達過熱 |
| Focus 圓環尺寸 | `FOCUS_RING_SIZE_DP` = 60dp | 圓環外徑（SSOT A.1） |
| Focus 圓環線寬 | `FOCUS_RING_STROKE_DP` = 1.5dp | 邊框寬度（SSOT A.1） |
| Focus AF 超時 | `FOCUS_AF_TIMEOUT_MS` = 2000ms | 超時 → Failed 狀態（SSOT A.2.1） |
| Focus fadeout | `FOCUS_FADEOUT_MS` = 500ms | drift 後淡出（SSOT A.2.2.1） |
| Focus Failed | `FOCUS_FAILED_MS` = 800ms | Failed 振盪時長（SSOT A.2.3） |
| Focus 長按閾值 | `FOCUS_LONG_PRESS_MS` = 500ms | 觸發 AE/AF Lock（SSOT A.2.4） |
| Focus drift 閾值 | `FOCUS_DRIFT_THRESHOLD_DEG` = 15° | Focused → fadeout → Idle（SSOT A.2.2.1） |
| 對齊統一門檻 | `ALIGNED_THRESHOLD_DEG` = 5° | 三通道共用（SSOT §2.0） |
| Level 漸入區間 | `LEVEL_FADE_RANGE_DEG` = 10° | N=10°，T7 核心公式（SSOT §2.0.1） |
| 快門閃爍 | `SHUTTER_FLASH_DURATION_MS` = 300ms | 拍攝確認（SSOT §10.3.1） |
| 標準過渡 | `TRANSITION_FAST_MS` = 300ms | 動畫通用時長（SSOT §2.0） |

---

## 5. 當前 Sprint 活躍規則

### 5.1 isAllAligned 判斷

- 使用動態聚合 `rules.all { ... }`，不硬編碼規則數量
- 加權門檻公式（SSOT §4.2）取代 Boolean AND-gate
- 常數抽取至 companion object

### 5.2 PTF 模式規格 (Layout Spec v2.3)

- 進入: Pill Toggle 點擊 PTF 側
- 退出: Pill Toggle 點回 Photo / ✕ 按鈕（雙路徑）
- 快門冷卻: 進入後 1 秒不可按
- 規則引擎: 六規則降為三規則（FramingRule + LevelRule + AngleRule）
- 零硬體切換鐵律: 不 rebind CameraX, 不 close ML Kit, 不重校 IMU

### 5.3 評分公式權重（D13 待驗證）

- 角度 0.40 / 水平 0.25 / 構圖 0.20 / 光線 0.15

### 5.4 場景判斷優先級

```
PASS_TO_FRIEND (手動) > pitch > 35° (Food) > pitch ∈ [-10°,35°] + face (Portrait) > General > pitch < -10° (LowAngle)
切換 hysteresis: 500ms
```

### 5.5 P05 Photo Mode Recovery 活躍範圍

| Ticket | 狀態 | 驗收焦點 |
| ------ | ---- | ------- |
| T1 | ✅ Done | Eye-AF 視覺環 flag 關閉，FocusIndicatorOverlay 不渲染 |
| T2 | ✅ Done | TapFocusRing 五態渲染，Canvas ≤1 次 drawCircle |
| T3 | 🔧 Active | TapFocusController 狀態機 SSOT A.3 + CameraX AF 接線 |
| T4 | 🔧 Active | Scrim y=0 α0.22 → y=120px α0 |
| T5 | ✅ Done | 快門色彩硬編碼 B.3.3 |
| T6 | ✅ Done | Grid 預設可見 |
| T7 | 🔧 Active | Level Traction 退場 N=10° 公式 |

**Strangler Fig 架構**：TapFocusRing/Controller 為新建系統，FocusIndicatorOverlay 為 MVP 關閉但保留的舊系統。兩套共存不衝突。

---

## 6. Ambient UI 顏色常數

| 用途 | 色值 | Alpha 上限 |
|------|------|-----------|
| Pitch Traction / Halo 未對齊 | `#D4A030` | 0.20 / 0.25 |
| Halo 對齊（呼吸峰值） | `#50C864` | 0.18（呼吸 0.10↔0.18） |
| 呼吸韻律 | alpha 0.10 ↔ 0.18 | 2000ms 週期 |
| 底部 Photo 背景 | `#0D0D0D` | — |
| 底部 PTF 背景 | `#1A1714` | — |
| **Focus Focusing** | `#FFFFFF` | **0.9** |
| **Focus Focused** | `#50C864` | **0.8** |
| **Focus Failed** | `#D4A030` | **0.6** |
| **Focus Locked** | `#D4A030` | **0.85** |
| 快門外環靜態 | `#FFFFFF` | 0.30 |
| 快門外環呼吸 | `#50C864` | 0.10→0.18 |
| 快門內圓 | `#FFFFFF` | 1.0 |
| 快門進度環 | `#50C864` | 0.80 |

---

## 文件元資訊

- **版本**: ssot_context v1.4
- **建立日期**: 2026-05-04
- **更新日期**: 2026-05-24
- **來源**: SSOT v4.0.2 + Layout Spec v2.3 + P05 Contract (Strangler Fig 架構變更)
- **行數**: ~155 行
- **變更摘要 (v1.3 → v1.4)**:
  - §4 FOCUS_* 常數從 3 → 7（補 RING_SIZE, RING_STROKE, FADEOUT, FAILED）
  - §4 新增 ALIGNED_THRESHOLD_DEG, LEVEL_FADE_RANGE_DEG (10°), SHUTTER_FLASH_DURATION_MS, TRANSITION_FAST_MS
  - §5.4 補 LowAngleMode (pitch < -10°)
  - §5.5 新增 P05 活躍範圍（T1-T7 狀態表）
  - §6 Halo 對齊 alpha 0.35 → 0.18（修正 SSOT 代碼對齊）
- **變更摘要 (v1.2 → v1.3)**:
  - SSOT 版本 v4.0 → v4.0.2, Layout Spec v2.2 → v2.3
  - §1 新增 `camera/focus/` 子目錄 + overlay 條目更新
  - §2 新增 TapFocusState / TapFocusRing / TapFocusController + FocusIndicatorOverlay 狀態標註
  - §3 CameraViewModel 升級為全面禁止新增函數 + 新增 EyeAfController / FocusIndicatorOverlay 禁區
  - §4 新增 Focus AF 超時 / drift 閾值 / 長按閾值
  - §5.2 Layout Spec 引用 v2.0 → v2.3
  - §6 新增 Focus 四態色彩 + 快門四元素色值
- **下次更新觸發**: SSOT 版本升級 / Sprint 階段變更 / W2 啟動時
