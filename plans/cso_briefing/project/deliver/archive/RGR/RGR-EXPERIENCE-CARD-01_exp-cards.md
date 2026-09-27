# RGR-EXPERIENCE-CARD-01: 建立 Skill 層經驗卡格式與查卡規則，並以兩張泛化模式卡種子化

**簽發日期**: 2026-09-24
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: RGR-VERIFY-CHECKLIST-01（CLOSED，種子卡 1 之實例）、RGR-FIXTURE-REPORT-PATH-01（CLOSED，種子卡 2 之實例）
- `coordination_dependency`: 無

> **檔案治理/來源說明**：裁決來源＝Akira 2026-09-23／09-24：純 Skill-level 最小票，不建 MCP／DB／向量檢索／背景學習；種子卡至多 2 張且 MUST 為**可泛化的辨識與處置模式**，非事件紀錄（事件版之 `retire_when` 現已命中，開票即成 dead card）。`SSOT_Evidence_Collection_Spec.md` 依 Akira 2026-09-24 明示**豁免載入**（該規格拘束外審回流取證紀律，與本票 Skill 層卡片格式無關）。
> 開票期一手取證（Desktop 唯讀）：
> - skill 目錄現有四檔（`SKILL.md`、`REPAIR_ROUTING.md`、`OPS_CALIBRATION.md`、`EXAMPLE.md`）＋ `runner/`，**無**既有 knowledge／notes 目錄 → 需新建 `experience-cards/`，不重用。
> - `SKILL.md` L343「## Advisor Repair Mode And Calibration」為既有 pointer 慣例節（L345 指向 `REPAIR_ROUTING.md`／`OPS_CALIBRATION.md`），本票之查卡規則依同一慣例落位。
> - 不建 `EXPERIENCE_CARDS.md` 索引（Akira 裁定：目錄＋grep 即可，避免雙載體同步義務）。
> - 票面寫入權：SKAD Desktop 唯一；執行端僅勾選＋附 receipt。

## Verify Config
TICKET_ID=RGR-EXPERIENCE-CARD-01
CREATED=2026-09-24
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/experience-cards/
FORBIDDEN=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/runner/,.claude/skills/qa/,.claude/PROGRESS.md
MAX_LINES=N/A
DIFF_REF=6bcb419
ANCHOR_PROBED=2026-09-24（Desktop 開票期）
BASE_COMMIT=6bcb419c81d11f8f179ade4f889addd33f64a42a
TARGET_BLOB=N/A（Desktop 無合法 blob-hash producer；AI-0 逐檔讀現值）
INTRODUCED_CAPABILITY=skill:experience_cards
DEPENDENCY_DELTA=NONE

MAX_LINES=N/A 註：docs preset，純新增卡片與規則段，行數非阻斷判準（TPL v3.5 終局裁決）。

**`INTRODUCED_CAPABILITY` 誠實揭露**：本票引入 skill 層新機制 `skill:experience_cards`，固定 corpus 無字面命中 → 機械觸發外審條件 6a。依 Akira 2026-09-24 之獨立開發者裁量與「可行性與證據分離律」，本能力為**本地、純文件、可逆、零新網路面、零依賴、零成本**，未命中硬風險判準任一款，故取證結果為「不觸硬風險，免外審凍結」；票面如實揭露，不代為宣稱免審。

## Action Items

### AI-0: 錨點復驗
- 確認 `SKILL.md` L343 節標題「## Advisor Repair Mode And Calibration」與 L345 pointer 句現值存在。
- 確認 `.claude/skills/ticket-rigor-gate/experience-cards/` 尚不存在（若已存在 → 停止回報）。
- [x] 完成

### AI-1: 建立兩張泛化種子卡
- **新增檔**：`experience-cards/exp-path-token-directory-prefix-01.md`、`experience-cards/exp-runtime-state-vs-fixture-output-01.md`
- 每卡 200–300 字，YAML frontmatter 欄位固定：`id`／`status`／`topic`／`trigger`／`decision`／`why`／`reuse_when`／`avoid_when`／`confidence`／`source_refs`／`last_confirmed`／`retire_when`。**`reuse_when`、`avoid_when`、`retire_when` 三欄 MUST 非空**。
- **卡 1**：trigger＝scope verifier 對 trailing-slash 目錄 token 與巢狀檔得出矛盾結果；decision＝查 token 比對是否採 repo 相對路徑前綴判定，補正例與邊界負例，勿只看 basename 或泛用 glob；retire_when＝所有受支援 token 形式已有相同 prefix／boundary contract 且對應測試長期覆蓋。
- **卡 2**：trigger＝回歸會修改 Git-tracked report／state 檔，或測試後需人工還原；decision＝先辨識該檔是否為 runtime latch；是則保留能力，優先改 fixture 落點至持久 gitignored root，再刪 tracked output；retire_when＝測試與 runtime state 已分離且同類 fixture 均不再寫入版控輸出。
- `source_refs` 各指向對應 CLOSED 票與 commit，**禁搬入 raw report 全文**。
- [x] 完成

### AI-2: SKILL.md 加查卡規則
- **檔案**：`.claude/skills/ticket-rigor-gate/SKILL.md`
- **落位**：依 L343 節之既有 pointer 慣例，新增一段（≤12 行）載明五條：①遇停滯／scope 爭議／驗證誤判／修法不唯一／重複失敗／已知被否決方向時，先以問題、檔案、錯誤類型 grep `experience-cards/`；②僅 `confidence: high` 且 `trigger` 與 `reuse_when` **同時**命中，方得以卡之 `decision` 為優先處置參考；③`avoid_when` 命中、證據過期或情境不完全相同 → 不得套用，改走正常 local evidence／exception 路徑；④任務結束時，僅在產生可重用裁決／誤判模式／有效修法／明確不可重試路徑時，才產生 candidate card；⑤candidate card 未經確認前不視為 skill rule。
- **MUST 明載**：raw evidence 僅作 `source_refs`，**禁整批載入 skill context**。
- [x] 完成

### AI-3: 驗收與提交
- `git diff --name-only HEAD` ⊆ ALLOWED → commit `[RGR-EXPERIENCE-CARD-01] docs(rigor-gate): seed experience cards and lookup rule`。
- 本票單獨 commit。
- [x] 完成 — commit `1bedb76`

## Hard Constraints
1. 禁建 MCP server／API／資料庫／vector DB／graph／queue／webhook／背景 job；禁新增 runner、schema、fixture。
2. 禁建 `EXPERIENCE_CARDS.md` 或任何第二套索引載體。
3. 種子卡**上限 2 張**；禁批次掃描歷史資料生成卡片。
4. 禁自動修改 skill rule、ticket、SSOT 或 repo；卡片不具 rule 效力。
5. 禁把卡寫成事件紀錄（「某次 bug 已修」式），MUST 為可泛化之辨識與處置模式。

## Error Handling
- `experience-cards/` 已存在 → 停止回報。
- 任一卡之 `reuse_when`／`avoid_when`／`retire_when` 無法寫出非空且可判定之內容 → 該卡不建立，停止回報（寧缺勿濫）。
- `git diff --name-only` 超出 ALLOWED → 停止回報。
- **[標配]** `git check-ignore -v <ALLOWED 標的>` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** `experience-cards/` 下恰 2 個 `.md`，各含全部 12 個 frontmatter 欄位，且 `reuse_when`／`avoid_when`／`retire_when` 三欄皆非空。
- [x] **[存在性]** `SKILL.md` 含查卡規則段，明載五條與「raw evidence 僅作 source_refs、禁整批載入」。
- [x] **[存在性]** repo 內無 `EXPERIENCE_CARDS.md`；`git diff --stat` 顯示無 runner／schema／fixture 異動。
- [x] **[行為性]** given 卡 1 之 `retire_when`，expect 其判定不因 RGR-VERIFY-CHECKLIST-01 已 CLOSED 而立即成立（即卡片非 dead card；以 token 形式覆蓋面為準，非以單張票收尾為準）。— PASS，`retire_when` 字面條件為 token 形式覆蓋面而非單票收尾
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

## Receipt: SV/Cross-Review
```
Codex Cross-Review: PASS（REACHABILITY: NONE），Scope=269f235..1bedb76
Structural Verify: PASS (A/B/C/D/E/F/G) [ADVISORY — PROGRESS.md 78/80 行，非阻斷]
```
