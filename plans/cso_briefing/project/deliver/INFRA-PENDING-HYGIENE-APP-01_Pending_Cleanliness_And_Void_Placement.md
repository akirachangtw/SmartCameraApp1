# INFRA-PENDING-HYGIENE-APP-01: Pending/ 淨空原則與 VOID 歸位紀律立法（Rev B：終態補集定義）

**簽發日期**: 2026-09-02 ｜ **Rev B**: 2026-09-05（SMTR 依 Akira 丁案裁決重寫）
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（純治理文本立法，無功能實作面）

## Rev B 修訂摘要（Akira 2026-09-05 丁案裁決）

執行端於 AI-3 提報 SSOT 衝突並依 Error Handling 停工升裁——**該提報正確，票面 Rev A 之 AI-3／AI-4 前提確已被現場證偽**。SMTR 終審取證後裁決丁案（執行端所列三案皆不採）。

**Rev A 被證偽之處（一手取證，非採信回報）**：

| Rev A 主張 | 現場實況 | 證據 |
|---|---|---|
| APP 活態未交付＝`PENDING` 單值 | `IN_PROGRESS` 三筆在用（`ADV-CR-R0-PRECLEAR-01`／`INFRA-QA-ACTIVATION-01`／本票自身） | `aud_20260905_6cac382cdc`（冊面全量 28 筆） |
| `BLOCKED` 為「若日後使用」之前瞻假設 | `ADV-AGENTS-CR-03` 現為 `BLOCKED` | 同上 |
| L81 為活態枚舉之法源 | **`BLOCKED` 不在 L81 之任何一側**——L81 列「維持 PENDING/REVIEW/CLOSED/VOID」與「不引入 TICKET_RIGOR/IN_PROGRESS/EXECUTED」，`BLOCKED` 兩側皆未提及，證其枚舉本即不完整 | `aud_20260905_6703eb116f`（L81 逐字） |
| （Rev A 未察）`TICKET_RIGOR` 於 APP 未引入 | 已寫入 skill 契約：`ticket-run/SKILL.md` L34 納入派工清單理由、L62「選中 `TICKET_RIGOR` 票時步驟 7 轉為 `IN_PROGRESS`」 | `aud_20260905_939331a26c` |

**丁案之核心（本次修訂之方法論）**：`Pending/` 淨空原則**不需要枚舉活態集合**，只需要**終態**定義。以補集取代枚舉——「未達終態者留在 `Pending/`」——即可達成同一治理目的，且：①對枚舉變動免疫（平台新增態、skill 採用新態皆不必改條文）；②不牴觸 L81 任何字面，**HC-1 禁改 L81 之約束無須解除**；③不必把 ticket-run skill 之正常運作定性為「過渡期違規」；④不寫入應然而從未然之空條文。

**Rev B 八處修訂**：①本摘要新增；②檔案治理節之「枚舉收斂為單值」段整段重寫；③AI-3 標題與三條內容改終態補集；④AI-3 原第 3 條（`BLOCKED` 前瞻條）**刪除**；⑤AI-4 增 `file` 欄正規化前置；⑥AI-4 之 AC 由「零落差」改「產出落差清單」；⑦HC-4 由「MUST 用 `PENDING` 單值」改「禁寫入任何形式之 lifecycle 完整枚舉」；⑧AC 兩處對應重寫。

**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: Akira 2026-08-19 L2 治理裁定字面「確定廢除或凍結的票不要用 PENDING 狀態；工作完全做完後，看到的 PENDING 應該是空的」；Akira 2026-09-02 明示 APP void 落點為 `plans/cso_briefing/project/void/`；Akira 2026-09-05 丁案裁決；PLT `INFRA-PENDING-HYGIENE-01`（CLOSED）為立法藍本**但其枚舉措辭不適用於 APP**
- `coordination_dependency`: **`INFRA-LIFECYCLE-SSOT-01`（APP 冊 seq 35，PENDING）——ALLOWED 交集硬約束**：同改 `.claude/rules/ssot-governance.md`。該票之 `depends_on` 已鎖本票，其 AI-0 動作 A 要求本票達 `REVIEW`／`CLOSED` 方得開工。**本票 MUST 先行**；本票 Rev B 之條文 **MUST NOT** 含 lifecycle 枚舉宣告，否則將於該票 AI-3 之互斥檢核中被判為殘留並觸發升裁

## 檔案治理／來源說明

**SSOT**：`.claude/rules/ssot-governance.md`（APP 端生命週期與 Closure 紀律唯一 SSOT）。

**現況缺口（2026-09-02 live grep，`aud_20260902_422aff6307`／`38830eefce`）**：APP 檔內 `docs/void`／`VOID 歸位`／`一一對應` 字面**全部零命中**；既有 `## Ticket Closure 紀律` 已具搬移紀律、凍結點語義、冊面轉態四要件、矛盾停止，完整度**高於** PLT 同節，唯獨缺 VOID 側之對稱規格與 `Pending/` 淨空原則。

**目錄實況**：`plans/cso_briefing/project/void/` 已實存，內有 `INFRA-CLAUDE-MERGE-01_Claude_Settings_Merge_Rapid_Trial.md`（6,654B）——已有 VOID 票依慣例歸位但**無條文依據**，屬「實作先於立法」。本票補其法源。

**⚠️ 立法措辭紀律（Rev B 核心，取代 Rev A 之「收斂為單值」）**：PLT 藍本以四值枚舉表述「活態未交付」，且警告「禁寫成僅 PENDING」。**該措辭於 APP 端不移植亦不反轉，而是改用終態補集**：

> 票檔應離開 `Pending/` 之條件為 lifecycle 達**終態** ∈ {`CLOSED`, `VOID`}；未達終態者留在 `Pending/`，**不論其為何值**。

`CLOSED`／`VOID` 二態語義穩定、為既有 Closure 紀律所需且 L81 明列，具名引用無爭議；活態側則一律不列舉。本票**不裁決** APP lifecycle 是否應收斂回四態——該方向裁決屬 L2，且 L81 射程過寬之修正已另立 `INFRA-LIFECYCLE-SSOT-01` 承接。

## Verify Config
TICKET_ID=INFRA-PENDING-HYGIENE-APP-01
CREATED=2026-09-02
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md
FORBIDDEN=plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,.claude/settings.json,.claude/hooks/**,.claude/skills/**,.claude/agents/**,.claude/CLAUDE.md,app/**,D:/DevSources/workflow/mcp-ticket-platform/**
MAX_LINES=N/A（純文本立法，行數不具驗收語義；TPL v3.5 R3）
DIFF_REF=<開工時 `git rev-parse --short HEAD` 回填>
BASE_COMMIT=<同上>
TARGET_BLOB=N/A（docs preset 單檔，V1 降級為 AI-0 錨點制）
ANCHOR_PROBED=2026-09-05（Rev B 重寫同日；派工時 MUST 重跑 AI-0 並更新本欄時戳）

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/rules/ssot-governance.md`
- **動作 A（結構錨點，以字面非行號）**：三處逐字確認唯一存在——①`## Governance Artifact 清單（單一真相來源）`；②`## Ticket Closure 紀律`；③該節內以 `- **搬移紀律**：` 起始之條目
- **動作 B（L81 現況記錄，非法源）**：`## Ticket Rigor Gate（選用制，INFRA-GOVSYNC-01）` 節首之四態制裁剪句在盤，記錄其現值存收尾。**Rev B 起該句不再是本票任何條文之法源**（丁案改用終態補集），故其字面若已被他票改寫，**不停止**，僅載明差異續行——與 Rev A 之「不存在即升裁」相反
- **動作 C（冊面現況快照，AI-4 之基線）**：`registry_list(project="SmartCameraApp1", fields=["id","lifecycle","file"])` 全量取回並存收尾，作為 AI-4 落差清單之比對基線。**MUST 記錄總筆數**
- **動作 D（目錄現況快照）**：`plans/cso_briefing/project/Pending/`、`deliver/`、`void/` 三目錄檔案清單全量存收尾
- 動作 A 任一不符 → 依 TPL v3.8 基線漂移三分流分類回報
- **[標配]** `git check-ignore -v .claude/rules/ssot-governance.md` 命中 → 停止回報
- [x] 完成

### AI-1: `void/` 檔類定義（Rev B 未變更）
- **檔案**: 同上，`## Governance Artifact 清單（單一真相來源）` 節內
- **動作**: 補一列／一句定義 `plans/cso_briefing/project/void/` ＝ 已作廢（`lifecycle=VOID`）票檔之歸位目錄，與 `Pending/`（未達終態）、`deliver/`（已交付）並列**三分**
- **路徑字面 MUST 為 APP 實際落點** `plans/cso_briefing/project/void/`，**MUST NOT** 寫成 PLT 之 `docs/void/`
- [x] 完成

### AI-2: VOID 歸位紀律（Rev B 未變更）
- **檔案**: 同上，`## Ticket Closure 紀律` 節內，緊接冊面轉態紀律條目之後
- **動作**: 新增條目「**VOID 歸位紀律**」，MUST 明載四要件：①票檔移 `plans/cso_briefing/project/void/`；②冊面 `file` 欄同步改指 void 路徑；③以 `registry_set_checkpoint` 載明作廢事由（**APP 為隔離專案，`registry_append_note` 恆回 `REGISTRY_CROSS_PROJECT_REF`，敘事唯一合法通道為 checkpoint**）；④**復活紀律**——`VOID` 為終態，議題日後重啟 MUST 開新票，不得改回原票 lifecycle；原解凍／續辦條件自動降級為新票之開票前置檢核。並載明既有產物留存不刪，供新票直取為 `evidence_dependency`
- **平台限制之如實載明**: MUST 附註「`registry_upsert_ticket` 對已歸檔票回 `REGISTRY_ARCHIVED_TICKET`，且 `registry_unarchive` 平台**未實作**（PLT `INFRA-REGISTRY-API-GAPS-01` 明示範圍收斂不實作）——故『開新票』非風格建議而是**平台強制**」
- [x] 完成

### AI-3: `Pending/` 淨空原則（Rev B 重寫：終態補集定義）
- **檔案**: 同上，`## Ticket Closure 紀律` 節末或 `## Ticket Rigor Gate` 節前之適當位置
- **動作**: 新增「**`Pending/` 淨空原則**」段，MUST 明載三條：
  1. **終態補集定義**：`plans/cso_briefing/project/Pending/` 僅承載**未達終態**之票檔；終態 ∈ {`CLOSED`, `VOID`}。票檔離開 `Pending/` 之條件為該票達終態，**與其當下為何非終態值無關**。**MUST 附理由字面**：「lifecycle 合法值之 SSOT 在平台，本檔不列舉活態值，以免成為無同步機制之副本」
  2. 經判定無復活路徑者 MUST 轉 `VOID` 並依 VOID 歸位紀律離開 `Pending/`；**MUST NOT** 以 `blocked_by` 承載凍結語義而長期滯留未達終態
  3. `Pending/` 內**非票檔**之文件（編纂檔、executor briefing、分析稿等）不受本原則之票檔紀律拘束，但 MUST 於 AI-4 之落差清單中單獨分類，**MUST NOT** 混入「孤兒票」計數
- **紀律**：
  - **MUST NOT** 於本段寫入活態值之任何列舉（含 `PENDING`／`IN_PROGRESS`／`BLOCKED` 之並列、括號註記或對照表）
  - `CLOSED`／`VOID` 之具名引用**限於終態定義用途**，MUST NOT 擴寫為完整枚舉
  - **MUST NOT** 書寫任何「APP 不使用 X 狀態」之斷言（Rev A 之 `BLOCKED` 前瞻條已刪除，理由：`ADV-AGENTS-CR-03` 現為 `BLOCKED`，該斷言已被證偽）
- [x] 完成

### AI-4: 目錄↔冊面一一對應條文（Rev B：增正規化前置與落差清單）
- **檔案**: 同上，AI-3 同段末
- **動作 A（條文）**: 補一句：`Pending/` 之票檔集合與冊面**未達終態**票集合 MUST 一一對應；發現落差即為治理 finding，MUST 於發現當批修正或明載於冊面
- **動作 B（`file` 欄正規化前置，Rev B 新增，MUST 先於任何比對）**: APP 冊 `file` 欄現存**兩種格式**——短路徑（如 `Pending/INFRA-SV-GOV-01_SV_Governance_Fix.md`）與長路徑（如 `plans/cso_briefing/project/Pending/INFRA-PENDING-HYGIENE-APP-01_...md`，本票自身即為長路徑實例）。比對前 **MUST** 先將兩者正規化至同一形式（建議取 basename 或統一去除 `plans/cso_briefing/project/` 前綴），**否則全部舊票將被誤判為落差**。條文 MUST 載明此正規化要求，使日後執行者不重蹈
- **動作 C（落差清單產出，取代 Rev A 之零落差要求）**: 依 AI-0 動作 C／D 之快照，產出落差清單並分四類：①`Pending/` 有檔而冊面查無（孤兒票檔）；②冊面未達終態而 `Pending/` 無檔；③冊面 `file` 欄指標與實際落點不符；④`Pending/` 內非票檔文件（依 AI-3 第 3 條單獨分類）。**本票僅產出清單，MUST NOT 代為處置任何一筆**——處置屬後續裁決
- **事件支撐（MUST 於條文或註記載明）**: PLT 端 2026-08-30 實測發現四張票檔在 `docs/pending/` 而冊面查無，其中兩張為已 VOID 歸檔票之滯留副本；APP 端 2026-09-05 已知至少存在 `MEO-FIX-A`／`MEO-FIX-B` 系列孤兒檔與 `void/INFRA-CLAUDE-MERGE-01`（票檔在盤而主冊與歸檔冊皆查無）
- [x] 完成

### AI-5: 修訂波及面全檔掃描
- **動作**: 以 grep 全檔清點 `VOID`／`Pending`／`void/`／`pending` 之**所有**實例（含表格與括號註記），確認無與 AI-1~AI-4 新條文矛盾之殘留敘述
- **Rev B 增列**：全檔清點 lifecycle 值之出現，確認新增條文中**活態值之列舉命中＝0**（`CLOSED`／`VOID` 之終態引用除外）。此為與 `INFRA-LIFECYCLE-SSOT-01`（seq 35）之互斥前置——該票 AI-3 將檢核本檔「枚舉宣告命中＝0」，本票若留下枚舉即觸發其升裁
- 有矛盾者同批改正，無則回報「全檔掃描零矛盾、零活態枚舉」
- [x] 完成（全檔掃描零矛盾、新增條文零活態枚舉；見收尾實錄逐位歸類）

## Hard Constraints
1. **禁改 L81 四態制裁剪句之任何字面**（其射程修正屬 `INFRA-LIFECYCLE-SSOT-01` 射程）
2. 禁改 `## Ticket Closure 紀律` 既有條目之語義；本票僅**新增**條文
3. 禁動 `## 治理載體↔存取工具對應圖譜` 節（`INFRA-TOOLDESC-APP-01` 交付成果）
4. **（Rev B 改寫）立法用語 MUST NOT 含任何形式之 lifecycle 完整枚舉**（含清單、表格、括號並列）；活態一律以「未達終態」表述；`CLOSED`／`VOID` 僅得作終態具名引用。**Rev A 之「MUST 用 `PENDING` 單值」已作廢，MUST NOT 施作**
5. 禁以本票夾帶 registry schema 變更或任何 JSON 異動
6. **禁處置 AI-4 落差清單之任何一筆**（含刪檔、搬檔、補登冊面）——本票只立法與盤點
7. 完成後 STOP，等待 SMTR 複審，禁自行擴 scope

## Error Handling
- AI-0 動作 A 任一錨點不符 → 依三分流分類回報
- 新條文與既有條文語義衝突且無法在不改既有語義前提下消解 → 停止回報升裁，禁自行取捨
- AI-4 落差清單產出過程中發現需寫入冊面方能完成 → 停止回報（違 HC-6，盤點不含處置）
- **[標配]** `git check-ignore -v .claude/rules/ssot-governance.md` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** 檔內出現 `plans/cso_briefing/project/void/` 檔類定義，且與 `Pending/`／`deliver/` 並列；`docs/void/`（PLT 路徑）字面命中＝0
- [x] **[存在性]** `## Ticket Closure 紀律` 節內存在「VOID 歸位紀律」條目，四要件字面俱全，含 `registry_set_checkpoint` 為敘事唯一通道之 APP 專屬約束，並含 `registry_unarchive` 未實作之平台限制註記
- [x] **[行為性]（Rev B 核心守門）** `Pending/` 淨空原則段以**終態補集**表述；新增條文活態值列舉命中＝0——收尾實錄逐一列出 lifecycle 值於本檔之所有出現位置並歸類（終態引用／實況敘述／列舉／既有 L81 遺留列舉）
- [x] **[存在性]** 全檔無「APP 不使用 `BLOCKED`」或等義斷言（Rev A 該條已刪除，未寫入本檔）
- [x] **[存在性]** 目錄↔冊面一一對應條文存在，且含 `file` 欄正規化要求之字面
- [x] **[行為性]** AI-4 落差清單已產出，四類分列且逐筆可指認（檔名或票號）；清單中**零筆被處置**（見收尾實錄）
- [x] **[可達性]** 落差清單之基線可指認：AI-0 動作 C 冊面快照 29 筆、動作 D 三目錄檔案數 Pending/=28／deliver/(遞迴)=56／void/=1，均載於收尾
- [x] **[行為性]** AI-5 全檔掃描完成，回報「零矛盾、零活態枚舉」
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset，純治理文本，無編譯產物)
- [x] `git diff --stat` 僅涉及 `.claude/rules/ssot-governance.md`

---

**End of INFRA-PENDING-HYGIENE-APP-01 (Rev B)**
