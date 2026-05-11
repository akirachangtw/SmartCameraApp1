# CSO 戰略對話 Handoff — W1.5-R 授權 + BHA 統一裁決
**Date**: 2026-05-11 (Session 5)
**前接**: Session 4 Handoff (2026-05-09)
**CSO 審查來源**: Claude Opus CSO + GPT CSO 雙重裁決交叉比對
**消費者**: Gemini 執行參謀長 → Claude Code → Traycer

---

## Executive Summary

T02 Hard Gate 未通過，暴露座標空間錯配（rotation-aware 歸一化缺失）。BHA（Boundary Health Audit）量化 Camera 模組健康度為 **32/100（Red）**，確認 CameraViewModel.kt 為 2457 行 God Object，Domain 層有 23 hits android.* import 跨 15 檔案。

雙 CSO 報告（Claude + GPT）統一裁決：
1. **Phase 0**：只修 2 個 call site 的 rotation swap（不做任何重構）
2. **Phase 1**：獨立票抽出 CoordinateTransformer（W2 前完成）
3. **Phase 2**：恢復 W2（T03→T04→T06）
4. **Phase 3**：W3 前抽出 DetectionResultProcessor
5. **Phase 4**：Post-MVP Domain 邊界修復

Sprint 天數彈性已確認：**完成有商業價值 APP > Sprint 天數硬限**。

---

## Done（本 Session 完成事項）

- **W2 Contract 2 已簽發**：T02→03→04→06，含 Risk #1 Hard Gate + Risk #3 str_replace 防禦
- **T02 執行完畢**：Layout 5 項驗收 4 PASS / 1 FAIL（Hard Gate 座標偏移 2.25×）
- **Session Boundary Rule 修訂**：「禁止跨 session」升級為「每次 str_replace 前強制 re-view 完整檔案」
- **BHA-01 診斷完成**：Claude Code 執行 4 切面 grep 診斷，GPT 複查修正數據
- **雙 CSO 交叉比對完成**：Claude CSO + GPT CSO 統一裁決產出
- **四階段 Strangler Fig 執行序列確定**：Phase 0-4 票規格完成
- **KAIROS #3 議程擴充至 8 項**：新增 K3-7（Session Rule 修訂）+ K3-8（Wave 前 Smoke Test SOP）
- **gstack v1.5 工作流確認**：CSO 技術決策 Ticket → Gemini 執行參謀長

---

## Current State

| 項目 | 狀態 |
|------|------|
| W1 | ✅ CLOSED |
| INFRA | ✅ CLOSED |
| Pre-W2-A~D | ✅ CLOSED |
| T02 Layout (非 Hard Gate 部分) | ✅ 4/5 PASS |
| T02 Hard Gate (座標) | 🔴 FAIL — 待 Phase 0 修復 |
| W2 Contract 2 | ⏸️ SUSPENDED（Hard Gate 未通過） |
| Camera 邊界健康 | 🔴 32/100 |

---

## BHA 統一裁決（雙 CSO 合議）

### 根因確認

`CameraViewModel.kt` 的 `observeCompositionAdvice()` 和 `observePtfAlignment()` 使用 `det.imageWidth`（480）/ `det.imageHeight`（640）做歸一化，但 ML Kit `Face.boundingBox` 已在 rotated space（640×480）。rotation=90 時，face_x 可達 640，除以 480 後 normalized_x > 1.0，Halo 繪製在畫面外或錯位 2.25 倍。

### D1-D8 逐項裁決表

| Gate | 切面 | 健康分 | T02 相關 | 裁決 | 後續票號 |
|------|------|--------|---------|------|---------|
| D1 | Domain→Data（DetectionResult 持有 ML Kit 型別） | 35 | 否 | 延遲至 Post-MVP | ARCH-01 |
| D2 | Domain→Framework（15 檔有 android.* import） | 30 | 否 | 延遲至 Post-MVP | ARCH-02 |
| D3 | ViewModel 職責膨脹（2457 行） | 15 | 間接 | Phase 1+3 分期抽取 | REFACTOR-COORD-01, REFACTOR-DETECT-01 |
| D4 | Repository 隔離（domain repo 洩漏 CameraX） | 20 | 否 | 延遲至 Post-MVP | ARCH-03 |
| D5 | 座標 SSOT（rotation 歸一化錯配） | 0 | **直接根因** | **Phase 0 立即修** | BHA-01-FIX |
| D6 | contentBounds/letterbox 未納入 mapping | 40 | 殘留風險 | Phase 0 後獨立票 | T02B-CONTENTBOUNDS |
| D7 | AF 雙份狀態機（presentation + domain） | 45 | 否 | Phase 1.5 債務 | ARCH-04 |
| D8 | EyeAf 無條件重觸發（每 2s） | 40 | 否 | T02 後第二優先 | FIX-EYEAF-01 |

### 可複用器官（Reusable Organs）

保留並信任：
- Composition rule classes（消費 normalized [0,1] 的 FaceLandmarkData）
- `CompositionRule.evaluate()` 座標契約
- `NormalizedToPixelTransformer`（前提：上游 normalized 座標正確）
- `PreviewViewGeometryMapper` / rotation-aware effective-dimension pattern
- `CameraRepositoryImpl` 作為 CameraX/ML Kit 的存放處
- AmbientConstants SSOT（W1 成果）
- isAllAligned 聚合邏輯（W1 成果）
- PTF sealed class 架構
- 224 unit tests（邏輯分支覆蓋）

不可視為穩定 SSOT：
- `DetectionResult` 作為 domain model（攜帶 ML Kit Face/DetectedObject）
- `CameraRepository` 作為 domain port（洩漏 CameraX/PreviewView）
- `CameraViewModel` 作為座標/AF/detection 的所有者

---

## 四階段執行序列（Strangler Fig Pattern）

### Phase 0：BHA-01-FIX（P0，立即執行）

**目標**：修復 T02 Hard Gate，恢復座標正確性。

```
/uc
Intent: BHA-01-FIX — Fix rotation-aware normalization in two call sites.

Two sites to fix:
1. CameraViewModel.observeCompositionAdvice()
2. CameraViewModel.observePtfAlignment()

Required change:
val rotation = det.rotationDegrees
val imgW = (if (rotation == 90 || rotation == 270) det.imageHeight else det.imageWidth).toFloat()
val imgH = (if (rotation == 90 || rotation == 270) det.imageWidth else det.imageHeight).toFloat()

Verification (Gate 1 + Gate 2):
- Log: raw imageSize, rotation, effective size, normalized bbox
- rotation=0 and rotation=90: normalized x/y/w/h all within [0, 1]
- Sony device: face bbox/halo alignment confirmed visually
- 人臉在畫面左側 → Halo 不在右側
- 人臉在畫面中央 → Halo 不偏移到邊緣
- PTF Halo 與 Composition Halo 同源 bbox 同位置
- 前鏡頭 mirror 行為不因本修復改變
- Unit test: rotation=0 和 rotation=90 兩種 case

Non-Regression (Gate 3):
- 不改 public domain model
- 不改 repository interface
- 不改 overlay rendering contract
- 不改 ML Kit detector
- 不改 CameraX binding

Scope: CameraViewModel.kt ONLY, 2 call sites ONLY.
Do NOT refactor, extract, or create new models.
```

**驗收後**：T02 Hard Gate 重新提交，PASS 後進入 Phase 1。

**已知殘留風險（Phase 0 不修，記錄即可）**：
- D6 contentBounds/letterbox mapping（→ T02B-CONTENTBOUNDS）
- `NormalizedToPixelTransformer` 使用 Canvas 全螢幕而非 Preview contentBounds

---

### Phase 1：REFACTOR-COORD-01（P0，Phase 0 後立即執行）

**目標**：從 ViewModel 抽出座標映射邏輯，建立統一 CoordinateTransformer。

**為什麼不能推遲**：W2 的 T04（PTF Entry）和 T06（Scene State）都碰 CameraViewModel.kt 且涉及座標邏輯。不抽出 = 在 2457 行 + 74 座標 hit 中做 str_replace，風險不可接受。

**GPT CSO 相容性說明**：GPT 要求「不同票做」——Phase 0 和 Phase 1 是兩張獨立票、兩次 commit、兩次驗證。滿足「Hard Gate 不是架構清潔日」原則。Phase 1 是 Hard Gate 過後的下一張票，不是同一張。

```
/uc
Intent: REFACTOR-COORD-01 — Extract CoordinateTransformer from CameraViewModel.

Steps:
1. Create presentation/camera/coordinator/CoordinateTransformer.kt
2. Move ALL coordinate normalization logic from CameraViewModel into Transformer
   - observeCompositionAdvice() 中的歸一化段
   - observePtfAlignment() 中的歸一化段
   - rebuildNewPipelineSensorToViewMatrix() 邏輯
   - 所有 boundingBox → normalized rect 的轉換
3. Transformer API: 
   fun normalize(bbox: Rect, frameWidth: Int, frameHeight: Int, rotation: Int): NormalizedRect
4. CameraViewModel 改為呼叫 Transformer，不保留任何 inline 座標邏輯
5. Update unit tests: import path changes only

Acceptance:
- CameraViewModel.kt 減少 ~150+ 行
- grep "boundingBox.*normalize\|imgW\|imgH" CameraViewModel.kt → 0 hits
- 所有既有 unit tests pass
- ./gradlew assembleDebug pass
- Sony device re-verification: face bbox alignment unchanged（與 Phase 0 結果一致）

Scope: CameraViewModel.kt + new CoordinateTransformer.kt
Do NOT modify domain models, repository interface, or overlay rendering.
```

---

### Phase 2：恢復 W2（T03→T04→T06）

**前提**：Phase 0 + Phase 1 完成且驗證通過。

W2 Contract 2 的票指令**需 Gemini 參謀長修訂以下內容**：
- T04 scope 更新：座標相關操作改為呼叫 CoordinateTransformer（非 ViewModel inline）
- T06 scope 確認：SceneState 如需 detection 位置資訊，透過 Transformer 取得
- Session 策略更新：「禁止跨 session」規則已修訂為「每次 str_replace 前強制 re-view 完整檔案」，/compact 後繼續不視為違規
- 中間 /qa /sv 策略：T03/T04 完成後只跑 `./gradlew assembleDebug`，最終 /qa + /sv 在 T04 或 T06 後一次性執行

---

### Phase 3：REFACTOR-DETECT-01（W2 完成後、W3 前）

**目標**：從 ViewModel 抽出 ML Kit callback 處理，建立 DetectionResultProcessor。

**為什麼 W3 前必須完成**：T08（ML Kit Detector 互斥強制）需要修改 detector 切換邏輯。如果切換邏輯還在 2300 行 ViewModel 中（Phase 1 後），T08 的 str_replace 風險仍然很高。抽出後 T08 在獨立模組中操作。

```
/uc
Intent: REFACTOR-DETECT-01 — Extract DetectionResultProcessor from CameraViewModel.

Steps:
1. Create presentation/camera/processor/DetectionResultProcessor.kt
2. Move ML Kit callback handling, face/object result filtering,
   detection result distribution from CameraViewModel
3. ViewModel observes Processor's output StateFlow instead of raw ML Kit results
4. T08 (ML Kit Mutex) will operate on Processor, not ViewModel

Acceptance:
- CameraViewModel.kt reduced by ~200+ lines
- grep "Face\|FaceDetector\|ObjectDetector\|DetectedObject" CameraViewModel.kt → <10 hits
- ./gradlew assembleDebug pass
- All existing detection-related unit tests pass
```

---

### Phase 4：Post-MVP Domain 邊界修復（不阻塞 Sprint）

登記為 Boundary Health Debt Register，不排入 Sprint：

| 票號 | 內容 | 來源 Gate |
|------|------|----------|
| ARCH-01 | DetectionResult 去 ML Kit 化（domain adapter） | D1 |
| ARCH-02 | Domain 層去 android.* 依賴（自定義幾何型別） | D2 |
| ARCH-03 | CameraRepository port 清洗（去 CameraX 洩漏） | D4 |
| ARCH-04 | AF 雙狀態機合併 | D7 |
| T02B-CONTENTBOUNDS | contentBounds/letterbox 納入 overlay mapping | D6 |
| FIX-EYEAF-01 | EyeAfController focused guard + cooldown | D8 |

---

## 風險矩陣（更新版）

| # | 風險 | 嚴重度 | 狀態 | 對沖 |
|---|------|--------|------|------|
| 1 | Layout 改動破壞 ML Kit 座標映射 | Critical | 🔴 已觸發（T02 Hard Gate FAIL） | Phase 0 修復 |
| 2 | ML Kit detector 切換 500ms 舊 callback | Critical | 🔲 W3 觸發 | Phase 3 REFACTOR-DETECT-01 |
| 3 | CameraScreen.kt str_replace 失敗 | High | 🟡 緩解（re-view 規則已修訂） | 每次 str_replace 前 re-view |
| 4 | CameraViewModel.kt 7 票碰撞累積 | High | 🟡 緩解中 | Phase 1+3 降碰撞至 3-4 票 |
| 5 | Scene 切換未重置 EMA → Halo 殘影 | Medium | 🔲 W2' 觸發 | T06 驗收含 EMA 歸零斷言 |
| 6 | Paywall blur z-order 被打亂 | Medium | 🔲 W5 觸發 | 12B 放 W5 最後 |
| 7 | contentBounds/letterbox 殘留偏移 | Medium | 📋 已登記 | T02B-CONTENTBOUNDS |
| 8 | EyeAf 每 2s 無條件重觸發 | Medium | 📋 已登記 | FIX-EYEAF-01 |
| 9 | Domain 層 android.* 污染影響測試隔離 | Low (短期) | 📋 Phase 4 | ARCH-02 |

---

## Key Decisions（Session 5）

| 決策 | 理由 | 來源 |
|------|------|------|
| T02 Hard Gate 根因確認：rotation-aware 歸一化缺失 | BHA 診斷 + 雙 CSO 合議 | BHA-01 + 雙 CSO |
| Phase 0 只修 2 個 call site，禁止同票重構 | GPT CSO Gate 0-3 框架 | GPT CSO |
| Phase 1 CoordinateTransformer 在 W2 前抽取 | T04/T06 碰座標，不抽 = 高風險 str_replace | Claude CSO |
| Phase 3 DetectionResultProcessor 在 W3 前抽取 | T08 ML Kit 互斥需獨立模組操作 | Claude CSO |
| Session Boundary Rule 修訂 | re-view > session 邊界；/compact 後繼續合法 | Claude CSO Session 5 |
| Sprint 天數彈性確認 | 完成有商業價值 APP > Sprint 天數硬限 | Akira |
| BHA 原始數據需修正 | Framework type 洩漏低報（37→70 hits） | GPT 複查 |
| D6 contentBounds 獨立開票 | 與 rotation 修復分離，避免驗證面擴大 | GPT CSO |
| gstack 工作流遵循 | CSO 決策 Ticket → Gemini 參謀長執行 | Akira 確認 |

---

## KAIROS #3 議程（累計 8 項）

| 編號 | 議題 | 來源 |
|------|------|------|
| K3-1 | Debug Overlay Zero Tolerance Rule | D9 截圖 |
| K3-2 | Hardcoded Text Scanning at /sv gate | D9 截圖 |
| K3-3 | KAIROS Real-Device Screenshot 制度化 | D9 截圖 |
| K3-4 | Decommission glob pattern | INFRA-02 |
| K3-5 | SSOT 衝突裁決 SOP | W1 經驗 |
| K3-6 | Payload scope 一致性 | cross_review 經驗 |
| K3-7 | Session Boundary Rule 修訂（re-view 取代 session 邊界） | T02 /compact 經驗 |
| K3-8 | Wave 前 Real-Device Smoke Test SOP | T02 Hard Gate 暴露 D1-D8 既存債 |

---

## 需要載入的文檔（新 Gemini 參謀長 Session）

| 文檔 | 來源 | 用途 |
|------|------|------|
| `Cueshot_CSO_Knowledge_Base_v2.md` | Project Knowledge | 產品 DNA + gstack + 技術護欄 |
| `Cueshot_Execution_Sequence_v1.md` | 上傳 | 17 票總覽 + 波次序列（Phase 1+3 修訂基準） |
| 本 Handoff 文件 | 上傳 | Session 5 決策鏈 + 四階段執行序列 + 票規格 |
| `bha_cso_report.md` | 上傳 | BHA 量化數據 + GPT 修正 |
| `T02_Hard_Gate診斷報告GPT-CSO.md` | 上傳 | GPT CSO D1-D8 逐項裁決 + Gate 0-3 框架 |

---

## Gemini 參謀長 Action Items

| # | 動作 | 產出 | 優先級 |
|---|------|------|--------|
| 1 | 將 Phase 0（BHA-01-FIX）打包為 Claude Code 可執行 Contract | BHA-01-FIX Contract | P0 |
| 2 | 將 Phase 1（REFACTOR-COORD-01）打包為 Claude Code 可執行 Contract | REFACTOR-COORD-01 Contract | P0 |
| 3 | 修訂 Execution Sequence v1 → v1.1：插入 W1.5-R、更新 T04/T06 scope | Execution Sequence v1.1 | P0 |
| 4 | 將 Phase 3（REFACTOR-DETECT-01）打包（W2 完成後觸發） | REFACTOR-DETECT-01 Contract | P1 |
| 5 | 登記 Boundary Health Debt Register（ARCH-01~04 + T02B + FIX-EYEAF） | Debt Register 文件 | P1 |
| 6 | 更新 gstack v1.5 混合工作流文檔（歸檔 BHA 決策鏈） | gstack v1.5 更新版 | P2 |

---

## Next Steps

1. 開新 Gemini 參謀長 session，載入上述 5 份文檔
2. Gemini 執行 Action Items #1-#3（P0）
3. Claude Code 執行 Phase 0 → Phase 1 → Device 驗證
4. Phase 0 PASS 後 → T02 Hard Gate 重新提交 CSO
5. Phase 1 PASS 後 → W2 Contract 2 恢復（RESUME）
6. W2 完成後 → Phase 3 → W3 啟動

---

**End of Session 5 Handoff**
