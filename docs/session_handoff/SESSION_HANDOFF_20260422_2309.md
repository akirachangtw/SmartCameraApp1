# Cueshot 工作交接 — 20260422_2309

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| — | — | 本次 Session 為純驗測操作，無程式碼修改，無新 commit |

**本次 Session 執行操作摘要**：
- 確認 Sony XQ-BT52 (`HQ616R2713`) Camera 權限狀態：`CAMERA: granted=false` → 手動授予 `pm grant`
- 重新編譯最新 APK（`f1b735d`，Apr 22 22:43）並安裝至 Sony + Pixel 9a
- 擴大 Sony Logcat buffer 至 8MB，解決 CamX verbose log 淹沒問題
- 兩台裝置均確認 BUG-1 存在：`LiveCameraSource.kt:948` `initOrientationListener()` crash

---

## 當前阻塞點

- [x] **BUG-1（🔴 Critical — 阻擋所有驗測）**：`LiveCameraSource.kt:948` 的 `initOrientationListener()` 呼叫 `context.getDisplay()`，但注入的是 `ApplicationContext`（非 Visual Context），在 Android 12+ 拋出 `UnsupportedOperationException`。兩台裝置（Pixel 9a Android 14 + Sony XQ-BT52 Android 12）100% 復現。Camera bind 成功後立即例外，導致 ImageAnalysis / ML Kit / Halo / EdgeWarmth 整條管線無法運作。

---

## 下一步待辦

### 優先：修復 BUG-1（需 CSO 授權）

1. **讀取 `LiveCameraSource.kt` L930-L960**，確認 `initOrientationListener()` 的完整實作
2. **修復方案（擇一）**：
   - 方案 A：將 `getDisplay()` 替換為 `WindowManager` API（Android 30+ 推薦）
     ```kotlin
     // 替換前：context.getDisplay()
     // 替換後：(context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
     ```
   - 方案 B：在 `bindToLifecycle()` 的 lambda 中改傳入 `activityContext`（需確認 Hilt 注入鏈）
3. **修復後執行**：
   - `./gradlew assembleDeviceDebug`
   - ADB 安裝至 Pixel 9a + Sony
   - 執行 Logcat 確認無 `Failed to bind camera UseCases` 例外
   - 繼續 D8-VERIFY-P3 完整驗測流程

### 後續（BUG-1 修復後）

4. **P3 實機手感驗測**（依 `plans/cso_briefing/D1_D8_Verification_Checklist_v1.md`）：
   - Portrait 模式：有人臉時確認琥珀金 Halo 圓環
   - PTF 模式：Halo 引力場偏亮弧
   - roll ≥ 3° 時 EdgeWarmth 即時方向光
   - Logcat 確認 `observePtfAlignment()` 輸出 `isAligned` + `offset`
5. **截圖驗證 UI 純淨度**（P1/P2/P4/P5）：
   ```bash
   adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/P3_verify.png"
   adb -s 59131JEBF17244 pull //sdcard//Download//P3_verify.png tmp/P3_verify.png
   adb -s 59131JEBF17244 shell "rm /sdcard/Download/P3_verify.png"
   ```
6. **P6/P7 CSO 決策**：圖片瀏覽入口補按鈕 vs 推遲 D13；頂部工具列圖示確認
7. **通過後恢復 D9 開發**（ML Kit Object Detection FOOD）

---

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| Sony Camera 權限手動授予（`pm grant`）| App runtime 權限請求流程疑似使用者曾拒絕，Logcat 歷史顯示 `GrantPermissionsActivity` 曾啟動但未完成 | Sony `HQ616R2713` 測試環境 |
| Logcat buffer 擴大至 8MB | Sony CamX/CHIUSECASE verbose log 淹沒 App log（預設 buffer 不足）| Sony 實機偵錯 SOP |
| BUG-1 列為驗測阻擋點（不在 D8-VERIFY-P3 Ticket 範圍內修復）| Ticket 明確禁止自行修補，需 CSO 授權 | `LiveCameraSource.kt` |

---

## 🎯 Context 指標針 (Context Pointers)

（新 Session Agent 請主動執行以下指令，**嚴禁依賴歷史記憶**）

1. **確認最新 commit 狀態**（本次 Session 無新 commit）：
   ```bash
   git log --oneline -6
   ```

2. **核心檔案最新狀態**：
   - Read `.claude/PROGRESS.md`（確認 D8-VERIFY 狀態）
   - Read `CLAUDE.local.md`（ADB device ID）

3. **BUG-1 根本原因檔案**：
   - Read `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt` L930-L970（`initOrientationListener` 函數）
   - Read `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt` L1290-L1310（`bindToLifecycle$lambda$25` 呼叫點）

4. **驗測 Checklist**：
   - Read `plans/cso_briefing/D1_D8_Verification_Checklist_v1.md`

---

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（ADB 安全格式、焦土政策、最高憲法）
- `.claude/PROGRESS.md` — 當前狀態：D8-VERIFY ✅ CLOSED，P3 實機驗測被 BUG-1 阻擋
- `CLAUDE.local.md` — ADB device ID（Pixel 9a `59131JEBF17244`、Sony `HQ616R2713`）
- `plans/cso_briefing/D1_D8_Verification_Checklist_v1.md` — P3/P6/P7 待驗清單
- `app/src/main/.../data/source/camera/LiveCameraSource.kt` — BUG-1 修復目標（L948）

## 不需帶入的 Context

- Logcat buffer 擴大操作（已執行，Sony 8MB 設定在重啟後需重設）
- Sony Camera 權限授予過程（已完成，`CAMERA: granted=true`）
- APK 版本排查過程（舊版 Apr 20 vs 新版 Apr 22，已確認安裝最新版）
- 四個指定 tag（FaceDetection/MLKit/ImageAnalysis/CompositionEngine）無輸出的診斷過程（根因已確認為 BUG-1）
