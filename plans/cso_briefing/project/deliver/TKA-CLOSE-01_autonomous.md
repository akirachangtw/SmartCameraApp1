# TKA-CLOSE-01: ticket-author 新增 `close` 子命令，使自主工作流 Agent 能對 REVIEW 票經本地機械取證、reviewer 選擇／fallback 與逐項 AC 驗收後自主 CLOSED 並 read-back，中間衍生物只存 `.tmp` 七天、原票只留最小最終結果，並直接替換阻擋自主閉環的舊條款

**Revision**: Rev A.2（2026-10-21，RGR cycle 1 同票修補，4a 計數＝2；報告 `.tmp/rigor-gate/RGR_TKA-CLOSE-01_20261021.md`。差分：F1 新增 R9c 替換 `:282` ② 子句〔`N_A` 依據改於票檔最終結果揭露；缺漏 `reviews` 欄有可考結果者採 `reviews_observed`〕並同步 helper／Mode: close／HC9／AC／測試／殘留清單；F2 `decide` 的 `--round`／`--fallback-tried` 改必填；F3 第 2 條 AC 改標存在性、第 3 條補標可達性並寫明 stub 邊界；F4 決策表增 X3〔scope UNKNOWN→針對性補證，達上限不新增終局〕）。Rev A.1（2026-10-21，同票修正；Owner 執行裁決「TKA-CLOSE-01 同票修正」：取代前述同檔等待與 worktree 建議，維持正式票、不退回 plan、不另開 repair／協調票、不增加外審或 Owner 重複批准。差分：ADV-ENTRY-01 同檔協調解除與 AI-0 重寫；AC verdict 完整性與完整 AC 文字；核心 AC／補證邊界；決策順序與有界 RECONCILE；dependency LEAD；read-back／finalize 同一判準；decision 綁定與歷史 CLOSED 保護；實作 diff 範圍歸屬；reviewer 分類統一；七天清理語義；測試涵蓋面）。Rev A（2026-10-21，首次開票）。

**簽發日期**: 2026-10-21
**簽發者**: Claude
**來源議題**: TKAuthor
**Mode**: normal  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無（`TKA-BATCH-01`、`TKA-EVGATE-01` 為已 CLOSED 之唯讀背景，非判準或錨點來源）
- `coordination_dependency`: 無（Rev A.1：原 `ADV-ENTRY-01` 同檔協調已解除——該票已達 `REVIEW`，其 `ssot-governance.md` 修改已提交於 `54f5624a` 並為 HEAD 祖先；不再等待、不另建 worktree）

**檔案治理／來源說明**：本票為 Plan-backed 單票。
- Parent type: Plan-backed
- Parent absolute path: `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261021-p2-close-plan.md`（`.tmp` 路徑僅為讀取位置；下列內容已嵌入，本票於該檔消失後仍自足）
- Parent artifact status: `Fixture: false`、`Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`、`Status: FINAL`（第三版，原地取代前版）
- Plan-local item ID: `T01`（非票號，僅留於本區）
- Parent read timestamp: 2026-10-21 13:30+08:00（本次 invocation 起始處讀取）
- Selected item（Ticket Map 原文摘要）: TKAuthor `close` 的機械取證、Agent 驗收與自主收束——新增可由自主工作流 Agent 調用的 `/ticket-author close <票號>`；REVIEW 票經本地命令取證、reviewer 選擇／fallback、逐項 AC 驗收後自主 CLOSED 並讀回；只在原票留最小最終結果；直接改寫阻擋自主閉環的舊條款。
- Plan Gate／eligibility 結果: PASS／ALLOWED
- 核定（Owner 2026-10-21，全數 APPROVED，嵌入自 plan D1–D8 與兩次審查修正）：
  1. `close` 可由自主工作流中的 Agent 調用，不限使用者本人、不逐票等 Owner；CLOSED 寫入者與 HUMAN_GATE 中阻擋自主關票的條款直接改寫，不保留矛盾條款再疊括註、豁免或平行規則。
  2. hard blocker 僅四項：核心 AC 已確認 FAIL；已知安全問題；不可逆損害／資料遺失風險；下一張相依票明確需要、但尚未交付的 contract。其餘（非核心 UNVERIFIED、有效 N/A、可選測試未跑、首次真實使用待觀察、可選外審不可用）披露而不阻擋，也不強制 repair ticket。
  3. 每條 AC 判 PASS／FAIL／N/A／UNVERIFIED；先讀機械證據；不以 checkbox、模型記憶、舊 prompt 當完成證據；可用有效既有證據；exit 0 只支持其實際驗證內容；不建通用 AC parser、固定 AC 命令語法、Template schema。
  4. reviewer：純文字／文件／metadata／低風險→優先 Sonnet；Claude 所寫程式與承重邏輯→優先 Codex；任一不可用→另一者 fallback 並記實際 reviewer 與 limitation；高風險或結果矛盾→針對未決點升級複審；不建通用 router；Traycer／T2 是可選外審。
  5. close report、AC 明細、命令輸出、reviewer 原始回應、重試紀錄皆為中間衍生物：只存 `.tmp`、保留七天到期清理、不 commit、不上 TKR、不寫回原票作永久 report、不成為七天後仍必須存在的依賴。原票僅留最小最終結果（交付摘要、最終驗收結論、實作提交、實際收束結果）；Registry 維持既有最小 metadata，不新增 schema。
  6. 不新增 mode 以外的治理系統、Gate、ledger、manifest 或 reason code（`close` 子命令本身是本票交付）；不改 `gate_ledger.py`；不補造或回填歷史 receipt；不重開或批次修改既有 CLOSED 票。
  7. 維持單一 atomic 票；檔案數多不構成拆票理由。
- **Rev A.1 執行裁決**（Owner 2026-10-21，嵌入；與上列核定同為 APPROVED）：
  A. ADV 同檔協調已解除；AI-0 改為基線與漂移重驗（見 AI-0），舊錨點因 ADV 提交漂移不是停工理由，重新定位並確認需求仍存在後更新同票錨點續作；保留 ADV 已完成的有效修改，不覆寫回 `0d343c51` 舊文。
  B. `decide` 須對照 preflight 的原票完整 AC 清單驗證 verdict 完整性（每條 AC 恰一筆；不漏、不重、不引用不存在 AC；空 `acs` 或 AC 清單不可得不得使「所有核心 AC 通過」因空集合成立）；AC 原文與必要續行完整供 reviewer 使用，400 字元限制僅用於 stdout 預覽。
  C. 核心 AC 依交付目標必要性判定，難驗不降級；外審工具可選不代表其發現的實質核心缺陷也可選；核心 UNVERIFIED 沿有界次數補證與 fallback，仍不足保留 `REVIEW`，不假報、不製造人工批准步驟。
  D. 已確認的四類 hard blocker 優先於 RECONCILE；RECONCILE 受補證上限約束，達上限仍矛盾→保留 `REVIEW` 並精確回報，不重複同一補證動作，不再 AUTO-CHAIN 自己；既有停止碼不得解讀為「一定要 Owner 人工批准」。
  E. dependency：只查直接相依與具體必要 contract；有直接證據缺件→blocker；有具體線索→針對線索補查、不以 UNKNOWN 蓋過；僅無法取得完整相依清單→標未知，不憑空判缺件、不虛報已全面查核。
  F. `finalize` 與 `readback` 使用同一判準（lifecycle、目標票檔解析、`reviews` 依平台契約與適用結果比對，不為填滿四欄假填可選外審 PASS）；讀回不一致不得寫「讀回一致」；部分成功如實回報，後續只補缺。
  G. `decision.json` 須綁定票號、目標票檔、對應實作基線／提交，`finalize` 與 `decide` 落實檢查；歷史 CLOSED 票不批次補寫、不重驗、不修改；七天後暫存遺失而無法確認屬本次部分成功→如實回報，不猜測補寫。
  H. 實作 diff 範圍：不宣稱僅靠最新本票前綴提交即排除所有他票變更；對 `DIFF_REF..` 區間逐提交辨識歸屬；他票已提交修改不算本票 scope violation；無法建立可信範圍標 UNKNOWN 並針對性補證；不擴成 Git 歷史管理或 worktree 平台。
  I. reviewer 分類統一（helper、SKILL、測試一致）：依本票實際改動的檔案類型，而非 preset 名稱。
  J. 七天清理：明確時間界線、第一層只枚舉候選票號目錄、允許安全清除到期候選內部內容、不跟隨連結、排除本次輸出、每次處理上限、失敗不阻擋、呼叫時清理（不宣稱無後續呼叫也能在第七天精確刪除）。
  K. 測試沿用 unittest、scratch 假 repo、假 Registry adapter 與 stub reviewer，證明調度契約與實際副作用，不宣稱已證明真實 Agent 必定遵守 Skill 或模型判讀品質；首次真實使用不作永久關票前置。
  L. 既有開票偏差（先寫 `Pending/` 草稿再跑 authoring gate）如實保留，不因此作廢重開、回滾登冊或另開治理票。
- 本輪審查裁決（開票時統一納入，並統一修正 plan 中有歧義之定義）：
  ①**核心 AC**＝依交付目標必要性判定：其不成立則票面一句話交付目標不成立者為核心；**難以驗證不是降級為非核心的理由**；有疑義時從嚴視為核心並先補證；僅「與交付目標無必要性」或票面明示為可選／觀測／外審者為非核心。
  ②**核心 UNVERIFIED**：優先自主補證與 fallback；仍不足才保留 `REVIEW`；不假報 PASS，也不自動製造人工批准步驟。
  ③**dependency contract**：只查直接相依與具體 contract；無法判定時如實標「未知」，不宣稱已確認無缺件。
  ④**七天清理**：有界、排除本次輸出、不跟隨連結；清理失敗不阻擋主流程；不新增 hook 或 daemon。
  ⑤**同次最小更新**已知過期的 closure 專案記憶（不另開票）。
  ⑥**checkpoint** 只在其事項確實解決時標 `RESOLVED`。
  ⑦**本次部分成功**可補寫最終結果；不批次修改歷史 CLOSED 票。
  ⑧**隔離測試**驗證調度契約，不將 stub PASS 冒充真實模型行為。
- 範圍外（deferred）：RAPID MODE 在 APP 內的定義補完；`TICKET_REGISTRY.json` 版控政策；`gate_ledger.py`／`gate_stop_guard.py` 全面修復；feature／bug milestone 管理；batch 或批次關票；通用模型 router；Registry `file` 欄過期之修正（housekeeping 範疇）。

**開票時 live 證據基線**（2026-10-21 13:30–13:37+08:00，`main` @ `0d343c51dc7ea128fc321636bdcff383a4b9a785`，工作樹乾淨）：
- 重複票檢查：Registry（main＋archive，topic `TKAuthor`）僅 `TKA-ALIGN-01`、`TKA-BATCH-01`、`TKA-CHANNEL-01`、`TKA-EVGATE-01`（皆 CLOSED，標的不同）；`Pending/`、`deliver/`（含 `deliver/archive/`）、`void/` 無等價 outcome（`ticket-author close`／`Mode: close` 零命中）；`TKA-CLOSE` 於全冊、檔名與 commit 皆未使用。
- 改寫錨點（`ssot-governance.md`、`sv/SKILL.md`、`ticket-author/` 各檔）逐字 `grep -cF` 皆恰 1 次，列於 AI-0；阻擋自主關票之條款全文搜尋（`SMTR／HUMAN_GATE／限審查方／審查方／停在 REVIEW／等待 T2`）結果：實際阻擋點為 `ssot-governance.md` 的 `:81、:82、:88、:96、:142、:160、:168、:172、:205、:282`，另 `:277、:280、:281、:283` 為連動；`.claude/CLAUDE.md` 無 REVIEW／CLOSED 字樣。
- 入口：`ticket-author/SKILL.md` frontmatter `argument-hint` 與 mode 清單為三值；`stop-conditions.md` 的 `STOP_INVALID_MODE` 同列三值；`SKILL.md` 有 `This skill only authors new Tickets.` 一句。
- 冊面 `file` 過期為系統性現象：`TKA-ALIGN-01`、`TKA-CHANNEL-01`、`PJMAN-OUTCOMES-01` 冊面 `file=deliver/…`，實檔在 `deliver/archive/<topic>/`；`registry_list(include_archive=true)` 才查得。`gate_ledger.py status --ticket <archive 票>` 可讀（`loc:none`），`check` 對 `deliver/` 票回 exit 4（既有限制，不在本票修復）。
- `verify_checklist.sh`：conf 模式寫 `tmp/verify/active.json` 並輪替快照，目錄寫死相對 cwd；CLI 模式忽略 `DIFF_END`——故本票 helper 不重用它，scope 以純 git 取得。
- 保留期慣例：`cross_review.sh:133` 的 `find … -maxdepth 1 -type f -mtime +14 -delete`（有界、不遞迴）；SessionStart／stop hook 無通用 `.tmp` 清理。
- 隔離測試慣例：`.claude/skills/qa/tests/test_report_fix_closure.sh`（scratch 假 git repo＋PATH 前置 stub `codex`）；Python 測試用標準庫 `unittest`（`test_route.py`、`test_gate_ledger.py`），本機 `python` 3.14.5、無 `pytest`。
- reviewer 能力：`codex exec` 調用慣例見 `cross_review.sh:440-446`（`-c model=… -c model_reasoning_effort=… -`，stdin 傳 payload），`command -v codex` 在 `:63`；Sonnet 為 Agent 工具之 `model` 參數或當前 session；`.claude/agents/` 僅有 `cross-reviewer.md`（opus，R0 唯讀、無 verdict）。
- 權限（使用者層級，非本票範圍）：`~/.claude/settings.json` `defaultMode=auto`，registry 寫入工具未全數列入 allow；repo `settings.json` allow 僅 `Skill(qa)`。本票不改任何 settings。
- 平台契約（PLT `tool-contracts.md`）：`registry_set_status(lifecycle=CLOSED)` 在 `checkpoint.state == OPEN` 時拒收；成功後 checkpoint 清為 null；`registry_set_checkpoint` 提供 `resolution` 時 state 設為 `RESOLVED`（TKA-EVGATE-01 已實操驗證）。
- 並行（Rev A.1 取代原「IN_PROGRESS」敘述）：`ADV-ENTRY-01` 冊面 `lifecycle=REVIEW`、`file=deliver/ADV-ENTRY-01_route.md`；其 `ssot-governance.md` 修改提交於 `54f5624a`（`git merge-base --is-ancestor` 為真）。本票錨點本來即取自含該提交的 `0d343c51`，故 ADV 的有效修改已在基線內，無須回溯。其餘 `Pending/` 票 ALLOWED 無一觸及本票檔案。
- **Rev A.1 基線重驗**（2026-10-21 14:26–14:3x+08:00，`main` @ `c8385e648b6c2fb808f923baae0ec64374aae6b0`，工作樹乾淨）：自 `0d343c51` 起的新提交僅 `6771b989`（ADV-ENTRY-01 票檔收尾）、`a55ee9e3`（projman align）、`c8385e64`（`input-contract.md` 第 64 行：diag／plan 預設寫 artifact，Owner 免票授權）。`ssot-governance.md`（blob `dd659b1f`）、`sv/SKILL.md`（`e7a70c12`）、`ticket-author/SKILL.md`（`c60e1a10`）、`state-transitions.md`（`c3f93347`）、`stop-conditions.md`（`3ba68d3c`）自開票時未變；`input-contract.md` blob 由 `a6aaaa3e` 變為 `56554cb5`（僅該行語義，不涉本票插入點 `## Structured option`）。AI-0 全部錨點重驗仍各恰 1 次。無語義衝突。
- 6a 實作級外審：`INTRODUCED_CAPABILITY=feature:ticket-author-close` 於固定 corpus 無完整字面命中 → 持久協議（`Instructions_for_Claude.md` §6a）之機械觸發成立；其性質為取證／bounded probe，非本票或 close 的 gate（見已知限制）。

**Template 讀取紀錄**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件日期 2026-05-29，effective revision date 2026-09-14（v3.11）；本次 invocation 全文讀取（322 行），檔案 mtime 2026-09-14。

## Verify Config

TICKET_ID=TKA-CLOSE-01
CREATED=2026-10-21
PRESET=feature
ALLOWED=.claude/skills/ticket-author/SKILL.md,.claude/skills/ticket-author/references/input-contract.md,.claude/skills/ticket-author/references/state-transitions.md,.claude/skills/ticket-author/references/stop-conditions.md,.claude/skills/ticket-author/scripts/close_helper.py,.claude/skills/ticket-author/tests/test_close.py,.claude/rules/ssot-governance.md,.claude/skills/sv/SKILL.md
FORBIDDEN=.claude/scripts/gate_ledger.py,.claude/hooks/gate_stop_guard.py,.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/sv/scripts/cross_review.sh,.claude/rules/cross-review.md,.claude/CLAUDE.md,.claude/settings.json,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/deliver/TKA-BATCH-01_selected-set.md,plans/cso_briefing/project/deliver/TKA-EVGATE-01_derivation.md,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json
MAX_LINES=N/A
DIFF_REF=c8385e64
ANCHOR_PROBED=2026-10-21 14:30
BASE_COMMIT=0d343c51dc7ea128fc321636bdcff383a4b9a785
TARGET_BLOB=.claude/skills/ticket-author/SKILL.md:c60e1a10;.claude/skills/ticket-author/references/input-contract.md:56554cb5;.claude/skills/ticket-author/references/state-transitions.md:c3f93347;.claude/skills/ticket-author/references/stop-conditions.md:3ba68d3c;.claude/skills/ticket-author/scripts/close_helper.py:NEW;.claude/skills/ticket-author/tests/test_close.py:NEW;.claude/rules/ssot-governance.md:dd659b1f;.claude/skills/sv/SKILL.md:e7a70c12
INTRODUCED_CAPABILITY=feature:ticket-author-close
DEPENDENCY_DELTA=NONE

> PRESET=feature（新增 `.py` helper、`unittest`，並改寫治理條文與兩份 skill 文字；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A，同 `ADV-ENTRY-01`／`ADV-APICALL-01` 先例）。`MAX_LINES=N/A`：含授權／守門／fail-closed 判定類不變量，失敗形狀為審查發現式，不可先驗估算（Template v3.5 R3）。`DEPENDENCY_DELTA=NONE`：helper 與測試僅用標準庫（`unittest`），無新依賴。`ALLOWED` 皆為精確路徑（`verify_checklist` 對含 `/` 之 token 為字面比對，不用 glob）。

## Action Items

### AI-0: 錨點與基線復驗（派工後、修改前）

- **動作**: 以 live `grep -cF` 逐字確認下列「現值」各於指定檔內恰 1 次（以下區塊為逐字內容，反引號為字面字元）；不符時依本節末「錨點漂移處理」，不是直接停工：

```text
ssot-governance.md :81  ，Traycer T2 另由 Akira 於 VS Code 手動執行 T2/CSO 複審 |
ssot-governance.md :82  | ⑤ `CLOSED` | 複審通過 | **限審查方 SMTR/CSO**（終審 `reviews` 回填＋收束 note） |
ssot-governance.md :88  有缺即不得轉 `CLOSED`。**
ssot-governance.md :96  `REVIEW` 本身即為「等待最終 T2／CSO 複審」之合理停留態
ssot-governance.md :142 4. **Agent 寫入上限**：Agent 最高可寫 lifecycle 為 `REVIEW`；`CLOSED` 限審查方 SMTR/CSO（回指 §五段觸發與寫入者⑤）。
ssot-governance.md :159 | Agent closeout 於本次呼叫實際完成（lifecycle 由本次寫入達 `REVIEW`
ssot-governance.md :160 | 已收束重入（`lifecycle ∈ {REVIEW, CLOSED}` 且票檔位於 `deliver/`） |
ssot-governance.md :168 - **`PENDING` MUST NOT 被當作 blocker**——lifecycle 正常停留於 `REVIEW`，等待人工 Traycer 複審與終審方轉 `CLOSED`。
ssot-governance.md :172 `registry lifecycle ∈ {REVIEW, CLOSED}` **且**票檔實際位於 `deliver/`
ssot-governance.md :205 `HUMAN_GATE`（Traycer T2／`CLOSED`／Rule 4a 無票）
ssot-governance.md :277 並於 PROGRESS.md 誠實揭露（K5-Rule 29）
ssot-governance.md :280 - **修補票紀律**：母票存在未閉環 AC → **MUST** 同步產出修補票
ssot-governance.md :281 - 母票 AC 佐證不明（無法判定該勾或保留）→ **MUST** 停止勾選該項並列入修補票追蹤，禁止推定勾選。
ssot-governance.md :282 ④收尾實錄之耐久載體為**票檔（`deliver/`）＋vault 決策檔**
ssot-governance.md :283 - **原子性**：四要件 **MUST**
sv/SKILL.md            執行 Agent closeout 至 `REVIEW`。本節為引用
sv/SKILL.md            3. **收束後成果校正**
ticket-author/SKILL.md argument-hint: "diag|plan|ticket [task text and @sources]"
ticket-author/SKILL.md This skill performs diagnosis, planning, evidence collection, and Ticket authoring.
ticket-author/SKILL.md This skill only authors new Tickets.
ticket-author/SKILL.md ## Conditional state matrix
references/stop-conditions.md   | `STOP_INVALID_MODE` | Mode absent or not `diag`, `plan`, `ticket` |
references/stop-conditions.md   ## Decision-fork discipline
references/input-contract.md    ## Structured option
references/state-transitions.md ## Regression transitions
```

  專案記憶（repo 外）：`governance-ticket-closure-sop-target-state.md` 內 AI-7 所列現值各恰 1 次。
- 併做（Rev A.1 取代原併做項）：
  1. 確認 repository root（`git rev-parse --show-toplevel`）、可讀性、目前 HEAD、branch、`git status --short`。
  2. 確認目前分支**已包含** `54f5624a`：`git merge-base --is-ancestor 54f5624a HEAD`（不能只假設已包含）。若非祖先：先以 `git branch --contains`／`git log` 確認提交與分支關係，沿專案既有安全方式處理；**不得**盲目 cherry-pick、reset 或改寫歷史。
  3. 讀取 `54f5624a` 之後 `ssot-governance.md` 的完整相關上下文（AI-5 所涉全部段落：五段表、Traycer 邊界、轉移表與冪等、HUMAN_REQUIRED 立即類、Ticket Closure 紀律），只針對受 ADV 修改影響的錨點與替換內容更新本票；**保留** ADV 已完成的有效修改，不覆寫回 `0d343c51` 舊文。
  4. 共享檔（`ssot-governance.md`、`sv/SKILL.md`、`ticket-author/` 各檔）若有未辨識的未提交變更：先辨識歸屬並保留，不 reset、不覆寫，也不把他票變更算成本票實作。
  5. `git check-ignore -v` 對 ALLOWED 內 8 個路徑（含兩個 NEW 路徑）皆無命中。
  6. `DIFF_REF` 回填（`git rev-parse --short HEAD`）並同步再生 `tmp/verify_cmd.conf`；`BASE_COMMIT..HEAD` 間觸及 ALLOWED 標的之提交依 Template 基線漂移三分流處理（輸出空→無漂移）。
  7. `python -c "import unittest, json, shutil, subprocess"` 成功；`python .claude/scripts/gate_ledger.py status --ticket TKA-CLOSE-01` 不阻斷。
  8. 記錄 `/mdlint` 對三份 `.md`（`SKILL.md`、`ssot-governance.md`、`sv/SKILL.md`）之 baseline warning 數。
  - 不再等待 ADV 達 REVIEW（已滿足），不為同檔協調建立 worktree。
- **錨點漂移處理**：錨點不符（含因 ADV 等已提交修改造成的漂移）不是直接停工理由——以 live 讀取重新定位（依證據，不憑記憶或猜測推定等價位置）、確認需求仍存在，於本票同步更新錨點並記入收尾實錄後續作；僅在實際存在無法安全判定的語義衝突時，精確回報該點。
- [x] 完成（見收尾實錄 §1–§6）

### AI-1: 新增內部 helper `close_helper.py`

- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`（NEW）
- **現值**: （新檔）
- **目標值**: Python 3、僅標準庫；單檔；全域選項 `--repo <root>`（預設 `git rev-parse --show-toplevel`）、`--now <epoch>`（測試用，預設現在）；所有子命令 stdout 輸出單一 JSON 物件；exit 0＝已產生決策或結果、2＝用法錯誤、3＝拒絕執行（前提不成立，例如 `finalize` 的讀回檢查或綁定檢查未通過）。helper **不**呼叫 Registry、模型、網路；唯讀 git（`rev-parse`／`log`／`diff`／`show`／`merge-base`／`ls-files`）；寫入僅限 `.tmp/ticket-author/close/<票號>/` 與 `finalize` 對目標票檔之單一最小區段。子命令契約：
  1. `preflight --ticket ID --registry F [--dependents F]`：
     - 讀 agent 提供之 `registry.json`（`registry_list` 單票物件）；定位票檔（冊面 `file` 優先，不存在則於 `plans/cso_briefing/project/deliver/archive/**` 找同 basename；0 筆＝`missing`、≥2 筆＝`ambiguous`；`file_field_stale` 標示過期）。
     - 解析票內 Verify Config（`PRESET`、`ALLOWED`、`FORBIDDEN`、`DIFF_REF`、`BASE_COMMIT`、`INTRODUCED_CAPABILITY`）。
     - **AC 清單（完整）**：取 `## Acceptance Criteria` 區每個以 `- [ ]`／`- [x]` 開頭的頂層項目為一條 AC（序號 `n` 自 1 起），其**必要續行**（下一個頂層 `- [`／標題／水平線之前、屬於該項的後續行，含縮排行與其間空行）併入該 AC 原文；區內不屬任何 AC 的引言（如 `>` 區塊）不算 AC。`preflight.json` 的 `acs[].text` 存**完整**原文與標記（型別標記、`[x]`、劃記 `~~…~~`）；僅 stdout 預覽以 `text_preview`（≤400 字元）呈現，**不得**以預覽截斷實際驗收語義。區段不存在或解析出 0 條 → `acs_status=UNAVAILABLE`。
     - **實作 diff 範圍與歸屬（Rev A.1）**：下界 `base` ＝ `DIFF_REF`（有效 commit）否則 `BASE_COMMIT`（有效）否則 UNKNOWN。逐提交檢視 `base..HEAD` 內每個提交：subject 以 `[<ID>]` 開頭者為 `own`；以他票 `[<其他ID>]` 開頭者為 `other:<ID>`；無票號前綴者為 `unprefixed`。`own` 提交中觸及 ALLOWED 者構成實作提交集合，其中最新者為 `impl_end`；僅觸及票檔而不觸及 ALLOWED 的 `own` 提交（紀錄提交）不納入。scope 檢查只針對 `own` 實作提交各自觸及的檔案（逐提交 `git diff-tree --no-commit-id --name-only -r`），對 ALLOWED／FORBIDDEN 比對（精確路徑，亦接受 basename 與 `*`）：有違規→`FAIL`；`other:*` 提交即使混在區間內也**不計**為本票違規，僅列入 `attribution`（含其觸及 ALLOWED 之資訊）；若有 `unprefixed` 提交觸及 ALLOWED 或無任何 `own` 實作提交，則 scope 為 `UNKNOWN`（附原因，供針對性補證），而非 PASS。`git diff --check` 同樣只對 `own` 實作提交逐一執行（`git diff --check <c>^ <c>`），任一失敗→`FAIL`，無提交可查→`UNKNOWN`。**不宣稱**僅靠最新本票前綴提交即已排除所有他票變更。
     - 呼叫 `gate_ledger.py status --ticket ID --json`（唯讀，失敗→`UNAVAILABLE`，不阻斷）取 receipts 摘要；目錄—lifecycle 一致性（`deliver/` 或 `deliver/archive/` ↔ `REVIEW`／`CLOSED`；其餘組合→`FAIL`）；`final_result_present`（票檔含 `<!-- close-result:begin -->`）；checkpoint 狀態。
     - 直接相依：`dependents.json` 內 `depends_on`／`blocked_by` 含本票者，及其票檔頭 `execution_dependency`／`evidence_dependency` 行含本票號者；各附至多 3 條提及本票號的原文行；不遞迴掃描整個專案；`status: CHECKED|UNKNOWN`（`dependents.json` 缺失、無效或任一列出之票檔不可讀→UNKNOWN）。
     - 結果寫入 `<dir>/preflight.json`；stdout 只輸出精簡摘要；最後呼叫 `sweep`（best-effort）。
  2. `reviewer --ticket ID [--sonnet-available 0|1] [--codex-available 0|1] [--escalate n1,n2]`：讀 `<dir>/preflight.json`。**分類依本票實際改動的檔案類型，不依 preset 名稱**：`class=docs` 若且唯若 ALLOWED 的每個檔案副檔名 ∈ {`.md`,`.txt`,`.rst`}；ALLOWED 中只要有任何其他副檔名（程式、腳本、設定、結構化資料等）→ `class=code`。因此 preset 非 `docs` 的純文字／metadata 票走 Sonnet，含承重程式的票即使附帶文字檔也走 Codex。`primary`：docs→`sonnet`、code→`codex`；`fallback` 為另一者。可用性：codex 預設 `shutil.which("codex")`（可被旗標覆蓋）、sonnet 預設 1；primary 不可用而 fallback 可用→`primary` 改為 fallback 且 `fallback_used=true`、附 `limitation`；皆不可用→`actual=none`。`--escalate`：回傳「另一位」reviewer 僅針對所列 AC 序號複審（不可用→`none`＋limitation）。不含任何模型輸出。
  3. `decide --ticket ID --registry F --verdicts F --round N --fallback-tried 0|1`：純函式依本票〈決策表〉輸出 `next`（`PRECONDITION_FAILED｜ALREADY_CLOSED｜WRITE_FINAL_ONLY｜BLOCKED｜SUPPLEMENT｜RECONCILE｜FALLBACK_REVIEWER｜NOT_CLOSED｜CLOSE`）與其參數。`verdicts.json` 由 Agent 寫入，欄位：`reviewer{primary,actual,fallback_used,limitation}`、`acs[{n,status(PASS|FAIL|N/A|UNVERIFIED),core(bool),evidence(短定位),note}]`、`security_issue(bool)`、`irreversible_risk(bool)`、`dependency_contract{status(NONE_FOUND|MISSING|LEAD|UNKNOWN),detail}`、`checkpoint_matter_resolved(true|false|null)`、選填 `reviews_observed{<qa|sv|codex|traycer>:{value,evidence}}`（`value` 僅限平台既有 enum，`evidence` 為證據定位；只在有機械證據顯示該審查實際結果時填寫）。
     - **`reviews_patch` 計算（Rev A.2，對應 R9c）**：只針對轉態前 `registry.json` 中**缺漏**的 `reviews` 欄位；有 `reviews_observed` 者採其值，否則填 `N_A`；可選外審（traycer）無證據時不得為 `PASS`；已有值的欄位不改。`decide` 輸出 `reviews_patch` 與 `reviews_na_fields`（補 `N_A` 的欄位清單），`readback` 預期值與 `finalize` 披露行都使用這同一個計算結果。
     - **verdict 完整性（Rev A.1）**：對照 `preflight.json` 的 `acs[]` 驗證——每條 AC 恰有一筆有效判定（`status`、`core` 皆為合法值）；不漏項、不重複序號、不引用不存在的 `n`；空 `acs` 視為全部缺項；`acs_status=UNAVAILABLE`（原票 AC 無法可靠取得）時不得默認完成。缺漏→要求補齊（計入補證輪次）；仍無法補齊→不轉 CLOSED。這是輸入完整性檢查，不新增 Gate 或 schema 系統。
     - **必填參數（Rev A.2）**：`--round` 與 `--fallback-tried` 為必填，缺少任一→exit 2（用法錯誤），不得以預設值使補證上限失效；呼叫端傳錯數字仍屬模型契約，不新增狀態檔或計數器。
     - 機械護欄：preflight `dependents.status=UNKNOWN` 時 `NONE_FOUND` 降為 `UNKNOWN`（`MISSING` 與 `LEAD` 不被降級、不被 UNKNOWN 蓋過）；`status=N/A` 且該 AC 無劃記（`~~`）者降為 `UNVERIFIED`；scope 或 `git diff --check` 為 `FAIL` 而 verdicts 無任何核心 AC 為 `FAIL`→列入 RECONCILE 判定（見決策表，且受補證上限約束）；scope 或 `git diff --check` 為 `UNKNOWN`→`SUPPLEMENT(scope_unknown)`（X3，共用上限，Rev A.2）。
     - **決策記錄與綁定**：決策寫入 `<dir>/decision.json`——**僅**在 lifecycle=`REVIEW` 的決策寫入；`CLOSED` 路徑與 `PRECONDITION_FAILED` 不寫入、不覆蓋，以保留先前 `next=CLOSE` 標記。記錄含最小綁定：`ticket`（票號）、`ticket_file`（目標票檔相對路徑）、`binding_ref`（`impl_end`；若 `impl_end` 不可得則為 `base`）、`next`、`ts`。`decide`（C2 列）與 `finalize` 都以共用函式 `verify_binding()` 檢查：票號相符、`ticket_file` 等於本次解析結果、`binding_ref` 等於本次 preflight 導出值且為 HEAD 祖先；任一不符→視為無先前決策。不新增 manifest、永久 receipt 或狀態平台。
  4. `readback --ticket ID --registry F`：讀 agent 於轉態後重讀的 `registry_after.json`，與預期比對——`lifecycle=CLOSED`；`file` 可解析為實存票檔（含 archive 備援）；`reviews` 各欄等於轉態前 `registry.json` 的 `reviews` 加上決策的 `reviews_patch`（patch 只處理**缺漏**欄位：有 `reviews_observed` 採其值，否則 `N_A`；**不得**為填滿四欄而假填 PASS；計算見 `decide`）。輸出 `ok` 與 `mismatches[]`；不一致不得回 ok。判準實作為共用函式 `verify_readback()`。
  5. `finalize --ticket ID --registry F --verdicts F --summary F --commits h1,h2`：先呼叫與 `readback` 相同的 `verify_readback()`——未通過→exit 3、不寫任何檔、**不得**寫出「讀回一致」；再呼叫 `verify_binding()`——未通過→exit 3、不寫任何檔（即使冊面為 CLOSED）；兩者皆通過才於票檔以 `<!-- close-result:begin -->`／`<!-- close-result:end -->` 之間冪等 upsert 單一小區段（上限 20 行，自包含、不以 `.tmp` 連結為唯一證據）：交付摘要（取自 `summary.txt` 前 3 行）、最終驗收結論（PASS／N/A／非核心 UNVERIFIED 各幾條；核心 AC 狀態）、實作提交（`--commits`）、實際收束結果（日期、CLOSED、讀回一致、實際 reviewer 與 fallback）、披露（非核心 UNVERIFIED／有效 N/A 簡述、dependency 未知或線索、可選外審未做、`reviews_na_fields` 中補 `N_A` 之欄位與依據；至多 5 行）；已存在則原地替換，不重複追加。
  6. `sweep [--root R] [--days 7] [--exclude DIR] [--max 200]`：
     - 候選：只枚舉 `R`（預設 `.tmp/ticket-author/close`）第一層、名稱符合票號樣式（`^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$`）的**目錄**；其他項目一律不碰。
     - 到期界線：候選的 `age = now − max(mtime)`，其中 `max(mtime)` 取候選目錄與其內所有項目（`lstat`，不跟隨連結）的最大 mtime；`age > days × 86400`（嚴格大於）即到期。
     - 清除：到期候選允許安全清除其**內部內容**後再移除候選目錄（自行以 `os.scandir`＋`lstat` 由內而外刪除；內部遇符號連結／Windows junction（`is_symlink()` 或 `FILE_ATTRIBUTE_REPARSE_POINT`）只移除連結本身，不遞迴、不刪除目標）；候選本身若為連結→整個略過。因此有內容的到期目錄可被清除，不因「第一層」字面而永遠無法清除。
     - 排除與上限：`--exclude`（本次輸出目錄）一律不碰；每次至多處理 `--max` 個候選，且每個候選至多檢視 5000 個項目（超過→不刪並記入輸出）。
     - 失敗不阻擋：任何例外只記入輸出 `errors[]`，永遠 exit 0；不新增 hook、daemon 或排程。清理僅在命令被呼叫時發生，不保證在第七天精確刪除。
- [x] 完成（見收尾實錄 §1–§6）

### AI-2: 新增隔離測試 `test_close.py`

- **檔案**: `.claude/skills/ticket-author/tests/test_close.py`（NEW）
- **現值**: （新檔）
- **目標值**: 標準庫 `unittest`；每案在 `tempfile` scratch 假 git repo 內執行（含最小 `plans/cso_briefing/project/{Pending,deliver,deliver/archive}`、假 `registry.json`／`dependents.json`／`verdicts.json`／`registry_after.json`（假 Registry adapter＝由測試扮演 Agent 讀寫）、假 `codex` PATH 替身，沿用 `test_report_fix_closure.sh` 慣例）；以 `subprocess` 呼叫真實 `close_helper.py` CLI（不直注內部狀態）；不寫真 repo、真 Registry、真 `.tmp`、真 ledger，也不建任何正式票、Registry entry 或 commit。測試群組（至少）：
  1. 調度契約整合測試——自 `SKILL.md` 的 `close-dispatch` 區塊解析出命令列，代入 scratch 路徑後逐條實際執行，由測試扮演 Agent 依序寫入各 JSON，並斷言每步 `next`、實際副作用與最終票檔（不只檢查文件字串）。
  2. AC 完整性：缺項、重複序號、引用不存在 AC、空 `acs`、`acs_status=UNAVAILABLE` 皆不會得到 `CLOSE`；補齊後可通過；完整 AC 文字與續行（含超過 400 字元者）完整出現在 `preflight.json`，stdout 預覽才截斷。
  3. 決策順序與有界性：四類 hard blocker 先於 RECONCILE／補證；RECONCILE 與其他補證共用上限，達上限仍矛盾→`NOT_CLOSED`，不重複回傳同一動作；核心 UNVERIFIED 的補證→fallback→`NOT_CLOSED` 序列；`decide` 省略 `--round` 或 `--fallback-tried`→exit 2（Rev A.2）；scope／`git diff --check` 為 UNKNOWN→`SUPPLEMENT(scope_unknown)`，達上限不新增終局（Rev A.2，X3）。
  4. dependency：`MISSING`→blocker；`LEAD`→補證，達上限→`NOT_CLOSED(dependency_lead_unresolved)`；清單不可得→`NONE_FOUND` 降為 `UNKNOWN`，`LEAD`／`MISSING` 不被降級。
  5. reviewer 分類與 fallback 一致：preset 非 `docs` 但 ALLOWED 僅 `.md`→Sonnet；含 `.py`（即使同時有 `.md`）→Codex；Codex／Sonnet 不可用的 fallback；皆不可用；`--escalate` 只針對所列 AC。
  6. 實作 diff 範圍：區間內混入他票已提交修改不被誤報為本票越界；無前綴提交觸及 ALLOWED→`UNKNOWN`；本票自身違規→`FAIL`；紀錄提交不納入；`DIFF_REF` 佔位時以 `BASE_COMMIT` 為下界，兩者皆無效→`UNKNOWN`；`git diff --check` 逐 own 提交。
  7. read-back 與 finalize：`reviews` 與預期不一致、lifecycle 非 CLOSED、`file` 不可解析時 `readback` 回 mismatch，`finalize` exit 3 且票檔零寫入、不出現「讀回一致」；`reviews_patch` 只處理缺漏欄位：有 `reviews_observed` 者採實際值、無者 `N_A`（並出現在最終結果披露）、已有值欄位不改、可選外審無證據不填 PASS（Rev A.2）。
  8. 綁定與部分成功：已 CLOSED 缺最終結果且 `decision.json` 綁定相符→補寫；票號、票檔或 `binding_ref` 任一不符→不補寫（如實回報）；歷史 CLOSED（無綁定決策）零修改；已完成重入冪等、不重複追加；`decide` 不覆蓋先前 `next=CLOSE` 的決策檔。
  9. 定位：冊面 `file` 過期而實檔在 `deliver/archive/**`、missing、ambiguous、lifecycle 與位置矛盾。
  10. 暫存與清理：到期（>7 天）且含檔案與子目錄的候選票號目錄被實際清除；七天內保留；`--exclude` 不刪；非票號樣式目錄與檔案不碰；候選內部的符號連結／junction 只移除連結、目標內容不受影響（環境無法建立連結時該案 `skipTest` 並標明原因，不得靜默通過）；超過 `--max` 時有界；刪除失敗不使命令非零；產出僅在 `.tmp/ticket-author/close/`；`git status` 除最終區段外無異動、無 commit。
  11. 最終結果：僅 CLOSED 且讀回與綁定通過時寫入；單一區段、≤20 行、自包含（不含 `.tmp` 路徑連結）；未 CLOSED 時零寫入；無永久中間 report 檔進入受版控路徑。
  12. 規則替換殘留 grep（`[存在性]`，對真實 repo 檔案）。
  reviewer 以替身表示；測試報告須標註「stub 通過≠真實模型行為」，亦不宣稱已證明真實 Agent 必定遵守 Skill。
- [x] 完成（見收尾實錄 §1–§6）

### AI-3: `ticket-author/SKILL.md` 新增 `close` 入口與 `Mode: close`

- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值與目標值**（五處）：
  1. frontmatter：`description: …Manual modes: diag, plan, ticket.` → `…Manual modes: diag, plan, ticket, close.`，並於描述加入 close（close an existing REVIEW Ticket on mechanical evidence）；`argument-hint: "diag|plan|ticket [task text and @sources]"` → `argument-hint: "diag|plan|ticket [task text and @sources] | close <TICKET_ID>"`。
  2. Mission 段：`This skill performs diagnosis, planning, evidence collection, and Ticket authoring.` → `This skill performs diagnosis, planning, evidence collection, Ticket authoring, and (in close mode) evidence-based closing of a REVIEW Ticket.`；其後 `It MUST NOT implement production code, execute a Ticket, or treat AI-generated material as sufficient live proof.` 原文不動。
  3. Invocation 的 Accepted modes 區塊新增一行 `/ticket-author close <TICKET_ID>`；`If mode is absent or unsupported, stop with STOP_INVALID_MODE` 原句不動。
  4. `Mode: ticket` 的 PASS 回報句：`This skill only authors new Tickets.` → `In ticket mode this skill only authors new Tickets; closing an existing REVIEW Ticket is close mode.`
  5. 於 `## Conditional state matrix` 之前新增 `## Mode: close` 整節，內容（逐字，英文，與本檔既有風格一致）：

````markdown
## Mode: close

Purpose: take one formal Ticket at `REVIEW` through deterministic local evidence, reviewer-backed AC verification and, once its delivery goal is established, an autonomous transition to `CLOSED` with Registry read-back. Invocation: `/ticket-author close <TICKET_ID>`. The autonomous workflow's Agent MAY invoke it (Transition Table: after Agent closeout reaches `REVIEW`); no per-Ticket Owner approval is requested. `close` does not author Tickets, execute a Ticket, or modify production code.

Scratch directory `<dir>` = `.tmp/ticket-author/close/<TICKET_ID>/`. Everything in `<dir>` (preflight output, AC detail, command output, raw reviewer responses, retries) is scratch: kept 7 days (cleaned by the helper when the command runs, not on a timer), never committed, never registered, never a dependency. The helper `close_helper.py` (standard library only) never calls the Registry or a model; the Agent reads and writes the Registry through the Registry tools and hands observations to the helper as JSON files in `<dir>`. The helper prints the next action.

```close-dispatch
python .claude/skills/ticket-author/scripts/close_helper.py preflight --ticket <ID> --registry <dir>/registry.json --dependents <dir>/dependents.json
python .claude/skills/ticket-author/scripts/close_helper.py reviewer --ticket <ID>
python .claude/skills/ticket-author/scripts/close_helper.py decide --ticket <ID> --registry <dir>/registry.json --verdicts <dir>/verdicts.json --round <N> --fallback-tried <0|1>
python .claude/skills/ticket-author/scripts/close_helper.py readback --ticket <ID> --registry <dir>/registry_after.json
python .claude/skills/ticket-author/scripts/close_helper.py finalize --ticket <ID> --registry <dir>/registry_after.json --verdicts <dir>/verdicts.json --summary <dir>/summary.txt --commits <h1,h2>
```

Procedure:

1. Observe through the Registry tools (authoritative channel): `registry_list(project=SmartCameraApp1, ticket_id=<ID>, include_archive=true, fields=["id","lifecycle","file","reviews","checkpoint"])` → write the single ticket object to `<dir>/registry.json`; `registry_list` over the other non-closed tickets with fields `["id","lifecycle","file","depends_on","blocked_by"]` → `<dir>/dependents.json`. A Registry that cannot be read is `STOP_ACCESS_BLOCKED`; an unknown Ticket ID is `STOP_MISSING_SUBJECT`.
2. Run `preflight`. Read its compact output, not raw files.
3. Run `reviewer`. Documents, rules text, metadata and other low-risk Tickets go to Sonnet first, Claude-written code and load-bearing logic to Codex first; the helper classifies by the files the Ticket changes, not by its PRESET name (a Ticket whose ALLOWED files are all plain text, `.md`, `.txt` or `.rst`, is low-risk; any code or other non-text file makes it code, even when text files come with it); if the primary is unavailable the other reviewer is used and the actual reviewer and limitation are recorded.
4. Verify each AC, evidence first. Read the mechanical evidence, rerun only cheap read-only commands that the evidence lacks, then compare it with the AC's meaning. Status per AC: `PASS` (direct, traceable mechanical evidence), `FAIL` (direct evidence it is not met), `N/A` (the Ticket itself strikes it out and the reason checks out), `UNVERIFIED` (cannot be safely rerun or evidence is insufficient; say so). An exit code supports only what the command actually checked; an implementation scope still `UNKNOWN` is not evidence that an AC about that scope passes; a checkbox, memory or an old prompt is never evidence; an AC that is hard to verify is not thereby non-core. Ask the selected reviewer with the compact evidence and the AC list only: Sonnet through the Agent tool with `model` set to `sonnet` (or inline when the session is already Sonnet); Codex by writing `<dir>/review_codex.md` and running `codex exec -c model="${CODEX_MODEL:-gpt-5.6-sol}" -c model_reasoning_effort="high" - < <dir>/review_codex.md`. Save raw responses to `<dir>/reviewer_<name>.txt`. Where mechanical evidence contradicts a reviewer, evidence wins and only the contradicting AC numbers go to the other reviewer (`reviewer --escalate`). Write `<dir>/verdicts.json` with exactly one entry for every AC that `preflight.json` lists (by `n`), each with `status`, `core` and a short evidence pointer; the complete AC text, including continuation lines, is in `preflight.json` and the stdout preview is only a preview. An optional reviewer being unavailable is not a defect, but a substantive core defect that any reviewer finds counts as evidence like any other. Core ACs and hard blockers are defined in `.claude/rules/ssot-governance.md` §Ticket Closure; this skill does not copy them.
5. Run `decide`. `--round` (supplement rounds so far) and `--fallback-tried` (1 once the other reviewer has been used) are required; `decide` refuses to run without them.
6. Act on `next`:
   - `SUPPLEMENT` or `RECONCILE`: collect the missing evidence (missing or invalid verdicts, a contradiction between evidence and a reviewer, a core `UNVERIFIED` AC, a concrete dependency lead, an implementation scope that the helper reports as `UNKNOWN`), at most 2 rounds per Ticket in total, update `verdicts.json`, run `decide` again with the new `--round`. At the cap `decide` returns `NOT_CLOSED`: never repeat the same action beyond the cap, and never invoke `close` again from inside `close`.
   - `FALLBACK_REVIEWER`: ask the other reviewer about the unresolved core ACs, update `verdicts.json`, run `decide` again with `--fallback-tried 1`.
   - `BLOCKED`: stop. Write nothing to the Ticket. Respond `STOP_DECISION_REQUIRED` naming the blocker and what must change.
   - `NOT_CLOSED`: the Ticket stays at `REVIEW`; write nothing to the Ticket; respond with the reason. Do not create a human-approval step.
   - `CLOSE`: when the decision says to resolve an open checkpoint, call `registry_set_checkpoint` with a `resolution` only because its matter is genuinely resolved; then `registry_set_status(lifecycle="CLOSED")` (plus the `reviews` patch from the decision; the patch fills only absent fields, with a result recorded in `verdicts.json` `reviews_observed` when mechanical evidence of that review exists and with `N_A` otherwise, and never fills an optional external review with `PASS` without evidence; the `N_A` fields are disclosed in the final result).
   - `ALREADY_CLOSED`: write nothing; go to step 7 for a read-back and respond.
   - `WRITE_FINAL_ONLY`: the Ticket is already `CLOSED` from an earlier run of this command; go to step 7 and step 8 only.
   - `PRECONDITION_FAILED`: respond `NOT_CLOSED` with the precondition (lifecycle other than `REVIEW`/`CLOSED`, Ticket file missing, ambiguous or contradicting the Registry); contradictions are `STOP_EVIDENCE_CONFLICT`.
7. Read back: `registry_list` again → `<dir>/registry_after.json`, then run `readback`. A mismatch is reported as is, never as consistent, and is `STOP_WRITE_FAILED` only after one minimal safe retry.
8. Only for a Ticket that is `CLOSED` in the read-back: write the 3-line delivery summary to `<dir>/summary.txt` and run `finalize`, which repeats the read-back check and the earlier-decision binding check itself and refuses to write when either fails. This minimal final result is the only thing kept on the original Ticket; a Ticket that was not closed gets nothing written.
9. Respond with the existing completion shape (`Mode: close`, `Gate: PASS`, `Final state: COMPLETE`) plus `Close result: CLOSED | ALREADY_CLOSED | NOT_CLOSED(<reason>)`, the actual reviewer(s) and limitations, and one line each for what actually happened: transition done or not, read-back ok or mismatch, final result written or skipped. Partial success is reported as partial. `STOP_*` is used only for the cases named above.

Re-entry is derived from observable state only (Registry lifecycle, the final-result section in the Ticket, and, when still present, an earlier `decision.json` in `<dir>` that is bound to this Ticket, its file and the implementation commit): a rerun after partial success updates the existing final-result section instead of appending; a `CLOSED` Ticket with no final-result section and no matching earlier close decision is left untouched and reported, so evidence lost after 7 days is never guessed back. Never batch-close or batch-edit closed Tickets. Losing `<dir>` after 7 days only means evidence is collected again.
````

  - 其餘 `SKILL.md` 既有字面（Core rules、Mode: diag／plan／ticket 等）不動。
- [x] 完成（見收尾實錄 §1–§6）

### AI-4: `references/` 三檔同步

- **檔案**: `references/stop-conditions.md`、`references/input-contract.md`、`references/state-transitions.md`（皆在 `.claude/skills/ticket-author/` 下）
- **現值→目標值**（逐字；外層以 `~~~~` 圍欄，內容中的反引號為字面字元）：

~~~~text
[stop-conditions.md] 現值：| `STOP_INVALID_MODE` | Mode absent or not `diag`, `plan`, `ticket` | `DECISION_REQUIRED` |
[stop-conditions.md] 目標：| `STOP_INVALID_MODE` | Mode absent or not `diag`, `plan`, `ticket`, `close` | `DECISION_REQUIRED` |
~~~~

~~~~markdown
[stop-conditions.md] 新增（插於 `## Decision-fork discipline` 之前）：

## `close` stops

`close` completes (Gate PASS) with `Close result: CLOSED | ALREADY_CLOSED | NOT_CLOSED(<reason>)` in every case except the stops below; it adds no reason code.

| Code | Trigger in `close` | Final state |
|---|---|---|
| `STOP_MISSING_SUBJECT` | Ticket ID missing, malformed, or not registered in the Registry (main or archive) | `DECISION_REQUIRED` |
| `STOP_ACCESS_BLOCKED` | The Registry cannot be read or written | `CHANNEL_BLOCKED` |
| `STOP_SOURCE_UNAVAILABLE` | A load-bearing source (the Ticket file) cannot be read | `CHANNEL_BLOCKED` |
| `STOP_EVIDENCE_CONFLICT` | Registry and Ticket file location contradict each other, or the Ticket file name is ambiguous | `DECISION_REQUIRED` |
| `STOP_DECISION_REQUIRED` | A hard blocker: a core AC is confirmed FAIL, a known security issue, irreversible damage or data-loss risk, or a contract that a dependent Ticket explicitly needs is not delivered | `DECISION_REQUIRED` |
| `STOP_WRITE_FAILED` | The transition or read-back still mismatches after one minimal safe retry | `CHANNEL_BLOCKED` |

A core AC that stays `UNVERIFIED` after supplementation and fallback, an unresolved checkpoint, and a lifecycle other than `REVIEW`/`CLOSED` are not stops: the Ticket stays as it is and the result is `NOT_CLOSED(<reason>)`.
~~~~

~~~~markdown
[input-contract.md] 新增（插於 `## Structured option` 之前）：

## `close`

Required: one formal Ticket ID registered in the target project's Registry (main or archive). `close` reads no parent artifact, plan item or `Fixture` header and never consults `diag`/`plan` artifacts. A missing or malformed ID stops with `STOP_MISSING_SUBJECT`.

The caller may be an Agent in the autonomous workflow or a person; no per-Ticket Owner approval is part of the input. Scratch input and output live in `.tmp/ticket-author/close/<TICKET_ID>/` (see `SKILL.md`, Mode: close).
~~~~

~~~~markdown
[state-transitions.md] 新增（插於 `## Regression transitions` 之前）：

### `close`

```text
INTAKE_PARSED
→ registry observed
→ preflight
→ reviewer selected
→ AC verdicts
→ decision
→ [supplement | fallback reviewer]*
→ transition
→ read-back
→ final result
→ COMPLETE
```

Mapping to existing states and receipts only (no new state): the preflight output is `LIVE_EVIDENCE`; verdicts and the decision are `MODE_ANALYSIS`/`MODE_GATE`; the transition and read-back are `OUTPUT_READY`. Re-entry is derived from observable state (Registry lifecycle, the final-result section in the Ticket, and an earlier `decision.json` while it still exists in the scratch directory); no manifest or state file is created.
~~~~

- [x] 完成（見收尾實錄 §1–§6）

### AI-5: `ssot-governance.md` 直接替換（R1–R10）

- **檔案**: `.claude/rules/ssot-governance.md`
- **原則**：下列皆為整句／整列／整條**替換或刪除**，不得以括註、豁免或平行規則疊加；完成後 AI-8 ② 的殘留 grep 須零命中。區塊內容為逐字文字（反引號為字面字元）。

**R6（:81 列尾）**

```text
現值：，Traycer T2 另由 Akira 於 VS Code 手動執行 T2/CSO 複審 |
目標：；Traycer T2 為可選外部審查，不阻擋後續 close |
```

**R1（:82 整列）**

```text
現值：| ⑤ `CLOSED` | 複審通過 | **限審查方 SMTR/CSO**（終審 `reviews` 回填＋收束 note） |
目標：| ⑤ `CLOSED` | 核心 AC 成立且無 hard blocker（`/ticket-author close` 流程判定） | 執行者（Agent）經 `/ticket-author close <票號>` 寫入（`reviews` 讀回確認＋票檔最小最終結果） |
```

**R7（:88 粗體句）**

```text
現值：**⑤段收束時 MUST 逐項核對 `verification.required` 各值於 `reviews` 有對應結果（PASS 或明示豁免記於 note），有缺即不得轉 `CLOSED`。**
目標：**close 轉 `CLOSED` 前，核心 AC 所依賴之必要驗證（`verification.required` 中屬核心者）MUST 於 `reviews` 有對應結果；可選外審（Traycer／T2）與非核心項之缺漏依 §Ticket Closure 紀律之披露規則處理，不阻擋 `CLOSED`。**
```

**R5（:96 整段）**

```text
現值：Traycer 為封閉 VS Code plugin，**MUST NOT** 假設可由 agent／CLI／MCP 工具鏈外部觸發。票面聲明需 Traycer T2 審查者，該審查動作 **MUST** 由人工（Akira）於 VS Code 內手動執行；執行端 **MUST NOT** 因無法觸發而靜默跳過、推定 PASS，或另尋機械替代路徑。此類票之收尾 **MUST** 正常轉入 `④REVIEW`（票檔搬 `deliver/`＋`file` 欄及冊面更新，同 §「五段觸發與寫入者」④列），**MUST NOT** 停於 `③EXECUTED`——`REVIEW` 本身即為「等待最終 T2／CSO 複審」之合理停留態，`EXECUTED` 僅代表「AC 自驗全綠、未過 Cross Review」之更早期狀態，用其表示「等 Traycer」語意錯位。`reviews.traycer` 欄位維持 `PENDING`（或依 §4.5 契約填 `N_A`）以標記人工複審未完成；lifecycle 本身 **MUST NOT** 因此卡在 `EXECUTED`。
目標：Traycer 為封閉 VS Code plugin，**MUST NOT** 假設可由 agent／CLI／MCP 工具鏈外部觸發。票面聲明 Traycer T2 者，T2 為**可選外部審查**（由人於 VS Code 手動執行）：執行端 **MUST NOT** 推定其 PASS 或另尋機械替代路徑，亦 **MUST NOT** 以其未執行阻擋收束。此類票之收尾 **MUST** 正常轉入 `④REVIEW` 並由 close 繼續；`reviews.traycer` 維持 `PENDING`（或依票面填 `N_A`）；T2 後續若有具體 finding，另開 repair ticket。`EXECUTED` 僅代表「AC 自驗全綠、未過 Cross Review」，**MUST NOT** 用以表示「等 Traycer」。
```

**R2（:142 整條）**

```text
現值：4. **Agent 寫入上限**：Agent 最高可寫 lifecycle 為 `REVIEW`；`CLOSED` 限審查方 SMTR/CSO（回指 §五段觸發與寫入者⑤）。
目標：4. **Agent 寫入範圍**：Agent 可寫 lifecycle 至 `CLOSED`；`CLOSED` 僅得經 `/ticket-author close` 流程寫入（核心 AC 成立且無 hard blocker，見 §Ticket Closure 紀律）。
```

**R10（新增列；插於 :159 列之後、:160 列之前）**

```text
目標：| Agent closeout 至 `REVIEW` 於本次呼叫實際完成，或「已收束重入」判得 `lifecycle = REVIEW`（若上一列 projman align 條件成立，先依該列處理） | `MUST AUTO-CHAIN` 呼叫 `/ticket-author close <票號>`（一次；close 不自行重呼叫自己） | 逐票等待 Owner 批准、以 `REVIEW` 為停點、close 未達 `CLOSED` 後反覆重呼叫 |
```

**R4（:160 整列）**

```text
現值：| 已收束重入（`lifecycle ∈ {REVIEW, CLOSED}` 且票檔位於 `deliver/`） | `MUST RETURN`（見下「最小冪等回傳」） | 重寫 status、二次搬移票檔、二次寫等價 checkpoint、建空 commit |
目標：| 已收束重入（`lifecycle ∈ {REVIEW, CLOSED}` 且票檔位於 `deliver/`，含 `deliver/archive/`） | `lifecycle = CLOSED`：`MUST RETURN`（見下「最小冪等回傳」）；`lifecycle = REVIEW`：closeout 部分 `MUST RETURN`（不重做），並接續上列 close 列 | 重寫 status、二次搬移票檔、二次寫等價 checkpoint、建空 commit |
```

**R5'（:168 整條）**

```text
現值：- **`PENDING` MUST NOT 被當作 blocker**——lifecycle 正常停留於 `REVIEW`，等待人工 Traycer 複審與終審方轉 `CLOSED`。
目標：- **`PENDING` MUST NOT 被當作 blocker**——Agent closeout 後 lifecycle 為 `REVIEW`，由 close 繼續判定並轉 `CLOSED`；T2 為可選外審。
```

**R4'（:172 整段）**

```text
現值：`registry lifecycle ∈ {REVIEW, CLOSED}` **且**票檔實際位於 `deliver/` → `MUST RETURN`（已收束）：**MUST NOT** 再呼叫 `registry_set_status`／`registry_upsert_ticket` 轉態、**MUST NOT** 再搬移或 unlink 票檔、**MUST NOT** 再寫等價 checkpoint、**MUST NOT** 建立空 commit。二者不一致（如票檔已搬 `deliver/` 而冊面 `file` 仍指 `Pending/`）→ 交既有 Closure Gate 判定（回指 §Ticket Closure 紀律「矛盾停止」），Agent **MUST NOT** 猜測修復。
目標：`registry lifecycle = CLOSED` **且**票檔實際位於 `deliver/`（含 `deliver/archive/`；冊面 `file` 過期者以實檔位置為準）→ `MUST RETURN`（已收束）：**MUST NOT** 再呼叫 `registry_set_status`／`registry_upsert_ticket` 轉態、**MUST NOT** 再搬移或 unlink 票檔、**MUST NOT** 再寫等價 checkpoint、**MUST NOT** 建立空 commit。`lifecycle = REVIEW` 且票檔位於 `deliver/` 時，closeout 動作（搬移票檔、等價 checkpoint）視為已完成而不重做，但 **MUST NOT** 因此阻擋 `/ticket-author close`（其轉 `CLOSED` 不屬 closeout 重入）。二者不一致（如票檔已搬 `deliver/` 而冊面 `file` 仍指 `Pending/`）→ 交既有 Closure Gate 判定（回指 §Ticket Closure 紀律「矛盾停止」），Agent **MUST NOT** 猜測修復。
```

**R3（:205 片段）**

```text
現值：`HUMAN_GATE`（Traycer T2／`CLOSED`／Rule 4a 無票）
目標：`HUMAN_GATE`（Rule 4a 無票）
```

**R8a（:277 片段）**

```text
現值：並於 PROGRESS.md 誠實揭露（K5-Rule 29）
目標：並於票檔最終結果（close）誠實揭露（K5-Rule 29）
```

**R8b（:280 整條）**

```text
現值：- **修補票紀律**：母票存在未閉環 AC → **MUST** 同步產出修補票（`-FIX-[X]` 後綴或後續票）置於 `Pending/`，載明追蹤條目與轉 PASS 條件。
目標：- **修補票紀律**：核心 AC 未閉環者票不得轉 `CLOSED`（見下「核心 AC 與 close 判定」）；非核心 AC 未閉環只於票檔最終結果披露，**不強制**開修補票，執行端得自行判斷另開 follow-up。
```

**R8c（:281 整條）**

```text
現值：- 母票 AC 佐證不明（無法判定該勾或保留）→ **MUST** 停止勾選該項並列入修補票追蹤，禁止推定勾選。
目標：- 母票 AC 佐證不明（無法判定該勾或保留）→ **MUST** 停止勾選該項並於票檔最終結果披露；該項為核心 AC 時，票保留 `REVIEW`（先依 close 補證與 fallback），禁止推定勾選。
```

**新增兩條（插於 :281 之後）**

```text
- **核心 AC 與 close 判定**：核心 AC＝其不成立則票面一句話交付目標不成立之 AC；難以驗證不是降級為非核心的理由，有疑義時從嚴視為核心並先補證；僅「與交付目標無必要性」或票面明示為可選／觀測／外審者為非核心。close 轉 `CLOSED` 須所有核心 AC 為 PASS 或有效 N/A。hard blocker 僅：核心 AC 已確認 FAIL、已知安全問題、不可逆損害／資料遺失風險、下一張相依票明確需要但尚未交付的 contract（只查直接相依與具體 contract；有具體缺件線索者針對該線索補查，不以「未知」蓋過；無法判定且無線索時標示「未知」，不宣稱已確認無缺件）。核心 AC 為 UNVERIFIED 時先自主補證與 fallback，仍不足則保留 `REVIEW`，不報 PASS，亦不新增人工批准步驟。可選外審工具的缺席不構成缺陷，但其發現的實質核心缺陷同樣計入核心 AC 判定。非核心 UNVERIFIED、有效 N/A、可選測試未跑、首次真實使用待觀察、可選外審不可用，披露於票檔最終結果，不阻擋 `CLOSED`。
- **checkpoint 處置**：轉 `CLOSED` 前若冊面 checkpoint 為 `OPEN`，僅於其事項確實解決時以 `resolution` 標 `RESOLVED`（其事項僅為已被本節取消之人工關票等待者，視為已解決，於 `resolution` 註明）；事項未解決則不為通過平台檢查而標記，票保留 `REVIEW`。
```

**R9a（:282 之 ④ 至句末）**

```text
現值：④收尾實錄之耐久載體為**票檔（`deliver/`）＋vault 決策檔**；`checkpoint` 為收尾前之工作載體，依 PLT 契約於 `set_status(CLOSED)` 轉態時清為 `null` 並 unlink `.tmp/checkpoints/{ticket_id}.md`（見「checkpoint 生命週期與 detail 檔」節）。**轉終態前**，收尾實錄 MUST 已落票檔；若需保留冊面可見之收尾摘要，MUST 於 `set_status(CLOSED)` **之後**以 `registry_set_checkpoint` 回填（此序不改變已轉 `CLOSED` 之 `closed_at`）。
目標：④收尾結果之耐久載體為**票檔（`deliver/`）內之最小最終結果**（交付摘要、最終驗收結論、實作提交、實際收束結果），於轉態與 read-back 完成後寫入；close report、AC 明細、命令輸出、reviewer 回應等中間衍生物只存 `.tmp` 七天，不 commit、不上 TKR；`checkpoint` 為收尾前之工作載體，依 PLT 契約於 `set_status(CLOSED)` 轉態時清為 `null` 並 unlink `.tmp/checkpoints/{ticket_id}.md`（見「checkpoint 生命週期與 detail 檔」節），冊面不要求保留 CLOSED 後之 checkpoint 摘要。
```

**R9c（:282 之 ② 子句；Rev A.2 補，與 R9a 同行）**

```text
現值：②`reviews` 四欄（qa／sv／codex／traycer）各填實測值，無可考記錄者填 `N_A` 並於 checkpoint 揭露豁免依據，**MUST NOT** 以推定補 `PASS`（撤警正向取證律）；
目標：②`reviews` 四欄（qa／sv／codex／traycer）有可考記錄者填實測值（既有 enum，附證據定位），無可考記錄者填 `N_A` 並於票檔最終結果之披露行揭露依據，**MUST NOT** 以推定補 `PASS`（撤警正向取證律），可選外審無證據者亦不得填 `PASS`；
```

**R9b（:283 整條）**

```text
現值：- **原子性**：四要件 **MUST** 於同一收尾回合內完成，**MUST NOT** 分批跨 session；任一要件缺漏即視為該票**未收尾**，不得回報結案。`CLOSED` 情形之合法時序為「②③備妥＋④收尾實錄落票檔（四要件之必要部分，轉態前完成）→ ①`set_status(CLOSED)` 執行轉態 →（若需）checkpoint 摘要回填（④之選填延伸，非四要件計數對象）」，該序**不違反**本條之同回合要求。冊面異動之 APP Git persistence 依 §Session Persistence 邊界第 5 點**雙軌擇一**執行（隨票即時 `[<TICKET_ID>]` 單票 commit，或延至 `/compact closeout` 走 `[GOVERNANCE-SYNC]`）；scoped `git_diff` 零意外變更之驗證義務隨該軌道執行，**MUST NOT 取消**。
目標：- **原子性**：①②③ 與轉態 **MUST** 於同一收尾回合內完成；④最小最終結果於轉態與 read-back 後寫入，部分成功時如實回報並允許同票重入補寫，**MUST NOT** 虛報結案。冊面異動之 APP Git persistence 依 §Session Persistence 邊界第 5 點**雙軌擇一**執行（隨票即時 `[<TICKET_ID>]` 單票 commit，或延至 `/compact closeout` 走 `[GOVERNANCE-SYNC]`）；scoped `git_diff` 零意外變更之驗證義務隨該軌道執行，**MUST NOT 取消**。
```

- [x] 完成（見收尾實錄 §1–§6）

### AI-6: `sv/SKILL.md` Step 11 接續 close

- **檔案**: `.claude/skills/sv/SKILL.md`
- **現值→目標值**（逐字，反引號為字面字元）：

```text
現值：執行 Agent closeout 至 `REVIEW`。本節為引用
目標：執行 Agent closeout 至 `REVIEW`，並於其後 `MUST AUTO-CHAIN` `/ticket-author close <TICKET_ID>`（該表 close 列）。本節為引用
```

```text
新增（第 3 項之後）：4. **自主收束**：第 3 項之後（無論 align 是否觸發）**MUST AUTO-CHAIN** 呼叫 `/ticket-author close <TICKET_ID>`；第 1 項命中「已收束重入」且 `lifecycle = REVIEW` 者亦接續 close。close 的取證、reviewer、轉態與 read-back 由 `ticket-author` 的 `Mode: close` 規範，此處**不複製**。
```

- [x] 完成（見收尾實錄 §1–§6）

### AI-7: 同次最小更新過期的 closure 專案記憶（repo 外，不入 git diff scope）

- **檔案**: `C:\Users\Administrator\.claude\projects\D--DevSources-AndroidProj-SmartCameraApp1\memory\governance-ticket-closure-sop-target-state.md` 與同目錄 `MEMORY.md` 對應索引行
- **現值→目標值**（逐字，反引號為字面字元；僅修改這些處，其餘內容不動，不新增記憶檔）：

```text
現值（description）：/sv PASS 後（governance 票免 Traycer）標準歸檔 SOP 三動作 + 冊面目標狀態為 REVIEW 非 CLOSED
目標：/sv PASS 後（governance 票免 Traycer）標準歸檔 SOP 三動作 → REVIEW；2026-10-21 起 REVIEW 後由 /ticket-author close 自主 CLOSED
```

```text
現值（第 3 點）：3. `lifecycle` 轉態目標為 **`REVIEW`**（不是 `CLOSED`）——`CLOSED` 是後續 Desktop 治理 session（如 SMTR）獨立收束覆核後才轉的終態，Claude Code session 完成 /sv 閉環時只推進到 `REVIEW`。
目標：3. `lifecycle` 轉態目標為 **`REVIEW`**；隨後由 `/ticket-author close <票號>`（自主工作流 Agent 可調用）完成 REVIEW→CLOSED 與 read-back，不再等待 SMTR／Desktop 覆核（TKA-CLOSE-01）。
```

```text
現值（Why 段整段）：自 `**Why**：Akira 2026-08-26 對話明確指定此三動作序列與目標狀態` 起，至該段結尾（含「冊面轉態屬 Desktop 治理 session 職責…Claude Code session MUST NOT 自行寫入 TICKET_REGISTRY.json 或搬移票檔」之敘述）
目標：**Why**：Akira 2026-08-26 指定三動作序列；2026-10-21 裁決「自主閉環優先」，REVIEW 後經 close 自主 CLOSED，不再人工接力（見 TKA-CLOSE-01、ssot-governance.md §Ticket Closure 紀律）。
```

```text
現值（MEMORY.md 索引行片段）：lifecycle→REVIEW（非CLOSED）
目標：→REVIEW，之後經 /ticket-author close 自主 CLOSED
```

- How to apply 段保留（其「自主以 MCP 正規通道執行三動作」與新規則一致）。
- [x] 完成（見收尾實錄 §1–§6）

### AI-8: 檢核、殘留 grep、測試執行與收尾實錄

- **動作**: ①`python -m unittest discover -s .claude/skills/ticket-author/tests -p "test_*.py"` exit 0，並把各案結果（含 `skipTest` 原因）如實列入收尾實錄；②殘留 grep（`ssot-governance.md`、`sv/SKILL.md`、`ticket-author/`）下列舊字面零命中：`限審查方 SMTR/CSO`、`Traycer T2 另由 Akira`、`等待最終 T2／CSO 複審`、`等待人工 Traycer 複審與終審方轉`、`HUMAN_GATE（Traycer T2／`、`於 PROGRESS.md 誠實揭露（K5-Rule 29）`、`停止勾選該項並列入修補票追蹤`、`Agent 最高可寫 lifecycle 為`、`並於 checkpoint 揭露豁免依據`（Rev A.2，R9c）；③`git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉 ALLOWED，FORBIDDEN 各檔 diff 為空；新增行 `STOP_[A-Z_]+` 集合為既有碼子集；未新增 Gate／state／ledger／manifest／schema／hook／daemon；④既有 `bash .claude/skills/qa/tests/test_report_fix_closure.sh`（exit 0）與 `python -m unittest discover -s .claude/scripts/tests -p "test_gate_ledger.py"` 仍通過（本票未觸及，抽查回歸）；⑤`/mdlint` 對三份 `.md` 不高於 AI-0 baseline；⑥對真實 repo 以 `preflight` 對一張既有 CLOSED archive 票（例如 `TKA-ALIGN-01`）做一次唯讀 dry-run，前後 `git status`、`tmp/verify/active.json`、`.git/gate/` 無異動；⑦收尾實錄如實載明未驗證項：真實 Sonnet／Codex 判讀品質、Agent 是否依 `Mode: close` 調度、使用者層級權限；這些**不**作為本票或其他票之關票前置；⑧`/qa`、`/sv` 依既有流程；本票完成後依新規則經 close 收束時，不以首次真實使用作為永久前置。
- [x] 完成（見收尾實錄 §4、§6、§7）

## Hard Constraints

1. 不新增 mode 以外的治理系統：不得新增 Gate、state、ledger、manifest、schema、hook、daemon、DB、reason code 或平行狀態機；`close` 子命令本身是本票交付。
2. 阻擋自主閉環的舊條款一律直接替換或刪除（AI-5 的 R1–R10），**不得**以括註、豁免、平行規則或「除非…」疊加；完成後不得留存互相矛盾的字面。
3. helper 僅用標準庫；不呼叫 Registry、模型或網路；git 僅唯讀；寫入僅限 `.tmp/ticket-author/close/<票號>/` 與 `finalize` 對目標票檔之單一最小區段；不得寫 `tmp/verify/active.json`、`.git/gate/`、`TICKET_REGISTRY*.json`；不得 `git add`／`commit`／`checkout`／`reset`／`cherry-pick`；不擴成 Git 歷史管理或 worktree 平台。
4. 原票僅在該票於 Registry 讀回為 `CLOSED`、`verify_readback()` 與 `verify_binding()` 皆通過時寫入最小最終結果（≤20 行、單一區段、自包含）；close report、AC 明細、命令輸出、reviewer 原始回應、重試紀錄只存 `.tmp` 七天，不 commit、不上 TKR、不寫回原票作永久 report，最終結果不得只留七天後失效的暫存連結作唯一證據。
5. 七天清理：明確以 `age > 7×86400` 秒判定到期；第一層只枚舉票號樣式目錄；允許安全清除到期候選的內部內容；不跟隨符號連結／junction、不越界；排除本次輸出目錄；每次至多 200 個候選；失敗不阻擋主流程；不得以 hook、排程或常駐程序執行；文件與回報不得宣稱無後續呼叫也能在第七天精確刪除。
6. 核心 AC 依交付目標必要性判定，不得因難驗而降級；外審工具可選但其發現的實質核心缺陷計入判定；核心 UNVERIFIED 不得報 PASS，先用既有有效證據，再自主補證／fallback（補證與矛盾處理合計至多 2 輪、fallback reviewer 至多 1 次），仍不足則保留 `REVIEW`，不得製造人工批准步驟；hard blocker 僅限四項且優先於 RECONCILE；既有停止碼不得被解讀為必須 Owner 人工批准。
7. `decide` 必須對照 preflight 的原票完整 AC 清單驗證 verdict 完整性：不得以空集合、漏項、重複或不存在的 AC 使「所有核心 AC 通過」成立；AC 清單不可得時不得默認完成；AC 原文與續行完整保存，400 字元限制僅用於 stdout 預覽。
8. dependency：只查直接相依與具體必要 contract，不遞迴掃描；直接證據缺件→blocker；具體線索→針對線索補查，不以 UNKNOWN 蓋過；單純無法取得完整清單→標「未知」，不憑空判缺件，亦不虛報已全面查核。
9. `finalize` 與 `readback` 共用同一判準；`reviews` 依平台契約與適用結果比對，不為填滿四欄假填可選外審 PASS；缺漏欄位有可考結果（`reviews_observed`）者填實際值、無者填 `N_A` 並於最終結果披露，`set_status` 送出值、`readback` 預期與 `finalize` 披露使用同一計算結果（Rev A.2，R9c）；讀回不一致不得寫「讀回一致」；已轉 CLOSED 但讀回或寫入失敗須如實回報部分成功，後續只補缺。
10. checkpoint 僅在其事項確實解決時標 `RESOLVED`，不得為讓平台接受 CLOSED 而清除尚未解決的實質問題；僅為已被新規則取消的人工關票等待者，視為已解決並於 `resolution` 註明。
11. `decision.json` 須綁定票號、目標票檔與對應實作基線／提交，`finalize`／`decide` 實際檢查；不新增 manifest、永久 receipt 或狀態平台；歷史 CLOSED 票不批次補寫、不重驗、不修改；七天後暫存遺失而無法確認屬本次部分成功→如實回報，不猜測補寫；不修改 `TKA-BATCH-01`、`TKA-EVGATE-01` 等已 CLOSED 票檔。
12. 實作 diff 範圍逐提交辨識歸屬；不把 ADV 或其他票已提交的修改誤算為本票 scope violation；不宣稱僅靠最新本票前綴提交已排除所有他票變更；無法建立可信範圍標 UNKNOWN 並針對性補證。
13. reviewer 分類依本票實際改動的檔案類型（helper、SKILL、測試一致），不依 preset 名稱；不建通用 router；真實模型不可用、品質未測與 MCP 實際權限限制如實揭露，不以 stub PASS 冒充驗證，也不繞過平台權限。
14. 不修改 FORBIDDEN 內任何檔（含 `gate_ledger.py`、`verify_checklist.sh`、`cross_review.sh`、`.claude/CLAUDE.md`、`settings.json`、Template、Registry 檔）；不補造或回填歷史 receipt；保留他票已提交的有效修改，不覆寫。
15. 測試只用 scratch 假 repo 與替身，不建正式票、Registry entry、Pending 檔或 commit。
16. 不得宣稱已提升 Traycer 通過率、token 效率、關票速度或品質指標，亦不宣稱已證明真實 Agent 必定遵守 Skill。

## Error Handling

- AI-0 錨點漂移（含因 ADV 等已提交修改）→ 不直接停工：以 live 讀取重新定位、確認需求仍存在、同票更新錨點並記入收尾實錄後續作；僅在實際存在無法安全判定的語義衝突時，精確回報該點（不得憑記憶或猜測推定等價位置）
- `git check-ignore -v` 命中任一 ALLOWED 路徑 → 停止回報（gitignore 死結律）
- 目前分支未包含 `54f5624a`（`git merge-base --is-ancestor` 非 0）→ 先確認提交與分支關係，沿專案既有安全方式處理；不得盲目 cherry-pick、reset 或改寫歷史
- 共享檔有未辨識的未提交變更 → 先辨識歸屬並保留，不 reset、不覆寫，不把他票變更算成本票實作；僅在無法安全判定語義衝突時精確回報
- 無法在不修改 FORBIDDEN 檔下完成（例如須改 `gate_ledger.py` 才能表達 receipt）→ 停止，請求裁決，不擴大範圍
- 無法在不新增 Gate／state／ledger／manifest／schema／hook／daemon／reason code 下表達 → 停止回報
- AI-5 改寫後仍須以括註或疊加規則才能消除矛盾 → 停止回報
- 環境無法建立符號連結／junction 以測試「不跟隨連結」→ 該案 `skipTest` 並在收尾實錄明載原因，不得標為通過
- `python`／`unittest` 不可用 → 停止回報
- 修改後 `git diff --stat` 觸及 ALLOWED 以外檔案 → 停止回報

## close 決策表（`decide` 唯一依據；上而下首個成立者生效；`--round` 為補證／矛盾處理合計輪數，上限 2）

| # | 條件 | `next` |
| --- | --- | --- |
| P | 冊面 lifecycle 非 `REVIEW`／`CLOSED`；或票檔 `missing`／`ambiguous`；或位置與 lifecycle 矛盾 | `PRECONDITION_FAILED`（回報 `NOT_CLOSED(precondition)`；矛盾／同名多筆為 `STOP_EVIDENCE_CONFLICT`） |
| C1 | lifecycle=`CLOSED` 且票檔已有最終結果區段 | `ALREADY_CLOSED` |
| C2 | lifecycle=`CLOSED`、無最終結果區段，且 `decision.json` 存在、`next=CLOSE`、`verify_binding()` 通過（票號、目標票檔、`binding_ref` 皆相符） | `WRITE_FINAL_ONLY` |
| C3 | lifecycle=`CLOSED`、無最終結果區段，且無通過綁定的先前決策 | `ALREADY_CLOSED`（註記「歷史 CLOSED 或無法確認屬本次部分成功，未修改」） |
| B1 | （以下限 lifecycle=`REVIEW`）已提供 verdicts 中任一核心 AC=`FAIL` | `BLOCKED(core_ac_fail)` |
| B2 | `security_issue=true` | `BLOCKED(security)` |
| B3 | `irreversible_risk=true` | `BLOCKED(irreversible)` |
| B4 | `dependency_contract.status=MISSING` | `BLOCKED(dependency_contract)` |
| V1 | `acs_status=UNAVAILABLE` | `NOT_CLOSED(ac_list_unavailable)` |
| V2 | verdicts 缺項、重複序號、引用不存在 AC、`status`／`core` 非法，或 `acs` 為空，且 `--round` < 2 | `SUPPLEMENT(verdicts_incomplete，列出 missing／duplicate／unknown)` |
| V3 | 同上，`--round` ≥ 2 | `NOT_CLOSED(verdicts_incomplete)` |
| X1 | 實測 scope 或 `git diff --check` 為 `FAIL`，而 verdicts 無任何核心 AC 為 `FAIL`，且 `--round` < 2 | `RECONCILE` |
| X2 | 同上，`--round` ≥ 2 | `NOT_CLOSED(reconcile_unresolved)` |
| X3 | （Rev A.2）實測 scope 或 `git diff --check` 為 `UNKNOWN`，且 `--round` < 2 | `SUPPLEMENT(scope_unknown)`（針對性補證，裁決 H） |
| U1 | 任一核心 AC=`UNVERIFIED` 且 `--round` < 2 | `SUPPLEMENT` |
| U2 | 同上，`--round` ≥ 2，`--fallback-tried=0` 且另一 reviewer 可用 | `FALLBACK_REVIEWER` |
| U3 | 同上，`--round` ≥ 2 且已 fallback 或無可用 fallback | `NOT_CLOSED(core_unverified)` |
| L1 | `dependency_contract.status=LEAD` 且 `--round` < 2 | `SUPPLEMENT(dependency_lead)` |
| L2 | 同上，`--round` ≥ 2 | `NOT_CLOSED(dependency_lead_unresolved)` |
| K1 | 核心 AC 皆為 PASS／有效 N/A，checkpoint=`OPEN` 且 `checkpoint_matter_resolved≠true` | `NOT_CLOSED(checkpoint_unresolved)` |
| K2 | 同上，checkpoint=`OPEN` 且 `checkpoint_matter_resolved=true` | `CLOSE(resolve_checkpoint)` |
| K3 | 核心 AC 皆為 PASS／有效 N/A，checkpoint 為 none／`RESOLVED` | `CLOSE` |

`dependency_contract.status=UNKNOWN`、非核心 UNVERIFIED、有效 N/A、可選外審未做 → 不影響 K1–K3，一律披露於最終結果。B 列優先於 V／X／U／L（已確認的 hard blocker 不被 RECONCILE 或補證延遲）。X3 達上限後不另立終局、不新增 hard blocker：範圍仍為 `UNKNOWN` 時，涉及實作範圍之 AC 不得以 `UNKNOWN` 範圍判 `PASS`——核心者維持 `UNVERIFIED` 走 U 列，非核心者披露於最終結果（Rev A.2）。`NOT_CLOSED` 之後不再回傳任何補證動作，亦不再鏈出 close。

## 狀態矩陣（涵蓋本票可達且與契約相關之組合；開票時建立，Rev A.1 增列 S12–S16）

| # | lifecycle | 位置 | 冊面 `file` | 最終結果區段 | 先前 decision | checkpoint | 預期 `next` / 結果 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| S1 | REVIEW | deliver | 正確 | 無 | 無 | 無 | 依 verdicts：B／V／X／U／L／K 表 |
| S2 | REVIEW | deliver | 正確 | 無 | 無 | OPEN，事項已解決（含已取消的人工關票等待） | `CLOSE(resolve_checkpoint)` |
| S3 | REVIEW | deliver | 正確 | 無 | 無 | OPEN，事項未解決 | `NOT_CLOSED(checkpoint_unresolved)` |
| S4 | REVIEW | Pending（未搬移） | 指 Pending | — | — | — | `PRECONDITION_FAILED`（矛盾→`STOP_EVIDENCE_CONFLICT`） |
| S5 | CLOSED | deliver/archive | 過期 | 有 | — | null | `ALREADY_CLOSED`，零寫入 |
| S6 | CLOSED | deliver | 正確 | 無 | `CLOSE`，綁定通過 | null | `WRITE_FINAL_ONLY`（部分成功補寫） |
| S7 | CLOSED | deliver | 正確 | 無 | 無 | null | `ALREADY_CLOSED`（歷史，未修改） |
| S8 | CLOSED | deliver | 正確 | 有 | 任意 | null | `ALREADY_CLOSED`，重跑不重複追加 |
| S9 | IN_PROGRESS | Pending | 指 Pending | — | — | — | `PRECONDITION_FAILED`→`NOT_CLOSED(precondition)` |
| S10 | REVIEW | deliver | 正確 | 無 | `CLOSE` 但轉態失敗 | OPEN/無 | 由 verdicts 重算；不得因 decision.json 殘留而跳過轉態 |
| S11 | CLOSED（讀回後） | deliver | 正確 | finalize 失敗 | `CLOSE`，綁定通過 | null | 回報部分成功；重入走 S6 |
| S12 | REVIEW | deliver | 正確 | 無 | 無 | 無 | verdicts 為空或缺項→V2（補證）→達上限 V3；不得因空集合 `CLOSE` |
| S13 | CLOSED | deliver | 正確 | 無 | `CLOSE` 但票檔／`binding_ref` 不符（舊暫存殘留） | null | `ALREADY_CLOSED`（C3），不補寫 |
| S14 | REVIEW | deliver | 正確 | 無 | 無 | 無 | 區間混入他票已提交修改→scope 不計其為本票違規；有無前綴提交觸及 ALLOWED→scope `UNKNOWN`→X3 補證；達上限仍 UNKNOWN→涉及範圍之核心 AC 維持 UNVERIFIED 走 U 列、非核心披露（Rev A.2） |
| S15 | REVIEW | deliver | 正確 | 無 | 無 | 無 | `dependency_contract=LEAD`→補證；達上限→`NOT_CLOSED(dependency_lead_unresolved)`；不被 `UNKNOWN` 蓋過 |
| S16 | CLOSED | deliver | 正確 | 無 | `CLOSE`，綁定通過 | null | 先 `verify_readback()` 再 finalize；讀回不一致→exit 3、零寫入、如實回報 |

反例記錄（Evidence gate 條款；各取一個符合前提而結果不同之狀態，並含處置）：①`:172` 原文使 `REVIEW`＋`deliver/` 一律 RETURN（符合前提卻阻擋 close，與目標衝突）→ 以 R4／R4' 直接改寫，CLOSED 冪等保留（S5、S8）；②AUTO-CHAIN 的 closeout→close 遞迴風險：close 未達 CLOSED 時 `REVIEW` 重入只觸發一次 close，不自我重呼叫（V3、X2、U3、L2、K1 皆回報 NOT_CLOSED 而不再鏈）；③冊面 `file` 過期但實檔在 archive（S5，live 證據：`TKA-ALIGN-01`、`TKA-CHANNEL-01`）；④部分成功重入（S6、S10、S11、S16）；⑤`DIFF_END` 導出遇「僅觸及票檔的紀錄提交」→不納入；遇區間內混入他票提交（Rev A.1：逐提交辨識，不計為本票違規）；無可信本票提交→UNKNOWN 而非 PASS；⑥核心 AC 僅 UNVERIFIED（U1–U3）→不報 PASS；⑦reviewer 皆不可用→`actual=none`，核心 AC 走 U1–U3；⑧dependents 清單不可讀→`NONE_FOUND` 降為 `UNKNOWN`，不宣稱已確認無缺件；⑨`sweep` 遇到連結或本次輸出目錄→連結只移除連結本身、本次目錄略過；⑩historical CLOSED 且無綁定決策（S7）→不被補寫；⑪空 `acs` 使「所有核心 AC 通過」因空集合成立（Rev A.1 新增）→V2／V3 擋下；⑫舊暫存的 `decision.json` 與本次票檔或實作提交不符（S13）→綁定失敗，不補寫；⑬`LEAD` 被 `UNKNOWN` 蓋過（S15）→`LEAD` 不被降級；⑭`preset` 非 `docs` 的純 `.md` 票與含 `.py` 又含 `.md` 的票（Rev A.1）→依檔案類型分別走 Sonnet、Codex；⑮第一層不遞迴使有內容的到期目錄永遠無法清除（Rev A.1）→允許安全清除到期候選內部內容。

## Acceptance Criteria

> **Rev A.2 AC 修訂閉環（RGR cycle 1，2026-10-21）**：①第 2 條 AC 由 `[可達性]` 改標 `[存在性]`、第 3 條補標 `[可達性]`（內容與預期不變，只改證據層級標示並寫明 stub 邊界）；②決策表 AC 增 X3 與「省略必填參數→exit 2」兩案；③read-back AC 的 `reviews_patch` 預期改為「缺漏欄位有 `reviews_observed` 採實際值、否則 `N_A` 並披露」。**兄弟 AC 重驗**：部分成功與歷史保護 AC、最終結果 AC 共用 `verify_readback()`／`finalize`，其預期隨同一計算結果一致（`N_A` 欄位改在最終結果披露，仍 ≤20 行、自包含）；治理字面 AC 之「AI-5 新字面逐字存在」自動涵蓋 R9c，「AI-8 ② 舊字面零命中」清單已補 R9c 舊句；實作 diff 範圍 AC 不變，其 `UNKNOWN` 結果改由 X3 承接；dependency、reviewer、定位、暫存清理 AC 不受影響。沒有 AC 被移除、縮減或豁免。

- [x] **[存在性]** `close_helper.py` 與 `test_close.py` 存在於 ALLOWED 所列精確路徑；兩檔僅 import 標準庫（`import` 行逐一核對）；`python -m py_compile` 兩檔皆通過
- [x] **[存在性]**（Rev A.2 由 `[可達性]` 改標：本條為子命令與 producer 位置之交叉核對；路徑實走見下一條）入口到 helper：`SKILL.md` 的 `close-dispatch` 區塊中每條命令的子命令皆為 helper `--help` 所列；各命令的輸入檔（`registry.json`、`dependents.json`、`verdicts.json`、`registry_after.json`、`summary.txt`）皆有 `Mode: close` 程序步驟中的 producer。**producer 證據欄**：`SKILL.md:<行號>`（registry.json／dependents.json＝程序步驟 1；verdicts.json＝步驟 4；registry_after.json＝步驟 7；summary.txt＝步驟 8）。已回填：`SKILL.md:281`（registry.json／dependents.json，步驟 1）、`:284`（verdicts.json，步驟 4）、`:295`（registry_after.json，步驟 7）、`:296`（summary.txt，步驟 8）
- [x] **[可達性][行為性]**（Rev A.2 補標可達性；邊界：Agent 角色由測試扮演、Registry 為假 adapter、reviewer 為替身；真實 Agent 路徑依裁決 K「首次真實使用不作永久關票前置」，不在本條宣稱範圍）given scratch 假 repo 中一張 REVIEW 票（純文字類，已有符合範圍之提交），when 測試依 `close-dispatch` 區塊實際逐條執行 helper 並扮演 Agent（假 Registry adapter）提供各 JSON，expect 依序得到 `preflight`→`reviewer`(primary=sonnet)→`decide`(`CLOSE`)→`readback`(ok)→`finalize`，票檔恰有一個最終結果區段，除該區段與 `.tmp/ticket-author/close/` 外無異動、無 commit、無其他檔寫入
- [x] **[行為性]** AC 完整性：缺項、重複序號、引用不存在 AC、空 `acs`、`acs_status=UNAVAILABLE` 皆不得得到 `CLOSE`（補證後補齊才可通過）；超過 400 字元並含續行的 AC 在 `preflight.json` 完整保存，僅 stdout 預覽截斷
- [x] **[行為性]** 決策順序與有界性：〈決策表〉每一列（P、C1–C3、B1–B4、V1–V3、X1–X3、U1–U3、L1–L2、K1–K3）各有一案；`decide` 省略 `--round` 或 `--fallback-tried`→exit 2（Rev A.2）；四類 hard blocker 先於 V／X／U／L；RECONCILE 與其他補證共用上限，達上限→`NOT_CLOSED`，不重複回傳同一動作；核心 UNVERIFIED 不報 PASS；非核心 UNVERIFIED、有效 N/A、可選外審未做不阻擋 `CLOSE`；`N/A` 無劃記降為 `UNVERIFIED`
- [x] **[行為性]** dependency：直接證據缺件→`BLOCKED`；`LEAD`→補證、達上限→`NOT_CLOSED(dependency_lead_unresolved)`；清單不可得→`NONE_FOUND` 降為 `UNKNOWN` 並披露，`LEAD`／`MISSING` 不被 `UNKNOWN` 蓋過；只查直接相依
- [x] **[行為性]** reviewer 分類與 fallback 與 SKILL 文字一致：preset 非 `docs` 但 ALLOWED 僅 `.md`/`.txt`/`.rst`→sonnet；ALLOWED 含任何其他副檔名（即使同時有 `.md`）→codex；codex 不可用→sonnet 並記 `fallback_used`／limitation；sonnet 不可用→codex；皆不可用→`actual=none`；`--escalate` 只回傳另一位 reviewer 與所列 AC 序號
- [x] **[行為性]** 實作 diff 範圍：區間內混入他票已提交修改不被誤報為本票越界；無前綴提交觸及 ALLOWED→scope `UNKNOWN`；本票自身越界→`FAIL`；僅觸及票檔之紀錄提交不納入；`DIFF_REF` 佔位時以 `BASE_COMMIT` 為下界，兩者皆無效→`UNKNOWN`；`git diff --check` 只對本票實作提交執行，失敗→`FAIL`；無可信範圍→`UNKNOWN` 而非 PASS
- [x] **[行為性]** read-back 與 finalize 同一判準：lifecycle 非 `CLOSED`、`file` 不可解析、`reviews` 與預期不符時 `readback` 回 mismatch，`finalize` exit 3、票檔零寫入、不出現「讀回一致」；`reviews_patch` 只處理缺漏欄位（有 `reviews_observed` 採實際值，否則 `N_A` 並於最終結果披露），已有值不改，不假填 PASS；`set_status` 送出值、`readback` 預期與 `finalize` 披露一致（Rev A.2）；讀回通過但寫入失敗時如實回報部分成功
- [x] **[行為性]** 部分成功與歷史保護：已 CLOSED 缺最終結果且 `decision.json` 綁定（票號、目標票檔、`binding_ref`）相符→只補寫最終結果；任一綁定項不符→不補寫並如實回報；歷史 CLOSED（無綁定決策）零修改、不重驗；已完成重入冪等、不重複追加；`decide` 不覆蓋先前 `next=CLOSE` 的決策檔；S5–S8、S10–S13、S16 各一案
- [x] **[行為性]** 定位與一致性：冊面 `file` 過期而實檔在 `deliver/archive/**` 被正確找到；`missing`、`ambiguous`、lifecycle 與位置矛盾分別被辨識
- [x] **[行為性]** 暫存邊界與清理：到期（>7 天）且含檔案與子目錄的候選票號目錄被實際清除；七天內保留；`--exclude` 不刪；非票號樣式目錄與檔案不碰；候選內部的符號連結／junction 只移除連結本身、目標內容不受影響（環境不支援時該案為 `skipTest` 並載明原因）；每次處理有上限；刪除失敗不使命令非零；所有產出僅在 `.tmp/ticket-author/close/`；沒有 commit、沒有 TKR／Registry 檔異動
- [x] **[行為性]** 最終結果：僅在讀回與綁定通過時寫入；單一區段、≤20 行、自包含（不以 `.tmp` 連結為唯一證據）；`NOT_CLOSED`／`BLOCKED` 情境原票零寫入（內容雜湊前後相同）；沒有永久中間 report 進入受版控路徑
- [x] **[存在性]** `SKILL.md` 含 `Mode: close`、`close-dispatch` 區塊、frontmatter 與 Accepted modes 的 `close`；`This skill only authors new Tickets.` 已被改寫；`stop-conditions.md` 的 `STOP_INVALID_MODE` 列與 `close` stops 表存在；`input-contract.md`／`state-transitions.md` 各有 `close` 節；新增行 `STOP_[A-Z_]+` 集合為既有碼子集，未新增 Gate／state／ledger／manifest／schema／hook／daemon
- [x] **[存在性]** `ssot-governance.md` 與 `sv/SKILL.md`：AI-5／AI-6 的新字面逐字存在（各 `grep -cF` 為 1），AI-8 ② 所列舊字面零命中；新增兩條（核心 AC 與 close 判定、checkpoint 處置）存在；Table 新增 close 列與 R4 改寫列相鄰一致；未保留括註式豁免；ADV 已提交的有效修改未被覆寫（`git diff` 僅涉及 AI-5 所列替換）
- [x] **[存在性]** 專案記憶（repo 外）：`governance-ticket-closure-sop-target-state.md` 與 `MEMORY.md` 索引行含新字面且舊字面（`lifecycle→REVIEW（非CLOSED）`、`CLOSED 是後續 Desktop 治理 session（如 SMTR）獨立收束覆核後才轉的終態`）零命中
- [x] **[存在性]** 收尾實錄如實載明：unittest 結果與 `skipTest` 原因、對真實 repo 的唯讀 dry-run 結果、AI-0 的漂移重定位記錄（若有）、未驗證項（真實 Sonnet／Codex 判讀品質、Agent 是否依 `Mode: close` 調度、使用者層級權限），且未把 stub PASS 當作模型行為驗證、未宣稱已證明真實 Agent 必定遵守 Skill、未把首次真實使用列為關票前置
- [ ] ~~`assembleDebug` PASS~~ (N/A: feature preset 之 `.claude/` 工具、測試與治理文字，無 `app/`／gradle 變更)
- [x] `git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉及 ALLOWED 檔案（FORBIDDEN 各檔 diff 為空）（證據：`c8385e64..eb5413af` 內本票 5 個前綴提交觸及之檔案聯集＝ALLOWED 8 檔，FORBIDDEN 各檔於本票提交零命中；區間另混入他票已提交修改〔RGR-DERIV／EQUIV／REPLAY／RERUN-01、GOVERNANCE-SYNC projman align 與一組 a7be7634／cf12e18a 互撤提交〕，依逐提交歸屬不計為本票越界，原始 `git diff --stat` 因此不是純 ALLOWED，如實揭露）

## 已知限制（非阻擋，如實揭露）

1. **模型端行為未驗**：Sonnet／Codex 的實際判讀品質、Agent 是否依 `Mode: close` 調度，皆無法以機械測試證明；測試以 stub 驗證 helper 的決策與副作用及 `close-dispatch` 命令的可執行性，**不代表**真實模型行為。此不作為本票或任何票的關票前置。
2. **權限屬使用者層級**：`~/.claude/settings.json`（`defaultMode=auto`）未列入全部 registry 寫入工具；被拒時 close 精確回報授權缺口，不繞過。本票不修改任何 settings。
3. **`check` 與 `deliver/`**：票在 `deliver/` 時 `gate_ledger.py check` 回 exit 4（既有限制）；close 不呼叫 `check`，因 closeout 的 `check` 已在票進 REVIEW 前通過。
4. **原票最終結果的持久化**：編輯 `deliver/` 票檔後屬 tracked dirty，沿用既有 §Session Persistence 雙軌，不新增專用提交設計；`deliver/archive/` 下票的治理檔提交路徑（Governance Artifact 表為單層 `deliver/*.md`）不在本票範圍。
5. **6a 外審**：`INTRODUCED_CAPABILITY` 觸發持久協議的機械取證；依 Gate 的研究授權於執行期辦理（一次窄範圍、不重送同題），結果記收尾實錄；其性質為取證而非 close 的 gate，不阻擋本票或 close。
6. **同檔協調已解除（Rev A.1）**：`ADV-ENTRY-01` 已達 `REVIEW`、其修改已提交於 `54f5624a`；AI-0 仍確認分支包含該提交並保留他票已提交修改，共享檔的未提交變更先辨識歸屬、不覆寫。
7. **清理時機**：七天清理只在 `close` 被呼叫時執行（`preflight` 內 best-effort），不保證在第七天精確刪除；暫存遺失時重新取證即可，且無法確認屬本次部分成功者不補寫歷史。
8. **既有開票偏差（Rev A.1 如實保留）**：本票開立時先寫 `Pending/` 草稿再跑 authoring gate，與 skill 步驟 18 順序不一致；登冊前冊面無痕跡、最終無 STOP，Owner 裁決不作廢重開、不回滾登冊、不另開治理票。
9. **證據依賴**：`registry_list` 的 `fields` 投影是否支援各欄位以平台為準；若某欄位不可取得，agent 以不含該欄位的查詢重試並於 `registry.json` 缺欄處如實留空，helper 對缺欄走 UNKNOWN／降級而非猜測。

## 收尾實錄（執行端；2026-10-21；Mode: normal；Rev A.2）

### 1. 基線與 AI-0

- 啟動：`/normal TKA-CLOSE-01 Rev A.2`（`activate_ticket.sh` ACTIVATED，owner 為本 session）；`DIFF_REF=c8385e64`（`main` HEAD，工作樹開工時 ssot／sv／ticket-author 檔皆已有本票先行編輯，歸屬本票）。
- AI-0 錨點復驗：全部錨點各恰 1 次，僅 `input-contract.md` blob 自開票後變動（`c8385e64` 單行，已在 `TARGET_BLOB`），無語義衝突；`54f5624a` 為 HEAD 祖先；`git check-ignore` 對 8 個 ALLOWED 路徑無命中；`python -c "import unittest, json, shutil, subprocess"` 成功。
- mdlint：專案無獨立 lint 腳本（`/mdlint` 為 skill 指引），改以自寫近似檢查（MD022／MD032／MD040）比對 HEAD 與工作樹六份 `.md`，**計數無增加**；此為近似檢查，非 `/mdlint` 本體輸出。

### 2. 實作（AI-1 ~ AI-7）

- AI-1 `scripts/close_helper.py`（標準庫；子命令 `preflight／reviewer／decide／readback／finalize／sweep`；`verify_binding()`／`verify_readback()` 共用函式）。
- AI-2 `tests/test_close.py`：65 案（初版 62＋Cross-Review 後增 registry 物件 id 綁定 3 案）；`python -m unittest discover -s .claude/skills/ticket-author/tests -p "test_*.py"` → `Ran 65 tests … OK`，**零 skip**（符號連結／junction 兩案在本機皆實際建立連結並通過；Windows 專屬開檔刪除失敗案亦實際執行）。
- AI-3／AI-4／AI-5／AI-6：以票內逐字目標值套用；AI-5 的 15 組現值→目標值、R10 列與兩條新增 bullet、AI-6 的 sv 替換與第 4 項，各目標字面於目標檔恰 1 次、現值零命中（腳本比對 `bad 0`）。
- AI-7：repo 外記憶 `governance-ticket-closure-sop-target-state.md`（description、第 3 點、Why 段）與 `MEMORY.md` 索引行已依逐字目標更新，舊字面（`非CLOSED`、`獨立收束覆核後才轉`）零命中；How to apply 段未動。

### 3. 實作細節與對票面的如實差異

- **實作提交認定（對 AI-1 §preflight「實作 diff 範圍」的澄清）**：票文字寫「`own` 提交中觸及 ALLOWED 者構成實作提交集合」。實作時發現若 `own` 提交**只**觸及 ALLOWED 以外的檔案，會因不觸及 ALLOWED 而被排除、越界無從偵測（測試 `test_own_commit_outside_allowed_fails` 首次即抓到）。故改為：`own` 提交中觸及**任何非豁免檔案**者為實作提交（豁免＝同名票檔〔含 Pending→deliver 搬移前後〕與 `.claude/PROGRESS*.md`）；僅觸及票檔之紀錄提交仍不納入。語義比票面更嚴，未放寬任何判定；非語義變更。
- `preflight` stdout 摘要另含 `base` 欄（便於走查）；不影響契約欄位。
- 內部預設：`reviewer`／`decide` 的 sonnet 可用性預設為可用、codex 預設以 `shutil.which("codex")` 判定，與票文字一致。

### 4. 唯讀 dry-run 與回歸（AI-8 ①④⑥）

- `preflight --ticket TKA-ALIGN-01`（冊面 `file=deliver/TKA-ALIGN-01_retro.md` 過期、實檔在 `deliver/archive/TKA/`）：`locate_status=found`、`file_field_stale=true`、`location=deliver/archive`、`consistency=PASS`、`ac_count=13`、`scope=PASS`、`diff_check=PASS`。前後 `git status --short`、`tmp/verify/active.json` md5、`.git/gate/` 目錄清單雜湊、HEAD 皆相同。dry-run 只寫 `.tmp/ticket-author/close/TKA-ALIGN-01/`（gitignore）。
- `bash .claude/skills/qa/tests/test_report_fix_closure.sh` → `ALL PASS`（exit 0）；`test_gate_ledger.py` → 53 tests OK。
- 殘留 grep：AI-8 ② 九個舊字面於 `ssot-governance.md`、`sv/SKILL.md`、`ticket-author/` `.md` 皆零命中（含 R9c `並於 checkpoint 揭露豁免依據`）；新增行 `STOP_[A-Z_]+` 集合（`ACCESS_BLOCKED／DECISION_REQUIRED／EVIDENCE_CONFLICT／INVALID_MODE／MISSING_SUBJECT／SOURCE_UNAVAILABLE／WRITE_FAILED`）皆為 HEAD 既有碼；`git diff --check` 乾淨；工作樹變更僅 ALLOWED 內六個既有檔＋兩個新檔。

### 5. 未驗證項與限制（如實揭露，皆非本票或其他票之關票前置）

- 真實 Sonnet／Codex 判讀品質未驗；測試中 reviewer 以替身（旗標／PATH stub）表示，**stub 通過≠真實模型行為**。
- 真實 Agent 是否依 `Mode: close` 調度未驗；測試扮演 Agent 並實際執行 `close-dispatch` 區塊命令，只證明命令可執行、各步 `next` 與副作用正確，不證明 Agent 必定遵守 Skill。
- 使用者層級權限（`defaultMode=auto` 未列入全部 registry 寫入工具）：被拒時 close 精確回報缺口，未繞過；本票未改任何 settings。
- 6a 實作級外審（`INTRODUCED_CAPABILITY` 機械觸發）：本次**未執行**付費研究呼叫；依已知限制 5 其性質為取證而非 close 的 gate，不阻擋本票。
- 票檔 Pending 草稿先於 authoring gate 之偏差（已知限制 8）依 Owner 裁決保留。
- `sweep` 只在命令被呼叫時執行；Windows 以外平台的連結／開檔刪除案未於本機驗證。

### 6. QA／SV／Cross-Review（2026-10-21；範圍 `c8385e64..eb5413af`）

- `/qa`：`check qa`／`check commit` exit 0；Step 0 PASS（提交前工作樹差異為空）；Build／Lint N/A（無 app 變更）；Dashboard 同步延至下一輪 `/compact`。提交：`25af6b6e`（實作）、`2ded9afa`（X3 fallback 測試不依賴本機 codex）、`6c4972e5`（registry 物件 id 綁定）。
- `/sv`：`check sv` exit 0；structural 預檢 PASS。Codex（gpt-5.6-sol）**連續 3 次 REPORT_FIX**，皆為不同細節缺陷（R1 報告未揭露 X3 達上限不另立終局之設計行為；R2 報告誤述 `readback` 以 `ok` 而非 exit code 表達，並附 ADVISORY：registry 物件 `id` 未綁 CLI 票號→已採納為程式修正＋3 案測試；R3 報告把 `decision.json` 誤列為 `decide` 輸入，實為輸出→已更正）。第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2）；Codex 另提 ADVISORY（`file` 指向他票既存檔時未驗票檔歸屬）**未處理**。
- Gate：INC-001（executor 1 輪 fail、`step_limit` 已記）；Advisor（API，`req_011CgEz2WKuo9M7SAGiAaudS`，另一次 `TRUNCATED` 失敗未記帳）結論＝無已授權自動續行策略 → 曾升報 `HUMAN_REQUIRED: NO_AUTHORIZED_STRATEGY`；Owner 其後裁決（不再送同一 Codex 迴圈、不重設次數、LIMIT 不等於 blocker 亦不是 PASS，定點處置 X3）後該項已於 ledger resolve。**未取得 Cross-Review 終局 PASS／ADVISORY；本票 SV 如實記為「Codex 3×REPORT_FIX＋LIMIT，未重審」，不改寫為 PASS／ADVISORY。**

### 7. Owner 裁決後的 X3 定點處置（2026-10-21）

- **X3 具體主張與位置**：Codex R1 稱「`SKILL.md:284` 要求 scope 仍 `UNKNOWN` 且補證達上限時 `decide` 回 `NOT_CLOSED`，但 `close_helper.py` 達上限後落入 K 區回 `CLOSE`」。對照：票面〈close 決策表〉X3 列與其後說明（`TKA-CLOSE-01_autonomous.md:524,534`）明文「達上限後不另立終局、不新增 hard blocker；涉及範圍之核心 AC 維持 `UNVERIFIED` 走 U 列」；程式 `close_helper.py` `cmd_decide` 的 `scope_unknown` 分支僅在 `rnd < MAX_ROUNDS` 觸發；測試 `test_x3_scope_unknown_then_cap` 三側斷言（round 0＝`SUPPLEMENT(scope_unknown)`；round 2＋全 PASS＝`CLOSE`；round 2＋核心 `UNVERIFIED`＝`FALLBACK_REVIEWER`）。
- **判定＝報告／文字措辭問題，非核心缺陷或安全問題**：helper 從不把 `UNKNOWN` scope 判為 PASS，也不把 scope 狀態映射到任一 AC；「不得以 UNKNOWN 範圍判 PASS」由 `verdicts.json` 作者（Agent）承擔，屬票面明文設計與模型契約。唯一確實的不一致是 `SKILL.md` 步驟 6 一句「達上限 `decide` returns `NOT_CLOSED`」過寬 → 已改為「達上限不再要求補證：回 `NOT_CLOSED`，但 scope 仍 `UNKNOWN` 不自成終局，由 AC 判定決定，涉及範圍之 AC 維持 `UNVERIFIED`」（`316faa12`）。`sv_report` 另已補揭露此設計與其限定。
- **另一 reviewer 一次定點判讀**（Sonnet，唯讀，僅讀票面決策表、`cmd_decide`、`SKILL.md` 步驟 5–6 與 X3 測試）：`MATCHES_SPEC`，四件產物無矛盾；並指出一處輕微缺口——`diff_check` 仍為 `UNKNOWN` 時最終結果未披露 → 已採納修正（`eb5413af`，最終結果披露行新增 `git diff --check 為 UNKNOWN`，並加測試）。reviewer 為模型判讀，**不構成 PASS，亦不取代 Codex 終局**。
- Codex 另提 ADVISORY 皆已採納並加測試：registry 物件 `id` 綁 CLI 票號（`6c4972e5`）、票檔 `TICKET_ID` 綁 CLI 票號（`316faa12`）。測試：`Ran 67 tests … OK`（零 skip）。
- **核心 AC／hard blocker 判定**：本票無 FAIL 之核心 AC、無安全問題、無不可逆風險、`TKA-BATCH-01`／`TKA-EVGATE-01` 等相依票未被修改；`[實機]`／真實 Agent 調度類項目屬已知限制 1，非核心缺件。故不成立 hard blocker，依 Owner 裁決接續 closeout 至 `REVIEW` 並自主 close。
- 其餘如實限制：Codex 終局 verdict 缺席（冊面 `reviews.codex` 以 `N_A` 填寫並於本節揭露實際為 `REPORT_FIX×3`＋LIMIT，不填 `PASS`）；Dashboard 同步與 `PROGRESS_ARCHIVE` Done 標記延至下一輪 `/compact`。

<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: ticket-author 新增 `close` 子命令（`close_helper.py` 6 子命令＋68 項隔離 unittest），Agent 可對 REVIEW 票自主取證、判定並 CLOSED，中間產物僅存 .tmp 七天。 / ssot-governance.md 以 R1–R10 直接替換阻擋自主閉環的舊條款（CLOSED 改由 close 寫入、Traycer T2 降為可選外審）；sv/SKILL.md Step 11 接續 close。 / Codex Cross-Review 連續 3×REPORT_FIX 觸 LIMIT 無終局（reviews.codex=N_A，非 PASS）；X3 經 Owner 裁決定點處置，真實 Agent／模型判讀未驗。
- 最終驗收結論: 核心 AC 15 條皆為 PASS 或有效 N/A；非核心 PASS 3／N/A 1／UNVERIFIED 0
- 實作提交: 25af6b6e, 2ded9afa, 6c4972e5, 316faa12, eb5413af, 95995373
- 實際收束結果: 2026-10-21 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: sonnet（fallback）
- 披露: reviewer limitation: Codex not re-run (3x REPORT_FIX + LIMIT; Owner ruling: no further Codex loop). AC verdicts are the Agent's from mechanic
<!-- close-result:end -->
