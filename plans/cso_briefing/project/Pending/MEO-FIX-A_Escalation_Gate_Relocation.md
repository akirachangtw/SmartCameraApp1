# MEO-FIX-A (Rev J): Escalation gate 執法點遷移(Stop→PreToolUse)+ episode identity + 機械可讀裁決管道

**簽發日期**: 2026-07-21(Rev B 07-22;Rev C/C.1 07-22;Rev D 07-23;Rev E 07-25;Rev F 07-25;Rev G 07-25;Rev H 07-26;Rev I 07-26;**Rev J 2026-07-26**)
**簽發者**: Claude(CSO);Rev F 依 Codex Rev E 復審(`reports/MEO-FIX-A_RevE_Codex_ReReview_20260725.md`:C1/C2/M2 PASS、C3 FAIL、M1 PARTIAL、§6 五 PASS/一 PARTIAL/二 FAIL)之 R1–R4 required corrections 修訂
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: execute_review
**Rev**: J
**前置依賴**:
1. ✅ **`INFRA-GOV-DEESC-01` 已於 2026-07-25 完成收案**(commits `39e497e`／`b0c7435`;票檔入 `deliver/`;`MEO-` family provenance 已落 `ticket_pattern.md`)——前置解除。本票仍 **MUST NOT** 觸碰 `.claude/rules/ticket_pattern.md`(維持 FORBIDDEN)。
2. 本票為 **MEO-T3 治理審查 blocked 狀態下的必要修補路徑**——母票 MEO-T3 第三次 execute_review 判定 `Approach Drift` 且 `strict_verification` blocked(`.claude/PROGRESS.md` Next Actions #17),其重送 PASS 之前提即為本票所修之 livelock 結構;本票 `PRESET=bugfix` 走修補路徑,不觸犯「PASS 前禁開新 MEO 功能票」。
3. Rev F **MUST** 通過 Codex 復審解除 `REPORT_FIX` 方得入 `/uc`。

## Rev J 修訂說明(Codex Rev I delta 復審 F1/F2;CSO 裁決 2026-07-26)

| # | 來源 | 缺陷 | Rev J 落點 |
|---|---|---|---|
| 1 | Codex Rev I F1(BLOCKER,`plans/cso_briefing/reports/MEO-FIX-A_RevI_Codex_Delta_ReReview_20260726.md`) | **(B)/(C) 分類域裂隙**:Rev I 之 (B) 擴充至 16 鍵,而 (C) 沿用 Rev H 三類結構未同步擴域——「通過 (B) 之各行 MUST 歸入三類」對 live `STATE.md` 之 **9 個合法非 HC-5 鍵行**無類可歸,照票驗收機械 FAIL(票面自證不可兌現,§C.8 死條款同構;Rev I 對 (B) 做正向試跑卻未對 (C) 做同標的試跑,半套執行) | (C) 分類域字面收窄至「**`#` 註記行 only**」,全部 16 合法鍵行由 (B) 完整承接;原 (C)(1) 改**權限聲明**(HC-5 四鍵為唯一授權 mutation 標的,行為性驗證由 AC §6-3/§6-4 fixture 承擔,非結構稽核);**否決**新增第四類承接鍵行——「值變更」分類於 current-state 稽核**機械不可判**(無 baseline,依 (A)),復活即第四條死條款 |
| 2 | Codex Rev I F2(MAJOR) | **16 鍵來源敘述與碼源不符**:`MEO_LAST_TASK_TYPE`/`MEO_LAST_FAILURE_MODE`/`MEO_QUOTA_STATE` 於兩 hook 均零引用,Rev I 將檔內觀測鍵錯誤折入「hook 碼源聯集」敘述(過度宣稱) | 來源敘述拆分為 **13 live hook keys + 3 SSOT/schema keys**;後三者 authority=`decision-tree.yaml` L45/115/131(`MEO_QUOTA_STATE` 枚舉定義+trigger 消費)與 `SKILL.md` L208–214 STATE schema 模板,producer=執行者 agent 診斷流程經 R9 自舉豁免通道(CSO 2026-07-26 grep 雙檔取證) |

**Rev J 重入協定(取代 Rev I 版;僅此範圍,禁擴張)**:
1. Rev J **MUST** 通過 Codex **delta 復審**(差分=本節 + Verify Config `REV` + AC §6-8(iii)(B)(C) + Rev I 協定取代標記)並重新錨定 Ticket SHA-256 後方得重入(Rev I 受審錨 `680526D7...` 隨本次變動失效)。
2. 重入序=conf 再生(`REV=J`,`DIFF_REF=fdd6375`/`DIFF_END=e065d20` 字面直取)→ `/qa` 重跑 → `/sv`;**AI-0 不重跑**。
3. **現行值唯一化**(沿 Rev I 協定 3):`MAX_LINES=332`/`DIFF_REF=fdd6375`/`DIFF_END=e065d20` 為唯一現行值;其他數值一律歷史敘述,禁作執行依據。
4. **零變動條款**(沿 Rev I 協定 4):AI-1–AI-5 程式碼與 fixture、`STATE.md`、Error Handling α 均零觸碰;`tmp/verify_cmd.conf` 於 delta PASS 前維持 `REV=F` 停止態。
5. 歷史記載不回改(Rev G/H/I 各節保留原文,僅加取代狀態標記)。

**Rev J 自洽性雙向試跑證據(新條款雙向自證,CSO 2026-07-26)**:(C) 新分類域對 live `STATE.md` 正向試跑——檔內全部 `#` 註記行經逐塊核驗僅二型:遷移區塊(雙錨唯一命中,Codex Rev I §4.3 佐證)與診斷/覆核/註記區塊(agent 產物),**無第三型殘餘**,分類域完備;(B) 承接 16 鍵行之判準式維持 Codex Rev I §4.3 零違例取證。Codex Rev I Advisory(重入後新 artifacts 之 `/qa`/`/sv` 核對義務)維持 advisory 地位,由 `/qa` 報告承接,不入本輪票面。

## Rev I 修訂說明(Codex Rev H delta 復審 F1/F2/F3;CSO 裁決 2026-07-26)

| # | 來源 | 缺陷 | Rev I 落點 |
|---|---|---|---|
| 1 | Codex F1(BLOCKER,`plans/cso_briefing/reports/MEO-FIX-A_RevH_Codex_Delta_ReReview_20260726.md`) | **活動條款殘值脫鉤**:AI-2 §C.8 仍字面要求「將其行數併入 `MAX_LINES=300` 合計」,與 Verify Config／AC 之 **332** 並存為兩個現行值,執行者無法依 SSOT 唯一決定;且「固定 `DIFF_END=e065d20`」與「其後新增 artifacts 併入該固定 range 計數」機械互斥(F-b 殘餘),於 332 精確邊界上任一非零 artifact 即立即超額 | §C.8 廢除行數併計條款,改**驗收期 artifacts 不入行數計帳**、逐檔列舉由 AC §6-8(ii) 承接;`300` 自現行條款全面退場 |
| 2 | Codex F2(BLOCKER) | **baseline SHA-256 有指令無實值**:唯讀窮盡查核(`tmp/verify_cmd.conf`／票面／Executor Briefing／reports／repo 全樹)僅見「MUST 記錄 baseline SHA」之指令文字,無任何耐久載體存其實值;現行 `STATE.md` SHA 屬 after-state,不得充 before-state。CSO 覆核佐證:`STATE.md` 尾段 AI-0 診斷區塊字面載 `record STATE.md baseline SHA-256`,證**指令確曾下達、實值確未落地** | **誠實裁決:歷史 delta 不可重建**。AC §6-8(iii) 廢除「以 baseline SHA-256 證明已變更」之宣稱,改**現況結構性稽核**;AI-0 步驟 4 同步廢除其佐證前提地位 |
| 3 | Codex F3(MAJOR) | **三類白名單非完全定界**:第 (2)(3) 類僅具名稱,無 block delimiter／允許鍵集合／producer anchor,任何自由文字標為 diagnosis/review 即可歸類,與「鍵級白名單禁一切未授權鍵」之宣稱不等價 | 三軌定界:**(a)** 白名單自碼源取證擴充為**顯式 16 鍵**集合+三型行文法機械判準;**(b)** 遷移區塊以**字面起訖錨**定界凍結;**(c)** 第 (3) 類 producer 事實更正並分層誠實揭露 |
| 4 | CSO 碼源取證(2026-07-26) | **producer 誤植**:票面稱第 (3) 類為「hook 自動代謝註記」,惟 `meo_signal_update.sh` 與 `meo_pretooluse_gate.sh` 之全部 `STATE.md` 寫入僅經 `kv_set`／`kv_set_f`(純 `KEY=value` 之 `sed` 就地替換或 `printf` 追加),**兩 hook 從不產生 `#` 註記行**;該類實由執行者 agent 經 gate v1.2(CSO R9)`STATE.md` 自舉豁免分支寫入 | AC §6-8(iii) 第 (3) 類 producer 字面更正,並據以重定機械判準邊界 |

**Rev I 重入協定(取代 Rev H 版;⚠️ 已由 Rev J 重入協定取代,本節僅存歷史)**:
1. Rev I **MUST** 通過 Codex **delta 復審**(差分=本節 + Verify Config `REV` + AI-0 步驟 4 + AI-2 §C.8 + AC §6-8(iii) + Rev H 協定取代標記)並重新錨定 Ticket SHA-256 後方得重入(Rev H 識別錨 `1BD69169...` 隨本次變動失效)。
2. 重入序=conf 再生(`REV=I`,`DIFF_REF=fdd6375`／`DIFF_END=e065d20` 字面直取)→ `/qa` 重跑 → `/sv`;**AI-0 不重跑**(斷言為 Rev F 開工時態,已驗訖並經 AI-1–AI-5 合法推進)。
3. **現行值唯一化**:`MAX_LINES=332`、`DIFF_REF=fdd6375`、`DIFF_END=e065d20` 字面固定且為**唯一現行值**;票面任一處出現 `260`／`300` 等其他數值者一律為歷史修訂敘述,**MUST NOT** 作為執行依據。
4. AI-1–AI-5 程式碼與 fixture **零變動**;`STATE.md` **零觸碰**;`tmp/verify_cmd.conf` 於 delta PASS 前維持 `REV=F` 停止態;Error Handling α 條款**字面零變動**;Rev I 僅修票面治理。
5. 歷史記載不回改(Rev G／Rev H 修訂說明表與協定保留原文,僅加取代狀態標記)。

**Rev I 波及面全檔掃描證據(修訂波及面全檔掃描律,CSO 2026-07-26;行號為 Rev H 版面)**:以 `grep_files(regex=True)` 全檔掃 `300|MAX_LINES|baseline|白名單` 命中 **15** 處、掃 `REV=|Rev H|\*\*Rev\*\*` 命中 **18** 處,逐處分類完竣——**現行執行依據**僅 L71 `MAX_LINES=332`、L292 AC (iv)、L293 合計句三處(皆為 332,無殘值);**歷史敘述**為 L17/18/19/79/81 修訂說明與 L22–36 舊協定;**無關語境**為 L49/L261(error class 無白名單自由抽取)與 L263(裁決載體白名單律)。**唯一未同步之現行活動條款為 AI-2 §C.8**(本次 F1 修訂標的),已於本 Rev 更正。此掃描為 Rev G→H→I 連續三輪之殘值同族缺陷(K4-cand-20260725-04「開票期機械檢核缺口」)之直接論據。

## Rev H 修訂說明(Codex Rev G delta 復審 F1 + CSO 前置取證 F-a/F-b;CSO 裁決 2026-07-26)

| # | 來源 | 缺陷 | Rev H 落點 |
|---|---|---|---|
| 1 | Codex F1(BLOCKER,`plans/cso_briefing/reports/MEO-FIX-A_RevG_Codex_Delta_ReReview_20260725.md`) | **wildcard token 未同步至行數 consumer**:`verify_checklist.sh` L43–50 scope matcher 認 glob、L405–409 行數計算器 awk `$3==token` 字面等值不認,`SCHEMA.md` 65 行漏算 → `/qa` 將以 `+236≤300` 假綠;且真值已超額:tracked ALLOWED additions=301、added+deleted=**332** > `MAX_LINES=300`(Rev F +40 估算低估 §C.1 強制四節之 `SCHEMA.md` 65 行) | `MAX_LINES` 依實測回校 **332**;新增 AC §6-8(iv) 顯式路徑 glob-aware 核對;`+236` 明文禁充證據 |
| 2 | CSO 前置取證 F-a(2026-07-26,全樹 3,455 檔) | **baseline 複本死條款**:AC §6-8(iii) 要求 `git diff --no-index <baseline複本>` 行數佐證,但 AI-0 步驟 4 僅授權記錄 baseline **SHA-256**,複本產生通道從未授權且複本實測不存在,行數重建機械不可得——**前置資源恆存律**違反,本票第三條死條款(Rev F 既存,兩輪復審未攔) | AC §6-8(iii) 改**鍵級白名單制**;AI-0 步驟 4 SHA-256 用途重定義為「變更事實」佐證(差分註記) |
| 3 | CSO 結構分析 F-b | **活動標的折入固定常數**:`STATE.md` 之 delta 含 hook 代謝註記(無界追加)與 2026-07-25 out-of-band 遷移區塊,計入 `MAX_LINES` 即簽下「不因實作變更、僅因時間流逝即可 FAIL」之契約,livelock 同形 | `MAX_LINES` 治理範圍收窄至 **tracked ALLOWED**;`STATE.md` 改結構性 AC(鍵級白名單)——反夾帶意圖升嚴:行數預算容任意短內容,白名單禁一切未授權鍵 |

**Rev H 重入協定(取代 Rev G 版;⚠️ 已由 Rev I 重入協定取代,本節僅存歷史)**:
1. Rev H **MUST** 通過 Codex **delta 復審**(差分=本節 + Verify Config `REV/MAX_LINES/DIFF_REF/DIFF_END` + AC §6-8(iii)(iv) + AI-0 步驟 4 差分註記 + Rev G 協定取代標記)並重新錨定 Ticket SHA-256 後方得重入(Rev G 識別錨 `7772BCF...` 隨本次變動失效)。
2. 重入序=conf 再生(`REV=H`,`DIFF_REF=fdd6375` 字面直取)→ `/qa` 重跑 → `/sv`;**AI-0 不重跑**(斷言為 Rev F 開工時態,已驗訖並經 AI-1–AI-5 合法推進)。
3. `DIFF_REF=fdd6375`、`DIFF_END=e065d20` **字面固定**——唯一合法來源即此二字面值,禁自現 HEAD 或任何間接來源重導出。
4. AI-1–AI-5 程式碼與 fixture **零變動**;Error Handling α 條款**字面零變動**;Rev H 僅修票面治理。
5. 歷史記載不回改(Rev G 修訂說明表與協定保留原文,僅加取代狀態標記)。

## Rev G 修訂說明(執行期 /qa Step 0 SCOPE VIOLATION 取證;CSO 裁決 2026-07-25)

| # | 來源 | 缺陷 | Rev G 落點 |
|---|---|---|---|
| 1 | 執行者 /qa Step 0 STOP(Rule 4c,commit `e065d20` 後取證) | **ALLOWED 死 token**:`meo/adjudications/` 裸尾斜線目錄 token 於 `token_matches_file()`(INFRA-VERIFY-GLOB-01 修正版)語意下僅作字面比對(token 不含 `*/?/[` 即不展開 glob),**永不匹配該目錄下任何檔案**——票面授權範圍含一條機械上不可能兌現的殭屍條款,致本票自身交付物(AI-2 §C 之 `SCHEMA.md`/`.gitkeep`,commit `e065d20`)被誤判 SCOPE VIOLATION。build/tests/lint 全綠,純機械閘門缺口,非程式碼缺陷、非執行者過失 | ALLOWED token 改 `meo/adjudications/*`;AC §6-8 (i) 拼寫同步;conf 再生註記改 Rev G 語意 |

**Rev G 重入協定(僅此範圍,禁擴張;⚠️ 已由 Rev H 重入協定取代,本節僅存歷史)**:
1. Rev G **MUST** 通過 Codex **delta 復審**(差分=本表 + 本協定 + token/Rev 字樣同步處)並重新錨定 Ticket SHA-256 後,方得重入(Rev F 復審錨 `71054F44...` 隨票面變動失效,依 Key Decision「票面任何字元變動即令復審錨失效」教義,以重新錨定取代 fiat 例外)。
2. 重入序=conf 再生(`REV=G`)→ `/qa` 重跑 → `/sv`;**AI-0 前置檢核不重跑**——其斷言(`meo/adjudications` 尚不存在、tracked=6、drift guard)為 Rev F 開工時態,已於 Rev F Boot 驗訖,且已被 AI-1–AI-5 合法推進(現況:目錄存在、tracked=8、commit `e065d20`)。
3. `DIFF_REF` **沿用 Rev F 開工時已回填之既值**(AI-1–AI-5 工作於其上完成);**MUST NOT** 以現 HEAD 重填,否則 diff 基線流失、§6-8 範圍核對失效。
4. AI-1–AI-5 已完成之程式碼與 fixture 測試**零變動**;Rev G 不觸碰任何 ALLOWED 檔案內容,僅修票面;Error Handling α 條款**字面零變動**(裁決載體連續性)。
5. 歷史修訂記載(Rev C.1 更正註記等)所稱 `meo/adjudications/` entry 即本次改拼之 token;**歷史記載不回改**(truthful history)。

## Rev F 修訂說明(Codex Rev E 復審 R1–R4 + CSO α 裁決)

| # | 來源 | 缺陷 | Rev F 落點 |
|---|---|---|---|
| 1 | Codex C3 / R1 | **BLOCKER:release transition 遺漏 resolving decision**。live `decision-tree.yaml` 之 `pending_release.signal_preservation` 字面要求「resolving decision MUST 寫入 `MEO_LAST_ESCALATION_DECISION` **先於** `MEO_ESCALATION_PENDING` 清除」;Rev E §C.5 mutation set 僅含 `PENDING`+`WM_*` 且緊接禁改其他鍵 → 照票實作必留 stale decision(前一 episode 之值),與當前裁決檔分歧,`decision_authority_not_usurped` 無法自 STATE 證明 | §C.5 改**五步全成全敗序**;HC-5 mutation set 字面同步 |
| 2 | Codex R2 | decision 一致性無 AC 覆蓋 | AC-5 增 (e)(f);AC-7 增 SSOT 消費點 |
| 3 | Codex M1 / R3 | **MAJOR:absolute path 不可達**。Claude Code hook 契約之 `tool_input.file_path` 為**絕對路徑**(如 `D:/DevSources/.../STATE.md`),不以 `STATE.md`／`.claude/...` 起頭;Rev E AI-3 §3 之相對前綴字面比對**永不命中**,gate 形同虛設 | AI-3 §3 改 canonical absolute matching 契約;新增 AC-6 四組正反測試 |
| 4 | Codex §8 / R4 | external producer drift:`mcp-ticket-platform` 若未來接入 Claude Code session,agent 可繞過 gate 直呼 `write_file_safe`(settings matcher 僅涵蓋 `Write\|Edit\|MultiEdit\|Bash`) | AI-0 增 drift guard 檢核;HC-11 |
| 5 | CSO 裁決(2026-07-25) | α「error class 字面比對」之**比對域全域無定義**(CSO grep + 執行者 grep 雙向取證;`meo_signal_update.sh` L46-58 之 `ERR_CLASS` 為無白名單自由正則抽取,codebase 從未建立「合格 class 清單」概念) | Error Handling α 條款**字面化並解凍**;記憶檔降級 |

**CSO 自陳(Rev E 缺陷成因,供 KAIROS)**:§C.5 之 mutation set 係**逐字轉錄 Codex Rev D 報告 R2 措辭**(「只能修改 active ID 對應的 `PENDING` 與 watermark 鍵」),未對照 live `decision-tree.yaml` 逐鍵驗證完備性——審查者的修正集措辭本身不完備,轉錄即繼承其遺漏。**修正集轉錄完備性律**(候選):吸收任何 required corrections 時,MUST 對照其所指向之 live SSOT 檔逐項驗證,禁以審查者措辭為規格 SSOT。

**Rev C.1 兩項更正(CSO 自陳,Rev E 校正計數,Rev F 沿用)**:
- **L52/56 殘留**:Rev C.1 僅修主段落,未清 Verify Config 註記與 AI-0 之 `tracked` 字樣;Rev D 之「7 項 tracked」表述仍失真(Codex Rev D M2 取證)。**現在式正確表述:ALLOWED 計 8 個 path entries;其中既有 git-tracked **6** 項、`STATE.md` gitignored、`meo/adjudications/` 於開工時尚不存在。**(Codex Rev E 已 PASS 此項)
- **conf 再生免除裁決撤回**:**Rev bump 一律再生 conf,無例外**(本 Rev F 亦適用;現存 conf 為 `INFRA-GOV-DEESC-01 Rev A`,尚未進入本票 Boot——此為正確停止狀態)。

**STATE.md 追蹤狀態(終局,不再重開)**:`git ls-files -- STATE.md` 為空、`git check-ignore -v` 命中 `.gitignore:76`——曾 tracked、後經 `rm --cached` 除籍,**現為 gitignored**,且此為 MEO-T3 既定裁決。維持現狀,**MUST NOT** 為本票重新 `git add`。`STATE.md` 仍留 ALLOWED——ALLOWED 承載「授權修改」語義,不等於「入 git diff 治理」。

## 背景(裁決摘要)

`MEO_ESCALATION_PENDING=true` 綁在 Stop 事件封鎖構成 livelock:Stop-block 強迫 agent 繼續,pending_release 四要素禁止 agent 動作,唯一輸出「Holding — unchanged」無限重複,燒毀 turn/quota。同族前例:R13 StopGate latch deadlock。

追加根因:level-trigger 與「訊號保全」互相衝突——release 後 STREAK 永久高於閾值致必然重掛(假陽性復鎖);裁決有語義但無 episode identity,裁決檔與 escalation 事件無可達關聯。

本票為結構修補,**不改** escalation 觸發閾值數值(D1:`RETRY>=2 ∥ STREAK>=2`)與信號累加算式,僅增設「新 episode 何時成立」之武裝條件與執法點遷移。

## Verify Config

TICKET_ID=MEO-FIX-A
CREATED=2026-07-21
REV=J
PRESET=bugfix
ALLOWED=stop_orchestrator.sh,STATE.md,gate.conf,.claude/skills/model-effort-orchestrator/hooks/meo_pretooluse_gate.sh,.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh,.claude/skills/model-effort-orchestrator/decision-tree.yaml,.claude/skills/model-effort-orchestrator/SKILL.md,meo/adjudications/*
FORBIDDEN=.claude/skills/sv/scripts/cross_review.sh,.claude/CLAUDE.md,.claude/skills/qa/scripts/verify_checklist.sh,.claude/hooks/stop.sh,.claude/hooks/telegram_stop.sh,.claude/settings.json,.claude/rules/ticket_pattern.md
MAX_LINES=332
DIFF_REF=fdd6375
DIFF_END=e065d20

> **MAX_LINES 調整說明(260→300)**:Rev F 新增 (a) 五步 release transition 含 decision materialization 與終驗 read-back(估 +15 行)、(b) canonical absolute path 正規化與 exact/prefix 分治比對(估 +25 行)。依動作直取,非語義化升級。

> **MAX_LINES 回校說明(300→332,Rev H)**:依 `fdd6375..e065d20` tracked ALLOWED **實測**回校(Codex delta 取證+CSO `git_changed_files` 獨立覆核):added=301(含 §C.1 強制四節之 `SCHEMA.md` 65 行——Rev F +40 估算之低估主因)、deleted=31,added+deleted=**332**。實測值落錨,非估算(K4-cand-20260722-01 閾值實證回校律);`STATE.md` 不再計入(治理範圍收窄至 tracked ALLOWED,見 AC §6-8(iii);Rev H 改制,Rev I 再修訂)。

> **實路徑機械證據(2026-07-22 取證,Rev F 沿用)**:`stop_orchestrator.sh` 位於 **repo 根**(1,239 bytes);`gate.conf` 位於 **repo 根**(522 bytes);`STATE.md` 位於 **repo 根**、**現為 gitignored**(`.gitignore:76`);三個 MEO hooks 位於 `.claude/skills/model-effort-orchestrator/hooks/`;`meo/adjudications/` 為本票新建。
>
> **FORBIDDEN 說明**:`.claude/settings.json` 入 FORBIDDEN——hook 註冊變更屬 D5 架構層;`.claude/hooks/stop.sh`、`telegram_stop.sh` 為另兩個 Stop hook,收斂屬 TODO-3 稽核票;`.claude/rules/ticket_pattern.md` 入 FORBIDDEN——provenance 補登已由 `INFRA-GOV-DEESC-01` 完成,本票禁碰。
>
> ⚠️ **conf 再生**:Rev J 定稿並通過 Codex delta 復審後 **MUST** 重建 `tmp/verify_cmd.conf`(`REV=J`);`DIFF_REF`/`DIFF_END` 依字面固定值 `fdd6375`/`e065d20` 直取(見 Rev J 重入協定 3,禁自現 HEAD 或間接來源重導出),兩動作為原子組。

- [ ] **AI-0(開工前置)**:
  1. 以 `git ls-files` 逐項確認 ALLOWED 之 **6 項既有 git-tracked 標的**實存(`stop_orchestrator.sh`、`gate.conf`、`meo_pretooluse_gate.sh`、`meo_signal_update.sh`、`decision-tree.yaml`、`SKILL.md`);以 `git check-ignore -v STATE.md` 確認其 **gitignored**;以 `test -d meo/adjudications` 確認其**尚不存在**;FORBIDDEN 7 項實存(教訓 #11)。**MUST NOT** 以「7 項 tracked」或任何未經 `git ls-files` 實測之計數表述。
  2. **【Rev F/R4】External producer drift guard**: 執行 `claude mcp get mcp-ticket-platform`,**MUST** 回報未配置。**若已配置 → 立即 STOP 回報**——外部 producer 前提失效(agent 可繞過 gate 直呼 `write_file_safe`);**MUST NOT** 於本票修改 `.claude/settings.json`(FORBIDDEN),須另立 D5／settings scope 票。
  3. `DIFF_REF` 回填並同步 AC-9;`tmp/verify_cmd.conf` 依 `REV=F` 重建。**(Rev G 差分註記:本步屬 Rev F Boot,已執行訖;Rev G 重入時 AI-0 不重跑,conf 再生依 Rev G 重入協定 2–3 為 `REV=G` 且 DIFF_REF 沿用既值。)**
  4. **記錄 `STATE.md` 修改前 baseline SHA-256**(Rev F 開工時態指令)。**(Rev I 差分註記:本步之產出**未落任何耐久載體**——Codex Rev H F2 唯讀窮盡查核與 CSO 覆核(`STATE.md` 尾段 AI-0 診斷區塊字面載本指令、全樹無實值)一致認定 **baseline SHA-256 實值不存在、歷史 delta 機械不可重建**。故本 SHA-256 **不再作為任何 AC 之佐證前提**,Rev H 之「變更事實佐證」用途一併廢止;AC §6-8(iii) 改現況結構性稽核。**MUST NOT** 以現行 `STATE.md` SHA 回填為 before-state,**MUST NOT** 以推測補值。本步保留於票面僅為真實歷史記載,不生執行義務。)**
  5. 證據貼證據欄。

## Action Items

### AI-1【C1 已 PASS,維持】Stop 分支 fail-open 契約

- **檔案**: `stop_orchestrator.sh`(MEO gate 分支)
- **現值**: `MEO_ESCALATION_PENDING=true` → 輸出 `"decision":"block"`(quota-exhausted 與 normal pending 兩分支皆是),無限重複
- **目標值**:
  1. pending=true → **MUST NOT** 輸出 `decision=block`。允許停止之正確形式為**省略 `decision` 鍵**:輸出不含 `decision` 之 `systemMessage`(註記「MEO escalation pending — 新 MEO 治理寫入前須外部裁決,見 `meo/adjudications/`」),或 stdout 保持空白;exit 0。
  2. **MUST NOT** 輸出 `decision=approve`——Stop top-level `decision` 僅接受 `"block"` 或省略,無 `"approve"` 值。
  3. `MEO_QUOTA_STATE=exhausted` 分支同屬「等待外部」類,一併改為省略 `decision` + 註記。
  4. 驗收 **MUST** 驗證 session **真正終止**,而非僅驗 JSON 可解析(AC-1)。
- [ ] 完成

### AI-2【C2 已 PASS,維持;§C 依 R1 修訂】Episode identity + watermark re-arm + 外部裁決管道

- **檔案**: `.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh`、`STATE.md`、`meo/adjudications/`(新建)、`meo_pretooluse_gate.sh`

#### §A concurrency-safe ID producer 與狀態轉移律【C2 PASS,原文維持】

- **現值**: 無 `MEO_ESCALATION_ID` producer;既有 `kv_set` 以 `sed -i` 就地改寫 `STATE.md`,**本身即非原子**(既存潛債)。
- **目標值**:
  1. 以 **`mkdir` 原子性**作互斥:`mkdir "$STATE_FILE.lock"` 成功者進入臨界區,失敗者退避重試(上限 5 次、每次 sleep 0.1s);逾限 → **fail-open exit 0 且 MUST NOT 寫出部分 ID 或部分 kv**。臨界區退出前 `rmdir` 解鎖;`trap` 確保異常路徑亦解鎖。
  2. 臨界區 **MUST** 涵蓋 ID 遞增與所有 `kv_set` 之 read-modify-write,不得只鎖 ID。
  3. `MEO_ESCALATION_ID` 於臨界區內讀取後 +1 寫回(monotonic **且有鎖**);**MUST NOT** 採無鎖遞增或以時間戳假冒序號。
  4. **狀態轉移三律(Codex C2,防同一 episode 重編號)**:
     - **律一**:**僅** `MEO_ESCALATION_PENDING=false` **且** §B 表列 delta 條件達標時,才遞增 `MEO_ESCALATION_ID` 並將 `PENDING` 置 true。
     - **律二**:`PENDING=true` 期間**只累積原始訊號**(RETRY/STREAK/ERROR_CLASS 照常更新),**MUST NOT** 遞增 ID、**MUST NOT** 建立新 episode;active ID 保持不變。
     - **律三**:`MEO_ESCALATION_ID` 缺失或空值 → 以 `0` 初始化後續行;**非數字** → **fail-open exit 0 且零寫入**(不猜測、不修復、不覆寫)。
- [ ] 完成

#### §B Watermark 公式與狀態轉移表(三類信號)【C2 PASS,原文維持】

裁決完成時記錄 watermark 快照:`MEO_WM_RETRY`、`MEO_WM_STREAK`、`MEO_WM_ERROR_CLASS`、`MEO_WM_REJECT`。**閾值數值沿用 D1 不變**,僅將絕對比較改為相對 watermark 之增量比較。下表條件為 §A 律一之「delta 條件」:

| 信號 | watermark 衰減規則(先執行) | delta 條件 | 說明 |
|---|---|---|---|
| `MEO_RETRY_COUNT` | 若 `RETRY < WM_RETRY`(成功路徑已歸零)→ `WM_RETRY := 0` | `RETRY - WM_RETRY >= 2` | 對應現行 `RETRY>=2`;watermark 隨計數器歸零而衰減,避免永久抑制 |
| `MEO_SAME_ERROR_CLASS_STREAK` | 若 `LAST_ERROR_CLASS != WM_ERROR_CLASS`(class 已切換,STREAK 被重置為 1)→ `WM_STREAK := 0`、`WM_ERROR_CLASS := LAST_ERROR_CLASS` | `STREAK - WM_STREAK >= 2` | 對應現行 `STREAK>=2`;異類錯誤視為新問題,watermark 不跨類抑制 |
| `MEO_VERIFIER_REJECT_COUNT` | 若 `REJECT < WM_REJECT` → `WM_REJECT := REJECT` | **不作為觸發條件** | ⚠️ REJECT **不在** `meo_signal_update.sh` 之 escalation 條件內;本票僅**記錄** watermark,**MUST NOT** 新增為觸發器(違 HC-3)。`decision-tree.yaml` 現值誤列其為 trigger,由 AI-5 條款 5 修正 |

- **合取關係**:delta 條件達標 = 上表任一條件為真(沿用現行 `∥` 語義)。**但新 episode 是否成立,仍受 §A 律一之 `PENDING=false` 前提節制。**
- **不變式**:**MUST NOT** 清零 `MEO_SAME_ERROR_CLASS_STREAK`(CSO R4 + MEO-T3 signal-preservation);watermark 是獨立鍵,信號本身永遠保全。
- **已裁決後首筆同類失敗**:因 `STREAK - WM_STREAK = 1 < 2`,不重播——此即 §6-3 驗收標的。
- [ ] 完成

#### §C 外部 producer 與可執行 consumer【Rev E 雙檔設計 PASS 維持;§C.5 依 R1 重寫】

1. **目錄 artifact(Fugu SSOT-3)**: 空目錄不入 git。`meo/adjudications/` **MUST** 含 `.gitkeep` 與 `SCHEMA.md`,二者為 git diff 可見之 artifact 憑證。`SCHEMA.md` **MUST** 記載下列 §C.2 雙檔規格、§C.5 五步 transition、§C.8 處置規則與 §C.4 誠實聲明。

2. **裁決 artifacts —— 雙 immutable 檔【Codex Rev E 3.1 PASS】**:單一檔案無法內嵌其自身建立後才產生之 `audit_id`,故拆為兩個各自 immutable 之 artifact:
   - **(a) decision record**:`meo/adjudications/<MEO_ESCALATION_ID>.md`。四必填欄:`decision`(enum:`approve_upgrade`/`deny_retry_with_high_effort`/`defer_and_checkpoint`)、`adjudicator`、`timestamp`、`signals_preserved`。**此檔內不含 audit_id**(機械上不可能)。
   - **(b) audit sidecar**:`meo/adjudications/<MEO_ESCALATION_ID>.audit.md`。三必填欄:`episode_id`(MUST 等於檔名 ID 與 decision record 檔名 ID)、`decision_record_audit_id`(= 建立 (a) 時 MCP 回傳 envelope 之 `audit_id`,由 producer 轉錄)、`timestamp`。
   - 兩檔皆 `mode="create"`,建立後皆 immutable。

3. **Producer(外部,順序固定)**: 裁決 artifacts 一律由 Akira 經 `mcp-ticket-platform:write_file_safe` 於 agent session **外**建立,順序:
   1. `mode="create"` 建立 (a) decision record → 取回 envelope 之 `audit_id`;
   2. `mode="create"` 建立 (b) audit sidecar,將上步 `audit_id` 轉錄入 `decision_record_audit_id` 欄。
   - **MUST NOT** 使用 `overwrite`/`append`——既有 receipt 禁覆寫,重複裁決須以新 ID 另立兩檔。
   - PreToolUse gate **MUST NOT** 內建任何「允許 agent 建立或改寫裁決 artifacts」之例外分支(另見 §C.5a)。

4. **可執行 consumer**: **MUST** 於 `meo_pretooluse_gate.sh` 內以固定函式 `release_pending()` 實作,可由函式符號錨定回填證據。**MUST NOT** 以 `decision-tree.yaml` 充當 consumer——YAML 是 SSOT(語義來源),不是可執行體。`release_pending()` 驗證項(全部通過方得進入 §C.5):
   - (a)(b) 兩檔**均**存在,且兩者檔名 ID **精確等於** `STATE.md` 之 active `MEO_ESCALATION_ID`(任一不符 → 視同無裁決);
   - (a) 之 `decision` 值屬 enum 三者之一,四必填欄齊備且非空;
   - (b) 之 `episode_id` 等於同一 ID,`decision_record_audit_id` 非空,三必填欄齊備;
   - 任一項不合 → 不放行、**STATE 零寫入**,**MUST NOT** 部分放行。
   - ⚠️ **保證範圍誠實聲明(MUST 同步寫入 `SCHEMA.md`)**:sidecar 驗證所證明者為**通道一致性**(兩檔經同一外部 MCP 通道循序建立),**非密碼學真實性**;偽造之真正防線為 gate 對 `meo/adjudications/` 路徑寫入之 `deny`(AI-3 §3)。**MUST NOT** 於任何文件、註解或 commit 訊息中宣稱 receipt 機制可防偽。

5. **【Rev F/R1】唯一 release mutation owner 與五步全成全敗 transition**: `release_pending()` 為 **STATE release transition 之唯一可信執行體**,驗證通過後由其**自行**執行下列序列,不得委由被攔截之 tool call 代勞。**全序列 MUST 位於與 §A.1 signal producer 同一把 `mkdir` 鎖之單一臨界區內**,採**全成全敗**語義:
   1. **重新 read-back**:於鎖內重讀 active `MEO_ESCALATION_ID`、`MEO_ESCALATION_PENDING`,及 (a)(b) 兩裁決 artifacts;與 §C.4 驗證時之值不一致 → 中止、零寫入。
   2. **寫入 resolving decision**:將 (a) 之 `decision` enum 值寫入 `MEO_LAST_ESCALATION_DECISION`。
   3. **寫入 watermark**:寫入該 episode 對應之 `MEO_WM_*` 鍵。
   4. **最後**才將 `MEO_ESCALATION_PENDING` 由 `true`→`false`。
   5. **終驗 read-back**:確認 `MEO_LAST_ESCALATION_DECISION`、`MEO_WM_*`、`MEO_ESCALATION_PENDING` 三者**全部**符合預期值。
   - **順序為硬約束**:步驟 2 **MUST 先於**步驟 4——此即 live `decision-tree.yaml` `pending_release.signal_preservation` 之 record-before-clear 契約。
   - **任一步失敗 → 中止並回退為「未 release」語義**:**MUST NOT** 只清 `PENDING`;**MUST NOT** 留部分 watermark;**MUST NOT** 保留上一 episode 之 stale decision。
   - **範圍**:除步驟 2/3/4 所列鍵外,**MUST NOT** 修改任何其他 STATE 鍵、MEO scripts、SSOT 檔或裁決 artifacts。
   - **觸發本次驗證之該筆原始 `Write/Edit/MultiEdit/Bash` 呼叫,MUST 仍回 `deny`**——release 不得成為夾帶任意 MEO payload 之通道。

5a. **「純讀」字面收窄(取代 Rev D 之「gate 純讀零例外面」)**: gate 對 `meo/adjudications/` **保持純讀**(禁建立、禁改寫裁決 artifacts);其**唯一允許之 side effect** 為 §C.5 所定之受鎖 release transition。除此之外 gate 零寫入。
   > ⚠️ 本條**字面修訂** 2026-07-23 裁決 `MEO_FIXA_RevD_Consolidated_Ruling` 之「gate 純讀零例外面」表述(退役型態:條件觸發型翻盤;觸發條件=Codex C3 取證 mutation ownership 未定義)。外部 producer 教義與 `release_pending()` 為 consumer 之定位**不變**。

6. **Read-back**: `STATE.md`、decision record、audit sidecar、`release_pending()` 四方同 ID 驗證(AC-2)。

7. **TTL(D4)已移出本票**——不實作、不留 stub、票面不承諾;另掛後續票承接。

8. **裁決 artifacts 處置【Codex Rev E 3.3 PASS,維持】**: `meo/adjudications/` 下所有 artifacts(`.gitkeep`、`SCHEMA.md`、decision records、audit sidecars)一律 **git-tracked**,理由:裁決為永久稽核軌跡,且 record-before-clear 需 diff 可見憑證。**MUST NOT** 加入 `.gitignore`、**MUST NOT** 外部歸檔。驗收期間為滿足 AC-5 負向測試而產生之 artifacts,**MUST** 於 AC-9 逐檔名列舉(承接點=AC §6-8(ii));**其行數不計入 `MAX_LINES`**。**(Rev I 修訂,Codex Rev H F1:`MAX_LINES=332` 係對固定 range `fdd6375..e065d20` 之**實測**值,而驗收期 artifacts 產生於該 range 之後,故「固定 `DIFF_END`」與「其後新增檔併入該 range 計數」機械互斥;本票裁定 artifacts 走**逐檔列舉承接、不入行數計帳**,`MAX_LINES` 治理範圍維持「固定 range 內 tracked ALLOWED 差分」。舊條款「併入 `MAX_LINES=300` 合計」**廢除**,`300` 自現行條款全面退場,僅存於歷史修訂說明。)**
- [ ] 完成

### AI-3【M1 schema/order 已 PASS;§3 依 R3 修訂】PreToolUse 契約、分支順序與 canonical path 攔截範圍

- **檔案**: `.claude/skills/model-effort-orchestrator/hooks/meo_pretooluse_gate.sh`
- **現值**: 現行僅實作 `STATE.md` bootstrap 豁免與 diagnosis check,**尚無完整 pending gate 語義**;Rev E 之相對前綴比對規格與實際 hook 契約不符(見下)
- **目標值**:
  1. **契約(Codex M1,PASS 維持)**: 輸出 **MUST** 採現行 PreToolUse 契約——`hookSpecificOutput.hookEventName="PreToolUse"`、`hookSpecificOutput.permissionDecision`(`"allow"` | `"deny"`)、`hookSpecificOutput.permissionDecisionReason`。**MUST NOT** 使用已 deprecated 之 top-level `decision`/`reason`。
  2. **順序**: pending gate 判定 **MUST 先於** STATE bootstrap 豁免分支求值。
  3. **【Rev F/R3】攔截範圍 —— canonical absolute path 比對契約**:
     ⚠️ **現況取證**:Claude Code hook 契約之 `tool_input.file_path` 為**絕對路徑**(如 `D:/DevSources/AndroidProj/SmartCameraApp1/STATE.md`),**不**以 `STATE.md` 或 `.claude/...` 起頭。**MUST NOT** 實作相對前綴字面比對——該比對永不命中,gate 將形同虛設(官方依據:`https://code.claude.com/docs/en/hooks`)。
     - **(i) canonical root 推導**:自 `CLAUDE_PROJECT_DIR` 取得;該變數不存在時,自 hook 自身位置回推 repo 根。無法確定 → **STOP 回報**(見 Error Handling),**MUST NOT** 猜測。
     - **(ii) 正規化**:比對前將待測路徑與基準路徑之 `\` 一律轉為 `/`,收斂為單一形式後再比對。
     - **(iii) 檔案級標的採 canonical absolute **exact match**:`<root>/STATE.md`、`<root>/gate.conf`。**MUST NOT** 用 prefix 或 substring——否則 `STATE.md.bak`、`gate.conf.tmp` 將誤命中。
     - **(iv) 目錄級標的採 canonical absolute **prefix match**:`<root>/.claude/skills/model-effort-orchestrator/`、`<root>/meo/adjudications/`(prefix **MUST** 以 `/` 結尾,避免同名前綴目錄誤命中)。
     - **(v) `Bash`**:維持票面既有之 literal substring policy 比對 `tool_input.command`;但測試 **MUST** 涵蓋 Windows 絕對路徑(正斜線與反斜線兩形)。
     - 上述全部採字面/機械比對,**MUST NOT** 語義化。
     - 命中者為「MEO 治理寫入」,其餘一律 `allow`。
  4. pending=true 且 `release_pending()` 未通過 → `permissionDecision="deny"`;範圍外任務與正常 session 終止不受影響。
  5. 未授權之 `pending→false` 寫入一律 `deny`。`release_pending()` 通過時:由 **`release_pending()` 自行**完成 §C.5 五步 transition,**且對觸發本次驗證之原始 tool call 仍回 `deny`**;其他 MEO 治理寫入亦仍 `deny`。**MUST NOT** 以回 `allow` 之方式讓被攔截工具代為執行 transition。
- [ ] 完成

### AI-4 SKILL.md §11.2 authority 修正

- **檔案**: `.claude/skills/model-effort-orchestrator/SKILL.md` §11.2
- **現值**: `escalation_authority: orchestrator`(名不符實,orchestrator 僅讀旗標)
- **目標值**: `escalation_authority: external (Akira/CSO checkpoint via meo/adjudications/ record + audit sidecar, produced outside agent session)`
- [ ] 完成

### AI-5【M2/§6-6 + Fugu SSOT;依 R1/R2 增列消費點】SSOT 層級與衍生說明一致化

- **檔案**: `.claude/skills/model-effort-orchestrator/decision-tree.yaml`、`gate.conf`(`[meo]` 節)、`SKILL.md` 衍生說明
- **目標值**:
  1. **SSOT 層級不得更動**:`decision-tree.yaml` = **SSOT**(治理語義唯一真相);hook 腳本 = **implementation**;`SKILL.md`／`gate.conf` = **衍生說明**(描述性引用,不得另立平行語義來源)。
  2. `decision-tree.yaml` 之 mechanical inputs、R3/R4、pending_release、escalation history schema 四節同步至新語義(範圍見 HC-5),**含 §C.2 雙檔規格、§C.5 五步 transition 與 §C.5a mutation owner**。
  3. **【Rev F/R1】`pending_release.signal_preservation` 之 record-before-clear 契約 MUST 有明確消費點**:`decision-tree.yaml` 須字面載明 `MEO_LAST_ESCALATION_DECISION` 由 `release_pending()` 於清除 `PENDING` **之前**寫入;三層說明不得有任一處遺漏此鍵。
  4. `gate.conf` `[meo]` 節說明改寫:pending 時 Stop **不 block**、封鎖點在 PreToolUse、裁決 artifacts 為外部產生(雙檔)、release transition 由 `release_pending()` 依五步序執行。
  5. 三者與 hook 真實行為零分歧(AC-7)。
  6. **(Fugu SSOT-1)** `decision-tree.yaml` 現值列 `MEO_VERIFIER_REJECT_COUNT` 為 escalation trigger,但 `meo_signal_update.sh` 從未以其觸發——**現存 SSOT-hook 分歧**。處置:自 trigger 清單移除,或明標 `reserved`(僅記錄、非觸發)。此為 **SSOT 對齊實況**,非閾值變更,不違 HC-3;**MUST NOT** 反向修改腳本使其成為 trigger。
  7. **(Fugu SSOT-2)** `actions.request_escalation.authority` 現值 `orchestrator` → 同步為 external。
  8. ⚠️ **Rev C.1 之 AI-5 條款 5(MEO family provenance 補登)已刪除**——已由 `INFRA-GOV-DEESC-01` 完成;本票 **MUST NOT** 觸碰該檔。
- [ ] 完成

## Hard Constraints

1. **MUST NOT** 增設**第四個** Stop hook。⚠️ 現況如實記載:`.claude/settings.json` 已註冊三個 Stop hooks(`.claude/hooks/stop.sh`、`.claude/hooks/telegram_stop.sh`、`stop_orchestrator.sh`),G2「單一 orchestrator」**未在本 repo 落地**;收斂屬 TODO-3 稽核票,本票 **MUST NOT** 觸碰另兩個 hook 或 `settings.json`。
2. **hook stdout 契約**:
   - **Stop**:block 時僅三鍵(`decision`/`reason`/`systemMessage`);**允許停止時 MUST 省略 `decision` 鍵或輸出空 stdout,MUST NOT 輸出 `decision=approve`**。
   - **PreToolUse**:**MUST** 使用 `hookSpecificOutput.hookEventName` / `permissionDecision`(`allow`|`deny`)/ `permissionDecisionReason`;**MUST NOT** 使用已 deprecated 之 top-level `decision`/`reason`。
   - exit 0 為 JSON 解析前提。
3. **MUST NOT** 重置或清零 `MEO_SAME_ERROR_CLASS_STREAK`;**MUST NOT** 改動 escalation 觸發閾值數值(D1:2)與信號累加算式;**MUST NOT** 將 `MEO_VERIFIER_REJECT_COUNT` 新增為觸發器。只得增設 episode 建立條件(watermark + `PENDING=false` guard)。
4. 裁決 artifacts(decision record + audit sidecar)**MUST** 由 agent session 外部以 `mode="create"` 產生;agent **MUST NOT** 自產裁決內容、自選 `decision` 值、自填 `decision_record_audit_id`、覆寫既有 artifacts,或於 gate 中開設任何裁決檔建立例外通道。
5. **ALLOWED 語義限制範圍**: `stop_orchestrator.sh` 僅 MEO gate 分支;`gate.conf` 僅 `[meo]` 節;`SKILL.md` 僅 §11.2 及 escalation 衍生說明;**`STATE.md` 僅下列 MEO 鍵**——`MEO_ESCALATION_ID`、`MEO_WM_*`、`MEO_ESCALATION_PENDING`、**`MEO_LAST_ESCALATION_DECISION`**(Rev F 依 R1 增列),**MUST NOT** 動非 MEO 鍵;`decision-tree.yaml` 合法修改範圍為 **mechanical inputs、R3、R4、pending_release、escalation history schema 五節**;`meo_signal_update.sh` 僅 ID producer、原子鎖、watermark 邏輯;`meo_pretooluse_gate.sh` 僅 pending gate、canonical path 比對、`release_pending()` 及其五步 transition。
6. **MUST NOT** 於同一 commit 混合 `.kt/.xml` 原始碼;commit 訊息 **MUST** 使用 `[MEO-FIX-A]`。
7. **SSOT 不得翻轉**: `decision-tree.yaml` 為治理語義 SSOT;**MUST NOT** 於任何文件或腳本註解中宣告腳本為 SSOT。
8. **MUST NOT** 實作或留置 TTL stub(已移出本票);**MUST NOT** 觸碰 `.claude/rules/ticket_pattern.md`。
9. **MUST NOT** 修改 `mcp-ticket-platform` 任何原始碼以使其自動注入 audit metadata——該路徑屬跨 repo scope,須另立票,**禁於本票暗渡**(Codex Rev D R1 明示)。
10. **MUST NOT** 以任何形式放寬 §C.5 之全成全敗語義(例如「先清 pending,decision 稍後補寫」)——該放寬即重建 Codex Rev E 所指之 stale decision 缺陷。
11. **【Rev F/R4】MUST NOT** 於本票將 `mcp-ticket-platform` 接入 Claude Code session,亦 **MUST NOT** 修改 `.claude/settings.json` 之 PreToolUse matcher 以涵蓋 MCP 工具——該項屬 D5／settings scope,須另立票。
12. `Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.`

## Error Handling

- `stop_orchestrator.sh` MEO 分支無法與非 MEO 邏輯乾淨切分 → 停止回報,勿動非 MEO 行。
- PreToolUse 遷移後出現新自舉死鎖(R8/R13 族)→ 停止回報,**禁繞道 Bash 寫 `STATE.md`**。
- **canonical project root 無法自 `CLAUDE_PROJECT_DIR` 或 hook 位置確定** → 停止回報,**禁以硬編絕對路徑或猜測代替**(AI-3 §3-i)。
- **正規化後仍無法區分 repo 內外同名 `STATE.md`** → 停止回報,禁放寬為 basename 比對。
- `decision-tree.yaml` patch 錨點模糊 → 停止回報。
- ID producer 無法在 production 路徑真實產生(僅能以 fixture 直注證明)→ 停止回報,禁標 Done(Rule 9 測試驗 intent)。
- 原子鎖導致 hook 明顯延遲或死鎖 → 停止回報,禁以移除鎖規避。
- `release_pending()` 無法在不擴大放行面的前提下實作 → 停止回報,**禁以「先全面放行、日後收斂」過渡**。
- `release_pending()` 執行五步 transition 時無法沿用 §A.1 同一把鎖(例如鎖語義衝突或重入)→ 停止回報,**禁另立第二把鎖**。
- **五步 transition 無法達成全成全敗(例如 `kv_set` 無法在單一臨界區內完成多鍵寫入並終驗)** → 停止回報,**禁降級為逐鍵盡力寫入**。
- watermark 或 `PENDING` guard 改動意外影響「已裁決後抑制重播」以外之 escalation 行為 → 停止回報,禁靜默調參。
- 觸及 FORBIDDEN(含 `ticket_pattern.md`)或需改 `.claude/CLAUDE.md`／`.claude/settings.json` → 停止並走 Cross-Review Checkpoint,禁 inline。
- `claude mcp get mcp-ticket-platform` 回報**已配置** → 停止回報(AI-0.2 drift guard)。
- **過渡治理 α(機械條件,已內聯;Rev F 字面化)**:本票落地前若發生失敗,得依 α 續行——**上限 3 次**、**逐次存證**;超過 3 次 → 回 β(停止並回報 Akira)。
  - **【Rev F 字面化裁決,CSO 2026-07-25】比對域定義(補齊 Rev D/E 之懸空條款)**:α 之「error class 字面比對」其**比對兩造**為 `STATE.md` 之 **`MEO_LAST_ERROR_CLASS`** 與字面值 **`Exit`**,採**字面等值**判定。**合格 class 清單僅此一項**;**MUST NOT** 語義擴張、**MUST NOT** 納入其他 class、**MUST NOT** 以正則或子字串近似比對。
    - **取證依據(一級來源)**:兩次已動用之 git-tracked 存證行均載 `MEO_LAST_ERROR_CLASS=Exit`——第 1 次 commit `ce1cbc7` / `.claude/PROGRESS.md` S81(read-back `STREAK=15`);第 2 次 S82(read-back `STREAK=16`),序列相接,額度計數確認為 **2**。
    - **無定義之取證**:CSO 與執行者雙向 grep 全 repo 及 vault `search_decisions` 均未見比對域字面定義;`meo_signal_update.sh` L46-58 之 `ERR_CLASS` 係對錯誤訊息作無白名單自由正則抽取(取首個英文詞),codebase 從未建立「合格 class 清單」概念。故本條為**首次成文**,非收窄既有。
  - **額度狀態**:已動用 **2/3**;**第 3 次額度隨 Rev F 簽發即解凍**(Rev E 凍結條款解除,依解凍條件 (ii))。動用第 3 次時 **MUST** 逐項存證並明引本條。
  - **⚠️ 載體降級指令**:`meo-escalation-transitional-alpha` 記憶檔(2026-07-22)為 agent session 轉錄,**非權威裁決載體**,**MUST NOT** 於任何存證欄引為「裁決」或「常設裁決」;後續引據一律改引**本條**(`MEO-FIX-A` Rev F Error Handling α 條款)。此為**裁決載體白名單律**之適用:存證「引據」欄 MUST 指向耐久載體(vault 決策檔／票面／規則檔)。
  - **退役**:α scope 為「本票落地前」;Rev F 落地後 livelock 結構拆除,α 自然失效,不須另行撤銷。
- `INFRA-GOV-DEESC-01` 前置已完成——本條不再構成停止事由;惟若開工時實測 `ticket_pattern.md` 之 `MEO-` provenance 行不存在 → 停止回報(前置狀態與票面不符)。

## Acceptance Criteria(v3.1 四型:存在性／可達性／行為性／實機)

- [ ] **[行為性]** pending=true 時 Stop 不 block,單次回覆可正常結束;**連續 8 次終止測試不得重入**。(§6-1)
- [ ] **[可達性]** 每次 escalation episode 由 production producer 於原子鎖臨界區內建立唯一 `MEO_ESCALATION_ID`;`STATE.md`、`meo/adjudications/<id>.md`、`meo/adjudications/<id>.audit.md`、`release_pending()` **四者**同 ID read-back 一致;以函式符號錨回填 producer／lock／`release_pending()` 證據。(§6-2)
- [ ] **[行為性]** `PENDING=true` 期間連續注入同類失敗,`MEO_ESCALATION_ID` **不遞增**、不建立新 episode,原始訊號照常累積;`PENDING=false` 且 delta 達標才遞增並掛起;ID 缺值→0 初始化、非數字→fail-open 零寫入。(§6-3)
- [ ] **[行為性]** 已裁決且 `STREAK` 保全高於閾值時,第一筆同類失敗**不重播**(`STREAK - WM_STREAK = 1 < 2`)。(§6-3)
- [ ] **[行為性]** gate 攔截與 release 行為(§6-4,Codex C3/R1/R2):
  - **(a)** 無裁決 artifacts 時,**僅** AI-3 §3 所列標的之 `Write/Edit/MultiEdit/Bash` 呼叫被 `deny`;範圍外任務與正常 session 終止不受阻。
  - **(b)** 雙檔在位且驗證通過後,該 ID 之五步 transition 由 `release_pending()` **自行**完成(STATE read-back 佐證)。
  - **(c)** 觸發該次驗證之原始 tool call **仍被 `deny`**(hook 輸出佐證)。
  - **(d)** 其他 MEO 寫入**仍被 `deny`**;僅 (a) 檔或僅 (b) 檔存在時**不放行且 STATE 零寫入**(缺檔負向測試)。
  - **(e)【Rev F/R2】decision 一致性**:release 後 `MEO_LAST_ESCALATION_DECISION` **等於**當前 decision record 之 `decision` enum 值。**先注入不同之 stale prior decision**(如 STATE 為 `defer_and_checkpoint`、裁決檔為 `approve_upgrade`),驗證 release 後**被當前外部裁決取代**,非殘留舊值。
  - **(f)【Rev F/R2】失敗零寫入**:`decision` 值 invalid 或必填欄缺失時,STATE **三鍵全部零寫入**(`MEO_LAST_ESCALATION_DECISION`、`MEO_WM_*`、`MEO_ESCALATION_PENDING` 均不變)。
- [ ] **[可達性]【Rev F/R3】canonical absolute path guard 可達性**:以實際 hook 執行驗證下列四組,並回填 `檔案:行號` producer 證據:
  - **正向①** 絕對正斜線路徑(`<root>/STATE.md`)命中 → `deny`;
  - **正向②** 絕對反斜線路徑(`<root>\STATE.md`)經正規化後命中 → `deny`;
  - **負向③** repo **外**同名 `STATE.md`(如 `C:/tmp/STATE.md`)**不**命中 → `allow`;
  - **負向④** `STATE.md.bak`、`gate.conf.tmp` **不**命中 → `allow`(佐證採 exact match 而非 prefix)。
- [ ] **[行為性]** PreToolUse 輸出經實際 hook 執行驗證為 `hookSpecificOutput.permissionDecision` 契約,非 top-level `decision`;pending gate 求值**先於** STATE bootstrap 豁免;gate 內**不存在**裁決檔建立或改寫分支,且除 §C.5 受鎖 transition 外**零寫入**(程式碼檢視佐證)。(§6-5)
- [ ] **[存在性]** `decision-tree.yaml`(SSOT)、`gate.conf`、`SKILL.md` 三層說明與 hook 真實語意一致;**`MEO_LAST_ESCALATION_DECISION` 於三層均有明確消費點且載明 record-before-clear 順序**(Rev F/R1);`MEO_VERIFIER_REJECT_COUNT` 不再被列為 trigger(或明標 `reserved`);`request_escalation.authority` 為 external;無任何處宣告腳本為 SSOT;`SCHEMA.md` 含雙檔規格、五步 transition、§C.8 處置規則與「通道一致性非防偽」誠實聲明。(§6-6)
- [ ] **[存在性]** `./gradlew assembleDebug` PASS。(§6-7)
- [ ] **[存在性]** `git diff --stat <DIFF_REF>` 範圍核對,**三項分列**(§6-8):
  - **(i) ALLOWED path entries = 8**(含 `STATE.md`、`meo/adjudications/*`),此為授權範圍計數,**非** tracked 計數;
  - **(ii) post-change git-tracked files = 8**:既有 6 項 + 新增 `meo/adjudications/.gitkeep` + `meo/adjudications/SCHEMA.md`;**另加**驗收期產生之裁決 artifacts,**MUST 逐檔名列舉**(§C.8);
  - **(iii)【Rev I 改制:現況結構性稽核 + 顯式全鍵白名單】** `STATE.md` 為 **gitignored,機械上不可見於 git diff**。
    - **(A) 事實層誠實裁決**:AI-0 所記 baseline SHA-256 **實值不存在於任何耐久載體**(Codex Rev H F2 唯讀窮盡查核 + CSO 覆核),**歷史 delta 機械不可重建**。本 AC **MUST NOT** 宣稱、亦 **MUST NOT** 要求以 baseline SHA-256 證明「已變更」事實;**MUST NOT** 以現行 SHA 回填 before-state;**MUST NOT** 以推測補值。此為 Rev H「以 baseline SHA 證明變更事實」宣稱之**誠實降格**。
    - **(B) 現況結構性稽核(機械可判,取代一切差分/行數佐證)**:對**現行** `STATE.md` 逐行核驗,每一行 **MUST** 屬下列三型之一,否則 **FAIL**——①`#` 起首行(含 `##` 節標頭與 `#` 註記行);②空行;③`^<KEY>=` 形式之鍵行且 `<KEY>` ∈ 下列**顯式 16 鍵集合**。判準式(零命中方為 PASS):`grep -nvE '^(#|$|MEO_(LAST_DIAGNOSIS_TS|LAST_TASK_TYPE|LAST_FAILURE_MODE|RETRY_COUNT|SAME_ERROR_CLASS_STREAK|VERIFIER_REJECT_COUNT|LAST_ERROR_CLASS|QUOTA_STATE|ESCALATION_PENDING|LAST_ESCALATION_DECISION|FASTPATH|ESCALATION_ID|WM_RETRY|WM_STREAK|WM_ERROR_CLASS|WM_REJECT)=)' STATE.md`。**16 鍵集合之來源(Rev J 拆分敘述)**=**13 個 live hook keys**(`meo_signal_update.sh` 與 `meo_pretooluse_gate.sh` 之 `kv_set`／`kv_set_f` 寫入鍵聯集,加兩 hook 之唯讀消費鍵)**+ 3 個 SSOT/schema keys**(`MEO_LAST_TASK_TYPE`/`MEO_LAST_FAILURE_MODE`/`MEO_QUOTA_STATE`——兩 hook 零引用;authority=`decision-tree.yaml` L45/115/131 之 `MEO_QUOTA_STATE` 枚舉定義與 trigger 消費、`SKILL.md` L208–214 之 STATE schema 模板;producer=執行者 agent 診斷流程經 R9 自舉豁免通道)(CSO 2026-07-26 碼源+SSOT grep 雙檔取證);**擴充本集合須經確認決議**,**MUST NOT** 於驗收期自行增列以求綠。
    - **(C) 註記行分類與定界【Rev J 修訂:分類域=`#` 註記行 only】**:本項分類域**嚴格限於 `#` 起首之註記行**;全部 16 合法鍵行由 (B) 文法**完整承接,不入本項分類**(Codex Rev I F1 更正:Rev I 原「通過 (B) 之各行歸入三類」對 9 個合法非 HC-5 鍵行無類可歸,屬分類域裂隙)。`#` 註記行 MUST 逐塊歸入下列 (2)(3) 二類並列舉取證——**(1)【Rev J 改制:權限聲明,非行分類】** HC-5 四鍵(`MEO_ESCALATION_ID`/`MEO_WM_*`/`MEO_ESCALATION_PENDING`/`MEO_LAST_ESCALATION_DECISION`)為本票實作**唯一授權 mutation 標的**(producer=`release_pending()` 五步 transition 與 signal hook);其行為性驗證由 AC §6-3/§6-4 fixture 承擔;其餘 12 鍵本票不得修改——惟「值是否曾變更」於 current-state 稽核**機械不可判**(無 baseline,依 (A)),本聲明為權限邊界記載,**MUST NOT** 作為結構稽核之 PASS/FAIL 判項;**(2)** 2026-07-25 Akira out-of-band 遷移區塊,**字面定界**=起始錨 `# Legacy pending migration (2026-07-25,` 起、至終止錨 `K4-cand-20260725-01 registered.` 止(含首尾兩錨),該區塊**內容凍結**:區塊內 **MUST NOT** 出現任何 `<KEY>=` 形式行,且驗收期 **MUST NOT** 增修一字(佐證=區塊原文 + `.claude/PROGRESS.md` 項 20);**(3)** 其餘 `#` 註記區塊,producer=**執行者 agent 經 PreToolUse gate v1.2(CSO R9)之 `STATE.md` 自舉豁免分支**寫入。
      > **Rev I 事實更正**:Rev H 稱第 (3) 類為「hook 自動代謝註記」係 producer 誤植。碼源取證確認兩 MEO hook 對 `STATE.md` 之全部寫入僅經 `kv_set`／`kv_set_f` 之純 `KEY=value`(`sed` 就地替換或 `printf` 追加),**從不產生 `#` 註記行**;故第 (3) 類非 hook 產物,machine-producer anchor 於此不存在,判準改依 (B) 之行文法。
    - **(D) 誠實揭露(非機械範圍)**:第 (3) 類之**內容語意**適格性(是否確為診斷／覆核記載)**非**鍵級機械可判,屬**人工結構審查**;`/qa` 報告 **MUST** 明文標記本項為人工審查結論,**MUST NOT** 宣稱 (C) 各類均達鍵級機械白名單(Rev J 措辭同步:原「三類」結構已改制)。機械保證之範圍嚴格限於:(B) 三型行文法零違例 + (C)(2) 區塊之字面錨定界與凍結。
    - **(E)** **MUST NOT** 以「git diff 無顯示」推定 `STATE.md` 未被越權修改;`STATE.md` 行數**不計入** `MAX_LINES`(其治理範圍=固定 range 內 tracked ALLOWED)。
  - **(iv)【Rev H】MAX_LINES binding 證據(glob-aware 顯式路徑)**:行數核對之**唯一** binding 證據=以**顯式 8 檔路徑**執行 `git diff --numstat fdd6375 e065d20 -- <逐檔列名 8 個 tracked ALLOWED 檔>` 之逐檔加總(預期 added=301/deleted=31/合計 332);verifier Step 0 因 wildcard 不入行數計算器所示之 `+236` 為**已知漏算值,MUST NOT 充當 MAX_LINES 證據**,`/qa` 報告 MUST 明文註記此標記。
  - 全部變更無夾帶,tracked ALLOWED added+deleted 合計不超過 `MAX_LINES=332`。

⚠️ DEFENSIVE WARNING: 本票唯一目的是拆除 livelock 結構。任何「順手」調整 escalation 閾值、清零信號、為便利在 gate 開設裁決檔建立通道、以回 `allow` 讓被攔截工具代執行 transition、將五步 transition 降級為盡力寫入、或以「先全面放行日後收斂」過渡 `release_pending()` 之行為,一律 STOP——那將重建本票所要消除的根因。

---
## 冊面收束註記(2026-08-01,MEO-DECOMM-01 Step E)
本票 lifecycle=CLOSED 之語意為 **STOPPED/superseded**(前提消滅型退役),非驗收完成。MEO v1 執法層已依 MEO-DECOMM-01 退役(hooks 解武 0b321c2/產物歸檔 efb3c3f),修復對象消滅。Rev K 回修週期就此終止。裁決鏈=vault `2026-08-01_MEO_V1_Enforcement_Layer_Decommission.md`。
