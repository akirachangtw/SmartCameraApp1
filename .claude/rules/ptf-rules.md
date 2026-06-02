---
description: PTF 場景規則
globs:
  - "**/*Ptf*.kt"
  - "**/*PassToFriend*.kt"
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# PTF Rules

### Scene Modes (4 modes, no LowAngle)

```
PTF (user_selected) → PassToFriendMode [最高優先]
pitch > 35°         → FoodMode [IMU 自動]
pitch ∈ [-10°, 35°] AND face ≥ 1 → PortraitMode [ML Kit]
pitch ∈ [-10°, 35°] AND face == 0 → GeneralMode [fallback]
```

| PTF Tab toggle | 300ms 切換，零 CameraX rebind | | | |
| 快門冷卻 | PTF 進入後 1s 內不可按 | | | |

# 實機測試日誌格式

```markdown
# device_test_YYYYMMDD.md

## 測試裝置
- 型號: [e.g. Pixel 7a]
- Android: [version]
- 效能等級: [FULL/LIMITED/LEGACY]

## 測試項目
| 功能 | 預期行為 | 實際行為 | Pass/Fail | 備註 |
|------|---------|---------|-----------|------|
| Pitch Traction 60fps | 邊緣引導光流暢 | | | |
| Pitch >35° 切 FoodMode | 自動切換 <500ms | | | |
| Face Detection Halo | 圓形 Halo 跟蹤人臉 | | | |
| PTF Tab toggle | 300ms 切換，零 CameraX rebind | | | |
| 快門冷卻 | PTF 進入後 1s 內不可按 | | | |

## 發現的問題
| Issue | Severity | Reproduce Steps | Workaround |
|-------|----------|-----------------|------------|
```

22. **狀態機時序紀律**：任何 mode-entry cooldown（模式切入冷卻）**MUST** 綁定**切換時間戳（transition timestamp）**，而非前次動作時間戳（prior action timestamp）。切換至目標模式的當下 **MUST** 立即更新 `modeEntryTimeMs = timeProvider()`，並以此為基準計算冷卻期。使用前次操作（如上次拍照時間 `lastCaptureTimeMs`）作為冷卻起點是語意錯誤，會導致模式剛切入時冷卻期已部分或全部消耗完畢。觸發來源：FIX-P0-SESSION-B（PTF 1s entry lockout 改用 `modeEntryTimeMs`，與 Rule 16 capture-to-capture cooldown 分層）。
