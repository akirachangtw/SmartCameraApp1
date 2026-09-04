# INFRA-TOOLDESC-APP-01: APP 端移植「治理載體↔存取工具對應圖譜」規則條文

**簽發日期**: 2026-09-02
**簽發者**: Claude（SMTR）
**Rev**: A（2026-09-02 票號前綴自 `MCP-` 改為 `INFRA-`，Akira 裁決；派工前重命名，非修訂輪）
**來源議題**: INFRA
**Mode**: uc ｜ **Effort**: Low
**Traycer Workflow**: N/A（純規則文本新增，零程式碼、零邏輯）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: PLT 端 `MCP-TOOLDESC-01`（mcp-ticket-platform，seq 14，2026-09-01 CLOSED，commit `dc59546a`／`ef74ee12`）
- `coordination_dependency`: 無

## 檔案治理／來源說明

**本票非 PLT 端之「同型票」，而是單層移植票。** PLT `MCP-TOOLDESC-01` 有三層（工具層 docstring／Server 層 `instructions=`／規則層），前二層皆位於 `apps/mcp_server/main.py`——**該檔僅存在於 mcp-ticket-platform repo**，APP repo 無 MCP server 原始碼。故 APP 端可行者僅規則層。

缺口 SMTR 2026-09-02 live 取證成立：`SmartCameraApp1/.claude/rules/ssot-governance.md`（9956 B，七節）全檔 grep `get_ticket`／`registry_list`／`執行審批票`／`存取工具` **均零命中**（正向對照 `^## ` 命中七節，證 grep 有效）。

**APP 端之本地化必要性**：PLT 條文不可逐字照抄，因 APP 端有三處實質差異——冊面 SSOT 路徑不同（`plans/cso_briefing/project/TICKET_REGISTRY.json`）、票檔目錄不同（`Pending/`／`deliver/`，`archive/` 為人工批次專用）、且 **APP 為隔離專案，`registry_append_note` 對本專案恆回 `REGISTRY_CROSS_PROJECT_REF`**，敘事註記唯一合法通道為 `registry_set_checkpoint`。第三點是 APP 端獨有且發生頻率高於 `get_ticket` 誤用之工具選擇陷阱，MUST 納入條文。

**範圍**：單檔、單節新增。零程式碼、零 `apps/**`、零 `plans/**`。

**票號前綴裁決（2026-09-02，Akira）**：本票原開為 `MCP-TOOLDESC-APP-01`，因 APP 冊面無 `MCP` 議題，該前綴將依票號議題前綴律建立新 topic 並於日後歸檔時新增 `archive/MCP/` 目錄；Akira 裁定改用既有之 `INFRA-` 前綴以免歸檔目錄增生。連帶：冊面 `topic` 自 `MCP` 改為 `INFRA`、`seq` 自 1（`MCP` 群組）改為 28（`INFRA` 群組 max+1，既有為 20／23／24／27）、`file` 指標與票檔檔名同步更名。此為**派工前重命名**，不計入修訂版次。

## Verify Config

```
TICKET_ID=INFRA-TOOLDESC-APP-01
CREATED=2026-09-02
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md
FORBIDDEN=.claude/CLAUDE.md, .claude/settings.json, .claude/hooks/**, .claude/skills/**, .claude/rules/mcp-service-safety.md, .claude/rules/ticket_pattern.md, app/**, plans/**
MAX_LINES=35
DIFF_REF=cbd81c1d
ANCHOR_PROBED=2026-09-02 00:5x（SMTR live 直讀）
BASE_COMMIT=cbd81c1d
TARGET_BLOB=N/A（docs 型單檔小改，AI-0 錨點制）
```

**MAX_LINES 推導註記**（非 gate）：條文約 25 行 ×1.4 → 35。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）

- **檔案**: `.claude/rules/ssot-governance.md`
- **正向錨點**（MUST 命中，行號僅供定位）：`## Ticket Closure 紀律`（L54）、`## SSOT 版本引用紀律`（L47）、`## Governance Artifact 清單（單一真相來源）`（L13）
- **負向錨點**（MUST 零命中）：全檔 grep `get_ticket`、`registry_list`、`執行審批票`、`存取工具`
- 字面錨點 **MUST NOT** 作為完成判定唯一依據；完成判定以 AC 為準。
- 任一不符 → 依 TICKET_TPL v3.9 基線漂移三分流分類回報，禁自行推定等價位置。
- [x] 完成

### AI-1: 新增「治理載體↔存取工具對應圖譜」節

- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 全檔零處說明兩套票系統之分野，零處出現工具選用判準
- **目標值**: 於 `## Ticket Closure 紀律`（L54）**之前**新增 `## 治理載體↔存取工具對應圖譜` 節，MUST 含六要素：
  1. **兩套系統明示分立**：治理冊面（`plans/cso_briefing/project/TICKET_REGISTRY.json`，即本規則全篇治理對象）與執行審批票（SQLite，mcp-ticket-platform repo 之 `core/migrations/001_init_v2.sql`）為**互不相通之兩套資料**，同名概念不共享資料。工具由同一台 `mcp-ticket-platform` MCP server 提供，服務兩專案。
  2. **工具對應表**：治理冊面用 `registry_list`／`registry_upsert_ticket`／`registry_set_status`／`registry_set_checkpoint`／`registry_archive_tickets`，**均需 `project=SmartCameraApp1`**；執行審批票用 `create_ticket`／`get_ticket`／`list_tickets`／`approve_ticket`／`execute_approved_ticket`，**單鍵 `ticket_id`、無 `project` 參數**。
  3. **機械判準**（RFC 2119）：查詢本規則所治理之票（`Pending/`／`deliver/` 下票檔所對應者）**MUST** 用 `registry_list`；**MUST NOT** 用 `get_ticket`。
  4. **誤用簽名**：`get_ticket` 對治理冊面票**恆回 `NOT_FOUND`**——該回應為正確行為（查的是另一個資料庫），**MUST NOT** 被解讀為「該票未入冊」或「registry write-back 未完成」。
  5. **APP 端專屬約束**：本專案為隔離專案，`registry_append_note` **恆回 `REGISTRY_CROSS_PROJECT_REF`**，敘事註記唯一合法通道為 `registry_set_checkpoint`，且敘事欄位 **MUST** 置於 `checkpoint_data` 物件內——未列於該工具 schema 之參數（如頂層 `summary`）會被**靜默丟棄且仍回 `SUCCESS`**，故寫入後 **MUST** 以 `registry_list fields=["checkpoint"]` read-back 驗證（2026-09-02 實測：頂層 `summary` 寫入後 `checkpoint` 為 `{}`）。
  6. **深入參照**：`mcp-ticket-platform` repo 之 `docs/deliver/archive/OTHERS/tool-contracts.md` L404-406 與 L598 起（**跨 repo 唯讀參照**；本專案不持有該檔）。
- **MUST NOT** 改動本檔其餘任何條文；既有七節字面逐字保留。
- [x] 完成

## Hard Constraints

1. **MUST NOT** 觸碰 `app/**`（Android 主碼）與 `plans/**`（票務與治理載體）。
2. **MUST NOT** 觸碰 `.claude/` 下除 ALLOWED 以外任何檔案——特別是 `ticket_pattern.md`、`mcp-service-safety.md`、`skills/**`。
3. **MUST NOT** 於本票新增、修改或移除任何工具程式碼——APP repo 無 MCP server 原始碼，任何「順手補 docstring」之嘗試即越界。
4. **MUST NOT** 逐字照抄 PLT 條文——三處本地化差異（冊面路徑／票檔目錄／`append_note` 封鎖）MUST 反映 APP 實況。
5. **MUST NOT** 於條文中寫入未經 live 復驗之檔案路徑。
6. 本專案隔離：冊面敘事註記 **MUST** 走 `registry_set_checkpoint(checkpoint_data=...)`，**MUST NOT** 用 `registry_append_note`。

## Error Handling

- AI-0 任一正向錨點不符 → 依 TPL v3.9 三分流分類回報
- 任一負向錨點已命中（他人已實作）→ 停止回報，附命中行號
- `docs/deliver/archive/OTHERS/tool-contracts.md` 於 mcp-ticket-platform repo 不存在 → 停止回報，附 `list_directory` 結果，禁猜測路徑
- 既有七節任一字面遭改動 → 停止回報，禁自行還原後續行
- **[標配]** `git check-ignore -v .claude/rules/ssot-governance.md` 命中 → 停止回報（gitignore 死結律）

⚠️ DEFENSIVE WARNING: Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

## Acceptance Criteria

- [x] **[存在性]** AC-301：`.claude/rules/ssot-governance.md` 含 `## 治理載體↔存取工具對應圖譜` 節
- [x] **[存在性]** AC-302：該節內 grep `registry_list` ≥1、`get_ticket` ≥1、`MUST NOT` ≥1、`NOT_FOUND` ≥1、`REGISTRY_CROSS_PROJECT_REF` ≥1、`checkpoint_data` ≥1
- [x] **[存在性]** AC-303：該節位於 `## Ticket Closure 紀律` 之前；既有七節（`Governance Artifact 清單`／`PROGRESS.md Dashboard 紀律`／`Pipeline Artifact 豁免邊界`／`PROGRESS_ARCHIVE.md Sprint 邊界治理`／`SSOT 版本引用紀律`／`Ticket Closure 紀律`／`Ticket Rigor Gate`）標題與內容逐字未變
- [x] **[存在性]** AC-304：該節含 `project=SmartCameraApp1` 字面（證已本地化，非照抄 PLT 之 `project=mcp-ticket-platform`）
- [x] **[行為性]** AC-305：given 僅讀 `.claude/rules/ssot-governance.md`（不讀任何工具 description、不讀 server `instructions=`）之情境；expect 可據該節單獨判定「查 `INFRA-CR-MODEL-APP-01` 應用 `registry_list project=SmartCameraApp1 topic=INFRA`」——規則層具**獨立可用性**
- [x] **[行為性]** AC-306：given 僅讀該節之情境；expect 可判定「本專案敘事註記須用 `registry_set_checkpoint` 且欄位置於 `checkpoint_data`」——APP 專屬約束之獨立可用性
- [x] `git diff --stat` 僅涉 `.claude/rules/ssot-governance.md`（Implementation diff；全 payload 另含 Pipeline Artifacts `PROGRESS.md`／`PROGRESS_ARCHIVE.md`，經 R2 Checkpoint Akira 選項A裁決確認豁免範圍）

## 收尾義務

1. 冊面敘事註記走 `registry_set_checkpoint(checkpoint_data=...)`（HC-6），並以 `registry_list fields=["checkpoint"]` read-back 驗證後方可回報完成。
2. **MUST NOT** 重複歸檔決策——PLT 端 `MCP-TOOLDESC-01` 之裁決鏈為唯一 SSOT，本票僅為 APP 側規則層移植。
