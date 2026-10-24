# ADV-COMPACT-01: compact 不再提交任何票務 artifact（提交集合收斂為 PROGRESS／PROGRESS_ARCHIVE），SSOT 之票務保存責任改為 TKA 單一 owner

**Revision**: Rev A.1（2026-10-24，Akira 裁決「接受本批與 ADV 命名」後原地補清：compact 自身產物——Handoff、歸檔、PROGRESS／ARCHIVE——之保存契約，新增 AI-6 與對應 AC；原 AI-1～AI-5 不變）。

**簽發日期**: 2026-10-24
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: ADV-SAVE-01 —— 須先由 TKA close 確定性保存票檔，再移除 compact 兜底，避免保存真空
- `evidence_dependency`: ACAGOV-RUNTIME-TABLE-01 —— §Session Persistence 雙軌與 Governance Artifact `deliver/*.md` 列之原立法；INFRA-CTAWR-CKPT-TRIGGER-01 —— checkpoint 兩段式提交之原立法目的（QASV 清場）
- `coordination_dependency`: 無

## 來源（Parent）
- Parent type：Plan-backed；路徑 `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261024-autonomy-closeloop-plan.md`（Revision R2；Fixture false／Plan Gate PASS／Ticket mode eligibility ALLOWED）；plan-local item `T02`；讀取時間 2026-10-24T05:30Z。
- Selected item：compact 責任收斂與 Session Persistence 單一 owner。
- 承重決策（使用者 2026-10-24 分工裁決，逐字要旨）：票務最終收束及票務文件保存歸 TKA；compact **僅**負責 PROGRESS、PROGRESS_ARCHIVE、Handoff 與歸檔，以及自身產物保存；**不採 compact 作為票務提交的永久兜底**；成果表仍由 projman 負責。plan D2：移除 checkpoint 第一段票務提交；Governance Artifact 清單收斂為自身產物；Session Persistence 改單一 owner。
- 自身產物之現況證據（2026-10-24 live）：①Handoff（`docs/session_handoff/SESSION_HANDOFF_<timestamp>.md`）與其 `Archive/` 皆被 `.gitignore:73` 排除（`git check-ignore -v` 實測命中）——為**本機檔、不進 git**，其「保存」只能是本機寫入並讀回確認，不得 `git add -f`；②歷史 handoff 歸檔（compact §7：根目錄僅留最新 2 份、其餘移 `Archive/`、不覆寫同名）同屬本機操作；③`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 為 tracked，以 `[COMPACT-SYNC]` 提交（compact checkpoint 第二段與 closeout Auto Commit）；④compact 現行規格對上述產物「寫入後是否讀回確認、提交後是否仍乾淨」無明文，確認訊息（`交接文件已儲存至…`）可在未驗證時輸出。本票補清此缺口（AI-6），不改產物位置或內容結構。
- 揭露之直接後果：①`.claude/rules/*.md` 自清單移除後，**非票**改動之規則檔不再由 compact closeout 提交（依「僅」字）；②REVIEW 狀態票檔在 TKA close 成功前保持未提交（ADV-SAVE-01 D1）；③實作提交之清場責任回歸既有 QA Step 4.5／低風險列實作提交。

- 票號歸屬：本票由「工程師自主閉環」議題（源自 ADV-CLAUDECLI-01 收束與 Advisor 成果表 P1 缺口）發起，依 Akira 2026-10-24 裁決，同一議題 session 發起之票一律掛該議題前綴 `ADV`，不依修改落點另分前綴；原開票 ID 已以 `registry_rename_ticket` 改名。

## Verify Config
TICKET_ID=ADV-COMPACT-01
CREATED=2026-10-24
PRESET=docs
ALLOWED=.claude/skills/compact/SKILL.md,.claude/rules/ssot-governance.md
FORBIDDEN=.claude/skills/ticket-author/**,.claude/skills/sv/**,.claude/skills/qa/**,.claude/skills/projman/**,.claude/scripts/**,.claude/hooks/**,.claude/settings.json,.claude/settings.local.json,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,.claude/rules/cross-review.md,.claude/rules/concurrency.md,.claude/rules/ticket_pattern.md,CLAUDE.md,app/**,plans/roadmap/**,plans/cso_briefing/project/TICKET_REGISTRY.json,.gitignore
MAX_LINES=100
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-24 14:00
BASE_COMMIT=2d8e7e34
TARGET_BLOB=N/A（docs preset；AI-0 逐檔讀現值錨點替代）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/compact/SKILL.md`、`.claude/rules/ssot-governance.md`
- **動作**: 逐字確認：①compact checkpoint「**第一段——當前票 artifacts**」區塊（含 `補入該票票檔：`plans/cso_briefing/project/{Pending,deliver,void}/<TICKET_ID>_*.md``）唯一；②compact「## 冊面收束一致性閘」節之級 A（票檔進 `deliver/`）與級 B（`PROGRESS.md` diff 標 CLOSED）字面；③compact §9.5 第 3 點「票 closeout 前 **SHOULD** 跑 `closeout` 模式」；④ssot §Governance Artifact 清單五列（PROGRESS、ARCHIVE、`TICKET_REGISTRY.json`、`.claude/rules/*.md`、`deliver/*.md`）；⑤ssot §Session Persistence 第 2–6 點；⑥ssot Transition Table projman 列含「採軌道 A 者先完成當次 `[<TICKET_ID>]` 單票收尾提交，採軌道 B 者不為此提前 `/compact`」；⑦ssot §Ticket Closure「原子性」含「雙軌擇一」。並確認 ADV-SAVE-01 已交付（`close_helper.py` 有 `persist`）。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置；ADV-SAVE-01 未交付 → 停止。**
- [x] 完成（2026-10-24：錨點全命中——checkpoint 第一段＝compact:94、一致性閘級 A／B＝:337–341、§9.5 第 3 點＝:299、ssot 表五列＝:11–19、Session Persistence 第 2–6 點＝:256–263、projman 列＝:159、原子性＝:287；ADV-SAVE-01 已 CLOSED 且 `persist` 已交付〔`ef289991`／`e06e15aa`〕）

### AI-1: 移除 compact checkpoint 第一段票務提交
- **檔案**: `.claude/skills/compact/SKILL.md`
- **現值**: checkpoint 規則為「當前票 artifacts ＋ PROGRESS」兩段式 scoped commit，第一段依 `tmp/verify_cmd.conf` 提交 `ALLOWED` dirty 檔與票檔。
- **目標值**: checkpoint 只提交第二段（PROGRESS／PROGRESS_ARCHIVE）；第一段整段刪除，並以一句註明「票務 artifact 之保存歸 TKA close（`close_helper.py persist`），實作提交歸 QA Step 4.5／低風險列；compact 不提交、不兜底」。原 QASV 清場理由改由該句之 owner 承接。
- [x] 完成（compact/SKILL.md §4.2：刪除第一段票務提交，改述『提交集合僅限 PROGRESS／ARCHIVE、不兜底』並回指 TKA persist）

### AI-2: 冊面收束一致性閘收斂與時機索引
- **檔案**: `.claude/skills/compact/SKILL.md`
- **現值**: 一致性閘級 A 以票檔進 `deliver/` 觸發並可停止 Auto Commit；§9.5 第 3 點要求票 closeout 前跑 `closeout`。
- **目標值**: 刪除級 A（票檔位置屬 TKA close 之 preflight／readback 驗證範圍）；保留級 B（`PROGRESS.md` 宣稱 CLOSED 須與冊面一致，屬 compact 自身產物正確性）與既有停止語義；§9.5 第 3 點改為「票之 closeout 與票檔保存由 `/sv` Step 11 與 TKA close 承擔，compact 無需配合跑 `closeout`」。
- [x] 完成（compact/SKILL.md：一致性閘刪級 A 與 deliver 觸發、保留級 B；§9.5 第 3 點改述；§8.1 例外改為 PROGRESS／ARCHIVE pathspec commit）

### AI-3: Governance Artifact 清單收斂
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 表五列。
- **目標值**: 表僅留 `.claude/PROGRESS.md`、`.claude/PROGRESS_ARCHIVE.md`（`[COMPACT-SYNC]`）；刪除 `TICKET_REGISTRY.json`（已於 `82840aad` 退役 git 追蹤，`.gitignore:81`）、`.claude/rules/*.md`、`plans/cso_briefing/project/deliver/*.md` 三列；表下補一句：票檔保存歸 TKA close，規則檔與其他非 compact 產物由產生它的票或作業自行提交。
- [x] 完成（ssot-governance.md：§Governance Artifact 清單刪三列＋owner 說明句）

### AI-4: Session Persistence 單一 owner
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 第 2 點 artifact set 含 `TICKET_REGISTRY.json` 且「MUST NOT 排除」；第 3 點未走軌道 A 須 `/compact closeout`；第 4 點「任一軌道」；第 5 點雙軌擇一；第 6 點補列 `deliver/*.md`。
- **目標值**: 第 2 點 artifact set 改為「已收束票之 `deliver/` 票檔（TKA 保存）與 PROGRESS／ARCHIVE（compact 保存）」，刪除 registry 檔納入條（註明其寫入通道為平台 API、檔案已不受 git 追蹤）；第 3 點改為 REVIEW 票檔於 TKA close 成功前未提交屬已知狀態，不以 compact 兜底；第 4 點改為 TKA `persist` 之 pathspec 單檔驗證；第 5 點改為單一 owner（TKA close 於 read-back 確認 CLOSED 後以 `[<TICKET_ID>]` 單檔提交），刪除軌道 B 對票務 artifact 之適用，保留「不得混入無法歸屬之 dirty change」禁令；第 6 點改為記錄 `deliver/*.md` 列已因本票移除及理由。
- [x] 完成（ssot-governance.md §Session Persistence 第 2–6 點改單一 owner、揭露三項後果；刪軌道 A／B 適用）

### AI-5: projman 列與 Ticket Closure 原子性字面
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: projman 列「採軌道 A 者先完成當次 `[<TICKET_ID>]` 單票收尾提交，採軌道 B 者不為此提前 `/compact`」；原子性「雙軌擇一執行（隨票即時……或延至 `/compact closeout` 走 `[GOVERNANCE-SYNC]`）」。
- **目標值**: projman 列改為「`[PROJMAN-SYNC]` 以 pathspec 單獨提交，不等待票檔提交（票檔於 CLOSED 後由 TKA 保存），二者互不夾帶」；原子性改為「冊面異動之票檔 persistence 由 TKA close `persist` 執行」。不改兩處其他語義。
- [x] 完成（ssot-governance.md：projman 列改 `[PROJMAN-SYNC]` pathspec 單獨提交；Ticket Closure 原子性改由 TKA persist）

### AI-6: compact 自身產物保存契約（Handoff、歸檔、PROGRESS／ARCHIVE）
- **檔案**: `.claude/skills/compact/SKILL.md`
- **現值**: handoff 產出（§4.2、§6）、housekeeping 歸檔（§7）、PROGRESS／ARCHIVE 提交（checkpoint 第二段、closeout Auto Commit）各有規格，但無「寫後驗證」與「確認訊息以驗證結果為準」之明文；確認訊息範本（`交接文件已儲存至 docs/session_handoff/…，PROGRESS.md 已同步更新。Housekeeping 已完成…`）無條件。
- **目標值**: 新增「自身產物保存契約」一節（置於 §7 之後或確認訊息節前）：
  1. **Handoff**：寫入後 **MUST** 讀回確認檔案存在且非空；該目錄為被 ignore 之本機載體，**MUST NOT** `git add -f`／提交；寫入或讀回失敗 → 回報 `⛔ Handoff 未保存`，**MUST NOT** 輸出「交接文件已儲存」。
  2. **歸檔（housekeeping）**：每次移動後確認來源已不在根目錄、目的地存在；同名衝突跳過並載明（既有規則）；任一移動失敗 → 回報未完成之檔案，不宣稱 housekeeping 已完成。
  3. **PROGRESS／ARCHIVE**：`[COMPACT-SYNC]` 提交後 **MUST** 以 `git status --short -- <檔>` 確認該檔已無差分；仍有差分或提交失敗 → 回報 `⛔ Auto Commit FAILED`／保留，**MUST NOT** 宣稱「PROGRESS.md 已同步」。
  4. 確認訊息 **MUST** 逐項依上列驗證結果輸出（已保存／未保存＋原因），不得以範本字面無條件輸出。
- [x] 完成（compact/SKILL.md 新增〈自身產物保存契約〉；確認訊息改條件式）

## Hard Constraints
1. compact 之 PROGRESS／ARCHIVE／Handoff／歸檔既有行為不變（含第二段提交、Housekeeping、handoff 產出與內容結構）；AI-6 僅新增寫後驗證與回報誠實性，不改位置、命名或 ignore 狀態。
2. 不改 QA Step 4.5、projman、TKA close、Transition Table 其他列之語義；不新增 lifecycle／verdict／receipt 枚舉。
3. 不刪除既有一致性閘級 B 之停止語義。
4. 每處修改附一句理由回指使用者 2026-10-24 分工裁決或本票；不留「軌道 B 保存票務 artifact」殘句。

## Error Handling
- ADV-SAVE-01 未交付或 `persist` 行為與 AI-4 第 5 點敘述不符 → 停止回報（不可先移除兜底）。
- ssot 或 compact 內另有本票未列之票務提交點（grep `git commit`／`軌道`／`closeout` 命中 AI-0 以外處）→ 停止回報，不自行擴充。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** compact/SKILL.md 之提交點（`grep -n "git commit"`）只剩 PROGRESS／ARCHIVE 相關（checkpoint 第二段與 closeout Auto Commit 讀表），無 `[<TICKET_ID>]` 提交；一致性閘無級 A；§9.5 第 3 點已改。 （實測：`grep -n "git commit" compact/SKILL.md` 僅剩 PROGRESS／ARCHIVE 相關〔:94 checkpoint、:98 禁令、:251 禁令、:354–357 Auto Commit 讀表〕；`grep -c "\[<TICKET_ID>\] <"`＝0；一致性閘 `grep -c "級 A"`＝0；§9.5 第 3 點已改）
- [x] **[存在性]** ssot §Governance Artifact 清單恰兩列（PROGRESS、ARCHIVE）且附 owner 說明句；§Session Persistence 第 2–6 點、projman 列、原子性無「軌道 B」適用於票務 artifact 之字面（`grep -n "軌道 B\|雙軌"` 僅剩已改寫之歷史說明或零命中，逐筆附判讀）。 （實測：ssot 表列僅 PROGRESS／ARCHIVE 二列＋owner 說明句；`grep -n "軌道 [AB]\|雙軌"` 僅剩 :255、:258 兩處改寫說明字面〔『取代…雙軌條款』『不再有軌道 A／B 之選擇』〕）
- [x] **[存在性]** 三項直接後果（rules 檔不再由 compact 提交、REVIEW 票檔未提交狀態、實作提交 owner）於 ssot 對應位置有明文揭露。 （ssot：§Governance Artifact owner 說明句＝①規則檔由產生它之作業自行提交；Session Persistence 第 3 點＝②REVIEW 票檔未提交屬已知狀態；第 2 點＝③實作提交 owner 為 QA Step 4.5／低風險列）
- [x] **[存在性]** SSOT 與 ADV-SAVE-01 交付之 `close_helper.py persist`／`ticket-author/SKILL.md` close 步驟敘述一致（觸發僅 CLOSED、pathspec 單檔、`[<TICKET_ID>]` 前綴）——逐條對照表附於收尾實錄。 （對照：觸發僅 CLOSED—ssot 第 2 點／ticket-author SKILL 8b『only for a Ticket that is CLOSED』；pathspec 單檔—SKILL 8b『single deliver/ file with a pathspec』；前綴—SKILL 8b『one [<TICKET_ID>] commit』，與 helper `cmd_persist` commit message `[%s] docs(ticket): persist closeout record` 一致）
- [x] **[存在性]** compact/SKILL.md 含「自身產物保存契約」，涵蓋 Handoff（讀回確認、被 ignore 之本機載體、不 `git add -f`）、歸檔（移動後確認、同名跳過載明）、PROGRESS／ARCHIVE（提交後無差分確認）與確認訊息依驗證結果輸出；`grep -n "git add -f"` 僅出現於禁止語境。 （compact/SKILL.md:363–371〈自身產物保存契約〉含 Handoff 讀回確認／被 ignore 本機載體／不 `git add -f`〔僅出現於禁止語境〕、歸檔移動後確認／同名跳過、PROGRESS／ARCHIVE 提交後無差分確認）
- [x] **[存在性]** 確認訊息範本不再無條件宣稱「已儲存」「已同步」「Housekeeping 已完成」，改為以驗證結果為條件之字面。 （確認訊息範本改為『全部通過時之範本』，任一項未通過僅輸出已通過項並列未通過原因）
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)（票面劃記 N/A，無 app/ 變更，未執行 Gradle）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（實測 2 檔：compact/SKILL.md、ssot-governance.md；`git diff --check` 無輸出）

## 收尾實錄（Agent closeout，2026-10-24；交 TKA close 判定，非自行終審）

### AC4 逐條對照表（TKA close 第 1 輪 reviewer 判 AC4 FAIL〔缺對照表與 helper 實作佐證〕後補件，2026-10-24；僅補證據、未改實作）
| 契約項 | SSOT／compact 敘述（本票 `b85b8617`） | ticket-author SKILL close 步驟 8b（`.claude/skills/ticket-author/SKILL.md:299`） | helper 實作（`.claude/skills/ticket-author/scripts/close_helper.py`） | 一致？ |
| --- | --- | --- | --- | --- |
| 觸發條件：僅 read-back 確認 CLOSED | ssot-governance.md 〈Session Persistence〉第 2 點：「已收束票之 `deliver/` 票檔由 **TKA close**（`close_helper.py persist`，於 read-back 確認 CLOSED 後……）保存」；第 5 點：owner 唯一為 TKA close | 「Persist (only for a Ticket that is `CLOSED` in the read-back, after `finalize` when it ran)」「Nothing is persisted for `NOT_CLOSED`, `PRECONDITION_FAILED`, `BLOCKED` or any `STOP_*`」 | full 驗證走 `verify_readback`（`:735` `if after.get("lifecycle") != "CLOSED":` 記不符）；recover 驗證 `verify_recover`（`:1205` 同判斷）；`cmd_persist`（`:1227`）驗證不過即 `persist_out(..., False)`、零 git 寫入 | 一致 |
| 單檔 pathspec | ssot 第 2 點：「`[<TICKET_ID>]` 單票單檔 commit」；第 4 點：「以 pathspec 限定單檔」 | 「one `[<TICKET_ID>]` commit of that Ticket's single `deliver/` file with a pathspec (…no `-A`, no `--allow-empty`, no other path)」 | `:1245` `check-ignore -q -- <rel>`、`:1261` `git add -- <rel>`（僅未追蹤時）、`:1265` `git commit -o -m <msg> -- <rel>`（`rel` 為 `locate_ticket` 解出之該票單一路徑） | 一致 |
| 提交前綴 | ssot 第 2 點：「`[<TICKET_ID>]`」；compact §4.2：「票檔保存……以 `[<TICKET_ID>]` 單檔提交」 | 「one `[<TICKET_ID>]` commit」 | `:1264` `msg = "[%s] docs(ticket): persist closeout record (TKA close persist)" % tid`；真實使用實證：`e06e15aa`〔ADV-SAVE-01〕、`e15c06b8`〔ADV-CKPTDOC-01〕提交主旨皆以 `[<TICKET_ID>]` 開頭、僅含該票票檔 | 一致 |
| 失敗不冒稱成功 | ssot 第 3 點：「`persist` 失敗（`saved: false`）須如實回報……同票重入」 | 「when it is false never write saved or committed」「report the close as partial success」 | `:1195` `obj["saved"] = bool(saved)`；所有 `saved:false` 路徑 exit 3（`persist_out`） | 一致 |
| 不兜底（compact 不提交票務檔） | compact §4.2 與 ssot 清單僅留 PROGRESS／ARCHIVE | — | — | 一致（ssot／compact 內部） |

- **路徑**：低風險免 QA／SV（ALLOWED 僅 `.md` 之 `compact/SKILL.md`、`ssot-governance.md`；無 `app/`／腳本／hook 變更、無不可逆操作、可 `git revert`）；已先 `check commit`（exit 0）再以 `[ADV-COMPACT-01]` 提交。
- **實作提交**：`b85b8617`（2 檔，+28／−33）。
- **驗證**：見各 AC 回填之 grep 實測；`git diff --check` 無輸出；SSOT 與 `ticket-author` SKILL close 步驟 8b 之 persist 契約對照一致（觸發僅 CLOSED、pathspec 單檔、`[<TICKET_ID>]` 前綴）。
- **三項直接後果（已於 ssot 明載）**：①非票之 `.claude/rules/*.md` 修改不再由 compact closeout 提交（由產生它之作業自行提交；先例：模板 v3.12 `306b5276`）；②REVIEW 票檔於 TKA close 成功前保持未提交；③實作提交之清場責任回歸 QA Step 4.5／低風險列。
- **未執行**：`/qa`、`/sv`、Cross-Review、Gradle（依風險分級省略／票面 N/A）；`reviews` 四欄皆 `N_A`。
- **限制**：compact 之新契約為模型契約，無機械後盾；一致性閘仍保留級 B（PROGRESS 宣稱 CLOSED 須與冊面一致）。PROGRESS.md Dashboard 同步屬 FORBIDDEN 延後項，待下一輪 `/compact`（該輪即首次依新契約執行之觀察機會，非盲測）。


<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: 交付：compact 提交集合收斂為 PROGRESS／ARCHIVE（移除票務提交段與一致性閘級 A）、ssot 清單刪三列並改 Session Persistence 為 TKA 單一 owner、projman 列／原子性字面改寫、新增〈自身產物保存契約〉（提交 b85b8617，2 檔）。 / 驗證：TKA close reviewer gpt-6-sol 第 2 輪 8 條 AC：PASS×7／N/A×1；首輪（前一次 close 呼叫）判 AC4 FAIL→BLOCKED（缺逐條對照表與 helper 原始碼佐證），補件後重審通過。 / 限制：低風險 docs 票省略 /qa、/sv、Cross-Review（reviews 皆 N_A）；新契約為模型契約無機械後盾；PROGRESS 同步待 /compact（該輪為首次依新契約執行之觀察）。
- 最終驗收結論: 核心 AC 6 條皆為 PASS 或有效 N/A；非核心 PASS 1／N/A 1／UNVERIFIED 0／FAIL 0（共 8 條 AC）
- 實作提交: b85b86177fb5425cbea678e2d6ea14de5b5ea64c
- 實際收束結果: 2026-10-24 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: codex/gpt-6-sol（tier=heavy）；覆蓋 AC 8／8（全部），independent_call=True
- 披露: reviewer limitation: reviewer 無法讀檔，證據逐字嵌入 prompt；回覆檔取自 codex exec 最終訊息段（原始輸出 reviewer_reply_raw.txt）；QA／SV／Cross-Review 依風險分級省略；首輪（同票前一次 clos
<!-- close-result:end -->
