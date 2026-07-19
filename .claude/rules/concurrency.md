> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Concurrency Governance — 並行 Session 治理規約

事件支撐依據：`2026-07-16_Parallel_Session_Interference_SixCases`（Obsidian CSO）＋
`2026-07-17_TPRG_Case2_And_Concurrency_Case7_ReceiptBreak_Case2`（Obsidian CSO）。
①–⑦為並行干擾七例，R1–R2 為跨 session 回執斷鏈兩例。九例對照表見文末。

## §1 Scratch 隔離制

- 每票 scratch **MUST** 使用 `tmp/<TICKET_ID>/` 專屬子目錄。
- 全域 glob 清理（如 `rm tmp/*`、`rm -rf tmp/*`）**MUST NOT** 使用。
- Auto-cleanup **MUST** 排除 `sv_report_<TICKET_ID>.md` 型自訂命名 artifacts。
- 事件支撐：⑤

## §2 Worktree／分支串行令

- 同 worktree 同分支：同家族票 **MUST** 串行執行，禁止同時開工。
- 跨家族票需並行時 **MUST** 改用 `git worktree` 隔離（既有工具鏈能力，零新依賴）。
- 事件支撐：④⑤⑥⑦

## §3 票務生命週期單向流

- 票執行落地即 **MUST** 移出 `Pending/`（Pending→deliver 單向），杜絕「無票」誤報。
- 跨 session 執行完成之回執 **MUST** 於完成當刻流回治理線（PROGRESS 條目或裁決歸檔），
  **MUST NOT** 留下「已收尾但治理線不知情」中間態。
- 事件支撐：R1（GAP-06 系回執斷鏈）R2（COMPRESS-01 `fc78525` 回執延遲流回）

## §4 並行開工佔用宣告

- 並行開工 **MUST** 先落佔用宣告：ticket ID＋branch＋觸碰面（ALLOWED 清單），格式 token 化
  供未來 R0 Pre-flight 機械讀取。
- 讀取器本身 **MUST NOT** 本票實作，列入待開票清單。
- 事件支撐：④⑤⑥

## §5 共享狀態檔寫入規約

- PROGRESS.md 等共享狀態檔：append 前 **MUST** 現場 `wc -l` 直取現值，禁憑票面快照。
- 並行期間 **MUST** 宣告單一寫入者。
- 事件支撐：②⑥

## §6 未建模狀態禁用令

- 任何裁決／票面創造管線世界模型外的狀態（partial 票、合法零 diff、越錨快照等）
  **MUST** 先建模（宣告 token＋閘門分支）後使用；先例：`ZERO-DIFF-DECLARED`、Addendum 快照制。
- 事件支撐：①③④

## 待開票清單

1. R0 Pre-flight 佔用宣告讀取器（§4 token 格式之機械讀取實作）
2. Scratch 全域 glob FORBIDDEN 之 lint/hook 化（§1 措辭之強制執行）
3. `Pending/` → `deliver/` 生命週期自動搬遷（§3 單向流之機械化）
4. §5 TOCTOU 缺口修補：現行「append 前 `wc -l` 直取現值」為 check-then-use，讀值與實際寫入間無原子鎖，並行寫入者仍可能於間隙交錯；需設計鎖機制或等效序列化手段（如檔案鎖、單寫入者佇列）方能真正杜絕競態，「宣告單一寫入者」目前僅為程序約定，非機械強制。本項由 `INFRA-CR-R0-PRECLEAR-01` 涵蓋（A+ snapshot-then-verify 方案採納，Akira 2026-07-20 對話裁決），**不另立票**——後續 session 見本條目 **MUST** 不得重複開票，如需實作請直接承接 `INFRA-CR-R0-PRECLEAR-01`

## 九例對照表

| 編號 | 案例 | 章節 | 佐證（repo 內 corroborating evidence） |
| --- | --- | --- | --- |
| ① | GAP-11 L1 | §6 | `cross-reviewer.md`、`INFRA-SV-ZERODIFF-01.md`（SSOT 原文止於標籤層級，無事件敘述） |
| ② | HEADROOM PROGRESS.md 衝突 | §5 | `KAIROS_D15_Headroom_Decommission_Retrospective.md` |
| ③ | 零 diff Check B | §6 | `INFRA-SV-ZERODIFF-01.md` |
| ④ | DIFF_REF anchor drift | §2§4§6 | `KAIROS_D15_...md`（CSO 裁決 B）、`SESSION_HANDOFF_20260719_1756.md` |
| ⑤ | scratch 互刪 | §1§2§4 | `KAIROS_D15_...md` Error Pattern 2 / Rule Candidate 2 |
| ⑥ | 共享狀態檔並行追加 | §2§4§5 | `SESSION_HANDOFF_20260719_1756.md`、`INFRA-PROGRESS-COMPRESS-01_Dashboard_LineCap_Archive.md` |
| ⑦ | 同檔 working tree 跨票混雜 | §2 | `SESSION_HANDOFF_20260719_1756.md`（07-17 SSOT 確認為獨立第七例，止於標籤層級） |
| R1 | GAP-06 系回執斷鏈 | §3 | `.claude/PROGRESS.md` Next Actions #9 |
| R2 | COMPRESS-01 `fc78525` 回執未流回治理線 | §3 | `INFRA-PROGRESS-COMPRESS-01_Dashboard_LineCap_Archive.md` |

原始決策 SSOT 存於 Obsidian CSO（非 git-tracked），逐字全文未併入本檔；上表佐證欄位為
repo 內可核驗之 corroborating evidence，非 SSOT 原文引用。
