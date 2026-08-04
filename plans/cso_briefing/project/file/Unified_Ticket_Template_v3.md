# 統一 Ticket 模板 v3.2（CSO + Pipeline v1.6 合併版）

**版本**: v3.3
**產出日期**: 2026-05-29
**Revision**: v3.1（2026-07-06，INFRA-REACH-02 AI-1）— Acceptance Criteria 增列 AC 類型學（`[存在性]`/`[可達性]`/`[行為性]`/`[實機]`），對治 K4-50「AC 語義折損律」
**Revision**: v3.2（2026-07-31，INFRA-TPL-V32-01）— ①錨點預檢律常設化（`ANCHOR_PROBED` 欄 + AI-0 標準文塊）；②MAX_LINES 推導註記強制欄；③PRESET 增 `docs` 型並明定固定 AC 適用性；④範例 Ticket 同步 AC 類型學。依據 `Dispatch_Anchor_Precheck_Doctrine`（KD2）與 TKR-02 家族審查
**Revision**: v3.3（2026-08-01，INFRA-TPL-V33-01）— ①序號機械取號律（開票前置檢核）；②gitignore 死結預檢納入 Error Handling 標配。依據 `KAIROS_CaseC_Harvest_Round_Merged_Laws`（KD-2/KD-3）; 增加 "來源議題": 識別票據發起出於來源議題
**來源**: CSO 裁決官 Ticket 格式 + TRA Ticket v2 合併，消除冗餘
**設計目標**: 每張 Ticket ~350-450 tokens，單一來源零重複
**雙載體版控**: 雙份就近可讀,明定 app-repo 份為唯一 SSOT(受版控者); Smarter-Project 份降為衍生副本。

---

## 模板

文件內不再使用 [file:N] 工具引用格式, 若有來源，主文件開頭有簡短的「檔案治理/來源說明」。

```markdown
# [TICKET-ID]: [一句話可驗證目標]

**簽發日期**: 2026-06-14
**簽發者**: Claude / GPT / Perplexity
**來源議題**: Cueshot | MEO | Advisor | TKR | RGR | ... |
**Mode**: normal | uc  ||  **Effort**: Low | Medium | High
**Traycer Workflow**: N/A | architectural_review |　execute_review |　strict_verification　
**前置依賴**: 無 / [TICKET-ID]

## Verify Config
TICKET_ID=[ID]
CREATED=[YYYY-MM-DD]
PRESET=[feature|bugfix|refactor|hotfix|docs]
ALLOWED=[會修改的檔案，逗號分隔]
FORBIDDEN=[禁止觸碰的檔案，逗號分隔]
MAX_LINES=[數字]
DIFF_REF=[起始 commit hash]
ANCHOR_PROBED=[YYYY-MM-DD hh:mm]

**MAX_LINES 推導註記**（額度制截斷序律，**強制欄；無此註記之票視同格式不合格**）：[逐 AI 行數分項] → [緩衝倍率] → [進位結果]。**截斷序**：[實作超額時之停止規則]。

## Action Items

> **AI-0 為強制首項**（派工前錨點預檢律，`Dispatch_Anchor_Precheck_Doctrine` KD2）。開票方 MUST 於草擬同刻以 live `read_file_safe`／`grep_files` 直查取樣全部「現值」錨點並填寫 `ANCHOR_PROBED` 時戳；**禁以記憶、前次 session 輸出或執行者回報充當錨點**。派工時若目標檔已有新 commit 越過 `ANCHOR_PROBED` 時刻，MUST 重跑預檢方可派工。

> **序號機械取號律**（律 A，`KAIROS_CaseC_Harvest_Round_Merged_Laws` KD-2/KD-3）。指派任何票號／候選序號前，開票方 MUST 以**全冊機械取號**決定新號：對 registry 全檔（per-project `TICKET_REGISTRY.json`）與 `deliver/`、`reports/` 執行 ID 樣式 grep 取 max-ID，`max+1` 為新號。**禁只掃 `Pending/`**（已通過票離開 Pending/，只掃必碰撞）；**禁以 `tail`／局部讀取驗證「目前最大號」**（局部讀取必然漏看他處已登記條目）。二次實證：PROTO-01 票號碰撞、K4-46 雙登。

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `path/to/File.kt`
- **動作**: 逐字確認各 AI 之「現值」字串於檔內命中數與票面所載相符（標示「唯一」者恰 1 次）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [ ] 完成

### AI-1: [動作描述]
- **檔案**: `path/to/File.kt`
- **現值**: `[要改的代碼/值]`
- **目標值**: `[改成的代碼/值]`
- [ ] 完成

### AI-2: [動作描述]
...

## Hard Constraints
1. [約束]
2. [約束]

## Error Handling
- [條件] → 停止回報
- [條件] → 停止回報
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）

## Acceptance Criteria
- [ ] **[類型]** [可驗證條件]（類型見下方「AC 類型學」：存在性/可達性/行為性/實機）
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案
```

---

## 欄位說明

| 欄位 | 用途 | 消費者 | 冗餘防護 |
|------|------|-------|---------|
| **標題** | 一句話目標（= TRA Outcome） | 人類 + /sv | — |
| **Mode/Effort** | Claude Code 執行模式 | Claude Code | CSO 獨有 |
| **Verify Config** | 機械式 scope 驗證參數 | verify_checklist.sh | ALLOWED = 唯一檔案清單來源，正文不重複 |
| **AI-N** | 逐項精確修改指令 | Claude Code | 每項一個修補點，強制逐項打勾 |
| **Hard Constraints** | 禁止行為（補充 FORBIDDEN 以外的語義約束） | Claude Code + /sv | 僅放 FORBIDDEN 無法表達的語義約束 |
| **Error Handling** | 何時停止而非繼續嘗試 | Claude Code | 對治 /uc 首個成功就停 |
| **Acceptance Criteria** | 人類 + 機械雙重驗收 | Akira + verify_checklist.sh | 最後兩項固定（assembleDebug + diff stat） |

### AC 類型學（v3.1，INFRA-REACH-02 AI-1）

每條 Acceptance Criteria **MUST** 標記以下四型之一：

| 標記 | 定義 | 驗收方式 |
|------|------|---------|
| `[存在性]` | 檔案/字串/條款存在 | grep / 檔案存在檢查 |
| `[可達性]` | Guard/gate 依賴的 input 在 `app/src/main/` 有 production producer | **必附 producer 證據欄**（`檔案:行號`），執行後回填 |
| `[行為性]` | 經 production 路徑驅動之測試 | 禁 fixture 直注被測 state |
| `[實機]` | 真機驗證閘 | 實機 Logcat / 截圖證據 |

**強制規則**：AC 涉 Guard / gate / early-return / 外生 runtime state → **MUST** 至少各含一條 `[可達性]` + `[行為性]` AC。`[存在性]` AC 不得單獨承載「落地」語義（K4-50「AC 語義折損律」：AC 自然語言語義在機械驗收拆解時必然折損為存在性檢查，除非開票時以 AC 類型顯式編碼＋producer 證據欄）。

### 冗餘消除規則

```
❌ 不要：
  檔案範圍: EyeAfController.kt, LiveCameraSource.kt   ← 與 ALLOWED 重複
  Hard Constraints: 禁止修改 CameraViewModel.kt        ← 與 FORBIDDEN 重複

✅ 要：
  ALLOWED=EyeAfController.kt,LiveCameraSource.kt       ← 唯一來源
  FORBIDDEN=CameraViewModel.kt                          ← 唯一來源
  Hard Constraints: 禁止新增 sealed class 成員           ← FORBIDDEN 無法表達的語義約束
```

### Preset 快查

| Preset | AI-N 密度 | ALLOWED | MAX_LINES | 典型場景 |
|--------|----------|---------|-----------|---------|
| feature | 完整 AI-N | 嚴格列舉 | 80-150 | 新功能：TapFocusRing 實作 |
| bugfix | 完整 AI-N | 省略 | 100 | 修錯：GazeAF Ghost Face |
| refactor | 精簡 AI-N | 寬鬆 | 300 | 重構：PitchTraction 清理 |
| hotfix | 1-2 AI-N | 省略 | 50 | 緊急：crash 修復 |
| docs | 完整 AI-N（行級替換，現值/目標值逐字） | **嚴格列舉** | 30-80 | 契約增補、治理檔同步、歸檔建檔、模板修訂 |

**固定 AC 之 preset 適用性**：`assembleDebug PASS` 對 `docs` 型 **N/A**（純文檔票無編譯產物，強制保留即製造不可能滿足之 AC——FIX-F traycer=FAIL 永久留帳之制度性來源）；`git diff --stat 僅涉及 ALLOWED 檔案` 對**所有** preset **保留強制**。`docs` 型票 MUST 於 AC 區以 `~~assembleDebug PASS~~ (N/A: docs preset)` 顯式劃記，禁靜默刪除（豁免停點不豁免可見性）。

---

## 範例 Ticket

```markdown
# P05-R1: GazeAF Ghost Face 琥珀圓環消除（bbox 過濾 + gazeScore 門檻 + 越界夾值）

**簽發日期**: 2026-06-14
**簽發者**: Claude
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: execute_review
**前置依賴**: 無

## Verify Config
TICKET_ID=P05-R1
CREATED=2026-05-29
PRESET=bugfix
ALLOWED=EyeAfController.kt,LiveCameraSource.kt
FORBIDDEN=CameraViewModel.kt,CameraRepository.kt,TapFocusController.kt
MAX_LINES=100
DIFF_REF=fccd5cf
ANCHOR_PROBED=2026-05-29 10:00

## Action Items

### AI-1: EyeAfController bbox 邊界過濾（Guard 3 之後）
- **檔案**: `app/src/main/java/.../focus/EyeAfController.kt`
- **現值**: Guard 3 後直接使用 faces 清單
- **目標值**: 新增 validFaces 過濾（bbox.left >= 0f && bbox.top >= 0f && bbox.right <= 1f && bbox.bottom <= 1f），空則 return null
- [ ] 完成

### AI-2: EyeAfController gazeScore 最低門檻
- **檔案**: `app/src/main/java/.../focus/EyeAfController.kt`
- **現值**: `if (gazeScore == null || gazeScore.isAligned)`
- **目標值**: `if (gazeScore == null || gazeScore.isAligned || gazeScore.score <= 0.1f)`
- [ ] 完成

### AI-3: LiveCameraSource 越界座標夾值
- **檔案**: `app/src/main/java/.../camera/LiveCameraSource.kt`
- **現值**: focusOnPoint() 直接使用 point.x / point.y
- **目標值**: `val clampedX = point.x.coerceIn(0f, previewWidth.toFloat())` / `val clampedY = point.y.coerceIn(0f, previewHeight.toFloat())`
- [ ] 完成

## Hard Constraints
1. 禁止修改 TapFocusController 的任何狀態機邏輯
2. 禁止新增 EyeAfController 的公開 API

## Error Handling
- EyeAfController Guard 編號無法對應 → 停止回報
- normalizedBbox 欄位名稱與代碼不符 → 停止回報

## Acceptance Criteria
- [ ] **[實機]** Logcat 不再出現 `IllegalArgumentException: None of the specified AF/AE/AWB MeteringPoints`
- [ ] **[行為性]** gazeScore=0.0 的 trigger 被攔截（Logcat 確認）
- [ ] **[行為性]** bbox.left < 0 的 face 被過濾（Logcat 確認）
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案
```

---

### Mode 判斷規則

Mode 決定 Claude Code 以何種對話模式執行。

| 條件 | Mode | 理由 |
|------|------|------|
| ≥2 檔案修改，無架構設計決策 | `/uc` | 純執行，省 Token |
| Bug fix，已有明確修復步驟 | `/uc` | 不需討論，直接修 |
| 新建檔案或設計新 State 結構 | `/normal` → `/uc` | 設計決策需對話確認後再執行 |
| 跨層修改（Domain + Presentation + DI） | `/normal` → `/uc` | 順序錯誤白做，需先確認路徑 |
| 單檔案 <30 行小改 | **維持當前模式** | 切換開銷 > 節省，不強制切換 |

**「`/normal` → `/uc`」的含義**：Claude Code 先在 `/normal` 模式下與 Gemini Slice 進行設計確認（確認介面邊界、依賴順序、新 State 結構），確認後切 `/uc` 執行實作。

---

### Effort 判斷規則

Effort 決定 Claude Code 的思考深度（`--effort` 參數等效）。

| 子任務類型 | Effort | 理由 |
|-----------|--------|------|
| 新增 data class / interface | **Low** | 純結構定義，無邏輯 |
| 實作純函數（評分公式） | **Medium** | 有數學邏輯但邊界明確 |
| Compose UI 元件 | **Medium** | Canvas 繪圖邏輯需座標計算，不涉及架構決策 |
| 多模組整合（IMU + ML Kit + 規則引擎串接） | **High** | 涉及生命週期、執行緒切換、錯誤處理的交叉考量 |
| 編譯錯誤修復 | **Low** | 錯誤訊息已告知問題，直接修 |
| 邏輯 Bug（需追蹤數據流） | **High** | 需追蹤多規則互動、推演邊界條件 |
| 重構 / 拆分 God Object | **High** | 需理解全局依賴關係，決定新邊界 |
| 新 Rule（模式已建立） | **Medium** | 模式已有（RuleResult 介面），只需填入新邏輯 |

**混合型票的 Effort 取最高**：如果一張票同時包含「新增 data class（Low）」+ 「多模組整合（High）」，Effort = **High**。

**End of 統一 Ticket 模板 v3.2**
