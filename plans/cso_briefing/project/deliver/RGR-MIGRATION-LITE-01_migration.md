# RGR-MIGRATION-LITE-01: 為 alias／normalization／guard 類票加入條件式完整性審查、gate 內家族閉合，以及 AC 規範修訂後之兄弟 AC 重驗義務

**簽發日期**: 2026-10-01
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**Revision**:
- Rev A（2026-10-01，SPEC_DEFECT，Akira 裁定 D1 甲／D2 乙／D3 確認）：吸收首輪 gate PARTIAL 之 F1–F10 與後輪修正 #1–#6。要點：F1 採選項 A（缺口必記 finding，裁決不分支）；F2 歷史回放升為阻斷性可達性 AC；F3 定義家族與 bounded matrix，計數以初次 finding 為第一次；F4 維持單票並載明理由；F5 依賴欄補正；F6 AI-3 落點由路徑限定之 `ssot-governance.md`（frontmatter 為 `globs:`）改為無條件載入之 `.claude/CLAUDE.md`，刪除 Agent SDK 與 rules-residency 檢查；F7 常駐條文不列舉任何工具；F8 token 落點與硬體 Guard 排除；F9 HC 計數不含 AI-0；F10 AC-7 改驗 `ssot-governance.md` 零異動。**4a 計數＝1。**
**前置依賴（依賴三分）**：
- `execution_dependency`: RGR-AUTONOMOUS-CLOSURE-01（已落地之 session-local closure loop）；現行 `SKILL.md` severity table 與 Mechanical Verdict Rule
- `evidence_dependency`: TKR-PATHFIELD-01（保存之 L212、L227、L228 歷史漏檢證據）
- `coordination_dependency`: 無

> **檔案治理／來源說明**：`TKR-PATHFIELD-01`（PLT，CLOSED）暴露兩類 RGR 漏檢，經 SKAD 一手核驗 `docs/deliver/TKR-PATHFIELD-01_path-field.md`：①**狀態類壓縮**——L227 守衛以 `is not None` 判定，致顯式 `path: None` 靜默清空既有 `file`；L228 雙鍵同值更新繞過守衛；即 key absent／present-with-null／present-with-value 被壓成同一狀態。②**兄弟 AC 漏檢**——L212 明載「R1 劃除 AC-1307 時未連帶檢查引用同一不可達母體之其他 AC」，AC-1304 至下一輪方被發現。
>
> **三檔分工**：`SKILL.md` 承載條件式審查之觸發與檢查面；`REPAIR_ROUTING.md` 承載 gate execution 內之家族閉合；`.claude/CLAUDE.md` 承載 gate 外 AC 規範修訂後之兄弟 AC 重驗。第三者置於無條件載入之專案指令檔，因 TKR 之漏檢發生於 Traycer 回流後之 fix-forward，當下未呼叫 gate；且 `.claude/rules/ssot-governance.md` 之 frontmatter 為 `globs:`（限 PROGRESS 與 SSOT 檔），修訂 `Pending/` 票檔時不匹配。
>
> **單票理由（F4）**：兩類機制（gate 內審查／閉合、gate 外重驗）共用同一證據來源與同一歷史回放驗收，總量小；拆票將使同一回放被兩票重複引用，徒增一輪治理成本。
>
> **與既有條文之相容**：`ssot-governance.md` §Ticket Rigor Gate 明定 rigor gate 為按需工具、不得作為常駐前置。本票 AI-3 之義務為兄弟 AC 重驗，**不指定任何工具**，亦不修改 `ssot-governance.md`。

## Verify Config
TICKET_ID=RGR-MIGRATION-LITE-01
CREATED=2026-10-01
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/CLAUDE.md
FORBIDDEN=.claude/rules/,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/EXAMPLE.md,.claude/skills/ticket-rigor-gate/runner/,.claude/skills/ticket-rigor-gate/experience-cards/,app/,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=40
DIFF_REF=9f3722aa7c879c89ba6acdcd39e5461de8dd6359
ANCHOR_PROBED=2026-10-01 16:58
BASE_COMMIT=9f3722aa7c879c89ba6acdcd39e5461de8dd6359
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**開票時 live read**：HEAD=`9f3722a`，工作樹 clean。三個插入錨點各恰 1 處：`SKILL.md` `## Review Algorithm`（L256）；`REPAIR_ROUTING.md` `## Research Fallback`（L137）；`.claude/CLAUDE.md` `## SOP`（L118）。`.claude/CLAUDE.md` 無 frontmatter（Rev A 時 live read 確認）。

**`TARGET_BLOB=N/A` 依據**：開票端（Desktop）無合法 blob-hash producer；AI-0 由執行端以 `git hash-object` 記錄三檔現值於 receipt。依 TPL v3.11，本欄為資訊性診斷值，`N/A` 不豁免 scope 或 evidence 驗證。

## Action Items

### AI-0: 錨點與基線
- 確認三個 ALLOWED 檔案存在，且上列三個插入錨點各恰 1 處。
- 以 `git hash-object` 記錄三檔修改前 blob hash 於 receipt。
- 確認 `.claude/CLAUDE.md` 為本專案使用中之根指令檔（無 frontmatter 路徑限定）。
- 執行 `git check-ignore -v` 與工作樹基線檢查。
- 任一錨點不唯一或檔案不存在 → **在任何編輯之前**停止整張票並回報；不得以推定位置寫入，不得部分落地。
- [x] 完成 — 三檔存在且未被 ignore；三錨點各恰 1 處（SKILL.md L256／REPAIR_ROUTING.md L137／CLAUDE.md L118，與票面一致）；CLAUDE.md 確認無 frontmatter；工作樹 clean；blob hash：SKILL.md=5a48b66d／REPAIR_ROUTING.md=73bfe46e／CLAUDE.md=f4e81e44

### AI-1: SKILL.md 新增 Migration-Lite Review
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`（`## Review Algorithm` 正前方新增 `## Migration-Lite Review` 節）
- **目標值**（語意固定，≤15 行，不含範例矩陣）：
  - **觸發**：票面引入或改變欄位 aliasing、deprecation、normalization、migration、legacy-record preservation、create/update compatibility，或承諾零 mutation 且**管轄持久化或共享可變資料**之 rejection guard 時，啟動。無持久化資料或相容語意變更之純 presentation 變更，及不持久化資料之硬體 Guard，不觸發。
  - **Token 落點**：report §1 Verdict 下一行記 `MIGRATION_LITE=ACTIVE` 或 `MIGRATION_LITE=NOT_TRIGGERED`。
  - **啟動時檢查五面**：①實質相異之 stored-state 與 input 類別，於 key 是否存在影響行為時區分 absent、present-with-null、present-with-value；②各適用類別之 create/update accept-or-reject、持久化形狀、保留值與副作用；③production readers、writers、serializers、archive paths、APIs、UIs 逐一為已改、受明示相容行為保護，或揭露待決；④每條真實資料行為性 AC 指明可達之 population、source、entrypoint、observer；⑤無法經 normalized writer 建立之 legacy fixture，須於該邊界之下 seed 並於受測操作前斷言其形狀。
  - **裁決**：All identified Migration-Lite completeness gaps MUST be recorded as findings and MUST NOT be omitted merely because their expected impact appears limited. Severity and final disposition remain governed by the existing severity table and Mechanical Verdict Rule.
  - 等價類得合併；不要求 Cartesian-product artifact。Preflight PASS 表示規格可派工，不表示其未來實作已獲證明。
- [x] 完成 — `## Migration-Lite Review` 節已插入 `## Review Algorithm` 正前方，12 行（≤15），不含範例矩陣

### AI-2: REPAIR_ROUTING.md 新增 Migration-Lite Family Closure
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`（`## Research Fallback` 正前方新增 `### Migration-Lite Family Closure`）
- **目標值**（語意固定）：
  - **定義**：finding family＝共用同一 alias、state class 或 guard 契約之 finding 集合；bounded matrix＝以 AI-1 五面為軸、逐格列出該家族所有適用類別之處置表。
  - `TICKET_RIGOR` execution 內，修復 active Migration-Lite finding 後 MUST 重跑完整 active profile，並掃描共用受影響欄位契約、population、entrypoint 或 consumer 之兄弟 Action Items、constraints、error rules 與 ACs。
  - 被移除、縮減、豁免或修訂之 AC，須於重算 verdict 前重驗所有共用同一 population、source、entrypoint 或 contract assumption 之兄弟 AC。
  - **計數**：The initial Critical or Major finding is the first appearance of its family. On the first rerun after repair, any Critical or Major finding in the same family counts as the second appearance, including the original finding if it remains unresolved. 第二次出現 → 下一次修復 MUST 以 bounded matrix 閉合該家族，不得再加 example-specific branch。matrix 中須產品或政策裁決之格標 `L2-DECIDE`，以 `C_EXCEPTION` 回報。第三次修復迭代仍非 PASS → 依既有閉合上限停止，回報未解類別與受影響 AC。
  - 上述 session-local 修復不決定 SKAD 是否開 Rev、拆票、取代、作廢或替換本票。
- [x] 完成 — `### Migration-Lite Family Closure` 已插入 `## Research Fallback` 正前方（置於 `## General Autonomous Repair Default` 末段子節）

### AI-3: CLAUDE.md 新增 AC 修訂閉環
- **檔案**: `.claude/CLAUDE.md`（`## SOP` 正前方新增 `## Acceptance Criteria 修訂閉環` 節）
- **目標值**（逐字，繁體中文）：
  > Acceptance Criterion 遭移除、縮減或豁免，或其前置條件、預期結果被改變後，依賴舊有語意的審查結論即告失效，不得再引用為現行結論。
  > 恢復實作或下游審查前，修訂者 MUST 重驗共用相同資料母體、來源、入口或契約假設的兄弟 Acceptance Criteria。重驗方式不限，不因此使任何審查工具成為強制前置；結論 MUST 記錄於票檔。
  > 回執、勾選、完成註記、純錯字及其他非規範性修改不觸發本規則。本規則本身不改變 lifecycle、不建立 Rev、不拆票或新增票，也不計為新的外部審查輪次。
- 條文 MUST NOT 出現 fresh rigor-gate、得執行 rigor、應重跑 gate 或任何指定工具之字樣。
- [x] 完成 — `## Acceptance Criteria 修訂閉環` 已逐字插入 `## SOP` 正前方（commit `a4fc083`）；`grep` 確認條文零工具具名字樣。**修正**：初版誤將三行引文渲染為三段散文並多加一道 `---` 分隔線（10 行，落 154 行），與 R2 rigor gate 報告（`.tmp/rigor-gate/RGR_RGR-MIGRATION-LITE-01_20261001_R2.md` L70，Akira 已選 Option A）預先核可之「標題＋空行＋3 行 `>` 引文＋空行」共 6 行、恰 150 行不符；已修正為逐字 3 行 blockquote、移除多餘 `---`，commit `0b3585c` 核實現為 150 行（`wc -l`），與該檔自身宣告之「≤150 行」目標及 rigor gate 預核可形狀一致

## Hard Constraints
1. 僅修改 ALLOWED 三檔。
2. 不新增 runner、registry 欄位、Research API、依賴、report schema 欄位或獨立 artifact；report 僅新增 `MIGRATION_LITE=ACTIVE|NOT_TRIGGERED` 一個 token。
3. 不修改 `.claude/rules/` 下任何檔案；不將任何審查工具設為派工或審查之強制前置。
4. 不修改既有 Mechanical Verdict Rule、severity 判準、session-local 三次上限、Research Fallback 或 disposition 欄位；不建立 Migration-Lite 專屬之裁決分支。
5. 純 presentation 變更與不持久化資料之硬體 Guard 不得觸發 Migration-Lite。
6. 內容 AI 總數 ≤3（不含 AI-0）；需第四個內容步驟 → 停止回報。

## Error Handling
- AI-0 錨點歧義或檔案不存在 → 於任何編輯之前停止整張票並回報；不得部分落地。
- 任何文字使審查工具成為強制前置，或於 CLAUDE.md 條文中指名特定工具 → 停止，改寫為工具中立措辭。
- 任何改動波及 FORBIDDEN → 停止回報；不得以還原後當作未發生。
- 超出 `MAX_LINES` → 記錄實測值（遙測用途，非停止事由）。
- `git check-ignore -v <ALLOWED 三檔>` 任一命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** `SKILL.md` 定義 Migration-Lite 之窄觸發條件（rejection guard 限管轄持久化或共享可變資料），及純 presentation 變更與不持久化資料之硬體 Guard 不觸發之條件；token 落於 §1 Verdict 下一行。
- [x] **[存在性]** 啟動後要求 state/input classes、consumer closure、AC reachability 與 legacy fixture validity，並區分 absent／null／value；明定已識別缺口 MUST 記為 finding、裁決依既有 severity table 與 Mechanical Verdict Rule，無 Migration-Lite 專屬裁決分支。
- [x] **[存在性]** 規則允許合併等價類，且不要求建立完整 Cartesian-product artifact。
- [x] **[存在性]** `REPAIR_ROUTING.md` 定義 finding family 與 bounded matrix，要求修訂後重跑完整 active profile，並掃描同契約、同母體、同入口或同 consumer 之兄弟 AI／AC。
- [x] **[存在性]** 計數規則：初次 finding 為第一次，修復後首次 rerun 之同族 Critical／Major（含未解之原 finding）為第二次，第二次即切 bounded matrix；決策格標 `L2-DECIDE` 以 `C_EXCEPTION` 回報；第三次仍非 PASS 依既有上限停止。
- [x] **[存在性]** `.claude/CLAUDE.md` 明載：AC 遭移除、縮減或豁免，或其前置條件、預期結果被改變後，依賴舊語意之審查結論失效；恢復實作或下游審查前須重驗共享相同資料母體、來源、入口或契約假設之兄弟 AC；重驗方式不限、結論記錄於票檔；條文不指名任何工具，且不自動改 lifecycle、開 Rev、拆票或增加外部審查輪次。
- [x] **[可達性／歷史回放]** 將修訂後規則套用至保存之 `TKR-PATHFIELD-01` 歷史證據（`docs/deliver/TKR-PATHFIELD-01_path-field.md` 現存凍結票檔，含 T2 R1／R2／R4 回流紀錄，即票首所列 L212、L227、L228 及其所引 AC-1304、AC-1307；以人工規則對照執行，不另跑 gate），並確認：①alias／normalization／legacy-record 語意使 `MIGRATION_LITE=ACTIVE`；②更新輸入中之 `path` 被區分為 key absent、key present with null、key present with value；③AC-1307 因共用資料母體或入口假設失效而修訂時，同次重驗涵蓋 AC-1304。三項任一無法由修訂後規則導出即為 FAIL。結果以下列結構記於 Closeout：`SOURCE=` `TRIGGER_RESULT=` `STATE_CLASSES=` `AMENDED_AC=` `SIBLING_FOUND=` `RESULT=`。
- [x] **[範圍]** `git diff --name-only` 僅含三個 ALLOWED 檔案；`git diff --check` 通過；`.claude/rules/ssot-governance.md` 為零異動。
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)

## Closeout

**歷史回放驗收（AC「[可達性／歷史回放]」）**

```
SOURCE=mcp-ticket-platform repo docs/deliver/TKR-PATHFIELD-01_path-field.md（凍結票檔，Rev B，T2 R1/R2/R4 回流裁決節）；對應行號 L178(AC-1304)／L185(AC-1307 劃除)／L212(R2 回流裁決)／L227-230(R4 回流裁決)
TRIGGER_RESULT=ACTIVE — 票涉及 file/path 欄位 aliasing（AI-1 schema 契約定義路徑欄）、讀取期 normalization（AI-2 讀寫正規化）、legacy path 欄保留（MUST NOT 刪除既有 path）、create/update compatibility（既有票更新一律拒收 path 鍵）；非純 presentation 變更，符合 SKILL.md Migration-Lite Review 觸發定義
STATE_CLASSES=R1 原始守衛以 `alias_update_value is not None` 為哨兵，未區分 key absent 與 key present-with-null——呼叫端顯式傳入 `path: None`（L227）時守衛不觸發，既有 file 值遭靜默清空（資料遺失，T2 R4 一手重現於 `131f684`）；亦未以 key 存在性判定 present-with-value 同值雙鍵情境（L228，`file`+`path` 同值更新繞過 alias 檢查，等同未經檢查直寫 file）。T2 R4 收斂規則改為「鍵存在即拒，不論其值」——即改以 key-presence 判定，與 Migration-Lite Review facet①「key 存在影響行為時，MUST 區分 absent／present-with-null／present-with-value」要求之判準一致；套用本票新規則會要求 AI-1 明確列出並驗證此三態，產出與 R4 實際修法相同之結論
AMENDED_AC=AC-1307（T2 Round 1 劃除／標記不可達，因前提消滅：archived-only 票不經 list_tickets／board 公開入口可見，見票面 L185、L198）
SIBLING_FOUND=AC-1304（及其延伸 AC-1313）與 AC-1307 共用同一母體（僅 path 之歸檔票）與同一入口假設（經 list_tickets／board 可達）。R1 劃除 AC-1307 時未重驗 AC-1304，致 AC-1304 之不可達前置延續至 T2 R2 方被發現（L212：「R1 劃除 AC-1307 時未連帶檢查引用同一不可達母體之其他 AC，為本輪殘留之成因」）。套用本票 REPAIR_ROUTING.md 新訂 Migration-Lite Family Closure 與 CLAUDE.md 新訂「AC 修訂閉環」條款——AC-1307 於 R1 遭移除時，兩條款皆要求同次重驗共用同一母體／入口假設之兄弟 AC（即 AC-1304）——會在 R1 當下即導出與 T2 R2 相同之結論（AC-1304 前置須改為主冊 fixture 重現），較實際歷史提早一輪抓出
RESULT=PASS — 三項（①②③）皆可由修訂後規則導出；歷史缺口（R1 未連帶重驗 AC-1304）為修訂前規則之已知漏洞，修訂後規則之條文明文要求涵蓋此情境
```
