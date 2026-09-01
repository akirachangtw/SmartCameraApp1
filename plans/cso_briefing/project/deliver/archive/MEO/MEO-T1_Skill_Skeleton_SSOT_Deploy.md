# MEO-T1: MEO skill 骨架與 SSOT 部署,STATE.md 增補 §MEO 區段

**簽發日期**: 2026-07-20
**簽發者**: Claude
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: 無

## Verify Config
TICKET_ID=MEO-T1
CREATED=2026-07-20
PRESET=feature
ALLOWED=.claude/skills/model-effort-orchestrator/SKILL.md,.claude/skills/model-effort-orchestrator/decision-tree.yaml,.claude/skills/model-effort-orchestrator/prompt-template.md,STATE.md
FORBIDDEN=gate.conf,.claude/settings.json,stop_orchestrator.sh
MAX_LINES=600
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: 建立目錄骨架
- **檔案**: `.claude/skills/model-effort-orchestrator/`(含空 `hooks/`、`examples/` 子目錄)
- **現值**: 不存在
- **目標值**: 目錄結構就位
- [x] 完成

### AI-2: 部署 SKILL.md
- **檔案**: `.claude/skills/model-effort-orchestrator/SKILL.md`
- **現值**: 不存在
- **目標值**: `plans/cso_briefing/project/file/MEO/MEO_SKILL_v0.2.md` 原樣複製
- [x] 完成（`diff` 逐字比對一致）

### AI-3: 部署 decision-tree.yaml
- **檔案**: `.claude/skills/model-effort-orchestrator/decision-tree.yaml`
- **現值**: 不存在
- **目標值**: `plans/cso_briefing/project/file/MEO/MEO_decision-tree.yaml` 原樣複製
- [x] 完成（`diff` 逐字比對一致）

### AI-4: 生成 prompt-template.md
- **檔案**: `.claude/skills/model-effort-orchestrator/prompt-template.md`
- **現值**: 不存在
- **目標值**: 依 decision-tree.yaml R1–R7 撰寫 agent-facing 規則;首三行必含 `derived-from: decision-tree.yaml v0.2`
- [x] 完成

### AI-5: STATE.md append §MEO 區段
- **檔案**: `STATE.md`
- **現值**: 無 `## MEO` 區段
- **目標值**: Payload A3 區塊原樣 append 至檔尾
- [x] 完成 — **CSO 裁決變更**：`test -f STATE.md` 核實 STATE.md 從未建立（非「既有五區段+append」情境），Akira 2026-07-20 對話裁決選項 1：改為新建 STATE.md，內容僅含 §MEO 10 行 KV

## Hard Constraints
1. STATE.md 僅允許 append,禁止重排或改寫既有五區段任何內容
2. Payload 源檔內容禁止即興改寫;發現問題停止回報,不自行修正
3. 檔案編碼 UTF-8 無 BOM

## Error Handling
- `## MEO` 區段已存在 → 跳過 AI-5,回報 idempotent skip
- Payload 源檔缺失或內容異常 → 停止回報
- STATE.md append 後 read-back 不符 → 停止回報,勿重複 patch

## Acceptance Criteria
- [x] **[存在性]** 四檔案 + hooks/ + examples/ 存在
- [x] **[存在性]** `grep -c '^MEO_' STATE.md` 回傳 `10`
- [x] **[存在性]** prompt-template.md 首三行含 `derived-from: decision-tree.yaml v0.2`
- [x] **[行為性]** STATE.md 為本票新建（非既有檔案 append，見 AI-5 CSO 裁決）,全檔僅 11 行(1 標題+10 KV),無其他內容可能被誤動
- [x] `assembleDebug` PASS（15s，0 error）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 + `.claude/PROGRESS.md`（pipeline artifact）
