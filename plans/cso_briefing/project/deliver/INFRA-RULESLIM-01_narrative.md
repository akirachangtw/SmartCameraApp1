# INFRA-RULESLIM-01: 移除 ssot-governance.md 歷史敘事並更新 frontmatter description，標題與「證據缺口與研究授權」節不變，規範性語意經逐行分類核對保留

**Revision**：Rev B（2026-10-19；Rev=1；Desktop 審查裁決 CHANGES_REQUESTED，Akira 方案 A）。Rev A（2026-10-18；Rev=0；commit `881c4c9e`、票檔 `69848801`）。
**Rev B 差分（逐點）**：①冊面 `REVIEW→IN_PROGRESS`（`lifecycle` 參數）、票檔自 `deliver/` 退回 `Pending/`；②Verify Config 重錨：`DIFF_REF`／`BASE_COMMIT`＝`7c9d983a`、`TARGET_BLOB`＝`1db12d9a`（Rev A 值 `e95b14c5`／`e95b14c5`／`f6fde5a8`，保留於此作歷史），`ANCHOR_PROBED`＝2026-10-19 00:38；③新增 AI-6（恢復三處語意退化：E15 邊界句、E9 註冊事實、hunk 181 一致性依據）與 AI-7（AC 7b 全量逐行分類、更正宣稱、重跑不變量）；④新增 AC 7b／7c，並以 Rev B 終值補註 AC 1／5／6／7／10；⑤**更正 Rev A 宣稱**：Rev A「規則句不變／差集僅三處預期」不成立——逐字關鍵詞差集只證明含 MUST／禁／不得／SHOULD 的句子是否仍逐字存在，**不證明語義承接**；Rev A 的三處缺陷（E15 邊界句、E9 註冊事實、hunk 181 一致性依據）皆不含該等關鍵詞，故 AC 7 抓不到，詳見證據分級「Rev B 更正」與 AC 7b；⑥約束②「不改寫、不壓縮任何規則句」與 Rev A E5 實際改寫之矛盾如實承認：E5 將原 L175 合併契約段改寫為指標＋兩條 MUST NOT，Rev B 以 AC 7b 逐項核對其承接；⑦Rev B 的三處恢復皆不升級為 MUST NOT（另案 PENDING_DECISION）。

**檔案治理/來源說明**：本票為 Plan-backed 單票。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261018-ssotgov-trim-plan.md`（`.tmp` 非長期載體，下列內容使本票獨立成立）；Parent artifact status＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`；plan-local item＝`T01`（僅作 provenance，**不是**票號；Akira 2026-10-18 裁決，見 `.claude/CLAUDE.md` Rule 4e）；Parent read timestamp＝2026-10-18 14:2x（+08:00，全文實讀 98 行）。Template 實讀：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件產出日期 2026-05-29，最近有效修訂日 2026-09-14。

**Selected item 原文（plan-local T01 goal，摘要不改義）**：以「僅刪除預先列舉之敘事片段＋改寫 frontmatter 第 2 行」的方式，使 `.claude/rules/ssot-governance.md` 字元數下降，且所有 `##`／`###`／H1 標題、所有含 MUST／MUST NOT／禁止／不得之規則句、以及「證據缺口與研究授權」節皆不變。

**相關約束（取自 parent 與 Akira 2026-10-18 指令）**：①不拆檔、不新增任何文件；②不改寫、不壓縮任何規則句；③frontmatter 維持單行，使下游已引用的 `ssot-governance.md:11` 不位移；④凡規則依據為使用者裁決者，保留簡短授權標記（形如「Akira YYYY-MM-DD 裁決」），不保留事件經過；⑤「15–25%」為使用者推估，**已被實測否定**（見證據分級），不作通過門檻；⑥效果面（模型行為是否因此改變）無法以確定性方法證明，收尾 MUST 標 `UNVERIFIED`。

**證據分級**（2026-10-18 14:2x live，HEAD `e95b14c5`，工作樹 clean）：

- **CONFIRMED（本地工具輸出）**：①目標檔 300 行、28,614 字元（Python `len`）、CR 位元組數 0（LF，`.gitattributes` `eol=lf`）、file sha8 `ff0d9c1b`、`git hash-object` sha8 `f6fde5a8`、已追蹤、`git check-ignore` 無命中；內容自 `6aae9f10` 起未變（`git diff 6aae9f10 HEAD -- <目標>` 為空）。②標題集合（`grep -E '^#{1,3} '`）sha8 `f3d6a5b1`；「證據缺口與研究授權」節（標題起 7 行）sha8 `9e648bca`；L11 為 Governance Artifact 清單引言句。③**預跑（暫存副本，未動 repo）**：套用下列 AI-1～AI-3 全部替換後，結果檔 sha8 `45f7a9c4`、27,058 字元（**−1,556，−5.44%**）、298 行；標題集合 sha8 仍 `f3d6a5b1`、新增節 sha8 仍 `9e648bca`、L11 逐字相同、CR 位元組 0；`git diff` 風格統計＝14 行修改＋2 行刪除（numstat 預期 `14 added / 16 deleted`）。含 MUST／禁／不得／SHOULD 之句 126→125，差集僅為下列三處（皆在列舉片段內）：「APP 端專屬約束」句（E4 去證據括號）、原 L175 合併句（E5 改寫，兩條 MUST NOT 保留）、L260 `立法事由` 句（E10 刪除）。④無程式解析本檔文字：`.claude` 之 hooks、skills scripts、scripts 對本檔僅有檔名提及（`gate_stop_guard.py:5` docstring、`session_start.sh:130` echo、`preflight_activation.sh:130` 註解、`cross_review.sh:14` 註解）；`觸發來源／立法事由／翻盤紀錄／事件支撐` 於 `.sh`／`.py` 零命中；將移除的 7 個代表片段於其他活躍檔零逐字引用。⑤以**行號**引用本檔的外部位置僅二：`.claude/PROGRESS.md:13`（提及 `ssot-governance.md:11`）、`plans/roadmap/autonomous-engineering-gate-outcomes.md:36`（引用 `ssot-governance.md:211`，現值為 Advisor 契約句）。⑥冊面全冊（主冊 9＋歸檔 104）無等價成果票；`INFRA-RULESLIM` stem 於 Registry、`Pending/`、`deliver/`、`void/` 零命中。
- **UNVERIFIED（無確定性證明方法）**：精簡後模型遵循行為是否改變；實際 token 節省量（無 `count_tokens` 實測，以字元數為度量）。
- **與 parent 的衝突處置**：parent 推算淨移除 2.0–2.2K 字元（7–8%），預跑實測 **1,556（5.44%）**，低於 parent 的 1,800 字元「須如實回報原因」門檻。原因（如實）：①為保留授權標記，L21／L100／L273／L289／L290／L300 改為「就地縮短」而非整段刪除；②L300 保留「平台為唯一語義權威、APP 採五段語義」實質句；③Gate 現況事實（Stop guard 已註冊、升報通知對齊未實作、驗證限制）各保留一份；④frontmatter `description` 改寫後較原句長 48 字元（53→101）。**依 parent 條文，不為湊數而補刪規則句**。以實測值為準，AC 以預跑結果為基準。
- **Rev B 更正（取代 Rev A 證據分級③「差集僅三處預期、規則句不變」之宣稱）**：①Rev A 的逐字關鍵詞差集（`before 126 after 125`）只能證明「含關鍵詞的句子是否逐字還在」，不能證明語義承接；`881c4c9e` 另有三處承重退化皆不含關鍵詞：舊 L300 邊界句「APP 採取最小必要之 consumer 實作…」被刪、舊 L247 由「已註冊於 `settings.json` 之 Stop」降為「已落地」、舊 L181「一致性依據」被整行刪除。②Rev B 對 `881c4c9e` 全部 16 條刪改行逐行分類（AC 7b／AI-7），缺陷共三處，皆已補回（AI-6）；其餘 13 列確認為歷史/證據或授權標記，或事實已由新文本承接。③Rev B 終值（實測，2026-10-19）：file sha8 `7a426261`；27,210 字元（對 `6aae9f10` 累計 **−1,404，−4.91%**；較 Rev A 多 152 字元）；299 行（累計 −1）；累計 numstat `15 16`、Rev B 增量 numstat `3 2`；標題 `f3d6a5b1`、新增節 `9e648bca`、L11 `a440e7ca` 不變、CR 0；累計關鍵詞差集 `before 126 after 125`（與 Rev A 同，GONE 三筆／ADDED 兩筆），Rev B 增量關鍵詞差集為空（`125→125`）。④Rev A 所列 126→125、sha8 `45f7a9c4`、27,058／298、numstat `14 16` 為 **Rev A 結果值，不再是終值**。⑤**外部行號**：Rev B 於舊 L181 位置恢復一行，Advisor 契約句回到 **`:211`**，Rev A「`:211` 位移至 `:210`、引用應改 `:210`」之交代**撤回**；`:11` 仍未位移。

**External-review decision**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` §6，L43–L50，2026-10-18 live 直讀；分項記錄）：6a—`INTRODUCED_CAPABILITY=NONE`，不成立；6b①—`ALLOWED` 單檔，首層 path segment distinct＝1 <2，不成立；6b②—`### AI-[1-9][0-9]*:` 標題數＝5 <8，不成立；6b③—`DEPENDENCY_DELTA=NONE`，不成立；6c—目標檔無檔頭 `Revision` 修訂歷史（版次來源唯一為該欄位），且本票為純文件性變更（不改判準、不改狀態機、不改執行邏輯），不納本款。外審 **NOT_TRIGGERED**，不送 PPSS。計數讀取（O1）：`Rev=1 ｜ AI 標題數=7 ｜ AI 深度=1 ｜ 4a/4d/6b 狀態=未成立`（Rev A 為 `Rev=0 ｜ AI 標題數=5`；Rev B 新增 AI-6／AI-7，仍 <8；Rev=1 <3）。

**Cross-Review／QA／SV**：依 2026-10-02 風險分級（`.claude/CLAUDE.md` SOP 區：低風險 docs／治理文本票〔僅 `.md`、可 `git revert`〕得省略 `/qa`、`/sv`、cross_review），**本票明示省略**（Akira 2026-10-18 指令確認）；以 AC 的確定性靜態檢查代之。

**Rigor Gate Waiver**：規範性子句數觸發條件字面成立（Hard Constraints／Error Handling 約 14 句含 MUST／不得／禁／停止回報），其餘四條觸發（拆票、≥3 檔、validator／schema／狀態機類、FIX 家族）皆不成立。本票為單檔精確字串替換，預跑暫存副本已驗證結果 sha8 與全部不變量，純 `.md` 可 `git revert`，並已依 2026-10-02 風險分級省略 `/qa`、`/sv`、cross_review；Akira 2026-10-18 於評估後選定不跑 RGR（票面補強三處〔AC 7 命令、commit 前綴先例、本 Waiver〕取代之）。依 `ssot-governance.md` §0 waiver 直跳條款，得 `PENDING → IN_PROGRESS` 直跳。

**簽發日期**: 2026-10-18
**簽發者**: Claude
**來源議題**: INFRA
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無（`6aae9f10` 為移除前基準 commit，非票；plan 與預跑結果已內嵌於本票）
- `coordination_dependency`: 無 —— 但與任何其他編輯 `.claude/rules/ssot-governance.md` 之票 **MUST 串行**（`concurrency.md` §2）。已知另有兩個尚未開票之工作項亦會編輯本檔：`20261017-projman-plan / T03 / 治理條款呼叫與 ssot-governance.md:11 一行修正`、`20261016-cli-advisor-plan / T02 / Advisor 契約放寬`（plan-local item，不是正式 Ticket ID，不得預先虛構票號；其開票者須讀 live 檔）。

## Verify Config
TICKET_ID=INFRA-RULESLIM-01
CREATED=2026-10-18
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md
FORBIDDEN=.claude/CLAUDE.md,CLAUDE.md,CLAUDE.local.md,.claude/rules/concurrency.md,.claude/rules/cross-review.md,.claude/rules/ticket_pattern.md,.claude/rules/decommission.md,.claude/rules/kairos-lessons.md,.claude/rules/engineering-discipline.md,.claude/rules/mcp-service-safety.md,.claude/skills/sv/SKILL.md,.claude/skills/qa/SKILL.md,.claude/skills/uc/SKILL.md,.claude/skills/compact/SKILL.md,.claude/skills/ticket-author/SKILL.md,.claude/skills/sv/scripts/cross_review.sh,.claude/hooks/gate_stop_guard.py,.claude/hooks/session_start.sh,.claude/scripts/gate_ledger.py,.claude/settings.json,.claude/settings.local.json,.agents/skills/compact/SKILL.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,plans/roadmap/autonomous-engineering-gate-outcomes.md,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json
MAX_LINES=40
DIFF_REF=7c9d983a
ANCHOR_PROBED=2026-10-19 00:38
BASE_COMMIT=7c9d983a
TARGET_BLOB=.claude/rules/ssot-governance.md:1db12d9a
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

> `PRESET=docs`：僅修改一個 `.md` 規則檔；`assembleDebug` N/A。`MAX_LINES=40` 為 production-only diff 估算校準遙測（預跑 diff 為 14 修改＋2 刪除行），**非 execution gate**。`FORBIDDEN` 使用字面路徑（避開 `verify_checklist.sh` 對含 `/` token 的 glob 字面比對缺陷）。`BASE_COMMIT`／`ANCHOR_PROBED`／`TARGET_BLOB` 為資訊性診斷值（v3.11），不作 gate；漂移處置見 AI-0 #7。**EOL**：本檔 MUST 保持 LF，不得出現 CR。**DEFERRED-TO-DOCSYNC（不開票）**：FORBIDDEN 命中 `.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md`（對照 `rules/cross-review.md`「管線標準觸碰面登記表」）→ Dashboard 同步、Done 標記、票檔 Pending→deliver 搬移與 checkbox 勾選屬標準 SOP 收尾，於下一輪 `/compact` 正常處理，**不另開票**。暫存物（若有）MUST 置於 `tmp/INFRA-RULESLIM-01/` 或系統暫存目錄（`concurrency.md` §1）。

## Action Items

> **AI-0 為強制首項**。以下為錨點與基線復驗；字面錨點不符時依 TPL「基線漂移三分流」（v3.11）分類並以現值續作，不得單獨作為停工依據，但「`current` 字串於檔內找不到且非他票等價完成」者停止回報（見 Error Handling）。

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/rules/ssot-governance.md`（唯讀）
- **動作**: 實際執行並記錄輸出：
  1. `git rev-parse --short HEAD`；`git status --porcelain` 對目標檔；`git hash-object .claude/rules/ssot-governance.md | cut -c1-8`（開票時 `f6fde5a8`）；`sha256sum .claude/rules/ssot-governance.md | cut -c1-8`（開票時 `ff0d9c1b`）；`wc -l`（開票時 300）；`git ls-files --error-unmatch` 命中；`git check-ignore -v` 無命中。
  2. 標題集合：`grep -E '^#{1,3} ' .claude/rules/ssot-governance.md | sha256sum | cut -c1-8`（開票時 `f3d6a5b1`）。新增節：`grep -A6 -F '### 證據缺口與研究授權' .claude/rules/ssot-governance.md | sha256sum | cut -c1-8`（開票時 `9e648bca`）。L11：`sed -n 11p .claude/rules/ssot-governance.md | sha256sum | cut -c1-8`（記錄值，供 AC 4 比對）。
  3. EOL：`python -I -c "print(open('.claude/rules/ssot-governance.md','rb').read().count(b'\r'))"` 應為 0。（**勿**用 `grep -c $'\r'`，該寫法在命令替換內會誤判。）
  4. 對下列 AI-1～AI-3 的每一個 `current` 字串（E0–E15）逐一 `grep -cF -- '<current>' .claude/rules/ssot-governance.md` 並記錄命中數；開票時皆為 1（E10 以不含開頭三個空格的子字串比對）。
  5. 行號引用基線：`grep -rnE "ssot-governance\.md:[0-9]+" .claude plans/roadmap .agents CLAUDE.md --include=*.md --include=*.sh --include=*.py | grep -v PROGRESS_ARCHIVE`（開票時恰二筆，見證據分級⑤）；`sed -n 211p`（開票時為 `- Advisor 由主 agent 承接…`）。
  6. 程式解析者：`grep -rnE "觸發來源|立法事由|翻盤紀錄|事件支撐" .claude --include=*.sh --include=*.py | grep -v /tests/` 應零命中；`.claude` 內 hooks／skills scripts／scripts／tests 對本檔之提及僅為檔名（開票時 4 處，見證據分級④）。
  7. 漂移處置：若 #1 的 sha8 ≠ `ff0d9c1b`（他票已編輯本檔）→ 依 v3.11 分類：先 `git diff --name-only e95b14c5..HEAD -- .claude/rules/ssot-governance.md` 與前綴歸因；`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 為資訊性，記錄後以現值續作（此時 AC 1 的 sha8 `45f7a9c4` 不再適用，改驗 AC 2–11）；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止。
- **#1–#4 之 sha／命中數不符且無法依 #7 歸因 → 停止回報，禁自行推定等價位置；#5／#6 結果與預期不同 → 記錄後以現值續作並於收尾揭露。**
- [x] 完成（2026-10-18 14:4x：HEAD `e95b14c5`、blob `f6fde5a8`、file sha8 `ff0d9c1b`、300 行、已追蹤且未被 ignore；標題 `f3d6a5b1`、新增節 `9e648bca`、L11 sha8 `a440e7ca`；CR 位元組 0；17 個現值區塊（E0–E15，含 E8a／E8b）於 live 檔皆命中恰 1 次；行號引用恰二筆（`:11`、`:211`）且 `sed -n 211p` 為 Advisor 句；程式解析者 0；`preflight_activation.sh` V0 PASS／V1 `BASELINE_MATCHED`／`WRITE_AUTHORITY=YES`；無漂移，AI-0 #7 N/A）

### AI-1: frontmatter `description` 更新（E0）
- **檔案**: `.claude/rules/ssot-governance.md`（第 2 行；**維持單行、行數不變**）
- **現值**（唯一）:

```text
description: PROGRESS.md / SSOT 文件治理規則 — 確保儀表板精簡、歸檔紀律
```

- **目標值**:

```text
description: 治理規則 SSOT — Ticket 生命週期、Transition Table、Autonomous Engineering Gate、Closure、PROGRESS 紀律
```

- [x] 完成（E0 已套用；見 AI-4 實測）

### AI-2: 移除歷史敘事，保留授權標記（E1–E4、E10–E15）
- **檔案**: `.claude/rules/ssot-governance.md`
- **實作註記（2026-10-18 執行期發現，如實記錄）**：首次以 Edit 工具逐項套用時，**目標值為空字串的刪除型替換**（E2、E6、E10、E14）會連同被刪片段之後的行尾換行一併吞掉，使相鄰行合併（L72 與其後空行、L259／L260／L261 等），實測 sha8 `dc9213fd`、294 行、numstat `15 21`，**與預跑不符**。處置：僅 `git checkout -- .claude/rules/ssot-governance.md` 還原本票自己的未提交編輯（還原後 sha8 `ff0d9c1b` 與 AI-0 一致），改以與預跑同一份**精確字串替換腳本**一次性套用同一組 E0–E15，結果與預跑位元組一致（sha8 `45f7a9c4`）。後續若需手動以 Edit 做刪除型替換，MUST 把相鄰換行納入 `old_string` 並自行驗行數，或改用精確字串替換腳本。
- **動作**: 以 Edit 或等價之**精確字串替換**逐一處理（不得改寫其他字元）。「目標值」為 `（刪除）` 者，刪除該 `current` 字串；為 `（刪除整行）` 者，刪除整行含行尾換行。每項 `current` 於檔內唯一。
- **E1（L21）現值**:

```text
觸發來源：Akira 2026-07-30 對話裁決——`.gitignore` Registry 裁決記錄僅存於 PROGRESS.md 敘述文字/memory，未回寫進 `compact/SKILL.md` 執行邏輯，導致每次 `/compact` 仍以單檔硬編碼判斷 Scope 汙染；改為表驅動以杜絕「規則寫了但執行邏輯沒讀到」重演。
```

  **目標值**: `（Akira 2026-07-30 對話裁決：以表驅動判斷 Scope 汙染。）`
- **E2（L72 尾句）現值**:

```text
事件支撐：PLT 端 2026-08-30 實測發現四張票檔在 `docs/pending/` 而冊面查無，其中兩張為已 VOID 歸檔票之滯留副本；APP 端 2026-09-05 已知至少存在 `MEO-FIX-A`／`MEO-FIX-B` 系列孤兒檔與 `void/INFRA-CLAUDE-MERGE-01`（票檔在盤而主冊與歸檔冊皆查無）。
```

  **目標值**: （刪除）。刪除後該行應結束於「④`Pending/` 內非票檔文件。」。
- **E3（L100）現值**:

```text
（Akira 2026-09-09 裁決；事件：Traycer 對未入冊且已裁決不核准開立之 `INFRA-PREFLIGHT-CORE-01-FIX-D` 產出 T1 `Approach Drift` 判定，其存在性依據為 Traycer 自家 Epic 之 `Todo` 狀態）
```

  **目標值**: `（Akira 2026-09-09 裁決）`
- **E4（L113 括號）現值**:

```text
（2026-09-02 實測：頂層 `summary` 寫入後 `checkpoint` 為 `{}`）
```

  **目標值**: （刪除）。刪除後「…read-back 驗證」之後直接接句號。
- **E10（L260，整行，開頭三個空格）現值**:

```text
   **立法事由**：`ACAGOV-RUNTIME-TABLE-01` 自身收尾即走軌道 A（commit `2fbc017`），與原單軌 MUST 相衝；Akira 裁決不追罰、不 revert，改撤該 MUST——本地 commit 為最廉價之回退點，強制延後反降低可回復性（可逆優先）。
```

  **目標值**: （刪除整行）。（該節第 5 點標題已含「Akira 2026-09-15 裁決」授權標記。）
- **E11（L273，整行）現值**:

```text
觸發來源：`RGR-INTERACTIVE-CLOSURE-AUTH-01` 執行期間，Action Items 與 AC checkbox 僅勾入本地 untracked 票檔、未同步 registry checkpoint，導致 Akira 由 TKR 系統視角判讀為「全數未完成」，與執行端本地認知不同步（Akira 2026-09-25 對話裁決，免票直接授權補此條）。軌 3（回報前核對）為同日追加：僅要求「做兩件事」仍不足以防止漏做或做錯後未察覺就回報，故補一道獨立於軌 1／2 執行過程之事後核對步驟。
```

  **目標值**: `（Akira 2026-09-25 對話裁決，免票直接授權補此條；軌 3 為同日追加。）`
- **E12（L289，整行）現值**:

```text
- 觸發來源：INFRA-CLOSURE-SOP-01（Akira 2026-07-17 對話裁決：全票適用之標準收尾流程；實例缺口 INFRA-AGENTS-CR-02 Done 但票內 checkbox 全未勾）；即時勾選條款觸發來源：`INFRA-QA-COMMIT-EMPTYDIFF-01` + `-FIX-A`（碼閉環與 Codex PASS 數日後才因 Akira 詢問完成度而補勾，Akira 2026-07-19 對話裁決授權補訂）。
```

  **目標值**: `- 授權：Akira 2026-07-17 對話裁決（全票適用之標準收尾流程）；即時勾選條款由 Akira 2026-07-19 對話裁決授權補訂。`
- **E13（L290 尾句）現值**:

```text
觸發來源：`INFRA-CONCURRENCY-01`（Akira 2026-07-19 對話裁決：「這本來就是標準的 SOP 執行票流程」，避免治理疊床架屋）。
```

  **目標值**: `（Akira 2026-07-19 對話裁決）`（接在「…處理即可。」之後，不另加句號）。
- **E14（L291 尾句）現值**:

```text
觸發來源:MEO v1 十輪修補無停損。
```

  **目標值**: （刪除）。
- **E15（L300，整行）現值**:

```text
**翻盤紀錄**：2026-08-27 治理減量裁決（APP 冊 lifecycle 維持 `PENDING / REVIEW / CLOSED / VOID` 四態之刻意裁剪）由 2026-09-06 Akira D-1 裁決推翻，翻盤型態＝明示 supersede：PLT／TKR 平台為 lifecycle、狀態轉換、registry 資料契約與共通票務流程之唯一語義權威，APP 自此採 PLT 五段語義（見 §Ticket 生命週期流程（五段）），採取最小必要之 consumer 實作而非重寫平台 enum 或移植全套 script。（`INFRA-LIFECYCLE-SSOT-01`）
```

  **目標值**: `**翻盤紀錄**：2026-09-06 Akira D-1 裁決明示 supersede 2026-08-27 治理減量裁決（APP 冊 lifecycle 四態之刻意裁剪）：PLT／TKR 平台為 lifecycle、狀態轉換、registry 資料契約與共通票務流程之唯一語義權威，APP 自此採 PLT 五段語義（見 §Ticket 生命週期流程（五段））。`
- [x] 完成（E1–E4、E10–E15 已套用；見 AI-4 實測）

### AI-3: 精簡 Gate 節落地現況敘事，保留禁令與缺口揭露（E5–E9）
- **檔案**: `.claude/rules/ssot-governance.md`
- **動作**: 同 AI-2，精確字串替換。**不得觸碰** `覆蓋矩陣` 中「機械後盾」「無機械後盾」及其後之缺口揭露文字。
- **E5（L175，整行）現值**:

```text
> 契約狀態：本節為契約文字。helper 與 ledger 已存在（ADV-GATE-02）；入口 `check` 呼叫已接入 qa／commit／sv／closeout／done，`resume` 讀 `status`（ADV-GATE-03）。呼叫路徑屬模型契約，不被機械阻擋；本 Gate 專用 Stop guard（`.claude/hooks/gate_stop_guard.py`）已註冊於 `.claude/settings.json` 的 Stop；升報通知對齊尚未實作。Advisor 採納之驗證限制（內容加密、不可驗）照實保留；agent **MUST NOT** 宣稱完整 Gate，**MUST NOT** 宣稱 hook 涵蓋途中違規。
```

  **目標值**:

```text
> 契約狀態：本節為契約文字；現況與宣告限制見 §實作狀態與宣告限制。入口 `check` 呼叫路徑屬模型契約，不被機械阻擋；agent **MUST NOT** 宣稱完整 Gate，**MUST NOT** 宣稱 hook 涵蓋途中違規。
```

- **E6（L181，整行）現值**:

```text
- 一致性依據：Transition Table 節首引言（本檔 `:126`）之限制字面範圍為「本節」且該行未變；本節為獨立節並受上述不外溢條件約束。將該限制改為全檔通用屬另案修訂。
```

  **目標值**: （刪除整行）。
- **E7（L224 句首）現值**:

```text
入口 check 呼叫已接入（ADV-GATE-03）。
```

  **目標值**: （刪除）。刪除後該行應為「…正常入口最小落點：`qa`＝…」。
- **E8a（L225）現值**:

```text
現況（ADV-GATE-04 提交）：
```

  **目標值**:

```text
現況：
```

- **E8b（L225）現值**:

```text
（ADV-GATE-03；`done` 見
```

  **目標值**:

```text
（`done` 見
```

- **E9（L247，整行）現值**:

```text
- 現況：helper 與 ledger 已存在（ADV-GATE-02），入口 `check` 呼叫已接入（ADV-GATE-03），本 Gate 專用 Stop guard（`gate_stop_guard.py`）已註冊於 Stop（ADV-GATE-04）；升報通知對齊尚未實作。Advisor 採納之驗證限制（內容加密、不可驗）照實保留；不宣稱完整 Gate，不宣稱 hook 涵蓋途中違規。
```

  **目標值**:

```text
- 現況：helper 與 ledger、入口 `check` 呼叫、本 Gate Stop guard（`gate_stop_guard.py`）均已落地；升報通知對齊尚未實作。Advisor 採納之驗證限制（內容加密、不可驗）照實保留；不宣稱完整 Gate，不宣稱 hook 涵蓋途中違規。
```

- [x] 完成（E5–E9 已套用，與 AI-2 同一次套用；見 AI-4 實測）

### AI-4: 不變量驗證與量測記錄（不改檔）
- **檔案**: `.claude/rules/ssot-governance.md`（唯讀驗證）
- **動作**: 於 AI-1～AI-3 完成後，重跑 AI-0 #1–#3 的同一組命令，並記錄：①file sha8（base 未漂移時應為 `45f7a9c4`）；②標題集合 sha8（應 `f3d6a5b1`）；③新增節 sha8（應 `9e648bca`）；④L11 sha8 與 AI-0 #2 記錄值相同；⑤CR 位元組數 0；⑥`git diff --numstat -- .claude/rules/ssot-governance.md`（base 未漂移時應為 `14 16`）與 `git diff -U0` 的 hunk 起始（舊檔）行集合（預跑為 13 個 hunk：{2,21,72,100,113,175,181,224,247,260,273,289,300}；相鄰行 224／225 與 289／290／291 各合併為一個 hunk，故實際集合 ⊂ 16 個編輯行號 {2,21,72,100,113,175,181,224,225,247,260,273,289,290,291,300}）；⑦字元數（`len`）與行數（預期 27,058／298），並與 28,614／300 相減記錄淨移除量與百分比；⑧含 MUST／MUST NOT／禁／不得／SHOULD 之句前後數量（預期 126→125，差集僅證據分級③所列三處）。
- **⑧ 的命令**（於 repo 根目錄、尚未 commit 時執行；以 `HEAD` 為編輯前基準，無引數時比對工作樹現檔；開票時已對暫存預跑結果驗證輸出為 `before 126 after 125`、GONE 三筆、ADDED 兩筆，對未編輯檔輸出 GONE／ADDED 皆空）：

```bash
python -I -c 'import re,subprocess,collections as C,sys;sys.stdout.reconfigure(encoding="utf-8");kw=re.compile("MUST|禁|不得|SHOULD");f=".claude/rules/ssot-governance.md";b=subprocess.run(["git","show","HEAD:"+f],capture_output=True).stdout.decode("utf-8");a=open(sys.argv[1] if len(sys.argv)>1 else f,encoding="utf-8").read();S=lambda t:C.Counter(s.strip() for s in re.split("[。\n]",t) if kw.search(s));x,y=S(b),S(a);print("before",sum(x.values()),"after",sum(y.values()));print("GONE",[s[:50] for s in x if x[s]>y.get(s,0)]);print("ADDED",[s[:50] for s in y if y[s]>x.get(s,0)])'
```

  預期 GONE：「APP 端專屬約束」句（E4 去證據括號）、原 L175 合併句（E5 改寫）、`立法事由` 句（E10 刪除）；預期 ADDED：「APP 端專屬約束」句（E4 後版本）、E5 新句。GONE／ADDED 若出現上述以外的句子 → 屬規則句被動到，停止回報。
- 任一不變量（②③④⑤）不符 → 停止回報；其餘不符 → 記錄差異原因（含是否因 AI-0 #7 之漂移）。
- [x] 完成（2026-10-18 14:5x，**最終套用後**重跑）：①file sha8 `45f7a9c4`；②標題集合 `f3d6a5b1`；③新增節 `9e648bca`；④L11 sha8 `a440e7ca`（與 AI-0 同）；⑤CR 位元組 0；⑥`git diff --numstat` ＝ `14 16`，hunk 起始行 {2,21,72,100,113,175,181,224,247,260,273,289,300}；⑦27,058 字元、298 行（28,614／300 → **−1,556，−5.44%**）；⑧`before 126 after 125`，GONE 三筆、ADDED 兩筆，與預期完全一致。AI-0 #6 重跑 0 命中。（首次 Edit 套用之失敗結果見 AI-2 實作註記，已還原重做，本欄為最終值。）

### AI-5: 外部行號引用盤點與收尾交代（不改他檔）
- **檔案**: 無（寫入票面收尾實錄）
- **動作**: ①重跑 AI-0 #5 的 grep，逐筆標註「仍指向同一句／已位移」：預期 `ssot-governance.md:11` 仍指向 Governance Artifact 清單引言句（未位移）；`ssot-governance.md:211`（`plans/roadmap/autonomous-engineering-gate-outcomes.md:36`）因 E6 刪除一行而位移，Advisor 契約句現位於 `:210`（Rev A 值；Rev B 因 R3 恢復一行已回到 `:211`，見 AI-7）。②**不修改**上述兩個引用檔（皆在 FORBIDDEN），將「過期行號引用清單」寫入收尾實錄，交其所有者（`projman align`／下一輪 `/compact`）處理，不寫成「無影響」。③收尾實錄 MUST 記錄：移除前基準 `6aae9f10` 與 BASE_COMMIT `e95b14c5`、AC 實測值、本票未驗證項（`UNVERIFIED`：模型遵循行為、token 節省量）。④不宣稱「行為不變已驗證」，僅可宣稱「diff 範圍受限於列舉片段；規則關鍵詞句逐字差集見 AC 7／7c，規範性內容之語意承接以 AC 7b 逐行分類為準」（Rev B 更正，取代 Rev A 之「規則句未被移除」）。
- [x] 完成（見文末「收尾實錄」之外部行號引用清單與交代；Rev B 補充見 AI-7）

### AI-6: Rev B 恢復三處語意退化（R1–R3）
- **檔案**: `.claude/rules/ssot-governance.md`
- **動作**: 以精確字串替換腳本套用（三處皆為**非空**替換；刪除型替換禁用 Edit 空 `new_string`）。**寫入前先讀 `.claude/settings.json` 確認 `gate_stop_guard.py` 仍註冊於 `hooks.Stop`**（Rev B 實測：`settings.json:150`，Stop 共 4 支 hook）。三處皆**不得升級為 MUST NOT**（另案 PENDING_DECISION）。
- **R1（E15'，原 :298、現 :299）現值**:

```text
**翻盤紀錄**：2026-09-06 Akira D-1 裁決明示 supersede 2026-08-27 治理減量裁決（APP 冊 lifecycle 四態之刻意裁剪）：PLT／TKR 平台為 lifecycle、狀態轉換、registry 資料契約與共通票務流程之唯一語義權威，APP 自此採 PLT 五段語義（見 §Ticket 生命週期流程（五段））。
```

  **目標值**（翻盤歷史縮為簡句＋另立一句邊界，邊界句措辭照抄使用者指定原文）:

```text
**翻盤紀錄**：2026-09-06 Akira D-1 裁決推翻 2026-08-27 治理減量裁決（APP 冊 lifecycle 四態之刻意裁剪）。PLT／TKR 平台為 lifecycle、狀態轉換、registry 資料契約與共通票務流程之唯一語義權威，APP 自此採 PLT 五段語義（見 §Ticket 生命週期流程（五段））。APP 採取最小必要之 consumer 實作，而非重寫平台 enum 或移植全套 script。
```

- **R2（E9'，原 :246、現 :247）現值（片段）**:

```text
- 現況：helper 與 ledger、入口 `check` 呼叫、本 Gate Stop guard（`gate_stop_guard.py`）均已落地；升報通知對齊尚未實作。
```

  **目標值（片段）**:

```text
- 現況：helper 與 ledger、入口 `check` 呼叫均已落地；本 Gate Stop guard（`gate_stop_guard.py`）已註冊於 `.claude/settings.json` 之 Stop；升報通知對齊尚未實作。
```

- **R3（hunk 181 恢復，現 :181）現值（插入點：不外溢條件 bullet 之後、`### Gate 適用條件與順序` 之前）**:

```text
本節不新增 lifecycle 值、approval／review verdict 或 registry schema。

### Gate 適用條件與順序
```

  **目標值**（恢復規範邊界；去除原句之自指行號 `:126` 與時點證據「且該行未變」）:

```text
本節不新增 lifecycle 值、approval／review verdict 或 registry schema。
- 一致性依據：Transition Table 節首引言之限制字面範圍為其「本節」；本節為獨立節並受上述不外溢條件約束，將該限制改為全檔通用屬另案修訂。

### Gate 適用條件與順序
```

- [x] 完成（2026-10-19 00:5x：R1–R3 已以 `revb_apply.py` 套用；讀回三處命中，見 AI-7 實測）

### AI-7: AC 7b 全量逐行分類、更正宣稱、重跑不變量（不改他檔）
- **檔案**: 本票票檔；`.claude/rules/ssot-governance.md`（唯讀驗證）
- **動作**: ①對 `881c4c9e` 全部 16 條刪除／改寫行（以 `git show 6aae9f10:.claude/rules/ssot-governance.md` 為舊檔基準，**全量重判**，含 hunk 181「一致性依據」與 hunk 72「事件支撐」，不只 E9／E15）逐行分類為「歷史/證據｜授權標記｜現況事實｜規範邊界」；「現況事實」「規範邊界」兩類每列指出新文本承接行號，並以 `grep -nF` 驗證該承接短語於 live 檔**唯一命中**；無承接者即補回。②更正 Rev A 宣稱（證據分級「Rev B 更正」、AI-5 ④、收尾實錄、registry checkpoint 摘要）。③重跑 AI-4 全部不變量並記實測。④外部行號引用重新盤點（`:11`、Advisor 契約句行號）。
- **分類結果（Rev B 實測，16 列；承接行號為 Rev B 後 live 檔行號）**:

| 舊行 | 舊文開頭 | 分類 | 承接（新行號） | 備註 |
| --- | --- | --- | --- | --- |
| 2 | description: PROGRESS.md /… | 歷史/證據 | —（無需承接） | frontmatter 舊 description 過時；新 description 即 L2 自身 |
| 21 | 觸發來源：Akira 2026-07-30 對話裁決… | 授權標記＋歷史/證據 | 授權標記 → `:21`；規則本體 → `:11` | 原句為立法事由敘事；規則本體在 L11 原樣保留 |
| 72 | **落差清單分類**（依現況快照產出，**MUST … | 歷史/證據 | —（無需承接） | 事件支撐（PLT 2026-08-30／APP 2026-09-05 實測）；落差清單分類規則本體同行保留 |
| 100 | （Akira 2026-09-09 裁決；事件：Tr… | 授權標記＋歷史/證據 | 授權標記 → `:100` | Traycer 事件經過刪除；§Traycer 產出物之票務地位 1–4 點規則本體不變 |
| 113 | - **APP 端專屬約束**：敘事註記可用 `re… | 歷史/證據 | 規則本體 → `:113` | 2026-09-02 實測括號；「靜默丟棄」規則與 read-back 義務同行保留 |
| 175 | > 契約狀態：本節為契約文字。helper 與 le… | 現況事實＋規範邊界 | 規範邊界：呼叫路徑屬模型契約、不被機械阻擋、禁宣稱完整 Gate／hook 涵蓋 → `:175`；現況事實：Stop guard 已註冊於 settings.json 之 Stop → `:247`；現況事實：升報通知對齊尚未實作 → `:247`；現況事實：Advisor 驗證限制照實保留 → `:247`；現況事實：入口 check 已接入 qa／commit／sv／closeout／done，resume 讀 status → `:225` | 原一句合併四類內容；ADV-GATE-02／03 票號標籤屬歷史/證據 |
| 181 | - 一致性依據：Transition Table 節… | 規範邊界 | 規範邊界：Transition Table 限制範圍為其「本節」，改為全檔通用屬另案修訂 → `:181` | Rev B 恢復（去除自指 `:126` 與時點證據「該行未變」）；881c4c9e 原刪除為缺陷 |
| 224 | - 正常入口最小落點：入口 check 呼叫已接入（… | 現況事實＋歷史/證據 | 現況事實：入口 check 呼叫已接入 → `:225` | 刪除句為「入口 check 呼叫已接入（ADV-GATE-03）。」；事實由覆蓋矩陣現況句承接，票號標籤屬歷史 |
| 225 | - 覆蓋矩陣四軸分開記錄：check 程式結果（確定… | 歷史/證據 | 事實保留 → `:225` | 僅刪票號標籤 ADV-GATE-03／04；覆蓋缺口揭露原樣保留 |
| 247 | - 現況：helper 與 ledger 已存在（A… | 現況事實 | 現況事實：Stop guard 已註冊 → `:247` | Rev B 恢復（881c4c9e 原降級為「已落地」為缺陷）；settings.json:150 實測仍註冊 |
| 260 | **立法事由**：`ACAGOV-RUNTIME-T… | 歷史/證據 | 授權標記保留於同項標題 → `:256` | ACAGOV-RUNTIME-TABLE-01 立法事由敘事；雙軌合法之規則本體在第 5 點保留 |
| 273 | 觸發來源：`RGR-INTERACTIVE-CLOS… | 授權標記＋歷史/證據 | 授權標記 → `:272`；規則本體：軌 3 回報前核對 → `:268` | 事件經過與軌 3 追加理由屬歷史；軌 3 規範本體在第 3 點 |
| 289 | - 觸發來源：INFRA-CLOSURE-SOP-0… | 授權標記＋歷史/證據 | 授權標記 → `:288` | 實例缺口與票號為歷史 |
| 290 | - **FORBIDDEN 延後項免開票紀律**：票… | 授權標記＋歷史/證據 | 授權標記 → `:289` | 引文與票號為歷史；免開票紀律本體同行保留 |
| 291 | - **結構歧義停損律**(2026-08-01 C… | 歷史/證據 | 規則本體 → `:290` | 「觸發來源:MEO v1 十輪修補無停損」；停損律本體保留 |
| 300 | **翻盤紀錄**：2026-08-27 治理減量裁決… | 現況事實＋規範邊界 | 現況事實：APP 採 PLT 五段語義、平台為唯一語義權威 → `:299`；規範邊界：最小必要 consumer 實作、非重寫 enum／移植 script → `:299` | Rev B 恢復邊界句（881c4c9e 原刪除為缺陷）；未升級為 MUST NOT（另案 PENDING_DECISION） |

- **結論**：16 列中「現況事實／規範邊界」類共 5 列（舊 :175、:181、:224、:247、:300），其中 **3 列為 `881c4c9e` 缺陷並已補回**（舊 :181 → 現 `:181`；舊 :247 → 現 `:247`；舊 :300 → 現 `:299`），另 2 列（舊 :175、:224）經核對事實與邊界已有承接；其餘 11 列為歷史/證據或授權標記，授權標記已保留。
- **Rev B 不變量實測（AI-6 後）**: ①file sha8 `7a426261`；②標題集合 `f3d6a5b1`；③新增節 `9e648bca`；④L11 sha8 `a440e7ca`；⑤CR 位元組 0；⑥Rev B 增量 numstat `3 2`、hunk 起始（舊檔）{180,246,298}，累計（對 `6aae9f10`）numstat `15 16`；⑦27,210 字元／299 行（累計 −1,404，−4.91%；−1 行）；⑧關鍵詞差集：累計 `before 126 after 125`（GONE 三筆／ADDED 兩筆，與 Rev A 同）、Rev B 增量 `before 125 after 125`（GONE／ADDED 皆空）。
- **外部行號引用（Rev B 重新盤點）**: `.claude/PROGRESS.md` 提及 `ssot-governance.md:11` —— 未位移；`plans/roadmap/autonomous-engineering-gate-outcomes.md:36` 引用 `ssot-governance.md:211` —— **Rev B 後仍指向 Advisor 契約句（`:211`），Rev A 之「已位移至 `:210`」撤回**。另：`.claude/PROGRESS.md` 內我於 Rev A checkpoint 寫入之兩處「`:211` 應改 `:210`」文字（Dashboard 本票列與 Next Actions #30 增補）**現已失實**，PROGRESS.md 列 FORBIDDEN，屬 DEFERRED-TO-DOCSYNC，於下一輪 `/compact` 更正。
- [x] 完成（見上列實測與「收尾實錄（Rev B）」）

## Hard Constraints
1. 只准做 AI-1～AI-3 列舉的 E0–E15 精確字串替換；**不得**改寫、合併、重排或壓縮任何其他字元，**不得**新增規則句。Rev B 增列 AI-6 之 R1–R3（皆為非空替換）；刪除型替換禁用 Edit 空 `new_string`（會吞行尾換行），改用精確字串替換腳本。
2. **不得**更動任何 `#`／`##`／`###` 標題、「證據缺口與研究授權」節（標題起 7 行）與 L11。
3. **不得**新增任何檔案；被移除文字不另存新檔，可回復點為 `6aae9f10:.claude/rules/ssot-governance.md`（git 歷史）。
4. **不得**修改任何 FORBIDDEN 檔，包含引用本檔行號的 `plans/roadmap/autonomous-engineering-gate-outcomes.md` 與 `.claude/PROGRESS.md`。
5. 授權標記只保留「Akira YYYY-MM-DD 裁決」形式，**不得**保留或新增事件經過敘述。
6. 收尾 **MUST NOT** 宣稱行為效果已驗證；`UNVERIFIED` 項如實揭露。
7. commit 前綴使用 `[INFRA-RULESLIM-01]`（單票）。`ssot-governance.md` §Governance Artifact 清單表對 `.claude/rules/*.md` 列 `[GOVERNANCE-SYNC]`，係針對 `/compact` Auto Commit 之無票治理同步；有票的規則檔編輯沿用票號前綴，先例：`ADV-GATE-01` 之 `2441aeba` 編輯本檔即用 `[ADV-GATE-01]`。票檔搬 `deliver/`、冊面轉態依 `ssot-governance.md` §Session Persistence 邊界第 5 點雙軌擇一，**不混入他票 artifact**。
8. 本票省略 `/qa`、`/sv`、cross_review 之依據僅為 2026-10-02 風險分級（docs／治理文本、僅 `.md`、可 `git revert`）；若執行中發現需改動非 `.md` 檔或擴大 ALLOWED，**立即停止**，不得沿用該省略。
9. Rev B：R1 邊界句 MUST 與使用者指定原文逐字一致，R1／R2／R3 **不得升級為 MUST NOT**（另案 PENDING_DECISION）；R2 寫入前 MUST 先讀 `.claude/settings.json` 確認註冊仍在。
10. Rev B：`20261017-projman-plan / T03 / 治理條款呼叫與 ssot-governance.md:11 一行修正` 與 `20261016-cli-advisor-plan / T02 / Advisor 契約放寬` 兩個 plan-local 工作項 **須待本 Rev B commit 後才能讀 live 檔**（不是正式 Ticket ID）。

## Error Handling
- AI-0 #1–#4 sha／命中數不符且依 #7 歸因為 `ALREADY_EXECUTED_PENDING_CLOSURE` → 停止回報（直送收束審查，禁重複施作）。
- 任一 `current` 字串於檔內找不到，且不能歸因為他票已等價完成（`CANDIDATE_SUPERSEDED`）→ 停止回報，禁自行推定等價位置。
- 任一 `current` 字串命中數 >1 → 停止回報（錨點不再唯一）。
- AI-4 ②（標題集合）、③（新增節）、④（L11）、⑤（CR 位元組）任一不符 → 停止回報，不自行修補。
- 發現需修改 `.claude/rules/ssot-governance.md` 以外任何檔案才能完成 → 停止回報（Scope 擴張，須升報）。
- 另有他票於同期編輯本檔致衝突 → 停止回報，依 `concurrency.md` §2 串行處理。
- Rev B：`.claude/settings.json` 不再於 `hooks.Stop` 註冊 `gate_stop_guard.py` → 停止回報（R2 不得寫入未成立的註冊事實）。
- Rev B：AC 7b 任一「現況事實／規範邊界」列無承接 → 補回；補回後仍無法唯一命中承接短語 → 停止回報。
- Rev B：R1／R2／R3 任一 `current` 命中數 ≠ 1 → 停止回報，禁自行推定等價位置。
- **[標配]** 本令含 commit 標的：`git check-ignore -v .claude/rules/ssot-governance.md` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律）。開票時已驗證無命中。

## Acceptance Criteria
- [x] **[存在性]** AI-1～AI-3 全部套用後，`sha256sum .claude/rules/ssot-governance.md | cut -c1-8` ＝ `45f7a9c4`（僅在 AI-0 #1 base 為 `ff0d9c1b` 時適用；base 已漂移則本項改記「N/A: 依 AI-0 #7 以現值續作」並以 AC 2–11 為準）。【Rev B：AI-6 後終值 `7a426261`，僅在 base 為 Rev A 結果檔 sha8 `45f7a9c4`／blob `1db12d9a` 時適用】
- [x] **[存在性]** 標題集合 sha8 仍為 `f3d6a5b1`（`grep -E '^#{1,3} ' … | sha256sum | cut -c1-8`）。
- [x] **[存在性]** 「證據缺口與研究授權」節 sha8 仍為 `9e648bca`（`grep -A6 -F '### 證據缺口與研究授權' … | sha256sum | cut -c1-8`）。
- [x] **[存在性]** L11 逐字不變（`sed -n 11p` 之 sha8 與 AI-0 #2 記錄值相同）。
- [x] **[存在性]** `git diff --numstat -- .claude/rules/ssot-governance.md` 為 `14 16`（base 未漂移時）；`git diff -U0` 的所有 hunk 起始（舊檔）行 ⊂ {2,21,72,100,113,175,181,224,225,247,260,273,289,290,291,300}（預跑實際為 13 個 hunk：{2,21,72,100,113,175,181,224,247,260,273,289,300}）。【Rev B：對 `881c4c9e` 內容之增量 numstat `3 2`、hunk 起始 {180,246,298}；對 `6aae9f10` 累計 numstat `15 16`】
- [x] **[存在性]** 字元數 28,614→27,058（−1,556，−5.44%）、行數 300→298 如實記錄；若與預跑不同，註明原因。**不以 parent 的 15–25% 或 1,800 字元為通過門檻**（見證據分級「與 parent 的衝突處置」）。【Rev B 終值：28,614→27,210（累計 −1,404，−4.91%）、300→299 行】
- [x] **[存在性]** 含 MUST／MUST NOT／禁／不得／SHOULD 之句：以 AI-4 ⑧ 的命令執行，GONE 恰為證據分級③所列三處（E4／E5／E10 範圍內）、ADDED 恰為 E4 後版本與 E5 新句兩筆，其餘逐句不變（base 未漂移時 `before 126 after 125`）。【Rev B 更正：本項僅證逐字關鍵詞句，**不足以證明語義承接**（Rev A 三處缺陷皆不含關鍵詞）；Rev B 增量差集為空（`before 125 after 125`）；語意核對以 AC 7b 為準】
- [x] **[存在性]** **AC 7b（Rev B 新增）**：對 `881c4c9e` 全部 16 條刪除／改寫行（舊檔行號 2,21,72,100,113,175,181,224,225,247,260,273,289,290,291,300）逐行分類為「歷史/證據｜授權標記｜現況事實｜規範邊界」；「現況事實」「規範邊界」兩類每列 MUST 指出新文本承接行號，且該承接短語以 `grep -nF` 於 live 檔唯一命中；無承接者即視為缺陷並補回。分類表見 AI-7；Rev B 實測缺陷 3 處（舊 :181、:247、:300）皆已補回，且含 hunk 181 與 hunk 72 全量重判。
- [x] **[存在性]** **AC 7c（Rev B 新增）**：R1 邊界句與使用者指定原文逐字一致（`APP 採取最小必要之 consumer 實作，而非重寫平台 enum 或移植全套 script`）且未升級為 MUST NOT；R2 寫入前已讀 `.claude/settings.json`，確認 `gate_stop_guard.py` 仍註冊於 `hooks.Stop`（實測 `settings.json:150`）。
- [x] **[存在性]** 檔案 CR 位元組數為 0（LF），檔尾換行保留。
- [x] **[存在性]** 無程式解析者：AI-0 #6 之 grep 於修改後重跑仍零命中。
- [x] **[存在性]** 外部行號引用盤點完成並寫入收尾實錄：`:11` 未位移；`:211`【Rev A：位移至 `:210`；**Rev B：R3 恢復一行後回到 `:211`，Rev A 之位移交代撤回**】；兩個引用檔未被修改（`git diff --stat` 不含它們）。
- [x] **[存在性]** 移除前版本可回復：`git cat-file -e 6aae9f10:.claude/rules/ssot-governance.md` exit 0，且收尾實錄記錄 `6aae9f10` 與 `e95b14c5`。
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

## 收尾實錄

**結論**：代碼閉環（commit `881c4c9e`，單檔 `+14／-16`）；本票依風險分級省略 `/qa`、`/sv`、cross_review，並依票面 Rigor Gate Waiver 未跑 RGR；以 AC 的確定性靜態檢查取代。**效果面 `UNVERIFIED`**（見下）。【Rev B 更正：Rev A 所指靜態檢查未涵蓋語義承接，經 Desktop 審查退回，見「收尾實錄（Rev B）」】

**基準與可回復**：BASE_COMMIT／DIFF_REF＝`e95b14c5`；移除前版本＝`6aae9f10:.claude/rules/ssot-governance.md`（`git cat-file -e` exit 0）；本票 commit＝`881c4c9e`。

**AC 佐證（最終套用後實測）**：AC1 file sha8 `45f7a9c4`；AC2 標題集合 `f3d6a5b1`；AC3 新增節 `9e648bca`；AC4 L11 sha8 `a440e7ca` 與 AI-0 同；AC5 numstat `14 16`、13 個 hunk 起始行 {2,21,72,100,113,175,181,224,247,260,273,289,300} ⊂ 16 個編輯行號；AC6 28,614→27,058 字元（−1,556，−5.44%）、300→298 行（低於 parent 1,800 字元門檻，原因見證據分級「與 parent 的衝突處置」，不補刪規則句）；【Rev B 更正：AC1／5／6／7／10 之 Rev A 值為歷史值，終值見「收尾實錄（Rev B）」】AC7 `before 126 after 125`、GONE 三筆／ADDED 兩筆皆為預期；AC8 CR 位元組 0、檔尾換行保留；AC9 AI-0 #6 重跑 0 命中；AC10 見下；AC11 `git cat-file -e 6aae9f10:...` OK；`git diff --stat` 僅涉 ALLOWED 單檔。`assembleDebug` N/A（docs preset）。

**外部行號引用清單（AI-5，兩個引用檔皆在 FORBIDDEN，本票未改，交所有者）**：
1. `.claude/PROGRESS.md:13` 提及 `ssot-governance.md:11` —— **未位移**（L11 逐字相同，sha8 `a440e7ca`），仍指向 Governance Artifact 清單引言句。
2. `plans/roadmap/autonomous-engineering-gate-outcomes.md:36` 引用 `ssot-governance.md:211`（Advisor 契約句）—— **已位移**：因 E6 刪除一行，該句現位於 **`:210`**（`:211` 現為 `- PPSS 證據…`）。待其所有者（`projman align`／下一輪 `/compact`）改指 `:210` 或改指 `§Advisor 與 PPSS` 標題。【**Rev B 撤回**：R3 恢復一行後該句回到 `:211`，本項交代作廢】

**偏差與失誤（如實）**：
1. 首次以 Edit 工具套用時，空字串刪除型替換連同行尾換行一併吞掉，致行合併（sha8 `dc9213fd`、294 行、numstat `15 21`）；AI-4 驗證當場抓到，僅以 `git checkout --` 還原本票自己的未提交編輯，改用與預跑同一份精確字串替換腳本一次套用，結果位元組與預跑一致。腳本曾在 repo 內多寫出一個未追蹤的 `ssot-governance.md.manifest.json`，已刪除、從未進 commit。細節見 AI-2 實作註記。
2. 開工時 `registry_set_status` 首次呼叫誤用參數名 `status`（被靜默忽略，僅 registry revision 845→846 無效增量），已以 `lifecycle` 重做（→847），並記於冊面 note。
3. 票面註冊後曾補強三處（AC 7 命令、commit 前綴先例、Rigor Gate Waiver），均在開工前完成，未改 Selected item 與 Goal。

**UNVERIFIED／未做**：①精簡後模型是否因此改變遵循行為——無確定性證明方法，不宣稱「行為不變已驗證」，僅宣稱「diff 範圍受限於列舉片段、規則句差集僅三處預期變動」【**Rev B 更正：此宣稱不成立**（見證據分級「Rev B 更正」）；Rev B 起僅宣稱「diff 範圍受限於列舉片段；規範性內容之語意承接以 AC 7b 逐行分類為準」】；②實際 token 節省量（無 `count_tokens` 實測，以字元數為度量）；③未跑 `/qa`、`/sv`、cross_review、RGR（依風險分級與 Waiver 省略）。

**DEFERRED-TO-DOCSYNC（不另開票）**：`.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` Dashboard 同步與 Done 標記於下一輪 `/compact` 處理；上列第 2 項過期行號引用交其所有者。

## 收尾實錄（Rev B）

**結論**：Rev B 代碼閉環（commit `9c80995b`，單檔 `+3／-2`）。恢復 `881c4c9e` 三處語意退化並完成 AC 7b 全量逐行分類；本票仍依風險分級省略 `/qa`、`/sv`、cross_review，並依票面 Rigor Gate Waiver 未跑 RGR。**效果面 `UNVERIFIED`**。本節取代 Rev A「收尾實錄」中與之矛盾之宣稱。

**基準與可回復**：Rev B 開工 HEAD／`DIFF_REF`／`BASE_COMMIT`＝`7c9d983a`；Rev A commit＝`881c4c9e`（票檔 `69848801`）；Rev B commit＝`9c80995b`；移除前版本＝`6aae9f10:.claude/rules/ssot-governance.md`（`git cat-file -e` exit 0）。

**AC 佐證（Rev B 終值，commit 後實測）**：AC1 file sha8 `7a426261`；AC2 標題集合 `f3d6a5b1`；AC3 新增節 `9e648bca`；AC4 L11 sha8 `a440e7ca`；AC5 Rev B 增量 numstat `3 2`、hunk 起始 {180,246,298}，對 `6aae9f10` 累計 `15 16`；AC6 28,614→27,210 字元（累計 **−1,404，−4.91%**）、300→299 行；AC7 累計 `before 126 after 125`（GONE 三筆／ADDED 兩筆）、Rev B 增量 `125→125`（空差集）；**AC 7b** 16 列全量分類，缺陷 3 處（舊 :181、:247、:300）皆已補回，承接短語於 live 檔唯一命中；**AC 7c** R1 邊界句與指定原文逐字一致且未升級為 MUST NOT、R2 寫入前已確認 `settings.json:150` 註冊仍在；AC8 CR 位元組 0；AC9 AI-0 #6 重跑 0 命中；AC10 見下；AC11 OK。`assembleDebug` N/A（docs preset）；`git diff --stat` 僅涉 ALLOWED 單檔（本節票檔屬收尾 artifact）。

**更正清單（Rev A 宣稱 → Rev B）**：①證據分級③「差集僅三處預期、規則句不變」→ 已於證據分級新增「Rev B 更正」並於 Rev A 收尾實錄就地標註；②AI-5 ④「規則句未被移除」→ 改為「規範性內容之語意承接以 AC 7b 為準」；③Rev A 收尾實錄結論／AC 佐證／UNVERIFIED／外部行號各處就地加註【Rev B 更正／撤回】；④H1 標題與冊面 `title` 去除「規則句不變」；⑤registry checkpoint 摘要於本次收尾以 Rev B 終值重寫。

**外部行號引用（Rev B 重新盤點）**：`.claude/PROGRESS.md` 提及 `ssot-governance.md:11` —— 未位移；`plans/roadmap/autonomous-engineering-gate-outcomes.md:36` 引用 `ssot-governance.md:211` —— Rev B 後 `:211` 仍為 Advisor 契約句，**Rev A「已位移至 `:210`」之交代撤回**，該引用檔無需因本票修改。

**偏差與失誤（Rev B，如實）**：
1. **Rev A 的 AC 7 設計缺陷（O4＝SPEC_DEFECT）**：以「含關鍵詞句的逐字差集」當作語意不變的證據，無法偵測不含關鍵詞的承重句被刪／降級；Rev A 的三處缺陷（舊 :181、:247、:300）因此漏網，經 Desktop 審查退回。Rev B 補 AC 7b 逐行分類並全量重判。
2. 開工時曾把 `ANCHOR_PROBED` 填成非實測時間（`09:10`；系統實際為 `00:38`），發現後立即更正並重新啟用 conf；該欄為資訊性診斷值，未影響任何判定。
3. 我在 Rev A 的 `/compact` checkpoint 把「`:211` 應改 `:210`」寫進 `.claude/PROGRESS.md` 兩處；該判斷隨 R3 恢復而失實，PROGRESS.md 列 FORBIDDEN，列為下方 DEFERRED-TO-DOCSYNC。

**UNVERIFIED／未做**：①精簡後模型是否因此改變遵循行為——無確定性證明方法，不宣稱「行為不變已驗證」；僅宣稱「diff 範圍受限於列舉片段；規範性內容之語意承接以 AC 7b 逐行分類為準」；②實際 token 節省量（以字元數為度量）；③未跑 `/qa`、`/sv`、cross_review、RGR。

**DEFERRED-TO-DOCSYNC（不另開票）**：`.claude/PROGRESS.md` 需於下一輪 `/compact` 更正三處——Dashboard 本票列（`規則關鍵詞句 126→125（差集三處預期）`、`28,614→27,058／−1,556／−5.44%／300→298` 改 Rev B 終值 `27,210／−1,404／−4.91%／299`、「`:211` 現應為 `:210`」撤回）、Last Updated 同句、Next Actions #30 增補同句；`projman` T03 與 `cli-advisor` T02 兩個 plan-local 工作項（見 Hard Constraint 10）現可讀 live 檔。
