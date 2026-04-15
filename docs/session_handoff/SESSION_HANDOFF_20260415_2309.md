# Cueshot 工作交接 — 2026-04-15 23:09

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `presentation/camera/CameraViewModel.kt` | companion object | 新增 `isPtfCooldownActive(now, lastCaptureTimeMs, cooldownMs)` internal pure fn（Rule 16 可測試抽象） |
| `presentation/camera/CameraViewModel.kt` | 欄位宣告 | 新增 `internal var timeProvider: () -> Long` test seam；`capturePhoto()` 改用 `timeProvider()` + `isPtfCooldownActive()` |
| `presentation/camera/CameraViewModelTest.kt` | 新增測試區塊 | 4 個 Rule 16 負向測試：哨兵首發不攔截、三步驟冷卻序列(500ms/1001ms)、邊界 1000ms 通過、邊界 999ms 攔截 |
| `data/preferences/PtfPreferences.kt` | 全新建立 | DataStore 封裝 `ptf_onboarded` flag；`isPtfOnboarded: Flow<Boolean>`；`markPtfOnboarded()` |
| `di/AppModule.kt` | `providePtfPreferences()` | `@Provides @Singleton PtfPreferences` 新增 |
| `presentation/camera/CameraScreen.kt` | `ShutterButtonSection` | `cameraMode: CameraMode` 參數；`animateDpAsState` 72→80dp（Rule 14 置頂）；進度環 84→94dp |
| `presentation/camera/CameraScreen.kt` | PTF exit button | `if (isPtfActive)` TopEnd IconButton，點擊 `SetCameraMode(Normal)` |
| `presentation/camera/CameraScreen.kt` | onboarding hint | `AnimatedVisibility` (fadeIn+slideInVertically)；`isPtfOnboarded.collectAsState(initial=true)` 防閃現 |
| `presentation/camera/CameraState.kt` | `CameraUiEvent` | 新增 `DismissPtfOnboarding` event |
| `presentation/camera/CameraViewModel.kt` | constructor / 方法 | 注入 `ptfPreferences`；暴露 `isPtfOnboarded: Flow<Boolean>`；新增 `dismissPtfOnboarding()` |
| `res/values/strings.xml` | PTF 字串 | 新增 `ptf_exit_cd`、`ptf_onboarding_message`、`ptf_onboarding_got_it` |
| `.claude/skills/sv/SKILL.md` | 階段 A.5 | 新增 Android 16KB ELF 對齊驗證步驟；執行 `tools/check_elf_alignment.sh`；🟡 警告級 |

## Git Commits（本 Session）

| Hash | 類型 | 內容 |
|------|------|------|
| `c4da634` | Code | D7-Batch2 全部 .kt + .xml（PTF UI + onboarding + Rule 16 tests） |
| `0537ee9` | Docs | PROGRESS.md 封板（192t/0failed，D7-B2 ✅ DONE） |
| `e54fa2a` | Docs | 三個 session handoff 歸檔（1845/1910/2120） |
| `858b0a2` | Docs | D6/D7 Gemini briefing 歸檔 |
| `eec9bae` | Docs | /sv SKILL.md 新增 16KB ELF 檢查 |

## 當前阻塞點

無

## 下一步待辦

**D7-Batch3 — 實機回歸驗證（Pixel 9a `59131JEBF17244`）**

1. 安裝最新 device build：
   ```bash
   ./gradlew assembleDeviceDebug
   adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
   ```

2. 驗證 PTF 快門動畫：進入 PTF 模式，確認快門按鈕 72→80dp 動畫（300ms），進度環同步 94dp。

3. 驗證 ✕ 退出按鈕：PTF 模式下右上角出現 ✕，點擊後切回 Normal 模式，確認按鈕消失。

4. 驗證首次教學 hint：卸載重裝（清除 DataStore），進入 PTF 模式，確認 hint 以 fadeIn+slideInVertically 出現；點「知道了」後永久消失（重進 PTF 不再顯示）。

5. 驗證 Rule 16 冷卻：PTF 模式下快速連按快門，確認 1000ms 內第二次按下無反應（Logcat 顯示 `[PTF-Cooldown] Shutter blocked`）；1001ms 後可再次拍攝。

6. 驗證 `resetFocusState()` 冷卻 sentinel 重置：對焦後切換回 Normal 再切回 PTF，確認第一次快門不被攔截。

7. Batch3 通過 → 執行 D7 封板 commit + 呼叫 Traycer `strict_verification` 終審。

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `isPtfCooldownActive()` 提取為 companion object pure fn（非注入 TimeProvider 到 constructor） | 最小侵入：不改 Hilt constructor 簽名；與現有 `computeFocalLengthPixels()` 模式一致；pure fn 無需 mock 依賴 | `CameraViewModel.kt`、`CameraViewModelTest.kt` |
| `internal var timeProvider: () -> Long` 作為 test seam | 允許測試注入虛擬時鐘，生產環境預設 `System.currentTimeMillis()`，zero overhead | `CameraViewModel.kt` |
| `/sv` SKILL 新增 16KB ELF 檢查為 🟡 警告級（非 🔴 阻斷） | Android 15+ 強制要求，但 debug build 不影響執行；Release 前清零即可 | `.claude/skills/sv/SKILL.md` |
| `isPtfOnboarded.collectAsState(initial = true)` | 初始值 `true` 防 DataStore 未載入時 hint 閃現再消失（防呆設計） | `CameraScreen.kt` |

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（特別是 Rule 14、16）
- `.claude/PROGRESS.md` — D7 當前狀態（Batch2 ✅ DONE，Batch3 待執行）
- `CLAUDE.local.md` — ADB device ID（Pixel 9a `59131JEBF17244`）

## 不需帶入的 Context

- D7-B2-T2 Rule 16 測試設計討論 — 已 commit (`c4da634`)，`isPtfCooldownActive` 純函數設計已定案
- TimeProvider 方案比較（注入 vs companion fn vs 反射）— 已採用 companion fn，決策已歸檔
- Batch2 原子化 commit 流程 — 已完成，working tree 乾淨
- Session handoff / Gemini briefing 歸檔 — 已 commit，無後續動作
- `/sv` SKILL.md 16KB 整合討論 — 已 commit (`eec9bae`)
- QA 報告細節（192t/0failed）— 已寫入 PROGRESS.md
