---
name: ticket-run
description: 列出目前 PENDING 票並依序號選取派工開工，取代直接輸入票號。當使用者說「有什麼票」「跑票」「派工」「ticket」「開一張票」等語句時觸發。
---

# ticket-run

以序號選取取代票號輸入，供手機端 Remote Control 派工使用（`MOBCD-02` 建立，`MOBCD-04` 選項化，`MOBCD-05` 回植並增列 NOT_FOUND 歸因，`MOBCD-07` 導入 repo-local Project Profile 與兩層數字選單）。

**互動總則（最高階約束）**：使用者之**每一次**輸入 MUST 為單一數字。MUST NOT 要求輸入 `y`／`yes`／`n`／slash command／票號／任何英文字母或符號——slash 與英文於手機端需切換鍵盤，違反本 skill 存在之目的。
**MUST NOT** 建立 lock 檔、`selection_state`、session token 或任何外部狀態載體——序號與票號之對應關係存於本次對話歷史，Claude 直接引用自己上一則輸出之清單即可。Profile 為靜態設定檔，非狀態載體。

## Profile Load Gate（先於十步流程與一切操作）

本節 MUST 早於**任何** `registry_list`／`grep_files`／`search_files`／`registry_set_status`／`registry_append_note`／`write_file_safe`／`git rev-parse --short HEAD`／交棒動作。

1. **取 workspace root**：以 Claude Code Bash 執行 `git rev-parse --show-toplevel`。此為唯一不依賴 Profile 之路徑來源（Profile 自身無法自 Profile 取路徑）。
2. **讀 Profile**：`read_file_safe(<ROOT> + "/.claude/skills/ticket-run/ticket-run.project.md", max_chars=1200)`。MUST NOT 傳相對路徑——該原語不接受之，本線已三次實測回 `NOT_FOUND`（G-1／P-1／MOBCD-05 首次觸發）。
3. **五項驗證**（任一不符即輸出 `PROFILE_INVALID：<欄位或原因>` 並停止）：
   - 五欄位（`PROJECT`／`REPO_ROOT`／`TICKET_FILE_BASE`／`TICKET_SEARCH_ROOT`／`VERIFY_CONF`）皆存在非空且各僅一次
   - `REPO_ROOT` 等於前述 `git rev-parse` 之輸出
   - `TICKET_FILE_BASE` 與 `TICKET_SEARCH_ROOT` 均以 `REPO_ROOT` 為前綴
   - `VERIFY_CONF` 等於 `${REPO_ROOT}/tmp/verify_cmd.conf`
4. **fail-closed**：MUST NOT 以 hardcode 預設值、CWD 推測、他 repo profile 或對話記憶回補。
5. 通過後，以下步驟一律引用 `PROFILE.PROJECT`／`PROFILE.REPO_ROOT`／`PROFILE.TICKET_FILE_BASE`／`PROFILE.TICKET_SEARCH_ROOT`／`PROFILE.VERIFY_CONF`。

## 十步流程（逐字固定，MUST NOT 臨場更動）

1. **依序呼叫三次**，每次僅帶單一 `lifecycle` 值，`project` 與 `fields` 固定，均帶 `limit=50`：
   - `registry_list(project=<PROFILE.PROJECT>, lifecycle="PENDING", fields=["id","title","file","lifecycle"], limit=50)`
   - `registry_list(project=<PROFILE.PROJECT>, lifecycle="TICKET_RIGOR", fields=["id","title","file","lifecycle"], limit=50)`
   - `registry_list(project=<PROFILE.PROJECT>, lifecycle="IN_PROGRESS", fields=["id","title","file","lifecycle"], limit=50)`
   - **MUST NOT 合併為單次呼叫**：`lifecycle` 僅接受**單一字串值**。傳陣列回 pydantic `string_type` 驗證錯誤（會報錯，尚屬安全）；傳逗號分隔字串 `"PENDING,IN_PROGRESS,TICKET_RIGOR"` 則回 **`ok:true` + `total:0`**——**靜默陷阱**，與「確實無票」不可區辨（同 `lifecycle="OPEN"` 之失效模式）。
   - **三狀態之納入理由**：`PENDING` 為待派工；`TICKET_RIGOR` 為 rigor gate stage① 寫回、已審待派；`IN_PROGRESS` 為已開工未完成、需續跑者。僅取 `PENDING` 會使後二者在手機端**完全不可見**，而中斷後續跑正是手機派工之主要場景。
   - **`limit=50` 之理由**：查詢 `limit` 與顯示上限（見步驟 3 之兩層選單）**MUST NOT 合一**。查詢若限低值，①分組層抽 prefix 會漏（APP 實測 PENDING 已 22 張）、②步驟 7 窄重驗之 selected id 若排在截斷點後將被誤判「票狀態已變更」——二者皆為**靜默假陰性**。
2. 三次結果**全為空**時，MUST 明確輸出「目前沒有可派工的票（PENDING／TICKET_RIGOR／IN_PROGRESS 均為 0）」，MUST NOT 僅回空白或「查無資料」，**不出任何選單**。
3. 有結果時，先依 `PENDING` → `TICKET_RIGOR` → `IN_PROGRESS` 之序合併，再插入**兩層數字選單**分組層，逐字如下：
   - **3.1 prefix 抽取**：對每筆 record，`prefix` = `id` 中第一個 `-` 之前的子字串；`id` 無 `-` 者，`prefix` = 整個 `id`。依 prefix 分組計數，張數降序、同數依字母序，末項恆為「全部 (<總數>)」。
   - **不驗 prefix 格式**：抽到什麼列什麼。`KAIROS3-`／`TRACTION-` 等含數字或逾 6 字母之舊 family MUST 正常可選（APP 實測 22 張 PENDING 中有 3 張屬此類）。
   - **跳過空層**：不重複 prefix 數為 1 時，MUST 跳過第一層直接出第二層。
   - **3.2 第一層輸出**（不重複 prefix 數 ≥2 時）：
     ```
     可派工分組：

     1. <prefix1> (<張數>)
     2. <prefix2> (<張數>)
     ...
     N. 全部 (<總數>)

     請輸入序號
     ```
     使用者回超出範圍之數字 → 重列第一層，MUST NOT 進第二層。
   - **3.3 第二層輸出**：依所選分組（或「全部」）之票，依 `PENDING` → `TICKET_RIGOR` → `IN_PROGRESS` 之序、單一連續編號、每票一行：`<n>. <id> [<lifecycle>] — <title 截斷至 40 字>`；編號自 1 連續遞增，**跨狀態不重編**。
     - `[<lifecycle>]` 標記 MUST 顯示，使用者需據此區辨「新開工」與「續跑」。
     - **顯示上限 12**：逾 12 張時顯示前 12 張，尾行輸出「…另有 N 張未顯示；請縮小分組或於桌面端整理後再派工」。MUST NOT 做 pagination。
     - **末行 MUST 附返回項**（第一層曾顯示時，即不重複 prefix 數 ≥2）：
       ```
       0 = 返回議題選單
       ```
       第一層因「跳過空層」未顯示時（不重複 prefix 數為 1），MUST NOT 附此項——無上層可返，顯示之即為死選項。
     - 選中 `IN_PROGRESS` 票時，步驟 7 之 `set_status` 為冪等操作（狀態不變），CAS 仍照常執行；步驟 9 之 note 語義為**本次執行之發起溯源**，故續跑時再記一筆屬正確行為，非重複。
     - 選中 `TICKET_RIGOR` 票時，步驟 7 將其轉為 `IN_PROGRESS`，為已審→開工之正確轉換。
   - **不新增外部狀態**：第一層所選分組存於對話歷史，與序號選票同理。
4. 等待使用者回覆單一數字（第二層之選票序號，或 `0` 返回）。
   - **回 `0`** → 重新輸出**步驟 3.2 第一層**（分組選單），等待重選分組。MUST NOT 重跑步驟 1 之三次 `registry_list`——分組資料存於對話歷史，重查徒增往返與 payload。
   - **`0` 僅於第一層曾顯示時有效**；第一層未顯示時（單一 prefix），`0` 視為超出範圍，重列第二層。
   - 回超出範圍之數字 → 重列第二層，MUST NOT 寫冊面。
5. 呼叫 `grep_files(paths=[<PROFILE.TICKET_FILE_BASE> + "/" + <選中票之 file 欄>], query="^(# |ALLOWED=|MAX_LINES=|\*\*Mode\*\*|\*\*前置依賴|- `(execution|evidence|coordination)_dependency`)", regex=True, max_matches=10)`，自回傳 snippet 組成 **5–9 行**摘要。
   - **強制顯示欄位（MUST 全數呈現，缺一即為本步失敗）**：
     1. 票名（標題行）
     2. **Mode／Effort**（格式示例：`Mode/Effort：normal → uc ｜ Medium`）
     3. **前置依賴**（格式示例：`前置依賴：無（與 MEO-T3 並行，ALLOWED 不相交）`；三分依賴有值者逐項列出，全無則明寫「無」）
     4. `ALLOWED=`
     5. `MAX_LINES=`
   - **理由**：Mode／Effort 決定開工後該走哪條執行路徑（影響步驟 10.3 之選擇），前置依賴決定該票**此刻是否可動**。二者缺席時，使用者僅憑票名與 ALLOWED 無法判斷「該不該現在跑」——此為 `MOBCD-01` §2 實測所載「摘要僅部分足夠」之直接補正。
   - 某欄位於票面不存在時，該行輸出 `<欄位名>：（票面未載）`，MUST NOT 靜默略過——靜默略過會使「票面缺欄」與「摘要漏抓」不可區辨。
   - 冊面 `file` 欄為相對 **repo root** 之路徑（如 `docs/pending/…`），`grep_files` 之 `paths` 不接受相對路徑，故 base 常數為必要而非冗餘。
   - **MUST NOT 改回 `read_file_safe` 窗口讀取，MUST NOT 以調大 `max_chars` 為修法**：票面 `ALLOWED=` 之字元位移隨票長變動（實測 `TKR-ARCH-04` 落於第 6,951 字元、`TKR-ARCH-03-FIX-B` 落於第 3,143 字元），任何固定窗口均會對長票**靜默失效**——摘要看似成功，實則缺 ALLOWED。`grep_files` 依模式定位，不受位移影響，且僅回命中行，輸出量恆定，適合手機端。
6. 輸出三行選項並等待使用者回覆單一數字：
   ```
   1 = 確認開工
   2 = 取消
   0 = 返回議題選單
   ```
   - **回 `0`** → 放棄本次選票，重新輸出**步驟 3.2 第一層**（分組選單）。同步驟 4 之返回：MUST NOT 重跑步驟 1，MUST NOT 寫冊面，MUST NOT 視為取消（取消為終止流程，返回為續留流程）。
   - **`0` 僅於第一層曾顯示時有效**；第一層未顯示時，回 `0` 改重新輸出**第二層**（票清單），使用者仍得改選他票。
   `1` → 進步驟 7。`2` → 取消，流程結束，不寫冊面。`0` → 返回（見上）。**其他輸入 → 重顯相同摘要與 1／2／0 選項**，MUST NOT 寫冊面、MUST NOT 視為取消、MUST NOT 改變選中 ticket（手機誤觸不應等同取消）。
7. **窄重驗**：呼叫 `registry_list(project=<PROFILE.PROJECT>, lifecycle=<選中票之 lifecycle>, fields=["id","lifecycle","revision"], limit=50)`：
   - 僅取 `id == selected_id` 之 record；**找不到，或其 lifecycle 已非選取時之狀態 → 停止並要求重新列票**，MUST NOT 沿用列票時之舊 lifecycle。
   - `expected_revision` 取回傳之 `revision`。**`revision` 為全冊級單一計數器**，各 record 回同值，不存在「selected record 專屬 revision」；窄重驗之目的為①確認 selected id 仍在預期 lifecycle、②縮小 payload，**非**取得專屬 revision。CAS 因此為**全冊級樂觀鎖**——他票之任何寫入亦觸發 MISMATCH（2026-09-03 實測自然發生一次），為 Akira 明示保留之已知代價。
   - 呼叫 `registry_set_status(<id>, "IN_PROGRESS", expected_revision=<回傳之 revision>)`。
8. 回傳 `REGISTRY_REVISION_MISMATCH` 時，MUST 停止並告知「冊面剛被其他流程更新，請重選」，MUST NOT 自動重試。
9. 呼叫 `registry_append_note(<id>, "<YYYY-MM-DD hh:mm> 執行起始｜來源：手機 Remote Control（Claude Code @<PROFILE.PROJECT>）")`。
   - 本 note 之語義為**執行溯源**（記錄本次執行之發起環境與時間），非派工動作紀錄。冊面 `updated_at` 僅日級精度，`lifecycle` 僅表狀態，二者均不承載「何時、自何處發起」。
10. **交棒**（次序固定，`10.1` MUST 早於 `10.3`）：
    - **10.1 同步 `tmp/verify_cmd.conf`**：以 `grep_files(paths=[<PROFILE.TICKET_FILE_BASE> + "/" + <選中票之 file 欄>], query="^(TICKET_ID|CREATED|PRESET|ALLOWED|FORBIDDEN|MAX_LINES|ANCHOR_PROBED|BASE_COMMIT|TARGET_BLOB)=", regex=True, max_matches=12)` 自票面擷取各欄位；`DIFF_REF` 以 `git rev-parse --short HEAD`（cwd 為 `PROFILE.REPO_ROOT`）之**實際輸出**回填（MUST NOT 沿用票面之 `<開工時…回填>` 佔位字面）；以 `write_file_safe(path=<PROFILE.VERIFY_CONF>, mode="overwrite")` 寫入。
      - **MUST 明文絕對路徑，MUST NOT 傳相對路徑**：實測（`MOBCD-04` AI-4）相對路徑被解析至 **`D:/DevSources/ObsidianVault/tmp/verify_cmd.conf`**——即 allowed roots 之首者——且 `ok:true` 不報錯、不警告。
    - **10.2 read-back 驗證**：重讀 `<PROFILE.VERIFY_CONF>`，確認 `TICKET_ID` 等於本次選中之票號。不符 → **停止並告知，MUST NOT 續出選單**。
    - **10.3 輸出交棒選單**，恰五項：
      ```
      1 = 開始執行（進入 uc 模式）
      2 = 開始執行（進入 normal 模式）
      3 = 先看完整票面
      4 = 先跑 rigor gate 審查
      5 = 停在這裡
      ```
      `1` → 依 `.claude/skills/uc/SKILL.md` 進入該模式；`2` → **不進入** uc 臥底模式，以一般對話模式依票面 Action Items 逐項執行，每項完成後回報（適用於票面 `Mode` 非 uc、或需人機互動確認者）；`3` → 輸出票面後**重出同一選單**（MUST NOT 將使用者留在無選項狀態）；`4` → 依 `ticket-rigor-gate` skill 執行；`5` → 結束，明確輸出「已開工，停在此處」。
      非 `1`–`5` 之輸入 → 重出選單，MUST NOT 預設任一選項。
      - **選項 1／2 之選擇依據**：步驟 5 摘要所示之 `Mode` 欄。`Mode` 含 `uc` 者建議選 `1`；`Mode` 為 `normal` 或票面需逐步確認者選 `2`。skill MUST NOT 依 `Mode` 自動代選，選擇權恆在使用者。

### 步驟 10 之存在理由（MUST NOT 刪除本節）

- **10.1 防跨票污染（高嚴重度）**：`uc/SKILL.md` 契約要求自 `tmp/verify_cmd.conf` 讀 `TICKET_ID` 且 MUST NOT 猜測。本 skill 於 `MOBCD-04` 前不寫該檔，致手機端開 A 票後執行 `/uc` 時，`/uc` 讀到**殘留之前一張票 ID**，並將收尾 SOP（勾 AC＋lifecycle sync＋append_note）套用於錯誤的票。**此失效不報錯、不中斷**，故 MUST 由本步機械消除。
- **10.3 消除流程斷點**：步驟 9 後若無規格，Claude 是否提議下一步屬即興行為，實測呈現「有的票跳 `/uc`、有的不跳」之不可預期性。

## 硬性參數約束

- `project` 值 MUST 為 `<PROFILE.PROJECT>` 引用，MUST NOT hardcode 字面量。理由：MCP 為遠端服務，PLT 與 APP 兩 cwd 連同一 server，不帶 `project` 會回全冊含他專案票。
- MUST NOT 使用 `actionable_only`：其語義為 `lifecycle=="PENDING"` 且 `depends_on` 全數 `CLOSED`/`VOID` 之子集，非單純等價於 `lifecycle=="PENDING"`，語義不透明。
- MUST NOT 使用 `lifecycle="OPEN"` 或任何未經實測之枚舉值——非法值回 `ok:true` + 空集合，不報錯，會被誤讀為「無票」。
- `lifecycle` 參數**僅接受單一字串值**，MUST NOT 傳陣列（回驗證錯誤）或逗號分隔字串（回 `ok:true` + `total:0` 之靜默空集合）。多狀態查詢 MUST 以多次呼叫達成，見步驟 1。
- 已實測枚舉集：`DRAFT_NEEDED`／`PENDING`／`IN_PROGRESS`／`REVIEW`／`TICKET_RIGOR`／`CLOSED`，MUST 視為不完全列舉（未來可能新增值）。
- `reviews` 四欄 key 固定為 `qa`／`sv`／`codex`／`traycer`。
- 查詢 `limit`（50）與顯示上限（12）**MUST NOT 合一**：查詢受限會使分組層抽 prefix 遺漏、步驟 7 窄重驗誤判票狀態已變更，二者皆為靜默假陰性；顯示上限僅作用於第二層之呈現，不回饋查詢層。

## 錯誤處置

- Profile Gate 之 `PROFILE_INVALID` → 依 Profile Load Gate 規則停止，MUST NOT 呼叫任何 registry。
- `registry_list` 回 `total=0` 非錯誤，走步驟 2。
- `REGISTRY_REVISION_MISMATCH` → 停止，告知，不自動重試。
- 回傳 `PROJECT_AMBIGUOUS` → 表示 `PROFILE.PROJECT` 未正確引用，屬本 SKILL 實作缺陷，停止回報。
- 使用者於第一層回非數字或超出範圍 → 重列第一層，MUST NOT 進第二層。
- 使用者於第二層回非數字或超出清單範圍 → 重列第二層，MUST NOT 寫入冊面。
- 使用者於步驟 6 回非 `1`／`2`／`0` 之其他輸入 → 重顯步驟 5 摘要與 1／2／0 選項，MUST NOT 寫入冊面、MUST NOT 視為取消。
- 使用者於步驟 4 或步驟 6 回 `0` 而第一層未曾顯示（單一 prefix）→ 依各該步驟之規則重列第二層，MUST NOT 輸出空白第一層。
- 步驟 7 窄重驗找不到 selected id，或其 lifecycle 已變更 → 停止並要求重新列票。
- `git rev-parse` 不可用或回空 → conf 之 `DIFF_REF` MUST NOT 留佔位字面，停止回報。
- 步驟 10.2 read-back 之 `TICKET_ID` 不符 → 停止並告知，不出選單。

### `grep_files` 回傳之三分支歸因（步驟 5 與 10.1 共用，MUST NOT 簡化為單一分支）

`grep_files` 對**不存在之檔案**同樣回 `ok:true` 且 `matches` 為空，與「檔案存在但無命中」在 `matches` 層面**不可區辨**。MUST 依 `visited_files` 判別，MUST NOT 僅看 `matches` 是否為空：

1. **`visited_files` ≥ 1 且 `matches` 空** → 檔案已讀取，該欄位確實不存在於票面。
   - 步驟 5：輸出「（票面未載）」佔位並續行。
   - 步驟 10.1：該票缺 Verify Config 段，**停止並告知票號**，MUST NOT 以空值寫入 conf。
2. **`visited_files` 為 0 且 `skipped_files` 之 reason 為 `not_found`** → 檔案不在該路徑。MUST 以 `search_files` 定位，其 `path` 為 `<PROFILE.TICKET_SEARCH_ROOT>`、`pattern` 為 `file` 欄之 basename、`exclude_globs` 含 `**/venv/**`／`**/.git/**`／`**/tmp/**`：
   - 命中 → **停止**並報「冊面 `file` 欄與實體不符：冊面記 `<file 欄值>`，實體位於 `<實際路徑>`」。**MUST NOT 自動修改冊面**——冊面異動屬人工裁決。
   - 未命中 → **停止**並報「檔案不存在於 `<PROFILE.TICKET_SEARCH_ROOT>` 之下，票面尚未落地或已刪除」。
   - **MUST NOT 以 repo root 為 `search_files` 之範圍**：實測耗時 14.8 秒且觸 `visited_files` 50000 上限而截斷（root 含 venv 與 tmp 測試資料）。
3. **其他 `skipped_files` reason（如 `not_a_file`）** → 停止並**逐字**回報該 reason，MUST NOT 歸類至上述任一分支。

三分支之訊息 MUST 明確區辨「工具側路徑問題」與「冊面／實體脫節」，MUST NOT 以單一訊息涵蓋二者，MUST NOT 猜測等價位置。**事件依據**：`MOBCD-05` 首次觸發時，未回植之 PLT 版將 P-1（相對路徑）之 `NOT_FOUND` 誤歸因為「冊面記錄與實體不一致」，致使用者被要求裁決一個不存在的問題（經查證冊面正確、檔案存在）。

## 範圍邊界

- 本 skill 涵蓋 Profile Gate、開工與交棒（十步流程），MUST NOT 順手擴充搜尋、模糊比對或其他步驟；步驟上限為 10，新增步驟需 Akira 明示。
- MUST NOT 建立 lock 檔、`selection_state`、session token 或任何外部狀態載體。步驟 10.1 之 `tmp/verify_cmd.conf` 為 `uc` 契約要求之**既有**檔，屬同步而非新建狀態載體；Profile 為靜態設定檔，非狀態載體。
- 本 skill **不執行票的內容**。十步流程止於開工與交棒；實際執行由選單導向之 `uc`／`normal`／`ticket-rigor-gate` 承擔。
- **本檔為 `ticket-run` 之 SSOT**（2026-09-04 Akira L2：後續疊代集中於 PLT）。APP 版為單向下游，同步方向恆為 PLT → APP，MUST NOT 反向；於 APP 側直接改動屬違規，須改於本檔後同步。Profile 為 repo-local，MUST NOT 跨 project 讀取、切換或選擇——PLT skill MUST NOT 讀 APP profile，反之亦然。
