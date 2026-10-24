# ADV-CKPTDOC-01: `sv/SKILL.md` Step 11 第 2 項之 checkpoint 清空敘述改為「轉 CLOSED 成功時清空」，與 PLT 契約及實作一致

**簽發日期**: 2026-10-24
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ACAGOV-RUNTIME-TABLE-01 —— Step 11 Closeout Auto-Chain 原立法
- `coordination_dependency`: 無（若 ADV-SAVE-01 已交付，本票 close 即該票 `persist` 之首次真實使用，屬指示式觀察、非盲測，收尾實錄須標明）

## 來源（Parent）
- Parent type：Plan-backed；路徑 `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261024-autonomy-closeloop-plan.md`（Revision R2；Fixture false／Plan Gate PASS／Ticket mode eligibility ALLOWED）；plan-local item `T03`；讀取時間 2026-10-24T05:30Z。
- Selected item：Step 11 checkpoint 清空敘述校正（diag C9）。

## 證據（開票時 live，2026-10-24）
- 現值：`.claude/skills/sv/SKILL.md:212` 含「（`set_status` 每次呼叫清空 checkpoint）」（全 `.claude/**/*.md` 唯一命中）。
- PLT 契約：`D:\DevSources\workflow\mcp-ticket-platform\docs\deliver\archive\OTHERS\tool-contracts.md:672`「`lifecycle` 目標為 `CLOSED` 時……轉態成功後 `checkpoint` 清為 `null`」（跨 repo 唯讀）。
- PLT 實作：`D:\DevSources\workflow\mcp-ticket-platform\core\services\registry_service.py:1094-1095`（`if target_lifecycle == "CLOSED": updated_ticket["checkpoint"] = None`）。
- APP SSOT：`ssot-governance.md` §checkpoint 生命週期第 1 點與 §Ticket Closure 冊面轉態紀律（`set_status(CLOSED)` 轉態時清為 `null`）。
- 實測：ADV-CLAUDECLI-01 本 session `set_status`→REVIEW 後 checkpoint 仍在、→CLOSED 後為 null。
- 反例檢查：寫入順序規則（checkpoint 置於 `set_status` 之後）本身不改——轉 REVIEW 不清空時，先寫後轉亦可保留，但規則維持「之後」不產生錯誤結果（CLOSED 前寫入的 checkpoint 仍會在 CLOSED 時清空，與規則用意一致）；故本票只改括號理由，不改順序規則。

- 票號歸屬：本票由「工程師自主閉環」議題（源自 ADV-CLAUDECLI-01 收束與 Advisor 成果表 P1 缺口）發起，依 Akira 2026-10-24 裁決，同一議題 session 發起之票一律掛該議題前綴 `ADV`，不依修改落點另分前綴；原開票 ID 已以 `registry_rename_ticket` 改名。

## Verify Config
TICKET_ID=ADV-CKPTDOC-01
CREATED=2026-10-24
PRESET=docs
ALLOWED=.claude/skills/sv/SKILL.md
FORBIDDEN=.claude/skills/ticket-author/**,.claude/skills/compact/**,.claude/skills/qa/**,.claude/rules/**,.claude/scripts/**,.claude/hooks/**,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,CLAUDE.md,app/**
MAX_LINES=2
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-24 14:05
BASE_COMMIT=2d8e7e34
TARGET_BLOB=N/A（docs preset 單檔小改；AI-0 錨點替代）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/sv/SKILL.md`
- **動作**: 確認「（`set_status` 每次呼叫清空 checkpoint）」於檔內恰 1 次，位於 Step 11 第 2 項。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置。**
- [x] 完成（2026-10-24：`grep -c` 命中 1，位於 `sv/SKILL.md:212`；全 `.claude/**/*.md` 唯一命中）

### AI-1: 校正括號理由
- **檔案**: `.claude/skills/sv/SKILL.md`
- **現值**: `（`set_status` 每次呼叫清空 checkpoint）`
- **目標值**: `（`set_status` 轉 `CLOSED` 成功時清空 checkpoint，見 `ssot-governance.md` §checkpoint 生命週期）`
- [x] 完成（2026-10-24：提交 `e3ee1f5a`，1 檔 1+/1-）

## Hard Constraints
1. 只改該括號；Step 11 其他文字、順序規則與項次不變。

## Error Handling
- 現值不唯一或已被他票改動 → 依三分流記錄後以現值續作；語義已正確則跳過並載明。
- **[標配]** `git check-ignore -v .claude/skills/sv/SKILL.md` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** `grep -c "每次呼叫清空" .claude/skills/sv/SKILL.md` = 0，且新字面「轉 `CLOSED` 成功時清空 checkpoint」恰 1 次。（實測 old=0／new=1，2026-10-24）
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)（票面劃記 N/A，無 app/ 變更，未執行 Gradle）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（1 檔、改動 ≤2 行）（實測 `sv/SKILL.md | 2 +-`，1 insertion／1 deletion；`git diff --check` 無輸出）

## 收尾實錄（Agent closeout，2026-10-24；交 TKA close 判定，非自行終審）
- **路徑**：低風險免 QA／SV（ALLOWED 僅 `.claude/skills/sv/SKILL.md` 一個 `.md`、無 `app/`／腳本／hook 變更、無不可逆操作、可 `git revert`）；已先 `check commit`（exit 0）再以 `[ADV-CKPTDOC-01]` 提交。
- **實作提交**：`e3ee1f5a`（`sv/SKILL.md` 1 insertion／1 deletion）。
- **驗證**：`grep -c "每次呼叫清空"`＝0、新字面「轉 `CLOSED` 成功時清空 checkpoint」＝1；`git diff --stat` 僅 ALLOWED；`git diff --check` 無輸出。
- **未執行**：`/qa`、`/sv`、Cross-Review、Gradle（依風險分級省略／票面 N/A）。`reviews.qa／sv／codex／traycer` 皆填 `N_A`。
- **揭露**：`PROGRESS.md` Dashboard 同步屬 FORBIDDEN 延後項，於下一輪 `/compact` 處理。本票 close 之保存由 ADV-SAVE-01 之 `persist` 執行（第二次真實使用，指示式觀察）。


<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: 交付：sv/SKILL.md Step 11 第 2 項括號改為「`set_status` 轉 `CLOSED` 成功時清空 checkpoint」（提交 e3ee1f5a，1 檔 1+/1-）。 / 驗證：grep 舊字面 0／新字面 1，與 PLT 契約及 registry_service.py:1094-1095 一致；TKA close reviewer gpt-6-sol 3 條 AC：PASS／N/A／PASS。 / 限制：低風險 docs 票依風險分級省略 /qa、/sv、Cross-Review（reviews 皆 N_A）；reviewer 無法讀檔，證據逐字嵌入。
- 最終驗收結論: 核心 AC 1 條皆為 PASS 或有效 N/A；非核心 PASS 1／N/A 1／UNVERIFIED 0／FAIL 0（共 3 條 AC）
- 實作提交: e3ee1f5af5848825ac4137d5d32b0830f95ae4a4
- 實際收束結果: 2026-10-24 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: codex/gpt-6-sol（tier=heavy）；覆蓋 AC 3／3（全部），independent_call=True
- 披露: reviewer limitation: reviewer 無法讀檔，證據逐字嵌入 prompt；回覆檔取自 codex exec 最終訊息段（原始輸出 reviewer_reply_raw.txt）；Cross-Review／QA／SV 依風險分級省略
<!-- close-result:end -->
