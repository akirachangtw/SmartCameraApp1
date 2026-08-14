# INFRA-CLOSURE-REGISTRY-GATE-01: Ticket Closure 紀律增列「冊面轉態四要件」（registry 收尾入規）

**簽發日期**: 2026-08-01
**簽發者**: Claude（SMTR/CSO）
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（純 .md 治理票，Codex PASS 即閉環）
**前置依賴**: 無（⑦ INFRA-QA-STEP0-GA-EXEMPT-01、⑨ INFRA-TPL-V32-01 均已 CLOSED）

> **檔案治理/來源說明**：本票為 `TKR02_Family_Ticketing_Audit_20260730.md` 桶 B 第 ⑧ 項「冊面轉態 gate（Ticket Closure SOP 機械化）」，Akira 核可批次優先序 ⑦→⑨→**⑧**→TKR-05→⑤⑥ 之第三張票。
>
> **缺口正向取證（2026-08-01，CSO live grep）**：現行 `Ticket Closure 紀律`（L53-60，INFRA-CLOSURE-SOP-01 於 2026-07-17 落地，commit `9032723`）三條 MUST 全屬**票檔層**（勾選／搬移 `deliver/`／修補票）；對 `.claude/rules/ssot-governance.md` 全檔 grep `registry|REGISTRY|lifecycle|checkpoint|冊面` **僅命中 L21**（Governance Artifact commit 前綴表），Closure 節內**零冊面層條文**。
>
> **事件支撐（活體案例）**：`INFRA-TPL-V32-01` 票檔層三步全數合規（AC 全勾、已搬 `deliver/`、無未閉環 AC 故免修補票），冊面四要件**全漏**（lifecycle 滯留 PENDING、reviews 四欄 PENDING、`file` 指標仍指已 NOT_FOUND 之 `Pending/`、無收尾 checkpoint），錯誤終態隨 `0fd8a51` 固化入版控，翌日方由 CSO 取證發現並以 `042f4f7` 補寫。**此為制度性盲區而非人為疏失**——執行者完全遵循現行三條 MUST 仍必然漏冊面。審查檔另載同型債：`file` 欄失步 ×4、「冊面收尾非機械化」為家族兩度遲延與污染 Traycer T2 放行依據之 split-brain 根因。

## Verify Config
TICKET_ID=INFRA-CLOSURE-REGISTRY-GATE-01
CREATED=2026-08-01
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md
FORBIDDEN=.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/settings.json,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md
MAX_LINES=30
DIFF_REF=042f4f7
ANCHOR_PROBED=2026-08-01 11:41

**MAX_LINES 推導註記**（額度制截斷序律，強制欄）：AI-0 = 0 行（唯讀復驗，不改檔）；AI-1 冊面轉態四要件條款 ≈ 7 行（1 條四要件主 bullet + 1 條原子性 MUST + 1 條矛盾停止 MUST + 前後空行與換行餘裕）；AI-2 強制層級聲明 ≈ 3 行 → 小計 10 行 → 緩衝倍率 1.5×（單檔連續區塊插入，無跨檔擴散風險，倍率取下限）= 15 → 進位對齊 `docs` preset 下界 **30**。**截斷序**：實作累計異動超過 30 行時 **MUST 停止回報**，禁以「僅差數行」自行放行，亦**禁省略 AI-2**（AI-2 為誠實定性條款，省略即令本票淪為 gate 偽裝關閉）。

## Action Items

> **AI-0 為強制首項**（派工前錨點預檢律，`Dispatch_Anchor_Precheck_Doctrine` KD2）。開票方已於 `ANCHOR_PROBED` 時刻以 `grep_files` 直查取樣，二錨點均恰 1 次命中。派工時若 `.claude/rules/ssot-governance.md` 已有新 commit 越過 `042f4f7`，MUST 重跑預檢方可派工。

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/rules/ssot-governance.md`
- **動作**: 逐字確認下列二字串於檔內命中數**恰 1 次**：
  1. `## Ticket Closure 紀律`（開票時實測 L53，唯一）
  2. `- 母票 AC 佐證不明（無法判定該勾或保留）→ **MUST** 停止勾選該項並列入修補票追蹤，禁止推定勾選。`（開票時實測 L58，唯一）
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成

### AI-1: 增列「冊面轉態紀律」四要件
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**（錨點，插入點為此行**之後**）: `- 母票 AC 佐證不明（無法判定該勾或保留）→ **MUST** 停止勾選該項並列入修補票追蹤，禁止推定勾選。`
- **目標值**: 於該行後緊接插入下列三條 bullet（原行保留不動）：

```
- **冊面轉態紀律**：票檔層三步（勾選／搬移／修補票）完成後，`TICKET_REGISTRY.json` 對應條目 **MUST** 同步完成**四要件**——①`lifecycle` 轉終態；②`reviews` 四欄（qa／sv／codex／traycer）各填實測值，無可考記錄者填 `N_A` 並於 checkpoint 揭露豁免依據，**MUST NOT** 以推定補 `PASS`（撤警正向取證律）；③`file` 欄指標由 `Pending/` 改指實際落點 `deliver/` 或 `archive/`；④`checkpoint.resolution` 寫入收尾實錄（含 commit hash、審查終值來源、豁免揭露）。
- **原子性**：四要件 **MUST** 於同一收尾回合內完成，**MUST NOT** 分批跨 session；任一要件缺漏即視為該票**未收尾**，不得回報結案。冊面異動走獨立 `[GOVERNANCE-SYNC]` commit，寫入後 **MUST** 以 scoped `git_diff` 驗證零意外變更（「新內容已存在」不足為據）。
- **矛盾停止**：票檔層與冊面層狀態不一致（如票檔已搬 `deliver/` 而冊面 `file` 仍指 `Pending/`）→ **MUST** 停止回報並列入待裁，禁單方推定何者為真。
```
- [x] 完成

### AI-2: 強制層級誠實聲明（禁 gate 偽裝關閉）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**（錨點）: AI-1 插入之末條 bullet（`- **矛盾停止**：` 起始行）
- **目標值**: 於其後緊接插入下列一條 bullet：

```
> **強制層級（誠實定性）**：本節「冊面轉態紀律」為**紀律層**條款，非機械閘。冊面轉態發生於 Desktop 治理 session，而 `verify_checklist.sh` / `structural_verify.sh` 執行於 Claude Code 端——**閘門位置與動作發生位置不重合**，腳本層對本失效型無攔截力（`INFRA-TPL-V32-01` 事故全程未經 Claude Code）。真正之機械強制須由 mcp-ticket-platform server 端提供（`lifecycle` 轉終態時強制 `checkpoint` 非空、`file` 指標存在性校驗），屬 TKR-05 平台原語域，依前置資源恆存律**不得由本票預支**；在該閘落地前，本節效力等同宣告式紀律，違反不會被工具攔截。
```
- [x] 完成

## Hard Constraints
1. 純 `.md` 治理票，零觸碰 `.kt` / `.sh` / `CLAUDE.md`（沿用 INFRA-CLOSURE-SOP-01 HC-1）。
2. **禁新增任何檔案**——`.gitignore` L74-77 反選集覆蓋 `plans/cso_briefing/project/*`（僅 `TICKET_REGISTRY.json` 有白名單），同目錄新檔將**靜默維持 untracked**（與 hook 置錯 repo 靜默 exit 0 同型失效）。本票檔自身亦為 untracked，屬既有現況，根治見 deferred ①。
3. **禁修改既有 L53-60 任何一行**——本票為純增列。原三條 MUST 與 FORBIDDEN 延後項條款均維持逐字不動。
4. 禁在本票 commit 內夾帶 `TICKET_REGISTRY.json` 異動（FORBIDDEN 已列）；本票之冊面轉態屬收尾階段動作，走獨立 `[GOVERNANCE-SYNC]` commit（混型 commit 債見 deferred ②）。
5. commit 前綴 **MUST** 為 `[INFRA-CLOSURE-REGISTRY-GATE-01]`（本票動 app-repo 治理文檔，非 Governance Artifact 檔類，禁用 `GOVERNANCE-SYNC` / `COMPACT-SYNC`）。

## Error Handling
- AI-0 任一錨點命中數 ≠ 1 → 停止回報，禁推定等價位置。
- 累計異動行數 > `MAX_LINES=30` → 停止回報（截斷序），禁省略 AI-2 換取行數。
- 插入後 `Ticket Closure 紀律` 節內出現條款語義重疊或與既有三條 MUST 矛盾 → 停止回報，禁自行裁量合併。
- `.claude/rules/` 目錄在派工時已有他票並行異動 → 停止回報，禁盲目套用。

## Acceptance Criteria
- [x] **[存在性]** `grep -c '冊面轉態紀律\|原子性\|矛盾停止'` 於 `.claude/rules/ssot-governance.md` 命中 **3** 條 bullet 起始行，且四要件關鍵詞 `lifecycle`／`reviews`／`file`／`checkpoint` 各至少 1 次
- [x] **[存在性]** 強制層級聲明落地：`grep -n '強制層級（誠實定性）'` 恰 1 次命中，且同段含 `TKR-05` 與 `非機械閘` 二詞（AI-2 未被省略之正向取證）
- [x] **[存在性]** 既有條文零位移驗證：`git diff` 於本檔僅見 `+` 行、**零 `-` 行**（HC-3 純增列）
- [x] **[行為性] dogfood**：本票自身收尾即為新條款首次適用案例——收尾時依四要件完成冊面轉態，並以 `registry_list` 回讀確認本票條目四要件齊備（`lifecycle=CLOSED`、`reviews` 四欄有值、`file` 指 `deliver/`、`checkpoint.resolution` 非空）。**回讀證據 MUST 附於本票完成回報**，禁以「已執行」宣告代替回讀
  - 回讀證據（2026-08-01 `registry_list topic=INFRA lifecycle=CLOSED`）：`lifecycle=CLOSED`；`reviews={qa:PASS,sv:PASS,codex:PASS,traycer:N_A}`；`file=deliver/INFRA-CLOSURE-REGISTRY-GATE-01_Registry_Transition_Four_Requirements.md`；`checkpoint.resolution` 非空（含 commit d3fd12a、SV/Codex 終值、Traycer N/A 依據）
- [x] ~~`assembleDebug` PASS~~ **(N/A: docs preset — TICKET_TPL v3.2 固定 AC 適用性條款)**
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

> **AC 類型學適用性揭露**（禁靜默省略）：本票無 `[可達性]` AC——該型要求 Guard/gate 依賴之 input 於 `app/src/main/` 有 production producer，而本票條款之消費者為 Desktop 治理 session 之人／agent 遵循層，**結構上不存在 production producer**。此非規避，係型別不適用；`[行為性]` AC 以 dogfood 回讀承載落地語義，故本票不構成「以 `[存在性]` AC 偽裝關閉 gate 票」（SMTR_INDEX 開票格式強制規則）。

## 待後續承接（deferred，本票明列不做）
1. **`.gitignore` 同目錄盲區根治**：`!plans/cso_briefing/project/file/` 白名單（Akira 2026-07-31 裁決 2-c，另開獨立 INFRA 票）。在此之前本目錄新增檔案一律靜默 untracked。
2. **混型 commit 治理**：⑥ `INFRA-QA-COMMIT-MIXEDTYPE-01`（審查檔桶 B，事件 ×2：FIX-D／FIX-F）。
3. **平台端機械閘**：`lifecycle` 轉終態強制 `checkpoint` 非空／`file` 指標存在性校驗，歸 TKR-05 桶 A（依前置資源恆存律不得由本票預支）。
4. **`.gitignore` 行號錯記勘誤**：SMTR 常駐記載「`:71` 排除整個 `plans/cso_briefing/`」，實測為 **L74-77 四行反選集**且 `TICKET_REGISTRY.json` 有白名單——本項承接自 INFRA-TPL-V32-01 deferred ②，**仍無承接載體**，MUST 於 ⑩ KAIROS 收割輪或 SMTR_INDEX 修訂時清償（基礎設施異動即檔律）。

**End of INFRA-CLOSURE-REGISTRY-GATE-01**
