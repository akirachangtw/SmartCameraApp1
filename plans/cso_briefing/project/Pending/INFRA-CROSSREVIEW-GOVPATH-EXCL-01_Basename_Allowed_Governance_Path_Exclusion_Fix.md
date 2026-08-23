# INFRA-CROSSREVIEW-GOVPATH-EXCL-01: cross_review.sh 治理路徑排除誤判 basename-only ALLOWED 修正

**簽發日期**: 2026-08-15
**Rev A**（2026-08-21，SMTR 派工前審查修補；Akira 授權「審查並派工執行」）：修補六項——F1 自指阻塞防護入 HC、F2 ALLOWED/FORBIDDEN 改完整路徑、F3 MAX_LINES 依 TPL v3.5 改 N/A、F4 AI-1 與 Error Handling 歧義處置自撞消解、F5 增存在性 AC、F6 AI-0 增基線 SHA 存在性復驗。原判準凡經修改者以刪除線並列，禁靜默替換。
**簽發者**: Claude（INFRA-MAXLINES-GATE-01 執行中發現，Akira 2026-08-15 對話裁決選項 B 授權開票）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（治理／腳本票，依 governance-ticket-traycer-exempt 先例）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無
- `coordination_dependency`: `INFRA-MAXLINES-GATE-01` —— 該票 Cross-Review 因本缺陷阻塞（R1 空 diff 誤判 REPORT_FIX），待本票修復後重跑

## Verify Config
TICKET_ID=INFRA-CROSSREVIEW-GOVPATH-EXCL-01
CREATED=2026-08-15
PRESET=hotfix
ALLOWED=.claude/skills/sv/scripts/cross_review.sh
FORBIDDEN=.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/qa/scripts/structural_verify.sh,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/TICKET_REGISTRY.json,.claude/PROGRESS.md
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-08-15 17:38（Rev A 復驗 2026-08-21 20:xx：L342/L344/L346/L347/L348/L354 六處現值經 SMTR live grep 確認未漂移）

**Rev A 欄位修補說明**：
- ~~`ALLOWED=cross_review.sh`~~ → 完整路徑。**理由（F2）**：本票原以 basename 填 ALLOWED，正是本票所修缺陷之觸發形狀——票面自身即缺陷案例，將導致本票之 Cross-Review payload 亦被 `:!.claude/` 清空（自指阻塞）。改完整路徑後，即使以修正前腳本執行亦不觸發缺陷，為 fail-safe 之立即防護；FORBIDDEN 同批改完整路徑（原 basename 對 `verify_checklist.sh` 等同名不同物之防護亦不可靠）。
- ~~`MAX_LINES=20`~~ → `N/A`。**理由（F3）**：TPL v3.5 已明載 MAX_LINES 完全退出 execution gate（四類值一律不阻斷），且 APP 端 `verify_checklist.sh` 已於 `0bb6b4c`（INFRA-MAXLINES-GATE-01）落地該退役；原值 20 係 v3.5 前語義殘留，且 AI-1 之逐 token 解析＋歧義保守實作預期超過該值。依 R3「安全票 N/A」：本票標的為 Cross-Review payload 生成路徑，失敗形狀（哪些路徑被誤排除）為審查發現式，不可先驗估算。

## 缺陷描述

`.claude/skills/sv/scripts/cross_review.sh` L346-349 之治理路徑排除邏輯：

```bash
EXCLUDE_ARGS=(':!sv_report.md')
for _gpath in .claude/ docs/ plans/; do
  echo "$CONF_ALLOWED" | grep -qF "$_gpath" || EXCLUDE_ARGS+=( ":!${_gpath}" )
done
```

以 `grep -qF "$_gpath"`（字面子字串比對）判斷 `tmp/verify_cmd.conf` 之 `ALLOWED` 是否「涵蓋」某治理目錄。當票面 `ALLOWED` 依合法格式（`ticket_pattern.md`／TPL 範例慣例）填 **basename token**（如 `ALLOWED=verify_checklist.sh`，未含目錄前綴字面）而其實體路徑恰落於 `.claude/`／`docs/`／`plans/` 之下時，字面比對必然 miss，導致該治理目錄被整段排除於 Cross-Review diff payload 之外——**即使該檔案正是本票 ALLOWED 標的本身**。

**實測案例**：`INFRA-MAXLINES-GATE-01`，`ALLOWED=verify_checklist.sh`（實體路徑 `.claude/skills/qa/scripts/verify_checklist.sh`），`cross_review.sh` 產生之 payload `## Git Diff` 區段完全空白，Codex R1 因無 diff 可審而判 `REPORT_FIX`（誤判為 sv_report.md 完備性問題，實為 payload 生成缺陷）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`
- **動作**: 逐字確認下列現值命中數與票面所載相符：
  - `for _gpath in .claude/ docs/ plans/; do` —— 唯一（1 次）
  - `echo "$CONF_ALLOWED" | grep -qF "$_gpath" || EXCLUDE_ARGS+=( ":!${_gpath}" )` —— 唯一（1 次）
- **Rev A 增列（F6，AC-1 前提取證）**: `git cat-file -e 73cd4ff^{commit}` 與 `git cat-file -e 0bb6b4c^{commit}` 均須成功（AC-1 重放所依之基線區間仍在盤）；任一不存在 → 停止回報（禁改用其他區間自行替代，該案例為缺陷原始實證，替換即失去重放意義）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [ ] 完成

### AI-1: 排除判定改以 ALLOWED 逐 token 之實際 git-tracked 路徑解析比對
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`
- **現值**: `echo "$CONF_ALLOWED" | grep -qF "$_gpath"` — 對 `CONF_ALLOWED` 原始字面（可能為 basename）與治理目錄前綴做字面子字串比對
- **目標值**: 對 `CONF_ALLOWED` 逐 token 呼叫 `git ls-files` 或等效方式解析出其實際 tracked 路徑（若唯一命中），以**解析後路徑**是否落於 `$_gpath` 之下判定涵蓋，取代純字面子字串比對；basename token 對應多個候選路徑（歧義）時，**MUST** 保守判定為「涵蓋」（不排除該治理目錄），避免同一缺陷以歧義形式復發
- **Rev A 補充（F4，歧義處置唯一化）**：歧義**不構成停止事由**——保守判定為「涵蓋」即為既定處置，並 **MUST** 於 stdout 輸出一行 `⚠️ ALLOWED token '<token>' 解析出 N 個候選路徑，保守判定為涵蓋 <gpath>`（可稽核性要求）。token 於 `git ls-files` **零命中**（未 tracked／拼字錯誤）時，同樣保守判定為「涵蓋」並輸出對應警示行，**MUST NOT** 因零命中而排除該治理目錄。
- [ ] 完成

## Hard Constraints
1. **MUST NOT** 觸碰 `verify_checklist.sh`／`structural_verify.sh`——同名不同物，非本票標的。
2. **MUST NOT** 改動 Cross-Review 之四級 Verdict 語義（PASS/BLOCKER/REPORT_FIX/ADVISORY）與 exit code 對應。
3. **MUST NOT** 移除既有 `sv_report.md` 排除（`:!sv_report.md`）。
4. 本票不得混入 `.md` 治理文件變更（CLAUDE.md 鐵律 3）。
5. **Rev A 增列（F1，自指阻塞防護）**：本票標的即 Cross-Review payload 生成器本身。執行 `/sv` cross-review **MUST** 於 AI-1 落地並 commit **之後**進行，使腳本以修正後邏輯生成自身之 payload（自愈路徑）；**MUST NOT** 以修正前腳本產生本票 payload。若 R1 payload 之 `## Git Diff` 區段為空 → 停止回報（表示自愈未成立，禁以 `EXTRA_EXCLUDE` 或手工貼 diff 繞道）。
6. **Rev A 增列**：RAPID_MODE 於 APP 端**不適用**（Akira 2026-08-21 裁決不引入，維持 APP 全程序現狀）；本票 QA／SV／Cross-Review 依 `.claude/CLAUDE.md` SOP 全程序執行，禁援引任何豁免。

## Error Handling
- AI-0 任一命中數不符 → 停止回報
- ~~`git ls-files` 對 basename token 命中 ≥2 候選且歧義無法保守收斂 → 停止回報，附候選清單~~ **（Rev A 廢止，F4）**：本條與 AI-1「歧義 MUST 保守判定為涵蓋」自撞——AI-1 已將歧義定為既定處置而非例外，保留本條將使執行者面對兩條互斥指令。歧義與零命中一律依 AI-1 Rev A 補充處置（保守涵蓋＋stdout 警示行），**不停止**。
- R1 payload `## Git Diff` 區段為空 → 停止回報（HC-5 自指阻塞未自愈）
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報

## Acceptance Criteria
- [ ] **[存在性]**（Rev A 增列，F5）修正後 `cross_review.sh`：`git ls-files` 字面命中 ≥1；舊式比對 `echo "$CONF_ALLOWED" | grep -qF "$_gpath"` 命中 **0**；`保守判定為涵蓋` 警示字串命中 ≥1（歧義／零命中路徑之可稽核性落地）
- [ ] **[行為性]** 重放 `INFRA-MAXLINES-GATE-01` 案例（`ALLOWED=verify_checklist.sh` conf，DIFF_REF=73cd4ff，DIFF_END=0bb6b4c）：修正後 `cross_review.sh` 產出之 payload `## Git Diff` 區段含實際 diff（非空白）
- [ ] **[行為性]** 既有 `plans/`-scoped SSOT 票（ALLOWED 含完整路徑前綴如 `plans/architecture/SSOT_v4_0_7.md`）之既有排除行為不回歸（該類票之 payload 仍正確涵蓋 `plans/`）
- [ ] **[行為性]**（Rev A 增列）負向迴歸：ALLOWED **不含**任何治理路徑之票（如純 `app/src/...` 標的），三治理目錄 **仍全數被排除**（`EXCLUDE_ARGS` 含 `:!.claude/`／`:!docs/`／`:!plans/` 三項）——證明修正未把排除機制整體弱化為恆不排除
- [ ] **[可達性]** N/A 顯式劃記：本票標的為 CI／審查工具鏈腳本，消費者為 Codex payload 生成路徑，`app/src/main/` 無 production producer，型別不適用（非規避）
- [ ] ~~`assembleDebug` PASS~~ (N/A: 無 .kt/.xml 變更)
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

## 完成後動作
- **MUST NOT** 自行搬移票檔（Pending→deliver 由 CSO 收束時執行）
- **MUST NOT** 自行寫入 `TICKET_REGISTRY.json`（單一寫入者為 Desktop 治理 session，concurrency.md §5）
