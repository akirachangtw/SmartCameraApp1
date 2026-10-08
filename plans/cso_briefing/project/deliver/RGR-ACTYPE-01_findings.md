# RGR-ACTYPE-01: 使 ticket-rigor-gate 對「文件契約位置誤標 [可達性]」與「無 observer 之程序斷言」兩類 AC 產生首輪 finding

**檔案治理/來源說明**：本票由 `/ticket-author ticket` 自 Plan-backed parent 產出；本節自含，`.tmp` 暫存檔消失後仍可獨立閱讀。
- Parent type：Plan-backed；plan-local item：`T01`（RGR-1＋RGR-2 單一耦合項）。
- Parent 絕對路徑：`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261008-rgr-ac-evidence-plan.md`；artifact status：`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`／`Fixture: false`；Parent read：2026-10-08 18:50。
- 上游規格（未入版控，`.gitignore` 排除）：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\reports\RGR-AC-EVIDENCE-TYPE_Spec_20261008.md`，本票讀取其 §1–§11 現文。規格 §10（E1–E4，Akira 2026-10-08）與本次呼叫之 Approved constraints，覆蓋 plan artifact 的下列條目：①回放 AC 由 desk replay 改為 `[行為性]` given/expect，以 RGR 互動式實跑為 observer（E1）；②刪除 `[範圍]` AC（E2）；③`REPAIR_ROUTING.md` A-4 不開對齊票、不列 follow-up（E3）；④流程載體條文行號改由本票重探（E4）；⑤回放新增第 3 項期望（處置須要求明載順序不屬驗收或指名 observer）。
- 核准決策（規格 §2 現文，皆 APPROVED，Akira 2026-10-08）：D1 範圍僅 RGR-1、RGR-2，只改 `ticket-rigor-gate/SKILL.md`；D2 RGR-1 以「驗收對象」而非 preset 判定；D3 RGR-2 不一律拆 AC，先判定程序是否屬驗收再分流；D4 回放結論限於「RGR 產生正確 finding」。**A1**：RGR-1 補一句，列出 producer 與 consumer 的定義位置不構成 trace；被引用檔本身是可執行指令產物（如 skill）時亦同。**A2**：RGR-2 表格第三欄改為偵測問題；處置移到表後獨立段落。**A3**：同檔一致性——typology 表 reachability 列措辭對齊 RGR-1；Vacuous Verification 引言補一句第四變體。
- 依賴三分之更正：plan artifact 將 `evidence_dependency` 記為「無」；本票 AC 之 replay fixture 逐字引用 `TKA-ALIGN-01` 的 AC 凍結證據，依 TPL 依賴三分定義（AC 所引用之既有產物來源票）改填 `TKA-ALIGN-01`。
- AC 類型說明：本票不含 `[可達性]` AC。TPL 之 `[可達性]` 定義為 `app/src/main/` 之 production producer，本票標的為 skill 規則文字，無此類 producer；「規則是否實際生效」由 `[行為性]` replay 以全新 subagent 執行之 RGR 互動式 Preflight 實跑承載（符合 TPL 對 `[存在性]` 不得單獨承載「落地」語義之要求）。TPL 強制規則對非 production gate 之適用出口屬 Template／RGR 議題線，不在本票處理。
- **Rev A**（2026-10-08，Akira 裁決，依 SKAD 審查冊面 note rev 602、603；lifecycle 維持 PENDING；本票早已登冊，Rev A 後僅 read-back，不重新登冊、不 upsert 覆寫）：R1 AI-7 改為派發全新 subagent 執行 replay，避免執行者 context 已含預期 finding 而使 replay 淪為 Self-Satisfying；R2 Research 條改為不呼叫並記 `RESEARCH_SKIPPED` 續跑，僅 runner／Codex 嘗試才停止；R3 fixture 去識別化（中性路徑與 TICKET_ID、來源說明不提本票或 TKA，sha1 比對留主 session）。證據改存本票收尾實錄，不再建立 `.tmp/RGR-ACTYPE-01/`。

**簽發日期**: 2026-10-08
**簽發者**: Claude
**來源議題**: RGR
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: TKA-ALIGN-01 —— 其 `TKA-ALIGN-01_retro.md` L125／L126／L129 為 replay 母體（已 CLOSED）
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=RGR-ACTYPE-01
CREATED=2026-10-08
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/rules/cross-review.md,.claude/skills/sv/**,.claude/skills/qa/scripts/structural_verify.sh,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/deliver/TKA-ALIGN-01_retro.md,plans/cso_briefing/project/TICKET_REGISTRY.json,app/**
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-08 18:50
BASE_COMMIT=91e1a2f7
TARGET_BLOB=.claude/skills/ticket-rigor-gate/SKILL.md:353bdb48
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

> MAX_LINES=N/A：AI 含審查守門判定（gate finding 規則）類不變量，失敗形狀枚舉為審查發現式，不可先驗估算（TPL R3）。
> `TARGET_BLOB` 已由 `git hash-object` 實測取得。`FORBIDDEN` 中的 `TICKET_REGISTRY.json` 僅禁直接編輯；經平台 `registry_*` 原語之本票生命週期操作不在此限。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **動作**: 逐字確認下列字串各恰命中 1 次（開票時 2026-10-08 18:50 live grep 取樣；括號行號僅定位用）：
  - AI-1 現值整行（L219）、AI-5 現值整行（L234）；
  - `| on-device | 實機 | proves runtime behavior on the real target environment or device |`（L221）、`### On-Device Requirement Rule`（L223）；
  - `| Degradable Guard | `（L240）、`For **document-layer evidence**` 段首（L244）、`## Mandatory Runtime Load`（L246）；
  - `### Mechanical Verdict Rule`（L186）。
- 記錄 `git hash-object` 與 `git status --short` 為診斷資訊（非 gate）。AI-1／AI-5 現值命中數 ≠ 1 → 停止回報，禁推定等價位置；其餘錨點漂移依 TPL v3.11 三分流記錄分類後以工作樹現值續作。
- [x] 完成（2026-10-08 開工；八項錨點各恰命中 1 次；SKILL.md blob 353bdb48、工作樹乾淨；activate_ticket.sh 回填 DIFF_REF=91e1a2f7）

### AI-1: RGR-1 — reachability 列措辭對齊（A3）
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **現值**: `| reachability | 可達性 | proves the production path can reach the guarded behavior |`
- **目標值**: `| reachability | 可達性 | proves a production runtime or workflow-control path can reach the guarded behavior (see the rule below) |`
- [x] 完成（reachability 列更新；整行 grep 命中 1）

### AI-2: RGR-1 — Typology 表後新增三段（A1）
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **現值**: Typology 表末列 `| on-device | 實機 | ... |` 之後為一空行，接 `### On-Device Requirement Rule`。
- **目標值**: 於表末列之後、`### On-Device Requirement Rule` 之前（各段間以空行分隔）逐字插入：

```text
A `[可達性]` AC MUST exercise or trace a production runtime or workflow-control path from a stated source or precondition through guarded behavior to a named observer. A trace MUST show the path being taken — an execution result, emitted state, receipt, or a non-default production write point; listing where a producer and a consumer are defined does not by itself constitute a trace.

An AC that only asserts the presence or location of a document, contract, section, symbol, producer, consumer, or `file:line` reference is `[存在性]`, even if that material describes a runtime guard, and even if the cited file is itself an executable instruction artifact such as a skill. A docs-only ticket MAY use `[可達性]` only when it actually verifies a reachable production or workflow-control path.

A mislabeled AC MUST be recorded as a finding. Severity and disposition remain governed by the existing severity table and Mechanical Verdict Rule.
```
- [x] 完成（三段插入於 Typology 表後、On-Device Requirement Rule 之前；首句各 grep 命中 1；與規格 sha1 相同）

### AI-3: RGR-2 — Vacuous Verification 表新增第四列（A2）
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **現值**: 表末列為 `| Degradable Guard | ... |`（三欄表，表頭 `Variant | Symptom | Detection Question`）。
- **目標值**: 於 `Degradable Guard` 列之後緊接逐字新增一列（三欄）：

```text
| Unobserved Procedure Claim | An AC asserts an internal step, ordering, file-read, or skipped action, but names only the final outcome and no observer for the claimed procedure | Is the procedure itself acceptance scope, and if so, **which observer records that it happened** (or happened in this order)? |
```
- [x] 完成（Unobserved Procedure Claim 列緊接 Degradable Guard，三欄；與規格 sha1 相同）

### AI-4: RGR-2 — 表後新增處置段（A2）
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **現值**: `For **document-layer evidence**…` 段之後為一空行，接 `## Mandatory Runtime Load`。
- **目標值**: 於該段之後、`## Mandatory Runtime Load` 之前（各段間以空行分隔）逐字插入：

```text
**Mandatory disposition (Unobserved Procedure Claim)**: decide whether the procedure itself is required. If not, rewrite the AC around the observable outcome and state that internal ordering is not acceptance scope. If required, provide a separate observable clause or AC naming its observer and evidence. Without an observer, the procedure claim MUST NOT count as PASS and may only be disclosed.

A tool-call transcript is one possible observer, not a mandatory evidence format. Existing logs, receipts, emitted state, or other durable evidence MAY be used when they directly observe the claimed procedure.
```
- [x] 完成（處置段位於 document-layer evidence 段之後、Mandatory Runtime Load 之前；與規格 sha1 相同）

### AI-5: RGR-2 — 引言補第四變體句（A3）
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **現值**: `Three previously separate variants collapse into one family so the reviewer can apply a pattern library instead of three scattered rules.`
- **目標值**: 同一行末尾以一個空格接續：`` `Unobserved Procedure Claim` was added later and belongs to the same family. ``（整行為原句＋此句）
- [x] 完成（引言行已含第四變體句；grep 命中 1）

### AI-6: 建立 replay fixture（僅寫 `.tmp/`）
- **動作**: 建立去識別化 fixture `.tmp/rgr-replay/FX-01.md`（中性路徑與檔名；fixture 內不得出現本票 ID、`TKA`、`ALIGN`、`ACTYPE` 或 replay 目的之任何字樣）。三行 AC 一律以機械抽取取得，禁手改：`git show dde3ba61:plans/cso_briefing/project/deliver/TKA-ALIGN-01_retro.md | sed -n '125p;126p;129p'`；抽取後逐行 sha1 前 8 碼 MUST 依序為 `c90254f2`／`f5ece4c0`／`951a1f48`（開票時實測，凍結 commit 與工作樹相同），不符 → 停止回報。sha1 比對與抽取來源 commit 僅留在主 session，不寫入 fixture。抽取結果另 MUST 不含 `TKA`／`RGR`／`ALIGN`／`ACTYPE` 字面（開票時實測三行皆無），否則停止回報。
- fixture 骨架（其餘內容固定如下，三行 AC 置於 `## Acceptance Criteria` 之下）：

```text
# FX-01: 審查輸入樣本

檔案治理/來源說明：本檔為獨立審查輸入樣本，非正式票。

**簽發日期**: 2026-10-06
**簽發者**: Claude
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=FX-01
CREATED=2026-10-06
PRESET=docs
ALLOWED=.claude/skills/ticket-author/SKILL.md
FORBIDDEN=app/**
MAX_LINES=N/A
DIFF_REF=N/A
ANCHOR_PROBED=2026-10-06 14:47
BASE_COMMIT=N/A
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-1: 對齊三模式契約
- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值**: （占位）
- **目標值**: （占位）
- [ ] 完成

## Hard Constraints
1. 只改 ALLOWED。

## Error Handling
- 錨點不符 → 停止回報

## Acceptance Criteria
<此處貼入三行抽取結果>
- [ ] ~~assembleDebug PASS~~ (N/A: docs preset)
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案
```
- [x] 完成（.tmp/rgr-replay/FX-01.md 已建，2026 bytes；三行 sha1 c90254f2／f5ece4c0／951a1f48 相符；抽取結果與 fixture 全文中 TKA／RGR／ALIGN／ACTYPE／replay／本票 字面皆 0 命中）

### AI-7: 派發全新 subagent 執行 Preflight replay 並記錄首份 report findings
- **動作**（AI-1～AI-5 已完成並寫入後執行；規則以改後之 SKILL.md 現值為準；證據一律寫入本票收尾實錄）：
  1. replay 前快照（主 session）：`Pending/` 檔案集合、`registry_list(project=SmartCameraApp1)` 之 ID 集合與 total、`git status --short`。
  2. 以 Agent 工具派發全新 subagent（`subagent_type=general-purpose`；不得使用 fork，不繼承本票 context）。dispatch prompt 逐字為下列全文，且 MUST 不含預期 finding、TKA 史實或本票內容；prompt 原文 MUST 存入收尾實錄：

```text
Run the ticket-rigor-gate skill in Preflight Mode (interactive lane) on
@.tmp/rgr-replay/FX-01.md, following .claude/skills/ticket-rigor-gate/SKILL.md
exactly, including Mandatory Runtime Load. Treat the fixture as self-contained:
do not search for or read other ticket files, Registry entries, or other .tmp/ content.
Do not use the runner lane, Codex, or external research (record RESEARCH_SKIPPED if reached).
Write the report to the path SKILL.md specifies, then stop. Do not apply any repair.
```

  3. subagent 寫出之首份 report（`.tmp/rigor-gate/RGR_FX-01_<YYYYMMDD>.md`）即首輪 findings；記錄其路徑與 sha1。
  4. 因 fixture 未入冊，SKILL.md `Durable Output Requirement → Registry Write-Back` 對本 fixture 不執行（dispatch prompt 之「then stop」即此偏離之落實）；此為 fixture 專屬限制（Registry 禁寫）所致之已授權偏離，MUST 於證據中如實記載，不得視為 gate 拒收。（來源／現值衝突：規格 §11 稱 SKILL.md 本身無 registry 寫入指令；現值 `Registry Write-Back` 條實際要求 operator 執行 stage ① 轉態，以現值為準。）
  5. replay 後再取同三項快照並比對；依 AC 逐項判定 (1)(2)(3)，結果、report 路徑與 sha1 寫入本票收尾實錄。
- [x] 完成（subagent af3f234c1fc1fa54a 已執行，首份 report 與前後快照比對見收尾實錄）

## Hard Constraints
1. 產品修改僅限 ALLOWED 單檔。流程載體不計入產品範圍：本票票檔（`Pending/` → `deliver/` 搬移）、本票之 Registry 生命週期操作（僅經平台 `registry_*` 原語）、`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 之例行 Dashboard 同步（依 `ssot-governance.md` 之 Governance Artifact 清單與「Dashboard 慣例同步」條，開票時觀測 L15–L19、L209，行號僅定位用）；不得觸及他票之票檔與 Registry 條目。
2. 不新增任何 section 或 subsection 標題；所有新增內容均位於既有節內（新增表列屬既有表之延伸）。
3. AI-1～AI-5 之規則文字依目標值逐字寫入；不得改寫既有 Mechanism Bypass、Self-Satisfying AC、Degradable Guard 列與處置段、Severity Assignment Rules、Mechanical Verdict Rule；不新增 verdict 分支。
4. 不處理：REPORT_FIX 自動閉環、Codex 重試次數與成本上限、R0 routing、`sv_report` 格式／preflight／parser／receipt／Checkpoint 行為、`OUTCOME／SEQUENCE` 強制標籤制、TKA-ALIGN-01 原票與既有 artifact 之修訂。
5. replay 結論僅限「新版 RGR 對這些票面模式產生正確 finding」；MUST NOT 宣稱 R1 必然自動閉環、R2／R3 必然不再發生或 Cross-Review 必然 PASS。
6. replay 專屬限制（僅適用 AI-6、AI-7，不適用 AI-1～AI-5）：fixture 與 report 僅寫 `.tmp/`；全程不得寫 Registry、`Pending/`、`deliver/`；對 `TKA-ALIGN-01_retro.md` 僅唯讀抽取；禁 runner lane、禁 Codex、禁 Research 外部通道；禁以 desk replay 自行判 PASS；fixture 之 repair 與驗收無關。

## Error Handling
- AI-1／AI-5 現值命中數 ≠ 1，或 AI-6 抽取三行 sha1 不符 → 停止回報，禁推定等價位置。
- gate 拒收 fixture 輸入，或無法對該 fixture 產出 report（含要求其須已入冊方可審查）→ 停止回報 `DECISION_REQUIRED`，附 gate 原文；禁退回 desk replay 自行判 PASS。
- subagent 嘗試呼叫 runner 或 Codex → 停止回報，不得放行。Research 通道不呼叫，記 `RESEARCH_SKIPPED` 後續跑至首份 report。
- subagent 之 report 或回報顯示其讀取 fixture 以外之票檔、Registry 條目或其他 `.tmp/` 內容，或實際 dispatch prompt 與 AI-7 全文不符 → 該次 replay 無效，停止回報。
- 首份 report 之 findings 缺 (1)(2)(3) 任一 → 停止回報，附 report 原文；禁改寫規則文字（HC-3）或 fixture 以規避。
- replay 前後 `Pending/`、Registry ID 集合與 total、`git status --short` 快照出現差異 → 停止回報，附差異清單。
- 同一驗證項經兩次修正仍不符 AC → 停止回報。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）。開票時實測：`SKILL.md` 未被排除；`deliver/` 落點未被排除；`Pending/` 期票檔依 `.gitignore` 為 untracked 屬既有凍結點語義（INFRA-TRACKSCOPE-01），非本票 commit 標的。

## Acceptance Criteria
- [x] **[存在性]** Typology 節：reachability 列等於 AI-1 目標值（整行命中 1）；表後、`### On-Device Requirement Rule` 之前含 AI-2 三段（各以首句字面命中 1）；AI-2 新增文字內 `preset`（不分大小寫）字面零命中（判準以驗收對象而非 preset 表述；「docs-only」僅出現於允許句，非條件判準）。（證據：reachability 列整行命中 1；AI-2 三段首句各命中 1 且位於 On-Device 標題之前；新增文字 preset 不分大小寫命中 0）
- [x] **[存在性]** Vacuous Verification：表含 `Unobserved Procedure Claim` 列恰 1 列且為三欄、緊接 `Degradable Guard` 列之後；`**Mandatory disposition (Unobserved Procedure Claim)**` 段含「程序非需求 → 改寫為結果 AC 並明載順序不屬驗收」與「程序屬需求 → 指名 observer」兩分支及 `A tool-call transcript is one possible observer` 句，位於 `For **document-layer evidence**` 段之後、`## Mandatory Runtime Load` 之前；引言行含第四變體句。（證據：新列恰 1 列、cols=3、緊接 Degradable Guard；處置段與 transcript 句各命中並位於 document-layer 段之後、Mandatory Runtime Load 之前；引言第四變體句命中 1）
- [x] **[存在性]** `git diff -U0 <DIFF_REF>..<DIFF_END> -- .claude/skills/ticket-rigor-gate/SKILL.md` 之刪除行恰為 AI-1 與 AI-5 之兩行現值；既有 Mechanism Bypass、Self-Satisfying AC、Degradable Guard 列、Severity Assignment Rules、Mechanical Verdict Rule 位元組級不變；新增行中無 `^#` 標題行。（證據：git diff -U0 刪除行恰為 AI-1、AI-5 兩行現值；numstat 13 增 2 刪；新增標題行 0；既有列與 Severity／Mechanical Verdict Rule 無刪除）
- [x] **[行為性]** given AI-6 建立之去識別化 replay fixture（`.tmp/rgr-replay/FX-01.md`，`PRESET=docs`，逐字含 TKA-ALIGN-01 L125／L126／L129 三行 AC），when 由全新 subagent（不繼承本票 context）依 AI-7 dispatch prompt 原文以 Preflight Mode 審查（不經 runner、不呼叫 Codex、不做 repair），expect 其寫出之首份 report（`.tmp/rigor-gate/RGR_FX-01_<YYYYMMDD>.md`）之 findings 含：(1) 對 L125 AC 之 typology finding（引用文件契約 `file:line` 卻標 `[可達性]`，應改標 `[存在性]`）；(2) 對 L126 AC 之 `Unobserved Procedure Claim` finding（「未讀完整 TPL」程序斷言無 observer，而 L129 僅驗結果）；(3) 對 (2) 之處置要求「明載順序不屬驗收」或「指名 observer」二擇一；且 replay 前後 `Pending/` 檔案集合、`registry_list(project=SmartCameraApp1)` 之 ID 集合與 total、`git status --short` 三項快照相同。observer＝上述首份 report（路徑與 sha1）、dispatch prompt 原文，及前後快照比對紀錄。結論限界見 HC-5。（證據：見收尾實錄 replay 判定；三項快照前後相同；單次抽樣）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（以 `DIFF_REF..DIFF_END` 實作 commit 範圍計；HC-1 所列流程載體另行提交，不在此範圍內計算）（證據：實作 commit e6090236；git diff --name-only 91e1a2f7..e6090236 僅 SKILL.md；verify_checklist Step 0 PASS）

## 收尾實錄（Closeout，2026-10-08）

- **實作**：`.claude/skills/ticket-rigor-gate/SKILL.md` 單檔，`git diff --numstat` 為 13 增 2 刪；AI-1～AI-5 規則文字與規格逐字（sha1）相同；`DIFF_REF=91e1a2f7`。
- **裁決紀錄**：Akira 2026-10-08 裁決不開 Rev B，RGR 對本票回報之 F1（Medium）、F2～F4（Minor）記為 non-blocking observation，不修改票面。
- **replay 執行**：fixture `.tmp/rgr-replay/FX-01.md`（2026 bytes，執行前後 sha1 前 12 碼 `9078f59126a4` 不變）；subagent `af3f234c1fc1fa54a`（Agent 工具、`general-purpose`、非 fork）；dispatch prompt 逐字如 AI-7 全文：

```text
Run the ticket-rigor-gate skill in Preflight Mode (interactive lane) on
@.tmp/rgr-replay/FX-01.md, following .claude/skills/ticket-rigor-gate/SKILL.md
exactly, including Mandatory Runtime Load. Treat the fixture as self-contained:
do not search for or read other ticket files, Registry entries, or other .tmp/ content.
Do not use the runner lane, Codex, or external research (record RESEARCH_SKIPPED if reached).
Write the report to the path SKILL.md specifies, then stop. Do not apply any repair.
```

- **首份 report**：`.tmp/rigor-gate/RGR_FX-01_20261008.md`（20140 bytes，sha1 前 12 碼 `a6430544a39d`）；Gate=PARTIAL（4 Major）。subagent 自述讀取 SKILL.md、`REPAIR_ROUTING.md` 全文與 fixture，未讀 `OPS_CALIBRATION.md`；未做 repair、未呼叫 runner／Codex／Research，未寫 Registry。本票僅依 subagent 自述與 report 內容判定未讀取 fixture 以外內容，無獨立工具呼叫紀錄可交叉驗證。
- **期望判定**（以 report 為 observer）：
  1. **成立**：F-1 對 AC1（L125，標 `[可達性]` 而僅列 `file:line`）提出 mislabeled AC finding，並直接引用新增 Typology 條文（「僅斷言…`file:line`…者為 `[存在性]`」「列舉 producer 與 consumer 定義處本身不構成 trace」），§8 證據表將 AC1 實際類型判為「存在性」。此一引用同時回應 F1 之鑑別力疑慮：finding 依據為本票新增文字。處置選項含「改標 `[存在性]`」與「升級為真正 trace」二者，並非單一方向。
  2. **成立**：F-3 以 `Unobserved Procedure Claim` 為類別，指出 AC2（L126）斷言「未讀完整 TPL」而無 observer，並同列 AC3（L129）零寫入未具名 observer。
  3. **實質成立，措辭有差**：F-3 之處置為「依 Mandatory disposition 重寫 AC2；未具 observer 前不得計為 PASS，只能揭露」，DECISION_QUESTION 含「若程序屬驗收，由哪個 observer 記錄」與「若否，AC2 改為只斷言最終結果」兩分支。與預期之「明載順序不屬驗收」或「指名 observer」二擇一結構相符，但 report 未逐字出現「順序不屬驗收」，而以「只斷言最終結果」表述。
- **前後快照（主 session）**：`Pending/` 16 檔，檔名集合 sha1 前 12 碼 `af24d6f3e852` 前後相同；`registry_list(project=SmartCameraApp1)` 12 筆、ID 集合與各筆 revision（606）前後相同；`git status --short` 前後皆僅 ` M .claude/skills/ticket-rigor-gate/SKILL.md`。
- **揭露與限制**：
  - replay 為單次抽樣，僅證明新版 RGR 對這些票面模式產生所列 finding；不宣稱穩定性、R1 自動閉環、R2／R3 不再發生或 Cross-Review 必然 PASS（HC-5）。
  - report 中 F-2、F-5、F-5b、F-6、F-7、F-9 等 finding 針對 fixture 骨架之占位與缺漏而生，與本票驗收期望無關，不計入驗收。
  - subagent 在 report 使用 `RESEARCH_SKIPPED` 與 `REGISTRY_WRITEBACK_SKIPPED` 字面，兩者皆非 SKILL.md 既有 REASON_CODES；對應 RGR 審查 F2，已列為 non-blocking observation。
  - Registry Write-Back 對 fixture 依 AI-7 步驟 4 未執行，屬已授權偏離，非 gate 拒收。

### 驗收鏈結果（2026-10-08）

- **實作 commit**：`e6090236`（`[RGR-ACTYPE-01]`，1 檔 +13／-2，僅 ALLOWED）；`DIFF_REF=91e1a2f7`、`DIFF_END=e6090236`。
- **QA**：Step 0 Scope Gate PASS（`git diff --name-only 91e1a2f7..e6090236` 僅 SKILL.md）；Build／Tests／Lint／靜態盲區掃描 N/A（PRESET=docs）；`.claude/PROGRESS.md` 已達 80 行上限，Step 4 SKIPPED，Dashboard 與 Done 標記依 SOP 留待後續 `/compact`。
- **SV**：PASS（`.tmp/sv_report.md`，Stage A～C 除範圍／殘骸／一致性外皆 N/A）；`structural_verify.sh --post` PASS（ADVISORY：PROGRESS.md 80 行）。
- **Cross-Review**：Codex 機械輪次 R1，直接 **PASS**（`Guard Input Reachability: 無新 Guard`、`REACHABILITY: NONE`，無 REPORT_FIX 輪次）；結果檔 `.tmp/cross_review_result_20261008_222226.md`。Codex stderr 含 Windows sandbox（error 1312）與 MCP `AuthRequired` 環境錯誤，來自其讀取自身 memories 目錄，未影響 verdict。
- **誠實揭露**：①R0 預審（`cross-reviewer` subagent）本輪未執行；`cross-review.md` R0 路由字面為 MUST，SKAD 審查引述 Akira 2026-10-01 裁定（RGR-MIGRATION-LITE-01 冊面）謂 R0 非現行收束流程必要前置，本票未另行核對該裁定原文。②`[行為性]` AC 證據為單次 subagent 實跑，無可重播腳本，`.tmp/` 內 fixture 與 report 為短期傳遞，非耐久證據；耐久紀錄為本節與票面所載 sha1、dispatch prompt 原文與判定。
- **reviews**：qa=PASS（僅 Step 0；Build／Test／Lint N/A）、sv=PASS、codex=PASS（R1）、traycer=N_A（Header `Traycer Workflow: N/A`）。
