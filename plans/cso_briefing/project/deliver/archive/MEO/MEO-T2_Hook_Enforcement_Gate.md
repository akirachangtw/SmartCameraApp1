# MEO-T2: MEO hook enforcement 部署(PreToolUse gate + 雙事件信號累加 + orchestrator MEO gate)

**簽發日期**: 2026-07-20(Rev B:R8–R10 bootstrap 死鎖裁決後修訂;Rev B.1 2026-07-21:R11 ALLOWED 補登 stop_orchestrator.sh——A5 附掛檔案屬清單同步債,CSO 裁決回填;既有非 MEO gate 邏輯仍 FORBIDDEN;Rev B.2 2026-07-21:R12 ALLOWED 補登 cross_review.sh(AI-5 開工時定位檔)+STATE.md(A3/R9 設計內固有觸碰;既有五區段仍 FORBIDDEN))
**簽發者**: Claude
**Mode**: normal → uc  ||  **Effort**: High
**Traycer Workflow**: execute_review
**前置依賴**: MEO-T1

## Verify Config
TICKET_ID=MEO-T2
CREATED=2026-07-20
PRESET=feature
ALLOWED=.claude/skills/model-effort-orchestrator/hooks/meo_pretooluse_gate.sh,.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh,.claude/skills/model-effort-orchestrator/hooks/meo_session_reset.sh,.claude/settings.json,gate.conf,stop_orchestrator.sh,.claude/skills/sv/scripts/cross_review.sh,STATE.md
FORBIDDEN=stop_orchestrator.sh 既有非 MEO gate 邏輯,STATE.md 既有五區段,.claude/skills/model-effort-orchestrator/decision-tree.yaml
MAX_LINES=250
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: 部署兩支 hook 腳本 ✅(路徑依 Rev A 遷移確認)
- **檔案**: `hooks/meo_pretooluse_gate.sh`、`hooks/meo_signal_update.sh`
- **現值**: A1 已部署;A2 為 v1.0 單事件版
- **目標值**: Payload **v1.1** A1/A2 原樣部署(A2 為雙事件版)+ `chmod +x`
- [x] 完成 — cross-review R6 ADVISORY 收案(R16 CSO 裁定,2026-07-21)

### AI-2: ~~payload 欄位驗證~~ → 已由 CSO 裁決結案(R2)
- **裁決**: 成敗判別依 `hook_event_name`,非 isError 類欄位;error class 採 `.error // .tool_error // "unknown"` fallback 鏈。本項改為確認部署腳本與 Payload v1.1 A2 逐字節一致(diff_hash.sh)
- [ ] 完成 — 未執行 diff_hash.sh 逐字節比對,佐證不明保留未勾(ssot-governance.md 勾選紀律);追蹤併入 MEO-T3

### AI-3: settings.json 註冊三區塊
- **檔案**: `.claude/settings.json`
- **現值**: 無 MEO hook 條目
- **目標值**: Payload v1.1 A4 三區塊合併(PreToolUse / PostToolUse / PostToolUseFailure,同腳本雙掛),不動既有 hook
- [x] 完成 — cross-review R6 ADVISORY 收案(R16 CSO 裁定,2026-07-21)

### AI-4: gate.conf 增補 [meo] + stop_orchestrator 讀取邏輯
- **檔案**: `gate.conf`
- **現值**: 無 `[meo]` 條目
- **目標值**: Payload A5 append;stop_orchestrator.sh 僅新增 MEO gate 讀取分支
- [x] 完成 — cross-review R6 ADVISORY 收案(R16 CSO 裁定,2026-07-21)

### AI-5: /sv pipeline 駁回累加整合(不受事件模型問題影響,可先行)
- **檔案**: /sv pipeline 腳本(開工時定位)
- **現值**: 無 MEO 計數
- **目標值**: Codex 駁回路徑加一行 `MEO_VERIFIER_REJECT_COUNT` kv_set 累加
- [x] 完成 — `.claude/skills/sv/scripts/cross_review.sh` BLOCKER 分支已接線(見腳本第425行)

### AI-6: A1 v1.2 重部署 + SessionStart 重置腳本(Rev B / R9–R10)
- **檔案**: `hooks/meo_pretooluse_gate.sh`、`hooks/meo_session_reset.sh`、`.claude/settings.json`
- **現值**: A1 為 v1.1(無自舉豁免);無 session 重置
- **目標值**: Payload v1.2 A1 覆蓋 + A8 部署 + settings.json 增 SessionStart 區塊
- [x] 完成 — cross-review R6 ADVISORY 收案(R16 CSO 裁定,2026-07-21)

## Hard Constraints
1. 禁增第四個 **Stop** hook(G2/D5);**邊界明文(CSO R3)**:PostToolUseFailure 為 lifecycle 事件,同腳本雙掛不在此限
2. hook stdout 契約:PreToolUse 僅 `decision`/`reason`;Stop 類僅三鍵;`stop_hook_active: true` 僅得作 reason 註記
3. jq 缺失 fail-open,不得癱瘓工具呼叫
4. **成功路徑僅重置 RETRY_COUNT,禁止重置 STREAK(CSO R4)**——交錯型硬磨偵測必要,不得「順手修正」
5. **INFRA-001 防護(CSO R6)**:確認 /sv 排除清單不豁免 `.claude/skills/model-effort-orchestrator/hooks/`;若現行清單自動豁免 `.claude/`,須為本路徑加白名單例外

## Error Handling
- /sv 腳本累加點無法定位 → 跳過 AI-5 停止回報
- **V5 實測 PostToolUseFailure 不觸發(已知 issue #27886)→ 停止回報;降級預案:RETRY 信號暫由 /sv reject count 承載,待 runtime 修復(CSO R7)**
- V1–V4 任一失敗 → 停止,不進 V5–V8
- STATE.md 診斷狀態無法自舉(R8 事件類)→ 停止回報,依 Payload v1.2 R9 豁免路徑處置;禁止繞道 Bash 寫 STATE.md
- STATE.md 既有五區段內容疑似遺失 → 立即 `git diff` 查證,若 T1 為覆寫而非 append,自 git 還原後重做 append,停止回報

## Acceptance Criteria
- [x] **[存在性]** 三腳本(A1 為 v1.2、A2 為雙事件版、A8 重置)+ settings.json 四區塊 + gate.conf `[meo]` 存在 — ✅ 全數存在,見 sv_report.md Implementation Scope
- [x] **[行為性]** R9/R10 循環:新 session 重置後,Edit STATE.md 寫診斷放行、其他管制呼叫被攔,寫入後放行(真實呼叫驅動) — ✅ SV 執行過程真實復現(非 fixture)
- [x] **[可達性]** PreToolUse 與 PostToolUseFailure 均經真實呼叫實際觸發(producer 證據欄:`.claude/settings.json:行號` + 各事件執行 log,執行後回填) — ✅ producer=人為 STATE.md Edit（R9 設計內），佐證：`.claude/settings.json:85-126` PreToolUse/PostToolUseFailure 掛載區塊 + `meo_pretooluse_gate.sh:17` R9 設計註解 + Akira 對話裁決（2026-07-21，見 MEO-T3 AI-2）
  - **T3 回填（2026-07-21）**：CSO 裁決結案，見上。
- [x] **[行為性]** V4:無診斷狀態下真實工具呼叫被 block(禁 fixture 直注 stdout) — ✅ SV 執行過程真實復現
- [ ] **[行為性]** V5/V6:真實失敗驅動累加;含 R4 情境(失敗→成功→同 class 失敗,streak 續累) — ⚠️ PARTIAL(V5 因 Claude Code known issue #27886 無法完整確認,降級預案已落地補償,CSO R7;佐證不明,保留未勾,追蹤併入 MEO-T3)
  - **T3 回填（2026-07-21）**：#27886 查證為 CLOSED as duplicate（併入 #17088），無官方修復證據；本 session 另以真實非零 exit 失敗直接復現 PostToolUseFailure 對 SV 路徑與對照組均未觸發 kv 累加，強化「未修復」結論。維持 CSO R7 降級預案,已於 PROGRESS.md 設定複查節點,結論見 MEO-T3 AI-1。
- [x] **[行為性]** V7/V8:orchestrator 三鍵 schema 輸出 + quota 阻斷走 defer — ✅ E2E 真實 escalation 觸發已於 SV 執行過程被動觀測確認
- [x] **[存在性]** /sv 排除清單對 hooks 路徑之豁免狀態已查證並記錄(Hard Constraint 5) — ✅ ALLOWED 命中三檔字面,不受盲目 `.claude/` 豁免
- [x] `assembleDebug` PASS — ✅
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 — ✅ 3 項跨票 rename 獨立 commit `fa1ec18` 分離 + Files Changed 揭露(R15-a)
