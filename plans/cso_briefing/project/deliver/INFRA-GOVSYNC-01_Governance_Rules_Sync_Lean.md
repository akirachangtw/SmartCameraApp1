# INFRA-GOVSYNC-01: 治理規則同步（APP-3 縮減版）— Rigor Gate 選用制入規＋凍結點語義補句＋mcp-service-safety 去死引用移植＋G2 定調

**簽發日期**: 2026-08-27
**簽發者**: Claude (SKAD)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: `RGR-APPDEPLOY-01`（CLOSED；rigor-gate skill 已在 APP 落地，本票 AI-1 之段落不為空殼——盤點檔 L55 綁定理由之前提已滿足）
- `evidence_dependency`: `PLT_TO_APP_MIGRATION_INVENTORY_AND_DIRECTION_20260826.md` §3 #1/#2＋L88（APP-3 原範圍）；`INFRA-TRACKSCOPE-01` 收尾實錄（範圍縮減依據）；PLT `ssot-governance.md` L65-91（五段生命週期源文本）
- `coordination_dependency`: 無（原裁決 B 之 APP-2↔APP-3 綁定，由 execution_dependency 指向已 CLOSED 之 APP-2 消解；本票即 `WKTR-APPDEPLOY-01`／`RGR-APPDEPLOY-01` 兩票遺留掛鉤之清償載體）

## 檔案治理／來源說明

**範圍縮減沿革（三段，全數有據）**：盤點檔 L88 原定 APP-3＝「mcp-service-safety 新增＋ssot-governance 五段移入＋registry 退役」。①registry 退役＋`.gitignore` 反轉——已由 `INFRA-TRACKSCOPE-01`（CLOSED，2026-08-27）完成，本票零 `.gitignore` 觸碰。②五段生命週期——**本票採選用制裁剪，不引入五段狀態機**（見下）。③mcp-service-safety——移植並改寫 §5 去死引用。

**⭐ 裁剪聲明（Akira 2026-08-27 治理減量方向之適用，開票端預設，Akira 得單回合改令）**：PLT 五段制（`PENDING → TICKET_RIGOR → IN_PROGRESS → EXECUTED → REVIEW → CLOSED`）要求 APP 冊 lifecycle schema 增三態並使 gate 成為必經關卡。依 Akira 2026-08-27 對話裁決方向（治理精力上限二成、工具不加關卡、中間產物不永久保存），本票將 `TICKET_RIGOR` 引為**選用前置**：APP 冊維持現行 `PENDING/REVIEW/CLOSED/VOID` 狀態集不變；新票**得**於派工前跑 `ticket-rigor-gate` skill 取品質報告，Akira 明示指派時 **MUST** 跑；報告落 `.tmp/rigor-gate/`（`RGR-APPDEPLOY-01` L2 裁決：執行期產物，禁作長期證據引用）。與 PLT 之差異登記為刻意分叉（fork 型態：APP＝域之輕量制／PLT＝法之全閘制），日後 ENVSYNC 合併裁決 MUST 以本票後狀態為 APP 基線。**若 Akira 要求全五段制，回覆一聲即出 Rev A 改寫 AI-1，禁執行端自行升級。**

**開票期 live 取證（2026-08-27 16:39）**：
- APP `ssot-governance.md`：8,530 B（`aud_20260827_28f692b9c6`）；全檔 `TICKET_RIGOR`／`IN_PROGRESS`／`生命週期` **零命中**、L57 搬移紀律行無凍結點語義（`aud_20260827_476d58fbee`）——AI-1/AI-2 缺口為真。
- APP `mcp-service-safety.md`：**不存在**（`not_found`，`aud_20260827_087138ec09`）；PLT 端五節結構 §1(L8)/§2(L14)/§3(L19)/§4(L25)/§5(L33)。盤點檔 L69：§1–3 通用可搬，§5 引 `.claude/hooks/mcp_service_pid_guard.py` 而 APP hooks 無此檔（`aud_20260826_136632e34c`）→ 純複製即死引用，MUST 改寫。
- **PLT L100 stale 路徑不在本票射程**：該缺陷（PLT 檔內誤植 APP 路徑 `plans/cso_briefing/...`，正確應為 `docs/pending→deliver`）屬 PLT 端檔案，依票檔落點律與 D-1 判準排除；PLT 端 RAPID_MODE 適用（§0：git restore 五分鐘可復原），建議 Akira 於 PLT session 一行順修，免票。
- 律 A 取號：APP 冊 INFRA 群組 24 筆，`INFRA-GOVSYNC-*` 零碰撞（`aud_20260827_4eba2df3e9`）。

## Verify Config
TICKET_ID=INFRA-GOVSYNC-01
CREATED=2026-08-27
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md,.claude/rules/mcp-service-safety.md
FORBIDDEN=.claude/rules/ticket_pattern.md,.claude/skills/,.gitignore,plans/cso_briefing/,app/
MAX_LINES=60
DIFF_REF=9f1fa52b
ANCHOR_PROBED=2026-08-27 16:39
BASE_COMMIT=9f1fa52b
TARGET_BLOB=N/A

**TARGET_BLOB=N/A 理由**：開票端無 `git hash-object` 通道（同 WKTR/RGR-APPDEPLOY-01 先例）；既存標的以位元組數 8,530 錨定，新建標的 pre-image 空集。V1 降級 AI-0 錨點制。
**MAX_LINES=60**：遙測非判準。AI-1≈18＋AI-2≈2＋AI-3≈30（§1–4 沿源＋§5 改寫）＋AI-4≈3 ≈ 53，進位 60。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- `ssot-governance.md`：`wc -c`＝8,530；`TICKET_RIGOR` 全檔命中 0；L57 字面 `- **搬移紀律**：勾選完成後，票檔 **MUST** 由 \`plans/cso_briefing/project/Pending/\` 搬移至 \`plans/cso_briefing/project/deliver/\`。` 恰 1 次。
- `mcp-service-safety.md`：於 APP `.claude/rules/` **不存在**。
- 源端 PLT `mcp-service-safety.md` §1–§5 節標題行號 8/14/19/25/33 復驗。
- 漂移依 TPL v3.8 三分流分類停止（同前二票字面，略）。
- [x] 完成

### AI-1: `ssot-governance.md` 檔尾新增「Ticket Rigor Gate（選用制）」節
- **檔案**: `.claude/rules/ssot-governance.md`（append 於檔尾）
- **目標值**（逐字）:
```
## Ticket Rigor Gate（選用制，INFRA-GOVSYNC-01）

- APP 冊 lifecycle 維持 `PENDING / REVIEW / CLOSED / VOID`，**不引入** PLT 五段制之 `TICKET_RIGOR / IN_PROGRESS / EXECUTED` 狀態——此為對 PLT 全閘制之刻意裁剪（Akira 2026-08-27 治理減量裁決），非同步遺漏；日後兩端規則合併裁決 MUST 以本節為 APP 基線。
- 新票**得**於派工前執行 `.claude/skills/ticket-rigor-gate/` 取品質報告；Akira 明示指派 rigor 檢核之票 **MUST** 執行並於派工訊息附報告要點。
- 報告與遙測一律落 `.tmp/rigor-gate/`（`RGR-APPDEPLOY-01` L2 裁決：執行期產物，KEEP_N=10 輪替）；**MUST NOT** 於任何票面 AC 引用 `.tmp/` 下 rigor 產物為長期證據——耐久結論唯一載體為票面／冊面收尾實錄。
- 本節不新增任何 MUST 級派工前置；未跑 gate 之票不因此退件。
```
- [x] 完成

### AI-2: 搬移紀律行補凍結點語義（純增一句，L57 原句一字不動）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: AI-0 所錨 L57 原句
- **目標值**: 原句之後**同行接續**增：`**凍結點語義**（INFRA-TRACKSCOPE-01，2026-08-27）：\`Pending/\` 期票檔為 untracked，搬移至 \`deliver/\` 之首次 commit 即該票檔進入版控之凍結版本，此前修訂史不入 git log。`
- [x] 完成

### AI-3: 新增 `mcp-service-safety.md`（§1–§4 位元組級沿 PLT，§5 改寫去死引用）
- **檔案**: `.claude/rules/mcp-service-safety.md`（新建）
- **目標值**: §1–§4（PLT L1–L32 對應內容）逐字沿源；§5「翻盤條件」內凡引用 `.claude/hooks/mcp_service_pid_guard.py` 之句改寫為：`（PLT 端由 mcp_service_pid_guard.py hook 機械強制；APP 端無此 hook，本檔全文屬行為契約層，無機械閘）`，其餘語句沿源。**MUST NOT** 複製或新建任何 hook 檔。
- 改寫後全檔 grep `pid_guard`：僅上述聲明句恰 1 次命中。
- [x] 完成

### AI-4: G2 定調（`RGR-APPDEPLOY-01` 收尾實錄遺留）：dashboard 慣例同步入規
- **檔案**: `.claude/rules/ssot-governance.md`（Ticket Closure 紀律節內純增一 bullet）
- **目標值**（逐字）: `- **Dashboard 慣例同步**：\`.claude/PROGRESS.md\`／\`PROGRESS_ARCHIVE.md\` 之例行 Dashboard 列更新得與實作同 commit 或以 \`[COMPACT-SYNC]\` 獨立提交，**不計入**票面「git diff --stat 僅涉 ALLOWED」AC 之違規判定（INFRA-GOVSYNC-01 定調，消解 RGR-APPDEPLOY-01 G2 帳實矛盾）。`
- [x] 完成

## Hard Constraints
1. `ssot-governance.md` 既有內容零修改（AI-2 為原句後接續增寫、AI-1/AI-4 為純增段落／bullet）；AC 以 `git diff` 零 `-` 行驗證。
2. **禁引入五段狀態機、禁改 APP 冊 lifecycle 枚舉、禁增任何 MUST 級派工前置**（裁剪聲明之字面拘束；升級須 Akira 明示 Rev A）。
3. 禁觸 PLT 端任何檔案（L100 修正明文排除，屬 PLT RAPID 域）。
4. 禁複製／新建任何 hook；RAPID 於 APP 不適用；commit 前綴 `[INFRA-GOVSYNC-01]`。

## Error Handling
- AI-0 任一不符 → 三分流分類停止。
- **[標配]** `git check-ignore -v` 對 ALLOWED 二檔任一命中 → 停止回報（KAIROS #20；開票端預檢：`.claude/rules/` 現行 tracked，`ticket_pattern.md` 同目錄有本日 commit 為證，推定零命中；執行端仍 MUST 實跑）。
- AI-3 源端 §5 內 hook 引用句數 ≠ 開票推定 → 停止列清單升裁（禁自行擴大改寫面）。

## Acceptance Criteria
- [x] **[存在性]** `ssot-governance.md`：`Ticket Rigor Gate（選用制` 恰 1、`凍結點語義` 恰 1、`Dashboard 慣例同步` 恰 1；`git diff 9f1fa52b -- .claude/rules/ssot-governance.md` 之 `-` 行數 0
- [x] **[存在性]** `mcp-service-safety.md` 於 APP 存在；`pid_guard` **§5 內**恰 1（僅聲明句；AC 字面修正：全檔實測 3 處，L6/L21 為 §1-4 逐字沿源之原文合法引用，Akira 2026-08-27 對話裁決不擴大改寫面，AC 判準改為「§5 內恰 1」，詳見 sv_report.md）；§1–§4 與 PLT 端對應區段逐字一致（diff 抽驗）
- [x] **[行為性]** given 全檔 grep `TICKET_RIGOR`，expect 命中僅落於 AI-1 新增節之字面（「不引入」語境），無任何 lifecycle 枚舉／狀態轉移語句引入該 token
- [x] **[可達性]** N/A 顯式劃記（治理文本，無 production producer）
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉 ALLOWED 二檔（Dashboard 慣例同步依 AI-4 新規不計）

## 完成後動作
1. 票檔勾選後搬 `deliver/`（凍結點）；冊面三步序轉態（registry 已退出版控，無需任何手簽 commit）。
2. 本票收束即清償 `WKTR-APPDEPLOY-01`／`RGR-APPDEPLOY-01` 之 coordination 遺留掛鉤，收尾實錄 MUST 明文記載清償。

---
**End of INFRA-GOVSYNC-01**
