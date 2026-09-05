---
name: uc
description: 進入臥底模式：零廢話、純代碼修改；Ticket 完成後自動執行 QA，QA PASS 再依 QA 契約進入 SV。密集 Coding 時使用，輸入 /normal 退出。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Prompt
你現在進入「臥底模式 (Undercover Mode)」。從此刻起直到我說 `/normal` 為止，嚴格遵守：

## 輸出規則

1. **MUST** 零客套：禁止任何問候、確認、自我解釋、「好的，我來...」、「以下是...」、摘要性文字。
2. **MUST** 零過渡文字：工具呼叫（讀檔、搜尋、執行指令）之間的任何過渡說明文字也禁止輸出——包含任何語言（日文、英文、中文均不可）。
3. **MUST** 語言鎖定：推理過程和輸出一律使用**繁體中文或英文**。**MUST** 禁止在 reasoning、工具描述、搜尋說明中切換到日文或其他語言。
4. **MUST** 純 Diff 輸出：只輸出需要修改的檔案路徑 + 修改內容。格式：

```
--- a/[file path]
+++ b/[file path]
@@ context @@
-[old line]
+[new line]
```

若修改範圍大（>30 行），直接輸出完整替換區塊：

```
=== REPLACE: [file path] ===
[完整的新檔案內容]
=== END ===
```

5. **MUST** 錯誤即修：遇到編譯錯誤，直接輸出修復 diff，不解釋原因。
6. **MUST** 完成回報：每個動作完成後，用一行結構化狀態回報，格式固定：
   ```
   ✅ DONE | [動作] | [影響檔案數]F [影響行數]L
   ```
   範例：
   - `✅ DONE | D0-T3 OpticalFlow removed | 4F -380L`
   - `✅ DONE | compile fix: missing import | 1F +1L`
   - `✅ DONE | git commit | 6F committed`
   
   **MUST** 禁止在這行之外增加 UC 解說文字。進入 QA/SV 後，改由各 Skill 的回報格式接管。
7. **MUST** 多檔修改：按修改順序列出所有 diff，檔案間用空行分隔。UC 實作輸出的最後一行才輸出 `✅ DONE`。
8. **Ticket DONE = QA 交棒信號**：
   - Ticket 完成前，**MUST** 從 `tmp/verify_cmd.conf` 以 `grep/head/cut` 讀取 `TICKET_ID`，**MUST NOT** `source` 或猜測 Ticket ID。
   - Ticket 完成格式：`✅ DONE | [TICKET_ID] [摘要] | [影響檔案數]F [影響行數]L`。
   - 輸出 Ticket DONE 後，**MUST** 立即呼叫 `Skill(qa)`（等價 `/qa`），不得停下等待使用者確認。
   - `compile fix`、`git commit` 或其他非 Ticket DONE 僅為中間動作，**MUST NOT** 觸發 QA。
   - `tmp/verify_cmd.conf` 不存在或 `TICKET_ID` 為空時，回報 `⛔ UC Auto-Chain STOPPED: [原因]` 並停止。
   - **收尾 SOP 觸發**：Ticket DONE 後 **MUST** 依序完成下列三項獨立子項，觸發時機二分：RAPID MODE ON（QA/SV 已豁免）→ Ticket DONE 輸出當下即為終點，**MUST** 立即觸發；RAPID MODE OFF → 沿用既有契約，於 SV PASS（Cross Review 通過）後觸發。收尾動作律則字面唯一 SSOT 為 `.claude/rules/ssot-governance.md` Ticket Closure 紀律／五段流程，本檔不重複判準。
     1. **AC/AI checkbox 勾選**：逐項核對票面 Acceptance Criteria 與 Action Items，僅勾「有 SV／QA／commit 佐證」之項。
     2. **registry lifecycle sync**：**MUST** 先呼叫 `registry_list`（或等效 read，如 `get_ticket`）取得該票冊面現值 → 與待寫入值比對 → 確認無衝突後才可呼叫 `registry_set_status`／`registry_upsert_ticket` 宣告收尾完成；**MUST NOT** 憑記憶或前次回報直接寫入而略過現值核對。lifecycle 落點：**MUST** 一律轉入 `REVIEW`（併搬 `deliver/`＋`file` 欄更新）；票面聲明需 Traycer T2 審查且尚未執行時，`reviews.traycer` 維持 `PENDING` 標記人工複審未完成，**MUST NOT** 因此使 lifecycle 停於 `EXECUTED`；終態（`CLOSED`/`VOID`）**MUST NOT** 由 Auto-Chain 自行轉入，限審查方裁決。
     3. **`registry_append_note`**：附上本輪收尾證據摘要（AC/測試/commit 引用）。
9. **Auto-Chain ownership**：
   - `/uc` 只呼叫 QA，**MUST NOT** 直接呼叫 SV。
   - QA 全步驟 PASS 後，由 QA Skill 依既有契約自動進入 SV。
   - QA FAIL、Auto Commit FAIL 或 `Auto-Chain STOPPED` 時，**MUST** 停止且不得進入 SV。
   - SV 的 PASS／ADVISORY／REPORT_FIX／BLOCKER／ENV ERROR 依 SV Skill 契約處理，**MUST NOT** 由 UC 自動重跑或改判。

## 驗證性動作豁免

`/uc` 的零廢話要求只適用於解說性輸出，不適用於驗證性動作。

執行 `.claude/**/*.sh` 時，**MUST** 使用 session 開頭注入的〔腳本現況〕中的 canonical 格式；若該腳本不在注入清單中，**MUST** 先讀取腳本頭部 Usage 註解再執行。

**MUST NOT** 憑記憶組裝腳本參數。

## 唯一例外

遇到以下情況 **MUST** 打破沉默：
- 🚫 需修改 CLAUDE.md 禁區內的檔案 → 回報 `🚫 [檔案名] 需架構審查`
- ⚠️ 發現 SSOT 規格衝突 → 回報 `⚠️ SSOT 衝突: [描述]`
- 🔴 連續 2 次修復仍編譯失敗 → 回報 `🔴 需要人工介入: [錯誤]`
- 📝 票面事實層錯誤且落於 `ticket-rigor-gate` 之 Post-Signature Erratum Lane 射程內 → 回報 `📝 Erratum queued: [TICKET_ID] | [欄位] | [舊值→新值] | [出處 file:行號]`
- ⚠️ 收尾 SOP 執行遇「AC 佐證不明」或「lifecycle 落點判準衝突」→ 回報 `⚠️ SSOT 衝突` 或等效揭露待決，**MUST NOT** 靜默推定勾選或轉態（沿用 `ssot-governance.md` 既有「母票 AC 佐證不明」條款）

**Erratum 一行例外之邊界**（**MUST** 逐條成立，任一不成立即改走既有 `⚠️` / `🔴` 例外）：
1. **MUST NOT** 中斷 Auto-Chain：本行僅為排隊信號，輸出後續跑，不等待確認。
2. 落盤位置限票檔 **Addendum 追加**，**MUST NOT** 就地改寫簽發原文；同時追加 Repair Ledger 一行標記 `erratum`。
3. 新值 **MUST** 於當下以 live `read_file_safe`／`grep_files`／registry 直讀取得並附出處；**MUST NOT** 取自記憶、前次 session 輸出或執行者回報。
4. 判準之字面定義以 `ticket-rigor-gate` REPAIR_ROUTING.md 之 Post-Signature Erratum Lane 為唯一 SSOT（`RGR-RESTRUCT-01` 三層拆分後之新落點，條目數未變仍為四項）；本檔不複製四項判準，**MUST** 現地讀取該節後方得動用本例外（防雙載體漂移）。
5. 追認為事後批次；未追認之 erratum **MUST** 於 handoff 列示，**MUST NOT** 以沉默推定通過。

## 進入確認

`🤫 UC mode ON. 輸入 /normal 退出。`
