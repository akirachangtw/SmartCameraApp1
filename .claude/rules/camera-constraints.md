---
description: 相機硬體約束 — CameraX / ML Kit / Executor / Performance
paths:
  - "**/*Camera*.kt"
  - "**/*CameraX*.kt"
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Camera Constraints

# Cueshot — AI Coaching Camera (MVP)

## Product Overview

**App 名稱**: Cueshot
**定位**: AI 離線導拍引擎 — 不只告訴你怎麼拍對，還懂得什麼時候不該干預。
**開發者**: Akira（獨立開發者，台灣）

### Key Technologies

- **Language**: Kotlin (Primary)
- **Architecture**: Clean Architecture (domain/data/presentation) + MVVM
- **UI**: Jetpack Compose (單一 Activity)
- **Camera**: CameraX 1.4+
- **AI/ML**: ML Kit Face Detection + Object Detection（**MUST** 互斥，不可同時開啟）
- **Sensor**: IMU (gyroscope) + ImuCompensator
- **Image Processing**: ML Kit Selfie Segmentation + StackBlur (Phase 1.5 DOF)
- **DI**: Hilt
- **Testing**: JUnit, Mockito

### Scene Modes (4 modes, no LowAngle)

```
PTF (user_selected) → PassToFriendMode [最高優先]
pitch > 35°         → FoodMode [IMU 自動]
pitch ∈ [-10°, 35°] AND face ≥ 1 → PortraitMode [ML Kit]
pitch ∈ [-10°, 35°] AND face == 0 → GeneralMode [fallback]
```

ML Kit Face Detection 與 Object Detection **MUST** 互斥，切換需 500ms hysteresis。

### North Star Metric

**IFTR (Instruction Follow-Through Rate) > 70%** — 引導後 3 秒內手機姿態朝正確方向改變 ≥ 3°。

## ⚡ 效能預算與併發防禦 (Performance & Concurrency Strategy)

### 三層非同步管道 (Three-Tier Async Pipeline)

* **偵測層 (ML Pipe)**：物體偵測與語義解析 (10fps)，**MUST** 強制綁定 `Dispatchers.Default`。
* **補償層 (IMU Pipe)**：陀螺儀融合 (100-200 Hz)，座標預測插值。
* **渲染層 (UI Pipe)**：Compose 繪製 (60fps)，僅接收預測後的輕量化狀態。

### 三級裝置降級策略

| 等級 | 核心引導 | 輔助功能 | 關閉功能 |
|------|---------|---------|---------|
| FULL | Pitch Traction 60fps + Halo 10fps + 所有規則 | 鏡頭髒汙 2fps + 雜亂框 + 減法留白 | 無 |
| LIMITED | Pitch Traction 60fps + Halo 8fps + 核心 4 規則 | 鏡頭髒汙 1fps + 減法留白 | 雜亂框 |
| LEGACY | Pitch Traction 60fps + Halo 5fps + 2 規則 | 無 | 鏡頭髒汙 + 減法留白 + 雜亂框 + 光線 |

**核心保護**：無論哪個等級，Pitch Traction (IMU 驅動) 永遠 60fps。

### 分幀計算

```
Frame 1: ML Kit + Rules + Priority → 65ms
Frame 2: Histogram + Minimalist + Clutter → 13ms
Frame 5: Lens Smudge (每 5 幀) → 12ms
```

端到端峰值：< 100ms（實際 ~65ms）。記憶體：< 150MB。電池增耗：< 15%。

## ⚠️ ADB 使用指南

**MUST** 絕對不要使用：
```bash
adb shell screencap -p > file.png          # 會造成 Claude Code Chat API 崩潰
adb shell screencap -p | <任何重導向操作>
```

**✅ 正確截圖方法**（先存手機再 pull）：
```bash
adb -s DEVICE_ID shell "screencap -p > /sdcard/Download/temp_screen.png"
adb -s DEVICE_ID pull //sdcard//Download//temp_screen.png tmp/screenshot.png
adb -s DEVICE_ID shell "rm /sdcard/Download/temp_screen.png"
```

15. **防護機制 KDoc 引用真實呼叫鏈規則**：當 KDoc 描述多道防線（double-gating）或安全機制時，**SHOULD** 引用**實際參與呼叫鏈的具體類別名稱與方法**（如 `LiveCameraSource.focusOnPoint()` 而非泛稱 `the framework`），並標注觸發判斷點（如「as detected by `FixedFocusDetector` at camera bind time」）。**SHOULD** 禁止引用未在此呼叫路徑上的類別。觸發來源：D6-B3 KDoc 校準（`EyeAfController` 第二道防線誤引用 `CameraXFocusController`，實際路徑為 `LiveCameraSource`）。

16. **硬體呼叫雙重門控規則**：任何觸發硬體資源（對焦、閃光、音效）的 Controller 或 UseCase，**MUST** 實作**兩道獨立 Guard**：①鏡頭方向 Guard（前鏡頭永遠 early-return，不分情境）；②節流 Guard（時間戳 cooldown，初始哨兵值 **MUST** 為 `Long.MIN_VALUE / 2` 而非 `0L`，確保首次呼叫必定通過）。兩道 Guard 缺一不可，且 **MUST** 有對應的負向單元測試（前鏡頭抑制 + 節流攔截各 ≥1 條）。觸發來源：D6-B3 `EyeAfController` 設計審計；哨兵值 bug（`0L` 導致首次呼叫被誤攔截）。

30. **Filter Rotation 維度規則**：於 detection/face 管線新增任何 predicate filter 時，**MUST** 考慮 rotation 維度。rotation=0 下正確的 filter，在 rotation=90/270 時可能誤殺（漏放正確目標）或誤放。filter 判斷 **MUST** 對 rotation 不變，或明確對每個 rotation 分支各自驗證。觸發來源：K4-8（publishSanitizedResult vs observeCompositionAdvice 座標系不一致）。

31. **幾何量 Pixel-Space 計算規則**：bbox 長寬比（aspect ratio）等幾何量 **MUST** 在 pixel-space 計算，**MUST** 禁止對 normalized 座標直接相除。normalized 座標對不同分母（width vs height）相除無幾何意義——normW/normH=1.33 可能實為 pixel-space 57×57 正方形，卻被誤判為 landscape。觸發來源：K4-9。

32. **座標系選擇一致性規則**：ML Kit 座標系選擇（`fromMediaImage` vs `fromBitmap`）**MUST** 與下游 `CoordinateTransformer` 的 rotation 補償假設一致。橫向拍攝時 HAL rotation=0，`fromMediaImage` 不旋轉但 `fromBitmap` 已含旋轉；兩者混用導致 ±90° 座標偏移。觸發來源：K4-27（Issue #1 源碼審查橫向座標陷阱）。
