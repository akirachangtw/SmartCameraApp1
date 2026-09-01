# INFRA-GOV-DEESC-01 (Rev A): 裁決噪音自動化消解（family provenance 自動補登 + DIFF_REF 回填標準化）

檔案治理／來源說明：本票為 doc-only 治理規則修補，目標是把兩類「每次必然觸發、且結論可預先機械化」的 STOP-escalate 降級為自動化路徑，同時**保留**真正需要人類判斷的 STOP 分支。不改任何腳本代碼、regex 或判定引擎。

**Rev A 修訂註記（2026-07-25，執行者停止回報 → CSO 裁決）**：執行者依 Error Handling 條款 1 停止回報——原票四條款之「現值」描述之規則本文，實際居於本票 FORBIDDEN 之 `.claude/CLAUDE.md`（Rule 4a）或不存在於任何 ALLOWED 檔，構成整票 scope 假設（ALLOWED＝兩份規則落點）與規則本文實際居住地之系統性錯位，非單一條款孤立問題。CSO 裁決（B/A/A/A/A）：① 擴 ALLOWED 納入 `.claude/CLAUDE.md`，family provenance 規則本文以 CLAUDE.md Rule 4a 為 SSOT、兩檔同步改，避免 CLAUDE.md（每 session 必載高權文件）與 ticket_pattern.md 字面互斥（違鐵律 11、殭屍條款持續復發）；② 六欄 provenance 表以 ticket_pattern.md **新增獨立區塊**承載（既有兩表皆 3 欄、無相容錨點，禁動既有表結構）；③ DIFF_REF 模板規則**新建**於 ticket_pattern.md「票據模板規範」章節（誠實標示為新建 SSOT 落點，非改既有段落）；④ DIFF_REF 回填三動作寫入 qa/SKILL.md **Step 0（Scope Gate）**，不強求「Boot Sequence」字面（該章節僅存於 FORBIDDEN 之 CLAUDE.md；回填時機＝票務開工，語意歸屬 Scope Gate）；⑤ Anchor Drift STOP 誠實框定為**首次成文**（grep 全 repo 現無任何機械定義，僅 PROGRESS.md 歷史敘述），非「收窄既有」，並註明尚無 hook 機械強制點、屬 /sv//qa 執行者行為準則。

**簽發日期**: 2026-07-23（Rev A: 2026-07-25）
**簽發者**: Claude (CSO)（Akira 2026-07-23 指示：工作流自動化與智慧化，釋放 CSO 時間予 MVP 開發）
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: execute_review
**前置依賴**: 無。⚠️ **下游依賴**：`MEO-FIX-A` Rev D/E 以本票為前置（AI-1c 之 `MEO-` family 一次性補登），本票 MUST 先行完成。

## 背景（兩類實戰噪音事件）

**噪音一（family provenance）**：MEO-FIX-A AI-0 觸發 CLAUDE.md **Rule 4a**——`MEO-` family 結構合法（regex PASS）但未登記 provenance 表 → STOP + escalate CSO。然而該票**本身就是 CSO 簽發**、且 CSO 已直接指示執行；escalation 的答案在觸發前即已確定。凡「CSO 簽發之票啟用新 family」皆必然重演此空轉。規則原意是防 **agent 自行發明 Ticket ID**，不是防 CSO 開新系列。⚠️ 規則本文居 CLAUDE.md Rule 4a（原票誤將修改標的指向 ticket_pattern.md，Rev A 已更正落點）。

**噪音二（DIFF_REF 漂移）**：原子化提交紀律使 HEAD 高頻前進；任何在票面**起草時硬編**的 `DIFF_REF` 在 Pending 停留期間必然失效，觸發 Anchor Drift STOP（實例：PA-BOUND-01 `2c883cf` → `d17c62a`，取證三呼叫 + 一次 CSO 裁決 + Rev A.1 重錨才放行）。漂移是設計缺陷的確定性後果；本 session 已對三張票逐一改為回填式，本票將其常設化，杜絕再犯。

## Verify Config
TICKET_ID=INFRA-GOV-DEESC-01
CREATED=2026-07-23
PRESET=bugfix
ALLOWED=.claude/CLAUDE.md,.claude/rules/ticket_pattern.md,.claude/skills/qa/SKILL.md
FORBIDDEN=.claude/skills/qa/scripts/verify_checklist.sh,.claude/settings.json,.claude/rules/ssot-governance.md
MAX_LINES=120
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>

## Action Items

### AI-1a: family provenance 規則本文改兩分支（SSOT＝CLAUDE.md Rule 4a）
- **檔案**: `.claude/CLAUDE.md`（Rule 4a）
- **現值**: Rule 4a 現為「family 未登記 → 無條件 STOP + escalate CSO，禁自行補表或改 regex」——無條件觸發。
- **目標值**: 改為兩分支，判定條件全機械、禁語義化：
  1. **自動補登分支**：family 未登記 **且** 同 family 之票檔實存於 `plans/cso_briefing/project/Pending/` 或 `deliver/` **且** 該票檔「簽發者」欄含 `CSO` → 開工（AI-0/Boot）時**自動 append** provenance 行（family、首票 TICKET_ID、票檔相對路徑、補登日期）至 ticket_pattern.md 之 Family Provenance 補登紀錄區塊（見 AI-1b），**MUST NOT** 改動 Ticket ID regex 與判定邏輯，**MUST** 於證據欄留補登紀錄。CSO 簽發票檔本身即授權證據；補登是轉錄，非裁決。
  2. **STOP 分支（保留）**：family 未登記 **且** 無任何符合上述條件之票檔（commit 訊息／分支名憑空出現新 family）→ 維持 STOP + escalate——此為規則原意防禦之「agent 自行發明 ID」場景，**MUST NOT** 自動化。
- **SSOT 紀律**: 規則邏輯以此處（CLAUDE.md Rule 4a）為唯一 SSOT；ticket_pattern.md 不得複寫規則本文，僅得以指針引用（見 AI-1b），杜絕兩檔字面互斥。
- [x] 完成

### AI-1b: ticket_pattern.md 承載六欄 provenance 表（新增獨立區塊）＋指針
- **檔案**: `.claude/rules/ticket_pattern.md`
- **現值**: 既有兩表皆 3 欄（「合法格式一覽」＝格式/說明/範例；「變更紀錄」＝Ticket/新增格式/說明），無能承載六欄之相容錨點；規則本文分散、部分僅於「變更紀錄」表尾一句提及 Rule 4a。
- **目標值**:
  1. **新增獨立區塊** `## Family Provenance 補登紀錄`，六欄表頭：`family｜首票 TICKET_ID｜代表票｜來源路徑｜補登日期｜追認依據`。**MUST NOT** 改動既有兩表結構或既有列。
  2. **變更紀錄表尾指針**：更新為「family provenance STOP/自動補登分流規則之 SSOT＝CLAUDE.md Rule 4a（two-branch，INFRA-GOV-DEESC-01 Rev A）」，本檔僅承載補登資料表，不複寫規則本文。
- [x] 完成

### AI-1c: 一次性補登 `MEO-` family（CSO 事後追認）
- **檔案**: `.claude/rules/ticket_pattern.md`（Family Provenance 補登紀錄 區塊，AI-1b 建立）
- **目標值**: append `MEO-` family 一行——family `MEO-`、首票 `MEO-T2`、現行代表票 `MEO-FIX-A`、來源路徑 `plans/cso_briefing/project/Pending/MEO-FIX-A_Escalation_Gate_Relocation.md`、補登日期 `2026-07-25`、追認依據「CSO 簽發並直接指示執行」。純敘述性紀錄，**MUST NOT** 改動 regex 或判定邏輯。
- **來源**: 本項原列於 `MEO-FIX-A` Rev C.1 之 AI-5 條款 5，但 `ticket_pattern.md` 不在該票 ALLOWED，構成 scope 自相矛盾（Codex M2 + Fugu Residual Issue 2 同時指出）。Rev D 已刪除該條並將 `ticket_pattern.md` 移入其 FORBIDDEN，本項改由本票承接。
- ⚠️ **排程約束**: 本票因此成為 `MEO-FIX-A` Rev D/E 之**前置依賴**，MUST 先行完成。
- [x] 完成

### AI-2a: DIFF_REF 模板規則（模板層，新建 SSOT 落點）
- **檔案**: `.claude/rules/ticket_pattern.md`
- **現值**: 本檔目前無「票據模板」章節，亦無任何 Verify Config／DIFF_REF 相關文字（grep 確認 DIFF_REF 僅出現於 FORBIDDEN 之 verify_checklist.sh 與 CLAUDE.md）。
- **目標值**: **新增章節** `## 票據模板規範`（誠實標示為新建 SSOT 落點，非修改既有段落），寫入規則：票據模板之 `DIFF_REF` **MUST** 為字面佔位 `<開工時 git rev-parse --short HEAD 回填>`；起草時 **MUST NOT** 硬編具體雜湊。
- [x] 完成

### AI-2b: DIFF_REF 回填三原子動作（Scope Gate 層）
- **檔案**: `.claude/skills/qa/SKILL.md`（**Step 0：Ticket Scope 驗證 / Scope Gate**）
- **現值**: qa/SKILL.md 無名為「Boot Sequence」之章節（該章節唯一定義於 FORBIDDEN 之 CLAUDE.md）；Step 0 現讀取 `tmp/verify_cmd.conf` 執行 Scope Gate。DIFF_REF 回填時機為「開工某張票時」，語意歸屬 Scope Gate 而非 session Boot。
- **目標值**: 於 Step 0（Scope Gate）新增回填三原子動作子條款——開工時自動執行 `git rev-parse --short HEAD` 回填 `DIFF_REF`、同步再生 `tmp/verify_cmd.conf`、回填值與 conf 摘要記入證據欄。三動作為原子組，缺一即停。**不強求標題字面對應「Boot Sequence」。**
- [x] 完成

### AI-2c: Anchor Drift STOP 首次成文（Scope Gate 層）
- **檔案**: `.claude/skills/qa/SKILL.md`（Step 0：Scope Gate）
- **現值**: grep 全 repo，「Anchor Drift」僅出現於 `.claude/PROGRESS.md`（歷史事件敘述）；兩份 ALLOWED 檔與任何 .sh／規則檔皆**無**此機制之機械定義。此判定分支目前僅存於 /sv//qa 人工執行慣例，**未落文字**。
- **目標值**: 於 Step 0 **新增一段明文首次成文**（誠實框定為「首次成文」，非「收窄既有」）：回填制下，Anchor Drift STOP 僅保留一種觸發——「回填後之 `DIFF_REF..HEAD` 之間出現**觸及本票 ALLOWED 檔**之 commit」（票面現值描述失真風險，需人裁決）；其餘 HEAD 前進一律不觸發。**MUST 於同段字面註明**：本判定目前**尚無 hook 機械強制點**，屬 /sv//qa 執行者行為準則，未來若機械化須另立票（禁以現在式措辭暗示機制已存在——現在式主張須現在式證據律）。
- [x] 完成

## Hard Constraints
1. doc-only：**MUST NOT** 修改任何 `.sh`／`.py`／判定引擎；三條治理規則以文字承載，由既有 AI-0/Boot/Scope-Gate 流程照章執行。
2. **MUST NOT** 改動 Ticket ID regex 本體（two-branch 修改限於 STOP/自動補登分流，不觸及 ID 格式 regex）。
3. **MUST NOT** 移除任一 STOP 分支之「保留」場景（AI-1a §2 STOP 分支、AI-2c Anchor Drift 保留觸發）——自動化只消解「結論預定」的 escalation，不消解真裁決。
4. CLAUDE.md 之修改**僅限 Rule 4a**（family provenance 兩分支）；**MUST NOT** 觸及 CLAUDE.md 其他任何規則或 Boot Sequence 章節。
5. `Do NOT refactor outside this scope. Once completed, STOP and wait for Quality Gate verification.`

## Error Handling
- 三檔任一 patch 錨點模糊 → 停止回報。
- 規則現行文字與本票「現值」描述不符（規則已被他票先行修改）→ 停止回報，禁強行套用。
- CLAUDE.md Rule 4a 現值與 AI-1a 描述不符（Rule 編號漂移或已被改）→ 停止回報，禁猜測對應。

## Acceptance Criteria
- [x] **[存在性]** CLAUDE.md Rule 4a 含自動補登分支與保留之 STOP 分支，兩分支機械條件逐字可查；Ticket ID regex 本體位元未變（以 `git diff` 佐證 regex 行未動）。
- [x] **[存在性]** ticket_pattern.md 含新增 `## Family Provenance 補登紀錄` 區塊，六欄齊備（family／首票／代表票／來源路徑／補登日期／追認依據），且含 `MEO-` family 一行；既有兩表結構未動（`git diff` 佐證僅新增區塊與敘述行）。
- [x] **[存在性]** ticket_pattern.md 含新增 `## 票據模板規範` 章節，`DIFF_REF` 為回填佔位字面；變更紀錄表尾指針指向 CLAUDE.md Rule 4a SSOT。
- [x] **[存在性]** qa/SKILL.md Step 0（Scope Gate）含回填三原子動作，及 Anchor Drift STOP 首次成文段（含「尚無 hook 機械強制點」字面註記）。
- [x] **[行為性]** 負向保留驗證：構造「無 CSO 簽發票檔之新 family」情境（紙上推演即可，證據欄記載推演），確認落入 STOP 分支而非自動補登。
- [x] **[存在性]** `git diff --stat <DIFF_REF>` 僅涉及三個 ALLOWED 檔，總變更不超過 `MAX_LINES=120`。
- [x] `assembleDebug` PASS
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

⚠️ DEFENSIVE WARNING: 本票消解的是「答案預先確定」的裁決噪音。若實作中發現任何場景的答案**不**預先確定，該場景必須留在 STOP 分支——寧可多停一次，不可少停該停的一次。
