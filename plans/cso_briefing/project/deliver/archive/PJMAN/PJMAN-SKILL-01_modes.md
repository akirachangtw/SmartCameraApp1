# PJMAN-SKILL-01: 讓 projman Skill 具備 base／update／align 三模式、摘要置前的量化成果呈現、新舊成果文件相容回填、align 直接回報，以及供後續治理條款呼叫的行為契約（本票不啟用任何自動呼叫）

**Revision**: Rev B.3（含 2026-10-18 REVIEW 內同票修補：V4 狀態七值集合，Akira 裁決，見收尾實錄 §9；Rev 標籤沿用，不另增審查回合；2026-10-17；Akira Rev B.2 審查裁決：接受 F8／F3／F5 與兄弟 AC 修補；收準兩處——①手動 update 的授權依據＝使用者對指定文件的修訂授權，無衝突標記只是格式前提，已知並行衝突略過並回報，不新增鎖或狀態系統；②500 行為精簡參考、非 execution gate，統一 AI-7／第一條 AC／Error Handling／External-review 回流；4a 計數＝1）。Rev B.2（2026-10-17；Akira RGR 裁決落檔：F8 採第一案〔舊格式 per-ID 錨點寫入該列證據格 `align: <日期> <HEAD40> 範圍 <ID>`，全域最近 align 只作摘要〕、F3 採寫入前現值基準、F5 採「無約定」；同步 AI-3／AI-4／AI-5／HC2／HC3／AC／狀態矩陣／V1／V3／V4；**4a**：Rev B＋B.1＋B.2 觸發 4a，存廢裁決＝**續修原票**〔不退回 plan、不另開修補票、不縮減原成果〕，裁決落檔後 4a 計數歸零）。Rev B.1（2026-10-17；Rev 計數＝2；RGR cycle 1 修補 F1／F2／F4／F6／F7〔AC 標型與示例推演證據紀律、V1／V3 基準前提、`約定` 值域、未追蹤文件 `git commit -o`、AC12 分支〕；F3／F5／F8 為待裁 C_EXCEPTION，未改；報告 `.tmp/rigor-gate/RGR_PJMAN-SKILL-01_20261017.md`）。Rev B（2026-10-17；Rev=1；Akira 授權執行 6a 外審，回流已消化：僅更新 External-review 欄與過期之「必要未決事項」、證據分級 UNKNOWN 條、AI-1 約束、AI-7 靜態檢核、Error Handling 與第一條 AC 的長度檢核；Selected item／Goal／ALLOWED／FORBIDDEN／其餘 AI 與 AC 不變）。Rev A（2026-10-17；首次開票，Rev=0）。

**檔案治理／來源說明**：本票為 Plan-backed 單票（plan-local item `T01`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261017-projman-plan.md`（Rev C，2026-10-17；`.tmp` 非長期載體，下列來源資訊已內嵌，本票於該檔消失後仍可獨立閱讀）；Parent artifact 狀態＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`；Parent evidence status＝SUPPORTED（上游已審，不代替本票之當前本地取證）；Parent 上游診斷＝`.tmp/ticket-author/20261017-projman-diag.md`（`Gate: PASS`／`Verdict: PARTIALLY_CONFIRMED`）；Parent 讀取時間＝2026-10-17 21:0x–21:1x（本地，完整讀取）。

**Selected item 原文（T01 goal，摘要不改義）**：`projman/SKILL.md` 支援 `update`、新成果呈現、`align` 直接回報逐項結果／階段數量／主要缺口／下一步、對舊格式文件的相容 `align`、update 完整性核對指令，以及「被治理條款呼叫時」的章節（成果文件定位規則為可被引用之單一章節且排除歸檔；單檔寫入與寫前 dirty 略過；失敗只報告「已執行／已回填／未提交」與 dirty 路徑；只消費既有證據；從未 align 階段的預設錨點；由條款觸發時的回報項目與紀錄）。手動使用即完整可用，**Skill 自身不觸發任何自動動作**。

**相關約束（取自 parent 決策表，本票適用者）**：
- **D-1／D-7／D-8**：`disable-model-invocation: false` 保留；成果 Markdown 為唯一可編輯來源（不得有平行 JSON／DB／API）；`已驗收`／`約定` 為數值來源，`達成度` 若顯示屬衍生，未盤點 `—`≠零通過 `0`，不計跨階段平均；舊格式文件不強制遷移，`align` 相容回填舊 5 欄文件。
- **D-2／D-3／D-4**：票前綴只是可選定位線索，缺前綴舊文件不因此整類排除；每階段／成果 ID 各自記錄最近 align（日期、HEAD 全雜湊、檢查範圍），校正某階段不消費其他階段未檢查的交付；從未 align 階段以成果文件首次提交為預設錨點；`git log` 只協助列示候選，不代替功能證據，不將 docs-only 或規則型成果整類排除。
- **D-6／D-6b**：被條款呼叫時，寫前 `git status --short -- <文件>` 非空即略過並回報；保存只在條款授予單檔保存時以 `git commit -o <成果文件>` 進行，提交失敗回報「已執行／已回填／未提交」與 dirty 路徑，不回滾原票；自動模式只消費既有證據，不新增付費呼叫；授權條款本體、實際啟用與 Governance 條文屬 T03，**不在本票**。
- **D-15（T01 範圍：只定義回報契約）**：被條款呼叫時的回報項目＝呼叫來源、Skill 實際呼叫、回填差分、結果摘要、保存結果（commit hash 或未提交原因）、Claude Code session id 與依序工具呼叫；取不到如實標 `UNAVAILABLE`，不得事後編造；本票**不宣稱自動鏈已驗證**，PM2-E 類驗證項維持未驗。
- **Parent Plan Gate／eligibility 結果**：Plan Gate＝PASS；Ticket mode eligibility＝ALLOWED；Fixture＝false（本次 ticket 模式第 1 步逐項核對屬實）。

**證據分級**（2026-10-17 21:0x–21:1x live 直讀；HEAD `2bfae8fb`，開票時工作樹 clean）：
- **CONFIRMED（本地原始碼／工具輸出）**：①`.claude/skills/projman/SKILL.md` 153 行、`i/lf w/lf`（`\r` 行數 0）、blob `fa74fd20`、已追蹤且 `git check-ignore -v` 零命中；現值錨點 25 個（含 `name`、`description`、`disable-model-invocation` 三行）皆行首匹配且各恰 1 次（見 AI-0）；`MODE` 現僅 `base`／`align` 兩值（L22–L29），定位規則在 L38–L45（L41 含 `--exclude-dir=archive`），align 回填邊界在 L103，判斷用語在 L113–L118，frontmatter `disable-model-invocation: false` 在 L5；該檔僅有三個 commit（`52450f11`／`003cdcba`／`82b64bc1`，皆 `[GOVERNANCE-SYNC]`、無 Ticket）。②舊格式樣本 `plans/roadmap/autonomous-engineering-gate-outcomes.md`：標題 `## 基準`（L3）／`## 階段成果表`（L30）／`## 已確認變更`（L45）；表頭五欄 `階段／成果 ID｜預期功能與規格｜驗收方式與必要前提｜實際成果與證據｜落差與下一步`（L32）；`最近 align` 為單行散文且含 40 碼 HEAD（L28）；列 ID 粒度＝每列一個 ID（`S0`、`S1-R1`、`S3-R4`… 以階段-需求編碼，非票名票號）；以 `grep -rl "^## 階段成果表" plans/ --exclude-dir=archive` 為唯一命中。③重複票查驗：Registry 主冊＋歸檔共 112 筆，`PJMAN` ID 0、`Projman` topic 0（現有 topic 共 12：KAIROS／MEO／Advisor／Cueshot／Memory／INFRA／WKTR／RGR／MCP／MOBCD／TKAuthor／ACAGOV）；`Pending/`（7 檔）／`deliver/`／`void/` 檔名無 `pjman`／`projman`；`plans/`、`.claude/`、`.tmp/`、`docs/` 內 `pjman` 字面僅出現於 parent plan／diag；`ticket_pattern.md` 的 `PJMAN` 命中 0（為新 family，結構文法既已涵蓋，**不改 regex**）；全冊 `reports/` 目錄不存在。④核對指令可行性（唯讀探測）：Git Bash 下 `awk`／`grep`／`git`／`python -X utf8 -I` 皆可用；對舊格式樣本抽 ID 集合、`comm` 比對 HEAD 與工作樹得 0 差異；`git log --diff-filter=A --follow --format=%H -- <文件> | tail -1` 可取首次提交。⑤session 證據最小途徑（**本 session 限定**）：`CLAUDE_CODE_SESSION_ID` 環境變數值＝transcript 檔名 `~/.claude/projects/D--DevSources-AndroidProj-SmartCameraApp1/<session-id>.jsonl` 之主檔名；該 transcript 內 `sessionId` 欄位與 `tool_use` 區塊（依序、含 id）可由唯讀 `python -X utf8 -I` 讀出。⑥`.gitignore`：`tmp/` 已排除（L52，scratch 可用 `tmp/PJMAN-SKILL-01/`）；`Pending/` 受 `plans/cso_briefing/project/*`（L81）排除（凍結點語義，預期）；`deliver/`、`plans/roadmap/` 不被排除。⑦同檔並行票：Pending 7 檔皆非 projman；冊面唯一近期票 `ADV-APICALL-01` 為 `REVIEW`、改動範圍 `.claude/scripts/`，與 `projman/SKILL.md` 無交集；自 parent 基線 `5b80bc32` 至 HEAD 僅 3 個 commit（`ce6969c3`／`6c8f83f9`／`2bfae8fb`，只動 `.claude/PROGRESS*.md` 與 `deliver/ADV-APICALL-01_receipt.md`），`.claude/skills`、`.claude/rules`、`plans/roadmap`、`.claude/hooks` 零 diff。⑧`projman` 字面的現行 consumer：僅 `ADV-APICALL-01_receipt.md`（FORBIDDEN 註記）與樣本成果文件的提及；`sv`／`uc`／`qa`／`compact`／`ssot-governance.md` 零命中——本票不接線任何 consumer。
- **Source／live 衝突處置（可見）**：①parent Live baseline 為 `5b80bc32`，live HEAD 為 `2bfae8fb`（+3 個只動 `.claude/PROGRESS*.md` 與 `deliver/ADV-APICALL-01_receipt.md` 之 commit），本票表面零 diff → 以 live 為準、不影響範圍。②parent D-6 將「自動模式是否連本地新建置／測試也禁止」列 `RECOMMENDED_ONLY` 並交 T01 取證 → 本票採**預設不新增本地建置或測試、僅用唯讀命令**（AI-6 #2；可逆，屬 Skill 文字預設，T03 條款不因此受縛）。③parent T01 `estimated_scope` 將 `ticket_pattern.md` 列為「ALLOWED 或 spawn 動作」二選一 → 本票兩者並行：spawn 當下追加一列、列入 ALLOWED 由 Auto Commit 一併提交（見上「開票時檔案異動揭露」）。
- **SUPPORTED**：無外部來源依賴。
- **HYPOTHESIS**：新 Skill 文字能使模型在 `align`／`update` 時遵守「新舊格式各自的寫入集合」與「per-ID 範圍」——需 T02 真實 Skill 執行才能驗證，本票以隔離示例推演（非真實執行）取證。
- **UNKNOWN（有 AC／限定交付承接）**：①session id 與工具序列在**未來其他 session／其他執行形態（headless、subagent）**是否同樣可取得——本票只規定「取不到即 `UNAVAILABLE`」；②Skill 文字加長後模型遵循度——不在本票驗證；③transcript 路徑／格式與 `CLAUDE_CODE_SESSION_ID` 的**官方文件化程度**——6a 外審之公開搜尋未命中（僅限該次 5 條查詢，不代表不存在），本地 CONFIRMED 僅限本 session。
- **已知限制（誠實揭露）**：本票只改指令文字；`align`／`update` 的真實行為屬 T02；所有「示例推演」均標明非真實 Skill 執行；自動鏈（條款＋consumer）屬 T03，本票交付後**不存在**任何會呼叫 align 的條文。

**External-review decision**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` v3.4 §6〔L43–L50〕與「分流順序與發送通道」節，2026-10-17 live 直讀；分項記錄，不以整節 WAIVED 掩蓋已成立的觸發）：
- **6a（成立；外審 EXECUTED，2026-10-17）**：`INTRODUCED_CAPABILITY=feature:projman-update-mode`≠`NONE`；固定 corpus（vault `D:\DevSources\Smarter-Project` 決策檔目錄、`Handoff/`、`Pending/` `deliver/` `void/`、`TICKET_REGISTRY.json`、`_ARCHIVE.json`）該 token 字面 **0 命中**（本票自身不計）→ 條件成立。處置＝依 Akira 2026-10-17 授權（在 Autonomous Engineering Gate 既有用途、成本與權限邊界內執行必要外審取證，不再逐次請示），Claude 自行呼叫 `research_submit`：`rsch-20261017-7619d266f3`（2026-10-17，5 條查詢，估計 $0.005，artifact `D:\DevSources\workflow\mcp-ticket-platform\data\research\2026-10\rsch-20261017-7619d266f3.json` 為 runtime 衍生物，**結論以本欄為準**）。**回件性質**：搜尋服務回傳 5 筆**頁面摘錄**，皆為 github.com 上對官方文件的轉載／參考整理／issue，**未取得 `code.claude.com` 一手頁面**；故下列多為 `SUPPORTED`，本回流**不代表官方契約已確認，亦不構成本票品質驗收 PASS**。回流消化（每項：結論｜證據等級｜來源與日期｜處置）：
  1. `disable-model-invocation`：`true`＝Claude 不得自動載入、僅使用者可呼叫；預設 `false`＝使用者與模型皆可｜`SUPPORTED`｜`github.com/CodeWithBehnam/cc-docs/blob/main/claude-code-docs/skills.md`（last_updated 2026-10-13）、`github.com/sitharaj88/claude-skills/.../frontmatter.md`（last_updated 2026-10-15）｜本票維持 `false` 與既定決策一致，無需變更。
  2. `$ARGUMENTS` 以 skill 名之後文字取代｜`SUPPORTED`｜同上 skills.md｜與現行 Invocation 區用法一致。另有 `${CLAUDE_SESSION_ID}` 字串替換｜`SUPPORTED`（`github.com/ThibautBaissac/rails_ai_agents/.../claude-code-frontmatter-reference.md`，自述基於官方文件 2026-03，last_updated 2026-10-13）｜**本票不採用**：session id 仍以本地已證實之 `CLAUDE_CODE_SESSION_ID` 為主；替換值與該環境變數是否一致、model-invoked 情境是否可用＝`HYPOTHESIS`，不阻擋，留 T02 真實執行時可選 probe。
  3. SKILL.md 建議：body <500 行、`name` ≤64 字元、`description` ≤1024 字元｜`SUPPORTED`（`github.com/SKD-international/agent-os/.../anthropic-best-practices.md`，last_updated 2026-10-16）；清單中 `description` 截斷於 250 字元｜`SUPPORTED`（rails_ai_agents 參考文件）｜**已納入本票**：AI-1 約束（`description` ≤250 字元）、AI-7 靜態檢核與第一條 AC（記錄最終 `wc -l`）、Error Handling。**500 行為精簡參考、非 execution gate（Rev B.3）**：超出先去重，必要內容保留並回報原因；不因行數停工、不自行拆檔、不刪需求。
  4. transcript 路徑／格式與 `CLAUDE_CODE_SESSION_ID` 的官方文件｜**官方面 `UNKNOWN`**（公開搜尋未命中，僅限本次 5 條查詢；非「不存在」，屬六態①）｜本地 `CONFIRMED`（本 session 唯讀實測，見證據分級⑤）｜風險＝屬實作細節、可能隨版本變動；已由「取不到寫 `UNAVAILABLE`、不編造」承接，不構成否決。
  5. `git commit -o <path>` 語義｜搜尋未回傳官方頁面｜**本地 bounded probe 已執行並升 `CONFIRMED`**（暫存 repo，`git 2.54.0.windows.1`，2026-10-17）：另一檔 `A.md` 已 staged、`B.md` 工作樹修改時，`git commit -o B.md` 僅提交 `B.md`，`A.md` 維持 staged；對無變更路徑回「nothing to commit」。
  6. Markdown 表格欄數規則（markdownlint MD056 等）｜官方面 `UNKNOWN`（搜尋未命中）｜V6 以自有欄數核對＋repo 內 `/mdlint` Rule G／H 為準，不依賴外部規則編號。
  - **四問回答摘要**：①證據等級如上；②硬風險判準①–⑥皆**未命中**（①官方 contract 未見禁止，`false` 為預設；②無 provider 請求格式；③無新公開網路面；④Markdown 文字可 `git revert`，無不可逆寫入；⑤外審費用 ≈$0.005 且已執行，無持續費用；⑥probe 未失敗）；③替代路徑——回件未提供，本地設計取捨：不新增 `update`（以 `base` 重跑）違反 parent 已裁決需求（無聲覆蓋風險）、腳本化核對違反「不新增腳本」、hook 記 session 違反「不新增日誌系統」，故維持現設計；④≤`HYPOTHESIS` 項＝`${CLAUDE_SESSION_ID}` 替換一致性（probe 留 T02，不阻擋）、transcript 格式跨版本穩定（`UNKNOWN`，以 `UNAVAILABLE` 契約承接）。
  - **防遞歸律**：本 capability 已完成一次外審＋一次 bounded probe；不因來源格式不足或文件表述差異重觸發同一外審。
- **其他觸發項**：6b①—`ALLOWED` 兩檔首層 path segment 皆為 `.claude`，distinct＝1 <2，不成立（開工後若 ALLOWED 增跨首層路徑 MUST 重判）；6b②—`### AI-[1-9][0-9]*:` 標題數＝7 <8，不成立；6b③—`DEPENDENCY_DELTA=NONE`，不成立；6c—`projman/SKILL.md` 無 `Revision` 欄位修訂歷史，不納本款。
- **Cross-Review／QA／SV**：**不豁免**（理由：新增 `update` 模式與跨格式寫入邊界契約，下游 T02／T03 直接引用；雖為 Markdown 且可 `git revert`，但屬 agent 行為契約而非單純敘述文件）。`assembleDebug` N/A（docs preset）。Traycer＝N/A。

**必要未決事項**：無（原 6a 外審待裁事項已於 Rev B 依 Akira 2026-10-17 授權處置：外審 EXECUTED，結論見上；本欄不再保留 A／B 選項）。Cross-Review／QA／SV 仍不豁免，External-review 欄任何狀態都不代表品質驗收 PASS。

**簽發日期**: 2026-10-17
**簽發者**: Claude
**來源議題**: Projman
**Mode**: normal  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（上游為 plan artifact 與 diag，非正式票）
- `evidence_dependency`: 無（判準、錨點、HC 與 AC 所引用者為 parent plan Rev C、diag、現行 `projman/SKILL.md` 及舊格式樣本成果文件，皆非正式票產物）
- `coordination_dependency`: 無（plan-local `T02`／`T03` 為本票下游，於本票 `REVIEW` 後另行開票，不構成本票之同批收束要求；與任何編輯 `.claude/skills/projman/SKILL.md` 之票須串行，`concurrency.md` §2，開票時掃描結果＝無）

## Verify Config
TICKET_ID=PJMAN-SKILL-01
CREATED=2026-10-17
PRESET=docs
ALLOWED=.claude/skills/projman/SKILL.md,.claude/rules/ticket_pattern.md
FORBIDDEN=.claude/rules/ssot-governance.md,.claude/rules/cross-review.md,.claude/rules/concurrency.md,.claude/CLAUDE.md,.claude/skills/sv/**,.claude/skills/uc/**,.claude/skills/qa/**,.claude/skills/compact/**,.claude/skills/ticket-author/**,.claude/hooks/**,.claude/scripts/**,.claude/settings.json,plans/roadmap/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,app/**
MAX_LINES=300
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-17 21:12
BASE_COMMIT=2bfae8fb
TARGET_BLOB=.claude/skills/projman/SKILL.md:fa74fd20;.claude/rules/ticket_pattern.md:f51b4bf7
INTRODUCED_CAPABILITY=feature:projman-update-mode
DEPENDENCY_DELTA=NONE

> PRESET=docs（兩檔皆為 Skill 指令文字與 family 登記表；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A 見 AC）。`MAX_LINES=300` 為 production-only diff 估算校準遙測（TPL v3.5：非 execution gate；現檔 153 行，預估新增約 170–200 行、替換約 50–80 行），**非**停止或退件判準。
>
> **`TARGET_BLOB` 說明**：兩 blob 為 `BASE_COMMIT=2bfae8fb` 當下 HEAD 之值（`git hash-object`／`git rev-parse HEAD:<path>`）。`.claude/rules/ticket_pattern.md` 之工作樹於開票（spawn）當下**已含一行 PJMAN family provenance**（Rule 4a 授權，見下「開票時檔案異動揭露」），故其工作樹 blob 異於 `f51b4bf7`；AI-0 以「與 HEAD 之唯一差異＝該新增一行」取代 blob 相等比對。
>
> **開票時檔案異動揭露（不隱藏）**：依 `CLAUDE.md` Rule 4a「新 Ticket 使用尚未登記之 family 時，MUST 於 spawn 當下同步補登 provenance 表」與 TPL v3.6「縮寫規則」，開票流程已於 `.claude/rules/ticket_pattern.md`「Family Provenance 補登紀錄」表**末端追加 1 列**（`PJMAN-`，5 字母合 2–6 上限；全冊 grep 查重 0 命中；**不改 regex**、不動他列）。範圍＝該檔單檔單列；授權＝Rule 4a（補登為轉錄、非裁決）＋使用者 2026-10-17 於 ticket 指令明示「新 family provenance 依現行規則於開票時處理；若有實際檔案異動，明確記錄其範圍與授權」。該列**尚未提交**（工作樹 dirty），故將 `.claude/rules/ticket_pattern.md` 列入本票 `ALLOWED`，由本票 Auto Commit 一併提交（兩檔同屬 `.md`）。

## Action Items

> **AI-0 為強制首項**。以下各項為錨點與基線復驗；任一項不符，先依 TPL「基線漂移三分流」分類（`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 為資訊性，記錄後以工作樹現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止並直送收束審查；`BLOCKED_UNATTRIBUTED_STATE` 因本票僅觸及 `.claude/` 可逆治理面，降為記錄後續作），**禁以「推定等價位置」充當字面錨點**。

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/projman/SKILL.md`、`.claude/rules/ticket_pattern.md`（及唯讀參照檔）
- **動作**: 逐字確認各「現值」於檔內命中數與票面所載相符（標「唯一」者恰 1 次），並記錄 `DIFF_REF`：
  1. 兩檔皆已追蹤（`git ls-files` 2/2）、`git check-ignore -v` 對兩檔零命中；`projman/SKILL.md` 工作樹 blob＝`TARGET_BLOB`（`fa74fd20`）、`i/lf w/lf`、`\r` 行數 0、共 153 行；`git status --short` 對 `projman/SKILL.md` 為空。
  2. `projman/SKILL.md` 現值錨點（皆行首匹配，**唯一**＝各恰 1 次）：`^argument-hint: `、`^第一個非空白 token 為 `MODE`，只接受：`、``^`MODE` 缺少或不在上述兩值``、`^由使用者直接呼叫，或依使用者明確指示由模型呼叫`、`^## 節奏$`、`^2\. 已有對應成果文件 → 更新同一份`、`^### 文件結構（單一文件、單一表）`、`^## 基準$`、`^## 階段成果表$`、`^## 已確認變更$`、`^重新執行 `base`（文件已存在）：`、`^流程：$`、`^5\. 在對話給短版成果摘要與初判。`、`^回填邊界：`align` 只寫`、`^- 每個階段列只寫\*\*一個\*\*判斷`、`^## 路線與規格調整$`、`^## 第二方複核$`、`^## 邊界$`、`^## base$`、`^## align$`、`^### 證據規則$`、`^- 舊證據可沿用並註明日期與來源`、`^「已確認變更」僅在有使用者已確認的變更時才新增列`、`^- 「實際成果與證據」初始填「未開始」`、`^description: `、`^name: projman$`；且 `^disable-model-invocation: false$` 恰 1 次。
  3. `ticket_pattern.md`：70 行（未含新增列時）／`i/lf w/lf`；`TKA-` 列為表末（開票時 L64）；工作樹與 `HEAD` 的**唯一差異**＝末端新增之 `` | `PJMAN-` | `` 一列（`git diff HEAD -- .claude/rules/ticket_pattern.md` 僅 `+` 該 1 行、`-` 零行）；`PJMAN` 於該檔命中恰 1 列。若該列已被他途提交進 `HEAD` → 本票對該檔零 diff，記錄後續作，不重複追加。
  4. 唯讀參照：`plans/roadmap/autonomous-engineering-gate-outcomes.md` 仍為舊 5 欄格式（`^## 階段成果表$` 恰 1 次、表頭含 `預期功能與規格`、不含 `已驗收`）——作為舊格式樣本，**本票不修改該檔**（FORBIDDEN）。
  5. `grep -rl "^## 階段成果表" plans/ --exclude-dir=archive` 輸出集合記錄於收尾實錄（開票時＝上列樣本 1 檔）；`registry_list(project=SmartCameraApp1, include_archive=true)` 對 `PJMAN-*`（本票除外）零命中、冊面 topic 清單含 `Projman` 恰 1 筆（本票開票時依使用者一次性授權登記）。
  6. 基線漂移：`git diff --name-only 2bfae8fb..HEAD -- .claude/skills/projman/SKILL.md .claude/rules/ticket_pattern.md` 輸出為空（空＝未及標的，續作；非空依三分流歸因）。
  7. 工具可用性：`bash -n` 不適用；`awk`、`grep`、`git`、`python -X utf8 -I -c "print(1)"` 於 Git Bash 可執行（AI-7 核對指令所依）；若不可用 → 見 Error Handling（不得新增 parser 框架或腳本）。
  8. `INTRODUCED_CAPABILITY`／外審狀態未變（本票 External-review 欄）；scratch 目錄 `tmp/PJMAN-SKILL-01/` 不存在或為空（`concurrency.md` §1 專屬子目錄）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成（2026-10-17；AI-0：2026-10-17 23:5x；錨點 26/26 唯一、blob `fa74fd20`、LF、153 行、check-ignore 0；`ticket_pattern.md` 唯一差異＝PJMAN 1 列（+1/−0，HEAD 尚無 → 非零 diff 分支，隨本票提交）；舊格式樣本仍 5 欄、無 `已驗收`；漂移 `2bfae8fb..HEAD` 空；工具可用；scratch 目錄先前不存在。DIFF_REF＝`2bfae8fb`（activation 已寫入 conf）。）

### AI-1: Frontmatter、Invocation 與 節奏——三模式入口
- **檔案**: `.claude/skills/projman/SKILL.md`
- **現值（唯一）**：
  1. `argument-hint: "base|align [topic、成果文件路徑或階段 ID，以及需求文字]"`（L4）與 `description:`（L3，含「Manual modes: base, align.」）。
  2. Invocation 區：`第一個非空白 token 為 `MODE`，只接受：` 下方 code block 僅列 `/projman base` 與 `/projman align` 兩行；``MODE` 缺少或不在上述兩值 → 只顯示上列用法後停止，不猜測模式、不開始工作。``
  3. 節奏區首句：`由使用者直接呼叫，或依使用者明確指示由模型呼叫；align 在階段成果點執行。…`
- **目標值**：
  1. `argument-hint` 改列 `base|update|align`；`description` 的模式清單同步為 `base, update, align`（`name`、`disable-model-invocation: false` 位元組不變）。
  2. Invocation 用法 code block 增 `/projman update  [成果文件路徑 或 修訂內容]` 一行；「兩值」改為「三值」，缺少或不支援 → 只顯示用法並停止，不猜測模式。
  3. 節奏區：保留現有「不得自動掛到每張 Ticket 完成、每次對話、交接或小修改…」整句不動；補述 `update` 只在**真實修訂**（需求增補、澄清、規格修訂、Bug 揭露基準缺漏）出現時使用，不為驗證而製造修訂；補述「被治理條款呼叫」的行為見本檔專節，**本檔不定義、不啟用任何條款**。
- **約束**：frontmatter 其餘行零變更；節奏區現有句子位元組不刪不改（僅追加）；`description` 長度 ≤250 字元（清單截斷準則，6a 回流 `SUPPORTED`）、`name` 不變。
- [x] 完成（2026-10-17；AI-1：SKILL.md L2–L5（name／description 122 字元／argument-hint 三模式／flag 不變）、L16–L32（Invocation 三值）、L35 節奏整句位元組不變、L37 補述 update 與專節指引。）

### AI-2: 成果文件節——定位規則單一章節、命名、票前綴僅輔助
- **檔案**: `.claude/skills/projman/SKILL.md`
- **現值（唯一）**：`## 成果文件` 之「位置決定順序」編號清單，其中第 2 項 `2. 已有對應成果文件 → 更新同一份，不建平行副本。判定只看結構標記：…`（含 `grep -rl "^## 階段成果表" plans/ --exclude-dir=archive`）、第 3 項預設 `plans/roadmap/<topic>-outcomes.md`。
- **目標值**：
  1. 把「位置決定順序」整理為**可被引用的單一章節**，標題字面 `### 成果文件定位規則`（供後續治理條款以此章節為候選集合來源而不複製邏輯）；順序語意不變：使用者指定 → 已有對應成果文件 → 新建預設 → 多候選詢問一次。
  2. 「已有對應成果文件」判定保持**結構標記為資格**（`^## 階段成果表` 命中且議題相符），並明寫：**票前綴只是可選定位線索、不是資格**；舊文件缺前綴、缺議題識別欄不因此排除；自動搜尋一律排除 `archive/`（`--exclude-dir=archive`），**使用者明確指定該路徑者除外**；零個或多個候選無法唯一判定 → 說明並詢問一次，不猜測、不建立平行副本。
  3. 新建檔名規則：以**議題全名**開頭（`plans/roadmap/<議題全名>-outcomes.md`，例 `projman-outcomes.md`）；同議題已有有效文件先沿用；不得放在 `tmp/`、`.tmp/` 或其他暫存目錄（沿用現文）。
- **約束**：不新增搜尋工具或腳本；定位只用 `grep`／`ls`／`git`；不改變現行「排除 archive」語意。
- [x] 完成（2026-10-17；AI-2：SKILL.md L41 `### 成果文件定位規則`（恰 1 次）、L46 結構標記＋`--exclude-dir=archive`＋票前綴僅線索＋使用者明確指定除外、L47 議題全名、L48 多候選詢問一次。）

### AI-3: 文件結構（新格式）、格式辨識與量化規則
- **檔案**: `.claude/skills/projman/SKILL.md`
- **現值（唯一）**：`### 文件結構（單一文件、單一表）` 起至「`「已確認變更」僅在有使用者已確認的變更時才新增列，簡短一行即可。`」止之整段（含五欄 code block 範本）。
- **目標值**：以「**新格式（建議格式）**」取代該範本，並保留「**舊格式（相容）**」之辨識說明（不再作為新建範本）。須含下列逐項（用語可調，語意不可少）：
  1. **識別（基準區）**：專案穩定識別（`project_key`）與顯示名（`project_label`）分開；議題穩定識別（`topic_key`）與顯示名（`topic_label`）分開；`票前綴` 為**可選**欄；`最近 align` 摘要行（日期、HEAD 全雜湊、檢查範圍＝階段 ID 清單）；`目前階段`；其餘既有基準欄（目標、最小成功場景、不可默默刪減的核心能力、約束、非目標、假設）保留。
  2. **摘要表**（`## 階段成果表` 標題字面保留，作為定位錨點）固定短欄：`ID | 階段 | 成果 | 已驗收 | 約定 | 達成度 | 狀態`；其後為 `## 明細`，每個 ID 一個 `### <ID>`，含：預期功能與規格、驗收方式與必要前提、實際成果與證據、落差與下一步、該 ID 的 `最近 align`（日期、HEAD 全雜湊、檢查範圍，**固定單行**）。`## 已確認變更` 表沿用。
  3. **量化規則**：`已驗收`、`約定` 為各列獨立**整數**數值來源（`約定` 值域＝正整數或 `—`；`—` 表示該列無約定項，`0` 不合法；`約定`＝`—` 時 `已驗收`＝`—`、`達成度`＝`—`〔無分母〕、`狀態`＝`無約定`，不計比例〔Rev B.2，F5〕）；`達成度` 若顯示屬**衍生呈現**＝`round(100×已驗收/約定)%`，須與計數一致，不得另行維護；`約定` 為正整數之列，`已驗收`＝`—` 表示**未盤點**（`達成度` 亦為 `—`，`狀態` 為 `未盤點`），不得寫 `0`；`0` 專指**已盤點且零項通過**；`狀態` 值域＝`未盤點｜符合｜部分符合｜不符合｜未驗｜尚不具備驗收條件｜無約定`（前六值沿用既有判斷用語；`無約定` 為 Rev B.2 新增，**只用於** `約定`＝`—` 之列，與缺必要前提之 `尚不具備驗收條件` 分開，判斷用語節得追加其定義一條）；**不計跨階段平均、不設總計列／總百分比**；次要成果不得抵銷核心缺口（每 ID 各自計數）。
  4. **ID 穩定**：ID 不編入票名、票號或票數；不重用、不改名；新增項目以新 ID 追加；階段與票可多對多，拆票或加修補票不構成改基準的理由。
  5. **格式辨識**：`## 階段成果表` 下表頭含 `已驗收` 與 `約定` 欄 → 新格式；表頭含 `預期功能與規格` 欄 → 舊 5 欄格式；兩組標記同時存在、或皆無 → 視為**無法辨識**，不寫入、說明並詢問一次。
- **約束**：舊格式說明只描述辨識與相容回填，不要求遷移；不引入第二份可編輯資料或 JSON；表格欄數須逐行一致。
- [x] 完成（2026-10-17；AI-3：SKILL.md L52–L102（新格式範本、量化規則 L91、舊 5 欄相容 L100、格式辨識 L102）。）

### AI-4: base 調整與新增 update 模式（含完整性核對指令）
- **檔案**: `.claude/skills/projman/SKILL.md`
- **現值（唯一）**：`## base` 節中 `- 「實際成果與證據」初始填「未開始」，「落差與下一步」填「—」。`（唯一）與 `重新執行 `base`（文件已存在）：` 區塊；其後緊接 `## align`。
- **目標值**：
  1. `base`：新建文件採新格式；初始 `已驗收`＝`—`、`狀態`＝`未盤點`（`約定`＝`—` 之列為 `無約定`）；明細「實際成果與證據」填「未開始」，**已知已有進度者填「待首次盤點」**（不一律寫未開始，也不預填符合）；`約定` 項數依需求列明，不為填表新增規格。重新執行 `base` 的既有四條規則（不得無聲覆蓋、縮減需確認、歧義一次問清）保留。
  2. 於 `## base` 與 `## align` 之間新增 `## update` 節：**輸入**＝真實需求增補、澄清、規格修訂，或 Bug 揭露的基準缺漏；**一般修補不自動新增成果**；**不為驗證而製造修訂**。**行為**：保留既有 ID、有效證據與未受影響內容；新增成果不繼承完成狀態（新列 `已驗收`＝`—`、`狀態`＝`未盤點`，`約定`＝`—` 之新列為 `無約定`；既有列增加 `約定` 時 `已驗收` 不增、`達成度`／`狀態` 重新依規則衍生）；任何分母（`約定`）變動須在 `已確認變更` 有同次一列（日期、變更前後、影響、依據）；核心縮減、新成本／權限／交付取捨須先列差異並取得使用者明確確認；舊格式文件**不主動遷移**，僅在使用者明示要求時遷移，且遷移須保留全部原需求、ID 與證據。手動 `update` 遇文件未追蹤或 dirty 時（Rev B.2，F3；授權口徑 Rev B.3 收準）：**授權依據＝使用者對該指定文件的修訂授權**（非文件狀態）；以寫前完整讀取之現值為修訂起點與 V1／V3 基準，保留未受影響內容。不含合併衝突標記（`<<<<<<<`／`>>>>>>>`）**只是格式前提，不構成授權證明**；含衝突標記 → 不寫入，說明並詢問一次。**已知並行衝突**（例：同檔另有 session 正在修改）→ 略過該文件並回報，不新增鎖或狀態系統。被治理條款呼叫時之 dirty 略過規則（AI-6 #3）不變。
  3. `update` 完整性核對（寫入後 MUST 執行；指令優先、**不建 parser 框架、不新增腳本檔**，以內嵌於本檔的 `bash`／`awk`／`git`／`python -X utf8 -I -c` 單行指令表達）：**V1** ID 集合保留（`git show HEAD:<文件>` 與工作樹之摘要表第一欄 ID 集合，`comm` 比對，舊有 ID 不得消失；**基準前提（Rev B.1）**：僅當文件已追蹤且寫入前 `git status --short -- <文件>` 為空時，`git show HEAD:<文件>` 才等於寫入前狀態，V1／V3 皆以此為基準；**未追蹤或 dirty 之手動 `update`（Rev B.2，F3；授權依據見 AI-4 #2，Rev B.3）**：以寫入前**完整讀取**之文件現值為 V1／V3 比較基準，該現值保留於當次上下文，並於對話列精簡摘要（ID 集合、各 ID `約定`），寫後以指令輸出對照該摘要；不要求先提交或還原，不新增快照檔）；**V2** 摘要表 ID 與明細 `### <ID>` 互為子集（新格式）；**V3** 分母不無聲變動（逐 ID 比對 `約定`，任一變動須對應 `已確認變更` 新列）；**V4** 計數與衍生一致（`達成度`＝`round(100×已驗收/約定)%`；`已驗收`≤`約定`；`—` 與 `0` 不混；`約定` 為正整數且 `已驗收`＝`—` 時 `達成度`＝`—` 且 `狀態`＝`未盤點`；`約定`＝`0` 判違規；`約定`＝`—` 時 `已驗收`／`達成度` 皆須為 `—` 且 `狀態`＝`無約定`；`無約定` 只可出現於 `約定`＝`—` 之列，Rev B.2）；**V5** 無總計／平均列（grep `平均｜總計｜合計｜Total`）；**V6** 表格欄數逐行一致並依 `/mdlint` Rule G／H 檢查表格格式；V1–V6 任一不過 → 回報並修正，不宣稱完成。對舊 5 欄文件僅適用 V1、V6（其餘不適用須明寫）。
- **約束**：核對指令為**說明文字中的命令片段**，不得成為 repo 內新增的腳本檔；`update` 不修改 `約定` 以外之核心能力清單，除非屬使用者已確認變更。
- [x] 完成（2026-10-17；AI-4：base L104–L120（待首次盤點／無約定）、update L122–L168（輸入、行為、手動 update 寫前現值 L133、V1–V6 L135–L166、舊格式僅 V1／V6 L168）。）

### AI-5: align 調整——新舊格式寫入邊界、每 ID 範圍與錨點、直接回報、證據規則補充
- **檔案**: `.claude/skills/projman/SKILL.md`
- **現值（唯一）**：`## align` 節 `流程：`、`回填邊界：`align` 只寫「實際成果與證據」「落差與下一步」與「最近 align」。…`、`5. 在對話給短版成果摘要與初判。`、`### 證據規則` 條列（含 `- 舊證據可沿用並註明日期與來源；…`）。
- **目標值**：
  1. **寫入邊界（依格式辨識結果；writer／observer 對齊）**：
     - **舊 5 欄格式**：只更新本次實際檢查 ID 之列的「實際成果與證據」「落差與下一步」，以及基準區「最近 align」（單行散文，沿用）。**per-ID 錨點（Rev B.2，F8 第一案）**：每個本次檢查 ID 的錨點寫在**該列「實際成果與證據」格內**，固定格式 `align: <日期> <HEAD40> 範圍 <ID>`（Markdown 表格格內不得換行：該段以 `；` 分隔附於格尾、同格恰一段，既有者原位更新、不重複追加；不得含 `|`；以樣式 `align: [0-9]{4}-[0-9]{2}-[0-9]{2} [0-9a-f]{40} 範圍 ` 定位）；基準區「最近 align」**只作摘要**，不作任何 ID 的錨點；舊文件缺該摘要行時不新增（不屬遷移）。寫入集合仍為上述三處，不擴張。
     - **新格式**：明細中該 ID 的「實際成果與證據」「落差與下一步」「最近 align（單行）」；摘要表中**本次實際檢查之 ID** 的 `已驗收`（僅由證據支持之整數、不得超過 `約定`；`約定`＝`—` 之列不寫整數）、`達成度`（衍生）、`狀態`；基準區 `最近 align` 摘要行。
     - **兩種格式皆不得**改動：`約定`、預期功能與規格、驗收方式與必要前提、不可默默刪減的核心能力、ID／階段／成果名稱、`已確認變更` 列（這些只能經使用者確認的 `update`）。
  2. **每 ID 範圍**：只更新本次實際檢查的 ID；**不消費其他階段尚未檢查的交付**；每 ID 的 `最近 align` 記日期、HEAD 全雜湊、檢查範圍。**預設錨點**：從未 align 的 ID 以成果文件首次提交（`git log --diff-filter=A --follow --format=%H -- <文件> | tail -1`；文件尚未提交則用建立當下 HEAD）起算；**舊格式**以該列證據格內 `align:` 行之 HEAD40 為該 ID 錨點，**沒有列內記錄的 ID 視為從未 align**，不因全域摘要行更新而視為已檢查，全域摘要行之 HEAD 不作錨點（Rev B.2）；`git log <錨點>..HEAD` **僅協助列示候選交付**，不作資格條件、不代替功能證據，不以提交前綴或目錄排除將 docs-only／規則型成果整類排除。
  3. **直接回報**（對話正文，非只說文件已更新）：①單項結果（ID、`狀態`、`已驗收／約定`、一行證據與限制）；②各階段 `已驗收／約定` 計數（**不給跨階段平均或總百分比**；`無約定` 列不計入計數與比例，另列其 ID）；③主要缺口；④下一步；⑤真正需要使用者決策的事項（無則寫「無」）。取代現行第 5 步的「短版成果摘要與初判」；原「摘要必須回答」五問保留。
  4. **證據規則補充**：證據**來源**與**證明範圍**分開——文件、ledger、測試、完整場景不互相升格（例：ledger 登記事件不證其中調用的真實性；單元測試不等於整體功能；round 通過不提升證據強度）；精確數字須可定位核對（檔案:行號或指令）；搜尋未命中只限本次檢查範圍，不得宣稱全域不存在；已決事項先重用；結果清楚就退出，不反覆修表。既有「舊證據可沿用」等條保留。
- **約束**：現行「判斷用語」「尚不具備驗收條件」「路線與規格調整」三節文字不改義（Rev B.2：判斷用語節得追加 `無約定` 一條定義，既有用語不改義）；`align` 不得為使現況看起來符合而縮小核心需求。
- [x] 完成（2026-10-17；AI-5：align L170–L240（寫入邊界 舊 L184／新 L185／皆不得 L186、列內 align: 錨點樣式 L184、每 ID 範圍與首次提交錨點 L188–L192、直接回報 L197–、證據規則補充 L221–、`無約定` 定義入判斷用語）。）

### AI-6: 新增「被治理條款呼叫時」專節，並補述 邊界
- **檔案**: `.claude/skills/projman/SKILL.md`
- **現值（唯一）**：`## 路線與規格調整`（新節置於其前）與 `## 邊界`（補述）。
- **目標值**：新增 `## 被治理條款呼叫時（行為契約）`，開頭明寫：**本節只描述被某治理條款呼叫時的行為；本 Skill 不定義、不啟用該條款，也不驗證自動鏈**。內容：
  1. **候選集合**＝本檔 `### 成果文件定位規則` 的輸出；唯一合理候選才執行，零個或多個 → 不執行並以一行說明原因（根本沒有成果文件時不輸出）；**不複製、不改寫**定位邏輯。
  2. **只消費既有證據**（被收束票的 sv_report、commit、收束敘事、既有測試輸出）；不新增付費呼叫；預設不新增本地建置或測試，僅用唯讀命令（`git`／`grep`／`ls`）；需要新執行的項目標「未驗」並寫原因。
  3. **寫前檢查**：`git status --short -- <文件>` 非空（他人或他 session 未提交修改）→ 略過並報告；只寫該一份成果文件，不改其他檔案、產品需求、Registry 或 lifecycle。
  4. **保存**：僅當所呼叫之條款授予單檔保存時，以 `git commit -o <成果文件>` 提交（前綴由條款指定），且只含本次 align 產物；否則只回填並報告「已回填、未提交」。成果文件**未追蹤**時，`git commit -o` 必然以 pathspec 錯誤失敗（本地 probe CONFIRMED，2026-10-17）→ 直接報告「已回填、未提交（未追蹤）」，**不改用 `git add`**（Rev B.1）。**成果文件不要求寫入該提交自己的 hash**；commit hash 於對話回報，不為補 hash 製造補交。
  5. **失敗處置**：提交或寫入失敗 → 回報「已執行／已回填／未提交」與 dirty 路徑；不回滾原票、不還原檔案、不重試迴圈、不開 Gate incident，不回頭視為原票或下一張票未收束；如實列出已知限制（「寫前乾淨」只是單次觀察、不排除並行寫入；殘留 dirty 異動可能被下一張票的廣域提交帶走）。
  6. **回報項目與紀錄**：呼叫來源、Skill 實際呼叫、回填差分（`git diff --stat` 與各 ID 前後 `已驗收`）、結果摘要、保存結果、**Claude Code session id 與依序工具呼叫**。取得方式（本 session 已證實之最小途徑）：session id＝環境變數 `CLAUDE_CODE_SESSION_ID`；工具序列＝`~/.claude/projects/<專案目錄>/<session-id>.jsonl` 內依序之 `tool_use`（唯讀 `python -X utf8 -I` 讀取）；**任一取不到 → 該欄寫 `UNAVAILABLE`，不得事後編造、不得以記憶補寫**；不新增日誌系統。
  7. **自動／手動判別**：同一回合內、收束寫入完成後、沒有新的使用者訊息、就呼叫了 `Skill(projman)` 者，可**候選**記為「自動」並附上列證據；需使用者另外下令者記為「手動」（證明 align 可用，**不算**自動觸發驗證）；僅在文件自填「自動」、或只找到成果文件的提交，皆不足以升級；若成果文件存在對應的自動觸發驗證項（本議題為 `PM2-E`），將上列內容寫入其明細並標「自評，待審查確認」，由審查方確認；無法核對或未觸發 → 該項維持未驗。
  8. **不遞迴**：被條款觸發的 align 不是 Ticket 收束，不再觸發另一次 align。
  9. **邊界節補述**：`## 邊界` 現有「除成果文件外不寫入其他檔案」保留，並補一句「被條款呼叫時亦同」。
- **約束**：本節（自 `## 被治理條款呼叫時` 標題至下一個 `## ` 標題）不得出現「保證」「自癒」「必定補判」三詞（含否定形式，以免 grep 誤判；全檔既有他處的「不保證成功」不在此限、不得更動）；漏跑僅得稱「可能於後續相關收束時補判」；本節任何句子不得構成 Skill 自行啟動 `align` 的指示；不得引用尚不存在的條款文字或編號為既有。
- [x] 完成（2026-10-17；AI-6：專節 L242–L254（9 條）、邊界 L268–L270 補「被條款呼叫時亦同」；專節範圍禁用詞 grep 0 命中。）

### AI-7: 靜態檢核、隔離示例推演（非真實 Skill 執行）與收尾實錄
- **檔案**: `.claude/skills/projman/SKILL.md`（唯讀檢核）、`.claude/rules/ticket_pattern.md`（確認 provenance 列）、本票檔（收尾實錄）；scratch＝`tmp/PJMAN-SKILL-01/`（`.gitignore:52` 已排除；不得全域 glob 清理，僅清理本目錄）
- **動作**:
  1. 靜態：frontmatter `disable-model-invocation: false` 位元組不變；`grep` 清點三模式、`### 成果文件定位規則`、`--exclude-dir=archive`、新格式固定欄 `ID | 階段 | 成果 | 已驗收 | 約定 | 達成度 | 狀態`、V1–V6、`UNAVAILABLE`、`自評，待審查確認`、`無約定`、舊格式列內錨點樣式 `align: <日期> <HEAD40> 範圍 <ID>`（Rev B.2）皆存在；禁用詞 grep（`保證`、`自癒`、`必定補判`）於 `## 被治理條款呼叫時` 專節範圍內零命中（以 `sed -n '/^## 被治理條款呼叫時/,/^## 路線與規格調整/p'` 取範圍；全檔既有 `不虛構能力、不保證成功` 為修改前即存在之他處用語，不在檢核範圍）；表格欄數逐行一致；記錄 `wc -l .claude/skills/projman/SKILL.md`（500 行為精簡參考、非 gate：超出先去重，必要內容保留並回報原因，Rev B.3）、`description` 字元數 ≤250（6a 回流之 `SUPPORTED` 準則，非 gate；超出依 Error Handling 回報）；`git diff --stat` 僅涉 ALLOWED。
  2. 隔離示例推演（以 `tmp/PJMAN-SKILL-01/` 內虛構樣本，**逐項標註「示例推演，非真實 Skill 執行」**；**證據紀律（Rev B.1）**：指令類〔V1–V6、首次提交錨點、`git status` 檢查〕一律自最終 `SKILL.md` 逐字抽出後執行，不得改寫或另寫等價指令；判斷類〔寫入集合、只動 A、失敗報告格式〕每一步附最終版 `SKILL.md:行號`（first-hand），無行號之步驟不計證據；V1／V3 須於暫存 git repo 內執行（樣本先提交、再修改），因 `tmp/` 受主 repo 忽略）：依「狀態矩陣」逐列演練（見下），並含：舊格式樣本（取自 `autonomous-engineering-gate-outcomes.md` 之複本，**起始斷言**其表頭五欄、無 `已驗收`／`約定`）；新格式樣本（≥2 階段、含 `—`、`0`、部分通過各一，及 `約定`＝`—`／`狀態`＝`無約定` 一列）；V1–V6 對「刪 ID、改分母無確認列、達成度不符、`—` 改 `0`、加平均列、欄數不齊」六種破壞各自被抓出；校正階段 A 不改階段 B 的 `最近 align`；舊 5 欄 A／B 局部校正（A 列內 `align:` 行更新、B 列不變且不因全域摘要更新視為已檢查，Rev B.2 F8）；暫存 git repo 驗首次提交錨點（c1 提交文件、c2／c3 後續提交 → 候選為 `c1..HEAD`，非全歷史）；被條款呼叫章節的「dirty 略過」「零／多候選不猜」「提交失敗報告格式」。
  3. 收尾實錄（寫入本票檔）：AI-0 結果、各 AI 結果、定位交叉核對證據欄（`檔案:行號`；原 `[可達性]`，Rev B.1 改標 `[存在性]`）、示例推演結果與限定交付聲明（真實 Skill 端到端屬 T02、自動鏈屬 T03、本票不宣稱 PM1 通過）、External-review 欄最新狀態、`ticket_pattern.md` provenance 列確認。
- **約束**：推演樣本與輸出只放 `tmp/PJMAN-SKILL-01/`；不修改 `plans/roadmap/` 任何檔；推演結果與規格不符時，如實記為落差並回頭修正 Skill 文字，**不得改寫規格去符合推演**。
- [x] 完成（2026-10-17；AI-7：靜態檢核、隔離示例推演（非真實 Skill 執行）與收尾實錄已完成，見下「收尾實錄」。）

## Hard Constraints
1. `disable-model-invocation: false` 位元組不變；`name` 不變。
2. 不新增腳本、hook、狀態庫、鎖、常駐服務、日誌系統或第二份可編輯資料／JSON（含 `update` 寫前基準之快照檔；未追蹤或 dirty 時之基準只存於當次上下文與對話摘要，Rev B.2；亦不新增鎖或並行狀態系統，Rev B.3）；核對指令僅為 `SKILL.md` 說明文字中的命令片段。
3. `align` 寫入集合只能是 AI-5 所列（舊格式／新格式各一組；舊格式之列內 `align: <日期> <HEAD40> 範圍 <ID>` 錨點行屬該列「實際成果與證據」格內，不構成擴張，全域「最近 align」只作摘要、不作錨點）；**不得**擴張至 `約定`、預期規格、驗收條件、核心能力、ID、已確認變更。
4. 「被治理條款呼叫時」專節不得啟用或暗示已存在任何自動呼叫條款；不得宣稱自動鏈已驗證；漏跑僅稱「可能於後續相關收束補判」。
5. 示例推演一律標明非真實 Skill 執行，不得寫成 production 路徑已驗；真實端到端執行屬 T02。
6. session id、工具序列只能取自 `CLAUDE_CODE_SESSION_ID` 與 transcript；取不到寫 `UNAVAILABLE`；不得編造。
7. 新舊格式辨識不到、或標記並存時不寫入；舊格式文件不批次遷移、不在 `align` 時自動遷移。
8. 不呼叫付費模型或外部服務；PPSS 外審未經 Akira 裁決不執行。
9. 本票不修改任何 FORBIDDEN 檔；`ticket_pattern.md` 之 diff 僅限 PJMAN provenance 一列。
10. 示例推演發現的規格落差，須修 Skill 文字或如實記為落差，不得靠縮小 parent 已裁決需求使之通過。

## Error Handling
- AI-0 任一錨點命中數與票面不符 → 依三分流分類；`projman/SKILL.md` 被他人改動而與 `TARGET_BLOB` 不符且歸因非本票 → 記錄後以現值續作，並重算 AI 內行級現值；`ALREADY_EXECUTED_PENDING_CLOSURE` → 停止。
- `git check-ignore -v` 對 `.claude/skills/projman/SKILL.md` 或 `.claude/rules/ticket_pattern.md` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）。
- `ticket_pattern.md` 與 `HEAD` 之 diff 含 PJMAN 列以外任何變更 → 停止回報，不自行還原他人變更。
- 需要修改 FORBIDDEN 檔（例如接線 `sv`／`ssot-governance.md` 才能使 Skill 章節成立）→ 停止回報；該交付屬 T03。
- AI-7 核對指令在 Git Bash 不可執行（`awk`／`python -X utf8 -I` 缺失）→ 停止回報；**不得**以新增腳本或 parser 框架補救。
- 示例推演顯示 parent 已裁決的行為無法以現行 Skill 文字表達（例：新舊格式寫入集合互相衝突）→ 停止回報並附最小反例，**不得**自行縮小需求。
- `CLAUDE_CODE_SESSION_ID` 或 transcript 在本 session 不可讀 → 僅影響示例推演中該項的取證宣稱（標 `UNAVAILABLE`），不阻擋文字交付。
- 最終 `projman/SKILL.md` ≥500 行（Rev B.3：500 行為精簡參考、**非 execution gate**）→ 先去除重複敘述；去重後仍超出者保留必要內容，於收尾實錄回報各節行數與保留原因；**不因行數停工**、**不得**自行拆出新檔（拆檔會超出 ALLOWED）、**不得刪減需求**。
- 發現本票需要 `projman-outcomes.md` 才能驗證 → 停止回報；建立該文件屬 T02。

## Acceptance Criteria

> **Rev B.1 AC 修訂閉環（RGR cycle 1，2026-10-17）**：第 6 條 AC 由 `[可達性]` 改標 `[存在性]`（只做位置交叉核對；Skill 實際執行路徑屬 T02）；第 7–11 條示例推演 AC 補證據紀律（指令類自最終 `SKILL.md` 逐字抽出執行；判斷類每步附 `SKILL.md:行號`，只驗規格可表達性；V1／V3 於暫存 git repo 執行）；第 12 條補「HEAD 已含該列」分支。**兄弟 AC 重驗**：第 1–5 條存在性 AC 的母體（最終 `SKILL.md`）與判準不變；第 7–11 條共用同一批樣本與同一份 `SKILL.md`，預期不變，只收緊證據來源，沒有任何一條被移除、縮減或豁免；第 13 條收尾實錄改引用「定位交叉核對證據欄」（AI-7 #3 同步）。**Rev B.2 已裁（Akira 2026-10-17）**：F8 第一案、F3 寫入前現值基準、F5「無約定」。AC 變動：舊格式 align AC 期望改為「本次 ID 列證據格（含列內 `align:` 行）／落差與下一步／全域摘要」三處；新增舊 5 欄 A／B 局部校正 AC；新格式規範 AC 增 `無約定`；update 存在性 AC 增寫前現值基準；V1–V6 推演 AC 增 dirty 樣本與 `無約定` 列。**兄弟 AC 重驗**：新格式 A／B 校正 AC 的寫入集合不變（F8 只改舊格式）；V1–V6 推演 AC 原有六種破壞的預期不變，只增案例；存在性 AC（三模式、定位規則、專節、provenance）母體與判準不變；被條款呼叫之推演 AC 的 dirty 略過預期不變（F3 只適用手動 update）；首次提交錨點 AC 不變（舊格式無列內記錄之 ID 同樣以首次提交為錨點）。沒有 AC 被移除、縮減或豁免。**Rev B.3**：第一條 AC 的行數檢核由「`wc -l` <500」改為「記錄行數；≥500 附去重說明與保留原因」（精簡參考、非 gate）；update 存在性 AC 改用「依使用者修訂授權」口徑。兄弟 AC 重驗：第一條 AC 其餘判準（三模式、用法行、`disable-model-invocation`、`description` ≤250）不變；其他存在性 AC 的母體不變；V1–V6 推演 AC 的 dirty 樣本案例仍以寫前現值為基準，預期不變。
- [x] **[存在性]** `projman/SKILL.md` 含 `base`、`update`、`align` 三模式入口與用法行；缺少或不支援模式時「只顯示用法並停止」的句子存在；`argument-hint` 與 `description` 皆列三模式；`^disable-model-invocation: false$` 恰 1 次；`wc -l` 已記錄（500 行為精簡參考、非 gate；≥500 時附去重說明與必要內容保留原因，Rev B.3）、`description` ≤250 字元。
- [x] **[存在性]** 節奏區現有「不得自動掛到每張 Ticket 完成、每次對話、交接或小修改」整句仍存在（位元組不變）；`## 被治理條款呼叫時` 專節存在且開頭含「不定義、不啟用該條款，也不驗證自動鏈」語意；專節範圍內 grep `保證｜自癒｜必定補判` 零命中（範圍取法見 AI-7 #1；全檔既有他處用語不計）。
- [x] **[存在性]** 標題字面 `### 成果文件定位規則` 恰 1 次，其內含 `--exclude-dir=archive`、「使用者明確指定該路徑者除外」、「票前綴只是可選定位線索」、「議題全名」；專節以該章節為候選集合來源且不複製定位指令。
- [x] **[存在性]** 新格式規範含：`project_key`／`project_label`／`topic_key`／`topic_label` 分開、固定摘要欄 `ID | 階段 | 成果 | 已驗收 | 約定 | 達成度 | 狀態`、`## 明細` 與每 ID `### <ID>`、`—`＝未盤點≠`0`、`約定`＝`—` 之列 `已驗收`／`達成度` 為 `—` 且 `狀態`＝`無約定`（與 `尚不具備驗收條件` 分開，不計比例，Rev B.2）、無總計／平均、ID 不編入票名票號、格式辨識三分（新／舊／無法辨識或並存）。
- [x] **[存在性]** `## update` 節含：真實修訂才使用、不為驗證製造修訂、保留 ID／證據、新增成果不繼承完成狀態、分母變動須 `已確認變更` 列、核心縮減與成本／權限取捨須明確確認、未追蹤或 dirty 之手動 update 依使用者對指定文件的修訂授權、以完整寫前現值為基準並保留未受影響內容（無衝突標記只是格式前提；已知並行衝突略過並回報；不要求提交、不新增快照檔、不新增鎖或狀態系統）、V1–V6 六項核對指令。
- [x] **[存在性]**（Rev B.1 由 `[可達性]` 改標：本條只做位置交叉核對；Skill 實際執行路徑的可達性屬 T02。本票只改指令文字，不觸 runtime／guard／gate，On-Device 規則不適用）V1–V6 每一條指令的輸入（舊格式樣本與新格式樣本）、輸出與觀察者可逐一指出；`align` 寫入集合（舊／新各一組）在 Skill 文字中的位置與 AI-5 目標值逐項相符。**producer／consumer 證據欄（執行後回填 `檔案:行號`）**：V1–V6 指令位置＝`.claude/skills/projman/SKILL.md:139`（`ids()`）、`:144`（V1）、`:148`（V2）、`:152`–`:153`（V3 `den()`＋`diff`）、`:157`（V4）、`:161`（V5）、`:165`（V6）；舊格式樣本輸入＝`plans/roadmap/autonomous-engineering-gate-outcomes.md:32`（五欄表頭）〔複本 `tmp/PJMAN-SKILL-01/w/old.md`〕；新格式樣本輸入＝`tmp/PJMAN-SKILL-01/w/new.md`（推演腳本內 `NEW` 常數產生之虛構樣本，scratch、未版控）；寫入集合位置＝`.claude/skills/projman/SKILL.md:184`（舊）、`:185`（新）、`:186`（皆不得）。
- [x] **[行為性]** 〔示例推演，非真實 Skill 執行；V1–V6 指令一律自最終 `SKILL.md` 逐字抽出後執行，不得改寫或另寫等價指令，紀錄附各指令來源 `SKILL.md:行號`；V1／V3 於暫存 git repo 內執行（樣本先提交、再修改）〕given 新格式樣本（≥2 階段，含 `—`／`0`／部分通過，及 `約定`＝`—`／`狀態`＝`無約定` 一列）／expect V1–V6 全過；given 六種破壞（刪 ID、改分母無確認列、達成度不符、`—` 改 `0`、加平均列、欄數不齊）／expect 各自被對應核對抓出；given 寫入前已 dirty 之新格式樣本（Rev B.2，F3）／expect V1／V3 以對話所列寫前現值摘要為基準，仍抓出刪 ID 與改分母無確認列。
- [x] **[行為性]** 〔示例推演，非真實 Skill 執行；判斷類推演，每一步附最終版 `SKILL.md:行號`（first-hand）為依據，無行號之步驟不計證據；本項只驗規格可表達性，不證 Skill 實際行為（屬 T02）〕given 舊 5 欄樣本複本（起始斷言：五欄表頭、無 `已驗收`／`約定`）與一次 align 回填／expect 變更僅限本次檢查 ID 之列的「實際成果與證據」（含列內 `align: <日期> <HEAD40> 範圍 <ID>` 行）「落差與下一步」與基準區「最近 align」摘要三處，其餘位元組不變。
- [x] **[行為性]** 〔示例推演，非真實 Skill 執行；判斷類推演，每一步附最終版 `SKILL.md:行號`（first-hand），無行號之步驟不計證據；只驗規格可表達性，不證 Skill 實際行為（屬 T02）；Rev B.2 F8〕given 舊 5 欄樣本複本（起始斷言：A 列證據格已有 `align:` 行錨點 X、B 列無列內 `align:` 行）、只校正 A／expect A 列 `align:` 行原位更新為新 HEAD40 與範圍 A；B 列位元組不變且仍無列內記錄；全域「最近 align」摘要行更新後，B 仍視為從未 align（下次 B 之候選範圍以成果文件首次提交為錨點，而非全域摘要之 HEAD）。
- [x] **[行為性]** 〔示例推演，非真實 Skill 執行；判斷類推演，每一步附最終版 `SKILL.md:行號`（first-hand），無行號之步驟不計證據；只驗規格可表達性，不證 Skill 實際行為（屬 T02）〕given 新格式樣本、校正階段 A／expect 只有 A 的明細三處、摘要列 `已驗收`／`達成度`／`狀態` 與基準區 `最近 align` 摘要行變動，B 的 `最近 align`、`約定`、預期規格不變，`已驗收`≤`約定`。
- [x] **[行為性]** 〔示例推演，非真實 Skill 執行；暫存 git repo；首次提交錨點與候選列示指令一律自最終 `SKILL.md` 逐字抽出後執行，紀錄附來源 `SKILL.md:行號`〕given 文件於 c1 提交、c2／c3 為後續提交／expect 從未 align 階段之候選交付範圍為 `c1..HEAD`，不是全歷史；docs-only 的 c3 仍被列為候選。
- [x] **[行為性]** 〔示例推演，非真實 Skill 執行；`git status --short -- <文件>` 等指令自最終 `SKILL.md` 逐字抽出執行，判斷步驟附 `SKILL.md:行號`（first-hand），無行號之步驟不計證據；只驗規格可表達性，不證 Skill 實際行為（屬 T02）〕given 被條款呼叫的情境：文件 dirty／expect 略過並報告；零或多個候選／expect 不執行並一行說明；提交失敗／expect 報告「已執行、已回填、未提交」與 dirty 路徑且不回滾原票；`CLAUDE_CODE_SESSION_ID` 或 transcript 不可讀／expect 該欄 `UNAVAILABLE`、不得編造。
- [x] **[存在性]** `.claude/rules/ticket_pattern.md` 與 `HEAD` 之 diff 僅為末端新增 1 列（`+1`／`-0`）——**或**（Rev B.1，對應 AI-0 #3「已被他途提交」分支）`HEAD` 已含該列恰 1 次且本票對該檔 diff 為空——該列首欄 `` `PJMAN-` ``、共六個資料欄（family／首票 TICKET_ID／代表票／來源路徑／補登日期／追認依據，與他列格式一致）、`PJMAN` 於該檔恰 1 列；regex 行（`commit-hook=`）位元組不變。
- [x] **[存在性]** 收尾實錄載明：示例推演＝非真實 Skill 執行；真實 `base`／`align` 端到端屬 T02、自動鏈屬 T03，本票不宣稱 PM1 通過或自動鏈已驗；External-review（6a）狀態如實記載。
- [x] `git check-ignore -v` 對兩個 ALLOWED 檔零命中
- ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`.claude/skills/projman/SKILL.md`、`.claude/rules/ticket_pattern.md`；票檔搬移與 Dashboard 慣例同步屬 Pipeline Artifact／收尾，不計本條）

## 狀態矩陣（AI-7 示例推演之範圍；開票時建立，僅涵蓋可達且與契約相關之組合）

| 格式 | 模式 | 文件狀態 | 預期 |
| --- | --- | --- | --- |
| 無文件 | base | 不存在 | 於 `plans/roadmap/<議題全名>-outcomes.md` 建新格式文件 |
| 無文件 | update／align | 不存在 | 說明並詢問一次；不自行建立 |
| 舊 5 欄 | base | 存在 | 不無聲覆蓋；只補使用者明示部分；不遷移 |
| 舊 5 欄 | update | 存在 | 保留 ID／證據於舊結構修訂；僅使用者明示時遷移 |
| 舊 5 欄 | align | 存在 | 寫入集合＝本次 ID 列之實際成果與證據（含列內 `align:` 錨點行）／落差與下一步／基準區最近 align（僅摘要）；無列內記錄之 ID 視為從未 align |
| 新格式 | base | 存在 | 不無聲覆蓋原目標與核心能力 |
| 新格式 | update | 存在 | V1–V6 全過；分母變動有確認列 |
| 任一 | update（手動） | 未追蹤或 dirty | 依使用者對該文件之修訂授權進行；以寫前完整讀取之現值為 V1／V3 基準並於對話列精簡摘要；保留未受影響內容；不要求提交或還原、不新增快照檔 |
| 任一 | update（手動） | 含衝突標記，或已知並行衝突 | 衝突標記：不寫入，說明並詢問一次；已知並行衝突：略過該文件並回報；不新增鎖或狀態系統 |
| 新格式 | align | 存在 | 新格式寫入集合；僅本次檢查 ID |
| 無法辨識／標記並存 | 任一 | 存在 | 不寫入；說明並詢問一次 |
| 任一 | 被條款呼叫 | dirty | 略過並報告 |
| 任一 | 被條款呼叫 | 乾淨且唯一 | 只寫該文件；保存依條款授權 |
| 任一 | 被條款呼叫 | 零或多候選 | 不執行；一行說明原因 |

## 收尾實錄（Agent，QA／SV 前；2026-10-17）

### 1. 派工與基線
- 派工：`/normal PJMAN-SKILL-01` activation 成功（owner `e024aeab…`；`DIFF_REF=2bfae8fb`）。開工時票檔為 **Rev B.3**（Rev B 之後由 RGR 執行與 Akira 裁決修訂：F8 舊格式列內錨點、F3 寫前現值基準、F5 `無約定`、500 行為精簡參考）；本次實作一律依 Rev B.3 版面。registry：`TICKET_RIGOR`→`IN_PROGRESS`（本次寫入）。
- 票面使用者指示維持：QA／SV／Cross-Review 不豁免；`assembleDebug` 為 docs 型 N/A；provenance 隨票保存（HEAD 尚未含該列 → 採**非零 diff 分支**，不採零 diff 分支）；T01 不啟用自動 align，不宣稱 T02／T03 已驗證。

### 2. AI-0 結果
錨點 26／26 各恰 1 次（含 `disable-model-invocation: false`）、`projman/SKILL.md` blob `fa74fd20`、`i/lf w/lf`、153 行、`git check-ignore -v` 對兩檔 0 命中；`ticket_pattern.md` 與 `HEAD` 唯一差異＝末端新增 PJMAN 1 列（+1／−0），HEAD 內 `PJMAN` 0；舊格式樣本仍為 5 欄且無 `已驗收`；`grep -rl "^## 階段成果表" plans/ --exclude-dir=archive` 輸出＝樣本 1 檔；`2bfae8fb..HEAD` 於兩標的零 diff；`awk`／`python -X utf8 -I`／`git 2.54.0.windows.1` 可用；`tmp/PJMAN-SKILL-01/` 原不存在。

### 3. 實作與靜態檢核（AI-1～AI-6、AI-7 #1）
- `projman/SKILL.md`：153 → **271 行**（`wc -l`，500 行精簡參考內、非 gate）；`description` **122 字元**（≤250）；`name`／`disable-model-invocation: false` 位元組不變；`git diff --stat`：SKILL.md 166 行異動（+142／−24）、`ticket_pattern.md` +1。
- 靜態：三模式用法行、`### 成果文件定位規則`（恰 1）、`--exclude-dir=archive`、「使用者明確指定該路徑者除外」、「票前綴只是可選定位線索」、「議題全名」、固定摘要欄、`project_key／project_label／topic_key／topic_label`、`## 明細`、`無約定`、`UNAVAILABLE`、`自評，待審查確認`、列內錨點樣式、V1–V6、「不定義、不啟用該條款，也不驗證自動鏈」皆存在；專節（`## 被治理條款呼叫時` 至 `## 路線與規格調整`）內 `保證｜自癒｜必定補判` 0 命中；節奏整句 `grep -cF` 命中 1（位元組不變）；表格欄數一致；程式碼圍欄皆具語言標記；**無 `$數字`、`${`、`!` 後接反引號之字樣**（見偏離②）。
- 定位交叉核對證據欄（`[存在性]`）：見 AC6 欄位。

### 4. 隔離示例推演（**示例推演，非真實 Skill 執行**；scratch＝`tmp/PJMAN-SKILL-01/`，結果檔 `results.txt`）
指令類一律自最終 `SKILL.md` 以「找行→逐字取出」方式抽出執行（來源行：ids `:139`、V1 `:144`、V2 `:148`、den `:152`、V3 `:153`、V4 `:157`、V5 `:161`、V6 `:165`、首次提交錨點 `:191`、候選列示 `:192`、定位規則 `:46`、寫前檢查 `:248`、保存 `:249`、列內錨點定位 `:184`）；判斷類步驟附行號（寫入集合 `:184／:185／:186`、每 ID 範圍 `:188`、候選集合 `:246`、保存 `:249`、失敗處置 `:250`、回報項目 `:251`）。V1／V3 於暫存 git repo 內（樣本先提交、再修改）執行。
| 案例 | 結果 |
| --- | --- |
| A 新格式樣本（≥2 階段；含 `—`、`0`、部分通過、`約定`＝`—`／`無約定`） | V1–V6 全過（輸出皆空） |
| B1 刪 ID | V1 輸出 `D1-B` |
| B2 改分母（無確認列） | V3 輸出 `D1-A=3`→`D1-A=4`；V4 為空（刻意同步達成度，隔離 V3） |
| B3 達成度不符 | V4 `D1-A: 達成度與計數不一致` |
| B4 `—` 改 `0` | V4 `D2-A: 已驗收為整數時狀態不得為未盤點` |
| B5 加平均列 | V5 輸出該列 |
| B6 欄數不齊 | V6 `14: 欄數7≠8` |
| C 寫前 dirty（`git status` 為 `M`） | 以對話所列寫前現值摘要為基準：V1 `D1-B`、V3 `D1-A=3`→`4` 與缺 `D1-B`（僅『基準來源』由 `git show HEAD:` 換為摘要檔，其餘指令文字不變） |
| D 舊 5 欄樣本複本（起始斷言：5 欄、無 `已驗收`） | V1 空；V6 空（含 `local\|advisor\|r0` 反引號豎線之舊檔不誤報）；align 式編輯後變更行＝2（S1-R1 列兩欄＋基準區最近 align），其餘位元組不變 |
| E 舊格式 per-ID 錨點 | 更新 A（S1-R1）列內 `align:` 行原位換 HEAD40；全域摘要更新後，列內錨點定位輸出僅 S1-R1，S1-R5 無列內記錄＝視為從未 align，錨點取首次提交 |
| F 新格式只校正 A | 3 個 hunk（摘要列、A 明細、基準區最近 align）；B 列與明細位元組不變、B 的 `最近 align` 仍「無」；V2／V4／V5／V6 全空 |
| G 首次提交錨點（c0 其他、c1 提交文件、c2 程式、c3 docs-only） | 錨點＝c1；候選＝c3、c2（不含 c0／c1，docs-only c3 仍列入） |
| H 被條款呼叫 | 候選 1／2／0 個各自輸出 1／2／0 檔（archive 已排除）；`git status --short -- DOC` 乾淨為空、dirty 為 `M …`（→略過並報告）；未追蹤 `git commit -o` 以 pathspec 錯誤失敗（→報告「已回填、未提交（未追蹤）」，不改用 `git add`）；`env -u CLAUDE_CODE_SESSION_ID` 時 session id／工具序列皆 `UNAVAILABLE`、id 存在但 transcript 路徑不存在時僅工具序列 `UNAVAILABLE` |
| 補充負向（V2／V4） | V2：多出明細／缺明細各抓出 ID；V4：`約定`＝0、`無約定` 誤用於正整數列、`約定`＝`—` 但狀態錯、`已驗收`>`約定` 各被抓出；合法樣本為空 |

### 5. 偏離與發現（照實）
- ①票面於開工前已由他人／他 session 修訂至 Rev B.3（本 session 的 RGR 執行曾被使用者中斷；registry 的 `rigor` 欄與 `.tmp/rigor-gate/RGR_PJMAN-SKILL-01_20261017.md` 由該輪留下，**本次未重跑 RGR、不引用其為本次驗證證據**）。
- ②Skill 引數替換風險（`HYPOTHESIS`，未驗證）：6a 回流稱 skill 內容可有 `$N`／`${…}` 替換、`!` 加反引號可能為動態注入；故 SKILL.md 內 awk 欄位一律寫 `$(n)`、不含 `${`、不含 `!` 緊接反引號；`$(n)` 在 Git Bash awk 下實測可用（推演 A–H 皆以該寫法執行）。真實 Skill 載入後是否被替換，屬 T02 觀察項。
- ③V3 以兩行（`den()` 定義＋`diff`）表達，V1／V3 之 HEAD 基準前提與 dirty 改用摘要基準皆寫入 SKILL.md；摘要基準為對話層動作，無指令文字可逐字抽出，已於案例 C 明示。
- ④推演之『session id 不可得』以 `env -u` 模擬，非真實跨 session／headless 取證。

### 6. `ticket_pattern.md` provenance 確認
`git diff -U0 HEAD -- .claude/rules/ticket_pattern.md`＝+1／−0；`PJMAN` 於檔內恰 1 列；該列與 `TKA-` 列同為 8 個 `|` 分隔欄（6 資料欄）；`commit-hook=` 行與 `HEAD` 逐字相同。尚未提交，隨本票 Auto Commit 一併提交。

### 7. 限定交付聲明與 External-review 現況
- 本票只改指令文字；示例推演＝**非真實 Skill 執行**；真實 `base`／`align` 端到端屬 T02、自動鏈屬 T03；**不宣稱 PM1 通過，不宣稱自動鏈（PM2）已驗證，不宣稱 T02／T03 已驗證**。`update` 真實執行與 `${CLAUDE_SESSION_ID}` 替換一致性維持未驗。
- External-review：6a **EXECUTED**（2026-10-17，`rsch-20261017-7619d266f3`，回件為頁面摘錄、多為 `SUPPORTED`，不代表官方契約確認、不構成品質 PASS）；`git commit -o` 語義已本地 probe 升 `CONFIRMED`；本票未重送外審（防遞歸律）。
- 待辦於下游：`/qa`（Step 0 DIFF_REF 回填與 scope、靜態檢查、Step 4.5 提交）→ `/sv`（含 Cross-Review，不豁免）→ closeout。

### 8. QA／SV／Cross-Review 結果（2026-10-17，R1／L1）

- **QA**：Step 0 PASS；Step 1–3.5 N/A（docs preset，`assembleDebug` 維持 N/A）；Step 4.5 Auto Commit `0d8ce72d`（ALLOWED 2 檔，`[PJMAN-SKILL-01]`）。
- **SV**：preflight（`structural_verify.sh` A/B/C/D）PASS；`.tmp/sv_report.md` 如實揭露 MISSING-INTEGRATION-TEST（限定交付）、新格式路徑 PARTIAL（尚未真實執行）、自動呼叫無 producer。
- **R0**：FORWARD（NORMAL，無 override；記錄 `.tmp/fail-advisor/R0_PJMAN-SKILL-01_20261017_235804.md`，非 `tmp/` 耐久證據，結論已複製於此）。
- **Codex（機械輪次 R1）**：第 1 次 **REPORT_FIX**（Stage B③ 將新格式 producer 表述為「不存在」，應改列 (A) 已定義 production writer）→ 依 Gate 開 incident INC-001，僅修 `.tmp/sv_report.md`（改引 `SKILL.md:54–86,100,114`，保留「尚未真實執行」揭露，不增新事實；Codex 引 `:111` 與實際不符，採實際行號）→ 第 2 次 **ADVISORY**（exit 0）：①被治理條款呼叫觸發路徑 missing-wiring（已明載、不宣稱 T03／PM2）；②V4 未驗 `狀態` 值域完整 enum（`SKILL.md:165`；**未修**，屬驗證強化、ADVISORY；留 T02 實跑時一併觀察，不宣稱 V4 已抓此類錯誤）〔**已處置（2026-10-18）**：V4 已補七值集合檢查並驗證，修補與審查依據見 §9〕。Gate ledger：INC-001 executor 1 輪 pass、closed；無 pending human。
- **未做**：Step 10 Done 標記與 `PROGRESS.md` Dashboard 同步（PROGRESS.md 已達 80 行上限且非 ALLOWED）延至下一輪 `/compact`，不另開 DOCSYNC 票。Traycer Workflow N/A → `reviews.traycer=N_A`。
- **限定交付不變**：僅 Skill 指令文字＋provenance 一列；示例推演非真實 Skill 執行；不宣稱 T02／T03／PM1／自動鏈已驗。

### 9. REVIEW 內同票修補：V4 狀態七值集合（2026-10-18，Akira 裁決；不另開票、不改 AC 成果要求）

- **缺口（第一版 Cross-Review ADVISORY 項）**：V4 指令對「數值與達成度正確、但 `狀態` 不屬量化規則七值集合」的列（如 `完成`）輸出為空，說明文字亦未要求；SV 報告未宣稱已抓此類錯誤。
- **修補**：commit `86521a8f`（`[PJMAN-SKILL-01]`，僅 `.claude/skills/projman/SKILL.md`，+2／−2）。`SKILL.md:155` 說明補「`狀態` 須屬上述量化規則之七值集合，值域外輸出違規列」；`SKILL.md:157` 指令於分支鏈最前加 `st !~ /^(未盤點|符合|部分符合|不符合|未驗|尚不具備驗收條件|無約定)$/` → 輸出 `狀態須屬七值集合`。七值與 `SKILL.md:96` 相同，**未改合法值域**；原計數、比例、`—`／`0`、`無約定`／`未盤點` 各分支位元組不變。靜態不變量不變（271 行、description 122、`disable-model-invocation: false` 恰 1、節奏整句 1、專節 0 命中禁語、無 `$數字`／`${`／`!`+反引號）。
- **局部驗證（非真實 Skill 執行；scratch 結果檔 `tmp/PJMAN-SKILL-01/results_v4fix.txt`，7 日清理，結論已複製於此）**：V4 指令自最終 `SKILL.md`（新）與修補前 `HEAD`（舊）逐字抽出，對同一 25 案差異比對，**25／25 符合預期、0 失敗**：①目標 7 案（非法狀態 `完成`／`已完成`／`done`／空白／`部分符合!`，以及 `約定`＝`—`、`已驗收`＝`—` 列配非法狀態）新指令皆輸出違規列，舊指令對前 5 案輸出為空；②合法 8 案（七值各一例的正確組合＋基準樣本）新舊皆空，無誤報；③既有分支 10 案（`約定`＝0、`無約定` 誤用、`約定`＝`—` 配其他狀態、已驗收>約定、達成度不符、整數卻標未盤點、`—` 卻標符合、`約定`＝`—` 但已驗收有值、`約定`／`已驗收` 非整數）新舊輸出逐字相同。驗證腳本自身缺陷（首次把 `"$DOC"` 多附一次致每列重複輸出）已修後重跑，上列為修後結果。**未對真實成果文件執行 V4**（repo 內尚無非樣本新格式成果文件，屬 T02）。
- **Cross-Review（範圍 `d871bfc7..86521a8f`，僅本修補 diff）**：R0 **FORWARD**（NORMAL；記錄 `.tmp/fail-advisor/R0_PJMAN-SKILL-01_20261018_010426.md`）；Codex 第 1 次 **REPORT_FIX**（SV 報告將 producer 判 (A)，但證據 `SKILL.md:54–86,100,114` 在本輪窄範圍 diff 之外，應判 (B)；意見成立，僅修 `.tmp/sv_report.md` 措辭，不增新事實）→ 第 2 次 **ADVISORY**（exit 0；①局部 25 案未驅動真實 `/projman`，`MISSING-INTEGRATION-TEST` 已揭露；②「被治理條款呼叫」路徑尚無 caller，既有 missing-wiring，T03 承接）。**receipt 對應修正後版本**：`TICKET=PJMAN-SKILL-01 VERDICT=ADVISORY DIFF_SCOPE=d871bfc7..86521a8f COMMIT=86521a8f4ad9b6773686b80249c01ef371be9eb5 TS=20261018_010516`（`.tmp/cross_review_pass/PJMAN-SKILL-01`，非版控，已複製於此）；第一版 receipt 另存（`DIFF_SCOPE=2bfae8fb..0d8ce72d COMMIT=0d8ce72d… VERDICT=ADVISORY TS=20261017_235953`）。state 未手動清除（ADVISORY 後由腳本清）、未繞過上限（REPORT_FIX 計數 1／2）、未重跑無關研究（無 PPSS）。
- **Gate 入口檢核（如實揭露）**：`gate_ledger.py check qa --ticket PJMAN-SKILL-01` 回 exit 4（`not mutable (loc:deliver)`）——票為 REVIEW 並位於 `deliver/`，在 Gate 適用範圍（非終態票）之外；未偽造 ledger 事件或 receipt，未把票退回 `Pending/` 或改 lifecycle 以通過檢核；本輪無新 Gate incident。
- **限定交付不變**：僅 Skill 指令文字＋provenance 一列；局部驗證非真實 Skill 執行；新格式 writer 尚未真實執行（T02）；被治理條款呼叫路徑無 caller（T03，**不在本票補做**）；不宣稱 PM1／自動鏈／T02／T03 已驗證。
- **收束權限**：執行端回報可結案（lifecycle 維持 `REVIEW`，`reviews={qa:PASS,sv:PASS,codex:PASS〔映射；實際 verdict＝ADVISORY〕,traycer:N_A}` 不變）；轉 `CLOSED` 限有權審查方（SMTR/CSO）核對 required reviews 與票檔／冊面一致後執行並 read-back。本票不自行連開 T02／T03。
