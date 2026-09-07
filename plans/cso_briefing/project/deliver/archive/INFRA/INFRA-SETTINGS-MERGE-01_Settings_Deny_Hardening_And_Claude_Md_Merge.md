# INFRA-SETTINGS-MERGE-01: settings.json deny 補強＋CLAUDE.md 段落級合併（RAPID 引入案已撤回）

**簽發日期**: 2026-08-21 ｜ **改號日期**: 2026-08-22
**簽發者**: Claude（SMTR）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（.claude/ 治理文本定點插入；APP 端 RAPID **不引入**，本票依 `.claude/CLAUDE.md` SOP 全程序執行 QA／SV／Cross-Review，本欄 N/A 係依治理票免 Traycer 先例，非援引 §0）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ENV_DIFF_MATRIX v1.0（S-4 deny 差集；C-2/C-3/S-2/S-3 端專屬段落清單）；INFRA-ENVSYNC-AUDIT-01 checkpoint（兩檔禁整檔覆寫裁定）
- `coordination_dependency`: INFRA-ENVSYNC-BATCH-01、INFRA-TRACKSCOPE-01（ALLOWED 三票兩兩零相交，可並行）

## 改號與範圍撤回說明（2026-08-22）

**原票號**: `INFRA-CLAUDE-MERGE-01`（2026-08-21 簽發，含 RAPID_MODE 範圍限定版引入）
**現票號**: `INFRA-SETTINGS-MERGE-01`（Akira 2026-08-22 裁決「即修建議降為 INFRA-SETTINGS-MERGE-01」）

**撤回事由（前提消滅型）**：原票 AI-1~AI-4 之全部依據為 Akira 2026-08-21「暫時引入 RAPID_MODE」裁決；該裁決已由 Akira 2026-08-22 **「RAPID 裁決＝不引入（維持 APP 全程序現狀）」翻盤**。前提消滅，四項 AI 一併退役。原票號生命週期於改號同時終止，**MUST NOT** 另行派工或收束。

**原 AI-1~AI-4 退役清單（禁靜默刪除，保留可考）**：
| 原 AI | 內容 | 退役型態 |
|-------|------|---------|
| ~~AI-1~~ | 建立 `docs/RAPID_MODE.md`（APP 試行限定版，含 §-1 fail-closed 適用範圍節） | 前提消滅型 |
| ~~AI-2~~ | CLAUDE.md Boot Sequence 插入 RAPID 讀取＋Boot Report `🚀 RAPID MODE:` 行 | 前提消滅型 |
| ~~AI-3~~ | CLAUDE.md 從屬條款（規則從屬 RAPID §2 豁免枚舉） | 前提消滅型 |
| ~~AI-4~~ | CLAUDE.md SOP 節「跳過 /qa、/sv、cross_review、Traycer」行 | 前提消滅型 |

**存續範圍**：原 AI-5（settings.json deny 補強）升為本票 AI-1；另補入原票未涵蓋、但屬 AUDIT-01「段落級合併」裁定射程之 CLAUDE.md 定點修正（AI-2）。`docs/RAPID_MODE.md` 自 ALLOWED 移除——APP 端**不建立**該檔。

## 檔案治理／來源說明

矩陣 S-4：APP `settings.json` 之 deny 陣列較 PLT 少二則防護（`git -C * reset --hard`、`git -C * push --force`）。APP 既有 deny 含 `"Bash(git reset --hard *)"`，但**不涵蓋 `-C <path>` 形式**——即於 APP repo 內對他 repo 下達 `git -C ../other reset --hard` 不被攔截。本票補齊該缺口。

端專屬段落（C-2 架構 SSOT 引用、C-3 `.kt`/`.xml` 副檔名字面、Rule 4a provenance 註記、S-2 cueshot-memory hook、S-3 adb／emulator／gradlew 權限族、telegram hooks）**全數保留不動**——本票僅得執行定點插入，禁整檔覆寫（AUDIT-01 checkpoint 裁定）。

## Verify Config
TICKET_ID=INFRA-SETTINGS-MERGE-01
CREATED=2026-08-21（改號 2026-08-22）
PRESET=docs
ALLOWED=.claude/settings.json,.claude/CLAUDE.md
FORBIDDEN=.claude/rules/**,.claude/skills/**,.claude/hooks/**,.claude/settings.local.json,app/**,docs/RAPID_MODE.md
MAX_LINES=N/A
DIFF_REF=字面佔位待開工回填
ANCHOR_PROBED=2026-08-21（APP CLAUDE.md 全文＋settings.json 全文 SMTR live 直讀）；改號後錨點未再實測，派工時執行端 MUST 全數重跑 AI-0

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- APP `.claude/settings.json`：`"Bash(git reset --hard *)"` 唯一命中（deny 陣列內）；`git -C * reset --hard` 命中＝**0**；`git -C * push --force` 命中＝**0**
- APP `.claude/CLAUDE.md`：`## SOP` 唯一命中；全檔 `RAPID` 命中＝**0**（若 ≠0 → 停止回報：表示 RAPID 已由他途引入，與 2026-08-22 不引入裁決矛盾）
- APP repo 內 `docs/RAPID_MODE.md` **不存在**（`test -f` 應失敗；存在 → 停止回報升裁）
- 任一不符 → 停止回報
- [x] 完成

### AI-1: settings.json deny 補強（S-4；承原 AI-5）
- **檔案**: `.claude/settings.json`
- **動作**: deny 陣列增二則，與 PLT 同字面：`"Bash(git -C * reset --hard *)"`、`"Bash(git -C * push --force *)"`
- **MUST** 僅在 deny 陣列內插入；allow／ask 陣列與其餘鍵零觸碰。插入後 **MUST** 以 `python -c "import json,sys;json.load(open('.claude/settings.json'))"` 驗 JSON 合法（尾逗號為此類編輯之常見致命錯）
- [x] 完成

### AI-2: CLAUDE.md 治理現況語境句（段落級合併殘餘項）
- **檔案**: `.claude/CLAUDE.md`
- **現值**: `## SOP` 標題行
- **目標值**: 於該標題行**之後、SOP 代碼塊之前**插入單行語境句（逐字）：
  `> 本 repo 全程序執行，無 RAPID／快速通道；任何票（含 .claude/ 與 plans/ 治理文本票）均不得跳過 3./qa、4./sv、4.5.cross_review（Akira 2026-08-22 裁決）。`
- **理由**：不引入之裁決若僅存於冊面與決策檔，執行端 session 無從得知；本句為該裁決之**行為契約層落點**，防止日後以「PLT 有 RAPID、兩端應同步」為由再度引入。
- [x] 完成

## Hard Constraints
1. **MUST NOT** 於本 repo 建立 `docs/RAPID_MODE.md` 或任何等效豁免載體（FORBIDDEN 已列，此處重申語義：含以其他檔名、其他目錄承載同等豁免枚舉者）。
2. **MUST NOT** 改動 CLAUDE.md／settings.json 之端專屬段落（見「檔案治理」節清單）；本票僅得執行 AI-1／AI-2 之定點插入。
3. CLAUDE.md 落地後 **MUST** 仍 ≤150 行；超標 → 停止回報升裁（禁自行精簡他段落換行數）。
4. **MUST NOT** 復活原 AI-1~AI-4 之任何內容（前提消滅型退役，禁以「順手一併做」復活）。

## Error Handling
- AI-0 任一不符 → 停止回報
- AI-1 JSON 驗證失敗 → `git restore .claude/settings.json` 後停止回報（禁帶病續行）
- AI-2 插入後行數 >150 → 停止回報升裁
- **[標配]** `git check-ignore -v .claude/settings.json .claude/CLAUDE.md` 命中 → 停止回報（gitignore 死結律）

## Acceptance Criteria
- [x] **[存在性]** `settings.json` 內 `git -C * reset --hard`、`git -C * push --force` 各唯一命中；JSON 解析成功（回填：`python -c "import json,sys;json.load(open('.claude/settings.json'))"` 無錯誤輸出）
- [x] **[存在性]** `CLAUDE.md` 含 `本 repo 全程序執行，無 RAPID` 字面；全檔 `docs/RAPID_MODE.md` 命中＝**0**；行數 ≤150（回填：實測 148 行）
- [x] **[存在性]** 負向：repo 內 `RAPID_MODE` 檔名之 `git ls-files` 命中＝**0**（HC-1 之正向取證）
- [ ] **[行為性]** deny 生效實測：**PARTIAL**（回填：`git -C .. reset --hard HEAD` 含尾隨引數 → 被 permission 拒絕；`git -C .. reset --hard` 裸指令 → 未攔截，實際執行並回傳 git 自身錯誤，非 permission 拒絕。`Bash(...)` pattern 之 `*` 匹配語義缺口，PLT 同字面規則推定共享同一缺口。Hard Constraint 2 不授權本票自行擴寫 pattern，禁推定勾選，追蹤：待 Akira/CSO 裁決是否開後續票收斂裸指令覆蓋——見 `sv_report.md` AC-4 段落與 checkpoint.resolution）
- [x] **[可達性]** N/A 顯式劃記：本票標的為 Claude Code 設定與行為契約文本，無 `app/src/main/` production producer，型別不適用（非規避）
- [x] ~~`assembleDebug` PASS~~（實際仍執行並 PASS，42s；沿用 `INFRA-TOOLDESC-APP-01` 先例，docs preset 票仍跑全量 build 取強證據，非跳過）
- [x] `git diff --stat` 僅涉及 ALLOWED 二檔（+ `.claude/PROGRESS.md` Pipeline Artifact 豁免）

**End of INFRA-SETTINGS-MERGE-01**
