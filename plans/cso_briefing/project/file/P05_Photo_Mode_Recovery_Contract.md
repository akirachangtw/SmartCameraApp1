# P05 Photo Mode Recovery Contract

**文件編號**: CONTRACT-P05-20260518
**簽發**: CSO (Claude Opus, claude.ai)
**日期**: 2026-05-18
**基準文件**: SSOT v4.0.2 附錄 A · Layout Spec v2.3 · 對焦系統完整架構解析 · BHA Report · Traycer Workflow v2.0.1
**適用範圍**: Phase 0.5 Photo 模式基礎修復（7 票）

---

## §0 Executive Summary

Phase 0.5 實機驗測暴露 Photo 模式 7 項偏離。根因分析揭示最嚴重的問題（三層圓環永駐 + 琥珀圓環）不是 Bug 而是**架構偏離**——代碼中的 Eye-AF 自動追蹤系統與 SSOT v4.0.2 定義的 Tap-to-Focus 系統是完全不同的設計。

**戰略決策**：採用 Strangler Fig——新建 Tap-to-Focus 系統，關閉 Eye-AF 視覺追蹤環（保留光學 AF），不修改 CameraViewModel（2457 行）。

**Traycer 配額原則**：養兵千日，用在一時。P05 七票僅在 T3（最高風險）使用 `execute_review`，全部完成後使用一次 `strict_verification`。其餘票由 cross_review.sh + /qa + /sv 覆蓋。

### 代碼 vs SSOT 偏離總覽

| 維度 | SSOT v4.0.2 | 實際代碼 | 偏離量 |
|------|------------|---------|--------|
| Focus 觸發 | 用戶 Tap | ML Kit 自動偵測 | 完全不同 |
| Focus 視覺 | 單圈 60dp | 三層同心圓 180dp | 3× 尺寸, 3× Canvas |
| 狀態模型 | 4 態 | 6 態 | +2 態 |
| 色彩命名 | 白/綠/琥珀/亮金 | CYAN→Amber, YELLOW→Green | 命名反直覺 |
| Scrim | y=0 α0.22→y=120px α0 | 未覆蓋 TopBar | 缺失 |
| 快門色彩 | R15 硬編碼 #FFFFFF | MaterialTheme.colorScheme | 跟隨裝置主題 |

---

## §1 CSO 戰略裁決（不可修改）

| 決策 | 裁決 | 理由 |
|------|------|------|
| Eye-AF 光學行為 | ✅ 保留 | 自動觸發 AF 讓照片更清晰，有商業價值 |
| Eye-AF 視覺追蹤環 | 🔴 MVP 關閉 | 三層 180dp 持續顯示違反 Ambient UI + 不在 SSOT 規格中 |
| Focus Indicator 修復策略 | **Strangler Fig（新建）** | 舊系統（6 態/3 圈/180dp/ML Kit 驅動）與目標態差異過大，修補 > 新建 |
| CameraViewModel 修改 | 🔴 禁止新增函數 | 2457 行已 8.2× 超標，任何新增都加重膨脹 |
| 一票一功能 | ✅ 強制 | 每票只改一個 concern，防止混合修改導致 attribution 模糊 |

---

## §2 七票定義

### P05-T1: Eye-AF 視覺追蹤環關閉

**執行者**: Claude Code
**預估**: 15 分鐘
**風險**: 極低
**審查**: /qa + /sv + cross_review（不用 Traycer）

**目標**: 關閉 Eye-AF 的視覺反饋（三層圓環），保留光學 AF 行為。

**執行策略**: 在 CameraScreen 中 FocusIndicatorOverlay 的 Composable 呼叫處加入 flag 控制。

```kotlin
// CameraScreen.kt 中找到 FocusIndicatorOverlay 的呼叫點
// 加入 MVP flag 關閉視覺渲染

val isTapFocusReady = false  // P05-T2/T3 完成後改為 true

if (isTapFocusReady) {
    // T2/T3 完成後這裡改為 TapFocusRing(...)
} 
// Eye-AF 視覺追蹤環 MVP 關閉
// 光學 AF（EyeAfController → CameraX startFocusAndMetering）保留運作
```

**保留不動**:
- `EyeAfController.kt` — 光學 AF 觸發邏輯
- `VisualTargetState.kt` — 狀態定義（未來可能復用）
- ML Kit Face Detection 偵測鏈
- `CameraViewModel.kt` 中所有 Eye-AF 相關函數

**禁區**: 不刪除 FocusIndicatorOverlay.kt（T2 需要參考）。不修改 EyeAfController。不修改 CameraViewModel。

**驗收**:
- [ ] App 啟動 → 對著人臉 → 無任何圓環（三層/琥珀/單圈都不出現）
- [ ] 拍照功能正常（Eye-AF 光學行為仍在運作——照片仍然對焦清晰）
- [ ] `git diff --stat` 只有 CameraScreen.kt 被修改

---

### P05-T2: TapFocusRing 新建（純渲染）

**執行者**: Claude Code
**預估**: 1 小時
**風險**: 低
**審查**: /qa + /sv + cross_review

**目標**: 新建 `TapFocusRing.kt`，實作 SSOT 附錄 A 的單圈 60dp 視覺渲染。

**新建檔案**: `app/src/main/java/.../presentation/camera/overlay/TapFocusRing.kt`

**SSOT 附錄 A 精確規格對照**:

| 狀態 | 色值 | Alpha | Scale | 動畫 | 持續 |
|------|------|-------|-------|------|------|
| Idle | — | — | — | 不渲染（guard return） | — |
| Focusing | 白色 #FFFFFF | 0.9 | 1.3→1.0 + 60° 弧旋 | spring/tween | 至 AF 回調 |
| Focused | 綠色 #50C864 | 0.8 | 0.85 脈動 0.85→0.92→0.85 | infinite pulse | 永駐至 drift/re-tap |
| Failed | 琥珀 #D4A030 | 0.6 | 1.0 + ±4px 振盪→1.20 膨脹 | 衰減 | 0.8s + 0.5s 淡出 |
| Locked | 亮金 #D4A030 | 0.85 | 0.85 | 靜態 + 🔒 Badge | 永駐至 re-tap |

**Canvas 預算**: 最大 **1 次 drawCircle** + Locked 時 1 個 Composable Badge = **符合 SSOT A.5 預算**。

**新建 Sealed Class**:

```kotlin
sealed class TapFocusState {
    object Idle : TapFocusState()
    data class Focusing(val center: Offset) : TapFocusState()
    data class Focused(val center: Offset) : TapFocusState()
    data class Failed(val center: Offset) : TapFocusState()
    data class Locked(val center: Offset) : TapFocusState()
}
```

**禁區**: 不寫 Tap 事件接線（T3）。不寫 CameraX AF 呼叫（T3）。不寫狀態轉移邏輯（T3）。不修改任何既有檔案。

**驗收**:
- [ ] 新檔案存在且 assembleDebug 通過
- [ ] 在 CameraScreen 硬編碼 `TapFocusState.Focusing(center=螢幕中心)` → 看到單圈白色 60dp
- [ ] 逐一硬編碼 5 態 → 各態色彩/scale/動畫正確
- [ ] Canvas 呼叫數 = 1（Locked 時 +1 Badge Composable）

---

### P05-T3: TapFocusController 狀態機 + CameraX 接線

**執行者**: Claude Code
**預估**: 2 小時
**風險**: **高**（跨層耦合 + 硬體依賴）
**審查**: /qa + /sv + cross_review + **Traycer execute_review** ✅

**目標**: 接通 Tap 事件 → 狀態轉移 → CameraX AF → 結果回寫 + AE/AF Lock。

**新建檔案**: `app/src/main/java/.../presentation/camera/focus/TapFocusController.kt`

**CameraX AF 接入路徑（需 Traycer 預審確認）**:

現有 AF 呼叫鏈：EyeAfController → CameraRepository.startFocusAndMetering()。新建的 TapFocusController 應使用**同一個 Repository 介面**，避免繞過 DI。

如果 CameraRepository 已有 `startFocusAndMetering(MeteringPoint)` 方法 → 直接使用。
如果不存在 → 新增最小介面方法到 CameraRepository interface（唯一允許的既有檔案修改）。

**SSOT A.3 狀態轉移矩陣（完整實作）**:

```
Tap(x,y) → Focusing
  ├─ CameraX AF Success → Focused（永駐）
  │    ├─ IMU drift ≥15° → 500ms fadeout → Idle
  │    └─ Re-tap(x2,y2) → 舊圈 Failed + 新位置 Focusing
  ├─ CameraX AF Fail → Failed（0.8s → 0.5s fadeout → Idle）
  ├─ 2s 無回調 → Failed（超時保護，SSOT A.6: FOCUS_AF_TIMEOUT_MS）
  └─ Long press ≥500ms → Locked + CameraX AE/AF Lock（永駐，不受 drift）
       └─ Re-tap → 舊圈 Failed + 新位置 Focusing + 解除 AE/AF Lock
```

**IMU drift 監聽**:
- 來源：DeviceSensorManager（已存在，Hilt 注入）
- 閾值：15°（`AmbientConstants.FOCUS_DRIFT_THRESHOLD_DEG`）
- 僅在 Focused 狀態下監聽，Locked 狀態不受影響

**禁區**: 不修改 FocusIndicatorOverlay（舊系統）。不修改 EyeAfController。不在 CameraViewModel 新增函數（除非 Repository 介面不足）。不修改 ML Kit 偵測鏈。

**驗收**:
- [ ] Tap viewfinder → 白色 Focusing 圓環 → AF 成功 → 綠色 Focused
- [ ] 手機移動 15° → Focused 淡出消失
- [ ] Tap 但 AF 失敗 → 琥珀 Failed 振盪 → 消失
- [ ] 2s 無回調 → Failed（超時保護）
- [ ] Long press 500ms → 亮金 Locked + 🔒 Badge → 手機移動不消失
- [ ] Re-tap → 舊 Locked/Focused 觸發 Failed → 新位置 Focusing
- [ ] 前置鏡頭 → Locked 被拒絕（Rule §3.2）
- [ ] `git diff --stat` 新建 1-2 檔 + CameraScreen 接線 + 可能的 Repository 介面擴充

---

### P05-T4: Scrim 覆蓋修復

**執行者**: Claude Code
**預估**: 30 分鐘
**審查**: /qa + /sv + cross_review

**目標**: Scrim 從 y=0 開始覆蓋 TopBar 區域。

**SSOT / Layout Spec 規格**:
- Scrim 起始: y=0, #000000 α0.22
- Scrim 結束: y=120px, #000000 α0
- 漸變曲線: `FastOutSlowInEasing`
- 圖示位置: y=24px + `statusBarsPadding()`

**禁區**: 不修改 TopBar 圖示內容。不修改 Viewfinder 尺寸。

**驗收**: Scrim 從螢幕頂部開始，漸層覆蓋 TopBar 圖示區域，120px 處完全透明。

---

### P05-T5: 快門色彩硬編碼（R15）

**執行者**: Claude Code
**預估**: 30 分鐘
**審查**: /qa + /sv + cross_review

**目標**: 快門按鈕色彩脫離 MaterialTheme，硬編碼為 SSOT B.3.3 定義值。

**SSOT B.3.3 色值表**:

| 元素 | 色值 |
|------|------|
| 外環靜態 | #FFFFFF α0.30 |
| 外環呼吸（對齊時） | #50C864 α0.10→0.18 |
| 內圓 | #FFFFFF |
| 進度環 | #50C864 α0.80 |

**根因**: Claude Code 使用了 `MaterialTheme.colorScheme.primary`，導致 Sony 藍 / Pixel 粉。

**禁區**: 不修改快門尺寸（T4 PTF 相關問題另開票）。不修改快門動畫邏輯。

**驗收**: Sony + Pixel 快門按鈕顏色一致（白色內圓 + 白色外環）。

---

### P05-T6: Grid 可見性修復

**執行者**: Claude Code
**預估**: 15 分鐘
**審查**: /qa + /sv

**目標**: 確認三分網格預設可見性與 SSOT 一致，修復消失問題。

**SSOT**: Layer 1 三分網格 #FFFFFF α0.08, 線寬 0.5px。

**診斷優先**: 先確認 Grid 的預設值是 ON 還是 OFF（查 Settings/DataStore）。如果預設 OFF → 改為 ON。如果預設 ON 但不渲染 → 查渲染條件。

**禁區**: 不修改 Grid 的視覺規格（色值/線寬）。

**驗收**: App 啟動 → Grid 可見（四條白色細線，α0.08）。

---

### P05-T7: Level Traction 退場修復

**執行者**: Claude Code
**預估**: 1 小時
**審查**: /qa + /sv + cross_review

**目標**: Level Traction 對齊後完全消失（α→0），不永駐。

**SSOT §2.0.1 規格**:

```kotlin
val levelOpacity = (maxDeviation / AmbientConstants.LEVEL_FADE_RANGE_DEG)  // N=10°
    .coerceIn(0f, 1f)

if (levelOpacity < 0.01f) return@Canvas  // 完全消失
```

- 偏差 0° → opacity 0.00（完全消失）
- 偏差 ≥10° → opacity 1.00（完全可見）
- 中間線性插值

**「永遠不消失」的可能根因**:
1. `maxDeviation` 計算錯誤（永遠 >10°）
2. `levelOpacity` 用了固定值而非動態計算
3. IMU 數據有固定 offset（角度感測不準的關聯問題）

**禁區**: 不修改 Pitch Traction。不修改 Halo Traction。不修改 IMU 校正邏輯（#2 角度不準是獨立問題，記錄到 DISCOVERY.md）。

**驗收**: 手機放平（0° roll）→ Level Traction 完全消失。手機傾斜 10° → Level 完全可見。

---

## §3 Traycer 配額策略（養兵千日，用在一時）

**原則**: Traycer ROI 永遠小於 APP 完成度及商業價值。P05 的目標是讓 Photo 模式能用，不是追求完美的審查覆蓋率。

| 票 | Traycer | 替代審查 | 理由 |
|-----|---------|---------|------|
| T1 | ❌ | cross_review | 一行 flag，Traycer = 大砲打蚊子 |
| T2 | ❌ | cross_review | 純新建渲染，無跨層風險 |
| **T3** | ✅ `execute_review` | — | CameraX 接線 + 狀態機 = 唯一高風險票 |
| T4 | ❌ | cross_review | Scrim UI 調整 |
| T5 | ❌ | cross_review | 色值替換 |
| T6 | ❌ | /qa + /sv | 最簡單的修復 |
| T7 | ❌ | cross_review | Level 邏輯修復 |
| **P05 全部完成** | ✅ `strict_verification` | — | Phase 0.5 Gate 最終驗收 |

**Traycer 消耗**: 2 次 = ~$0.45。**節省的配額保留給 W2 高風險票（CameraScreen 三票）。**

### Traycer Workflow v2.0.1 微調

P05 的 Traycer 使用跳過前三個 Command（trigger_workflow + architectural_review + ticket_breakdown），直接從 execute_review 開始。理由：

| 被跳過的 Command | 替代 | 理由 |
|-----------------|------|------|
| trigger_workflow | 本 Contract §0 | CSO Contract 已做 SSOT 版本確認 + Scope 鎖定 |
| architectural_review | 本 Contract §1 | CSO 已做架構壓力測試（Strangler Fig 裁決） |
| ticket_breakdown | 本 Contract §2 | CSO 已拆 7 票含禁區 + 驗收條件 |

**T3 的 Traycer execute_review 聚焦點**:

```
重點掃描（與標準 execute_review 相同的四項反向掃描，但額外追加）：

追加掃描 1: CameraX AF 接入路徑
- TapFocusController 呼叫 startFocusAndMetering 的路徑是否經過 Repository?
- 是否繞過了 DI 直接取 CameraControl?（如果是 → 🟠 Scope Creep）

追加掃描 2: 狀態隔離
- TapFocusState 和 VisualTargetState 是否完全獨立?
- 有沒有共享任何 StateFlow 或 ViewModel 欄位?（如果是 → 🔴 Drift）

追加掃描 3: Eye-AF 光學行為
- T1 的 flag 是否影響了 EyeAfController 的光學 AF 觸發?
- 確認 Eye-AF 仍在自動對焦（照片應仍清晰）

追加掃描 4: 超時保護
- FOCUS_AF_TIMEOUT_MS (2000ms) 是否有實作?
- timeout 路徑是否正確觸發 Failed 狀態?
```

---

## §4 執行排程與依賴

```
Day 1（Claude Code Session）
━━━━━━━━━━━━━━━━━━━━━━━
T1 Eye-AF 關閉 (15min) → /qa + /sv + cross_review
    ↓
T2 TapFocusRing 新建 (1hr) → /qa + /sv + cross_review
    ↓
T6 Grid 修復 (15min) → /qa + /sv
    ↓
T5 快門色彩 (30min) → /qa + /sv + cross_review

Day 2（Claude Code Session）
━━━━━━━━━━━━━━━━━━━━━━━
T3 TapFocusController (2hr) → /qa + /sv + cross_review → Traycer execute_review ✅
    ↓
T4 Scrim 修復 (30min) → /qa + /sv + cross_review
    ↓
T7 Level 退場 (1hr) → /qa + /sv + cross_review

Day 2 結束
━━━━━━━━━━━━━━━━━━━━━━━
全部完成 → 實機截圖 → Traycer strict_verification ✅ → Phase 0.5 Gate
```

**排程理由**:
- Day 1 先做 T1+T2（消除三層圓環 + 建立新視覺），搭配低風險的 T6+T5
- Day 2 做最高風險的 T3（有 Traycer 保護），搭配 T4+T7
- T3 放 Day 2 開頭，確保完整 session 專注在最複雜的狀態機

---

## §5 Phase 0.5 Gate 驗收（Traycer strict_verification 聚焦項）

| # | 驗收項 | 方法 | PASS 標準 |
|---|--------|------|----------|
| 1 | 三層圓環消失 | 實機截圖 — 對著人臉 | 無任何圓環 |
| 2 | Tap-to-Focus 完整生命週期 | 實機操作 | 4 態轉換正確（含超時） |
| 3 | AE/AF Lock | 長按 500ms | 亮金 + 🔒 Badge + 不受 drift |
| 4 | Scrim 覆蓋 | 實機截圖 | TopBar 有漸層 Scrim |
| 5 | 快門顏色一致 | Sony + Pixel 截圖 | 兩台都是白色 |
| 6 | Grid 可見 | 實機截圖 | 四條白色細線可見 |
| 7 | Level 退場 | 手機放平 | Level Traction 完全消失 |
| 8 | Eye-AF 光學 | 拍照 + 檢查清晰度 | 照片仍自動對焦清晰 |
| 9 | 效能 | FPS 監測 | 不低於 P0-FPS 基線 (15.9) 的 80% (≥12.7) |

**全部 9 項 PASS → Phase 0.5 PASS → W2 RESUME Gate 開啟**

---

## §6 本 Contract 不包含的項目

| 排除項 | 理由 | 預定時機 |
|--------|------|---------|
| PTF 快門過大 / 進度環溢出 (#4) | PTF 模式問題，非 Photo 基礎 | W2 或獨立票 |
| 美食 45° 識別 (#5) | D9 scope，ML Kit OD | D9 |
| 人像 Halo 圓環 (#6) | Halo Traction 系統，非 Focus | W2 Halo 票 |
| 角度感測不準 (#2) | IMU 校正，功能級 | W2 T04 |
| FocusIndicatorOverlay 刪除 | Eye-AF 視覺系統保留供未來評估 | Post-MVP |
| VisualTargetState 重構 | 六態→四態的架構改造 | Post-MVP |
| OverlayColor 命名淨化 (CYAN→Amber) | 命名債務，不影響功能 | KAIROS D10 |

---

## §7 文件元資訊

| 欄位 | 值 |
|------|-----|
| 文件編號 | CONTRACT-P05-20260518 |
| 簽發者 | CSO (Claude Opus, claude.ai) |
| 日期 | 2026-05-18 |
| 輸入文件 | SSOT v4.0.2 · Layout Spec v2.3 · 對焦系統架構解析 · BHA Report · Traycer Workflow v2.0.1 · Phase 0.5 驗測報告 |
| Traycer 預算 | 2 次 (~$0.45) — execute_review(T3) + strict_verification(P05) |
| 下次審查 | Phase 0.5 Gate 或 KAIROS D10 |

---

**End of P05 Photo Mode Recovery Contract**
