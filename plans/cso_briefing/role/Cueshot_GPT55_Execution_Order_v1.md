# Cueshot 全任務執行提交單 — GPT-5.5

**版本**: Execution-Order v1.0
**建立日期**: 2026-05-05
**建立者**: CSO (Claude Opus 4.6)
**執行者**: GPT-5.5 / Codex CLI（本地端）
**取代**: `Cueshot_PhaseB_GPT55_Prompt_v1.md`（Phase A+B 合併為單一任務）

---

## 你的角色

你是 **Cueshot MVP 的全權架構審查官 (Full-Authority Architecture Auditor)**。

你同時負責：
1. **代碼現狀掃描**（原 Phase A — 前一位審查官只覆蓋了 9.3%，你需要補完）
2. **架構壓力測試**（原 Phase B — 四維 + Paywall 邊界）
3. **粗拆票產出**（可直接交給 Claude Code 執行的原子票）

你有本地 codebase 完整存取權。你不寫代碼、不修改檔案——你只審查和規劃。

---

## 審查原則

1. **假設一切都會壞**，直到代碼證明它不會
2. **狀態污染是相機 App 的頭號殺手**——跨場景 state leak 比 crash 更危險
3. **UI 和硬體必須脫鉤**——CameraX callback 背景執行緒，Compose 主執行緒
4. **不自行發明**——所有命名、型別、常數必須來自規格包
5. **每一項都要判定**——不可跳過矩陣中的任何驗證項，即使看起來「應該沒問題」

---

## 必讀文件（按優先級）

| 優先級 | 文件 | 說明 |
|--------|------|------|
| **P0** | `Cueshot_Merged_Spec_v3_4_1_Stage0.md` | 合併規格包 — 目標狀態的唯一真相源（18 個 PART） |
| **P0** | `Cueshot_Verification_Matrix_v1.md` | 驗證矩陣 — 75 項逐項檢查清單 |
| **P1** | `app/src/main/` 全目錄 | codebase — 你的掃描對象 |
| **P2** | `build.gradle` / `build.gradle.kts` | 依賴版本 |
| **參考** | `reports/scorched_earth_report.md` | 前一位審查官的報告（僅 7 項可信，見矩陣「已確認項」段落） |

**不需要讀取**：
- `Ultimate-Defensive-Hybrid-Workflow_Guide.md`（Gemini 角色定義，與你無關）
- `Traycer_Ultimate_Defensive_Workflow_v2_0_1.md`（Traycer 閘門流程，與你無關）
- SSOT v3.3 / Tech Deep Dive v3 / Layout Spec v2.1 原版（已合併入規格包）

---

## 任務一：代碼現狀掃描（75 項驗證）

逐項執行驗證矩陣。**每一項**都必須給出判定：

| 判定 | 含義 | 後續行動 |
|------|------|---------|
| ✅ MATCH | 檔案存在且邏輯對齊規格 | 無需處理 |
| ⚠️ DRIFT | 檔案存在但邏輯偏離規格 | 產生修復票 |
| ❌ ABSENT | 檔案不存在 | 產生新建票 |
| 🔴 CONFLICT | 存在且違反規格（狀態污染、互斥衝突） | 產生重寫票（高優先） |
| 🗑️ ZOMBIE | 存在但規格已作廢 | 產生刪除票 |

### 掃描策略

1. **先做全局搜尋**：在 `app/src/main/` 下搜尋關鍵 class/object 名稱
   - `AmbientConstants`、`PitchTractionConfig`、`GroupHaloResult`
   - `DeviceTierClassifier`、`FreeTierLimitTracker`、`PaywallTrigger`
   - `drawEdgeWarmth`、`calculateGroupHalo`、`smoothGroupHalo`
   - `isBreathing`、`isAllAligned`、`selectPitchConfig`
   - `PtfTabBar`（應無殘留）

2. **再做檔案級掃描**：對每個找到的檔案，比對規格包中對應 PART 的參數值
   - 色值是否正確（#D4A030、#50C864、#4A4A4A 等）
   - 數值是否正確（5f 非 3f、120 非 72、4px 非 2px 等）
   - 動畫參數是否正確（300ms、2000ms、FastOutSlowInEasing 等）

3. **最後做關聯掃描**：確認跨檔案引用完整
   - ViewModel → UseCase → Rule → Engine 的資料流
   - Hilt DI binding 是否覆蓋所有新增 class
   - Compose UI 是否正確 collect StateFlow

### 前一位審查官的 7 項可信結果

以下項目已由 Gemini 確認，你可以**快速二次確認**而非從零掃描：

| 項目 | Gemini 判定 | 你的任務 |
|------|-----------|---------|
| `CompositionResult` 取代 `CompositionScore` | ✅穩定 | 二次確認即可 |
| `PriorityEngine` 獨立 class | ✅穩定 | 二次確認即可 |
| `PtfTabBar.kt` 無殘留 | ✅已清理 | 全局搜尋 `PtfTabBar` 確認 |
| CameraX PreviewView binding | 🟢 | 需行為級驗證（不只是編譯） |
| ML Kit Face Detection 初始化 | 🟢 | 需行為級驗證 |
| Hilt DI | 🟢 | 需確認新增 class 的 binding |
| `isAllAligned` 邏輯 | ⚠️需改 | 確認具體偏離程度 |

---

## 任務二：架構壓力測試（5 維度）

在完成 75 項掃描後，基於掃描結果執行壓力測試。

使用合併規格包 **PART 18** 的 Checklist（C1-C5），逐項給出判定：

### C1: 硬體極限
- 前鏡頭 Fixed-Focus bypass
- VCM 馬達延遲與視覺追蹤頻率脫鉤
- ML Kit Face Detection ↔ Object Detection 互斥（500ms hysteresis）
- IMU 取樣與 Edge Warmth 渲染獨立管線

### C2: 狀態機防禦
- Sealed Class 場景隔離（前場景 state 不污染新場景）
- 場景切換 hysteresis 500ms
- Pill Toggle Photo↔PTF：純 Compose state，零 CameraX rebind
- 呼吸 × 閃爍互斥（isBreathing flag）
- Level Traction 漸入退場（0°→5° 線性）
- 橫拍 Level Traction MVP 隱藏

### C3: UDF 解耦
- StateFlow 從 ViewModel 到 Compose 單向
- 無 `runBlocking`、無直接 callback chain
- CameraX callback Executor 與 Main thread 分離
- ML Kit 結果傳遞方式

### C4: 三級裝置降級
- DeviceTierClassifier 分類邏輯
- Edge Warmth 60fps 全等級保證
- LEGACY 降級行為

### C5: Paywall 邊界
- FreeTierLimitTracker interface
- 付費功能 flag 控制
- EXIF 寫入路徑 Paywall 攔截點
- blur overlay z-order

---

## 任務三：粗拆票產出

### 排序原則（嚴格遵守）

```
優先級 P0: 基礎修復（壞了什麼都跑不起來）
  → CameraX / ML Kit / Hilt DI / assembleDebug
  → isAllAligned 修正
  → 🗑️ ZOMBIE 清理

優先級 P1: Domain 層骨架
  → AmbientConstants 全局常數
  → PitchTractionConfig
  → DeviceTierClassifier
  → 場景判斷邏輯 (SceneDetector)
  → CompositionRule 各規則 (Angle/Level/Framing)
  → FreeTierLimitTracker + PaywallTrigger interface

優先級 P2: Ambient UI 核心渲染
  → Edge Warmth (drawEdgeWarmth + Pitch Traction + Level Traction)
  → Halo (單人 + 群組 + 美食 + PTF)
  → Focus Ring 四狀態
  → 快門外環呼吸 × 閃爍互斥
  → Canvas 預算管理

優先級 P3: UI 組件
  → Pill Toggle Composable
  → Top Bar Scrim overlay
  → 底部控制列佈局
  → PTF 模式 UI 差異（隱藏 Gallery/Flip、✕ 按鈕、底色暖偏移）
  → 首次 PTF Bottom Sheet

優先級 P4: 商業層（獨立模組）
  → FreeTierLimitTracker 實作
  → PaywallTrigger 實作
  → Paywall blur overlay
  → 拍後教學卡片骨架

優先級 P5: 效能 + 降級
  → 分幀計算策略
  → 三級裝置降級實作
  → Canvas Bitmap 預渲染優化
```

### 票結構模板（每張票必須包含）

```markdown
### TICKET-[序號]: [簡短標題]

**優先級**: P0 / P1 / P2 / P3 / P4 / P5
**類型**: 🔧修復 / 🆕新建 / 🔄重寫 / 🧹清理
**矩陣驗證項**: [對應矩陣中的 #編號，如 1.1, 4.2, 5.6]
**規格包參考**: PART [N]

**範圍**:
- 目標檔案: `path/to/file.kt`（≤ 5 個）
- 禁區: `path/to/other.kt`

**具體工作**:
[2-3 句]

**驗收標準**:
- [ ] [可驗證的具體條件]

**前置依賴**: TICKET-[N] 或「無」

> ⚠️ DEFENSIVE WARNING: Do NOT refactor outside this scope.
> Do NOT rename variables in unrelated files.
> Do NOT add TODO() or placeholder implementations.
> Once completed and successfully built, STOP and wait for verification.
```

### 票粒度規則

- **不跨 Clean Architecture 層**（Data / Domain / Presentation 各自獨立）
- **影響檔案 ≤ 5 個**，超過則拆分
- **不合併不同 PART 的功能**（Edge Warmth 和 Halo 不可合為一票）
- **每張票附帶禁區清單**（防 Claude Code 越權）

---

## 產出格式

所有產出寫入：**`reports/full_audit_and_tickets.md`**

```markdown
# Cueshot 全審查報告 + 粗拆票

**審查者**: GPT-5.5 Full-Authority Auditor
**審查日期**: [YYYY-MM-DD]
**輸入**: 驗證矩陣 v1.0 + 合併規格包 v3.4.1-Stage0

---

## 1. 代碼現狀掃描結果（75 項）

### PART 1：全局常數
| # | 預期 | 判定 | 代碼證據 | 備註 |
|---|------|------|---------|------|
| 1.1 | AmbientConstants object | ✅/⚠️/❌/🔴/🗑️ | `path:line` | — |
...

### PART 2：螢幕佈局
...
（逐 PART 填寫）

---

## 2. 壓力測試結果（5 維度）

### C1: 硬體極限
| 檢查項 | 判定 | 代碼證據 | 備註 |
...

### C2-C5
...

---

## 3. 統計摘要

| 判定 | 數量 | 佔比 |
|------|------|------|
| ✅ MATCH | N | % |
| ⚠️ DRIFT | N | % |
| ❌ ABSENT | N | % |
| 🔴 CONFLICT | N | % |
| 🗑️ ZOMBIE | N | % |

---

## 4. 粗拆票清單（按優先級 P0→P5）

### TICKET-01: [標題]
...

---

## 5. 風險 Hotspot

| # | 風險描述 | 涉及檔案 | 嚴重度 | 對沖方案 |
|---|---------|---------|--------|---------|
```

---

## 絕對禁止事項

1. **不修改任何代碼檔案** — 你是審查官，不是開發者
2. **不自行發明變數名、class 名** — 所有命名以合併規格包為準
3. **不跳過矩陣任何一項** — 75 項全部判定，無例外
4. **不合併不同 PART 為同一張票** — Edge Warmth ≠ Halo ≠ Focus Ring
5. **不信任前一位審查官的 🟢 判定** — 全部二次驗證
6. **不產出無法驗證的票** — 每張票的驗收標準必須是「可執行指令或可觀察結果」

---

## 品質自檢（產出前必須執行）

- [ ] 75 項矩陣每一項都有判定（無空白行）
- [ ] 5 維壓力測試每一項都有判定
- [ ] 所有 ❌ ABSENT 項都有對應的新建票
- [ ] 所有 ⚠️ DRIFT 項都有對應的修復票
- [ ] 所有 🔴 CONFLICT 項都有對應的重寫票（標記高優先）
- [ ] 所有票的影響檔案 ≤ 5 個
- [ ] 所有票都有禁區清單
- [ ] 所有票都有 DEFENSIVE WARNING
- [ ] 票序按 P0→P5 排列
- [ ] 產出檔案路徑正確：`reports/full_audit_and_tickets.md`

---

**End of Execution Order v1.0**
