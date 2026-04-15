# Cueshot 工作交接 — 2026-04-15 21:20

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `domain/camera/CameraMode.kt` | 全新建立 | D7-B2-FIX Step 1：`sealed interface CameraMode`，包含 `Normal`、`PassToFriend` objects。`enterTimeMs` 不存在此介面（冷卻由 ViewModel `lastCaptureTimeMs` 管理）。**已 commit (c698879)** |
| `presentation/camera/CameraState.kt` | `passToFriendMode` 欄位、`CameraUiEvent` | D7-B2-FIX Step 2a：移除 `PassToFriendMode` sealed class；`passToFriendMode` → `cameraMode: CameraMode = CameraMode.Normal`；`TogglePassToFriendMode` → `SetCameraMode(val mode: CameraMode)`；新增 `DismissPtfOnboarding` event。**已 commit (c698879)** |
| `presentation/camera/CameraViewModel.kt` | `togglePassToFriendMode()`、`capturePhoto()`、`resetFocusState()`、constructor | D7-B2-FIX Step 2b：`togglePassToFriendMode()` → `setCameraMode(mode: CameraMode)`；capturePhoto Guard 改判 `CameraMode.PassToFriend`；`resetFocusState()` 末尾加 `lastCaptureTimeMs = Long.MIN_VALUE / 2`；注入 `ptfPreferences: PtfPreferences`；暴露 `isPtfOnboarded: Flow<Boolean>`；新增 `dismissPtfOnboarding()`。**已 commit (c698879)，Batch2 部分未 commit** |
| `presentation/camera/components/CameraTabBar.kt` | 參數型別、import | D7-B2-FIX Step 2c：參數 `passToFriendMode: PassToFriendMode` → `cameraMode: CameraMode`；import 換至 domain layer。**已 commit (c698879)** |
| `presentation/camera/CameraScreen.kt` | `isPtfActive`、`ShutterButtonSection` 呼叫、tab callback、新增 ✕ 按鈕 + 教學 hint | D7-B2-FIX Step 2d + Batch2：isPtfActive 改判 `CameraMode.PassToFriend`；`ShutterButtonSection` 新增 `cameraMode` 參數；`onToggle` 改傳明確 mode；新增 ✕ 退出按鈕（TopEnd，僅 PTF 顯示）；新增 `AnimatedVisibility` 教學 hint（`isPtfActive && !isPtfOnboarded`）。**Batch2 部分未 commit** |
| `presentation/camera/CameraScreen.kt` | `ShutterButtonSection` composable | Batch2 T1：新增 `cameraMode: CameraMode` 參數；`animateDpAsState` 72→80dp（Rule 14 置頂）；`ringBaseSize` 同步 84→94dp。**未 commit** |
| `data/preferences/PtfPreferences.kt` | 全新建立 | Batch2 T3：DataStore 封裝 `ptf_onboarded` flag；`isPtfOnboarded: Flow<Boolean>`；`markPtfOnboarded()` suspend fun。**未 commit（untracked）** |
| `di/AppModule.kt` | `providePtfPreferences()` | Batch2 T2：新增 `@Provides @Singleton fun providePtfPreferences`。**未 commit** |
| `res/values/strings.xml` | PTF 相關字串 | Batch2 T7：新增 `ptf_exit_cd`、`ptf_onboarding_message`、`ptf_onboarding_got_it`。**未 commit** |
| `devspec/feature_record_Backlog.md` | ADR-D7-B2-FIX | D7-B2-FIX Step 3：CameraMode sealed interface 擴展契約 ADR，KAIROS #3 候選 Rule 17。**已 commit (45fffab)** |
| `.claude/PROGRESS.md` | Last Updated、Current Day、D7-B2-FIX + Batch2 Ticket 表 | QA 更新。**未 commit** |

## 當前阻塞點

無

## 下一步待辦

**立即執行：Batch2 原子化 Commit 封板**

1. **Commit 1（程式碼）**：
   ```bash
   git add \
     app/src/main/java/com/akirachang/ai/photographer/data/preferences/PtfPreferences.kt \
     app/src/main/java/com/akirachang/ai/photographer/di/AppModule.kt \
     app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt \
     app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraState.kt \
     app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt \
     app/src/main/res/values/strings.xml
   git commit -m "feat(ptf): add large shutter animation, exit button and first-launch onboarding hint (D7-Batch2)"
   ```

2. **Commit 2（文件）**：
   ```bash
   git add .claude/PROGRESS.md
   git commit -m "docs(progress): record D7-Batch2 completion and update sprint status"
   ```

3. **確認 PROGRESS.md** 的 `Current Day` 從 `🔄 WIP` 改為 `✅ DONE`（在 Commit 2 之前先更新）。

4. **下一批次 D7-Batch3（回歸驗證）**：
   - 實機驗證 PTF 模式：快門 80dp 動畫、✕ 退出、教學 hint 首次顯示/永久消失
   - 確認 `resetFocusState()` 冷卻 sentinel 重置未破壞現有對焦行為
   - 若 Batch3 通過 → 執行 D7 封板 commit + Traycer 終審

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `isPtfOnboarded.collectAsState(initial = true)` | 初始值設 `true` 防止 DataStore 未載入時 hint 閃現再消失（防呆設計） | `CameraScreen.kt` L160 |
| `PtfPreferences` 用獨立 DataStore（`ptf_preferences`，非共用 `overlay_preferences`） | 關注點分離；PTF flag 語意與 overlay 設定不同；未來 D9+ 可擴展 PTF 相關設定 | `PtfPreferences.kt`、`AppModule.kt` |
| ✕ 退出按鈕用 `if (isPtfActive)` 而非 `AnimatedVisibility` | 按鈕進/退與模式切換同步，無需額外動畫層；simplicity | `CameraScreen.kt` PTF exit block |
| 教學 hint 用 `AnimatedVisibility` (fadeIn + slideInVertically) | 首次顯示需視覺緩入，避免突兀；符合 Ambient UI 風格 | `CameraScreen.kt` hint block |
| `ShutterButtonSection` 的 `animateDpAsState` 置頂（Rule 14 合規） | Rule 14：所有 Composable 中的動畫狀態必須在 if/when 之前宣告 | `CameraScreen.kt` L668-L680 |
| Traycer `git commit -am` 陷阱已修正：改用精確 `git add [files]` 兩步 commit | `-am` 會自動 stage 所有已追蹤檔案，破壞原子化 commit 紀律 | Git workflow |

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（特別是 Rule 14、16 及禁區條款）
- `.claude/PROGRESS.md` — D7 目前狀態（B2-FIX ✅，Batch2 🔄 WIP，待 commit 封板）
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt` — 含所有未 commit 的 Batch2 修改
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraState.kt` — 含 `DismissPtfOnboarding` event
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt` — 含 `ptfPreferences` 注入與 `dismissPtfOnboarding()`
- `app/src/main/java/com/akirachang/ai/photographer/data/preferences/PtfPreferences.kt` — 新建未 commit

## 不需帶入的 Context

- D7-B2-FIX 架構重構討論 — 已 commit (c698879)，CameraMode 決策已歸檔至 Backlog ADR
- PassToFriendMode 命名衝突分析 — 已解決，採用 CameraMode sealed interface
- QA/SV 執行細節 — 驗收已通過，Build ✅ / 188t/0failed / Lint 0 new
- `TogglePassToFriendMode` → `SetCameraMode` 的遷移過程 — 已完成並 commit
- Traycer `-am` 陷阱討論 — 已修正，採用精確 stage 流程
