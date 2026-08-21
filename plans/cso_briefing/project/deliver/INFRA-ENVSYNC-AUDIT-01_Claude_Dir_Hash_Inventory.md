# INFRA-ENVSYNC-AUDIT-01: .claude/ 兩端全清單 hash 盤點（單向 PLT→APP 同步之取證前置）

**簽發日期**: 2026-08-21
**簽發者**: Claude（SMTR 主 session；Akira 2026-08-21 裁決「APP 的很多東西其實是很舊的……單向 MCP 同步 APP」）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（唯讀盤點票，產物為報告檔，零 production 觸碰；免 Traycer/Codex 依 docs 型先例）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ENV_DIFF_MATRIX_claude_dir_v1（`D:/DevSources/Skill-Advisor/deliver/`；本票即其 §7 G-1／G-2 缺口之消解令）
- `coordination_dependency`: INFRA-STATUS-SYNC-01（K-1 單點先行，ALLOWED 不相交可並行；本票報告 MUST 將 status 二檔標記為「SYNC-01 承接中」避免雙重派工）

## 檔案治理／來源說明

矩陣 v1.0 已完成兩端 `.claude/` 之存在性／尺寸層比對，惟 §7 誠實登錄三缺口：G-1 同名檔位元級同一性未驗證（rules 七個同名檔尺寸全不同，已證同名≠同文）、G-2 `agents/`／`scripts/`／`settings.local.json` 未清點、G-3 APP PROGRESS.md 路徑指涉未查。本票以一次機械盤點消解 G-1／G-2／G-3，產出分類清單供 Akira 裁決全量同步範圍。**本票唯讀＋報告，不執行任何同步。**

## Verify Config
TICKET_ID=INFRA-ENVSYNC-AUDIT-01
CREATED=2026-08-21
PRESET=docs
ALLOWED=plans/cso_briefing/project/file/ENVSYNC_AUDIT_20260821.md
FORBIDDEN=.claude/**,app/**,docs/**
MAX_LINES=N/A（報告行數＝盤點結果之被動函數，非估算對象）
DIFF_REF=字面佔位待開工回填
ANCHOR_PROBED=2026-08-21（矩陣 §1–§6 全項本 session live 直讀；兩端 .claude/ 頂層與 skills/hooks/rules 清單為既取證基線）

## Action Items

### AI-0: 錨點復驗（派工後、盤點前）
- **動作**: 兩端 `.claude/` 頂層 `ls` 與矩陣 §1 對照——PLT 12 項（含 worktrees/）、APP 12 項（含 settings.json.pre-decomm.bak）。清單漂移 → 不停止，於報告「基線漂移」節記載實測差異後續行（本票即盤點票，漂移本身是產出）。
- [x] 完成

### AI-1: 全清單 hash 盤點
- **動作**: 對兩端 `.claude/` 遞迴清單（排除 `__pycache__/`、`worktrees/`、`*.bak`、`PROGRESS*.md`——進度檔屬各端活體，恆不同步），逐檔 `git hash-object`，產出四欄表：`相對路徑 | PLT hash（前 8 碼）| APP hash（前 8 碼）| 判定`。判定枚舉（受控字典，禁增）：`IDENTICAL`／`DIVERGED`／`PLT_ONLY`／`APP_ONLY`／`SYNC01_承接中`（status 二檔專用）。
- [x] 完成

### AI-2: DIVERGED 檔分叉定年
- **動作**: 每一 `DIVERGED` 檔於兩端各取 `git log -1 --format='%h %ci' -- <path>`，併入表格第五欄；另附 `git diff --stat`（跨 repo 以 `git diff --no-index --stat` 實作）之單行摘要。**MUST NOT** 輸出全文 diff（報告膨脹防護）。
- [x] 完成

### AI-3: 同步分類（機械規則，禁語義裁量）
- **動作**: 依矩陣既有裁定自動分類：
  - `禁搬`（§8-4 字面清單）：端專屬 hooks（mcp_service_pid_guard.py／snapshot_cueshot_memory.py）、PLT 獨有 rules（mcp-service-safety.md）、APP 獨有 rules 六檔、PLT 獨有 skills 四項、APP 獨有 skill（dtaudit）、settings.json（S-2/S-3 端專屬段落所在）、CLAUDE.md（C-1/C-2/C-3 端專屬段落所在）
  - `候選同步`：其餘 `DIVERGED` 與 `PLT_ONLY` 項
  - `待裁`：`APP_ONLY` 非上列者
  凡分類規則未涵蓋之情形 → 列 `待裁`，禁現場定奪。
- [x] 完成

### AI-4: G-3 消解與報告收束
- **動作**: `test -f` APP repo 根之 `PROGRESS.md`（G-3：APP CLAUDE.md Boot 無 `.claude/` 前綴之指涉歧義），結果入報告。報告末節 MUST 列「待 Akira 裁決清單」：①`候選同步` 全表；②CLAUDE.md／settings.json 之段落級合併是否另立票（禁整檔覆寫，C-2/C-3/S-2/S-3 端專屬內容在內）；③RAPID_MODE 是否引入 APP（C-1，涉 §0 凌駕條款，屬治理憲章級變更）；④`待裁` 項逐一。
- [x] 完成

## Hard Constraints
1. 本票 **MUST NOT** 寫入或改動 `.claude/` 下任何檔案（FORBIDDEN 已表達，此處重申語義：含「順手同步一兩個小檔」）。
2. 分類 **MUST** 依 AI-3 機械規則；規則外一律 `待裁`。
3. 報告中 status 二檔 MUST 標 `SYNC01_承接中`，禁重複列入 `候選同步`。

## Error Handling
- 跨 repo 路徑不可讀（bash cat 權限異常）→ 停止回報
- **[標配]** `git check-ignore -v plans/cso_briefing/project/file/ENVSYNC_AUDIT_20260821.md` 命中 → 停止回報（gitignore 死結律，候選 #20；INFRA-GITIGNORE-WL-01 已白名單化 file/，預期不命中，仍 MUST 實跑）

## Acceptance Criteria
- [x] **[存在性]** 報告檔在盤，四欄表列數＝兩端檔案聯集數（報告自載聯集數與表列數之相等宣告）；判定欄僅含五種受控枚舉值
- [x] **[存在性]** 每一 `DIVERGED` 列均有定年欄與 diff --stat 摘要；全檔零全文 diff 區塊
- [x] **[行為性]** 抽樣複驗：報告任取 3 檔（含至少 1 個 IDENTICAL 與 1 個 DIVERGED）重跑 `git hash-object`，與表載值一致（production 路徑重放，禁引用首跑輸出充當複驗）
- [x] **[可達性]** N/A 顯式劃記：本票無 Guard/gate/runtime state，型別不適用（比照 INFRA-CLOSURE-REGISTRY-GATE-01 揭露式劃記先例）
- [x] ~~`assembleDebug` PASS~~（N/A 顯式劃記：docs preset 唯讀盤點）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

**End of INFRA-ENVSYNC-AUDIT-01**
