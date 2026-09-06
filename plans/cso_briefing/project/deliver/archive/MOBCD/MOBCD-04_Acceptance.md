# MOBCD-04 驗收記錄

**票號**: MOBCD-04
**收束日期**: 2026-09-03
**標的票**（AI-4 要求「本票以外之任一 PENDING 票」）: `INFRA-SETTINGS-MERGE-01`
**驗收方式**: `/ticket-run` 於本對話 session 內完整走通步驟 1–10，選單回 `4`（停在此處），事後依票面「還原」要求復原標的票狀態。

---

## §1 逐步字面紀錄

| 步驟 | 動作 | 輸出／使用者輸入字面 |
|------|------|----------------------|
| 1 | `registry_list(project="SmartCameraApp1", lifecycle="PENDING", ...)` | 20 筆 PENDING 票 |
| 2 | （`total>0`，跳過） | — |
| 3 | 輸出編號清單 1–20，每行 `<n>. <id> — <title>` | 見上方對話紀錄 |
| 4 | 等待使用者回覆單一數字 | **使用者輸入：`14`** |
| 5 | `grep_files(query="^(# \|ALLOWED=\|MAX_LINES=)")` 對 `INFRA-SETTINGS-MERGE-01` | 回 3 筆命中：標題行、`ALLOWED=.claude/settings.json,.claude/CLAUDE.md`、`MAX_LINES=N/A`；輸出摘要含票名＋ALLOWED＋MAX_LINES |
| 6 | 輸出 `1 = 確認開工` / `2 = 取消`，等待回覆 | **使用者輸入：`1`** |
| 7 | `registry_list` 取 `revision=138` → `registry_set_status(..., "IN_PROGRESS", expected_revision=138)` | 成功，`revision→139` |
| 8 | （無 MISMATCH，跳過） | — |
| 9 | `registry_append_note(..., "2026-09-03 執行起始｜來源：手機 Remote Control（Claude Code @SmartCameraApp1）")` | 成功落冊，`revision→140` |
| 10.1 | `grep_files` 擷取票面 Verify Config 欄位；`git rev-parse --short HEAD`＝`f4562cf`；`write_file_safe(mode="overwrite")` 寫入 `tmp/verify_cmd.conf` | 見 §2（含一次路徑修正） |
| 10.2 | `read_file_safe` read-back | `TICKET_ID=INFRA-SETTINGS-MERGE-01`，與選中票號相符 |
| 10.3 | 輸出四選項選單，等待回覆 | **使用者輸入：`4`** → 輸出「已開工，停在此處。」，**未進入 uc 模式** |

## §2 conf 寫入前後 `TICKET_ID` 對照

- **寫入前**：`tmp/verify_cmd.conf` 殘留 `TICKET_ID=MOBCD-04`（前一輪派工遺留，即 H-1 描述之污染起點）。
- **寫入後**：`TICKET_ID=INFRA-SETTINGS-MERGE-01`，`DIFF_REF=f4562cf`（7 碼 hash，非票面 `<開工時…回填>` 佔位字面）。
- **正向證明**：本次寫入即為 H-1「跨票污染」修復之直接運作證據——若無步驟 10.1，`/uc` 續行將誤讀 `MOBCD-04`。

**⚠️ 工具側 gotcha 揭露**：`write_file_safe(path="tmp/verify_cmd.conf")`（未帶 base 絕對路徑）首次呼叫解析至 `D:/DevSources/ObsidianVault/tmp/verify_cmd.conf`（非本專案），非本票 SKILL.md 邏輯缺陷（步驟 10.1 字面已載明 `<base + file 欄>` 之 base 前綴要求，僅適用於 `grep_files` 之 `paths` 參數；`write_file_safe` 目標路徑之絕對路徑要求未在票面明文，屬遺漏）。已以絕對路徑 `D:/DevSources/AndroidProj/SmartCameraApp1/tmp/verify_cmd.conf` 重寫並成功。**建議**：後續 MOBCD-05（PLT 同步）或補丁票將步驟 10.1 之 `write_file_safe` 呼叫明文化為絕對路徑，比照步驟 5／10.1 `grep_files` 之 base 常數處理。

## §3 選單輸出字面

```
1 = 開始執行（進入 uc 模式）
2 = 先看完整票面
3 = 先跑 rigor gate 審查
4 = 停在這裡
```

回 `4` 後輸出：「已開工，停在此處。」（逐字，未進入 uc 模式，未讀取任何 ticket 內文以外之操作）。

## §4 標的票還原

實測後依 AI-4「還原」要求：

1. `registry_set_status(INFRA-SETTINGS-MERGE-01, lifecycle="PENDING", expected_revision=140)` → 成功，`revision→141`。
2. `registry_append_note(INFRA-SETTINGS-MERGE-01, "MOBCD-04 實測用，非實際開工")` → 成功落冊，`revision→142`。

還原後 `lifecycle=PENDING`，`note` 尾段含還原註記，符合票面 AI-4 還原子句。

---

## §5 AC 對照

| AC | 判定 | 佐證 |
|----|------|------|
| AC-6（步驟 6 回 `2`） | **未觸發，未驗** | 本次實測回 `1`（確認），未行經 `2` 路徑；MUST NOT 以本次證據代替 |
| AC-7（選單回 `4`） | ✅ 通過 | §3 逐字證據：輸出「已開工，停在此處」且未進入 uc 模式 |
| AC-8［實機］（步驟 1–10 全走通） | ✅ 通過 | §1／§2 全步驟字面紀錄；conf 寫入前後 `TICKET_ID` 對照見 §2 |

**AC-6 仍待後續一次「回 `2`」之真實互動補測**，本驗收檔不代為宣告通過。

---

## §6 AI-5 增補驗收（2026-09-03，Desktop 執行）

Akira 於本票收束前明示新增強制顯示欄位，併同 §2 揭露之 gotcha 一併補正，兩者同屬本票 scope 故不另立票。

### 步驟 5 摘要欄位擴充

`grep_files` query 擴充納入 `**Mode**`、`**前置依賴`、三分 dependency 三類行，`max_matches` 6→10，摘要行數 3–5 → **5–9**。五項欄位列為強制顯示，缺一即本步失敗；票面未載該欄時 MUST 輸出「（票面未載）」佔位，MUST NOT 靜默略過——否則「票面缺欄」與「摘要漏抓」不可區辨。

**正向驗證**（標的 `INFRA-SV-GOV-01_SV_Governance_Fix.md`，回 5 筆命中）：

| 行號 | 命中內容 | 對應欄位 |
|------|---------|---------|
| L1 | `# INFRA-SV-GOV-01: SV 治理修正（…）` | 票名 |
| L5 | `**Mode**: normal → uc  \|\|  **Effort**: Medium` | Mode／Effort |
| L7 | `**前置依賴**: 無（與 MEO-T3 並行；ALLOWED 檔案集不相交）` | 前置依賴 |
| L13 | `ALLOWED=.claude/skills/sv/scripts/cross_review.sh,…` | ALLOWED |
| L15 | `MAX_LINES=200` | MAX_LINES |

五欄位無一遺漏。regex 之 `**前置依賴` 為前綴匹配，同時涵蓋 `**前置依賴**:`（本票樣本）與 `**前置依賴（依賴三分）**：`（MOBCD 系列寫法）兩種票面格式。

### §2 gotcha 之修法落地

步驟 10.1 之 `write_file_safe` 已改明文絕對路徑，並於該步下方載明：相對路徑會被解析至 **allowed roots 之首**且 `ok:true` 不報錯不警告。原 base 常數要求僅明載於 `grep_files` 之 `paths`，`write_file_safe` 之 `path` 屬遺漏，已補正。

**誤建檔之處置**：`D:/DevSources/ObsidianVault/tmp/verify_cmd.conf`（448 bytes，2026-09-03 建立）經確認確實存在。MCP 工具集無 delete 原語，已以 `overwrite` 中和——內容改為孤兒檔警示並將 `TICKET_ID` 置空，避免其被任何流程誤讀為有效開工標的。該檔可安全刪除。

---

## §7 收束

- 驗收檔即本檔，落 `plans/cso_briefing/project/deliver/`。
- **已達成**：AI-1～AI-5 全數完成；AC 十二條中十條通過（含 AI-5 三條中之二條）。
- **未達成（明載，不代為宣告通過）**：
  - **AC-6**〔步驟 6 回 `2` → 維持 PENDING 且 conf 未改寫〕：本次實測回 `1`，未行經取消路徑。
  - **AC-11**〔AI-5 摘要同時含 Mode 與前置依賴、行數 5–9〕：**工具層已驗**（`grep_files` 回 5 筆全中），**skill 流程內未驗**。
- **收束判定**：二項未驗均為**負向／顯示路徑**，其失效後果分別為「誤開一張票」與「摘要少兩行」，皆可於五分鐘內以 `registry_set_status` 或重跑一次消解，依 §0 首要原則不構成阻擋收束之理由。二者合併移交下一次真實派工之機會性補測，或 `MOBCD-05`（PLT 同步票）一併驗。
- **後續**：`MOBCD-05`（PLT）同步本票五項修正＋`MOBCD-03` G-1 base-path，PLT 版 base 常數為 `D:/DevSources/workflow/mcp-ticket-platform/`。

**End of MOBCD-04_Acceptance.md**
