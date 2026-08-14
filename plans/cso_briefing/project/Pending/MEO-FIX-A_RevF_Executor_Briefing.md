# MEO-FIX-A Rev F — 執行者派工 Briefing（Executor Dispatch Briefing）

**日期**: 2026-07-25 ｜ **簽發**: CSO ｜ **配套票**: `Pending/MEO-FIX-A_Escalation_Gate_Relocation.md`（**Rev F**，SHA-256 `71054F44...`）
**授權**: Codex Rev F 復審 **PASS**（`reports/MEO-FIX-A_RevF_Codex_ReReview_20260725.md`，9/9 驗收邊界），票面設計階段 `REPORT_FIX` **解除**；Akira 2026-07-25 核准派工。
**路由**: AI-0 → `/uc` 實作 → `/qa` → `/sv` → Codex 實作 cross-review → Traycer execute_review
**紀律**: 票面為 SSOT；本 Briefing 綁定 Codex 復審報告 §7 之 A1–A5 執行核驗點（該報告為你的必載文件）。兩者若與票面衝突 → 停止回報。

## 一、A1–A5 綁定（含重開條件——違反即 REPORT_FIX 重開，全輪重來）

**A1 — 全成全敗 MUST 為實際檔案原子性（最高風險項）**
- **MUST NOT** 以多次 live `sed -i`／`kv_set` 逐鍵寫入後宣稱原子——票面 §A 已載明 `kv_set` 就地改寫「本身即非原子」。
- 推薦實作（或同等 crash-consistency 方案）：鎖內建 STATE temporary copy → 五步修改全作用於 copy → 對 copy 終驗 → **單次 atomic replace** 原 STATE。
- `/sv` **MUST** 注入三情境並證明原 STATE 不變：① decision materialization 後失敗；② watermark 更新中失敗；③ pending clear 前失敗。
- **重開條件**：live file 逐鍵 best-effort 寫入 → REPORT_FIX 立即重開。

**A2 — Windows canonicalization 四形驗證**
- 實作 MUST 驗證：drive-letter 大小寫／`\`↔`/` 正規化／repo root 尾斜線／`.` 與 `..` 詞法正規化。
- **重開條件**：任一形式可繞過 exact/prefix guard → 重開 M1。

**A3 — External producer boundary（開工時再實測，禁引用復審輪取證）**
- AI-0.2：`claude mcp get mcp-ticket-platform` MUST 回報未配置；已配置 → STOP，禁改 FORBIDDEN settings，另立 D5 票。

**A4 — Dirty worktree disposition（已由 Akira 裁決消解）**
- `tools/.claude/`（untracked）：經 Akira 確認為先前安裝後移除之某 MCP 暫存殘留，**已由 Akira 移除**；Claude Code／VS 相關 MCP 經確認正常運作。你開工時若見 worktree 已無此目錄＝符合預期；**若仍存在 → 停止回報**（與裁決狀態不符），**MUST NOT** 自行清理。
- `.claude/PROGRESS.md`（modified）：滾動 S-row 屬 session 代謝，保留其所有權；**MUST NOT** 夾帶入本票 commit。回填 DIFF_REF 後若 scope verifier 將其視為本票 payload → 停止並取得 disposition。

**A5 — Boot 序（＝票面 AI-0.3/0.4，原子組）**
- 回填當時 HEAD 為 DIFF_REF → 重建 `REV=F` conf → 記錄 STATE baseline SHA-256 → drift guard。缺一即停。

## 二、α 過渡授權狀態更新（Rev F 生效）

- **比對域已字面化**（Rev F Error Handling α 條款）：`MEO_LAST_ERROR_CLASS` ⟷ 字面值 `Exit` 字面等值；合格清單僅此一項；禁語義擴張、禁正則/子字串近似。
- **第 3 次額度已解凍**。動用時 MUST 逐項存證並**明引 Rev F α 條款**。
- **⚠️ 載體降級**：你的記憶檔 `meo-escalation-transitional-alpha`（2026-07-22）自即日起為**非權威轉錄**，**MUST NOT** 於任何存證欄引為「裁決」或「常設裁決」；建議於該檔頂部自行加註降級標記。你上輪拒用該檔為證據之判斷經 CSO 肯認——此即正確行為的常設化。
- α 於本票落地後自然退役。

## 三、執行紅線提醒（票面之導讀，非替代）

1. §C.5 五步順序為硬約束：**寫 `MEO_LAST_ESCALATION_DECISION` 先於清 `PENDING`**（record-before-clear）；任一步敗 → 回退「未 release」，禁部分寫入。
2. AI-3 §3：**禁相對前綴比對**（永不命中）；檔案級 exact match、目錄級 prefix match（尾斜線）；canonical root 無法確定 → STOP，禁猜測。
3. 觸發 release 驗證之原始 tool call **仍回 `deny`**；禁以 `allow` 讓被攔截工具代執行 transition。
4. 禁清零 `STREAK`、禁動閾值、禁增第四 Stop hook、禁碰 FORBIDDEN 七項。
5. commit 前綴 `[MEO-FIX-A]`；禁混 `.kt/.xml`。

## 四、Purpose Statement

本次派工目標：完成 MEO-FIX-A Rev F 全部 AI-0～AI-5 並通過 `/qa`→`/sv`（含 A1 三情境注入），產出可送 Codex 實作 cross-review 之完整證據欄。
預期帶回：AI/AC 逐項勾稽證據＋SV 報告＋四方同 ID read-back 紀錄。
