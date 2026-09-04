---
name: ticket-run
description: 列出目前 PENDING 票並依序號選取派工開工，取代直接輸入票號。當使用者說「有什麼票」「跑票」「派工」「ticket」「開一張票」等語句時觸發。
---

# ticket-run

以序號選取取代票號輸入，供手機端 Remote Control 派工使用（`MOBCD-02` 建立，`MOBCD-04` 選項化）。

**互動總則（最高階約束）**：使用者之**每一次**輸入 MUST 為單一數字。MUST NOT 要求輸入 `y`／`yes`／`n`／slash command／票號／任何英文字母或符號——slash 與英文於手機端需切換鍵盤，違反本 skill 存在之目的。
**MUST NOT** 建立 lock 檔、`selection_state`、session token 或任何外部狀態載體——序號與票號之對應關係存於本次對話歷史，Claude 直接引用自己上一則輸出之清單即可。

## 十步流程（逐字固定，MUST NOT 臨場更動）

1. 呼叫 `registry_list(project="SmartCameraApp1", lifecycle="PENDING", fields=["id","title","file"])`。
2. `total=0` 時，MUST 明確輸出「目前沒有 PENDING 票（冊面共 `<total>` 筆）」，MUST NOT 僅回空白或「查無資料」。
3. `total>0` 時，輸出編號清單，每票一行：`<n>. <id> — <title 截斷至 40 字>`；編號自 1 連續遞增。
4. 等待使用者回覆單一數字。
5. 呼叫 `grep_files(paths=["D:/DevSources/AndroidProj/SmartCameraApp1/plans/cso_briefing/project/" + <選中票之 file 欄>], query="^(# |ALLOWED=|MAX_LINES=|\*\*Mode\*\*|\*\*前置依賴|- `(execution|evidence|coordination)_dependency`)", regex=True, max_matches=10)`，自回傳 snippet 組成 **5–9 行**摘要。
   - **強制顯示欄位（MUST 全數呈現，缺一即為本步失敗）**：
     1. 票名（標題行）
     2. **Mode／Effort**（格式示例：`Mode/Effort：normal → uc ｜ Medium`）
     3. **前置依賴**（格式示例：`前置依賴：無（與 MEO-T3 並行，ALLOWED 不相交）`；三分依賴有值者逐項列出，全無則明寫「無」）
     4. `ALLOWED=`
     5. `MAX_LINES=`
   - **理由**：Mode／Effort 決定開工後該走哪條執行路徑（影響步驟 10.3 之選擇），前置依賴決定該票**此刻是否可動**。二者缺席時，使用者僅憑票名與 ALLOWED 無法判斷「該不該現在跑」——此為 `MOBCD-01` §2 實測所載「摘要僅部分足夠」之直接補正。
   - 某欄位於票面不存在時，該行輸出 `<欄位名>：（票面未載）`，MUST NOT 靜默略過——靜默略過會使「票面缺欄」與「摘要漏抓」不可區辨。
   - 冊面 `file` 欄為相對 `plans/cso_briefing/project/` 之路徑，`grep_files` 之 `paths` 不接受相對路徑，故 base 常數為必要而非冗餘。
   - **MUST NOT 改回 `read_file_safe` 窗口讀取，MUST NOT 以調大 `max_chars` 為修法**：票面 `ALLOWED=` 之字元位移隨票長變動（實測 `TKR-ARCH-04` 落於第 6,951 字元、`TKR-ARCH-03-FIX-B` 落於第 3,143 字元），任何固定窗口均會對長票**靜默失效**——摘要看似成功，實則缺 ALLOWED。`grep_files` 依模式定位，不受位移影響，且僅回命中行，輸出量恆定，適合手機端。
6. 輸出兩行選項並等待使用者回覆單一數字：
   ```
   1 = 確認開工
   2 = 取消
   ```
   `2` 或任何非 `1` 之輸入一律視為取消，流程結束，不寫冊面。
7. 呼叫 `registry_list` 取當前 `revision`，再呼叫 `registry_set_status(<id>, "IN_PROGRESS", expected_revision=<該值>)`。
8. 回傳 `REGISTRY_REVISION_MISMATCH` 時，MUST 停止並告知「冊面剛被其他流程更新，請重選」，MUST NOT 自動重試。
9. 呼叫 `registry_append_note(<id>, "<YYYY-MM-DD hh:mm> 執行起始｜來源：手機 Remote Control（Claude Code @SmartCameraApp1）")`。
   - 本 note 之語義為**執行溯源**（記錄本次執行之發起環境與時間），非派工動作紀錄。冊面 `updated_at` 僅日級精度，`lifecycle` 僅表狀態，二者均不承載「何時、自何處發起」。
10. **交棒**（次序固定，`10.1` MUST 早於 `10.3`）：
    - **10.1 同步 `tmp/verify_cmd.conf`**：以 `grep_files(paths=[<base + file 欄>], query="^(TICKET_ID|CREATED|PRESET|ALLOWED|FORBIDDEN|MAX_LINES|ANCHOR_PROBED|BASE_COMMIT|TARGET_BLOB)=", regex=True, max_matches=12)` 自票面擷取各欄位；`DIFF_REF` 以 `git rev-parse --short HEAD` 之**實際輸出**回填（MUST NOT 沿用票面之 `<開工時…回填>` 佔位字面）；以 `write_file_safe(path="D:/DevSources/AndroidProj/SmartCameraApp1/tmp/verify_cmd.conf", mode="overwrite")` 寫入。
      - **MUST 明文絕對路徑，MUST NOT 傳相對路徑 `tmp/verify_cmd.conf`**：實測（`MOBCD-04` AI-4）相對路徑被解析至 **`D:/DevSources/ObsidianVault/tmp/verify_cmd.conf`**——即 allowed roots 之首者——且 `ok:true` 不報錯、不警告。本 skill 之 base 常數要求原僅明載於 `grep_files` 之 `paths`，`write_file_safe` 之 `path` 屬遺漏，已於此補正。
    - **10.2 read-back 驗證**：重讀該檔，確認 `TICKET_ID` 等於本次選中之票號。不符 → **停止並告知，MUST NOT 續出選單**。
    - **10.3 輸出交棒選單**，恰四項：
      ```
      1 = 開始執行（進入 uc 模式）
      2 = 先看完整票面
      3 = 先跑 rigor gate 審查
      4 = 停在這裡
      ```
      `1` → 依 `.claude/skills/uc/SKILL.md` 進入該模式；`2` → 輸出票面後**重出同一選單**（MUST NOT 將使用者留在無選項狀態）；`3` → 依 `ticket-rigor-gate` skill 執行；`4` → 結束，明確輸出「已開工，停在此處」。
      非 `1`–`4` 之輸入 → 重出選單，MUST NOT 預設任一選項。

### 步驟 10 之存在理由（MUST NOT 刪除本節）

- **10.1 防跨票污染（高嚴重度）**：`uc/SKILL.md` 契約要求自 `tmp/verify_cmd.conf` 讀 `TICKET_ID` 且 MUST NOT 猜測。本 skill 於 `MOBCD-04` 前不寫該檔，致手機端開 A 票後執行 `/uc` 時，`/uc` 讀到**殘留之前一張票 ID**，並將收尾 SOP（勾 AC＋lifecycle sync＋append_note）套用於錯誤的票。**此失效不報錯、不中斷**，故 MUST 由本步機械消除。
- **10.3 消除流程斷點**：步驟 9 後若無規格，Claude 是否提議下一步屬即興行為，實測呈現「有的票跳 `/uc`、有的不跳」之不可預期性。

## 硬性參數約束

- `project` 值 MUST 為字面量 `"SmartCameraApp1"`。理由：MCP 為遠端服務，PLT 與 APP 兩 cwd 連同一 server，不帶 `project` 會回全冊含他專案票。
- MUST NOT 使用 `actionable_only`：其語義為 `lifecycle=="PENDING"` 且 `depends_on` 全數 `CLOSED`/`VOID` 之子集，非單純等價於 `lifecycle=="PENDING"`，語義不透明。
- MUST NOT 使用 `lifecycle="OPEN"` 或任何未經實測之枚舉值——非法值回 `ok:true` + 空集合，不報錯，會被誤讀為「無票」。
- 已實測枚舉集：`DRAFT_NEEDED`／`PENDING`／`IN_PROGRESS`／`REVIEW`／`TICKET_RIGOR`／`CLOSED`，MUST 視為不完全列舉（未來可能新增值）。
- `reviews` 四欄 key 固定為 `qa`／`sv`／`codex`／`traycer`。

## 錯誤處置

- `registry_list` 回 `total=0` 非錯誤，走步驟 2。
- `REGISTRY_REVISION_MISMATCH` → 停止，告知，不自動重試。
- 回傳 `PROJECT_AMBIGUOUS` → 表示 `project` 未寫死，屬本 SKILL 實作缺陷，停止回報。
- 使用者回非數字或超出清單範圍 → 重列清單，MUST NOT 寫入冊面。
- 使用者於步驟 6 回非 `1` → 視為取消，MUST NOT 寫入冊面。
- 步驟 5 之 `grep_files` 回零命中 → 該票缺標題行或 `ALLOWED=` 段，告知票號並續行（摘要降級為僅標題），MUST NOT 改回窗口讀取。
- 步驟 10.1 之 `grep_files` 回零命中 → 該票缺 Verify Config 段，**停止並告知票號**，MUST NOT 以空值寫入 conf。
- `git rev-parse` 不可用或回空 → conf 之 `DIFF_REF` MUST NOT 留佔位字面，停止回報。
- 步驟 10.2 read-back 之 `TICKET_ID` 不符 → 停止並告知，不出選單。

## 範圍邊界

- 本 skill 涵蓋開工與交棒（步驟 1–10），MUST NOT 順手擴充搜尋、模糊比對或其他步驟；步驟上限為 10，新增步驟需 Akira 明示。
- MUST NOT 建立 lock 檔、`selection_state`、session token 或任何外部狀態載體。步驟 10.1 之 `tmp/verify_cmd.conf` 為 `uc` 契約要求之**既有**檔，屬同步而非新建狀態載體。
- 本 skill **不執行票的內容**。步驟 1–10 止於開工與交棒；實際執行由選單導向之 `uc`／`ticket-rigor-gate` 承擔。
