# 統一 Ticket 模板（CSO + Pipeline v1.6 合併版）

**版本**: v3.7
**產出日期**: 2026-05-29
**Revision**: v3.1（2026-07-06，INFRA-REACH-02 AI-1）— Acceptance Criteria 增列 AC 類型學（`[存在性]`/`[可達性]`/`[行為性]`/`[實機]`），對治 K4-50「AC 語義折損律」
**Revision**: v3.2（2026-07-31，INFRA-TPL-V32-01）— ①錨點預檢律常設化（`ANCHOR_PROBED` 欄 + AI-0 標準文塊）；②MAX_LINES 推導註記強制欄；③PRESET 增 `docs` 型並明定固定 AC 適用性；④範例 Ticket 同步 AC 類型學。依據 `Dispatch_Anchor_Precheck_Doctrine`（KD2）與 TKR-02 家族審查
**Revision**: v3.3（2026-08-01，INFRA-TPL-V33-01）— ①序號機械取號律（開票前置檢核）；②gitignore 死結預檢納入 Error Handling 標配。依據 `KAIROS_CaseC_Harvest_Round_Merged_Laws`（KD-2/KD-3）; 增加 "來源議題": 識別票據發起出於來源議題
**Revision**: v3.4（2026-08-05，Akira 直接置換）— 「前置依賴」單欄廢除，改為 execution_dependency / evidence_dependency / coordination_dependency 三分欄。事件支撐：TKR-06 Round 1 之 m-1 與 TKR-07 Round 1 之 M-1 為同一缺陷連續兩票復發（票面宣稱「前置依賴：無」，實則存在證據依賴），證明修正停在單票措辭層無效
**Revision**: v3.5（2026-08-13，MAX_LINES 量尺退役案）— ①量尺唯一化：量尺 ＝ `git diff --stat` insertions＋deletions，計量範圍限 production 檔案 diff；②MAX_LINES **完全退出 execution gate**（QA／SV／Cross-Review／approval／merge／archive），降為 production-only diff 實測值之估算校準遙測；③安全不變量類票標 `N/A`；④每次量測 MUST 形成與 ticket／worktree／commit SHA／DIFF_REF 綁定之 evidence record。連帶：v3.2 所立「MAX_LINES 推導註記強制欄」失去格式合格判準地位（降為估算參考），`N/A` 票免附。依據 `Ticket_MaxLines_Metric_Retirement`（R1–R3）＋先例 `MAXLINES_Measurement_Scope_Ruling`（2026-07-09，度量對象錯置／production-diff-only）
**Revision**: v3.6（2026-08-13，Akira 直接裁決）— 立「票號議題前綴律」：票號第一段 MUST 為來源議題之登記 token 或登記縮寫，並與票面 `**來源議題**` 欄同源；歸檔歸屬以票號第一段為唯一判準，新票冊面 `topic` 欄 MUST 與前綴同源。既有票號**不追溯改號**（已固化於 commit message／commit-msg hook 稽核面／冊面互指／票檔檔名）。併立「發現來源 ≠ 歸屬」二階判定：歸屬以**關聯性**（標的／需求源／產物落在該議題領域）與**相依性**（該議題之票對本問題存在依賴三分關係，或修復落點傳導至該議題交付面）二維任一成立為準——有則歸該議題；二維皆不成立則歸 `INFRA`（獨立歸屬線，不隸屬任何產品議題）。禁以發現地或話題沾附為歸屬依據。立法目的：消除冊面歸檔時「此票該歸哪條線」之爭議
**Revision**: v3.7（2026-08-14，INFRA-TPL-V37-01）— 立「票檔落點律」：票檔 MUST 落於實作專案之票務目錄並登記於 TKR 平台對應 project 冊面；設計/協調型專案 MUST NOT 設 Pending/ 或存放票檔。與 v3.6「發現來源 ≠ 歸屬」分工：彼律管議題線歸屬，本律管物理落點。事件支撐：ADV-WTREE-01（2026-08-14 作廢重開 WKTR-01 於 mcp-ticket-platform）
**來源**: CSO 裁決官 Ticket 格式 + TRA Ticket v2 合併，消除冗餘
**設計目標**: 每張 Ticket ~350-450 tokens，單一來源零重複
**單一載體**（v3.7 更正，INFRA-TPL-V37-01）: 僅 app-repo 份為唯一 SSOT（受版控）；Smarter-Project 衍生副本已於 v3.6 前刪除且不重建，「雙載體版控」措辭作廢。

---

## 模板

文件內不再使用 [file:N] 工具引用格式, 若有來源，主文件開頭有簡短的「檔案治理/來源說明」。

```markdown
# [TICKET-ID]: [一句話可驗證目標]

**簽發日期**: 2026-06-14
**簽發者**: Claude / GPT / Perplexity
**來源議題**: Cueshot | MEO | Advisor | TKR | RGR | INFRA | ... |  ← **MUST** 與票號第一段同源（見「票號議題前綴律」）
**Mode**: normal | uc  ||  **Effort**: Low | Medium | High
**Traycer Workflow**: N/A | architectural_review |　execute_review |　strict_verification　
**前置依賴（依賴三分）**：三欄 MUST 全部填寫，禁留白、禁併為單欄
- `execution_dependency`: 無 / [TICKET-ID] —— 本票開工前必須先完成之票
- `evidence_dependency`: 無 / [TICKET-ID] —— 本票之判準、錨點、HC 或 AC 所引用之既有裁決／產物來源票
- `coordination_dependency`: 無 / [TICKET-ID] —— 需與本票同批收束或同步差分之票

## Verify Config
TICKET_ID=[ID]
CREATED=[YYYY-MM-DD]
PRESET=[feature|bugfix|refactor|hotfix|docs]
ALLOWED=[會修改的檔案，逗號分隔]
FORBIDDEN=[禁止觸碰的檔案，逗號分隔]
MAX_LINES=[數字 | N/A]
DIFF_REF=[起始 commit hash]
ANCHOR_PROBED=[YYYY-MM-DD hh:mm]

**MAX_LINES 量尺定義與性質**（v3.5，`Ticket_MaxLines_Metric_Retirement` R1–R3 ＋ 2026-08-13 終局裁決）：

- **量尺唯一化（R1）**：量尺 ＝ `git diff --stat` 對 `DIFF_REF` 基線之 **insertions ＋ deletions 總和**；**MUST NOT** 以整檔行數計，**MUST NOT** 整檔與增量混用（量尺雙義為 MCP-PID-OWNERSHIP-GUARD-01 四 Rev 連環超額之根因層一：依 AC 自身量尺實測僅 ~168 行，未超額）。
- **計量範圍（R1，併載 `MAXLINES_Measurement_Scope_Ruling` 2026-07-09 先例）**：限 **production 檔案 diff only**；test／fake scaffolding 行數 **MUST** 排除於計量之外，由審查品質閘治理。「量尺＝diff」與「範圍＝production」兩者同時成立。
- **完全退出 execution gate（終局裁決，2026-08-13）**：MAX_LINES **MUST NOT** 作為 QA／SV／Cross-Review／approval／merge／archive 任一環節之 execution gate。**合法超額、`N/A`、空值、非法格式四類值一律 MUST NOT** 設定 failure flag、**MUST NOT** 觸發停止回報、**MUST NOT** 導致非零 exit code。理由：Git worktree 多工作目錄並列執行 ＋ QA／SV／Cross-Review Evidence-First 並行流程下，最終 production diff 受審查發現、補救修正、證據補齊、重新驗證與整合需求影響，開票期行數預估**不具備作為執行期停止判準之穩定性**（MAX_LINES 四次觸發零真陽性）。
- **保留用途**：僅為 production-only diff 實測值之**估算校準遙測**；其資料品質問題（缺值／非法格式）由 **ticket lint 層**處理，**MUST NOT** 上升為執行期阻斷。
- **交付阻斷權歸屬（唯五類）**：①scope／授權違規；②evidence 缺失或綁定不一致；③未處置之高嚴重度 finding；④必要品質驗證失敗；⑤未完成之審批或 required checkpoint。MAX_LINES 不在其列。
- **量測取證綁定（終局裁決，2026-08-13）**：每次量測 **MUST** 形成與 **ticket ID ＋ worktree ＋ commit SHA ＋ `DIFF_REF`** 綁定之 evidence record，以支援跨 session 並行、Cross-Review 重放與事後審計；**MUST NOT** 以共享工作樹、共享可變設定檔（如 `tmp/verify_cmd.conf`）或即時 `git diff` 作為**唯一**取證來源（與 `.claude/rules/concurrency.md` §1 律 B「receipt MUST 於收口當刻複製進耐久載體」同源）。
- **安全票 N/A（R3）**：AI 含 fail-closed／守門／授權判定類安全不變量者，MAX_LINES **MUST** 標 `N/A`，並附註「失敗形狀枚舉為審查發現式，不可先驗估算」；`N/A` 為顯式跳過語義，**MUST NOT** 被實作為算術比較（否則 fail-open 假 PASS）。
- **推導註記**（v3.2 立，v3.5 降級）：非 `N/A` 票 **SHOULD** 附〔逐 AI 行數分項〕→〔緩衝倍率〕→〔進位結果〕作估算參考；**已非格式合格判準**（v3.2「無此註記之票視同格式不合格」條款於此廢止），`N/A` 票免附。原「截斷序」（超額停止規則）隨 R2 失效，**MUST NOT** 再作停止依據。

## Action Items

> **AI-0 為強制首項**（派工前錨點預檢律，`Dispatch_Anchor_Precheck_Doctrine` KD2）。開票方 MUST 於草擬同刻以 live `read_file_safe`／`grep_files` 直查取樣全部「現值」錨點並填寫 `ANCHOR_PROBED` 時戳；**禁以記憶、前次 session 輸出或執行者回報充當錨點**。派工時若目標檔已有新 commit 越過 `ANCHOR_PROBED` 時刻，MUST 重跑預檢方可派工。

> **序號機械取號律**（律 A，`KAIROS_CaseC_Harvest_Round_Merged_Laws` KD-2/KD-3）。指派任何票號／候選序號前，開票方 MUST 以**全冊機械取號**決定新號：對 registry 全檔（per-project `TICKET_REGISTRY.json`）與 `deliver/`、`reports/` 執行 ID 樣式 grep 取 max-ID，`max+1` 為新號。**禁只掃 `Pending/`**（已通過票離開 Pending/，只掃必碰撞）；**禁以 `tail`／局部讀取驗證「目前最大號」**（局部讀取必然漏看他處已登記條目）。二次實證：PROTO-01 票號碰撞、K4-46 雙登。

> **票號議題前綴律**（v3.6，Akira 2026-08-13 直接裁決；與律 A 配對執行）。票號**第一段** MUST 為「來源議題」之登記 token 或其登記縮寫，且 MUST 與票面 `**來源議題**` 欄同源——二者不一致即開票 lint FAIL（自我校驗欄）。**歸檔歸屬以票號第一段為唯一判準**；新票之冊面 `topic` 欄 MUST 與前綴同源，**禁二者分歧**（立法目的：消除歸檔時「此票該歸哪條線」之爭議）。
>
> | 來源議題 | ID 前綴 | provenance 狀態 |
> |---------|---------|----------------|
> | Cueshot | `CUE` | 既有（`CUE-SNAP-01-FIX-A`） |
> | MEO | `MEO` | 既有 family（`ticket_pattern.md` 已登記） |
> | TKR | `TKR` | 既有 family |
> | INFRA（基礎設施／工作流設計線） | `INFRA` | 既有最大前綴（冊面實測 17 筆）；**獨立歸屬線，不隸屬任何產品議題** |
> | KAIROS（回顧收割線） | `KAIROS` | 既有 family |
> | Advisor | `ADV` | **新增**——首次使用當刻 MUST 補登 provenance |
> | RGR | `RGR` | **新增**——同上 |
>
> **發現來源 ≠ 歸屬**（Akira 2026-08-13 裁決）：歸屬 MUST 以**關聯性**與**相依性**二維為準，**MUST NOT** 以「哪個 session／議題發現它」為準。二階判定：
>
> 1. **先問關聯性／相依性**——二維**任一**成立即為實質歸屬，**有 → 歸該議題**（不因它由他議題 session 發現而改歸）：
>    - **關聯性**：問題之標的、需求源或產物落在該議題領域內（該議題之專屬管線／模組／規格）。
>    - **相依性**：該議題之票對本問題存在 `execution`／`evidence`／`coordination` 依賴（見「前置依賴（依賴三分）」），或本問題之修復落點在該議題標的內、改動會傳導至該議題之交付面。
> 2. **二維皆不成立 → 歸 `INFRA`**：問題實質為工作流本身之設計邏輯問題者，`MUST` 歸 `INFRA`。`INFRA` 為**獨立歸屬線**，不隸屬任何產品議題，亦為無關聯亦無相依時之歸屬線。
>
> **排除項**：「發現地」不成立歸屬；「話題上沾到」不成立歸屬——沾附而無關聯亦無相依者，MUST NOT 據以主張歸屬（否則歸屬爭議僅換形式復現）。正向取證先例：`INFRA-MAXLINES-GATE-01`／`-EVIDENCE-01` 於他議題 session 中發現，經二維檢驗均不成立（缺陷在既有工作流 MAX_LINES 閘之設計邏輯、修復落點在 `verify_checklist.sh`，非任何產品議題之專屬管線，亦無議題票對其存在依賴三分關係），故歸 `INFRA`。
>
> **縮寫規則**：MUST 為 2–6 個大寫英文字母；MUST NOT 與既有前綴碰撞（開票前以律 A 之全冊 grep 一併驗證**前綴唯一性**，非僅序號唯一性）。新議題之縮寫 MUST 於 spawn 當刻同步補登 `ticket_pattern.md` Family Provenance 表，**禁依賴事後稽核 escalate 才補登**（`CLAUDE.md` Rule 4a）。
>
> **不追溯（既有票號一律沿用）**：`INFRA-*`／`GAP-*`／`STATE-*`／`TRACTION-*`／`KAIROS*-*` 等既有票號 **MUST NOT** 追溯改號——票號已固化於 commit message、`.git/hooks/commit-msg` 稽核面、冊面互指（`depends_on`／`blocked_by`）與票檔檔名四處；改號即製造帳實分離。本律僅拘束生效日（2026-08-13）後新開之票。

> **票檔落點律**（v3.7，Akira 2026-08-14 裁決；與「發現來源 ≠ 歸屬」二階判定配對執行）。票檔 MUST 落於**實作專案**之票務目錄（如 SmartCameraApp1 `plans/cso_briefing/project/Pending/`、mcp-ticket-platform `docs/pending/`），並登記於 TKR 平台**對應 project** 之冊面。設計／協調型專案（Skill-Advisor、Smarter-Project 等）僅承載審查、裁決、交付物與交接載體，**MUST NOT** 設 Pending/ 目錄或存放票檔。二律分工：議題線歸屬依「發現來源 ≠ 歸屬」二維判定（關聯性／相依性）；物理落點依本律取實作專案——議題線與落點專案**可以不同**（如 WKTR 線落 mcp-ticket-platform）。事件支撐：ADV-WTREE-01 開於 Skill-Advisor/Pending/（非 git、非實作場，DIFF_REF 恆 N/A、AC 無法承載實作驗收），2026-08-14 作廢重開為 WKTR-01。

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

**MAX_LINES 退出 AC 類型學**（v3.5，R2 ＋ 2026-08-13 終局裁決）：MAX_LINES **MUST NOT** 標記為四型中任一型（尤其 `[行為性]`），**MUST NOT** 列入 Acceptance Criteria 區，亦 **MUST NOT** 作為任何 execution gate——其性質為估算校準遙測，判準地位已全面退場，詳見上文「MAX_LINES 量尺定義與性質」。

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

| Preset | AI-N 密度 | ALLOWED | MAX_LINES（估算，非判準） | 典型場景 |
|--------|----------|---------|-----------|---------|
| feature | 完整 AI-N | 嚴格列舉 | 80-150 | 新功能：TapFocusRing 實作 |
| bugfix | 完整 AI-N | 省略 | 100 | 修錯：GazeAF Ghost Face |
| refactor | 精簡 AI-N | 寬鬆 | 300 | 重構：PitchTraction 清理 |
| hotfix | 1-2 AI-N | 省略 | 50 | 緊急：crash 修復 |
| docs | 完整 AI-N（行級替換，現值/目標值逐字） | **嚴格列舉** | 30-80 | 契約增補、治理檔同步、歸檔建檔、模板修訂 |

**MAX_LINES 欄之性質**（v3.5）：上表數值為 preset 慣性**估算區間**，非退件門檻、非 execution gate；安全不變量類票不套用區間，一律標 `N/A`（R3）。

**固定 AC 之 preset 適用性**：`assembleDebug PASS` 對 `docs` 型 **N/A**（純文檔票無編譯產物，強制保留即製造不可能滿足之 AC——FIX-F traycer=FAIL 永久留帳之制度性來源）；`git diff --stat 僅涉及 ALLOWED 檔案` 對**所有** preset **保留強制**。`docs` 型票 MUST 於 AC 區以 `~~assembleDebug PASS~~ (N/A: docs preset)` 顯式劃記，禁靜默刪除（豁免停點不豁免可見性）。

---

## 範例 Ticket

```markdown
# P05-R1: GazeAF Ghost Face 琥珀圓環消除（bbox 過濾 + gazeScore 門檻 + 越界夾值）

**簽發日期**: 2026-06-14
**簽發者**: Claude
**來源議題**: Cueshot
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: execute_review
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無
- `coordination_dependency`: 無

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

**End of 統一 Ticket 模板**
