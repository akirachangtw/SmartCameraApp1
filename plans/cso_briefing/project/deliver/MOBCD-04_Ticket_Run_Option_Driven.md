# MOBCD-04: ticket-run 全互動選項化，新增步驟 10 交棒選單與 conf 同步，並以定位擷取取代窗口讀取

**簽發日期**: 2026-09-02
**簽發者**: Claude (SKAD)
**來源議題**: MOBCD
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: `MOBCD-03`（APP 側 `.claude/skills/ticket-run/SKILL.md` 已落地）
- `evidence_dependency`: `docs/rigor-gate/RGR_MOBCD-02_20260901.md`（PLT，Major M-1）；`.claude/skills/uc/SKILL.md` L8 之 `tmp/verify_cmd.conf` 契約
- `coordination_dependency`: 無

**檔案治理/來源說明**：MOBCD-03 手機端實測後，Akira 2026-09-02 L2 明示：「反正就是讓我可以用選取的方式，最好是用 Yes/No 或 123 這種選項，都不要讓我輸入太多的字。」本票據此將 ticket-run 之**全部**等待輸入點統一為數字選取，並補齊實測暴露之三項缺陷。

**開票期實測發現（Desktop 第一手，三筆）**：

| # | 發現 | 取證 | 嚴重度 |
|---|------|------|--------|
| H-1 | **跨票污染（靜默）**：`/uc` 依契約 MUST 自 `tmp/verify_cmd.conf` 讀 `TICKET_ID` 且 MUST NOT 猜測；而 ticket-run 九步**不寫該檔**。手機端以 ticket-run 開 A 票後執行 `/uc`，`/uc` 將讀到殘留之前一張票 ID，並將收尾 SOP（勾 AC＋lifecycle sync＋append_note）套用於**錯誤的票** | 實測 `read_file_safe(APP/tmp/verify_cmd.conf)` → `TICKET_ID=MOBCD-03`, `DIFF_REF=f4562cf`（桌機執行 MOBCD-03 時所寫，不因手機端派工而更新）；對照 `uc/SKILL.md` 條 8 之字面契約 | **高**——不報錯、不中斷，事後才會發現收錯票。Akira 回報之「沒跳 /uc」屬僥倖避開 |
| H-2 | **流程斷點無規格**：九步止於 `append_note`，其後行為未定義。Claude 是否提議下一步屬即興，故「有的票跳 /uc、有的不跳」 | Akira 2026-09-02 回報；對照 SKILL.md「範圍邊界」節僅涵蓋步驟 1–9 | 中——每次行為不可預期，手機端需自行打字跨越缺口 |
| H-3 | **長票摘要靜默失效**（RGR M-1 之確認與擴大）：步驟 5 以 `read_file_safe(max_chars=1000)` 窗口讀取，長票之 `ALLOWED=` 位於窗口外 | RGR 報告實測 `TKR-ARCH-04` 之 ALLOWED 落於第 6,951 字元、`TKR-ARCH-03-FIX-B` 落於第 3,143 字元，均超出窗口 | 中——**且本票新增之 conf 同步同受其害**（Verify Config 恆位於票面中段），故 MUST 於同票一併修，不得僅調大 `max_chars` |

**票號 provenance**：`MOBCD-` 前綴已登記於 PLT `.claude/rules/ticket_pattern.md` L65。全冊機械取號：兩專案 registry 併掃 `grep MOBCD-0`，命中 01／02（PLT）、03（APP），max-ID = 03，取 `-04`。

## Verify Config
TICKET_ID=MOBCD-04
CREATED=2026-09-02
PRESET=docs
ALLOWED=.claude/skills/ticket-run/SKILL.md,plans/cso_briefing/project/deliver/MOBCD-04_Acceptance.md
FORBIDDEN=app/,src/,gradle/,.claude/skills/uc/,D:/DevSources/workflow/mcp-ticket-platform/
MAX_LINES=110
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-09-02（`.claude/skills/ticket-run/SKILL.md` 為 MOBCD-03 產出，開工時以 AI-0 復驗其存在與九步結構）
BASE_COMMIT=N/A
TARGET_BLOB=N/A

> **互動總則（本票之唯一驗收精神）**：skill 執行期間，使用者之**每一次**輸入 MUST 為單一數字。MUST NOT 要求輸入 `y`／`yes`／`n`／slash command／票號／任何英文字母或符號。判定方式為機械掃描：SKILL.md 中凡「等待使用者」之步驟，其接受值 MUST 僅列舉數字。

## Action Items

### AI-0: 錨點復驗（開工前）
- **動作**: 確認 `.claude/skills/ticket-run/SKILL.md` 存在且含步驟 1–9；`registry_list(project="SmartCameraApp1", lifecycle="PENDING")` 確認本票在列。
- **不符 → 停止回報。**
- [x] 完成（本票由 ticket-run 步驟 7 開工時已轉 `IN_PROGRESS`，符合正常派工流程；SKILL.md 現存且含十步結構，內含步驟 1–9）

### AI-1: 步驟 6 確認方式改為數字
- **檔案**: `.claude/skills/ticket-run/SKILL.md`
- **現值**: 步驟 6 為「等待使用者回覆 `y` 確認；非 `y` 一律視為取消」
- **目標值**: 改為輸出兩行選項並等待數字：`1 = 確認開工`、`2 = 取消`。`2` 或任何非 `1` 輸入一律視為取消，流程結束且不寫冊面。錯誤處置段之「使用者回非 `y`」同步改為「回非 `1`」。
- **波及面**: MUST grep 全檔清點 `y` 之所有實例（步驟 6、錯誤處置、AC 引用），逐一改正。
- [x] 完成（2026-09-03：步驟 6 改輸出 `1 = 確認開工` / `2 = 取消` 兩行選項；錯誤處置「使用者回非 `y`」改為「於步驟 6 回非 `1`」；另於檔頭新增「互動總則」最高階約束。波及面掃描：`grep "回覆 \`y\`|非 \`y\`|回 \`y\`"` 回零命中）

### AI-2: 步驟 5 改為定位擷取，廢除窗口讀取（修 H-3／RGR M-1）
- **檔案**: `.claude/skills/ticket-run/SKILL.md`
- **現值**: `read_file_safe(<file>, max_chars=1000)` 後於窗口內尋找 Goal 與 ALLOWED
- **目標值**: 改以 `grep_files` 定位擷取，**不讀全文、不設窗口**：
  - 呼叫 `grep_files(paths=[<base + file 欄>], query="^(# |ALLOWED=|MAX_LINES=)", regex=True)`
  - 自回傳 snippet 直接取得標題行與 `ALLOWED=` 行，組成 3–5 行摘要
- **理由（MUST 寫入 SKILL.md 該步下方）**: 票面 `ALLOWED=` 之字元位移隨票長變動（實測 6,951／3,143 字元），任何固定 `max_chars` 均會對長票靜默失效；`grep_files` 回傳含行號與 snippet，不受位移影響，且輸出量恆定，適合手機端。
- **MUST NOT** 以調大 `max_chars` 作為修法。
- [x] 完成（2026-09-03：步驟 5 改 `grep_files(query="^(# |ALLOWED=|MAX_LINES=)", regex=True, max_matches=6)`，無 `max_chars` 參數；位移實測數據 6,951／3,143 已寫入該步下方作為修法理由。**新實作已正向驗證**：對 `MEO-T3_H1_H2_Validation.md` 實測回 3 筆命中——L1 標題、L13 `ALLOWED=`（三檔完整未截斷）、L15 `MAX_LINES=400`，補上 MOBCD-03 §3 遺留之「步驟 5 無正向證據」疑點）

### AI-3: 新增步驟 10 — conf 同步與交棒選單（修 H-1／H-2）
- **檔案**: `.claude/skills/ticket-run/SKILL.md`
- **目標值**: 於步驟 9 後新增步驟 10，含下列**固定次序**：
  1. **先同步 conf**（H-1 為高嚴重度，MUST 早於任何交棒選項）：以 `grep_files(paths=[<base + file 欄>], query="^(TICKET_ID|CREATED|PRESET|ALLOWED|FORBIDDEN|MAX_LINES|ANCHOR_PROBED|BASE_COMMIT|TARGET_BLOB)=", regex=True)` 自票面擷取各欄位；`DIFF_REF` 以 `git rev-parse --short HEAD` 之實際輸出回填（MUST NOT 沿用票面佔位字面）；以 `write_file_safe(mode="overwrite")` 寫入 `tmp/verify_cmd.conf`
  2. **寫入後 read-back 驗證**：重讀該檔，確認 `TICKET_ID` 等於本次選中之票號。不符 → 停止並告知，MUST NOT 續出選單
  3. **輸出交棒選單**，恰四項，每項一行：
     ```
     1 = 開始執行（進入 uc 模式）
     2 = 先看完整票面
     3 = 先跑 rigor gate 審查
     4 = 停在這裡
     ```
  4. 選 `1` → 依 `.claude/skills/uc/SKILL.md` 進入該模式；選 `2` → 輸出票面後**重出同一選單**；選 `3` → 依 `ticket-rigor-gate` skill 執行；選 `4` → 結束，明確輸出「已開工，停在此處」
- **Hard 要求**: 選單 MUST NOT 要求使用者輸入 `/uc` 或任何 slash command——slash 於手機端需切換鍵盤，違反互動總則。
- [x] 完成（2026-09-03：步驟 10 含 10.1 conf 同步、10.2 read-back、10.3 四選項選單，次序固定且 10.1 早於 10.3；另新增「步驟 10 之存在理由」節載明 H-1 高嚴重度污染機制與 H-2 斷點，標記 MUST NOT 刪除；「範圍邊界」節同步更新為步驟 1–10，並新增一條「本 skill 不執行票的內容」以消除 H-2 之語義歧義）

### AI-4: 實測並落檔
- **檔案**: `plans/cso_briefing/project/deliver/MOBCD-04_Acceptance.md`
- **動作**: 手機端觸發 skill，標的選本票以外之任一 PENDING 票，走完步驟 1–10，於選單選 `4`（停在這裡，避免實測時真的進 uc 模式改動程式碼）。逐項記錄 AC 證據，並附 conf 寫入前後之 `TICKET_ID` 對照。
- **還原**: 實測後 MUST 將該標的票 `lifecycle` 還原 `PENDING`（本票非其真正開工），並以 `registry_append_note` 註記「MOBCD-04 實測用，非實際開工」。
- [x] 完成（2026-09-03，標的票 `INFRA-SETTINGS-MERGE-01`，步驟 1–10 全走通，選單回 `4`；已還原 `lifecycle=PENDING` + 註記「MOBCD-04 實測用，非實際開工」；證據落 `MOBCD-04_Acceptance.md`。**AC-6〔步驟 6 回 `2`〕未觸發**——本次實測回 `1`，未行經取消路徑，仍待補測）

### AI-5: 摘要強制欄位擴充與 write_file_safe 絕對路徑補正（2026-09-03 增補）
- **檔案**: `.claude/skills/ticket-run/SKILL.md`
- **增補依據**: ①Akira 2026-09-03 L2 明示「選取票摘要新增強制性顯示：Mode/Effort、前置依賴」；②AI-4 驗收檔 §2 揭露之工具側 gotcha。二者同屬本票 scope（skill 互動與步驟 5／10.1 實作），依反過度治理原則不另立票。
- **目標值**:
  1. 步驟 5 之 `grep_files` query 擴充納入 `**Mode**`、`**前置依賴`、三分 dependency 三類行，`max_matches` 提至 10，摘要行數由 3–5 改為 **5–9**
  2. 明列五項**強制顯示欄位**：票名／Mode-Effort／前置依賴／ALLOWED／MAX_LINES，缺一即為本步失敗；欄位於票面不存在時 MUST 輸出「（票面未載）」而非靜默略過——否則「票面缺欄」與「摘要漏抓」不可區辨
  3. 步驟 10.1 之 `write_file_safe` 改明文絕對路徑，並載明相對路徑會被解析至 allowed roots 之首且 `ok:true` 不報錯
- **理由**: Mode／Effort 決定開工後走哪條執行路徑（影響步驟 10.3 之選擇），前置依賴決定該票**此刻是否可動**。二者缺席時，使用者僅憑票名與 ALLOWED 無法判斷「該不該現在跑」——此為 `MOBCD-01` §2 實測所載「摘要僅部分足夠」之直接補正。
- [x] 完成（2026-09-03：SKILL.md 7,194→8,623 bytes。**新 query 已正向驗證**——對 `INFRA-SV-GOV-01_SV_Governance_Fix.md` 實測回 5 筆，涵蓋 L1 標題、L5 `**Mode**: normal → uc || **Effort**: Medium`、L7 `**前置依賴**: 無（與 MEO-T3 並行；ALLOWED 檔案集不相交）`、L13 `ALLOWED=`、L15 `MAX_LINES=200`，五欄位無一遺漏。regex 之 `**前置依賴` 為前綴匹配，同時涵蓋 `**前置依賴**:` 與 `**前置依賴（依賴三分）**：` 兩種票面寫法）

## Hard Constraints
1. MUST NOT 改動 `.claude/skills/uc/` 任何檔案；本票僅改 ticket-run，uc 為唯讀契約來源。
2. MUST NOT 改動 PLT repo；PLT 版之同步另立 MOBCD-05。
3. MUST NOT 以調大 `max_chars` 作為 H-3 之修法。
4. MUST NOT 於任何等待輸入點接受非數字值。
5. MUST NOT 建立 lock 檔、`selection_state` 或任何外部狀態載體；步驟 10 之 `tmp/verify_cmd.conf` 為 uc 契約要求之既有檔，屬同步而非新建狀態載體。
6. 步驟上限為 10；新增步驟需 Akira 明示。

## Error Handling
- `git rev-parse` 不可用或回空 → conf 之 `DIFF_REF` MUST NOT 留佔位字面，停止回報
- conf read-back 之 `TICKET_ID` 不符 → 停止並告知，不出選單（防 H-1 復發）
- `grep_files` 對票檔回零命中 → 該票缺 Verify Config 段，停止並告知票號，MUST NOT 以空值寫入 conf
- 使用者於選單回非 1–4 → 重出選單，MUST NOT 預設任一選項
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** SKILL.md 中所有「等待使用者」步驟（4、6、10）之接受值均僅列舉數字；全檔 grep `回覆 y`／`非 y`／`/uc` 作為**使用者輸入要求**之實例為 0（實測：唯一 `/uc` 命中為 L48「步驟 9 後若無規格…有的票跳 `/uc`」，屬 H-2 問題敘述之歷史引用，非使用者輸入要求）
- [x] **[存在性]** 步驟 5 使用 `grep_files` 且**不含** `max_chars` 參數；該步下方載有位移實測數據（6,951／3,143）作為修法理由（L19-21 現地核驗）
- [x] **[存在性]** 步驟 10 含四選項選單，次序為「先 conf 同步、後選單」（10.1→10.2→10.3），且含 10.2 read-back 驗證子步驟（L32-43 現地核驗）
- [x] **[行為性]** given 選中票 T 完成步驟 10.1–10.2，expect `tmp/verify_cmd.conf` 之 `TICKET_ID` 等於 T 之票號，expect `DIFF_REF` 為 7 碼 hash 而非佔位字面（T=MOBCD-04 自身：`tmp/verify_cmd.conf` 現讀 `TICKET_ID=MOBCD-04`、`DIFF_REF=f4562cf`〔7碼、對照 `git rev-parse --short HEAD` 相符，非票面 `<開工時…回填>` 佔位字面〕）
- [x] **[行為性]** given 標的為長票（票面 `ALLOWED=` 位於第 3,000 字元之後），expect 步驟 5 摘要仍含 `ALLOWED=` 內容（H-3 已修之正向證明；本 session 直接以 `grep_files` 對 `INFRA-COMPACT-REVB-APP-01`〔`ALLOWED=` 位於第 3,421 字元〕實測，回傳 3 筆命中含 L32 `ALLOWED=.claude/skills/compact/SKILL.md`，非 mobile 觸發但為同一工具呼叫之直接驗證）
- [ ] **[行為性]** given 使用者於步驟 6 回 `2`，expect 標的票 `lifecycle` 維持 `PENDING` 且 conf 未被改寫 —— 未驗，需手機端互動回覆
- [x] **[行為性]** given 使用者於選單回 `4`，expect 輸出「已開工，停在此處」且不進入 uc 模式（標的票 `INFRA-SETTINGS-MERGE-01`，輸出「已開工，停在此處。」逐字相符，未進入 uc 模式，見 `MOBCD-04_Acceptance.md` §3）
- [x] **[實機]** 手機端完成一次步驟 1–10，證據落 `plans/cso_briefing/project/deliver/MOBCD-04_Acceptance.md`：MUST 含每一次使用者輸入之字面（應全為單一數字）、conf 寫入前後 `TICKET_ID` 對照、選單輸出字面（標的 `INFRA-SETTINGS-MERGE-01`，輸入序列 `14`→`1`→`4` 全為單一數字；conf `TICKET_ID` 前 `MOBCD-04`→後 `INFRA-SETTINGS-MERGE-01`；已依 AI-4 還原子句復原）
- [x] **[存在性]**（AI-5）步驟 5 明列五項強制顯示欄位（票名／Mode-Effort／前置依賴／ALLOWED／MAX_LINES），且載有「缺一即為本步失敗」與「票面未載時輸出佔位而非靜默略過」二條
- [x] **[存在性]**（AI-5）步驟 10.1 之 `write_file_safe` 呼叫為明文絕對路徑，且載有相對路徑會落至 allowed roots 之首（實測 `ObsidianVault`）且 `ok:true` 不報錯之警示
- [ ] **[行為性]**（AI-5）given 標的票面同時含 `**Mode**` 與 `**前置依賴**` 行，expect 步驟 5 摘要**同時**輸出該二行內容且行數 5–9 —— **工具層已驗**（`grep_files` 對 `INFRA-SV-GOV-01` 回 5 筆全中），**skill 流程內未驗**，隨 AC-6 一併補測
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset，不觸及 app/；核驗：本票異動僅 `.claude/skills/ticket-run/SKILL.md`，零 app/ 觸碰)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 —— `.claude/skills/` 未受 `.gitignore` 排除（`git check-ignore -v` 零命中）；`git status --short` 現地核驗：dirty/untracked 僅 `.claude/skills/ticket-run/`（含 SKILL.md）與 `plans/cso_briefing/project/deliver/MOBCD-04_Acceptance.md` 兩個 ALLOWED 標的，無其他污染

---

## Addendum-1（2026-09-03，Claude Code CLI session，誠實揭露）

AI-0～AI-3 於本 session 承接時已由前一輪（2026-09-03 標註）完成並落地於 `.claude/skills/ticket-run/SKILL.md`（十步版）；本 session 現地核驗 AC-1～AC-3（存在性）與 AC-4（行為性，以 MOBCD-04 自身為標的票之 conf 同步證據）全數通過。AC-5（長票 `grep_files` 正向驗證）由本 session 直接以 MCP `grep_files` 工具對 `INFRA-COMPACT-REVB-APP-01`（`ALLOWED=` 位於第 3,421 字元）實測補齊，取得非 mobile 觸發但為同一工具呼叫路徑之直接證據。

AI-4／AC-6／AC-7／AC-8 **保留未勾**：三者要求「手機端 Remote Control 觸發 skill、即時回覆單一數字」之真實互動，本 session 為 Claude Code CLI（無手機端接入能力），且 AI-4 動作本身要求選定一張他票為標的並於實測後執行還原（`lifecycle` 復 `PENDING` + `registry_append_note` 註記），此類「開工又還原」之狀態變更不宜由 CLI session 憑空模擬——需真實手機端 session 產生 producer 證據。末項 `git diff --stat` 範圍確認同步保留未勾，待 AI-4 完成、`SKILL.md` 定案後以真實 diff 核驗。

## Addendum-2（2026-09-03，`/ticket-run` 真實互動，AI-4 補齊）

Akira 於本對話 session 內以 `/ticket-run` 觸發 skill，標的選 `INFRA-SETTINGS-MERGE-01`（本票以外之真實 PENDING 票），完整走通步驟 1–10：使用者輸入序列 `14`（選票）→`1`（確認開工）→`4`（選單停在此處），全為單一數字，符合互動總則。步驟 10.1 conf 同步成功（`TICKET_ID` 前 `MOBCD-04`→後 `INFRA-SETTINGS-MERGE-01`，直接證明 H-1 跨票污染修復生效）；10.2 read-back 通過；10.3 選單輸出逐字相符，回 `4` 後正確停止、未進入 uc 模式。事後依 AI-4「還原」子句將標的票 `lifecycle` 復 `PENDING` 並附還原註記。完整逐字證據見 `MOBCD-04_Acceptance.md`。

**本次補齊**：AI-4（完成，含還原）、AC-7、AC-8［實機］、末項 `git diff --stat` 範圍確認。**AC-6 仍未觸發**（本次選擇 `1` 而非 `2`，未行經取消路徑）——本票收束前若無另一次真實「回 `2`」互動，AC-6 應誠實標記未驗，不得以邏輯推定通過。

**工具側發現**（非本票程式缺陷，供 MOBCD-05 或後續補丁票參考）：`write_file_safe` 對相對路徑 `tmp/verify_cmd.conf` 之預設解析落在錯誤專案根（`ObsidianVault` 而非 `SmartCameraApp1`），已以絕對路徑修正並成功；SKILL.md 步驟 10.1 現僅對 `grep_files` 之 `paths` 參數明文要求 base 前綴，未涵蓋 `write_file_safe` 之目標 `path`，此為文字遺漏，非邏輯錯誤。

**收束後續**：`MOBCD-05`（PLT）：將本票三項修正（數字選項化、定位擷取、步驟 10）連同 MOBCD-03 之 G-1 base-path 修補一併回植 PLT 版 SKILL.md，base 常數為 `D:/DevSources/workflow/mcp-ticket-platform/`；MOBCD-05 起草時 MUST 一併吸收上述 `write_file_safe` base-path 明文化建議。
