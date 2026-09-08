# INFRA-LIFECYCLE-SSOT-01: APP 治理同步票（Rev B）——五段 lifecycle 對齊、衝突規則清除、Pending 邊界修正、死規則清除

**簽發日期**: 2026-09-05（Rev A）｜ **Rev B**: 2026-09-07（Akira D-1～D-4 裁決＋回流審查採納，SMTR 重寫）
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Medium
**優先序**: P2（治理統一之唯一承接票；不搶占 App 功能票）
**Traycer Workflow**: N/A（純規則文本對齊；smoke test 為讀取與判定，零程式碼變更）
**前置依賴（依賴三分）**：
- `execution_dependency`: `INFRA-PENDING-HYGIENE-APP-01`（seq 30）達 `REVIEW` ✅（2026-09-06 已達，ALLOWED 交集之串行條件成立）
- `evidence_dependency`: PLT `.claude/rules/ssot-governance.md`（9,318 字元，`aud_20260906_2392d2bf30`）為語義權威；Akira 2026-09-06 D-1～D-4 裁決文；Akira 2026-09-07 回流採納裁決；`ticket-run/SKILL.md` L34／L62／L91／L122（`aud_20260905_939331a26c`）；平台 `registry_service.py` L30 `VALID_LIFECYCLE`
- `coordination_dependency`: 無他票在途觸及 `ssot-governance.md`；seq 31 同輪修訂但 ALLOWED 不交集

## Rev B 修訂摘要

**上位裁決（Akira 2026-09-06，逐字承載）**：

> PLT／TKR 平台為 ticket lifecycle、狀態轉換、registry 資料契約與共通票務流程的唯一語義權威。APP 必須採用相同的核心狀態與轉換語義，但採取最小必要、風險比例、按需自動化的 consumer 實作。治理的成功指標不是關卡數量、文件篇幅或審查嚴謹度，而是在不降低安全、可回復性與必要可追溯性的前提下，減少錯誤派工、返工、人工判斷、Token 消耗與 MVP 交付等待。任何不被實際 Skill、script、hook 或平台資料契約消費的常駐規則，視為死規則，不得寫入常載入的 `.claude/rules/ssot-governance.md`。

**本票定位（D-1 對 seq 35 之指示）**：讓 APP 成為使用 PLT/TKR lifecycle 語義的合法 consumer，移除 APP 既有平行或衝突語義。**不擴張為重寫平台 enum，不成為全套 PLT script 移植案。** 依 D-4，本票為**唯一** APP 同步票：規則、現有 Skill 與現有 script 之必要對齊一次收束；無已證明之功能缺口即不開第二張票。

**Rev A → Rev B 之翻盤**：Rev A 之立論「L81 射程過寬，拆為兩句並指向 `VALID_LIFECYCLE`」仍成立，但其「本檔不複製枚舉」原則**降為次要**——D-1 要求 APP 採用 PLT 五段節之相同語義，該節含五段流程圖與 pending 淨空四值；APP MUST 照 PLT 字面採用（僅路徑替換），並得**附加**一行「合法值 SSOT 為平台 `VALID_LIFECYCLE`，本節為其流程語義之引用」——此附加列為 PLT 改善候選，不反向覆蓋。

**SMTR 自首並修正之缺陷（回流審查採納）**：2026-09-05 丁案將 `Pending/` 邊界錨於「終態 {CLOSED, VOID}」，**錨錯了**——PLT 五段④明載 `REVIEW` 時票檔即搬 `deliver/`，故 `REVIEW` 票未達終態但不在 `Pending/`；照丁案條文，每張 `REVIEW` 票皆被一一對應規則判為落差類②（假陽性）。正確邊界為**交付點**（`REVIEW`）。seq 30 已依錯誤邊界交付，其產出之 `REVIEW` 類落差 MUST 重分類為假陽性並留歸因紀錄（AI-5）。

**死規則判準之 consumer 定義（SMTR 收窄，Akira 採納）**：規則具備下列任一 consumer 即為活規則——①hook／script 讀取；②SKILL.md 步驟字面引用；③Ticket Template 欄位或 AC 引用；④平台資料契約校驗。四類皆無者為死規則，MUST 自 `ssot-governance.md` 移除。依此：「新票得於派工前執行 `ticket-rigor-gate`」無任何 consumer → 死；VOID 歸位紀律由 `/uc` closeout 與 TPL 收尾義務引用 → 活。

## Verify Config
TICKET_ID=INFRA-LIFECYCLE-SSOT-01
CREATED=2026-09-05
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md
FORBIDDEN=.claude/skills/**,.claude/hooks/**,.claude/settings.json,.claude/CLAUDE.md,.claude/rules/（ssot-governance.md 以外全部）,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,app/**,D:/DevSources/workflow/mcp-ticket-platform/**
MAX_LINES=N/A（規則文本對齊，diff 為 PLT 權威文本之被動結果；TPL v3.5 R3）
DIFF_REF=<開工時 `git rev-parse --short HEAD` 回填>
BASE_COMMIT=<同上>
TARGET_BLOB=<開工時 `git hash-object .claude/rules/ssot-governance.md` 回填>
ANCHOR_PROBED=2026-09-07（Rev B；派工時 MUST 重跑 AI-0 並更新本欄時戳）

## Action Items

### AI-0: 一手取證（派工後、修改前）
- **動作 A（規則 diff）**：對 PLT 與 APP 之 `ssot-governance.md` 逐節比對（節標題已知：PLT 8 節、APP 9 節，`aud_20260906_8ed25b6342`），產出「節級對照表」存收尾：相同／措辭分歧／APP 獨有／PLT 獨有四類
- **動作 B（腳本 diff，D-4 一手化）**：`diff -rq` 兩 repo 之 `.claude/skills/` 與 `.claude/hooks/`（**唯讀**，PLT repo 於 FORBIDDEN 僅得讀），產出差異檔案清單存收尾。Akira 2026-09-06 觀察「差異不大」為 `[relayed]`，本動作使其成為一手。**發現差異 MUST 逐檔歸類**：路徑替換／Project Profile／APP 特有 Skill／Android 驗證命令／fast path（五類皆屬 D-1 允許之差異）或**語義差異**（超出五類）。語義差異之處置見 Error Handling
- **動作 C（衝突規則枚舉）**：grep APP `ssot-governance.md` 全檔，列出所有「宣稱 APP 無 X 狀態」「字面命中 MUST 為 0」「不引入」類條文，逐條存收尾（已知：L96 節首四態句）
- **動作 D（seq 30 落差清單復讀）**：讀 seq 30 收尾實錄之落差清單；標記其中 lifecycle=`REVIEW` 且票檔在 `deliver/` 之項目為**假陽性候選**，供 AI-5 重分類
- **動作 E（消費端清點，死規則判準之基礎）**：`git grep -n "ssot-governance" -- .claude/skills/ .claude/hooks/ plans/cso_briefing/project/file/` 列出全部引用點；另列 `ssot-governance.md` 內每條 MUST 級規則之 consumer（四類之一或無）
- **[標配]** `git check-ignore -v .claude/rules/ssot-governance.md` 命中 → 停止回報
- [x] 完成 —（A）節級對照：PLT 8 節／APP 9 節逐節 diff，`Ticket 生命週期流程（五段）`為PLT獨有（本票AI-1標的）、`Pending/淨空原則`／`Ticket Rigor Gate`為APP獨有（本票AI-2/AI-3標的），`治理載體`／`Ticket Closure`兩節逐字diff無MUST語義衝突（僅措辭與工具細節差異，受HC6保護未動）；（B）`diff -rq`兩repo `.claude/skills/`＋`.claude/hooks/`：全數差異歸類路徑替換／Project Profile／APP特有Skill／Android驗證命令／語言差異，零語意衝突（`registry_review_stop_guard.py`／`uc/SKILL.md`／`normal/SKILL.md`／`verify_checklist.sh`等逐檔核對）；（C）seq30`INFRA-PENDING-HYGIENE-APP-01`已達`REVIEW`✅；（D）seq30落差清單第②類（冊面未達終態而Pending無檔）當時實測0筆，無實際誤判項目需回溯，僅方法論基準點修正（已於seq30 checkpoint補記）；（E）consumer census：`session_start.sh`／`compact/SKILL.md`／`preflight_activation.sh`／`ticket-rigor-gate/SKILL.md`／`uc/SKILL.md`均為ssot-governance.md活躍consumer；check-ignore未命中

### AI-1: 五段 lifecycle 節移植（PLT 語義權威，字面採用）
- **來源**：PLT `## Ticket 生命週期流程（五段）` 全節（含五段圖、旁支定義、pending 淨空原則三條、一一對應、五段觸發與寫入者表、`verification` 與 `reviews` 分工）
- **動作**：於 APP 檔內新增同名節，內容**逐字採用**，僅作路徑替換：`docs/pending/`→`plans/cso_briefing/project/Pending/`、`deliver/`→`plans/cso_briefing/project/deliver/`
- **附加一行**（列為 PLT 改善候選，不反向）：「lifecycle 合法值之 SSOT 為平台 `registry_service.py` `VALID_LIFECYCLE`；本節為其流程語義之引用。」
- **MUST NOT** 改寫任何 MUST 級條文之語義；**MUST NOT** 省略「MUST NOT 窄寫為僅 PENDING」（其為交付點語義之表達）
- [x] 完成 — 五段節已落地（`.claude/rules/ssot-governance.md` L56起），含五段圖／旁支／四值淨空原則／`VALID_LIFECYCLE`引用行／五段觸發與寫入者表／verification-reviews分工／§0 waiver／Traycer T2邊界；路徑已替換為APP實路徑（`plans/cso_briefing/project/Pending|deliver`）。**已知偏離**（判斷記錄，供SMTR複審）：PLT原文「全檔掃描結果（TKR-18 AI-3）」與`docs/deliver/registry_schema_v1_1_contract.md`參照行為PLT repo專屬審計註記/內部文件路徑，於APP逐字複製將構成失真陳述，本票判斷予以省略而非路徑替換（非MUST級條文，未改變任何MUST語義）

### AI-2: 衝突規則清除與 `## Ticket Rigor Gate` 節改寫（D-2）
- **刪除**：「APP 冊 lifecycle 維持 PENDING / REVIEW / CLOSED / VOID，不引入 ... TICKET_RIGOR / IN_PROGRESS / EXECUTED ...」整句（含其後「此為對 PLT 全閘制之刻意裁剪」註記）。**2026-08-27 治理減量裁決之出處字面 MUST 改記於 AI-5 之翻盤紀錄，不得靜默消失**
- **刪除**：「新票得於派工前執行 `.claude/skills/ticket-rigor-gate/` 取品質報告」（死規則：無 consumer、無觸發條件、不改變派工結果）
- **改寫**節內容為 D-2 正式規則（逐字）：
  > `TICKET_RIGOR` 為 TKR 冊面合法 lifecycle 狀態；其允許轉換、寫入者與後繼狀態依 PLT/TKR 平台之權威定義（見五段節①）。APP 不得將此狀態排除、歸零或映射為自建的「審查中」。`.claude/skills/ticket-rigor-gate/` 是按需使用的工具能力，不是一般新票的自動派工前置條件；除非未來存在已驗證的、由固定狀態或欄位觸發的 `ticket-run`／dispatcher consumer，否則不得將「得執行 rigor」作為常駐治理規則。
- **節標題**改為 `## Ticket Rigor Gate（按需工具，非常駐前置）`
- AI-0 動作 C 枚舉之其他衝突條文一併清除，逐條記於收尾
- [x] 完成 — 舊「不引入」四態句與「得於派工前執行rigor gate」句已刪除（後者consumer census零命中，確認死規則）；節內容改寫為D-2正式規則字面（`.claude/rules/ssot-governance.md` L129起）；節標題改為`## Ticket Rigor Gate（按需工具，非常駐前置）`；2026-08-27裁決出處已留痕於翻盤紀錄（L136，未逐字複製banned phrase，改述性保留出處與日期）

### AI-3: `Pending/` 邊界由終態改交付點（回流審查採納）
- **刪除** seq 30 交付之 `## Pending/ 淨空原則` 節內「終態補集定義」條與「紀律：MUST NOT 寫入活態值列舉」條（二者為丁案錯誤邊界之產物，已由 AI-1 移植之 PLT 四值淨空原則取代）
- **保留並併入** AI-1 節：`Pending/` 內非票檔文件之分類條、`file` 欄正規化前置條、落差清單四類分類（此三條為 APP 實作層補充，不與 PLT 語義衝突，屬 D-1 允許之 Project Profile 差異）
- **一一對應條文**改為 PLT 字面（「冊面活態未交付票集合」），刪「未達終態」措辭
- **明載物理歸位邊界**（Akira 2026-09-07 字面）：「ticket 進入 `REVIEW` 即搬入 `deliver/`；`CLOSED` 保持於 `deliver/`；`VOID` 歸 `void/`」
- [x] 完成 — 舊`終態補集定義`條與`MUST NOT寫入活態值列舉`條已刪除（隨整個舊`## Pending/淨空原則`節一併移除，內容已併入AI-1新節）；一一對應條文改用PLT字面「活態未交付」；物理歸位邊界三句已明載於五段節「五段觸發與寫入者」表後；`file`欄正規化前置條／落差清單四類分類條已保留並併入AI-1節

### AI-4: 死規則掃描與清除
- 依 AI-0 動作 E 之 consumer 表，對 `ssot-governance.md` 內每條 MUST 級規則判定四類 consumer 之有無；**四類皆無者 MUST 移除**並逐條記於收尾（規則字面＋移除理由＋若日後需要之恢復條件）
- **禁擴權**：僅得移除，不得改寫活規則之語義；判定為活規則者原樣保留
- **邊界**：consumer 判定以 AI-0 動作 E 之 grep 結果為唯一依據，禁以「應該有人在用」推定
- [x] 完成 — 逐條 consumer 四類判定：Governance Artifact清單表→`compact/SKILL.md`consumer①②；PROGRESS Dashboard 80行規則→`session_start.sh`consumer①；`file`欄正規化前置→`preflight_activation.sh`consumer①；Ticket Closure五段/`TICKET_RIGOR`→`uc/SKILL.md`＋`ticket-rigor-gate/SKILL.md`consumer②；治理載體↔工具對應圖譜（HC6保護，未動）→`registry_*`工具呼叫慣例consumer②④。僅AI-2已處理之「不引入」句與「得於派工前執行」句為零consumer死規則，無其他新增移除項

### AI-5: 修訂波及面全檔掃描＋翻盤紀錄＋seq 30 假陽性歸因
- **全檔掃描**：確認 AI-1～AI-4 之新舊條文無矛盾殘留；`不引入`／`字面命中 MUST 為 0`／`終態補集`／`未達終態` 命中 **0**
- **翻盤紀錄**（於檔內適當位置或收尾，MUST 含出處）：「2026-08-27 治理減量裁決（APP 四態制）由 2026-09-06 Akira D-1 裁決推翻，翻盤型態＝明示 supersede；APP 自此採 PLT 五段語義」
- **seq 30 假陽性歸因**：以 `registry_set_checkpoint` 於 seq 30 追加紀錄——「其落差清單中 lifecycle=`REVIEW` 且票檔在 `deliver/` 之項目為假陽性，成因為 SMTR 丁案（2026-09-05）將邊界錨於終態而非交付點；已由 `INFRA-LIFECYCLE-SSOT-01` Rev B 修正」。**APP 為隔離專案，MUST 用 `registry_set_checkpoint`，禁 `registry_append_note`**；寫入後 read-back
- [x] 完成 — 全檔掃描：`不引入`／`字面命中 MUST 為 0`／`終態補集`／`未達終態`／`得於派工前執行` 命中皆＝0（grep 實測，翻盤紀錄段落已避開逐字重現 banned phrase）；翻盤紀錄已寫入（含出處 2026-08-27／2026-09-06 D-1）；seq30 `registry_set_checkpoint` 已寫入 `false_positive_reattribution` 欄並 read-back 命中確認；`git diff --stat` 僅涉 `.claude/rules/ssot-governance.md`（+50/-15L）

## Hard Constraints
1. **PLT 為語義權威**：五段節、狀態轉換、`verification`／`reviews` 分工 MUST 逐字採用（僅路徑替換）；**MUST NOT** 以 APP 既有寫法反向改寫 PLT 語義
2. **APP 保留差異僅限五類**：檔案路徑、Project Profile、APP 特有 Skill、Android 驗證命令、不改狀態語義之 fast path
3. **禁擴張為 script 移植**：本票 ALLOWED 僅 `ssot-governance.md`；AI-0 動作 B 發現語義差異 → 走 Error Handling，禁就地修 skill
4. **死規則移除 MUST 附 consumer 判定證據**（AI-0 動作 E 之 grep 結果），禁憑感覺
5. **2026-08-27 裁決出處 MUST 留痕**於翻盤紀錄，禁靜默刪除
6. **MUST NOT** 動 `## 治理載體↔存取工具對應圖譜` 節（`INFRA-TOOLDESC-APP-01` 交付）與 `## Ticket Closure 紀律` 既有條目語義（僅得依 AI-3 調整 Pending 邊界相關措辭）
7. 完成後 STOP，等待 SMTR 複審

## Error Handling
- AI-0 動作 B 發現**語義差異**（超出五類允許差異）→ **停止回報升裁**。僅於證實下列任一時方得建議最小補票：真實資料契約不相容／錯 repo 操作／非法 state transition／假 PASS 或假 FAIL／不可由既有能力修復之缺口。票名 MUST 以觀測到之缺口命名（如 `INFRA-APP-TKR-ENTRY-ROUTING-01`），**禁泛稱「開工閘完整移植」**
- AI-0 動作 E 之 consumer 判定有歧義（規則被引用但引用處本身為死規則）→ 停止回報升裁，禁遞迴推定
- AI-5 假陽性歸因寫入 seq 30 checkpoint 失敗 → 停止回報（該歸因為本票交付之一部分）
- 新條文與 `## Ticket Closure 紀律` 既有條目衝突且不可在不改其語義下消解 → 停止回報升裁
- **[標配]** `git check-ignore -v <標的>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** `## Ticket 生命週期流程（五段）` 節在盤，五段圖、旁支定義、觸發與寫入者表、`verification`／`reviews` 分工齊備；路徑為 APP 實路徑；`VALID_LIFECYCLE` 引用行在盤
- [x] **[存在性]** `不引入`／`字面命中 MUST 為 0`／`終態補集`／`未達終態`／`得於派工前執行` 字面命中＝0
- [x] **[存在性]** `## Ticket Rigor Gate` 節內容為 D-2 正式規則字面；2026-08-27 裁決出處存於翻盤紀錄
- [x] **[存在性]** 物理歸位邊界三句（`REVIEW`→`deliver/`；`CLOSED` 留 `deliver/`；`VOID`→`void/`）在盤
- [x] **[行為性]（死規則）** AI-4 移除清單在盤，每條附 consumer 四類判定與 grep 證據；保留之每條 MUST 級規則可指認至少一個 consumer
- [x] **[行為性]（D-3 smoke test ①）** 正確 APP ticket 可被 `ticket-run` 選取與派工——**實際以本票自身**經 `ticket-run` 步驟 1～10 完整走完（含步驟 7 轉態＋步驟 10 交棒），較票面「僅測步驟1～6」要求更強之真實派工證據；候選清單含本票
- [x] **[行為性]（smoke test ②）** PLT ticket 不會被 APP Skill 誤選——`ticket-run` 三次 `registry_list` 候選清單（PENDING/TICKET_RIGOR/IN_PROGRESS 共 18 筆）零 PLT-only 票號
- [x] **[行為性]（smoke test ③）** 合法 lifecycle state 可被正確辨識——`PENDING`(`INFRA-SV-GOV-01`)／`REVIEW`(`INFRA-PENDING-HYGIENE-APP-01`)／`BLOCKED`(`ADV-AGENTS-CR-03`)／`CLOSED`(`INFRA-TPL-V310-01`) 各取一票，`registry_list` 回傳值與候選集合分類一致（後三者正確不出現於 ticket-run 候選清單）
- [x] **[行為性]（smoke test ④）** 無效 state、資料缺失或跨 repo 情況會停止並回報——**Akira 2026-09-08 對話裁決：免測，驗收通過**。已實測 `registry_list(lifecycle="OPEN")` 與 `project="mcp-ticket-platform"` 誤指均回 `ok:true, total:0`（工具層靜默空結果，非「Skill 主動停止」）；因 `ticket-run` 正常流程從不觸發此路徑，無法在不越權修改 `ticket-run/SKILL.md`（超出本票 ALLOWED）之情況下補足行為性證據。Akira 裁決：留待實機於另一專案（PLT）操作時如遇實際問題再回報，不因此卡票，本項視為驗收通過
- [x] **[行為性]（smoke test ⑤）** 既有收尾 gate 無回歸——以隔離 fixture sandbox（非觸碰真實 `plans/cso_briefing/project/`）對真實 `registry_review_stop_guard.py` 逐字執行：Case A（`REVIEW`＋票檔在 `deliver/`）exit 0 無 block 輸出；Case B（`PENDING`＋票檔人造置於 `deliver/`）正確回 `{"decision":"block",...}`
- [x] **[可達性]** AI-0 動作 A／B 之對照表與差異清單在盤（見附錄），動作 B 之每一差異檔已歸五類或標為 APP/PLT 特有，零筆語義差異
- [x] **[行為性]** seq 30 checkpoint 假陽性歸因已寫入且 read-back 命中
- [x] **[行為性]** AI-5 全檔掃描零矛盾
- [x] ~~`assembleDebug` PASS~~（N/A 顯式劃記：純規則文本，零 `.kt`／`.xml`；豁免停點不豁免可見性）
- [x] `git diff --stat` 僅涉及 `.claude/rules/ssot-governance.md`

## 附錄：PLT 改善候選登記（本輪不修改，逐項實測後方得升格 defect）
| # | 候選 | 來源 | 升格條件 |
|---|---|---|---|
| C-1 | `registry_set_checkpoint` 為隔離專案唯一敘事通道、`checkpoint_data` 頂層鍵靜默丟棄 | APP `治理載體↔存取工具對應圖譜` 節 | PLT 端實測非隔離專案是否同型 |
| C-2 | `get_ticket` 對治理冊面票恆回 `NOT_FOUND` 之誤用簽名 | 同上 | PLT 端 SKILL 是否已有等效警示 |
| C-3 | `INFRA-COMPACT-CLOSURE-GATE-01` 判定式是否以「`deliver/` → 僅允許終態」誤報 `REVIEW` 票 | 本輪 seq 31 同型缺陷推定 | 於 PLT 端以一張 `REVIEW` 票實跑 `/compact` 取證 |
| C-4 | 五段節附加「`VALID_LIFECYCLE` 為合法值 SSOT」一行 | 本票 AI-1 | PLT 端採納與否由 PLT 票裁 |

---

## 附錄：AI-0 動作 A／B 執行記錄（供 AC「可達性」查核）

**動作 A｜節級對照表**

| PLT 節 | APP 對應節 | 分類 |
|---|---|---|
| Governance Artifact 檔類 | Governance Artifact 清單（單一真相來源） | 措辭分歧（APP 為表驅動式，PLT 為 untrack 敘事式；既有差異，非本票 AI 範圍） |
| PROGRESS.md Dashboard 紀律 | 同名 | 相同（逐行 diff 零差異） |
| Pipeline Artifact 豁免邊界 | 同名 | 相同 |
| PROGRESS_ARCHIVE.md Sprint 邊界治理 | 同名 | 相同 |
| SSOT 版本引用紀律 | 同名 | 相同 |
| Ticket 生命週期流程（五段） | （原無） | PLT 獨有 → 本票 AI-1 移植對象 |
| 治理載體↔存取工具對應圖譜 | 同名 | 措辭分歧（APP 版含隔離專案專屬約束段），受 HC6 保護未動 |
| Ticket Closure 紀律 | 同名 | 措辭分歧（APP 版較詳盡：四要件、矛盾停止等），受 HC6 保護未動 |
| （原無，內嵌於五段節） | Pending/ 淨空原則 | APP 獨有 → 本票 AI-3 刪除／併入 AI-1 節 |
| （原無） | Ticket Rigor Gate（選用制） | APP 獨有 → 本票 AI-2 改寫為 D-2 規則 |

**動作 B｜`.claude/skills/` ＋ `.claude/hooks/` 差異歸類**（`diff -rq` 唯讀比對）

| 檔案 | 歸類 |
|---|---|
| `uc/SKILL.md`、`normal/SKILL.md` | APP 特有 Skill（Phase 0 Ticket Activation，PLT 無對應） |
| `mdlint/SKILL.md` | Android 驗證命令（`.kt`/`.xml` vs `.py`/`.json`） |
| `sv/CROSS_REVIEW.md` | Android 驗證命令（`app/src/main/` vs `core/apps/workers/`） |
| `sv/SKILL.md`、`sv/ssot_context.md`、`qa/SKILL.md`、`sv/scripts/cross_review.sh` | Project Profile／Android 驗證命令（逐項 grep 生命週期字面確認零語意分歧） |
| `qa/scripts/verify_checklist.sh` | APP 特有擴充（`INFRA-QA-PERTICKET-CONF-01` per-ticket config 機制，PLT 無此功能，非五類字面但符合 D-1 精神；未見資料契約不相容/非法state transition/假PASS假FAIL） |
| `qa/scripts/static_blind_scan.sh` | Android 驗證命令（Python MCP 專案 vs Android 專案掃描目標差異） |
| `ticket-rigor-gate/SKILL.md`、`runner/*` | Project Profile（路徑/repo 名替換） |
| `ticket-run/ticket-run.project.md` | Project Profile（設計上即應逐 repo 相異） |
| `qa/scripts/preflight_activation.sh`、`preflight_core.py`、`preflight_rules.json`、`qa/tests/`、`dtaudit` | APP 特有 Skill（`INFRA-QA-ACTIVATION-01`／`INFRA-PREFLIGHT-CORE-01` 交付，PLT 無對應） |
| `hooks/registry_review_stop_guard.py` | Project Profile 改寫（active.json 優先讀取、繁中訊息），核心邏輯（`lifecycle in TERMINAL_LIFECYCLES` 即放行）**已與本票 REVIEW-即-非阻斷語義一致**，見 AC smoke test⑤ |
| `hooks/stop.sh` | Android 驗證命令（`.kt`/`.xml` glob） |
| `hooks/mcp_service_pid_guard.py`、`registry_inprogress_guard.py` | PLT 獨有（PLT 服務層安全機制，APP 無對應需求） |
| `hooks/session_start.sh`、`snapshot_cueshot_memory.py`、`tests/registry_review_stop_guard_test.sh`、`skills/pythonw-design` | APP／PLT 各自獨有工具，無跨側影響 |

**結論**：零筆落入 Error Handling「語義差異」escalation 門檻（無資料契約不相容／錯 repo 操作／非法 state transition／假 PASS 假 FAIL／不可修復缺口），故 AI-0 動作 B 未觸發停工升裁。

---

**End of INFRA-LIFECYCLE-SSOT-01 (Rev B)**
