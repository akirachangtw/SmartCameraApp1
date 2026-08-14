# INFRA-CONCURRENCY-01: 並行 Session 治理規約立法（Concurrency Governance Legislation）

**簽發日期**: 2026-07-19
**簽發者**: Claude（CSO session，Akira 2026-07-19 對話裁決「開票 INFRA-CONCURRENCY-01」授權）
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: execute_review（.claude/rules/ 新增 = 治理票）
**前置依賴**: 決策 SSOT `2026-07-16_Parallel_Session_Interference_SixCases`（六例歸因＋五項範圍定案）＋ `2026-07-17_TPRG_Case2_And_Concurrency_Case7_ReceiptBreak_Case2`（案例⑦＋回執斷鏈第二例）

> 定位聲明：本票為**純立法票**——產出單一規約 SSOT 檔，條文化既有裁決。任何機械化執行件（lint、hook、R0 Pre-flight 讀取器）一律「登記待開票」，MUST NOT 本票夾帶。素材共九例：並行干擾七例（①GAP-11 L1 ②HEADROOM PROGRESS.md 衝突 ③零 diff Check B ④DIFF_REF anchor drift ⑤scratch 互刪 ⑥共享狀態檔並行追加 ⑦同檔 working tree 跨票混雜）＋跨 session 回執斷鏈兩例（GAP-06 系／COMPRESS-01 fc78525 回執未流回治理線）。

## Verify Config
TICKET_ID=INFRA-CONCURRENCY-01
CREATED=2026-07-19
PRESET=refactor
ALLOWED=.claude/rules/concurrency.md
FORBIDDEN=.claude/CLAUDE.md,.claude/settings.json,.claude/PROGRESS.md,.claude/rules/decommission.md,.claude/rules/cross-review.md,.claude/skills/qa/SKILL.md,.claude/skills/sv/SKILL.md,.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/sv/scripts/cross_review.sh,.claude/agents/cross-reviewer.md
MAX_LINES=120
DIFF_REF=<開工前 MUST 現場 `git rev-parse --short HEAD` 直取填入；禁沿用他票 ref 或憑記憶填值——依「簽發時可行性缺檢」教訓＋KAIROS 候選④快照殭屍化防護>

## Action Items

### AI-1: 建立 `.claude/rules/concurrency.md`（並行治理規約 SSOT）
- **檔案**: `.claude/rules/concurrency.md`（新建）
- **現值**: 不存在。管線世界模型不存在「並行 session」實體——錨點、scratch、票務狀態、共享狀態檔全部隱含單 session 假設（六例 SSOT 根因定式）
- **目標值**: 規約含以下六章，**每條 MUST 標註事件支撐編號**（①–⑦／回執斷鏈 R1–R2），禁無實例條款：

  **§1 Scratch 隔離制**
  - 每票 scratch MUST 使用 `tmp/<TICKET_ID>/` 專屬子目錄
  - 全域 glob 清理（如 `rm tmp/*` 系）列 FORBIDDEN，措辭 MUST 可被 grep-level lint 機械匹配
  - Auto-cleanup MUST 排除 `sv_report_<TICKET_ID>.md` 型自訂命名 artifacts（吸收 Traycer D15 CANDIDATE RULE #2）
  - 事件支撐：⑤
  
  **§2 Worktree／分支串行令**
  - 同 worktree 同分支：同家族票 MUST 串行；跨家族票並行 MUST 改用 git worktree 隔離（既有工具鏈能力，零新依賴）
  - 事件支撐：④⑤⑥⑦
  
  **§3 票務生命週期單向流**
  - 票執行落地即移出 `Pending/`（Pending→deliver 單向，杜絕「無票」誤報；欠帳先例 DOCSYNC-02）
  - 跨 session 執行完成之回執 MUST 於完成當刻流回治理線（PROGRESS 條目或裁決歸檔），禁「已收尾但治理線不知情」中間態
  - 事件支撐：回執斷鏈 R1–R2
  
  **§4 並行開工佔用宣告**
  - 並行開工 MUST 先落佔用宣告：ticket ID＋branch＋觸碰面（ALLOWED 清單），格式 token 化（供未來 R0 Pre-flight 機械讀取；讀取器本票不實作，登記待開票）
  - 事件支撐：④⑤⑥
  
  **§5 共享狀態檔寫入規約**
  - PROGRESS.md 等共享狀態檔：append 前 MUST 現場 `wc -l` 直取現值（禁憑票面快照），並行期間單寫入者宣告
  - 事件支撐：②⑥
  
  **§6 未建模狀態禁用令（條文化六例 SSOT 決策 3）**
  - 任何裁決／票面創造管線世界模型外的狀態（partial 票、合法零 diff、越錨快照等）MUST 先建模（宣告 token＋閘門分支）後使用；先例：ZERO-DIFF-DECLARED、Addendum 快照制
  - 事件支撐：①③④（至少四例直接源於此，見 SSOT）

- [x] 完成

## Hard Constraints
1. 本票 `.md` 觸碰僅限 `.claude/rules/concurrency.md` 單檔新建；lint／hook／Pre-flight 讀取器等機械化件 MUST NOT 夾帶，僅於結論登記待開票清單
2. 條文 MUST 逐條附事件支撐編號；無實例支撐之推測性條款 MUST NOT 入文
3. 條文若與既有已批准裁決字面衝突 → 停止回報附兩造原文，不得自行仲裁改寫既有裁決
4. 不動 `.claude/CLAUDE.md`（規則採納屬 KAIROS 收割權限，本票僅立規約檔）

## Error Handling
- 九例對照表任一例無法在決策庫定位原始歸檔 → 停止回報缺口清單，不得憑記憶補寫事件細節
- 發現 §1–§6 任一章需連動修改既有 skill/腳本才能生效 → 記入結論＋待開票，本票僅完成規約文本
- DIFF_REF 開工時發現 working tree 有他票未收口變更（案例⑦條件）→ 停止回報，等待串行

## Acceptance Criteria
- [x] **[內容性]** `concurrency.md` 含 §1–§6 全六章，每條附事件支撐編號，九例全數被至少一條引用（對照表附文末）
- [x] **[機械性]** §1 全域 glob FORBIDDEN 條款措辭通過機械匹配自測：附建議 grep pattern 並實測命中條款本文
- [x] **[行為性]** 總行數 ≤120（`wc -l` 現場直取回報，總行數＋構成比雙值）
- [x] **[隔離性]** `git diff --stat` 僅涉及 `.claude/rules/concurrency.md`
- [x] **[承接性]** 結論列出待開票清單（至少含：R0 Pre-flight 佔用宣告讀取器、glob-rm lint、Pending/ 生命週期自動搬遷）

## Closure

- Codex Cross-Review: R1 **PASS**（REACHABILITY: NONE）
- `structural_verify.sh --post`: PASS（ADVISORY）
- Commit: `1651b02`（code）+ `09fa5b4`（docsync）
- Traycer 終審: ✅ Well Implemented（Akira 2026-07-19 確認，T2）
- 票檔搬移: `deliver/`（本次收尾）
