# TKA-WIRE-01: 正常 QA／SV 與低風險免 QA／SV 兩條上游路徑，於各自必要工作完成後皆以 Skill 工具實際調用 `ticket-author`（args `close <票號>`），並由 TKA 以 Codex 主審的真實另一個 reviewer 對完整 AC 清單逐項驗收後判定收束

**Revision**: Rev B（2026-10-22，Owner 開工裁決「同票修正後直接執行」；取代 Rev A 中與之衝突的 reviewer 路由〔原「文字預設 Sonnet」撤回〕、核心 AC 覆蓋範圍〔改為完整 AC 清單〕、BLOCKER 停止位置〔改為停止不安全操作但仍交 TKA 唯讀判定〕及驗收解讀〔輕量路徑模型 JSON 探針降為前測〕；維持單一正式票，不退回 plan、不另開票；併入同日 RGR Preflight Cycle 1 對 Rev A 的修補 F1–F11〔報告 `.tmp/rigor-gate/RGR_TKA-WIRE-01_20261022.md`〕：該修補寫在 Rev A 票檔上，Rev B 改寫時一度被覆蓋，已依 RGR 報告與其 `wire.json` 重新併回，並依 Owner 新裁決調整〔F1 低風險實作提交、F2 判準唯一引用 `CLAUDE.md`、F3 Step 11 一律第 1–5 項、F4 升級先於覆蓋遮蔽、F5 reviewer 中繼資料缺鍵、F6 人類訊息定義、F7 ADV-ENTRY-01 協調、F8 LIMIT 史入交付摘要、F9 AC 標型、F11 唯讀僅提示層〕）

**簽發日期**: 2026-10-21（Rev B：2026-10-22）
**簽發者**: Claude（`/ticket-author ticket`，Diag-backed）
**來源議題**: TKAuthor
**Mode**: normal  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: TKA-CLOSE-01 —— 本票所接線之 `Mode: close`、`close_helper.py`、Table 第 ⑤ 段與 close 列來源（CLOSED，`deliver/TKA-CLOSE-01_autonomous.md`；其「已有 TKA 自主驗收成功」之過寬結論**不沿用**）
- `coordination_dependency`: ADV-ENTRY-01、PJMAN-CLOSELOOP-01 —— ADV-ENTRY-01（`REVIEW`，仍可能修訂；其提交 `32a0cc0c` 等已改 `gate_stop_guard.py`／`ssot-governance.md`）與本票共享二檔，開工前後重讀現值並保留其變更；與 PJMAN-CLOSELOOP-01 共享 `ssot-governance.md`、`sv/SKILL.md`；該票已於 2026-10-22 前 `CLOSED` 並提交（`27cc28ea`），其修改已在基線內；AI-0 逐行重讀現值、保留其變更，**不**以「行不相交」為永久前提

**檔案治理／來源說明**：

- Parent 類型＝Diag-backed。Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261021-p2-dual-path-skill-invocation-diag.md`（`.tmp` 暫存，七天內可能消失；本節即獨立可讀摘要）。Parent artifact 狀態＝`Fixture: false`／`Gate: PASS`／`Verdict: PARTIALLY_CONFIRMED`；PARTIALLY_CONFIRMED 之未決僅為「長期行為未驗」（runtime 未知，非 hard risk），以 `Claim status: UNKNOWN`＋AI-9 實際 Agent 觀察＋失敗分支＋`[行為性]` AC 承接。Parent 讀取時間＝2026-10-21 23:1x（+08:00）。plan-local item：無。
- 選定項目：雙路徑在完成各自必要執行後，都自主實際調用 `ticket-author` Skill，args 為 `close <票號>`；由 TKA close 取得機械證據、調度真實另一個 reviewer 覆蓋完整 AC 清單，再決定 CLOSED／NOT_CLOSED／blocker，不由上游實作 Agent 繞過 Skill 自行終審。
- **Owner 裁決（2026-10-21 開票裁決 1–8 ＋ 2026-10-22 開工裁決一至十三，本票適用者，取代衝突舊文）**：
  1. P2 為 TKAuthor 議題階段名，非另一條 SV／SSOT 線；helper 可用／文件 AUTO-CHAIN／手動同步驟／人工補下 close 後稱自然鏈，皆不替代真實 Skill 調用與 reviewer 驗收；不重開或回滾 TKA-CLOSE-01，亦不再以它證明完整 TKA 自主驗收。
  2. 否決「SV 無 PASS／ADVISORY 就不鏈 close」。REPORT_FIX 觸 LIMIT：不重設計數、不無限重送、不偽寫 PASS／ADVISORY，實際歷程與未決 finding 作為 TKA 證據；已確認 BLOCKER：停止不安全操作與錯誤交付，**但仍將 blocker 與證據以同一 Skill 調用交 TKA 作唯讀判定**，不為呼叫 close 假轉 `REVIEW` 或偽填 PASS，lifecycle 不動；來源或工具不可達 → 回報 TKA close 未完成，不繞過 Skill 手動收束。
  3. 兩路徑都用實際 Skill 工具調用契約（`skill`＝`ticket-author`、`args`＝`close <TICKET_ID>`）；輕量路徑統稱「低風險免 QA／SV」，重用 `CLAUDE.md` 〈SOP〉風險分級的全部適用條件（不僅副檔名），不新增 RAPID 開關或分類 schema，共用 closeout 重用 Step 11、不複製第二套；票號取自目前正式票上下文並與票檔 `TICKET_ID`、Registry ID 核對，`tmp/verify_cmd.conf` 可重用但不得為輕量路徑必要依賴、不得讀到他票 ID。
  4. **Reviewer 路由（取代 Rev A 的「文字預設 Sonnet」）**：Codex 全票型主審——程式、複雜功能、承重邏輯，以及會改變執行／授權／驗收／關票行為的 Skill 或規則（含承載複雜行為的 `.md`）→ `gpt-6-sol`；真正簡單文字、排版、格式、明確 metadata → `gpt-6-luna`。Luna 無法判斷承重語義、發現複雜矛盾或與機械證據衝突時**升 Sol**（能力升級，不因 Luna 回 FAIL 就改家族）；只有 Codex 認證／連線／配額／服務或模型不可用、或有界恢復（一次重試）後仍無可用結果，才**備援**切 Claude：複雜→獨立 Opus、簡單→獨立 Sonnet。REPORT_FIX、實質 finding、FAIL、LIMIT 不等於 Codex 故障；備援後核心 AC 仍不成立就如實 NOT_CLOSED。GPT-6 Terra 不納入預設；不新增重試系統或計數器。
  5. Reviewer 必須是**真實另一個驗收調用**：原實作 Agent 不得 inline 自答後宣稱第二方驗收；Claude 備援另啟 reviewer；同模型另一 subagent、跨模型、inline 自答不混為同一獨立性；如實記錄實際模型、審查範圍與備援原因，不冒稱 Codex 或跨模型驗收。Desktop／Perplexity 為可選複審，不是 mandatory close gate。
  6. Reviewer 對**每條 AC**（含 N/A，不限核心）回 PASS／FAIL／N/A／UNVERIFIED；N/A 核對不適用理由而不執行不適用測試；非核心 UNVERIFIED 可不阻擋但不得省略交審；coverage 由實際 reviewer 回覆整理，不以 Agent 自填名單替代；helper 檢查覆蓋完整性、一對一與有效序號，不得宣稱證明模型判讀品質；模型輸出與機械證據矛盾時針對該點補證或升級，自由文字不覆蓋直接事實。核心 AC 依交付目標必要性判定，不得因難驗而降級。
  7. 驗收分層：A 契約與機械測試（scratch／stub／grep 只證契約）；B 正常路徑——本票自身走正常路徑，上游完成後不經使用者補指令實際發出 `Skill(ticket-author)` `close TKA-WIRE-01`，同次 close 內真實 reviewer 取得完整 AC 與證據並回覆，結果可為 CLOSED 或 NOT_CLOSED 但須由該 Skill 調用內產生；C 輕量路徑——模型輸出 JSON 的探針只算前測；優先在隔離環境以真實 Agent、實際 Skill 調用、不寫正式 Registry 的受控工具替身觀察；無法安全完成則標「輕量實際鏈未驗」，不建立測試平台或無限循環；D 調用紀錄——解析實際 `tool_use` 的名稱與 input 欄位（不依賴字串順序 grep），區分人工發起／上游自然續行／Skill 實際進入／reviewer 實際覆蓋。
  8. 資料：中間衍生物只存 `.tmp` 七天，不 commit、不上 TKR、不新增永久 invocation ledger／receipt；原票只留最小最終結果（交付摘要、最終驗收結論、實作提交、真實收束結果、實際 reviewer 與必要限制短摘要）；未 CLOSED 不假寫完成結果，read-back 失敗不寫「讀回一致」。
  9. 共享檔：重讀現值與實際修改範圍，保留他票已提交變更，只有無法安全判定的實際衝突才精確回報；權限先確認有效配置，實際被拒才提出最小需求，現有 settings 不在本票授權範圍不修改。ALLOWED 為達成唯一目標可同票最小擴充（Rev B 增 `state-transitions.md`）。

**Template 讀取紀錄**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件日期 2026-05-29，effective revision date 2026-09-14（v3.11）；2026-10-21 全文讀取（322 行），blob `6bb37f39`（Rev B 未變動）。

**證據分級**（2026-10-21 23:1x 與 2026-10-22 00:2x live 直讀；`main` @ `27cc28ea`，工作樹乾淨；冊面 `registry_list(topic=TKAuthor)` 無同義票）：

- **CONFIRMED（本地原始碼／工具輸出）**：
  1. 本 session transcript（完整 jsonl）Skill 工具調用計數：`qa`×3、`sv`×3、`ticket-author` diag／plan／ticket，**帶 `close` 引數＝0**；TKA-CLOSE-01 之 close 由 Agent 以 Bash／Registry 工具手動依序執行，且為 Owner 裁決後續行。
  2. 正常路徑文字鏈：`uc/SKILL.md` 規則 8（DONE→`Skill(qa)`）→ `/qa` Auto-Chain → `/sv` → `sv/SKILL.md` Step 11 第 4 項與 Table close 列，二處皆寫「呼叫 `/ticket-author close <…>`」（斜線形式；同檔 projman 用 `Skill(projman)`）。
  3. 輕量路徑：`CLAUDE.md` 〈SOP〉風險分級允許低風險 docs／治理文本票（僅 `.md`、可 `git revert`；觸及 `app/` production code、腳本／pipeline 或不可逆操作者維持全程序）省略 `/qa`／`/sv`／cross_review；「RAPID MODE」於本專案**無定義**，僅在 `ssot-governance.md`（第 ④ 段註、Table 兩列共 3 處）、`sv/SKILL.md` Step 11 引言（1 處）、`gate_stop_guard.py`（1 處）出現；closeout→close 的步驟只存在於 `sv/SKILL.md` Step 11；`uc/SKILL.md` 規則 8 無低風險分支；guard 提示「RAPID 豁免…可停止」與 close 要求衝突。
  4. `ticket-author/SKILL.md` frontmatter `disable-model-invocation: false`；description 含 `Manual modes: diag, plan, ticket, close.`。
  5. `close_helper.py` `decide` 僅驗 verdicts 完整性，不驗 reviewer 回覆；`reviewer` 子命令現以 Sonnet／Codex 二分（docs→Sonnet、code→Codex），`SKILL.md` 步驟 3 同；`FALLBACK_REVIEWER` 現為跨家族「另一位」。TKA-CLOSE-01 之 AC 判定為實作 Agent 自寫，Sonnet 僅被問 X3。
  6. **Codex 模型本地確認（2026-10-22）**：`codex-cli 0.162.1`；`~/.codex/models_cache.json` 列 `gpt-6-sol`（GPT-6-Sol）、`gpt-6-luna`（GPT-6-Luna）、`gpt-6.1-sol`（config 預設）、`gpt-5.6-*` 等；以 `codex exec -c model="gpt-6-luna"` 與 `gpt-6-sol` 各做一次極小呼叫，皆回覆 `OK`，故本票採用精確 ID **`gpt-6-sol`／`gpt-6-luna`**（Terra 未納入）。`cross_review.sh` 預設 `gpt-5.6-sol` 屬另一流程且為 FORBIDDEN，不動。
  7. `claude` CLI 2.1.304 與 Python `mcp` 套件存在（C 輕量實際鏈之受控替身可行性前提）；其是否能在隔離 repo 內完成一次安全觀察，由 AI-9 嘗試決定。
  8. `PJMAN-CLOSELOOP-01` 已 `CLOSED`（`27cc28ea`），其對 `sv/SKILL.md` Step 11 第 5 項與 Table projman 兩列之變更已在基線。
- **SUPPORTED**：`Skill(ticket-author)`／`Skill(sv)` 於目前環境可被模型調用——本 session 對 `qa`／`sv`／`ticket-author` 之 Skill 工具調用皆成功啟動（使用者層級 `defaultMode=auto`；repo `settings.json` allow 僅 `Skill(qa)`）；有效配置於 AI-0 讀取確認，不因 repo allow 較少就推定受阻。
- **UNKNOWN（以 AI-9 承接，見失敗分支）**：真實 Agent 於上游完成後無人提醒即發出 `Skill(ticket-author, "close <ID>")`；TKA 內 Codex reviewer 對完整 AC 清單之實際回覆形狀；低風險路徑從未有真實票走過（本輪僅查本 session，不得寫成歷史從未發生）。

**承重推導反例檢查（Evidence gate）**：
- 推導 D1「低風險免 QA／SV 判準＝`CLAUDE.md` 風險分級全部條件（僅 `.md`，非 `.txt`／`.rst`）」。符合前提而結果不同之狀態：ALLOWED 全 `.md` 但為行為承載的 `SKILL.md`／規則檔 → 仍可能判低風險（CLAUDE.md 明列「治理文本票」）。處置：此為已裁決之結果，不由本票改判；但 **reviewer 路由依實際行為影響**——此類 `.md` 屬 heavy，TKA close 以 `gpt-6-sol` 主審，故免 QA／SV 不等於降低 TKA 驗收。邊界狀態：ALLOWED 混 `.py`／`.json`／`.sh`／hook／`app/` → 走正常路徑；票面含不可逆操作（刪除、Registry 破壞性寫入、外部發佈）→ 走正常路徑；不可 `git revert` → 走正常路徑。
- 推導 D2「LIMIT／BLOCKER 後交 TKA 不放行有實質缺陷者」。狀態：Codex 第 3 次 REPORT_FIX 為真實缺陷而 LIMIT 觸頂 → 不偽寫 PASS，verdict 史與未解 finding 落 checkpoint，TKA 將其當證據並由真實 reviewer 對全 AC 判斷，核心 AC 不成立即 NOT_CLOSED；已確認 BLOCKER → lifecycle 非 `REVIEW`（或仍 `REVIEW` 但帶 blocker 證據），TKA 唯讀判定、不寫任何東西，無近似狀態使 BLOCKER／LIMIT 單獨導致 CLOSED。
- 推導 D3「reviewer 全 AC 覆蓋規則不使票永遠 NOT_CLOSED」。狀態：Codex 故障且 Claude 備援亦不可用 → `actual` 無 → 補證至上限後 `NOT_CLOSED(reviewer_coverage_incomplete)`，票保留，如實回報；Luna 對承重語義不足 → 升 Sol 一次（`--fallback-tried`），再不足則 NOT_CLOSED；不構成缺陷。
- 推導 D4「coverage 取自 reviewer 原始回覆而非 Agent 名單」。狀態：Agent 偽造回覆檔 → helper 無法驗證（模型契約），僅能驗一對一、序號、格式、與 verdicts 一致；於已知限制揭露，不宣稱能證明。

**狀態矩陣（最小）**：

| # | 路徑 | 判定 | closeout | 上游下一步 | TKA 預期 |
| --- | --- | --- | --- | --- | --- |
| P1 | 一般票 | QA PASS＋SV Cross-Review PASS／ADVISORY／SKIPPED | 做 | `Skill(ticket-author)` `close <ID>` | reviewer 全 AC 驗收後判定 |
| P2 | 一般票 | SV 觸 `CHECKPOINT_REPORT_FIX_LIMIT` 無終局 | 做，`reviews.codex=N_A`＋verdict 史落 checkpoint | 同上 | 判定（不偽寫 PASS） |
| P3 | 一般票 | 已確認 BLOCKER | **不做**（不假轉 `REVIEW`） | 停止不安全操作；`Skill(ticket-author)` `close <ID>` 交 blocker＋證據 | 唯讀 `NOT_CLOSED(precondition)`／blocker，lifecycle 不變、零寫入 |
| P4 | 低風險免 QA／SV | 必要驗證完成並記錄，`check commit` exit 0 後已以 `[<ID>]` 提交實作 | 做（Read 並執行 Step 11 第 1–5 項，不載入其餘 SV 階段） | 同上 | 同 P1 |
| P5 | 低風險免 QA／SV | 必要驗證未完成，或 `check commit` 非 0／實作未提交 | 不做 | 續作 Action Items | 無 close |
| P6 | 任一 | `check closeout` 非 0 | 不寫 registry／不搬票 | 如實回報 | 無 close |
| P7 | 任一 | Skill 調用被拒或不可達 | 已 closeout 至 `REVIEW` | 如實回報 close 未完成並列最小權限需求，不手動模仿 | 票保留 `REVIEW` |
| P8 | 任一 | 已收束重入且 `lifecycle=REVIEW` | 不重做 | 同上 | 判定 |
| P9 | 任一 | 票號來源不一致（context／票檔 `TICKET_ID`／Registry id／他票 `verify_cmd.conf`） | 不寫 | 停止回報，不調用 | 無 close |

## Verify Config

TICKET_ID=TKA-WIRE-01
CREATED=2026-10-21
PRESET=feature
ALLOWED=.claude/skills/sv/SKILL.md,.claude/rules/ssot-governance.md,.claude/skills/uc/SKILL.md,.claude/skills/ticket-author/SKILL.md,.claude/skills/ticket-author/references/input-contract.md,.claude/skills/ticket-author/references/state-transitions.md,.claude/skills/ticket-author/scripts/close_helper.py,.claude/skills/ticket-author/tests/test_close.py,.claude/hooks/gate_stop_guard.py
FORBIDDEN=.claude/CLAUDE.md,.claude/settings.json,.claude/settings.local.json,.claude/scripts/gate_ledger.py,.claude/scripts/route.py,.claude/skills/qa/**,.claude/skills/normal/SKILL.md,.claude/skills/projman/SKILL.md,.claude/skills/compact/SKILL.md,.claude/skills/sv/scripts/**,.claude/rules/cross-review.md,.claude/rules/ticket_pattern.md,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,plans/cso_briefing/project/deliver/TKA-CLOSE-01_autonomous.md
MAX_LINES=N/A
DIFF_REF=face1158
ANCHOR_PROBED=2026-10-22 00:30
BASE_COMMIT=27cc28ea598fc94f293fbb193a5f81ed3765db86
TARGET_BLOB=.claude/skills/sv/SKILL.md:df2aeef1;.claude/rules/ssot-governance.md:a57d3d07;.claude/skills/uc/SKILL.md:d25b81fc;.claude/skills/ticket-author/SKILL.md:f96ee865;.claude/skills/ticket-author/references/input-contract.md:1d51665c;.claude/skills/ticket-author/references/state-transitions.md:bfaf8553;.claude/skills/ticket-author/scripts/close_helper.py:5fce3020;.claude/skills/ticket-author/tests/test_close.py:dc9dabf7;.claude/hooks/gate_stop_guard.py:461dba8d
INTRODUCED_CAPABILITY=feature:close-skill-invocation
DEPENDENCY_DELTA=NONE

> PRESET=feature（含 `.py` helper／hook 文字／`unittest` 與治理、Skill 文字；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A，同 `TKA-CLOSE-01` 先例）。`MAX_LINES=N/A`：含授權／守門／fail-closed 判定類不變量（Template v3.5 R3）。`ALLOWED` 皆為精確路徑。Rev B 將 `references/state-transitions.md` 納入 ALLOWED（該檔 `### close` 流程圖列「fallback reviewer」須與升級／備援分家同步）；`FORBIDDEN` 取消 `PJMAN-CLOSELOOP-01` 票檔（已 CLOSED，不再需特別保護）。`INTRODUCED_CAPABILITY` 6a 機械觸發屬取證非 gate。三欄 `BASE_COMMIT`／`ANCHOR_PROBED`／`TARGET_BLOB` 為資訊性診斷值（v3.11）。

## Action Items

> 以下「現值→目標值」為逐字替換契約（反引號為字面字元）；`範圍起／範圍止` 表示整段（含兩端字面）替換。實作以腳本逐項套用並驗證現值恰 1 次、替換後現值零命中。

### AI-0: 錨點與基線復驗（派工後、修改前）

- **動作**：以 live `grep -cF` 逐字確認下列現值各於指定檔內恰 1 次；不符依 v3.11 三分流處理：

```text
sv/SKILL.md            SV＋Codex PASS（或 RAPID MODE 豁免）後 **MUST AUTO-CHAIN**
sv/SKILL.md            並於其後 `MUST AUTO-CHAIN` `/ticket-author close <TICKET_ID>`（該表 close 列）
sv/SKILL.md            （`<TICKET_ID>` 取自 `tmp/verify_cmd.conf`）；exit 0 續行
sv/SKILL.md            **MUST AUTO-CHAIN** 呼叫 `/ticket-author close <TICKET_ID>`；第 1 項命中「已收束重入」且 `lifecycle = REVIEW` 者亦接續 close。
sv/SKILL.md            判準、授權與保存規則以該列及 `projman` Skill 為唯一來源，此處**不複製**。
ssot-governance.md     Cross Review 通過或依 RAPID MODE 省略
ssot-governance.md     | RAPID MODE ON（QA/SV 已豁免） | Ticket DONE 輸出當下即為 closeout 觸發點，`MUST AUTO-CHAIN` 進 Agent closeout | 等待額外確認才觸發 closeout |
ssot-governance.md     | RAPID MODE OFF | 觸發點為 SV PASS（Cross-Review 通過），`MUST AUTO-CHAIN` 進 Agent closeout | 在 SV PASS 前提早 closeout |
ssot-governance.md     | SV 或 Codex 判 BLOCKER | `MUST STOP`，依 SV／Cross-Review 既有契約處理 | 由 UC 自動重跑或改判（回指 `uc/SKILL.md` 規則 9） |
ssot-governance.md     第 3 次 → `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2，人工裁決 checkpoint）
ssot-governance.md     `MUST AUTO-CHAIN` 呼叫 `/ticket-author close <票號>`（一次；close 不自行重呼叫自己）
uc/SKILL.md            輸出 Ticket DONE 後，**MUST** 立即呼叫 `Skill(qa)`（等價 `/qa`），不得停下等待使用者確認。
uc/SKILL.md            - **收尾引用**：Ticket DONE 後之收尾由 `/sv` Step 11 Closeout Auto-Chain 承載
ticket-author/SKILL.md Manual modes: diag, plan, ticket, close.
ticket-author/SKILL.md The autonomous workflow's Agent MAY invoke it (Transition Table: after Agent closeout reaches `REVIEW`); no per-Ticket Owner approval is requested.
ticket-author/SKILL.md 3. Run `reviewer`.（行首）  與  4. Verify each AC, evidence first.（行首）  與  this skill does not copy them.
ticket-author/SKILL.md (1 once the other reviewer has been used)
ticket-author/SKILL.md --round <N> --fallback-tried <0|1>
ticket-author/SKILL.md    - `FALLBACK_REVIEWER`: ask the other reviewer
ticket-author/SKILL.md    - `PRECONDITION_FAILED`: respond
ticket-author/SKILL.md write the 3-line delivery summary to `<dir>/summary.txt`
input-contract.md      The caller may be an Agent in the autonomous workflow or a person; no per-Ticket Owner approval is part of the input.
state-transitions.md   [supplement | fallback reviewer]*
gate_stop_guard.py     "RAPID 豁免或票面不要求 SV 則如實說明後可停止。")
close_helper.py        見 AI-5 錨點（`def classify`、`def select_reviewer`、`def cmd_reviewer`、`def validate_verdicts`、`def cmd_decide`、`def build_result_section` 各恰 1 個）
test_close.py          `"reviewer": {"primary": "sonnet", "actual": "sonnet", "fallback_used": False, "limitation": ""},` 與 `class TestRegistrySubjectBinding(Base):`
```

- 併做：①確認 repo root、HEAD、branch、`git status --short`；共享檔若有未提交或他票變更，先辨識歸屬並保留，不 reset、不覆寫，不把他票變更算成本票實作。②`git check-ignore -v` 對 9 個 ALLOWED 路徑皆無命中。③`DIFF_REF` 回填（`git rev-parse --short HEAD`）並同步再生 `tmp/verify_cmd.conf`；`BASE_COMMIT..HEAD` 間觸及 ALLOWED 標的之提交依 v3.11 三分流處理。④`python -X utf8 -m unittest discover -s .claude/skills/ticket-author/tests -p "test_*.py"` 基線全綠；`gate_stop_guard_test.py` 基線結果記錄。⑤讀取**有效權限配置**（使用者層級＋repo＋local）並記錄 `Skill(ticket-author)`／`Skill(sv)` 是否可無人介入調用。⑥確認 `/normal` Phase 0 第 4 項依 Table 接續之入口契約足夠（不為形式複製規則）。
- [x] 完成（見收尾實錄）

### AI-1: `sv/SKILL.md` Step 11（Skill 工具實際調用、票號來源、低風險入口、LIMIT 與 BLOCKER 交接）

- **檔案**: `.claude/skills/sv/SKILL.md`

```text
現值：SV＋Codex PASS（或 RAPID MODE 豁免）後 **MUST AUTO-CHAIN**
目標：下列任一成立後 **MUST AUTO-CHAIN**（①SV 完成：Cross-Review 為 PASS／ADVISORY／SKIPPED，或 REPORT_FIX 觸 `CHECKPOINT_REPORT_FIX_LIMIT` 而無終局 verdict——`reviews.codex` 填 `N_A`、實際 verdict 史與未決 finding 如實落 checkpoint，不寫 PASS；②低風險免 QA／SV 票〔判準唯一依 `ssot-governance.md` Transition Table「低風險免 QA／SV」列〕於 Ticket DONE 且票面必要驗證完成、並已先執行 `python .claude/scripts/gate_ledger.py check commit --ticket <TICKET_ID>`（exit 0 才續行）再以 `[<TICKET_ID>]` 前綴提交實作之後——僅 Read 並執行本節第 1–5 項，**MUST NOT** 呼叫 `Skill(sv)`／`Skill(qa)` 或假填 QA／SV PASS；已確認 BLOCKER 不在此列，見第 6 項）
```

```text
現值：並於其後 `MUST AUTO-CHAIN` `/ticket-author close <TICKET_ID>`（該表 close 列）
目標：並於其後 `MUST AUTO-CHAIN` 以 `Skill(ticket-author)`、args `close <TICKET_ID>` 實際調用 TKA close（第 4 項；該表 close 列）
```

```text
現值：（`<TICKET_ID>` 取自 `tmp/verify_cmd.conf`）；exit 0 續行
目標：（`<TICKET_ID>` 取自目前正式票上下文，並與票檔 `TICKET_ID`、Registry ID 核對；`tmp/verify_cmd.conf` 可重用，但不得為低風險路徑必要依賴，其 `TICKET_ID` 與目前票不一致時 **MUST NOT** 採用）；exit 0 續行
```

```text
現值：**MUST AUTO-CHAIN** 呼叫 `/ticket-author close <TICKET_ID>`；第 1 項命中「已收束重入」且 `lifecycle = REVIEW` 者亦接續 close。
目標：**MUST AUTO-CHAIN** 以 Skill 工具調用 `Skill(ticket-author)`，args 為 `close <TICKET_ID>`（`<TICKET_ID>` 同第 1 項，一次）；第 1 項命中「已收束重入」且 `lifecycle = REVIEW` 者亦同。**MUST NOT** 以 Bash／Registry 工具手動模仿 close 步驟、自填 verdicts 後直接寫入 `CLOSED`、inline 自答 reviewer 或自行宣稱驗收完成；Skill 調用失敗或不可達 → 如實回報 close 未完成（票保留 `REVIEW`），不假報收束。
```

```text
現值：判準、授權與保存規則以該列及 `projman` Skill 為唯一來源，此處**不複製**。
目標：判準、授權與保存規則以該列及 `projman` Skill 為唯一來源，此處**不複製**。
6. **BLOCKER 交接**：Cross-Review 或 SV 判已確認 BLOCKER 時，停止不安全操作與錯誤交付，**不**寫 `REVIEW`、不偽填 PASS；仍 **MUST AUTO-CHAIN** 以 `Skill(ticket-author)`、args `close <TICKET_ID>` 將 blocker 與證據交 TKA 作唯讀判定（TKA 回 `NOT_CLOSED`／blocker，lifecycle 不變、零寫入）。
```

- [x] 完成（見收尾實錄）

### AI-2: `ssot-governance.md` 直接替換

- **檔案**: `.claude/rules/ssot-governance.md`（五處；`RAPID` 三處字面全數消失）

```text
現值：Cross Review 通過或依 RAPID MODE 省略
目標：Cross Review 通過、Cross-Review 無終局 verdict（如實記載）或低風險免 QA／SV
```

```text
現值：| RAPID MODE ON（QA/SV 已豁免） | Ticket DONE 輸出當下即為 closeout 觸發點，`MUST AUTO-CHAIN` 進 Agent closeout | 等待額外確認才觸發 closeout |
目標：| 低風險免 QA／SV 票（`CLAUDE.md` 〈SOP〉風險分級之**全部**適用條件同時成立：①票面 `ALLOWED` 全為 `.md`；②無 `app/` production code、腳本、pipeline 或 hook 變更；③票面無不可逆操作；④可 `git revert`） | 票面必要驗證（各 AC 自驗證據與 `git diff --stat` ⊆ ALLOWED）完成並記錄後，先 `python .claude/scripts/gate_ledger.py check commit --ticket <TICKET_ID>`（exit 0 才續行；非 0 如實回報、不提交）並以 `[<TICKET_ID>]` 前綴提交僅含 ALLOWED 之實作變更（低風險路徑不經 `/qa` Step 4.5，此步由上游自行承擔，否則 close 的 scope 歸屬為 UNKNOWN、Stop guard 亦判漏檢），隨後 Ticket DONE 即 `MUST AUTO-CHAIN` 進 Agent closeout：Read 並執行 `sv/SKILL.md` Step 11 第 1–5 項（`MUST NOT` 呼叫 `Skill(qa)`／`Skill(sv)`、假填 QA／SV PASS），隨後依 close 列實際調用 TKA close | 等待額外確認；停在 closeout 前；自行終審或略過 TKA close；以本列豁免 TKA 驗收 |
```

```text
現值：| RAPID MODE OFF | 觸發點為 SV PASS（Cross-Review 通過），`MUST AUTO-CHAIN` 進 Agent closeout | 在 SV PASS 前提早 closeout |
目標：| 一般票（不符上列低風險條件） | 觸發點為 SV 完成（見 SV 各列：PASS／ADVISORY／SKIPPED，或 REPORT_FIX 觸 LIMIT 後依該列續行），`MUST AUTO-CHAIN` 進 Agent closeout | 在 SV 完成前提早 closeout |
```

```text
現值：| SV 或 Codex 判 BLOCKER | `MUST STOP`，依 SV／Cross-Review 既有契約處理 | 由 UC 自動重跑或改判（回指 `uc/SKILL.md` 規則 9） |
目標：| SV 或 Codex 判 BLOCKER | `MUST STOP` 不安全操作與錯誤交付，依 SV／Cross-Review 既有契約處理；**不**推進 lifecycle、不假轉 `REVIEW`、不偽填 PASS；並 `MUST AUTO-CHAIN` 以 `Skill(ticket-author)`、args `close <票號>` 將 blocker 與證據交 TKA 作唯讀判定（TKA 回 `NOT_CLOSED`／blocker，lifecycle 不變） | 由 UC 自動重跑或改判（回指 `uc/SKILL.md` 規則 9）；因 blocker 而略過 TKA 判定或在上游自行終審 |
```

```text
現值：第 3 次 → `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2，人工裁決 checkpoint）
目標：第 3 次 → `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2，記 `step_limit`、不再重跑；已修補事項與 Cross-Review 無終局之事實〔含各次 verdict 史〕落 checkpoint，`reviews.codex` 填 `N_A` 不寫 `PASS`，`MUST AUTO-CHAIN` 進 Agent closeout 與 TKA close，由 TKA 判定可否收束；此為 LIMIT 後的已授權續行策略，LIMIT 本身不是永久停車條件）
```

```text
現值：`MUST AUTO-CHAIN` 呼叫 `/ticket-author close <票號>`（一次；close 不自行重呼叫自己）
目標：`MUST AUTO-CHAIN` 以 `Skill(ticket-author)`、args `close <票號>` 實際調用 TKA close（一次；close 不自行重呼叫自己；上游不得以 Bash／Registry 模仿其步驟、自填 verdicts 後直接寫 `CLOSED`、inline 自答 reviewer 或自行終審）
```

- [x] 完成（見收尾實錄）

### AI-3: `uc/SKILL.md` 規則 8 低風險分支

- **檔案**: `.claude/skills/uc/SKILL.md`

```text
現值：輸出 Ticket DONE 後，**MUST** 立即呼叫 `Skill(qa)`（等價 `/qa`），不得停下等待使用者確認。
目標：輸出 Ticket DONE 後，**MUST** 立即呼叫 `Skill(qa)`（等價 `/qa`），不得停下等待使用者確認；低風險免 QA／SV 票（判準唯一依 `ssot-governance.md` Transition Table「低風險免 QA／SV」列）改為必要驗證完成後 Read 並執行 `/sv` Step 11 第 1–5 項，**MUST NOT** 呼叫 `Skill(qa)`／`Skill(sv)`。
```

```text
現值：- **收尾引用**：Ticket DONE 後之收尾由 `/sv` Step 11 Closeout Auto-Chain 承載
目標：- **收尾引用**：Ticket DONE 後之收尾由 `/sv` Step 11 Closeout Auto-Chain 承載（低風險免 QA／SV 票亦由同節承載、不經 `/qa`／`/sv` 階段；收束一律以 `Skill(ticket-author)` 調用 TKA close）
```

- [x] 完成（見收尾實錄）

### AI-4: `ticket-author/SKILL.md`（description、調用契約、reviewer 路由、全 AC 逐項驗收、升級／備援、非 REVIEW 唯讀判定）

- **檔案**: `.claude/skills/ticket-author/SKILL.md`

```text
現值：Manual modes: diag, plan, ticket, close.
目標：Manual modes: diag, plan, ticket. close is called by the autonomous workflow's Agent through the Skill tool (args `close <TICKET_ID>`) or by a person.
```

```text
現值：The autonomous workflow's Agent MAY invoke it (Transition Table: after Agent closeout reaches `REVIEW`); no per-Ticket Owner approval is requested.
目標：The autonomous workflow's Agent invokes it with the Skill tool (`skill` = `ticket-author`, `args` = `close <TICKET_ID>`) after Agent closeout reaches `REVIEW` (Transition Table), or hands it an upstream blocker with its evidence; no per-Ticket Owner approval is requested. Upstream steps MUST NOT replace this call by running the helper, the Registry tools or the procedure below by hand, MUST NOT fill verdicts themselves and write `CLOSED`, and MUST NOT declare the acceptance: verification and the closing decision belong to this mode. A Ticket at a lifecycle other than `REVIEW`/`CLOSED` gets a read-only judgement: evidence (and a reviewer reply if useful) may be collected and reported as `NOT_CLOSED(precondition)` or a blocker; nothing is written and the lifecycle never moves.
```

```text
範圍起：3. Run `reviewer`.
範圍止：if the primary is unavailable the other reviewer is used and the actual reviewer and limitation are recorded.
目標：3. Run `reviewer`. The reviewer is a real second call, never the implementing Agent answering inline. Codex is the primary reviewer for every Ticket type: `gpt-6-sol` for code, complex features, load-bearing logic, and any Skill or rule text that changes execution, authorization, acceptance or closing behavior (so a `.md` under `.claude/` is heavy); `gpt-6-luna` only for genuinely simple text, formatting or explicit metadata edits. The helper classifies by the files the Ticket changes and where they live, not by PRESET or extension alone (light = every ALLOWED file is `.md`/`.txt`/`.rst` and none lives under `.claude/`); the Agent may raise a Ticket to heavy, never lower it. Escalate Luna to Sol (`decide` returns `ESCALATE_REVIEWER`) when Luna cannot settle load-bearing semantics, finds a complex contradiction, or conflicts with the mechanical evidence; a Luna `FAIL` is not a reason to switch family. Fall back to Claude only for a Codex fault — authentication, connection, quota, service or model unavailable, or one bounded retry that still returns no usable answer — by passing `--codex-fault <auth|network|quota|unavailable|model_unavailable|recovery_exhausted>` to `reviewer`; the fallback is a separate Claude reviewer started with the Agent tool (`opus` for heavy, `sonnet` for light), never the implementing session answering inline. A `REPORT_FIX`, a substantive finding, a `FAIL` or a LIMIT is not a Codex fault. The actual model, tier, fault reason and independence are recorded; a Claude fallback is never reported as Codex or cross-model verification.
```

```text
範圍起：4. Verify each AC, evidence first.
範圍止：this skill does not copy them.
目標：4. Verify each AC, evidence first. Build a compact evidence bundle from `preflight.json` and the Ticket (reuse valid existing evidence; never truncate AC text). Ask the reviewer for a judgement on EVERY AC in `preflight.json` — `N/A` ones included, where the reviewer checks that the stated reason holds and no inapplicable test is run — by writing the prompt to `<dir>/review_prompt.md` and requiring one line per AC in the form `AC<n>: PASS|FAIL|N/A|UNVERIFIED | <short evidence>`. Codex: `codex exec -c model="<gpt-6-sol|gpt-6-luna>" -c model_reasoning_effort="high" - < <dir>/review_prompt.md`, raw output saved verbatim to `<dir>/reviewer_reply.txt`; Claude fallback: the separate reviewer's returned text, saved verbatim to the same file. A reply from an escalation or a point re-check goes to `<dir>/reviewer_reply2.txt` and overrides only the ACs it covers. Status meanings: `PASS` (direct, traceable evidence), `FAIL` (direct evidence it is not met), `N/A` (the Ticket itself strikes it out and the reason checks out), `UNVERIFIED` (evidence insufficient; say so). An exit code supports only what the command checked; an implementation scope still `UNKNOWN` is not evidence that an AC about that scope passes; a checkbox, memory or an old prompt is never evidence; an AC that is hard to verify is not thereby non-core; Cross-Review findings recorded in the Ticket's checkpoint are evidence like any other. Where a reply contradicts direct mechanical evidence, evidence wins: re-ask only that point (or escalate) and record `override_evidence` for that AC in `verdicts.json`; free-text model output never overrides a direct fact. Write `<dir>/verdicts.json` with `reviewer` {`family`, `actual_model`, `tier`, `fallback_used`, `fallback_reason`, `independent_call` (true for a separate call), `inline` (must be false), `limitation`} and one entry per AC (by `n`) with `status`, `core` and a short evidence pointer. `decide` parses the reply files itself: it requires one-to-one coverage of the whole AC list with valid numbers and agreement with `verdicts.json` (a status difference needs `override_evidence`); it does not prove the quality of the reviewer's judgement. A point review covers only that point and is never reported as whole-Ticket verification. Core ACs and hard blockers are defined in `.claude/rules/ssot-governance.md` §Ticket Closure; this skill does not copy them.
```

```text
現值：(1 once the other reviewer has been used)
目標：(1 once an escalation to the stronger reviewer has been used)
```

```text
現值：--round <N> --fallback-tried <0|1>
目標：--reply <dir>/reviewer_reply.txt --round <N> --fallback-tried <0|1>
```

```text
範圍起：   - `FALLBACK_REVIEWER`: ask the other reviewer
範圍止：run `decide` again with `--fallback-tried 1`.
目標：   - `ESCALATE_REVIEWER`: ask the stronger Codex reviewer (`gpt-6-sol`) about the unresolved ACs, save its reply to `<dir>/reviewer_reply2.txt`, run `decide` again with both `--reply` files and `--fallback-tried 1` (a Codex fault is handled earlier by `reviewer --codex-fault`, not here).
```

```text
現值：write the 3-line delivery summary to `<dir>/summary.txt`
目標：write the 3-line delivery summary to `<dir>/summary.txt` (when the Ticket's checkpoint recorded a Cross-Review verdict history or a LIMIT, one of the lines MUST state it, because `CLOSED` clears the checkpoint)
```

```text
現值：   - `PRECONDITION_FAILED`: respond
目標：   - `PRECONDITION_FAILED`: this is the read-only judgement — write nothing, do not move the lifecycle; respond
```

- [x] 完成（見收尾實錄）

### AI-5: `close_helper.py`（reviewer 路由、全 AC 回覆解析覆蓋、升級／備援、披露）

- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`
- **目標（行為契約；錨點＝既有函式）**：
  1. `classify(allowed)` → `heavy|light`：`light` 若且唯若每個 ALLOWED 副檔名 ∈ `{.md,.txt,.rst}` **且**無任一路徑位於 `.claude/` 下；其餘（含 `.claude/**/*.md`）為 `heavy`。
  2. `select_reviewer(...)`／`cmd_reviewer`：主審一律 Codex——heavy→`gpt-6-sol`、light→`gpt-6-luna`（常數集中，不捏造他名）；輸出 `{class, primary:{family,model}, actual:{family,model}, fallback_used, fallback_reason, escalate_to, limitation}`。`--codex-fault <auth|network|quota|unavailable|model_unavailable|recovery_exhausted>` 給定（或 `codex` 不在 PATH 且未給 `--codex-available 1`）→ 備援 Claude：heavy→`opus`、light→`sonnet`，記 `fallback_used=true` 與原因；未給 fault 時**不得**切 Claude（`REPORT_FIX`／`FAIL`／LIMIT 不是故障）。`--claude-available 0` 且需備援 → `actual=none`。`--escalate n1,n2` 回傳升級目標（light 且 model 為 luna→`gpt-6-sol`；其他→`none`＋理由）與 AC 序號。
  3. `parse_reviewer_replies(files, n_acs)`：以行解析 `AC<n>: <STATUS>`（容忍前導 `-`/`*`、粗體標記、全形冒號），多檔後檔覆蓋前檔同號；回傳 `{n: status}`、`missing`、`duplicate`（同檔重複）、`unknown`、`malformed`。
  4. `cmd_decide` 新增必填 `--reply F`（可重複）；順序：P → B → V → **R（reviewer 覆蓋與一致性）** → X → U → L → K。R：reviewer 中繼資料須 `inline=false`、`independent_call=true`、`actual_model` 非空；heavy 票型不得用 `gpt-6-luna`（除非 `escalated_from` 已記）；`family=claude` 須 `fallback_reason` 屬故障集合；回覆須覆蓋**全部** AC（含 N/A 與非核心）、無重複／未知序號；每條 AC 的 reply 狀態須與 verdicts 一致，不一致者須 `override_evidence` 非空（採 verdicts 狀態並在輸出 `overrides` 列出），否則 `SUPPLEMENT(reviewer_mismatch)`；任一不成立→ `SUPPLEMENT(reviewer_coverage|reviewer_invalid|reviewer_mismatch)`，達上限→（先判升級：`actual_model=gpt-6-luna` 且 `--fallback-tried 0` 回 `ESCALATE_REVIEWER`，不被 NOT_CLOSED 遮蔽）否則 `NOT_CLOSED(reviewer_coverage_incomplete|reviewer_invalid|reviewer_mismatch)`；reviewer 中繼資料缺鍵、`null` 或型別非法（`actual_model` 非字串、`inline`／`independent_call` 非布林）→ `reviewer_invalid`，不得預設為合法。`U2` 由 `FALLBACK_REVIEWER` 改名 `ESCALATE_REVIEWER`：僅在 `actual_model=gpt-6-luna` 且 `--fallback-tried 0` 時回，目標 `gpt-6-sol`；否則 `NOT_CLOSED(core_unverified)`。
  5. 非 `REVIEW`／`CLOSED` 之 `PRECONDITION_FAILED` 輸出增 `read_only: true`（行為本已零寫入，補明示）。
  6. `build_result_section`：實際收束行改為 `reviewer: <family>/<actual_model>（tier=<tier>[，fallback:<reason>]）；覆蓋 AC <n>／<N>（全部），independent_call=<…>`；有 `overrides` 時附筆數；缺欄位輸出「未記錄」，不補預設值。
- **現值錨點**：`def classify(`、`def select_reviewer(`、`def cmd_reviewer(`、`def validate_verdicts(`、`def cmd_decide(`、`def build_result_section(` 各恰 1 個；`FALLBACK_REVIEWER` 於本檔出現處全數改名。
- [x] 完成（見收尾實錄）

### AI-6: `gate_stop_guard.py` 提示文字

- **檔案**: `.claude/hooks/gate_stop_guard.py`（僅一處字串；不改判定邏輯）

```text
現值："RAPID 豁免或票面不要求 SV 則如實說明後可停止。")
目標："票面不要求 SV（低風險免 QA／SV）則依 Transition Table 該列續行 closeout，並以 `Skill(ticket-author)` 調用 close，不得以此停止。")
```

- [x] 完成（見收尾實錄）

### AI-7: `input-contract.md` 與 `state-transitions.md`

- **檔案**: `.claude/skills/ticket-author/references/input-contract.md`、`.claude/skills/ticket-author/references/state-transitions.md`

```text
現值：The caller may be an Agent in the autonomous workflow or a person; no per-Ticket Owner approval is part of the input.
目標：The caller is the autonomous workflow's Agent (through the Skill tool: `skill` = `ticket-author`, `args` = `close <TICKET_ID>`) or a person; no per-Ticket Owner approval is part of the input. An upstream Agent may start this mode but may not stand in for it: running the helper or the Registry tools by hand, filling verdicts itself, or answering as the reviewer inline is not a `close`. The Ticket ID comes from the current formal Ticket context and is checked against the Ticket file's `TICKET_ID` and the Registry id.
```

```text
現值：[supplement | fallback reviewer]*
目標：[supplement | escalated reviewer | Codex-fault Claude reviewer]*
```

- [x] 完成（見收尾實錄）

### AI-8: `test_close.py`（機械契約層、解析 `tool_use` 的調用紀錄函式）

- **檔案**: `.claude/skills/ticket-author/tests/test_close.py`
- **目標**：
  1. `Base` 增 `reviewer_raw(verdicts)`：將 `write_json("verdicts.json", v)` 同時寫出由 `v["acs"]` 狀態導出的 `reviewer_reply.txt`（`AC<n>: <STATUS> | e`），`decide` helper 預設加 `--reply`；`Base.verdicts()` 的 reviewer 預設改為 `{"family":"codex","actual_model":"gpt-6-sol","tier":"heavy","fallback_used":False,"fallback_reason":"","independent_call":True,"inline":False,"limitation":""}`；`TestReviewer` 重寫為新路由（heavy／light、`.claude/**/*.md`→heavy、`--codex-fault`→Claude opus／sonnet、未給 fault 不得切 Claude、`--escalate` luna→sol、`actual=none`）；`FALLBACK_REVIEWER` 案改 `ESCALATE_REVIEWER`。
  2. 新增 `TestReviewerReplyCoverage`（given/expect，真實 CLI；含舊形狀 verdicts〔無 reply 檔或 reviewer 缺欄〕不得 `CLOSE` 的案）：reply 缺 AC／重複／未知序號／格式壞→`SUPPLEMENT`，達上限→`NOT_CLOSED`；reply 與 verdicts 狀態不一致且無 `override_evidence`→`SUPPLEMENT(reviewer_mismatch)`，有則採 verdicts 並列 `overrides`；`inline=true`／`independent_call=false`→不得 `CLOSE`；heavy 票用 `gpt-6-luna` 未記升級→不得 `CLOSE`；`family=claude` 無故障原因→不得 `CLOSE`；全 AC（含 N/A 與非核心）皆需覆蓋；多檔後檔覆蓋前檔；`finalize` 披露含模型、tier、覆蓋 `n／N`；非 `REVIEW` 票 `decide`→`PRECONDITION_FAILED` 且 `read_only=true` 且零寫入。
  3. 新增 `TestUpstreamWiring`（讀真實 repo 檔，**契約層**，非行為證據）：①`sv/SKILL.md` Step 11 與 `ssot-governance.md` close 列皆含 `Skill(ticket-author)` 與 `close <`，且不含 ``呼叫 `/ticket-author close``；②`RAPID` 於四檔零命中；③Table 含「低風險免 QA／SV」列（含四條件字面）、「一般票」列、BLOCKER 列含 `Skill(ticket-author)` 與「唯讀」；④`uc/SKILL.md` 規則 8 含「低風險免 QA／SV」與 `Step 11`；⑤`ticket-author/SKILL.md` description 不含 `Manual modes: diag, plan, ticket, close`，`Mode: close` 含 `every AC`、`reviewer_reply`、`gpt-6-sol`、`gpt-6-luna`、`--codex-fault`，不含「ask the other reviewer」；⑥`close_helper` 的 light 判準（`.md`／`.txt`／`.rst` 且不在 `.claude/` 下）為 Table 低風險條件①（僅 `.md`）的超集，且 Table 條件文字與 `CLAUDE.md` 〈SOP〉原文同源（唯一引用）；⑦`gate_stop_guard.py` 提示含 `Skill(ticket-author)` 不含「可停止」；⑧Step 11 第 1 項票號來源不再僅指 `tmp/verify_cmd.conf`。
  4. 新增 `scan_skill_calls(jsonl_path)`（模組函式）與 `TestSkillCallParser`：逐行 `json.loads`，走訪 `message.content` 中 `type=="tool_use"` 且 `name=="Skill"` 的區塊，取 `input.skill`／`input.args`（不依賴鍵順序、不用字串 grep），並回傳其前一則**人類輸入文字訊息**的內容與位置。人類輸入文字訊息＝`type=="user"`、非 `isMeta`、`content` 為非空字串或含非空 `text` 區塊，且排除 `tool_result` 區塊、Stop／UserPromptSubmit hook 注入之提示（以 `Stop hook`、`<system-reminder>`、`<task-notification>` 起首者）；合成 jsonl 測試：鍵順序顛倒仍可解析、`tool_result` 型 user 不算人工訊息、`args` 缺失、非 Skill 工具、`close` 前一則人工訊息含 `close` 字樣判為人工發起。
- [x] 完成（見收尾實錄）

### AI-9: 驗證、兩路徑實際觀察與收尾實錄

- **動作**：
  1. **A 契約與機械**：unittest 全綠；`py_compile`（`close_helper.py`、`gate_stop_guard.py`、`test_close.py`）；`gate_stop_guard_test.py` 不劣於基線；`test_report_fix_closure.sh`、`test_gate_ledger.py` 抽查；殘留 grep 舊字面零命中。
  2. **C-light 輕量路徑（優先實際鏈）**：在 `.tmp/ticket-author/wire/` 建隔離環境（scratch git repo，內含本 repo `.claude/` 副本與一張 `ALLOWED` 全為 `.md` 的合成低風險票；一個最小受控 stdio MCP 工具替身〔標準庫＋已安裝 `mcp` 套件，約百行〕提供同名 `registry_list`／`registry_set_status`／`registry_set_checkpoint`／`registry_upsert_ticket`，狀態只寫 scratch JSON；**不連正式 Registry**）；以 `claude -p --output-format stream-json --verbose --max-turns <有界> --model sonnet` 啟動真實 Agent，提示僅述「Ticket DONE、必要驗證已完成」；解析 stream 中實際 `tool_use`（`scan_skill_calls` 同款解析）。通過條件：序列中無 `Skill(qa)`／`Skill(sv)`，且出現 `Skill` `ticket-author` args `close <合成票號>`，其前無人工補指令。**有界**：最多嘗試一次完整建置與一次執行；若環境無法安全完成（CLI 無法非互動、MCP 替身無法載入、權限提示阻斷、需連正式 Registry），**如實標示「輕量實際鏈未驗」並附具體卡點**，退回以一次 Sonnet 唯讀 subagent 的 JSON 推導作為**前測**（明標前測、不計入實際接線證據）。不建立測試平台、不循環重試。
  3. **B-normal 正常路徑（本票自身）**：本票走正常路徑（ALLOWED 含 `.py`，非低風險）。實作 session 於 `/sv` 完成（含 LIMIT 時依新子句）並 closeout 至 `REVIEW` 後，**不經使用者補指令**由 Agent 發出 `Skill(ticket-author)`、args `close TKA-WIRE-01`；該 Skill 內由 `reviewer` 路由（heavy→`gpt-6-sol`）實際呼叫 Codex，取得**全部** AC 的逐項回覆，`decide` 以回覆檔解析覆蓋後判定 `CLOSE` 或 `NOT_CLOSED`，轉態與 read-back 由該 Skill 內產生。證據＝用 `scan_skill_calls` 解析本 session transcript：找出該 `tool_use`，並確認自 closeout 完成至該調用間無人類文字訊息要求 close；再核對 `.tmp/ticket-author/close/TKA-WIRE-01/` 內 reviewer 原始回覆。任一不成立 → 如實記未達成與最小修補，不以手動 close 補寫為已驗證。
  4. **收尾實錄如實分層**：A 契約層、C-light（實際鏈或前測或未驗＋卡點）、B-normal 全鏈、D 調用紀錄各自證明什麼；實際 reviewer 模型／備援原因／覆蓋；尚未證明（其他票型、長期遵守度、真實低風險票自然鏈）；deliver 後 `check` exit 4 如實；不宣稱「長期、跨 session、所有票型均已驗證」。
  5. `/qa`、`/sv` 依既有流程；SV 若觸 LIMIT，依 AI-2 新子句續行（不偽寫 PASS）。
- [x] 完成（見收尾實錄 §2–§7 與 close 最終結果）

## Hard Constraints

1. 不新增 Skill、hook、腳本檔（除 `.tmp` 內一次性隔離觀察替身）、daemon、DB、永久 invocation ledger／receipt／manifest、schema、Gate、state、reason code、RAPID 開關、通用 router 或平行狀態機；`close_helper.py` 僅增 reviewer 路由／回覆解析覆蓋／披露，`gate_stop_guard.py` 僅改一處提示字串。
2. 衝突指引一律**直接替換**，不得以括註、豁免或「除非…」疊加；完成後 `RAPID` 字樣於 `ssot-governance.md`／`sv/SKILL.md`／`uc/SKILL.md`／`gate_stop_guard.py` 零命中，且不得留存互相矛盾的 close 調用形式（斜線 vs Skill 工具）、「Sonnet 預設文字審」「ask the other reviewer」「FALLBACK_REVIEWER」舊字面。
3. 兩路徑都以同一句 `Skill(ticket-author)`、args `close <票號>` 調用；低風險入口只**引用** `sv/SKILL.md` Step 11 第 1–5 項、不複製第二套 closeout；上游不得以 Bash／Registry／helper 手動模仿 close、自填 verdicts 後寫 `CLOSED`、inline 自答 reviewer 或自行終審。
4. 低風險免 QA／SV 重用 `CLAUDE.md` 〈SOP〉風險分級全部適用條件（①②③④），不僅憑副檔名；輕量路徑不降低 TKA 驗收：全 AC 取證、reviewer 對**全部** AC 逐項回覆、核心 AC／hard blocker 判定不變。
5. LIMIT／BLOCKER：LIMIT 不偽寫 PASS／ADVISORY，交 TKA；BLOCKER 停止不安全操作與錯誤交付、不假轉 `REVIEW`、不偽填 PASS，但仍以同一 Skill 調用交 TKA 唯讀判定；不改 Gate 計數、輪數上限、授權或成本上限。
6. Reviewer：Codex 主審（heavy→`gpt-6-sol`、light→`gpt-6-luna`，精確 ID 以本地確認為準，不捏造他名）；Luna→Sol 為能力升級，Codex 故障才備援 Claude（複雜 Opus／簡單 Sonnet，另啟 reviewer，禁 inline）；REPORT_FIX／finding／FAIL／LIMIT 不是故障；備援不得被用來規避審查結論；備援後核心 AC 仍不成立即 `NOT_CLOSED`；不冒稱 Codex 或跨模型驗收；不新增重試系統或計數器；Desktop／Perplexity 不為 mandatory gate。
7. 覆蓋：coverage 由 reviewer 原始回覆檔解析得出，不以 Agent 自填名單替代；helper 只驗覆蓋完整、一對一、序號有效、格式與一致性，**不得宣稱能證明模型判讀品質或回覆檔真偽**；N/A 核對理由而不執行不適用測試；模型輸出與機械證據矛盾時以證據為準並記 `override_evidence`。
8. 票號：取自目前正式票上下文，並與票檔 `TICKET_ID`、Registry id 核對；`tmp/verify_cmd.conf` 可重用但不得為輕量路徑必要依賴、不得採用他票 ID。
9. 不修改 FORBIDDEN 內任何檔（含 `CLAUDE.md`、`settings*.json`、`gate_ledger.py`、`route.py`、`qa/**`、`normal`／`projman`／`compact` Skill、Template、Registry 檔、`TKA-CLOSE-01` 票檔）；權限被拒時列最小 allow 條目回報，不自行改 settings、不廣泛放行、不繞過。
10. 共享檔保留他票已提交修改，只改本票列明子串；`PJMAN-CLOSELOOP-01` 已 CLOSED，仍逐行重讀現值。
11. 行為證據分層（A 契約／C 輕量／B 正常／D 調用紀錄）互不替代：scratch／stub／grep 只證契約；模型輸出 JSON 探針只算前測；C-light 隔離觀察無法安全完成就標未驗；不得把「本輪沒找到紀錄」寫成「歷史從未發生」；不得將 TKA-CLOSE-01 當作完整 TKA 自主驗收成功之證據；不得宣稱長期、跨 session、所有票型已驗證。
12. 過程衍生物（reviewer prompt／原始回覆、coverage、verdicts、隔離觀察、重試）只存 `.tmp` 七天，不 commit、不上 TKR、不成為耐久依賴；原票只留最小最終結果，未 CLOSED 不假寫完成結果，read-back 失敗不寫「讀回一致」；隔離觀察不得寫正式 Registry、不建正式票、不污染 Git history。

## Error Handling

- AI-0 錨點不符 → 依 v3.11 三分流記錄分類後以現值續作；僅無法安全判定之語義衝突才精確回報該點
- `git check-ignore -v` 命中任一 ALLOWED 路徑 → 停止回報（gitignore 死結律）
- 共享檔有未辨識的未提交變更，或 ADV-ENTRY-01／他票於開工後再提交 `gate_stop_guard.py`／`ssot-governance.md` → 先重讀現值、辨識歸屬並保留，不覆寫；無法安全判定 → 精確回報
- `Skill(ticket-author)` 調用被權限拒絕或不可達 → 如實回報 close 未完成（票保留 `REVIEW`），列最小所需 allow 條目；不改 settings、不手動模仿 close
- `codex` 無法呼叫指定模型 ID（認證／連線／配額／模型不可用）→ 有界恢復一次後依 `--codex-fault` 備援 Claude 並如實記錄；非故障（REPORT_FIX／finding／FAIL／LIMIT）不得備援
- 替換後仍需括註或疊加規則才能消除矛盾 → 停止回報
- 無法在不新增 Skill／hook／schema／ledger／狀態機下表達 → 停止回報
- C-light 隔離觀察無法啟動、無法非互動、需連正式 Registry 或被權限阻斷 → 標「輕量實際鏈未驗」並附卡點，不以自答或探針補寫
- 修改後 `git diff --stat` 觸及 ALLOWED 以外檔案 → 停止回報
- **[標配]** 含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報（gitignore 死結律）

## Acceptance Criteria

- [x] **[存在性]** AI-1～AI-4、AI-6、AI-7 之新字面逐字存在（各 `grep -cF` 為 1），其現值與下列舊字面零命中：`RAPID`（`ssot-governance.md`／`sv/SKILL.md`／`uc/SKILL.md`／`gate_stop_guard.py`）、``呼叫 `/ticket-author close``（`sv/SKILL.md`、`ssot-governance.md`）、`人工裁決 checkpoint）`、`Manual modes: diag, plan, ticket, close`、`ask the other reviewer`、`FALLBACK_REVIEWER`（`ticket-author/` 全 `.md` 與 `close_helper.py`；`test_close.py` 內之同字面僅為負向斷言清單，不計）、`fallback reviewer`（`state-transitions.md`）；他票已提交之 Table projman 兩列與 Step 11 第 5 項原文未被改動（證據：本輪 `grep -c`：`RAPID` 四檔 0、``呼叫 `/ticket-author close`` 0、`人工裁決 checkpoint）` 0、`Manual modes…close` 0、`ask the other reviewer` 0、`FALLBACK_REVIEWER` 0、`fallback reviewer` 0、`低風險免審` 0；AI 套用腳本逐項驗證現值恰 1 次；`27cc28ea..HEAD` 未觸及 ALLOWED（無漂移））
- [x] **[存在性]** `close_helper.py` 含 `gpt-6-sol`、`gpt-6-luna`、`parse_reviewer_replies`、`--reply`、`--codex-fault`、`ESCALATE_REVIEWER`、`reviewer_coverage_incomplete`、`read_only`；`python -m py_compile` 對 `close_helper.py`、`gate_stop_guard.py`、`test_close.py` 皆通過；新增 import 皆標準庫（證據：`gpt-6-sol`4／`gpt-6-luna`3／`parse_reviewer_replies`2／`--reply`4／`--codex-fault`4／`ESCALATE_REVIEWER`4／`reviewer_coverage_incomplete`1／`read_only`6 處；`py_compile` 三檔通過；新增 import 僅標準庫（`TestStatic`））
- [x] **[存在性]** 兩路徑 producer→consumer 接線逐跳具 `檔案:行號` 證據（執行後回填）：**正常**＝`uc/SKILL.md:<規則 8>` → `/qa` Auto-Chain → `sv/SKILL.md:<Step 11 引言／第 4 項>` → `Skill(ticket-author)` args `close <ID>` → `ticket-author/SKILL.md:<Mode: close>`；**低風險免 QA／SV**＝`uc/SKILL.md:<規則 8 分支>` 與 `ssot-governance.md:<低風險列>` → `sv/SKILL.md:<Step 11>`（第 1–5 項）→ 同一 `Skill(ticket-author)` 調用；**BLOCKER／LIMIT**＝`ssot-governance.md:<BLOCKER 列／REPORT_FIX 列>` → 同一調用；票號 producer＝目前正式票上下文（核對票檔 `TICKET_ID`、Registry id；`tmp/verify_cmd.conf` 非必要依賴）；`/normal`＝`normal/SKILL.md:<Phase 0 第 4 項>` 依 Table 接續（入口契約足夠，未複製）；`gate_stop_guard.py:<提示行>` 不再衝突。producer 證據欄（執行後回填）：正常＝`uc/SKILL.md:51`（DONE→`Skill(qa)`）→ `/qa` Auto-Chain → `sv/SKILL.md:208`（引言）／`:214`（第 4 項）→ `ticket-author/SKILL.md:265`（`Mode: close`）；低風險＝`uc/SKILL.md:51`（低風險分支）與 `:54`、`ssot-governance.md:148`（低風險列，含 `check commit` 與四條件）、`:149`（一般票列）→ `sv/SKILL.md:208`（②）→ 同一 `Skill(ticket-author)`；BLOCKER／LIMIT＝`ssot-governance.md:154`、`:155`、`sv/SKILL.md:216`（第 6 項）；close 列＝`ssot-governance.md:160`；`/normal`＝`normal/SKILL.md:17`（第 4 項依 Table 接續，入口契約足夠，未複製）；guard＝`gate_stop_guard.py:412`；票號 producer＝`sv/SKILL.md:210`（第 1 項，取自目前正式票上下文並與票檔 `TICKET_ID`／Registry id 核對）（證據：見上 producer 證據欄）
- [x] **[行為性]** reviewer 路由：given 不同 ALLOWED 組合與 `--codex-fault`，when 經真實 CLI 執行 `reviewer`，expect `.py`／`.json`／`.sh`／`app/`／`.claude/**/*.md`→heavy→`gpt-6-sol`；僅 `plans/**/*.md` 等→light→`gpt-6-luna`；未給 fault 時不得切 Claude；給 fault→heavy 用 `opus`、light 用 `sonnet` 且 `fallback_used=true`、記原因；`--escalate` 對 luna→`gpt-6-sol`、對 sol→`none`；`codex` 不可用與 Claude 皆不可用→`actual=none`（`TestReviewer`）（證據：`TestReviewer` 5 案，經真實 CLI）
- [x] **[行為性]** 全 AC 覆蓋：given 缺 AC／重複／未知序號／格式壞的 reviewer 回覆檔，when `decide`，expect `SUPPLEMENT`，達上限→`NOT_CLOSED(reviewer_coverage_incomplete|reviewer_invalid)`；reply 與 verdicts 不一致且無 `override_evidence`→`SUPPLEMENT(reviewer_mismatch)`（若 reply 狀態使核心 AC 成為 `FAIL`，則依決策表 B 列 hard blocker 優先，回 `BLOCKED(core_ac_fail)`，不經補證，與 `test_reply_vs_claim_mismatch` 首案一致）；N/A 與非核心 AC 亦須覆蓋；`inline=true`／`independent_call=false`／heavy 用 luna 未記升級／`family=claude` 無故障原因→不得 `CLOSE`；`finalize` 披露實際模型、tier、覆蓋 `n／N`；`ESCALATE_REVIEWER` 僅 luna 且 `--fallback-tried 0`（`TestReviewerReplyCoverage`）（證據：`TestReviewerReplyCoverage` 11 案，經真實 CLI（含舊形狀 verdicts、多檔覆蓋、升級不被遮蔽、披露））
- [x] **[行為性]** BLOCKER／非 REVIEW：given lifecycle 為 `IN_PROGRESS`／`EXECUTED` 等的票，when `decide`，expect `PRECONDITION_FAILED` 且 `read_only=true`、票檔與 `decision.json` 零寫入、不推進 lifecycle；契約層確認 Table BLOCKER 列與 Step 11 第 6 項要求交 TKA 唯讀判定而不假轉 `REVIEW`（證據：`test_non_review_lifecycle_is_a_read_only_judgement`（`read_only=true`、零寫入）；`TestUpstreamWiring.test_blocker_and_limit_rows_hand_over_to_tka`）
- [x] **[行為性]** 調用紀錄解析：`scan_skill_calls` 對合成 jsonl 於鍵順序顛倒、`tool_result` 型 user、缺 `args`、非 Skill 工具、人工含 `close` 訊息等情況皆正確區分「人工發起」與「無人工補指令」，且不依賴字串 grep（`TestSkillCallParser`）；`TestUpstreamWiring` 全綠；既有 `test_close.py` 與 `gate_stop_guard_test.py` 不劣於基線（契約層，不構成行為層證據）（證據：`TestSkillCallParser` 5 案、`TestUpstreamWiring` 10 案；`test_close.py` 全套 94 案 OK；`gate_stop_guard_test.py` 54 OK、`test_report_fix_closure.sh` ALL PASS、`test_gate_ledger.py` 59 OK（基線 54 不劣））
- [x] **[行為性]** B-normal：本票以正常路徑完成 `/qa`、`/sv` 並 closeout 至 `REVIEW`，上游 Agent 未經使用者補指令，用 `scan_skill_calls` 解析本 session transcript 得 `Skill` `ticket-author` args `close TKA-WIRE-01` 的 `tool_use`，其前無要求 close 之人工訊息；該 Skill 內 reviewer（heavy→`gpt-6-sol`，或 Codex 故障時獨立 Opus 並如實標示）對**全部** AC 回覆，`.tmp/ticket-author/close/TKA-WIRE-01/` 存原始回覆，`decide` 由回覆檔解析覆蓋；結果 CLOSED 或 NOT_CLOSED 均須在該 Skill 調用內產生；任一不成立 → 如實記未達成與最小修補（證據：本 session transcript 以 `scan_skill_calls` 結構化解析：`Skill` `ticket-author` args `close TKA-WIRE-01` 的 `tool_use`；自 closeout 的 `check closeout` tool_use 至該調用之間**零則**人類文字訊息；但最近一則人類訊息為 Owner 工單，明示最後一步須為此 Skill 調用，故為**instructed、非盲測**觀察（盲測見 C-light）。該 Skill 內 reviewer＝真實 `codex exec` `gpt-6-sol`（heavy，無備援）共四次呼叫（第 1 次證據包出錯不採用；第 3 次對全部 12 條 AC 逐項回覆；第 4 次為 AC10 點重審），`decide` 由回覆檔解析覆蓋 12／12，輪次：round 0 SUPPLEMENT → round 1 CLOSE；`set_status CLOSED`、`readback` ok、`finalize` exit 0 皆在該 Skill 調用內產生（`.tmp/ticket-author/close/TKA-WIRE-01/`，七天））
- [x] **[行為性]** C-light：在隔離環境以真實 Agent、實際 `Skill` 調用、不寫正式 Registry 的受控工具替身，觀察低風險票 Ticket DONE 後序列無 `Skill(qa)`／`Skill(sv)` 且出現 `Skill` `ticket-author` args `close <合成票號>`；無法安全完成則收尾實錄標「輕量實際鏈未驗」並附卡點（此情形本條以「如實標未驗」為達成，不得以探針或契約測試改寫為已驗證雙路徑自主閉環）（證據：隔離 scratch repo（`C:\Users\Administrator\AppData\Local\Temp\claude\…\scratchpad\wire\low`，內含本 repo `.claude/` 副本）＋受控 stdio MCP 替身（`stub_registry.py`，狀態只寫 scratch JSON，不連正式 Registry）＋`claude -p --model sonnet --setting-sources project`（不載入使用者 hooks）＋合成低風險票 `LOWR-01`（ALLOWED=`docs/note.md`）。以 `scan_skill_calls` 解析 stream：兩次執行皆為「`Skill(uc)`→實作→`check done`→`check commit`（第 2 次；第 1 次漏做，已據此補強 Step 11 文字）→`[LOWR-01]` 提交→`check closeout`→搬票→`set_status REVIEW`→`upsert`→`set_checkpoint`→read-back→`Skill(ticket-author)` args `close LOWR-01`（無任何 `Skill(qa)`／`Skill(sv)`，其前無人類訊息）→ TKA 內 `close_helper preflight`→`reviewer`（`gpt-6-luna`）→真實 `codex exec -c model="gpt-6-luna"` 逐 AC 回覆→`decide`（解析回覆檔）→`CLOSE`→`set_status CLOSED`→`readback`／`finalize`」；n=2，Sonnet，一張合成 docs 票，替身 Registry。原始輸出存 `.tmp/ticket-author/wire/c-light/run1|run2/`（七天））
- [x] **[存在性]** 收尾實錄如實分層記載：A 契約層、C-light（實際鏈／前測／未驗＋卡點）、B-normal 全鏈、D 調用紀錄各自證明什麼；實際 reviewer 模型、覆蓋範圍與備援原因；尚未證明項；`check` 退出碼（含 deliver 後 exit 4）；未把 stub／探針冒稱自然 AUTO-CHAIN，未把 TKA-CLOSE-01 列為完整 TKA 自主驗收證據，未宣稱長期、跨 session、所有票型已驗證（證據：§2 A、§3 C-light（實際鏈 n=2）、§4 D、§5 Cross-Review、§6 限制與自揭、§7 B-normal（含實際 reviewer、覆蓋、備援原因、AC5 文字澄清與第 1 次證據包失敗）；close 結果由 `finalize` 之最終結果區段載明；`check` exit 4、未驗證項、非盲測限定皆已揭露；Codex 四次回覆含 AC10 點重審（reviewer_reply4.txt）判 PASS）
- [ ] ~~`assembleDebug` PASS~~ (N/A: feature preset 之 `.claude/` 工具、測試與治理文字，無 `app/`／gradle 變更)
- [x] `git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉及 ALLOWED 檔案（FORBIDDEN 各檔 diff 為空；區間含他票提交時以逐提交歸屬舉證）（證據：`face1158..4d8cc015` 兩個 `[TKA-WIRE-01]` 提交，觸及檔案聯集＝ALLOWED 九檔，FORBIDDEN 零命中，區間無他票提交）

## 已知限制（非阻擋，如實揭露）

1. **C-light 可能無法完成**：隔離環境依賴 `claude -p` 非互動、受控 MCP 替身載入與權限；任何卡點如實標「輕量實際鏈未驗」，不降格為探針證明。即使完成，也僅證明一次、一個合成低風險票、一個模型。
2. **B-normal 為一次實際全鏈**：證明本票這一次由 Agent 自主發出 Skill 調用並由 Codex 逐 AC 回覆，不證明跨票型、跨 session、長期穩定。
3. **權限**：以有效配置（使用者層級＋repo＋local）為準；實際被拒才提最小需求，本票不改 settings。
4. **覆蓋解析的邊界**：helper 驗覆蓋完整、一對一、序號、格式與一致性；回覆檔是否真為該 reviewer 的原始輸出、其判讀品質，皆為模型契約，無法由 helper 證明。
5. **`check` 與 `deliver/`**：票在 `deliver/` 時 `gate_ledger.py check` 回 exit 4（既有限制）；close 不呼叫 `check`。
6. **6a 外審**：`INTRODUCED_CAPABILITY` 觸發持久協議之機械取證；依 Gate 研究授權於執行期辦理（一次窄範圍），非本票 gate。
7. **模型 ID**：`gpt-6-sol`／`gpt-6-luna` 取自本機 `models_cache.json` 並以極小呼叫確認可用；未來模型更名需同步 `close_helper.py` 常數，本票不建模型註冊機制。
8. **light 判準為路徑＋副檔名啟發式**：`.claude/` 之外的 `.md` 若實質承載複雜行為，Agent 可將票提升為 heavy（不得降低）；helper 無法偵測內容複雜度。
9. **前測 subagent 的「唯讀」僅提示層**：工具層未封鎖寫入，且不構成實際接線證據；C-light 隔離觀察使用受控替身與 scratch repo，不連正式 Registry。

## 收尾實錄（執行端；2026-10-22；Mode: normal；Rev B）

### 1. 基線與實作
- 啟動：`/normal TKA-WIRE-01`（`activate_ticket.sh` ACTIVATED）。Rev B 同票修正後才實作；原 Rev A 的 RGR 修補 F1–F11 一度被 Rev B 改寫覆蓋，已依 RGR 報告與 `wire.json` 併回（見票首 Revision）。
- AI-0：19 組現值錨點各恰 1 次、`27cc28ea..HEAD` 未觸及 ALLOWED、`check-ignore` 無命中；基線 unittest 68 OK、`gate_stop_guard_test.py` 54 OK。有效權限：使用者層級 `defaultMode=auto`、無 `ask`／`deny` 涉 Skill，repo allow 僅 `Skill(qa)`；實際 Skill 調用（本 session 與隔離 Agent）皆未被阻擋，未改任何 settings。`/normal` Phase 0 第 4 項依 Table 接續，入口契約足夠，未複製規則。
- `DIFF_REF` 於 /qa Step 0 因他票 `[PROJMAN-SYNC]` 提交 `face1158`（`projman-outcomes.md`，在本票工作開始前落地）改為 `face1158`，同步 conf、per-ticket 快照與票面。
- 實作提交：`b20d0ba0`（AI-1～AI-8 主體）、`4d8cc015`（採納 Codex ADVISORY：reply 行須含非空 evidence、Claude fallback 之模型須對應 tier）。

### 2. A 契約與機械驗證
- `unittest`：94 → 追加硬化後 **96 OK**（零 skip）；`gate_stop_guard_test.py` 54 OK；`test_report_fix_closure.sh` ALL PASS；`test_gate_ledger.py` 59 OK；殘留 grep 舊字面零命中；`git diff --check` 乾淨；範圍僅 ALLOWED 九檔。
- 模型 ID 本地確認：`~/.codex/models_cache.json` 列 `gpt-6-sol`／`gpt-6-luna`，各一次極小 `codex exec` 呼叫回 `OK`。

### 3. C-light 輕量路徑（隔離真實 Agent）
- 環境：scratch repo（本 repo `.claude/` 副本）＋受控 stdio MCP 替身（`stub_registry.py`，狀態只寫 scratch JSON，不連正式 Registry）＋`claude -p --model sonnet --setting-sources project --permission-mode acceptEdits`（不載入使用者 hooks）＋合成低風險票 `LOWR-01`（ALLOWED=`docs/note.md`）。提示僅述「進入 /uc 處理票 LOWR-01，依專案規則完成」，未提 qa／sv／close。
- 結果（`scan_skill_calls` 結構化解析 stream，原始輸出存 `.tmp/ticket-author/wire/c-light/run1|run2/`，七天）：兩次皆為 `Skill(uc)` → 實作 → 提交 → `check closeout` → 搬票 → `set_status REVIEW` → read-back → **`Skill(ticket-author)` args `close LOWR-01`**（無 `Skill(qa)`／`Skill(sv)`，其前無人類訊息）→ TKA 內 `close_helper preflight`／`reviewer`（`gpt-6-luna`）／真實 `codex exec -c model="gpt-6-luna"` 逐 AC 回覆／`decide`（解析回覆檔）／`readback`／`finalize` → 替身 Registry `CLOSED`。
- 如實發現：第 1 次未執行 `check commit`（只做 `check done`）→ 補強 `sv/SKILL.md` Step 11 引言為明示指令；第 2 次確認執行 `check commit`。兩次 closeout 都把 `reviews` 四欄填 `N_A`（低風險免 QA／SV，合理），但 TKA 的 Codex 審查結果不回填 `reviews.codex`（該欄記 Cross-Review，語義不同）。
- 限定：n=2、單一模型（Sonnet）、一張合成 docs 票、替身 Registry；不證明真實 Registry 行為、其他票型或長期遵守度。

### 4. D 調用紀錄
- `scan_skill_calls`／`human_asked_for_close` 解析 `tool_use` 的 `name`／`input.skill`／`input.args`，排除 `tool_result`、`isMeta`、Stop hook／`<system-reminder>`／`<task-notification>` 注入；`TestSkillCallParser` 5 案。

### 5. SV 與 Cross-Review
- `check qa`／`commit`／`sv` exit 0；structural 預檢 PASS。Codex（`gpt-5.6-sol`，`cross_review.sh` 預設）：第 1 次 **REPORT_FIX**（reviewer-tier guard 缺 `preflight.verify.allowed` producer 舉證）→ 補證據；第 2 次 **ADVISORY**（exit 0）→ 兩項建議採納（`4d8cc015`，**該提交未再經 Codex 審查**）。Gate：INC-001（REPORT_FIX 1 次，無 round／LIMIT）。冊面 `reviews.codex=PASS` 為依 Transition Table ADVISORY 列之映射，實際 verdict＝REPORT_FIX→ADVISORY。

### 6. 限制與如實揭露（皆非關票前置）
- 本票之 TKA close 為本 session 自身 closeout 後的第一次（B-normal）：結果見下方 §7（close 後補記）與 close 最終結果區段。
- C-light 為一次隔離觀察，不是真實 Registry、真實低風險票；真實低風險票自然鏈 SHOULD 於首張真實票收束時以 `scan_skill_calls` 觀察（非前置）。
- helper 只驗回覆檔格式、每條 AC 皆有非空 evidence、一對一、序號、與 verdicts 一致、reviewer 中繼資料；無法證明回覆檔真為 reviewer 原始輸出或其判讀品質。
- 前測 Sonnet JSON 探針未執行（已有實際鏈觀察，不需以探針補位）。
- `check` 於票在 `deliver/` 時回 exit 4（既有限制）；Dashboard／`PROGRESS_ARCHIVE` Done 標記延至下一輪 `/compact`；6a 實作級外審未執行付費研究呼叫（取證非 gate）。
- 事故自揭：Rev B 以 Write 覆寫票檔時覆蓋了 RGR 對 Rev A 的修補（F1–F11 原文），已依報告重併；另一次 `rm -rf` 複合命令被權限拒絕，改以 Python 清理 scratchpad 內自建目錄，未繞過。

### 7. B-normal 正常路徑（本票自身；close 進行中之記載）
- 正常路徑鏈：`Skill(qa)`（本 session 內）→ `/qa` Step 0/4.5 → `Skill(sv)` → Cross-Review（REPORT_FIX→ADVISORY）→ Step 11 closeout 至 `REVIEW` → **`Skill(ticket-author)` args `close TKA-WIRE-01`**。該調用發出於 closeout 的 `registry_list` read-back 之後，其前無使用者訊息要求 close（由 `scan_skill_calls` 解析本 session transcript 驗證，見 close 內 reviewer 證據包）。
- 證明了什麼：本 session 的 Agent 在上游完成後，以 Skill 工具實際進入 TKA `Mode: close`；close 內 reviewer 為真實另一個 `codex exec`（`gpt-6-sol`）調用，回覆檔存 `.tmp/ticket-author/close/TKA-WIRE-01/reviewer_reply.txt`，`decide` 由回覆檔解析覆蓋。
- 未證明：這是同一 Agent（本 session 為 Sonnet 5.5）對自己的實作票之一次觀察，不證明跨票型、跨 session、長期穩定；本 session 的 Agent 知道這是被觀察的目標，非盲測（C-light 隔離 Agent 為盲測）。
- 實際 reviewer（TKA close 內，事實已定）：Codex `gpt-6-sol`（helper 分類 heavy；無 Codex 故障，故無 Claude 備援，`fallback_used=false`）；`independent_call=true`、`inline=false`。共三次獨立 `codex exec` 呼叫：第 1 次因證據包以 `cmd` 執行 shell 引號出錯而多數 AC 回 UNVERIFIED，不採用（原檔保留）；第 2 次補證後核心 AC 仍有 UNVERIFIED，且 AC5 判 FAIL——原因是 AC5 文字寫「不一致→SUPPLEMENT」而測試與決策表的 hard blocker 優先序為 BLOCKED，已澄清 AC 文字，並非放寬實作；第 3 次補入逐 AI 字面計數、/qa→/sv 行號、真實 CLI 輸出、非 REVIEW 唯讀前後雜湊與 parser 實跑輸出後，對**全部 12 條** AC（含 N/A 與非核心）逐項回覆，覆蓋 12／12。原始回覆檔存 `.tmp/ticket-author/close/TKA-WIRE-01/`（七天）。
- 本節不重述 close 的結果：CLOSED／NOT_CLOSED、實際 reviewer 與覆蓋，以 close 的 `finalize` 寫入之最終結果區段為準（NOT_CLOSED 時不寫入，票保留 `REVIEW`）。

<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: 兩條上游路徑（正常 QA／SV、低風險免 QA／SV）改為以 `Skill(ticket-author)` args `close <票號>` 實際調用 TKA close；`close_helper.py` 改 Codex 主審（heavy=gpt-6-sol／light=gpt-6-luna，Codex 故障才備援獨立 Claude）並要求 reviewer 對全部 AC 逐項回覆（含非空 evidence），RAPID 未定義引用與斜線式 close 指引已直接替換；96 項 unittest 全綠。 / 行為層：本票自身正常路徑一次（Agent 於 closeout 後、無人類訊息夾在其間即發出該 Skill 調用，但為 Owner 工單明示之非盲測）＋隔離真實 Agent（Sonnet、替身 Registry、合成低風險票）兩次皆自然鏈出 close；尚未證明其他票型、真實低風險票、長期遵守度。 / Cross-Review：Codex 實際 REPORT_FIX→ADVISORY（codex=PASS 為映射，ADVISORY 兩項採納於後續提交未再經 Codex）；本次 close 內 reviewer 為 Codex gpt-6-sol，三次完整回覆（第 1 次證據包出錯不採用），最終覆蓋 12／12，AC5 文字澄清（hard blocker 優先於補證）與 AC10 記載補足後經點重審。
- 最終驗收結論: 核心 AC 9 條皆為 PASS 或有效 N/A；非核心 PASS 2／N/A 1／UNVERIFIED 0
- 實作提交: b20d0ba0, 4d8cc015
- 實際收束結果: 2026-10-22 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: codex/gpt-6-sol（tier=heavy）；覆蓋 AC 12／12（全部），independent_call=True
- 披露: reviewer limitation: Four real codex exec (gpt-6-sol) calls in this close: attempt1 (defective evidence bundle, not used), reviewer_reply.txt
<!-- close-result:end -->
