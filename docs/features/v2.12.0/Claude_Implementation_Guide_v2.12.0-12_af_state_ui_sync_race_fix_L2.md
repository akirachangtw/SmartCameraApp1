# Claude Implementation Guide: v2.12.0-12 AF State UI Sync Race Fix (L2)

**Status**: Architect Reviewed & Approved with Strict Constraints
**Target Developer**: Claude Code
**Context**: `v2.12.0-12_af_state_ui_sync_race_fix-L1_Context.md`

身為資深 Android 架構師，針對本次 L1 提出的修復方案，我進行了嚴格的技術設計審查。雖然 L1 正確指出了 Bug 的根因與表面修復方法（例如取消 Coroutine、修改 UI 傳參），但從架構防禦、UDF (Unidirectional Data Flow) 潔淨度以及狀態機的健壯性來看，目前的修復方向仍屬於「頭痛醫頭」的 Patch。

為了確保這套系統能在不同硬體（Sony vs. Pixel）及極端操作下穩定運行，請 Claude 嚴格遵循以下 4 大架構防禦準則進行實作：

---

## 1. 軟硬體能力矩陣（Hardware/Software Capability Matrix）防禦

**[架構師審查]**：
L1 指出 Pixel 9a 前鏡頭為 Fixed-focus，而 Sony XQ-BT52 支援 AF。目前的 AF 觸發機制對於 Fixed-focus 鏡頭雖然有 `isFixedFocusLens` 的攔截，但這層攔截過於依賴呼叫端的自律，未能內化到硬體能力的抽象層中。

**[實作綱領]**：
*   **硬體能力與邏輯狀態的隔離**：對於 Fixed-focus 鏡頭（如 Pixel 9a 前鏡頭），根本不應允許產生 `focusJob` 或進入 `AfState.Focusing` 狀態。
*   **防禦實作**：在 `focusOnPoint()` 的最上游，必須強制斷言目前鏡頭的對焦能力。若 `isFixedFocusLens == true`，則不發送 HAL 請求，並應立即將狀態導向 `Idle` 或對應的預設狀態，拒絕任何非法的對焦狀態變更。

---

## 2. Sealed Class 狀態機防禦（State Machine Defense）

**[架構師審查]**：
L1 提出在 `switchCamera()` 中先呼叫 `focusJob?.cancel()` 來解決死鎖（Race Condition）。這是非常脆弱的非同步設計（Wait-and-kill pattern）。Coroutine 的 Cancellation 是協作式的（Cooperative），如果在取消的瞬間 HAL 剛好回傳結果，仍然有極小機率穿透防線覆蓋狀態。

**[實作綱領]**：
*   **世代標記 (Generation Token) / Session ID 機制**：
    依賴 `.cancel()` 進行架構防禦是不及格的。必須為每一次的相機連線或對焦請求引入 `SessionToken` 或檢查 `currentLensFacing`。
*   **狀態遷移的合法性檢查**：當 HAL Callback 準備將狀態寫入 `AfState.Locked` 時，必須進行雙重驗證：
    1. 當前的 `AfState` 是否為 `Focusing`？（如果不允許從 `Idle` 直接跳轉到 `Locked`，則拋棄該 Callback）。
    2. 當前活躍的鏡頭與發起請求時的鏡頭是否一致？
*   **防禦實作**：
    ```kotlin
    // 概念代碼：必須在 ViewModel 的 Repository 或 State holder 中進行攔截
    if (activeCameraId == requestCameraId && currentState is AfState.Focusing) {
        _state.update { it.copy(afState = AfState.Locked) }
    } else {
        // Drop outdated or invalid callback
    }
    ```

---

## 3. UDF 解耦（UI vs Camera State）

**[架構師審查]**：
L1 建議在 Compose UI 層將傳參改為 `isLocked = state.isAEAFLocked || state.afState is AfState.Locked`。這是嚴重的 UDF 架構破壞。UI 層（`CameraScreen.kt`）絕對不該包含業務邏輯（Business Logic）或承擔狀態聚合的責任。

**[實作綱領]**：
*   **單一事實來源 (Single Source of Truth) 與衍伸狀態 (Derived State)**：
    ViewModel 必須在暴露給 UI 之前，將「長按 AE/AF 鎖定」與「Eye-AF 對焦成功」的狀態聚合為一個專門供 UI 消費的屬性。
*   **防禦實作**：
    在 `CameraState` 中擴展一個供 UI 直接使用的唯讀屬性：
    ```kotlin
    // CameraState.kt
    val isFocusIndicatorLocked: Boolean
        get() = isAEAFLocked || afState is AfState.Locked
    ```
    在 `CameraScreen.kt` 中只能寫：
    ```kotlin
    isLocked = state.isFocusIndicatorLocked
    ```
    嚴禁在 Composable 內部進行 `||` 運算。

---

## 4. 破壞性邊界條件防禦（Destructive Boundary Conditions）

**[架構師審查]**：
真實世界的用戶行為充滿破壞性。L1 僅考慮了穩態下的「切換鏡頭」，但未考慮連續狂按切換鏡頭、或在 AF 鎖定瞬間 App 被推入背景（Lifecycle `onPause`）的極端場景。如果因為 HAL 的延遲 Callback 導致 UI 殘留 Locked 動畫，會造成嚴重的「幽靈對焦框」Bug。

**[實作綱領]**：
*   **強制生命週期重置 (Lifecycle Teardown)**：
    不只是 `switchCamera`，當 App 進入背景或相機 Session 重置時，必須設計一個 `resetFocusState()` 統一方法，徹底清空 `focusJob` 並將 `AfState` 強制壓回 `Idle`。
*   **切換鏡頭的防抖/原子性 (Atomicity)**：
    在 `switchCamera()` 執行期間，應該有一個短暫的 Transition 狀態，此期間拒絕所有外部的 AF 觸發。確保前鏡頭（特別是 Pixel 9a 這種 Fixed-focus）啟動時，面對的是一個乾淨無污染的 `Idle` 狀態。

---

## 結論與要求

Claude，在實行 L1 的需求時，**不要只做表面填補**。
1. 更新 `CameraState` 以提供乾淨的衍生屬性供 UI 接接。
2. 針對 Race Condition，除了 `cancel()` 之外，必須在狀態更新區塊加上狀態/鏡頭一致性的嚴格校驗。
3. 把防禦網推陳到 HAL 回調與 Lifecycle 邊界。

請基於以上指南進行程式碼 PR 實作。
