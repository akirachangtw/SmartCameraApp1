# INFRA-MAXLINES-EVIDENCE-01: 量測 evidence record 綁定層（ticket ＋ worktree ＋ commit SHA ＋ DIFF_REF）

**簽發日期**: 2026-08-13
**簽發者**: CSO（Akira 2026-08-13 對話裁決授權簽發）
**來源議題**: INFRA
**Mode**: normal → uc  ||  **Effort**: High
**Traycer Workflow**: N/A（治理／腳本票，依 governance-ticket-traycer-exempt 先例）
**前置依賴（依賴三分）**：
- `execution_dependency`: `INFRA-MAXLINES-GATE-01` —— 本票持久化之遙測值形狀由該票 AI-2 四分支輸出定義，需先落地
- `evidence_dependency`: `INFRA-TPL-V33-02` —— TPL v3.5「量測取證綁定」條款為本票判準來源；`.claude/rules/concurrency.md` §1 律 B（receipt MUST 於收口當刻複製進耐久載體）為同源規約
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=INFRA-MAXLINES-EVIDENCE-01
CREATED=2026-08-13
PRESET=refactor
ALLOWED=verify_checklist.sh,SKILL.md
FORBIDDEN=Unified_Ticket_Template_v3.md,TICKET_REGISTRY.json,PROGRESS.md,concurrency.md
MAX_LINES=80
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-08-13 17:17

**MAX_LINES 推導註記**（估算參考，非判準）：AI-1 evidence record 產出 ~35 行 → AI-2 worktree／SHA 綁定 ~15 行 → AI-3 SKILL.md 契約段 ~10 行 → 小計 60 → 緩衝 1.3 倍 → 進位 80。

## 問題陳述

2026-08-13 終局裁決要求：每次量測 MUST 形成與 **ticket ID ＋ worktree ＋ commit SHA ＋ DIFF_REF** 綁定之 evidence record，以支援跨 session 並行、Cross-Review 重放與事後審計；**MUST NOT** 以共享工作樹、共享可變設定檔或即時 `git diff` 作為**唯一**取證來源。

現況三項禁忌全數命中：
1. **唯一取證來源即為即時 `git diff`** —— `[0.3]` 量測結果僅印至 stdout，隨 session scrollback 滅失，無持久化。
2. **設定載體為共享可變檔** —— `tmp/verify_cmd.conf` 為全域單槽位，跨票覆寫已有實證（concurrency.md §1 律 B：同日兩次覆寫 ＋ 一次執行者 STOP）。
3. **無 worktree／SHA 綁定** —— 並行 worktree 下無法區辨量測出自哪個工作目錄、哪個 commit 端點。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **動作**: 逐字確認下列現值命中數：
  - `MAX_LINES_CONF=$(get_conf "MAX_LINES" "$CONF_FILE")` —— 唯一（1 次）
  - `echo "✅ Step 0 — Scope 驗證通過，繼續 /qa Step 1"` —— 唯一（1 次）
  - `INFRA-MAXLINES-GATE-01` 之四分支遙測輸出（`⚪`／`⚠️`／`✅ 實測`）**MUST** 已存在——若不存在，表示 `execution_dependency` 未落地 → **停止回報**
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成

### AI-1: evidence record 產出
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **現值**: `[0.3]` 量測結果僅 `echo` 至 stdout，無持久化落點
- **目標值**: 於 Step 0 收尾寫出一筆 append-only evidence record 至 `tmp/<TICKET_ID>/evidence/step0_<commit_sha>.txt`（scratch 隔離制 §1：每票專屬子目錄），內容 **MUST** 含六欄位：`TICKET_ID` / `WORKTREE`（`git rev-parse --show-toplevel`）/ `COMMIT_SHA`（`git rev-parse HEAD`，全長）/ `DIFF_REF` / `DIFF_END_RESOLVED` / `MEASURED_LINES`（insertions＋deletions，production-only）
- **關鍵**: 檔名含 `<commit_sha>` 使同票不同端點各自留痕，**MUST NOT** 覆寫既有 record（append-only，禁單槽位）
- [x] 完成

### AI-2: worktree 綁定與並行區辨
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **現值**: 全檔無 `git rev-parse --show-toplevel` 呼叫，無 worktree 識別
- **目標值**: 取 `--show-toplevel` 為 `WORKTREE` 欄位值；當 `WORKTREE` 與 `tmp/` 所在 repo root 不一致（跨 worktree 執行）→ evidence record **MUST** 落於當前 worktree 之 `tmp/` 下，並於 stdout 明示落點路徑供 Cross-Review 重放引用
- [x] 完成

### AI-3: /qa 契約明文化
- **檔案**: `.claude/skills/qa/SKILL.md`
- **現值**: Step 0 段落未載 evidence record 產出義務與落點
- **目標值**: 於 Step 0 描述補一段：evidence record 落點格式、六欄位清單、以及「收口三件套之 receipt MUST 於收口當刻複製進耐久載體，禁僅留路徑引用」之交叉引用（concurrency.md §1 律 B）
- [x] 完成

## Hard Constraints
1. **MUST NOT** 使 evidence record 缺失成為 Step 0 阻斷理由——本票建立取證能力，`evidence 缺失` 之阻斷權屬交付閘（五類第②項），非 Step 0 之 MAX_LINES 路徑。
2. **MUST NOT** 以 `tmp/verify_cmd.conf` 或任何全域單槽位檔作為 evidence record 落點（律 B 明禁）。
3. **MUST NOT** 觸碰 `INFRA-MAXLINES-GATE-01` 已落地之四分支判定邏輯（僅新增持久化，不改判定）。
4. **MUST NOT** 於同一 commit 混入 `.kt`／`.xml` 變更。

## Error Handling
- `execution_dependency`（`INFRA-MAXLINES-GATE-01`）未落地 → 停止回報，禁在舊 gate 結構上疊加 evidence 層
- `tmp/<TICKET_ID>/` 目錄建立失敗 → 停止回報（禁 fallback 至共享 `tmp/` 根目錄）
- evidence record 落點已存在同名檔且內容不一致 → 停止回報（並行競態徵兆，禁覆寫）
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）

## Acceptance Criteria
- [x] **[存在性]** `verify_checklist.sh` 含 `git rev-parse --show-toplevel` 與 `git rev-parse HEAD`，各 ≥1 次（L459/L460）
- [x] **[可達性]** evidence record 之六欄位值均有 production producer（非 hardcode／非空字串）；**producer 證據欄**：`WORKTREE`=L459、`COMMIT_SHA`=L460、`TICKET_ID`=conf 讀值（qa/scripts/verify_checklist.sh L168）、`DIFF_REF`/`DIFF_END_RESOLVED`=既有變數（L178/L206）、`MEASURED_LINES`=`$ADDED`（L428-448）
- [x] **[行為性]** 實跑 `/qa` Step 0 後，`tmp/INFRA-MAXLINES-EVIDENCE-01/evidence/step0_fbcb6121a67c60f1c8122ddaa027ddbd1d2fff2d.txt` 存在且六欄位齊備；`MEASURED_LINES=36` 與 `git diff --numstat 3df34a6..fbcb612` 手算 `10+26=36` 相符
- [x] **[行為性]** 同票以兩個不同 `commit_sha`（`fbcb612…`／`8b67da3…`）各跑一次 → 產生**兩筆**獨立 record（`step0_fbcb6121....txt`／`step0_8b67da30....txt`），無覆寫
- [x] **[行為性]** evidence record 缺失或寫入失敗時，Step 0 exit code **不因此**為非零（Hard Constraint 1）——設計驗證：mkdir/write 失敗路徑僅 echo 至 stderr 不觸碰 `$FAIL`；首次乾淨執行實測 `exit=0`
- [x] **[存在性]** `SKILL.md` Step 0 段含 evidence record 六欄位清單與律 B 交叉引用
- [x] ~~`assembleDebug` PASS~~ (N/A: 無 .kt/.xml 變更；本輪仍執行並 PASS，見 QA/SV 報告)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

## 完成後動作
- **MUST NOT** 自行搬移票檔（Pending→deliver 由 CSO 收束時執行）
- **MUST NOT** 自行寫入 `TICKET_REGISTRY.json`（單一寫入者為 Desktop 治理 session，concurrency.md §5）

## 收尾揭露（2026-08-24，Akira 對話直接指示執行）

- 碼閉環：`fbcb612`（`.claude/skills/qa/scripts/verify_checklist.sh` + `.claude/skills/qa/SKILL.md`）。`/qa` Build/Test/Lint 全 PASS（`assembleDebug` 47s、`testDeviceDebugUnitTest` 209/209、0 new lint）。
- `/sv` 階段 A-C 全 PASS（Compile 19s、Lint 0 new warnings、Scope 邊界正確、16KB ELF ALIGNED、語意審查 CLEAN、UDF/SSOT N/A—純 CI 工具鏈腳本）。
- **`/sv` 階段 D（Codex Cross-Review）未執行 — DEFERRED，非 PASS 非 N_A**：模擬 `cross_review.sh` L347-349 排除邏輯確認本票 basename-only `ALLOWED` 會觸發與 `INFRA-MAXLINES-GATE-01` 同型缺陷（`.claude/` 整段被排除，payload 空 diff），未呼叫 Codex 以避免無效審查。Akira 2026-08-24 對話裁決選項 B：本票暫緩，待 `INFRA-CROSSREVIEW-GOVPATH-EXCL-01` 落地後補跑。
- **本節「完成後動作」兩項 MUST NOT 之例外**：Akira 於 2026-08-24 對話中直接指示「請協助逐一勾選執行項目及 AC 測試，並於完成後進行檔案搬移歸檔及更改冊面狀態」，構成本票文本自身以外之即時 CSO 授權，覆蓋上方兩項預設 MUST NOT（非機械閘門，紀律層豁免，如實記錄於此）。
- AI/AC checkbox 依撤警正向取證律，僅勾有 QA/SV 實跑或設計驗證佐證之項；本次全數 8 項 AC 均有具體證據附註於各行。

## Addendum（2026-08-26，`/sv` 階段 D 補跑）

- 前置阻塞已解：`INFRA-CROSSREVIEW-GOVPATH-EXCL-01`（basename-only `ALLOWED` 誤排除 `.claude/` 缺陷修復）已落地並 Done（`390f50d`）。
- 本票重跑 `/sv` 階段 A-D（`3df34a6..fbcb612`）：Compile ✅ PASS（47s）、Lint ✅ PASS（51s，0 new）、Scope ✅ PASS、16KB ELF ✅ ALIGNED、Codex Cross-Review R2 **✅ PASS**（`REACHABILITY: NONE`，無 BLOCKER/REPORT_FIX，判定依據見 `.tmp/cross_review_result_20260826_162843.md`）。
- 前次「暫緩」揭露（見上節）已由本次補跑消解；`/sv` 四階段全數 PASS。
- `TICKET_REGISTRY.json` `reviews.codex` 欄同步、Traycer N/A 落記仍屬 Desktop 治理 session 單一寫入者範圍（`concurrency.md` §5），本次未觸碰。

