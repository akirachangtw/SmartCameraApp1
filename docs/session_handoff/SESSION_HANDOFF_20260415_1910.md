# Cueshot 工作交接 — 2026-04-15 19:10

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/src/main/.../domain/camera/CameraMode.kt` | 全新建立（**未 commit，untracked**） | D7-B2-FIX Step 1：新建 `sealed interface CameraMode`，包含 `Normal`（預設）與 `PassToFriend`（遞手機）兩個子項。`Food`/`Portrait` 保留註解佔位供 D9+ 使用。**enterTimeMs 不存在此介面**（冷卻由 ViewModel `lastCaptureTimeMs` 管理）。 |

> ⚠️ **注意**：D7-B2-FIX 實作中斷於 Step 1 完成後。`CameraMode.kt` 已建立但尚未 commit，Step 2a~2d（重命名）尚未執行。

## 當前阻塞點

無

## 下一步待辦

**立即執行 D7-B2-FIX，從 Step 2a 開始（CameraMode.kt 已存在，勿重建）：**

1. **Step 2a — `CameraState.kt`**：
   - 移除 `PassToFriendMode` sealed class（整個區塊刪除）
   - `CameraState.passToFriendMode: PassToFriendMode` → `cameraMode: CameraMode = CameraMode.Normal`
   - `CameraUiEvent.TogglePassToFriendMode` → `SetCameraMode(val mode: CameraMode)`
   - 新增 import：`com.akirachang.ai.photographer.domain.camera.CameraMode`

2. **Step 2b — `CameraViewModel.kt`**：
   - `togglePassToFriendMode()` → `setCameraMode(mode: CameraMode)`（接收明確的目標 mode，不再 toggle）
   - `onEvent` 分支：`is CameraUiEvent.SetCameraMode -> setCameraMode(event.mode)`
   - `capturePhoto()` Guard：`_state.value.passToFriendMode is PassToFriendMode.Active` → `_state.value.cameraMode is CameraMode.PassToFriend`
   - `resetFocusState()` 末尾加入：`lastCaptureTimeMs = Long.MIN_VALUE / 2`（重置冷卻哨兵）
   - 移除 `PassToFriendMode` import（已移至 domain layer）

3. **Step 2c — `CameraTabBar.kt`**：
   - 參數 `passToFriendMode: PassToFriendMode` → `cameraMode: CameraMode`
   - `val isPtfActive = passToFriendMode is PassToFriendMode.Active` → `val isPtfActive = cameraMode is CameraMode.PassToFriend`
   - import 改為 `com.akirachang.ai.photographer.domain.camera.CameraMode`
   - KDoc 參數說明同步更新

4. **Step 2d — `CameraScreen.kt`**：
   - `val isPtfActive = state.passToFriendMode is PassToFriendMode.Active` → `val isPtfActive = state.cameraMode is CameraMode.PassToFriend`
   - `passToFriendMode = state.passToFriendMode` → `cameraMode = state.cameraMode`
   - `onToggle = { viewModel.onEvent(CameraUiEvent.TogglePassToFriendMode) }` → `onToggle = { viewModel.onEvent(CameraUiEvent.SetCameraMode(if (state.cameraMode is CameraMode.PassToFriend) CameraMode.Normal else CameraMode.PassToFriend)) }`
   - 新增 import：`com.akirachang.ai.photographer.domain.camera.CameraMode`

5. **Step 3 — `devspec/feature_record_Backlog.md`**：
   - 追加 ADR：「新增相機模式需繼承 `CameraMode` sealed interface，同步實作 ViewModel 轉換邏輯與 CameraScreen UI 遮罩過濾器」（候選 KAIROS #3 規則）

6. **`.claude/PROGRESS.md`**：
   - 追加 D7-B2-FIX Ticket 記錄
   - 追加 Batch2（T1 大快門 UI、T2 快門冷卻負向測試、T3 教學 BottomSheet）與 Batch3（回歸驗證）Ticket 骨架

7. **Build + `/qa` + `/sv` 驗收**

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `CameraMode` 放在 `domain/camera/` 層（非 `presentation/camera/`） | 場景模式是業務邏輯（Domain），Rule Engine（D8+）需直接消費；放 presentation 層會導致 Domain 反向依賴 UI | `CameraMode.kt`；D8 的 `PassToFriendRule` 可直接 import domain layer |
| `CameraMode.PassToFriend` 不攜帶 `enterTimeMs` | 冷卻是 ViewModel 實作細節（Rule 16），不應滲透至 Domain 模型；哨兵值由 `lastCaptureTimeMs` 管理 | `CameraViewModel.kt`（無需修改冷卻邏輯，只改 Guard 的型別判斷） |
| `TogglePassToFriendMode` → `SetCameraMode(mode: CameraMode)` | Toggle 語意對未來多 mode 擴展不友善；明確傳入目標 mode 更可讀、可測試 | `CameraUiEvent`、`CameraScreen.kt` 的 Tab callback |
| `CLAUDE.md` 不修改，ADR 記錄至 Backlog 待 KAIROS #3 | CLAUDE.md 屬禁區，日常 Ticket 不可修改（CLAUDE.md 鐵律）；D10 KAIROS #3 正式提煉規則 | `devspec/feature_record_Backlog.md` |

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（特別是 Rules 14/16 及禁區條款）
- `.claude/PROGRESS.md` — D7 目前狀態（Batch0+B1 ✅，B2-FIX 進行中）
- `app/src/main/.../domain/camera/CameraMode.kt` — Step 1 已建立，新 Session 需讀取確認內容再繼續 Step 2a
- `app/src/main/.../presentation/camera/CameraState.kt` — Step 2a 的修改起點（含現有 `PassToFriendMode` 定義位置）
- `app/src/main/.../presentation/camera/CameraViewModel.kt` — Step 2b 起點（`togglePassToFriendMode` L495、`capturePhoto` Guard L591、`resetFocusState` L627）
- `app/src/main/.../presentation/camera/components/CameraTabBar.kt` — Step 2c 起點

## 不需帶入的 Context

- D7-Batch0 技術債修復細節 — 已 commit，根因已解決
- D7-B1 的實作推導過程 — 已 commit，命名衝突分析已決策
- `PassToFriendMode` 的命名衝突討論 — 已決策採用 Option A (CameraMode)，結論記錄於本文件
- `/qa`、`/sv` 的執行過程細節 — 驗收已通過，記錄於 PROGRESS.md
- `CameraMode` 是否攜帶 `enterTimeMs` 的討論 — 已決策不攜帶
