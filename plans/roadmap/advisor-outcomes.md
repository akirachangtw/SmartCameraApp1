# Advisor Outcomes

## 基準
- project_key：SmartCameraApp1　project_label：Cueshot APP
- topic_key：advisor　topic_label：Advisor
- 票前綴：ADV-（可選線索，非唯一資格；已有 `ADV-ENTRY-01` 等）
- 專案目標：工程師使用 Advisor、PPSS 與多個 Skill 時，能力取得、下一步判斷與接續規則不再分散；重用既有能力形成自主決策閉環，使主 executor 不停在「已諮詢」「建議下一步」，也不需使用者手動搬運結果、逐步下令。不是只建立一個能呼叫模型的入口，也不是讓所有 Skill 讀同一份提示詞就算完成。
- 最小成功場景：主 executor 接到已授權工作後：①知道且安全可做的，直接執行與驗證；②遇到無法判定的工程問題，經共同入口取得 Advisor 協助；③需要外部事實時，再取得必要的 PPSS 證據；④消費建議與證據，完成修復、驗證並返回原工作；⑤需要接續其他 Skill 時，由共同 Routing 決定下一步，executor 實際呼叫，完成後接回共同流程；⑥只有真正需要新的產品決策、權限、不可逆操作或其他人類權威，或既有授權內的合法有界路徑已無可行下一步時，才提交最小問題；阻塞限於相關工作，其他無依賴、已授權且安全的工作仍續行；⑦最後統一報告成果、限制、未完成項與真正待決事項。取得回答或記帳成功，不等於工程問題已解決。
- 不可默默刪減的核心能力：1 Advisor API、native Advisor、PPSS 經共同入口取得與返回；2 派發、回件、票與 incident 正確關聯，重複回交不重複記帳，失敗情境（incident 關閉交錯、快取例外、版本不相容、必要資料缺失）不隱藏；3 executor 消費結果、完成修驗並返回原工作（回答／記帳成功不等於解決）；4 跨 Skill 下一步政策集中於共同 Routing 決策入口，executor 實際呼叫後繼並接回，已遷移舊決策路徑移除、無雙軌；5 Skill 新增、替換、停用的主要調整集中於接點與必要 adapter，不連帶改寫無關 Skill；可選能力停用不阻塞無關工作，必要能力缺席明確處理、不靜默當作通過；6 有界自主續作：已知、安全、已授權的工作直接做；只有真正需要新的產品決策、權限、不可逆操作或其他人類權威，或既有授權內的合法有界路徑已無可行下一步時，才提交最小問題（沿用既有成本、計輪與停損，不另設一套）；阻塞限於相關工作，其他無依賴、已授權且安全的工作仍續行；可延後且不影響核心功能的不足不自動成為人工停點，也不把未完成的必要成果寫成通過；最後統一報告。
- 規格／架構約束：主 executor 維持工作 owner；重用既有 `.claude/scripts/route.py`、接線表、caller、工具及 ledger；沿用既有授權、成本、計輪與停損語義，不因重整另設一套；中央接續是功能要求，不預先限定必須用常駐主 loop、daemon 或特定框架實作；歷史隔離案例、舊入口與目前入口的真實全鏈證據必須分開，不得互相冒充。
- 驗收案例語義（各列驗收方式共用）：可使用自然工作案例，或明確標示的受控代表案例；必須實際走目前入口及本項所要求的真實能力。代表案例不冒稱自然事故；模擬／離線部分不得替代要求真實工具執行的證據。不為驗收製造虛假失敗，也不以等待自然事故作為額外前提。
- 主要 AI 模型（設計前提，使用者 2026-10-22 明示，非可選偏好）：Advisor 這項能力繼承自 Claude Advisor 功能，主要 AI 模型是 Claude（`advisor.api`、原生 `advisor` 與 Claude 訂閱 CLI 皆屬 Claude 家族，仍是不同路徑，不混稱）；備援是 Codex。現行 `route_table.json` 無主備欄位，`alternatives` 只列同角色其他可用來源、不排序；主備順序由誰判定與如何落實，屬 P2 共同 Routing 的細部接續方式，待實作取證確認，不重新選擇平行框架。
- 非目標：企業級 orchestrator 平台、DB、queue、第二套 ledger、簽章系統；為每個小修增加研究、審查或後補票；自動擴張到未授權的產品需求、發布或不可逆操作。
- 假設（尚未確認的技術或依賴）：P2 沿用既有 route.py 與共用接線表；具體接續介面、facts 與模型判斷的銜接方式待實作取證確認，不重新選擇平行框架；既有回報中的「真實全鏈未驗／N＝0」為歷史限制，待首次盤點核對，本次不重新判定；`ADV-ENTRY-01` 為限定交付回報，其範圍與本表各約定項的對應待首次盤點確認。
- 目前階段：P1
- 最近 align：2026-10-22 1d8a1fe8a8b02430929346cdf48235d9927370aa P1-A P1-B P1-C

## 階段成果表
| ID | 階段 | 成果 | 已驗收 | 約定 | 達成度 | 狀態 |
| --- | --- | --- | --- | --- | --- | --- |
| P1-A | P1 能力共用入口 | Advisor／PPSS 取得與返回 | 1 | 4 | 25% | 部分符合 |
| P1-B | P1 | 派發、回件與記帳正確性 | 4 | 5 | 80% | 部分符合 |
| P1-C | P1 | 消費結果、修驗與返回原工作 | 5 | 5 | 100% | 符合 |
| P2-A | P2 工作流中央接續 | 中央決策與實際接續 | — | 4 | — | 未盤點 |
| P2-B | P2 | Skill 新增、替換與停用 | — | 5 | — | 未盤點 |
| P2-C | P2 | 有界自主續作、局部隔離與最終總結 | — | 5 | — | 未盤點 |

## 明細
### P1-A
- 預期功能與規格：Advisor／PPSS 取得與返回（4 項）：①Advisor API 經共同入口取得並返回 executor；②native Advisor 經共同入口派發與回交取得並返回 executor；③PPSS 經共同入口取得並返回 executor；④僅在無法判定的工程問題（Advisor）或外部事實缺口（PPSS）才取得，知道且安全可做的直接執行，不為每個小修增加諮詢或研究。
- 驗收方式與必要前提：以自然工作案例或明確標示的受控代表案例（語義見基準〈規格／架構約束〉驗收案例語義），經目前共同入口（`route.py` 及其接線）對 Advisor API、native Advisor、PPSS 各做一次真實取得，並確認回件到達 executor；④以「已知可做」與「無法判定」兩種情境對照。證據標記分開寫；歷史隔離案例、舊入口與目前入口的真實全鏈不互相替代。前提：目前入口與所需能力可用。
- 實際成果與證據：[已執行] 2026-10-22 @HEAD a189bcef：`python -I .claude/scripts/tests/test_route.py` 65 案 OK（A1–A15、N1–N7、P1–P9 等，經 `route.main`／子行程執行真實入口與真實 ledger CLI）；`python -I .claude/scripts/route.py table check` 回 ok:true，callable＝advisor.api／advisor.native／research.ppss。限制：API 以注入替身 client、native／PPSS 以派發／回交夾具，**僅 stub 層通過**，不是真實取得。[文件] `plans/cso_briefing/project/deliver/ADV-ENTRY-01_route.md` 收束結果載明真實 session 端到端 N＝0。[靜態] `git log bdc4595d..HEAD -- .claude/scripts` 為空，票內證據仍對應現行腳本。約定①②③[未驗]：未對 Advisor API、native Advisor、PPSS 各做真實取得（API 與 PPSS 為付費呼叫，本次未獲授權；native 需於 ledger 開 incident，會寫入進行中票的共用 ledger）。約定④[未驗]：無「已知可做」與「無法判定」兩情境的對照案例，僅 Gate 條文（`.claude/rules/ssot-governance.md:214`）為模型契約，無機械後盾。**補（2026-10-22 @HEAD 1d8a1fe8，使用者授權之真實接線驗證）：**[已執行] 約定③：受控代表案例（明標，非自然事故；隔離 repo 票號 `CASE-ROUTE-01`，獨立 ledger `.git/gate/CASE-ROUTE-01.jsonl`，位於暫存目錄，非耐久）經目前 Router `route.py consult --source research.ppss` 完成 dispatch（`d2-90022a788b532dbe`）→真實 `research_submit`（request_id `rsch-20261022-2e67597440`，cache＝miss，回報 `estimated_cost_usd` 0.005，為估計值，5 筆結果）→bind→record（ledger seq 2，`RETRIEVED`）→read（seq 3，`SOURCE_READ`、`evidence_ok` true），回件到達 executor 並被引用；限制：Router 只記宣告，不證工具真以派發參數被呼叫。約定①[未驗]：`advisor.api` 需 `ADVISOR_ANTHROPIC_API_KEY`（本機已設，屬計費 API 通道，使用者要求優先訂閱，未使用）。約定②[未驗]：原生 `advisor` 工具本次未測；Codex 與 Claude 低模型經表內別名（`native_advisor` adapter）接入，不是原生 advisor 也不是 API caller，不計入②。約定④[未計入]：已知可做對照 `CASE-CTRL-02` 不開 incident、不呼叫顧問或研究即修復並驗證（ledger 無檔，零 consult、零 PPSS）；但另一側「無法判定」並非真正無法判定（executor 本可本地解），對照不完整。
- 落差與下一步：判斷：部分符合 — 已驗收 1/4（③）。缺口：①Advisor API 未實取（計費通道，需使用者授權用金鑰一次）、②原生 advisor 工具未測、④缺真正無法判定之案例。下一步：授權後以最小 payload 與 evidence 經 `advisor.api` 實取一次；②在下一個自然需要原生顧問的問題用 `advisor.native` 派發與回交一次；④待自然出現之無法判定問題，不為驗收製造失敗。
- 最近 align：2026-10-22 1d8a1fe8a8b02430929346cdf48235d9927370aa 範圍 P1-A

### P1-B
- 預期功能與規格：派發、回件與記帳正確性（5 項）：①派發、回件、票與 incident 正確關聯；②重複回交不重複記帳；③incident 在呼叫期間被關閉的交錯情境不隱藏失敗；④快取例外（cache hit）情境不隱藏失敗、不誤判；⑤版本不相容與必要資料缺失不隱藏失敗。
- 驗收方式與必要前提：對各情境做實際執行或可重現的案例，確認結果為明確的成功或失敗回報而非靜默通過；記帳以 ledger 現況對照。登記事件不證明其中調用的真實性，案例分層（隔離案例／目前入口真實案例）分開記。前提：目前入口可執行，ledger 可讀。
- 實際成果與證據：[已執行] 2026-10-22 @HEAD a189bcef：`python -I` 執行 `test_route.py` 65 OK、`test_gate_ledger.py` 59 OK、`test_advisor_call.py` 25 OK（路徑 `.claude/scripts/tests/`；經真實入口 CLI 與真實 ledger CLI，工具端為注入替身）。計入四項：①關聯＝A3、N2、N3、F3、B1、B2；②重複回交不重複記帳＝N5、K1、F1 系列、B6；③incident 關閉交錯＝A10、F2 兩案（API 呼叫中被關閉：保留 receipt、不記帳；native 檢查與寫入之間被關閉：ledger 拒絕）；⑤版本不相容與必要資料缺失＝B7（缺 `ppss_records` 之舊 ledger 一律拒絕）、A5–A7、N7、P9。約定④快取例外[未驗]：B3–B6 離線案例通過，但輸入形狀依 PLT 契約與先前回報，本次未觀察真實 PPSS 快取命中，不計入。限制：測試與實作同票同作者；Rev C 差分有真實 Codex PASS（`3e92898f..d7ed267c`），Rev C.1 起為聚焦測試與終審接受（`ADV-ENTRY-01_route.md` Rev C.2 第 3 點），非 Codex 審查；ledger 登記不證明工具調用真實性。**補（2026-10-22 @HEAD 1d8a1fe8，真實接線；計數不變仍為 4/5）：**[已執行] 約定①②之真實工具層佐證：隔離 ledger 的同一 incident `INC-001` 內，PPSS 一次、Codex 別名一次、Claude 別名一次，各自派發→回交，ledger 事件 seq 2–5 與派發 id 一一對應（`d2-90022a788b532dbe`、`d2-9271a85bf714453f`、`d2-34292ec1b3a07b49`）；對 Codex 回交重放同一 `--dispatch-id` 得 `REFUSED stale_or_duplicate_dispatch`（exit 5），事件數不增。本次 cache＝miss，未走 `--cached-artifact`，約定④仍無真實命中。**偏差（具體未接通處）**：兩個別名來源的 consult 在 ledger note 都寫成 `source=advisor.native`（`.claude/scripts/route.py:630` 把來源字面寫死），只有 `ref=` 欄可分辨 Codex 與 Claude；ticket、incident、dispatch 的關聯成立，來源名稱無法由 note 辨識。約定③⑤本次未新增證據。
- 落差與下一步：判斷：部分符合 — 已驗收 4/5。缺口：約定④真實快取命中未觀察；另記一項約定外缺陷：別名來源在 ledger note 被記成 `advisor.native`（`route.py:630` 寫死），多來源並用時無法由 note 辨識實際來源。下一步：最小修法為 `route.py:630` 改用 `a.source`（改程式屬另案授權，本次未改）；快取命中待自然出現，不為驗收製造。
- 最近 align：2026-10-22 1d8a1fe8a8b02430929346cdf48235d9927370aa 範圍 P1-B

### P1-C
- 預期功能與規格：消費結果、修驗與返回原工作（5 項）：①executor 消費該情境實際需要的 Advisor 建議及／或 PPSS 證據，採取適用處置，而非只記錄；不強迫無外部事實缺口的問題呼叫 PPSS；②修復後執行局部驗證，由驗證結果判斷問題是否解決（取得回答或記帳成功不等於解決）；③驗證後返回原工作流程，不停在「已諮詢」「建議下一步」；④修復循環沿用既有授權、成本、計輪與停損語義，不另設一套；⑤目前入口的真實全鏈（取得→消費→修驗→返回）有獨立證據，不以歷史隔離案例或舊入口冒充。
- 驗收方式與必要前提：以自然工作案例或明確標示的受控代表案例走完整鏈（語義見基準〈規格／架構約束〉驗收案例語義；不要求同一案例同時呼叫 Advisor 與 PPSS，各適用分支分別觀察），檢視 transcript 與 ledger 中取得、消費、處置、驗證、返回各步；④對照既有授權／計輪／停損語義的現行條文；⑤證據明寫所屬入口與案例層級。前提：具備可實際執行的自然工作案例或受控代表案例，且目前入口與所需能力可用。
- 實際成果與證據：[靜態] 約定④（入口側）：`.claude/scripts/route.py` 對 ledger 只呼叫 `status --json`、`consult`、`ppss record|read`（`:317`、`:352`、`:735`、`:652`），無 `round start|end`／`human`／`step-limit`／`ANTHROPIC` 字樣（`grep -c` 命中 1，為 docstring 的 "human note"）；輪數上限仍在 `.claude/scripts/gate_ledger.py:618-622`（逐層上限與 advisor／r0 層須同 incident consult），入口未另設計輪、成本或停損。範圍：入口與 ledger 兩檔，不證 executor 行為。[未驗] 約定①②③⑤：executor 消費、修驗、返回原工作屬模型契約（`ADV-ENTRY-01_route.md` 限定交付聲明④：模型是否經入口並依回傳續行＝UNKNOWN）；目前入口真實全鏈 N＝0 沿用該票收束結果，為歷史限制，本次未重新判定，也未找到可作 N≥1 的目前入口真實紀錄（`grep -rl "via=route.py" tmp .tmp` 僅命中票檔、報告、stderr 文字，非 ledger 事件；僅限本次檢查範圍，不宣稱全域不存在）。歷史隔離案例、舊入口與目前入口證據分開，未互相升格。**補（2026-10-22 @HEAD 1d8a1fe8，使用者授權之真實接線驗證；受控代表案例，明標，非自然事故，由同一 executor 設計與執行，自評）：**[已執行] 隔離 repo 內 `rotate.py` 在 Windows 因 `os.replace` 時目的檔仍開啟而 `PermissionError [WinError 5]`（修前 `test_rotate` 失敗，本地重現）。經目前 Router 走完——取得：PPSS（見 P1-A）取回 Microsoft Learn 句子 "A file must also be closed before an application can move it."，`ppss read` 記 `evidence_ok` true；Codex 別名一次（`codex exec -m gpt-6-luna`，`codex-cli 0.162.1`，ChatGPT 訂閱登入，stderr 記 model＝gpt-6-luna、provider＝openai、tokens used 27,140）建議先關檔再 replace。消費與處置：採納，讀檔改為 `with` 區塊，`test_rotate` 由失敗轉 OK。第二位顧問：Codex 別名設 `enabled:false` 時 Router 回 `SOURCE_DISABLED` 與 `alternatives:[advisor.claude-haiku]`（exit 4，未替呼叫端選擇），executor 選 Claude 別名，`claude -p --model haiku`（`claude 2.1.304`，claude.ai 訂閱登入，`apiKeySource` 空，已清除 API 金鑰環境變數），實際模型 `claude-haiku-5-5`，CLI 回報 `total_cost_usd` 0.0019（CLI 報告值；訂閱下不當作實際計費或實際零成本），回覆指出缺檔會 `FileNotFoundError`；executor 先本地重現屬實，再採納（缺檔視為空），重跑 `test_rotate` OK。返回原流程：下一必要步驟 `python rotate.py log.txt <行>` 對既有與缺檔 log 皆正確前插。已知可做對照 `CASE-CTRL-02`：修 `greet.add` 回傳 `a-b`，不開 incident、不呼叫顧問或研究，ledger 無檔。ledger 終態：`INC-001` open，`consults.advisor` 2、`ppss_records` 1。歷史限制「真實全鏈 N＝0」改記為：目前入口受控代表案例 N＝1，自然事故仍為 0。限制：Router 只記宣告，不證工具真以派發參數被呼叫或其內容；顧問為別名來源，非原生 advisor 或 `advisor.api`，不替代 P1-A 約定①②；單一 Windows 小案例；「無法判定」並非真正無法判定；未寫入真實 repo 的 ledger。
- 落差與下一步：判斷：符合 — 已驗收 5/5（約定①②③⑤ 本次新增，④沿用 2026-10-22 靜態證據）。限制：僅一個受控代表案例、同一 executor 自評；P1-C 的符合不等於 P1-A 的 Advisor API 與原生 advisor 已取得。下一步：待自然出現之無法判定工程問題，再觀察一次目前入口全鏈；不為驗收製造失敗。
- 最近 align：2026-10-22 1d8a1fe8a8b02430929346cdf48235d9927370aa 範圍 P1-C

### P2-A
- 預期功能與規格：中央決策與實際接續（4 項）：①Skill 保留專業執行責任，返回結果與必要 facts，可以提出建議、缺口與協助需求；不自行維護或執行全域後繼政策，跨 Skill 的最終接續由共同 Routing／中央主 loop 決定（「中央主 loop」指共同決策責任，不限定須用常駐程序、daemon 或特定框架）；②跨 Skill 的下一步政策集中在共同 Routing 決策入口；③executor 依決策實際呼叫後繼 Skill，完成後接回共同流程，而不只輸出建議；④已遷移的舊決策路徑移除，無雙軌。
- 驗收方式與必要前提：檢視 Skill 的返回內容與 Routing 決策入口；以自然工作案例或明確標示的受控代表案例，走一條至少跨兩個 Skill 的流程，觀察實際呼叫與接回（案例語義見基準〈規格／架構約束〉驗收案例語義）；對已遷移項目以 grep 確認舊決策路徑已移除。前提：共同 Routing 決策入口已存在。
- 實際成果與證據：未開始（P2 仍是必要後續，使用者 2026-10-22 指示）。
- 落差與下一步：— （尚未盤點）
- 最近 align：無

### P2-B
- 預期功能與規格：Skill 新增、替換與停用（5 項）：①新增 Skill 的主要調整集中於共同接點與必要 adapter，不連帶改寫無關 Skill；②替換 Skill 同上；③停用 Skill 同上；④可選能力停用不阻塞無關工作；⑤必要能力缺席明確處理（回報並依規則處置），不靜默當作通過。
- 驗收方式與必要前提：各以一次實際的新增／替換／停用演練對照 diff 範圍；④⑤以停用可選能力與模擬必要能力缺席各一案例觀察行為。前提：P2-A 的共同接點已存在。
- 實際成果與證據：未開始（P2 仍是必要後續）。
- 落差與下一步：— （尚未盤點）
- 最近 align：無

### P2-C
- 預期功能與規格：有界自主續作、局部隔離與最終總結（5 項）：①知道且安全、已授權的工作由 executor 直接續作，不逐步等使用者下令；②只有真正需要新的產品決策、權限、不可逆操作或其他人類權威，或既有授權內的合法有界路徑已無可行下一步時，才提交最小問題；阻塞限於相關工作，其他無依賴、已授權且安全的工作仍續行（沿用既有成本、計輪與停損；可延後且不影響核心功能的不足不自動成為人工停點；不把未完成的必要成果寫成通過）；③局部阻塞不自動停止其他無依賴、已授權且安全的工作；④不自動擴張到未授權的產品需求、發布或不可逆操作；⑤最後統一報告成果、限制、未完成項與真正待決事項。
- 驗收方式與必要前提：以自然工作案例或明確標示的受控代表案例（含局部阻塞，語義見基準〈規格／架構約束〉驗收案例語義）觀察局部阻塞時的停下時點，以及其他合法工作的實際續行；檢視最終報告是否含四類內容；④對照授權邊界。前提：P2-A 的中央接續可用。
- 實際成果與證據：未開始（P2 仍是必要後續）。
- 落差與下一步：— （尚未盤點）
- 最近 align：無

## 已確認變更
| 日期 | 變更前 | 變更後 | 影響 | 使用者確認 |
| --- | --- | --- | --- | --- |
| 2026-10-22 | 假設寫 P2 Routing 落點與形式尚未決定；P1-C①預設消費 Advisor 與 PPSS 兩者；驗收案例限「真實工程問題」；停止條件僅列人類權威；P2-A①寫 Skill「不自行決定跨 Skill 下一步」 | 架構骨架沿用 route.py 與共用接線表，僅細部接續方式待實作取證；Advisor／PPSS 按情境需要消費，不強迫兩者皆用；驗收可用自然案例或明標受控代表案例，必須走目前入口與真實能力；合法有界路徑耗盡亦提交最小問題，阻塞限相關工作；Skill 可提建議，全域後繼政策由共同 Routing／中央主 loop 決定 | 僅澄清需求與驗收語義；六個 ID 與約定數（4／5／5／4／5／5）不變，已驗收、達成度、狀態不變（仍全部未盤點），未執行 align，最近 align 仍為無 | 使用者 2026-10-22 明示 update 指示 |
| 2026-10-22 | 基準未載明 Advisor 的主備模型 | 寫入設計前提：Advisor 繼承 Claude Advisor 功能，主要 AI 模型為 Claude，備援為 Codex | 僅補需求前提；六個 ID 與約定數（4／5／5／4／5／5）、已驗收、達成度、狀態不變；未新增約定項（是否新增「主要 Claude 失效時經 Router 改用 Codex」約定，待使用者確認）；本次 update 未執行 align | 使用者 2026-10-22 明示指示 |
