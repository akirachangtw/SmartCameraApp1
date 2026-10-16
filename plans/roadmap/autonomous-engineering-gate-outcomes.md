# Autonomous Engineering Gate Outcomes

## 基準

- 基準日期：2026-10-16。來源：使用者於 `/projman base` 訊息提供之需求基準（本文件依其整理，未從現有程式反推或改寫需求）。
- 專案目標：指定工作後，executor 能自行完成已授權工作；遇到不能判定的工程問題，取得顧問策略，必要時取得外部事實，再完成修復、驗證並返回原工作。只有真正需要新權威，或合法有界路徑仍未收斂時，才交給 Akira。
- 最小成功場景：
  - 至少一個非 R0 限定的代表性工程問題，完成「問題與證據 → 可用顧問策略 → executor 消費策略 → 修復 → 驗證 → 返回原工作」。
  - 證明 Cross-Review 達步驟上限後，符合條件的新策略有一次合法、有界的恢復送審：第一次可用、第二次不可重發、綁定正確修復範圍與報告、保留既有歷史。
- 不可默默刪減的核心能力：
  1. 承接一般實作、QA、SV、審查與收尾中的工程問題，不限 R0。
  2. 顧問回件必須成為後續修復與驗證，不只保存回答。
  3. 顧問可提早介入，不為湊次數重試。
  4. 步驟級上限不能被當成整條工程解題鏈結束。
  5. 成本與終止條件有界，不無限重播。
  6. 真正權限限制不繞過，可解的工程問題不提前丟給使用者。
  7. 並行與恢復不能破壞其他 session 的工作。
- 規格／架構約束：
  - CLI、直接模型 API、原生 Advisor、模型名稱、回件格式與腳本位置皆為可替換實作選擇；直接 Opus API 是合法候選，非唯一指定。模型能回話不等於核心成果完成。
  - 已接受合理 API 支出，不再沿用零 API 支出的舊限制。
  - 適用範圍只限本工作流。
- 非目標：擴成通用治理平台；重做整個 Gate；重開 CLOSED 票；把通知與所有擴充覆蓋設為核心 MVP 前置。
- 假設（待核對，不當成已證實缺陷）：
  - A. 整合驗收依賴可能倒置：呼叫器驗收可能要求 API 顧問 → advisor consult → advisor 層修驗，但現役身分規則可能仍限定原生 Advisor。**狀態：已裁決、未落地**——既有 D-a 已採「實際工程顧問呼叫沿用 `consult --kind advisor`，附必要回件證據，禁止主 agent 自判冒記」；不再要求使用者裁決，待落地後於 align 核對實際實作（含 T01 驗收是否先用到 T02 身分放寬的依賴）。
  - B. 一次恢復送審可能只有文字約定：須核對實際一次性狀態承載點、讀取者、第二次拒絕、修復範圍與報告綁定、歷史保留。不能以 Gate 每層三輪取代一次性恢復控制。
- 歷史背景（使用者提供，**非本次新證據**，待首次盤點）：歷史遠端基線 `5b1a1d21dd6452d4a86412e5d1996e746138dde6`；S1 有部分底座成果；S2 新路徑當時仍在規劃；S3、S4 當時未有完整閉環；不能說只剩通知就全部完成。
- 目前階段：S2（依使用者歷史背景，待首次盤點確認）
- 最近 align：2026-10-16 首次現況盤點（S0 核對決策、S1～S4 逐階段取證，S5 未展開）：S0 符合；S1-R1／R5／R8 部分符合；S2-R2 部分符合；S3-R3 未驗、S3-R4 部分符合、S3-R7 部分符合；S4-R6 尚不具備驗收條件。取證修訂：HEAD `06292acc0ced97d16fb6e2cca3b0534477788f9b`，工作樹乾淨；歷史基線 `5b1a1d21` 為其祖先，基線至 HEAD 之間 `.claude/scripts|hooks|rules|skills/sv|skills/qa` 無 commit（閘門面未變）。本次實測：4 套隔離測試（53＋54 tests＋2 支 shell）。沿用紀錄：`.git/gate/*.jsonl`、PROGRESS。未呼叫模型或付費 API，未改 Gate／helper／plan／Registry

## 階段成果表

| 階段／成果 ID | 預期功能與規格 | 驗收方式與必要前提 | 實際成果與證據 | 落差與下一步 |
| --- | --- | --- | --- | --- |
| S0 目標與限制確定 | 使用者目標、權威、成本、非目標與方法可替換性清楚；不再把零 API 支出當成現行限制 | 可核對 `/projman base` 訊息中的明確需求決策 | [文件] 基準段含目標、權威（升報條件）、成本（已接受合理 API 支出）、非目標、方法可替換性；「已確認變更」記載零 API 支出限制已撤除；顧問 plan D-d 同樣撤除該限制，二者一致。僅核對需求決策，未涉其他階段。 | 判斷：符合 — 需求決策可核對且與 plan D-d 無衝突。 |
| S1-R1 已授權工作自修驗 | R1：已知且已授權的工作，executor 可自行修復、驗證並續作 | 相關 helper、測試及代表性續作證據；限制與未驗整合範圍明列。不能只靠規則或檔案存在宣稱完成 | [已執行·本次] 2026-10-16 `test_gate_ledger.py` 53 tests OK；`gate_stop_guard_test.py` 54 tests OK（皆為 stdlib unittest，以暫存 repo／暫存目錄執行，未動本 repo 的 ledger）。[已執行·沿用紀錄] `.git/gate/*.jsonl`（唯讀）有 8 張票的 incident 紀錄，其中 ADV-GATE-02／03／04／OWNER-01／REVIEW-01 含 finding→round_start→round_end；ADV-GATE-02 round 結果 pass，ADV-GATE-04 兩輪 fail 後記 step_limit。[靜態] 入口 `check` 呼叫已接入 qa／commit／sv／closeout／done（`ssot-governance.md` 覆蓋矩陣）。[文件] 同矩陣載明 `done` 無推進證據 producer。 | 判斷：部分符合 — 自修驗的 helper、入口與多票真實 ledger 紀錄存在；類型：實作可能存在但缺端到端執行證據（`done` 無 producer；rounds 的 pass／fail 結果僅 ADV-GATE-02／04 逐筆讀過，其餘僅見計數）。不重做已交付能力。 |
| S1-R5 分層停損與升級語意 | R5：分層停損、可提早升級、不重播；新顧問來源加入後，規則語意與實際承接一致 | 分兩段：**底座語意**（分層停損、提早升級、不重播的既有機制）在 S1 核對；**新顧問來源 consult／advisor 層承接**於 S2／S3 確認（必要前提：假設 A 核對）。新來源段未完成時，R5 不宣稱全部完成，但不阻擋 S2 呼叫能力先行 | [已執行·本次] 2026-10-16 `python -I -m unittest discover -s .claude/scripts/tests -p test_gate_ledger.py`：53 tests OK（含逐層上限獨立、策略去重跨層、首個 advisor／r0 輪須同 incident consult、r0 耗盡升 advisor 不升人類）——僅證 helper 底座，不代表整體功能。（首次嘗試以 pytest 因環境未安裝失敗，改以 unittest 通過。）[已執行·沿用紀錄] ADV-GATE-04 ledger seq 9–17：步驟上限後記 `step_limit`→local／advisor consult→5 筆 `strategy_scan` 逐項排除→`human NO_AUTHORIZED_STRATEGY`，顯示停損與提早升級規則實際被使用。[靜態] 現役契約仍限定原生 Advisor：`ssot-governance.md:211`、`rules/cross-review.md:51`、`gate_stop_guard.py:377`；`gate_ledger.py` `consult --kind` 僅 `local|advisor|r0`，`--note` 為自由文字、helper 不驗 receipt。 | 判斷：部分符合 — 底座語意有單元測試與真實使用紀錄；類型：新顧問來源（API）承接＝已知必要能力尚未交付，且現役契約尚未放寬（plan D-a 已核准、未落地）。R5 不宣稱全部完成。下一步：隨 plan T02 放寬後於 S2／S3 確認承接。 |
| S1-R8 並行／恢復隔離 | R8：並行／恢復不破壞其他 session 工作；涵蓋 owner、marker、reset 與對應隔離行為 | 同 S1；不得由局部測試宣稱完整跨 session 隔離，未驗範圍須明列 | [已執行·本次] 2026-10-16 `owner_writer_test.sh` ALL PASS、`session_start_owner_test.sh` ALL PASS（含以真實 `activate_ticket.sh` 在暫存目錄寫 owner；owner 非本人時 reset 整組略過、本人時清三檔且 `active.json` 保留）、`gate_stop_guard_test.py` 54 OK——皆為暫存 repo／暫存目錄的隔離測試（stub 層）。[文件] `.claude/PROGRESS.md` #30 載明 owner（ADV-OWNER-01）、reset 清理隔離（ADV-RESETISO-01）限定交付 [stub]，真實 Stop／`/clear`／SessionStart 鏈、同 session env↔payload 逐值比對仍 UNVERIFIED。 | 判斷：部分符合 — owner／marker／reset 的隔離行為在暫存環境下有測試通過；類型：實作可能存在但缺真實多 session 執行證據。不得由局部測試宣稱完整跨 session 隔離；真實並行情境未驗。 |
| S2-R2 顧問真正可用 | R2：遇到未知工程問題可取得可採取的顧問策略，不限 R0；涵蓋可用入口、成功／錯誤回件與成本控制 | 可用呼叫入口；**真實呼叫須成功取得可用策略**；成本控制。錯誤處理以決定性測試驗證；真實錯誤僅沿用自然發生的紀錄，**不刻意發送付費錯誤請求**。錯誤、空回件、截斷一律不可記為成功策略。plan、mock 或模型能回話不代表完整完成。必要前提：真實顧問服務可呼叫（API 支出已接受）；假設 A 核對 | [已執行·沿用紀錄] 原生 Advisor 有真實使用：ADV-GATE-04 ledger seq 11 `consult --kind advisor`，note 記為實際原生 Advisor 調用並附建議概述（回件內容加密不可驗）；本 session 亦兩次成功取得可用策略並被消費（未經 Gate incident，非 Gate 鏈證據）。[靜態] 新 API／CLI 顧問入口：本次搜尋範圍＝本 repo 已追蹤與未追蹤檔中 `*.py|*.sh|*.ps1|*.cmd|*.bat|*.toml|*.yaml|*.json`（排除 `plans/`、`docs/`、`app/`），關鍵字 `anthropic|ANTHROPIC_API_KEY|messages.create|claude -p|--bare|advisor_20`：無命中；`.tmp/ticket-author/20261016-cli-advisor-plan.md` 為 plan 宣告（含 Rev C.2），非呼叫入口，亦非真實調用。[未驗] 無任何去識別化的真實 API 成功／錯誤回件與成本（`usage`）紀錄可查；原生路徑的錯誤與成本證據亦無紀錄。本次未呼叫付費 API。 | 判斷：部分符合 — 原生 Advisor 路徑有真實成功紀錄；類型：新呼叫入口＝已知必要能力尚未交付（plan 已設計，T01 尚未開票），真實成功／錯誤回件與成本控制證據目前不存在，這不是因本次禁止付費而推定失敗。下一步：開 T01 完成呼叫器與一次真實成功取得可用策略（錯誤／空回件／截斷不計成功）。 |
| S3-R3 顧問策略消費閉環 | R3：顧問回件被 executor 消費，完成修復、驗證並返回原工作 | 至少一個非 R0 限定的代表性完整工程場景（問題與證據 → 策略 → 消費 → 修復 → 驗證 → 返回原工作）。必要前提：S2 顧問可用；假設 A 核對 | [已執行·沿用紀錄] 對 8 張票 ledger 逐一統計事件：無任何 incident 同時具備 `consult --kind advisor` 後接 advisor 層 `round_start`／`round_end(pass)` 的完整序列。最接近者 ADV-GATE-04：seq 11 原生 Advisor consult 後未開 advisor 層 round，5 筆 strategy_scan 全數排除→`human NO_AUTHORIZED_STRATEGY`→Akira 裁決限定交付（鏈 2／3／4 保持 UNVERIFIED）。[文件] PROGRESS #30：相關票皆限定交付，真實 cross-reviewer 新定義整合未驗。[靜態] 原生 Advisor 為現役合法 consult（契約未變），API 回件則尚不能合法記為 advisor consult（見 S1-R5 之條文行號）。 | 判斷：未驗 — 類型分兩段：原生 Advisor 路徑具備前提（契約合法）但無完整鏈執行證據；API 路徑缺驗收前提（入口未交付＋契約未放寬）。未把未合法承接的回件當成合法 consult。下一步：見回報「最小下一步」。 |
| S3-R4 外部事實回流 | R4：必要外部事實可取證，回流同一問題，並被後續修復與驗證實際消費 | 外部事實分支另有適用情境與消費證據；不以 R3 的成功案例概括 | [已執行·沿用紀錄] ADV-GATE-02 ledger seq 1–5：一次 PPSS 真實鏈驗收實跑（`ppss_record` state=RETRIEVED，來源網域 docs.python.org；`ppss_read` 一次，`supports: none`、`evidence_ok: False`；round_end pass）——為 helper 驗收，非工程問題。[已執行·本次] `test_gate_ledger.py` 含 `test_ppss_does_not_touch_layer_counts`（53 OK 之一）。 | 判斷：部分符合 — 取證與回流機制有一次真實實跑；類型：實作存在但缺消費證據（該次 `evidence_ok=False`，無後續修復或驗證實際消費外部事實的紀錄）。不以 R3 或此次實跑概括外部事實分支。 |
| S3-R7 合法升報與續作 | R7：真正需要人類才升報，其他合法工作仍續作；避免修好卻因沒有合法再審出口而變成人工例行確認 | 代表情境顯示：可解問題不升報、真需人類時才升報；顧問來源已有合法承接。分支可分列，不以單一成功案例概括 | [已執行·本次] `test_gate_ledger.py` 53 OK（含 NO_AUTHORIZED_STRATEGY 須 advisor 層、r0 耗盡升 advisor 不升人類）、`gate_stop_guard_test.py` 54 OK。[已執行·沿用紀錄] ADV-GATE-04 ledger：三次 REPORT_FIX 修報告後達 LIMIT，記 `human NO_AUTHORIZED_STRATEGY`，由 Akira 以豁免額外 Codex PASS 收束；ADV-LAYER-01 亦有 `human`／`human_resolve`。[靜態] 升報類別與前置條件見 `ssot-governance.md` 升報節。 | 判斷：部分符合 — 升報前置有機械檢查，且已有真實案例；該 ADV-GATE-04 案例同時顯示缺口：修好後無合法再審出口而升給人工（對應 R6 未交付）。類型：已知必要能力尚未交付（恢復送審）。隨 S4-R6 處理。 |
| S4-R6 受限後一次合法恢復送審 | R6：Cross-Review 步驟上限後，合法新策略仍有一次有界恢復送審；一次性使用狀態可保存、可讀取、可拒絕第二次，綁定正確 diff／報告，不清除原歷史；每層三輪與恢復送審一次是不同計數 | 第一次可用；第二次不可重發；正確 diff／報告綁定；原計數與歷史不清除。必須有實際狀態承載與讀寫機制，不能只靠文字約定。必要前提：假設 B 核對 | [靜態] 搜尋邊界：本 repo `.claude/` 已追蹤與未追蹤檔（排除 `PROGRESS*`），關鍵字 `resume_review|resubmit|恢復送審|one-shot|一次性恢復|recovery claim|.resume`：僅 2 筆無關命中（`CLAUDE.md` SOP 步驟名 Resume、`REPAIR_ROUTING.md` 外部事實問題不重送）；`.git/gate/` 無 `*.resume*` 檔；`.tmp/` 僅 `cross_review_state`（失敗預算）與 `cross_review_pass`（PASS 回執），皆非恢復送審載體。`rules/cross-review.md` 退出機制：預算用盡後再重跑非已授權策略，唯一恢復門為 Akira ack。[文件] plan 的 T03（承載 `<git-common-dir>/gate/<TICKET>.resume.json`、`claim`／`verify`、O_EXCL 防重發、diff／報告綁定）僅為設計，未開票、未實作。此為有限搜尋，不宣稱全 repo 絕對不存在。 | 判斷：尚不具備驗收條件 — 類型：已知必要能力尚未交付（一次性承載、寫入／讀取者、第二次拒絕、diff／報告綁定、原計數歷史保留皆無實作可驗）；假設 B 在本次搜尋範圍內成立，但不以關鍵字搜尋證明不存在。S4 尚未實作不判為當前階段失敗。Gate 每層三輪（ledger）與恢復送審一次為不同計數，不得互相替代。下一步：T01 之後開 T03。 |
| S5-R9 升報通知 | R9：升報通知對準真實待人狀態；通知與真實待人狀態一致，必要擴充分支得到驗證（含按實際需要補充的其餘入口與恢復情境） | 通知內容與真實待人狀態一致；擴充分支各自有適用情境。**不在下一次核心盤點範圍**，不阻擋 S2～S4 的核心 MVP 交付 | 不在下一次核心盤點範圍 | — |

## 已確認變更

| 日期 | 變更前 | 變更後 | 影響 | 使用者確認 |
| --- | --- | --- | --- | --- |
| 2026-10-16 | S2-R2 驗收含「真實成功／錯誤回件」；S1-R5 單列驗收 | S2-R2：真實呼叫須成功取得可用策略，錯誤處理改決定性測試、不刻意發送付費錯誤請求；S1-R5：底座在 S1 核對、新來源承接於 S2／S3 確認 | 驗收口徑收準，未縮減核心成果；R5 新來源段未完成不宣稱全部完成 | 使用者（2026-10-16 後續訊息） |
| 2026-10-16 | S4-R6 判斷為「未驗」；假設 A 待使用者裁決；S2-R2／S3-R3 證據措辭含「不存在」「被消費」 | S4-R6 改「尚不具備驗收條件」（假設 B 保留待核實）；假設 A 改為既有 D-a 已裁決、未落地；S2-R2 改「局部證據／本次範圍未定位」，S3-R3 明示局部證據非 Gate 閉環證據；下一步回既有顧問 plan 有限收斂（修 T01 驗收對 T02 身分放寬的依賴；實作前定 S4 一次性狀態的承載、讀取與防重發；不新增平行 plan、不重做 diag、不重議 D-a／D-b） | 驗收口徑與判斷收準，未縮減核心成果；53 項 helper 測試（`python -I -m unittest discover` 重跑通過）僅證 helper 底座，不當整體功能通過 | 使用者（2026-10-16 後續訊息） |
| 2026-10-16 | 沿用零 API 支出限制 | 已接受合理 API 支出；直接 Opus API 為合法候選；本次 base 不呼叫模型 API | S2 方法選擇放寬，實作選擇仍可替換 | 使用者 `/projman base` 訊息（2026-10-16） |
