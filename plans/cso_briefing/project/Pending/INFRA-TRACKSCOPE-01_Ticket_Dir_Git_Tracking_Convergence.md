# INFRA-TRACKSCOPE-01: 票務目錄 Git 追蹤集收斂（TKR-20/21 APP 移植，執行期產物退役）

**簽發日期**: 2026-08-22 ｜ **簽發者**: Claude（SMTR）｜ **來源議題**: INFRA
**Mode**: /uc ｜ **Effort**: Low ｜ **Traycer Workflow**: N/A（gitignore＋git index 操作，無程式邏輯改動；操作可逆——`git add -f` 即復追蹤。**註**：APP 端 RAPID_MODE 經 Akira 2026-08-22 裁決**不引入**，本票依 `.claude/CLAUDE.md` 全程序執行 QA／SV／Cross-Review，本欄 N/A 係依既有治理票免 Traycer 先例，非援引 §0 豁免）
**移植來源**: `mcp-ticket-platform/docs/deliver/TKR-20_Registry_Untrack_Execution_D8_Batch.md`（＋Addendum-1）、`TKR-21_Git_Tracking_Scope_Convergence.md`
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: TKR-20（registry JSON 退役先例＋untrack 驗證手法：因果鏈前後對照探針）、TKR-21（`docs/*` 白名單制先例＋四項 `git check-ignore` 逐項豁免驗證）；本票為 TKR-21 AI-3 收尾所載「L100 `plans/cso_briefing/project/Pending→deliver` stale 路徑異常，留待另案」之另案
- `coordination_dependency`: `INFRA-CROSSREVIEW-GOVPATH-EXCL-01`（在途，票檔位於本票退役側 `Pending/`；AI-0 有序處置）；`INFRA-ENVSYNC-BATCH-01`（其 ALLOWED 含 `plans/cso_briefing/project/file/ENVSYNC_AUDIT_20260821.md` 與 `.claude/rules/ssot-governance.md`——本票 ALLOWED 刻意與之**零相交**，可並行）

## 檔案治理／來源說明

**Akira 2026-08-22 裁決字面**：「僅 Git 追蹤 `plans/cso_briefing/project/{deliver, void}`」。

**目標態（Akira 2026-08-22 確認：`deliver/`＋`void/`＋`file/` tracked）**：`plans/cso_briefing/project/` 內僅 `deliver/`、`void/`、`file/` 三者 tracked，其餘（`Pending/`、`Handoff/`、`TICKET_REGISTRY.json`、`Postponed.md`）全數退役。`plans/cso_briefing/` 之 `archive/`／`reports/`／`role/`／`CSO_INDEX.md`／`CSO_Session_Startup_SOP.md` 及 `.claude/` 樹、`app/**` 均不在射程。

**✅ `file/` 保留已獲 Akira 2026-08-22 明示確認（原為 SMTR 保守保留之待裁項，現為既定目標態）**。保留理由留存如下，供日後翻盤時對照：
1. `Unified_Ticket_Template_v3.md` —— TPL 自 v3.7 起為**單載體版控 SSOT**（Smarter-Project 衍生副本已刪除且禁重建）；退役即消滅其版本歷史，TPL 修訂將無 diff 可審。
2. `SSOT_v4_0_6.md`、`Portrait_MVP_Kotlin_Contract_v0_*.md`、`P08_Codebase_Sanitization_Contract.md` 等契約／架構文本 —— 屬 versioned artifacts（比照 TKR-21「契約檔天然落在 tracked 側」之處置：PLT 契約檔住 `docs/deliver/` 故無需搬遷；APP 契約檔住 `file/`，同語義應保留 tracked）。
3. `ENVSYNC_AUDIT_20260821.md` —— `INFRA-ENVSYNC-BATCH-01` AI-2 之寫入標的且列於其 ALLOWED；退役後該票 `git diff --stat` AC 將**空洞滿足**，且其標配 `git check-ignore` 檢查將命中而觸發停止回報（gitignore 死結）。
**翻盤條件**：日後若裁定 `file/` 退役，需先行處置 TPL SSOT 之替代版控載體並修訂 BATCH-01，屬另票範圍，本票 MUST NOT 夾帶。

**凍結點語義（承 TKR-21）**：票檔於 REVIEW 段搬 `deliver/` 即凍結點——`Pending/` 期 untracked，搬移瞬間成為 tracked 新檔，首次 commit 即終審凍結版。`void/` 同理承接作廢票。

**registry 退役依據（承 TKR-20 D6）**：`TICKET_REGISTRY.json` 屬 live operational state，由 MCP 服務層讀寫（`registry_*` 原語），非 versioned artifact；退役後檔案留磁碟、服務照常讀寫，既有 git 歷史保留可考。**⚠️ 與 PLT 之差異**：PLT 端 TKR-19 已落地 revision 樂觀併發，untrack 後尚有 revision 單調序作防線；APP 冊為 isolated project，其 revision 機制是否等效**未經取證**——故本票 AI-0 增列備份實存驗證（前置資源恆存律，承 TKR-20 AI-0 手法），驗證不過即停止升裁。

## Verify Config
TICKET_ID=INFRA-TRACKSCOPE-01
CREATED=2026-08-22
PRESET=docs
ALLOWED=.gitignore,plans/cso_briefing/CSO_INDEX.md
FORBIDDEN=.claude/rules/**,.claude/skills/**,.claude/settings.json,.claude/CLAUDE.md,plans/cso_briefing/project/deliver/**,plans/cso_briefing/project/void/**,plans/cso_briefing/project/file/**,app/**
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-08-22（SMTR live 取證：`project/` 七項清單＝deliver／file／Handoff／Pending／void／Postponed.md／TICKET_REGISTRY.json；`git status` 實測 `TICKET_REGISTRY.json` 與 `Pending/INFRA-CROSSREVIEW-*.md` 均為 tracked-modified，證追蹤現況；派工時執行端逐一復驗）

⚠️ **FORBIDDEN 之 `deliver/**`／`void/**`／`file/**` 係防誤動其**內容**；本票對其**零操作**（保留側）。`git rm --cached` 標的僅 `Pending/`／`Handoff/`／`TICKET_REGISTRY.json`／`Postponed.md`，且 index 操作不改檔案內容——票面顯式劃記此界線（承 TKR-20／TKR-21 先例）。

## Action Items

### AI-0: 前置復驗與在途處置（派工後、修改前）
- **枚舉基線**：`git ls-files plans/cso_briefing/project/` 全量輸出，逐目錄分類記錄（AI-2 作用面基線，MUST 存收尾）
- **在途票有序處置**：`git status --short plans/cso_briefing/project/Pending/` 若有 tracked-modified 項（已知：`INFRA-CROSSREVIEW-GOVPATH-EXCL-01` Rev A），**MUST 先以獨立 commit 落版**再進 AI-1；禁令其中間修訂史因 untrack 而永久遺失
- **備份實存驗證（前置資源恆存律）**：確認 APP `TICKET_REGISTRY.json` 之備份機制實際存在且近期有產出（備份檔 mtime ≤ 24h）；**不存在或停擺 → 停止回報升裁**（untrack 後備份為僅存防線）
- **`.gitignore` 現況**：`plans/cso_briefing` 字面**零命中**（已有＝他票先行 → 停止回報）
- 任一不符 → 停止回報
- [ ] 完成

### AI-1: gitignore 白名單新增
- **檔案**: `.gitignore`
- 追加下列區塊（**禁**與既有規則重複列）：
```
# plans/cso_briefing/project/ 追蹤集：僅凍結交付與版控文本入版控
# （Akira 2026-08-22 裁決；移植 TKR-20/TKR-21。file/ 保守保留，見票面說明）
plans/cso_briefing/project/*
!plans/cso_briefing/project/deliver/
!plans/cso_briefing/project/void/
!plans/cso_briefing/project/file/
```
- **豁免驗證（白名單易錯，MUST 逐項且結果存收尾）**：`git check-ignore -v` 對下列六標的——
  - **零命中**（保留側）：`plans/cso_briefing/project/deliver/`（任一存在檔）、`plans/cso_briefing/project/void/`（任一存在檔）、`plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`、`plans/cso_briefing/project/file/ENVSYNC_AUDIT_20260821.md`
  - **命中**（退役側）：`plans/cso_briefing/project/TICKET_REGISTRY.json`、`plans/cso_briefing/project/Pending/`（任一存在檔）
- 任一項與預期相反 → `git restore .gitignore` 後停止回報（禁帶病續行）
- [ ] 完成

### AI-2: 既有追蹤檔退役
- 依 AI-0 枚舉清單，對 `Pending/`／`Handoff/`／`TICKET_REGISTRY.json`／`Postponed.md` 之**所有現追蹤檔**執行 `git rm --cached`（工作樹保留）
- **禁**觸及 `deliver/`／`void/`／`file/` 任一項
- 驗證：`git ls-files plans/cso_briefing/project/` 僅餘 `deliver/`／`void/`／`file/` 三類；提交後 `git status` 潔淨
- 與 AI-1 可同筆 commit（同一目的），與 AI-3 分筆
- [ ] 完成

### AI-3: 治理文本語境同批（範圍嚴格限縮）
- **檔案**: `plans/cso_briefing/CSO_INDEX.md`
- 全檔掃描票檔／冊面版控相關字面，與新追蹤集矛盾者逐一改正，並補一句凍結點語義（REVIEW 段搬 `deliver/` 即凍結點）；**掃描結果（矛盾實例清單或「零矛盾」）MUST 列於收尾**
- **MUST NOT** 觸及 `.claude/rules/ssot-governance.md`——該檔為 `INFRA-ENVSYNC-BATCH-01` 之 ALLOWED 標的，跨票同檔編輯即製造衝突；其 L100 stale 路徑之修正**留待 BATCH-01 收束後另案**（本票收尾 MUST 明文轉交此待辦）
- 獨立 commit
- [ ] 完成

## Hard Constraints
1. `deliver/`／`void/`／`file/` 之內容與追蹤狀態零變動（保留側）。
2. **禁任何檔案搬遷**（承 TKR-21 HC-2）。
3. 禁 `git add -A`／`git add .`（白名單生效前誤加回退役檔之風險）。
4. 禁刪除任何實體檔案——本票純 index 與 gitignore 操作。
5. `.claude/` 樹零觸碰（與 BATCH-01 之衝突防護）。
6. **MUST NOT** 退役 `file/`——保留為 Akira 2026-08-22 明示確認之目標態；變更須另票並先處置 TPL SSOT 替代版控載體。

## Error Handling
- AI-0 備份機制不存在／停擺 → 停止回報升裁（前置不成立）
- AI-0 在途票 tracked-modified 未落版即進 AI-1 → 視為違令，停止回報
- AI-1 豁免驗證任一項與預期相反 → `git restore .gitignore` 後停止回報
- AI-2 誤觸保留側 → `git restore --staged` 後停止回報
- AI-3 掃描遇歧義字面 → 停止列清單升裁
- **[標配]** `git check-ignore -v .gitignore plans/cso_briefing/CSO_INDEX.md` 命中 → 停止回報（gitignore 死結律；註：本票 AI-1 後**退役側**命中屬預期目標態，該律適用對象為 ALLOWED 兩檔與保留側）

## Acceptance Criteria
- [ ] **[存在性]** `.gitignore` 含五行白名單區塊（含二行註解）；`plans/cso_briefing/project/*` 字面唯一命中
- [ ] **[行為性]** AI-1 六項 `git check-ignore -v` 結果全符預期（保留側四項零命中／退役側二項命中），原始輸出存收尾
- [ ] **[行為性]** `git ls-files plans/cso_briefing/project/` 僅餘 `deliver/`／`void/`／`file/`；AI-0 枚舉基線與退役後清單對照存收尾
- [ ] **[行為性]** 因果鏈前後對照（承 TKR-20 手法）：AI-2 前對 `TICKET_REGISTRY.json` 執行一次服務層寫入探針（如 `registry_append_note` 測試字串後撤回）→ `git status` 顯示 `M`；AI-2 後同操作 → `git status` 零顯示。**兩次探針原始輸出均存收尾**（退役生效之端到端證明，禁以單次事後觀察替代）
- [ ] **[行為性]** 保留側反向證明：對 `file/Unified_Ticket_Template_v3.md` 附加一空白行後 `git status` **仍顯示 `M`**（證明白名單未誤傷 TPL SSOT 版控），驗畢 `git restore` 還原
- [ ] **[存在性]** CSO_INDEX.md 掃描結果（矛盾清單或「零矛盾」）列於收尾；ssot-governance L100 待辦之轉交字面在盤
- [ ] **[可達性]** N/A 顯式劃記：本票標的為 git index 與 gitignore，無 Guard／gate／runtime state，型別不適用（非規避）
- [ ] ~~`assembleDebug` PASS~~（N/A：docs preset，零 `.kt`／`.xml` 變更）
- [ ] `git diff --stat` 僅涉 ALLOWED 二檔（＋退役檔之 index deletion 記錄屬預期）

## 完成後動作
- **MUST NOT** 自行搬移票檔（Pending→deliver 由 SMTR／CSO 收束時執行）
- **MUST NOT** 自行寫入 `TICKET_REGISTRY.json`（單一寫入者為 Desktop 治理 session）
- ~~收尾 MUST 提請 Akira 確認 `file/` 保守保留之處置~~ **（2026-08-22 已解除：Akira 明示確認保留，無待裁項）**

**End of INFRA-TRACKSCOPE-01**
