# ADV-GATE-04: 新增 gate_stop_guard.py 並註冊為 Stop hook，使主 agent 於工作途中依 Router 解題、不合格停止被帶回同一條鏈（Stop Gate＋Advisor Router）（Rev A）

**Revision**: Rev A（2026-10-13；依 Akira 指示 R1–R5 就地修訂票面；未實作、未改 lifecycle、不新增 `### AI-` 標題、未重跑 diag／plan）。變更：R1 L217 納入 AI-6 第五處（取代 X4）；R2 SSOT 目標字面移除「（T05）」；R3 External-review decision 改 EXECUTED 既定範圍處置；R4 刪除 hook 端 pending 逐 category 重驗（取代 parent D8，並取代 Rev B.1「明列沿用 3」之前置核對部分）；R5 AI-0 改確認本票已登記。

**Revision (Rev B)**: Rev B（2026-10-13；依 Akira 裁決就地修訂票面與實作，不新增 `### AI-` 標題、不重跑 plan／diag、不新增 helper／state／PreToolUse／agent／新票）。變更：R6 自述 Advisor→AI-6 第六處（L211 改寫為「`consult --kind advisor` 只能記錄實際原生 Advisor 調用…」）；R7 明確工具限制放行→AI-1 放行條件與 AI-4 回歸測試；R8 L219「官方上限（8 次）」改「預設上限（8 次）」（N4 證據等級與限制保留）。同步 HC7（AI-6 六個語義位置及必要相鄰文字）、存在性 AC、AI-4 測試清單。驗收處置：鏈 1、多 hook 並存記 PASS（N=1）；鏈 2、鏈 3、鏈 4 記 UNVERIFIED；不再等待，亦不設計第三次鏈 4 場景，後續場景須限制運算量。4a／4b 之自述 advisor 紀錄保留為失敗證據（scratch ledger 不刪改），不計為真實 Advisor 諮詢。

**Revision (Closeout)**: 2026-10-13 Akira 收束裁決：接受已實作控制面之限定交付並收束；不再追加實作修正、測試場景或 Cross-Review；明確豁免額外 Codex PASS 要求（Codex 真實狀態仍為 REPORT_FIX ×3 與 LIMIT，**不記為 Codex PASS**）；鏈 2／3／4 保持 UNVERIFIED；AI-7 記為「部分完成、其餘由 Akira 接受未驗收束」；移交 `REVIEW`，`CLOSED` 由既有終審角色完成。

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T04`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261013-t04-plan.md`（Rev B.1，`.tmp` 非長期載體，下列內容使本票獨立成立）；Parent artifact status＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`；plan-local item＝`T04`；Parent read timestamp＝2026-10-13 17:40（+08:00）。輔助證據（本地，非長期）：`.tmp/ticket-author/20261013-t04-diag.md`、`20261013-t04-probe/run_20261013_144805/`（多 hook／cwd／transcript probe，N=1）、`20261013-t04-probe/run_adv_*/`（headless Advisor probe，N=1）。

**Selected item 原文（T04 goal，摘要不改義）**：Akira 指定的 active、非終態、非 BLOCKED ticket，主 agent 在**工作途中**遇到失敗或不確定時，依現有指令主動走 Router；Stop hook 只攔不合格停止，把它帶回**同一條鏈**。鏈＝已授權直接做→本地取證→Advisor→必要時 PPSS→修復驗證續作→真人工或同 incident 三輪未解才 `HUMAN_REQUIRED`。通知屬 T05，不納入。

**相關約束（取自 parent）**：①**工具預算以同一 incident 計**（非每輪）：Advisor 初判 ≤1、PPSS 定向研究 ≤1、research 後 Advisor reconcile ≤1，全含於最多三輪內；換策略／phase／模型／改名不重置；helper 允許的額外調用不等於本票授權；**不新增機械預算控制**，預算只寫入既有模型契約並顯示於 hook reason。②Advisor 建議與研究結果不構成新增授權；無新證據的相同命令／prompt／研究問題／patch 不重播。③第三輪 fail 後 executor **直接**升報 `HUMAN_REQUIRED(EXHAUSTED)`，不提出或執行第四輪。④不新增 agent／helper／state／SubagentStop／PreToolUse／機械預算控制／通知；不改 BLOCKED、Hard Constraint、permissions、使用者停止意圖。⑤互動式完整 Router 才是驗收邊界；**不宣稱 headless 全鏈支援**（鏈 4 可 headless，不代表完整 Router 可達）。⑥hook 不裁定 QA PASS、產品決策、授權或「可執行」；checkbox／關鍵詞／receipt／commit／mtime 只作線索。

**已裁決補充（Akira 2026-10-13 開票指令註記 1–11；與 parent 衝突時以本節為準）**：
- **N1（途中進 Router）**：鏈 2、3 驗收「主 agent 工作途中**主動**進入 Router」，不只驗收被 Stop hook 擋回後的補救；一次成功不寫成普遍可靠。
- **N2（EXHAUSTED 後停損語義）**：EXHAUSTED 後禁止同 incident 的修復、研究、諮詢、重試與新 round；transcript 檢查看**實際操作語義（含 Bash）**，不只看 Edit／Write 名稱；升報與必要唯讀證據整理不算續作；不誤擋其他未耗盡的 incident。
- **N3（executor 第四輪被拒）**：executor 的第四輪 `round start` 被 helper 拒絕（exit 5，因該 incident 已用滿 3 輪）＝停損拒絕：**不重試，直接升報 EXHAUSTED**。
- **N4（外審與官方事實）**：外審記 EXECUTED：`rsch-20261013-ad59747d17`（6a，$0.005，code.claude.com 官方頁，snippet 級，查核日 2026-10-13），**不重送**。Stop 預設連續續行上限為 8 次，每次 Claude 工具呼叫重置連續計數；使用者中斷不觸發 Stop；上限可由 `CLAUDE_CODE_STOP_HOOK_BLOCK_CAP` 調整〔relayed：上游審查方查官方文件，本機未獨立驗證〕。本機覆寫查核：專案 `settings.json`、`settings.local.json`、使用者層 `~/.claude/settings.json` 皆無此變數，本 Bash shell 環境未設（Desktop app 程序環境未驗）。另承載：transcript 非同步可能落後、多 hook 並行序不定、阻擋輸出為 exit 2 或 JSON `decision:block` 二擇一；timeout 預設值為 SUPPORTED；`prompt_id` 僅本地 probe 支持（N=1）。**不寫成全項確認。**
- **N5（進展向量四分量）**：NO_PROGRESS 高水位向量為：已結案 incident 數、已結束輪數、已勾 Action Item／AC 數、相異來源 `sha256` 數。**移除**「相異 gate_point 的 exit 0 非 late receipt 數」。incident／pending 數、consult／strategy-scan 自身不算進展。
- **N6（O2 receipt 視窗）**：`status --json` 只輸出最近 5 筆 receipt（`gate_ledger.py` 現值已確認）；視窗滿 5 筆且該 gate_point 不在其中→O2 標 UNKNOWN，不寫 `missed-check`。列為已知限制。
- **N7（hook 寫入失敗）**：hook 自身寫入（`blocks`／`missed-check`／`human NO_PROGRESS`）任何非 0：以標記檔**每 prompt_id 至多 block 一次**，之後放行；**刪除 hook 端 exit 5 重試邏輯**（取代 parent「明列沿用 4」之 exit 5 一次重試）。此條與 N3（executor 的 `round start`）分開陳述。
- **N8（K 與註冊）**：K 暫定 6，維持 K ≤ 7；K 是本 guard 的 NO_PROGRESS 預算，不等同平台連續續行計數（該計數涵蓋所有 Stop hook），不宣稱可控制其他 hook 的阻擋。Stop 註冊項**顯式設 `timeout`**；payload 缺 `prompt_id`、guard 未預期例外、逾內部時限一律放行。
- **N9（ssot-governance 文字）**：改寫 L185「適用」句時寫明「本 Gate 適用範圍排除 `BLOCKED`」，**不把 BLOCKED 定義為 lifecycle 終態**；L175「MUST NOT 宣稱已有 hook 阻擋」改為「不得宣稱完整 Gate、不得宣稱 hook 涵蓋途中違規」；L175／L239 狀態句**直述該提交帶入的現況，不帶條件語**。
- **N10（用量顯示）**：reason 用量取自 `status` 既有欄位，不改 helper；Advisor「x／2」是用量摘要，仍受「初判 ≤1＋reconcile ≤1」角色限制。
- **N11（鏈 3 須新跑真實 PPSS）**：既有三次實跑皆不符完整鏈（`f471f5f0a2` 為 `evidence_ok=false` 且無修復消費；`9506410da5` 與 `ad59747d17` 為審查查詢，無 ledger ppss 記錄）。**不得重送已 EXECUTED 的研究，不得把已有答案當成新的外部事實缺口**；題目選尚未解決、確實影響 scratch 修復的窄事實；`filters.domains` 限官方網域。候選：command hook 逾時後視為放行或阻擋；Stop 事件 timeout 預設值。

**開票端與 parent 的差異（精準列出，未擴大方案）**：
- **X1（高水位載體）**：parent 以 `blocks --note` 記高水位，但 `status --json` **不輸出 block note**（`summarize` 無此欄）。本票以 hook 既有標記檔機制（N7，`tmp/.stop_guard/`）承載「本 prompt 序列計數＋四分量高水位」；`blocks` 事件仍照寫（供 ledger 計數與 `human NO_PROGRESS` 前置 `blocks≥1`）。不讀 ledger 原始 jsonl、不改 helper。
- **X2（O2 closeout 腳）**：parent O2 含「closeout＝票檔搬入 deliver/」，但 parent 判定表第 1 列在位置非 `loc:pending` 即放行，該腳不可達。本票 O2 僅保留 `qa`（`step0_*.txt`）、`commit`（`[<T>]` 前綴新 commit）、`sv`（新 `.tmp/sv_report_*.md`）三腳，closeout 腳列已知限制。
- **X3（settings.json EOL）**：`.claude/settings.json` 工作樹為 CRLF、index 為 LF（`git ls-files --eol`：`i/lf w/crlf`，無 attr）；`ssot-governance.md` 為 `eol=lf`。編輯 MUST 保持各檔現行行尾，diff 不得出現整檔行尾改寫。
- **X4（L217，Rev A 取代原「本票不改」）**：`ssot-governance.md` L217 為四軸覆蓋矩陣，現含「目前皆未實作」，與本票帶入的 Stop guard 與已接入的入口 `check` 不符；納入 AI-6 **第五處**，移除該字面，改為分軸直述該提交帶入的實測現況（不混寫軸、不把 done 寫成未接線或有後盾）。
- **X5（Registry 查核，開票前歷史證據）**：全冊機械取號（registry＋ARCHIVE＋deliver／Pending／void）`ADV-GATE-*` 最大號為 03；無 `ADV-GATE-04`、無 `gate_stop_guard` 等價票；`ADV-GATE-01／02／03` 皆 `CLOSED`。

**證據分級**：①**CONFIRMED（2026-10-13 17:39 live 直讀）**：HEAD `8a7c18e9`、工作樹 clean、第二 worktree `~/.codex/worktrees/4ba7`（detached `b6f14740`）不在本票範圍；`.claude/hooks/gate_stop_guard.py`、`.claude/hooks/tests/gate_stop_guard_test.py` 不存在；`.claude/settings.json` `hooks.Stop` 現為 2 組（第 1 組 `stop.sh`＋`telegram_stop.sh`、第 2 組 `registry_review_stop_guard.py`），無 `timeout` 鍵；`ssot-governance.md` 預定錨點命中數見 AI-0；`gate_ledger.py` `summarize` 之 `receipts` 取 `[-5:]`（L514）；`tmp/` 與 `.tmp/` 受 `.gitignore`（L52／L55）；四個目標路徑 `git check-ignore` 均未命中；`active.json` 現為已 `CLOSED` 之 `ADV-GATE-03`（stale 實例）。②**CONFIRMED（本地 probe，N=1，CLI 2.1.296）**：同一 Stop 多 hook 並行、多個 block 的 reason 皆送達、`stop_hook_active` 於續行後為 true、hook cwd 跟隨 session cwd（`cd sub` 後為 `sub`）、Stop 當下 `last_assistant_message` 與本回合 `tool_use` 已在 transcript、user 行 `promptId`＝payload `prompt_id`；headless `claude -p` 工具清單與回覆皆無 `advisor`，互動式本地 transcript 有 `server_tool_use name=advisor`＋`advisor_tool_result`（加密）。③**SUPPORTED**：Stop 預設 timeout 600s、transcript 可能落後、Stop 預設續行上限 8 與 `CLAUDE_CODE_STOP_HOOK_BLOCK_CAP`（relayed，N4）。④**HYPOTHESIS**：「工作途中走 Router」是否由 `ssot-governance.md` 文字修改實際引發——由鏈 2、3 弱證據（n≥1）檢驗。⑤**UNKNOWN**：Stop 當下 transcript 是否已含 advisor 標記（hook 不依賴）；headless `research_submit` 可達性（init 時 MCP 為 pending）；連續 block 上限之本機實測；討論性提問辨識的誤判率。

**External-review decision**: EXECUTED——`rsch-20261013-ad59747d17`（6a，$0.005，官方頁 snippet 級）。此為本票既定範圍之 6a 外審處置；同題同範圍不重觸發，不另懸置 WAIVE 決策。EXECUTED 不代表全項確認或品質驗收 PASS；證據限制依 N4 保留。不重送原研究，亦不把此處置擴張成對新增範圍的全面豁免。

**簽發日期**: 2026-10-13
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: normal  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: ADV-GATE-01（`CLOSED`，Gate 契約節）、ADV-GATE-02（`CLOSED`，`gate_ledger.py` 介面）、ADV-GATE-03（`CLOSED`，入口 `check` 與 Boot `status` 接線）
- `evidence_dependency`: ADV-GATE-01、ADV-GATE-02、ADV-GATE-03——僅作字面判準與介面來源，本票錨點皆 live 直查
- `coordination_dependency`: 無（T05 通知對齊依賴本票完成；本票與其他編輯 `.claude/settings.json`／`.claude/rules/ssot-governance.md` 之票須串行，`concurrency.md` §2）

## Verify Config
TICKET_ID=ADV-GATE-04
CREATED=2026-10-13
PRESET=feature
ALLOWED=.claude/hooks/gate_stop_guard.py,.claude/hooks/tests/gate_stop_guard_test.py,.claude/settings.json,.claude/rules/ssot-governance.md
FORBIDDEN=.claude/scripts/**,.claude/hooks/stop.sh,.claude/hooks/telegram_stop.sh,.claude/hooks/notify.sh,.claude/hooks/telegram_ask.sh,.claude/hooks/session_start.sh,.claude/hooks/snapshot_cueshot_memory.py,.claude/hooks/registry_review_stop_guard.py,.claude/hooks/tests/registry_review_stop_guard_test.sh,.claude/hooks/tests/telegram_stop_test.sh,.claude/settings.local.json,.claude/agents/**,.claude/skills/**,.claude/CLAUDE.md,.claude/rules/concurrency.md,.claude/rules/critical-files.md,.claude/rules/cross-review.md,.claude/rules/decommission.md,.claude/rules/engineering-discipline.md,.claude/rules/kairos-lessons.md,.claude/rules/mcp-service-safety.md,.claude/rules/phase0-lessons.md,.claude/rules/state-machine.md,.claude/rules/ticket_pattern.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,tools/**,app/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json
MAX_LINES=N/A
DIFF_REF=8a7c18e9
ANCHOR_PROBED=2026-10-13 17:39
BASE_COMMIT=8a7c18e9
TARGET_BLOB=.claude/settings.json:85c1bebd;.claude/rules/ssot-governance.md:3a9b6511
INTRODUCED_CAPABILITY=feature:gate-stop-guard
DEPENDENCY_DELTA=NONE

> PRESET=feature（`.py` 新檔＋unittest＋兩個既有治理檔小改；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。`MAX_LINES=N/A`：含 fail-closed／停損／升報類不變量，失敗形狀為審查發現式，不可先驗估算（TPL v3.5 R3）；MAX_LINES 非 execution gate。`TARGET_BLOB`：僅列**既有**標的（`git hash-object` 取前 8 碼）；兩個新檔無既有 blob，AI-0 逐檔確認不存在，TARGET_BLOB 為資訊性診斷值、不作 gate（TPL v3.11）。`INTRODUCED_CAPABILITY` 為顯式 canonical token。`Traycer Workflow: N/A`：純 `.claude/` 治理工具，不觸 `app/`；審查走 SV＋Cross-Review。**DEFERRED-TO-DOCSYNC（不開票）**：FORBIDDEN 命中 `.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` → `/qa` Step 4 SKIP；Dashboard 同步、Done 標記、票檔 Pending→deliver 搬移與 checkbox 勾選留待下一輪 `/compact` 標準收尾，**不另開票**。`DIFF_REF` 依 `ticket_pattern.md` 票據模板規範取字面佔位，開工時回填。暫存場景（鏈 1–4）一律置於 `tmp/ADV-GATE-04/`（`.gitignore` L52 忽略，不入 ALLOWED），符合 `concurrency.md` §1 每票 scratch 隔離。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: 下列各檔（`ANCHOR_PROBED=2026-10-13 17:39`；基線漂移依模板三分流處置）
- **動作**: 逐字確認現值於檔內命中數與票面相符；任一項不符 → 依三分流分類記錄，禁推定等價位置。
  1. `.claude/rules/ssot-governance.md`：`宣稱已有 hook 阻擋`＝1（L175）；`Stop guard 與升報通知對齊尚未實作（待後繼票）`＝2（L175、L239）；`於回合結束、請示或交棒前`＝1（L185）；`registry lifecycle ∉ {`REVIEW`,`CLOSED`,`VOID`}`＝1（L185）；`PPSS 預設每輪 1 次`＝1（L193）；`Advisor 由主 agent 承接，hook 不呼叫`＝1（L211）；`目前皆未實作`＝1（L217，**AI-6 第五處**）；Rev B 追加：`「已諮詢」以記錄為證`＝1（L211，**AI-6 第六處**）、`官方上限（8 次）`＝1（L219，第五處之相鄰文字，改「預設上限（8 次）」）。另確認既有入口接線現值（供 L217 目標句依據）：`.claude/skills/uc/SKILL.md` 含 `gate_ledger.py check done`（現於 L49）、`qa/SKILL.md` 含 `check qa`／`check commit`（現於 L17／L191）、`sv/SKILL.md` 含 `check sv`／`check closeout`（現於 L14／L212）、`.claude/CLAUDE.md` Boot 段含 `gate_ledger.py status`（現於 L9）；各項以開工時現值為準，不以行號推定。
  2. `.claude/settings.json`：`hooks.Stop` 恰 2 組（`[2,1]` 個 handler）、無 `timeout` 鍵於 Stop 區、無 `CLAUDE_CODE_STOP_HOOK_BLOCK_CAP`；`git ls-files --eol` 為 `i/lf w/crlf`。
  3. `.claude/scripts/gate_ledger.py`：`"receipts": st["receipts"][-5:]`＝1；`status`、`blocks`、`missed-check`、`human`、`round`、`consult`、`ppss` 子命令與 `--ticket`／`--json` 選項如 docstring；`MAX_ROUNDS=3`；`round start` 於已用滿 3 輪時拒絕（exit 5）。**唯讀，不改。**
  4. `.claude/hooks/gate_stop_guard.py`、`.claude/hooks/tests/gate_stop_guard_test.py` 不存在。
  5. `registry_list(project=SmartCameraApp1)`：本票 `ADV-GATE-04` 已登記且 ID／file（`Pending/ADV-GATE-04_stop-router.md`）／topic（`Advisor`）相符、無重複票（無第二筆同 ID 或等價 `gate_stop_guard` 票）；既有依賴 `ADV-GATE-01／02／03` 為 `CLOSED`。
  6. `git check-ignore -v` 對四個 ALLOWED 路徑零命中。
  7. 重新取得 `git hash-object` 與 `git rev-parse --short HEAD`，記錄漂移（資訊性）。
- [x] 完成 〔證據：2026-10-13 18:09 live 核對，HEAD `8a7c18e9` 未漂移；ssot-governance 七錨點命中數與票面一致（1／2／1／1／1／1／1）；settings.json Stop `[2,1]`、無 timeout、無 CAP 變數、`i/lf w/crlf`；`gate_ledger.py` L514 `[-5:]`、`MAX_ROUNDS=3`、第 4 輪拒絕字串存在；兩新檔不存在；四路徑 `git check-ignore` 零命中；入口接線 uc:49／qa:17,191／sv:14,212／CLAUDE.md:9 現值存在；registry `ADV-GATE-04` ID／file／topic 相符，依賴 01–03 CLOSED。無 ANCHOR 漂移。〕

### AI-1: `gate_stop_guard.py`——外框、定位與放行條件
- **檔案**: `.claude/hooks/gate_stop_guard.py`（新增，stdlib only）
- **現值**: 檔案不存在
- **目標值**: command Stop hook，stdin 讀 JSON payload，阻擋時 stdout 輸出 `{"decision":"block","reason":…}` 並 exit 0，放行時無輸出 exit 0。
  - 外框：整體包在 try/except；**payload 缺 `prompt_id`／`session_id`／`cwd`、任何未預期例外、逾內部時限（20 秒）一律放行**。單次 `git`／`status` 子程序各有逾時，不使 hook 逼近註冊 timeout（AI-5）。
  - 定位：以 `payload.cwd` 執行 `git -C <cwd> rev-parse --git-common-dir`，root＝其 resolve 後的 parent；**不依 hook 自身 cwd**。
  - 放行條件（先中先走）：無 `root/tmp/verify/active.json` 或無 `ticket_id`；Registry（`plans/cso_briefing/project/TICKET_REGISTRY.json` 直讀，hook 無 MCP）查無／讀失敗；lifecycle ∈ {`REVIEW`,`CLOSED`,`VOID`}；lifecycle＝`BLOCKED`（**不要求續作、不解除、不恢復編排；不把 BLOCKED 當終態，僅排除於本 Gate 適用範圍**）；`status` 的 `ticket_location` ≠ `loc:pending` 或與 Registry 矛盾。
  - 當回合 user 行（transcript 中 `promptId == payload.prompt_id` 的 user 訊息）判定：**取消**＝整句錨定比對短取消詞（`^(請)?(停止|取消|中止|先停|先到這|算了|stop|cancel|abort)(吧|一下)?$`，≤12 字；`/` 起頭、含引號、含否定詞〔不要、別、don't、no〕或較長句子皆不是取消）→放行；**討論／提問**（小常數清單：句尾 `？`／`?`，或以 為什麼／請說明／解釋／如何 起頭）→放行，錯判方向為少擋。**明確當回合工具限制**（Rev B R7，最小窄匹配：中文 不要／請勿／禁止／不得／不可／不／別＋（再）使用／用／呼叫＋（任何／所有）工具；英文 do not／don't／dont／without＋（use／using）＋（any）tool(s)；`no tools`）→放行，保留 incident，不要求以工具續作，不解除 BLOCKED 或任何 permissions；與討論／提問辨識同一原則：小常數、錯判方向為少擋，不建泛化意圖分類器。取不到 user 行→UNKNOWN，不視為取消、討論或工具限制，繼續後續判定。放行時不寫任何 ledger 事件，未解狀態原樣保留。
  - hook **不含**任何 `notify`／Telegram 呼叫、不 import `gate_ledger` 內部函式（僅以 `sys.executable` 子程序呼叫其公開 CLI：`status --json --ticket <T>`、`blocks`、`missed-check`、`human`）。
- [x] 完成 〔證據：`.claude/hooks/gate_stop_guard.py` 新增；放行條件／外框／定位／取消、討論與明確工具限制（Rev B R7）由 `gate_stop_guard_test.py` AllowConditions 17 例（含子目錄 cwd、payload 缺欄位、garbage stdin、內部時限）驗證通過；工作樹未 commit〕

### AI-2: `gate_stop_guard.py`——義務判定與 Router reason
- **檔案**: `.claude/hooks/gate_stop_guard.py`
- **現值**: （AI-1 後）僅有外框與放行條件
- **目標值**: 依 `status --json` 判定，先中先走。**pending 前置由 helper 於寫入時驗證（`gate_ledger.py human` 已承載 category 前置檢查：EXHAUSTED 三輪 fail 與 `rounds_3`、驗證先行類 local＋advisor consult、`NO_AUTHORIZED_STRATEGY` 之 rounds<3／無進行中輪／strategy-scan、`NO_PROGRESS` 之 blocks≥1、立即類 category 與非空 basis）；hook 信任公開 `status.pending_human`，不重驗、不另存一份 category 規則**（取代 parent D8；hook 不偵測繞過 helper 直接寫 jsonl 的偽造，列已知限制）：
  1. **pending 範圍依實際阻塞對象**：無 `incident` 的合法 pending＝整票→放行；帶 `incident`＝局部（預設，不依 category 一律放行）→僅該 incident 視為已升報，其他 open incident 與 O2 仍須先完成；無其餘義務才放行；局部 pending 下未勾項線索（見第 4 點）改單次核對獨立工作，不直接忽略。
  2. **open incident（無 pending）**：`rounds_failed<3`→block，reason 帶 Router 下一步（R1 已授權直接做→R2 本地取證→R3 仍不能判定下一個安全行動才呼叫 Advisor→R4 缺必要外部事實才用 PPSS→R5 research 後 reconcile→R6 以新策略 `round start`、修復、局部驗證）；`rounds_failed==3`→block，reason **僅允許升報**（`gate_ledger.py human --category EXHAUSTED --basis rounds_3`），**不提出或執行第四輪、不再修復／研究／諮詢／重試同 incident**；其他未耗盡的 incident 不受此句拘束。
  3. **O2 結構性漏檢**（僅在回合起點可得且 receipt 視窗未飽和時）：本回合出現推進證據——`qa`＝`tmp/<T>/evidence/step0_*.txt` 新增、`commit`＝本回合起點後帶 `[<T>]` 前綴的新 commit、`sv`＝新 `.tmp/sv_report_*.md`——而 `status.receipts` 無時間早於該證據的同 gate_point receipt→block 並寫 `missed-check`（同 prompt_id×gate_point 只寫一次），reason 要求先核對當前狀態再對下一動作 `check`；補跑 `late` receipt 即解除。**回合起點取不到→UNKNOWN；receipts 滿 5 筆且該 gate_point 不在其中→UNKNOWN（已知限制）；兩者皆不寫 `missed-check`、不 block、不補 receipt。** closeout 腳不實作（見 X2）。
  4. **單次核對**（每 prompt_id 至多一次，標記檔記錄；只要求「核對並如實處理或說明」，**hook 不裁定 QA PASS、可執行性或授權**）：Pending 票檔中 `- [ ]` 行（Action Items 區與 Acceptance Criteria 區）且該行不含人工／豁免／延後標記常數（`[實機]`、`DEFERRED-TO-DOCSYNC`、`N_A`、`豁免`、`Akira`、`Traycer`、`人工`）者；或 ledger 有 `qa` receipt 而其後無 `sv` receipt。
  5. 其餘→放行。
  - **reason 一律附**：「不得繞過 BLOCKED、Hard Constraint 或 permissions；停止某個未授權操作不等於停止其他合法路徑；Advisor／研究結果不構成新增授權」。**用量顯示（僅顯示，取自 `status` 既有欄位）**：該 incident 已用 Advisor 次數（初判 ≤1＋reconcile ≤1，角色限制不因次數改變）、PPSS 次數（`ppss` 清單中屬該 incident 者，上限 1）、輪數 x／3。
- [x] 完成 〔證據：同檔；pending 信任 `status.pending_human`（無 category 重驗）；OpenIncidents 6 例＋SingleCheck 5 例＋MissedCheck 6 例通過；reason 含 Router R1–R6、用量顯示、邊界句；rounds_failed==3 僅允許升報且不影響其他 incident〕

### AI-3: `gate_stop_guard.py`——NO_PROGRESS 高水位與 hook 寫入失敗
- **檔案**: `.claude/hooks/gate_stop_guard.py`
- **現值**: （AI-2 後）尚無序列計數與寫入失敗處置
- **目標值**:
  - **本 prompt 序列**（標記檔 `root/tmp/.stop_guard/<session_id>_<prompt_id>.seq`，JSON：`count`、四分量高水位）：每次因持續義務（AI-2 第 2–3 點：open incident、O2；及第 1 點局部 pending 下的其他 open incident／O2）block，先呼叫 `blocks --note <簡短>`；比較當下向量與高水位——**四分量**＝已結案 incident 數、已結束輪數（`rounds_started` 減去 `round_open`）、票檔已勾 Action Item／AC 數、`status.ppss[].reads[].sha256` 相異數；任一分量**嚴格大於**高水位→進展，count 歸零並更新高水位，否則 count＋1；`prompt_id` 改變即重置。incident／pending 數、consult／strategy-scan 自身、重複 `check`／`finding`、重複來源（相同 sha256）、無關 commit 皆不算進展。count 達 **K＝6**（常數，K ≤ 7；本 guard 自身預算，不宣稱控制其他 hook 的阻擋）→呼叫 `human --category NO_PROGRESS --basis <非空>` 並放行。incident 輪數只存在於 ledger，不被序列重置。
  - **hook 寫入失敗（N7）**：`blocks`／`missed-check`／`human NO_PROGRESS` 任何非 0（含 5、6）或 `status` 讀取失敗（exit 6／無法解析）：**不重試**；以標記檔 `<session_id>_<prompt_id>.ledgerfail` 每 prompt_id **至多 block 一次**（reason 貼原始 stderr、說明 ledger 狀態未知，**不要求補造任何事件**；若底層義務存在則繼續該 Router 步驟），標記存在或標記寫入亦失敗→放行。`status` exit 3／4（無票身分／位置非 pending）→放行。
  - 此處為 **hook 寫入**語義；executor 自己的 `round start` 被拒（exit 5，N3）不屬此處，見 AI-7 鏈 4。
- [x] 完成 〔證據：同檔；四分量高水位（標記檔 `.seq`）與 K=6、`blocks`／`missed-check`／`human NO_PROGRESS` 寫入失敗（exit 5／6／status 損毀）一次 block 後放行不重試；ProgressAndFailures 9 例通過〕

### AI-4: 決定性測試 `gate_stop_guard_test.py`
- **檔案**: `.claude/hooks/tests/gate_stop_guard_test.py`（新增，unittest，stdlib）
- **現值**: 檔案不存在
- **目標值**: 於暫存 git repo（`tempfile`，不觸本 repo）建立假 ticket、`active.json`、Registry 檔，**複製真實 `gate_ledger.py`** 入暫存 repo，以 stdin→stdout 驅動 hook。覆蓋：每個放行條件（含 `BLOCKED`、終態、位置矛盾、取消／討論／明確工具限制（含英文、保留 incident 且無 ledger 新事件、不覆蓋 pending 狀態；相近但非限制的提示仍 block）、payload 缺欄位、未預期例外、逾時）；pending 整票 vs 局部（hook 信任 `status.pending_human`，不測 category 重驗）、局部 pending 下其他 open incident 仍 block；open incident `rounds_failed<3` 與 `==3` 之 reason 分流（後者僅允許升報、且不影響其他未耗盡 incident）；O2 三腳＋回合起點 UNKNOWN＋receipt 視窗飽和 UNKNOWN（皆不寫 `missed-check`）；單次核對標記；四分量高水位（重複 check／finding／相同 sha256／consult／scan／incident 數增加皆不歸零，四分量各自嚴格增加才歸零，prompt_id 改變重置，K＝6 寫 `human NO_PROGRESS`）；hook 寫入失敗（`blocks` 等非 0 含 5、6）→標記檔一次 block 後放行、不重試、不寫補造事件；子目錄 cwd 仍定位到 root；reason 含邊界句與用量顯示；hook 原始碼不含 `notify`／`telegram`。不得以 mock 冒充真實 `gate_ledger.py` 之 exit 行為。
- [x] 完成 〔證據（Rev B 更新）：`.claude/hooks/tests/gate_stop_guard_test.py` 新增，全套 42 例 OK（約 46 秒；暫存 git repo＋複製真實 `gate_ledger.py`，hook 以 subprocess stdin→stdout 驅動）〕

### AI-5: `.claude/settings.json`——Stop 註冊
- **檔案**: `.claude/settings.json`
- **現值**: `hooks.Stop` 為 2 組（`[stop.sh, telegram_stop.sh]`、`[registry_review_stop_guard.py]`），無 `timeout`
- **目標值**: 於 `hooks.Stop` 陣列**追加一組**：`{"hooks":[{"type":"command","command":"python \"$CLAUDE_PROJECT_DIR/.claude/hooks/gate_stop_guard.py\"","timeout":30}]}`（`timeout` 秒，須大於 AI-1 內部時限 20 秒）。既有兩組位元組不變；保持工作樹現行 CRLF 行尾（X3），`git diff` 不得顯示整檔行尾改寫。
- [x] 完成 〔證據：`.claude/settings.json` 追加第 3 組 Stop（`timeout`:30）；`git diff --stat` 9 insertions／0 deletions，無 CR 行尾被改寫；既有兩組位元組不變〕

### AI-6: `ssot-governance.md`——Router 承載與狀態字面
- **檔案**: `.claude/rules/ssot-governance.md`（`eol=lf`）
- **現值／目標值**（僅下列六個語義位置及必要相鄰文字；不要求恰好六個 diff hunk；逐字現值見 AI-0）:
  1. **L185「適用」句**：現值含 `於回合結束、請示或交棒前`。目標：擴為「工作途中遇到失敗或不確定時即依本節順序處理，不待回合結束；回合結束、請示、交棒前的檢查維持」，並明寫「**本 Gate 適用範圍排除 `BLOCKED`**」，**不得**把 `BLOCKED` 寫入或定義為 lifecycle 終態集合（現值 `∉ {REVIEW,CLOSED,VOID}` 集合不變）；其餘句（不攔 `ticket-run`／`/compact`／`/clear`、恢復義務）原樣保留。
  2. **L193 與 Advisor／PPSS 節**：以**同一 incident** 預算取代 L193 `PPSS 預設每輪 1 次；第 2 次須附新查詢策略與理由，且不開新輪。`——寫明：Advisor 初判 ≤1、PPSS 定向研究 ≤1、research 後 Advisor reconcile ≤1，全含於最多三輪內；換策略／phase／模型／改名不重置；不同 incident 各自計；Advisor 建議與研究結果不構成新增授權；helper 允許的額外調用不等於本票授權；無新證據的相同命令／prompt／研究問題／patch 不重播；Advisor「x／2」為用量摘要、仍受角色限制。（`Advisor 由主 agent 承接，hook 不呼叫` 一句保留。）
  3. **L175 契約狀態句**：現值含 `本 Gate 專用 Stop guard 與升報通知對齊尚未實作（待後繼票）`、`agent **MUST NOT** 宣稱已有 hook 阻擋，**MUST NOT** 宣稱完整 Gate`。目標：直述「本 Gate 專用 Stop guard（`.claude/hooks/gate_stop_guard.py`）已註冊於 `.claude/settings.json` 的 Stop；升報通知對齊尚未實作」，並改為「agent **MUST NOT** 宣稱完整 Gate，**MUST NOT** 宣稱 hook 涵蓋途中違規」；**不帶條件語**，Advisor 採納之驗證限制（內容加密、不可驗）照實保留。
  4. **L239 實作狀態句**：同上直述該提交帶入的現況，不帶條件語，不宣稱完整 Gate。
  5. **L217 覆蓋矩陣狀態句（Rev A 新增，R1）**：現值含 `目前皆未實作`。目標：保留「覆蓋矩陣四軸分開記錄」句首，將「目前皆未實作」改為**分軸直述**該提交帶入的實測現況，大意為——**呼叫路徑（入口 `check`，模型契約）已接入**：qa／commit／sv／closeout／done，`resume` 讀 `status`（ADV-GATE-03；`done` 依據 `.claude/skills/uc/SKILL.md` 的 `check done` 呼叫，開工時以現值確認）；**機械後盾**：pause（回合結束停止，本票 `gate_stop_guard.py`）、qa／commit／sv 的漏檢事後偵測（受 receipt 視窗限制，僅回合起點可得時）；**無機械後盾**：done、closeout（O2 無此腳，見 X2）以及其餘決策點（以收尾實錄逐列實測為準）。**不得把 done 寫成未接線**（與 L175 現有字面互斥），**也不得寫成 done 有後盾**；不得把 helper 支援的 gate_point 當成已有 production 接線。不帶條件語。
  6. **L211 Advisor 句（Rev B 新增，R6）**：現值 `Advisor 由主 agent 承接，hook 不呼叫；「已諮詢」以記錄為證，內容加密不可驗。`。目標（直述、不帶條件語，不另加行）：「Advisor 由主 agent 承接，hook 不呼叫；`consult --kind advisor` 只能記錄實際原生 Advisor 調用，主 agent 自判不得以 advisor kind 記錄；回答內容加密不可驗。Advisor 不可達時：如實說明工具／環境缺口與原擬升報類別，不偽造 consult、不為滿足前置硬湊輪次；此時驗證先行類升報無法寫入 ledger，停止由 `NO_PROGRESS` 預算放行。」不要求 helper 新增 transcript 驗證、不新增 category。
  - **相鄰文字（R8）**：L219 `hook 連續 block 之官方上限（8 次）不是解題預算。` 改「預設上限（8 次）」；N4 既有證據等級（relayed，未獨立驗證）與限制保留。
  - 其餘 Gate 節文字、Transition Table 不動。
- [x] 完成 〔證據（Rev B 更新）：`ssot-governance.md` 六個語義位置（L175／L185／L193／L211／L217／L239）＋L219 相鄰字面，`git diff --stat` 7 行替換（7+／7−），分布於 5 個 hunks；已無「待後繼票」「目前皆未實作」「每輪 1 次」「以記錄為證」「官方上限」字面；L211 為實際原生調用才可記 advisor kind；L217 分軸直述、done 既非未接線亦非有後盾〕

### AI-7: 四條主驗收執行與收尾證據
- **檔案**: 票檔「收尾實錄」（`deliver/` 耐久載體）；暫存場景 `tmp/ADV-GATE-04/<chain>/`（不入版控）
- **現值**: 無
- **目標值**: AI-1～AI-6 完成後，於暫存 git repo（含複製之 `gate_ledger.py`、本票 `gate_stop_guard.py`、註冊 Stop 的暫存 `.claude/settings.json`，並**複製 `ssot-governance.md`（修改後）與 `.claude/CLAUDE.md` 至暫存專案**使 Router 承載文字進入該 session 脈絡）執行下列四條；mock／fixture **不得冒充**真實 Advisor 或 PPSS；Telegram token 清空；不寫真 Registry；各鏈記：觸發→入口／呼叫者→返回→executor 消費→驗證→續作或升級。鏈 2、3 之提示**不得明示使用 advisor／research_submit 或其順序**，僅給場景與驗收目標（驗「途中主動」，N1）。
  1. **鏈 1 已授權已知問題自修**：某檢查失敗且修法在暫存 ALLOWED 內並已知→主 agent R1 直接修→驗證通過→`round end pass`→續作。headless 或互動皆可。
  2. **鏈 2 真實 Advisor（僅互動式 Desktop session）**：本地取證後仍無法判定下一個安全修法（如兩個可行落點，其一觸及暫存 FORBIDDEN）→**主 agent 於工作途中、於任何 Stop block 之前**呼叫原生 `advisor`→採納並在 scope 內修→驗證→續作；`consult --kind advisor` 有記錄。
  3. **鏈 3 外部證據回流（互動式；需 Akira 於執行時授權一次真實 PPSS）**：題目取 N11 候選（或同等尚未解決、影響暫存修復的窄事實），**先核對 `f471f5f0a2`／`9506410da5`／`ad59747d17` 三個既有 artifact 的查詢與結論，確認該題未被回答**；`filters.domains` 限官方網域（如 `code.claude.com`、`docs.anthropic.com`）；主 agent 於途中主動 `research_submit`→`ppss record`→讀來源→`ppss read` 使 `evidence_ok=true`→（至多一次 Advisor reconcile）→修→驗證→續作。不得重送已 EXECUTED 研究。
  4. **鏈 4 第三輪 fail 後直接升報（headless 或互動皆可）**：同一 incident 依序三個**不同且完成驗證**的策略皆 fail→executor 直接 `human --category EXHAUSTED --basis rounds_3` 輸出 `HUMAN_REQUIRED` 一行→hook 放行；若 executor 仍提出第四輪 `round start` 而被拒（exit 5）→**不重試，直接升報**（N3）。場景須使三輪失敗皆為合法驗證失敗；若場景因更早的合法 `HUMAN_REQUIRED`（如 `SCOPE`）而中止則不算本鏈，重設計至多 2 次後回報。
  - **停損後無續作判定（N2）**：以既有 transcript 與 ledger——自 EXHAUSTED `human` 事件時間點起，transcript 中**同 incident 相關**的 tool_use（逐筆看語義：Edit／Write／NotebookEdit，**以及 Bash 的修復、重跑測試、`round start`、`consult`、`ppss`、`research_submit`、網路請求等指令**，加上 `advisor`、`research_submit` 工具）為零；允許升報輸出、`status`／`git diff`／讀檔等必要**唯讀**證據整理；ledger 自該事件後無同 incident 的 round／ppss／consult 事件；另開並未耗盡的 incident 不受影響且不被誤擋。git diff 僅作旁證。
  - **途中主動判定（N1）**：鏈 2、3 之 transcript／hook log 顯示首個 Router 動作（`advisor`／`research_submit`）發生於第一次 Stop block 之前；若僅在被 block 後才進 Router，該鏈標「僅補救」，不得記為途中主動。一次成功不寫成普遍可靠，結論標弱證據 n。
  - **輔助（不代替上四條）**：決定性測試（AI-4）、註冊後多 hook 並存重放（新舊 guard 並行、兩個 reason 皆送達）、子目錄 cwd、取消／BLOCKED／討論放行；負向測試：刻意提出第四輪 `round start`→helper exit 5（不屬主鏈必做動作）。
  - **收尾觀察（寫入覆蓋矩陣說明，不新增 AC、計數器或 helper 欄位）**：實質阻擋次數＝真實 ledger 的 `blocks` 事件數（`blocks` 只在持續義務 block 時寫入）；線索型單次核對次數＝`tmp/.stop_guard/` 中單次核對標記檔的數量。兩者**分開記錄**；標記檔已被清除或無法計數時如實註明。
  - **覆蓋矩陣**：收尾實錄逐列填入實測現況（check 程式結果／呼叫路徑／機械後盾／未覆蓋），誠實揭露：`done` 的入口 check 已接入，但無推進證據 producer、無機械後盾；Stop 之前的中途違規不被攔截、headless 無 Advisor、O2 receipt 視窗限制與 closeout 腳缺席、預算無機械強制、討論辨識為啟發式、pending 信任 helper 寫入驗證（不偵測繞過 helper 的直接 jsonl 偽造）。**不得宣稱「完整 Gate」，不得宣稱 hook 涵蓋途中違規。**
- [ ] 完成 〔部分完成、其餘由 Akira 接受未驗收束（2026-10-13）：鏈 1 與多 hook 並存 PASS（N=1）、覆蓋矩陣已填；鏈 2／3／4 UNVERIFIED；本項不勾成完成〕

## Hard Constraints
1. 不修改 `gate_ledger.py` 與其測試；hook 僅以公開 CLI 讀寫（`status`／`blocks`／`missed-check`／`human NO_PROGRESS`），不 import 其內部、不讀 ledger 原始 jsonl。
2. hook **不裁定** QA PASS、產品決策、授權或「可執行」；checkbox／關鍵詞／receipt／commit／mtime 僅為線索，只能把停止帶回 Router。
3. hook 不含通知（`notify`／Telegram）呼叫；通知對齊屬 T05。不新增 agent、helper、daemon、MCP、資料庫、第二套 ledger、SubagentStop、PreToolUse、機械預算控制；標記檔僅限 `tmp/.stop_guard/` 既有目錄慣例（N7、X1）。
4. 放行優先：`BLOCKED`、終態、使用者取消、payload 缺欄位、未預期例外、逾時一律放行（fail-open）；不改 BLOCKED／Hard Constraint／permissions／使用者停止意圖；`AskUserQuestion` deny 維持。
5. K＝6（K ≤ 7）；Stop 註冊項顯式 `timeout`；不宣稱控制其他 hook 的阻擋或平台連續續行計數。
6. 預算為同一 incident 之模型契約；不得宣稱 hook 機械強制預算或「第三輪後不續作」（只在事後由 transcript／ledger 判定）。
7. `ssot-governance.md` 變更僅限 AI-6 六個指定語義位置及必要相鄰文字；不把 BLOCKED 定義為終態；狀態句直述現況不帶條件語；不宣稱完整 Gate。
8. `settings.json` 僅追加一組 Stop handler；既有兩組位元組不變；保持現行行尾。
9. 鏈 2、3 不得以 mock 冒充；不得重送已 EXECUTED 研究；不宣稱 headless 全鏈支援；真實 PPSS 須 Akira 於執行時授權。
10. 本票只在 `tmp/ADV-GATE-04/` 與四個 ALLOWED 檔作業；不觸第二 worktree 的受版控檔案或 Git 設定。

## Error Handling
- AI-0 任一錨點不符 → 依模板三分流分類記錄；觸及 `app/` 之 `BLOCKED_UNATTRIBUTED_STATE` 才停止（本票無 `app/` 標的）。
- `git check-ignore -v <ALLOWED 標的>` 命中 → 停止回報（gitignore 死結律，標配）。
- `settings.json` 編輯後 `git diff --stat` 顯示整檔改動（行尾被改寫）→ 還原並停止回報。
- `gate_ledger.py` 實測行為與 docstring／AI-0 描述不符且需改 helper → 停止回報（本票禁改 helper）。
- 編輯 `settings.json`／註冊 Stop hook 或啟動 nested `claude` 被權限拒絕 → 記為 `HUMAN_REQUIRED(PERMISSION)`，**不改道重試同一被拒動作**，不繞過 sandbox／permissions。
- 互動式 Desktop session 不可用，或 Akira 未授權真實 PPSS → 對應鏈（2／3）維持未驗，如實揭露，不以 mock 補；其餘 AI 照常完成。
- 鏈 4 場景無法產生三個合法失敗 → 重設計至多 2 次後回報。
- 發現第二個獨立缺陷或需擴大 scope（如需改 helper、其他 hook、`CLAUDE.md`、skills）→ 先本地取證與 Advisor，依 Gate 以 `SCOPE` 處置，不自行擴大。
- 鏈 2、3 顯示 Router 僅在被 block 後才被呼叫 → 如實標「僅補救」，不改寫為途中主動。

## Acceptance Criteria
- [x] **[存在性]** `.claude/hooks/gate_stop_guard.py` 與 `.claude/hooks/tests/gate_stop_guard_test.py` 存在；`python -m py_compile` 通過；hook 原始碼不含 `notify`、`telegram`、對 `gate_ledger` 的 import。 〔證據：兩檔存在；`py_compile` 通過；`gate_stop_guard_test.py` 的 `test_source_has_no_notification_or_internal_import` 通過〕
- [x] **[存在性]** `.claude/settings.json` 的 `hooks.Stop` 為 3 組，第 3 組僅含 `gate_stop_guard.py` 且顯式 `timeout`（大於內部時限）；前 2 組位元組不變；`git diff` 無整檔行尾改寫。 〔證據：`git diff --stat -- .claude/settings.json` 9 insertions／0 deletions，`json` 解析 Stop 為 `[[None,None],[None],[30]]`，diff 無 CR 行尾改寫〕
- [x] **[存在性]** `ssot-governance.md` 之 diff 僅涉 AI-6 六個指定語義位置及必要相鄰文字（不要求恰好六個 hunk），且：L185 寫明「適用範圍排除 BLOCKED」且終態集合未含 BLOCKED；L193 舊「每輪」字面已移除、同一 incident 預算與「不構成授權」「額外調用不等於授權」已寫入；L175／L239 無條件語、無「待後繼票」字面、無「（T05）」、含「不得宣稱完整 Gate」與「不得宣稱 hook 涵蓋途中違規」；L217 已無「目前皆未實作」，分軸直述（呼叫路徑已接入含 done；機械後盾含 pause 與 qa／commit／sv 漏檢事後偵測；無機械後盾含 done、closeout），done 既非未接線亦非有後盾；L211 已改為「`consult --kind advisor` 只能記錄實際原生 Advisor 調用」、Advisor 不可達時如實說明且停止由 `NO_PROGRESS` 預算放行，無「以記錄為證」字面；L219 為「預設上限（8 次）」。 〔證據（Rev B 更新）：`git diff --stat` 7 行替換（7+／7−），分布於 5 個 hunks（L175／L185／L193／L211／L217／L219／L239，其中 L219 為相鄰文字）；`grep` 無「待後繼票」「目前皆未實作」「每輪 1 次」「（T05）」「以記錄為證」「官方上限」；L217 分軸直述且 done 既非未接線亦非有後盾〕
- [x] **[可達性]** hook 各輸入皆有 production producer，執行後回填〔檔案:行號〕：payload 欄位（`session_id`／`prompt_id`／`cwd`／`transcript_path`／`stop_hook_active`／`last_assistant_message`）引 `tmp/…/run_20261013_144805/hook_log.jsonl` 實測；`tmp/verify/active.json` 之 writer＝`.claude/scripts/activate_ticket.sh`（寫入段）與 `.claude/skills/qa/scripts/verify_checklist.sh:55`；Registry 檔；`status --json` 之 `incidents`／`pending_human`／`receipts`／`ppss`／`blocks`／`missed_checks` 欄位（`gate_ledger.py` `summarize`，`receipts[-5:]` 於 L514）；O2 三推進證據 producer（`verify_checklist.sh:568` 寫 `step0_<sha>.txt`、帶 `[<T>]` 前綴 commit、`.tmp/sv_report_*.md` 之寫入者）；transcript user 行 `promptId`。producer 證據欄：〔執行後回填〕 〔producer 證據：hook 實際使用的 payload 欄位 `session_id`／`prompt_id`／`cwd`／`transcript_path`（事件另提供 `stop_hook_active`／`last_assistant_message`，hook 未使用）見 `.tmp/ticket-author/20261013-t04-probe/run_20261013_144805/hook_log.jsonl` 實測，且鏈 4a／4b／5 真實註冊執行中 hook 正確收到；`tmp/verify/active.json` writer＝`.claude/scripts/activate_ticket.sh:184-194`、`.claude/skills/qa/scripts/verify_checklist.sh:55`；Registry 檔 `plans/cso_briefing/project/TICKET_REGISTRY.json`；`status --json` 欄位＝`gate_ledger.py:499`（`summarize`）與 L514（`receipts[-5:]`）；O2 證據 producer＝`verify_checklist.sh:568`（`step0_<sha>.txt`）、`lib_verify.sh:131-152`（`.tmp/sv_report_*.md`）、帶 `[<T>]` 前綴 commit（`git log`）；transcript user 行 `promptId`＝payload `prompt_id`（probe 實證，鏈 4a 中 hook 亦正確取得）。限制：closeout 腳不實作（X2）、receipt 視窗 5 筆（N6）〕
- [x] **[行為性]** given/expect（AI-4，經暫存 repo＋真實 `gate_ledger.py`）：given `BLOCKED`／終態／取消／討論／明確工具限制（保留 incident、不解除 pending）／payload 缺 `prompt_id`／hook 例外／逾時，expect 放行且 ledger 無新事件；given 相近但非限制的提示（如「請使用工具修復」），expect 仍 block；given open incident `rounds_failed<3` 無 pending，expect block 且 reason 含 Router 步驟、用量、邊界句；given `rounds_failed==3` 無 pending，expect reason 僅允許升報；given 局部 pending 與另一 open incident，expect block 另一 incident；given 整票 pending 合法，expect 放行；given hook 寫入非 0（含 5、6），expect 標記檔一次 block 後放行、不重試；given 四分量皆未嚴格增加連續 6 次，expect 寫 `human NO_PROGRESS` 後放行；given 重複 check／finding／相同 sha256 或 incident／pending 數增加，expect 不歸零。 〔證據（Rev B 更新）：`gate_stop_guard_test.py` 42 例全 OK（約 46 秒，含 R7 三例工具限制回歸；暫存 git repo＋複製真實 `gate_ledger.py`，hook 以 subprocess 驅動；含 exit 5／6／status 損毀寫入失敗、高水位、K=6、局部／整票 pending、BLOCKED、取消／討論／工具限制、子目錄 cwd）；helper 既有 `test_gate_ledger.py` 39 例仍 OK。pure function 例（四分量 sha256）以合成 status dict 驗證。此為已測分支，不替代主鏈〕
- [x] **[行為性]** **鏈 1**：given 已授權已知失敗，expect 主 agent 直接修復、驗證通過、`round end pass`、續作，無 advisor／ppss 事件。 〔證據：headless `claude -p`（CLI 2.1.296）暫存專案 `tmp/ADV-GATE-04/chain1`，N=1：agent 工作途中自行走 Gate——finding→`consult local`→`round start`（新策略）→修 `calc.py`→`python test_calc.py` OK→`round end pass`→`check done`；ledger 5 事件、無 advisor／ppss 事件、blocks=0（hook 未需介入）；弱證據，不寫成普遍可靠〕
- [ ] **[行為性]** **鏈 2（互動式）**：given 本地無法判定下一個安全修法，expect 途中（首次 Stop block 之前）呼叫原生 `advisor`，transcript 有 `server_tool_use advisor`＋`advisor_tool_result`、ledger 有 `consult advisor`，其後修復於 scope 內、驗證通過、續作；內容加密，以行為與 note 證明消費。弱證據 n，標註。 〔UNVERIFIED（Akira Rev B 裁決不再等待）：需互動式 Desktop session（headless 無 `advisor` 工具，probe N=1）；且 headless 兩次實跑中 agent 以 `consult --kind advisor --note 'Advisor (main agent): …'` 自述充當 Advisor 紀錄——ledger 無法分辨，故此紀錄不得充當真實 Advisor 證據〕
- [ ] **[行為性]** **鏈 3（互動式）**：given 修復需未被既有 artifact 回答的外部窄事實，expect 途中主動新跑一次真實 `research_submit`（官方網域限制），`ppss record`／`ppss read` 使 `evidence_ok=true`，消費後修復、驗證、續作，且未重送三個既有 artifact 的研究。弱證據 n，標註。 〔UNVERIFIED（Akira Rev B 裁決不再等待）：需互動式 session＋Akira 於執行時授權一次真實 PPSS；題目須先核對三個既有 artifact 未回答〕
- [ ] **[行為性]** **鏈 4**：given 同一 incident 三個相異策略皆合法驗證失敗，expect executor 直接升報 `HUMAN_REQUIRED(EXHAUSTED)`、hook 放行，EXHAUSTED 事件後 transcript（含 Bash 語義）與 ledger 無同 incident 續作，另一未耗盡 incident 不被誤擋；若出現第四輪 `round start` 被拒（exit 5），expect 不重試直接升報。 〔UNVERIFIED（Akira Rev B 裁決：不設計第三次場景；executor 第三輪後直接升報之真實行為未驗，決定性分支與 helper 測試只列已測部分，不替代主鏈）：2 次場景設計——4a（互斥測試）agent 於 round 0 即以 `NO_AUTHORIZED_STRATEGY` 升報，其 advisor consult 為自述；4b（9 hex 零 nonce）agent 啟動數小時級預計算、round 開啟未收，測試 timeout 570s 被終止（已終止由本次啟動之殘留 `mine.py` 程序樹 PID 28500 及 8 子程序）。無「三個相異策略皆驗證失敗」之實例，故未驗。輔助真實觀察：4a 中 hook 在 local pending 下僅單次核對一次、agent 核對後停止；4b 中 hook 於 round 進行中的提前停止實際 block 一次並帶回 Router，agent 續作〕
- [x] **[行為性]** 註冊後多 hook 並存重放：given 新 guard 與既有 guard 同時 block，expect 兩個 reason 皆送達、不互相覆蓋（沿用 probe 形狀，n≥1）。 〔證據：headless 暫存專案 `chain5`（新 guard＋既有 `registry_review_stop_guard.py` 同註冊，ticket AC 全勾且有 open incident），N=1：同一次 Stop 兩個 hook 皆 block，agent 收到兩則 `Stop hook feedback`（各含 `registry_review_stop_guard`／`gate_stop_guard`），回覆中同時列出兩者；舊 guard 僅一次，新 guard 其後每次 Stop 持續 block（5 次，ledger blocks=5，因使用者提示禁止工具而無進展，未達 K=6 即達 max-turns）〕
- [x] **[存在性]** 收尾實錄含逐列覆蓋矩陣實測現況與上列揭露項，無「完整 Gate」或「hook 涵蓋途中違規」宣稱。 〔證據：下方「覆蓋矩陣（Rev B 填寫）」四類分開記錄：已實作／已測分支／真實行為未驗／觀察到的缺陷；SV 與 Cross-Review 結果於其後「收尾實錄」補記〕
- ~~`assembleDebug` PASS~~ (N/A: 無 `app/` 變更，僅 `.claude/` 治理工具與規則)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 〔證據：`git diff --name-only` 僅 `.claude/rules/ssot-governance.md`、`.claude/settings.json`；新檔 `gate_stop_guard.py`、`gate_stop_guard_test.py`（untracked）；皆 ⊆ ALLOWED〕


## 執行中紀錄（部分，未收束；非「收尾實錄」）
- **日期**：2026-10-13（約 18:09–19:00，+08:00）；啟動：`activate_ticket.sh ADV-GATE-04`；冊面 `PENDING→IN_PROGRESS`。
- **AI-0**：無漂移（HEAD `8a7c18e9`）。AI-1～AI-6 完成並勾選，工作樹未 commit（尚未跑 `/qa`、`/sv`；AI-7 未完成，不進入）。
- **AI-7 實跑摘要**（暫存場景 `tmp/ADV-GATE-04/chain{1,4a,4b,5}`，`.gitignore` 忽略，易被清理，故重點已複製於此）：
  - 鏈 1 PASS（headless，N=1，弱證據）。鏈 2、鏈 3 UNVERIFIED（互動式 Desktop session；真實 PPSS 授權）。鏈 4 UNVERIFIED（2 次設計未成立，不再設計第三次）。多 hook 並存 PASS（N=1）。
  - **重要發現（影響 Gate 完整性判斷，非本票缺陷但須揭露）**：headless 無 `advisor` 工具時，agent 自行以 `consult --kind advisor --note "Advisor (main agent): …"` 寫入「已諮詢」紀錄（4a、4b 各 1 次），helper 與 hook 皆無法分辨；`NO_AUTHORIZED_STRATEGY` 的前置（local＋advisor consult）因此可被自述滿足。鏈 2 只能以 transcript `server_tool_use advisor` 為證，不得以 ledger consult 代替。
  - **觀察**：round 進行中、agent 欲等待長時間背景作業而停止時，hook 依設計 block（4b）；K=6 為其停損。使用者提示「不要使用任何工具」時，hook 仍持續 block（chain5，5 次）——使用者對工具的限制不被辨識為取消，K=6 為唯一出口；列為已知限制。
  - scratch agent 曾對真實 Registry 做唯讀 `registry_list`（查無 TST-GATE-01），無寫入。
- **收尾觀察（只計本次範圍；排除 seq／ledgerfail 與其他 session 標記；寫入失敗或標記缺失造成之限制：本次所有 ledger 寫入皆成功，標記檔 `tmp/.stop_guard/` 於各 scratch 專案內完整保留）**：

  | 範圍 | ledger `blocks`（實質阻擋） | 單次核對標記（`*.check`） |
  | --- | --- | --- |
  | 真實 repo `ADV-GATE-04`（本 session 首次 Stop：hook 已實際觸發一次單次核對，agent 依票面核對後如實說明） | 0（ledger 不存在；單次核對不寫 ledger） | 1 |
  | chain1 | 0 | 0 |
  | chain4a | 0 | 1 |
  | chain4b | 1 | 0 |
  | chain5 | 5 | 0 |

## 覆蓋矩陣（Rev B 填寫；四類分開記錄）
**A. 已實作（控制面）**
- `.claude/hooks/gate_stop_guard.py`：fail-open 外框（缺 payload 欄位／未預期例外／20 秒內部時限）；`git-common-dir` 定位；放行條件（無 active、Registry 終態、`BLOCKED`、位置非 `loc:pending`）；使用者取消、討論／提問、明確工具限制放行（保留 incident）；整票 vs 局部 pending（信任 `status.pending_human`）；open incident（`rounds_failed<3` 帶 Router、`==3` 僅允許升報）；O2 漏檢偵測（`qa`／`commit`／`sv` 三腳，受回合起點與 receipt 5 筆視窗限制）；單次核對；四分量高水位與 K=6 `NO_PROGRESS`；hook 寫入失敗一次 block 後放行。
- `.claude/settings.json`：Stop 第 3 組（`timeout` 30）。
- `.claude/rules/ssot-governance.md`：六個語義位置（適用句／預算／L175／L211／L217／L239）與 L219 相鄰字面。
- 四軸（對應 L217）：呼叫路徑（入口 `check`，模型契約）已接入 qa／commit／sv／closeout／done，`resume` 讀 `status`；機械後盾＝pause（本 guard）與 qa／commit／sv 漏檢事後偵測；無機械後盾＝done（有入口 check，但無推進證據 producer）、closeout（無漏檢偵測腳）及其餘決策點。

**B. 已測分支（決定性，暫存 git repo＋真實 `gate_ledger.py`；不替代主鏈）**
- `gate_stop_guard_test.py` 42 例 OK：放行條件、取消／討論／工具限制、open incident 兩種 reason、局部／整票 pending、單次核對、O2 三腳與 UNKNOWN／視窗飽和、高水位與 K=6、寫入 exit 5／6 與 status 損毀、標記檔不可寫、子目錄 cwd、原始碼無通知／內部 import。helper `test_gate_ledger.py` 39 例 OK（未改）。
- 真實執行（N=1，弱證據）：鏈 1 PASS；多 hook 並存 PASS；另觀察到 hook 於 4a 局部 pending 下單次核對、於 4b round 進行中提前停止被 block。

**C. 真實行為未驗（UNVERIFIED）**
- 鏈 2（真實 Advisor、途中主動）、鏈 3（真實 PPSS 回流）、鏈 4（executor 第三輪後直接升報與停損後無續作判定）。
- R7 工具限制放行與 R6 新 L211 字面對真實 agent 行為的影響（僅有決定性測試）；「途中走 Router」是否由文字修改引發（僅鏈 1 一次、與 Advisor／PPSS 無關）。
- Stop 當下 transcript 是否含 advisor 標記；平台連續續行預設上限（8，relayed）之本機實測；使用者 interrupt 是否觸發 Stop；多 prompt 的 `promptId` 對應。

**D. 觀察到的缺陷／限制**
- 自述 Advisor：headless 無 `advisor` 工具時 agent 以 `consult --kind advisor --note 'Advisor (main agent): …'` 自行記錄（4a、4b），helper 與 hook 無法分辨；該紀錄保留為失敗證據，不計為真實 Advisor 諮詢。L211 已改寫為只能記錄實際原生調用，**無機械強制**。
- **未覆蓋**：Advisor 不可達環境下，驗證先行類升報無法寫入 ledger，需經 K 次 block 才由 `NO_PROGRESS` 放行（headless 限定）。
- 使用者「不要使用工具」曾造成持續 block（chain5，5 次）；R7 已加窄匹配放行並有回歸測試，真實鏈未重跑。
- round 進行中等待長時間背景作業而停止時被 block（4b）；K=6 為唯一停損。長時場景已停止（殘留程序已清），後續場景須限制運算量。
- `done` 無推進證據 producer、無機械後盾；closeout 無漏檢偵測腳；O2 受 receipt 5 筆視窗限制；Stop 之前的中途違規不被攔截；預算無機械強制；pending 信任 helper 寫入驗證（不偵測繞過 helper 的直接 jsonl 偽造）；討論與工具限制辨識為啟發式（錯判方向為少擋）。
- **seq 標記無法寫入可能使 K 失效（已報缺口，本次未修）**：`persistent_block()` 對 `tmp/.stop_guard/<session>_<prompt>.seq` 的 `marker_write` 回傳值不檢查（`gate_stop_guard.py` 兩處）；若標記目錄不可寫，每次評估皆視為首次（count=1），`NO_PROGRESS`（K=6）永遠不觸發，持續義務將 block 至平台預設續行上限。證據狀態：僅由原始碼閱讀得出，無對應測試（現有 `test_marker_unwritable_allows` 只涵蓋 ledgerfail 路徑）；依 Akira 收束裁決不再追加實作。
- 不宣稱完整 Router 已驗收、完整 Gate、hook 涵蓋途中違規或 headless 全鏈支援。

## 收尾實錄（Agent；QA／SV 結果；未轉 REVIEW）
- **QA**：Step 0 PASS（`check qa` exit 0，pre-commit diff 為空）；Build／Lint N/A；Tests＝hook 42 例、helper 39 例 OK；PROGRESS SKIPPED — DEFERRED-TO-DOCSYNC；Auto Commit `d724475f`（4 檔，980+／7−）。
- **SV**：前置 Gate `check sv` exit 0；階段 A／B／C 無 BLOCKER；於 HEAD 重跑兩套測試仍為 42／39 OK。
- **Cross-Review（Codex）**：`REPORT_FIX` ×3（皆 codex 來源）——①ssot-governance 差異「7 hunk」應為「7 行替換，分布於 5 個 hunks」；②Registry lifecycle 缺 production writer 證據（已補 PLT `registry_service.py:1045/1102/493/463`、`main.py:906`）；③報告列出 hook 未使用的 payload 欄位（已改為「事件另提供、hook 未使用」）。第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2）；**第③項修正於 LIMIT 後完成，未經 Codex 重驗**。機械輪次 `R0`（`ROUND=0`、`REPORT_FIX_COUNT=2`、`LAST=REPORT_FIX`）；邏輯輪次 vault 未記錄。Codex 未判任何程式碼缺陷。
- **Gate**：INC-001（subject `sv-report:ssot-hunk-count`）rounds 2／3 失敗、`step_limit` 1；本地取證＋**真實原生 Advisor 調用 1 次**（互動式本 session）＋strategy-scan 5 項後，依 L211 新字面升報 `HUMAN_REQUIRED: NO_AUTHORIZED_STRATEGY | ADV-GATE-04 | INC-001 | round=2/3`（pending 存在於 ledger）。**未清 state、未用 `REPORT_FIX_CLOSURE=1`、未再自動重跑。**
- **張力（交 Akira 裁決，不自行定案）**：`sv/SKILL.md` 寫「`REPORT_FIX_SOURCE=codex` 一律 `REPORT_FIX_ESCALATE` 並停止」，而本次前兩次 codex 來源 REPORT_FIX 依 Gate「新且已授權策略＝新一輪」處理並重跑（比照 ADV-GATE-03 先例）。
- **冊面**：lifecycle 維持 `IN_PROGRESS`；不轉 `REVIEW`／`EXECUTED`（Cross-Review 未通過、鏈 2／3／4 與 AI-7 UNVERIFIED）；票檔留 `Pending/`。
- **未勾項全盤點**：AC 鏈 2／3／4＝豁免〈Akira 2026-10-13 Rev B 裁決：UNVERIFIED，不再等待〉；AI-7＝豁免〈同上〉；Cross-Review 通過＝裁決中〈Akira〉。
- **Akira 收束裁決（2026-10-13）**：接受已實作控制面之限定交付；**明確豁免額外 Codex PASS 要求**。Codex 真實狀態＝REPORT_FIX ×3＋`CHECKPOINT_REPORT_FIX_LIMIT`，最後一處報告修正未經 Codex 重驗；**豁免不等於 PASS**，冊面 `reviews.codex` 記 `N_A` 並於 checkpoint 揭露豁免依據。不重跑、不清 state、不用 `REPORT_FIX_CLOSURE=1`。
- **incident 處置**：以 helper 現有合法路徑 `human resolve --incident INC-001 --ref <Akira 收束裁決>` 清除 `NO_AUTHORIZED_STRATEGY` pending（ledger seq 18，`cleared:1`），隨後 `check closeout` exit 0（seq 19）。INC-001 失敗歷史完整保留（rounds 2／3、均 fail、`step_limit` 1、5 項 strategy-scan），**未偽造 round end pass、未改原始 ledger**。**介面缺口（具體）**：helper 無「依 authority 接受未驗／豁免並關閉 incident」路徑——incident 只能以 `round end --result pass` 結案，故 INC-001 在 ledger 仍為 `open`；因票轉 `REVIEW`（終態 lifecycle）後 Stop guard 一律放行，實際無執行面影響，但 ledger 的 `open` 與事實不符。依裁決不另造修復迴圈。
- **移交**：lifecycle `REVIEW`（由既有終審角色完成後續 `CLOSED`）；票檔搬入 `deliver/`；`reviews`＝qa PASS／sv PASS（階段 A–C 無 BLOCKER，structural PASS）／codex N_A（Akira 豁免）／traycer N_A。PROGRESS 同步、Done 標記、Dashboard 依 FORBIDDEN 留待下一輪 `/compact` 標準收尾（DEFERRED-TO-DOCSYNC，不另開票）。
- **已交付範圍**：`gate_stop_guard.py`、`gate_stop_guard_test.py`（42 例）、`settings.json` Stop 第 3 組、`ssot-governance.md` 六個語義位置＋L219 相鄰字面（commit `d724475f`）。**未驗**：鏈 2、3、4（含 executor 第三輪後直接升報之真實行為）；R7 工具限制放行與 L211 新字面對真實 agent 行為的影響；Stop 當下 advisor 標記可見性；平台連續續行預設上限本機值；使用者 interrupt。本次真實 Advisor 調用僅作補充證據，不升級為鏈 2 PASS。
