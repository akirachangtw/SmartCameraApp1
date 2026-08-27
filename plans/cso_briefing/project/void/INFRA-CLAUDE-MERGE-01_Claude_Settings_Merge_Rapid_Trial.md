# INFRA-CLAUDE-MERGE-01: CLAUDE.md／settings.json 段落級合併＋RAPID_MODE 試行引入（範圍限定版）

**簽發日期**: 2026-08-21
**簽發者**: Claude（SMTR；Akira 2026-08-21 裁決：「INFRA 跟 Skill 相關的文檔不需要／可跳過 QA/SV 部分項目，暫時引入 RAPID_MODE」）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（.claude/ 治理文本；本票落地前 APP 無 RAPID，依 APP 全程序跑 Codex）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ENV_DIFF_MATRIX v1.0（C-1/C-2/C-3/S-2/S-3/S-4）；INFRA-ENVSYNC-AUDIT-01 checkpoint（CLAUDE.md／settings.json 禁整檔覆寫裁定）
- `coordination_dependency`: INFRA-ENVSYNC-BATCH-01（ALLOWED 不相交可並行）

## 檔案治理／來源說明

矩陣 C-1：RAPID_MODE 為兩端最大單一差異。Akira 裁決**暫時（試行）引入 APP**，其字面理由限定於「INFRA 跟 Skill 相關的文檔」——故本票引入之 RAPID 為**範圍限定版**，非 PLT 之全 repo 版；產品碼票（Cueshot 線、app/**、.kt/.xml）**一律不適用**，QA/SV/Cross-Review 全程序照舊。另承接矩陣 S-4（APP deny 較 PLT 少二則防護）與 AUDIT-01 裁定之「兩檔段落級合併、禁整檔覆寫」。端專屬段落（C-2 架構 SSOT 引用、C-3 副檔名字面、S-2 cueshot-memory hook、S-3 adb 權限族）全數保留不動。

## Verify Config
TICKET_ID=INFRA-CLAUDE-MERGE-01
CREATED=2026-08-21
PRESET=docs
ALLOWED=.claude/CLAUDE.md,.claude/settings.json,docs/RAPID_MODE.md
FORBIDDEN=.claude/rules/**,.claude/skills/**,.claude/hooks/**,app/**
MAX_LINES=N/A（含 fail-closed 適用性判定條款，失敗形狀為審查發現式）
DIFF_REF=字面佔位待開工回填
ANCHOR_PROBED=2026-08-21（APP CLAUDE.md 全文＋settings.json 全文本 session live 直讀；PLT docs/RAPID_MODE.md 存在性由 PLT CLAUDE.md Boot 條文佐證，AI-0 補實測）

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- APP `.claude/CLAUDE.md` 唯一命中：`## ⚡ Boot Sequence`、`## SOP`、`→ 讀 PROGRESS.md → 輸出 Boot Report`；全檔 `RAPID` 命中＝0。
- APP `.claude/settings.json` 唯一命中：`"Bash(git reset --hard *)"`（deny 陣列內）；`git -C * reset --hard` 命中＝0。
- PLT `D:/DevSources/workflow/mcp-ticket-platform/docs/RAPID_MODE.md` 存在（`test -f`）且含 `§0` 字面。
- 任一不符 → 停止回報。
- [ ] 完成

### AI-1: 建立 `docs/RAPID_MODE.md`（APP 試行限定版）
- **動作**: 以 PLT 端 `docs/RAPID_MODE.md` 為底本整檔複製，於**檔首（標題行之後）**插入以下節（逐字）：

  `## §-1 適用範圍（APP 試行限定，凌駕本檔其餘任何較寬適用敘述）`
  `- 本檔僅適用於同時滿足下列全部條件之票：PRESET=docs，且 ALLOWED 全數位於 .claude/**、docs/**、plans/cso_briefing/** 之內，且來源議題為 INFRA 線（票號 INFRA- 前綴）。`
  `- 任何 ALLOWED 或實際 diff 觸及 app/**、*.kt、*.xml 之票一律不適用；QA／SV／Cross-Review／Traycer 依 CLAUDE.md 全程序照舊，禁援引本檔任何豁免。`
  `- 適用性判定為 fail-closed：任一條件無法機械確認即視為不適用。`
  `- 試行狀態：本檔為暫時引入（Akira 2026-08-21 裁決）；常設化或撤除由 Akira 另行裁決，撤除前本節禁修改。`
- 若底本內含與本節直接矛盾且無法以「本節凌駕」語義消解之條款 → 停止回報升裁。
- [ ] 完成

### AI-2: CLAUDE.md Boot Sequence 插入 RAPID 讀取
- **現值**: `→ 讀 PROGRESS.md → 輸出 Boot Report`
- **目標值**: `→ 讀 PROGRESS.md → 讀 docs/RAPID_MODE.md（存在即生效，適用範圍以該檔 §-1 限定節為準）→ 輸出 Boot Report`
- 並於 Boot Report 代碼塊 `📊` 行之後插入一行：`🚀 RAPID MODE: [ON（docs/RAPID_MODE.md 存在且本票落於 §-1 適用範圍）/ OFF]`
- [ ] 完成

### AI-3: CLAUDE.md 從屬條款（Boot 段末插入，逐字）
- `RAPID MODE ON 且本票落於 §-1 適用範圍時，本檔規則從屬 docs/RAPID_MODE.md §2 豁免枚舉；範圍外之票（含一切產品碼票）不受任何影響。§0 第一原則僅於適用範圍內援引。`
- [ ] 完成

### AI-4: CLAUDE.md SOP 節補跳過行（逐字）
- 於 SOP 代碼塊後插入：`RAPID MODE ON（限 §-1 範圍內之票）：跳過 3./qa、4./sv、4.5.cross_review、5.Traycer，改走 0→1→2→6.Git。`
- [ ] 完成

### AI-5: settings.json deny 補強（S-4）
- **動作**: deny 陣列增二則：`"Bash(git -C * reset --hard *)"`、`"Bash(git -C * push --force *)"`（與 PLT 同字面）。
- [ ] 完成

## Hard Constraints
1. **MUST NOT** 改動 CLAUDE.md 端專屬段落（Rule 1 之 SSOT_v4_0_7 引用、Rule 3 之 .kt/.xml 字面、Rule 4a provenance 註記、SSOT 節、Skills 節）與 settings.json 端專屬段落（adb／emulator／gradlew 權限族、cueshot-memory hook、telegram hooks）——本票僅得執行 AI-2~AI-5 之定點插入。
2. CLAUDE.md 落地後 **MUST** 仍 ≤150 行；超標 → 停止回報升裁（禁自行精簡他段落換行數）。
3. `docs/RAPID_MODE.md` 除 §-1 插入節外 **MUST** 與 PLT 底本位元組一致（`git diff --no-index` 僅顯示 §-1 區塊）。
4. **MUST NOT** 將 RAPID 適用範圍擴至 Cueshot／GAP／STATE／TRACTION 等產品線票（含以「也是文檔改動」為由）。

## Error Handling
- AI-0 任一不符 → 停止回報
- 底本矛盾（AI-1 末項）→ 停止回報升裁
- **[標配]** `git check-ignore -v .claude/CLAUDE.md .claude/settings.json docs/RAPID_MODE.md` 命中 → 停止回報（#20；docs/ 為 APP 新目錄，命中風險非零，MUST 實跑）

## Acceptance Criteria
- [ ] **[存在性]** `docs/RAPID_MODE.md` 在盤且 `§-1 適用範圍（APP 試行限定` 字面命中；`試行狀態` 命中；除 §-1 外與 PLT 底本 diff 為零
- [ ] **[存在性]** CLAUDE.md `RAPID` 引用恰 4 處（Boot 讀取／Boot Report 行／從屬條款／SOP 跳過行）且行數 ≤150；settings.json 新 deny 二則命中各 1
- [ ] **[行為性]** 實際 session Boot Report 顯示 `🚀 RAPID MODE:` 行（ON 或 OFF 均可，行存在即證接線）；並以一張 §-1 範圍內票之 Boot 實測 ON、敘明依據
- [ ] **[可達性]** 適用性判定之 input＝票面 PRESET／ALLOWED 欄與票號前綴（producer＝TPL v3.7 必填欄，證據欄：TPL 章節引註，執行後回填）
- [ ] ~~`assembleDebug` PASS~~（N/A：docs preset，零 build 面觸碰）
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

**End of INFRA-CLAUDE-MERGE-01**
