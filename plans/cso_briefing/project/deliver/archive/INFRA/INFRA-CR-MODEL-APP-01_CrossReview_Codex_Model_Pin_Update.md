# INFRA-CR-MODEL-APP-01: cross_review.sh Codex 預設模型更新為 gpt-5.6-sol（APP 端同型）

**簽發日期**: 2026-09-01
**簽發者**: Claude（SMTR）
**Rev**: A
**來源議題**: INFRA
**Mode**: uc ｜ **Effort**: Low
**Traycer Workflow**: N/A（單行常數異動，零邏輯變更）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: PLT 端 `INFRA-CR-MODEL-01`（mcp-ticket-platform，seq 13，2026-09-01 CLOSED，commit `fae67fe`）；決策檔 `2026-09-01_CrossReview_Codex_Model_Pin_Update_gpt56sol`
- `coordination_dependency`: 無（PLT 端已收束，本票為 D-6「各端立票、禁單票跨 repo」之 APP 側對應）

## 檔案治理／來源說明

PLT 端 `INFRA-CR-MODEL-01` 已將 `cross_review.sh` 之 Codex 預設模型自 `gpt-5.5` 更新為 `gpt-5.6-sol`。APP 端同型缺口經 SMTR 2026-09-01 live 正向取證成立：

- `SmartCameraApp1/.claude/skills/sv/scripts/cross_review.sh` **L33**：`CODEX_MODEL="${CODEX_MODEL:-gpt-5.5}"`（與 PLT 端修正前完全同型）
- consumer 點 **L389**（`echo "   Codex: $CODEX_MODEL | effort: $CODEX_REASONING_EFFORT"`）、**L394**（`-c "model=\"$CODEX_MODEL\""`）

**授權範圍嚴格限縮**：依決策檔 KD#1，Akira 2026-08-20 之明示 supersede 裁決**僅及 `CODEX_MODEL` 一行**，禁擴張至 `effort`／`summary`／改 `auto`。固定版本紀律（KD#2）沿用，立法理由「CI-like gate 需跨 session 可重現性」未被推翻。

**supersedes 邊與 G-2 事實記載已由 PLT 端決策檔一次完成**（決策檔 KD#6），本票 **MUST NOT** 重複建邊或重複歸檔——同一 supersede 關係禁雙記。

**審查腿／修補腿分野**（決策檔 KD#3）：`cross_review.sh` 屬審查腿，維持 sol + high；rigor-gate 修補腿之 Terra medium 預設射程不及此檔，**MUST NOT** 以該裁決為由改本檔為 Terra。

## Verify Config

```
TICKET_ID=INFRA-CR-MODEL-APP-01
CREATED=2026-09-01
PRESET=fix
ALLOWED=.claude/skills/sv/scripts/cross_review.sh
FORBIDDEN=.claude/CLAUDE.md, .claude/settings.json, .claude/hooks/**, .claude/rules/**, .claude/skills/sv/SKILL.md, app/**, plans/**
MAX_LINES=6
DIFF_REF=<派工時以 `git rev-parse HEAD` 記錄實值回填>
ANCHOR_PROBED=2026-09-01 19:2x（SMTR live 直讀，L33／L389／L394）
BASE_COMMIT=<同 DIFF_REF>
TARGET_BLOB=N/A（單行常數異動，AI-0 錨點制）
```

**MAX_LINES 推導註記**（非 gate）：1 行改動 ×1.4，取整數下限 6（含可能之尾隨空白正規化）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）

- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`
- **正向錨點**（MUST 逐字命中）：
  - L33 `CODEX_MODEL="${CODEX_MODEL:-gpt-5.5}"`
  - L389 `echo "   Codex: $CODEX_MODEL | effort: $CODEX_REASONING_EFFORT"`
  - L394 `-c "model=\"$CODEX_MODEL\"" \`
- **負向錨點**（MUST 零命中）：全檔 grep `gpt-5.6-sol`
- 字面錨點僅供定位，**MUST NOT** 作為完成判定唯一依據；完成判定以下方 AC 為準。
- 任一不符 → 依 TICKET_TPL v3.8 基線漂移三分流分類回報，禁自行推定等價位置。
- [x] 完成

### AI-1: 現地核對 model slug 合法性

- **動作**: 執行 `codex debug models`，確認 `gpt-5.6-sol` 為現行合法 slug（PLT 端 AI-0 動作 B 之同型步驟）
- **目標值**: 輸出含 `gpt-5.6-sol`（display_name 預期為 `GPT-5.6-Sol`）
- **不符則停止回報**，禁以「PLT 端已驗過」略過——PLT 驗證時點為 2026-09-01 稍早，且二手 model ID 須各端現地核對（決策檔 KAIROS#2）。
- [x] 完成

### AI-2: 更新 L33 預設值

- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`
- **現值**: `CODEX_MODEL="${CODEX_MODEL:-gpt-5.5}"`（L33）
- **目標值**: `CODEX_MODEL="${CODEX_MODEL:-gpt-5.6-sol}"`
- **MUST NOT** 改動 L34／L35（`effort`／`summary` 預設），**MUST NOT** 改為 `auto`，**MUST NOT** 改動 `${VAR:-default}` 之 env override 語法結構。
- [x] 完成

## Hard Constraints

1. **MUST NOT** 改動 `CODEX_MODEL` 以外之任何常數或邏輯——授權範圍僅及一行。
2. **MUST NOT** 觸碰 `plans/**`（票務與治理載體）、`app/**`（Android 主碼）、`.claude/` 下除 ALLOWED 以外任何檔案。
3. **MUST NOT** 於本票建立任何 `supersedes` 圖譜邊或執行 `archive_decision`——PLT 端決策檔已一次完成（決策檔 KD#6）。
4. **MUST NOT** 破壞 env override：`CODEX_MODEL=gpt-5.5 ./cross_review.sh` 仍須生效。
5. 本票屬 SmartCameraApp1 隔離專案：冊面敘事註記 **MUST** 走 `registry_set_checkpoint`，**MUST NOT** 用 `registry_append_note`（該通道對本專案恆回 `REGISTRY_CROSS_PROJECT_REF`）。

## Error Handling

- AI-0 任一正向錨點不符 → 三分流分類回報
- 負向錨點 `gpt-5.6-sol` 已命中 → 停止回報（他人已實作）
- AI-1 之 `codex debug models` 無 `gpt-5.6-sol` → 停止回報，附完整輸出，禁寫入未驗證 slug
- 修改後腳本語法檢查（`bash -n`）失敗 → 停止回報
- **[標配]** `git check-ignore -v .claude/skills/sv/scripts/cross_review.sh` 命中 → 停止回報（gitignore 死結律）

⚠️ DEFENSIVE WARNING: Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

## Acceptance Criteria

- [x] **[存在性]** AC-201：`cross_review.sh` 全檔 grep `gpt-5.5` 命中數 **1→0**（記錄前後實數）
- [x] **[存在性]** AC-202：全檔 grep `gpt-5.6-sol` 命中數 **0→1**
- [x] **[存在性]** AC-203：L34／L35（`effort`／`summary` 預設）逐字未變；consumer 點 L389／L394 逐字未變
- [x] **[可達性]** AC-204：`bash -n .claude/skills/sv/scripts/cross_review.sh` exit 0
- [x] **[行為性]** AC-205：given 腳本主路徑實際執行（非 grep 推定）；expect 輸出行含 `gpt-5.6-sol | effort: high`
- [x] **[行為性]** AC-206：given `CODEX_MODEL=gpt-5.5` 環境變數；expect 輸出行含 `gpt-5.5`——env override 未被破壞之回歸證明
- [x] `git diff --stat` 僅涉 `.claude/skills/sv/scripts/cross_review.sh`，且為 +1/−1

## 收尾義務

1. 冊面敘事註記走 `registry_set_checkpoint`（HC-5），記錄 AC-201/202 之前後實數與 AI-1 之 `codex debug models` 輸出摘要。
2. **MUST NOT** 重複歸檔決策——PLT 端 `2026-09-01_CrossReview_Codex_Model_Pin_Update_gpt56sol` 為本異動之唯一決策 SSOT，本票僅為其 APP 側執行。
