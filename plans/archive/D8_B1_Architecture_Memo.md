# D8-B1 架構決策備忘錄
Date: 2026-04-17
Scope: PTF 規則引擎 + Halo 牽引架構對齊
Mode: /normal 階段產出

---

## 1. 核心架構決策（Finalized）

### 決策 1 — PtfRuleAggregator vs PriorityEngine 隔離原則
- **決策**：`PtfRuleAggregator` 為獨立類別，完全不繼承、不呼叫 `PriorityEngine`。
- **理由**：PTF 模式採 AND-Gate 全滿足邏輯；PriorityEngine 採加權最大值 OR 模型，語意根本不同，強行共用會污染 Normal 模式邏輯。
- **否決方案**：繼承/擴展 PriorityEngine → 否決，因會在 Normal 模式權重引擎中引入 PTF 條件分支，違反 SSOT §4.1 純粹性。
- **影響範圍**：`PriorityEngine.kt`（禁區，零修改）；`PtfRuleAggregator.kt`（新建）；`CompositionModule.kt`（新增獨立 @Provides 綁定）。

---

### 決策 2 — AND-Gate 聚合邏輯
- **決策**：所有 PTF 條件（人臉偵測到、臉置中、對齊度達標）同時為 true 才輸出 `isAligned = true`；任一不滿足即輸出 false + offset 向量。
- **理由**：PTF 場景需要「全部就緒才觸發」語意；若用 OR/加權模型，部分對齊時會誤觸「可以拍了」。
- **否決方案**：加權分數閾值（≥ 70 即算對齊）→ 否決，過於模糊，難以解釋給使用者。
- **影響範圍**：`PtfRuleAggregator.evaluate()` 內部邏輯；後續 `HaloGuidanceLayer` 顯示邏輯的 isAligned 判斷。

---

### 決策 3 — 牽引向量資料型別（PtfAlignmentResult / PtfOffset）
- **決策**：`PtfAlignmentResult(isAligned: Boolean, offset: PtfOffset, failedRules: List<PtfRuleName>)`，其中 `PtfOffset(dx: Float, dy: Float)` 為純 Kotlin data class；`failedRules` 依 FRAMING > ANGLE > LEVEL 優先級排序，供 UI 層映射對應視覺回饋。
- **理由**：`androidx.compose.ui.geometry.Offset` 屬 Compose UI 套件，用在 Domain 層違反 Rule 13（Domain 零 Android/Compose SDK 依賴）。純 Kotlin data class 可在 JVM unit test 直接測試，無需 Robolectric。
- **否決方案**：直接用 `androidx.compose.ui.geometry.Offset` → 否決，Domain 層引入 Compose 依賴，且 JVM 測試需 stub。
- **影響範圍**：`PtfAlignmentResult.kt`（新建含 PtfOffset + PtfRuleName）；`HaloGuidanceLayer.kt`（將 PtfOffset 轉換為 Compose Offset 於 UI 層）。
- **座標系裁定**：座標系經 CSO 裁定為 normalized [0,1]，SSOT 補丁 §3.3/§3.4 已同步修訂。

---

### 決策 4 — DI 綁定策略
- **決策**：使用 `@Provides`（非 `@Binds @IntoSet`）+ `@PtfEngine` `@Qualifier` 在 `CompositionModule.kt` 獨立提供 `PtfRuleAggregator`。
- **理由**：`PtfRuleAggregator` 不屬於 Normal 模式的 `Set<CompositionRule>`，不能加入 multibinding set，否則 `AnalyzeCompositionUseCase` 會在 Normal 模式下也執行 PTF 邏輯。
- **否決方案**：`@Binds @IntoSet` 加入現有 Set → 否決，Normal 模式會混入 PTF 邏輯，PTF 狀態機將被汙染。
- **影響範圍**：`CompositionModule.kt`（新增 `@PtfEngine @Qualifier` + `@Provides fun providePtfRuleAggregator`）；`CameraViewModel.kt`（注入時加 `@PtfEngine`）。

---

### 決策 5 — Halo 元件共用策略
- **決策**：[未討論 - 需回頭補齊]
- **備註**：D8-B1-T1 聚焦 Domain 層介面定義；Halo UI 元件複用策略屬 D8-B2 範圍，需在 B2 /normal 階段補充確認是否沿用 `HaloGuidanceLayer` 或新建 `PtfHaloLayer`。

---

### 決策 6 — Face BBox 抖動對沖（EMA 濾波）
- **決策**：EMA(α=0.3) 由 ViewModel 映射層完成，`PtfRuleAggregator` 純接收端，不自持狀態。
- **理由**：Aggregator 保持無狀態純函數，可直接 JVM 單元測試，不需 mock 時序。EMA 的 α=0.3 由上游決定，Aggregator 不關心來源平滑度。
- **否決方案**：Aggregator 內建 EMA → 否決，引入時序狀態，破壞純函數性，測試複雜度暴增。
- **影響範圍**：`CameraViewModel.kt`（D8-B2/B3 接線時需在 face 座標映射層加 EMA 邏輯）；`PtfRuleAggregator.kt`（確保無任何 mutable state 欄位）。

---

### 決策 7 — Rule 16 是否延伸為共用 ThrottleGuard
- **決策**：[未討論 - 需回頭補齊]
- **備註**：Rule 16 目前僅在 `CameraViewModel.isPtfCooldownActive()` 中實作。是否提取為通用 `ThrottleGuard` 工具類，留至 D10 KAIROS #3 收割時評估。

---

## 2. 前瞻性考量（Forward-Looking）

### D9 美食模式共用性
- **共用點**：`PtfRuleAggregator` 的 AND-Gate 模板（全滿足才 isAligned）可作為 FoodCompositionAggregator 的設計參考。
- **必須擴展的點**：D9 Food 模式數據源為 Object Detection bbox（非 Face Detection），需另建獨立 aggregator，不可直接繼承 PTF 版本。

### Phase 1.5 TiltRule / SmileRule 擴展
- 若加入 Normal 模式，走現有 `@Binds @IntoSet` OCP 路徑，零改動 `PriorityEngine`。
- 若是 PTF 模式專屬規則，在 `PtfRuleAggregator` 的 AND 條件列表中增加一個判斷分支即可。

---

## 3. 已識別的實作陷阱（Known Pitfalls）

1. **`FaceLandmarkData` 使用 `android.graphics.RectF/PointF`（SSOT-TYPE-DRIFT-01）**：
   `PtfRuleAggregator` 接收此型別作為輸入，但其「計算 offset 的內部邏輯」只能操作 `.left/.top/.right/.bottom` float 值，不可引入其他 Android SDK 方法，確保可在 JVM 純測試環境下 mock 輸入。

2. **BBox 中心點計算座標系**：
   ML Kit face bbox 在 PTF 模式與 Normal 模式使用相同的 `FaceLandmarkData` 管線，座標系一致（normalized [0,1]）。但 offset 輸出為 **view-space px**，需在 Aggregator 內部乘以 `previewWidth/Height`，此乘數必須由外部傳入（不可 hardcode）。

3. **`CameraViewModel` 切換 Aggregator 時的競態**：
   `CameraMode` 切換（Normal ↔ PassToFriend）與 ML Kit 偵測幀到達可能並發。需確保 ViewModel 在切換模式時 `detectedFaces` 的 flow 不會帶入前一模式的舊數據（使用 `distinctUntilChanged` 或在 PTF 模式進入點清空 state）。

---

## 4. 超出範圍的議題（Deferred Topics）

| 議題 | 延後至 |
|------|--------|
| Halo UI 元件共用 vs 新建策略 | D8-B2 /normal 階段 |
| `ThrottleGuard` 共用工具類提取 | D10 KAIROS #3 |
| `FaceLandmarkData` 遷移至純 Kotlin 型別（SSOT-TYPE-DRIFT-01） | D10 後技術債票 |
| EMA α=0.3 參數調校（目前硬編碼） | D14 IFTR 埋點後依數據調整 |

---

## 5. 待 Akira 最終拍板的項目（Pending Human Decision）

1. **`previewWidth/Height` 傳入方式**：`PtfRuleAggregator.evaluate()` 計算 view-space offset 需要 viewport 尺寸。方案 A：由 caller（ViewModel）傳入；方案 B：Aggregator 只輸出 normalized offset（dx/dy 在 [0,1]），UI 層自行轉 px。建議選 **方案 B**（Domain 層保持正規化，Rule 13 更嚴謹），但需 Akira 確認。

2. **`@PtfEngine` Qualifier 命名**：是否同意使用 `@PtfEngine` 作為 Qualifier 名稱，或偏好其他命名（如 `@PassToFriend`、`@PtfAggregator`）？