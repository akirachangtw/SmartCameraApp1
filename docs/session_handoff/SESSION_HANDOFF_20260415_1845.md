# Cueshot 工作交接 — 2026-04-15 18:45

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/src/main/.../camera/CameraState.kt` | `PassToFriendMode` (新增)、`CameraState.passToFriendMode`、`CameraUiEvent.TogglePassToFriendMode` | D7-B1-T1：新建 `PassToFriendMode` sealed class (`Off` / `Active(enterTimeMs: Long)`)；`CameraState` 新增 `passToFriendMode` 欄位；`CameraUiEvent` 新增 `TogglePassToFriendMode`。**禁止修改現有 `PtfState`（那是 Tap-to-Focus，語意不同）** |
| `app/src/main/.../camera/CameraViewModel.kt` | `togglePassToFriendMode()`、`onEvent()`、`capturePhoto()`、`lastCaptureTimeMs` | D7-B1-T1/T2：新增 `togglePassToFriendMode()` 切換邏輯；`onEvent` 新增分支；新增 `lastCaptureTimeMs: Long = Long.MIN_VALUE / 2` 哨兵值；`capturePhoto()` 加入 Rule 16 1s 冷卻 Guard（僅 `Active` 時生效） |
| `app/src/main/.../camera/components/CameraTabBar.kt` | 全新建立 | D7-B1-T1：300ms 色彩動畫雙 Tab（拍照 ↔ 遞手機）；Rule 14 compliant（4x `animateColorAsState` 置頂）；`TAG_TAB_NORMAL` / `TAG_TAB_PTF` 語意探針；stateless composable（無 ViewModel 依賴） |
| `app/src/main/.../camera/CameraScreen.kt` | `CameraScreen()`、底部控制欄 | D7-B1-T1/T3：`isPtfActive` 計算；48dp 佔位符 → `CameraTabBar`；`GridOverlay` / `HaloGuidanceLayer` / `CompositionGuideOverlay` / `CompositionHint` 加入 `!isPtfActive` 隱藏條件；`EdgeWarmthLayer` + `BreathingRing` 保留不動 |
| `app/src/main/res/values/strings.xml` | PTF Tab 字串 | 新增 4 個 PTF Tab i18n 字串：`tab_normal_mode`、`tab_pass_to_friend`（及對應 content description）。Rule 9 i18n 守則合規 |
| `.claude/PROGRESS.md` | D7-Batch1 區塊、Sprint Health | D7-B1 三張 Ticket 全數 ✅ DONE；Sprint Health 更新（Build/Tests/Lint） |

## 當前阻塞點

無

## 下一步待辦

1. **D8-T1（PTF 規則引擎）**：讀取 `domain/composition/` 目錄，確認現有 `CompositionRule` 介面定義；新建 `PassToFriendRule.kt`，當 `passToFriendMode is Active` 時覆寫所有其他規則輸出（最高優先級）；在 `CompositionModule.kt` 新增 `@Binds @IntoSet`
2. **D8-T2（Halo 牽引）**：`HaloGuidanceLayer` 在 PTF Active 時切換為全螢幕綠色 Halo（`#50C864`）引導被攝者看鏡頭；確認 `HaloGuidanceLayer` 目前的 `isAllAligned` 邏輯不與 PTF 綠色衝突
3. **D8-T3（首次教學動畫）**：讀取 `CameraScreen.kt`，確認是否已有 `showOnboarding` 狀態欄位；若無，在 `CameraState` 新增 `showPtfTutorial: Boolean`，PTF 首次進入時顯示半透明指引覆蓋層（3秒後自動消失）

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `PassToFriendMode` 與 `PtfState` 語意完全分離，不修改現有 `PtfState` | `PtfState` 是 Tap-to-Focus 硬體對焦狀態機（Idle/Focusing/Focused/Failed），與 Pass-To-Friend 場景模式是不同維度的狀態 | `CameraState.kt`（新增 `passToFriendMode` 欄位，不修改 `ptfState`） |
| `CameraTabBar` 使用 `Modifier.weight(1f)` 取代固定 48dp 佔位符 | 與快門按鈕等比例共存，避免底部欄寬度計算問題 | `CameraTabBar.kt`、`CameraScreen.kt` 底部 Row |
| Rule 16 冷卻哨兵值設為 `Long.MIN_VALUE / 2`（非 `0L`） | 確保 PTF 進入後第一拍能立即擊發，`0L` 會導致約 48 天後才能首次拍照（`now - 0 > 1000`）——但初始進入時確保不被誤攔截 | `CameraViewModel.kt` `lastCaptureTimeMs` |
| PTF 極簡 UI 隱藏清單：`GridOverlay` / `HaloGuidanceLayer` / `CompositionGuideOverlay` / `CompositionHint`；**保留** `EdgeWarmthLayer` / `BreathingRing` | 場景規格：PTF 模式下構圖引導干擾被攝者，但 Edge Warmth 是姿態輔助，呼吸韻律是對齊回饋，兩者對「遞手機」流程有益 | `CameraScreen.kt` |

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（特別是 Rules 14/16）
- `.claude/PROGRESS.md` — D7 ✅ DONE；D8 Ticket 定義（D8 骨架目前 PROGRESS.md 尚未建立）
- `app/src/main/.../camera/CameraState.kt` — 確認 `PassToFriendMode` 與 `PtfState` 共存結構
- `app/src/main/.../camera/CameraScreen.kt` — 了解 PTF Tab Bar 接線現狀，D8-T3 教學動畫安裝位置
- `app/src/main/.../camera/components/HaloGuidanceLayer.kt` — D8-T2 PTF Halo 改色邏輯的修改起點
- `app/src/main/.../domain/composition/CompositionRule.kt` — D8-T1 新建規則前必讀介面契約

## 不需帶入的 Context

- D7-Batch0 技術債修復細節（Compose `mutableStateOf` 修復、magic string 消除）— 已 commit，根因已解決
- D7-B1 的實作推導過程（命名衝突分析、佔位符替換決策）— 已決策並落地，不需再討論
- `/qa` 和 `/sv` 的執行細節 — 驗收已通過，結果已記錄於 PROGRESS.md
- Rule 16 冷卻哨兵值的推導過程 — 已決策 `Long.MIN_VALUE / 2`，記錄於本文件
