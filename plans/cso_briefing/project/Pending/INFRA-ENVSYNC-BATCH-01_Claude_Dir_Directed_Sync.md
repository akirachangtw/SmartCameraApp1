# INFRA-ENVSYNC-BATCH-01: .claude/ 17 檔逐檔定向同步（PLT→APP 預設向＋fast-forward 凍結閘）

**簽發日期**: 2026-08-21
**簽發者**: Claude（SMTR；Akira 2026-08-21 裁決批次同步 17 檔）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（.claude/ 治理文本同步；APP 端 RAPID_MODE 經 Akira 2026-08-22 裁決**不引入**，本票依 `.claude/CLAUDE.md` SOP 全程序執行 QA／SV／Cross-Review，本欄 N/A 係依治理票免 Traycer 先例，非援引 §0 豁免）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: INFRA-ENVSYNC-AUDIT-01（CLOSED；hash 表＋定年表為本票唯一選檔依據）；其 checkpoint 審查裁定（「定年新舊不構成 superset 證明」）
- `coordination_dependency`: INFRA-SETTINGS-MERGE-01（ALLOWED 不相交可並行；原 INFRA-CLAUDE-MERGE-01 已於 2026-08-22 前提消滅型 VOID，改號承接）；INFRA-TRACKSCOPE-01（ALLOWED 零相交，且其 AI-3 明文將 `.claude/rules/ssot-governance.md` L100 stale 路徑修正**轉交本票收束後另案**，禁跨票同檔編輯）；INFRA-MAXLINES-BACKPORT-01（PLT 冊反向票，互不阻塞）

## 檔案治理／來源說明

AUDIT-01 候選同步 20 項，經審查裁定剔除三例外（verify_checklist.sh→反向票；cross_review.sh→凍結待 INFRA-CROSSREVIEW-GOVPATH-EXCL-01；settings.local.json→重分類禁搬），餘 17 檔即本票 ALLOWED 全集。**審查已證偽「全域單向」**：kairos-lessons.md APP 端（08-01，#18 SessionStart 認知條款）新於 PLT 端（07-27）——故本票每檔 MUST 過 fast-forward 閘，PLT→APP 僅為預設向，非公理。**已知高凍結嫌疑三檔**（APP 端 08-01 INFRA-RULES-CARRIER-01 部署所及）：kairos-lessons.md、concurrency.md、decommission.md——後二者 PLT 端 08-06 版本是否已含 APP 之律 B／#19 條款未核，一律以閘判定，禁憑定年推定。

## Verify Config
TICKET_ID=INFRA-ENVSYNC-BATCH-01
CREATED=2026-08-21
PRESET=docs
ALLOWED=.claude/agents/cross-reviewer.md,.claude/hooks/stop.sh,.claude/rules/concurrency.md,.claude/rules/cross-review.md,.claude/rules/decommission.md,.claude/rules/kairos-lessons.md,.claude/rules/phase0-lessons.md,.claude/rules/ssot-governance.md,.claude/rules/ticket_pattern.md,.claude/skills/compact/SKILL.md,.claude/skills/mdlint/SKILL.md,.claude/skills/qa/SKILL.md,.claude/skills/qa/scripts/static_blind_scan.sh,.claude/skills/sv/CROSS_REVIEW.md,.claude/skills/sv/SKILL.md,.claude/skills/sv/ssot_context.md,.claude/skills/uc/SKILL.md,plans/cso_briefing/project/file/ENVSYNC_AUDIT_20260821.md
FORBIDDEN=.claude/CLAUDE.md,.claude/settings.json,.claude/settings.local.json,.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/sv/scripts/cross_review.sh,.claude/skills/status/**,app/**
MAX_LINES=N/A（覆寫幅度＝兩端分叉之被動結果）
DIFF_REF=字面佔位待開工回填
ANCHOR_PROBED=2026-08-21（AUDIT-01 報告 hash 表即錨；表載值經審查方抽驗一致）

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **動作 A**：自 AUDIT-01 報告表任抽 3 檔重跑 `git hash-object` 兩端比對，與表載值一致；不一致 → 停止回報（基線已漂移，報告失效）。
- **動作 B**：兩端 status 二檔仍 hash 相等（SYNC-01 成果未被擾動）。
- [x] 完成

### AI-1: 逐檔 fast-forward 閘＋定向同步（17 檔逐一）
- **判定（機械）**：以 APP 檔為左、PLT 檔為右執行 `git diff --no-index <APP> <PLT>`；diff 中 `-` 行（APP 獨有內容）剔除純空白行後——
  - **＝0** → fast-forward 成立：`cp` PLT→APP，`git hash-object` 兩端相等後打勾；
  - **＞0** → 該檔**凍結**：禁覆寫、禁手工合併、禁「僅搬 PLT 新增段」，原樣列入凍結清單（附 APP 獨有行之行號範圍與首行摘錄）。
- **禁語義裁量**：APP 獨有行「看起來不重要／只是措辭」不構成覆寫理由。
- [x] 完成

### AI-2: 凍結清單回流
- **檔案**: `plans/cso_briefing/project/file/ENVSYNC_AUDIT_20260821.md`
- **動作**: 檔尾 append 新節 `## 凍結清單（BATCH-01 回流，待 Akira 合併裁決）`：三欄表（檔案｜APP 獨有行摘要｜建議處置=一律「待裁」）。同步成功檔亦列於同節之「已同步」子表（檔名＋終態 hash）。
- [x] 完成

### AI-3: commit 落版
- **動作**: 同步成功檔依類別分批 commit（rules 一批、skills 一批、agents/hooks 一批），前綴 `[INFRA-ENVSYNC-BATCH-01]`；報告回流走 `[INFRA-ENVSYNC-BATCH-01]` 同前綴獨立 commit。
- [x] 完成（rules 批 `b0636f3`、skills 批 `bb727f8`、agents/hooks 批 0 檔無需 commit、報告回流 `73a164d`）

## Hard Constraints
1. 每檔終態 **MUST** 二擇一：hash 兩端相等（已同步）或列名凍結清單——**禁第三態**（部分改寫、混合版）。
2. 凍結檔 **MUST NOT** 以任何形式改動（含「先備份再試合併」）。
3. **MUST NOT** 觸碰 FORBIDDEN 五類（含 status 二檔——SYNC-01 已收束成果）。

## Error Handling
- AI-0 任一不符 → 停止回報
- 任一 `cp` 後 hash 不等 → 停止回報（通道損毀）
- 17 檔全數凍結 → 停止回報（選檔依據系統性失效，回 CSO）
- **[標配]** `git check-ignore -v` 對 ALLOWED 全清單 → 命中即停止回報（gitignore 死結律 #20）

## Acceptance Criteria
- [x] **[存在性]** 凍結清單節在盤，且「已同步」＋「凍結」列數合計＝17（禁缺漏）——2 已同步 + 15 凍結 = 17 ✓
- [x] **[存在性]** kairos-lessons.md 之處置有明文記載——審查預測命中：凍結（APP 獨有非空行 17 行，L9-10 scope 定義＋L21 K1-Rule 11 條款等），已列入凍結表
- [x] **[行為性]** 已同步檔重跑 `git hash-object` 兩端相等（production 路徑重放）——實際已同步檔僅 2 檔（非票面預期抽樣基數 3；如實揭露：`rules/concurrency.md`、`skills/uc/SKILL.md` 全數複驗，非抽樣 3/N，因 N=2 < 3），兩檔 hash 皆相等
- [x] **[可達性]** N/A 顯式劃記（無 Guard/gate/runtime state）
- [x] ~~`assembleDebug` PASS~~（N/A：docs preset）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

**End of INFRA-ENVSYNC-BATCH-01**
