# INFRA-QA-COMMIT-EMPTYDIFF-01-FIX-A: T2 初審兩項 Minor 修補（模板同步＋防禦路徑條文化）

**簽發日期**: 2026-07-19
**簽發者**: Claude（CSO session，依 Traycer T2 初審 🟡 Minor Issue 回判開票）
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: execute_review（修補後依 T2 指示 re-run 同一 review command）
**母票**: `INFRA-QA-COMMIT-EMPTYDIFF-01`（commit 43e4e5a 收口，本票不重開母票範圍）
**T2 依據**: Traycer 初審 2026-07-19：Finding 1 報告模板未同步（SKILL.md L183-L188 vs L108-L114 分支）；Finding 2 malformed ALLOWED 停止規則未條文化（母票 Error Handling L42-L44 要求）

## Verify Config
TICKET_ID=INFRA-QA-COMMIT-EMPTYDIFF-01-FIX-A
CREATED=2026-07-19
PRESET=refactor
ALLOWED=.claude/skills/qa/SKILL.md
FORBIDDEN=.claude/CLAUDE.md,.claude/settings.json,.claude/PROGRESS.md,.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/sv/scripts/cross_review.sh,.claude/agents/cross-reviewer.md,plans/**
MAX_LINES=40
DIFF_REF=43e4e5a
# 開工前 MUST 校驗：`git rev-parse --short HEAD` 與 43e4e5a 比對；不一致依母票 spawn Step 0.3 重錨三件套
# （目標檔零異動確認＋更新＋留痕）；目標檔有外部異動 → STOP 回報 diff

## Action Items

### AI-1: QA Report 模板同步零 diff 變體（消解 Finding 1 split-brain）
- **檔案**: `.claude/skills/qa/SKILL.md`（報告模板節，T2 定位 L183-L188；以現場錨定為準，禁憑行號盲改）
- **現值**: 模板僅示 `✅ Auto Commit: [hash] [TICKET_ID]` 單一形態，隱含「必有 hash」假設
- **目標值**: 模板列出**兩種互斥形態**並標註分支條件：
  - 非零 diff：`✅ Auto Commit: [hash] [TICKET_ID]`（既有，不動）
  - 真零 diff：`✅ Auto Commit: SKIPPED（ZERO-DIFF-DECLARED，DIFF_END=DIFF_REF）`
  - token 字面 MUST 與 L108-L114 分支輸出完全一致（同一 `ZERO-DIFF-DECLARED` 字串，禁改寫），並加一行指示：報告形態由 Step 4.5 分支判定直取，禁自由措辭
- [x] 已實作（`SKILL.md:189-190` 現場核對，雙形態 + token 字面一致）

### AI-2: malformed ALLOWED 停止規則條文化（消解 Finding 2 防禦路徑缺口）
- **檔案**: `.claude/skills/qa/SKILL.md`（Step 4.5 前置檢查區，T2 定位 L99-L136 範圍內錨定）
- **現值**: 已覆蓋 conf 缺失／ALLOWED 為空／scope 污染／零 diff 分流四類；無 malformed ALLOWED 處置
- **目標值**: 於執行 `git status --short -- <ALLOWED 逐檔清單>` **之前**新增停止規則：
  - ALLOWED 條目含 wildcard／glob／pathspec 異常字元或格式無法安全解析 → **STOP**
  - STOP 回報 MUST **逐字引述 `tmp/verify_cmd.conf` 實際內容**（禁猜測、禁憑記憶轉述、禁自行修正後續行）
- [x] 已實作（`SKILL.md:109` 現場核對，位置正確 — 位於零 diff `git status --short` 執行之前）

## Hard Constraints
1. 僅動 `.claude/skills/qa/SKILL.md` 單檔；兩處編輯均為 prompt 層文字，禁觸碰任何 `.sh`
2. L108-L114 既有零 diff 分支邏輯**零改動**——本票只補模板與防禦路徑，不重構母票交付物
3. 錨定紀律：以條文內容錨定編輯位置，T2 行號僅供定位參考；錨點非唯一 → STOP

## Error Handling
- 模板節或 Step 4.5 前置區條文與 T2 描述不符（結構已漂移）→ STOP 回報現場引文
- 開工時 working tree 有他票未收口變更 → STOP（案例⑦防護）

## Acceptance Criteria
- [x] **[AC1 模板雙形態]** 報告模板節 grep 同時命中 `Auto Commit: [hash]` 與 `ZERO-DIFF-DECLARED`（實測輸出附報告）—— `SKILL.md:189-190` 現場核對命中
- [x] **[AC2 token 一致性]** 模板節與 L108-L114 分支的 `ZERO-DIFF-DECLARED` 字串 diff 級一致（同一字面）—— 現場核對確認同一字串
- [x] **[AC3 防禦路徑條文化]** Step 4.5 前置檢查含 malformed ALLOWED STOP 規則且明文「逐字引述 tmp/verify_cmd.conf 實際內容」（grep 實測命中）—— `SKILL.md:109` 現場核對命中
- [x] **[AC4 隔離性]** `git diff --stat` 僅涉 `.claude/skills/qa/SKILL.md`，總變更 ≤40 行 —— `git show --stat 7324dab` 確認單檔 5+/1-
- [x] **[AC5 回歸]** 母票 AC 四條全數維持 PASS（零 diff 分支邏輯未被本票觸動之證明：L108-L114 區塊 diff 為空）—— AI-2 為插入新行於既有邏輯之前，既有分支本體內容零改動
- [x] **[AC6 承接]** 修補後 re-run QA/SV → Akira 依 T2 指示 re-run 同一 review command（附現行票面 read-back 含時戳）—— Codex R1 PASS（本 session 執行），Traycer 終審 ✅ APPROVE（Akira 2026-07-19 確認）

## Closure

- Codex Cross-Review: R1 **PASS**（REACHABILITY: NONE）
- `structural_verify.sh --post`: PASS（ADVISORY）
- Commit: `7324dab`
- Traycer 終審: ✅ APPROVE（Akira 2026-07-19 確認）
- 票檔搬移: `deliver/`（本次收尾）
