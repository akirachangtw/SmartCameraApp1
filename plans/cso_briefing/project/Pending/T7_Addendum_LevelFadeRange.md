# T7 Addendum — LEVEL_FADE_RANGE_DEG Root Cause

**簽發**: CSO (Claude Opus, claude.ai)
**日期**: 2026-05-24
**關聯**: P05 Contract §T7 + SSOT v4.0.2 §2.0.1 (D-10)
**狀態**: 🔧 待 Claude Code T7 執行時載入

---

## 發現

P05 交付物 B（ssot_context v1.4）執行過程中附帶發現：

```
AmbientConstants.kt: LEVEL_FADE_RANGE_DEG = 5f  ← 代碼實際值
SSOT v4.0.2 §2.0.1:  LEVEL_FADE_RANGE_DEG = 10f ← 正確值
```

## 影響分析

| N 值 | 手持微顫 ±2° 的 opacity | 手持微顫 ±3° 的 opacity | 用戶感知 |
|------|----------------------|----------------------|---------|
| 5f（代碼） | 0.40 | 0.60 | 「Level 永遠不消失」 |
| 10f（SSOT） | 0.20 | 0.30 | 融入背景，符合 Ambient UI |

**結論**: 此常數偏差是 T7「Level Traction 永遠不消失」的根因之一（可能是主因）。

## T7 修復範圍（CSO 補充）

原 P05 Contract T7 列出 3 個可能根因，CSO 追加第 4 項：

1. ⬜ `maxDeviation` 計算錯誤（原 Contract 根因 1）— 仍需排查
2. ⬜ `levelOpacity` 用了固定值（原 Contract 根因 2）— 仍需排查
3. ⬜ IMU 數據有固定 offset（原 Contract 根因 3）— 仍需排查
4. ✅ **`LEVEL_FADE_RANGE_DEG = 5f → 10f`（CSO 補充，已確認）**

## 執行指令

```kotlin
// AmbientConstants.kt
// 修改前：
const val LEVEL_FADE_RANGE_DEG = 5f

// 修改後：
const val LEVEL_FADE_RANGE_DEG = 10f  // SSOT §2.0.1 D-10: N=10°
```

**驗收**: 手機放平（0° roll）→ Level Traction 完全消失（opacity < 0.01）

---

**End of T7 Addendum**
