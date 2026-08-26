# INFRA-TPL-V38-01: TPL v3.7→v3.8 — Session-Aware 驗證條款（P0＋P1，QA 草案 Rev B 採納落地）

**簽發日期**: 2026-08-23 ｜ **簽發者**: Claude（SMTR；Akira 2026-08-23 裁決「同意 QA 修補（R1-R5）之採納與開票授權」）
**來源議題**: INFRA
**Mode**: uc ｜ **Effort**: Medium
**Traycer Workflow**: N/A（TPL 純文本修訂，比照 INFRA-TPL-V32-01～V37-01 家族先例；APP 無 RAPID，QA／SV／Cross-Review 依全程序）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `Skill-Advisor/deliver/QA-Session-Aware-Verification.md` Rev B（含 R1–R5 採納條文）；INFRA-MAXLINES-GATE-01 checkpoint（ALREADY_EXECUTED 原型案例）；INFRA-TRACKSCOPE-01 Rev A（負向錨點未取證即設之同族案例）
- `coordination_dependency`: 在途五票（BATCH-01／TRACKSCOPE-01／SETTINGS-MERGE-01／CROSSREVIEW-GOVPATH-EXCL-01／MAXLINES-BACKPORT-01）依 R4 祖父條款不受本票追溯；P2（activation protocol 腳本）／P3（per-ticket config）待本票 CLOSED 後另票

## 檔案治理／來源說明

QA 草案診斷：現行 AI-0 以「唯一字面命中數」同時承擔定位、前提驗證、完成判定、防錯票四責任，repo 正常演進即引爆假停（實證：INFRA-MAXLINES-GATE-01 2026-08-23 假停——票已由自身 commit `0bb6b4c` 完成，錨點描述的是施作前狀態）。本票將 P0（凍結新開「唯一字面作唯一授權」票面）與 P1（模板增訂）落入 TPL SSOT。**標的為 `file/` 內之版控 SSOT**（TRACKSCOPE-01 目標態保留側，Akira 2026-08-22 確認）。

## Verify Config
TICKET_ID=INFRA-TPL-V38-01
CREATED=2026-08-23
PRESET=docs
ALLOWED=plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md
FORBIDDEN=.claude/**,plans/cso_briefing/project/Pending/**,plans/cso_briefing/project/deliver/**,app/**
MAX_LINES=N/A
DIFF_REF=字面佔位待開工回填
ANCHOR_PROBED=2026-08-23（SMTR live grep 全檔標題結構＋下列錨點行號：L3 版本行／L38 ANCHOR_PROBED 欄／L53 AI-0 強制首項塊／L139 AC 強制規則／L141 MAX_LINES 退出條款／L270 Revision 歷史；派工時執行端 MUST 重跑）

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- 唯一命中：`**版本**: v3.7`、`ANCHOR_PROBED=[YYYY-MM-DD hh:mm]`、`> **AI-0 為強制首項**`、`### AC 類型學（v3.1，INFRA-REACH-02 AI-1）`、`## Revision 歷史`
- 全檔零命中：`BASE_COMMIT=`、`TARGET_BLOB=`、`ALREADY_EXECUTED`、`semantic_contract`
- 任一不符 → 停止回報
- [x] 完成

### AI-1: 版本行 v3.7 → v3.8
- **現值**: `**版本**: v3.7` ｜ **目標值**: `**版本**: v3.8`
- [x] 完成

### AI-2: Verify Config 增二欄（R3——KV 整合，禁 YAML 孤島）
- **現值**: `ANCHOR_PROBED=[YYYY-MM-DD hh:mm]`
- **目標值**（於該行之後追加二行＋說明）:
  `BASE_COMMIT=[開票時 HEAD，票面前提所依之不可變基線 commit]`
  `TARGET_BLOB=[逐標的 git hash-object，格式 path:sha8，多筆分號分隔]`
  並於欄位說明段落補：二欄為 V1 baseline 判定之機械輸入；`docs` preset 且單檔小改之票得標 `N/A`（比例原則），標 N/A 者 V1 降級為 AI-0 錨點制。
- [x] 完成

### AI-3: AI-0 標準文塊增訂「基線漂移三分流」（R1＋R2 核心）
- **現值**: `> **AI-0 為強制首項**` 起始之引言塊（至其段落終了）
- **目標值**: 於該塊**之後**追加新引言塊，逐字要點：
  1. **範圍不相交自動通過（R1）**：AI-0 錨點不符時，MUST 先跑 `git diff --name-only <BASE_COMMIT>..HEAD -- <ALLOWED 各標的>`；輸出為**空** → 漂移未及本票標的，錨點失配屬票面撰寫層問題，停止並回報「ANCHOR_STALE_SCOPE_INTACT」；輸出**非空** → 進入歸因判定。
  2. **歸因機械判準（R2）**：清點 BASE_COMMIT..HEAD 間觸及標的之 commit 前綴——含**本票 ID** → 回報 `ALREADY_EXECUTED_PENDING_CLOSURE`（直送 SMTR 收束審查，禁重複施作、禁自行勾選）；含**他票 ID** → 回報 `CANDIDATE_SUPERSEDED`（附該票 ID，禁自判等價）；**無前綴 commit** 觸及標的 → 回報 `BLOCKED_UNATTRIBUTED_STATE`（唯讀取證後升裁）。
  3. 三態均為**停止＋分類回報**，禁「推定等價位置」續作——本條取代舊制單一「不符即 STOP 無分類」，使停止報告自帶處置路由。
- [x] 完成

### AI-4: AC 類型學段增訂 semantic contract 映射（R3）
- **現值**: `**強制規則**：AC 涉 Guard / gate / early-return / 外生 runtime state` 起始行
- **目標值**: 於該段**之後**追加：**語義契約條款（v3.8）**——涉行為變更之票，`[行為性]` AC SHOULD 以 given/expect 形式書寫（即 semantic contract 之 TPL 原生載體），使其可承受重構、改名與行號位移；**MUST NOT** 另立 YAML/JSON 平行契約區塊（禁雙軌）。字面錨點（AI 現值/目標值）保留定位用途，**MUST NOT** 作為完成判定之唯一依據（P0 凍結：v3.8 起新票禁以「唯一字面命中」承載前提驗證＋完成判定雙責任）。
- [x] 完成

### AI-5: Revision 歷史增列 v3.8 條目（R4 祖父條款入史）
- **現值**: `## Revision 歷史` 標題行
- **目標值**: 於該節**既有末條之後**追加：`**Revision**: v3.8（2026-08-23，INFRA-TPL-V38-01）— ①Verify Config 增 BASE_COMMIT／TARGET_BLOB 欄（V1 baseline 機械化）；②AI-0 基線漂移三分流（範圍不相交自動通過／ALREADY_EXECUTED_PENDING_CLOSURE／CANDIDATE_SUPERSEDED／BLOCKED_UNATTRIBUTED_STATE）；③[行為性] AC given/expect 語義契約化，禁 YAML 雙軌；④P0 凍結唯一字面雙責任票面。**forward-only**：既有在途票之 AI-0 依原條款執行（祖父條款），不追溯。依據 QA-Session-Aware-Verification Rev B（R1–R5）＋ INFRA-MAXLINES-GATE-01 假停原型案例。`
- [x] 完成

## Hard Constraints
1. **MUST NOT** 刪改既有任何條款（v3.1–v3.7 全數保留）；本票純增訂。
2. **MUST NOT** 引入 YAML 區塊（R3 禁雙軌）。
3. **MUST NOT** 觸及在途五票票面（R4 forward-only）。
4. TPL 為單載體 SSOT（v3.7 裁決）——**MUST NOT** 於任何他處建立副本或摘錄版。

## Error Handling
- AI-0 任一不符 → 依本票 AI-3 所立之新三分流語義自我適用：先跑範圍不相交判定再分類回報（本票即新制之首個自我引導案例）
- **[標配]** `git check-ignore -v plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md` 命中 → 停止回報（TRACKSCOPE-01 白名單 `!file/` 應保其 tracked，命中即該票落地異常，連帶升裁）

## Acceptance Criteria
- [x] **[存在性]** `**版本**: v3.8` 唯一命中；`BASE_COMMIT=`／`TARGET_BLOB=`／`ALREADY_EXECUTED_PENDING_CLOSURE`／`範圍不相交自動通過`／`given/expect` 各 ≥1 命中；`semantic_contract:`（YAML 鍵形式）命中＝0——實測：v3.8=1／BASE_COMMIT==1／TARGET_BLOB==1／ALREADY_EXECUTED_PENDING_CLOSURE=2／範圍不相交自動通過=2／given/expect=2／semantic_contract:=0
- [x] **[存在性]** v3.1–v3.7 全部既有 Revision 條目原樣在盤（HC-1 之正向取證：`Revision**: v3.` 命中數 ≥8）——實測 8（v3.1~v3.6 六條 + 檔頭 v3.7 一條 + 新增 v3.8 一條）
- [x] **[行為性]** 以本票自身為測例：依新三分流對本票 AI-0 錨點重放一次，輸出分類——ANCHOR 全符→直進（`git diff --name-only 418a552..HEAD -- <ALLOWED>` 於派工基線下為空），三分流本輪未觸發，重放輸出見 sv_report.md
- [x] **[可達性]** N/A 顯式劃記：TPL 為開票規範文本，無 runtime Guard/gate，型別不適用
- [x] ~~`assembleDebug` PASS~~（N/A：docs preset）
- [x] `git diff --stat` 僅涉 ALLOWED 單檔

**End of INFRA-TPL-V38-01**
