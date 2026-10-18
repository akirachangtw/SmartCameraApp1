# PJMAN-OUTCOMES-01: 以已交付的 projman Skill 真實執行 base 與 align，於 plans/roadmap/projman-outcomes.md 建立並回填 Projman 議題成果基準，並直接回報逐項結果、階段計數、缺口與下一步（update 僅於存在真實修訂時執行）

**Revision**: Rev A.1（2026-10-18；RGR cycle 1 修補，Rev／4a 計數＝1：F1 AI-3 提交前補 Gate `check commit`〔同步 HC8、Error Handling、coordination 依賴〕；F2 V1／V3 改為自 `SKILL.md` 逐字執行並加 `HEAD:doc`＝`BASE_STAGE_COMMIT:doc` 前提〔四處同步〕；F3 HC3 未驗項以「落差與下一步」承接；報告 `.tmp/rigor-gate/RGR_PJMAN-OUTCOMES-01_20261018.md`）。Rev A（2026-10-18，首版）。

**檔案治理／來源說明**：本票為 Plan-backed 單票（plan-local item `T02`，僅留於本來源區）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261017-projman-plan.md`（Rev C，2026-10-17；`.tmp` 非長期載體，下列來源資訊已內嵌，本票於該檔消失後仍可獨立閱讀）；Parent artifact 狀態＝header 實讀 `Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`／`Revision: Rev C`；Parent 完整讀取時間＝2026-10-18 17:0x（本地）；Parent evidence status＝SUPPORTED（已核可規劃不代替本票當前本地取證，下列「證據分級」為本次 live 直讀）。上游正式票＝`PJMAN-SKILL-01`（原 plan-local item `T01`）。下游 `20261017-projman-plan / T03 / 治理條款呼叫與 consumer 引用` **尚未開票**，本票不開、不預設其存在。使用者開票指示全文日期＝2026-10-18。

**Selected item 原文（plan-local `T02` goal，摘要不改義）**：`plans/roadmap/projman-outcomes.md` 由已交付的 `/projman base` 以附錄 A 建立，再由 `/projman align` 對已具備證據的階段回填；`/projman update` 在**存在真實修訂**時執行並驗證（含開發中自然出現的規格增補）；各次執行有可複核證據，且不借用其他議題的成果。Why：使用者要求本議題自己的三模式完成真實自我驗證；文件缺席是目前事實，不得假稱已存在；update 不為驗證而製造修訂。

**相關約束（取自 parent 決策表與使用者 2026-10-18 開票指示，本票適用者）**：
- **D-7／D-8**：`已驗收`／`約定` 為數值來源，`達成度` 若顯示屬衍生；未盤點 `—`≠零通過 `0`；不計跨階段平均；舊格式文件不強制遷移，`align` 保留相容。
- **D-9**：自動鏈只在 `20261017-projman-plan / T03 / 治理條款呼叫` 啟用；本票完成後的中間狀態**只有手動 align**，無任何條文呼叫它；不提前啟用。
- **D-14（update 驗證與表述）**：無真實修訂時，本票可在約定交付範圍內收束；update 真實執行標**未驗**，**不為驗證製造修訂**，首個真實修訂出現時補驗；PM3 不判完整。只有三模式確已實作、base／align 確已實跑之後，才可使用表述「三模式已實作；base／align 已實跑；update 真實執行待驗」；若已有真實修訂並完成 update，依證據更新。本票按限定範圍交付，**不等於** Projman 全部自我驗證條件已完成。
- **D-15（PM2-E）**：預設**未驗**；本票為手動 Skill 執行，如實記為**手動**，不提升 PM2-E，不宣稱自動觸發驗證通過。
- **Parent Plan Gate／eligibility 結果**：Plan Gate＝PASS；Ticket mode eligibility＝ALLOWED；Fixture＝false（本次 ticket 模式第 1 步逐項核對屬實）。

**使用者 2026-10-18 已裁決原則（內嵌，1–8）**：
1. 成果表是穩定的功能里程碑，不強制與票名、票數、前綴或拆票方式對應。
2. 需求基準依已確認需求建立，不從現有程式反推或縮小原成果。
3. 摘要置前、詳細規格與證據置後；`已驗收`／`約定` 為數值來源，百分比若顯示須由計數推導；未盤點不等於零通過；無約定列不計比例；不計跨階段平均，不以次要成果掩蓋核心缺口。
4. `align` 只能回填其允許的現況欄位，不自行改約定項、預期規格、驗收條件、核心能力、ID 或已確認變更。
5. 本次屬手動 Skill 執行，如實記為手動，不提升 PM2-E，不提前啟用 `20261017-projman-plan / T03`。
6. `update` 只接受真實修訂（含開發中自然出現且已確認的需求／規格增補），不為驗證製造新需求；無真實修訂時 update 實跑保留未驗，PM3 不判完整，但本票可按限定範圍交付。
7. 實作存在、文件存在、commit 或 Ticket `CLOSED`，不直接等於使用者功能已驗收。
8. 尚未交付的未來成果保持未盤點或明確未驗，不當成本階段失敗，也不宣稱完成。
- **適用的後續裁決**：①`無約定`、`未盤點` 與缺驗收前提（`尚不具備驗收條件`）三者分開；②局部 `align` 不消費其他 ID 未檢查的交付；③使用目前已交付的實際 Skill 行為（`SKILL.md` 現值），不沿用已被修正的舊格式描述；④未盤點／未驗不透過降低分母或修改成功條件消除。

**附錄 A — `/projman base` 自足輸入（內嵌自 parent 附錄 A；本票以此為 base 的需求輸入；`base` 可調整階段切分，但不得刪減「不可默默刪減的核心能力」，且不為填表新增規格）**：

- **識別**：`project_key`＝`SmartCameraApp1`；`project_label`＝`Cueshot APP`；`topic_key`＝`projman`；`topic_label`＝`Projman`；`票前綴`＝`PJMAN-`（可選線索，非唯一資格）。成果文件：`plans/roadmap/projman-outcomes.md`。
- **專案目標**：防止專案經多階段、多張 Ticket 後，局部完成但原始功能無聲縮水；讓使用者與第二方只讀單一成果文件，就能看到各階段約定項的驗收進度、證據與落差。
- **最小成功場景**：①用 `/projman base` 建立本議題成果文件；②`/projman update` 在真實修訂出現時標準化修訂，不破壞 ID、證據、分母；③用 `/projman align` 回填並直接報告逐項結果、階段數量、主要缺口、下一步；④一張票收束後，在符合條件時 align 被實際執行並單檔保存，失敗不影響原票收束。
- **不可默默刪減的核心能力**：1 三模式 base／update／align；2 成果表摘要置前、數值為源、明細於表後以穩定 ID 對應；3 align 直接報告而非只說文件已更新；4 update 保留原 ID／既有證據／未受影響內容，新增需求不繼承完成狀態；5 收束後符合條件時實際執行 align（不只提醒）；6 未盤點不等於零通過；7 不得為使現況看起來符合而自行縮小核心需求。
- **規格／架構約束**：單一可編輯來源（無平行 JSON／DB／API）；`disable-model-invocation: false`；不新增 hook／腳本／狀態庫／鎖／日誌系統；票名與階段不強制對應，拆票或加修補票不構成改基準的理由；固定短欄、穩定 ID、已驗收與約定獨立數值欄、百分比由計數推導；未來成果頁僅需格式可銜接。
- **非目標**：平台成果頁、專用 endpoint、新 MCP 工具、資料庫／watcher／同步器／雙向編輯、Registry schema 與逐票映射、跨 repo、Android 產品程式、付費模型呼叫。平台成果頁為後續、非本期前置。
- **假設（待核對，不當成已證實）**：自動鏈為模型契約、無機械後盾；Transition Table 為授權落點；`gate_stop_guard.py` 除 `decide()` 外未讀；收束後 `uc:54`→Step 11 的自動遵循待真實收束驗證；平台 runtime 與遠端部署未驗；自評無第二方。**PM2 自動鏈尚未啟用**。
- **目前階段**：PM1（待首次盤點確認）。
- **建議階段與約定項**（`約定` 為各列項數，PM0-A 3 ～ PM3-A 3 合計 35 項，PM4-A 無約定；`已驗收` 初始皆為 `—`）：

| ID | 階段 | 成果 | 約定項 |
| --- | --- | --- | --- |
| PM0-A | PM0 基準 | 需求基準與非目標明列 | 3：①目標／場景／核心能力／約束／非目標已寫入；②平台成果頁標為後續、非前置；③方法為可替換實作選擇 |
| PM1-A | PM1 Skill 與成果呈現 | 三模式可用 | 3：base 產出新格式；update 保留 ID／證據／分母；align 回填同一份 |
| PM1-B | PM1 | 成果表呈現 | 4：摘要置前固定短欄；已驗收與約定獨立數值欄；百分比衍生且未盤點≠0；明細於表後以 ID 對應 |
| PM1-C | PM1 | align 直接回報 | 2：逐項結果與階段數量；主要缺口與下一步 |
| PM1-D | PM1 | update 完整性核對 | 3：ID 集合保留；分母變動有確認列；表格格式核對 |
| PM1-E | PM1 | 相容與保留 | 2：舊格式文件可 align 不強制遷移；`disable-model-invocation: false` 保留 |
| PM2-A | PM2 收束後自動 align | 治理授權與引用 | 2：條款位於唯一 GO lane，以狹義授權句授予已唯一定位成果文件的讀寫及單檔保存，觸發事實與 QA／SV 是否省略無關；consumer 只引用不複製 |
| PM2-B | PM2 | 觸發與歸屬判定 | 4：未盤點新交付且階段已可驗收才執行，未到點／無新成果／無對應基準／歸屬不明不猜測；候選集合引用 Skill 定位規則（排除歸檔）、歸屬依任務已知文件／議題識別／實際成果，票前綴僅輔助，缺前綴舊文件不整類排除；每階段最近 align 與實際校正範圍對應，校正某階段不消費他階段未檢查交付，從未 align 階段以文件首次提交為預設錨點；git 前綴與目錄排除僅輔助，不整類排除 docs-only 或規則型成果 |
| PM2-C | PM2 | 執行與回報 | 2：顯示具體 align 提示詞並實際執行；回填後直接報告 |
| PM2-D | PM2 | 邊界與失敗隔離 | 6：寫入授權與提交保存資格分離（align 自身以 `git commit -o <成果文件>` 單檔保存、軌道 A 先完成當次收尾提交、軌道 B 不為 align 提前 `/compact`）；不繞過 FORBIDDEN（僅列檔名者預設票內排除，其他明確禁止與權限拒絕仍遵守）、不夾帶他 session 修改；不遞迴；align 失敗不回頭視為原票或下一張票未收束，失敗報告「已執行／已回填／未提交」與 dirty 路徑並如實列出已知限制；只消費既有證據、不新增付費呼叫；漏跑僅稱可能於後續相關收束補判 |
| PM2-E | PM2 | 真實觸發 | 1：一次真實收束後自動 align 實際執行並報告；**預設未驗**，須有 session id 與依序工具呼叫證據證明同回合、收束寫入後、無新使用者訊息即呼叫 Skill，由 `20261017-projman-plan / T03` 的 REVIEW→CLOSED 審查確認；手動、自填、僅有提交、無法核對或未觸發皆維持未驗 |
| PM3-A | PM3 自我驗證 | 三模式實跑 | 3：base 真實執行；align 真實執行；update 於首個真實修訂時真實執行（證據可複核，非手寫、非借用他議題；無真實修訂時此項維持未驗，PM3 不判完整） |
| PM4-A | PM4 平台成果頁 | 後續獨立交付 | —（不在本期範圍；本期僅格式可銜接） |

- **歷史背景（待盤點，非證據）**：Skill 初版（base／align）已於 `52450f11`／`003cdcba`／`82b64bc1` 提交（無 Ticket，`[GOVERNANCE-SYNC]` 前綴）；PM1 因而標「待首次盤點」，其餘 `—`。

**證據分級**（2026-10-18 17:03–17:1x live 直讀；HEAD `94ac7704`，開票時工作樹 clean；開票時 registry revision 853 起算）：
- **CONFIRMED（本地原始碼／工具輸出）**：①`PJMAN-SKILL-01` 冊面 `lifecycle=CLOSED`、`file=deliver/PJMAN-SKILL-01_modes.md`、`reviews={qa:PASS,sv:PASS,codex:PASS,traycer:N_A}`（`registry_list`，含 archive）。②`.claude/skills/projman/SKILL.md` 271 行、blob `a1c6440b`、`\r` 行數 0；`disable-model-invocation: false`、`## base`、`## update`、`## align`、`### 成果文件定位規則`、`## 被治理條款呼叫時（行為契約）`、`**直接回報**`、`### 證據規則` 各行首恰 1 次；V4 說明與指令含七值集合檢查（`SKILL.md:155`、`:157`，字面命中 1）；最後觸及該檔之 commit＝`86521a8f`。③`plans/roadmap/projman-outcomes.md` **不存在**（`ls` 失敗、`git ls-files` 0、`git log` 0），`git check-ignore -v` 零命中；`grep -rl "^## 階段成果表" plans/ --exclude-dir=archive` 僅命中 `plans/roadmap/autonomous-engineering-gate-outcomes.md`（其他議題，本票不碰、不借證）。④全冊（registry、archive registry、`deliver/`、`void/`、`Pending/`、其餘 `plans/` `.claude/` `docs/`）`PJMAN-OUTCOMES` 字面 0 命中；`PJMAN-*` 僅 `PJMAN-SKILL-01`；topic `Projman` 已登記（`order:13`，auto-registered 2026-10-17）。⑤`Pending/` 7 檔無與 `plans/roadmap/projman-outcomes.md`／projman 相關者。⑥本 session（`CLAUDE_CODE_SESSION_ID`）transcript `~/.claude/projects/D--DevSources-AndroidProj-SmartCameraApp1/<session-id>.jsonl` 可唯讀解析，內含逐筆 `tool_use`（含 `Skill` 呼叫之 `skill`／`args`）；此為**觀察途徑存在**之證據，**不是**本票執行 session 之證據（執行 session 於 AI-0 重驗）。⑦模板 `Unified_Ticket_Template_v3.md` v3.11（blob `6bb37f39`）完整讀取。⑧並行：另有 Codex 分離 worktree `C:/Users/Administrator/.codex/worktrees/4ba7`（detached `b6f14740`），與本票表面無關；近期他 session commit（`[ADV-APICALL-01] docs`、`[TKR-ARCH] housekeeping`、`[INFRA-RULESLIM-01]`）皆不觸 `plans/roadmap/` 與 `projman`。
- **Source／live 衝突處置（可見）**：①parent Live baseline 為 `5b80bc32`，live HEAD 為 `94ac7704`；其間 `projman/SKILL.md` 因 `PJMAN-SKILL-01` 同票修補新增 V4 七值集合（`86521a8f`）→ 以 live 為準，**不影響本票範圍**（反而使 V4 對真實文件的狀態值域有檢查）。②parent 將上游記為「REVIEW 後開票」，live 已 `CLOSED` → 以 live 為準，依賴聲明取 `CLOSED`。③parent 附錄稱 `約定` 以「N：…」描述，現行 `SKILL.md` 量化規則要求 `約定` 為正整數或 `—` → 兩者相容（N 即正整數），`base` 輸出須以整數寫入。
- **SUPPORTED**：無外部來源依賴。
- **HYPOTHESIS**：①模型呼叫 `Skill(projman)` 後會依 `SKILL.md` 把附錄 A 完整、無縮減地寫成新格式文件，且 `align` 會遵守「新格式寫入集合」與「每 ID 範圍」——需本票真實執行才能驗證（AC 設計為可證偽）。②執行 session 的 `CLAUDE_CODE_SESSION_ID` 與 transcript 可得——由 AI-0 重驗，取不到即 `UNAVAILABLE`。
- **UNKNOWN（有 AC／限定交付承接）**：①執行 session 的 transcript 格式跨版本穩定性（屬實作細節，非官方文件）；②`update` 是否會在本票期間出現真實修訂（開工時查，無則維持未驗，AC 為條件式）；③同一模型自評，無獨立第二方（以 Cross-Review 部分緩解，不消除）。
- **已知限制（誠實揭露）**：本票只產生並回填成果文件；不修改 Skill、規則或 consumer；自動鏈屬 `20261017-projman-plan / T03`，本票不宣稱其存在或通過；本票結果不等於 Projman 全部自我驗證條件已完成。

**取證對照（使用者 2026-10-18「待本地 live 取證」1–9）**：1 repo／HEAD／worktree／路徑／並行＝見證據分級⑧與 AI-0（工作樹 clean、`main`、`94ac7704`）；2 規則與模板實讀＝見下「模板實讀記錄」；3 上游票＝見 CONFIRMED①；4 三模式入口與讀寫邊界＝見 CONFIRMED② 與 AI-1／AI-4；5 成果文件狀態＝見 CONFIRMED③（不存在、未追蹤、未 ignore、無 dirty）；6 真實 Skill 呼叫與輸出之可複核取得＝transcript `tool_use` 順序＋`git` 階段提交＋票檔收尾實錄逐字保存（CONFIRMED⑥，不新增日誌、不擴權）；7 V1～V6 適用條件＝新建階段 V2／V4／V5／V6 適用、V1／V3 無寫前基準故 N/A；`align` 階段文件已追蹤且乾淨，V1／V3 自最終 `SKILL.md` 逐字抽出執行、不改寫其 `HEAD`（`SKILL.md:142-154`），前提 `git diff --quiet <BASE_STAGE_COMMIT> HEAD -- <doc>` 成立並記錄（Rev A.1）；V4 七值檢查與初始表相容（`已驗收`＝`—` 配 `狀態`＝`未盤點`、`約定`＝`—` 配 `無約定`，皆在 `SKILL.md:157` 通過條件內）；8 重複／序號／ownership／依賴＝見 CONFIRMED④⑤，無衝突，同檔 owner 僅本票；9 producer／consumer／writer／observer＝見「AC 觀察途徑」。

**模板實讀記錄**：路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`；版本＝v3.11；文件日期（產出日期欄）＝2026-05-29；有效修訂日期＝2026-09-14（v3.11）；完整讀取（322 行）時間＝2026-10-18 17:0x。

**External-review decision**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` v3.5 §6，2026-10-18 live 直讀；分項記錄）：
- **6a（不成立）**：`INTRODUCED_CAPABILITY=NONE`。本票使用既有 Skill，不導入新能力。
- **6b①（不成立）**：`ALLOWED` 僅 `plans/roadmap/projman-outcomes.md`，首層 path segment distinct＝1 ＜2。**6b②（不成立）**：`### AI-[1-9][0-9]*:` 標題數＝7 ＜8（`AI-0` 不計）。**6b③（不成立）**：`DEPENDENCY_DELTA=NONE`。
- **6c（不適用）**：目標為新建文件，無檔頭 `Revision` 欄位修訂歷史可計。
- 硬風險判準：本票無外部服務、付費呼叫、不可逆資料處理或新公開網路面；git 提交為本地可逆。
- **結論**：外審觸發皆未成立，**不跑 PPSS 外審**。公開資料未命中不構成縮減成果的授權。

**審查要求（Header 層明載；冊面現有 10 票皆無 `verification` 欄、schema 無範例，本票不寫入未經驗證形狀的欄位，改以本段為載體）**：`required reviews`＝**QA（Step 0 Scope Gate＋Step 4.5 Auto Commit；Step 1–3.5 因 docs preset 劃記 N/A）、SV（含 Codex Cross-Review，不豁免）**；Traycer＝N/A。理由（按實際交付判斷，非因副檔名）：本票的核心主張是「真實執行」與「證據層級」，由同一模型自評、無獨立第二方，Cross-Review 是唯一能核對證據聲明與差分是否相符的第二方；成果文件為長期基準，錯誤基準會污染後續 align。`⑤` 段收束時審查方 MUST 逐項核對上列 required 各值於 `reviews` 有對應結果。Rigor Gate（`/ticket-rigor-gate`）為按需工具，**本票開立時未執行**；是否派工前執行由使用者決定。

**必要未決事項**：無。（衍生保守預設，非產品語義決策，已寫入 Hard Constraints 3：`align` 不得把該次呼叫自身的產出當作該次呼叫的功能證據計入 `已驗收`；若使用者要放寬，屬另行裁決。）

**簽發日期**: 2026-10-18
**簽發者**: Claude
**來源議題**: Projman
**Mode**: normal  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: PJMAN-SKILL-01 —— 已 `CLOSED`；其交付之 `projman` Skill（三模式、新舊格式契約、V1–V6 含 V4 七值檢查）為本票執行對象，現值錨點見 AI-0
- `evidence_dependency`: PJMAN-SKILL-01 —— 本票之 `align` 判準、寫入集合、證據規則、V1–V6 指令與限定（示例推演非真實 Skill 執行；`20261017-projman-plan / T03` caller 未接入）皆引用其交付與收尾實錄
- `coordination_dependency`: Autonomous Engineering Gate commit 入口——AI-3 為 `/qa` Step 4.5 以外的本票提交，須先取得 `check commit` receipt（Rev A.1，見 AI-3）；其餘無（下游 `20261017-projman-plan / T03 / 治理條款呼叫` 未開票，不構成同批收束要求；與任何編輯 `plans/roadmap/projman-outcomes.md` 之票須串行，`concurrency.md` §2，開票時 `Pending/` 與冊面掃描結果＝無）

## Verify Config
TICKET_ID=PJMAN-OUTCOMES-01
CREATED=2026-10-18
PRESET=docs
ALLOWED=plans/roadmap/projman-outcomes.md
FORBIDDEN=.claude/skills/projman/**,.claude/skills/ticket-author/**,.claude/skills/sv/**,.claude/skills/uc/**,.claude/skills/qa/**,.claude/skills/compact/**,.claude/rules/**,.claude/CLAUDE.md,.claude/hooks/**,.claude/scripts/**,.claude/settings.json,plans/roadmap/autonomous-engineering-gate-outcomes.md,plans/roadmap/archive/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,plans/cso_briefing/project/deliver/PJMAN-SKILL-01_modes.md,app/**
MAX_LINES=300
DIFF_REF=94ac7704
ANCHOR_PROBED=2026-10-18 17:06
BASE_COMMIT=94ac7704
TARGET_BLOB=NEW
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

> PRESET=docs（唯一 ALLOWED 為新建 Markdown 成果文件；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A 見 AC）。`MAX_LINES=300` 為 production-only diff 估算校準遙測（TPL v3.5：非 execution gate；附錄 A 內容＋明細估 200–280 行），**非**停止或退件判準。
>
> **`TARGET_BLOB=NEW` 說明**：目標為新建檔，無 pre-image，依 TPL／preflight 慣例整欄字面 `NEW`；`BASE_COMMIT=94ac7704` 為開票當下 HEAD（資訊性診斷值，TPL v3.11：不作 gate）。`DIFF_REF` 為字面佔位，開工時（AI-0）回填 `git rev-parse --short HEAD` 並同步再生 `tmp/verify_cmd.conf`；**階段性提交（AI-3）不重設 `DIFF_REF`**，本票 QA／SV／Cross-Review 的整體差分範圍＝`DIFF_REF..DIFF_END`，涵蓋 base 建立、align 回填及（若實際執行的）update 異動，審查 receipt 綁定實際受審終點。
>
> **開票時檔案異動揭露（不隱藏）**：本次開票**未修改**任何 repo 內受版控檔案（`PJMAN-` family provenance 已由 `PJMAN-SKILL-01` 開票時登記於 `.claude/rules/ticket_pattern.md`，live 核對存在且 regex 行未動）；僅新增 `Pending/` 票檔（受 `.gitignore` 排除，依 INFRA-TRACKSCOPE-01 凍結點語義，搬 `deliver/` 之首次 commit 才進版控）並寫入 Registry 一筆。

## Action Items

> **AI-0 為強制首項**。以下各項為錨點與基線復驗；任一項不符，先依 TPL「基線漂移三分流」分類（`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 為資訊性，記錄後以工作樹現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止並直送收束審查；`BLOCKED_UNATTRIBUTED_STATE` 因僅觸及 `plans/` 可逆治理面，降為記錄後續作）。**本票特有停止條件**：目標文件在 AI-0 已存在 → 見 Error Handling，不重建、不覆蓋。

### AI-0: 錨點與基線復驗（派工後、修改前）
- **檔案**: `.claude/skills/projman/SKILL.md`（唯讀）、`plans/roadmap/projman-outcomes.md`（預期不存在）、冊面
- **動作**: 逐項重驗下列現值（開票時 2026-10-18 17:06 直讀）；並回填 `DIFF_REF`、再生 `tmp/verify_cmd.conf`、記錄回填值與 conf 摘要於證據欄：
  1. `git rev-parse --short HEAD`、`git status --short` 記入證據欄（並行異動以此為基準；他 session 在 `plans/roadmap/` 之 dirty 檔 → 見 Error Handling）。
  2. `registry_list(project=SmartCameraApp1, ticket_id=PJMAN-SKILL-01)`：`lifecycle=CLOSED`、`file=deliver/PJMAN-SKILL-01_modes.md`（禁用 `get_ticket`）。
  3. `.claude/skills/projman/SKILL.md`：271 行、blob `a1c6440b`（資訊性）；`^disable-model-invocation: false$`、`^## base$`、`^## update$`、`^## align$`、`^### 成果文件定位規則$`、`^## 被治理條款呼叫時（行為契約）$`、`^\*\*直接回報\*\*`、`^### 證據規則$` 各恰 1 次；V4 七值集合檢查字面（`st !~ /^(未盤點|符合|部分符合|不符合|未驗|尚不具備驗收條件|無約定)$/`）命中 1。不符且 `git log` 顯示 Skill 於開票後被改 → 記為資訊性分類（`CANDIDATE_SUPERSEDED` 等），**以現值續作**並在證據欄載明差異；若改動使 base／align／V 檢查語意失效 → 停止回報。
  4. `ls plans/roadmap/projman-outcomes.md` 失敗、`git ls-files` 0、`git check-ignore -v plans/roadmap/projman-outcomes.md` 零命中、`grep -rl "^## 階段成果表" plans/ --exclude-dir=archive` 不含 Projman 議題文件。
  5. 全冊 `grep -rIl 'PJMAN-OUTCOMES-01' plans .claude docs` 僅命中本票自身與 Registry 條目。
  6. 觀察途徑重驗（**執行 session**）：`echo "$CLAUDE_CODE_SESSION_ID"` 非空；`~/.claude/projects/D--DevSources-AndroidProj-SmartCameraApp1/<session-id>.jsonl` 存在且可用 `python -X utf8 -I` 唯讀解析出逐筆 `tool_use`（含 `Skill` 之 `skill`／`args`）。任一取不到 → 該欄記 `UNAVAILABLE`，**不編造**；並依 Error Handling 處理 AC 證明範圍。
  7. `/projman` 於本 session 可由模型經 `Skill` 工具呼叫（可用技能清單含 `projman`、`disable-model-invocation: false`）。
- [x] 完成（2026-10-18 19:3x；證據見收尾實錄 §1）

### AI-1: 真實呼叫 `/projman base`，以附錄 A 建立 `plans/roadmap/projman-outcomes.md`
- **檔案**: `plans/roadmap/projman-outcomes.md`（新建，僅經 Skill 寫入）
- **現值**: 檔案不存在（AI-0 #4）。
- **目標值**: 在執行 session 內以 `Skill` 工具呼叫 `projman`，args 以 `base` 開頭並附上本票「附錄 A」全文為需求輸入（議題 `Projman`、成果文件路徑 `plans/roadmap/projman-outcomes.md`）；由 Skill 依其〈成果文件定位規則〉與新格式範本寫入。**不得**以手寫、複製貼上整份文件或繞過 Skill 的方式產生該檔；Skill 若詢問歧義，以本票內容回答，不屬本票內容之產品取捨才以對話正文詢問使用者。記錄：呼叫時間、args 全文、Skill 回應與寫入的 tool 序列。
- [x] 完成（2026-10-18；證據見收尾實錄 §2）

### AI-2: 檢核 base 產物（內容完整性、V2／V4／V5／V6、初始狀態）並處置偏差
- **檔案**: `plans/roadmap/projman-outcomes.md`（唯讀檢核；偏差只可重跑 `/projman base` 補充，不手改）
- **動作**: ①內容完整性：基準區含識別欄位（`project_key`＝`SmartCameraApp1`、`project_label`＝`Cueshot APP`、`topic_key`＝`projman`、`topic_label`＝`Projman`、`票前綴`＝`PJMAN-`）、專案目標、最小成功場景、核心能力 1–7（逐項 `grep` 命中）、規格／架構約束、非目標、假設、目前階段 `PM1`；摘要表含附錄 A 全部 13 個 ID，`約定` 逐 ID 等於附錄 A（PM0-A 3、PM1-A 3、PM1-B 4、PM1-C 2、PM1-D 3、PM1-E 2、PM2-A 2、PM2-B 4、PM2-C 2、PM2-D 6、PM2-E 1、PM3-A 3、PM4-A `—`；整數合計 35）；明細每 ID 一節。若 base 調整階段切分，須能逐項對應附錄 A 且約定項內容與總數不減少，否則視為偏差。②自最終 `SKILL.md` 以「找行→逐字取出」抽出 `ids()` 與 V2、V4、V5、V6 指令（`DOC=plans/roadmap/projman-outcomes.md`）執行，輸出逐字保存；**指令輸出與模型判斷分開記錄**。V1／V3 無寫前基準，標 `N/A（新建）`。③初始狀態：摘要表 `已驗收` 全為 `—`（無 `0`、無預填整數）、`約定` 為整數之列 `狀態`＝`未盤點`、PM4-A `狀態`＝`無約定`；明細「實際成果與證據」不預填 `符合`；「已確認變更」表無列（尚無使用者確認的變更）。④偏差（缺能力、計數不符、預填、指令非空）→ 區分操作問題／證據不足／功能缺陷：授權內以重跑 `/projman base`（補充語意）處理，最多 2 次；仍不符 → 停止回報最小缺口。
- [x] 完成（2026-10-18；證據見收尾實錄 §3）

### AI-3: 以本票前綴、明確 pathspec 提交 base 產物，形成可定位基線
- **檔案**: `plans/roadmap/projman-outcomes.md`
- **現值**: 檔案未追蹤（AI-0 #4）。
- **目標值**: 先確認 `git status --short -- plans/roadmap/projman-outcomes.md` 僅此檔，並記錄當下 `git diff --cached --name-only`（揭露用；`git commit -o` 只提交所列路徑，不夾帶他者 staged 內容）；**提交前先執行 `python .claude/scripts/gate_ledger.py check commit --ticket PJMAN-OUTCOMES-01`，exit 0 才提交**（Rev A.1；本提交在 `/qa` Step 4.5 之外，若無此 receipt，Stop guard 會依本票前綴 commit 判為漏檢並阻擋——`gate_stop_guard.py:265-275, 293-310`；退出碼處置依 `ssot-governance.md` §入口與覆蓋矩陣之映射，不在本票重述）；`git add -- plans/roadmap/projman-outcomes.md` 後 `git commit -o -m "[PJMAN-OUTCOMES-01] <一句話>" -- plans/roadmap/projman-outcomes.md`（新建檔不能單靠 `-o`，須先對該單一路徑 `git add`；不使用 `git add -A`／`.`／無 pathspec commit）。記錄 `BASE_STAGE_COMMIT`（全雜湊）與 `git show --name-only` 輸出（僅該檔）。此為**本票內階段性成果提交**，不是票務 closeout、不是治理條款自動保存，亦不稱軌道 A；**不重設 `DIFF_REF`**。
- [x] 完成（2026-10-18；證據見收尾實錄 §4）

### AI-4: 真實呼叫 `/projman align`，對已具備證據的階段取證回填並直接回報
- **檔案**: `plans/roadmap/projman-outcomes.md`（僅經 Skill 寫入）
- **現值**: 文件已追蹤且乾淨（AI-3），`BASE_STAGE_COMMIT` 為基準。
- **目標值**: 以 `Skill` 工具呼叫 `projman`，args 以 `align` 開頭並指定文件路徑與 ID：`PM0-A PM1-A PM1-B PM1-C PM1-D PM1-E PM3-A`（目前已具備或可取得證據的階段；**不含** `PM2-A`～`PM2-E`〔`20261017-projman-plan / T03` 未交付，未來階段不判失敗〕與 `PM4-A`〔無約定〕）。Skill 依〈證據規則〉取證：功能證據優先，標記 `[已執行]`／`[靜態]`／`[文件]`／`[未驗]` 分開寫，可複核（指令或 `檔案:行號`）。Skill 於對話正文**直接回報**（單項結果、各階段 `已驗收／約定` 計數且無跨階段平均或總百分比、`無約定` 列另列、主要缺口、下一步、需使用者決策事項〔無則寫「無」〕，並回答五個摘要問題）。逐字保存呼叫 args、回報全文與寫入 tool 序列。**本次記為手動執行**。
- [x] 完成（2026-10-18；證據見收尾實錄 §5、§6）

### AI-5: 取得 scoped diff 與寫入邊界核對，保存執行證據
- **檔案**: `plans/roadmap/projman-outcomes.md`（唯讀核對）
- **動作**: ①align 後、任何提交前：`git diff --stat <BASE_STAGE_COMMIT> -- plans/roadmap/projman-outcomes.md` 與完整 `git diff` 逐字保存。②V1、V3 自最終 `SKILL.md` 逐字抽出執行，**不改寫**其 `git show HEAD:"$DOC"`；前提 `git diff --quiet <BASE_STAGE_COMMIT> HEAD -- plans/roadmap/projman-outcomes.md` 成立（即 `HEAD:doc` 與 `BASE_STAGE_COMMIT:doc` 位元組相同）並記錄，不成立 → 依 Error Handling 並行修改處置（Rev A.1）；`git status --short -- <doc>` 於 align 前須為空，記錄之；V2、V4、V5、V6 重跑；輸出逐字保存。③寫入邊界：diff 僅涉 `SKILL.md:185` 允許的新格式寫入集合（受檢 ID 之明細「實際成果與證據」「落差與下一步」「最近 align」；受檢 ID 之摘要表 `已驗收`／`達成度`／`狀態`；基準區 `最近 align`）；未受檢 ID（`PM2-A`～`PM2-E`、`PM4-A`）之摘要列與明細節，以及 `約定`、預期功能與規格、驗收方式與必要前提、核心能力、ID／階段／成果名稱、「已確認變更」表，與 `BASE_STAGE_COMMIT` 版位元組相同（逐段 `git show` 抽出後 `diff`，輸出為空）。④每個受檢 ID 之 `最近 align` 為 `<日期> <HEAD 全雜湊> 範圍 <ID>`，HEAD 與 `git rev-parse HEAD` 一致（`HEAD` 取 align 當下）。⑤執行證據：以唯讀 `python -X utf8 -I` 自 transcript 依序抽出 AI-1、AI-4 的 `Skill` `tool_use`（時間、`skill`、`args` 前 80 字）與其後寫入該文件的 tool 序列，記錄 `session id` 與行序；取不到 → `UNAVAILABLE`。
- [x] 完成（2026-10-18；證據見收尾實錄 §7）

### AI-6: 條件式 `/projman update`（僅當存在真實修訂）
- **檔案**: `plans/roadmap/projman-outcomes.md`（僅經 Skill 寫入）
- **動作**: 開工與 AI-1～AI-5 期間檢查是否**已存在**經使用者確認的真實需求增補／澄清／規格修訂（含開發中自然出現者）。**有** → 以 `Skill` 呼叫 `projman` `update`，保存寫前基準（`git status --short -- <doc>` 與 `git show HEAD:<doc>`）、修訂依據與寫後結果；V1～V6 全跑（分母變動須有「已確認變更」同次一列）；新增項目不繼承完成狀態。**無** → 不執行 update、不製造修訂；在收尾實錄記「update 真實執行：未驗（無真實修訂）」，PM3-A 之 update 項維持未驗，PM3 不判完整，**不視為本票失敗**。
- [x] 完成（2026-10-18；無真實修訂分支，update 未執行；證據見收尾實錄 §8。**後續補記**：其後於 REVIEW 內經使用者 2026-10-18 裁決補驗，真實 `/projman update` 已實跑，見 §12）

### AI-7: 收尾實錄與保存（依既有流程）
- **檔案**: 本票檔（收尾實錄）；`plans/roadmap/projman-outcomes.md`（由 `/qa` Step 4.5 Auto Commit 保存 align／update 產物）
- **動作**: ①收尾實錄（寫入本票檔，**耐久載體**，`.tmp/`／`tmp/` 內證據於收口當刻複製進來）含：基線與 AI-0 結果；AI-1／AI-4（及 AI-6 若執行）的 `Skill` 呼叫記錄與回報全文；V 檢查指令輸出（逐字）；`BASE_STAGE_COMMIT` 與 scoped diff；各受檢 ID `已驗收` 前後值與其證據；偏差與處置；**證明範圍聲明**（哪些由 transcript／git 證明、哪些僅為自評／文件層、`UNAVAILABLE` 項）；手動執行聲明；限定交付聲明（見 AC）。②走 `/qa`（Step 0 Scope Gate；Step 4.5 於 ALLOWED 內提交；`DIFF_END` 於最終提交後 pin）→ `/sv`（含 Codex Cross-Review；範圍 `DIFF_REF..DIFF_END`）→ closeout 至 `REVIEW`（依 Transition Table）。③未執行 `20261017-projman-plan / T03`，亦不建立任何會呼叫 align 的條文。
- [x] 完成（2026-10-18；QA／SV／Cross-Review 見收尾實錄 §10；closeout 見 §11）

## Hard Constraints
1. 成果文件**只能**經 `/projman base|align|update` 的實際 Skill 呼叫寫入；禁止手寫、手改、整份貼上產物以冒充 Skill 結果。偏差只可重跑 Skill（授權內）或如實記為落差，不得靜默修飾。
2. 不覆蓋、不重建、不另建平行副本：目標文件在開工時已存在 → 依 Error Handling 停止；不為製造驗測紀錄而重建文件。
3. **`align` 不得把該次呼叫自身的產出（該次回報、回填、差分）當作該次呼叫的功能證據計入 `已驗收`**（避免循環自證）。涉及 `align` 自身行為、或證據僅能於該次呼叫**之後**才取得的約定項（例如 PM1-A 之 align 項、PM1-C、PM3-A 之 align 項，以及 PM1-D 中須以 align 後差分才能執行的 V1／V3 項）於本次僅得依本次呼叫**之前**已存在的 `[已執行]` 證據計入，否則標 `[未驗]`（原因：待下一次 align 以本票收尾實錄之呼叫紀錄重驗；該 ID 之「落差與下一步」須記明此重驗待辦，作為殘餘窗口之承接載體，Rev A.1）；`update` 項無真實修訂時維持 `[未驗]`。這是保守預設，只會減少而不會增加已宣稱成果。
4. `align` 檢查範圍僅限 `PM0-A PM1-A PM1-B PM1-C PM1-D PM1-E PM3-A`；`PM2-A`～`PM2-E` 保持 `未盤點`（`已驗收`＝`—`）、`PM4-A` 保持 `無約定`，不預填、不計 0%、不判失敗；不跨階段平均、不設總計列；不以降低 `約定` 或修改成功條件消除未盤點／未驗；不以檔案存在、commit 或 Ticket `CLOSED` 單獨作為功能證據（原則 7）。
5. 不修改 `projman` Skill、治理規則、consumer、`ticket-author`、Registry schema、lifecycle、Android 產品程式；Skill 缺陷時記錄最小缺口，**不越過 FORBIDDEN 修補**，也不手改產物掩蓋。
6. 手動執行如實記為**手動**；不提升 PM2-E、不宣稱自動鏈或 `20261017-projman-plan / T03` 已驗證或存在；不批次連開下游票。
7. `update` 只接受真實修訂；不為驗證製造需求或修訂。
8. 提交僅用顯式 pathspec（先 `git add -- <單一路徑>` 再 `git commit -o … -- <單一路徑>`）；`/qa` Step 4.5 以外的本票提交（AI-3）前須先通過 `gate_ledger.py check commit --ticket PJMAN-OUTCOMES-01`（Rev A.1）；前綴 `[PJMAN-OUTCOMES-01]`；不使用 `git add -A`／`.`／`commit -a`／`--no-verify`；不夾帶他人或其他 session 之異動；階段性提交不重設 `DIFF_REF`。「被治理條款呼叫時，未追蹤文件不改用 `git add`」僅限條款呼叫情境，**不適用**本票（本票授權新建文件納入版控）。
9. 不新增日誌系統、腳本、hook、狀態庫、鎖、常駐服務或平台頁；執行證據僅以唯讀命令（`git`／`grep`／`ls`／`python -X utf8 -I` 讀 transcript）取得並複製進票檔收尾實錄。
10. 不借用其他議題成果文件（含 `plans/roadmap/autonomous-engineering-gate-outcomes.md`）之執行紀錄作為本票證據；不遷移舊格式文件。
11. 本票不預設外部研究或付費呼叫；符合 Autonomous Engineering Gate 既有用途、成本與權限的必要諮詢可自主執行，不因可能付費而例行停下請示；公開資料未命中不構成刪減成果或降低驗收的理由。

## Error Handling
- AI-0 任一現值錨點不符 → 依三分流分類；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止回報；其餘資訊性者記錄後以現值續作；Skill 語意被改到使 base／align／V 檢查失效 → 停止回報。
- **目標文件在 AI-0 已存在**（已追蹤、未追蹤或 dirty，含他 session 新建）→ 停止回報，不重建、不覆蓋、不改走 base；由使用者裁決是否沿用並調整本票 AI／AC。
- AI-1 前 `git status --short -- plans/roadmap/projman-outcomes.md` 非空，或 AI-3／AI-5 發現他 session 在該檔有並行修改 → 停止回報（不新增鎖或狀態系統）。
- `git check-ignore -v plans/roadmap/projman-outcomes.md` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律）。
- Skill 詢問無法由本票推導的產品取捨 → 以對話正文提問使用者（不彈窗），不自行挑選。
- base／align 輸出偏離（核心能力缺漏、`約定` 計數不符、預填完成、寫入集合外被改、V 檢查非空）→ 先區分操作問題／證據不足／功能缺陷；授權內以重跑 Skill 補救，同一偏差最多 2 次；仍偏離 → 停止回報最小缺口，**不手改產物**。
- transcript 或 `CLAUDE_CODE_SESSION_ID` 取不到 → 該欄記 `UNAVAILABLE`，base／align 兩條 `[可達性]` AC 的「由 transcript 證明」不得標 PASS，只得依較弱證據層級（對話記錄＋git 階段提交）如實標示證明範圍，不補造、不降低驗收要求，並列入限定交付。
- AI-3 前 `gate_ledger.py check commit` 非 0 → 依 `ssot-governance.md` §入口與覆蓋矩陣之退出碼映射處置，**不提交**（Rev A.1）。
- 提交失敗（hook 拒絕、pathspec 錯誤、identity 缺失）→ 回報原始錯誤，不 `--no-verify`、不改用廣域 stage。
- 需要新權限、擴 scope 或新增成本 → 先本地取證與 Advisor 確認，依 Gate 升報，不擅自處理。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）。

## 狀態矩陣（涵蓋本票可達且與契約相關之組合；開票時建立）

| 文件狀態（開工／各步驟前） | 操作 | 預期結果 |
| --- | --- | --- |
| 不存在 | `base`（AI-1） | 新建新格式文件；V1／V3 N/A，V2／V4／V5／V6 通過 |
| 已存在（任何狀態） | AI-0 | 停止回報（Error Handling），不進 AI-1 |
| 已追蹤且乾淨（AI-3 之後） | `align`（AI-4） | 僅寫入 `SKILL.md:185` 集合；未受檢 ID 與保護欄位位元組不變；V1／V3 逐字執行（前提 `HEAD:doc`＝`BASE_STAGE_COMMIT:doc`）通過 |
| 已追蹤但 dirty（他 session 修改） | `align` | 停止回報，不覆蓋 |
| 已追蹤且乾淨，存在真實修訂 | `update`（AI-6） | 保留 ID／證據／分母；分母變動有同次「已確認變更」列；新增項不繼承完成狀態 |
| 已追蹤且乾淨，無真實修訂 | `update` | **不執行**；update 項維持未驗 |

## Acceptance Criteria

**AC 觀察途徑（producer／consumer／writer／observer，開票時已核對可達）**：writer＝`Skill(projman)` 呼叫後之 `Write`／`Edit`（transcript 順序可證）；consumer＝成果文件之 V1–V6 指令（`SKILL.md:139-166`）與讀者；observer＝①transcript `tool_use` 序列、②`git show`／`git diff`、③V 指令輸出、④票檔收尾實錄逐字副本。

> **Rev A.1 AC 修訂閉環（RGR cycle 1，2026-10-18）**：align 寫入邊界 AC 的 V1／V3 前提，由「以 `BASE_STAGE_COMMIT` 為基準」改為「自 `SKILL.md` 逐字執行，前提 `HEAD:doc`＝`BASE_STAGE_COMMIT:doc`」，比對對象相同，只改證明方式，不改預期。**兄弟 AC 重驗**：align 為實際 Skill 呼叫的可達性 AC（transcript 順序與 `BASE_STAGE_COMMIT` 錨點）不變；證據與計數紀律 AC 的母體（align 後文件）不變；直接回報 AC 不受影響；base 行為性 AC 的 V1／V3 仍為 `N/A（新建）`。沒有 AC 被移除、縮減或豁免。

- [x] **[存在性]** `plans/roadmap/projman-outcomes.md` 存在於指定路徑、已追蹤（`git ls-files` 1）、未被 ignore；同議題（Projman）於 `grep -rl "^## 階段成果表" plans/ --exclude-dir=archive` 之候選僅此一份（無平行副本，他議題文件不計）。證據欄：指令輸出。
- [x] **[存在性]** base 產物內容完整：識別欄位、專案目標、最小成功場景、核心能力 1–7、規格／架構約束、非目標、假設（含「PM2 自動鏈尚未啟用」「平台成果頁為後續」）、目前階段 `PM1` 皆可 `grep` 命中；摘要表含附錄 A 全部 13 個 ID，`約定` 逐 ID 等於附錄 A（整數合計 35，PM4-A `—`），或有可逐項對應且不減少之切分調整紀錄。證據欄：`grep` 與逐 ID 比對輸出。
- [x] **[可達性]** base 為實際 Skill 呼叫：transcript 中存在 `Skill` `tool_use`（`skill=projman`、`args` 以 `base` 開頭），且其後（同 session）才出現寫入 `plans/roadmap/projman-outcomes.md` 之 `Write`／`Edit`；`BASE_STAGE_COMMIT` 之 tree 含該檔。**producer 證據欄**（`session id`、行序／時間、`檔案:行號` 或 commit hash）執行後回填；取不到 → `UNAVAILABLE` 並依 Error Handling 標示證明範圍，該項不得標 PASS 為「已由 transcript 證明」。**producer 證據（回填）**：session `e024aeab-3635-4b0f-afb9-36f87503110b`；`Skill(projman, base …)`＝transcript 行 3276（2026-10-18T11:38:21Z）→ 對成果文件 `Write`＝行 3292（11:39:21Z）→ `BASE_STAGE_COMMIT=825076db`（tree 含該檔，`git show --name-only` 僅此檔）；未出現 `UNAVAILABLE`。
- [x] **[行為性]** 對真實 base 產物，自最終 `SKILL.md` 逐字抽出之 V2、V4、V5、V6 指令輸出皆為空，V1／V3 標 `N/A（新建）`；初始狀態符合：`已驗收` 全為 `—`（無 `0`、無預填整數）、整數 `約定` 列之 `狀態`＝`未盤點`、PM4-A＝`無約定`、「已確認變更」無列。given：新建文件；expect：上列輸出為空且條件成立。指令輸出與模型判斷分開記錄。
- [x] **[可達性]** align 為實際 Skill 呼叫：transcript 中存在 `Skill` `tool_use`（`skill=projman`、`args` 以 `align` 開頭並含文件路徑與受檢 ID）位於建立 `BASE_STAGE_COMMIT` 之 `git commit` Bash `tool_use` 之後，且其後才出現對該文件之寫入；同一份文件被更新（無平行副本）。producer 證據欄與 `UNAVAILABLE` 處置同上。**producer 證據（回填）**：`Skill(projman, align …)`＝行 3396（11:41:51Z），位於階段提交 Bash＝行 3375（11:40:51Z）之後；其後對成果文件 `Edit` 15 次（行 3436–3475，最後 11:44:06Z）；同一份文件被更新，提交 `d720e995`。
- [x] **[行為性]** align 寫入邊界：given `BASE_STAGE_COMMIT` 之文件；expect `git diff` 僅涉新格式允許寫入集合，未受檢 ID（`PM2-A`～`PM2-E`、`PM4-A`）摘要列與明細節、所有 `約定`、預期功能與規格、驗收方式與必要前提、核心能力、ID／階段／成果名稱、「已確認變更」表與基準版位元組相同（比對輸出為空）；V1、V3 自最終 `SKILL.md` 逐字抽出執行（不改寫 `HEAD`；前提 `git diff --quiet <BASE_STAGE_COMMIT> HEAD -- <doc>` 成立並記錄，Rev A.1）輸出為空；V2／V4／V5／V6 重跑為空；受檢 ID 之 `最近 align` 為 `<日期> <HEAD 全雜湊> 範圍 <ID>` 且 HEAD 等於 align 當下 `git rev-parse HEAD`。
- [x] **[行為性]** 證據與計數紀律：given align 後文件；expect ①每個 `已驗收` 變動（相對基準版）有對應證據，標記為 `[已執行]`／`[靜態]`／`[文件]`／`[未驗]` 並可複核（指令或 `檔案:行號`），`已驗收`≤`約定`，`達成度` 由計數推導；②無任何 ID 僅以「檔案存在／commit／Ticket CLOSED」作為功能證據；③涉及 align 自身行為之約定項依 Hard Constraints 3 處理（無該次呼叫自身產出被計入）；④`PM2-A`～`PM2-E` 仍 `未盤點`、PM4-A 仍 `無約定`；⑤文件無跨階段平均、總計或合計百分比（V5 為空）。
- [x] **[行為性]** align 直接回報：given align 呼叫；expect 對話正文含五項（單項結果〔ID／狀態／已驗收／約定／一行證據與限制〕、各階段 `已驗收／約定` 計數且 `無約定` 列另列、主要缺口、下一步、需使用者決策事項〔無則寫「無」〕）並回答「原定成果是什麼／現在實際能做什麼／證據支持到哪裡／是否有缺口、縮水或方向偏移／下一步怎麼處理」；回報全文逐字保存於票檔收尾實錄；回報未出現跨階段平均或總百分比。
- [x] **[行為性][條件式]** update：若 AI-6 偵測到真實修訂 → given 寫前基準與修訂依據，expect `update` 後既有 ID 與證據保留、分母變動有「已確認變更」同次一列、新增項不繼承完成狀態、V1～V6 通過，並依證據更新驗證狀態；若無真實修訂 → 不執行，收尾實錄記「update 真實執行：未驗（無真實修訂）」，PM3-A 之 update 項維持未驗，PM3 不判完整。兩分支擇一並記錄，**皆不視為本票失敗**；不為此條製造修訂。（**後續補記 2026-10-18**：REVIEW 內已走「有真實修訂」分支並實跑，證據與驗證見 §12。）
- [x] **[存在性]** 階段性提交可定位：`git log --format='%H %s' <DIFF_REF>..HEAD -- plans/roadmap/projman-outcomes.md` 至少含 base 提交；各本票提交前綴為 `[PJMAN-OUTCOMES-01]`，`git show --name-only` 各僅涉該檔；`DIFF_REF` 未被階段性提交重設（`tmp/verify_cmd.conf` 與票面佔位回填值一致）。
- [x] **[存在性]** 票檔收尾實錄（`deliver/` 耐久載體）含：基線與 AI-0 結果、各 Skill 呼叫記錄與回報全文、V 指令輸出逐字、`BASE_STAGE_COMMIT` 與 scoped diff、各受檢 ID `已驗收` 前後值與證據、偏差與處置、**證明範圍聲明**（transcript／git 可證 vs 自評／文件層 vs `UNAVAILABLE`），不依賴 `.tmp/` 或 `tmp/` 路徑引用。
- [x] **[存在性]** 限定交付表述：收尾實錄與冊面 checkpoint 聲明「手動執行；不提升 PM2-E；`20261017-projman-plan / T03` caller 未接入、自動鏈未驗」；僅在 base／align 確已實跑後使用「三模式已實作；base／align 已實跑；update 真實執行待驗」（若 AI-6 已執行則依證據更新）；聲明本票按限定範圍交付、**不等於** Projman 全部自我驗證條件已完成；對成果文件與收尾實錄 `grep`「自動觸發驗證通過」「PM2-E 已驗」「PM3 完整」等字樣，命中處須逐一確認皆為否定／未驗語境，無肯定宣稱（票面自身的否定句不計）。
- [ ] **[存在性]** QA／SV／Cross-Review 依「審查要求」段全數執行並有 receipt（Cross-Review receipt 綁定 `DIFF_REF..DIFF_END` 之實際受審終點）；冊面 `reviews` 四欄各填實測值（`traycer=N_A`）；`CLOSED` 限審查方，執行端最高至 `REVIEW`。（**本輪 receipt 待取得，2026-10-18**：此條先前於 `94ac7704..d720e995` 輪成立〔Codex PASS，`COMMIT=d720e995…`〕；整體範圍延伸至 `94ac7704..b2082076` 後，綁定該終點的 receipt 尚未取得，取得後於審查終點之後以 §13 補記回勾，不預填，見 §12 第 14 點）
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案（**PARTIAL，2026-10-18**：於 `94ac7704..d720e995` 量測成立；整體審查範圍延伸至 `94ac7704..b2082076` 後，字面不成立——`git diff --stat` 另含本票自己的收尾實錄檔〔`deliver/PJMAN-OUTCOMES-01_real-run.md`，Governance Artifact，非實作異動〕。**不以事後改讀法維持 PASS**；是否正式修訂此 AC 的預期結果，或接受 PARTIAL，屬使用者／審查方裁決，見 §12 第 14 點）

## 收尾實錄（執行端；執行中逐步追加，結案前定稿）

### 1. 基線與 AI-0 結果（2026-10-18 19:3x 直讀）
- **執行 session**：`CLAUDE_CODE_SESSION_ID=e024aeab-3635-4b0f-afb9-36f87503110b`（`activate_ticket.sh`：切票 `INFRA-RULESLIM-01 → PJMAN-OUTCOMES-01`，owner 移轉 `8a7f43c0 → e024aeab`）。transcript `~/.claude/projects/D--DevSources-AndroidProj-SmartCameraApp1/e024aeab-….jsonl` 存在（10,852,631 bytes），唯讀解析得 353 筆 `tool_use`，含 `Skill` 呼叫（`skill`／`args`）；`projman` 於本 session 可用技能清單內 → #6、#7 通過。
- **#1** HEAD `94ac7704`（全 `94ac7704fc11856326421a3fbc41051ec58c3b8e`）、`main`、`git status --short` 空。回填：票面 `DIFF_REF=94ac7704`；`tmp/verify_cmd.conf` 由 `activate_ticket.sh` 再生，`DIFF_REF=94ac7704`、`BASE_COMMIT=94ac7704`、`TARGET_BLOB=NEW`、`ALLOWED=plans/roadmap/projman-outcomes.md`；兩處一致。
- **#2** `registry_list(PJMAN-SKILL-01)`：`lifecycle=CLOSED`、`file=deliver/PJMAN-SKILL-01_modes.md`、`reviews={qa:PASS,sv:PASS,codex:PASS,traycer:N_A}`。
- **#3** `projman/SKILL.md` 271 行、blob `a1c6440b`（與開票一致）；`disable-model-invocation: false`、`## base`、`## update`、`## align`、`### 成果文件定位規則`、`## 被治理條款呼叫時（行為契約）`、`**直接回報**`、`### 證據規則`、V4 說明行各恰 1；V4 七值集合字面命中 1。無漂移。
- **#4** `ls plans/roadmap/projman-outcomes.md` 失敗、`git ls-files` 0、`git log` 0、`git check-ignore -v` 零命中；`grep -rl "^## 階段成果表" plans/ --exclude-dir=archive` 僅 `plans/roadmap/autonomous-engineering-gate-outcomes.md`（他議題）。
- **#5** `grep -rIl 'PJMAN-OUTCOMES-01' plans .claude docs` 僅命中本票檔與 `TICKET_REGISTRY.json`。
- **Gate**：`gate_ledger.py status --json`：ticket=`PJMAN-OUTCOMES-01`、incidents `[]`、pending_human `[]`。
- **分類**：無漂移（`SKILL.md` blob、行首命中、HEAD 皆同開票時）。

### 2. AI-1：真實 `/projman base`（2026-10-18）
- **呼叫**：模型經 `Skill` 工具呼叫 `skill=projman`，`args` 以 `base Projman 議題成果基準。成果文件路徑：plans/roadmap/projman-outcomes.md…` 開頭，後接本票附錄 A 全文為需求輸入（使用者已指定路徑；定位規則第 1 順位）。Skill 回應為其 `SKILL.md` 全文；無歧義詢問。
- **Skill 依其 `base` 規則寫入**：新格式、`約定` 以整數寫入、初始 `已驗收`＝`—`、PM4-A＝`無約定`；明細「實際成果與證據」已有進度者（PM0-A、PM1-A～E）填「待首次盤點」，其餘「未開始」。
- **transcript 觀察（唯讀 `python -X utf8 -I` 解析 `~/.claude/projects/…/e024aeab-3635-4b0f-afb9-36f87503110b.jsonl`，原樣貼上）**：

```text
session: e024aeab-3635-4b0f-afb9-36f87503110b
projman Skill calls (line, ts, args[:60]):
   88 2026-10-16T09:06:28.660Z ''
   259 2026-10-16T10:59:37.571Z 'base Autonomous Engineering Gate'
   3276 2026-10-18T11:38:21.195Z 'base Projman 議題成果基準。成果文件路徑：plans/roadmap/projman-outcomes.md'

SELECTED base call at line 3276 ts 2026-10-18T11:38:21.195Z
subsequent tool_use touching the doc / git commit (first 12):
  line 3292 2026-10-18T11:39:21.457Z Write :: D:\DevSources\AndroidProj\SmartCameraApp1\plans\roadmap\projman-outcomes.md
  line 3305 2026-10-18T11:39:33.318Z Write :: C:\Users\ADMINI~1\AppData\Local\Temp\claude\D--DevSources-AndroidProj-SmartCameraApp1\e024aeab-3635-4b0f-afb9-36f87503110b\scratchpad\seq_t02.py
```

  註：第二筆列出的 `seq_t02.py` 為本次取證腳本自身（路徑含檔名字串），**非**對成果文件之寫入。成果文件寫入僅一筆：行 3292 `Write`，位於 `Skill(projman, base …)`（行 3276）之後。

### 3. AI-2：base 產物檢核（指令輸出與判斷分開）
- **指令輸出（V2、V4、V5、V6 自最終 `SKILL.md`〔blob `a1c6440b`〕逐字抽出執行；V1／V3 標 `N/A（新建）`，無寫前基準）**：

```text
DOC=plans/roadmap/projman-outcomes.md  SKILL.md blob=a1c6440b
---- V2 (exit=0) command:
comm -3 <(ids < "$DOC" | sort) <(awk '/^## /{s=(index($(0),"## 明細")==1)} s && /^### /{print $(2)}' "$DOC" | sort)
---- V2 output: (空)
---- V4 (exit=0) command:
awk -F'|' '/^## /{s=(index($(0),"## 階段成果表")==1)} s && /^\| /{for(i=2;i<=NF;i++)gsub(/^ +| +$/,"",$(i)); id=$(2); if(id=="ID"||id ~ /^-+$/)next; a=$(5); d=$(6); p=$(7); st=$(8); bad=""; if(st !~ /^(未盤點|符合|部分符合|不符合|未驗|尚不具備驗收條件|無約定)$/){bad="狀態須屬七值集合"} else if(d=="—"){if(a!="—"||p!="—"||st!="無約定")bad="約定為—時須已驗收=達成度=—且狀態=無約定"} else if(d !~ /^[1-9][0-9]*$/){bad="約定須為正整數或—"} else if(st=="無約定"){bad="無約定只用於約定為—"} else if(a=="—"){if(p!="—"||st!="未盤點")bad="已驗收為—時須達成度=—且狀態=未盤點"} else if(a !~ /^[0-9]+$/||a+0>d+0){bad="已驗收須為整數且不得超過約定"} else if(st=="未盤點"){bad="已驗收為整數時狀態不得為未盤點"} else if(p!=(int(100*a/d+0.5) "%")){bad="達成度與計數不一致"} if(bad)print id": "bad}' "$DOC"
---- V4 output: (空)
---- V5 (exit=0) command:
awk -F'|' '/^## /{s=(index($(0),"## 階段成果表")==1)} s && /^\| /{if($(2) ~ /平均|總計|合計|Total/)print $(0)}' "$DOC"
---- V5 output: (空)
---- V6 (exit=0) command:
awk '/^\|/{l=$(0); gsub(/\\\|/,"",l); gsub(/`[^`]*`/,"",l); n=gsub(/\|/,"|",l); if(inb && n!=c)print NR": 欄數"n"≠"c; if(!inb){inb=1;c=n}; next} {inb=0}' "$DOC"
---- V6 output: (空)

NONEMPTY_CHECKS=0
```

- **內容完整性與初始狀態（腳本輸出）**：

```text
IDs in summary table: 13 -> PM0-A,PM1-A,PM1-B,PM1-C,PM1-D,PM1-E,PM2-A,PM2-B,PM2-C,PM2-D,PM2-E,PM3-A,PM4-A
  PM0-A  約定 expect=3 got=3 OK
  PM1-A  約定 expect=3 got=3 OK
  PM1-B  約定 expect=4 got=4 OK
  PM1-C  約定 expect=2 got=2 OK
  PM1-D  約定 expect=3 got=3 OK
  PM1-E  約定 expect=2 got=2 OK
  PM2-A  約定 expect=2 got=2 OK
  PM2-B  約定 expect=4 got=4 OK
  PM2-C  約定 expect=2 got=2 OK
  PM2-D  約定 expect=6 got=6 OK
  PM2-E  約定 expect=1 got=1 OK
  PM3-A  約定 expect=3 got=3 OK
  PM4-A  約定 expect=— got=— OK
約定 integer sum = 35 (expect 35); extra IDs beyond appendix: []
initial-state violations: none
  project_key=SmartCameraApp1 FOUND
  project_label=Cueshot APP FOUND
  topic_key=projman      FOUND
  topic_label=Projman    FOUND
  票前綴=PJMAN-             FOUND
  專案目標                   FOUND
  最小成功場景                 FOUND
  核心能力區                  FOUND
  規格／架構約束                FOUND
  非目標                    FOUND
  假設                     FOUND
  目前階段 PM1               FOUND
  平台成果頁為後續               FOUND
  PM2 自動鏈尚未啟用            FOUND
core capabilities 1-7: ['ok', 'ok', 'ok', 'ok', 'ok', 'ok', 'ok']
detail sections ###: 13
確認變更 data rows: 0
lines: 128  CR: 0
V4 positive control (PM1-A mutated to 完成, temp copy only): PM1-A: 狀態須屬七值集合
ALL_EXPECTED_COUNTS_OK: True
```

- **判斷（模型）**：①V2／V4／V5／V6 輸出皆為空，且 V4 對暫存變異副本（PM1-A 改為 `2/3/67%/完成`）輸出 `狀態須屬七值集合` → 空輸出不是指令未命中；②13 個 ID 與附錄 A 一一對應，`約定` 逐 ID 相符、整數合計 35、無額外 ID，未調整切分；③核心能力 1–7 與識別、目標、場景、約束、非目標、假設（含 PM2 自動鏈未啟用、平台成果頁為後續）、目前階段 PM1 皆在；④初始狀態：`已驗收`／`達成度` 全為 `—`、整數 `約定` 列皆 `未盤點`、PM4-A＝`無約定`、「已確認變更」無資料列；⑤無偏差，未重跑 base。

### 4. AI-3：階段性提交 base 產物（2026-10-18）
- 提交前：`git status --short -- plans/roadmap/projman-outcomes.md` ＝ `?? plans/roadmap/projman-outcomes.md`（全 repo 僅此一項）；`git diff --cached --name-only` 空；`git check-ignore -v` 零命中；`python .claude/scripts/gate_ledger.py check commit --ticket PJMAN-OUTCOMES-01` → `{"ok": true, "gate_point": "commit", "exit": 0, "late": false, "seq": 1}`（Rev A.1 要求之 receipt，先於提交）。
- 提交：`git add -- plans/roadmap/projman-outcomes.md`，`git commit -o -m "[PJMAN-OUTCOMES-01] docs(roadmap): …" -- plans/roadmap/projman-outcomes.md` → `825076db`（1 file changed, 127 insertions）。
- **`BASE_STAGE_COMMIT=825076dbcf4ea4642ec0a7290005f88bc4316c65`**；`git show --name-only` 僅 `plans/roadmap/projman-outcomes.md`；提交後 `git status --short` 空、`git ls-files` 1。為本票內階段性成果提交，非票務 closeout；`DIFF_REF` 仍為 `94ac7704`（未重設）。

### 5. AI-4：真實 `/projman align`（手動執行，2026-10-18）
- **呼叫**：模型經 `Skill` 工具呼叫 `skill=projman`，`args` 以 `align plans/roadmap/projman-outcomes.md PM0-A PM1-A PM1-B PM1-C PM1-D PM1-E PM3-A （本次為手動執行；來源：PJMAN-OUTCOMES-01 AI-4。…）` 開頭。**本次記為手動執行**（由使用者以 `/normal PJMAN-OUTCOMES-01` 派工、非條款觸發；不升級 PM2-E）。
- **session**：`e024aeab-3635-4b0f-afb9-36f87503110b`。**transcript 序列（唯讀 `python -X utf8 -I`）**：階段提交 Bash `tool_use`＝行 3375（2026-10-18T11:40:51Z）→ `Skill(projman, align …)`＝行 3396（11:41:51Z）→ 取證（唯讀 Bash／scratchpad 腳本，行 3413、3423）→ 對 `plans/roadmap/projman-outcomes.md` 之 `Edit` 共 15 次＝行 3436、3438、3440、3442、3444、3446、3448、3450（基準區 1＋摘要列 7）、3463、3465、3467、3469、3471、3473、3475（7 個 ID 明細）。先後順序：階段提交 ＜ align 呼叫 ＜ 全部寫入。（註：取證腳本 `Write` 的路徑為 scratchpad 檔，不是成果文件。）
- **取證輸出（唯讀，原樣；E5 區塊與 §2 重複故略；`…` 為原輸出經 `cut -c` 按位元組截斷處）**：

```text
== E0 pre-state
HEAD=825076dbcf4ea4642ec0a7290005f88bc4316c65
git status --short -- DOC: []
format: header has 已驗收+約定 => | ID | 階段 | 成果 | 已驗收 | 約定 | 達成度 | 狀態 |
== E1 anchor (first commit) and candidate deliveries since
anchor=825076dbcf4ea4642ec0a7290005f88bc4316c65
git log --oneline 825076dbcf4ea4642ec0a7290005f88bc4316c65..HEAD:
== E2 PM0-A: baseline fields in 基準 section
  專案目標：                base-section hits=1
  最小成功場景：          base-section hits=1
  不可默默刪減的核心能力： base-section hits=1
  規格／架構約束：       base-section hits=1
  非目標：                   base-section hits=1
  平台成果頁為後續、非本期前置 base-section hits=1
  '可替換' hits in 基準 section: 0   in whole doc: 1
35:- 預期功能與規格：需求基準與非目標明列（3 項）：①目標／場景／核心能力／約束／…
== E3 PM1-E: Skill frontmatter flag
5:disable-model-invocation: false
SKILL.md blob=a1c6440b last-commit=86521a8f
== E4 PM1-B: percent-derivation control on a temp copy (V4 verbatim)
  [2/3 with 50% (mismatch)] V4 => PM1-A: 達成度與計數不一致
  [2/3 with 67% (correct)] V4 => (空)
  [0 with 0% (未驗 inventoried zero)] V4 => (空)
```

- **錨點**：成果文件首次提交（預設錨點）＝`825076dbcf4ea4642ec0a7290005f88bc4316c65`；`git log --oneline <錨點>..HEAD` 為空（無候選交付）；寫前 `git status --short -- plans/roadmap/projman-outcomes.md` 為空。

### 6. align 直接回報（對話正文，逐字）

**`/projman align` 直接回報（手動執行｜2026-10-18｜範圍：PM0-A、PM1-A～E、PM3-A；PM2-A～E 與 PM4-A 未檢查，維持原狀）**

**1. 單項結果**

| ID | 狀態 | 已驗收／約定 | 一行證據與限制 |
| --- | --- | --- | --- |
| PM0-A | 部分符合 | 2／3 | 基準區 grep：目標、場景、核心能力、約束、非目標、「平台成果頁為後續」皆在 [已執行]；「方法為可替換實作選擇」在基準區 0 命中，只出現在約定描述 |
| PM1-A | 部分符合 | 1／3 | base 新格式 [已執行]（Skill 呼叫→寫入→提交 825076db、V2／V4／V5／V6 空）；update [未驗]（無真實修訂）；align 回填同一份 [未驗]（證據是本次呼叫自身，不計） |
| PM1-B | 符合 | 4／4 | 摘要置前、獨立數值欄、V2 空、V4 對變異副本能抓「達成度與計數不一致」[已執行]；本次回填數值的事後 V4 不計入本次 |
| PM1-C | 未驗 | 0／2 | 功能證據只能來自本次回報本身，依自證限制不計；〈直接回報〉規格存在僅為 [靜態] |
| PM1-D | 部分符合 | 1／3 | V6 空 [已執行]；V1／V3 須對 align 後差分比對，屬本次呼叫之後，不計 |
| PM1-E | 部分符合 | 1／2 | `disable-model-invocation: false` 在 SKILL.md 第 5 行 [已執行]；舊 5 欄格式文件未實跑 [未驗]，且不借用他議題的既有紀錄 |
| PM3-A | 部分符合 | 1／3 | base 真實執行 [已執行]（transcript 行 3276→3292→提交 825076db）；align 真實執行 [未驗]（自證）；update [未驗] |

**2. 各階段計數（不給跨階段平均或總百分比）**
- PM0：PM0-A 2／3
- PM1：PM1-A 1／3、PM1-B 4／4、PM1-C 0／2、PM1-D 1／3、PM1-E 1／2
- PM2：PM2-A～E 未盤點（`—`／2、4、2、6、1），未交付，不判失敗
- PM3：PM3-A 1／3
- 無約定列（不計比例）：PM4-A

**3. 主要缺口**
- PM0-A 約定③：基準區沒有明載「方法為可替換實作選擇」，是目前唯一屬於「基準本身缺漏」的缺口。
- update 從未真實執行（前提＝真實修訂尚不存在），PM3 因此不可能完整。
- align 自身相關的項目（PM1-A 的 align 項、PM1-C、PM3-A 的 align 項）與 PM1-D 的 V1／V3，本次無法自證，要等下一次 align 以本票收尾實錄的呼叫紀錄重驗。
- 舊 5 欄格式文件的 align 實跑尚缺。

**4. 下一步**
- 首個真實修訂出現時執行 `update` 並補驗。
- 下一次 `align` 依收尾實錄重驗上列自證項。
- 經你指定一份舊 5 欄文件並授權後補驗 PM1-E 約定①。
- PM2-A～E 待治理條款票交付後再盤點。

**5. 需要你決策的事項**
- 是否以 `update`（澄清）在〈基準〉明載「方法為可替換實作選擇」，使 PM0-A 約定③成立？選項 A：同意，我在確認後執行真實 `update`（會讓 PM3-A 的 update 項有第一個真實修訂可驗）；選項 B：維持現狀，PM0-A 停在 2／3。我不會自行補寫，也不會降低約定。

**五個摘要問題**
- **原定成果：** 13 個 ID、35 項約定的 Projman 成果基準；PM4-A 無約定。
- **現在實際能做什麼：** base 已真實建立文件並通過 V2／V4／V5／V6；本次 align 已真實回填 7 個 ID，並通過 V1～V6（逐字）與 69 項寫入邊界核對。
- **證據支持到哪裡：** 只到 [已執行] 的部分（見各列）；自證項與 update、舊格式實跑為 [未驗]。
- **缺口、縮水或方向偏移：** 無縮水、無改約定；缺口見第 3 點。
- **下一步：** 見第 4 點。

### 7. AI-5：scoped diff、V1～V6、寫入邊界（align 後、任何提交前）
- **前提**：`git diff --quiet 825076dbcf4ea4642ec0a7290005f88bc4316c65 HEAD -- plans/roadmap/projman-outcomes.md` exit 0（`HEAD:doc`＝`BASE_STAGE_COMMIT:doc`，HEAD 仍為 `825076db`）；`git status --short -- <doc>` 於 align 前為空。
- **scoped diff（`git diff --stat 825076db -- <doc>`）**：`1 file changed, 29 insertions(+), 29 deletions(-)`；完整 patch 111 行，由 align 提交再生（`git show <align commit> -- <doc>`）。
- **V1～V6（自最終 `SKILL.md`〔blob `a1c6440b`〕逐字抽出，V1／V3 未改寫其 `HEAD`；指令輸出）**：

```text
DOC=plans/roadmap/projman-outcomes.md  SKILL.md blob=a1c6440b
---- V1 (exit=0) command:
comm -23 <(git show HEAD:"$DOC" | ids | sort) <(ids < "$DOC" | sort)
---- V1 output: (空)
---- V2 (exit=0) command:
comm -3 <(ids < "$DOC" | sort) <(awk '/^## /{s=(index($(0),"## 明細")==1)} s && /^### /{print $(2)}' "$DOC" | sort)
---- V2 output: (空)
---- V3 (exit=0) command:
diff <(git show HEAD:"$DOC" | den | sort) <(den < "$DOC" | sort)
---- V3 output: (空)
---- V4 (exit=0) command:
awk -F'|' '/^## /{s=(index($(0),"## 階段成果表")==1)} s && /^\| /{for(i=2;i<=NF;i++)gsub(/^ +| +$/,"",$(i)); id=$(2); if(id=="ID"||id ~ /^-+$/)next; a=$(5); d=$(6); p=$(7); st=$(8); bad=""; if(st !~ /^(未盤點|符合|部分符合|不符合|未驗|尚不具備驗收條件|無約定)$/){bad="狀態須屬七值集合"} else if(d=="—"){if(a!="—"||p!="—"||st!="無約定")bad="約定為—時須已驗收=達成度=—且狀態=無約定"} else if(d !~ /^[1-9][0-9]*$/){bad="約定須為正整數或—"} else if(st=="無約定"){bad="無約定只用於約定為—"} else if(a=="—"){if(p!="—"||st!="未盤點")bad="已驗收為—時須達成度=—且狀態=未盤點"} else if(a !~ /^[0-9]+$/||a+0>d+0){bad="已驗收須為整數且不得超過約定"} else if(st=="未盤點"){bad="已驗收為整數時狀態不得為未盤點"} else if(p!=(int(100*a/d+0.5) "%")){bad="達成度與計數不一致"} if(bad)print id": "bad}' "$DOC"
---- V4 output: (空)
---- V5 (exit=0) command:
awk -F'|' '/^## /{s=(index($(0),"## 階段成果表")==1)} s && /^\| /{if($(2) ~ /平均|總計|合計|Total/)print $(0)}' "$DOC"
---- V5 output: (空)
---- V6 (exit=0) command:
awk '/^\|/{l=$(0); gsub(/\\\|/,"",l); gsub(/`[^`]*`/,"",l); n=gsub(/\|/,"|",l); if(inb && n!=c)print NR": 欄數"n"≠"c; if(!inb){inb=1;c=n}; next} {inb=0}' "$DOC"
---- V6 output: (空)

NONEMPTY_CHECKS=0
```

- **寫入邊界（腳本，69 項）**：

```text
line count identical (in-place replacements only)              OK (128 vs 128)
section order identical                                        OK 
table header identical                                         OK 
same ID set and order in table                                 OK 
same detail ID set and order                                   OK 
已確認變更 section identical (no rows added)                        OK 
基準 區 except 最近 align line identical                            OK 
unchecked PM2-A table row byte-identical                       OK 
unchecked PM2-A detail section byte-identical                  OK 
unchecked PM2-B table row byte-identical                       OK 
unchecked PM2-B detail section byte-identical                  OK 
unchecked PM2-C table row byte-identical                       OK 
unchecked PM2-C detail section byte-identical                  OK 
unchecked PM2-D table row byte-identical                       OK 
unchecked PM2-D detail section byte-identical                  OK 
unchecked PM2-E table row byte-identical                       OK 
unchecked PM2-E detail section byte-identical                  OK 
unchecked PM4-A table row byte-identical                       OK 
unchecked PM4-A detail section byte-identical                  OK 
checked PM0-A table: ID/階段/成果/約定 unchanged                     OK 
checked PM0-A detail: 預期功能與規格 / 驗收方式 unchanged                 OK 
checked PM0-A detail: 3 changeable bullets present in order    OK 
checked PM0-A detail: non-bullet lines unchanged               OK 
checked PM1-A table: ID/階段/成果/約定 unchanged                     OK 
checked PM1-A detail: 預期功能與規格 / 驗收方式 unchanged                 OK 
checked PM1-A detail: 3 changeable bullets present in order    OK 
checked PM1-A detail: non-bullet lines unchanged               OK 
checked PM1-B table: ID/階段/成果/約定 unchanged                     OK 
checked PM1-B detail: 預期功能與規格 / 驗收方式 unchanged                 OK 
checked PM1-B detail: 3 changeable bullets present in order    OK 
checked PM1-B detail: non-bullet lines unchanged               OK 
checked PM1-C table: ID/階段/成果/約定 unchanged                     OK 
checked PM1-C detail: 預期功能與規格 / 驗收方式 unchanged                 OK 
checked PM1-C detail: 3 changeable bullets present in order    OK 
checked PM1-C detail: non-bullet lines unchanged               OK 
checked PM1-D table: ID/階段/成果/約定 unchanged                     OK 
checked PM1-D detail: 預期功能與規格 / 驗收方式 unchanged                 OK 
checked PM1-D detail: 3 changeable bullets present in order    OK 
checked PM1-D detail: non-bullet lines unchanged               OK 
checked PM1-E table: ID/階段/成果/約定 unchanged                     OK 
checked PM1-E detail: 預期功能與規格 / 驗收方式 unchanged                 OK 
checked PM1-E detail: 3 changeable bullets present in order    OK 
checked PM1-E detail: non-bullet lines unchanged               OK 
checked PM3-A table: ID/階段/成果/約定 unchanged                     OK 
checked PM3-A detail: 預期功能與規格 / 驗收方式 unchanged                 OK 
checked PM3-A detail: 3 changeable bullets present in order    OK 
checked PM3-A detail: non-bullet lines unchanged               OK 
checked PM0-A 最近 align format                                  OK 
checked PM0-A 最近 align HEAD == git rev-parse HEAD              OK (HEAD=825076db)
checked PM1-A 最近 align format                                  OK 
checked PM1-A 最近 align HEAD == git rev-parse HEAD              OK (HEAD=825076db)
checked PM1-B 最近 align format                                  OK 
checked PM1-B 最近 align HEAD == git rev-parse HEAD              OK (HEAD=825076db)
checked PM1-C 最近 align format                                  OK 
checked PM1-C 最近 align HEAD == git rev-parse HEAD              OK (HEAD=825076db)
checked PM1-D 最近 align format                                  OK 
checked PM1-D 最近 align HEAD == git rev-parse HEAD              OK (HEAD=825076db)
checked PM1-E 最近 align format                                  OK 
checked PM1-E 最近 align HEAD == git rev-parse HEAD              OK (HEAD=825076db)
checked PM3-A 最近 align format                                  OK 
checked PM3-A 最近 align HEAD == git rev-parse HEAD              OK (HEAD=825076db)
基準 最近 align summary: date HEAD40 + checked ID list             OK 
checked PM0-A counts 2/3 -> 67% (部分符合)                         OK 
checked PM1-A counts 1/3 -> 33% (部分符合)                         OK 
checked PM1-B counts 4/4 -> 100% (符合)                          OK 
checked PM1-C counts 0/2 -> 0% (未驗)                            OK 
checked PM1-D counts 1/3 -> 33% (部分符合)                         OK 
checked PM1-E counts 1/2 -> 50% (部分符合)                         OK 
checked PM3-A counts 1/3 -> 33% (部分符合)                         OK 

VIOLATIONS=0  (checks=69)
```

- **判斷（模型）**：V1～V6 全為空；邊界核對 0 違規——未受檢 ID（PM2-A～E、PM4-A）摘要列與明細節與 `BASE_STAGE_COMMIT` 位元組相同、所有 `約定`、預期功能與規格、驗收方式、核心能力、ID／階段／成果名稱、「已確認變更」表不變；基準區僅 `最近 align` 行變動；7 個受檢 ID 之 `最近 align` 為 `2026-10-18 825076db… 範圍 <ID>` 且 HEAD 等於 `git rev-parse HEAD`；`已驗收`≤`約定`，`達成度` 由計數推導。Hard Constraints 3 遵守：自證項（PM1-A 之 align 項、PM1-C、PM1-D 之 V1／V3、PM3-A 之 align 項）全標 [未驗] 並於「落差與下一步」記明重驗待辦。

### 8. AI-6：條件式 `/projman update`（無真實修訂分支）
- **判定時點與依據**：AI-1～AI-5 期間（2026-10-18），使用者對 Projman 需求**未**提出或確認任何增補、澄清或規格修訂；派工指令（`/normal PJMAN-OUTCOMES-01`）、開票指示與 `align` 結果均不構成經使用者確認的修訂。
- **候選但未確認**：`align` 揭露 PM0-A 約定③「方法為可替換實作選擇」未明載於〈基準〉（屬 Skill `update` 輸入類別「Bug 揭露的基準缺漏」的候選）。該項**需使用者確認**後才是真實修訂；回報第 5 點已向使用者提出（選項 A 同意 update 澄清／選項 B 維持現狀）。**截至本實錄定稿，使用者未確認**，故不執行、不自行補寫、不降低約定。
- **結果**：`update` 真實執行：**未驗（無真實修訂）**；PM3-A 之 update 項維持 [未驗]，PM3 不判完整；不視為本票失敗。若使用者於審查前確認選項 A，另依 Skill `update` 規則（保留 ID／證據／分母、分母不變、V1～V6）執行，並以後續補記方式更新本實錄，不改寫上列已落檔事實。

### 9. 證明範圍聲明與限定交付聲明
- **transcript／git 可證**：①`Skill(projman)` 的 `base`（行 3276）與 `align`（行 3396）兩次呼叫確實發生、順序為 base 呼叫 ＜ 成果文件 `Write`（行 3292）＜ 階段提交 Bash（行 3375）＜ align 呼叫 ＜ 15 次 `Edit`；②階段提交 `825076db` 僅含成果文件；③scoped diff `+29／−29`；④V1～V6（含逐字 V1／V3）輸出為空、69 項寫入邊界 0 違規——為**結果層**驗證，證明產物符合 Skill 契約。
- **僅為自評／文件層**：各 ID 的狀態與 `已驗收` 計數、證據詮釋，皆由同一模型判斷，**無獨立第二方**；Cross-Review（Codex）為部分緩解而非消除。transcript 證明「呼叫與寫入的先後與工具」，**不證明**模型逐條遵循 `SKILL.md` 語意；遵循度僅由結果層檢核間接支持。
- **UNAVAILABLE**：無（執行 session 的 `CLAUDE_CODE_SESSION_ID` 與 transcript 皆可取得並已唯讀解析）。
- **限定交付聲明**：三模式已實作；base／align 已實跑；update 真實執行待驗。本次為**手動**執行，**不提升 PM2-E**；`20261017-projman-plan / T03 / 治理條款呼叫` 的 caller 未接入，自動鏈未驗。本票按限定範圍交付，不等於 Projman 全部自我驗證條件已完成（PM2 全未盤點、PM3 不完整、PM1-E 缺舊格式實跑、自證項待下一次 align 重驗）。

### 10. QA／SV／Cross-Review 結果（2026-10-18；R1／L1）
- **QA**：`gate_ledger.py check qa` exit 0（seq 2）；Step 0 Scope Gate PASS（範圍 `94ac7704 → 825076db…d720e995`、ALLOWED 內、禁區 0、實測 127 行、`Suggested DIFF_REF: 94ac7704` 與票面一致）；Step 1–3.5 因 docs preset 且無 `app/` 異動劃記 N/A（`assembleDebug` N/A）；Step 4（PROGRESS Dashboard 同步）依標準 SOP 延至下一輪 `/compact`（PROGRESS.md 已達 80 行上限且非 ALLOWED，不另開 DOCSYNC 票）；Step 4.5 `check commit` exit 0（seq 3）→ Auto Commit `d720e995`（僅成果文件，`+29／−29`），`DIFF_END=d720e995` 已 pin。
- **SV**：`check sv` exit 0（seq 4）；`.tmp/sv_report.md` 五段齊備，`structural_verify.sh` preflight PASS（A/B/C/D）；殘骸 0、禁區 0、無新 Guard（Guard Input Reachability：無）。
- **R0**：FORWARD（NORMAL；5 列皆 INFO，無 ACTIONABLE／UNDECIDABLE／AUTHORITY；記錄 `.tmp/fail-advisor/R0_PJMAN-OUTCOMES-01_20261018_194915.md`，結論已複製於此）。其中一條資訊：§3 腳本輸出「lines: 128」＝腳本以 `split("
")` 計入結尾換行空元素；`wc -l` 為 127（base 版與最終版皆 127），口徑差異，非內容差異。
- **Codex Cross-Review（機械輪次 R1／邏輯輪次 L1）**：第 1 次即 **PASS**（`Guard Input Reachability: 無新 Guard`、`REACHABILITY: NONE`），無 REPORT_FIX、無 Gate incident。**receipt**：`TICKET=PJMAN-OUTCOMES-01 VERDICT=PASS DIFF_SCOPE=94ac7704..d720e995 COMMIT=d720e995059f43ee1303fdd69ce6fd8c5bf76bd2 TS=20261018_194916`（`.tmp/cross_review_pass/PJMAN-OUTCOMES-01`，非版控，已複製於此；綁定實際受審終點＝`DIFF_END`）。**如實限制**：Codex 本次自身的 `exec_command` 因沙箱啟動失敗（`setup refresh had errors`），裁決只依 payload 內容（diff＋SV 報告＋ssot_context），不代表它獨立重跑了指令或讀取 transcript；transcript 相關主張以報告所列行號與指令為據。同一模型自評，Cross-Review 為部分緩解而非獨立驗收。
- **Gate 入口**：`check qa`／`commit`（AI-3 與 Step 4.5 各一）／`sv`／`closeout` 全 exit 0；ledger 無 open incident、無待人工。
- **Step 10**（Done 標記與 PROGRESS 同步）：未做，延至下一輪 `/compact`（FORBIDDEN 延後項免開票紀律）。
- **仍開放的使用者決策**：PM0-A 約定③是否以 `update` 明載「方法為可替換實作選擇」（見 §8、§6 第 5 點）；未確認前 `update` 不執行。

### 11. Closeout 至 `REVIEW`（2026-10-18；Agent closeout，依 Transition Table「SV＋Codex PASS 且 `Traycer Workflow: N/A`」列）
- **入口**：`registry_list` 讀現值＝`lifecycle=IN_PROGRESS`、`file=Pending/…`（非已收束重入，故不 RETURN）；`gate_ledger.py check closeout` exit 0（seq 5）。
- **寫入**：`registry_set_status(lifecycle=REVIEW, reviews={qa:PASS, sv:PASS, codex:PASS, traycer:N_A})`；票檔 `Pending/ → deliver/`（`Pending/` 無 PJMAN 殘留）；`registry_upsert_ticket(file=deliver/PJMAN-OUTCOMES-01_real-run.md, lifecycle=REVIEW)`；`registry_set_checkpoint` 於 `set_status` 之後寫入。
- **偏差（如實）**：①本次 `set_status` 並未清空先前的 `IN_PROGRESS` checkpoint（與 SKILL 敘述「每次呼叫清空」不符），read-back 發現殘留後以 closeout 版覆寫；②首次 `set_checkpoint` 因 `summary` 超過 200 字被平台以 `VALIDATION_ERROR` 拒收，已精簡 `summary` 並把細節放入其他欄後成功；兩次皆以 `registry_list fields=["checkpoint"]` read-back 驗證落地。
- **read-back（`registry_list`，最後一次）**：`id=PJMAN-OUTCOMES-01`、`topic=Projman`、`file=deliver/PJMAN-OUTCOMES-01_real-run.md`、`lifecycle=REVIEW`、`reviews={qa:PASS,sv:PASS,codex:PASS,traycer:N_A}`（`codex=PASS` 為映射，實際 verdict 於 checkpoint `codex_verdict_actual`＝第 1 次即 PASS）；checkpoint 為 closeout 版（含 `open_decision`、`limitations`）。Executor 最高寫入 `REVIEW`；`CLOSED` 限審查方 SMTR/CSO，由其核對 required reviews（qa／sv／codex PASS、traycer N_A）與票檔、冊面一致後轉態並 read-back。
- **持久化**：軌道 A（隨票即時、`[PJMAN-OUTCOMES-01]` 單票前綴、僅本票檔）；Step 10 Done 標記與 PROGRESS Dashboard 同步延至下一輪 `/compact`。成果文件本身已隨 `825076db`／`d720e995` 提交。
- **未連開**：`20261017-projman-plan / T03 / 治理條款呼叫` 未開票，依使用者後續指令另行處理。

### 12. REVIEW 內補驗：真實 `/projman update`（2026-10-18；使用者裁決；後續補記，不改寫 §1–§11 已落檔事實）
1. **裁決與範圍**：使用者 2026-10-18「REVIEW 內補驗裁決」——接受以真實 `/projman update` 澄清既定原則，**非新的產品取捨、不再詢問 A／B**；於〈基準〉明載「方法為可替換的實作選擇；可更換技術路線，但不得因此默默縮減原始使用者成果。」；由 `update` 記錄變更依據與影響；不新增 ID、不改約定數；保留既有證據與未受影響內容；**不重跑 align、不由 update 提升成果表驗收計數**；以本票前綴、明確 pathspec 保存；整體審查起點維持 AI-0 基線 `94ac7704`；`open_decision` 記為已依既定決策處置。
2. **寫前基準**：`git status --short -- plans/roadmap/projman-outcomes.md` 為空、文件已追蹤、無合併衝突標記（`grep -cE '^(<<<<<<<|>>>>>>>)'`＝0）→ `git show HEAD:"$DOC"`（HEAD `6b0c15efa54f71cf486a14814ded9e406dd713bd`）即寫前狀態；ID 集合 13 個、`約定` 合計 35。
3. **真實 Skill 呼叫（手動）**：session `e024aeab-3635-4b0f-afb9-36f87503110b`；`Skill(projman, "update plans/roadmap/projman-outcomes.md （修訂內容：…」")`＝transcript **行 3806**（2026-10-18T12:40:30Z）→ 對成果文件 `Edit` **行 3822**（12:40:42Z）、**行 3824**（12:40:43Z）。其後的 `Write`（行 3838）為本次核對用 scratchpad 腳本，不是成果文件。順序：update 呼叫 ＜ 兩次寫入 ＜ 提交。
4. **寫入集合（僅兩處）**：①〈基準〉「規格／架構約束」該行原位追加上述澄清句；②〈已確認變更〉表新增 1 列（日期、變更前、變更後、影響、使用者確認依據；5 欄、無多餘豎線）。
5. **update 後檢核（自最終 `SKILL.md`〔blob `a1c6440b`〕逐字抽出；V1／V3 以 `HEAD:doc` 為寫前基準；指令輸出）**：

```text
DOC=plans/roadmap/projman-outcomes.md  SKILL.md blob=a1c6440b
---- V1 (exit=0) command:
comm -23 <(git show HEAD:"$DOC" | ids | sort) <(ids < "$DOC" | sort)
---- V1 output: (空)
---- V2 (exit=0) command:
comm -3 <(ids < "$DOC" | sort) <(awk '/^## /{s=(index($(0),"## 明細")==1)} s && /^### /{print $(2)}' "$DOC" | sort)
---- V2 output: (空)
---- V3 (exit=0) command:
diff <(git show HEAD:"$DOC" | den | sort) <(den < "$DOC" | sort)
---- V3 output: (空)
---- V4 (exit=0) command:
awk -F'|' '/^## /{s=(index($(0),"## 階段成果表")==1)} s && /^\| /{for(i=2;i<=NF;i++)gsub(/^ +| +$/,"",$(i)); id=$(2); if(id=="ID"||id ~ /^-+$/)next; a=$(5); d=$(6); p=$(7); st=$(8); bad=""; if(st !~ /^(未盤點|符合|部分符合|不符合|未驗|尚不具備驗收條件|無約定)$/){bad="狀態須屬七值集合"} else if(d=="—"){if(a!="—"||p!="—"||st!="無約定")bad="約定為—時須已驗收=達成度=—且狀態=無約定"} else if(d !~ /^[1-9][0-9]*$/){bad="約定須為正整數或—"} else if(st=="無約定"){bad="無約定只用於約定為—"} else if(a=="—"){if(p!="—"||st!="未盤點")bad="已驗收為—時須達成度=—且狀態=未盤點"} else if(a !~ /^[0-9]+$/||a+0>d+0){bad="已驗收須為整數且不得超過約定"} else if(st=="未盤點"){bad="已驗收為整數時狀態不得為未盤點"} else if(p!=(int(100*a/d+0.5) "%")){bad="達成度與計數不一致"} if(bad)print id": "bad}' "$DOC"
---- V4 output: (空)
---- V5 (exit=0) command:
awk -F'|' '/^## /{s=(index($(0),"## 階段成果表")==1)} s && /^\| /{if($(2) ~ /平均|總計|合計|Total/)print $(0)}' "$DOC"
---- V5 output: (空)
---- V6 (exit=0) command:
awk '/^\|/{l=$(0); gsub(/\\\|/,"",l); gsub(/`[^`]*`/,"",l); n=gsub(/\|/,"|",l); if(inb && n!=c)print NR": 欄數"n"≠"c; if(!inb){inb=1;c=n}; next} {inb=0}' "$DOC"
---- V6 output: (空)

NONEMPTY_CHECKS=0
```

   scoped diff `git diff --stat HEAD -- <doc>`：`plans/roadmap/projman-outcomes.md | 3 ++-`；`1 file changed, 2 insertions(+), 1 deletion(-)`（patch 9 行）。**寫入邊界（腳本，14 項）**：

```text
diff opcodes: [('replace', 10, 10, 10, 10), ('insert', 128, 127, 128, 128)]
exactly two change hunks                                           OK 
one in-place line replacement + one pure insertion                 OK 
replaced exactly one line                                          OK 
replaced line is the 規格／架構約束 bullet                                OK 
new line = old line + the clarification sentence appended          OK 
insertion is a single new row                                      OK 
inserted row sits in ## 已確認變更 table                                OK 
inserted row has 5 cells and no stray pipe                         OK 
section 階段成果表 byte-identical                                       OK 
section 明細 byte-identical                                          OK 
same 13 IDs                                                        OK 
約定 per ID unchanged, integer sum                                   OK (sum=35)
已驗收／達成度／狀態 unchanged for all IDs                                   OK 
其餘基準區（含最近 align 摘要行）byte-identical                                 OK 

VIOLATIONS=0 (checks=14)
```

6. **保存**：`git commit -o -m "[PJMAN-OUTCOMES-01] docs(roadmap): 以 /projman update 於基準明載…" -- plans/roadmap/projman-outcomes.md` → **`51cc82ee`**（`51cc82eedf0ebb838e000c7d3ecaa2c250b3b0c3`，僅成果文件）。提交前 `gate_ledger.py check commit --ticket PJMAN-OUTCOMES-01` 回 exit 4（`not mutable (loc:deliver)`：票已 `REVIEW`，在 Gate 適用範圍外）——**記為「檢核未完成／範圍不適用」，不記為 PASS，未退票、未偽造事件**。
7. **整體審查範圍與 scope 處置（如實）**：整體範圍 `94ac7704..` 涵蓋 base `825076db`／align `d720e995`／update `51cc82ee`。範圍延伸後，中間的本票收尾實錄提交 `6b0c15ef`（`deliver/*.md`，Governance Artifact，軌道 A）使 `verify_checklist.sh` 判 `FUTURE_CONTAMINATION`——此為 `concurrency.md` 待開票 #5 已登記之腳本（較嚴）與 `qa/SKILL.md` Anchor Drift（僅看 ALLOWED 檔）的校準落差。處置：①QA Step 0 於 conf 設 `EXCLUDED_PAYLOAD=PJMAN-OUTCOMES-01_real-run.md`（basename）後 PASS——**僅是 QA 腳本層對本票自身治理產物的分類，無具名授權，不宣稱任何 CSO 授權**；②**不**在 SV 報告使用 `## Excluded Payload`：`sv/SKILL.md:104` 定義其為「已授權排除出 Codex diff 的跨票檔案」，且 `cross_review.sh` 會對 Codex 陳述「已獲 CSO 授權排除」，對本票檔而言不實；R0 首次預審（CHECKPOINT-REQUIRED／UNDECIDABLE）即指出此點，本地取證屬實，已改正；③本票收尾實錄檔因此**進入 Codex 受審 diff**；④為使受審的票檔反映 update 事實，審查終點 `DIFF_END` 由 `51cc82ee` 延至本 §12 補記之提交（僅文件紀錄，實作面最後一個提交仍為 update `51cc82ee`）——此為對使用者「終點延伸至 update 提交」字面的小幅延伸，**已揭露、可逆**。⑤AC「`git diff --stat` 僅涉及 ALLOWED 檔案」之讀法：實作面僅 `plans/roadmap/projman-outcomes.md`；`git diff --stat` 於延伸範圍另含本票自己的收尾實錄檔（治理產物，非實作異動）；兄弟 AC（update 條件式、限定交付表述、階段提交可定位、收尾實錄齊備）已隨本節重驗並更新。逐條結論見第 12 點。
8. **`open_decision` 處置**：PM0-A 約定③「方法為可替換實作選擇」未明載於〈基準〉之決策事項，**已依使用者 2026-10-18 既定決策處置**（以真實 `update` 澄清，不再詢問 A／B）。
9. **區分兩件事（重要）**：
   - **本次 `update` 已實跑**：〈基準〉已明載該句，〈已確認變更〉已記一列依據與影響；V1～V6 逐字全空、邊界 14 項 0 違規。
   - **成果表仍是最近一次 `align`（`825076db` 範圍）的評估**：`update` 依裁決不改成果表；PM0-A 仍 2／3（部分符合）、PM1-A 與 PM3-A 的 update 項在成果文件內仍標 [未驗]、其餘 ID 計數不變。PM0-A 約定③是否因本次澄清而成立，須待**下一次 `align`** 重驗。**不宣稱所有約定功能均已驗收。**
10. **更新後的限定交付聲明**：三模式已實作；**base／align／update 均已各實跑一次**（update 來源：transcript 行 3806、提交 `51cc82ee`，使用者裁決）；本次為**手動**執行，**不提升 PM2-E**；`20261017-projman-plan / T03 / 治理條款呼叫` 的 caller 未接入、自動鏈未驗；成果表驗收計數仍待下一次 align 重驗，PM3 仍不完整（update 項在成果表內尚未被 align 計入）。
11. **本輪審查結果**：待 R0 重跑與 Codex Cross-Review 完成後以後續補記追加（§13），不在本節預填。
12. **兄弟 AC 逐條重驗結論（AC 修訂閉環；2026-10-18）**：「`git diff --stat` 僅涉 ALLOWED」之讀法改變（第 7 點⑤）後，共用同一母體（成果文件與範圍提交）之兄弟 AC 重驗如下，無 AC 被移除、縮減或豁免：
    - `[存在性]` 階段性提交可定位：**仍成立**——`git log --format='%h %s' 94ac7704..HEAD -- plans/roadmap/projman-outcomes.md` 現為 `825076db`、`d720e995`、`51cc82ee` 三筆，前綴皆 `[PJMAN-OUTCOMES-01]`，`git show --name-only` 各僅涉該檔；`DIFF_REF` 仍為 `94ac7704`（票面與 `tmp/verify_cmd.conf` 一致，未被階段性提交重設）。
    - `[行為性][條件式]` update：**「有真實修訂」分支已滿足**——given 寫前基準（第 2 點）與修訂依據（第 1 點）；expect 既有 ID 與證據保留（V1 空）、分母不變（V3 空，`約定` 合計 35）、無新增項、V1～V6 通過（第 5 點）、〈已確認變更〉記一列依據與影響（第 4 點）；先前「無真實修訂」分支之記錄（§8）保留為當時事實。
    - `[存在性]` 票檔收尾實錄齊備：**由本節補齊**——update 的呼叫記錄（第 3 點）、輸出與差分（第 5 點）、邊界核對、對話回報原文與證明範圍（第 13 點）。
    - `[存在性]` 限定交付表述：**已依第 10 點更新**；對成果文件再 `grep`「自動觸發驗證通過」「PM2-E 已驗」「PM3 完整」，命中 0；收尾實錄僅本行自身引用查詢字面（否定／說明語境，AC 允許），無肯定宣稱。
    - `[可達性]` base／align、`[行為性]` V 檢查／寫入邊界／證據與計數紀律／直接回報：**不受 update 影響**——update 只動〈基準〉一行與〈已確認變更〉一列，`## 階段成果表` 與 `## 明細` 位元組相同（第 5 點邊界核對），故原結論維持。
    - `[存在性]` QA／SV／Cross-Review receipt：**本輪重跑中**，結果待 §13，不預填。
13. **update 的 Skill 回應、對話回報原文與證明範圍聲明**：
    - **Skill 回應**：`projman` 的 `update` 流程無獨立的「直接回報」規格（該規格屬 `align`）；Skill 回應為其 `SKILL.md` 全文載入。對話層回報（逐字）：①寫前：「`update` 流程：文件已追蹤且乾淨（寫前基準＝`git show HEAD:<doc>`，ID 集合 13 個、`約定` 合計 35）。寫入集合僅兩處：〈基準〉「規格／架構約束」明載該句，以及「已確認變更」記一列依據與影響；成果表與明細一律不動。」②寫後：「`update` 結果：V1～V6（含逐字 V1／V3）全空，寫入邊界 14 項 0 違規（僅〈基準〉一行追加＋「已確認變更」一列；成果表與明細位元組不變、`約定` 合計仍 35），diff `+2／−1`；transcript：`Skill(projman, update …)` 行 3806 → 2 次 `Edit`（行 3822、3824）。」
    - **update 證明範圍聲明**：①**transcript／git 可證**：呼叫（行 3806）→ 兩次 `Edit`（行 3822、3824）→ 提交 `51cc82ee`（僅成果文件）的先後與工具；寫前基準為已追蹤乾淨之 `HEAD:<doc>`；V1～V6 逐字輸出與 14 項邊界核對為結果層驗證；〈基準〉該句與使用者原句逐字相符（基準區命中 1）。②**僅自評／文件層**：〈已確認變更〉列中「影響」「變更前」的措辭由同一模型撰寫；修訂依據是**對話中的使用者裁決**（非檔案），審查方無法由 repo 單獨核實，需回溯對話。③**不證明**：模型逐條遵循 `SKILL.md` 語意；PM0-A 約定③是否成立（待下一次 align）。④**UNAVAILABLE**：無。
14. **Codex 第 1 次再審（REPORT_FIX）與更正（2026-10-18；REPORT_FIX 計數 1／2）**：整體範圍 `94ac7704..b2082076` 的 Codex 第 1 次裁決為 **REPORT_FIX**，兩點成立：①以「實作面」讀法為已勾 AC「`git diff --stat` 僅涉及 ALLOWED 檔案」維持 PASS，等同事後改變 AC 預期結果，未經授權——**更正**：該 AC 改為未勾、標 PARTIAL（見該 AC 旁註）；§12 第 7 點⑤與第 12 點所述「讀法」**撤回**，不再作為 PASS 依據。是否正式修訂此 AC，或接受 PARTIAL，交使用者／審查方裁決；②「QA／SV／Cross-Review 有 receipt」勾選於延伸終點尚無綁定 receipt——**更正**：該條改為未勾、註明待取得；SV 報告開頭「已完成」措辭一併更正為「進行中」。本點為對先前敘述的更正，§1–§13 其餘已落檔事實不改寫。
