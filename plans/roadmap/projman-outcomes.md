# Projman Outcomes

## 基準
- project_key：SmartCameraApp1　project_label：Cueshot APP
- topic_key：projman　topic_label：Projman
- 票前綴：PJMAN-（可選線索，非唯一資格）
- 專案目標：防止專案經多階段、多張 Ticket 後，局部完成但原始功能無聲縮水；讓使用者與第二方只讀單一成果文件，就能看到各階段約定項的驗收進度、證據與落差。
- 最小成功場景：①用 `/projman base` 建立本議題成果文件；②`/projman update` 在真實修訂出現時標準化修訂，不破壞 ID、證據、分母；③用 `/projman align` 回填並直接報告逐項結果、階段數量、主要缺口、下一步；④一張票收束後，在符合條件時 align 被實際執行並單檔保存，失敗不影響原票收束。
- 不可默默刪減的核心能力：1 三模式 base／update／align；2 成果表摘要置前、數值為源、明細於表後以穩定 ID 對應；3 align 直接報告而非只說文件已更新；4 update 保留原 ID／既有證據／未受影響內容，新增需求不繼承完成狀態；5 收束後符合條件時實際執行 align（不只提醒）；6 未盤點不等於零通過；7 不得為使現況看起來符合而自行縮小核心需求；8 成果文件唯一寫入入口、三模式共同自主單檔保存、本輪最後一張票收束或最終交付前的最終覆蓋確認、自動流程不選回已確認錯誤的舊表（PM5，2026-10-22 增補）；寫入入口有機械強制，未經 Projman 對應模式的直接改檔會被攔截，且 base 只新建、update 只改規格相關、align 只改成果表與證據（PM6，2026-10-22 增補）。
- 規格／架構約束：單一可編輯來源（無平行 JSON／DB／API）；`disable-model-invocation: false`；不新增狀態庫／鎖／日誌系統；允許為「成果文件只能經 Projman 三模式寫入」新增一支檢查腳本與其 hook 掛載（Claude Code `PreToolUse` 加 git `commit-msg`），不得因此新增平行資料來源（PM6，2026-10-22 放寬）；票名與階段不強制對應，拆票或加修補票不構成改基準的理由；固定短欄、穩定 ID、已驗收與約定獨立數值欄、百分比由計數推導；未來成果頁僅需格式可銜接。方法為可替換的實作選擇；可更換技術路線，但不得因此默默縮減原始使用者成果。
- 非目標：平台成果頁、專用 endpoint、新 MCP 工具、資料庫／watcher／同步器／雙向編輯、Registry schema 與逐票映射、跨 repo、Android 產品程式、付費模型呼叫。平台成果頁為後續、非本期前置。
- 假設（尚未確認的技術或依賴）：自動鏈為模型契約、無機械後盾；Transition Table 為授權落點；`gate_stop_guard.py` 除 `decide()` 外未讀；收束後 `uc:54`→Step 11 的自動遵循待真實收束驗證；平台 runtime 與遠端部署未驗；自評無第二方。2026-10-22 澄清過期敘述：PM2 自動鏈已啟用（`PJMAN-AUTOALIGN-01`，2026-10-20 CLOSED），並有一次經審查方確認為自動發起的候選觸發（`e2f77049`，見 PM2-E）；唯一寫入入口、共同自主保存、最終覆蓋確認與定位回報為 `PJMAN-CLOSELOOP-01`（2026-10-22 CLOSED）交付的模型契約，真實遵循待首次觀察（見 PM5-A～D）。2026-10-22 新增 PM6 已知限制：Claude Code 端只能證明 Skill 曾被呼叫，不能證明內容對錯，內容靠區域規則判斷；Bash 寫檔（`sed -i`、重導向、`python -c`）只能做字串偵測、無法完全封死，故需 git 端保底；`.git/hooks` 不進版控，須手動部署；`git commit --no-verify` 可繞過 git 端。
- 目前階段：PM5（閉環補強；PM2 部分約定仍待真實觀察）
- 最近 align：2026-10-22 800c53948dd277d9c26759ff6080a3f451a5115b PM5-A PM5-B PM5-C PM5-D

## 階段成果表
| ID | 階段 | 成果 | 已驗收 | 約定 | 達成度 | 狀態 |
| --- | --- | --- | --- | --- | --- | --- |
| PM0-A | PM0 基準 | 需求基準與非目標明列 | 3 | 3 | 100% | 符合 |
| PM1-A | PM1 Skill 與成果呈現 | 三模式可用 | 3 | 3 | 100% | 符合 |
| PM1-B | PM1 | 成果表呈現 | 4 | 4 | 100% | 符合 |
| PM1-C | PM1 | align 直接回報 | 2 | 2 | 100% | 符合 |
| PM1-D | PM1 | update 完整性核對 | 2 | 3 | 67% | 部分符合 |
| PM1-E | PM1 | 相容與保留 | 2 | 2 | 100% | 符合 |
| PM2-A | PM2 收束後自動 align | 治理授權與引用 | 2 | 2 | 100% | 符合 |
| PM2-B | PM2 | 觸發與歸屬判定 | 2 | 4 | 50% | 部分符合 |
| PM2-C | PM2 | 執行與回報 | 2 | 2 | 100% | 符合 |
| PM2-D | PM2 | 邊界與失敗隔離 | 4 | 6 | 67% | 部分符合 |
| PM2-E | PM2 | 真實觸發 | 1 | 1 | 100% | 符合 |
| PM3-A | PM3 自我驗證 | 三模式實跑 | 3 | 3 | 100% | 符合 |
| PM4-A | PM4 平台成果頁 | 後續獨立交付 | — | — | — | 無約定 |
| PM5-A | PM5 閉環補強 | 唯一寫入入口 | 2 | 3 | 67% | 部分符合 |
| PM5-B | PM5 | 共同自主保存 | 4 | 5 | 80% | 部分符合 |
| PM5-C | PM5 | 最終覆蓋確認 | 2 | 3 | 67% | 部分符合 |
| PM5-D | PM5 | 不選回已確認錯誤的舊表 | 2 | 3 | 67% | 部分符合 |
| PM6-A | PM6 寫入入口機械強制 | 成果文件直接改檔被攔截、三模式區域受限 | — | 3 | — | 未盤點 |

## 明細
### PM0-A
- 預期功能與規格：需求基準與非目標明列（3 項）：①目標／場景／核心能力／約束／非目標已寫入；②平台成果頁標為後續、非前置；③方法為可替換實作選擇。
- 驗收方式與必要前提：讀本文件〈基準〉各欄，逐項對照三項約定。前提：無。
- 實際成果與證據：[已執行] 2026-10-18 以 awk 取本文件 `## 基準` 區（至下一個 `##`）逐字串 grep：專案目標／最小成功場景／不可默默刪減的核心能力／規格／架構約束／非目標各 1 命中，「平台成果頁為後續、非本期前置」1 命中 → 約定①②成立。[已執行] 同區「可替換」0 命中；全文「可替換」僅 1 處（PM0-A 約定描述，第 35 行）→ 約定③未見於基準區。[文件] 基準由 base 依 PJMAN-OUTCOMES-01 附錄 A 寫入（提交 825076db）。 [已執行] 2026-10-21 重驗約定③：取 `## 基準` 區逐字 grep「方法為可替換的實作選擇」命中 1（第 10 行，由真實 update 提交 `51cc82ee` 寫入；`git diff d720e995 51cc82ee` hunk 為 `@@ -10 +10 @@` 與 `@@ -127,0 +128 @@`），「平台成果頁為後續」命中 1 → 約定③成立；約定①②沿用 2026-10-18 證據。
- 落差與下一步：判斷：符合 — 已驗收 3/3。限制：文字層驗收、執行端自評（同一模型），無獨立第二方。下一步：—
- 最近 align：2026-10-21 6c4972e5254a5a6c902012f8daa4b9a435a26938 範圍 PM0-A

### PM1-A
- 預期功能與規格：三模式可用（3 項）：base 產出新格式；update 保留 ID／證據／分母；align 回填同一份。
- 驗收方式與必要前提：base 為實際 Skill 執行並產出新格式文件；update 在真實修訂時執行並檢查 ID／證據／分母保留；align 為實際 Skill 執行並回填同一份文件。前提：update 項須存在真實修訂；證據須可複核（非手寫、非借用他議題）。
- 實際成果與證據：[已執行] 約定①base 產出新格式：`Skill(projman, base …)`（session e024aeab-3635-4b0f-afb9-36f87503110b 的 transcript 行 3276）→ `Write` 本文件（行 3292）→ 階段提交 825076db；V2／V4／V5／V6 對本文件輸出皆為空（13 ID、約定合計 35）。[未驗] 約定②update 保留 ID／證據／分母：無真實修訂，依規不為驗證製造修訂。[未驗] 約定③align 回填同一份：其證據只能來自本次 align 呼叫自身的產出，依 PJMAN-OUTCOMES-01 Hard Constraints 3 不計入本次。 [已執行] 2026-10-21 消費既有證據：約定②：真實 update（session `e024aeab-3635-4b0f-afb9-36f87503110b`，transcript 行 3807〔行號 1 起算；先前票檔以 0 起算記 3806，同一事件〕）→ 寫入提交 `51cc82ee`；`git diff --stat d720e995 51cc82ee` ＝ +2／−1，僅基準一行與「已確認變更」一列；`## 階段成果表`、`## 明細` 兩區段在 update 前後雜湊相同；V1～V6 輸出皆空（`plans/cso_briefing/project/deliver/archive/PJMAN/PJMAN-OUTCOMES-01_real-run.md` §12.5）；ID 13 個、`約定` 合計 35 不變；修訂內容為使用者對基準缺漏的澄清裁決（update 的合法輸入，非為驗證製造）→ 成立。約定③：align 真實執行（行 3397 → `d720e995`、行 1144〔session `4a678cbe-9cee-400a-8479-95de6747ac41`〕→ `e2f77049`）皆回填同一份文件，repo 內僅此一份 projman 結構標記文件、無平行副本 → 成立。約定①沿用。限制：update 僅一次、修訂未涉及分母（見 PM1-D）。
- 落差與下一步：判斷：符合 — 已驗收 3/3。缺口：無。限制：執行端自評；update 僅單次觀察。下一步：—
- 最近 align：2026-10-21 6c4972e5254a5a6c902012f8daa4b9a435a26938 範圍 PM1-A

### PM1-B
- 預期功能與規格：成果表呈現（4 項）：摘要置前固定短欄；已驗收與約定獨立數值欄；百分比衍生且未盤點≠0；明細於表後以 ID 對應。
- 驗收方式與必要前提：檢視本文件摘要表置前與固定欄位；以完整性核對（V2、V4、V5、V6）對文件執行。前提：無。
- 實際成果與證據：[已執行] ①摘要表（本文件第 16 行 `## 階段成果表` 起）位於 `## 明細` 之前，7 固定欄；②「已驗收」「約定」為獨立數值欄（V4 逐列讀 $(5)／$(6)，輸出為空）；③百分比衍生與未盤點≠0：base 狀態 13 列「已驗收」皆為 — 未寫 0（V4 空）；V4 對暫存變異副本——2/3 標 50% 輸出「達成度與計數不一致」，2/3 標 67% 與 0/3 標 0% 輸出為空（機制證據，副本已刪）；④V2 輸出為空（摘要 13 ID 與明細 13 節互為子集）。
- 落差與下一步：判斷：符合 — 已驗收 4/4。限制：本次回填之數值達成度屬本次呼叫產出，其 V4 結果由 PJMAN-OUTCOMES-01 AI-5 事後記錄，不計入本次證據。下一步：—
- 最近 align：2026-10-18 825076dbcf4ea4642ec0a7290005f88bc4316c65 範圍 PM1-B

### PM1-C
- 預期功能與規格：align 直接回報（2 項）：逐項結果與階段數量；主要缺口與下一步。
- 驗收方式與必要前提：實際執行 align，檢視對話正文回報是否含上述各項。前提：須有一次 align 實際執行。
- 實際成果與證據：[未驗] 約定①逐項結果與階段數量、②主要缺口與下一步：功能證據只能來自 align 自身的對話回報，依 PJMAN-OUTCOMES-01 Hard Constraints 3 不計入本次呼叫。[靜態] `.claude/skills/projman/SKILL.md` 197 行起〈直接回報〉規格存在；規格存在不等於功能證據。 [已執行] 2026-10-21 消費先前 align 的實際回報：2026-10-18 align 直接回報逐字存於 `plans/cso_briefing/project/deliver/archive/PJMAN/PJMAN-OUTCOMES-01_real-run.md` §6（約 357–401 行），含單項結果表（7 個 ID 的狀態、已驗收／約定、一行證據）、各階段計數（明列不給平均或總百分比）、主要缺口、下一步、待決策事項與五個摘要問題 → 約定①逐項結果與階段數量、②主要缺口與下一步皆成立。另 `e2f77049` 後對話回報（session `4a678cbe-9cee-400a-8479-95de6747ac41` 行 1221）同樣列 PM2 單項結果與計數、限制與留意事項。證據來自先前呼叫、由本次後續 align 消費，非自證。限制：行 1221 的回報未採五段格式；自評。
- 落差與下一步：判斷：符合 — 已驗收 2/2。缺口：無。下一步：—
- 最近 align：2026-10-21 6c4972e5254a5a6c902012f8daa4b9a435a26938 範圍 PM1-C

### PM1-D
- 預期功能與規格：update 完整性核對（3 項）：ID 集合保留；分母變動有確認列；表格格式核對。
- 驗收方式與必要前提：完整性核對指令（V1、V3、V6）對真實文件執行並檢視輸出。前提：V1、V3 需有寫前基準（已追蹤的版本或寫前摘要）。
- 實際成果與證據：[已執行] 約定③表格格式核對：V6 對本文件輸出為空（另 V2 輸出為空）。[未驗] 約定①ID 集合保留（V1）與②分母變動有確認列（V3）：兩者須對「align 後差分」比對，其結果在本次呼叫之後才存在，依 PJMAN-OUTCOMES-01 Hard Constraints 3 不計入本次；對未變動文件預跑只會得到 HEAD 與工作樹相同的空洞輸出，不採為證據。 [已執行] 2026-10-21 約定①：真實 update 後 V1 輸出為空，update 前後 ID 同為 13 個（`plans/cso_briefing/project/deliver/archive/PJMAN/PJMAN-OUTCOMES-01_real-run.md` §12.5 逐字輸出）→ 成立；約定③沿用（V6 空）。[未驗] 約定②分母變動有確認列：V3 在三次真實修訂（`d720e995`、`51cc82ee`、`e2f77049`）後輸出皆空，但三次皆未變動任何 `約定` 分母，V3 從未在「分母確實變動」情境產生差異並對應確認列；僅有 PJMAN-SKILL-01 的隔離示例推演（非真實 Skill 執行）。空輸出只證明沒有無聲變動，不證明偵測路徑有效。
- 落差與下一步：判斷：部分符合 — 已驗收 2/3。缺口：約定②缺真實的分母變動案例（功能已實作〔`SKILL.md` V3〕但未驗）。下一步：首個真實的分母變動 update 時補驗；不為驗證製造修訂。
- 最近 align：2026-10-21 6c4972e5254a5a6c902012f8daa4b9a435a26938 範圍 PM1-D

### PM1-E
- 預期功能與規格：相容與保留（2 項）：舊格式文件可 align 不強制遷移；`disable-model-invocation: false` 保留。
- 驗收方式與必要前提：對舊 5 欄格式文件實跑 align 並確認未被遷移；檢查 Skill frontmatter。前提：須有一份舊 5 欄格式文件可供實跑。
- 實際成果與證據：[已執行] 約定②`disable-model-invocation: false` 保留：`grep -n '^disable-model-invocation:' .claude/skills/projman/SKILL.md` → 第 5 行 `false`；blob a1c6440b，最後觸及該檔之 commit 86521a8f。[未驗] 約定①舊 5 欄格式文件 align 不強制遷移：本議題未對舊格式文件實跑，且不借用他議題成果文件的既有執行紀錄。 [已執行] 2026-10-21 約定②複驗：`grep -n '^disable-model-invocation:' .claude/skills/projman/SKILL.md` ＝ 第 5 行 `false`，最後觸及該檔 commit `86521a8f`。[未驗] 約定①舊 5 欄文件 align 不強制遷移：相容回填已實作（`SKILL.md` 〈回填邊界〉舊 5 欄段）但本議題未對舊格式文件實跑；他議題文件（`plans/roadmap/autonomous-engineering-gate-outcomes.md`）已有一筆 align 提交 `a55ee9e3`，依先前「不借用他議題紀錄」規則未計入。 [已執行] 2026-10-22 align（Akira 2026-10-22 裁決指令）：約定②複驗：`grep -n '^disable-model-invocation:' .claude/skills/projman/SKILL.md` ＝ 第 5 行 `false`；最後觸及該檔 commit `3e92898f`（PJMAN-CLOSELOOP-01，frontmatter 未動）。約定①→採納既有相容證據 `a55ee9e3`（2026-10-21，[GOVERNANCE-SYNC]，他議題文件 `plans/roadmap/autonomous-engineering-gate-outcomes.md`，舊 5 欄格式，條款觸發之 align）：提交前後表頭同為 `階段／成果 ID｜預期功能與規格｜驗收方式與必要前提｜實際成果與證據｜落差與下一步`（`git show a55ee9e3^:` 與 `a55ee9e3:` 皆第 32 行），提交後該檔 `已驗收` 字串 0 次（未被遷移為新格式），diff 為 4+／4− 且僅證據與摘要文字更新 → 舊 5 欄文件經 align「不強制遷移」成立。**限定**：此證據只證明「舊格式文件 align 相容、不遷移」；**不證**該文件之定位正確——該文件已於 2026-10-21 被使用者確認過期且錯誤、列入作廢清單（`projman/SKILL.md` 〈成果文件定位規則〉第 2 項）；操作對象屬他議題文件，先前「不借用他議題紀錄」之保守處置由 Owner 2026-10-22 裁決覆寫為採納相容證據。
- 落差與下一步：判斷：符合 — 已驗收 2/2。限制：約定①證據來自他議題舊格式文件與一次條款觸發之 align、自評，不證定位正確；本議題文件為新格式，未對本議題文件做舊格式實跑。下一步：—
- 最近 align：2026-10-22 6ea5e2616bc1f84a80507a82f7a77174a32a9a55 範圍 PM1-E

### PM2-A
- 預期功能與規格：治理授權與引用（2 項）：條款位於唯一 GO lane，以狹義授權句授予已唯一定位成果文件的讀寫及單檔保存，觸發事實與 QA／SV 是否省略無關；consumer 只引用不複製。
- 驗收方式與必要前提：檢視治理條款與 consumer 引用的實際文字。前提：治理條款呼叫票 `PJMAN-AUTOALIGN-01` 已於 2026-10-20 交付並 CLOSED（2026-10-22 update 澄清過期前提；前提已滿足）。
- 實際成果與證據：[已執行] 2026-10-20 檢視治理條款與 consumer 引用實際文字（驗收方式＝檢視文字）：①`.claude/rules/ssot-governance.md:159` 新列位於 Ticket Runtime Transition Table（唯一 GO lane，`:124`），含狹義授權句逐字「本票正常收束後……不修改其他檔案、產品需求、Registry 或 lifecycle」（該句於 `.claude/**/*.md` 僅此 1 處）、左欄含「觸發與 `/qa`、`/sv` 是否依風險分級省略無關」、控制詞僅 `MUST AUTO-CHAIN`（python 逐項字串檢查皆 True）→ 約定①成立；②`.claude/skills/sv/SKILL.md:213` 為唯一引用（`projman` 命中行＝[213]），`uc`／`qa`／`compact` 的 SKILL.md 命中 0，governance／sv 無 `grep -rl` 定位指令或 `^## 階段成果表` 字面（無 predicate／定位邏輯副本）→ 約定②成立。[文件] 交付提交 `93dd64f4`（範圍 499d583b..93dd64f4）；票檔 `plans/cso_briefing/project/deliver/PJMAN-AUTOALIGN-01_go-lane.md` §5（QA／SV PASS、R0 FORWARD、Codex 首次 REPORT_FIX 後 ADVISORY，receipt `TS=20261020_155026`）。限制：文字層驗收；條款是否被模型實際遵循屬 PM2-E；Codex ADVISORY 指出 `ssot-governance.md:159` 四態回報與 `projman/SKILL.md:250` 三態字面分歧，屬 PM2-D 與 Skill 後續修補，非 PM2-A 約定。
- 落差與下一步：判斷：符合 — 已驗收 2/2（約定①②）。缺口：無（文字層）；限制見證據欄。下一步：—
- 最近 align：2026-10-20 9315b48607927a085cdf076938ec9b3272a07040 範圍 PM2-A

### PM2-B
- 預期功能與規格：觸發與歸屬判定（4 項）：①未盤點新交付且階段已可驗收才執行，未到點／無新成果／無對應基準／歸屬不明不猜測；②候選集合引用 Skill 定位規則（排除歸檔）、歸屬依任務已知文件／議題識別／實際成果，票前綴僅輔助，缺前綴舊文件不整類排除；③每階段最近 align 與實際校正範圍對應，校正某階段不消費他階段未檢查交付，從未 align 階段以文件首次提交為預設錨點；④git 前綴與目錄排除僅輔助，不整類排除 docs-only 或規則型成果。
- 驗收方式與必要前提：以隔離案例或真實觸發逐項檢視。前提：治理條款呼叫票 `PJMAN-AUTOALIGN-01` 已於 2026-10-20 交付並 CLOSED（2026-10-22 update 澄清過期前提；前提已滿足）。
- 實際成果與證據：[已執行] 約定③：首次提交錨點指令 `git log --diff-filter=A --follow --format=%H -- plans/roadmap/projman-outcomes.md | tail -1` ＝ `825076dbcf4ea4642ec0a7290005f88bc4316c65`（PM2-A～E 無列內 align 記錄，以此為預設錨點）；先前真實 align 提交 `d720e995`（僅檢查 PM0-A PM1-A～E PM3-A）對本文件的 diff 中 PM2 列零變更（`git show d720e995 -- 本文件 | grep PM2-` 無 +/- 行），印證校正某階段不消費他階段 → 成立。[已執行] 約定④：`git log --oneline 825076db..HEAD` 列示之候選交付涵蓋 docs／治理規則型提交（如 `93dd64f4`、`d720e995`、`8d14db59`），未因前綴或目錄被排除 → 成立。[未驗] 約定①（未到點／無新成果／無對應基準／歸屬不明不猜測）與約定②（候選集合引用 Skill 定位規則、三訊號歸屬、票前綴僅輔助、缺前綴舊文件不整類排除）：僅有 `[靜態]` 條文（`ssot-governance.md:159` 左欄與 MUST NOT）與 `[stub-equivalent]` 隔離推演（票檔 §4.2 C4–C7、C13，條款判讀由同一模型依條文推演、無工具輸出，非真實觸發）；本次觸發的歸屬判讀（`grep -rl '^## 階段成果表' plans/ --exclude-dir=archive` 得兩份文件，依三訊號取 projman-outcomes.md）為同一模型自評、屬本次呼叫自身行為，待審查確認，不計入。 [已執行] 2026-10-21 約定②部分證據：真實觸發 `e2f77049` 時，`grep -rl '^## 階段成果表' plans/ --exclude-dir=archive` 得兩份候選（`autonomous-engineering-gate-outcomes.md` 與本文件），依三訊號唯一歸屬本文件（`PJMAN-AUTOALIGN-01` 收尾實錄 §10）。[未驗] 約定②其餘：本例票前綴恰與文件相符，「前綴僅輔助」「缺前綴舊文件不整類排除」未被真實案例檢驗（舊文件被排除是因三訊號皆不成立）；約定①拒絕分支（未到點／無新成果／無對應基準／歸屬不明不猜測）僅有 stub 層推演（同票 §4.2 C4–C7、C13），無真實案例。約定③④沿用 2026-10-20 證據。
- 落差與下一步：判斷：部分符合 — 已驗收 2/4。缺口：約定①②已實作未驗（正向路徑有一次真實觀察，拒絕與前綴／缺前綴情境無）。下一步：待後續真實收束累積案例，不為驗證製造。
- 最近 align：2026-10-21 6c4972e5254a5a6c902012f8daa4b9a435a26938 範圍 PM2-B

### PM2-C
- 預期功能與規格：執行與回報（2 項）：顯示具體 align 提示詞並實際執行；回填後直接報告。
- 驗收方式與必要前提：檢視一次被條款觸發的 align 之提示詞、實際呼叫與對話回報。前提：治理條款呼叫票 `PJMAN-AUTOALIGN-01` 已於 2026-10-20 交付並 CLOSED（2026-10-22 update 澄清過期前提；前提已滿足）。
- 實際成果與證據：[未驗] 約定①顯示具體 align 提示詞並實際執行、約定②回填後直接報告：功能證據只能來自本次被條款觸發的 align 呼叫與其對話回報，即本次呼叫自身，依 PJMAN-OUTCOMES-01 Hard Constraints 3 先例不計入本次（呼叫紀錄見 PM2-E）。[靜態] 條文：`ssot-governance.md:159` 右欄「顯示具體 align 提示詞、實際執行」；`projman/SKILL.md:197` 起〈直接回報〉。規格存在不等於功能證據。 [已執行] 2026-10-21 約定①：session `4a678cbe-9cee-400a-8479-95de6747ac41` 行 1144 `Skill(projman)` 實際呼叫，args 為具體 align 提示詞（`align plans/roadmap/projman-outcomes.md PM2-A … PM2-E` 加來源與限制註記）；審查方 SKAD 2026-10-20 於 PJMAN-AUTOALIGN-01 結案審查確認本次為自動發起（registry checkpoint `reviewer_disposition`）。提示詞經工具呼叫參數呈現，未另以對話正文單獨顯示。約定②：回填後對話直接回報存在（行 1221，2026-10-20T07:58:50Z；列 PM2-A～E 單項結果與計數、限制與請留意事項），保存提交 `e2f77049` 僅涉本文件。結案審查未逐項確認報告內容；報告存在性由本次讀取 transcript 核對。證據來自先前呼叫、由本次後續 align 消費，非自證。
- 落差與下一步：判斷：符合 — 已驗收 2/2。缺口：無。限制：單次觀察、執行端自評；行 1221 回報未採五段格式。下一步：—
- 最近 align：2026-10-21 6c4972e5254a5a6c902012f8daa4b9a435a26938 範圍 PM2-C

### PM2-D
- 預期功能與規格：邊界與失敗隔離（6 項）：①寫入授權與提交保存資格分離（align 自身以 `git commit -o <成果文件>` 單檔保存、軌道 A 先完成當次收尾提交、軌道 B 不為 align 提前 `/compact`）；②不繞過 FORBIDDEN（僅列檔名者預設票內排除，其他明確禁止與權限拒絕仍遵守）、不夾帶他 session 修改；③不遞迴；④align 失敗不回頭視為原票或下一張票未收束，失敗報告依實際進度擇一「未完成呼叫」／「回填未完成」／「已回填未提交」（另「已提交」與「無差分」為非失敗結果）並附 dirty 路徑，且如實列出已知限制；⑤只消費既有證據、不新增付費呼叫；⑥漏跑僅稱可能於後續相關收束補判。
- 驗收方式與必要前提：以隔離案例逐項檢視，不得以文件自述代替。前提：治理條款呼叫票 `PJMAN-AUTOALIGN-01` 已於 2026-10-20 交付並 CLOSED（2026-10-22 update 澄清過期前提；前提已滿足）。
- 實際成果與證據：[已執行]（暫存 repo 真實 git 輸出；僅 stub 層，非真實觸發）約定①：票檔 §4.1 C1 輸出 `git commit -o plans/roadmap/demo-outcomes.md` 後 `files=[plans/roadmap/demo-outcomes.md]`，他檔 staged 仍為 staged、未追蹤檔仍未追蹤；軌道 A 先於 align：本次觸發前已有 `[PJMAN-AUTOALIGN-01]` 收尾提交 `9315b486`（`git log`），軌道 B 未提前 `/compact`；條文 `ssot-governance.md:159` → 成立。約定④：C9 輸出 pre-commit exit 1 後文件仍 ` M`（可回報 dirty 路徑）、模擬原票狀態 hash 未變（`yes`）、無重試 → 成立。[未驗] 約定②（不繞過 FORBIDDEN／不夾帶他 session 修改：C1／C2 僅證 `-o` 不夾帶與 dirty 略過的機械面，FORBIDDEN 解讀屬模型判讀）、約定③不遞迴（僅條文與 `projman/SKILL.md:253`，真實行為待觀察）、約定⑤只消費既有證據不新增付費呼叫（僅條文；實際為本次呼叫自身）、約定⑥漏跑僅稱可能補判（僅用語檢查；驗收方式要求隔離案例逐項檢視、不得以文件自述代替）。Codex ADVISORY：`ssot-governance.md:159` 四態與 `projman/SKILL.md:250` 三態字面分歧，未證實造成行為影響。 [已執行] 2026-10-21 消費真實觸發 `e2f77049` 的既有證據：約定①：`git show --stat e2f77049` 僅涉本文件（21+／21−）；順序為軌道 A 收尾提交 `9315b486` → 行 1144 align 呼叫 → 單檔 `git commit -o` 提交；該 session 無 `compact` Skill 呼叫（軌道 B 未提前）；約定③：e2f77049 之後至本次使用者直接呼叫（行 1618）間，session `4a678cbe-9cee-400a-8479-95de6747ac41` 的 `Skill(projman)` 呼叫為 0 次，無遞迴；約定⑤：該次 align 工具序列（行 1158–1202）僅唯讀 git／grep／transcript 讀取、單檔 python 寫入與 git commit，無建置、測試、研究、顧問或付費呼叫；約定⑥：`ssot-governance.md:159` 與 `projman/SKILL.md:254` 皆用「可能於後續相關收束時補判」，新列 `保證`／`自癒`／`必定補判` 命中 0，執行端回報（行 1221）稱自動鏈為模型契約、無機械後盾。約定④沿用 2026-10-20 stub 層證據（暫存 repo 提交失敗案；真實失敗路徑未發生）。[未驗] 約定②：不繞過 FORBIDDEN 僅「票內排除」半項有真實觀察（該票 FORBIDDEN 僅列本檔名且 align 照常執行），「其他明確禁止與權限拒絕仍遵守」與「不夾帶他 session 修改」（當時無他人 dirty 檔）無真實案例。 [已執行] 2026-10-22 align（Akira 2026-10-22 裁決指令啟動，手動、非條款自動觸發；只消費既有證據）逐項重判，不預填計數：約定⑥→[未驗]：驗收方式要求「隔離案例逐項檢視，不得以文件自述代替」，先前僅以 `ssot-governance.md`／Skill 用語檢查（`保證`／`自癒`／`必定補判` 命中 0）與執行端回報用語作證，屬文件自述層級、無隔離案例；依 Owner 裁決維持未驗，已驗收由 5 降為 4（對 2026-10-21 計入⑥之較寬判讀的更正，非功能退化）。約定①→成立：沿用 `e2f77049` 真實觸發（已追蹤乾淨單檔 `git commit -o`、軌道 A 先行、軌道 B 未提前；覆蓋範圍僅此一種情境）；另 PJMAN-CLOSELOOP-01 之隔離 repo 真實 git 輸出（`tmp/PJMAN-CLOSELOOP-01/ai8_out.txt`，非版控）(a) 未追蹤新檔＋他檔 staged 時單檔提交不帶他檔、(e) 已知來源 dirty 單一提交，屬 stub-equivalent 補充，不計為真實觸發；base 新檔與失敗路徑無真實案例。約定③→成立：沿用 `e2f77049` 後至使用者直接呼叫間 `Skill(projman)` 0 次之一次觀察（規則未變：被條款呼叫節第 8 項）；新增的「最終覆蓋確認」路徑尚無遞迴觀察，屬 PM5-C。約定⑤→成立：沿用該次 align 工具序列僅唯讀 git／grep／transcript 讀取、單檔寫入與提交之觀察（被條款呼叫節第 2 項未變）；本次 2026-10-22 之 update／align 為使用者指令、非條款觸發，不計入。約定④→維持計入並標註：規格字面已於 2026-10-22 update 對齊四態（未完成呼叫／回填未完成／已回填未提交；「已提交」「無差分」為非失敗結果）；舊證據（C9 stub：pre-commit exit 1 後文件仍 ` M`、可回報 dirty 路徑、原票狀態 hash 未變、無重試）驗的是失敗路徑形狀，與標籤字面無涉，標籤字面為 [靜態]（`projman/SKILL.md` 〈寫入與保存〉第 5 項；舊三態字面於 `.claude/rules`、`.claude/skills` 全部 `.md` 命中 0）；真實失敗路徑未發生，仍僅 stub 層。約定②→維持 [未驗]（無新證據）。分層：PM5-B 之新路徑證據不計入本項（不互相升格）。
- 落差與下一步：判斷：部分符合 — 已驗收 4/6。缺口：約定②已實作未驗（「其他明確禁止與權限拒絕仍遵守」「不夾帶他 session 修改」無真實案例）；約定⑥無隔離案例，維持未驗。限制：①③④⑤皆為單次觀察或 stub 層、自評；①未涵蓋 base 新檔與失敗路徑的真實觸發。下一步：後續真實案例累積②；⑥待隔離案例；不為驗證製造。
- 最近 align：2026-10-22 6ea5e2616bc1f84a80507a82f7a77174a32a9a55 範圍 PM2-D

### PM2-E
- 預期功能與規格：真實觸發（1 項）：一次真實收束後自動 align 實際執行並報告；預設未驗，須有 session id 與依序工具呼叫證據證明同回合、收束寫入後、無新使用者訊息即呼叫 Skill，由治理條款呼叫票的 REVIEW→CLOSED 審查確認；手動、自填、僅有提交、無法核對或未觸發皆維持未驗。
- 驗收方式與必要前提：核對該次觸發的 session id 與依序工具呼叫。前提：一次符合條件的真實收束觸發；治理條款呼叫票 `PJMAN-AUTOALIGN-01` 已於 2026-10-20 交付並 CLOSED（2026-10-22 update 澄清過期前提；前提已滿足）。
- 實際成果與證據：[未驗] 約定①一次真實收束後自動 align 實際執行並報告：本次為首個**候選**觸發，以下為**自評，待審查確認**，不升級已驗收。①呼叫來源：PJMAN-AUTOALIGN-01 Agent closeout 本次呼叫實際完成後（`registry_set_status` REVIEW、`registry_set_checkpoint` 已 read-back、軌道 A 收尾提交 `9315b486`），依 `ssot-governance.md:159` 新列觸發；②Skill 實際呼叫：`Skill(projman)`，args 以 `align plans/roadmap/projman-outcomes.md PM2-A PM2-B PM2-C PM2-D PM2-E` 開頭；③session id：`4a678cbe-9cee-400a-8479-95de6747ac41`；④依序工具呼叫（transcript `~/.claude/projects/D--DevSources-AndroidProj-SmartCameraApp1/4a678cbe-9cee-400a-8479-95de6747ac41.jsonl`，行號 1 起算，UTC）：1092 `registry_set_status`（REVIEW，07:53:42）→ 1097 `registry_set_checkpoint`（summary 超過 200 字元被拒）→ 1111 `registry_set_checkpoint`（重寫成功）→ 1099／1113 `registry_list`（read-back）→ 1119 `Bash`（git status）→ 1125 `Bash`（軌道 A：git add／commit `9315b486`）→ 1137 `Bash`（候選／歸屬／錨點判讀）→ 1144 `Skill`（projman align，07:55:04）；⑤判別依據：1092 之後至 1144 間 jsonl 無 role=user 的使用者文字訊息（僅 1145 `tool_result` 與 1146 skill 載入注入），tool_result 同秒返回；⑥回填差分與保存結果：以本次提交為準，commit hash 與 `git diff --stat` 於對話回報（本文件不寫入自身提交 hash）。不能升級的原因：同一模型自評、無獨立第二方；`Skill(projman)` 不在 `permissions.allow`，是否出現權限提示無法由 transcript 判定（jsonl 無使用者介入痕跡）；模型是否依文字鏈遵循屬首次觀察。 [已執行] 2026-10-21 審查方確認：PJMAN-AUTOALIGN-01 結案審查（registry checkpoint `reviewer_disposition`，SKAD 2026-10-20，Akira 明示授權）判定本次為自動發起：transcript 行 1092–1197 的 user 事件 promptId 相同＝同一回合，起點為 `registry_set_status(REVIEW)` 的 tool_result，`Skill(projman)`（行 1144）在其後且在軌道 A 收尾提交 `9315b486` 之後，其餘為 tool_result，唯一文字事件為 Skill 載入注入文字＝無新使用者訊息。審查方自述未比對原始 transcript（摘錄為執行端產出）；本次已獨立重讀原始 transcript（session `4a678cbe-9cee-400a-8479-95de6747ac41` 行 1092–1197）：user 事件 15 筆全屬同一 promptId，其中 14 筆 tool_result、1 筆 Skill 載入文字（行 1146）；審查方記 14 筆，差 1 筆 tool_result，來源未釐清，不影響結論。權限介入：transcript 無權限事件類型，無法判定（`Skill(projman)` 不在 `permissions.allow`），不宣稱無人值守。session id 與依序工具呼叫見 2026-10-20 自評明細（PJMAN-AUTOALIGN-01 收尾實錄 §10、§11）。
- 落差與下一步：判斷：符合 — 已驗收 1/1。限制：僅一次觀察，不推及所有場景；權限介入未判定；自動鏈為模型契約、無機械後盾。下一步：後續真實收束累積觀察。
- 最近 align：2026-10-21 6c4972e5254a5a6c902012f8daa4b9a435a26938 範圍 PM2-E

### PM3-A
- 預期功能與規格：三模式實跑（3 項）：base 真實執行；align 真實執行；update 於首個真實修訂時真實執行（證據可複核，非手寫、非借用他議題；無真實修訂時此項維持未驗，PM3 不判完整）。
- 驗收方式與必要前提：以 Skill 實際呼叫紀錄（工具序列）與 git 提交／差分核對三項。前提：update 項須存在真實修訂。
- 實際成果與證據：[已執行] 約定①base 真實執行：`Skill(projman, base …)`（transcript 行 3276）→ `Write` 本文件（行 3292）→ 階段提交 825076db（`git show --name-only` 僅此檔）；非手寫、非借用他議題。[未驗] 約定②align 真實執行：其證據為本次呼叫自身，依 PJMAN-OUTCOMES-01 Hard Constraints 3 不計入本次。[未驗] 約定③update 於首個真實修訂時真實執行：目前無真實修訂，不為驗證製造。 [已執行] 2026-10-21 消費既有證據：約定②align 真實執行：行 3397（2026-10-18T11:41:51Z）→ 提交 `d720e995`；行 1144（session `4a678cbe-9cee-400a-8479-95de6747ac41`，2026-10-20）→ 提交 `e2f77049`。約定③update 於首個真實修訂時真實執行：行 3807（2026-10-18T12:40:30Z）→ 兩次 Edit → 提交 `51cc82ee`；修訂為使用者對基準缺漏的澄清裁決（update 的合法輸入，非為驗證製造）。約定①沿用（base：行 3277 → `825076db`）。行號 1 起算（先前票檔以 0 起算記 3276／3396／3806，同一事件）。證據可由 transcript 與 git 複核，非手寫、非借用他議題。
- 落差與下一步：判斷：符合 — 已驗收 3/3。限制：update 僅一次、修訂未涉分母；執行端自評（同一模型）。下一步：—
- 最近 align：2026-10-21 6c4972e5254a5a6c902012f8daa4b9a435a26938 範圍 PM3-A

### PM4-A
- 預期功能與規格：後續獨立交付（不在本期範圍；本期僅格式可銜接）。無約定項。
- 驗收方式與必要前提：不適用（無約定項）。
- 實際成果與證據：未開始
- 落差與下一步：—
- 最近 align：無

### PM5-A
- 預期功能與規格：唯一寫入入口（3 項）：①`.claude/CLAUDE.md` Rule 4 f) 與 Skills 行指向 `/projman`，措辭為「不得繞過 Projman 直接寫入」並明許實際 Projman 呼叫內依模式與授權使用既有編輯工具，細則僅在 Projman Skill；②`base` 對既有檔的修訂規則轉向 `update`／`align`，手動 `update` dirty bullet 改為共同規則引用且無互斥授權句；③真實觀察：後續對成果文件的建立或內容異動確實僅經 Projman 對應模式。
- 驗收方式與必要前提：①②以靜態檢視實際文字（grep 與 diff）；③以真實使用觀察。證據分層（靜態／隔離案例／文字推演／具名豁免／真實觀察）分別列示，不互相升格。前提：③須有後續對成果文件的真實異動案例；相關交付 `PJMAN-CLOSELOOP-01`（2026-10-22 CLOSED，實作 `3e92898f`）。
- 實際成果與證據：[靜態] 2026-10-22 @HEAD `6ea5e261`：約定①：`.claude/CLAUDE.md` `^f\) ` 恰 1（Rule 4 f)），含「不得繞過 Projman 直接寫入」「`/projman`」與 `.claude/skills/projman/SKILL.md` 路徑；Skills 行含 `/projman` 專案成果對齊；細則僅在 `projman/SKILL.md` 〈寫入與保存〉（`^## 寫入與保存` 1）。約定②：`projman/SKILL.md` 〈base〉重新執行規則轉向 `update`／`align`，手動 `update` dirty bullet 改為〈寫入與保存〉第 1 項引用；舊句「授權依據是使用者對該指定文件的修訂授權」「不得無聲覆蓋原目標與核心能力」全倉命中 0（PJMAN-CLOSELOOP-01 `evidence.txt` AC-2，提交 `3e92898f`）。[文件] PJMAN-CLOSELOOP-01 AC-1／AC-2 PASS（close reviewer Sonnet，2026-10-22）。[隔離案例] 無。[具名豁免] 不適用（`AC6-AC7-SCOPED-ACCEPTANCE` 僅涉 AC-6／AC-7）。[未驗] 約定③真實觀察：本次 update／align 由使用者指令啟動並經 Projman 寫入，為單次觀察，不構成「後續異動確實僅經 Projman」的案例（無直接寫入被檢驗或被攔）；不推定 Rule 4 f) 會被遵循（模型契約、無機械後盾，自評）。 [靜態] 2026-10-22 重核 @HEAD `800c5394`（自上次 align 錨點 `6ea5e261` 起，`projman/SKILL.md`、`CLAUDE.md`、`ticket_pattern.md` 無改動；TKA-WIRE-01 只改 `ssot-governance.md` 與 `sv/SKILL.md`）：`^f\) ` 恰 1，「不得繞過 Projman 直接寫入」於 `CLAUDE.md` 與 `projman/SKILL.md` 各 1，Skills 行含 `/projman`，結論不變。[未驗] 約定③仍無後續真實案例；本次為使用者指令之手動 align，不構成觀察。
- 落差與下一步：判斷：部分符合 — 已驗收 2/3（約定①②靜態成立）。缺口：約定③真實觀察未驗。限制：模型契約、無機械後盾；自評。下一步：待後續真實案例累積，不為驗證製造。
- 最近 align：2026-10-22 800c53948dd277d9c26759ff6080a3f451a5115b 範圍 PM5-A

### PM5-B
- 預期功能與規格：共同自主保存（5 項）：①寫前檢查以目前內容為基準；來源證明須「實際呼叫紀錄＋寫入紀錄＋內容識別」聯合（blob 相等僅輔助）；staged 之 index 版與工作樹版各自判定；來源不明或並行則略過（含手動 `update`）；②核對依模式與格式適用（舊 5 欄僅 V1／V6；`update` 的 V3 差異有同次「已確認變更」列為合法；`align` 的 V3 為空；`base` 新建檔無 V1／V3），已知來源 dirty 時 V1／V3 基準為寫前目前內容；③保存序列：未追蹤且未被 ignore 者先 `git add -- <doc>`，再 `git commit -o -m "[PROJMAN-SYNC] <摘要>" -- <doc>`，`base` 新檔可自主保存（必要能力）、已被拒絕的提交不自動重試、無差分不提交；④回報：四態（未完成呼叫／回填未完成／已回填未提交／已提交）與「無差分」僅單點定義於 Skill，治理列與 `sv` 僅引用，舊三態字面消除，每次寫入後報告記錄寫後 blob id；⑤真實接續：先前 Projman 報告所記 blob 與拒絕記載等 producer 實際存在，並有一次真實的自主保存案例。
- 驗收方式與必要前提：①～④以靜態檢視、隔離 repo 真實 git 輸出（模擬報告輸入，`[stub-equivalent]`）與文字推演；⑤以真實觀察。證據分層分別列示，不互相升格；具名豁免只豁免特定阻擋效果，不升級任何真實接續項。前提：⑤須有首次經新路徑產生的真實 Projman 報告與其後續呼叫。
- 實際成果與證據：[靜態] 2026-10-22 @HEAD `6ea5e261`：約定①～④：`projman/SKILL.md` 〈寫入與保存〉第 1–6 項存在；舊三態字面全倉 `.md` 命中 0；四態與「無差分」僅單點定義於第 5 項，`ssot-governance.md` L159 列與 `sv/SKILL.md` Step 11 僅引用（PJMAN-CLOSELOOP-01 AC-2／AC-3／AC-4／AC-9 PASS，close reviewer Sonnet 2026-10-22）。[隔離案例，stub-equivalent]（隔離 repo 真實 git 輸出；輸入為腳本變數模擬之報告 blob 與呼叫／寫入紀錄，**非真實 Projman 報告**；`tmp/PJMAN-CLOSELOOP-01/ai8_out.txt`，非版控、日後可能被清理）：(a) 未追蹤新檔＋他檔 staged：`add`、`commit -o` rc=0，提交僅含該檔；(b) base add 成功、git hook 令 commit rc=1，二版各自 PROVEN，恢復提交 rc=0、HEAD blob＝工作樹 blob；(c) staged 含額外不明內容／工作樹回到 HEAD 內容→UNKNOWN 略過；(d) blob 相等但缺呼叫或缺寫入紀錄→UNKNOWN，紀錄齊全且 blob 等→PROVEN；(e) 已追蹤 dirty 單一 commit；(f) 被 ignore 檔 `add` rc=1、未 add 檔 `commit -o` rc=1；(g) 無差分不提交。[文字推演] 票檔 AC-7 C11–C13（手動 update 來源不明略過、被拒絕提交不自動重試、V1／V3 基準）。[具名豁免] `AC6-AC7-SCOPED-ACCEPTANCE`（Akira 2026-10-21）：AC-6／AC-7 於票面明載範圍內成立，僅豁免「producer (C)→不得標完成」之阻擋效果，不提升為真實行為 PASS。[已執行，真實觀察，約定② 局部] 2026-10-22 `update` 對本文件：V1–V6 輸出，V3 差異僅 PM5-A..D 且有同次「已確認變更」列（合法差異、未被當作失敗），寫後 `git commit -o` 單檔提交 `6ea5e261`（寫前乾淨、已追蹤、44+／9−、他檔未帶入），寫後 blob `d65e10cb12a095b7a8d9829d7582c5fd0fbcb33b`；使用者指令啟動之單次觀察，非條款自動觸發。[未驗] 約定⑤真實接續（missing-wiring，Producer (C)）：source-proof（後續呼叫以先前 Projman 報告所記 blob 證明 dirty 來源）、rejected-commit-retry（先前報告載明提交被拒）、final-coverage、residual-save-continuation 四個 guard 之必要輸入路徑**未被實際走過**——本次 update 雖已於報告記寫後 blob，但 dirty 情境之後續消費未發生；無「已被拒絕的提交」真實案例；無 base 新檔真實自主保存案例；無自動化 integration test；`Skill(projman)` 未列 `permissions.allow`（是否出現權限提示無法判定）；跨 session transcript 可取得性未驗；`m3_family_audit.sh` 對 `[PROJMAN-SYNC]` 之處置未驗。以上皆未豁免、未提升。 [靜態] 2026-10-22 重核 @HEAD `800c5394`（自上次 align 錨點 `6ea5e261` 起，`projman/SKILL.md`、`CLAUDE.md`、`ticket_pattern.md` 無改動；TKA-WIRE-01 只改 `ssot-governance.md` 與 `sv/SKILL.md`）：舊三態字面全倉 `.md` 命中 0；`ssot-governance.md` projman 列（`^\| Agent closeout 於本次呼叫實際完成（`）仍不含 `git commit -o`／`git status --short`／`hash-object`／`git add`／`GOVERNANCE-SYNC`／`→`，並引用〈寫入與保存〉；`sv` Step 11 仍僅引用；TKA-WIRE-01 其間只改 close 列、Step 11 引言與第 4／6 項，PM5-B 所引字面未變，約定①～④結論不變。[已執行，真實觀察，限制面] TKA-WIRE-01 票檔（`plans/cso_briefing/project/deliver/TKA-WIRE-01_dual-path.md` 收尾實錄）記載：其 `/qa` Step 0 時 `DIFF_REF` 因他票 `[PROJMAN-SYNC]` 提交 `face1158`（本文件的 align 保存）改為 `face1158`，並同步 conf、per-ticket 快照與票面——即本項已揭露之「Projman 提交落入他票 DIFF 範圍」窗口實際發生一次；票檔記載之處置為前移 `DIFF_REF`，未逐項核對該票 `/qa` 輸出。此為已知限制面的單次觀察，**不是**約定⑤所需的 producer，不改計數、不升級任何 (C) 項。[文件] 同票另載實際 Skill 調用未被阻擋（使用者層級 `defaultMode=auto`、repo allow 僅 `Skill(qa)`），屬 `Skill(ticket-author)` 觀察，不證 `Skill(projman)` 權限行為，U2 維持未驗。
- 落差與下一步：判斷：部分符合 — 已驗收 4/5（約定①～④於靜態／隔離案例／文字推演層次成立，且為具名豁免範圍內成立）。缺口：約定⑤真實接續未驗；四個 guard 的 missing-wiring 未豁免、未提升。限制：①～④之來源證明輸入為模擬；模型契約、無機械後盾；自評。下一步：首次 dirty 情境後續呼叫以報告 blob 證明來源、首次被拒提交案例、首次 base 新檔自主保存，各待真實案例；不為驗證製造。
- 最近 align：2026-10-22 800c53948dd277d9c26759ff6080a3f451a5115b 範圍 PM5-B

### PM5-C
- 預期功能與規格：最終覆蓋確認（3 項）：①Transition Table 新列＋Skill〈最終覆蓋確認〉＋`sv` Step 11 第 5 項引用，判準單點在 Skill、控制詞僅封閉四值；②判斷分支：非最後一張票不做、有新證據才 align／無新證據回報「已涵蓋、無需重跑」、Registry 無 OPEN 票不構成完成、缺口只揭露不開票、重入時「最終交付總結前」獨立觸發僅讀票務事實、不遞迴；③真實觸發：本輪最後一張票 close 後或最終交付總結前，確實執行一次覆蓋確認並依結果回報。
- 驗收方式與必要前提：①靜態檢視；②文字推演逐步附 `檔案:行號`；③真實觀察。證據分層分別列示，不互相升格。前提：③須有一次符合條件的真實「最後一張票」收束或最終交付；「最後一張」為綜合判斷、無機械 producer。
- 實際成果與證據：[靜態] 2026-10-22 @HEAD `6ea5e261`：約定①：`ssot-governance.md` 新列（`^\| 本輪最後一張票的` 恰 1，位於 close 列與已收束重入列之間，控制詞僅 `MUST AUTO-CHAIN`）；`projman/SKILL.md` `### 最終覆蓋確認`（恰 1）；`sv/SKILL.md` Step 11 第 5 項（恰 1，僅引用）；判準單點在 Skill（PJMAN-CLOSELOOP-01 AC-2／AC-3／AC-4 PASS，close reviewer 2026-10-22）。[文字推演] 約定②：票檔 AC-7 C1–C7（含 C5 重入、C6 不遞迴、C7 缺口只揭露）逐步附 `檔案:行號`。[具名豁免] `AC6-AC7-SCOPED-ACCEPTANCE`：AC-7 範圍內成立，不提升；C2–C7 的「最後一張票」判斷輸入無機械 producer（Producer (C)／missing-wiring，未豁免）。[隔離案例] 無。[未驗] 約定③真實觸發：本輪最後一張票 close 後之覆蓋確認**未被自動觸發**——2026-10-22 PJMAN-CLOSELOOP-01 closeout 時 Agent 判定不觸發 align（成果文件須先 update）；其後的 update 與本次 align 由使用者指令啟動，**不得回填為前次 close 後已自主觸發**，亦不計入本項。 [靜態] 2026-10-22 重核 @HEAD `800c5394`（自上次 align 錨點 `6ea5e261` 起，`projman/SKILL.md`、`CLAUDE.md`、`ticket_pattern.md` 無改動；TKA-WIRE-01 只改 `ssot-governance.md` 與 `sv/SKILL.md`）：`ssot-governance.md` 新列仍恰 1，位於 close 列（L160）與已收束重入列（L162）之間（L161），`→`／`保證`／`自癒`／`必定補判` 命中 0，轉移表內 `| ` 列數 18；`sv` Step 11 第 5 項仍恰 1（L215）；TKA-WIRE-01 其間改 Step 11 引言與第 4 項（改以 `Skill(ticket-author)` 調用 close）並新增第 6 項，未動第 5 項引用，約定①②結論不變。[未驗] 約定③仍無真實「最後一張票」覆蓋確認案例（TKA-WIRE-01 收尾票檔未記載本項觸發，且其後無 `[PROJMAN-SYNC]` 提交）。
- 落差與下一步：判斷：部分符合 — 已驗收 2/3（約定①②靜態與文字推演成立）。缺口：約定③真實觸發未驗；「最後一張票」判斷為 (C)／missing-wiring，未豁免。限制：模型契約、無機械後盾；自評。下一步：待一次真實「最後一張票」收束或最終交付總結前，確實執行覆蓋確認並依結果回報。
- 最近 align：2026-10-22 800c53948dd277d9c26759ff6080a3f451a5115b 範圍 PM5-C

### PM5-D
- 預期功能與規格：不選回已確認錯誤的舊表（3 項）：①定位規則加作廢清單（`plans/roadmap/autonomous-engineering-gate-outcomes.md`），使用者指定須來自當前對話，執行端自行於引數指名不得覆寫；②定位回報判讀：本議題原本無候選且無明示目標→不執行、不輸出；本議題相關候選被排除而無合法候選→「定位未完成」；使用者明示成果文件找不到→「定位未完成」；有候選但不能唯一歸屬→揭露歧義；全域排除不致使無關議題產生雜訊；③真實觀察：顧問議題真實收束時不選回舊表並回報「定位未完成」，且無關議題無雜訊。
- 驗收方式與必要前提：①靜態檢視；②文字推演；③真實觀察。證據分層分別列示，不互相升格。前提：③須有顧問議題票真實收束；顧問成果文件之資料處置（定位／建立／搬移舊表）屬另線、本項不要求。
- 實際成果與證據：[靜態] 2026-10-22 @HEAD `6ea5e261`：約定①：`projman/SKILL.md` 〈成果文件定位規則〉第 2 項載明作廢清單與 `plans/roadmap/autonomous-engineering-gate-outcomes.md`（`作廢清單` 命中 3）、第 1 項「指定須來自使用者於當前對話；執行端自行於引數或 payload 指名者不算且不得覆寫作廢清單」；〈被治理條款呼叫時〉第 1 項載明「定位未完成」與不輸出之分情形（`定位未完成` 命中 1）。[文字推演] 約定②：票檔 AC-7 C8（四情況）、C9（執行端指名舊表仍被排除；使用者當前對話明示可覆寫）、C10（全域排除不使無關議題產生雜訊）逐步附 `檔案:行號`。[具名豁免] `AC6-AC7-SCOPED-ACCEPTANCE`：AC-7 範圍內成立，不提升；定位回報依賴「相關候選」之議題判讀、無機械 producer（Producer (C)／missing-wiring，未豁免）。[隔離案例] 無。[未驗] 約定③真實觀察：顧問議題票真實收束時不選回舊表並回報「定位未完成」，且無關議題無雜訊——尚無顧問議題票於新規則下真實收束；本次手動 align 以使用者明示路徑定位本文件，未走自動搜尋排除路徑，不構成觀察。舊表之資料處置（定位／建立／搬移）屬另線、本項不要求，亦未執行。 [靜態] 2026-10-22 重核 @HEAD `800c5394`（自上次 align 錨點 `6ea5e261` 起，`projman/SKILL.md`、`CLAUDE.md`、`ticket_pattern.md` 無改動；TKA-WIRE-01 只改 `ssot-governance.md` 與 `sv/SKILL.md`）：`作廢清單` 於 `projman/SKILL.md` 命中 3、`定位未完成` 命中 1，該檔其間無改動，約定①②結論不變。[未驗] 約定③仍無顧問議題票於新規則下真實收束之觀察；本次手動 align 以使用者明示路徑定位本文件，不構成觀察。
- 落差與下一步：判斷：部分符合 — 已驗收 2/3（約定①靜態、②文字推演成立）。缺口：約定③真實觀察未驗；定位回報判讀為 (C)／missing-wiring，未豁免。限制：模型契約、無機械後盾；自評。下一步：待顧問議題票真實收束累積案例，不為驗證製造。
- 最近 align：2026-10-22 800c53948dd277d9c26759ff6080a3f451a5115b 範圍 PM5-D

### PM6-A
- 預期功能與規格：寫入入口機械強制（3 項）：①事前攔截：成果文件（以結構標記 `## 階段成果表` 判定）的 Edit／Write 在沒有有效 Projman 標記時被 deny，標記由 `Skill` 呼叫 `projman` 時記下模式與時間、約 30 分鐘有效；②區域規則：`base` 只能建立 HEAD 不存在的新檔，`update` 只能改基準、各 ID 的預期功能與驗收方式、約定（同批須加已確認變更列）與已確認變更，不改成果表數值、明細證據與最近 align（動約定列的達成度與狀態重新衍生除外），`align` 只能改明細的證據、落差、最近 align、摘要表的已驗收／達成度／狀態與基準的最近 align，不改約定、預期功能與驗收方式、核心能力、已確認變更；③git 保底：`commit-msg` 對成果文件要求訊息為 `[PROJMAN-SYNC]` 加模式，並以同一套規則檢查 diff 只落在該模式的區域。
- 驗收方式與必要前提：①以真實 Claude Code session 做反例（無標記直接 Edit 成果文件被 deny）與正例（Skill 呼叫內依模式改檔被放行）；②以單元測試涵蓋三模式各自允許與禁止的區域與邊界（含動約定須同批變更列）；③以反例（訊息或區域不符的提交被拒）與正例（符合的提交通過）。證據分層（單元測試／隔離案例／真實 session 觀察）分別列示，不互相升格。前提：已開票並交付檢查腳本與 hook 掛載（開票須經 `/ticket-author plan`，本項不含實作）；`.git/hooks` 已部署；Bash 寫檔偵測的缺口依〈基準〉假設如實揭露，不算已封死。
- 實際成果與證據：未開始（使用者 2026-10-22 明示需求；尚無實作）。
- 落差與下一步：— （尚未盤點；下一步為經 `/ticket-author plan` 規劃並開票，本次 update 不開票、不改 hook）
- 最近 align：無

## 已確認變更
| 日期 | 變更前 | 變更後 | 影響 | 使用者確認 |
| --- | --- | --- | --- | --- |
| 2026-10-18 | 〈基準〉未明載「方法為可替換的實作選擇」（PM0-A 約定③的缺口，由 align 揭露） | 〈基準〉「規格／架構約束」明載：「方法為可替換的實作選擇；可更換技術路線，但不得因此默默縮減原始使用者成果。」 | 澄清既定原則；不新增 ID、不改任何約定數（合計仍 35）；成果表的已驗收／達成度／狀態未由 update 變動，PM0-A 約定③是否成立待下一次 align 重驗 | 使用者 2026-10-18 對 PJMAN-OUTCOMES-01 REVIEW 內補驗之裁決（接受以真實 update 澄清，非新的產品取捨） |
| 2026-10-22 | 成果表無唯一寫入入口、共同自主保存、最終覆蓋確認、舊表排除相關成果項 | 以新 ID 追加 PM5-A（約定 3）、PM5-B（5）、PM5-C（3）、PM5-D（3），合計新增 14 項，全文約定合計 35→49；新列已驗收＝—、狀態＝未盤點，不繼承任何既有完成狀態；〈基準〉核心能力增補第 8 項 | 純增補，不縮減、不改既有 ID 與約定；既有各列已驗收／達成度／狀態由 update 不變，待 align 逐項重判 | 使用者 2026-10-22 對 `PJMAN-CLOSELOOP-01` 結案裁決（新增能力需求已確認，update 授權已確認） |
| 2026-10-22 | PM2-D 約定④ 規格字面為三態「已執行／已回填／未提交」，治理列與 Skill 已統一為四態 | PM2-D 約定④ 規格字面改為依實際進度擇一「未完成呼叫」／「回填未完成」／「已回填未提交」（另「已提交」與「無差分」為非失敗結果）並附 dirty 路徑、如實列出已知限制 | 僅對齊舊規格描述至現行契約（`PJMAN-AUTOALIGN-01` remaining ②）；約定數仍為 6，不縮減；PM2-D 的證據與判斷由後續 align 重判 | 使用者 2026-10-22 對 `PJMAN-CLOSELOOP-01` 結案裁決 |
| 2026-10-22 | 〈基準〉假設稱「PM2 自動鏈尚未啟用」、目前階段 PM1，PM2-A～E 必要前提稱「治理條款呼叫票尚未（開票、）交付」 | 〈基準〉假設改載 PM2 自動鏈已啟用與 PM5 模型契約待觀察、目前階段 PM5；PM2-A～E 前提改載 `PJMAN-AUTOALIGN-01` 已於 2026-10-20 交付並 CLOSED、前提已滿足 | 澄清已過期的基準與必要前提；不新增 ID、不改任何約定數、不改任何已驗收／達成度／狀態 | 使用者 2026-10-22 對 `PJMAN-CLOSELOOP-01` 結案裁決 |
| 2026-10-22 | 成果文件寫入入口僅為行為契約（無機械後盾），〈基準〉約束禁止新增 hook／腳本 | 放寬約束：允許新增一支檢查腳本與其 hook 掛載（Claude Code `PreToolUse` 加 git `commit-msg`）強制成果文件只能經 Projman 三模式寫入；以新 ID PM6-A 追加 3 項約定；核心能力 #8 補寫入入口機械強制；假設補 PM6 已知限制 | 新增成本與約束放寬；以新列追加，約定合計 49→52；PM6-A 已驗收＝—、狀態＝未盤點，不繼承完成狀態；其餘 ID 的約定數、已驗收、達成度、狀態不變；目前階段維持 PM5；實作須另經 `/ticket-author plan` 開票，本次未開票、未動 hook；本次 update 未執行 align | 使用者 2026-10-22 明示：成果文件禁止直接修改，base 後規格需求改走 update、更新成果表走 align，並要求機械強制；並以「請使用 /projman update」確認上述提議 |
