# GAP-09-EYEAF-BACKOFF: EyeAF 對焦穩定性 — Failed backoff 接線 + 鎖定保持雙層驗收

**簽發日期**: 2026-07-13
**簽發者**: Claude (CSO)
**Mode**: uc  ||  **Effort**: High（邏輯 Bug：需追蹤 EyeAfController Guard 鏈 × AfState 數據流交互）
**Traycer Workflow**: execute_review（觸 CameraViewModel，SC 領土相鄰）
**前置依賴**: GAP-10-BREATH-RESTART（序列執行；與 GAP-06-TARGET-01 可並行開發、序列落地）

## 檔案治理/來源說明
來源：D15-16 實測報告 §3 症狀①③（對焦不成功 + 重複對焦 = 同一迴路）。實證鏈：`EyeAfController.kt:54` `GAZE_AF_COOLDOWN_MS=2000L` + Guard 4 gaze 未對齊即放行（logcat `gazeScore=84.4`/`isAligned=false`）+ `CameraViewModel.kt:2141-2142` `AfState.Failed(failCount=1)` **硬編碼永不累加** + `FocusTriggerThrottle`（三振暫停 + 指數 backoff）存在但註明 "for Step 5+ usage" **未接線** → 對焦失敗 → 2s 冷卻期滿 → 再觸發 → 無限重對焦迴圈。
**Akira 加註（2026-07-13）**：焦距穩定性為本票核心驗收目標，拆兩層納管（見 AC）。

## Verify Config
TICKET_ID=GAP-09-EYEAF-BACKOFF
CREATED=2026-07-13
PRESET=bugfix
ALLOWED=EyeAfController.kt,CameraViewModel.kt
FORBIDDEN=HaloGuidanceLayer.kt,CameraScreen.kt,GuidanceStateConstants.kt,LiveCameraSource.kt
MAX_LINES=100
DIFF_REF=2ba2b83

## Action Items

### AI-1: failCount 真實累加
- **檔案**: `CameraViewModel.kt`（:2141-2142，行號依 HEAD 語義定位）
- **現值**: `AfState.Failed(failCount = 1)` 硬編碼
- **目標值**: 自前態讀取並累加（`(prevState as? AfState.Failed)?.failCount?.plus(1) ?: 1` 語義），成功對焦即歸零
- [ ] 完成

### AI-2: FocusTriggerThrottle 接線至 EyeAF 觸發路徑
- **檔案**: `EyeAfController.kt`
- **現值**: `FocusTriggerThrottle` 存在但未被 EyeAF 觸發鏈消費；Failed 態無 backoff，僅 2s 固定冷卻
- **目標值**: EyeAF 觸發前查詢 throttle；`AfState.Failed` 依 failCount 進指數 backoff（複用 FocusTriggerThrottle 既有三振暫停 + 指數參數，**禁新增常數**）
- [ ] 完成

### AI-3: 觸發判定 Logcat 遙測補強
- **檔案**: `EyeAfController.kt`
- **現值**: 觸發/攔截無區分 backoff 攔截之 log
- **目標值**: backoff 攔截時輸出 `EyeAF throttled: failCount=N, backoff=Xms`（實機 AC 驗證依據）
- [ ] 完成

## Hard Constraints
1. 禁止修改 `GAZE_AF_COOLDOWN_MS` 數值本身 —— 本票修 Failed 路徑缺失，不重錨正常冷卻
2. 禁止觸碰 Guard 7a（Locked + 無漂移跳過重觸發）判定邏輯 —— 鎖定後漂移層本票**僅觀察不修**（見 AC 第 4 條與票尾條款）
3. FocusTriggerThrottle 若需去除 "for Step 5+ usage" 註記，僅改註解，禁改其內部參數

## Error Handling
- FocusTriggerThrottle 介面與 EyeAF 觸發鏈型別不相容（需改 throttle 簽名才能接）→ 停止回報
- AfState sealed class 需新增成員才能表達 backoff 態 → 停止回報（禁自行擴 State 結構）

## Acceptance Criteria
- [ ] **[可達性]** FocusTriggerThrottle 有 production 消費者，證據欄回填 `EyeAfController.kt:＿＿`
- [ ] **[行為性]** production 路徑驅動：連續對焦失敗 → failCount 1→2→3 累加、backoff 間隔遞增、三振暫停生效（禁 fixture 直注 Failed 態）
- [ ] **[實機]**（焦距穩定層①·失敗迴圈）對螢幕/低對比場景誘發對焦失敗 → logcat **無 2s 週期重觸發**，可見 `EyeAF throttled` 遞增間隔
- [ ] **[實機]**（焦距穩定層②·鎖定保持 — Akira 加註）Locked 態下人臉靜止 **10 秒**：無重觸發、焦距無可見推拉。**本條為觀察閘非修復閘**：若 FAIL，照實記錄漂移現象與 logcat，**禁併入本票修復** → D17 後獨立開票
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

## 票尾條款（教訓 #5 防護：條款優先序自檢）
AC 第 4 條（鎖定保持）與 Hard Constraint 2（禁觸 Guard 7a）**不衝突**：前者為觀察義務，後者為修改禁令。若觀察 FAIL，出口為「開新票」而非「本票內修」——優先序：Hard Constraint 2 > AC 第 4 條的修復衝動。
