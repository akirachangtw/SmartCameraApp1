# STATE-CONTRACT-02 (Stub): Legacy _compositionState 殭屍通道清理

**簽發日期**: 2026-06-24
**簽發者**: CSO S18
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: execute_review
**前置依賴**: STATE-CONTRACT-01 ✅ PASS + 真機 AC PASS

## 檔案治理
本票為 STATE-CONTRACT-01 的後續清理票。STATE-CONTRACT-01 導入 GuidanceState 六態後，_compositionState legacy 通道暫時保留供 CompositionGuideOverlay 過渡使用。本票完成殭屍通道的正式廢棄與 consumer 遷移。

## Verify Config
TICKET_ID=STATE-CONTRACT-02
CREATED=2026-06-24
PRESET=refactor
ALLOWED=app/src/main/java/**/CameraViewModel.kt,app/src/main/java/**/CameraState.kt,app/src/main/java/**/CameraScreen.kt,app/src/main/java/**/CompositionState.kt,app/src/main/java/**/CompositionGuideOverlay.kt,app/src/main/java/**/DirectionalIndicator.kt
FORBIDDEN=app/src/main/java/**/LiveCameraSource.kt,plans/architecture/SSOT_v4_0_6.md,.claude/CLAUDE.md,.claude/rules/*.md
MAX_LINES=150
DIFF_REF=[填入 STATE-CONTRACT-01 完成後 HEAD]

## Scope（設計待 STATE-CONTRACT-01 PASS 後細化）

### SC02-1: 廢棄 _compositionState StateFlow
- 移除 CameraViewModel 中 _compositionState 的 emit 邏輯
- 移除 CameraScreen 中 compositionState.collectAsState() 訂閱（L150）

### SC02-2: CompositionGuideOverlay 路由遷移
- CompositionGuideOverlay 從消費 legacy compositionState.guideData 改為消費 state.compositionResult 對應欄位
- 確認 CompositionResult (D2+) 是否有 guideData 等價欄位；若無，需設計 adapter

### SC02-3: DirectionalIndicator 處置
- Phase B 確認 DirectionalIndicator 為 dormant（全 repo 零 call site）
- 裁決：標記 @Deprecated 或直接移除（視 STATE-CONTRACT-01 後 CameraScreen 狀態而定）

### SC02-4: focusUiStateFlow 清理評估
- FocusIndicatorOverlay 為 dormant，focusUiStateFlow 為 defined-but-unconsumed
- 評估是否在本票一併清理，或拆為獨立 cleanup ticket

## Hard Constraints
1. 不得影響 STATE-CONTRACT-01 導入的 GuidanceState 六態 contract
2. 不得 remount FocusIndicatorOverlay
3. 不得修改 LiveCameraSource / CameraRepository 層
4. CompositionGuideOverlay 遷移後的行為必須與遷移前視覺等價（或明確記錄差異）

## Acceptance Criteria
- [ ] CameraScreen 不再 collectAsState(_compositionState)
- [ ] CompositionGuideOverlay 消費 state.compositionResult（或等價路徑）
- [ ] DirectionalIndicator 已標記 @Deprecated 或移除
- [ ] assembleDebug PASS
- [ ] git diff --stat 僅涉及 ALLOWED 檔案

## 來源
本 stub 由 CSO S18 裁決 R-15 產出，基於 PHASE-B-01 SSOT-GAP-01（dead-but-still-subscribed）分析。

---

## CSO Amendment A1（2026-07-12 · SC-04 K4-η STOP 偵察發現歸入）

**來源**: SC-04 執行期 K4-η 消費點對沖攔截 → CSO 唯讀偵察令（Q1/Q2/Q3）→ 裁決記錄 `SC04_K4eta_STOP_Ruling_ShutterRing_PTF`（Obsidian CSO）。

### SC02-5: 快門呼吸環雙真理源遷移（新增 scope 項）
- **現況（偵察實證）**: `ShutterButtonSection`（CameraScreen.kt:550）之快門呼吸環（Canvas testTag=`TAG_SHUTTER_BREATHING_RING`，CameraScreen.kt:597-607）由 legacy `compositionResult.isAllAligned` 驅動（KDoc L543），alpha 硬編碼 `0.10f/0.18f`（L580-581）——與 `state.guidanceState` 驅動之 HaloGuidanceLayer 為**並行雙真理源**，同屏共存非互斥。
- **目標**: 「已對齊」語意單一化——快門環改由 guidanceState 衍生訊號驅動（或明確裁決其保留 legacy 語意並記錄理由）；alpha 硬編碼須常數化（值域裁決依 Postponed #8 之 D15-16 Track B 觀察項回收結果，禁在無感知數據下改值）。
- **前置**: D15-16 Track B 觀察項「雙綠呼吸振幅不同步是否可辨/違和」數據回收。
- **禁區**: 本 amendment 僅登記 scope，AC 待開票細化時依 TICKET_TPL v3.1 AC 類型學補齊（禁預賦，R-T16 O3 同源紀律）。
