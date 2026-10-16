# ADV-RULE4C-01: 對齊 CLAUDE.md Rule 4c 與 Transition Table QA FAIL 列於現役 Autonomous Engineering Gate，消除「自主續作」與「等待人工」兩套語意

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T01`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261012-rule4c-plan.md`（invocation 之 Source 路徑字串於傳遞中損毀為 `D:DevSourcesAndroidProjSmartCameraApp1.tmp\ticket-author61012-rule4c-plan.md`；`.tmp/ticket-author/` 下僅此一檔符合 `rule4c-plan`，唯一可解，已核對檔頭）；Parent artifact status＝`Fixture: false`、`Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`；plan-local item＝`T01 — Rule 4c 單句取代`；Parent read timestamp＝2026-10-12 13:40（HEAD `470bf098`）。`.tmp` 路徑僅為溯源；本票自含。模板＝`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md` v3.11（文件日期 2026-05-29；有效修訂 v3.11 於 2026-09-14；全檔 322 行已於本 session 完整讀取，檔自 commit `6ee2f94d`〔2026-09-15〕後無變動）。

**Selected item 原文（plan T01 goal）**：Rule 4c 整行逐字等於 plan D1；其餘位元組不變。plan 原 `estimated_scope`＝`.claude/CLAUDE.md` 單行。

**已裁決決策（Akira 2026-10-12）**：
- **D1（APPROVED，Rule 4c 新句逐字）**：`c) /qa /sv /compact /dream 是驗收工具，本身不構成新增實作授權；無 Ticket 授權之問題 → 回報，不自行修復。本條**不適用於**當前已啟動 Ticket 之 ALLOWED 內可自修問題：該類問題依 `.claude/rules/ssot-governance.md` §Autonomous Engineering Gate 修復、局部驗證並揭露；確需新增權限、經取證與 Advisor 確認須擴 scope，或涉及不可逆操作者，依該節升報。`
- **D2（APPROVED，限定情境重放，n=1，弱證據）**：①已授權、scope 內可自修 QA FAIL→修復並驗證，不要求例行確認；②疑似 scope→先本地取證／Advisor，不直接升報；③真正新增權限或不可逆操作→不自行放行。重放讀修改後 Rule 4c 與其回指之現役 Gate，不提供預期答案，保存原文，不宣稱 production 行為已被證明。
- **D3（APPROVED）**：topic `Advisor`、前綴 `ADV`。
- **D4（APPROVED）**：`External-review decision: WAIVED`，Akira，2026-10-12（見下）。
- **D5（APPROVED，開票指令「Decisions 追加」）**：修改方式以**替換或刪除**既有停止條款為主，不以追加例外為預設。
- **D6（APPROVED）**：Transition Table「QA FAIL」列**直接改寫判定與終點**（可自修→依 Gate 續作；不可自修／人工邊界／停損→停止），**不保留原句加回指**。
- **D7（APPROVED）**：刪除 `ssot-governance.md` `### QA FAIL 優先序` 之過渡句（「過渡期：T02 對齊前…」）。
- **D8（AC 要求）**：完成後任一情境不得同時存在「自主續作」與「等待人工」兩套語意；過渡句字面零命中。

**來源／live 衝突處置（可見）**：①plan 與開票指令首段約束「ALLOWED 僅 `.claude/CLAUDE.md`、不改 rules」；但 D6／D7 要求改寫 `ssot-governance.md`。依 Source precedence（產品語義：明確的當前使用者決策優先），**後附之 D5–D8 為較新且較具體之 APPROVED 裁決**，故 ALLOWED 擴為 `.claude/CLAUDE.md` 與 `.claude/rules/ssot-governance.md`；「不改 rules」解讀為不改**其他** rules 檔（`cross-review.md`、`kairos-lessons.md` 等）。②D6 要求移除「Table 加回指」形式：`ADV-GATE-01` 所加之回指行（`> QA FAIL 列之適用邊界及 Gate 前置見 §Autonomous Engineering Gate。`）於列改寫後成為重複，併入 D5「替換／刪除」處置（見 AI-2）。③`2026-09-16 收窄律`僅有 vault 轉述，不視為已核實 repo 條文，不另開治理票。④**已知殘餘（FORBIDDEN 範圍，不處置）**：D7 刪除過渡句後，`uc/SKILL.md` 規則 9「QA FAIL…**MUST** 停止且不得進入 SV」與 `qa/SKILL.md`「Auto-Chain 停止條件」之字面「停止」不再有檔內解讀句；新 Table 列以「QA PASS 前不進入 /sv」明文其語意。兩 SKILL 檔之措辭對齊屬 autogate plan T02，本票不動、不占用 T02、不改其 Ticket Map。⑤（RGR 預審補入）回指行移除後，Gate 節不外溢條件②之括註「Table 節對本節之回指一行除外」成為過時同義句，以 AI-4 同步（單處括註替換，非追加例外）。

**證據分級**：①**CONFIRMED（2026-10-12 13:40 live 直讀）**：`.claude/CLAUDE.md` 129 行、`i/lf w/lf`、blob `4b32d29a`、Rule 4c 唯一一行以 `c) /qa /sv /compact /dream` 起首（L72）；`.claude/rules/ssot-governance.md` 295 行、`i/lf w/lf`、blob `710267e2`；轉移表 QA FAIL 列現值 `| QA FAIL | \`MUST STOP\`，回報失敗項 | 自行修復後靜默重跑並宣告 PASS |`（L153）；回指行位於 L162（前後各一空行）；過渡句位於 `### QA FAIL 優先序` 第四個項目（L228），`T02 對齊前` 於全 repo `.claude/` 僅此一處；`## Autonomous Engineering Gate` 與 `### QA FAIL 優先序` 標題存在；兩檔未被 `.gitignore` 排除；工作樹 clean；冊面（main＋archive）及 `Pending/deliver/void` 無同義票；codex worktree 為 detached 且與兩目標檔無重疊。②**HYPOTHESIS**：改寫後三處文字（Rule 4c、Table 列、Gate 節）無雙重語意（以 AC-5／AC-6 重放檢驗；n=1 弱證據）。

**External-review decision**: WAIVED — 裁決者 Akira，2026-10-12；理由：單句／單列、可逆（`git revert`）之授權邊界對齊，不新增外部能力、不推定外部事實；本地第一方原文與 plan 已足。比照 `ADV-REPORTFIX-01`／`ADV-GATE-01` 票面先例。**豁免僅限本票，不傳遞至 T03／T04；不另啟外部研究。**

**簽發日期**: 2026-10-12
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ADV-GATE-01（Gate 節與 QA FAIL 優先序之來源，已 CLOSED）——僅作字面來源，錨點皆為 live 直查
- `coordination_dependency`: 無（autogate plan T02 尚未開票；T02 若改 `CLAUDE.md` 或上述 SKILL 檔，須串行於本票之後；T02 條款處置清單中之 `CLAUDE.md` Rule 4c 項由本票承接，T02 開票時須自其清單移除該項）

## Verify Config
TICKET_ID=ADV-RULE4C-01
CREATED=2026-10-12
PRESET=docs
ALLOWED=.claude/CLAUDE.md,.claude/rules/ssot-governance.md
FORBIDDEN=.claude/hooks/**,.claude/settings.json,.claude/settings.local.json,.claude/agents/**,.claude/scripts/**,.claude/skills/**,.claude/rules/concurrency.md,.claude/rules/critical-files.md,.claude/rules/cross-review.md,.claude/rules/decommission.md,.claude/rules/engineering-discipline.md,.claude/rules/kairos-lessons.md,.claude/rules/mcp-service-safety.md,.claude/rules/phase0-lessons.md,.claude/rules/state-machine.md,.claude/rules/ticket_pattern.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,app/**,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-12 13:40
BASE_COMMIT=470bf098
TARGET_BLOB=.claude/CLAUDE.md:4b32d29a;.claude/rules/ssot-governance.md:710267e2
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

> PRESET=docs（零 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。`MAX_LINES=N/A`：授權邊界／停損類契約文字，規模不可先驗估算（TPL v3.5 R3）；非 execution gate。`INTRODUCED_CAPABILITY=NONE`、`DEPENDENCY_DELTA=NONE` 為顯式填寫（無新導入能力或依賴）；外審仍依上列 WAIVED 記錄。`TARGET_BLOB` 由 `git hash-object` 直取。**DEFERRED-TO-DOCSYNC（不開票）**：`.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` 命中本票 FORBIDDEN，同步與 Done 標記依 §FORBIDDEN 延後項免開票紀律，於下一輪 `/compact` 處理；票檔 Pending→`deliver/` 搬移與 registry 轉態屬 closeout 通道，不屬 ALLOWED。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）＋取號紀錄
- **檔案**: `.claude/CLAUDE.md`、`.claude/rules/ssot-governance.md`
- **動作**: 逐字確認（行號僅供定位；2026-10-12 13:40 live 取樣，HEAD `470bf098`，工作樹 clean）：
  - blob `4b32d29a`／`710267e2`；`git ls-files --eol` 皆 `i/lf w/lf`（**不得用 `grep -c $'\r'` 判 EOL——本工具環境不可靠**）；`CLAUDE.md` 129 行。
  - 唯一命中＝1：`CLAUDE.md` 之 `^c) /qa /sv /compact /dream 是驗收工具，不是實作授權。發現問題 → 回報，不自行修復。$`；`ssot-governance.md` 之 `^| QA FAIL | `MUST STOP`，回報失敗項 | 自行修復後靜默重跑並宣告 PASS |$`、`^> QA FAIL 列之適用邊界及 Gate 前置見 §Autonomous Engineering Gate。$`、`^- 過渡期：T02 對齊前，`、`^## Autonomous Engineering Gate$`、`^### QA FAIL 優先序$`、`^### Traycer 語義$`。
  - 取號紀錄（TPL 律 A，全冊機械取號）：對 `TICKET_REGISTRY.json`、`TICKET_REGISTRY_ARCHIVE.json`、`Pending/`、`deliver/`、`void/`、`reports/` 以 `ADV-` 樣式 grep，既有 ID＝`ADV-AGENTS-CR-03`、`ADV-AGENTS-CR-05`、`ADV-CR-R0-PRECLEAR-01`（及 `-I1`／`-I2`）、`ADV-GATE-01`、`ADV-REPORTFIX-01`；`ADV-RULE4C` 於冊面、票檔目錄、`git log --all --grep` 皆零命中；新 NAME 段 `RULE4C` 無碰撞，序號 `01`。前綴 `ADV` 已登記於 `ticket_pattern.md` Family Provenance（2026-08-13）。
- **任一錨點不符 → 依 TPL v3.11 基線漂移三分流處置**（僅觸及 `.claude/` 可逆治理面）：先跑 `git diff --name-only 470bf098..HEAD -- <ALLOWED 兩檔>`；為空→行號／措辭漂移，只重讀補證續作；非空→看前綴：含本票 ID→`ALREADY_EXECUTED_PENDING_CLOSURE`（停止）；他票 ID→`CANDIDATE_SUPERSEDED`（記錄後以現值續作）；無前綴→`BLOCKED_UNATTRIBUTED_STATE`（治理面降為記錄後續作）。禁推定等價位置。
- [x] 完成（依據：blob 4b32d29a／710267e2 與 TARGET_BLOB 一致、HEAD 470bf098 未漂移、工作樹 clean；六個錨點皆唯一命中；取號紀錄已於開票時完成）

### AI-1: `.claude/CLAUDE.md` Rule 4c 整行替換（行數不變）
- **檔案**: `.claude/CLAUDE.md`
- **現值**: `c) /qa /sv /compact /dream 是驗收工具，不是實作授權。發現問題 → 回報，不自行修復。`（唯一）
- **目標值**（逐字，單行）: `c) /qa /sv /compact /dream 是驗收工具，本身不構成新增實作授權；無 Ticket 授權之問題 → 回報，不自行修復。本條**不適用於**當前已啟動 Ticket 之 ALLOWED 內可自修問題：該類問題依 `.claude/rules/ssot-governance.md` §Autonomous Engineering Gate 修復、局部驗證並揭露；確需新增權限、經取證與 Advisor 確認須擴 scope，或涉及不可逆操作者，依該節升報。`
- [x] 完成（依據：Rule 4c 整行逐字替換，129 行不變，`i/lf w/lf`）

### AI-2: `.claude/rules/ssot-governance.md` Transition Table「QA FAIL」列直接改寫；移除已重複之回指行
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 列 `| QA FAIL | \`MUST STOP\`，回報失敗項 | 自行修復後靜默重跑並宣告 PASS |`；其後 L162 之回指行 `> QA FAIL 列之適用邊界及 Gate 前置見 §Autonomous Engineering Gate。`（含其前後各一空行中的一個）。
- **目標值**（下列行內 `\``為 Markdown 轉義，實際寫入字元為單一反引號 `` ` ``，不得寫入反斜線）:
  - 該列改為（逐字；第三欄 `MUST NOT` 儲存格**位元組不變**）：`| QA FAIL | 可自修且在既有授權內：\`MUST AUTO-CHAIN\` 依 §Autonomous Engineering Gate 修復、局部驗證並揭露後重跑 \`/qa\`（\`/qa\` PASS 前不進入 \`/sv\`）；不可自修、人工邊界或停損成立：\`MUST STOP\`，回報失敗項 | 自行修復後靜默重跑並宣告 PASS |`
  - 刪除回指行 `> QA FAIL 列之適用邊界及 Gate 前置見 §Autonomous Engineering Gate。` 及與其相鄰之一個空行，使 Table 末列與 `### Traycer 語義` 之間恰留一個空行。
  - 該列不得含 Gate 詞彙之值（`HUMAN_REQUIRED|EXHAUSTED|NO_AUTHORIZED_STRATEGY|NO_PROGRESS|SOURCE_READ|RETRIEVED|missed_check|step_limit|strategy_scan`）；控制詞集合仍 ⊆ 四值。
- [x] 完成（依據：QA FAIL 列改寫、第三欄位元組不變；回指行及相鄰空行已刪；Table 末列與 `### Traycer 語義` 間恰一空行）

### AI-3: 刪除 `### QA FAIL 優先序` 過渡句
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**（行內 `\``為 Markdown 轉義，實際字元為單一反引號）: 項目 `- 過渡期：T02 對齊前，\`qa/SKILL.md\`「Auto-Chain 停止條件」與 \`uc/SKILL.md\` 規則 8 對 QA FAIL 之「停止」解讀為不進入 /sv，不排除依本節自修後重跑 /qa。`（唯一）
- **目標值**: 整個項目刪除；同節其餘四項（可自修優先、疑似 scope 不得直接停止、`MUST NOT` 非靜默、Table 其他 `MUST STOP` 列語意不變）位元組不變。
- [x] 完成（依據：過渡句項目已刪，同節其餘四項位元組不變）

### AI-4: `.claude/rules/ssot-governance.md` Gate 節「詞彙域與不外溢條件」之回指例外句同步
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 不外溢條件②內之括註 `（Table 節對本節之回指一行除外）`（唯一）——其所指之「回指一行」即 AI-2 移除之回指行。
- **目標值**: 該括註改為 `（Table 節對本節之回指除外）`；同行其餘位元組不變。
- [x] 完成（依據：括註改為「Table 節對本節之回指除外」，同行其餘位元組不變）

### AI-5: 一致性自查（寫入後、提交前）
- **動作**: 執行 AC 之全部確定性檢查；以兩個 fresh subagent 完成 AC-5／AC-6 重放；`/mdlint .claude/CLAUDE.md` 與 `/mdlint .claude/rules/ssot-governance.md` 結果記入收尾實錄。任一 `[存在性]` 檢查失敗→修正後重跑**完整** AC 集合。
- [x] 完成（依據：AC 腳本全 PASS；AC-5／AC-6 重放完成，見收尾實錄）

## Hard Constraints
1. **範圍**：僅 `.claude/CLAUDE.md`（只改 Rule 4c 一行）與 `.claude/rules/ssot-governance.md`（僅 AI-2～AI-4 所列處）。Rule 4a／4b／4d／4e、Boot Sequence、其餘 `CLAUDE.md` 內容；其餘 rules 檔、`skills/`、`hooks/`、`settings*.json`、`scripts/`、PROGRESS、`app/`、registry 一律不碰。
2. **行數／行尾**：`CLAUDE.md` 總行數與行尾不變（`i/lf w/lf`，129 行）；`ssot-governance.md` 淨減 3 行（回指行 1＋相鄰空行 1＋過渡句 1），行尾不變。不得產生 mixed EOL。
3. **替換優先**（D5）：不以追加例外句為手段；所有調整限 AI-1～AI-4 之替換／刪除。
4. **不引用尚不存在者**：新文字不含 helper、hook、`check` 呼叫；不宣稱機械 Gate 已落地。
5. **Table 約束**：控制詞仍恰四值；QA FAIL 列外之 Table 列位元組不變；`MUST NOT` 儲存格位元組不變；不新增 Gate 詞彙之值於 Table 節。
6. **不處置 SKILL 檔措辭**：`qa／uc／sv／compact` SKILL 之「停止」字面對齊屬 autogate plan T02；本票不動、不占用 T02、不改其 Ticket Map。
7. **外審 WAIVED 僅限本票**，不傳遞至 T03／T04，不另啟外部研究。
8. staging 僅顯式路徑；禁 `git add -A`／`.`／`commit -a`。commit 前綴 `[ADV-RULE4C-01]`；本票僅 `.md`。

## Error Handling
- AI-0 任一錨點不符 → 依三分流分類處置；行號漂移、措辭變動 → 只重讀補證，不停止；`ALREADY_EXECUTED_PENDING_CLOSURE` → 停止回報
- 寫入後除 AI-1～AI-4 目標外有任何額外 diff（`git diff -U0` 出現其他 `-`／`+` 行）→ 還原重做；無法還原 → 停止回報
- AC-5／AC-6 重放顯示任一情境同時存在「自主續作」與「等待人工」，或因 `uc`／`qa` SKILL 措辭而無法決定 → **停止回報並附讀取路徑原文，不改 SKILL 檔、不補追加例外句**
- 編輯上述兩檔被 auto mode classifier／權限拒絕 → 停止回報，不繞過、不換工具重試同一被拒動作
- mixed EOL、mdlint 殘留 warning 無法修復 → 停止回報，不 commit
- scope gate 因本票清單以外之 dirty 檔而 FAIL → 停止回報，由 Akira 處置（禁 stash／restore／順手 commit）
- **[標配]** `git check-ignore -v` 對兩目標檔命中 → 停止回報（gitignore 死結律；開票時已驗證未命中）

## Acceptance Criteria
- [x] **[存在性]** Rule 4c 逐字符合 AI-1 目標值；`CLAUDE.md` 相對 `BASE_COMMIT` 的 `git diff -U0` 恰一刪（舊 Rule 4c）一增（新 Rule 4c）；總行數仍 129；`git ls-files --eol` 為 `i/lf w/lf`；Rule 4a／4b／4d／4e、Boot Sequence 位元組不變。（依據：腳本 PASS，diff 一刪一增、129 行、`i/lf w/lf`）
- [x] **[存在性]** `ssot-governance.md` 相對 `BASE_COMMIT` 之 `git diff -U0`：刪除行僅限舊 QA FAIL 列、回指行、其相鄰一空行、過渡句、AI-4 之舊括註行；新增行僅限新 QA FAIL 列（逐字等於 AI-2 目標值）與 AI-4 之新括註行（除括註外與舊行逐位元組相同）；Table 其他列、`MUST NOT` 儲存格、`:126` 引言、不變量 ①–④ 位元組不變；控制詞集合仍 ⊆ 四值；新列不含 Gate 詞彙之值；`i/lf w/lf`。（依據：腳本 PASS，刪除 5 行〔舊列、回指行、空行、舊括註行、過渡句〕、新增 2 行〔新列、新括註行〕；控制詞 ⊆ 四值；新列無 Gate 詞彙）
- [x] **[存在性]** 過渡句字面零命中：`grep -rn "T02 對齊前\|過渡期：\|回指一行" .claude/CLAUDE.md .claude/rules/` 無輸出；`## Autonomous Engineering Gate`（恰 1）、`### QA FAIL 優先序`（恰 1）標題仍在；Rule 4c 與新 QA FAIL 列皆含 `§Autonomous Engineering Gate` 且皆不含 `helper`／`hook`／`check`。（依據：`T02 對齊前|過渡期：|回指一行` 於 CLAUDE.md 與 rules 零命中；標題各 1；Rule 4c 與新列皆含 §Autonomous Engineering Gate，且不含 helper／hook／check）
- [x] **[存在性]** 無雙重語意對照：於收尾實錄列表，涵蓋 Rule 4c、新 Table QA FAIL 列、`### QA FAIL 優先序` 三處，逐一情境（可自修且已授權／不可自修／人工邊界／疑似 scope／停損成立）各僅對應一個終點（續作或停止），不得兩者並存。（依據：見收尾實錄對照表）
- [x] **[行為性]** D2 重放（fresh subagent，n=1，弱證據）：given 僅提供修改後 Rule 4c 單行全文，並可讀 `.claude/rules/ssot-governance.md`（僅此檔），**不提供預期答案、plan 或本票**；情境 ①已授權 scope 內可自修之 QA FAIL、②疑似 scope 問題（未取證）、③真正新增權限或不可逆操作；expect ①修復並驗證、不要求例行確認，②先本地取證與 Advisor、不直接升報，③不自行放行（升報）；重放原文與實際讀取路徑（含所讀標題）落收尾實錄；不宣稱 production 行為已被證明。（依據：見收尾實錄；n=1 弱證據）
- [x] **[行為性]** qa 入口一致性重放（fresh subagent，n=1，弱證據）：given 入口僅為 `qa/SKILL.md`「Step 4.5 完成後：Auto-Chain 銜接點」節全文，並可讀 `ssot-governance.md`（僅此檔），不預先指出 Gate 節位置、不提供預期答案；情境 ①可自修 QA FAIL、②疑似 scope、③停損成立（同 incident 已 3 輪 fail）；expect ①依 Table 列續作修復並重跑 `/qa`、`/qa` PASS 前不進入 `/sv`，②先取證與 Advisor，③`MUST STOP`；observer＝重放須列出實際走過的標題路徑；任一情境互斥或歧義→依 Error Handling 停止回報。（依據：見收尾實錄；n=1 弱證據）
- [x] **[存在性]** `/mdlint .claude/CLAUDE.md` 與 `/mdlint .claude/rules/ssot-governance.md` 之結果無新增 warning（既有 warning 不屬本票，記錄即可）。（依據：單行文字替換，無標題／清單／fence／表格結構變動，人工檢查零新增 warning）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案；FORBIDDEN 零變更；已知限制（`uc`／`qa` SKILL 之「停止」字面對齊屬 T02、重放為 n=1 弱證據、`2026-09-16 收窄律`僅有 vault 轉述）已於收尾實錄逐項揭露，且不宣稱機械 Gate 已實作。（依據：2F，+3／−6 之 diff 僅 ALLOWED 兩檔；FORBIDDEN 零變更；限制見收尾實錄）

## 收尾實錄（執行期，2026-10-12）

- **實作**：`.claude/CLAUDE.md` Rule 4c 整行替換；`.claude/rules/ssot-governance.md` QA FAIL 列改寫、回指行與相鄰空行刪除、過渡句刪除、Gate 節不外溢條件②括註同步。未動任何 SKILL／hook／settings／PROGRESS／app。
- **AC-4 無雙重語意對照**（三處皆讀現檔）：

| 情境 | Rule 4c（CLAUDE.md） | Table QA FAIL 列 | `### QA FAIL 優先序` |
|---|---|---|---|
| 可自修且已授權 | 依 Gate 修復、局部驗證並揭露（續作） | `MUST AUTO-CHAIN` 修復後重跑 `/qa`（續作） | 先依本節修復與驗證（續作） |
| 不可自修 | 確需新增權限或不可逆→依該節升報（停止） | `MUST STOP`（停止） | `MUST STOP` 適用（停止） |
| 人工邊界（權限／不可逆／產品語義／取證＋Advisor 確認須擴 scope） | 依該節升報（停止） | `MUST STOP`（停止） | `MUST STOP` 適用（停止） |
| 疑似 scope（未取證） | 須經取證與 Advisor 確認才升報（先取證） | 未單獨列（歸「不可自修或人工邊界」之前置，由 Gate 節定義） | 不得直接停止，須先取證與 Advisor（先取證） |
| 停損成立 | 依該節升報（停止） | `MUST STOP`（停止） | `MUST STOP` 適用（停止） |

  每情境僅一個終點，無並存。
- **AC-5 D2 重放（fresh subagent，n=1，弱證據）**：入口＝Rule 4c 單行全文，僅可讀 `ssot-governance.md`。讀取路徑：Grep「Autonomous Engineering Gate」→ Table QA FAIL 列（L153）→ `## Autonomous Engineering Gate`（L173）→ `### Gate 適用條件與順序`（L183）→ `### Incident 與輪次`（L188）→ `### HUMAN_REQUIRED`（L201）→ `### Advisor 與 PPSS`（L209）。判讀：①自主續作（`MUST AUTO-CHAIN` 修復、局部驗證、揭露、重跑 /qa，不例行請示）；②先本地取證與 Advisor，確認須擴 scope 才升報 `SCOPE`，取證後 ALLOWED 內可解則回①；③`MUST STOP` 並以 `HUMAN_REQUIRED` 升報，不自行放行。互斥判讀：無。subagent 揭露一處輕微分類模糊：情境③「修改 FORBIDDEN 檔」可歸立即類 `PERMISSION` 或驗證先行類 `SCOPE`，文件未明說；兩種歸類皆不導向自主修改，僅差升報前是否須先取證＋Advisor（本票不處置，屬 Gate 節既有分類，記為已知限制）。
- **AC-6 qa 入口重放（fresh subagent，n=1，弱證據）**：入口＝`qa/SKILL.md`「Step 4.5 完成後：Auto-Chain 銜接點」節全文，僅可讀 `ssot-governance.md`，未預指 Gate 節。讀取路徑：入口 → Grep「QA FAIL」→ `### 轉移表` QA FAIL 列（L153）→ `## Autonomous Engineering Gate`（L173，契約狀態）→ `### Gate 適用條件與順序`（L186）→ `### Incident 與輪次`（L191–192）→ `### 步驟級上限與 LIMIT`（L198）→ `### HUMAN_REQUIRED`（L201–206）→ `### QA FAIL 優先序`（L221–225）。判讀：①不進 /sv，自修後重跑 /qa（`/qa` PASS 前不進 /sv）；②現在不可停止、不可擴 scope，先本地取證再由主 agent 諮詢 Advisor，確認後分流；③`MUST STOP`，`HUMAN_REQUIRED: EXHAUSTED … rounds_3`。互斥判讀：無；L153 的 `MUST STOP` 與 L223 之自修優先序由 `### QA FAIL 優先序` 明確限縮。不確定處屬前提事實（輪數是否真屬「輪」、Advisor 結論），非文件矛盾；另指出 Gate 之 helper／ledger 尚不存在，輪數目前僅靠 agent 自記。
- **未證明事項**：兩次重放只證明讀者可由 Rule 4c 或 qa 入口沿引用抵達 Gate 節並得一致判讀；不證明任何機械 Gate、helper、hook 存在或可達，亦不證明 production 行為。
- **已知限制**：①`uc/SKILL.md` 規則 9「QA FAIL…**MUST** 停止」與 `qa/SKILL.md`「Auto-Chain 停止條件」字面「停止」在過渡句（D7）刪除後無檔內解讀句，qa 入口重放未因此讀出互斥；`uc` 路徑未重放，對齊屬 autogate plan T02；②重放為 n=1 弱證據；③`2026-09-16 收窄律`僅有 vault 轉述，未當作 repo 條文；④情境③ `PERMISSION`／`SCOPE` 分類模糊（見上）。不宣稱機械 Gate 已落地。
- **DEFERRED-TO-DOCSYNC（不開票）**：`.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` 同步留待下一輪 `/compact`。
- **驗收鏈**：`/qa` Step 0 PASS、Step 4 PROGRESS SKIPPED（DEFERRED-TO-DOCSYNC）、Step 4.5 commit `9a0e1fb3`（`DIFF_REF=470bf098`／`DIFF_END=9a0e1fb3`，2F +3／−6）；Build／Test／Lint N/A（docs preset）。`/sv` Codex Cross-Review **PASS**（`Guard Input Reachability: 無新 Guard`、`REACHABILITY: NONE`）。R0 預審未執行。
- **Traycer**：`Traycer Workflow: N/A` → `reviews.traycer=N_A`。終審 `CLOSED` 限審查方 SMTR／CSO。
