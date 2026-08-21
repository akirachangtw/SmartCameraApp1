# INFRA-CROSSREVIEW-GOVPATH-EXCL-01: cross_review.sh 治理路徑排除誤判 basename-only ALLOWED 修正

**簽發日期**: 2026-08-15
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
ALLOWED=cross_review.sh
FORBIDDEN=verify_checklist.sh,structural_verify.sh,Unified_Ticket_Template_v3.md,TICKET_REGISTRY.json,PROGRESS.md
MAX_LINES=20
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-08-15 17:38

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
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [ ] 完成

### AI-1: 排除判定改以 ALLOWED 逐 token 之實際 git-tracked 路徑解析比對
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`
- **現值**: `echo "$CONF_ALLOWED" | grep -qF "$_gpath"` — 對 `CONF_ALLOWED` 原始字面（可能為 basename）與治理目錄前綴做字面子字串比對
- **目標值**: 對 `CONF_ALLOWED` 逐 token 呼叫 `git ls-files` 或等效方式解析出其實際 tracked 路徑（若唯一命中），以**解析後路徑**是否落於 `$_gpath` 之下判定涵蓋，取代純字面子字串比對；basename token 對應多個候選路徑（歧義）時，**MUST** 保守判定為「涵蓋」（不排除該治理目錄），避免同一缺陷以歧義形式復發
- [ ] 完成

## Hard Constraints
1. **MUST NOT** 觸碰 `verify_checklist.sh`／`structural_verify.sh`——同名不同物，非本票標的。
2. **MUST NOT** 改動 Cross-Review 之四級 Verdict 語義（PASS/BLOCKER/REPORT_FIX/ADVISORY）與 exit code 對應。
3. **MUST NOT** 移除既有 `sv_report.md` 排除（`:!sv_report.md`）。
4. 本票不得混入 `.md` 治理文件變更（CLAUDE.md 鐵律 3）。

## Error Handling
- AI-0 任一命中數不符 → 停止回報
- `git ls-files` 對 basename token 命中 ≥2 候選且歧義無法保守收斂 → 停止回報，附候選清單
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報

## Acceptance Criteria
- [ ] **[行為性]** 重放 `INFRA-MAXLINES-GATE-01` 案例（`ALLOWED=verify_checklist.sh` conf，DIFF_REF=73cd4ff，DIFF_END=0bb6b4c）：修正後 `cross_review.sh` 產出之 payload `## Git Diff` 區段含實際 diff（非空白）
- [ ] **[行為性]** 既有 `plans/`-scoped SSOT 票（ALLOWED 含完整路徑前綴如 `plans/architecture/SSOT_v4_0_7.md`）之既有排除行為不回歸（該類票之 payload 仍正確涵蓋 `plans/`）
- [ ] ~~`assembleDebug` PASS~~ (N/A: 無 .kt/.xml 變更)
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

## 完成後動作
- **MUST NOT** 自行搬移票檔（Pending→deliver 由 CSO 收束時執行）
- **MUST NOT** 自行寫入 `TICKET_REGISTRY.json`（單一寫入者為 Desktop 治理 session，concurrency.md §5）
