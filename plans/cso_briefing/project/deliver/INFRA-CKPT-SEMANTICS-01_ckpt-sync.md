# INFRA-CKPT-SEMANTICS-01: checkpoint 生命週期語義對齊 PLT 契約（四要件④／機械強制方向／schema 註解）

**簽發日期**: 2026-09-09
**簽發者**: Claude（SMTR）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（純治理文本同步，無 runtime 行為變更）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `INFRA-LIFECYCLE-SSOT-01`（CLOSED，`deliver/`）——D-1 上位裁決「PLT／TKR 為 lifecycle、狀態轉換與 registry 資料契約之唯一語義權威；APP 採相同核心語義但最小 consumer 實作」為本票全部修訂之法源
- `coordination_dependency`: 無

## 檔案治理／來源說明

**開票成因（SMTR 自承誤判之更正）**：本 session 於三張票 `REVIEW→CLOSED` 轉態時，觀察到 `registry_set_status(lifecycle=CLOSED)` 回傳 `checkpoint: null`，SMTR 當下判為「平台缺陷、幾近永久資料遺失」，並據此於三票 `closed_at_smtr` 欄與母票 `INFRA-PREFLIGHT-CORE-01` Rev D.1 寫入「平台缺陷實證」。**該判定錯誤。**

**一手核實（PLT repo）**：
- `docs/deliver/archive/OTHERS/tool-contracts.md:646`（`aud_20260909_0974837b89`）：「`lifecycle` 目標為 `CLOSED` 時：現存 `checkpoint.state == "OPEN"` 拒收；轉態成功後 `checkpoint` 清為 `null` 並嘗試刪除 `.tmp/checkpoints/{ticket_id}.md`（best-effort，刪除失敗僅回傳 `warning`，不回滾已完成的 registry 寫入）。」
- `core/services/registry_service.py` L895 附近（`aud_20260909_4e4b172848`）：程式碼註解逐字為 `# Check R2 mechanical checks: CLOSED clears checkpoint & deletes detail file`，並明載權限檢查先於 registry 寫入、unlink 為 best-effort follow-up。
- 同檔 `tool-contracts.md:664`：`detail_content` 寫入 `.tmp/checkpoints/{ticket_id}.md`，性質為 **transient 工作檔**，`CLOSED` 轉態時由 `registry_set_status` unlink。

**即：清空 checkpoint 與刪除 detail 檔均為 PLT 文件化之刻意設計（R2 mechanical check），非缺陷。** 同理，SMTR 先前判定 `registry_set_checkpoint(detail_content=...)` 為「假成功、未落檔」亦為誤判——該檔實際落於 `D:/DevSources/workflow/mcp-ticket-platform/.tmp/checkpoints/INFRA-PREFLIGHT-CORE-01.md`（4,535 bytes，`aud_20260909_7839be9eaf`），落點在 **PLT repo** 而非 APP 專案目錄。

**誤判之根因（可稽核，非托辭）**：SMTR 依 `TICKET_REGISTRY.json` 第 9 行 schema 註解所載之 `detail_ref: reports/checkpoints/<TICKET_ID>.md` 於 APP repo 搜尋，全 repo 零命中，遂判「假成功」。該註解**路徑過期且性質描述缺漏**（實為 `.tmp/checkpoints/`，且為 transient）。SMTR 未依 `Instructions_for_Claude`「檔案存取速查」明載之「深入參照 `tool-contracts.md`」讀取契約原文——**判準在盤而未被讀取**，此即 `Extreview_Trigger_Hysteresis_And_QueryPack` 教訓 1 之同型復現（該教訓原針對觸發條件，本次落在契約查證）。

**本票要修的是 APP 端三處與 PLT 契約牴觸／過期之治理文本**，不是平台。PLT 無需異動。

**待修三處（逐處附行號與衝突內容）**：

| # | 位置 | 現況 | 與 PLT 契約之衝突 |
|---|---|---|---|
| ① | `ssot-governance.md` L118 四要件④ | 「`checkpoint.resolution` 寫入收尾實錄」列為終態轉換之必要條件 | `set_status(CLOSED)` 會清空整個 `checkpoint`。依現行字面，四要件④於 `CLOSED` 票**不可能穩定成立**——先寫後轉態必被清空，先轉態後寫則四要件之「原子性」（L119「同一收尾回合內完成」）字面成疑 |
| ② | `ssot-governance.md` L124 | 「真正之機械強制須由 mcp-ticket-platform server 端提供（`lifecycle` 轉終態時**強制 `checkpoint` 非空**、`file` 指標存在性校驗）」 | **方向與 PLT 設計相反**。PLT 於轉終態時**清空** checkpoint 並刪 detail 檔。此條若不修，將導向一張與 PLT 資料契約牴觸之平台需求票 |
| ③ | `TICKET_REGISTRY.json` 第 9 行 `_schema` 註解 | `detail_ref: reports/checkpoints/<TICKET_ID>.md` | 實為 `.tmp/checkpoints/{ticket_id}.md`，且為 transient 工作檔、`CLOSED` 時 unlink。**本票 MUST NOT 直改該檔**（`Instructions_for_Claude`：「禁以任何通道直改 `TICKET_REGISTRY.json`」），僅於 `ssot-governance.md` 記載其過期並指明契約原文位置 |

**耐久落點之正解（本票核心語義）**：收尾實錄之耐久載體為**票檔＋vault 決策檔**；`checkpoint` 為**收尾前之工作載體**，於 `CLOSED` 轉態時依設計清空。此與同檔 L133 既有原則同構（「耐久結論唯一載體為票面／冊面收尾實錄」，`.tmp/` 產物不得作長期證據）——`.tmp/checkpoints/` 正是 `.tmp/` 產物。

## Verify Config
TICKET_ID=INFRA-CKPT-SEMANTICS-01
CREATED=2026-09-09
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md
FORBIDDEN=plans/cso_briefing/project/TICKET_REGISTRY.json,.claude/skills/**,.claude/settings.json,.claude/CLAUDE.md,app/**,tests/**
MAX_LINES=N/A（純治理文本同步票，失敗形狀為審查發現式，不可先驗估算；比例原則符合 docs preset）
DIFF_REF=416c711
ANCHOR_PROBED=2026-09-09 15:40
BASE_COMMIT=416c71193f751f332a3053183797765e0cb99dc7
TARGET_BLOB=N/A（docs preset 且屬治理文本定點行級替換，V1 降級為 AI-0 錨點制，依 TPL v3.10 比例原則）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點復驗（唯讀，修改前）
- **檔案**: `.claude/rules/ssot-governance.md`、PLT `docs/deliver/archive/OTHERS/tool-contracts.md`
- **動作**:
  1. `ssot-governance.md` L118 含「④`checkpoint.resolution` 寫入收尾實錄」字面唯一命中
  2. `ssot-governance.md` L124 含「強制 `checkpoint` 非空」字面唯一命中
  3. **PLT 契約復驗**：`tool-contracts.md` 含「轉態成功後 `checkpoint` 清為 `null`」與「`.tmp/checkpoints/{ticket_id}.md`」二處字面；`registry_service.py` 含 `updated_ticket["checkpoint"] = None` 於 `set_status` 內
  4. `INFRA-LIFECYCLE-SSOT-01` D-1「PLT 為唯一語義權威」條款仍在盤未被推翻（法源時效復驗）
- **任一項不符 → 停止回報，禁自行推定等價位置。** 特別是動作 3：若 PLT 契約已變更（例如改為保留 checkpoint），本票全部前提消滅，MUST 停止升裁而非改寫本票
- [x] 完成 — 動作1：L118「④checkpoint.resolution 寫入收尾實錄」唯一命中；動作2：L124「強制checkpoint非空」唯一命中；動作3：PLT `tool-contracts.md:646/654/664/719`＋`registry_service.py:888,895`（`updated_ticket["checkpoint"] = None`）全數逐字核實；動作4：`INFRA-LIFECYCLE-SSOT-01` D-1 條款於本檔翻盤紀錄（L143）仍在盤未被推翻；check-ignore 未命中

### AI-1: 四要件④改寫——耐久落點與時序
- **檔案**: `.claude/rules/ssot-governance.md`（L118）
- **現值**: `④checkpoint.resolution 寫入收尾實錄（含 co...）`
- **目標值**: 改寫為——收尾實錄之**耐久載體為票檔（`deliver/`）＋vault 決策檔**；`checkpoint` 為收尾前工作載體，依 PLT 契約於 `CLOSED` 轉態時清為 `null` 並 unlink `.tmp/checkpoints/{ticket_id}.md`。四要件④之要求改為：**轉終態前**，收尾實錄 MUST 已落票檔；若需保留冊面可見之收尾摘要，MUST 於 `set_status(CLOSED)` **之後**以 `registry_set_checkpoint` 寫入（PLT `AC-506` 證此序合法：CLOSED 票執行 `set_checkpoint` 僅更新 `updated_at`，`closed_at` 不變）
- **MUST** 同時修訂 L119「原子性」條款，明載四要件④於 `CLOSED` 情形之合法時序為「①②③ → 轉態 → ④回填」，該序**不違反**「同一收尾回合內完成」
- **MUST NOT** 改動四要件①②③之任何字面
- [x] 完成 — L118 ④已改寫（耐久載體＝票檔＋vault；checkpoint 為工作載體，CLOSED 時依 PLT 契約清空）；L119 原子性條款已附 `CLOSED` 合法時序句；①②③字面逐字未動（`git diff` 佐證）

### AI-2: 機械強制方向更正
- **檔案**: `.claude/rules/ssot-governance.md`（L124）
- **現值**: 「真正之機械強制須由 mcp-ticket-platform server 端提供（`lifecycle` 轉終態時強制 `checkpoint` 非空、`file` 指標存在性校驗）」
- **目標值**: 刪除「強制 `checkpoint` 非空」之訴求（與 PLT R2 mechanical check 相反）；保留「`file` 指標存在性校驗」之訴求（與 PLT 契約無衝突）；改以「轉終態前 `file` 欄指向之票檔 MUST 實際存在」表述。**MUST** 附一句說明：`checkpoint` 於終態被清空為 PLT 刻意設計，APP 不得反向要求
- **MUST NOT** 改動同段「閘門位置與動作發生位置不重合」之誠實定性（該定性正確且與本票無關）
- [x] 完成 — 「強制`checkpoint`非空」訴求已刪除；`file`指標存在性校驗訴求保留並改寫為「轉終態前file欄指標所指票檔MUST實際存在」；已附一句說明checkpoint清空為PLT刻意設計；「閘門位置與動作發生位置不重合」定性逐字未動

### AI-3: 新增「checkpoint 生命週期與 detail 檔」小節
- **檔案**: `.claude/rules/ssot-governance.md`（置於「APP 端專屬約束」L108 之後）
- **目標值**: 新增小節，內容限下列五點，逐點附 PLT 契約出處：
  1. `checkpoint` 為**工作載體非耐久載體**；`state=OPEN` 時 `CLOSED` 轉態被拒；轉態成功後清為 `null`
  2. `detail_content` 落 **PLT repo** `.tmp/checkpoints/{ticket_id}.md`（**非** APP 專案目錄），性質 transient，`CLOSED` 時 unlink（best-effort，失敗僅回 `warning`）
  3. **`TICKET_REGISTRY.json` `_schema` 註解之 `detail_ref: reports/checkpoints/<TICKET_ID>.md` 為過期字面**，實際落點見第 2 點；契約原文以 PLT `tool-contracts.md` 為準。**本檔不得直改 `TICKET_REGISTRY.json`**（`Instructions_for_Claude` 明禁），僅此記載
  4. 承 L133 原則：`.tmp/` 產物不得作長期證據；`.tmp/checkpoints/` 屬之
  5. **誤判防護**：查證 registry 工具行為時 MUST 讀 PLT `tool-contracts.md` 原文，**MUST NOT** 以 APP 端 schema 註解或工具回傳值形狀推定契約
- **MUST NOT** 於本小節新增任何 `lifecycle` 值枚舉、狀態轉換規則或工具參數規格（PLT 為語義權威，APP 僅記錄 consumer 面事實）
- [x] 完成 — 新節「## checkpoint 生命週期與 detail 檔」已插入「APP 端專屬約束」之後，五點齊備各附 PLT 出處（`tool-contracts.md:646/654/664/719`、`registry_service.py:888-895`）；第3點明載 schema 註解過期且禁直改 `TICKET_REGISTRY.json`；零 lifecycle 枚舉/狀態轉換規則新增

### AI-4: 誤判記載更正（冊面與母票）
- **檔案**: 冊面（`registry_set_checkpoint`）＋ `deliver/INFRA-PREFLIGHT-CORE-01_Preflight_Decision_Core_Extraction.md`
- **動作**:
  1. 三張本 session 轉 CLOSED 之票（`INFRA-PENDING-HYGIENE-APP-01`／`INFRA-REGISTRY-LIFECYCLE-GATE-APP-01`／`INFRA-LIFECYCLE-SSOT-01`）之 `closed_at_smtr` 欄含「⚠️ 平台缺陷實證」字樣者，**MUST** 以 `registry_set_checkpoint` 新增 `smtr_correction` 欄更正為：「該判定錯誤；`set_status(CLOSED)` 清空 checkpoint 為 PLT 文件化設計（`tool-contracts.md:646`、`registry_service.py` R2 mechanical check），非缺陷。轉態前備份、轉態後回填之作法仍為正確實務，理由由『防缺陷』更正為『依設計保留耐久記載』。」
  2. 母票 Rev D.1 之「`detail_content` 假成功」段落 **MUST** 加註更正：實際落於 PLT repo `.tmp/checkpoints/`（4,535 B，`aud_20260909_7839be9eaf`），為 transient 設計非假成功；SMTR 當時依過期 schema 註解於 APP repo 搜尋而誤判
- **MUST NOT** 刪除原誤判記載——保留原文＋更正註記（如實標示誤記，同 `INFRA-COMPACT-CLOSURE-GATE-APP-01` Rev D 之 F6' 處理方式）
- **⛔ 更正三票 checkpoint MUST 先 `registry_list fields=["checkpoint"]` 備份**，寫入後 read-back 驗證（該三票已 CLOSED，`set_checkpoint` 於 CLOSED 票合法且不改 `closed_at`）
- [x] 完成 — 三票（`INFRA-PENDING-HYGIENE-APP-01`／`INFRA-REGISTRY-LIFECYCLE-GATE-APP-01`／`INFRA-LIFECYCLE-SSOT-01`）均先 `registry_list fields=["checkpoint"]` 備份，再以 `registry_set_checkpoint(checkpoint_data={"smtr_correction":...})` 新增更正欄，寫入後 read-back 逐一確認原 `closed_at_smtr` 等既有欄位逐字保留、`closed_at` 未變；母票 `INFRA-PREFLIGHT-CORE-01` Rev D.1「假成功」段落已加註更正（原文保留未刪）

## Hard Constraints
1. **禁直改 `TICKET_REGISTRY.json`**（任何通道）；③之處置僅為於 `ssot-governance.md` 記載其過期
2. **禁修改 PLT repo 任何檔案**——本票之結論為 PLT 無需異動；若執行中認為 PLT 確有缺陷 → 停止回報升裁，禁自行開 PLT 票
3. **禁新增 `lifecycle` 枚舉、狀態轉換規則或工具參數規格**（PLT 為唯一語義權威，`INFRA-LIFECYCLE-SSOT-01` D-1）
4. **禁改動四要件①②③、L119 除時序句以外之字面、L124 之「閘門位置不重合」誠實定性、L133 原則**
5. AI-4 之更正 **MUST NOT** 刪除原誤判記載；保留原文＋更正註記
6. 文本異動與冊面異動 **MUST** 分屬不同 commit（`[INFRA-CKPT-SEMANTICS-01]` 與 `[GOVERNANCE-SYNC]`），承 Rule 3
7. 中途發現既知未達項依 D.3-2 僅連結既有位置，禁升裁

## Error Handling
- AI-0 任一錨點不符 → 依 TPL v3.10 三分流分類回報，禁單一 STOP 無分類
- AI-0 動作 3 顯示 PLT 契約已變更 → **停止升裁**，本票全部前提消滅，禁改寫本票續作
- AI-4 三票任一 checkpoint 備份失敗或 read-back 不一致 → 停止回報，禁盲重試（避免覆蓋既有敘事）
- **[標配]** `git check-ignore -v .claude/rules/ssot-governance.md` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** `ssot-governance.md` 四要件④已改寫為「耐久載體＝票檔＋vault；`checkpoint` 為工作載體，`CLOSED` 時依 PLT 契約清空」，且 L119 已載明 `CLOSED` 情形之合法時序（①②③→轉態→④回填）；原「`checkpoint.resolution` 寫入收尾實錄」作為終態必要條件之字面命中＝0
- [x] **[存在性]** L124「強制 `checkpoint` 非空」字面命中＝0；`file` 指標存在性校驗訴求保留；「閘門位置與動作發生位置不重合」定性逐字未動（`git diff` 佐證）
- [x] **[存在性]** 新增小節五點齊備，每點附 PLT 契約出處（`tool-contracts.md` 行號或 `registry_service.py` 函式名）；第 3 點明載 `TICKET_REGISTRY.json` schema 註解過期且不得直改
- [x] **[行為性]（誤判更正）** given 三張 CLOSED 票之 checkpoint，when 執行 `registry_list fields=["checkpoint"]`，expect 各含 `smtr_correction` 欄且原 `closed_at_smtr` 原文逐字保留；母票 Rev D.1 段落含更正註記且原文未刪 —— 三票 read-back 實測皆含 `smtr_correction`，`closed_at_smtr` 逐字未變（見 AI-4 收尾記錄）；母票段落已加註且原文保留
- [x] **[存在性]** 四要件①②③、L133 原則逐字未動（`git diff` 佐證）
- [x] **[存在性]** 文本 commit 與冊面 `[GOVERNANCE-SYNC]` commit 分離；`git diff --stat` 各自僅涉其範圍 —— `.claude/rules/ssot-governance.md` 以 `[INFRA-CKPT-SEMANTICS-01]` 獨立 commit；母票 `deliver/INFRA-PREFLIGHT-CORE-01_...md` 更正以 `[GOVERNANCE-SYNC]` 獨立 commit；三票 checkpoint 更正經 MCP `registry_set_checkpoint`（非 git diff）
- [x] `assembleDebug` PASS (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`.claude/rules/ssot-governance.md`）

---

## 收尾實錄（耐久載體＝本票檔；依本票新立四要件④，轉終態前落檔）

**碼閉環（2026-09-09）**：AI-0～AI-4 全數完成，AC 8/8 全勾。

**Commit 鏈（`git_log` 一手取證，`aud_20260909_caac5fea56`）**：
| commit | 時間 | 內容 |
|---|---|---|
| `c167357` | 20:40:00 | `[INFRA-CKPT-SEMANTICS-01]` checkpoint 生命週期語義對齊 PLT 契約（`.claude/rules/ssot-governance.md`，ALLOWED 唯一檔） |
| `2e7271a` | 20:40:07 | `[GOVERNANCE-SYNC]` 母票 `INFRA-PREFLIGHT-CORE-01` Rev D.1 誤判更正：`detail_content` 非假成功 |
| `689f6c4` | 20:44:18 | `[INFRA-CKPT-SEMANTICS-01]` PROGRESS.md Dashboard 同步：碼閉環 |
| `0b39db5` | 20:47:58 | Codex R1 `ADVISORY`：澄清 `CLOSED` 合法時序措辭 |
| `0092c27` | 20:50:23 | Codex R2 `REPORT_FIX`：消除四要件④與 checkpoint 回填必選／可選語義衝突 |
| `e866197` | 20:52:17 | `[GOVERNANCE-SYNC]` 結案：票檔搬 `deliver/` |

**審查終值**：`/qa` PASS；`/sv` Cross-Review R1 `ADVISORY` → R2 `REPORT_FIX` → R3 **PASS**。`reviews={qa:PASS, sv:PASS, codex:PASS, traycer:N_A}`——`traycer=N_A` 之豁免依據：純治理文本票，無 runtime 行為變更、零 `.kt`，票面 §Traycer Workflow 明示 N/A（非推定 PASS，撤警正向取證律遵循）。

**AI-4 誤判更正落地**：三張 CLOSED 票（`INFRA-PENDING-HYGIENE-APP-01` seq 30／`INFRA-REGISTRY-LIFECYCLE-GATE-APP-01` seq 34／`INFRA-LIFECYCLE-SSOT-01` seq 35）均先備份 checkpoint，再以 `registry_set_checkpoint(checkpoint_data={"smtr_correction": ...})` 新增更正欄；read-back 逐一確認原 `closed_at_smtr` 等既有欄位逐字保留、`closed_at` 未變。母票 `INFRA-PREFLIGHT-CORE-01` Rev D.1 段落已加註更正且原文保留不刪。

**SMTR 終審覆核（2026-09-09，SKAD session；非採信自述）**：
- `git_log`（`aud_20260909_caac5fea56`）：上表六 commit 逐一命中，時序一致。
- `git_diff` `2568333..e866197`（`aud_20260909_0b0efaa256`／`687a365d05`）：波及四檔——`ssot-governance.md`(+14/-5)、本票檔(+122)、母票更正(+1)、`PROGRESS.md`(Dashboard 慣例，依 INFRA-GOVSYNC-01 不計入 ALLOWED 違規)。L118 四要件④／L119 原子性時序句／L124 機械強制方向／新節五點字面逐一核實；①②③與 L133 逐字未動。
- `registry_list`（`aud_20260909_129edba443`）：seq 30／34／35 三票 `smtr_correction` 欄均在盤，`closed_at_smtr` 原文完整。

**本票自身之四要件④首例適用（EVIDENCE_GAP 補證，非 SPEC_DEFECT）**：終審發現收尾實錄原僅存冊面 `checkpoint.closure_record`，而該欄依 PLT 契約於 `set_status(CLOSED)` 轉態時清為 `null`；若逕行轉態，本票將成為自身新立規則之首例違反。故依合法時序於**轉態前**將實錄落本票檔（本節），冊面摘要於轉態後回填。此為分類 `EVIDENCE_GAP`（補證即可，未改任何 `R-n`／AC／HC），依停止半徑律不開 Rev、不新增 AI、不計入 4a。

**待補版控同步**：本節為 Desktop 治理 session 以 `write_file_safe mode=append` 寫入，尚未 commit。Claude Code 端 MUST 以獨立 `[GOVERNANCE-SYNC] INFRA-CKPT-SEMANTICS-01 收尾實錄落票檔` commit 收束，並以 scoped `git_diff` 驗證僅涉本票檔。
