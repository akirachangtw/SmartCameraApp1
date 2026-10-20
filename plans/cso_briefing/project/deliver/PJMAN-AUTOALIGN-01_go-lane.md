# PJMAN-AUTOALIGN-01: 於 ssot-governance.md Transition Table 新增唯一 GO 列並於 sv Step 11 加一行引用，使 Agent closeout 本次呼叫實際完成後、符合條件的 Projman align 依狹義授權實際執行、單檔保存並直接回報（含 :11 一行純文字更正）

**Revision**: Rev A.2（2026-10-20；Akira 收準裁決，不回 plan、不另開票、不重跑完整 RGR，4a 計數＝2：①保留 AC-4／AC-5 改標，撤回「裁決⑤明示豁免可達性」誤引，統一為分階段驗收〔票內驗條款／引用／靜態一致性／隔離案例；真實接續依收束後正常判讀與呼叫結果由 REVIEW→CLOSED 審查方確認，未觸發或證據不足仍未驗〕；②保留 F2／F3／F4 修補與既有局部重驗；③6a 不列 Akira 待決，依 corpus 核對、既有證據重用與 Gate 研究授權處理；④`Skill(projman)` 許可改列首次觸發觀察，不改 settings、不現在另開票，未列 allow 不推定無法呼叫；⑤失敗回報依實際進度分「未完成呼叫」／「回填未完成」／「已回填未提交」／「已提交」）。Rev A.1（2026-10-20；RGR cycle 1 修補，4a 計數＝1；報告 `.tmp/rigor-gate/RGR_PJMAN-AUTOALIGN-01_20261020.md`：F1 AC-4／AC-5 由 `[可達性]` 改標 `[存在性]`，U1 補 On-Device 處置〔原寫「明示豁免」，Rev A.2 撤回改為分階段驗收〕；F2 AI-5／AC-6／AC-7 判斷類步驟逐步附 `檔案:行號`；F3 證據分級衝突處置補第 6 點〔Skill L35 與新列之關係〕、新列「可檢查點」補出處；F4 AC-3／AC-8／`git diff --stat` 綁定 `DIFF_REF..DIFF_END` 範圍）。Rev A（2026-10-20，首版；4a 計數＝0）。

**檔案治理／來源說明**：本票為 Plan-backed 單票（plan-local item `T03`，僅留於本來源區）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261017-projman-plan.md`（Rev C，2026-10-17；`.tmp` 非長期載體，下列來源資訊已內嵌，本票於該檔消失後仍可獨立閱讀）；Parent artifact 狀態＝header 實讀 `Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`／`Revision: Rev C`；Parent 完整讀取時間＝2026-10-20 13:3x–13:4x（本地）；Parent evidence status＝SUPPORTED（已核可規劃不代替本票當前本地取證，下列「證據分級」為本次 live 直讀）。上游正式票＝`PJMAN-SKILL-01`（原 plan-local `T01`）、`PJMAN-OUTCOMES-01`（原 plan-local `T02`），皆已 `CLOSED`。使用者開票指示日期＝2026-10-20，ticket 模式、`allow_new_topic=false`、使用既有 `Projman` topic。

**Selected item 原文（plan-local `T03` goal，摘要不改義）**：在 `ssot-governance.md` Transition Table 新增一列（唯一 GO predicate），授權「Agent closeout 於本次呼叫實際完成」後，對唯一歸屬的成果文件執行 `/projman align`；條文含 D-13 狹義授權句（逐字）、候選集合引用 projman Skill 定位規則（不複製）、歸屬三訊號、每階段範圍與預設錨點、git 僅輔助、寫入／提交分項與單檔 `git commit -o` 保存、失敗報告「已執行／已回填／未提交」與 dirty 路徑、FORBIDDEN 預設解讀、只消費既有證據、生效時點與首個合格觸發點；`sv/SKILL.md` Step 11 僅加一行引用；依 D-16 更正 `ssot-governance.md:11` 一行純文字；規則與 consumer 同票同 commit 落地；**不新增** Governance Artifact 清單列。使用者本次 Goal 表述：接通 Projman 在票正常收束後的階段判讀、實際 align 呼叫、成果保存與直接回報。

**相關約束（取自 parent 決策表，本票適用者）**：
- **D-2**：候選集合引用 projman Skill〈成果文件定位規則〉（含排除 `archive/`），不複製定位邏輯；票前綴只是可選線索；三訊號＝①被收束票文字明確指名該文件或其議題識別、②文件宣告的議題識別／票前綴與票 ID 或 registry topic 相符、③票的實際交付（ALLOWED 內 diff、AC）對應該文件某階段預期功能；唯一合理候選才執行，零個或多個不猜測、不執行，以一行說明原因（根本沒有成果文件時不輸出）；缺前綴舊文件不整類排除。
- **D-3／D-4**：每 ID 各自的最近 align（日期、HEAD 全雜湊、範圍）；校正某階段不消費他階段未檢查的交付；從未 align 的 ID 以成果文件首次提交為預設錨點；`git log` 僅輔助列示候選交付，不作資格條件，不整類排除 docs-only／規則型成果。
- **D-5**：漏跑只能稱「可能於後續相關收束時補判」，不宣稱保證自癒。
- **D-6／D-6b**：寫入授權與提交保存資格分項；align 自身以 `git commit -o <成果文件>`（`[GOVERNANCE-SYNC]`）單檔保存，寫前 `git status --short -- <文件>` 為空；軌道 A 先完成當次收尾提交，軌道 B 不為 align 提前 `/compact`；失敗報告「已執行／已回填／未提交」與 dirty 路徑（parent 原文；Rev A.2 依實際進度細分為「未完成呼叫」／「回填未完成」／「已回填未提交」／「已提交」，見新列），不回滾原票、不重試迴圈、不開 Gate incident；原票 FORBIDDEN 僅列檔名者預設為票內範圍排除；不新增 Governance Artifact 清單列；不遞迴；只消費既有證據。
- **D-9**：自動鏈只在本票啟用；本票之前無任何條文呼叫 align。
- **D-11**：觸發事實＝「Agent closeout 於本次呼叫實際完成（lifecycle 由本次寫入達 `REVIEW`、票檔已在 `deliver/`、checkpoint 已 read-back，且非『已收束重入』）」，與 QA／SV 是否依風險分級省略無關；條款本體置於 Transition Table（唯一 GO lane），sv Step 11 僅引用。
- **D-12（RECOMMENDED_ONLY → 本票採納）**：本票走完整程序（QA／SV／Cross-Review 不省略），理由見 Hard Constraints 7。
- **D-13**：狹義授權句逐字：「本票正常收束後，符合條件的 Projman 校正依本條取得獨立的成果文件讀寫及單檔保存授權；範圍僅限已唯一定位的成果文件與其現況回填，不擴張原票實作範圍，不修改其他檔案、產品需求、Registry 或 lifecycle。」條款與 consumer 引用提交後即生效，本票自身的 Agent closeout 是第一個**候選**觸發點。
- **D-15**：PM2-E 預設未驗；符合「同回合、收束寫入後、無新使用者訊息即呼叫 `Skill(projman)`」並有 session id 與依序工具呼叫證據、經 REVIEW→CLOSED 審查確認，才可記為自動；手動、自填、僅有提交、無法核對或未觸發皆維持未驗。
- **D-16**：`ssot-governance.md:11` 與 `compact/SKILL.md:362` 不一致的純文字更正（本票唯一的「其他治理段落」例外）。
- **Parent Plan Gate／eligibility 結果**：Plan Gate＝PASS；Ticket mode eligibility＝ALLOWED；Fixture＝false（本次 ticket 模式第 1 步逐項核對屬實）。

**使用者 2026-10-20 已裁決原則（內嵌）**：①候選定位引用 Skill，不複製；②票前綴僅輔助，不作唯一資格；③到階段成果點才執行，不逐票校正；④條款與 consumer 提交生效後，本票收束可成為首次候選觸發點，但不得為湊驗證忽略正常觸發條件；⑤真實觸發預設未驗，依實際呼叫證據及審查確認；⑥成果交付終點與純收尾紀錄提交分開，不因每次補紀錄自動延伸成果審查範圍；⑦不修改 Gate／Advisor／R0 預算或路由，五次 R0 案例（`PJMAN-OUTCOMES-01`）另線分析、不作本票前置；⑧平台成果頁仍不在本期範圍；⑨本次取證限定本地（不呼叫 PPSS／外部服務）；⑩只開本票，不執行實作。

**證據分級**（2026-10-20 13:3x–13:5x live 直讀；HEAD `499d583b`，開票時工作樹乾淨；registry revision 910；`D:\DevSources\AndroidProj\SmartCameraApp1`）：

- **CONFIRMED（本地原始碼／工具輸出）**：
  1. 上游票：`registry_list(include_archive=true)` 顯示 `PJMAN-SKILL-01`、`PJMAN-OUTCOMES-01` 皆 `lifecycle=CLOSED`、`topic=Projman`、`reviews={qa:PASS,sv:PASS,codex:PASS,traycer:N_A}`；`PJMAN-SKILL-01` 票檔來源區含 plan-local `T01`、`PJMAN-OUTCOMES-01` 含 `T02`（直接 lookup，母體＝registry 主冊＋archive、`Pending/`、`deliver/`、`void/`）。全冊無 `PJMAN-AUTOALIGN-*`（registry 主冊 13 筆＋archive 104 筆、`Pending/`／`deliver/`／`void/` 檔名、`grep -rIl PJMAN-AUTOALIGN plans docs .claude` 皆 0 命中）；`reports/` 無內容；`Projman` 為冊面已登記 topic（`topics` 清單含之）；無等價議題票（registry 全欄位關鍵字掃描僅命中上述兩張 PJMAN 票與無關票）。
  2. `.claude/skills/projman/SKILL.md`：271 行、blob `a1c6440b`、最後觸及 commit `86521a8f`；`disable-model-invocation: false`（L5）；〈成果文件定位規則〉L41–50（自動搜尋排除 `archive/`，L46）；〈被治理條款呼叫時（行為契約）〉L242–254 九項（L246 候選集合＝定位規則輸出；L247 只消費既有證據；L248 寫前 `git status --short -- "$DOC"` 非空即略過；L249 僅當條款授予單檔保存才 `git commit -o`、前綴由條款指定；L250 失敗處置與已知限制；L251 回報項目含 `CLAUDE_CODE_SESSION_ID`＋transcript 工具序列、取不到寫 `UNAVAILABLE`；L252 自動／手動判別；L253 不遞迴；L254 漏跑措辭）；L37 明載「本檔不定義、不啟用任何條款」。
  3. `plans/roadmap/projman-outcomes.md`：128 行、blob `865283c1`、已追蹤、未被 ignore、工作樹乾淨；PM2-A～E 摘要表（L25–29）`已驗收＝—`、`狀態＝未盤點`（約定 2／4／2／6／1），明細 L76–109 皆「實際成果與證據：未開始」「最近 align：無」；`目前階段：PM1`（L13）；基準 `最近 align`（L14）＝2026-10-18 `825076db…`（PM0-A PM1-A～E PM3-A）；首次提交 `git log --diff-filter=A --follow` ＝ `825076dbcf4ea4642ec0a7290005f88bc4316c65`（無列內 align 記錄的 PM2 ID 以此為預設錨點）。
  4. `.claude/rules/ssot-governance.md`：299 行、blob `f99a9547`、LF（bytes `\r\n`＝0）、最後觸及 commit `8d14db59`；`## Ticket Runtime Transition Table` L124；控制詞（封閉四值）L128–135；不變量 L137–142；`### 轉移表` L144，資料列 13 條 L148–160（L158＝`SV＋Codex PASS 且 Traycer Workflow ≠ N/A`，L159＝`已收束重入`，L160＝`Header 缺失或非法`）；`### 最小冪等回傳` L169–171；Governance Artifact 清單 L9–23（表列 5 條 L15–19，L11 為待更正句）；`## Session Persistence 邊界` L250，雙軌持久化 L256、軌道 A L257、軌道 B L258、唯一禁止 L259；檔內 `projman`（不分大小寫）＝0 命中。
  5. `.claude/skills/sv/SKILL.md`：270 行、blob `b6615e0d`、LF；`## Step 11: Closeout Auto-Chain` L206，L208 引言（「本節為引用，MUST NOT 複製四要件、GO predicate 或 Traycer 判定表全文」）、L210 第 1 項入口冪等（命中即 `MUST RETURN`，此路徑不呼叫 `check`；其後 `check closeout`）、L212 第 2 項敘事寫入（`registry_set_checkpoint`，置於 `registry_set_status` 之後，寫後 read-back）；`## 回報格式` L214；`projman` 命中＝0。
  6. consumer 有界清點（`grep -rn` 於 `.claude/rules`、`.claude/skills`、`.claude/hooks`、`.claude/scripts`、`.claude/CLAUDE.md`）：`projman` 於 projman 目錄以外僅 `.claude/rules/ticket_pattern.md:65`（family provenance），**無任何 caller**；「Agent closeout」於 `ssot-governance.md` 以外僅 `sv/SKILL.md:208`；`Step 11` 引用者＝`uc/SKILL.md:54`（收尾引用、不複製）與 `ssot-governance.md` 入口矩陣句（L224，closeout＝同檔 `## Step 11: Closeout Auto-Chain`）。
  7. 同主張清點：`tracked dirty 檔案才判 BLOCKED` 於 `.claude/**/*.md` 僅 `ssot-governance.md:11` 一處；`compact/SKILL.md:362` 寫「保留於工作樹、不納入本輪任何 commit……無須阻斷」（Akira 2026-09-12 免票裁決，表列檔案照常逐檔提交）；`qa/SKILL.md:173–175` 另規 Step 4.5 對非 ALLOWED 且非 Pipeline Artifact 的 dirty 檔回報 `⛔ Scope 汙染`，**語意不同**，本票不改。
  8. 提交通道：`.git/hooks/commit-msg` 自 `ticket_pattern.md` 取 `commit-hook=` regex `^\[[A-Z][A-Z0-9]*(-[A-Z0-9]+)+\]`，`[GOVERNANCE-SYNC] …` 字面測試命中；`pre-commit` 僅攔 `.kt/.java/.xml` 與 `.md` 混提；`.claude/settings.json` `permissions.allow` 含 `Bash(git commit *)`、`Skill(qa)`、`Skill(qa:*)`，**不含 `Skill(projman)`**，`deny` 9 條無 Skill／commit 相關項。
  9. 並行：registry 無 `IN_PROGRESS`、無 `REVIEW`；`PENDING` 7 筆（`GAP-06-TARGET-01-REVA-FIX-A` ALLOWED＝`.claude/PROGRESS.md`；`GAP-09-EYEAF-BACKOFF`、`STATE-CONTRACT-02／06`、`TRACTION-PROTO-02` ALLOWED 皆為 app／prototype 檔；`KAIROS3／4_Candidate_Compilation` 為候選彙編、無 ALLOWED，KAIROS4 L627 僅把 `ssot-governance.md` 列為某候選之常設化落點）皆不觸本票 ALLOWED 兩檔；`.tmp/ticket-author/20261019-routing-entry-diag.md`（Verdict `PARTIALLY_CONFIRMED`，**未開票**）提議把 sv Step 11 等後繼散文集中到 wiring 表，並記 projman 條款 caller 未接、日後以同表登記、不預留（該 diag L16、L86、L135）。
  10. 觀察途徑：本 session `CLAUDE_CODE_SESSION_ID` 存在，`~/.claude/projects/D--DevSources-AndroidProj-SmartCameraApp1/<session-id>.jsonl` 可讀（約 1.2MB）；此為**途徑存在**之證據，不是執行 session 的證據（執行 session 於 AI-0 重驗）。
  11. 前例：`ADV-APICALL-01_receipt.md:53` 的 FORBIDDEN 含 `plans/roadmap/autonomous-engineering-gate-outcomes.md`，`:62` 註明成果表由 `projman align` 回填、非該票寫入範圍（plan Rev C 原記 `:44`／`:53`，行號已位移）。
  12. 模板 `Unified_Ticket_Template_v3.md` v3.11 完整讀取（見「模板實讀記錄」）；`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` 版本 v3.5，§6（L43–L50）實讀。
- **SUPPORTED**：無外部來源依賴（本次限定本地取證）。
- **HYPOTHESIS**：`git commit -o <已追蹤檔>` 只提交指定路徑、不夾帶他檔（`PJMAN-SKILL-01` 已有本地 probe 升 `CONFIRMED`；本票 AI-5 於暫存 repo 重驗）。
- **UNKNOWN（已有明示處置，非 AC 阻斷）**：
  - **U1**：模型在真實收束時是否依 `uc:54`→Step 11→新第 3 項→Transition Table 列→`Skill(projman)` 的文字鏈實際遵循。處置＝**限定交付**：不在本票 AC；首次真實收束的結果（觸發／未觸發及原因）如實記錄，PM2-E 預設未驗，由 REVIEW→CLOSED 審查方依 session id 與依序工具呼叫確認（使用者裁決⑤、D-15）。**分階段驗收（Rev A.2；取代 Rev A.1 之「裁決⑤明示豁免」誤引，撤回）**：①**票內**驗條款、引用、靜態一致性及隔離案例（AC-1～AC-9）；②**真實接續**依本票收束後之正常判讀與呼叫結果（AC-10 收尾實錄），由 REVIEW→CLOSED 審查方確認。未觸發或證據不足時，真實接續**仍為未驗**；限定交付不等於自動成果已完成。本票 AC 中無 `[可達性]`，真實路徑之可達性屬第②階段待驗，非豁免。
  - **U2**：`Skill(projman)` 不在 `permissions.allow`，自動呼叫是否跳出權限提示、提示在無人值守下如何處理，以及「核准權限提示」是否屬 D-15／Skill L252 所稱「新的使用者訊息」——Skill 未定義。處置＝AI-0 記錄現況，**本票不改 `settings.json`**；權限被拒或 Skill 不可用走條款失敗路徑（略過並回報）；「核准提示是否算新使用者訊息」交 REVIEW 審查方判定，本票不自行裁決。**首次觸發觀察（Rev A.2）**：`Skill(projman)` 未列 allow **不推定為無法呼叫**（可能出現權限提示、或經其他規則放行）；首次真實觸發時如實記錄實際呼叫結果（是否提示、是否執行），本票不改 `settings.json`、不現在另開票。
  - **U3**：transcript 路徑／格式跨版本穩定性（沿用 `PJMAN-SKILL-01` 之 `UNAVAILABLE` 契約）。
- **Source／live 衝突處置（可見）**：
  1. Plan Rev C 稱 `.claude/rules` 自 `c03864fd` 起「無異動」、基線 `5b80bc32`；live 顯示 `ssot-governance.md` 之後被 `881c4c9e`／`9c80995b`（`INFRA-RULESLIM-01`）與 `8d14db59`（`ADV-APIGATE-01`）修改，Session Persistence 雙軌由 plan 記的 `:249–251` 位移至 `:256–259`（Transition Table 起點 `:124`、13 資料列 `:148–160`、`:11` 未變）→ **以 live 為準**；本票 AI 一律用字面錨點加命中數，不沿用 plan 行號。
  2. Plan 記 `PJMAN-OUTCOMES-01` 於 T02 後為 REVIEW；live 冊面為 `CLOSED`，`.claude/PROGRESS.md` Dashboard 仍寫 REVIEW → **以冊面為準**，PROGRESS 屬 `DEFERRED-TO-DOCSYNC`，本票不修。
  3. Plan 記前例 `ADV-APICALL-01_receipt.md:44／:53`；live 為 `:53／:62` → 以 live 為準（見 CONFIRMED 11）。
  4. Plan D-7 撤回「consumer 不可達」疑慮；live 新增事實 U2（`Skill(projman)` 不在 allow list）→ 不推翻 D-7（路徑仍存在），但列為 UNKNOWN 並要求 AI-0 記錄。
  5. **Skill 內部邊角（本票不修，列 Observation）**：`projman/SKILL.md` L248 規定寫前 `git status --short -- "$DOC"` 非空即略過，而 L249 另規「成果文件未追蹤時 `git commit -o` 失敗 → 報告已回填、未提交（未追蹤）」；未追蹤且未被 ignore 的檔案 `git status --short` 輸出 `??`（非空），會在 L248 即略過，**L249 未追蹤分支僅在檔案被 `.gitignore` 忽略（status 為空）時可達**。條款文字以 L248 為準；若需調和，屬 `projman` Skill 後續修補（本票 FORBIDDEN）。
  6. **Skill 節奏句與新列之關係（Rev A.1 揭露）**：`projman/SKILL.md:35` 寫「由使用者直接呼叫，或依使用者明確指示由模型呼叫……不得自動掛到每張 Ticket 完成」。新列每次 Agent closeout 皆判讀，但僅於「階段可檢查且自錨點起有新交付」時執行 align。依 `projman/SKILL.md:37`（被治理條款呼叫時見專節）、`:242`（〈被治理條款呼叫時（行為契約）〉）與 D-9／D-11 之解讀，條款呼叫由該專節承接、執行時點受 `:172`（align 使用時點）約束，**屬解讀而非 Skill 字面已明示**；若審查方認為需調和 L35 字面，屬 `projman` Skill 後續修補（本票 FORBIDDEN）。
  7. **失敗回報字面（Rev A.2）**：`projman/SKILL.md:250` 固定寫「已執行／已回填／未提交」；本票新列改為依實際進度回報「未完成呼叫」／「回填未完成」／「已回填未提交」／「已提交」之一，呼叫失敗時不得聲稱已回填。條款呼叫時以新列（唯一 GO lane）為準；Skill 字面之對齊屬 `projman` Skill 後續修補（本票 FORBIDDEN）。
- **已知限制（誠實揭露）**：自動鏈為模型契約，**無機械後盾**，可能漏跑或誤跑；本票交付的是條款與引用文字，不證明模型遵循；AI-5 為示例推演，非真實自動觸發、非真實 Skill 執行（`MISSING-INTEGRATION-TEST`）；「寫前乾淨」只是單次觀察，無法排除並行寫入；失敗後殘留 dirty 異動可能被下一張票廣域提交帶走，並涉及 `qa/SKILL.md:173–175` 與 Akira 2026-09-08 對話裁決（記憶，非 repo SSOT）的字面不一致（本票不改 `qa`）；同一模型自評、無獨立第二方；本票結果**不等於** Projman 全部自我驗證條件已完成，也不宣稱 PM2 通過。

**模板實讀記錄**：路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`；版本＝v3.11；文件日期（產出日期欄）＝2026-05-29；有效修訂日期＝2026-09-14（v3.11）；完整讀取（322 行）時間＝2026-10-20 13:4x（本地）。

**External-review decision**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` v3.5 §6〔L43–L50〕，2026-10-20 live 直讀；分項記錄，不以整節 WAIVED 掩蓋已成立的觸發）：

- **6a（成立；外審未執行）**：`INTRODUCED_CAPABILITY=feature:projman-auto-align`≠`NONE`；固定 corpus（vault `D:\DevSources\Smarter-Project` 決策檔目錄、`plans/cso_briefing/project/Handoff/`、`Pending/` `deliver/` `void/`、`TICKET_REGISTRY.json`、`TICKET_REGISTRY_ARCHIVE.json`）該 token 字面 **0 命中**（本票自身不計）→ 條件成立。處置：使用者本次指示限定**本地取證**，故未呼叫 PPSS／`research_submit`；硬風險判準①–⑥皆**未命中**（①無官方 contract 禁止；②無 provider 請求格式；③無新公開網路面；④Markdown 文字與本地 git 提交可 `git revert`、無不可逆資料寫入；⑤無外部費用；⑥無失敗 probe），故**非 BLOCKED**。本項為**非阻擋事項**，**不列 Akira 待決**（Rev A.2）：依開票時已確認之 corpus 核對結果、重用既有證據（本票證據分級 CONFIRMED 1–12 與上游 `PJMAN-SKILL-01` 外審 `rsch-20261017-7619d266f3` 之回流），執行中若出現須外部事實始能判定之缺口，依 `ssot-governance.md` §Autonomous Engineering Gate「證據缺口與研究授權」自主研究，不因可能付費再次請示；結果屬 `EVIDENCE_GAP`，依 O4 併入收尾實錄、不開 Rev、不計 4a。承重未知（U1 模型遵循、U2 權限提示）屬本地 repo 行為，外審不能取代首次真實收束的呼叫證據。
- **6b①（不成立）**：`ALLOWED` 兩檔首層 path segment 皆 `.claude`，distinct＝1 ＜2。**6b②（不成立）**：`### AI-[1-9][0-9]*:` 標題數＝6 ＜8（`AI-0` 不計）。**6b③（不成立）**：`DEPENDENCY_DELTA=NONE`。
- **6c（不適用）**：`ssot-governance.md`、`sv/SKILL.md` 檔頭皆無 `Revision` 欄位修訂歷史可計（`grep -n "^\*\*Revision\|^Revision"` 0 命中）。
- 防遞歸：本 capability 尚無外審紀錄，故不適用防遞歸律；公開資料未命中不構成縮減成果的授權。

**審查要求（Header 層明載；冊面既有票皆無 `verification` 欄範例，本票不寫入未經驗證形狀的欄位，改以本段為載體）**：`required reviews`＝**QA（Step 0 Scope Gate＋Step 4.5 Auto Commit；Step 1–3.5 因 docs preset 劃記 N/A）、SV（含 Codex Cross-Review，不豁免）**；Traycer＝N/A（純 `.md` 治理票）。理由：本票新增**自動行為**授權，不屬 `CLAUDE.md` SOP 風險分級可省略之低風險 docs 票；且存在**時序依賴**（見 Hard Constraints 7）。`⑤` 段收束時審查方 MUST 逐項核對 `verification.required` 各值於 `reviews` 有對應結果。R0 路由、report-only REPORT_FIX 閉環與 Gate 步驟級上限依 `cross-review.md`／`ssot-governance.md` 現行規定，本票不改其預算或路由。

**必要未決事項**：無（Rev A.2）。6a 依 Gate 研究授權處理（見 External-review 欄）；`Skill(projman)` 許可列為首次觸發觀察項（見 U2），不改 `settings.json`、不現在另開票。

**簽發日期**: 2026-10-20
**簽發者**: Claude
**來源議題**: Projman（登記縮寫 PJMAN；`ticket_pattern.md:65` 已登記 family；冊面 `topic`＝`Projman`，`allow_new_topic=false`）
**Mode**: normal  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: PJMAN-SKILL-01 —— 已 `CLOSED`；其交付之 `projman` Skill〈成果文件定位規則〉（L41–50）與〈被治理條款呼叫時（行為契約）〉（L242–254）為本票條款所引用，須先存在（直接 lookup 已核：同 parent、plan-local `T01`、非 VOID）
- `evidence_dependency`: PJMAN-SKILL-01、PJMAN-OUTCOMES-01、ACAGOV-RUNTIME-TABLE-01 —— 前二者為 Skill 契約與成果文件（PM2-A～E 原定義、`caller 未接入` 限制）之來源；後者為 Transition Table 與雙軌持久化條文之來源票
- `coordination_dependency`: 無（Registry 無 `IN_PROGRESS`／`REVIEW`、`Pending/` 7 筆皆不觸 ALLOWED 兩檔；資訊性提示：未開票的 routing-entry diag 若日後成票並改 sv Step 11 後繼散文或 Transition Table，須與本票串行，`concurrency.md` §2）

## Verify Config
TICKET_ID=PJMAN-AUTOALIGN-01
CREATED=2026-10-20
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md,.claude/skills/sv/SKILL.md
FORBIDDEN=.claude/skills/projman/SKILL.md,.claude/skills/uc/SKILL.md,.claude/skills/qa/**,.claude/skills/compact/SKILL.md,.claude/skills/ticket-author/**,.claude/skills/sv/CROSS_REVIEW.md,.claude/skills/sv/ssot_context.md,.claude/skills/sv/scripts/**,.claude/rules/cross-review.md,.claude/rules/concurrency.md,.claude/rules/ticket_pattern.md,.claude/rules/decommission.md,.claude/CLAUDE.md,.claude/hooks/**,.claude/scripts/**,.claude/settings.json,.claude/settings.local.json,plans/roadmap/projman-outcomes.md,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,app/**
MAX_LINES=30
DIFF_REF=499d583b
ANCHOR_PROBED=2026-10-20 13:50
BASE_COMMIT=499d583b
TARGET_BLOB=.claude/rules/ssot-governance.md:f99a9547;.claude/skills/sv/SKILL.md:b6615e0d
INTRODUCED_CAPABILITY=feature:projman-auto-align
DEPENDENCY_DELTA=NONE

> PRESET=docs（兩檔皆為治理規則／Skill 指令文字；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A 見 AC）。`MAX_LINES=30` 為 production-only diff 估算校準遙測（TPL v3.5：非 execution gate）：預估 Transition Table 新增 1 列（單行長度約 1.5K 字元）、sv 新增 1 行、`:11` 替換 1 行（+1／−1），行數不大但單列字元量大，**非**停止或退件判準。
>
> **`TARGET_BLOB`／`BASE_COMMIT` 說明**：兩 blob 為 `BASE_COMMIT=499d583b` 當下 HEAD 之值（`git hash-object`，Git Bash 可用，故不標 `N/A`）。三欄（`BASE_COMMIT`／`ANCHOR_PROBED`／`TARGET_BLOB`）為資訊性診斷值（TPL v3.11），**不作 gate**。`DIFF_REF` 為字面佔位，開工時（AI-0）回填 `git rev-parse --short HEAD` 並同步再生 `tmp/verify_cmd.conf`。
>
> **FORBIDDEN 與管線觸碰面之處置（`cross-review.md` R0 Pre-flight）**：`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 為 `/qa` Step 4／4.5 之標準觸碰面，本票將其列入 FORBIDDEN，處置條款 **`DEFERRED-TO-DOCSYNC`**：Dashboard 同步、`PROGRESS_ARCHIVE.md` Done 標記、票檔 Pending→deliver 搬移與票內 checkbox 勾選依標準 SOP 於下一輪 `/compact` 或後續 session 之正常收尾處理，**MUST NOT** 另開 DOCSYNC 票（`ssot-governance.md`「FORBIDDEN 延後項免開票紀律」）；其增量同步屬 Pipeline Artifact，不計為本票違規。
>
> **`plans/roadmap/projman-outcomes.md` 之 FORBIDDEN 解讀（D-6b）**：該路徑列入 FORBIDDEN 僅為**票內範圍排除**（本票實作範圍不含成果文件），**無其他限制語意**；本票收束後由 Transition Table 新列觸發的 align，其成果文件讀寫與單檔保存屬該列之**獨立授權**，不屬本票 ALLOWED、亦不受本票 FORBIDDEN 阻擋（現行實務佐證：`ADV-APICALL-01_receipt.md:53,62`）。本票各處（Hard Constraints、Error Handling、AC）**MUST NOT** 把該路徑寫成「明確禁止」或「禁止寫入」，以免條款對本票自身的首次候選觸發失效。
>
> **開票時檔案異動揭露（不隱藏）**：本次開票**未修改**任何 repo 內受版控檔案（`PJMAN-` family provenance 已由 `PJMAN-SKILL-01` 登記於 `ticket_pattern.md:65`，live 核對存在且 regex 行未動）；僅新增 `Pending/` 票檔（受 `.gitignore` 排除，依 INFRA-TRACKSCOPE-01 凍結點語義，搬 `deliver/` 之首次 commit 才進版控）並寫入 Registry 一筆。
>
> **R0／Cross-Review 授權對照（`cross-review.md:13`：治理檔須「ALLOWED 已列＋行為經 AI／AC 明確授權」；關鍵詞僅為取證提示）**：`ssot-governance.md` 之 Transition Table 新列＝AI-1／AC-1、AC-2、AC-4；`:11` 純文字更正＝AI-3／AC-8；`sv/SKILL.md` Step 11 新第 3 項＝AI-2／AC-1、AC-2、AC-4。本票不改任何 feature flag 預設值、DI 綁定、Gate 預算或路由。

## Action Items

> **AI-0 為強制首項**。以下各項為錨點與基線復驗；任一項不符，先依 TPL「基線漂移三分流」分類（`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 為資訊性，記錄後以工作樹現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止並直送收束審查；`BLOCKED_UNATTRIBUTED_STATE` 因本票僅觸及 `.claude/` 可逆治理面，降為記錄後續作），**禁以「推定等價位置」充當字面錨點**。計數以 `grep -cF`（或明列的 `grep -c -E`）取得，byte／換行以 `python -X utf8 -I` 取得，不用 `grep -c $'\r'`。

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/rules/ssot-governance.md`、`.claude/skills/sv/SKILL.md`（及唯讀參照檔）
- **動作**: 逐字確認各「現值」於檔內命中數與票面所載相符（標「唯一」者恰 1 次），並記錄 `DIFF_REF`：
  1. 環境：`git rev-parse --short HEAD` 記錄；`git status --short -- <ALLOWED 兩檔>` 為空；`git check-ignore -v` 對兩檔零命中；兩檔已追蹤；`git ls-files --eol` 為 `i/lf w/lf`；工作樹 blob 等於 `TARGET_BLOB`（`f99a9547`／`b6615e0d`）；`git diff --name-only 499d583b..HEAD -- <ALLOWED 兩檔>` 輸出為空（空＝未及標的；非空依三分流歸因）。
  2. `ssot-governance.md` 現值錨點（皆唯一，各恰 1 次）：`^## Ticket Runtime Transition Table$`（L124）、`^### 轉移表$`（L144）、`^### 最小冪等回傳$`（L169）、`^## Session Persistence 邊界$`（L250）、`軌道 A — 隨票即時`（L257）、`軌道 B — session 邊界`（L258）、列首樣式 `^\| SV＋Codex PASS 且 .Traycer Workflow ≠ N/A. \|`（`grep -c -E`，L158）、列首樣式 `^\| 已收束重入（`（`grep -c -E`，L159）、`表列外之 tracked dirty 檔案才判 BLOCKED`（L11）；`### 轉移表` 至 `### Traycer 語義` 間以 `| ` 起首之列數＝15（表頭＋分隔線＋13 資料列）；Governance Artifact 清單 L13–L19（表頭＋分隔線＋5 資料列）之 `sed -n 13,19p | git hash-object --stdin` 記錄（現為 `46016506…`，供 AC-3 位元組比對）。
  3. 生產者缺位基線（本票新增前）：`projman`（不分大小寫）於 `ssot-governance.md`、`sv/SKILL.md`、`uc/SKILL.md`、`qa/SKILL.md`、`compact/SKILL.md` 命中皆 0；D-13 授權句關鍵片段 `符合條件的 Projman 校正` 於 `.claude/**/*.md` 命中 0；`tracked dirty 檔案才判 BLOCKED` 於 `.claude/**/*.md` 僅 L11 一處（同主張全檔清點）。
  4. `sv/SKILL.md` 現值錨點（皆唯一）：`^## Step 11: Closeout Auto-Chain$`（L206）、`2. **敘事寫入**：走 `（`grep -cF`，L212）、`^## 回報格式$`（L214）；Step 11 區段內不存在以 `3. ` 起首之項。
  5. 唯讀參照：`projman/SKILL.md` 之 `^### 成果文件定位規則$`（L41）、`^## 被治理條款呼叫時（行為契約）$`（L242）各恰 1 次、`^disable-model-invocation: false$` 恰 1 次、L246–L254 為第 1–9 項、`UNAVAILABLE` ≥1；`projman-outcomes.md` 已追蹤且 `git status --short -- plans/roadmap/projman-outcomes.md` 為空、`^## 階段成果表$` 恰 1 次、PM2-A～E 五列 `已驗收＝—`／`未盤點`；首次提交 hash＝`825076dbcf4ea4642ec0a7290005f88bc4316c65`。
  6. 通道與權限（U2 記錄，不修改）：`.claude/settings.json` `permissions.allow` 含 `Skill(qa)`、`Skill(qa:*)`、不含 `Skill(projman)` 的現況原樣記錄；`commit-msg` regex 對 `[GOVERNANCE-SYNC] test` 字面命中；本 session `CLAUDE_CODE_SESSION_ID` 與 transcript 可讀性記錄（取不到寫 `UNAVAILABLE`）。
  7. 並行與串行：`registry_list` 確認無 `IN_PROGRESS`；`Pending/`＋冊面無其他票 ALLOWED 觸 `ssot-governance.md`／`sv/SKILL.md`／`projman/SKILL.md`／`projman-outcomes.md`（記錄結果）；scratch 目錄 `tmp/PJMAN-AUTOALIGN-01/` 不存在或為空（`concurrency.md` §1）。
  8. 版本：模板 `Unified_Ticket_Template_v3.md` 檔頭仍為 v3.11、`Instructions_for_Claude.md` 檔頭仍為 v3.5（不符＝記錄為資訊性差異，不停工）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成（2026-10-20；`/normal PJMAN-AUTOALIGN-01` 啟用，session `4a678cbe-9cee-400a-8479-95de6747ac41`；AI-0：HEAD `499d583b`＝BASE_COMMIT，`DIFF_REF=499d583b` 已回填票面並由 activate_ticket.sh 再生 `tmp/verify_cmd.conf`／`tmp/verify/PJMAN-AUTOALIGN-01.rev-0.conf`；兩檔 status 空、check-ignore 0、`i/lf w/lf`、blob `f99a9547`／`b6615e0d`＝TARGET_BLOB、`499d583b..HEAD` 漂移空；`ssot-governance.md` 錨點全為 1（Transition Table L124、轉移表 L144、最小冪等 L169、Session Persistence L250、軌道 A/B 各 1、row158=1、row159=1、L11=1）、轉移表 `| ` 列數 15、清單表 L13–19 hash `46016506`；基線 `projman` 於五檔皆 0、D-13 片段 0、`tracked dirty 檔案才判 BLOCKED` 僅 L11；`sv` Step 11／敘事寫入／回報格式各 1、Step 11 內無 `3. `；`projman/SKILL.md` 定位 1／條款專節 1／flag 1／UNAVAILABLE 1、L246–254 為第 1–9 項；`projman-outcomes.md` 已追蹤且 status 空、`## 階段成果表` 1、PM2-A～E 皆 `—`／`未盤點`、首次提交 `825076dbcf4ea4642ec0a7290005f88bc4316c65`；`permissions.allow` Skill 僅 `Skill(qa)`、`Skill(qa:*)`，**無 `Skill(projman)`**（U2 現況）；`[GOVERNANCE-SYNC]` 通過 commit-msg regex；`CLAUDE_CODE_SESSION_ID` 存在、transcript 可讀（約 2.5MB）；模板 v3.11、Instructions v3.5 未變；scratch `tmp/PJMAN-AUTOALIGN-01/` 原不存在。並行：開工時 registry 無其他 IN_PROGRESS（本票由 PENDING 轉 IN_PROGRESS）。註：本票於開票後經另一流程修訂至 Rev A.2〔RGR 報告 `.tmp/rigor-gate/RGR_PJMAN-AUTOALIGN-01_20261020.md`〕，本次實作以 Rev A.2 現值為準。）

### AI-1: `ssot-governance.md` Transition Table 新增一列（唯一 GO predicate；授權依據：本 AI／AC-1、AC-2、AC-4）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值（唯一）**: 以列首樣式 `^\| 已收束重入（` 命中的列（L159，`grep -c -E` 恰 1 次）；其前一列以列首樣式 `^\| SV＋Codex PASS 且 .Traycer Workflow ≠ N/A. \|` 命中（L158）。
- **目標值**: 於上述兩列**之間**插入下列單列（其餘列位元組不變）。列內**不得**含未轉義之 `|`，**不得**出現 `→`，控制詞僅用封閉四值之 `MUST AUTO-CHAIN`；「未完成呼叫」／「回填未完成」／「已回填未提交」／「已提交」為依實際進度擇一之輸出字面，非 verdict／receipt 枚舉（Rev A.2）：

```markdown
| Agent closeout 於本次呼叫實際完成（lifecycle 由本次寫入達 `REVIEW`、票檔已在 `deliver/`、checkpoint 已 read-back；非「已收束重入」分支），且同時成立：①存在唯一合理候選的成果文件（候選集合為 `projman` Skill〈成果文件定位規則〉之輸出；歸屬依三訊號綜合判讀：被收束票文字明確指名該文件或其議題識別、文件宣告的議題識別或票前綴與票 ID 或 registry topic 相符、票的實際交付〔ALLOWED 內 diff、AC〕對應該文件某階段的預期功能；票前綴僅輔助，缺前綴舊文件不整類排除）；②被收束票的實際交付使該文件某階段 ID 達到可檢查點（即 `projman` Skill〈align〉之使用時點），且該 ID 自其錨點起有未盤點的新交付。觸發與 `/qa`、`/sv` 是否依風險分級省略無關 | `MUST AUTO-CHAIN` 呼叫 `Skill(projman)` align，並於同一回合顯示具體 align 提示詞、實際執行；採軌道 A 者先完成當次 `[<TICKET_ID>]` 單票收尾提交，採軌道 B 者不為此提前 `/compact`。授權（逐字；「本票」＝被收束之票）：「本票正常收束後，符合條件的 Projman 校正依本條取得獨立的成果文件讀寫及單檔保存授權；範圍僅限已唯一定位的成果文件與其現況回填，不擴張原票實作範圍，不修改其他檔案、產品需求、Registry 或 lifecycle。」候選集合、每 ID 範圍與預設錨點、回填、回報項目與自動／手動判別依 `projman` Skill〈成果文件定位規則〉與〈被治理條款呼叫時（行為契約）〉，本列不複製；`git log` 僅輔助列示候選交付。保存：寫前 `git status --short -- <成果文件>` 為空，才以 `git commit -o <成果文件>`（前綴 `[GOVERNANCE-SYNC]`）單檔提交，提交僅含本次 align 產物；非空則略過並回報。被收束票 `FORBIDDEN` 中僅列檔名而無其他限制語意者，視為票內範圍排除、不阻擋本列；其他明確禁止、權限拒絕與安全限制仍遵守，無法排除實質衝突時略過本次 align 並回報。只消費既有證據，不新增付費呼叫，預設不新增本地建置或測試。依實際進度回報「未完成呼叫」（Skill 不可用、權限被拒或呼叫未完成）、「回填未完成」、「已回填未提交」或「已提交」之一（輸出字面，非 verdict／receipt 枚舉），呼叫或回填未完成時不得聲稱已回填，未提交者附 dirty 路徑，並如實列出已知限制：寫前乾淨只是單次觀察、不排除並行寫入，殘留 dirty 異動可能被下一張票的廣域提交帶走 | 在「已收束重入」分支觸發；左欄任一條件不成立時猜測歸屬或強行執行（本列不適用，僅以一行說明原因，根本沒有成果文件時不輸出）；以票前綴、提交前綴或目錄排除作為唯一資格，或整類排除 docs-only／規則型成果；遞迴（align 非 Ticket 收束，其提交不帶票前綴，不再觸發 align）；修改成果文件以外的檔案、Registry 或 lifecycle；以 `git add`、`--allow-empty` 或補交 hash 製造提交；因失敗而回滾原票、還原檔案、重試迴圈、開 Gate incident（回指 §Autonomous Engineering Gate），或回頭視為原票或下一張票未收束；宣稱漏跑必能補判（漏跑僅得稱「可能於後續相關收束時補判」）或本鏈路已有機械後盾 |
```

- **約束**: 新列為資料列第 14 條；不新增 lifecycle 值、verdict／receipt 枚舉、`HUMAN_REQUIRED` 類別、ledger 事件；不改其他列、控制詞、不變量、Traycer 語義、最小冪等回傳；不改 Governance Artifact 清單；不複製成果文件定位指令（如 `grep -rl "^## 階段成果表"`）。授權句與 `PJMAN` Skill 契約若出現字面衝突，以授權句為準並於收尾實錄記錄，**不改 `projman/SKILL.md`**。
- [x] 完成（2026-10-20；`ssot-governance.md:159` 新列，1527 字元，由票面 AI-1 目標值區塊程式化插入於 `:160 已收束重入` 之前；與票面區塊逐字相等〔python `row==blk` True〕；其餘行位元組與 HEAD 相同（僅 L11 另有 AI-3 差異）；轉移表 `| ` 列數 15→16〔資料列 13→14〕；`git diff -U0` 該 hunk `@@ -158,0 +159 @@`。）

### AI-2: `sv/SKILL.md` Step 11 新增一行引用（授權依據：本 AI／AC-1、AC-2、AC-4）
- **檔案**: `.claude/skills/sv/SKILL.md`
- **現值（唯一）**: Step 11 第 2 項，以 `grep -cF '2. **敘事寫入**：走 '` 命中（L212，恰 1 次，所在段落尾）；其後為空行與 `## 回報格式`（L214）。
- **目標值**: 於第 2 項**之後**（`## 回報格式` 之前）新增第 3 項，其餘行位元組不變：

```markdown
3. **收束後成果校正**：第 1 項未命中 `MUST RETURN`、且第 2 項寫入與 read-back 完成（本次呼叫實際完成收束）後，是否呼叫 `Skill(projman)` align 唯一依 `.claude/rules/ssot-governance.md` §Ticket Runtime Transition Table 之「Agent closeout 於本次呼叫實際完成」列；判準、授權與保存規則以該列為唯一來源，此處**不複製**，亦不複製成果文件定位邏輯。
```

- **約束**: 不改 Step 11 引言（L208）與第 1、2 項；不改 `uc`／`qa`／`compact`；`sv/SKILL.md` 內 `projman` 命中以此一行為限。
- [x] 完成（2026-10-20；`sv/SKILL.md:213` 新第 3 項，位於 `:212` 第 2 項之後、`:215 ## 回報格式` 之前；與票面區塊逐字相同；`git diff -U0` 該檔僅一個 hunk `@@ -212,0 +213 @@`；`sv/SKILL.md` 內 `projman` 命中行＝[213]。）

### AI-3: `ssot-governance.md:11` 一行純文字更正（D-16；授權依據：本 AI／AC-8）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值（唯一）**（位於 L11 句中，其前後文字不動）：

```markdown
Scope 汙染判斷式 **MUST** 以此表為準（表列外之 tracked dirty 檔案才判 BLOCKED）；
```

- **目標值**：

```markdown
Scope 汙染判斷式 **MUST** 以此表為準（表列外之 tracked dirty 檔案保留於工作樹、不納入本輪 commit、不阻斷；表列檔案照常逐檔提交，對齊 `compact/SKILL.md` Auto Commit 步驟 1）；
```

- **約束**: 僅限 `/compact` Auto Commit 的 Scope 盤點語意，**不得**讀為對 `qa/SKILL.md` Step 4.5（L173–175）語意的變更；本行以外 L11 其餘位元組不變；`Governance Artifact 清單` 表列（L13–L19）位元組不變。若 Edit 工具以空字串刪除時吞掉行尾換行，須以 `git diff` 確認恰為預期單行變更（`INFRA-RULESLIM-01` 先例）。
- [x] 完成（2026-10-20；`ssot-governance.md:11` 以票面現值／目標值區塊精確替換〔現值命中 1〕；`git diff -U0` hunk `@@ -11 +11 @@` 恰一行；`tracked dirty 檔案才判 BLOCKED` 於 `.claude/**/*.md` 命中 0；`qa/` 目錄零 diff；清單表 L13–19 hash 仍 `46016506`。）

### AI-4: 靜態與可達性核對（不改檔案）
- **檔案**: `.claude/rules/ssot-governance.md`、`.claude/skills/sv/SKILL.md`（唯讀核對）；`.claude/skills/projman/SKILL.md`、`.claude/skills/uc/SKILL.md`、`.claude/skills/qa/SKILL.md`、`.claude/skills/compact/SKILL.md`（唯讀）
- **動作**: 逐項取證並記錄於收尾實錄：①新列與新第 3 項存在、命中數符合 AC-1；②不複製核對（AC-2）；③`git diff -U0` 僅三個 hunk（L11、新列、sv 新行）且 `sed -n 13,19p` 雜湊與 AI-0 記錄相同（AC-3）；④鏈路 producer／consumer `檔案:行號`（以最終檔為準）：觸發事實 producer＝`sv/SKILL.md` Step 11 第 1、2 項（L210、L212）；consumer 引用＝`sv/SKILL.md` 新第 3 項；GO 列＝`ssot-governance.md` 新列；Skill 契約＝`projman/SKILL.md:242–254`；寫入與保存＝`projman/SKILL.md:248–249`；觀察者＝對話回報、`git log -1 -- plans/roadmap/projman-outcomes.md`、transcript `tool_use` 順序；⑤RETURN 分支不觸發之文字依據＝新列左欄「非『已收束重入』」與 `ssot-governance.md` 最小冪等回傳；⑥新列內 `→` 計數＝0、`保證`／`自癒`／`必定補判` 計數＝0、控制詞僅 `MUST AUTO-CHAIN`。
- **約束**: 本項只讀；任何發現需改 ALLOWED 以外檔案才能成立者，依 Error Handling 停止回報，不擴權。
- [x] 完成（2026-10-20；AI-4 核對輸出：①新列 `ssot-governance.md:159` 含 D-13 授權句逐字〔該句於 governance 檔總計 1 次〕、`MUST AUTO-CHAIN`、`Skill(projman)`、`git commit -o`、`[GOVERNANCE-SYNC]`、`Agent closeout 於本次呼叫實際完成`；`sv/SKILL.md:213` `收束後成果校正` 恰 1 次；②`projman` 命中：sv 1 行〔:213〕、governance 1 行〔:159〕、uc／qa／compact 皆 0；D-13 片段僅 governance 一檔；`grep -rl` 定位指令與 `^## 階段成果表` 字面於 governance／sv 皆 0；Governance Artifact 表列 5 條；③`git diff -U0`：governance 2 hunk〔L11、:159〕、sv 1 hunk〔:213〕，清單表 hash `46016506` 不變；④鏈路 `檔案:行號`（最終檔）：觸發事實 producer＝`sv/SKILL.md:210`〔第 1 項入口冪等／RETURN〕、`:212`〔第 2 項敘事寫入＋read-back〕；consumer 引用＝`sv/SKILL.md:213`；GO 列＝`ssot-governance.md:159`；RAPID ON／OFF＝`ssot-governance.md:148–149`；Skill 契約＝`projman/SKILL.md:242–254`，寫前檢查 `:248`、保存 `:249`、失敗 `:250`、回報與 session id／工具序列 `:251`、自動／手動 `:252`、不遞迴 `:253`、漏跑措辭 `:254`；候選集合定位規則 `projman/SKILL.md:41–50`；觀察者＝對話回報、`git log -1 --format=%H -- plans/roadmap/projman-outcomes.md`、transcript `tool_use`；⑤RETURN 不觸發文字依據＝`ssot-governance.md:159` 左欄「非『已收束重入』」、`sv/SKILL.md:210`、`ssot-governance.md:160`〔已收束重入列〕與 `:170`〔最小冪等回傳〕；不遞迴＝`ssot-governance.md:159` 右欄「遞迴（align 非 Ticket 收束……）」與 `projman/SKILL.md:253`；⑥新列 `→`＝0、`保證`／`自癒`／`必定補判`＝0、控制詞僅 `MUST AUTO-CHAIN`、`|` 數＝4。）

### AI-5: 示例推演（暫存 repo；標明非真實 Skill 執行、非真實自動觸發）
- **檔案**: `tmp/PJMAN-AUTOALIGN-01/`（專屬 scratch；全程不碰主工作樹受版控檔）
- **動作**: 於暫存 git repo 內以真實 `git` 指令建立並斷言起始形狀（fixture 不經被測行為：以 `git status --short -- <doc>`／`git ls-files` 斷言 tracked／clean／dirty／untracked／ignored 起點），逐案套用 AI-1 新列條文並**如實標示：條款判讀由執行端依條文推演、`git` 機械行為為真實指令輸出**：
  - C1 唯一歸屬、文件 tracked clean、他檔另有 dirty → `git commit -o <doc>` 只含該檔；提交訊息 `[GOVERNANCE-SYNC] …` 通過 `commit-msg` regex；
  - C2 文件 tracked dirty（模擬他 session 修改）→ 寫前 `git status --short` 非空，略過並回報、不寫檔；
  - C3a 文件未追蹤且未 ignore → 寫前 status 輸出 `??`（非空）→ 略過；C3b 文件未追蹤且被 `.gitignore` 忽略 → status 為空，回填後 `git commit -o` 以 pathspec 錯誤失敗 → 報告「已回填、未提交（未追蹤）」，不改用 `git add`（印證證據分級衝突處置 5）；
  - C4 無成果文件 → 不輸出；C5 有成果文件但歸屬不明（無三訊號）→ 一行說明原因、不執行；C6 兩份皆含 `^## 階段成果表` 且議題皆相符 → 不猜測，一行說明；
  - C7 缺前綴舊 5 欄文件、被收束票文字明確指名該文件（訊號①）→ 歸屬成立（不因缺前綴整類排除）；
  - C8 每 ID 範圍：文件含已 align 的 ID A（列內錨點）與從未 align 的 ID B → B 以 `git log --diff-filter=A --follow --format=%H -- <doc> | tail -1` 為錨點，A 的校正不改 B 的「最近 align」；docs-only 交付的 commit 仍出現於 `git log <錨點>..HEAD` 候選列示（未因前綴或目錄被排除）；
  - C9 提交失敗（暫存 repo 加會失敗的 pre-commit hook）→ 報告「已回填未提交」與 dirty 路徑；另以 Skill 不可用模擬 → 報告「未完成呼叫」、不聲稱已回填（Rev A.2）；模擬的原票 lifecycle／checkpoint 檔案位元組不變；無重試迴圈；
  - C10 原票 FORBIDDEN 僅列該成果文件檔名 → 視為票內範圍排除而執行；另有明確禁止或權限拒絕 → 略過並回報；
  - C11 已收束重入（lifecycle `REVIEW` 且票檔在 `deliver/`）→ 不觸發；C12 align 提交後 → 提交訊息不帶票前綴、非 Ticket 收束，不再觸發 align；
  - C13 唯一候選但該 ID 自錨點起無新交付（`git log <錨點>..HEAD` 無對應該 ID 預期功能的交付），或該階段尚未達可檢查點 → 不執行，一行說明原因（含無對應基準）。
- **約束**: 判斷類步驟（歸屬、略過、不觸發、不遞迴等）**逐步附最終 `ssot-governance.md` 新列或 `projman/SKILL.md` 之 `檔案:行號`（first-hand）**，無行號之步驟不計證據；`git` 指令輸出逐字保存（Rev A.1）。推演結果記入收尾實錄並標 `[stub-equivalent]`／`MISSING-INTEGRATION-TEST`；不建立正式票、不寫 Registry、不改 repo 受版控檔；scratch 結束後只清 `tmp/PJMAN-AUTOALIGN-01/`（`concurrency.md` §1，禁全域 glob）。
- [x] 完成（2026-10-20；暫存 repo 真實 git 輸出與 13 案條款判讀〔逐步附 `ssot-governance.md:159`／`projman/SKILL.md` 行號〕已逐字存入本票「收尾實錄 §4」；標 `[stub-equivalent]`／`MISSING-INTEGRATION-TEST`；**scratch 清理未執行**：`rm -rf tmp/PJMAN-AUTOALIGN-01/{r,rehearse.*}` 被權限拒絕（使用者拒絕該指令），依「不改道重試同一被拒動作」未以其他方式刪除；暫存 repo `tmp/PJMAN-AUTOALIGN-01/r/`、`rehearse.sh`、`rehearse.out` 仍留在該票專屬 scratch 目錄（`tmp/` 非版控，不影響 diff），待使用者自行清理或明示授權；主工作樹受版控檔僅 AI-1／2／3 的預期變更。）

### AI-6: 收尾實錄與保存（依既有流程）
- **檔案**: 本票檔（`Pending/` → `deliver/`，依標準 SOP；不屬 ALLOWED 兩檔）
- **動作**: 收尾實錄含：AI-0 結果與 `DIFF_REF`；AI-1／2／3 目標文字逐字比對；AI-4 核對輸出；AI-5 各案輸出與限定標記；U1／U2／U3 現況；External-review 欄最新狀態（6a 依 Gate 研究授權處理之實際結果：未發生外部事實缺口，或已研究之查詢與結論）；`Skill(projman)` 首次觸發觀察結果（U2）；Observation（Skill L248／L249 邊角）；`DIFF_END` 與審查範圍聲明；QA／SV／R0／Cross-Review 結果與 receipt；**收束觸發判讀**：Agent closeout 完成後，**僅依新列正常條件**判讀並如實記錄「觸發／未觸發（含一行原因）」，觸發時附呼叫來源、`Skill(projman)` 實際呼叫、回填差分、結果摘要、保存結果（commit hash 或未提交原因）、session id 與依序工具呼叫（取不到寫 `UNAVAILABLE`）；該判讀屬**純收尾紀錄提交**，不延伸成果審查範圍。冊面 `reviews` 四欄各填實測值（`traycer=N_A`）；執行端 lifecycle 最高至 `REVIEW`，`CLOSED` 限審查方。
- **約束**: 勾選須於各項實際完成的同一回合內（`ssot-governance.md` Ticket Closure 紀律）；`[行為性]`／真實觸發相關項未實測者保留未勾並誠實揭露；MUST NOT 宣稱自動鏈已驗證、PM2-E 已升級或 Projman 全部自我驗證完成。
- [ ] 完成

## Hard Constraints
1. **同票同 commit 落地**：Transition Table 新列與 sv 新第 3 項屬同一交付提交（`/qa` Step 4.5 單次 commit，stage 僅 ALLOWED 兩檔＋Pipeline Artifact），不得拆成「只有條款」或「只有引用」的中間狀態。
2. **不複製、不擴張**：GO predicate 只在 Transition Table 新列；`sv`／`uc`／`qa`／`compact` 無 predicate 副本、無成果文件定位邏輯副本；不新增 Governance Artifact 清單列；不新增 lifecycle 值、verdict／receipt 枚舉、`HUMAN_REQUIRED` 類別或 ledger 事件；不新增腳本、hook、狀態庫、鎖、日誌系統；不改 `projman/SKILL.md`、`qa`（含 L173–175）、`gate_stop_guard.py`、`registry_review_stop_guard.py`、`settings.json`（含不加 `Skill(projman)` 許可）、Registry schema。除 D-16 的 `:11` 一行外，不改其他治理段落；不改一般票提交紀律（`ssot-governance.md` Session Persistence 雙軌）。
3. **不動 Gate／Advisor／R0**：不修改 Gate、Advisor、R0 的預算、計數或路由；`PJMAN-OUTCOMES-01` 的五次 R0 案例另線分析，不作本票前置，本票亦不以任何方式處理它。
4. **成果文件範圍**：本票實作範圍**不含** `plans/roadmap/projman-outcomes.md`（見 Verify Config 註）；收束後由新列觸發的 align 屬條款獨立授權，**不得**因本票 FORBIDDEN 清單而被視為明確禁止；本票不寫入、不預填 PM2 各 ID。
5. **首次候選觸發的紀律**：本票自身收束是否觸發，僅依新列正常條件判讀；**不得**為取得 PM2-E 證據而忽略、製造或繞過任一條件（含候選不唯一、文件 dirty、未到階段點、無新交付）；未觸發則 PM2-E 維持未驗並如實記錄原因；觸發亦不自動升級 PM2-E，須依 D-15 由審查方確認。
6. **成果交付終點與純收尾紀錄分開**：`DIFF_END` 於 `/qa` Step 4.5 釘在含兩檔實作的交付提交；其後 (a) 票檔 Pending→deliver、收尾實錄與勾選等純紀錄提交，(b) 被新列觸發的 align 提交，皆**不計入** `DIFF_REF..DIFF_END`、不自動延伸 Codex 審查範圍；僅當紀錄提交改動 ALLOWED 檔的實質內容，才視為新交付終點並依 SOP 重新受審。
7. **不走 docs 省略路徑**：`CLAUDE.md` SOP 風險分級雖允許低風險 docs 票省略 `/qa`／`/sv`／cross_review，本票**不套用**。理由：①**時序依賴**——新列與引用須經 `/qa` Step 4.5 提交生效，本票 Step 11 收尾才有條款可依（首次候選觸發點的前提）；②新增自動行為授權，非低風險 docs。
8. **措辭紀律**：不宣稱自動鏈已驗證、已有機械後盾、保證或自癒；漏跑僅稱「可能於後續相關收束時補判」；新列僅用封閉四值控制詞，列內無 `→`。
9. **取證與外部服務**：本票預設不呼叫付費模型、PPSS 或外部服務；開票時 6a 外審未執行（使用者限定本地取證）。執行中若出現須外部事實始能判定之缺口，依 `ssot-governance.md` §Autonomous Engineering Gate「證據缺口與研究授權」自主處理，不因可能付費再次請示（Rev A.2）。
10. **`:11` 更正邊界**：僅對齊 `compact/SKILL.md:362`，限 `/compact` Auto Commit 的 Scope 盤點；`qa/SKILL.md:173–175` 與 Akira 2026-09-08 對話裁決的字面不一致維持現狀並記為限制。
11. **Auto Commit 紀律**：禁止 `git add .`；stage 僅 ALLOWED 兩檔（＋Pipeline Artifact 增量）；scratch 僅用 `tmp/PJMAN-AUTOALIGN-01/`，禁全域 glob 清理。

## Error Handling
- AI-0 任一錨點命中數與票面不符 → 依 TPL 基線漂移三分流分類處置（資訊性者記錄後以現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止回報）；禁推定等價位置。
- ALLOWED 兩檔於開工時已有未提交修改（他人或他 session）→ 停止回報，不覆寫。
- 新列文字經檢查需新增 lifecycle 值、verdict／receipt 枚舉、`HUMAN_REQUIRED` 類別、ledger 事件或第五個控制詞才能成立 → 停止回報（不得擴權）。
- 要使條款可運作而必須修改 ALLOWED 以外檔案（含 `projman/SKILL.md`、`qa`、`settings.json`、hook）→ 停止回報，列出所需檔案與理由，不擴 scope。
- 發現 `:11` 同主張另有 live 實例於 ALLOWED 以外檔案 → 記錄於收尾實錄，不改該檔。
- `Edit` 造成非預期行合併或換行丟失（`git diff` 顯示超出預期 hunk）→ 還原該次編輯並以最小替換重做，不接受含額外變動的結果。
- 本票收束時新列正常條件不全成立 → 不執行 align，僅一行說明原因並記錄；不得為求觸發而改票面、文件或環境。
- 新列觸發後寫入、提交、Skill 不可用或權限被拒 → 依新列失敗處置，按實際進度回報「未完成呼叫」／「回填未完成」／「已回填未提交」之一（未提交者附 dirty 路徑，呼叫失敗時不聲稱已回填）；不回滾原票、不還原檔案、不重試迴圈、不開 Gate incident。
- `git check-ignore -v <ALLOWED 標的>` 命中 → 停止回報（gitignore 死結律，候選 #20）。
- `commit-msg` hook 拒絕本票交付提交 → 修正訊息前綴重試一次；仍失敗 → 停止回報，不繞過 hook。

## 狀態矩陣（涵蓋本票條款可達且與契約相關之組合；開票時建立）

| # | closeout 狀態 | 候選 | 階段點／新交付 | 文件 git 狀態 | 原票 FORBIDDEN | 期望 |
| --- | --- | --- | --- | --- | --- | --- |
| S1 | 本次實際完成 | 唯一 | 達點＋有新交付 | tracked clean | 僅列檔名 | 執行 align；`git commit -o` 單檔（`[GOVERNANCE-SYNC]`）；對話直接回報；原票 lifecycle／checkpoint 不變 |
| S2 | 本次實際完成 | 唯一 | 達點＋有新交付 | tracked dirty | — | 略過並回報 dirty 路徑；不寫檔 |
| S3a | 本次實際完成 | 唯一 | 達點＋有新交付 | 未追蹤、未 ignore | — | 寫前 status 為 `??`（非空）→ 略過並回報 |
| S3b | 本次實際完成 | 唯一 | 達點＋有新交付 | 未追蹤、被 ignore | — | 回填；`git commit -o` 失敗 → 報告「已回填、未提交（未追蹤）」；不 `git add` |
| S4 | 本次實際完成 | 零（無成果文件） | — | — | — | 不執行、不輸出 |
| S5 | 本次實際完成 | 零（有文件但歸屬不明） | — | — | — | 不猜測、不執行，一行說明 |
| S6 | 本次實際完成 | 多個 | — | — | — | 不猜測、不執行，一行說明（列候選） |
| S7 | 本次實際完成 | 唯一 | 未達點或無新交付 | — | — | 不執行，一行說明（含無對應基準） |
| S8 | 已收束重入（RETURN） | — | — | — | — | 不觸發 |
| S9 | 本次實際完成 | 唯一 | 達點＋有新交付 | 任一 | — | Skill 不可用或權限被拒 → 「未完成呼叫」；回填中斷 → 「回填未完成」；提交失敗 → 「已回填未提交」＋dirty 路徑；不回滾、不重試、不開 incident、不回頭視原票未收束 |
| S10 | 本次實際完成 | 唯一 | 達點＋有新交付 | tracked clean | 明確禁止或權限拒絕 | 略過並回報，不擴權 |
| S11 | 本次實際完成 | 唯一（缺前綴舊 5 欄文件，訊號①成立） | 達點＋有新交付 | tracked clean | 僅列檔名 | 歸屬成立；依 Skill 舊格式規則回填；不因缺前綴整類排除 |
| S12 | align 自身提交之後 | — | — | — | — | 不遞迴（非 Ticket 收束，提交不帶票前綴） |

## Acceptance Criteria

> **Rev A.1 AC 修訂閉環（RGR cycle 1，2026-10-20）**：①AC-4／AC-5 由 `[可達性]` 改標 `[存在性]`（內容不變，只改證據層級標示；真實路徑可達性屬分階段驗收第②階段待驗，見 U1；Rev A.2 撤回「裁決⑤明示豁免」之誤引）；②AC-6／AC-7 判斷類步驟加逐步 `檔案:行號` 要求（收緊證據來源，預期不變）；③AC-3、AC-8、`git diff --stat` 綁定 `<DIFF_REF>..<DIFF_END>`（提交前為 `<DIFF_REF>` 對工作樹），與 HC6 一致。**兄弟 AC 重驗**：AC-1／AC-2 之計數（新列逐字授權句 `grep -cF`＝1、`projman` 命中數）不受「可檢查點」補註影響（已重跑核對）；AC-9 新列 `→`／禁用詞／控制詞計數不變（已重跑 python 列檢查）；AC-10、AC-11 之母體與判準不變；AI→AC 對照與 R0／Cross-Review 授權對照以 AC 編號引用，不受改標影響。沒有 AC 被移除、縮減或豁免；真實路徑可達性未被豁免，屬分階段驗收第②階段待驗（Rev A.2）。

> **AI→AC 對照**：AI-0→收尾實錄 AI-0 receipt（AC-11 前置）；AI-1→AC-1、AC-2、AC-3、AC-4、AC-5、AC-9；AI-2→AC-1、AC-2、AC-3、AC-4；AI-3→AC-3、AC-8；AI-4→AC-2、AC-3、AC-4、AC-5、AC-9；AI-5→AC-6、AC-7；AI-6→AC-10、AC-11。每條 AC 的路徑皆在 ALLOWED 兩檔或其唯讀核對範圍內。

- [x] **[存在性]** AC-1：`ssot-governance.md` 轉移表資料列由 13 增為 14，新列位於 `已收束重入` 列之前；新列含 D-13 授權句**逐字**（`grep -cF` 該句＝1）、`MUST AUTO-CHAIN`、`Skill(projman)`、`git commit -o`、`[GOVERNANCE-SYNC]`、`Agent closeout 於本次呼叫實際完成`；`sv/SKILL.md` Step 11 新增第 3 項（`收束後成果校正` 恰 1 次）且該項只含引用；`表列外之 tracked dirty 檔案才判 BLOCKED` 於 `.claude/**/*.md` 命中 0。（驗證 2026-10-20：轉移表 `| ` 列 15→16＝資料列 13→14；新列 `ssot-governance.md:159`、`已收束重入` 在 `:160`；D-13 授權句逐字 True 且全檔僅 1 處；`MUST AUTO-CHAIN`／`Skill(projman)`／`git commit -o`／`[GOVERNANCE-SYNC]`／`Agent closeout 於本次呼叫實際完成` 皆含；`sv/SKILL.md:213` `收束後成果校正` 恰 1 次；舊句 `tracked dirty 檔案才判 BLOCKED` 於 `.claude/**/*.md` 命中 0。）
- [x] **[存在性]** AC-2（不複製）：D-13 授權句於 `.claude/**/*.md` 僅 `ssot-governance.md` 一處；`projman`（不分大小寫）於 `sv/SKILL.md` 命中行數＝1（新第 3 項），於 `uc`／`qa`／`compact` 的 SKILL.md 命中＝0；`grep -rl '^## 階段成果表' ` 指令字面於 `ssot-governance.md`、`sv/SKILL.md` 命中＝0；Governance Artifact 清單表列仍為 5 條。（驗證：D-13 片段僅 governance 一檔；`projman` 命中 sv 1 行〔:213〕、governance 1 行〔:159〕、uc／qa／compact 各 0；`grep -rl` 指令與 `^## 階段成果表` 字面於 governance／sv 皆 0；清單表列 5 條。）
- [x] **[存在性]** AC-3：`git diff -U0`（範圍：提交前為 `<DIFF_REF>` 對工作樹，提交後為 `<DIFF_REF>..<DIFF_END>`，`DIFF_END`＝`/qa` Step 4.5 交付提交；收尾紀錄與 align 提交不計，回指 HC6，Rev A.1）於 `ssot-governance.md` 僅兩個 hunk（L11、新列）、於 `sv/SKILL.md` 僅一個 hunk（新行）；`sed -n 13,19p ssot-governance.md | git hash-object --stdin` 與 AI-0 記錄之 `46016506…` 相同（清單表位元組不變）。（驗證：提交 `93dd64f4`，範圍 `499d583b..93dd64f4`；governance 2 hunk〔L11、:159〕、sv 1 hunk〔:213〕；清單表 L13–19 hash `46016506` 不變。）
- [x] **[存在性]**（Rev A.1 由 `[可達性]` 改標：本條為位置交叉核對；真實路徑之可達性屬分階段驗收第②階段，由 REVIEW→CLOSED 審查方依收束判讀與呼叫結果確認，見 U1）AC-4（觸發 → 條款 → Skill → 更新 → 保存 → 回報）：producer 證據欄逐環附 `檔案:行號`——觸發事實 producer＝`sv/SKILL.md:210`、`:212`（開票時 live；執行後回填最終行號）；consumer 引用＝`sv/SKILL.md` 新第 3 項；GO 列＝`ssot-governance.md` 新列；Skill 契約＝`projman/SKILL.md:242–254`；寫前檢查／保存＝`projman/SKILL.md:248–249`；觀察者＝對話回報、`git log -1 --format=%H -- plans/roadmap/projman-outcomes.md`、transcript `tool_use` 順序（`projman/SKILL.md:251`）。RAPID MODE ON／OFF 兩列（`ssot-governance.md:148–149`）皆收斂於 Agent closeout，故經新列涵蓋，無需另列。producer 證據（最終檔）：觸發事實 `sv/SKILL.md:210`、`:212`；consumer 引用 `sv/SKILL.md:213`；GO 列 `ssot-governance.md:159`；RAPID ON／OFF `ssot-governance.md:148–149`；Skill 契約 `projman/SKILL.md:242–254`、寫前檢查 `:248`、保存 `:249`、回報／session id `:251`、自動／手動 `:252`；定位規則 `projman/SKILL.md:41–50`；觀察者＝對話回報、`git log -1 --format=%H -- plans/roadmap/projman-outcomes.md`、transcript `tool_use`（Codex 亦以 `REACHABILITY: closeout-auto-align:sv/SKILL.md:212` 回報 (B)）。（驗證：位置交叉核對見 AI-4 勾選處與下 producer 證據。）
- [x] **[存在性]**（Rev A.1 由 `[可達性]` 改標：文字依據之位置核對，非路徑實走）AC-5（重入與不遞迴）：RETURN 分支不觸發之文字依據（新列左欄「非『已收束重入』」、`sv/SKILL.md` 第 1 項、`ssot-governance.md` 最小冪等回傳）與 align 非 Ticket 收束、提交不帶票前綴之文字依據（新列右欄、`projman/SKILL.md:253`）各附 `檔案:行號`。producer 證據（最終檔）：RETURN 不觸發 — `ssot-governance.md:159` 左欄「非『已收束重入』分支」與 MUST NOT「在『已收束重入』分支觸發」、`:160`（已收束重入列）、`:170`（最小冪等回傳）、`sv/SKILL.md:210`；不遞迴 — `ssot-governance.md:159` MUST NOT「遞迴（align 非 Ticket 收束……）」、`projman/SKILL.md:253`。（驗證：位置交叉核對見 AI-4 勾選處與下 producer 證據。）
- [x] **[行為性][示例推演，標明 stub-equivalent／非真實自動觸發]** AC-6：given 暫存 repo 已建立並斷言起始形狀、expect 狀態矩陣 S1、S2、S3a、S3b、S4、S5、S6、S7、S8、S9、S10、S12 逐案結果符合；`git commit -o` 單檔提交不夾帶他檔（C1）；提交訊息 `[GOVERNANCE-SYNC] …` 通過 `commit-msg` regex；失敗案（C9）依實際進度報告（提交失敗＝「已回填未提交」＋dirty 路徑；Skill 不可用＝「未完成呼叫」、不聲稱已回填）、模擬原票狀態位元組不變；判斷類結果逐步附最終條文 `檔案:行號`（first-hand），無行號之步驟不計（Rev A.1）。（驗證：S1、S2、S3a、S3b、S4、S5、S6、S7〔C13〕、S8〔C11〕、S9、S10、S12 逐案結果見收尾實錄 §4.1／§4.2；`git commit -o` 單檔不夾帶他檔〔C1：`files=[plans/roadmap/demo-outcomes.md]`，other.md 仍 staged〕；regex OK；C9 提交失敗＝「已回填未提交」＋dirty 路徑、原票狀態 hash 未變；判斷類步驟皆附最終條文行號。**標 `[stub-equivalent]`／`MISSING-INTEGRATION-TEST`，非真實自動觸發。**）
- [x] **[行為性][示例推演，標明 stub-equivalent]** AC-7：given 缺前綴舊 5 欄文件與被收束票文字明確指名該文件（S11）、expect 歸屬成立；given 含已 align 之 ID A 與從未 align 之 ID B、expect B 以文件首次提交為預設錨點、A 的校正不改 B 的「最近 align」（C8）；docs-only 交付仍出現於 `git log <錨點>..HEAD` 候選列示；判斷類結果逐步附最終條文 `檔案:行號`（first-hand），無行號之步驟不計（Rev A.1）。（驗證：C7／S11 歸屬成立；C8：B1 錨點＝首次提交 `b1d27000…`、A1 校正僅改 A1 行、docs-only 提交 `2c61997` 與 align 提交 `029a47b` 皆列於 `git log <錨點>..HEAD`；見 §4.1／§4.2。**標 `[stub-equivalent]`。**）
- [x] **[存在性]** AC-8（D-16）：`git diff`（範圍同 AC-3，Rev A.1）核對 `:11` 僅該句改動，語意與 `compact/SKILL.md:362` 一致（保留於工作樹、不納入提交、不阻斷、表列檔案照常提交），且措辭未改動 `qa/SKILL.md:173–175`（該檔零 diff）。（驗證：`git diff -U0` 該 hunk `@@ -11 +11 @@` 單句；語意對齊 `compact/SKILL.md:362`；`qa/` 零 diff；`git diff --name-only 499d583b..93dd64f4` 僅兩檔。）
- [x] **[存在性]** AC-9：新列內 `→` 計數＝0、`保證`／`自癒`／`必定補判` 計數＝0（以新列所在單行為範圍取樣）、控制詞僅 `MUST AUTO-CHAIN`；`ssot-governance.md` 檔內其他列位元組不變。（驗證：新列 `→`＝0、`保證`／`自癒`／`必定補判`＝0、`|`＝4、控制詞僅 `MUST AUTO-CHAIN`；governance 其餘行與 HEAD 位元組相同〔差異索引僅 L11〕。）
- [x] **[存在性]** AC-10（限定交付聲明）：收尾實錄載明：真實收束觸發**不是本票 AC**；本票結果如實記為「觸發／未觸發（含原因）」；PM2-E 預設未驗，除非審查方依 session id 與依序工具呼叫證據確認；U1／U2／U3 現況；示例推演＝非真實 Skill 執行／非真實自動觸發（`MISSING-INTEGRATION-TEST`）；自動鏈為模型契約、無機械後盾；不宣稱 PM2 通過或 Projman 全部自我驗證完成；Skill L248／L249 邊角列 Observation。（驗證：見收尾實錄 §8。）
- [x] **[存在性]** AC-11：QA（Step 0／4.5）、SV（含 Codex Cross-Review）依「審查要求」全數執行並有 receipt（Cross-Review receipt 綁定 `DIFF_REF..DIFF_END`）；冊面 `reviews` 四欄各填實測值（`traycer=N_A`，實際 verdict 另記 checkpoint）；`CLOSED` 限審查方，執行端最高至 `REVIEW`；`DIFF_END` 與純紀錄提交分開（Hard Constraints 6）。（驗證：見收尾實錄 §5；QA Step 0／4.5 PASS、SV 前置＋structural_verify PASS、R0 FORWARD、Codex 首次 REPORT_FIX→INC-001 executor 1 輪 pass→ADVISORY〔receipt 已複製於 §5〕；冊面 reviews 於 Step 11 寫入並 read-back；`CLOSED` 限審查方。）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉及 ALLOWED 檔案（範圍同 AC-3；收束後由新列觸發之 align 提交寫入 `projman-outcomes.md` 不在此範圍內，回指 HC6，Rev A.1）

## 決策紀錄（開票階段，供審查方核對）

1. **D-12 採納**：plan 為 `RECOMMENDED_ONLY`；本票以 Header 層「審查要求」落實，理由見 Hard Constraints 7（時序依賴＋新增自動行為）；若審查方認為可省略，屬另行裁決。
2. **授權句 `本票` 的指稱**：D-13 逐字句中的「本票」在 Transition Table 語境易生歧義；AI-1 於句前加「（逐字；「本票」＝被收束之票）」限定，句本身逐字不改。
3. **新列位置**：置於 `已收束重入` 列之前（左欄含「非已收束重入」，語意相鄰）；不新增小節、不改其他列。
4. **失敗路徑含 Skill 不可用／權限被拒**：屬條款自身失敗處置（略過並回報），不更動 Gate 對 `PERMISSION` 的既有處理。
5. **不新增 Gate 入口 `check commit`**：Gate 入口矩陣以 `/qa` Step 4.5 為 commit 落點（`ssot-governance.md` 入口矩陣句）；`/compact` Auto Commit 同樣不呼叫；align 單檔提交比照，不修改 Gate。

## Parent source（embedded）

- Parent 絕對路徑：`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261017-projman-plan.md`（Rev C）；plan-local item：`T03`；Selected item 文字與相關約束見本票首部；Plan Gate／eligibility：PASS／ALLOWED；Parent 讀取時間：2026-10-20 13:3x–13:4x（本地）。
- 上游診斷：`.tmp/ticket-author/20261017-projman-diag.md`（Fixture false／Gate PASS／PARTIALLY_CONFIRMED）。

## 收尾實錄（執行端；2026-10-20，Mode: normal；活動時戳 UTC+8）

> 本節為耐久載體（`.tmp/`／`tmp/` 證據於收口當刻複製進來）。本票於開票後被修訂至 Rev A.2，以下實作與驗證以 Rev A.2 現值為準。

### 1. 基線與 AI-0

見 AI-0 勾選處；`DIFF_REF=499d583b`（`BASE_COMMIT` 相同）；開工 `registry_set_status IN_PROGRESS`（revision 915 起算）。

### 2. AI-1／2／3 實作與逐字比對

- AI-1：`ssot-governance.md:159` 新列（1527 字元），與票面 AI-1 區塊逐字相等；轉移表資料列 13→14。
- AI-2：`sv/SKILL.md:213` 新第 3 項，與票面 AI-2 區塊逐字相同。
- AI-3：`ssot-governance.md:11` 單句替換，與票面 AI-3 目標區塊逐字相同。
- 全檔差異：`git diff --stat`＝`ssot-governance.md` 3 行（+2／−1，含 L11 一行替換）、`sv/SKILL.md` +1；hunk 數 G=2、S=1；兩檔皆 LF、CRLF 0。

### 3. AI-4 核對

見 AI-4 勾選處（producer／consumer `檔案:行號` 全表）。

### 4. AI-5 示例推演（`[stub-equivalent]`／`MISSING-INTEGRATION-TEST`）

**限定聲明**：以下 `git` 輸出為暫存 repo 內**真實指令輸出**（腳本與輸出在 `tmp/PJMAN-AUTOALIGN-01/`，已逐字複製於此；scratch 清理被權限拒絕，檔案仍在原處）；條款判讀由執行端依最終條文推演，**非真實 `Skill(projman)` 執行、非真實自動觸發**。

#### 4.1 `git` 機械行為（逐字輸出）

```text
first-commit anchor: b1d2700095c5b4653da59e65f845b0e95bcbe05a
=== C1 tracked clean + other.md dirty/staged ===
pre-state status(doc)=[] ls-files=[plans/roadmap/demo-outcomes.md]
commit-msg regex: OK
committed: 029a47b files=[plans/roadmap/demo-outcomes.md ]
after: other.md staged=[other.md ] untracked=[noise.txt ] status(doc)=[]
=== C8 per-ID: A1 line changed, B1 untouched; anchors ===
B1 last-align line: 4:- 最近 align：無 -- 19:- 最近 align：無 
A1 last-align line: 15:- 最近 align：2026-10-20 b1d2700095c5b4653da59e65f845b0e95bcbe05a 範圍 A1
B1 (never aligned) default anchor = first commit: b1d2700095c5b4653da59e65f845b0e95bcbe05a
docs-only commit for candidate listing:
2c61997 [GOVERNANCE-SYNC] docs: rules-like change
029a47b [GOVERNANCE-SYNC] docs(roadmap): demo align
=== C2 tracked dirty (simulate other session edit) ===
status(doc)=[ M plans/roadmap/demo-outcomes.md] -> non-empty => skip write, report dirty path: plans/roadmap/demo-outcomes.md
=== C3a untracked not ignored ===
status(new)=[?? plans/roadmap/new-outcomes.md] -> non-empty (??) => skip
=== C3b untracked + ignored ===
status(ign)=[] ls-files=[]
error: pathspec 'plans/roadmap/ign-outcomes.md' did not match any file(s) known to git
exit=1 (non-zero => report: backfilled, not committed (untracked); no git add)
=== C9 commit fails (pre-commit hook) ===
pre-commit REJECT
exit=1
after fail: status(doc)=[ M plans/roadmap/demo-outcomes.md] => report 'already committed? NO; 已回填未提交' + dirty path: plans/roadmap/demo-outcomes.md; retries=0
original-ticket state hash unchanged: yes
=== C11/C12 recursion check: align commit message has no ticket prefix beyond GOVERNANCE-SYNC family ===
[DEMO-IGN-01] ignore
done
```

#### 4.2 條款判讀（每步附最終條文 `檔案:行號`，first-hand）

| 案 | 判讀 | 條文依據（最終檔） |
| --- | --- | --- |
| C1／S1 | 唯一歸屬、文件 tracked clean、他檔 staged／untracked → 執行；`git commit -o <doc>` 只含該檔（輸出：`files=[plans/roadmap/demo-outcomes.md]`，`other.md` 仍 staged、`noise.txt` 仍 untracked）；提交訊息通過 regex | `ssot-governance.md:159`（保存：`git commit -o`、`[GOVERNANCE-SYNC]`、提交僅含本次 align 產物）；`projman/SKILL.md:249` |
| C2／S2 | 文件 ` M`（模擬他 session 修改）→ 寫前 status 非空 → 略過並回報 dirty 路徑、不寫檔 | `ssot-governance.md:159`（「非空則略過並回報」）；`projman/SKILL.md:248` |
| C3a／S3a | 未追蹤且未 ignore → status 為 `??`（非空）→ 略過 | 同上 `:159`、`projman/SKILL.md:248` |
| C3b／S3b | 未追蹤且被 ignore → status 為空 → 回填；`git commit -o` 失敗（`error: pathspec … did not match`，exit 1）→ 回報「已回填未提交」＋路徑，不 `git add` | `ssot-governance.md:159`（失敗回報四態、MUST NOT `git add`）；`projman/SKILL.md:249`（未追蹤分支） |
| C4／S4 | 無成果文件 → 不執行、不輸出 | `ssot-governance.md:159`（「根本沒有成果文件時不輸出」）；`projman/SKILL.md:246` |
| C5／S5 | 有文件但三訊號皆不成立 → 不猜測、不執行，一行說明原因 | `ssot-governance.md:159` 左欄①與 MUST NOT「左欄任一條件不成立時猜測歸屬或強行執行」；`projman/SKILL.md:246` |
| C6／S6 | 兩份皆含 `^## 階段成果表` 且議題皆相符 → 非唯一合理候選 → 不猜測、不執行，一行說明（列候選） | 同上 `:159`；`projman/SKILL.md:46`（候選判定）、`:246` |
| C7／S11 | 缺前綴舊 5 欄文件、被收束票文字明確指名該文件（訊號①）→ 歸屬成立，不因缺前綴整類排除 | `ssot-governance.md:159` 左欄①（「票前綴僅輔助，缺前綴舊文件不整類排除」）；`projman/SKILL.md:46` |
| C8 | B1 從未 align → 預設錨點＝文件首次提交（輸出 `b1d27000…`）；A1 校正僅改 A1 的「最近 align」行（`15:- 最近 align：2026-10-20 b1d27000… 範圍 A1`），B1 兩處仍為 `最近 align：無`；docs-only 提交 `2c61997`、align 提交 `029a47b` 均出現於 `git log <錨點>..HEAD` 候選列示（未因前綴／目錄排除） | `ssot-governance.md:159`（每 ID 範圍與預設錨點依 Skill；`git log` 僅輔助）；`projman/SKILL.md:188`、`:191`、`:195` |
| C9／S9 | 提交失敗（pre-commit exit 1）→ 文件仍 ` M` → 回報「已回填未提交」＋dirty 路徑 `plans/roadmap/demo-outcomes.md`；模擬原票狀態 hash 未變（`yes`）；retries＝0。Skill 不可用或權限被拒 → 回報「未完成呼叫」，不聲稱已回填；回填中斷 → 「回填未完成」 | `ssot-governance.md:159`（四態回報、MUST NOT 回滾／重試／開 Gate incident／回頭視原票未收束）；`projman/SKILL.md:250` |
| C10／S10 | 原票 FORBIDDEN 僅列該成果文件檔名 → 視為票內範圍排除而執行；另有明確禁止或權限拒絕 → 略過並回報 | `ssot-governance.md:159`（「被收束票 FORBIDDEN 中僅列檔名……」） |
| C11／S8 | 已收束重入（`lifecycle=REVIEW` 且票檔在 `deliver/`）→ 不觸發 | `ssot-governance.md:159` 左欄「非『已收束重入』分支」與 MUST NOT「在『已收束重入』分支觸發」、`:160`（已收束重入列）、`:170`（最小冪等回傳）；`sv/SKILL.md:210` |
| C12／S12 | align 提交訊息 `[GOVERNANCE-SYNC] …`，非 Ticket 收束 → 不再觸發 align | `ssot-governance.md:159` MUST NOT「遞迴（align 非 Ticket 收束……）」；`projman/SKILL.md:253` |
| C13／S7 | 唯一候選但該 ID 無新交付或未達可檢查點 → 不執行，一行說明原因 | `ssot-governance.md:159` 左欄②（「可檢查點……且該 ID 自其錨點起有未盤點的新交付」）；`projman/SKILL.md:172`（align 使用時點） |

註：4.1 末行 `git log -1` 顯示 `[DEMO-IGN-01] ignore` 為腳本收尾列印的最後一筆暫存 repo 提交訊息，與 C12 判讀無關（C12 屬條文判讀，非該指令輸出）；C3b 的 `.gitignore` 提交使用演練用票前綴 `[DEMO-IGN-01]`，僅存在於暫存 repo（`tmp/PJMAN-AUTOALIGN-01/r/`，未清除）。

### 5. QA／SV／R0／Cross-Review 結果（2026-10-20；範圍 `499d583b..93dd64f4`）

- **QA**：`gate_ledger.py check qa` exit 0；`verify_checklist.sh` PASS（提交前 diff 為空＝無變更；提交後重跑 ALLOWED 2/2、禁區 0、新增 4 行／估算 30、Config 新鮮度正常）；Step 1–3.5 N/A（docs preset）；Step 4 SKIPPED — DEFERRED-TO-DOCSYNC（FORBIDDEN 命中 `.claude/PROGRESS.md`）；Step 4.5：`check commit` exit 0，提交 `93dd64f4`（`[PJMAN-AUTOALIGN-01]` 前綴，stage 僅 ALLOWED 兩檔），`DIFF_END=93dd64f4` 寫入 `tmp/verify_cmd.conf`。Step 0 evidence record（複製自 `tmp/PJMAN-AUTOALIGN-01/evidence/step0_93dd64f4….txt`）：`TICKET_ID=PJMAN-AUTOALIGN-01`／`WORKTREE=D:/DevSources/AndroidProj/SmartCameraApp1`／`COMMIT_SHA=93dd64f46f559d78c1b23cd0cd380f7a1c14f623`／`DIFF_REF=499d583b`／`DIFF_END_RESOLVED=93dd64f4`／`MEASURED_LINES=4`。
- **SV**：`check sv` exit 0；`git diff --name-only HEAD` 為空；Stage A 因 docs preset N/A；`structural_verify.sh`（A/B/C/D）PASS。
- **R0**：cross-reviewer subagent **FORWARD**（6 列皆 INFO，無 ACTIONABLE／UNDECIDABLE／AUTHORITY），CLASSIFICATION＝NORMAL；記錄 `.tmp/fail-advisor/R0_PJMAN-AUTOALIGN-01_20261020_154745.md`（非版控；摘要已複製於此）。payload 以 stub codex（PATH `/d/…` 形式並斷言 `command -v`）生成，stub 未呼叫真實 Codex。Codex 首次 REPORT_FIX 後重跑**未**另派 R0（變更僅 `.tmp/sv_report.md` 文字，diff 端點與 payload 之 diff 內容未變）——如實揭露。
- **Codex Cross-Review**：第 1 次 **REPORT_FIX**（`REPORT_FIX_SOURCE=codex`）：RF-1「Guard producer 判定錯誤」——SV 報告把 `Skill(projman)` 可呼叫性以 PARTIAL 降級，Codex 要求提供 production producer 證據或改判 (C)。處置依 Gate：開 `INC-001`（gate-point sv、verdict REPORT_FIX），executor 層 round 1 策略＝補第一手 producer 證據（`e024aeab-3635-4b0f-afb9-36f87503110b.jsonl` 第 3277 行〔1 起算〕模型 `Skill(projman, base …)` `tool_use`，第 3278 行 `tool_result` `Launching skill: projman`，其後 `825076db` 提交 `projman-outcomes.md`）並重述分類為 (B)、不主張真實觸發已通過；僅改 `.tmp/sv_report.md`，不改 ALLOWED 檔；重跑→第 2 次 **ADVISORY**（exit 0），round 1 `pass`、INC-001 closed（rounds 1／failed 0）。BLOCKER `ROUND` 未遞增。**receipt（複製進耐久載體）**：`TICKET=PJMAN-AUTOALIGN-01 VERDICT=ADVISORY DIFF_SCOPE=499d583b..93dd64f4 COMMIT=93dd64f46f559d78c1b23cd0cd380f7a1c14f623 TS=20261020_155026 REACHABILITY=closeout-auto-align:sv/SKILL.md:212`。
- **Codex ADVISORY 三則（非阻斷，原意）**：①真實 auto-align 行為尚未驗證，AC-6／7 僅 `[stub-equivalent]`，解除條件＝一次真實 closeout 成功呼叫 `Skill(projman)` 並保存結果證據；②`ssot-governance.md:159` 四態與 `projman/SKILL.md:250` 三態字面分歧，未證實造成行為影響，解除條件＝統一兩處契約（屬 `projman` Skill 後續修補，本票 FORBIDDEN）；③`projman/SKILL.md:248–249` 未追蹤分支可達性邊角屬既有邏輯、不在本 diff。

### 6. 殘餘與 Observation

- 自動鏈為模型契約、**無機械後盾**；模型遵循度（U1）、`Skill(projman)` 權限提示行為（U2）、transcript 格式穩定（U3）皆維持 UNKNOWN／首次觸發觀察，不因本票通過而升級。
- Skill 內部邊角（不修）：`projman/SKILL.md:248` 與 `:249`；`:35` 節奏句與新列之關係為解讀；`:250` 三態字面與新列四態不同。後續若修補 Skill，屬獨立票。
- 本票於開票後經另一流程（RGR）修訂至 Rev A.2，實作以 Rev A.2 為準；本 session 未重跑完整 RGR。
- scratch 清理（`tmp/PJMAN-AUTOALIGN-01/{r,rehearse.*,stub}`）：`rm -rf` 被權限拒絕，未改道刪除；`tmp/` 非版控，不影響 diff，待使用者清理或授權。
- 並行／序列：開工至收尾期間 registry 無其他 `IN_PROGRESS`；`PROGRESS.md` 仍寫 `PJMAN-OUTCOMES-01` 為 REVIEW、冊面為 CLOSED，屬 DEFERRED-TO-DOCSYNC。

### 7. `DIFF_END` 與審查範圍聲明

成果交付終點＝`93dd64f4`（`DIFF_REF=499d583b..DIFF_END=93dd64f4`），Cross-Review receipt 綁定該範圍。本節之後的票檔收尾紀錄提交（Pending→deliver、本收尾實錄、勾選）與被新列觸發的 align 提交，皆**不計入**上述範圍、不自動延伸成果審查範圍（Hard Constraints 6）；僅當後續提交改動 ALLOWED 兩檔實質內容，才視為新交付終點並重新受審。

### 8. 限定交付聲明（AC-10）

- 真實收束觸發**不是本票 AC**；本票結果如實記為「觸發／未觸發（含原因）」，見 §10（收尾後判讀，另以純紀錄提交保存）。
- **PM2-E 預設未驗**：僅在審查方依 session id 與依序工具呼叫證據確認「同回合、收束寫入後、無新使用者訊息即呼叫 `Skill(projman)`」時才可升級；手動、自填、僅有提交、無法核對或未觸發皆維持未驗。
- 示例推演＝非真實 Skill 執行、非真實自動觸發（`MISSING-INTEGRATION-TEST`，`[stub-equivalent]`）；自動鏈為模型契約、無機械後盾；漏跑僅得稱「可能於後續相關收束時補判」。
- **不宣稱** PM2 通過、自動鏈已驗證，或 Projman 全部自我驗證條件已完成；本票按限定範圍交付。
- U1／U2／U3 現況見 §6；Skill L248／L249 邊角列於 §6 Observation。
